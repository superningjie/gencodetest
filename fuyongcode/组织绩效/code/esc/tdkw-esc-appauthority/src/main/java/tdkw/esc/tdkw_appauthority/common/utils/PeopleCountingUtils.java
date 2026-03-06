package tdkw.esc.tdkw_appauthority.common.utils;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hrmp.hrpi.business.domian.repository.HRPIEmployeeRepository;
import kd.hrmp.hrpi.business.infrastructure.utils.DateUtil;
import kd.hrmp.hrpi.business.infrastructure.utils.QFilterUtil;
import kd.hrmp.hrpi.common.HRPIValueConstants;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import kd.wtc.wtbs.common.constants.WTCCommonConstants;
import kd.wtc.wtbs.common.util.WTCDateUtils;
import kd.wtc.wtss.business.servicehelper.mobile.MobileHomePageServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import tdkw.esc.tdkw_appauthority.common.hrmp.HRRoleAndPersonUtils;
import tdkw.esc.tdkw_appauthority.common.hrmp.HRUserRoleCacheUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static tdkw.esc.tdkw_appauthority.common.mobie.WorkTypeFilterUtils.filterWorkType;

/**
 * @author XXXX
 * @description 人员统计工具类
 * @date 2023/6/28
 */
public class PeopleCountingUtils {


    public static final HRBaseServiceHelper depempServiceHelper = new HRBaseServiceHelper("hrpi_depemp");

    /**
     * 日志
     */
    private static final Log logger = LogFactory.getLog(PeopleCountingUtils.class);

    /**
     * 控制权限中角色的编码
     */
    private static final String ROLE_ORG = "tdkw_statisticperson";

    /**
     * @description 领导统计-人员统计过滤
     * @author XXXX
     * @date 2023/6/27
     */
    public static List<Long> getPersonsByEndDate(String pkOrg, Date endDate, List<Object> postGradeList, List<Object> psNclList, String role, List<Long> allBelowHROrg, List<Long> persons) {
        // 获取权限信息
        Map<String, Object> adminOrg = getAdminOrg();
        // 如果包含10000L就返回true
        boolean hasAllOrgPerm = (boolean) adminOrg.get("isAdmin");
        // 权限组织
        List<Long> hasPerOrg = (List<Long>) adminOrg.get("adminOrgIds");

        if (null == allBelowHROrg || allBelowHROrg.size() == 0) {
            // 组织是否为空，为空则查询所有组织下的数据
            if (StringUtils.isNotEmpty(pkOrg)) {
                allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(pkOrg);
            } else {
                allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds("100000");
            }

            // 全组织查看权限
            if (!hasAllOrgPerm) {
                // 获取普通组织与权限组织的交集
                allBelowHROrg.retainAll(hasPerOrg);
            }
        }
        logger.info("筛选组织" + allBelowHROrg);
        // 根据组织与日期过滤人员信息
        // DynamicObject[] currentPersonByOrg = getCurrentPersonByOrgs(allBelowHROrg, endDate);
        // //人员ID集合
        // List<Long> personIds = Arrays.stream(currentPersonByOrg).map(object -> (Long) object.get("person.id")).collect(Collectors.toList());
        // //按组织与日期查询人员
        // List<Map<String, Object>> personCountAndChargeInfo = HRPIPersonServiceHelper.getPersonByOrgs(allBelowHROrg, endDate);
        //  // 根据组织过滤后的人员ID集合
        // List<Long> personIds = personCountAndChargeInfo.stream().map(object -> (Long) object.get("person")).collect(Collectors.toList());
        // //组织过滤
        // QFilter filter = new QFilter("empposrel.adminorg", QCP.in, allBelowHROrg);
        // 日期过滤
        // filter.and(new QFilter("empposrel.startdate", "<=", endDate).and("empposrel.enddate", ">=", endDate));
        QFilter filter = new QFilter("1", QCP.in, 1);

        if (null != persons && persons.size() > 0) {
//            personIds.retainAll(persons);
            // 人员过滤
            filter.and("person.id", QCP.in, persons);
        }

//        //人员过滤
//        QFilter filter = new QFilter("person.id", QCP.in, personIds);

        // 岗位层级过滤
        if (null != postGradeList && postGradeList.size() > 0) {
            // 岗位层级过滤
            filter.and("empposrel.tdkw_postlevel", QCP.in, postGradeList);
        }

        // 人员类型过滤
        if (null != psNclList && psNclList.size() > 0) {
            filter.and("empposrel.tdkw_employtype.number", QCP.in, psNclList);
        }

        // 查询人事业务档案
        List<DynamicObject> allErManFileByPerson = getAllErManFileByPerson(filter);

        List<Long> erManFileIds = allErManFileByPerson.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
        // 人员任职经历
        QFilter qFilter = new QFilter("person.id", QCP.in, erManFileIds);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and("isprimary", QCP.equals, "1");
        // 组织过滤
        qFilter.and("adminorg", QCP.in, allBelowHROrg);
        // 日期过滤
        qFilter.and(new QFilter("startdate", "<=", endDate).and("sysenddate", ">=", endDate));
        DynamicObject[] loads = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person.id,startdate,sysenddate,enddate", qFilter.toArray());
        List<DynamicObject> personEmpposorgrel = new ArrayList<>();
        for (DynamicObject d : loads) {
            personEmpposorgrel.add(d);
        }


        // 过滤后的人员id
        return personEmpposorgrel.stream().map(object -> object.getLong("person.id")).collect(Collectors.toList());
    }

