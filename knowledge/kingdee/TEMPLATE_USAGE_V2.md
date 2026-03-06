# 金蝶云苍穹代码生成器 - 使用说明 V2.0

> **更新日期**: 2026-03-06  
> **基于真实项目**: 360度测评 + 合同续签模块

---

## 1. 模板体系

### 1.1 新增模板文件

| 模板文件 | 用途 | 输出位置 |
|---------|------|---------|
| `FormPlugin_Real.java.template` | 表单编辑插件 | `plugin/form/` |
| `DomainService_Real.java.template` | 领域服务 | `business/service/` |
| `EntityService_Real.java.template` | 实体服务 | `business/entityservice/` |
| `form_metadata_real.dym.template` | 表单元数据 | `datamodel/` |
| `AppConstant_Real.java.template` | 应用常量 | `common/` |

### 1.2 项目结构映射

```
fuyongcode/360度测评/
├── datamodel/
│   └── cds0_cea/
│       └── 1.5.0/
│           └── main/
│               └── cds0_cea/
│                   ├── metadata/           # ← .dym 元数据文件
│                   │   ├── cea_assessform.dym
│                   │   └── cea_assessactivity.dym
│                   └── dbschema/           # 数据库结构
│
└── code/opmc-cea/src/main/java/cds0/opmc/cea/
    ├── business/
    │   ├── service/                       # ← 领域服务
    │   │   ├── AssessActDimSettingDomainService.java
    │   │   └── AssessObjDomainService.java
    │   └── entityservice/                 # ← 实体服务
    │       ├── AssessActivityEntityService.java
    │       └── DimSettingEntityService.java
    ├── common/
    │   └── AppflgConstant.java            # ← 应用常量
    └── plugin/
        ├── form/                          # ← 表单插件
        │   ├── AssessActivityEdit.java
        │   └── AssessFormFixImportPlugin.java
        └── operate/                       # ← 操作插件
            └── ...
```

---

## 2. 如何使用新模板

### 步骤1：填写配置

复制 `examples/360_assess_example.yaml` 并根据你的需求修改：

```yaml
project:
  name: "你的项目名"
  module_prefix: "你的模块前缀"
  org_name: "你的组织名"
  app_name: "你的应用名"

entities:
  - name: "YourEntity"
    key: "your_entity_key"
    name_cn: "你的实体名称"
```

### 步骤2：运行生成

```bash
# 方式1: 通过配置文件生成
python main.py --config knowledge/kingdee/examples/your_config.yaml

# 方式2: 通过自然语言描述生成
python main.py --generate "创建一个请假申请单，包含申请人、请假类型、开始日期、结束日期字段"
```

### 步骤3：查看输出

生成的代码将保存在：
```
outputs/generated_code/{session_id}/
├── metadata/
│   └── your_entity.dym
└── src/
    └── YourEntityEdit.java
```

---

## 3. 模板可配置项

### 3.1 表单插件 (FormPlugin)

```yaml
form_plugins:
  - name: "AssessActivityEdit"
    
    # 控件常量定义
    control_constants:
      - name: "TOOLBAR"
        value: "toolbar"
    
    # 引用的服务
    services:
      - class_name: "AssessActivityEntityService"
        var_name: "ASSESS_ACTIVITY_ENTITY_SERVICE"
    
    # 工具栏按钮及对应方法
    toolbar_buttons:
      - const_name: "ADD"
        method_name: "showAddDialog"
    
    # 特性开关
    has_entry_grid: true              # 是否有分录
    implement_hyperlink: true         # 是否实现超链接
    
    # 字段变更规则
    field_change_rules:
      - field_name: "endDate"
        logic: |
          // 结束日期变化时计算天数
          calculateDays();
```

### 3.2 领域服务 (DomainService)

```yaml
domain_services:
  - name: "AssessActDimSettingDomainService"
    
    # 引用的实体服务
    entity_services:
      - class_name: "DimSettingEntityService"
        var_name: "DIM_SETTING_ENTITY_SERVICE"
    
    # 服务方法
    methods:
      - name: "deleteDimSetting"
        description: "删除维度设置"
        return_type: "void"
        params:
          - name: "dimsettingIds"
            type: "List<Long>"
        use_tx: true                    # 是否使用事务
        body: |
          // 删除逻辑
```

### 3.3 元数据 (.dym)

```yaml
entities:
  - name: "AssessActivity"
    key: "cea_assessactivity"
    name_cn: "测评活动"
    parent_form: "BasedataFormAp"    # 继承的父表单
    
    entry_entities:                  # 分录定义
      - name: "分组维度设置"
        key: "entryentity"
    
    fields:                          # 字段配置
      - oid: "scoresystem"
        must_input: "true"
```

