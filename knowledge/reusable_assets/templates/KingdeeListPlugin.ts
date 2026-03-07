/**
 * {{className}} - 列表插件
 * {{description}}
 * 
 * 注册位置：单据列表 > 插件
 * 基类：AbstractListPlugin
 */

import { AbstractListPlugin } from "@cosmic/bos-core/kd/bos/list/plugin";
import { 
    SetFilterEvent, 
    FilterContainerSearchClickArgs,
    BeforeShowBillFormEvent,
    BeforeFilterF7SelectEvent,
    FilterContainerInitArgs
} from "@cosmic/bos-core/kd/bos/form/events";
import { ItemClickEvent } from "@cosmic/bos-core/kd/bos/form/control/events";
import { 
    BeforePackageDataEvent,
    PackageDataEvent,
    BeforeCreateListColumnsArgs
} from "@cosmic/bos-core/kd/bos/list/events";
import { ListColumn } from "@cosmic/bos-core/kd/bos/list";
import { LocaleString } from "@cosmic/bos-core/kd/bos/localization";
import { QFilter } from "@cosmic/bos-core/kd/bos/data/filter";
import { BillShowParameter, ShowType } from "@cosmic/bos-core/kd/bos/form";
import { BusinessDataServiceHelper } from "@cosmic/bos-core/kd/bos/service";
import { HyperLinkClickArgs, BillListHyperLinkClickEvent } from "@cosmic/bos-core/kd/bos/list/events";
import { BillList } from "@cosmic/bos-core/kd/bos/list/control";
import { SchemeFilterColumn, CommonFilterColumn } from "@cosmic/bos-core/kd/bos/list/filter";

class {{className}} extends AbstractListPlugin {
    
