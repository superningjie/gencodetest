package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.container.Container;
import kd.bos.form.control.Control;
import kd.bos.form.events.LoadCustomControlMetasArgs;
import kd.bos.form.events.OnGetControlArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.metadata.form.Border;
import kd.bos.metadata.form.Margin;
import kd.bos.metadata.form.Padding;
import kd.bos.metadata.form.Style;
import kd.bos.metadata.form.container.FlexPanelAp;
import kd.bos.metadata.form.control.ImageAp;
import kd.bos.metadata.form.control.LabelAp;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hrmp.hrobs.business.domain.service.portal.IAppConfigService;
import kd.hrmp.hrobs.formplugin.utils.HrobsPageUtil;
import kd.hrmp.hrobs.formplugin.utils.ShowFormUtils;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.hrmp.hrobs.formplugin.util.DynamicObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @Date 2023/6/7 18:00
 * @Description 门户PC端-应用卡片-更多
 * @Demander xxx
 * @Document
 * @Basedata tdkw_hrobs_pc_appcard_ext、tdkw_hrobs_pc_morecard
 * @Version 1.0
 **/
public class MoreCardFormPlugin extends AbstractFormPlugin {
    @Override
    public void loadCustomControlMetas(LoadCustomControlMetasArgs args) {
        super.loadCustomControlMetas(args);
        this.buildAllApp(args);
    }

    @Override
    public void onGetControl(OnGetControlArgs args) {
        super.onGetControl(args);
        String key = String.valueOf(args.getKey());
        if (key.startsWith("appflex")) {
            Container container = new Container();
            container.setKey(key);
            container.setView(this.getView());
            container.addClickListener(this);
            args.setControl(container);
        }
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        String key = source.getKey();
        if (key.startsWith("appflex")) {
            this.showPcView(key);
        }
    }

    /**
     * @author xxx
     * @Description 加载所有应用
     * @Date 2023/6/8 10:41
     */
    private void buildAllApp(LoadCustomControlMetasArgs args) {
        DynamicObject hrobs_card = DynamicObjectUtils.findDynamicObjectByKey("hrobs_card", "pcbindform.number", "tdkw_hrobs_pc_morecard", null, null);
        if (ObjectUtils.isEmpty(hrobs_card)) {
            return;
        }
        // 卡片id
        Long cardId = (Long) hrobs_card.getPkValue();
        FormShowParameter formShowParameter = (FormShowParameter)args.getSource();
        // 卡片布局id
        Long cardLayoutId = formShowParameter.getCustomParam("cardLayoutId");
        Long userId = UserServiceHelper.getCurrentUserId();
        IAppCache appCache = AppCache.get("hssc");
        if (ObjectUtils.isEmpty(cardLayoutId)){
            String cardLayoutIdSt = appCache.get("hrobs_schemelayout_cardLayoutId", String.class);
            if (ObjectUtils.isNotEmpty(cardLayoutIdSt)) {
                cardLayoutId = Long.parseLong(cardLayoutIdSt);
            }
        }else {
            appCache.put("hrobs_schemelayout_cardLayoutId", cardLayoutId);
        }


        if (ObjectUtils.isNotEmpty(cardId) && ObjectUtils.isNotEmpty(cardLayoutId)) {
            List<Map<String, Object>> appConfigList = IAppConfigService.getInstance().getCardLayoutApps("0", cardId, cardLayoutId);
            if (appConfigList != null && !appConfigList.isEmpty()) {
                FlexPanelAp appListFlex = new FlexPanelAp();
                appListFlex.setKey("contentflexpanelap");
                String appId = formShowParameter.getAppId();
                String view = "47150e89000000ac";
                for (int i = 0; i < appConfigList.size(); ++i) {
                    Map<String, Object> appConfig = appConfigList.get(i);
                    if ("0".equals(appConfig.get("appsource"))) {
                        boolean checkPermission = PermissionServiceHelper.checkPermission(userId, appId, (String) appConfig.get("formId"), view);
                        if (!checkPermission) {
                            continue;
                        }
                    }
                    FlexPanelAp appFlex = this.getAppFlex(appConfig, i);
                    appListFlex.getItems().add(appFlex);
                }
                this.addFlexPanel("contentflexpanelap", appListFlex, args);
            }
        }
    }

