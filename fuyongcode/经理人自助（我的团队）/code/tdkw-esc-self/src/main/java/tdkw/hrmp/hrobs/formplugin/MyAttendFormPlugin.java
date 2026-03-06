package tdkw.hrmp.hrobs.formplugin;

import kd.bos.form.control.Label;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.sdk.wtc.wtpm.business.punchcard.WTPMPunchCardHelper;
import kd.sdk.wtc.wtte.business.exrecord.WTTEExRecordHelper;
import org.apache.commons.lang.time.DateUtils;
import org.apache.fop.util.ListUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import static kd.hr.haos.common.util.OrgDateTimeUtil.getStartOfDay;

/**
 * @Metadata ： tdkw_myattend1
 * @Description ：门户- 我的考勤信息
 * @author xxx
 * @Date ：2023/05/25
 * @Version: 1.0
 */
public class MyAttendFormPlugin extends AbstractFormPlugin {


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        this.getView().setVisible(false, "tdkw_belate2", "tdkw_leaveearly2", "tdkw_absenteeism2", "tdkw_lackcard2", "tdkw_normal2", "tdkw_notcheckin2");
        this.getView().setVisible(false, "tdkw_belate1", "tdkw_leaveearly1", "tdkw_absenteeism1", "tdkw_lackcard1", "tdkw_normal1", "tdkw_notcheckin1");

        Map<String, Long> personModelId = this.getPersonModelId();
        if (personModelId != null) {
            //设置当前日期、昨日、明日
            setCurDate();
            //设置当前日期、昨日、明日班次
        //    setShift(personModelId);
        }

    }


    /**
     * @Describe 设置当前日、昨日、明日
     */
    public void setCurDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        Date yesterday = DateUtils.addDays(today, -1);
        Date tomorrow = DateUtils.addDays(today, 1);
        String today_s = "(" + sdf.format(today) + ")";
        String yesterday_s = "(" + sdf.format(yesterday) + ")";
        String tomorrow_s = "(" + sdf.format(tomorrow) + ")";
        Label today_l = this.getControl("tdkw_today");
        Label yesterday_l = this.getControl("tdkw_yesterday");
        Label tomorrow_l = this.getControl("tdkw_tomorrow");
        today_l.setText(today_s);
        yesterday_l.setText(yesterday_s);
        tomorrow_l.setText(tomorrow_s);
    }

//    /**
//     * @Describe 设置当前日期、昨日、明日班次
//     */
//    public void setShift(Map<String, Long> personModelId) {
//
//        Long personId = personModelId.get("person");
//        Date today = new Date();
//        Date yesterday = DateUtils.addDays(today, -1);
//        Date tomorrow = DateUtils.addDays(today, 1);
//        Map<Date, Map<String, Object>> punchCardDetail = WTPMPunchCardHelper.getPunchCardDetail(personId, getStartOfDay(yesterday), getEndOfDay(tomorrow));
//
//        //今日班次
//        setClass("tdkw_todayclass", punchCardDetail, getStartOfDay(today));
//        //昨日班次
//        setYesterdayClass(punchCardDetail, getStartOfDay(yesterday));
//        //明日班次
//        setClass("tdkw_tomorrowclass", punchCardDetail, getStartOfDay(tomorrow));
//        //
//        setShowLabel(punchCardDetail, personModelId);
//
//
//    }

