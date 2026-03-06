package dgdl.odc.homs.opplugin;

import dgdl.odc.homs.validator.MonthBillCheckValidator;
import dgdl.odc.homs.validator.MonthDetailSaveValidator;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;

import java.util.Arrays;

/**
 * @Author: yaoshuai
 * @CreateTime: 2024-03-29  15:02
 * @Description: 月度编制信息管理保存校验
 */
public class MonthBillCheckOp extends AbstractOperationServicePlugIn {

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("dgdl_planmonth_bill");
        e.getFieldKeys().add("adminorg");
        e.getFieldKeys().add("dgdl_layer");
        String[] fields={"adminorg","dgdl_bz_jobproperty","dgdl_bz_laborreltype","dgdl_bz_adminorg1","dgdl_bz_adminorg2","dgdl_bz_adminorg3","dgdl_bz_adminorg4","dgdl_bz_adminorg5","dgdl_bz_adminorg6","dgdl_bz_laborreltype","dgdl_bz_jobproperty","dgdl_jz1","dgdl_jz2","dgdl_jz3","dgdl_jz4","dgdl_jz5","dgdl_jz6","dgdl_jz7",
                "dgdl_jz8","dgdl_jz9","dgdl_jz10","dgdl_jz11","dgdl_jz12","dgdl_bz1","dgdl_bz2","dgdl_bz3","dgdl_bz4","dgdl_bz5","dgdl_bz6","dgdl_bz7","dgdl_bz8","dgdl_bz9","dgdl_bz10","dgdl_bz11","dgdl_bz12"};
        e.getFieldKeys().addAll(Arrays.asList(fields));
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        super.onAddValidators(e);
        e.addValidator(new MonthBillCheckValidator());
    }

}
