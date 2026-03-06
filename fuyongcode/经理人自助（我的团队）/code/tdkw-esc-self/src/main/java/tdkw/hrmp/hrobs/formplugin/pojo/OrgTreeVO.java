package tdkw.hrmp.hrobs.formplugin.pojo;

import java.util.List;

/**
 * @author xxx
 * @date 2024年01月31日 12:03
 * @version: 1.0
 */
public class OrgTreeVO {
    private String orgId;

    private String orgName;

    private String pid;

    private String sortcode;

    private boolean hasChildren = false;

    private List<OrgTreeVO> children;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSortcode() {
        return sortcode;
    }

    public void setSortcode(String sortcode) {
        this.sortcode = sortcode;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public List<OrgTreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<OrgTreeVO> children) {
        this.children = children;
    }
}
