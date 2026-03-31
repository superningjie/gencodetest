"""
核心数据模型单元测试
"""
import pytest
from datetime import datetime
from core.data_models import (
    Requirement,
    ImplementationItem,
    ImplementationType,
    CodeLanguage,
    CodeAsset,
    GeneratedCode,
    BatchJob,
    SkillContext,
)


class TestRequirement:
    """测试需求模型"""

    def test_requirement_creation(self):
        req = Requirement(
            id="",
            name="测试需求",
            description="这是一个测试需求",
            category="功能增强",
            related_module="HR_CORE",
            customization_type="标准扩展"
        )
        assert req.name == "测试需求"
        assert req.id.startswith("REQ_")
        assert req.priority == "中"
        assert req.confidence == 0.8

    def test_requirement_with_custom_id(self):
        req = Requirement(
            id="REQ_CUSTOM_001",
            name="自定义ID需求",
            description="测试",
            category="功能增强",
            related_module="HR_CORE",
            customization_type="标准扩展"
        )
        assert req.id == "REQ_CUSTOM_001"


class TestImplementationItem:
    """测试实现项模型"""

    def test_implementation_item_creation(self):
        item = ImplementationItem(
            id="",
            requirement_id="REQ_001",
            name="测试实现项",
            description="实现详情",
            impl_type=ImplementationType.JAVA_API,
            language=CodeLanguage.JAVA
        )
        assert item.impl_type == ImplementationType.JAVA_API
        assert item.language == CodeLanguage.JAVA
        assert item.id.startswith("IMPL_")
        assert item.complexity == "中"


class TestCodeAsset:
    """测试代码资产模型"""

    def test_code_asset_creation(self):
        asset = CodeAsset(
            id="",
            name="测试模板",
            asset_type="file_template",
            language=CodeLanguage.JAVA
        )
        assert asset.asset_type == "file_template"
        assert asset.id.startswith("ASSET_")
        assert asset.usage_count == 0
        assert asset.success_rate == 1.0


class TestGeneratedCode:
    """测试生成代码模型"""

    def test_generated_code_creation(self):
        code = GeneratedCode(
            id="",
            implementation_item_id="IMPL_001",
            file_path="src/Test.java",
            language=CodeLanguage.JAVA,
            content="public class Test {}"
        )
        assert code.file_path == "src/Test.java"
        assert code.language == CodeLanguage.JAVA
        assert code.status == "generated"
        assert code.id.startswith("CODE_")


class TestBatchJob:
    """测试批处理作业模型"""

    def test_batch_job_progress(self):
        job = BatchJob(
            job_id="BATCH_001",
            status="running",
            total_items=10,
            completed_items=5,
            failed_items=2
        )
        assert job.progress == 0.7  # (5+2)/10

    def test_batch_job_zero_total(self):
        job = BatchJob(
            job_id="BATCH_002",
            status="pending",
            total_items=0
        )
        assert job.progress == 0.0


class TestSkillContext:
    """测试Skill上下文"""

    def test_context_creation(self):
        ctx = SkillContext(
            session_id="test_session_001",
            user_input="测试输入"
        )
        assert ctx.session_id == "test_session_001"
        assert ctx.user_input == "测试输入"

    def test_requirements_storage(self):
        ctx = SkillContext(session_id="test")
        reqs = [
            Requirement(
                id="REQ_001",
                name="需求1",
                description="描述1",
                category="功能增强",
                related_module="HR_CORE",
                customization_type="标准扩展"
            )
        ]
        ctx.set_requirements(reqs)
        retrieved = ctx.get_requirements()
        assert len(retrieved) == 1
        assert retrieved[0].id == "REQ_001"

    def test_implementation_items_storage(self):
        ctx = SkillContext(session_id="test")
        items = [
            ImplementationItem(
                id="IMPL_001",
                requirement_id="REQ_001",
                name="实现项1",
                description="描述",
                impl_type=ImplementationType.JAVA_API,
                language=CodeLanguage.JAVA
            )
        ]
        ctx.set_implementation_items(items)
        retrieved = ctx.get_implementation_items()
        assert len(retrieved) == 1
        assert retrieved[0].impl_type == ImplementationType.JAVA_API

    def test_raw_data_access(self):
        ctx = SkillContext(session_id="test")
        ctx.set_raw("custom_key", "custom_value")
        assert ctx.get_raw("custom_key") == "custom_value"
        assert ctx.get_raw("nonexistent", "default") == "default"
