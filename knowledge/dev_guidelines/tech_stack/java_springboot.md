# Java SpringBoot 开发规范

## 1. 项目结构

```
src/main/java/com/kingdee/hr/
├── controller/          # 控制层
├── service/             # 业务层
│   ├── impl/           # 实现类
├── repository/          # 数据访问层
├── entity/              # 实体类
├── dto/                 # 数据传输对象
├── vo/                  # 视图对象
├── config/              # 配置类
├── exception/           # 异常处理
├── util/                # 工具类
└── plugin/              # 插件接口
```

## 2. 命名规范

### 2.1 类命名
- Controller: `XxxController`
- Service: `XxxService` / `XxxServiceImpl`
- Repository: `XxxRepository`
- Entity: `XxxEntity`
- DTO: `XxxDTO`
- VO: `XxxVO`

### 2.2 方法命名
- 查询: `getXxx`, `findXxx`, `queryXxx`
- 新增: `createXxx`, `addXxx`, `saveXxx`
- 更新: `updateXxx`, `modifyXxx`
- 删除: `deleteXxx`, `removeXxx`
- 校验: `validateXxx`, `checkXxx`

## 3. 代码规范

### 3.1 控制层
```java
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/records")
    public Result<List<AttendanceVO>> getAttendanceRecords(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(attendanceService.getRecords(startDate, endDate));
    }

    @PostMapping("/check-in")
    public Result<Void> checkIn(@RequestBody @Valid CheckInDTO checkInDTO) {
        attendanceService.checkIn(checkInDTO);
        return Result.success();
    }
}
```

### 3.2 业务层
```java
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PluginManager pluginManager;

    @Override
    @Transactional
    public void checkIn(CheckInDTO dto) {
        // 1. 参数校验
        validateCheckInParams(dto);

        // 2. 调用插件扩展点
        ICheckInValidator validator = pluginManager.getExtension("check_in_validator", ICheckInValidator.class);
        if (validator != null) {
            validator.validate(dto);
        }

        // 3. 业务逻辑
        AttendanceEntity entity = convertToEntity(dto);
        attendanceRepository.save(entity);

        // 4. 发布事件
        eventPublisher.publishEvent(new CheckInEvent(entity));
    }
}
```

### 3.3 数据访问层
```java
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    @Query("SELECT a FROM AttendanceEntity a WHERE a.employeeId = :employeeId AND a.checkInDate BETWEEN :startDate AND :endDate")
    List<AttendanceEntity> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
```

## 4. 异常处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
```

## 5. 接口设计规范

### 5.1 RESTful API
- GET: 查询资源
- POST: 创建资源
- PUT: 更新资源（全量）
- PATCH: 更新资源（部分）
- DELETE: 删除资源

### 5.2 统一返回格式
```json
{
    "code": "SUCCESS",
    "message": "操作成功",
    "data": {},
    "timestamp": 1710000000000
}
```
