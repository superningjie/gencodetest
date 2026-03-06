package tdkw.hrmp.hrobs.common.myteam.common.excel;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/8/2-17:27
 * @description TODO
 */
@ExcelHeadProperty(text = "我的团队", lastCol = 2)
@ExcelRowProperty
public class MyTeamExcelFormatBean {

    @ExcelProperty(index = 0, title = "人员编码")
    private String no;

    @ExcelProperty(index = 1, title = "姓名")
    private String name;

    @ExcelProperty(index = 2, title = "业务板块")
    private String businesssector;

    @ExcelProperty(index = 3, title = "组织名称")
    private String org;

    @ExcelProperty(index = 4, title = "事业部/中心")
    private String careerdep;

    @ExcelProperty(index = 5, title = "部门")
    private String dep;

    @ExcelProperty(index = 6, title = "序列")
    private String sequence;

    @ExcelProperty(index = 7, title = "岗位层级")
    private String postlevel;

    @ExcelProperty(index = 8, title = "岗位")
    private String position;

    @ExcelProperty(index = 9, title = "人员类别")
    private String persontype;

    @ExcelProperty(index = 10, title = "性别")
    private String gender;

    @ExcelProperty(index = 11, title = "证件号码")
    private String idno;

    @ExcelProperty(index = 12, title = "年龄")
    private String age;

    @ExcelProperty(index = 13, title = "民族")
    private String folk;

    @ExcelProperty(index = 14, title = "籍贯")
    private String origin;

    @ExcelProperty(index = 15, title = "婚姻")
    private String marriage;

    @ExcelProperty(index = 16, title = "参加工作日期")
    private String workdate;


    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusinesssector() {
        return businesssector;
    }

    public void setBusinesssector(String businesssector) {
        this.businesssector = businesssector;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getCareerdep() {
        return careerdep;
    }

    public void setCareerdep(String careerdep) {
        this.careerdep = careerdep;
    }

    public String getDep() {
        return dep;
    }

    public void setDep(String dep) {
        this.dep = dep;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getPostlevel() {
        return postlevel;
    }

    public void setPostlevel(String postlevel) {
        this.postlevel = postlevel;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPersontype() {
        return persontype;
    }

    public void setPersontype(String persontype) {
        this.persontype = persontype;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdno() {
        return idno;
    }

    public void setIdno(String idno) {
        this.idno = idno;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getFolk() {
        return folk;
    }

    public void setFolk(String folk) {
        this.folk = folk;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getMarriage() {
        return marriage;
    }

    public void setMarriage(String marriage) {
        this.marriage = marriage;
    }

    public String getWorkdate() {
        return workdate;
    }

    public void setWorkdate(String workdate) {
        this.workdate = workdate;
    }
}
