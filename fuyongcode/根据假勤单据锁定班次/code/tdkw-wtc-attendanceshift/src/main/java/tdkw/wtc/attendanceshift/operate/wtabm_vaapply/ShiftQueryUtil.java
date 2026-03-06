package tdkw.wtc.attendanceshift.operate.wtabm_vaapply;

import kd.bos.dataentity.Tuple;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.result.OpenApiResult;
import kd.bos.openapi.common.util.OpenApiSdkUtil;
import kd.wtc.wts.mservice.openapi.roster.model.*;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Author: shichang
 * Description: 班次查询工具类
 */
public class ShiftQueryUtil {
    private static final Log logger = LogFactory.getLog(ShiftQueryUtil.class);

    public static ShiftModel loadTodayShiftTimes(long attFileBoId) {

        String url = "/v2/wts/roster/getRosterData";
        Map<String, Object> request = new HashMap<>();
        RosterQueryReq rosterQueryReq = new RosterQueryReq();

        List<Long> boIds = new ArrayList<>();
        boIds.add(attFileBoId);
        rosterQueryReq.setAttFileBoIds(boIds);
        //开始时间与结束时间都是今天，只查今天的排班
        LocalDate now = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = now.format(formatter);

        rosterQueryReq.setStartDate(startDate);
        rosterQueryReq.setEndDate(startDate);
        rosterQueryReq.setGetShiftDetail(true);
        request.put("rosterQueryReq", rosterQueryReq);

        OpenApiResult apiResult;
        try {
            apiResult = OpenApiSdkUtil.invoke(url, request);
        } catch (Exception e) {
            logger.error("调用接口获取排班发生异常：" + rosterQueryReq, e);
            return null;
        }

        if (apiResult.isStatus()) {
            RosterQueryResult queryResult = (RosterQueryResult) apiResult.getData();
            Map<Long, ShiftModel> shiftModels = queryResult.getShiftModelMap();

            List<RosterQueryPersonModel> personModels = queryResult.getPersonModels();
            for (RosterQueryPersonModel personModel : personModels) {
                if (attFileBoId == personModel.getAttFileBoId()) {
                    List<RosterDayModel> rosterDayModels = personModel.getRosterDayModels();
                    //获取排班
                    for (RosterDayModel dayModel : rosterDayModels) {
                        Date rosterDate = dayModel.getRosterDate();
                        String formatted = rosterDate.toInstant().atZone(ZoneId.systemDefault()).format(formatter);
                        if (formatted.equals(startDate)) {
                            Long shiftBoId = dayModel.getShiftBoId();
                            return shiftModels.get(shiftBoId);
                        }
                    }
                }
            }
        } else {
            logger.error("调用接口" + url + "获取排班信息失败：" + apiResult.getMessage());
        }

        return null;
    }

    /**
     * 取指定日期班次
     */
    public static ShiftModel loadSpecifyDateShiftTimes(long attFileBoId, Date date) {
        String url = "/v2/wts/roster/getRosterData";
        Map<String, Object> request = new HashMap<>();
        RosterQueryReq rosterQueryReq = new RosterQueryReq();

        List<Long> boIds = new ArrayList<>();
        boIds.add(attFileBoId);
        rosterQueryReq.setAttFileBoIds(boIds);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format(date);

        rosterQueryReq.setStartDate(startDate);
        rosterQueryReq.setEndDate(startDate);
        rosterQueryReq.setGetShiftDetail(true);
        request.put("rosterQueryReq", rosterQueryReq);

        OpenApiResult apiResult;
        try {
            apiResult = OpenApiSdkUtil.invoke(url, request);
        } catch (Exception e) {
            logger.error("调用接口获取排班发生异常：" + rosterQueryReq, e);
            return null;
        }

        if (apiResult.isStatus()) {
            RosterQueryResult queryResult = (RosterQueryResult) apiResult.getData();
            Map<Long, ShiftModel> shiftModels = queryResult.getShiftModelMap();

            List<RosterQueryPersonModel> personModels = queryResult.getPersonModels();
            for (RosterQueryPersonModel personModel : personModels) {
                if (attFileBoId == personModel.getAttFileBoId()) {
                    List<RosterDayModel> rosterDayModels = personModel.getRosterDayModels();
                    //获取排班
                    for (RosterDayModel dayModel : rosterDayModels) {
                        Date rosterDate = dayModel.getRosterDate();
                        String formatted = sdf.format(rosterDate);
                        if (formatted.equals(startDate)) {
                            Long shiftBoId = dayModel.getShiftBoId();
                            return shiftModels.get(shiftBoId);
                        }
                    }
                }
            }
        } else {
            logger.error("调用接口" + url + "获取排班信息失败：" + apiResult.getMessage());
        }

        return null;
    }

