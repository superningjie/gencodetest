package cds0.opmc.cea.business.jobtask;

import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.business.service.DimassesserDomainService;
import cds0.opmc.cea.business.service.HandlerFindDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.RoleTypeConstants;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
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
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.threads.ThreadPool;
import kd.bos.threads.ThreadPools;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.opmc.pmd.business.application.service.PerffileApplicationService;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.ENTRYENTITY;
import static cds0.opmc.cea.common.AppflgConstant.ID;

public class ActivityAddActObjTask extends AbstractTask {
    private static final ThreadPool THREAD_POOL = ThreadPools.newCachedThreadPool("ActivityAddActObjTask#&", 3, 5);
    private static final Semaphore SEMAPHORE = new Semaphore(5);
    private static final Log LOG = LogFactory.getLog(ActivityAddActObjTask.class);
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();
    private static final PerffileApplicationService PERF_FILE_APPLICATION_SERVICE = new PerffileApplicationService();
    private static final DimSettingEntityService DIM_SETTING_ENTITY_SERVICE = DimSettingEntityService.getInstance();
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final DimassesserDomainService DIMASSESSER_DOMAIN_SERVICE = DimassesserDomainService.getInstance();
    private static final AssessFormEntityService ASSESS_FORM_ENTITY_SERVICE = AssessFormEntityService.getInstance();
    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();
    private static final HandlerFindDomainService HANDLER_FIND_DOMAIN_SERVICE = HandlerFindDomainService.getInstance();
    private static final AsserSeekConfEntityService asserSeekConfEntityService = AsserSeekConfEntityService.getInstance();


    private static Map<Long,Function<List<Long>,Map<Long, List<Long>>>> roleTypeHandlerMap;
    /**
     * 测评活动启动分布式锁颗粒度
     */
    private static final String ACTIVITY_START_UP_LOCK = "opmc/datalock/activity/addobj/";
    private static final int PRECREATEINST_MQ_PERCOUNT = 200;
    private static final String SUCCESS_LIST = "success_list";
    private static final String STARTDATE = "startdate";
    private static final String ENDDATE = "enddate";
    private static final String PMDORG = "pmdorg";
    private static final String DIMGROUP = "dimGroup";

