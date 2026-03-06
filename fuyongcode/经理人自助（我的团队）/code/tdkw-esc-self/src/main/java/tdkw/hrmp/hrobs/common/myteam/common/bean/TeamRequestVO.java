package tdkw.hrmp.hrobs.common.myteam.common.bean;

import java.io.Serializable;
import java.util.List;

public class TeamRequestVO implements Serializable {
    private static final long serialVersionUID = -1l;

    /**
     * 姓名
     */
    private String name;
    /**
     * 是否兼职
     */
    private String isPartTime;
    /**
     * 年龄
     */
    private int ageStart;

    /**
     * 年龄
     */
    private int ageEnd;

    /**
     * 司龄
     */
    private double SeniorityStart;
    /**
     * 司龄
     */
    private double SeniorityEnd;
    /**
     * 工龄
     */
    private double socialWorkAgeStart;
    /**
     * 工龄
     */
    private double socialWorkAgeEnd;
    /**
     * 所属组织
     */
    private Long adminorg;



    /**
     * 部门
     */
    private Long department;
    /**
     * 任职类型
     */
    private List<Long> postype;
    /**
     * 学历
     */
    private Long education;

    /**
     * 职能团队
     */
    private Long team;

    /**
     * 职能团队
     */
    private Long school;

    /**
     * 职能团队
     */
    private String schoolName;

    /**
     * 职能团队编码
     */
    private String teamNumber;

    /**
     * 专业
     */
    private String speciality;

    /**
     * 职能团队
     */
    private List<Long> positionList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAgeStart() {
        return ageStart;
    }

    public void setAgeStart(int ageStart) {
        this.ageStart = ageStart;
    }

    public int getAgeEnd() {
        return ageEnd;
    }

    public void setAgeEnd(int ageEnd) {
        this.ageEnd = ageEnd;
    }

    public double getSeniorityStart() {
        return SeniorityStart;
    }

    public void setSeniorityStart(double seniorityStart) {
        SeniorityStart = seniorityStart;
    }

    public double getSeniorityEnd() {
        return SeniorityEnd;
    }

    public void setSeniorityEnd(double seniorityEnd) {
        SeniorityEnd = seniorityEnd;
    }

    public double getSocialWorkAgeStart() {
        return socialWorkAgeStart;
    }

    public void setSocialWorkAgeStart(double socialWorkAgeStart) {
        this.socialWorkAgeStart = socialWorkAgeStart;
    }

    public double getSocialWorkAgeEnd() {
        return socialWorkAgeEnd;
    }

    public void setSocialWorkAgeEnd(double socialWorkAgeEnd) {
        this.socialWorkAgeEnd = socialWorkAgeEnd;
    }

    public Long getAdminorg() {
        return adminorg;
    }

    public void setAdminorg(Long adminorg) {
        this.adminorg = adminorg;
    }

    public Long getEducation() {
        return education;
    }

    public void setEducation(Long education) {
        this.education = education;
    }

    public Long getTeam() {
        return team;
    }

    public void setTeam(Long team) {
        this.team = team;
    }

    public String getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(String teamNumber) {
        this.teamNumber = teamNumber;
    }

    public List<Long> getPositionList() {
        return positionList;
    }

    public void setPositionList(List<Long> positionList) {
        this.positionList = positionList;
    }

    public String getIsPartTime() {
        return isPartTime;
    }

    public void setIsPartTime(String isPartTime) {
        this.isPartTime = isPartTime;
    }

    public Long getSchool() {
        return school;
    }

    public void setSchool(Long school) {
        this.school = school;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public List<Long> getPostype() {
        return postype;
    }

    public void setPostype(List<Long> postype) {
        this.postype = postype;
    }

    public Long getDepartment() {
        return department;
    }

    public void setDepartment(Long department) {
        this.department = department;
    }
}
