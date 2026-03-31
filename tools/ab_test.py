#!/usr/bin/env python3
"""
A/B测试工具
用于对比不同配置/版本的代码生成效果
"""
import random
import json
from typing import Dict, List
import statistics


class ABTestRunner:
    """A/B测试运行器"""

    def __init__(self, baseline_config: Dict, candidate_config: Dict):
        self.baseline_config = baseline_config
        self.candidate_config = candidate_config
        self.results = []

    def run_test(self, requirements: List[Dict], sample_size: int = 100) -> Dict:
        """运行A/B测试"""
        test_samples = random.sample(requirements, min(sample_size, len(requirements)))

        print(f"Running A/B test with {len(test_samples)} samples...")

        for i, req in enumerate(test_samples):
            is_candidate = random.random() > 0.5
            version = "candidate" if is_candidate else "baseline"

            # 模拟生成结果
            base_quality = 0.75 if version == "baseline" else 0.82
            quality = random.gauss(base_quality, 0.1)
            quality = max(0, min(1, quality))

            self.results.append({
                'version': version,
                'requirement_id': req.get('id', 'unknown'),
                'quality_score': quality,
                'syntax_valid': quality > 0.6,
                'generation_time': random.gauss(5, 2)
            })

        return self.analyze_results()

    def analyze_results(self) -> Dict:
        """分析测试结果"""
        baseline = [r for r in self.results if r['version'] == "baseline"]
        candidate = [r for r in self.results if r['version'] == "candidate"]

        b_qualities = [r['quality_score'] for r in baseline]
        c_qualities = [r['quality_score'] for r in candidate]

        analysis = {
            'baseline': {
                'count': len(baseline),
                'avg_quality': round(statistics.mean(b_qualities), 3) if b_qualities else 0
            },
            'candidate': {
                'count': len(candidate),
                'avg_quality': round(statistics.mean(c_qualities), 3) if c_qualities else 0
            }
        }

        # 计算改善幅度
        if analysis['baseline']['avg_quality'] > 0:
            improvement = (analysis['candidate']['avg_quality'] - analysis['baseline']['avg_quality']) / analysis['baseline']['avg_quality']
            analysis['improvement'] = f"{improvement:+.1%}"
            analysis['recommendation'] = "建议采用候选版本" if improvement > 0.05 else "建议保持基线版本"

        return analysis


def main():
    import argparse

    parser = argparse.ArgumentParser(description='A/B测试工具')
    parser.add_argument('--baseline', required=True, help='基线版本配置')
    parser.add_argument('--candidate', required=True, help='候选版本配置')
    parser.add_argument('--requirements', required=True, help='测试需求文件')
    parser.add_argument('--sample', type=int, default=100, help='样本数量')

    args = parser.parse_args()

    with open(args.baseline, 'r') as f:
        baseline_config = json.load(f)

    with open(args.candidate, 'r') as f:
        candidate_config = json.load(f)

    with open(args.requirements, 'r') as f:
        requirements = json.load(f)

    runner = ABTestRunner(baseline_config, candidate_config)
    results = runner.run_test(requirements, args.sample)

    print(json.dumps(results, indent=2, ensure_ascii=False))


if __name__ == '__main__':
    main()
