package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.Image;
import kd.bos.form.control.Label;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Metadata ： tdkw_myhistory
 * @Description ：员服门户-我的历程
 * @author xxx
 * @Date ：2023/05/22
 * @Version: 3.0  需求变更2023年8月22号
 * #83520 PC端员工自助门户-我的历程取值逻辑调整
 * http://ones.xxx.com/project/#/team/JbjqrWit/task/DE84BgNqX5QsR9uK
 */
public class MyHistoryFormPlugin extends AbstractFormPlugin {

    private static final Log Logger = LogFactory.getLog(MyHistoryFormPlugin.class);
    public int zero = 0;
    public int one = 1;
    public int two = 2;
    public int three = 3;
    public String levelName = "tdkw_levelname";
    public String levelDate = "tdkw_leveldate";

    /**
     * 我的历程时间轴取值逻辑调整如下：
     * 首先，查询员工所有任职经历中“异动类型”字段是否有“晋升”，
     * 如果没有晋升记录，取最新一条主要任职对应的职层和开始日期；
     * 如果有晋升记录，则取最新的三条晋升记录中对应的职层和开始日期字段内容进行显示，
     * 假设晋升的记录不足3条，有几条晋升就显示几条。
     *
     * @param e
     */
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        Map<String, Long> personModelId = this.getPersonModelId();
        if (personModelId != null) {
            Long personId = personModelId.get("person");
            QFilter qFilter = new QFilter("person", QCP.equals, personId)
                    .and("iscurrentversion", QCP.equals, "1")
                    .and("businessstatus", QCP.equals, "1");
            DynamicObject perserlen = BusinessDataServiceHelper.loadSingle("hrpi_perserlen", "person,joincomdate", qFilter.toArray());
            if (perserlen != null) {
                setDays((Date) perserlen.get("joincomdate"));
            }
            setLevelVisible(zero);
            QFilter commonFilter = new QFilter("person", QCP.equals, personId);
            commonFilter.and("iscurrentversion", QCP.equals, "1");
            commonFilter.and("initstatus", QCP.equals, "2");

            QFilter promotionFilter = commonFilter.copy();
            //XY00001-入职，XY00002-转正，XY00004-晋升
            List<String> changetypeNumber = new ArrayList<>();
//            changetypeNumber.add("XY00001");
            // changetypeNumber.add("XY00002");
            changetypeNumber.add("XY00004");
//            promotionFilter.and("tdkw_changetype.number", QCP.in, changetypeNumber);
            Logger.info("HR自助门户-我的历程，过滤条件：" + promotionFilter);

            QFilter mainJobFilter = commonFilter.copy();
            mainJobFilter.and("isprimary", QCP.equals, "1");

            DynamicObject[] empposorgrels = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,startdate", promotionFilter.toArray(), "startdate desc", 3);
            List<DynamicObject> showList = new ArrayList<>();

            List<DynamicObject> advanceList = Arrays.stream(empposorgrels).collect(Collectors.toList());
            int size = empposorgrels.length;
            showList.addAll(advanceList);
            if (size == 0) {
                DynamicObject[] priEmp = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,startdate", mainJobFilter.toArray(), "startdate desc", 1);
                if (priEmp != null && priEmp.length > 0) {
                    showList.add(priEmp[0]);
                }
            } else if (size < 3) {
                mainJobFilter.and("startdate ", "<", empposorgrels[size - 1].get("startdate"));
                DynamicObject[] priEmp = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,startdate", mainJobFilter.toArray(), "startdate desc", 1);
                if (priEmp != null && priEmp.length > 0) {
                    showList.add(priEmp[0]);
                }
            }
            int length = showList.size();
            if (length < 3) {
                for (int i = 0; i < length; i++) {
                    if (i < 3) {
                        String level = "";
                        // TODO  缺少tdkw_ranks 字段职层 临时写死
//                        DynamicObject ranks = showList.get(i).getDynamicObject("tdkw_ranks");
//                        if (ranks != null) {
//                            ILocaleString name = ranks.getLocaleString("name");
//                            if (name != null) {
//                                level = name.getLocaleValue_zh_CN();
//                            }
//                        }
                        level = "工程师";
                        this.getModel().setValue(levelName + String.valueOf(i + 1), level);
                        this.getModel().setValue(levelDate + String.valueOf(i + 1), showList.get(i).get("startdate"));
                    }
                }
            } else {
                for (int i = 0; i < length; i++) {
                    String level = "";
                    if (i == 0) {
                        // TODO  缺少tdkw_ranks 字段职层 临时写死
//                        DynamicObject ranks = showList.get(i).getDynamicObject("tdkw_ranks");
//                        if (ranks != null) {
//                            ILocaleString name = ranks.getLocaleString("name");
//                            if (name != null) {
//                                level = name.getLocaleValue_zh_CN();
//                            }
//                        }
                        level = "专员";
                        this.getModel().setValue(levelName + String.valueOf(i + 1), level);
                        this.getModel().setValue(levelDate + String.valueOf(i + 1), showList.get(i).get("startdate"));
                    } else if (i == 1) {
                        // TODO  缺少tdkw_ranks 字段职层 临时写死
//                        DynamicObject ranks = showList.get(i).getDynamicObject("tdkw_ranks");
//                        if (ranks != null) {
//                            ILocaleString name = ranks.getLocaleString("name");
//                            if (name != null) {
//                                level = name.getLocaleValue_zh_CN();
//                            }
//                        }
                        level = "初级工程师";
                        this.getModel().setValue(levelName + String.valueOf(i + 2), level);
                        this.getModel().setValue(levelDate + String.valueOf(i + 2), showList.get(i).get("startdate"));
                    } else if (i == 2) {
                        // TODO  缺少tdkw_ranks 字段职层 临时写死
//                        DynamicObject ranks = showList.get(i).getDynamicObject("tdkw_ranks");
//                        if (ranks != null) {
//                            ILocaleString name = ranks.getLocaleString("name");
//                            if (name != null) {
//                                level = name.getLocaleValue_zh_CN();
//                            }
//                        }
                        level = "中级工程师";
                        this.getModel().setValue(levelName + String.valueOf(i), level);
                        this.getModel().setValue(levelDate + String.valueOf(i), showList.get(i).get("startdate"));
                    }
                }
            }

            setLevelVisible(length);


            //您已入党*年*月*天-中共党员
//            String tip = setJoinPartyDateTips(personId);
//            Image ccImage = this.getView().getControl("tdkw_ccimage");
//            ccImage.setTips(tip);
        }

    }

    /**
     * 设置悬浮提示-您已入党*年*月*天-中共党员
     *
     * @param personId
     * @return
     */
    public String setJoinPartyDateTips(Long personId) {
        String tip = "";

        QFilter perFilter = new QFilter("person", QCP.equals, personId);
        perFilter.and("iscurrentversion", QCP.equals, "1");
        perFilter.and("initstatus", QCP.equals, "2");
        //XY00001-中共党员
        perFilter.and("politicalstatus.number", QCP.equals, "XY00001");
        DynamicObject perInfo = BusinessDataServiceHelper.loadSingle("hrpi_perregion", "id,politicalstatus,joinpartydate", perFilter.toArray());
        if (perInfo != null) {
            Date joinPartyDate = perInfo.getDate("joinpartydate");
            if (joinPartyDate != null) {
                Instant instant = joinPartyDate.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();

                // atZone()方法返回在指定时区从此Instant生成的ZonedDateTime。
                LocalDate joinPartyLocalDate = instant.atZone(zoneId).toLocalDate();
                Period period = Period.between(joinPartyLocalDate, LocalDate.now());
                tip = "您已入党" + period.getYears() + "年" + period.getMonths() + "月" + period.getDays() + "天";
            }
        }
        return tip;
    }

    /**
     * 设置已入职年-月-日；分开计算-年月日
     *
     * @param startdate
     */
    public void setDays(Date startdate) {
//        GateWayUtils gateWayUtils = new GateWayUtils();
//        int year = gateWayUtils.getYear(startdate);
//        int month = gateWayUtils.getMonth(startdate);
//        int day = gateWayUtils.getDay(startdate);

        Period period = Period.between(startdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now());

        Label yearLabel = this.getControl("tdkw_year");
        yearLabel.setText(String.valueOf(period.getYears()));
        Label monthLabel = this.getControl("tdkw_month");
        monthLabel.setText(String.valueOf(period.getMonths()));
        Label dayLabel = this.getControl("tdkw_day");
        dayLabel.setText(String.valueOf(period.getDays()));
    }

    public void setLevelVisible(int length) {
        if (length == one) {
            this.getView().setVisible(Boolean.TRUE, "tdkw_flex1");
            this.getView().setVisible(Boolean.FALSE, "tdkw_flex2");
            this.getView().setVisible(Boolean.FALSE, "tdkw_flex3");
        } else if (length == two) {
            this.getView().setVisible(Boolean.TRUE, "tdkw_flex1");
            this.getView().setVisible(Boolean.TRUE, "tdkw_flex2");
            this.getView().setVisible(Boolean.FALSE, "tdkw_flex3");
        } else if (length >= three) {
            this.getView().setVisible(Boolean.TRUE, "tdkw_flex1");
            this.getView().setVisible(Boolean.TRUE, "tdkw_flex2");
            this.getView().setVisible(Boolean.TRUE, "tdkw_flex3");
        } else {
            this.getView().setVisible(Boolean.FALSE, "tdkw_flex1");
            this.getView().setVisible(Boolean.FALSE, "tdkw_flex2");
            this.getView().setVisible(Boolean.FALSE, "tdkw_flex3");
        }
    }

    /**
     * 获取当前用户的员工id、档案id等
     *
     * @return
     */
    private Map<String, Long> getPersonModelId() {
        Map<String, Object> personModelIdMap = (Map) HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonService", "getPersonModelId", new Object[0]);
        Map<String, Long> personModelInfo = personModelIdMap != null ? (Map) personModelIdMap.get("data") : null;
        return personModelInfo;
    }
}
