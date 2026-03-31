package tdkw.hrmp.hrobs.formplugin;

import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.exception.KDBizException;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.Label;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.hrmp.hrobs.common.app.api.AdminOrgChangeStatisticApi;
import tdkw.hrmp.hrobs.common.app.result.CustomApiResultEx;
import tdkw.hrmp.hrobs.formplugin.report.SkipToThisMonthInReport;
import tdkw.hrmp.hrobs.formplugin.report.SkipToThisMonthOutReport;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @version 1.0
 * @description: TODO
 * @date 2024/4/7 0007 上午 9:05
 */

public class LeaderQueryFormCountPlugin extends AbstractFormPlugin {
    private static final Log LOGGER = LogFactory.getLog(LeaderQueryFormCountPlugin.class);
    /**
     * 本月入职报表标识
     */
    public static final String MONTHONBOARDINGRPT = "tdkw_monthonboardingrpt";
    /**
     * 本月离职报表标识
     */
    public static final String MONTHLEAVE = "tdkw_monthleave";

    /**
     * 本月异动报表标识·
     */
    public static final String CHANGES_THIS_MONTH = "tdkw_monthchange";
    /**
     * 组织变动明细查询单据标识
     */
    private static final String HOMS_ORGCHGRECORD = "homs_orgchgrecord";
    /**
     * 组织新增控件标识
     */
    private static final String ADD_COUNT = "tdkw_addcount";
    /**
     * 组织撤销控件标识
     */
    private static final String BACK_COUNT = "tdkw_backcount";
    /**
     * 组织变动控件标识
     */
    private static final String CHANGE_COUNT = "tdkw_changecount";
    /**
     * 本月入职控件标识
     */
    private static final String INDUCTION_COUNT = "tdkw_inductioncount";
    /**
     * 本月离职控件标识
     */
    private static final String DEPARTURE_COUNT = "tdkw_departurecount";
    /**
     * 本月退休控件标识
     */
    private static final String RETIRE_COUNT = "tdkw_retire";
    /**
     * 本月异动控件标识
     */
    private static final String MOVE_COUNT = "tdkw_movecount";
    private static final List<String> LIST = Arrays.asList(
            "XY00003", "XY00004", "XY00005", "XY00006");
    private static final List<String> SINGLE_LIST = Arrays.asList("XY00008",
            "XY00009", "XY00010", "XY00012");
    private static final List<String> SINGLE_LIST2 = Arrays.asList("XY00011", "XY00013");

    private static final List<String> PERSON_LIST = Arrays.asList("XY00001", "XY00005", "XY00007", "XY00008", "XY00009");


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        AdminOrgChangeStatisticApi adminOrgChangeStatisticApi = new AdminOrgChangeStatisticApi();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        LOGGER.info("组织startdata---" + dateFormat.format(new Date()));

        try {
            CustomApiResult<CustomApiResultEx> count = adminOrgChangeStatisticApi.count();
            Map data = (Map) count.getData().getData();
            setLabName(ADD_COUNT, data == null ? "0" : data.get("addCount").toString());
            setLabName(BACK_COUNT, data == null ? "0" : data.get("backCount").toString());
            setLabName(CHANGE_COUNT, data == null ? "0" : data.get("changeCount").toString());
        } catch (ParseException ex) {
            throw new KDBizException("获取数据异常");
        }
        LOGGER.info("组织enddata---" + dateFormat.format(new Date()));

        LOGGER.info("入职startdata---" + dateFormat.format(new Date()));
        SkipToThisMonthInReport skipToThisMonthInReport = new SkipToThisMonthInReport();
        String thisMonthIn = skipToThisMonthInReport.getThisMonthIn(this.getClass().getName());
        setLabName(INDUCTION_COUNT, thisMonthIn == null ? "0" : thisMonthIn);
        LOGGER.info("入职enddata---" + dateFormat.format(new Date()));

        LOGGER.info("离职startdata---" + dateFormat.format(new Date()));
        SkipToThisMonthOutReport skipToThisMonthOutReport = new SkipToThisMonthOutReport();
        String thisMonthOut = skipToThisMonthOutReport.getThisMonthOut(this.getClass().getName(), "dimission");
        setLabName(DEPARTURE_COUNT, thisMonthOut == null ? "0" : thisMonthOut);
        LOGGER.info("离职enddata---" + dateFormat.format(new Date()));

        LOGGER.info("退休startdata---" + dateFormat.format(new Date()));
        String retireMonthOut = skipToThisMonthOutReport.getThisMonthOut(this.getClass().getName(), "retire");
        setLabName(RETIRE_COUNT, retireMonthOut == null ? "0" : retireMonthOut);
        LOGGER.info("退休enddata---" + dateFormat.format(new Date()));

        LOGGER.info("异动startdata---" + dateFormat.format(new Date()));
        //ChangesThisMonthRptPlugin changesThisMonthRptPlugin = new ChangesThisMonthRptPlugin();

