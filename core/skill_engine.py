"""
插件化Skill引擎 [V1.1] [V2.0增强]
支持动态Skill注册、自定义类加载、代码生成专项支持
"""
import os
import sys
import yaml
import importlib
import inspect
from abc import ABC, abstractmethod
from typing import Dict, List, Type, Optional, Any, Callable
from pathlib import Path
import asyncio
import logging
from datetime import datetime

from core.data_models import SkillContext, Requirement, ImplementationItem, ImplementationType, CodeLanguage, GeneratedCode, CodeAsset

logger = logging.getLogger(__name__)


class BaseSkill(ABC):
    """Skill基类 [V1.1]"""

    def __init__(self, config: Dict = None):
        self.config = config or {}
        self.skill_id = self.config.get('skill_id', self.__class__.__name__)
        self.version = self.config.get('version', '1.0')

    @abstractmethod
    async def execute(self, context: SkillContext) -> Dict:
        """执行Skill逻辑"""
        pass

    def validate_config(self) -> bool:
        """验证配置"""
        return True


class PluginRegistry:
    """插件注册中心 [V1.1] [V2.0增强]"""

    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._skills = {}
            cls._instance._output_formats = {}
            cls._instance._code_generators = {}  # [V2.0] 代码生成器注册
            cls._instance._asset_matchers = {}   # [V2.0] 资产匹配器注册
            cls._instance._initialized = False
        return cls._instance

    def register(self, category: str, name: str, handler: Callable):
        """通用注册方法"""
        registry_map = {
            'skill': self._skills,
            'output_format': self._output_formats,
            'code_generator': self._code_generators,  # [V2.0]
            'asset_matcher': self._asset_matchers,    # [V2.0]
        }

        if category not in registry_map:
            raise ValueError(f"Unknown category: {category}")

        registry_map[category][name] = handler
        logger.info(f"Registered {category}: {name}")

    def get_skill(self, name: str) -> Optional[Type[BaseSkill]]:
        """获取Skill类"""
        return self._skills.get(name)

    def get_code_generator(self, name: str) -> Optional[Callable]:  # [V2.0]
        """获取代码生成器"""
        return self._code_generators.get(name)

    def get_asset_matcher(self, name: str) -> Optional[Callable]:  # [V2.0]
        """获取资产匹配器"""
        return self._asset_matchers.get(name)

    def list_skills(self) -> List[str]:
        """列出所有已注册Skill"""
        return list(self._skills.keys())

    def load_skill_from_yaml(self, yaml_path: str) -> Optional[BaseSkill]:
        """从YAML配置加载Skill [V1.1] [V2.0增强]"""
        try:
            with open(yaml_path, 'r', encoding='utf-8') as f:
                config = yaml.safe_load(f)

            skill_id = config.get('skill_id')
            if not skill_id:
                logger.error(f"Skill ID not found in {yaml_path}")
                return None

            # 检查是否有自定义类 [V1.1]
            custom_class_path = config.get('custom_class')
            if custom_class_path:
                skill_class = self._load_custom_class(custom_class_path)
                if skill_class:
                    return skill_class(config)

            # 使用默认Skill类
            skill_type = config.get('type', 'default')
            if skill_type == 'code_generator':  # [V2.0]
                return CodeGeneratorSkill(config)
            elif skill_type == 'asset_matcher':  # [V2.0]
                return AssetMatcherSkill(config)
            elif skill_type == 'implement_splitter':  # [V2.0]
                return ImplementSplitterSkill(config)
            else:
                return DefaultSkill(config)

        except Exception as e:
            logger.error(f"Failed to load skill from {yaml_path}: {e}")
            return None

    def _load_custom_class(self, class_path: str) -> Optional[Type[BaseSkill]]:
        """动态加载自定义类 [V1.1]"""
        try:
            module_path, class_name = class_path.rsplit('.', 1)
            module = importlib.import_module(module_path)
            skill_class = getattr(module, class_name)

            if not issubclass(skill_class, BaseSkill):
                logger.error(f"Class {class_name} is not a subclass of BaseSkill")
                return None

            return skill_class
        except Exception as e:
            logger.error(f"Failed to load custom class {class_path}: {e}")
            return None

    def auto_discover_skills(self, skills_dir: str = "skills"):
        """自动发现目录下的所有Skill [V1.1]"""
        skills_path = Path(skills_dir)
        if not skills_path.exists():
            logger.warning(f"Skills directory not found: {skills_dir}")
            return

        for skill_dir in skills_path.iterdir():
            if skill_dir.is_dir():
                yaml_file = skill_dir / "skill.yaml"
                if yaml_file.exists():
                    skill = self.load_skill_from_yaml(str(yaml_file))
                    if skill:
                        self.register('skill', skill.skill_id, skill.__class__)


