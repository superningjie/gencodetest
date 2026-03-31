package tdkw.esc.leaderquery.report.tradeunion;

import com.google.common.collect.Sets;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.*;

public class TradeUnionRightTablePlugin extends AbstractReportListDataPlugin {
    private static final Log logger = LogFactory.getLog(TradeUnionRightTablePlugin.class);
    //todo 假实体待建
    private static final String entryNumber = "tdkw_tradeunion_pc";
    //一次性展示200条数据
    private static final Integer BATCH_COUNT = 200;


    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        return queryData(reportQueryParam);
    }

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

    /**
     * @param
     * @return
     */
    private DataSet queryData(ReportQueryParam reportQueryParam) {
        //组织过滤
        QFilter orgQfilter = new QFilter("1", QCP.equals, 1);
        //左树过滤
        QFilter leftTreeFilter = new QFilter("1", QCP.equals, 1);
        //人事业务过滤
        QFilter erManFileQfilter = new QFilter("1", QCP.equals, 1);
        //教育经历过滤
        QFilter eduQfilter = new QFilter("1", QCP.equals, 1);
        //人员时序性属性过滤
        QFilter perTsPropQfilter = new QFilter("1", QCP.equals, 1);
        //人员非时序性属性过滤
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        //基本信息补充过滤
        QFilter politicalQfilter = new QFilter("1", QCP.equals, 1);
        //其他任职信息过滤
        QFilter otherEmpFilter = new QFilter("1", QCP.equals, 1);
        //服务年限过滤
        QFilter perserlenQfilter = new QFilter("1", QCP.equals, 1);
        String algoKey = this.getClass().getName();
        versionControl(eduQfilter, perTsPropQfilter, pernontsprQfilter, politicalQfilter, erManFileQfilter, otherEmpFilter, perserlenQfilter);

        //控权
        orgQfilter = getHasRightOrg(orgQfilter);
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
        // TODO 缺少元数据异常
        return null;
        /*
        //查询其他任职信息 1.人员ID 2.人员 3. 任职开始日期 4.任职结束日期 5.任职组织 6.任职职务
        DataSet otherEmpDataSet = QueryServiceHelper.queryDataSet(algoKey, "tdkw_dgtj_experience", "tdkw_dgtjperson" +
                        ".id as personId," + "tdkw_dgtjperson.name as personame,tdkw_dgtjstarttime,tdkw_dgtjendtime," +
                        "tdkw_dgtjorg,tdkw_dgtjjob,id as dgtjId",
                new QFilter[]{otherEmpFilter}, null);
        //将DataSet中的值转化成DYCollection，将dataset中查询的person.id放到LIST中
        DynamicObjectCollection otherEmpCollection = ORM.create().toPlainDynamicObjectCollection(otherEmpDataSet.copy());
        //取出其他任职信息中的员工ID
        List<Long> personIds = otherEmpCollection.stream().map(i -> i.getLong("personId")).collect(Collectors.toList());
        QFilter personQfilter = new QFilter("person", QCP.in, personIds);
        logger.info("其他任职信息查出的人员为:" + Arrays.toString(new List[]{personIds}));
        logger.info("其他任职信息查出的人员数为:" + personIds.size());

        //查询人事业务档案
        DataSet ermanfileDataSet = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", "number as tdkw_number,person.id,empposrel" +
                ".company.name as company,tdkw_index,person.tdkw_pkid,empposrel.adminorg as adminorg ", new QFilter[]{erManFileQfilter, orgQfilter}, null);        //查询服务年限
        //查询人员非时序性属性
        DataSet hrpiPernontspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pernontsprop", "person.id," +
                        "gender.name as gender,birthday,age,servicelen,entservicelen,tdkw_origin.tdkw_address as address,tdkw_full_pinyin_name as pinyin", new QFilter[]{personQfilter,
                        pernontsprQfilter},
                null);
        //查询人员时序性属性
        DataSet hrpiPertspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pertsprop", "person.id,marriagestatus.name as marriagestatus", new QFilter[]{personQfilter, perTsPropQfilter}, null);
        //查询基本信息补充
        DataSet baseDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perregion", "person.id,politicalstatus.name as politicalstatus", new QFilter[]{personQfilter, politicalQfilter}, null);
        //查询教育经历
        DataSet eduDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pereduexp", "person.id,education.name education", new QFilter[]{personQfilter, eduQfilter}, null);

        // 其他任职信息内连接教育经历
        DataSet finish = otherEmpDataSet.leftJoin(eduDataSet).on("personId", "person.id").select("personId", "personame",
                "tdkw_dgtjstarttime", "tdkw_dgtjjob", "tdkw_dgtjorg", "tdkw_dgtjendtime", "education", "dgtjId").finish();
        //查服务年限
        DataSet hrpiPerserlenDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perserlen", "person.id,tdkw_newjointime", new QFilter[]{personQfilter, perserlenQfilter}, null);
        //内连接人员时序性属性
        finish = finish.leftJoin(hrpiPertspropDataSet).on("personId", "person.id").select("personId", "personame",
                "tdkw_dgtjstarttime", "tdkw_dgtjjob", "tdkw_dgtjorg", "tdkw_dgtjendtime", "education", "marriagestatus", "dgtjId"
        ).finish();
        // 内连接人事业务档案
        finish = finish.join(ermanfileDataSet).on("personId", "person.id").select("tdkw_number", "personId", "personame",
                "tdkw_dgtjstarttime", "tdkw_dgtjjob", "tdkw_dgtjorg", "tdkw_dgtjendtime", "education",
                "marriagestatus", "dgtjId", "company", "tdkw_index", "person.tdkw_pkid", "adminorg").finish();
        // 内连接人员非时序性属性
        finish = finish.leftJoin(hrpiPernontspropDataSet).on("personId", "person.id").select("tdkw_number", "personId", "personame",
                "tdkw_dgtjstarttime", "tdkw_dgtjjob", "tdkw_dgtjorg", "tdkw_dgtjendtime", "education", "marriagestatus",
                "company", "tdkw_index", "servicelen", "gender", "birthday", "age", "dgtjId",
                "address", "person.tdkw_pkid", "adminorg", "pinyin").finish();
        finish = finish.leftJoin(hrpiPerserlenDataSet).on("personId", "person.id").select("tdkw_number", "personId", "personame",
                "tdkw_dgtjstarttime", "tdkw_dgtjjob", "tdkw_dgtjorg", "tdkw_dgtjendtime", "education", "marriagestatus",
                "company", "tdkw_index", "servicelen", "gender", "birthday", "age", "dgtjId",
                "address", "person.tdkw_pkid", "adminorg", "tdkw_newjointime", "pinyin").finish();
        // 内连接基本信息
        finish = finish.leftJoin(baseDataSet).on("personId", "person.id").select("tdkw_number", "personame as tdkw_rptname",
                "personId", "company as tdkw_rptcompany", "education as tdkw_rpteducation", "marriagestatus " +
                        "as tdkw_rptmarriege", "servicelen as tdkw_rptworkage"
                , "gender as tdkw_rptgender", "birthday as tdkw_rptbrithday", "age as tdkw_rptage", "politicalstatus " +
                        "as tdkw_rptpolitical", "tdkw_index", "dgtjId", "tdkw_dgtjjob as tdkw_rptdgtjjob",
                "tdkw_dgtjorg as tdkw_rptdgtjorg", "tdkw_dgtjendtime as tdkw_rptworkagend", "tdkw_dgtjstarttime as " +
                        "tdkw_rptworkdate", "address as tdkw_rptaddress", "person.tdkw_pkid as personid", "adminorg", "tdkw_newjointime as tdkw_rptsocialage", "pinyin").finish().orderBy(new String[]{"tdkw_index"});
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
                        String name = row.getString("tdkw_rptname");
                        return name.contains((CharSequence) searchName);
                    }
                });
            }
        }
        List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
        if (filterItems.size() == 0) {
            logger.info("默认过滤任职期内");
            HashMap<String, Object> param = Maps.newHashMapWithExpectedSize(1);
            Date time = new Date();
            param.put("var", time);
            logger.info("当前日期为" + time);
            finish = finish.filter("tdkw_rptworkagend >= var " +
                            "or tdkw_rptworkagend is null",
                    param);
        }
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            HashMap<String, Object> param = Maps.newHashMapWithExpectedSize(1);
            switch (propName) {
                case "tdkw_dgtj_org":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_rptdgtjjob in var", param);
                    }
                    break;
                case "tdkw_name":
                    //姓名
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String name = row.getString("tdkw_rptname");
                                return name.contains((CharSequence) value);
                            }
                        });
                    }
                    break;
                //todo 人员非时序性属性
                case "tdkw_agestart":
                    //起始年龄
                    if ((Integer) value != 0) {
                        param.put("var", value);
                        finish = finish.filter("tdkw_rptage >= var", param);

                    }
                    break;
                case "tdkw_ageend":
                    //终止年龄
                    if ((Integer) value != 0) {
                        param.put("var", value);
                        finish = finish.filter("tdkw_rptage <= var", param);
                    }
                    break;
                case "tdkw_istenure":
                    //是否任职期内
                    if (StringUtils.isNotBlank((String) value)) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
                        Date time = calendar.getTime();
                        param.put("var", time);
                        //默认为任职期内
                        if (StringUtils.equals((CharSequence) value, "1")) {
                            finish = finish.filter("tdkw_rptworkagend >= var " +
                                            "or tdkw_rptworkagend is null",
                                    param);
                            logger.info("默认任职期内");
                        }
                    }
                    break;
            }
        }
        return finish;*/
    }

    private QFilter getHasRightOrg(QFilter orgQfilter) {
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), entryNumber);
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        logger.info("领导查询-工会报表是否有所有权限:" + hasAllOrgPerm);
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            logger.info("有权限的组织为:" + Arrays.toString(new List[]{hasPerOrg}));
            //根据认识业务档案的员工任职的所属部门进行控权
            orgQfilter.and("empposrel.adminorg", QCP.in, hasPerOrg);
        }
        return orgQfilter;
    }

    private static void versionControl(QFilter eduQfilter, QFilter perTsPropQfilter,
                                       QFilter pernontsprQfilter, QFilter politicalQfilter, QFilter erManFileQfilter,
                                       QFilter otherEmpFilter, QFilter perserlenQfilter

    ) {
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
        erManFileQfilter.and("empposrel.datastatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFileQfilter.and("empposrel.businessstatus", QCP.equals, "1");
        //过滤离职、退休、外聘
        erManFileQfilter.and("filetype.number", QCP.not_equals, "1050_S");
        erManFileQfilter.and("filetype.number", QCP.not_equals, "1060_S");
        //全职任职
        erManFileQfilter.and("empposrel.postype.number", QCP.equals, "XY00001");
        erManFileQfilter.and("empposrel.isprimary", QCP.equals, "1");

        //其他任职信息
        otherEmpFilter.and("tdkw_dgtjorg.tdkw_basedatafield.number", QCP.equals, "XY002");
        otherEmpFilter.and("billstatus", QCP.equals, "C");
        // 服务年限
        perserlenQfilter.and("iscurrentversion", QCP.equals, "1");
        perserlenQfilter.and("datastatus", QCP.equals, "1");
    }
}
