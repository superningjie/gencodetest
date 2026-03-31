package tdkw.hrmp.hrobs.formplugin;

import kd.bos.filter.FilterColumn;
import kd.bos.form.events.FilterContainerInitArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.ListShowParameter;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/7/25 16:02
 * @Description 异常记录 列表插件
 * @Demander xxx
 * @Document #78609 PC端员工自助门户-考勤日历-异常次数需过滤展示当前登陆账号数据 http://ones.xxx.com/project/#/team/JbjqrWit/task/DE84BgNqTg1kTTyo
 * @Basedata tdkw_wtte_exrecord_ext
 * @Version 1.0
 **/
public class ExRecordListPlugin extends AbstractListPlugin {
    private static final Log LOGGER = LogFactory.getLog(ExRecordListPlugin.class);

    @Override
    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);
        ListShowParameter listShowParameter = (ListShowParameter) this.getView().getFormShowParameter();
        String attendance = listShowParameter.getCustomParam("attendance");
        if (StringUtils.equals("attendance", attendance)) {
            this.getView().setVisible(false, "exconfirm");
            QFilter userFilter = new QFilter("personid.person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());

            Object startDate = listShowParameter.getCustomParam("startDate");
            Object endDate = listShowParameter.getCustomParam("endDate");
            if (ObjectUtils.isNotEmpty(startDate) && ObjectUtils.isNotEmpty(endDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date start = sdf.parse((String) startDate);
                    Date end = sdf.parse((String) endDate);
                    Calendar cale = Calendar.getInstance();
                    cale.clear();
                    cale.setTime(end);
                    cale.set(Calendar.DAY_OF_MONTH, cale.getActualMaximum(Calendar.DAY_OF_MONTH));
                    cale.set(Calendar.HOUR_OF_DAY, cale.getActualMaximum(Calendar.HOUR_OF_DAY));
                    cale.set(Calendar.MINUTE, cale.getActualMaximum(Calendar.MINUTE));
                    cale.set(Calendar.SECOND, cale.getActualMaximum(Calendar.SECOND));
                    cale.set(Calendar.MILLISECOND, cale.getActualMaximum(Calendar.MILLISECOND));
                    end = cale.getTime();
                    userFilter.and("recorddate", QCP.large_equals, start).and("recorddate", QCP.less_equals, end);
                } catch (ParseException ex) {
                    LOGGER.info("日期解析异常");
                    throw new RuntimeException(ex);
                }
            }
            e.addCustomQFilter(userFilter);
        }
    }

    /**
     * 考勤组织设置为空 异常类型赋默认值
     *
     * @param args
     */
    @Override
    public void filterContainerInit(FilterContainerInitArgs args) {
        super.filterContainerInit(args);
        ListShowParameter listShowParameter = (ListShowParameter) this.getView().getFormShowParameter();
        String attendance = listShowParameter.getCustomParam("attendance");
        // 获取常用过滤集合
        QFilter typeQfilter = new QFilter("number", QCP.in, new String[]{"1010_S", "1020_S", "1030_S", "1040_S"});
        List<Object> list = QueryServiceHelper.queryPrimaryKeys("wtbd_exattribute", typeQfilter.toArray(), null, 100);
        List<String> collect = list.stream().map(String::valueOf).collect(Collectors.toList());
        List<FilterColumn> commonFilterColumns = args.getCommonFilterColumns();
        if (StringUtils.equals("attendance", attendance)) {
            commonFilterColumns.removeIf(i -> StringUtils.equals("recorddate", i.getFieldName()));
        }
        for (FilterColumn commonFilterColumn : commonFilterColumns) {
            String fieldName = commonFilterColumn.getFieldName();
            if ("exattributeid.name".equals(fieldName)) {
                commonFilterColumn.setDefaultValues(collect.toArray());
            }
            // 考勤组织为
            if ("org.name".equals(fieldName)) {
                commonFilterColumn.setDefaultValue(null);
            }
        }
    }
}