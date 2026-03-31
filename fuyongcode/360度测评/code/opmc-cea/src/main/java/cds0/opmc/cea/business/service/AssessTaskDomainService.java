package cds0.opmc.cea.business.service;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.business.bo.AssessTaskBO;
import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.AssessManageUtils;
import cds0.opmc.cea.common.AssessTaskStatusEnum;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.constants.HRBaseConstants;

import java.util.*;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.ASSESS_TASK_STATUS;
import static cds0.opmc.cea.common.AppflgConstant.ENTRYENTITY;

public class AssessTaskDomainService {
    protected static final AssessTaskEntityService assessTaskEntityService = AssessTaskEntityService.getInstance();
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final EvalDimSettingEntityService EVAL_DIM_SETTING_ENTITY_SERVICE = EvalDimSettingEntityService.getInstance();
    private static final AssessTaskEntityService ASSESS_TASK_ENTITY_SERVICE = AssessTaskEntityService.getInstance();
    private static final MessageService MESSAGE_SERVICE = MessageService.getInstance();
    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();

    public static AssessTaskDomainService getInstance() {
        return ServiceFactory.getService(AssessTaskDomainService.class);
    }

    /**
     * 创建测评任务
     * @param assessTaskBos
     * @return
     */
    public List<DynamicObject> createAssessTask(List<AssessTaskBO> assessTaskBos) {
        List<DynamicObject> assessTaskList = new ArrayList(assessTaskBos.size());
        assessTaskBos.stream().forEach(assessTaskBO -> {
            DynamicObject assessTask = assessTaskEntityService.generateEmptyDynamicObject();
            assessTask.set("assessobj", assessTaskBO.getAssessObjId());
            assessTask.set("person", assessTaskBO.getPersonId());
            assessTask.set("perffile", assessTaskBO.getPerfFileId());
            assessTask.set("evaldim", assessTaskBO.getEvalDimId());
            assessTask.set("assessact", assessTaskBO.getAssessActId());
            assessTask.set("assestaskstatus", AssessTaskStatusEnum.WAITTING.getValue());
            assessTask.set("taskarrivaltime", new Date());
            assessTask.set("dimgroup", assessTaskBO.getDimGroupId());
            assessTask.set("dimassesser", assessTaskBO.getDimAssessorId());
            assessTask.set("assesserperson", assessTaskBO.getAssesserPersonId());
            assessTask.set("enable", HRBaseConstants.INT_ONE);
            assessTaskList.add(assessTask);
        });
        return assessTaskList;
    }

    //失效考核任务
    public void invalidAssessTask(List<Long> assessTaskIds) {
        DynamicObject[] assessTasks =  assessTaskEntityService.query("id,status", new QFilter[] {
                new QFilter("id" , QCP.in, assessTaskIds)
        });
        DynamicObject[] updateAssessTasks = new DynamicObject[assessTasks.length];
        int index = 0;
        for (DynamicObject assessTask : assessTasks) {
            assessTask.set(ASSESS_TASK_STATUS, AssessTaskStatusEnum.EXPRIED.getValue());
            updateAssessTasks[index++] = assessTask;
        }
        assessTaskEntityService.update(updateAssessTasks);
    }

    /**
     * 失效指定维度测评人的测评任务
     * @param dimAssersIdList
     */
    public void invalidAssessTaskDimAssesser(List<Long> dimAssersIdList){
        DynamicObject[] assessTasks = assessTaskEntityService.queryAssessTaskDimAssesser(dimAssersIdList);
        Arrays.stream(assessTasks).forEach(task -> { task.set(ASSESS_TASK_STATUS, AssessTaskStatusEnum.EXPRIED.getValue()); });
        assessTaskEntityService.update(assessTasks);
    }


