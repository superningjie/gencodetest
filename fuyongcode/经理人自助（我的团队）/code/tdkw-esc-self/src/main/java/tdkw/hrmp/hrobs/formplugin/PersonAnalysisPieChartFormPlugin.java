package tdkw.hrmp.hrobs.formplugin;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.chart.ItemValue;
import kd.bos.form.chart.PieChart;
import kd.bos.form.chart.PieSeries;
import kd.bos.form.chart.Position;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ChartClickEvent;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.QueryServiceHelper;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/6/20 11:49
 * @Description 人员分析 人员结构分布 饼图表单插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personanalysis
 * @Version 1.0
 **/
public class PersonAnalysisPieChartFormPlugin extends AbstractFormPlugin implements ClickListener {
    private Log logger = LogFactory.getLog(PersonAnalysisPieChartFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_piechartap");
        this.addClickListeners("tdkw_querybtn");
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        PieChart pieChart = this.getControl("tdkw_piechartap");
        DynamicObject dataEntity = this.getModel().getDataEntity();
        String tdkwPiecharttype = dataEntity.getString("tdkw_piecharttype");

        this.drawChart(pieChart, tdkwPiecharttype);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        PieChart pieChart = this.getControl("tdkw_piechartap");
        switch (name) {
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
            case "tdkw_piecharttype":
                String tdkwPiecharttype = (String) this.getModel().getValue("tdkw_piecharttype");
                this.drawChart(pieChart, tdkwPiecharttype);
                this.getView().updateView("tdkw_piechartap");
                break;
            default:
                break;
        }
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        if (key.equals("tdkw_piechartap")) {
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
            PieChart pieChart = this.getControl("tdkw_piechartap");
            String tdkwPiecharttype = dataEntity.getString("tdkw_piecharttype");
            this.drawChart(pieChart, tdkwPiecharttype);
            this.getView().updateView("tdkw_piechartap");
        }
    }

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

}
