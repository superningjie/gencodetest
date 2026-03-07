/**
 * 单据联查代码片段
 * 使用场景：打开关联单据界面
 */

{{#if fromBillForm}}
// 在表单插件中打开其他单据
let fsp = new BillShowParameter();
fsp.setFormId("{{targetFormId}}");           // 目标单据标识
fsp.setPkId({{pkId}});                       // 单据主键
fsp.setStatus(OperationStatus.{{status}});   // 打开状态：VIEW/EDIT/ADD

// 设置打开方式
fsp.getOpenStyle().setShowType(ShowType.{{showType}});  // Modal/MainNewTabPage/InnerItem

{{#if customParams}}
// 设置自定义参数
{{#each customParams}}
fsp.setCustomParam("{{key}}", {{value}});
{{/each}}
{{/if}}

{{#if closeCallback}}
// 设置关闭回调
let closeCallBack = new CloseCallBack(this, "{{callbackId}}");
fsp.setCloseCallBack(closeCallBack);
{{/if}}

this.getView().showForm(fsp);
{{/if}}

{{#if fromList}}
// 在列表插件中超链接联查
billListHyperLinkClick(args: HyperLinkClickArgs): void {
    let fieldKey = args.getFieldName();
    
    if (fieldKey == "{{sourceField}}") {
        // 取消默认行为
        args.setCancel(true);
        
        let billLinkEvent = args.getHyperLinkClickEvent() as BillListHyperLinkClickEvent;
        let billList = billLinkEvent.getSource() as BillList;
        let entityKey = billList.getEntityId();
        let pk = billList.getFocusRowPkId();
        
        // 加载当前行数据获取源单信息
        let billObj = BusinessDataServiceHelper.loadSingle(
            pk, 
            entityKey, 
            "{{loadFields}}"
        );
        
        if (billObj != null) {
            let showParameter = new BillShowParameter();
            showParameter.setFormId(billObj.getString("{{sourceBillTypeField}}"));
            showParameter.setPkId(billObj.getLong("{{sourceBillIdField}}"));
            showParameter.setStatus(OperationStatus.VIEW);
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            this.getView().showForm(showParameter);
        }
    }
}
{{/if}}

{{#if closedCallback}}
// 弹窗关闭回调处理
closedCallBack(closedCallBackEvent: ClosedCallBackEvent): void {
    if (closedCallBackEvent.getActionId() == "{{callbackId}}") {
        let rows = closedCallBackEvent.getReturnData() as ListSelectedRowCollection;
        if (rows != null && rows.size() > 0) {
            this.getModel().setValue("{{targetField}}", rows.get(0).getNumber());
        }
    }
}
{{/if}}

// ========== 常用联查示例 ==========

// 示例1：打开指定单据查看
// let fsp = new BillShowParameter();
// fsp.setFormId("bd_material");
// fsp.setPkId(materialId);
// fsp.setStatus(OperationStatus.VIEW);
// fsp.getOpenStyle().setShowType(ShowType.MainNewTabPage);
// this.getView().showForm(fsp);

// 示例2：打开F7选择列表并获取返回值
// let fsp = new ListShowParameter();
// fsp.setBillFormId("bd_currency");
// fsp.setFormId("bos_listf7");
// fsp.setLookUp(true);
// fsp.setMultiSelect(false);
// let closeCallBack = new CloseCallBack(this, "selectCurrency");
// fsp.setCloseCallBack(closeCallBack);
// this.getView().showForm(fsp);

// 示例3：带返回值的弹窗
// let fsp = new FormShowParameter();
// fsp.setFormId("demo_selectform");
// fsp.getOpenStyle().setShowType(ShowType.Modal);
// let css = new StyleCss();
// css.setWidth("960px");
// css.setHeight("580px");
// fsp.getOpenStyle().setInlineStyleCss(css);
// let closeCallBack = new CloseCallBack(this, "myCallback");
// fsp.setCloseCallBack(closeCallBack);
// this.getView().showForm(fsp);