    /**
     * 指定维度测评人是否存在未完成的测评任务
     * @param dimAssersIdList
     * @return
     */
    public boolean hasUnCompletedAssessTaskDimAssesser(List<Long> dimAssersIdList){
        return assessTaskEntityService.countUnCompletedAssessTaskDimAssesser(dimAssersIdList) > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 测评对象在指定维度上是否有未处理的任务
     * @param assessObjIds
     * @param evalDimId
     * @return
     */
    public Map<Long,Boolean> hasUnCompletedAssessTaskAssessObjEvalDim(List<Long> assessObjIds, Long evalDimId){
        Map<Long,Boolean> assObjHasUncompletedAssessTaskMap = new HashMap<>();
        assessObjIds.stream().forEach(assObjId -> {
            assObjHasUncompletedAssessTaskMap.put(assObjId,ASSESS_TASK_ENTITY_SERVICE.countAssessTaskAssessObjEvalDim(assObjId, evalDimId) > 0 ? Boolean.TRUE : Boolean.FALSE);
        });
        return assObjHasUncompletedAssessTaskMap;
    }

    /**
     * 当测评对象所有维度的维度顺序一样时，查询改测评对象在所有维度上是否存在未完成的任务
     * @param assessObjId
     * @return
     */
    public boolean hasUnCompletedAssessTaskAssessObjAllEvalDim(Long assessObjId){
        return ASSESS_TASK_ENTITY_SERVICE.countAssessTaskAssessObj(assessObjId) > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 指定测评对象下是否存在未发送的维度测评人
     * @param assessObjId
     * @return
     */
    public boolean hasUnSendAssessTaskAssessObj(Long assessObjId){
        return ASSESS_TASK_ENTITY_SERVICE.countAssessTaskDimAssesser(Arrays.stream(DIMASSESSER_ENTITY_SERVICE.queryDimAsserAssObj(Collections.singletonList(assessObjId))).map(e->e.getLong(AppflgConstant.ID)).collect(Collectors.toList())) < DIMASSESSER_ENTITY_SERVICE.queryDimAsserAssObj(Collections.singletonList(assessObjId)).length ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 测评对象在指定维度上是否出现有些维度测评人未发送测评人的情况
     * @param assessObjIds
     * @param evalDimId
     * @return
     */
    public Map<Long,Boolean> hasUnSendAssessTaskAssessObjEvalDim(List<Long> assessObjIds, Long evalDimId){
        Map<Long,Boolean> assObjHasUnSendAssessTaskMap = new HashMap<>();
        Map<Long, List<DynamicObject>> assObjAsserDimGroup = Arrays.stream(DIMASSESSER_ENTITY_SERVICE.queryDimAssesserAssobjEvalDim(assessObjIds, evalDimId)).collect(Collectors.groupingBy(k->k.getLong("assobj.id"),Collectors.toList()));
        assessObjIds.stream().forEach(assObjId -> {
            // 如果当前测评对象在当前维度上的维度测评人对应的任务数量小于维度测评人数量，则表明有的维度测评人还未发送测评任务
            List<DynamicObject> assObjdimAsser = assObjAsserDimGroup.get(assObjId);
            if(assObjdimAsser == null){ // 场景：该维度下有测评人数据，但是这些测评人数据测评人为空，这种情况等同于存在未发送任务的维度测评人
                assObjHasUnSendAssessTaskMap.put(assObjId, Boolean.TRUE);
            }else{
                assObjHasUnSendAssessTaskMap.put(assObjId,ASSESS_TASK_ENTITY_SERVICE.countAssessTaskDimAssesser(assObjdimAsser.stream().map(e->e.getLong(AppflgConstant.ID)).collect(Collectors.toList())) < assObjAsserDimGroup.get(assObjId).size() ? Boolean.TRUE : Boolean.FALSE);
            }
        });
        return assObjHasUnSendAssessTaskMap;
    }

    /**
     * 失效测评活动下的测评任务
      */
    public void invalidAssessTaskActivitys(List<Long> assessActIds){
        DynamicObject[] assessTasks = assessTaskEntityService.queryAssessTaskActivitys(assessActIds);
        Arrays.stream(assessTasks).forEach(assessTask -> {
            assessTask.set(ASSESS_TASK_STATUS, AssessTaskStatusEnum.EXPRIED.getValue());
        });
        assessTaskEntityService.update(assessTasks);
    }

    /**
     * 判断当前的测评对象在某个维度上是否可以生成测评任务并发送
     * @param assessObjId
     * @param evalDimId
     * @return
     */
    public boolean canGenerateSendAssessTask(Long assessObjId,Long evalDimId){
        boolean canGenAndSend = Boolean.FALSE;
        DynamicObject assessObj = ASSESS_OBJ_ENTITY_SERVICE.queryOne(assessObjId);
        DynamicObjectCollection dimAssessers = assessObj.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY);
        Set<Integer> evalDimIndexSet = dimAssessers.stream().map(assesser -> assesser.getInt("evaldim.dimindex")).collect(Collectors.toSet());
        if(evalDimIndexSet.size() == HRBaseConstants.INT_ONE){
            // 当前测评对象下所有维度的维度顺序一样，则可以生成测评任务并发送
            canGenAndSend = Boolean.TRUE;
        }else{
                // 判断当前维度的前置维度下的维度测评人测评任务是否都已完成，若已完成，则当前维度可生成测评任务并发送
                DynamicObject evalDim = EVAL_DIM_SETTING_ENTITY_SERVICE.queryOne(evalDimId);
                // 获取所有前置维度的维度测评人数据
                List<Long> dimAssersIdList = dimAssessers.stream().filter(asser -> asser.getInt("evaldim.dimindex") < evalDim.getInt("dimindex")).
                        collect(Collectors.toList()).stream().map(dimasser -> dimasser.getLong(HRBaseConstants.ID)).
                        collect(Collectors.toList());
                // 查询前置维度的维度测评人是否存在未完成的测评任务（待处理和暂存）
                canGenAndSend = hasUnCompletedAssessTaskDimAssesser(dimAssersIdList) ? Boolean.FALSE : Boolean.TRUE;
                if(canGenAndSend){
                    // 先升序排序
                    List<Integer> evalDimIndexList = evalDimIndexSet.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
                    if(evalDimIndexList.indexOf(evalDim.getInt("dimindex")) > 0){
                        // 获取当前维度的上一个维度顺序
                        Integer preEvalDimIndex = evalDimIndexList.get(evalDimIndexList.indexOf(evalDim.getInt("dimindex")) - 1);
                        // 当前测评对象下，当前维度的前一个维度顺序的维度测评人
                        List<Long> preEvalDimAssersIdList = dimAssessers.stream().filter(asser -> (asser.getInt("evaldim.dimindex") == preEvalDimIndex && asser.getLong("assesser.id") != 0L)).
                                collect(Collectors.toList()).stream().map(dimasser -> dimasser.getLong(HRBaseConstants.ID)).
                                collect(Collectors.toList());
                        // 如果上一个维度顺序的维度已有任务且任务数量与这个维度的维度测评人数量一致，且当前维度的前置维度未出现未处理的任务，则当前维度可以生成任务发送，否则，表明当前维度测评人还有人未发送任务，则不允许当前维度生成任务发送
                        canGenAndSend = assessTaskEntityService.countAssessTaskDimAssesser(preEvalDimAssersIdList) < preEvalDimAssersIdList.size() ? Boolean.FALSE : Boolean.TRUE;
                }
            }
        }
        return canGenAndSend;
    }

    /**
     * 打分提交时触发下一个下一个维度生成测评任务并发送
     * @param assessObjs
     * @param evalDimId
     * @return
     */
    public Map<Long,Boolean> nextEvalDimAssessTaskTrigger(DynamicObject[] assessObjs, Long evalDimId){
        List<Long> assessObjIds = Arrays.stream(assessObjs).map(e->e.getLong(AppflgConstant.ID)).collect(Collectors.toList());
        Map<Long,Boolean> assessObjLastEvalDimMap = new HashMap<>();
        Map<Long,Boolean> assObjHasUncompletedAssessTaskMap = hasUnCompletedAssessTaskAssessObjEvalDim(assessObjIds,evalDimId);
        Map<Long,Boolean> assObjHasUnSendAssessTaskMap = hasUnSendAssessTaskAssessObjEvalDim(assessObjIds,evalDimId);
        Map<Long,DynamicObject> assessObjMap = Arrays.stream(assessObjs).collect(Collectors.toMap(k->k.getLong(AppflgConstant.ID), V->V));
        DynamicObject evalDim = EVAL_DIM_SETTING_ENTITY_SERVICE.queryOne(evalDimId);
        List<DynamicObject> toSendAssessTasks =new ArrayList<DynamicObject>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        Arrays.stream(assessObjs).forEach(assObj -> {
            DynamicObjectCollection dimAssessers = assObj.getDynamicObjectCollection(ENTRYENTITY);
            Set<Integer> evalDimIndexSet = dimAssessers.stream().map(assesser -> assesser.getInt("evaldim.dimindex")).collect(Collectors.toSet());
            if(HRBaseConstants.INT_ONE == evalDimIndexSet.size()){
                // 当前测评对象下的维度测评人，维度顺序相同
                if(!hasUnCompletedAssessTaskAssessObjAllEvalDim(assObj.getLong(AppflgConstant.ID)) && !hasUnSendAssessTaskAssessObj(assObj.getLong(AppflgConstant.ID))){
                    // 当前测评对象在所有维度上不存在未完成的任务，则把该测评对象设置为已处理
                    // 当前维度为该测评对象最后维度，且当前测评对象在该维度上没有未发送任务的维度测评人，且当前测评对象在该维度上所有维度测评人的测评任务均已处理，则测评对象状态更新为已完成
                    assObj.set("assesstaus", AssessStatusEnum.PROCESSED.getValue());
                    // 反写测评对象结束时间
                    assObj.set("overtime", new Date());
                    // 计算测评对象综合得分反写到测评对象上
                    assObj.set("calscore", AssessManageUtils.compositeScore(assObj.getLong(AppflgConstant.ID)));
                    assObj.set("modscore", AssessManageUtils.compositeScore(assObj.getLong(AppflgConstant.ID)));
                }
            }else{
                // 当前测评对象下的维度测评人，维度顺序不相同
                // 先升序排序
                List<Integer> evalDimIndexList = evalDimIndexSet.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
                if(evalDimIndexList.indexOf(evalDim.getInt("dimindex")) > -1 && (evalDimIndexList.indexOf(evalDim.getInt("dimindex")) + 1) < evalDimIndexList.size()){
                    // ==== 可取到下一个维度,且当前测评对象在当前维度上没有未处理的任务，且当前维度上所有的维度测评人都已发送了测评任务，则允许当前测评对象在下个维度上生成测评任务并发送
                    if(!assObjHasUncompletedAssessTaskMap.get(assObj.getLong(AppflgConstant.ID)) && !assObjHasUnSendAssessTaskMap.get(assObj.getLong(AppflgConstant.ID))){
                        // 获取当前维度的下一个维度顺序
                        Integer nextEvalDimIndex = evalDimIndexList.get(evalDimIndexList.indexOf(evalDim.getInt("dimindex")) + 1);
                        // 当前维度的下一个维度顺序的维度测评人
                        List<DynamicObject> nextEvalDimAssers = dimAssessers.stream().filter(asser -> asser.getInt("evaldim.dimindex") == nextEvalDimIndex).
                                collect(Collectors.toList());
                        toSendAssessTasks.addAll(assessTaskCreate(assObj,nextEvalDimAssers.stream().collect(Collectors.toList())));
                    }
                    // 当前维度对于当前测评对象来说不是最后一个维度
                    assessObjLastEvalDimMap.put(assObj.getLong(AppflgConstant.ID),Boolean.FALSE);
                }else{
                    // 当前维度对于当前测评对象来说为最后一个维度
                    assessObjLastEvalDimMap.put(assObj.getLong(AppflgConstant.ID),Boolean.TRUE);
                }
            }
        });
        TXHandle required = TX.required();
        try{
            // 测评任务落库
            ASSESS_TASK_ENTITY_SERVICE.save(toSendAssessTasks.stream().toArray(DynamicObject[]::new));
            // 发送测评任务
            toSendAssessTasks.stream().collect(Collectors.groupingBy(task -> task.getLong("assessobj"), Collectors.toList())).entrySet().stream().forEach(entry -> {
                MESSAGE_SERVICE.sendAssessTaskMessage(entry.getValue(),assessObjMap.get(entry.getKey()));
            });
        }catch (Exception e) {
            required.markRollback();
        } finally {
            required.close();
        }
        return assessObjLastEvalDimMap;
    }

    /**
     * 打分提交
     * @param assessObjIds
     * @param evalDimId
     */
    public void assembleAssessObjStatuSubmit(List<Long> assessObjIds, Long evalDimId){
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(assessObjIds);
        Long activityId = Arrays.stream(assessObjs).map(e->e.getLong("assessact.id")).collect(Collectors.toSet()).iterator().next();
        DynamicObject assessActivity = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(activityId);
        Map<Long,DynamicObject> assessObjMap = Arrays.stream(assessObjs).collect(Collectors.toMap(k->k.getLong(AppflgConstant.ID), v->v));
        Map<Long, Boolean>  assessObjLastEvalDimMap = nextEvalDimAssessTaskTrigger(assessObjs, evalDimId);
        Map<Long,Boolean> assObjHasUncompletedAssessTaskMap = hasUnCompletedAssessTaskAssessObjEvalDim(assessObjIds,evalDimId);
        Map<Long,Boolean> assObjHasUnSendAssessTaskMap = hasUnSendAssessTaskAssessObjEvalDim(assessObjIds,evalDimId);
        assessObjLastEvalDimMap.entrySet().stream().forEach(entry -> {
            if(entry.getValue() && !assObjHasUnSendAssessTaskMap.get(entry.getKey()) && !assObjHasUncompletedAssessTaskMap.get(entry.getKey())){
                // 当前维度为该测评对象最后维度，且当前测评对象在该维度上没有未发送任务的维度测评人，且当前测评对象在该维度上所有维度测评人的测评任务均已处理，则测评对象状态更新为已完成
                assessObjMap.get(entry.getKey()).set("assesstaus", AssessStatusEnum.PROCESSED.getValue());
                // 反写测评对象结束时间
                assessObjMap.get(entry.getKey()).set("overtime", new Date());
                // 计算测评对象综合得分反写到测评对象上
                assessObjMap.get(entry.getKey()).set("calscore", AssessManageUtils.compositeScore(entry.getKey()));
                assessObjMap.get(entry.getKey()).set("modscore", AssessManageUtils.compositeScore(entry.getKey()));
            }
        });
        ASSESS_OBJ_ENTITY_SERVICE.update(assessObjs);
        // 检查当前测评活动下所有的测评对象是否都已完成，是则将测评活动状态置为已结束
//        if(!ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.hasHavingAssesssObjInActivity(activityId)){
//            assessActivity.set("activitystatus", AssessActivityStatusEnum.FINISHED.getValue());
//            assessActivity.set("overtime", new Date());
//            ASSESS_ACTIVITY_ENTITY_SERVICE.updateOne(assessActivity);
//        }
    }

    /**
     * 根据维度测评人关系生成维度测评人测评任务
     * @param assObj
     * @param dimAssessers
     * @return
     */
    private List<DynamicObject> assessTaskCreate(DynamicObject assObj, List<DynamicObject> dimAssessers){
        List<AssessTaskBO> assessTaskBOList = new ArrayList<AssessTaskBO>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        dimAssessers.stream().forEach(dimAsser -> {
            AssessTaskBO bo = new AssessTaskBO(assObj.getLong("id"),
                    assObj.getLong("person.id"),
                    assObj.getLong("perffile.id"),
                    dimAsser.getLong("evaldim.id"),
                    assObj.getLong("assessact.id"),
                    assObj.getLong("dimgroup.id"),
                    dimAsser.getLong("id"),
                    dimAsser.getLong("assesser.id")
            );
            if(dimAsser.getLong("assesser.id") != 0L && assessTaskEntityService.countAssessTaskDimAssesser(Collections.singletonList(dimAsser.getLong("id"))) == HRBaseConstants.INT_ZERO){
                // 测评人不为空且该维度测评人未生成测评任务，才生成测评任务
                assessTaskBOList.add(bo);
            }
        });
        return createAssessTask(assessTaskBOList);
    }
}
