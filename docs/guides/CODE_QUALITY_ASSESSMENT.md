# 代码质量评估体系

> **目标**: 建立多维度的代码质量度量体系，确保生成代码达到生产标准

---

## 1. 质量评估维度

### 1.1 四维质量模型

```
                    ┌─────────────────────┐
                    │    Code Quality     │
                    └──────────┬──────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
    ┌────▼────┐          ┌────▼────┐          ┌────▼────┐
    │ Syntax  │          │  Style  │          │Security │
    │  正确性  │          │  规范性  │          │  安全性  │
    └────┬────┘          └────┬────┘          └────┬────┘
         │                     │                     │
         └─────────────────────┼─────────────────────┘
                               │
                         ┌────▼────┐
                         │Maintain │
                         │ ability │
                         │ 可维护性 │
                         └─────────┘
```

| 维度 | 权重 | 核心指标 | 检查工具 |
|------|------|---------|---------|
| 语法正确性 | 40% | 编译通过率、AST 完整性 | tree-sitter、编译器 |
| 规范符合度 | 25% | 命名规范、格式规范、注释覆盖率 | Checkstyle、自定义规则 |
| 安全性 | 25% | 漏洞扫描、敏感信息检测 | Bandit、Semgrep |
| 可维护性 | 10% | 圈复杂度、代码重复率 | Radon、SonarQube |

---

## 2. 语法正确性检查

### 2.1 tree-sitter 集成

```python
# core/quality/syntax_checker.py
from tree_sitter import Language, Parser
import tree_sitter_java as ts_java
import tree_sitter_kotlin as ts_kotlin
from dataclasses import dataclass
from typing import List, Optional

@dataclass
class SyntaxError:
    """语法错误"""
    message: str
    line: int
    column: int
    error_type: str
    severity: str  # error, warning

@dataclass
class SyntaxCheckResult:
    """语法检查结果"""
    is_valid: bool
    errors: List[SyntaxError]
    ast_stats: dict
    language: str

class TreeSitterSyntaxChecker:
    """基于 tree-sitter 的语法检查器"""
    
    # 语言解析器映射
    PARSERS = {
        'java': Parser(Language(ts_java.language())),
        'kotlin': Parser(Language(ts_kotlin.language())),
        # 'sql': Parser(Language(ts_sql.language())),  # 待添加
    }
    
    # 错误节点类型
    ERROR_NODE_TYPES = {'ERROR', 'MISSING'}
    
    def check(self, code: str, language: str) -> SyntaxCheckResult:
        """
        检查代码语法
        
        Args:
            code: 源代码
            language: 编程语言 (java, kotlin)
            
        Returns:
            语法检查结果
        """
        parser = self.PARSERS.get(language)
        if not parser:
            return SyntaxCheckResult(
                is_valid=True,  # 不支持的语言默认通过
                errors=[],
                ast_stats={},
                language=language
            )
        
        try:
            # 解析代码
            tree = parser.parse(bytes(code, 'utf8'))
            root_node = tree.root_node
            
            # 收集错误
            errors = self._collect_syntax_errors(root_node)
            
            # AST 统计
            ast_stats = self._analyze_ast(root_node)
            
            return SyntaxCheckResult(
                is_valid=len(errors) == 0,
                errors=errors,
                ast_stats=ast_stats,
                language=language
            )
            
        except Exception as e:
            return SyntaxCheckResult(
                is_valid=False,
                errors=[SyntaxError(
                    message=f"Parse error: {str(e)}",
                    line=0,
                    column=0,
                    error_type="PARSE_EXCEPTION",
                    severity="error"
                )],
                ast_stats={},
                language=language
            )
    
    def _collect_syntax_errors(self, node) -> List[SyntaxError]:
        """递归收集语法错误"""
        errors = []
        
        if node.type in self.ERROR_NODE_TYPES:
            errors.append(SyntaxError(
                message=f"Syntax error: {node.type}",
                line=node.start_point[0] + 1,
                column=node.start_point[1],
                error_type=node.type,
                severity="error"
            ))
        
        # 检查特定错误模式
        if node.type == 'class_declaration':
            errors.extend(self._check_class_declaration(node))
        elif node.type == 'method_declaration':
            errors.extend(self._check_method_declaration(node))
        
        # 递归检查子节点
        for child in node.children:
            errors.extend(self._collect_syntax_errors(child))
        
        return errors
    
    def _check_class_declaration(self, node) -> List[SyntaxError]:
        """检查类声明的特定规则"""
        errors = []
        
        # 检查类名是否符合驼峰命名
        name_node = node.child_by_field_name('name')
        if name_node:
            class_name = name_node.text.decode('utf8')
            if not class_name[0].isupper():
                errors.append(SyntaxError(
                    message=f"Class name should start with uppercase: {class_name}",
                    line=name_node.start_point[0] + 1,
                    column=name_node.start_point[1],
                    error_type="NAMING_CONVENTION",
                    severity="warning"
                ))
        
        return errors
    
    def _check_method_declaration(self, node) -> List[SyntaxError]:
        """检查方法声明的特定规则"""
        errors = []
        
        # 检查方法体是否存在
        body = node.child_by_field_name('body')
        if not body:
            # 可能是抽象方法，检查 abstract 修饰符
            modifiers = node.child_by_field_name('modifiers')
            is_abstract = False
            if modifiers:
                for child in modifiers.children:
                    if child.text.decode('utf8') == 'abstract':
                        is_abstract = True
                        break
            
            if not is_abstract:
                errors.append(SyntaxError(
                    message="Non-abstract method must have a body",
                    line=node.start_point[0] + 1,
                    column=node.start_point[1],
                    error_type="MISSING_METHOD_BODY",
                    severity="error"
                ))
        
        return errors
    
    def _analyze_ast(self, node) -> dict:
        """分析 AST 统计信息"""
        stats = {
            'total_nodes': 0,
            'max_depth': 0,
            'classes': 0,
            'methods': 0,
            'fields': 0
        }
        
        def traverse(n, depth=0):
            stats['total_nodes'] += 1
            stats['max_depth'] = max(stats['max_depth'], depth)
            
            if n.type == 'class_declaration':
                stats['classes'] += 1
            elif n.type == 'method_declaration':
                stats['methods'] += 1
            elif n.type == 'field_declaration':
                stats['fields'] += 1
            
            for child in n.children:
                traverse(child, depth + 1)
        
        traverse(node)
        return stats
```

