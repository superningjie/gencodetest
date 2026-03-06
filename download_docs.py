#!/usr/bin/env python3
"""
下载金蝶苍穹开发平台文档
"""
import requests
import os
import json
import time

BASE_URL = "https://api.github.com/repos/superningjie/gencodetest/contents"
RAW_URL = "https://raw.githubusercontent.com/superningjie/gencodetest/feature-standard"
DOC_DIR = "sourcedoc/苍穹开发平台文档"

def get_file_list():
    """获取文件列表"""
    url = f"{BASE_URL}/sourcedoc/%E8%8B%8D%E7%A9%B9%E5%BC%80%E5%8F%91%E5%B9%B3%E5%8F%B0%E6%96%87%E6%A1%A3?ref=feature-standard"
    try:
        resp = requests.get(url, timeout=30)
        return resp.json()
    except Exception as e:
        print(f"Error: {e}")
        return []

def download_file(file_info, output_dir):
    """下载单个文件"""
    name = file_info['name']
    download_url = file_info['download_url']
    
    try:
        resp = requests.get(download_url, timeout=30)
        output_path = os.path.join(output_dir, name)
        with open(output_path, 'wb') as f:
            f.write(resp.content)
        print(f"✓ Downloaded: {name} ({len(resp.content)} bytes)")
        return True
    except Exception as e:
        print(f"✗ Failed: {name} - {e}")
        return False

if __name__ == "__main__":
    # 创建输出目录
    output_dir = "/root/.openclaw/workspace/gencodetest/sourcedoc/苍穹开发平台文档"
    os.makedirs(output_dir, exist_ok=True)
    
    print("Fetching file list...")
    files = get_file_list()
    print(f"Found {len(files)} files")
    
    success = 0
    for file_info in files:
        if file_info.get('type') == 'file':
            if download_file(file_info, output_dir):
                success += 1
            time.sleep(0.5)  # 避免请求过快
    
    print(f"\nTotal: {success}/{len(files)} files downloaded")
