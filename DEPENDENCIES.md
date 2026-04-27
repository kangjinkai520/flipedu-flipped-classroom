# FlipEdu 依赖说明

本项目是一个多模块毕业设计项目，不是单一 Python 项目，所以根目录不放统一的 `requirements.txt`。各模块的依赖入口如下。

## 1. 后端业务服务

目录：

```text
flip-classroom-backend
```

依赖文件：

```text
flip-classroom-backend/pom.xml
```

主要环境：

- Java 17
- Maven
- Spring Boot 4.0.5
- MySQL

主要依赖：

- `spring-boot-starter-webmvc`
- `spring-boot-starter-jdbc`
- `mysql-connector-j`
- `spring-security-crypto`
- `lombok`

常用命令：

```bat
cd /d D:\graduation\flip-classroom-backend
D:\ProgramData\anaconda3\envs\graduation\Library\bin\mvn.cmd package -DskipTests
```

## 2. 前端应用

目录：

```text
flip-classroom-uniapp
```

主要环境：

- HBuilderX
- uni-app / Vue

说明：

当前前端主要由 HBuilderX 管理和运行，没有独立的根目录 `package.json` 依赖安装流程。打开 `flip-classroom-uniapp` 后运行到浏览器或小程序模拟器即可。

前端接口地址配置：

```text
flip-classroom-uniapp/common/config.js
```

默认地址：

```js
baseURL: 'http://127.0.0.1:18081'
aiBaseURL: 'http://127.0.0.1:8000'
```

## 3. AI 学习助手服务

目录：

```text
students-ai
```

依赖文件：

```text
students-ai/requirements.txt
```

主要环境：

- Python
- Django
- Django REST Framework
- 智谱 AI API

主要依赖：

- `Django`
- `djangorestframework`
- `django-cors-headers`
- `python-dotenv`
- `openai`
- `langchain`
- `chromadb`
- `sentence-transformers`
- `requests`
- `numpy`
- `pandas`
- `Pillow`

安装示例：

```bat
cd /d D:\graduation\students-ai
D:\ProgramData\anaconda3\envs\kevin\python.exe -m pip install -r requirements.txt
```

运行前复制配置模板：

```bat
copy students-ai\.env.example students-ai\.env
```

然后在 `.env` 中填写自己的智谱 API Key。

## 4. 不应提交的依赖和运行产物

以下内容属于本地依赖、密钥、数据库或构建产物，不应上传 GitHub：

- `node_modules/`
- `flip-classroom-backend/target/`
- `flip-classroom-uniapp/unpackage/`
- `students-ai/.env`
- `students-ai/db.sqlite3`
- `students-ai/chroma/`
- `students-ai/**/__pycache__/`
- 日志文件
- 上传图片和运行时缓存

这些内容已经在根目录 `.gitignore` 中排除。
