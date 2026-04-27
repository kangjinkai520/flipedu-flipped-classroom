from dotenv import load_dotenv
from openai import OpenAI
import os

load_dotenv()

client = OpenAI(
    api_key=os.getenv("ZHIPU_API_KEY"),
    base_url=os.getenv("ZHIPU_BASE_URL", "https://open.bigmodel.cn/api/paas/v4/"),
)

response = client.chat.completions.create(
    model=os.getenv("ZHIPU_TEXT_MODEL", "glm-4-flash"),
    messages=[{"role": "user", "content": "你好"}],
)

print(response.choices[0].message.content)
