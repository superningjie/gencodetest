package tdkw.hrmp.hrobs.common.app.api;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;


@ApiModel
public class AdminOrgChangeParamRequest implements Serializable {

    private static final long serialVersionUID = 2199222756188912101L;

    @ApiParam(value = "组织类型编码")
    private String orgNum;

    @ApiParam("组织id")
    private String orgId;

    @ApiParam("关键词")
    private String name;

    @ApiParam("开始日期")
    private Date start;

    @ApiParam("结束日期")
    private Date end;

    @ApiParam("行政组织类型编码")
    private Object[] orgType;

    @ApiParam(value = "分页页码：从1开始", required = true, example = "1")
    @NotNull
    @Min(1)
    private Integer pageNum;

    @ApiParam(value = "每一页的数目", required = true, example = "10")
    @NotNull
    @Min(1)
    private Integer pageSize;

    public AdminOrgChangeParamRequest() {

    }

    public AdminOrgChangeParamRequest(String orgNum, String orgId, String name, Object[] orgType, Date start, Date end, Integer pageNum, Integer pageSize) {
        this.orgNum = orgNum;
        this.orgId = orgId;
        this.name = name;
        this.start = start;
        this.end = end;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.orgType = orgType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object[] getOrgType() {
        return orgType;
    }

    public void setOrgType(Object[] orgType) {
        this.orgType = orgType;
    }

    public String getOrgNum() {
        return orgNum;
    }

    public void setOrgNum(String orgNum) {
        this.orgNum = orgNum;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "AdminOrgChangeParamRequest{" +
                "orgNum='" + orgNum + '\'' +
                ", orgId='" + orgId + '\'' +
                ", name='" + name + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", orgType=" + Arrays.toString(orgType) +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}
