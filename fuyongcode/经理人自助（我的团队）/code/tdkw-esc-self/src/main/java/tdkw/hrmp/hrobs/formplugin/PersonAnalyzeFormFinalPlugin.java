package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.chart.*;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ChartClickEvent;
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
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.util.Pair;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.app.utils.PeopleCountingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;


/**
 * @description: 人员分析所有图表插件
 * @author xxx
 * @date: 2023/10/24 16:15
 * @param:
 * @param: null
 * @return: null
 **/
public class PersonAnalyzeFormFinalPlugin extends AbstractFormPlugin {


    private final Log logger = LogFactory.getLog(PersonAnalyzeFormPlugin.class);

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
        // 饼图
        this.addClickListeners("tdkw_piechartap");
        //政治面貌柱状图
        this.addClickListeners("tdkw_politicalchartap");
        // 查询按钮
        this.addClickListeners("tdkw_querybtn");
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        PieChart pieChart = this.getControl("tdkw_piechartap");
        if ("tdkw_piecharttype".equals(name)) {
            String tdkwPiecharttype = (String) this.getModel().getValue("tdkw_piecharttype");
            this.drawChart(pieChart, tdkwPiecharttype);
            this.getView().updateView("tdkw_piechartap");
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        DynamicObject dataEntity = this.getModel().getDataEntity();
        this.drawAllChart(dataEntity);
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
        } else if (key.equals("tdkw_piechartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }
            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_personinforpt");
            showParameter.getOpenStyle().setShowType(ShowType.Modal);
            showParameter.setHasRight(true);
            showParameter.setCustomParam("personIds", this.getPageCache().get(e.getSeriesName() + name));
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
        }  //政治面貌柱状图
        else if (key.equals("tdkw_politicalchartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }
            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_politicalinforpt");
            showParameter.getOpenStyle().setShowType(ShowType.Modal);
            showParameter.setHasRight(true);
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
            this.drawAllChart(dataEntity);
        }
    }


    private void drawAllChart(DynamicObject dataEntity) {
        IDataModel model = this.getModel();
        //开始时间
        Date tdkwBegindate = dataEntity.getDate("tdkw_begindate");
        //结束时间
        Date tdkwEnddate = dataEntity.getDate("tdkw_enddate");
        // 组织
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");

        // ==========通过sql查询数据=========
        DynamicObjectCollection dynamicObjects = getData(dataEntity, tdkwBegindate, tdkwOrg);
        // ==========通过sql查询数据=========

        // ==========总人数===========
        PointLineChart pointLineChart = this.getControl("tdkw_pointlinechartap");
        this.drawChartPoint(pointLineChart, model, tdkwBegindate, tdkwEnddate, dynamicObjects);
        // ==========总人数===========

        // ==========处理饼图数据===========
        IPageCache pageCache = this.getPageCache();
        String personIds = pageCache.get(DateTimeUtils.dateFormat(tdkwEnddate, "yyyy.MM"));
        if (StringUtils.isEmpty(personIds) || StringUtils.equals("[]", personIds)) {
            return;
        }
        // 从缓存中取出id
        Set<String> personIdSet = Arrays.stream(personIds.substring(1, personIds.length() - 1).split(",")).map(String::trim).collect(Collectors.toSet());
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        DynamicObjectCollection dynamicObjectsPies = new DynamicObjectCollection();

        // 过滤失效的数据
        dynamicObjects.stream()
                .filter(dynamicObject -> "1".equals(dynamicObject.getString("businessstatus")))
                .forEach(dynamicObject -> {
                    String personId = dynamicObject.getString("person.id");
                    if (!objectObjectHashMap.containsKey(personId) && personIdSet.contains(personId)) {
                        objectObjectHashMap.put(personId, 0);
                        dynamicObjectsPies.add(dynamicObject);
                    }
                });
        // ==========处理饼图数据===========

        // ==========人员结构分布 饼图===========
        PieChart pieChart = this.getControl("tdkw_piechartap");
        String tdkwPieChartType = dataEntity.getString("tdkw_piecharttype");
        this.drawChart(pieChart, tdkwPieChartType, dynamicObjectsPies);
        // ==========人员结构分布 饼图===========

        // ==========获取组织数据===========
        QFilter filter = new QFilter("parent", QCP.equals, tdkwOrg.getPkValue());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("enable", QCP.equals, "1");
        filter.and("datastatus", QCP.equals, "1");
        filter.and("status", QCP.equals, "C");

        //查询出hr组织数据并进行排序
        DynamicObjectCollection orgList = QueryServiceHelper.query("haos_adminorghr", "id,name,structlongnumber", filter.toArray(), "sortcode");
        // ==========获取组织数据===========

        // 简历时间过滤
        List<DynamicObject> personDynamicObject = dynamicObjects.stream()
                .filter(dynamicObject -> {
                    Date startDate = dynamicObject.getDate("startdate");
                    Date endDate = dynamicObject.getDate("sysenddate");
                    if (startDate == null || endDate == null) {
                        return false;
                    }
                    return !(startDate.compareTo(tdkwEnddate) > 0 || endDate.compareTo(tdkwBegindate) < 0 || endDate.compareTo(tdkwEnddate) == 0);
                })
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return;
        }
        // x轴数据
        List<String> xNames = new ArrayList<>();
        // 总人数柱状图y轴数据
        List<Number> headCountyYDataList = new ArrayList<>();
        // 年龄柱状图y轴数据
        List<Number> ageYDataList = new ArrayList<>();
        // 简历完整度柱状图y轴数据
        List<Number> resumeYDataList = new ArrayList<>();
        // 政治面貌y轴数据
        List<ItemValue> yData1s = new ArrayList<>();
        List<ItemValue> yData2s = new ArrayList<>();
        List<ItemValue> yData3s = new ArrayList<>();
        List<ItemValue> yData4s = new ArrayList<>();

        // 总人数
        List<Long> dirOrgPersonAll = new ArrayList<>();
        // 任职经历时间校验
        List<DynamicObject> collects = personDynamicObject.stream()
                .filter(i -> i.getDate("startdate")
                        .compareTo(tdkwEnddate) <= 0 && i.getDate("sysenddate")
                        .compareTo(tdkwEnddate) >= 0)
                .collect(Collectors.toList());

        // 循环处理数据
        // 年龄总和
        int allAgeSum = 0;
        // 简历完整度总和
        BigDecimal allResumeSum = BigDecimal.ZERO;
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.DOWN);
        for (DynamicObject dynamicObject : orgList) {
            //long finalOrgId = dynamicObject.getLong("id");
            String finalOrgName = dynamicObject.getString("name");
            String finalOrgStruct = dynamicObject.getString("structlongnumber");
            List<Long> dirOrgPersons = collects.stream()
                    .filter(psn -> Optional.ofNullable(psn.getString("adminorg.structlongnumber")).orElse(StringUtils.EMPTY).startsWith(finalOrgStruct))
                    .map(object -> object.getLong("person.id")).collect(Collectors.toList());
            int curTotalSize = dirOrgPersons.size();
            if (curTotalSize == 0) {
                continue;
            }
            xNames.add(finalOrgName);
            headCountyYDataList.add(curTotalSize);
            // =======计算总人数=======

            // =======年龄统计=======
            int ageSum = dynamicObjectsPies.stream()
                    .filter(i -> (Long) i.get("tdkw_company") != 0)
                    .filter(i -> dirOrgPersons.contains(i.getLong("person.id")))
                    .mapToInt(i -> i.getInt("tdkw_age")).sum();
            // 年龄y轴数据
            ageYDataList.add(Double.valueOf(df.format((double) ageSum / curTotalSize)));
            // 年龄总和
            allAgeSum += ageSum;
            // =======年龄统计=======

            // =======简历完整度统计=======
            BigDecimal progressbarSum = dynamicObjectsPies.stream()
                    .filter(i -> (Long) i.get("tdkw_company") != 0)
                    .filter(i -> dirOrgPersons.contains(i.getLong("person.id")))
                    .map(i -> i.getBigDecimal("tdkw_progressbar"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal divide = progressbarSum.divide(new BigDecimal(curTotalSize), 6, RoundingMode.HALF_UP);
            divide = divide.multiply(new BigDecimal(100));
            divide = divide.setScale(2, RoundingMode.HALF_UP);
            resumeYDataList.add(divide.doubleValue());
            allResumeSum = allResumeSum.add(progressbarSum);
            // =======简历完整度统计=======
            dirOrgPersonAll.addAll(dirOrgPersons);
            this.getPageCache().put(this.getClass().getName() + finalOrgName, dirOrgPersons.toString());
        }
        // 直属组织下人数 防止有人直接挂在组织下，本来不应该有这种情况
        List<Long> dirOrgPersons = collects.stream().filter(i -> i.get("adminorgid").equals(tdkwOrg.getPkValue()))
                .map(i -> i.getLong("person.id")).collect(Collectors.toList());
        if (orgList.size() != 0 && dirOrgPersons.size() != 0) {
            dirOrgPersonAll.addAll(dirOrgPersons);
        }

        int totalSize = dirOrgPersonAll.size();
        // 汇总人数、汇总年龄、汇总简历完整度
        if (totalSize != 0) {
            xNames.add(tdkwOrg.getString("name"));
            // 汇总人数
            headCountyYDataList.add(totalSize);
            // 汇总年龄
            ageYDataList.add(Double.valueOf(df.format((double) allAgeSum / totalSize)));
            // 简历完整度
            BigDecimal divides = allResumeSum.divide(new BigDecimal(dirOrgPersonAll.size()), 6, RoundingMode.HALF_UP);
            divides = divides.multiply(new BigDecimal(100));
            divides = divides.setScale(2, RoundingMode.HALF_UP);
            resumeYDataList.add(divides.doubleValue());
            this.getPageCache().put(this.getClass().getName() + tdkwOrg, dirOrgPersonAll.toString());
        }

        // ===========总人数=============
        HistogramChart histogramChart = this.getControl("tdkw_totalnumchartap");
        drawHeadCount(histogramChart, xNames, headCountyYDataList);
        // ===========总人数=============
        // ===========平均年龄=============
        HistogramChart histogramChartAgeAvg = this.getControl("tdkw_ageavgchartap");
        this.drawChartAgeavg(histogramChartAgeAvg, xNames, ageYDataList);
        // ===========平均年龄=============

        // ===========简历完整度=============
        HistogramChart histogramChartResume = this.getControl("tdkw_resumechartap");
        this.drawChartResume(histogramChartResume, xNames, resumeYDataList);
        // ===========简历完整度=============

//        // ===========政治面貌=============
//        HistogramChart histogramPoliticalChart = this.getControl("tdkw_politicalchartap");
//        this.drawChartPolitical(histogramPoliticalChart, xNames, yData1s, yData2s, yData3s, yData4s);
//        // ===========政治面貌=============

        this.getPageCache().put("orgId", String.valueOf(tdkwOrg.getString("id")));

        // ===========更新页面=============
        this.getView().updateView("tdkw_pointlinechartap");
        this.getView().updateView("tdkw_piechartap");
        this.getView().updateView("tdkw_totalnumchartap");
        this.getView().updateView("tdkw_ageavgchartap");
        this.getView().updateView("tdkw_resumechartap");
        this.getView().updateView("tdkw_politicalchartap");
        // ===========更新页面=============
    }


    /**
     * @description: 折线图
     * @return: void
     **/
    public void drawChartPoint(PointLineChart pointLineChart, IDataModel model, Date tdkwBegindate, Date tdkwEnddate, List<DynamicObject> personEmpposorgrel) {
        boolean isX = true;
        // 创建分类轴，X轴方式展现
        Axis categoryAxis = this.createCategoryAxis(pointLineChart, "", isX);
        // 设置分类轴nametextstyle属性，
        Map<String, Object> nametextstyle = Maps.newHashMap();
        nametextstyle.put("fontSize", 14);
        categoryAxis.setPropValue("nameTextStyle", nametextstyle);
        // 设置分类轴名称位置属性，end表示在最后
        categoryAxis.setPropValue("nameLocation", new String("end"));
        // 设置分类轴分类值显示位置，bottom表示在下
        categoryAxis.setPropValue("position", "bottom");
        // 设置分类轴分类值liaxisLabel属性
        Map<String, Object> axislabel = Maps.newHashMap();
        Map<String, Object> textstyle = Maps.newHashMap();
        textstyle.put("fontSize", "12");
        axislabel.put("textStyle", textstyle);
        categoryAxis.setPropValue("axisLabel", axislabel);
        // 创建数据轴，name为其名字。
        Axis ValueAxis = this.createValueAxis(pointLineChart, "", !isX);
        // 设置数据轴的nameTextStyle属性
        Map<String, Object> yAxisnametextstyle = Maps.newHashMap();
        yAxisnametextstyle.put("fontSize", 14);
        ValueAxis.setPropValue("nameTextStyle", yAxisnametextstyle);
        Date pointLineBeginDate = (Date) model.getValue("tdkw_pointlinebegindate");
        Date pointLineEndDate = (Date) model.getValue("tdkw_pointlineenddate");
        if (ObjectUtils.isEmpty(pointLineBeginDate) || ObjectUtils.isEmpty(pointLineEndDate)) {
            return;
        }
        List<Date> monthList = PeopleCountingUtils.getMonth(pointLineBeginDate, pointLineEndDate);
        // 设置分类轴数据
        categoryAxis.setCategorys(contructCatetoryData(monthList));
        // 创建折线并赋值
        this.createLineSeries(pointLineChart, "人员", contructValueData(monthList, tdkwBegindate, tdkwEnddate, personEmpposorgrel), "");
        // 设置图的边距
        pointLineChart.setMargin(Position.right, "30px");
        pointLineChart.setMargin(Position.left, "30px");
        // 设置图例的位置
        pointLineChart.setLegendPropValue("top", "8%");
        // 设置图例中文字的字体大小和颜色等
        Map<String, Object> legendtextstyle = Maps.newHashMap();
        legendtextstyle.put("fontSize", 14);
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
    private List<BigDecimal> contructValueData(List<Date> monthList, Date tdkwBegindate, Date tdkwEnddate, List<DynamicObject> personEmpposorgrel) {
        long start = System.currentTimeMillis();
        List<BigDecimal> valueData = new ArrayList<>();
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
            List<Long> longs = personIds.get(date);
            keyValues.put(DateTimeUtils.dateFormat(date, "yyyy.MM"), longs.toString());
            valueData.add(BigDecimal.valueOf(longs.size()));
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
     * @author xxx
     * @Description 生成饼图
     * @Date 2023/6/30 11:06
     */
    public void drawChart(PieChart pieChart, String pieChartType, DynamicObjectCollection personObjects) {
        long start = System.currentTimeMillis();

        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");

        Date tdkwEnddate = (Date) this.getModel().getValue("tdkw_enddate");
        IPageCache pageCache = this.getPageCache();
        String personIds = pageCache.get(DateTimeUtils.dateFormat(tdkwEnddate, "yyyy.MM"));
        if (StringUtils.isEmpty(personIds) || StringUtils.equals("[]", personIds)) {
            return;
        }
        logger.info("饼图获取人员信息集合耗时" + (System.currentTimeMillis() - start));
        if (StringUtils.equals("0", pieChartType)) {
            // 岗位层级
            PieSeries series = pieChart.createPieSeries("岗位层级");
            Map<Object, Long> tdkwPostlevel = personObjects.stream()
                    .filter(i -> ObjectUtils.isNotEmpty(i.get("tdkw_postlevel")))
                    .collect(Collectors.groupingBy(i -> i.get("tdkw_postlevel"), Collectors.counting()));
            Set<Object> objects = tdkwPostlevel.keySet();
            ItemValue[] data = new ItemValue[tdkwPostlevel.size()];
            int i = 0;
            for (Object key : objects) {
                String keyName = key.toString();
                if (StringUtils.equals("1", keyName)) {
                    keyName = "集团高管-集团直管";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("1", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person.id"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("2", keyName)) {
                    keyName = "集团高管-授权行业";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("2", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person.id"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("3", keyName)) {
                    keyName = "其他高管";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("3", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person.id"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("4", keyName)) {
                    keyName = "中层";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("4", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person.id"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("5", keyName)) {
                    keyName = "基层";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("5", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person.id"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                }
                data[i] = new ItemValue(keyName, tdkwPostlevel.get(key));
                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("1", pieChartType)) {
            // 集团司龄
            PieSeries series = pieChart.createPieSeries("集团司龄");
            // 1年以下
            Long count1 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0).count();
            // 1-2年
            Long count2 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) < 0).count();
            // 3-5年
            Long count3 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) >= 0
                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) < 0).count();
            // 6-10年
            Long count4 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) >= 0
                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0).count();
            // 10年以上
            Long count5 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0).count();
            Map<String, Long> tdkw_comsercount = new ListOrderedMap<>();
            if (count1 != 0) {
                tdkw_comsercount.put("1年以下", count1);
                List<Long> collect1 = personObjects.stream()
                        .filter(j -> ((BigDecimal) j.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "1年以下", collect1.toString());
            }
            if (count2 != 0) {
                tdkw_comsercount.put("1-2年", count2);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "1-2年", collect1.toString());
            }
            if (count3 != 0) {
                tdkw_comsercount.put("3-5年", count3);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) >= 0
                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "3-5年", collect1.toString());
            }
            if (count4 != 0) {
                tdkw_comsercount.put("6-10年", count4);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) >= 0
                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "6-10年", collect1.toString());
            }
            if (count5 != 0) {
                tdkw_comsercount.put("10年以上", count5);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "10年以上", collect1.toString());
            }

            Set<String> strings = tdkw_comsercount.keySet();
            ItemValue[] data = new ItemValue[tdkw_comsercount.size()];
            int i = 0;
            for (String key : strings) {
                data[i] = new ItemValue(key, tdkw_comsercount.get(key));
                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("2", pieChartType)) {
            // 性别
            PieSeries series = pieChart.createPieSeries("性别");
            Map<Object, Long> tdkw_gender = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_gender") != 0)
                    .collect(Collectors.groupingBy(i -> i.get("tdkw_gender"), Collectors.counting()));
            Set<Object> objects = tdkw_gender.keySet();
            ItemValue[] data = new ItemValue[tdkw_gender.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_sex", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_gender.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_gender").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("性别" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("3", pieChartType)) {
            // 学历
            PieSeries series = pieChart.createPieSeries("学历");
            Map<Object, Long> tdkw_education = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_education") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_education"), Collectors.counting()));
            Set<Object> objects = tdkw_education.keySet();
            ItemValue[] data = new ItemValue[tdkw_education.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_diploma", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_education.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_education").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("学历" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("4", pieChartType)) {
            // 年龄
            PieSeries series = pieChart.createPieSeries("年龄");
            // 25及以下
            Long count1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 0)
                    .filter(i -> (int) i.get("tdkw_age") <= 25).count();
            // 26-30
            Long count2 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30).count();
            // 31-35
            Long count3 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35).count();
            // 36-40
            Long count4 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40).count();
            // 41-45
            Long count5 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45).count();
            // 46-50
            Long count6 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50).count();
            // 51-55
            Long count7 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55).count();
            // 55以上
            Long count8 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") > 55).count();
            Map<String, Long> tdkw_age = new ListOrderedMap<>();
            if (count1 != 0) {
                tdkw_age.put("25及以下", count1);
                List<Long> collect1 = personObjects.stream()
                        .filter(j -> (int) j.get("tdkw_age") != 0).filter(j -> (int) j.get("tdkw_age") <= 25)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "25及以下", collect1.toString());
            }
            if (count2 != 0) {
                tdkw_age.put("26-30", count2);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "26-30", collect1.toString());
            }
            if (count3 != 0) {
                tdkw_age.put("31-35", count3);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "31-35", collect1.toString());
            }
            if (count4 != 0) {
                tdkw_age.put("36-40", count4);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "36-40", collect1.toString());
            }
            if (count5 != 0) {
                tdkw_age.put("41-45", count5);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "41-45", collect1.toString());
            }
            if (count6 != 0) {
                tdkw_age.put("46-50", count6);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "46-50", collect1.toString());
            }
            if (count7 != 0) {
                tdkw_age.put("51-55", count7);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "51-55", collect1.toString());
            }
            if (count8 != 0) {
                tdkw_age.put("55以上", count8);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") > 55)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "55以上", collect1.toString());
            }

            Set<String> strings = tdkw_age.keySet();
            ItemValue[] data = new ItemValue[tdkw_age.size()];
            int i = 0;
            for (String key : strings) {
                data[i] = new ItemValue(key, tdkw_age.get(key));
                i++;
            }

            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("5", pieChartType)) {
            // 政治面貌
            PieSeries series = pieChart.createPieSeries("政治面貌");
            Map<Object, Long> tdkw_politicalstatus = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_politicalstatus") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_politicalstatus"), Collectors.counting()));
            Set<Object> objects = tdkw_politicalstatus.keySet();
            ItemValue[] data = new ItemValue[tdkw_politicalstatus.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_politicalstatus", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_politicalstatus.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_politicalstatus").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("政治面貌" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("6", pieChartType)) {
            // 社会工龄
            PieSeries series = pieChart.createPieSeries("社会工龄");
            // 1年以下
            long count1 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) < 0).count();
            // 1-2年
            long count2 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) >= 0
                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) < 0).count();
            // 3-5年
            long count3 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) >= 0
                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) < 0).count();
            // 6-10年
            long count4 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) >= 0
                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) <= 0).count();
            // 10年以上
            long count5 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) > 0).count();
            Map<String, Long> tdkw_socialworkage = new ListOrderedMap<>();
            if (count1 != 0) {
                tdkw_socialworkage.put("1年以下", count1);
                List<Long> collect1 = personObjects.stream()
                        .filter(j -> ((BigDecimal) j.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "1年以下", collect1.toString());
            }
            if (count2 != 0) {
                tdkw_socialworkage.put("1-2年", count2);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) >= 0
                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "1-2年", collect1.toString());
            }
            if (count3 != 0) {
                tdkw_socialworkage.put("3-5年", count3);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) >= 0
                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "3-5年", collect1.toString());
            }
            if (count4 != 0) {
                tdkw_socialworkage.put("6-10年", count4);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) >= 0
                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) <= 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "6-10年", collect1.toString());
            }
            if (count5 != 0) {
                tdkw_socialworkage.put("10年以上", count5);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) > 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "10年以上", collect1.toString());
            }

            Set<String> strings = tdkw_socialworkage.keySet();
            ItemValue[] data = new ItemValue[tdkw_socialworkage.size()];
            int i = 0;
            for (String key : strings) {
                data[i] = new ItemValue(key, tdkw_socialworkage.get(key));
                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("7", pieChartType)) {
            // 婚姻状况
            PieSeries series = pieChart.createPieSeries("婚姻状况");
            Map<Object, Long> tdkw_marriagestatus = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_marriagestatus") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_marriagestatus"), Collectors.counting()));
            Set<Object> objects = tdkw_marriagestatus.keySet();
            ItemValue[] data = new ItemValue[tdkw_marriagestatus.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_marriagestatus", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_marriagestatus.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_marriagestatus").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("婚姻状况" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("8", pieChartType)) {
            // 婚姻状况
            PieSeries series = pieChart.createPieSeries("人员类型");
            Map<Object, Long> tdkw_employtype = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_employtype") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_employtype"), Collectors.counting()));
            Set<Object> objects = tdkw_employtype.keySet();
            ItemValue[] data = new ItemValue[tdkw_employtype.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_laborreltype", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_employtype.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_employtype").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("人员类型" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        }
        pieChart.setLegendPropValue("right", "0px");

        logger.info("饼图耗时" + (System.currentTimeMillis() - start));

    }

