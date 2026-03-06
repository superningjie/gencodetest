package hr;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.*;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.FormOperate;
import kd.bos.mvc.list.ListView;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.util.HRJSONUtils;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;
import kd.hr.htm.business.application.IHtmToHrcsAppService;
import kd.hr.htm.business.domain.repository.PermissionRepository;
import kd.hr.htm.business.domain.service.certify.IQuitCertifyCacheService;
import kd.hr.htm.business.domain.service.certify.IQuitCertifyService;
import kd.hr.htm.common.enums.CertifyPrintType;
import kd.hr.htm.common.enums.PrintPriviewTypeEnum;
import kd.hr.htm.common.enums.YesNo;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description 离职证明表单插件
 * 参考标品 kd.hr.htm.formplugin.certify.QuitCertifyEditPlugin
 * @Version 1.0.0
 * @Date 2024/9/6 15:49
 * @Created by sxf
 */
public class QuitCertifyEditExtPlugin extends HRDataBaseEdit implements BeforeF7SelectListener {

    private final IQuitCertifyService certifyService = new QuitCertifyServiceExtImpl();

    private static final HRBaseServiceHelper CETIFY_SERVICE_HELPER = new HRBaseServiceHelper("tdkw_certifymange");

    private IQuitCertifyCacheService cacheService = IQuitCertifyCacheService.getInstance();

    private static final String OPTKEY_PRINTPREVIEW = "printpreview";

    private static final String HTM_PRINT = "htm_print";

    @Override
    public void beforeBindData(EventObject e) {
        DynamicObject certify = this.getModel().getDataEntity(true);
        List<HashMap<String, Object>> certifyAllNumber = this.certifyService.getCertifyAllNumber(certify.getLong("person.id"));
        Optional<HashMap<String, Object>> certifyNumberOpt = certifyAllNumber.stream().filter((row) -> {
            return (Boolean)row.get("ismajor");
        }).findAny();
        certifyNumberOpt.ifPresent((row) -> {
            this.getModel().setValue("certificatetype", HRJSONUtils.getLongValOfCustomParam(row.get("certificatetype")));
            this.getModel().setValue("certifynumber", row.get("number"));
        });
        this.getModel().setValue("issuancetype", CertifyPrintType.PAPER.getValue());
        this.getModel().setValue("poshis", certify.getDynamicObject("tdkw_transferbill.tdkw_bposition"));
//        Map<String, Date> isCom = this.certifyService.getIsCom(certify);
//        this.setIsCom(certify, isCom);
        this.getModel().setDataChanged(false);
        this.getView().setVisible(Boolean.TRUE, new String[]{"flexpanelap"});
    }

