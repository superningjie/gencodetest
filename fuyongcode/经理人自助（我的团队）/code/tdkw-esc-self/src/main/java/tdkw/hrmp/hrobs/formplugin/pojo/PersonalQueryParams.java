package tdkw.hrmp.hrobs.formplugin.pojo;

import java.util.List;

/**
 * 人员花名册查询参数VO类
 *
 * @author xxx
 * @date 2024年01月31日 09:23
 * @version: 1.0
 */
public class PersonalQueryParams {

    // 分页页码，从1开始
    private Integer pageNo;

    // 分页每页的条数
    private Integer pageSize;

    // 方法名
    private String key;

    // 左侧组织树的id
    private String orgId;

    // 模糊查询，姓名，支持中文+拼音查询
    private String searchName;

    // 截止日期
    private String endDate;

    // 岗位层级
    private List<Integer> postLevel;

    // 模糊查询学校名称
    private String school;

    // 学历档案
    private List<String> degrees;

    // 年龄开始
    private Integer startAge;

    // 年龄结束
    private Integer endAge;

    // 集团司龄开始
    private Integer startGroupAge;

    // 集团司龄结束
    private Integer endGroupAge;

    // 专业
    private String major;

    // 婚姻状况，婚育
    private List<String> maritals;

    // 从事行业
    private List<String> industrys;

    // 性别
    private List<String> sexs;

    // 职层
    private List<String> ranks;

    // 岗位序列
    private List<String> sequences;

    // 政治面貌
    private List<String> politicals;

    // 工作地点
    private String workPlace;

    // 院校类别
    private List<String> schoolTypes;

    // 家属工作单位
    private String familyWork;

    // 人员类别
    private List<String> employTypes;

    // 岗位标签
    private List<String> subSequences;

    // 社会工龄开始
    private Integer startWorkAge;

    // 社会工龄结束
    private Integer endWorkAge;

    // 公司司龄开始
    private Integer startCorpAge;

    // 公司司龄结束
    private Integer endCorpAge;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<Integer> getPostLevel() {
        return postLevel;
    }

    public void setPostLevel(List<Integer> postLevel) {
        this.postLevel = postLevel;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public List<String> getDegrees() {
        return degrees;
    }

    public void setDegrees(List<String> degrees) {
        this.degrees = degrees;
    }

    public Integer getStartAge() {
        return startAge;
    }

    public void setStartAge(Integer startAge) {
        this.startAge = startAge;
    }

    public Integer getEndAge() {
        return endAge;
    }

    public void setEndAge(Integer endAge) {
        this.endAge = endAge;
    }

    public Integer getStartGroupAge() {
        return startGroupAge;
    }

    public void setStartGroupAge(Integer startGroupAge) {
        this.startGroupAge = startGroupAge;
    }

    public Integer getEndGroupAge() {
        return endGroupAge;
    }

    public void setEndGroupAge(Integer endGroupAge) {
        this.endGroupAge = endGroupAge;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public List<String> getMaritals() {
        return maritals;
    }

    public void setMaritals(List<String> maritals) {
        this.maritals = maritals;
    }

    public List<String> getIndustrys() {
        return industrys;
    }

    public void setIndustrys(List<String> industrys) {
        this.industrys = industrys;
    }

    public List<String> getSexs() {
        return sexs;
    }

    public void setSexs(List<String> sexs) {
        this.sexs = sexs;
    }

    public List<String> getRanks() {
        return ranks;
    }

    public void setRanks(List<String> ranks) {
        this.ranks = ranks;
    }

    public List<String> getSequences() {
        return sequences;
    }

    public void setSequences(List<String> sequences) {
        this.sequences = sequences;
    }

    public List<String> getPoliticals() {
        return politicals;
    }

    public void setPoliticals(List<String> politicals) {
        this.politicals = politicals;
    }

    public String getWorkPlace() {
        return workPlace;
    }

    public void setWorkPlace(String workPlace) {
        this.workPlace = workPlace;
    }

    public List<String> getSchoolTypes() {
        return schoolTypes;
    }

    public void setSchoolTypes(List<String> schoolTypes) {
        this.schoolTypes = schoolTypes;
    }

    public String getFamilyWork() {
        return familyWork;
    }

    public void setFamilyWork(String familyWork) {
        this.familyWork = familyWork;
    }

    public List<String> getEmployTypes() {
        return employTypes;
    }

    public void setEmployTypes(List<String> employTypes) {
        this.employTypes = employTypes;
    }

    public List<String> getSubSequences() {
        return subSequences;
    }

    public void setSubSequences(List<String> subSequences) {
        this.subSequences = subSequences;
    }

    public Integer getStartWorkAge() {
        return startWorkAge;
    }

    public void setStartWorkAge(Integer startWorkAge) {
        this.startWorkAge = startWorkAge;
    }

    public Integer getEndWorkAge() {
        return endWorkAge;
    }

    public void setEndWorkAge(Integer endWorkAge) {
        this.endWorkAge = endWorkAge;
    }

    public Integer getStartCorpAge() {
        return startCorpAge;
    }

    public void setStartCorpAge(Integer startCorpAge) {
        this.startCorpAge = startCorpAge;
    }

    public Integer getEndCorpAge() {
        return endCorpAge;
    }

    public void setEndCorpAge(Integer endCorpAge) {
        this.endCorpAge = endCorpAge;
    }
}
