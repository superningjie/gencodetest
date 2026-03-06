# Pipeline配置: customization_code_generation

## 概述

金蝶星瀚HR定制化开发代码生成Pipeline，实现从需求到代码的端到端自动化。

## 基本信息

| 属性 | 值 |
|-----|-----|
| **Pipeline名称** | customization_code_generation |
| **版本** | 2.0.0 |
| **描述** | 金蝶星瀚HR定制化开发代码生成Pipeline |

## 执行流程图

```
┌─────────────────┐
│ 1. 需求解析      │ requirement_analyzer
│   (parse_requirement)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 2. 实现拆分      │ code_implement_splitter
│   (split_implementation)
└────────┬────────┘
         │
         ├──────────────────┐
         │                  │
         ▼                  ▼
┌─────────────────┐  ┌─────────────────┐
│ 3. 资产匹配      │  │ 4. 代码生成      │
│   (match_assets) │  │   (generate_code)│
│   [并行执行]      │  │   [依赖: 3]      │
└────────┬────────┘  └────────┬────────┘
         │                    │
         └────────────────────┘
                            │
                            ▼
                  ┌─────────────────┐
                  │ 5. 质量检查      │
                  │   (quality_check)│
                  └────────┬────────┘
                            │
                            ▼
                  ┌─────────────────┐
                  │ 6. 工作量评估    │
                  │   (estimate_effort)
                  └────────┬────────┘
                            │
                            ▼
                  ┌─────────────────┐
                  │ 7. 风险评估      │
                  │   (assess_risks) │
                  └────────┬────────┘
                            │
                            ▼
                  ┌─────────────────┐
                  │ 8. 输出组装      │
                  │   (assemble_output)
                  └─────────────────┘
```

## 步骤详情

### Step 1: 需求解析

| 属性 | 值 |
|-----|-----|
| **ID** | parse_requirement |
| **Skill** | requirement_analyzer |
| **输出Key** | parsed_requirement |

**配置参数:**
- extract_keywords: true
- identify_module: true

**输入:** 用户原始需求文本
**输出:** 标准化的Requirement对象列表

---

### Step 2: 实现拆分

| 属性 | 值 |
|-----|-----|
| **ID** | split_implementation |
| **Skill** | code_implement_splitter |
| **输入** | {{parse_requirement}} |
| **输出Key** | implement_items |

**配置参数:**
- rules_file: "splitter_rules.yaml"
- split_granularity: "function_level"
- max_items_per_batch: 5

**功能:** 将需求拆分为具体的实现项（API/插件/报表等）

---

### Step 3: 资产匹配

| 属性 | 值 |
|-----|-----|
| **ID** | match_assets |
| **Skill** | asset_matcher |
| **输入** | {{split_implementation}} |
| **输出Key** | matched_assets |
| **并行** | 是 |

**配置参数:**
- asset_index: "knowledge/reusable_assets/asset_index.yaml"
- match_threshold: 3.0
- max_assets_per_item: 3

**功能:** 为实现项匹配可复用的代码资产

---

### Step 4: 代码生成

| 属性 | 值 |
|-----|-----|
| **ID** | generate_code |
| **Skill** | code_generator |
| **输入** | {{split_implementation}} |
| **输出Key** | generated_code |
| **依赖** | match_assets |

**配置参数:**
- templates_dir: "skills/code_generator/prompt_templates"
- generate_tests: true
- add_comments: "detailed"
- post_processors:
  - syntax_checker
  - style_formatter

**功能:** 基于实现项和资产生成代码

---

### Step 5: 质量检查

| 属性 | 值 |
|-----|-----|
| **ID** | quality_check |
| **Skill** | code_quality_checker |
| **输入** | {{generate_code}} |
| **输出Key** | quality_report |

**配置参数:**
- min_quality_score: 0.7
- check_syntax: true
- check_security: true

**功能:** 检查生成代码的质量

---

### Step 6: 工作量评估

| 属性 | 值 |
|-----|-----|
| **ID** | estimate_effort |
| **Skill** | effort_estimator |
| **输入** | {{split_implementation}} |
| **输出Key** | estimation |

**配置参数:**
- unit_price: 3000

**功能:** 评估开发工作量和成本

---

### Step 7: 风险评估

| 属性 | 值 |
|-----|-----|
| **ID** | assess_risks |
| **Skill** | risk_assessor |
| **输入** | {{split_implementation}} |
| **输出Key** | risks |

**配置参数:**
- risk_levels: ["高", "中", "低"]

**功能:** 识别项目风险

---

### Step 8: 输出组装

| 属性 | 值 |
|-----|-----|
| **ID** | assemble_output |
| **Skill** | output_assembler |
| **输入** | 多输入（见下方） |
| **输出Key** | final_deliverable |

**输入配置:**
```json
{
  "code": "{{generate_code}}",
  "quality": "{{quality_check}}",
  "estimation": "{{estimate_effort}}",
  "risks": "{{assess_risks}}",
  "metadata": "{{parse_requirement}}"
}
```

**配置参数:**
- output_formats: ["code", "markdown", "json"]

**功能:** 组装最终输出结果

## 数据流

### 核心数据对象

1. **Requirement** - 需求对象
2. **ImplementationItem** - 实现项
3. **CodeAsset** - 代码资产
4. **GeneratedCode** - 生成的代码

### 数据传递

```
Requirement[] → ImplementationItem[] → CodeAsset[] → GeneratedCode[]
     ↓                ↓                    ↓              ↓
  需求解析         实现拆分            资产匹配        代码生成
```

## 执行特性

### 并行执行
- Step 3 (match_assets) 支持并行执行
- 提高处理效率

### 依赖管理
- Step 4 (generate_code) 依赖 Step 3 (match_assets)
- 确保资产匹配完成后再生成代码

### 条件执行
- 支持基于步骤状态的条件执行
- 示例: `step_id.status == 'completed'`

## 错误处理

| 错误类型 | 处理策略 |
|---------|---------|
| Skill未找到 | 记录错误，跳过该步骤 |
| 执行失败 | 重试（可配置重试次数） |
| 超时 | 超时时间300秒 |

## 扩展点

### 添加新步骤

在Pipeline中添加新步骤:

```markdown
### Step N: 新步骤

| 属性 | 值 |
|-----|-----|
| **ID** | new_step_id |
| **Skill** | skill_name |
| **输入** | {{previous_step}} |
| **输出Key** | new_output |
| **依赖** | dependency_step |
```

### 修改配置

编辑对应步骤的配置参数表即可。

## 版本历史

| 版本 | 日期 | 变更 |
|-----|------|------|
| 2.0.0 | 2024-03-10 | 初始版本，支持代码生成全流程 |
