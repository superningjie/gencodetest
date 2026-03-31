package dgdl.odc.homs.opplugin;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import dgdl.odc.homs.common.DateTimeCommon;
import kd.bos.algo.DataSet;
import kd.bos.algo.GroupbyDataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.form.ClientProperties;
import kd.bos.form.control.EntryGrid;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.hr.haos.mservice.HAOSBatchAdminOrgInfoQueryService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author: yaoshuai
 * @CreateTime: 2024-01-29  15:02
 * @Description: 同步在岗人员
 */
public class SycnOnPersonOp extends AbstractOperationServicePlugIn {

    private static Log logger = LogFactory.getLog(SycnOnPersonOp.class);

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
            "job.dgdl_post_aisle.id as dgdl_post_aisle," +
            "job.dgdl_jobproperty.id as dgdl_jobproperty," +
            "job.dgdl_joblabel.fbasedataid as jobLabelId," +
            "position.dgdl_external_dispatch as dgdl_dispatch," +
            "position.workplace.id as dgdl_workplace";

    /**
     * 工作地点查询字段
     */
    private final static String LOCATION_FILED = "dgdl_employee.person.id as personId,dgdl_coresubsidyentity.dgdl_subsilocation.id as subsilocation";

    /**
     * 编制详情
     */
    private static final String PLAN_DETAIL = "dgdl_planyear_detail";

    /**
     * 查询下级组织微服务接口类
     */
    private static HAOSBatchAdminOrgInfoQueryService hAOSBatchAdminOrgInfoQueryService = new HAOSBatchAdminOrgInfoQueryService();

