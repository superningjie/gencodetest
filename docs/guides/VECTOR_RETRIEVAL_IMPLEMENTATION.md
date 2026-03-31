# 向量检索资产匹配实施指南

> **目标**: 将资产匹配准确率从 75% 提升到 85%+

---

## 1. 技术架构

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        Asset Matching Pipeline                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │   需求解析    │ → │  Embedding   │ → │  向量检索     │      │
│  │              │    │   编码       │    │   (Top-K)    │      │
│  └──────────────┘    └──────────────┘    └──────────────┘      │
│                                               ↓                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │  结果返回    │ ← │  重排序       │ ← │  候选过滤    │      │
│  │              │    │  (精排)      │    │  (粗排)      │      │
│  └──────────────┘    └──────────────┘    └──────────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 组件选择

| 组件 | 推荐方案 | 备选方案 | 选择理由 |
|------|---------|---------|---------|
| Embedding 模型 | text-embedding-3-small | BGE-large | 性价比高，1536维 |
| 向量数据库 | ChromaDB (本地) | Pinecone (云端) | 零配置，适合开发 |
| 相似度算法 | 余弦相似度 | 欧氏距离 | 适合文本语义匹配 |
| 重排序模型 | Cross-Encoder | 规则排序 | 精度高，可离线 |

---

## 2. 实施步骤

### Step 1: 环境准备

```bash
# 安装依赖
pip install chromadb sentence-transformers openai

# 创建数据目录
mkdir -p vector_db assets_index
```

### Step 2: Embedding 服务封装

```python
# core/embedding_service.py
from typing import List, Union
import openai
from sentence_transformers import SentenceTransformer

class EmbeddingService:
    """文本 Embedding 服务"""
    
    def __init__(self, provider: str = "openai"):
        self.provider = provider
        
        if provider == "openai":
            self.client = openai.OpenAI()
            self.model = "text-embedding-3-small"
        elif provider == "local":
            self.model = SentenceTransformer('BAAI/bge-large-zh-v1.5')
    
    def embed(
        self,
        texts: Union[str, List[str]],
        batch_size: int = 100
    ) -> List[List[float]]:
        """
        将文本转换为向量
        
        Args:
            texts: 单条文本或文本列表
            batch_size: 批处理大小
            
        Returns:
            向量列表，每个向量 1536 维 (OpenAI) 或 1024 维 (BGE)
        """
        if isinstance(texts, str):
            texts = [texts]
        
        if self.provider == "openai":
            return self._embed_openai(texts)
        else:
            return self._embed_local(texts, batch_size)
    
    def _embed_openai(self, texts: List[str]) -> List[List[float]]:
        response = self.client.embeddings.create(
            model=self.model,
            input=texts
        )
        return [item.embedding for item in response.data]
    
    def _embed_local(
        self,
        texts: List[str],
        batch_size: int
    ) -> List[List[float]]:
        embeddings = []
        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]
            batch_embeddings = self.model.encode(batch)
            embeddings.extend(batch_embeddings.tolist())
        return embeddings
```

### Step 3: 向量数据库管理

