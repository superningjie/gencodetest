package dgdl.odc.homs.task;

import com.alibaba.dubbo.common.utils.StringUtils;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:月度编制计划实际人数同步
 * @Author: zhangjun
 * @Since: 2024/3/20
 **/
public class PlanMonthBillCreateTask extends AbstractTask {
    private static final Log logger = LogFactory.getLog(PlanMonthBillCreateTask.class);


    /**
     * 任职经历获取字段
     */
    private final static String EMP_FILED = "id as empId,person.id as personId," +
            "adminorg.structnumber as structnumber," +
            "adminorg.structlongnumber as structlongnumber," +
            "job.dgdl_jobproperty.id as jobpropertyId," +
            "job.dgdl_joblabel.fbasedataid as jobLabelBasedataid";

    /**
     * 岗位i属性
     */
    private final static String[] PROPERTY_ARR = new String[]{"job1", "job2"};


    /**
     * 用工关系类型
     * 1030_S   实习（在职）
     * 1040_S   劳务派遣（在职）
     * 1050_S   退休返聘（在职）
     * 1200_S   正式工（在职）
     * 1220_S   合作伙伴（在职）
     */
    private final static String[] FILE_TYPE = new String[]{"1030_S", "1040_S", "1050_S", "1200_S", "1220_S"};

    /**
     * 档案业务类型
     */
    private final static String[] ERM_TYPE = new String[]{"1010_S", "1070_S", "1110_S", "1150_S", "1190_S"};


