package dgdl.odc.homs.task;

import dgdl.odc.homs.common.DateTimeCommon;
import kd.bos.algo.DataSet;
import kd.bos.algo.GroupbyDataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.threads.ThreadPools;
import kd.hr.haos.mservice.HAOSBatchAdminOrgInfoQueryService;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @Author 姚帅
 * @Date 2024/03/07 13:40
 * @Version 1.0
 * 任务提醒面板
 */
public class YearReportExportTask extends AbstractTask {

    private static final Log logger = LogFactory.getLog(YearReportExportTask.class);

    /**
     * 任职经历
     */
    private final static String HRPI_EMPPOSORGREL = "hrpi_empposorgrel";

    /**
     * 职业信息
     */
    private final static String EMPENTREL = "hrpi_empentrel";

    /**
     * 个人职级
     */
    private final static String PERSON = "dgdl_personagerank";


    /**
     * 岗位标签数组
     */
    private final static String[] JOBLABEL_ARR = new String[]{"1", "2", "3", "4"};

    /**
     * 任职经历获取字段
     */
    private final static String EMP_FILED = "id as empId,person.id as personId," +
            "job.id as dgdl_stdposition," +
            "adminorg.id as adminorgId," +
            "job.dgdl_post_aisle.id as dgdl_post_aisle," +
            "job.dgdl_jobproperty.id as dgdl_jobproperty," +
            "job.dgdl_joblabel.fbasedataid as jobLabelId," +
            "position.dgdl_external_dispatch as dgdl_dispatch," +
            "position.workplace.id as dgdl_workplace";

    /**
     * 工作地点查询字段
     */
    private final static String LOCATION_FILED = "dgdl_employee.person.id as personId,dgdl_coresubsidyentity.dgdl_subsilocation.id as subsilocation";

    private final static String ALGO = "dgdl.odc.homs.task.YearReportExportTask";

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    /**
     * 查询字段
     */
    private static final String FILED = "id,billstatus,dgdl_synctime," +
            "dgdl_adminorg1," +
            "dgdl_adminorg2," +
            "dgdl_adminorg3," +
            "dgdl_adminorg4," +
            "dgdl_adminorg5," +
            "dgdl_adminorg6," +
            "adminorg," +
            "dgdl_planyear," +
            "dgdl_islastorg," +
            "dgdl_entry.dgdl_groupid," +
            "dgdl_entry.seq," +
            "dgdl_entry.dgdl_detail_adminorg1," +
            "dgdl_entry.dgdl_detail_adminorg2," +
            "dgdl_entry.dgdl_detail_adminorg3," +
            "dgdl_entry.dgdl_detail_adminorg4," +
            "dgdl_entry.dgdl_detail_adminorg5," +
            "dgdl_entry.dgdl_detail_adminorg6," +
            "dgdl_entry.dgdl_detail_adminorg," +
            "dgdl_entry.dgdl_laborreltype," +
            "dgdl_entry.dgdl_stdposition," +
            "dgdl_entry.dgdl_personrank," +
            "dgdl_entry.dgdl_post_aisle," +
            "dgdl_entry.dgdl_jobproperty," +
            "dgdl_entry.dgdl_label1," +
            "dgdl_entry.dgdl_label2," +
            "dgdl_entry.dgdl_label3," +
            "dgdl_entry.dgdl_label4," +
            "dgdl_entry.dgdl_dispatch," +
            "dgdl_entry.dgdl_workplace," +
            "dgdl_entry.dgdl_actual," +
            "dgdl_entry.dgdl_add," +
            "dgdl_entry.dgdl_reduce," +
            "dgdl_entry.dgdl_plan," +
            "dgdl_entry.dgdl_control," +
            "dgdl_entry.dgdl_prechg10," +
            "dgdl_entry.dgdl_prechg11," +
            "dgdl_entry.dgdl_prechg12," +
            "dgdl_entry.dgdl_chg1," +
            "dgdl_entry.dgdl_chg2," +
            "dgdl_entry.dgdl_chg3," +
            "dgdl_entry.dgdl_chg4," +
            "dgdl_entry.dgdl_chg5," +
            "dgdl_entry.dgdl_chg6," +
            "dgdl_entry.dgdl_chg7," +
            "dgdl_entry.dgdl_chg8," +
            "dgdl_entry.dgdl_chg9," +
            "dgdl_entry.dgdl_chg10," +
            "dgdl_entry.dgdl_chg11," +
            "dgdl_entry.dgdl_chg12," +
            "dgdl_entry.dgdl_chg13," +
            "dgdl_entry.dgdl_preavg10," +
            "dgdl_entry.dgdl_preavg11," +
            "dgdl_entry.dgdl_preavg12," +
            "dgdl_entry.dgdl_avg1," +
            "dgdl_entry.dgdl_avg2," +
            "dgdl_entry.dgdl_avg3," +
            "dgdl_entry.dgdl_avg4," +
            "dgdl_entry.dgdl_avg5," +
            "dgdl_entry.dgdl_avg6," +
            "dgdl_entry.dgdl_avg7," +
            "dgdl_entry.dgdl_avg8," +
            "dgdl_entry.dgdl_avg9," +
            "dgdl_entry.dgdl_avg10," +
            "dgdl_entry.dgdl_avg11," +
            "dgdl_entry.dgdl_avg12," +
            "dgdl_entry.dgdl_mulperson," +
            "dgdl_entry.dgdl_average";


