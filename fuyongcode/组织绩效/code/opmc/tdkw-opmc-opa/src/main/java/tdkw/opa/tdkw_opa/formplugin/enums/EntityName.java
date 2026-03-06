package tdkw.opa.tdkw_opa.formplugin.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 组织绩效常用实体标识
 *
 * @author 余梦圆
 * @version 1.0
 * @since 2024-08-18
 */
public class EntityName {
    // 组织绩效指标 表单
    public static final String BILL_ORG_PERF_METRICS = "tdkw_org_perf_metrics";

    public static final String BILL_ORG_TARGET_AUDIT = "tdkw_metrics_audit";
    public static final String BILL_CALCULATE_SCORE = "tdkw_calculate_score";

    // 指标分解
    public static final String BILL_TARGET_DECOMPOSE = "tdkw_target_decompose";
    // 指标对齐
    public static final String BILL_TARGET_ALIGNMENT = "tdkw_target_alignment";

    // 计算规则
    public static final String BASE_CALCULATION_RULES = "tdkw_calculation_rules";
    // 亮灯规则
    public static final String BASE_LIGHT_RULE = "tdkw_light_rule";
    // 通知人员配置表
    public static final String BASE_NOTIFY_CONFIG = "tdkw_notify_config";

    public static final String BASE_METRIC_TYPE = "tdkw_metric_type";
    // 指标库
    public static final String BASE_PERF_METRICS = "tdkw_perf_metrics";


    // 我的目标
    public static final String BASE_MY_TARGET = "tdkw_my_target";

    public static final String FORM_ORG_VIEW = "tdkw_perf_metrics_view";
    public static final String FORM_ORG_LIST_VIEW = "tdkw_org_perf_list_show";
    public static final String FORM_ADD_AREA = "tdkw_add_area";
    public static final String FORM_CHANGE_LOG = "tdkw_org_perf_change_log";
    public static final String FORM_USE_ANNUAL_GOAL = "tdkw_use_annual_goal";



    // 分录 固定字段 标识
    public static final List<String> AUDIT_FIELDS = Arrays.asList(
            "tdkw_superorg",
            "tdkw_metric",
            "tdkw_metric_str",
            "tdkw_weight",
            "tdkw_metric_desc",
            "tdkw_target_value",
            "tdkw_unit",
            "tdkw_rating_standard"
    );

    // 分录 固定字段 标识
    public static final List<String> FIELD_LIST = Arrays.asList(
            "tdkw_superorg",
            "tdkw_metric",
            "tdkw_metric_str",
            "tdkw_weight",
            "tdkw_metric_desc",
            "tdkw_target_value",
            "tdkw_unit",
            "tdkw_rating_standard",
            "tdkw_q1_completion",
            "tdkw_q2_completion",
            "tdkw_q3_completion",
            "tdkw_q4_completion",
            "tdkw_annual_val",
            "tdkw_ctr_annual_val",
            "tdkw_eval_res"
    );

    // 按组织查看 可见字段
    public static final List<String> ORG_VIEW_FIELD = Arrays.asList(
            "fseq",
            "tdkw_adminorg.name",// 所属组织
            "tdkw_industry_name", // 行业
            "tdkw_adminorg.company.name", // 公司
            "tdkw_department_name", // 部门
            "tdkw_assess_year", // 考核年份
            "tdkw_eval_res_list",// 评估结果
            "billstatus", // 状态
            "modifytime" // 更新时间
    );

    // 按公司指标查看 可见字段
    public static final List<String> COMPANY_VIEW_FIELD = Arrays.asList(
            "fseq",
            "tdkw_adminorg.name",// 所属组织
            "tdkw_industry_name", // 行业
            "tdkw_adminorg.company.name", // 公司
            "tdkw_department_name", // 部门
            "tdkw_assess_year", // 考核年份
            "tdkw_area_type.name",// 指标类型
            "tdkw_metric_str",//指标名称
            "tdkw_weight",// 权重
            // 指标描述
            "tdkw_metric_desc",
            // 目标值
            "tdkw_target_value",
            // 单位
            "tdkw_unit",
            // 评分标准
            "tdkw_rating_standard",
            // Q1季度完成情况
            "tdkw_q1_completion",
            // Q2季度完成情况
            "tdkw_q2_completion",
            // Q3季度完成情况
            "tdkw_q3_completion",
            // Q4季度完成情况
            "tdkw_q4_completion",
            // 年度完成值
            "tdkw_annual_val",
            // 评估得分
            "tdkw_eval_res",
            // 填报人
            "creator.name",
            "billstatus", // 状态
            "modifytime" // 更新时间
    );

    // 按中心指标查看 可见字段
    public static final List<String> CENTER_VIEW_FIELD = Arrays.asList(
            "fseq",
            "tdkw_adminorg.name",// 所属组织
            "tdkw_industry_name", // 行业
            "tdkw_adminorg.company.name", // 公司
            "tdkw_department_name", // 部门
            "tdkw_assess_year", // 考核年份
            "tdkw_area_type.name",// 指标类型
            "tdkw_metric_str",//指标名称
            "tdkw_weight",// 权重
            // 指标描述
            "tdkw_metric_desc",
            // 评分标准
            "tdkw_rating_standard",
            // 年度完成情况
            "tdkw_ctr_annual_val",
            // 评估得分
            "tdkw_eval_res",
            // 填报人
            "creator.name",
            "billstatus", // 状态
            "modifytime" // 更新时间
    );

    public static final HashMap<String, String> unitMap = new HashMap<String, String>() {{
        put("yuan", "元");
        put("wan_yuan", "万元");
        put("bai_wan_yuan", "百万元");
        put("qian_wan_yuan", "千万元");
        put("percent", "百分比");
    }};

}
