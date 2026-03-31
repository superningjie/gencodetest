package tdkw.opa.tdkw_opa.formplugin.basedata;

import kd.bos.bill.AbstractBillPlugIn;

import java.util.EventObject;

public class LightRuleFormPlugin extends AbstractBillPlugIn {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        // 第一组数据
        this.getModel().setValue("tdkw_score_scale", "10分", 0);
        this.getModel().setValue("tdkw_rate_conversion", "评分/分制满分", 0);
        this.getModel().setValue("tdkw_score_example", "6", 0);
        this.getModel().setValue("tdkw_rate_result", "60%", 0);

        // 第二组数据
        this.getModel().setValue("tdkw_score_scale", "100分", 1);
        this.getModel().setValue("tdkw_rate_conversion", "评分/分制满分", 1);
        this.getModel().setValue("tdkw_score_example", "70", 1);
        this.getModel().setValue("tdkw_rate_result", "70%", 1);

        // 第三组数据
        this.getModel().setValue("tdkw_score_scale", "算数求和情况下，假设满分40分的指标", 2);
        this.getModel().setValue("tdkw_rate_conversion", "评分/分制满分", 2);
        this.getModel().setValue("tdkw_score_example", "20", 2);
        this.getModel().setValue("tdkw_rate_result", "50%", 2);
    }
}
