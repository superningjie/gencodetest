"""
Pipeline配置化执行引擎 [V1.1] [V2.0增强]
支持条件执行、状态管理、并行执行
"""
import yaml
import asyncio
from typing import Dict, List, Any, Optional
from pathlib import Path
from dataclasses import dataclass, field
from enum import Enum
import logging

from core.data_models import SkillContext, BatchJob
from core.skill_engine import registry, BaseSkill

logger = logging.getLogger(__name__)


class StepStatus(Enum):
    """步骤状态"""
    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"
    SKIPPED = "skipped"


@dataclass
class StepResult:
    """步骤执行结果"""
    step_id: str
    status: StepStatus
    output: Dict = field(default_factory=dict)
    error: Optional[str] = None
    execution_time: float = 0.0


@dataclass
class PipelineStep:
    """Pipeline步骤定义"""
    id: str
    skill: str
    input_key: Optional[str] = None
    output_key: str = ""
    config: Dict = field(default_factory=dict)
    condition: Optional[str] = None  # 执行条件
    parallel: bool = False           # 是否并行执行
    depends_on: List[str] = field(default_factory=list)  # 依赖步骤
    retry_count: int = 0             # 重试次数
    timeout: int = 300               # 超时时间（秒）


@dataclass
class PipelineConfig:
    """Pipeline配置"""
    name: str
    version: str
    steps: List[PipelineStep]
    global_config: Dict = field(default_factory=dict)


