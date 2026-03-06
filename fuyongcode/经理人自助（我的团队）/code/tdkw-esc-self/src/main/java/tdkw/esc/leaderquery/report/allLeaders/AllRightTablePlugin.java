package tdkw.esc.leaderquery.report.allLeaders;

import com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.Row;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AllRightTablePlugin extends AbstractReportListDataPlugin {
    private static final Log logger = LogFactory.getLog(AllRightTablePlugin.class);
    //todo 假实体待建
    private static final String entryNumber = "tdkw_allexecutive_pc";

    private static final Integer BATCH_COUNT = 200;

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        /**
         * 字段取值：
         * 公司：人事业务档案-员工任职id-任职经历-所属公司-行政组织名称
         * 姓名：人事业务档案-姓名
         * 岗位：人事业务档案-员工任职id-任职经历-岗位-岗位名称
         * 岗位层级：人事业务档案-任职经历-岗位层级
         * 组织层级：人事业务档案-任职经历-行政组织层级
         * 人员类别：人事业务档案-任职经历-用工关系类型-名称
         *
         * 性别：人员非时序性属性-性别-名称
         * 籍贯：人员非时序性属性-籍贯-详细地址
         * 出生日期：人员非时序性属性-出生日期
         * 年龄：人员非时序性属性-年龄
         *
         * 婚姻状况：人员时序性属性-婚姻状况-名称
         *
         * 学历：教育经历-学历-名称；取最高学历
         *
         * 政治面貌：基本信息补充-政治面貌-名称
         *
         *
         * 社会工龄：服务年限-社会工龄
         * 集团司龄：服务年限-集团服务年限
         * 公司司龄：服务年限-公司服务年限
         */
        long start = System.currentTimeMillis();
        DataSet dataSet = queryBatchData(reportQueryParam);
        long end = System.currentTimeMillis();
        logger.info("全部高管报表query查询耗时:" + (end - start) + "毫秒");
        return dataSet;
    }

    private DataSet queryBatchData(ReportQueryParam reportQueryParam) {
        //组织过滤
        QFilter orgQfilter = new QFilter("1", QCP.equals, 1);
        //左树过滤
        QFilter leftTreeFilter = new QFilter("1", QCP.equals, 1);
        //人事业务过滤
        QFilter erManFileQfilter = new QFilter("1", QCP.equals, 1);
        //岗位层级过滤
        QFilter levelQfilter = new QFilter("1", QCP.equals, 1);
        // QFilter depEmpQfilter = new QFilter("1", QCP.equals, 1);
        //教育经历过滤
        QFilter eduQfilter = new QFilter("1", QCP.equals, 1);
        //人员时序性属性过滤
        QFilter perTsPropQfilter = new QFilter("1", QCP.equals, 1);
        //服务年限过滤
        QFilter perserlenQfilter = new QFilter("1", QCP.equals, 1);
        //人员非时序性属性过滤
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        //基本信息补充过滤
        QFilter politicalQfilter = new QFilter("1", QCP.equals, 1);
        //query查询时过滤
        QFilter erManFileFilter = new QFilter("1", QCP.equals, 1);

        String algoKey = this.getClass().getName();
        versionControl(eduQfilter, perTsPropQfilter, perserlenQfilter, pernontsprQfilter, politicalQfilter, erManFileQfilter);

        //控权
        orgQfilter = getHasRightOrg(orgQfilter);
        //左树
        if (null != reportQueryParam.getCustomParam().get("orgIds")) {
            //左树点击组织时对右表进行过滤
            List orgIds = (List) reportQueryParam.getCustomParam().get("orgIds");
            logger.info("左树的过滤组织:" + Arrays.toString(new List[]{orgIds}));
            if (!orgIds.contains(100000L)) {
                if( orgIds.size() == 1 ){
                    List hrOrgIds = HRRoleAndPersonUtils.getHROrgIds(orgIds);
                    leftTreeFilter = leftTreeFilter.and("empposrel.adminorg.company", QCP.in, reportQueryParam.getCustomParam().get("orgIds")).or("empposrel.adminorg", QCP.in, hrOrgIds);
                    orgQfilter = orgQfilter.and(leftTreeFilter);
                }else {
                    leftTreeFilter = leftTreeFilter.and("empposrel.adminorg.company", QCP.in, reportQueryParam.getCustomParam().get("orgIds")).or("empposrel.adminorg", QCP.in, reportQueryParam.getCustomParam().get("orgIds"));
                    orgQfilter = orgQfilter.and(leftTreeFilter);
                }
            }
        }
        //岗位层级=其他高管&中层
        levelQfilter.and("empposrel.tdkw_postlevel", QCP.equals, "1");
        levelQfilter.or("empposrel.tdkw_postlevel", QCP.equals, "2");


        //查询人事业务档案 姓名,人员id,任职经历-所属公司,任职经历-所属部门,任职经历-岗位,任职经历-岗位层级,任职经历-开始日期
        DataSet ermanfileDataSet = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", "number as tdkw_number,id as ermanfileid,name,person.id,empposrel.company.name as company,empposrel.company.adminorglayer.name as companylayer,empposrel.adminorg.name as adminorg,empposrel.position.name as position,empposrel.startdate startdate", new QFilter[]{orgQfilter}, null);
       return ermanfileDataSet;
       /*
        //personFlow.addField("case when depemp.position.tdkw_displayvalue = '' then depemp.position.name else depemp.position.tdkw_displayvalue end as depemp.position.name", "depemp.position.name");
        DynamicObjectCollection ermanfileCollection = ORM.create().toPlainDynamicObjectCollection(ermanfileDataSet.copy());
        //拿出人员
        List<Long> personIds = ermanfileCollection.stream().map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter personQfilter = new QFilter("person", QCP.in, personIds);
        logger.info("人事业务档案查出的人员为:" + Arrays.toString(new List[]{personIds}));
        logger.info("人事业务档案查出的人员数为:" + personIds.size());

        //查询任职经历
        //DataSet hrpiEmpposorgrelDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_empposorgrel", "person.id,company.name,adminorg.name,position.name,tdkw_postlevel,startdate", new QFilter[]{personQfilter, perTsPropQfilter}, null);
        //查询服务年限
        DataSet hrpiPerserlenDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perserlen", "person.id,tdkw_executivesdate,socialworkage,comsercount,tdkw_newjointime", new QFilter[]{personQfilter, perserlenQfilter}, null);
        //查询人员非时序性属性
        DataSet hrpiPernontspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pernontsprop", "person.id,gender.name as gender,tdkw_origin.tdkw_address as address,birthday,age,tdkw_full_pinyin_name as pinyin", new QFilter[]{personQfilter, pernontsprQfilter}, null);
        //查询人员时序性属性
        DataSet hrpiPertspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pertsprop", "person.id,marriagestatus.name as marriagestatus", new QFilter[]{personQfilter, perTsPropQfilter}, null);
        //查询基本信息补充
        DataSet baseDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perregion", "person.id,politicalstatus.name as politicalstatus", new QFilter[]{personQfilter, politicalQfilter}, null);
        //查询教育经历
        DataSet eduDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pereduexp", "person.id,education.name education", new QFilter[]{personQfilter, eduQfilter}, null);

        //todo 人事业务档案内连接教育经历
        DataSet finish = ermanfileDataSet.leftJoin(eduDataSet).on("person.id", "person.id").select("tdkw_number", "personid", "ermanfileid", "name", "person.id", "company", "companylayer", "adminorg", "position", "level", "startdate", "education", "tdkw_index").finish();
        //todo 内连接人员时序性属性
        finish = finish.leftJoin(hrpiPertspropDataSet).on("person.id", "person.id").select("tdkw_number", "personid", "ermanfileid", "name", "person.id", "company", "companylayer", "adminorg", "position", "level", "startdate", "education", "marriagestatus", "tdkw_index").finish();
        //todo 内连接服务年限
        finish = finish.leftJoin(hrpiPerserlenDataSet).on("person.id", "person.id").select("tdkw_number", "personid", "ermanfileid", "name", "person.id", "company", "companylayer", "adminorg", "position", "level", "startdate", "education", "marriagestatus", "tdkw_executivesdate", "socialworkage", "comsercount", "tdkw_newjointime", "tdkw_index").finish();
        //finish = finish.leftJoin(hrpiPerserlenDataSet).on("person.id", "person.id").select("tdkw_number","personid", "ermanfileid", "name", "person.id", "company", "companylayer", "adminorg", "position", "level", "startdate", "education", "marriagestatus", "tdkw_executivesdate", "socialworkage", "comsercount", "tdkw_newjointime", "tdkw_index").finish();
        //todo 内连接人员非时序性属性
        finish = finish.leftJoin(hrpiPernontspropDataSet).on("person.id", "person.id").select("tdkw_number", "personid", "ermanfileid", "name", "person.id", "company", "companylayer", "adminorg", "position", "level", "startdate", "education", "marriagestatus", "tdkw_executivesdate", "socialworkage", "comsercount", "tdkw_newjointime", "gender", "address", "birthday", "age", "tdkw_index", "pinyin").finish();
        //finish = finish.leftJoin(hrpiPernontspropDataSet).on("person.id", "person.id").select("tdkw_number","personid", "ermanfileid", "name", "person.id", "company", "companylayer", "adminorg", "position", "level", "startdate", "education", "marriagestatus", "tdkw_executivesdate", "socialworkage", "comsercount","tdkw_newjointime", "gender", "address", "birthday", "age", "tdkw_index").finish();
        //todo 内连接基本信息
        finish = finish.leftJoin(baseDataSet).on("person.id", "person.id").select("tdkw_number", "personid", "ermanfileid", "name as tdkw_name", "company as tdkw_company", "companylayer as tdkw_companylayer", "adminorg as tdkw_adminorg", "position as tdkw_position", "level as tdkw_level", "startdate as tdkw_startdate", "education as tdkw_education", "marriagestatus as tdkw_marriagestatus", "tdkw_executivesdate", "socialworkage as tdkw_socialworkage", "comsercount", "tdkw_newjointime as tdkw_comsercount", "gender as tdkw_gender", "address as tdkw_address", "birthday as tdkw_birthday", "age as tdkw_age", "politicalstatus as tdkw_politicalstatus", "tdkw_index", "pinyin").finish().orderBy(new String[]{"tdkw_index"});
        //finish = finish.leftJoin(baseDataSet).on("person.id", "person.id").select("tdkw_number","personid", "ermanfileid", "name as tdkw_name", "company as tdkw_company", "companylayer as tdkw_companylayer", "adminorg as tdkw_adminorg", "position as tdkw_position", "level as tdkw_level", "startdate as tdkw_startdate", "education as tdkw_education", "marriagestatus as tdkw_marriagestatus", "tdkw_executivesdate", "socialworkage as tdkw_socialworkage", "comsercount","tdkw_newjointime as tdkw_comsercount", "gender as tdkw_gender", "address as tdkw_address", "birthday as tdkw_birthday", "age as tdkw_age", "politicalstatus as tdkw_politicalstatus", "tdkw_index").finish().orderBy(new String[]{"tdkw_index"});

        Object searchName = reportQueryParam.getCustomParam().get("searchName");
//        if (searchName != null) {
//            if (StringUtils.isNotBlank((CharSequence) searchName)){
//                finish = finish.filter(new FilterFunction() {
//                    @Override
//                    public boolean test(Row row) {
//                        String name = row.getString("tdkw_name");
//                        return name.contains((CharSequence) searchName);
//                    }
//                });
//            }
//        }
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
        finish = conditionalQuery(reportQueryParam, finish);
        return finish;*/
    }

    private DataSet conditionalQuery(ReportQueryParam reportQueryParam, DataSet finish) {
        List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            HashMap<String, Object> param = Maps.newHashMapWithExpectedSize(1);
            switch (propName) {
                //todo 人事业务档案
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
                //todo 任职经历
                case "tdkw_position_filter":
                    //岗位
                    finish = finish.filter(new FilterFunction() {
                        @Override
                        public boolean test(Row row) {
                            String position = row.getString("tdkw_position");
                            return position.contains((CharSequence) value);
                        }
                    });
                    logger.info("查询岗位:" + value);
                    break;
                case "tdkw_postlevel_filter":

                    //岗位层级
                case "tdkw_positionlevel":
                    if (StringUtils.isNotBlank((String) value)) {
                        List<String> strings = Arrays.asList("1", "2", "3", "4", "5");
                        ArrayList<Object> valueList = new ArrayList<>();
                        for (String str : strings) {
                            if (((String) value).contains(str)) {
                                valueList.add(str);
                            }
                        }
                        param.put("var", valueList);
                        finish = finish.filter("tdkw_level in var", param);
                    }
                    break;
                //todo 服务年限
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
                case "tdkw_workage":
                    //起始集团司龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_comsercount >= var", param);
//                        perserlenQfilter.and("comsercount", QCP.large_equals, value);
                    }
                    break;
                case "tdkw_workagen":
                    //终止集团司龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_comsercount <= var", param);
//                        perserlenQfilter.and("comsercount", QCP.less_equals, value);
                    }
                    break;
            }
        }
        return finish;
    }


    private QFilter getHasRightOrg(QFilter orgQfilter) {
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), entryNumber);
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        logger.info("领导查询-全部高管报表是否有所有权限:" + hasAllOrgPerm);
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            logger.info("有权限的组织为:" + Arrays.toString(new List[]{hasPerOrg}));
            //根据认识业务档案的员工任职的所属部门进行控权
            orgQfilter.and("empposrel.adminorg", QCP.in, hasPerOrg);
        }
        return orgQfilter;
    }

    private static void versionControl(QFilter eduQfilter, QFilter perTsPropQfilter, QFilter perserlenQfilter, QFilter pernontsprQfilter, QFilter politicalQfilter, QFilter erManFileQfilter) {
        //教育经历
        eduQfilter.and("iscurrentversion", QCP.equals, "1");
        eduQfilter.and("datastatus", QCP.equals, "1");
        eduQfilter.and("tdkw_ishighestcheck", QCP.equals, "1");
        // 人员时序性属性
        perTsPropQfilter.and("iscurrentversion", QCP.equals, "1");
        perTsPropQfilter.and("datastatus", QCP.equals, "1");
        // 服务年限
        perserlenQfilter.and("iscurrentversion", QCP.equals, "1");
        perserlenQfilter.and("datastatus", QCP.equals, "1");
        // 人员非时序性属性
        pernontsprQfilter.and("iscurrentversion", QCP.equals, "1");
        pernontsprQfilter.and("datastatus", QCP.equals, "1");
        // 基本信息补充
        politicalQfilter.and("iscurrentversion", QCP.equals, "1");
        politicalQfilter.and("datastatus", QCP.equals, "1");
        // 人事业务档案
        erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
        //erManFileQfilter.and("datastatus", QCP.equals,"1");
        erManFileQfilter.and("businessstatus", QCP.equals, "1");
        //全职任职
        // erManFileQfilter.and("empposrel.postype.number", QCP.equals, "XY00001");
        //erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFileQfilter.and("empposrel.datastatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFileQfilter.and("empposrel.businessstatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");

    }
}
