# Skill: requirement_analyzer

## 基本信息

| 属性 | 值 |
|-----|-----|
| **Skill ID** | requirement_analyzer |
| **名称** | 需求分析器 |
| **版本** | 1.1 |
| **类型** | default |
| **实现类** | `core.skill_engine.RequirementAnalyzerSkill` |

## 功能描述

解析自然语言需求，提取结构化信息，将用户输入转换为标准化的Requirement对象。

## 输入

| 参数名 | 类型 | 描述 | 必填 |
|-------|------|------|------|
| user_input | str | 用户的自然语言需求描述 | 是 |

## 输出

| 参数名 | 类型 | 描述 |
|-------|------|------|
| requirements | List[Requirement] | 解析后的需求对象列表 |

## 配置参数

| 参数名 | 类型 | 默认值 | 描述 |
|-------|------|--------|------|
| extract_keywords | bool | true | 是否提取关键词 |
| identify_module | bool | true | 是否识别所属模块 |
| generate_clarification | bool | false | 是否生成澄清问题 |

## 处理流程

1. 接收用户输入的原始需求文本
2. 使用NLP技术提取关键信息
3. 识别需求类别（功能增强/报表开发/接口开发等）
4. 识别所属HR模块（HR_CORE/HR_PAYROLL/HR_ATTENDANCE等）
5. 生成标准化的Requirement对象

## 示例

### 输入示例
```
开发一个考勤打卡插件，支持GPS位置校验
```

### 输出示例
```json
{
  "requirements": [
    {
      "id": "REQ_001",
      "name": "考勤打卡插件",
      "description": "开发一个考勤打卡插件，支持GPS位置校验",
      "category": "功能增强",
      "related_module": "HR_ATTENDANCE",
      "customization_type": "标准扩展"
    }
  ]
}
```

## 依赖

- 无前置依赖

## 后续步骤

通常后续接 `code_implement_splitter` 进行需求拆分