### 2.2 Java 编译器验证

```python
import subprocess
import tempfile
import os
from pathlib import Path

class JavaCompilerChecker:
    """使用 javac 进行编译验证"""
    
    def __init__(self, java_home: Optional[str] = None):
        self.java_home = java_home or os.getenv('JAVA_HOME')
        self.javac_path = self._find_javac()
    
    def _find_javac(self) -> str:
        """查找 javac 路径"""
        if self.java_home:
            javac = Path(self.java_home) / 'bin' / 'javac'
            if javac.exists():
                return str(javac)
        
        # 尝试从 PATH 查找
        result = subprocess.run(['which', 'javac'], capture_output=True, text=True)
        if result.returncode == 0:
            return result.stdout.strip()
        
        raise RuntimeError("javac not found")
    
    def compile_check(self, code: str, class_name: str) -> SyntaxCheckResult:
        """
        编译检查代码
        
        流程：
        1. 创建临时文件
        2. 调用 javac 编译
        3. 解析错误输出
        4. 清理临时文件
        """
        with tempfile.TemporaryDirectory() as tmpdir:
            # 写入源代码
            source_file = Path(tmpdir) / f"{class_name}.java"
            source_file.write_text(code, encoding='utf8')
            
            # 编译
            result = subprocess.run(
                [self.javac_path, '-d', tmpdir, str(source_file)],
                capture_output=True,
                text=True
            )
            
            if result.returncode == 0:
                return SyntaxCheckResult(
                    is_valid=True,
                    errors=[],
                    ast_stats={'compiled': True},
                    language='java'
                )
            else:
                errors = self._parse_javac_errors(result.stderr)
                return SyntaxCheckResult(
                    is_valid=False,
                    errors=errors,
                    ast_stats={'compiled': False},
                    language='java'
                )
    
    def _parse_javac_errors(self, stderr: str) -> List[SyntaxError]:
        """解析 javac 错误输出"""
        errors = []
        
        for line in stderr.strip().split('\n'):
            # 解析错误格式: File.java:line:column: error: message
            if ': error:' in line:
                parts = line.split(':')
                if len(parts) >= 4:
                    line_num = int(parts[1]) if parts[1].isdigit() else 0
                    col_num = int(parts[2]) if parts[2].isdigit() else 0
                    message = ':'.join(parts[3:]).replace(' error:', '').strip()
                    
                    errors.append(SyntaxError(
                        message=message,
                        line=line_num,
                        column=col_num,
                        error_type="COMPILATION_ERROR",
                        severity="error"
                    ))
        
        return errors
```

