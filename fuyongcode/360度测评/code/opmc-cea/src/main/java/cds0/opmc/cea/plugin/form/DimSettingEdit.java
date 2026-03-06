package cds0.opmc.cea.plugin.form;

import static cds0.opmc.cea.common.AppflgConstant.*;

import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.business.service.HandlerFindDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.RoleTypeConstants;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.DBServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DimSettingEdit extends HRDataBaseEdit implements BeforeF7SelectListener {
    private static final Log LOG = LogFactory.getLog(DimSettingEdit.class);
    private static final EvalDimSettingEntityService EVAL_DIM_SETTING_ENTITY_SERVICE = EvalDimSettingEntityService.getInstance();
    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();
    private  static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();

    private static final AssObjScoItemInstEntityService ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE = AssObjScoItemInstEntityService.getInstance();

    private static final HandlerFindDomainService HANDLER_FIND_DOMAIN_SERVICE = HandlerFindDomainService.getInstance();

    private static final DimSettingEntityService DIM_SETTING_ENTITY_SERVICE = DimSettingEntityService.getInstance();

    private static final AssessFormEntityService ASSESS_FORM_ENTITY_SERVICE = AssessFormEntityService.getInstance();

    private static Map<Long, Function<List<Long>,Map<Long, List<Long>>>> roleTypeHandlerMap;

    private static final String EVALDIMTOOLBAR = "advcontoolbarap";
    private static final String BTNTOOLBAR = "toolbarap";
    private static final String EVALROLE = "evalrole";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 分组基础资料字段监听
        BasedataEdit fieldEdit = this.getView().getControl(EVALROLE);
        fieldEdit.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        e.setCheckDataChange(false);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        String fileKey = beforeF7SelectEvent.getProperty().getName();
        if (HRStringUtils.isNotEmpty(fileKey) && EVALROLE.equals(fileKey)) {
            // 筛选角色类型使用环节为360测评的数据
            ListShowParameter formShowParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            formShowParameter.getListFilterParameter().getQFilters()
                    .add(new QFilter("processtype", QCP.equals, "G"));
        }
    }

    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        OperationStatus operationStatus = ((BaseShowParameter)getView().getFormShowParameter()).getStatus();
        if(OperationStatus.VIEW.equals(operationStatus)){
            // 查看态，维度设置工具栏隐藏
            getView().setVisible(Boolean.FALSE, EVALDIMTOOLBAR,BTNTOOLBAR);
        }
    }

    public void beforeDoOperation(BeforeDoOperationEventArgs beforeDoOperationEventArgs){
        super.beforeDoOperation(beforeDoOperationEventArgs);
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
                && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch(operateKey){
                case OP_KEY_CONFIRM:
                    if(OperationStatus.ADDNEW.equals(((BaseShowParameter)getView().getFormShowParameter()).getStatus())){
                        //新增分组：保存测评维度分录数据
                        saveEvalDimData();
                        // 把分组设置数据返回到测评活动父页面
                        getView().returnDataToParent(getModel().getDataEntity());
                        // 分组维度设置新增
                        getView().getParentView().getPageCache().put("entryop","add");
                    }else{
                        // 编辑分组：更新该分组下的维度数据
                        updateEvalDimData();
                        // 把分组设置数据返回到测评活动父页面
                        getView().returnDataToParent(getModel().getDataEntity());
                        getView().getParentView().getPageCache().put("entryop","edit");
                    }
                    getView().close();
                    break;
            }
        }
    }

    /**
     * 保存测评维度分录数据
     */
    private void saveEvalDimData(){
        // 分组ID生成
        long[] newDimSettingIds = DBServiceHelper.genLongIds("t_cea_dimsetting", HRBaseConstants.INT_ONE);
        // 设置分组ID
        getModel().getDataEntity().set(ID,newDimSettingIds[HRBaseConstants.INT_ZERO]);
        // 生成测评维度数据
        List<DynamicObject> newEvalDims = EVAL_DIM_SETTING_ENTITY_SERVICE.generateEvalDimSetting(getModel().getDataEntity().getDynamicObjectCollection(ENTRYENTITY),newDimSettingIds[HRBaseConstants.INT_ZERO]);
        // 保存测评维度数据
        EVAL_DIM_SETTING_ENTITY_SERVICE.save(newEvalDims.stream().toArray(DynamicObject[]::new));
    }

    /**
     * 更新维度数据
     */
    private void updateEvalDimData(){
        List<DynamicObject> updateList = new ArrayList<DynamicObject>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
       List<DynamicObject> deleteList = new ArrayList<DynamicObject>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
       List<DynamicObject> addList = new ArrayList<DynamicObject>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        DynamicObjectCollection newEvalDims = getModel().getDataEntity().getDynamicObjectCollection(ENTRYENTITY);
        Map<Long,DynamicObject> newEvalDimMap = newEvalDims.stream().collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
        Long dimsettingId = getModel().getDataEntity().getLong(ID);
        DynamicObject[] oldEvalDims = EVAL_DIM_SETTING_ENTITY_SERVICE.queryEvalDimByDimSettingId(dimsettingId);
        Arrays.stream(oldEvalDims).forEach(dim -> {
           if(newEvalDimMap.get(dim.getLong(ID)) != null){
               // update
               dim.set(DIMINDEX,newEvalDimMap.get(dim.getLong(ID)).get(DIMINDEX));
               dim.set(DIMNAME,newEvalDimMap.get(dim.getLong(ID)).get(DIMNAME));
               dim.set(NAME,newEvalDimMap.get(dim.getLong(ID)).get(DIMNAME));
               //dim.set(NUMBER,newEvalDimMap.get(dim.getLong(ID)).get(NUMBER));
               dim.set(DIMWEIGHT,newEvalDimMap.get(dim.getLong(ID)).get(DIMWEIGHT));
               dim.set(EVALROLE,newEvalDimMap.get(dim.getLong(ID)).getDynamicObject(EVALROLE).getLong(ID));
               dim.set(ENTRYID, dimsettingId);
               updateList.add(dim);
           }else{
               // delete
               deleteList.add(dim);
           }
       });
        // 筛选出新增的维度数据
       List<DynamicObject> addEvalDims = newEvalDims.stream().filter(e -> e.getLong(ID)==0).collect(Collectors.toList());
       addList.addAll(EVAL_DIM_SETTING_ENTITY_SERVICE.generateEvalDimSetting(addEvalDims,dimsettingId));
        TXHandle required = TX.required();
        try {
            // 更新
            // 针对更新的维度，重新计算并更新该分组下的所有测评对象在这些更新的维度上的维度测评人权重
            reCalulateAssObjAssesserWeigth(updateList);
            EVAL_DIM_SETTING_ENTITY_SERVICE.update(updateList.stream().toArray(DynamicObject[]::new));
            // 删除
            // 删除维度前，先删除维度对应的维度测评人关系数据及维度测评人对应的指标打分详情数据
            DIMASSESSER_ENTITY_SERVICE.delete(Arrays.stream(DIMASSESSER_ENTITY_SERVICE.queryEvalDims(deleteList.stream().map(e -> e.getLong(ID)).collect(Collectors.toList()))).map(e->e.getLong(ID)).collect(Collectors.toList()).toArray());
            EVAL_DIM_SETTING_ENTITY_SERVICE.delete(deleteList.stream().map(e -> e.getLong(ID)).collect(Collectors.toList()).toArray());
            // 新增
            DynamicObject[] newEvalDimObjs = Arrays.stream(EVAL_DIM_SETTING_ENTITY_SERVICE.save(addList.stream().toArray(DynamicObject[]::new))).toArray(DynamicObject[]::new);
            // 当前分组下的测评对象中 添加新增维度的维度测评人关系数据
            addEvalDimAssessr(newEvalDimObjs);
            // 生成测评对象下新增的维度测评人的指标打分实例数据
            generateDimAssesserIndScoreInstData();
        } catch (Exception e) {
            LOG.error("updateEvalDimData 1 fail:", e);
            required.markRollback();
        } finally {
            required.close();
        }
   }

    /**
     * 针对更新的维度，重新计算并更新该分组下的所有测评对象在这些更新的维度上的维度测评人权重
     * @param updateList
     */
    private void reCalulateAssObjAssesserWeigth(List<DynamicObject> updateList){
        Map<Long,DynamicObject> updateEvalDimMap = updateList.stream().collect(Collectors.toMap(k->k.getLong(ID), v->v));
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryToStartUpAssessObjInDimSetting(getModel().getDataEntity().getLong(ID));
        // 该分组下的所有测评对象下的需要更新的维度的维度测评人关系
        DynamicObject[] dimAssessers = DIMASSESSER_ENTITY_SERVICE.queryDimAsserAssObjEvalDims(Arrays.stream(assessObjs).map(e->e.getLong(ID)).collect(Collectors.toList()),
                updateList.stream().map(k->k.getLong(ID)).collect(Collectors.toList()));
        // 按测评对象分组
        Map<Long,List<DynamicObject>> assObjDimAsserMap = Arrays.stream(dimAssessers).collect(Collectors.groupingBy(k->k.getLong("assobj.id"), Collectors.toList()));
        assObjDimAsserMap.entrySet().stream().forEach(entry -> {
            // 当前测评对象下的维度测评人关系再按维度分组
            Map<Long,List<DynamicObject>> evalDimAsserMap = assObjDimAsserMap.get(entry.getKey()).stream().collect(Collectors.groupingBy(k->k.getLong("evaldim.id"),Collectors.toList()));
            evalDimAsserMap.entrySet().stream().forEach(evalDimAssers -> {
                // 当前维度下的维度测评人关系
                List<DynamicObject> currentEvalDimAssers = evalDimAssers.getValue();
                Double avg = Math.floor(updateEvalDimMap.get(evalDimAssers.getKey()).getBigDecimal("dimweight").doubleValue() / currentEvalDimAssers.size()); // 向下取整
                Double modval = updateEvalDimMap.get(evalDimAssers.getKey()).getBigDecimal("dimweight").doubleValue() % currentEvalDimAssers.size();
                // 当前测评对象的当前维度的维度测评人关系
                currentEvalDimAssers.stream().forEach(asser -> {
                    asser.set("dimweight", avg);
                });
                currentEvalDimAssers.get(HRBaseConstants.INT_ZERO).set("dimweight", avg+modval);
            });
        });
        DIMASSESSER_ENTITY_SERVICE.update(dimAssessers);
    }

    /**
     * 生成测评对象下新增的维度测评人的指标打分实例数据
     */
    private void generateDimAssesserIndScoreInstData(){
        List<Long> assObjDimAsserList = new ArrayList<>();
        DynamicObject dimGroup = DIM_SETTING_ENTITY_SERVICE.queryDimSettingByPk(getModel().getDataEntity().getLong(ID));
        DynamicObject assessForm = ASSESS_FORM_ENTITY_SERVICE.queryAssessFormByPk(dimGroup.getLong("assessform.id"));
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryToStartUpAssessObjInDimSetting(getModel().getDataEntity().getLong(ID));
        Arrays.stream(assessObjs).forEach(assessObj -> {
            assObjDimAsserList.addAll(assessObj.getDynamicObjectCollection(ENTRYENTITY).stream().map(dimasser -> dimasser.getLong(ID)).collect(Collectors.toList()));
        });
        // 已生成指标打分实例数据的维度测评人
        List<Long> dimAsserScoreInsList = Arrays.stream(ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.queryScoreItemInsAssobj(Arrays.stream(assessObjs).map(e->e.getLong(ID)).collect(Collectors.toList()))).
                map(k->k.getLong("entryid")).distinct().collect(Collectors.toList());
        // 从测评对象下的维度测评人中剔除掉已生成指标打分实例数据的维度测评人
        assObjDimAsserList.removeAll(dimAsserScoreInsList);
        // 需要生成指标打分实例数据的维度测评人
        DynamicObject[] dimAssessers = DIMASSESSER_ENTITY_SERVICE.loadDynamicObjectArray(assObjDimAsserList.toArray());
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
     * 添加维度测评人数据
     * @param newEvalDimObjs
     */
   private void addEvalDimAssessr(DynamicObject[] newEvalDimObjs){
       DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryToStartUpAssessObjInDimSetting(getModel().getDataEntity().getLong(ID));
       // 维度测评人类型 获取对应角色类型测评人
       Map<Long,Map<Long, List<Long>> > evalDimRoleTypeHandlerMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
       if(null == roleTypeHandlerMap || roleTypeHandlerMap.isEmpty()){
           roleTypeHandlerMap = initroleTypeHandlerMap();
       }

       Arrays.stream(newEvalDimObjs).forEach(evalDim -> {
           evalDimRoleTypeHandlerMap.put(evalDim.getLong("evalrole"), roleTypeHandlerMap.get(evalDim.getLong("evalrole")).
                   apply(Arrays.stream(assessObjs).map(e->e.getLong("person.id")).collect(Collectors.toList())));
       });

       Arrays.stream(assessObjs).forEach(assessObj -> {
           DynamicObjectCollection dimAsserEntry = assessObj.getDynamicObjectCollection(ENTRYENTITY);
           // 每个测评对象添加指定维度的维度测评人
            Arrays.stream(newEvalDimObjs).forEach(newEvalDim -> {
                List<Long> evalDimHandlers = evalDimRoleTypeHandlerMap.get(newEvalDim.getLong("evalrole")).get(assessObj.getLong("person.id")); // handlerSet.stream().collect(Collectors.toList());
                if(evalDimHandlers!=null){
                    //Double avgWeight = (evalDim.getBigDecimal("dimweight").doubleValue() / evalDimHandlers.size());
                    Double avg = Math.floor(newEvalDim.getBigDecimal("dimweight").doubleValue() / evalDimHandlers.size()); // 向下取整
                    Double modval = newEvalDim.getBigDecimal("dimweight").doubleValue() % evalDimHandlers.size();
                    int index = 0;
                    for (Long handler : evalDimHandlers) {
                        DynamicObject dimAssesserDy = dimAsserEntry.addNew();
                        if (index == 0) {
                            dimAssesserDy.set("dimweight", avg + modval);
                        } else {
                            dimAssesserDy.set("dimweight", avg);
                        }
                        dimAssesserDy.set("assesser", handler);
                        dimAssesserDy.set("evaldim", newEvalDim);
                        dimAssesserDy.set("dimname",newEvalDim.getString("dimname"));
                        dimAssesserDy.set("assesstatus", DimAssesserStatusEnum.UNFILLED.getValue());
                        dimAssesserDy.set("groupdim", assessObj.getLong("dimgroup.id")); // 分组
                        dimAssesserDy.set("assobj", assessObj);   // 测评对象
                        index++;
                    }
                }else{
                    // 没找到处理人，则不添加维度测评人关系
                }
            });
       });
       // 保存测评对象 新增的维度测评人关系
       ASSESS_OBJ_ENTITY_SERVICE.save(assessObjs);
    }

    private Map<Long, Function<List<Long>,Map<Long, List<Long>>>> initroleTypeHandlerMap(){
        Map<Long,Function<List<Long>,Map<Long, List<Long>>>> roleTypeHandlerMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        //直接上级
        roleTypeHandlerMap.put(RoleTypeConstants.DIRECT_SUPERIOR, handerParamsBo-> HANDLER_FIND_DOMAIN_SERVICE.getDirectSuperiorIdsByPersonIds(handerParamsBo));
        //同级同事
        roleTypeHandlerMap.put(RoleTypeConstants.PEER_COLLEGE, handerParamsBo-> HANDLER_FIND_DOMAIN_SERVICE.getPeerColegeIdsByPersonIds(handerParamsBo));
        //直接下级
        roleTypeHandlerMap.put(RoleTypeConstants.DIRECT_SUBORDINATE, handerParamsBo-> HANDLER_FIND_DOMAIN_SERVICE.getDirectChildrenIdsByPersonIds(handerParamsBo));
        return roleTypeHandlerMap;
    }

}
