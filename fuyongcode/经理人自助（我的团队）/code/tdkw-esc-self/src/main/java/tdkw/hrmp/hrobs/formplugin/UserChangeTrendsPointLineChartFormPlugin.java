package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.chart.*;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ChartClickEvent;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.events.ClientCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.util.Pair;
import tdkw.hrmp.hrobs.common.app.utils.PeopleCountingUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;

/**
 * @author xxx
 * @Date 2023/6/19 17:04
 * @Description 人员分析 人员变化趋势 折线图表单插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personanalysis
 * @Version 1.0
 **/
public class UserChangeTrendsPointLineChartFormPlugin extends AbstractFormPlugin implements ClickListener {
    private Log logger = LogFactory.getLog(UserChangeTrendsPointLineChartFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //折线图
        this.addClickListeners("tdkw_pointlinechartap");


        //总人数柱状图
        this.addClickListeners("tdkw_totalnumchartap");


        //人员平均年龄柱状图
        this.addClickListeners("tdkw_ageavgchartap");


        //人员简历完整度柱状图
        this.addClickListeners("tdkw_resumechartap");
        this.addClickListeners("tdkw_querybtn");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();


        //折线图
        if (key.equals("tdkw_pointlinechartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }

            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_personinforpt");
            showParameter.getOpenStyle().setShowType(ShowType.Modal);
            showParameter.setHasRight(true);
            showParameter.setCustomParam("personIds", this.getPageCache().get(name));
            this.getView().showForm(showParameter);
        }

        //总人数柱状图
        else if (key.equals("tdkw_totalnumchartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }

            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_personinforpt");
            showParameter.getOpenStyle().setShowType(ShowType.Modal);
            showParameter.setHasRight(true);
            showParameter.setCustomParam("personIds", this.getPageCache().get(this.getClass().getName() + name));
            this.getView().showForm(showParameter);
        }
        //平均年龄柱状图
        else if (key.equals("tdkw_ageavgchartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }
            if (name.equals("XXX集团")) {
                return;
            }
            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_personinforpt");
            showParameter.getOpenStyle().setShowType(ShowType.Modal);
            showParameter.setHasRight(true);
            showParameter.setCustomParam("personIds", this.getPageCache().get(this.getClass().getName() + name));
            this.getView().showForm(showParameter);
        }
        //简历完整度柱状图
        else if (key.equals("tdkw_resumechartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }

            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_personfileintegrity");
            showParameter.getOpenStyle().setShowType(ShowType.Modal);
            DynamicObject dataEntity = this.getModel().getDataEntity();
            Date tdkwEnddate = dataEntity.getDate("tdkw_enddate");
            showParameter.setCustomParam("endDate", tdkwEnddate);
            showParameter.setCustomParam("orgId", this.getPageCache().get("orgId"));
            showParameter.setCustomParam("personIds", this.getPageCache().get(this.getClass().getName() + name));
            this.getView().showForm(showParameter);
        } else if (key.equals("tdkw_querybtn")) {
            DynamicObject dataEntity = this.getModel().getDataEntity();
            //开始时间
            Date tdkwBegindate = dataEntity.getDate("tdkw_begindate");
            //结束时间
            Date tdkwEnddate = dataEntity.getDate("tdkw_enddate");
            if (tdkwBegindate == null) {
                this.getView().showErrorNotification("开始时间不能为空！");
                return;
            } else if (tdkwEnddate == null) {
                this.getView().showErrorNotification("结束时间不能为空！");
                return;
            } else if (tdkwBegindate.compareTo(tdkwEnddate) > 0) {
                this.getView().showErrorNotification("开始时间不能大于结束时间！");
                return;
            }
            IDataModel model = this.getModel();
            PointLineChart pointLineChart = this.getControl("tdkw_pointlinechartap");
            this.drawChartPoint(pointLineChart, model);
            this.getView().addClientCallBack("ayncDrawPic");
            this.getView().updateView("tdkw_pointlinechartap");

            this.getView().updateView("tdkw_totalnumchartap");

            this.getView().updateView("tdkw_ageavgchartap");

            this.getView().updateView("tdkw_resumechartap");

        }

    }


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        IDataModel model = this.getModel();
        PointLineChart pointLineChart = this.getControl("tdkw_pointlinechartap");
        this.drawChartPoint(pointLineChart, model);
        this.getView().addClientCallBack("ayncDrawPic");
    }

    @Override
    public void clientCallBack(ClientCallBackEvent e) {
        super.clientCallBack(e);
        if ("ayncDrawPic".equals(e.getName())) {
            //开始绘制总人数柱状图
            HistogramChart histogramChartTotal = this.getControl("tdkw_totalnumchartap");
            this.drawChartTotal(histogramChartTotal);
        }

    }

    public void drawChartPoint(PointLineChart pointLineChart, IDataModel model) {

        boolean isX = true;

        // 创建分类轴，X轴方式展现
        Axis categoryAxis = this.createCategoryAxis(pointLineChart, "", isX);

        // 设置分类轴nametextstyle属性，
        Map<String, Object> nametextstyle = Maps.newHashMap();
//        nametextstyle.put("color", "#000000");
        // nametextstyle.put("fontStyle", "italic");
        nametextstyle.put("fontSize", 14);
        categoryAxis.setPropValue("nameTextStyle", nametextstyle);

        // 设置分类轴名称位置属性，end表示在最后
        categoryAxis.setPropValue("nameLocation", new String("end"));

        // 设置分类轴分类值显示位置，bottom表示在下
        categoryAxis.setPropValue("position", "bottom");
        // 设置分类轴分类值liaxisLabel属性
        Map<String, Object> axislabel = Maps.newHashMap();
        Map<String, Object> textstyle = Maps.newHashMap();
//        textstyle.put("color", "#000000");
        textstyle.put("fontSize", "12");
        axislabel.put("textStyle", textstyle);
        categoryAxis.setPropValue("axisLabel", axislabel);

        // 创建数据轴，name为其名字。
        Axis ValueAxis = this.createValueAxis(pointLineChart, "", !isX);
        // 设置数据轴的nameTextStyle属性
        Map<String, Object> yAxisnametextstyle = Maps.newHashMap();
//        yAxisnametextstyle.put("color", "#000000");
        yAxisnametextstyle.put("fontSize", 14);
        // yAxisnametextstyle.put("fontStyle", "oblique");
        ValueAxis.setPropValue("nameTextStyle", yAxisnametextstyle);


        Date pointLineBeginDate = (Date) model.getValue("tdkw_pointlinebegindate");
        Date pointLineEndDate = (Date) model.getValue("tdkw_pointlineenddate");
        if (ObjectUtils.isEmpty(pointLineBeginDate) || ObjectUtils.isEmpty(pointLineEndDate)) {
            return;
        }
//        List<Pair<Date, Date>> monthLists = getMonthList(pointLineBeginDate, pointLineEndDate);
        List<Date> monthList = PeopleCountingUtils.getMonth(pointLineBeginDate, pointLineEndDate);
//        for (Pair<Date, Date> pair : monthLists) {
//            monthList.add(pair.getValue());
//        }
//        List<Date> monthList = PeopleCountingUtil.getMonth(pointLineBeginDate, pointLineEndDate);


        // 设置分类轴数据
        categoryAxis.setCategorys(contructCatetoryData(monthList));
        // 创建折线并赋值
        this.createLineSeries(pointLineChart, "人员", contructValueData(monthList), "");

        // 设置图的边距
        pointLineChart.setMargin(Position.right, "30px");
//        pointLineChart.setMargin(Position.top, "80px");
        pointLineChart.setMargin(Position.left, "30px");

        // 设置图例的位置
        pointLineChart.setLegendPropValue("top", "8%");
        // 设置图例中文字的字体大小和颜色等
        Map<String, Object> legendtextstyle = Maps.newHashMap();
        legendtextstyle.put("fontSize", 14);
//        legendtextstyle.put("color", "#000000");
        pointLineChart.setLegendPropValue("textStyle", legendtextstyle);


        // 刷新图标
        pointLineChart.refresh();
    }

    /**
     * @author xxx
     * @Description X轴数据
     * @Date 2023/6/20 11:21
     */
    private List<String> contructCatetoryData(List<Date> monthList) {
        List<String> categoryData = new ArrayList<>();
        for (Date dateDatePair : monthList) {
            String month = DateTimeUtils.dateFormat(dateDatePair, "yyyy.MM");
            categoryData.add(month);
        }

        return categoryData;
    }

    /**
     * @author xxx
     * @Description 构建折线图数据
     * @Date 2023/6/28 10:08
     */
    private List<BigDecimal> contructValueData(List<Date> monthList) {
        long start = System.currentTimeMillis();

        List<BigDecimal> valueData = new ArrayList<>();

        DynamicObject dataEntity = this.getModel().getDataEntity();
        // 组织
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return valueData;
        }
        //开始时间
        Date tdkwBegindate = dataEntity.getDate("tdkw_begindate");
        //结束时间
        Date tdkwEnddate = dataEntity.getDate("tdkw_enddate");


        List<DynamicObject> personEmpposorgrel = getpersonEmpposorgrel(tdkwOrg.getLong("id"));

        logger.info("折线图至查询任职经历耗时" + (System.currentTimeMillis() - start));


        Map<Date, List<Long>> personIds = new HashMap<>();


        for (Date date : monthList) {
            personIds.put(date, new ArrayList<>());
        }
        for (DynamicObject dynamicObject : personEmpposorgrel) {
            //任职简历开始时间和结束时间
            Date startDate = dynamicObject.getDate("startdate");
            Date endDate = dynamicObject.getDate("sysenddate");
            Long personId = dynamicObject.getLong("person.id");
            //任职经历开始结束时间大于条件开始结束时间
            if (startDate == null || endDate == null || tdkwBegindate == null || tdkwEnddate == null) {
                logger.info("页面条件开始时间" + tdkwBegindate + "页面条件结束时间" + tdkwEnddate);
                logger.info("任职经历开始时间" + startDate + "任职经历结束时间：" + endDate);
                continue;
            }
            if (startDate.compareTo(tdkwBegindate) <= 0 && endDate.compareTo(tdkwEnddate) > 0) {
                for (Date date : monthList) {
                    List<Long> ids = personIds.get(date);
                    ids.add(personId);
                    personIds.put(date, ids);
                }
                //任职经历开始时间小于条件开始结束时间，但结束时间大于条件结束时间
            } else if (startDate.compareTo(tdkwBegindate) >= 0 && endDate.compareTo(tdkwEnddate) > 0) {
                for (Date date : monthList) {
                    if (date.compareTo(startDate) < 0) {
                        continue;
                    }
                    List<Long> ids = personIds.get(date);
                    ids.add(personId);
                    personIds.put(date, ids);
                }
                //任职经历开始时间大于条件开始结束时间，但结束时间小于条件结束时间
            } else if (startDate.compareTo(tdkwBegindate) <= 0 && endDate.compareTo(tdkwEnddate) <= 0) {
                for (Date date : monthList) {
                    if (date.compareTo(endDate) > 0) {
                        break;
                    }
                    List<Long> ids = personIds.get(date);
                    ids.add(personId);
                    personIds.put(date, ids);
                }
                //任职经历开始结束时间小于条件开始结束时间
            } else if (startDate.compareTo(tdkwBegindate) >= 0 && endDate.compareTo(tdkwEnddate) <= 0) {
                for (Date date : monthList) {
                    if (date.compareTo(startDate) <= 0) {
                        continue;
                    }
                    if (date.compareTo(endDate) >= 0) {
                        break;
                    }
                    List<Long> ids = personIds.get(date);
                    ids.add(personId);
                    personIds.put(date, ids);
                }
            }
        }

        logger.info("折线图任职经历时间条件筛选耗时" + (System.currentTimeMillis() - start));

        Map<String, String> keyValues = new HashMap<>();

        for (Date date : monthList) {
            List<Long> erManFileIds = personIds.get(date);
            keyValues.put(DateTimeUtils.dateFormat(date, "yyyy.MM"), erManFileIds.toString());
            valueData.add(BigDecimal.valueOf(erManFileIds.size()));
        }
        IPageCache pageCache = this.getPageCache();
        pageCache.put(keyValues);
        logger.info("折线图耗时" + (System.currentTimeMillis() - start));
        return valueData;
    }

    /**
     * @author xxx
     * @Description 获取间隔的所有月份
     * @Date 2023/6/20 10:53
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
                datePairs.add(new Pair<>(currentDate, endDateCalendar.getTime()));
                currentDate = DateUtils.addMonths(currentDate, 1);
            }
            Pair<Date, Date> dateDatePair = datePairs.remove(0);
            datePairs.add(0, new Pair<>(a.getTime(), dateDatePair.getSecond()));
            dateDatePair = datePairs.remove(datePairs.size() - 1);
            datePairs.add(new Pair<>(dateDatePair.getFirst(), b.getTime()));
        }
        return datePairs;
    }

    /**
     * 创建类目型坐标轴
     * <p>
     * 是否X轴，ture创建X轴，false创建Y轴
     */
    private Axis createCategoryAxis(PointLineChart pointLineChart, String name, boolean isx) {
        Axis axis = null;
        if (isx) axis = pointLineChart.createXAxis(name, AxisType.category);
        else axis = pointLineChart.createYAxis(name, AxisType.category);

        // 创建一个map存储x轴的复杂属性的属性-值对
        Map<String, Object> axisTick = Maps.newHashMap();
        axisTick.put("interval", Integer.valueOf(0));

        axisTick.put("show", true);
        axisTick.put("grid", Position.left);

        axis.setPropValue("axisTick", axisTick);
        return axis;
    }

    /**
     * 创建值类型坐标轴
     *
     * @param name 坐标轴名称
     *             是否X轴，ture创建X轴，false创建Y轴
     */
    private Axis createValueAxis(PointLineChart pointLineChart, String name, boolean isx) {
        Axis axis = null;
        if (isx) axis = pointLineChart.createXAxis(name, AxisType.value);
        else axis = pointLineChart.createYAxis(name, AxisType.value);

        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> axisTick = Maps.newHashMap();
        axisTick.put("show", true);
        axis.setPropValue("axisTick", axisTick);
//        axis.setPropValue("min", 540);
//        axis.setPropValue("max", 600);
//        axis.setPropValue("interval", 10);

        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> splitLine = Maps.newHashMap();
        Map<String, Object> lineStyle = Maps.newHashMap();
        lineStyle.put("type", "dotted");
//        lineStyle.put("color", "#E2E2E2");
        splitLine.put("lineStyle", lineStyle);
        axis.setPropValue("splitLine", splitLine);
        pointLineChart.setShowTooltip(true);
        return axis;
    }

    // 创建折线
    private void createLineSeries(PointLineChart pointLineChart, String name, List<BigDecimal> values, String color) {
        // 折线的名字
        LineSeries expireSeries = pointLineChart.createSeries(name);

        // 设置折线上文本的相关属性
        Label label = new Label();
        label.setShow(true);
        label.setColor("#000000");
        //折线图字体倾斜度
        label.setRotate("-30");
        expireSeries.setLabel(label);

        // 连线颜色
        expireSeries.setItemColor(color);
        // 动画效果
        expireSeries.setAnimationDuration(2000);
        // 该点纵坐标的值setData(Number[] data)
        expireSeries.setData((Number[]) values.toArray(new Number[0]));
    }

    /**
     * 绘制总人数柱状图表
     *
     * @param histogramChart
     */
    public void drawChartTotal(HistogramChart histogramChart) {
        long start = System.currentTimeMillis();

        DynamicObject dataEntity = this.getModel().getDataEntity();
        // 组织
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        //结束时间
        Date tdkwEnddate = dataEntity.getDate("tdkw_enddate");
        //开始时间
        Date tdkwBegindate = dataEntity.getDate("tdkw_begindate");
        List<DynamicObject> personEmpposorgrel = getpersonEmpposorgrel(tdkwOrg.getLong("id"));
        logger.info("总人数柱状图至查询任职经历耗时" + (System.currentTimeMillis() - start));

        List<Long> personIds = new ArrayList<>();
        List<DynamicObject> personDy = new ArrayList<>();
        for (DynamicObject dynamicObject : personEmpposorgrel) {
            //任职简历开始时间和结束时间
            Date startDate = dynamicObject.getDate("startdate");
            Date endDate = dynamicObject.getDate("sysenddate");
            Long personId = dynamicObject.getLong("person.id");
            //任职经历开始结束时间大于条件开始结束时间
            if (startDate == null || endDate == null || tdkwBegindate == null || tdkwEnddate == null) {
                logger.info("页面条件开始时间" + tdkwBegindate + "页面条件结束时间" + tdkwEnddate);
                logger.info("任职经历开始时间" + startDate + "任职经历结束时间：" + endDate);
                continue;
            }
            if (startDate.compareTo(tdkwEnddate) > 0 || endDate.compareTo(tdkwBegindate) < 0 || endDate.compareTo(tdkwEnddate) == 0) {
                continue;
            } else {
                personIds.add(personId);
                personDy.add(dynamicObject);
            }
        }

        logger.info("折线图任职经历时间条件筛选耗时" + (System.currentTimeMillis() - start));
        //设置是否显示图例
        histogramChart.setShowLegend(false);
        boolean isLegendVertical = true;
        histogramChart.setLegendVertical(isLegendVertical);
        Map<String, Object> titlePropValue = new HashMap<>();
        // 主标题内容：text
        titlePropValue.put("text", "条形图");
        // 设置触发提示框的类型为axis：坐标轴触发
        histogramChart.addTooltip("trigger", "axis");
        // 字符串模板格式
        List<Object> toolTipFuncPath = new ArrayList<>();
        String formatter = "{b0}: {c0}";
        histogramChart.addTooltip("formatter", formatter);
        toolTipFuncPath.add("tooltip");
        toolTipFuncPath.add("formatter");
        histogramChart.addFuncPath(toolTipFuncPath);
        histogramChart.setShowTooltip(true);
        // 设置图例位置为中上
        histogramChart.setLegendAlign(XAlign.center, YAlign.top);
        //AxisType可以调整纵轴或横轴显示数据
        Axis x = histogramChart.createXAxis("组织", AxisType.category);
        Axis y = histogramChart.createYAxis("", AxisType.value);
        BarSeries barseries = histogramChart.createSeries("总人数");
        // 设置系列名称，用于tooltip的显示，legend 的图例筛选.
        barseries.setName("组织");
        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
        barseries.setStack("数量");


        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return;
        }
        QFilter filter = new QFilter("parent", QCP.equals, tdkwOrg.getPkValue());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("enable", QCP.equals, "1");

        //查询出hr组织数据并进行排序
        DynamicObject[] load = BusinessDataServiceHelper.load("haos_adminorghr", "id,name", filter.toArray(), "sortcode");

        DynamicObjectCollection haos_adminorghr = new DynamicObjectCollection();
        for (DynamicObject dynamicObject : load) {
            haos_adminorghr.add(dynamicObject);
        }

        List<Long> orgIds = haos_adminorghr.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
        if (orgIds.size() == 0) {
            orgIds = Collections.singletonList((Long) tdkwOrg.getPkValue());
        }

        List<Long> finalOrgIds = orgIds;

        if (personIds == null || personIds.size() == 0) {
            return;
        }
        DataSet personInfos = PersonInfoUtil.queryPersonInfo(personIds, this.getClass().getName());
        // 查询缓存人员信息
        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(personInfos.copy());


        List<String> xNames = new ArrayList<>();
        List<Number> yDatas = new ArrayList<>();
        List<Number> yDataIf = new ArrayList<>();

        Map<Long, List<Long>> orgForPersons = new HashMap<>();
        Map<Long, DynamicObject> orgNameMap = new HashMap<>();
        List<Long> dirOrgPersonAll = new ArrayList<>();

        List<DynamicObject> collects = personDy.stream().filter(i -> i.getDate("startdate")
                .compareTo(tdkwEnddate) <= 0 && i.getDate("sysenddate")
                .compareTo(tdkwEnddate) >= 0).collect(Collectors.toList());

//        List<Long> dirOrgPersonInfo = personObjects.stream()
//                .filter(i -> null != i.get("tdkw_politicalstatus") && !i.getString("tdkw_politicalstatus").equals("0"))
//                .map(i -> i.getLong("person")).collect(Collectors.toList());
//        List<ItemValue> itemValue = new ArrayList<>();

        for (Long finalOrgId : finalOrgIds) {
            //获取当前层级组织及其下级组织
            List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(finalOrgId.toString());
            List<Long> dirOrgPersons = collects.stream()
                    .filter(i -> allBelowHROrg.contains(i.getLong("adminorg.id")))
                    .map(i -> i.getLong("person.id"))
                    .collect(Collectors.toList());

            if (dirOrgPersons.size() == 0) {
                continue;
            }
            //将查询出来的数据存入缓存以便在后续的两个柱状图中使用(组织下对应人员)
            orgForPersons.put(finalOrgId, dirOrgPersons);

            QFilter orgFilter = new QFilter("id", QCP.equals, finalOrgId);
            DynamicObject orgObject = QueryServiceHelper.queryOne("haos_adminorghr", "id,name", orgFilter.toArray());
            //将查询出来的数据存入缓存以便在后续的两个柱状图中使用(组织下对应名称)
            orgNameMap.put(finalOrgId, orgObject);

            dirOrgPersonAll.addAll(dirOrgPersons);
            xNames.add(orgObject.getString("name"));
            yDatas.add(dirOrgPersons.size());


            this.getPageCache().put(this.getClass().getName() + orgObject.getString("name"), dirOrgPersons.toString());
        }


        // 直属组织下人数
        List<Long> dirOrgPersons = collects.stream().filter(i -> i.get("adminorg.id")
                        .equals(tdkwOrg.getPkValue()))
                .map(i -> i.getLong("person.id")).collect(Collectors.toList());

//        dirOrgPersonInfo.retainAll(dirOrgPersons);
        if (haos_adminorghr.size() != 0 && dirOrgPersons.size() != 0) {
            dirOrgPersonAll.addAll(dirOrgPersons);
        }

        //汇总人数
        if (dirOrgPersonAll.size() != 0) {
            xNames.add(dataEntity.getDynamicObject("tdkw_org").getString("name"));
            yDatas.add(dirOrgPersonAll.size());


            this.getPageCache().put(this.getClass().getName() + dataEntity.getDynamicObject("tdkw_org"), dirOrgPersonAll.toString());
        }

        x.setPropValue("data", xNames);

        Map<String, Object> axisLabel = new HashMap<>();
        axisLabel.put("rotate", 15);
        axisLabel.put("interval", 0);
        x.setPropValue("axisLabel", axisLabel);

        barseries.setData(yDatas.toArray(new Number[yDatas.size()]));


        // 设置柱条颜色（直接设置颜色值和渐变设置）

        barseries.setBarWidth("20px");
        Label label = new Label();
        label.setShow(true);
        label.setColor("#000000");
        barseries.setLabel(label);


        List<Map<String, Object>> dataZoom = new ArrayList<>();
        Map<String, Object> dataZoomX = Maps.newHashMap();
        dataZoomX.put("id", "dataZoomX");
        dataZoomX.put("type", "slider");
        dataZoom.add(dataZoomX);
        histogramChart.addProperty("dataZoom", dataZoom);
        histogramChart.addProperty("height", "200px");

        histogramChart.refresh();

        logger.info("总人数耗时" + (System.currentTimeMillis() - start));

        //开始绘制人员平均年龄柱状图
        HistogramChart histogramChartAgeavg = this.getControl("tdkw_ageavgchartap");
        this.drawChartAgeavg(histogramChartAgeavg, personObjects, haos_adminorghr, finalOrgIds, orgForPersons, orgNameMap);

        //开始汇总简历完整度柱状图
        HistogramChart histogramChartResume = this.getControl("tdkw_resumechartap");
        this.drawChartResume(histogramChartResume, personObjects, haos_adminorghr, finalOrgIds, orgForPersons, orgNameMap);
        DynamicObject dataEntity1 = this.getModel().getDataEntity();
        this.getPageCache().put("orgId", String.valueOf(dataEntity1.getLong("tdkw_org.id")));


    }

    /**
     * 绘制平均年龄柱状图表
     *
     * @param histogramChart
     * @param personObjects
     * @param haos_adminorghr
     * @param finalOrgIds
     * @param orgForPersons
     * @param orgNameMap
     */
    public void drawChartAgeavg(HistogramChart histogramChart, DynamicObjectCollection personObjects, DynamicObjectCollection haos_adminorghr, List<Long> finalOrgIds, Map<Long, List<Long>> orgForPersons, Map<Long, DynamicObject> orgNameMap) {
        long start = System.currentTimeMillis();

        //设置是否显示图例
        histogramChart.setShowLegend(false);
        boolean isLegendVertical = true;
        histogramChart.setLegendVertical(isLegendVertical);
        Map<String, Object> titlePropValue = new HashMap<>();
        // 主标题内容：text
        titlePropValue.put("text", "条形图");
        // 设置触发提示框的类型为axis：坐标轴触发
        histogramChart.addTooltip("trigger", "axis");
        // 字符串模板格式
        List<Object> toolTipFuncPath = new ArrayList<>();
        String formatter = "{b0}: {c0}";
        histogramChart.addTooltip("formatter", formatter);
        toolTipFuncPath.add("tooltip");
        toolTipFuncPath.add("formatter");
        histogramChart.addFuncPath(toolTipFuncPath);
        histogramChart.setShowTooltip(true);
        // 设置图例位置为中上
        histogramChart.setLegendAlign(XAlign.center, YAlign.top);
        //AxisType可以调整纵轴或横轴显示数据
        Axis x = histogramChart.createXAxis("组织", AxisType.category);
        Axis y = histogramChart.createYAxis("", AxisType.value);
        BarSeries barseries = histogramChart.createSeries("总人数");
        // 设置系列名称，用于tooltip的显示，legend 的图例筛选.
        barseries.setName("组织");
        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
        barseries.setStack("数量");

        DynamicObject dataEntity = this.getModel().getDataEntity();
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return;
        }

        List<String> xNames = new ArrayList<>();
        List<Number> yDatas = new ArrayList<>();


        //获取缓存orgPersonMaps
        List<Long> dirOrgPersonAll = new ArrayList<>();

        int ageSums = 0;

        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.DOWN);
        for (Long finalOrgId : finalOrgIds) {
            List<Long> persons = orgForPersons.get(finalOrgId);
            if (null == persons || persons.size() == 0) {
                continue;
            }
            List<Long> finalPersons = persons;

            if (finalPersons.size() == 0) {
                continue;
            }
            // 年龄合计
            int ageSum = personObjects.stream().filter(i -> (Long) i.get("tdkw_company") != 0).filter(i -> finalPersons.contains(i.getLong("person"))).mapToInt(i -> i.getInt("tdkw_age")).sum();
            DynamicObject orgObject = orgNameMap.get(finalOrgId);
            ageSums += ageSum;
            dirOrgPersonAll.addAll(finalPersons);


            xNames.add(orgObject.getString("name"));

            yDatas.add(Double.valueOf(df.format((double) ageSum / finalPersons.size())));
        }


//        // 直属组织下人数
        List<Long> dirOrgPersons = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue()))
                .map(i -> i.getLong("person")).collect(Collectors.toList());

        if (haos_adminorghr.size() != 0 && dirOrgPersons.size() != 0) {
            // 年龄合计
            int ageSum = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue()))
                    .mapToInt(i -> i.getInt("tdkw_age")).sum();

            dirOrgPersonAll.addAll(dirOrgPersons);
            ageSums += ageSum;
        }

        //汇总数
        if (dirOrgPersonAll.size() != 0) {
            xNames.add(dataEntity.getDynamicObject("tdkw_org").getString("name"));
            yDatas.add(Double.valueOf(df.format((double) ageSums / dirOrgPersonAll.size())));
        }


        //全集团组织平均值
        xNames.add("XXX集团");
        yDatas.add(getSuperiorOrg());

        x.setPropValue("data", xNames);

        Map<String, Object> axisLabel = new HashMap<>();
        axisLabel.put("rotate", 15);
        axisLabel.put("interval", 0);
        x.setPropValue("axisLabel", axisLabel);

        barseries.setData(yDatas.toArray(new Number[yDatas.size()]));

        // 设置柱条颜色（直接设置颜色值和渐变设置）