//    /**
//     * @author xxx
//     * @Description 生成饼图
//     * @Date 2023/6/30 11:06
//     */
//    public void drawChart(PieChart pieChart, String pieChartType, DynamicObjectCollection personInfos) {
//        long start = System.currentTimeMillis();
//
//        pieChart.clearData();
//        pieChart.setShowTooltip(true);
//        //设置为位置
//        pieChart.setMargin(Position.right, "30px");
//        pieChart.setMargin(Position.top, "30px");
//        pieChart.setMargin(Position.bottom, "10px");
//        pieChart.setMargin(Position.left, "20px");
//        pieChart.setLegendPropValue("left", "right");
//
//        Date tdkwEnddate = (Date) this.getModel().getValue("tdkw_enddate");
//        IPageCache pageCache = this.getPageCache();
//        String personIds = pageCache.get(DateTimeUtils.dateFormat(tdkwEnddate, "yyyy.MM"));
//        if (StringUtils.isEmpty(personIds) || StringUtils.equals("[]", personIds)) {
//            return;
//        }
//        String[] ids = personIds.substring(1, personIds.length() - 1).replaceAll(" ", "").split(",");
//        // ids转为map
//        Map<String, String> idMap = new HashMap<>();
//        for (String id : ids) {
//            idMap.put(id, id);
//        }
//        // 过滤collect中的数据
//        List<DynamicObject> personObjects = personInfos.stream().filter(dynamicObject -> {
//            String person = dynamicObject.getString("person");
//            if (idMap.containsKey(person)) {
//                return true;
//            }
//            return false;
//        }).collect(Collectors.toList());
//        logger.info("饼图获取人员信息集合耗时" + (System.currentTimeMillis() - start));
//        if (StringUtils.equals("0", pieChartType)) {
//            // 岗位层级
//            PieSeries series = pieChart.createPieSeries("岗位层级");
//            Map<Object, Long> tdkwPostlevel = personObjects.stream()
//                    .filter(i -> ObjectUtils.isNotEmpty(i.get("tdkw_postlevel")))
//                    .collect(Collectors.groupingBy(i -> i.get("tdkw_postlevel"), Collectors.counting()));
//            Set<Object> objects = tdkwPostlevel.keySet();
//            ItemValue[] data = new ItemValue[tdkwPostlevel.size()];
//            int i = 0;
//            for (Object key : objects) {
//                String keyName = key.toString();
//                if (StringUtils.equals("1", keyName)) {
//                    keyName = "集团高管-集团直管";
//                    List<Long> collect1 = personObjects.stream()
//                            .filter(j -> StringUtils.equals("1", j.getString("tdkw_postlevel")))
//                            .map(j -> j.getLong("person"))
//                            .collect(Collectors.toList());
//                    pageCache.put("岗位层级" + keyName, collect1.toString());
//                } else if (StringUtils.equals("2", keyName)) {
//                    keyName = "集团高管-授权行业";
//                    List<Long> collect1 = personObjects.stream()
//                            .filter(j -> StringUtils.equals("2", j.getString("tdkw_postlevel")))
//                            .map(j -> j.getLong("person"))
//                            .collect(Collectors.toList());
//                    pageCache.put("岗位层级" + keyName, collect1.toString());
//                } else if (StringUtils.equals("3", keyName)) {
//                    keyName = "其他高管";
//                    List<Long> collect1 = personObjects.stream()
//                            .filter(j -> StringUtils.equals("3", j.getString("tdkw_postlevel")))
//                            .map(j -> j.getLong("person"))
//                            .collect(Collectors.toList());
//                    pageCache.put("岗位层级" + keyName, collect1.toString());
//                } else if (StringUtils.equals("4", keyName)) {
//                    keyName = "中层";
//                    List<Long> collect1 = personObjects.stream()
//                            .filter(j -> StringUtils.equals("4", j.getString("tdkw_postlevel")))
//                            .map(j -> j.getLong("person"))
//                            .collect(Collectors.toList());
//                    pageCache.put("岗位层级" + keyName, collect1.toString());
//                } else if (StringUtils.equals("5", keyName)) {
//                    keyName = "基层";
//                    List<Long> collect1 = personObjects.stream()
//                            .filter(j -> StringUtils.equals("5", j.getString("tdkw_postlevel")))
//                            .map(j -> j.getLong("person"))
//                            .collect(Collectors.toList());
//                    pageCache.put("岗位层级" + keyName, collect1.toString());
//                }
//                data[i] = new ItemValue(keyName, tdkwPostlevel.get(key));
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("1", pieChartType)) {
//            // 集团司龄
//            PieSeries series = pieChart.createPieSeries("集团司龄");
//            // 1年以下
//            long count1 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0).count();
//            // 1-2年
//            long count2 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
//                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) < 0).count();
//            // 3-5年
//            long count3 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) >= 0
//                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) < 0).count();
//            // 6-10年
//            long count4 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) >= 0
//                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0).count();
//            // 10年以上
//            long count5 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0).count();
//            Map<String, Long> tdkw_comsercount = new ListOrderedMap<>();
//            if (count1 != 0) {
//                tdkw_comsercount.put("1年以下", count1);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> ((BigDecimal) j.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("集团司龄" + "1年以下", collect1.toString());
//            }
//            if (count2 != 0) {
//                tdkw_comsercount.put("1-2年", count2);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
//                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) < 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("集团司龄" + "1-2年", collect1.toString());
//            }
//            if (count3 != 0) {
//                tdkw_comsercount.put("3-5年", count3);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) >= 0
//                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) < 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("集团司龄" + "3-5年", collect1.toString());
//            }
//            if (count4 != 0) {
//                tdkw_comsercount.put("6-10年", count4);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) >= 0
//                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("集团司龄" + "6-10年", collect1.toString());
//            }
//            if (count5 != 0) {
//                tdkw_comsercount.put("10年以上", count5);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("集团司龄" + "10年以上", collect1.toString());
//            }
//
//            Set<String> strings = tdkw_comsercount.keySet();
//            ItemValue[] data = new ItemValue[tdkw_comsercount.size()];
//            int i = 0;
//            for (String key : strings) {
//                data[i] = new ItemValue(key, tdkw_comsercount.get(key));
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("2", pieChartType)) {
//            // 性别
//            PieSeries series = pieChart.createPieSeries("性别");
//            Map<Object, Long> tdkw_gender = personObjects.stream()
//                    .filter(i -> (Long) i.get("tdkw_gender") != 0)
//                    .collect(Collectors.groupingBy(i -> i.get("tdkw_gender"), Collectors.counting()));
//            Set<Object> objects = tdkw_gender.keySet();
//            ItemValue[] data = new ItemValue[tdkw_gender.size()];
//            int i = 0;
//            for (Object key : objects) {
//                QFilter sexFilter = new QFilter("id", QCP.equals, key);
//                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_sex", "id,name", sexFilter.toArray());
//                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_gender.get(key));
//
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> j.get("tdkw_gender").equals(dynamicObject.get("id")))
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("性别" + dynamicObject.getString("name"), collect1.toString());
//
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("3", pieChartType)) {
//            // 学历
//            PieSeries series = pieChart.createPieSeries("学历");
//            Map<Object, Long> tdkw_education = personObjects.stream()
//                    .filter(i -> (Long) i.get("tdkw_education") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_education"), Collectors.counting()));
//            Set<Object> objects = tdkw_education.keySet();
//            ItemValue[] data = new ItemValue[tdkw_education.size()];
//            int i = 0;
//            for (Object key : objects) {
//                QFilter sexFilter = new QFilter("id", QCP.equals, key);
//                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_diploma", "id,name", sexFilter.toArray());
//                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_education.get(key));
//
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> j.get("tdkw_education").equals(dynamicObject.get("id")))
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("学历" + dynamicObject.getString("name"), collect1.toString());
//
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("4", pieChartType)) {
//            // 年龄
//            PieSeries series = pieChart.createPieSeries("年龄");
//            // 25及以下
//            long count1 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") >= 0)
//                    .filter(i -> (int) i.get("tdkw_age") <= 25).count();
//            // 26-30
//            long count2 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30).count();
//            // 31-35
//            long count3 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35).count();
//            // 36-40
//            long count4 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40).count();
//            // 41-45
//            long count5 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45).count();
//            // 46-50
//            long count6 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50).count();
//            // 51-55
//            long count7 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55).count();
//            // 55以上
//            long count8 = personObjects.stream()
//                    .filter(i -> (int) i.get("tdkw_age") > 55).count();
//            Map<String, Long> tdkw_age = new ListOrderedMap<>();
//            if (count1 != 0) {
//                tdkw_age.put("25及以下", count1);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> (int) j.get("tdkw_age") != 0).filter(j -> (int) j.get("tdkw_age") <= 25)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "25及以下", collect1.toString());
//            }
//            if (count2 != 0) {
//                tdkw_age.put("26-30", count2);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "26-30", collect1.toString());
//            }
//            if (count3 != 0) {
//                tdkw_age.put("31-35", count3);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "31-35", collect1.toString());
//            }
//            if (count4 != 0) {
//                tdkw_age.put("36-40", count4);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "36-40", collect1.toString());
//            }
//            if (count5 != 0) {
//                tdkw_age.put("41-45", count5);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "41-45", collect1.toString());
//            }
//            if (count6 != 0) {
//                tdkw_age.put("46-50", count6);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "46-50", collect1.toString());
//            }
//            if (count7 != 0) {
//                tdkw_age.put("51-55", count7);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "51-55", collect1.toString());
//            }
//            if (count8 != 0) {
//                tdkw_age.put("55以上", count8);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> (int) i.get("tdkw_age") > 55)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("年龄" + "55以上", collect1.toString());
//            }
//
//            Set<String> strings = tdkw_age.keySet();
//            ItemValue[] data = new ItemValue[tdkw_age.size()];
//            int i = 0;
//            for (String key : strings) {
//                data[i] = new ItemValue(key, tdkw_age.get(key));
//                i++;
//            }
//
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("5", pieChartType)) {
//            // 政治面貌
//            PieSeries series = pieChart.createPieSeries("政治面貌");
//            Map<Object, Long> tdkw_politicalstatus = personObjects.stream()
//                    .filter(i -> (Long) i.get("tdkw_politicalstatus") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_politicalstatus"), Collectors.counting()));
//            Set<Object> objects = tdkw_politicalstatus.keySet();
//            ItemValue[] data = new ItemValue[tdkw_politicalstatus.size()];
//            int i = 0;
//            for (Object key : objects) {
//                QFilter sexFilter = new QFilter("id", QCP.equals, key);
//                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_politicalstatus", "id,name", sexFilter.toArray());
//                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_politicalstatus.get(key));
//
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> j.get("tdkw_politicalstatus").equals(dynamicObject.get("id")))
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("政治面貌" + dynamicObject.getString("name"), collect1.toString());
//
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("6", pieChartType)) {
//            // 社会工龄
//            PieSeries series = pieChart.createPieSeries("社会工龄");
//            // 1年以下
//            long count1 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) < 0).count();
//            // 1-2年
//            long count2 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) >= 0
//                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) < 0).count();
//            // 3-5年
//            long count3 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) >= 0
//                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) < 0).count();
//            // 6-10年
//            long count4 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) >= 0
//                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) <= 0).count();
//            // 10年以上
//            long count5 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) > 0).count();
//            Map<String, Long> tdkw_socialworkage = new ListOrderedMap<>();
//            if (count1 != 0) {
//                tdkw_socialworkage.put("1年以下", count1);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> ((BigDecimal) j.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) < 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("社会工龄" + "1年以下", collect1.toString());
//            }
//            if (count2 != 0) {
//                tdkw_socialworkage.put("1-2年", count2);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) >= 0
//                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) < 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("社会工龄" + "1-2年", collect1.toString());
//            }
//            if (count3 != 0) {
//                tdkw_socialworkage.put("3-5年", count3);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) >= 0
//                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) < 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("社会工龄" + "3-5年", collect1.toString());
//            }
//            if (count4 != 0) {
//                tdkw_socialworkage.put("6-10年", count4);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) >= 0
//                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) <= 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("社会工龄" + "6-10年", collect1.toString());
//            }
//            if (count5 != 0) {
//                tdkw_socialworkage.put("10年以上", count5);
//                List<Long> collect1 = personObjects.stream()
//                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) > 0)
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("社会工龄" + "10年以上", collect1.toString());
//            }
//
//            Set<String> strings = tdkw_socialworkage.keySet();
//            ItemValue[] data = new ItemValue[tdkw_socialworkage.size()];
//            int i = 0;
//            for (String key : strings) {
//                data[i] = new ItemValue(key, tdkw_socialworkage.get(key));
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("7", pieChartType)) {
//            // 婚姻状况
//            PieSeries series = pieChart.createPieSeries("婚姻状况");
//            Map<Object, Long> tdkw_marriagestatus = personObjects.stream()
//                    .filter(i -> (Long) i.get("tdkw_marriagestatus") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_marriagestatus"), Collectors.counting()));
//            Set<Object> objects = tdkw_marriagestatus.keySet();
//            ItemValue[] data = new ItemValue[tdkw_marriagestatus.size()];
//            int i = 0;
//            for (Object key : objects) {
//                QFilter sexFilter = new QFilter("id", QCP.equals, key);
//                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_marriagestatus", "id,name", sexFilter.toArray());
//                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_marriagestatus.get(key));
//
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> j.get("tdkw_marriagestatus").equals(dynamicObject.get("id")))
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("婚姻状况" + dynamicObject.getString("name"), collect1.toString());
//
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        } else if (StringUtils.equals("8", pieChartType)) {
//            // 婚姻状况
//            PieSeries series = pieChart.createPieSeries("人员类型");
//            Map<Object, Long> tdkw_employtype = personObjects.stream()
//                    .filter(i -> (Long) i.get("tdkw_employtype") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_employtype"), Collectors.counting()));
//            Set<Object> objects = tdkw_employtype.keySet();
//            ItemValue[] data = new ItemValue[tdkw_employtype.size()];
//            int i = 0;
//            for (Object key : objects) {
//                QFilter sexFilter = new QFilter("id", QCP.equals, key);
//                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_laborreltype", "id,name", sexFilter.toArray());
//                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_employtype.get(key));
//
//                List<Long> collect1 = personObjects.stream()
//                        .filter(j -> j.get("tdkw_employtype").equals(dynamicObject.get("id")))
//                        .map(j -> j.getLong("person")).collect(Collectors.toList());
//                pageCache.put("人员类型" + dynamicObject.getString("name"), collect1.toString());
//
//                i++;
//            }
//            series.setData(data);
//            series.setRadius("50%", "75%");
//
//            Map<String, Object> labelMap = new HashMap<>();
//            Map<String, Object> normalMap = new HashMap<>();
//            normalMap.put("show", true);
//            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
//            StringBuilder formatterBuilder = new StringBuilder();
//            formatterBuilder.append("{d}%");
//            formatterBuilder.append("({c}");
//            formatterBuilder.append("人)");
//            normalMap.put("formatter", formatterBuilder.toString());
//            labelMap.put("normal", normalMap);
//            series.setPropValue("label", labelMap);
//        }
//        pieChart.setLegendPropValue("right", "0px");
//
//        logger.info("饼图耗时" + (System.currentTimeMillis() - start));
//
//    }


    // 旧版

    /**
     * @author xxx
     * @Description 生成饼图
     * @Date 2023/6/30 11:06
     */
    public void drawChart(PieChart pieChart, String pieChartType) {
        long start = System.currentTimeMillis();

        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");

        Date tdkwEnddate = (Date) this.getModel().getValue("tdkw_enddate");
        IPageCache pageCache = this.getPageCache();
        String personIds = pageCache.get(DateTimeUtils.dateFormat(tdkwEnddate, "yyyy.MM"));
        if (StringUtils.isEmpty(personIds) || StringUtils.equals("[]", personIds)) {
            return;
        }
        String[] ids = personIds.substring(1, personIds.length() - 1).replaceAll(" ", "").split(",");
        List<Long> collect = Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
        DataSet personInfos = PersonInfoUtil.queryPersonInfo(collect, this.getClass().getName());
        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(personInfos.copy());

        logger.info("饼图获取人员信息集合耗时" + (System.currentTimeMillis() - start));

        if (StringUtils.equals("0", pieChartType)) {
            // 岗位层级
            PieSeries series = pieChart.createPieSeries("岗位层级");
            Map<Object, Long> tdkwPostlevel = personObjects.stream()
                    .filter(i -> ObjectUtils.isNotEmpty(i.get("tdkw_postlevel")))
                    .collect(Collectors.groupingBy(i -> i.get("tdkw_postlevel"), Collectors.counting()));
            Set<Object> objects = tdkwPostlevel.keySet();
            ItemValue[] data = new ItemValue[tdkwPostlevel.size()];
            int i = 0;
            for (Object key : objects) {
                String keyName = key.toString();
                if (StringUtils.equals("1", keyName)) {
                    keyName = "集团高管-集团直管";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("1", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("2", keyName)) {
                    keyName = "集团高管-授权行业";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("2", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("3", keyName)) {
                    keyName = "其他高管";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("3", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("4", keyName)) {
                    keyName = "中层";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("4", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                } else if (StringUtils.equals("5", keyName)) {
                    keyName = "基层";
                    List<Long> collect1 = personObjects.stream()
                            .filter(j -> StringUtils.equals("5", j.getString("tdkw_postlevel")))
                            .map(j -> j.getLong("person"))
                            .collect(Collectors.toList());
                    pageCache.put("岗位层级" + keyName, collect1.toString());
                }
                data[i] = new ItemValue(keyName, tdkwPostlevel.get(key));
                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("1", pieChartType)) {
            // 集团司龄
            PieSeries series = pieChart.createPieSeries("集团司龄");
            // 1年以下
            Long count1 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0).collect(Collectors.counting());
            // 1-2年
            Long count2 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) < 0).collect(Collectors.counting());
            // 3-5年
            Long count3 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) >= 0
                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) < 0).collect(Collectors.counting());
            // 6-10年
            Long count4 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) >= 0
                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0).collect(Collectors.counting());
            // 10年以上
            Long count5 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0).collect(Collectors.counting());
            Map<String, Long> tdkw_comsercount = new ListOrderedMap<>();
            if (count1 != 0) {
                tdkw_comsercount.put("1年以下", count1);
                List<Long> collect1 = personObjects.stream()
                        .filter(j -> ((BigDecimal) j.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "1年以下", collect1.toString());
            }
            if (count2 != 0) {
                tdkw_comsercount.put("1-2年", count2);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "1-2年", collect1.toString());
            }
            if (count3 != 0) {
                tdkw_comsercount.put("3-5年", count3);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(3)) >= 0
                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "3-5年", collect1.toString());
            }
            if (count4 != 0) {
                tdkw_comsercount.put("6-10年", count4);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(6)) >= 0
                                && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "6-10年", collect1.toString());
            }
            if (count5 != 0) {
                tdkw_comsercount.put("10年以上", count5);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("集团司龄" + "10年以上", collect1.toString());
            }

            Set<String> strings = tdkw_comsercount.keySet();
            ItemValue[] data = new ItemValue[tdkw_comsercount.size()];
            int i = 0;
            for (String key : strings) {
                data[i] = new ItemValue(key, tdkw_comsercount.get(key));
                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("2", pieChartType)) {
            // 性别
            PieSeries series = pieChart.createPieSeries("性别");
            Map<Object, Long> tdkw_gender = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_gender") != 0)
                    .collect(Collectors.groupingBy(i -> i.get("tdkw_gender"), Collectors.counting()));
            Set<Object> objects = tdkw_gender.keySet();
            ItemValue[] data = new ItemValue[tdkw_gender.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_sex", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_gender.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_gender").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("性别" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("3", pieChartType)) {
            // 学历
            PieSeries series = pieChart.createPieSeries("学历");
            Map<Object, Long> tdkw_education = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_education") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_education"), Collectors.counting()));
            Set<Object> objects = tdkw_education.keySet();
            ItemValue[] data = new ItemValue[tdkw_education.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_diploma", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_education.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_education").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("学历" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("4", pieChartType)) {
            // 年龄
            PieSeries series = pieChart.createPieSeries("年龄");
            // 25及以下
            Long count1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 0)
                    .filter(i -> (int) i.get("tdkw_age") <= 25).collect(Collectors.counting());
            // 26-30
            Long count2 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30).collect(Collectors.counting());
            // 31-35
            Long count3 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35).collect(Collectors.counting());
            // 36-40
            Long count4 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40).collect(Collectors.counting());
            // 41-45
            Long count5 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45).collect(Collectors.counting());
            // 46-50
            Long count6 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50).collect(Collectors.counting());
            // 51-55
            Long count7 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55).collect(Collectors.counting());
            // 55以上
            Long count8 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") > 55).collect(Collectors.counting());
            Map<String, Long> tdkw_age = new ListOrderedMap<>();
            if (count1 != 0) {
                tdkw_age.put("25及以下", count1);
                List<Long> collect1 = personObjects.stream()
                        .filter(j -> (int) j.get("tdkw_age") != 0).filter(j -> (int) j.get("tdkw_age") <= 25)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "25及以下", collect1.toString());
            }
            if (count2 != 0) {
                tdkw_age.put("26-30", count2);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "26-30", collect1.toString());
            }
            if (count3 != 0) {
                tdkw_age.put("31-35", count3);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "31-35", collect1.toString());
            }
            if (count4 != 0) {
                tdkw_age.put("36-40", count4);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "36-40", collect1.toString());
            }
            if (count5 != 0) {
                tdkw_age.put("41-45", count5);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "41-45", collect1.toString());
            }
            if (count6 != 0) {
                tdkw_age.put("46-50", count6);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "46-50", collect1.toString());
            }
            if (count7 != 0) {
                tdkw_age.put("51-55", count7);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "51-55", collect1.toString());
            }
            if (count8 != 0) {
                tdkw_age.put("55以上", count8);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> (int) i.get("tdkw_age") > 55)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("年龄" + "55以上", collect1.toString());
            }

            Set<String> strings = tdkw_age.keySet();
            ItemValue[] data = new ItemValue[tdkw_age.size()];
            int i = 0;
            for (String key : strings) {
                data[i] = new ItemValue(key, tdkw_age.get(key));
                i++;
            }

            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("5", pieChartType)) {
            // 政治面貌
            PieSeries series = pieChart.createPieSeries("政治面貌");
            Map<Object, Long> tdkw_politicalstatus = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_politicalstatus") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_politicalstatus"), Collectors.counting()));
            Set<Object> objects = tdkw_politicalstatus.keySet();
            ItemValue[] data = new ItemValue[tdkw_politicalstatus.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_politicalstatus", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_politicalstatus.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_politicalstatus").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("政治面貌" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("6", pieChartType)) {
            // 社会工龄
            PieSeries series = pieChart.createPieSeries("社会工龄");
            // 1年以下
            Long count1 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) < 0).collect(Collectors.counting());
            // 1-2年
            Long count2 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) >= 0
                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) < 0).collect(Collectors.counting());
            // 3-5年
            Long count3 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) >= 0
                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) < 0).collect(Collectors.counting());
            // 6-10年
            Long count4 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) >= 0
                            && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) <= 0).collect(Collectors.counting());
            // 10年以上
            Long count5 = personObjects.stream()
                    .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) > 0).collect(Collectors.counting());
            Map<String, Long> tdkw_socialworkage = new ListOrderedMap<>();
            if (count1 != 0) {
                tdkw_socialworkage.put("1年以下", count1);
                List<Long> collect1 = personObjects.stream()
                        .filter(j -> ((BigDecimal) j.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "1年以下", collect1.toString());
            }
            if (count2 != 0) {
                tdkw_socialworkage.put("1-2年", count2);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(1)) >= 0
                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "1-2年", collect1.toString());
            }
            if (count3 != 0) {
                tdkw_socialworkage.put("3-5年", count3);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(3)) >= 0
                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) < 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "3-5年", collect1.toString());
            }
            if (count4 != 0) {
                tdkw_socialworkage.put("6-10年", count4);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(6)) >= 0
                                && ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) <= 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "6-10年", collect1.toString());
            }
            if (count5 != 0) {
                tdkw_socialworkage.put("10年以上", count5);
                List<Long> collect1 = personObjects.stream()
                        .filter(i -> ((BigDecimal) i.get("tdkw_socialworkage")).compareTo(new BigDecimal(10)) > 0)
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("社会工龄" + "10年以上", collect1.toString());
            }

            Set<String> strings = tdkw_socialworkage.keySet();
            ItemValue[] data = new ItemValue[tdkw_socialworkage.size()];
            int i = 0;
            for (String key : strings) {
                data[i] = new ItemValue(key, tdkw_socialworkage.get(key));
                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("7", pieChartType)) {
            // 婚姻状况
            PieSeries series = pieChart.createPieSeries("婚姻状况");
            Map<Object, Long> tdkw_marriagestatus = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_marriagestatus") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_marriagestatus"), Collectors.counting()));
            Set<Object> objects = tdkw_marriagestatus.keySet();
            ItemValue[] data = new ItemValue[tdkw_marriagestatus.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_marriagestatus", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_marriagestatus.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_marriagestatus").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("婚姻状况" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        } else if (StringUtils.equals("8", pieChartType)) {
            // 婚姻状况
            PieSeries series = pieChart.createPieSeries("人员类型");
            Map<Object, Long> tdkw_employtype = personObjects.stream()
                    .filter(i -> (Long) i.get("tdkw_employtype") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_employtype"), Collectors.counting()));
            Set<Object> objects = tdkw_employtype.keySet();
            ItemValue[] data = new ItemValue[tdkw_employtype.size()];
            int i = 0;
            for (Object key : objects) {
                QFilter sexFilter = new QFilter("id", QCP.equals, key);
                DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_laborreltype", "id,name", sexFilter.toArray());
                data[i] = new ItemValue(dynamicObject.getString("name"), tdkw_employtype.get(key));

                List<Long> collect1 = personObjects.stream()
                        .filter(j -> j.get("tdkw_employtype").equals(dynamicObject.get("id")))
                        .map(j -> j.getLong("person")).collect(Collectors.toList());
                pageCache.put("人员类型" + dynamicObject.getString("name"), collect1.toString());

                i++;
            }
            series.setData(data);
            series.setRadius("50%", "75%");

            Map<String, Object> labelMap = new HashMap<>();
            Map<String, Object> normalMap = new HashMap<>();
            normalMap.put("show", true);
            //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
            StringBuilder formatterBuilder = new StringBuilder();
            formatterBuilder.append("{d}%");
            formatterBuilder.append("({c}");
            formatterBuilder.append("人)");
            normalMap.put("formatter", formatterBuilder.toString());
            labelMap.put("normal", normalMap);
            series.setPropValue("label", labelMap);
        }
        pieChart.setLegendPropValue("right", "0px");

        logger.info("饼图耗时" + (System.currentTimeMillis() - start));

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

        logger.info("柱状图任职经历时间条件筛选耗时" + (System.currentTimeMillis() - start));
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

        Map<Long, List<Long>> orgForPersons = new HashMap<>();
        Map<Long, DynamicObject> orgNameMap = new HashMap<>();
        List<Long> dirOrgPersonAll = new ArrayList<>();

        List<DynamicObject> collects = personDy.stream().filter(i -> i.getDate("startdate")
                .compareTo(tdkwEnddate) <= 0 && i.getDate("sysenddate")
                .compareTo(tdkwEnddate) >= 0).collect(Collectors.toList());


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
        if (haos_adminorghr.size() != 0 && dirOrgPersons.size() != 0) {
            dirOrgPersonAll.addAll(dirOrgPersons);
        }

        //汇总人数
        if (dirOrgPersonAll.size() != 0) {
            xNames.add(dataEntity.getDynamicObject("tdkw_org").getString("name"));
            yDatas.add(dirOrgPersonAll.size());
            this.getPageCache().put(this.getClass().getName() + dataEntity.getDynamicObject("tdkw_org").getString("name"), dirOrgPersonAll.toString());
        }

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

        logger.info("总人数耗时" + (System.currentTimeMillis() - start));

//        //开始绘制人员平均年龄柱状图
//        HistogramChart histogramChartAgeavg = this.getControl("tdkw_ageavgchartap");
//        this.drawChartAgeavg(histogramChartAgeavg, personObjects, haos_adminorghr, finalOrgIds, orgForPersons, orgNameMap);
//
//        //开始汇总简历完整度柱状图
//        HistogramChart histogramChartResume = this.getControl("tdkw_resumechartap");
//        this.drawChartResume(histogramChartResume, personObjects, haos_adminorghr, finalOrgIds, orgForPersons, orgNameMap);
//        //开始绘制政治面貌柱状图
//        HistogramChart politicalChartResume = this.getControl("tdkw_politicalchartap");
//        this.drawChartPolitical(politicalChartResume, personObjects, dirOrgPersonAll, finalOrgIds, orgForPersons, orgNameMap);
//        DynamicObject dataEntity1 = this.getModel().getDataEntity();
//        this.getPageCache().put("orgId", String.valueOf(dataEntity1.getLong("tdkw_org.id")));


    }


    /**
     * @description: 绘制总人数
     * @author xxx
     * @date: 2023/10/22 14:55
     * @param:
     * @param: histogramChart
     * @param: xNames
     * @param: yDatas
     * @return: void
     **/
    private void drawHeadCount(HistogramChart histogramChart, List<String> xNames, List<Number> yDatas) {
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
        x.setPropValue("data", xNames);
        Map<String, Object> axisLabel = new HashMap<>();
        axisLabel.put("rotate", 15);
        axisLabel.put("interval", 0);
        x.setPropValue("axisLabel", axisLabel);
        barseries.setData(yDatas.toArray(new Number[0]));
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
    }

    /**
     * 绘制平均年龄柱状图表
     *
     * @param histogramChart
     */
    public void drawChartAgeavg(HistogramChart histogramChart, List<String> xNameList, List<Number> yDataList) {
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
        x.setPropValue("data", xNameList);
        Map<String, Object> axisLabel = new HashMap<>();
        axisLabel.put("rotate", 15);
        axisLabel.put("interval", 0);
        x.setPropValue("axisLabel", axisLabel);
        barseries.setData(yDataList.toArray(new Number[0]));
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
    }

    /**
     * 绘制简历完整度柱状图表
     *
     * @param histogramChart
     */
    public void drawChartResume(HistogramChart histogramChart, List<String> xNameList, List<Number> yDataList) {
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
        x.setPropValue("data", xNameList);
        Map<String, Object> axisLabel = new HashMap<>();
        axisLabel.put("rotate", 15);
        axisLabel.put("interval", 0);
        x.setPropValue("axisLabel", axisLabel);
        barseries.setData(yDataList.toArray(new Number[0]));
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
    }

    /**
     * 绘制政治面貌柱状图
     *
     * @param histogramChart
     * @param personObjectss
     * @param dirOrgPersonAll
     * @param finalOrgIds
     * @param orgForPersons
     * @param orgNameMap
     */
    public void drawChartPolitical(HistogramChart histogramChart, DynamicObjectCollection personObjectss, List<Long> dirOrgPersonAll, List<Long> finalOrgIds, Map<Long, List<Long>> orgForPersons, Map<Long, DynamicObject> orgNameMap) {
        long start = System.currentTimeMillis();
        //设置是否显示图例
        histogramChart.setShowLegend(true);
        //设置图例方向
        boolean isLegendVertical = false;
        histogramChart.setLegendVertical(isLegendVertical);
        // 设置图例位置
//        histogramChart.setLegendAlign(XAlign.center, YAlign.top);
        histogramChart.setLegendPropValue("x", XAlign.right);
        histogramChart.setLegendPropValue("y", "10%");

        Map<String, Object> titlePropValue = new HashMap<>();
        // 主标题内容：text
        titlePropValue.put("text", "条形图");
        // 设置触发提示框的类型为axis：坐标轴触发
        histogramChart.addTooltip("trigger", "axis");
        // 字符串模板格式
//        List<Object> toolTipFuncPath = new ArrayList<>();
//        String formatter = "{b0}: {c0}";
//        histogramChart.addTooltip("formatter", formatter);
//        toolTipFuncPath.add("tooltip");
//        toolTipFuncPath.add("formatter");
//        histogramChart.addFuncPath(toolTipFuncPath);
        histogramChart.setShowTooltip(true);


        //AxisType可以调整纵轴或横轴显示数据
        Axis x = histogramChart.createXAxis("组织", AxisType.category);
        Axis y = histogramChart.createYAxis("", AxisType.value);
        Map<String, Object> yAxisLabel = new HashMap<>();
        yAxisLabel.put("formatter", "{value}%");
        y.setPropValue("axisLabel", yAxisLabel);
        x.setPropValue("axisLabel", yAxisLabel);

        DynamicObject dataEntity = this.getModel().getDataEntity();
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return;
        }


        List<String> xNames = new ArrayList<>();
        List<ItemValue> yData1s = new ArrayList<>();
        List<ItemValue> yData2s = new ArrayList<>();
        List<ItemValue> yData3s = new ArrayList<>();
        List<ItemValue> yData4s = new ArrayList<>();

        //筛选当前时间下符合的数据
        List<DynamicObject> personObjects = personObjectss.stream()
                .filter(person -> dirOrgPersonAll.contains(person.getLong("person")))
                .collect(Collectors.toList());

        //查询高层人数
        List<Long> seniorPartyPersons = personObjects.stream()
                .filter(i -> (Long) i.get("tdkw_company") != 0)
                .filter(i -> StringUtils.equals("1", i.getString("tdkw_postlevel")) || StringUtils.equals("2", i.getString("tdkw_postlevel")))
                .map(i -> i.getLong("person")).collect(Collectors.toList());
        //查询中共党员(高管）
        List<Long> seniorPartyMembers = personObjects.stream()
                .filter(i -> (Long) i.get("tdkw_company") != 0)
                .filter(i -> StringUtils.equals("1", i.getString("tdkw_postlevel")) || StringUtils.equals("2", i.getString("tdkw_postlevel")))
                .filter(i -> null != i.get("politicalstatus.name")
                        && (i.getString("politicalstatus.name").equals("中共党员") || i.getString("politicalstatus.name").equals("中共预备党员")))
                .map(i -> i.getLong("person")).collect(Collectors.toList());
        //查询非中共党员(高管）
        List<Long> nonSeniorPartyMembers = seniorPartyPersons.stream().filter(i -> !seniorPartyMembers.contains(i))
                .collect(Collectors.toList());

        //查询中层人数
        List<Long> middlePartyPersons = personObjects.stream()
                .filter(i -> (Long) i.get("tdkw_company") != 0)
                .filter(i -> StringUtils.equals("3", i.getString("tdkw_postlevel")) || StringUtils.equals("4", i.getString("tdkw_postlevel")))
                .map(i -> i.getLong("person")).collect(Collectors.toList());
        //查询中共党员(中层)
        List<Long> middlePartyMembers = personObjects.stream()
                .filter(i -> (Long) i.get("tdkw_company") != 0)
                .filter(i -> StringUtils.equals("3", i.getString("tdkw_postlevel")) || StringUtils.equals("4", i.getString("tdkw_postlevel")))
                .filter(i -> null != i.get("politicalstatus.name")
                        && (i.getString("politicalstatus.name").equals("中共党员") || i.getString("politicalstatus.name").equals("中共预备党员")))
                .map(i -> i.getLong("person")).collect(Collectors.toList());
        //查询非中共党员(中层)
        List<Long> nonMiddlePartyMembers = middlePartyPersons.stream().filter(i -> !middlePartyMembers.contains(i))
                .collect(Collectors.toList());

        for (Long finalOrgId : finalOrgIds) {
            List<Long> persons = orgForPersons.get(finalOrgId);
            if (null == persons || persons.size() == 0) {
                continue;
            }
            List<Long> finalPersons = persons;
            if (finalPersons.size() == 0) {
                continue;
            }
            //查询对应组织下的高层人数
            List<Long> seniorPartyPerson = new ArrayList<>(persons);
            seniorPartyPerson.retainAll(seniorPartyPersons);

            //查询对应组织下的中共党员（高管）
            List<Long> seniorPartyMember = new ArrayList<>(persons);
            seniorPartyMember.retainAll(seniorPartyMembers);

            //查询对应组织下的非中共党员（高管）
            List<Long> nonSeniorPartyMember = new ArrayList<>(persons);
            nonSeniorPartyMember.retainAll(nonSeniorPartyMembers);

            //查询对应组织下的中层人数
            List<Long> middlePartyPerson = new ArrayList<>(persons);
            middlePartyPerson.retainAll(middlePartyPersons);

            if (middlePartyPerson.size() == 0 && seniorPartyPerson.size() == 0) {
                continue;
            }

            //查询对应组织下的中共党员(中层)
            List<Long> middlePartyMember = new ArrayList<>(persons);
            middlePartyMember.retainAll(middlePartyMembers);

            //查询对应组织下的非中共党员(中层)
            List<Long> nonMiddlePartyMember = new ArrayList<>(persons);
            nonMiddlePartyMember.retainAll(nonMiddlePartyMembers);

            DynamicObject orgObject = orgNameMap.get(finalOrgId);
            xNames.add(orgObject.getString("name"));
            if (seniorPartyPerson.size() != 0) {
                BigDecimal seniordivide = new BigDecimal(seniorPartyMember.size()).divide(new BigDecimal(seniorPartyPerson.size()), 5, BigDecimal.ROUND_DOWN);
                seniordivide = seniordivide.multiply(new BigDecimal(100));
                seniordivide = seniordivide.setScale(2, BigDecimal.ROUND_DOWN);
                yData1s.add(new ItemValue(seniorPartyMember.size() + "中共党员（高管）" + orgObject.getString("name"), seniordivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : seniordivide, null));

                this.getPageCache().put(this.getClass().getName() + seniorPartyMember.size() + "中共党员（高管）" + orgObject.getString("name"), seniorPartyMember.toString());

                BigDecimal nonseniordivide = new BigDecimal(nonSeniorPartyMember.size()).divide(new BigDecimal(seniorPartyPerson.size()), 5, BigDecimal.ROUND_DOWN);
                nonseniordivide = nonseniordivide.multiply(new BigDecimal(100));
                nonseniordivide = nonseniordivide.setScale(2, BigDecimal.ROUND_DOWN);
                yData2s.add(new ItemValue(nonSeniorPartyMember.size() + "非中共党员（高管）" + orgObject.getString("name"), nonseniordivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : nonseniordivide, null));
                this.getPageCache().put(this.getClass().getName() + nonSeniorPartyMember.size() + "非中共党员（高管）" + orgObject.getString("name"), nonSeniorPartyMember.toString());

            } else {
                yData1s.add(new ItemValue(seniorPartyMember.size() + "中共党员（高管）" + orgObject.getString("name"), 0, null));
                yData2s.add(new ItemValue(nonSeniorPartyMember.size() + "非中共党员（高管）" + orgObject.getString("name"), 0, null));
            }


            if (middlePartyPerson.size() != 0) {
                BigDecimal middledivide = new BigDecimal(middlePartyMember.size()).divide(new BigDecimal(middlePartyPerson.size()), 5, BigDecimal.ROUND_DOWN);
                middledivide = middledivide.multiply(new BigDecimal(100));
                middledivide = middledivide.setScale(2, BigDecimal.ROUND_DOWN);
                yData3s.add(new ItemValue(middlePartyMember.size() + "中共党员(中层)" + orgObject.getString("name"), middledivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : middledivide, null));
                this.getPageCache().put(this.getClass().getName() + middlePartyMember.size() + "中共党员(中层)" + orgObject.getString("name"), middlePartyMember.toString());

                BigDecimal nonmiddledivide = new BigDecimal(nonMiddlePartyMember.size()).divide(new BigDecimal(middlePartyPerson.size()), 5, BigDecimal.ROUND_DOWN);
                nonmiddledivide = nonmiddledivide.multiply(new BigDecimal(100));
                nonmiddledivide = nonmiddledivide.setScale(2, BigDecimal.ROUND_DOWN);
                yData4s.add(new ItemValue(nonMiddlePartyMember.size() + "非中共党员(中层)" + orgObject.getString("name"), nonmiddledivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : nonmiddledivide, null));
                this.getPageCache().put(this.getClass().getName() + nonMiddlePartyMember.size() + "非中共党员(中层)" + orgObject.getString("name"), nonMiddlePartyMember.toString());

            } else {
                yData3s.add(new ItemValue(middlePartyMember.size() + "中共党员(中层)" + orgObject.getString("name"), 0, null));
                yData4s.add(new ItemValue(nonMiddlePartyMember.size() + "非中共党员(中层)" + orgObject.getString("name"), 0, null));
            }

        }