---

## 3. 编码规范检查

### 3.1 金蝶编码规范规则集

```python
# core/quality/style_checker.py
import re
from dataclasses import dataclass
from typing import List

@dataclass
class StyleViolation:
    """规范违规"""
    rule_id: str
    message: str
    line: int
    column: int
    severity: str  # error, warning, info

class KingdeeStyleChecker:
    """金蝶编码规范检查器"""
    
    RULES = {
        'KD001': {
            'name': 'ClassNaming',
            'description': '类名必须使用大驼峰命名法',
            'pattern': r'^([A-Z][a-zA-Z0-9]*)$',
            'severity': 'error'
        },
        'KD002': {
            'name': 'MethodNaming',
            'description': '方法名必须使用小驼峰命名法',
            'pattern': r'^([a-z][a-zA-Z0-9]*)$',
            'severity': 'error'
        },
        'KD003': {
            'name': 'ConstantNaming',
            'description': '常量必须使用全大写下划线命名',
            'pattern': r'^([A-Z_][A-Z0-9_]*)$',
            'severity': 'error'
        },
        'KD004': {
            'name': 'PackageNaming',
            'description': '包名必须使用全小写',
            'pattern': r'^([a-z][a-z0-9]*)(\.[a-z][a-z0-9]*)*$',
            'severity': 'error'
        },
        'KD005': {
            'name': 'LineLength',
            'description': '单行长度不超过 120 字符',
            'max_length': 120,
            'severity': 'warning'
        },
        'KD006': {
            'name': 'ImportWildcard',
            'description': '禁止 import 通配符',
            'pattern': r'import\s+[\w.]+\.\*;',
            'severity': 'warning'
        },
        'KD007': {
            'name': 'TodoComment',
            'description': '禁止提交 TODO 注释',
            'pattern': r'(?i)TODO|FIXME|XXX',
            'severity': 'warning'
        },
        'KD008': {
            'name': 'MagicNumber',
            'description': '魔法数字应该定义为常量',
            'pattern': r'(?!<.*>)(\b\d{3,}\b|\b[0-9]{2,}\b)',
            'severity': 'info'
        }
    }
    
    def check(self, code: str, language: str = 'java') -> List[StyleViolation]:
        """检查代码规范"""
        violations = []
        lines = code.split('\n')
        
        for rule_id, rule in self.RULES.items():
            if 'pattern' in rule:
                violations.extend(
                    self._check_pattern(rule_id, rule, lines)
                )
            elif 'max_length' in rule:
                violations.extend(
                    self._check_line_length(rule_id, rule, lines)
                )
        
        return violations
    
    def _check_pattern(
        self,
        rule_id: str,
        rule: dict,
        lines: List[str]
    ) -> List[StyleViolation]:
        """基于正则的模式检查"""
        violations = []
        pattern = re.compile(rule['pattern'])
        
        for line_num, line in enumerate(lines, 1):
            matches = pattern.finditer(line)
            for match in matches:
                # 检查是否应该被排除
                if self._should_exclude(rule_id, line):
                    continue
                
                violations.append(StyleViolation(
                    rule_id=rule_id,
                    message=rule['description'],
                    line=line_num,
                    column=match.start() + 1,
                    severity=rule['severity']
                ))
        
        return violations
    
    def _check_line_length(
        self,
        rule_id: str,
        rule: dict,
        lines: List[str]
    ) -> List[StyleViolation]:
        """检查行长度"""
        violations = []
        max_length = rule['max_length']
        
        for line_num, line in enumerate(lines, 1):
            if len(line) > max_length:
                violations.append(StyleViolation(
                    rule_id=rule_id,
                    message=f"Line too long ({len(line)} > {max_length})",
                    line=line_num,
                    column=max_length + 1,
                    severity=rule['severity']
                ))
        
        return violations
    
    def _should_exclude(self, rule_id: str, line: str) -> bool:
        """检查是否应该排除此行"""
        # 注释行排除某些检查
        if line.strip().startswith('//'):
            return rule_id in ['KD008']  # 魔法数字检查排除注释
        return False
```

