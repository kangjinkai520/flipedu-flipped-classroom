"""
快速下载 bge-reranker-base 模型
使用国内镜像加速下载
"""
from huggingface_hub import snapshot_download
import os
from pathlib import Path

# 设置国内镜像
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'

ai_models_dir = Path(os.getenv("AI_MODELS_DIR", r"D:\ai_models"))
model_dir = ai_models_dir / "bge_reranker_base"
model_dir.parent.mkdir(parents=True, exist_ok=True)

# 下载模型
model_path = snapshot_download(
    repo_id="BAAI/bge-reranker-base",
    local_dir=str(model_dir),
    resume_download=True,  # 支持断点续传
)

print(f"模型下载完成: {model_path}")
