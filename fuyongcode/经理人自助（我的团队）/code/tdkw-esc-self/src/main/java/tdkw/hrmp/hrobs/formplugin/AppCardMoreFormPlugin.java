package tdkw.hrmp.hrobs.formplugin;

import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Label;
import kd.bos.form.plugin.AbstractFormPlugin;

import org.apache.commons.lang.StringUtils;

import java.util.EventObject;

/**
 * @author xxx
 * @Date 2023/6/7 11:02
 * @Description 门户PC端-应用卡片-更多
 * @Demander xxx
 * @Document
 * @Basedata tdkw_hrobs_pc_appcard_ext、tdkw_hrobs_pc_morecard
 * @Version 1.0
 **/
public class AppCardMoreFormPlugin extends AbstractFormPlugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Label tdkw_more = this.getView().getControl("tdkw_more");
        tdkw_more.addClickListener(this);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Label source = (Label) evt.getSource();
        String key = source.getKey();
        if (StringUtils.equals(key, "tdkw_more")) {
            // 更多
            FormShowParameter ShowParameter = new FormShowParameter();
            ShowParameter.setFormId("tdkw_hrobs_pc_morecard");
            ShowParameter.getOpenStyle().setShowType(ShowType.Modal);
            this.getView().showForm(ShowParameter);
        }
    }
}