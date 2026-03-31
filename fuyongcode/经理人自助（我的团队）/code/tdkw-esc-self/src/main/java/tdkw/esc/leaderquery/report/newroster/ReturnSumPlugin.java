package tdkw.esc.leaderquery.report.newroster;

import kd.bos.report.plugin.AbstractReportFormPlugin;

import java.util.Map;

/**
 * @author xxx
 * @version 1.0
 * @description: TODO
 * @date 2024/2/2 0002 上午 9:40
 */

public class ReturnSumPlugin extends AbstractReportFormPlugin {
    @Override
    public Integer resetDataCount() {
        Map<String, Object> customParam = this.getQueryParam().getCustomParam();
        Integer sum = (Integer) customParam.get("sum");
        return sum;
    }
}
