/**
 * {{className}} - 单据转换插件
 * {{description}}
 * 
 * 注册位置：业务流开发 > 转换路线 > 插件
 * 基类：AbstractConvertPlugIn
 */

package {{packageName}};

import kd.bos.entity.BillEntityType;
import kd.bos.entity.botp.ConvertOpType;
import kd.bos.entity.botp.ConvertRuleElement;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;

// 事件参数
import kd.bos.entity.botp.plugin.args.AfterBuildQueryParemeterEventArgs;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.entity.botp.plugin.args.AfterCreateLinkEventArgs;
import kd.bos.entity.botp.plugin.args.AfterCreateTargetEventArgs;
import kd.bos.entity.botp.plugin.args.AfterFieldMappingEventArgs;
import kd.bos.entity.botp.plugin.args.AfterGetSourceDataEventArgs;
import kd.bos.entity.botp.plugin.args.BeforeBuildGroupModeEventArgs;
import kd.bos.entity.botp.plugin.args.BeforeBuildRowConditionEventArgs;
import kd.bos.entity.botp.plugin.args.BeforeCreateLinkEventArgs;
import kd.bos.entity.botp.plugin.args.BeforeCreateTargetEventArgs;
import kd.bos.entity.botp.plugin.args.BeforeGetSourceDataEventArgs;
import kd.bos.entity.botp.plugin.args.InitVariableEventArgs;

/**
 * {{className}}
 * 
 * 源单主实体：{{sourceMainEntity}}
 * 目标单主实体：{{targetMainEntity}}
 * 转换方式：{{convertOpType}}
 */
public class {{className}} extends AbstractConvertPlugIn {
    
