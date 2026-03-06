package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import java.util.EventObject;

/**
 * @author xxx
 * @date： 2023/7/8
 * @description : 公告栏表单插件
 */
public class BulletinBrdFormPlugin extends AbstractFormPlugin {

    @Override
    public void afterBindData(EventObject e) {
        newTabPage();
        super.afterBindData(e);
    }

    /**
     * 创建页签
     */
    private void newTabPage() {
        QFilter qFilter = new QFilter("enable", QCP.equals, "1");
        DynamicObject[] type = BusinessDataServiceHelper.load("tdkw_announcement_type",
                "id,name,number,enable", qFilter.toArray());
        IFormView formView = getView();
        for (DynamicObject object : type) {
            FormShowParameter formShowParameter = new FormShowParameter();
            formShowParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
            formShowParameter.getOpenStyle().setTargetKey("tdkw_tabap");
            formShowParameter.setFormId("tdkw_bulletinbrd_content");
            formShowParameter.setCaption(object.getString("name"));
            formShowParameter.setCustomParam("typeNumber", object.getString("number"));
            formView.showForm(formShowParameter);
        }
    }
}
