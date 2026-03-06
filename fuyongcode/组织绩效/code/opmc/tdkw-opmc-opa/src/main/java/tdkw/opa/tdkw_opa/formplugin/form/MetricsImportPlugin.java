package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.api.ApiResult;
import kd.bos.entity.plugin.ImportLogger;
import kd.bos.form.plugin.impt.BatchImportPlugin;
import kd.bos.form.plugin.impt.ImportBillData;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MetricsImportPlugin extends BatchImportPlugin {
    private static String getTypeNumber(String property, String number) {
        String addressTypeNumber = System.getProperty(property);
        if (StringUtils.isEmpty(addressTypeNumber)) {
            addressTypeNumber = number;
        }

        return addressTypeNumber;
    }

    @Override
    protected ApiResult save(List<ImportBillData> rowDatas, ImportLogger logger) {
        QFilter typeFilter = new QFilter("number", QCP.in, new String[]{"T0001", "T0002", "T0003", "T0004", "T0005"});
        DynamicObjectCollection metricTypes = QueryServiceHelper.query("tdkw_metric_type", "name,number,enable", typeFilter.toArray());

        // 创建 Map 存储编码和名称的对应关系
        Map<String, String> metricTypeMap = new HashMap<>();
        for (DynamicObject metricType : metricTypes) {
            String number = metricType.getString("number");
            String name = metricType.getString("name");
            metricTypeMap.put(number, name);
        }

        Iterator<ImportBillData> it = rowDatas.iterator();
        while (it.hasNext()) {
            ImportBillData data = it.next();
            Map<String, Object> billData = data.getData();
            JSONArray parentEntry = (JSONArray) billData.get("tdkw_hideentry"); // 单据体
            //判断有几个本级公司组织绩效
            int count = 0;
            //判断有几个上级组织
            boolean hasSuperOrgs = false;
            for (int i = 0; i < parentEntry.size(); i++) {
                JSONObject parentRow = parentEntry.getJSONObject(i);
                String areaName = parentRow.getJSONObject("tdkw_area_type").getString("name");


                // 只对 KPI 类进行处理
                if (StringUtils.equals(areaName, metricTypeMap.get("T0001")) || StringUtils.equals(areaName, metricTypeMap.get("T0002"))) { // KPI类
                    JSONArray subEntry = parentRow.getJSONArray("tdkw_subentryentity");
                    Iterator<Object> subEntryIterator = subEntry.iterator(); // 使用 Iterator 来遍历 subEntry

                    while (subEntryIterator.hasNext()) {
                        JSONObject subRow = (JSONObject) subEntryIterator.next();
                        String name = subRow.getString("tdkw_metric_str");

                        // 校验指标名称是否正确
                        QFilter qFilter = new QFilter("name", QCP.equals, name);
                        qFilter.and("tdkw_metric_type.name", QCP.equals, areaName);
                        DynamicObject metrics = QueryServiceHelper.queryOne(EntityName.BASE_PERF_METRICS, "id,tdkw_description,tdkw_rating_standard", qFilter.toArray());

                        // 校验不通过，记录日志，移除数据
                        if (metrics == null || metrics.getLong("id") == 0L) {
                            logger.log(data.getStartIndex(), "KPI类的指标名称填写不正确").fail();
                            subEntryIterator.remove(); // 使用 Iterator 的 remove 方法移除
                            continue; // 跳过当前循环，继续下一个元素
                        }

                        // 组装需要带出的基本单位（基础资料）
                        JSONObject newColum = new JSONObject();
                        newColum.put("importprop", "id");
                        newColum.put("id", metrics.getLong("id"));
                        subRow.put("tdkw_metric", newColum);
                        subRow.put("tdkw_description", metrics.getString("tdkw_description"));
                        subRow.put("tdkw_rating_standard", metrics.getString("tdkw_rating_standard"));

                        // 校验KPI类不允许引入的字段
                        if (subRow.containsKey("tdkw_ctr_annual_val")) {
                            subRow.remove("tdkw_ctr_annual_val");
                            logger.log(data.getStartIndex(), "KPI类不允许引入中心年度完成情况，已移除。").fail();
                        }
                    }
                }

                // 校验中心重点工作类/协同类不允许引入的字段
                if (StringUtils.equals(areaName, metricTypeMap.get("T0003")) || StringUtils.equals(areaName, metricTypeMap.get("T0004"))) { // 中心重点工作类/协同类
                    JSONArray subEntry = parentRow.getJSONArray("tdkw_subentryentity");
                    for (int j = 0; j < subEntry.size(); j++) {
                        JSONObject subRow = subEntry.getJSONObject(j);

                        if (subRow.containsKey("tdkw_target_value")) {
                            subRow.remove("tdkw_target_value");
                            logger.log(data.getStartIndex(), "中心重点工作类/协同类不允许引入目标值，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_unit")) {
                            subRow.remove("tdkw_unit");
                            logger.log(data.getStartIndex(), "中心重点工作类/协同类不允许引入单位，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q1_completion")) {
                            subRow.remove("tdkw_q1_completion");
                            logger.log(data.getStartIndex(), "中心重点工作类/协同类不允许引入Q1季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q2_completion")) {
                            subRow.remove("tdkw_q2_completion");
                            logger.log(data.getStartIndex(), "中心重点工作类/协同类不允许引入Q2季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q3_completion")) {
                            subRow.remove("tdkw_q3_completion");
                            logger.log(data.getStartIndex(), "中心重点工作类/协同类不允许引入Q3季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q4_completion")) {
                            subRow.remove("tdkw_q4_completion");
                            logger.log(data.getStartIndex(), "中心重点工作类/协同类不允许引入Q4季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_annual_val")) {
                            subRow.remove("tdkw_annual_val");
                            logger.log(data.getStartIndex(), "中心重点工作类/协同类不允许引入公司年度完成值，已移除。").fail();
                        }
                    }
                }

                //本级公司
                if (StringUtils.equals(areaName, metricTypeMap.get("T0005"))) {
                    count++;
                    JSONArray subEntry = parentRow.getJSONArray("tdkw_subentryentity");
                    hasSuperOrgs = subEntry.size() > 1;
                    for (int j = 0; j < subEntry.size(); j++) {
                        JSONObject subRow = subEntry.getJSONObject(j);
                        //上级组织
                        String superOrgNumber = subRow.getJSONObject("tdkw_superorg").getString("number");
                        QFilter superOrgFilter = new QFilter("number", QCP.equals, superOrgNumber);
                        superOrgFilter.and("iscurrentversion", QCP.equals, "1"); // 当前版本
                        superOrgFilter.and("datastatus", QCP.equals, "1"); // 生效中
                        superOrgFilter.and("status", QCP.equals, "C");

                        DynamicObject superOrg = QueryServiceHelper.queryOne("haos_adminorghr", "id", superOrgFilter.toArray());
                        if (superOrg == null) {
                            logger.log(data.getStartIndex(), "上级组织编码不正确：" + superOrgNumber).fail();
                            it.remove();
                            continue;
                        }
                        //权重
                        String weight = subRow.getString("tdkw_weight");
                        if (!weight.matches("^\\d{1,3}$")) {
                            logger.log(data.getStartIndex(), "权重格式不正确：" + weight).fail();
                            it.remove();
                            continue;
                        }
                        if (subRow.containsKey("tdkw_ctr_annual_val")) {
                            subRow.remove("tdkw_ctr_annual_val");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入中心年度完成情况，已移除。").fail();
                        }

                        if (subRow.containsKey("tdkw_target_value")) {
                            subRow.remove("tdkw_target_value");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入目标值，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_unit")) {
                            subRow.remove("tdkw_unit");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入单位，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q1_completion")) {
                            subRow.remove("tdkw_q1_completion");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入Q1季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q2_completion")) {
                            subRow.remove("tdkw_q2_completion");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入Q2季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q3_completion")) {
                            subRow.remove("tdkw_q3_completion");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入Q3季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_q4_completion")) {
                            subRow.remove("tdkw_q4_completion");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入Q4季度完成情况，已移除。").fail();
                        }
                        if (subRow.containsKey("tdkw_annual_val")) {
                            subRow.remove("tdkw_annual_val");
                            logger.log(data.getStartIndex(), "本级公司组织绩效不允许引入公司年度完成值，已移除。").fail();
                        }
                    }


                }
            }

            // 校验年份是否合法
            String yearStr = billData.get("tdkw_year").toString();
            String billStatus = billData.get("tdkw_billstatus").toString();
            billData.put("billstatus", billStatus);
            try {
                // 使用 LocalDate 进行校验
                LocalDate localDate = LocalDate.of(Integer.parseInt(yearStr), 1, 1);
                // 将 LocalDate 转换为 Date
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                billData.put("tdkw_assess_year", date);
            } catch (DateTimeException e) {
                logger.log(data.getStartIndex(), "年份格式无效：" + yearStr).fail();
                it.remove(); // 移除无效数据
                continue; // 跳过当前循环，继续下一个元素

            }

            String orgNum = ((JSONObject) billData).getJSONObject("tdkw_adminorg").getString("number");
            QFilter orgFilter = new QFilter("number", QCP.equals, orgNum);
            orgFilter.and("iscurrentversion", QCP.equals, "1"); // 当前版本
            orgFilter.and("datastatus", QCP.equals, "1"); // 生效中
            orgFilter.and("status", QCP.equals, "C");

            DynamicObject orgObject = QueryServiceHelper.queryOne("haos_adminorghr", "id", orgFilter.toArray());
            if (orgObject == null) {
                logger.log(data.getStartIndex(), "所属组织编码不正确：" + orgNum).fail();
                it.remove(); // 移除无效数据
                continue; // 跳过当前循环，继续下一个元素
            }
            // 校验本级公司组织绩效数量
            if (count > 1) {
                logger.log(data.getStartIndex(), "本级公司组织绩效数量不能超过1个").fail();
                it.remove(); // 移除无效数据
                continue; // 跳过当前循环，继续下一个元素
            }
            // 校验上级组织
            if (hasSuperOrgs) {
                logger.log(data.getStartIndex(), "只能有一个本级组织").fail();
                it.remove(); // 移除无效数据
                continue; // 跳过当前循环，继续下一个元素
            }
            String industryName = setIndustry(orgObject.getLong("id"), Collections.singletonList(getTypeNumber("constant.hr.haos.haos_adminorgtype.industrynumber", "XY00003")));
            String departmentName = setIndustry(orgObject.getLong("id"), Arrays.asList("XY00006", "XY00007", "XY00008"));
            billData.put("tdkw_industry_name", industryName);
            billData.put("tdkw_department_name", departmentName);
        }

        // 调用缺省方法保存合法的数据
        return super.save(rowDatas, logger);
    }

    private String setIndustry(Long orgId, List<String> numberList) {
        // 如果 orgId 不为 0，执行查询
        if (orgId != null && orgId != 0L) {
            // 查询组织信息
            QFilter qFilter = new QFilter("id", QCP.equals, orgId);
            DynamicObject adminOrgHrDy = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "orgtype,parent,name", qFilter.toArray());

            // 如果查询到组织信息
            if (!ObjectUtils.isEmpty(adminOrgHrDy)) {
                // 获取 orgtype 的动态对象
                DynamicObject orgTypeDy = adminOrgHrDy.getDynamicObject("orgtype");
                if (!ObjectUtils.isEmpty(orgTypeDy)) {
                    // 获取 orgtype 的编码
                    String orgTypeNumber = orgTypeDy.getString("number");

                    // 如果 orgTypeNumber 在传入的 numberList 中，返回当前组织名称
                    if (numberList.contains(orgTypeNumber)) {
                        return adminOrgHrDy.getString("name");
                    }
                    else {
                        // 如果 orgTypeNumber 不在列表中，则继续进行检查
                        for (String number : numberList) {
                            // 比较 orgTypeNumber 和传入 numberList 的最后一位数字大小
                            int actualNumber = Integer.parseInt(orgTypeNumber.substring(orgTypeNumber.length() - 1));
                            int needNumber = Integer.parseInt(number.substring(number.length() - 1));

                            // 如果实际数字大于目标数字，继续递归查找上级组织
                            if (actualNumber > needNumber) {
                                Long parentOrgId = adminOrgHrDy.getLong("parent.id");
                                if (parentOrgId != null && parentOrgId != 0L) {
                                    return setIndustry(parentOrgId, numberList);
                                }
                            }
                        }
                    }
                }
            }
        }
        // 如果没有找到符合条件的组织，返回空字符串
        return "";
    }


}