class PipelineExecutor:
    """Pipeline执行器 [V1.1] [V2.0增强]"""

    def __init__(self, config_path: str):
        self.config_path = config_path
        self.config = self._load_config()
        self.results: Dict[str, StepResult] = {}
        self.context: Optional[SkillContext] = None

    def _load_config(self) -> PipelineConfig:
        """加载Pipeline配置"""
        with open(self.config_path, 'r', encoding='utf-8') as f:
            data = yaml.safe_load(f)

        pipeline_data = data.get('pipeline', {})

        steps = []
        for step_data in pipeline_data.get('steps', []):
            steps.append(PipelineStep(
                id=step_data['id'],
                skill=step_data['skill'],
                input_key=step_data.get('input'),
                output_key=step_data.get('output_key', step_data['id']),
                config=step_data.get('config', {}),
                condition=step_data.get('condition'),
                parallel=step_data.get('parallel', False),
                depends_on=step_data.get('depends_on', []),
                retry_count=step_data.get('retry_count', 0),
                timeout=step_data.get('timeout', 300)
            ))

        return PipelineConfig(
            name=pipeline_data.get('name', 'unnamed'),
            version=pipeline_data.get('version', '1.0'),
            steps=steps,
            global_config=pipeline_data.get('global_config', {})
        )

    async def execute(self, initial_context: SkillContext) -> Dict[str, Any]:
        """执行Pipeline"""
        self.context = initial_context
        self.results = {}

        logger.info(f"Starting pipeline: {self.config.name} v{self.config.version}")

        # 构建依赖图
        execution_order = self._build_execution_order()

        # 按顺序执行
        for step_group in execution_order:
            if len(step_group) > 1 and all(s.parallel for s in step_group):
                # 并行执行
                await self._execute_parallel(step_group)
            else:
                # 串行执行
                for step in step_group:
                    await self._execute_step(step)

        # 汇总结果
        return self._compile_results()

    def _build_execution_order(self) -> List[List[PipelineStep]]:
        """构建执行顺序（拓扑排序）"""
        # 简化的执行顺序：按依赖关系分组
        executed = set()
        order = []

        while len(executed) < len(self.config.steps):
            group = []
            for step in self.config.steps:
                if step.id in executed:
                    continue
                # 检查依赖是否已满足
                if all(dep in executed for dep in step.depends_on):
                    group.append(step)
                    executed.add(step.id)

            if group:
                order.append(group)
            else:
                # 存在循环依赖
                raise ValueError("Circular dependency detected in pipeline")

        return order

    async def _execute_step(self, step: PipelineStep) -> StepResult:
        """执行单个步骤"""
        import time
        start_time = time.time()

        logger.info(f"Executing step: {step.id} (skill: {step.skill})")

        # 检查条件
        if step.condition and not self._evaluate_condition(step.condition):
            result = StepResult(
                step_id=step.id,
                status=StepStatus.SKIPPED,
                output={}
            )
            self.results[step.id] = result
            return result

        # 准备输入
        if step.input_key:
            input_data = self._get_input_data(step.input_key)
            self.context.set_raw('current_input', input_data)

        # 获取Skill
        skill_class = registry.get_skill(step.skill)
        if not skill_class:
            error_msg = f"Skill not found: {step.skill}"
            logger.error(error_msg)
            result = StepResult(
                step_id=step.id,
                status=StepStatus.FAILED,
                error=error_msg
            )
            self.results[step.id] = result
            return result

        # 执行Skill
        skill = skill_class(step.config)
        retry = 0

        while retry <= step.retry_count:
            try:
                # 设置超时
                output = await asyncio.wait_for(
                    skill.execute(self.context),
                    timeout=step.timeout
                )

                execution_time = time.time() - start_time

                if output.get('status') == 'error':
                    raise Exception(output.get('message', 'Unknown error'))

                result = StepResult(
                    step_id=step.id,
                    status=StepStatus.COMPLETED,
                    output=output.get('data', {}),
                    execution_time=execution_time
                )

                self.results[step.id] = result
                logger.info(f"Step {step.id} completed in {execution_time:.2f}s")
                return result

            except asyncio.TimeoutError:
                logger.warning(f"Step {step.id} timeout (attempt {retry + 1})")
                retry += 1
            except Exception as e:
                logger.error(f"Step {step.id} failed: {e}")
                if retry >= step.retry_count:
                    execution_time = time.time() - start_time
                    result = StepResult(
                        step_id=step.id,
                        status=StepStatus.FAILED,
                        error=str(e),
                        execution_time=execution_time
                    )
                    self.results[step.id] = result
                    return result
                retry += 1

        return self.results[step.id]

    async def _execute_parallel(self, steps: List[PipelineStep]):
        """并行执行多个步骤"""
        tasks = [self._execute_step(step) for step in steps]
        await asyncio.gather(*tasks, return_exceptions=True)

    def _evaluate_condition(self, condition: str) -> bool:
        """评估条件表达式（简化版）"""
        # 支持简单的条件：step_id.status == 'completed'
        try:
            if '==' in condition:
                left, right = condition.split('==')
                left = left.strip()
                right = right.strip().strip("'")

                if '.' in left:
                    step_id, attr = left.split('.')
                    if step_id in self.results:
                        result = self.results[step_id]
                        if attr == 'status':
                            return result.status.value == right

            # 默认通过
            return True
        except:
            return True

    def _get_input_data(self, input_key) -> Any:
        """获取输入数据"""
        # 如果input_key是dict（复杂输入），直接返回
        if isinstance(input_key, dict):
            result = {}
            for key, value in input_key.items():
                result[key] = self._get_input_data(value)
            return result

        # 如果input_key不是字符串，直接返回
        if not isinstance(input_key, str):
            return input_key

        # 支持引用其他步骤的输出：step_id.output_field
        if '.' in input_key:
            step_id, field = input_key.split('.', 1)
            if step_id in self.results:
                output = self.results[step_id].output
                # 支持嵌套访问：step_id.data.items
                keys = field.split('.')
                value = output
                for key in keys:
                    if isinstance(value, dict):
                        value = value.get(key)
                    else:
                        return None
                return value

        # 从context获取
        return self.context.get_raw(input_key)

    def _compile_results(self) -> Dict[str, Any]:
        """编译执行结果"""
        return {
            'pipeline_name': self.config.name,
            'version': self.config.version,
            'status': 'completed' if all(
                r.status == StepStatus.COMPLETED for r in self.results.values()
            ) else 'partial',
            'steps': {
                step_id: {
                    'status': result.status.value,
                    'output': result.output,
                    'error': result.error,
                    'execution_time': result.execution_time
                }
                for step_id, result in self.results.items()
            },
            'summary': {
                'total': len(self.config.steps),
                'completed': sum(1 for r in self.results.values() if r.status == StepStatus.COMPLETED),
                'failed': sum(1 for r in self.results.values() if r.status == StepStatus.FAILED),
                'skipped': sum(1 for r in self.results.values() if r.status == StepStatus.SKIPPED)
            }
        }


