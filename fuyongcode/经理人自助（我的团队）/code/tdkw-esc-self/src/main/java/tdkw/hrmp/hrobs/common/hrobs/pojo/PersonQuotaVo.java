package tdkw.hrmp.hrobs.common.hrobs.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

/**
 * 人员定额
 */
public class PersonQuotaVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 考勤档案
     */
    private Long id;

    /**
     * 考勤档案编号
     */
    private String number;

    /**
     * 年份
     */
    private Date date;

    /**
     * 定额明细
     */
    LinkedList<QuotaVo> quotaVoList;

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

    public LinkedList<QuotaVo> getQuotaVoList() {
        return quotaVoList;
    }

    public void setQuotaVoList(LinkedList<QuotaVo> quotaVoList) {
        this.quotaVoList = quotaVoList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