//        // 直属组织下人数
//        List<Long> dirOrgPersons = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue())).map(i -> i.getLong("person")).collect(Collectors.toList());
//        if (haos_adminorghr.size() != 0 && dirOrgPersons.size() != 0) {
//            //查询对应组织下的中共党员（高管）
//            List<Long> seniorPartyMember = new ArrayList<>(dirOrgPersons);
//            seniorPartyMember.retainAll(seniorPartyMembers);
//            //查询对应组织下的非中共党员（高管）
//            List<Long> nonseniorPartyMember = new ArrayList<>(dirOrgPersons);
//            nonseniorPartyMember.retainAll(nonSeniorPartyMembers);
//            //查询对应组织下的中共党员(中层)
//            List<Long> middlePartyMember = new ArrayList<>(dirOrgPersons);
//            middlePartyMember.retainAll(middlePartyMembers);
//            //查询对应组织下的非中共党员(中层)
//            List<Long> nonmiddlePartyMember = new ArrayList<>(dirOrgPersons);
//            nonmiddlePartyMember.retainAll(nonMiddlePartyMembers);
//        }
        //汇总数
        if (seniorPartyPersons.size() != 0 || middlePartyPersons.size() != 0) {
            xNames.add(dataEntity.getDynamicObject("tdkw_org").getString("name"));
        }

        //汇总数
        if (seniorPartyPersons.size() != 0) {

            BigDecimal seniordivide = new BigDecimal(seniorPartyMembers.size()).divide(new BigDecimal(seniorPartyPersons.size()), 5, BigDecimal.ROUND_DOWN);
            seniordivide = seniordivide.multiply(new BigDecimal(100));
            seniordivide = seniordivide.setScale(2, BigDecimal.ROUND_DOWN);
            yData1s.add(new ItemValue(seniorPartyMembers.size() + "中共党员（高管）" + dataEntity.getDynamicObject("tdkw_org").getString("name"), seniordivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : seniordivide, null));
            this.getPageCache().put(this.getClass().getName() + seniorPartyMembers.size() + "中共党员（高管）" + dataEntity.getDynamicObject("tdkw_org").getString("name"), seniorPartyMembers.toString());

            BigDecimal nonseniordivide = new BigDecimal(nonSeniorPartyMembers.size()).divide(new BigDecimal(seniorPartyPersons.size()), 5, BigDecimal.ROUND_DOWN);
            nonseniordivide = nonseniordivide.multiply(new BigDecimal(100));
            nonseniordivide = nonseniordivide.setScale(2, BigDecimal.ROUND_DOWN);
            yData2s.add(new ItemValue(nonSeniorPartyMembers.size() + "非中共党员（高管）" + dataEntity.getDynamicObject("tdkw_org").getString("name"), nonseniordivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : nonseniordivide, null));
            this.getPageCache().put(this.getClass().getName() + nonSeniorPartyMembers.size() + "非中共党员（高管）" + dataEntity.getDynamicObject("tdkw_org").getString("name"), nonSeniorPartyMembers.toString());
        } else {
            yData1s.add(new ItemValue(seniorPartyMembers.size() + "中共党员（高管）" + dataEntity.getDynamicObject("tdkw_org").getString("name"), 0, null));
            yData2s.add(new ItemValue(nonSeniorPartyMembers.size() + "非中共党员（高管）" + dataEntity.getDynamicObject("tdkw_org").getString("name"), 0, null));

        }
        //汇总数
        if (middlePartyPersons.size() != 0) {
            BigDecimal middledivide = new BigDecimal(middlePartyMembers.size()).divide(new BigDecimal(middlePartyPersons.size()), 5, BigDecimal.ROUND_DOWN);
            middledivide = middledivide.multiply(new BigDecimal(100));
            middledivide = middledivide.setScale(2, BigDecimal.ROUND_DOWN);
            yData3s.add(new ItemValue(middlePartyMembers.size() + "中共党员(中层)" + dataEntity.getDynamicObject("tdkw_org").getString("name"), middledivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : middledivide, null));
            this.getPageCache().put(this.getClass().getName() + middlePartyMembers.size() + "中共党员(中层)" + dataEntity.getDynamicObject("tdkw_org").getString("name"), middlePartyMembers.toString());

            BigDecimal nonmiddledivide = new BigDecimal(nonMiddlePartyMembers.size()).divide(new BigDecimal(middlePartyPersons.size()), 5, BigDecimal.ROUND_DOWN);
            nonmiddledivide = nonmiddledivide.multiply(new BigDecimal(100));
            nonmiddledivide = nonmiddledivide.setScale(2, BigDecimal.ROUND_DOWN);
            yData4s.add(new ItemValue(nonMiddlePartyMembers.size() + "非中共党员(中层)" + dataEntity.getDynamicObject("tdkw_org").getString("name"), nonmiddledivide.compareTo(BigDecimal.ZERO) == 0 ? 0 : nonmiddledivide, null));
            this.getPageCache().put(this.getClass().getName() + nonMiddlePartyMembers.size() + "非中共党员(中层)" + dataEntity.getDynamicObject("tdkw_org").getString("name"), nonMiddlePartyMembers.toString());

        } else {
            yData3s.add(new ItemValue(middlePartyMembers.size() + "中共党员(中层)" + dataEntity.getDynamicObject("tdkw_org").getString("name"), 0, null));
            yData4s.add(new ItemValue(nonMiddlePartyMembers.size() + "非中共党员(中层)" + dataEntity.getDynamicObject("tdkw_org").getString("name"), 0, null));
        }


        x.setPropValue("data", xNames);

        HashMap<String, Object> map = new HashMap();
        HashMap<String, Object> normap = new HashMap();
        map.put("focus", "series");
        normap.put("normal", map);


        BarSeries barseries = histogramChart.createSeries("总人数");
        // 设置系列名称，用于tooltip的显示，legend 的图例筛选.
        barseries.setName("中共党员（高管）");
        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
        barseries.setStack("高层数量");
        barseries.setPropValue("emphasis", map);
        barseries.setPropValue("barGap", "5%");
        Map<String, Object> axisLabel = new HashMap<>();
        axisLabel.put("rotate", 15);
        axisLabel.put("interval", 0);
        x.setPropValue("axisLabel", axisLabel);
        barseries.setData(yData1s.toArray(new ItemValue[yData1s.size()]));
        barseries.addData(888);

        // 设置柱条颜色（直接设置颜色值和渐变设置）
