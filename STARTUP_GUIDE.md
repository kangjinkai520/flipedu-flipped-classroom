# 项目启动说明

本项目包含三个服务：

- Spring Boot 后端：`http://127.0.0.1:18081`
- uni-app 前端：由 HBuilderX 启动，前端请求 `http://127.0.0.1:18081`
- AI 助手后端：`http://127.0.0.1:8000`

## 1. 启动 Spring Boot 后端

打开一个新的 PowerShell 窗口，执行：

```powershell
cd D:\graduation\flip-classroom-backend
D:\ProgramData\anaconda3\envs\graduation\Library\lib\jvm\bin\java.exe -jar target\flip-classroom-backend-0.0.1-SNAPSHOT.jar
```

看到类似 `Started FlipClassroomBackendApplication` 就说明启动成功。

这个窗口不要关闭，关闭后 `18081` 后端服务会停止。

## 2. 启动 AI 助手后端

智谱 AI 是国内平台，默认建议先使用直连方式。

打开一个新的 PowerShell 窗口，执行：

```powershell
cd D:\graduation\students-ai
D:\ProgramData\anaconda3\envs\kevin\python.exe manage.py runserver 127.0.0.1:8000 --noreload
```

看到 `Starting development server at http://127.0.0.1:8000/` 就说明启动成功。

这个窗口不要关闭，关闭后 AI 助手服务会停止。

## 3. 如果 AI 直连失败

如果 AI 助手提示无法生成答案、连接失败，或者 Python 后端访问智谱不通，再使用代理备用启动方式。

原因是：浏览器能访问外网，不代表 Python 进程一定能使用同一套网络环境。某些 VPN/代理客户端只接管浏览器，Python 后端需要显式配置代理。

代理备用启动命令：

```powershell
$env:HTTP_PROXY='http://127.0.0.1:6666'
$env:HTTPS_PROXY='http://127.0.0.1:6666'
cd D:\graduation\students-ai
D:\ProgramData\anaconda3\envs\kevin\python.exe manage.py runserver 127.0.0.1:8000 --noreload
```

说明：

- `127.0.0.1:6666` 是本机 VPN/flclient 暴露的代理端口，不是智谱 API 地址。
- 智谱本身是国内平台，正常网络下优先直连。
- 只有当前电脑直连失败时，才使用代理备用方式。

## 4. 启动前端

使用 HBuilderX 打开：

```text
D:\graduation\flip-classroom-uniapp
```

然后运行到浏览器或微信开发者工具。

前端配置文件：

```text
D:\graduation\flip-classroom-uniapp\common\config.js
```

关键地址应为：

```js
baseURL: 'http://127.0.0.1:18081'
aiBaseURL: 'http://127.0.0.1:8000'
```

## 5. 快速验证

Spring Boot：

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:18081/courses
```

AI 后端：

```powershell
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8000/ai/assistant-stream/?query=hello&user_id=1&save_history=0&use_rag=0"
```

两个后端窗口都保持打开后，再进入前端页面测试登录、课程、签到和 AI 助手。
