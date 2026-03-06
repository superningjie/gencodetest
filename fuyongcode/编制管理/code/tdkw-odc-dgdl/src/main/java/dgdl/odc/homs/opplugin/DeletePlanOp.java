package dgdl.odc.homs.opplugin;

import dgdl.odc.homs.validator.DeletePlanValidator;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;


/**
 * @Author: yaoshuai
 * @CreateTime: 2024-01-29  15:02
 * @Description: 删除编制计划校验
 */
public class DeletePlanOp extends AbstractOperationServicePlugIn {

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("dgdl_org");
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        super.onAddValidators(e);
        e.addValidator(new DeletePlanValidator());
    }
}
