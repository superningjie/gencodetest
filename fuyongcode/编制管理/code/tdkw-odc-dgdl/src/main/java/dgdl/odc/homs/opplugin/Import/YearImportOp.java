package dgdl.odc.homs.opplugin.Import;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import kd.bos.entity.plugin.ImportLogger;
import kd.bos.form.plugin.impt.ImportBillData;
import kd.hr.hbp.formplugin.web.newhismodel.impt.HisBatchImportPlugin;

import java.util.List;

/**
 * 年度编制计划过滤
 * */
public class YearImportOp extends HisBatchImportPlugin {

    private static Log Logger = LogFactory.getLog(YearImportOp.class);

    @Override
    protected void beforeSave(List<ImportBillData> billdatas, ImportLogger logger) {
        super.beforeSave(billdatas, logger);
        Logger.info("YearImportOp开始执行导入="+billdatas.size());
        for (ImportBillData importBillData : billdatas) {

        }
    }
}