//        barseries.setColor("#d0332f");

//        barseries.setBarWidth("20px");
        Label label = new Label();
        label.setShow(false);
        label.setColor("#000000");
        label.setPosition(Position.left);
        barseries.setLabel(label);

        BarSeries barseries2 = histogramChart.createSeries("总人数");
        barseries2.setName("非中共党员（高管）");
        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
        barseries2.setStack("高层数量");
        barseries2.setData(yData2s.toArray(new ItemValue[yData2s.size()]));
//        barseries2.setData(yData2s.toArray(new Number[yData2s.size()]));
        barseries2.setPropValue("emphasis", map);
        barseries2.setPropValue("barGap", "5%");

//        barseries2.setBarWidth("20px");
        Label label2 = new Label();
        label2.setShow(false);
        label2.setColor("#000000");

        barseries2.setLabel(label2);

        BarSeries barseries3 = histogramChart.createSeries("总人数");
        barseries3.setName("中共党员(中层)");
        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
        barseries3.setStack("中层数量");
        barseries3.setData(yData3s.toArray(new ItemValue[yData3s.size()]));
//        barseries2.setData(yData2s.toArray(new Number[yData2s.size()]));
        barseries3.setPropValue("emphasis", map);
        barseries3.setPropValue("barGap", "5%");

