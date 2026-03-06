# SQL报表开发规范

## 1. 基本规范

### 1.1 命名规范
- 表别名：使用有意义的缩写，如 `emp` (employee), `dept` (department)
- 字段别名：使用驼峰命名，如 `employeeName`, `departmentCode`
- 临时表：以 `tmp_` 前缀，如 `tmp_attendance_summary`

### 1.2 注释规范
```sql
-- =============================================
-- 报表名称: 月度考勤汇总表
-- 功能描述: 统计员工月度出勤、请假、加班情况
-- 创建日期: 2024-03-10
-- 作者: 开发团队
-- 修改历史:
--   2024-03-15 增加加班时长统计
-- =============================================
```

## 2. 查询优化

### 2.1 索引使用
```sql
-- 好的做法：使用索引字段作为查询条件
SELECT emp.employee_id, emp.employee_name
FROM hr_employee emp
WHERE emp.department_id = :deptId        -- 有索引
  AND emp.status = 'ACTIVE'              -- 有索引
  AND emp.join_date >= :startDate;       -- 有索引

-- 避免：对索引字段使用函数
-- WHERE DATE(emp.create_time) = '2024-03-10'  -- 不好
WHERE emp.create_time >= '2024-03-10' 
  AND emp.create_time < '2024-03-11'      -- 好
```

### 2.2 分页查询
```sql
-- 使用ROWNUM进行分页（Oracle）
SELECT * FROM (
    SELECT t.*, ROWNUM rn FROM (
        SELECT emp.employee_id, emp.employee_name, 
               att.check_in_time, att.check_out_time
        FROM hr_employee emp
        LEFT JOIN hr_attendance att ON emp.employee_id = att.employee_id
        WHERE emp.department_id = :deptId
        ORDER BY att.check_in_time DESC
    ) t WHERE ROWNUM <= :endRow
) WHERE rn > :startRow;

-- 使用OFFSET FETCH（SQL Server 2012+）
SELECT emp.employee_id, emp.employee_name
FROM hr_employee emp
WHERE emp.department_id = :deptId
ORDER BY emp.employee_id
OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY;
```

## 3. 复杂报表示例

### 3.1 考勤月度汇总
```sql
WITH attendance_daily AS (
    -- 每日考勤数据
    SELECT 
        employee_id,
        TRUNC(check_in_date) as work_date,
        MIN(check_in_time) as first_check_in,
        MAX(check_out_time) as last_check_out,
        CASE 
            WHEN MIN(check_in_time) > '09:00' THEN '迟到'
            WHEN MAX(check_out_time) < '18:00' THEN '早退'
            ELSE '正常'
        END as attendance_status
    FROM hr_attendance_record
    WHERE check_in_date >= :startDate
      AND check_in_date < :endDate
    GROUP BY employee_id, TRUNC(check_in_date)
),
leave_summary AS (
    -- 请假汇总
    SELECT 
        employee_id,
        leave_type,
        SUM(leave_days) as total_days
    FROM hr_leave_record
    WHERE start_date >= :startDate
      AND end_date < :endDate
      AND status = 'APPROVED'
    GROUP BY employee_id, leave_type
),
overtime_summary AS (
    -- 加班汇总
    SELECT 
        employee_id,
        SUM(overtime_hours) as total_hours
    FROM hr_overtime_record
    WHERE overtime_date >= :startDate
      AND overtime_date < :endDate
      AND status = 'APPROVED'
    GROUP BY employee_id
)
-- 最终汇总
SELECT 
    emp.employee_id,
    emp.employee_name,
    dept.department_name,
    COUNT(DISTINCT ad.work_date) as work_days,
    SUM(CASE WHEN ad.attendance_status = '迟到' THEN 1 ELSE 0 END) as late_count,
    SUM(CASE WHEN ad.attendance_status = '早退' THEN 1 ELSE 0 END) as early_leave_count,
    NVL(ls_total.total_days, 0) as leave_days,
    NVL(ot.total_hours, 0) as overtime_hours
FROM hr_employee emp
JOIN hr_department dept ON emp.department_id = dept.department_id
LEFT JOIN attendance_daily ad ON emp.employee_id = ad.employee_id
LEFT JOIN (
    SELECT employee_id, SUM(total_days) as total_days
    FROM leave_summary
    GROUP BY employee_id
) ls_total ON emp.employee_id = ls_total.employee_id
LEFT JOIN overtime_summary ot ON emp.employee_id = ot.employee_id
WHERE emp.status = 'ACTIVE'
GROUP BY emp.employee_id, emp.employee_name, dept.department_name
ORDER BY dept.department_name, emp.employee_id;
```

## 4. 性能监控

### 4.1 执行计划分析
```sql
-- Oracle
EXPLAIN PLAN FOR
SELECT * FROM hr_employee WHERE department_id = 100;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- SQL Server
SET SHOWPLAN_ALL ON;
GO
SELECT * FROM hr_employee WHERE department_id = 100;
GO
SET SHOWPLAN_ALL OFF;
```

### 4.2 慢查询优化
1. 识别慢查询（执行时间>1秒）
2. 分析执行计划，检查是否走索引
3. 优化SQL写法，减少全表扫描
4. 考虑增加复合索引
5. 对大表进行分区
