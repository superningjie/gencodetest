package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.field.ComboEdit;
import kd.bos.form.field.TextEdit;
import kd.bos.form.plugin.AbstractFormPlugin;

/**
 * @author xxx
 * @date 2023/11/17
 * @description 门户Banner表单插件
 */
public class PortalBannerPlugin extends AbstractFormPlugin {
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        if (e.getProperty().getName().equals("tdkw_skipornot")) {
            DynamicObject dataEntity = this.getModel().getDataEntity(true);
            boolean skipornot = dataEntity.getBoolean("tdkw_skipornot");
            TextEdit url = this.getControl("tdkw_url");
            ComboEdit mc = this.getControl("tdkw_mc");
            url.setMustInput(skipornot);
            mc.setMustInput(skipornot);
        }
    }
}