class DefaultSkill(BaseSkill):
    """默认Skill实现"""

    async def execute(self, context: SkillContext) -> Dict:
        """默认执行逻辑"""
        return {
            'status': 'success',
            'message': 'Default skill executed',
            'data': {}
        }


class ImplementSplitterSkill(BaseSkill):  # [V2.0新增]
    """开发实现拆分器 Skill

    将需求拆分为具体的实现项
    """

    def __init__(self, config: Dict = None):
        super().__init__(config)
        self.rules_file = self.config.get('rules_file', 'splitter_rules.yaml')
        self.rules = self._load_rules()

    def _load_rules(self) -> Dict:
        """加载拆分规则"""
        rules_path = Path(self.config.get('skill_dir', '')) / self.rules_file
        if rules_path.exists():
            with open(rules_path, 'r', encoding='utf-8') as f:
                return yaml.safe_load(f)
        return {}

    async def execute(self, context: SkillContext) -> Dict:
        """执行需求拆分"""
        requirements = context.get_requirements()

        if not requirements:
            return {'status': 'error', 'message': 'No requirements found'}

        implementation_items = []

        for req in requirements:
            items = await self._split_requirement(req)
            implementation_items.extend(items)

        context.set_implementation_items(implementation_items)

        return {
            'status': 'success',
            'data': {
                'total_requirements': len(requirements),
                'total_implementations': len(implementation_items),
                'implementations': [self._item_to_dict(item) for item in implementation_items]
            }
        }

    async def _split_requirement(self, req: Requirement) -> List[ImplementationItem]:
        """拆分单个需求为实现项"""
        items = []

        # 根据需求类型和定制化类型决定拆分策略
        split_strategy = self._determine_strategy(req)

        if split_strategy == 'api_development':
            # API开发：拆分为Controller, Service, DAO
            items.extend([
                ImplementationItem(
                    id="",
                    requirement_id=req.id,
                    name=f"{req.name}_Controller",
                    description=f"{req.description} - REST API接口层",
                    impl_type=ImplementationType.JAVA_API,
                    language=CodeLanguage.JAVA,
                    tech_stack="SpringBoot 2.7",
                    extension_point="",
                    related_module=req.related_module
                ),
                ImplementationItem(
                    id="",
                    requirement_id=req.id,
                    name=f"{req.name}_Service",
                    description=f"{req.description} - 业务逻辑层",
                    impl_type=ImplementationType.JAVA_API,
                    language=CodeLanguage.JAVA,
                    tech_stack="SpringBoot 2.7",
                    extension_point="",
                    related_module=req.related_module
                ),
                ImplementationItem(
                    id="",
                    requirement_id=req.id,
                    name=f"{req.name}_Repository",
                    description=f"{req.description} - 数据访问层",
                    impl_type=ImplementationType.JAVA_API,
                    language=CodeLanguage.JAVA,
                    tech_stack="SpringBoot 2.7 + MyBatis",
                    extension_point="",
                    related_module=req.related_module
                )
            ])

        elif split_strategy == 'plugin_development':
            # 插件开发
            items.append(ImplementationItem(
                id="",
                requirement_id=req.id,
                name=f"{req.name}_Plugin",
                description=req.description,
                impl_type=ImplementationType.KOTLIN_PLUGIN,
                language=CodeLanguage.KOTLIN,
                tech_stack="金蝶插件框架 5.0",
                extension_point=self._detect_extension_point(req),
                related_module=req.related_module
            ))

        elif split_strategy == 'report_development':
            # 报表开发
            items.append(ImplementationItem(
                id="",
                requirement_id=req.id,
                name=f"{req.name}_Report",
                description=req.description,
                impl_type=ImplementationType.SQL_REPORT,
                language=CodeLanguage.SQL,
                tech_stack="Oracle 19c / SQL Server 2019",
                related_module=req.related_module
            ))

        else:
            # 默认：单一项
            items.append(ImplementationItem(
                id="",
                requirement_id=req.id,
                name=req.name,
                description=req.description,
                impl_type=ImplementationType.CONFIGURATION,
                language=CodeLanguage.YAML,
                tech_stack="金蝶配置平台",
                related_module=req.related_module
            ))

        return items

    def _determine_strategy(self, req: Requirement) -> str:
        """确定拆分策略"""
        desc = req.description.lower()

        if '插件' in desc or '扩展' in desc or '自定义' in desc:
            return 'plugin_development'
        elif '报表' in desc or '统计' in desc or '查询' in desc:
            return 'report_development'
        elif '接口' in desc or 'api' in desc or '服务' in desc:
            return 'api_development'
        else:
            return 'configuration'

    def _detect_extension_point(self, req: Requirement) -> str:
        """检测扩展点"""
        # 基于规则匹配扩展点
        desc = req.description.lower()
        module = req.related_module.lower()

        extension_points = {
            'hr_attendance': {
                '打卡': 'attendance_check_in',
                '请假': 'attendance_leave',
                '加班': 'attendance_overtime'
            },
            'hr_payroll': {
                '计算': 'payroll_calculation',
                '发放': 'payroll_distribution'
            }
        }

        for mod, points in extension_points.items():
            if mod in module:
                for keyword, point_id in points.items():
                    if keyword in desc:
                        return point_id

        return "generic_extension"

    def _item_to_dict(self, item: ImplementationItem) -> Dict:
        """转换为字典"""
        return {
            'id': item.id,
            'name': item.name,
            'type': item.impl_type.value,
            'language': item.language.value,
            'tech_stack': item.tech_stack
        }


