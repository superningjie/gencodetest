package tdkw.hrmp.hrobs.formplugin.monitor.task;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运营监控移动端调度任务
 */
public class MonitorMobileDataTask extends AbstractTask {

    private static final Log logger = LogFactory.getLog(MonitorMobileDataTask.class);

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        logger.info("======================运营监控移动端调度任务开始======================");
        // 查询接口配置表
        QFilter appConfigFilter = new QFilter("billno", QCP.equals, "0001");
        DynamicObject monitorAppConfig = BusinessDataServiceHelper.loadSingle("tdkw_monitor_app_config", "id,entryentity.tdkw_monitor_api,entryentity.tdkw_monitor_apicode", appConfigFilter.toArray());
        DynamicObjectCollection appConfigEntry = monitorAppConfig.getDynamicObjectCollection("entryentity");
        Set<Long> apiIds = appConfigEntry.stream().map(dynamicObject -> dynamicObject.getDynamicObject(1).getLong("id")).collect(Collectors.toSet());

        // 查询接口列表
        QFilter apiFilter = new QFilter("id", QCP.in, apiIds);
        DynamicObjectCollection apiList = QueryServiceHelper.query("openapi_apilist", "id,number,name,appid.name,appid.number", apiFilter.toArray());
        Set<String> apiNumbers = apiList.stream().map(dynamicObject -> dynamicObject.getString("number")).collect(Collectors.toSet());

        // apiList转为map
        Map<String, String> apiNameMap = apiList.stream().collect(Collectors.toMap(dynamicObject -> dynamicObject.getString("number"), dynamicObject -> dynamicObject.getString("name")));
        Map<String, String> apiAppNameMap = apiList.stream().collect(Collectors.toMap(dynamicObject -> dynamicObject.getString("number"), dynamicObject -> dynamicObject.getString("appid.name")));
        Map<String, String> apiAppIdMap = apiList.stream().collect(Collectors.toMap(dynamicObject -> dynamicObject.getString("number"), dynamicObject -> dynamicObject.getString("appid.number")));

        // 查询接口调用日志表
        QFilter logFilter = new QFilter("opname", QCP.in, apiNumbers);
        // 获取当前日期
        // 设置 00:00:00 和 23:59:59
        LocalDate currentDate = null;
        Object monitorDate = map.get("date");
        if (StringUtils.isBlank(monitorDate.toString())) {
            currentDate = LocalDate.now();
        } else {
            currentDate = LocalDate.parse((CharSequence) monitorDate);
        }
        LocalDate yesterday = currentDate.minusDays(1);
        LocalDateTime beginDate = LocalDateTime.of(yesterday, LocalTime.MIN);
        LocalDateTime endDate = LocalDateTime.of(yesterday, LocalTime.MAX);
        // 将字符串解析为日期对象
        QFilter logTimeBeginFilter = new QFilter("opdate", QCP.large_equals, beginDate);
        QFilter logTimeEndFilter = new QFilter("opdate", QCP.less_than, endDate);
        // 本地先注释
        DynamicObjectCollection apiLogDataList = QueryServiceHelper.query("openapi_log_data", "id,opname,opdate,userid", new QFilter[]{logFilter, logTimeBeginFilter, logTimeEndFilter});
        // 转换
        DynamicObject[] operaMonitorLogs = apiLogDataList.stream().map(dynamicObject -> {
            String apiNumber = dynamicObject.getString("opname");
            DynamicObject operaMonitorLog = BusinessDataServiceHelper.newDynamicObject("tdkw_opera_monitor_log");
            operaMonitorLog.set("billstatus", "A");
            operaMonitorLog.set("tdkw_monitor_appid", apiAppIdMap.get(apiNumber));
            operaMonitorLog.set("tdkw_monitor_appname", apiAppNameMap.get(apiNumber));
            operaMonitorLog.set("tdkw_monitor_operaid", "");
            operaMonitorLog.set("tdkw_monitor_operaname", apiNameMap.get(apiNumber));
            operaMonitorLog.set("tdkw_monitor_operatype", "button");
            long userId = dynamicObject.getLong("userid");
            operaMonitorLog.set("tdkw_monitor_userfield", userId);
            operaMonitorLog.set("tdkw_monitor_hrid", HRRoleAndPersonUtils.getHRUser(userId));
            operaMonitorLog.set("tdkw_monitor_query", "");
            operaMonitorLog.set("tdkw_monitor_client_type", "2");
            operaMonitorLog.set("createtime", dynamicObject.getDate("opdate"));
            return operaMonitorLog;
        }).toArray(DynamicObject[]::new);
        logger.info("保存移动端运营监控日志共" + operaMonitorLogs.length + "条");
        SaveServiceHelper.save(operaMonitorLogs);
        logger.info("保存移动端运营监控日志成功");
        logger.info("======================运营监控移动端调度任务结束======================");
    }
}