    /**
     * 取指定日期班次
     */
    public static Map<String, ShiftModel> shiftTimesMap(long attFileBoId, Date sdate, Date edate) {
        String url = "/v2/wts/roster/getRosterData";
        Map<String, Object> request = new HashMap<>();
        RosterQueryReq rosterQueryReq = new RosterQueryReq();

        List<Long> boIds = new ArrayList<>();
        boIds.add(attFileBoId);
        rosterQueryReq.setAttFileBoIds(boIds);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format(sdate);
        String endDate = sdf.format(edate);
        rosterQueryReq.setStartDate(startDate);
        rosterQueryReq.setEndDate(endDate);
        rosterQueryReq.setGetShiftDetail(true);
        request.put("rosterQueryReq", rosterQueryReq);

        OpenApiResult apiResult;
        try {
            apiResult = OpenApiSdkUtil.invoke(url, request);
        } catch (Exception e) {
            logger.error("调用接口获取排班发生异常：" + rosterQueryReq, e);
            return null;
        }

        if (apiResult.isStatus()) {
            RosterQueryResult queryResult = (RosterQueryResult) apiResult.getData();
            Map<Long, ShiftModel> shiftModels = queryResult.getShiftModelMap();

            Map<String, ShiftModel> map = new HashMap<>();
            List<RosterQueryPersonModel> personModels = queryResult.getPersonModels();
            for (RosterQueryPersonModel personModel : personModels) {
                if (attFileBoId == personModel.getAttFileBoId()) {
                    List<RosterDayModel> rosterDayModels = personModel.getRosterDayModels();
                    //获取排班
                    for (RosterDayModel dayModel : rosterDayModels) {
                        Date rosterDate = dayModel.getRosterDate();
                        String formatted = sdf.format(rosterDate);
                        Long shiftBoId = dayModel.getShiftBoId();
                        ShiftModel shiftModel = shiftModels.get(shiftBoId);
                        map.put(formatted, shiftModel);
                    }
                    return map;
                }
            }
        } else {
            logger.error("调用接口" + url + "获取排班信息失败：" + apiResult.getMessage());
        }

        return null;
    }

    public static Tuple<RosterDayModel, ShiftModel> loadTodayShift(long attFileBoId) {

        String url = "/v2/wts/roster/getRosterData";
        Map<String, Object> request = new HashMap<>();
        RosterQueryReq rosterQueryReq = new RosterQueryReq();

        List<Long> boIds = new ArrayList<>();
        boIds.add(attFileBoId);
        rosterQueryReq.setAttFileBoIds(boIds);
        //开始时间与结束时间都是今天，只查今天的排班
        LocalDate now = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = now.format(formatter);

        rosterQueryReq.setStartDate(startDate);
        rosterQueryReq.setEndDate(startDate);
        rosterQueryReq.setGetShiftDetail(true);
        request.put("rosterQueryReq", rosterQueryReq);

        OpenApiResult apiResult;
        try {
            apiResult = OpenApiSdkUtil.invoke(url, request);
        } catch (Exception e) {
            logger.error("调用接口获取排班发生异常：" + rosterQueryReq, e);
            return null;
        }

        if (apiResult.isStatus()) {
            RosterQueryResult queryResult = (RosterQueryResult) apiResult.getData();
            Map<Long, ShiftModel> shiftModels = queryResult.getShiftModelMap();

            List<RosterQueryPersonModel> personModels = queryResult.getPersonModels();
            for (RosterQueryPersonModel personModel : personModels) {
                if (attFileBoId == personModel.getAttFileBoId()) {
                    List<RosterDayModel> rosterDayModels = personModel.getRosterDayModels();
                    //获取排班
                    for (RosterDayModel dayModel : rosterDayModels) {
                        Date rosterDate = dayModel.getRosterDate();
                        String formatted = rosterDate.toInstant().atZone(ZoneId.systemDefault()).format(formatter);
                        if (formatted.equals(startDate)) {
                            Long shiftBoId = dayModel.getShiftBoId();
                            return new Tuple<>(dayModel, shiftModels.get(shiftBoId));
                        }
                    }
                }
            }
        } else {
            logger.error("调用接口" + url + "获取排班信息失败：" + apiResult.getMessage());
        }

        return null;
    }

