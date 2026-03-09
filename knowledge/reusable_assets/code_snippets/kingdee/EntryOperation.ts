/**
 * 分录行操作代码片段
 * 使用场景：分录行添加、删除、批量创建
 */

{{#if batchCreateRows}}
// 批量创建分录行（在afterCreateNewData中使用）
// 批量创建{{rowCount}}行分录
this.getModel().batchCreateNewEntryRow("{{entryKey}}", {{rowCount}});

// 初始化每行数据
for (let i = 0; i < {{rowCount}}; i++) {
    {{#each initFields}}
    this.getModel().setValue("{{fieldKey}}", {{fieldValue}}, i);
    {{/each}}
}
{{/if}}

{{#if afterAddRow}}
// 分录行添加后初始化（在afterAddRow中使用）
afterAddRow(event: AfterAddRowEventArgs): void {
    if (event.getEntryProp().getName() == "{{entryKey}}") {
        {{#if copyFromHead}}
        // 从单据头复制值到分录
        let headValue = this.getModel().getValue("{{headField}}") as {{headFieldType}};
        {{/if}}
        
        for (let row of event.getRowDataEntities()) {
            {{#each rowInitFields}}
            // {{description}}
            this.getModel().setValue("{{fieldKey}}", {{fieldValue}}, row.getRowIndex());
            {{/each}}
            
            {{#if copyFromHead}}
            this.getModel().setValue("{{entryField}}", headValue, row.getRowIndex());
            {{/if}}
        }
    }
}
{{/if}}

{{#if deleteEntryRows}}
// 删除分录行数据
this.getModel().deleteEntryData("{{entryKey}}");
{{/if}}

{{#if appendEntryRow}}
// 在指定位置追加分录行
this.getModel().appendEntryRow("{{entryKey}}", {{rowIndex}}, {{rowCount}});
{{/if}}

// ========== 常用分录操作示例 ==========

// 示例1：新建单据时默认创建10行分录
// afterCreateNewData(e: EventObject): void {
//     super.afterCreateNewData(e);
//     this.getModel().batchCreateNewEntryRow("entryentity", 10);
//     for (let i = 0; i < 10; i++) {
//         this.getModel().setValue("seq", i + 1, i);
//         this.getModel().setValue("defaultflag", true, i);
//     }
// }

// 示例2：分录行添加后自动填充付款日期
// afterAddRow(event: AfterAddRowEventArgs): void {
//     if (event.getEntryProp().getName() == "paymententry") {
//         let bizDate = this.getModel().getValue("bizDate") as Date;
//         let billingDays = this.getModel().getValue("billingdays") as number;
//         let payDate = bizDate.setDate(bizDate.getDate() + billingDays);
//         
//         for (let row of event.getRowDataEntities()) {
//             this.getModel().setValue("paydate", new Date(payDate), row.getRowIndex());
//         }
//     }
// }

// 示例3：复制数据后清除特定分录行
// afterCopyData(event: EventObject): void {
//     this.getModel().deleteEntryData("preentry");
//     this.getModel().appendEntryRow("preentry", 0, 1);
// }
