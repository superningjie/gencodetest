package tdkw.hrmp.hrobs.formplugin.report;

import javafx.util.Pair;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.filter.ReportFilter;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.List;

/**
 * SkipToThisMonthReportFormPlugin
 *
 * @author xxx
 * @date 2023/7/14
 */
public class SkipToThisMonthInReportFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {

    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
    }

    private static final Log logger = LogFactory.getLog(SkipToThisMonthInReportFormPlugin.class);
    /**
     * 控制权限中角色的编码
     */
    private static final String ROLE_ORG = "tdkw_thismonthentry_pc";

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        if (StringUtils.equals("person", hyperLinkClickEvent.getFieldName())) {
            // 点击获取人事业务档案的主键(隐藏列)
            DynamicObject rowData = hyperLinkClickEvent.getRowData();
            // 参数为查询配置的查询字段的标识  跳转参数
            String pkID = rowData.getString("personid");
            // 人员内码
            String personNumber = rowData.getString("person.number");
            // 判断权限
            boolean jurisdiction = Boolean.FALSE;
            // 判断离职
            boolean dimission = Boolean.FALSE;
            if (personNumber.length() != 6) {
                personNumber = personNumber.substring(0, 6);
            }
            // 人事业务档案过滤条件
            QFilter erManFileQfilter = new QFilter("person.number", QCP.equals, personNumber);
            erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
            erManFileQfilter.and("datastatus", QCP.equals, "1");
            erManFileQfilter.and("businessstatus", QCP.equals, "1");
            DynamicObject ermanfile = QueryServiceHelper.queryOne("hspm_ermanfile", "empposrel.adminorg.id as adminorg,empentrel.laborrelstatus.number as number", new QFilter[]{erManFileQfilter});

            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), ROLE_ORG);// 如果角色控权包含10000L的话 不对权限进行控制
            boolean hasAllOrgPerm = result.isHasAllOrgPerm();

            // 判断是否能穿透
            if (!hasAllOrgPerm) {
                List<Long> hasPerOrg = result.getHasPermOrgs();
                if (hasPerOrg.size() > 0) {
                    jurisdiction = hasPerOrg.contains(ermanfile.getLong("adminorg"));
                }
            } else {
                jurisdiction = Boolean.TRUE;
            }

            if (jurisdiction) {
                try {
                    String number = ermanfile.getString("number");
                    if (StringUtils.isNotEmpty(number)) {
                        if (StringUtils.equals(number, "XY00008") || StringUtils.equals(number, "XY00009") || StringUtils.equals(number, "XY000121") || StringUtils.equals(number, "XY00013")) {
                            dimission = Boolean.TRUE;
                        }
                    }
                    FormShowParameter formShowParameter = new FormShowParameter();
                    formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                    formShowParameter.setCustomParam("erfileId", pkID);
                    formShowParameter.setCustomParam("isLeave", dimission);
                    formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                    formShowParameter.setHasRight(true);
                    this.getView().showForm(formShowParameter);
                } catch (Exception e) {
                    this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "SkipToThisMonthInReportFormPlugin_1", "tdkw-esc-hrobs-formplugin-report"));
                }
            } else {
                this.getView().showErrorNotification(ResManager.loadKDString("该员工已调离本组织，您无权查看当前人员的详细信息！", "SkipToThisMonthInReportFormPlugin_2", "tdkw-esc-hrobs-formplugin-report"));
            }
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        // 给时间控件设置值
        setTime(this.getModel());
        DynamicObjectCollection employTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
        for (DynamicObject tempId : load) {
            employTypeForDisplay.add(tempId);
        }
        this.getModel().setValue("tdkw_laborreltype", employTypeForDisplay);
        getView().refresh();
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);
    }

    private void setTime(IDataModel model) {
        Pair<Date, Date> pair = getTime();
        model.setValue("tdkw_begindate", pair.getKey());
        model.setValue("tdkw_enddate", pair.getValue());
    }

    private Pair<Date, Date> getTime() {
        // 获取本月月初到今天
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date startTime = calendar.getTime();
        return new Pair<>(startTime, endTime);
    }
}