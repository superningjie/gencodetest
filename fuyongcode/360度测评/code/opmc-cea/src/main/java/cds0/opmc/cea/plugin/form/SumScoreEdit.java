package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.business.service.MessageService;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测评打分详情页评分插件
 *
 * @author sxf
 * @date 2024-06-18 11:45:38
 */
public class SumScoreEdit extends AbstractBillPlugIn implements BeforeF7SelectListener {
    private static final String BASE_RESULT = "cds0_checklevelmap";
    private static final String BASE_DECIMAL_RESULT = "cds0_levelmap";
    private static final String BASE_NAME = "cds0_basedatafield1";
    private static final String ENTRY_ENTITY = "cds0_entryentity";
    private static final String INDICATOR_NAME = "indicatorname";
    private static final String MAX_SCORE= "maxscore";
    private static final Log LOG = LogFactory.getLog(MessageService.class);
    private static final PerformanceLevelEntityService PERFORMANCE_LEVEL_SERVICE = PerformanceLevelEntityService.getInstance();
    private static final AssessFormEntityService ASSESS_FORM_ENTITY_SERVICE = AssessFormEntityService.getInstance();
    private static final ScoreSystemEntityService SCORE_SYSTEM_ENTITY_SERVICE = ScoreSystemEntityService.getInstance();
    private Integer columnLength;
    private DynamicObjectCollection indicatorNameCollection;
    private String perflevelId;
    private Long scoreSystem;
    private BigDecimal magnification = new BigDecimal("1");
    private String scoreSystemMap;

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {

    }

    @Override
    public void afterCreateNewData(EventObject e) {
        this.getAssessForm();
    }

    private void getAssessForm() {
        //查询测评表,确认测评类型assformtype,获取测评指标数据indicatorNameCollection
        FormShowParameter parameter = this.getView().getFormShowParameter();
        LOG.info("scoreSystemMap----->"+parameter.getCustomParam("scoreSystemMap"));
        LOG.info("scoreSystem----->"+parameter.getCustomParam("scoreSystem"));
        LOG.info("assessFormId----->"+parameter.getCustomParam("assessFormId"));
        scoreSystemMap = parameter.getCustomParam("scoreSystemMap");
        if(!parameter.getCustomParam("scoreSystem").toString().equals("0")) {
            scoreSystem = parameter.getCustomParam("scoreSystem");
        }
        Long assessFormId = parameter.getCustomParam("assessFormId");
        QFilter qFilter = new QFilter("id", QCP.equals, assessFormId);
        DynamicObject assessForm = ASSESS_FORM_ENTITY_SERVICE.queryOne("assformtype,levelmapnumber,entryentity.indicatorname", new QFilter[]{qFilter});
        if (assessForm != null) {
            perflevelId = assessForm.getString("levelmapnumber");
            indicatorNameCollection = (DynamicObjectCollection) assessForm.get("entryentity");
            this.sumScore(assessForm.getString("assformtype"));
        }
    }
    /**
     * 监控值变化
     * 如果值有变化则调用算分逻辑重新算分,并更新缓存里的值
     */
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        //如果表数据变化
        if (this.getModel().getDataChanged()) {
            this.getAssessForm();
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("cds0_entryentity");
    }

    /**
     *评分
     **/
    private void sumScore(String type) {
        //从页面上获取每个人的评分
        LinkedHashMap<String, BigDecimal> personScoreMap = new LinkedHashMap<String, BigDecimal>();
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        //按等计算
        //获取权重和指标名称
        DynamicObject dynamicObject = (DynamicObject) this.getModel().getValue("cds0_basedatafield");
        String indicatorname = dynamicObject.getString(INDICATOR_NAME);
        BigDecimal indicatorWeight=new BigDecimal("1.0");
        BigDecimal indicatorweight1 = dynamicObject.getBigDecimal("indicatorweight");
        if( indicatorweight1 != null) {
            if (indicatorweight1.compareTo(BigDecimal.ZERO)!=0) {
                indicatorWeight = indicatorweight1.divide(new BigDecimal("100"));
            }
        }
        HashMap<String, BigDecimal> perflevel = this.getPerflevel();
        if (type.equals("20")) {
            DynamicObjectCollection o = dataEntity.getDynamicObjectCollection(ENTRY_ENTITY);
            for (DynamicObject entryObj : o) {
                //获取用户id
                DynamicObject userObject = (DynamicObject) entryObj.get(BASE_NAME);
                Long userId = userObject.getLong("id");
                BigDecimal score = new BigDecimal("0.0");
                for (int i = 1; i < columnLength + 1; i++) {
                    String result = BASE_RESULT + i;
                    if (entryObj.getBoolean(result)) {
                        //按等
                        score = perflevel.get(result).multiply(indicatorWeight).multiply(magnification);
                    }
                }
                personScoreMap.put(userId.toString(), score);
            }
        }
        //按分计算
        if (type.equals("10")) {
            DynamicObjectCollection o = dataEntity.getDynamicObjectCollection(ENTRY_ENTITY);
            for (DynamicObject entryObj : o) {
                //获取用户id
                DynamicObject userObject = (DynamicObject) entryObj.get(BASE_NAME);
                Long userId = userObject.getLong("id");
                BigDecimal score = new BigDecimal("0.0");
                for (int i = 1; i < columnLength + 1; i++) {
                    BigDecimal result = entryObj.getBigDecimal(BASE_DECIMAL_RESULT + i);
                    if (result != null && result.compareTo(BigDecimal.ZERO) != 0) {
                        score = result.multiply(indicatorWeight).multiply(magnification);
                    }
                }
                personScoreMap.put(userId.toString(), score);
            }
        }
        this.showScore(personScoreMap, indicatorname);
    }

