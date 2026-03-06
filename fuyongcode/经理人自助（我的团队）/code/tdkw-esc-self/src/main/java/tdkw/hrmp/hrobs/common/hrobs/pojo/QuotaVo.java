package tdkw.hrmp.hrobs.common.hrobs.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 定额明细对象
 */
public class QuotaVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 上期结余
     */
    private BigDecimal previousPeriodBalance;
    /**
     * 调整时长
     */
    private BigDecimal adjustValue;
    /**
     * 本期享有
     */
    private BigDecimal currentPeriodEnjoy;
    /**
     * 已用时长
     */
    private BigDecimal alreadyVaApplyDays;
    /**
     * 冻结时长
     */
    private BigDecimal freezeDays;
    /**
     * 可用时长
     */
    private BigDecimal availableDays;
    /**
     * 定额类型
     */
    private String qtTypeName;
    /**
     * 定额类型ID
     */
    private Long qtType;
    /**
     * 结转时长
     */
    private BigDecimal carryoverDays;
    /**
     * 失效时长
     */
    private BigDecimal invalidDays;


    /**
     * 已用单据明细列表
     */
    private List<QuotaBillDetailVo> usedDetail;

    /**
     * 冻结单据明细列表
     */
    private List<QuotaBillDetailVo> freezeDetail;


    public BigDecimal getPreviousPeriodBalance() {
        return previousPeriodBalance;
    }

    public void setPreviousPeriodBalance(BigDecimal previousPeriodBalance) {
        this.previousPeriodBalance = previousPeriodBalance;
    }

    public BigDecimal getAdjustValue() {
        return adjustValue;
    }

    public void setAdjustValue(BigDecimal adjustValue) {
        this.adjustValue = adjustValue;
    }

    public BigDecimal getCurrentPeriodEnjoy() {
        return currentPeriodEnjoy;
    }

    public void setCurrentPeriodEnjoy(BigDecimal currentPeriodEnjoy) {
        this.currentPeriodEnjoy = currentPeriodEnjoy;
    }

    public BigDecimal getAlreadyVaApplyDays() {
        return alreadyVaApplyDays;
    }

    public void setAlreadyVaApplyDays(BigDecimal alreadyVaApplyDays) {
        this.alreadyVaApplyDays = alreadyVaApplyDays;
    }

    public BigDecimal getFreezeDays() {
        return freezeDays;
    }

    public void setFreezeDays(BigDecimal freezeDays) {
        this.freezeDays = freezeDays;
    }

    public BigDecimal getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(BigDecimal availableDays) {
        this.availableDays = availableDays;
    }

    public String getQtTypeName() {
        return qtTypeName;
    }

    public void setQtTypeName(String qtTypeName) {
        this.qtTypeName = qtTypeName;
    }

    public Long getQtType() {
        return qtType;
    }

    public void setQtType(Long qtType) {
        this.qtType = qtType;
    }

    public BigDecimal getCarryoverDays() {
        return carryoverDays;
    }

    public void setCarryoverDays(BigDecimal carryoverDays) {
        this.carryoverDays = carryoverDays;
    }

    public BigDecimal getInvalidDays() {
        return invalidDays;
    }

    public void setInvalidDays(BigDecimal invalidDays) {
        this.invalidDays = invalidDays;
    }

    public List<QuotaBillDetailVo> getUsedDetail() {
        return usedDetail;
    }

    public void setUsedDetail(List<QuotaBillDetailVo> usedDetail) {
        this.usedDetail = usedDetail;
    }

    public List<QuotaBillDetailVo> getFreezeDetail() {
        return freezeDetail;
    }

    public void setFreezeDetail(List<QuotaBillDetailVo> freezeDetail) {
        this.freezeDetail = freezeDetail;
    }
}
