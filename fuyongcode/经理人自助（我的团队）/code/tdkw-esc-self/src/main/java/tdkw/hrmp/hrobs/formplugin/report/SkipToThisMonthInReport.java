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
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * ThisMonthOnBoardingReport
 * 本月入职
 *
 * @author xxx
 * @date 2023/7/14
 */
public class SkipToThisMonthInReport extends AbstractReportListDataPlugin {

    /**
     * 控制权限中角色的编码
     */
    private static final String ROLE_ORG = "tdkw_thismonthentry_pc";
    private static final Log logger = LogFactory.getLog(SkipToThisMonthInReport.class);

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        return getDataSet(this.getClass().getName(), reportQueryParam);
    }

    private DataSet getDataSet(String algoKey, ReportQueryParam reportQueryParam) {
        long start = System.currentTimeMillis();
        // 获取表头过滤条件
        HashMap<String, Object> topFilter = getTopFilter(reportQueryParam);
        QFilter topFlowFilter = (QFilter) topFilter.get("flowFilter");
        QFilter topErManFileFilter = (QFilter) topFilter.get("erManFileFilter");
        QFilter pernontsprQfilter = (QFilter) topFilter.get("pernontsprQfilter");
        QFilter flowFilter = getPersonIds();
        // 入职,查变动类型-变动大类为入职
        flowFilter.and("tdkw_changetype.tdkw_chgevent.number", QCP.equals, "1010_S");
        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create("hpfs_personflow");
        DynamicObject[] query = hrBaseServiceHelper.query("person", new QFilter[]{flowFilter, topFlowFilter});
        // 本月入职的人员id
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter personFilter = new QFilter("person", QCP.in, personIds);
        // 人员非时序性属性
        DataSet hrpiPernontspropDataSet = ORM.create().queryDataSet(algoKey, "hrpi_pernontsprop", "person,tdkw_full_pinyin_name as pinyin",
                new QFilter[]{personFilter, pernontsprQfilter});
        // 查询流入流出表数据
        DataSet dataSet = ORM.create().queryDataSet(algoKey, "hpfs_personflow", "person,flowtime,adminorg,adminorg.company,depemp.position,depemp.position.tdkw_positionlevel,adminorg.sortcode,personnumber",
                new QFilter[]{flowFilter, topFlowFilter});
        // 查询人事业务档案数据
        DataSet ermanFileDataSet = ORM.create().queryDataSet(algoKey, "hspm_ermanfile", "id,person,empposrel.position,empposrel.tdkw_postlevel,tdkw_index,person.tdkw_pkid",
                new QFilter[]{personFilter, topErManFileFilter});
        // 查询参加党派记录 是否当前版本=1，数据版本状态=1
        QFilter versionFilter = new QFilter("iscurrentversion", QCP.equals, "1").and("datastatus", QCP.equals, "1").and(new QFilter("tdkw_enddate", QCP.equals, null).or("tdkw_enddate", QCP.large_equals, new Date()));
        DataSet partyDataSet = ORM.create().queryDataSet(algoKey, "tdkw_hrpi_attendrecords", "person,tdkw_partyorg,tdkw_partyaffairs", new QFilter[]{personFilter, versionFilter});
        // 查询其他任职信息
        DataSet otherEmployInfoDataSet = ORM.create().queryDataSet(algoKey, "tdkw_hrpi_otheremployinf", "person,tdkw_post.number", new QFilter[]{personFilter, versionFilter});
        // 查询任职信息维护 任职信息维护中的单据状态=审批通过
        versionFilter = new QFilter("billstatus", QCP.equals, "C").and(new QFilter("tdkw_dgtjendtime", QCP.equals, null).or("tdkw_dgtjendtime", QCP.large_equals, new Date()));
        // 党工团纪的人员标识单独处理
        personFilter = new QFilter("tdkw_dgtjperson", QCP.in, personIds);
        DataSet gtjExperienceDataSet = ORM.create().queryDataSet(algoKey, "tdkw_dgtj_experience", "tdkw_dgtjperson,tdkw_dgtjorg.tdkw_basedatafield.name", new QFilter[]{personFilter, versionFilter});
        // 人员非时序性属性左连接流入流出表
        DataSet data = hrpiPernontspropDataSet.join(dataSet).on("person", "person").select("pinyin", "person", "flowtime", "adminorg", "adminorg.company", "depemp.position", "depemp.position.tdkw_positionlevel", "adminorg.sortcode", "personnumber").finish();
        // 表左连接 流入流出表左连接人事业务档案
        DataSet finish = data.join(ermanFileDataSet).on("person", "person").select("pinyin", "person", "depemp.position as tdkw_position", "depemp.position.tdkw_positionlevel as tdkw_postlevel", "flowtime as tdkw_joincomdate", "adminorg.company as tdkw_company", "adminorg as tdkw_adminorg", "id as tdkw_ermanfileid", "tdkw_index as index", "person.tdkw_pkid as personid", "adminorg.sortcode as sortcode", "personnumber").finish().orderBy(new String[]{"tdkw_postlevel", "sortcode", "tdkw_joincomdate desc", "personnumber"});
        // 获取对任职类型的过滤
        String workType = (String) topFilter.get("workType");
        switch (workType) {
            case "A":
                // 连接参加党派记录
                finish = finish.leftJoin(partyDataSet).on("person", "person").select("person", "tdkw_position", "tdkw_postlevel", "tdkw_joincomdate", "tdkw_company", "tdkw_adminorg", "tdkw_partyorg", "tdkw_partyaffairs", "tdkw_ermanfileid").finish();
                finish = finish.where("tdkw_partyorg is not null");
                break;
            case "B":
                // 连接任职信息维护-工会
                finish = finish.leftJoin(gtjExperienceDataSet).on("person", "tdkw_dgtjperson").select("person", "tdkw_position", "tdkw_postlevel", "tdkw_joincomdate", "tdkw_company", "tdkw_adminorg", "tdkw_dgtjorg.tdkw_basedatafield.name", "tdkw_ermanfileid").finish();
                finish = finish.where("tdkw_dgtjorg.tdkw_basedatafield.name = '工'");
                break;
            case "C":
                // 连接任职信息维护-团委
                finish = finish.leftJoin(gtjExperienceDataSet).on("person", "tdkw_dgtjperson").select("person", "tdkw_position", "tdkw_postlevel", "tdkw_joincomdate", "tdkw_company", "tdkw_adminorg", "tdkw_dgtjorg.tdkw_basedatafield.name", "tdkw_ermanfileid").finish();
                finish = finish.where("tdkw_dgtjorg.tdkw_basedatafield.name = '团'");
                break;
            case "D":
                // 连接任职信息维护-纪检委
                finish = finish.leftJoin(gtjExperienceDataSet).on("person", "tdkw_dgtjperson").select("person", "tdkw_position", "tdkw_postlevel", "tdkw_joincomdate", "tdkw_company", "tdkw_adminorg", "tdkw_dgtjorg.tdkw_basedatafield.name", "tdkw_ermanfileid").finish();
                finish = finish.where("tdkw_dgtjorg.tdkw_basedatafield.name = '纪'");
                break;
            case "E":
                // 连接其他任职信息 经营班子
                finish = finish.leftJoin(otherEmployInfoDataSet).on("person", "person").select("person", "tdkw_position", "tdkw_postlevel", "tdkw_joincomdate", "tdkw_company", "tdkw_adminorg", "tdkw_post.number", "tdkw_ermanfileid").finish();
                finish = finish.where("tdkw_post.number in ('GGZW03','GGZW04','GGZW05')");
                break;
            case "F":
                // 连接其他任职信息 财务负责人
                finish = finish.leftJoin(otherEmployInfoDataSet).on("person", "person").select("person", "tdkw_position", "tdkw_postlevel", "tdkw_joincomdate", "tdkw_company", "tdkw_adminorg", "tdkw_post.number", "tdkw_ermanfileid").finish();
                finish = finish.where("tdkw_post.number in ('GGZW10')");
                break;
            case "G":
                // 连接其他任职信息 董监事
                finish = finish.leftJoin(otherEmployInfoDataSet).on("person", "person").select("person", "tdkw_position", "tdkw_postlevel", "tdkw_joincomdate", "tdkw_company", "tdkw_adminorg", "tdkw_post.number", "tdkw_ermanfileid").finish();
                finish = finish.where("tdkw_post.number in ('GGZW01','GGZW02','GGZW06','GGZW07','GGZW08','GGZW09','GGZW11')");
                break;
            default:
                break;
        }
        long end = System.currentTimeMillis();
        logger.info("本月入职报表耗时 = " + (end - start) + "毫秒");
        return finish;
    }

    /**
     * 根据表头构建过滤条件
     */
    private HashMap<String, Object> getTopFilter(ReportQueryParam reportQueryParam) {
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        QFilter erManFileFilter = new QFilter("1", QCP.equals, 1);
        erManFileFilter.and("businessstatus", QCP.equals, "1");
        erManFileFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFileFilter.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFileFilter.and("empposrel.datastatus", QCP.equals, "1");
        erManFileFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        QFilter flowFilter = new QFilter("1", QCP.equals, 1);
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        pernontsprQfilter.and("iscurrentversion", QCP.equals, "1");
        pernontsprQfilter.and("datastatus", QCP.equals, "1");
        map.put("pernontsprQfilter", pernontsprQfilter);
        map.put("flowFilter", flowFilter);
        map.put("erManFileFilter", erManFileFilter);
        map.put("workType", "-1");
        Pair<Date, Date> pair = getTime();
        if (null == reportQueryParam) {
            QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
            DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
            List<Object> ids = Arrays.stream(load).map(DataEntityBase::getPkValue).collect(Collectors.toList());
            erManFileFilter.and("empposrel.tdkw_employtype", QCP.in, ids);
            flowFilter.and("flowtime", QCP.large_equals, pair.getKey());
            flowFilter.and("flowtime", QCP.less_equals, pair.getValue());
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
            flowFilter.and("flowtime", QCP.large_equals, pair.getKey());
            flowFilter.and("flowtime", QCP.less_equals, pair.getValue());
        } else {
            for (FilterItemInfo filterItem : filterItems) {
                String propName = filterItem.getPropName();
                Object value = filterItem.getValue();
                switch (propName) {
                    // 所属组织 基础资料
                    case "tdkw_companyfilter":
                        if (ObjectUtils.isNotEmpty(value)) {
                            DynamicObject org = (DynamicObject) value;
                            List<Long> orgId = HRRoleAndPersonUtils.getHROrgIds(String.valueOf(org.getLong("id")));

                            QFilter orgFilter = new QFilter("adminorg.company", QCP.in, orgId).or("adminorg", QCP.in, orgId);
                            flowFilter.and(orgFilter);
                        }
                        break;
                    // 岗位层级  枚举
                    case "tdkw_postlevefilter":
                        if (ObjectUtils.isNotEmpty(value)) {
                            erManFileFilter.and("empposrel.tdkw_postlevel", QCP.equals, value);
                        }
                        break;
                    // 日期范围 流入流出表
                    case "tdkw_begindate":
                        if (ObjectUtils.isNotEmpty(value)) {
                            flowFilter.and("flowtime", QCP.large_equals, value);
                        }
                        break;
                    // 日期范围  流入流出表
                    case "tdkw_enddate":
                        if (ObjectUtils.isNotEmpty(value)) {
                            flowFilter.and("flowtime", QCP.less_equals, value);
                        }
                        break;
                    // 人员类型 基础资料 流入流出表 laborreltype
                    case "tdkw_laborreltype":
                        if (ObjectUtils.isNotEmpty(value)) {
                            DynamicObjectCollection userType = (DynamicObjectCollection) value;
                            List<Long> ids = userType.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                            erManFileFilter.and("empposrel.tdkw_employtype", QCP.in, ids);
                        }
                        break;
                    // 任职类型 枚举  单独处理
                    case "tdkw_postype":
                        if (ObjectUtils.isNotEmpty(value)) {
                            map.put("workType", value);
                        }
                        break;
                    // 姓名
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
                                        System.out.println();
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
                }
            }
        }
        map.put("pernontsprQfilter", pernontsprQfilter);
        map.put("flowFilter", flowFilter);
        map.put("erManFileFilter", erManFileFilter);
        return map;
    }

    public String getThisMonthIn(String algoKey) {
        // TODO 缺少字段报错  临时处理写死
/*        DataSet dataSet = getDataSet(algoKey, null);
        int size = ORM.create().toPlainDynamicObjectCollection(dataSet.copy()).size();
        return String.valueOf(size);
 */
        return "10";
    }

    private QFilter getPersonIds() {
        // 查询流入流出表,获取本月入职人员
        // 控权组组织过滤,过滤当前组织
        QFilter qFilter = new QFilter("1", QCP.equals, 1);
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), ROLE_ORG);// 如果角色控权包含10000L的话 不对权限进行控制
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            // 获取权限范围组织下所有人员id
            qFilter.and("adminorghis", QCP.in, hasPerOrg);
        }
        return qFilter;
    }

    private Pair<Date, Date> getTime() {
        // 获取本月月初到今天
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date startTime = calendar.getTime();
        return new Pair<>(startTime, endTime);
    }
}