    /**
     * 显示姓名和分数
     */
    private void showScore(LinkedHashMap<String, BigDecimal> personScoreMap, String indicatorname) {
        this.getView().getParentView().getPageCache().put(indicatorname, SerializationUtils.toJsonString(personScoreMap));
        //创建总评map,记录每个指标项相加的结果
        LinkedHashMap<String, BigDecimal> totalMap = new LinkedHashMap<>();
        //遍历所有指标获取相加的总结果
        for (int i = 0; i < indicatorNameCollection.size(); i++) {
            //遍历获取缓存中存的各个指标的数据
            String indicatorname1 = indicatorNameCollection.get(i).getString(INDICATOR_NAME);
            String cacheMap = this.getView().getParentView().getPageCache().get(indicatorname1);
            if (!StringUtils.isEmpty(cacheMap)) {
                LinkedHashMap<String, BigDecimal> map = SerializationUtils.fromJsonString(cacheMap, LinkedHashMap.class);
                for (String key : map.keySet()) {
                    BigDecimal totalDecimal = totalMap.get(key);
                    BigDecimal decimal = map.get(key);
                    if (totalDecimal != null) {
                        if (decimal != null) {
                            totalMap.put(key, totalDecimal.add(decimal));
                        }
                    } else {
                        totalMap.put(key, decimal);
                    }
                }
            }
        }
        //排序
        LinkedHashMap<String, BigDecimal> sortedMap = totalMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        //获取分录数量
        int entryentitySize = this.getView().getParentView().getModel().getEntryEntity(ENTRY_ENTITY).size();
        //获取分录数量和参评人的数量差值，如果差值>0，则补充
        int size = personScoreMap.size() - entryentitySize;
        if (size > 0) {
            this.getView().getParentView().getModel().batchCreateNewEntryRow(ENTRY_ENTITY, size);
        }
        //赋值
        int i = 0;
        for (String masterid : sortedMap.keySet()) {
            //赋值用户
            this.getView().getParentView().getModel().setValue("cds0_basedatafield", Long.parseLong(masterid), i);
            //赋值分数
            this.getView().getParentView().getModel().setValue("cds0_decimalfield", totalMap.get(masterid), i);
            this.getView().sendFormAction(this.getView().getParentView());
            i++;
        }
    }

    /**
     * 获取绩效等级
     * @param
     * @return HashMap
     */
    private HashMap getPerflevel() {
        QFilter qFilter = new QFilter("number", QCP.equals, perflevelId).and("iscurrentversion", QCP.equals, "1");
        DynamicObject query = PERFORMANCE_LEVEL_SERVICE.queryOne("scoremapentryentity.scoresystem,scoresubentryentity.scorelevel,scoresubentryentity.defaultscore,scoresubentryentity.minscore,scoresubentryentity.maxscore", new QFilter[]{qFilter});
        DynamicObjectCollection dynamicObjectCollection = (DynamicObjectCollection) query.get("scoremapentryentity");
        DynamicObject dynamicObject = dynamicObjectCollection.get(0);
        DynamicObjectCollection scoresubentryentity = (DynamicObjectCollection) dynamicObject.get("scoresubentryentity");
        //获取评分分制数据
        DynamicObject scoresystemObject = (DynamicObject) dynamicObject.get("scoresystem");
        columnLength = scoresubentryentity.size();
        return this.transScoreMap(scoresubentryentity, scoresystemObject);
    }

    /**
     * 转化评分
     * @param  dynamicObjectCollection,scoresystemObject
     * @return HashMap
     */
    private HashMap<String, BigDecimal> transScoreMap(DynamicObjectCollection dynamicObjectCollection, DynamicObject scoresystemObject) {
        HashMap<String, BigDecimal> map = new HashMap<>();
        int i = 1;
        if (scoreSystemMap != null && scoreSystemMap.equals("1")) {
            if (scoreSystem != null) {
                if (!scoreSystem.toString().equals(scoresystemObject.getPkValue().toString())) {
                    //获取从测评活动取到的评分分制
                    DynamicObject dynamicObject1 = SCORE_SYSTEM_ENTITY_SERVICE.queryOne(scoreSystem);
                    //获取活动最大分数
                    BigDecimal maxscore = dynamicObject1.getBigDecimal(MAX_SCORE);
                    //获取绩效等级的最大分数
                    BigDecimal levelMaxscore1 = scoresystemObject.getBigDecimal(MAX_SCORE);
                    magnification = maxscore.divide(levelMaxscore1);
                }
            }
        }
        for (DynamicObject dynamicObject : dynamicObjectCollection) {
            //分数
            BigDecimal defaultscore = dynamicObject.getBigDecimal("defaultscore");
            map.put(BASE_RESULT + i, defaultscore);
            i++;
        }
        return map;
    }
}
