import json
import os
import uuid
from datetime import datetime

from django.conf import settings
from django.contrib.auth.hashers import check_password, make_password
from django.http import JsonResponse, StreamingHttpResponse
from django.views.decorators.csrf import csrf_exempt

from LLMModule.ai_utils import generate_title
from LLMModule.zhipu_llm import ZhipuLLM as LLM
from .models import ChatMessage, ChatTopic, User

try:
    from LLMModule.RAGBaseOfChromaDB import RAGBaseOfChromaDB
    rag = RAGBaseOfChromaDB(device="cpu")
    print("RAG module loaded")
except Exception as e:
    print(f"RAG module unavailable: {e}")
    rag = None


def _as_bool(value, default=True):
    if value is None:
        return default
    return str(value).strip().lower() not in {"0", "false", "off", "no"}


def _message_to_dict(message):
    return {
        "id": message.id,
        "topic_id": message.topic_id,
        "role": message.role,
        "content": message.content,
        "image_url": message.image_url,
        "created_at": message.created_at.isoformat() if message.created_at else None,
    }


def _topic_to_dict(topic):
    return {
        "id": topic.id,
        "user_id": topic.user_id,
        "title": topic.title,
        "image_url": topic.image_url,
        "created_at": topic.created_at.isoformat() if topic.created_at else None,
        "updated_at": topic.updated_at.isoformat() if topic.updated_at else None,
    }


def assistant_stream(request):
    query = request.GET.get("query") or request.GET.get("query123")
    if not query:
        return JsonResponse({"error": "缺少 query 参数"}, status=400)

    websearch = int(request.GET.get("websearch", 0))
    deep_think = int(request.GET.get("deepthink", 0) or request.GET.get("deep_think", 0) or 0)
    topic_id = int(request.GET.get("topic_id", 0) or 0)
    user_id = int(request.GET.get("user_id", 1) or 1)
    image_url = request.GET.get("image_url", "")
    save_history = _as_bool(request.GET.get("save_history", "1"), default=True)
    use_rag = _as_bool(request.GET.get("use_rag", "1"), default=True)
    window_size = max(2, min(50, int(request.GET.get("window_size", 10) or 10)))

    def generate_stream():
        nonlocal topic_id
        try:
            llm = LLM()

            if save_history and topic_id == 0:
                title = generate_title(query, max_length=20)
                topic = ChatTopic.objects.create(
                    user_id=user_id,
                    title=title,
                    image_url=image_url or "",
                    created_at=datetime.now(),
                    updated_at=datetime.now(),
                )
                topic_id = topic.id
                yield f"data: {json.dumps({'topic_id': topic_id}, ensure_ascii=False)}\n\n"

            if save_history and topic_id > 0:
                ChatMessage.objects.create(
                    topic_id=topic_id,
                    role="user",
                    content=query,
                    image_url=image_url or "",
                    created_at=datetime.now(),
                )
                history_messages = list(reversed(
                    ChatMessage.objects.filter(topic_id=topic_id).order_by("-created_at")[:window_size]
                ))
            else:
                history_messages = []

            rag_chunks = None
            if use_rag and rag is not None:
                try:
                    chunks = rag.query(query)
                    if chunks:
                        rag_chunks = chunks
                except Exception as e:
                    print(f"[RAG] query failed: {e}")

            image_path = None
            if image_url:
                image_path = str(settings.BASE_DIR / "static" / "upload" / os.path.basename(image_url))

            response = llm.chat_with_context(
                query=query,
                history_messages=history_messages,
                image_path=image_path,
                rag_chunks=rag_chunks,
                deep_think=deep_think,
                websearch=bool(websearch),
                stream=True,
            )

            full_content = ""
            for content_chunk in response:
                full_content += content_chunk
                yield f"data: {json.dumps({'content': content_chunk}, ensure_ascii=False)}\n\n"

            if save_history and topic_id > 0:
                ChatMessage.objects.create(
                    topic_id=topic_id,
                    role="assistant",
                    content=full_content,
                    created_at=datetime.now(),
                )

            yield "data: [DONE]\n\n"
        except Exception as e:
            print(f"[assistant_stream] failed: {e}")
            yield f"data: {json.dumps({'error': str(e)}, ensure_ascii=False)}\n\n"
            yield "data: [DONE]\n\n"

    response = StreamingHttpResponse(generate_stream(), content_type="text/event-stream; charset=utf-8")
    response["Cache-Control"] = "no-cache"
    response["X-Accel-Buffering"] = "no"
    return response