    private void setIsCom(DynamicObject certify, Map<String, Date> isCom) {
        if (!isCom.isEmpty()) {
            this.getModel().setValue("isnoncompete", YesNo.YES.getValue());
            this.getModel().setValue("starttime", isCom.get("starttime"));
            this.getModel().setValue("endtime", isCom.get("endtime"));
        } else {
            certify.set("isnoncompete", YesNo.NO.getValue());
            this.getModel().setValue("isnoncompete", YesNo.NO.getValue());
        }

        CETIFY_SERVICE_HELPER.updateDataOne(certify);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate operate = (FormOperate)args.getSource();
        String operateKey = operate.getOperateKey();
        if (OPTKEY_PRINTPREVIEW.equals(operateKey) && !this.quitHandlePageValPermission()) {
            this.getView().showErrorNotification(ResManager.loadKDString("您没有开具离职证明的功能权限。", "QuitCertifyEditPlugin_1", "hr-htm-formplugin", new Object[0]));
            args.setCancel(true);
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        if (args.getOperationResult() != null && args.getOperationResult().isSuccess()) {
            FormOperate operate = (FormOperate)args.getSource();
            String operateKey = operate.getOperateKey();
            if (OPTKEY_PRINTPREVIEW.equals(operateKey)) {
                FormShowParameter formShowParameter = this.getView().getFormShowParameter();
                DynamicObject certify = this.getModel().getDataEntity(true);
                if (!HRStringUtils.isEmpty((String)formShowParameter.getCustomParam("isEditPage"))) {
                    this.certifyService.updateCertifyByPreview(certify);
                } else {
                    Map<String, Object> customParams = formShowParameter.getCustomParams();
                    IQuitCertifyService.getInstance().updateCertifyByHandCertifyLogPreview(HRJSONUtils.getLongValOfCustomParam(customParams.get("certifyhandle")), HRJSONUtils.getLongValOfCustomParam(customParams.get("id")), PrintPriviewTypeEnum.LOG.getValue());
                }
            }

        }
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        if (this.cacheService.getIsPrintedCache(this.getModel().getDataEntity().getLong("id"))) {
            this.getView().showConfirm(String.format(ResManager.loadKDString("已操作打印预览，是否完成离职证明开具？", "QuitCertifyEditPlugin_0", "hr-htm-formplugin", new Object[0])), MessageBoxOptions.OKCancel, new ConfirmCallBackListener(HTM_PRINT, this.getPluginName(), MessageCallBackType.Plugin));
            e.setCancel(true);
        }

    }


    private boolean quitHandlePageValPermission() {
        IFormView parentView = this.getView().getParentView();
        String formId;
        if (ListView.class.equals(parentView.getClass())) {
            formId = ((ListView)parentView).getBillFormId();
        } else {
            formId = parentView.getEntityId();
        }

        return PermissionRepository.getInstance().checkOperatePermission(formId, "HRQXX0039");
    }

    @Override
    public void confirmCallBack(MessageBoxClosedEvent messageBoxClosedEvent) {
        super.confirmCallBack(messageBoxClosedEvent);
        if (HTM_PRINT.equals(messageBoxClosedEvent.getCallBackId())) {
            this.getModel().setDataChanged(false);
            this.cacheService.removePrintedCache(this.getModel().getDataEntity().getLong("id"));
            if (messageBoxClosedEvent.getResult() == MessageBoxResult.Yes) {
                if (!PermissionRepository.getInstance().checkOperatePermission("tdkw_certifymange", "HRQXX0039")) {
                    this.getView().showErrorNotification(ResManager.loadKDString("您没有开具离职证明的功能权限。", "QuitCertifyEditPlugin_1", "hr-htm-formplugin", new Object[0]));
                    return;
                }

                DynamicObject activityinsObj = this.getModel().getDataEntity().getDynamicObject("activityins");
                Long currUserId = RequestContext.get().getCurrUserId();
                if (activityinsObj != null) {
                    boolean canUseTaskStatus = IHtmToHrcsAppService.getInstance().canSubmitStatus(activityinsObj.getString("taskstatus"));
                    if (canUseTaskStatus) {
                        OperationResult activityAddHandlerResult = IHtmToHrcsAppService.getInstance().activityAddHandler(activityinsObj, currUserId);
                        if (!activityAddHandlerResult.isSuccess()) {
                            this.getView().showOperationResult(activityAddHandlerResult);
                            return;
                        }

                        OperationResult consentTaskResult = IHtmToHrcsAppService.getInstance().consentTask(activityinsObj.getLong("id"), currUserId, "");
                        if (!consentTaskResult.isSuccess()) {
                            this.getView().showOperationResult(consentTaskResult);
                            return;
                        }
                    }
                }

                this.certifyService.updateQuitApplyAndCertifyByPrint(this.getModel().getDataEntity(true));
                this.getView().close();
                this.getView().getParentView().invokeOperation("refresh");
                this.getView().sendFormAction(this.getView().getParentView());
            }

            this.getView().close();
        }

    }

    @Override
    public void propertyChanged(PropertyChangedArgs propertyarg) {
        super.propertyChanged(propertyarg);
        String field = propertyarg.getProperty().getName();
        DynamicObject certify = this.getModel().getDataEntity(true);
        if ("certificatetype".equals(field)) {
            Long certifyLong = certify.getLong("certificatetype.id");
            this.getModel().setValue("certifynumber", this.certifyService.getCertifyNumber(certify.getLong("person.id"), certifyLong));
        }

    }

    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        DynamicObject certify = this.getModel().getDataEntity(true);
        List<HashMap<String, Object>> certifyAllNumber = this.certifyService.getCertifyAllNumber(certify.getLong("person.id"));
        List<Long> collect = (List)certifyAllNumber.stream().map((row) -> {
            return (Long)row.get("certificatetype");
        }).collect(Collectors.toList());
        QFilter filter = new QFilter("id", "in", collect);
        beforeF7SelectEvent.getCustomQFilters().add(filter);
    }
}