    /**
     * @param requestContext
     * @param requestMap
     * @throws KDException
     * @description
     */
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> requestMap) throws KDException {
        //在启用中的月度计划
        QFilter planQFilter = new QFilter("enable", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1");
        Object  plannumber = requestMap.get("plannumber");
        if(plannumber!=null&&!plannumber.toString().isEmpty()){
            planQFilter.and("number", QCP.equals, plannumber);
        }
        DynamicObject[] planMonths = BusinessDataServiceHelper.load("dgdl_planmonth", "id", planQFilter.toArray());
        List<Long> allId = Arrays.stream(planMonths).map(plan -> plan.getLong("id")).collect(Collectors.toList());
        logger.info("PlanMonthBillCreateTask启用中的编制计划=" + allId);
        QFilter bzBillQFilter = new QFilter("dgdl_planmonth.id", QCP.in, allId);
        //编制信息管理
        DynamicObject[] bzBillObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bill", "id,dgdl_planmonth", bzBillQFilter.toArray());
        for (DynamicObject bzBillObj : bzBillObjs) {
            //编制计划
            DynamicObject planMonth = bzBillObj.getDynamicObject("dgdl_planmonth");
            QFilter bzQFilter = new QFilter("dgdl_planmonth_bill.dgdl_planmonth.id", QCP.equals, planMonth.getPkValue());
            //获取层级,生成组织信息
            String hierarchy = planMonth.getString("dgdl_orghierarchy");
            DynamicObject dgdlOrg = planMonth.getDynamicObject("dgdl_org");
            String sonNumber = dgdlOrg.getString("number");
            //获取组织管理体系的所有行政组织
            Map<String, DynamicObject> hierarchyMap = getAdminByOrg(sonNumber, hierarchy);
            Set<String> orgStructNumbers = hierarchyMap.keySet();
            List<Long> orgList = getOrgList(sonNumber);

            //分组字段
            List<String> groupList = new ArrayList<>();
            groupList.add("structnumber");
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
            logger.info("PlanMonthBillCreateTask任职经历查询语句" + empFiler);
            //获取任职经历信息
            DataSet empData = QueryServiceHelper.queryDataSet(this.getClass().getName(), "hrpi_empposorgrel", EMP_FILED, empFiler.toArray(), "");
            //标签维度
            String labeldimension = planMonth.getString("dgdl_labeldimension");
            //存储人数信息
            Map<String,Integer> countMap = new HashMap<>();
            //人员明细
            Map<String, Set<Long>> personDetail = new HashMap<>();
            //1=用工关系类型:dgdl_bz_laborreltype,2=一线非一线:dgdl_bz_jobproperty
            int i = 0;
            logger.info("planstarttime:{}",new Date());
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

                    empData = empData.addField("case when propertyNumber='job1' then 0 " +
                            "when propertyNumber='job2' then 0 " +
                            "else 1 end", "dgdl_property");

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

                            for (int j = split.length-1; j>0 ; j--) {

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
                        countMap.put(structNumber + "-3-" + typeNumberMap.get(typeNumber) + property, details.size());
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
                            countMap.put(structNumber + "-1-" + typeNumberMap.get(typeNumber), details.size());
                        }
                    } else if (i == 2) {
                        //任职经历关联岗位属性
                        empData = empData.leftJoin(jobpropertyDataSet).on("jobpropertyId", "id").select("empId", "personId", "structnumber","structlongnumber", "number propertyNumber").finish();
                        empData = empData.addField("case when propertyNumber='job1' then 0 " +
                                "when propertyNumber='job2' then 0 " +
                                "else 1 end", "dgdl_property");

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
                            countMap.put(structNumber + "-2-" + next.getString("dgdl_property"), details.size());
                        }
                    }
                }
            }
            logger.info("planendtime:{}",new Date());

            logger.info("PlanMonthBillCreateTask存储的key值=" + countMap.keySet());
            logger.info("PlanMonthBillCreateTask查询语句=" + bzQFilter);
            //当前计划下所有编制详情
            DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "adminorg,dgdl_key,dgdl_bz_jobproperty,dgdl_bz_laborreltype,dgdl_layer,dgdl_bz_actual,dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6,dgdl_mulperson", bzQFilter.toArray());
            logger.info("PlanMonthBillCreateTask计划名称=" + planMonth.getString("name") + "下有:" + bzObjs.length + "条编制管理信息");
            int level=Integer.valueOf(hierarchy);
            Map<String,List<DynamicObject>> objlevelMap=new HashMap<>();

            for (DynamicObject bzObj : bzObjs) {
                String levelstr=bzObj.getString("dgdl_layer");
                if(levelstr!=null&&!levelstr.isEmpty()) {
                    List<DynamicObject> bzlist=new ArrayList<>();
                    if(objlevelMap.containsKey(levelstr)) {
                        bzlist=objlevelMap.get(levelstr);
                    }
                    bzlist.add(bzObj);
                    objlevelMap.put(levelstr,bzlist);

                }
            }



            for (int j = level; j >1; j--) {
                List<DynamicObject> bzlist=objlevelMap.get(String.valueOf(j));
                for (DynamicObject bz : bzlist) {
                    String  key = bz.getString("dgdl_key");

                    String parentornstruct = bz.getDynamicObject("dgdl_bz_adminorg"+(j-1)).getString("structnumber");
                    String adminorgstruct = bz.getDynamicObject("adminorg").getString("structnumber");
                    String parentkey=key.replace(adminorgstruct,parentornstruct);
                    Integer detailsize = countMap.get(key);
                    Integer parentydetailsize = countMap.get(parentkey);
                    if(parentydetailsize==null){
                        parentydetailsize=0;
                    }
                    if(detailsize==null){
                        detailsize=0;
                    }
//                    newcountMap.put(key,detailList.size())
                    parentydetailsize=parentydetailsize+detailsize;
                    countMap.put(parentkey,parentydetailsize);
                }


            }


            for (DynamicObject bzObj : bzObjs) {
                //末级组织
                String  key = bzObj.getString("dgdl_key");
                //岗位属性
                String property = kd.bos.util.StringUtils.isEmpty(bzObj.getString("dgdl_bz_jobproperty")) ? "" : bzObj.getString("dgdl_bz_jobproperty");
                //用工关系类型
                String type = kd.bos.util.StringUtils.isEmpty(bzObj.getString("dgdl_bz_laborreltype")) ? "" : bzObj.getString("dgdl_bz_laborreltype");
               Integer detailsize= countMap.get(key);

                //实际人数
                if (detailsize == null) {
                    bzObj.set("dgdl_bz_actual", 0);
                } else {
                    //人员明细
//                    DynamicObjectCollection personList = bzObj.getDynamicObjectCollection("dgdl_mulperson");
//                    personList.clear();
//                    for (Long pkId : detailList) {
//                        DynamicObject newCurrency = new DynamicObject(personList.getDynamicObjectType());
//                        newCurrency.set("fbasedataid", pkId);
//                        personList.add(newCurrency);
//                    }
//                    bzObj.set("dgdl_mulperson", personList);
                    bzObj.set("dgdl_bz_actual", detailsize);
                }
            }

