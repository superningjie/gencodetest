package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
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
import org.apache.commons.lang3.ObjectUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 人员信息完整度图表插件
 * @author xxx
 * @date: 2023/11/14 11:35
 * @param:
 * @param: null
 * @return: null
 **/
public class PersonInfoIntegrityPlugin extends AbstractFormPlugin {
    private final Log logger = LogFactory.getLog(PersonInfoIntegrityPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //人员简历完整度柱状图
        this.addClickListeners("tdkw_resumechartap");
        // 查询按钮
        this.addClickListeners("tdkw_querybtn");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();

        //简历完整度柱状图
        if (key.equals("tdkw_resumechartap")) {
            ChartClickEvent e = (ChartClickEvent) evt;
            String name = e.getName();
            if (ObjectUtils.isEmpty(name)) {
                return;
            }

            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId("tdkw_personfileintegrity");
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            showParameter.setCaption(name);
            showParameter.setCustomParam("orgId", this.getPageCache().get("orgId"));
            showParameter.setCustomParam("personIds", this.getPageCache().get(this.getClass().getName() + "完整度" + name));
            this.getView().showForm(showParameter);
        } else if (key.equals("tdkw_querybtn")) {
            HistogramChart histogramChartResume = this.getControl("tdkw_resumechartap");
            this.drawChartResume(histogramChartResume);
            this.getView().updateView("tdkw_resumechartap");
        }

    }


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        HistogramChart histogramChartResume = this.getControl("tdkw_resumechartap");
        this.drawChartResume(histogramChartResume);
    }

    /**
     * 绘制总人数柱状图表
     *
     * @param histogramChart
     */
    public void drawChartResume(HistogramChart histogramChart) {
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

        Long tdkwOrgId = tdkwOrg.getLong("id");

        boolean hasAllOrgPerm = true;
        if (tdkwOrgId != 100000) {
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ryxxwzd_pc");
            logger.info("人员权限信息：" + result);
            hasAllOrgPerm = result.isHasAllOrgPerm();
        }

        //获取需过滤的组织ID
        List<Long> allPersonByOrg = new ArrayList<>();
        QFilter erManFileFiler = new QFilter("1", QCP.in, 1);

        if (tdkwOrgId != 100000 || !hasAllOrgPerm) {
            //非全集团需要获取组织信息
            allPersonByOrg = getAllPersonByOrg((Long) tdkwOrgId);
            erManFileFiler.and("empposrel.adminorg", QCP.in, allPersonByOrg);
            logger.info("人员组织过滤：" + allPersonByOrg);
        }
        //组织下所有人员
        DataSet personInfoIntegrity = PersonInfoUtil.getPersonInfoIntegrity(erManFileFiler, this.getClass().getName());
        // 查询缓存人员信息
        DynamicObjectCollection allErManFileByPerson = ORM.create().toPlainDynamicObjectCollection(personInfoIntegrity.copy());
        logger.info("简历信息完整度人员信息查询耗时" + (System.currentTimeMillis() - start));
        logger.info("未筛选前人员" + allErManFileByPerson);

        //判断逻辑（符合以下条件人员在人员档案信息修改时进行稽核校验）：
//        1、【职业信息的“用工关系类型”在“XY00001正式员工”】 且【主任职的所属板块=XXX地产，且岗位名称不包含“置业顾问” ，且职层不在“O6”、“O7”、“O8”中】
//        2、【职业信息的“用工关系类型”=“XY00005退休返聘员工” 且 用工关系状态=XY00019 返聘中】
//        3、【职业信息的“用工关系类型”=“XY00003 内退员工” 且 “用工关系状态”=XY00018 已内部退养】
        //符合正式员工情况下逻辑
        List<DynamicObject> formalAndInsured = allErManFileByPerson.stream().filter(person -> "XY00001".equals(person.getString("tdkw_employtype.number")))
//                .filter(person -> (!person.getString("position.name").contains("置业顾问") && "XXX地产".equals(person.getString("tdkw_sectorid.name"))))
                .filter(person -> !"O6".equals(person.getString("tdkw_ranks.number")) &&
                        !"O7".equals(person.getString("tdkw_ranks.number")) &&
                        !"O8".equals(person.getString("tdkw_ranks.number")))
                .collect(Collectors.toList());
        //剔除主任职的所属板块=XXX地产，且岗位名称不包含“置业顾问
        formalAndInsured = PersonInfoUtil.removeSectoridAndPosition(formalAndInsured);
        logger.info("符合正式员工情况下人员" + formalAndInsured.size());
        //符合退休返聘员工和返聘中情况下逻辑
        List<DynamicObject> retirementAndReEmployment = allErManFileByPerson.stream().filter(person -> "XY00005".equals(person.getString("tdkw_employtype.number")) || "XY000019".equals(person.getString("laborrelstatus.number")))
                .filter(person -> !"O6".equals(person.getString("tdkw_ranks.number")) &&
                        !"O7".equals(person.getString("tdkw_ranks.number")) &&
                        !"O8".equals(person.getString("tdkw_ranks.number")))
                .collect(Collectors.toList());
        //剔除主任职的所属板块=XXX地产，且岗位名称不包含“置业顾问
        retirementAndReEmployment = PersonInfoUtil.removeSectoridAndPosition(retirementAndReEmployment);
        logger.info("符合退休返聘员工和返聘中情况下逻辑人员" + retirementAndReEmployment.size());
        //符合内退员工和已内部退养情况下逻辑
        List<DynamicObject> earlyRetirementAndRetirement = allErManFileByPerson.stream().filter(person -> "XY00003".equals(person.getString("tdkw_employtype.number")) || "XY000018".equals(person.getString("laborrelstatus.number")))
                .filter(person -> !"O6".equals(person.getString("tdkw_ranks.number")) &&
                        !"O7".equals(person.getString("tdkw_ranks.number")) &&
                        !"O8".equals(person.getString("tdkw_ranks.number")))
                .collect(Collectors.toList());
        //剔除主任职的所属板块=XXX地产，且岗位名称不包含“置业顾问
        earlyRetirementAndRetirement = PersonInfoUtil.removeSectoridAndPosition(earlyRetirementAndRetirement);
        logger.info("符合内退员工和已内部退养情况下人员" + earlyRetirementAndRetirement.size());

        List<DynamicObject> personObjects = new ArrayList<>();
        personObjects.addAll(formalAndInsured);
        personObjects.addAll(retirementAndReEmployment);
        personObjects.addAll(earlyRetirementAndRetirement);

        logger.info("简历信息完整度人员信息筛选汇合耗时" + (System.currentTimeMillis() - start));

        List<String> xNames = new ArrayList<>();
        List<Number> yDatas = new ArrayList<>();

        List<Long> dirOrgPersonAll = new ArrayList<>();
        BigDecimal progressbarSumAll = BigDecimal.ZERO;
        for (Long finalOrgId : finalOrgIds) {
            //获取当前层级组织及其下级组织人员
            List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(finalOrgId.toString());
            List<Long> dirOrgPersons = personObjects.stream()
                    .filter(i -> allBelowHROrg.contains(i.getLong("adminorg.id")))
                    .map(i -> i.getLong("person.id"))
                    .collect(Collectors.toList());

            if (dirOrgPersons.size() == 0) {
                continue;
            }
            // 简历完整度合计
            BigDecimal progressbarSum = personObjects.stream().filter(i -> (Long) i.get("tdkw_company") != 0).
                    filter(i -> dirOrgPersons.contains(i.getLong("person.id"))).map(i -> i.getBigDecimal("tdkw_progressbar")).reduce(BigDecimal.ZERO, BigDecimal::add);

            QFilter orgFilter = new QFilter("id", QCP.equals, finalOrgId);
            DynamicObject orgObject = QueryServiceHelper.queryOne("haos_adminorghr", "id,name", orgFilter.toArray());

            xNames.add(orgObject.getString("name"));

            progressbarSumAll = progressbarSumAll.add(progressbarSum);
            dirOrgPersonAll.addAll(dirOrgPersons);


            BigDecimal divide = progressbarSum.divide(new BigDecimal(dirOrgPersons.size()), 5, BigDecimal.ROUND_HALF_UP);
            divide = divide.multiply(new BigDecimal(100));
            divide = divide.setScale(2, BigDecimal.ROUND_HALF_UP);
            yDatas.add(divide.doubleValue());

            this.getPageCache().put(this.getClass().getName() + "完整度" + orgObject.getString("name"), dirOrgPersons.toString());

        }


        // 直属组织下人数
        List<Long> dirOrgPersons = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue())).map(i -> i.getLong("person")).collect(Collectors.toList());
        if (haos_adminorghr.size() != 0 && dirOrgPersons.size() != 0) {
            // 简历完整度合计
            BigDecimal progressbarSum = personObjects.stream().filter(i -> i.get("tdkw_dept").equals(tdkwOrg.getPkValue())).map(i -> i.getBigDecimal("tdkw_progressbar")).reduce(BigDecimal.ZERO, BigDecimal::add);
            progressbarSumAll = progressbarSumAll.add(progressbarSum);
            dirOrgPersonAll.addAll(dirOrgPersons);
        }

        //如果只有一个部门则不显示汇总,或所选组织与筛选出组织不一致
        if (!finalOrgIds.get(0).equals(tdkwOrgId) || finalOrgIds.size() > 1) {
            //汇总数
            if (dirOrgPersonAll.size() != 0) {
                xNames.add(dataEntity.getDynamicObject("tdkw_org").getString("name"));

                BigDecimal divides = progressbarSumAll.divide(new BigDecimal(dirOrgPersonAll.size()), 5, BigDecimal.ROUND_HALF_UP);
                divides = divides.multiply(new BigDecimal(100));
                divides = divides.setScale(2, BigDecimal.ROUND_HALF_UP);
                yDatas.add(divides.doubleValue());
            }
            this.getPageCache().put(this.getClass().getName() + "完整度" + dataEntity.getDynamicObject("tdkw_org").getString("name"), dirOrgPersonAll.toString());
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

        this.getPageCache().put("orgId", String.valueOf(dataEntity.getLong("tdkw_org.id")));

        logger.info("人员信息完整度耗时" + (System.currentTimeMillis() - start));

    }


    public static List<Long> getAllPersonByOrg(Long orgId) {

        //组织集合（获取下级组织）
        List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(orgId.toString());

        //获取权限信息
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ryxxwzd_pc");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //全组织查看权限
        if (!hasAllOrgPerm) {
            //权限组织
            List<Long> hasPerOrg = result.getHasPermOrgs();
            //获取普通组织与权限组织的交集
            allBelowHROrg.retainAll(hasPerOrg);
        }
//        QFilter filter = new QFilter("empposrel.adminorg", QCP.in, allBelowHROrg);
//        //日期过滤
//        filter.and(new QFilter("empposrel.startdate", "<=", date).and("empposrel.enddate", ">=", date));

        return allBelowHROrg;
    }
}
