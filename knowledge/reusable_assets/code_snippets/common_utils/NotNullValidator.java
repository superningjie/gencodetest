package com.kingdee.hr.common.util;

import com.kingdee.hr.common.exception.BusinessException;

/**
 * 非空校验工具类
 * 可复用资产ID: snippet_validation_notnull
 */
public class NotNullValidator {

    /**
     * 校验对象非空
     * @param obj 待校验对象
     * @param paramName 参数名
     * @param errorCode 错误码
     */
    public static void validate(Object obj, String paramName, String errorCode) {
        if (obj == null) {
            throw new BusinessException(errorCode, paramName + "不能为空");
        }
    }

    /**
     * 校验字符串非空
     * @param str 待校验字符串
     * @param paramName 参数名
     * @param errorCode 错误码
     */
    public static void validateNotEmpty(String str, String paramName, String errorCode) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(errorCode, paramName + "不能为空");
        }
    }

    /**
     * 校验多个字段
     * @param params 参数名和值的映射
     * @param errorCode 错误码
     */
    public static void validateMultiple(java.util.Map<String, Object> params, String errorCode) {
        for (java.util.Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                throw new BusinessException(errorCode, entry.getKey() + "不能为空");
            }
        }
    }
}
