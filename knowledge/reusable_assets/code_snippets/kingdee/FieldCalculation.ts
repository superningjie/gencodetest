/**
 * 字段联动计算代码片段
 * 使用场景：propertyChanged事件中实现字段联动计算
 * 
 * 示例：数量 * 单价 = 金额
 */

// 字段联动计算
for (let change of e.getChangeSet()) {
    let changedField = change.getFieldName();
    let rowIndex = change.getRowIndex();
    
    // 当{{triggerField}}字段变化时
    if ("{{triggerField}}" == changedField) {
        {{#each calcFields}}
        let {{fieldName}} = this.getModel().getValue("{{fieldKey}}", rowIndex) as {{fieldType}};
        {{/each}}
        
        // 执行计算：{{formula}}
        let result = {{calculationExpression}};
        
        // 设置计算结果到目标字段
        this.getModel().setValue("{{targetField}}", result, rowIndex);
    }
}

// ========== 常用计算示例 ==========

// 示例1：金额计算（数量 * 单价 = 金额）
// for (let change of e.getChangeSet()) {
//     let rowIndex = change.getRowIndex();
//     let qty = this.getModel().getValue("qty", rowIndex) as BigDecimal;
//     let price = this.getModel().getValue("price", rowIndex) as BigDecimal;
//     let amount = qty.multiply(price);
//     this.getModel().setValue("amount", amount, rowIndex);
// }

// 示例2：日期计算（订单日期 + 账期 = 付款日期）
// let bizDate = this.getModel().getValue("bizDate") as Date;
// let billingDays = this.getModel().getValue("billingdays") as number;
// let payDate = bizDate.setDate(bizDate.getDate() + billingDays);
// this.getModel().setValue("paydate", new Date(payDate));
