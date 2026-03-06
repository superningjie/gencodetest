#!/bin/bash
cd "$(dirname "$0")"
echo "启动金蝶星瀚HR AI顾问 Web服务..."
python3 web_ui/web_ui.py
