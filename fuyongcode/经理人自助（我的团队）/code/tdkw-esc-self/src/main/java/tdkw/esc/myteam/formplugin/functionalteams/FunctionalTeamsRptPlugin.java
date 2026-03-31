package tdkw.esc.myteam.formplugin.functionalteams;

import com.alibaba.fastjson.JSON;
import kd.bos.algo.*;
import kd.bos.algo.input.CollectionInput;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import tdkw.hrmp.hrobs.common.myteam.common.bean.TeamRequestVO;
import tdkw.hrmp.hrobs.common.myteam.common.orgscopeutil.GetTeamOrgScopeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author guokairong
 * @Date 2023/8/14 11:16
 */
public class FunctionalTeamsRptPlugin extends AbstractReportListDataPlugin {
    /**
     * 报表的自定义字段
     * 公司名称、岗位、人员编码、姓名、缺少信息
     */
    private static String[] FIELDS = new String[]{"tdkw_person", "tdkw_company", "tdkw_department", "tdkw_mainpositions", "tdkw_age", "tdkw_entservicelen", "tdkw_servicelen", "tdkw_educate", "tdkw_schoolname", "tdkw_specialityname", "tdkw_schoolrecord"};

    /**
     * 报表列表上所有字段对应的数据类型,必须和FIELDS中的元素一一对应
     * 公司名称、岗位、人员编码、姓名、缺少信息
     */
    private static final DataType[] DATATYPES = {DataType.LongType, DataType.LongType, DataType.LongType, DataType.LongType, DataType.IntegerType, DataType.BigDecimalType, DataType.BigDecimalType, DataType.LongType, DataType.LongType, DataType.StringType, DataType.StringType};
    private static final Log logger = LogFactory.getLog(FunctionalTeamsRptPlugin.class);

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        // 创建一个空的DataSet
        Collection<Object[]> coll = new ArrayList<>();

        //将自定义的字段和类型对应插入
        RowMeta rowMeta = RowMetaFactory.createRowMeta(FIELDS, DATATYPES);
        CollectionInput inputs = new CollectionInput(rowMeta, coll);

        //Algo 创建dataSet
        DataSet resultDataSet = Algo.create(this.getClass().getName()).createDataSet(inputs);


        //构建查询参数
        TeamRequestVO teamRequestVO = new TeamRequestVO();
        Map<String, Object> customParam = reportQueryParam.getCustomParam();
        Long teamsId = (Long) customParam.get("teams");
        String name = String.valueOf(customParam.get("name"));
        List<Long> positionList = customParam.get("positionids") == null ? null : (List<Long>) customParam.get("positionids");
        teamRequestVO.setTeam(teamsId);
        teamRequestVO.setPositionList(positionList);
        //所属组织
        DynamicObject organizationFilter = reportQueryParam.getFilter().getDynamicObject("tdkw_companyfilter");
        //任职类型
        DynamicObjectCollection postypeFilter = reportQueryParam.getFilter().getDynamicObjectCollection("tdkw_postype");
        // 学校
//        DynamicObject schoolFilter = reportQueryParam.getFilter().getDynamicObject("tdkw_school");
        String schoolNameFilter = reportQueryParam.getFilter().getString("tdkw_schooolname");
        //姓名
        String personName = reportQueryParam.getFilter().getString("tdkw_namefilter");
        if (StringUtils.isNotEmpty(name) && !"null".equals(name)) {
            personName = name;
        }
        //专业
        String speciality = reportQueryParam.getFilter().getString("tdkw_speciality");
        //是否兼职
        String isPartTime = reportQueryParam.getFilter().getString("tdkw_isparttime");

        //年龄
        int ageFilterStart = reportQueryParam.getFilter().getInt("tdkw_agestart");
        int ageFilterEnd = reportQueryParam.getFilter().getInt("tdkw_ageend");

