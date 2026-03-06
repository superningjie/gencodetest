#!/usr/bin/env python3
"""
Markdown配置加载器
支持从Markdown文档解析配置信息
"""
import re
import yaml
from pathlib import Path
from typing import Dict, List, Any, Optional


class MarkdownConfigLoader:
    """Markdown配置加载器"""

    @staticmethod
    def parse_skill_md(md_path: str) -> Dict:
        """解析Skill的Markdown文档"""
        with open(md_path, 'r', encoding='utf-8') as f:
            content = f.read()

        config = {
            'skill_id': '',
            'name': '',
            'version': '1.0',
            'type': 'default',
            'custom_class': '',
            'description': '',
            'config': {},
            'inputs': [],
            'outputs': []
        }

        # 解析标题
        title_match = re.search(r'# Skill:\s*(\w+)', content)
        if title_match:
            config['skill_id'] = title_match.group(1)

        # 解析基本信息表格 - 使用简单的方式
        lines = content.split('\n')
        in_basic_table = False

        for i, line in enumerate(lines):
            if '## 基本信息' in line:
                in_basic_table = True
                continue
            if in_basic_table:
                if line.startswith('## '):
                    break
                if '|' in line and '属性' not in line and '---' not in line:
                    parts = [p.strip() for p in line.split('|')]
                    if len(parts) >= 3:
                        key = parts[1].replace('*', '').replace('**', '').strip()
                        value = parts[2].strip()
                        if key == 'Skill ID':
                            config['skill_id'] = value
                        elif key == '名称':
                            config['name'] = value
                        elif key == '版本':
                            config['version'] = value
                        elif key == '类型':
                            config['type'] = value
                        elif key == '实现类':
                            config['custom_class'] = value.replace('`', '')

        # 解析功能描述
        desc_match = re.search(r'## 功能描述\s*\n\s*([^#]+)', content)
        if desc_match:
            config['description'] = desc_match.group(1).strip()

        return config

    @staticmethod
    def parse_pipeline_md(md_path: str) -> Dict:
        """解析Pipeline的Markdown文档"""
        with open(md_path, 'r', encoding='utf-8') as f:
            content = f.read()

        config = {
            'pipeline': {
                'name': '',
                'version': '1.0.0',
                'steps': []
            }
        }

        # 解析基本信息
        lines = content.split('\n')
        in_info_table = False

        for line in lines:
            if '## 基本信息' in line:
                in_info_table = True
                continue
            if in_info_table and line.startswith('## '):
                break
            if in_info_table and '|' in line:
                parts = [p.strip() for p in line.split('|')]
                if len(parts) >= 3:
                    key = parts[1].replace('*', '').strip()
                    value = parts[2].strip()
                    if 'Pipeline名称' in key or '名称' in key:
                        config['pipeline']['name'] = value
                    elif '版本' in key:
                        config['pipeline']['version'] = value

        # 解析步骤 - 简单方式
        step_sections = content.split('### Step ')

        for section in step_sections[1:]:  # 跳过第一个空部分
            lines = section.split('\n')
            step_name = lines[0].strip()

            step = {
                'id': '',
                'skill': '',
                'output_key': '',
                'config': {}
            }

            in_step_table = False
            in_config_table = False

            for line in lines[1:]:
                if '##' in line and 'Step' not in line:
                    break

                if '| 属性 | 值 |' in line or '| 属性 | 值 |' in line:
                    in_step_table = True
                    continue

                if in_step_table and '|' in line and '---' not in line:
                    parts = [p.strip() for p in line.split('|')]
                    if len(parts) >= 3:
                        key = parts[1].replace('*', '').strip()
                        value = parts[2].strip()

                        if key == 'ID':
                            step['id'] = value
                            step['output_key'] = value
                        elif key == 'Skill':
                            step['skill'] = value
                        elif key == '输入':
                            if value.startswith('{{') and value.endswith('}}'):
                                step['input'] = value
                        elif key == '依赖':
                            if value:
                                step['depends_on'] = [v.strip() for v in value.split(',')]
                        elif key == '并行':
                            if '是' in value or 'yes' in value.lower():
                                step['parallel'] = True

            if step['id']:
                config['pipeline']['steps'].append(step)

        return config

    @staticmethod
    def load_all_skills(knowledge_docs_dir: str) -> Dict[str, Dict]:
        """加载所有Skill配置"""
        skills_dir = Path(knowledge_docs_dir) / "skills"
        skills = {}

        if not skills_dir.exists():
            return skills

        for md_file in skills_dir.glob("*.md"):
            config = MarkdownConfigLoader.parse_skill_md(str(md_file))
            if config['skill_id']:
                skills[config['skill_id']] = config

        return skills

    @staticmethod
    def load_pipeline(knowledge_docs_dir: str) -> Optional[Dict]:
        """加载Pipeline配置"""
        pipeline_file = Path(knowledge_docs_dir) / "pipeline" / "pipeline.md"

        if not pipeline_file.exists():
            return None

        return MarkdownConfigLoader.parse_pipeline_md(str(pipeline_file))


def convert_md_to_yaml(knowledge_docs_dir: str, output_dir: str):
    """将Markdown配置转换为YAML"""
    import os

    os.makedirs(output_dir, exist_ok=True)

    # 转换Skills
    skills = MarkdownConfigLoader.load_all_skills(knowledge_docs_dir)
    for skill_id, config in skills.items():
        skill_dir = Path(output_dir) / "skills" / skill_id
        skill_dir.mkdir(parents=True, exist_ok=True)

        yaml_path = skill_dir / "skill.yaml"
        with open(yaml_path, 'w', encoding='utf-8') as f:
            yaml.dump(config, f, allow_unicode=True, sort_keys=False)

    # 转换Pipeline
    pipeline = MarkdownConfigLoader.load_pipeline(knowledge_docs_dir)
    if pipeline:
        yaml_path = Path(output_dir) / "pipeline.yaml"
        with open(yaml_path, 'w', encoding='utf-8') as f:
            yaml.dump(pipeline, f, allow_unicode=True, sort_keys=False)

    print(f"已转换 {len(skills)} 个Skill配置")
    if pipeline:
        print(f"已转换Pipeline配置")


if __name__ == '__main__':
    # 测试
    loader = MarkdownConfigLoader()

    # 解析示例
    test_md = """# Skill: test_skill

## 基本信息

| 属性 | 值 |
|-----|-----|
| **Skill ID** | test_skill |
| **名称** | 测试Skill |
| **版本** | 1.0 |

## 功能描述

这是一个测试Skill
"""

    # 保存测试文件
    test_path = "/tmp/test_skill.md"
    with open(test_path, 'w') as f:
        f.write(test_md)

    config = loader.parse_skill_md(test_path)
    print("解析结果:", config)
