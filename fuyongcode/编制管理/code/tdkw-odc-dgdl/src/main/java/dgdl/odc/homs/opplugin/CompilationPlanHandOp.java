package dgdl.odc.homs.opplugin;

import com.alibaba.dubbo.common.utils.StringUtils;
import dgdl.odc.homs.common.DateTimeCommon;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.url.UrlService;
import kd.bos.workflow.engine.msg.info.MessageInfo;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: lzf
 * @CreateTime: 2023-09-06  15:02
 * @Description: 年度编制计划, 月度编制计划 启用时 或者催办时 发送消息
 */
public class CompilationPlanHandOp extends AbstractOperationServicePlugIn {
    private static Log logger = LogFactory.getLog(CompilationPlanHandOp.class);

    /**
     * 年度标识
     */
    private static final String YEAR_FROMBILL = "dgdl_planyear_bill";
    /**
     * 月度标识
     */
    private static final String MONTH_FROMBILL = "dgdl_planmonth_bill";

    /**
     * 任职经历获取字段
     */
    private final static String EMP_FILED = "id as empId,person.id as personId," +
            "adminorg.structnumber as structnumber," +
            "adminorg.structlongnumber as structlongnumber," +
            "job.dgdl_jobproperty.id as jobpropertyId," +
            "job.dgdl_joblabel.fbasedataid as jobLabelBasedataid";

    /**
     * 岗位标签数组
     */
    private final static String[] JOBLABEL_ARR = new String[]{"1", "2", "3", "4", "5", "6", "7"};

    /**
     * 岗位i属性
     */
    private final static String[] PROPERTY_ARR = new String[]{"job1", "job2"};


    /**
     * 用工关系类型
     * 1030_S   实习（在职）
     * 1040_S   劳务派遣（在职）
     * 1050_S   退休返聘（在职）
     * 1010_S   正式工（在职）
     * 1220_S   合作伙伴（在职）
     */
    private final static String[] FILE_TYPE = new String[]{"1030_S", "1040_S", "1050_S", "1010_S", "1220_S"};

    /**
     * 档案业务类型
     */
    private final static String[] ERM_TYPE = new String[]{"1010_S", "1070_S", "1110_S", "1150_S", "1190_S"};