### 3.2 JavaDoc 检查

```python
class JavaDocChecker:
    """JavaDoc 注释检查器"""
    
    REQUIRED_TAGS = {
        'public': ['@param', '@return', '@throws'],
        'protected': ['@param', '@return', '@throws']
    }
    
    def check(self, code: str) -> List[StyleViolation]:
        """检查 JavaDoc 完整性"""
        violations = []
        
        # 解析类和方法
        classes = self._extract_classes(code)
        methods = self._extract_methods(code)
        
        # 检查类的 JavaDoc
        for cls in classes:
            if not cls['javadoc']:
                violations.append(StyleViolation(
                    rule_id='JD001',
                    message=f"Class {cls['name']} missing JavaDoc",
                    line=cls['line'],
                    column=1,
                    severity='warning'
                ))
        
        # 检查方法的 JavaDoc
        for method in methods:
            if method['access'] in self.REQUIRED_TAGS:
                if not method['javadoc']:
                    violations.append(StyleViolation(
                        rule_id='JD002',
                        message=f"Method {method['name']} missing JavaDoc",
                        line=method['line'],
                        column=1,
                        severity='warning'
                    ))
                else:
                    # 检查必要的标签
                    required = self.REQUIRED_TAGS[method['access']]
                    missing = self._check_missing_tags(
                        method['javadoc'],
                        required,
                        method
                    )
                    for tag in missing:
                        violations.append(StyleViolation(
                            rule_id='JD003',
                            message=f"Missing {tag} tag",
                            line=method['line'],
                            column=1,
                            severity='warning'
                        ))
        
        return violations
```

---

## 4. 安全性检查

### 4.1 安全规则集

