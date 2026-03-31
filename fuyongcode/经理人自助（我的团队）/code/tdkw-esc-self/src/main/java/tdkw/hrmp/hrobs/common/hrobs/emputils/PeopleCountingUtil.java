package tdkw.hrmp.hrobs.common.hrobs.emputils;

import com.alibaba.fastjson.JSONArray;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.business.openservicehelper.hrpi.HRPIPersonServiceHelper;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import kd.wtc.wtbs.common.constants.WTCCommonConstants;
import kd.wtc.wtbs.common.util.WTCDateUtils;
import kd.wtc.wtss.business.servicehelper.mobile.MobileHomePageServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @description 人员统计工具类
 * @date 2023/6/28
 */
public class PeopleCountingUtil {

    /**
     * @description 获取下级组织
     * @author xxx
     * @date 2023/6/26
     */
    public static List<Long> getAllBelowHROrg(List<Long> hrOrg, List<Long> allBelow) {
        QFilter filter = new QFilter("parent", QCP.in, hrOrg);
        DynamicObjectCollection adminOrgHR = QueryServiceHelper.query("haos_adminorghr", "id", filter.toArray());
        hrOrg.clear();
        for (DynamicObject item : adminOrgHR) {
            long id = item.getLong("id");
            hrOrg.add(id);
            allBelow.add(id);
        }
        if (hrOrg.size() > 0) {
            getAllBelowHROrg(hrOrg, allBelow);
        } else {
            return allBelow;
        }
        return allBelow;
    }

    /**
     * @description 需要统计的人员id
     * @author xxx
     * @date 2023/6/27
     */
    public static List<Long> getPersons(String pkOrg, Date endDate, JSONArray postGradeList, JSONArray psNclList) {
        //人员类型数据转换成Long
        JSONArray personTypes = new JSONArray();
        for (Object psNcl : psNclList) {
            personTypes.add(new Long(psNcl.toString()));
        }
        psNclList = personTypes;

        //组织id集合
        List<Long> orgIdList = new ArrayList<>();
        //按组织与日期查询人员
        List<Map<String, Object>> personCountAndChargeInfo;
        //组织是否为空，为空则查询所有组织下的数据
        if (StringUtils.isNotEmpty(pkOrg)) {
            orgIdList.add(new Long(pkOrg));

            List<Long> allBelow = new ArrayList<>();
            allBelow.add(new Long(pkOrg));

            //获取所有下级组织
            List<Long> allBelowHROrg = PeopleCountingUtil.getAllBelowHROrg(orgIdList, allBelow);
            //按组织与日期查询人员及汇总数
            personCountAndChargeInfo = HRPIPersonServiceHelper.getPersonByOrgs(allBelowHROrg, endDate);
        } else {
            //是否当前版本 = 是
            QFilter hrFilter = new QFilter("iscurrentversion", QCP.equals, true);
            //上级组织 = 空
            hrFilter.and(new QFilter("parent", QCP.equals, 0L).or("parent", QCP.equals, null));
            //HR行政组织 - 最上级组织
            DynamicObject[] orgHrs = BusinessDataServiceHelper.load("haos_adminorghr", "id", hrFilter.toArray());
            //组织id集合
            List<Long> orgHrList = new ArrayList<>();
            //组织id集合
            List<Long> allBelow = new ArrayList<>();
            for (DynamicObject orgHr : orgHrs) {
                allBelow.add((Long) orgHr.getPkValue());
                orgHrList.add((Long) orgHr.getPkValue());
            }

            //获取全部组织id
            orgIdList = PeopleCountingUtil.getAllBelowHROrg(orgHrList, allBelow);
            //按组织与日期查询人员
            personCountAndChargeInfo = HRPIPersonServiceHelper.getPersonByOrgs(orgIdList, endDate);
        }

        //根据组织过滤后的人员ID集合
        List<Long> personIds = personCountAndChargeInfo.stream().map(object -> (Long) object.get("person")).collect(Collectors.toList());

        //是否当前版本 = 是
        QFilter filter = new QFilter("iscurrentversion", QCP.equals, true);
        //业务状态 = 生效中
        filter.and("businessstatus", QCP.equals, "1");
        //人员过滤
        filter.and("person.id", QCP.in, personIds);

        //岗位层级过滤
        if (postGradeList.size() > 0) {
            //岗位层级过滤
            filter.and("tdkw_postlevel", QCP.in, postGradeList);
        }

        //人员类型过滤
        if (psNclList.size() > 0) {
            filter.and("tdkw_employtype.id", QCP.in, psNclList);
        }

        //查询任职经历
        DynamicObject[] empPosOrgRel = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,person", filter.toArray());

        //过滤后的人员id
        return Arrays.stream(empPosOrgRel).map(object -> (Long) object.getDynamicObject("person").getPkValue()).collect(Collectors.toList());
    }

    /**
     * @description 获取最近六个月当中每个月的最后一天
     * @author xxx
     * @date 2023/6/27
     */
    public static List<Date> getLastDayOfExSixMonth(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<Date> list = new ArrayList<>();
        Date dateTime = null;
        try {
            dateTime = sdf.parse(sdf.format(date));
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        list.add(dateTime);
        Calendar calendar = Calendar.getInstance();
        Date temp;
        // ArrayList<String> strings = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            calendar.setTime(dateTime);
            calendar.add(Calendar.MONTH, -i);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            temp = calendar.getTime();
            // strings.add(sdf.format(temp));
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
     * @author xxx
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
     * @author xxx
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
}