    /**
     * 客户端地址
     */
    private String clientPath = "";//客户端地址


    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("name");
        e.getFieldKeys().add("dgdl_startmonth");
        e.getFieldKeys().add("dgdl_endmonth");
        e.getFieldKeys().add("dgdl_org");
        e.getFieldKeys().add("dgdl_year");
        e.getFieldKeys().add("dgdl_orghierarchy");
        e.getFieldKeys().add("dgdl_labeldimension");
        e.getFieldKeys().add("dgdl_useworktype");
        e.getFieldKeys().add("dgdl_controlediting");
        e.getFieldKeys().add("dgdl_way");
        e.getFieldKeys().add("dgdl_people");
        e.getFieldKeys().add("dgdl_isunify");
    }

    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        super.endOperationTransaction(e);
        logger.info("开始执行endOperationTransaction");
        String operationKey = e.getOperationKey();
        for (DynamicObject dataEntity : e.getDataEntities()) {
            String billKey = dataEntity.getDataEntityType().getName();//获取当前单据标识
            logger.info("CompilationPlanUrgeOp标识=" + billKey);
            //单据id
            Object pkValue = dataEntity.getPkValue();
            //获取编制规划单位
            DynamicObject dgdlOrg = dataEntity.getDynamicObject("dgdl_org");
            String sonNumber = dgdlOrg.getString("number");
            String sonName = dgdlOrg.getString("name");
            //开始时间
            Date yearStartMonth = dataEntity.getDate("dgdl_startmonth");
            Date monthStartDate = DateTimeCommon.getMonthStartDate(yearStartMonth);
            Date date = DateTimeCommon.addDay(monthStartDate, -1);
            if (date.compareTo(new Date()) > 0) {
                date = new Date();
            }

            DynamicObject planBillObj = null;
            logger.info("CompilationPlanUrgeOp单据id=" + pkValue);
            //创建编制信息:已经生成过的编制信息管理不能再次生成
            if ("dgdl_planyear".equals(billKey)) {
                QFilter planQFilter = new QFilter("dgdl_planyear.id", QCP.equals, pkValue);
                planBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planyear_bill", "id", planQFilter.toArray());
                if (planBillObj!=null) {
                    DeleteServiceHelper.delete("dgdl_planyear_bill",new QFilter[]{planQFilter});
                }
            } else if ("dgdl_planmonth".equals(billKey)) {
                QFilter planQFilter = new QFilter("dgdl_planmonth.id", QCP.equals, pkValue);
                planBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bill", "id", planQFilter.toArray());
                if (planBillObj!=null) {
                    DeleteServiceHelper.delete("dgdl_planmonth_bill",new QFilter[]{planQFilter});
                }
            }
            Long successId = null;

                DynamicObject planBill = null;
                if ("dgdl_planyear".equals(billKey)) {
                    planBill = BusinessDataServiceHelper.newDynamicObject("dgdl_planyear_bill");
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    String billno = format.format(new Date());
                    planBill.set("billno", billno);
                    planBill.set("dgdl_planyear", dataEntity);
                    //编制状态
                    planBill.set("billstatus", "A");
                    //审批状态
                    planBill.set("auditstatus", "A");
                } else if ("dgdl_planmonth".equals(billKey)) {
                    planBill = BusinessDataServiceHelper.newDynamicObject("dgdl_planmonth_bill");
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    String billno = format.format(new Date());
                    planBill.set("billno", billno);
                    planBill.set("createtime", new Date());
                    planBill.set("dgdl_planmonth", dataEntity);
                    //编制状态
                    planBill.set("billstatus", "A");
                    //审批状态
                    planBill.set("auditstatus", "A");
                    //默认设置未同步
                    planBill.set("dgdl_is_syn", "02");
                }
                OperateOption option = OperateOption.create();
                OperationResult endResult = null;
                //获取层级,生成组织信息
                String hierarchy = dataEntity.getString("dgdl_orghierarchy");
                //获取组织管理体系的所有行政组织
                Map<String, DynamicObject> hierarchyMap = getAdminByOrg(sonNumber, hierarchy);
                Set<String> orgStructNumbers = hierarchyMap.keySet();
//                Map<String, DynamicObject> lastOrgMap = getlastOrg(sonNumber, hierarchy);
//
//                Set<String> orgStructlongNumbers = lastOrgMap.keySet();
                List<Long> orgList = getOrgList(sonNumber);
                //转换层级
                int intHierarchy = Integer.parseInt(hierarchy);
                //存储非当前bu组织
                Map<String, DynamicObject> notFindOrgMap = new HashMap<>();
                logger.info("CompilationPlanUrgeOpMap存储的数据长度=" + orgList.size());
                if ("dgdl_planyear".equals(billKey)) {
                    //筛选可以生生成数据的组织
                    //筛选任职经历//结束时>=20231231 and 开始时间<=20231231
                    QFilter empFiler = new QFilter("iscurrentversion", QCP.equals, "1").and("datastatus", QCP.equals, "1")
                            .and("isprimary", QCP.equals, "1")
                            .and("adminorg.id", QCP.in, orgList)
                            .and("enddate", QCP.large_equals, date).and("startdate", QCP.less_equals, date);
                    DynamicObject[] empObjs = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,adminorg.structlongnumber", empFiler.toArray());
                    //有数据的组织长编码keySet
                    Map<String, Long> structLongMap = new HashMap<>();
                    for (DynamicObject empObj : empObjs) {
                        structLongMap.put(empObj.getString("adminorg.structlongnumber"), (Long) empObj.getPkValue());
                    }
                    Set<String> keySet = structLongMap.keySet();
                    //最终需要生成数据的组织
                    Set<String> dataSet = new HashSet<>();
                    for (String structLongNumber : keySet) {
                        String[] splitArray = structLongNumber.split("!");
                        dataSet.addAll(Arrays.asList(splitArray));
                    }
                    DynamicObject[] planObjs = new DynamicObject[]{planBill};
                    endResult = OperationServiceHelper.executeOperate("save", "dgdl_planyear_bill", planObjs, option);
                    successId = (Long) endResult.getSuccessPkIds().get(0);
                    //生成年度编制详情
                    List<DynamicObject> saveObj = new ArrayList<>();
                    for (String structNumber : dataSet) {
                        //解析行政组织长编码
                        DynamicObject lastOrg = hierarchyMap.get(structNumber);
                        if (Objects.nonNull(lastOrg)) {
                            DynamicObject detailData = BusinessDataServiceHelper.newDynamicObject("dgdl_planyear_detail");
                            //末端组织
                            detailData.set("adminorg", lastOrg);
                            //通过长编码获取各个父级行政组织
                            String stringLongNumber = lastOrg.getString("structlongnumber");
                            String[] splitArray = stringLongNumber.split("!");
                            //组织长编码长度
                            int splitLength = splitArray.length;
                            //是否末级组织
                            if (splitLength > intHierarchy) {
                                //当前组织层级
                                String thisOrgLayer = lastOrg.getString("adminorglayer.number");
                                int layer = Integer.parseInt(thisOrgLayer);
                                logger.info("CompilationPlanUrgeOp公司编码={},年度当前组织层级={}", lastOrg.getString("number"), layer);
                                if ((layer - 1) == intHierarchy) {
                                    detailData.set("dgdl_islastorg", false);
                                } else {
                                    detailData.set("dgdl_islastorg", true);
                                }
                            } else {
                                detailData.set("dgdl_islastorg", true);
                            }
                            for (String longNumber : splitArray) {
                                DynamicObject obj = hierarchyMap.get(longNumber);
                                if (Objects.isNull(obj)) {
                                    obj = notFindOrgMap.get(longNumber);
                                    if (Objects.isNull(obj)) {
                                        //获取当前子集团下所选层级的行政组织
                                        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                                                .and("structnumber", QCP.equals, longNumber)
                                                .and("iscurrentversion", QCP.equals, "1");
                                        obj = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "id,number,adminorglayer.number", orgQFilter.toArray());
                                        //存储非当前bu组织
                                        notFindOrgMap.put(longNumber, obj);
                                    }
                                    //非当前bu组织
                                    detailData.set("dgdl_is_org", "02");
                                }
                                if (Objects.nonNull(obj)) {
                                    String adminorglayer = obj.getString("adminorglayer.number");
                                    if ("02".equals(adminorglayer)) {
                                        detailData.set("dgdl_adminorg1", obj);
                                    } else if ("03".equals(adminorglayer)) {
                                        detailData.set("dgdl_adminorg2", obj);
                                    } else if ("04".equals(adminorglayer)) {
                                        detailData.set("dgdl_adminorg3", obj);
                                    } else if ("05".equals(adminorglayer)) {
                                        detailData.set("dgdl_adminorg4", obj);
                                    } else if ("06".equals(adminorglayer)) {
                                        detailData.set("dgdl_adminorg5", obj);
                                    } else if ("07".equals(adminorglayer)) {
                                        detailData.set("dgdl_adminorg6", obj);
                                    }
                                }
                            }
                            //人员同步时间
                            detailData.set("dgdl_synctime", new Date());
                            //编制状态
                            detailData.set("billstatus", "A");
                            //审批状态
                            detailData.set("auditstatus", "A");
                            //年度编制计划
                            detailData.set("dgdl_planyear", dataEntity);
                            //编制单位
                            detailData.set("dgdl_orgfield", dgdlOrg);
                            saveObj.add(detailData);
                        } else {
                            logger.info("CompilationPlanUrgeOp没有获取到的组织编码=" + structNumber);
                        }
                    }
                    DynamicObject[] objArray = new DynamicObject[saveObj.size()];
                    for (int i = 0; i < saveObj.size(); i++) {
                        objArray[i] = saveObj.get(i);
                    }
                    //调用保存操作
                    OperateOption detailOption = OperateOption.create();
                    OperationResult detailResult = OperationServiceHelper.executeOperate("save", "dgdl_planyear_detail", objArray, detailOption);
                    logger.info("CompilationPlanUrgeOp编制详情校验器操作结果=" + detailResult);
                } else if ("dgdl_planmonth".equals(billKey)) {
                    DynamicObjectCollection bzCollection = planBill.getDynamicObjectCollection("dgdl_bz_entry");//编制分录
                    //分组字段
                    List<String> groupList = new ArrayList<>();
                    groupList.add("structnumber");
                    //码值存储
                    //岗位属性
                    Map<String, String> propertyMap = new HashMap<>();
                    propertyMap.put("0", "一线");
                    propertyMap.put("1", "非一线");
                    //用工关系类型
                    Map<String, String> typeMap = new HashMap<>();
                    typeMap.put("helpmate", "合作伙伴");
                    typeMap.put("interns_reserve_personnel", "实习生");
                    typeMap.put("labor_dispatch", "劳务派遣");
                    typeMap.put("regular_workers", "正式工");
                    typeMap.put("rehired_after_retirement", "退休返聘");
                    typeMap.put("dayan", "大雁");
                    //用工关系类型
                    Map<String, String> typeNumberMap = new HashMap<>();
                    typeNumberMap.put("1220_S", "helpmate");
                    typeNumberMap.put("1030_S", "interns_reserve_personnel");
                    typeNumberMap.put("1040_S", "labor_dispatch");
                    typeNumberMap.put("1200_S", "regular_workers");
                    typeNumberMap.put("1050_S", "rehired_after_retirement");
                    typeNumberMap.put("dayan", "dayan");
                    //获取任职经历查询条件
                    QFilter empFiler = new QFilter("iscurrentversion", QCP.equals, "1")
                            .and("datastatus", QCP.equals, "1")
                            .and("isprimary", QCP.equals, "1")
                            .and("businessstatus", QCP.equals, "1")
                            .and("adminorg.id", QCP.in, orgList);
                    //获取任职经历信息
                    DataSet empData = QueryServiceHelper.queryDataSet(this.getClass().getName(), "hrpi_empposorgrel", EMP_FILED, empFiler.toArray(), "");
                    //标签维度
                    String labeldimension = dataEntity.getString("dgdl_labeldimension");
                    //存储人数信息
                    Map<String, Set<Long>> countMap = new HashMap<>();
                    //人员明细
                    Map<String, Set<Long>> personDetail = new HashMap<>();
                    //1=用工关系类型:dgdl_bz_laborreltype,2=一线非一线:dgdl_bz_jobproperty
                    int i = 0;
                    if (StringUtils.isNotEmpty(labeldimension)) {
                        if (labeldimension.contains("1") && labeldimension.contains("2")) {
                            i = 3;
                        } else if (labeldimension.contains("1")) {
                            i = 1;
                        } else if (labeldimension.contains("2")) {
                            i = 2;
                        }

                        //查询岗位属性
                        QFilter jobpropertyFilter = new QFilter("enable", QCP.equals, "1").and("number", QCP.in, PROPERTY_ARR);
                        DataSet jobpropertyDataSet = QueryServiceHelper.queryDataSet(this.getClass().getName(), "dgdl_hbss_jobproperty", "id,number,name", jobpropertyFilter.toArray(), "");
                        //查询岗位标签
                        QFilter joblabelFilter = new QFilter("enable", QCP.equals, "1").and("number", QCP.equals, "01");
                        DataSet joblabelDataSet = QueryServiceHelper.queryDataSet(this.getClass().getName(), "dgdl_hbss_joblabel", "id,number jobNumber", joblabelFilter.toArray(), "");
                        //查询人事业务档案类型
                        QFilter[] ermFiler = new QFilter[]{new QFilter("iscurrentversion", QCP.equals, "1"),
                                new QFilter("datastatus", QCP.equals, "1"),
                                new QFilter("filetype.number", QCP.in, ERM_TYPE)
                        };
                        DataSet ermData = QueryServiceHelper.queryDataSet(this.getClass().getName(), "hspm_ermanfile", "id,person.id personId,filetype.number typeNumber", ermFiler, "");
                        //职业信息empentrel
                        QFilter[] empentrelFiler = new QFilter[]{new QFilter("iscurrentversion", QCP.equals, "1"),
                                new QFilter("datastatus", QCP.equals, "1"),
                                new QFilter("islatestrecord", QCP.equals, "1"),
                                new QFilter("labrelstatusprd.id", QCP.equals, 1010L)
                        };
                        DataSet empentrelData = QueryServiceHelper.queryDataSet(this.getClass().getName(), "hrpi_empentrel", "id,person.id personId,laborrelstatus.number", empentrelFiler, "");
                        //试用中属于正式工
                        empentrelData=empentrelData.addField("case when laborrelstatus.number = '1010_S'  then '1200_S' else laborrelstatus.number end", "laborrelstatusNum");

                        ermData = ermData.join(empentrelData).on("personId", "personId").select("personId", "laborrelstatusNum typeNumber").finish();

                        if (i == 3) {
                            //任职经历关联岗位属性(一线非一线)
                            empData = empData.leftJoin(jobpropertyDataSet).on("jobpropertyId", "id").select("empId", "personId", "structnumber","structlongnumber", "jobLabelBasedataid", "number propertyNumber").finish();
                            for (String s : PROPERTY_ARR) {
                                empData = empData.addField("case when propertyNumber='" + s + "' then 0 else 1 end", "dgdl_property");
                            }
                            //关联人事业务档案类型
                            empData = empData.join(ermData).on("personId", "personId").select("personId", "structnumber","structlongnumber","typeNumber", "jobLabelBasedataid", "dgdl_property").finish();
                            empData = empData.leftJoin(joblabelDataSet).on("jobLabelBasedataid", "id").select("personId", "structnumber", "structlongnumber","typeNumber", "jobNumber", "dgdl_property").finish();
                            empData = empData.addField("case when typeNumber = '1200_S'  and jobNumber = '01' then 1 else 0 end", "dgdl_label");
                            //岗位标签分组
                            empData = empData.addField("case when typeNumber= '1200_S' then jobNumber " +
                                    " when typeNumber= '1030_S' then '1030' " +
                                    " when typeNumber= '1040_S' then '1040' " +
                                    " when typeNumber= '1050_S' then '1050' " +
                                    " when typeNumber= '1220_S' then '1220' " +
                                    " else '0' end", "joblabel");
                            //人员明细统计
                            DataSet copyEmpData = empData.copy();
                            while (copyEmpData.hasNext()) {
                                Row row = copyEmpData.next();
                                //组织
                                String structNumber = row.getString("structnumber");
                                if(!orgStructNumbers.contains(structNumber)){
                                    String structlongnumber = row.getString("structlongnumber");
                                    String[] split = structlongnumber.split("!");

                                    for (int j = split.length-1; j >0 ; j--) {

                                        if(orgStructNumbers.contains(split[j])){
                                            structNumber=split[j];
                                            break;
                                        }

                                    }
                                }

                                //用工关系类型
                                String typeNumber = row.getString("typeNumber");
                                if ("1200_S".equals(typeNumber)) {
                                    //大雁
                                    if ("1".equals(row.getString("dgdl_label"))) {
                                        typeNumber = "dayan";
                                    }
                                }
                                //岗位
                                String property = row.getString("dgdl_property");
                                //明细
                                Set<Long> detailList = personDetail.get(structNumber + typeNumber + property);
                                //人员
                                Long personId = row.getLong("personId");
                                if (detailList == null) {
                                    detailList = new HashSet<>();
                                }
                                detailList.add(personId);
                                personDetail.put(structNumber + typeNumber + property, detailList);
                            }
                            //分组后结果集
                            DataSet groupDataSet = empData.groupBy(new String[]{"structnumber", "structlongnumber","typeNumber", "joblabel", "dgdl_label", "dgdl_property"}).count().finish();
                            while (groupDataSet.hasNext()) {
                                Row next = groupDataSet.next();
                                //组织长编码
                                String structNumber = next.getString("structNumber");


                                if(!orgStructNumbers.contains(structNumber)){
                                    String structlongnumber = next.getString("structlongnumber");
                                    String[] split = structlongnumber.split("!");

                                    for (int j = split.length-1; j >0 ; j--) {

                                        if(orgStructNumbers.contains(split[j])){
                                            structNumber=split[j];
                                            break;
                                        }

                                    }
                                }
                                //岗位属性
                                String property = next.getString("dgdl_property");
                                //用工关系类型
                                String typeNumber = next.getString("typeNumber");
                                if ("1200_S".equals(typeNumber)) {
                                    //大雁
                                    if ("1".equals(next.getString("dgdl_label"))) {
                                        typeNumber = "dayan";
                                    }
                                }
                                //人员明细
                                Set<Long> details = personDetail.get(structNumber + typeNumber + property);
                                //总人数
                                countMap.put(structNumber + "-3-" + typeNumberMap.get(typeNumber) + property, details);
                            }
                        } else {
                            if (i == 1) {
                                //关联人事业务档案类型
                                empData = empData.join(ermData).on("personId", "personId").select("personId", "structnumber","structlongnumber", "typeNumber", "jobLabelBasedataid").finish();
                                //关联岗位标签
                                empData = empData.leftJoin(joblabelDataSet).on("jobLabelBasedataid", "id").select("personId", "structnumber","structlongnumber", "typeNumber", "jobNumber").finish();
                                empData = empData.addField("case when typeNumber = '1200_S'  and jobNumber = '01' then 1 else 0 end", "dgdl_label");
                                //岗位标签分组
                                empData = empData.addField("case when typeNumber= '1200_S' then jobNumber " +
                                        " when typeNumber= '1070_S' then '1070' " +
                                        " when typeNumber= '1110_S' then '1110' " +
                                        " when typeNumber= '1150_S' then '1150' " +
                                        " when typeNumber= '1190_S' then '1190' " +
                                        " else '0' end", "joblabel");

                                DataSet copyEmpData = empData.copy();
                                while (copyEmpData.hasNext()) {
                                    Row row = copyEmpData.next();
                                    //组织
                                    String structNumber = row.getString("structnumber");
                                    if(!orgStructNumbers.contains(structNumber)){
                                        String structlongnumber = row.getString("structlongnumber");
                                        String[] split = structlongnumber.split("!");

                                        for (int j = split.length-1; j >0 ; j--) {

                                            if(orgStructNumbers.contains(split[j])){
                                                structNumber=split[j];
                                                break;
                                            }

                                        }
                                    }
                                    //用工关系类型
                                    String typeNumber = row.getString("typeNumber");
                                    if ("1200_S".equals(typeNumber)) {
                                        //大雁
                                        if ("1".equals(row.getString("dgdl_label"))) {
                                            typeNumber = "dayan";
                                        }
                                    }
                                    //明细
                                    Set<Long> detailList = personDetail.get(structNumber + typeNumber);
                                    //人员
                                    Long personId = row.getLong("personId");
                                    if (detailList == null) {
                                        detailList = new HashSet<>();
                                    }
                                    detailList.add(personId);
                                    personDetail.put(structNumber + typeNumber, detailList);
                                }
                                //分组后结果集
                                DataSet groupDataSet = empData.groupBy(new String[]{"structnumber", "structlongnumber","typeNumber", "joblabel", "dgdl_label"}).count().finish();
                                while (groupDataSet.hasNext()) {
                                    Row next = groupDataSet.next();
                                    //组织长编码
                                    String structNumber = next.getString("structNumber");
                                    if(!orgStructNumbers.contains(structNumber)){
                                        String structlongnumber = next.getString("structlongnumber");
                                        String[] split = structlongnumber.split("!");

                                        for (int j = split.length-1; j >0 ; j--) {

                                            if(orgStructNumbers.contains(split[j])){
                                                structNumber=split[j];
                                                break;
                                            }

                                        }
                                    }
                                    //用工关系类型
                                    String typeNumber = next.getString("typeNumber");
                                    if ("1200_S".equals(typeNumber)) {
                                        //大雁
                                        if ("1".equals(next.getString("dgdl_label"))) {
                                            typeNumber = "dayan";
                                        }
                                    }
                                    //人员明细
                                    Set<Long> details = personDetail.get(structNumber + typeNumber);
                                    //总人数
                                    countMap.put(structNumber + "-1-" + typeNumberMap.get(typeNumber), details);
                                }
                            } else if (i == 2) {
                                //任职经历关联岗位属性
                                empData = empData.leftJoin(jobpropertyDataSet).on("jobpropertyId", "id").select("empId", "personId", "structnumber","structlongnumber", "number propertyNumber").finish();
                                for (String s : PROPERTY_ARR) {
                                    empData = empData.addField("case when propertyNumber='" + s + "' then 0 else 1 end", "dgdl_property");
                                    groupList.add("dgdl_property");
                                }

                                DataSet copyEmpData = empData.copy();
                                while (copyEmpData.hasNext()) {
                                    Row row = copyEmpData.next();
                                    //组织
                                    String structNumber = row.getString("structnumber");
                                    if(!orgStructNumbers.contains(structNumber)){
                                        String structlongnumber = row.getString("structlongnumber");
                                        String[] split = structlongnumber.split("!");

                                        for (int j = split.length-1; j >0 ; j--) {

                                            if(orgStructNumbers.contains(split[j])){
                                                structNumber=split[j];
                                                break;
                                            }

                                        }
                                    }
                                    //岗位
                                    String property = row.getString("dgdl_property");
                                    //明细
                                    Set<Long> detailList = personDetail.get(structNumber + property);
                                    //人员
                                    Long personId = row.getLong("personId");
                                    if (detailList == null) {
                                        detailList = new HashSet<>();
                                    }
                                    detailList.add(personId);
                                    personDetail.put(structNumber + property, detailList);
                                }
                                //任职经历分组过滤条件
                                //分组后结果集
                                DataSet groupDataSet = empData.groupBy(groupList.toArray(new String[0])).count().finish();
                                while (groupDataSet.hasNext()) {
                                    Row next = groupDataSet.next();
                                    //组织长编码
                                    String structNumber = next.getString("structNumber");
                                    if(!orgStructNumbers.contains(structNumber)){
                                        String structlongnumber = next.getString("structlongnumber");
                                        String[] split = structlongnumber.split("!");

                                        for (int j = split.length-1; j >0 ; j--) {

                                            if(orgStructNumbers.contains(split[j])){
                                                structNumber=split[j];
                                                break;
                                            }

                                        }
                                    }
                                    //岗位
                                    String property = next.getString("dgdl_property");
                                    //人员明细
                                    Set<Long> details = personDetail.get(structNumber + property);
                                    //总人数
                                    countMap.put(structNumber + "-2-" + next.getString("dgdl_property"), details);
                                }
                            }
                        }
                    }
                    for (String structNumber : orgStructNumbers) {
                        //分录行
                        DynamicObject addNew = null;
                        if (i == 1) {
                            //用工关系类型
                            for (String type : typeMap.keySet()) {
                                addNew = bzCollection.addNew();
                                addNew.set("dgdl_bz_laborreltype", type);
                                //key
                                String key = structNumber + "-1-" + type;
                                Set<Long> detailList = countMap.get(key);
                                //实际人数
                                if (detailList == null) {
                                    addNew.set("dgdl_bz_actual", 0);
                                } else {
                                    //人员明细
                                    DynamicObjectCollection personList = addNew.getDynamicObjectCollection("dgdl_mulperson");
                                    personList.clear();
                                    for (Long pkId : detailList) {
                                        DynamicObject newCurrency = new DynamicObject(personList.getDynamicObjectType());
                                        newCurrency.set("fbasedataid", pkId);
                                        personList.add(newCurrency);
                                    }
                                    addNew.set("dgdl_mulperson", personList);
                                    addNew.set("dgdl_bz_actual", detailList.size());
                                }
                                this.setBillData(addNew, dataEntity, hierarchyMap, structNumber, notFindOrgMap);
                            }
                        } else if (i == 2) {
                            //岗位属性
                            for (String property : propertyMap.keySet()) {
                                addNew = bzCollection.addNew();
                                addNew.set("dgdl_bz_jobproperty", property);
                                //key
                                String key = structNumber + "-2-" + property;
                                Set<Long> detailList = countMap.get(key);
                                //实际人数
                                if (detailList == null) {
                                    addNew.set("dgdl_bz_actual", 0);
                                } else {
                                    //人员明细
                                    DynamicObjectCollection personList = addNew.getDynamicObjectCollection("dgdl_mulperson");
                                    personList.clear();
                                    for (Long pkId : detailList) {
                                        DynamicObject newCurrency = new DynamicObject(personList.getDynamicObjectType());
                                        newCurrency.set("fbasedataid", pkId);
                                        personList.add(newCurrency);
                                    }
                                    addNew.set("dgdl_mulperson", personList);
                                    addNew.set("dgdl_bz_actual", detailList.size());
                                }

                                this.setBillData(addNew, dataEntity, hierarchyMap, structNumber, notFindOrgMap);
                            }
                        } else if (i == 3) {
                            //岗位属性
                            for (String property : propertyMap.keySet()) {
                                for (String type : typeMap.keySet()) {
                                    addNew = bzCollection.addNew();
                                    //用工关系类型
                                    addNew.set("dgdl_bz_laborreltype", type);
                                    //岗位属性
                                    addNew.set("dgdl_bz_jobproperty", property);
                                    //key
                                    String key = structNumber + "-3-" + type + property;
                                    Set<Long> detailList = countMap.get(key);
                                    //实际人数
                                    if (detailList == null) {
                                        addNew.set("dgdl_bz_actual", 0);
                                    } else {
                                        //人员明细
                                        DynamicObjectCollection personList = addNew.getDynamicObjectCollection("dgdl_mulperson");
                                        personList.clear();
                                        for (Long pkId : detailList) {
                                            DynamicObject newCurrency = new DynamicObject(personList.getDynamicObjectType());
                                            newCurrency.set("fbasedataid", pkId);
                                            personList.add(newCurrency);
                                        }
                                        addNew.set("dgdl_mulperson", personList);
                                        addNew.set("dgdl_bz_actual", detailList.size());
                                    }

                                    this.setBillData(addNew, dataEntity, hierarchyMap, structNumber, notFindOrgMap);
                                }
                            }
                        }
                    }
                    //3.保存月度编制信息管理
                    DynamicObject[] planObjs = new DynamicObject[]{planBill};
                    endResult = OperationServiceHelper.executeOperate("save", "dgdl_planmonth_bill", planObjs, option);
                    successId = (Long) endResult.getSuccessPkIds().get(0);
                    logger.info("CompilationPlanUrgeOp月度编制详情操作结果=" + endResult);
                    logger.info("CompilationPlanUrgeOp存储数量=" + notFindOrgMap.size());
                }


            //获取编制计划催办配置
            QFilter sonQFilter = new QFilter("dgdl_org.number", QCP.equals, sonNumber);
            DynamicObject[] planObjs = BusinessDataServiceHelper.load("dgdl_planperson", "dgdl_personplan.number", sonQFilter.toArray());
            List<String> planList = new ArrayList<>();
            for (DynamicObject planObj : planObjs) {
                String personNumber = planObj.getString("dgdl_personplan.number");
                planList.add(personNumber);
            }
            if (planList.isEmpty()) {
                return;
            }
            //获取平台人
            QFilter userQFilter = new QFilter("number", QCP.in, planList);
            DynamicObject[] userObjs = BusinessDataServiceHelper.load("bos_user", "id", userQFilter.toArray());
            logger.info("CompilationPlanUrgeOp查询平台人sql=" + userQFilter);
            List<Long> userList = new ArrayList<>();
            for (DynamicObject userObj : userObjs) {
                long id = userObj.getLong("id");
                userList.add(id);
            }
            logger.info("CompilationPlanUrgeOp发送消息人信息=" + userList + ",操作标识=" + operationKey + ",successId=" + successId);
            //处理时间
            SimpleDateFormat yearSim = new SimpleDateFormat("yyyy");
            //所属年份
            Date year = dataEntity.getDate("dgdl_year");
            String yearStr = yearSim.format(year);
            for (Long userId : userList) {
                String content = null;
                if ("enable".equals(operationKey) || "monthenable".equals(operationKey)) {
                    content = yearStr + "年" + sonName + "集团编制计划已启动,请及时维护编制信息";
                } else if ("urge".equals(operationKey)) {
                    content = "请尽快维护" + yearStr + "年" + sonName + "集团编制信息,如已维护,请忽略此消息";
                }
                if ("dgdl_planyear".equals(billKey)) {//年度编制计划
                    if (successId != null) {
                        String url = getUrl(successId, YEAR_FROMBILL);
                        if ("enable".equals(operationKey)) {//启用 年度编制计划,月度编制计划：启用（通知所有人）
                            sendMessage("年度编制计划消息通知", content, Collections.singletonList(userId), url);
                        } else if ("urge".equals(operationKey)) {//催办 取当前层级的下一层级 有当前编制规划单位对应 【HR通用角色】中的【角色管理】中的角色成员
                            sendMessage("年度编制计划消息通知", content, Collections.singletonList(userId), url);
                        }
                    }
                } else if ("dgdl_planmonth".equals(billKey)) {//月度编制计划
                    if (successId != null) {
                        String url = getUrl(successId, MONTH_FROMBILL);
                        if ("monthenable".equals(operationKey)) {//启用 年度编制计划,月度编制计划：启用（通知所有人）
                            sendMessage("月度编制计划消息通知", content, Collections.singletonList(userId), url);
                        } else if ("urge".equals(operationKey)) {//催办 取当前层级的下一层级 有当前编制规划单位对应 【HR通用角色】中的【角色管理】中的角色成员
                            sendMessage("月度编制计划消息通知", content, Collections.singletonList(userId), url);
                        }
                    }
                }

            }
        }
    }

    private Map<String, DynamicObject> getAdminByOrg(String orgNumber, String hierarchy) {
        logger.info("CompilationPlanUrgeOp所需生成的组织层级" + hierarchy + ",组织编码=" + orgNumber);
        //转换层级
        int intOrg = Integer.parseInt(hierarchy);
        //控制层级
        List<String> orghierarchyList = new ArrayList<>();
        for (int i = 1; i <= intOrg + 1; i++) {
            orghierarchyList.add("0" + i);
        }
        //获取当前子集团下所选层级的行政组织
        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("org.number", QCP.equals, orgNumber)
                .and("enable", QCP.equals, "1")
                .and("adminorglayer.number", QCP.in, orghierarchyList).and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,parent.number,structlongnumber,structnumber,adminorglayer.number", orgQFilter.toArray(), " createtime desc");
        logger.info("CompilationPlanUrgeOp行政组织长度=" + orgObjs.length);
        Map<String, DynamicObject> orgMap = new HashMap<>();

        for (DynamicObject orgData : orgObjs) {
            String orgStructNumber = orgData.getString("structnumber");
            orgMap.put(orgStructNumber, orgData);
        }
        return orgMap;
    }


    private Map<String, DynamicObject> getlastOrg(String orgNumber, String hierarchy) {
        logger.info("CompilationPlanUrgeOp所需生成的组织层级" + hierarchy + ",组织编码=" + orgNumber);
        //转换层级
        int intOrg = Integer.parseInt(hierarchy);

        //获取当前子集团下所选层级的行政组织
        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("org.number", QCP.equals, orgNumber)
                .and("enable", QCP.equals, "1")
                .and("adminorglayer.number", QCP.equals, "0" + intOrg).and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,parent.number,structlongnumber,structnumber,adminorglayer.number", orgQFilter.toArray(), " createtime desc");
        logger.info("CompilationPlanUrgeOp行政组织长度=" + orgObjs.length);
        Map<String, DynamicObject> orgMap = new HashMap<>();

        for (DynamicObject orgData : orgObjs) {
            String orgStructNumber = orgData.getString("structlongnumber");
            orgMap.put(orgStructNumber, orgData);
        }
        return orgMap;
    }

    private List<Long> getOrgList(String orgNumber) {
        List<Long> orglist=new ArrayList();
        //获取当前子集团下所选层级的行政组织
        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("org.number", QCP.equals, orgNumber)
                .and("enable", QCP.equals, "1")
                .and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,parent.number,structlongnumber,structnumber,adminorglayer.number", orgQFilter.toArray(), " createtime desc");

        for (DynamicObject orgData : orgObjs) {

            orglist.add(orgData.getLong("id"));
        }
        return orglist;
    }


    /**
     * 发送消息
     *
     * @param title    标题
     * @param content  内容
     * @param userList 用户列表
     */
    public static void sendMessage(String title, String content, List<Long> userList, String url) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setType(MessageInfo.TYPE_MESSAGE);
        messageInfo.setTitle(title);
        messageInfo.setTitle(content);
        messageInfo.setUserIds(userList);
        messageInfo.setContentUrl(url);
        long l = MessageCenterServiceHelper.sendMessage(messageInfo);
        logger.info("消息id" + l);
    }

    private String getUrl(Long successId, String frombill) {
        RequestContext ctx = RequestContext.get();
        //拿到完整的客户端网址
        this.clientPath = UrlService.getDomainContextUrlByTenantCode(ctx.getTenantCode());
        StringBuilder contentUrl = new StringBuilder(clientPath);
        //pc端跳转单据路径
        contentUrl.append("?formId=").append(frombill).append("&app=dgdl_homs_ext").append("&pkId=").append(successId);
        logger.info("CompilationPlanUrgeOp:跳转地址为 " + contentUrl);
        return contentUrl.toString();
    }

    /**
     * @param addNew       当前分录
     * @param dataEntity   月度计划
     * @param hierarchyMap 组织信息
     * @param structNumber 当前组织长编码
     */
    private void setBillData(DynamicObject addNew, DynamicObject dataEntity, Map<String, DynamicObject> hierarchyMap, String structNumber, Map<String, DynamicObject> notFindOrgMap) {
        //解析行政组织长编码
        DynamicObject lastOrg = hierarchyMap.get(structNumber);
        if (Objects.isNull(lastOrg)) {
            lastOrg = notFindOrgMap.get(structNumber);
            if (Objects.isNull(lastOrg)) {
                //获取当前子集团下所选层级的行政组织
                QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                        .and("structnumber", QCP.equals, structNumber)
                        .and("iscurrentversion", QCP.equals, "1");
                lastOrg = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "id,structlongnumber,number,adminorglayer.number", orgQFilter.toArray());
                //将不属于当前bu下的组织进行存储
                notFindOrgMap.put(structNumber, lastOrg);
            }
        }
        //末端组织
        addNew.set("adminorg", lastOrg);
        //通过长编码获取各个父级行政组织
        String stringLongNumber = lastOrg.getString("structlongnumber");
        String[] splitArray = stringLongNumber.split("!");
        for (int i = 0; i < splitArray.length; i++) {
            String longNumber = splitArray[i];
            DynamicObject obj = hierarchyMap.get(longNumber);
            if (Objects.isNull(obj)) {
                obj = notFindOrgMap.get(longNumber);
                if (Objects.isNull(obj)) {
                    //获取当前子集团下所选层级的行政组织
                    QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                            .and("structnumber", QCP.equals, longNumber)
                            .and("iscurrentversion", QCP.equals, "1");
                    obj = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "id,name,number,adminorglayer.number", orgQFilter.toArray());
                    addNew.set("dgdl_is_org", "02");
                    //将不属于当前bu下的组织进行存储
                    notFindOrgMap.put(longNumber, obj);
                }
            }
            if (Objects.nonNull(obj)) {
                String adminorglayer = obj.getString("adminorglayer.number");
                if ("02".equals(adminorglayer)) {
                    addNew.set("dgdl_bz_adminorg1", obj);
                } else if ("03".equals(adminorglayer)) {
                    addNew.set("dgdl_bz_adminorg2", obj);
                } else if ("04".equals(adminorglayer)) {
                    addNew.set("dgdl_bz_adminorg3", obj);
                } else if ("05".equals(adminorglayer)) {
                    addNew.set("dgdl_bz_adminorg4", obj);
                } else if ("06".equals(adminorglayer)) {
                    addNew.set("dgdl_bz_adminorg5", obj);
                } else if ("07".equals(adminorglayer)) {
                    addNew.set("dgdl_bz_adminorg6", obj);
                }
            }
        }
        //当前组织层级
        String thisLayer = lastOrg.getString("adminorglayer.number");
        String layer = "";
        switch (thisLayer) {
            case "02":
                layer = "1";
                break;
            case "03":
                layer = "2";
                break;
            case "04":
                layer = "3";
                break;
            case "05":
                layer = "4";
                break;
            case "06":
                layer = "5";
                break;
            case "07":
                layer = "6";
                break;
        }
        logger.info("CompilationPlanUrgeOp末级组织=" + lastOrg.getString("name") + "对应层级=" + layer);
        addNew.set("dgdl_layer", layer);
        //控编方式
        addNew.set("dgdl_bz_controlediting", dataEntity.getString("dgdl_controlediting"));
        //弹性方式
        addNew.set("dgdl_bz_way", dataEntity.getString("dgdl_way"));
        //弹性额度
        addNew.set("dgdl_bz_percent", dataEntity.getInt("dgdl_people"));
        //是否统一控编
        String isunify = dataEntity.getString("dgdl_isunify");
        if ("true".equals(isunify)) {
            //是否系统内置
            addNew.set("dgdl_bz_isinsert", "01");
        }
        addNew.set("dgdl_isfreeze", "01");

    }
}

