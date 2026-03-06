"""
Pipeline执行器单元测试
"""
import pytest
import asyncio
from unittest.mock import Mock, patch
from core.pipeline_executor import (
    PipelineExecutor,
    PipelineConfig,
    PipelineStep,
    StepStatus,
    StepResult,
)
from core.data_models import SkillContext


class TestPipelineStep:
    """测试Pipeline步骤"""

    def test_step_creation(self):
        step = PipelineStep(
            id="test_step",
            skill="test_skill",
            output_key="test_output"
        )
        assert step.id == "test_step"
        assert step.skill == "test_skill"
        assert step.output_key == "test_output"
        assert step.parallel is False
        assert step.retry_count == 0

    def test_step_with_dependencies(self):
        step = PipelineStep(
            id="step2",
            skill="skill2",
            output_key="output2",
            depends_on=["step1"],
            parallel=True
        )
        assert step.depends_on == ["step1"]
        assert step.parallel is True


class TestPipelineConfig:
    """测试Pipeline配置"""

    def test_config_creation(self):
        steps = [
            PipelineStep(id="step1", skill="skill1", output_key="out1"),
            PipelineStep(id="step2", skill="skill2", output_key="out2"),
        ]
        config = PipelineConfig(
            name="test_pipeline",
            version="1.0.0",
            steps=steps
        )
        assert config.name == "test_pipeline"
        assert config.version == "1.0.0"
        assert len(config.steps) == 2


class TestStepResult:
    """测试步骤结果"""

    def test_result_creation(self):
        result = StepResult(
            step_id="step1",
            status=StepStatus.COMPLETED,
            output={"key": "value"},
            execution_time=1.5
        )
        assert result.step_id == "step1"
        assert result.status == StepStatus.COMPLETED
        assert result.output == {"key": "value"}
        assert result.execution_time == 1.5

    def test_failed_result(self):
        result = StepResult(
            step_id="step1",
            status=StepStatus.FAILED,
            error="Something went wrong"
        )
        assert result.status == StepStatus.FAILED
        assert result.error == "Something went wrong"


class TestPipelineExecutor:
    """测试Pipeline执行器"""

    def test_executor_creation(self):
        # 需要存在pipeline.yaml文件
        with patch('core.pipeline_executor.PipelineExecutor._load_config') as mock_load:
            mock_load.return_value = PipelineConfig(
                name="test",
                version="1.0",
                steps=[]
            )
            executor = PipelineExecutor("pipeline.yaml")
            assert executor.config.name == "test"

    def test_build_execution_order_linear(self):
        """测试线性依赖的执行顺序"""
        with patch('core.pipeline_executor.PipelineExecutor._load_config') as mock_load:
            steps = [
                PipelineStep(id="step1", skill="s1", output_key="o1"),
                PipelineStep(id="step2", skill="s2", output_key="o2", depends_on=["step1"]),
                PipelineStep(id="step3", skill="s3", output_key="o3", depends_on=["step2"]),
            ]
            mock_load.return_value = PipelineConfig(
                name="test",
                version="1.0",
                steps=steps
            )
            executor = PipelineExecutor("pipeline.yaml")
            order = executor._build_execution_order()
            
            assert len(order) == 3
            assert order[0][0].id == "step1"
            assert order[1][0].id == "step2"
            assert order[2][0].id == "step3"

    def test_build_execution_order_parallel(self):
        """测试并行执行的分组"""
        with patch('core.pipeline_executor.PipelineExecutor._load_config') as mock_load:
            steps = [
                PipelineStep(id="step1", skill="s1", output_key="o1"),
                PipelineStep(id="step2", skill="s2", output_key="o2", depends_on=["step1"], parallel=True),
                PipelineStep(id="step3", skill="s3", output_key="o3", depends_on=["step1"], parallel=True),
            ]
            mock_load.return_value = PipelineConfig(
                name="test",
                version="1.0",
                steps=steps
            )
            executor = PipelineExecutor("pipeline.yaml")
            order = executor._build_execution_order()
            
            assert len(order) == 2
            assert len(order[0]) == 1  # step1 单独
            assert len(order[1]) == 2  # step2, step3 并行
