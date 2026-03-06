package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.datamodel.events.QueryImportBasedataEventArgs;
import kd.bos.exception.KDBizException;
import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.ConfirmTypes;
import kd.bos.form.FormShowParameter;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import tdkw.opa.tdkw_opa.formplugin.enums.LightingDetailsVO;
import tdkw.opa.tdkw_opa.formplugin.enums.OrgHeaderVO;
import tdkw.opa.tdkw_opa.formplugin.enums.OrgMapVO;
import tdkw.opa.tdkw_opa.formplugin.enums.OrgTargetVO;
import tdkw.opa.tdkw_opa.formplugin.enums.TagVO;
import tdkw.opa.tdkw_opa.formplugin.enums.Target;
import tdkw.opa.tdkw_opa.formplugin.utils.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author: xxx
 * @create: 2024/08/20 10:34
 * @description: 组织绩效地图
 **/
public class PerformanceMapFormPlugin extends AbstractFormPlugin {
    private static final Log logger = LogFactory.getLog(PerformanceMapFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("tdkw_toolbarap");
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        this.getModel().setValue("tdkw_date", getThisYear(0));
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        String isFirst = this.getView().getPageCache().get("isFirst");
        if (StringUtils.isBlank(isFirst)) {
            mapInit();
            this.getView().getPageCache().put("isFirst", "0");
        }

    }

    private void mapInit() {
        //初始化年份
        Object dateField = this.getModel().getValue("tdkw_date");
        if (dateField == null) {
            this.getModel().setValue("tdkw_date", getThisYear(0));
        }
        Date date = (Date) this.getModel().getValue("tdkw_date");
        //查询亮灯规则
        //使用状态为可用
        QFilter lightFilter = new QFilter("enable", QCP.equals, "1");
        DynamicObject lightRule = BusinessDataServiceHelper.loadSingle("tdkw_light_rule", "tdkw_integerredbig,tdkw_integeryellowbig", lightFilter.toArray());

        Object orgIdObject = this.getModel().getValue("tdkw_orgid");
        if (orgIdObject == null) {
            return;
        }

        //获取当前用户
        long orgId = Long.parseLong(orgIdObject.toString());

        //初始默认KPI
        Object typeNumberField = this.getModel().getValue("tdkw_targettypenumber");
        if (typeNumberField == null) {
            this.getModel().setValue("tdkw_targettypenumber", "T0001");
        }
        String targetTypeNumber = (String) this.getModel().getValue("tdkw_targettypenumber");
        OrgHeaderVO decompose = performanceDecompose(orgId, date, targetTypeNumber, lightRule);
        OrgHeaderVO alignment = performanceAlignment(orgId, date, targetTypeNumber, lightRule);
        OrgMapVO orgMapVO = new OrgMapVO();
        orgMapVO.setDecomposeHeaderVO(decompose);
        orgMapVO.setAlignmentHeaderVO(alignment);

        //初始化的时候传全部的指标类型
        QFilter targetNumberFilter = new QFilter("number", QCP.in, new String[]{"T0001", "T0002", "T0003","T0004"});
        DynamicObject[] metricTypes = BusinessDataServiceHelper.load("tdkw_metric_type", "number,name", targetNumberFilter.toArray(),"number");

        ArrayList<Target> targets = new ArrayList<>();
        for (DynamicObject metricType : metricTypes) {
            Target target = new Target();
            target.setLabel(metricType.getString("name"));
            target.setValue(metricType.getString("number"));
            targets.add(target);
        }
        orgMapVO.setTarget(targets);

        CustomControl control = this.getControl("customcontrolap");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", orgMapVO);
        jsonObject.put("click", "update");
        jsonObject.put("time", System.currentTimeMillis());
        control.setData(jsonObject);
    }



    @Override
    public void customEvent(CustomEventArgs e) {
        super.customEvent(e);
        String eventName = e.getEventName();
        String eventArgs = e.getEventArgs();
        JSONObject param;
        try {
            //获取自定义控件传过来的json
            param = JSONObject.parseObject(eventArgs);
        } catch (Exception exception) {
            throw new KDBizException("参数不规范!");
        }
        CustomControl control = this.getControl("customcontrolap");
        switch (eventName) {
            case "select":
                if (param == null || param.get("year") == null || param.get("type") == null) {
                    return;
                }
                //查询亮灯规则
                //使用状态为可用
                QFilter lightFilter = new QFilter("enable", QCP.equals, "1");
                DynamicObject lightRule = BusinessDataServiceHelper.loadSingle("tdkw_light_rule", "tdkw_integerredbig,tdkw_integeryellowbig", lightFilter.toArray());


                long orgId = Long.parseLong(this.getView().getPageCache().get("orgId"));
                Date year = param.getDate("year");
                String type = param.getString("type");
                this.getModel().setValue("tdkw_date", year);
                this.getModel().setValue("tdkw_targettypenumber", type);
                OrgHeaderVO decompose = performanceDecompose(orgId, year, type, lightRule);
                OrgHeaderVO alignment = performanceAlignment(orgId, year, type, lightRule);
                OrgMapVO orgMapVO = new OrgMapVO();
                orgMapVO.setDecomposeHeaderVO(decompose);
                orgMapVO.setAlignmentHeaderVO(alignment);

                JSONObject jsonObject = new JSONObject();
//            jsonObject.put("data","");
                if (decompose == null && alignment == null) {
                    jsonObject.put("data", new ArrayList<>());
                } else {
                    jsonObject.put("data", orgMapVO);
                }
                jsonObject.put("click", "update");
                jsonObject.put("time", System.currentTimeMillis());
                control.setData(jsonObject);

                break;
            case "viewDetail":

                if (param == null) {
//                    this.getView().showConfirm("尚未维护完成情况，暂无亮灯详情。", MessageBoxOptions.OK, ConfirmTypes.Fail, null);
                    return;
                } else {
                    if (param.get("lightingDetailsVO") == null) {
//                        this.getView().showConfirm("尚未维护完成情况，暂无亮灯详情。", MessageBoxOptions.OK, ConfirmTypes.Fail, null);
                        return;
                    }
                    JSONObject detailsVO = JSONArray.parseObject(param.get("lightingDetailsVO").toString());

                    if (detailsVO == null) {
                        this.getView().showConfirm("尚未维护完成情况，暂无亮灯详情。", MessageBoxOptions.OK, ConfirmTypes.Fail, null);
                        return;
                    }
                    //创建弹出页面对象，FormShowParameter表示弹出页面为动态表单
                    FormShowParameter showParameter = new FormShowParameter();
                    //设置弹出哪个动态表单
                    showParameter.setFormId("tdkw_map_detail");
                    //设置弹出页面标题
                    showParameter.setCaption("指标详情");
                    //设置弹出页面的打开方式，支持模态，新标签等
                    showParameter.getOpenStyle().setShowType(ShowType.Modal);
                    //弹出动态表单页面
                    showParameter.setCustomParam("detailsVO", detailsVO);
                    this.getView().showForm(showParameter);
                }
                break;
        }
    }

