/**
 * {{className}} - 单据表单插件
 * {{description}}
 * 
 * 注册位置：表单设计器 > 插件 > 注册
 * 基类：AbstractBillPlugIn
 */

import { AbstractBillPlugIn } from "@cosmic/bos-core/kd/bos/bill";
import { EventObject } from "@cosmic/bos-script/java/util";
import { PropertyChangedArgs } from "@cosmic/bos-core/kd/bos/entity/datamodel/events";
import { BeforeDoOperationEventArgs, AfterDoOperationEventArgs } from "@cosmic/bos-core/kd/bos/form/events";
import { AfterAddRowEventArgs } from "@cosmic/bos-core/kd/bos/entity/datamodel/events";

// 控件导入
import { TextEdit } from "@cosmic/bos-core/kd/bos/form/control";
import { BasedataEdit } from "@cosmic/bos-core/kd/bos/form/control";
import { EntryGrid } from "@cosmic/bos-core/kd/bos/form/control";
import { Button } from "@cosmic/bos-core/kd/bos/form/control";
import { Toolbar } from "@cosmic/bos-core/kd/bos/form/control";

class {{className}} extends AbstractBillPlugIn {
    
    /**
     * 注册事件监听
     * 在此方法中注册按钮点击、字段值改变等监听
     */
    registerListener(e: EventObject): void {
        super.registerListener(e);
        
        {{#if hasButtonListener}}
        // 注册按钮点击监听
        {{#each buttons}}
        let {{controlVar}} = this.getView().getControl("{{controlKey}}") as Button;
        {{controlVar}}.addClickListener({
            click: event => {
                this.handle{{eventName}}Click();
            }
        });
        {{/each}}
        {{/if}}
        
        {{#if hasF7Listener}}
        // 注册F7选择监听
        {{#each f7Fields}}
        let {{controlVar}} = this.getView().getControl("{{controlKey}}") as BasedataEdit;
        {{controlVar}}.addBeforeF7SelectListener({
            beforeF7Select(e: BeforeF7SelectEvent) {
                {{f7FilterLogic}}
            }
        });
        {{/each}}
        {{/if}}
        
        {{#if hasEntryGridListener}}
        // 注册分录表格监听
        {{#each entryGrids}}
        let {{controlVar}} = this.getView().getControl("{{controlKey}}") as EntryGrid;
        {{controlVar}}.addDataBindListener({
            entryGridBindData(event) {
                {{entryGridLogic}}
            }
        });
        {{/each}}
        {{/if}}
    }
    
    {{#if useAfterCreateNewData}}
    /**
     * 新建单据后初始化数据
     * 触发时机：用户点击【新增】按钮创建新单据时
     */
    afterCreateNewData(e: EventObject): void {
        super.afterCreateNewData(e);
        
        {{#each initFields}}
        // 初始化{{fieldName}}字段
        this.getModel().setValue("{{fieldKey}}", {{fieldValue}});
        {{/each}}
        
        {{#if createEntryRows}}
        // 批量创建{{entryRowCount}}行分录
        this.getModel().batchCreateNewEntryRow("{{entryEntityKey}}", {{entryRowCount}});
        for (let i = 0; i < {{entryRowCount}}; i++) {
            this.getModel().setValue("{{entryFieldKey}}", {{entryFieldValue}}, i);
        }
        {{/if}}
        
        {{customInitLogic}}
    }
    {{/if}}
    
    {{#if useAfterLoadData}}
    /**
     * 单据数据加载完成后
     * 触发时机：单据数据从数据库加载到界面后
     */
    afterLoadData(e: EventObject): void {
        super.afterLoadData(e);
        
        {{#each loadDataLogic}}
        {{this}}
        {{/each}}
    }
    {{/if}}
    
    {{#if useAfterBindData}}
    /**
     * 数据绑定到界面后
     * 触发时机：数据模型与界面控件绑定完成后
     */
    afterBindData(e: EventObject): void {
        super.afterBindData(e);
        
        {{#if lockByStatus}}
        // 根据单据状态锁定界面
        let billStatus = this.getModel().getValue("{{statusField}}") as string;
        if ("{{lockStatus}}" == billStatus) {
            (this.getView() as IBillView).setBillStatus(BillOperationStatus.VIEW);
        }
        {{/if}}
        
        {{#each bindDataLogic}}
        {{this}}
        {{/each}}
    }
    {{/if}}
    
    {{#if usePropertyChanged}}
    /**
     * 字段值发生变化时
     * 触发时机：用户修改字段值或代码调用setValue时
     */
    propertyChanged(e: PropertyChangedArgs): void {
        super.propertyChanged(e);
        
        {{#if hasCalculation}}
        // 字段联动计算
        for (let change of e.getChangeSet()) {
            let changedField = change.getFieldName();
            
            {{#each calculations}}
            if ("{{triggerField}}" == changedField) {
                {{calculationLogic}}
            }
            {{/each}}
        }
        {{/if}}
        
        {{customPropertyLogic}}
    }
    {{/if}}
    
    {{#if useBeforeDoOperation}}
    /**
     * 执行操作前触发
     * 触发时机：保存、提交、审核等操作执行前
     * 可用于：校验数据、提示确认、阻止操作
     */
    beforeDoOperation(args: BeforeDoOperationEventArgs): void {
        let operate = args.getSource() as FormOperate;
        let operateKey = operate.getOperateKey();
        
        {{#each operationHandlers}}
        if ("{{operateKey}}" == operateKey) {
            {{#if needConfirm}}
            // 显示确认提示
            let afterConfirm = operate.getOption().getVariableValue("afterconfirm", "");
            if (afterConfirm == "") {
                let confirmCallBacks = new ConfirmCallBackListener("{{operateKey}}", this.getPluginName());
                let confirmTip = "{{confirmMessage}}";
                this.getView().showConfirm(confirmTip, MessageBoxOptions.YesNo, ConfirmTypes.Default, confirmCallBacks);
                args.setCancel(true);
                return;
            }
            {{/if}}
            
            {{operationLogic}}
        }
        {{/each}}
    }
    {{/if}}
    
    {{#if useAfterDoOperation}}
    /**
     * 执行操作后触发
     * 触发时机：保存、提交、审核等操作执行后
     * 可用于：处理操作结果、打开其他界面
     */
    afterDoOperation(args: AfterDoOperationEventArgs): void {
        super.afterDoOperation(args);
        
        if (args.getOperationResult() == null || !args.getOperationResult().isSuccess()) {
            return;
        }
        
        let operateKey = args.getOperateKey();
        
        {{#each afterOperationHandlers}}
        if ("{{operateKey}}" == operateKey) {
            {{#if showMessage}}
            args.getOperationResult().setShowMessage(true);
            {{/if}}
            
            {{#if openForm}}
            // 打开指定界面
            let fsp = new FormShowParameter();
            fsp.setFormId("{{targetFormId}}");
            fsp.getOpenStyle().setShowType(ShowType.Modal);
            this.getView().showForm(fsp);
            {{/if}}
            
            {{operationLogic}}
        }
        {{/each}}
    }
    {{/if}}
    
    {{#if useAfterAddRow}}
    /**
     * 分录行添加后
     * 触发时机：用户在分录中新增行后
     */
    afterAddRow(event: AfterAddRowEventArgs): void {
        if (event.getEntryProp().getName() == "{{entryEntityKey}}") {
            {{#each rowInitLogic}}
            {{this}}
            {{/each}}
            
            {{#if copyHeadField}}
            // 从单据头复制字段值到分录
            let headValue = this.getModel().getValue("{{headFieldKey}}") as {{headFieldType}};
            for (let row of event.getRowDataEntities()) {
                this.getModel().setValue("{{entryFieldKey}}", headValue, row.getRowIndex());
            }
            {{/if}}
        }
    }
    {{/if}}
    
    {{#if usePreOpenForm}}
    /**
     * 界面打开前处理
     * 触发时机：单据界面即将打开时
     * 可用于：修改界面标题、设置参数、阻止打开
     */
    preOpenForm(e: PreOpenFormEventArgs): void {
        {{#if setCaption}}
        // 修改界面标题
        let fsp = e.getSource() as FormShowParameter;
        fsp.setCaption("{{formCaption}}");
        {{/if}}
        
        {{#if cancelOpen}}
        // 阻止界面打开
        e.setCancel(true);
        e.setCancelMessage("{{cancelMessage}}");
        {{/if}}
        
        {{customPreOpenLogic}}
    }
    {{/if}}
    
    {{#if useBeforeClosed}}
    /**
     * 界面关闭前处理
     * 触发时机：用户关闭界面前
     * 可用于：返回数据给父页面
     */
    beforeClosed(e: BeforeClosedEvent): void {
        {{#if returnData}}
        // 返回数据给父页面
        let returnValue = this.getModel().getValue("{{returnFieldKey}}");
        this.getView().returnDataToParent(returnValue);
        {{/if}}
        
        {{customCloseLogic}}
    }
    {{/if}}
    
    {{#if useConfirmCallback}}
    /**
     * 确认弹窗回调
     * 用户点击确认弹窗按钮后触发
     */
    confirmCallBack(messageBoxClosedEvent: MessageBoxClosedEvent): void {
        {{#each confirmHandlers}}
        if ("{{callBackId}}" == messageBoxClosedEvent.getCallBackId()) {
            if (messageBoxClosedEvent.getResult() == MessageBoxResult.Yes) {
                // 用户确认，继续执行操作
                let operateOption = OperateOption.create();
                operateOption.setVariableValue("afterconfirm", "true");
                this.getView().invokeOperation("{{operateKey}}", operateOption);
            }
        }
        {{/each}}
    }
    {{/if}}
    
    {{#if useClosedCallback}}
    /**
     * 弹窗关闭回调
     * 子界面关闭后触发
     */
    closedCallBack(closedCallBackEvent: ClosedCallBackEvent): void {
        {{#each closedHandlers}}
        if (closedCallBackEvent.getActionId() == "{{actionId}}") {
            let rows = closedCallBackEvent.getReturnData() as ListSelectedRowCollection;
            if (rows != null && rows.size() > 0) {
                this.getModel().setValue("{{targetField}}", rows.get(0).getNumber());
            }
        }
        {{/each}}
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
