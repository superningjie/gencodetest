package tdkw.hrmp.hrobs.common.myteam.common.excel;

import java.util.List;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/8/3-12:56
 * @description 用于存储前端获取的人员简洁信息
 */


public class MyTeamBriefInfoBean {

    /**
     * 人员id
     */
    String id;



    /**
     * 岗位id
     */
    String positionid;

    /**
     * 岗位名称
     */
    String position;

    /**
     * 部门id
     */
    String departmentid;

    /**
     * 部门名称
     */
    String department;

    List<MyTeamBriefInfoBean> children;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<MyTeamBriefInfoBean> getChildren() {
        return children;
    }

    public void setChildren(List<MyTeamBriefInfoBean> children) {
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPositionid() {
        return positionid;
    }

    public void setPositionid(String positionid) {
        this.positionid = positionid;
    }

    public String getDepartmentid() {
        return departmentid;
    }

    public void setDepartmentid(String departmentid) {
        this.departmentid = departmentid;
    }
}