class AssetMatcherSkill(BaseSkill):  # [V2.0新增]
    """资产匹配器 Skill

    为实现项匹配可复用资产
    """

    def __init__(self, config: Dict = None):
        super().__init__(config)
        self.asset_index_path = self.config.get('asset_index', 'knowledge/reusable_assets/asset_index.yaml')
        self.assets = self._load_assets()

    def _load_assets(self) -> List[CodeAsset]:
        """加载资产索引"""
        assets = []
        index_path = Path(self.asset_index_path)

        if index_path.exists():
            with open(index_path, 'r', encoding='utf-8') as f:
                index = yaml.safe_load(f)

            for asset_data in index.get('assets', []):
                assets.append(CodeAsset(
                    id=asset_data.get('id', ''),
                    name=asset_data.get('name', ''),
                    asset_type=asset_data.get('type', ''),
                    language=CodeLanguage(asset_data.get('language', 'java')),
                    file_path=asset_data.get('path', ''),
                    tags=asset_data.get('tags', []),
                    description=asset_data.get('description', ''),
                    usage_context=asset_data.get('usage_context', [])
                ))

        return assets

    async def execute(self, context: SkillContext) -> Dict:
        """执行资产匹配"""
        impl_items = context.get_implementation_items()

        if not impl_items:
            return {'status': 'error', 'message': 'No implementation items found'}

        matched_assets = []

        for item in impl_items:
            assets = await self._match_assets_for_item(item)
            matched_assets.extend(assets)
            item.required_assets = [a.id for a in assets]

        context.set_assets(matched_assets)

        return {
            'status': 'success',
            'data': {
                'total_items': len(impl_items),
                'matched_assets': len(matched_assets),
                'assets': [{'id': a.id, 'name': a.name, 'type': a.asset_type} for a in matched_assets]
            }
        }

    async def _match_assets_for_item(self, item: ImplementationItem) -> List[CodeAsset]:
        """为单个实现项匹配资产"""
        matched = []

        # 1. 语言匹配
        lang_assets = [a for a in self.assets if a.language == item.language]

        # 2. 上下文匹配
        for asset in lang_assets:
            score = 0

            # 标签匹配
            item_tags = set(item.name.lower().split('_'))
            asset_tags = set(tag.lower() for tag in asset.tags)
            tag_overlap = len(item_tags & asset_tags)
            score += tag_overlap * 2

            # 使用场景匹配
            if item.impl_type.value in asset.usage_context:
                score += 3

            # 描述关键词匹配
            desc_keywords = item.description.lower().split()
            asset_desc = asset.description.lower()
            for keyword in desc_keywords:
                if keyword in asset_desc:
                    score += 1

            if score >= 3:  # 阈值
                matched.append(asset)

        # 按质量排序，返回Top 3
        matched.sort(key=lambda a: (a.avg_rating, a.success_rate), reverse=True)
        return matched[:3]


