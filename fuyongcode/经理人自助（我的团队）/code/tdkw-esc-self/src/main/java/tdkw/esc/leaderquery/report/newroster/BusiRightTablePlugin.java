package tdkw.esc.leaderquery.report.newroster;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.GroupbyDataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRStructOrgUtils;
import tdkw.esc.leaderquery.common.PinyinUtil;
import tdkw.esc.leaderquery.report.newroster.RightTablePlugin;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RightTablePlugin
 *
 * @author xxx
 * @date 2023/9/25
 */
public class BusiRightTablePlugin extends AbstractReportListDataPlugin {

    private static final Log logger = LogFactory.getLog(RightTablePlugin.class);
    //todo 假实体待建
    private static final String entryNumber = "tdkw_divisionroster_pc";

    private static final Integer BATCH_COUNT = 200;

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        /**
         * 字段取值：
         * 公司：人事业务档案-员工任职id-任职经历-所属公司-行政组织名称
         * 姓名：人事业务档案-姓名
         * 岗位：人事业务档案-员工任职id-任职经历-岗位-岗位名称
         * 岗位层级：人事业务档案-任职经历-岗位层级
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
         * 集团司龄：服务年限-本加入集团司龄
         * 公司司龄：服务年限-公司服务年限
         */
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

    private DataSet queryData(ReportQueryParam reportQueryParam) {
        String algoKey = this.getClass().getName();
        ORM orm = ORM.create();
        QFilter eduQfilter = new QFilter("1", QCP.equals, 1);
        QFilter perTsPropQfilter = new QFilter("1", QCP.equals, 1);
        QFilter perserlenQfilter = new QFilter("1", QCP.equals, 1);
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        QFilter politicalQfilter = new QFilter("1", QCP.equals, 1);
        QFilter collegeTypeQfilter = new QFilter("1", QCP.equals, 1);
        QFilter erManFileQfilter = new QFilter("1", QCP.equals, 1);
        QFilter empposorgrelQfilter = new QFilter("1", QCP.equals, 1);
//        empposorgrelQfilter.and("tdkw_changereason.number", QCP.not_equals, "XY00017");
        empposorgrelQfilter.and("initstatus", QCP.equals, "2");
        empposorgrelQfilter.and("iscurrentversion", QCP.equals, "1");
        empposorgrelQfilter.and("datastatus", QCP.not_equals, "-1");
        empposorgrelQfilter.and("isprimary",QCP.equals,"1");
        QFilter historyFilter = new QFilter("1", QCP.equals, 1);
        versionControl(eduQfilter, perTsPropQfilter, perserlenQfilter, pernontsprQfilter, politicalQfilter, collegeTypeQfilter, erManFileQfilter);
//        getHasRightOrg(empposorgrelQfilter);
        //左树
        if (null != reportQueryParam.getCustomParam().get("orgIds")) {
            //左树点击组织时对右表进行过滤
            List<Long> orgIds = (List<Long>) reportQueryParam.getCustomParam().get("orgIds");
            orgIds = orgIds.stream().distinct().collect(Collectors.toList());
            logger.info("左树的过滤组织:" + Arrays.toString(new List[]{orgIds}));
            logger.info("左树的过滤组织:" + orgIds.size());
            if (!orgIds.contains(100000L)) {
                if (orgIds.size() == 1) {
                    List<Long> hrOrgIds = HRStructOrgUtils.getStructOrgIds(orgIds.get(0).toString(),9999);
                    historyFilter = historyFilter.and("adminorg.company", QCP.in, hrOrgIds).or("adminorg", QCP.in, hrOrgIds);
                    empposorgrelQfilter = empposorgrelQfilter.and(historyFilter);
                } else {
                    historyFilter = historyFilter.and("adminorg.company", QCP.in, orgIds).or("adminorg", QCP.in, orgIds);
                    empposorgrelQfilter = empposorgrelQfilter.and(historyFilter);
                }
            }else {
                // 2025-10-31 增加报表过滤，当前人组织
                //获取当前登录人员所属组织
                List<Long> userIds = new ArrayList<>(1);
                userIds.add(UserServiceHelper.getCurrentUserId());
                // 获取当前登录人员所属公司
                String adminOrg = String.valueOf(UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId()));

                empposorgrelQfilter = empposorgrelQfilter.and("adminorg.company", QCP.in, Long.valueOf(adminOrg));
            }
        }
        //查询人事业务档案
        DataSet erManFileDataSet = orm.queryDataSet(algoKey, "hspm_ermanfile", "id as ermanfileid,person as tdkw_personrpt,person.id as tdkw_personid,person.name,person.number", new QFilter[]{erManFileQfilter}, null);
//        return erManFileDataSet;
        DynamicObjectCollection erManFileData = QueryServiceHelper.query("hspm_ermanfile", "person.id", new QFilter[]{erManFileQfilter});
        List<Long> personIds = erManFileData.stream().map(dynamicObject -> dynamicObject.getLong("person.id")).collect(Collectors.toList());
        //List<Long> personIds = orm.toPlainDynamicObjectCollection(erManFileDataSet.copy()).stream().map(i -> i.getLong("tdkw_personid")).collect(Collectors.toList());
        QFilter personQfilter = new QFilter("person", QCP.in, personIds);
        FilterItemInfo tdkw_deadtline = reportQueryParam.getFilter().getFilterItem("tdkw_deadtline");
        if( ObjectUtils.isNotEmpty(tdkw_deadtline) ){
            Date date = tdkw_deadtline.getDate();
            QFilter qf = new QFilter("startdate",QCP.less_equals,date);
            qf.and("sysenddate",QCP.large_equals,date);
            empposorgrelQfilter.and(qf);
            logger.info("tdkw_deadtline" + date);
        }
        DataSet hrpi_empposorgrel = orm.queryDataSet(algoKey, "hrpi_empposorgrel", "company as tdkw_companyrpt,person.number,position as tdkw_positionrpt," +
                "adminorg.sortcode as sortcode,isprimary," +
                "position.name as newname,startdate,sysenddate as enddate,adminorg as tdkw_adminrpt,postype as tdkw_postype,number as tdkw_versionnumber", new QFilter[]{empposorgrelQfilter}, null);
        logger.info("empposorgrelQfilter" + empposorgrelQfilter.toString());
        erManFileDataSet = hrpi_empposorgrel.leftJoin(erManFileDataSet).on("person.number", "person.number").select("tdkw_personid","ermanfileid", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "sortcode", "isprimary", "newname", "startdate", "enddate", "person.name", "tdkw_adminrpt", "tdkw_postype", "tdkw_versionnumber").finish();