```python
# core/quality/security_checker.py
from typing import List, Dict
import re

@dataclass
class SecurityIssue:
    """安全问题"""
    severity: str  # critical, high, medium, low
    category: str
    message: str
    line: int
    code_snippet: str
    remediation: str

class SecurityChecker:
    """代码安全检查器"""
    
    # 安全规则定义
    SECURITY_RULES = [
        {
            'id': 'SEC001',
            'name': 'SQL Injection',
            'severity': 'critical',
            'pattern': r'(?:Statement|PreparedStatement).*\+.*\$\{?\w+\}?',
            'message': 'Potential SQL injection vulnerability',
            'remediation': 'Use PreparedStatement with parameterized queries'
        },
        {
            'id': 'SEC002',
            'name': 'Hardcoded Password',
            'severity': 'critical',
            'pattern': r'(?i)(password|passwd|pwd)\s*=\s*["\'][^"\']+["\']',
            'message': 'Hardcoded password detected',
            'remediation': 'Use environment variables or secret management'
        },
        {
            'id': 'SEC003',
            'name': 'Hardcoded API Key',
            'severity': 'critical',
            'pattern': r'(?i)(api[_-]?key|apikey|token)\s*=\s*["\']\w+["\']',
            'message': 'Hardcoded API key detected',
            'remediation': 'Use secure configuration management'
        },
        {
            'id': 'SEC004',
            'name': 'Weak Random',
            'severity': 'high',
            'pattern': r'new\s+Random\(',
            'message': 'Using java.util.Random for security purposes',
            'remediation': 'Use java.security.SecureRandom for cryptographic operations'
        },
        {
            'id': 'SEC005',
            'name': 'XSS Risk',
            'severity': 'high',
            'pattern': r'response\.getWriter\(\)\.print\s*\(\s*[^)]+\$\{?\w+\}?',
            'message': 'Potential XSS vulnerability',
            'remediation': 'Escape output or use templating engine with auto-escaping'
        },
        {
            'id': 'SEC006',
            'name': 'Insecure Deserialization',
            'severity': 'critical',
            'pattern': r'ObjectInputStream.*readObject',
            'message': 'Insecure deserialization detected',
            'remediation': 'Validate input before deserialization or use safe formats like JSON'
        },
        {
            'id': 'SEC007',
            'name': 'Path Traversal',
            'severity': 'high',
            'pattern': r'new\s+File\s*\(\s*[^)]+\$\{?\w+\}?',
            'message': 'Potential path traversal vulnerability',
            'remediation': 'Validate and sanitize file paths'
        },
        {
            'id': 'SEC008',
            'name': 'Debug Code',
            'severity': 'medium',
            'pattern': r'(?i)System\.out\.print|printStackTrace\(\)',
            'message': 'Debug code should not be in production',
            'remediation': 'Use logging framework instead'
        }
    ]
    
    def check(self, code: str, language: str = 'java') -> List[SecurityIssue]:
        """执行安全扫描"""
        issues = []
        lines = code.split('\n')
        
        for rule in self.SECURITY_RULES:
            pattern = re.compile(rule['pattern'])
            
            for line_num, line in enumerate(lines, 1):
                matches = pattern.finditer(line)
                
                for match in matches:
                    # 检查是否在注释中
                    if self._is_in_comment(line, match.start()):
                        continue
                    
                    issues.append(SecurityIssue(
                        severity=rule['severity'],
                        category=rule['name'],
                        message=rule['message'],
                        line=line_num,
                        code_snippet=line.strip(),
                        remediation=rule['remediation']
                    ))
        
        return issues
    
    def _is_in_comment(self, line: str, pos: int) -> bool:
        """检查位置是否在注释中"""
        # 简单检查：如果在 // 之后
        comment_start = line.find('//')
        if comment_start != -1 and pos > comment_start:
            return True
        return False
```

### 4.2 集成 Bandit (Python)

```python
import subprocess
import json

class BanditSecurityChecker:
    """集成 Bandit 进行 Python 安全检查"""
    
    def check(self, code: str) -> List[SecurityIssue]:
        """使用 Bandit 扫描 Python 代码"""
        with tempfile.NamedTemporaryFile(
            mode='w',
            suffix='.py',
            delete=False
        ) as f:
            f.write(code)
            temp_path = f.name
        
        try:
            result = subprocess.run(
                ['bandit', '-f', 'json', temp_path],
                capture_output=True,
                text=True
            )
            
            if result.stdout:
                report = json.loads(result.stdout)
                return self._parse_bandit_report(report)
            
            return []
        finally:
            os.unlink(temp_path)
    
    def _parse_bandit_report(self, report: dict) -> List[SecurityIssue]:
        """解析 Bandit 报告"""
        issues = []
        
        for result in report.get('results', []):
            issues.append(SecurityIssue(
                severity=result['issue_severity'].lower(),
                category=result['test_name'],
                message=result['issue_text'],
                line=result['line_number'],
                code_snippet=result['code'],
                remediation=result.get('more_info', 'Review Bandit documentation')
            ))
        
        return issues
```

---

## 5. 可维护性分析

### 5.1 圈复杂度计算

