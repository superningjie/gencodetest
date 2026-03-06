package tdkw.hrmp.hrobs.formplugin.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xxx
 * @Date 2023/5/26 17:52
 * @Description PC端 当月出勤卡片 人员考勤期间 实体类
 * @Demander xxx
 * @Document 员工自助门户与个人卡片-XXX集团需求规格说明书_V2.0(4)
 * @Basedata tdkw_hrobs_pc_attendance
 * @Version 1.0
 **/
public class PerAttPeriod implements Serializable {

    private static final long serialVersionUID = -2919216265011713338L;

    /**
     * 人员考勤期间id，人员id+"_"+期间id+人员考勤期间开始时间yyyy-MM-dd
     */
    private String id;
    /**
     * 考勤档案主键
     */
    private Long fileId;
    /**
     * 考勤档案BoId
     */
    private Long fileBoId;

    /**
     * 考勤人员
     */
    private Long personId;
    /**
     * 人员考勤期间开始时间
     */
    private Date perAttPeriodStartDate;

    /**
     * 人员考勤期间结束时间
     */
    private Date perAttPeriodEndDate;
    /**
     * 工时归属规则id
     */
    private Long mhsa;
    /**
     * 期间id
     */
    private Long attPeriodId;
    /**
     * 期间开始时间
     */
    private Date attPeriodStartDate;
    /**
     * 期间结束时间
     */
    private Date attPeriodEndDate;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Date getPerAttPeriodStartDate() {
        return perAttPeriodStartDate;
    }

    public void setPerAttPeriodStartDate(Date perAttPeriodStartDate) {
        this.perAttPeriodStartDate = perAttPeriodStartDate;
    }

    public Date getPerAttPeriodEndDate() {
        return perAttPeriodEndDate;
    }

    public void setPerAttPeriodEndDate(Date perAttPeriodEndDate) {
        this.perAttPeriodEndDate = perAttPeriodEndDate;
    }

    public Long getMhsa() {
        return mhsa;
    }

    public void setMhsa(Long mhsa) {
        this.mhsa = mhsa;
    }

    public Long getAttPeriodId() {
        return attPeriodId;
    }

    public void setAttPeriodId(Long attPeriodId) {
        this.attPeriodId = attPeriodId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getFileBoId() {
        return fileBoId;
    }

    public void setFileBoId(Long fileBoId) {
        this.fileBoId = fileBoId;
    }

    public Date getAttPeriodStartDate() {
        return attPeriodStartDate;
    }

    public void setAttPeriodStartDate(Date attPeriodStartDate) {
        this.attPeriodStartDate = attPeriodStartDate;
    }

    public Date getAttPeriodEndDate() {
        return attPeriodEndDate;
    }

    public void setAttPeriodEndDate(Date attPeriodEndDate) {
        this.attPeriodEndDate = attPeriodEndDate;
    }

    @Override
    public String toString() {
        return "PerAttPeriod{" +
                "id='" + id + '\'' +
                ", fileId=" + fileId +
                ", fileBoId=" + fileBoId +
                ", personId=" + personId +
                ", perAttPeriodStartDate=" + perAttPeriodStartDate +
                ", perAttPeriodEndDate=" + perAttPeriodEndDate +
                ", mhsa=" + mhsa +
                ", attPeriodId=" + attPeriodId +
                ", attPeriodStartDate=" + attPeriodStartDate +
                ", attPeriodEndDate=" + attPeriodEndDate +
                '}';
    }
}