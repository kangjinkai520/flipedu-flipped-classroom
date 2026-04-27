$env:HTTP_PROXY = 'http://127.0.0.1:6666'
$env:HTTPS_PROXY = 'http://127.0.0.1:6666'
cd D:\graduation\students-ai
& 'D:\ProgramData\anaconda3\envs\kevin\python.exe' manage.py runserver 127.0.0.1:8000 --noreload
