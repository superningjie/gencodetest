/**
 * 操作校验代码片段
 * 使用场景：操作插件中创建自定义校验器
 */

// 在onAddValidators中添加校验器
create{{validatorName}}Validator(): AbstractValidator {
    let plugin = this;
    
    let validator = this.createObject(AbstractValidator, [], {
        // 准备需要校验的字段
        preparePropertys(): KSet<any> {
            let fields = superObject(validator).preparePropertys();
            {{#each validateFields}}
            fields.add("{{fieldKey}}");  // {{description}}
            {{/each}}
            return fields;
        },
        
        // 执行校验逻辑
        validate(): void {
            let rowDataModel = new RowDataModel(
                validator.getEntityKey(), 
                validator.getValidateContext().getSubEntityType()
            );
            
            for (let rowDataEntity of validator.getDataEntities()) {
                rowDataModel.setRowContext(rowDataEntity.getDataEntity());
                
                {{#each validateRules}}
                // {{description}}
                let {{fieldVar}} = rowDataModel.getValue("{{fieldKey}}") as {{fieldType}};
                if ({{condition}}) {
                    let info = new ValidationErrorInfo(
                        "",                           // entityKey
                        rowDataEntity.getBillPkId(),  // 单据主键
                        rowDataEntity.getDataEntityIndex(), // 数据实体索引
                        rowDataEntity.getRowIndex(),  // 行索引
                        "{{errorCode}}",              // 错误代码
                        validator.getValidateContext().getOperateName(), // 操作名称
                        "{{errorMessage}}",           // 错误消息
                        ErrorLevel.{{errorLevel}}     // 错误级别
                    );
                    validator.getValidateResult().addErrorInfo(info);
                }
                {{/each}}
            }
        }
    });
    
    return validator;
}

// ========== 常用校验示例 ==========

// 示例1：必填字段校验
// let fieldValue = rowDataModel.getValue("requiredfield") as string;
// if (fieldValue == null || fieldValue.trim() == "") {
//     let info = new ValidationErrorInfo(
//         "",
//         rowDataEntity.getBillPkId(),
//         rowDataEntity.getDataEntityIndex(),
//         rowDataEntity.getRowIndex(),
//         "ERR_REQUIRED_FIELD",
//         validator.getValidateContext().getOperateName(),
//         "必填字段不能为空",
//         ErrorLevel.Error
//     );
//     validator.getValidateResult().addErrorInfo(info);
// }

// 示例2：数值范围校验
// let amount = rowDataModel.getValue("amount") as BigDecimal;
// if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
//     let info = new ValidationErrorInfo(
//         "",
//         rowDataEntity.getBillPkId(),
//         rowDataEntity.getDataEntityIndex(),
//         rowDataEntity.getRowIndex(),
//         "ERR_AMOUNT_INVALID",
//         validator.getValidateContext().getOperateName(),
//         "金额必须大于0",
//         ErrorLevel.Error
//     );
//     validator.getValidateResult().addErrorInfo(info);
// }

// 示例3：日期逻辑校验
// let startDate = rowDataModel.getValue("startdate") as Date;
// let endDate = rowDataModel.getValue("enddate") as Date;
// if (startDate != null && endDate != null && startDate.getTime() > endDate.getTime()) {
//     let info = new ValidationErrorInfo(
//         "",
//         rowDataEntity.getBillPkId(),
//         rowDataEntity.getDataEntityIndex(),
//         rowDataEntity.getRowIndex(),
//         "ERR_DATE_LOGIC",
//         validator.getValidateContext().getOperateName(),
//         "开始日期不能晚于结束日期",
//         ErrorLevel.Error
//     );
//     validator.getValidateResult().addErrorInfo(info);
// }
