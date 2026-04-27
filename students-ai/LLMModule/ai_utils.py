import os

from openai import OpenAI


def generate_title(query: str, max_length: int = 20) -> str:
    """Generate a short conversation title with Zhipu AI."""
    fallback_title = query[:max_length] if len(query) <= max_length else query[:max_length - 3] + "..."
    try:
        api_key = os.getenv("ZHIPU_API_KEY") or os.getenv("BIGMODEL_API_KEY")
        if not api_key:
            return fallback_title

        client = OpenAI(
            api_key=api_key,
            base_url=os.getenv("ZHIPU_BASE_URL", "https://open.bigmodel.cn/api/paas/v4/"),
        )
        response = client.chat.completions.create(
            model=os.getenv("ZHIPU_TEXT_MODEL", "glm-4-flash"),
            messages=[
                {
                    "role": "user",
                    "content": f"请为下面的问题生成一个不超过 {max_length} 个字的中文标题，只返回标题：\n{query}"
                }
            ],
            max_tokens=80,
            temperature=0.5
        )

        title = (response.choices[0].message.content or "").strip()
        title = title.replace('"', '').replace("'", '').replace("。", '').replace(".", '')
        return title[:max_length] if title else fallback_title
    except Exception as e:
        print(f"[ZhipuTitle] generate title failed: {e}")
        return fallback_title
