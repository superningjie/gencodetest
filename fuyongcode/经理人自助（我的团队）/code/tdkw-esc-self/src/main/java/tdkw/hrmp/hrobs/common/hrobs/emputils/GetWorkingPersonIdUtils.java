package tdkw.hrmp.hrobs.common.hrobs.emputils;

import com.alibaba.fastjson.JSONArray;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * getOneMonthWorkPersonIdUtils
 *
 * @author xxx
 * @date 2023/6/29
 */
public class GetWorkingPersonIdUtils {
    private static final Log logger = LogFactory.getLog(GetWorkingPersonIdUtils.class);



    /**
     * 查询一个月内入职人员id集合 职业信息基础页面
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static List<Long> getOneMonthWorkPersonIdList(String beginDate, String endDate, String pkOrg, JSONArray postGradeList, JSONArray psNclList) {
        List<Date> firstDayOfExSixMonth = null;
        List<Date> lastDayOfExSixMonth = null;
        try {
            firstDayOfExSixMonth = ECharsDateTimeUtil.getFirstDayOfExSixMonth(endDate);
            lastDayOfExSixMonth = ECharsDateTimeUtil.getLastDayOfExSixMonth(endDate);
        } catch (
                ParseException e) {
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }
        //搜索日期范围
        Date startDay = firstDayOfExSixMonth.get(5);
        Date endDay = lastDayOfExSixMonth.get(5);
        //查找对应时间段内入职成功的personId
        //通过职业信息基础页面 hrpi_empentrel 的首次用工时间 firststartdate 来查询
        List<Long> filterPersonIds = PeopleCountingUtil.getPersons(pkOrg, endDay, postGradeList, psNclList);
        QFilter filter = GetEmpFilterUtil.getTotalFilter(startDay, endDay, filterPersonIds);
        DynamicObject[] person = BusinessDataServiceHelper.load("hrpi_empentrel", "person", filter.toArray());
        // HRBaseServiceHelper query = new HRBaseServiceHelper("hrpi_empentrel");
        //DynamicObject[] person = query.query("person", new QFilter[]{null});
        ArrayList<Long> personIdList = new ArrayList<>();
        for (DynamicObject object : person) {
            long personId = object.getLong("person.id");
            personIdList.add(personId);
        }
        return personIdList;
    }

    /**
     * 获取这个月的入职人数
     *
     * @return 入职人数
     */
    public static List<Long> getThisMonthWorkPersonIdList() {
        //搜索日期范围
        Date endDay = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDay);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDay = calendar.getTime();
        //查找对应时间段内入职成功的personId
        //通过职业信息基础页面 hrpi_empentrel 的首次用工时间 firststartdate 来查询
        //是否当前版本为1 用工关系类型为用工进行中
        //hrpi_pereduexp
       /* QFilter qFilter = new QFilter("iscurrentversion", QCP.equals, "1");
        qFilter.and("labrelstatusprd.name", QCP.equals, "用工进行中");
        qFilter.and("firststartdate", QCP.large_equals, startDay);*/
        //  QFilter qFilter = new QFilter("iscurrentversion", QCP.equals, "1");
        // qFilter.and("labrelstatusprd.name", QCP.equals, "用工进行中");
   /*     QFilter qFilter = new QFilter("firststartdate", QCP.large_equals, startDay);
        qFilter.and("firststartdate", QCP.less_equals, endDay);
        qFilter.and("labrelstatusprd.name", QCP.equals, "用工进行中");*/
        // PeopleCountingUtil.getPersons()
        // QFilter filter = GetEmpFilterUtil.getTotalFilter(startDay, endDay,);
        QFilter filter = new QFilter("1", "=", 1);
        DynamicObject[] person = BusinessDataServiceHelper.load("hrpi_empentrel", "person", filter.toArray());
        //   HRBaseServiceHelper query = new HRBaseServiceHelper("hrpi_empentrel");
        //   DynamicObject[] person = query.query("person", new QFilter[]{null});
        ArrayList<Long> personIdList = new ArrayList<>();
        for (DynamicObject object : person) {
            long personId = object.getLong("person.id");
            personIdList.add(personId);
        }
        return personIdList;
    }

}
