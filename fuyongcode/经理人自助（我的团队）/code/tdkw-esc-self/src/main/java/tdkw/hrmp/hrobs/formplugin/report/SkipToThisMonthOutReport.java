package tdkw.hrmp.hrobs.formplugin.report;

import com.google.common.collect.Maps;
import javafx.util.Pair;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DataEntityBase;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PinyinUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SkipToThisMonthOutReport
 *
 * @author xxx
 * @date 2023/7/14
 */
public class SkipToThisMonthOutReport extends AbstractReportListDataPlugin {


    /**
     * 控制权限中角色的编码
     */
    private static final String ROLE_ORG = "tdkw_thismonthdepart_pc";
    //public static final Log logger = LogFactory.getLog(SkipToThisMonthOutReport.class);

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        DataSet finish = getDataSet(this.getClass().getName(), reportQueryParam, null);
        return finish;
    }

    public String getThisMonthOut(String algoKey, String diMissionOrRetire) {
        // TODO 缺少字段 临时处理
        /*
        DataSet dataSet = getDataSet(algoKey, null, diMissionOrRetire);
        int size = ORM.create().toPlainDynamicObjectCollection(dataSet).size();
        // logger.info("SkipToThisMonthOutReport java:55 本月离职人数为:" + size + "人");
        return String.valueOf(size);
        */
        return "5";
    }

    private DataSet getDataSet(String algoKey, ReportQueryParam reportQueryParam, String diMissionOrRetire) {
        HashMap<String, Object> topFilter = getTopFilter(reportQueryParam);
        QFilter topFlowFilter = (QFilter) topFilter.get("flowFilter");
        QFilter topErManFileFilter = (QFilter) topFilter.get("erManFileFilter");
        QFilter pernontsprQfilter = (QFilter) topFilter.get("pernontsprQfilter");
        QFilter quitfileQfilter = (QFilter) topFilter.get("quitfileQfilter");
        //是否进行了表头对人事业务档案过滤
        QFilter flowFilter = getPersonIds();
        if (StringUtils.isNotEmpty(diMissionOrRetire)) {
            //退休
            if ("retire".equals(diMissionOrRetire)) {
                //离职或退休过滤
                flowFilter.and("tdkw_changereason.number", QCP.equals, "XY00050");
            }
            //离职
            if ("dimission".equals(diMissionOrRetire)) {
                //入职,查变动类型-变动大类为离职
                flowFilter.and("tdkw_changetype.tdkw_chgevent.number", QCP.equals, "1070_S");
                flowFilter.and("tdkw_changereason.number", QCP.not_equals, "XY00050");
            }
        } else {
            //入职,查变动类型-变动大类为离职
            flowFilter.and((QFilter) topFilter.get("diMissionOrRetireFilter"));
        }
        flowFilter.and("depemp.isprimary", QCP.equals, Boolean.TRUE);
        //查询离职档案数据
        DataSet quitfileDataSet = ORM.create().queryDataSet(algoKey, "htm_quitfileinfo", "employee.id,contractenddate", new QFilter[]{quitfileQfilter});
        List<Long> employeeId = ORM.create().toPlainDynamicObjectCollection(quitfileDataSet.copy()).stream().map(i -> i.getLong("employee.id")).collect(Collectors.toList());
        flowFilter.and("employee.id", QCP.in, employeeId);

        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create("hpfs_personflow");
        DynamicObject[] query = hrBaseServiceHelper.query("person", new QFilter[]{flowFilter, topFlowFilter});
        //本月离职的人员id
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter personFilter = new QFilter("person", QCP.in, personIds);

        // 人员非时序性属性
        DataSet hrpiPernontspropDataSet = ORM.create().queryDataSet(algoKey, "hrpi_pernontsprop", "person,tdkw_full_pinyin_name as pinyin", new QFilter[]{personFilter, pernontsprQfilter});

        //查询报表数据
        DataSet dataSet = ORM.create().queryDataSet(algoKey, "hpfs_personflow", "person,employee.id,flowtime,tdkw_changereason.name,personnumber", new QFilter[]{flowFilter, topFlowFilter});

        dataSet = quitfileDataSet.leftJoin(dataSet).on("employee.id", "employee.id").select("person", "employee.id", "tdkw_changereason.name", "personnumber", "contractenddate as flowtime").finish();

        // int size = ORM.create().toPlainDynamicObjectCollection(dataSet).size();
        // logger.info("SkipToThisMonthOutReport java:73 流入流出表人数为:" + size + "人");

        //人事业务档案
        QFilter erManFile = new QFilter("businessstatus", QCP.equals, "1");
        erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        //全职任职
        erManFile.and("empposrel.postype.number", QCP.equals, "XY00001");
        //erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFile.and("empposrel.datastatus", QCP.equals, "1");
        erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);

        DataSet ermanFileDataSet = ORM.create().queryDataSet(algoKey, "hspm_ermanfile", "id,person,empposrel.position,empposrel.tdkw_postlevel,empposrel.company,empposrel.adminorg,tdkw_index,person.tdkw_pkid,empposrel.adminorg.sortcode", new QFilter[]{erManFile, personFilter, topErManFileFilter});

        //todo 解决对人事业务档案过滤后的左连接问题
        List<Long> personId = ORM.create().toPlainDynamicObjectCollection(ermanFileDataSet.copy()).stream().map(i -> i.getLong("person")).collect(Collectors.toList());
        HashMap<String, Object> param = new HashMap<>();
        param.put("param", personId);
        DataSet flowDataSet = dataSet.filter("person in param", param);

        //人员非时序性属性左连接流入流出表
        DataSet data = hrpiPernontspropDataSet.join(flowDataSet).on("person", "person").select("pinyin", "person", "flowtime", "tdkw_changereason.name", "personnumber").finish();

        DataSet finish = data.leftJoin(ermanFileDataSet).on("person", "person").select("person", "empposrel.position as tdkw_position", "empposrel.tdkw_postlevel as tdkw_postlevel", "flowtime as tdkw_leavedate", "empposrel.company as tdkw_company", "tdkw_changereason.name as tdkw_reason", "empposrel.adminorg as tdkw_adminorg", "id as tdkw_ermanfileid", "tdkw_index as index", "person.tdkw_pkid as personid", "empposrel.adminorg.sortcode as sortcode", "personnumber").finish().orderBy(new String[]{"tdkw_postlevel", "sortcode", "tdkw_leavedate desc", "personnumber"});
        finish.copy().print(true);

        return finish;
    }

    private HashMap<String, Object> getTopFilter(ReportQueryParam reportQueryParam) {
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        QFilter erManFileFilter = new QFilter("1", QCP.equals, 1);
        QFilter flowFilter = new QFilter("1", QCP.equals, 1);
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        QFilter quitfileQfilter = new QFilter("1", QCP.equals, 1);
        pernontsprQfilter.and("iscurrentversion", QCP.equals, "1");
        pernontsprQfilter.and("datastatus", QCP.equals, "1");

        map.put("pernontsprQfilter", pernontsprQfilter);
        map.put("quitfileQfilter", quitfileQfilter);
        map.put("flowFilter", flowFilter);
        map.put("erManFileFilter", erManFileFilter);

        Pair<Date, Date> pair = getTime();
        if (null == reportQueryParam) {
            QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
            DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
            List<Object> ids = Arrays.stream(load).map(DataEntityBase::getPkValue).collect(Collectors.toList());
            erManFileFilter.and("empposrel.tdkw_employtype", QCP.in, ids);
//            flowFilter.and("flowtime", QCP.large_equals, pair.getKey());
//            flowFilter.and("flowtime", QCP.less_equals, pair.getValue());
            quitfileQfilter.and("contractenddate", QCP.large_equals, pair.getKey());
            quitfileQfilter.and("contractenddate", QCP.less_equals, pair.getValue());
            map.put("erManFileFilter", erManFileFilter);
            map.put("flowFilter", flowFilter);
            return map;
        }
        List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
        if (0 == filterItems.size()) {
            QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
            DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
            List<Object> ids = Arrays.stream(load).map(DataEntityBase::getPkValue).collect(Collectors.toList());
            erManFileFilter.and("empposrel.tdkw_employtype", QCP.in, ids);
//            flowFilter.and("flowtime", QCP.large_equals, pair.getKey());
//            flowFilter.and("flowtime", QCP.less_equals, pair.getValue());
            quitfileQfilter.and("contractenddate", QCP.large_equals, pair.getKey());
            quitfileQfilter.and("contractenddate", QCP.less_equals, pair.getValue());
            Object diMissionType = reportQueryParam.getCustomParam().get("diMissionType");
            if (ObjectUtils.isNotEmpty(diMissionType)) {
                //退休
                if ("retire".equals(diMissionType)) {
                    //离职或退休过滤
                    QFilter diMissionOrRetireFilter = new QFilter("tdkw_changereason.number", QCP.equals, "XY00050");
                    map.put("diMissionOrRetireFilter", diMissionOrRetireFilter);
                }
                //离职
                if ("dimission".equals(diMissionType)) {
                    //离职或退休过滤
                    QFilter diMissionOrRetireFilter = new QFilter("tdkw_changetype.tdkw_chgevent.number", QCP.equals, "1070_S");
                    flowFilter.and("tdkw_changereason.number", QCP.not_equals, "XY00050");
                    map.put("diMissionOrRetireFilter", diMissionOrRetireFilter);
                }
            }

        } else {
            for (FilterItemInfo filterItem : filterItems) {
                String propName = filterItem.getPropName();
                Object value = filterItem.getValue();
                switch (propName) {
                    //所属组织 基础资料
                    case "tdkw_companyfilter":
                        if (ObjectUtils.isNotEmpty(value)) {
                            DynamicObject org = (DynamicObject) value;
                            List<Long> orgId = HRRoleAndPersonUtils.getHROrgIds(String.valueOf(org.getLong("id")));
                            QFilter orgFilter = new QFilter("empposrel.company", QCP.in, orgId).or("empposrel.adminorg", QCP.in, orgId);
                            erManFileFilter.and(orgFilter);
                        }
                        break;
                    //岗位层级  枚举
                    case "tdkw_postlevefilter":
                        if (ObjectUtils.isNotEmpty(value)) {
                            erManFileFilter.and("empposrel.tdkw_postlevel", QCP.equals, value);
                        }
                        break;
                    //日期范围 流入流出表
                    case "tdkw_begindate":
                        if (ObjectUtils.isNotEmpty(value)) {
//                            flowFilter.and("flowtime", QCP.large_equals, value);
                            quitfileQfilter.and("contractenddate", QCP.large_equals, value);
                        }
                        break;
                    // 日期范围  流入流出表
                    case "tdkw_enddate":
                        if (ObjectUtils.isNotEmpty(value)) {
//                            flowFilter.and("flowtime", QCP.less_equals, value);
                            quitfileQfilter.and("contractenddate", QCP.less_equals, value);
                        }
                        break;
                    //人员类型 基础资料 流入流出表 laborreltype
                    case "tdkw_laborreltype":
                        if (ObjectUtils.isNotEmpty(value)) {
                            DynamicObjectCollection userType = (DynamicObjectCollection) value;
                            List<Long> ids = userType.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                            erManFileFilter.and("empposrel.tdkw_employtype", QCP.in, ids);
                        }
                        break;
                    //姓名
                    case "tdkw_personname":
                        if (StringUtils.isNotBlank((String) value)) {
                            String nameStr = value.toString();
                            if (PinyinUtil.containsLetters(nameStr)) {
                                // 包含字母 pinyin
                                String pinYinName;
                                try {
                                    pinYinName = PinyinUtil.getPingYin(nameStr);
                                } catch (Exception e) {
                                    // 按字符分割数组对单个文字进行转拼音
                                    // 拼接汇总拼音
                                    StringBuilder pinYinNameBuilder = new StringBuilder();
                                    for (int i = 0; i < nameStr.length(); i++) {
                                        String pingYin = PinyinUtil.getPingYin(String.valueOf(nameStr.charAt(i)));
                                        pinYinNameBuilder.append(pingYin);
                                    }
                                    pinYinName = pinYinNameBuilder.toString();
                                }
                                pernontsprQfilter.and("tdkw_full_pinyin_name", QCP.like, "%" + pinYinName + "%");
                            } else {
                                // 不包含字母
                                flowFilter.and("personname", QCP.like, "%" + value + "%");
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            Object diMissionType = reportQueryParam.getCustomParam().get("diMissionType");
            if (StringUtils.equals((CharSequence) diMissionType, "dimission")) {
                //离职或退休过滤
                QFilter diMissionOrRetireFilter = new QFilter("tdkw_changetype.tdkw_chgevent.number", QCP.equals, "1070_S");
                flowFilter.and("tdkw_changereason.number", QCP.not_equals, "XY00050");
                map.put("diMissionOrRetireFilter", diMissionOrRetireFilter);
            } else if (StringUtils.equals((CharSequence) diMissionType, "retire")) {
                //退休过滤
                QFilter diMissionOrRetireFilter = new QFilter("tdkw_changereason.number", QCP.equals, "XY00050");
                map.put("diMissionOrRetireFilter", diMissionOrRetireFilter);
            }
        }
        map.put("quitfileQfilter", quitfileQfilter);
        map.put("pernontsprQfilter", pernontsprQfilter);
        map.put("flowFilter", flowFilter);
        map.put("erManFileFilter", erManFileFilter);
        return map;
    }

    private QFilter getPersonIds() {
        //查询流入流出表,获取本月入职人员
        //todo 控权组组织过滤,过滤当前组织
        QFilter qFilter = new QFilter("1", QCP.equals, 1);
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), ROLE_ORG);//如果角色控权包含10000L的话 不对权限进行控制
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            //没有所有组织的权限
            List<Long> hasPerOrg = result.getHasPermOrgs();
            //获取权限范围组织下所有人员id
            // List<Map<String, Object>> personIdByOrg = HRPIPersonServiceHelper.getPersonByOrgs(hasPerOrg, new Date());
            // List<Long> personIds = personIdByOrg.stream().map(o -> (Long) o.get("person")).collect(Collectors.toList());
            qFilter.and("adminorg", QCP.in, hasPerOrg);
        }
        return qFilter;
    }

    private Pair<Date, Date> getTime() {
        //获取本月月初到今天
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date startTime = calendar.getTime();
        Pair<Date, Date> pair = new Pair<>(startTime, endTime);
        return pair;
    }
}
