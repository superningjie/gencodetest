package cds0.opmc.cea.business.bo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DimassesserBO {

    // 测评对象
    private Long assessObjId;

    /**
     * Map1：k--> evalDimId
     *       v--> assesserDimweigh
     * Map2：k--> assesserId
     *       v--> dimweight
     */
    private Map<Long,List<Map<Long,BigDecimal>>> evalDimAssesserDimweight;

    /**
     * 添加测评人需要字段 ----------------------------------------------
     */

    //绩效档案id
    private Long perfFileId;

    //测评活动id
    private Long assessActId;
    //维度分组id
    private Long dimGroupId;








    public Long getAssessObjId() {
        return assessObjId;
    }

    public void setAssessObjId(Long assessObjId) {
        this.assessObjId = assessObjId;
    }

    public Long getPerfFileId() {
        return perfFileId;
    }

    public void setPerfFileId(Long perfFileId) {
        this.perfFileId = perfFileId;
    }

    public Long getAssessActId() {
        return assessActId;
    }

    public void setAssessActId(Long assessActId) {
        this.assessActId = assessActId;
    }

    public Long getDimGroupId() {
        return dimGroupId;
    }

    public void setDimGroupId(Long dimGroupId) {
        this.dimGroupId = dimGroupId;
    }

    public Map<Long, List<Map<Long, BigDecimal>>> getEvalDimAssesserDimweight() {
        return evalDimAssesserDimweight;
    }

    public void setEvalDimAssesserDimweight(Map<Long, List<Map<Long, BigDecimal>>> evalDimAssesserDimweight) {
        this.evalDimAssesserDimweight = evalDimAssesserDimweight;
    }
}
