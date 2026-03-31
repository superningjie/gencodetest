"""
金蝶星瀚HR AI顾问系统 V2.0 - 定制化开发代码生成
主入口程序 [V1.1] [V2.0增强]
"""
import argparse
import asyncio
import yaml
import sys
from pathlib import Path
from typing import Dict, Any

from core.data_models import SkillContext, Requirement
from core.pipeline_executor import PipelineExecutor, BatchPipelineExecutor
from core.skill_engine import registry
from core.knowledge_base import get_knowledge_base


def setup_logging():
    """配置日志"""
    import logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler('logs/app.log', encoding='utf-8')
        ]
    )


def load_config(config_path: str = "config.yaml") -> Dict:
    """加载主配置"""
    with open(config_path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)


async def interactive_mode(pipeline_path: str):
    """交互模式"""
    print("\n" + "="*60)
    print("金蝶星瀚HR AI顾问系统 V2.0 - 交互模式")
    print("="*60)
    print("命令：")
    print("  generate <需求描述>  - 生成定制化开发代码")
    print("  batch <文件路径>     - 批量处理需求文件")
    print("  status <job_id>      - 查询批处理状态")
    print("  plugins              - 列出所有插件")
    print("  pipeline:info        - 显示Pipeline信息")
    print("  help                 - 显示帮助")
    print("  exit                 - 退出")
    print("="*60 + "\n")

    # 初始化
    registry.auto_discover_skills("skills")
    kb = get_knowledge_base()

    batch_executor = BatchPipelineExecutor(pipeline_path)

    while True:
        try:
            command = input("\nAI顾问 > ").strip()

            if not command:
                continue

            if command == "exit":
                print("再见！")
                break

            elif command == "help":
                print("可用命令：generate, batch, status, plugins, pipeline:info, exit")

            elif command == "plugins":
                skills = registry.list_skills()
                print(f"\n已注册 {len(skills)} 个Skill：")
                for skill in skills:
                    print(f"  - {skill}")

            elif command == "pipeline:info":
                executor = PipelineExecutor(pipeline_path)
                print(f"\nPipeline: {executor.config.name}")
                print(f"版本: {executor.config.version}")
                print(f"步骤数: {len(executor.config.steps)}")
                print("\n执行流程：")
                for step in executor.config.steps:
                    print(f"  {step.id}: {step.skill} -> {step.output_key}")

            elif command.startswith("generate "):
                description = command[9:].strip()
                await generate_code(description, pipeline_path)

            elif command.startswith("batch "):
                file_path = command[6:].strip()
                await batch_process(file_path, pipeline_path, batch_executor)

            elif command.startswith("status "):
                job_id = command[7:].strip()
                job = batch_executor.get_job_status(job_id)
                if job:
                    print(f"\n作业 {job_id}:")
                    print(f"  状态: {job.status}")
                    print(f"  进度: {job.completed_items}/{job.total_items} ({job.progress*100:.1f}%)")
                    print(f"  失败: {job.failed_items}")
                else:
                    print(f"未找到作业: {job_id}")

            else:
                print(f"未知命令: {command}")

        except KeyboardInterrupt:
            print("\n再见！")
            break
        except Exception as e:
            print(f"错误: {e}")


async def generate_code(description: str, pipeline_path: str):
    """生成代码"""
    print(f"\n正在分析需求: {description[:50]}...")

    # 创建上下文
    context = SkillContext(
        session_id=f"session_{id(description)}",
        user_input=description
    )

    # 创建需求对象
    req = Requirement(
        id="",
        name="定制化开发需求",
        description=description,
        category="功能增强",
        related_module="HR_CORE",
        customization_type="标准扩展"
    )
    context.set_requirements([req])

    # 执行Pipeline
    executor = PipelineExecutor(pipeline_path)
    result = await executor.execute(context)

    # 显示结果
    print("\n" + "="*60)
    print("生成结果")
    print("="*60)

    if result['status'] == 'completed':
        print(f"✅ Pipeline执行成功")

        # 显示生成的代码文件
        generated_code = context.get_generated_code()
        if generated_code:
            print(f"\n生成了 {len(generated_code)} 个文件：")
            for code in generated_code:
                print(f"  📄 {code.file_path} (质量分: {code.quality_score:.2f})")

                # 保存到输出目录
                output_dir = Path("outputs/generated_code") / context.session_id
                output_dir.mkdir(parents=True, exist_ok=True)

                file_path = output_dir / code.file_path.replace('/', '_')
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(code.content)

                print(f"     已保存到: {file_path}")
    else:
        print(f"⚠️ Pipeline部分完成")
        print(f"成功: {result['summary']['completed']}")
        print(f"失败: {result['summary']['failed']}")

    print("="*60)


async def batch_process(file_path: str, pipeline_path: str, executor: BatchPipelineExecutor):
    """批量处理"""
    import json

    print(f"\n加载批量需求文件: {file_path}")

    with open(file_path, 'r', encoding='utf-8') as f:
        items = json.load(f)

    print(f"找到 {len(items)} 个需求项")

    # 提交批处理作业
    job_id = await executor.submit_job(items)

    print(f"✅ 批处理作业已提交: {job_id}")
    print(f"使用 'status {job_id}' 查询进度")


async def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='金蝶星瀚HR AI顾问系统 V2.0')
    parser.add_argument('--pipeline', default='pipeline.yaml', help='Pipeline配置文件路径')
    parser.add_argument('--config', default='config.yaml', help='主配置文件路径')
    parser.add_argument('--generate', help='直接生成代码（传入需求描述）')
    parser.add_argument('--batch', help='批量处理（传入需求文件路径）')
    parser.add_argument('--interactive', action='store_true', help='交互模式')

    args = parser.parse_args()

    # 设置日志
    setup_logging()

    # 确保目录存在
    Path("logs").mkdir(exist_ok=True)
    Path("outputs/generated_code").mkdir(parents=True, exist_ok=True)

    # 自动发现Skills
    registry.auto_discover_skills("skills")

    if args.interactive or (not args.generate and not args.batch):
        # 交互模式
        await interactive_mode(args.pipeline)
    elif args.generate:
        # 直接生成
        await generate_code(args.generate, args.pipeline)
    elif args.batch:
        # 批量处理
        batch_executor = BatchPipelineExecutor(args.pipeline)
        await batch_process(args.batch, args.pipeline, batch_executor)


if __name__ == "__main__":
    asyncio.run(main())