```python
# core/quality/complexity_analyzer.py
from tree_sitter import TreeCursor
from typing import Dict

class ComplexityAnalyzer:
    """代码复杂度分析器"""
    
    # 增加复杂度的节点类型
    COMPLEXITY_NODES = {
        'java': [
            'if_statement',
            'for_statement',
            'while_statement',
            'do_statement',
            'catch_clause',
            'switch_label',  # case/default
            'conditional_expression',  # ternary
            'binary_expression'  # &&, ||
        ]
    }
    
    def calculate_cyclomatic_complexity(
        self,
        method_node,
        language: str = 'java'
    ) -> int:
        """
        计算圈复杂度 (McCabe)
        
        公式: M = E - N + 2P
        简化: 从 1 开始，每遇到一个分支节点 +1
        """
        complexity = 1
        target_types = self.COMPLEXITY_NODES.get(language, [])
        
        def traverse(node):
            nonlocal complexity
            
            if node.type in target_types:
                # 对于逻辑运算符，每个增加 1
                if node.type == 'binary_expression':
                    operator = self._get_operator(node)
                    if operator in ['&&', '||']:
                        complexity += 1
                else:
                    complexity += 1
            
            for child in node.children:
                traverse(child)
        
        traverse(method_node)
        return complexity
    
    def calculate_cognitive_complexity(
        self,
        method_node,
        language: str = 'java'
    ) -> int:
        """
        计算认知复杂度 (Cognitive Complexity)
        
        规则：
        - 基础复杂度: 0
        - 结构增量: if/for/while/catch 等 +1
        - 嵌套增量: 每多一层嵌套 +1
        - 逻辑增量: && 和 || 每个 +1
        """
        complexity = 0
        target_types = self.COMPLEXITY_NODES.get(language, [])
        
        def traverse(node, nesting_level=0):
            nonlocal complexity
            
            if node.type in target_types:
                # 结构增量
                complexity += 1
                
                # 嵌套增量
                if nesting_level > 0:
                    complexity += nesting_level
                
                # 递归子节点，嵌套层数 +1
                for child in node.children:
                    traverse(child, nesting_level + 1)
            else:
                # 逻辑增量
                if node.type == 'binary_expression':
                    operator = self._get_operator(node)
                    if operator in ['&&', '||']:
                        complexity += 1
                
                # 非分支节点，保持当前嵌套层数
                for child in node.children:
                    traverse(child, nesting_level)
        
        traverse(method_node)
        return complexity
    
    def _get_operator(self, node) -> str:
        """获取二元表达式的运算符"""
        for child in node.children:
            if child.type in ['&&', '||', '==', '!=', '<', '>', '<=', '>=']:
                return child.type
        return ''
```

### 5.2 代码重复检测

```python
from difflib import SequenceMatcher
from typing import List, Tuple

class DuplicationDetector:
    """代码重复检测器"""
    
    def __init__(self, min_lines: int = 6):
        self.min_lines = min_lines
    
    def detect(self, code: str) -> List[Dict]:
        """
        检测代码重复
        
        算法：
        1. 将代码按行分割
        2. 滑动窗口提取代码块
        3. 计算代码块之间的相似度
        4. 聚类相似的代码块
        """
        lines = code.split('\n')
        duplicates = []
        
        # 提取所有代码块
        blocks = []
        for i in range(len(lines) - self.min_lines + 1):
            block = '\n'.join(lines[i:i + self.min_lines])
            normalized = self._normalize(block)
            blocks.append({
                'start': i + 1,
                'end': i + self.min_lines,
                'content': block,
                'normalized': normalized
            })
        
        # 比较代码块
        for i in range(len(blocks)):
            for j in range(i + 1, len(blocks)):
                similarity = self._calculate_similarity(
                    blocks[i]['normalized'],
                    blocks[j]['normalized']
                )
                
                if similarity > 0.8:  # 80% 相似度阈值
                    duplicates.append({
                        'block1': blocks[i],
                        'block2': blocks[j],
                        'similarity': similarity
                    })
        
        return duplicates
    
    def _normalize(self, code: str) -> str:
        """标准化代码用于比较"""
        # 移除注释
        lines = []
        for line in code.split('\n'):
            # 移除行尾注释
            if '//' in line:
                line = line[:line.index('//')]
            lines.append(line)
        
        # 移除空白字符
        normalized = '\n'.join(
            line.strip() for line in lines if line.strip()
        )
        
        # 替换标识符为通用标记
        import re
        normalized = re.sub(r'\b[a-zA-Z_]\w*\b', 'ID', normalized)
        normalized = re.sub(r'\b\d+\b', 'NUM', normalized)
        
        return normalized
    
    def _calculate_similarity(self, s1: str, s2: str) -> float:
        """计算两个字符串的相似度"""
        return SequenceMatcher(None, s1, s2).ratio()
```

---

## 6. 综合质量报告

### 6.1 质量评分计算

