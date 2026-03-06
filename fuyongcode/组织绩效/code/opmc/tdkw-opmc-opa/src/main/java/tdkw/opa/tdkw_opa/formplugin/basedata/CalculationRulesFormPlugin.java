package tdkw.opa.tdkw_opa.formplugin.basedata;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.field.MulBasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import tdkw.opa.tdkw_opa.formplugin.enums.BaseConstant;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class CalculationRulesFormPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        // 侦听基础资料字段的事件
        MulBasedataEdit fieldEdit = this.getView().getControl("tdkw_metric_select");
        fieldEdit.addBeforeF7SelectListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        // 第一组数据：按完成比例计算
        this.getModel().setValue("tdkw_rule_name1", "按完成比例计算", 0);
        this.getModel().setValue("tdkw_score_desc", "得分=年度完成值/目标值*100%*100，得分超过100分按100分计算", 0);
        this.getModel().setValue("tdkw_metric_example", "指标A目标值10万元，年度目标完成值8万元", 0);
        this.getModel().setValue("tdkw_score_result", "80", 0);

        // 第二组数据：按偏差率扣减计算
        this.getModel().setValue("tdkw_rule_name1", "按偏差率扣减计算", 1);
        this.getModel().setValue("tdkw_score_desc", "偏差率=(年度完成值-目标值)/目标值\n得分=100-（扣分系数*偏差值*100），得分超过100分按100分计算", 1);
        this.getModel().setValue("tdkw_metric_example", "指标A目标值3%，年度目标完成值4.5%，扣分系数为1", 1);
        this.getModel().setValue("tdkw_score_result", "50", 1);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        String fieldKey = event.getProperty().getName();
        if (StringUtils.equals(fieldKey, "tdkw_metric_select")) {
            DynamicObjectCollection rulesSetting = this.getModel().getEntryEntity("tdkw_rules_setting");
            List<Long> metricIds = new ArrayList<>();

            for (DynamicObject setting : rulesSetting) {
                DynamicObjectCollection metricSelect = setting.getDynamicObjectCollection("tdkw_metric_select");
                for (DynamicObject metric : metricSelect) {
                    DynamicObject fbasedataid = metric.getDynamicObject("fbasedataid");
                    if (fbasedataid != null) {
                        Long metricId = fbasedataid.getLong("id");
                        metricIds.add(metricId);
                    }
                }
            }
            // 构造过滤条件
            if (!metricIds.isEmpty()) {
                QFilter qFilter = new QFilter(BaseConstant.ID, QCP.not_in, metricIds);
                // 设置列表过滤条件
                ListShowParameter showParameter = (ListShowParameter) event.getFormShowParameter();
                showParameter.getListFilterParameter().setFilter(qFilter);
            }
        }
    }

}
