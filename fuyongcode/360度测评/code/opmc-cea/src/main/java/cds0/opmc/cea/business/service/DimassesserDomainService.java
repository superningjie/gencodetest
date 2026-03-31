package cds0.opmc.cea.business.service;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.business.bo.AssessTaskBO;
import cds0.opmc.cea.business.bo.DimassesserBO;
import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.AssessTaskStatusEnum;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.util.CollectionUtils;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRObjectUtils;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;
import kd.opmc.pbs.business.external.hrpi.IHRPIPersonService;
import kd.opmc.pbs.business.external.hrpi.IHRPIWorkRoleService;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.*;

/**
 * 增加、删除、调整评测人
 */
public class DimassesserDomainService extends HRDataBaseEdit {
    private static final Log LOG = LogFactory.getLog(DimassesserDomainService.class);
    private static final AssessObjEntityService assessObjEntityService = AssessObjEntityService.getInstance();
    protected static final AssessTaskEntityService assessTaskEntityService = AssessTaskEntityService.getInstance();
    protected static final AssessTaskDomainService assessTaskDomainService = AssessTaskDomainService.getInstance();
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final AssObjScoItemInstEntityService ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE = AssObjScoItemInstEntityService.getInstance();
    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();
    private static final AssessFormEntityService ASSESS_FORM_ENTITY_SERVICE = AssessFormEntityService.getInstance();
    private static final AssessTaskDomainService ASSESS_TASK_DOMAIN_SERVICE = AssessTaskDomainService.getInstance();
    private static final EvalDimSettingEntityService EVAL_DIM_SETTING_ENTITY_SERVICE = EvalDimSettingEntityService.getInstance();
    private static final MessageService MESSAGE_SERVICE = MessageService.getInstance();

    public static DimassesserDomainService getInstance() {
        return ServiceFactory.getService(DimassesserDomainService.class);
    }


