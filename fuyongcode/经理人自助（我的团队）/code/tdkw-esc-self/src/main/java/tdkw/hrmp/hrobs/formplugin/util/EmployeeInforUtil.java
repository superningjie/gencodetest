package tdkw.hrmp.hrobs.formplugin.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.Tuple;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.ValueMapItem;
import kd.bos.entity.property.ComboProp;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.model.ruleengine.PolicyResult;
import kd.hr.hbp.common.model.ruleengine.RuleEngineResponseUtils;
import kd.hr.hbp.common.model.ruleengine.RuleResult;
import kd.hr.hbp.common.model.ruleengine.SceneResult;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hrcs.common.model.UserRoleInfo;
import kd.hrmp.hrpi.mservice.HRPIPersonService;
import kd.sdk.hr.hspm.business.service.AttacheHandlerService;
import kd.sdk.hr.hspm.common.constants.HspmCommonConstants;
import kd.sdk.hr.hspm.common.constants.MobileDrawConstants;
import kd.sdk.hr.hspm.common.enums.ClientTypeEnum;
import kd.sdk.hr.hspm.common.utils.ParamAnalysisUtil;
import kd.sdk.hr.hspm.common.utils.PersonModelUtil;
import kd.sdk.hr.hspm.common.utils.PropertyHelper;
import kd.sdk.hr.hspm.common.utils.QFilterUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.GetImageUtil;
import tdkw.hrmp.hrobs.formplugin.emputils.DateIntervalMerger;
import tdkw.hrmp.hrobs.formplugin.emputils.Interval;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description ： 员工信息查询-返回数据封装
 * @ClassName ：EmployeeINFOR
 * @author xxx
 * @Date ：2023/7/24 9:18
 * @Version: 1.0
 */
public class EmployeeInforUtil {
    private static final HRBaseServiceHelper HSPM_ERMANFILE = new HRBaseServiceHelper("hspm_ermanfile");

    //字段显示顺序
    private static int showOrder = 0;

    private static final Log logger = LogFactory.getLog(EmployeeInforUtil.class);