```python
from dataclasses import dataclass
from typing import List

@dataclass
class QualityReport:
    """综合质量报告"""
    
    # 各维度评分 (0-100)
    syntax_score: float
    style_score: float
    security_score: float
    maintainability_score: float
    
    # 详细结果
    syntax_errors: List[SyntaxError]
    style_violations: List[StyleViolation]
    security_issues: List[SecurityIssue]
    complexity_metrics: Dict
    
    # 统计
    total_lines: int
    code_lines: int
    comment_lines: int
    blank_lines: int
    
    @property
    def overall_score(self) -> float:
        """综合质量分 (0-100)"""
        weights = {
            'syntax': 0.40,
            'style': 0.25,
            'security': 0.25,
            'maintainability': 0.10
        }
        
        return (
            self.syntax_score * weights['syntax'] +
            self.style_score * weights['style'] +
            self.security_score * weights['security'] +
            self.maintainability_score * weights['maintainability']
        )
    
    @property
    def grade(self) -> str:
        """质量等级"""
        score = self.overall_score
        if score >= 90:
            return 'A (优秀)'
        elif score >= 80:
            return 'B (良好)'
        elif score >= 70:
            return 'C (及格)'
        elif score >= 60:
            return 'D (待改进)'
        else:
            return 'F (不合格)'


class QualityAggregator:
    """质量聚合器"""
    
    def __init__(self):
        self.syntax_checker = TreeSitterSyntaxChecker()
        self.style_checker = KingdeeStyleChecker()
        self.security_checker = SecurityChecker()
        self.complexity_analyzer = ComplexityAnalyzer()
    
    def generate_report(self, code: str, language: str = 'java') -> QualityReport:
        """生成完整质量报告"""
        
        # 语法检查
        syntax_result = self.syntax_checker.check(code, language)
        syntax_score = 100 if syntax_result.is_valid else max(0, 100 - len(syntax_result.errors) * 10)
        
        # 规范检查
        style_violations = self.style_checker.check(code, language)
        style_score = max(0, 100 - len(style_violations) * 5)
        
        # 安全检查
        security_issues = self.security_checker.check(code, language)
        security_score = self._calculate_security_score(security_issues)
        
        # 可维护性分析
        maintainability_score = self._calculate_maintainability_score(code)
        
        # 代码统计
        stats = self._count_lines(code)
        
        return QualityReport(
            syntax_score=syntax_score,
            style_score=style_score,
            security_score=security_score,
            maintainability_score=maintainability_score,
            syntax_errors=syntax_result.errors,
            style_violations=style_violations,
            security_issues=security_issues,
            complexity_metrics={},
            **stats
        )
    
    def _calculate_security_score(self, issues: List[SecurityIssue]) -> float:
        """计算安全评分"""
        deductions = {
            'critical': 30,
            'high': 15,
            'medium': 5,
            'low': 2
        }
        
        total_deduction = sum(
            deductions.get(issue.severity, 0)
            for issue in issues
        )
        
        return max(0, 100 - total_deduction)
    
    def _calculate_maintainability_score(self, code: str) -> float:
        """计算可维护性评分"""
        # 简化计算：基于注释率和代码长度
        lines = code.split('\n')
        comment_lines = sum(1 for l in lines if l.strip().startswith('//'))
        code_lines = len([l for l in lines if l.strip()])
        
        if code_lines == 0:
            return 100
        
        comment_ratio = comment_lines / code_lines
        
        # 理想注释率 10-20%
        if 0.1 <= comment_ratio <= 0.2:
            return 100
        elif comment_ratio < 0.1:
            return 50 + comment_ratio * 500  # 线性递减
        else:
            return 100 - (comment_ratio - 0.2) * 100  # 过多注释扣分
    
    def _count_lines(self, code: str) -> Dict:
        """统计代码行数"""
        lines = code.split('\n')
        
        total = len(lines)
        blank = sum(1 for l in lines if not l.strip())
        comment = sum(1 for l in lines if l.strip().startswith('//'))
        code_lines = total - blank - comment
        
        return {
            'total_lines': total,
            'code_lines': code_lines,
            'comment_lines': comment,
            'blank_lines': blank
        }
```

