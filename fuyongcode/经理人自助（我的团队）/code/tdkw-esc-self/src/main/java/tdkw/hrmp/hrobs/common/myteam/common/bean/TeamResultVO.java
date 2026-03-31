package tdkw.hrmp.hrobs.common.myteam.common.bean;

import java.io.Serializable;
import java.util.List;

public class TeamResultVO implements Serializable {
    private static final long serialVersionUID = -1l;

    private String image;
    private String postname;
    private String pkPsndoc;
    private String isMainJob;
    private String psnclname;
    private String jobTypeName;
    private String psnType;
    private String index;
    private Double corpworkage;
    private Double orgglbdef2;
    private List<String> partjobs;
    private String trnstypename;
    private String name;
    private String personid;
    private String pk;
    private int age;
    private String eduname;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPostname() {
        return postname;
    }

    public void setPostname(String postname) {
        this.postname = postname;
    }

    public String getPkPsndoc() {
        return pkPsndoc;
    }

    public void setPkPsndoc(String pkPsndoc) {
        this.pkPsndoc = pkPsndoc;
    }

    public String getIsMainJob() {
        return isMainJob;
    }

    public void setIsMainJob(String isMainJob) {
        this.isMainJob = isMainJob;
    }

    public String getPsnclname() {
        return psnclname;
    }

    public void setPsnclname(String psnclname) {
        this.psnclname = psnclname;
    }

    public String getJobTypeName() {
        return jobTypeName;
    }

    public void setJobTypeName(String jobTypeName) {
        this.jobTypeName = jobTypeName;
    }

    public String getPsnType() {
        return psnType;
    }

    public void setPsnType(String psnType) {
        this.psnType = psnType;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Double getCorpworkage() {
        return corpworkage;
    }

    public void setCorpworkage(Double corpworkage) {
        this.corpworkage = corpworkage;
    }

    public Double getOrgglbdef2() {
        return orgglbdef2;
    }

    public void setOrgglbdef2(Double orgglbdef2) {
        this.orgglbdef2 = orgglbdef2;
    }

    public List<String> getPartjobs() {
        return partjobs;
    }

    public void setPartjobs(List<String> partjobs) {
        this.partjobs = partjobs;
    }

    public String getTrnstypename() {
        return trnstypename;
    }

    public void setTrnstypename(String trnstypename) {
        this.trnstypename = trnstypename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEduname() {
        return eduname;
    }

    public void setEduname(String eduname) {
        this.eduname = eduname;
    }
}
