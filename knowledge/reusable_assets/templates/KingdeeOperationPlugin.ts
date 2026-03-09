/**
 * {{className}} - 操作服务插件
 * {{description}}
 * 
 * 注册位置：业务服务 > 操作服务 > 插件
 * 基类：AbstractOperationServicePlugIn
 */

import { AbstractOperationServicePlugIn, AddValidatorsEventArgs } from "@cosmic/bos-core/kd/bos/entity/plugin";
import { 
    PreparePropertysEventArgs,
    BeforeOperationArgs, 
    AfterOperationArgs 
} from "@cosmic/bos-core/kd/bos/entity/plugin/args";
import { ValidationErrorInfo, ErrorLevel } from "@cosmic/bos-core/kd/bos/entity/validation";
import { RowDataModel } from "@cosmic/bos-core/kd/bos/entity/datamodel";

// 交互相关
import { 
    InteractionContext, 
    OperateErrorInfo,
    KDInteractionException,
    InteractionConfirmResult 
} from "@cosmic/bos-core/kd/bos/entity/operate";
import { MessageBoxResult } from "@cosmic/bos-core/kd/bos/form/message";
import { OperateOption, OperateOptionConst } from "@cosmic/bos-core/kd/bos/entity/operate/option";

class {{className}} extends AbstractOperationServicePlugIn {
    
    {{#each constants}}
    // {{description}}
    private {{name}} = "{{value}}";
    {{/each}}
    
    /**
     * 准备操作需要的字段
     * 触发时机：操作执行前，加载数据时
     * 作用：添加操作需要的额外字段到查询中
     */
    onPreparePropertys(e: PreparePropertysEventArgs): void {
        {{#each headFields}}
        // 添加单头字段：{{description}}
        e.getFieldKeys().add("{{fieldKey}}");
        {{/each}}
        
        {{#each entryFields}}
        // 添加分录字段：{{description}}
        e.getFieldKeys().add("{{fieldKey}}");
        {{/each}}
    }
    
    {{#if useBeforeExecute}}
    /**
     * 操作事务执行前
     * 触发时机：操作执行前，在事务内
     * 作用：执行校验、设置取消标志
     */
    beforeExecuteOperationTransaction(e: BeforeOperationArgs): void {
        {{#if needValidation}}
        // 执行自定义校验逻辑
        {{validationLogic}}
        {{/if}}
        
        {{#if needInteraction}}
        // 显示交互提示，需要用户确认
        if (!this.showInteractionMessage(e)) {
            e.setCancel(true);
            return;
        }
        {{/if}}
        
        {{#each beforeLogic}}
        {{this}}
        {{/each}}
    }
    {{/if}}
    
    {{#if useAfterExecute}}
    /**
     * 操作事务执行后
     * 触发时机：操作执行后，在事务外（异常不会回滚）
     * 作用：执行后续逻辑、发送通知
     */
    afterExecuteOperationTransaction(e: AfterOperationArgs): void {
        {{#each afterLogic}}
        {{this}}
        {{/each}}
        
        {{#if sendNotification}}
        // 发送操作完成通知
        this.sendOperationNotification(e);
        {{/if}}
    }
    {{/if}}
    
    {{#if useOnAddValidators}}
    /**
     * 添加自定义校验器
     * 触发时机：操作校验阶段
     * 作用：添加业务校验规则
     */
    onAddValidators(e: AddValidatorsEventArgs): void {
        {{#each validators}}
        // {{description}}
        let {{validatorVar}} = this.create{{validatorName}}Validator();
        e.addValidator({{validatorVar}});
        {{/each}}
    }
    
    {{#each validatorMethods}}
    /**
     * 创建{{description}}校验器
     */
    create{{validatorName}}Validator(): AbstractValidator {
        let plugin = this;
        
        let validator = this.createObject(AbstractValidator, [], {
            // 准备需要校验的字段
            preparePropertys(): KSet<any> {
                let fields = superObject(validator).preparePropertys();
                {{#each validateFields}}
                fields.add("{{fieldKey}}");
                {{/each}}
                return fields;
            },
            
            // 执行校验
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
                            ErrorLevel.{{errorLevel}}     // 错误级别：Error/Warning
                        );
                        validator.getValidateResult().addErrorInfo(info);
                    }
                    {{/each}}
                }
            }
        });
        
        return validator;
    }
    {{/each}}
    {{/if}}
    
    {{#if needInteraction}}
    /**
     * 显示交互确认信息
     * @returns true继续操作，false中断操作
     */
    showInteractionMessage(e: BeforeOperationArgs): boolean {
        // 校验回调是否在弹窗之后，避免死循环
        let confirmResultString = this.getOption().getVariableValue(
            OperateOptionConst.INTERACTIONCONFIRMRESULT, 
            ""
        );
        let confirmResult = InteractionConfirmResult.fromJsonString(confirmResultString);
        
        let sponsorKey = "{{interactionSponsorKey}}";
        
        // 校验确认结果是否包含在中断信息内
        if (confirmResult.getResults().containsKey(sponsorKey)) {
            let result = confirmResult.getResults().get(sponsorKey);
            if (result == MessageBoxResult.Yes.toString()) {
                return true;  // 用户确认继续
            } else {
                return false; // 用户取消
            }
        }
        
        // 创建操作中断异常上下文
        let interactionContext = new InteractionContext();
        interactionContext.setSimpleMessage("{{interactionSimpleMessage}}");
        
        {{#each interactionErrorInfos}}
        let errorInfo{{index}} = new OperateErrorInfo();
        errorInfo{{index}}.setMessage("{{message}}");
        errorInfo{{index}}.setLevel(ErrorLevel.{{level}});
        interactionContext.addOperateInfo(errorInfo{{index}});
        {{/each}}
        
        // 抛出中断异常
        throw new KDInteractionException(sponsorKey, interactionContext);
    }
    {{/if}}
    
    {{#each customMethods}}
    /**
     * {{description}}
     */
    {{methodName}}({{params}}): {{returnType}} {
        {{methodBody}}
    }
    {{/each}}
}

let plugin = new {{className}}();
export { plugin };