        //司龄
        BigDecimal seniorityFilterStart = reportQueryParam.getFilter().getBigDecimal("tdkw_workagestart");
        BigDecimal seniorityFilterEnd = reportQueryParam.getFilter().getBigDecimal("tdkw_workageend");

        //工龄
        BigDecimal societyAgeFilterStart = reportQueryParam.getFilter().getBigDecimal("tdkw_senioritystart");
        BigDecimal societyAgeFilterEnd = reportQueryParam.getFilter().getBigDecimal("tdkw_seniorityend");

        //学历
        DynamicObject educationFilter = reportQueryParam.getFilter().getDynamicObject("tdkw_education");

        if (!ObjectUtils.isEmpty(organizationFilter)) {
            //过滤任职经历的部门
            teamRequestVO.setAdminorg(organizationFilter.getLong("id"));
        }else {
            // 2025-10-31 增加报表过滤，当前人组织
            //获取当前登录人员所属组织
            List<Long> userIds = new ArrayList<>(1);
            userIds.add(UserServiceHelper.getCurrentUserId());
            // 获取当前登录人员所属组织
            Long adminOrg = UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId());
            teamRequestVO.setAdminorg(adminOrg);
            // 获取当前登录人员所属部门

            QFilter userFilter = new QFilter("user", "in", userIds);
            QFilter enableFilter = new QFilter("enable", QCP.equals, "1");

            DynamicObject[] persons = BusinessDataServiceHelper.load("hrpi_personuserrel", "id,person,user", new QFilter[]{userFilter,enableFilter});
            if (persons.length > 0 && persons != null){
                Long personId = persons[0].getLong("person");
                // 人员任职经历
                QFilter qFilter = new QFilter("person.id", QCP.in, personId);
                qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
                qFilter.and("datastatus", QCP.equals, "1");
                qFilter.and("isprimary", QCP.equals, "1");
                qFilter.and("businessstatus", QCP.equals, "1");

                DynamicObject[] emppDynamicObjects= BusinessDataServiceHelper.load("hrpi_empposorgrel", "adminorg", qFilter.toArray());
                if (emppDynamicObjects != null){
                    Long adminorg = emppDynamicObjects[0].getLong("adminorg.id");
                    teamRequestVO.setDepartment(adminorg);
                }
            }



        }
        if (!ObjectUtils.isEmpty(postypeFilter)) {
            //过滤任职类型
            List<Long> postypes= postypeFilter.stream().map(i->i.getLong("id")).collect(Collectors.toList());
            teamRequestVO.setPostype(postypes);
        }
