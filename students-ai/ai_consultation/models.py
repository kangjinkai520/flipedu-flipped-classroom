from django.db import models


class User(models.Model):
    username = models.CharField(max_length=50, unique=True)
    password = models.CharField(max_length=128)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "ai_consultation_user"

    def __str__(self):
        return self.username


class ChatTopic(models.Model):
    user = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name="topics",
        default=1
    )
    title = models.CharField(max_length=255, default="新对话")
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    image_url = models.CharField(max_length=500, blank=True, null=True, default="")

    class Meta:
        db_table = "ai_consultation_chattopic"
        ordering = ["-created_at"]

    def __str__(self):
        return f"{self.title} (user {self.user_id})"


class ChatMessage(models.Model):
    ROLE_CHOICES = [
        ("user", "用户"),
        ("assistant", "助手"),
    ]

    topic = models.ForeignKey(
        ChatTopic,
        on_delete=models.CASCADE,
        related_name="messages",
    )
    role = models.CharField(max_length=20, choices=ROLE_CHOICES)
    content = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    image_url = models.CharField(max_length=500, blank=True, null=True, default="")

    class Meta:
        db_table = "ai_consultation_chatmessage"
        ordering = ["created_at"]

    def __str__(self):
        return f"{self.role}: {self.content[:50]}..."