```python
# core/vector_store.py
import chromadb
from chromadb.config import Settings
from typing import List, Dict, Optional
import json

class VectorAssetStore:
    """基于向量的资产存储"""
    
    def __init__(
        self,
        collection_name: str = "code_assets",
        persist_directory: str = "./vector_db"
    ):
        self.client = chromadb.PersistentClient(
            path=persist_directory,
            settings=Settings(
                anonymized_telemetry=False
            )
        )
        self.collection = self.client.get_or_create_collection(
            name=collection_name
        )
    
    def index_assets(
        self,
        assets: List[CodeAsset],
        embedding_service: EmbeddingService
    ):
        """
        批量索引资产到向量库
        
        资产描述文本构建规则：
        1. 资产名称
        2. 资产描述
        3. 标签列表
        4. 使用场景
        """
        documents = []
        metadatas = []
        ids = []
        
        for asset in assets:
            # 构建描述文本
            doc = self._build_document(asset)
            documents.append(doc)
            
            # 元数据（可过滤字段）
            metadatas.append({
                "id": asset.id,
                "name": asset.name,
                "type": asset.asset_type,
                "language": asset.language.value,
                "tags": json.dumps(asset.tags),
                "file_path": asset.file_path,
                "success_rate": asset.success_rate,
                "usage_count": asset.usage_count
            })
            
            ids.append(asset.id)
        
        # 生成 Embedding
        embeddings = embedding_service.embed(documents)
        
        # 批量添加到向量库
        self.collection.add(
            embeddings=embeddings,
            documents=documents,
            metadatas=metadatas,
            ids=ids
        )
    
    def _build_document(self, asset: CodeAsset) -> str:
        """构建资产描述文档"""
        parts = [
            f"名称: {asset.name}",
            f"描述: {asset.description}",
            f"类型: {asset.asset_type}",
            f"语言: {asset.language.value}",
            f"标签: {', '.join(asset.tags)}",
            f"使用场景: {', '.join(asset.usage_context)}"
        ]
        return "\n".join(parts)
    
    def search(
        self,
        query: str,
        embedding_service: EmbeddingService,
        top_k: int = 10,
        filters: Optional[Dict] = None,
        min_similarity: float = 0.7
    ) -> List[AssetSearchResult]:
        """
        语义检索资产
        
        Args:
            query: 查询文本（需求描述）
            top_k: 返回结果数量
            filters: 过滤条件，如 {"language": "java"}
            min_similarity: 最小相似度阈值
            
        Returns:
            匹配结果列表，按相似度排序
        """
        # 生成查询向量
        query_embedding = embedding_service.embed(query)[0]
        
        # 构建 where 条件
        where_clause = self._build_where_clause(filters)
        
        # 向量检索
        results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=top_k,
            where=where_clause,
            include=["metadatas", "documents", "distances"]
        )
        
        # 解析结果
        search_results = []
        for i, (metadata, document, distance) in enumerate(zip(
            results['metadatas'][0],
            results['documents'][0],
            results['distances'][0]
        )):
            # 余弦距离转相似度
            similarity = 1 - distance
            
            if similarity < min_similarity:
                continue
            
            # 重建资产对象
            asset = self._rebuild_asset(metadata)
            
            search_results.append(AssetSearchResult(
                asset=asset,
                similarity=similarity,
                matched_content=document
            ))
        
        return search_results
    
    def _build_where_clause(
        self,
        filters: Optional[Dict]
    ) -> Optional[Dict]:
        """构建 ChromaDB where 条件"""
        if not filters:
            return None
        
        where = {}
        for key, value in filters.items():
            where[key] = value
        
        return where
    
    def _rebuild_asset(self, metadata: Dict) -> CodeAsset:
        """从元数据重建资产对象"""
        return CodeAsset(
            id=metadata["id"],
            name=metadata["name"],
            asset_type=metadata["type"],
            language=CodeLanguage(metadata["language"]),
            file_path=metadata["file_path"],
            tags=json.loads(metadata["tags"]),
            success_rate=metadata["success_rate"],
            usage_count=metadata["usage_count"]
        )
```

### Step 4: 重排序优化

```python
# core/reranker.py
from typing import List
import numpy as np

class AssetReranker:
    """资产结果重排序"""
    
    def __init__(self):
        # 各因素权重
        self.weights = {
            'similarity': 0.4,      # 语义相似度
            'success_rate': 0.2,    # 历史成功率
            'usage_count': 0.15,    # 使用频次
            'recency': 0.1,         # 最近使用
            'complexity_match': 0.15  # 复杂度匹配
        }
    
    def rerank(
        self,
        results: List[AssetSearchResult],
        requirement_context: dict
    ) -> List[AssetSearchResult]:
        """
        对检索结果进行重排序
        
        排序因素：
        1. 语义相似度（基础）
        2. 历史成功率（质量）
        3. 使用频次（流行度）
        4. 最近使用（时效性）
        5. 复杂度匹配（适配性）
        """
        scored_results = []
        
        for result in results:
            score = self._calculate_score(result, requirement_context)
            scored_results.append((result, score))
        
        # 按综合分数排序
        scored_results.sort(key=lambda x: x[1], reverse=True)
        
        return [r for r, _ in scored_results]
    
    def _calculate_score(
        self,
        result: AssetSearchResult,
        context: dict
    ) -> float:
        """计算综合分数"""
        asset = result.asset
        
        # 1. 语义相似度 (已归一化)
        similarity_score = result.similarity
        
        # 2. 成功率 (0-1)
        success_score = asset.success_rate
        
        # 3. 使用频次 (对数压缩)
        usage_score = np.log1p(asset.usage_count) / 5  # 归一化到 ~0-1
        
        # 4. 时效性 (指数衰减)
        if asset.last_used:
            days_since_use = (datetime.now() - asset.last_used).days
            recency_score = np.exp(-days_since_use / 30)  # 30天半衰期
        else:
            recency_score = 0.5
        
        # 5. 复杂度匹配
        complexity_score = self._match_complexity(asset, context)
        
        # 加权求和
        final_score = (
            self.weights['similarity'] * similarity_score +
            self.weights['success_rate'] * success_score +
            self.weights['usage_count'] * usage_score +
            self.weights['recency'] * recency_score +
            self.weights['complexity_match'] * complexity_score
        )
        
        return final_score
    
    def _match_complexity(self, asset: CodeAsset, context: dict) -> float:
        """评估复杂度匹配度"""
        # 简化的复杂度估计
        asset_complexity = len(asset.content.split('\n')) / 100  # 百行代码
        req_complexity = context.get('estimated_complexity', 1.0)
        
        # 越接近越好
        diff = abs(asset_complexity - req_complexity)
        return max(0, 1 - diff / max(asset_complexity, req_complexity))
```