class CodeGeneratorSkill(BaseSkill):  # [V2.0新增]
    """代码生成器 Skill

    基于实现项和资产生成代码
    """

    def __init__(self, config: Dict = None):
        super().__init__(config)
        self.templates_dir = self.config.get('templates_dir', 'prompt_templates')
        self.post_processors = self._init_post_processors()

    def _init_post_processors(self) -> List:
        """初始化后处理器"""
        processors = []
        processors_config = self.config.get('post_processors', [])

        for proc_config in processors_config:
            proc_type = proc_config.get('type')
            if proc_type == 'syntax_checker':
                processors.append(SyntaxChecker())
            elif proc_type == 'style_formatter':
                processors.append(StyleFormatter())

        return processors

    async def execute(self, context: SkillContext) -> Dict:
        """执行代码生成"""
        impl_items = context.get_implementation_items()
        assets = context.get_assets()

        if not impl_items:
            return {'status': 'error', 'message': 'No implementation items found'}

        generated_codes = []

        for item in impl_items:
            code = await self._generate_code_for_item(item, assets)
            generated_codes.append(code)

        context.set_generated_code(generated_codes)

        return {
            'status': 'success',
            'data': {
                'total_items': len(impl_items),
                'generated_files': len(generated_codes),
                'files': [{'id': c.id, 'path': c.file_path, 'language': c.language.value} for c in generated_codes]
            }
        }

    async def _generate_code_for_item(self, item: ImplementationItem, assets: List[CodeAsset]) -> GeneratedCode:
        """为单个实现项生成代码"""
        # 1. 加载模板
        template = self._load_template(item)

        # 2. 准备上下文
        template_context = self._prepare_context(item, assets)

        # 3. 生成代码（这里简化处理，实际应调用LLM）
        raw_code = self._render_template(template, template_context)

        # 4. 后处理
        processed_code = self._post_process(raw_code, item)

        # 5. 创建GeneratedCode对象
        code = GeneratedCode(
            id="",
            implementation_item_id=item.id,
            file_path=self._determine_file_path(item),
            language=item.language,
            content=processed_code,
            used_assets=[a.id for a in assets if a.id in item.required_assets]
        )

        # 6. 质量检查
        code.quality_score = self._assess_quality(code)
        code.syntax_valid = self._check_syntax(code)

        return code

    def _load_template(self, item: ImplementationItem) -> str:
        """加载代码模板 - 支持金蝶苍穹特定类型"""
        
        # 金蝶苍穹模板映射
        kingdee_template_map = {
            # 元数据模板
            ImplementationType.ENTITY_METADATA: 'kingdee/metadata/entity_metadata.yaml.template',
            ImplementationType.FORM_METADATA: 'kingdee/metadata/form_metadata.yaml.template',
            ImplementationType.SERVICE_METADATA: 'kingdee/metadata/service_metadata.yaml.template',
            
            # KingScript 模板
            ImplementationType.KINGSCRIPT_SERVICE: 'kingdee/kingscript/service_v2.ks.template',
            ImplementationType.KINGSCRIPT_OPERATION: 'kingdee/kingscript/service_v2.ks.template',
            ImplementationType.KINGSCRIPT_PLUGIN: 'kingdee/kingscript/form_plugin_v2.ks.template',
            
            # Java 插件模板
            ImplementationType.JAVA_FORM_PLUGIN: 'kingdee/java_plugin/FormPlugin_v2.java.template',
            ImplementationType.JAVA_LIST_PLUGIN: 'kingdee/java_plugin/ListPlugin.java.template',
            ImplementationType.JAVA_OPERATION_PLUGIN: 'kingdee/java_plugin/OperationPlugin.java.template',
            ImplementationType.JAVA_TRANSFORM_PLUGIN: 'kingdee/java_plugin/TransformPlugin.java.template',
            
            # 传统模板（向后兼容）
            (ImplementationType.JAVA_API, CodeLanguage.JAVA): 'java_api/controller.java',
            (ImplementationType.KOTLIN_PLUGIN, CodeLanguage.KOTLIN): 'kotlin_plugin/plugin.kt',
            (ImplementationType.SQL_REPORT, CodeLanguage.SQL): 'sql_report/query.sql',
        }
        
        # 根据实现类型获取模板
        if item.impl_type in kingdee_template_map:
            template_file = kingdee_template_map[item.impl_type]
        else:
            # 回退到传统映射
            template_file = kingdee_template_map.get(
                (item.impl_type, item.language), 
                'default.txt'
            )
        
        # 尝试多个路径查找模板
        possible_paths = [
            Path(self.templates_dir) / template_file,
            Path('knowledge') / template_file,
            Path(template_file),
        ]
        
        for template_path in possible_paths:
            if template_path.exists():
                with open(template_path, 'r', encoding='utf-8') as f:
                    return f.read()
        
        # 返回默认模板
        return self._get_default_template(item)

    def _get_default_template(self, item: ImplementationItem) -> str:
        """获取默认模板"""
        if item.language == CodeLanguage.JAVA:
            return """
public class {class_name} {{
    // TODO: Implement {description}

    {methods}
}}
"""
        elif item.language == CodeLanguage.KOTLIN:
            return """
class {class_name} : AbstractPlugin() {{
    override fun onEnable() {{
        // TODO: Implement {description}
    }}
}}
"""
        else:
            return "-- TODO: Implement {description}"

    def _prepare_context(self, item: ImplementationItem, assets: List[CodeAsset]) -> Dict:
        """准备模板上下文 - 支持金蝶苍穹特定字段"""
        
        # 基础上下文
        context = {
            'class_name': item.name,
            'description': item.description,
            'package': self._determine_package(item),
            'methods': self._generate_methods(item),
            'imports': self._generate_imports(item, assets),
            'assets': [a.content for a in assets if a.id in item.required_assets],
            # 金蝶特定字段
            'module_prefix': self._determine_module_prefix(item),
            'entity_name': item.name,
            'entity_name_cn': item.description[:20],
            'table_name': f"t_{item.related_module.lower()}_{item.name.lower()}",
            'extend_type': item.extend_type or 'new',
            'extend_target': item.extend_target or '',
            'parent_template': item.parent_template or '',
            'author': 'AI-Generated',
            'date': datetime.now().strftime('%Y-%m-%d'),
        }
        
        # 根据实现类型添加特定上下文
        if item.impl_type == ImplementationType.ENTITY_METADATA:
            context['fields'] = item.entity_definition.get('fields', [])
            context['indexes'] = item.entity_definition.get('indexes', [])
            
        elif item.impl_type == ImplementationType.FORM_METADATA:
            context['entity_id'] = item.entity_definition.get('id', '')
            context['layout_type'] = item.form_definition.get('layout_type', 'card')
            context['sections'] = item.form_definition.get('sections', [])
            context['plugins'] = item.form_definition.get('plugins', [])
            context['business_rules'] = item.form_definition.get('business_rules', [])
            
        elif item.impl_type in [ImplementationType.KINGSCRIPT_SERVICE, 
                                ImplementationType.KINGSCRIPT_OPERATION]:
            context['service_name'] = item.name
            context['service_id'] = f"{context['module_prefix']}.{item.name}"
            context['service_description'] = item.description
            context['operations'] = item.service_definition.get('operations', [])
            
        elif item.impl_type == ImplementationType.KINGSCRIPT_PLUGIN:
            context['plugin_name'] = item.name
            context['plugin_id'] = f"{context['module_prefix']}.{item.name}"
            context['default_fields'] = item.form_definition.get('default_fields', [])
            context['field_rules'] = item.form_definition.get('field_rules', [])
            context['validations'] = item.form_definition.get('validations', [])
            
        elif item.impl_type in [ImplementationType.JAVA_FORM_PLUGIN,
                                ImplementationType.JAVA_LIST_PLUGIN,
                                ImplementationType.JAVA_OPERATION_PLUGIN]:
            context['class_name'] = item.name
            context['plugin_description'] = item.description
            context['default_fields'] = item.form_definition.get('default_fields', [])
            context['validations'] = item.form_definition.get('validations', [])
            
        return context
    
    def _determine_module_prefix(self, item: ImplementationItem) -> str:
        """确定模块前缀"""
        module_map = {
            'HR_CORE': 'kingdee.hr.core',
            'HR_PAYROLL': 'kingdee.hr.payroll',
            'HR_ATTENDANCE': 'kingdee.hr.attendance',
            'HR_RECRUITMENT': 'kingdee.hr.recruitment',
        }
        return module_map.get(item.related_module, 'kingdee.hr')

    def _render_template(self, template: str, context: Dict) -> str:
        """渲染模板（简化版，实际应使用Jinja2）"""
        result = template
        for key, value in context.items():
            result = result.replace(f'{{{key}}}', str(value))
        return result

    def _post_process(self, code: str, item: ImplementationItem) -> str:
        """后处理代码"""
        for processor in self.post_processors:
            code = processor.process(code, item)
        return code

    def _determine_file_path(self, item: ImplementationItem) -> str:
        """确定文件路径 - 支持金蝶苍穹特定类型"""
        
        # 金蝶苍穹路径映射
        kingdee_path_map = {
            ImplementationType.ENTITY_METADATA: 'metadata/entities',
            ImplementationType.FORM_METADATA: 'metadata/forms',
            ImplementationType.SERVICE_METADATA: 'metadata/services',
            ImplementationType.KINGSCRIPT_SERVICE: 'scripts/services',
            ImplementationType.KINGSCRIPT_OPERATION: 'scripts/operations',
            ImplementationType.KINGSCRIPT_PLUGIN: 'scripts/plugins',
            ImplementationType.JAVA_FORM_PLUGIN: 'src/com/kingdee/hr/plugin/form',
            ImplementationType.JAVA_LIST_PLUGIN: 'src/com/kingdee/hr/plugin/list',
            ImplementationType.JAVA_OPERATION_PLUGIN: 'src/com/kingdee/hr/plugin/operation',
            ImplementationType.JAVA_TRANSFORM_PLUGIN: 'src/com/kingdee/hr/plugin/transform',
            ImplementationType.JAVA_EXTENSION_POINT: 'src/com/kingdee/hr/extension',
            ImplementationType.JAVA_EXTENSION_IMPL: 'src/com/kingdee/hr/extension/impl',
        }
        
        # 获取目录
        base_dir = kingdee_path_map.get(item.impl_type, 'src')
        
        # 获取扩展名
        ext_map = {
            CodeLanguage.JAVA: 'java',
            CodeLanguage.KOTLIN: 'kt',
            CodeLanguage.SQL: 'sql',
            CodeLanguage.YAML: 'yaml',
            CodeLanguage.XML: 'xml',
        }
        ext = ext_map.get(item.language, 'txt')
        
        # 特殊处理元数据类型
        if item.impl_type in [ImplementationType.ENTITY_METADATA,
                              ImplementationType.FORM_METADATA,
                              ImplementationType.SERVICE_METADATA]:
            ext = 'yaml'
        elif item.impl_type in [ImplementationType.KINGSCRIPT_SERVICE,
                                ImplementationType.KINGSCRIPT_OPERATION,
                                ImplementationType.KINGSCRIPT_PLUGIN]:
            ext = 'ks'
        
        return f"{base_dir}/{item.name}.{ext}"

    def _determine_package(self, item: ImplementationItem) -> str:
        """确定包名"""
        module_map = {
            'HR_CORE': 'com.kingdee.hr.core',
            'HR_PAYROLL': 'com.kingdee.hr.payroll',
            'HR_ATTENDANCE': 'com.kingdee.hr.attendance',
            'HR_RECRUITMENT': 'com.kingdee.hr.recruitment'
        }
        return module_map.get(item.related_module, 'com.kingdee.hr')

    def _generate_methods(self, item: ImplementationItem) -> str:
        """生成方法签名"""
        return "// Methods to be implemented"

    def _generate_imports(self, item: ImplementationItem, assets: List[CodeAsset]) -> str:
        """生成导入语句"""
        return "import java.util.*;"

    def _assess_quality(self, code: GeneratedCode) -> float:
        """评估代码质量"""
        score = 0.5  # 基础分

        # 代码长度适中
        lines = len(code.content.split('\n'))
        if 10 < lines < 500:
            score += 0.1

        # 包含注释
        if '//' in code.content or '/*' in code.content:
            score += 0.1

        # 语法有效
        if code.syntax_valid:
            score += 0.2

        # 使用了资产
        if code.used_assets:
            score += 0.1

        return min(score, 1.0)

    def _check_syntax(self, code: GeneratedCode) -> bool:
        """检查语法（简化版）"""
        content = code.content

        if code.language == CodeLanguage.JAVA:
            # 简单检查：类定义、括号匹配
            return 'class' in content and content.count('{') == content.count('}')
        elif code.language == CodeLanguage.KOTLIN:
            return 'class' in content or 'fun' in content
        elif code.language == CodeLanguage.SQL:
            return 'SELECT' in content.upper() or 'INSERT' in content.upper()

        return True