class BatchPipelineExecutor:
    """批处理Pipeline执行器 [V1.1] [V2.0增强]"""

    def __init__(self, pipeline_config_path: str, max_concurrent: int = 3):
        self.pipeline_config_path = pipeline_config_path
        self.max_concurrent = max_concurrent
        self.jobs: Dict[str, BatchJob] = {}

    async def submit_job(self, items: List[Dict], global_context: Dict = None) -> str:
        """提交批处理作业"""
        import uuid
        job_id = f"BATCH_{uuid.uuid4().hex[:8].upper()}"

        job = BatchJob(
            job_id=job_id,
            status='pending',
            total_items=len(items)
        )
        self.jobs[job_id] = job

        # 异步执行
        asyncio.create_task(self._execute_batch(job, items, global_context or {}))

        return job_id

    async def _execute_batch(self, job: BatchJob, items: List[Dict], global_context: Dict):
        """执行批处理"""
        job.status = 'running'

        semaphore = asyncio.Semaphore(self.max_concurrent)

        async def process_item(item: Dict):
            async with semaphore:
                try:
                    # 创建上下文
                    context = SkillContext(
                        session_id=f"{job.job_id}_{item.get('id', 'unknown')}",
                        user_input=item.get('description', ''),
                        pipeline_config=global_context
                    )

                    # 设置需求
                    from core.data_models import Requirement
                    req = Requirement(
                        id=item.get('id', ''),
                        name=item.get('name', ''),
                        description=item.get('description', ''),
                        category=item.get('category', '功能增强'),
                        related_module=item.get('module', 'HR_CORE'),
                        customization_type=item.get('type', '标准扩展')
                    )
                    context.set_requirements([req])

                    # 执行Pipeline
                    executor = PipelineExecutor(self.pipeline_config_path)
                    result = await executor.execute(context)

                    # 保存结果
                    await self._save_result(job.job_id, item.get('id'), result)

                    job.completed_items += 1

                except Exception as e:
                    logger.error(f"Batch item failed: {e}")
                    job.failed_items += 1

        # 并发处理
        await asyncio.gather(*[process_item(item) for item in items])

        job.status = 'completed' if job.failed_items == 0 else 'partial'

        # 保存作业结果
        await self._save_job_result(job)

    async def _save_result(self, job_id: str, item_id: str, result: Dict):
        """保存单个结果"""
        import json
        from datetime import datetime

        output_dir = Path(f"logs/batches/{job_id}")
        output_dir.mkdir(parents=True, exist_ok=True)

        result_file = output_dir / f"{item_id}.json"
        with open(result_file, 'w', encoding='utf-8') as f:
            json.dump({
                'item_id': item_id,
                'timestamp': datetime.now().isoformat(),
                'result': result
            }, f, ensure_ascii=False, indent=2)

    async def _save_job_result(self, job: BatchJob):
        """保存作业汇总结果"""
        import json
        from datetime import datetime

        summary = {
            'job_id': job.job_id,
            'status': job.status,
            'total': job.total_items,
            'completed': job.completed_items,
            'failed': job.failed_items,
            'progress': job.progress,
            'completed_at': datetime.now().isoformat()
        }

        output_dir = Path(f"logs/batches/{job.job_id}")
        with open(output_dir / "summary.json", 'w', encoding='utf-8') as f:
            json.dump(summary, f, ensure_ascii=False, indent=2)

    def get_job_status(self, job_id: str) -> Optional[BatchJob]:
        """获取作业状态"""
        return self.jobs.get(job_id)