def chat(request):
    return JsonResponse({"message": "聊天"})


def knowledge(request):
    return JsonResponse({"message": "知识库"})


def history_list(request):
    try:
        user_id = int(request.GET.get("user_id", 1) or 1)
        topics = ChatTopic.objects.filter(user_id=user_id).order_by("-created_at")
        return JsonResponse({"history": [_topic_to_dict(topic) for topic in topics]})
    except Exception as e:
        return JsonResponse({"error": str(e)}, status=500)


def detail(request):
    try:
        topic_id = int(request.GET.get("id"))
        messages = ChatMessage.objects.filter(topic_id=topic_id).order_by("created_at")
        return JsonResponse({"messages": [_message_to_dict(message) for message in messages]})
    except Exception as e:
        return JsonResponse({"error": str(e)}, status=500)


def deldetail(request):
    try:
        topic_id = int(request.GET.get("id"))
        ChatMessage.objects.filter(topic_id=topic_id).delete()
        ChatTopic.objects.filter(id=topic_id).delete()
        return JsonResponse({"message": "删除成功"})
    except Exception as e:
        return JsonResponse({"error": str(e)}, status=500)


@csrf_exempt
def register(request):
    if request.method != "POST":
        return JsonResponse({"error": "只支持 POST"}, status=405)
    try:
        data = json.loads(request.body or "{}")
        username = data.get("username", "").strip()
        password = data.get("password", "").strip()
        if not username or not password:
            return JsonResponse({"success": False, "message": "账号和密码不能为空"}, status=400)
        if User.objects.filter(username=username).exists():
            return JsonResponse({"success": False, "message": "账号已存在"}, status=400)
        user = User.objects.create(username=username, password=make_password(password))
        return JsonResponse({"success": True, "message": "注册成功", "user": {"id": user.id, "username": user.username}})
    except Exception as e:
        return JsonResponse({"success": False, "message": str(e)}, status=500)


@csrf_exempt
def login(request):
    if request.method != "POST":
        return JsonResponse({"error": "只支持 POST"}, status=405)
    try:
        data = json.loads(request.body or "{}")
        username = data.get("username", "").strip()
        password = data.get("password", "").strip()
        user = User.objects.filter(username=username).first()
        if not user or not check_password(password, user.password):
            return JsonResponse({"success": False, "message": "账号或密码错误"}, status=401)
        return JsonResponse({"success": True, "message": "登录成功", "user": {"id": user.id, "username": user.username}})
    except Exception as e:
        return JsonResponse({"success": False, "message": str(e)}, status=500)


@csrf_exempt
def upload_image(request):
    if request.method != "POST":
        return JsonResponse({"error": "只支持 POST"}, status=405)
    try:
        image = request.FILES.get("image")
        if not image:
            return JsonResponse({"error": "未上传图片"}, status=400)

        allowed_types = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"}
        if image.content_type not in allowed_types:
            return JsonResponse({"error": "图片格式不支持"}, status=400)
        if image.size > 10 * 1024 * 1024:
            return JsonResponse({"error": "图片不能超过 10MB"}, status=400)

        ext = image.name.split(".")[-1]
        filename = f"{uuid.uuid4().hex}.{ext}"
        upload_dir = settings.BASE_DIR / "static" / "upload"
        os.makedirs(upload_dir, exist_ok=True)
        file_path = os.path.join(upload_dir, filename)
        with open(file_path, "wb+") as destination:
            for chunk in image.chunks():
                destination.write(chunk)

        image_url = f"{settings.STATIC_URL}upload/{filename}"
        return JsonResponse({"message": "上传成功", "url": image_url, "filename": filename})
    except Exception as e:
        return JsonResponse({"error": str(e)}, status=500)
