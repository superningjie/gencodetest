package tdkw.wtc.common;

import com.google.common.collect.Sets;
import kd.bos.algo.DataSet;
import kd.bos.algo.DataType;
import kd.bos.algo.RowMeta;
import kd.bos.algo.datatype.BigDecimalType;
import kd.bos.algo.datatype.IntegerType;
import kd.bos.algo.datatype.LongType;
import kd.bos.algo.datatype.TimestampType;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.entity.report.ReportColumn;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.wtc.wtbs.business.history.service.WTCHisServiceHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @description
 * @author: hxl
 * @date: 2024/8/6 18:06
 */
public class CommonUtils {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * @param entityName   单据表示
     * @param selectFields 查询字段
     * @param filter       过滤条件
     * @return 查出数据
     */
    public static DynamicObject[] load(String entityName, String selectFields, QFilter filter) {
        return  HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonSyncService", "listFieldsFilterInfo", selectFields, filter, entityName);
    }

    /**
     *
     * @param appId 应用id
     * @param parentEntityNumber 单据标识
     * @param propFullKey 字段标识
     * @return
     */
    public static QFilter getDataRuleForBdProp(String appId, String parentEntityNumber, String propFullKey) {
        return getDataRuleForBdProp(RequestContext.get().getCurrUserId(), appId, parentEntityNumber, propFullKey, "47156aff000000ac");
    }

    public static QFilter getDataRuleForBdProp(long userId, String appId, String parentEntityNumber, String propFullKey, String permItemId) {
        return (QFilter) invokeHRMPService("hrcs", "IHRCSDataPermissionService", "getDataRuleForBdProp", new Object[]{userId, appId, parentEntityNumber, propFullKey, permItemId, new HashMap()});
    }

    public static <T> T invokeHRMPService(String appId, String serviceName, String methodName, Object... paras) {
        return invokeBizService("hrmp", appId, serviceName, methodName, paras);
    }

    public static <T> T invokeBizService(String cloudId, String appId, String serviceName, String methodName, Object... paras) {
        return DispatchServiceHelper.invokeBizService(cloudId, appId, serviceName, methodName, paras);
    }

    public static DynamicObject getCurrUserAttFile() {
        DynamicObject bos_user = BusinessDataServiceHelper.loadSingleFromCache( RequestContext.get().getCurrUserId(),"bos_user");
        DynamicObject attQuery = BusinessDataServiceHelper.loadSingleFromCache(Constant.KEY_WTP_ATTFILEBASE, "org", new QFilter("personnum", QCP.equals, bos_user.get("number")).and(
                WTCHisServiceHelper.isCurrentVersion(true)).toArray());
        return attQuery;
    }

    public static DataSet getHeadFilters(ReportQueryParam reportQueryParam, DataSet select) {
        List<QFilter> filters = reportQueryParam.getFilter().getHeadFilters();
        StringBuilder sb = new StringBuilder();
        RowMeta rowMeta = select.getRowMeta();
        HashSet<String> numberSet = Sets.newHashSet("mdnb_age", "mdnb_exitcontroltime", "mdnb_careertenure", "mdnb_rankyears");
        for (int i = 0; i < filters.size(); i++) {
            QFilter q = filters.get(i);
            String field = q.getProperty();
            DataType dataType = rowMeta.getField(field).getDataType();
            String qstr = q.toString();
            if (dataType instanceof IntegerType || (dataType instanceof LongType && numberSet.contains(field)) || dataType instanceof BigDecimalType) {
                sb.append(qstr, 0, q.toString().indexOf("'")).append(q.getValue());
            } else if (dataType instanceof LongType) {
                //sb.append(qstr.replaceFirst(" ", ".name "));
                sb.append(qstr.replaceAll(field, field + ".name "));
            } else if (dataType instanceof TimestampType) {
                if (q.toString().contains("AND")) {
                    String[] strs = q.toString().split("AND");
                    for (int j = 0; j < strs.length; j++) {
                        String str = strs[j];
                        sb.append(str.replaceFirst("'", "TO_DATE('")).append(",'yyyy-MM-dd')");
                        if (j != strs.length - 1) {
                            sb.append(" and ");
                        }
                    }
                } else {
                    sb.append(qstr, 0, q.toString().indexOf("'")).append("TO_DATE('").append(q.getValue()).append("','yyyy-MM-dd')");
                }
            } else {
                sb.append(qstr);
            }
            if (i != filters.size() - 1) {
                sb.append(" and ");
            }
        }
        if (sb.length() > 0) {
            select = select.filter(sb.toString().replaceAll("null ''", "null"));
        }
        return select;
    }

    @NotNull
    public static String[] getStrReplace(DataSet personDataSet) {
        String[] fieldNames = personDataSet.getRowMeta().getFieldNames();
        IntStream.range(0, fieldNames.length)
                .forEach(i -> {
                    String fieldName = fieldNames[i];
                    if (fieldName.contains(".")) {
                        fieldNames[i] = fieldName+" AS "+fieldName.replace(".", "_");
                    }
                });
        return fieldNames;
    }

    public static void columnsSetFreeze(List<AbstractReportColumn> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 1) {
                break;
            }
            ReportColumn reportColumn = (ReportColumn) columns.get(i);
            reportColumn.setFreeze(true);
            columns.set(i, reportColumn);
        }
    }

}
