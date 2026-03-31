package tdkw.esc.leaderquery.report.supervisor;

import com.google.common.collect.Sets;
import kd.bos.algo.*;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.entity.QueryEntityType;
import kd.bos.entity.report.*;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.util.CollectionUtils;
import kd.hr.hbp.business.servicehelper.HRQueryEntityHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.report.ReportOrgScopeUtils;
import tdkw.esc.leaderquery.report.supervisor.SupRepository;

import java.util.*;

/**
 * RightTablePlugin
 * 董监事  已合并
 *
 * @author xxx
 * @date 2023/6/17
 */
public class RightTablePlugin extends AbstractReportListDataPlugin {

    /**
     * 查询字段和列名
     *
     * @return
     */
    private static Map<String, String> createMap() {
        HashMap<String, String> map = new LinkedHashMap<>();
        map.put("tdkw_company.name", "公司");
        map.put("person.name", "姓名");
        map.put("tdkw_poststype.name", "董监事");
        map.put("tdkw_startdate", "任期开始时间");
        map.put("tdkw_enddate", "任期结束时间");
        map.put("hspm_ermanfile.pernontsprop.gender.name", "性别");
        map.put("hrpi_pernontsprop.tdkw_origin.tdkw_origin", "籍贯");
        map.put("hrpi_pertsprop.marriagestatus.name", "婚姻状况");
        map.put("hrpi_pereduexp.education.name", "学历");
        map.put("hrpi_perregion.politicalstatus.name", "政治面貌");
        map.put("hrpi_pernontsprop.birthday", "出生日期");
        map.put("hspm_ermanfile.pernontsprop.age", "年龄");
        map.put("hspm_ermanfile.pernontsprop.servicelen", "工龄");
        map.put("hspm_ermanfile.pernontsprop.entservicelen", "集团司龄");
        map.put("hspm_ermanfile.empposrel.company.name", "所属公司-控权用");
        map.put("hspm_ermanfile.boid", "人事业务档案主ID");
        return map;
    }

    /**
     * 创建右表列
     *
     * @param columns
     * @return
     * @throws Throwable
     */
    @Override
    public List<AbstractReportColumn> getColumns(List<AbstractReportColumn> columns) throws Throwable {
        Map<String, String> map = createMap();
        /**
         * 三种创建列方法
         * createReportColumn 创建普通列
         * createReportColumnHyperlink  创建带有超链接的列
         * createReportColumnHide   创建隐藏列
         */
        //隐藏人事业务档案id
        //columns.add(createReportColumnHide("boid", ReportColumn.TYPE_TEXT, "人事业务档案id"));
        for (String key : map.keySet()) {
            String value = map.get(key);
            if ("姓名".equals(value)) {
                columns.add(createReportColumnHyperlink(key, ReportColumn.TYPE_TEXT, value));
                continue;
            }
            if ("人事业务档案主ID".equals(value)||"所属公司-控权用".equals(value)) {
                columns.add(createReportColumnHide(key, ReportColumn.TYPE_TEXT, value));
                continue;
            }
            if ("出生日期".equals(value) || "任期开始时间".equals(value) || "任期结束时间".equals(value)) {
                columns.add(createReportColumn(key, ReportColumn.TYPE_DATE, value));
                continue;
            }
            if ("工龄".equals(value) || "集团司龄".equals(value)) {
                ReportColumn reportColumn = createReportColumn(key, ReportColumn.TYPE_DECIMAL, value);
                reportColumn.setScale(1);
                reportColumn.setZeroShow(true);
                columns.add(reportColumn);
                continue;
            }
            columns.add(createReportColumn(key, ReportColumn.TYPE_TEXT, value));
        }
        return super.getColumns(columns);
    }


