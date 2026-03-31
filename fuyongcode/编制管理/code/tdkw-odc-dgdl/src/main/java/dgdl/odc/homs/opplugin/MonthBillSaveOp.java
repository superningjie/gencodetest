package dgdl.odc.homs.opplugin;

import dgdl.odc.homs.validator.MonthBillSaveValidator;
import dgdl.odc.homs.validator.MonthDetailSaveValidator;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;

/**
 * @Author: yaoshuai
 * @CreateTime: 2024-03-29  15:02
 * @Description: 月度编制信息管理保存校验
 */
public class MonthBillSaveOp extends AbstractOperationServicePlugIn {

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("dgdl_planmonth_bill");
        e.getFieldKeys().add("adminorg");
        e.getFieldKeys().add("dgdl_layer");
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        super.onAddValidators(e);
        e.addValidator(new MonthDetailSaveValidator());
    }

}
