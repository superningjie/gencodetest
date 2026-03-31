"""
文件化知识库 [V1.1] [V2.0增强]
支持业务知识、技术规范、可复用资产三层检索
"""
import os
import yaml
import json
import re
from typing import Dict, List, Any, Optional, Tuple
from pathlib import Path
from dataclasses import dataclass
import logging

logger = logging.getLogger(__name__)


@dataclass
class KnowledgeItem:
    """知识项"""
    id: str
    category: str           # business, technical, asset
    subcategory: str        # hr_products, dev_guidelines, reusable_assets
    source_file: str
    content: str
    metadata: Dict
    relevance_score: float = 0.0


class KnowledgeBase:
    """知识库管理器 [V1.1] [V2.0增强]"""

    def __init__(self, knowledge_dir: str = "knowledge"):
        self.knowledge_dir = Path(knowledge_dir)
        self.index: Dict[str, List[KnowledgeItem]] = {
            'business': [],      # 业务知识
            'technical': [],     # 技术规范
            'asset': []          # 可复用资产
        }
        self.inverted_index: Dict[str, List[str]] = {}  # 倒排索引
        self._loaded = False

    def load(self):
        """加载所有知识"""
        if self._loaded:
            return

        logger.info("Loading knowledge base...")

        # 加载业务知识
        self._load_business_knowledge()

        # 加载技术规范
        self._load_technical_guidelines()

        # 加载可复用资产
        self._load_reusable_assets()

        # 构建倒排索引
        self._build_inverted_index()

        self._loaded = True
        logger.info(f"Knowledge base loaded: {sum(len(v) for v in self.index.values())} items")

    def _load_business_knowledge(self):
        """加载业务知识（HR产品模块）"""
        hr_products_dir = self.knowledge_dir / "hr_products"

        if not hr_products_dir.exists():
            return

        for yaml_file in hr_products_dir.glob("*.yaml"):
            try:
                with open(yaml_file, 'r', encoding='utf-8') as f:
                    data = yaml.safe_load(f)

                module = data.get('module', yaml_file.stem)

                # 索引功能点
                for func in data.get('functions', []):
                    item = KnowledgeItem(
                        id=f"{module}_{func.get('id', '')}",
                        category='business',
                        subcategory='hr_products',
                        source_file=str(yaml_file),
                        content=json.dumps(func, ensure_ascii=False),
                        metadata={
                            'module': module,
                            'function_id': func.get('id'),
                            'function_name': func.get('name'),
                            'extension_points': func.get('extension_points', [])
                        }
                    )
                    self.index['business'].append(item)

            except Exception as e:
                logger.error(f"Failed to load {yaml_file}: {e}")

    def _load_technical_guidelines(self):
        """加载技术规范"""
        guidelines_dir = self.knowledge_dir / "dev_guidelines"

        if not guidelines_dir.exists():
            return

        # 递归加载所有markdown文件
        for md_file in guidelines_dir.rglob("*.md"):
            try:
                with open(md_file, 'r', encoding='utf-8') as f:
                    content = f.read()

                # 提取章节
                sections = self._parse_markdown_sections(content)

                for i, (title, section_content) in enumerate(sections):
                    item = KnowledgeItem(
                        id=f"{md_file.stem}_sec{i}",
                        category='technical',
                        subcategory='/'.join(md_file.relative_to(guidelines_dir).parts[:-1]),
                        source_file=str(md_file),
                        content=section_content,
                        metadata={
                            'title': title,
                            'file': str(md_file.relative_to(self.knowledge_dir))
                        }
                    )
                    self.index['technical'].append(item)

            except Exception as e:
                logger.error(f"Failed to load {md_file}: {e}")

    def _load_reusable_assets(self):
        """加载可复用资产"""
        assets_dir = self.knowledge_dir / "reusable_assets"

        if not assets_dir.exists():
            return

        # 加载索引文件
        index_file = assets_dir / "asset_index.yaml"
        if index_file.exists():
            with open(index_file, 'r', encoding='utf-8') as f:
                index = yaml.safe_load(f)

            for asset_data in index.get('assets', []):
                asset_path = assets_dir / asset_data.get('path', '')

                content = ""
                if asset_path.exists():
                    with open(asset_path, 'r', encoding='utf-8') as f:
                        content = f.read()

                item = KnowledgeItem(
                    id=asset_data.get('id', ''),
                    category='asset',
                    subcategory=asset_data.get('type', 'code_snippet'),
                    source_file=str(asset_path),
                    content=content,
                    metadata={
                        'name': asset_data.get('name'),
                        'type': asset_data.get('type'),
                        'language': asset_data.get('language'),
                        'tags': asset_data.get('tags', []),
                        'usage_context': asset_data.get('usage_context', []),
                        'variables': asset_data.get('variables', [])
                    }
                )
                self.index['asset'].append(item)

    def _parse_markdown_sections(self, content: str) -> List[Tuple[str, str]]:
        """解析Markdown章节"""
        sections = []
        lines = content.split('\n')
        current_title = "Introduction"
        current_content = []

        for line in lines:
            if line.startswith('# '):
                if current_content:
                    sections.append((current_title, '\n'.join(current_content)))
                current_title = line[2:].strip()
                current_content = []
            elif line.startswith('## '):
                if current_content:
                    sections.append((current_title, '\n'.join(current_content)))
                current_title = line[3:].strip()
                current_content = []
            else:
                current_content.append(line)

        if current_content:
            sections.append((current_title, '\n'.join(current_content)))

        return sections

    def _build_inverted_index(self):
        """构建倒排索引"""
        for category, items in self.index.items():
            for item in items:
                # 提取关键词
                keywords = self._extract_keywords(item.content + ' ' + str(item.metadata))

                for keyword in keywords:
                    if keyword not in self.inverted_index:
                        self.inverted_index[keyword] = []
                    self.inverted_index[keyword].append(item.id)

    def _extract_keywords(self, text: str) -> List[str]:
        """提取关键词（简化版）"""
        # 清理文本
        text = re.sub(r'[^\u4e00-\u9fa5a-zA-Z0-9_]', ' ', text)

        # 分词（简化处理）
        words = text.split()

        # 过滤停用词
        stopwords = {'的', '了', '在', '是', 'and', 'the', 'of', 'to'}
        keywords = [w.lower() for w in words if len(w) > 1 and w.lower() not in stopwords]

        return keywords

    def search(self, query: str, category: Optional[str] = None, top_k: int = 5) -> List[KnowledgeItem]:
        """搜索知识"""
        if not self._loaded:
            self.load()

        # 提取查询关键词
        query_keywords = self._extract_keywords(query)

        # 计算相关度
        scores: Dict[str, float] = {}

        for keyword in query_keywords:
            if keyword in self.inverted_index:
                for item_id in self.inverted_index[keyword]:
                    scores[item_id] = scores.get(item_id, 0) + 1

        # 归一化分数
        for item_id in scores:
            scores[item_id] /= len(query_keywords)

        # 获取候选项
        candidates = []
        search_categories = [category] if category else ['business', 'technical', 'asset']

        for cat in search_categories:
            for item in self.index.get(cat, []):
                if item.id in scores:
                    item.relevance_score = scores[item.id]
                    candidates.append(item)

        # 排序并返回Top K
        candidates.sort(key=lambda x: x.relevance_score, reverse=True)
        return candidates[:top_k]

    def get_by_id(self, item_id: str) -> Optional[KnowledgeItem]:
        """通过ID获取知识项"""
        for category, items in self.index.items():
            for item in items:
                if item.id == item_id:
                    return item
        return None

    def get_assets_by_context(self, context: str, language: Optional[str] = None) -> List[KnowledgeItem]:
        """根据上下文获取资产"""
        assets = self.index.get('asset', [])

        # 过滤语言
        if language:
            assets = [a for a in assets if a.metadata.get('language') == language]

        # 根据使用场景过滤
        context_assets = []
        for asset in assets:
            usage_contexts = asset.metadata.get('usage_context', [])
            if any(ctx in context for ctx in usage_contexts):
                context_assets.append(asset)

        return context_assets

    def get_extension_points(self, module: str) -> List[Dict]:
        """获取模块的扩展点信息"""
        extension_points = []

        for item in self.index.get('business', []):
            if item.metadata.get('module') == module:
                points = item.metadata.get('extension_points', [])
                extension_points.extend(points)

        return extension_points

    def get_coding_standard(self, topic: str) -> Optional[str]:
        """获取编码规范"""
        results = self.search(topic, category='technical', top_k=1)
        if results:
            return results[0].content
        return None


# 便捷函数
def get_knowledge_base() -> KnowledgeBase:
    """获取知识库单例"""
    kb = KnowledgeBase()
    kb.load()
    return kb
