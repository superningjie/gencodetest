package tdkw.wtc.attendanceshift.operate.wtte_form.util;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.service.KDDateUtils;
import kd.wtc.wts.mservice.openapi.roster.model.ShiftModel;
import tdkw.wtc.attendanceshift.operate.wtabm_vaapply.ShiftQueryUtil;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description: 假勤单据-归属日期工具类
 * @author: ljl
 * @create: 2024-10-26 14:11
 **/
public class StandardDateUtil {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat SDFTIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Log logger = LogFactory.getLog(StandardDateUtil.class);

    /**
     * 获取开始日期/结束日期的班次归属日期
     * @param vaApplyDate    日期（开始/结束）
     * @param attFileBoId    休假人/出差人考勤档案id
     * @param isEndDate      是否是结束日期
     * @return Date
     */
    public static Date getStandardDate(Date vaApplyDate, Long attFileBoId, boolean isEndDate){
        Date currentDate = getDate(vaApplyDate);
        // 归属日期
        Date standardDate = currentDate;
        // 前一天
        Date preDate = addDays(-1, currentDate);
        // 后一天
        Date afterDate = addDays(1, currentDate);
        // 班次信息
        Map<String, ShiftModel> shiftModelMap = ShiftQueryUtil.shiftTimesMap(attFileBoId, preDate, afterDate);
        // 前一天班次开始、结束日期
        ShiftModel preShiftModel = shiftModelMap.get(SDF.format(preDate));
        ShiftModel afterShiftModel = shiftModelMap.get(SDF.format(afterDate));
        ShiftModel shiftModel = shiftModelMap.get(SDF.format(vaApplyDate));
        if (preShiftModel != null && afterShiftModel != null && shiftModel != null){
            // isOffNoPlan为true时，取前一天班次的结束日期为开始日期，后一天班次的开始日期为结束日期
            Date preShiftStartDate;
            Date preShiftEndDate;
            if (preShiftModel.isOffNoPlan()){
                List<Date> offNoPlanShiftDate = getOffNoPlanShiftDate(preDate,attFileBoId);
                preShiftStartDate = offNoPlanShiftDate.get(0);
                preShiftEndDate = offNoPlanShiftDate.get(1);
            }else {
                preShiftStartDate = ShiftQueryUtil.getShiftStartDate(preShiftModel, preDate);
                preShiftEndDate = ShiftQueryUtil.getShiftEndDate(preShiftModel, preDate);
            }
            // 后一天班次开始、结束日期
            Date afterShiftStartDate;
            Date afterShiftEndDate;
            // isOffNoPlan为true时，取前一天班次的结束日期为开始日期，后一天班次的开始日期为结束日期
            if (afterShiftModel.isOffNoPlan()){
                List<Date> offNoPlanShiftDate = getOffNoPlanShiftDate(afterDate,attFileBoId);
                afterShiftStartDate = offNoPlanShiftDate.get(0);
                afterShiftEndDate = offNoPlanShiftDate.get(1);
            }else {
                afterShiftStartDate = ShiftQueryUtil.getShiftStartDate(afterShiftModel, afterDate);
                afterShiftEndDate = ShiftQueryUtil.getShiftEndDate(afterShiftModel, afterDate);
            }

            // 当天班次开始、结束日期
            Date shiftStartDate;
            Date shiftEndDate;
            // isOffNoPlan为true时，取前一天班次的结束日期为开始日期，后一天班次的开始日期为结束日期
            if (shiftModel.isOffNoPlan()){
                shiftStartDate = preShiftEndDate;
                shiftEndDate = afterShiftStartDate;
            }else {
                shiftStartDate = ShiftQueryUtil.getShiftStartDate(shiftModel, currentDate);
                shiftEndDate = ShiftQueryUtil.getShiftEndDate(shiftModel, currentDate);
            }

            logger.info("前一天班次的开始日期：{}，结束日期：{}",SDFTIME.format(preShiftStartDate),SDFTIME.format(preShiftEndDate));
            logger.info("当天班次的开始日期：{}，结束日期：{}",SDFTIME.format(shiftStartDate),SDFTIME.format(shiftEndDate));
            logger.info("后一天班次的开始日期：{}，结束日期：{}",SDFTIME.format(afterShiftStartDate),SDFTIME.format(afterShiftEndDate));

            // 休假日期落在前一天或者当天班次内
            if (vaApplyDate.after(preShiftStartDate) && vaApplyDate.before(preShiftEndDate)){
                standardDate = preDate;
            }else if(vaApplyDate.after(shiftStartDate) && vaApplyDate.before(shiftEndDate)){
                standardDate = currentDate;
            }
            // 是否在前一天和当天之间：开始取当天；结束取前一天
            if (!vaApplyDate.before(preShiftEndDate) && !vaApplyDate.after(shiftStartDate)) {
                standardDate = currentDate;
                if (isEndDate){
                    standardDate = preDate;
                }
            }
            // 是否在当天和后一天之间：开始取后一天，结束取当天
            if (!vaApplyDate.before(shiftEndDate) && !vaApplyDate.after(afterShiftStartDate)){
                standardDate = afterDate;
                if (isEndDate){
                    standardDate = currentDate;
                }
            }
        }
        return standardDate;
    }

    private static List<Date> getOffNoPlanShiftDate(Date date,long attFileBoId){
        List<Date> resultDateList = new ArrayList<>(2);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 前一天
        Date preDate = addDays(-1, date);
        // 后一天
        Date afterDate = addDays(1, date);
        // 班次信息
        Map<String, ShiftModel> shiftModelMap = ShiftQueryUtil.shiftTimesMap(attFileBoId, preDate, afterDate);
        ShiftModel preShiftModel = shiftModelMap.get(SDF.format(preDate));
        Date preShiftEndDate;
        if (preShiftModel == null || preShiftModel.isOffNoPlan()) {
            preShiftEndDate = calendar.getTime();
        }else {
            preShiftEndDate = ShiftQueryUtil.getShiftEndDate(preShiftModel, preDate);
        }
        // 后一天班次开始、结束日期
        ShiftModel afterShiftModel = shiftModelMap.get(SDF.format(afterDate));
        Date afterShiftStartDate;
        if (afterShiftModel.isOffNoPlan()){
            // 获取该日期的结束时间 (23:59:59.999)
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            afterShiftStartDate = calendar.getTime();
        }else {
            afterShiftStartDate = ShiftQueryUtil.getShiftStartDate(afterShiftModel, afterDate);
        }
        resultDateList.add(preShiftEndDate);
        resultDateList.add(afterShiftStartDate);
        return resultDateList;
    }

    public static Date addDays(int day, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(KDDateUtils.getSysTimeZone());
        calendar.setTime(date);
        calendar.add(5, day);
        Date newDate = calendar.getTime();
        return newDate;
    }

    /**
     * 获取日期整点
     *
     * @return
     */
    public static Date getDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
