package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.IFormView;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.portal.util.SerializationUtils;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.StringUtils;
import kd.wtc.wtss.common.dto.mobilehome.CalendarGetDataModel;
import org.jetbrains.annotations.NotNull;
import tdkw.hrmp.hrobs.common.hrobs.pojo.AttPeriodItemStat;
import tdkw.hrmp.hrobs.common.hrobs.pojo.PerAttPeriod;
import tdkw.hrmp.hrobs.common.hrobs.util.AttendanceUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2023/11/22
 * @description
 */
public class ExceptionRecordFormPlugin extends AbstractFormPlugin {
    private static final Log LOGGER = LogFactory.getLog(ExceptionRecordFormPlugin.class);

    @Override
    public void customEvent(CustomEventArgs e) {
        super.customEvent(e);
        String key = e.getKey();
        String eventName = e.getEventName();
        if ("customcalendar".equals(key) && "getDateData".equals(eventName)) {
            // 获取当前登录用户的个人Id
            long attPersonId = AttendanceUtil.getAttPersonIdOfCurrentLoginUser();
            CalendarGetDataModel getDate = SerializationUtils.fromJsonString(e.getEventArgs(), CalendarGetDataModel.class);
            String chooseDay = getDate.getChooseDay();
            if (StringUtils.isNotEmpty(chooseDay)) {
                LOGGER.info("焦点日期：" + chooseDay);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date parse = sdf.parse(chooseDay);
                    Calendar cale = Calendar.getInstance();
                    cale.setTime(parse);
                    cale.set(Calendar.DAY_OF_MONTH, cale.getActualMaximum(Calendar.DAY_OF_MONTH));
                    cale.set(Calendar.HOUR_OF_DAY, cale.getActualMaximum(Calendar.HOUR_OF_DAY));
                    parse = cale.getTime();
                    //跨服务器节点实例化插件失败，请使用方法getViewNoPlugin来获取view
                    String parentPageId = this.getView().getFormShowParameter().getParentPageId();
                    IFormView parentView = this.getView().getViewNoPlugin(parentPageId);
                    // IFormView parentView = this.getView().getParentView();
                    if (parentView != null) {
                        // 月初
                        LOGGER.info("64月初：" + sdf.format(getDate(parse)));
                        parentView.getPageCache().put("startDate", sdf.format(getDate(parse)));
                        // 月末
                        LOGGER.info("67月末：" + sdf.format(parse));
                        parentView.getPageCache().put("endDate", sdf.format(parse));
                        // 异常天数
                        Integer newNumber = getNewNumber(attPersonId, cale.getTime());
                        LOGGER.info("异常天数：" + newNumber);

                        // 获取当前考勤档案信息，获取考勤档案需要指定日期，因为档案是时序性对象，按当前日期获取
                        Map<String, String> attFile = AttendanceUtil.getAttFile(attPersonId, parse);
                        // 获取假勤门户方案id
                        long schemeId = AttendanceUtil.getSelfServiceSchemeId(attFile);
                        // 获取当前人员考勤期间,先获取全量人员考勤期间，再获取当前的
                        PerAttPeriod currentAttPeriod = AttendanceUtil.getPerAttPeriod(attPersonId, parse);
                        if (currentAttPeriod == null) {
                            LOGGER.error("该人员本期考勤期间未存在数据！");
                            parentView.getModel().setValue("tdkw_data1", 0);
                            parentView.getModel().setValue("tdkw_data2", 0);
                            parentView.updateView("tdkw_data1");
                            parentView.updateView("tdkw_data2");
                            this.getView().sendFormAction(parentView);
                            return;
                        }
                        // 获取期间汇总数据源相关配置和统计值
                        List<AttPeriodItemStat> attPeriodItemStats = AttendanceUtil.getAttPeriodItemStat(attPersonId, "A", currentAttPeriod.getId(), schemeId, 2);
                        LOGGER.info("attPeriodItemStats" + attPeriodItemStats);

                        parentView.getModel().setValue("tdkw_data1", newNumber);
                        parentView.getModel().setValue("tdkw_data2", attPeriodItemStats.get(1).getAttItemValue());
                        parentView.updateView("tdkw_data1");
                        parentView.updateView("tdkw_data2");
                        this.getView().sendFormAction(parentView);
                    }
                } catch (ParseException ex) {
                    LOGGER.info("日期解析异常");
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private static Integer getNewNumber(long attPersonId, Date now) {
        // 本人
        QFilter qFilter = new QFilter("personid", QCP.equals, attPersonId);
        qFilter.and("recorddate", QCP.less_equals, now);
        // 获取月初
        Date start = getDate(now);
        // 本月
        qFilter.and("recorddate", QCP.large_equals, start);
        // 异常类型
        qFilter.and("exattributeid.number", QCP.in, new String[]{"1010_S", "1020_S", "1030_S", "1040_S"});
        // 数据类别 单独处理 只取有结果的 详见 kd.wtc.wtte.formplugin.web.ex.ExRecordList 的datatype逻辑
        qFilter.and("attitemvid", QCP.large_than, 0L);
        DynamicObject[] load = BusinessDataServiceHelper.load("wtte_exrecord", "id", qFilter.toArray());
        if (null == load) {
            return 0;
        } else {
            return load.length;
        }
    }

    @NotNull
    private static Date getDate(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        // calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        // calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        // calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date start = calendar.getTime();
        return start;
    }
}
