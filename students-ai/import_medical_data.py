"""
将datasets文件夹中的JSON医疗对话数据导入到ChromaDB向量数据库
"""

import chromadb
import json
import os
from chromadb.utils import embedding_functions
from hashlib import md5
from pathlib import Path


def import_medical_data():
    """
    导入医疗数据到ChromaDB
    """
    # 获取项目根目录
    base_dir = Path(__file__).resolve().parent.parent
    ai_models_dir = Path(os.getenv("AI_MODELS_DIR", r"D:\ai_models"))
    
    # 配置路径
    db_path = base_dir / "medical_db"
    model_path = ai_models_dir / "gte_base_zh"
    json_path = base_dir / "datasets" / "medical_new_2.json"
    
    print(f"[导入] 数据库路径: {db_path}")
    print(f"[导入] 模型路径: {model_path}")
    print(f"[导入] JSON文件路径: {json_path}")

    # 使用本地持久化客户端连接ChromaDB
    chroma_client = chromadb.PersistentClient(path=str(db_path))
    
    # 使用gte-base-zh模型
    print("[导入] 加载embedding模型...")
    ef = embedding_functions.SentenceTransformerEmbeddingFunction(
        model_name=str(model_path),
        device="cpu",  # 如果有GPU，可以改为 "cuda"
    )
    
    # 获取或创建集合
    print("[导入] 获取/创建集合: my_medical_1")
    collection = chroma_client.get_or_create_collection(
        name="my_medical_1",
        metadata={"hnsw:space": "cosine"},  # 使用余弦相似度
        embedding_function=ef,
    )
    
    # 读取JSONL数据（每行一个JSON对象）
    print(f"[导入] 读取JSONL文件: {json_path}")
    data = []
    error_count = 0
    
    with open(json_path, "r", encoding="utf-8-sig") as f:  # 使用utf-8-sig处理BOM
        for line_num, line in enumerate(f, 1):
            line = line.strip()
            if not line:  # 跳过空行
                continue
                
            # 移除行尾可能的逗号
            if line.endswith(','):
                line = line[:-1]
                
            try:
                item = json.loads(line)
                # 检查是否有必需字段
                if "name" in item and "desc" in item:
                    # 使用name和desc组合作为content
                    data.append({
                        "filename": item.get("name", "unknown"),
                        "content": f"{item.get('name', '')}: {item.get('desc', '')}"
                    })
                else:
                    print(f"[警告] 第{line_num}行缺少必需字段")
                    error_count += 1
            except json.JSONDecodeError as e:
                if error_count < 5:  # 只显示前5个错误
                    print(f"[警告] 第{line_num}行JSON解析失败: {e}")
                error_count += 1
                continue
    
    if error_count > 5:
        print(f"[警告] 共有 {error_count} 行解析失败")
    
    print(f"[导入] 成功读取 {len(data)} 条数据")
    
    if len(data) == 0:
        print("[错误] 没有成功读取任何数据，请检查JSON文件格式")
        return
    
    # 准备数据
    ids, documents, metadatas = [], [], []
    
    for item in data:
        # 使用md5生成唯一ID（基于文件名和内容）
        unique_id = md5(
            (item["filename"] + item["content"]).encode("utf-8")
        ).hexdigest()
        
        ids.append(unique_id)
        documents.append(item["content"])
        metadatas.append({"from": item["filename"]})
    
    # 分批添加到ChromaDB（避免超过最大批次大小）
    batch_size = 5000  # ChromaDB的最大批次大小约为5461，我们用5000保险
    total_batches = (len(ids) + batch_size - 1) // batch_size
    
    print(f"[导入] 开始分批导入数据到ChromaDB，共 {total_batches} 批...")
    
    for i in range(0, len(ids), batch_size):
        batch_num = i // batch_size + 1
        batch_ids = ids[i:i + batch_size]
        batch_documents = documents[i:i + batch_size]
        batch_metadatas = metadatas[i:i + batch_size]
        
        print(f"[导入] 正在导入第 {batch_num}/{total_batches} 批（{len(batch_ids)} 条记录）...")
        
        collection.add(
            ids=batch_ids,
            documents=batch_documents,
            metadatas=batch_metadatas,
        )
    
    
    # 显示结果
    total_count = collection.count()
    print(f"[成功] 数据导入完成！")
    print(f"[统计] 数据库中共有 {total_count} 条记录")
    
    # 预览前3条数据
    print("\n[预览] 前3条数据:")
    preview = collection.peek(limit=3)
    for i, (doc_id, doc, meta) in enumerate(zip(
        preview['ids'], 
        preview['documents'], 
        preview['metadatas']
    )):
        print(f"\n记录 {i+1}:")
        print(f"  ID: {doc_id}")
        print(f"  来源: {meta.get('from', 'unknown')}")
        print(f"  内容: {doc[:100]}..." if len(doc) > 100 else f"  内容: {doc}")


if __name__ == "__main__":
    print("=" * 60)
    print("医疗知识库数据导入工具")
    print("=" * 60)
    
    # 执行导入
    import_medical_data()
    
    print("\n" + "=" * 60)
    print("导入完成！现在可以使用RAG功能进行检索了。")
    print("=" * 60)
