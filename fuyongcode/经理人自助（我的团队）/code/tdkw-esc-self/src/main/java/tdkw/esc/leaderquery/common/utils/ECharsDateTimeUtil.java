package tdkw.esc.leaderquery.common.utils;

import kd.bos.login.utils.DateUtils;
import org.apache.commons.math3.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DateTimeUtil
 *
 * @author xxx
 * @date 2023/6/27
 */
public class ECharsDateTimeUtil {


    /**
     * @author xxx
     * @Description 获取间隔的所有月份
     */
    public static List<Pair<Date, Date>> getMonthList(Date start, Date end) {
        Calendar a = Calendar.getInstance();
        a.setTime(start);
        Calendar b = Calendar.getInstance();
        b.setTime(end);
        b.set(Calendar.HOUR_OF_DAY, 23);
        b.set(Calendar.MINUTE, 59);
        b.set(Calendar.SECOND, 59);
        Calendar aToUse = org.apache.commons.lang3.time.DateUtils.truncate(a, Calendar.MONTH);
        Calendar bToUse = org.apache.commons.lang3.time.DateUtils.truncate(b, Calendar.MONTH);
        List<Pair<Date, Date>> datePairs = new LinkedList<>();
        if (aToUse.compareTo(bToUse) > 0) {
            return Collections.emptyList();
        }
        if (aToUse.compareTo(bToUse) == 0) {
            datePairs.add(new Pair<>(a.getTime(), b.getTime()));
        } else {
            Date currentDate = aToUse.getTime();
            while (currentDate.compareTo(bToUse.getTime()) <= 0) {
                Calendar endDateCalendar = Calendar.getInstance();
                endDateCalendar.setTime(currentDate);
                endDateCalendar.set(Calendar.DAY_OF_MONTH, endDateCalendar.getActualMaximum(Calendar.DATE));
                datePairs.add(new Pair<>(currentDate, endDateCalendar.getTime()));
                currentDate = org.apache.commons.lang3.time.DateUtils.addMonths(currentDate, 1);
            }
            Pair<Date, Date> dateDatePair = datePairs.remove(0);
            datePairs.add(0, new Pair<>(a.getTime(), dateDatePair.getSecond()));
            dateDatePair = datePairs.remove(datePairs.size() - 1);
            datePairs.add(new Pair<>(dateDatePair.getFirst(), b.getTime()));
        }
        return datePairs;
    }

    /**
     * 到截止日期每个月最后一天
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static List<Date> getLastDayOfExSixMonth(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        ArrayList<Date> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = sdf.parse(date);
        //判断是不是今天
        Date nowDate = new Date();
        if (sdf.format(nowDate).equals(date)) {
            //当前时分秒之前
            list.add(nowDate);

        } else {
            //那天的最后一秒
            calendar.setTime(dateTime);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date time = calendar.getTime();
            list.add(time);
        }

        // list.add(dateTime);

        Date temp;
        // ArrayList<String> strings = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            calendar.setTime(dateTime);
            calendar.add(Calendar.MONTH, -i);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            temp = calendar.getTime();
            // strings.add(sdf.format(temp));
            list.add(temp);
        }
        list.sort(Date::compareTo);
        return list;
    }

    /**
     * 六个月来每个月第一天
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static List<Date> getFirstDayOfExSixMonth(String date) throws ParseException {
        ArrayList<Date> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = sdf.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        Date temp;
        // ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            calendar.setTime(dateTime);
            calendar.add(Calendar.MONTH, -i);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            temp = calendar.getTime();
            //strings.add(sdf.format(temp));
            list.add(temp);
        }
        list.sort(Date::compareTo);
        return list;
    }

    /**
     * 获取三年的最后一天最后一秒
     *
     * @param startYearParam 从哪年开始查
     */
    public static List<Date> getEndDayOfYear(String startYearParam) {
        ArrayList<Date> list = new ArrayList<>();
        //开始查询的某年
        int intStartYearParam = Integer.parseInt(startYearParam);
        //获取要查询的最后一年是哪年
        //int queryLastYearInt = intStartYearParam + 2;
        //获取现在是哪年
        String nowYear = DateUtils.getYear();
        int nowYearInt = Integer.parseInt(nowYear);
        // 2021  2022  2023
        for (int i = 0; i < 3; i++) {
            //intStartYearParam = intStartYearParam + i;
            //如果那年在今年之前  获取那年最后一天最后一秒
            if (intStartYearParam + i < nowYearInt) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, intStartYearParam + i);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                list.add(calendar.getTime());
            } else {
                list.add(new Date());
                break;
            }
        }
        list.sort(Date::compareTo);
        return list;
    }

    /**
     * 获取三年第一天
     *
     * @param startYearParam 传参的开始年
     * @return
     */
    public static List<Date> getFirstDayOfYear(String startYearParam) {
        ArrayList<Date> list = new ArrayList<>();
        //开始查询的某年
        int intStartYearParam = Integer.parseInt(startYearParam);
        //获取要查询的最后一年是哪年
        Integer queryLastYearInt = intStartYearParam + 2;
        //获取现在是哪年
        String nowYear = DateUtils.getYear();
        int nowYearInt = Integer.parseInt(nowYear);
        // 2021  2022  2023
        for (int i = 0; i < 3; i++) {
            // intStartYearParam = intStartYearParam + i;
            if (intStartYearParam + i <= nowYearInt) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, intStartYearParam + i);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                list.add(calendar.getTime());
            } else {
                break;
            }
        }
        list.sort(Date::compareTo);
        return list;
    }


}
       /* //如果是查询到今年为止  获取前两年的第一天到最后一天 现在的时间
        if ((Integer.getInteger(nowYear) == queryLastYearInt)) {
            Date date = new Date();
            list.add(date);
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.YEAR, intStartYearParam);
            list.add(calendar.getTime());
            calendar.clear();
            calendar.set(Calendar.YEAR, intStartYearParam + 1);
            list.add(calendar.getTime());
            //如果不是今年  获取每年的第一天
        } else {
            for (int i = 0; i < 3; i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, intStartYearParam + i);
                //超过今年的就不放了  今年之前的放进去
                if (new Date().before(calendar.getTime())) {

                }
                list.add(calendar.getTime());
            }
        }
        ArrayList<Date> returnList = new ArrayList<>();
        for (Date date : list) {
            if (date) {

            }
        }*/

