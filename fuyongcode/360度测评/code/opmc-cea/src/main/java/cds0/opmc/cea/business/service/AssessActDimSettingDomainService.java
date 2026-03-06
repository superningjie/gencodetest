package cds0.opmc.cea.business.service;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.common.enums.AssessActivityStatusEnum;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.hr.hbp.common.constants.HRBaseConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.*;

public class AssessActDimSettingDomainService {
    private static final Log LOG = LogFactory.getLog(AssessActDimSettingDomainService.class);
    private static final DimSettingEntityService DIM_SETTING_ENTITY_SERVICE = DimSettingEntityService.getInstance();
    private static final EvalDimSettingEntityService EVAL_DIM_SETTING_ENTITY_SERVICE = EvalDimSettingEntityService.getInstance();
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();
    private static final AssessTaskEntityService ASSESS_TASK_ENTITY_SERVICE = AssessTaskEntityService.getInstance();
    private static final AssessTaskDomainService ASSESS_TASK_DOMAIN_SERVICE = AssessTaskDomainService.getInstance();
    private static final AssObjScoItemInstEntityService ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE = AssObjScoItemInstEntityService.getInstance();
    public static AssessActDimSettingDomainService getInstance() {
        return ServiceFactory.getService(AssessActDimSettingDomainService.class);
    }

    /**
     * 删除分组维度数据
     * @param dimsettingIds
     */
    public void deleteDimSetting(List<Long> dimsettingIds){
        TXHandle required = TX.required();
        try {
            // 删除分组下的测评对象的测评维度人的指标打分实例数据
            deleteScoreItemInsAssObj(dimsettingIds);
            // 删除分组前，需要将改分组下所有的测评对象删除，且测评对象下的维度测评人关系也需要删除
            ASSESS_OBJ_ENTITY_SERVICE.deleteAssessObjByDimSettings(dimsettingIds);
            // 删除分组的维度数据
            EVAL_DIM_SETTING_ENTITY_SERVICE.deleteEvalDimByDimSettingId(dimsettingIds);
            // 删除分组数据
            DIM_SETTING_ENTITY_SERVICE.delete(dimsettingIds.toArray());
        } catch (Exception e) {
            LOG.error("deleteDimSetting 1 fail:", e);
            required.markRollback();
        } finally {
            required.close();
        }
    }

    /**
     * 删除分组下的测评对象的测评维度人的指标打分实例数据
     * @param dimsettingIds
     */
    public void deleteScoreItemInsAssObj(List<Long> dimsettingIds){
        List<Long> dimassesserList = new ArrayList<Long>();
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByDimSettings(dimsettingIds);
        // 分组下所有的测评对象的维度测评人关系
        Arrays.stream(assessObjs).forEach(assessObj -> {
            dimassesserList.addAll(assessObj.getDynamicObjectCollection(ENTRYENTITY).stream().map(e -> e.getLong(ID)).collect(Collectors.toList()));
        });
        // 删除维度测评人关系前，需要把测评维度人的指标打分实例分录删除
        ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.deleteScoreItemInsAssObj(dimassesserList);
    }

    /**
     * 删除测评对象下的维度测评人的指标打分实例数据
     */
    public void deleteScoreItemInsByAssObj(List<Long> assessObjIds){
        List<Long> dimassesserList = new ArrayList<Long>();
        Arrays.stream(ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(assessObjIds)).forEach(assessObj ->{
            dimassesserList.addAll(assessObj.getDynamicObjectCollection(ENTRYENTITY).stream().map(dimasser -> dimasser.getLong(ID)).collect(Collectors.toList()));
        });
        // 删除测评维度人的指标打分实例分录
        ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.deleteScoreItemInsAssObj(dimassesserList);
    }