---

## 4. 实际代码特征

### 4.1 包命名规范

```
cds0.opmc.cea
│   │    │
│   │    └── 应用名
│   └── 组织名
└── 模块前缀
```

### 4.2 关键API使用模式

```java
// 数据模型操作
DynamicObject data = getModel().getDataEntity();
DynamicObjectCollection entries = getModel().getEntryEntity("entryentity");

// 设置值
getModel().setValue("fieldName", value);

// 获取值
Object value = getModel().getValue("fieldName");

// 视图操作
this.getView().showMessage("提示信息");
this.getView().setEnable(false, "fieldName");
this.getView().setVisible(false, "toolbar");

// 服务调用
AssessActivityEntityService service = AssessActivityEntityService.getInstance();
DynamicObject result = service.queryByPk(pkId);

// 事务处理
TXHandle txHandle = TX.required();
try {
    // 业务逻辑
    txHandle.commit();
} catch (Exception e) {
    txHandle.rollback();
} finally {
    txHandle.close();
}
```

### 4.3 常量定义规范

```java
public interface AppflgConstant {
    String KEY_APP_NAME = "opmc-cea";
    
    // 表单ID常量
    String CEA_ASSESSACTIVITY = "cea_assessactivity";
    String CEA_DIMSETTING = "cea_dimsetting";
    
    // 操作键
    String OP_KEY_SAVE = "save";
    
    // 控件ID
    String TOOLBAR = "toolbar";
    String ENTRYENTITY = "entryentity";
}
```

---

## 5. 待填充的模板变量

### 5.1 需要你提供的信息

| 变量 | 说明 | 示例 |
|------|------|------|
| `module_prefix` | 模块前缀 | `cds0`, `tdkw` |
| `org_name` | 组织/公司简称 | `opmc`, `hlcm` |
| `app_name` | 应用名称 | `cea`, `renew` |
| `isv_code` | ISV开发商代码 | `tdkw` |
| `entity_name` | 实体英文名称 | `AssessActivity` |
| `entity_key` | 实体标识 | `cea_assessactivity` |

### 5.2 自动生成的变量

| 变量 | 说明 | 生成规则 |
|------|------|---------|
| `package_name` | 包名 | `cds0.opmc.cea.xxx` |
| `class_name` | 类名 | 大驼峰命名 |
| `entity_id` | 实体ID | 随机生成 |
| `master_id` | 主数据ID | 随机生成 |
| `modify_date` | 修改时间 | 当前时间戳 |

---

## 6. 快速开始

### 创建你的第一个单据

1. **创建配置文件** `my_bill.yaml`:

```yaml
project:
  name: "my-project"
  module_prefix: "my"
  org_name: "company"
  app_name: "hr"

entities:
  - name: "LeaveApply"
    key: "hr_leaveapply"
    name_cn: "请假申请单"
    parent_form: "BasedataFormAp"

java_code:
  form_plugins:
    - name: "LeaveApplyEdit"
      description: "请假申请编辑插件"
      control_constants:
        - name: "TOOLBAR"
          value: "toolbar"
```

2. **运行生成命令**:

```bash
python main.py --config my_bill.yaml
```

3. **查看生成的代码**:

```
outputs/generated_code/xxx/
├── metadata/hr_leaveapply.dym
└── src/LeaveApplyEdit.java
```

---

## 7. 高级自定义

### 7.1 自定义业务逻辑

在配置文件中直接编写业务逻辑代码：

```yaml
form_plugins:
  - name: "MyPlugin"
    after_bind_data_logic: |
      // 你的业务逻辑
      DynamicObject data = getModel().getDataEntity();
      if (data != null) {
          // 处理数据
      }
```

### 7.2 继承现有模板

```yaml
entities:
  - name: "MyBill"
    # 继承标准单据模板
    parent_form: "BasedataFormAp"
    
    # 或继承自定义模板
    parent_form: "cea_assessactivity"
```

---

## 8. 提交到代码仓

完成上述优化后，提交所有更改：

```bash
cd gencodetest
git add -A
git commit -m "feat: 优化代码生成模板，支持真实项目结构

- 新增表单插件模板（继承HRDataBaseEdit）
- 新增领域服务模板（事务支持）
- 新增实体服务模板（单例模式）
- 新增元数据模板（.dym格式）
- 新增应用常量模板
- 添加360度测评和合同续签项目示例
- 更新模板配置文件"
git push origin feature-standard
```