    /**
     * 查询字段
     */
    private static final String FILED = "id,billstatus,dgdl_synctime," +
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
            "dgdl_entry.dgdl_add_str," +
            "dgdl_entry.dgdl_reduce_str," +
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


    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("dgdl_planyear");
        e.getFieldKeys().add("adminorg");
        e.getFieldKeys().add("dgdl_adminorg1");
        e.getFieldKeys().add("dgdl_adminorg2");
        e.getFieldKeys().add("dgdl_adminorg3");
        e.getFieldKeys().add("dgdl_adminorg4");
        e.getFieldKeys().add("dgdl_adminorg5");
        e.getFieldKeys().add("dgdl_adminorg6");
        e.getFieldKeys().add("dgdl_detail_adminorg1");
        e.getFieldKeys().add("dgdl_detail_adminorg2");
        e.getFieldKeys().add("dgdl_detail_adminorg3");
        e.getFieldKeys().add("dgdl_detail_adminorg4");
        e.getFieldKeys().add("dgdl_detail_adminorg5");
        e.getFieldKeys().add("dgdl_detail_adminorg6");
        e.getFieldKeys().add("dgdl_detail_adminorg");
        e.getFieldKeys().add("dgdl_stdposition");
        e.getFieldKeys().add("dgdl_personrank");
        e.getFieldKeys().add("dgdl_laborreltype");
        e.getFieldKeys().add("dgdl_post_aisle");
        e.getFieldKeys().add("dgdl_jobproperty");
        e.getFieldKeys().add("dgdl_label1");
        e.getFieldKeys().add("dgdl_label2");
        e.getFieldKeys().add("dgdl_label3");
        e.getFieldKeys().add("dgdl_label4");
        e.getFieldKeys().add("dgdl_dispatch");
        e.getFieldKeys().add("dgdl_workplace");
        e.getFieldKeys().add("dgdl_actual");
        e.getFieldKeys().add("dgdl_groupid");
        e.getFieldKeys().add("dgdl_mulperson");
        e.getFieldKeys().add("dgdl_synctime");
        e.getFieldKeys().add("dgdl_add_str");
        e.getFieldKeys().add("dgdl_reduce_str");

    }

    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        super.beginOperationTransaction(e);
        DynamicObject[] dataEntities = e.getDataEntities();
        String algoKey = this.getClass().getName();
        //同步日期
        String dgdlDate = this.getOption().getVariableValue("dgdl_date");
        Date syndate = DateTimeCommon.ConvertToDate(dgdlDate);
        //查询岗位标签
        QFilter joblabelFilter = new QFilter("enable", QCP.equals, "1");
        DataSet joblabelDataSet = QueryServiceHelper.queryDataSet(algoKey, "dgdl_hbss_joblabel", "id,number", joblabelFilter.toArray(), "");
        for (DynamicObject dataEntity : dataEntities) {
            //年度管理计划
            DynamicObject planYear = dataEntity.getDynamicObject("dgdl_planyear");

            List<String> jumpFieldList = setJUMPFieldList(planYear);
            //获取组织层级
            DynamicObject dgdlAdminorg1 = dataEntity.getDynamicObject("dgdl_adminorg1");
            DynamicObject dgdlAdminorg2 = dataEntity.getDynamicObject("dgdl_adminorg2");
            DynamicObject dgdlAdminorg3 = dataEntity.getDynamicObject("dgdl_adminorg3");
            DynamicObject dgdlAdminorg4 = dataEntity.getDynamicObject("dgdl_adminorg4");
            DynamicObject dgdlAdminorg5 = dataEntity.getDynamicObject("dgdl_adminorg5");
            DynamicObject dgdlAdminorg6 = dataEntity.getDynamicObject("dgdl_adminorg6");
            //开始时间
            Date dgdlStartmonth = planYear.getDate("dgdl_startmonth");
            int startMonth = Integer.parseInt(new SimpleDateFormat("MM").format(dgdlStartmonth));
            //分组字段
            List<String> groupList = new ArrayList<>();
            //末端组织
            DynamicObject lastOrg = dataEntity.getDynamicObject("adminorg");
            //判断当时是否是最末级组织
            boolean isLast = this.isLastOrg(dataEntity);
            List<Long> adminList = new ArrayList<>();
            if (isLast) {
                List<Long> allList = this.getAllOrg(lastOrg.getLong("id"));
                adminList.addAll(allList);
            } else {
                adminList.add(lastOrg.getLong("id"));
            }
            //获取任职经历查询条件
            QFilter empFiler = new QFilter("iscurrentversion", QCP.equals, "1")
                    .and("datastatus", QCP.equals, "1")
                    .and("isprimary", QCP.equals, "1")
                    .and("businessstatus", QCP.equals, "1")
                    .and("adminorg.id", QCP.in, adminList)
                    .and("enddate", QCP.large_equals, syndate).and("startdate", QCP.less_equals, syndate);

            logger.info("同步在岗人员获取任职经历信息条件：" + empFiler);
            //获取任职经历信息
            DataSet empData = QueryServiceHelper.queryDataSet(algoKey, HRPI_EMPPOSORGREL, EMP_FILED, empFiler.toArray(), "");
            //任职经历关联岗位标签
            empData = empData.leftJoin(joblabelDataSet).on("jobLabelId", "id").select("empId", "personId", "dgdl_stdposition", "dgdl_post_aisle", "dgdl_jobproperty", "dgdl_dispatch", "dgdl_workplace", "number jobLableNumber").finish();
            logger.info("PlanPersonFormPlugin任职经历获取长度：" + empData.copy().count("empId", false));
            //标签维度
            String labeldimension = planYear.getString("dgdl_labeldimension");
            //动态添加标签是否有值字段
            String[] labelArr = labeldimension.split(",");
            //最终查询字段
            List<String> fieldList = new ArrayList<>();

            for (String s : labelArr) {
                if (Arrays.asList(JOBLABEL_ARR).contains(s)) {
                    String labelNumber = "0" + s;
                    empData = empData.addField("case when jobLableNumber='" + labelNumber + "' then 1 else 0 end", "dgdl_label" + s);
                    groupList.add("dgdl_label" + s);
                    fieldList.add("dgdl_label" + s);
                }
            }

            //任职经历分组过滤条件
            GroupbyDataSet groupbyDataSet = empData.groupBy(new String[]{"empId", "personId", "dgdl_stdposition", "dgdl_post_aisle", "dgdl_jobproperty", "dgdl_dispatch", "dgdl_workplace"});
            for (String s : labelArr) {
                if (Arrays.asList(JOBLABEL_ARR).contains(s)) {
                    groupbyDataSet = groupbyDataSet.max("dgdl_label" + s);
                }
            }
            empData = groupbyDataSet.finish();

            //个人职级
            QFilter personQfiler = new QFilter("iscurrentversion", QCP.equals, "1")
                    .and("datastatus", QCP.equals, "1")
                    .and("businessstatus", QCP.equals, "1")
                    .and("enddate", QCP.large_equals, syndate).and("startdate", QCP.less_equals, syndate);
            logger.info("PlanPersonFormPlugin个人职级filter=" + personQfiler);
            DataSet personData = QueryServiceHelper.queryDataSet(algoKey, PERSON, "id,person.id as person,dgdl_personrank.id as dgdl_personrank", personQfiler.toArray(), "");
            //添加查询字段
            fieldList.add("empId");
            fieldList.add("personId");
            fieldList.add("dgdl_stdposition");
            fieldList.add("dgdl_post_aisle");
            fieldList.add("dgdl_jobproperty");
            fieldList.add("dgdl_dispatch");
            fieldList.add("dgdl_workplace");
            fieldList.add("dgdl_personrank");
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
                DataSet empentrelData = QueryServiceHelper.queryDataSet(algoKey, EMPENTREL, "id,person.id as person,laborreltype.id as dgdl_laborreltype", empentrelQfiler.toArray(), "");
                //添加查询字段
                fieldList.add("dgdl_laborreltype");
                empData = empData.join(empentrelData).on("personId", "person").select(fieldList.toArray(new String[0])).finish();
                //用工类型分组
                groupList.add("dgdl_laborreltype");
            }
            logger.info("PlanPersonFormPlugin分组之后的长度：" + empData.copy().count("personId", false));
            if (StringUtils.isNotEmpty(labeldimension)) {
                //标签选择是否外派，分组条件增加是否外派
                if (labeldimension.contains("5")) {
                    groupList.add("dgdl_dispatch");
                }
                //标签选择工作地点，分组条件增加工作地点
                if (labeldimension.contains("6")) {
                    groupList.add("dgdl_workplace");

                    //查询岗位标签
                    QFilter filter = new QFilter("iscurrentversion", QCP.equals, "1");
                    filter.and("dgdl_coresubsidyentity.dgdl_startdate", QCP.less_equals, syndate).and("dgdl_coresubsidyentity.dgdl_enddate", QCP.large_equals, syndate);
                    DataSet locationDataSet = QueryServiceHelper.queryDataSet(this.getClass().getName(), "dgdl_hiscoresubsidies", LOCATION_FILED, filter.toArray(), null);
                    fieldList.add("subsilocation");
                    groupList.add("subsilocation");
                    //任职经历关联岗位标签
                    empData = empData.leftJoin(locationDataSet).on("personId", "personId").select(fieldList.toArray(new String[0])).finish();
                }
            }
            //分组字段标准岗位
            groupList.add("dgdl_stdposition");
            //分组字段岗位通道
            groupList.add("dgdl_post_aisle");
            //分组字段岗位属性
            groupList.add("dgdl_jobproperty");
            //分组字段职级属性
            groupList.add("dgdl_personrank");

            //计划类型
            String planType = planYear.getString("dgdl_plantype");

            //获取自身数据
            QFilter detailQFilter = new QFilter("dgdl_planyear.number", QCP.equals, planYear.getString("number"))
                    .and("adminorg.number", QCP.equals, lastOrg.getString("number"));
            logger.info("SycnOnPersonOp获取自身详情信息SQL=" + detailQFilter);
            DynamicObject thisDetail = BusinessDataServiceHelper.loadSingle(PLAN_DETAIL, FILED, detailQFilter.toArray());
            if (Objects.nonNull(thisDetail)) {
                //封装分录数据用于比对更新
                DynamicObjectCollection dgdlEntryCol = thisDetail.getDynamicObjectCollection("dgdl_entry");
                Map<String, DynamicObject> entryMap = new HashMap<>();
                for (DynamicObject entryObj : dgdlEntryCol) {
                    String dgdlGroupid = entryObj.getString("dgdl_groupid");
                    entryMap.put(dgdlGroupid, entryObj);
                }
                //先清空在赋值,删除当前组织的数据
                Iterator<DynamicObject> iterator = dgdlEntryCol.iterator();
                while (iterator.hasNext()) {
                    DynamicObject next = iterator.next();
                    String entryLastName = next.getDynamicObject("dgdl_detail_adminorg").getString("number");
                    if (entryLastName.equals(lastOrg.getString("number"))) {
                        iterator.remove();
                    }
                }
                //存储人员信息
                Map<Long, String> personMsg = new HashMap<>();
                DataSet copyEmpData = empData.copy();
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
                logger.info("SycnOnPersonOpMap存储的信息=" + personMsg);
                //分组后结果集
                DataSet groupDataSet = empData.groupBy(groupList.toArray(new String[0])).count().finish();
                List<String> fieldNames = Arrays.asList(groupDataSet.getRowMeta().getFieldNames());
                logger.info("SycnOnPersonOp即将用来分组的信息=" + groupList);
                //分录赋值
                int index = 0;
                while (groupDataSet.hasNext()) {
                    Row row = groupDataSet.next();
                    //分组唯一值
                    StringBuilder groupId = new StringBuilder();
                    for (String s : groupList) {
                        groupId.append(row.getString(s)).append("-");
                    }
                    groupId.append(lastOrg.getPkValue());
                    logger.info("SycnOnPersonOp即将存储的groupId=" + groupId);
                    DynamicObject addNew = new DynamicObject(dgdlEntryCol.getDynamicObjectType());
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
                    if (Objects.nonNull(dgdlPersonrank) && dgdlPersonrank != 0) {
                        DynamicObject jobLevelObj = BusinessDataServiceHelper.loadSingle(dgdlPersonrank, "hbjm_joblevelhr");
                        addNew.set("dgdl_personrank", jobLevelObj);
                    }

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
                        logger.info("SycnOnPersonOp:dgdl_dispatch=" + row.getString("dgdl_dispatch"));
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
                    addNew.set("dgdl_actual", row.get("count"));
                    //计划年编制
                    addNew.set("dgdl_plan", row.get("count"));
                    //人员明细
                    DynamicObjectCollection personDetail = addNew.getDynamicObjectCollection("dgdl_mulperson");
                    logger.info("SycnOnPersonOp清除人员明细");
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
                    //原始数据(用于更新编制人数)
                    DynamicObject oldData = entryMap.get(groupId.toString());
                    if (Objects.nonNull(oldData)) {
                        oldData.get("dgdl_add");
                        //计划新增
                        addNew.set("dgdl_add", oldData.get("dgdl_add"));
                        //计划减少
                        addNew.set("dgdl_reduce", oldData.get("dgdl_reduce"));
                        //计划年编制
                        addNew.set("dgdl_plan", oldData.get("dgdl_plan"));
                        //控编人数
                        addNew.set("dgdl_control", oldData.get("dgdl_control"));
                        //人数计算
                        setCount(addNew, oldData,jumpFieldList);
                    } else {
                        //月编制人数默认赋值
                        setAvg(addNew, (Integer) row.get("count"),jumpFieldList);
                    }
                    dgdlEntryCol.add(index, addNew);
                }
                for (int i = 0; i < dgdlEntryCol.size(); i++) {
                    DynamicObject addNew = dgdlEntryCol.get(i);
                    addNew.set("seq", i);
                }
                if (!personMsg.isEmpty()) {
                    //人员同步时间
                    thisDetail.set("dgdl_synctime", new Date());
                    //执行保存操作
                    OperateOption option = OperateOption.create();
                    DynamicObject[] planObjs = new DynamicObject[]{thisDetail};
                    OperationResult endResult = OperationServiceHelper.executeOperate("save", "dgdl_planyear_detail", planObjs, option);
                    logger.info("SycnOnPersonOp同步在岗人员信息=" + endResult);
                }
            }
        }
    }

    private   List<String>  setJUMPFieldList(DynamicObject dgdlPlanObj) {
        List<String> fieldlist=new ArrayList<>();

        //隐藏明细页签分录月份字段
        if (dgdlPlanObj == null) {
            return fieldlist;
        }
        //所属年度

        Date dgdlYear = dgdlPlanObj.getDate("dgdl_year");
        int year = DateTimeCommon.getYear(dgdlYear);
        //分录字段重命名
        //开始月份
        Date dgdlStartmonth = dgdlPlanObj.getDate("dgdl_startmonth");
        LocalDate date1 = dgdlStartmonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        //结束月份
        Date dgdlEndmonth = dgdlPlanObj.getDate("dgdl_endmonth");
        LocalDate date2 = dgdlEndmonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long monthCount = DateTimeCommon.reduceMon(dgdlStartmonth, dgdlEndmonth);

        // 罗列年份和月份
        List<YearMonth> yearMonths = new ArrayList<>();
        YearMonth startYearMonth = YearMonth.of(date1.getYear(), date1.getMonth());
        YearMonth endYearMonth = YearMonth.of(date2.getYear(), date2.getMonth());

        YearMonth current = startYearMonth;
        while (!current.isAfter(endYearMonth)) {
            yearMonths.add(current);
            current = current.plusMonths(1);
        }

        // 打印结果
        for (YearMonth ym : yearMonths) {
            int monthValue = ym.getMonthValue();
            int ymyear = ym.getYear();
            if(ymyear==year){
                String fieldKey1 = "dgdl_chg" + monthValue;
                String fieldKey2 = "dgdl_avg" + monthValue;
                fieldlist.add(fieldKey1);
                fieldlist.add(fieldKey2);
            }else {
                String fieldKey1 = "dgdl_prechg" + monthValue;
                String fieldKey2 = "dgdl_preavg" + monthValue;
                fieldlist.add(fieldKey1);
                fieldlist.add(fieldKey2);
            }
        }



        return fieldlist;
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
     * 判断当前是否是最末级组织
     *
     * @param dataEntity
     * @return
     */
    private boolean isLastOrg(DynamicObject dataEntity) {
        //编制计划
        DynamicObject planyear = dataEntity.getDynamicObject("dgdl_planyear");
        //末端组织
        DynamicObject adminorg = dataEntity.getDynamicObject("adminorg");
        String lastName = adminorg.getString("number");
        logger.info("DeletePlanDetailValidator末端组织编码=" + lastName);
        //组织层级
        String hierarchy = planyear.getString("dgdl_orghierarchy");
        int intHierarchy = Integer.parseInt(hierarchy);
        logger.info("DeletePlanDetailValidator编制计划层级=" + intHierarchy);
        String adminStr = "dgdl_adminorg";
        List<String> adminList = new ArrayList<>();
        for (int i = 1; i <= intHierarchy; i++) {
            DynamicObject admin = dataEntity.getDynamicObject(adminStr + i);
            if (Objects.nonNull(admin)) {
                adminList.add(adminStr + i);
                if (admin.getString("number").equals(lastName)) {
                    break;
                }
            }
        }
        logger.info("DeletePlanDetailValidator要匹配的集合=" + adminList);
        if(adminList.size()==0){
            return false;
        }
        //上级行政组织
        String selectName = adminList.get(adminList.size() - 1);
        logger.info("DeletePlanDetailValidator要匹配的行政组织=" + selectName);

        QFilter qFilter = new QFilter("dgdl_planyear.id", QCP.equals, planyear.getPkValue())
                .and(selectName + ".number", QCP.equals, lastName);
        logger.info("DeletePlanDetailValidator查询语句=" + qFilter);
        DynamicObject[] load = BusinessDataServiceHelper.load("dgdl_planyear_detail", "id", qFilter.toArray());
        logger.info("DeletePlanDetailValidator查询长度=" + load.length);
        if (load.length > 1) {
            return false;
        } else {
            return true;
        }
    }


    private void setCount(DynamicObject addNew, DynamicObject thisObj, List<String> jumpFieldList) {
        logger.info("setCount");
        //        //计划-去年
//        addNew.set("dgdl_prechg10", thisObj.get("dgdl_prechg10"));
//        addNew.set("dgdl_prechg11", thisObj.get("dgdl_prechg11"));
//        addNew.set("dgdl_prechg12", thisObj.get("dgdl_prechg12"));
//        //计划-今年
//        addNew.set("dgdl_chg1", thisObj.get("dgdl_chg1"));
//        addNew.set("dgdl_chg2", thisObj.get("dgdl_chg2"));
//        addNew.set("dgdl_chg3", thisObj.get("dgdl_chg3"));
//        addNew.set("dgdl_chg4", thisObj.get("dgdl_chg4"));
//        addNew.set("dgdl_chg5", thisObj.get("dgdl_chg5"));
//        addNew.set("dgdl_chg6", thisObj.get("dgdl_chg6"));
//        addNew.set("dgdl_chg7", thisObj.get("dgdl_chg7"));
//        addNew.set("dgdl_chg8", thisObj.get("dgdl_chg8"));
//        addNew.set("dgdl_chg9", thisObj.get("dgdl_chg9"));
//        addNew.set("dgdl_chg10", thisObj.get("dgdl_chg10"));
//        addNew.set("dgdl_chg11", thisObj.get("dgdl_chg11"));
//        addNew.set("dgdl_chg12", thisObj.get("dgdl_chg11"));
//        //实际-去年
//        addNew.set("dgdl_preavg10", thisObj.get("dgdl_preavg10"));
//        addNew.set("dgdl_preavg11", thisObj.get("dgdl_preavg11"));
//        addNew.set("dgdl_preavg12", thisObj.get("dgdl_preavg12"));
//        //实际-今年
//        addNew.set("dgdl_avg1", thisObj.get("dgdl_avg1"));
//        addNew.set("dgdl_avg2", thisObj.get("dgdl_avg2"));
//        addNew.set("dgdl_avg3", thisObj.get("dgdl_avg3"));
//        addNew.set("dgdl_avg4", thisObj.get("dgdl_avg4"));
//        addNew.set("dgdl_avg5", thisObj.get("dgdl_avg5"));
//        addNew.set("dgdl_avg6", thisObj.get("dgdl_avg6"));
//        addNew.set("dgdl_avg7", thisObj.get("dgdl_avg7"));
//        addNew.set("dgdl_avg8", thisObj.get("dgdl_avg8"));
//        addNew.set("dgdl_avg9", thisObj.get("dgdl_avg9"));
//        addNew.set("dgdl_avg10", thisObj.get("dgdl_avg10"));
//        addNew.set("dgdl_avg11", thisObj.get("dgdl_avg11"));
//        addNew.set("dgdl_avg12", thisObj.get("dgdl_avg12"));
        //平均
        BigDecimal avg=BigDecimal.ZERO;
        int i=0;
        for (String field : jumpFieldList) {
            addNew.set(field, thisObj.get(field));
            if(field.contains("avg")){
                avg= avg.add(thisObj.getBigDecimal(field)==null? BigDecimal.ZERO:thisObj.getBigDecimal(field));
                i++;
            }
        }
        avg=avg.divide(new BigDecimal(i));
        addNew.set("dgdl_average",avg);


    }

    private void setAvg(DynamicObject addNew, int count,List<String> jumpFieldList) {
        logger.info("setAvg");

        for (String field : jumpFieldList) {

            if (field.contains("avg")) {
                addNew.set(field, count);
            }
        }
        addNew.set("dgdl_average",count);
    }


}