    /**
     * 删除测评活动下的测评对象的测评维度人的指标打分实例数据
     * @param assessActIds
     */
    private void deleteScoreItemInsAssessAct(List<Long> assessActIds){
        List<Long> dimassesserList = new ArrayList<Long>();
        DynamicObject[] assessActs = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityArrayByPk(assessActIds);
        // 测评活动下的所有的分组维度设置
        Arrays.stream(assessActs).forEach(assessAct -> {
            dimassesserList.addAll(assessAct.getDynamicObjectCollection(ENTRYENTITY).stream().map(e -> e.getLong(ID)).collect(Collectors.toList()));
        });
        deleteScoreItemInsAssObj(dimassesserList);
    }
    /**
     * 当前的分组维度设置下是否存在测评中或已完成的测评对象
     * @param dimsettingId
     * @return
     */
    public boolean hasBeingAssessObjInCurrentDimsetting(Long dimsettingId){
        return ASSESS_OBJ_ENTITY_SERVICE.countBeIngAssessObjInDimSetting(dimsettingId) > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 测评活动下是否存在测评中的测评对象
     * @param assessActId
     * @return
     */
    public boolean hasAssessingObjInActivity(List<Long> assessActId){
        return ASSESS_OBJ_ENTITY_SERVICE.countAssessingObjInActivity(assessActId) > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 指定测评活动下是否存在测评中的测评对象
     * @param assessActId
     * @return
     */
    public boolean hasAssessingObjInActivity(Long assessActId){
        return ASSESS_OBJ_ENTITY_SERVICE.countAssessingObjInActivity(assessActId) > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     *
     * @param assessActId
     * @return
     */
    public boolean hasHavingAssesssObjInActivity(Long assessActId){
        return ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityHaving(assessActId) > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 单个测评活动启动
     * @param assessActId
     */
    public void startUpSingle(Long assessActId){
        DynamicObject assessAct = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(assessActId);
        assessAct.set(ASSESS_ACTIVITY_STATUS, AssessActivityStatusEnum.HAVINGIN.getValue());
        ASSESS_ACTIVITY_ENTITY_SERVICE.updateOne(assessAct);
    }

    /**
     * 批量测评活动启动
     * @param assessActIds
     */
    public void startUpBatch(List<Long> assessActIds){
        DynamicObject[] assessActs = ASSESS_ACTIVITY_ENTITY_SERVICE.loadDynamicObjectArray(assessActIds.toArray());
        Arrays.stream(assessActs).forEach(assessAct -> {
            assessAct.set(ASSESS_ACTIVITY_STATUS, AssessActivityStatusEnum.HAVINGIN.getValue());
        });
        ASSESS_ACTIVITY_ENTITY_SERVICE.update(assessActs);
    }

    /**
     * 当前测评活动下的测评对象数量
     * @param assessActId
     * @return
     */
    public int countAssessObjInCurrentActivity(Long assessActId){
        return ASSESS_OBJ_ENTITY_SERVICE.countAssessObjInCurrentActivity(assessActId);
    }

    /**
     * 删除测评活动（待启动和已结束）
     * @param assessActIds
     */
    public void deleteAssessActivity(List<Long> assessActIds){
        TXHandle required = TX.required();
        try {
            // 删除任务
            ASSESS_TASK_ENTITY_SERVICE.deleteAssessTaskActivitys(assessActIds);
            // 删除维度测评人关系前，需要把测评维度人的指标打分实例分录删除
            deleteScoreItemInsAssessAct(assessActIds);
            // 删除测评活动下的测评对象 (级联删除维度测评人分录)
            ASSESS_OBJ_ENTITY_SERVICE.deleteAssessObjByActivitys(assessActIds);
            // 删除测评活动下分组设置下的所有维度数据
            List<Long> dimSettingIds = new ArrayList<Long>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
            DynamicObject[] assessActivitys = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityArrayByPk(assessActIds);
            Arrays.stream(assessActivitys).forEach(act -> {
                dimSettingIds.addAll(act.getDynamicObjectCollection(ENTRYENTITY).stream().map(dimsetting -> dimsetting.getLong(ID)).collect(Collectors.toList()));
            });
            EVAL_DIM_SETTING_ENTITY_SERVICE.deleteEvalDimByDimSettingId(dimSettingIds);
            // 删除测评活动（级联删除分组维度设置）
            ASSESS_ACTIVITY_ENTITY_SERVICE.deleteByActivityPks(assessActIds);
        } catch (Exception e) {
            LOG.error("deleteAssessActivity 1 fail:", e);
            required.markRollback();
        } finally {
            required.close();
        }
    }

    /**
     * 更新测评活动下的测评对象的状态为已完成
     * @param assessActId
     */
    private void updateAssessObjProcssedStatusActivity(List<Long> assessActId){
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByActivitys(assessActId);
        Arrays.stream(assessObjs).forEach(assobj -> assobj.set("assesstaus", AssessStatusEnum.PROCESSED.getValue()));
        ASSESS_OBJ_ENTITY_SERVICE.update(assessObjs);
    }

    /**
     * 更新测评活动状态为已结束
     * @param assessActId
     */
    private void updateAssessActivityFinishedStatus(List<Long> assessActId){
        DynamicObject[] assessActs = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityArrayByPk(assessActId);
        Arrays.stream(assessActs).forEach(act -> {
            act.set("overtime", new Date());
            act.set("activitystatus", AssessActivityStatusEnum.FINISHED.getValue());
        });
        ASSESS_ACTIVITY_ENTITY_SERVICE.update(assessActs);
    }
    /**
     * 结束测评活动
     * @param assessActId
     */
    public void finishAssessActivity(List<Long> assessActId){
        TXHandle required = TX.required();
        try {
            // 失效测评活动下的测评任务
            ASSESS_TASK_DOMAIN_SERVICE.invalidAssessTaskActivitys(assessActId);
            // 更新测评活动下的测评对象状态为已完成
            updateAssessObjProcssedStatusActivity(assessActId);
            // 结束测评活动,更新测评活动状态为已结束
            updateAssessActivityFinishedStatus(assessActId);
        } catch (Exception e) {
            LOG.error("finishAssessActivity 1 fail:", e);
            required.markRollback();
        } finally {
            required.close();
        }
    }
}
