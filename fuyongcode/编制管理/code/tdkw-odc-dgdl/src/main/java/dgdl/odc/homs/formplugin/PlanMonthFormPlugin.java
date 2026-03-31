package dgdl.odc.homs.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.login.utils.StringUtils;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.fi.ap.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.Objects;

/**
 * @author lzf
 * @date 2023/9/7 15:49
 * @description
 */
public class PlanMonthFormPlugin extends AbstractBillPlugIn {
    private static Log logger = LogFactory.getLog(PlanMonthFormPlugin.class);


    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        IDataModel model = this.getModel();
        //改变值
        String filed = e.getProperty().getName();
        if ("dgdl_controlediting".equals(filed)){
            String controlediting = (String)model.getValue(filed);
            if (StringUtils.isNotEmpty(controlediting)){
                if ("1".equals(controlediting) || "2".equals(controlediting)){
                    model.setValue("dgdl_way",null);
                    model.setValue("dgdl_people",null);
                }
            }
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        IDataModel model = this.getModel();
        //编制计划单位
        DynamicObject org = (DynamicObject) model.getValue("dgdl_org");
        //名称
        ILocaleString localeString = (ILocaleString) getModel().getValue("name");
        // 简体
        String name = localeString.getLocaleValue_zh_CN();
        if (Objects.nonNull(org)) {
            Object orgId = org.getPkValue();
            QFilter qFilter = new QFilter("dgdl_planmonth.dgdl_org.id", QCP.equals, orgId)
                    .and("dgdl_planmonth.name", QCP.equals, name);
            DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bill", "id", qFilter.toArray());
            if (Objects.nonNull(dynamicObject)) {
                this.getView().setEnable(false, "dgdl_org", "number", "dgdl_year", "dgdl_startmonth", "dgdl_endmonth",
                        "dgdl_orghierarchy", "dgdl_plantype", "dgdl_labeldimension", "dgdl_controlediting", "dgdl_way", "dgdl_peoplecount", "dgdl_percent", "dgdl_isunify","dgdl_people");
            }
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        Date dgdlEndmonth = dataEntity.getDate("dgdl_endmonth");
        if (dgdlEndmonth != null) {
            dataEntity.set("dgdl_endmonth", DateUtils.getMaxMonthDate(dgdlEndmonth));
        }
    }
}
