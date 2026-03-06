package tdkw.opa.tdkw_opa.formplugin.enums;

import java.util.List;

/**
 * @author: xxx
 * @create: 2024/08/21 11:03
 * @description: 组织绩效地图-第二层-指标
 **/
public class OrgTargetVO {
    /**
     * 指标id
     */
    private String targetId;

    /**
     * 指标名称
     */
    private String targetName;

    /**
     * 指标描述
     */
    private String targetDescription;

    /**
     * 指标类型  控制指标显示哪些属性
     */
    private String targetType;

    /**
     * 目标值 拼接单位
     */
    private String goalValue;

    /**
     * 权重 拼接单位
     */
    private String weight;

    /**
     * 年度完成值  拼接单位
     */
    private String annualValue;

    /**
     * 指标分数 String 显示分数或者"/"
     */
    private String targetScore;

    /**
     * 标签
     */
    private TagVO tagVO;

    /**
     * 亮灯详情
     */
    private LightingDetailsVO lightingDetailsVO;

    /**
     * 指标详情
     */
    private List<OrgTargetVO> children;

    /**
     * 亮灯详情
     */
    private String light;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(String goalValue) {
        this.goalValue = goalValue;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAnnualValue() {
        return annualValue;
    }

    public void setAnnualValue(String annualValue) {
        this.annualValue = annualValue;
    }

    public String getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(String targetScore) {
        this.targetScore = targetScore;
    }

    public TagVO getTagVO() {
        return tagVO;
    }

    public void setTagVO(TagVO tagVO) {
        this.tagVO = tagVO;
    }

    public LightingDetailsVO getLightingDetailsVO() {
        return lightingDetailsVO;
    }

    public void setLightingDetailsVO(LightingDetailsVO lightingDetailsVO) {
        this.lightingDetailsVO = lightingDetailsVO;
    }

    public List<OrgTargetVO> getChildren() {
        return children;
    }

    public void setChildren(List<OrgTargetVO> children) {
        this.children = children;
    }
}
