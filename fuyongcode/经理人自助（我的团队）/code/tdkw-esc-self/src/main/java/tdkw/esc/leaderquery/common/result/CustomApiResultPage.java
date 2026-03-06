package tdkw.esc.leaderquery.common.result;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.sdk.annotation.SdkPublic;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@SdkPublic
@ApiModel
public class CustomApiResultPage implements Serializable {

    private static final long serialVersionUID = -5034787816147206007L;
    private @NotNull @ApiParam(
            value = "状态：true - 成功, false - 失败",
            required = true
    ) boolean status = true;

    private @ApiParam("响应状态") Integer statusCode;
    private @ApiParam("错误信息") String message;
    private @ApiParam("响应数据Data") Object data;

    private @ApiParam("响应数据List") Object list;

    @ApiParam(value="分页页码：从1开始")
    private Integer pageNum;

    @ApiParam(value="每一页的数目")
    private Integer pageSize;

    @ApiParam(value="总条数")
    private Integer total;

    @ApiParam(value="总页数")
    private Integer pages;

    public CustomApiResultPage() {

    }

    /**
     * API接口需要勾选：出参仅返回Data域
     * @param data 出参
     * @return 出参
     */
    public static <T> CustomApiResult<CustomApiResultPage> success(T data) {
        CustomApiResult<CustomApiResultPage> responseData = new CustomApiResult<>();
        CustomApiResultPage result = new CustomApiResultPage();
        result.setStatusCode(200);
        result.setMessage("success");
        if (data instanceof CustomApiResultPage) {
            responseData.setData((CustomApiResultPage)data);
            return responseData;
        } else if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            if (map.get("pageNum") == null) {
                result.setData(data);
                responseData.setData(result);
                return responseData;
            }
            result.getPage(map);
            if (map.get("data") != null) {
                result.setData(map.get("data"));
            }
            if (map.get("list") != null) {
                result.setList(map.get("list"));
            }
            responseData.setData(result);
            return responseData;
        }
        result.setData(data);
        responseData.setData(result);
        return responseData;
    }

    public static CustomApiResult<CustomApiResultPage> fail(String errorMessage) {
        CustomApiResult<CustomApiResultPage> responseData = new CustomApiResult<>();
        CustomApiResultPage result = new CustomApiResultPage();
        result.setStatusCode(400);
        result.setData(null);
        result.setMessage(errorMessage);
        responseData.setData(result);
        responseData.setStatus(false);
        responseData.setErrorCode("400");
        responseData.setMessage(errorMessage);
        return responseData;
    }

    private void getPage(Map<?, ?> map) {
        if (map.get("pageNum") instanceof Integer) {
            this.setPageNum((Integer) map.get("pageNum"));
        }
        if (map.get("pageSize") instanceof Integer) {
            this.setPageSize((Integer) map.get("pageSize"));
        }
        if (map.get("total") instanceof Integer) {
            this.setTotal((Integer) map.get("total"));
        }
        if (map.get("pages") instanceof Integer) {
            this.setPages((Integer) map.get("pages"));
        }
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Object getList() {
        return list;
    }

    public void setList(Object list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "CustomApiResultPage{" +
                "status=" + status +
                ", statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", list=" + list +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", total=" + total +
                ", pages=" + pages +
                '}';
    }
}