### 6.2 报告生成

```python
def format_quality_report(report: QualityReport) -> str:
    """格式化质量报告为 Markdown"""
    
    return f"""# 代码质量报告

## 总体评估

| 指标 | 得分 | 等级 |
|------|------|------|
| 综合质量分 | {report.overall_score:.1f}/100 | {report.grade} |
| 语法正确性 | {report.syntax_score:.1f}/100 | {'✅' if report.syntax_score >= 90 else '⚠️'} |
| 规范符合度 | {report.style_score:.1f}/100 | {'✅' if report.style_score >= 90 else '⚠️'} |
| 安全性 | {report.security_score:.1f}/100 | {'✅' if report.security_score >= 90 else '⚠️'} |
| 可维护性 | {report.maintainability_score:.1f}/100 | {'✅' if report.maintainability_score >= 90 else '⚠️'} |

## 代码统计

- 总行数: {report.total_lines}
- 代码行: {report.code_lines}
- 注释行: {report.comment_lines}
- 空行: {report.blank_lines}
- 注释率: {report.comment_lines / report.total_lines * 100:.1f}%

## 问题详情

### 语法错误 ({len(report.syntax_errors)})

{'| 行号 | 列号 | 错误类型 | 描述 |' if report.syntax_errors else '✅ 无语法错误'}
{'|' + '-'*6 + '|' + '-'*6 + '|' + '-'*10 + '|' + '-'*30 + '|' if report.syntax_errors else ''}
{chr(10).join(f'| {e.line} | {e.column} | {e.error_type} | {e.message} |' for e in report.syntax_errors)}

### 规范违规 ({len(report.style_violations)})

{'| 规则 | 行号 | 严重程度 | 描述 |' if report.style_violations else '✅ 无规范违规'}
{'|' + '-'*6 + '|' + '-'*6 + '|' + '-'*8 + '|' + '-'*30 + '|' if report.style_violations else ''}
{chr(10).join(f'| {v.rule_id} | {v.line} | {v.severity} | {v.message} |' for v in report.style_violations)}

### 安全问题 ({len(report.security_issues)})

{'| 严重度 | 类别 | 行号 | 描述 |' if report.security_issues else '✅ 无安全问题'}
{'|' + '-'*8 + '|' + '-'*15 + '|' + '-'*6 + '|' + '-'*30 + '|' if report.security_issues else ''}
{chr(10).join(f'| {i.severity} | {i.category} | {i.line} | {i.message} |' for i in report.security_issues)}

---

*报告生成时间: {datetime.now().isoformat()}*
"""
```

---

## 7. 集成到 Pipeline

```python
# skills/code_quality_checker/enhanced_checker.py
class EnhancedCodeQualityChecker(BaseSkill):
    """增强版代码质量检查器"""
    
    def __init__(self, config: Dict = None):
        super().__init__(config)
        self.aggregator = QualityAggregator()
        self.min_overall_score = config.get('min_score', 70)
    
    async def execute(self, context: SkillContext) -> Dict:
        generated_code = context.get_generated_code()
        
        reports = []
        failed_files = []
        
        for code_item in generated_code:
            # 生成质量报告
            report = self.aggregator.generate_report(
                code_item.content,
                code_item.language.value
            )
            
            reports.append(report)
            
            # 更新代码项的质量分
            code_item.quality_score = report.overall_score / 100
            code_item.syntax_valid = report.syntax_score >= 90
            
            # 检查是否通过阈值
            if report.overall_score < self.min_overall_score:
                failed_files.append({
                    'file': code_item.file_path,
                    'score': report.overall_score,
                    'grade': report.grade
                })
        
        # 生成汇总报告
        summary = {
            'total_files': len(generated_code),
            'passed_files': len(generated_code) - len(failed_files),
            'failed_files': len(failed_files),
            'avg_score': sum(r.overall_score for r in reports) / len(reports) if reports else 0,
            'reports': [format_quality_report(r) for r in reports]
        }
        
        return {
            'status': 'success' if not failed_files else 'warning',
            'data': summary
        }
```

---

*维护记录：*
- 2026-03-06: 创建初始版本
