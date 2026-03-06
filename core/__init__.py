"""
金蝶星瀚HR AI顾问系统 V2.0 - 核心引擎

包含：
- data_models: 标准化数据模型定义
- skill_engine: 插件化Skill引擎
- pipeline_executor: Pipeline执行器
- knowledge_base: 知识库管理
- config_loader: 配置加载器
"""

from core.data_models import (
    Requirement,
    ImplementationItem,
    ImplementationType,
    CodeLanguage,
    CodeAsset,
    GeneratedCode,
    EstimationItem,
    Risk,
    BatchJob,
    SkillContext,
)

from core.skill_engine import (
    BaseSkill,
    PluginRegistry,
    registry,
    DefaultSkill,
    ImplementSplitterSkill,
    AssetMatcherSkill,
    CodeGeneratorSkill,
    RequirementAnalyzerSkill,
    CodeQualityCheckerSkill,
    EffortEstimatorSkill,
    RiskAssessorSkill,
    OutputAssemblerSkill,
    CodePostProcessor,
    SyntaxChecker,
    StyleFormatter,
)

from core.pipeline_executor import (
    PipelineExecutor,
    BatchPipelineExecutor,
    PipelineConfig,
    PipelineStep,
    StepResult,
    StepStatus,
)

__version__ = "2.0.0"
__all__ = [
    # Data Models
    "Requirement",
    "ImplementationItem",
    "ImplementationType",
    "CodeLanguage",
    "CodeAsset",
    "GeneratedCode",
    "EstimationItem",
    "Risk",
    "BatchJob",
    "SkillContext",
    # Skill Engine
    "BaseSkill",
    "PluginRegistry",
    "registry",
    "DefaultSkill",
    "ImplementSplitterSkill",
    "AssetMatcherSkill",
    "CodeGeneratorSkill",
    "RequirementAnalyzerSkill",
    "CodeQualityCheckerSkill",
    "EffortEstimatorSkill",
    "RiskAssessorSkill",
    "OutputAssemblerSkill",
    "CodePostProcessor",
    "SyntaxChecker",
    "StyleFormatter",
    # Pipeline
    "PipelineExecutor",
    "BatchPipelineExecutor",
    "PipelineConfig",
    "PipelineStep",
    "StepResult",
    "StepStatus",
]