    @NotNull
    public static Date getShiftStartDate(ShiftModel shiftModel) {
        Date startDate;
        String refStartDay = shiftModel.getRefStartDay();
        int shiftStart = shiftModel.getShiftStart();
        //D：当日；C：次日
        if ("D".equals(refStartDay)) {
            //今天的开始时间
            startDate = Date.from(LocalTime.ofSecondOfDay(0).plusSeconds(shiftStart).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            startDate = Date.from(LocalDate.now().atStartOfDay().toLocalTime().plusSeconds(shiftStart).atDate(LocalDate.now().minusDays(-1)).atZone(ZoneId.systemDefault()).toInstant());
        }
        return startDate;
    }

    @NotNull
    public static Date getShiftStartDate(ShiftModel shiftModel, Date date) {
        Instant instant = date.toInstant();
        LocalTime localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date startDate;
        String refStartDay = shiftModel.getRefStartDay();
        int shiftStart = shiftModel.getShiftStart();
        //D：当日；C：次日
        if ("D".equals(refStartDay)) {
            //今天的开始时间
            startDate = Date.from(LocalTime.ofSecondOfDay(0).plusSeconds(shiftStart).atDate(localDate).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            startDate = Date.from(localTime.plusSeconds(shiftStart).atDate(localDate.minusDays(-1)).atZone(ZoneId.systemDefault()).toInstant());
        }
        return startDate;
    }

    public static Date getShiftStartDateOrDefault(ShiftModel shiftModel, Date date) {
        String refStartDay = shiftModel.getRefStartDay();
        int shiftStart = shiftModel.getShiftStart();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = 8;
        int minutes = 0;
        int remainingSeconds = 0;
        //D：当日；C：次日
        if ("D".equals(refStartDay)) {
            hour = shiftStart / 60 / 60;
            minutes = shiftStart / 60 % 60;
            remainingSeconds = shiftStart % 60;
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, remainingSeconds);
        date = calendar.getTime();
        return date;
    }

    @NotNull
    public static Date getShiftEndDate(ShiftModel shiftModel) {
        String refEndDay = shiftModel.getRefEndDay();
        int shiftEnd = shiftModel.getShiftEnd();
        Date endDate;
        if ("D".equals(refEndDay)) {
            //今天的开始时间
            endDate = Date.from(LocalTime.ofSecondOfDay(0).plusSeconds(shiftEnd).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            endDate = Date.from(LocalDate.now().atStartOfDay().toLocalTime().plusSeconds(shiftEnd).atDate(LocalDate.now().minusDays(-1)).atZone(ZoneId.systemDefault()).toInstant());
        }
        return endDate;
    }

    @NotNull
    public static Date getShiftEndDate(ShiftModel shiftModel, Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String refEndDay = shiftModel.getRefEndDay();
        int shiftEnd = shiftModel.getShiftEnd();
        Date endDate;
        if ("D".equals(refEndDay)) {
            //今天的开始时间
            endDate = Date.from(LocalTime.ofSecondOfDay(0).plusSeconds(shiftEnd).atDate(localDate).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            endDate = Date.from(localDate.atStartOfDay().toLocalTime().plusSeconds(shiftEnd).atDate(localDate.minusDays(-1)).atZone(ZoneId.systemDefault()).toInstant());
        }
        return endDate;
    }

    public static Date getShiftEndDateOrDefault(ShiftModel shiftModel, Date date) {
        String refEndDay = shiftModel.getRefEndDay();
        int shiftEnd = shiftModel.getShiftEnd();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = 17;
        int minutes = 30;
        int remainingSeconds = 0;
        //D：当日；C：次日
        if ("D".equals(refEndDay)) {
            hour = shiftEnd / 60 / 60;
            minutes = shiftEnd / 60 % 60;
            remainingSeconds = shiftEnd % 60;
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, remainingSeconds);
        date = calendar.getTime();
        return date;
    }

    /**
     * 取指定日期班次
     */
    public static Tuple<RosterDayModel, ShiftModel> loadSpecifyDateShift(long attFileBoId, Date date) {
        String url = "/v2/wts/roster/getRosterData";
        Map<String, Object> request = new HashMap<>();
        RosterQueryReq rosterQueryReq = new RosterQueryReq();

        List<Long> boIds = new ArrayList<>();
        boIds.add(attFileBoId);
        rosterQueryReq.setAttFileBoIds(boIds);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format(date);

        rosterQueryReq.setStartDate(startDate);
        rosterQueryReq.setEndDate(startDate);
        rosterQueryReq.setGetShiftDetail(true);
        request.put("rosterQueryReq", rosterQueryReq);

        OpenApiResult apiResult;
        try {
            apiResult = OpenApiSdkUtil.invoke(url, request);
        } catch (Exception e) {
            logger.error("调用接口获取排班发生异常：" + rosterQueryReq, e);
            return null;
        }

        if (apiResult.isStatus()) {
            RosterQueryResult queryResult = (RosterQueryResult) apiResult.getData();
            Map<Long, ShiftModel> shiftModels = queryResult.getShiftModelMap();

            List<RosterQueryPersonModel> personModels = queryResult.getPersonModels();
            for (RosterQueryPersonModel personModel : personModels) {
                if (attFileBoId == personModel.getAttFileBoId()) {
                    List<RosterDayModel> rosterDayModels = personModel.getRosterDayModels();
                    //获取排班
                    for (RosterDayModel dayModel : rosterDayModels) {
                        Date rosterDate = dayModel.getRosterDate();
                        String formatted = sdf.format(rosterDate);
                        if (formatted.equals(startDate)) {
                            Long shiftBoId = dayModel.getShiftBoId();
                            return new Tuple<>(dayModel, shiftModels.get(shiftBoId));
                        }
                    }
                }
            }
        } else {
            logger.error("调用接口" + url + "获取排班信息失败：" + apiResult.getMessage());
        }

        return null;
    }

    /**
     * 取指定日期班次时段开始时间
     */
    public static Date getShiftDetailModelStartDate(ShiftDetailModel shiftModel, Date date) {
        Instant instant = date.toInstant();
        LocalTime localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date startDate;
        String refStartDay = shiftModel.getRefStartTime();
        int shiftStart = shiftModel.getStartTime();
        //D：当日；C：次日
        if ("D".equals(refStartDay)) {
            //今天的开始时间
            startDate = Date.from(LocalTime.ofSecondOfDay(0).plusSeconds(shiftStart).atDate(localDate).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            startDate = Date.from(localTime.plusSeconds(shiftStart).atDate(localDate.minusDays(-1)).atZone(ZoneId.systemDefault()).toInstant());
        }
        return startDate;
    }

    /**
     * 取指定日期班次时段结束时间
     */
    public static Date getShiftDetailModelEndDate(ShiftDetailModel shiftModel, Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String refEndDay = shiftModel.getRefEndTime();
        int shiftEnd = shiftModel.getEndTime();
        Date endDate;
        if ("D".equals(refEndDay)) {
            //今天的开始时间
            endDate = Date.from(LocalTime.ofSecondOfDay(0).plusSeconds(shiftEnd).atDate(localDate).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            endDate = Date.from(localDate.atStartOfDay().toLocalTime().plusSeconds(shiftEnd).atDate(localDate.minusDays(-1)).atZone(ZoneId.systemDefault()).toInstant());
        }
        return endDate;
    }

}
