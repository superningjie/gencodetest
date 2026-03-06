-- =============================================
-- 报表名称: {{reportName}}
-- 功能描述: {{description}}
-- 生成时间: {{generateTime}}
-- =============================================

WITH temp_data AS (
    -- 临时数据预处理
    SELECT 
        {{selectFields}}
    FROM {{fromTables}}
    WHERE 1=1
    {{#whereConditions}}
      AND {{condition}}
    {{/whereConditions}}
)

SELECT 
    {{selectFields}}
FROM temp_data
{{#groupByFields}}
GROUP BY {{groupByFields}}
{{/groupByFields}}
ORDER BY 1;