//        if (!ObjectUtils.isEmpty(schoolFilter)) {
//            //过滤学校
//            teamRequestVO.setSchool(schoolFilter.getLong("id"));
//        }
        if (!ObjectUtils.isEmpty(schoolNameFilter)) {
            //过滤学校
            teamRequestVO.setSchoolName(schoolNameFilter);
        }
        if (!ObjectUtils.isEmpty(educationFilter)) {
            //教育经历
            teamRequestVO.setEducation(educationFilter.getLong("id"));
        }
        if (!StringUtils.isEmpty(personName)) {
            //过滤姓名
            teamRequestVO.setName(personName);
        }
        if (!StringUtils.isEmpty(speciality)) {
            //过滤专业
            teamRequestVO.setSpeciality(speciality);
        }
        if (!StringUtils.isEmpty(isPartTime) && "0".equals(isPartTime)) {
            //过滤是否兼职
            teamRequestVO.setIsPartTime(isPartTime);
        }
        if (ageFilterStart != 0) {
            //过滤年龄
            teamRequestVO.setAgeStart(ageFilterStart);
        }
        if (ageFilterEnd != 0) {
            //过滤年龄
            teamRequestVO.setAgeEnd(ageFilterEnd);
        }
        if (seniorityFilterStart.compareTo(new BigDecimal(0)) > 0) {
            //过滤司龄
            teamRequestVO.setSeniorityStart(seniorityFilterStart.doubleValue());
        }
        if (seniorityFilterEnd.compareTo(new BigDecimal(0)) > 0) {
            //过滤司龄
            teamRequestVO.setSeniorityEnd(seniorityFilterEnd.doubleValue());
        }
        if (societyAgeFilterStart.compareTo(new BigDecimal(0)) > 0) {
            //过滤工龄
            teamRequestVO.setSocialWorkAgeStart(societyAgeFilterStart.doubleValue());
        }
        if (societyAgeFilterEnd.compareTo(new BigDecimal(0)) > 0) {
            //过滤工龄
            teamRequestVO.setSocialWorkAgeEnd(societyAgeFilterEnd.doubleValue());
        }
        logger.info("职能团队请求参数"+ JSON.toJSONString(teamRequestVO));
        DataSet CareerExperiences = GetTeamOrgScopeUtil.getResultOfPc(teamRequestVO);
        if (CareerExperiences != null) {
            DataSet copy = CareerExperiences.copy();
            setResultData(CareerExperiences, coll);
            List<Row> careerExpericencList = new ArrayList<>();
            for (Row careerExperience : copy) {
                careerExpericencList.add(careerExperience);
            }
            logger.info("下属列表返回careerExpericencList：" + careerExpericencList.size());
            logger.info("下属列表返回careerExpericencList：" + careerExpericencList);
            //根据人员id去重
            List<Row> rowList = careerExpericencList.stream().collect(Collectors.toMap(map -> map.get("person"), map -> map, (p1, p2) -> p1)).values().stream().collect(Collectors.toList());
            int size = rowList.size();
            logger.info("下属列表返回rowList：" + rowList.size());
            logger.info("下属列表返回rowList：" + rowList);
            if (size > 0) {
                logger.info("下属列表返回rowList>0：");
                double avgAge = 0d;
                double avgWorkAge = 0d;
                for (Row row : rowList) {
                    avgAge += null != row.getDouble("age") ? row.getDouble("age") : 0;
                    avgWorkAge += null != row.getDouble("entservicelen") ? row.getDouble("entservicelen") : 0;
                }
                avgAge = avgAge / size;
                avgWorkAge = avgWorkAge / size;
                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                String avgAgeString = decimalFormat.format(new BigDecimal(String.valueOf(avgAge)));
                String avgWorkAgeString = decimalFormat.format(new BigDecimal(String.valueOf(avgWorkAge)));
                Map<String, Object> returnMap = new HashMap<>();
                returnMap.put("avgAgeString", avgAgeString);
                returnMap.put("avgWorkAgeString", avgWorkAgeString);
                returnMap.put("peopleSize", size);
                logger.info("下属列表返回returnMap：" + returnMap);
                reportQueryParam.setCustomParam(returnMap);
            }
        }

        return resultDataSet;
    }


    /**
     * 构建返回结果集
     *
     * @param dataSet
     * @param coll
     */
    private void setResultData(DataSet dataSet, Collection<Object[]> coll) {
        List<Row> careerExpericencList = new ArrayList<>();
        for (Row row : dataSet) {
            Object[] resultData = new Object[11];
            //姓名
            resultData[0] = row.getString("person");
            //公司
            resultData[1] = row.getString("company");
            //部门
            resultData[2] = row.getString("adminorg");
            //岗位
            resultData[3] = row.getString("position");
            //年龄
            resultData[4] = row.getBigDecimal("age");
            //司龄
            resultData[5] = row.getBigDecimal("entservicelen");
            //工龄
            resultData[6] = row.getBigDecimal("servicelen");
            //学历
            resultData[7] = row.getBigDecimal("education");
            //学校
            resultData[8] = row.getString("graduateschool");
            //专业
            resultData[9] = row.getString("major");
            //学校名称
            resultData[10] = row.getString("schoolname");
            coll.add(resultData);
        }
    }
}
