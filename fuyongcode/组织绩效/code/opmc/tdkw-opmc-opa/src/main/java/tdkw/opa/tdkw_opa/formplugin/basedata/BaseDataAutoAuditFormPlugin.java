package tdkw.opa.tdkw_opa.formplugin.basedata;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import tdkw.opa.tdkw_opa.formplugin.enums.OperationConst;

public class BaseDataAutoAuditFormPlugin extends AbstractFormPlugin {
    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        if (StringUtils.equals(OperationConst.SUBMIT, formOperate.getOperateKey()) || StringUtils.equals(OperationConst.AUDIT, formOperate.getOperateKey())) {
            formOperate.getOption().setVariableValue(OperateOptionConst.ISSHOWMESSAGE, "false");
        }

    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        // 计算指标明细 计算操作
        if (StringUtils.equals(OperationConst.SAVE, formOperate.getOperateKey()) && args.getOperationResult().isSuccess()) {
            this.getView().invokeOperation(OperationConst.SUBMIT);
            this.getView().invokeOperation(OperationConst.AUDIT);
        }

    }
}
