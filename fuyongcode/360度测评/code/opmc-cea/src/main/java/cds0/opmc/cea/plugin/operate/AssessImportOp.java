package cds0.opmc.cea.plugin.operate;

import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.validate.AbstractValidator;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRObjectUtils;
import kd.hr.hbp.opplugin.web.HRDataBaseOp;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.ENTRYENTITY;
import static cds0.opmc.cea.common.AppflgConstant.ID;

public class AssessImportOp extends HRDataBaseOp {
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final AssessFormEntityService ASSESS_FORM_ENTITY_SERVICE = AssessFormEntityService.getInstance();
    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();
    private static final EvalDimSettingEntityService EVAL_DIM_SETTING_ENTITY_SERVICE = EvalDimSettingEntityService.getInstance();
    private static final UserEntityService USER_ENTITY_SERVICE = UserEntityService.getInstance();

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        List<String> fieldKeys = e.getFieldKeys();
        fieldKeys.add("entryentity");
        fieldKeys.add("entryentity.dimweight");
        fieldKeys.add("entryentity.evaldim");
        fieldKeys.add("entryentity.assesser");
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs args) {
        args.addValidator(new AbstractValidator() {
            @Override
            public void validate() {
                ExtendedDataEntity[] dataEntities = this.getDataEntities();
                for (ExtendedDataEntity dataEntity : dataEntities) {
                    DynamicObject data = dataEntity.getDataEntity();
                    DynamicObjectCollection entryEntity = data.getDynamicObjectCollection("entryentity");
                    if (entryEntity != null && !entryEntity.isEmpty()) {
                        // 校验测评人 工号+姓名是否可以找到相应的人
                        entryEntity.stream().forEach(asser -> {
                            DynamicObject user = USER_ENTITY_SERVICE.queryUserByNumber(asser.getString("assesser.number"));
                            if(user == null){
                                this.addMessage(dataEntity, ResManager.loadKDString("工号：{0}，找不到人员。", "AssessImportOp_1", AppflgConstant.KEY_APP_NAME, asser.getString("assesser.number")));
                            }
                        });

                        // 按维度分组
                        Map<Long, List<DynamicObject>> currEvalDimMap = entryEntity.stream().collect(Collectors.groupingBy(k -> k.getLong("evaldim.id"), Collectors.toList()));
                        Map<Long, DynamicObject> evalDimMap = Arrays.stream(EVAL_DIM_SETTING_ENTITY_SERVICE.loadDynamicObjectArray(entryEntity.stream().map(e -> e.getLong("evaldim.id")).distinct().toArray())).collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
                        currEvalDimMap.forEach((key, value) -> {
                            // 判断当前维度的维度测评人权重之和是否不等于维度权重
                            BigDecimal dimweight = value.stream().map(e -> e.getBigDecimal("dimweight")).reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal assesserdimweight = BigDecimal.ZERO;
                            DynamicObject dynamicObject = evalDimMap.get(key);
                            if (dynamicObject != null) {
                                assesserdimweight = dynamicObject.getBigDecimal("dimweight");
                            }
                            if (assesserdimweight.compareTo(dimweight) != 0) {
                                this.addMessage(dataEntity, ResManager.loadKDString("测评对象:{0},维度:{1}下的维度测评人权重之和不等于该维度的权重。", "AssessImportOp_0", AppflgConstant.KEY_APP_NAME, data.getString("perffile.name"), evalDimMap.get(key).getString("dimname")));
                            }
                            // 同一个维度可能存在设置相同多个测评人
                            List<DynamicObject> collect = value.stream().filter(e -> e.getLong("assesser.id") != 0L).collect(Collectors.toList());
                            if (collect.stream().map(e -> e.getLong("assesser.id")).collect(Collectors.toSet()).size() != collect.size()) {
                                this.addMessage(dataEntity, ResManager.loadKDString("测评对象:{0},维度:{1}下存在相同的测评人", "AssessImportOp_1", AppflgConstant.KEY_APP_NAME, data.getString("perffile.name"), dynamicObject.getString("dimname")));
                            }
                        });
                        for (DynamicObject dynamicObject : entryEntity) {
                            if (!HRObjectUtils.isEmpty(dynamicObject.getDynamicObject("evaldim"))) {
                                dynamicObject.set("dimname", dynamicObject.getString("evaldim.name"));
                            }
                        }
                    }
                }

            }

        });
    }

    /**
     * 在数据入库之后处理 新增测评人，生成指标打分实例数据
     * @param args
     */
    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs args){
        super.afterExecuteOperationTransaction(args);
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(Arrays.stream(args.getDataEntities()).map(e->e.getLong("id")).collect(Collectors.toList()));
        DynamicObject[] assessForms = ASSESS_FORM_ENTITY_SERVICE.queryAssessFormByPks(Arrays.stream(assessObjs).map(e->e.getLong("dimgroup.assessform.id")).distinct().collect(Collectors.toList()));
        Map<Long,DynamicObject> assessFormMap = Arrays.stream(assessForms).collect(Collectors.toMap(k->k.getLong(AppflgConstant.ID), v -> v));
        Arrays.stream(assessObjs).forEach(assessObj -> {
            DynamicObjectCollection dimAssessers = assessObj.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY);
            // 需要生成指标打分实例数据的维度测评人
            List<DynamicObject> dimAssesserNeedGen = Arrays.stream(DIMASSESSER_ENTITY_SERVICE.loadDynamicObjectArray(dimAssessers.stream().map(e->e.getLong(AppflgConstant.ID)).collect(Collectors.toList()).toArray())).filter(dimAsser -> dimAsser.getDynamicObjectCollection(AppflgConstant.ENTRYENTITY).size() == HRBaseConstants.INT_ZERO).collect(Collectors.toList());
            dimAssesserNeedGen.stream().forEach(dimasser -> {
                dimasser.set("assesstatus", DimAssesserStatusEnum.UNFILLED.getValue());
                dimasser.set("groupdim", assessObj.getLong("dimgroup.id"));
                dimasser.set("assobj", assessObj.getLong(AppflgConstant.ID));
                // 生成该维度测评人的指标打分实例分录数据
                DynamicObjectCollection scoreInstEntry = dimasser.getDynamicObjectCollection(ENTRYENTITY);
                // 更具当前维度测评人所属分组，对应的测评表下的指标分录表，生成指标打分实例分录数据
                assessFormMap.get(assessObj.getLong("dimgroup.assessform.id")).getDynamicObjectCollection(ENTRYENTITY).stream().forEach(indicator -> {
                    DynamicObject indicatorInstDy = scoreInstEntry.addNew();
                    indicatorInstDy.set("assessobj", assessObj.getLong(AppflgConstant.ID));
                    indicatorInstDy.set("assessformrow", indicator.getLong(ID));
                    indicatorInstDy.set("indicatorweight", indicator.getBigDecimal("indicatorweight"));
                });
            });
            // 保存
            DIMASSESSER_ENTITY_SERVICE.save(dimAssesserNeedGen.stream().toArray(DynamicObject[]::new));
        });
    }

}