    {{#if useSetFilter}}
    /**
     * 设置列表过滤条件
     * 触发时机：列表查询数据前
     * 作用：添加自定义过滤条件、设置排序
     */
    setFilter(e: SetFilterEvent): void {
        {{#each customFilters}}
        // 添加{{description}}过滤条件
        e.addCustomQFilter(new QFilter("{{fieldKey}}", "{{operator}}", {{value}}));
        {{/each}}
        
        {{#if setOrderBy}}
        // 设置排序字段
        e.setOrderBy("{{orderByClause}}");
        {{/if}}
        
        {{customFilterLogic}}
    }
    {{/if}}
    
    {{#if useFilterContainerSearchClick}}
    /**
     * 过滤面板搜索点击
     * 触发时机：用户点击过滤面板查询按钮
     * 作用：追加快捷过滤条件
     */
    filterContainerSearchClick(e: FilterContainerSearchClickArgs): void {
        {{#each fastFilters}}
        // 添加{{description}}快捷过滤
        e.addFastFilter("{{fieldKey}}", {{value}});
        {{/each}}
        
        {{searchClickLogic}}
    }
    {{/if}}
    
    {{#if useFilterContainerInit}}
    /**
     * 过滤面板初始化
     * 触发时机：过滤面板加载时
     * 作用：添加自定义过滤选项
     */
    filterContainerInit(e: FilterContainerInitArgs): void {
        {{#each schemeFilterColumns}}
        // 添加{{description}}方案过滤列
        let {{varName}} = new SchemeFilterColumn("{{fieldKey}}");
        {{#if setCaption}}
        {{varName}}.setCaption(new LocaleString("{{caption}}"));
        {{/if}}
        e.addFilterColumn({{varName}});
        {{/each}}
        
        {{#each commonFilterColumns}}
        // 添加{{description}}常用过滤列
        let {{varName}} = new CommonFilterColumn("{{fieldKey}}");
        e.addFilterColumn({{varName}});
        {{/each}}
    }
    {{/if}}
    
    {{#if useFilterContainerBeforeF7Select}}
    /**
     * 过滤面板F7选择前
     * 触发时机：用户在过滤面板点击F7字段
     * 作用：设置F7列表的过滤条件
     */
    filterContainerBeforeF7Select(e: BeforeFilterF7SelectEvent): void {
        let fieldName = e.getFieldName();
        
        {{#each f7Filters}}
        if ("{{fieldKey}}" == fieldName) {
            e.getQfilters().add(new QFilter("{{filterField}}", "{{operator}}", {{value}}));
        }
        {{/each}}
    }
    {{/if}}
    
    {{#if useFilterColumnSetFilter}}
    /**
     * 常用过滤列设置过滤
     * 触发时机：常用过滤字段设置值
     * 作用：自定义过滤逻辑
     */
    filterColumnSetFilter(e: SetFilterEvent): void {
        {{#each columnFilters}}
        if ("{{fieldKey}}" == e.getFieldName()) {
            e.addCustomQFilter(new QFilter("{{filterField}}", "{{operator}}", {{value}}));
        }
        {{/each}}
    }
    {{/if}}
    
    {{#if useBeforeShowBill}}
    /**
     * 列表显示单据前
     * 触发时机：用户点击列表行打开单据前
     * 作用：传递自定义参数到单据界面
     */
    beforeShowBill(e: BeforeShowBillFormEvent): void {
        let showParameter = e.getParameter() as BillShowParameter;
        
        {{#each customParams}}
        // 设置{{description}}参数
        showParameter.setCustomParam("{{paramKey}}", {{value}});
        {{/each}}
        
        {{#if setStatus}}
        // 设置打开状态
        showParameter.setStatus(OperationStatus.{{openStatus}});
        {{/if}}
        
        {{beforeShowLogic}}
    }
    {{/if}}
    
    {{#if useBeforePackageData}}
    /**
     * 列表打包数据前
     * 触发时机：列表数据打包到前端前
     * 作用：批量调整字段值
     */
    beforePackageData(e: BeforePackageDataEvent): void {
        for (let row of e.getPageData()) {
            {{#each packageDataModifiers}}
            // {{description}}
            row.set("{{fieldKey}}", {{value}});
            {{/each}}
        }
    }
    {{/if}}
    
    {{#if usePackageData}}
    /**
     * 列表打包数据
     * 触发时机：列表数据打包到前端
     * 作用：调整单元格显示值
     */
    packageData(e: PackageDataEvent): void {
        {{#each cellFormatters}}
        if ("{{fieldKey}}" == e.getColKey() && {{condition}}) {
            e.setFormatValue("{{formatValue}}");
        }
        {{/each}}
    }
    {{/if}}
    
    {{#if useBeforeCreateListColumns}}
    /**
     * 创建列表列前
     * 触发时机：列表列创建前
     * 作用：动态添加自定义列
     */
    beforeCreateListColumns(event: BeforeCreateListColumnsArgs): void {
        {{#each dynamicColumns}}
        // 添加{{description}}列
        let {{varName}} = new ListColumn();
        {{varName}}.setCaption(new LocaleString("{{caption}}"));
        {{varName}}.setKey("{{key}}");
        {{varName}}.setListFieldKey("{{listFieldKey}}");
        {{varName}}.setFieldName("{{fieldName}}");
        {{#if width}}
        {{varName}}.setWidth({{width}});
        {{/if}}
        event.addListColumn({{varName}});
        {{/each}}
    }
    {{/if}}
    
    {{#if useItemClick}}
    /**
     * 工具栏项点击
     * 触发时机：用户点击列表工具栏按钮
     */
    itemClick(e: ItemClickEvent): void {
        let itemKey = e.getItemKey();
        
        {{#each itemClickHandlers}}
        if ("{{itemKey}}" == itemKey) {
            {{handlerLogic}}
        }
        {{/each}}
    }
    {{/if}}
    
    {{#if useBillListHyperLinkClick}}
    /**
     * 列表超链接点击
     * 触发时机：用户点击列表中的超链接字段
     * 作用：实现单据联查
     */
    billListHyperLinkClick(e: HyperLinkClickArgs): void {
        let fieldKey = e.getFieldName();
        
        {{#each hyperLinkHandlers}}
        if (fieldKey == "{{fieldKey}}") {
            // 取消默认行为
            e.setCancel(true);
            
            let billLinkEvent = e.getHyperLinkClickEvent() as BillListHyperLinkClickEvent;
            let billList = billLinkEvent.getSource() as BillList;
            let entityKey = billList.getEntityId();
            let pk = billList.getFocusRowPkId();
            
            // 加载当前行数据
            let billObj = BusinessDataServiceHelper.loadSingle(pk, entityKey, "{{loadFields}}");
            if (billObj != null) {
                let showParameter = new BillShowParameter();
                showParameter.setFormId(billObj.getString("{{sourceBillTypeField}}"));
                showParameter.setPkId(billObj.getLong("{{sourceBillIdField}}"));
                showParameter.setStatus(OperationStatus.VIEW);
                showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                this.getView().showForm(showParameter);
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
