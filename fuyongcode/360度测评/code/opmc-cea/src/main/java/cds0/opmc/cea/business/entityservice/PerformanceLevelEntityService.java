package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;

import cds0.opmc.cea.business.service.AssessObjDomainService;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.constants.HRHisBaseConstants;
import kd.hr.hbp.common.constants.newhismodel.HisFieldNameConstants;
import kd.opmc.pbs.business.domain.OpmcEntityService;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import static cds0.opmc.cea.common.AppflgConstant.PERFLEVEL;

public class PerformanceLevelEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper PerformanceLevelServiceHelper = new HRBaseServiceHelper(PERFLEVEL);
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();
    private static final AssessObjDomainService ASSESS_OBJ_DOMAIN_SERVICE = AssessObjDomainService.getInstance();
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return PerformanceLevelServiceHelper;
    }

    public static PerformanceLevelEntityService getInstance() {
        return ServiceFactory.getService(PerformanceLevelEntityService.class);
    }


    /**
     * 分制转换
     * @return
     */
    public String transScore(String score, Long activityId, String levelentry){
        if (!StringUtils.isEmpty(score) && !score.equals("false")) {
            //获取活动数据
            DynamicObject activityObject = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(activityId);
            //获取是否分制映射
            String scoresystemmap = activityObject.getString("scoresystemmap");
            //如果活动配置了分制映射则将分数进行转化 score=score*(活动最大分数/绩效等级最大分数)
            if ("1".equals(scoresystemmap)) {
                //如果是的话去做分制映射
                DynamicObject actScore = (DynamicObject) activityObject.get("scoresystem");
                //获取从测评活动取到的评分分制
                DynamicObject scoreSystemObject = ASSESS_OBJ_DOMAIN_SERVICE.queryScoreSystemById((Long) actScore.getPkValue());
                //获取活动最大分数
                BigDecimal actMaxScore = scoreSystemObject.getBigDecimal("maxscore");
                //获取绩效等级最大分
                QFilter qFilter = new QFilter("number", QCP.equals, levelentry).and("iscurrentversion", QCP.equals, "1");
                DynamicObject dynamicObject1 = this.queryOne("scoremapentryentity.scoresystem,scoresubentryentity.scorelevel,scoresubentryentity.defaultscore,scoresubentryentity.minscore,scoresubentryentity.maxscore", new QFilter[]{qFilter});
                DynamicObjectCollection scoremapentryentity = dynamicObject1.getDynamicObjectCollection("scoremapentryentity");
                DynamicObject scoresystem = scoremapentryentity.get(0).getDynamicObject("scoresystem");
                BigDecimal levelMaxScore = scoresystem.getBigDecimal("maxscore");
                score =String.join(",",score,actMaxScore.toString(),levelMaxScore.toString());
            }
        }
        return score;
    }
    public DynamicObject queryOneByParam(DynamicObject assessFormRow){
        return PerformanceLevelServiceHelper.queryOne("entryentity.levelname,entryentity.seq", new QFilter[] {
                new QFilter(HRBaseConstants.NUMBER, QCP.equals, assessFormRow.get("levelmap")),
                new QFilter(HRBaseConstants.STATUS, QCP.equals, "C"),
                new QFilter(HRHisBaseConstants.FIELD_DATASTATUS, QCP.equals, "1"),
                new QFilter(HRBaseConstants.FIELD_ISCURRENTVERSION, QCP.equals, "0"),
                new QFilter(HisFieldNameConstants.HISVERSION, QCP.not_equals, "COPYTEMP")
        });
    }

    /**
     * 绩效等级查询
     * @return
     */
    public DynamicObjectCollection getPerformanceLevel(String perflevelId){
        //查询绩效等级数据,并创建分录
        QFilter qFilter1 = new QFilter("number", QCP.equals, perflevelId).and("iscurrentversion", QCP.equals, "1");
        DynamicObject query = this.queryOne("scoresubentryentity.scorelevel,scoresubentryentity.defaultscore,scoresubentryentity.minscore,scoresubentryentity.maxscore", new QFilter[]{qFilter1});
        DynamicObjectCollection dynamicObjectCollection = (DynamicObjectCollection) query.get("scoremapentryentity");
        if(dynamicObjectCollection!= null && dynamicObjectCollection.size()>0) {
            DynamicObject dynamicObject = dynamicObjectCollection.get(0);
            DynamicObjectCollection scoresubentryentity = (DynamicObjectCollection) dynamicObject.get("scoresubentryentity");
            if (scoresubentryentity != null && scoresubentryentity.size() > 0) {
                return scoresubentryentity;
            }
        }
        return  null;
    }

    public DynamicObject queryByNumber(String number){
        return queryOne("entryentity.levelname,entryentity.seq", new QFilter[] {
                new QFilter(HRBaseConstants.NUMBER, QCP.equals, number),
                new QFilter(HRBaseConstants.STATUS, QCP.equals, "C"),
                new QFilter(HRHisBaseConstants.FIELD_DATASTATUS, QCP.equals, "1"),
                new QFilter(HRBaseConstants.FIELD_ISCURRENTVERSION, QCP.equals, "0"),
                new QFilter(HisFieldNameConstants.HISVERSION, QCP.not_equals, "COPYTEMP")
        });
    }
}
