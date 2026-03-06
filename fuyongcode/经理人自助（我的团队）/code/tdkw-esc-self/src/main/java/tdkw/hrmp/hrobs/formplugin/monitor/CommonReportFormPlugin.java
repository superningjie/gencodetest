package tdkw.hrmp.hrobs.formplugin.monitor;

import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import tdkw.hrmp.hrobs.common.monitor.MonitorUtil;

/**
 * @description: PC运营监控监-报表查询监听
 * @author xxx
 * @date: 2023/10/24 14:25
 * @param:
 * @param: null
 * @return: null
 **/
public class CommonReportFormPlugin extends AbstractReportFormPlugin {

    private static final Log logger = LogFactory.getLog(CommonReportFormPlugin.class);

    @Override
    public boolean verifyQuery(ReportQueryParam queryParam) {
        // 应用名称
        String appName = this.getView().getParentView().getFormShowParameter().getFormName();
        // 应用标识
        String appId = this.getView().getParentView().getFormShowParameter().getAppId();
        // 操作名称
        String operaName = this.getView().getFormShowParameter().getFormName();
        // 操作id
        String operaId = "";
        try {
            String pageId = this.getView().getFormShowParameter().getPageId();
            logger.info("报表pageId:" + pageId);
            operaId = pageId.split("root")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
//        String query = "";
//        List<FilterItemInfo> filterItems = queryParam.getFilter().getFilterItems();
//        if (CollectionUtils.isNotEmpty(filterItems)) {
//            List<Map<String, Object>> collect = filterItems.stream().map(filterItemInfo -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("propName", filterItemInfo.getPropName());
//                map.put("compareType", filterItemInfo.getCompareType());
//                map.put("value", filterItemInfo.getValue());
//                return map;
//            }).collect(Collectors.toList());
//            JSONArray jsonArray = new JSONArray(Collections.singletonList(collect));
//            query = jsonArray.toJSONString();
//        }
        MonitorUtil.save(appId, appName, operaId, operaName, "button", "", "1");
        return super.verifyQuery(queryParam);
    }
}
