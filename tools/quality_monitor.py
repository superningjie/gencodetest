#!/usr/bin/env python3
"""
代码生成质量监控脚本
用于收集指标、分析问题、生成报告
"""
import json
import sqlite3
from pathlib import Path
from datetime import datetime, timedelta
from typing import Dict, List
import argparse


class QualityMonitor:
    """质量监控器"""

    def __init__(self, db_path: str = "logs/quality.db"):
        self.db_path = db_path
        self.init_db()

    def init_db(self):
        """初始化数据库"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        # 创建表 - 使用顶格字符串避免缩进问题
        cursor.execute("""
CREATE TABLE IF NOT EXISTS generation_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp TEXT,
    requirement_id TEXT,
    requirement_desc TEXT,
    impl_type TEXT,
    language TEXT,
    quality_score REAL,
    syntax_valid INTEGER,
    style_score REAL,
    security_issues INTEGER,
    generation_time REAL,
    user_rating INTEGER,
    status TEXT
)
        """)

        cursor.execute("""
CREATE TABLE IF NOT EXISTS asset_usage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp TEXT,
    asset_id TEXT,
    usage_context TEXT,
    success INTEGER
)
        """)

        conn.commit()
        conn.close()

    def record_generation(self, record: Dict):
        """记录生成结果"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute("""
INSERT INTO generation_records 
(timestamp, requirement_id, requirement_desc, impl_type, language,
 quality_score, syntax_valid, style_score, security_issues,
 generation_time, user_rating, status)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            datetime.now().isoformat(),
            record.get('requirement_id'),
            record.get('requirement_desc', '')[:200],
            record.get('impl_type'),
            record.get('language'),
            record.get('quality_score', 0),
            1 if record.get('syntax_valid') else 0,
            record.get('style_score', 0),
            record.get('security_issues', 0),
            record.get('generation_time', 0),
            record.get('user_rating'),
            record.get('status', 'generated')
        ))

        conn.commit()
        conn.close()

    def get_metrics(self, days: int = 7) -> Dict:
        """获取质量指标"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        since = (datetime.now() - timedelta(days=days)).isoformat()

        cursor.execute("""
SELECT 
    COUNT(*) as total,
    AVG(quality_score) as avg_quality,
    SUM(syntax_valid) as syntax_valid_count,
    AVG(style_score) as avg_style,
    AVG(generation_time) as avg_time,
    AVG(user_rating) as avg_rating
FROM generation_records
WHERE timestamp > ?
        """, (since,))

        row = cursor.fetchone()

        metrics = {
            'period_days': days,
            'total_generations': row[0] or 0,
            'avg_quality_score': round(row[1] or 0, 3),
            'syntax_pass_rate': round((row[2] or 0) / max(row[0], 1), 3),
            'avg_style_score': round(row[3] or 0, 3),
            'avg_generation_time': round(row[4] or 0, 2),
            'avg_user_rating': round(row[5] or 0, 2)
        }

        cursor.execute("""
SELECT impl_type, COUNT(*), AVG(quality_score)
FROM generation_records
WHERE timestamp > ?
GROUP BY impl_type
        """, (since,))

        metrics['by_type'] = {
            row[0]: {'count': row[1], 'avg_quality': round(row[2], 3)}
            for row in cursor.fetchall()
        }

        conn.close()
        return metrics

    def generate_report(self, output_path: str = None):
        """生成质量报告"""
        metrics = self.get_metrics()

        report = f"""
# 代码生成质量报告
生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

## 总体指标

| 指标 | 数值 | 目标 | 状态 |
|-----|------|------|------|
| 总生成次数 | {metrics['total_generations']} | - | - |
| 平均质量分 | {metrics['avg_quality_score']:.3f} | >0.8 | {'✅' if metrics['avg_quality_score'] > 0.8 else '⚠️'} |
| 语法通过率 | {metrics['syntax_pass_rate']:.1%} | >95% | {'✅' if metrics['syntax_pass_rate'] > 0.95 else '⚠️'} |

## 优化建议

1. **工程层**: 检查并更新拆分规则
2. **知识层**: 补充高频使用场景的代码模板
3. **模型层**: 收集低质量案例，优化Prompt模板
"""

        if output_path:
            with open(output_path, 'w', encoding='utf-8') as f:
                f.write(report)

        return report


def main():
    parser = argparse.ArgumentParser(description='代码生成质量监控')
    parser.add_argument('--report', action='store_true', help='生成质量报告')
    parser.add_argument('--metrics', action='store_true', help='查看指标')
    parser.add_argument('--days', type=int, default=7, help='统计天数')

    args = parser.parse_args()

    monitor = QualityMonitor()

    if args.report:
        report = monitor.generate_report()
        print(report)
    elif args.metrics:
        metrics = monitor.get_metrics(args.days)
        print(json.dumps(metrics, indent=2, ensure_ascii=False))
    else:
        parser.print_help()


if __name__ == '__main__':
    main()