        List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
        boolean flag = false;
        List<Object> invalidValues = Arrays.asList("-", "", null);
        String filterSchool = null;
        String filtermajor = null;
        for (FilterItemInfo filterItem : filterItems) {
            if ("tdkw_school".equals(filterItem.getPropName())) {
                if (!invalidValues.contains(filterItem.getValue())) {
                    flag = true;
                    //过滤条件学校
                    filterSchool = (String) filterItem.getValue();

                }
            }
            if ("tdkw_major".equals(filterItem.getPropName())) {
                if (!invalidValues.contains(filterItem.getValue())) {
                    flag = true;
                    //过滤条件专业
                    filtermajor = (String) filterItem.getValue();
                }
            }
        }
//        //查询人员非时序性属性
//        DataSet hrpiPernontspropDataSet = orm.queryDataSet(algoKey, "hrpi_pernontsprop", "gender as tdkw_genderrpt,tdkw_origin.tdkw_address as tdkw_addressrpt,birthday as tdkw_birthdayrpt,person.id,age as tdkw_agerpt,tdkw_contrworkloc.tdkw_detailedaddress,tdkw_full_pinyin_name as pinyin", new QFilter[]{personQfilter, pernontsprQfilter}, null);
//        //查询人员时序性属性
//        DataSet hrpiPertspropDataSet = orm.queryDataSet(algoKey, "hrpi_pertsprop", "person.id,marriagestatus as tdkw_marriagestatusrpt", new QFilter[]{perTsPropQfilter, personQfilter}, null);
//        // 院校类型单独处理
//        //查教育经历
//        DataSet eduDataSet = orm.queryDataSet(algoKey, "hrpi_pereduexp", "person.id,graduateschool.id,graduateschool.name as tdkw_schoolname,schoolrecord,education as tdkw_educationrpt,major as tdkw_majorrpt", new QFilter[]{personQfilter, eduQfilter}, null);
//        //拿出院校id
//        //List<Long> schoolIds = orm.toPlainDynamicObjectCollection(eduDataSet.copy()).stream().map(i -> i.getLong("graduateschool.id")).collect(Collectors.toList());
//        DynamicObjectCollection hrpiPereduexp = QueryServiceHelper.query("hrpi_pereduexp", "graduateschool.id", new QFilter[]{personQfilter, eduQfilter});
//        List<Long> schoolIds = hrpiPereduexp.stream().map(dynamicObject -> dynamicObject.getLong("graduateschool.id")).collect(Collectors.toList());
//        //查基本信息补充
//        DataSet baseDataSet = orm.queryDataSet(algoKey, "hrpi_perregion", "person.id,politicalstatus as tdkw_politicalstatusrpt", new QFilter[]{personQfilter, politicalQfilter}, null);
//        //查服务年限
//        DataSet hrpiPerserlenDataSet = orm.queryDataSet(algoKey, "hrpi_perserlen", "person.id,socialworkage as tdkw_socialworkagerpt,tdkw_newjointime as tdkw_comsercountrpt,tdkw_serviceyear as tdkw_serviceyearrpt", new QFilter[]{personQfilter, perserlenQfilter}, null);

