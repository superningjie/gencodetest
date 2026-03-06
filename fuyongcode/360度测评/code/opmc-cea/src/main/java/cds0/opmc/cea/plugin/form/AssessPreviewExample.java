package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessFormRowEntityService;
import cds0.opmc.cea.business.entityservice.PerformanceLevelEntityService;
import cds0.opmc.cea.common.enums.AssessFormTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ClientProperties;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.EntryGrid;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.constants.HRHisBaseConstants;
import kd.hr.hbp.common.constants.newhismodel.HisFieldNameConstants;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

public class AssessPreviewExample extends HRDataBaseEdit {
    private final static String KEY_ENTRYENTITY = "cds0_entryentity";
    private static final AssessFormRowEntityService assessFormRowEntityService = AssessFormRowEntityService.getInstance();
    private static final PerformanceLevelEntityService performanceLevelEntityService = PerformanceLevelEntityService.getInstance();
    @Override
    public void createNewData(BizDataEventArgs e) {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessFormRowId = parameter.getCustomParam("assessFormRowId");
        DynamicObject assessFormRow = assessFormRowEntityService.queryOne("indicatordesc, indicatorindex, indicatorweight, levelmap, indicatortype", assessFormRowId);
        //遍历改表头，隐藏不需要展示的列
        hideAndAssignment(assessFormRow);
    }
    @Override
    public void afterCreateNewData(EventObject e) {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessFormRowId = parameter.getCustomParam("assessFormRowId");
        this.getModel().setValue("cds0_basedatafield", assessFormRowId);
        this.getModel().setValue("cds0_textfield1", "、");
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        Map<String, String> signMeterHeaderMap = JSON.parseObject(this.getPageCache().get("signMeterHeader"), new TypeReference<HashMap<String, String>>() {});
        String headerName;
        if (name.contains("levelmap")) {
            headerName = signMeterHeaderMap.get(name);
            BigDecimal minScore;
            BigDecimal maxScore;
            boolean isSingleChoice = Boolean.valueOf(this.getPageCache().get("isSingleChoice"));
            int index = Integer.valueOf(this.getPageCache().get("index"));
            for (ChangeData changeData : e.getChangeSet()) {
                int rowIndex = changeData.getRowIndex();
                if (name.contains("cds0_levelmap")) {
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
                                minScore = new BigDecimal(headerName.substring(headerName.indexOf("[") + 1, headerName.indexOf("-")));
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
                            if (fail) {
                                cleanErrorValue(name, oldValue, rowIndex, "输入分值应在设定区间内：" + headerName);
                                return;
                            }
                        }
                        for (int i = 1; i <= index + 1; i++) {
                            String sign = name.substring(0, name.length() - 1) + i;
                            if (!sign.equals(name)) {
                                this.getModel().initValue(sign, null, rowIndex);
                            }
                        }
                        this.getView().updateView(KEY_ENTRYENTITY);
                    }
                } else {
                    Boolean newValue;
                    if (changeData.getNewValue() == null) {
                        newValue = null;
                    } else {
                        newValue = (Boolean) changeData.getNewValue();
                    }
                    if (newValue != null && newValue != false) {
                        for (int i = 1; i <= index + 1; i++) {
                            String sign = name.substring(0, name.length() - 1) + i;
                            if (!sign.equals(name)) {
                                this.getModel().initValue(sign, null, rowIndex);
                            }
                        }
                        this.getView().updateView(KEY_ENTRYENTITY);
                    }
                }
            }
        }
    }


    private void hideAndAssignment(DynamicObject assessFormRow) {
        DynamicObject levelMap = performanceLevelEntityService.queryOneByParam(assessFormRow);
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
            if (assessFormRow.get("indicatortype").equals(AssessFormTypeEnum.SCORE.getValue())) {
                isSingleChoice = false;
                sign = "cds0_levelmap";
                scoreNum++;
                minScoreValue = scoreSub.getString("minscore");
                minOperation = scoreSub.getString("scoreminoperation");
                maxScoreValue = scoreSub.getString("maxscore");
                maxOperation = scoreSub.getString("scoremaxoperation");
                if (minScoreValue == null || minScoreValue.equals("0.000000")) {
                    minScoreValue = minScore;
                }
                if (maxScoreValue == null || maxScoreValue.equals("0.000000")) {
                    maxScoreValue = maxScore;
                }
                if (minOperation != null && minOperation.equals("10")) {
                    minSymbol = "(";
                } else {
                    minSymbol = "[";
                }
                if (maxOperation != null && maxOperation.equals("10")) {
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

    private void cleanErrorValue(String name, String oldValue, int rowIndex, String msg) {
        this.getModel().setValue(name, oldValue, rowIndex);
        this.getView().showErrorNotification(msg);
    }
}