# 后处理器基类
class CodePostProcessor(ABC):
    """代码后处理器基类"""

    @abstractmethod
    def process(self, code: str, context: Any) -> str:
        pass


class SyntaxChecker(CodePostProcessor):
    """语法检查器"""

    def process(self, code: str, context: Any) -> str:
        # 简单语法检查逻辑
        return code


class StyleFormatter(CodePostProcessor):
    """风格格式化器"""

    def process(self, code: str, context: Any) -> str:
        # 简单格式化：统一缩进
        lines = code.split('\n')
        formatted = []
        indent = 0

        for line in lines:
            stripped = line.strip()
            if stripped.endswith('}'):
                indent = max(0, indent - 1)

            formatted.append('    ' * indent + stripped)

            if stripped.endswith('{'):
                indent += 1

        return '\n'.join(formatted)


# 全局注册表实例
registry = PluginRegistry()


class RequirementAnalyzerSkill(BaseSkill):
    """需求分析器 Skill"""

    async def execute(self, context: SkillContext) -> Dict:
        """执行需求分析"""
        user_input = context.user_input

        if not user_input:
            return {'status': 'error', 'message': 'No user input found'}

        # 创建需求对象
        req = Requirement(
            id="",
            name="用户输入需求",
            description=user_input,
            category="功能增强",
            related_module="HR_CORE",
            customization_type="标准扩展"
        )

        context.set_requirements([req])

        return {
            'status': 'success',
            'data': {
                'requirements_count': 1,
                'requirements': [{
                    'id': req.id,
                    'name': req.name,
                    'description': req.description
                }]
            }
        }


