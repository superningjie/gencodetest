package cds0.opmc.cea.plugin.form;


import cds0.opmc.cea.business.service.AssessActDimSettingDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.form.control.Label;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;
import java.util.EventObject;

public class AssessActFinishConfirmPlugin extends HRDataBaseEdit {

    private static final AssessActDimSettingDomainService ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE = AssessActDimSettingDomainService.getInstance();

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        // 设置二次弹窗校验内容
        Label label = getView().getControl("validmessage");
        label.setText(getView().getFormShowParameter().getCustomParam("validMessage"));
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
                && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch (operateKey){
                case AppflgConstant.OP_KEY_CANCEL:
                    getView().getParentView().getPageCache().put("confirmop","cancel");
                    getView().close();
                    break;
                case AppflgConstant.OP_KEY_FINISH:
                    getView().getParentView().getPageCache().put("confirmop","finish");
                    getView().close();
            }
        }
    }
}
