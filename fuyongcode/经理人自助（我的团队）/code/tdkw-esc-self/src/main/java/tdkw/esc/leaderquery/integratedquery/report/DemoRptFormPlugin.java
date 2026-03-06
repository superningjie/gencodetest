package tdkw.esc.leaderquery.integratedquery.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.NumberFormatProvider;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DemoRptFormPlugin extends AbstractReportFormPlugin {
    @Override
    public void preProcessExportData(List<AbstractReportColumn> exportColumns, DynamicObjectCollection data,
                                     NumberFormatProvider numberFormatProvider) {
        // 获取报表数据控件
        ReportList billList = this.getView().getControl("reportlistap");
        // 获取数据控件中选择的行
        ArrayList selRows = (ArrayList) billList.getEntryState().get("selRows");
        if (null == selRows) {
            return;
        }
        // 如果有选择,则只导出选择行,没选择,则导出所有
        if ((!data.isEmpty()) && (selRows.size() > 0)) {
            Iterator arg5 = data.iterator();
            while (arg5.hasNext()) {
                DynamicObject dyo = (DynamicObject) arg5.next();
                int fseq = (int) dyo.get("fseq");
                if (!selRows.contains(fseq)) {
                    // 删除未选择的行,保留已选的行
                    arg5.remove();
                }
            }
        }
    }
}
