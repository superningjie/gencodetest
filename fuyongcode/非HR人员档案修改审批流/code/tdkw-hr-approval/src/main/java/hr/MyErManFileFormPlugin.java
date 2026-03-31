package hr;

import com.google.common.collect.Maps;
import kd.bos.dataentity.Tuple;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.exception.KDBizException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.container.Container;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.form.container.FlexPanelAp;
import kd.bos.metadata.form.control.LabelAp;
import kd.bos.orm.util.CollectionUtils;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.control.HRFlexPanelAp;
import kd.hr.hbp.common.control.HRLabelAp;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.sdk.hr.hspm.business.helper.ApprovalHelper;
import kd.sdk.hr.hspm.business.repository.ErmanFileRepository;
import kd.sdk.hr.hspm.business.service.AttacheHandlerService;
import kd.sdk.hr.hspm.business.service.PageRegConfigService;
import kd.sdk.hr.hspm.common.enums.ClientTypeEnum;
import kd.sdk.hr.hspm.common.utils.ApprovalEntityUtils;
import kd.sdk.hr.hspm.common.utils.CommonUtil;

import java.text.MessageFormat;
import java.util.*;

/**
 * @Description：我的档案表单插件
 */
public class MyErManFileFormPlugin extends AbstractFormPlugin {

    private static final Log LOGGER = LogFactory.getLog(MyErManFileFormPlugin.class);

