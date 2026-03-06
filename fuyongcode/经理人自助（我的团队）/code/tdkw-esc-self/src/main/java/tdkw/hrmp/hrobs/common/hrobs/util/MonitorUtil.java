package tdkw.hrmp.hrobs.common.hrobs.util;


import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;


/**
 * @description: PC运营监控工具类
 * @author xxx
 * @date: 2023/10/24 10:30
 * @param:
 * @param: null
 * @return: null
 **/
public class MonitorUtil {


    /**
     * @description: 保存
     * @author xxx
     * @date: 2023/10/24 10:48
     **/
    public static void save(String appId, String appName, String operaId, String operaName, String operaType, String query, String clientType) {
        DynamicObject operaMonitorLog = BusinessDataServiceHelper.newDynamicObject("tdkw_opera_monitor_log");
        operaMonitorLog.set("billstatus", "A");
        operaMonitorLog.set("tdkw_monitor_appid", appId);
        operaMonitorLog.set("tdkw_monitor_appname", appName);
        operaMonitorLog.set("tdkw_monitor_operaid", operaId);
        operaMonitorLog.set("tdkw_monitor_operaname", operaName);
        operaMonitorLog.set("tdkw_monitor_operatype", operaType);
        long currentUserId = UserServiceHelper.getCurrentUserId();
        operaMonitorLog.set("tdkw_monitor_userfield", currentUserId);
        operaMonitorLog.set("tdkw_monitor_hrid", HRRoleAndPersonUtils.getHRUser(currentUserId));
        operaMonitorLog.set("tdkw_monitor_query", query);
        operaMonitorLog.set("tdkw_monitor_client_type", clientType);
        SaveServiceHelper.save(new DynamicObject[]{operaMonitorLog});
    }


}
