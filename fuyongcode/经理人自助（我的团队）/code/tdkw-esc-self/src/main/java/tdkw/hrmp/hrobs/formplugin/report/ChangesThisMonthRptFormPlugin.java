package tdkw.hrmp.hrobs.formplugin.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.field.DateRangeEdit;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * @author xxx
 * @date： 2023/8/8
 * @description : 本月异动报表表单插件-点击跳转人事业务档案
 */
public class ChangesThisMonthRptFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {
    private static final Log logger = LogFactory.getLog(ChangesThisMonthRptFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        Object tdkw_person = hyperLinkClickEvent.getRowData().getDynamicObject("tdkw_person").getPkValue();
        DynamicObject person = BusinessDataServiceHelper.loadSingle(tdkw_person, "hrpi_person");
        //人员工号 查询组织用
        String personNumber = person.getString("number");
        logger.info("跳转的人员工号为" + personNumber);
        //判断是否再入职
        if (personNumber.length() != 6) {
            personNumber = personNumber.substring(0, 6);
        }
        //判断权限
        Boolean jurisdiction = Boolean.FALSE;
        //判断离职
        Boolean dimission = Boolean.FALSE;
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_monthchanges_pc");
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        QFilter erManFileQfilter = new QFilter("person.number", QCP.equals, personNumber);
        erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
        erManFileQfilter.and("datastatus", QCP.not_equals, "-1");
        erManFileQfilter.and("businessstatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");
        DynamicObjectCollection ermanfile = QueryServiceHelper.query("hspm_ermanfile", "empposrel.company.id as company,empposrel.adminorg.id as adminorg,empentrel.laborrelstatus.number as number,empposrel.startdate", new QFilter[]{erManFileQfilter});
        //判断是否能穿透
        // 判断是否能穿透
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            Set<Long> allOrg = new HashSet<>();
            if(hasPerOrg.size() > 0) {
                if(ObjectUtils.isNotEmpty(ermanfile)) {
                    for (DynamicObject obj : ermanfile) {
                        if ( ObjectUtils.isNotEmpty(obj.getLong("company")) ){
                            allOrg.add(obj.getLong("company"));
                        }
                        if( ObjectUtils.isNotEmpty(obj.getLong("adminorg")) ){
                            allOrg.add(obj.getLong("adminorg"));
                        }
                    }
                    logger.info("所有组织数据" + allOrg);
                    for (Long aLong : allOrg) {
                        if(hasPerOrg.contains(aLong)) {
                            jurisdiction = Boolean.TRUE;
                        }
                    }
                }
            }
        }else{
            jurisdiction = Boolean.TRUE;
        }

        if (jurisdiction) {
            try {
                for (DynamicObject obj : ermanfile) {
                    String number = obj.getString("number");
                    if(StringUtils.isNotEmpty(number)) {
                        if (StringUtils.equals(number, "XY00008") || StringUtils.equals(number, "XY00009") || StringUtils.equals(number, "XY000121") || StringUtils.equals(number, "XY00013")) {
                            dimission = Boolean.TRUE;
                        }
                    }
                }
                FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                formShowParameter.setCustomParam("erfileId", person.getString("tdkw_pkid"));
                formShowParameter.setCustomParam("isLeave", dimission);
                formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                formShowParameter.setHasRight(true);
                this.getView().showForm(formShowParameter);
            } catch (Exception e) {
                this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "RightTableClickFormPlugin", "tdkw-esc-tdkw_integrated_query-report-ext", new Object[0]));
            }
        } else {
            this.getView().showErrorNotification("该员工已调离本组织，您无权查看当前人员的详细信息！");
        }
        logger.info("是否有权限查看" + jurisdiction);
        logger.info("是否离职" + dimission);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);

        DynamicObjectCollection employTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
        for (DynamicObject tempId : load) {
            employTypeForDisplay.add(tempId);
        }
        this.getModel().setValue("tdkw_laborreltypefilter", employTypeForDisplay);
        demoFieldValue();
        getView().refresh();
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);
    }

    private void demoFieldValue() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        Date date = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        // 需要通过如下方式，获取日期范围字段，开始、结束属性对象的标识
        DateRangeEdit headFieldEdit = this.getView().getControl("tdkw_daterangefilter");
        String key_headdatestart = headFieldEdit.getStartDateFieldKey();
        String key_headdateend = headFieldEdit.getEndDateFieldKey();

        // 赋值
        this.getModel().setValue(key_headdatestart, date);
        this.getModel().setValue(key_headdateend, new Date());
    }
}
