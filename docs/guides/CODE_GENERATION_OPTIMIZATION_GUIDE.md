# 金蝶星瀚HR AI顾问系统 - 代码生成优化指引

> **版本**: V2.0  
> **更新日期**: 2026-03-06  
> **适用范围**: 代码生成模块 (code_generator Skill)

---

## 目录

1. [现状诊断](#1-现状诊断)
2. [优化路线图](#2-优化路线图)
3. [核心优化方向](#3-核心优化方向)
4. [实施 checklist](#4-实施-checklist)

---

## 1. 现状诊断

### 1.1 当前架构评估

```
┌─────────────────────────────────────────────────────────────┐
│                    CodeGeneratorSkill                        │
├─────────────────────────────────────────────────────────────┤
│  1. _load_template()     →  基于 impl_type 的硬编码映射      │
│  2. _prepare_context()   →  简单的字段填充                    │
│  3. _render_template()   →  字符串替换（非 Jinja2）          │
│  4. _post_process()      →  基础语法检查                      │
│  5. _assess_quality()    →  基于启发式的评分                  │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 已知问题清单

| 问题 | 严重程度 | 影响 | 当前状态 |
|------|---------|------|---------|
| 无真实 LLM 集成 | 🔴 P0 | 生成代码为模板填充，无智能 | 待接入 |
| 模板引擎简陋 | 🔴 P0 | 字符串替换，无法处理复杂逻辑 | 待升级 Jinja2 |
| 资产匹配基于关键词 | 🟡 P1 | 匹配精度低（~75%） | 需引入向量检索 |
| 无真实语法检查 | 🟡 P1 | 使用简单字符串匹配 | 需引入 tree-sitter |
| 缺少 Few-shot 示例 | 🟡 P1 | LLM 输出质量不稳定 | 需构建示例库 |
| 无反馈闭环 | 🟢 P2 | 无法持续学习优化 | 需设计反馈机制 |

### 1.3 性能基准

| 指标 | 当前值 | 目标值 | 差距 |
|------|-------|-------|------|
| 代码编译通过率 | ~90% | >95% | +5% |
| 平均生成时间 | ~5s | <3s | -40% |
| 资产匹配准确率 | ~75% | >85% | +10% |
| 端到端成功率 | ~80% | >90% | +10% |

---

## 2. 优化路线图

### Phase 1: 基础能力补强（1-2 周）

```
优先级: P0
目标: 让系统能真正生成可用代码
```

- [ ] **2.1 接入真实 LLM**
  - OpenAI GPT-4 / Claude 3 接入
  - Prompt 工程优化
  - 流式响应支持

- [ ] **2.2 升级模板引擎**
  - 迁移到 Jinja2
  - 模板继承与宏定义
  - 自定义过滤器

- [ ] **2.3 增强语法检查**
  - 接入 tree-sitter
  - 多语言支持（Java/Kotlin/SQL）
  - 错误定位与修复建议

### Phase 2: 智能增强（2-4 周）

```
优先级: P1
目标: 提升代码质量和匹配精度
```

- [ ] **2.4 向量检索资产匹配**
  - Embedding 模型选型
  - 向量数据库（Chroma/Pinecone）
  - 语义相似度计算

- [ ] **2.5 Few-shot 学习**
  - 优质代码示例库构建
  - 动态示例选择
  - 示例效果评估

- [ ] **2.6 需求理解增强**
  - 实体抽取优化
  - 意图识别细化
  - 多轮对话支持

### Phase 3: 工程化完善（持续）

```
优先级: P2
目标: 系统稳定运行，持续进化
```

- [ ] **2.7 反馈闭环**
  - 用户评分收集
  - 自动质量评估
  - 模型微调 pipeline

- [ ] **2.8 A/B 测试框架**
  - 多版本 Prompt 对比
  - 生成策略实验
  - 数据驱动决策

- [ ] **2.9 监控与告警**
  - 生成质量指标看板
  - 异常检测
  - 自动降级机制

---

## 3. 核心优化方向

### 3.1 LLM 接入与 Prompt 工程

#### 3.1.1 推荐模型选型

| 场景 | 推荐模型 | 理由 | 成本估算 |
|------|---------|------|---------|
| 代码生成 | GPT-4 / Claude 3 Opus | 代码理解能力最强 | $0.03-0.06/1K tokens |
| 代码审查 | GPT-3.5-turbo / Claude 3 Sonnet | 性价比最优 | $0.001-0.003/1K tokens |
| 需求分析 | GPT-4-mini / Claude 3 Haiku | 快速响应 | $0.0001-0.00025/1K tokens |

#### 3.1.2 Prompt 结构模板

```python
# 推荐的分层 Prompt 结构
SYSTEM_PROMPT = """你是金蝶星瀚HR系统的资深开发工程师。
精通 Java/Kotlin 插件开发、SpringBoot API 开发、SQL 报表优化。
严格遵循金蝶开发规范，生成可直接编译运行的代码。
"""

CONTEXT_PROMPT = """
## 业务上下文
{hr_module_context}

## 技术规范
{coding_guidelines}

## 可复用资产
{reusable_assets}
"""

TASK_PROMPT = """
## 开发任务
{implementation_description}

## 技术要求
- 语言: {language}
- 框架: {framework}
- 扩展点: {extension_point}

## 输出要求
1. 完整的、可直接编译的代码
2. 包含必要的 import 语句
3. 添加关键逻辑的注释
4. 遵循金蝶命名规范
"""

FEW_SHOT_EXAMPLES = """
## 参考示例
{selected_examples}
"""
```

#### 3.1.3 动态示例选择策略

```python
class FewShotExampleSelector:
    """基于语义相似度的示例选择器"""
    
    def select_examples(self, requirement: str, k: int = 3) -> List[str]:
        # 1. 计算需求与示例库中各示例的语义相似度
        # 2. 选择 Top-k 最相似的示例
        # 3. 确保示例多样性（不同类型、不同复杂度）
        pass
```

### 3.2 模板引擎升级

#### 3.2.1 Jinja2 模板结构

```
knowledge/reusable_assets/templates_v2/
├── base/
│   └── base_class.java.j2          # 基础类模板
├── java_api/
│   ├── controller.java.j2          # Controller 模板
│   ├── service.java.j2             # Service 模板
│   ├── service_impl.java.j2        # ServiceImpl 模板
│   └── repository.java.j2          # Repository 模板
├── kotlin_plugin/
│   ├── plugin.kt.j2                # 插件主类
│   ├── validator.kt.j2             # 校验器
│   └── listener.kt.j2              # 监听器
└── sql_report/
    ├── simple_query.sql.j2         # 简单查询
    └── complex_report.sql.j2       # 复杂报表
```

#### 3.2.2 模板示例

```jinja2
{# controller.java.j2 #}
package {{ package_name }};

import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

{% for import in additional_imports %}
import {{ import }};
{% endfor %}

/**
 * {{ description }}
 * 
 * @author AI-Generated
 * @since {{ generation_date }}
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("{{ base_path }}")
@Validated
public class {{ class_name }}Controller {

    private final {{ service_name }} {{ service_name|lower_first }};

{% for method in methods %}
    /**
     * {{ method.description }}
     */
    @{{ method.http_method }}("{{ method.path }}")
    public Result<{{ method.return_type }}> {{ method.name }}(
        {% for param in method.params %}
        {% if param.is_path %}@PathVariable{% else %}@RequestParam{% endif %} {{ param.type }} {{ param.name }}{% if not loop.last %},{% endif %}
        {% endfor %}
    ) {
        {% if method.return_type != 'void' %}
        return Result.success({{ service_name|lower_first }}.{{ method.name }}({{ method.params|map(attribute='name')|join(', ') }}));
        {% else %}
        {{ service_name|lower_first }}.{{ method.name }}({{ method.params|map(attribute='name')|join(', ') }});
        return Result.success();
        {% endif %}
    }
{% endfor %}
}
```

#### 3.2.3 自定义 Jinja2 过滤器

```python
def lower_first(s: str) -> str:
    """首字母小写"""
    return s[0].lower() + s[1:] if s else s

def camel_to_snake(s: str) -> str:
    """驼峰转下划线"""
    return re.sub(r'(?<!^)(?=[A-Z])', '_', s).lower()

def snake_to_camel(s: str) -> str:
    """下划线转驼峰"""
    return ''.join(word.capitalize() for word in s.split('_'))

# 注册到 Jinja2 环境
env = Environment(loader=FileSystemLoader('templates'))
env.filters['lower_first'] = lower_first
env.filters['camel_to_snake'] = camel_to_snake
env.filters['snake_to_camel'] = snake_to_camel
```

### 3.3 资产智能匹配

#### 3.3.1 向量检索架构

```
┌─────────────────────────────────────────────────────────┐
│                    Asset Matching                        │
├─────────────────────────────────────────────────────────┤
│  1. 文本 Embedding                                      │
│     ├─ 模型: text-embedding-3-small / BGE-large        │
│     └─ 输出: 1536/1024 维向量                           │
│                                                         │
│  2. 向量存储                                            │
│     ├─ 本地: ChromaDB (开发/测试)                       │
│     └─ 云端: Pinecone / Weaviate (生产)                 │
│                                                         │
│  3. 相似度检索                                          │
│     ├─ 余弦相似度                                       │
│     └─ Top-K + 阈值过滤                                 │
│                                                         │
│  4. 结果重排序                                          │
│     ├─ 使用频次加权                                     │
│     └─ 成功率加权                                       │
└─────────────────────────────────────────────────────────┘
```

#### 3.3.2 实现代码

```python
from typing import List, Tuple
import chromadb
from chromadb.utils import embedding_functions

class VectorAssetMatcher:
    """基于向量检索的资产匹配器"""
    
    def __init__(self, collection_name: str = "code_assets"):
        self.client = chromadb.PersistentClient(path="./chroma_db")
        self.embedding_func = embedding_functions.OpenAIEmbeddingFunction(
            api_key=os.getenv("OPENAI_API_KEY"),
            model_name="text-embedding-3-small"
        )
        self.collection = self.client.get_or_create_collection(
            name=collection_name,
            embedding_function=self.embedding_func
        )
    
    def index_asset(self, asset: CodeAsset):
        """将资产索引到向量库"""
        # 构建资产描述文本
        doc = f"{asset.name}\n{asset.description}\n标签: {', '.join(asset.tags)}"
        
        self.collection.add(
            ids=[asset.id],
            documents=[doc],
            metadatas=[{
                "name": asset.name,
                "type": asset.asset_type,
                "language": asset.language.value,
                "path": asset.file_path
            }]
        )
    
    def search(
        self, 
        query: str, 
        language: CodeLanguage = None,
        top_k: int = 5
    ) -> List[Tuple[CodeAsset, float]]:
        """语义检索资产"""
        
        where_filter = None
        if language:
            where_filter = {"language": language.value}
        
        results = self.collection.query(
            query_texts=[query],
            n_results=top_k,
            where=where_filter
        )
        
        # 转换为资产对象和相似度分数
        assets_with_scores = []
        for i, (doc, metadata, distance) in enumerate(zip(
            results['documents'][0],
            results['metadatas'][0],
            results['distances'][0]
        )):
            # 余弦距离转相似度分数
            similarity = 1 - distance
            asset = self._load_asset_from_metadata(metadata)
            assets_with_scores.append((asset, similarity))
        
        return assets_with_scores
```

### 3.4 语法检查与质量控制

#### 3.4.1 tree-sitter 集成

```python
from tree_sitter import Language, Parser
import tree_sitter_java as ts_java
import tree_sitter_kotlin as ts_kotlin

class SyntaxChecker:
    """基于 tree-sitter 的语法检查器"""
    
    PARSERS = {
        CodeLanguage.JAVA: Parser(Language(ts_java.language())),
        CodeLanguage.KOTLIN: Parser(Language(ts_kotlin.language())),
    }
    
    def check(self, code: str, language: CodeLanguage) -> SyntaxCheckResult:
        parser = self.PARSERS.get(language)
        if not parser:
            return SyntaxCheckResult(valid=True)  # 不支持的语言跳过
        
        try:
            tree = parser.parse(bytes(code, "utf8"))
            root_node = tree.root_node
            
            # 检查是否有错误节点
            errors = self._collect_errors(root_node)
            
            return SyntaxCheckResult(
                valid=len(errors) == 0,
                errors=errors,
                ast_summary=self._summarize_ast(root_node)
            )
        except Exception as e:
            return SyntaxCheckResult(
                valid=False,
                errors=[SyntaxError(message=str(e), line=0, column=0)]
            )
    
    def _collect_errors(self, node) -> List[SyntaxError]:
        """递归收集所有错误节点"""
        errors = []
        if node.type == "ERROR":
            errors.append(SyntaxError(
                message="Syntax error",
                line=node.start_point[0] + 1,
                column=node.start_point[1]
            ))
        for child in node.children:
            errors.extend(self._collect_errors(child))
        return errors
```

#### 3.4.2 代码质量评估维度

```python
@dataclass
class CodeQualityReport:
    """代码质量评估报告"""
    
    # 语法正确性 (0-1)
    syntax_valid: float
    
    # 规范符合度 (0-1)
    style_compliance: float
    
    # 安全评分 (0-1)
    security_score: float
    
    # 复杂度指标
    cyclomatic_complexity: int
    cognitive_complexity: int
    lines_of_code: int
    
    # 可维护性
    documentation_coverage: float
    test_coverage: float
    
    @property
    def overall_score(self) -> float:
        """综合质量分"""
        return (
            self.syntax_valid * 0.4 +
            self.style_compliance * 0.3 +
            self.security_score * 0.3
        )
```

### 3.5 反馈闭环设计

#### 3.5.1 数据收集

```python
@dataclass
class GenerationFeedback:
    """代码生成反馈"""
    generation_id: str
    timestamp: datetime
    
    # 输入
    requirement: str
    implementation_type: ImplementationType
    
    # 输出
    generated_code: str
    quality_score: float
    
    # 反馈
    user_rating: int  # 1-5 星
    user_comments: str
    
    # 后续行为
    was_modified: bool  # 用户是否修改了代码
    modification_diff: str  # 修改 diff
    compilation_success: bool  # 是否编译成功
    test_pass_rate: float  # 测试通过率
```

#### 3.5.2 持续学习 Pipeline

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ 收集反馈数据  │ → │ 质量评估与标注 │ → │ 模型微调     │
└──────────────┘    └──────────────┘    └──────────────┘
        ↑                                          ↓
        └──────────────┐    ┌──────────────┐    ┌──────────────┐
                      │ 部署新版本  │ ← │ A/B 测试验证 │
                      └──────────────┘    └──────────────┘
```

---

## 4. 实施 Checklist

### Phase 1 检查清单

- [ ] **LLM 接入**
  - [ ] 创建 `llm_client.py` 模块
  - [ ] 实现 OpenAI/Claude 客户端封装
  - [ ] 添加 API Key 配置管理
  - [ ] 实现重试和降级逻辑
  - [ ] 添加 Token 使用监控

- [ ] **模板引擎升级**
  - [ ] 安装 Jinja2 依赖
  - [ ] 创建 `template_engine.py` 模块
  - [ ] 迁移现有模板到 Jinja2 格式
  - [ ] 实现自定义过滤器
  - [ ] 添加模板缓存

- [ ] **语法检查**
  - [ ] 安装 tree-sitter 和相关语言包
  - [ ] 创建 `syntax_checker.py` 模块
  - [ ] 实现 Java/Kotlin/SQL 检查
  - [ ] 集成到 CodeGeneratorSkill

### Phase 2 检查清单

- [ ] **向量检索**
  - [ ] 选择 Embedding 模型
  - [ ] 搭建向量数据库
  - [ ] 实现资产索引 pipeline
  - [ ] 替换现有关键词匹配
  - [ ] 评估匹配精度提升

- [ ] **Few-shot 示例**
  - [ ] 收集优质代码示例
  - [ ] 构建示例标注体系
  - [ ] 实现示例选择算法
  - [ ] 集成到 Prompt 构建

- [ ] **需求理解**
  - [ ] 增强实体抽取
  - [ ] 完善意图识别
  - [ ] 支持多轮对话

### Phase 3 检查清单

- [ ] **反馈闭环**
  - [ ] 设计反馈收集界面
  - [ ] 实现数据存储
  - [ ] 构建评估指标
  - [ ] 自动化质量报告

- [ ] **监控告警**
  - [ ] 搭建指标看板
  - [ ] 配置告警规则
  - [ ] 实现自动降级

---

## 附录

### A. 推荐依赖清单

```txt
# LLM 客户端
openai>=1.0.0
anthropic>=0.18.0

# 模板引擎
jinja2>=3.1.2

# 向量检索
chromadb>=0.4.0
sentence-transformers>=2.2.0

# 语法检查
tree-sitter>=0.20.0
tree-sitter-java>=0.20.0
tree-sitter-kotlin>=0.3.0

# 代码质量
radon>=6.0.0          # 复杂度分析
bandit>=1.7.0         # 安全检查
```

### B. 参考资源

- [OpenAI Prompt Engineering Guide](https://platform.openai.com/docs/guides/prompt-engineering)
- [Jinja2 Documentation](https://jinja.palletsprojects.com/)
- [Tree-sitter Documentation](https://tree-sitter.github.io/tree-sitter/)
- [ChromaDB Documentation](https://docs.trychroma.com/)

---

*文档维护：每当完成一个优化项，请更新此文档中的状态标记。*
