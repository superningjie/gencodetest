package tdkw.hrmp.hrobs.formplugin.report;

import javafx.util.Pair;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.form.ClientProperties;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * SkipToThisMonthOutReportFormPlugin
 *
 * @author xxx
 * @date 2023/7/14
 */
public class SkipToThisMonthOutReportFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {


    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        super.beforeQuery(queryParam);
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.getControl("tdkw_toolbarap");
        Toolbar toolBar = this.getControl("tdkw_toolbarap");
        toolBar.addItemClickListener(this);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
    }

    private static final Log logger = LogFactory.getLog(SkipToThisMonthOutReportFormPlugin.class);

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        if (StringUtils.equals("person", hyperLinkClickEvent.getFieldName())) {
            //点击获取人事业务档案的主键(隐藏列)
            DynamicObject rowData = hyperLinkClickEvent.getRowData();
            //参数为查询配置的查询字段的标识
            String pkId = (String) rowData.get("personid");
            //查询人员工号，判断是否在入职
            String personNumber = rowData.getString("person.number");
            logger.info("跳转的人员pkid为" + pkId);
            logger.info("跳转的人员personNumber为" + personNumber);
            //人员是否入职
            boolean ifReEmp = false;
            //人员是否有权限
            boolean isOrgPerm = false;
            //判断是否再入职
            if (personNumber.length() != 6) {
                //截取前6位人员工号
                personNumber = personNumber.substring(0, 6);
                //人员再入职
                ifReEmp = true;
            }
            //在入职人员获取最新任职经历中对应部门
            Long personAmdinorg;
            if (ifReEmp) {
                //人员员工号
                QFilter erManFileQfilter = new QFilter("person.number", QCP.equals, personNumber);
                //初始化状态
                erManFileQfilter.and("initstatus", QCP.equals, "2");
                //是否当前版本
                erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
                //数据版本状态
                erManFileQfilter.and("datastatus", QCP.equals, "1");
                //业务档案状态
                erManFileQfilter.and("businessstatus", QCP.equals, "1");
                DynamicObject objectCollection = QueryServiceHelper.queryOne("hspm_ermanfile", "empposrel.company.id as company,empposrel.adminorg.id as adminorg", new QFilter[]{erManFileQfilter});
                personAmdinorg = objectCollection.getLong("adminorg");
            } else {
                personAmdinorg = rowData.getLong("tdkw_adminorg.id");
            }
            //获取当前人员权限
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_thismonthdepart_pc");
            if (!result.isHasAllOrgPerm()) {
                if (result.getHasPermOrgs().contains(personAmdinorg)) {
                    isOrgPerm = true;
                }
            } else {
                isOrgPerm = true;
            }
            if (isOrgPerm) {
                try {
                    FormShowParameter formShowParameter = new FormShowParameter();
                    formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                    formShowParameter.setCustomParam("erfileId", pkId);
                    formShowParameter.setCustomParam("isLeave", Boolean.TRUE);
                    formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                    formShowParameter.setHasRight(true);
                    this.getView().showForm(formShowParameter);
                } catch (Exception e) {
                    this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "SkipToThisMonthOutReportFormPlugin", "tdkw-esc-hrobs-formplugin-ext", new Object[0]));
                }
            } else {
                this.getView().showErrorNotification("该员工已入职XXX内其他组织，您无权查看当前人员的详细信息");
            }

        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //给离职日期赋值
        setTime(this.getModel());
        DynamicObjectCollection employTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
        for (DynamicObject tempId : load) {
            employTypeForDisplay.add(tempId);
        }
        this.getModel().setValue("tdkw_laborreltype", employTypeForDisplay);

        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        //离职类型
        Object diMissionType = formShowParameter.getCustomParams().get("diMissionType");
        if (ObjectUtils.isNotEmpty(diMissionType)) {
            //退休
            if ("retire".equals(diMissionType)) {
                //this.getModel().setValue("tdkw_dimissionorretire", "retire");
                setNotSelect("tdkw_leavebuttonap1");
                setSelect("tdkw_retirebuttonap1");
            }
            //离职
            if ("dimission".equals(diMissionType)) {
                // this.getModel().setValue("tdkw_dimissionorretire", "dimission");
                setNotSelect("tdkw_retirebuttonap1");
                setSelect("tdkw_leavebuttonap1");
            }
        }
        Map<String, Object> customParam = new HashMap<>();
        customParam.put("diMissionType", diMissionType);
        this.getQueryParam().setCustomParam(customParam);

        getView().refresh();
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> customParams = formShowParameter.getCustomParams();
        String diMissionType = null;
        String itemKey = evt.getItemKey();
        if (StringUtils.equals(itemKey, "tdkw_leavebuttonap1")) {
            setSelect("tdkw_leavebuttonap1");
            setNotSelect("tdkw_retirebuttonap1");
            diMissionType = "dimission";
        } else if (StringUtils.equals(itemKey, "tdkw_retirebuttonap1")) {
            setSelect("tdkw_retirebuttonap1");
            setNotSelect("tdkw_leavebuttonap1");
            diMissionType = "retire";
        }
        if (StringUtils.isNotBlank(diMissionType)) {
            customParams.put("diMissionType", diMissionType);
            this.getQueryParam().setCustomParam(customParams);
        }
        getView().refresh();
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);

    }

    private void setSelect(String toolbar) {
        HashMap<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(ClientProperties.BackColor, "#276ff5");
        fieldMap.put(ClientProperties.ForeColor, "#f2f2f2");
        Map<String, Object> s = new HashMap<>();
        Map<String, Object> b = new HashMap<>();
        b.put("r", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        b.put("l", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        b.put("t", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        b.put("b", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        s.put("b", b);
        fieldMap.put("s", s);
        this.getView().updateControlMetadata(toolbar, fieldMap);


    }

    private void setNotSelect(String toolbar) {
        HashMap<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(ClientProperties.BackColor, "#f2f2f2");
        fieldMap.put(ClientProperties.ForeColor, "#276ff5");
        Map<String, Object> s = new HashMap<>();
        Map<String, Object> b = new HashMap<>();
        b.put("r", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        b.put("l", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        b.put("t", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        b.put("b", "1px_solid_#276ff5");// 拼接为 宽度_边框样式_颜色
        s.put("b", b);
        fieldMap.put("s", s);
        this.getView().updateControlMetadata(toolbar, fieldMap);
    }

    private void setTime(IDataModel model) {
        Pair<Date, Date> pair = getTime();
        model.setValue("tdkw_begindate", pair.getKey());
        model.setValue("tdkw_enddate", pair.getValue());
    }

    private Pair<Date, Date> getTime() {
        //获取本月月初到今天
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date startTime = calendar.getTime();
        Pair<Date, Date> pair = new Pair<>(startTime, endTime);
        return pair;
    }

}
