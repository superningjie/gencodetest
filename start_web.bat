@echo off
chcp 65001
echo 启动金蝶星瀚HR AI顾问 Web服务...
cd /d "%~dp0"
python web_ui/web_ui.py
pause
