/**
 * {class_name} - 单据表单插件
 * {description}
 * 
 * 注册位置：表单设计器 > 插件 > 注册
 * 基类：AbstractBillPlugIn
 */

import {{ AbstractBillPlugIn }} from "@cosmic/bos-core/kd/bos/bill";
import {{ EventObject }} from "@cosmic/bos-script/java/util";
import {{ PropertyChangedArgs }} from "@cosmic/bos-core/kd/bos/entity/datamodel/events";
import {{ BeforeDoOperationEventArgs, AfterDoOperationEventArgs }} from "@cosmic/bos-core/kd/bos/form/events";
import {{ AfterAddRowEventArgs }} from "@cosmic/bos-core/kd/bos/entity/datamodel/events";
import {{ EntryGrid }} from "@cosmic/bos-core/kd/bos/form/control";
import {{ Button }} from "@cosmic/bos-core/kd/bos/form/control";
import {{ BillOperationStatus }} from "@cosmic/bos-core/kd/bos/bill/consts";
import {{ IBillView }} from "@cosmic/bos-core/kd/bos/bill/view";

class {class_name} extends AbstractBillPlugIn {{
    
    // 分录标识
    private readonly ENTRY_KEY = "{entry_key}";
    
    /**
     * 注册事件监听
     */
    registerListener(e: EventObject): void {{
        super.registerListener(e);
    }}
    
    /**
     * 新建单据后初始化数据
     */
    afterCreateNewData(e: EventObject): void {{
        super.afterCreateNewData(e);
        
        {init_fields_code}
        
        {create_entry_code}
    }}
    
    /**
     * 单据数据加载完成后
     */
    afterLoadData(e: EventObject): void {{
        super.afterLoadData(e);
        
        // 如果分录为空，自动创建一行
        let entryData = this.getModel().getEntryData(this.ENTRY_KEY);
        if (entryData == null || entryData.size() == 0) {{
            this.getModel().batchCreateNewEntryRow(this.ENTRY_KEY, 1);
        }}
    }}
    
    /**
     * 数据绑定到界面后
     */
    afterBindData(e: EventObject): void {{
        super.afterBindData(e);
        
        // 根据单据状态锁定界面
        let billStatus = this.getModel().getValue("billstatus") as string;
        if ("C" == billStatus || "D" == billStatus) {{
            (this.getView() as IBillView).setBillStatus(BillOperationStatus.VIEW);
        }}
    }}
    
    /**
     * 字段值发生变化时
     * 单据头字段变更时同步到所有分录行
     */
    propertyChanged(e: PropertyChangedArgs): void {{
        super.propertyChanged(e);
        
        for (let change of e.getChangeSet()) {{
            let changedField = change.getFieldName();
            {property_changed_code}
        }}
    }}
    
    /**
     * 分录行添加后
     * 自动从单据头复制人员信息到新增的分录行
     */
    afterAddRow(event: AfterAddRowEventArgs): void {{
        if (event.getEntryProp().getName() == this.ENTRY_KEY) {{
            // 获取单据头上的信息
            {head_fields_code}
            
            // 为新增的分录行填充数据
            for (let row of event.getRowDataEntities()) {{
                let rowIndex = row.getRowIndex();
                {fill_entry_code}
            }}
        }}
    }}
    
    /**
     * 执行操作前触发 - 数据校验
     */
    beforeDoOperation(args: BeforeDoOperationEventArgs): void {{
        let operate = args.getSource() as FormOperate;
        let operateKey = operate.getOperateKey();
        
        if ("save" == operateKey || "submit" == operateKey) {{
            // 保存前校验
            if (!this.validateBeforeSave()) {{
                args.setCancel(true);
            }}
        }}
    }}
    
    /**
     * 执行操作后触发
     */
    afterDoOperation(args: AfterDoOperationEventArgs): void {{
        super.afterDoOperation(args);
        
        if (args.getOperationResult() == null || !args.getOperationResult().isSuccess()) {{
            return;
        }}
        
        if ("save" == args.getOperateKey()) {{
            args.getOperationResult().setMessage("保存成功！");
            args.getOperationResult().setShowMessage(true);
        }}
    }}
    
    /**
     * 保存前数据校验
     */
    private validateBeforeSave(): boolean {{
        // 校验必填字段
        let billStatus = this.getModel().getValue("billstatus") as string;
        if (!billStatus || billStatus.trim() == "") {{
            this.getView().showMessage("单据状态不能为空！");
            return false;
        }}
        
        // 校验分录
        let entryData = this.getModel().getEntryData(this.ENTRY_KEY);
        if (entryData == null || entryData.size() == 0) {{
            this.getView().showMessage("分录不能为空，请至少添加一条记录！");
            return false;
        }}
        
        return true;
    }}
    
    /**
     * 同步字段值到所有分录行
     */
    private syncEntryField(fieldKey: string, value: any): void {{
        if (value == null) return;
        
        let entryData = this.getModel().getEntryData(this.ENTRY_KEY);
        if (entryData == null) return;
        
        for (let i = 0; i < entryData.size(); i++) {{
            this.getModel().setValue(fieldKey, value, i);
        }}
    }}
}}

let plugin = new {class_name}();
export {{ plugin }};
