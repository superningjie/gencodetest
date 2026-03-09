/**
 * 界面控件控制代码片段
 * 使用场景：根据条件锁定或隐藏界面控件
 */

{{#if useInAfterBindData}}
// 在afterBindData中根据数据状态控制界面
let billStatus = this.getModel().getValue("{{statusField}}") as string;

{{#each uiControls}}
{{#if lockCondition}}
// 当{{description}}时，{{action}}控件
if ({{lockCondition}}) {
    {{#if isEntryField}}
    // 分录字段
    let rows = this.getModel().getEntryData("{{entryKey}}");
    for (let i = 0; i < rows.size(); i++) {
        this.getView().set{{actionType}}(false, i, "{{controlKey}}");
    }
    {{else}}
    // 单头字段
    this.getView().set{{actionType}}(false, "{{controlKey}}");
    {{/if}}
}
{{/if}}
{{/each}}
{{/if}}

{{#if useInPropertyChanged}}
// 在propertyChanged中根据字段值变化控制界面
for (let change of e.getChangeSet()) {
    if ("{{triggerField}}" == change.getFieldName()) {
        let value = this.getModel().getValue("{{triggerField}}") as {{fieldType}};
        
        {{#each conditionalControls}}
        // 当{{description}}时
        if ({{condition}}) {
            this.getView().setVisible({{visible}}, "{{controlKey}}");
            this.getView().setEnable({{enable}}, "{{controlKey}}");
        }
        {{/each}}
    }
}
{{/if}}

// ========== 常用控制示例 ==========

// 示例1：根据单据状态锁定整个界面
// let billStatus = this.getModel().getValue("billstatus") as string;
// if ("C" == billStatus) {  // 已审核
//     (this.getView() as IBillView).setBillStatus(BillOperationStatus.VIEW);
// }

// 示例2：字段值改变时控制其他字段可见性
// propertyChanged(e: PropertyChangedArgs): void {
//     for (let change of e.getChangeSet()) {
//         if ("billtype" == change.getFieldName()) {
//             let billType = this.getModel().getValue("billtype") as string;
//             if ("SPECIAL" == billType) {
//                 this.getView().setVisible(true, "specialfield");
//             } else {
//                 this.getView().setVisible(false, "specialfield");
//             }
//         }
//     }
// }

// 示例3：根据分录数据锁定行字段
// let entryGrid = this.getView().getControl("entryentity") as EntryGrid;
// entryGrid.addDataBindListener({
//     entryGridBindData(event) {
//         let rows = event.getRows();
//         for (let i = 0; i < rows.size(); i++) {
//             let row = rows.get(i);
//             let isLocked = row.get("islocked") as boolean;
//             if (isLocked) {
//                 plugin.getView().setEnable(false, row.getRowIndex(), "amount");
//             }
//         }
//     }
// });
