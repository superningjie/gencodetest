package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.bo.AssObjScoItemInstEntityBO;
import cds0.opmc.cea.business.bo.AssessTaskEntityBO;
import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.AssessTaskStatusEnum;
import cds0.opmc.cea.common.enums.AssessFormTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ClientProperties;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.IPageCache;
import kd.bos.form.control.EntryGrid;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GradeExamplePlugin extends HRDataBaseEdit {
    private static final Log LOG = LogFactory.getLog(DimSettingEdit.class);
    private final static String KEY_ENTRYENTITY = "cds0_entryentity";
    private static final PerformanceLevelEntityService performanceLevelEntityService = PerformanceLevelEntityService.getInstance();
    private static final AssObjScoItemInstEntityService assObjScoItemInstEntityService = AssObjScoItemInstEntityService.getInstance();
    private static final AssessFormRowEntityService assessFormRowEntityService = AssessFormRowEntityService.getInstance();
    protected static final AssessTaskEntityService assessTaskEntityService = AssessTaskEntityService.getInstance();
    protected static final DimassesserEntityService dimassesserEntityService = DimassesserEntityService.getInstance();
    @Override
    public void createNewData(BizDataEventArgs e) {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessFormRowId = parameter.getCustomParam("assessFormRowId");
        DynamicObject assessFormRow = assessFormRowEntityService.queryByPk(assessFormRowId);
        //遍历改表头，隐藏不需要展示的列
        hideAndAssignment(assessFormRow);
        List<Long> dimAssessorIds = parameter.getCustomParam("dimAssessorIds");
        DynamicObject dataEntity = new DynamicObject(this.getModel().getDataEntityType());
        DynamicObjectCollection rows = dataEntity.getDynamicObjectCollection(KEY_ENTRYENTITY);
        DynamicObject[] assObjScoItemInstList = assObjScoItemInstEntityService.queryByAssessFormRowIdAndDimAssessorIds(assessFormRowId, dimAssessorIds);
        //根据测评人与测评表获取实例表数据赋值
        Map<String, String> idSignMap = JSON.parseObject(this.getPageCache().get("idSign"), new TypeReference<HashMap<String, String>>() {});
        for (DynamicObject assObjScoItemInst : assObjScoItemInstList) {
            DynamicObject newRow = new DynamicObject(rows.getDynamicObjectType());
            newRow.set("cds0_basedatafield2", assObjScoItemInst.get("id"));
            newRow.set("cds0_textfield2", assObjScoItemInst.get("id"));
            newRow.set("cds0_textfield4", assObjScoItemInst.get("assessobj.id"));
            newRow.set("cds0_basedatafield1", assObjScoItemInst.get("assessobj.person"));
            if (HRStringUtils.isNotEmpty(assObjScoItemInst.getString("levelentry")) && HRStringUtils.isNotEmpty(idSignMap.get(assObjScoItemInst.getString("levelentry")))) {
                newRow.set(idSignMap.get(assObjScoItemInst.getString("levelentry")), assObjScoItemInst.getString("scorevalue"));
            }
            rows.add(newRow);
        }
        e.setDataEntity(dataEntity);
    }
    @Override
    public void afterCreateNewData(EventObject e) {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        //赋值顺序 名称 描述 权重/分值
        Long assessFormRowId = parameter.getCustomParam("assessFormRowId");
        DynamicObject assessFormRow = assessFormRowEntityService.queryByPk(assessFormRowId);
        this.getModel().setValue("cds0_basedatafield", assessFormRowId);
        isCompleted(true);
        String indicatorWeight = assessFormRow.getString("indicatorweight");
        String indicatorScore = assessFormRow.getString("indicatorscore");
        if (!HRStringUtils.equals(indicatorWeight, "0") && !HRStringUtils.equals(indicatorWeight, "0.00")) {
            this.getModel().setValue("cds0_textfield31", indicatorWeight);
        } else {
            this.getModel().setValue("cds0_textfield31", new BigDecimal(indicatorScore).setScale(2));
        }
        if (HRStringUtils.isNotEmpty(assessFormRow.getString("indicatorindex"))) {
            this.getModel().setValue("cds0_textfield1", "、");
        }
        //加载完成后改变父页面进度字段，出发进度计算
        IFormView subView = this.getView().getView(this.getView().getParentView().getPageId());
        subView.getModel().setValue("cds0_textfield4", new Date() + UUID.randomUUID().toString());
        this.getView().sendFormAction(subView);
        Boolean isShow = parameter.getCustomParam("isShow");
        if(isShow != null && isShow){
            IPageCache iPageCache = this.getView().getParentView().getService(IPageCache.class);
            int dimAssessorNum = Integer.valueOf(iPageCache.get("dimAssessors"));
            int[] freezeRows = new int[dimAssessorNum];
            for (int i = 0; i <= dimAssessorNum - 1; i++) {
                freezeRows[i] = i;
            }
            EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY);
            entryGrid.setRowLock(true, freezeRows);
        }
        this.getModel().setValue("cds0_largetextfield", assessFormRow.getString("indicatordesc"));
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessFormRowId = parameter.getCustomParam("assessFormRowId");
        String name = e.getProperty().getName();
        Map<String, String> signMeterHeaderMap = JSON.parseObject(this.getPageCache().get("signMeterHeader"), new TypeReference<HashMap<String, String>>() {});
        Map<String, String> signIdMap = JSON.parseObject(this.getPageCache().get("signId"), new TypeReference<HashMap<String, String>>() {});
        Map<String, String> idSignMap = JSON.parseObject(this.getPageCache().get("idSign"), new TypeReference<HashMap<String, String>>() {});
        String headerName;
        if (name.contains("levelmap")) {
            //如果测评项数值改变
            headerName = signMeterHeaderMap.get(name);
            BigDecimal minScore;
            BigDecimal maxScore;
            boolean isSingleChoice = Boolean.valueOf(this.getPageCache().get("isSingleChoice"));
            int index = Integer.valueOf(this.getPageCache().get("index"));
            for (ChangeData changeData: e.getChangeSet()) {
                int rowIndex = changeData.getRowIndex();
                //如果测评项是打分
                if (name.contains("cds0_levelmap")){
                    //数值校验
                    BigDecimal newValue;
                    if (changeData.getNewValue() == null) {
                        newValue = null;
                    } else {
                        newValue = (BigDecimal) changeData.getNewValue();
                    }
                    if (newValue != null && newValue.compareTo(new BigDecimal("0")) != 0) {
                        String oldValue;
                        if (changeData.getOldValue() == null) {
                            oldValue = null;
                        } else {
                            oldValue = changeData.getOldValue().toString();
                        }
                        if (!isSingleChoice) {
                            boolean fail = false;
                            if (headerName.contains("(")) {
                                minScore = new BigDecimal(headerName.substring(headerName.indexOf("(") + 1, headerName.indexOf("-")));
                                if (minScore.compareTo(newValue) >= 0) {
                                    fail = true;
                                }
                            } else {
                                minScore =  new BigDecimal(headerName.substring(headerName.indexOf("[") + 1, headerName.indexOf("-")));
                                if (minScore.compareTo(newValue) > 0) {
                                    fail = true;
                                }
                            }
                            if (headerName.contains(")")) {
                                maxScore = new BigDecimal(headerName.substring(headerName.indexOf("-") + 1, headerName.indexOf(")")));
                                if (maxScore.compareTo(newValue) <= 0) {
                                    fail = true;
                                }
                            } else {
                                maxScore = new BigDecimal(headerName.substring(headerName.indexOf("-") + 1, headerName.indexOf("]")));
                                if (maxScore.compareTo(newValue) < 0) {
                                    fail = true;
                                }
                            }
                            //数值有误则提示，清空错误值
                            if (fail) {
                                cleanErrorValue(name, oldValue, rowIndex, ResManager.loadKDString("输入分值应在设定区间内：","GradeExamplePlugin_0", AppflgConstant.KEY_APP_NAME) + headerName);
                                return;
                            }
                        }
                        //无误则清空其他测评项
                        for (int i = 1;i <= index + 1;i++) {
                            String sign = name.substring(0, name.length()-1) + i;
                            if (!HRStringUtils.equals(sign, name)) {
                                this.getModel().initValue(sign, null, rowIndex);
                            }
                        }
                        this.getView().updateView(KEY_ENTRYENTITY);
                    }
                } else {
                    //清空其他测评项
                    Boolean newValue;
                    if (changeData.getNewValue() == null) {
                        newValue = null;
                    } else {
                        newValue = (Boolean) changeData.getNewValue();
                    }
                    if (newValue != null && newValue != false) {
                        for (int i = 1;i <= index + 1;i++) {
                            String sign = name.substring(0, name.length()-1) + i;
                            if (!HRStringUtils.equals(sign, name)) {
                                this.getModel().initValue(sign, null, rowIndex);
                            }
                        }
                        this.getView().updateView(KEY_ENTRYENTITY);
                    }
                }
            }
            //数值处理成功后，改变父页面进度字段，触发进度计算
            isCompleted(false);
        } else if (HRStringUtils.equals(name, "cds0_save")) {
            //保存
            IPageCache iPageCache = this.getView().getParentView().getService(IPageCache.class);
            DynamicObjectCollection dynamicObjects = this.getModel().getEntryEntity(KEY_ENTRYENTITY);
            List<AssObjScoItemInstEntityBO> AssObjScoItemInstEntityBOList = new ArrayList<>();
            Set<Long> assessObjectIds = new HashSet<>();
            Map<String, Integer> checkOneIndexMap = new HashMap<>();
            List<Long> dimAssessorIds = parameter.getCustomParam("dimAssessorIds");
            HashMap<String, Integer> userScoreMap = JSON.parseObject(iPageCache.get("userScoreMap"), new TypeReference<HashMap<String, Integer>>() {});
            if (userScoreMap == null) {
                userScoreMap = new HashMap<>();
            }
            //获取所有测评项数据，校验
            for (DynamicObject dynamicObject : dynamicObjects) {
                Long id = dynamicObject.getLong("cds0_textfield2");
                Long assessObjectId = dynamicObject.getLong("cds0_textfield4");
                AssObjScoItemInstEntityBO assObjScoItemInst = getScore(signIdMap, signMeterHeaderMap, id, dynamicObject);
                if (HRStringUtils.isEmpty(assObjScoItemInst.getScoreValue())) {
                    //说明有行没分，要报错
                    DynamicObject assessFormRow = assessFormRowEntityService.queryByPk(assessFormRowId);
                    this.getView().showMessage(ResManager.loadKDString("序号为","SubmitCheck_0", AppflgConstant.KEY_APP_NAME) + assessFormRow.getString("indicatorindex") + ResManager.loadKDString("的","SubmitCheck_1", AppflgConstant.KEY_APP_NAME) + assessFormRow.getString("indicatorname") + ResManager.loadKDString("测评项有测评指标还未评价，完成后再提交！","SubmitCheck_2", AppflgConstant.KEY_APP_NAME));
                    IFormView subView = this.getView().getView(this.getView().getParentView().getPageId());
                    subView.getModel().setValue("cds0_issuccess", "error_" + new Date().getTime());
                    this.getView().sendFormAction(subView);
                    iPageCache.remove("itemInstList");
                    iPageCache.remove("assessTaskList");
                    iPageCache.remove("dimAssessorList");
                    iPageCache.remove("assessObjectIdList");
                    iPageCache.remove("evalDimId");
                    iPageCache.remove("checkOneIndexValueFail");
                    iPageCache.remove("userScoreMap");
                    return;
                }
                AssObjScoItemInstEntityBOList.add(assObjScoItemInst);
                assessObjectIds.add(assessObjectId);
                //最高分值校验
                String sign = idSignMap.get(assObjScoItemInst.getLevelEntry());
                if (HRStringUtils.isNotEmpty(sign) && HRStringUtils.equals(sign.substring(sign.length() - 1), "1")) {
                    if (checkOneIndexMap.get(assObjScoItemInst.getLevelEntry()) != null) {
                        checkOneIndexMap.put(assObjScoItemInst.getLevelEntry(), checkOneIndexMap.get(assObjScoItemInst.getLevelEntry()) + 1);
                    } else {
                        checkOneIndexMap.put(assObjScoItemInst.getLevelEntry(), 1);
                    }
                    if (userScoreMap.get(assessObjectId + "-" + assObjScoItemInst.getLevelEntry() + "-" + assObjScoItemInst.getScoreValue()) != null) {
                        userScoreMap.put(assessObjectId + "-" + assObjScoItemInst.getLevelEntry() + "-" + assObjScoItemInst.getScoreValue(), userScoreMap.get(assessObjectId + "-" + assObjScoItemInst.getLevelEntry() + "-" + assObjScoItemInst.getScoreValue()) + 1);
                    } else {
                        userScoreMap.put(assessObjectId + "-" + assObjScoItemInst.getLevelEntry() + "-" + assObjScoItemInst.getScoreValue(), 1);
                    }
                }
            }
            checkOneIndexMap.entrySet().forEach(entry -> {
                if (entry.getValue() >= dimAssessorIds.size()) {
                    iPageCache.put("checkOneIndexValueFail", JSON.toJSONString(true));
                }
            });
            iPageCache.put("userScoreMap", JSON.toJSONString(userScoreMap));
            //任务状态改为已处理
            DynamicObject[] assessTasks = assessTaskEntityService.queryByDimAssessor(dimAssessorIds);
            List<AssessTaskEntityBO> assessTaskBOList = new ArrayList<>();
            Arrays.stream(assessTasks).forEach(item -> {
                AssessTaskEntityBO assessTaskEntityBO = new AssessTaskEntityBO();
                assessTaskEntityBO.setId(item.getLong("id"));
                assessTaskEntityBO.setAssesTaskStatus(AssessTaskStatusEnum.PROCESSED.getValue());
                assessTaskBOList.add(assessTaskEntityBO);
            });
            //维度测评人状态改为已填报
            DynamicObject[] dimAssessors = dimassesserEntityService.queryByIds(dimAssessorIds);
            List<Long> dimAssessorIdList = new ArrayList<>();
            Arrays.stream(dimAssessors).forEach(item -> {
                dimAssessorIdList.add(item.getLong("id"));
            });
            //存缓存
            List<AssObjScoItemInstEntityBO> itemInstList = JSON.parseArray(iPageCache.get("itemInstList"), AssObjScoItemInstEntityBO.class);
            if (itemInstList == null) {
                itemInstList = new ArrayList<>();
            }
            itemInstList.addAll(AssObjScoItemInstEntityBOList);
            iPageCache.put("itemInstList", JSON.toJSONString(itemInstList));
            List<AssessTaskEntityBO> assessTaskList = JSON.parseArray(iPageCache.get("assessTaskList"), AssessTaskEntityBO.class);
            if (assessTaskList == null) {
                assessTaskList = new ArrayList<>();
            }
            assessTaskList.addAll(assessTaskBOList);
            iPageCache.put("assessTaskList", JSON.toJSONString(assessTaskList));
            List<Long> dimAssessorList = JSON.parseArray(iPageCache.get("dimAssessorList"), Long.class);
            if (dimAssessorList == null) {
                dimAssessorList = new ArrayList<>();
            }
            dimAssessorList.addAll(dimAssessorIdList);
            iPageCache.put("dimAssessorList", JSON.toJSONString(dimAssessorList));
            Set<Long> assessObjectIdList = JSON.parseObject(iPageCache.get("assessObjectIdList"), new TypeReference<HashSet<Long>>() {});
            if (assessObjectIdList == null) {
                assessObjectIdList = new HashSet<>();
            }
            assessObjectIdList.addAll(assessObjectIds);
            iPageCache.put("assessObjectIdList", JSON.toJSONString(assessObjectIdList));
            iPageCache.put("evalDimId", parameter.getCustomParam("evalDimId").toString());
            //提交成功提示，改父页面字段，表示此条指标已完成保存（校验完毕后数据存缓存）
            IFormView subView = this.getView().getView(this.getView().getParentView().getPageId());
            subView.getModel().setValue("cds0_issuccess", this.getView().getPageId() + "_" + new Date().getTime());
            this.getView().sendFormAction(subView);
        } else if (HRStringUtils.equals(name , "cds0_savetemp")) {
            //暂存
            DynamicObjectCollection dynamicObjects = this.getModel().getEntryEntity(KEY_ENTRYENTITY);
            Map<Long,DynamicObject> dynamicObjectMap = Arrays.stream(assObjScoItemInstEntityService.loadDynamicObjectArray(dynamicObjects.stream().map(k->k.getLong("cds0_textfield2")).collect(Collectors.toList()).toArray())).collect(Collectors.toMap(k -> k.getLong("id"), v->v));
            //更新所有实例
            List<DynamicObject> dynamicObjectList = new ArrayList<>();
            for (DynamicObject dynamicObject : dynamicObjects) {
                Long id = dynamicObject.getLong("cds0_textfield2");
                AssObjScoItemInstEntityBO assObjScoItemInst = getScore(signIdMap, signMeterHeaderMap, id, dynamicObject);
                DynamicObject assObjScoItemInstDynamicObject = dynamicObjectMap.get(id);
                assObjScoItemInstDynamicObject.set("levelscore", assObjScoItemInst.getLevelScore());
                assObjScoItemInstDynamicObject.set("levelentry", assObjScoItemInst.getLevelEntry());
                assObjScoItemInstDynamicObject.set("scorevalue", assObjScoItemInst.getScoreValue());
                dynamicObjectList.add(assObjScoItemInstDynamicObject);
            }
            //任务状态改为暂存
            List<Long> dimAssessorIds = parameter.getCustomParam("dimAssessorIds");
            DynamicObject[] assessTasks = assessTaskEntityService.query("assestaskstatus", new QFilter[]{
                    new QFilter("dimassesser", QCP.in, dimAssessorIds)
            });
            Arrays.stream(assessTasks).forEach(item -> item.set("assestaskstatus", AssessTaskStatusEnum.TEMPSTORAGE.getValue()));
            TXHandle required = TX.required();
            try {
                assObjScoItemInstEntityService.update(dynamicObjectList.toArray(new DynamicObject[0]));
                assessTaskEntityService.update(assessTasks);
            } catch (Exception exception) {
                LOG.error("startupAssessObject 1 fail:", e);
                required.markRollback();
            } finally {
                required.close();
            }
        }else if (HRStringUtils.equals(name, "cds0_savesuccess")) {
            //若父页面表示保存成功后，冻结测评项
            for (ChangeData changeData: e.getChangeSet()) {
                if (HRStringUtils.equals(changeData.getNewValue().toString(), "1")) {
                    IPageCache iPageCache = this.getView().getParentView().getService(IPageCache.class);
                    int dimAssessorNum = Integer.valueOf(iPageCache.get("dimAssessors"));
                    int[] freezeRows = new int[dimAssessorNum];
                    for (int i = 0; i <= dimAssessorNum - 1; i++) {
                        freezeRows[i] = i;
                    }
                    EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY);
                    entryGrid.setRowLock(true, freezeRows);
                }
            }

        }
    }

    private void isCompleted(boolean isFirst) {
        //进度校验
        Map<String, String> signIdMap = JSON.parseObject(this.getPageCache().get("signId"), new TypeReference<HashMap<String, String>>() {});
        Map<String, String> signMeterHeaderMap = JSON.parseObject(this.getPageCache().get("signMeterHeader"), new TypeReference<HashMap<String, String>>() {});
        if (isFirst) {
            //初始化时，校验当前实例数据并计算，该指标是否已全部打分
            DynamicObjectCollection dynamicObjects = this.getModel().getEntryEntity(KEY_ENTRYENTITY);
            boolean isCompleted = true;
            for (DynamicObject dynamicObject : dynamicObjects) {
                Long id = dynamicObject.getLong("cds0_textfield2");
                AssObjScoItemInstEntityBO assObjScoItemInst = getScore(signIdMap, signMeterHeaderMap, id, dynamicObject);
                if (assObjScoItemInst.getScoreValue() == null || assObjScoItemInst.getScoreValue().equals("")) {
                    //说明有行没分
                    isCompleted = false;
                }
            }
            //计算结果存缓存
            if (isCompleted) {
                this.getPageCache().put(this.getView().getPageId() + "isCompleted", true + "");
            } else {
                this.getPageCache().put(this.getView().getPageId() + "isCompleted", false + "");
            }
        } else {
            //值改变时进行计算
            boolean isCompletedCache = Boolean.valueOf(this.getPageCache().get(this.getView().getPageId() + "isCompleted"));
            DynamicObjectCollection dynamicObjects = this.getModel().getEntryEntity(KEY_ENTRYENTITY);
            boolean isCompleted = true;
            for (DynamicObject dynamicObject : dynamicObjects) {
                Long id = dynamicObject.getLong("cds0_textfield2");
                AssObjScoItemInstEntityBO assObjScoItemInst = getScore(signIdMap, signMeterHeaderMap, id, dynamicObject);
                if (assObjScoItemInst.getScoreValue() == null || assObjScoItemInst.getScoreValue().equals("")) {
                    //说明有行没分
                    isCompleted = false;
                }
            }
            //根据之前计算结果与值改变后结果，进行完成情况计算。结果存缓存，并通知父页面
            if (isCompletedCache) {
                if (!isCompleted) {
                    IFormView subView = this.getView().getView(this.getView().getParentView().getPageId());
                    int num = Integer.valueOf(subView.getModel().getValue("cds0_integerfield").toString());
                    subView.getModel().setValue("cds0_integerfield", num - 1);
                    this.getView().sendFormAction(subView);
                    this.getPageCache().put(this.getView().getPageId() + "isCompleted", false + "");
                }
            } else {
                if (isCompleted) {
                    IFormView subView = this.getView().getView(this.getView().getParentView().getPageId());
                    int num = Integer.valueOf(subView.getModel().getValue("cds0_integerfield").toString());
                    subView.getModel().setValue("cds0_integerfield", num + 1);
                    this.getView().sendFormAction(subView);
                    this.getPageCache().put(this.getView().getPageId() + "isCompleted", true + "");
                }
            }
        }
    }

    private void cleanErrorValue(String name, String oldValue, int rowIndex, String msg) {
        this.getModel().setValue(name, oldValue, rowIndex);
        this.getView().showErrorNotification(msg);
    }

    private AssObjScoItemInstEntityBO getScore(Map<String, String> signIdMap, Map<String, String> signMeterHeaderMap, Long id, DynamicObject dynamicObject) {
        AssObjScoItemInstEntityBO assObjScoItemInst = new AssObjScoItemInstEntityBO();
        assObjScoItemInst.setId(id);
        String sign;
        String signName = null;
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessActivityId = parameter.getCustomParam("assessActivityId");
        boolean isSingleChoice = Boolean.valueOf(this.getPageCache().get("isSingleChoice"));
        int index = Integer.valueOf(this.getPageCache().get("index"));
        if (isSingleChoice) {
            sign = "cds0_checklevelmap";
        } else {
            sign = "cds0_levelmap";
        }
        //获取页面数据
        for (int i = 1;i <= index;i++) {
            String value = dynamicObject.getString(sign + i);
            if (HRStringUtils.isNotEmpty(value) && !HRStringUtils.equals(value, "0") && !HRStringUtils.equals(value, "0.0") && !HRStringUtils.equals(value, "0.00") && !HRStringUtils.equals(value, "false")) {
                assObjScoItemInst.setScoreValue(value);
                assObjScoItemInst.setLevelEntry(signIdMap.get(sign + i));
                assObjScoItemInst.setLevelScore(value);
                signName = sign + i;
            }
        }
        //若是打等则分值换算
        if (isSingleChoice && signName != null) {
            String headerName = signMeterHeaderMap.get(signName);
            assObjScoItemInst.setLevelScore(headerName.substring(headerName.indexOf("(") + 1, headerName.indexOf(")")));
        }
        assObjScoItemInst.setMapScore(performanceLevelEntityService.transScore(assObjScoItemInst.getLevelScore(), assessActivityId, this.getPageCache().get("levelmap")));
        return assObjScoItemInst;
    }

    private void hideAndAssignment(DynamicObject assessFormRow) {
        DynamicObject levelMap = performanceLevelEntityService.queryByNumber(assessFormRow.getString("levelmap"));
        DynamicObject levelMapDynamicObject = performanceLevelEntityService.loadSingle(levelMap.get("id"));
        DynamicObjectCollection scoreMapEntry = levelMapDynamicObject.getDynamicObjectCollection("scoremapentryentity");
        String minScore = scoreMapEntry.get(0).getString("scoresystem.minscore");
        String maxScore = scoreMapEntry.get(0).getString("scoresystem.maxscore");
        DynamicObjectCollection scoreSubs = scoreMapEntry.get(0).getDynamicObjectCollection("scoresubentryentity");
        int index = 1;
        //前端渲染用
        Map<String, String> idSignMap = new HashMap<>();
        //保存暂存用(获取有值的标识对应是哪个绩效等级 获取后保存id)
        Map<String, String> signIdMap = new HashMap<>();
        //分值校验用
        Map<String, String> signMeterHeaderMap = new HashMap<>();
        EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY);
        DecimalFormat decimalFormat = new DecimalFormat("0");
        LocaleString name;
        String id;
        String minScoreValue;
        String minOperation;
        String minSymbol;
        String maxScoreValue;
        String maxOperation;
        String maxSymbol;
        String defaultScore;
        int scoreNum = 0;
        int gradeNum = 0;
        boolean isSingleChoice = false;
        for (DynamicObject scoreSub : scoreSubs) {
            id = scoreSub.getString("id");
            name = new LocaleString(scoreSub.getString("scorelevel"));
            String sign;
            //若是打分项，则计算分值区间存缓存，校验用
            if (HRStringUtils.equals(assessFormRow.getString("indicatortype"), AssessFormTypeEnum.SCORE.getValue())) {
                isSingleChoice = false;
                sign = "cds0_levelmap";
                scoreNum++;
                minScoreValue = scoreSub.getString("minscore");
                minOperation = scoreSub.getString("scoreminoperation");
                maxScoreValue = scoreSub.getString("maxscore");
                maxOperation = scoreSub.getString("scoremaxoperation");
                if (minScoreValue == null || HRStringUtils.equals(minScoreValue, "0.000000")) {
                    minScoreValue = minScore;
                }
                if (maxScoreValue == null || HRStringUtils.equals(maxScoreValue, "0.000000")) {
                    maxScoreValue = maxScore;
                }
                if (minOperation != null && HRStringUtils.equals(minOperation, "10")) {
                    minSymbol = "(";
                } else {
                    minSymbol = "[";
                }
                if (maxOperation != null && HRStringUtils.equals(maxOperation, "10")) {
                    maxSymbol = ")";
                } else {
                    maxSymbol = "]";
                }
                minScoreValue = decimalFormat.format(Double.valueOf(minScoreValue));
                maxScoreValue = decimalFormat.format(Double.valueOf(maxScoreValue));
                entryGrid.setColumnProperty(sign + index, ClientProperties.Header, name);
                signMeterHeaderMap.put(sign + index++, minSymbol + minScoreValue + "-" + maxScoreValue + maxSymbol);
            } else {
                isSingleChoice = true;
                gradeNum++;
                sign = "cds0_checklevelmap";
                defaultScore = scoreSub.getString("defaultscore");
                entryGrid.setColumnProperty(sign + index, ClientProperties.Header, name);
                signMeterHeaderMap.put(sign + index++, "(" + decimalFormat.format(Double.valueOf(defaultScore)) + ")");
            }
            idSignMap.put(id, sign + index);
            signIdMap.put(sign + index, id);
        }
        for (int i = 1 + scoreNum;i <= 7;i++) {
            this.getView().setVisible(false, "cds0_levelmap" + i);
        }
        for (int i = 1 + gradeNum;i <= 7;i++) {
            this.getView().setVisible(false, "cds0_checklevelmap" + i);
        }
        this.getPageCache().put("levelmap", assessFormRow.getString("levelmap"));
        this.getPageCache().put("idSign", JSON.toJSONString(idSignMap));
        this.getPageCache().put("signId", JSON.toJSONString(signIdMap));
        this.getPageCache().put("signMeterHeader", JSON.toJSONString(signMeterHeaderMap));
        this.getPageCache().put("index", scoreSubs.size() + "");
        this.getPageCache().put("isSingleChoice", isSingleChoice + "");
        this.getView().getParentView().getPageCache().put("scoreSubNum", scoreSubs.size() + "");
    }
}