        // 查询sql
        String conditionSql = "SELECT nprop.fgenderid as tdkw_genderrpt, " +
//                "nprop.fk_tdkw_origin as tdkw_addressrpt, " +
                "nprop.fbirthday as tdkw_birthdayrpt, " +
                "nprop.fpersonid, " +
                "nprop.fage as tdkw_agerpt, " +
                "nprop.fid as workplace, " +
//                "nprop.fk_tdkw_full_pinyin_name as pinyin, " +
                "prop.fmarriagestatusid as tdkw_marriagestatusrpt, " +
//                "edu.fgraduateschool as graduateschoolid, " +
//                "collage.fname as tdkw_schoolname, " +
//                "edu.fschoolrecord as schoolrecord, " +
//                "edu.feducationid as tdkw_educationrpt, " +
//                "edu.fmajor as tdkw_majorrpt, " +
                "region.fpoliticalstatusid as tdkw_politicalstatusrpt, " +
                "plen.fsocialworkage as tdkw_socialworkagerpt " +
//                "plen.fk_tdkw_newjointime as tdkw_comsercountrpt, " +
//                "plen.fk_tdkw_serviceyear as tdkw_serviceyearrpt " +
                "FROM t_hrpi_pernontsprop nprop " +
                "LEFT JOIN t_hrpi_pertsprop prop ON nprop.fpersonid = prop.fpersonid " +
                //教育经历逻辑复杂 单独处理
//                "LEFT JOIN t_hrpi_pereduexp edu ON nprop.fpersonid = edu.fpersonid " +
                "LEFT JOIN t_hrpi_perregion region ON nprop.fpersonid = region.fpersonid " +
                "LEFT JOIN t_hrpi_perserlen plen ON nprop.fpersonid = plen.fpersonid " +
//                "LEFT JOIN t_hbss_college collage ON edu.fgraduateschool = collage.fid " +
                //工作地点跨库  单独处理
                //"LEFT JOIN T_BD_AdminDivision place ON nprop.fk_tdkw_protocol_workplac = place.fid " +
                "WHERE nprop.fiscurrentversion = '1' " +
                "AND nprop.fdatastatus = '1' " +
                "AND prop.fiscurrentversion = '1' " +
                "AND prop.fdatastatus = '1' " +
//                "AND edu.fiscurrentversion = '1' " +
//                "AND edu.fdatastatus = '1' " +
//                "AND edu.fk_tdkw_ishighestcheck = '1' " +
                "AND region.fiscurrentversion = '1' " +
                "AND region.fdatastatus = '1' " +
                "AND plen.fiscurrentversion = '1' " +
                "AND plen.fdatastatus = '1'";
        DataSet hr = DB.queryDataSet(algoKey, DBRoute.of("hr"), conditionSql);
        if (!flag) {
            //若未输入学校或专业则取最高级
//            eduQfilter.and("tdkw_ishighestcheck", QCP.equals, "1");
        }

