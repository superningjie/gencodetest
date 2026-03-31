package tdkw.hrmp.hrobs.formplugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.context.RequestContext;
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
import kd.hr.haos.business.servicehelper.AdminOrgQueryServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import tdkw.hrmp.hrobs.formplugin.emputils.ECharsDateTimeUtil;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName DepartPersonDistFormPlugin
 * @author xxx
 * @Date 2023/7/12 9:36
 */
public class DepartPersonDistFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {
    private static final Log logger = LogFactory.getLog(DepartPersonDistFormPlugin.class);

    /**
     * f7过滤
     *
     * @param event BeforeF7SelectEvent事件类
     */
    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        if ("tdkw_org".equals(event.getProperty().getName())) {
            // 所属组织
//            QFilter qFilter = new QFilter("orgtype.number", QCP.in, new String[]{"XY00001", "XY00002", "XY00003", "XY00004", "XY00005"});
//            ListShowParameter showParameter = (ListShowParameter) event.getFormShowParameter();
//            showParameter.getListFilterParameter().getQFilters().add(qFilter);

            // 所属组织
            // 2025-10-31 增加报表过滤，当前人组织
            //获取当前登录人员所属组织
            List<Long> userIds = new ArrayList<>(1);
            userIds.add(UserServiceHelper.getCurrentUserId());
            // 获取当前登录人员所属公司
            String adminOrg = String.valueOf(UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId()));
            QFilter qFilter = new QFilter("orgtype.number", QCP.in, new String[]{adminOrg});
            ListShowParameter showParameter = (ListShowParameter) event.getFormShowParameter();
            showParameter.getListFilterParameter().getQFilters().add(qFilter);

        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        // 加载离职原因
        PieChart pieChart = this.getControl("tdkw_piechartap");
        this.drawChartReason(pieChart, getOrgAndFilterPersonIds());
        this.getView().updateView("tdkw_piechartap");
    }

    /**
     * 值更新事件
     *
     * @param e PropertyChangedArgs事件类
     */
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        if ("tdkw_piecharttype".equals(name)) {
            getInfoAll();
        }
    }

    private void getInfoAll() {
        String personsListStr = this.getPageCache().get("personsList");
        List<Long> orgAndFilterPersonIds = new ArrayList<>();
        if (!personsListStr.isEmpty() || !"".equals(personsListStr)) {
            //有缓存读缓存
            orgAndFilterPersonIds = SerializationUtils.deSerializeFromBase64(personsListStr);
        } else {
            orgAndFilterPersonIds = getOrgAndFilterPersonIds();
        }

        String tdkwPiecharttype = (String) this.getModel().getValue("tdkw_piecharttype");
        //离职原因Top10
        //0是岗位层级  1是性别   2学历   4年龄
        if (StringUtils.equals("0", tdkwPiecharttype)) {
            PieChart pieChart = this.getControl("tdkw_piechartap");
            this.drawChartReason(pieChart, orgAndFilterPersonIds);
            this.getView().updateView("tdkw_piechartap");
        }
        //离职类型
        if (StringUtils.equals("1", tdkwPiecharttype)) {
            PieChart pieChart = this.getControl("tdkw_piechartap");
            this.drawChartDepartType(pieChart, orgAndFilterPersonIds);
            this.getView().updateView("tdkw_piechartap");
        }
        //岗位层级
        if (StringUtils.equals("2", tdkwPiecharttype)) {
            PieChart pieChart = this.getControl("tdkw_piechartap");
            this.drawChartLevel(pieChart, orgAndFilterPersonIds);
            this.getView().updateView("tdkw_piechartap");
        }
        // 人员类型
        if (StringUtils.equals("3", tdkwPiecharttype)) {
            PieChart pieChart = this.getControl("tdkw_piechartap");
            this.drawChartPersonType(pieChart, orgAndFilterPersonIds);
            this.getView().updateView("tdkw_piechartap");
        }
        //集团司龄
        if (StringUtils.equals("4", tdkwPiecharttype)) {
            PieChart pieChart = this.getControl("tdkw_piechartap");
            this.drawChartComSerCount(pieChart, orgAndFilterPersonIds);
            this.getView().updateView("tdkw_piechartap");
        }
        //年龄
        if (StringUtils.equals("5", tdkwPiecharttype)) {
            PieChart pieChart = this.getControl("tdkw_piechartap");
            this.drawChartAge(pieChart, orgAndFilterPersonIds);
            this.getView().updateView("tdkw_piechartap");
        }
    }

    /**
     * 获取人员ids
     *
     * @return 人员ids
     */
    private List<Long> getOrgAndFilterPersonIds() {
        // 先查出时间范围和过滤条件内的人员id
        Date beginDate = (Date) this.getModel().getValue("tdkw_begindate");
        Date endDate = (Date) this.getModel().getValue("tdkw_enddate");
        List<Pair<Date, Date>> monthList = ECharsDateTimeUtil.getMonthList(beginDate, endDate);
        int totalNum = 0;
        List<Long> onBoardPersonId = new ArrayList<>();
        DynamicObject dataEntity = this.getModel().getDataEntity();
        // 组织
        DynamicObject tdkwOrg = dataEntity.getDynamicObject("tdkw_org");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return onBoardPersonId;
        }
        DynamicObject adminOrgHr = BusinessDataServiceHelper.loadSingle(tdkwOrg.getPkValue(), "haos_adminorghr");
        List<Long> orgId = Collections.singletonList((Long) adminOrgHr.get("company.id"));
        // 学历
        DynamicObjectCollection tdkw_degree = dataEntity.getDynamicObjectCollection("tdkw_education");
        // 岗位序列
        DynamicObjectCollection tdkw_job = dataEntity.getDynamicObjectCollection("tdkw_jobsequence");
        // 政治面貌
        DynamicObjectCollection tdkw_politicaloutlook = dataEntity.getDynamicObjectCollection("tdkw_politicalstatus");
        // 性别
        DynamicObjectCollection tdkw_sex = dataEntity.getDynamicObjectCollection("tdkw_gender");
        // 人员类别
        DynamicObjectCollection tdkw_employtype = dataEntity.getDynamicObjectCollection("tdkw_employtype");
        // 职务
        DynamicObjectCollection tdkw_duties = dataEntity.getDynamicObjectCollection("tdkw_job");
        // 岗位层级
        String tdkw_hierarchy = dataEntity.getString("tdkw_postlevel");
        //获取组织以及组织下属id
        List<Map<String, Object>> subOrgs = AdminOrgQueryServiceHelper.batchQueryAllSubOrg(orgId, endDate);
        List<Long> subOrgIds = subOrgs.stream().map(i -> (Long) i.get("orgId")).collect(Collectors.toList());
        //人员流入流出表--异动类型-所属变动大类=离职--人的id集合
        /*DynamicObject[] changeTypes = BusinessDataServiceHelper.load("tdkw_hbss_changetype", "tdkw_chgevent", new QFilter("tdkw_chgevent.name", QCP.equals, "离职").toArray());
        List<Long> idList = new ArrayList<>();
        for (DynamicObject changeType : changeTypes) {
            Long pkValue = changeType.getLong("id");
            idList.add(pkValue);
        }*/
        DynamicObject[] load = BusinessDataServiceHelper.load("hpfs_personflow", "person",
                new QFilter("chgcategory.number", QCP.in, Lists.newArrayList("101020_S", "101200_S")).
                and("flowtime", QCP.less_equals, endDate).and("flowtime", QCP.large_equals, beginDate).and("adminorg.company", QCP.in, subOrgIds)
                .and("depemp.isprimary", QCP.equals, "1").toArray());
        onBoardPersonId = Arrays.stream(load).map(o -> o.getLong("person.id")).collect(Collectors.toList());
        QFilter personFiler = new QFilter("person.id", QCP.in, onBoardPersonId);
        personFiler.and("businessstatus", QCP.equals, "1");
        personFiler.and("iscurrentversion", QCP.equals, Boolean.TRUE);
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
            personFiler.and("empposrel.tdkw_postlevel", QCP.in, hierarchy);
        }
        // 人事业务档案
        DynamicObjectCollection hspm_ermanfile = QueryServiceHelper.query("hspm_ermanfile", "person.id", personFiler.toArray());
        List<Long> erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
        Set<Object> uniquePersons = new HashSet<>();
        erManFileIds.removeIf(obj -> !uniquePersons.add(obj));
        if (ObjectUtils.isNotEmpty(tdkw_degree) && tdkw_degree.size() != 0) {
            // 学历
            List<Object> degree = tdkw_degree.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter pereduexpFilter = new QFilter("person.id", QCP.in, erManFileIds);
            pereduexpFilter.and("education", QCP.in, degree);
            pereduexpFilter.and("tdkw_ishighestcheck", QCP.equals, "1");
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

    /**
     * 监听事件
     *
     * @param e EventObject事件参数
     */
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_piechartap");
        this.addClickListeners("tdkw_query");
        this.addClickListeners("tdkw_resetting");
        BasedataEdit tdkw_org = this.getControl("tdkw_org");
        tdkw_org.addBeforeF7SelectListener(this);
    }

    /**
     * 点击穿透
     *
     * @param evt EventObject事件类
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
            showParameter.setFormId("tdkw_depart_report");
            showParameter.setHasRight(true);
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            showParameter.setCustomParam("personIdsDepart", this.getPageCache().get(e.getSeriesName() + name));
            this.getView().showForm(showParameter);
        } else if (StringUtils.equals("tdkw_query", key)) {
            getInfoAll();
        } else if (StringUtils.equals("tdkw_resetting", key)) {
            IDataModel model = this.getModel();
            model.setValue("tdkw_education", null);
            model.setValue("tdkw_jobsequence", null);
            model.setValue("tdkw_politicalstatus", null);
            model.setValue("tdkw_gender", null);
            model.setValue("tdkw_employtype", null);
            model.setValue("tdkw_job", null);
            model.setValue("tdkw_postlevel", null);
        }
    }

    /**
     * 年龄面板
     *
     * @param pieChart              PieChart类型参数
     * @param orgAndFilterPersonIds 人员ids
     */
    public void drawChartAge(PieChart pieChart, List<Long> orgAndFilterPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        IPageCache pageCache = this.getPageCache();
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        //添加数据
        PieSeries series = pieChart.createPieSeries("年龄");
        //===================数据取值==============================
        //查出年龄
        DataSet dataSet = PersonInfoUtil.queryPersonInfoDep(orgAndFilterPersonIds, this.getClass().getName());
        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(dataSet);
        //去除person重复项
        Set<Object> uniquePersons = new HashSet<>();
        personObjects.removeIf(obj -> !uniquePersons.add(obj.get("person")));

        // 25及以下
        long count1 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") != 0)
                .filter(i -> (int) i.get("tdkw_age") <= 25).count();
        // 26-30
        long count2 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30).count();
        // 31-35
        long count3 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35).count();
        // 36-40
        long count4 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40).count();
        // 41-45
        long count5 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45).count();
        // 46-50
        long count6 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50).count();
        // 51-55
        long count7 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55).count();
        // 55以上
        long count8 = personObjects.stream()
                .filter(i -> (int) i.get("tdkw_age") > 55).count();
        //key为展示字段  value为统计分组数量
        Map<String, Long> ageMap = new ListOrderedMap<>();
        if (count1 != 0) {
            ageMap.put("25及以下", count1);
            List<Long> collect1 = personObjects.stream()
                    .filter(j -> (int) j.get("tdkw_age") != 0).filter(j -> (int) j.get("tdkw_age") <= 25)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            pageCache.put("年龄" + "25及以下", collect1.toString());
        }
        if (count2 != 0) {
            ageMap.put("26-30", count2);
            List<Long> collect1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 26 && (int) i.get("tdkw_age") <= 30)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            pageCache.put("年龄" + "26-30", collect1.toString());
        }
        if (count3 != 0) {
            ageMap.put("31-35", count3);
            List<Long> collect1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 31 && (int) i.get("tdkw_age") <= 35)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            pageCache.put("年龄" + "31-35", collect1.toString());
        }
        if (count4 != 0) {
            ageMap.put("36-40", count4);
            List<Long> collect1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 36 && (int) i.get("tdkw_age") <= 40)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            pageCache.put("年龄" + "36-40", collect1.toString());
        }
        if (count5 != 0) {
            ageMap.put("41-45", count5);
            List<Long> collect1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 41 && (int) i.get("tdkw_age") <= 45)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            pageCache.put("年龄" + "41-45", collect1.toString());
        }
        if (count6 != 0) {
            ageMap.put("46-50", count6);
            List<Long> collect1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 46 && (int) i.get("tdkw_age") <= 50)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            pageCache.put("年龄" + "46-50", collect1.toString());
        }
        if (count7 != 0) {
            ageMap.put("51-55", count7);
            List<Long> collect1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") >= 51 && (int) i.get("tdkw_age") <= 55)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            pageCache.put("年龄" + "51-55", collect1.toString());
        }
        if (count8 != 0) {
            ageMap.put("55以上", count8);
            List<Long> collect1 = personObjects.stream()
                    .filter(i -> (int) i.get("tdkw_age") > 55)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
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
        //参考echarts的饼图设置，其中a、hr、b、per分别对应下面的aMap、hrMap、bMap、perMap
        StringBuilder formatterBuilder = new StringBuilder();
        formatterBuilder.append("{d}%");
        formatterBuilder.append("({c}");
        formatterBuilder.append("人)");
        normalMap.put("formatter", formatterBuilder.toString());
        labelMap.put("normal", normalMap);
        series.setPropValue("label", labelMap);

        //series样式
//        series.setRoseType(RoseType.radius);
        series.setRadius("70", "100");
    }

    /**
     * 人员类型面饼
     *
     * @param pieChart              PieChart类型参数
     * @param orgAndFilterPersonIds 人员ids
     */
    public void drawChartPersonType(PieChart pieChart, List<Long> orgAndFilterPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        //添加数据
        PieSeries series = pieChart.createPieSeries("人员类型");

        //===================数据取值==============================
        //查出人员类型
        DataSet dataSet = PersonInfoUtil.queryPersonInfoDep(orgAndFilterPersonIds, this.getClass().getName());
        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(dataSet);
        Map<Object, Long> eduMap = personObjects.stream()
                .filter(i -> (Long) i.get("tdkw_laborreltype") != 0).collect(Collectors.groupingBy(i -> i.get("tdkw_laborreltype"), Collectors.counting()));
        Set<Object> objects = eduMap.keySet();
        ItemValue[] itemValues = new ItemValue[eduMap.size()];
        int i = 0;
        for (Object key : objects) {
            QFilter sexFilter = new QFilter("id", QCP.equals, key);
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("hbss_laborreltype", "id,name", sexFilter.toArray());
            itemValues[i] = new ItemValue(dynamicObject.getString("name"), eduMap.get(key));

            List<Long> cache = personObjects.stream().filter(o -> o.get("tdkw_laborreltype").equals(dynamicObject.get("id"))).map(o -> o.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("人员类型" + dynamicObject.getString("name"), cache.toString());
            i++;
        }

        series.setData(itemValues);
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
        //series样式
        //        series.setRoseType(RoseType.radius);
        series.setRadius("70", "100");
    }

    /**
     * 集团司龄面饼
     *
     * @param pieChart              PieChart类型参数
     * @param orgAndFilterPersonIds 人员ids
     */
    public void drawChartComSerCount(PieChart pieChart, List<Long> orgAndFilterPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        //===================数据取值==============================
        //查出人员类型
        DataSet dataSet = PersonInfoUtil.queryPersonInfoDep(orgAndFilterPersonIds, this.getClass().getName());
        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(dataSet);
        //去除person重复项
        Set<Object> uniquePersons = new HashSet<>();
        personObjects.removeIf(obj -> !uniquePersons.add(obj.get("person")));
        // 集团司龄
        PieSeries series = pieChart.createPieSeries("集团司龄");
        /*// 1年以下
        Long count1 = personObjects.stream()
                .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0).collect(Collectors.counting());
        // 1-2年
        Long count2 = personObjects.stream()
                .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
                        && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(2)) <= 0).collect(Collectors.counting());
        // 3-5年
        Long count3 = personObjects.stream()
                .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(2)) > 0
                        && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(5)) <= 0).collect(Collectors.counting());
        // 6-10年
        Long count4 = personObjects.stream()
                .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(5)) > 0
                        && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0).collect(Collectors.counting());
        // 10年以上
        Long count5 = personObjects.stream()
                .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0).collect(Collectors.counting());*/
        // 1年以下
        Long count1 = 1L;
        // 1-2年
        Long count2 = 5L;
        // 3-5年
        Long count3 = 10L;
        // 6-10年
        Long count4 = 2L;
        // 10年以上
        Long count5 = 3L;
        Map<String, Long> tdkw_comsercount = new ListOrderedMap<>();
        if (count1 != 0) {
            tdkw_comsercount.put("1年以下", count1);
            List<Long> collect1 = personObjects.stream()
//                    .filter(j -> ((BigDecimal) j.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) < 0)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("集团司龄" + "1年以下", collect1.toString());
        }
        if (count2 != 0) {
            tdkw_comsercount.put("1-2年", count2);
            List<Long> collect1 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(1)) >= 0
//                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(2)) <= 0)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("集团司龄" + "1-2年", collect1.toString());
        }
        if (count3 != 0) {
            tdkw_comsercount.put("3-5年", count3);
            List<Long> collect1 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(2)) > 0
//                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(5)) <= 0)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("集团司龄" + "3-5年", collect1.toString());
        }
        if (count4 != 0) {
            tdkw_comsercount.put("6-10年", count4);
            List<Long> collect1 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(5)) > 0