    /**
     * @author xxx
     * @Description 打开单据页面
     * @Date 2023/7/19 9:36
     */
    private void showPcView(String flexKey) {
        String[] splitStr = flexKey.split("#");
        String formId = splitStr[1];
        if (formId.equalsIgnoreCase("formId")) {
            this.getView().showErrorNotification(ResManager.loadKDString("未设置应用处理页面，无法跳转", "PcCommonAppPlugin_1", "hrmp-hrobs-formplugin", new Object[0]));
        } else {
            String appSource = splitStr[2];
            String modelType = splitStr[3];
            Long appConfigId = Long.valueOf(splitStr[4]);
            if (HRStringUtils.equals(appSource, "0")) {
                String showType = "0";
                FormShowParameter formShowParameter;
                switch (modelType) {
                    case "baseformmodel":
                        formShowParameter = ShowFormUtils.buildBaseModel(showType, formId);
                        break;
                    case "billformmodel":
                        formShowParameter = ShowFormUtils.buildBillModel(showType, formId);
                        break;
                    case "querylistmodel":
                        formShowParameter = ShowFormUtils.buildQueryListFormModel(formId);
                        break;
                    default:
                        formShowParameter = ShowFormUtils.buildDynamicModel(formId);
                }
                formShowParameter.setCustomParam("tag","tag");
                this.getView().getFormShowParameter().getOpenStyle().setShowType(ShowType.Default);
                this.getView().showForm(formShowParameter);
                this.getView().close();
            } else if (HRStringUtils.equals(appSource, "1")) {
                Map<String, Object> map = IAppConfigService.getInstance().getAppConfigById(appConfigId, "0");
                this.getView().openUrl(String.valueOf(map.get("entryentity.url")));
            }

        }
    }

    private FlexPanelAp getAppFlex(Map<String, Object> appConfigDy, int index) {
        Margin margin = new Margin();
        margin.setTop("22px");
        margin.setLeft("20px");
        StringBuilder apKey = new StringBuilder();
        apKey.append("appflex").append("#").append(appConfigDy.get("formId")).append("#").append(appConfigDy.get("appsource")).append("#").append(appConfigDy.get("modeltype")).append("#").append(appConfigDy.get("id"));
        FlexPanelAp appFlex = HrobsPageUtil.customFlexPane(apKey.toString(), margin, (Padding) null, (Border) null, "96px", "74px");
        HrobsPageUtil.setFlexGrowAndShrink(appFlex, 0, 1);
        HrobsPageUtil.setFlexDirection(appFlex, "column", false, "center", "center");
        appFlex.setBackColor("#ffffff");
        appFlex.setClickable(true);
        ImageAp imageAp = this.buildImageAp((String) appConfigDy.get("systemicon"), index);
        LabelAp titleLabel = this.buildAppTitleLabel(new LocaleString(appConfigDy.get("name").toString()), index);
        appFlex.getItems().add(imageAp);
        appFlex.getItems().add(titleLabel);
        return appFlex;
    }

    private void addFlexPanel(String id, FlexPanelAp flexPanelAp, LoadCustomControlMetasArgs args) {
        Map<String, Object> params = new HashMap(16);
        params.put("id", id);
        params.put("items", flexPanelAp.createControl().get("items"));
        args.getItems().add(params);
    }

    /**
     * 标准产品图标48*48，现改成35*35，和主页面保持一致
     * @param imageUrl
     * @param index
     * @return
     */
    private ImageAp buildImageAp(String imageUrl, int index) {
        ImageAp imageAp = new ImageAp();
        imageAp.setImageKey(imageUrl);
        imageAp.setKey("imagekey" + index);
        imageAp.setHeight(new LocaleString("35px"));
        imageAp.setWidth(new LocaleString("35px"));
        return imageAp;
    }

    private LabelAp buildAppTitleLabel(LocaleString title, int index) {
        Style style = new Style();
        Margin margin = new Margin();
        margin.setTop("6px");
        style.setMargin(margin);
        LabelAp titleLabelAp = HrobsPageUtil.customLabel("labelname" + index, title, 12, "#666666");
        titleLabelAp.setStyle(style);
        return titleLabelAp;
    }
}