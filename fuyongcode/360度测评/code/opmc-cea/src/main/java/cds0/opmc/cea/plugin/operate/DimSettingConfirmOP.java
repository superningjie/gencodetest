package cds0.opmc.cea.plugin.operate;

import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.hr.hbp.opplugin.web.HRCoreBaseBillOp;

public class DimSettingConfirmOP extends HRCoreBaseBillOp {
    @Override
    public void onAddValidators(AddValidatorsEventArgs e){
        e.addValidator(new DimSettingValidator());
    }

    /**
     * 在数据入库之前计算等级字段值
     * @param e
     */
    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e){

    }
}
