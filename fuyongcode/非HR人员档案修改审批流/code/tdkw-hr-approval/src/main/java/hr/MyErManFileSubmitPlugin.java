package hr;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.exception.KDBizException;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.IFormView;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.control.Button;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.ProgressEvent;
import kd.bos.form.control.events.ProgresssListener;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hspm.formplugin.multiviewconfig.MultiViewConfigPlugin;
import kd.hr.hspm.formplugin.web.employee.MyErManFilePlugin;
import org.apache.commons.collections4.CollectionUtils;
import kd.sdk.hr.hspm.business.helper.ApprovalHelper;
import kd.sdk.hr.hspm.business.repository.ErmanFileRepository;

import java.text.MessageFormat;
import java.util.*;

/**
 * 我的档案提交审核插件
 */
public class MyErManFileSubmitPlugin extends AbstractFormPlugin implements ProgresssListener{

    private static final List<String> GROUP_NAME_LIST = Arrays.asList("职业资格", "专业技术职称");

    private static final String SUBMIT = "submit";
    private static final Log LOGGER = LogFactory.getLog(MultiViewConfigPlugin.class);

    private static final HRBaseServiceHelper INFOAPPROVAL_HELPER = new HRBaseServiceHelper("hspm_infoapprova");


    public void registerListener(EventObject evt) {
        super.registerListener(evt);
        this.addClickListeners(new String[]{SUBMIT});
        ApprovalHelper.setSubmitButtonVisibleListener(this.getView(), this);
    }

    @Override
    public void confirmCallBack(MessageBoxClosedEvent messageBoxClosedEvent) {
        super.confirmCallBack(messageBoxClosedEvent);
        MessageBoxResult result = messageBoxClosedEvent.getResult();
        if (MessageBoxResult.Yes.equals(result) || MessageBoxResult.OK.equals(result)) {
            String key = messageBoxClosedEvent.getCallBackId();
            if ("ApprovalSubmitCallBack".equals(key)) {
                ApprovalHelper.submit(this.getView());
            } else if ("HomePageCloseCallBack".equals(key)) {
                LOGGER.info("HomePageCloaseCallBack...");
                ApprovalHelper.delCacheData(this.getView());
                IFormView view = this.getView();
                view.getPageCache().put("HomePageClose", "true");
                view.close();
            }

        }
        Long personId = (Long)this.getView().getFormShowParameter().getCustomParam("personId");
        if(personId==null){
            personId = this.getPersonId();
        }
        DynamicObject erFileDy = ErmanFileRepository.getPrimaryErmanFile(personId);
        DynamicObject bill = getInProcessWorkFlowBill(personId);
        if(bill!=null){
            bill.set("tdkw_company",erFileDy.get("empposrel.company"));
            bill.set("tdkw_department",erFileDy.get("empposrel.adminorg"));
            INFOAPPROVAL_HELPER.save(new DynamicObject[]{bill});
        }
    }

    public static DynamicObject getInProcessWorkFlowBill(Long personId) {
        if (personId != null && personId != 0L) {
            QFilter personFilter = new QFilter("person", "=", personId);
            QFilter statusFilter = (new QFilter("billstatus", "=", "B")).or("billstatus", "=", "G").or("billstatus", "=", "D");
            DynamicObject bill = BusinessDataServiceHelper.loadSingle("hspm_infoapproval","tdkw_company,tdkw_department",new QFilter[]{personFilter, statusFilter});

            return bill;
        } else {
            LOGGER.warn("personId is null");
            return null;
        }
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Button)evt.getSource()).getKey();
        if (SUBMIT.equalsIgnoreCase(key)){
            ApprovalHelper.submitConfirm(this.getView(), this);
        }
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);
        String key = ((Button)evt.getSource()).getKey();
        String buNumber = getBuNumber();
        if (SUBMIT.equalsIgnoreCase(key)) {
            ConfirmCallBackListener confirm = new ConfirmCallBackListener("ApprovalSubmitCallBack", this);
            Long personId = (Long)this.getView().getFormShowParameter().getCustomParam("personId");
            Long configId = (Long)this.getView().getFormShowParameter().getCustomParam("configId");
            IAppCache appCache = AppCache.get("hspm");
            Long currUserId = RequestContext.get().getCurrUserId();
            appCache.put(currUserId + "hspm_myermanfile" + "personId", personId);
            appCache.put(currUserId + "hspm_myermanfile" + "configId", configId);
        }
        // 特殊信息集管控：职业资格、职称信息，需要特殊BU管控则添加条件
