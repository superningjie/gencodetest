package cds0.opmc.cea.plugin.operate;

import cds0.opmc.cea.business.entityservice.PerformanceLevelEntityService;
import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.opplugin.web.HRDataBaseOp;

import java.math.BigDecimal;
import java.util.List;


public class AssessForImportOp extends HRDataBaseOp {
    private static final PerformanceLevelEntityService PERFORMANCE_LEVEL_SERVICE = PerformanceLevelEntityService.getInstance();
    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        List<String> fieldKeys = e.getFieldKeys();
        fieldKeys.add("indicatorweight");
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs args) {
        args.addValidator(new AbstractValidator() {
            @Override
            public void validate() {
                ExtendedDataEntity[] dataEntities = this.getDataEntities();
                for (ExtendedDataEntity dataEntity : dataEntities) {
                    DynamicObject data = dataEntity.getDataEntity();
                    QFilter qFilter = new QFilter("number", QCP.equals, data.getString("levelmap")).and("iscurrentversion", QCP.equals, "1");
                    DynamicObject query = PERFORMANCE_LEVEL_SERVICE.queryOne("scoremapentryentity.scoresystem,scoremapentryentity.levelscoremap,scoresubentryentity.scorelevel,scoresubentryentity.defaultscore,scoresubentryentity.minscore,scoresubentryentity.maxscore", new QFilter[]{qFilter});
                    DynamicObjectCollection dynamicObjectCollection = (DynamicObjectCollection) query.get("scoremapentryentity");
                    if(dynamicObjectCollection.isEmpty()){
                        this.addErrorMessage(dataEntity, ResManager.loadKDString("绩效等级：{0}的绩效等级与分制映射配置未配置", "AssessForImportOp_0", AppflgConstant.KEY_APP_NAME, data.getString("levelmap")));
                    }
                    if(data.getInt("indicatorscore") == HRBaseConstants.INT_ZERO && data.getBigDecimal("indicatorweight").doubleValue() == 0.00){
                        this.addErrorMessage(dataEntity, ResManager.loadKDString("分值/权重请任选其一导入", "AssessForImportOp_1", AppflgConstant.KEY_APP_NAME));
                    }
                    if(data.getInt("indicatorscore") != HRBaseConstants.INT_ZERO && data.getBigDecimal("indicatorweight").doubleValue() != 0.00){
                        this.addErrorMessage(dataEntity, ResManager.loadKDString("分值/权重请任选其一导入", "AssessForImportOp_1", AppflgConstant.KEY_APP_NAME));
                    }
                    if (BigDecimal.ZERO.compareTo(data.getBigDecimal("indicatorweight")) == 0) {
                        data.set("indicatorweight", null);
                    }
                }
            }
    });
}
}
