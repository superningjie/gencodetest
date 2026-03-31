package tdkw.opa.tdkw_opa.formplugin.enums;

import java.util.Date;

/**
 * @author: xxx
 * @create: 2024/08/21 11:13
 * @description: 标签
 **/
public class TagVO {
    /**
     * 年份
     */
    private String year;

    /**
     * 指标类型名称
     */
    private String targetTypeName;

    /**
     * 组织名称
     */
    private String orgName;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTargetTypeName() {
        return targetTypeName;
    }

    public void setTargetTypeName(String targetTypeName) {
        this.targetTypeName = targetTypeName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}
