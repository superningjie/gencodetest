package dgdl.odc.homs.common;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperateErrorInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.validate.ErrorLevel;
import kd.bos.entity.validate.ValidateResult;
import kd.bos.entity.validate.ValidationErrorInfo;
import kd.bos.exception.KDBizException;

import java.util.List;

/**
 * @ClassName ExceptionUtil
 * @description:
 * @author: hl
 * @create: 2023-06-19 10:43
 **/
public class ExceptionUtil {

    /**
     * 获取操作结果详细报错信息
     * @param optRst 操作结果
     * @return
     */
    public static String getOperationResultDetailMsg(OperationResult optRst) {
        StringBuffer validateMsg = new StringBuffer();
        if(optRst.getValidateResult() != null) {
            List<ValidateResult> validateErrors = optRst.getValidateResult().getValidateErrors();
            if(validateErrors != null) {
                for (int j = 0; j < validateErrors.size(); j++) {
                    List<OperateErrorInfo> allErrorInfo = validateErrors.get(j).getAllErrorInfo();
                    for (int k = 0; k < allErrorInfo.size(); k++) {
                        validateMsg.append(allErrorInfo.get(k).getMessage()+",");
                    }
                }
            }
        }
        return validateMsg.toString();
    }

    /**
     * 封装验证失败信息
     * @param obj 单据对象
     * @param exp 异常
     * @return
     */
    public static ValidationErrorInfo buildErrMessage(DynamicObject obj, KDBizException exp){
        Object pkId = obj.getPkValue();
        int dataIndex = 0;
        int rowIndex = 0;
        ErrorLevel errorLevel = ErrorLevel.Error;
        String msg = exp.getMessage();
        ValidationErrorInfo info = new ValidationErrorInfo("", pkId, dataIndex, rowIndex, "001","", msg, errorLevel);
        return info;
    }
}
