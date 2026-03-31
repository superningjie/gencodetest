package tdkw.esc.leaderquery.report.groupcommittee;

import com.google.common.collect.Lists;
import kd.bos.algo.DataSet;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.QueryEntityType;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.business.servicehelper.HRQueryEntityHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.util.HRDateTimeUtils;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hspm.business.domian.repository.ReportDisplayRepository;
import kd.sdk.hr.hspm.business.repository.ext.service.EmpReportExtCommon;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository
 *
 * @author xxx
 * @date 2023/6/17
 */
public class GroupRepository {
    /**
     * 查询领导查询-工团纪
     * 此处为查询配置的标识   配置工具-查询配置。
     */
    //TODO 改为要读取的查询配置编码
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper("hspm_gtjquery");
    private static final String FIELD_ADMINIORG = "hrpi_empposorgrel.adminorg";
    private static final Long WORKING_STATE_ID = 1010L;

    public GroupRepository() {
    }

    public static DynamicObject generate() {
        return HELPER.generateEmptyDynamicObject();
    }

    public static DataSet getDataSet(String selectFields, FilterInfo filterInfo, Long id) {
        QueryEntityType queryType = (QueryEntityType) generate().getDataEntityType();
        String sort = ReportDisplayRepository.getSort(id);
        List<Object> ids = HRQueryEntityHelper.getInstance().queryAllPkByKSql(queryType, new QFilter[]{handleFilter(filterInfo)}, sort);
        return HRQueryEntityHelper.getInstance().getQueryDataSet(queryType, selectFields, new QFilter[]{new QFilter("id", "in", ids)}, sort);
    }

    public static QFilter getOrgFilter(String fieldName) {
        //TODO entityNum:"tdkw_report" 改为报表的标识
        HasPermOrgResult result = PermissionServiceHelper.getAllPermOrgs(RequestContext.get().getCurrUserId(), "hspm", "tdkw_groupcom_report", "47150e89000000ac");
        if (result == null) {
            return QFilter.isNull("id");
        } else {
            return !result.hasAllOrgPerm() ? new QFilter(fieldName, "in", result.getHasPermOrgs()) : QFilter.isNotNull("id");
        }
    }

    public static QFilter getAdminOrgFilter(String fieldName) {
        //TODO "tdkw_report" 改为报表的标识
        AuthorizedOrgResult result = (AuthorizedOrgResult) HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSBizDataPermissionService", "getAuthorizedAdminOrgsF7", new Object[]{RequestContext.get().getCurrUserId(), "hrpi", "tdkw_groupcom_report", "47150e89000000ac", "adminorg"});
        if (result == null) {
            return QFilter.isNull("id");
        } else {
            return !result.isHasAllOrgPerm() ? new QFilter(fieldName, "in", result.getHasPermOrgs()) : QFilter.isNotNull("id");
        }
    }

    public static QFilter handleFilter(FilterInfo filterInfo) {
        QFilter filter = initFilter(filterInfo);
        andCommonFilter(filterInfo, filter);
        andDataRuleFilter(filter);
        andQueryFilter(filterInfo, filter);
        EmpReportExtCommon.addExtQueryFilter(filterInfo, filter);
        return filter;
    }

    public static Map<String, List<QFilter>> reletionMapFilter(FilterInfo filterInfo) {
        Date queryDate = getQueryDate(filterInfo);
        Map<String, List<QFilter>> reletionMap = new HashMap(16);
        reletionMap.put("hrpi_empposorgrel", Lists.newArrayList(new QFilter[]{(new QFilter("hrpi_empposorgrel.startdate", "<=", queryDate)).and(new QFilter("hrpi_empposorgrel.enddate", ">=", queryDate))}));
        reletionMap.put("hrpi_contrworkloc", Lists.newArrayList(new QFilter[]{(new QFilter("hrpi_contrworkloc.startdate", "<=", queryDate)).and(new QFilter("hrpi_contrworkloc.enddate", ">=", queryDate))}));
        reletionMap.put("hrpi_empjobrel", Lists.newArrayList(new QFilter[]{(new QFilter("hrpi_empjobrel.startdate", "<=", queryDate)).and(new QFilter("hrpi_empjobrel.enddate", ">=", queryDate))}));
        reletionMap.put("hrpi_employee", Lists.newArrayList(new QFilter[]{(new QFilter("hrpi_employee.bsed", "<=", queryDate)).and(new QFilter("hrpi_employee.bsled", ">=", queryDate))}));
        reletionMap.put("hrpi_pertsprop", Lists.newArrayList(new QFilter[]{(new QFilter("hrpi_pertsprop.bsed", "<=", queryDate)).and(new QFilter("hrpi_pertsprop.bsled", ">=", queryDate))}));
        EmpReportExtCommon.addExtReletionFilter(filterInfo, reletionMap);
        return reletionMap;
    }

