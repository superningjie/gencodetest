package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.EventObject;

/**
 * @author: xxx
 * @create: 2024/08/30 18:12
 * @description:
 **/
public class MapDetailFormPlugin extends AbstractFormPlugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        JSONObject detailsVO = (JSONObject) this.getView().getFormShowParameter().getCustomParams().get("detailsVO");
        if (detailsVO==null){
            return;
        }
        this.getModel().setValue("tdkw_target_name",detailsVO.getString("targetName"));
        this.getModel().setValue("tdkw_target_descri",detailsVO.getString("targetDescription"));
        this.getModel().setValue("tdkw_target_score",detailsVO.getString("targetScoreStandard"));
        String targetNumber = detailsVO.getString("targetNumber");
        if (StringUtils.equals("T0001",targetNumber) || StringUtils.equals("T0002",targetNumber)){
            //隐藏年度完成情况
            this.getView().setVisible(Boolean.FALSE,"tdkw_year_panel");

            this.getModel().setValue("tdkw_q1_completion", detailsVO.getString("firstCompletion"),0);
            this.getModel().setValue("tdkw_q2_completion", detailsVO.getString("secondCompletion"),0);
            this.getModel().setValue("tdkw_q3_completion", detailsVO.getString("thirdCompletion"),0);
            this.getModel().setValue("tdkw_q4_completion", detailsVO.getString("fourthCompletion"),0);
        }else if (StringUtils.equals("T0003",targetNumber) || StringUtils.equals("T0004",targetNumber)){
            //隐藏单据体
            this.getView().setVisible(Boolean.FALSE,"tdkw_entry_panel");
            this.getModel().setValue("tdkw_year_complete",detailsVO.getString("yearCompletion"));

        }
    }
}