//    /**
//     * 设置考勤班次
//     *
//     * @param labelKey        班次标签标识
//     * @param punchCardDetail 获取到的信息
//     */
//    public void setClass(String labelKey, Map<Date, Map<String, Object>> punchCardDetail, Date date) {
//        Label tomorrowClass = this.getControl(labelKey);
//
//        if (punchCardDetail.keySet().contains(getStartOfDay(date))) {
//            Map<String, Object> tomorrowMap = punchCardDetail.get(getStartOfDay(date));
//            if (tomorrowMap.keySet().contains("shift")) {
//                List<Map<String, Date>> shift = (List<Map<String, Date>>) tomorrowMap.get("shift");
//                List<Date> start = new ArrayList<>();
//                List<Date> end = new ArrayList<>();
//                for (Map<String, Date> stringDateMap : shift) {
//                    for (String s : stringDateMap.keySet()) {
//                        if (StringUtils.equals(s, "start")) {
//                            start.add(stringDateMap.get(s));
//                        }
//                        if (StringUtils.equals(s, "end")) {
//                            end.add(stringDateMap.get(s));
//                        }
//                    }
//                }
//
//                String[] startDate = String.valueOf(Collections.min(start)).split(" ");
//                String[] endDate = String.valueOf(Collections.max(end)).split(" ");
//                if (startDate != null && endDate != null) {
//                    tomorrowClass.setText("行政班次：" + startDate[1].substring(0, 5) + "-" + endDate[1].substring(0, 5));
//                } else {
//                    tomorrowClass.setText("行政班次：无");
//                }
//            } else {
//                tomorrowClass.setText("行政班次：无");
//            }
//        } else {
//            tomorrowClass.setText("行政班次：无");
//        }
//    }
//
//    /**
//     * 设置昨天考勤班次
//     *
//     * @param punchCardDetail 获取到的信息
//     * @param date            昨天日期
//     */
//    public void setYesterdayClass(Map<Date, Map<String, Object>> punchCardDetail, Date date) {
//
//        if (punchCardDetail.keySet().contains(getStartOfDay(date))) {
//            Map<String, Object> tomorrowMap = punchCardDetail.get(getStartOfDay(date));
//            if (tomorrowMap.keySet().contains("shift")) {
//                List<Map<String, Date>> shift = (List<Map<String, Date>>) tomorrowMap.get("shift");
//                List<Date> start = new ArrayList<>();
//                List<Date> end = new ArrayList<>();
//                for (Map<String, Date> stringDateMap : shift) {
//                    for (String s : stringDateMap.keySet()) {
//                        if (StringUtils.equals(s, "start")) {
//                            start.add(stringDateMap.get(s));
//                        }
//                        if (StringUtils.equals(s, "end")) {
//                            end.add(stringDateMap.get(s));
//                        }
//                    }
//                }
//                if(start!=null){
//                    Object startDate = Collections.min(start);
//                    if (startDate != null ) {
//                        this.getModel().setValue("tdkw_yesclassstart", startDate);
//                    }
//                }
//                if(end!=null){
//                    Object endDate = Collections.max(end);
//                    if (endDate != null ) {
//                        this.getModel().setValue("tdkw_yesclassend", endDate);
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 根据考勤，控制显示标签
//     * 考勤方式，多次打卡、一次打卡、不打卡
//     * type,1-多次打卡、2-一次打卡、3-不打卡
//     *
//     * @param punchCardDetail
//     * @param personModelId
//     */
//    public void setShowLabel(Map<Date, Map<String, Object>> punchCardDetail, Map<String, Long> personModelId)  {
//        Date today = new Date();
//        Date yesterday = DateUtils.addDays(today, -1);
//
//        if (punchCardDetail.keySet().contains(getStartOfDay(today))) {
//            Map<String, Object> todayMap = punchCardDetail.get(getStartOfDay(today));
//            if (todayMap.keySet().contains("type")) {
//                String type = (String) todayMap.get("type");
//                if (StringUtils.equals("3", type)) {
//                    this.getView().setVisible(true, "tdkw_notcheckin2");
//                }
//                if (StringUtils.equals("2", type)) {
//                    Date sign = (Date) todayMap.get("sign");
//                    if (sign != null) {
//                        this.getView().setVisible(true, "tdkw_normal2");
//                    } else {
//                        this.getView().setVisible(true, "tdkw_lackcard2");
//                    }
//                }
//                if (StringUtils.equals("1", type)) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//                    List<Map<String, String>> getAttExInfo = WTTEExRecordHelper.getAttExInfo(personModelId.get("person"), getStartOfDay(today).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), getExs());
//                    if (getAttExInfo.size()>0) {
//                        for (Map<String, String> item : getAttExInfo) {
//                            if (item.containsKey("exType")) {
//                                String exType = item.get("exType");
//
//                                if (StringUtils.equals("1320384356908327936", exType)) {
//                                    this.getView().setVisible(true, "tdkw_lackcard2");
//                                } else if (StringUtils.equals("1320384239123833856", exType)) {
//                                    this.getView().setVisible(true, "tdkw_absenteeism2");
//                                } else if (StringUtils.equals("1320384079815828480", exType)) {
//                                    this.getView().setVisible(true, "tdkw_leaveearly2");
//                                } else if (StringUtils.equals("1320383951981782016", exType)) {
//                                    this.getView().setVisible(true, "tdkw_belate2");
//                                } else {
//                                    this.getView().setVisible(true, "tdkw_normal2");
//                                }
//                            }
//                        }
//                    } else {
//                        this.getView().setVisible(true, "tdkw_normal2");
//                    }
//                }
//            }
//        }
//
//        if (punchCardDetail.keySet().contains(getStartOfDay(yesterday))) {
//            Map<String, Object> todayMap = punchCardDetail.get(getStartOfDay(yesterday));
//            if (todayMap.keySet().contains("type")) {
//                String type = (String) todayMap.get("type");
//                if (StringUtils.equals("3", type)) {
//                    this.getView().setVisible(true, "tdkw_notcheckin1");
//                    setProgress(50, 51, 100, 100);
//                }
//                if (StringUtils.equals("2", type)) {
//                    Object sign = todayMap.get("sign");
//                    if (sign != null) {
//                        setProgress(50, 51, 100, 100);
//                        this.getView().setVisible(true, "tdkw_normal1");
//                    } else {
//                        this.getView().setVisible(true, "tdkw_absenteeism1");
//                    }
//                }
//                if (StringUtils.equals("1", type)) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//                    List<Map<String, String>> getAttExInfo = WTTEExRecordHelper.getAttExInfo(personModelId.get("person"),getStartOfDay(yesterday).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), getExs());
//                    if (getAttExInfo.size()>0) {
//                        for (Map<String, String> item : getAttExInfo) {
//                            if (item.containsKey("exType")) {
//                                String exType = item.get("exType");
//                                if (StringUtils.equals("1320384356908327936", exType)) {
//                                    this.getView().setVisible(true, "tdkw_lackcard1");
//                                } else if (StringUtils.equals("1320384239123833856", exType)) {
//                                    this.getView().setVisible(true, "tdkw_absenteeism1");
//                                } else if (StringUtils.equals("1320384079815828480", exType)) {
//                                    this.getView().setVisible(true, "tdkw_leaveearly1");
//                                } else if (StringUtils.equals("1320383951981782016", exType)) {
//                                    this.getView().setVisible(true, "tdkw_belate1");
//                                } else {
//                                    this.getView().setVisible(true, "tdkw_normal1");
//                                }
//                            }
//                        }
//                    } else {
//                        this.getView().setVisible(true, "tdkw_normal1");
//                    }
//                    SimpleDateFormat sdfYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    //打卡详情-赋值给实际上下班时间字段
//                    List<Map<String, Object>> sign = (List<Map<String, Object>>) todayMap.get("sign");
//                    if(sign!=null){
//                        List<Date> start = new ArrayList<>();
//                        List<Date> end = new ArrayList<>();
//
//                        for (Map<String, Object> stringDateMap : sign) {
//                            for (String s : stringDateMap.keySet()) {
//                                if (StringUtils.equals(s, "signon")) {
//                                    try {
//                                        start.add(sdfYMDHMS.parse((String) stringDateMap.get(s)));
//                                    }
//                                    catch (ParseException e){
//                                    }
//                                }
//                                if (StringUtils.equals(s, "signoff")) {
//                                    try {
//                                        end.add(sdfYMDHMS.parse((String) stringDateMap.get(s)));
//                                    }
//                                    catch (ParseException e){
//                                    }
//                                }
//                            }
//                        }
//                        if(start.size()>0&&end.size()>0){
//                            Object startDate = Collections.min(start);
//                            Object endDate = Collections.max(end);
//                            this.getModel().setValue("tdkw_realtime1", startDate);
//                            this.getModel().setValue("tdkw_realtime2", endDate);
//                            setProgressPercent();
//
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public void setProgressPercent() {
//        Date realStartDate = (Date) this.getModel().getValue("tdkw_realtime1");
//        Date realEndDate = (Date) this.getModel().getValue("tdkw_realtime2");
//        Date startDate = (Date) this.getModel().getValue("tdkw_yesclassstart");
//        Date endDate = (Date) this.getModel().getValue("tdkw_yesclassend");
//        long standard = endDate.getTime() - startDate.getTime();
//        int standardMin = (int) (standard / (1000 * 60));
//        //早退打卡
//        if (realStartDate.compareTo(startDate)<1 && realEndDate.compareTo(endDate)==-1) {
//            long real = realEndDate.getTime() - startDate.getTime();
//            int realMin = (int) (real / (1000 * 60)) * 100;
//            int result = realMin / standardMin;
//            setProgress(result, 101 - result, 100, 0);
//            this.getView().updateControlMetadata("tdkw_realtime1", blueColor());
//            this.getView().updateControlMetadata("tdkw_realtime2", redColor());
//        }
//        //正常打卡
//        if (realStartDate.compareTo(startDate)<1 && realEndDate.compareTo(endDate)>-1) {
//            setProgress(50, 51, 100, 100);
//            this.getView().updateControlMetadata("tdkw_realtime1", blueColor());
//            this.getView().updateControlMetadata("tdkw_realtime2", blueColor());
//        }
//        //早退+迟到打卡
//        if (realStartDate.compareTo(startDate)==1 && realEndDate.compareTo(endDate)==-1) {
//            long real = realStartDate.getTime() - startDate.getTime();
//            int realMin = (int) (real / (1000 * 60)) * 100;
//            int result = realMin / standardMin;
//            long realEnd = startDate.getTime() - realEndDate.getTime();
//            int realMinEnd = (int) (realEnd / (1000 * 60)) * 100;
//            int resultEnd = realMinEnd / standardMin;
//            setProgress(result, 101 - result, result * 2, resultEnd * 2);
//            this.getView().updateControlMetadata("tdkw_realtime1", redColor());
//            this.getView().updateControlMetadata("tdkw_realtime2", redColor());
//        }
//        //迟到打卡
//        if (realStartDate.compareTo(startDate)==1 && realEndDate.compareTo(endDate)>-1) {
//            long real = realStartDate.getTime() - startDate.getTime();
//            int realMin = (int) (real / (1000 * 60)) * 100;
//            int result = realMin / standardMin;
//            setProgress(result, 101 - result, 0, 100);
//            this.getView().updateControlMetadata("tdkw_realtime1", redColor());
//            this.getView().updateControlMetadata("tdkw_realtime2", blueColor());
//        }
//    }
//
//    public void setProgress(int p1, int p2, int s1, int s2) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put(ClientProperties.Width, new LocaleString(p1 + "%"));
//        this.getView().updateControlMetadata("tdkw_progress1", map1);
//
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put(ClientProperties.Width, new LocaleString(p2 + "%"));
//        this.getView().updateControlMetadata("tdkw_progress2", map2);
//
//        ProgressBar bar1 = this.getView().getControl("tdkw_progress1");
//        IClientViewProxy proxy1 = this.getView().getService(IClientViewProxy.class);
//        proxy1.setFieldProperty(bar1.getKey(), ClientProperties.Percent, s1);
//        bar1.start();
//
//        ProgressBar bar2 = this.getView().getControl("tdkw_progress2");
//        IClientViewProxy proxy2 = this.getView().getService(IClientViewProxy.class);
//        proxy2.setFieldProperty(bar2.getKey(), ClientProperties.Percent, s2);
//        bar2.start();
//    }

    private Map<String, Long> getPersonModelId() {
        Map<String, Object> personModelIdMap = (Map) HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonService", "getPersonModelId", new Object[0]);
        Map<String, Long> personModelInfo = personModelIdMap != null ? (Map) personModelIdMap.get("data") : null;
        return personModelInfo;
    }

}
