/**
 * 列表过滤代码片段
 * 使用场景：列表插件中设置过滤条件
 */

{{#if useSetFilter}}
// 在setFilter事件中设置列表过滤
setFilter(e: SetFilterEvent): void {
    {{#each filters}}
    // {{description}}
    e.addCustomQFilter(new QFilter("{{fieldKey}}", "{{operator}}", {{value}}));
    {{/each}}
    
    {{#if setOrderBy}}
    // 设置排序
    e.setOrderBy("{{orderByClause}}");
    {{/if}}
}
{{/if}}

{{#if useFilterContainerBeforeF7Select}}
// 过滤面板F7选择前设置过滤
filterContainerBeforeF7Select(e: BeforeFilterF7SelectEvent): void {
    let fieldName = e.getFieldName();
    
    {{#each f7Filters}}
    if ("{{fieldKey}}" == fieldName) {
        e.getQfilters().add(new QFilter("{{filterField}}", "{{operator}}", {{value}}));
    }
    {{/each}}
}
{{/if}}

{{#if useFilterContainerSearchClick}}
// 过滤面板搜索点击时追加快捷条件
filterContainerSearchClick(e: FilterContainerSearchClickArgs): void {
    {{#each fastFilters}}
    e.addFastFilter("{{fieldKey}}", {{value}});
    {{/each}}
}
{{/if}}

// ========== 常用列表过滤示例 ==========

// 示例1：按组织过滤列表数据
// setFilter(e: SetFilterEvent): void {
//     let currentOrg = this.getPageCache().get("currentOrg");
//     e.addCustomQFilter(new QFilter("org.id", "=", currentOrg));
// }

// 示例2：按状态过滤并排序
// setFilter(e: SetFilterEvent): void {
//     e.addCustomQFilter(new QFilter("billstatus", "!=", "Z"));  // 排除暂存
//     e.setOrderBy("bizdate desc, billno desc");
// }

// 示例3：过滤面板F7动态过滤
// filterContainerBeforeF7Select(e: BeforeFilterF7SelectEvent): void {
//     let fieldName = e.getFieldName();
//     if ("org.id" == fieldName) {
//         e.getQfilters().add(new QFilter("enable", "=", true));
//     }
// }

// 示例4：搜索时追加默认条件
// filterContainerSearchClick(e: FilterContainerSearchClickArgs): void {
//     e.addFastFilter("enable", true);
// }

// 示例5：组合条件过滤
// setFilter(e: SetFilterEvent): void {
//     // 条件1：有效状态
//     e.addCustomQFilter(new QFilter("enable", "=", true));
//     // 条件2：当前用户相关
//     let currentUser = this.getPageCache().get("userId");
//     e.addCustomQFilter(new QFilter("creator.id", "=", currentUser));
//     // 条件3：日期范围
//     e.addCustomQFilter(new QFilter("bizdate", ">=", "2024-01-01"));
// }
