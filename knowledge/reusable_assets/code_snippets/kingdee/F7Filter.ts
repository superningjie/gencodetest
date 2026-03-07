/**
 * F7基础资料选择过滤代码片段
 * 使用场景：registerListener中为BasedataEdit添加过滤条件
 */

// 获取基础资料控件
let {{controlVar}} = this.getView().getControl("{{controlKey}}") as BasedataEdit;

// 添加F7选择前监听，设置过滤条件
{{controlVar}}.addBeforeF7SelectListener({
    beforeF7Select(e: BeforeF7SelectEvent) {
        // 创建过滤条件：{{description}}
        let filter = new QFilter("{{filterField}}", "{{operator}}", {{filterValue}});
        
        // 获取列表显示参数并设置过滤
        let fsp = e.getFormShowParameter() as ListShowParameter;
        fsp.getListFilterParameter().setFilter(filter);
        
        {{#if additionalFilters}}
        // 添加额外过滤条件
        {{#each additionalFilters}}
        fsp.getListFilterParameter().addFilter(
            new QFilter("{{field}}", "{{op}}", {{val}})
        );
        {{/each}}
        {{/if}}
    }
});

// ========== 常用过滤示例 ==========

// 示例1：按组织过滤
// let orgEdit = this.getView().getControl("org") as BasedataEdit;
// orgEdit.addBeforeF7SelectListener({
//     beforeF7Select(e: BeforeF7SelectEvent) {
//         let filter = new QFilter("enable", "=", true);
//         let fsp = e.getFormShowParameter() as ListShowParameter;
//         fsp.getListFilterParameter().setFilter(filter);
//     }
// });

// 示例2：按当前用户权限过滤
// let userEdit = this.getView().getControl("user") as BasedataEdit;
// userEdit.addBeforeF7SelectListener({
//     beforeF7Select(e: BeforeF7SelectEvent) {
//         let currentOrg = this.getModel().getValue("org") as number;
//         let filter = new QFilter("org.id", "=", currentOrg);
//         let fsp = e.getFormShowParameter() as ListShowParameter;
//         fsp.getListFilterParameter().setFilter(filter);
//     }
// });