    /**
     * 员工头部信息：
     * 员工主键-pkPsndoc  姓名-name  编号-code  公司名称-orgName  公司主键-pkOrg  部门名称-deptName  部门主键-pkDept
     * 岗位名称-postName  岗位层级-postLevel  人员类型：离职，退休-psnType 异动事件-trnsevent  异动类型-trnstypename
     * 人员类型-psnclname  年龄-age   集团司龄-sysCorpAge   个人照片base64-image   个人照片byte格式-imagebytes
     * <p>
     * data.psnResumeInfo.pkPsndoc	string	员工主键
     * data.psnResumeInfo.name	string	姓名
     * data.psnResumeInfo.code	string	编号
     * data.psnResumeInfo.orgName	string	公司名称
     * data.psnResumeInfo.pkOrg	string	公司主键
     * data.psnResumeInfo.deptName	string	部门名称
     * data.psnResumeInfo.pkDept	string	部门主键
     * data.psnResumeInfo.postName	string	岗位名称
     * data.psnResumeInfo.postLevel	string	岗位层级
     * data.psnResumeInfo.psnType	string	人员类型：离职，退休
     * data.psnResumeInfo.trnsevent	int	异动事件
     * data.psnResumeInfo.trnstypename	string	异动类型
     * data.psnResumeInfo.psnclname	string	人员类型
     * data.psnResumeInfo.age	string	年龄
     * data.psnResumeInfo.sysCorpAge	string	集团司龄
     * data.psnResumeInfo.image	string	个人照片base64
     * data.psnResumeInfo.imagebytes	byte[]	个人照片byte格式
     *
     * @param personId 员工id
     * @return
     */
    public static JSONObject psnResumeInfo(String personId) {
        JSONObject fieldMapValue = new JSONObject();
        String name = "", code = "", orgName = "", pkOrg = "", deptName = "", pkDept = "";
        String postName = "", postLevel = "", psnType = "", trnsevent = "", trnstypename = "";
        String psnclname = "", sysCorpAge = "", image = "";
        int age = 0;
        QFilter filter = new QFilter("person", QCP.equals, Long.parseLong(personId));
        filter.and("iscurrentversion", QCP.equals, "1");
        filter.and("initstatus", QCP.equals, "2");
        //   filter.and("businessstatus",QCP.equals,"1");
        DynamicObject pernontsprop = BusinessDataServiceHelper.loadSingle("hrpi_pernontsprop", "person,age,entservicelen", filter.toArray());

        if (pernontsprop != null) {
            age = pernontsprop.getInt("age");
            sysCorpAge = String.valueOf(pernontsprop.getBigDecimal("entservicelen"));
            DynamicObject person = pernontsprop.getDynamicObject("person");
            if (person != null) {
                person = BusinessDataServiceHelper.loadSingle(person.getPkValue(), "hrpi_person", "headsculpture,name.number");
                name = person.getString("name");
                code = person.getString("number");
                try {
                    image = GetImageUtil.getUrlByHrPersonId(Long.parseLong(personId));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //加上是否主任职-是
        QFilter empposrelFilter = filter.copy();
        empposrelFilter.and("isprimary", QCP.equals, "1");
        DynamicObject[] empposrels = BusinessDataServiceHelper.load("hrpi_empposorgrel", "tdkw_postlevel,company,position,adminorg,tdkw_displayvalue", empposrelFilter.toArray(), "startdate desc");

        if (empposrels.length > 0) {
            DynamicObject empposrel = empposrels[0];
            DynamicObject company = empposrel.getDynamicObject("company");
            orgName = company != null ? company.getLocaleString("name").getLocaleValue_zh_CN() : null;
            pkOrg = company != null ? String.valueOf(company.getPkValue()) : null;
            DynamicObject adminorg = empposrel.getDynamicObject("adminorg");
            deptName = adminorg != null ? adminorg.getLocaleString("name").getLocaleValue_zh_CN() : null;
            pkDept = adminorg != null ? String.valueOf(adminorg.getPkValue()) : null;
            String displayValue = BusinessDataServiceHelper.loadSingle(empposrel.getLong("position.id"), "hbpm_positionhr").getString("tdkw_displayvalue");
            DynamicObject position = empposrel.getDynamicObject("position");
            postName = displayValue != null && !displayValue.isEmpty() ? displayValue : position.getLocaleString("name").getLocaleValue_zh_CN();
            postLevel = (String) empposrel.get("tdkw_postlevel");
        }


        fieldMapValue.put("pkPsndoc", personId);
        fieldMapValue.put("name", name);
        fieldMapValue.put("code", code);
        fieldMapValue.put("orgName", orgName);
        fieldMapValue.put("pkOrg", pkOrg);
        fieldMapValue.put("deptName", deptName);
        fieldMapValue.put("pkDept", pkDept);

        fieldMapValue.put("postName", postName);
        fieldMapValue.put("postLevel", postLevel);
        fieldMapValue.put("psnType", psnType);
        fieldMapValue.put("trnsevent", trnsevent);
        fieldMapValue.put("trnstypename", trnstypename);

        fieldMapValue.put("psnclname", psnclname);
        fieldMapValue.put("age", age);
        fieldMapValue.put("sysCorpAge", sysCorpAge);
        fieldMapValue.put("image", image);
        logger.info("XXX个人信息：头部数据" + fieldMapValue);

        return fieldMapValue;
    }


    /**
     * 所有页签数据
     * data.psnInfoSetVoList[].code	string	编码
     * data.psnInfoSetVoList[].name	string	标题
     * data.psnInfoSetVoList[].pkInfoSet	string	主键key
     * data.psnInfoSetVoList[].pkFiledCode	string	记录的主键名称
     * data.psnInfoSetVoList[].tableCode	string	表名
     * data.psnInfoSetVoList[].canEditFlag	string	是否可编辑 N=否，Y=是
     * data.psnInfoSetVoList[].canDeleteFlag	string	是否可删除 N=否，Y=是
     * data.psnInfoSetVoList[].checkFlag	string	是否需要审核 N=否，Y=是
     * data.psnInfoSetVoList[].addFlag	string	是否可新增子集 N=否，Y=是
     * data.psnInfoSetVoList[].mustEntryFlag	string	是否子集必录 N=否，Y=是
     * data.psnInfoSetVoList[].showOrder	int	显示顺序
     * data.psnInfoSetVoList[].auditStatus	int	该信息项的审核状态 0=待提交，1=待审核，2=审核通过，3=审核未通过
     *
     * @param mainEntry
     * @param pkPsndoc
     * @return
     */
    public static JSONArray psnInfoSetVoList(List<Map<String, Object>> mainEntry, String pkPsndoc) {

        JSONArray psnInfoSetVoList = new JSONArray();
        int showOrderTab = 0;
        for (Map<String, Object> main : mainEntry) {
            logger.info("XXX个人信息：页签栏配置" + showOrder + main);
            try {
                String groupName = (String) main.get("groupname");
                String tableCode = (String) main.get("mappingFormid");
                String pkInfoSet = (String) main.get("gid");
//                //不显示任职经历和前任职经历，任职经历总表显示成任职经历，因为之前和前端做过图标映射，不改标识前端不显示图标
//                if(StringUtils.equals(tableCode,"hrpi_emporgrelall")){
//                    tableCode = "hrpi_empposorgrel";
//                    groupName ="任职经历";
//                }
//                if(StringUtils.equals(tableCode,"hrpi_preworkexp")||StringUtils.equals(tableCode,"hrpi_empposorgrel")){
//                    continue;
//                }

                JSONObject psnInfoSetVo = new JSONObject();
                psnInfoSetVo.put("addFlag", "Y");
                psnInfoSetVo.put("canDeleteFlag", "Y");
                psnInfoSetVo.put("canEditFlag", "Y");
                psnInfoSetVo.put("checkFlag", "Y");
                psnInfoSetVo.put("code", tableCode);
                psnInfoSetVo.put("mustEntryFlag", "Y");
                psnInfoSetVo.put("name", groupName);
                psnInfoSetVo.put("pkFiledCode", tableCode);
                psnInfoSetVo.put("pkInfoSet", pkInfoSet);
                psnInfoSetVo.put("showOrder", showOrderTab++);
                psnInfoSetVo.put("tableCode", tableCode);
                // -获取的二级菜单；没有二级菜单就是直接返回原来的菜单
                List<Map<String, Object>> groups = ParamAnalysisUtil.getGroups(main);
                logger.info("二级菜单:" + JSONObject.toJSONString(groups));
                List<Map<String, Object>> fields = new ArrayList<>();
                JSONArray records = new JSONArray();
                JSONArray fieldTemplates = new JSONArray();
                if (!CollectionUtils.isEmpty(groups)) {
                    for (Map<String, Object> group : groups) {
                        //二级菜单的字段
                        if (fields.size() == 0) {
                            fields = ParamAnalysisUtil.getFields(group);
                        } else {
                            List<Map<String, Object>> fields1 = ParamAnalysisUtil.getFields(group);
                            for (Map<String, Object> item : fields1) {
                                fields.add(item);
                            }
                        }
                    }
                }
                logger.info("二级菜单字段：" + JSONObject.toJSONString(fields));
                //用来排序的
                Map<Integer, String> fieldsOrder = fieldsOrder(fields);
                if (!CollectionUtils.isEmpty(fields)) {
                    //字段可能来自不同表
                    Map<String, List<Map<String, Object>>> groupFields = (Map) fields.stream().collect(Collectors.groupingBy((val) -> {
                        return (String) val.get("pnumber");
                    }));

                    showOrder = 0;
                    //分组后字段构建
                    for (Map.Entry<String, List<Map<String, Object>>> entry : groupFields.entrySet()) {
                        List<Map<String, Object>> value = entry.getValue();
                        String key = entry.getKey();
                        //查询的字段以及对应的中文名称
                        Map<String, String> selectPropsName = value.stream().collect(Collectors.toMap(item -> (String) item.get(HRBaseConstants.NUMBER), item -> (String) item.get("displayname")));
                        //查询的字段以及对应的字段类型
                        Map<String, String> selectPropsType = value.stream().collect(Collectors.toMap(item -> (String) item.get(HRBaseConstants.NUMBER), item -> (String) item.get("type")));

                        if (fieldTemplates.size() == 0) {
                            fieldTemplates = fieldTemplates(selectPropsName, selectPropsType, pkInfoSet, key);
                        } else {
                            fieldTemplates.addAll(fieldTemplates(selectPropsName, selectPropsType, pkInfoSet, key));
                        }
                    }
                    fieldTemplates = fieldTemplatesSort(fieldTemplates, fieldsOrder);
                    //数据构建
                    int groupFieldsSize = groupFields.size();

                    if ("hrpi_pereduexp".equals(tableCode)) {
                        logger.info("数据来源:" + groupFieldsSize);
                    }

                    //数据来源一张表
                    if (groupFieldsSize == 1) {
                        for (Map.Entry<String, List<Map<String, Object>>> entry : groupFields.entrySet()) {
                            String key = entry.getKey();
                            List<Map<String, Object>> value = entry.getValue();
                            //查询的字段
                            List<String> selectProps = value.stream().map(val -> (String) val.get(HRBaseConstants.NUMBER)).collect(Collectors.toList());
                            //查询的字段以及对应的中文名称
                            Map<String, String> selectPropsName = value.stream().collect(Collectors.toMap(item -> (String) item.get(HRBaseConstants.NUMBER), item -> (String) item.get("displayname")));
                            //查询的字段以及对应的字段类型
                            Map<String, String> selectPropsType = value.stream().collect(Collectors.toMap(item -> (String) item.get(HRBaseConstants.NUMBER), item -> (String) item.get("type")));

                            QFilter qFilter = getQFilter(entry, setCusParams(pkPsndoc));
                            if (qFilter != null) {
                                HRBaseServiceHelper serviceHelper = new HRBaseServiceHelper(key);
                                // 排序字段
                                String orderBys = "createtime";
                                //任职经历、前工作经历-根据时间从最新到最后，
                                if (StringUtils.equals("hrpi_empposorgrel", key) || StringUtils.equals("hrpi_preworkexp", key)) {
                                    orderBys = "startdate desc";
                                }
                                //任职经历总
                                if (StringUtils.equals("hrpi_emporgrelall", key)) {
                                    selectProps.add("tdkw_changereason");
                                    orderBys = "startdate desc";
                                }
                                //教育经历
                                if (StringUtils.equals("hrpi_pereduexp", key)) {
                                    orderBys = "gradutiondate desc";
                                }
                                //其他任职信息
                                if (StringUtils.equals("tdkw_hrpi_otheremployinf", key)) {
                                    orderBys = "tdkw_startdate desc";
                                }
                                //家庭成员信息
                                if (StringUtils.equals("hrpi_familymemb", key)) {
                                    orderBys = "familymembship.number asc";
                                }
                                // 社会团体
                                if (StringUtils.equals("tdkw_hrpi_social_group", key)) {
                                    orderBys = "tdkw_start_date desc";
                                }
                                DynamicObject[] dataArray = serviceHelper.query(String.join(HRBaseConstants.COMMA, selectProps),
                                        new QFilter[]{qFilter}, orderBys);
                                if (StringUtils.equals("hrpi_emporgrelall", key)) {
                                    logger.info("XXX个人信息：任职经历获取条数1前" + dataArray.length);
                                    // 过滤虚拟兼职
                                    List<DynamicObject> filterEmpDataList = Arrays.stream(dataArray).filter(dynamicObject -> !"XY00017".equals(dynamicObject.getString("tdkw_changereason.number"))).collect(Collectors.toList());
                                    List<Long> ids = filterEmpDataList.stream().mapToLong(dynamicObject -> dynamicObject.getLong("id")).boxed().collect(Collectors.toList());
                                    DynamicObject[] load = BusinessDataServiceHelper.load("hrpi_emporgrelall", String.join(HRBaseConstants.COMMA, selectProps), new QFilter[]{new QFilter("id", QCP.in, ids)}, orderBys);
                                    // 按照三个字段进行分组，通过stream分组，key希望有两个一个是id，一个是分组的依据
                                    // startdate,enddate,company,adminorg,position,tdkw_jobsequence,tdkw_ranks,isprimary,postype,tdkw_changereason
                                    Map<String, List<DynamicObject>> empListMap = Arrays.stream(load).collect(
                                            Collectors.groupingBy(dynamicObject -> dynamicObject.getString("adminorg")
                                                    + dynamicObject.getString("tdkw_companyname")
                                                    + dynamicObject.getString("position")));

                                    // 取出empListMap的value的size大于的数据返回map
                                    Map<String, List<DynamicObject>> empListMergeMap = empListMap.entrySet().stream().filter(entry1 -> entry1.getValue().size() > 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                    // 判断empListMergeMap的开始和结束时间是否间隔两天以上
                                    empListMergeMap.forEach((tagKey, dynamicObjects) -> {
                                        dynamicObjects.sort(Comparator.comparing(empDynamicObject -> empDynamicObject.getDate("startdate")));
                                        // 取出开始时间
                                        List<Date> startDateList = dynamicObjects.stream().map(empDynamicObject -> empDynamicObject.getDate("startdate")).collect(Collectors.toList());
                                        // 取出结束时间
                                        List<Date> endDateList = dynamicObjects.stream().map(empDynamicObject -> empDynamicObject.getDate("enddate")).collect(Collectors.toList());
                                        List<Interval> mergedIntervals = DateIntervalMerger.mergeIntervals(startDateList, endDateList);
                                        // 原始任职数据id集合
                                        List<Long> originIds = dynamicObjects.stream().mapToLong(empDynamicObject -> empDynamicObject.getLong("id")).boxed().collect(Collectors.toList());

                                        // 如果存在，则需要重新处理数据
                                        if (dynamicObjects.size() != mergedIntervals.size()) {
                                            List<Long> keepIds = new ArrayList<>();
                                            // 根据开始时间和结束时间取出数据
                                            mergedIntervals.forEach(interval -> {
                                                Date start = interval.start;
                                                Date end = interval.end;
                                                List<Long> perKeepIds = dynamicObjects.stream().filter(perKeepObject -> {
                                                    Date startdate = perKeepObject.getDate("startdate");
                                                    Date enddate = perKeepObject.getDate("enddate");
                                                    return startdate.equals(start) && enddate.equals(end);
                                                }).mapToLong(empDynamicObject -> empDynamicObject.getLong("id")).boxed().collect(Collectors.toList());
                                                keepIds.addAll(perKeepIds);
                                            });
                                            // 取差集、得到要删除的数据
                                            originIds.removeAll(keepIds);
                                            // 取出要删除的数据
                                            List<DynamicObject> deleteList = dynamicObjects.stream().filter(empDynamicObject -> originIds.contains(empDynamicObject.getLong("id"))).collect(Collectors.toList());
                                            List<DynamicObject> keepList = dynamicObjects.stream().filter(empDynamicObject -> keepIds.contains(empDynamicObject.getLong("id"))).collect(Collectors.toList());
                                            int keepSize = keepIds.size();
                                            // 合并的条数
                                            int mergeSize = mergedIntervals.size();
                                            // 从待删除列表中取出数据重新设置值
                                            List<DynamicObject> mergeAfterList4Del = deleteList.subList(0, mergeSize - keepSize);
                                            // 把时间相同的数据移除
                                            mergedIntervals.removeIf(interval -> {
                                                Date start = interval.start;
                                                Date end = interval.end;
                                                return dynamicObjects.stream().anyMatch(empDynamicObject -> {
                                                    Date startdate = empDynamicObject.getDate("startdate");
                                                    Date enddate = empDynamicObject.getDate("enddate");
                                                    return startdate.equals(start) && enddate.equals(end);
                                                });
                                            });
                                            // 遍历设置合并后的时间
                                            for (int i = 0; i < mergeSize - keepSize; i++) {
                                                mergeAfterList4Del.get(i).set("startdate", mergedIntervals.get(i).start);
                                                mergeAfterList4Del.get(i).set("enddate", mergedIntervals.get(i).end);
                                            }
                                            // 把保留的数组和重新设置的数组进行合并
                                            List<DynamicObject> newMergeList = new ArrayList<>();
                                            newMergeList.addAll(mergeAfterList4Del);
                                            newMergeList.addAll(keepList);
                                            // 清空原来的数据
                                            empListMap.put(tagKey, newMergeList);
                                        }
                                    });
                                    // 取出empListMap中的所有List<DynamicObject> 对empList按照startdate降序排列
                                    List<DynamicObject> dataArrayNullEmp = empListMap.values().stream().flatMap(Collection::stream)
                                            .sorted((o1, o2) -> o2.getDate("startdate")
                                                    .compareTo(o1.getDate("startdate"))).collect(Collectors.toList());

                                    logger.info("XXX个人信息：任职经历获取条数2后" + dataArrayNullEmp.size());
                                    if (records.size() == 0) {
                                        records = records(dataArrayNullEmp.toArray(new DynamicObject[0]), selectProps, selectPropsName, pkInfoSet, selectPropsType);
                                    } else {
                                        records.addAll(records(dataArrayNullEmp.toArray(new DynamicObject[0]), selectProps, selectPropsName, pkInfoSet, selectPropsType));
                                    }
                                } else {
                                    if (records.size() == 0) {
                                        records = records(dataArray, selectProps, selectPropsName, pkInfoSet, selectPropsType);
                                    } else {
                                        records.addAll(records(dataArray, selectProps, selectPropsName, pkInfoSet, selectPropsType));
                                    }
                                }
                            }

                        }
                        records = recordsOrder(records, fieldsOrder);
                    }
                    //数据来源多张表
                    else {
                        Map<Integer, JSONArray> records1 = new HashMap<>();
                        for (Map.Entry<String, List<Map<String, Object>>> entry : groupFields.entrySet()) {
                            String key = entry.getKey();
                            List<Map<String, Object>> value = entry.getValue();
                            //查询的字段
                            List<String> selectProps = value.stream().map(val -> (String) val.get(HRBaseConstants.NUMBER)).collect(Collectors.toList());
                            //查询的字段以及对应的中文名称
                            Map<String, String> selectPropsName = value.stream().collect(Collectors.toMap(item -> (String) item.get(HRBaseConstants.NUMBER), item -> (String) item.get("displayname")));
                            //查询的字段以及对应的字段类型
                            Map<String, String> selectPropsType = value.stream().collect(Collectors.toMap(item -> (String) item.get(HRBaseConstants.NUMBER), item -> (String) item.get("type")));

                            QFilter qFilter = getQFilter(entry, setCusParams(pkPsndoc));
                            if (qFilter != null) {
                                HRBaseServiceHelper serviceHelper = new HRBaseServiceHelper(key);
                                // 排序字段
                                String orderBys = "createtime";
                                //任职经历、前工作经历-根据时间从最新到最后，
                                if (StringUtils.equals("hrpi_empposorgrel", key) || StringUtils.equals("hrpi_preworkexp", key)) {
                                    orderBys = "startdate desc";
                                }
                                //任职经历总
                                if (StringUtils.equals("hrpi_emporgrelall", key)) {
                                    selectProps.add("tdkw_changereason");
                                    orderBys = "startdate desc";
                                }
                                //教育经历
                                if (StringUtils.equals("hrpi_pereduexp", key)) {
                                    orderBys = "gradutiondate desc";
                                }
                                //其他任职信息
                                if (StringUtils.equals("tdkw_hrpi_otheremployinf", key)) {
                                    orderBys = "tdkw_startdate desc";
                                }
                                //家庭成员信息
                                if (StringUtils.equals("hrpi_familymemb", key)) {
                                    orderBys = "familymembship.number asc";
                                }
                                DynamicObject[] dataArray = serviceHelper.query(String.join(HRBaseConstants.COMMA, selectProps),
                                        new QFilter[]{qFilter}, orderBys);

                                List<DynamicObject> dataArrayNullEmp = new ArrayList<>();
                                if (StringUtils.equals("hrpi_emporgrelall", key)) {
                                    logger.info("XXX个人信息：任职经历获取条数前" + dataArray.length);
                                    List<Long> ids = new ArrayList<>();
                                    for (int i = 0; i < dataArray.length; i++) {
                                        ids.add(dataArray[i].getLong("id"));
                                    }
                                    DynamicObject[] load = BusinessDataServiceHelper.load("hrpi_emporgrelall", String.join(HRBaseConstants.COMMA, selectProps), new QFilter[]{new QFilter("id", QCP.in, ids)}, orderBys);
                                    for (DynamicObject dynamicObject : load) {
                                        if ("XY00017".equals(dynamicObject.getString("tdkw_changereason.number"))) {
                                            continue;
                                        }
                                        dataArrayNullEmp.add(dynamicObject);
                                    }
                                    recordsMany(records1, dataArrayNullEmp.toArray(new DynamicObject[0]), selectProps, selectPropsName, pkInfoSet, selectPropsType);
                                    logger.info("XXX个人信息：任职经历获取条数后" + dataArrayNullEmp.size());
                                } else {
                                    recordsMany(records1, dataArray, selectProps, selectPropsName, pkInfoSet, selectPropsType);
                                }
                            }
                        }

                        for (Integer item : records1.keySet()) {
                            JSONObject record = new JSONObject();
                            JSONArray allField = records1.get(item);
                            record.put("fields", allField);
                            record.put("recordId", item);
                            records.add(record);
                        }
                        records = recordsOrder(records, fieldsOrder);
                    }
                }
                try {
                    psnInfoSetVo.put("fieldTemplates", fieldTemplates);
                } catch (Exception e) {
                    logger.error("XXX个人信息：字段封装失败：" + e.getMessage());
                    logger.error("XXX个人信息：字段封装失败,页签栏配置" + main);
                }

                try {
                    psnInfoSetVo.put("records", records);
                } catch (Exception e) {
                    logger.error("XXX个人信息：数据封装失败：" + e.getMessage());
                    logger.error("XXX个人信息：数据封装失败,页签栏配置" + main);
                }
                try {
                    psnInfoSetVoList.add(psnInfoSetVo);
                } catch (Exception e) {
                    logger.error("XXX个人信息：数据添加失败：" + e.getMessage());
                    logger.error("XXX个人信息：数据添加失败,页签栏配置" + main);
                }


            } catch (Exception e) {
                logger.error("XXX个人信息：封装行数据，原因：" + e);
                logger.error("XXX个人信息：封装行数据，原因：" + e.getMessage());
                logger.error("XXX个人信息：封装行数据,页签栏配置" + main);
            }

        }
        logger.info("XXX个人信息：各页签栏数据" + psnInfoSetVoList);
        return psnInfoSetVoList;
    }


    /**
     * 配置字段
     * data.psnInfoSetVoList[].fieldTemplates[].showValue	object	展示的值
     * data.psnInfoSetVoList[].fieldTemplates[].value	object	原值
     * data.psnInfoSetVoList[].fieldTemplates[].pkInfoSet	string	父项目key
     * data.psnInfoSetVoList[].fieldTemplates[].code	string	字段编码
     * data.psnInfoSetVoList[].fieldTemplates[].name	string	字段名称
     * data.psnInfoSetVoList[].fieldTemplates[].showFlag	string	是否展示 N=否，Y=是
     * data.psnInfoSetVoList[].fieldTemplates[].canEditFlag	string	是否可编辑 N=否，Y=是
     * data.psnInfoSetVoList[].fieldTemplates[].checkFlag	string	是否需要审核 N=否，Y=是
     * data.psnInfoSetVoList[].fieldTemplates[].showListFlag	string	是否列表简要显示 N=否，Y=是
     * data.psnInfoSetVoList[].fieldTemplates[].mustFlag	string	是否必填 N=否，Y=是
     * data.psnInfoSetVoList[].fieldTemplates[].showOrder	int	显示顺序
     * data.psnInfoSetVoList[].fieldTemplates[].maxLength	int	最大长度
     * data.psnInfoSetVoList[].fieldTemplates[].dataType	int	数据类型：0=字符，1=整数，2=数量，3=日期，101=开始日期，102=结束日期，20=日期(无时区)，4=逻辑，5=参照，6=下拉，8=时间，9=大文本，10=图片，14=邮件地址，15=时间戳，16=自定义项，17=多语言，18=金额，100=日期公式
     * data.psnInfoSetVoList[].fieldTemplates[].pkRefInfo	string	参照主键
     * data.psnInfoSetVoList[].fieldTemplates[].refModel	string	参照模型
     * data.psnInfoSetVoList[].fieldTemplates[].alterFlag	string	是否改动（编辑入参时使用） N=否，Y=是
     **/
    public static JSONArray fieldTemplates(Map<String, String> fields, Map<String, String> selectPropsType, String pkInfoSet, String entityKey) {
        JSONArray fieldTemplates = new JSONArray();
        for (String field : fields.keySet()) {
            JSONObject fieldTemplate = new JSONObject();

            //信息组字段隐藏
            boolean hideOrNot = fieldHiding(field, entityKey);
            if (hideOrNot) {
                continue;
            }
            fieldTemplate.put("alterFlag", "N");
            fieldTemplate.put("canEditFlag", "N");
            fieldTemplate.put("checkFlag", "N");
            fieldTemplate.put("code", field);
            //解决bug-#90209 移动端领导查询的人员简历，结束日期2999-12-31时未显示为至今
            //http://ones.xxx.com/project/#/team/JbjqrWit/task/Fqvxbmqhokp3y9vN
            if (StringUtils.equals("enddate", field) && StringUtils.equals(entityKey, "hrpi_empposorgrel")) {
                fieldTemplate.put("dataType", 0);
            } else {
                fieldTemplate.put("dataType", getDataType(selectPropsType.get(field)));
            }

            fieldTemplate.put("maxLength", 40);
            fieldTemplate.put("mustFlag", "Y");

            fieldTemplate.put("name", fields.get(field));
            fieldTemplate.put("pkInfoSet", pkInfoSet);
            fieldTemplate.put("showFlag", "Y");

            fieldTemplate.put("showListFlag", "Y");
            fieldTemplate.put("showOrder", showOrder++);

            fieldTemplates.add(fieldTemplate);
        }

        return fieldTemplates;
    }


    /**
     * 配置字段对应值
     * data.psnInfoSetVoList[].records[].editType	int	编辑类型 1=修改 2=新增 3=删除
     * data.psnInfoSetVoList[].records[].recordId	string	记录id
     * data.psnInfoSetVoList[].records[].fields[].showValue	object	展示的值
     * data.psnInfoSetVoList[].records[].fields[].value	object	原值
     * data.psnInfoSetVoList[].records[].fields[].pkInfoSet	string	父项目key
     * data.psnInfoSetVoList[].records[].fields[].code	string	字段编码
     * data.psnInfoSetVoList[].records[].fields[].name	string	字段名称
     * data.psnInfoSetVoList[].records[].fields[].showFlag	string	是否展示 N=否，Y=是
     * data.psnInfoSetVoList[].records[].fields[].canEditFlag	string	是否可编辑 N=否，Y=是
     * data.psnInfoSetVoList[].records[].fields[].checkFlag	string	是否需要审核 N=否，Y=是
     * data.psnInfoSetVoList[].records[].fields[].showListFlag	string	是否列表简要显示 N=否，Y=是
     * data.psnInfoSetVoList[].records[].fields[].mustFlag	string	是否必填 N=否，Y=是
     * data.psnInfoSetVoList[].records[].fields[].showOrder	int	显示顺序
     * data.psnInfoSetVoList[].records[].fields[].maxLength	int	最大长度
     * data.psnInfoSetVoList[].records[].fields[].dataType	int	数据类型：0=字符，1=整数，2=数量，3=日期，101=开始日期，102=结束日期，20=日期(无时区)，4=逻辑，5=参照，6=下拉，8=时间，9=大文本，10=图片，14=邮件地址，15=时间戳，16=自定义项，17=多语言，18=金额，100=日期公式
     * data.psnInfoSetVoList[].records[].fields[].pkRefInfo	string	参照主键
     * data.psnInfoSetVoList[].records[].fields[].refModel	string	参照模型
     * data.psnInfoSetVoList[].records[].fields[].alterFlag	string	是否改动（编辑入参时使用） N=否，Y=是
     **/
    public static JSONArray records(DynamicObject[] dataArray, List<String> selectProps, Map<String, String> selectPropsName, String pkInfoSet, Map<String, String> selectPropsType) {


        logger.info("XXX个人信息：数据：" + dataArray + "字段:" + selectProps + "字段类型：" + selectPropsType + "字段名称：" + selectPropsName);
        JSONArray records = new JSONArray();
        for (DynamicObject item : dataArray) {
            JSONObject record = new JSONObject();
            JSONArray fields = new JSONArray();
            int showorder = 0;
            for (String selectProp : selectProps) {
                logger.info(selectProps + "selectProps517");
                if (selectProp.contains("image")) {
                    continue;
                }
                JSONObject field = new JSONObject();
                Object value = "";
                int dataType = 0;
                try {
                    Object fieldValue = item.get(selectProp);
                    if (fieldValue instanceof DynamicObject) {
                        try {
                            logger.info("执行了534行");
                            //单独对籍贯处理-取详细地址-tdkw_address
                            if ("tdkw_origin".equals(selectProp)) {
                                DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(((DynamicObject) fieldValue).getPkValue(), "tdkw_hbss_admindivision", "id,tdkw_address");
                                value = admindivision.getString("tdkw_address");
                            } else {
                                Object name = ((DynamicObject) fieldValue).get("name");
                                if (name instanceof OrmLocaleValue) {
                                    value = ((OrmLocaleValue) name).getLocaleValue_zh_CN();
                                } else {
                                    value = (String) name;
                                }
                            }
                        } catch (Exception e) {
                            Object pkValue = ((DynamicObject) fieldValue).getPkValue();
                            value = String.valueOf(pkValue);
                        }
                    } else if (fieldValue instanceof DynamicObjectCollection) {
                        DynamicObjectCollection fieldValue_DOC = (DynamicObjectCollection) fieldValue;
                        for (DynamicObject fieldValue_DO : fieldValue_DOC) {
                            try {
                                Object fieldValue_object = fieldValue_DO.getDynamicObject("fbasedataid").get("name");
                                if (fieldValue_object instanceof OrmLocaleValue) {
                                    if (StringUtils.isBlank((String) value)) {
                                        value = ((OrmLocaleValue) fieldValue_object).getLocaleValue_zh_CN();
                                    } else {
                                        value = value + ";" + ((OrmLocaleValue) fieldValue_object).getLocaleValue_zh_CN();
                                    }

                                } else {
                                    if (StringUtils.isBlank((String) value)) {
                                        value = (String) fieldValue_object;
                                    } else {
                                        value = value + ";" + (String) fieldValue_object;
                                    }
                                }
                            } catch (Exception e) {
                                Object pkValue = ((DynamicObject) fieldValue_DO).getPkValue();
                                if (StringUtils.isBlank((String) value)) {
                                    value = String.valueOf(pkValue);
                                } else {
                                    value = value + ";" + String.valueOf(pkValue);
                                }
                            }
                        }
                    } else if (fieldValue instanceof Date) {
                        dataType = 20;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        value = sdf.format((Date) fieldValue);
                        // 解决bug-#90209 移动端领导查询的人员简历，结束日期2999-12-31时未显示为至今
                        //  http://ones.xxx.com/project/#/team/JbjqrWit/task/Fqvxbmqhokp3y9vN
                        if (selectProp.equals("enddate") && item.getDynamicObjectType().getName().equals("hrpi_empposorgrel") && "2999-12-31".equals(value)) {
                            value = "至今";
                            dataType = 0;
                        }
                    } else if (fieldValue instanceof OrmLocaleValue) {
                        value = ((OrmLocaleValue) fieldValue).getLocaleValue_zh_CN();
                        if (Objects.isNull(value)) {
                            //GLANG是通用语言，多语言字段当前环境语言为空时，显示通用语言
                            value = ((OrmLocaleValue) fieldValue).get("GLang");
                        }
                    } else {
                        logger.info("585selectProp" + selectProp);
                        if (StringUtils.equals(selectProp, "tdkw_administrative")) {
                            logger.info("对出生地/省市区重新赋值587" + fieldValue);
                            DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(fieldValue, "bd_admindivision", "id,fullname");
                            value = admindivision.getString("fullname");
                            if(ObjectUtils.isNotEmpty(value)){
                                value = ((String) value).replaceAll("_","");
                            }
                        } else {
                            value = fieldValue;
                        }
                    }

                    //下拉框判断，如果数据是下拉框，返回下拉框值对应标题
                    if (StringUtils.equals(selectPropsType.get(selectProp), "3")) {
                        value = getDownListTitle(item.getDynamicObjectType().getName(), selectProp, (String) fieldValue);
                    }

                } catch (Exception e) {
                    logger.error("XXX个人信息：获取字段错误：" + e.getMessage());
                }

                if (value == null) {
                    value = "";
                }
                if (value instanceof String) {
                    if (((String) value).indexOf("2999") != -1) {
                        value = "至今";
                    }
                }

                logger.info("selectProp:" + selectProp + " linhao value:" + value + "  selectPropsName.get(selectProp))" + selectPropsName.get(selectProp));
                if ("结束日期".equals(selectPropsName.get(selectProp)) || "enddate".equals(selectProp)) {
                    logger.info("linhao value:" + value);
                    if (value instanceof String) {
                        if (((String) value).indexOf("2999") != -1) {
                            value = "至今";
                        }
                    } else if (value instanceof Date) {
                        if (((String) value).indexOf("2999") != -1) {
                            value = "至今";
                        }
                    }
                }
                //信息组字段隐藏
                boolean hideOrNot = fieldHiding(selectProp, item.getDataEntityType().getName());
                if (hideOrNot) {
                    continue;
                }

                if ("tdkw_newjointime".equals(selectProp)) {
                    //集团司龄保留1位小数
                    if (value instanceof BigDecimal) {
                        value = ((BigDecimal) value).setScale(1);
                    }
                }

                field.put("showValue", value);
                field.put("value", value);
                field.put("pkInfoSet", pkInfoSet);
                field.put("code", selectProp);
                field.put("name", selectPropsName.get(selectProp));
                field.put("showFlag", "Y");
                field.put("canEditFlag", "N");
                field.put("checkFlag", "N");
                field.put("showListFlag", "Y");
                field.put("mustFlag", "N");
                field.put("showOrder", showorder++);
                field.put("maxLength", 50);
                field.put("dataType", dataType);
                field.put("refModel", "");
                field.put("alterFlag", "N");
                fields.add(field);
            }

            record.put("fields", fields);
            record.put("recordId", item.get("id"));
            records.add(record);
        }
        return records;
    }


    /**
     * 数据来源多张表情况下处理
     *
     * @param records
     * @param dataArray
     * @param selectProps
     * @param selectPropsName
     * @param pkInfoSet
     */
    public static void recordsMany(Map<Integer, JSONArray> records, DynamicObject[] dataArray, List<String> selectProps, Map<String, String> selectPropsName, String pkInfoSet, Map<String, String> selectPropsType) {
        logger.info("XXX个人信息：数据：" + dataArray + "字段:" + selectProps + "字段类型：" + selectPropsType + "字段名称：" + selectPropsName);
        int showOrder = 0;
        int index = 0;
        for (DynamicObject item : dataArray) {
            JSONArray fields = new JSONArray();
            for (String selectProp : selectProps) {
                logger.info(selectProps + "selectProps663");
                if (selectProp.contains("image")) {
                    continue;
                }
                JSONObject field = new JSONObject();
                Object value = "";
                int dataType = 0;
                try {
                    Object fieldValue = item.get(selectProp);
                    if ("tdkw_schooltype".equals(selectProp)) {
                        fieldValue = item.get("graduateschool");
                    }
                    if (fieldValue instanceof DynamicObject) {
                        DynamicObject fieldValueDO = (DynamicObject) fieldValue;
                        try {
                            logger.info("执行了679行");
                            //单独对籍贯处理-取详细地址-tdkw_address
                            if ("tdkw_origin".equals(selectProp)) {
                                DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(fieldValueDO.getPkValue(), "tdkw_hbss_admindivision", "id,tdkw_address");
                                value = admindivision.getString("tdkw_address");
                            } else if ("tdkw_schooltype".equals(selectProp)) {
                                DynamicObjectCollection collegecharact = fieldValueDO.getDynamicObjectCollection("collegecharact");
                                for (DynamicObject dynamicObject : collegecharact) {
                                    value += dynamicObject.getDynamicObject(1).getString("name");
                                    if (dynamicObject != collegecharact.get(collegecharact.size() - 1)) {
                                        value += ",";
                                    }
                                }
                            } else {
                                fieldValueDO = BusinessDataServiceHelper.loadSingle(fieldValueDO.getPkValue(), fieldValueDO.getDataEntityType().getName(), "name");
                                Object name = fieldValueDO.get("name");
                                if (name instanceof OrmLocaleValue) {
                                    value = ((OrmLocaleValue) name).getLocaleValue_zh_CN();
                                } else {
                                    value = (String) name;
                                }
                            }

                        } catch (Exception e) {
                            logger.info("异常693行" + selectProp);
                            Object pkValue = fieldValueDO.getPkValue();
                            value = String.valueOf(pkValue);
                        }
                    } else if (fieldValue instanceof DynamicObjectCollection) {
                        DynamicObjectCollection fieldValue_DOC = (DynamicObjectCollection) fieldValue;
                        for (DynamicObject fieldValue_DO : fieldValue_DOC) {
                            try {
                                Object fieldValue_object = fieldValue_DO.getDynamicObject("fbasedataid").get("name");
                                if (fieldValue_object instanceof OrmLocaleValue) {
                                    if (StringUtils.isBlank((String) value)) {
                                        value = ((OrmLocaleValue) fieldValue_object).getLocaleValue_zh_CN();
                                    } else {
                                        value = value + ";" + ((OrmLocaleValue) fieldValue_object).getLocaleValue_zh_CN();
                                    }

                                } else {
                                    if (StringUtils.isBlank((String) value)) {
                                        value = (String) fieldValue_object;
                                    } else {
                                        value = value + ";" + (String) fieldValue_object;
                                    }
                                }
                            } catch (Exception e) {
                                Object pkValue = ((DynamicObject) fieldValue_DO).getPkValue();
                                if (StringUtils.isBlank((String) value)) {
                                    value = String.valueOf(pkValue);
                                } else {
                                    value = value + ";" + String.valueOf(pkValue);
                                }
                            }
                        }
                    } else if (fieldValue instanceof Date) {
                        dataType = 20;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        value = sdf.format((Date) fieldValue);
                        if (((String) value).indexOf("2999") != -1) {
                            value = "至今";
                        }
                    } else if (fieldValue instanceof OrmLocaleValue) {
                        value = ((OrmLocaleValue) fieldValue).getLocaleValue_zh_CN();
                    } else {
                        logger.info("777行+" + selectProp);
                        if (StringUtils.equals(selectProp, "tdkw_administrative")) {
                            logger.info("对出生地/省市区重新赋值587" + fieldValue);
                            DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(fieldValue, "bd_admindivision", "id,fullname");
                            value = admindivision.getString("fullname");
                            if(ObjectUtils.isNotEmpty(value)){
                                value = ((String) value).replaceAll("_","");
                            }
                        } else if ("tdkw_firstdegree".equals(selectProp) || "tdkw_highestdegree".equals(selectProp) || "tdkw_jobhighesteducation".equals(selectProp)) {
                            if ((Boolean) fieldValue) {
                                value = "是";
                            } else {
                                value = "否";
                            }
                        } else {
                            value = fieldValue;
                        }
                    }

                    //下拉框判断，如果数据是下拉框，返回下拉框值对应标题
                    if (StringUtils.equals(selectPropsType.get(selectProp), "3")) {
                        value = getDownListTitle(item.getDynamicObjectType().getName(), selectProp, (String) fieldValue);
                    }

                } catch (Exception e) {
                    logger.error("XXX个人信息：获取字段错误：" + e.getMessage());
                }

                if (value == null) {
                    value = "";
                }

                if (value instanceof String) {
                    if (((String) value).indexOf("2999") != -1) {
                        value = "至今";
                    }
                }
                logger.info("selectProp:" + selectProp + " linhao value:" + value + "  selectPropsName.get(selectProp))" + selectPropsName.get(selectProp));
                if ("结束日期".equals(selectPropsName.get(selectProp)) || "enddate".equals(selectProp)) {
                    logger.info("linhao value:" + value);
                    if (value instanceof String) {
                        if (((String) value).indexOf("2999") != -1) {
                            value = "至今";
                        }
                    } else if (value instanceof Date) {
                        if (((String) value).indexOf("2999") != -1) {
                            value = "至今";
                        }
                    }
                }
                //信息组字段隐藏
                boolean hideOrNot = fieldHiding(selectProp, item.getDataEntityType().getName());
                if (hideOrNot) {
                    continue;
                }
                field.put("showValue", value);
                field.put("value", value);
                field.put("pkInfoSet", pkInfoSet);
                field.put("code", selectProp);
                field.put("name", selectPropsName.get(selectProp));
                field.put("showFlag", "Y");
                field.put("canEditFlag", "N");
                field.put("checkFlag", "N");
                field.put("showListFlag", "Y");
                field.put("mustFlag", "N");
                field.put("showOrder", showOrder++);
                field.put("maxLength", 50);
                field.put("dataType", dataType);
                field.put("refModel", "");
                field.put("alterFlag", "N");
                fields.add(field);
            }

            if (records.containsKey(index)) {
                JSONArray sumFields = records.get(index);
                sumFields.addAll(fields);
                records.put(index++, sumFields);
            } else {
                records.put(index++, fields);
            }
        }
        //   return records;
    }

    /**
     * @author xxx
     * @description 信息组字段隐藏
     * @date 2023/11/15
     */
    public static boolean fieldHiding(String fieldName, String entityName) {
        //教育经历-证书类型，附件，证书编号不显示
        if (("certtype".equals(fieldName) || "number".equals(fieldName) || "attachmentpanelap_std".equals(fieldName)) && "hrpi_pereduexpcert".equals(entityName)) {
            logger.info("教育经历：entityName：" + entityName + "，field：" + fieldName);
            return true;
        }
        //职业技能鉴定书-职业技能鉴定证书不显示
        else if ("tdkw_skilltesting".equals(fieldName) && "tdkw_hrpi_skillidentify".equals(entityName)) {
            logger.info("职业技能鉴定书：entityName：" + entityName + "，field：" + fieldName);
            return true;
        }
        //职称信息-职称证书不显示
        else if ("attachmentpanelap_std".equals(fieldName) && "hrpi_perprotitle".equals(entityName)) {
            logger.info("职称信息：entityName：" + entityName + "，field：" + fieldName);
            return true;
        }
        //执（职）业资格-执（职）业资格证书不显示
        else if ("attachmentpanelap_std".equals(fieldName) && "hrpi_perocpqual".equals(entityName)) {
            logger.info("执（职）业资格：entityName：" + entityName + "，field：" + fieldName);
            return true;
        }
        // 任职经历隐藏所属公司
        else if ("company".equals(fieldName) && "hrpi_emporgrelall".equals(entityName)) {
            logger.info("任职经历：entityName：" + entityName + "，field：" + fieldName);
            return true;
        }
        else {
            return false;
        }
    }


    public static Map<String, Object> setCusParams(String personId) {
        Map<String, Object> cusParams = new HashMap<>();
        if (HRStringUtils.isNotEmpty(personId)) {
            QFilter filter = new QFilter("person", QCP.equals,Long.parseLong(personId) );
            filter.and("datastatus", QCP.equals, "1");
            filter.and("businessstatus", QCP.equals, "1");
            filter.and("iscurrentversion", QCP.equals, "1");
            DynamicObject empList = BusinessDataServiceHelper.loadSingle("hrpi_employee", "id,person,laborreltype.number", filter.toArray());
            List<Long> empIds = Collections.singletonList(empList.getLong("id"));
            DynamicObject[] erFileDys = listPrimaryErmanfFileByEmployeeIds(empIds);
            DynamicObject erFileDy = new DynamicObject();
            if (erFileDys.length > 0) {
                erFileDy = erFileDys[0];
            }
            cusParams.put("person", erFileDy.getLong("person.id"));
            cusParams.put("employee", erFileDy.getLong("employee.id"));
            cusParams.put("depemp", erFileDy.getLong("depemp.id"));
            cusParams.put("cmpemp", erFileDy.getLong("cmpemp.id"));
            cusParams.put("empentrel", erFileDy.getLong("empentrel.id"));
            cusParams.put("empposrel", erFileDy.getLong("empposrel.id"));
            cusParams.put("name", erFileDy.getString("name"));
            cusParams.put("erfileid", erFileDy.getLong("id"));
        }
        return cusParams;
    }


    /**
     * 获取过滤信息 * * @param entry 信息组（key:附表名，value：字段列表） * @param values formShowParameter的参数值 * @return QFilter
     */
    protected static QFilter getQFilter(Map.Entry<String, List<Map<String, Object>>> entry, Map<String, Object> values) {
        QFilter qFilter = PersonModelUtil.getQFilter(entry.getKey(), values);
        if (qFilter != null) {
            qFilter.and(HspmCommonConstants.IS_CURRENTVERSION, QCP.equals, Boolean.TRUE);
            if (values.get(MobileDrawConstants.FILTER_PARAM) != null) {
                String filterP = values.get(MobileDrawConstants.FILTER_PARAM).toString();
                List<QFilter> filters = (List<QFilter>) SerializationUtils.fromJsonStringToList(filterP, QFilter.class);
                if (!CollectionUtils.isEmpty(filters)) {
                    for (QFilter filter : filters) {
                        qFilter.and(filter);
                    }
                }
            }
        }

        String pageNumber = entry.getKey();
        if ("hrpi_emporgrelall".equals(pageNumber)) {
            if (validate(pageNumber, values, "person")) {
                Long dataId = Long.valueOf(values.get("person").toString());
                //根据人员id查出唯一索引，确保唯一性
                QFilter filter1 = new QFilter("id", QCP.equals, dataId);
                filter1.and("iscurrentversion", QCP.equals, Boolean.TRUE);
                filter1.and("initstatus", QCP.equals, "2");
                filter1.and("datastatus", QCP.equals, "1");
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hrpi_person", "personindexid,id,name", filter1.toArray());
                Long personindexId = dynamicObject.getLong("personindexid");

                logger.info("自然人唯一索引：" + personindexId);
                QFilter qFilter1 = new QFilter("personindexid", QCP.equals, personindexId);
                qFilter1.and("iscurrentversion", QCP.equals, Boolean.TRUE);
                qFilter1.and("initstatus", QCP.equals, "2");
                qFilter1.and("datastatus", QCP.equals, "1");
                DynamicObjectCollection hrpiPerson = QueryServiceHelper.query("hrpi_person", "personindexid,id,name", qFilter1.toArray());

                List<Long> personIds = hrpiPerson.stream().map(person -> person.getLong("id")).collect(Collectors.toList());
                QFilter filter = new QFilter("person", "in", personIds);
                filter.and(new QFilter("iscurrentversion", "=", true));
                filter.and("initstatus", QCP.equals, "2");
                // filter.and("datastatus", QCP.not_equals, "-1");
                filter.and("datastatus", QCP.equals, "1");

//                filter.and(new QFilter("tdkw_changereason.number", QCP.not_equals, "XY00017").or("tdkw_changereason.id", QCP.is_null, null));

                logger.info("linhaofilter:" + filter);
                return filter;
            } else {
                return null;
            }
        }
        // 社会团体过滤条件
        if ("tdkw_hrpi_social_group".equals(pageNumber)) {
            qFilter.and("iscurrentversion", QCP.equals, "1");
            qFilter.and("datastatus", QCP.equals, "1");
        }
        return qFilter;
    }


    /**
     * 获取档案多视图方案Id
     *
     * @param personId 人员id
     * @return
     */
    public static ConfigIdTuple<Boolean, String, Long> getCnfId(Long personId) {
        String message = "";
        boolean result = Boolean.FALSE;
        HRPIPersonService hrpiPersonService = new HRPIPersonService();
        //  userId = RequestContext.get().getCurrUserId();
        //根据用户id获取人员id、档案id等数据
        // Map<String, Object> resultPerson = hrpiPersonService.getPersonModelIdByUserId(userId);
        Long cnfId = 0L;
        if (personId != 0L) {
            QFilter filter = new QFilter("person", QCP.equals, personId);
            filter.and("datastatus", QCP.equals, "1");
            filter.and("businessstatus", QCP.equals, "1");
            filter.and("iscurrentversion", QCP.equals, "1");
            DynamicObject empList = BusinessDataServiceHelper.loadSingle("hrpi_employee", "id,person,laborreltype.number", filter.toArray());
            List<Long> empIds = Collections.singletonList(empList.getLong("id"));
            DynamicObject[] erFileDys = listPrimaryErmanfFileByEmployeeIds(empIds);
            DynamicObject erFileDy = new DynamicObject();
            if (erFileDys.length > 0) {
                erFileDy = erFileDys[0];
            }
            if (erFileDy == null) {
                message = ("人员档案为空");
                result = Boolean.TRUE;
            } else {
                Tuple<Boolean, Long> tuple = AttacheHandlerService.getInstance().handleRuleEngine(null, erFileDy.getLong("id"), erFileDy, "hspm" + ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), (Map) null, false);
                cnfId = (Long) tuple.item2;
                Boolean getRule = (Boolean) tuple.item1;
                if (!getRule) {
                    result = Boolean.FALSE;
                    message = "没有匹配的显示默认方案，请联系管理员调整业务规则!";
                }
            }
        } else {
            message = "人员Id在系统不存在，请联系管理员处理！";
            result = Boolean.TRUE;
        }

        ConfigIdTuple<Boolean, String, Long> configId = new ConfigIdTuple<>(result, message, cnfId);
        return configId;
    }

    /**
     * 根据企业人id获取人员主档案信息，包括在职和离职人员的主档案
     *
     * @param employeeIds 企业人
     * @return
     */
    public static DynamicObject[] listPrimaryErmanfFileByEmployeeIds(List<Long> employeeIds) {
        //业务状态生效中
        QFilter bussQf = new QFilter("businessstatus", QCP.equals, HRBaseConstants.STR_ONE);
        //主任职
        QFilter primaryStatusQf = new QFilter("empposrel.isprimary", QCP.equals, HRBaseConstants.STR_ONE);
        //personId
        QFilter empQf = new QFilter("employee.id", QCP.in, employeeIds);
        //当前版本
        QFilter currentQf = new QFilter("iscurrentversion", QCP.equals, HRBaseConstants.STR_ONE);
        //生效中
        QFilter dataStatusQf = new QFilter("datastatus", QCP.equals, HRBaseConstants.STR_ONE);
        QFilter[] qFilters = {bussQf, empQf, primaryStatusQf, currentQf, dataStatusQf, QFilterUtil.getInitStatusFinish()};
        return HSPM_ERMANFILE.loadDynamicObjectArray(qFilters);
    }

    /**
     * 获取有权限的页签
     *
     * @param currUserId
     */
    public static JSONArray getTab(Long currUserId) {
        Boolean target = false;
//        List<Long> personIds  = new ArrayList<>();
//        personIds.add(Long.valueOf(personId));
//        List<Long> userIds = SendMessageUtil.getBosUserIdsByPersonIds(personIds);
        List<UserRoleInfo> userRoleInfos = HRRoleAndPersonUtils.getUserRole(currUserId, "tdkw_performancequery");
        if (userRoleInfos.size() > 0) {
            target = true;
        }
        JSONArray resultData = new JSONArray();
        JSONObject jsobTab1 = new JSONObject();
        jsobTab1.put("tabName", "个人信息");
        jsobTab1.put("tabCode", "RESUME-001");
        jsobTab1.put("isPower", true);
        resultData.add(jsobTab1);
        JSONObject jsobTab2 = new JSONObject();
        jsobTab2.put("tabName", "绩效信息");
        jsobTab2.put("tabCode", "RESUME-002");
        jsobTab2.put("isPower", target);
        resultData.add(jsobTab2);
        JSONObject jsobTab3 = new JSONObject();
        jsobTab3.put("tabName", "奖励情况");
        jsobTab3.put("tabCode", "RESUME-003");
        jsobTab3.put("isPower", true);
        resultData.add(jsobTab3);
        JSONObject jsobTab4 = new JSONObject();
        jsobTab4.put("tabName", "培训信息");
        jsobTab4.put("tabCode", "RESUME-004");
        jsobTab4.put("isPower", true);
        resultData.add(jsobTab4);
        return resultData;
    }

    /**
     * 苍穹系统映射XXX前端字段类型
     * 苍穹：
     * MuliLangTextProp-1   TextProp-2  ComboProp-3   DateProp-4  BooleanProp-5
     * DecimalProp-6    LongProp-7   IntegerProp-8   BasedataProp-9  CreaterProp-10
     * ModifierProp-11   PictureProp-12  BillStatusProp-13  UserProp-14   CityProp-15
     * AdminDivisionProp-16   MainOrgProp-17  AttachmentProp-18  TextAreaProp-19  CurrencyProp-20
     * OrgProp-21  QueryProp-22  I18NNAMEPROP-23  ADDRESSPROP-24  MULBASEDATAPROP-25
     * MULQUERYPROP-26  AmountProp-27
     * XXX：
     * 0=字符，1=整数，2=数量，3=日期，101=开始日期，102=结束日期，20=日期(无时区)，
     * 4=逻辑，5=参照，6=下拉，8=时间，9=大文本，10=图片，14=邮件地址，15=时间戳，
     * 16=自定义项，17=多语言，18=金额，100=日期公式
     * <p>
     * 备注：4逻辑对应苍穹复选框
     *
     * @param selfDataType
     * @return
     */
    public static int getDataType(String selfDataType) {
        int dataType = 0;
        switch (selfDataType) {
            case "3":
                dataType = 0;
                break;
            case "4":
                dataType = 20;
                break;
            case "5":
                dataType = 4;
                break;
            default:
                dataType = 0;
                break;
        }

        return dataType;
    }

    /**
     * @param fromMark  表单标识
     * @param fieldMark 字段标识
     * @param value     数据
     * @return valueTitle  返回下拉框对应值
     */
    public static String getDownListTitle(String fromMark, String fieldMark, String value) {

        String valueTitle = "";
        DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject(fromMark);
        ComboProp iDataEntityProperty = (ComboProp) dynamicObject.getDataEntityType().getProperties().get(fieldMark);
        HashMap<String, String> mapSetResult = new HashMap<>(16);

        for (ValueMapItem comboItem : iDataEntityProperty.getComboItems()) {
            String key = comboItem.getValue();
            String title = comboItem.getName().getLocaleValue_zh_CN();
            mapSetResult.put(key, title);
        }
        if (mapSetResult.containsKey(value)) {
            valueTitle = mapSetResult.get(value);
        }

        return valueTitle;
    }

    /**
     * 按照配置的顺序排序   封装字段模板
     *
     * @param fieldTemplates
     */
    public static JSONArray fieldTemplatesSort(JSONArray fieldTemplates, Map<Integer, String> fieldsOrder) {
        JSONArray fieldTemplatesSort = new JSONArray();

        for (int order : fieldsOrder.keySet()) {
            for (Object fieldTemplate : fieldTemplates) {
                JSONObject fieldTemplate1 = (JSONObject) fieldTemplate;
                String name = (String) fieldTemplate1.get("code");
                if (StringUtils.equals(fieldsOrder.get(order), name)) {
                    fieldTemplate1.put("showOrder", order);
                    fieldTemplatesSort.add(fieldTemplate1);
                }
            }
        }

        return fieldTemplatesSort;
    }

    /**
     * 按照配置的顺序排序   封装数据
     *
     * @param records
     * @return
     */
    public static JSONArray recordsOrder(JSONArray records, Map<Integer, String> fieldsOrder) {
        JSONArray recordsOrder = new JSONArray();
        for (Object record : records) {
            JSONObject record1 = (JSONObject) record;
            JSONArray fields = (JSONArray) record1.get("fields");
            JSONArray fieldsOrder_js = new JSONArray();
            for (int order : fieldsOrder.keySet()) {
                for (Object field : fields) {
                    JSONObject field1 = (JSONObject) field;
                    String code = (String) field1.get("code");
                    if (StringUtils.equals(fieldsOrder.get(order), code)) {
                        field1.put("showOrder", order);
                        fieldsOrder_js.add(field1);
                    }
                }
            }
            record1.put("fields", fieldsOrder_js);
            recordsOrder.add(record1);
        }

        return recordsOrder;
    }


    /**
     * 苍穹配置的字段，根据此字段顺序排序
     *
     * @param fields
     */
    public static Map<Integer, String> fieldsOrder(List<Map<String, Object>> fields) {
        Map<Integer, String> fieldsOrder = new HashMap<>();
        int order = 0;
        for (Map<String, Object> field : fields) {
            String fieldKey = (String) field.get("number");
            fieldsOrder.put(order++, fieldKey);
        }
        return fieldsOrder;
    }

    private static boolean validate(String pageNumber, Map<String, Object> values, String key) {
        return validateData(values, key) && PropertyHelper.existProperty(pageNumber, key);
    }

    private static boolean validateData(Map<String, Object> values, String key) {
        return values.get(key) != null && Long.parseLong(values.get(key).toString()) > 0L;
    }


    public static ConfigIdTuple<Boolean, String, Long> getLeaderCnfId(Long personId) {
        String message = "";
        boolean result = Boolean.FALSE;
        Long cnfId = 0L;
        if (personId != 0L) {
            QFilter filter = new QFilter("person", QCP.equals, personId);
            filter.and("datastatus", QCP.equals, "1");
            filter.and("businessstatus", QCP.equals, "1");
            filter.and("iscurrentversion", QCP.equals, "1");
            DynamicObject empList = BusinessDataServiceHelper.loadSingle("hrpi_employee",
                    "id,person,laborreltype.number", filter.toArray()
            );
            List<Long> empIds = Collections.singletonList(empList.getLong("id"));
            DynamicObject[] erFileDys = listPrimaryErmanfFileByEmployeeIds(empIds);
            DynamicObject erFileDy = new DynamicObject();
            if (erFileDys.length > 0) {
                erFileDy = erFileDys[0];
            }
            if (erFileDy == null) {
                message = ("人员档案为空");
                result = Boolean.TRUE;
            } else {
                Tuple<Boolean, Long> tuple = handleRuleEngine(erFileDy, "hspm" + ClientTypeEnum.EMPLOYEE_MOBILE.getCode());
                cnfId = tuple.item2;
                Boolean getRule = tuple.item1;
                if (!getRule) {
                    message = "没有匹配的显示默认方案，请联系管理员调整业务规则!";
                }
            }
        } else {
            message = "人员Id在系统不存在，请联系管理员处理！";
            result = Boolean.TRUE;
        }

        return new ConfigIdTuple<>(result, message, cnfId);
    }

    private static Tuple<Boolean, Long> handleRuleEngine(DynamicObject erFileDy, String relFormId) {
        String sceneNumber = "tdkw_leader_profile";
        Map<String, Object> responseMap = invokeRuleEngine(erFileDy, relFormId, sceneNumber);
        if (responseMap.size() != 0 && "200".equals(responseMap.get("responseCode"))) {
            return getCnfId(responseMap);
        } else {
            return Tuple.create(Boolean.FALSE, 0L);
        }
    }

    private static Map<String, Object> invokeRuleEngine(DynamicObject erFileDy, String source, String sceneNumber) {
        Map<String, Object> requestMap = new HashMap<>(16);
        requestMap.put("bizApp", "hspm");
        requestMap.put("sceneNumber", sceneNumber);
        DynamicObject dynamicObject = erFileDy.getDynamicObject("org");
        if (dynamicObject != null) {
            requestMap.put("buNumber", dynamicObject.getString("number"));
//            logger.info(MessageFormat.format("invokeRuleEngineOrg==={}", dynamicObject.getString("number")));
        } else {
            logger.info("invokeRuleEngineOrgEmpty");
        }

        Map<String, Object> paramMap = new HashMap<>(16);
        requestMap.put("inputParams", paramMap);
        Map<String, Object> responseMap = new HashMap<>(16);
        try {
            DynamicObject depEmp;
            DynamicObject empEntreDy;
            depEmp = erFileDy.getDynamicObject("depemp");
            empEntreDy = erFileDy.getDynamicObject("empentrel");
            if (!"hspmmobile".equals(source)) {
                paramMap.put("ermanfile", erFileDy);
            }

            paramMap.put("depemp", depEmp);
            paramMap.put("empentrel", empEntreDy);
            long cmpId = erFileDy.getLong("cmpemp.id");
            DynamicObject cmpEmp = getDynamicInfo("hrpi_managingscope", cmpId, "cmpemp");
            paramMap.put("managingscope", cmpEmp);
            long employId = erFileDy.getLong("employee.id");
            DynamicObject jobRel = getDynamicInfo("hrpi_empjobrel", employId, "employee");
            paramMap.put("empjobrel", jobRel);
            responseMap = HRMServiceHelper.invokeHRMPService("brm", "IBRMRuleService",
                    "callRuleEngine", requestMap);
        } catch (Exception e) {
            logger.error("ERManFileListPluginInvokeRuleEngineFail", e);
            logger.error("ERManFileListPluginInvokeRuleEngineFailMessage", responseMap);
        }
//        logger.info(MessageFormat.format("ERManFileListPluginInvokeRuleEngineResult==={}==={}==={}",
//                responseMap, requestMap.get("buNumber"), source));
        return responseMap;
    }

    private static DynamicObject getDynamicInfo(String entityName, Long filterId, String filterField) {
        if (filterId == 0L) {
            return null;
        } else {
//            logger.info(MessageFormat.format("ERManFileListGetDynamicInfo==={}", filterId));
            HRBaseServiceHelper serviceHelper = new HRBaseServiceHelper(entityName);
            QFilter qFilter = new QFilter(filterField, QCP.equals, filterId);
            QFilter cuFilter = new QFilter("iscurrentversion", QCP.equals, "1");
            QFilter[] qFilters = new QFilter[]{qFilter, cuFilter};
            return serviceHelper.loadDynamicObject(qFilters);
        }
    }

    private static Tuple<Boolean, Long> getCnfId(Map<String, Object> responseMap) {
        SceneResult sceneResult = RuleEngineResponseUtils.transferToSceneResult(responseMap);
        List<PolicyResult> policyResults = sceneResult.getPolicyResults();
        if (policyResults.size() == 0) {
            HRBaseServiceHelper hrBaseServiceHelper = new HRBaseServiceHelper("hspm_multiviewconfig");
            QFilter qFilter = new QFilter("number", QCP.equals, "2010_S");
            QFilter sFilter = new QFilter("employee", QCP.equals, "1");
            DynamicObject dy = hrBaseServiceHelper.queryOne("id", new QFilter[]{qFilter, sFilter});
            return dy != null ? Tuple.create(Boolean.TRUE, dy.getLong("id")) : Tuple.create(Boolean.FALSE, 0L);
        } else {
            for (PolicyResult policyResult : policyResults) {
                Map<String, Object> rosterResults = policyResult.getRosterResults();
                if (rosterResults != null && rosterResults.size() > 0) {
                    DynamicObject dynamicObject = (DynamicObject) rosterResults.get("result");
                    if (dynamicObject != null) {
                        return Tuple.create(Boolean.TRUE, dynamicObject.getLong("id"));
                    }
                }

                List<RuleResult> ruleResults = policyResult.getRuleResults();
                if (ruleResults != null && ruleResults.size() != 0) {

                    for (RuleResult ruleResult : ruleResults) {
                        long cnfId = handlerRule(ruleResult.getMatchResults());
                        if (cnfId != 0L) {
                            return Tuple.create(Boolean.TRUE, cnfId);
                        }
                    }
                } else {
                    long cnfId = handlerRule(policyResult.getDefaultResults());
                    if (cnfId != 0L) {
                        return Tuple.create(Boolean.TRUE, cnfId);
                    }
                }
            }
            return Tuple.create(Boolean.FALSE, 0L);
        }
    }

    private static long handlerRule(Map<String, Object> matchResults) {
        DynamicObject dynamicObject;
        if (matchResults != null && matchResults.size() > 0) {
            dynamicObject = (DynamicObject)matchResults.get("result");
            if (dynamicObject != null) {
                return dynamicObject.getLong("id");
            }
        }

        return 0L;
    }

}
