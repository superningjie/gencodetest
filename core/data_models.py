"""
标准化数据模型 V2.0
基于V1.1扩展，新增代码生成相关模型
"""
from dataclasses import dataclass, field
from typing import Dict, List, Optional, Any
from enum import Enum
import uuid
from datetime import datetime


class ImplementationType(Enum):
    """实现类型"""
    # 通用类型
    JAVA_API = "java_api"
    KOTLIN_PLUGIN = "kotlin_plugin"
    SQL_REPORT = "sql_report"
    PYTHON_SCRIPT = "python_script"
    CONFIGURATION = "configuration"
    
    # 金蝶云苍穹特定类型
    ENTITY_METADATA = "entity_metadata"          # 实体元数据
    FORM_METADATA = "form_metadata"              # 表单元数据
    SERVICE_METADATA = "service_metadata"        # 服务元数据
    KINGSCRIPT_SERVICE = "kingscript_service"    # KingScript服务
    KINGSCRIPT_OPERATION = "kingscript_operation" # KingScript操作
    KINGSCRIPT_PLUGIN = "kingscript_plugin"      # KingScript插件
    JAVA_FORM_PLUGIN = "java_form_plugin"        # Java表单插件
    JAVA_LIST_PLUGIN = "java_list_plugin"        # Java列表插件
    JAVA_OPERATION_PLUGIN = "java_operation_plugin" # Java操作插件
    JAVA_TRANSFORM_PLUGIN = "java_transform_plugin" # Java转换插件
    JAVA_EXTENSION_POINT = "java_extension_point"   # 扩展点定义
    JAVA_EXTENSION_IMPL = "java_extension_impl"     # 扩展实现
    CUSTOM_CONTROL = "custom_control"            # 自定义控件


class CodeLanguage(Enum):
    """代码语言"""
    JAVA = "java"
    KOTLIN = "kotlin"
    SQL = "sql"
    PYTHON = "python"
    XML = "xml"
    YAML = "yaml"
    TYPESCRIPT = "typescript"  # 金蝶KingScript使用TypeScript


@dataclass
class Requirement:
    """标准化需求对象 [V1.1]"""
    id: str
    name: str
    description: str
    category: str           # 系统集成/功能增强/报表开发/接口开发
    related_module: str     # HR_CORE/HR_PAYROLL/HR_ATTENDANCE/HR_RECRUITMENT
    customization_type: str # 简单配置/标准扩展/复杂定制/二次开发
    priority: str = "中"     # 高/中/低
    confidence: float = 0.8
    metadata: Dict = field(default_factory=dict)

    def __post_init__(self):
        if not self.id:
            self.id = f"REQ_{uuid.uuid4().hex[:8].upper()}"


@dataclass
class ImplementationItem:
    """开发实现项 [V2.0新增]

    将需求拆分为具体的、可实现的开发项
    """
    id: str
    requirement_id: str
    name: str
    description: str
    impl_type: ImplementationType
    language: CodeLanguage

    # 技术细节
    tech_stack: str = ""           # 技术栈，如：SpringBoot 2.7
    framework_version: str = ""    # 框架版本

    # 功能规格
    input_params: List[Dict] = field(default_factory=list)
    output_params: List[Dict] = field(default_factory=list)
    business_rules: List[str] = field(default_factory=list)

    # 依赖关系
    dependencies: List[str] = field(default_factory=list)  # 依赖的其他实现项ID
    required_assets: List[str] = field(default_factory=list)  # 需要的资产ID

    # 扩展点信息（如果是插件）
    extension_point: str = ""      # 扩展点ID

    # 模块信息
    related_module: str = "HR_CORE"  # 所属HR模块
    
    # 金蝶苍穹特定字段
    extend_type: str = ""          # 扩展类型: extend(扩展标品) / inherit(继承模板) / new(新建)
    parent_template: str = ""      # 父模板ID（继承时使用）
    extend_target: str = ""        # 扩展目标（扩展标品时使用）
    extension_point: str = ""      # 扩展点ID
    plugin_register: str = ""      # 插件注册位置
    
    # 元数据定义（元数据类型时使用）
    entity_definition: Dict = field(default_factory=dict)
    form_definition: Dict = field(default_factory=dict)
    service_definition: Dict = field(default_factory=dict)

    def __post_init__(self):
        if not self.id:
            self.id = f"IMPL_{uuid.uuid4().hex[:8].upper()}"


