package cds0.opmc.cea.business.jobtask;

import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.service.AssessActDimSettingDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import com.google.common.collect.Lists;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.dlock.DLock;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.threads.ThreadPool;
import kd.bos.threads.ThreadPools;
import kd.hr.hbp.common.constants.HRBaseConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ActivityDeleteActObjTask extends AbstractTask {
    private static final ThreadPool THREAD_POOL = ThreadPools.newCachedThreadPool("ActivityDeleteActObjTask#&", 3, 5);
    private static final Semaphore SEMAPHORE = new Semaphore(5);
    private static final Log LOG = LogFactory.getLog(ActivityStartUpActObjTask.class);
    private static final String ACTIVITY_START_UP_LOCK = "opmc/datalock/activity/delete/";
    private static final int PRECREATEINST_MQ_PERCOUNT = 200;
    private static final String SUCCESS_LIST = "success_list";
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final AssessActDimSettingDomainService ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE = AssessActDimSettingDomainService.getInstance();
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        if (map.containsKey(AppflgConstant.ACTIVITY_ID)) {
            try (DLock lock = DLock.create(ACTIVITY_START_UP_LOCK + map.get(AppflgConstant.ACTIVITY_ID))) {
                lock.lock();
                String desc = ResManager.loadKDString("正在为您自动删除评估对象...",
                        "ActivityDeleteActObjTask_0", AppflgConstant.KEY_APP_NAME);
                this.feedbackProgress(20, desc, null);
                // 测评对象ID 分批切割
                List<List<Long>> assessObjIds = Lists.partition((List) map.get(SUCCESS_LIST), PRECREATEINST_MQ_PERCOUNT );
                int count = assessObjIds.size();
                CountDownLatch atomicInteger = new CountDownLatch(count);
                for(int i = 0; i < count; i++){
                    List<Long> assObjIdsubList = assessObjIds.get(i);
                    int pecent = (i + 1)/count * 90;
                    batchDeleteAssessObject(assObjIdsubList,desc,pecent,atomicInteger);
                }
                try {
                    atomicInteger.await();
                    this.feedbackProgress(100, desc, null);
                } catch (InterruptedException e) {
                    LOG.error("==================ActivityAddActObjTask==================", e);
                }
            }
        }
    }

    private void batchDeleteAssessObject(List<Long> assObjIdsubList, String desc, int pecent, CountDownLatch atomicInteger) {
        try {
            LOG.info("==================start batchsaveEvaluationObject==================");
            SEMAPHORE.acquire();
            LOG.info("==================acquire semaphore batchsaveEvaluationObject==================");
            THREAD_POOL.execute(() -> {
                try {
                    deleteAssessObject(assObjIdsubList,desc);
                    if(pecent > HRBaseConstants.INT_ZERO){
                        this.feedbackProgress(pecent -1, desc, null);
                    }
                } catch (Exception exception) {
                    LOG.error("==================batchsaveEvaluationObject==================", exception);
                } finally {
                    SEMAPHORE.release();
                    atomicInteger.countDown();
                    LOG.info("==================release semaphore batchsaveEvaluationObject==================");
                }
            }, RequestContext.get());
        } catch (InterruptedException interruptedException) {
            LOG.error("==================batchsaveEvaluationObject InterruptedException==================", interruptedException);
        }
    }

    private void deleteAssessObject(List<Long> assObjIdsubList,String desc){
        // 将要处理的测评对象
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(assObjIdsubList);
        Set<Long> groupIdSet = Arrays.stream(assessObjs).map(e->e.getLong("dimgroup.id")).collect(Collectors.toSet());
        TXHandle required = TX.required();
        try{
            // 删除测评对象下的维度测评人的指标打分实例数据
            ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.deleteScoreItemInsByAssObj(assObjIdsubList);
            // 删除测评对象及其维度测评人关系数据
            ASSESS_OBJ_ENTITY_SERVICE.delete(assObjIdsubList.toArray());
        }catch (Exception e) {
            LOG.error("deleteAssessObject 1 fail:", e);
            required.markRollback();
        } finally {
            required.close();
        }
    }
}