    public void feedbackProgress(int progress, String desc) {
        super.feedbackProgress(progress, desc, null);
    }

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        if (map.containsKey(AppflgConstant.ACTIVITY_ID)) {
            try (DLock lock = DLock.create(ACTIVITY_START_UP_LOCK + map.get(AppflgConstant.ACTIVITY_ID))) {
                lock.lock();
                String desc = ResManager.loadKDString("正在为您自动添加评估对象...",
                        "ActivityAddActObjTask_0", AppflgConstant.KEY_APP_NAME);
                long assessActivityPkId = Long.parseLong(String.valueOf(map.get(AppflgConstant.ACTIVITY_ID)));
                // 测评活动
                DynamicObject activityDynObj = ASSESS_ACTIVITY_ENTITY_SERVICE.queryOne(assessActivityPkId);
                // 分组
                DynamicObject dimGroup = DIM_SETTING_ENTITY_SERVICE.queryDimSettingByPk(Long.parseLong(String.valueOf(map.get(DIMGROUP))));
                this.feedbackProgress(20, desc, null);
                List<List<Long>> perfileIds = Lists.partition((List) map.get(SUCCESS_LIST), PRECREATEINST_MQ_PERCOUNT );
                int count = perfileIds.size();
                CountDownLatch atomicInteger = new CountDownLatch(count);
                for(int i = 0; i < count; i++){
                    List<Long> perfileIdsubList = perfileIds.get(i);
                    int pecent = (i + 1)/count * 90;
                    batchsaveAssessObject(perfileIdsubList,activityDynObj,dimGroup,desc,pecent,atomicInteger);
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

    private void batchsaveAssessObject(List<Long> perfileIds, DynamicObject activityDynObj, DynamicObject dimGroup, String desc,int pecent,  CountDownLatch atomicInteger) {
        try {
            LOG.info("==================start batchsaveEvaluationObject==================");
            SEMAPHORE.acquire();
            LOG.info("==================acquire semaphore batchsaveEvaluationObject==================");
            THREAD_POOL.execute(() -> {
                try {
                    saveAssessObject(perfileIds,activityDynObj,dimGroup,desc);
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

    /**
     * 保存测评对象
     * @param perfileIds
     * @param activityDynObj
     * @param dimGroup
     * @param desc
     */
    private void saveAssessObject(List<Long> perfileIds, DynamicObject activityDynObj, DynamicObject dimGroup, String desc) {
        String selectors = "person,empposrel,employee,affiliateadminorg,startdate,enddate,pmdorg";
        DynamicObject[] assessObjectJoinInCol = PERF_FILE_APPLICATION_SERVICE.getPerffileListByIds(selectors,perfileIds);
        DynamicObjectCollection assessObjectJoinInList = new DynamicObjectCollection();
        Arrays.stream(assessObjectJoinInCol).forEach(data -> assessObjectJoinInList.add(data));
        this.feedbackProgress(40, desc, null);
        saveAssessObjectAndDimAssesserData(activityDynObj,dimGroup,assessObjectJoinInList,desc);
    }

    /**
     * 保存测评对象及生成维度测评人数据
     * @param curActivityDynObj
     * @param assessObjectJoinInList
     * @param desc
     */
    private void saveAssessObjectAndDimAssesserData(DynamicObject curActivityDynObj,DynamicObject dimGroup, DynamicObjectCollection assessObjectJoinInList, String desc) {
        // 维度测评人类型 获取对应角色类型测评人
        Map<Long,Map<Long, List<Long>> > evalDimRoleTypeHandlerMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        // 添加的测评对象平台人ID
        List<Long> personIdList = assessObjectJoinInList.stream().map(assJoinObj -> assJoinObj.getLong("person.id")).distinct().collect(Collectors.toList());
        if(null == roleTypeHandlerMap || roleTypeHandlerMap.isEmpty()){
            roleTypeHandlerMap = initroleTypeHandlerMap();
        }
        // 根据当前分组下各个维度的测评人角色类型 查找对应的测评人信息
        dimGroup.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY).stream().forEach(evalDim -> {
            evalDimRoleTypeHandlerMap.put(evalDim.getLong("evalrole.id"), roleTypeHandlerMap.get(evalDim.getLong("evalrole.id")).apply(personIdList));
        });
        // 生成添加的测评对象
        List<DynamicObject> assessObjDys = ASSESS_OBJ_ENTITY_SERVICE.generateAssessObj(curActivityDynObj,dimGroup,assessObjectJoinInList);
        // 生成测评对象下的维度测评人关系数据
        assessObjDys.stream().forEach(assObjDy -> {
            generateDimAssesserEntrys(assObjDy, dimGroup, dimGroup.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY),
                    assObjDy.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY), evalDimRoleTypeHandlerMap);
        });
        TXHandle required = TX.required();
        try{
            // 保存测评对象
            DynamicObject[] assessObjs = Arrays.stream(ASSESS_OBJ_ENTITY_SERVICE.save(assessObjDys.stream().toArray(DynamicObject[]::new))).toArray(DynamicObject[]::new);
            // 生成维度测评人 指标打分实例分录数据
            generateDimAssesserIndScoreInstData(assessObjs,dimGroup);
        }catch (Exception e) {
            LOG.error("startupAssessObject 1 fail:", e);
            required.markRollback();
        } finally {
            required.close();
        }
    }

    /**
     * 生成维度测评人 指标打分实例分录数据
     * @param assessObjs
     * @param dimGroup
     */
    private void generateDimAssesserIndScoreInstData(DynamicObject[] assessObjs, DynamicObject dimGroup){
        List<Long> assesserIdList = new ArrayList<Long>();
        DynamicObject assessForm = ASSESS_FORM_ENTITY_SERVICE.queryAssessFormByPk(dimGroup.getLong("assessform.id"));
        Arrays.stream(assessObjs).forEach(assessObj -> {
            assesserIdList.addAll(assessObj.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY).stream().map(dimasser->dimasser.getLong(ID)).collect(Collectors.toList()));
        });
        DynamicObject[] dimAssessers = DIMASSESSER_ENTITY_SERVICE.loadDynamicObjectArray(assesserIdList.toArray());
        // 生成该维度测评人的指标打分实例分录数据
        Arrays.stream(dimAssessers).forEach(dimasser -> {
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
        DIMASSESSER_ENTITY_SERVICE.save(dimAssessers);
    }
    /**
     * 生成该分组下的维度测评人关系分录数据
     * @param evalDimEntry
     * @param dimAssesserEntry
     */
    private void generateDimAssesserEntrys(DynamicObject assObjDy, DynamicObject dimGroup, DynamicObjectCollection evalDimEntry, DynamicObjectCollection dimAssesserEntry, Map<Long,Map<Long, List<Long>> > evalDimRoleTypeHandlerMap){
        evalDimEntry.stream().forEach(evalDim -> {
            // 当前维度，根据测评人角色获取对应查找到的测评人，生成维度测评人关系
            //Set<Long> handlerSet = new HashSet<Long>(Arrays.asList(1957383068159312896L,1959408898553151488L,1959409173640774656L,1958693968615047168L));
            List<Long> evalDimHandlers = evalDimRoleTypeHandlerMap.get(evalDim.getLong("evalrole.id")).get(assObjDy.getLong("person.id")); // handlerSet.stream().collect(Collectors.toList());

            DynamicObject[] asserSeekConf = asserSeekConfEntityService.query("seektype, entryentity, entryentity.perforg, entryentity.orgnumber", new QFilter[] {
//                    new QFilter(HRBaseConstants.STATUS, QCP.equals, "C"),
                    new QFilter("enable", QCP.equals, "1")
            });
            boolean check = true;
            if (asserSeekConf != null && asserSeekConf.length > 0) {
                String seekType = asserSeekConf[0].getString("seektype");
                DynamicObjectCollection orgList = asserSeekConf[0].getDynamicObjectCollection("entryentity");
                List<Long> orgIdList = orgList.stream().map(item -> item.getLong("perforg_id")).collect(Collectors.toList());
                Long a = assObjDy.getLong("assessact.adminorg.id");
                if (orgIdList.contains(assObjDy.getLong("assessact.adminorg.id"))) {
                    //如果在配置中，且配置为不找人，则改为false
                    if (seekType.equals("20")) {
                        check = false;
                    }
                } else {
                    //如果不在配置中，且配置为找人，则改为false
                    if (seekType.equals("10")) {
                        check = false;
                    }
                }
            }
            if(check && evalDimHandlers != null && !evalDimHandlers.isEmpty()){
                //Double avgWeight = (evalDim.getBigDecimal("dimweight").doubleValue() / evalDimHandlers.size());
                Double avg = Math.floor(evalDim.getBigDecimal("dimweight").doubleValue() / evalDimHandlers.size()); // 向下取整
                Double modval = evalDim.getBigDecimal("dimweight").doubleValue() % evalDimHandlers.size();
                int index = 0;
                for (Long handler : evalDimHandlers) {
                    DynamicObject dimAssesserDy = dimAssesserEntry.addNew();
                    if (index == 0) {
                        dimAssesserDy.set("dimweight", avg + modval);
                    } else {
                        dimAssesserDy.set("dimweight", avg);
                    }
                    dimAssesserDy.set("assesser", handler);
                    dimAssesserDy.set("evaldim", evalDim);
                    dimAssesserDy.set("dimname", evalDim.getString("dimname"));
                    dimAssesserDy.set("assesstatus", DimAssesserStatusEnum.UNFILLED.getValue());
                    dimAssesserDy.set("groupdim", dimGroup); // 分组
                    dimAssesserDy.set("assobj", assObjDy);   // 测评对象
                    index++;
                }
            }else{
                // 没找到处理人，在该维度下生成一条测评人为空的维度测评人数据
                DynamicObject dimAssesserDy = dimAssesserEntry.addNew();
                dimAssesserDy.set("dimweight", evalDim.getBigDecimal("dimweight"));
                dimAssesserDy.set("assesser", 0L);
                dimAssesserDy.set("evaldim", evalDim);
                dimAssesserDy.set("dimname", evalDim.getString("dimname"));
                dimAssesserDy.set("assesstatus", DimAssesserStatusEnum.UNFILLED.getValue());
                dimAssesserDy.set("groupdim", dimGroup); // 分组
                dimAssesserDy.set("assobj", assObjDy);   // 测评对象
            }
        });

    }

    private Map<Long, Function<List<Long>,Map<Long, List<Long>>>> initroleTypeHandlerMap(){
        Map<Long,Function<List<Long>,Map<Long, List<Long>>>> roleTypeHandlerMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        //直接上级
        roleTypeHandlerMap.put(RoleTypeConstants.DIRECT_SUPERIOR, handerParamsBo-> HANDLER_FIND_DOMAIN_SERVICE.getDirectSuperiorIdsByPersonIds(handerParamsBo));
        //同级同事
        roleTypeHandlerMap.put(RoleTypeConstants.PEER_COLLEGE, handerParamsBo-> HANDLER_FIND_DOMAIN_SERVICE.getPeerColegeIdsByPersonIds(handerParamsBo));
        //直接下级
        roleTypeHandlerMap.put(RoleTypeConstants.DIRECT_SUBORDINATE, handerParamsBo-> HANDLER_FIND_DOMAIN_SERVICE.getDirectChildrenIdsByPersonIds(handerParamsBo));
        //本人
        roleTypeHandlerMap.put(RoleTypeConstants.PERSON_SELF, handerParamsBo-> HANDLER_FIND_DOMAIN_SERVICE.getPersonSelfByPersonIds(handerParamsBo));

        return roleTypeHandlerMap;
    }
}
