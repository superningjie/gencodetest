#!/usr/bin/env python3
"""
Web UI 服务
提供HTTP API接口供前端调用
"""
import os
import sys
import json
import asyncio
from pathlib import Path
from datetime import datetime
from flask import Flask, render_template, request, jsonify, send_from_directory
from flask_cors import CORS

# 添加父目录到路径
sys.path.append(str(Path(__file__).parent.parent))

from core.data_models import SkillContext, Requirement
from core.pipeline_executor import PipelineExecutor, BatchPipelineExecutor
from core.skill_engine import registry

app = Flask(__name__, 
    template_folder='templates',
    static_folder='static'
)
CORS(app)

# 配置
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB

# 全局状态
pipeline_path = str(Path(__file__).parent.parent / "pipeline.yaml")
executor = PipelineExecutor(pipeline_path)
batch_executor = BatchPipelineExecutor(pipeline_path)


@app.route('/')
def index():
    """主页面"""
    return render_template('index.html')


@app.route('/api/generate', methods=['POST'])
def generate_code():
    """代码生成API"""
    try:
        data = request.get_json()
        requirement_text = data.get('requirement', '').strip()
        session_id = data.get('session_id', f"web_{datetime.now().timestamp()}")

        if not requirement_text:
            return jsonify({
                'success': False,
                'error': '需求描述不能为空'
            }), 400

        # 创建上下文
        context = SkillContext(
            session_id=session_id,
            user_input=requirement_text
        )

        # 创建需求对象
        req = Requirement(
            id="",
            name="Web需求",
            description=requirement_text,
            category="功能增强",
            related_module="HR_CORE",
            customization_type="标准扩展"
        )
        context.set_requirements([req])

        # 执行Pipeline
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        result = loop.run_until_complete(executor.execute(context))
        loop.close()

        # 格式化结果
        generated_code = context.get_generated_code()
        implementation_items = context.get_implementation_items()

        response_text = format_generation_result(
            result, 
            generated_code, 
            implementation_items
        )

        return jsonify({
            'success': True,
            'result': response_text,
            'data': {
                'pipeline_status': result.get('status'),
                'generated_files': len(generated_code),
                'implementations': [item.name for item in implementation_items]
            }
        })

    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/batch', methods=['POST'])
def batch_generate():
    """批量生成API"""
    try:
        data = request.get_json()
        items = data.get('items', [])

        if not items:
            return jsonify({
                'success': False,
                'error': '需求列表不能为空'
            }), 400

        # 提交批处理作业
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        job_id = loop.run_until_complete(batch_executor.submit_job(items))
        loop.close()

        return jsonify({
            'success': True,
            'job_id': job_id,
            'message': f'批处理作业已提交，共{len(items)}个需求'
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/batch/status/<job_id>', methods=['GET'])
def batch_status(job_id):
    """查询批处理状态"""
    try:
        job = batch_executor.get_job_status(job_id)
        if job:
            return jsonify({
                'success': True,
                'data': {
                    'job_id': job.job_id,
                    'status': job.status,
                    'total': job.total_items,
                    'completed': job.completed_items,
                    'failed': job.failed_items,
                    'progress': job.progress
                }
            })
        else:
            return jsonify({
                'success': False,
                'error': '作业不存在'
            }), 404
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


def format_generation_result(result, generated_code, implementation_items):
    """格式化生成结果"""
    lines = []

    # 执行摘要
    lines.append("## 生成完成 ✓")
    lines.append("")

    # 拆分结果
    if implementation_items:
        lines.append(f"**需求拆分**: 共识别 {len(implementation_items)} 个实现项")
        for item in implementation_items:
            lines.append(f"- {item.name} ({item.impl_type.value}, {item.language.value})")
        lines.append("")

    # 生成的代码
    if generated_code:
        lines.append(f"**生成文件**: {len(generated_code)} 个")
        lines.append("")

        for code in generated_code:
            lines.append(f"### {code.file_path}")
            lines.append(f"```java")  # 简化处理，实际应根据language判断
            lines.append(code.content)
            lines.append("```")
            lines.append("")

    # Pipeline状态
    summary = result.get('summary', {})
    lines.append("**执行统计**:")
    lines.append(f"- 总步骤: {summary.get('total', 0)}")
    lines.append(f"- 成功: {summary.get('completed', 0)}")
    lines.append(f"- 失败: {summary.get('failed', 0)}")

    return "\n".join(lines)


if __name__ == '__main__':
    # 注册Skills
    registry.auto_discover_skills(str(Path(__file__).parent.parent / "skills"))

    print("=" * 60)
    print("金蝶星瀚HR AI顾问 - Web服务")
    print("=" * 60)
    print(f"访问地址: http://localhost:5000")
    print("=" * 60)

    app.run(
        host='0.0.0.0',
        port=5000,
        debug=True,
        use_reloader=False
    )