        DataSet eduDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pereduexp", "id,person.id,graduateschool as graduateschoolid,graduateschool.name as tdkw_schoolname,schoolrecord,education.id as tdkw_educationrpt,major as tdkw_majorrpt,education.number", new QFilter[]{personQfilter, eduQfilter}, null);
        if (flag) {
            List<String> numberValues = Arrays.asList("XY00001", "XY00002", "XY00003", "XY00004");
            //过滤大专及以上学历
            HashMap<String, Object> paramNumber = Maps.newHashMapWithExpectedSize(1);
            paramNumber.put("var", numberValues);
            eduDataSet = eduDataSet.filter("education.number in var", paramNumber);

            //对人员教育经历进行处理
            DynamicObjectCollection dataSetObject = ORM.create().toPlainDynamicObjectCollection(eduDataSet.copy());
            Map<String, Map<String, DynamicObject>> resultMap = new HashMap<>();
            for (DynamicObject dynamicObject : dataSetObject) {
                String personId = dynamicObject.getString("person.id").toString();
                String school = dynamicObject.getString("tdkw_schoolname");
                String level = dynamicObject.getString("education.number");

                Map<String, DynamicObject> schoolMap = resultMap.getOrDefault(personId, new HashMap<>());
                DynamicObject existingData = schoolMap.get(school);
                if (existingData == null || compareLevel(level, existingData.get("education.number").toString()) > 0) {
                    schoolMap.put(school, dynamicObject);
                }
                resultMap.put(personId, schoolMap);
            }

            //过滤筛选学历
            List<Long> schoolIds = new ArrayList<>();
            for (Map<String, DynamicObject> newResultMap : resultMap.values()) {
                for (DynamicObject dynamicObject : newResultMap.values()) {
                    schoolIds.add(dynamicObject.getLong("id"));
                }
            }
            paramNumber.put("var1", schoolIds);
            eduDataSet = eduDataSet.filter("id in var1", paramNumber);
            logger.info("教育经历:" + ORM.create().toPlainDynamicObjectCollection(eduDataSet.copy()));
        }
        hr = hr.leftJoin(eduDataSet).on("fpersonid", "person.id").select("tdkw_genderrpt", "tdkw_birthdayrpt", "fpersonid", "tdkw_agerpt", "workplace",  "tdkw_marriagestatusrpt", "graduateschoolid", "tdkw_schoolname", "schoolrecord", "tdkw_educationrpt", "tdkw_majorrpt", "tdkw_politicalstatusrpt", "tdkw_socialworkagerpt").finish();

