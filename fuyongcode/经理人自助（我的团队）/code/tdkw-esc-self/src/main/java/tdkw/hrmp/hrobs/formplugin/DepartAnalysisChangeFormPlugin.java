package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
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
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.haos.business.servicehelper.AdminOrgQueryServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.util.Pair;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName DepartAnalysisChangeFormPlugin
 * @author xxx
 * @Date 2023/7/6 17:46
 */
public class DepartAnalysisChangeFormPlugin extends AbstractFormPlugin implements ClickListener {
    private static final Log logger = LogFactory.getLog(DepartAnalysisChangeFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_pointlinechartap");
        this.addClickListeners("tdkw_query");
    }


    @Override
    public void afterCreateNewData(EventObject e) {
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
        // TODO 人员组织获取
        //model.setValue("tdkw_org", 100000L);
        Long userId = Long.valueOf(RequestContext.get().getCurrUserId());
        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, this.getModel().getDataEntityType().getName(), "tdkw_org");
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
        PointLineChart pointLineChart = this.getControl("tdkw_pointlinechartap");
        this.drawChart(pointLineChart, model);
        logger.info("渲染时间：" + (System.currentTimeMillis() - timeMillis));
    }


    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        if (key.equals("tdkw_pointlinechartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name) || StringUtils.equals(this.getPageCache().get(name), "[]")) {
                return;
            }
            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_depart_report");
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            showParameter.setHasRight(true);
            showParameter.setCustomParam("personIdsDepart", this.getPageCache().get(name));
            this.getView().showForm(showParameter);
        } else if (key.equals("tdkw_query")) {
            IDataModel model = this.getModel();
            PointLineChart pointLineChart = this.getControl("tdkw_pointlinechartap");
            pointLineChart.clearGraphic();
            this.drawChart(pointLineChart, model);
        }
    }


    public void drawChart(PointLineChart pointLineChart, IDataModel model) {
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
        List<Pair<Date, Date>> monthList = getMonthList(pointLineBeginDate, pointLineEndDate);
        // 设置分类轴数据
        categoryAxis.setCategorys(contructCatetoryData(monthList));
        // 创建折线并赋值
        this.createLineSeries(pointLineChart, "人员", contructValueData(monthList, pointLineBeginDate, pointLineEndDate), "");
        // 设置图的边距
        pointLineChart.setMargin(Position.right, "30px");
        pointLineChart.setMargin(Position.top, "80px");
        pointLineChart.setMargin(Position.left, "30px");
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
     * @Date 2023/6/20 11:21
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

    /**
     * @author xxx
     * @Description 构建折线图数据
     * @Date 2023/6/28 10:08
     */
    private List<BigDecimal> contructValueData(List<Pair<Date, Date>> monthList, Date pointLineBeginDate, Date pointLineEndDate) {
        List<BigDecimal> valueData = new ArrayList<>();
        DynamicObject dataEntity = this.getModel().getDataEntity();
        // 组织
        Long tdkwOrg = dataEntity.getDynamicObject("tdkw_org").getLong("id");
        if (ObjectUtils.isEmpty(tdkwOrg)) {
            return valueData;
        }
        //获取人员权限组织范围
        boolean hasAllOrgPerm = true;
        if (tdkwOrg != 100000) {
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_lz_rybhqs_pc");
            logger.info("人员权限信息：" + result);
            hasAllOrgPerm = result.isHasAllOrgPerm();
        }
        //获取需过滤的组织ID
        List<Long> allPersonByOrg = new ArrayList<>();
        if (tdkwOrg != 100000 || !hasAllOrgPerm) {
            //非全集团需要获取组织信息
            allPersonByOrg = PersonInfoUtil.getAllPersonByOrg((Long) tdkwOrg);
        }
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

//        List<Map<String, Object>> subOrgs = AdminOrgQueryServiceHelper.batchQueryAllSubOrg(orgId, new Date());
//        List<Long> subOrgIds = subOrgs.stream().map(i -> (Long) i.get("orgId")).collect(Collectors.toList());
        //人员流入流出表--异动类型-所属变动大类=离职--人的id集合
//        DynamicObject[] changeTypes = BusinessDataServiceHelper.load("tdkw_hbss_changetype", "tdkw_chgevent", new QFilter("tdkw_chgevent.name", QCP.equals, "离职").toArray());
        DynamicObject[] changeTypes = new DynamicObject[]{};
        List<Long> idList = new ArrayList<>();
        QFilter personFiler = new QFilter("businessstatus", QCP.equals, "1");
        personFiler.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        personFiler.and("empposrel.postype.number", QCP.equals, "XY00001");
        personFiler.and("empposrel.datastatus", QCP.equals, "1");
        personFiler.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        if (ObjectUtils.isNotEmpty(tdkw_jobsequence) && tdkw_jobsequence.size() != 0) {
            List<Object> jobSequence = tdkw_jobsequence.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            personFiler.and("empposrel.tdkw_jobsequence", QCP.in, jobSequence);
        }
        if (ObjectUtils.isNotEmpty(tdkw_gender) && tdkw_gender.size() != 0) {
            List<Object> gender = tdkw_gender.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            personFiler.and("pernontsprop.gender", QCP.in, gender);
        }
        if (ObjectUtils.isNotEmpty(tdkw_employtype) && tdkw_employtype.size() != 0) {
            List<Object> employType = tdkw_employtype.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            personFiler.and("empposrel.tdkw_employtype", QCP.in, employType);
        }
        if (ObjectUtils.isNotEmpty(tdkw_job) && tdkw_job.size() != 0) {
            List<Object> duties = tdkw_job.stream().map(i -> i.getDynamicObject("fbasedataid").getPkValue()).collect(Collectors.toList());
            DynamicObjectCollection query = QueryServiceHelper.query("hbjm_jobgradehr", "entryboid", new QFilter[]{new QFilter("id", QCP.in, duties)});
            Collection<Long> entryboid = query.stream().map(i -> i.getLong("entryboid")).collect(Collectors.toList());
            personFiler.and("empposrel.tdkw_ranks.entryboid", QCP.in, entryboid);
        }
        if (ObjectUtils.isNotEmpty(tdkw_postlevel)) {
            String[] postLevel = tdkw_postlevel.substring(1, tdkw_postlevel.length() - 1).split(",");
            personFiler.and("empposrel.tdkw_postlevel", QCP.in, postLevel);
        }
        for (DynamicObject changeType : changeTypes) {
            Long pkValue = changeType.getLong("id");
            idList.add(pkValue);
        }


        Map<String, ArrayList<Long>> personMap = new HashMap<>();
        QFilter quitfileQfilter = new QFilter("contractenddate", QCP.less_equals, pointLineEndDate)
                .and("contractenddate", QCP.large_equals, pointLineBeginDate);
        DataSet quitfileDataSet = ORM.create().queryDataSet(this.getClass().getName(), "htm_quitfileinfo", "employee.id,contractenddate as flowtime", new QFilter[]{quitfileQfilter});
//        QFilter flowQF = new QFilter("tdkw_changetype.id", QCP.in, idList)
//                .and("depemp.isprimary", QCP.equals, "1");

        QFilter flowQF = new QFilter("depemp.isprimary", QCP.equals, "1");
//                .and("adminorg.company", QCP.in, subOrgIds)
//                .and("flowtime", QCP.less_equals, pointLineEndDate).and("flowtime", QCP.large_equals, pointLineBeginDate)

        //非全集团才需要对组织过滤
        if (tdkwOrg != 100000 || !hasAllOrgPerm) {
            flowQF.and("adminorg", QCP.in, allPersonByOrg);
        }

        DataSet flowDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hpfs_personflow", "employee.id,person", new QFilter[]{flowQF});
        flowDataSet = quitfileDataSet.leftJoin(flowDataSet).on("employee.id", "employee.id").select("person", "flowtime").finish();
        DynamicObjectCollection load = ORM.create().toPlainDynamicObjectCollection(flowDataSet);
//        DynamicObject[] load = BusinessDataServiceHelper.load("hpfs_personflow", "person,flowtime", new QFilter("tdkw_changetype.id", QCP.in, idList)
//                .and("adminorg.company", QCP.in, subOrgIds)
//                .and("flowtime", QCP.less_equals, pointLineEndDate).and("flowtime", QCP.large_equals, pointLineBeginDate)
//                .and("depemp.isprimary", QCP.equals, "1").toArray());
//        List<Long> depPersonIds = Arrays.stream(load).map(o -> o.getLong("person.id")).collect(Collectors.toList());
        for (DynamicObject object : load) {
//            int flowtime = ((Date) object.get("flowtime")).getCalendarDate().getYear();

            String flowtime = object.getString("flowtime");
            String key = flowtime.substring(0, 7);
            ArrayList<Long> person;
            if (personMap.containsKey(key)) {
                person = personMap.get(key);
            } else {
                person = new ArrayList<>(1);
            }
            person.add(object.getLong("person"));
            personMap.put(key, person);
        }
        // 人事业务档案
        DynamicObjectCollection hspm_ermanfile = QueryServiceHelper.query("hspm_ermanfile", "person.id", personFiler.toArray());
        List<Long> erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
        if (ObjectUtils.isNotEmpty(tdkw_education) && tdkw_education.size() != 0) {
            // 学历
            List<Object> education = tdkw_education.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter pereduexpFilter = new QFilter("person.id", QCP.in, erManFileIds);
            pereduexpFilter.and("education", QCP.in, education);
            // TODO 屏蔽二开字段
//            pereduexpFilter.and("tdkw_ishighestcheck", QCP.equals, "1");
            pereduexpFilter.and("hisversion", QCP.equals, "");
            // 教育经历
            hspm_ermanfile = QueryServiceHelper.query("hrpi_pereduexp", "person.id", pereduexpFilter.toArray());
            erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
        }
        if (ObjectUtils.isNotEmpty(tdkw_politicalstatus) && tdkw_politicalstatus.size() != 0) {
            // 政治面貌
            List<Object> politicalStatus = tdkw_politicalstatus.stream().map(i -> ((DynamicObject) i.get(1)).getPkValue()).collect(Collectors.toList());
            QFilter perregionFilter = new QFilter("person.id", QCP.in, erManFileIds);
            perregionFilter.and("politicalstatus", QCP.in, politicalStatus);
            // 基本信息补充
            hspm_ermanfile = QueryServiceHelper.query("hrpi_perregion", "person.id", perregionFilter.toArray());
            erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("person.id")).collect(Collectors.toList());
        }
        List<Long> personsList = new ArrayList<>();
        for (Pair<Date, Date> dateDatePair : monthList) {
            Date dateBegin = dateDatePair.getFirst();
            Date date = dateDatePair.getSecond();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String key = sdf.format(dateBegin);
            ArrayList<Long> person;
            if (personMap.containsKey(key)) {
                person = personMap.get(key);
            } else {
                person = new ArrayList<>(1);
            }
            //获取组织以及组织下属id
            ArrayList<Long> personList = new ArrayList<>(1);
            for (Long id : person) {
                if (erManFileIds.contains(id)) {
                    personList.add(id);
                }
            }
            IPageCache pageCache = this.getPageCache();
            Set<Object> uniquePersons1 = new HashSet<>();
            personList.removeIf(obj -> !uniquePersons1.add(obj));
            pageCache.put(DateTimeUtils.dateFormat(date, "yyyy.MM"), personList.toString());
            valueData.add(BigDecimal.valueOf(personList.size()));
            personsList.addAll(personList);
        }
        String personsListStr = SerializationUtils.serializeToBase64(personsList);
        this.getPageCache().put("personsList", personsListStr);
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
        if (isx)
            axis = pointLineChart.createXAxis(name, AxisType.category);
        else
            axis = pointLineChart.createYAxis(name, AxisType.category);
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
        if (isx)
            axis = pointLineChart.createXAxis(name, AxisType.value);
        else
            axis = pointLineChart.createYAxis(name, AxisType.value);
        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> axisTick = Maps.newHashMap();
        axisTick.put("show", true);
        axis.setPropValue("axisTick", axisTick);
        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> splitLine = Maps.newHashMap();
        Map<String, Object> lineStyle = Maps.newHashMap();
        lineStyle.put("type", "dotted");
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
        label.setRotate("-30");
        expireSeries.setLabel(label);
        // 连线颜色
        expireSeries.setItemColor(color);
        // 动画效果
        expireSeries.setAnimationDuration(2000);
        // 该点纵坐标的值setData(Number[] data)
        expireSeries.setData((Number[]) values.toArray(new Number[0]));
    }
}