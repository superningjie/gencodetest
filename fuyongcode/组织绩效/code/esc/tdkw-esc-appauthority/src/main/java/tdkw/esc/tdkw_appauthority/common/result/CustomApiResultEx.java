package tdkw.esc.tdkw_appauthority.common.result;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.sdk.annotation.SdkPublic;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * API接口出参封装对象工具类
 */
@SdkPublic
@ApiModel
public class CustomApiResultEx implements Serializable {
    private static final long serialVersionUID = 6401202865123163396L;

    private @NotNull @ApiParam(
            value = "状态：true - 成功, false - 失败",
            required = true
    ) boolean status = true;

    private @ApiParam("响应状态") Integer statusCode;
    private @ApiParam("错误信息") String message;
    private @ApiParam("响应数据") Object data;

    public CustomApiResultEx() {

    }

    /**
     * API接口需要勾选：出参仅返回Data域
     * @param data 出参
     * @return 出参
     */
    public static <T> CustomApiResult<CustomApiResultEx> success(T data) {
        CustomApiResult<CustomApiResultEx> responseData = new CustomApiResult<>();
        CustomApiResultEx result = new CustomApiResultEx();
        result.setStatusCode(200);
        result.setMessage("success");
        result.setData(data);
        responseData.setData(result);
        return responseData;
    }

    /**
     * 返回错误信息
     * @param errorMessage 错误信息
     * @return 出参
     */
    public static CustomApiResult<CustomApiResultEx> fail(String errorMessage) {
        CustomApiResult<CustomApiResultEx> responseData = new CustomApiResult<>();
        CustomApiResultEx result = new CustomApiResultEx();
        result.setStatusCode(400);
        result.setData(null);
        result.setMessage(errorMessage);
        responseData.setData(result);
        responseData.setStatus(false);
        responseData.setErrorCode("400");
        responseData.setMessage(errorMessage);
        return responseData;
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

}
