package dgdl.odc.homs.formplugin;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import dgdl.odc.homs.common.DateTimeCommon;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.TimeRangeEdit;
import kd.bos.form.operate.FormOperate;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.api.JobInfo;
import kd.bos.schedule.api.JobType;
import kd.bos.schedule.api.TaskInfo;
import kd.bos.schedule.form.JobForm;
import kd.bos.schedule.form.JobFormInfo;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;


/**
 * @author 姚帅
 * @version 1.0
 * @date 2024/1/11 13:36
 * @description: 年度编制计划表单插件
 **/
public class PlanYearFormPlugin extends AbstractBillPlugIn {

    private static Log logger = LogFactory.getLog(PlanYearFormPlugin.class);

    /**
     * 编制管理
     */
    private final static String YEAR_BILL = "dgdl_planyear_bill";


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }



    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        IDataModel model = this.getModel();
        String filedName = e.getProperty().getName();
        if ("dgdl_orgfield".equals(filedName)) {
            DynamicObject org = (DynamicObject) model.getValue("dgdl_orgfield");
            if (Objects.nonNull(org)) {
                logger.info("PlanPersonFormPlugin组织id=" + org.getPkValue());
                model.setValue("dgdl_org", org.getPkValue());
            }
        } else if ("dgdl_year".equals(filedName)) {
            //指定年的最后一天
            Date year = (Date)model.getValue("dgdl_year");
            SimpleDateFormat sim = new SimpleDateFormat("yyyy");
            model.setValue("dgdl_year_str", sim.format(year));
            //获取年
            int formatYear = Integer.parseInt(sim.format(year));
            LocalDate date = LocalDate.of(formatYear, Month.DECEMBER, 31);
            model.setValue("dgdl_endmonth", date);

        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        IDataModel model = this.getModel();
        logger.info("开始执行afterBindData");

        //编制计划单位
        DynamicObject org = (DynamicObject) model.getValue("dgdl_org");
        //名称
        ILocaleString localeString = (ILocaleString) getModel().getValue("name");
        // 简体
        String name = localeString.getLocaleValue_zh_CN();
        if (Objects.nonNull(org)) {
            Object orgId = org.getPkValue();
            QFilter qFilter = new QFilter("dgdl_planyear.dgdl_org.id", QCP.equals, orgId)
                    .and("dgdl_planyear.name", QCP.equals, name);
            DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(YEAR_BILL, "id", qFilter.toArray());
            if (Objects.nonNull(dynamicObject)) {
                this.getView().setEnable(false, "dgdl_org", "number", "dgdl_year", "dgdl_startmonth", "dgdl_endmonth",
                        "dgdl_orghierarchy", "dgdl_plantype", "dgdl_labeldimension", "dgdl_useworktype", "description", "changedescription");
            }
        }
    }


}