//        if (SUBMIT.equalsIgnoreCase(key) && "SS04".equals(buNumber)) {
        if (SUBMIT.equalsIgnoreCase(key)) {
            ConfirmCallBackListener confirm = new ConfirmCallBackListener("ApprovalSubmitCallBack", this);
            Map<String, List<Map<String, Object>>> updateFieldMap = getAuditFieldMap(this.getView());
            Set<String> groupNamesSet = updateFieldMap.keySet();
            if (groupNamesSet.size() < 2) {
                return;
            }
            List<String> newGroupNameList = (List<String>)CollectionUtils.intersection(groupNamesSet, GROUP_NAME_LIST);
            if (newGroupNameList.size() > 0) {
                GROUP_NAME_LIST.stream().forEach(f -> {
                    groupNamesSet.remove(f);
                });
                if (!GROUP_NAME_LIST.contains(groupNamesSet)) {
                    this.getView().showTipNotification("职业资格、职称信息特殊管控，请单独提交修改");
                    evt.setCancel(true);
                }
            }
        }
    }

    private Long getPersonId() {
        Map<String, Object> resultPerson = (Map) HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonService", "getPersonModelId", new Object[0]);
        LOGGER.info(MessageFormat.format("resultPerson:{0}", resultPerson));
        if (resultPerson != null && resultPerson.size() != 0) {
            Map<String, Object> idData = (Map)resultPerson.get("data");
            if (!Objects.isNull(idData) && idData.get("person") instanceof Long) {
                return (Long)idData.get("person");
            } else {
                throw new KDBizException(ResManager.loadKDString("登录用户获取的人员ID为空", "MyErManFilePlugin_1", "hr-hspm-formplugin", new Object[0]));
            }
        } else {
            throw new KDBizException(ResManager.loadKDString("登录用户获取的人员信息为空", "MyErManFilePlugin_0", "hr-hspm-formplugin", new Object[0]));
        }
    }

    /**
     * 获取BU编码
     *
     * @return String
     */
    private String getBuNumber() {
        Long personId = (Long)this.getView().getFormShowParameter().getCustomParam("personId");
        if(personId==null){
            personId = getPersonId();
        }
        DynamicObject ermanFile = ErmanFileRepository.getPrimaryErmanFile(personId);
        String buNumber = ermanFile == null ? "" : ermanFile.getString("org.number");
        return buNumber;
    }


    public static Map<String, List<Map<String, Object>>> getAuditFieldMap(IFormView view) {
        Map<String, List<Map<String, Object>>> updateFieldMap = new HashMap();
        Map<String, String> allCache = view.getPageCache().getAll();
        Iterator var3 = allCache.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var3.next();
            String key = (String)entry.getKey();
            if (!HRStringUtils.isEmpty(key) && key.endsWith("-entrycache")) {
                String cacheValue = (String)entry.getValue();
                List<Map<String, Object>> groupFields = (List)SerializationUtils.fromJsonString(cacheValue, List.class);
                updateFieldMap.put(key.replace("-entrycache", ""), groupFields);
            }
        }

        return updateFieldMap;
    }

    @Override
    public void onProgress(ProgressEvent progressEvent) {
        ApprovalHelper.handleSubmitButtonVisible(this.getView());
    }
}