    /**
     * @description 领导统计-人员统计过滤
     * @author XXXX
     * @date 2023/6/27
     */
    @SuppressWarnings("unchecked")
    public static List<DynamicObject> getPersonsIds(String pkOrg, Date endDate, List<Object> postGradeList, List<Object> psNclList) {
        // 获取权限信息
        Map<String, Object> adminOrg = getAdminOrg();
        // 如果包含10000L就返回true
        boolean hasAllOrgPerm = (boolean) adminOrg.get("isAdmin");
        // 权限组织
        List<Long> hasPerOrg = (List<Long>) adminOrg.get("adminOrgIds");
        List<Long> allBelowHROrg;

        // 组织是否为空，为空则查询所有组织下的数据
        if (StringUtils.isNotEmpty(pkOrg)) {
            allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(pkOrg);
        } else {
            allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds("100000");
        }

        // 全组织查看权限
        if (!hasAllOrgPerm) {
            // 获取普通组织与权限组织的交集
            allBelowHROrg.retainAll(hasPerOrg);
        }

        QFilter filter = new QFilter("1", QCP.in, 1);
        // 岗位层级过滤
        if (null != postGradeList && postGradeList.size() > 0) {
            filter.and("empposrel.tdkw_postlevel", QCP.in, postGradeList);
        }

        // 人员类型过滤
        if (null != psNclList && psNclList.size() > 0) {
            filter.and("empposrel.tdkw_employtype.number", QCP.in, psNclList);
        }
        // 人事业务档案
        List<Long> erManFileIds = PeopleCountingUtils.getAllErManFileByPersonIds(filter);

        // 人员任职经历
        QFilter qFilter = new QFilter("person.id", QCP.in, erManFileIds);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and("isprimary", QCP.equals, "1");
        if (null != endDate) {
            // 日期过滤
            qFilter.and(new QFilter("startdate", QCP.less_equals, endDate).and("sysenddate", QCP.large_equals, endDate));
        }
        // 组织过滤
        qFilter.and("adminorg.id", QCP.in, allBelowHROrg);
        return QueryServiceHelper.query("hrpi_empposorgrel", "person.id,startdate,sysenddate,tdkw_employtype.id,tdkw_employtype.name,tdkw_employtype.number,tdkw_postlevel", qFilter.toArray());
    }

