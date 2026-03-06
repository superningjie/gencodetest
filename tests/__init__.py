# 测试配置
import pytest
import sys
from pathlib import Path

# 添加项目根目录到路径
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

# 导入核心模块
import core
import core.data_models
import core.skill_engine
import core.pipeline_executor