    /**
     * 读取查询配置 LocalRepository为自己自己封装的读取查询配置的工具类 需要去改标识 一个查询配置对应一个Repository 相同的读取可复用
     */
    private final QueryEntityType queryType = (QueryEntityType) SupRepository.generate().getDataEntityType();


    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        //kd.hr.hspm.formplugin.web.report.EmpReportListPlugin#query 这个是人员花名册报表的方法
        // 里面的 HRQueryEntityHelper.getInstance().getQueryDataSet 是调用查询配置的sql
        // ErmanFileF7TreeListPlugin
        Map<String, String> map = createMap();
        IReportBatchQueryInfo byBatchInfo = reportQueryParam.byBatchInfo();
        List<Row> currentBatchRows = byBatchInfo.getCurrentBatchRows();
        Set<Long> matIdsOfCurrentBatch = new HashSet();
        DataSet queryDataSet;
        try {
            FilterInfo filterInfo = reportQueryParam.getFilter();
            this.reportPlanId = filterInfo.getLong("report");
            Set<String> queryFieldSet = new HashSet<>();
            // queryFieldSet.add("hrpi_pernontsprop.age");
            queryFieldSet.add("name");
            if (CollectionUtils.isEmpty(queryFieldSet)) {
                Object var11 = null;
                return (DataSet) var11;
            }

            matIdsOfCurrentBatch = this.getQueryIdList(currentBatchRows);
            // 过滤条件构建
            QFilter filter = new QFilter("id", QCP.in, matIdsOfCurrentBatch);
            filter.and("tdkw_company", QCP.not_equals,null);
            //todo 权限控制
            QFilter orgFilter = ReportOrgScopeUtils.getOrgFilter("tdkw_statisticentry", "hspm_ermanfile.empposrel.company");
            filter.and(orgFilter);
            // 左树过滤条件
            if (null != reportQueryParam.getCustomParam().get("orgIds")) {
                //左树点击组织时对右表进行过滤
                filter = filter.and(new QFilter("tdkw_company", QCP.in, reportQueryParam.getCustomParam().get("orgIds")));
                // filter = filter.and(new QFilter("org.id", QCP.equals, reportQueryParam.getCustomParam().get("orgId").toString()));
            }
            //表头过滤条件
            //岗位层级查询控件 岗位层级 tdkw_postlevel  岗位 tdkw_position 姓名 tdkw_name 年龄 tdkw_agestart tdkw_ageend
            List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
            for (FilterItemInfo filterItem : filterItems) {
                String propName = filterItem.getPropName();
                Object value = filterItem.getValue();
                switch (propName) {
                    //董监事
                    case "tdkw_poststype":
                        if (ObjectUtils.isNotEmpty(value)) {
                            DynamicObject party = (DynamicObject) value;
                            OrmLocaleValue partyName = (OrmLocaleValue) party.get("name");
                            String localeValueZhCn = partyName.getLocaleValue_zh_CN();
                            filter.and("tdkw_poststype.name", QCP.equals, localeValueZhCn);
                        }
                        break;
                    //岗位
                    case "tdkw_position":
                        if (StringUtils.isNotBlank((String) value)) {
                            filter.and("hrpi_empposorgrel.position.name", QCP.like, "%" + value + "%");
                        }
                        break;
                    //姓名
                    case "tdkw_name":
                        if (StringUtils.isNotBlank((String) value)) {
                            filter.and("person.name", QCP.like, "%" + value + "%");
                        }
                        break;
                    //起始年龄
                    case "tdkw_agestart":
                        if ((Integer) value != 0) {
                            filter.and("hrpi_pernontsprop.age", QCP.large_equals, value);
                        }
                        break;
                    //终止年龄
                    case "tdkw_ageend":
                        if ((Integer) value != 0) {
                            filter.and("hrpi_pernontsprop.age", QCP.less_equals, value);
                        }
                        break;
                }
            }

            /**
             * 查询右表值
             * this.queryType为读取查询配置
             * 参数二为需要查询的字段 标识为查询配置中的查询字段的标识
             * filter.toArray() 过滤条件 包括左树 表头 还可以自定义加入
             * orderBys:排序条件
             *
             */
            StringBuilder queryField = new StringBuilder();
            for (String s : map.keySet()) {
                if (queryField.length() > 0) {
                    queryField.append(",");
                }
                queryField.append(s);
            }

            queryDataSet = HRQueryEntityHelper.getInstance().getQueryDataSet(this.queryType, queryField.toString(), filter.toArray(), "createtime desc", null);

        } finally {
            //  LOGGER.info(MessageFormat.format("EmpReportListPlugin.query---end query by batch,current size={0},executeTime={1} ms.", ((Set)matIdsOfCurrentBatch).size(), System.currentTimeMillis() - startTime));
        }
        return queryDataSet;
    }


    /**
     * 创建普通右表列
     *
     * @param fieldKey  查询字段标识 与query方法一致 与查询配置中查询字段标识一致
     * @param fieldType 创建列的类型
     * @param caption   列的显示名称
     * @return 列
     */
    public ReportColumn createReportColumn(String fieldKey, String fieldType, String caption) {
        ReportColumn column = new ReportColumn();
        column.setFieldKey(fieldKey);
        column.setFieldType(fieldType);
        column.setCaption(new LocaleString(caption));
        return column;
    }

    /**
     * 创建隐藏右表列
     * 应用场景 把点击超链接需要用到的字段值查出来放在这里 便于后续使用
     *
     * @param fieldKey  查询字段标识 与query方法一致 与查询配置中查询字段标识一致
     * @param fieldType 创建列的类型
     * @param caption   列的显示名称
     * @return 列
     */
    public ReportColumn createReportColumnHide(String fieldKey, String fieldType, String caption) {
        ReportColumn column = new ReportColumn();
        column.setFieldKey(fieldKey);
        column.setFieldType(fieldType);
        //设置列隐藏
        column.setHide(true);
        column.setCaption(new LocaleString(caption));
        return column;
    }

    /**
     * 创建超链接右表列
     * 点击可以打开窗口
     *
     * @param fieldKey  查询字段标识 与query方法一致 与查询配置中查询字段标识一致
     * @param fieldType 创建列的类型
     * @param caption   列的显示名称
     * @return 列
     */
    public ReportColumn createReportColumnHyperlink(String fieldKey, String fieldType, String caption) {
        ReportColumn column = new ReportColumn();
        column.setFieldKey(fieldKey);
        column.setFieldType(fieldType);
        //设置为超链接列
        column.setHyperlink(true);
        column.setCaption(new LocaleString(caption));
        return column;
    }


    private static final Map<String, List<String>> CALCULATE_COLUMN_MAP = new HashMap(16);

    /**
     * hrpi_pernontsprop.age 是查询列表的字段标识 不清楚为什么标品单独列出来
     */
    static {
        CALCULATE_COLUMN_MAP.put("hrpi_pernontsprop.age", Collections.singletonList("hrpi_pernontsprop.birthday"));
    }


    private Long reportPlanId;

    @Override
    public DataSet queryBatchBy(ReportQueryParam queryParam) {
        IReportBatchQueryInfo byBatchInfo = queryParam.byBatchInfo();
        byBatchInfo.setCountPerBatch(BATCH_COUNT);
        QFilter filter = new QFilter("iscurrentversion", "=", "1");
        QFilter[] filters = new QFilter[]{filter};
        List<Object> ids = HRQueryEntityHelper.getInstance().queryAllPkByKSql(this.queryType, filters, null);
        DataSet dataSet = buildBatchDataSet(ids);
        byBatchInfo.setMaxRowCountCached(12);
        return dataSet;
    }

    private DataSet buildBatchDataSet(List<Object> ids) {
        RowMeta rowMeta = new RowMeta(new Field[]{new Field("id", DataType.LongType)});
        DataSetBuilder dataSetBuilder = Algo.create(this.getClass().getName()).createDataSetBuilder(rowMeta);
        Iterator var4 = ids.iterator();

        while (var4.hasNext()) {
            Object id = var4.next();
            dataSetBuilder.append(new Object[]{id});
        }

        return dataSetBuilder.build();
    }


    private static final Integer BATCH_COUNT = 1000;

    public Set<Long> getQueryIdList(List<Row> currentBatchRows) {
        Set<Long> matIdsOfCurrentBatch = Sets.newHashSetWithExpectedSize(BATCH_COUNT);
        Iterator var3 = currentBatchRows.iterator();

        while (var3.hasNext()) {
            Row currentBatchRow = (Row) var3.next();
            Long matId = currentBatchRow.getLong(0);
            matIdsOfCurrentBatch.add(matId);
        }

        return matIdsOfCurrentBatch;
    }

}