    /**
     * 指标分解
     *
     * @param orgId 组织id
     * @param year  考核年份
     * @param
     */
    public OrgHeaderVO performanceDecompose(long orgId, Date year, String targetTypeNumber, DynamicObject lightRule) {

        logger.info("指标分解入参orgId:{},year:{},targetTypeNumber:{}", orgId, year, targetTypeNumber);
        QFilter nowFilter = new QFilter("tdkw_adminorg.id", QCP.equals, orgId);

        LocalDate localDate = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int assessYear = localDate.getYear();

        nowFilter.and("YEAR(tdkw_assess_year)", QCP.equals, assessYear);
        nowFilter.and("tdkw_hideentry.tdkw_area_type.number", QCP.equals, targetTypeNumber);
//        QFilter nowFilter = addFilter(Collections.singletonList(orgId), year, targetTypeNumber);
        //查询到本级组织的指标
        String selectProperties = "tdkw_adminorg.id,tdkw_adminorg.name,tdkw_adminorg.number,tdkw_assess_year,tdkw_eval_res_list,tdkw_hideentry.tdkw_area_type.number,tdkw_hideentry.tdkw_area_type.name," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_superorg,tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id,tdkw_hideentry.tdkw_subentryentity.tdkw_metric.name," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_metric.tdkw_description,tdkw_hideentry.tdkw_subentryentity.tdkw_metric.tdkw_rating_standard," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_metric.tdkw_metric_type.number,tdkw_hideentry.tdkw_subentryentity.tdkw_weight," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_metric_str,tdkw_hideentry.tdkw_subentryentity.tdkw_metric_desc,tdkw_hideentry.tdkw_subentryentity.tdkw_rating_standard," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_target_value,tdkw_hideentry.tdkw_subentryentity.tdkw_unit," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_q1_completion," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_q2_completion,tdkw_hideentry.tdkw_subentryentity.tdkw_q3_completion," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_q4_completion,tdkw_hideentry.tdkw_subentryentity.tdkw_annual_val," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_ctr_annual_val,tdkw_hideentry.tdkw_subentryentity.tdkw_eval_res";

        DynamicObjectCollection orgPerfMetrics = QueryServiceHelper.query("tdkw_org_perf_metrics", selectProperties, nowFilter.toArray());
        logger.info("orgPerfMetrics:{}", orgPerfMetrics);
        if (orgPerfMetrics == null || orgPerfMetrics.size() == 0) {
            return null;
        }
        //本级 组织绩效指标  组织id+指标id
        Map<String, DynamicObject> targetDetailMap = orgPerfMetrics.stream().collect(Collectors.toMap(o -> o.getString("tdkw_adminorg.id") + "," + o.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id"), o -> o, (a, b) -> a));
        logger.info("targetDetailMap:{}", targetDetailMap);

        //TODO 头部统一赋值
        OrgHeaderVO orgHeaderVO = assignmentOrgHeaderVO(new OrgHeaderVO(), orgPerfMetrics.get(0), lightRule);
        ArrayList<OrgTargetVO> targetVOList = new ArrayList<>();
        orgHeaderVO.setChildren(targetVOList);
        logger.info("orgHeaderVO:{}", orgHeaderVO);

        //重点工作类，协同类
        if (StringUtils.equals(targetTypeNumber, "T0003") || StringUtils.equals(targetTypeNumber, "T0004")) {
            //TODO 展示本级指标
            for (DynamicObject orgPerfMetric : orgPerfMetrics) {
                //TODO 第二层指标 -无论分解与否
                OrgTargetVO orgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), orgPerfMetric, true, lightRule);
                targetVOList.add(orgTargetVO);
                logger.info("重点工作类，协同类orgTargetVO:{}", orgTargetVO);
            }
        } //部门-kpi
       else if (StringUtils.equals(targetTypeNumber, "T0002") ) {
            //TODO 展示本级指标
            for (DynamicObject orgPerfMetric : orgPerfMetrics) {
                //TODO 第二层指标 -无论分解与否
                OrgTargetVO orgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), orgPerfMetric, true, lightRule);
                targetVOList.add(orgTargetVO);
                logger.info("部门kpi类orgTargetVO:{}", orgTargetVO);
            }

        }
        //kpi类
        else if (StringUtils.equals(targetTypeNumber, "T0001")) {
            //获取本组织的所有下级组织id
            List<Long> orgIdList = HRRoleAndPersonUtils.getHROrgIds(String.valueOf(orgId));
            //过滤掉本组织id
            Long remove = orgIdList.remove(0);
            logger.info("remove掉的是本组织id吗:{}", remove);
//            orgIdList=orgIdList.stream().filter(o->!o.equals(orgId)).collect(Collectors.toList());
            QFilter juniorFilter = addFilter(orgIdList, year, targetTypeNumber);
            //查询组织绩效指标，用于赋值详情
            DynamicObjectCollection juniorOrgPerfMetrics = QueryServiceHelper.query("tdkw_org_perf_metrics", selectProperties, juniorFilter.toArray());
            //下级组织绩效指标 key:组织id+指标id
            Map<String, DynamicObject> juniorTargetDetailMap = juniorOrgPerfMetrics.stream().collect(Collectors.toMap(o -> o.getString("tdkw_adminorg.id") + "," + o.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id"), o -> o, (a, b) -> a));
            logger.info("juniorTargetDetailMap:{}", juniorTargetDetailMap);
            //查询指标对齐
            QFilter alignmentFilter = new QFilter("tdkw_targetalienadmin", QCP.in, orgIdList);
            alignmentFilter.and("YEAR(tdkw_org_year)", QCP.equals, assessYear);
            //TODO 只展示对齐的
            alignmentFilter.and("tdkw_targetalientry.tdkw_alignmentstatus", QCP.equals, "10");

            String selectTargetAlignmentProperties = "id,tdkw_targetalienadmin.id,tdkw_org_year,tdkw_targetalientry,tdkw_targetalientry.tdkw_alignmentstatus," +
                    "tdkw_targetalientry.tdkw_sourceadmin,tdkw_targetalientry.tdkw_findctid,tdkw_targetalientry.tdkw_findctid.tdkw_description,tdkw_targetalientry.tdkw_alignmentid," +
                    "tdkw_targetalientry.tdkw_findctid_name,tdkw_targetalientry.tdkw_findcttype.id,tdkw_targetalientry.tdkw_findctdesc,tdkw_targetalientry.tdkw_decomposedesc," +
                    "tdkw_targetalientry.tdkw_decomposebillid";
            DynamicObjectCollection targetAlignments = QueryServiceHelper.query("tdkw_target_alignment", selectTargetAlignmentProperties, alignmentFilter.toArray());
            String properties = "id,tdkw_targetalienadmin.id,tdkw_org_year,tdkw_targetalientry,tdkw_targetalientry.tdkw_alignmentstatus," +
                    "tdkw_targetalientry.tdkw_sourceadmin,tdkw_targetalientry.tdkw_findctid,tdkw_targetalientry.tdkw_findctid.tdkw_description,tdkw_targetalientry.tdkw_alignmentid," +
                    "tdkw_targetalientry.tdkw_findctid_name,tdkw_targetalientry.tdkw_findcttype,tdkw_targetalientry.tdkw_findcttype.id,tdkw_targetalientry.tdkw_findctdesc,tdkw_targetalientry.tdkw_decomposedesc," +
                    "tdkw_targetalientry.tdkw_decomposebillid";
            DynamicObject[] targetAlignmentsBusiness = BusinessDataServiceHelper.load("tdkw_target_alignment", properties, alignmentFilter.toArray());

            logger.info("targetAlignments:{}", targetAlignments);


//            Map<String, List<DynamicObject>> allMap = Arrays.stream(targetAlignments).collect(Collectors.groupingBy(o -> o.getString("tdkw_targetalientry.tdkw_sourceadmin") + o.getString("tdkw_targetalientry.tdkw_findctid.id")));

            //如果没有已对齐的 只展示自己的指标
            if (targetAlignments == null || targetAlignments.size() == 0) {
                for (DynamicObject orgPerfMetric : orgPerfMetrics) {
                    //TODO 第二层指标 -无论分解与否
                    OrgTargetVO orgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), orgPerfMetric, true, lightRule);
                    targetVOList.add(orgTargetVO);
                }
            } else {

                //TODO 更新对齐
                updateMultiDate(year, orgIdList, targetAlignmentsBusiness);

                //通过 指标来源+ 指标名称 ->多个 多选基础资料指标 + 对齐组织
                //指标来源 +指标id -》单据体id
                //单据体id-> 多选基础资料
                //单据体id-> 对齐组织

                //指标来源 +指标id -》单据体id
                Map<String, List<Long>> adminIdTargetIdEntryIdMap = Arrays.stream(targetAlignmentsBusiness)
                        .flatMap(dynamicObject -> dynamicObject.getDynamicObjectCollection("tdkw_targetalientry")
                                .stream())
                        .filter(entry -> StringUtils.equals(entry.getString("tdkw_alignmentstatus"), "10"))
                        .collect(Collectors.groupingBy(
                                entry -> entry.getString("tdkw_sourceadmin.id") + "," + entry.getString("tdkw_findctid.id"),
                                Collectors.mapping(
                                        entry -> entry.getLong("id"),
                                        Collectors.toList()
                                )
                        ));
                logger.info("key为指标来源id+指标id,value为单据体id,adminIdTargetIdEntryIdMap:{}", adminIdTargetIdEntryIdMap);

                //单据体id-> 多选基础资料 已对齐指标id
                Map<Long, DynamicObjectCollection> entryIdAlignmentTargetIdMap = Arrays.stream(targetAlignmentsBusiness)
                        .flatMap(dynamicObject -> dynamicObject.getDynamicObjectCollection("tdkw_targetalientry")
                                .stream()
                                .filter(entry -> StringUtils.equals(entry.getString("tdkw_alignmentstatus"), "10"))
                                .map(item -> new AbstractMap.SimpleEntry<>(item, dynamicObject)))
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().getLong("id"),
                                entry -> entry.getKey().getDynamicObjectCollection("tdkw_alignmentid")
                        ));
                logger.info("key为单据体id,value为多选基础资料 已对齐指标id,entryIdAlignmentTargetIdMap:{}", entryIdAlignmentTargetIdMap);

                //单据体id-> 对齐组织id(下级组织id)
                Map<Long, Long> entryIdAlignmentOrgIdMap = Arrays.stream(targetAlignmentsBusiness)
                        .flatMap(dynamicObject -> dynamicObject.getDynamicObjectCollection("tdkw_targetalientry")
                                .stream()
                                .filter(entry -> StringUtils.equals(entry.getString("tdkw_alignmentstatus"), "10"))
                                .map(item -> new AbstractMap.SimpleEntry<>(item, dynamicObject)))
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().getLong("id"),
                                entry -> entry.getValue().getLong("tdkw_targetalienadmin.id")
                        ));
                logger.info("key为单据体id,对齐组织id(下级组织id),entryIdAlignmentOrgIdMap:{}", entryIdAlignmentOrgIdMap);

                for (Entry<String, List<Long>> stringListEntry : adminIdTargetIdEntryIdMap.entrySet()) {
                    //指标来源 +指标id -》单据体id
                    String key = stringListEntry.getKey();
                    logger.info("key为指标来源id+指标id,stringListEntry.getKey():{}", key);
                    String everyOrgId = StringUtils.substringBefore(key, ",");
                    if (!StringUtils.equals(everyOrgId, String.valueOf(orgId))) {
                        continue;
                    }
                    DynamicObject orgMetrics = targetDetailMap.get(key);
                    if (orgMetrics == null) {
                        continue;
                    }
                    //父组织下面的指标
                    OrgTargetVO orgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), orgMetrics, true, lightRule);
                    targetVOList.add(orgTargetVO);
                    //指标下面的指标
                    ArrayList<OrgTargetVO> juniorTargetVOList = new ArrayList<>();
                    orgTargetVO.setChildren(juniorTargetVOList);

                    //单据体id
                    for (Long entryId : stringListEntry.getValue()) {
                        logger.info("value为单据体id,stringListEntry.getValue():{}", entryId);
                        //已对齐组织id
                        Long alignmentOrgId = entryIdAlignmentOrgIdMap.get(entryId);
                        if (alignmentOrgId == 0L) {
                            continue;
                        }
                        for (DynamicObject alignmentTarget : entryIdAlignmentTargetIdMap.get(entryId)) {
                            //已对齐指标id
                            long targetId = alignmentTarget.getLong("fbasedataid.id");
                            //已对齐组织id +"," + 已对齐指标id
                            String mapKey = alignmentOrgId + "," + targetId;
                            logger.info("已对齐组织id  + 已对齐指标id,mapKey:{}", mapKey);
                            //拿到组织绩效指标详情
                            DynamicObject juniorOrgMetrics = juniorTargetDetailMap.get(mapKey);
                            if (juniorOrgMetrics == null) {
                                continue;
                            }
                            OrgTargetVO assignmentOrgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), juniorOrgMetrics, true, lightRule);
                            juniorTargetVOList.add(assignmentOrgTargetVO);
                            //TODO 添加 下级组织 OrgTargetVO 第三层
                            addTargetList(mapKey, adminIdTargetIdEntryIdMap, assignmentOrgTargetVO, juniorTargetDetailMap, entryIdAlignmentOrgIdMap, entryIdAlignmentTargetIdMap, lightRule);
                        }
                    }
                }
                //如果遍历上面的都没有，把自己的加上
                if (targetVOList.size() == 0) {
                    for (DynamicObject orgPerfMetric : orgPerfMetrics) {
                        //TODO 第二层指标 -无论分解与否
                        OrgTargetVO orgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), orgPerfMetric, true, lightRule);
                        targetVOList.add(orgTargetVO);
                    }
                } else {
                    for (DynamicObject orgPerfMetric : orgPerfMetrics) {
                        //TODO 第二层指标 -没有对齐的
                        //组织id+指标id
                        String orgIdTargetId = orgPerfMetric.getString("tdkw_adminorg.id") + "," + orgPerfMetric.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id");
                        if (!adminIdTargetIdEntryIdMap.containsKey(orgIdTargetId)) {
                            OrgTargetVO orgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), orgPerfMetric, true, lightRule);
                            targetVOList.add(orgTargetVO);
                        }

                    }
                }
            }
        }

        return orgHeaderVO;

    }

    private void updateDate(DynamicObject alignment, Date year, Long orgId) {
        DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
        List<Long> alignmentTargetList = dynamicObjectCollection.stream().map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());

        LocalDate localDate = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int assessYear = localDate.getYear();


        //上级分解给本组织的数据
        DynamicObjectCollection query = QueryServiceHelper.query("tdkw_target_decompose", "id,tdkw_parentadminorg as orgid,tdkw_entryentity.tdkw_target.id as targetid," +
                        "tdkw_entryentity.tdkw_target.name as targetname,tdkw_entryentity.tdkw_target.tdkw_description as targetdesc,tdkw_entryentity.tdkw_target.tdkw_metric_type.id as typeid," +
                        "tdkw_entryentity.tdkw_subentryentity.tdkw_decomposedesc as decomposedesc",
                new QFilter[]{new QFilter("YEAR(tdkw_org_year)", QCP.equals, assessYear).and("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.equals, orgId)});
        List<Long> decomposeTargetList = query.stream().map(i -> i.getLong("targetid")).collect(Collectors.toList());

        //交集
        Collection<Long> intersection = CollectionUtils.intersection(decomposeTargetList, alignmentTargetList);
        //需要删除的集合
        Collection<Long> addCollection = CollectionUtils.subtract(decomposeTargetList, intersection);
        //需要增加的集合
        Collection<Long> delCollection = CollectionUtils.subtract(alignmentTargetList, intersection);

        for (int i = dynamicObjectCollection.size() - 1; i >= 0; i--) {
            DynamicObject row = dynamicObjectCollection.get(i);
            long targetId = row.getLong("tdkw_findctid.id");
            if (delCollection.contains(targetId)) {
                dynamicObjectCollection.remove(i);
            }
        }

        for (DynamicObject dynamicObject : query) {
            if (addCollection.contains(dynamicObject.getLong("targetid"))) {
                DynamicObject newEntryRow = dynamicObjectCollection.addNew();
                newEntryRow.set("tdkw_decomposebillid", dynamicObject.getLong("id"));
                newEntryRow.set("tdkw_sourceadmin", dynamicObject.getLong("orgid"));
                newEntryRow.set("tdkw_findctid", dynamicObject.getLong("targetid"));
                newEntryRow.set("tdkw_findctid_name", dynamicObject.getString("targetname"));
                newEntryRow.set("tdkw_findcttype", dynamicObject.getLong("typeid"));
                newEntryRow.set("tdkw_findctdesc", dynamicObject.getString("targetdesc"));
                newEntryRow.set("tdkw_decomposedesc", dynamicObject.getString("decomposedesc"));
                newEntryRow.set("tdkw_alignmentstatus", "20");
            }
        }
        SaveServiceHelper.save(new DynamicObject[]{alignment});

    }



    private void updateMultiDate(Date year, List<Long> juniorOrgIdList, DynamicObject[] targetAlignmentsBusiness) {
        LocalDate localDate = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int assessYear = localDate.getYear();


        //通过本组织id 查找分解给我的    上级指标id - alignmentTargetList
        //查询指标分解
        DynamicObjectCollection targetDecompose = QueryServiceHelper.query("tdkw_target_decompose", "id,tdkw_parentadminorg as orgid,tdkw_entryentity.tdkw_target.id as targetid," +
                        "tdkw_entryentity.tdkw_target.name as targetname,tdkw_entryentity.tdkw_target.tdkw_description as targetdesc,tdkw_entryentity.tdkw_target.tdkw_metric_type.id as typeid," +
                        "tdkw_entryentity.tdkw_subentryentity.tdkw_decomposedesc as decomposedesc,tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id",
                new QFilter[]{new QFilter("YEAR(tdkw_org_year)", QCP.equals, assessYear).and("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.in, juniorOrgIdList)});

        //对应原本的 decomposeTargetList  组织id -指标id
        Map<Long, List<Long>> orgIdSuperTargetIdDecomposeMap = targetDecompose.stream().collect(Collectors.groupingBy(o -> o.getLong("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id"),
                Collectors.mapping(
                        o -> o.getLong("targetid"),
                        Collectors.toList()
                )));

        //分解组织id -List<DynamicObject>  对应原本的query(DynamicObjectCollection)
        Map<Long, List<DynamicObject>> decomposeIdDynamicObjectList = targetDecompose.stream().collect(Collectors.groupingBy(o -> o.getLong("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id")));


        for (DynamicObject alignment : targetAlignmentsBusiness) {
           //对齐单据体
            DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
            List<Long> alignmentTargetList = dynamicObjectCollection.stream().map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());

            List<Long> decomposeTargetList = orgIdSuperTargetIdDecomposeMap.get(alignment.getLong("tdkw_targetalienadmin.id"));

            if (StringUtils.isBlank(decomposeTargetList)){
                dynamicObjectCollection.clear();
                SaveServiceHelper.save(new DynamicObject[]{alignment});
            }else {
                //交集
                Collection<Long> intersection = CollectionUtils.intersection(decomposeTargetList, alignmentTargetList);
                //需要增加的集合
                Collection<Long> addCollection = CollectionUtils.subtract(decomposeTargetList, intersection);
                //需要删除的集合
                Collection<Long> delCollection = CollectionUtils.subtract(alignmentTargetList, intersection);

                for (int i = dynamicObjectCollection.size() - 1; i >= 0; i--) {
                    DynamicObject row = dynamicObjectCollection.get(i);
                    long targetId = row.getLong("tdkw_findctid.id");
                    if (delCollection.contains(targetId)) {
                        dynamicObjectCollection.remove(i);
                    }
                }
                List<DynamicObject> query = decomposeIdDynamicObjectList.get(alignment.getLong("tdkw_targetalienadmin.id"));

                for (DynamicObject dynamicObject : query) {
                    if (addCollection.contains(dynamicObject.getLong("targetid"))) {
                        DynamicObject newEntryRow = dynamicObjectCollection.addNew();
                        newEntryRow.set("tdkw_decomposebillid", dynamicObject.getLong("id"));
                        newEntryRow.set("tdkw_sourceadmin", dynamicObject.getLong("orgid"));
                        newEntryRow.set("tdkw_findctid", dynamicObject.getLong("targetid"));
                        newEntryRow.set("tdkw_findctid_name", dynamicObject.getString("targetname"));
                        newEntryRow.set("tdkw_findcttype", dynamicObject.getLong("typeid"));
                        newEntryRow.set("tdkw_findctdesc", dynamicObject.getString("targetdesc"));
                        newEntryRow.set("tdkw_decomposedesc", dynamicObject.getString("decomposedesc"));
                        newEntryRow.set("tdkw_alignmentstatus", "20");
                    }
                }
            }


        }
        SaveServiceHelper.save(targetAlignmentsBusiness);

    }

    @Override
    public void queryImportBasedata(QueryImportBasedataEventArgs e) {
        super.queryImportBasedata(e);
    }

    private void addTargetList(String superKey, Map<String, List<Long>> adminIdTargetIdEntryIdMap, OrgTargetVO orgTargetVO, Map<String, DynamicObject> juniorTargetDetailMap, Map<Long, Long> entryIdAlignmentOrgIdMap, Map<Long, DynamicObjectCollection> entryIdAlignmentTargetIdMap, DynamicObject lightRule) {
        ArrayList<OrgTargetVO> juniorTargetVOList = new ArrayList<>();
        orgTargetVO.setChildren(juniorTargetVOList);
        for (Entry<String, List<Long>> stringListEntry : adminIdTargetIdEntryIdMap.entrySet()) {
            //指标来源 +指标id -》单据体id
            String key = stringListEntry.getKey();
            if (!StringUtils.equals(key, String.valueOf(superKey))) {
                continue;
            }
            //单据体id
            for (Long entryId : stringListEntry.getValue()) {
                //已对齐组织id
                Long alignmentOrgId = entryIdAlignmentOrgIdMap.get(entryId);
                if (alignmentOrgId == 0L) {
                    continue;
                }
                for (DynamicObject alignmentTarget : entryIdAlignmentTargetIdMap.get(entryId)) {
                    //已对齐指标id
                    long targetId = alignmentTarget.getLong("fbasedataid.id");
                    //已对齐组织id +"," + 已对齐指标id
                    String mapKey = alignmentOrgId + "," + targetId;
                    //拿到组织绩效指标详情
                    DynamicObject juniorOrgMetrics = juniorTargetDetailMap.get(mapKey);
                    if (juniorOrgMetrics == null) {
                        continue;
                    }
                    OrgTargetVO assignmentOrgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), juniorOrgMetrics, true, lightRule);
                    juniorTargetVOList.add(assignmentOrgTargetVO);
                    //TODO 添加 下级组织 OrgTargetVO 第三层
                    addTargetList(mapKey, adminIdTargetIdEntryIdMap, assignmentOrgTargetVO, juniorTargetDetailMap, entryIdAlignmentOrgIdMap, entryIdAlignmentTargetIdMap, lightRule);
                }
            }
        }
    }


    /**
     * 给orgHeaderVO赋值
     *
     * @param orgHeaderVO    头部VO
     * @param orgPerfMetrics 组织绩效指标
     * @return OrgHeaderVO 头部
     */
    private OrgHeaderVO assignmentOrgHeaderVO(OrgHeaderVO orgHeaderVO, @NotNull DynamicObject orgPerfMetrics, DynamicObject lightRule) {
        orgHeaderVO.setId(orgPerfMetrics.getString("tdkw_adminorg.id"));
        //组织名称
        orgHeaderVO.setOrgName(orgPerfMetrics.getString("tdkw_adminorg.name"));
        //行政组织编码
        orgHeaderVO.setOrgNumber(orgPerfMetrics.getString("tdkw_adminorg.number"));
        //评估得分
        orgHeaderVO.setOrgScore(orgPerfMetrics.getBigDecimal("tdkw_eval_res_list"));
        //标签

        TagVO tagVO = new TagVO();
        //考核年份
        tagVO.setYear(DateToString(orgPerfMetrics.getDate("tdkw_assess_year")));
        orgHeaderVO.setTagVO(tagVO);


        if (lightRule == null) {
            //TODO 没有亮灯规则 不显示灯
            orgHeaderVO.setLight("null");
        } else {
            //亮灯颜色
            BigDecimal redBig = lightRule.getBigDecimal("tdkw_integerredbig");
            BigDecimal yellowBig = lightRule.getBigDecimal("tdkw_integeryellowbig");

            BigDecimal lastScore = orgPerfMetrics.getBigDecimal("tdkw_eval_res_list");
            if (lastScore != null && lastScore.compareTo(new BigDecimal(0)) > 0) {

                int redCompare = lastScore.compareTo(redBig);
                int yellowCompare = lastScore.compareTo(yellowBig);
                if (redCompare < 0 || redCompare == 0) {
                    //比红灯小 或者等于都为红灯
                    orgHeaderVO.setLight("red");

                } else {
                    if (yellowCompare < 0 || yellowCompare == 0) {
                        //小于等于黄灯
                        orgHeaderVO.setLight("yellow");
                    } else {
                        orgHeaderVO.setLight("green");

                    }

                }
            } else {
                orgHeaderVO.setLight("gray");
            }

        }

        return orgHeaderVO;
    }

    /**
     * 给OrgTargetVO赋值
     *
     * @param orgTargetVO 标签VO
     * @param orgMetrics  组织绩效指标 orgMetrics为空会报错
     * @param isShow      针对指标对齐 父指标是否展示
     * @return OrgTargetVO 标签
     */
    private OrgTargetVO assignmentOrgTargetVO(OrgTargetVO orgTargetVO, @NotNull DynamicObject orgMetrics, boolean isShow, DynamicObject lightRule) {
        logger.info("orgMetrics:{},isShow:{}",orgMetrics,isShow);

        String targetNumber = orgMetrics.getString("tdkw_hideentry.tdkw_area_type.number");
        if (StringUtils.equals(targetNumber, "T0001") || StringUtils.equals(targetNumber, "T0002") ) {
            //唯一id
            orgTargetVO.setId(orgMetrics.getString("tdkw_adminorg.id") + "," + orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id"));
            //指标id
            orgTargetVO.setTargetId(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id"));
            //指标名称
            orgTargetVO.setTargetName(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric_str"));

        } else if (StringUtils.equals(targetNumber, "T0003") || StringUtils.equals(targetNumber, "T0004")) {
            //唯一id
            orgTargetVO.setId(orgMetrics.getString("tdkw_adminorg.id") + "," + UUID.randomUUID() + "," + orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric_str"));
            //指标名称
            orgTargetVO.setTargetName(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric_str"));

        }
        //指标描述
        orgTargetVO.setTargetDescription(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric_desc"));


        //指标类型
        orgTargetVO.setTargetType(orgMetrics.getString("tdkw_hideentry.tdkw_area_type.name"));
        //单位
        String unit = "";
        if (StringUtils.isNotBlank(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_unit"))) {
            switch (orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_unit")) {
                case "yuan":
                    unit = "元";
                    break;
                case "wan_yuan":
                    unit = "万元";
                    break;
                case "bai_wan_yuan":
                    unit = "百万元";
                    break;
                case "qian_wan_yuan":
                    unit = "千万元";
                    break;
                case "percent":
                    unit = "百分比";
                    break;
                default:
                    break;
            }
        }
        //目标值
        BigDecimal targetValue = orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_target_value");
        //权重
        String weight = orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_weight");
        //年度完成值
        BigDecimal annualValue = orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_annual_val");
        //评估结果
        BigDecimal evalRes = orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_eval_res");

        logger.info("目标值:{},权重:{},年度完成值:{},评估结果:{},unit:{},isShow:{}",targetValue,weight,annualValue,evalRes,unit,isShow);
        //目标值 +单位
        orgTargetVO.setGoalValue(judgeEmpty(targetValue, unit, isShow));
        //权重
        orgTargetVO.setWeight(judgeEmpty(weight,"%", isShow));
        //年度完成值 +单位
        orgTargetVO.setAnnualValue(judgeEmpty(annualValue, unit, isShow));
        //指标分数  评估结果
        orgTargetVO.setTargetScore(judgeEmpty(evalRes, isShow));

        //标签
        TagVO tagVO = new TagVO();
        //标签-年份
        tagVO.setYear(DateToString(orgMetrics.getDate("tdkw_assess_year")));
        //标签-指标类型名称
        tagVO.setTargetTypeName(orgMetrics.getString("tdkw_hideentry.tdkw_area_type.name"));
        //标签-组织名称
        tagVO.setOrgName(orgMetrics.getString("tdkw_adminorg.name"));
        orgTargetVO.setTagVO(tagVO);

        //亮灯
        LightingDetailsVO lightingDetailsVO = new LightingDetailsVO();
        lightingDetailsVO.setTargetNumber(targetNumber);
        //亮灯-指标名称
        lightingDetailsVO.setTargetName(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric_str"));
        //亮灯-指标描述
        lightingDetailsVO.setTargetDescription(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_metric_desc"));
        //亮灯-指标评分标准
        lightingDetailsVO.setTargetScoreStandard(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_rating_standard"));
        //亮灯-Q1完成情况
        lightingDetailsVO.setFirstCompletion(orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q1_completion") == null ? "0" : orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q1_completion") + unit);
        //亮灯-Q2完成情况
        lightingDetailsVO.setSecondCompletion(orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q2_completion") == null ? "0" : orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q2_completion") + unit);
        //亮灯-Q3完成情况
        lightingDetailsVO.setThirdCompletion(orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q3_completion") == null ? "0" : orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q3_completion") + unit);
        //亮灯-Q4完成情况
        lightingDetailsVO.setFourthCompletion(orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q4_completion") == null ? "0" : orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_q4_completion") + unit);
        //亮灯-年度完成情况  ->中心级
        lightingDetailsVO.setYearCompletion(orgMetrics.getString("tdkw_hideentry.tdkw_subentryentity.tdkw_ctr_annual_val"));
        orgTargetVO.setLightingDetailsVO(lightingDetailsVO);

        //亮灯颜色
        if (lightRule == null || !isShow) {
            //TODO 没有亮灯规则 或者不可见 则不显示灯
            orgTargetVO.setLight("");
            orgTargetVO.setLightingDetailsVO(null);

        } else {
            //没填数值就是 尚未维护完成情况
            if (orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_eval_res").compareTo(new BigDecimal(0)) > 0) {


                BigDecimal redBig = lightRule.getBigDecimal("tdkw_integerredbig");
                BigDecimal yellowBig = lightRule.getBigDecimal("tdkw_integeryellowbig");

                BigDecimal lastScore = orgMetrics.getBigDecimal("tdkw_hideentry.tdkw_subentryentity.tdkw_eval_res");
                int redCompare = lastScore.compareTo(redBig);
                int yellowCompare = lastScore.compareTo(yellowBig);
                if (redCompare < 0 || redCompare == 0) {
                    //比红灯小 或者等于都为红灯
                    orgTargetVO.setLight("red");

                } else {
                    if (yellowCompare < 0 || yellowCompare == 0) {
                        //小于等于黄灯
                        orgTargetVO.setLight("yellow");
                    } else {
                        orgTargetVO.setLight("green");
                    }

                }


            } else {
                orgTargetVO.setLight("gray");
            }
        }


        return orgTargetVO;
    }

    private String judgeEmpty(BigDecimal value, String unit, boolean isShow) {
        logger.info("judgeEmpty,value:{},unit:{},isShow:{}",value,unit,isShow);
        if (StringUtils.isBlank(value) || StringUtils.isBlank(unit) || !isShow || value.compareTo(BigDecimal.ZERO) == 0 || StringUtils.equals("null", unit)) {
            return "/";
        } else {
            return value + unit;
        }
    }


    /**
     * 如果是空的话。或者不展示，返回/
     *
     * @param value
     * @param isShow
     * @return
     */
    private String judgeEmpty(BigDecimal value, boolean isShow) {
        if (StringUtils.isBlank(value) || !isShow || value.compareTo(BigDecimal.ZERO) == 0) {
            return "/";
        } else {
            return value.toString();
        }
    }

    private String judgeEmpty(String value, boolean isShow) {
        if (StringUtils.isBlank(value) || !isShow || StringUtils.equals("null", value)) {
            return "/";
        } else {
            return value;
        }
    }

    private String judgeEmpty(String value, String unit, boolean isShow) {
        if (StringUtils.isBlank(value) || StringUtils.isBlank(unit) || !isShow || StringUtils.equals("null", value) || StringUtils.equals("null", unit)) {
            return "/";
        } else {
            return value + unit;
        }
    }


    /**
     * 指标对齐
     *
     * @param orgId            组织id
     * @param year             年份
     * @param targetTypeNumber 指标类型编码
     */
    public OrgHeaderVO performanceAlignment(long orgId, Date year, String targetTypeNumber, DynamicObject lightRule) {
        logger.info("指标对齐入参orgId:{},year:{},targetTypeNumber:{}", orgId, year, targetTypeNumber);

        QFilter nowFilter = addFilter(Collections.singletonList(orgId), year, targetTypeNumber);
        //查询到本级组织的指标
        String selectProperties = "tdkw_adminorg.id,tdkw_adminorg.name,tdkw_adminorg.number,tdkw_assess_year,tdkw_eval_res_list,tdkw_hideentry.tdkw_area_type.number,tdkw_hideentry.tdkw_area_type.name," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_superorg,tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id,tdkw_hideentry.tdkw_subentryentity.tdkw_metric.name," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_metric.tdkw_description,tdkw_hideentry.tdkw_subentryentity.tdkw_metric.tdkw_rating_standard," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_metric.tdkw_metric_type.number,tdkw_hideentry.tdkw_subentryentity.tdkw_weight," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_metric_str,tdkw_hideentry.tdkw_subentryentity.tdkw_metric_desc,tdkw_hideentry.tdkw_subentryentity.tdkw_rating_standard," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_target_value,tdkw_hideentry.tdkw_subentryentity.tdkw_unit," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_q1_completion," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_q2_completion,tdkw_hideentry.tdkw_subentryentity.tdkw_q3_completion," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_q4_completion,tdkw_hideentry.tdkw_subentryentity.tdkw_annual_val," +
                "tdkw_hideentry.tdkw_subentryentity.tdkw_ctr_annual_val,tdkw_hideentry.tdkw_subentryentity.tdkw_eval_res";
        DynamicObjectCollection orgPerfMetrics = QueryServiceHelper.query("tdkw_org_perf_metrics", selectProperties, nowFilter.toArray());
        logger.info("orgPerfMetrics:{}", orgPerfMetrics);
        if (orgPerfMetrics == null || orgPerfMetrics.size() == 0) {
            return null;
        }


        //是否有上级
        boolean haveSuper = false;
        //父指标 -是否展示 map
        Map<Long, Boolean> superTargetIsShowMap = new HashMap<>();
        DynamicObjectCollection superOrgPerfMetrics = null;
        //父指标 -已对齐子指标 map
        Map<Long, DynamicObjectCollection> superTargetJuniorTargetsMap = null;
        //父指标 组织绩效指标
        Map<Long, DynamicObject> superTargetIdPerformanceMap = new HashMap<>();
        if (StringUtils.equals("T0001", targetTypeNumber)) {
            DynamicObject orgDynamicObject = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "parent.id", new QFilter("id", QCP.equals, orgId).toArray());
            //上级 kpi

            long superOrgId = orgDynamicObject.getLong("parent.id");
            //有上级
            if (superOrgId != 0L) {
                QFilter superiorFilter = addFilter(Collections.singletonList(superOrgId), year, targetTypeNumber);
                superOrgPerfMetrics = QueryServiceHelper.query("tdkw_org_perf_metrics", selectProperties, superiorFilter.toArray());
                logger.info("上级组织绩效指标 superOrgPerfMetrics:{}", superOrgPerfMetrics);
            }
            if (superOrgPerfMetrics != null && superOrgPerfMetrics.size() > 0) {
                //上级组织指标id, 组织绩效指标
                superTargetIdPerformanceMap = superOrgPerfMetrics.stream().collect(Collectors.toMap(o -> o.getLong("tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id"), o -> o));

                logger.info("key:上级组织指标id, value: 组织绩效指标 ,superTargetIdPerformanceMap:{}", superTargetIdPerformanceMap);
                //有上级
                //1.查询 指标对齐的 上下级对应关系 有对齐才显示上级指标
                //需对齐组织
                QFilter alignmentFilter = new QFilter("tdkw_targetalienadmin", QCP.equals, orgId);
                //考核年份
                LocalDate localDate = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int assessYear = localDate.getYear();

                alignmentFilter.and("YEAR(tdkw_org_year)", QCP.equals, assessYear);
                //单据体的指标来源
                alignmentFilter.and("tdkw_targetalientry.tdkw_sourceadmin.id", QCP.equals, superOrgId);
                //单据体的对齐状态
                alignmentFilter.and("tdkw_targetalientry.tdkw_alignmentstatus", QCP.equals, "10");
                DynamicObject targetAlignment = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment", "tdkw_targetalienadmin,tdkw_org_year," +
                        "tdkw_targetalientry,tdkw_targetalientry.tdkw_sourceadmin.id,tdkw_targetalientry.tdkw_alignmentstatus,tdkw_targetalientry.tdkw_findctid," +
                        "tdkw_targetalientry.tdkw_findctid.id,tdkw_targetalientry.tdkw_alignmentid,tdkw_targetalientry.tdkw_decomposebillid," +
                        "tdkw_targetalientry.tdkw_findctid_name,tdkw_targetalientry.tdkw_findcttype.id,tdkw_targetalientry.tdkw_findctdesc", alignmentFilter.toArray());
                logger.info("targetAlignment:{}", targetAlignment);
                if (targetAlignment != null) {
                    //TODO 更新对齐
                    updateDate(targetAlignment, year, orgId);

                    DynamicObjectCollection targetAliEntry = targetAlignment.getDynamicObjectCollection("tdkw_targetalientry");
                    //key： 上级指标id   下级指标id   TODO query不能查多选基础资料
                    superTargetJuniorTargetsMap = targetAliEntry.stream().filter(o -> StringUtils.equals(o.getString("tdkw_alignmentstatus"), "10")).collect(Collectors.toMap(o -> o.getLong("tdkw_findctid.id"), o -> o.getDynamicObjectCollection("tdkw_alignmentid")));


                    //2.再查询指标分解 -->是否可见
                    QFilter decomposeFilter = new QFilter("tdkw_parentadminorg.id", QCP.equals, superOrgId);
                    decomposeFilter.and("YEAR(tdkw_org_year)", QCP.equals, assessYear);
                    decomposeFilter.and("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.equals, orgId);
                    String selectDecomposeProperties = "tdkw_parentadminorg.id,tdkw_org_year,tdkw_entryentity,tdkw_entryentity.tdkw_target.id,tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id,tdkw_entryentity.tdkw_subentryentity.tdkw_isshow";
                    DynamicObjectCollection targetDecompose = QueryServiceHelper.query("tdkw_target_decompose", selectDecomposeProperties, decomposeFilter.toArray());

                    //key : 父指标id value: isshow
                    superTargetIsShowMap = targetDecompose.stream().collect(Collectors.toMap(o -> o.getLong("tdkw_entryentity.tdkw_target.id"), o -> o.getBoolean("tdkw_entryentity.tdkw_subentryentity.tdkw_isshow")));

                    logger.info("key : 父指标id value: isshow,superTargetIsShowMap:{}", superTargetIsShowMap);
                    haveSuper = true;
                }
            }

        }
        //子指标id ->父指标id -> isShow
        //TODO 头部统一赋值
        OrgHeaderVO orgHeaderVO = assignmentOrgHeaderVO(new OrgHeaderVO(), orgPerfMetrics.get(0), lightRule);
        ArrayList<OrgTargetVO> targetVOList = new ArrayList<>();
        //头部添加 指标详情
        orgHeaderVO.setChildren(targetVOList);
        //TODO 直接带出 第二层 指标
        for (DynamicObject orgPerfMetric : orgPerfMetrics) {
            //一个kpi指标是一个对象
            OrgTargetVO orgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), orgPerfMetric, true, lightRule);
            targetVOList.add(orgTargetVO);

            //TODO 得在循环里面去给orgTargetVO 赋juniorTargetVOList
            ArrayList<OrgTargetVO> juniorTargetVOList = new ArrayList<>();
            //子指标详情 添加  父指标详情
            orgTargetVO.setChildren(juniorTargetVOList);
            //如果有父级
            logger.info("如果有父级,haveSuper:{},key:上级指标id,value:下级已对齐指标id,superTargetJuniorTargetsMap:{}", haveSuper, superTargetJuniorTargetsMap);
            if (haveSuper && superTargetJuniorTargetsMap != null) {
                //子指标id
                long juniorTargetId = orgPerfMetric.getLong("tdkw_hideentry.tdkw_subentryentity.tdkw_metric.id");
                //遍历 父指标-已对齐子指标 map
                for (Entry<Long, DynamicObjectCollection> entry : superTargetJuniorTargetsMap.entrySet()) {
                    for (DynamicObject juniorTargetObject : entry.getValue()) {
                        //多选基础资料 子指标id
                        Long juniorTargetObjectId = juniorTargetObject.getLong("fbasedataid.id");
                        logger.info("已对齐的子指标id juniorTargetObjectId:{},组织绩效指标 juniorTargetId:{}", juniorTargetObjectId, juniorTargetId);
                        if (!juniorTargetObjectId.equals(juniorTargetId)) {
                            continue;
                        }
                        //对齐的 父指标id
                        Long fatherTargetId = entry.getKey();
                        Boolean isShow = superTargetIsShowMap.get(fatherTargetId);
                        //父指标组织绩效
                        DynamicObject superPerformance = superTargetIdPerformanceMap.get(fatherTargetId);
                        if (superPerformance == null) {
                            continue;
                        }
                        OrgTargetVO superOrgTargetVO = assignmentOrgTargetVO(new OrgTargetVO(), superPerformance, isShow, lightRule);
                        juniorTargetVOList.add(superOrgTargetVO);

                    }
                }
            }
        }
        return orgHeaderVO;
    }

    private QFilter addFilter(List<Long> orgIdList, Date year, String targetTypeNumber) {
        QFilter qFilter = new QFilter("1", QCP.equals, 1);
        //组织id
        if (orgIdList.size() > 0) {
            qFilter.and("tdkw_adminorg.id", QCP.in, orgIdList);
        }
        //考核年份
        if (year != null) {
            LocalDate localDate = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int assessYear = localDate.getYear();

            qFilter.and("YEAR(tdkw_assess_year)", QCP.equals, assessYear);
        }
        //指标类型
        if (targetTypeNumber != null) {
            qFilter.and("tdkw_hideentry.tdkw_area_type.number", QCP.equals, targetTypeNumber);
        }
        return qFilter;
    }

    private QFilter addOrgFilter() {
        QFilter orgFilter = new QFilter("iscurrentversion", QCP.equals, "1");
        orgFilter.and("datastatus", QCP.equals, "1");
        orgFilter.and("status", QCP.equals, "C");
        return orgFilter;
    }

    /**
     * 任职经历
     *
     * @param personId hr人员id
     * @return QFilter 过滤器
     */
    private QFilter addEmFilter(long personId) {
        //是否当前版本 = 是
        QFilter filter = new QFilter("iscurrentversion", QCP.equals, "1");
        //生效状态 = 生效中
        filter.and("businessstatus", QCP.equals, "1");
        //数据版本状态 = 生效中
        filter.and("datastatus", QCP.equals, "1");
        //是否主任职 = 是
        filter.and("isprimary", QCP.equals, "1");
        filter.and("person.id", QCP.equals, personId);
        return filter;
    }


    private Date getThisYear(int year) {
        Calendar calendar = Calendar.getInstance();
        if (year == 0) {
            year = calendar.get(Calendar.YEAR); // 获取今年的年份
        }
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0); // 设置为今年的1月1日
        calendar.set(Calendar.MILLISECOND, 0); // 设置毫秒为0
        return calendar.getTime();
    }

    private String DateToString(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int year = instance.get(Calendar.YEAR);
        return String.valueOf(year);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        Object newValue = e.getChangeSet()[0].getNewValue();
        String name = e.getProperty().getName();
        if (StringUtils.equals("tdkw_orgid", name)) {

            QFilter lightFilter = new QFilter("enable", QCP.equals, "1");
            DynamicObject lightRule = BusinessDataServiceHelper.loadSingle("tdkw_light_rule", "tdkw_integerredbig,tdkw_integeryellowbig", lightFilter.toArray());

            CustomControl control = this.getControl("customcontrolap");
            Long orgId = Long.valueOf(newValue.toString());
            Date year = (Date) this.getModel().getValue("tdkw_date");
            String type = this.getModel().getValue("tdkw_targettypenumber").toString();

            OrgHeaderVO decompose = performanceDecompose(orgId, year, type, lightRule);
            OrgHeaderVO alignment = performanceAlignment(orgId, year, type, lightRule);
            OrgMapVO orgMapVO = new OrgMapVO();
            orgMapVO.setDecomposeHeaderVO(decompose);
            orgMapVO.setAlignmentHeaderVO(alignment);

            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("data","");
            jsonObject.put("data", orgMapVO);
            jsonObject.put("click", "update");
            jsonObject.put("time", System.currentTimeMillis());
            control.setData(jsonObject);
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if (StringUtils.equals("tdkw_refresh", itemKey)) {
            this.getView().getPageCache().put("isFirst", "");
            this.getView().updateView();
        } else if (StringUtils.equals("tdkw_close", itemKey)) {

            this.getView().getParentView().close();
            this.getView().close();
        }
    }
}
