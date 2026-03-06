# 金蝶星瀚HR AI顾问系统 V2.0

## 系统概述

基于V1.1架构增强的**定制化开发代码智能生成系统**，实现从需求到代码的端到端自动化。

### 核心能力

1. **智能需求拆分** - 将自然语言需求拆分为结构化实现项
2. **资产智能匹配** - 自动匹配可复用代码资产
3. **代码自动生成** - 基于模板和知识库生成高质量代码
4. **质量自动评估** - 语法检查、风格检查、安全检查

## 快速开始

### 安装依赖

```bash
pip install -r requirements.txt
```

### 交互模式

```bash
python main.py --interactive
```

### 单条需求生成

```bash
python main.py --generate "开发一个考勤打卡插件，支持GPS位置校验"
```

### 批量处理

```bash
python main.py --batch examples/batch_requirements.json
```

## 系统架构

### 三层知识分离

```
knowledge/
├── hr_products/          # 业务知识层（HR产品模块）
├── dev_guidelines/       # 技术规范层（开发标准）
└── reusable_assets/      # 资产实例层（代码片段、模板）
```

### Pipeline执行流程

```
需求解析 → 实现拆分 → 资产匹配 → 代码生成 → 质量检查 → 输出组装
```

## 项目结构

```
kingdee_ai_consultant_v2/
├── core/                          # 核心引擎
│   ├── data_models.py            # 标准化数据模型
│   ├── skill_engine.py           # 插件化Skill引擎
│   ├── pipeline_executor.py      # Pipeline执行器
│   └── knowledge_base.py         # 知识库管理
├── skills/                        # Skill模块
│   ├── requirement_analyzer/     # 需求分析
│   ├── code_implement_splitter/  # 实现拆分 ⭐
│   ├── asset_matcher/            # 资产匹配 ⭐
│   ├── code_generator/           # 代码生成 ⭐
│   └── ...
├── knowledge/                     # 知识库
│   ├── hr_products/              # 业务知识
│   ├── dev_guidelines/           # 技术规范
│   └── reusable_assets/          # 可复用资产
├── pipeline.yaml                  # Pipeline配置
├── config.yaml                    # 主配置
└── main.py                        # 入口程序
```

## 扩展开发

### 添加新的代码模板

1. 在 `knowledge/reusable_assets/templates/` 创建模板文件
2. 在 `asset_index.yaml` 注册模板
3. 重启系统即可使用

### 添加新的拆分规则

编辑 `skills/code_implement_splitter/splitter_rules.yaml`

## 版本历史

- **V2.0** (2024-03-10) - 新增代码生成功能，三层知识分离
- **V1.1** (2024-03-06) - Pipeline配置化，插件注册中心
- **V1.0** (2024-03-XX) - 基础架构搭建

## License

MIT License