    {{#each constants}}
    // {{description}}
    private static final String {{name}} = "{{value}}";
    {{/each}}
    
    /**
     * 初始化变量事件
     * 触发时机：转换开始时
     * 作用：获取上下文信息，构建必要变量
     */
    @Override
    public void initVariable(InitVariableEventArgs e) {
        // 获取上下文信息
        BillEntityType srcMainType = this.getSrcMainType();
        BillEntityType tgtMainType = this.getTgtMainType();
        ConvertRuleElement rule = this.getRule();
        ConvertOpType opType = this.getOpType();
        
        {{initVariableLogic}}
    }
    
    {{#if useAfterBuildQueryParameter}}
    /**
     * 构建取数参数后事件
     * 触发时机：构建源单查询参数后
     * 作用：添加额外的字段、过滤条件
     */
    @Override
    public void afterBuildQueryParemeter(AfterBuildQueryParemeterEventArgs e) {
        {{#each extraFields}}
        // 添加额外字段：{{description}}
        e.getSelectFields().add("{{fieldKey}}");
        {{/each}}
        
        {{#each extraFilters}}
        // 添加{{description}}过滤条件
        e.getFilters().add(new QFilter("{{fieldKey}}", "{{operator}}", {{value}}));
        {{/each}}
        
        {{afterBuildQueryLogic}}
    }
    {{/if}}
    
    {{#if useBeforeBuildRowCondition}}
    /**
     * 编译数据筛选条件前事件
     * 触发时机：构建行筛选条件前
     * 作用：设置或追加行筛选条件
     */
    @Override
    public void beforeBuildRowCondition(BeforeBuildRowConditionEventArgs e) {
        {{#if ignoreOriginalCondition}}
        // 忽略规则原生的条件
        e.setIgnoreOriginalCondition(true);
        {{/if}}
        
        {{#each rowConditions}}
        // 添加{{description}}条件
        e.addCondition("{{fieldKey}}", "{{operator}}", {{value}});
        {{/each}}
        
        {{beforeBuildRowConditionLogic}}
    }
    {{/if}}
    
    {{#if useBeforeGetSourceData}}
    /**
     * 取源单数据前事件
     * 触发时机：从数据库取源单数据前
     * 作用：修改取数语句、取数条件
     */
    @Override
    public void beforeGetSourceData(BeforeGetSourceDataEventArgs e) {
        {{beforeGetSourceDataLogic}}
    }
    {{/if}}
    
    {{#if useAfterGetSourceData}}
    /**
     * 取源单数据后事件
     * 触发时机：从数据库取到源单数据后
     * 作用：获取其他定制引用数据、替换系统数据
     */
    @Override
    public void afterGetSourceData(AfterGetSourceDataEventArgs e) {
        {{#each sourceDataModifiers}}
        {{this}}
        {{/each}}
        
        {{afterGetSourceDataLogic}}
    }
    {{/if}}
    
    {{#if useBeforeBuildGroupMode}}
    /**
     * 构建分单、行合并模式之前事件
     * 触发时机：构建分单、合并策略前
     * 作用：调整分单、合并策略及依赖字段
     */
    @Override
    public void beforeBuildGroupMode(BeforeBuildGroupModeEventArgs e) {
        {{#if changeGroupMode}}
        // 设置分单模式
        e.setGroupMode(GroupMode.{{groupMode}});  // BILL:按单据分, ENTRY:按分录行分
        {{/if}}
        
        {{#each groupByFields}}
        // 添加{{description}}分单字段
        e.addGroupByField("{{fieldKey}}");
        {{/each}}
        
        {{beforeBuildGroupModeLogic}}
    }
    {{/if}}
    
    {{#if useBeforeCreateTarget}}
    /**
     * 初始化创建目标单据数据包前事件（选单时触发）
     * 触发时机：选单时，基于现有目标单数据包进行追加处理前
     * 作用：获取现有目标单数据包，进行定制处理
     */
    @Override
    public void beforeCreateTarget(BeforeCreateTargetEventArgs e) {
        {{#if useExistingData}}
        // 获取现有目标单数据包
        List<DynamicObject> existingData = e.getTargetDatas();
        {{/if}}
        
        {{beforeCreateTargetLogic}}
    }
    {{/if}}
    
    {{#if useAfterCreateTarget}}
    /**
     * 创建目标单据数据包后事件（下推时触发）
     * 触发时机：下推时，分单规则创建好目标单后
     * 作用：对生成的目标单进行定制处理
     */
    @Override
    public void afterCreateTarget(AfterCreateTargetEventArgs e) {
        List<DynamicObject> targetDatas = e.getTargetDatas();
        
        for (DynamicObject targetData : targetDatas) {
            {{#each targetDataInit}}
            // {{description}}
            targetData.set("{{fieldKey}}", {{value}});
            {{/each}}
        }
        
        {{afterCreateTargetLogic}}
    }
    {{/if}}
    
    {{#if useAfterFieldMapping}}
    /**
     * 目标字段赋值完毕后事件
     * 触发时机：字段映射规则执行完毕后
     * 作用：继续填写目标字段值
     */
    @Override
    public void afterFieldMapping(AfterFieldMappingEventArgs e) {
        DynamicObject[] srcDatas = e.getSrcData();      // 源单数据
        DynamicObject targetData = e.getTargetData();   // 目标单数据
        
        {{#each fieldMappings}}
        // {{description}}
        {{sourceType}} {{sourceVar}} = srcDatas[0].get("{{sourceField}}") != null 
            ? ({{sourceType}}) srcDatas[0].get("{{sourceField}}") 
            : {{defaultValue}};
        targetData.set("{{targetField}}", {{transformation}});
        {{/each}}
        
        {{afterFieldMappingLogic}}
    }
    {{/if}}
    
    {{#if useBeforeCreateLink}}
    /**
     * 记录关联关系前事件
     * 触发时机：系统记录源单与目标单关联关系前
     * 作用：取消记录关联关系
     */
    @Override
    public void beforeCreateLink(BeforeCreateLinkEventArgs e) {
        {{#if cancelCreateLink}}
        // 取消记录关联关系
        e.setCancel(true);
        {{/if}}
        
        {{beforeCreateLinkLogic}}
    }
    {{/if}}
    
    {{#if useAfterCreateLink}}
    /**
     * 记录关联关系后事件
     * 触发时机：系统记录关联关系后
     * 作用：根据关联关系同步其他数据（如子单据体）
     */
    @Override
    public void afterCreateLink(AfterCreateLinkEventArgs e) {
        {{#if carrySubEntry}}
        // 携带其他子单据体数据
        {{carrySubEntryLogic}}
        {{/if}}
        
        {{afterCreateLinkLogic}}
    }
    {{/if}}
    
    {{#if useAfterConvert}}
    /**
     * 单据转换后事件（最后执行）
     * 触发时机：转换流程最后阶段
     * 作用：对生成的目标单数据进行最后修改
     */
    @Override
    public void afterConvert(AfterConvertEventArgs e) {
        List<DynamicObject> targetDatas = e.getTargetDatas();
        
        for (DynamicObject targetData : targetDatas) {
            {{#each finalModifiers}}
            // {{description}}
            targetData.set("{{fieldKey}}", {{value}});
            {{/each}}
        }
        
        {{afterConvertLogic}}
    }
    {{/if}}
    
    {{#each customMethods}}
    /**
     * {{description}}
     */
    private {{returnType}} {{methodName}}({{params}}) {
        {{methodBody}}
    }
    {{/each}}
}
