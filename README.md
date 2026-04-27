# FlipEdu 翻转课堂教学平台

FlipEdu 是一个面向课程教学、课堂互动、作业测验、签到考勤与 AI 学习辅助的毕业设计项目。项目采用前后端分离结构，包含 Spring Boot 业务后端、uni-app 前端和 Python AI 学习助手服务。

## 项目结构

```text
D:\graduation
├─ flip-classroom-backend      Spring Boot 后端，端口 18081
├─ flip-classroom-uniapp       uni-app 前端，HBuilderX 运行
├─ students-ai                 Python/Django AI 助手后端，端口 8000
├─ README.md                   项目说明
└─ STARTUP_GUIDE.md            本地启动备忘
```

`students-ai` 由原 AI 后端目录整理而来，真实 `.env`、数据库文件、Chroma 数据和上传图片不会进入仓库。

## 核心功能

- 账号与角色：管理员、教师、学生三类角色登录；支持账号停用，管理员账号受保护不可停用。
- 课程管理：教师创建课程、编辑课程，管理员查看课程、课程成员和课程状态。
- 审核中心：管理员审核教师发布的课程、教学资料、课程公告、随堂测验、课后作业，以及学生加入课程申请。
- 教学资料：教师上传/发布资料，学生按审核通过状态查看。
- 签到管理：支持普通签到和定位签到，学生可提交签到，异常签到由教师审核。
- 随堂测验：教师发布测验，学生作答，成绩反馈中可查看正确答案、我的答案和最终分数。
- 课后作业：教师发布作业，学生提交，教师批改，学生端显示作业完成进度。
- 成绩反馈：学生查看测验、作业等学习结果明细。
- AI 学习助手：课程详情页内置 AI 抽屉，支持快捷问题、流式输出、上下文历史和图片输入；Python 后端异常时前端不会白屏。
- 管理员模块：总览、课程管理、课程成员、用户管理、审核中心。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | uni-app / Vue，HBuilderX 运行 |
| 业务后端 | Java 17，Spring Boot 4，JDBC，MySQL |
| AI 后端 | Python，Django，智谱 AI API |
| 数据库 | MySQL，AI 服务可使用 SQLite/MySQL |

依赖说明见：

```text
DEPENDENCIES.md
```

## 本地端口

| 服务 | 地址 |
| --- | --- |
| Spring Boot 后端 | `http://127.0.0.1:18081` |
| AI 助手后端 | `http://127.0.0.1:8000` |
| 前端请求后端 | `http://127.0.0.1:18081` |
| 前端请求 AI | `http://127.0.0.1:8000` |

前端地址配置在：

```text
flip-classroom-uniapp/common/config.js
```

## 数据库初始化

后端数据库默认使用：

```text
database: flip_classroom
```

初始化 SQL 位于：

```text
flip-classroom-backend/src/main/resources/sql/
├─ midterm_schema.sql
├─ midterm_demo_data.sql
└─ sys_user_init.sql
```

建议先创建数据库，再按 schema、初始化用户、演示数据的顺序导入。

## 配置文件

后端真实配置文件不建议上传 GitHub。首次运行时复制模板：

```bat
copy flip-classroom-backend\src\main\resources\application.example.properties ^
     flip-classroom-backend\src\main\resources\application.properties
```

然后修改本机 MySQL 用户名和密码。

AI 后端复制 `.env.example` 为 `.env`：

```bat
copy students-ai\.env.example students-ai\.env
```

再填入自己的智谱 API Key。

## 启动 Spring Boot 后端

推荐使用 cmd 窗口启动，并保持窗口不要关闭：

```bat
cd /d D:\graduation\flip-classroom-backend
D:\ProgramData\anaconda3\envs\graduation\Library\lib\jvm\bin\java.exe -jar target\flip-classroom-backend-0.0.1-SNAPSHOT.jar
```

如果 jar 不存在，先打包：

```bat
cd /d D:\graduation\flip-classroom-backend
D:\ProgramData\anaconda3\envs\graduation\Library\bin\mvn.cmd package -DskipTests
```

看到类似下面日志表示启动成功：

```text
Started FlipClassroomBackendApplication
Tomcat started on port 18081
```

## 启动 AI 后端

AI 后端使用 `kevin` Python 环境：

```bat
cd /d D:\graduation\students-ai
D:\ProgramData\anaconda3\envs\kevin\python.exe manage.py runserver 127.0.0.1:8000 --noreload
```

如果开启 VPN 且智谱接口需要走本地代理，可先设置代理：

```bat
set HTTP_PROXY=http://127.0.0.1:6666
set HTTPS_PROXY=http://127.0.0.1:6666
```

## 启动前端

使用 HBuilderX 打开：

```text
D:\graduation\flip-classroom-uniapp
```

运行到浏览器或小程序模拟器。前端会请求：

```js
baseURL: 'http://127.0.0.1:18081'
aiBaseURL: 'http://127.0.0.1:8000'
```

## 常用验证

验证 Spring Boot：

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:18081/courses
```

验证审核中心接口：

```powershell
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:18081/admin/reviews?status=PENDING"
```

验证 AI 服务：

```powershell
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8000/ai/assistant-stream/?query=hello&user_id=1&save_history=0&use_rag=0"
```

## GitHub 上传注意事项

不要上传以下内容：

- `node_modules/`
- `target/`
- `unpackage/`
- `.env`
- `application.properties`
- 数据库文件、日志文件、临时文件
- 任何真实 API Key、数据库密码、个人账号 token

本仓库根目录已提供 `.gitignore`，上传前请确认真实密钥没有进入 Git 暂存区。
