package cds0.opmc.cea.business.bo;

public class AssessTaskBO {
    //测评对象id
    private Long assessObjId;
    //测评对象人员
    private Long personId;
    //绩效档案id
    private Long perfFileId;
    //测评维度id
    private Long evalDimId;
    //测评活动id
    private Long assessActId;
    //维度分组id
    private Long dimGroupId;
    //维度测评人id
    private Long dimAssessorId;
    //测评人人员id
    private Long assesserPersonId;
    public AssessTaskBO(){

    }
    public AssessTaskBO(Long assessObjId, Long personId, Long perfFileId,Long evalDimId,Long assessActId,Long dimGroupId,Long dimAssessorId, Long assesserPersonId){
        this.assessObjId = assessObjId;
        this.personId = personId;
        this.perfFileId = perfFileId;
        this.evalDimId = evalDimId;
        this.assessActId = assessActId;
        this.dimGroupId = dimGroupId;
        this.dimAssessorId = dimAssessorId;
        this.assesserPersonId = assesserPersonId;
    }

    public Long getAssessObjId() {
        return assessObjId;
    }

    public void setAssessObjId(Long assessObjId) {
        this.assessObjId = assessObjId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getPerfFileId() {
        return perfFileId;
    }

    public void setPerfFileId(Long perfFileId) {
        this.perfFileId = perfFileId;
    }

    public Long getEvalDimId() {
        return evalDimId;
    }

    public void setEvalDimId(Long evalDimId) {
        this.evalDimId = evalDimId;
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

    public Long getDimAssessorId() {
        return dimAssessorId;
    }

    public void setDimAssessorId(Long dimAssessorId) {
        this.dimAssessorId = dimAssessorId;
    }

    public Long getAssesserPersonId() {
        return assesserPersonId;
    }

    public void setAssesserPersonId(Long assesserPersonId) {
        this.assesserPersonId = assesserPersonId;
    }
}
