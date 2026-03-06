# Skill: code_implement_splitter

## 基本信息

| 属性 | 值 |
|-----|-----|
| **Skill ID** | code_implement_splitter |
| **名称** | 开发实现拆分器 |
| **版本** | 2.0 |
| **类型** | implement_splitter |
| **实现类** | `core.skill_engine.ImplementSplitterSkill` |

## 功能描述

将需求拆分为具体的、可实现的开发项，确定技术栈、实现类型和依赖关系。

## 输入

| 参数名 | 类型 | 描述 | 必填 |
|-------|------|------|------|
| requirements | List[Requirement] | 需求对象列表 | 是 |

## 输出

| 参数名 | 类型 | 描述 |
|-------|------|------|
| implementation_items | List[ImplementationItem] | 实现项列表 |

## 配置参数

| 参数名 | 类型 | 默认值 | 描述 |
|-------|------|--------|------|
| rules_file | str | "splitter_rules.yaml" | 拆分规则文件路径 |
| split_granularity | str | "function_level" | 拆分粒度 |
| max_items_per_batch | int | 5 | 每批最大项数 |

## 拆分规则

### 技术栈推断规则

| 关键词模式 | 技术栈 | 语言 | 实现类型 |
|-----------|--------|------|---------|
| 报表、统计、查询、数据 | sql_report | SQL | SQL报表 |
| 接口、API、服务、controller | java_api | Java | Java API |
| 插件、扩展、自定义、plugin | kotlin_plugin | Kotlin | Kotlin插件 |
| 脚本、自动化、定时任务 | python_script | Python | Python脚本 |
| 配置、参数、规则 | configuration | YAML | 配置 |

### 模块识别规则

| 关键词 | 模块 |
|-------|------|
| 考勤、打卡、排班、假期 | HR_ATTENDANCE |
| 薪酬、工资、社保、公积金 | HR_PAYROLL |
| 组织、员工、合同、档案 | HR_CORE |
| 招聘、面试、入职、简历 | HR_RECRUITMENT |

## 处理流程

1. 读取需求列表
2. 根据需求描述识别技术栈
3. 根据关键词识别所属模块
4. 根据需求类型确定拆分策略
5. 生成ImplementationItem对象列表

## 拆分策略

### API开发策略
拆分为三层：
- Controller层（REST接口）
- Service层（业务逻辑）
- Repository层（数据访问）

### 插件开发策略
生成单一插件项：
- 插件主类
- 扩展点配置
- 业务逻辑实现

### 报表开发策略
生成SQL报表项：
- 查询SQL
- 参数配置
- 导出逻辑

## 示例

### 输入示例
```json
{
  "requirements": [{
    "description": "开发一个考勤打卡插件，支持GPS位置校验"
  }]
}
```

### 输出示例
```json
{
  "implementation_items": [
    {
      "id": "IMPL_001",
      "name": "考勤打卡插件",
      "impl_type": "kotlin_plugin",
      "language": "kotlin",
      "tech_stack": "金蝶插件框架 5.0",
      "extension_point": "check_in_validator"
    }
  ]
}
```

## 依赖

- 前置: requirement_analyzer
- 后续: asset_matcher, code_generator