### Step 5: 集成到 AssetMatcherSkill

```python
# skills/asset_matcher/vector_matcher.py
from core.embedding_service import EmbeddingService
from core.vector_store import VectorAssetStore
from core.reranker import AssetReranker

class VectorAssetMatcherSkill(BaseSkill):
    """基于向量检索的资产匹配器"""
    
    def __init__(self, config: Dict = None):
        super().__init__(config)
        
        # 初始化服务
        self.embedding_service = EmbeddingService(
            provider=config.get('embedding_provider', 'openai')
        )
        self.vector_store = VectorAssetStore(
            collection_name=config.get('collection', 'code_assets')
        )
        self.reranker = AssetReranker()
        
        # 配置参数
        self.top_k = config.get('top_k', 10)
        self.min_similarity = config.get('min_similarity', 0.7)
        self.final_results = config.get('final_results', 3)
    
    async def execute(self, context: SkillContext) -> Dict:
        impl_items = context.get_implementation_items()
        
        if not impl_items:
            return {'status': 'error', 'message': 'No implementation items'}
        
        all_matched_assets = []
        
        for item in impl_items:
            # 1. 构建查询
            query = self._build_query(item)
            
            # 2. 向量检索
            filters = {'language': item.language.value}
            candidates = self.vector_store.search(
                query=query,
                embedding_service=self.embedding_service,
                top_k=self.top_k,
                filters=filters,
                min_similarity=self.min_similarity
            )
            
            if not candidates:
                continue
            
            # 3. 重排序
            context_info = {
                'estimated_complexity': self._estimate_complexity(item),
                'impl_type': item.impl_type.value
            }
            ranked = self.reranker.rerank(candidates, context_info)
            
            # 4. 取 Top-N
            selected = ranked[:self.final_results]
            
            # 5. 关联到实现项
            item.required_assets = [r.asset.id for r in selected]
            all_matched_assets.extend([r.asset for r in selected])
        
        context.set_assets(all_matched_assets)
        
        return {
            'status': 'success',
            'data': {
                'total_items': len(impl_items),
                'matched_assets': len(all_matched_assets),
                'assets': [
                    {'id': a.id, 'name': a.name, 'type': a.asset_type}
                    for a in all_matched_assets
                ]
            }
        }
    
    def _build_query(self, item: ImplementationItem) -> str:
        """构建检索查询"""
        return f"""
开发需求: {item.name}
功能描述: {item.description}
技术栈: {item.tech_stack}
实现类型: {item.impl_type.value}
""".strip()
    
    def _estimate_complexity(self, item: ImplementationItem) -> float:
        """估算需求复杂度"""
        desc_length = len(item.description)
        if desc_length < 50:
            return 0.5
        elif desc_length < 150:
            return 1.0
        else:
            return 2.0
```

---

## 3. 性能优化

### 3.1 索引优化

```python
# 批量索引优化
class BatchIndexingOptimizer:
    """批量索引优化器"""
    
    def __init__(self, batch_size: int = 100):
        self.batch_size = batch_size
    
    async def index_all_assets(
        self,
        assets: List[CodeAsset],
        vector_store: VectorAssetStore,
        embedding_service: EmbeddingService
    ):
        """批量索引所有资产"""
        total = len(assets)
        
        for i in range(0, total, self.batch_size):
            batch = assets[i:i + self.batch_size]
            
            print(f"Indexing batch {i//self.batch_size + 1}/{(total-1)//self.batch_size + 1}")
            
            vector_store.index_assets(batch, embedding_service)
            
            # 进度提示
            progress = min(100, (i + len(batch)) / total * 100)
            print(f"Progress: {progress:.1f}%")
```

### 3.2 查询缓存

