package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessTaskEntityService;
import cds0.opmc.cea.business.entityservice.DimassesserEntityService;
import cds0.opmc.cea.business.entityservice.PerformanceLevelEntityService;
import cds0.opmc.cea.business.entityservice.ScoreSystemEntityService;
import cds0.opmc.cea.business.service.AssessObjDomainService;
import cds0.opmc.cea.common.AssessManageUtils;
import cds0.opmc.cea.common.enums.ActAnonymousEnum;
import com.google.common.collect.Maps;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.entity.datamodel.AbstractFormDataModel;
import kd.bos.entity.datamodel.TableValueSetter;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.container.Container;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Image;
import kd.bos.form.control.Label;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRCollUtil;
import kd.hr.hbp.common.util.HRImageUrlUtil;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDynamicFormBasePlugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 360测评结果
 */
public class AssessObjResultEdit extends HRDynamicFormBasePlugin {

    private static final Log LOGGER = LogFactory.getLog(AssessObjResultEdit.class);
    private static final AssessObjDomainService ASSESS_OBJ_DOMAIN_SERVICE = AssessObjDomainService.getInstance();
    private static final PerformanceLevelEntityService PERFORMANCE_LEVEL_SERVICE = PerformanceLevelEntityService.getInstance();
    private static final AssessTaskEntityService ASSESS_TASK_ENTITY_SERVICE = AssessTaskEntityService.getInstance();
    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();
    private static final String ASSESSFORMID = "assessformid";

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        initHeadInfo();
        initAssessResultEntry();
        initAssessDimDetailEntry();
    }

    /**
     * 初始化头部信息
     */
    private void initHeadInfo() {
        IFormView view = this.getView();
        DynamicObject dynamicObject = this.getView().getModel().getDataEntity();
        Image image = view.getControl("headimg");
        String headSculpture = dynamicObject.getString("person.headsculpture");
        if (!HRStringUtils.isEmpty(headSculpture)) {
            image.setUrl(HRImageUrlUtil.getImageFullUrl(headSculpture));
        }
        this.setLabelText("laborrelstatus", dynamicObject.getString("employee.laborrelstatus.name"), "laborrelstatus");
        this.setLabelText("postype", dynamicObject.getString("perffile.postype.name"), "postype");
    }

    private void setLabelText(String key, String text, String... icon) {
        IFormView view = this.getView();
        Label label = view.getControl(key);
        if (!ObjectUtils.isEmpty(label)) {
            if (HRStringUtils.isEmpty(text)) {
                label.setText("");
                if (icon != null && icon.length > 0 && icon[0] != null) {
                    this.getView().setVisible(Boolean.FALSE, icon);
                }
            } else {
                label.setText(text);
            }

        }
    }

    /**
     * 初始化测评结果分录
     */
    public void initAssessResultEntry() {
        long assessFormId = this.getModel().getDataEntity().getLong("dimgroup.assessform.id");
        this.getPageCache().put(ASSESSFORMID, String.valueOf(assessFormId));
        DynamicObject assessForm = ASSESS_OBJ_DOMAIN_SERVICE.queryAssessFormById(assessFormId);
        Long id = this.getModel().getDataEntity().getLong(HRBaseConstants.ID);
        Map<Long, List<DynamicObject>> scoreMap = ASSESS_OBJ_DOMAIN_SERVICE.queryScoreByObjId(id);

        Map<Long, BigDecimal> scoreValue = calIndScoreValue(scoreMap);

        DynamicObjectCollection assessFormRow = assessForm.getDynamicObjectCollection("entryentity");
        AbstractFormDataModel model = (AbstractFormDataModel) this.getModel();
        model.beginInit();
        TableValueSetter vs = new TableValueSetter();
        vs.addField("indicator");
        vs.addField("scoreorweight");
        vs.addField("indivision");
        vs.addField("score");
        for (DynamicObject indicator : assessFormRow) {
            Long indId = indicator.getLong(HRBaseConstants.ID);

            int indicatorscore = indicator.getInt("indicatorscore");
            if (indicatorscore == 0) {
                vs.addRow(indId, indicator.get("indicatorweight"), assembleIndivision(indicator.getString("levelmap")), scoreValue.get(indId));
            } else {
                vs.addRow(indId, indicatorscore, assembleIndivision(indicator.getString("levelmap")), scoreValue.get(indId));
            }
        }
        model.batchCreateNewEntryRow("retentryentity", vs);
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

    /**
     * 通过所有的维度测评人打分实例计算出指标分值，并且设置综合得分
     *
     * @param indGroup 指标维度的打分实例分组
     * @return
     */
    private Map<Long, BigDecimal> calIndScoreValue(Map<Long, List<DynamicObject>> indGroup) {

        // 维度测评人权重
        Map<Long, BigDecimal> dimerWeightMap = ASSESS_OBJ_DOMAIN_SERVICE.queryDimassesserWeight(this.getModel().getDataEntity().getLong(HRBaseConstants.ID));
        LOGGER.info("维度测评人权重，<dimer,weight>：{}。", dimerWeightMap.toString());
        // 返回每个指标的得分
        Map<Long, BigDecimal> ret = Maps.newHashMapWithExpectedSize(indGroup.size());

        Map<Long, BigDecimal> indWeightMap = Maps.newHashMapWithExpectedSize(indGroup.size());

        for (Map.Entry<Long, List<DynamicObject>> scoreEntry : indGroup.entrySet()) {
            List<DynamicObject> scoreValues = scoreEntry.getValue();
            if (HRCollUtil.isEmpty(scoreValues)) {
                continue;
            }
            Long indId = scoreEntry.getKey();

            // 当前指标得分
            BigDecimal curIndScore = new BigDecimal("0.0");

            for (DynamicObject scoreValue : scoreValues) {
                // 指标权重，后续计算综合评分
                String indWeight = scoreValue.getString("indicatorweight");

                if (HRStringUtils.isNotEmpty(indWeight)
                        && !"0".equals(indWeight)
                        && !"0.0".equals(indWeight)
                        && !"0.00".equals(indWeight)
                        && !"0.000000".equals(indWeight)
                ) {
                    // 不为null,为0 都不计算是多少就算多少权重
                    if (!indWeightMap.containsKey(indId)) {
                        indWeightMap.put(indId, new BigDecimal(indWeight));
                    }
                } else {
                    // 为null ，说明权重是100，相当于不计算权重
                    indWeightMap.put(indId, new BigDecimal("100"));
                }

                // 统计指标分
                String ls = scoreValue.getString("levelscore");

                if (HRStringUtils.isNotEmpty(ls)) {
                    BigDecimal lsScore = new BigDecimal(ls);
                    BigDecimal dimerWeight = dimerWeightMap.get(scoreValue.getLong("entryid"));
                    curIndScore = curIndScore.add(lsScore.multiply(dimerWeight).divide(new BigDecimal("100")));
                }
            }

            ret.put(indId, curIndScore);
        }
        LOGGER.info("指标权重，<ind,weight>：{}。", indWeightMap.toString());
        LOGGER.info("指标得分，<ind,score>：{}。", ret.toString());
        // 计算综合评分
        DynamicObject dataEntity = this.getModel().getDataEntity();
//        BigDecimal modscore = dataEntity.getBigDecimal("modscore");
        Label overallscore = this.getView().getControl("overallscore");
//        String accuracy = dataEntity.getString("assessact.mpnumaccuracy");
//        String scaleType = dataEntity.getString("assessact.mpscaletype");
        overallscore.setText(AssessManageUtils.compositeScore(this.getModel().getDataEntity().getLong(HRBaseConstants.ID)).toString());
        // 结束总分的计算，这里再进行指标精度的计算，只是省略小数位。
        setIndScale(ret);
        return ret;
    }

    private void setIndScale(Map<Long, BigDecimal> ret) {
        DynamicObject dataEntity = this.getModel().getDataEntity();
        String accuracy = dataEntity.getString("assessact.mpnumaccuracy");
        String scaleType = dataEntity.getString("assessact.mpscaletype");
        for (Map.Entry<Long, BigDecimal> entrySet : ret.entrySet()) {
            BigDecimal indScore = entrySet.getValue();
            indScore = setSumScoreScale(indScore, accuracy, scaleType);
            ret.put(entrySet.getKey(), indScore);
        }
    }

    /**
     * 计算得分，放入综合得分
     * @param indWeightMap
     * @param indScoreMap
     */
    private void calSumScore(Map<Long, BigDecimal> indWeightMap, Map<Long, BigDecimal> indScoreMap) {
        Label overallscore = this.getView().getControl("overallscore");
        BigDecimal ovarallScore = new BigDecimal("0.0");
        for (Map.Entry<Long, BigDecimal> indScoreEntry : indScoreMap.entrySet()) {
            BigDecimal indWeight = indWeightMap.get(indScoreEntry.getKey());

            BigDecimal indScore = indScoreEntry.getValue();
            ovarallScore = ovarallScore.add(indScore.multiply(indWeight).divide(new BigDecimal("100")));
        }
        LOGGER.info("计算之后计算总分：{}。",ovarallScore);
        // 综合评分
        DynamicObject dataEntity = this.getModel().getDataEntity();
        String accuracy = dataEntity.getString("assessact.mpnumaccuracy");
        String scaleType = dataEntity.getString("assessact.mpscaletype");
        String scoreMap = dataEntity.getString("assessact.scoresystemmap");
        if ("1".equals(scoreMap)) {
            // 判断是否总分映射
            ovarallScore = transScoreMapWithScale(ovarallScore);
        }
        LOGGER.info("开始设置总分精度，accuracy：{}，scaleType：{}，设置前计算总分：{}。",accuracy,scaleType,ovarallScore);
        ovarallScore = setSumScoreScale(ovarallScore, accuracy, scaleType);
        LOGGER.info("结束设置总分精度，accuracy：{}，scaleType：{}，设置后计算总分：{}。",accuracy,scaleType,ovarallScore);
        overallscore.setText(ovarallScore.toString());

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
            if ("1".equals(scaleType)) {
                // 四舍五入
                ovarallScore = ovarallScore.setScale(Integer.parseInt(accuracy), RoundingMode.HALF_UP);
            } else {
                ovarallScore = ovarallScore.setScale(Integer.parseInt(accuracy), Integer.parseInt(scaleType));
            }
        }
        return ovarallScore;
    }

    public void initAssessDimDetailEntry() {

        String realOrAnoymous = this.getModel().getDataEntity().getString("assessact.realoranoymous");
        if (ActAnonymousEnum.ANONYMOUS.getCode().equals(realOrAnoymous)) {
            // 匿名不展示详情分录
            return;
        }
        long id = this.getModel().getDataEntity().getLong(HRBaseConstants.ID);
        DynamicObject[] assessTasks = ASSESS_OBJ_DOMAIN_SERVICE.queryAssessTaskByObjId(id);
        if (assessTasks.length == 0) {
            Container dimassDetail = this.getView().getControl("dimassdetail");
            dimassDetail.setInvisible(true);
        } else {
            AbstractFormDataModel model = (AbstractFormDataModel) this.getModel();
            model.beginInit();
            TableValueSetter vs = new TableValueSetter();
            vs.addField("assesstask");
            vs.addField("asserweight");

            for (DynamicObject task : assessTasks) {
                Long taskId = task.getLong(HRBaseConstants.ID);
                vs.addRow(taskId, assembleAsserWeight(taskId));
            }
            model.batchCreateNewEntryRow("dimentryentity", vs);
            model.endInit();
        }
    }

    /**
     * 测评人权重
     * @param taskId
     * @return
     */
    private String assembleAsserWeight(Long taskId){
        DynamicObject task = ASSESS_TASK_ENTITY_SERVICE.queryOne(taskId);
        return String.valueOf(DIMASSESSER_ENTITY_SERVICE.queryOne(task.getLong("dimassesser.id")).getBigDecimal("dimweight")) + "%";
    }

    /**
     * 映射评分分值
     *
     * @param ovarallScore
     * @return
     */
    public BigDecimal transScoreMapWithScale(BigDecimal ovarallScore) {

        long assFormId = this.getModel().getDataEntity().getLong("dimgroup.assessform.id");
        DynamicObject levelScore = ASSESS_OBJ_DOMAIN_SERVICE.queryPerfLevelByAssFormId(assFormId);
        // 分数转换
        long scoreSystemId = this.getModel().getDataEntity().getLong("assessact.scoresystem.id");
        DynamicObject actScore = ASSESS_OBJ_DOMAIN_SERVICE.queryScoreSystemById(scoreSystemId);

        BigDecimal actMaxScore = actScore.getBigDecimal("maxscore");
        // 获取绩效等级的最大分数
        BigDecimal levelMaxScore = levelScore.getBigDecimal("maxscore");
        LOGGER.info("开始映射总分，表单等级分制得分：{}，活动配置分制：{}，映射前计算总分：{}。", levelMaxScore, actMaxScore, ovarallScore);

        ovarallScore = ovarallScore.multiply(actMaxScore.divide(levelMaxScore));

        LOGGER.info("结束映射总分，表单等级分制得分：{}，活动配置分制：{}，映射前计算总分：{}。", levelMaxScore, actMaxScore, ovarallScore);
        return ovarallScore;
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);

        String operateKey = args.getOperateKey();

        if ("assdetail".equals(operateKey)) {
            FormShowParameter baseShowParameter = new FormShowParameter();
            baseShowParameter.setFormId("cea_assessdetail");
            baseShowParameter.getOpenStyle().setShowType(ShowType.Modal);
            baseShowParameter.setStatus(OperationStatus.VIEW);

            EntryGrid entryGrid = this.getControl("dimentryentity");
            // 获取选中行，数组为行号，从0开始
            int[] selectRows = entryGrid.getSelectRows();
            if (selectRows.length == 0) {
                //列小漏斗筛选，平台筛选会清空选中行。并且筛选的时候父单据体无法联动子单据体
                entryGrid.selectRows(0);
                entryGrid.entryRowClick(0);
                return;
            }
            DynamicObject areaContent = entryGrid.getModel().getDataEntity(true).getDynamicObjectCollection("dimentryentity").get(selectRows[0]);
            long dimerId = areaContent.getDynamicObject("assesstask").getLong("dimassesser.id");
            if (dimerId == 0L) {
                this.getView().showErrorNotification(
                        ResManager.loadKDString("测评明细不存在，请联系管理员。", "AssessObjResultEdit_0", "cds-opmc-cea"));
                return;
            }
            baseShowParameter.setCustomParam(ASSESSFORMID, this.getPageCache().get(ASSESSFORMID));
            baseShowParameter.setCustomParam("dimerid", dimerId);
            long scoreSystem = this.getModel().getDataEntity().getLong("assessact.scoresystem.id");
            baseShowParameter.setCustomParam("scoresystem", scoreSystem);
            this.getView().showForm(baseShowParameter);
        }
    }
}