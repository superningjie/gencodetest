package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import dm.jdbc.util.StringUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.form.ShowType;
import kd.bos.form.chart.*;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ChartClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.QueryServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/6/20 17:29
 * @Description 人员分析 人员信息简历完整度(%) 柱状图表单插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personanalysis
 * @Version 1.0
 **/
public class PersonResumeHistogramChartFormPlugin extends AbstractFormPlugin {
    private Log logger = LogFactory.getLog(PersonResumeHistogramChartFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_resumechartap");
        this.addClickListeners("tdkw_querybtn");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        if (key.equals("tdkw_resumechartap")) {
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
            HistogramChart histogramChart = this.getControl("tdkw_resumechartap");
            this.drawChart(histogramChart);
            DynamicObject dataEntity = this.getModel().getDataEntity();
            this.getPageCache().put("orgId", String.valueOf(dataEntity.getLong("tdkw_org.id")));
            this.getView().updateView("tdkw_resumechartap");
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        HistogramChart histogramChart = this.getControl("tdkw_resumechartap");
        this.drawChart(histogramChart);
        DynamicObject dataEntity = this.getModel().getDataEntity();
        this.getPageCache().put("orgId", String.valueOf(dataEntity.getLong("tdkw_org.id")));
    }

//    @Override
//    public void propertyChanged(PropertyChangedArgs e) {
//        super.propertyChanged(e);
//        String name = e.getProperty().getName();
//        HistogramChart histogramChart = this.getControl("tdkw_resumechartap");
//        switch (name) {
//            case "tdkw_org":
//            case "tdkw_begindate":
//            case "tdkw_enddate":
//            case "tdkw_education":
//            case "tdkw_jobsequence":
//            case "tdkw_politicalstatus":
//            case "tdkw_gender":
//            case "tdkw_employtype":
//            case "tdkw_job":
//            case "tdkw_postlevel":
//                this.drawChart(histogramChart);
//                this.getView().updateView("tdkw_resumechartap");
//                break;
//            default:
//                break;
//        }
//    }

    /**
     * @author xxx
     * @Description 展示图表
     * @Date 2023/6/20 17:19
     */
    public void drawChart(HistogramChart histogramChart) {
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
//        QFilter filter = new QFilter("parent", QCP.equals, tdkwOrg.getPkValue());
//        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        filter.and("enable", QCP.equals, "1");
//        //查询出hr组织数据并进行排序
//        DynamicObject[] load = BusinessDataServiceHelper.load("haos_adminorghr", "id,name", filter.toArray(), "sortcode");
//
////        DynamicObjectCollection haos_adminorghr = QueryServiceHelper.query("haos_adminorghr", "id,name", filter.toArray());
//        DynamicObjectCollection haos_adminorghr = new DynamicObjectCollection();
//        for (DynamicObject dynamicObject : load) {
//            haos_adminorghr.add(dynamicObject);
//        }
//        List<Long> orgIds = haos_adminorghr.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
//        if (orgIds.size() == 0) {
//            orgIds = Collections.singletonList((Long) tdkwOrg.getPkValue());
//        }
//        List<Long> finalOrgIds = orgIds;
        Date tdkwEnddate = dataEntity.getDate("tdkw_enddate");

        String personIds = this.getPageCache().get(DateTimeUtils.dateFormat(tdkwEnddate, "yyyy.MM"));
        if (StringUtils.equals("[]", personIds) || StringUtils.isEmpty(personIds)) {
            return;
        }
//        String[] ids = personIds.substring(1, personIds.length() - 1).replaceAll(" ", "").split(",");
//        List<Long> collect = Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
//        DataSet personInfos = PersonInfoUtil.queryPersonInfo(collect, this.getClass().getName());
//        // 查询缓存人员信息
//        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(personInfos.copy());


        String personObjectCache = this.getPageCache().get("personObjectCache");
        DynamicObjectCollection personObjects = SerializationUtils.deSerializeFromBase64(personObjectCache);

        String haos_adminorghrs = this.getPageCache().get("haos_adminorghrs");
        DynamicObjectCollection haos_adminorghr = SerializationUtils.deSerializeFromBase64(haos_adminorghrs);

        String finOrgIds = this.getPageCache().get("finOrgIds");
        List<Long> finalOrgIds = new ArrayList<>();
        if (StringUtil.isEmpty(finOrgIds)) {
            List<Long> orgIds = haos_adminorghr.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
            if (orgIds.size() == 0) {
                orgIds = Collections.singletonList((Long) tdkwOrg.getPkValue());
            }
            finalOrgIds = orgIds;
        } else {
            finalOrgIds = SerializationUtils.deSerializeFromBase64(finOrgIds);
        }


        List<String> xNames = new ArrayList<>();
        List<Number> yDatas = new ArrayList<>();


        //获取缓存orgPersonMaps
        String orgPersonMaps = this.getPageCache().get("orgPersonMaps");
        Map<Long, List<Long>> orgForPersons = new HashMap<>();
        if (!StringUtil.isEmpty(orgPersonMaps)) {
            orgForPersons = SerializationUtils.deSerializeFromBase64(orgPersonMaps);
        }

        //获取缓存orgsName
        String orgsName = this.getPageCache().get("orgsName");
        Map<Long, DynamicObject> orgNameMap = new HashMap<>();
        if (!StringUtil.isEmpty(orgsName)) {
            orgNameMap = SerializationUtils.deSerializeFromBase64(orgsName);
        }
        String dirOrgPersonAllstr = this.getPageCache().get("dirOrgPersonAllstr");
        List<Long> dirOrgPersonAll = new ArrayList<>();

        BigDecimal progressbarSumAll = BigDecimal.ZERO;
        for (Long finalOrgId : finalOrgIds) {
            List<Long> persons = new ArrayList<>();
            if (StringUtil.isEmpty(orgPersonMaps)) {
                persons = PersonInfoUtil.getAllPersonByOrg(finalOrgId, tdkwEnddate);
            } else {
                persons = orgForPersons.get(finalOrgId);
            }
//           persons = PersonInfoUtil.getAllPersonByOrg(finalOrgId, tdkwEnddate);
            if (null == persons || persons.size() == 0) {
                continue;
            }
            List<Long> finalPersons = persons;
            List<Long> dirOrgPersons = personObjects.stream().filter(i -> (Long) i.get("tdkw_company") != 0).filter(i -> finalPersons.contains(i.getLong("person"))).map(i -> i.getLong("person")).collect(Collectors.toList());
            if (dirOrgPersons.size() == 0) {
                continue;
            }
            // 简历完整度合计
            BigDecimal progressbarSum = personObjects.stream().filter(i -> (Long) i.get("tdkw_company") != 0).filter(i -> finalPersons.contains(i.getLong("person"))).map(i -> i.getBigDecimal("tdkw_progressbar")).reduce(BigDecimal.ZERO, BigDecimal::add);


//            QFilter orgFilter = new QFilter("id", QCP.equals, finalOrgId);
//            DynamicObject orgObject = QueryServiceHelper.queryOne("haos_adminorghr", "id,name", orgFilter.toArray());
            DynamicObject orgObject = new DynamicObject();
            if (StringUtil.isEmpty(orgsName)) {
                QFilter orgFilter = new QFilter("id", QCP.equals, finalOrgId);
                orgObject = QueryServiceHelper.queryOne("haos_adminorghr", "id,name", orgFilter.toArray());
            } else {
                orgObject = orgNameMap.get(finalOrgId);
            }
            xNames.add(orgObject.getString("name"));

            progressbarSumAll = progressbarSumAll.add(progressbarSum);
            dirOrgPersonAll.addAll(dirOrgPersons);


            BigDecimal divide = progressbarSum.divide(new BigDecimal(dirOrgPersons.size()), 6, BigDecimal.ROUND_HALF_UP);
            divide = divide.multiply(new BigDecimal(100));
            divide = divide.setScale(2, BigDecimal.ROUND_HALF_UP);
            yDatas.add(divide.doubleValue());

            this.getPageCache().put(this.getClass().getName() + orgObject.getString("name"), dirOrgPersons.toString());
        }


        // 直属组织下人数
        List<Long> dirOrgPersons = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue())).map(i -> i.getLong("person")).collect(Collectors.toList());
        if (haos_adminorghr.size() != 0 && dirOrgPersons.size() != 0) {
            // 简历完整度合计
            BigDecimal progressbarSum = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue())).map(i -> i.getBigDecimal("tdkw_progressbar")).reduce(BigDecimal.ZERO, BigDecimal::add);
            progressbarSumAll = progressbarSumAll.add(progressbarSum);
//            QFilter orgFilter = new QFilter("id", QCP.equals, tdkwOrg.getPkValue());
//            DynamicObject dirOrg = QueryServiceHelper.queryOne("haos_adminorghr", "id,name", orgFilter.toArray());
//            String dirOrgPreson = this.getPageCache().get("dirOrgPreson");
//            this.getPageCache().remove("dirOrgPreson");
//            DynamicObject dirOrg = SerializationUtils.deSerializeFromBase64(dirOrgPreson);
//            xNames.add(dirOrg.getString("name") + "(直属)");
            dirOrgPersonAll.addAll(dirOrgPersons);
//            BigDecimal divide = progressbarSum.divide(new BigDecimal(dirOrgPersons.size()), 6, BigDecimal.ROUND_HALF_UP);
//            divide = divide.multiply(new BigDecimal(100));
//            divide = divide.setScale(2, BigDecimal.ROUND_HALF_UP);
//            yDatas.add(divide.doubleValue());
//            this.getPageCache().put(this.getClass().getName() + dirOrg.getString("name") + "(直属)", dirOrgPersons.toString());
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
//        System.out.println("简历信息完整度耗时" + (System.currentTimeMillis() - start));

    }
}
