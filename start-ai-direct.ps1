$env:HTTP_PROXY = ''
$env:HTTPS_PROXY = ''
$env:http_proxy = ''
$env:https_proxy = ''
cd D:\graduation\doctors\backend
& 'D:\ProgramData\anaconda3\envs\kevin\python.exe' manage.py runserver 127.0.0.1:8000 --noreload
