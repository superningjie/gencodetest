package dgdl.odc.homs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.Objects;

/**
 * @Author: 姚帅
 * @CreateTime: 2023/1/26 14:33
 * @Description: 编制计划催办配置表单插件
 */
public class PlanPersonFormPlugin extends AbstractFormPlugin {

    private static Log logger = LogFactory.getLog(PlanPersonFormPlugin.class);

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        //获取值改变属性
        String changeName = e.getProperty().getName();
        IDataModel model = this.getModel();
        if ("dgdl_personplan".equals(changeName)) {
            DynamicObject user = (DynamicObject) model.getValue("dgdl_personplan");
            if (Objects.nonNull(user)) {
                //部门分类
                DynamicObjectCollection entity = user.getDynamicObjectCollection("entryentity");
                for (DynamicObject orgObj : entity) {
                    //获取主职信息
                    if ("false".equals(orgObj.getString("ispartjob"))) {
                        //获取行政组织
                        DynamicObject adminorg = orgObj.getDynamicObject("dpt");
                        model.setValue("dgdl_adminorg", adminorg);
                        //组织
                        if (Objects.nonNull(adminorg)) {
                            DynamicObject haosAdminorghr = BusinessDataServiceHelper.loadSingle(adminorg.getPkValue(), "haos_adminorghr");
                            DynamicObject org = haosAdminorghr.getDynamicObject("org");
                            model.setValue("dgdl_org", org);
                        }
                    }
                }
            }
        }
    }
}
