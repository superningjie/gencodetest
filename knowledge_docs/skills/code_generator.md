# Skill: code_generator

## 基本信息

| 属性 | 值 |
|-----|-----|
| **Skill ID** | code_generator |
| **名称** | 代码生成器 |
| **版本** | 2.0 |
| **类型** | code_generator |
| **实现类** | `core.skill_engine.CodeGeneratorSkill` |

## 功能描述

基于实现项和匹配的资产生成高质量代码，支持多种语言和框架。

## 输入

| 参数名 | 类型 | 描述 | 必填 |
|-------|------|------|------|
| implementation_items | List[ImplementationItem] | 实现项列表 | 是 |
| matched_assets | List[CodeAsset] | 匹配的资产列表 | 否 |

## 输出

| 参数名 | 类型 | 描述 |
|-------|------|------|
| generated_code | List[GeneratedCode] | 生成的代码列表 |

## 配置参数

| 参数名 | 类型 | 默认值 | 描述 |
|-------|------|--------|------|
| templates_dir | str | "skills/code_generator/prompt_templates" | 模板目录 |
| generate_tests | bool | true | 是否生成测试代码 |
| add_comments | str | "detailed" | 注释级别（none/simple/detailed） |

## 后处理器

| 处理器 | 功能 | 启用 |
|-------|------|------|
| syntax_checker | 语法检查 | ✓ |
| style_formatter | 风格格式化 | ✓ |
| import_optimizer | 导入优化 | ✓ |
| security_scanner | 安全检查 | ✓ |

## 支持的语言

| 语言 | 文件扩展名 | 模板目录 |
|-----|-----------|---------|
| Java | .java | java_api/ |
| Kotlin | .kt | kotlin_plugin/ |
| SQL | .sql | sql_report/ |
| Python | .py | python_script/ |
| YAML | .yaml | configuration/ |

## 生成流程

1. 加载代码模板
2. 准备模板上下文（变量替换）
3. 渲染模板生成原始代码
4. 执行后处理器流水线
5. 质量评估
6. 创建GeneratedCode对象

## 模板变量

### Java API模板变量
- `packageName` - 包名
- `className` - 类名
- `serviceName` - 服务名
- `basePath` - API基础路径
- `moduleName` - 模块名

### Kotlin插件模板变量
- `packageName` - 包名
- `className` - 类名
- `pluginId` - 插件ID
- `extensionPoints` - 扩展点列表

### SQL报表模板变量
- `reportName` - 报表名称
- `selectFields` - 查询字段
- `fromTables` - 表名
- `whereConditions` - 条件
- `groupByFields` - 分组字段

## 质量评估

| 指标 | 权重 | 说明 |
|-----|------|------|
| 代码长度 | 0.1 | 10-500行为佳 |
| 注释覆盖 | 0.1 | 包含注释加分 |
| 语法有效 | 0.2 | 语法正确加分 |
| 资产复用 | 0.1 | 使用资产加分 |
| 基础分 | 0.5 | 默认0.5 |

## 示例

### 输入示例
```json
{
  "implementation_items": [{
    "name": "AttendanceController",
    "impl_type": "java_api",
    "language": "java"
  }],
  "matched_assets": [{
    "id": "template_rest_controller",
    "type": "file_template"
  }]
}
```

### 输出示例
```json
{
  "generated_code": [{
    "id": "CODE_001",
    "file_path": "src/AttendanceController.java",
    "language": "java",
    "content": "package com.kingdee.hr.attendance;\n...",
    "quality_score": 0.85
  }]
}
```

## 依赖

- 前置: code_implement_splitter, asset_matcher
- 后续: code_quality_checker
