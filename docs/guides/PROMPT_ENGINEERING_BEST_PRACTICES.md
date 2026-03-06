# LLM Prompt 工程最佳实践

> **目标**: 提升代码生成质量，降低幻觉和编译错误率

---

## 1. Prompt 分层架构

### 1.1 四层 Prompt 结构

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 1: System Prompt (系统角色定义)                        │
│  → 定义 AI 的角色、能力和约束                                │
├─────────────────────────────────────────────────────────────┤
│  Layer 2: Context Prompt (上下文信息)                        │
│  → 业务背景、技术规范、可用资产                              │
├─────────────────────────────────────────────────────────────┤
│  Layer 3: Task Prompt (任务描述)                             │
│  → 具体开发需求、技术要求、输出格式                          │
├─────────────────────────────────────────────────────────────┤
│  Layer 4: Few-shot Examples (示例引导)                       │
│  → 相似任务的优质代码示例                                    │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 System Prompt 模板

```python
SYSTEM_PROMPT = """你是金蝶星瀚HR系统的资深开发工程师，拥有10年以上企业级软件开发经验。

## 核心能力
- 精通 Java 17、Kotlin、Spring Boot 2.7+ 开发
- 熟悉金蝶云苍穹/星瀚平台架构和插件机制
- 擅长 SQL 优化和复杂报表开发
- 严格遵守企业级代码规范和安全标准

## 编码原则
1. 代码必须完整、可编译、可直接运行
2. 遵循单一职责原则，函数长度不超过50行
3. 所有 public 方法必须有 JavaDoc 注释
4. 输入参数必须进行有效性校验
5. 敏感操作必须记录审计日志

## 禁止事项
- 不要生成伪代码或 TODO 标记
- 不要省略必要的 import 语句
- 不要硬编码魔法数字和字符串
- 不要忽略异常处理

## 输出格式
只输出代码本身，不需要解释说明。代码前后用 ``` 包裹。"""
```

### 1.3 Context Prompt 构建

```python
def build_context_prompt(
    hr_module: str,
    tech_stack: str,
    available_assets: List[CodeAsset]
) -> str:
    """构建上下文 Prompt"""
    
    # 业务上下文
    business_context = load_business_context(hr_module)
    
    # 技术规范
    guidelines = load_coding_guidelines(tech_stack)
    
    # 可用资产
    asset_descriptions = []
    for asset in available_assets[:3]:  # 最多3个资产
        asset_descriptions.append(f"""
- {asset.name}: {asset.description}
  使用场景: {', '.join(asset.usage_context)}
""")
    
    return f"""
## 业务上下文
{business_context}

## 技术规范
{guidelines}

## 可用资产（可直接使用）
{chr(10).join(asset_descriptions)}
"""
```

---

## 2. Task Prompt 优化技巧

### 2.1 结构化任务描述

```python
# ❌ 不好的任务描述
"写一个考勤打卡的接口"

# ✅ 好的任务描述
TASK_PROMPT = """
## 功能需求
开发一个考勤打卡接口，支持以下功能：
1. 接收员工打卡请求（员工ID、打卡时间、GPS坐标）
2. 校验员工是否在有效打卡范围内
3. 记录打卡记录到数据库
4. 返回打卡结果和异常提醒

## 技术要求
- 开发类型: REST API Controller
- 框架: Spring Boot 2.7
- 安全: 需要 JWT Token 验证
- 响应格式: 统一 Result<T> 包装

## 输入参数
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| checkInTime | LocalDateTime | 是 | 打卡时间 |
| latitude | Double | 否 | GPS纬度 |
| longitude | Double | 否 | GPS经度 |

## 输出要求
1. 完整的 Controller 类代码
2. 包含请求/响应 DTO 定义
3. 参数校验注解
4. Swagger 接口文档注解
"""
```

### 2.2 输出格式控制

```python
# 使用 XML 标签控制输出结构
OUTPUT_FORMAT_PROMPT = """
<output_requirements>
  <format>完整 Java 类代码</format>
  <structure>
    1. package 声明
    2. import 语句（按字母排序）
    3. 类级 JavaDoc
    4. 类定义和注解
    5. 字段声明
    6. 构造函数
    7. 业务方法（按调用顺序排列）
    8. 私有辅助方法
  </structure>
  <style>
    - 使用 4 空格缩进
    - 行长度不超过 120 字符
    - 大括号使用 Egyptian 风格
  </style>
