# Skill: asset_matcher

## 基本信息

| 属性 | 值 |
|-----|-----|
| **Skill ID** | asset_matcher |
| **名称** | 可复用资产匹配器 |
| **版本** | 2.0 |
| **类型** | asset_matcher |
| **实现类** | `core.skill_engine.AssetMatcherSkill` |

## 功能描述

为实现项匹配可复用的代码资产，提高代码复用率和开发效率。

## 输入

| 参数名 | 类型 | 描述 | 必填 |
|-------|------|------|------|
| implementation_items | List[ImplementationItem] | 实现项列表 | 是 |

## 输出

| 参数名 | 类型 | 描述 |
|-------|------|------|
| matched_assets | List[CodeAsset] | 匹配的资产列表 |

## 配置参数

| 参数名 | 类型 | 默认值 | 描述 |
|-------|------|--------|------|
| asset_index | str | "knowledge/reusable_assets/asset_index.yaml" | 资产索引文件路径 |
| match_threshold | float | 3.0 | 匹配阈值 |
| max_assets_per_item | int | 3 | 每项最大匹配资产数 |

## 匹配策略

### 1. 精确匹配
- 语言完全匹配
- 使用场景完全匹配
- 优先级: 1
- 权重: 1.0

### 2. 标签相似度
- 标签重叠度计算
- 优先级: 2
- 权重: 0.8

### 3. 上下文匹配
- 基于使用场景匹配
- 优先级: 3
- 权重: 0.6

## 匹配算法

```
score = 0

# 语言匹配
if asset.language == item.language:
    score += 2

# 标签匹配
item_tags = set(item.name.lower().split('_'))
asset_tags = set(tag.lower() for tag in asset.tags)
tag_overlap = len(item_tags & asset_tags)
score += tag_overlap * 2

# 使用场景匹配
if item.impl_type in asset.usage_context:
    score += 3

# 描述关键词匹配
for keyword in item.description.lower().split():
    if keyword in asset.description.lower():
        score += 1

if score >= threshold:
    matched_assets.append(asset)
```

## 资产类型

| 类型 | 描述 | 示例 |
|-----|------|------|
| code_snippet | 代码片段 | 参数校验器、工具函数 |
| file_template | 文件模板 | Controller模板、Service模板 |
| design_pattern | 设计模式 | 策略模式、工厂模式 |

## 示例

### 输入示例
```json
{
  "implementation_items": [{
    "name": "AttendanceController",
    "impl_type": "java_api",
    "language": "java"
  }]
}
```

### 输出示例
```json
{
  "matched_assets": [
    {
      "id": "template_rest_controller",
      "name": "REST Controller模板",
      "type": "file_template",
      "relevance_score": 0.95
    }
  ]
}
```

## 依赖

- 前置: code_implement_splitter
- 后续: code_generator
