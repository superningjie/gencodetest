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
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReqTripeListPlugin extends AbstractListPlugin {

    private static final Log LOGGER = LogFactory.getLog(ReqTripeListPlugin.class);

    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);
        ListShowParameter listShowParameter = (ListShowParameter) this.getView().getFormShowParameter();
        String attendance = listShowParameter.getCustomParam("attendance");
        if (StringUtils.equals("attendance", attendance)) {
            QFilter userFilter = new QFilter("tdkw_createuser.attperson.person", "=", MobileCommonServiceHelper.getInstance().getUserId());
            userFilter.and("billstatus", "=", "C");
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
                    QFilter startQfilter = new QFilter("tdkw_tripdetail.starttripedate", QCP.large_equals, start);
                    startQfilter.and("tdkw_tripdetail.starttripedate", QCP.less_equals,end);
                    QFilter endQfilter = new QFilter("tdkw_tripdetail.endtripedate", QCP.large_equals, start);
                    endQfilter.and("tdkw_tripdetail.endtripedate", QCP.less_equals,end);
                    QFilter or = startQfilter.or(endQfilter);
                    userFilter.and(or);
                } catch (ParseException ex) {
                    LOGGER.info("日期解析异常");
                    throw new RuntimeException(ex);
                }
            }
            e.addCustomQFilter(userFilter);
        }
    }

    public void filterContainerInit(FilterContainerInitArgs args) {
        super.filterContainerInit(args);
        ListShowParameter listShowParameter = (ListShowParameter) this.getView().getFormShowParameter();
        String attendance = listShowParameter.getCustomParam("attendance");
        List<FilterColumn> commonFilterColumns = args.getCommonFilterColumns();
        if (StringUtils.equals("attendance", attendance)) {
            // FilterColumn createTimeFilter = args.getFilterColumn("createtime");
            // createTimeFilter.setDefaultValue("63");
            commonFilterColumns.removeIf(i -> StringUtils.equals("createtime", i.getFieldName()));
        }
    }
}