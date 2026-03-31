package tdkw.hrmp.hrobs.formplugin.util;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @description: 人员分析工具类
 * @author xxx
 * @date: 2023/10/23 15:32
 * @param:
 * @param: null
 * @return: null
 **/
public class PersonAnalyzeUtil {


    /**
     * @description: 获取表头默认开始时间
     * @author xxx
     * @date: 2023/10/23 15:33
     * @param:
     * @return: Date
     **/
    public static Date getDefaultBeginDate() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 往前减去一年
        LocalDate previousYearDate = currentDate.minusYears(1);
        // 再加一个月
        LocalDate previousYearAndMonthDate = previousYearDate.plusMonths(1);
        // 定义日期格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 格式化日期为字符串
        String previousYearDateStr = previousYearAndMonthDate.format(formatter);
        String endDateStr = previousYearDateStr + " 00:00:00";
        // 将字符串解析为日期对象
        LocalDateTime headEndDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 转换为java.util.Date类型
        return Date.from(headEndDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @description: 获取表头默认结束时间
     * @author xxx
     * @date: 2023/10/23 15:33
     * @param:
     * @return: Date
     **/
    public static Date getDefaultEndDate() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 定义日期格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 格式化日期为字符串
        String previousYearDateStr = currentDate.format(formatter);
        String endDateStr = previousYearDateStr + " 00:00:00";
        // 将字符串解析为日期对象
        LocalDateTime headEndDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 转换为java.util.Date类型
        return Date.from(headEndDate.atZone(ZoneId.systemDefault()).toInstant());
    }

}
