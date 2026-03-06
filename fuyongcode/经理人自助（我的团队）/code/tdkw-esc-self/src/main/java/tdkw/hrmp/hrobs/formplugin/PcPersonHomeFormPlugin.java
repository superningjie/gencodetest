package tdkw.hrmp.hrobs.formplugin;

import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.Map;

/**
 * @Metadata： wtss_pcpersonhome 我的假勤
 * @Description ： 通过HR自助门户打开--控件显示隐藏
 * @ClassName ：PcPersonHomeFormPlugin
 * @author xxx
 * @Date ：2023/7/8 15:40
 * @Version: 1.0
 */
public class PcPersonHomeFormPlugin extends AbstractFormPlugin {

    @Override
    public void initialize() {
        super.initialize();
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        if(customParams.containsKey("target")){
            boolean target = (boolean) customParams.get("target");
            if(target){
                this.getView().setVisible(false,"flexpanelap1","flexpanelap2");
            }
        }
    }
}