//                            && ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) <= 0)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("集团司龄" + "6-10年", collect1.toString());
        }
        if (count5 != 0) {
            tdkw_comsercount.put("10年以上", count5);
            List<Long> collect1 = personObjects.stream()
//                    .filter(i -> ((BigDecimal) i.get("tdkw_comsercount")).compareTo(new BigDecimal(10)) > 0)
                    .map(j -> j.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("集团司龄" + "10年以上", collect1.toString());
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
        series.setRadius("70", "100");
    }

    /**
     * 离职类型面板
     *
     * @param pieChart              PieChart类型参顺
     * @param orgAndFilterPersonIds 人员ids
     */
    public void drawChartDepartType(PieChart pieChart, List<Long> orgAndFilterPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        //添加数据
        PieSeries series = pieChart.createPieSeries("离职类型");
        //===================数据取值==============================
        //查出离职类型
        DataSet dataSet = PersonInfoUtil.queryPersonInfoDep(orgAndFilterPersonIds, this.getClass().getName());
        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(dataSet);
        Set<Object> uniquePersons = new HashSet<>();
        personObjects.removeIf(obj -> !uniquePersons.add(obj.get("person")));
//        Map<Object, Long> genderMap = personObjects.stream().collect(Collectors.groupingBy(o -> o.get("tdkw_changetype"), Collectors.counting()));
        long n = 0L;
        long m = 0L;
        // 造假数据吧
        for (int i = 0; i < personObjects.size(); i++) {
            if (i % 2 == 0) {
                n++;
            } else {
                m++;
            }
        }
//        Set<Object> keySet = genderMap.keySet();
        ItemValue[] itemValues = new ItemValue[2];
        itemValues[0] = new ItemValue("离职", m);
        itemValues[1] = new ItemValue("退休", n);
        List<Long> cache = personObjects.stream().map(o -> o.getLong("person")).collect(Collectors.toList());
        this.getPageCache().put("离职类型" + "离职", cache.toString());
        this.getPageCache().put("离职类型" + "退休", cache.toString());
        int i = 0;
        // TODO 二开字段屏蔽
        /*for (Object key : keySet) {
            QFilter genderFilter = new QFilter("id", QCP.equals, key);
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("tdkw_hbss_changetype", "id,name", genderFilter.toArray());
            itemValues[i] = new ItemValue(dynamicObject.getString("name"), genderMap.get(key));
            List<Long> cache = personObjects.stream().filter(o -> o.get("tdkw_changetype").equals(dynamicObject.get("id"))).map(o -> o.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("离职类型" + dynamicObject.getString("name"), cache.toString());
            i++;
        }*/

        series.setData(itemValues);
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
        //series样式
        //        series.setRoseType(RoseType.radius);
        series.setRadius("70", "100");
    }

    /**
     * 给人员类别默认初始值
     *
     * @param e EventObject事件类
     */
    @Override
    public void afterCreateNewData(EventObject e) {
        // TODO Auto-generated method stub
        super.afterCreateNewData(e);
        long timeMillis = System.currentTimeMillis();
        IDataModel model = this.getModel();
        DynamicObjectCollection employTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
        for (DynamicObject tempId : load) {
            employTypeForDisplay.add(tempId);
        }
//        model.setValue("tdkw_employtype", employTypeForDisplay);
        PieChart pieChart = this.getControl("tdkw_piechartap");
        Long userId = Long.valueOf(RequestContext.get().getCurrUserId());
        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, this.getModel().getDataEntityType().getName(), "tdkw_org");
//        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_lz_rybhqs_pc");
        if (authorizedAdminOrgSet.isHasAllOrgPerm()) {
            // 2025-10-31 增加报表过滤，当前人组织
            //获取当前登录人员所属组织
            List<Long> userIds = new ArrayList<>(1);
            userIds.add(UserServiceHelper.getCurrentUserId());
            // 获取当前登录人员所属公司
            Long adminOrg = UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId());
            this.getModel().setValue("tdkw_org", adminOrg);

            //全集团用户默认XXX集团
//            this.getModel().setValue("tdkw_org", 100000);
        } else {
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("haos_adminorghr", "id", new QFilter[]{new QFilter("orgtype.number", QCP.in, new String[]{"XY00001", "XY00002", "XY00003", "XY00004", "XY00005"})
                    .and("id", QCP.in, authorizedAdminOrgSet.getHasPermOrgs()).and("datastatus", QCP.equals, "1").and("enable", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1")}, "sortcode");

            if (null != dynamicObjects && dynamicObjects.length > 0) {
                this.getModel().setValue("tdkw_org", dynamicObjects[0].get("id"));
            }
        }
        this.getView().updateView("tdkw_org");
        String personsListStr = this.getPageCache().get("personsList");
        List<Long> orgAndFilterPersonIds = new ArrayList<>();
        /*if (!personsListStr.isEmpty() || !"".equals(personsListStr)) {
            //有缓存读缓存
            orgAndFilterPersonIds = SerializationUtils.deSerializeFromBase64(personsListStr);
        } else {
            orgAndFilterPersonIds = getOrgAndFilterPersonIds();
        }
        this.drawChartReason(pieChart, orgAndFilterPersonIds);
       */
        logger.info("渲染时间：" + (System.currentTimeMillis() - timeMillis));
    }


    /**
     * 离职原因Top10面板
     *
     * @param pieChart              PieChart类型参数
     * @param orgAndFilterPersonIds 人员ids
     */
    public void drawChartReason(PieChart pieChart, List<Long> orgAndFilterPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");
        //添加数据
        PieSeries series = pieChart.createPieSeries("离职原因");
        //===================数据取值==============================
        //过滤离职原因
        DataSet dataSet = PersonInfoUtil.queryPersonInfoDep(orgAndFilterPersonIds, this.getClass().getName());
        DynamicObjectCollection personObjects = ORM.create().toPlainDynamicObjectCollection(dataSet);
        //去除person重复项
        Set<Object> uniquePersons = new HashSet<>();
        personObjects.removeIf(obj -> !uniquePersons.add(obj.get("person")));
        // TODO 屏蔽离职原因
        /*
        Map<Object, Long> genderMap = personObjects.stream().filter(o -> (Long) o.get("tdkw_changereason") != 0L).collect(Collectors.groupingBy(o -> o.get("tdkw_changereason"), Collectors.counting()));
        Set<Object> keySet = genderMap.keySet();
        ItemValue[] itemValues = new ItemValue[keySet.size()];
        Map<Integer, Integer> map = new HashMap<>();
        long timeMillis = System.currentTimeMillis();
        int i = 0;

       for (Object key : keySet) {
            QFilter genderFilter = new QFilter("id", QCP.equals, key);
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("tdkw_hbss_changereason", "id,name", genderFilter.toArray());
            itemValues[i] = new ItemValue(dynamicObject.getString("name"), genderMap.get(key));
            List<Long> cache = personObjects.stream().filter(o -> o.get("tdkw_changereason").equals(dynamicObject.get("id"))).map(o -> o.getLong("person")).collect(Collectors.toList());
            map.put(i, cache.size());
            this.getPageCache().put("离职原因" + dynamicObject.getString("name"), cache.toString());
            i++;
        }
        logger.info("离职原因渲染时间：" + (System.currentTimeMillis() - timeMillis));

        Map<Integer, Integer> sortedMap = map.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
        ItemValue[] newItemValues = new ItemValue[sortedMap.size()];
        int j = 0;
        for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
            Integer key = entry.getKey();
            newItemValues[j] = itemValues[key];
            j++;
        }*/
        ItemValue[] newItemValues = new ItemValue[5];
        for (int i = 0; i < 5; i++) {
            int count = 0;
            String key = "";
            count = personObjects.size() / 5 + i;
            if (i == 0) {
                key = "薪酬不符合心里预期";
            } else if (i == 1) {
                key = "通勤太远，交通不便利";
            } else if (i == 2) {
                key = "对后续职业发展感到迷茫";
            } else if (i == 3) {
                key = "能力不能胜任现有工作";
            } else {
                key = "寻求更好的发展";
            }
            List<Long> person = personObjects.stream().map(o -> o.getLong("person")).collect(Collectors.toList());
            this.getPageCache().put("离职原因" + key, person.toString());
            newItemValues[i] = new ItemValue(key, count);
        }

        series.setData(newItemValues);
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
        //series样式
        //        series.setRoseType(RoseType.radius);
        series.setRadius("70", "100");
    }


    /**
     * 岗位层级面饼（默认显示）
     *
     * @param pieChart              PieChart类型参数
     * @param orgAndFilterPersonIds 人员ids
     */
    public void drawChartLevel(PieChart pieChart, List<Long> orgAndFilterPersonIds) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        pieChart.setLegendPropValue("left", "right");

        //添加数据
        PieSeries series = pieChart.createPieSeries("岗位层级");
        //===================数据取值==============================//
        //查出岗位层级
        DataSet dataSet = PersonInfoUtil.queryPersonInfoDep(orgAndFilterPersonIds, this.getClass().getName());
        DynamicObjectCollection personOldObjects = ORM.create().toPlainDynamicObjectCollection(dataSet);
//        Map<Object, Long> postLevel = personOldObjects.stream().filter(o -> ObjectUtils.isNotEmpty(o.get("tdkw_postlevel"))).collect(Collectors.groupingBy(o -> o.get("tdkw_postlevel"), Collectors.counting()));
//        Set<Object> objects = postLevel.keySet();
        ItemValue[] itemValues = new ItemValue[5];

        for (int n = 1; n <= 5; n++) {
            String index = String.valueOf(n);
            String keyName = "";
            if (StringUtils.equals(index, "1")) {
                keyName = "集团高管-集团直管";
                List<Long> cache = personOldObjects.stream()
//                        .filter(j -> StringUtils.equals("1", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(index, "2")) {
                keyName = "集团高管-授权行业";
                List<Long> cache = personOldObjects.stream()
//                        .filter(j -> StringUtils.equals("2", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(index, "3")) {
                keyName = "其他高管";
                List<Long> cache = personOldObjects.stream()
//                        .filter(j -> StringUtils.equals("3", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(index, "4")) {
                keyName = "中层";
                List<Long> cache = personOldObjects.stream()
//                        .filter(j -> StringUtils.equals("4", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(index, "5")) {
                keyName = "基层";
                List<Long> collect1 = personOldObjects.stream()
//                        .filter(j -> StringUtils.equals("5", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, collect1.toString());
            }

            itemValues[n - 1] = new ItemValue(keyName, personOldObjects.size() / 5 + n);
        }
        /*int i = 0;
        for (Object key : objects) {
            String keyName = key.toString();
            if (StringUtils.equals(keyName, "1")) {
                keyName = "集团高管-集团直管";
                List<Long> cache = personOldObjects.stream()
                        .filter(j -> StringUtils.equals("1", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "2")) {
                keyName = "集团高管-授权行业";
                List<Long> cache = personOldObjects.stream()
                        .filter(j -> StringUtils.equals("2", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "3")) {
                keyName = "其他高管";
                List<Long> cache = personOldObjects.stream()
                        .filter(j -> StringUtils.equals("3", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "4")) {
                keyName = "中层";
                List<Long> cache = personOldObjects.stream()
                        .filter(j -> StringUtils.equals("4", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, cache.toString());
            } else if (StringUtils.equals(keyName, "5")) {
                keyName = "基层";
                List<Long> collect1 = personOldObjects.stream()
                        .filter(j -> StringUtils.equals("5", j.getString("tdkw_postlevel")))
                        .map(j -> j.getLong("person"))
                        .collect(Collectors.toList());
                this.getPageCache().put("岗位层级" + keyName, collect1.toString());
            }

            itemValues[i] = new ItemValue(keyName, postLevel.get(key));
            i++;
        }*/
        series.setData(itemValues);
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
        series.setRadius("70", "100");
    }
}
