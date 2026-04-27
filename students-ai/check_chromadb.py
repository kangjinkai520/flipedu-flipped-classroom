"""
快速检查ChromaDB中的记录数
"""
import chromadb
from pathlib import Path

# 配置路径
base_dir = Path(__file__).resolve().parent.parent
db_path = base_dir / "medical_db"

print(f"数据库路径: {db_path}")

# 连接ChromaDB
chroma_client = chromadb.PersistentClient(path=str(db_path))

# 获取集合
try:
    collection = chroma_client.get_collection(name="my_medical_1")
    count = collection.count()
    print(f"✅ 集合 'my_medical_1' 中有 {count} 条记录")
    
    # 预览前3条
    if count > 0:
        preview = collection.peek(limit=3)
        print(f"\n预览前3条记录：")
        for i, (doc_id, doc, meta) in enumerate(zip(
            preview['ids'], 
            preview['documents'], 
            preview['metadatas']
        ), 1):
            print(f"\n  记录{i}:")
            print(f"    来源: {meta.get('from', 'unknown')}")
            print(f"    内容: {doc[:80]}..." if len(doc) > 80 else f"    内容: {doc}")
except Exception as e:
    print(f"❌ 获取集合失败: {e}")
