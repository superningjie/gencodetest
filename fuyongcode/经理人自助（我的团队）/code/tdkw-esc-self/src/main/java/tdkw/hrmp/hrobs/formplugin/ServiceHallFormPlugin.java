package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.OperationStatus;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
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
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hrmp.hrobs.business.domain.service.portal.IAppConfigService;
import kd.hrmp.hrobs.formplugin.utils.HrobsPageUtil;
import kd.hrmp.hrobs.formplugin.utils.ShowFormUtils;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kd.hrmp.hrobs.formplugin.utils.ShowFormUtils.buildFormShowParameter;

/**
 * @author xxx
 * @Date 2023/7/12 14:40
 * @Description 办事大厅 表单插件
 * @Demander xxx
 * @Document https://www.kdocs.cn/l/chxbmD4Vzaw0
 * @Basedata tdkw_hrobs_servicehall
 * @Version 1.0
 **/
public class ServiceHallFormPlugin extends AbstractFormPlugin {
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
        FormShowParameter formShowParameter = (FormShowParameter) args.getSource();
        String cardIdStr = formShowParameter.getOpenStyle().getCustParam().get("cardId");
        String cardLayoutIdStr = formShowParameter.getOpenStyle().getCustParam().get("cardLayoutId");
        if (!StringUtils.isBlank(cardIdStr) && !StringUtils.isBlank(cardLayoutIdStr)) {
            Long cardId = Long.valueOf(cardIdStr);
            Long cardLayoutId = Long.valueOf(cardLayoutIdStr);
            List<Map<String, Object>> appConfigList = IAppConfigService.getInstance().getCardLayoutApps("0", cardId, cardLayoutId);
            if (appConfigList != null && !appConfigList.isEmpty()) {
                FlexPanelAp appListFlex = new FlexPanelAp();
                appListFlex.setKey("contentflexpanelap");
                long userId = RequestContext.get().getCurrUserId();
                String appId = formShowParameter.getAppId();
                String view;

                for (int i = 0; i < appConfigList.size(); ++i) {
                    Map<String, Object> appConfig = appConfigList.get(i);
                    String params_tag = (String) appConfig.get("entryentity.params_tag");
                    JSONObject jsonObject = JSONObject.parseObject(params_tag);
                    if (ObjectUtils.isNotEmpty(jsonObject) && ObjectUtils.isNotEmpty(jsonObject.getString("permissionItem"))) {
                        view = jsonObject.getString("permissionItem");
                    } else {
                        view = "47156aff000000ac";
                    }
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
     * @Description 跳转单据
     * @Date 2023/7/18 14:15
     */
    private void showPcView(String flexKey) {
        String[] splitStr = flexKey.split("#");
        String formId = splitStr[1];
        if (formId.equalsIgnoreCase("formId")) {
            this.getView().showErrorNotification(ResManager.loadKDString("未设置应用处理页面，无法跳转", "PcCommonAppPlugin_1", "hrmp-hrobs-formplugin", new Object[0]));
        } else if (formId.equalsIgnoreCase("tdkw_hspp_pcpwdinit")) {
//            this.openSalaryUrl();
            FormShowParameter showParameter = buildFormShowParameter("_submaintab_", formId, ShowType.MainNewTabPage, OperationStatus.ADDNEW);
            this.getView().getFormShowParameter().getOpenStyle().setShowType(ShowType.Default);
            this.getView().showForm(showParameter);
        } else if (formId.equalsIgnoreCase("hspm_myermanfile")) {
            FormShowParameter showParameter = buildFormShowParameter("_submaintab_", formId, ShowType.MainNewTabPage, OperationStatus.VIEW);
            this.getView().getFormShowParameter().getOpenStyle().setShowType(ShowType.Default);
            this.getView().showForm(showParameter);
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
                formShowParameter.setCustomParam("view-customer", "hr");
                if (formId.equalsIgnoreCase("tdkw_wsgl_form_yscgsq")) {
                    formShowParameter.setCustomParam("showFormParam", "person");
                }
                this.getView().getFormShowParameter().getOpenStyle().setShowType(ShowType.Default);
                this.getView().showForm(formShowParameter);
                //2023年11月15日，排查总部提供自助首页不刷新后，跳转出差单据后返回常用应用显示为空问题
                //this.getView().close();
            } else if (HRStringUtils.equals(appSource, "1")) {
                Map<String, Object> map = IAppConfigService.getInstance().getAppConfigById(appConfigId, "0");
                this.getView().openUrl(String.valueOf(map.get("entryentity.url")));
            }

        }
    }

    /**
     * @author xxx
     * @Description 单点登录跳转外部链接-薪资
     * @Date 2023/7/17 10:44
     */
    public void openSalaryUrl() {
        /*
            http://
            10.91.17.218
            /hrself/#/ssologinKingdee?sid=
            jiay.lin
            &token=
            59700730-d177-4353-888a-6bc005977c09
            &tokenUrl=/query-salary
        */
       /* StringBuilder url = new StringBuilder();
        url.append("http://");
        String ip = System.getProperty("hr.ipconfig.ncip");
        url.append(ip);
        url.append("/hrself/#/ssologinKingdee?sid=");
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员非时序性属性
        DynamicObject hrpi_pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
        // sid
        String tdkw_domainaccount = hrpi_pernontsprop.getString("tdkw_domainaccount");
        url.append(tdkw_domainaccount);
        url.append("&token=");
        String token = GetTokenUtil.getToken(tdkw_domainaccount);
        url.append(token);
        url.append("&tokenUrl=/query-salary");

        this.getView().openUrl(url.toString());*/
    }

    /**
     * 原本  appFlex 96*74  改成76*54
     * margin 22*20 改成5*20
     *
     * @param appConfigDy
     * @param index
     * @return
     */
    private FlexPanelAp getAppFlex(Map<String, Object> appConfigDy, int index) {
        Margin margin = new Margin();
        margin.setTop("5px");
        margin.setLeft("20px");
        StringBuilder apKey = new StringBuilder();
        apKey.append("appflex").append("#").append(appConfigDy.get("formId")).append("#").append(appConfigDy.get("appsource")).append("#").append(appConfigDy.get("modeltype")).append("#").append(appConfigDy.get("id"));
        FlexPanelAp appFlex = HrobsPageUtil.customFlexPane(apKey.toString(), margin, (Padding) null, (Border) null, "76px", "74px");
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