//        barseries3.setBarWidth("20px");
        Label label3 = new Label();
        label3.setShow(false);
        label3.setColor("#000000");
        label3.setPosition(Position.right);

        barseries3.setLabel(label3);

        BarSeries barseries4 = histogramChart.createSeries("总人数");
        barseries4.setName("非中共党员(中层)");
        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
        barseries4.setStack("中层数量");
        barseries4.setData(yData4s.toArray(new ItemValue[yData4s.size()]));
//        barseries2.setData(yData2s.toArray(new Number[yData2s.size()]));
        barseries4.setPropValue("emphasis", map);
        barseries4.setPropValue("barGap", "5%");

//        barseries4.setBarWidth("20px");
        Label label4 = new Label();
        label4.setShow(false);
        label4.setColor("#000000");


        barseries4.setLabel(label4);

//        BarSeries barseries3 = histogramChart.createSeries("总人数");
//        barseries3.setName("非党员");
//        // 设置数据堆叠，同个类目轴上系列配置相同的stack值可以堆叠放置
//        barseries3.setStack("数量");
//        barseries3.setData(yData3s.toArray(new ItemValue[yData3s.size()]));
////        barseries3.setData(yData3s.toArray(new Number[yData3s.size()]));
//
//        barseries3.setPropValue("emphasis", map);
//
//        barseries3.setBarWidth("20px");
//        Label label3 = new Label();
//        label3.setShow(true);
//        label3.setColor("#000000");
//
//        barseries3.setLabel(label3);

        List<Map<String, Object>> dataZoom = new ArrayList<>();
        Map<String, Object> dataZoomX = Maps.newHashMap();
        dataZoomX.put("id", "dataZoomX");
        dataZoomX.put("type", "slider");
        dataZoom.add(dataZoomX);
        histogramChart.addProperty("dataZoom", dataZoom);
        histogramChart.addProperty("height", "200px");
        List<Object> toolTipFuncPath = new ArrayList<>();
        String formatter = "function(data)" +
                "{console.log(data);" +
                "let str =data[0].axisValue + '</br>';" +
                "for(let i=0;i<data.length;i++){" +
                "let numbers = data[i].name.replace(/\\D/g, \"\");" +
                "let item = data[i]; str = str+ item.marker + item.seriesName + ' ';" +
                "if(item.seriesName === '中共党员(高管)' || item.seriesName === '中共党员(中层)'){" +
