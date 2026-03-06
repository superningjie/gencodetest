package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.common.AssessTaskStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static cds0.opmc.cea.common.AppflgConstant.ASSESS_TASK;

public class AssessTaskEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper assessTaskService = new HRBaseServiceHelper(ASSESS_TASK);
    public static AssessTaskEntityService getInstance() {
        return ServiceFactory.getService(AssessTaskEntityService.class);
    }
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return assessTaskService;
    }

    /**
     * 删除指定测评活动下的测评任务
     * @param assessActIds
     * @return
     */
    public int deleteAssessTaskActivitys(List<Long> assessActIds){
        QFilter qFilter = new QFilter("assessact.id", QCP.in, assessActIds);
        return deleteByFilter(new QFilter[]{qFilter});
    }

    /**
     * 查询测评活动下的测评任务
     * @param assessActIds
     * @return
     */
    public DynamicObject[] queryAssessTaskActivitys(List<Long> assessActIds){
        QFilter qFilter = new QFilter("assessact.id", QCP.in, assessActIds);
        return query("id,assestaskstatus",new QFilter[]{qFilter});
    }
    /**
     * 查询测评任务
     */
    public DynamicObject[] queryAssessTaskIds(List<Long> assessorIds){
        QFilter qFilter = new QFilter("dimassesser.id", QCP.in, assessorIds);
        return query("id,assestaskstatus",new QFilter[]{qFilter});
    }

    /**
     * 根据id查询测评任务
     */
    public DynamicObject[] queryAssessTaskByIds(List<Long> ids){
        QFilter qFilter = new QFilter("id", QCP.in, ids);
        return query("id, assestaskstatus, dimassesser, assesserperson, assesserperson.id, dimassesser.id, evaldim.id, evaldim.dimindex, evaldim.dimname, evaldim.dimweight, dimassesser.dimweight, dimassesser.assesser.id, dimassesser.assesser.name,dimassesser.assesstatus, perffile.id,perffile.name, dimgroup, dimgroup.id, dimgroup.dimension,assessobj.id",new QFilter[]{qFilter});
    }

    /**
     * 根据测评任务id和测评人id查询任务
     */
    public DynamicObject[] queryAssessTaskByActivityAndAssesser(Long activityId,Long assesserId){
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, activityId)
                .and("assesserperson.id",QCP.equals,assesserId);
        return query("id, assestaskstatus, dimassesser, assesserperson, assesserperson.id, dimassesser.id,assessact, assessact.id, evaldim.id, evaldim.dimindex, evaldim.dimname, evaldim.dimweight, dimassesser.dimweight, dimassesser.assesser.id, dimassesser.assesser.name,dimassesser.assesstatus, perffile.id,perffile.name, dimgroup, dimgroup.id, dimgroup.dimension,assessobj.id",new QFilter[]{qFilter});
    }

    /**
     * 查询指定状态的测评任务
     * @param assessTaskStatus
     * @return
     */
    public DynamicObject[] queryAssessTaskList(String assessTaskStatus){
        QFilter qFilter = new QFilter("assestaskstatus", QCP.equals, assessTaskStatus);
        return query("id, assessobj, dimassesser, dimassesser.id, dimgroup",new QFilter[]{qFilter});
    }

    //根据测评活动id查询测评任务分组维度相关信息
    public DynamicObject[] queryAssessTaskOfAssseeGroup(Long assessActId){
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, assessActId);
        return query("id, assestaskstatus, dimgroup, evaldim.dimindex, assessobj.id",new QFilter[]{qFilter});
    }

    /**
     * 统计指定维度测评人 未完成的测评任务数量
     * @param dimAssersIdList
     * @return
     */
    public int countUnCompletedAssessTaskDimAssesser(List<Long> dimAssersIdList){
        QFilter qFilter = new QFilter("dimassesser.id", QCP.in, dimAssersIdList).
                and("assestaskstatus", QCP.in, new HashSet<String>(Arrays.asList(AssessTaskStatusEnum.WAITTING.getValue(),AssessTaskStatusEnum.TEMPSTORAGE.getValue())));
        return count(ASSESS_TASK, new QFilter[]{qFilter});
    }

    /**
     * 同级指定维度测评人 的测评任务数量
     * @param dimAssersIdList
     * @return
     */
    public int countAssessTaskDimAssesser(List<Long> dimAssersIdList){
        QFilter qFilter = new QFilter("dimassesser.id", QCP.in, dimAssersIdList);
        return count(ASSESS_TASK, new QFilter[]{qFilter});
    }

    /**
     * 统计指定测评对象在指定维度上未处理的任务数量
     * @param assessObjId
     * @param evalDimId
     * @return
     */
    public int countAssessTaskAssessObjEvalDim(Long assessObjId, Long evalDimId){
        QFilter qFilter = new QFilter("assessobj.id", QCP.equals, assessObjId).
                and("evaldim.id", QCP.equals, evalDimId).
                and("assestaskstatus", QCP.in, new HashSet<String>(Arrays.asList(AssessTaskStatusEnum.WAITTING.getValue(),AssessTaskStatusEnum.TEMPSTORAGE.getValue())));
        return count(ASSESS_TASK, new QFilter[]{qFilter});
    }

    /**
     * 同级测评对象在所有维度上未处理的任务数量
     * @param assessObjId
     * @return
     */
    public int countAssessTaskAssessObj(Long assessObjId){
        QFilter qFilter = new QFilter("assessobj.id", QCP.equals, assessObjId).
                and("assestaskstatus", QCP.in, new HashSet<String>(Arrays.asList(AssessTaskStatusEnum.WAITTING.getValue(),AssessTaskStatusEnum.TEMPSTORAGE.getValue())));
        return count(ASSESS_TASK, new QFilter[]{qFilter});
    }

    /**
     * 查询指定维度测评人对应的待处理的测评任务
     * @param dimAssersIdList
     * @return
     */
    public DynamicObject[] queryAssessTaskDimAssesser(List<Long> dimAssersIdList){
        QFilter qFilter = new QFilter("dimassesser.id", QCP.in, dimAssersIdList).
                and("assestaskstatus", QCP.equals, AssessTaskStatusEnum.WAITTING.getValue());
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }


    /**
     * 查询测评对象的测评任务
     * @param objId
     * @return
     */
    public DynamicObject[] queryAssessTaskByObjId(Long objId){
        QFilter qFilter = new QFilter("assessobj", QCP.in, objId)
                .and("assestaskstatus", QCP.equals, AssessTaskStatusEnum.PROCESSED.getValue());;
        return query("person , assesserperson, assestaskstatus , dimassesser, evaldim",new QFilter[]{qFilter});
    }

    /**
     * 根据维度查询
     * @param assessActivityId
     * @param userId
     * @param meterHeaderList
     * @param number
     * @return
     */
    public DynamicObject[] queryAssessTaskByEvaldim(Long assessActivityId,Long userId, List<List<Long>> meterHeaderList,Integer number){
        return assessTaskService.query("evaldim,evaldim.dimindex,dimgroup,dimgroup.groupdimname,dimgroup.groupindex,dimgroup.assessform,dimassesser,assestaskstatus", new QFilter[]{
                new QFilter("assessact", QCP.equals, assessActivityId),
                new QFilter("dimassesser.assesser", QCP.equals, userId),
                new QFilter("assestaskstatus", QCP.not_equals, AssessTaskStatusEnum.EXPRIED.getValue()),
                new QFilter("id", QCP.in,meterHeaderList.get(number))//维度
        });
    }

    /**
     * 根据当前用户查询暂存待处理的任务
     * @param assessActivityId
     * @param userId
     * @return
     */
    public DynamicObject[] queryWaitingTempStorageByCurrentUserId(Long assessActivityId, Long userId) {
        return assessTaskService.query("evaldim,evaldim.dimindex,dimgroup,dimgroup.groupdimname,dimgroup.groupindex,dimgroup.assessform,dimassesser", new QFilter[]{
                new QFilter("assessact", QCP.equals, assessActivityId),
                new QFilter("dimassesser.assesser", QCP.equals, userId),
                new QFilter("assestaskstatus", QCP.not_equals, AssessTaskStatusEnum.EXPRIED.getValue()),
                new QFilter("assestaskstatus", QCP.not_equals, AssessTaskStatusEnum.PROCESSED.getValue())
        });
    }

    /**
     * 根据当前用户查询暂存待处理的任务
     * @param dimAssessorIds
     * @return
     */
    public DynamicObject[] queryByDimAssessor(List<Long> dimAssessorIds) {
        return query(new QFilter[]{
                new QFilter("dimassesser", QCP.in, dimAssessorIds)
        });
    }
}