        //左连接人员非时序性属性
//        DataSet finish = erManFileDataSet.leftJoin(hrpiPernontspropDataSet).on("tdkw_personid", "person.id").select("ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "tdkw_postlevelrpt", "tdkw_employtyperpt", "tdkw_index", "sortcode", "tdkw_personid", "tdkw_engagedindustry", "tdkw_ranks", "tdkw_jobsequence", "tdkw_contrworkloc.tdkw_detailedaddress", "isprimary", "asname", "newname", "person.name", "tdkw_genderrpt", "tdkw_addressrpt", "tdkw_birthdayrpt", "tdkw_agerpt", "pinyin", "personid", "jobfid", "tdkw_adminrpt", "tdkw_postype", "tdkw_versionnumber").finish();
//        //左连接人员时序性属性
//        finish = finish.leftJoin(hrpiPertspropDataSet).on("tdkw_personid", "person.id").select("ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "tdkw_postlevelrpt", "tdkw_employtyperpt", "tdkw_index", "sortcode", "tdkw_personid", "tdkw_engagedindustry", "tdkw_ranks", "tdkw_jobsequence", "tdkw_contrworkloc.tdkw_detailedaddress", "isprimary", "asname", "newname", "tdkw_marriagestatusrpt", "person.name", "tdkw_genderrpt", "tdkw_addressrpt", "tdkw_birthdayrpt", "tdkw_agerpt", "pinyin", "personid", "jobfid", "tdkw_adminrpt", "tdkw_postype", "tdkw_versionnumber").finish();
//        //左连接教育经历
//        finish = finish.leftJoin(eduDataSet).on("tdkw_personid", "person.id").select("ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "tdkw_postlevelrpt", "tdkw_employtyperpt", "tdkw_index", "sortcode", "tdkw_personid", "tdkw_engagedindustry", "tdkw_ranks", "tdkw_jobsequence", "tdkw_contrworkloc.tdkw_detailedaddress", "isprimary", "asname", "newname", "tdkw_marriagestatusrpt", "graduateschool.id", "tdkw_schoolname", "schoolrecord", "tdkw_educationrpt", "person.name", "tdkw_genderrpt", "tdkw_addressrpt", "tdkw_birthdayrpt", "tdkw_agerpt", "tdkw_majorrpt", "pinyin", "personid", "jobfid", "tdkw_adminrpt", "tdkw_postype", "tdkw_versionnumber").finish();
//        //左连接基本信息补充
//        finish = finish.leftJoin(baseDataSet).on("tdkw_personid", "person.id").select("ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "tdkw_postlevelrpt", "tdkw_employtyperpt", "tdkw_index", "sortcode", "tdkw_personid", "tdkw_engagedindustry", "tdkw_ranks", "tdkw_jobsequence", "tdkw_contrworkloc.tdkw_detailedaddress", "isprimary", "asname", "newname", "tdkw_marriagestatusrpt", "graduateschool.id", "tdkw_schoolname", "schoolrecord", "tdkw_educationrpt", "tdkw_politicalstatusrpt", "person.name", "tdkw_genderrpt", "tdkw_addressrpt", "tdkw_birthdayrpt", "tdkw_agerpt", "tdkw_majorrpt", "pinyin", "personid", "jobfid", "tdkw_adminrpt", "tdkw_postype", "tdkw_versionnumber").finish();
//        //左连接服务年限
//        finish = finish.leftJoin(hrpiPerserlenDataSet).on("tdkw_personid", "person.id").select("ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "tdkw_postlevelrpt", "tdkw_employtyperpt", "tdkw_index", "sortcode", "tdkw_personid", "tdkw_engagedindustry", "tdkw_ranks", "tdkw_jobsequence", "tdkw_contrworkloc.tdkw_detailedaddress", "isprimary", "asname", "newname", "tdkw_marriagestatusrpt", "graduateschool.id", "tdkw_schoolname", "schoolrecord", "tdkw_educationrpt", "tdkw_politicalstatusrpt", "tdkw_socialworkagerpt", "tdkw_comsercountrpt", "tdkw_serviceyearrpt", "person.name", "tdkw_genderrpt", "tdkw_addressrpt", "tdkw_birthdayrpt", "tdkw_agerpt", "tdkw_majorrpt", "pinyin", "personid", "jobfid", "tdkw_adminrpt", "tdkw_postype", "tdkw_versionnumber").finish().orderBy(new String[]{"sortcode", "tdkw_index"});

        DataSet finish = erManFileDataSet.leftJoin(hr).on("tdkw_personid", "fpersonid")
                .select("ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt",
                        "tdkw_positionrpt",
                        "sortcode", "tdkw_personid",
                        "workplace",
                        "isprimary", "newname", "tdkw_marriagestatusrpt",
                        "graduateschoolid as graduateschool.id", "tdkw_schoolname", "schoolrecord",
                        "tdkw_educationrpt", "tdkw_politicalstatusrpt", "tdkw_socialworkagerpt",
                        "person.name",
                        "tdkw_genderrpt","tdkw_birthdayrpt",
                        "tdkw_agerpt", "tdkw_majorrpt",
                        "tdkw_adminrpt", "tdkw_postype",
                        "tdkw_versionnumber").finish().orderBy(new String[]{"sortcode"});
        //工作地点单独处理
        DataSet dataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pernontsprop", "id", new QFilter[]{pernontsprQfilter}, null);
        finish = finish.leftJoin(dataSet).on("workplace", "id")
                .select("ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt",
                        "tdkw_positionrpt",
                        "sortcode", "tdkw_personid",
                        "workplace",
                        "isprimary", "newname", "tdkw_marriagestatusrpt",
                        "graduateschool.id", "tdkw_schoolname", "schoolrecord",
                        "tdkw_educationrpt", "tdkw_politicalstatusrpt", "tdkw_socialworkagerpt",
                        "person.name",
                        "tdkw_genderrpt","tdkw_birthdayrpt",
                        "tdkw_agerpt", "tdkw_majorrpt",
                        "tdkw_adminrpt", "tdkw_postype",
                        "tdkw_versionnumber").finish().orderBy(new String[]{"sortcode"});