    /**
     * 查询下级组织微服务接口类
     */
    private static HAOSBatchAdminOrgInfoQueryService hAOSBatchAdminOrgInfoQueryService = new HAOSBatchAdminOrgInfoQueryService();


    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        try {
            feedbackProgress(10, "开始执行任务", null);
            //执行逻辑
            dataEncapsulation(map);
            //执行逻辑
        } catch (Exception e) {
            HashMap<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", e.getMessage());
            logger.error("YearReportExportTask报错信息", e);
            feedbackCustomdata(result);
        }
    }


    /**
     * 查询报表数据进行封装
     */
    private void dataEncapsulation(Map<String, Object> map) throws InterruptedException {
        Long planId = (Long) map.get("planId");
        List<Long> pkValueList = (List<Long>) map.get("pkValueList");
        DynamicObject planYear = BusinessDataServiceHelper.loadSingle(planId, "dgdl_planyear");
        //开始月份
        Date dgdlStartmonth = planYear.getDate("dgdl_startmonth");
        //获取编制规划单位
        DynamicObject dgdlOrg = planYear.getDynamicObject("dgdl_org");
        Long orgId = (Long) dgdlOrg.getPkValue();
        //格式化开始月份
        Date monthStartDate = DateTimeCommon.getMonthStartDate(dgdlStartmonth);
        Date syndate = DateTimeCommon.addDay(monthStartDate, -1);
        logger.error("syndate:{}", syndate);

        if (syndate.compareTo(new Date()) > 0) {
            syndate = new Date();
        }
        //获取任职经历查询条件
        QFilter empFiler = new QFilter("iscurrentversion", QCP.equals, "1")
                .and("datastatus", QCP.equals, "1")
                .and("isprimary", QCP.equals, "1")
                .and("businessstatus", QCP.equals, "1")
                .and("adminorg.org.id", QCP.equals, orgId)
                .and("enddate", QCP.large_equals, syndate)
                .and("startdate", QCP.less_equals, syndate);
        //获取任职经历信息
        DataSet empDataSet = QueryServiceHelper.queryDataSet(ALGO, HRPI_EMPPOSORGREL, EMP_FILED, empFiler.toArray(), "");
        //个人职级
        QFilter personQfiler = new QFilter("iscurrentversion", QCP.equals, "1")
                .and("datastatus", QCP.equals, "1")
                .and("businessstatus", QCP.equals, "1")
                .and("enddate", QCP.large_equals, syndate).and("startdate", QCP.less_equals, syndate);
        DataSet personData = QueryServiceHelper.queryDataSet(ALGO, PERSON, "id,person.id as person,dgdl_personrank.id as dgdl_personrank", personQfiler.toArray(), "");
        //存储个人职级
        Map<Long, DynamicObject> personMap = new HashMap<>();
        DataSet personDatacopy=personData.copy();
        while (personDatacopy.hasNext()) {
            Row next = personDatacopy.next();
            if (next.getLong("dgdl_personrank")>0l) {
                DynamicObject jobLevelObj = BusinessDataServiceHelper.loadSingle(next.getLong("dgdl_personrank"), "hbjm_joblevelhr", "id,joblevelscm");
                QFilter personQfilter = new QFilter("joblevelscm.id", QCP.equals, jobLevelObj.getDynamicObject("joblevelscm").getLong("id"));
                DynamicObject rankObj = BusinessDataServiceHelper.loadSingle("hbjm_joblevelhr", "id", personQfilter.toArray());
                personMap.put(next.getLong("dgdl_personrank"), rankObj);
            }
        }
        //查询核心补贴
        QFilter filter = new QFilter("iscurrentversion", QCP.equals, "1");
        filter.and("dgdl_coresubsidyentity.dgdl_startdate", QCP.less_equals, syndate)
                .and("dgdl_coresubsidyentity.dgdl_enddate", QCP.large_equals, syndate);
        DataSet locationDataSet = QueryServiceHelper.queryDataSet(ALGO, "dgdl_hiscoresubsidies", LOCATION_FILED, filter.toArray(), null);
        //查询岗位标签
        QFilter joblabelFilter = new QFilter("enable", QCP.equals, "1");
        DataSet joblabelDataSet = QueryServiceHelper.queryDataSet(ALGO, "dgdl_hbss_joblabel", "id,number", joblabelFilter.toArray(), "");
        feedbackProgress(20, "开始执行任务", null);
        //获取属于末级组织的信息
        QFilter detailQFilter = new QFilter("id", QCP.in, pkValueList);
        DynamicObject[] detailObj = BusinessDataServiceHelper.load("dgdl_planyear_detail", FILED, detailQFilter.toArray());
        logger.info("YearReportExportTask末级组织需要执行的长度" + detailObj.length);
        for (DynamicObject detailData : detailObj) {
            //获取组织层级
            DynamicObject dgdlAdminorg1 = detailData.getDynamicObject("dgdl_adminorg1");
            DynamicObject dgdlAdminorg2 = detailData.getDynamicObject("dgdl_adminorg2");
            DynamicObject dgdlAdminorg3 = detailData.getDynamicObject("dgdl_adminorg3");
            DynamicObject dgdlAdminorg4 = detailData.getDynamicObject("dgdl_adminorg4");
            DynamicObject dgdlAdminorg5 = detailData.getDynamicObject("dgdl_adminorg5");
            DynamicObject dgdlAdminorg6 = detailData.getDynamicObject("dgdl_adminorg6");
            //末端组织
            DynamicObject lastOrg = detailData.getDynamicObject("adminorg");
            //获取当前组织人员
            DataSet thisOrgDataSet;
            //任职经历
            DataSet empCopyDataSet = empDataSet.copy();
            //个人职级
            DataSet personCopyDataSet = personData.copy();
            //查询核心补贴
            DataSet locationCopyDataSet = locationDataSet.copy();
            //岗位
            DataSet joblabelCopyDataSet = joblabelDataSet.copy();
            //是否末级组织:true = 不需要获取当前及以下组织
            boolean isLastOrg = detailData.getBoolean("dgdl_islastorg");
            if (isLastOrg) {
                thisOrgDataSet = empCopyDataSet.filter("adminorgId = " + lastOrg.getLong("id"));
            } else {
                //获取当前组织及以下组织的成员信息
                List<Long> orgIdList = this.getAllOrg(lastOrg.getLong("id"));
                QFilter qFilter = new QFilter("iscurrentversion", QCP.equals, "1")
                        .and("datastatus", QCP.equals, "1")
                        .and("isprimary", QCP.equals, "1")
                        .and("businessstatus", QCP.equals, "1")
                        .and("adminorg.id", QCP.in, orgIdList)
                        .and("enddate", QCP.large_equals, syndate).and("startdate", QCP.less_equals, syndate);
                thisOrgDataSet = QueryServiceHelper.queryDataSet(ALGO, HRPI_EMPPOSORGREL, EMP_FILED, qFilter.toArray(), "");
            }
            //存储人员信息
            Map<Long, String> personMsg = new HashMap<>();
            //分组字段
            List<String> groupList = new ArrayList<>();
            //获取信息来源
            DataSet groupDataSet = this.getDataSet(thisOrgDataSet, planYear, syndate, lastOrg, personMsg, groupList, personCopyDataSet, locationCopyDataSet, joblabelCopyDataSet);
            List<String> fieldNames = Arrays.asList(groupDataSet.getRowMeta().getFieldNames());
            //获取分录
            DynamicObjectCollection dgdlEntryCol = detailData.getDynamicObjectCollection("dgdl_entry");
            //分录赋值
            while (groupDataSet.hasNext()) {
                Row row = groupDataSet.next();
                //分组唯一值
                StringBuilder groupId = new StringBuilder();
                for (String s : groupList) {
                    groupId.append(row.getString(s)).append("-");
                }
                groupId.append(lastOrg.getPkValue());
                DynamicObject addNew = dgdlEntryCol.addNew();
                //组织
                addNew.set("dgdl_detail_adminorg1", dgdlAdminorg1);
                addNew.set("dgdl_detail_adminorg2", dgdlAdminorg2);
                addNew.set("dgdl_detail_adminorg3", dgdlAdminorg3);
                addNew.set("dgdl_detail_adminorg4", dgdlAdminorg4);
                addNew.set("dgdl_detail_adminorg5", dgdlAdminorg5);
                addNew.set("dgdl_detail_adminorg6", dgdlAdminorg6);
                //末级组织
                addNew.set("dgdl_detail_adminorg", lastOrg);
                //用工类型
                if (fieldNames.contains("dgdl_laborreltype")) {
                    DynamicObject hbssLaborreltypeObj = BusinessDataServiceHelper.newDynamicObject("hbss_laborreltype");
                    hbssLaborreltypeObj.set("id", row.getLong("dgdl_laborreltype"));
                    addNew.set("dgdl_laborreltype", hbssLaborreltypeObj);
                }
                //个人职级(HR主实体)
                Long dgdlPersonrank = row.getLong("dgdl_personrank");
                DynamicObject rankObj = personMap.get(dgdlPersonrank);
//                addNew.set("dgdl_personrank", rankObj);
                //标准岗位
                DynamicObject hbjmJobhrObj = BusinessDataServiceHelper.newDynamicObject("hbjm_jobhr");
                hbjmJobhrObj.set("id", row.getLong("dgdl_stdposition"));
                addNew.set("dgdl_stdposition", hbjmJobhrObj);
                //岗位通道
                DynamicObject aisleObj = BusinessDataServiceHelper.newDynamicObject("dgdl_post_aisle");
                aisleObj.set("id", row.getLong("dgdl_post_aisle"));
                addNew.set("dgdl_post_aisle", aisleObj);
                //岗位属性
                DynamicObject jobpropertyObj = BusinessDataServiceHelper.newDynamicObject("dgdl_hbss_jobproperty");
                jobpropertyObj.set("id", row.getLong("dgdl_jobproperty"));
                addNew.set("dgdl_jobproperty", jobpropertyObj);
                //岗位标签
                if (fieldNames.contains("dgdl_label1")) {
                    //大雁
                    addNew.set("dgdl_label1", row.getString("dgdl_label1"));
                }
                if (fieldNames.contains("dgdl_label2")) {
                    //新四化
                    addNew.set("dgdl_label2", row.getString("dgdl_label2"));
                }
                if (fieldNames.contains("dgdl_label3")) {
                    //国际化
                    addNew.set("dgdl_label3", row.getString("dgdl_label3"));
                }
                if (fieldNames.contains("dgdl_label4")) {
                    //数字化
                    addNew.set("dgdl_label4", row.getString("dgdl_label4"));
                }
                if (fieldNames.contains("dgdl_dispatch")) {
                    //对外派驻
                    addNew.set("dgdl_dispatch", row.getString("dgdl_dispatch"));
                }
                if (fieldNames.contains("subsilocation")) {
                    //工作地点
                    DynamicObject workplaceObj = BusinessDataServiceHelper.newDynamicObject("dgdl_subsidy_location");
                    workplaceObj.set("id", row.getLong("subsilocation"));
                    addNew.set("dgdl_workplace", workplaceObj);
                }
                //实际人数
                addNew.set("dgdl_actual", row.getInteger("count"));
                //月编制人数默认赋值
                setAvg(addNew, (Integer) row.get("count"));
                //计划年编制
                addNew.set("dgdl_plan", row.getInteger("count"));
                //人员明细
                DynamicObjectCollection personDetail = addNew.getDynamicObjectCollection("dgdl_mulperson");
                personDetail.clear();
                personMsg.keySet().forEach(
                        key -> {
                            if (groupId.toString().equals(personMsg.get(key))) {
                                DynamicObject newCurrency = new DynamicObject(personDetail.getDynamicObjectType());
                                newCurrency.set("fbasedataid", key);
                                personDetail.add(newCurrency);
                            }
                        }
                );
                addNew.set("dgdl_mulperson", personDetail);
                //分组唯一ID
                addNew.set("dgdl_groupid", groupId);
                //人员同步时间
                detailData.set("dgdl_synctime", new Date());
            }
        }
        //执行保存操作
        OperateOption option = OperateOption.create();
        OperationResult endResult = OperationServiceHelper.executeOperate("save", "dgdl_planyear_detail", detailObj, option);
        logger.info("YearReportExportTask同步在岗人员信息=" + endResult);

        feedbackProgress(60, "开始执行任务", null);
        if (detailObj.length > 0) {
            feedbackProgress(100, "开始执行任务", null);
        }

    }


    /**
     * 获取当前组织以及下级组织
     *
     * @param orgId
     * @return
     */
    private List<Long> getAllOrg(Long orgId) {
        List<Long> orgIdList = new ArrayList<>();
        orgIdList.add(orgId);
        List<Map<String, Object>> allSubOrg = hAOSBatchAdminOrgInfoQueryService.batchGetAllSubOrg(orgIdList, new Date());
        return allSubOrg.stream().map(t -> (Long) t.get("orgId")).collect(Collectors.toList());
    }


    /**
     * * 过滤所需要的数据
     *
     * @param empData         任职经历结果集
     * @param planYear        编制计划
     * @param syndate         同步时间
     * @param lastOrg         末级组织
     * @param personMsg       统计人员信息
     * @param groupList       过滤字段
     * @param personData      个人职级结果集
     * @param locationDataSet 补贴地点结果集
     * @param joblabelDataSet 岗位结果集
     * @return
     */
    private DataSet getDataSet(DataSet empData, DynamicObject planYear, Date syndate, DynamicObject lastOrg, Map<Long, String> personMsg, List<String> groupList, DataSet personData, DataSet locationDataSet, DataSet joblabelDataSet) {
        //最终查询字段
        List<String> fieldList = new ArrayList<>();
        //标签维度
        String labeldimension = planYear.getString("dgdl_labeldimension");
        //任职经历关联岗位标签
        empData = empData.leftJoin(joblabelDataSet).on("jobLabelId", "id").select("empId", "personId", "dgdl_stdposition", "dgdl_post_aisle", "dgdl_jobproperty", "dgdl_dispatch", "dgdl_workplace", "number jobLableNumber").finish();
        //动态添加标签是否有值字段
        String[] labelArr = labeldimension.split(",");
        for (String s : labelArr) {
            if (Arrays.asList(JOBLABEL_ARR).contains(s)) {
                String labelNumber = "0" + s;
                empData = empData.addField("case when jobLableNumber='" + labelNumber + "' then 1 else 0 end", "dgdl_label" + s);
                groupList.add("dgdl_label" + s);
                fieldList.add("dgdl_label" + s);
            }
        }
        //任职经历分组过滤条件
        GroupbyDataSet groupbyDataSet = empData.groupBy(new String[]{"empId", "personId", "dgdl_stdposition", "dgdl_post_aisle", "dgdl_jobproperty", "dgdl_dispatch"});
        for (String s : labelArr) {
            if (Arrays.asList(JOBLABEL_ARR).contains(s)) {
                groupbyDataSet = groupbyDataSet.max("dgdl_label" + s);
            }
        }
        empData = groupbyDataSet.finish();
        //添加查询字段
        fieldList.add("empId");
        fieldList.add("personId");
        fieldList.add("dgdl_stdposition");
        fieldList.add("dgdl_post_aisle");
        fieldList.add("dgdl_jobproperty");
        fieldList.add("dgdl_dispatch");
        if (StringUtils.isNotEmpty(labeldimension)) {
            //标签选择是否外派，分组条件增加是否外派
            if (labeldimension.contains("5")) {
                groupList.add("dgdl_dispatch");
            }
            //标签选择工作地点，分组条件增加工作地点
            if (labeldimension.contains("6")) {
                fieldList.add("subsilocation");
                groupList.add("subsilocation");
                //任职经历关联岗位标签
                empData = empData.leftJoin(locationDataSet).on("personId", "personId").select(fieldList.toArray(new String[0])).finish();
            }
        }
        fieldList.add("dgdl_personrank");
        //关联个人职级
        empData = empData.leftJoin(personData).on("personId", "person").select(fieldList.toArray(new String[0])).finish();
        //用工关系类型
        String useworktype = planYear.getString("dgdl_useworktype");
        if (StringUtils.isNotEmpty(useworktype)) {
            String[] workType = useworktype.split(",");
            QFilter empentrelQfiler = new QFilter("iscurrentversion", QCP.equals, "1")
                    .and("enddate", QCP.large_equals, syndate).and("startdate", QCP.less_equals, syndate)
                    .and("laborreltype.number", QCP.in, workType);
            logger.info("PlanPersonFormPlugin职业信息filter=" + empentrelQfiler);
            //获取职业信息
            DataSet empentrelData = QueryServiceHelper.queryDataSet(ALGO, EMPENTREL, "id,person.id as person,laborreltype.id as dgdl_laborreltype", empentrelQfiler.toArray(), "");
            //添加查询字段
            fieldList.add("dgdl_laborreltype");
            empData = empData.join(empentrelData).on("personId", "person").select(fieldList.toArray(new String[0])).finish();
            //用工类型分组
            groupList.add("dgdl_laborreltype");
        }
        //分组字段标准岗位
        groupList.add("dgdl_stdposition");
        //分组字段岗位通道
        groupList.add("dgdl_post_aisle");
        //分组字段岗位属性
        groupList.add("dgdl_jobproperty");
        //分组字段职级属性
        groupList.add("dgdl_personrank");
        DataSet copyEmpData = empData.copy();
        //存储人员信息
        while (copyEmpData.hasNext()) {
            Row row = copyEmpData.next();
            //分组唯一值
            StringBuilder groupId = new StringBuilder();
            for (String s : groupList) {
                groupId.append(row.getString(s)).append("-");
            }
            //拼接组织,避免组织不同其他信息相同的情况
            groupId.append(lastOrg.getPkValue());
            Long personId = row.getLong("personId");
            personMsg.put(personId, String.valueOf(groupId));
        }
        //分组后结果集
        return empData.groupBy(groupList.toArray(new String[0])).count().finish();
    }


    /**
     * @param addNew 实体
     * @param count  人数
     */
    private void setAvg(DynamicObject addNew, int count) {
        //实际-去年
        addNew.set("dgdl_preavg10", count);
        addNew.set("dgdl_preavg11", count);
        addNew.set("dgdl_preavg12", count);
        //实际-今年
        String str = "dgdl_avg";
        addNew.set("dgdl_avg1", count);
        addNew.set("dgdl_avg2", count);
        addNew.set("dgdl_avg3", count);
        addNew.set("dgdl_avg4", count);
        addNew.set("dgdl_avg5", count);
        addNew.set("dgdl_avg6", count);
        addNew.set("dgdl_avg7", count);
        addNew.set("dgdl_avg8", count);
        addNew.set("dgdl_avg9", count);
        addNew.set("dgdl_avg10", count);
        addNew.set("dgdl_avg11", count);
        addNew.set("dgdl_avg12", count);
        addNew.set("dgdl_average", count);
    }
}