    private static final HRBaseServiceHelper serviceHelper = new HRBaseServiceHelper("hspm_infoapproval");

    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper("hspm_infoapprovalcache");

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String formId = this.getView().getParentView().getEntityId();
        if("hspm_ermanfiletreelist".equals(formId)){
            this.getView().getPageCache().put("personId", this.getView().getFormShowParameter().getCustomParams().get("personId").toString());
            Map<String, Object> infoGroupConfig = this.initParam();
            ApprovalEntityUtils.initCacheFromEntry(this.getView(), infoGroupConfig);
            this.drawHeadArea();
            this.drawLeftMenu();
        }
    }
    private Map<String, Object> initParam() {
        Map<String, Object> result = Maps.newHashMapWithExpectedSize(2);
        long personId = 0L;
        long configId = 0L;
        if (!CollectionUtils.isEmpty(result)) {
            personId = (Long)result.getOrDefault("personId", 0L);
            configId = (Long)result.getOrDefault("configId", 0L);
        }

        if (personId == 0L) {
            personId = Long.parseLong(this.getView().getFormShowParameter().getCustomParams().get("personId").toString());
        }

        DynamicObject erFileDy = ErmanFileRepository.getPrimaryErmanFile(personId);
        if (Objects.isNull(erFileDy)) {
            throw new KDBizException(ResManager.loadKDString("登录用户获取的人员档案为空", "MyErManFilePlugin_2", "hr-hspm-formplugin", new Object[0]));
        } else {
            Tuple tuple;
            if (configId != 0L) {
                tuple = Tuple.create(Boolean.TRUE, configId);
            } else {
                tuple = AttacheHandlerService.getInstance().handleRuleEngine(this.getView(), erFileDy.getLong("id"), erFileDy, "hspm" + ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), (Map)null, false);
            }

            LOGGER.info(MessageFormat.format("handleRuleEngine_result:{0}", tuple));
            if (!(Boolean)tuple.item1) {
                this.getView().showTipNotification(ResManager.loadKDString("没有匹配的业务规则，当前页面将为您显示默认方案。若不符合要求，请联系管理员调整业务规则", "MultiViewTemplatePlugin_1", "hr-hspm-formplugin", new Object[0]));
            }

            Map<String, Object> infoGroupConfig = PageRegConfigService.getInstance().getInfoGroupConfig((Long)tuple.item2, ClientTypeEnum.EMPLOYEE_PC.getCode(), (String)null);
            LOGGER.info(MessageFormat.format("getInfoGroupConfig:{0}", infoGroupConfig));
            if (CollectionUtils.isEmpty(infoGroupConfig)) {
                throw new KDBizException(ResManager.loadKDString("业务规则匹配不成功，请刷新页面重试或联系管理员。", "MyErManFilePlugin_6", "hr-hspm-formplugin", new Object[0]));
            } else {
                this.setPageCache(erFileDy, infoGroupConfig);
                return infoGroupConfig;
            }
        }
    }

    private void setPageCache(DynamicObject erFileDy, Map<String, Object> infoGroupConfig) {
        IPageCache pageCache = this.getPageCache();
        pageCache.put("erfileId", erFileDy.getString("id"));
        pageCache.put("personId", erFileDy.getString("person.id"));
        pageCache.put("person.id", erFileDy.getString("person.id"));
        pageCache.put("depemp.id", erFileDy.getString("depemp.id"));
        pageCache.put("employee.id", erFileDy.getString("employee.id"));
        pageCache.put("cmpemp.id", erFileDy.getString("cmpemp.id"));
        pageCache.put("adminorgid", String.valueOf(erFileDy.getLong("depemp.adminorg.id")));
        List<Map<String, Object>> tabList = (List)infoGroupConfig.get("mainentry");
        List<Map<String, Object>> headList = (List)infoGroupConfig.get("headentity");
        if (!CollectionUtils.isEmpty(tabList) && !CollectionUtils.isEmpty(headList)) {
            pageCache.put("cnfjson", SerializationUtils.toJsonString(infoGroupConfig));
            pageCache.put("mainentry", SerializationUtils.toJsonString(tabList));
            pageCache.put("headentity", SerializationUtils.toJsonString(headList));
        } else {
            throw new KDBizException(ResManager.loadKDString("方案数据异常", "MyErManFilePlugin_4", "hr-hspm-formplugin", new Object[0]));
        }
    }

    private void drawHeadArea() {
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.getOpenStyle().setShowType(ShowType.InContainer);
        showParameter.getOpenStyle().setTargetKey("baseinfopanel");
        showParameter.setFormId("hspm_myermanfilehead");
        showParameter.setCustomParam("person", this.getLongFromCache("person.id"));
        showParameter.setCustomParam("depemp", this.getLongFromCache("depemp.id"));
        showParameter.setCustomParam("adminorgid", this.getLongFromCache("adminorgid"));
        showParameter.setCustomParam("employee", this.getLongFromCache("employee.id"));
        showParameter.setCustomParam("cmpemp", this.getLongFromCache("cmpemp.id"));
        showParameter.setCustomParam("params", this.getPageCache().get("headentity"));
        showParameter.setCustomParam("erfileid", this.getLongFromCache("erfileId"));
        this.getView().showForm(showParameter);
    }

    private long getLongFromCache(String key) {
        String value = this.getPageCache().get(key);
        return HRStringUtils.isNotEmpty(value) ? Long.parseLong(value) : 0L;
    }

    private List<Map<String, Object>> getTabList() {
        String mainEntry = this.getPageCache().get("mainentry");
        return HRStringUtils.isNotEmpty(mainEntry) ? (List<Map<String, Object>>)SerializationUtils.fromJsonStringToList(mainEntry, Map.class) : null;
    }

    private void drawLeftMenu() {
        List<Map<String, Object>> tabList = this.getTabList();
        FlexPanelAp relatePanelAp = (new HRFlexPanelAp.Builder("flexpanelrelateinfo")).setWrap(false).setDirection("column").build();
        Iterator var3 = tabList.iterator();

        while(true) {
            while(var3.hasNext()) {
                Map<String, Object> manMap = (Map)var3.next();
                String pageKey = (String)manMap.get("targetkey");
                String formId = (String)manMap.get("pagenumber");
                if (formId != null && !formId.isEmpty() && formId.trim().length() != 0) {
                    if (!CommonUtil.hasPerm(formId, "47150e89000000ac", "hssc", this.getView())) {
                        LOGGER.info(MessageFormat.format("drawLeftMenu_not_perm:{0}", formId));
                    } else {
                        String groupName = (String)manMap.get("groupname");
                        FlexPanelAp relateLabelPanelAp = (new HRFlexPanelAp.Builder("itempanelap" + pageKey)).setWrap(false).setDirection("row").setAlignContent("flex-start").setWidth("160px").setAlignItems("flex-start").setHeight("40px").build();
                        LabelAp lblRelatePageAp = ((HRLabelAp.Builder)((HRLabelAp.Builder)(new HRLabelAp.Builder(pageKey)).setId(pageKey).setName(groupName).setFontSize(14).setClickable(true).setForeColor("#333333").setPaddingLeft("20px")).setMarginTop("10px")).build();
                        relateLabelPanelAp.getItems().add(lblRelatePageAp);
                        if (ApprovalHelper.isNotPasss(this.getView(), pageKey, groupName)) {
                            LabelAp notPassLab = ((HRLabelAp.Builder)((HRLabelAp.Builder)((HRLabelAp.Builder)(new HRLabelAp.Builder(pageKey + "notPass")).setId(pageKey + "notPass").setName(".").setFontSize(14).setClickable(true).setForeColor("#FB2323").setBackColor("#FB2323").setRadius("4px").setMarginLeft("6px")).setMarginRight("10px")).setMarginTop("18px")).setWidth(new LocaleString("6px")).setHeight(new LocaleString("6px")).setShrink(0).build();
                            relateLabelPanelAp.getItems().add(notPassLab);
                        }

                        relatePanelAp.getItems().add(relateLabelPanelAp);
                    }
                } else {
                    LOGGER.info(MessageFormat.format("drawLeftMenu_data_ex:{0}", SerializationUtils.toJsonString(manMap)));
                }
            }

            Container container = (Container)this.getView().getControl("flexpanelrelateinfo");
            container.getItems().addAll(((Container)relatePanelAp.buildRuntimeControl()).getItems());
            this.getView().createControlIndex(container.getItems());
            this.getView().updateControlMetadata("flexpanelrelateinfo", relatePanelAp.createControl());
            return;
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        String formId = this.getView().getParentView().getEntityId();
        if("hspm_ermanfiletreelist".equals(formId)){
            this.getView().setVisible(false,"auditrecord");
            this.getView().setVisible(false,"changerecord");
        }
    }
}
