package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.PerformanceLevelEntityService;
import cds0.opmc.cea.business.entityservice.ScoreSystemEntityService;
import cds0.opmc.cea.business.service.AssessObjDomainService;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.AbstractFormDataModel;
import kd.bos.entity.datamodel.TableValueSetter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseList;

import java.util.EventObject;
import java.util.Map;

public class AssessObjResultDetailPlugin extends HRDataBaseList {

    private static final AssessObjDomainService ASSESS_OBJ_DOMAIN_SERVICE = AssessObjDomainService.getInstance();
    private static final PerformanceLevelEntityService PERFORMANCE_LEVEL_SERVICE = PerformanceLevelEntityService.getInstance();
    private static final String ASSESSFORMID = "assessformid";

    @Override
    public void beforeBindData(EventObject args) {
        super.beforeBindData(args);
        initAssessResultEntry();

    }

    /**
     * 初始化测评结果分录
     */
    public void initAssessResultEntry(){
        String assessFormId = this.getView().getFormShowParameter().getCustomParam(ASSESSFORMID);
        DynamicObject assessForm = ASSESS_OBJ_DOMAIN_SERVICE.queryAssessFormById(Long.valueOf(assessFormId));

        Long dimerId = this.getView().getFormShowParameter().getCustomParam("dimerid");

        Map<Long, String> scoreMap = ASSESS_OBJ_DOMAIN_SERVICE.queryScoreByDimer(dimerId);
        // 没有换算的分值
        DynamicObjectCollection assessFormRow = assessForm.getDynamicObjectCollection("entryentity");
        AbstractFormDataModel model = (AbstractFormDataModel)this.getModel();
        model.beginInit();
        TableValueSetter vs = new TableValueSetter();
        vs.addField("indicator");
        vs.addField("scoreorweight");
        vs.addField("indivision");
        vs.addField("score");
        for (DynamicObject indicator : assessFormRow) {
            Long indId = indicator.getLong(HRBaseConstants.ID);
            String score = scoreMap.get(indId);

            int indicatorscore = indicator.getInt("indicatorscore");
            if (indicatorscore == 0) {
                vs.addRow(indId, indicator.get("indicatorweight") , assembleIndivision(indicator.getString("levelmap")) ,score);
            }else {
                vs.addRow(indId, indicatorscore , assembleIndivision(indicator.getString("levelmap")) ,score);
            }
        }
        model.batchCreateNewEntryRow("entryentity", vs);
        model.endInit();
    }

    /**
     * 获取指标分制
     * @param levelmap
     * @return
     */
    private String assembleIndivision(String levelmap){
        QFilter qFilter = new QFilter("number", QCP.equals, levelmap).and("iscurrentversion", QCP.equals, "1");
        DynamicObject query = PERFORMANCE_LEVEL_SERVICE.queryOne("scoremapentryentity.scoresystem,scoremapentryentity.scoresystem.name,scoremapentryentity.levelscoremap,scoresubentryentity.scorelevel,scoresubentryentity.defaultscore,scoresubentryentity.minscore,scoresubentryentity.maxscore", new QFilter[]{qFilter});
        DynamicObjectCollection dynamicObjectCollection = (DynamicObjectCollection) query.get("scoremapentryentity");
        DynamicObject scoremap = dynamicObjectCollection.get(0);
        return scoremap !=null ? scoremap.getString("scoresystem.name") : HRStringUtils.EMPTY;
    }

}
