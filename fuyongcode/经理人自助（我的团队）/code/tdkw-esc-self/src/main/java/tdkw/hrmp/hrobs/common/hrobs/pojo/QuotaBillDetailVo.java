package tdkw.hrmp.hrobs.common.hrobs.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 人员定额
 */
public class QuotaBillDetailVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 考勤档案ID
     */
    private Long id;

    /**
     * 考勤档案编号
     */
    private String number;

    /**
     * 单据编码
     */
    private String billNo;

    /**
     * 定额类型ID
     */
    private Long quoteTypeId;

    /**
     * 单据开始日期
     */
    private Date beginDate;

    /**
     * 单据结束日期
     */
    private Date endDate;


    /**
     * 考勤项目ID
     */
    private Long projectId;


    /**
     * 值
     */
    private BigDecimal value;


    /**
     * 单位
     */
    private String unit;

    /**
     * 计算日期
     */
    private Date calDate;

    /**
     * 计算人
     */
    private String calPerson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Long getQuoteTypeId() {
        return quoteTypeId;
    }

    public void setQuoteTypeId(Long quoteTypeId) {
        this.quoteTypeId = quoteTypeId;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Date getCalDate() {
        return calDate;
    }

    public void setCalDate(Date calDate) {
        this.calDate = calDate;
    }

    public String getCalPerson() {
        return calPerson;
    }

    public void setCalPerson(String calPerson) {
        this.calPerson = calPerson;
    }
}
