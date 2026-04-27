from django.db import migrations, models
from django.contrib.auth.hashers import make_password, identify_hasher


def hash_plaintext_passwords(apps, schema_editor):
    User = apps.get_model("ai_consultation", "User")

    for user in User.objects.all().only("id", "password"):
        raw_password = user.password or ""

        # 已是哈希则跳过
        try:
            identify_hasher(raw_password)
            continue
        except Exception:
            pass

        user.password = make_password(raw_password)
        user.save(update_fields=["password"])


class Migration(migrations.Migration):

    dependencies = [
        ("ai_consultation", "0003_user_remove_chattopic_user_id_chattopic_user"),
    ]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="password",
            field=models.CharField(max_length=128, verbose_name="密码"),
        ),
        migrations.RunPython(hash_plaintext_passwords, migrations.RunPython.noop),
    ]
