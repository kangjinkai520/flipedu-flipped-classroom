from django.urls import path
from . import views

urlpatterns = [
    # 用户认证
    path('register/', views.register, name='register'),
    path('login/', views.login, name='login'),
    # AI对话
    path('assistant-stream/', views.assistant_stream, name='assistant_stream'),
    path('chat/', views.chat, name='chat'),
    path('knowledge/', views.knowledge, name='knowledge'),
    # 历史记录
    path('list/', views.history_list, name='list'),
    path('detail/', views.detail, name='detail'),
    path('del/', views.deldetail, name='delete'),
    # 文件上传
    path('upload/', views.upload_image, name='upload'),
]