    /**
     * @description 领导统计-人员统计过滤
     * @author XXXX
     * @date 2023/6/27
     */
    public static List<DynamicObject> getPersonsIds(String pkOrg, Date endDate, List<Object> postGradeList, List<Object> psNclList, String algoKey, String highLevelType) {
        // 获取权限信息
        Map<String, Object> adminOrg = getAdminOrg();
        // 如果包含10000L就返回true
        boolean hasAllOrgPerm = (boolean) adminOrg.get("isAdmin");
        // 权限组织
        List<Long> hasPerOrg = (List<Long>) adminOrg.get("adminOrgIds");
        //
        List<Long> allBelowHROrg;

        // 组织是否为空，为空则查询所有组织下的数据
        if (StringUtils.isNotEmpty(pkOrg)) {
            allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(pkOrg);
        } else {
            allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds("100000");
        }

        // 全组织查看权限
        if (!hasAllOrgPerm) {
            // 获取普通组织与权限组织的交集
            allBelowHROrg.retainAll(hasPerOrg);
        }

        QFilter filter = new QFilter("1", QCP.in, 1);
        // 岗位层级过滤
        if (null != postGradeList && postGradeList.size() > 0) {
            // 岗位层级过滤
            filter.and("empposrel.tdkw_postlevel", QCP.in, postGradeList);
        }

        // 人员类型过滤
        if (null != psNclList && psNclList.size() > 0) {
            filter.and("empposrel.tdkw_employtype.number", QCP.in, psNclList);
        }
        filter.and("businessstatus", QCP.equals, "1");
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("filetype.number", QCP.in, new String[]{"1010_S", "1050_S", "1060_S", "1070_S", "1110_S", "1190_S"});
        filter.and("empposrel.datastatus", QCP.equals, "1");
        filter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);

        // 人事业务档案
        DataSet erManFile = ORM.create().queryDataSet(algoKey, "hspm_ermanfile", "person,person.id,empposrel.tdkw_postlevel as tdkw_postlevel" +
                ",pernontsprop.gender as gender.id,pernontsprop.gender.name as gender.name,pernontsprop.gender.number as gender.number", filter.toArray());
        DynamicObjectCollection erManFilePerson = ORM.create().toPlainDynamicObjectCollection(erManFile.copy());
        List<Long> erManFilePersonIds = erManFilePerson.stream().map(object -> object.getLong("person.id")).collect(Collectors.toList());
        erManFilePersonIds = filterWorkType(erManFilePersonIds, highLevelType);
        // 人员任职经历
        QFilter qFilter = new QFilter("person.id", QCP.in, erManFilePersonIds);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and("isprimary", QCP.equals, "1");
        if (null != endDate) {
            // 日期过滤
            qFilter.and(new QFilter("startdate", "<=", endDate).and("sysenddate", ">=", endDate));
        }
        // 组织过滤
        qFilter.and("adminorg.id", QCP.in, allBelowHROrg);
        // 岗位层级
        DynamicObjectCollection empPosOrgRel = QueryServiceHelper.query("hrpi_empposorgrel", "person.id,startdate,sysenddate,tdkw_employtype.id,tdkw_employtype.name,tdkw_employtype.number,tdkw_postlevel", qFilter.toArray());
        List<Long> empPosOrgRelPersonIds = empPosOrgRel.stream().map(object -> object.getLong("person.id")).collect(Collectors.toList());

        Map<String, Object> param = new HashMap<>();
        param.put("var", empPosOrgRelPersonIds);
        // 执行过滤，开始时间与结束时间过滤
        erManFile = erManFile.filter("person.id in var", param);