//        barseries.setColor("#d0332f");
        barseries.setBarWidth("20px");
        Label label = new Label();
        label.setShow(true);
        label.setColor("#000000");
        barseries.setLabel(label);

        List<Map<String, Object>> dataZoom = new ArrayList<>();
        Map<String, Object> dataZoomX = Maps.newHashMap();
        dataZoomX.put("id", "dataZoomX");
        dataZoomX.put("type", "slider");
        dataZoom.add(dataZoomX);
        histogramChart.addProperty("dataZoom", dataZoom);
        histogramChart.addProperty("height", "200px");

        histogramChart.refresh();

        logger.info("平均年龄耗时" + (System.currentTimeMillis() - start));
    }

    /**
     * 绘制简历完整度柱状图表
     *
     * @param histogramChart
     * @param personObjects
     * @param haos_adminorghr
     * @param finalOrgIds
     * @param orgForPersons
     * @param orgNameMap
     */
    public void drawChartResume(HistogramChart histogramChart, DynamicObjectCollection personObjects, DynamicObjectCollection haos_adminorghr, List<Long> finalOrgIds, Map<Long, List<Long>> orgForPersons, Map<Long, DynamicObject> orgNameMap) {
        long start = System.currentTimeMillis();


        //设置是否显示图例
        histogramChart.setShowLegend(false);
        boolean isLegendVertical = true;
        histogramChart.setLegendVertical(isLegendVertical);
        Map<String, Object> titlePropValue = new HashMap<>();
        // 主标题内容：text
        titlePropValue.put("text", "条形图");
        // 设置触发提示框的类型为axis：坐标轴触发
        histogramChart.addTooltip("trigger", "axis");
        // 字符串模板格式
        List<Object> toolTipFuncPath = new ArrayList<>();
        String formatter = "{b0}: {c0}";
        histogramChart.addTooltip("formatter", formatter);
        toolTipFuncPath.add("tooltip");
        toolTipFuncPath.add("formatter");
        histogramChart.addFuncPath(toolTipFuncPath);
        histogramChart.setShowTooltip(true);
        // 设置图例位置为中上
        histogramChart.setLegendAlign(XAlign.center, YAlign.top);
        //AxisType可以调整纵轴或横轴显示数据
        Axis x = histogramChart.createXAxis("组织", AxisType.category);
        Axis y = histogramChart.createYAxis("", AxisType.value);
        BarSeries barseries = histogramChart.createSeries("总人数");
        // 设置系列名称，用于tooltip的显示，legend 的图例筛选.
        barseries.setName("组织");
        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
        barseries.setStack("数量");

        DynamicObject dataEntity = this.getModel().getDataEntity();
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return;
        }


        List<String> xNames = new ArrayList<>();
        List<Number> yDatas = new ArrayList<>();


        List<Long> dirOrgPersonAll = new ArrayList<>();

        BigDecimal progressbarSumAll = BigDecimal.ZERO;
        for (Long finalOrgId : finalOrgIds) {
            List<Long> persons = orgForPersons.get(finalOrgId);
            if (null == persons || persons.size() == 0) {
                continue;
            }
            List<Long> finalPersons = persons;
            if (finalPersons.size() == 0) {
                continue;
            }
            // 简历完整度合计
            BigDecimal progressbarSum = personObjects.stream().filter(i -> (Long) i.get("tdkw_company") != 0).filter(i -> finalPersons.contains(i.getLong("person"))).map(i -> i.getBigDecimal("tdkw_progressbar")).reduce(BigDecimal.ZERO, BigDecimal::add);


            DynamicObject orgObject = orgNameMap.get(finalOrgId);
            xNames.add(orgObject.getString("name"));

            progressbarSumAll = progressbarSumAll.add(progressbarSum);
            dirOrgPersonAll.addAll(finalPersons);


            BigDecimal divide = progressbarSum.divide(new BigDecimal(finalPersons.size()), 6, BigDecimal.ROUND_HALF_UP);
            divide = divide.multiply(new BigDecimal(100));
            divide = divide.setScale(2, BigDecimal.ROUND_HALF_UP);
            yDatas.add(divide.doubleValue());


        }


        // 直属组织下人数
        List<Long> dirOrgPersons = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue())).map(i -> i.getLong("person")).collect(Collectors.toList());
        if (haos_adminorghr.size() != 0 && dirOrgPersons.size() != 0) {
            // 简历完整度合计
            BigDecimal progressbarSum = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue())).map(i -> i.getBigDecimal("tdkw_progressbar")).reduce(BigDecimal.ZERO, BigDecimal::add);
            progressbarSumAll = progressbarSumAll.add(progressbarSum);
            dirOrgPersonAll.addAll(dirOrgPersons);
        }

        //汇总数
        if (dirOrgPersonAll.size() != 0) {
            xNames.add(dataEntity.getDynamicObject("tdkw_org").getString("name"));

            BigDecimal divides = progressbarSumAll.divide(new BigDecimal(dirOrgPersonAll.size()), 6, BigDecimal.ROUND_HALF_UP);
            divides = divides.multiply(new BigDecimal(100));
            divides = divides.setScale(2, BigDecimal.ROUND_HALF_UP);
            yDatas.add(divides.doubleValue());
        }
        this.getPageCache().put(this.getClass().getName() + dataEntity.getDynamicObject("tdkw_org").getString("name"), dirOrgPersonAll.toString());


        x.setPropValue("data", xNames);

        Map<String, Object> axisLabel = new HashMap<>();
        axisLabel.put("rotate", 15);
        axisLabel.put("interval", 0);
        x.setPropValue("axisLabel", axisLabel);

        barseries.setData(yDatas.toArray(new Number[yDatas.size()]));

        // 设置柱条颜色（直接设置颜色值和渐变设置）
