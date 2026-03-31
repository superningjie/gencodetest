package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessFormEntityService;
import cds0.opmc.cea.business.entityservice.AssessFormRowEntityService;
import cds0.opmc.cea.business.entityservice.DimSettingEntityService;
import cds0.opmc.cea.business.entityservice.PerformanceLevelEntityService;
import cds0.opmc.cea.business.service.DimassesserDomainService;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.IFormView;
import kd.bos.mvc.SessionManager;
import kd.bos.org.utils.DynamicObjectUtils;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.DBServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.impt.common.plugin.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AssessFormFixImportPlugin implements HRImportPlugin {
    private static final AssessFormEntityService ASSESS_FORM_ENTITY_SERVICE = AssessFormEntityService.getInstance();
    private static final AssessFormRowEntityService ASSESS_FORM_ROW_ENTITY_SERVICE = AssessFormRowEntityService.getInstance();
    private static final DimSettingEntityService DIM_SETTING_ENTITY_SERVICE = DimSettingEntityService.getInstance();
    private static final DimassesserDomainService DIMASSESSER_DOMAIN_SERVICE = DimassesserDomainService.getInstance();
    private static final PerformanceLevelEntityService PERFORMANCE_LEVEL_SERVICE = PerformanceLevelEntityService.getInstance();

    @Override
    public void afterLoadStartPage(AfterLoadStartPageEventArgs args) {
        HRImportPlugin.super.afterLoadStartPage(args);
        args.setFormId("cds0_cea_assessrowimport");
        args.setExtParams(args.getPageId());
    }

    @Override
    public void afterConvertDynamicObjects(AfterConvertDynamicObjectsEventArgs args) {
        HRImportPlugin.super.afterConvertDynamicObjects(args);
    }

    @Override
    public void beforeCallOperation(BeforeCallOperationEventArgs args) {
        HRImportPlugin.super.beforeCallOperation(args);
        Object[] dynamicObjects = args.getDynamicObjects();
        List<Long> ids = new ArrayList<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        for (Object object : dynamicObjects) {
            ids.add(((DynamicObject) object).getLong(HRBaseConstants.ID));
        }
        ConcurrentHashMap<String, Object> customParams = args.getCustomParams();
        if (customParams == null) {
            customParams = new ConcurrentHashMap<String, Object>(HRBaseConstants.INT_CAPACITY);
        }
        IFormView view = SessionManager.getCurrent().getViewNoPlugin(args.getExtParams());
        if (view != null) {
            DynamicObject dataEntity = view.getModel().getDataEntity();
            customParams.putIfAbsent("ids", ids);
            customParams.putIfAbsent("groupselect", dataEntity.getLong("groupselect.id"));
            //customParams.putIfAbsent("perflevel.number", dataEntity.getString("cds0_perflevel.number"));
            customParams.putIfAbsent("name", dataEntity.getString("name"));
        }
    }

    @Override
    public void afterCallOperation(AfterCallOperationEventArgs args) {
        HRImportPlugin.super.afterCallOperation(args);
        OperationResult result = args.getResult();
        if (result != null && result.isSuccess()) {
            ConcurrentHashMap<String, Object> customParams = args.getCustomParams();
            if (customParams != null) {
                Object groupselect = customParams.get("groupselect");
                //Object number = customParams.get("perflevel.number");
                Object name = customParams.get("name");
                if (groupselect == null || name == null) {  // || number == null
                    return;
                }
                DynamicObject dynamicObject = ASSESS_FORM_ENTITY_SERVICE.generateEmptyDynamicObject();
                long[] newDimIds = DBServiceHelper.genLongIds("t_cea_assessformfix", HRBaseConstants.INT_ONE);
                dynamicObject.set(HRBaseConstants.ID, newDimIds[0]);
                dynamicObject.set(HRBaseConstants.NAME, name);
                //dynamicObject.set("levelmapnumber", number);
                dynamicObject.set("dimgroup", groupselect);
                dynamicObject.set(HRBaseConstants.ENABLE, HRBaseConstants.ENABLED);
                DynamicObjectCollection entryEntity = dynamicObject.getDynamicObjectCollection("entryentity");
                Object ids = customParams.get("ids");
                if (ids != null) {
                    List<Long> longList = (List<Long>) ids;
                    DynamicObject[] dynamicObjects = ASSESS_FORM_ROW_ENTITY_SERVICE.loadDynamicObjectArray(longList.toArray());
                    long userId = UserServiceHelper.getCurrentUserId();
                    for (int index = 0; index < dynamicObjects.length; index++) {
                        DynamicObject addNew = entryEntity.addNew();
                        DynamicObjectUtils.copy(dynamicObjects[index], addNew);
                        addNew.set("indicatorindex", index + 1);
                        dynamicObject.set(HRBaseConstants.MODIFYTIME, new Date());
                        dynamicObject.set(HRBaseConstants.MODIFIER, userId);
                        dynamicObject.set(HRBaseConstants.CREATOR, userId);
                        dynamicObject.set(HRBaseConstants.CREATETIME, new Date());
                        if (BigDecimal.ZERO.compareTo(addNew.getBigDecimal("indicatorweight")) == 0) {
                            addNew.set("indicatorweight", null);
                        }
                        // 设置指标类型（打等/打分）
                        assembleIndicatorLevalMap(addNew);
                    }
                    ASSESS_FORM_ROW_ENTITY_SERVICE.delete(longList.toArray());
                }
                ASSESS_FORM_ENTITY_SERVICE.saveOne(dynamicObject);
                DynamicObject setting = DIM_SETTING_ENTITY_SERVICE.loadSingle(groupselect);
                // 删除旧的测评表数据
                clearOldAssessForm(setting);
                // 分组挂新的测评表
                setting.set("assessform", newDimIds[0]);
                DIM_SETTING_ENTITY_SERVICE.saveOne(setting);
                // 导入新的测评表，需要把该分组下的维度测评人旧的指标打分实例数据删除，重新基于新的测评表生成指标打分实例数据
                DIMASSESSER_DOMAIN_SERVICE.reGenerateDimAssesserIndScoreInstDataGroup(setting);
            }
        }

    }

    /**
     * 清除分组上挂的旧的测评表数据
     * @param setting
     */
    private void clearOldAssessForm(DynamicObject setting){
        if(setting.getLong("assessform.id") != 0L){
            ASSESS_FORM_ENTITY_SERVICE.deleteOne(setting.getLong("assessform.id"));
        }
    }

    /**
     * 处理指标等级映射编码
     * @param newIndicator
     */
    private void assembleIndicatorLevalMap(DynamicObject newIndicator){
        QFilter qFilter = new QFilter("number", QCP.equals, newIndicator.getString("levelmap")).and("iscurrentversion", QCP.equals, "1");
        DynamicObject query = PERFORMANCE_LEVEL_SERVICE.queryOne("scoremapentryentity.scoresystem,scoremapentryentity.levelscoremap,scoresubentryentity.scorelevel,scoresubentryentity.defaultscore,scoresubentryentity.minscore,scoresubentryentity.maxscore", new QFilter[]{qFilter});
        DynamicObjectCollection dynamicObjectCollection = (DynamicObjectCollection) query.get("scoremapentryentity");
        DynamicObject scoremap = dynamicObjectCollection.get(0);
        if(HRStringUtils.equals("20",scoremap.getString("levelscoremap"))){
            // 打等
            newIndicator.set("indicatortype", "20");
        }
        if(HRStringUtils.equals("10",scoremap.getString("levelscoremap"))){
            // 打分
            newIndicator.set("indicatortype", "10");
        }
    }

    @Override
    public void afterInitContext(AfterInitContextArgs args) {
        HRImportPlugin.super.afterInitContext(args);
    }

    @Override
    public void beforeCreateHeaderColumn(BeforeCreateHeaderColumnEventArgs args) {
        HRImportPlugin.super.beforeCreateHeaderColumn(args);
    }

    @Override
    public void beforeQueryRefBd(BeforeQueryRefBdEventArgs args) {
        HRImportPlugin.super.beforeQueryRefBd(args);
    }
}
