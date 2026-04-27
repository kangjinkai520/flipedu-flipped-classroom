import base64
import os

from openai import OpenAI


class ZhipuLLM:
    TEXT_MODEL = os.getenv("ZHIPU_TEXT_MODEL", "glm-4-flash")
    VISION_MODEL = os.getenv("ZHIPU_VISION_MODEL", "glm-4v-flash")
    BASE_URL = os.getenv("ZHIPU_BASE_URL", "https://open.bigmodel.cn/api/paas/v4/")

    SYSTEM_PROMPT = (
        "你是课程学习助手，面向学生提供清晰、简洁、可执行的学习帮助。"
        "回答时请结合课程语境，优先给出重点总结、步骤拆解、例子和易错点。"
        "如果问题过于宽泛，可以先给出通用框架，再提示学生补充具体材料。"
    )

    DEFAULT_TEMPERATURE = 0.6
    DEFAULT_MAX_TOKENS = 2048

    def __init__(self, api_key=None):
        self.api_key = api_key or os.getenv("ZHIPU_API_KEY") or os.getenv("BIGMODEL_API_KEY")
        self.client = OpenAI(
            api_key=self.api_key,
            base_url=self.BASE_URL,
        )

    def chat_stream(self, messages, model=None, temperature=None, max_tokens=None,
                    deep_think=False, websearch=False):
        model = model or self.TEXT_MODEL
        temperature = temperature if temperature is not None else self.DEFAULT_TEMPERATURE
        max_tokens = max_tokens or self.DEFAULT_MAX_TOKENS

        if not self.api_key:
            yield "AI 服务未配置智谱 API Key，请检查后端 .env 中的 ZHIPU_API_KEY。"
            return

        try:
            response = self.client.chat.completions.create(
                model=model,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
                stream=True
            )

            emitted = False
            for chunk in response:
                if chunk.choices and hasattr(chunk.choices[0].delta, "content"):
                    content = chunk.choices[0].delta.content
                    if content:
                        emitted = True
                        yield content

            if not emitted:
                yield "AI 暂时没有返回内容，请稍后重试。"
        except Exception as e:
            print(f"[ZhipuLLM] stream chat failed: {e}")
            yield "AI 生成失败，请检查智谱 API Key、网络代理或模型服务状态。"

    def encode_image(self, image_path):
        try:
            with open(image_path, "rb") as image_file:
                return base64.b64encode(image_file.read()).decode("utf-8")
        except Exception as e:
            print(f"[ZhipuLLM] encode image failed: {e}")
            return None

    def process_image_message(self, content, image_path):
        if not image_path or not os.path.exists(image_path):
            return {"role": "user", "content": content}

        base64_image = self.encode_image(image_path)
        if not base64_image:
            return {"role": "user", "content": content}

        image_type = os.path.splitext(image_path)[1][1:] or "png"
        return {
            "role": "user",
            "content": [
                {
                    "type": "image_url",
                    "image_url": {"url": f"data:image/{image_type};base64,{base64_image}"},
                },
                {"type": "text", "text": content},
            ],
        }

    def build_messages(self, query, history_messages=None, image_path=None, rag_chunks=None):
        messages = [{"role": "system", "content": self.SYSTEM_PROMPT}]

        if history_messages:
            for msg in history_messages:
                role = getattr(msg, "role", "user")
                content = getattr(msg, "content", "")
                image_url = getattr(msg, "image_url", "")
                if image_url:
                    img_path = image_url.replace("/static/", "static/").replace("/", os.sep)
                    messages.append(self.process_image_message(content, img_path))
                else:
                    messages.append({"role": role, "content": content})

        if rag_chunks and len(rag_chunks) > 0:
            context = "\n".join([f"{index + 1}. {chunk}" for index, chunk in enumerate(rag_chunks)])
            current_content = (
                "请参考以下资料回答学生问题。\n\n"
                f"资料：\n{context}\n\n"
                f"学生问题：{query}\n\n"
                "请用中文回答，并给出清晰结构。"
            )
        else:
            current_content = query

        if image_path and os.path.exists(image_path):
            messages.append(self.process_image_message(current_content, image_path))
        else:
            messages.append({"role": "user", "content": current_content})

        return messages

    def chat_with_context(self, query, history_messages=None, image_path=None,
                          rag_chunks=None, deep_think=False, websearch=False, stream=True):
        messages = self.build_messages(query, history_messages, image_path, rag_chunks)
        model = self.VISION_MODEL if image_path else self.TEXT_MODEL
        return self.chat_stream(
            messages,
            model=model,
            deep_think=deep_think,
            websearch=websearch
        )
