package tdkw.opa.tdkw_opa.formplugin.enums;

import java.math.BigDecimal;

/**
 * @author: xxx
 * @create: 2024/08/21 11:04
 * @description:
 **/
public class LightingDetailsVO {

    /**
     * 指标名称
     */
    private String targetName;

    /**
     * 指标描述
     */
    private String targetDescription;

    /**
     * 评分标准
     */
    private String targetScoreStandard;

    /**
     * Q1完成情况
     */
    private String firstCompletion;

    /**
     * Q2完成情况
     */
    private String secondCompletion;

    /**
     * Q3完成情况
     */
    private String thirdCompletion;

    /**
     * Q4完成情况
     */
    private String fourthCompletion;

    /**
     * 年度完成情况
     */
    private String yearCompletion;

    private String targetNumber;

    public String getTargetNumber() {
        return targetNumber;
    }

    public void setTargetNumber(String targetNumber) {
        this.targetNumber = targetNumber;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }

    public String getTargetScoreStandard() {
        return targetScoreStandard;
    }

    public void setTargetScoreStandard(String targetScoreStandard) {
        this.targetScoreStandard = targetScoreStandard;
    }

    public String getFirstCompletion() {
        return firstCompletion;
    }

    public void setFirstCompletion(String firstCompletion) {
        this.firstCompletion = firstCompletion;
    }

    public String getSecondCompletion() {
        return secondCompletion;
    }

    public void setSecondCompletion(String secondCompletion) {
        this.secondCompletion = secondCompletion;
    }

    public String getThirdCompletion() {
        return thirdCompletion;
    }

    public void setThirdCompletion(String thirdCompletion) {
        this.thirdCompletion = thirdCompletion;
    }

    public String getFourthCompletion() {
        return fourthCompletion;
    }

    public void setFourthCompletion(String fourthCompletion) {
        this.fourthCompletion = fourthCompletion;
    }

    public String getYearCompletion() {
        return yearCompletion;
    }

    public void setYearCompletion(String yearCompletion) {
        this.yearCompletion = yearCompletion;
    }
}