```python
from functools import lru_cache
import hashlib

class CachedEmbeddingService(EmbeddingService):
    """带缓存的 Embedding 服务"""
    
    def __init__(self, cache_size: int = 1000, **kwargs):
        super().__init__(**kwargs)
        self.cache_size = cache_size
        self._embed = lru_cache(maxsize=cache_size)(self._embed_impl)
    
    def _embed_impl(self, text_hash: str) -> List[float]:
        # 实际实现...
        pass
    
    def embed(self, texts: Union[str, List[str]]) -> List[List[float]]:
        if isinstance(texts, str):
            texts = [texts]
        
        results = []
        for text in texts:
            text_hash = hashlib.md5(text.encode()).hexdigest()
            results.append(self._embed(text_hash))
        
        return results
```

---

## 4. 评估与监控

### 4.1 离线评估

```python
class AssetMatchingEvaluator:
    """资产匹配效果评估器"""
    
    def __init__(self, test_data_path: str):
        self.test_cases = self._load_test_data(test_data_path)
    
    def evaluate(self, matcher: VectorAssetMatcherSkill) -> EvaluationResult:
        """
        评估匹配效果
        
        指标：
        - Recall@K: 正确资产在前K个中的比例
        - MRR: 平均倒数排名
        - NDCG: 归一化折损累积增益
        """
        metrics = {
            'recall@1': [],
            'recall@3': [],
            'recall@5': [],
            'mrr': []
        }
        
        for test_case in self.test_cases:
            # 执行匹配
            results = matcher.match(test_case['requirement'])
            result_ids = [r.asset.id for r in results]
            
            # 计算指标
            expected_id = test_case['expected_asset_id']
            
            if expected_id in result_ids:
                rank = result_ids.index(expected_id) + 1
                metrics['recall@1'].append(1 if rank <= 1 else 0)
                metrics['recall@3'].append(1 if rank <= 3 else 0)
                metrics['recall@5'].append(1 if rank <= 5 else 0)
                metrics['mrr'].append(1 / rank)
            else:
                metrics['recall@1'].append(0)
                metrics['recall@3'].append(0)
                metrics['recall@5'].append(0)
                metrics['mrr'].append(0)
        
        return EvaluationResult(
            recall_at_1=sum(metrics['recall@1']) / len(metrics['recall@1']),
            recall_at_3=sum(metrics['recall@3']) / len(metrics['recall@3']),
            recall_at_5=sum(metrics['recall@5']) / len(metrics['recall@5']),
            mrr=sum(metrics['mrr']) / len(metrics['mrr'])
        )
```

### 4.2 在线监控

```python
class MatchingMetricsCollector:
    """匹配指标收集器"""
    
    def __init__(self):
        self.metrics = {
            'query_count': 0,
            'avg_response_time': 0,
            'avg_similarity': 0,
            'cache_hit_rate': 0
        }
    
    def record_match(
        self,
        query_time: float,
        results: List[AssetSearchResult],
        cache_hit: bool
    ):
        """记录一次匹配"""
        self.metrics['query_count'] += 1
        
        # 更新平均响应时间
        n = self.metrics['query_count']
        self.metrics['avg_response_time'] = (
            (self.metrics['avg_response_time'] * (n-1) + query_time) / n
        )
        
        # 更新平均相似度
        if results:
            avg_sim = sum(r.similarity for r in results) / len(results)
            self.metrics['avg_similarity'] = (
                (self.metrics['avg_similarity'] * (n-1) + avg_sim) / n
            )
```

---

## 5. 部署配置

### 5.1 配置文件

```yaml
# config/vector_matching.yaml
vector_matching:
  embedding:
    provider: openai  # 或 local
    model: text-embedding-3-small
    batch_size: 100
    cache_size: 1000
  
  vector_store:
    type: chromadb
    persist_directory: ./vector_db
    collection_name: code_assets
  
  search:
    top_k: 10
    min_similarity: 0.7
    final_results: 3
  
  reranker:
    weights:
      similarity: 0.4
      success_rate: 0.2
      usage_count: 0.15
      recency: 0.1
      complexity_match: 0.15
```

---

## 6. 常见问题

### Q1: 向量检索返回结果为空
- 检查资产是否已索引
- 检查相似度阈值是否过高
- 检查过滤条件是否过于严格

### Q2: 检索速度慢
- 启用 Embedding 缓存
- 增加批处理大小
- 考虑使用云端向量数据库

### Q3: 匹配精度不高
- 优化资产描述文本
- 调整重排序权重
- 增加 Few-shot 示例

---

*维护记录：*
- 2026-03-06: 创建初始版本
