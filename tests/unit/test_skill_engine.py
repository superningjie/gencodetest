"""
Skill引擎单元测试
"""
import pytest
import asyncio
from core.skill_engine import (
    BaseSkill,
    PluginRegistry,
    registry,
    DefaultSkill,
    ImplementSplitterSkill,
    AssetMatcherSkill,
    CodeGeneratorSkill,
)
from core.data_models import SkillContext, Requirement


class TestPluginRegistry:
    """测试插件注册中心"""

    def test_singleton_pattern(self):
        reg1 = PluginRegistry()
        reg2 = PluginRegistry()
        assert reg1 is reg2

    def test_skill_registration(self):
        reg = PluginRegistry()
        reg.register('skill', 'test_skill', DefaultSkill)
        assert 'test_skill' in reg.list_skills()
        assert reg.get_skill('test_skill') == DefaultSkill

    def test_list_skills(self):
        reg = PluginRegistry()
        initial_count = len(reg.list_skills())
        reg.register('skill', f'test_skill_{initial_count}', DefaultSkill)
        assert len(reg.list_skills()) == initial_count + 1


class TestDefaultSkill:
    """测试默认Skill"""

    @pytest.mark.asyncio
    async def test_default_skill_execution(self):
        skill = DefaultSkill()
        ctx = SkillContext(session_id="test")
        result = await skill.execute(ctx)
        assert result['status'] == 'success'
        assert 'Default skill executed' in result['message']

    def test_skill_validation(self):
        skill = DefaultSkill()
        assert skill.validate_config() is True


class TestImplementSplitterSkill:
    """测试实现拆分器Skill"""

    @pytest.mark.asyncio
    async def test_split_requirement_no_requirements(self):
        skill = ImplementSplitterSkill()
        ctx = SkillContext(session_id="test")
        result = await skill.execute(ctx)
        assert result['status'] == 'error'
        assert 'No requirements found' in result['message']

    @pytest.mark.asyncio
    async def test_split_api_development(self):
        skill = ImplementSplitterSkill()
        ctx = SkillContext(session_id="test")
        req = Requirement(
            id="REQ_001",
            name="API开发需求",
            description="开发一个用户查询接口",
            category="接口开发",
            related_module="HR_CORE",
            customization_type="标准扩展"
        )
        ctx.set_requirements([req])
        result = await skill.execute(ctx)
        assert result['status'] == 'success'
        assert result['data']['total_requirements'] == 1
        assert result['data']['total_implementations'] >= 1


class TestAssetMatcherSkill:
    """测试资产匹配器Skill"""

    @pytest.mark.asyncio
    async def test_match_no_implementations(self):
        skill = AssetMatcherSkill()
        ctx = SkillContext(session_id="test")
        result = await skill.execute(ctx)
        assert result['status'] == 'error'
        assert 'No implementation items found' in result['message']


class TestCodeGeneratorSkill:
    """测试代码生成器Skill"""

    @pytest.mark.asyncio
    async def test_generate_no_implementations(self):
        skill = CodeGeneratorSkill()
        ctx = SkillContext(session_id="test")
        result = await skill.execute(ctx)
        assert result['status'] == 'error'
        assert 'No implementation items found' in result['message']
