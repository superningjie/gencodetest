# 金蝶云苍穹开发技术知识库（完整版）

> **版本**: V2.0  
> **来源**: 金蝶云社区开发平台文档（264篇）  
> **更新日期**: 2026-03-06

---

## 目录

1. [核心概念与架构](#1-核心概念与架构)
2. [KingScript 开发指南](#2-kingscript-开发指南)
3. [插件开发体系](#3-插件开发体系)
4. [页面与控件开发](#4-页面与控件开发)
5. [定制开发规范](#5-定制开发规范)
6. [代码生成应用](#6-代码生成应用)

---

## 1. 核心概念与架构

### 1.1 金蝶苍穹技术栈

```
金蝶云苍穹平台架构
├── 元数据驱动层
│   ├── 实体元数据（Entity Metadata）
│   ├── 表单元数据（Form Metadata）
│   └── 服务元数据（Service Metadata）
│
├── 开发层
│   ├── KingScript（脚本开发）
│   ├── Java插件开发
│   └── 自定义控件开发
│
├── 运行时层
│   ├── 苍穹ORM（kdorm）
│   ├── 插件引擎
│   └── 事件总线
│
└── 集成层
    ├── OpenAPI
    ├── 单据转换（BOTP）
    └── 微服务架构
```

### 1.2 与开源技术栈对比

| 维度 | 开源 Spring 生态 | 金蝶云苍穹 |
|------|-----------------|-----------|
| **开发模式** | 代码优先 | 元数据驱动 + 代码扩展 |
| **后端语言** | Java/Kotlin | KingScript / Java 插件 |
| **服务层** | Spring Service | 业务服务 + 操作服务 |
| **数据访问** | JPA/MyBatis | 实体元数据 + 查询服务 |
| **接口层** | REST Controller | 开放 API + 业务服务 |
| **扩展机制** | AOP / 继承 | 插件 + 扩展点 |

### 1.3 核心术语映射

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

---

## 2. KingScript 开发指南

### 2.1 KingScript 语言特性

KingScript 是金蝶自研的动态类型脚本语言，专为苍穹平台业务逻辑开发设计。

#### 核心特性

| 特性 | 说明 | 示例 |
|------|------|------|
| **动态类型** | 变量类型在运行时确定 | `var count = 10; var name = "test";` |
| **类Java语法** | 类似Java的语法结构 | 条件、循环、方法定义 |
| **元数据访问** | 直接访问苍穹元数据 | `entityService.save("entityId", data);` |
| **服务调用** | 便捷的服务调用能力 | `queryService.query("entityId", condition);` |

#### 基础语法示例

```kingscript
// 变量声明
var count = 10;
var name = "苍穹开发";
var isValid = true;
var items = ["item1", "item2", "item3"];

// 条件判断
if (count > 0) {
    return "有数据";
} else if (count == 0) {
    return "无数据";
} else {
    throw BusinessException("数据异常");
}

// 循环
for (var i = 0; i < items.size(); i++) {
    var item = items[i];
    // 处理逻辑
}

// 方法定义
function calculateTotal(price, quantity) {
    return price * quantity;
}

// 类定义
class OrderService {
    function createOrder(orderData) {
        // 业务逻辑
        return entityService.save("orderEntity", orderData);
    }
}
```

### 2.2 KingScript 服务开发

#### 业务服务定义

```kingscript
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
        var updateCount = entityService.update(
            "kingdee.hr.person",
            "id in (" + personIds.join(",") + ")",
            {"department": newDeptId}
        );
        
        return updateCount;
    }
}
```

#### 常用服务 API

| 服务 | 方法 | 说明 |
|------|------|------|
| **entityService** | `save(entityId, data)` | 保存实体数据 |
| **entityService** | `update(entityId, condition, data)` | 更新实体数据 |
| **entityService** | `delete(entityId, condition)` | 删除实体数据 |
| **queryService** | `query(entityId, condition, fields, pageInfo)` | 查询数据 |
| **queryService** | `loadSingle(entityId, condition)` | 加载单条数据 |
| **workflowService** | `startInstance(processId, bizKey)` | 启动工作流 |

### 2.3 插件开发（KingScript）

```kingscript
/**
 * 表单插件示例
 */
plugin FormSamplePlugin extends FormPlugin {
    
    /**
     * 表单加载后事件
     */
    onAfterLoad() {
        var model = this.getModel();
        // 设置默认值
        model.setValue("status", "草稿");
    }
    
    /**
     * 字段值变更事件
     */
    onFieldChange(fieldName, newValue, oldValue) {
        if (fieldName == "department") {
            // 清空关联字段
            this.getModel().setValue("position", null);
        }
    }
    
    /**
     * 保存前校验
     */
    onBeforeSave() {
        var model = this.getModel();
        var name = model.getValue("name");
        
        if (name == null || name == "") {
            this.getView().showMessage("姓名不能为空");
            return false; // 阻止保存
        }
        
        return true;
    }
}
```

---

## 3. 插件开发体系

### 3.1 插件类型总览

```
金蝶苍穹插件体系
├── 表单插件
│   ├── 动态表单插件 (AbstractFormPlugin)
│   ├── 单据界面插件 (AbstractBillPlugin)
│   ├── 基础资料界面插件 (AbstractBaseDataPlugin)
│   └── 移动端表单插件 (AbstractMobileFormPlugin)
│
├── 列表插件
│   ├── 标准单据列表插件 (AbstractListPlugin)
│   ├── 左树右表列表插件 (AbstractTreeListPlugin)
│   ├── 树形基础资料插件
│   └── 移动端列表插件
│
├── 操作插件
│   └── 单据操作插件 (AbstractOperationPlugin)
│
├── 转换插件
│   └── 单据转换插件 (AbstractTransformPlugin)
│
├── 反写插件
│   └── 单据反写插件 (AbstractWriteBackPlugin)
│
├── 报表插件
│   ├── 报表取数插件 (AbstractReportDataPlugin)
│   └── 报表界面插件 (AbstractReportViewPlugin)
│
├── 工作流插件
│   └── 工作流插件 (AbstractWorkflowPlugin)
│
├── 打印插件
│   └── 打印插件 (AbstractPrintPlugin)
│
├── 开放API插件
│   └── 开放API插件 (AbstractOpenAPIPlugin)
│
└── 后台任务插件
    └── 后台任务插件 (AbstractBackgroundTaskPlugin)
```

### 3.2 Java 插件基类详解

#### 动态表单插件基类

```java
package kd.bos.form.plugin;

public class AbstractFormPlugin extends AbstractDataModelPlugin 
    implements IFormPlugin {
    
    // 核心方法
    public IFormView getView();           // 获取视图模型
    public IDataModel getModel();         // 获取数据模型
    public PageCache getPageCache();      // 获取页面缓存
    public Control getControl(String key); // 获取控件
    
    // 事件注册
    public void addClickListeners(String... keys);      // 注册按钮点击监听
    public void addItemClickListeners(String... keys);  // 注册菜单项点击监听
}
```

#### 关键事件说明

| 事件 | 触发时机 | 常用场景 |
|------|---------|---------|
| `setPluginName` | 设置插件名称 | 插件初始化 |
| `preOpenForm` | 打开表单前 | 权限校验 |
| `createNewData` | 创建新数据 | 设置默认值 |
| `afterCreateNewData` | 创建数据后 | 关联数据处理 |
| `beforeBindData` | 绑定数据前 | 数据转换 |
| `afterBindData` | 绑定数据后 | 界面调整 |
| `beforeDoOperation` | 操作执行前 | 校验逻辑 |
| `afterDoOperation` | 操作执行后 | 后续处理 |
| `propertyChanged` | 属性值变更 | 联动处理 |

### 3.3 Java 插件代码模板

#### 动态表单插件

```java
package com.kingdee.hr.plugins;

import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.IFormView;
import kd.bos.form.control.events.BeforeDoOperationArgs;

/**
 * ${FormName} 表单插件
 * @description ${Description}
 */
public class ${ClassName}FormPlugin extends AbstractFormPlugin {
    
    @Override
    public void afterDoOperation(AfterDoOperationArgs e) {
        super.afterDoOperation(e);
        
        String operationKey = e.getOperationKey();
        if ("save".equals(operationKey)) {
            // 保存后的业务逻辑
        }
    }
    
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        
        String fieldName = e.getProperty().getName();
        if ("${FieldName}".equals(fieldName)) {
            // 字段值变更处理
            Object newValue = e.getNewValue();
            // 联动逻辑
        }
    }
    
    @Override
    public void beforeDoOperation(BeforeDoOperationArgs e) {
        super.beforeDoOperation(e);
        
        String operationKey = e.getOperationKey();
        if ("save".equals(operationKey)) {
            // 保存前校验
            IDataModel model = this.getModel();
            // 校验逻辑
        }
    }
}
```

#### 单据操作插件

```java
package com.kingdee.hr.plugins;

import kd.bos.entity.operate.AbstractOperationService;
import kd.bos.entity.operate.OperationContext;

/**
 * ${OperationName} 操作插件
 */
public class ${ClassName}OperationPlugin extends AbstractOperationService {
    
    @Override
    public void onPreparePropertys(OperationContext context) {
        super.onPreparePropertys(context);
        // 准备需要的字段
    }
    
    @Override
    public void onAddValidators(OperationContext context) {
        super.onAddValidators(context);
        // 添加校验器
    }
    
    @Override
    public void beforeExecuteOperationTransaction(OperationContext context) {
        super.beforeExecuteOperationTransaction(context);
        // 事务执行前逻辑
    }
    
    @Override
    public void afterExecuteOperationTransaction(OperationContext context) {
        super.afterExecuteOperationTransaction(context);
        // 事务执行后逻辑
    }
}
```

### 3.4 插件注册

插件开发完成后，需要在苍穹设计器中注册：

1. 打开【表单设计器】
2. 选择【插件】属性
3. 点击【注册】按钮
4. 填写插件类名（完整包路径）
5. 保存元数据

---

## 4. 页面与控件开发

### 4.1 页面类型

| 页面类型 | 用途 | 开发方式 |
|---------|------|---------|
| **单据** | 业务数据录入 | 设计器 + 插件 |
| **列表** | 数据查询展示 | 设计器 + 插件 |
| **基础资料** | 基础数据维护 | 设计器 + 插件 |
| **报表** | 数据统计分析 | 设计器 + 取数插件 |
| **动态表单** | 灵活界面布局 | 设计器 + 插件 |
| **移动页面** | 移动端界面 | 设计器 + 插件 |

### 4.2 常用控件

#### 通用字段控件

| 控件 | 类型 | 用途 |
|------|------|------|
| 文本字段 | String | 短文本输入 |
| 多行文本 | String | 长文本输入 |
| 整数 | Integer | 整数值 |
| 小数 | BigDecimal | 小数值 |
| 日期 | Date | 日期选择 |
| 长日期 | DateTime | 日期时间选择 |
| 下拉列表 | Enum | 枚举选择 |
| 复选框 | Boolean | 布尔值 |

#### 业务字段控件

| 控件 | 用途 | 特性 |
|------|------|------|
| 基础资料 | 关联基础数据 | F7选择、模糊查询 |
| 单据字段 | 关联业务单据 | 支持单据转换 |
| 用户字段 | 选择用户 | 关联用户体系 |
| 组织字段 | 选择组织 | 关联组织架构 |
| 弹性域 | 动态字段 | 可配置维度 |

### 4.3 布局与样式

#### Flex 布局

苍穹前端采用 Flex 布局系统：

```
容器属性：
- flex-direction: row | column
- justify-content: flex-start | center | flex-end | space-between
- align-items: flex-start | center | flex-end | stretch

子项属性：
- flex: 1 (占据剩余空间)
- flex-grow: 1 (扩展比例)
- flex-shrink: 0 (收缩比例)
```

---

## 5. 定制开发规范

### 5.1 命名规范

#### Java 命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 包名 | 全小写，反向域名 | `com.kingdee.hr.plugins` |
| 类名 | 大驼峰 | `PersonEntryPlugin` |
| 方法名 | 小驼峰 | `validateEntryData` |
| 常量 | 全大写下划线 | `MAX_RETRY_COUNT` |

#### KingScript 命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 服务名 | 大驼峰 | `PersonService` |
| 方法名 | 小驼峰 | `calculateSalary` |
| 变量名 | 小驼峰 | `entryDate` |

### 5.2 多语言开发规范

#### 程序提示语

```java
// 正确：使用多语言key
this.getView().showMessage(getResourceRepository().getLocalizedString("提示保存成功"));

// 错误：硬编码中文
this.getView().showMessage("保存成功");
```

#### 控件多语言

- 文本控件 → 使用多语言文本控件
- 缺省值 → 通过代码设置多语言key
- 格式化 → 使用国际产品部提供的公共服务类

### 5.3 性能规范

| 规范项 | 要求 |
|--------|------|
| 查询优化 | 避免全表扫描，使用索引字段过滤 |
| 分页处理 | 列表查询必须分页 |
| 批量操作 | 批量更新使用 entityService.update |
| 缓存使用 | 合理使用页面缓存和系统缓存 |
| 事务控制 | 长事务需拆分为多个短事务 |

---

## 6. 代码生成应用

### 6.1 实现类型映射

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
    JAVA_OPERATION_PLUGIN = "java_operation_plugin" # Java 操作插件
    JAVA_TRANSFORM_PLUGIN = "java_transform_plugin" # Java 转换插件
    
    # 扩展点
    JAVA_EXTENSION_POINT = "java_extension_point"      # 扩展点定义
    JAVA_EXTENSION_IMPL = "java_extension_impl"        # 扩展实现
```

### 6.2 技术栈推断规则

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
      
    # 操作插件
    - pattern: "操作|保存|提交|审核"
      stack: "kingdee_operation"
      impl_type: "java_operation_plugin"
      language: "java"
      
    # 转换插件
    - pattern: "转换|BOTP|单据转换"
      stack: "kingdee_transform"
      impl_type: "java_transform_plugin"
      language: "java"
```

### 6.3 包名规范

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
        KingdeeImplementationType.JAVA_OPERATION_PLUGIN: '.plugin.operation',
        KingdeeImplementationType.JAVA_TRANSFORM_PLUGIN: '.plugin.transform',
        KingdeeImplementationType.JAVA_EXTENSION_POINT: '.extension',
        KingdeeImplementationType.JAVA_EXTENSION_IMPL: '.extension.impl',
    }
    
    suffix = type_suffix.get(impl_type, '')
    return base + suffix
```

### 6.4 Prompt 工程模板

```python
KINGDEE_SYSTEM_PROMPT = """你是金蝶云苍穹平台的资深开发工程师。

## 核心能力
- 精通 KingScript 脚本开发
- 熟悉金蝶苍穹插件开发体系
- 掌握元数据驱动开发模式
- 了解苍穹ORM和查询服务

## 编码原则
1. 代码必须完整、可运行
2. 遵循金蝶开发规范和命名约定
3. 支持多语言和国际化
4. 合理使用缓存和性能优化

## 技术栈
- 后端: KingScript / Java 插件
- 数据访问: 苍穹ORM (QueryService, EntityService)
- 前端: 苍穹Design/移动Design
- 集成: OpenAPI / BOTP

## 输出要求
- 只输出代码本身
- 代码前后用 ``` 包裹
- 标注代码用途和关键逻辑
"""
```

---

## 附录

### A. 参考资源

- 金蝶云社区: https://vip.kingdee.com
- 开发平台专题: https://vip.kingdee.com/knowledge/specialDetail/218022218066869248
- 定制开发平台: https://vip.kingdee.com/knowledge/specialDetail/773127159950388736

### B. 文档清单

本文档基于以下金蝶官方文档整理：
- KingScript 快速入门
- 动态表单插件基类
- 单据操作插件基类
- 单据转换插件基类
- 金蝶云·星瀚定制化开发规范
- 开发平台核心术语
- 以及其他 264 篇技术文档

---

*维护记录：*
- 2026-03-06: 基于264篇金蝶官方文档生成完整知识库