    /**
     * 更新测评人数据
     */
    public void updateAssessorData(List<DimassesserBO> dimassesserBOList) {
        if (dimassesserBOList.isEmpty()) {
            return;
        }
        dimassesserBOList.stream().forEach(dimassesserBO -> {
            List<DynamicObject> addList = new ArrayList<>();
            List<DynamicObject> delList = new ArrayList<>();
            List<DynamicObject> addForTaskList = new ArrayList<>();
            DynamicObject assessObj = assessObjEntityService.queryOne(dimassesserBO.getAssessObjId());
            DynamicObjectCollection dimAssessorEntrys = assessObj.getDynamicObjectCollection(ENTRYENTITY);//拿到每个测评对象下的分录数据
            // 库数据按维度分组
            Map<Long, List<DynamicObject>> dimAssessorEntryMap = dimAssessorEntrys.stream().collect(Collectors.groupingBy(DynamicObject -> DynamicObject.getLong("evaldim.id")));
            // 删除修改
            for (Long evaldim : dimAssessorEntryMap.keySet()) {//维度下的多条数据
                List<DynamicObject> dimAssessorList = dimAssessorEntryMap.get(evaldim);
                Map<Long, List<Map<Long, BigDecimal>>> evalDimAssesserDimweight = dimassesserBO.getEvalDimAssesserDimweight();//参数--维度上的测评人
                List<Map<Long, BigDecimal>> dimAssessorParamList = evalDimAssesserDimweight.get(evaldim);
                if (dimAssessorParamList.isEmpty()) {// null --> 该维度上的测评人已经被删除完全
                    delList = dimAssessorEntrys.stream().filter(dimAssessorEntry -> dimAssessorEntry.getLong("evaldim.id") == evaldim).collect(Collectors.toList());
                } else {
                    for (Map<Long, BigDecimal> dimAssessorParam : dimAssessorParamList) {
                        for (Long assesserParam : dimAssessorParam.keySet()) {
                            Map<Long, DynamicObject> sqlData = dimAssessorList.stream().collect(Collectors.toMap(k -> k.getLong("assesser.id"), v -> v));
                            boolean flag = sqlData.containsKey(assesserParam);
                            if (dimAssessorList.size() > dimAssessorParamList.size()) {
                                //找出被删除的数据 不存在于参数的数据
                                for (Long i : sqlData.keySet()) {
                                    if (!dimAssessorParam.containsKey(i)) {
                                        //待删除数据
                                        delList = dimAssessorEntrys.stream().filter(dimAssessorEntry -> dimAssessorEntry.getLong("assesser.id") == i
                                                && dimAssessorEntry.getLong("evaldim.id") == evaldim).collect(Collectors.toList());
                                    }
                                }

                            } else if (!flag && (dimAssessorList.size() == dimAssessorParamList.size())) {//修改
                                //删除
                                for (Long i : sqlData.keySet()) {
                                    if (!dimAssessorParam.containsKey(i)) {
                                        //待删除数据
                                        delList = dimAssessorEntrys.stream().filter(dimAssessorEntry -> dimAssessorEntry.getLong("assesser.id") == i
                                                && dimAssessorEntry.getLong("evaldim.id") == evaldim).collect(Collectors.toList());
                                    }
                                }
                                //待新增的数据
                                DynamicObject dimAssessor = dimAssessorEntrys.addNew();
                                dimAssessor.set(ASSESSER, assesserParam);
                                dimAssessor.set(EVALDIM, evaldim);
                                dimAssessor.set(DIMWEIGHT, dimAssessorParam.get(assesserParam));
                                addList.add(assessObj);

                            } else if (!flag && (dimAssessorList.size() < dimAssessorParamList.size())) {//新增
                                DynamicObject dimAssessor = dimAssessorEntrys.addNew();
                                dimAssessor.set(ASSESSER, assesserParam);
                                dimAssessor.set(EVALDIM, evaldim);
                                dimAssessor.set(DIMWEIGHT, dimAssessorParam.get(assesserParam));
                                addForTaskList.add(dimAssessor);
                                addList.add(assessObj);
                            }
                            return;
                        }
                    }
                }
            }
            if (!delList.isEmpty()) { //删除
                // 待删除数据id
                List<Long> assesserIds = delList.stream().map(assessor -> assessor.getLong(ID)).collect(Collectors.toList());
                // 删除分录下测评人数据
                dimAssessorEntrys.removeAll(delList);
                // 设置任务失效状态
                DynamicObject[] tasks = assessTaskEntityService.queryAssessTaskIds(assesserIds);
                for (DynamicObject task : tasks) {
                    task.set("assestaskstatus", AssessTaskStatusEnum.EXPRIED.getValue());
                }
                assessTaskEntityService.update(tasks);
                assessObjEntityService.saveOne(assessObj);
            }
            if (!addList.isEmpty()) { //新增
                assessObjEntityService.saveOne(assessObj);
                //生成任务
                createAssessTask(addForTaskList, dimassesserBO);
            }
        });
    }

