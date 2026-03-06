package tdkw.hrmp.hrobs.formplugin.induction;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.chart.*;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ChartClickEvent;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.util.Pair;
import tdkw.hrmp.hrobs.common.hrobs.emputils.GetEmpFilterUtil;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/7/3 17:04
 * @Description 入职分析 人员变化趋势 折线图表单插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_onboardinganalysis
 * @Version 1.0
 **/
public class OnboradLineChartFormPlugin extends AbstractFormPlugin implements ClickListener {
    private static final Log logger = LogFactory.getLog(OnboradLineChartFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_pointlinechartap");
        this.addClickListeners("tdkw_query");
        this.addClickListeners("tdkw_resetting");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        if ("tdkw_pointlinechartap".equals(key)) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }
            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_onborad_report");
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            showParameter.setHasRight(true);
            if ("[]".equals(this.getPageCache().get(name))) {
                return;
            }
            showParameter.setCustomParam("personIds", this.getPageCache().get(name));
            this.getView().showForm(showParameter);
        } else if (StringUtils.equals("tdkw_query", key)) {
            IDataModel model = this.getModel();
            PointLineChart pointLineChart = this.getControl("tdkw_pointlinechartap");
            String finalPersonIds = this.getPageCache().get("finalPersonIds");
            List<Long> finalPersonIdsList = (List<Long>) SerializationUtils.fromJsonStringToList(finalPersonIds, Long.class);
            this.drawChart(pointLineChart, model, finalPersonIdsList);
        } else if (StringUtils.equals("tdkw_resetting", key)) {
            IDataModel model = this.getModel();
            model.setValue("tdkw_degree", null);
            model.setValue("tdkw_job", null);
            model.setValue("tdkw_politicaloutlook", null);
            model.setValue("tdkw_sex", null);
            model.setValue("tdkw_employtype", null);
            model.setValue("tdkw_duties", null);
            model.setValue("tdkw_hierarchy", null);
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        IDataModel model = this.getModel();
        PointLineChart pointLineChart = this.getControl("tdkw_pointlinechartap");
        String finalPersonIds = this.getPageCache().get("finalPersonIds");
        List<Long> finalPersonIdsList = (List<Long>) SerializationUtils.fromJsonStringToList(finalPersonIds, Long.class);
        logger.info("组织下人员：" + finalPersonIds);
        this.drawChart(pointLineChart, model, finalPersonIdsList);
    }


    public void drawChart(PointLineChart pointLineChart, IDataModel model, List<Long> finalPersonIdsList) {
        boolean isX = true;

        // 创建分类轴，X轴方式展现
        Axis categoryAxis = this.createCategoryAxis(pointLineChart, isX);

        // 设置分类轴nametextstyle属性，
        Map<String, Object> nametextstyle = Maps.newHashMap();
        nametextstyle.put("color", "#000000");
        // nametextstyle.put("fontStyle", "italic");
        nametextstyle.put("fontSize", 14);
        categoryAxis.setPropValue("nameTextStyle", nametextstyle);

        // 设置分类轴名称位置属性，end表示在最后
        categoryAxis.setPropValue("nameLocation", "end");

        // 设置分类轴分类值显示位置，bottom表示在下
        categoryAxis.setPropValue("position", "bottom");
        // 设置分类轴分类值liaxisLabel属性
        Map<String, Object> axislabel = Maps.newHashMap();
        Map<String, Object> textstyle = Maps.newHashMap();
        textstyle.put("color", "#000000");
        textstyle.put("fontSize", "12");
        axislabel.put("textStyle", textstyle);
        categoryAxis.setPropValue("axisLabel", axislabel);

        // 创建数据轴，name为其名字。
        Axis ValueAxis = this.createValueAxis(pointLineChart, !isX);
        // 设置数据轴的nameTextStyle属性
        Map<String, Object> yAxisnametextstyle = Maps.newHashMap();
        yAxisnametextstyle.put("color", "#000000");
        yAxisnametextstyle.put("fontSize", 14);
        ValueAxis.setPropValue("nameTextStyle", yAxisnametextstyle);

        Date pointLineBeginDate = (Date) model.getValue("tdkw_pointlinebegindate");
        Date pointLineEndDate = (Date) model.getValue("tdkw_pointlineenddate");
        if (ObjectUtils.isEmpty(pointLineBeginDate) || ObjectUtils.isEmpty(pointLineEndDate)) {
            return;
        }
        List<Pair<Date, Date>> monthList = getMonthList(pointLineBeginDate, pointLineEndDate);

        // 设置分类轴数据
        categoryAxis.setCategorys(contructCatetoryData(monthList));
        // 创建折线并赋值
        this.createLineSeries(pointLineChart, contructValueData(monthList, finalPersonIdsList));

        // 设置图的边距
        pointLineChart.setMargin(Position.right, "30px");
        pointLineChart.setMargin(Position.top, "80px");
        pointLineChart.setMargin(Position.left, "80px");

        // 设置图例的位置
        pointLineChart.setLegendPropValue("top", "8%");
        // 设置图例中文字的字体大小和颜色等
        Map<String, Object> legendtextstyle = Maps.newHashMap();
        legendtextstyle.put("fontSize", 14);
        legendtextstyle.put("color", "#000000");
        pointLineChart.setLegendPropValue("textStyle", legendtextstyle);

        // 刷新图标
        pointLineChart.refresh();
    }

    /**
     * @author xxx
     * @Description X轴数据
     * @Date 2023/7/3 11:21
     */
    private List<String> contructCatetoryData(List<Pair<Date, Date>> monthList) {
        List<String> categoryData = new ArrayList<>();
        for (Pair<Date, Date> dateDatePair : monthList) {
            Date date = dateDatePair.getFirst();
            String month = DateTimeUtils.dateFormat(date, "yyyy.MM");
            categoryData.add(month);
        }

        return categoryData;
    }

    private QFilter getFlowFilter(javafx.util.Pair<Date, Date> pair) {
        QFilter qFilter = new QFilter("flowtime", QCP.large_equals, pair.getKey());
        qFilter.and("flowtime", QCP.less_equals, pair.getValue());
        // 变动类型-所属变动大类为 入职
        qFilter.and("tdkw_changetype.tdkw_chgevent.number", QCP.equals, "1010_S");
        return qFilter;
    }

    /**
     * @author xxx
     * @Description 构建折线图数据
     * @Date 2023/7/3 10:08
     */
    private List<BigDecimal> contructValueData(List<Pair<Date, Date>> monthList, List<Long> finalPersonIdsList) {
        //==========================================================
        List<BigDecimal> valueData = new ArrayList<>();

        DynamicObject dataEntity = this.getModel().getDataEntity();

        // 组织
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return valueData;
        }

        //==========================================================
        for (Pair<Date, Date> dateDatePair : monthList) {
            Date date = dateDatePair.getSecond();
            // 去查时间范围内入职的人的id集合
            QFilter totalFilter = GetEmpFilterUtil.getTotalFilter(dateDatePair.getFirst(), dateDatePair.getSecond(), finalPersonIdsList);
            DynamicObject[] load = BusinessDataServiceHelper.load("hpfs_personflow", "person", totalFilter.toArray());
            // 那个月入职的人的id 集合
            List<Long> totalIdList = Arrays.stream(load).map(o -> o.getLong("person.id")).collect(Collectors.toList());

            IPageCache pageCache = this.getPageCache();
            pageCache.put(DateTimeUtils.dateFormat(date, "yyyy.MM"), totalIdList.toString());
            valueData.add(BigDecimal.valueOf(totalIdList.size()));
        }
        return valueData;
    }


    /**
     * @author xxx
     * @Description 获取间隔的所有月份
     * @Date 2023/7/3 10:53
     */
    private static List<Pair<Date, Date>> getMonthList(Date start, Date end) {
        Calendar a = Calendar.getInstance();
        a.setTime(start);
        Calendar b = Calendar.getInstance();
        b.setTime(end);
        Calendar aToUse = DateUtils.truncate(a, Calendar.MONTH);
        Calendar bToUse = DateUtils.truncate(b, Calendar.MONTH);
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
                endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
                endDateCalendar.set(Calendar.MINUTE, 59);
                endDateCalendar.set(Calendar.SECOND, 59);
                datePairs.add(new Pair<>(currentDate, endDateCalendar.getTime()));
                currentDate = DateUtils.addMonths(currentDate, 1);
            }
            Pair<Date, Date> dateDatePair = datePairs.remove(0);
            datePairs.add(0, new Pair<>(a.getTime(), dateDatePair.getSecond()));
            dateDatePair = datePairs.remove(datePairs.size() - 1);
            // b.set();
            b.set(Calendar.HOUR_OF_DAY, 23);
            b.set(Calendar.MINUTE, 59);
            b.set(Calendar.SECOND, 59);
            datePairs.add(new Pair<>(dateDatePair.getFirst(), b.getTime()));
        }
        return datePairs;
    }

    /**
     * 创建类目型坐标轴
     * <p>
     * 是否X轴，ture创建X轴，false创建Y轴
     */
    private Axis createCategoryAxis(PointLineChart pointLineChart, boolean isx) {
        Axis axis;
        if (isx)
            axis = pointLineChart.createXAxis("", AxisType.category);
        else
            axis = pointLineChart.createYAxis("", AxisType.category);

        // 创建一个map存储x轴的复杂属性的属性-值对
        Map<String, Object> axisTick = Maps.newHashMap();
        axisTick.put("interval", 0);

        axisTick.put("show", true);
        axisTick.put("grid", Position.left);

        axis.setPropValue("axisTick", axisTick);
        return axis;
    }

    /**
     * 创建值类型坐标轴
     */
    private Axis createValueAxis(PointLineChart pointLineChart, boolean isx) {
        Axis axis;
        if (isx)
            axis = pointLineChart.createXAxis("", AxisType.value);
        else
            axis = pointLineChart.createYAxis("", AxisType.value);

        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> axisTick = Maps.newHashMap();
        axisTick.put("show", true);
        axis.setPropValue("axisTick", axisTick);

        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> splitLine = Maps.newHashMap();
        Map<String, Object> lineStyle = Maps.newHashMap();
        lineStyle.put("type", "dotted");
        lineStyle.put("color", "#E2E2E2");
        splitLine.put("lineStyle", lineStyle);
        axis.setPropValue("splitLine", splitLine);
        pointLineChart.setShowTooltip(true);
        return axis;
    }

    // 创建折线
    private void createLineSeries(PointLineChart pointLineChart, List<BigDecimal> values) {
        // 折线的名字
        LineSeries expireSeries = pointLineChart.createSeries("人员");

        // 设置折线上文本的相关属性
        Label label = new Label();
        label.setShow(true);
        label.setColor("#000000");
        label.setRotate("-30");
        expireSeries.setLabel(label);

        // 连线颜色
        expireSeries.setItemColor("");
        // 动画效果
        expireSeries.setAnimationDuration(2000);
        // 该点纵坐标的值setData(Number[] data)
        expireSeries.setData(values.toArray(new Number[0]));
    }
}