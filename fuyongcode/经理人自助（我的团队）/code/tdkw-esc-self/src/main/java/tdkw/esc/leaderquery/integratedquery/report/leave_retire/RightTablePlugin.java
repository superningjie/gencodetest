package tdkw.esc.leaderquery.integratedquery.report.leave_retire;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import kd.bos.algo.*;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.QueryEntityType;
import kd.bos.entity.report.*;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.entity.EntityMetadata;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.util.CollectionUtils;
import kd.hr.hbp.business.servicehelper.HRQueryEntityHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.integratedquery.report.ReportOrgScopeUtils;
import tdkw.esc.leaderquery.integratedquery.report.leave_retire.LeaRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RightTablePlugin
 * 经营班子
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
        map.put("hrpi_empposorgrel.company.name", "公司");
        map.put("name", "姓名");
        map.put("hrpi_empposorgrel.position.name", "岗位");
        map.put("hrpi_empposorgrel.position.tdkw_positionlevel1", "岗位层级");
        map.put("hrpi_empposorgrel.tdkw_postlevel", "岗位级别");
        map.put("hrpi_pernontsprop.gender.name", "性别");
        map.put("hrpi_pertsprop.marriagestatus.name", "婚姻状况");
        map.put("hrpi_pereduexp.education.name", "学历");
        map.put("hrpi_perregion.politicalstatus.name", "政治面貌");
        map.put("hrpi_pernontsprop.age", "年龄");
        map.put("hrpi_perserlen.socialworkage", "社会工龄");
        map.put("hrpi_perserlen.comsercount", "集团司龄");
        map.put("hrpi_empentrel.laborreltype.name", "人员类型");
        map.put("hrpi_empposorgrel.tdkw_changereason.name", "离职/退休原因");
        map.put("hrpi_empposorgrel.lastworkdate", "离职/退休日期");


        //  map.put("hrpi_empposorgrel.lastworkdate", "离职日期");
        map.put("hrpi_empposorgrel.tdkw_ranks.name", "职务");
        map.put("hrpi_perserlen.tdkw_serviceyear", "公司司龄");
        map.put("hrpi_empposorgrel.tdkw_engagedindustry.name", "从事行业");
        map.put("hrpi_empposorgrel.tdkw_jobsequence.name", "岗位序列");
        map.put("hrpi_familymemb.workunit", "家属工作单位");
        map.put("hrpi_pereduexp.graduateschool.name", "毕业院校");
        map.put("hrpi_pernontsprop.tdkw_contrworkloc.name", "工作地点");
        // map.put("htm_quitapply.quittype.name", "离职/退休类型");
        map.put("boid", "人事业务档案主ID");
        //查院校类别过滤
        map.put("hrpi_pereduexp.graduateschool.id", "毕业院校id");
        map.put("collegecharact.fbasedataid", "院校类别id");
        map.put("collegecharact.fbasedataid.name", "院校类别名称");
        map.put("id", "sqlid");


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
        // columns.add(createReportColumnHide("boid", ReportColumn.TYPE_TEXT, "人事业务档案id"));
        for (String key : map.keySet()) {
            String value = map.get(key);
            if ("姓名".equals(value)) {
                //超链接列
                columns.add(createReportColumnHyperlink(key, ReportColumn.TYPE_TEXT, value));
                continue;
            }
            if ("人事业务档案主ID".equals(value) || "岗位级别".equals(value) || "职务".equals(value) || "公司司龄".equals(value) || "毕业院校id".equals(value) || "从事行业".equals(value) || "岗位序列".equals(value) || "家属工作单位".equals(value) || "毕业院校".equals(value) || "工作地点".equals(value) || "院校类别id".equals(value) || "院校类别名称".equals(value)|| "sqlid".equals(value)) {
                //隐藏列
                columns.add(createReportColumnHide(key, ReportColumn.TYPE_TEXT, value));
                continue;
            }
            if ("离职/退休日期".equals(value)) {
                //时间格式化列
                columns.add(createReportColumn(key, ReportColumn.TYPE_DATE, value));
                continue;
            }
            if ("社会工龄".equals(value) || "集团司龄".equals(value)) {
                //数字格式化列
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

    @Override
    public DataSet queryBatchBy(ReportQueryParam queryParam) {
        Map<String, String> fieldMap = createMap();
        StringBuilder queryField = new StringBuilder();
        for (String s : fieldMap.keySet()) {
            if ("hrpi_empposorgrel.position.tdkw_positionlevel1".equals(s) || "collegecharact.fbasedataid".equals(s) || "collegecharact.fbasedataid.name".equals(s)) {
                continue;
            }
            if (queryField.length() > 0) {
                queryField.append(",");
            }
            queryField.append(s);
        }

        IReportBatchQueryInfo byBatchInfo = queryParam.byBatchInfo();
        byBatchInfo.setCountPerBatch(BATCH_COUNT);


        // QFilter filter = new QFilter("iscurrentversion", "=", "1");
        // QFilter[] filters = new QFilter[]{filter};

        Map<String, Object> map = getAllFilters(queryParam);
        QFilter[] filters = new QFilter[]{(QFilter) map.get("filter")};


        DataSet queryDataSet = HRQueryEntityHelper.getInstance().getQueryDataSet(this.queryType, queryField.toString(), filters, null);

        DataSet dataSetBatch;
        dataSetBatch = getSchoolDataSet(queryDataSet, (Boolean) map.get("schoolTypeFlag"), (QFilter) map.get("schoolTypeFilter"), queryField);
        byBatchInfo.setMaxRowCountCached(12);
        List<Object> ids = ORM.create().toPlainDynamicObjectCollection(dataSetBatch).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
        DataSet dataSet = buildBatchDataSet(ids);
        return dataSet;
    }

    private DataSet getSchoolDataSet(DataSet queryDataSet, Boolean schoolTypeFlag, QFilter schoolTypeFilter, StringBuilder queryField) {
        DataSet queryDataSetjob;
        //todo 查院校类别
        // "hbss_college";
        String algoKey = this.getClass().getName();
        QFilter schoolQfilter = new QFilter("enable", QCP.equals, "1");
        schoolQfilter.and("iscurrentversion", QCP.equals,"1");
        schoolQfilter.and("datastatus", QCP.equals,"1");
        //查高等院校 id,院校类型id,院校类型名称
        DataSet schoolDataSet = ORM.create().queryDataSet(algoKey, "hbss_college", "id,collegecharact.fbasedataid,collegecharact.fbasedataid.name", new QFilter[]{schoolTypeFilter, schoolQfilter});
        // String[] reportFieldString =  new String[2];
        // reportFieldString[0]="id";
        // reportFieldString[1]="collegecharact.fbasedataid.id";
        String[] redundancyFieldString = new String[2];
        redundancyFieldString[0] = "collegecharact.fbasedataid";
        redundancyFieldString[1] = "collegecharact.fbasedataid.name";
        queryDataSetjob = queryDataSet.leftJoin(schoolDataSet).on("hrpi_pereduexp.graduateschool.id", "id").select(queryField.toString().split(","), redundancyFieldString).finish();
        if (schoolTypeFlag) {
            queryDataSetjob = queryDataSetjob.where("collegecharact.fbasedataid is not null");
        }
        return queryDataSetjob;
    }

    private Map<String, Object> getAllFilters(ReportQueryParam queryParam) {
        QFilter schoolTypeFilter = new QFilter("1", QCP.equals, 1);
        boolean schoolTypeFlag = false;
        QFilter filter = new QFilter("1", QCP.equals, 1);
        //取值范围：主表：人事业务档案；人员类型字段（人事业务档案-员工任职id-任职经历-异动类型-名称）=主动离职/被动离职/特殊内退 或者 异动原因（人事业务档案-员工任职id-任职经历-异动原因-名称）=退休
        QFilter leaveFilter = new QFilter("hrpi_empposorgrel.tdkw_changetype.number", QCP.in, Lists.newArrayList("XY00006", "XY00015", "XY00016"));
        QFilter retireFilter = new QFilter("hrpi_empposorgrel.tdkw_changereason.number", QCP.in, Lists.newArrayList("XY00050"));
        filter.and(leaveFilter.or(retireFilter));
        filter.and("hrpi_empposorgrel.company", QCP.not_equals, null);
        //todo 权限控制
        QFilter orgFilter = ReportOrgScopeUtils.getOrgFilter("tdkw_departandretire_pc", "hrpi_empposorgrel.adminorg");
        filter.and(orgFilter);
        //左树过滤条件
        if (null != queryParam.getCustomParam().get("orgIds")) {
            List orgIds = (List) queryParam.getCustomParam().get("orgIds");
            if (!orgIds.contains(100000L)) {
                //左树点击组织时对右表进行过滤
                QFilter leftTreeFilter = new QFilter("hrpi_empposorgrel.company", QCP.in, queryParam.getCustomParam().get("orgIds")).and("hrpi_empposorgrel.adminorg", QCP.in, queryParam.getCustomParam().get("orgIds"));
                filter = filter.and(leftTreeFilter);
            }
        }
        //表头过滤条件
        //岗位层级查询控件 岗位层级 tdkw_postlevel  岗位 tdkw_position 姓名 tdkw_name 年龄 tdkw_agestart tdkw_ageend
        List<FilterItemInfo> filterItems = queryParam.getFilter().getFilterItems();
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            switch (propName) {
                // 所属组织
                case "tdkw_curorg":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject curOrg = (DynamicObject) value;
                        filter.and("hrpi_empposorgrel.company", QCP.equals, curOrg.getLong("id"));
                    }
                    break;
                //岗位层级
                case "tdkw_positionlevel":
                    if (StringUtils.isNotBlank((String) value)) {
                        filter.and("hrpi_empposorgrel.tdkw_postlevel", QCP.equals, value);
                    }
                    break;
                //姓名
                case "tdkw_name":
                    if (StringUtils.isNotBlank((String) value)) {
                        filter.and("name", QCP.like, "%" + value + "%");
                    }
                    break;
                // 人员类别
                case "tdkw_persontype":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObjectCollection personType = (DynamicObjectCollection) value;
                        List<Long> ids = personType.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        filter.and("hrpi_empentrel.laborreltype", QCP.in, ids);
                    }
                    break;
                //离职/退休原因
                case "tdkw_reason":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObjectCollection reason = (DynamicObjectCollection) value;
                        List<Long> number = reason.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        filter.and("hrpi_empposorgrel.tdkw_changereason", QCP.in, number);
                    }
                    break;
                //离职/退休日期 开始
                case "tdkw_leave_date":
                    if (null != value) {
                        filter.and("hrpi_empposorgrel.lastworkdate", QCP.large_equals, value);
                    }
                    break;
                //离职/退休日期 开始
                case "tdkw_leave_datend":
                    if (null != value) {
                        filter.and("hrpi_empposorgrel.lastworkdate", QCP.less_equals, value);
                    }
                    break;
                // 学历
                case "tdkw_degree":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject degree = (DynamicObject) value;
                        filter.and("hrpi_pereduexp.education", QCP.equals, degree.getLong("id"));
                    }
                    break;
                //婚育
                case "tdkw_marriage":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject marriage = (DynamicObject) value;
                        filter.and("hrpi_pertsprop.marriagestatus", QCP.equals, marriage.getLong("id"));
                    }
                    break;
                //起始社会工龄
                case "tdkw_social_workage":
                    if ((Integer) value != 0) {
                        filter.and("hrpi_perserlen.socialworkage", QCP.large_equals, value);
                    }
                    break;
                //终止社会工龄
                case "tdkw_social_workagen":
                    if ((Integer) value != 0) {
                        filter.and("hrpi_perserlen.socialworkage", QCP.less_equals, value);
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
                //起始集团司龄
                case "tdkw_comagestart":
                    if ((Integer) value != 0) {
                        filter.and("hrpi_perserlen.comsercount", QCP.large_equals, value);
                    }
                    break;
                // 终止集团司龄
                case "tdkw_comageend":
                    if ((Integer) value != 0) {
                        filter.and("hrpi_perserlen.comsercount", QCP.less_equals, value);
                    }
                    break;
                //起始公司司龄
                case "tdkw_company_agestart":
                    if ((Integer) value != 0) {
                        filter.and("hrpi_perserlen.tdkw_serviceyear", QCP.large_equals, value);
                    }
                    break;
                //终止公司司龄
                case "tdkw_company_ageend":
                    if ((Integer) value != 0) {
                        filter.and("hrpi_perserlen.tdkw_serviceyear", QCP.less_equals, value);
                    }
                    break;
                //  从事行业
                case "tdkw_currjob":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject curJob = (DynamicObject) value;
                        filter.and("hrpi_empposorgrel.tdkw_engagedindustry", QCP.equals, curJob.getLong("id"));
                    }
                    break;
                //性别
                case "tdkw_gender":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject gender = (DynamicObject) value;
                        filter.and("hrpi_pernontsprop.gender", QCP.equals, gender.getLong("id"));
                    }
                    break;
                //  职务
                case "tdkw_ranks":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject ranks = (DynamicObject) value;
                        filter.and("hrpi_empposorgrel.tdkw_ranks", QCP.equals, ranks.getLong("id"));
                    }
                    break;
                //  岗位序列
                case "tdkw_jobseq":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject jobSeq = (DynamicObject) value;
                        filter.and("hrpi_empposorgrel.tdkw_jobsequence", QCP.equals, jobSeq.getLong("id"));
                    }
                    break;
                //政治面貌
                case "tdkw_political_status":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObject political = (DynamicObject) value;
                        filter.and("hrpi_perregion.politicalstatus", QCP.equals, political.getLong("id"));
                    }
                    break;
                //工作地点
                case "tdkw_workpalce":
                    if (StringUtils.isNotBlank((String) value)) {
                        filter.and("hrpi_pernontsprop.tdkw_contrworkloc.name", QCP.like, "%" + value + "%");
                    }
                    break;
                //  院校类型
                case "tdkw_schooltype":
                    if (ObjectUtils.isNotEmpty(value)) {
                        DynamicObjectCollection schoolStyle = (DynamicObjectCollection) value;
                        List<Long> number = schoolStyle.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        schoolTypeFilter.and("collegecharact.fbasedataid", QCP.in, number);
                        schoolTypeFlag = true;
                    }
                    break;
                //  家属工作单位
                case "tdkw_famwork":
                    if (StringUtils.isNotBlank((String) value)) {
                        filter.and("hrpi_familymemb.workunit", QCP.like, "%" + value + "%");
                    }
                    break;
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("filter", filter);
        map.put("schoolTypeFilter", schoolTypeFilter);
        map.put("schoolTypeFlag", schoolTypeFlag);
        return map;
    }

    /**
     * 读取查询配置 LocalRepository为自己自己封装的读取查询配置的工具类 需要去改标识 一个查询配置对应一个Repository 相同的读取可复用
     */
    private final QueryEntityType queryType = (QueryEntityType) LeaRepository.generate().getDataEntityType();


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
        DataSet queryDataSetlevel;
        DataSet queryDataSetjob;
        boolean schoolTypeFlag = false;
        try {
            FilterInfo filterInfo = reportQueryParam.getFilter();
            this.reportPlanId = filterInfo.getLong("report");
            Set<String> queryFieldSet = new HashSet<>();
            queryFieldSet.add("name");
            if (CollectionUtils.isEmpty(queryFieldSet)) {
                Object var11 = null;
                return (DataSet) var11;
            }

            matIdsOfCurrentBatch = this.getQueryIdList(currentBatchRows);
            // 过滤条件构建
            QFilter filter = new QFilter("id", QCP.in, matIdsOfCurrentBatch);
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
                if ("hrpi_empposorgrel.position.tdkw_positionlevel1".equals(s) || "collegecharact.fbasedataid".equals(s) || "collegecharact.fbasedataid.name".equals(s)) {
                    continue;
                }
                if (queryField.length() > 0) {
                    queryField.append(",");
                }
                queryField.append(s);
            }
            //System.out.println(queryField);
            queryDataSet = HRQueryEntityHelper.getInstance().getQueryDataSet(this.queryType, queryField.toString(), filter.toArray(), "createtime desc", null);
            /*//todo 查院校类别
            // "hbss_college";
            String algoKey = this.getClass().getName();
            QFilter schoolQfilter = new QFilter("enable", QCP.equals, "1");
            //查高等院校 id,院校类型id,院校类型名称
            DataSet schoolDataSet = ORM.create().queryDataSet(algoKey, "hbss_college", "id,collegecharact.fbasedataid,collegecharact.fbasedataid.name", new QFilter[]{schoolTypeFilter, schoolQfilter});
            String[] reportFieldString = queryField.toString().split(",");
            String[] redundancyFieldString = new String[2];
            redundancyFieldString[0] = "collegecharact.fbasedataid";
            redundancyFieldString[1] = "collegecharact.fbasedataid.name";
            queryDataSetjob = queryDataSet.leftJoin(schoolDataSet).on("hrpi_pereduexp.graduateschool.id", "id").select(reportFieldString, redundancyFieldString).finish();
            if (schoolTypeFlag) {
                queryDataSetjob = queryDataSetjob.where("collegecharact.fbasedataid is not null");
            }
            DataSet queryDataSetJobCopy = queryDataSetjob.copy();*/

            queryDataSetlevel = queryDataSet.addField("case when contains(hrpi_empposorgrel.tdkw_postlevel,'4')then '中层'" +
                    "when contains (hrpi_empposorgrel.tdkw_postlevel,'5') then '基层' " +
                    "when contains (hrpi_empposorgrel.tdkw_postlevel,'3') then '其他高管' " +
                    "when contains (hrpi_empposorgrel.tdkw_postlevel,'2') then '行业高管' " +
                    "when contains (hrpi_empposorgrel.tdkw_postlevel,'1') then '集团高管' " +
                    "else '' end", "hrpi_empposorgrel.position.tdkw_positionlevel1");
        } finally {
            //  LOGGER.info(MessageFormat.format("EmpReportListPlugin.query---end query by batch,current size={0},executeTime={1} ms.", ((Set)matIdsOfCurrentBatch).size(), System.currentTimeMillis() - startTime));
        }
        DynamicObjectCollection collection = ORM.create().toPlainDynamicObjectCollection(queryDataSetlevel.copy());
        return queryDataSetlevel;

    }

    //获取元数据
    MainEntityType entityType = EntityMetadataCache.getDataEntityType("tdkw_leave_retire_report");
    EntityMetadata entityMetadata = (EntityMetadata) MetadataDao.readRuntimeMeta(MetadataDao.getIdByNumber(entityType.getName(), MetaCategory.Entity), MetaCategory.Entity);

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


    private static final Integer BATCH_COUNT = 200;

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