//        barseries.setColor("#d0332f");
        barseries.setBarWidth("20px");
        Label label = new Label();
        label.setShow(true);
        label.setColor("#000000");
//        label.setFormatter("{c}%");
        barseries.setLabel(label);

        List<Map<String, Object>> dataZoom = new ArrayList<>();
        Map<String, Object> dataZoomX = Maps.newHashMap();
        dataZoomX.put("id", "dataZoomX");
        dataZoomX.put("type", "slider");
        dataZoom.add(dataZoomX);
        histogramChart.addProperty("dataZoom", dataZoom);
        histogramChart.addProperty("height", "200px");

        histogramChart.refresh();


        logger.info("简历信息完整度耗时" + (System.currentTimeMillis() - start));

    }


    public List<DynamicObject> getpersonEmpposorgrel(Long tdkwOrg) {
        DynamicObject dataEntity = this.getModel().getDataEntity();
        // 学历
        DynamicObjectCollection tdkw_education = dataEntity.getDynamicObjectCollection("tdkw_education");
        // 岗位序列
        DynamicObjectCollection tdkw_jobsequence = dataEntity.getDynamicObjectCollection("tdkw_jobsequence");
        // 政治面貌
        DynamicObjectCollection tdkw_politicalstatus = dataEntity.getDynamicObjectCollection("tdkw_politicalstatus");
        // 性别
        DynamicObjectCollection tdkw_gender = dataEntity.getDynamicObjectCollection("tdkw_gender");
        // 人员类别
        DynamicObjectCollection tdkw_employtype = dataEntity.getDynamicObjectCollection("tdkw_employtype");
        // 职务
        DynamicObjectCollection tdkw_job = dataEntity.getDynamicObjectCollection("tdkw_job");
        // 岗位层级
        String tdkw_postlevel = dataEntity.getString("tdkw_postlevel");

        QFilter erManFileFiler = new QFilter("1", QCP.in, 1);
        boolean hasAllOrgPerm = true;
        if (tdkwOrg != 100000) {
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ry_zrs_pc");
            logger.info("人员权限信息：" + result);
            hasAllOrgPerm = result.isHasAllOrgPerm();
        }
//        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ry_zrs_pc");
//        logger.info("人员权限信息：" + result);
        //如果包含10000L就返回true
//        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //获取需过滤的组织ID
        List<Long> allPersonByOrg = new ArrayList<>();
        if (tdkwOrg != 100000 || !hasAllOrgPerm) {
            //非全集团需要获取组织信息
            allPersonByOrg = PersonInfoUtil.getAllPersonByOrg((Long) tdkwOrg);
        }

        if (ObjectUtils.isNotEmpty(tdkw_jobsequence) && tdkw_jobsequence.size() != 0) {
            List<Object> jobSequence = tdkw_jobsequence.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("empposrel.tdkw_jobsequence", QCP.in, jobSequence);
        }
        if (ObjectUtils.isNotEmpty(tdkw_gender) && tdkw_gender.size() != 0) {
            List<Object> gender = tdkw_gender.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("pernontsprop.gender", QCP.in, gender);
        }
        if (ObjectUtils.isNotEmpty(tdkw_employtype) && tdkw_employtype.size() != 0) {
            List<Object> employType = tdkw_employtype.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("empposrel.tdkw_employtype", QCP.in, employType);
        }
        if (ObjectUtils.isNotEmpty(tdkw_job) && tdkw_job.size() != 0) {
            List<Object> job = tdkw_job.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("empposrel.tdkw_ranks", QCP.in, job);
        }
        if (ObjectUtils.isNotEmpty(tdkw_postlevel)) {
            String[] postLevel = tdkw_postlevel.substring(1, tdkw_postlevel.length() - 1).split(",");
            erManFileFiler.and("empposrel.tdkw_postlevel", QCP.in, postLevel);
        }


        // 人事业务档案
        List<Long> erManFileIds = PeopleCountingUtils.getAllErManFileByPersonIds(erManFileFiler);
        List<Long> erManFileEduAndPoliIds = new ArrayList<>(erManFileIds);


        if (ObjectUtils.isNotEmpty(tdkw_education) && tdkw_education.size() != 0) {
            // 学历
            List<Object> education = tdkw_education.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter pereduexpFilter = new QFilter("person.id", QCP.in, erManFileIds);
            pereduexpFilter.and("education", QCP.in, education);
            pereduexpFilter.and("ishighestdegree", QCP.equals, "1");
            pereduexpFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

            // 教育经历
            List<DynamicObject> ermanfileEdu = QueryServiceHelper.query("hrpi_pereduexp", "person.id", pereduexpFilter.toArray());
            List<Long> erManFileEduIds = ermanfileEdu.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
            erManFileEduAndPoliIds.retainAll(erManFileEduIds);
        }

        if (ObjectUtils.isNotEmpty(tdkw_politicalstatus) && tdkw_politicalstatus.size() != 0) {
            // 政治面貌
            List<Object> politicalStatus = tdkw_politicalstatus.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter perregionFilter = new QFilter("person.id", QCP.in, erManFileIds);
            perregionFilter.and("politicalstatus", QCP.in, politicalStatus);
            perregionFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

            // 基本信息补充
            List<DynamicObject> ermanfilePoli = QueryServiceHelper.query("hrpi_perregion", "person.id", perregionFilter.toArray());
            List<Long> erManFilePoliIds = ermanfilePoli.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
            erManFileEduAndPoliIds.retainAll(erManFilePoliIds);
        }

        //人员任职经历
        QFilter qFilter = new QFilter("person.id", QCP.in, erManFileEduAndPoliIds);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and("isprimary", QCP.equals, "1");
        qFilter.and("sysenddate", QCP.large_equals, dataEntity.getDate("tdkw_begindate"));

        //组织过滤
        //非全集团才需要对组织过滤
        if (tdkwOrg != 100000 || !hasAllOrgPerm) {
            qFilter.and("adminorg", QCP.in, allPersonByOrg);
        }
        return QueryServiceHelper.query("hrpi_empposorgrel", "person.id,startdate,sysenddate,adminorg.id", qFilter.toArray());
    }


    public BigDecimal getSuperiorOrg() {
        //人员总年龄纪录
        Integer ageSummary = 0;
        //人员总数量纪录
        int headcountSummary = 0;


        QFilter filter = new QFilter("businessstatus", QCP.equals, "1");
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("filetype.postype.number", QCP.equals, "XY00001");
        filter.and("empposrel.businessstatus", QCP.equals, "1");
        filter.and("empposrel.datastatus", QCP.equals, "1");
        filter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("empposrel.isprimary", QCP.equals, "1");

        //人事业务档案
        DataSet ermanFileData = ORM.create().queryDataSet(this.getClass().getName(), "hspm_ermanfile", "person,empposrel.company as company," +
                "person.id as personid,person.name as personname,empposrel.position,empposrel.tdkw_postlevel,empposrel.tdkw_employtype," +
                "pernontsprop.gender,pernontsprop.age as age,empposrel.adminorg as adminorg", new QFilter[]{filter});

        DynamicObjectCollection ermanFile = ORM.create().toPlainDynamicObjectCollection(ermanFileData);

        logger.info("XXX集团汇总人数：" + ermanFile.size());

        //组织直属人员比对人员非时序性属性中的其他过滤条件(人员唯一过滤)
        for (DynamicObject ermanFileEntity : ermanFile) {
            //年龄累加
            ageSummary = ageSummary + ermanFileEntity.getInt("age");
            //人员总数量累加
            headcountSummary++;
        }

        logger.info("总年龄：" + ageSummary + ",总人数：" + headcountSummary);
        //平均值
        BigDecimal meanValue = ZERO;
        if (!(ageSummary.compareTo(0) == 0)) {
            meanValue = valueOf(ageSummary).divide(valueOf(headcountSummary), 1, RoundingMode.DOWN);
        }

        return meanValue;
    }
}
