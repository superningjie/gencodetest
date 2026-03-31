package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.service.AssessActDimSettingDomainService;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import static cds0.opmc.cea.common.AppflgConstant.*;

public class AssessActConfirmPlugin extends HRDataBaseEdit {
    private static final AssessActDimSettingDomainService ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE = AssessActDimSettingDomainService.getInstance();
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
                && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch(operateKey){
                case OP_KEY_STARTUP:
                    // 启动测评
                    ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.startUpSingle(getView().getFormShowParameter().getCustomParam(ASSESS_ACTIVITY_ID));
                    getView().returnDataToParent(getView().getFormShowParameter().getCustomParam(ASSESS_ACTIVITY_ID));
                    getView().close();
                    break;
            }
        }
    }
}