//            SaveServiceHelper.update(orgObjs);
            SaveServiceHelper.update(bzObjs);
            //重新调整实际人数,上级实际人数 = 所有下级人数之和
//            if (bzObjs.length > 0) {
////                SaveServiceHelper.update(bzObjs);
//                for (DynamicObject bzDetail : bzObjs) {
//                    //当前组织层级
//                    String layer = bzDetail.getString("dgdl_layer");
//                    String orgLayer = "dgdl_bz_adminorg" + layer;
//                    //末级组织
//                    DynamicObject lastOrg = bzDetail.getDynamicObject("adminorg");
//                    //岗位属性
//                    String property = StringUtils.isEmpty(bzDetail.getString("dgdl_bz_jobproperty")) ? "" : bzDetail.getString("dgdl_bz_jobproperty");
//                    //用工关系类型
//                    String type = StringUtils.isEmpty(bzDetail.getString("dgdl_bz_laborreltype")) ? "" : bzDetail.getString("dgdl_bz_laborreltype");
//                    List<DynamicObject> allCollect = Arrays.stream(bzObjs).filter(obj -> Objects.nonNull(obj.getDynamicObject(orgLayer)) &&
//                            obj.getDynamicObject(orgLayer).getPkValue().equals(lastOrg.getPkValue())).collect(Collectors.toList());
//                    if (!allCollect.isEmpty()) {
//                        int allActual = allCollect.stream()
//                                .filter(obj -> property.equals(obj.getString("dgdl_bz_jobproperty")))
//                                .filter(obj -> type.equals(obj.getString("dgdl_bz_laborreltype")))
//                                .mapToInt(obj -> obj.getInt("dgdl_bz_actual")).sum();
//                        bzDetail.set("dgdl_bz_actual", allActual);
//                    }
//                }
//                long allSubOrgStartTime = System.currentTimeMillis();
//                SaveServiceHelper.update(bzObjs);
//                long allSubOrgEndTime = System.currentTimeMillis();
//                logger.info("PlanMonthBillCreateTask月度编制详情操作执行时间" + (allSubOrgEndTime - allSubOrgStartTime) / 1000);
//                logger.info("PlanMonthBillCreateTask修改完成");
//            }
        }
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

    private Map<String, DynamicObject> getAdminByOrg(String orgNumber, String hierarchy) {
        logger.info("PlanMonthBillCreateTask所需生成的组织层级" + hierarchy + ",组织编码=" + orgNumber);
        //转换层级
        int intOrg = Integer.parseInt(hierarchy);
        //控制层级
        List<String> orghierarchyList = new ArrayList<>();
        for (int i = 1; i <= intOrg + 1; i++) {
            orghierarchyList.add("0" + i);
        }
        //获取当前子集团下所选层级的行政组织
        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1").and("org.number", QCP.equals, orgNumber).and("adminorglayer.number", QCP.in, orghierarchyList).and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,parent.number,structlongnumber,structnumber,adminorglayer.number", orgQFilter.toArray(), " createtime desc");
        logger.info("PlanMonthBillCreateTask行政组织长度=" + orgObjs.length);
        Map<String, DynamicObject> orgMap = new HashMap<>();
        for (DynamicObject orgData : orgObjs) {
            String orgStructNumber = orgData.getString("structnumber");
            orgMap.put(orgStructNumber, orgData);
        }
        return orgMap;
    }
}