</output_requirements>
"""
```

---

## 3. Few-shot 示例工程

### 3.1 示例选择策略

```python
class DynamicExampleSelector:
    """动态示例选择器"""
    
    def __init__(self, embedding_model: str = "text-embedding-3-small"):
        self.embedder = OpenAIEmbeddings(model=embedding_model)
        self.example_db = ChromaDB(collection="code_examples")
    
    def select_examples(
        self,
        requirement: str,
        implementation_type: ImplementationType,
        k: int = 3
    ) -> List[CodeExample]:
        """
        选择最相关的示例
        
        策略：
        1. 类型匹配：优先选择同类型的示例
        2. 语义相似：使用 Embedding 计算相似度
        3. 难度适配：选择复杂度相近的示例
        4. 多样性保证：避免过于相似的示例
        """
        
        # 构建查询
        query = f"{implementation_type.value}: {requirement}"
        query_embedding = self.embedder.embed_query(query)
        
        # 初始检索（2k个候选）
        candidates = self.example_db.similarity_search(
            embedding=query_embedding,
            filter={"type": implementation_type.value},
            k=k*2
        )
        
        # 重排序和筛选
        selected = []
        for candidate in candidates:
            if len(selected) >= k:
                break
                
            # 多样性检查
            if not self._is_too_similar(candidate, selected):
                selected.append(candidate)
        
        return selected
    
    def _is_too_similar(
        self,
        candidate: CodeExample,
        selected: List[CodeExample],
        threshold: float = 0.9
    ) -> bool:
        """检查是否与已选示例过于相似"""
        for ex in selected:
            similarity = cosine_similarity(
                candidate.embedding,
                ex.embedding
            )
            if similarity > threshold:
                return True
        return False
```

### 3.2 示例格式规范

```python
@dataclass
class CodeExample:
    """代码示例"""
    id: str
    requirement: str           # 需求描述
    implementation_type: str   # 实现类型
    code: str                  # 完整代码
    quality_score: float       # 质量评分
    usage_count: int          # 使用次数
    embedding: List[float]    # 向量表示
    
    def to_prompt_format(self) -> str:
        """转换为 Prompt 格式"""
        return f"""
### 示例
需求: {self.requirement}

代码:
```java
{self.code}
```
"""
```

### 3.3 示例库构建

```python
# knowledge/reusable_assets/examples/
EXAMPLE_LIBRARY = {
    "java_api": [
        {
            "id": "ex_controller_001",
            "requirement": "开发员工信息查询接口，支持分页和模糊搜索",
            "code": """
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    
    @GetMapping
    public Result<Page<EmployeeDTO>> listEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        // 实现...
    }
}
""",
            "quality_score": 4.8,
            "tags": ["controller", "pagination", "search"]
        },
        # ... 更多示例
    ],
    "kotlin_plugin": [
        # ...
    ],
    "sql_report": [
        # ...
    ]
}
```

---

## 4. 高级技巧

### 4.1 Chain-of-Thought 引导

```python
# 对于复杂任务，引导 LLM 逐步思考
COT_PROMPT = """
请按以下步骤完成代码生成：

步骤1 - 分析需求
- 识别核心功能点
- 确定输入输出
- 梳理业务规则

步骤2 - 设计接口
- 定义方法签名
- 设计 DTO 对象
- 确定异常处理策略

步骤3 - 实现代码
- 编写主逻辑
- 添加参数校验
- 完善日志记录

请按上述步骤思考，但最终只输出步骤3的代码。
"""
```

### 4.2 Self-Consistency 验证

```python
async def generate_with_self_consistency(
    prompt: str,
    n_samples: int = 3,
    temperature: float = 0.7
) -> str:
    """
    使用自一致性验证生成代码
    
    策略：
    1. 生成多个候选代码
    2. 通过编译/静态分析筛选
    3. 选择质量最高的
    """
    
    # 并行生成多个候选
    candidates = await asyncio.gather(*[
        llm.generate(prompt, temperature=temperature)
        for _ in range(n_samples)
    ])
    
    # 评估每个候选
    scored_candidates = []
    for code in candidates:
        score = await evaluate_code_quality(code)
        scored_candidates.append((code, score))
    
    # 选择最高分
    scored_candidates.sort(key=lambda x: x[1], reverse=True)
    return scored_candidates[0][0]
```

### 4.3 迭代优化策略

```python
async def iterative_refinement(
    initial_requirement: str,
    max_iterations: int = 3
) -> str:
    """
    迭代优化生成结果
    """
    code = await generate_code(initial_requirement)
    
    for i in range(max_iterations):
        # 静态分析
        issues = static_analysis(code)
        
        if not issues:
            break
            
        # 构建修复 Prompt
        fix_prompt = f"""
以下代码存在以下问题：
{format_issues(issues)}

请修复这些问题，保持原有功能不变：
```java
{code}
```
"""
        code = await generate_code(fix_prompt)
    
    return code
```

---

## 5. Prompt 版本管理

### 5.1 版本控制策略

```
prompts/
├── v1.0/
│   ├── system_prompt.txt
│   ├── task_templates/
│   └── examples/
├── v1.1/
│   └── ...
└── v2.0/
    └── ...
```

### 5.2 A/B 测试框架

```python
class PromptExperiment:
    """Prompt A/B 测试"""
    
    def __init__(self, experiment_id: str):
        self.id = experiment_id
        self.variants = {
            "control": load_prompt("v1.0"),
            "treatment": load_prompt("v1.1")
        }
    
    async def generate(self, requirement: str) -> str:
        # 随机选择变体
        variant = random.choice(list(self.variants.keys()))
        
        prompt = self.variants[variant].format(requirement)
        code = await llm.generate(prompt)
        
        # 记录实验数据
        track_experiment(self.id, variant, requirement, code)
        
        return code
    
    def analyze_results(self) -> ExperimentResult:
        """分析实验结果"""
        return calculate_metrics(self.id)
```

---

## 6. 评估指标

| 指标 | 定义 | 目标值 |
|------|------|--------|
| 编译通过率 | 生成代码首次编译成功比例 | > 95% |
| 功能正确率 | 人工评审功能正确比例 | > 90% |
| 规范符合度 | 符合编码规范的比例 | > 85% |
| Token 效率 | 每功能点消耗 Token 数 | < 2000 |
| 生成耗时 | 平均生成时间 | < 5s |

---

*维护记录：*
- 2026-03-06: 创建初始版本
