package tdkw.esc.recruit.common.dto;

import java.math.BigDecimal;

/**
 * 同步北森招聘需求参数实体
 */
public class RecruitDemandDto {

    /**
     * 申请公司
     */
    private String applyCompany;
    /**
     * 申请部门
     */
    private String applyDept;
    /**
     * 截止时间
     */
    private String arrivalTime;
    /**
     * 头部实体
     */
//    private CpDto cp;
    /**
     * 申请日期
     */
    private String createDate;
    /**
     * 申请人姓名
     */
    private String createdBy;
    /**
     * 申请部门
     */
    private String createdDept;
    /**
     * 截至日期
     */
    private String deadline;
    /**
     * 学历要求
     */
    private String eduReq;
    /**
     * 员工编号
     */
    private String employeeNo;
    /**
     * 经验
     */
    private String experience;
    /**
     * 内部职位名称
     */
    private String inName;
    /**
     * 专业
     */
    private String major;
    /**
     * 最高薪酬(单位:千)
     */
    private BigDecimal maxSalary;
    /**
     * 最低薪酬(单位:千)
     */
    private BigDecimal minSalary;
    /**
     * 对外发布职位名称
     */
    private String outName;
    /**
     * 申请部门主键
     */
    private String pkDept;
    /**
     * 职位类别
     */
    private String postType;
    /**
     * 招聘人数
     */
    private Integer recruitNum;
    /**
     * 招聘原因
     */
    private String recruitReason;
    /**
     * 招聘来源
     */
    private String recruitSource;
    /**
     * 招聘类型
     */
    private String recruitType;
    /**
     * 岗位职责
     */
    private String responsibility;
    /**
     * 性别
     */
    private String sexSel;
    /**
     * 工作地点
     */
    private String workplace;
    /**
     * 下属人数
     */
    private Integer subordinateNum;
    /**
     * 任职要求
     */
    private String specification;
    /**
     * 招聘HR北森账号ID
     */
    private String userId;
    /**
     * 汇报对象-人员姓名
     */
    private String reportObject;

    public String getApplyCompany() {
        return applyCompany;
    }

    public void setApplyCompany(String applyCompany) {
        this.applyCompany = applyCompany;
    }

    public String getApplyDept() {
        return applyDept;
    }

    public void setApplyDept(String applyDept) {
        this.applyDept = applyDept;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

//    public CpDto getCp() {
//        return cp;
//    }
//
//    public void setCp(CpDto cp) {
//        this.cp = cp;
//    }


    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreatedDept() {
        return createdDept;
    }

    public void setCreatedDept(String createdDept) {
        this.createdDept = createdDept;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getEduReq() {
        return eduReq;
    }

    public void setEduReq(String eduReq) {
        this.eduReq = eduReq;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getInName() {
        return inName;
    }

    public void setInName(String inName) {
        this.inName = inName;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.maxSalary = maxSalary;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public String getOutName() {
        return outName;
    }

    public void setOutName(String outName) {
        this.outName = outName;
    }

    public String getPkDept() {
        return pkDept;
    }

    public void setPkDept(String pkDept) {
        this.pkDept = pkDept;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public Integer getRecruitNum() {
        return recruitNum;
    }

    public void setRecruitNum(Integer recruitNum) {
        this.recruitNum = recruitNum;
    }

    public String getRecruitReason() {
        return recruitReason;
    }

    public void setRecruitReason(String recruitReason) {
        this.recruitReason = recruitReason;
    }

    public String getRecruitSource() {
        return recruitSource;
    }

    public void setRecruitSource(String recruitSource) {
        this.recruitSource = recruitSource;
    }

    public String getRecruitType() {
        return recruitType;
    }

    public void setRecruitType(String recruitType) {
        this.recruitType = recruitType;
    }

    public String getResponsibility() {
        return responsibility;
    }

    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }

    public String getSexSel() {
        return sexSel;
    }

    public void setSexSel(String sexSel) {
        this.sexSel = sexSel;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public Integer getSubordinateNum() {
        return subordinateNum;
    }

    public void setSubordinateNum(Integer subordinateNum) {
        this.subordinateNum = subordinateNum;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReportObject() {
        return reportObject;
    }

    public void setReportObject(String reportObject) {
        this.reportObject = reportObject;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
