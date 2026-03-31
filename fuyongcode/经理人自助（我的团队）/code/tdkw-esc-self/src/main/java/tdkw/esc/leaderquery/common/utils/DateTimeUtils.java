package tdkw.esc.leaderquery.common.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 日期工具类
 *
 * @author xxx
 */
public class DateTimeUtils {


    public static String getDateByWeek(int week) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        calendar.set(Calendar.DAY_OF_WEEK, 1);// 2为星期一，1为星期天
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(calendar.getTime());
    }

    public static int getMouthByWeek(int week) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        calendar.set(Calendar.DAY_OF_WEEK, 7);
        return calendar.get(Calendar.MONTH) + 1;
    }


    /**
     * 获取一年有多少周
     *
     * @param date
     * @return
     */
    public static int getAllWeeks(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        calendar.setTime(date);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        if (week != 53) {
            week = 52;
        }
        return week;
    }


    /**
     * 获取某年的第几周的开始日期
     *
     * @param year
     * @param week
     * @return
     */
    public static Date getFirstDayOfWeek(int year, int week) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);
        Calendar cal = (GregorianCalendar) c.clone();
        cal.add(Calendar.DATE, week * 7);
        return getFirstDayOfWeek(cal.getTime());

    }


    /**
     * 获取某年的第几周的结束日期
     *
     * @param year
     * @param week
     * @return
     */
    public static Date getLastDayOfWeek(int year, int week) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);
        Calendar cal = (GregorianCalendar) c.clone();
        cal.add(Calendar.DATE, week * 7);
        return getLastDayOfWeek(cal.getTime());

    }


    /**
     * 获取当前时间所在周的开始日期
     *
     * @param date
     * @return
     */
    public static Date getFirstDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
        return c.getTime();
    }


    /**
     * 获取当前时间所在周的结束日期
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6); // Sunday
        return c.getTime();
    }


    /**
     * 获取时间在第几周
     *
     * @param date
     * @return
     */
    public static int getweekOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        calendar.setMinimalDaysInFirstWeek(4);// 可以不用设置
        //calendar.setTimeInMillis(System.currentTimeMillis());// 获得当前的时间戳
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);// 获得当前日期属于今年的第几周
        return weekOfYear;

    }

    /**
     * 获取日期的本周的开始时间
     *
     * @param date
     * @return
     */
    public static Date getBeginDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(date);
        int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0)
            day_of_week = 7;
        cal.add(Calendar.DATE, -day_of_week + 1);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取本周的结束时间
     *
     * @return
     */
    public static Date getEndDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(getBeginDayOfWeek(date));
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }

    /**
     * 向推count周的结束时间
     *
     * @param date
     * @param count
     * @return
     */
    public static Date getEndDayOfWeekcount(Date date, int count) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        calendar.setTime(date);
        calendar.setTime(getBeginDayOfWeek(date));
        calendar.add(Calendar.DATE, (count - 1) * 7 + 6);
        return getEndDayOfWeek(calendar.getTime());
    }

    /**
     * 返回昨天
     *
     * @param today
     * @return
     */
    public static Date yesterday(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        return calendar.getTime();
    }

    /**
     * 返回明天
     *
     * @param today
     * @return
     */
    public static Date tomorrow(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        return calendar.getTime();
    }

    /**
     * 获取当天的开始时间
     *
     * @return
     */
    public static Date getDayBegin(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取当天的结束时间
     *
     * @return
     */
    public static Date getDayEnd(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    /**
     * 获取当天的开始时间
     *
     * @return
     */
    public static Date getDayBegin() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取当天的结束时间
     *
     * @return
     */
    public static Date getDayEnd() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    /**
     * 获取昨天的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfYesterday() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    /**
     * 获取昨天的结束时间
     *
     * @return
     */
    public static Date getEndDayOfYesterDay() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayEnd());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    /**
     * 获取明天的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfTomorrow() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * 获取明天的结束时间
     *
     * @return
     */
    public static Date getEndDayOfTomorrow() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayEnd());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * 获取本周的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfWeek() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取本周的结束时间
     *
     * @return
     */
    public static Date getEndDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(getBeginDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }

    /**
     * 获取上周的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfLastWeek() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek - 7);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取上周的结束时间
     *
     * @return
     */
    public static Date getEndDayOfLastWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(getBeginDayOfLastWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }

    /**
     * 下周一
     *
     * @param date
     * @return
     */
    public static Date getNextWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(getEndDayOfWeek());
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    /**
     * 下周日
     *
     * @param date
     * @return
     */
    public static Date getNextWeekSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为一周开始的第一天
        cal.setTime(getNextWeekMonday(date));
        cal.add(Calendar.DATE, 6);
        return cal.getTime();
    }

    /**
     * 获取本月的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 1, 1);
        return getDayStartTime(calendar.getTime());
    }


    /**
     * 获取年份和月份
     *
     * @param year
     * @param month
     * @return
     */

    public static Date getMonth(int year, int month) {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        return c.getTime();

    }

    /**
     * 获取时间在第几周
     *
     * @param date
     * @return
     */
    public static int getMonthOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekOfYear = calendar.get(Calendar.MONTH) + 1;// 获得当前日期属于今年的第几月
        return weekOfYear;

    }

    /**
     * 获取本月的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(date), getNowMonth(date) - 1, 1);
        return getDayStartTime(calendar.getTime());
    }

    /**
     * 获取本月的结束时间
     *
     * @return
     */
    public static Date getEndDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth() - 1, day);
        return getDayEndTime(calendar.getTime());
    }

    /**
     * 获取本月的结束时间
     *
     * @return
     */
    public static Date getEndDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(date), getNowMonth(date) - 1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(date), getNowMonth(date) - 1, day);
        return getDayEndTime(calendar.getTime());
    }

    /**
     * 获取上月的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 2, 1);
        return getDayStartTime(calendar.getTime());
    }

    /**
     * 获取上月的结束时间
     *
     * @return
     */
    public static Date getEndDayOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 2, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth() - 2, day);
        return getDayEndTime(calendar.getTime());
    }

    /**
     * 获取下月的开始时间
     *
     * @return
     */
    public static Date getBeginDayOFNextMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() + 0, 1);
        return getDayStartTime(calendar.getTime());
    }

    /**
     * 获取下月的结束时间
     *
     * @return
     */
    public static Date getEndDayOfNextMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() + 0, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth() + 0, day);
        return getDayEndTime(calendar.getTime());
    }

    /**
     * 获取过去三个月的开始时间 ，结束时间为上个月的结束时间getEndDayOfLastMonth()
     *
     * @return
     */
    public static Date getBeginDayOfThreeMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 4, 1);
        return getDayStartTime(calendar.getTime());
    }

    /**
     * 获取未来三个月的结束时间 ，开始时间为下个月的开始时间getBeginDayOFNextMonth()
     *
     * @return
     */
    public static Date getEndDayOfThreeMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() + 2, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth() + 2, day);
        return getDayEndTime(calendar.getTime());
    }

    /**
     * 上季度的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfLastQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfThisQuarter());
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 3);
        // cal.set(Calendar.DATE,
        // getLastDayOfMonth(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1));
        return getDayStartTime(cal.getTime());
    }

    /**
     * 上季度的结束时间
     *
     * @return
     */
    public static Date getEndDayOfLastQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfThisQuarter());
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        cal.set(Calendar.DATE, getLastDayOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        return getDayEndTime(cal.getTime());
    }

    /**
     * 本季度的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfThisQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 00, 00, 00);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        try {
            if (currentMonth >= 1 && currentMonth <= 3) {
                cal.set(Calendar.MONTH, 0);
            } else if (currentMonth >= 4 && currentMonth <= 6) {
                cal.set(Calendar.MONTH, 3);
            } else if (currentMonth >= 7 && currentMonth <= 9) {
                cal.set(Calendar.MONTH, 6);
            } else if (currentMonth >= 10 && currentMonth <= 12) {
                cal.set(Calendar.MONTH, 9);
            }
            cal.set(Calendar.DATE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getDayStartTime(cal.getTime());

    }

    /**
     * 本季度的结束时间
     *
     * @return
     */
    public static Date getEndDayOfThisQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfThisQuarter());
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 2);
        cal.set(Calendar.DATE, getLastDayOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        return getDayEndTime(cal.getTime());
    }

    /**
     * 下季度的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfNextQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfThisQuarter());
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 3);
        // cal.set(Calendar.DATE,
        // getLastDayOfMonth(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1));
        return getDayStartTime(cal.getTime());
    }

    /**
     * 下季度的结束时间
     *
     * @return
     */
    public static Date getEndDayOfNextQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfNextQuarter());
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 2);
        cal.set(Calendar.DATE, getLastDayOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        return getDayEndTime(cal.getTime());
    }

    /**
     * 获取去年时间
     *
     * @param date
     * @return
     */
    public static Date getLastYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, -1);
        return cal.getTime();
    }

    /**
     * 获取去年的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfLastYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear() - 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取去年的结束时间
     *
     * @return
     */
    public static Date getEndDayOfLastYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear() - 1);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, getLastDayOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        return getDayEndTime(cal.getTime());
    }

    /**
     * 获取今年的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取今年的开始时间
     *
     * @param date
     * @return
     */
    public static Date getBeginDayOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, getNowYear(date));
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取今年的结束时间
     *
     * @return
     */
    public static Date getEndDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, getLastDayOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        return getDayEndTime(cal.getTime());
    }

    /**
     * 获取今年的结束时间
     *
     * @param date
     * @return
     */
    public static Date getEndDayOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, getNowYear(date));
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, getLastDayOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        return getDayEndTime(cal.getTime());
    }

    /**
     * 获取明年的开始时间
     *
     * @return
     */
    public static Date getBeginDayOfNextYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear() + 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取明年的结束时间
     *
     * @return
     */
    public static Date getEndDayOfNextYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear() + 1);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, getLastDayOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        return getDayEndTime(cal.getTime());
    }

    /**
     * 获取某个日期的开始时间
     *
     * @param d
     * @return
     */
    public static Timestamp getDayStartTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d)
            calendar.setTime(d);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }

    /**
     * 获取某个日期的结束时间
     *
     * @param d
     * @return
     */
    public static Timestamp getDayEndTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d)
            calendar.setTime(d);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTimeInMillis());
    }

    /**
     * 获取今年是哪一年
     *
     * @return
     */
    public static Integer getNowYear() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }

    /**
     * 获取今年是哪一年
     *
     * @return
     */
    public static Integer getNowYear(Date date) {
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }

    /**
     * 获取本月是哪一月
     *
     * @return
     */
    public static int getNowMonth() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
    }

    /**
     * 获取本月是哪一月
     *
     * @return
     */
    public static int getNowMonth(Date date) {
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
    }

    /**
     * 两个日期相减得到的天数
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int getDiffDays(Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("getDiffDays param is null!");
        }
        long diff = (endDate.getTime() - beginDate.getTime()) / (1000 * 60 * 60 * 24);
        int days = new Long(diff).intValue();
        return days;
    }

    /**
     * 两个日期相减得到的毫秒数
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long dateDiff(Date beginDate, Date endDate) {
        long date1ms = beginDate.getTime();
        long date2ms = endDate.getTime();
        return date2ms - date1ms;
    }

    /**
     * 获取两个日期中的最大日期
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date max(Date beginDate, Date endDate) {
        if (beginDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return beginDate;
        }
        if (beginDate.after(endDate)) {
            return beginDate;
        }
        return endDate;
    }

    /**
     * 获取两个日期中的最小日期
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date min(Date beginDate, Date endDate) {
        if (beginDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return beginDate;
        }
        if (beginDate.after(endDate)) {
            return endDate;
        }
        return beginDate;
    }

    /**
     * 返回某月该季度的第一个月
     *
     * @param date
     * @return
     */
    public static Date getFirstSeasonDate(Date date) {
        final int[] SEASON = {1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int sean = SEASON[cal.get(Calendar.MONTH)];
        cal.set(Calendar.MONTH, sean * 3 - 3);
        return cal.getTime();
    }

    /**
     * 返回某个日期下几天的日期
     *
     * @param date
     * @param i
     * @return
     */
    public static Date getNextDay(Date date, int i) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + i);
        return cal.getTime();
    }

    /**
     * 返回某个日期前几天的日期
     *
     * @param date
     * @param i
     * @return
     */
    public static Date getFrontDay(Date date, int i) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - i);
        return cal.getTime();
    }

    /**
     * 获取某年某月到某年某月按天的切片日期集合(间隔天数的集合)
     *
     * @param beginYear
     * @param beginMonth
     * @param endYear
     * @param endMonth
     * @param k
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List getTimeList(int beginYear, int beginMonth, int endYear, int endMonth, int k) {
        List list = new ArrayList();
        if (beginYear == endYear) {
            for (int j = beginMonth; j <= endMonth; j++) {
                list.add(getTimeList(beginYear, j, k));
            }
        } else {
            {
                for (int j = beginMonth; j < 12; j++) {
                    list.add(getTimeList(beginYear, j, k));
                }
                for (int i = beginYear + 1; i < endYear; i++) {
                    for (int j = 0; j < 12; j++) {
                        list.add(getTimeList(i, j, k));
                    }
                }
                for (int j = 0; j <= endMonth; j++) {
                    list.add(getTimeList(endYear, j, k));
                }
            }
        }
        return list;
    }

    /**
     * 获取某年某月按天切片日期集合(某个月间隔多少天的日期集合)
     *
     * @param beginYear
     * @param beginMonth
     * @param k
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List getTimeList(int beginYear, int beginMonth, int k) {
        List list = new ArrayList();
        Calendar begincal = new GregorianCalendar(beginYear, beginMonth, 1);
        int max = begincal.getActualMaximum(Calendar.DATE);
        for (int i = 1; i < max; i = i + k) {
            list.add(begincal.getTime());
            begincal.add(Calendar.DATE, k);
        }
        begincal = new GregorianCalendar(beginYear, beginMonth, max);
        list.add(begincal.getTime());
        return list;
    }

    public static int getLastDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        return calendar.get(Calendar.DATE);
    }

    /**
     * 日期格式的计算
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 将指定的日期字符串转换成日期
     *
     * @param dateStr 日期字符串
     *                格式
     * @return 日期对象
     */
    public static Date parseDate(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("日期转化错误");
        }
        return date;
    }

    public static String dateFormat(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String strDate = sdf.format(date);
        return strDate;
    }
}
