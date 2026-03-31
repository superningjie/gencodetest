# 金蝶云苍穹开发技术指引

> **适用对象**: 金蝶星瀚HR AI顾问系统 V2.0  
> **技术栈**: 金蝶云苍穹平台  
> **核心差异**: 元数据驱动 + KingScript + 插件框架（非 Spring）

---

## 目录

1. [金蝶苍穹技术栈概览](#1-金蝶苍穹技术栈概览)
2. [元数据驱动开发](#2-元数据驱动开发)
3. [KingScript 开发指南](#3-kingscript-开发指南)
4. [插件开发框架](#4-插件开发框架)
5. [代码生成适配](#5-代码生成适配)

---

## 1. 金蝶苍穹技术栈概览

### 1.1 与开源技术栈对比

| 维度 | 开源 Spring 生态 | 金蝶云苍穹 |
|------|-----------------|-----------|
| **开发模式** | 代码优先 | 元数据驱动 + 代码扩展 |
| **后端语言** | Java/Kotlin | KingScript / Java 插件 |
| **服务层** | Spring Service | 业务服务 + 操作服务 |
| **数据访问** | JPA/MyBatis | 实体元数据 + 查询服务 |
| **接口层** | REST Controller | 开放 API + 业务服务 |
| **扩展机制** | AOP / 继承 | 插件 + 扩展点 |
| **前端** | Vue/React | 苍穹 Design/移动 Design |

### 1.2 核心概念映射

```
Spring 生态                    金蝶苍穹
─────────────────────────────────────────────────
@Entity          →            实体元数据 (kdorm.Entity)
@Repository      →            数据查询服务 (QueryService)
@Service         →            业务服务 (BusinessService)
@Controller      →            开放 API / 操作服务
@Component      →            插件 (IPlugin)
@Configuration   →            应用配置元数据
```

### 1.3 开发层次结构

```
┌─────────────────────────────────────────────────────────┐
│                      应用层 (App)                        │
│         单据、报表、工作流、移动应用                        │
├─────────────────────────────────────────────────────────┤
│                      服务层 (Service)                    │
│    业务服务 (BusinessService) + 操作服务 (OperationService) │
├─────────────────────────────────────────────────────────┤
│                      插件层 (Plugin)                     │
│    表单插件、列表插件、操作插件、服务插件                     │
├─────────────────────────────────────────────────────────┤
│                      实体层 (Entity)                     │
│         元数据实体 + KingScript 业务逻辑                   │
├─────────────────────────────────────────────────────────┤
│                      数据层 (Data)                       │
│              苍穹 ORM + 多数据库支持                        │
└─────────────────────────────────────────────────────────┘
```

---

## 2. 元数据驱动开发

### 2.1 实体元数据 (Entity Metadata)

```json
// 实体元数据示例 - PersonEntity
{
  "entityId": "kingdee.hr.person",
  "entityName": "人员信息",
  "tableName": "t_hr_person",
  "fields": [
    {
      "fieldId": "name",
      "fieldName": "姓名",
      "dataType": "string",
      "length": 100,
      "required": true,
      "index": true
    },
    {
      "fieldId": "entryDate",
      "fieldName": "入职日期",
      "dataType": "date",
      "required": true
    },
    {
      "fieldId": "department",
      "fieldName": "所属部门",
      "dataType": "ref",
      "refEntity": "kingdee.hr.department",
      "relation": "many-to-one"
    }
  ],
  "indexes": [
    {
      "name": "idx_name_dept",
      "fields": ["name", "department"]
    }
  ]
}
```

### 2.2 业务服务元数据

```json
// 业务服务定义
{
  "serviceId": "kingdee.hr.personService",
  "serviceName": "人员服务",
  "operations": [
    {
      "operationId": "entry",
      "operationName": "入职办理",
      "inputParams": [
        {"name": "personInfo", "type": "entity", "entityId": "kingdee.hr.person"},
        {"name": "entryDate", "type": "date", "required": true}
      ],
      "outputParams": [
        {"name": "result", "type": "entity", "entityId": "kingdee.hr.person"}
      ],
      "script": "// KingScript 业务逻辑"
    }
  ]
}
```

### 2.3 表单元数据

```json
// 表单定义
{
  "formId": "kingdee.hr.person.entry",
  "formName": "入职登记",
  "entityId": "kingdee.hr.person",
  "layout": {
    "type": "card",
    "sections": [
      {
        "title": "基本信息",
        "fields": ["name", "gender", "birthDate"]
      },
      {
        "title": "工作信息",
        "fields": ["department", "position", "entryDate"]
      }
    ]
  },
  "plugins": [
    {"pluginId": "entryValidation", "type": "form", "className": "com.kingdee.hr.EntryFormPlugin"}
  ]
}
```

---

## 3. KingScript 开发指南

### 3.1 KingScript 语言特性

```kingscript
// KingScript 示例 - 人员入职业务逻辑

/**
 * 人员入职服务
 * @service kingdee.hr.personService
 */
service PersonService {
    
    /**
     * 入职办理
     * @param personInfo 人员信息
     * @param entryDate 入职日期
     * @return 入职后的人员信息
     */
    operation entry(personInfo, entryDate) {
        // 1. 数据校验
        if (personInfo == null) {
            throw BusinessException("人员信息不能为空");
        }
        
        // 2. 检查部门是否存在
        var dept = queryService.loadSingle(
            "kingdee.hr.department", 
            "id = " + personInfo.department
        );
        if (dept == null) {
            throw BusinessException("部门不存在");
        }
        
        // 3. 设置入职状态
        personInfo.entryStatus = "已入职";
        personInfo.entryDate = entryDate;
        
        // 4. 保存实体
        var saved = entityService.save("kingdee.hr.person", personInfo);
        
        // 5. 触发工作流
        workflowService.startInstance(
            "kingdee.hr.entryProcess",
            saved.id
        );
        
        return saved;
    }
    
    /**
     * 批量更新部门
     */
    operation batchUpdateDept(personIds, newDeptId) {
        // 使用批量操作
        var updateCount = entityService.update(
            "kingdee.hr.person",
            "id in (" + personIds.join(",") + ")",
            {"department": newDeptId}
        );
        
        return updateCount;
    }
}
```

### 3.2 KingScript vs Java

| 特性 | KingScript | Java |
|------|-----------|------|
| 类型系统 | 动态类型 | 静态类型 |
| 语法 | 类 JavaScript | 传统 Java |
| 运行环境 | 苍穹脚本引擎 | JVM |
| 数据库访问 | 元数据 ORM | JDBC/JPA |
| 事务控制 | 自动/声明式 | 编程式/@Transactional |
| 调试 | 日志 + 调试器 | IDE 调试 |

### 3.3 KingScript 代码模板

```kingscript
// 标准服务模板
service ${ServiceName} {
    
    /**
     * ${OperationDescription}
     */
    operation ${operationName}(${inputParams}) {
        // 参数校验
        ${validationLogic}
        
        // 业务逻辑
        ${businessLogic}
        
        // 数据操作
        ${dataOperation}
        
        // 返回结果
        return ${returnValue};
    }
}

// 查询模板
var result = queryService.query(
    "${entityId}",                    // 实体ID
    "${conditions}",                  // 查询条件
    "${fields}",                      // 返回字段
    {
        "page": ${pageNum},
        "size": ${pageSize},
        "orderBy": "${orderField} ${orderDirection}"
    }
);

// 实体操作模板
var entity = {
    "${field1}": ${value1},
    "${field2}": ${value2}
};

// 保存
var saved = entityService.save("${entityId}", entity);

// 更新
entityService.update("${entityId}", "id = " + id, updates);

// 删除
entityService.delete("${entityId}", "id = " + id);
```

---

## 4. 插件开发框架

### 4.1 插件类型

```
金蝶苍穹插件类型
├── 表单插件 (IFormPlugin)
│   ├── 表单加载事件
│   ├── 字段值变更事件
│   ├── 表单保存前/后事件
│   └── 按钮点击事件
│
├── 列表插件 (IListPlugin)
│   ├── 列表加载事件
│   ├── 过滤条件构建事件
│   └── 批量操作事件
│
├── 操作插件 (IOperationPlugin)
│   ├── 操作前校验
│   └── 操作后处理
│
├── 服务插件 (IServicePlugin)
│   ├── 服务调用前拦截
│   └── 服务调用后处理
│
└── 数据实体插件 (IEntityPlugin)
    ├── 保存前/后事件
    └── 删除前/后事件
```

### 4.2 插件代码模板

```java
// 表单插件模板 - Java
package com.kingdee.hr.plugins;

import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.events.BeforeDoOperationArgs;
import kd.bos.orm.operate.result.OperationResult;

/**
 * ${FormName} 表单插件
 * @description ${Description}
 * @author AI-Generated
 */
public class ${ClassName}FormPlugin extends AbstractFormPlugin {
    
    /**
     * 表单加载后事件
     */
    @Override
    public void afterDoOperation(AfterDoOperationArgs e) {
        super.afterDoOperation(e);
        
        String operationKey = e.getOperationKey();
        
        if ("save".equals(operationKey)) {
            // 保存后的业务逻辑
            ${AfterSaveLogic}
        }
    }
    
    /**
     * 字段值变更事件
     */
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        
        String fieldName = e.getProperty().getName();
        
        if ("${FieldName}".equals(fieldName)) {
            // 字段值变更处理
            Object newValue = e.getNewValue();
            ${FieldChangeLogic}
        }
    }
    
    /**
     * 保存前校验
     */
    @Override
    public void beforeDoOperation(BeforeDoOperationArgs e) {
        super.beforeDoOperation(e);
        
        String operationKey = e.getOperationKey();
        
        if ("save".equals(operationKey)) {
            // 保存前校验
            DynamicObject data = this.getModel();
            ${ValidationLogic}
        }
    }
}
```

```kingscript
// 表单插件模板 - KingScript
/**
 * ${FormName} 表单插件
 */
plugin ${PluginName} extends FormPlugin {
    
    /**
     * 表单加载后
     */
    onAfterLoad() {
        var model = this.getModel();
        ${LoadLogic}
    }
    
    /**
     * 字段值变更
     */
    onFieldChange(fieldName, newValue, oldValue) {
        if (fieldName == "${FieldName}") {
            ${FieldChangeLogic}
        }
    }
    
    /**
     * 保存前校验
     */
    onBeforeSave() {
        var model = this.getModel();
        ${ValidationLogic}
        return true; // 返回 false 阻止保存
    }
}
```

### 4.3 扩展点定义

```java
// 扩展点接口定义
public interface ${ExtensionPointName} extends ExtensionPoint {
    
    /**
     * ${MethodDescription}
     * @param ${ParamName} 参数说明
     * @return 返回值说明
     */
    ${ReturnType} ${methodName}(${ParameterType} ${ParamName});
}

// 扩展实现
public class ${ExtensionImplName} implements ${ExtensionPointName} {
    
    @Override
    public ${ReturnType} ${methodName}(${ParameterType} ${ParamName}) {
        ${ImplementationLogic}
    }
}
```

---

## 5. 代码生成适配

### 5.1 实现类型映射

```python
# 金蝶苍穹实现类型定义
class KingdeeImplementationType(Enum):
    """金蝶苍穹实现类型"""
    
    # 元数据
    ENTITY_METADATA = "entity_metadata"          # 实体元数据
    FORM_METADATA = "form_metadata"              # 表单元数据
    SERVICE_METADATA = "service_metadata"        # 服务元数据
    
    # KingScript
    KINGSCRIPT_SERVICE = "kingscript_service"    # KingScript 服务
    KINGSCRIPT_OPERATION = "kingscript_operation" # KingScript 操作
    KINGSCRIPT_PLUGIN = "kingscript_plugin"      # KingScript 插件
    
    # Java 插件
    JAVA_FORM_PLUGIN = "java_form_plugin"        # Java 表单插件
    JAVA_LIST_PLUGIN = "java_list_plugin"        # Java 列表插件
    JAVA_SERVICE_PLUGIN = "java_service_plugin"  # Java 服务插件
    JAVA_ENTITY_PLUGIN = "java_entity_plugin"    # Java 实体插件
    
    # 扩展点
    JAVA_EXTENSION_POINT = "java_extension_point"      # 扩展点定义
    JAVA_EXTENSION_IMPL = "java_extension_impl"        # 扩展实现
    
    # 报表
    KDORM_REPORT = "kdorm_report"                # KDORM 报表
    SQL_REPORT = "sql_report"                    # SQL 报表
```

### 5.2 技术栈推断规则

```yaml
# 技术栈推断规则 - 金蝶苍穹版
tech_stack_inference:
  rules:
    # 元数据相关
    - pattern: "实体|元数据|字段|属性"
      stack: "entity_metadata"
      impl_type: "entity_metadata"
      language: "json"
      
    - pattern: "表单|界面|页面|布局"
      stack: "form_metadata"
      impl_type: "form_metadata"
      language: "json"
      
    # KingScript 服务
    - pattern: "服务|业务逻辑|接口|API"
      stack: "kingscript_service"
      impl_type: "kingscript_service"
      language: "kingscript"
      
    # 插件开发
    - pattern: "插件|表单插件|列表插件|校验"
      stack: "kingdee_plugin"
      impl_type: "java_form_plugin"
      language: "java"
      
    # 扩展点
    - pattern: "扩展|扩展点|自定义|hook"
      stack: "extension_point"
      impl_type: "java_extension_point"
      language: "java"
      
    # 报表
    - pattern: "报表|统计|查询|kdorm"
      stack: "kdorm_report"
      impl_type: "kdorm_report"
      language: "kdorm"
```

### 5.3 代码生成模板路径映射

```python
# 模板路径映射
template_mapping = {
    # 元数据
    KingdeeImplementationType.ENTITY_METADATA: 
        "kingdee/metadata/entity.json.j2",
    KingdeeImplementationType.FORM_METADATA: 
        "kingdee/metadata/form.json.j2",
    KingdeeImplementationType.SERVICE_METADATA: 
        "kingdee/metadata/service.json.j2",
    
    # KingScript
    KingdeeImplementationType.KINGSCRIPT_SERVICE: 
        "kingdee/kingscript/service.ks.j2",
    KingdeeImplementationType.KINGSCRIPT_OPERATION: 
        "kingdee/kingscript/operation.ks.j2",
    KingdeeImplementationType.KINGSCRIPT_PLUGIN: 
        "kingdee/kingscript/plugin.ks.j2",
    
    # Java 插件
    KingdeeImplementationType.JAVA_FORM_PLUGIN: 
        "kingdee/java/form_plugin.java.j2",
    KingdeeImplementationType.JAVA_LIST_PLUGIN: 
        "kingdee/java/list_plugin.java.j2",
    KingdeeImplementationType.JAVA_SERVICE_PLUGIN: 
        "kingdee/java/service_plugin.java.j2",
    KingdeeImplementationType.JAVA_ENTITY_PLUGIN: 
        "kingdee/java/entity_plugin.java.j2",
    
    # 扩展点
    KingdeeImplementationType.JAVA_EXTENSION_POINT: 
        "kingdee/java/extension_point.java.j2",
    KingdeeImplementationType.JAVA_EXTENSION_IMPL: 
        "kingdee/java/extension_impl.java.j2",
    
    # 报表
    KingdeeImplementationType.KDORM_REPORT: 
        "kingdee/report/kdorm_report.xml.j2",
}
```

### 5.4 包名规范

```python
def determine_kingdee_package(module: str, impl_type: KingdeeImplementationType) -> str:
    """确定金蝶苍穹包名"""
    
    base_packages = {
        'HR_ATTENDANCE': 'com.kingdee.hr.attendance',
        'HR_PAYROLL': 'com.kingdee.hr.payroll',
        'HR_CORE': 'com.kingdee.hr.core',
        'HR_RECRUITMENT': 'com.kingdee.hr.recruitment',
    }
    
    base = base_packages.get(module, 'com.kingdee.hr')
    
    type_suffix = {
        KingdeeImplementationType.JAVA_FORM_PLUGIN: '.plugin.form',
        KingdeeImplementationType.JAVA_LIST_PLUGIN: '.plugin.list',
        KingdeeImplementationType.JAVA_SERVICE_PLUGIN: '.plugin.service',
        KingdeeImplementationType.JAVA_ENTITY_PLUGIN: '.plugin.entity',
        KingdeeImplementationType.JAVA_EXTENSION_POINT: '.extension',
        KingdeeImplementationType.JAVA_EXTENSION_IMPL: '.extension.impl',
    }
    
    suffix = type_suffix.get(impl_type, '')
    return base + suffix
```

---

## 6. 与通用优化指南的映射

### 6.1 能力映射表

| 通用优化项 | 金蝶苍穹适配 | 文档参考 |
|-----------|-------------|---------|
| LLM Prompt 工程 | 需增加 KingScript 示例和苍穹上下文 | [PROMPT_ENGINEERING_BEST_PRACTICES.md](../PROMPT_ENGINEERING_BEST_PRACTICES.md) |
| 向量检索资产匹配 | 需索引苍穹元数据和插件模板 | [VECTOR_RETRIEVAL_IMPLEMENTATION.md](../VECTOR_RETRIEVAL_IMPLEMENTATION.md) |
| 语法检查 | tree-sitter 需支持 KingScript | [CODE_QUALITY_ASSESSMENT.md](../CODE_QUALITY_ASSESSMENT.md) |
| 代码规范 | 需增加金蝶编码规范规则集 | [CODE_QUALITY_ASSESSMENT.md](../CODE_QUALITY_ASSESSMENT.md) |

### 6.2 待填充内容

**等待用户提供以下技术链接后补充：**

- [ ] 金蝶云苍穹官方开发文档
- [ ] KingScript 语言规范
- [ ] 元数据定义规范
- [ ] 插件开发指南
- [ ] 扩展点清单
- [ ] 标准代码示例库

---

## 附录

### A. 金蝶术语对照表

| 金蝶术语 | 开源等价概念 | 说明 |
|---------|-------------|------|
| 苍穹 | 低代码平台 | 金蝶云原生 PaaS 平台 |
| 星瀚 | HR SaaS | 基于苍穹的 HR 产品 |
| 元数据 | 数据模型定义 | JSON/YAML 格式的结构定义 |
| 单据 | 表单 + 业务对象 | 带界面的业务数据对象 |
| KingScript | 领域特定语言 | 金蝶自研脚本语言 |
| 插件 | 扩展点实现 | 事件驱动扩展机制 |
| 业务服务 | Service | 业务逻辑封装单元 |
| 操作服务 | Operation | 原子业务操作 |
| 实体服务 | DAO/Repository | 数据访问封装 |
| 查询服务 | Query | 数据查询封装 |

### B. 参考资源

**等待补充：**

- 金蝶官方文档链接
- 开发者社区链接
- 示例代码仓库

---

*文档状态: 框架已搭建，待填充具体内容*  
*维护记录: 2026-03-06 创建初始框架*
