package tdkw.hrmp.hrobs.formplugin.report;

import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @version 1.0
 * @description: TODO
 * @date 2024/3/29 0029 下午 5:35
 */

public class ChangeThisMonthRptPluginTest extends AbstractReportListDataPlugin {
    private static final Log LOGGER = LogFactory.getLog(ChangeThisMonthRptPluginTest.class);
    private static final List<String> LIST = Arrays.asList(
            "XY00003", "XY00004", "XY00005", "XY00006");
    private static final List<String> SINGLE_LIST = Arrays.asList("XY00008",
            "XY00009", "XY00010", "XY00012");
    private static final List<String> SINGLE_LIST2 = Arrays.asList("XY00011", "XY00013");
    private static final List<String> PERSON_LIST = Arrays.asList("XY00001", "XY00005", "XY00007", "XY00008", "XY00009");


    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        DataSet dataSet = abnormalDataSet(reportQueryParam);
        DataSet dataSet1 = tempPosDataSet(reportQueryParam);
        return dataSet.union(dataSet1);
    }

    private DataSet abnormalDataSet(ReportQueryParam reportQueryParam) {
        List<Long> orgIds = getOrgIds();
        QFilter flowQFilter = new QFilter("laborreltype.number", QCP.in, PERSON_LIST).and("flowtime", ">=", getDate(reportQueryParam).get("startdate")).and("flowtime", "<", getDate(reportQueryParam).get("enddate2"));
        if(ObjectUtils.isNotEmpty(orgIds)){
            flowQFilter.and("adminorghis", QCP.in, orgIds);
        }
        DynamicObject[] query = HRBaseServiceHelper.create("hpfs_personflow").query("person", new QFilter[]{flowQFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter powerFilter = new QFilter("person", QCP.in, personIds);


        QFilter inflowFilter = new QFilter("laborreltype.number", QCP.in, PERSON_LIST).and("flowtime", ">=",  getDate(reportQueryParam).get("startdate")).and("flowtime", "<", getDate(reportQueryParam).get("enddate")).and(powerFilter);
        QFilter inflowFilter1 = new QFilter("laborreltype.number", QCP.in, PERSON_LIST).and("flowtime", ">=", getDate(reportQueryParam).get("startdate2")).and("flowtime", "<", getDate(reportQueryParam).get("enddate2")).and(powerFilter);


        QFilter outflowFilter = new QFilter("flowtime", ">=", getDate(reportQueryParam).get("startdate")).and("flowtime", "<", getDate(reportQueryParam).get("enddate")).and(powerFilter);

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
                if( Objects.equals(oldName, newName) ){
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
                "hpfs_personflow", "person", new QFilter[]{outFlowFilter, inflowFilter, qFilter});
        DataSet singleAbnormal2 = ORM.create().queryDataSet(this.getClass().getName(),
                "hpfs_personflow","person", new QFilter[]{outFlowFilter1, inflowFilter1, qFilter});
        DataSet dataSet = doubleAbnormalDataSet.union(singleAbnormalDataSet).union(singleAbnormal2);
        LOGGER.info("abnormalDataSet方法的人员id" + ORM.create().toPlainDynamicObjectCollection(dataSet.copy()));
        return dataSet;
    }

    private DataSet tempPosDataSet(ReportQueryParam reportQueryParam) {
        List<Long> orgIds = getOrgIds();
        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1").and("isprimary", QCP.equals, "1");

        if(ObjectUtils.isNotEmpty(orgIds)){
            qFilter.and("adminorg", QCP.in, orgIds);
        }
        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create("hrpi_empposorgrel");
        DynamicObject[] query = hrBaseServiceHelper.query("person", new QFilter[]{qFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter laborFilter = new QFilter("person.id", QCP.in, personIds);
        QFilter empOrgFilter = new QFilter("postype", QCP.equals, "挂职")
                .and("iscurrentversion", QCP.equals, Boolean.TRUE)
                .and("datastatus", QCP.equals, "1")
                .and("startdate", ">=", getDate(reportQueryParam).get("startdate"))
                .and("startdate", "<", getDate(reportQueryParam).get("enddate"))
                .and(laborFilter);
        DataSet tenureDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hrpi_emporgrelout",
                "person", new QFilter[]{empOrgFilter}, null);
        LOGGER.info("tempPosDataSet方法的人员id" + ORM.create().toPlainDynamicObjectCollection(tenureDataSet.copy()));
        return tenureDataSet;
    }
    private Map<String,Date> getDate(ReportQueryParam reportQueryParam) {
        Map map = new HashMap<String,Date>();
        //获取本月月初
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        Date date = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        map.put("startdate",date);
        map.put("enddate",new Date());
        FilterInfo filter = reportQueryParam.getFilter();
        List<FilterItemInfo> filterItems = filter.getFilterItems();
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            if (value == null) {
                continue;
            }
            if( propName.equals("startdate") ){
                map.put("startdate", value);
                Date startdate2 = (Date) value;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startdate2);
                calendar.add(Calendar.DATE, 1);
                startdate2 = calendar.getTime();
                map.put("startdate2",startdate2);
            }
            if( propName.equals("enddate")){
                LocalDate localDate = ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate newLocalDate = localDate.plusDays(1);
                Date newDate = Date.from(newLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                map.put("enddate", newDate);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(newDate);
                calendar1.add(Calendar.DATE, 1);
                Date enddate2 = calendar1.getTime();
                map.put("enddate2", enddate2);
            }
        }
        LOGGER.info("map的数据" + map);
        return map;
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