        Object searchName = reportQueryParam.getCustomParam().get("searchName");
        logger.info("右边searchName为：" + searchName);
        // 判断搜索的searchName中是否包含字母、正则
        if (searchName != null && StringUtils.isNotBlank((CharSequence) searchName)) {
            String nameStr = searchName.toString();
            if (containsLetters(nameStr)) {
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
                finish = finish.where(" pinyin like '%" + pinYinName + "%'");
            } else {
                // 不包含字母
                finish = finish.where(" person.name like '%" + searchName + "%'");
            }
        }

//        List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            HashMap<String, Object> param = Maps.newHashMapWithExpectedSize(1);
            switch (propName) {
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
                        finish = finish.filter("tdkw_postlevelrpt in var", param);
                    }
                    break;
                //姓名
                case "tdkw_name":
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String name = row.getString("person.name");
                                return name.contains((CharSequence) value);
                            }
                        });
                    }
                    break;
                // 学历
                case "tdkw_degree":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_educationrpt in var", param);
                    }
                    break;
                //婚育
                case "tdkw_marriage":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_marriagestatusrpt in var", param);
                    }
                    break;
                //起始社会工龄
                case "tdkw_social_workage":
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_socialworkagerpt >= var", param);
                    }
                    break;
                //终止社会工龄
                case "tdkw_social_workagen":
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_socialworkagerpt <= var", param);
                    }
                    break;
                //起始年龄
                case "tdkw_agestart":
                    if ((Integer) value != 0) {
                        param.put("var", value);
                        finish = finish.filter("tdkw_agerpt >= var", param);
                    }
                    break;
                //终止年龄
                case "tdkw_ageend":
                    if ((Integer) value != 0) {
                        param.put("var", value);
                        finish = finish.filter("tdkw_agerpt <= var", param);
                    }
                    break;
                //起始集团司龄
                case "tdkw_comagestart":
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_comsercountrpt >= var", param);
                    }
                    break;
                // 终止集团司龄
                case "tdkw_comageend":
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_comsercountrpt <= var", param);
                    }
                    break;
                //起始公司司龄
                case "tdkw_company_agestart":
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_serviceyearrpt >= var", param);
                    }
                    break;
                //终止公司司龄
                case "tdkw_company_ageend":
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_serviceyearrpt <= var", param);
                    }
                    break;
                //  从事行业
                case "tdkw_currjob":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_engagedindustry in var", param);
                    }
                    break;
                //性别
                case "tdkw_gender":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_genderrpt in var", param);
                    }
                    break;
                //  职务
                case "tdkw_position":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        logger.info("职层的主键id" + ids);
                        DynamicObjectCollection query = QueryServiceHelper.query("hbjm_jobgradehr", "entryboid", new QFilter[]{new QFilter("id", QCP.in, ids)});
                        Collection<Long> entryboid = query.stream().map(i -> i.getLong("entryboid")).collect(Collectors.toList());
                        logger.info("职层的boid为" + entryboid);
                        param.put("var", entryboid);
                        finish = finish.filter("tdkw_ranks in var", param);
                    }
                    break;
                //  岗位序列
                case "tdkw_jobseq":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_jobsequence in var", param);
                    }
                    break;
                //政治面貌
                case "tdkw_political_status":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_politicalstatusrpt in var", param);
                    }
                    break;

                //工作地点
                case "tdkw_workpalce":
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String address = row.getString("tdkw_contrworkloc.tdkw_detailedaddress");
                                if (StringUtils.isNotBlank(address)) {
                                    return address.contains((CharSequence) value);
                                } else {
                                    return false;
                                }

                            }
                        });
                    }
                    break;
                // 院校类别
                case "tdkw_schooltype":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        collegeTypeQfilter.and("collegecharact.fbasedataid", QCP.in, ids);
                        //根据院校类型查院校
                        //collegeTypeQfilter.and("id", QCP.in, schoolIds);
                        //查出根据院校类型过滤出来的符合条件的院校
                        DynamicObjectCollection schools = QueryServiceHelper.query("hbss_college", "id", collegeTypeQfilter.toArray());
                        List<Long> id = schools.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        //进行过滤
                        param.put("var", id);
                        finish = finish.filter("graduateschool.id in var", param);
                    }
                    break;
                //  家属工作单位
                case "tdkw_famwork":
                    if (StringUtils.isNotBlank((String) value)) {
                        // 过滤家属工作单位 先查家庭成员信息
                        QFilter homQfilter = new QFilter("iscurrentversion", QCP.equals, "1");
                        homQfilter.and("datastatus", QCP.equals, "1");
                        DynamicObjectCollection homDynamicCollection = QueryServiceHelper.query("hrpi_familymemb", "person.id,workunit", new QFilter[]{homQfilter, personQfilter}, null);
                        //将符合条件的人员id添加到set集合里
                        Set<Long> finishSet = homDynamicCollection.stream().filter(i -> i.getString("workunit") != null && i.getString("workunit").contains((CharSequence) value)).map(i -> i.getLong("person.id")).collect(Collectors.toSet());
                        // 对finish进行人员id过滤
                        param.put("var", finishSet);
                        finish = finish.filter("tdkw_personid in var", param);
                    }
                    break;
                //截止日期
                case "tdkw_deadtline":
                    if (null != value) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime((Date) value);
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
                        Date time = calendar.getTime();
                        param.put("var", time);
                        finish = finish.filter("startdate <= var", param);
                        finish = finish.filter("enddate >= var", param);
                    }
                    break;
                // 人员类别
                case "tdkw_persontype":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_employtyperpt in var", param);
                    }
                    break;
                //是否兼职
                case "tdkw_isprimary":
                    if (StringUtils.isNotBlank((String) value)) {
                        if (StringUtils.equals((CharSequence) value, "0")) {
                            logger.info("只过滤主任职");
                            param.put("var", "1");
                            finish = finish.filter("isprimary = var", param);
                        }
                    } else {
                        param.put("var", "1");
                        finish = finish.filter("isprimary = var", param);
                    }
                    break;
                //  毕业院校
                case "tdkw_school":
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.where("tdkw_schoolname like '%" + value + "%' or schoolrecord like '%" + value + "%'");
                    }
                    break;
                //岗位标签
                case "tdkw_jobfrpt":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        QFilter qFilter = new QFilter("tdkw_postiontag.fbasedataid", QCP.in, ids);
                        DynamicObject[] load = BusinessDataServiceHelper.load("hbpm_positionhr", "id", new QFilter[]{qFilter});
                        ids = Arrays.stream(load).map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_positionrpt in var", param);

                    }
                    break;
                //专业
                case "tdkw_major":
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String major = row.getString("tdkw_majorrpt");
                                if (StringUtils.isNotBlank(major)) {
                                    return major.contains((CharSequence) value);
                                }
                                return false;

                            }
                        });
                    }
                    break;
                case "tdkw_postype":
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_postype in var", param);

                    }
                    break;
            }
        }
        //岗位标签特殊处理
       /* QFilter jobfQfilter = new QFilter("iscurrentversion", QCP.equals, "1");
        DataSet jobfDataSet = orm.queryDataSet(algoKey, "hbjm_jobfamilyhr", "id,name as jobfname", new QFilter[]{jobfQfilter}, null);
        finish = finish.leftJoin(jobfDataSet).on("jobfid", "id").select(new String[]{"tdkw_adminrpt", "ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "tdkw_postlevelrpt", "tdkw_employtyperpt", "tdkw_index", "sortcode", "tdkw_personid", "tdkw_engagedindustry", "tdkw_ranks", "tdkw_jobsequence", "tdkw_contrworkloc.tdkw_detailedaddress", "isprimary", "asname", "newname", "tdkw_marriagestatusrpt", "graduateschool.id as tdkw_collegerpt", "tdkw_schoolname", "schoolrecord", "tdkw_educationrpt", "tdkw_politicalstatusrpt", "tdkw_socialworkagerpt", "tdkw_comsercountrpt", "tdkw_serviceyearrpt", "tdkw_genderrpt", "tdkw_addressrpt", "tdkw_birthdayrpt", "tdkw_agerpt", "personid", "jobfid", "tdkw_majorrpt", "pinyin", "tdkw_postype", "tdkw_versionnumber"}, new String[]{"jobfname"}).finish();
        GroupbyDataSet groupbyDataSet = finish.groupBy(new String[]{"tdkw_adminrpt", "ermanfileid", "startdate", "enddate", "tdkw_companyrpt", "tdkw_personrpt", "tdkw_positionrpt", "tdkw_postlevelrpt", "tdkw_employtyperpt", "tdkw_index", "sortcode", "tdkw_personid", "tdkw_engagedindustry", "tdkw_ranks", "tdkw_jobsequence", "tdkw_contrworkloc.tdkw_detailedaddress", "isprimary", "asname", "newname", "tdkw_marriagestatusrpt", "tdkw_collegerpt", "tdkw_schoolname", "schoolrecord", "tdkw_educationrpt", "tdkw_politicalstatusrpt", "tdkw_socialworkagerpt", "tdkw_comsercountrpt", "tdkw_serviceyearrpt", "tdkw_genderrpt", "tdkw_addressrpt", "tdkw_birthdayrpt", "tdkw_agerpt", "personid", "tdkw_majorrpt", "pinyin", "tdkw_postype", "tdkw_versionnumber"});
        finish = groupbyDataSet.groupConcat("jobfname", null, ",").finish();
        finish = finish.addField("case when jobfname = 'null' then '无' else jobfname end as jobfname1", "jobfname1");
        //处理展示岗位
        finish = finish.addField("case when asname = '' then newname else asname end as newname", "newname");
        //处理展示学校
        finish = finish.addField("case when tdkw_schoolname = '其他院校' then schoolrecord else tdkw_schoolname end as tdkw_schoolname", "tdkw_schoolname");
        //当选择学历或学校时对结果过滤
        if (flag) {
            //过滤输入学校
            if (!invalidValues.contains(filterSchool)) {
                String finalFilterSchool = filterSchool;
                finish = finish.filter(new FilterFunction() {
                    @Override
                    public boolean test(Row row) {
                        String schoolName = row.getString("tdkw_schoolname");
                        return schoolName.contains((CharSequence) finalFilterSchool);
                    }
                });
            }

            //过滤输入学历
            if (!invalidValues.contains(filtermajor)) {
                String finalFiltermajor = filtermajor;
                finish = finish.filter(new FilterFunction() {
                    @Override
                    public boolean test(Row row) {
                        String majorName = row.getString("tdkw_majorrpt");
                        return majorName.contains((CharSequence) finalFiltermajor);
                    }
                });
            }
        }*/

        return finish.orderBy(new String[]{"sortcode"});
    }

    private static int compareLevel(String level1, String level2) {
        // 定义等级顺序，例如 "XY00001" > "XY00002" > "XY00003" > "XY00004"
        Map<String, Integer> levelOrder = new HashMap<>();
        levelOrder.put("XY00001", 1);
        levelOrder.put("XY00002", 2);
        levelOrder.put("XY00003", 3);
        levelOrder.put("XY00004", 4);

        return levelOrder.get(level2) - levelOrder.get(level1);
    }

    private QFilter getHasRightOrg(QFilter orgQfilter) {
        AuthorizedOrgResult result = HRStructOrgUtils.getStructUserAdminOrgs(UserServiceHelper.getCurrentUserId(), entryNumber);
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        logger.info("事业部花名册报表是否有所有权限:" + hasAllOrgPerm);
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            logger.info("有权限的组织为:" + Arrays.toString(new List[]{hasPerOrg}));
            //根据认识业务档案的员工任职的所属部门进行控权
            orgQfilter.and("adminorg", QCP.in, hasPerOrg);
        }
        return orgQfilter;
    }

    private static void versionControl(QFilter eduQfilter, QFilter perTsPropQfilter, QFilter perserlenQfilter, QFilter pernontsprQfilter, QFilter politicalQfilter, QFilter collegeTypeQfilter, QFilter erManFileQfilter) {
        //教育经历
        eduQfilter.and("iscurrentversion", QCP.equals, "1");
        eduQfilter.and("datastatus", QCP.equals, "1");
//        eduQfilter.and("tdkw_ishighestcheck", QCP.equals, "1");
        //高等院校
        collegeTypeQfilter.and("iscurrentversion", QCP.equals, "1");
        collegeTypeQfilter.and("datastatus", QCP.equals, "1");
        collegeTypeQfilter.and("enable", QCP.equals, "1");
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
        erManFileQfilter.and("person.iscurrentversion", QCP.equals, "1");
        erManFileQfilter.and("person.datastatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.isprimary", QCP.equals, "1");
    }
    /**
     * 判断字符串是否包含字母
     *
     * @param str
     * @return
     */
    public static boolean containsLetters(String str) {
        return str.matches(".*[a-zA-Z]+.*");
    }

}
