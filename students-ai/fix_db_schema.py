import os
import django
from django.db import connection

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'backend.settings')
django.setup()

def fix_database():
    with connection.cursor() as cursor:
        print("检查 ai_consultation_chattopic 表结构...")
        cursor.execute("DESCRIBE ai_consultation_chattopic")
        columns = [row[0] for row in cursor.fetchall()]
        print("当前列:", columns)
        
        if 'user_id' not in columns:
            print("发现缺少 user_id 列，正在尝试添加...")
            try:
                # 1. 添加user_id列
                # 假设ai_consultation_user表的id是BIGINT (Django默认BigAutoField)
                cursor.execute("ALTER TABLE ai_consultation_chattopic ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1")
                print("成功添加 user_id 列")
                
                # 2. 尝试添加外键约束
                try:
                    cursor.execute("""
                        ALTER TABLE ai_consultation_chattopic
                        ADD CONSTRAINT fk_ai_consultation_chattopic_user_id
                        FOREIGN KEY (user_id) REFERENCES ai_consultation_user(id)
                    """)
                    print("成功添加外键约束")
                except Exception as e:
                    print(f"添加外键约束失败 (可能是由于数据不一致或约束已存在): {e}")
                    
            except Exception as e:
                print(f"添加列失败: {e}")
        else:
            print("user_id 列已存在，无需添加。")

        #再次检查
        cursor.execute("DESCRIBE ai_consultation_chattopic")
        columns = [row[0] for row in cursor.fetchall()]
        print("最终列:", columns)

if __name__ == "__main__":
    fix_database()
