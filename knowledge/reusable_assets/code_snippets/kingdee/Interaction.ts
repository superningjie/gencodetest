/**
 * 用户交互代码片段
 * 使用场景：显示确认弹窗、消息提示等
 */

{{#if showConfirm}}
// 显示确认弹窗（在beforeDoOperation中使用）
let operate = args.getSource() as FormOperate;
let afterConfirm = operate.getOption().getVariableValue("afterconfirm", "");

if (afterConfirm == "") {
    // 创建确认回调监听器
    let confirmCallBacks = new ConfirmCallBackListener("{{confirmId}}", this.getPluginName());
    
    // 显示确认弹窗
    this.getView().showConfirm(
        "{{confirmMessage}}",                    // 提示消息
        MessageBoxOptions.{{buttonType}},        // 按钮类型
        ConfirmTypes.{{confirmType}},            // 确认类型
        confirmCallBacks                         // 回调监听器
    );
    
    // 取消当前操作，等待用户确认
    args.setCancel(true);
}

// 确认回调处理
confirmCallBack(messageBoxClosedEvent: MessageBoxClosedEvent): void {
    if ("{{confirmId}}" == messageBoxClosedEvent.getCallBackId()) {
        if (messageBoxClosedEvent.getResult() == MessageBoxResult.Yes) {
            // 用户确认，重新执行操作
            let operateOption = OperateOption.create();
            operateOption.setVariableValue("afterconfirm", "true");
            this.getView().invokeOperation("{{operateKey}}", operateOption);
        }
    }
}
{{/if}}

{{#if showMessage}}
// 显示消息提示
this.getView().showMessage("{{message}}");

// 或带类型的消息
// this.getView().showMessage("{{message}}", MessageType.{{messageType}});
{{/if}}

{{#if showError}}
// 显示错误消息
this.getView().showErrorNotification("{{errorMessage}}");
{{/if}}

{{#if showSuccess}}
// 显示成功消息（通常在afterDoOperation中使用）
afterDoOperation(args: AfterDoOperationEventArgs): void {
    if (args.getOperationResult() != null && args.getOperationResult().isSuccess()) {
        args.getOperationResult().setMessage("{{successMessage}}");
        args.getOperationResult().setShowMessage(true);
    }
}
{{/if}}

// ========== 常用交互示例 ==========

// 示例1：保存前确认
// beforeDoOperation(args: BeforeDoOperationEventArgs): void {
//     let operate = args.getSource() as FormOperate;
//     if ("save" == operate.getOperateKey()) {
//         let afterConfirm = operate.getOption().getVariableValue("afterconfirm", "");
//         if (afterConfirm == "") {
//             let confirmCallBacks = new ConfirmCallBackListener("confirmSave", this.getPluginName());
//             this.getView().showConfirm(
//                 "确定要保存当前单据吗？",
//                 MessageBoxOptions.YesNo,
//                 ConfirmTypes.Default,
//                 confirmCallBacks
//             );
//             args.setCancel(true);
//         }
//     }
// }

// 示例2：操作成功提示
// afterDoOperation(args: AfterDoOperationEventArgs): void {
//     if ("submit" == args.getOperateKey() && 
//         args.getOperationResult() != null && 
//         args.getOperationResult().isSuccess()) {
//         args.getOperationResult().setMessage("提交成功！");
//         args.getOperationResult().setShowMessage(true);
//     }
// }

// 示例3：点击按钮显示消息
// registerListener(e: EventObject): void {
//     let btn = this.getView().getControl("btnInfo") as Button;
//     btn.addClickListener({
//         click: event => {
//             let value = this.getModel().getValue("amount") as BigDecimal;
//             this.getView().showMessage(`当前金额：${value}`);
//         }
//     });
// }
