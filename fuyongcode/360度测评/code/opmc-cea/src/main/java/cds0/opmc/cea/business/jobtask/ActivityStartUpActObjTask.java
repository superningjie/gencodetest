package cds0.opmc.cea.business.jobtask;

import cds0.opmc.cea.business.bo.AssessTaskBO;
import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.entityservice.AssessTaskEntityService;
import cds0.opmc.cea.business.service.AssessTaskDomainService;
import cds0.opmc.cea.business.service.MessageService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import com.google.common.collect.Lists;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
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

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ActivityStartUpActObjTask extends AbstractTask {
    private static final ThreadPool THREAD_POOL = ThreadPools.newCachedThreadPool("ActivityStartUpActObjTask#&", 3, 5);
    private static final Semaphore SEMAPHORE = new Semaphore(5);
    private static final Log LOG = LogFactory.getLog(ActivityStartUpActObjTask.class);
    private static final String ACTIVITY_START_UP_LOCK = "opmc/datalock/activity/startup/";
    private static final int PRECREATEINST_MQ_PERCOUNT = 200;
    private static final String SUCCESS_LIST = "success_list";
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final AssessTaskDomainService ASSESS_TASK_DOMAIN_SERVICE = AssessTaskDomainService.getInstance();
    private static final AssessTaskEntityService ASSESS_TASK_ENTITY_SERVICE = AssessTaskEntityService.getInstance();
    private static final MessageService MESSAGE_SERVICE = MessageService.getInstance();
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        if (map.containsKey(AppflgConstant.ACTIVITY_ID)) {
            try (DLock lock = DLock.create(ACTIVITY_START_UP_LOCK + map.get(AppflgConstant.ACTIVITY_ID))) {
                lock.lock();
                String desc = ResManager.loadKDString("正在为您自动启动评估对象...",
                        "ActivityStartUpActObjTask_0", AppflgConstant.KEY_APP_NAME);
                this.feedbackProgress(20, desc, null);
                // 测评对象ID 分批切割
                List<List<Long>> assessObjIds = Lists.partition((List) map.get(SUCCESS_LIST), PRECREATEINST_MQ_PERCOUNT );
                int count = assessObjIds.size();
                CountDownLatch atomicInteger = new CountDownLatch(count);
                for(int i = 0; i < count; i++){
                    List<Long> assObjIdsubList = assessObjIds.get(i);
                    int pecent = (i + 1)/count * 90;
                    batchStartUpAssessObject(assObjIdsubList,desc,pecent,atomicInteger);
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

    private void batchStartUpAssessObject(List<Long> assObjIdsubList, String desc, int pecent, CountDownLatch atomicInteger) {
        try {
            LOG.info("==================start batchsaveEvaluationObject==================");
            SEMAPHORE.acquire();
            LOG.info("==================acquire semaphore batchsaveEvaluationObject==================");
            THREAD_POOL.execute(() -> {
                try {
                    startupAssessObject(assObjIdsubList,desc);
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

    private void startupAssessObject(List<Long> assObjIdsubList,String desc){
        // 将要处理的测评对象
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(assObjIdsubList);
        Map<Long,DynamicObject> assessObjMap = Arrays.stream(assessObjs).collect(Collectors.toMap(k->k.getLong(AppflgConstant.ID), V->V));
        // 按照分组进行分组
        Map<Long,List<DynamicObject>> groupAssessObjMap = Arrays.stream(assessObjs).collect(Collectors.groupingBy(k->k.getLong("dimgroup.id"), Collectors.toList()));
        groupAssessObjMap.entrySet().stream().forEach(entry -> {
            List<DynamicObject> toSendAssessTasks =new ArrayList<DynamicObject>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
            // 处理每一个分组
            entry.getValue().stream().forEach(assObj -> {
                // 当前测评对象下的维度测评人关系
                DynamicObjectCollection dimAssessers = assObj.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY);
                // 该测评对象的所有维度测评顺序集合
                Set<Integer> evalDimIndexSet = dimAssessers.stream().map(e->e.getInt("evaldim.dimindex")).collect(Collectors.toSet());
                if(evalDimIndexSet.size() == HRBaseConstants.INT_ONE){
                    // 所有维度测评顺序一致，则同时生成测评任务同时发送消息
                    toSendAssessTasks.addAll(assessTaskCreate(assObj,dimAssessers.stream().collect(Collectors.toList())));
                }else{
                    // 取当前最小的测评顺序的维度生成测评任务并发送消息
                    Integer min = evalDimIndexSet.stream().collect(Collectors.toList()).stream().min(Comparator.comparing(x -> x)).orElse(null);
                    // 准备生成发送测评任务的维度测评人关系
                    List<DynamicObject> toSendDimAssessers = dimAssessers.stream().filter(e -> e.getInt("evaldim.dimindex") == min).collect(Collectors.toList());
                    toSendAssessTasks.addAll(assessTaskCreate(assObj,toSendDimAssessers.stream().collect(Collectors.toList())));
                }
                // 设置测评对象测评状态为测评中
                assObj.set("assesstaus", AssessStatusEnum.ASSESSINGIN.getValue());
            });
            TXHandle required = TX.required();
            try{
                // 测评任务落库
                ASSESS_TASK_ENTITY_SERVICE.save(toSendAssessTasks.stream().toArray(DynamicObject[]::new));
                // 发送该分组下的测评任务
                toSendAssessTasks.stream().collect(Collectors.groupingBy(assessTask -> assessTask.getLong("evaldim"))).entrySet().stream().forEach(task -> {
                    task.getValue().stream().collect(Collectors.groupingBy(item -> item.getLong("assesserperson"))).entrySet().stream().forEach(x -> MESSAGE_SERVICE.sendAssessTaskMessage(x.getValue(), assessObjMap.get(x.getValue().get(0).getLong("assessobj"))));
                });
//                toSendAssessTasks.stream().collect(Collectors.groupingBy(task -> task.getLong("assessobj"), Collectors.toList())).entrySet().stream().forEach(sendTasks -> {
//                    MESSAGE_SERVICE.sendAssessTaskMessage(sendTasks.getValue(),assessObjMap.get(sendTasks.getKey()));
//                });
                //MESSAGE_SERVICE.sendAssessTaskMessage(toSendAssessTasks,entry.getValue().get(HRBaseConstants.INT_ZERO));
                // 更新测评对象
                ASSESS_OBJ_ENTITY_SERVICE.update(entry.getValue().stream().toArray(DynamicObject[]::new));
            }catch (Exception e) {
                LOG.error("startupAssessObject 1 fail:", e);
                required.markRollback();
            } finally {
                required.close();
            }
        });
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
            if(dimAsser.getLong("assesser.id") != 0L){
                // 测评人不为空才生成测评任务
                assessTaskBOList.add(bo);
            }
        });
        return ASSESS_TASK_DOMAIN_SERVICE.createAssessTask(assessTaskBOList);
    }
}
