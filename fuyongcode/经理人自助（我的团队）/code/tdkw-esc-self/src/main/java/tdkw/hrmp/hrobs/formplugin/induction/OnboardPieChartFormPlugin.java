package tdkw.hrmp.hrobs.formplugin.induction;

import com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.chart.ItemValue;
import kd.bos.form.chart.PieChart;
import kd.bos.form.chart.PieSeries;
import kd.bos.form.chart.Position;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ChartClickEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import tdkw.hrmp.hrobs.common.hrobs.emputils.GetEmpFilterUtil;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class OnboardPieChartFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {
    private static final Log logger = LogFactory.getLog(OnboardPieChartFormPlugin.class);

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        DynamicObjectCollection employTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
        for (DynamicObject tempId : load) {
            employTypeForDisplay.add(tempId);
        }
        // TODO 报错异常
//        model.setValue("tdkw_employtype", employTypeForDisplay);
        PieChart pieChart = this.getControl("tdkw_piechartap");
        // 从缓存中拿权限
        String isHasAllOrgPerm = this.getPageCache().get("isHasAllOrgPerm");
        if (StringUtils.isBlank(isHasAllOrgPerm)) {
            putOrgCache();
        }
        String tdkwOrg = this.getPageCache().get("tdkw_org");
        // 全集团用户默认XXX集团
        this.getModel().setValue("tdkw_org", Long.valueOf(tdkwOrg));
        this.getView().updateView("tdkw_org");
        List<Long> finalPersonIds = getOrgAndFilterPersonIds();
        this.getPageCache().put("finalPersonIds", SerializationUtils.toJsonString(finalPersonIds));
        this.drawChartLevel(pieChart, finalPersonIds);
    }

    private void putOrgCache() {
        Long org;
        // 获取权限放入缓存
        AuthorizedOrgResult orgScope = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), this.getModel().getDataEntityType().getName(), "tdkw_org");
        // 有全部组织权限
        if (orgScope.isHasAllOrgPerm()) {
            this.getPageCache().put("isHasAllOrgPerm", "true");
            org = 100000L;
            this.getPageCache().put("tdkw_org", String.valueOf(org));
        } else {
            this.getPageCache().put("isHasAllOrgPerm", "false");
            List<Long> hasPermOrgs = orgScope.getHasPermOrgs();
            this.getPageCache().put("orgScope", SerializationUtils.toJsonString(hasPermOrgs));
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("haos_adminorghr", "id", new QFilter[]{new QFilter("orgtype.number", QCP.in, new String[]{"XY00001", "XY00002", "XY00003", "XY00004", "XY00005"})
                    .and("id", QCP.in, hasPermOrgs).and("datastatus", QCP.equals, "1").and("enable", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1")}, "sortcode");
            if (null != dynamicObjects && dynamicObjects.length > 0) {
                org = dynamicObjects[0].getLong("id");
                this.getPageCache().put("tdkw_org", String.valueOf(org));
            }
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        if ("tdkw_piecharttype".equals(name)) {
            // 0是岗位层级  1是性别   2学历   4年龄
            List<Long> finalPersonIds = getOrgAndFilterPersonIds();
            this.getPageCache().put("finalPersonIds", SerializationUtils.toJsonString(finalPersonIds));
            String tdkwPiecharttype = (String) this.getModel().getValue("tdkw_piecharttype");
            // 岗位层级
            if (StringUtils.equals("0", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartLevel(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
            // 性别
            if (StringUtils.equals("1", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartGender(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
            // 学历
            if (StringUtils.equals("2", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartEdu(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
            // 年龄
            if (StringUtils.equals("3", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartAge(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
        }
    }

    /**
     * 获取流入流出表的过滤器
     *
     * @param pair
     * @return
     */
    private QFilter getFlowFilter(Pair<Date, Date> pair) {
        QFilter qFilter = new QFilter("flowtime", QCP.large_equals, pair.getKey());
        qFilter.and("flowtime", QCP.less_equals, pair.getValue());
        // 变动类型-所属变动大类为 入职
//        qFilter.and("tdkw_changetype.number", QCP.equals, "XY00001");
        return qFilter;
    }

    private List<Long> getOrgAndFilterPersonIds() {
        // 先查出时间范围和过滤条件内的人员id
        Date beginDate = (Date) this.getModel().getValue("tdkw_begindate");
        Date endDate = (Date) this.getModel().getValue("tdkw_enddate");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        endDate = calendar.getTime();

        DynamicObject dataEntity = this.getModel().getDataEntity();

        // 组织
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return new ArrayList<>();
        }
        // 将表头组织和权限交集进行处理
        QFilter orgQfilter = handleOrg(tdkwOrg);

        // 学历
        DynamicObjectCollection tdkw_degree = dataEntity.getDynamicObjectCollection("tdkw_degree");
        // 岗位序列
        DynamicObjectCollection tdkw_job = dataEntity.getDynamicObjectCollection("tdkw_job");
        // 政治面貌
        DynamicObjectCollection tdkw_politicaloutlook = dataEntity.getDynamicObjectCollection("tdkw_politicaloutlook");
        // 性别
        DynamicObjectCollection tdkw_sex = dataEntity.getDynamicObjectCollection("tdkw_sex");
        // 人员类别
        DynamicObjectCollection tdkw_employtype = dataEntity.getDynamicObjectCollection("tdkw_employtype");
        // 职务
        DynamicObjectCollection tdkw_duties = dataEntity.getDynamicObjectCollection("tdkw_duties");
        // 岗位层级
        String tdkw_hierarchy = dataEntity.getString("tdkw_hierarchy");

        Pair<Date, Date> pair = new Pair<>(beginDate, endDate);
        // 获取入职的qFilter
        QFilter flowFilter = getFlowFilter(pair);
        // 先查流入流出表入职的人员
        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create("hpfs_personflow");
        DynamicObject[] flowPerson = hrBaseServiceHelper.query("person", new QFilter[]{flowFilter, orgQfilter});
        // 入职人员id
        List<Long> personIdList = Arrays.stream(flowPerson).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        logger.info("流入流出表查询结果：" + personIdList);

        QFilter personFiler = new QFilter("1", QCP.equals, 1);
        personFiler.and("person.id", QCP.in, personIdList);
        personFiler.and("businessstatus", QCP.equals, "1");
        personFiler.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // TODO 这里只查询全职任职的
        personFiler.and("filetype.postype.number", QCP.equals, "1010_S");
        personFiler.and("empposrel.datastatus", QCP.equals, "1");
        personFiler.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);

        if (ObjectUtils.isNotEmpty(tdkw_job) && tdkw_job.size() != 0) {
            List<Object> job = tdkw_job.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            personFiler.and("empposrel.tdkw_jobsequence", QCP.in, job);
        }
        if (ObjectUtils.isNotEmpty(tdkw_sex) && tdkw_sex.size() != 0) {
            List<Object> sex = tdkw_sex.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            personFiler.and("pernontsprop.gender", QCP.in, sex);
        }
        if (ObjectUtils.isNotEmpty(tdkw_employtype) && tdkw_employtype.size() != 0) {
            List<Object> employType = tdkw_employtype.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            personFiler.and("empentrel.laborreltype", QCP.in, employType);
        }
        if (ObjectUtils.isNotEmpty(tdkw_duties) && tdkw_duties.size() != 0) {
            List<Object> duties = tdkw_duties.stream().map(i -> i.getDynamicObject("fbasedataid").getPkValue()).collect(Collectors.toList());
            DynamicObjectCollection query = QueryServiceHelper.query("hbjm_jobgradehr", "entryboid", new QFilter[]{new QFilter("id", QCP.in, duties)});
            Collection<Long> entryboid = query.stream().map(i -> i.getLong("entryboid")).collect(Collectors.toList());
            personFiler.and("empposrel.tdkw_ranks.entryboid", QCP.in, entryboid);
        }
        if (ObjectUtils.isNotEmpty(tdkw_hierarchy)) {
            String[] hierarchy = tdkw_hierarchy.substring(1, tdkw_hierarchy.length() - 1).split(",");
            personFiler.and("empposrel.position.tdkw_positionlevel", QCP.in, hierarchy);
        }

        // 人事业务档案
        DynamicObjectCollection hspm_ermanfile = QueryServiceHelper.query("hspm_ermanfile", "person.id", new QFilter[]{personFiler});
        logger.info("人事业务档案查询结果：" + hspm_ermanfile);
        List<Long> erManFileIds = hspm_ermanfile.stream().map(i -> i.getLong("person.id")).collect(Collectors.toList());

        if (ObjectUtils.isNotEmpty(tdkw_degree) && tdkw_degree.size() != 0) {
            // 学历
            List<Object> degree = tdkw_degree.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter pereduexpFilter = new QFilter("person.id", QCP.in, erManFileIds);
            pereduexpFilter.and("education", QCP.in, degree);
//            pereduexpFilter.and("tdkw_ishighestcheck", QCP.equals, "1");

            // 教育经历
            hspm_ermanfile = QueryServiceHelper.query("hrpi_pereduexp", "person.id", pereduexpFilter.toArray());
            erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
        }

        if (ObjectUtils.isNotEmpty(tdkw_politicaloutlook) && tdkw_politicaloutlook.size() != 0) {
            // 政治面貌
            List<Object> politicaloutlook = tdkw_politicaloutlook.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter perregionFilter = new QFilter("person.id", QCP.in, erManFileIds);
            perregionFilter.and("politicalstatus", QCP.in, politicaloutlook);

            // 基本信息补充
            hspm_ermanfile = QueryServiceHelper.query("hrpi_perregion", "person.id", perregionFilter.toArray());
            erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
        }
        return erManFileIds;
    }

    private QFilter handleOrg(DynamicObject tdkwOrg) {
        QFilter orgFilter = new QFilter("1", QCP.equals, 1);
        String topOrgId = String.valueOf(tdkwOrg.getPkValue());
        String isHasAllOrgPerm = this.getPageCache().get("isHasAllOrgPerm");
        if (StringUtils.isBlank(isHasAllOrgPerm)) {
            putOrgCache();
        }
        isHasAllOrgPerm = this.getPageCache().get("isHasAllOrgPerm");
        if (StringUtils.equals(isHasAllOrgPerm, "true")) {
            if (!"100000".equals(topOrgId)) {
                // 权限为全部  表头为不为XXX集团
                // 获取表头下级组织
                List<Long> topOrgList = HRRoleAndPersonUtils.getHROrgIds(topOrgId);
                orgFilter.and("adminorg", QCP.in, topOrgList);
            }

        } else {
            // 权限和表头都不为全部
            String orgScope = this.getPageCache().get("orgScope");
            // 权限的组织集合
            List<Long> rightOrgList = (List<Long>) SerializationUtils.fromJsonStringToList(orgScope, Long.class);
            // 获取表头下级组织
            List<Long> topOrgList = HRRoleAndPersonUtils.getHROrgIds(topOrgId);
            topOrgList.retainAll(rightOrgList);
            orgFilter.and("adminorg", QCP.in, topOrgList);
        }
        logger.info("orgFilter.toString()" + orgFilter);
        return orgFilter;
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_piechartap");
        this.addClickListeners("tdkw_query");
        BasedataEdit org = this.getControl("tdkw_org");
        org.addBeforeF7SelectListener(this);
    }


    /**
     * 点击穿透
     *
     * @param evt
     */
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        if (StringUtils.equals("tdkw_piechartap", key)) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }
            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_onborad_report");
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            showParameter.setHasRight(true);
            showParameter.setCustomParam("personIds", this.getPageCache().get(e.getSeriesName() + name));
            this.getView().showForm(showParameter);
        } else if (StringUtils.equals("tdkw_query", key)) {
            List<Long> finalPersonIds = getOrgAndFilterPersonIds();
            this.getPageCache().put("finalPersonIds", SerializationUtils.toJsonString(finalPersonIds));
            String tdkwPiecharttype = (String) this.getModel().getValue("tdkw_piecharttype");
            // 岗位层级
            if (StringUtils.equals("0", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartLevel(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
            // 性别
            if (StringUtils.equals("1", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartGender(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
            // 学历
            if (StringUtils.equals("2", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartEdu(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
            // 年龄
            if (StringUtils.equals("3", tdkwPiecharttype)) {
                PieChart pieChart = this.getControl("tdkw_piechartap");
                this.drawChartAge(pieChart, finalPersonIds);
                this.getView().updateView("tdkw_piechartap");
            }
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

    /**
     * 年龄面饼
     *
     * @param pieChart
     * @param finalPersonIds
     */
    public void drawChartAge(PieChart pieChart, List<Long> finalPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        IPageCache pageCache = this.getPageCache();
        // 设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        // 添加数据
        PieSeries series = pieChart.createPieSeries("年龄");
        //===================数据取值==============================
        // 查出年龄
        // 人员非时序性属性
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        pernontsprQfilter.and("iscurrentversion", QCP.equals, "1");
        pernontsprQfilter.and("datastatus", QCP.equals, "1");
        pernontsprQfilter.and("person.id", QCP.in, finalPersonIds);
        DynamicObject[] personObjects = BusinessDataServiceHelper.load("hrpi_pernontsprop", "age,person.id", new QFilter[]{pernontsprQfilter});

        // 25及以下
        long count1 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") != 0)
                .filter(i -> (int) i.get("age") <= 25).count();
        // 26-30
        long count2 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") >= 26 && (int) i.get("age") <= 30).count();
        // 31-35
        long count3 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") >= 31 && (int) i.get("age") <= 35).count();
        // 36-40
        long count4 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") >= 36 && (int) i.get("age") <= 40).count();
        // 41-45
        long count5 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") >= 41 && (int) i.get("age") <= 45).count();
        // 46-50
        long count6 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") >= 46 && (int) i.get("age") <= 50).count();
        // 51-55
        long count7 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") >= 51 && (int) i.get("age") <= 55).count();
        // 55以上
        long count8 = Arrays.stream(personObjects)
                .filter(i -> (int) i.get("age") > 55).count();
        // key为展示字段  value为统计分组数量
        Map<String, Long> ageMap = new ListOrderedMap<>();
        if (count1 != 0) {
            ageMap.put("25及以下", count1);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(j -> (int) j.get("age") != 0).filter(j -> (int) j.get("age") <= 25)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "25及以下", collect1.toString());
        }
        if (count2 != 0) {
            ageMap.put("26-30", count2);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(i -> (int) i.get("age") >= 26 && (int) i.get("age") <= 30)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "26-30", collect1.toString());
        }
        if (count3 != 0) {
            ageMap.put("31-35", count3);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(i -> (int) i.get("age") >= 31 && (int) i.get("age") <= 35)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "31-35", collect1.toString());
        }
        if (count4 != 0) {
            ageMap.put("36-40", count4);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(i -> (int) i.get("age") >= 36 && (int) i.get("age") <= 40)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "36-40", collect1.toString());
        }
        if (count5 != 0) {
            ageMap.put("41-45", count5);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(i -> (int) i.get("age") >= 41 && (int) i.get("age") <= 45)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "41-45", collect1.toString());
        }
        if (count6 != 0) {
            ageMap.put("46-50", count6);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(i -> (int) i.get("age") >= 46 && (int) i.get("age") <= 50)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "46-50", collect1.toString());
        }
        if (count7 != 0) {
            ageMap.put("51-55", count7);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(i -> (int) i.get("age") >= 51 && (int) i.get("age") <= 55)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "51-55", collect1.toString());
        }
        if (count8 != 0) {
            ageMap.put("55以上", count8);
            List<Long> collect1 = Arrays.stream(personObjects)
                    .filter(i -> (int) i.get("age") > 55)
                    .map(j -> j.getLong("person.id")).collect(Collectors.toList());
            pageCache.put("年龄" + "55以上", collect1.toString());
        }
        ItemValue[] itemValues = new ItemValue[ageMap.size()];
        Set<String> strings = ageMap.keySet();
        int i = 0;
        for (String key : strings) {
            itemValues[i] = new ItemValue(key, ageMap.get(key));
            i++;
        }
        series.setData(itemValues);
        Map<String, Object> labelMap = new HashMap<>();
        Map<String, Object> normalMap = new HashMap<>();
        normalMap.put("show", true);
        // 参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
        String formatterBuilder = "{d}%" +
                "({c}" +
                "人)";
        normalMap.put("formatter", formatterBuilder);
        labelMap.put("normal", normalMap);
        series.setPropValue("label", labelMap);

        // series样式
        series.setRadius("70", "100");
    }

    /**
     * 学历面饼
     *
     * @param pieChart
     */
    public void drawChartEdu(PieChart pieChart, List<Long> finalPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        // 设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        // 添加数据
        PieSeries series = pieChart.createPieSeries("学历");

        //===================数据取值==============================
        // 教育经历
        QFilter eduQfilter = new QFilter("person.id", QCP.in, finalPersonIds);
        eduQfilter.and("iscurrentversion", QCP.equals, "1");
        eduQfilter.and("datastatus", QCP.equals, "1");
//        eduQfilter.and("tdkw_ishighestcheck", QCP.equals, "1");
        DynamicObject[] personObjects = BusinessDataServiceHelper.load("hrpi_pereduexp", "education.id,person.id",
                new QFilter[]{eduQfilter});
        Map<Object, Long> eduMap = Arrays.stream(personObjects)
                .filter(i -> i.getLong("education.id") != 0).collect(Collectors.groupingBy(i -> i.get("education.id"), Collectors.counting()));
        Set<Object> objects = eduMap.keySet();
        ItemValue[] itemValues = new ItemValue[eduMap.size()];
        int i = 0;
        for (Object key : objects) {
            QFilter sexFilter = new QFilter("id", QCP.equals, key);
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_diploma", "id,name", sexFilter.toArray());
            itemValues[i] = new ItemValue(dynamicObject.getString("name"), eduMap.get(key));

            List<Long> cache = Arrays.stream(personObjects).filter(o -> o.get("education.id").equals(dynamicObject.get("id"))).map(o -> o.getLong("person.id")).collect(Collectors.toList());
            this.getPageCache().put("学历" + dynamicObject.getString("name"), cache.toString());
            i++;
        }

        series.setData(itemValues);
        Map<String, Object> labelMap = new HashMap<>();
        Map<String, Object> normalMap = new HashMap<>();
        normalMap.put("show", true);
        // 参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
        String formatterBuilder = "{d}%" +
                "({c}" +
                "人)";
        normalMap.put("formatter", formatterBuilder);
        labelMap.put("normal", normalMap);
        series.setPropValue("label", labelMap);
        // series样式
        series.setRadius("70", "100");
    }

    /**
     * 性别面饼
     *
     * @param pieChart
     * @param finalPersonIds
     */
    public void drawChartGender(PieChart pieChart, List<Long> finalPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        // 设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        // 添加数据
        PieSeries series = pieChart.createPieSeries("性别");
        //===================数据取值==============================
        // 查出性别
        // 人员非时序性属性
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        pernontsprQfilter.and("iscurrentversion", QCP.equals, "1");
        pernontsprQfilter.and("datastatus", QCP.equals, "1");
        pernontsprQfilter.and("person.id", QCP.in, finalPersonIds);
        DynamicObject[] personObjects = BusinessDataServiceHelper.load("hrpi_pernontsprop", "gender.id,person.id", new QFilter[]{pernontsprQfilter});
        Map<Object, Long> genderMap = Arrays.stream(personObjects).filter(o -> o.getLong("gender.id") != 0L).collect(Collectors.groupingBy(o -> o.get("gender.id"), Collectors.counting()));

        Set<Object> keySet = genderMap.keySet();
        ItemValue[] itemValues = new ItemValue[keySet.size()];
        int i = 0;
        for (Object key : keySet) {
            QFilter genderFilter = new QFilter("id", QCP.equals, key);
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_sex", "id,name", genderFilter.toArray());
            itemValues[i] = new ItemValue(dynamicObject.getString("name"), genderMap.get(key));
            List<Long> cache = Arrays.stream(personObjects).filter(o -> o.get("gender.id").equals(dynamicObject.get("id"))).map(o -> o.getLong("person.id")).collect(Collectors.toList());
            this.getPageCache().put("性别" + dynamicObject.getString("name"), cache.toString());
            i++;
        }

        series.setData(itemValues);
        Map<String, Object> labelMap = new HashMap<>();
        Map<String, Object> normalMap = new HashMap<>();
        normalMap.put("show", true);
        // 参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
        String formatterBuilder = "{d}%" +
                "({c}" +
                "人)";
        normalMap.put("formatter", formatterBuilder);
        labelMap.put("normal", normalMap);
        series.setPropValue("label", labelMap);
        // series样式
        series.setRadius("70", "100");
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        if ("tdkw_org".equals(event.getProperty().getName())) {
            // 所属组织
            QFilter qFilter = new QFilter("orgtype.number", QCP.in, new String[]{"XY00001", "XY00002", "XY00003", "XY00004", "XY00005"});
            ListShowParameter showParameter = (ListShowParameter) event.getFormShowParameter();
            showParameter.getListFilterParameter().getQFilters().add(qFilter);
        }
    }


    /**
     * 岗位层级面饼（默认显示）
     *
     * @param pieChart
     * @param finalPersonIds
     */
    public void drawChartLevel(PieChart pieChart, List<Long> finalPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        // 设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");

        // 添加数据
        PieSeries series = pieChart.createPieSeries("岗位层级");
        //===================数据取值==============================//
        // 查出岗位层级
        QFilter personFiler = new QFilter("person.id", QCP.in, finalPersonIds);
        QFilter erManFile = new QFilter("businessstatus", QCP.equals, "1");
        erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFile.and("empposrel.datastatus", QCP.equals, "1");
        erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        DataSet postLevelObject = QueryServiceHelper.queryDataSet(this.getClass().getName(), "hspm_ermanfile", "person", new QFilter[]{personFiler, erManFile}, null);
        DynamicObjectCollection postLevelObjects = ORM.create().toPlainDynamicObjectCollection(postLevelObject);
//        Map<Object, Long> postLevel = postLevelObjects.stream().filter(o -> ObjectUtils.isNotEmpty(o.get("empposrel.tdkw_postlevel"))).collect(Collectors.groupingBy(o -> o.get("empposrel.tdkw_postlevel"), Collectors.counting()));
//        Map<Object, Long> postLevel = Maps.newHashMapWithExpectedSize(HRBaseConstants.INITCAPACITY_HASHMAP);
//        Set<Object> objects = postLevel.keySet();
//        ItemValue[] itemValues = new ItemValue[objects.size()];
        ItemValue[] itemValues = new ItemValue[5];
        int i = 0;
        /*for (Object key : objects) {
            String keyName = key.toString();
            if (StringUtils.equals(keyName, "1")) {
                keyName = "集团高管-集团直管";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("1", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "2")) {
                keyName = "集团高管-授权行业";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("2", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "3")) {
                keyName = "其他高管";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("3", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "4")) {
                keyName = "中层";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("4", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "5")) {
                keyName = "基层";
                List<Long> collect1 = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("5", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, collect1.toString());
            }

            itemValues[i] = new ItemValue(keyName, postLevel.get(key));
            i++;
        }*/
        for (int index = 0; index < 5; index++) {
            String keyName = String.valueOf(index + 1);
            if (StringUtils.equals(keyName, "1")) {
                keyName = "集团高管-集团直管";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("1", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "2")) {
                keyName = "集团高管-授权行业";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("2", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "3")) {
                keyName = "其他高管";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("3", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "4")) {
                keyName = "中层";
                List<Long> cache = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("4", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "5")) {
                keyName = "基层";
                List<Long> collect1 = postLevelObjects.stream()
//                        .filter(j -> StringUtils.equals("5", j.getString("empposrel.tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, collect1.toString());
            }

            itemValues[index] = new ItemValue(keyName, (postLevelObjects.size() / 5) + index);
        }
        series.setData(itemValues);
        Map<String, Object> labelMap = new HashMap<>();
        Map<String, Object> normalMap = new HashMap<>();
        normalMap.put("show", true);
        // 参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
        String formatterBuilder = "{d}%" +
                "({c}" +
                "人)";
        normalMap.put("formatter", formatterBuilder);
        labelMap.put("normal", normalMap);
        series.setPropValue("label", labelMap);
        // series样式
        series.setRadius("70", "100");
    }

    private List<Long> AllIdData(Date beginDate, Date endDate) {
        ArrayList<Long> totalId = new ArrayList<>();

        QFilter totalFilter = GetEmpFilterUtil.getTotalFilter(beginDate, endDate, null);
        DynamicObject[] totalPerson = BusinessDataServiceHelper.load("hrpi_empentrel", "person", totalFilter.toArray());
        for (DynamicObject person : totalPerson) {
            totalId.add(person.getLong("person.id"));
        }
        return totalId;
    }


    private List<BigDecimal> contructValueData(List<Pair<Date, Date>> monthList) {
        //==========================================================
        int totalNum = 0;
        List<BigDecimal> valueData = new ArrayList<>();
        DynamicObject dataEntity = this.getModel().getDataEntity();
        // 组织
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return valueData;
        }
        List<Long> orgId = Collections.singletonList((Long) tdkwOrg.getPkValue());
        // 学历
        DynamicObjectCollection tdkw_degree = dataEntity.getDynamicObjectCollection("tdkw_degree");
        // 岗位序列
        DynamicObjectCollection tdkw_job = dataEntity.getDynamicObjectCollection("tdkw_job");
        // 政治面貌
        DynamicObjectCollection tdkw_politicaloutlook = dataEntity.getDynamicObjectCollection("tdkw_politicaloutlook");
        // 性别
        DynamicObjectCollection tdkw_sex = dataEntity.getDynamicObjectCollection("tdkw_sex");
        // 人员类别
        DynamicObjectCollection tdkw_employtype = dataEntity.getDynamicObjectCollection("tdkw_employtype");
        // 职务
        DynamicObjectCollection tdkw_duties = dataEntity.getDynamicObjectCollection("tdkw_duties");
        // 岗位层级
        String tdkw_hierarchy = dataEntity.getString("tdkw_hierarchy");


        //==========================================================
        for (Pair<Date, Date> dateDatePair : monthList) {
            ArrayList<Long> totalId = new ArrayList<>();
            Date date = dateDatePair.getSecond();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, -1);
            Date starTime = calendar.getTime();
            QFilter totalFilter = GetEmpFilterUtil.getTotalFilter(starTime, date, null);
            DynamicObject[] totalPerson = BusinessDataServiceHelper.load("hrpi_empentrel", "person", totalFilter.toArray());

            for (DynamicObject person : totalPerson) {
                totalId.add(person.getLong("person.id"));
            }

            QFilter personFiler = new QFilter("person.id", QCP.in, totalId);
            personFiler.and("businessstatus", QCP.equals, "1");
            personFiler.and("iscurrentversion", QCP.equals, Boolean.TRUE);
            personFiler.and("filetype.postype.number", QCP.equals, "1010_S");
            personFiler.and("empposrel.businessstatus", QCP.equals, "1");
            personFiler.and("empposrel.datastatus", QCP.equals, "1");
            personFiler.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);

            if (ObjectUtils.isNotEmpty(tdkw_job) && tdkw_job.size() != 0) {
                List<Object> job = tdkw_job.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
                personFiler.and("empposrel.tdkw_jobsequence", QCP.in, job);
            }
            if (ObjectUtils.isNotEmpty(tdkw_sex) && tdkw_sex.size() != 0) {
                List<Object> sex = tdkw_sex.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
                personFiler.and("pernontsprop.gender", QCP.in, sex);
            }
            if (ObjectUtils.isNotEmpty(tdkw_employtype) && tdkw_employtype.size() != 0) {
                List<Object> employType = tdkw_employtype.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
                personFiler.and("empentrel.laborreltype", QCP.in, employType);
            }
            if (ObjectUtils.isNotEmpty(tdkw_duties) && tdkw_duties.size() != 0) {
                List<Object> duties = tdkw_duties.stream().map(i -> i.getDynamicObject("fbasedataid").getPkValue()).collect(Collectors.toList());
                DynamicObjectCollection query = QueryServiceHelper.query("hbjm_jobgradehr", "entryboid", new QFilter[]{new QFilter("id", QCP.in, duties)});
                Collection<Long> entryboid = query.stream().map(i -> i.getLong("entryboid")).collect(Collectors.toList());
                personFiler.and("empposrel.tdkw_ranks.entryboid", QCP.in, entryboid);
            }
            if (ObjectUtils.isNotEmpty(tdkw_hierarchy)) {
                String[] hierarchy = tdkw_hierarchy.substring(1, tdkw_hierarchy.length() - 1).split(",");
                personFiler.and("empposrel.position.tdkw_positionlevel", QCP.in, hierarchy);
            }

            // 人事业务档案
            DynamicObjectCollection hspm_ermanfile = QueryServiceHelper.query("hspm_ermanfile", "person.id", personFiler.toArray());
            List<Long> erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());

            if (ObjectUtils.isNotEmpty(tdkw_degree) && tdkw_degree.size() != 0) {
                // 学历
                List<Object> degree = tdkw_degree.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
                QFilter pereduexpFilter = new QFilter("person.id", QCP.in, erManFileIds);
                pereduexpFilter.and("education", QCP.in, degree);
//                pereduexpFilter.and("tdkw_ishighestcheck", QCP.equals, "1");

                // 教育经历
                hspm_ermanfile = QueryServiceHelper.query("hrpi_pereduexp", "person.id", pereduexpFilter.toArray());
                erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
            }

            if (ObjectUtils.isNotEmpty(tdkw_politicaloutlook) && tdkw_politicaloutlook.size() != 0) {
                // 政治面貌
                List<Object> politicaloutlook = tdkw_politicaloutlook.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
                QFilter perregionFilter = new QFilter("person.id", QCP.in, erManFileIds);
                perregionFilter.and("politicalstatus", QCP.in, politicaloutlook);

                // 基本信息补充
                hspm_ermanfile = QueryServiceHelper.query("hrpi_perregion", "person.id", perregionFilter.toArray());
                erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
            }

            IPageCache pageCache = this.getPageCache();
            pageCache.put(DateTimeUtils.dateFormat(date, "yyyy.MM"), erManFileIds.toString());
            valueData.add(BigDecimal.valueOf(erManFileIds.size()));
        }
        return valueData;
    }
}