//                "str = str + ' &nbsp;&nbsp;&npsp;  ';" +
                "}" +
                "str = str + ': ' + numbers + '('+item.data.value+'%)</br>';}" +
                "return str;}";
        histogramChart.addTooltip("formatter", formatter);
        toolTipFuncPath.add("tooltip");
        toolTipFuncPath.add("formatter");
        histogramChart.addFuncPath(toolTipFuncPath);
        histogramChart.refresh();


        logger.info("政治面貌柱状图耗时" + (System.currentTimeMillis() - start));

    }

    /**
     * 获取人员任职经历
     *
     * @param tdkwOrg
     * @return
     */
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

    /**
     * 计算集团人员平均年龄(不随条件变化)
     *
     * @return
     */
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


    /**
     * 查询所有需要的数据
     *
     * @param dataEntity
     * @param tdkwBegindate
     * @param tdkwOrg
     * @return
     */
    private DynamicObjectCollection getData(DynamicObject dataEntity, Date tdkwBegindate, DynamicObject tdkwOrg) {
        // ===================处理数据开始=========================
        String name = this.getClass().getName();
        // 权限控制
        long currentUserId = UserServiceHelper.getCurrentUserId();
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currentUserId, "tdkw_ry_zrs_pc");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //结束时间
        Date tdkwEnddate = dataEntity.getDate("tdkw_enddate");
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
        // 职务
        if (ObjectUtils.isNotEmpty(tdkw_jobsequence) && tdkw_jobsequence.size() != 0) {
            List<Object> jobSequence = tdkw_jobsequence.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("empposrel.tdkw_jobsequence", QCP.in, jobSequence);
        }
        // 性别
        if (ObjectUtils.isNotEmpty(tdkw_gender) && tdkw_gender.size() != 0) {
            List<Object> gender = tdkw_gender.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("pernontsprop.gender", QCP.in, gender);
        }
        // 人员类型
        if (ObjectUtils.isNotEmpty(tdkw_employtype) && tdkw_employtype.size() != 0) {
            List<Object> employType = tdkw_employtype.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("empposrel.tdkw_employtype", QCP.in, employType);
        }
        // 职务
        if (ObjectUtils.isNotEmpty(tdkw_job) && tdkw_job.size() != 0) {
            List<Object> job = tdkw_job.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            erManFileFiler.and("empposrel.tdkw_ranks", QCP.in, job);
        }
        // 岗位层级
        if (ObjectUtils.isNotEmpty(tdkw_postlevel)) {
            String[] postLevel = tdkw_postlevel.substring(1, tdkw_postlevel.length() - 1).split(",");
            erManFileFiler.and("empposrel.tdkw_postlevel", QCP.in, postLevel);
        }

        //获取需过滤的组织ID
        List<Long> allPersonByOrg = new ArrayList<>();
        if (tdkwOrg.getLong("id") != 100000 || !hasAllOrgPerm) {
            //非全集团需要获取组织信息
            allPersonByOrg = PersonInfoUtil.getAllPersonByOrg((Long) tdkwOrg.getPkValue());
        }
        // =============统一到一个查询接口中，避免重复查询，提高效率============

        // 判断是否有全集团权限
        boolean haveAllPerm = (tdkwOrg.getLong("id") != 100000 || !hasAllOrgPerm);
        // 人事业务档案
        erManFileFiler.and("businessstatus", QCP.equals, "1");
        erManFileFiler.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFileFiler.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFileFiler.and("empposrel.datastatus", QCP.equals, "1");
        erManFileFiler.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        DataSet ermanDataSet = ORM.create().queryDataSet(name, "hspm_ermanfile", "person.id,id as ermanid", new QFilter[]{erManFileFiler});
        // 学历
        if (ObjectUtils.isNotEmpty(tdkw_education) && tdkw_education.size() != 0) {
            List<Object> education = tdkw_education.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter eduExpFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
            eduExpFilter.and("education", QCP.in, education);
            eduExpFilter.and("ishighestdegree", QCP.equals, "1");
            // 教育经历
            DataSet eduExpDataSet = ORM.create().queryDataSet(name, "hrpi_pereduexp", "person.id", eduExpFilter.toArray());
            ermanDataSet = ermanDataSet.join(eduExpDataSet).on("person.id", "person.id").select("person.id", "ermanid").finish();
        }
        if (ObjectUtils.isNotEmpty(tdkw_politicalstatus) && tdkw_politicalstatus.size() != 0) {
            // 政治面貌
            List<Object> politicalStatus = tdkw_politicalstatus.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter poliFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
            poliFilter.and("politicalstatus", QCP.in, politicalStatus);
            // 基本信息补充
            DataSet poliDataSet = ORM.create().queryDataSet(name, "hrpi_perregion", "person.id", poliFilter.toArray());
            ermanDataSet = ermanDataSet.join(poliDataSet).on("person.id", "person.id").select("person.id", "ermanid").finish();
        }
        // 任职经历
        QFilter empFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        empFilter.and("datastatus", QCP.equals, "1");
        empFilter.and("isprimary", QCP.equals, "1");
        empFilter.and("enddate", QCP.large_equals, tdkwBegindate);
        // 非全集团才做权限过滤
        if (haveAllPerm) {
            empFilter.and("adminorg", QCP.in, allPersonByOrg);
        }
        DataSet empDataSet = ORM.create().queryDataSet(name, "hrpi_empposorgrel", "id,person.id,startdate,sysenddate," +
                "tdkw_postlevel,businessstatus,adminorg.id as adminorgid,company.id as tdkw_company,adminorg.structlongnumber", empFilter.toArray());

        // 人员非时序
        QFilter propFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        DataSet propDataSet = ORM.create().queryDataSet(name, "hrpi_pernontsprop", "person.id,age as tdkw_age,tdkw_progressbar", new QFilter[]{propFilter});

        // 人员信息补充
        QFilter regionFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        regionFilter.and("datastatus", QCP.equals, "1");
        DataSet regionDataSet = ORM.create().queryDataSet(name, "hrpi_perregion", "person.id,politicalstatus.number", new QFilter[]{regionFilter});

        // 合并查询
        DataSet finish = empDataSet.leftJoin(propDataSet).on("person.id", "person.id").select("person.id", "startdate", "sysenddate", "tdkw_postlevel",
                        "businessstatus", "adminorgid", "tdkw_company", "tdkw_age", "tdkw_progressbar", "adminorg.structlongnumber").finish()
                .leftJoin(regionDataSet).on("person.id", "person.id").select("person.id", "startdate", "sysenddate", "tdkw_postlevel",
                        "businessstatus", "adminorgid", "tdkw_company", "tdkw_age", "tdkw_progressbar", "politicalstatus.number", "adminorg.structlongnumber").finish()
                .join(ermanDataSet).on("person.id", "person.id").select("person.id", "startdate", "sysenddate", "tdkw_postlevel",
                        "businessstatus", "adminorgid", "tdkw_company", "tdkw_age", "tdkw_progressbar", "politicalstatus.number", "adminorg.structlongnumber").finish();
        return ORM.create().toPlainDynamicObjectCollection(finish);
        // ===================处理数据结束=========================
    }

}



