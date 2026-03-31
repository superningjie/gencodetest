package tdkw.opa.tdkw_opa.formplugin.enums;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: xxx
 * @create: 2024/08/21 11:02
 * @description: 组织绩效地图-第一层-头部
 **/
public class OrgHeaderVO {

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 行政组织编码 1000000
     */
    private String orgNumber;

    /**
     * 组织 评估得分
     */
    private BigDecimal orgScore;

    /**
     * 指标详情
     */
    private List<OrgTargetVO> children;

    /**
     * 指标详情
     */
    private TagVO tagVO;

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

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public BigDecimal getOrgScore() {
        return orgScore;
    }

    public void setOrgScore(BigDecimal orgScore) {
        this.orgScore = orgScore;
    }

    public List<OrgTargetVO> getChildren() {
        return children;
    }

    public void setChildren(List<OrgTargetVO> children) {
        this.children = children;
    }

    public TagVO getTagVO() {
        return tagVO;
    }

    public void setTagVO(TagVO tagVO) {
        this.tagVO = tagVO;
    }
}
