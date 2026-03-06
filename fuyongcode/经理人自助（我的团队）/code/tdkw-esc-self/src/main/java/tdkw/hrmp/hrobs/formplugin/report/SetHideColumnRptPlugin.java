package tdkw.hrmp.hrobs.formplugin.report;

import com.alibaba.fastjson.JSONArray;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.entity.report.ReportColumn;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.list.ListShowParameter;
import kd.bos.list.ViewCommonUtil;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.mvc.report.ReportView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserConfigServiceHelper;

import java.util.*;

public class SetHideColumnRptPlugin extends AbstractListPlugin {

    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);

//        FormShowParameter formShowParameter = e.getFormShowParameter();
//        String reportFormID = formShowParameter.getFormId();
//        QFilter qFilter = new QFilter("tdkw_formid.number", QCP.equals, reportFormID);
//        qFilter.and("billstatus", QCP.equals, "C");
//        DynamicObject formidNumber = BusinessDataServiceHelper.loadSingle("tdkw_leader_query_display", qFilter.toArray());
//        Map<String, Boolean> maps = new HashMap<>();
//        if (formidNumber != null) {
//            //获取单据体集合
//            DynamicObjectCollection entryentity = formidNumber.getDynamicObjectCollection("entryentity");
//            //获取要显示的字段集
//            for (DynamicObject dynamicObject : entryentity) {
//                maps.put(dynamicObject.getString("tdkw_column"), dynamicObject.getBoolean("tdkw_checkboxfield"));
//            }
//            //获取用户列表配置
//            String reportFieldsControlKey = formShowParameter.getFormId() + "_reportlistap_reportcolumnsmap";
//            Long userId = RequestContext.get().getCurrUserId();
//            String reportFieldsControlBase64 = UserConfigServiceHelper.getSetting(userId, reportFieldsControlKey);
//            Map<String, Boolean> columns;
//            if (StringUtils.isNotBlank(reportFieldsControlBase64)) {
//                columns = SerializationUtils.deSerializeFromBase64(reportFieldsControlBase64);
//            } else {
//                columns = new HashMap<>();
//            }
//            columns.putAll(maps);
//            reportFieldsControlBase64 = SerializationUtils.serializeToBase64(columns);
//            UserConfigServiceHelper.setSetting(userId, reportFieldsControlKey, reportFieldsControlBase64);
//        }
    }
}
