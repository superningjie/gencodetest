#!/usr/bin/env python3
"""
配置转换脚本
将Markdown格式的配置文档转换为YAML格式供系统使用
"""
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from core.config_loader import MarkdownConfigLoader, convert_md_to_yaml

if __name__ == '__main__':
    knowledge_docs_dir = "knowledge_docs"
    output_dir = "."

    print("=" * 60)
    print("配置文档转换工具")
    print("=" * 60)
    print(f"\n源目录: {knowledge_docs_dir}")
    print(f"输出目录: {output_dir}\n")

    convert_md_to_yaml(knowledge_docs_dir, output_dir)

    print("\n" + "=" * 60)
    print("转换完成！")
    print("=" * 60)