    /**
     * 调整测评人
     *
     * @param dimassesserBOList
     */
    public void adjustDimAssesser(List<DimassesserBO> dimassesserBOList, String fromWhere) {
        List<Long> invalidAssessTaskAsserIdList = new ArrayList<Long>();
        Set<Long> evalDimIdSet = new HashSet<>();
        Map<Long, List<DynamicObject>> toAddAsserMap = new HashMap<Long, List<DynamicObject>>();
        List<Long> assObjList = dimassesserBOList.stream().map(bo -> bo.getAssessObjId()).collect(Collectors.toList());
        Map<Long, DimassesserBO> assObjIdBoMap = dimassesserBOList.stream().collect(Collectors.toMap(k -> k.getAssessObjId(), v -> v));
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(assObjList);
        Arrays.stream(assessObjs).forEach(assObj -> {
            evalDimIdSet.addAll(assObj.getDynamicObjectCollection(ENTRYENTITY).stream().map(e -> e.getLong("evaldim.id")).collect(Collectors.toSet()));
        });
        Map<Long, DynamicObject> evalDimMap = Arrays.stream(EVAL_DIM_SETTING_ENTITY_SERVICE.loadDynamicObjectArray(evalDimIdSet.toArray())).collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
        Map<Long, DynamicObject> assessObjMap = Arrays.stream(assessObjs).collect(Collectors.toMap(k -> k.getLong(AppflgConstant.ID), V -> V));
        Arrays.stream(assessObjs).forEach(assobj -> {
            List<DynamicObject> deleteList = new ArrayList<DynamicObject>();
            List<DynamicObject> addList = new ArrayList<DynamicObject>();
            // 处理每个测评对象
            DynamicObjectCollection dimAssesserEntry = assobj.getDynamicObjectCollection(ENTRYENTITY);
            // 获取每个测评对象对应的参数数据（维度--》 维度下的维度测评人数据（测评人、权重））
            Map<Long, List<Map<Long, BigDecimal>>> evalDimAssesserMap = assObjIdBoMap.get(assobj.getLong(ID)).getEvalDimAssesserDimweight();
            // 库里该测评对象下的维度测评人数据 按维度分组
            Map<Long, List<DynamicObject>> evalDimAssesserDataMap = dimAssesserEntry.stream().collect(Collectors.groupingBy(k -> k.getLong("evaldim.id"), Collectors.toList()));
            evalDimAssesserDataMap.entrySet().forEach(entry -> {
                // 参数： 取当前维度下的维度测评人参数
                List<Map<Long, BigDecimal>> evalDimAssesserParam = evalDimAssesserMap.get(entry.getKey());
                //如果空的维度测评人数量大于 1
                if (evalDimAssesserParam != null) {
                    //可以设置多个 空 维度测评人
                    long countNullAssesser = evalDimAssesserParam.stream().filter(evalDimAssesser -> evalDimAssesser.keySet().iterator().next() == 0).count();
                    if (countNullAssesser > 1) {
                        long assesserId = 0L;
                        for (int i = 0; i < evalDimAssesserParam.size(); i++) {
                            BigDecimal value = evalDimAssesserParam.get(i).entrySet().iterator().next().getValue();
                            Long next = evalDimAssesserParam.get(i).keySet().iterator().next();
                            if (next == 0) {
                                Map<Long, BigDecimal> evalDimAsserMap = new HashMap<>();
                                evalDimAsserMap.put(assesserId - i - 1, value);
                                evalDimAssesserParam.add(evalDimAsserMap);
                            }
                        }
                        evalDimAssesserParam = evalDimAssesserParam.stream().filter(map -> map.keySet().iterator().next() != 0).collect(Collectors.toList());
                    }

                    // 参数： 测评人 --》 测评人数据映射 （测评人、权重）
                    Map<Long, Map<Long, BigDecimal>> evalDimAsserParmMap = evalDimAssesserParam.stream().collect(Collectors.toMap(k -> k.entrySet().iterator().next().getKey(), v -> v));
                    // 库里： 当前维度的维度测评人 测评人id 列表集合
                    List<Long> evalDimAssessList = entry.getValue().stream().map(e -> e.getLong("assesser.id")).collect(Collectors.toList());
                    // 当前维度下，参数过来的测评人 列表集合
                    List<Long> paramDimAsserList = evalDimAssesserParam.stream().map(e -> e.entrySet().iterator().next().getKey()).collect(Collectors.toList());
                    // 筛选当前测评对象、当前维度下 哪些 维度测评人需要更新、哪些需要删除
                    entry.getValue().stream().forEach(dataAsser -> {
                        if (paramDimAsserList.contains(dataAsser.getLong("assesser.id"))) {
                            // update
                            dataAsser.set("dimweight", evalDimAsserParmMap.get(dataAsser.getLong("assesser.id")).entrySet().iterator().next().getValue());
                        } else {
                            // delete
                            deleteList.add(dataAsser);
                        }
                    });
                    // 当前测评对象、当前维度下，需要新加进来的维度测评人
                    // 处理新增数据
                    evalDimAssesserParam.stream().filter(asserParam -> !evalDimAssessList.contains(asserParam.entrySet().iterator().next().getKey())).collect(Collectors.toList()).stream().forEach(newasser -> {
                        DynamicObject newAsser = dimAssesserEntry.addNew();
                        newAsser.set("dimweight", newasser.entrySet().iterator().next().getValue());
                        //可以设置多个 空 维度测评人
                        if (newasser.entrySet().iterator().next().getKey() < 0) {
                            newAsser.set("assesser", 0);
                        } else {
                            newAsser.set("assesser", newasser.entrySet().iterator().next().getKey());
                        }
                        newAsser.set("evaldim", entry.getKey());
                        newAsser.set("dimname", evalDimMap.get(entry.getKey()).getString("dimname"));
                        newAsser.set("assesstatus", DimAssesserStatusEnum.UNFILLED.getValue());
                        newAsser.set("groupdim", assobj.getLong("dimgroup.id"));
                        newAsser.set("assobj", assobj.getLong(ID));
                        addList.add(newAsser);
                    });
                } else {
                    // 当前参数中没有该维度的测评人数据，表示该测评对象的该维度的维度测评人全部删除
                    deleteList.addAll(entry.getValue());
                }
            });
            // 处理删除数据
            dimAssesserEntry.removeAll(deleteList);
            invalidAssessTaskAsserIdList.addAll(deleteList.stream().map(e -> e.getLong(ID)).collect(Collectors.toList()));
            toAddAsserMap.put(assobj.getLong(ID), addList);
        });
        TXHandle required = TX.required();
        try {
            // 失效掉任务
            assessTaskDomainService.invalidAssessTaskDimAssesser(invalidAssessTaskAsserIdList);
            // 删除维度测评人的指标打分实例数据
            ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.deleteScoreItemInsAssObj(invalidAssessTaskAsserIdList);
            // 更新测评对象 (删除维度测评人、更新维度测评人、新增维度测评人入库)
            ASSESS_OBJ_ENTITY_SERVICE.save(assessObjs);
            // 生成维度测评人的指标打分实例数据
            generateDimAssesserIndScoreInstData(toAddAsserMap);
            // 待启动，设置维度测评人不需要生成测评任务 发送消息
            if (!HRStringUtils.equals("toStartUp", fromWhere)) {
                // 是否生成维度测评人的测评任务，并发送消息
                List<DynamicObject> toSendAssessTasks = assessTaskCreate(assessObjs, toAddAsserMap);
                // 消息合并发送：先按照维度进行分组，然后同一维度中，再按照相同测评人进行分组
                toSendAssessTasks.stream().collect(Collectors.groupingBy(task -> task.getLong("evaldim"), Collectors.toList())).
                        values().stream().forEach(evalDimTask -> {
                            evalDimTask.stream().collect(Collectors.groupingBy(asserTask -> asserTask.getLong("assesserperson"), Collectors.toList())).
                                    values().stream().forEach(asserTaskSend -> {
                                        MESSAGE_SERVICE.sendAssessTaskMessage(asserTaskSend, assessObjMap.get(asserTaskSend.get(0).getLong("assessobj")));
                                    });
                                });
                /*toSendAssessTasks.stream().collect(Collectors.groupingBy(task -> task.getLong("assessobj"), Collectors.toList())).entrySet().stream().forEach(entry -> {
                    MESSAGE_SERVICE.sendAssessTaskMessage(entry.getValue(), assessObjMap.get(entry.getKey()));
                });*/
                //MESSAGE_SERVICE.sendAssessTaskMessage(toSendAssessTasks,assessObjs[HRBaseConstants.INT_ZERO]);
                assessTaskEntityService.save(toSendAssessTasks.stream().toArray(DynamicObject[]::new));
            }
            // ================ 流转接口调用  ====================
            /**
             * key:维度ID，value:该维度下的测评对象ID集合
             */
            Map<Long,Set<Long>> evalDimAssObjMap = new HashMap<>();
            // 先根据维度进行分组
            Map<Long,List<DynamicObject>> evalDimAsserMap = Arrays.stream(DIMASSESSER_ENTITY_SERVICE.queryDimAsserAssObj(Arrays.stream(assessObjs).map(e->e.getLong(ID)).collect(Collectors.toList()))).collect(Collectors.groupingBy(k->k.getLong("evaldim.id"), Collectors.toList()));
            evalDimAsserMap.entrySet().stream().forEach(entry -> {
                // 每个测评维度中，对应的维度测评人，再按测评对象分组
                evalDimAssObjMap.put(entry.getKey(), entry.getValue().stream().collect(Collectors.groupingBy(k->k.getLong("assobj.id"), Collectors.toList())).keySet());
            });
            evalDimAssObjMap.entrySet().stream().forEach(evalDimAssObj -> {
                assessTaskDomainService.nextEvalDimAssessTaskTrigger(ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(evalDimAssObj.getValue().stream().collect(Collectors.toList())),evalDimAssObj.getKey());
            });
        } catch (Exception e) {
            LOG.error("startupAssessObject 1 fail:", e);
            required.markRollback();
        } finally {
            required.close();
        }

    }