@dataclass
class CodeAsset:
    """可复用代码资产 [V2.0新增]"""
    id: str
    name: str
    asset_type: str                # code_snippet, file_template, design_pattern
    language: CodeLanguage

    # 内容
    content: str = ""              # 代码内容
    file_path: str = ""            # 文件路径（如果是文件模板）

    # 元数据
    tags: List[str] = field(default_factory=list)
    description: str = ""
    usage_context: List[str] = field(default_factory=list)
    variables: List[str] = field(default_factory=list)  # 模板变量

    # 质量指标
    usage_count: int = 0
    success_rate: float = 1.0
    avg_rating: float = 5.0
    last_used: Optional[datetime] = None

    # 依赖
    dependencies: List[str] = field(default_factory=list)

    def __post_init__(self):
        if not self.id:
            self.id = f"ASSET_{uuid.uuid4().hex[:8].upper()}"


@dataclass
class GeneratedCode:
    """生成的代码 [V2.0新增]"""
    id: str
    implementation_item_id: str

    # 代码内容
    file_path: str                 # 目标文件路径
    language: CodeLanguage
    content: str                   # 完整代码内容

    # 生成信息
    generated_at: datetime = field(default_factory=datetime.now)
    generator_version: str = "2.0"
    prompt_version: str = ""

    # 使用的资产
    used_assets: List[str] = field(default_factory=list)

    # 质量评估
    quality_score: float = 0.0     # 综合质量分 0-1
    syntax_valid: bool = False
    style_score: float = 0.0
    security_issues: List[Dict] = field(default_factory=list)

    # 状态
    status: str = "generated"      # generated, reviewed, approved, rejected
    review_comments: List[str] = field(default_factory=list)

    # 元数据
    metadata: Dict = field(default_factory=dict)

    def __post_init__(self):
        if not self.id:
            self.id = f"CODE_{uuid.uuid4().hex[:8].upper()}"


@dataclass
class EstimationItem:
    """标准化评估项 [V1.1]"""
    phase: str      # 需求/设计/开发/测试/项目管理
    module: str
    function: str
    days: float
    unit_price: int = 3000
    risk: str = "低" # 低/中/高
    deliverable: str = ""
    assumptions: str = ""

    @property
    def cost(self) -> int:
        return int(self.days * self.unit_price)


@dataclass
class Risk:
    """标准化风险对象 [V1.1]"""
    category: str       # 需求/技术/集成/资源/业务
    description: str
    impact: str
    probability: str    # 高/中/低
    level: str          # 高/中/低
    mitigation: str = ""
    owner: str = "项目经理"


@dataclass
class BatchJob:
    """批处理作业 [V1.1]"""
    job_id: str
    status: str         # pending/running/completed/failed
    total_items: int
    completed_items: int = 0
    failed_items: int = 0

    @property
    def progress(self) -> float:
        if self.total_items == 0:
            return 0.0
        return (self.completed_items + self.failed_items) / self.total_items


@dataclass
class SkillContext:
    """Skill上下文 [V1.1增强]

    支持类型安全的数据访问
    """
    session_id: str
    user_input: str = ""
    pipeline_config: Dict = field(default_factory=dict)

    # 数据存储（类型安全）
    _data: Dict[str, Any] = field(default_factory=dict, repr=False)

    def set_requirements(self, requirements: List[Requirement]):
        """设置需求列表"""
        self._data['requirements'] = requirements

    def get_requirements(self) -> List[Requirement]:
        """获取需求列表"""
        return self._data.get('requirements', [])

    def set_implementation_items(self, items: List[ImplementationItem]):
        """设置实现项列表 [V2.0]"""
        self._data['implementation_items'] = items

    def get_implementation_items(self) -> List[ImplementationItem]:
        """获取实现项列表 [V2.0]"""
        return self._data.get('implementation_items', [])

    def set_generated_code(self, code_items: List[GeneratedCode]):
        """设置生成的代码 [V2.0]"""
        self._data['generated_code'] = code_items

    def get_generated_code(self) -> List[GeneratedCode]:
        """获取生成的代码 [V2.0]"""
        return self._data.get('generated_code', [])

    def set_assets(self, assets: List[CodeAsset]):
        """设置匹配的资产 [V2.0]"""
        self._data['matched_assets'] = assets

    def get_assets(self) -> List[CodeAsset]:
        """获取匹配的资产 [V2.0]"""
        return self._data.get('matched_assets', [])

    def get_raw(self, key: str, default=None):
        """通用原始数据访问"""
        return self._data.get(key, default)

    def set_raw(self, key: str, value: Any):
        """通用原始数据设置"""
        self._data[key] = value