    private static QFilter initFilter(FilterInfo filterInfo) {
        QFilter filter = (new QFilter("iscurrentversion", "=", "1")).and(new QFilter("datastatus", "=", "1")).and(new QFilter("person", "!=", 0L)).and(new QFilter("employee", "!=", 0L)).and(new QFilter("depemp", "!=", 0L)).and(new QFilter("initstatus", "=", "2")).and(new QFilter("hrpi_employee.laborrelstatus.labrelstatusprd", "=", WORKING_STATE_ID));
        Date queryDate = getQueryDate(filterInfo);
        filter.and((new QFilter("startdate", "<=", queryDate)).and(new QFilter("enddate", ">=", queryDate)));
        return filter;
    }


    private static void andCommonFilter(FilterInfo filterInfo, QFilter filter) {
        //TODO "hspm_qbggquery"改为要读取的查询配置编码
        QFilter commonFilter = (QFilter) filterInfo.getCommFilter().get("hspm_gtjquery");
        if (commonFilter != null) {
            filter.and(commonFilter);
        }

    }

    private static void andDataRuleFilter(QFilter filter) {
        //TODO "hspm_qbggquery 改为要读取的查询配置编码
        QFilter dataRuleFilter = (QFilter) HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSDataPermissionService", "getDataRule", new Object[]{RequestContext.get().getCurrUserId(), "1WXBPN7+OHJZ", "hspm_gtjquery", "47150e89000000ac", Collections.emptyMap()});
        if (dataRuleFilter != null) {
            filter.and(dataRuleFilter);
        }

    }

    private static void andQueryFilter(FilterInfo filterInfo, QFilter filter) {
        List<Long> orgFilter = new ArrayList(16);
        setListFilter(filterInfo, orgFilter, "org");
        if (!orgFilter.isEmpty()) {
            filter.and(new QFilter("org", "in", orgFilter));
        } else {
            filter.and(getOrgFilter("org"));
        }

        List<Long> adminOrgFilter = new ArrayList(16);
        setListFilter(filterInfo, adminOrgFilter, "adminorg");
        if (!adminOrgFilter.isEmpty()) {
            filter.and(new QFilter("hrpi_empposorgrel.adminorg", "in", adminOrgFilter));
        } else {
            filter.and(getAdminOrgFilter("hrpi_empposorgrel.adminorg"));
        }

        addCommonListFilter(filterInfo, filter, "job", "hrpi_empjobrel.job");
        addCommonListFilter(filterInfo, filter, "position", "hrpi_empposorgrel.position");
        addCommonListFilter(filterInfo, filter, "postype", "hrpi_empposorgrel.postype");
        List<Long> laborRelTypeClsFilter = new ArrayList(16);
        setListFilter(filterInfo, laborRelTypeClsFilter, "laborreltypecls");
        if (!laborRelTypeClsFilter.isEmpty()) {
            filter.and(new QFilter("hrpi_employee.laborreltype.laborreltypecls", "in", laborRelTypeClsFilter));
        }

        setPersonFilter(filter, filterInfo, "person");
    }

    public static Date getQueryDate(FilterInfo filterInfo) {
        FilterItemInfo itemInfo = filterInfo.getFilterItem("date");
        Date queryDate = (Date) itemInfo.getValue();
        if (queryDate == null) {
            queryDate = new Date();
        }

        try {
            queryDate = HRDateTimeUtils.parseDate(HRDateTimeUtils.format(queryDate), "yyyy-MM-dd");
            return queryDate;
        } catch (ParseException var4) {
            throw new KDBizException(var4, new ErrorCode("", "EmpReportRepository queryDate format error."), new Object[0]);
        }
    }

    private static void addCommonListFilter(FilterInfo filterInfo, QFilter filter, String fieldKey, String showNodeId) {
        List<Long> list = new ArrayList(16);
        setListFilter(filterInfo, list, fieldKey);
        if (!list.isEmpty()) {
            filter.and(new QFilter(showNodeId, "in", list));
        }

    }

    public static void setListFilter(FilterInfo filterInfo, List<Long> list, String key) {
        FilterItemInfo itemInfo = filterInfo.getFilterItem(key);
        DynamicObjectCollection curStr = itemInfo == null ? null : (DynamicObjectCollection) itemInfo.getValue();
        if (curStr != null) {
            list.addAll((Collection) curStr.stream().map((item) -> {
                return item.getLong("id");
            }).collect(Collectors.toList()));
        }

    }

    public static void setPersonFilter(QFilter filter, FilterInfo filterInfo, String key) {
        FilterItemInfo itemInfo = filterInfo.getFilterItem(key);
        String curStr = itemInfo == null ? " " : itemInfo.getString();
        if (HRStringUtils.isNotEmpty(curStr)) {
            String[] personFilters = curStr.split(";");
            QFilter personFilter = null;

            for (int index = 0; index < personFilters.length; ++index) {
                String filterStr = '%' + personFilters[index] + '%';
                if (personFilter == null) {
                    personFilter = new QFilter("person.number", "like", filterStr);
                } else {
                    personFilter.or("person.number", "like", filterStr);
                }

                personFilter.or("person.name", "like", filterStr);
            }

            filter.and(personFilter);
        }

    }
}
