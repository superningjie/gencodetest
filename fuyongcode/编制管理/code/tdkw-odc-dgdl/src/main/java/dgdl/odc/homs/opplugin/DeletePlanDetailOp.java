package dgdl.odc.homs.opplugin;

import dgdl.odc.homs.validator.DeletePlanDetailValidator;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;


/**
 * @Author: yaoshuai
 * @CreateTime: 2024-02-20  10:00
 * @Description: 删除编制详情,校验不是末级组织不能删除
 */
public class DeletePlanDetailOp extends AbstractOperationServicePlugIn {

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("dgdl_planyear");
        e.getFieldKeys().add("adminorg");
        e.getFieldKeys().add("dgdl_adminorg1");
        e.getFieldKeys().add("dgdl_adminorg2");
        e.getFieldKeys().add("dgdl_adminorg3");
        e.getFieldKeys().add("dgdl_adminorg4");
        e.getFieldKeys().add("dgdl_adminorg5");
        e.getFieldKeys().add("dgdl_adminorg6");
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        super.onAddValidators(e);
        e.addValidator(new DeletePlanDetailValidator());
    }

}