    /**
     * 根据维度测评人关系生成维度测评人测评任务
     *
     * @param assessObjs
     * @param toAddAsserMap
     * @return
     */
    private List<DynamicObject> assessTaskCreate(DynamicObject[] assessObjs, Map<Long, List<DynamicObject>> toAddAsserMap) {
        List<AssessTaskBO> assessTaskBOList = new ArrayList<AssessTaskBO>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        Map<Long, DynamicObject> assessObjMap = Arrays.stream(assessObjs).collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
        toAddAsserMap.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(dimAsser -> {
                AssessTaskBO bo = new AssessTaskBO();
                bo.setAssessObjId(assessObjMap.get(entry.getKey()).getLong("id"));
                bo.setPersonId(assessObjMap.get(entry.getKey()).getLong("person.id"));
                bo.setPerfFileId(assessObjMap.get(entry.getKey()).getLong("perffile.id"));
                bo.setEvalDimId(dimAsser.getLong("evaldim"));
                bo.setAssessActId(assessObjMap.get(entry.getKey()).getLong("assessact.id"));
                bo.setDimGroupId(assessObjMap.get(entry.getKey()).getLong("dimgroup.id"));
                bo.setDimAssessorId(dimAsser.getLong("id"));
                bo.setAssesserPersonId(dimAsser.getLong("assesser"));
                // 判断当前的维度测评人是否可生成测评任务
                if (ASSESS_TASK_DOMAIN_SERVICE.canGenerateSendAssessTask(bo.getAssessObjId(), bo.getEvalDimId())) {
                    assessTaskBOList.add(bo);
                }
            });
        });
        return ASSESS_TASK_DOMAIN_SERVICE.createAssessTask(assessTaskBOList);
    }

    /**
     * 生成维度测评人的指标打分实例数据
     *
     * @param toAddAsserMap
     */
    private void generateDimAssesserIndScoreInstData(Map<Long, List<DynamicObject>> toAddAsserMap) {
        // 添加了新维度测评人的测评对象
        List<Long> addNewDimAssesserObjList = toAddAsserMap.keySet().stream().collect(Collectors.toList());
        // 新添加的维度测评人 测评人列表
        List<Long> assesserIdList = new ArrayList<Long>();
        toAddAsserMap.values().stream().forEach(value -> assesserIdList.addAll(value.stream().map(e -> e.getLong("assesser")).distinct().collect(Collectors.toList())));
        // 添加了新的维度测评人的测评维度列表
        List<Long> evalDimIdList = new ArrayList<Long>();
        toAddAsserMap.values().stream().forEach(value -> evalDimIdList.addAll(value.stream().map(e -> e.getLong("evaldim")).distinct().collect(Collectors.toList())));
        // 新添加的维度测评人
        DynamicObject[] dimAssesserNew = DIMASSESSER_ENTITY_SERVICE.queryDimAssesserForAdjust(addNewDimAssesserObjList, assesserIdList, evalDimIdList);
        // 新添加的维度测评人所属分组对应的测评表
        DynamicObject[] assessForms = ASSESS_FORM_ENTITY_SERVICE.queryAssessFormByPks(Arrays.stream(dimAssesserNew).map(e -> e.getLong("groupdim.assessform.id")).collect(Collectors.toList()));
        // 分组与对应测评表映射
        Map<Long, DynamicObject> dimGroupAssessFormMap = Arrays.stream(assessForms).collect(Collectors.toMap(k -> k.getLong("dimgroup.id"), v -> v));
        // 生成该维度测评人的指标打分实例分录数据
        Arrays.stream(dimAssesserNew).forEach(dimasser -> {
            // 生成该维度测评人的指标打分实例分录数据
            DynamicObjectCollection scoreInstEntry = dimasser.getDynamicObjectCollection(ENTRYENTITY);
            // 更具当前维度测评人所属分组，对应的测评表下的指标分录表，生成指标打分实例分录数据
            dimGroupAssessFormMap.get(dimasser.getLong("groupdim.id")).getDynamicObjectCollection(ENTRYENTITY).stream().forEach(indicator -> {
                DynamicObject indicatorInstDy = scoreInstEntry.addNew();
                indicatorInstDy.set("assessobj", dimasser.getLong("assobj.id"));
                indicatorInstDy.set("assessformrow", indicator.getLong(ID));
                indicatorInstDy.set("indicatorweight", indicator.getBigDecimal("indicatorweight"));
            });
        });
        // 保存维度测评人 对应的指标打分实例分录数据
        DIMASSESSER_ENTITY_SERVICE.save(dimAssesserNew);
    }


    /**
     * 生成任务
     */
    public void createAssessTask(List<DynamicObject> addForTaskList, DimassesserBO dimassesserBO) {
        //生成新任务
        List<AssessTaskBO> assessTasks = new ArrayList<>();
        addForTaskList.stream().forEach(DynamicObject -> {
            AssessTaskBO assessTaskBO = new AssessTaskBO();
            assessTaskBO.setAssessActId(dimassesserBO.getAssessActId());
            assessTaskBO.setAssessObjId(dimassesserBO.getAssessObjId());
            assessTaskBO.setDimAssessorId(DynamicObject.getLong("assesser"));
            assessTaskBO.setDimGroupId(dimassesserBO.getDimGroupId());
            assessTaskBO.setPerfFileId(dimassesserBO.getPerfFileId());
            assessTaskBO.setEvalDimId(DynamicObject.getLong("evaldim"));
            boolean flag = assessTaskDomainService.canGenerateSendAssessTask(dimassesserBO.getAssessObjId(), DynamicObject.getLong("evaldim"));
            if (flag) {
                assessTasks.add(assessTaskBO);
            }


        });
        if (!assessTasks.isEmpty()) {
            assessTaskDomainService.createAssessTask(assessTasks);
        }
    }

    /**
     * 重新生成分组下的维度测评人的指标打分实例数据
     *
     * @param dimSetting
     */
    public void reGenerateDimAssesserIndScoreInstDataGroup(DynamicObject dimSetting) {
        // 该分组下的维度测评人
        DynamicObject[] dimAssesserGroup = DIMASSESSER_ENTITY_SERVICE.queryDimAsserDimGroup(dimSetting.getLong(ID));
        // 先清除该分组下的维度测评人的指标打分实例旧数据
        ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.deleteScoreItemInsAssObj(Arrays.stream(dimAssesserGroup).map(e -> e.getLong(ID)).collect(Collectors.toList()));
        DynamicObject assessForm = ASSESS_FORM_ENTITY_SERVICE.queryAssessFormByPk(dimSetting.getLong("assessform"));
        // 根据当前分组挂的最新的测评表，生成指标打分实例数据
        Arrays.stream(dimAssesserGroup).forEach(dimasser -> {
            // 生成该维度测评人的指标打分实例分录数据
            DynamicObjectCollection scoreInstEntry = dimasser.getDynamicObjectCollection(ENTRYENTITY);
            // 更具当前维度测评人所属分组，对应的测评表下的指标分录表，生成指标打分实例分录数据
            assessForm.getDynamicObjectCollection(ENTRYENTITY).stream().forEach(indicator -> {
                DynamicObject indicatorInstDy = scoreInstEntry.addNew();
                indicatorInstDy.set("assessobj", dimasser.getLong("assobj.id"));
                indicatorInstDy.set("assessformrow", indicator.getLong(ID));
                indicatorInstDy.set("indicatorweight", indicator.getBigDecimal("indicatorweight"));
            });
        });
        // 保存
        DIMASSESSER_ENTITY_SERVICE.save(dimAssesserGroup);
    }

}