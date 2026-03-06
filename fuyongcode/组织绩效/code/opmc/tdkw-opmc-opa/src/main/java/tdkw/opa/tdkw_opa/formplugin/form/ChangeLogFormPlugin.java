package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.Set;

public class ChangeLogFormPlugin extends AbstractFormPlugin {

    private static final Log logger = LogFactory.getLog(ChangeLogFormPlugin.class);


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        String modifyBillId = this.getView().getFormShowParameter().getCustomParam("modifybillid");
        QFilter qFilter = new QFilter("id", QCP.equals, Long.valueOf(modifyBillId));
        DynamicObject orgPerfMetrics = QueryServiceHelper.queryOne(EntityName.BILL_ORG_PERF_METRICS, "tdkw_adminorg.id", qFilter.toArray());
        ArrayList<QFilter> timelineOptions = new ArrayList<>();
        timelineOptions.add(new QFilter("bizobjnumber", QCP.equals, EntityName.BILL_ORG_PERF_METRICS));
        timelineOptions.add(new QFilter("modifybillid", QCP.in, new String[]{modifyBillId, modifyBillId + "+", modifyBillId + "-"}));
        DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("bos_aduit_log_new", "id,username,opdate,modifybillid,modifycontent_tag", (QFilter[]) timelineOptions.toArray(new QFilter[0]), "opdate desc");

        if (dynamicObjects != null) {
            for (DynamicObject dynamicObject : dynamicObjects) {
                String modifycontentTag = dynamicObject.getString("modifycontent_tag");
                Date opdate = dynamicObject.getDate("opdate");
                String username = dynamicObject.getString("username");
                JSONObject jsonObject = JSONObject.parseObject(modifycontentTag);
                JSONObject evalRes = jsonObject.getJSONObject("tdkw_eval_res_list");
                JSONArray hideEntry = jsonObject.getJSONArray("tdkw_hideentry");
                // 处理 tdkw_eval_res_list 字段
                if (evalRes != null) {
                    String oldValue = evalRes.getString("o"); // 旧值
                    String newValue = evalRes.getString("n"); // 新值
                    String changeItem = evalRes.getString("c"); // 变更项

                    if (!(StringUtils.isBlank(oldValue) && StringUtils.isBlank(newValue))) {
                        String operationType;
                        if (StringUtils.isBlank(oldValue)) {
                            operationType = "新增";
                        } else if (StringUtils.isBlank(newValue)) {
                            operationType = "删除";
                        } else {
                            operationType = "修改";
                        }
                        int rowIndex = this.getModel().createNewEntryRow("tdkw_entryentity");
                        this.getModel().setValue("tdkw_adminorg", orgPerfMetrics.getLong("tdkw_adminorg.id"), rowIndex);
                        this.getModel().setValue("tdkw_operation_type", operationType, rowIndex);
                        this.getModel().setValue("tdkw_change_item", changeItem, rowIndex);
                        this.getModel().setValue("tdkw_before_change", oldValue, rowIndex);
                        this.getModel().setValue("tdkw_after_change", newValue, rowIndex);
                        this.getModel().setValue("tdkw_change_date", opdate, rowIndex);
                        this.getModel().setValue("tdkw_operation_user", username, rowIndex);
                    }
                }
                if (hideEntry != null) {
                    for (Object o : hideEntry) {
                        JSONObject hideEntryObject = (JSONObject) o;
                        JSONArray subEntryArray = hideEntryObject.getJSONArray("tdkw_subentryentity");

                        if (subEntryArray != null) {
                            for (Object subEntryObj : subEntryArray) {
                                JSONObject subEntryJsonObj = (JSONObject) subEntryObj;

                                // 检查 keySet 是否只有 "tdkw_metric_log"
                                Set<String> keys = subEntryJsonObj.keySet();
                                Arrays.asList("k", "c", "f").forEach(keys::remove);
                                if (keys.size() == 1 && keys.contains("tdkw_metric_log")) {
                                    continue; // 仅有 "tdkw_metric_log" 时不处理
                                }

                                // 每一个 subEntryObj 对应 tdkw_subentryentity 的一条数据
                                for (String key : subEntryJsonObj.keySet()) {
                                    if (!"c".equals(key) && !"k".equals(key) && !"f".equals(key) && !"tdkw_metric_log".equals(key)) {
                                        JSONObject keyValueObj = subEntryJsonObj.getJSONObject(key);

                                        if (keyValueObj != null) {
                                            String oldValue = keyValueObj.getString("o"); // 旧值
                                            String newValue = keyValueObj.getString("n"); // 新值
                                            String changeItem = keyValueObj.getString("c"); // 变更项

                                            // 判断新增、修改或删除操作
                                            String operationType;
                                            if (StringUtils.isBlank(oldValue) && StringUtils.isBlank(newValue)) {
                                                continue;
                                            }
                                            if (StringUtils.isBlank(oldValue)) {
                                                operationType = "新增";
                                            } else if (StringUtils.isBlank(newValue)) {
                                                operationType = "删除";
                                            } else {
                                                operationType = "修改";
                                            }
                                            int rowIndex = this.getModel().createNewEntryRow("tdkw_entryentity");

                                            this.getModel().setValue("tdkw_operation_type", operationType, rowIndex);

                                            // 设置字段值
                                            this.getModel().setValue("tdkw_adminorg", orgPerfMetrics.getLong("tdkw_adminorg.id"), rowIndex);
                                            // 处理 tdkw_metric_log 字段

                                            JSONObject metricLogJson = subEntryJsonObj.getJSONObject("tdkw_metric_log");
                                            logger.info("metricLogJson=" + metricLogJson);
                                            if (metricLogJson != null) {
                                                String metricLog = metricLogJson.getString("n");
                                                if (StringUtils.isBlank(metricLog)) {
                                                    metricLog = metricLogJson.getString("o");
                                                }
                                                // 检查 metricLog 是否为 null 或空字符串后再调用 contains
                                                if (StringUtils.isNotBlank(metricLog) && metricLog.contains("+")) {
                                                    String metricName = metricLog.substring(0, metricLog.indexOf("+"));
                                                    this.getModel().setValue("tdkw_metric_name", metricName, rowIndex);
                                                }
                                            }
                                            this.getModel().setValue("tdkw_change_item", changeItem, rowIndex);
                                            this.getModel().setValue("tdkw_before_change", oldValue, rowIndex);
                                            this.getModel().setValue("tdkw_after_change", newValue, rowIndex);
                                            this.getModel().setValue("tdkw_change_date", opdate, rowIndex);
                                            this.getModel().setValue("tdkw_operation_user", username, rowIndex);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
