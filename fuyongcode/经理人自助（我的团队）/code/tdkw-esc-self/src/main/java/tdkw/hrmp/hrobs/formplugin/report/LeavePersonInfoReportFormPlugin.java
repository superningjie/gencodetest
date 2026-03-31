package tdkw.hrmp.hrobs.formplugin.report;

import cfca.svs.api.util.StringUtil;
import com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.report.ReportView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;
import java.util.Map;

/**
 * @author xxx
 * @Date 2023/6/29 10:08
 * @Description 入职分析 报表表单插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personinforpt
 * @Version 1.0
 **/
public class LeavePersonInfoReportFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
    }

    private static final Log logger = LogFactory.getLog(LeavePersonInfoReportFormPlugin.class);

    @Override
    public void hyperLinkClick(HyperLinkClickEvent evt) {
        if (StringUtils.equals("person", evt.getFieldName())) {
            Long personId = (Long) evt.getRowData().getDynamicObject("person").getPkValue();
            QFilter qFilter = new QFilter("id", QCP.equals, personId);
            DynamicObject person = BusinessDataServiceHelper.loadSingle("hrpi_person", "tdkw_pkid", qFilter.toArray());
            String pkId = person.getString("tdkw_pkid");

            //  String pkId = String.valueOf(personId);
            logger.info("跳转的人员pkid为" + pkId);
            try {
                FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                formShowParameter.setCustomParam("erfileId", pkId);
                formShowParameter.setCustomParam("isLeave", Boolean.TRUE);
                formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                formShowParameter.setHasRight(true);
                this.getView().showForm(formShowParameter);
            } catch (Exception e) {
                this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "PersonInfoReportFormPlugin", "tdkw-esc-hrobs-formplugin-ext", new Object[0]));
            }
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        ReportView source = (ReportView) e.getSource();
        String personIds = source.getFormShowParameter().getCustomParam("personIds");
        String personIdsDepart = source.getFormShowParameter().getCustomParam("personIdsDepart");
        if (StringUtils.equals(personIdsDepart, "[]")) {
            this.getView().showErrorNotification("信息为空！请返回！");
            return;
        }
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtil.isNotEmpty(personIds)) {
            params.put("personIds_a", personIds);
        } else {
            params.put("personIds_depart", personIdsDepart);
        }
        source.getQueryParam().setCustomParam(params);
        source.refresh();
    }
}