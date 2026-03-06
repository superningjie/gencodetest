package tdkw.hrmp.hrobs.common.myteam.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xxx
 * @Date 2023/8/21 11:55
 */
public class SubordinatePersonnelVo implements Serializable {
    private static final long serialVersionUID = -1l;

    /**
     * 人员id
     */
    private String id;
    /**
     * 上级id
     */
    private String managerId;
    /**
     * 上级岗位id
     */
    private String managerPositionId;
    /**
     * 是否离职前最新任职
     */
    private String islatestrecord;
    /**
     * 头像
     */
    private String ion;
    /**
     * 人员名称
     */
    private String name;
    /**
     * 岗位名称
     */
    private String position;
    /**
     * 部门名称
     */
    private String department;
    /**
     * 岗位id
     */
    private String positionid;
    /**
     * 部门id
     */
    private String departmentid;
    /**
     * 邮箱
     */
    private String mail;
    /**
     * 联系电话
     */
    private String phone;
    private List<SubordinatePersonnelVo> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIslatestrecord() {
        return islatestrecord;
    }

    public void setIslatestrecord(String islatestrecord) {
        this.islatestrecord = islatestrecord;
    }

    public String getIon() {
        return ion;
    }

    public void setIon(String ion) {
        this.ion = ion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<SubordinatePersonnelVo> getChildren() {
        return children;
    }

    public void setChildren(List<SubordinatePersonnelVo> children) {
        this.children = children;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getManagerPositionId() {
        return managerPositionId;
    }

    public void setManagerPositionId(String managerPositionId) {
        this.managerPositionId = managerPositionId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    /**
     * 当有多个同层级的组织时先按照index排序，再按照number排序
     * @param child 组织实体
     */
    public void addChild(SubordinatePersonnelVo child) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(child);

    }
}
