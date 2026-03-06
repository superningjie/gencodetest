# Kotlin 插件开发规范

## 1. 插件基础结构

```kotlin
package com.kingdee.hr.plugin

import com.kingdee.hr.plugin.sdk.AbstractPlugin
import com.kingdee.hr.plugin.sdk.IPlugin
import com.kingdee.hr.plugin.sdk.annotation.ExtensionPoint

class AttendancePlugin : AbstractPlugin(), IPlugin {

    override fun onEnable() {
        // 注册扩展点实现
        registerExtension("check_in_validator", CustomCheckInValidator())
        registerExtension("leave_approval_flow", CustomLeaveApprover())
    }

    override fun onDisable() {
        // 清理资源
    }
}
```

## 2. 扩展点实现

### 2.1 打卡校验扩展
```kotlin
class CustomCheckInValidator : ICheckInValidator {

    override fun validate(dto: CheckInDTO): ValidationResult {
        // 1. 校验GPS位置
        if (!isValidLocation(dto.latitude, dto.longitude)) {
            return ValidationResult.fail("INVALID_LOCATION", "不在有效打卡范围内")
        }

        // 2. 校验WiFi
        if (!isValidWiFi(dto.wifiMac)) {
            return ValidationResult.fail("INVALID_WIFI", "未连接到公司WiFi")
        }

        // 3. 校验时间
        if (!isValidTime(dto.checkInTime)) {
            return ValidationResult.fail("INVALID_TIME", "不在允许的打卡时间段")
        }

        return ValidationResult.success()
    }

    private fun isValidLocation(lat: Double, lng: Double): Boolean {
        // 实现位置校验逻辑
        return true
    }

    private fun isValidWiFi(mac: String): Boolean {
        // 实现WiFi校验逻辑
        return true
    }

    private fun isValidTime(time: LocalDateTime): Boolean {
        // 实现时间校验逻辑
        return true
    }
}
```

### 2.2 请假审批扩展
```kotlin
class CustomLeaveApprover : ILeaveApprover {

    override fun approve(leaveRequest: LeaveRequest): ApprovalResult {
        // 1. 检查假期余额
        val balance = getLeaveBalance(leaveRequest.employeeId, leaveRequest.leaveType)
        if (balance < leaveRequest.days) {
            return ApprovalResult.reject("INSUFFICIENT_BALANCE", "假期余额不足")
        }

        // 2. 检查审批权限
        val approver = getCurrentApprover()
        if (!hasApprovalAuthority(approver, leaveRequest)) {
            return ApprovalResult.reject("NO_AUTHORITY", "无审批权限")
        }

        // 3. 执行审批
        return ApprovalResult.approve(approver.id)
    }
}
```

## 3. 插件配置

```yaml
# plugin.yaml
plugin:
  id: "com.kingdee.hr.attendance.custom"
  name: "考勤自定义插件"
  version: "1.0.0"
  author: "开发团队"

  extensions:
    - pointId: "check_in_validator"
      implementation: "com.kingdee.hr.plugin.CustomCheckInValidator"
      priority: 100

    - pointId: "leave_approval_flow"
      implementation: "com.kingdee.hr.plugin.CustomLeaveApprover"
      priority: 50

  dependencies:
    - pluginId: "com.kingdee.hr.core"
      version: ">=5.0.0"
```

## 4. 最佳实践

1. **异常处理**: 使用try-catch捕获异常，转换为插件标准异常
2. **日志记录**: 使用插件提供的日志工具，统一格式
3. **性能优化**: 避免在扩展点中执行耗时操作，必要时使用异步处理
4. **配置外置**: 将可变配置提取到plugin.yaml，避免硬编码
5. **版本兼容**: 声明依赖版本范围，确保兼容性
