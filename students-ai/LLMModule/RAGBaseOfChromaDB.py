"""
RAG模块 - 基于ChromaDB的检索增强生成

使用ChromaDB进行文档检索，结合embedding模型和reranking模型实现两阶段检索：
1. Embedding检索：使用SentenceTransformer快速筛选Top-K文档
2. 二次精排：使用CrossEncoder对候选文档精确排序
"""

import chromadb
from chromadb.utils import embedding_functions
from sentence_transformers import CrossEncoder
import os

AI_MODELS_DIR = os.getenv("AI_MODELS_DIR", r"D:\ai_models")


class RAGBaseOfChromaDB:
    """基于ChromaDB的RAG检索类（本地持久化模式）"""
    
    def __init__(self, device="cpu"):
        """
        初始化RAG检索器
        """
        # 获取项目根目录
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        
        # 配置路径
        db_path = os.path.join(base_dir, "medical_db")
        embedding_model_path = os.path.join(AI_MODELS_DIR, "gte_base_zh")
        reranker_model_path = os.path.join(AI_MODELS_DIR, "bge_reranker_base")
        collection_name = "my_medical_1"
        
        # 连接ChromaDB（使用本地持久化客户端）
        print(f"[RAG] 使用本地持久化客户端连接: {db_path}")
        chroma_client = chromadb.PersistentClient(path=db_path)
        
        # 定义embedding函数
        print(f"[RAG] 加载embedding模型: {embedding_model_path}")
        ef = embedding_functions.SentenceTransformerEmbeddingFunction(
            model_name=embedding_model_path,
            device=device,
        )
        
        # 获取或创建集合
        print(f"[RAG] 获取集合: {collection_name}")
        self.collection = chroma_client.get_or_create_collection(
            name=collection_name,
            metadata={"hnsw:space": "cosine"},
            embedding_function=ef,
        )
        
        # 初始化reranker模型（延迟加载，首次查询时加载）
        self.reranker_model = None
        self.reranker_model_path = reranker_model_path
        self.device = device
        
        print("[RAG] RAG模块初始化完成")
    
    def _load_reranker(self):
        """延迟加载reranker模型"""
        if self.reranker_model is None:
            print(f"[RAG] 加载reranker模型: {self.reranker_model_path}")
            self.reranker_model = CrossEncoder(
                self.reranker_model_path,  # 直接使用本地路径
                device=self.device,
            )
        return self.reranker_model
    
    def query(self, query, n_results=10, top_k=5, score_threshold=0.2):
        """
        检索相关文档
        
        Args:
            query: 查询文本
            n_results: 初次检索返回的文档数量
            top_k: 二次精排后返回的文档数量
            score_threshold: 相关性分数阈值，低于此值的文档被过滤
        
        Returns:
            List[str]: 相关文档列表
        """
        # 第一阶段：使用embedding进行快速检索
        results = self.collection.query(query_texts=[query], n_results=n_results)
        documents = [result for result in results["documents"][0] if result is not None]
        
        if len(documents) == 0:
            print(f"[RAG] 未检索到相关文档，查询: {query}")
            return []
        
        print(f"[RAG] 初次检索到 {len(documents)} 个文档")
        
        # 第二阶段：使用CrossEncoder进行二次精排
        model = self._load_reranker()
        ranks = model.rank(query, documents, return_documents=True, top_k=top_k)
        
        print(f"[RAG] 查询: {query}")
        # 过滤低分文档
        chunks = []
        for rank in ranks:
            if rank["score"] > score_threshold:
                chunks.append(rank["text"])
                print(f"[RAG]   - 分数: {rank['score']:.3f}, 文档: {rank['text'][:50]}...")
        
        print(f"[RAG] 返回 {len(chunks)} 个高相关文档")
        return chunks
