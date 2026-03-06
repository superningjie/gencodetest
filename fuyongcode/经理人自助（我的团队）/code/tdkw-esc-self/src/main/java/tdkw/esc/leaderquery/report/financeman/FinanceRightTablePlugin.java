package tdkw.esc.leaderquery.report.financeman;

import com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FinanceRightTablePlugin extends AbstractReportListDataPlugin {
    private static final Log logger = LogFactory.getLog(FinanceRightTablePlugin.class);
    //todo 假实体待建
    private static final String entryNumber = "tdkw_financeperson_pc";

    private static final Integer BATCH_COUNT = 200;

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        long start = System.currentTimeMillis();
        DataSet dataSet = queryBatchData(reportQueryParam);
        long end = System.currentTimeMillis();
        logger.info("财务负责人报表query查询耗时:" + (end - start) + "毫秒");
        return dataSet;
    }

    private DataSet queryBatchData(ReportQueryParam reportQueryParam) {
        //组织过滤
        QFilter orgQfilter = new QFilter("1", QCP.equals, 1);
        //左树过滤
        QFilter leftTreeFilter = new QFilter("1", QCP.equals, 1);
        //其他任职信息过滤
        QFilter financeQFilter = new QFilter("1", QCP.equals, 1);
        //人事业务过滤
        QFilter erManFileQfilter = new QFilter("1", QCP.equals, 1);
        // QFilter depEmpQfilter = new QFilter("1", QCP.equals, 1);
        //教育经历过滤
        QFilter eduQfilter = new QFilter("1", QCP.equals, 1);
        //人员时序性属性过滤
        QFilter perTsPropQfilter = new QFilter("1", QCP.equals, 1);
        //人员非时序性属性过滤
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        //基本信息补充过滤
        QFilter politicalQfilter = new QFilter("1", QCP.equals, 1);
        //query查询时过滤
        QFilter queryQFilter = new QFilter("1", QCP.equals, 1);
        //特殊要求过滤
        QFilter specialQFilter = new QFilter("1", QCP.equals, 1);
        //服务年限过滤
        QFilter perserlenQfilter = new QFilter("1", QCP.equals, 1);

        String algoKey = this.getClass().getName();
        versionControl(eduQfilter, perTsPropQfilter, pernontsprQfilter, politicalQfilter, erManFileQfilter, financeQFilter, perserlenQfilter);
        orgQfilter = getHasRightOrg(orgQfilter);
        //当查询为queryBatchBy时，需要进行控权和左树过滤处理，以及岗位层级的特殊过滤
        //左树
        if (null != reportQueryParam.getCustomParam().get("orgIds")) {
            //左树点击组织时对右表进行过滤
            List orgIds = (List) reportQueryParam.getCustomParam().get("orgIds");
            logger.info("左树的过滤组织:" + Arrays.toString(new List[]{orgIds}));
            if (!orgIds.contains(100000L)) {
                if (orgIds.size() == 1) {
                    List hrOrgIds = HRRoleAndPersonUtils.getHROrgIds(orgIds);
                    leftTreeFilter = leftTreeFilter.and("empposrel.adminorg.company", QCP.in, reportQueryParam.getCustomParam().get("orgIds")).or("empposrel.adminorg", QCP.in, hrOrgIds);
                    orgQfilter = orgQfilter.and(leftTreeFilter);
                } else {
                    leftTreeFilter = leftTreeFilter.and("empposrel.adminorg.company", QCP.in, reportQueryParam.getCustomParam().get("orgIds")).or("empposrel.adminorg", QCP.in, reportQueryParam.getCustomParam().get("orgIds"));
                    orgQfilter = orgQfilter.and(leftTreeFilter);
                }
            }
        }
        //其他任职信息查询的字段 法人公司,姓名，职位，职位类型，开始日期，结束日期，任期/届数
        String financeData = "id as financeid,person.id,person.tdkw_pkid as personid,tdkw_company as tdkw_legalcompany,person.name tdkw_name,tdkw_post,tdkw_poststype,tdkw_startdate,tdkw_enddate,tdkw_tenure";
        //人事业务档案查询的字段 员工任职-所属公司-行政组织名称，员工任职-所属部门-行政组织名称，非时序性属性-性别，非时序性属性-年龄，非时序性属性-社会工龄，非时序性属性-司龄
        String ermanfileData = "number as tdkw_number,person.id,empposrel.company.name as tdkw_company,empposrel.adminorg.name as tdkw_adminorg,tdkw_index";
        //人员非时序性属性查询的字段 籍贯-详细地址，出生日期
        String hrpiPernontspropData = "person.id,tdkw_origin.tdkw_address as tdkw_address,birthday as tdkw_birthday,gender.name as tdkw_gender,age as tdkw_age,servicelen as tdkw_servicelen,entservicelen,tdkw_full_pinyin_name as pinyin";
        //人员时序性属性查询的字段 婚姻状况
        String hrpiPertspropData = "person.id,marriagestatus.name as tdkw_marriagestatus";
        //基本信息查询的字段 政治面貌
        String baseData = "person.id,politicalstatus.name as tdkw_politicalstatus";
        //教育经历查询的字段 学历
        String eduData = "person.id,education.name tdkw_education";
        // TODO 缺少元数据异常
        return null;
        /*//查询其他任职信息
        DataSet financeDataSet = QueryServiceHelper.queryDataSet(algoKey, "tdkw_hrpi_otheremployinf", financeData, new QFilter[]{specialQFilter, financeQFilter, queryQFilter}, null);
        //personFlow.addField("case when depemp.position.tdkw_displayvalue = '' then depemp.position.name else depemp.position.tdkw_displayvalue end as depemp.position.name", "depemp.position.name");
        DynamicObjectCollection attendrecordsCollection = ORM.create().toPlainDynamicObjectCollection(financeDataSet.copy());
        //拿出人员
        List<Long> personIds = attendrecordsCollection.stream().map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter personQfilter = new QFilter("person", QCP.in, personIds);
        logger.info("其他任职信息查出的人员为:" + Arrays.toString(new List[]{personIds}));
        logger.info("其他任职信息查出的人员数为:" + personIds.size());

        //查询人事业务档案 员工任职-所属公司，非时序性属性-性别，非时序性属性-年龄，非时序性属性-社会工龄，非时序性属性-司龄
        DataSet ermanfileDataSet = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", ermanfileData, new QFilter[]{personQfilter, erManFileQfilter, orgQfilter}, null);
        //查询人员非时序性属性
        DataSet hrpiPernontspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pernontsprop", hrpiPernontspropData, new QFilter[]{personQfilter, pernontsprQfilter}, null);
        //查询人员时序性属性
        DataSet hrpiPertspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pertsprop", hrpiPertspropData, new QFilter[]{personQfilter, perTsPropQfilter}, null);
        //查询基本信息补充
        DataSet baseDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perregion", baseData, new QFilter[]{personQfilter, politicalQfilter}, null);
        //查询教育经历
        DataSet eduDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pereduexp", eduData, new QFilter[]{personQfilter, eduQfilter}, null);
        //查服务年限
        DataSet hrpiPerserlenDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perserlen", "person.id,tdkw_newjointime", new QFilter[]{personQfilter, perserlenQfilter}, null);
        //todo 其他任职信息左连接教育经历
        DataSet finish = financeDataSet.leftJoin(eduDataSet).on("person.id", "person.id").select("financeid", "person.id", "personid", "tdkw_legalcompany", "tdkw_name", "tdkw_post", "tdkw_poststype", "tdkw_startdate", "tdkw_enddate", "tdkw_tenure", "tdkw_education").finish();
        //todo 左连接人员时序性属性
        finish = finish.leftJoin(hrpiPertspropDataSet).on("person.id", "person.id").select("financeid", "person.id", "personid", "tdkw_legalcompany", "tdkw_name", "tdkw_post", "tdkw_poststype", "tdkw_startdate", "tdkw_enddate", "tdkw_tenure", "tdkw_education", "tdkw_marriagestatus").finish();
        //todo 左连接人员非时序性属性
        finish = finish.leftJoin(hrpiPernontspropDataSet).on("person.id", "person.id").select("financeid", "person.id", "personid", "tdkw_legalcompany", "tdkw_name", "tdkw_post", "tdkw_poststype", "tdkw_startdate", "tdkw_enddate", "tdkw_tenure", "tdkw_education", "tdkw_marriagestatus", "tdkw_address", "tdkw_birthday", "tdkw_gender", "tdkw_age", "tdkw_servicelen", "pinyin").finish();
        //todo 左连接基本信息
        finish = finish.leftJoin(baseDataSet).on("person.id", "person.id").select("financeid", "person.id", "personid", "tdkw_legalcompany", "tdkw_name", "tdkw_post", "tdkw_poststype", "tdkw_startdate", "tdkw_enddate", "tdkw_tenure", "tdkw_education", "tdkw_marriagestatus", "tdkw_address", "tdkw_birthday", "tdkw_politicalstatus", "tdkw_gender", "tdkw_age", "tdkw_servicelen", "pinyin").finish();
        //左连接服务年限
        finish = finish.leftJoin(hrpiPerserlenDataSet).on("person.id", "person.id").select("financeid", "person.id", "personid", "tdkw_legalcompany", "tdkw_name", "tdkw_post", "tdkw_poststype", "tdkw_startdate", "tdkw_enddate", "tdkw_tenure", "tdkw_education", "tdkw_marriagestatus", "tdkw_address", "tdkw_birthday", "tdkw_politicalstatus", "tdkw_gender", "tdkw_age", "tdkw_servicelen", "tdkw_newjointime", "pinyin").finish();
        //todo 内连接人事业务档案
        //finish = finish.join(ermanfileDataSet).on("person.id", "person.id").select("tdkw_number","financeid", "person.id", "personid", "tdkw_legalcompany", "tdkw_name", "tdkw_post", "tdkw_poststype", "tdkw_startdate", "tdkw_enddate", "tdkw_tenure", "tdkw_education", "tdkw_marriagestatus", "tdkw_address", "tdkw_birthday", "tdkw_politicalstatus", "tdkw_company", "tdkw_adminorg", "tdkw_gender", "tdkw_age", "tdkw_servicelen", "tdkw_entservicelen", "tdkw_index").finish().orderBy(new String[]{"tdkw_index"});
        finish = finish.join(ermanfileDataSet).on("person.id", "person.id").select("tdkw_number", "financeid", "person.id", "personid", "tdkw_legalcompany", "tdkw_name", "tdkw_post", "tdkw_poststype", "tdkw_startdate", "tdkw_enddate", "tdkw_tenure", "tdkw_education", "tdkw_marriagestatus", "tdkw_address", "tdkw_birthday", "tdkw_politicalstatus", "tdkw_company", "tdkw_adminorg", "tdkw_gender", "tdkw_age", "tdkw_servicelen", "tdkw_newjointime as tdkw_entservicelen", "tdkw_index", "pinyin").finish().orderBy(new String[]{"tdkw_index"});

        Object searchName = reportQueryParam.getCustomParam().get("searchName");
        // 判断搜索的searchName中是否包含字母、正则
        if (searchName != null && StringUtils.isNotBlank((CharSequence) searchName)) {
            String nameStr = searchName.toString();
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
                finish = finish.where(" pinyin like '%" + pinYinName + "%'");
            } else {
                // 不包含字母
                finish = finish.filter(new FilterFunction() {
                    private static final long serialVersionUID = 4017820239971693392L;

                    @Override
                    public boolean test(Row row) {
                        String name = row.getString("tdkw_name");
                        return name.contains((CharSequence) searchName);
                    }
                });
            }
        }
        //当查询为queryBatchBy时，需要进行条件查询时的过滤
        finish = conditionalQuery(reportQueryParam, finish);
        return finish;*/
    }

    private DataSet conditionalQuery(ReportQueryParam reportQueryParam, DataSet finish) {
        List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
        if (filterItems.size() == 0) {
            logger.info("默认过滤任职期内");
            HashMap<String, Object> param = Maps.newHashMapWithExpectedSize(1);
            Date time = new Date();
            param.put("var", time);
            finish = finish.filter("(var>=tdkw_startdate and var<=tdkw_enddate) or tdkw_enddate is null", param);
            return finish;
        }
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            HashMap<String, Object> param = Maps.newHashMapWithExpectedSize(1);
            switch (propName) {
                //todo 其他任职信息
                case "tdkw_name_filter":
                    //姓名
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String name = row.getString("tdkw_name");
                                return name.contains((CharSequence) value);
                            }
                        });
                        logger.info("查询姓名:" + value);
                        //finish = finish.filter("tdkw_rptname like 'temp'", param);
                        // flowQfilter.and("person.name", QCP.like, "%" + value + "%");
                    }
                    break;
                case "tdkw_position_filter":
                    //职位
                    if (ObjectUtils.isNotEmpty(value)) {
                        long id = ((DynamicObject) value).getLong("id");
                        param.put("var", id);
                        finish = finish.filter("tdkw_post = var", param);
                    }
                    logger.info("查询职位:" + value);
                    break;
                //todo 人员非时限属性
                case "tdkw_agestart":
                    //起始年龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_age >= var", param);
                        //  perserlenQfilter.and("socialworkage", QCP.large_equals, value);
                    }
                    break;
                case "tdkw_ageend":
                    //终止年龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_age <= var", param);
                        // perserlenQfilter.and("socialworkage", QCP.less_equals, value);
                    }
                    break;
                case "tdkw_istenure":
                    //是否任期内
                    if ("1".equals(value)) {
                        Date date = new Date();
                        param.put("var", date);
                        finish = finish.filter("(var>=tdkw_startdate and var<=tdkw_enddate) or tdkw_enddate is null", param);
                    }
                    logger.info("查询是否任期内:" + value);
                    break;
            }
        }
        return finish;
    }


    private QFilter getHasRightOrg(QFilter orgQfilter) {
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), entryNumber);
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        logger.info("领导查询-财务负责人报表是否有所有权限:" + hasAllOrgPerm);
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            logger.info("有权限的组织为:" + Arrays.toString(new List[]{hasPerOrg}));
            //根据认识业务档案的员工任职的所属部门进行控权
            orgQfilter.and("empposrel.adminorg", QCP.in, hasPerOrg);
        }
        return orgQfilter;
    }

    //eduQfilter, perTsPropQfilter, pernontsprQfilter, politicalQfilter, erManFileQfilter,attendrecordsFilter
    private static void versionControl(QFilter eduQfilter, QFilter perTsPropQfilter, QFilter pernontsprQfilter, QFilter politicalQfilter, QFilter erManFileQfilter, QFilter financeQFilter, QFilter perserlenQfilter) {
        //教育经历
        eduQfilter.and("iscurrentversion", QCP.equals, "1");
        eduQfilter.and("datastatus", QCP.equals, "1");
        eduQfilter.and("tdkw_ishighestcheck", QCP.equals, "1");
        // 人员时序性属性
        perTsPropQfilter.and("iscurrentversion", QCP.equals, "1");
        perTsPropQfilter.and("datastatus", QCP.equals, "1");
        // 人员非时序性属性
        pernontsprQfilter.and("iscurrentversion", QCP.equals, "1");
        pernontsprQfilter.and("datastatus", QCP.equals, "1");
        // 基本信息补充
        politicalQfilter.and("iscurrentversion", QCP.equals, "1");
        politicalQfilter.and("datastatus", QCP.equals, "1");
        // 人事业务档案
        erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
        erManFileQfilter.and("datastatus", QCP.equals, "1");
        erManFileQfilter.and("businessstatus", QCP.equals, "1");
        //主任职过滤
        erManFileQfilter.and("empposrel.isprimary", QCP.equals, "1");
        erManFileQfilter.and("empposrel.postype.number", QCP.equals, "XY00001");

        erManFileQfilter.and("empposrel.datastatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFileQfilter.and("empposrel.businessstatus", QCP.equals, "1");
        //离职、退休、外聘过滤
        erManFileQfilter.and("filetype.number", QCP.not_equals, "1050_S");
        erManFileQfilter.and("filetype.number", QCP.not_equals, "1060_S");
        //其他任职信息
        financeQFilter.and("iscurrentversion", QCP.equals, "1");
        financeQFilter.and("datastatus", QCP.equals, "1");
        financeQFilter.and("tdkw_post.number", QCP.equals, "GGZW10");
        financeQFilter.and("tdkw_company", QCP.not_equals, null);
        // 服务年限
        perserlenQfilter.and("iscurrentversion", QCP.equals, "1");
        perserlenQfilter.and("datastatus", QCP.equals, "1");
    }
}