        return ORM.create().toPlainDynamicObjectCollection(erManFile.copy());
    }

    /**
     * @description 领导统计-人员统计过滤
     * @author XXXX
     * @date 2023/6/27
     */
    public static DynamicObjectCollection getPersonsByEndDate(String pkOrg, Date endDate, List<Object> postGradeList, List<Object> psNclList, String algoKey, String highLevelType) {
        // 获取结束时间的最后一秒
        Calendar calendar = Calendar.getInstance();
        // 那天的最后一秒
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        endDate = calendar.getTime();
        // 获取权限信息
        Map<String, Object> adminOrg = getAdminOrg();
        // 如果包含10000L就返回true
        boolean hasAllOrgPerm = (boolean) adminOrg.get("isAdmin");
        // 权限组织
        List<Long> hasPerOrg = (List<Long>) adminOrg.get("adminOrgIds");
        //
        List<Long> allBelowHROrg;

        // 组织是否为空，为空则查询所有组织下的数据
        if (StringUtils.isNotEmpty(pkOrg)) {
            allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(pkOrg);
        } else {
            allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds("100000");
        }
        logger.info("权限组织：" + hasPerOrg.toString());
        // 全组织查看权限
        if (!hasAllOrgPerm) {
            // 获取普通组织与权限组织的交集
            logger.info("交集组织allBelowHROrg：" + allBelowHROrg.toString());
            logger.info("交集组织hasPerOrg：" + hasPerOrg.toString());
            allBelowHROrg.retainAll(hasPerOrg);
        }
        logger.info("交集组织：" + allBelowHROrg.toString());

        QFilter filter = new QFilter("1", QCP.in, 1);
        // 岗位层级过滤
        if (null != postGradeList && postGradeList.size() > 0) {
            // 岗位层级过滤
            filter.and("empposrel.tdkw_postlevel", QCP.in, postGradeList);
        }

        // 人员类型过滤
        if (null != psNclList && psNclList.size() > 0) {
            filter.and("empposrel.tdkw_employtype.number", QCP.in, psNclList);
        }
        // 人事业务档案
        List<Long> erManFileIds = PeopleCountingUtils.getAllErManFileByPersonIds(filter);
        erManFileIds = filterWorkType(erManFileIds, highLevelType);
        List<Long> erManFileEduAndPoliIds = new ArrayList<>(erManFileIds);

        // 人员任职经历
        QFilter qFilter = new QFilter("person.id", QCP.in, erManFileEduAndPoliIds);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and("isprimary", QCP.equals, "1");
        // 组织过滤
        qFilter.and("adminorg.id", QCP.in, allBelowHROrg);
        // 日期过滤
        qFilter.and(new QFilter("startdate", "<=", endDate).and("sysenddate", ">=", endDate));
        DataSet empPosOrgRel = ORM.create().queryDataSet(algoKey, "hrpi_empposorgrel", "person,person.id,startdate,sysenddate,adminorg,tdkw_postlevel", qFilter.toArray());
        DynamicObjectCollection empPosOrgRelPersonIds = ORM.create().toPlainDynamicObjectCollection(empPosOrgRel.copy());
        List<Long> collect = empPosOrgRelPersonIds.stream().map(object -> object.getLong("person.id")).collect(Collectors.toList());

        QFilter peRnoNtsPropFilter = new QFilter("person.id", QCP.in, collect);
        peRnoNtsPropFilter.and("iscurrentversion", QCP.equals, true);
        // 人员非时序性属性
        DataSet peRnoNtsProp = ORM.create().queryDataSet(algoKey, "hrpi_pernontsprop", "person,person.id,age", new QFilter[]{peRnoNtsPropFilter});
        // 人员基本信息补充
        DataSet peRnoNtsGion = ORM.create().queryDataSet(algoKey, "hrpi_perregion", "person,person.id,politicalstatus.name", new QFilter[]{peRnoNtsPropFilter});

        DataSet finish = empPosOrgRel.leftJoin(peRnoNtsProp).on("person", "person").select("person", "person.id", "startdate", "sysenddate", "adminorg", "tdkw_postlevel", "age").finish();
        finish = finish.leftJoin(peRnoNtsGion).on("person", "person").select("person", "person.id", "startdate", "sysenddate", "adminorg", "tdkw_postlevel", "age", "politicalstatus.name").finish();

        return ORM.create().toPlainDynamicObjectCollection(finish.copy());
    }

    public static List<Long> getAllErManFileByPersonIds(QFilter filter) {
        filter.and("businessstatus", QCP.equals, "1");
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("filetype.number", QCP.in, new String[]{"1010_S", "1050_S", "1060_S", "1070_S", "1110_S", "1190_S"});
        filter.and("empposrel.datastatus", QCP.equals, "1");
        filter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
//        filter.and("person.id", QCP.equals, 1758887264718446775L);
        // 人事业务档案
        return QueryServiceHelper.query("hspm_ermanfile", "person.id", filter.toArray()).stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
    }

    /**
     * @author XXXX
     * @Description 获取人员对应的人事业务档案
     * @Date 2023/8/28
     */
    public static List<DynamicObject> getAllErManFileByPerson(QFilter filter) {
        filter.and("businessstatus", QCP.equals, "1");
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("filetype.number", QCP.in, new String[]{"1010_S", "1050_S", "1060_S", "1070_S", "1110_S", "1190_S"});
//        filter.and("empposrel.businessstatus", QCP.equals, "1");
        filter.and("empposrel.datastatus", QCP.equals, "1");
        filter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);

        // 人事业务档案
        return QueryServiceHelper.query("hspm_ermanfile", "id,person.id,person.name,empposrel.startdate,empposrel.sysenddate,pernontsprop.age as age,empposrel.adminorg as adminorg", filter.toArray());
    }

    /**
     * @author XXXX
     * @Description 按组织与日期过滤人员信息
     * @Date 2023/8/28
     */
    public static DynamicObject[] getCurrentPersonByOrgs(List<Long> orgIds, Date queryDate) {
        QFilter orgQf = new QFilter("adminorg.id", "in", orgIds);
        QFilter currentQf = new QFilter("employee.laborrelstatus.labrelstatusprd", "=", HRPIValueConstants.LABRELSTATUSPRD_ING);
        boolean queryDateEmpty = queryDate == null || DateUtil.dayEquals(queryDate, new Date());
        QFilter[] qFilters;
        if (queryDateEmpty) {
            qFilters = new QFilter[]{orgQf, currentQf, QFilterUtil.getCurrentQf(), QFilterUtil.getStatusFilters(), QFilterUtil.getInitStatusFinish()};
        } else {
            QFilter startQf = new QFilter("startdate", "<=", queryDate);
            QFilter endDateQf = new QFilter("enddate", ">=", queryDate);
            qFilters = new QFilter[]{orgQf, startQf, endDateQf, QFilterUtil.getCurrentQf(), QFilterUtil.getInitStatusFinish()};
        }

        DynamicObject[] depempsTmp = depempServiceHelper.queryOriginalArray("id,orgteam.id,otclassify.id,adminorg.id,employee.id,person.id,person.name,person.number,postype.name,postype.number", qFilters);
        if (!queryDateEmpty && depempsTmp.length > 0) {
            Map<Long, List<DynamicObject>> empDepMap = (Map) Arrays.stream(depempsTmp).collect(Collectors.groupingBy((depemp) -> {
                return depemp.getLong("employee.id");
            }));
            QFilter activeFilter = new QFilter("laborrelstatus.labrelstatusprd", "=", HRPIValueConstants.LABRELSTATUSPRD_ING);
            QFilter boidFilter = new QFilter("boid", "in", empDepMap.keySet());
            QFilter startQf = new QFilter("startdate", "<=", queryDate);
            QFilter endDateQf = new QFilter("enddate", ">=", queryDate);
            DynamicObject[] activeEmployeeArr = HRPIEmployeeRepository.employeeServiceHelper.queryOriginalArray("id,boid", new QFilter[]{boidFilter, activeFilter, startQf, endDateQf, QFilterUtil.getInitStatusFinish()});
            if (activeEmployeeArr.length > 0) {
                List<Long> activeEmployeeBoids = (List) Arrays.stream(activeEmployeeArr).map((employee) -> {
                    return employee.getLong("boid");
                }).collect(Collectors.toList());
                empDepMap.keySet().removeIf((employeeid) -> {
                    return !activeEmployeeBoids.contains(employeeid);
                });
                return (DynamicObject[]) empDepMap.values().stream().flatMap(Collection::stream).toArray((x$0) -> {
                    return new DynamicObject[x$0];
                });
            }
        }

        return depempsTmp;
    }

    /**
     * @description 获取最近六个月当中每个月的最后一天
     * @author XXXX
     * @date 2023/6/27
     */
    public static List<Date> getLastDayOfExSixMonth(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        ArrayList<Date> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = sdf.parse(date);
        // 那天的最后一秒
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date time = calendar.getTime();
        list.add(time);

        Date temp;
        for (int i = 1; i <= 5; i++) {
            calendar.setTime(dateTime);
            calendar.add(Calendar.MONTH, -i);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            temp = calendar.getTime();
            list.add(temp);
        }
        list.sort(Date::compareTo);
        return list;
    }

    /**
     * @description 获取指定时间中每个月的最后一天
     * @author XXXX
     * @date 2023/6/27
     */
    public static List<Date> getMonth(Date start, Date end) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        LocalDate startDate = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        calendar.setTime(end);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        LocalDate endDate = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period period = Period.between(startDate, endDate);
        // 两个日期之间相差的月份
        int months = (int) period.toTotalMonths();

        ArrayList<Date> list = new ArrayList<>();
        // 那天的最后一秒
        calendar.setTime(end);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date time = calendar.getTime();
        list.add(time);

        Date temp;
        for (int i = 1; i <= months; i++) {
            calendar.setTime(end);
            calendar.add(Calendar.MONTH, -i);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            temp = calendar.getTime();
            list.add(temp);
        }
        list.sort(Date::compareTo);
        return list;
    }

    /**
     * 根据编码获取基础资料
     *
     * @param entityNumber 单据标识
     * @param number       编码
     * @return DynamicObject
     * @author XXXXwyj
     * @date 2022/6/24
     */
    public static DynamicObject findDynamicObjectByNumber(String entityNumber, String number) {
        return findDynamicObjectByNumber(entityNumber, number, null, null);
    }

    /**
     * 根据编码获取基础资料
     *
     * @param entityNumber    单据标识
     * @param number          编码
     * @param requireMsg      错误信息
     * @param requireMsgForCq 查询星瀚基础资料错误信息
     * @return DynamicObject
     * @author XXXXwyj
     * @date 2022/6/24
     */
    public static DynamicObject findDynamicObjectByNumber(String entityNumber, String number, String requireMsg, String requireMsgForCq) {
        if (requireMsg != null) {
            Assert.notNull(number, requireMsg);
        } else if (StringUtils.isBlank(number)) {
            return null;
        }
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(entityNumber, "id", new QFilter("number", "=", number).toArray());
        if (StringUtils.isEmpty(requireMsgForCq) && dynamicObject == null) {
            return null;
        }
        Assert.notNull(dynamicObject, requireMsgForCq);
        return BusinessDataServiceHelper.loadSingle(dynamicObject.get("id"), entityNumber);
    }

    /**
     * 获取当前登录人对应的考勤人ID
     * <p>
     * 有可能获取不到考勤人ID，比如员工并未在考勤创建档案时就获取不到，此时返回null
     *
     * @return 当前登录人对应的考勤人ID
     */
    public static long getAttPersonIdOfCurrentLoginUser() {
        final Long userId = MobileCommonServiceHelper.getInstance().getUserId();
        if (userId == null) {
            return 0;
        }
        return userId;
    }

    /**
     * 获取考勤档案
     * <p>
     * 可能获取不到考勤档案，
     *
     * @param attPersonId 考勤人ID
     * @param attFileDate 档案日期，档案是时序性对象，需要指定日期获取
     * @return 考勤档案
     */
    public static Map<String, String> getAttFile(long attPersonId, Date attFileDate) {
        String attFileDateStr = WTCDateUtils.date2Str(attFileDate, WTCCommonConstants.DEF_DATE_FORMAT);
        return MobileHomePageServiceHelper.getAttFile(attPersonId, attFileDateStr);
    }

    /**
     * @description 获取权限组织并且存入缓存(app - 人员分析专用)
     * @author XXXX
     * @date 2023/10/16
     */
    public static Map<String, Object> getAdminOrg() {
        long currentUserId = UserServiceHelper.getCurrentUserId();
        // 获取缓存中的权限组织
        String adminOrgIdsStr = HRUserRoleCacheUtils.getCache(String.valueOf(currentUserId), "psnChange-app-adminOrgIds");
        String isAdminOrgStr = HRUserRoleCacheUtils.getCache(String.valueOf(currentUserId), "psnChange-app-isAdminOrg");
        logger.info("权限组织" + adminOrgIdsStr);
        logger.info("是否全组织权限" + isAdminOrgStr);
        // 如果包含10000L就返回true 不对组织进行过滤
        boolean isAdmin;
        // 权限组织
        List<Long> adminOrgIds = new ArrayList<>();
        if (kd.bos.util.StringUtils.isNotEmpty(isAdminOrgStr)) {
            isAdmin = Boolean.parseBoolean(isAdminOrgStr);
            if (StringUtils.isNotEmpty(adminOrgIdsStr)) {
                List<String> myList = new ArrayList<>(Arrays.asList(adminOrgIdsStr.split(",")));
                adminOrgIds = myList.stream().map(Long::valueOf).collect(Collectors.toList());
            }
        } else {
            // 控权组组织过滤,过滤当前组织
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), ROLE_ORG);
            adminOrgIds = result.getHasPermOrgs();
            isAdmin = result.isHasAllOrgPerm();
            // 将权限组织转为string对象
            String hasPerOrgStr = org.apache.commons.lang3.StringUtils.join(adminOrgIds, ",");
            // 将权限组织存入缓存
            HRUserRoleCacheUtils.putCache(String.valueOf(currentUserId), "psnChange-app-adminOrgIds", hasPerOrgStr);
            HRUserRoleCacheUtils.putCache(String.valueOf(currentUserId), "psnChange-app-isAdminOrg", String.valueOf(isAdmin));
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("isAdmin", isAdmin);
        map.put("adminOrgIds", adminOrgIds);
        return map;
    }
}