        // TODO 缺少字段 临时屏蔽处理
//        String changeSize = changeSize();
        String changeSize = "2";
        setLabName(MOVE_COUNT, changeSize == null ? "0" : changeSize);
        LOGGER.info("异动enddata---" + dateFormat.format(new Date()));
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners(ADD_COUNT, BACK_COUNT, CHANGE_COUNT, INDUCTION_COUNT, DEPARTURE_COUNT, MOVE_COUNT, RETIRE_COUNT);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        // TODO 异常临时屏蔽处理
        /*switch (key) {
            // 组织新增
            case ADD_COUNT:
                showList(new String[]{"1010"});
                break;
            // 组织撤销
            case BACK_COUNT:
                showList(new String[]{"1040"});
                break;
            // 组织更名
            case CHANGE_COUNT:
                showList(new String[]{"1020", "1030"});
                break;
            // 本月入职
            case INDUCTION_COUNT:
                showReport(MONTHONBOARDINGRPT, null);
                break;
            // 本月离职
            case DEPARTURE_COUNT:
                showReport(MONTHLEAVE, "dimission");
                break;
            // 本月退休
            case RETIRE_COUNT:
                showReport(MONTHLEAVE, "retire");
                break;
            // 本月异动
            case MOVE_COUNT:
                showReport(CHANGES_THIS_MONTH, null);
                break;
            default:
                throw new KDBizException("未知处理类型");
        }*/
    }

    /**
     * 展示列表
     *
     * @param id 变动场景id
     */
    private void showList(String[] id) {
        ListShowParameter showParameter = new ListShowParameter();
        showParameter.setCustomParam("searchchangescene", id);
        showParameter.setSelectedEntity("subentryentity");
        showParameter.setBillFormId(HOMS_ORGCHGRECORD);
        showParameter.setHasRight(true);
        showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        this.getView().showForm(showParameter);
    }

    /**
     * 展示报表
     *
     * @param formId 报表标识
     */
    private void showReport(String formId, String diMissionType) {
        ReportShowParameter showParameter = new ReportShowParameter();
        showParameter.setFormId(formId);
        if (StringUtils.isNotEmpty(diMissionType)) {
            showParameter.setCustomParam("diMissionType", diMissionType);
        }
        showParameter.setHasRight(true);
        showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        this.getView().showForm(showParameter);
    }

    /**
     * 设置控件名称
     *
     * @param property 控件标识
     * @param name     控件名称
     */
    private void setLabName(String property, String name) {
        Label label = this.getControl(property);
        label.setText(name);
    }

    private String changeSize() {
        DynamicObjectCollection abnormalDataSet = abnormalDataSet();
        DynamicObjectCollection tempPosDataSet = tempPosDataSet();
        int i = abnormalDataSet.size() + tempPosDataSet.size();
        return String.valueOf(i);
    }

    private DynamicObjectCollection abnormalDataSet() {
        List<Long> orgIds = getOrgIds();
        QFilter flowQFilter = new QFilter("laborreltype.number", QCP.in, PERSON_LIST).and("flowtime", ">=", getDate("startdate")).and("flowtime", "<", getDate("enddate"));
        if (ObjectUtils.isNotEmpty(orgIds)) {
            flowQFilter.and("adminorghis", QCP.in, orgIds);
        }
        DynamicObject[] query = HRBaseServiceHelper.create("hpfs_personflow").query("person", new QFilter[]{flowQFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter powerFilter = new QFilter("person", QCP.in, personIds);


        QFilter inflowFilter = new QFilter("laborreltype.number", QCP.in, PERSON_LIST).and("flowtime", ">=", getDate("startdate")).and("flowtime", "<", getDate("enddate")).and(powerFilter);
        QFilter inflowFilter1 = new QFilter("laborreltype.number", QCP.in, PERSON_LIST).and("flowtime", ">=", getDate()).and("flowtime", "<", getDate("enddate")).and(powerFilter);

        QFilter outflowFilter = new QFilter("flowtime", ">=", getDate("startdate")).and("flowtime", "<", getDate("enddate")).and(powerFilter);

        QFilter employeeFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE).and("datastatus", QCP.equals, "1").and(powerFilter);


        //任职经历总
        DataSet employeeDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hrpi_emporgrelall", "depemp,depemp.position.boid as oldpositionboid,depemp.position.adminorg as oldadminorg,position as oldposition", new QFilter[]{employeeFilter});

        //流入流出
        QFilter moveFilter = new QFilter("tdkw_changetype.number", QCP.in, LIST);
        QFilter flowTypeInFilter = new QFilter("flowtype", QCP.equals, "1").and(moveFilter);
        QFilter flowTypeOutFilter = new QFilter("flowtype", QCP.equals, "2").and(moveFilter);

        //查询报表数据
        DataSet inDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hpfs_personflow", "person,bill,depemp,depemp.position.name as positionname,depemp.position.boid as positionboid,depemp.adminorg as adminorg",
                new QFilter[]{inflowFilter, flowTypeInFilter});
        DataSet outDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hpfs_personflow", "person,bill,depemp", new QFilter[]{outflowFilter, flowTypeOutFilter});


        outDataSet = outDataSet.leftJoin(employeeDataSet).on("depemp", "depemp").select("oldpositionboid", "oldadminorg", "person", "depemp", "bill", "oldposition").finish();
        long startTime = System.currentTimeMillis();
        DynamicObjectCollection outDataSetSetCollection = ORM.create().toPlainDynamicObjectCollection(outDataSet.copy());
        Map<String, String> person = new HashMap<>();
        for (DynamicObject dynamicObject : outDataSetSetCollection) {
            if (!"".equals(dynamicObject.getString("bill"))) {
                person.put(dynamicObject.getString("bill"), (dynamicObject.getString("oldpositionboid") + dynamicObject.getString("oldposition") + dynamicObject.getString("oldadminorg")));
            }
        }
        LOGGER.info("遍历任职经历存map耗时:" + (System.currentTimeMillis() - startTime));
        LOGGER.info("Map信息：" + person);
        inDataSet = inDataSet.filter(new FilterFunction() {
            @Override
            public boolean test(Row row) {
                String newName = row.getString("positionboid") + row.getString("positionname") + row.getString("adminorg");
                String bill = row.getString("bill");
                String oldName = person.get(bill);
                if (Objects.equals(oldName, newName)) {
                    LOGGER.info("新岗位信息" + newName + "原岗位信息" + oldName);
                }
                return !Objects.equals(oldName, newName);
            }
        });
        LOGGER.info("过滤耗时:" + (System.currentTimeMillis() - startTime));

        DataSet doubleAbnormalDataSet = inDataSet.join(outDataSet).select().on("bill", "bill").select("person").finish();


        //只有流入获流出的数据
        QFilter outFlowFilter = new QFilter("tdkw_changetype.number", QCP.in, SINGLE_LIST);
        QFilter outFlowFilter1 = new QFilter("tdkw_changetype.number", QCP.in, SINGLE_LIST2);
        QFilter qFilter = new QFilter("person", QCP.in, personIds);

        //查询报表数据
        DataSet singleAbnormalDataSet = ORM.create().queryDataSet(this.getClass().getName(),
                "hpfs_personflow", "person", new QFilter[]{outFlowFilter,inflowFilter,qFilter});
        DataSet singleAbnormalDataSet1 = ORM.create().queryDataSet(this.getClass().getName(),
                "hpfs_personflow", "person", new QFilter[]{outFlowFilter1,inflowFilter1,qFilter});
        DataSet dataSet = doubleAbnormalDataSet.union(singleAbnormalDataSet).union(singleAbnormalDataSet1);

        DynamicObjectCollection dynamicObjects = ORM.create().toPlainDynamicObjectCollection(dataSet);
        LOGGER.info("abnormalDataSet方法的人员id" + dynamicObjects);
        return dynamicObjects;
    }

    private DynamicObjectCollection tempPosDataSet() {
        List<Long> orgIds = getOrgIds();
        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1").and("isprimary", QCP.equals, "1");

        if (ObjectUtils.isNotEmpty(orgIds)) {
            qFilter.and("adminorg", QCP.in, orgIds);
        }
        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create("hrpi_empposorgrel");
        DynamicObject[] query = hrBaseServiceHelper.query("person", new QFilter[]{qFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter laborFilter = new QFilter("person.id", QCP.in, personIds);
        QFilter empOrgFilter = new QFilter("postype", QCP.equals, "挂职")
                .and("iscurrentversion", QCP.equals, Boolean.TRUE)
                .and("datastatus", QCP.equals, "1")
                .and("startdate", ">=", getDate("startdate"))
                .and("startdate", "<", getDate("enddate"))
                .and(laborFilter);
        DynamicObjectCollection tenureDataSet = QueryServiceHelper.query("hrpi_emporgrelout",
                "person", new QFilter[]{empOrgFilter});
        LOGGER.info("tempPosDataSet方法的人员id" + tenureDataSet);
        return tenureDataSet;
    }

    private Date getDate(String string) {
        //获取本月月初
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        Date date = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (org.apache.commons.lang3.StringUtils.equals(string, "startdate")) {
            return date;
        } else {
            return new Date();
        }
    }

    private Date getDate() {
        //获取本月月初
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        Date date = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        date = calendar.getTime();
        return date;
    }



    private List<Long> getOrgIds() {
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_monthchanges_pc");//如果角色控权包含10000L的话 不对权限进行控制
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            return hasPerOrg;
        }
        return null;
    }

}
