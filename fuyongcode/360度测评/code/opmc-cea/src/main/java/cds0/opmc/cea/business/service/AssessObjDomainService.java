package cds0.opmc.cea.business.service;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.business.entityservice.*;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class AssessObjDomainService {
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();

    private static final AssessFormEntityService ASSESS_FORM_ENTITY_SERVICE = AssessFormEntityService.getInstance();

    private static final AssessTaskEntityService ASSESS_TASK_ENTITY_SERVICE = AssessTaskEntityService.getInstance();

    private static final AssObjScoItemInstEntityService ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE = AssObjScoItemInstEntityService.getInstance();

    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();

    private static final PerformanceLevelEntityService PERFORMANCE_LEVEL_SERVICE = PerformanceLevelEntityService.getInstance();

    private static final ScoreSystemEntityService SCORE_SYSTEM_ENTITY_SERVICE = ScoreSystemEntityService.getInstance();

    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();
    private static final EvalDimSettingEntityService EVAL_DIM_SETTING_ENTITY_SERVICE = EvalDimSettingEntityService.getInstance();

    public static AssessObjDomainService getInstance() {
        return ServiceFactory.getService(AssessObjDomainService.class);
    }

    public DynamicObject queryObjNameById(Object id) {
        return ASSESS_OBJ_ENTITY_SERVICE.queryObjNameById(id);
    }

    public DynamicObject queryAssessFormById(Long id) {
        return ASSESS_FORM_ENTITY_SERVICE.queryAssessFormByPk(id);
    }

    public DynamicObject[] queryAssessTaskByObjId(Long id) {
        return ASSESS_TASK_ENTITY_SERVICE.queryAssessTaskByObjId(id);
    }

    public Map<Long, String> queryScoreByDimer(Long dimassesserIds) {
        DynamicObject[] scoreItemIns = ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.queryScoreItemInsByDimer(dimassesserIds);
        return Arrays.stream(scoreItemIns).collect(Collectors.toMap(x -> x.getLong("assessformrow.id"), y -> y.getString("levelscore"), (x, y) -> x));
    }

    public Map<Long, List<DynamicObject>> queryScoreByObjId(Long id) {
        DynamicObject[] scoreItemIns = ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.queryScoreItemInsByObjId(id);
        return Arrays.stream(scoreItemIns).collect(Collectors.groupingBy(gen1 -> gen1.getLong("assessformrow.id")));
    }

    /**
     * 查询测评人维度指标
     *
     * @param assobjId 测评对象
     * @return <K：维度测评人，v:维度测评人的权重>
     */
    public Map<Long, BigDecimal> queryDimassesserWeight(Long assobjId) {
        DynamicObject[] dimAssesser = DIMASSESSER_ENTITY_SERVICE.queryByObjId(assobjId);
        return Arrays.stream(dimAssesser).collect(Collectors.toMap(x -> x.getLong(HRBaseConstants.ID), y -> y.getBigDecimal("dimweight"), (x, y) -> x));
    }

    public DynamicObject queryScoreSystemById(Long id) {
        return SCORE_SYSTEM_ENTITY_SERVICE.queryOne(id);
    }

    public DynamicObject queryPerfLevelByAssFormId(Long assFormId) {
        DynamicObject dynamicObject = ASSESS_FORM_ENTITY_SERVICE.queryAssessFormByPk(assFormId);
        String perfLevelNumber = dynamicObject.getString("levelmapnumber");
        QFilter qFilter = new QFilter("number", QCP.equals, perfLevelNumber).and("iscurrentversion", QCP.equals, "1");
        DynamicObject perfLevel = PERFORMANCE_LEVEL_SERVICE.queryOne("scoremapentryentity.scoresystem,scoresubentryentity.scorelevel,scoresubentryentity.defaultscore,scoresubentryentity.minscore,scoresubentryentity.maxscore", new QFilter[]{qFilter});
        DynamicObjectCollection scoremapentryentity = perfLevel.getDynamicObjectCollection("scoremapentryentity");

        return scoremapentryentity.get(0).getDynamicObject("scoresystem");
    }

    /**
     * 计算综合评分
     *
     * @param assessobjId 测评对象
     * @return score 分数
     */
    public BigDecimal compositeScore(Long assessobjId, String type) {
        //获取是否分制映射
        DynamicObject assessObject = ASSESS_OBJ_ENTITY_SERVICE.queryOne(assessobjId);
        DynamicObject activityObject = ASSESS_ACTIVITY_ENTITY_SERVICE.queryOne(assessObject.get("assessact.id"));
        String scoresystemmap = activityObject.getString("scoresystemmap");
        //根据测评对象查找到对应的测评对象打分实例
        DynamicObject[] assObjScoItems = ASS_OBJ_SCO_ITEM_INST_ENTITY_SERVICE.queryScoreItemsByObjId(assessobjId);
        BigDecimal score = new BigDecimal("0.0");
        BigDecimal divisor = new BigDecimal("100");
        Map<Long, BigDecimal> entryMap = new HashMap<>();
        //获取测评活动配置的精度
        DynamicObject assessactObject = ASSESS_OBJ_ENTITY_SERVICE.queryOne(assessobjId);
        String accuracy = assessactObject.getString("assessact.mpnumaccuracy");
        String scaleType = assessactObject.getString("assessact.mpscaletype");
        //遍历集合，获取entryId
        ArrayList<Long> entryIds = new ArrayList<>();
        for (DynamicObject dynamicObject : assObjScoItems) {
            long entryid = dynamicObject.getLong("entryid");
            entryIds.add(entryid);
        }
        //查询权重
        DynamicObject[] dimassesserObjects = DIMASSESSER_ENTITY_SERVICE.queryEvaldimByIds(entryIds);
        Map<Long, DynamicObject> dimassesserMap = Arrays.stream(dimassesserObjects).collect(Collectors.toMap(k -> (Long) k.getPkValue(), v -> v));
        for (DynamicObject dynamicObject : assObjScoItems) {
            //获取得分
            String levelscoreStr = dynamicObject.getString("mapscore");
            if (!StringUtils.isEmpty(levelscoreStr)) {
                // 判断是否映射
                BigDecimal levelscore = new BigDecimal("0.0");
                if (HRBaseConstants.STR_ONE.equals(scoresystemmap)) {
                    String[] split = levelscoreStr.split(",");
                    BigDecimal multiply = new BigDecimal(split[0]).multiply(new BigDecimal(split[1]));
                    //精度处理
                    if (HRBaseConstants.STR_ONE.equals(scaleType)) {
                        // 四舍五入
                        levelscore = multiply.divide(new BigDecimal(split[2]), Integer.parseInt(accuracy), RoundingMode.HALF_UP);
                    } else {
                        levelscore = multiply.divide(new BigDecimal(split[2]), Integer.parseInt(accuracy), Integer.parseInt(scaleType));
                    }
                } else {
                    levelscore = new BigDecimal(levelscoreStr);
                }
                BigDecimal indicatorweight = new BigDecimal("1.0");
                //获取指标权重
                BigDecimal indicatorweight1 = dynamicObject.getBigDecimal("indicatorweight");
                //如果指标权重不为0
                if (indicatorweight1 != null) {
                    if (indicatorweight1.compareTo(BigDecimal.ZERO) != 0) {
                        indicatorweight = indicatorweight1.divide(divisor);
                    }
                }
                long entryid = dynamicObject.getLong("entryid");
                //按维度权重算分
                if (type.equals("20")) {
                    //key为测评人,value为分数,来获取每个测评人获取的总分
                    if (entryMap.get(entryid) == null) {
                        entryMap.put(entryid, levelscore.multiply(indicatorweight));
                    } else {
                        BigDecimal oldNum = entryMap.get(entryid);
                        entryMap.put(entryid, oldNum.add(levelscore.multiply(indicatorweight)));
                    }
                }
                //按测评人权重算分
                if (type.equals("10")) {
                    //获取测评人权重
                    DynamicObject dimassesserObj = dimassesserMap.get(entryid);
                    if (dimassesserObj != null) {
                        BigDecimal dimweight = dimassesserObj.getBigDecimal("dimweight").divide(divisor);
                        //算分 总分+(得分*指标权重*测评人权重)
                        score = score.add(levelscore.multiply(indicatorweight).multiply(dimweight));
                    }
                }

            }
        }
        //判断entryMap是否为空,不为空则按照五矿的逻辑将值取出来计算
        Map<Long, AbstractMap.SimpleEntry<Integer, BigDecimal>> groupMap = new HashMap<>();
        //将角色id存入list
        ArrayList<Long> fevaldimids = new ArrayList<>();
        if (entryMap != null && entryMap.size()>0) {
            //遍历map,根据测评人判断属于哪种角色,以角色为key,<角色内测评人数,角色内测评人总分>为value
            for (Long key : entryMap.keySet()) {
                DynamicObject fevaldimidObject = dimassesserMap.get(key);
                DynamicObject fevaldimObject = (DynamicObject) fevaldimidObject.get("evaldim");
                Long fevaldimid = (Long) fevaldimObject.getPkValue();
                fevaldimids.add(fevaldimid);
                if (groupMap.get(fevaldimid) == null) {
                    AbstractMap.SimpleEntry<Integer, BigDecimal> pair = new AbstractMap.SimpleEntry<>(1, entryMap.get(key));
                    groupMap.put(fevaldimid, pair);
                } else {
                    AbstractMap.SimpleEntry<Integer, BigDecimal> oldPair = groupMap.get(fevaldimid);
                    AbstractMap.SimpleEntry<Integer, BigDecimal> newPair = new AbstractMap.SimpleEntry<>(oldPair.getKey() + 1, oldPair.getValue().add(entryMap.get(key)));
                    groupMap.put(fevaldimid, newPair);
                }
            }
        }
        DynamicObject[] dimSettingObjects = EVAL_DIM_SETTING_ENTITY_SERVICE.queryByIds(fevaldimids);
        Map<Long, DynamicObject> dimSettingMap = Arrays.stream(dimSettingObjects).collect(Collectors.toMap(k -> (Long) k.getPkValue(), v -> v));
        if (groupMap != null && groupMap.size() >0) {
            //遍历map,算出每个角色内的分数并累加
            for (Long key : groupMap.keySet()) {
                AbstractMap.SimpleEntry<Integer, BigDecimal> scoreEntry = groupMap.get(key);
                DynamicObject dimSettingObject = dimSettingMap.get(key);
                BigDecimal dimweight = dimSettingObject.getBigDecimal("dimweight");
                score = score.add(scoreEntry.getValue().divide(new BigDecimal(scoreEntry.getKey())).multiply(dimweight).divide(divisor));
            }
        }
        //进行精度处理
        score = setSumScoreScale(score, accuracy, scaleType);
        return score;
    }

    /**
     * 设置精度
     *
     * @param ovarallScore
     * @param accuracy
     * @param scaleType
     * @return
     */
    private BigDecimal setSumScoreScale(BigDecimal ovarallScore, String accuracy, String scaleType) {
        if (HRStringUtils.isNotEmpty(accuracy) && HRStringUtils.isNotEmpty(scaleType)) {
            //判断是否四舍五入,1为四舍五入
            if (HRBaseConstants.STR_ONE.equals(scaleType)) {
                // 四舍五入
                ovarallScore = ovarallScore.setScale(Integer.parseInt(accuracy), RoundingMode.HALF_UP);
            } else {
                ovarallScore = ovarallScore.setScale(Integer.parseInt(accuracy), Integer.parseInt(scaleType));
            }
        }
        return ovarallScore;
    }

//    /**
//     * 映射评分分值
//     *
//     * @param ovarallScore
//     * @return
//     */
//    public BigDecimal transScoreMapWithScale(BigDecimal ovarallScore, Long actScoreId, Long assessobjId) {
//        //根据测评对象获取测评任务
//        QFilter assessQFilter = new QFilter("assessobj", QCP.equals, assessobjId);
//        DynamicObject taskObject = ASSESS_TASK_ENTITY_SERVICE.queryOne("dimgroup.assessform.id", assessQFilter.toArray());
//        //根据测评任务获取分组
//        long assFormId = taskObject.getLong("dimgroup.assessform.id");
//        //根据测评表ID查询绩效等级
//        DynamicObject levelScore = ASSESS_OBJ_DOMAIN_SERVICE.queryPerfLevelByAssFormId(assFormId);
//        //获取绩效等级最大分数
//        BigDecimal levelMaxScore = levelScore.getBigDecimal("maxscore");
//        //获取从测评活动取到的评分分制
//        DynamicObject scoreSystemObject = queryScoreSystemById(actScoreId);
//        //获取活动最大分数
//        BigDecimal actMaxScore = scoreSystemObject.getBigDecimal("maxscore");
//        ovarallScore = ovarallScore.multiply(actMaxScore.divide(levelMaxScore));
//        return ovarallScore;
//    }

}