class CodeQualityCheckerSkill(BaseSkill):
    """代码质量检查器"""

    async def execute(self, context: SkillContext) -> Dict:
        generated_code = context.get_generated_code()

        # 简单的质量检查
        quality_report = {
            'total_files': len(generated_code),
            'passed': len(generated_code),
            'failed': 0,
            'details': []
        }

        for code in generated_code:
            quality_report['details'].append({
                'file': code.file_path,
                'quality_score': code.quality_score,
                'syntax_valid': code.syntax_valid
            })

        return {
            'status': 'success',
            'data': quality_report
        }


class EffortEstimatorSkill(BaseSkill):
    """工作量评估器"""

    async def execute(self, context: SkillContext) -> Dict:
        impl_items = context.get_implementation_items()

        estimation = {
            'total_items': len(impl_items),
            'total_days': len(impl_items) * 3,  # 简单估算：每项3天
            'total_cost': len(impl_items) * 3 * 3000,
            'details': []
        }

        for item in impl_items:
            estimation['details'].append({
                'name': item.name,
                'days': 3,
                'cost': 9000
            })

        return {
            'status': 'success',
            'data': estimation
        }


class RiskAssessorSkill(BaseSkill):
    """风险评估器"""

    async def execute(self, context: SkillContext) -> Dict:
        impl_items = context.get_implementation_items()

        risks = []
        for item in impl_items:
            # 根据实现类型和描述评估风险
            risk_level = '低'
            if item.impl_type.value.startswith('java_') or item.impl_type.value.startswith('kingscript_'):
                risk_level = '中'
            
            # 检查描述中的风险关键词
            high_risk_keywords = ['复杂', '高难度', '核心', '关键', '大量数据', '性能']
            if any(kw in item.description for kw in high_risk_keywords):
                risk_level = '高'
                risks.append({
                    'category': '技术',
                    'description': f'{item.name} 涉及复杂技术实现',
                    'level': risk_level
                })

        return {
            'status': 'success',
            'data': {
                'total_risks': len(risks),
                'risks': risks
            }
        }


class OutputAssemblerSkill(BaseSkill):
    """输出组装器"""

    async def execute(self, context: SkillContext) -> Dict:
        from datetime import datetime
        return {
            'status': 'success',
            'data': {
                'assembled': True,
                'timestamp': datetime.now().isoformat()
            }
        }
