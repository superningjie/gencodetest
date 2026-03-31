package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.container.Tab;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.TabSelectEvent;
import kd.bos.form.control.events.TabSelectListener;
import kd.bos.form.control.events.UploadEvent;
import kd.bos.form.control.events.UploadListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.impt.ExcelReader;
import kd.bos.impt.SheetHandler;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import tdkw.opa.tdkw_opa.formplugin.enums.BaseConstant;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateScoreFormPlugin extends AbstractBillPlugIn implements TabSelectListener, UploadListener {

    private static final Log logger = LogFactory.getLog(CalculateScoreFormPlugin.class);

    public void registerListener(EventObject e) {
        // 页签添加监听事件*
        Tab tab = this.getView().getControl("tdkw_tabap");
        tab.addTabSelectListener(this);

        Toolbar toolbar = this.getView().getControl("advcontoolbarap");
        toolbar.addUploadListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);

    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.getView().setVisible(false, "tdkw_bar_last_btn", "tdkw_confirm");

    }

    @Override
    public void tabSelected(TabSelectEvent event) {
        // 点击页签的key*
        String subTabKey = event.getTabKey();
        if (subTabKey.equals("tdkw_tab_calc_org_score")) {
            this.getView().setVisible(false, "bar_close", "bar_save", "tdkw_bar_next_btn");
            this.getView().setVisible(true, "tdkw_bar_last_btn", "tdkw_confirm");
        } else if (subTabKey.equals("tdkw_tab_calculate_score")) {
            this.getView().setVisible(true, "bar_close", "bar_save", "tdkw_bar_next_btn");
            this.getView().setVisible(false, "tdkw_bar_last_btn", "tdkw_confirm");
        }

    }
    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        if (StringUtils.equals("save", formOperate.getOperateKey())) {
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_calc_metric_detail");
            for (int i = 0; i < entryEntity.size(); i++) {
                DynamicObject entry = entryEntity.get(i);
                BigDecimal evalRes = entry.getBigDecimal("tdkw_eval_res");
                if (evalRes.compareTo(BigDecimal.ZERO) < 0) {
                    this.getView().showErrorNotification("第" + (i + 1) + "行评估结果需大于等于0");
                    args.setCancel(true);
                    break;
                }
            }
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();

        // 计算指标明细 计算操作
        if (StringUtils.equals("calculate_detail", formOperate.getOperateKey()) && args.getOperationResult().isSuccess()) {
            DynamicObjectCollection metricDetail = this.getModel().getEntryEntity("tdkw_calc_metric_detail");
            // 计算组织总分 分录
            DynamicObjectCollection calcOrgScore = this.getModel().getEntryEntity("tdkw_calc_org_score");
            // 构建 map，以 tdkw_adminorg1.id 为 key，tdkw_eval_res1 为 value
            Map<Long, BigDecimal> orgScoreMap = new HashMap<>();
            for (DynamicObject orgScore : calcOrgScore) {
                long orgId = orgScore.getLong("tdkw_adminorg1.id");
                if (orgId != 0L) {
                    BigDecimal evalRes = orgScore.getBigDecimal("tdkw_eval_res1");
                    orgScoreMap.put(orgId, evalRes);
                }
            }
            for (int i = 0, metricDetailSize = metricDetail.size(); i < metricDetailSize; i++) {
                DynamicObject detail = metricDetail.get(i);
                // 指标类型
                String metricType = detail.getString("tdkw_metric_type.number");
                // 指标名称 id
                long metricId = detail.getLong("tdkw_metric.id");

                String metricName = detail.getString("tdkw_metric.name");
                // 公司年度完成值
                BigDecimal cmpAnnualVal = detail.getBigDecimal("tdkw_cmp_annual_val");
                // 目标值
                BigDecimal targetValue = detail.getBigDecimal("tdkw_target_value");
                // 权重
                // 获取权重字符串，直接转换为BigDecimal，不再处理百分号
                String weightStr = detail.getString("tdkw_weight");
                BigDecimal weight = BigDecimal.ZERO;

                if (StringUtils.isNotEmpty(weightStr)) {
                    weight = new BigDecimal(weightStr).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                // 获取上级组织的评估结果
                logger.info("metricType" + metricType);
                // 1）KPI类指标的处理逻辑
                if (StringUtils.equals("T0001", metricType) || StringUtils.equals("T0002", metricType)) {
                    // 校验年度完成值和目标值是否有值
                    if (cmpAnnualVal == null || targetValue == null || targetValue.compareTo(BigDecimal.ZERO) == 0) {
                        this.getModel().setValue("tdkw_calc_result", "失败：年度完成值或目标值无效", i);
                        continue;
                    }

                    // 查询计算规则
                    QFilter filter = new QFilter(BaseConstant.NUMBER, QCP.equals, "JSGZ_00001");
                    DynamicObject calculationRules = BusinessDataServiceHelper.loadSingle(EntityName.BASE_CALCULATION_RULES, "tdkw_rule_name,tdkw_deduction_coef,tdkw_metric_select", filter.toArray());
                    DynamicObjectCollection rulesSetting = calculationRules.getDynamicObjectCollection("tdkw_rules_setting");

                    boolean found = false;
                    for (DynamicObject rule : rulesSetting) {
                        DynamicObjectCollection metricSelect = rule.getDynamicObjectCollection("tdkw_metric_select");

                        for (DynamicObject select : metricSelect) {
                            long selectMetricId = select.getLong("fbasedataid.id");
                            // 找到匹配的 metricId 后，进行分数计算
                            if (selectMetricId == metricId) {
                                // 计算规则名称
                                String ruleName = rule.getString("tdkw_rule_name");
                                // 按完成比例计算
                                if (StringUtils.equals("completionrate", ruleName)) {
                                    BigDecimal score = cmpAnnualVal.divide(targetValue, 10, RoundingMode.HALF_UP)  // 保留10位小数
                                            .multiply(BigDecimal.valueOf(100))
                                            .setScale(2, RoundingMode.HALF_UP);  // 保留两位小数
                                    if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
                                        score = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);  // 确保四舍五入并保留两位小数
                                    }
                                    if (score.compareTo(BigDecimal.ZERO) < 0){
                                        //小于0
                                        score=BigDecimal.ZERO;
                                    }
                                    this.getModel().setValue("tdkw_eval_res", score, i);
                                    this.getModel().setValue("tdkw_calc_result", "成功", i);
                                }

                                // 按偏差率扣减计算
                                else if (StringUtils.equals("deviationrate", ruleName)) {
                                    BigDecimal deductionCoef = rule.getBigDecimal("tdkw_deduction_coef");
                                    BigDecimal deviationRate = cmpAnnualVal.subtract(targetValue)
                                            .divide(targetValue, 10, RoundingMode.HALF_UP)  // 保留10位小数
                                            .setScale(2, RoundingMode.HALF_UP);  // 保留两位小数
                                    BigDecimal score = BigDecimal.valueOf(100)
                                            .subtract(deductionCoef.multiply(deviationRate)
                                                    .multiply(BigDecimal.valueOf(100)))
                                            .setScale(2, RoundingMode.HALF_UP);  // 最后保留两位小数
                                    if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
                                        score = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
                                    }
                                    if (score.compareTo(BigDecimal.ZERO) < 0){
                                        //小于0
                                        score=BigDecimal.ZERO;
                                    }
                                    this.getModel().setValue("tdkw_eval_res", score, i);
                                    this.getModel().setValue("tdkw_calc_result", "成功", i);
                                }

                                found = true;
                                break;  // 退出内层循环
                            }
                        }

                        if (found) {
                            break;  // 退出外层循环
                        }
                    }

                    if (!found) {
                        this.getView().showTipNotification("计算失败，" + metricName + "未设置计算规则，请设置规则后再计算");
                    }
                }
                // 2）公司级组织绩效指标的处理逻辑
                else if (StringUtils.equals("T0005", metricType)) {
                    // 获取上级组织的评估结果
                    logger.info("计算公司级组织绩效指标");
                    // 上级组织id
                    long superOrgId = detail.getLong("tdkw_superorg.id");
                    String superOrgName = detail.getString("tdkw_superorg.name");
                    BigDecimal superiorEvalRes = orgScoreMap.get(superOrgId);
                    logger.info("计算分数的superiorEvalRes" + superiorEvalRes);
                    // 如果没有在当前页面找到上级组织的评估结果，查询计算分数单据
                    if (superiorEvalRes == null) {
                        Date assessYearDate = (Date) this.getModel().getValue("tdkw_assess_year");
                        LocalDate localDate = assessYearDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        int assessYear = localDate.getYear();
                        QFilter filter = new QFilter("tdkw_adminorg.id", QCP.equals, superOrgId);
                        filter.and("YEAR(tdkw_assess_year)", QCP.equals, assessYear);
                        DynamicObject orgPerfMetrics = QueryServiceHelper.queryOne(EntityName.BILL_ORG_PERF_METRICS, "tdkw_eval_res_list", filter.toArray());

                        // 检查查询结果
                        if (orgPerfMetrics != null) {
                            superiorEvalRes = orgPerfMetrics.getBigDecimal("tdkw_eval_res_list");
                            logger.info("指标查询的superiorEvalRes" + superiorEvalRes);
                        }
                    }

                    // 处理评估结果
                    if (superiorEvalRes == null || superiorEvalRes.compareTo(BigDecimal.ZERO) == 0) {
                        this.getModel().setValue("tdkw_calc_result", "失败：上级组织" + superOrgName + "未计算评估结果", i);
                    } else {
                        BigDecimal score = superiorEvalRes.multiply(weight)
                                .setScale(2, RoundingMode.HALF_UP);  // 保留两位小数并四舍五入
                        if (score.compareTo(BigDecimal.ZERO) < 0){
                            //小于0
                            score=BigDecimal.ZERO;
                        }
                        this.getModel().setValue("tdkw_eval_res", score, i);
                        this.getModel().setValue("tdkw_calc_result", "成功", i);
                    }

                }
            }
        } else if (StringUtils.equals("calculate_org_score", formOperate.getOperateKey()) && args.getOperationResult().isSuccess()) {
            DynamicObjectCollection calcOrgScore = this.getModel().getEntryEntity("tdkw_calc_org_score");
            DynamicObjectCollection metricDetail = this.getModel().getEntryEntity("tdkw_calc_metric_detail");

            // 遍历计算组织总分表
            for (int i = 0; i < calcOrgScore.size(); i++) {
                DynamicObject orgScore = calcOrgScore.get(i);
                long orgId = orgScore.getLong("tdkw_adminorg1.id");

                // 累加组织总分时，也进行保留两位小数
                BigDecimal totalScore = BigDecimal.ZERO;
                for (DynamicObject detail : metricDetail) {
                    long detailOrgId = detail.getLong("tdkw_adminorg.id");

                    if (detailOrgId == orgId) {
                        BigDecimal evalRes = detail.getBigDecimal("tdkw_eval_res");
                        String weightStr = detail.getString("tdkw_weight");
                        BigDecimal weight = BigDecimal.ZERO;

                        // 解析权重
                        if (StringUtils.isNotEmpty(weightStr)) {
                            weight = new BigDecimal(weightStr).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);  // 保留10位小数
                        }

                        // 累加评估结果乘以权重的值
                        if (evalRes != null) {
                            totalScore = totalScore.add(evalRes.multiply(weight));  // 不进行四舍五入，保持精度
                        }
                    }
                }

                // 将累加结果设置为组织总分
                this.getModel().setValue("tdkw_eval_res1", totalScore.setScale(2, RoundingMode.HALF_UP), i);  // 保留两位小数
            }
        } else if (StringUtils.equals("bar_next", formOperate.getOperateKey()) && args.getOperationResult().isSuccess()) {
            this.getView().invokeOperation("save");
            Tab tab = this.getView().getControl("tdkw_tabap");
            tab.activeTab("tdkw_tab_calc_org_score");

        } else if (StringUtils.equals("bar_last", formOperate.getOperateKey()) && args.getOperationResult().isSuccess()) {
            this.getView().invokeOperation("save");
            Tab tab = this.getView().getControl("tdkw_tabap");
            tab.activeTab("tdkw_tab_calculate_score");

        } else if (StringUtils.equals("confirm", formOperate.getOperateKey()) && args.getOperationResult().isSuccess()) {
            this.getView().invokeOperation("save");
            this.getView().close();
        } else if (StringUtils.equals("save", formOperate.getOperateKey()) && args.getOperationResult().isSuccess()) {
            // 回写组织总分
            DynamicObjectCollection calcMetricDetail = this.getModel().getEntryEntity("tdkw_calc_metric_detail");
            Map<Long, DynamicObject> subentryIdToDyMap = new HashMap<>();

            for (DynamicObject row : calcMetricDetail) {
                Long subEntryId = row.getLong("tdkw_org_perf_subentryid");
                subentryIdToDyMap.put(subEntryId, row);
            }
            DynamicObjectCollection calcOrgScore = this.getModel().getEntryEntity("tdkw_calc_org_score");

            // 创建映射存储 tdkw_perf_metricsid 和 tdkw_eval_res1 组织总分
            Map<Long, BigDecimal> metricsIdToEvalResMap = new HashMap<>();
            List<Long> metricsIds = new ArrayList<>();

            for (DynamicObject row : calcOrgScore) {
                Long perfMetricsId = row.getLong("tdkw_perf_metricsid");
                BigDecimal evalRes = row.getBigDecimal("tdkw_eval_res1");

                if (evalRes != null) {
                    metricsIdToEvalResMap.put(perfMetricsId, evalRes);
                    metricsIds.add(perfMetricsId);
                }
            }

            // 查询 tdkw_org_perf_metrics 表
            if (!metricsIds.isEmpty()) {
                QFilter qFilter = new QFilter("id", QCP.in, metricsIds);
                DynamicObject[] orgPerfMetrics = BusinessDataServiceHelper.load(EntityName.BILL_ORG_PERF_METRICS, "id,billno,tdkw_metric,tdkw_metric_log,tdkw_assess_year,tdkw_eval_res,tdkw_annual_val,tdkw_ctr_annual_val,tdkw_adminorg,tdkw_subentryentity,tdkw_hideentry,tdkw_eval_res_list", qFilter.toArray());
                for (DynamicObject orgMetric : orgPerfMetrics) {
                    Long id = orgMetric.getLong("id");
                    BigDecimal evalRes = metricsIdToEvalResMap.get(id);

                    if (evalRes != null) {
                        orgMetric.set("tdkw_eval_res_list", evalRes);
                    }
                    // 遍历 hideEntry 和 subEntryEntity，进行 eval_res 赋值
                    DynamicObjectCollection hideEntry = orgMetric.getDynamicObjectCollection("tdkw_hideentry");
                    for (DynamicObject row : hideEntry) {
                        DynamicObjectCollection subEntryEntity = row.getDynamicObjectCollection("tdkw_subentryentity");
                        for (DynamicObject subRow : subEntryEntity) {
                            Long subEntryId = subRow.getLong("id");
                            String metricName = subRow.getString("tdkw_metric.name");

                            if (subentryIdToDyMap.containsKey(subEntryId)) {
                                DynamicObject dynamicObject = subentryIdToDyMap.get(subEntryId);
                                // 评估结果
                                BigDecimal subEvalRes = dynamicObject.getBigDecimal("tdkw_eval_res");
                                // 公司年度完成值
                                BigDecimal cmpAnnualVal = dynamicObject.getBigDecimal("tdkw_cmp_annual_val");
                                // 中心年度完成情况
                                String ctrAnnualVal = dynamicObject.getString("tdkw_ctr_annual_val");

                                // 评估结果
                                subRow.set("tdkw_eval_res", subEvalRes);
                                // 公司年度完成值
                                subRow.set("tdkw_annual_val", cmpAnnualVal);
                                // 中心年度完成情况
                                subRow.set("tdkw_ctr_annual_val", ctrAnnualVal);

                                // 获取当前时间戳
                                long timestamp = System.currentTimeMillis();
                                // 将 metricName 和时间戳组合
                                String metricLogValue = metricName + "+" + timestamp;

                                // 使用 model.setValue 方法，并指定父行号和子行号进行赋值
                                subRow.set("tdkw_metric_log", metricLogValue);
                            }
                        }
                    }
                }
                // 保存回写后的数据
                SaveServiceHelper.saveOperate(EntityName.BILL_ORG_PERF_METRICS, orgPerfMetrics, OperateOption.create());
            }

        }
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        DeleteServiceHelper.delete(dataEntity.getDataEntityType(), new Object[]{dataEntity.getPkValue()});
    }

    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String actionId = closedCallBackEvent.getActionId();
        if ("import".equals(actionId)) {
            this.getView().invokeOperation("refresh");
        }

    }

    @Override
    public void upload(UploadEvent evt) {
        UploadListener.super.upload(evt);
        String callbackKey = evt.getCallbackKey();
        if (StringUtils.equals(callbackKey, "tdkw_importdata")) {
            Object[] urls = evt.getUrls();
            if (urls != null && urls.length > 0) {
                this.importEntry(urls[0].toString());
            }
        }
    }


    public void importEntry(String url) {
        ExcelReader reader = new ExcelReader();

        // 获取附件的文件服务


        FileService fs = FileServiceFactory.getAttachmentFileService();
        InputStream stream = fs.getInputStream(url);
        List<Map<Integer, String>> list = new ArrayList<>();
        try {
            reader.read(stream, new SheetHandler() {
                @Override
                public void handleRow(ParsedRow parsedRow) {
                    Map<Integer, String> data = parsedRow.getData();
                    if (parsedRow.getRowNum() > 0) {
                        list.add(data);
                    }

                }
            });
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        boolean isSuccess = true;
        for (int i = 0; i < list.size(); i++) {
            //引入的数据
            Map<Integer, String> mapData = list.get(i);
            //组织名称
            String orgName = mapData.get(0);
            //指标类型
            String metricType = mapData.get(2);
            //指标名称
            String metricName = mapData.get(3);
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_calc_metric_detail");
            boolean entryHave = false;
            for (int j = 0; j < entryEntity.size(); j++) {
                DynamicObject entry = entryEntity.get(j);
                //组织名称
                String entryOrgName = entry.getString("tdkw_adminorg.name");
                //指标类型
                String entryMetricType = entry.getString("tdkw_metric_type.name");
                //指标名称
                String entryMetricName = entry.getString("tdkw_metric_str");
                //指标类型 编码
                String entryMetricTypeNumber = entry.getString("tdkw_metric_type.number");

                //对比组织名称，指标类型,指标名称
                if (StringUtils.equals(entryOrgName, orgName) && StringUtils.equals(entryMetricType, metricType) && StringUtils.equals(entryMetricName, metricName)) {
                    entryHave = true;
                    //kpi
                    if (StringUtils.equals(entryMetricTypeNumber, "T0001") || StringUtils.equals(entryMetricTypeNumber, "T0002")) {
                        // 公司年度完成值
                        this.getModel().setValue("tdkw_cmp_annual_val", mapData.get(9), j);
                        if (StringUtils.isNotBlank(mapData.get(10))) {
                            isSuccess = false;
                            this.getView().showErrorNotification("第" + (i + 1) + "行KPI类指标无需维护中心年度完成情况");
                        }
                    } else {
                        // 中心年度完成情况
                        this.getModel().setValue("tdkw_ctr_annual_val", mapData.get(10), j);
                        if (StringUtils.isNotBlank(mapData.get(9))) {
                            isSuccess = false;
                            this.getView().showErrorNotification("第" + (i + 1) + "行非KPI类指标无需维护公司年度完成值");
                        }
                    }
                    // 评估结果
                    this.getModel().setValue("tdkw_eval_res", mapData.get(11), j);
                    break;
                }


            }
            //如果 引入的文件没找到 对应的单据体
            if (!entryHave) {
                this.getView().showErrorNotification("第" + (i + 1) +"行所属组织、指标类型及指标名称不匹配，不可引入！");
                isSuccess=false;
            }
        }
        if (isSuccess) {
            this.getView().showSuccessNotification("引入成功！");
        }

    }

}
