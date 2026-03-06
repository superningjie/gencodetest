package tdkw.esc.leaderquery.integratedquery.report.newretireleave;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.GroupbyDataSet;
import kd.bos.algo.Row;
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
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.PinyinUtil;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RightTablePlugin
 *
 * @author xxx
 * @date 2023/8/8
 */
public class RightTablePlugin extends AbstractReportListDataPlugin {

    private static final Log logger = LogFactory.getLog(RightTablePlugin.class);

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        long start = System.currentTimeMillis();
        QFilter orgQfilter = new QFilter("1", QCP.equals, 1);
        QFilter leftTreeFilter = new QFilter("1", QCP.equals, 1);
        QFilter flowQfilter = new QFilter("1", QCP.equals, 1);
        // QFilter depEmpQfilter = new QFilter("1", QCP.equals, 1);
        QFilter eduQfilter = new QFilter("1", QCP.equals, 1);
        QFilter perTsPropQfilter = new QFilter("1", QCP.equals, 1);
        QFilter perserlenQfilter = new QFilter("1", QCP.equals, 1);
        QFilter pernontsprQfilter = new QFilter("1", QCP.equals, 1);
        QFilter politicalQfilter = new QFilter("1", QCP.equals, 1);
        QFilter collegeTypeQfilter = new QFilter("1", QCP.equals, 1);
        QFilter erManFileQfilter = new QFilter("1", QCP.equals, 1);
        String algoKey = this.getClass().getName();
        versionControl(eduQfilter, perTsPropQfilter, perserlenQfilter, pernontsprQfilter, politicalQfilter, collegeTypeQfilter, erManFileQfilter);

        //异动类型-所属变动大类=离职；组织人-任职类型=全职任职
//        flowQfilter.and("tdkw_changetype.tdkw_chgevent.number", QCP.equals, "1070_S");
//        flowQfilter.and("depemp.postype.number", QCP.equals, "XY00001");
        // 全职任职是1010_S啊
        flowQfilter.and("depemp.postype.number", QCP.equals, "1010_S");
        // 变动类型，101020_S-雇佣人员离职 ，101200_S-退休
        flowQfilter.and("chgcategory.number", QCP.in, Lists.newArrayList("101020_S", "101200_S"));
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
                    leftTreeFilter = leftTreeFilter.and("adminorg.company", QCP.in, reportQueryParam.getCustomParam().get("orgIds")).or("adminorg", QCP.in, hrOrgIds);
                    orgQfilter = orgQfilter.and(leftTreeFilter);
                }else {
                    leftTreeFilter = leftTreeFilter.and("adminorg.company", QCP.in, reportQueryParam.getCustomParam().get("orgIds")).or("adminorg", QCP.in, reportQueryParam.getCustomParam().get("orgIds"));
                    orgQfilter = orgQfilter.and(leftTreeFilter);
                }
            }else {
                // 2025-10-31 增加报表过滤，当前人组织
                //获取当前登录人员所属组织
                List<Long> userIds = new ArrayList<>(1);
                userIds.add(UserServiceHelper.getCurrentUserId());
                // 获取当前登录人员所属公司
                String adminOrg = String.valueOf(UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId()));

                orgQfilter = orgQfilter.and("adminorg.company", QCP.in, Long.valueOf(adminOrg));

            }
        }


        //查流入流出表 公司、姓名、岗位、岗位层级 职层 职位序列 异动类型、离职/退休原因、离职/退休日期 行政组织 行政组织.所属公司 行政组织.行业类别 行政组织排序码  ,adminorg.industrytype
        DataSet personFlow = QueryServiceHelper.queryDataSet(algoKey, "hpfs_personflow", "employee.id," +
                "adminorg,personname,depemp.position.id,flowtime,adminorg.sortcode,person.id,adminorg.company,depemp.id as depemp," +
                "adminorg.company.boid,depemp.position.name", new QFilter[]{flowQfilter, orgQfilter}, null);
        //查询离职档案数据
        DataSet quitfileDataSet = ORM.create().queryDataSet(algoKey, "htm_quitfileinfo", "employee.id,contractenddate", new QFilter[]{});
//        return quitfileDataSet;
        personFlow = personFlow.leftJoin(quitfileDataSet).on("employee.id", "employee.id").select("contractenddate as flowtime", "adminorg", "personname", "depemp.position.id", "adminorg.sortcode", "person.id", "adminorg.company", "depemp", "adminorg.company.boid", "depemp.position.name").finish();
        //personFlow.addField("case when depemp.position.tdkw_displayvalue = '' then depemp.position.name else depemp.position.tdkw_displayvalue end as depemp.position.name", "depemp.position.name");
        DynamicObjectCollection personFlowCollection = ORM.create().toPlainDynamicObjectCollection(personFlow.copy());
        //拿出人员
        List<Long> personIds = personFlowCollection.stream().map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter personQfilter = new QFilter("person", QCP.in, personIds);
        logger.info("流入流出表查出的人员为:" + Arrays.toString(new List[]{personIds}));
        logger.info("流入流出表查出的人员数为:" + personIds.size());
        //拿出所属公司boid
        Set<Long> companyBoids = personFlowCollection.stream().map(i -> i.getLong("adminorg.company.boid")).collect(Collectors.toSet());
        logger.info("流入流出表查出的所属公司boid为:" + Arrays.toString(new Set[]{companyBoids}));
        //查询任职经历基础
        DataSet empposorgre = QueryServiceHelper.queryDataSet(algoKey,"hrpi_empposorgrel","depemp.id as depemp",new QFilter[]{personQfilter,new QFilter("datastatus", QCP.equals,"1").and("iscurrentversion",QCP.equals,"1")},null);
        personFlow = personFlow.leftJoin(empposorgre).on("depemp","depemp").select("flowtime", "adminorg", "personname", "depemp.position.id", "adminorg.sortcode", "person.id", "adminorg.company", "adminorg.company.boid", "depemp.position.name").finish();


        //todo 院校类型单独处理
        //查教育经历
        DataSet eduDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pereduexp", "person.id,graduateschool.id,graduateschool.name,schoolrecord,education,major as tdkw_majorrpt", new QFilter[]{personQfilter, eduQfilter}, null);
        //拿出院校id
        List<Long> schoolIds = ORM.create().toPlainDynamicObjectCollection(eduDataSet.copy()).stream().map(i -> i.getLong("graduateschool.id")).collect(Collectors.toList());

        //查询人员时序性属性
        DataSet hrpiPertspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pertsprop",
                "person.id,marriagestatus", new QFilter[]{personQfilter, perTsPropQfilter}, null);
        //查询服务年限
        DataSet hrpiPerserlenDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perserlen", "person.id,socialworkage",
                new QFilter[]{personQfilter, perserlenQfilter}, null);
        //查询人员非时序性属性
        DataSet hrpiPernontspropDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pernontsprop",
                "gender,age,person.id", new QFilter[]{personQfilter, pernontsprQfilter}, null);
        //查询基本信息补充
        DataSet baseDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perregion", "person.id,politicalstatus",
                new QFilter[]{personQfilter, politicalQfilter}, null);
        //查询人事业务档案
        DataSet hspmErmanfileDataSet = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", "person.number as tdkw_number,person.id,boid,empposrel.position as tdkw_positionrpt", new QFilter[]{personQfilter, erManFileQfilter}, null);
        //todo 过滤家属工作单位 先查家庭成员信息
        QFilter homQfilter = new QFilter("iscurrentversion", QCP.equals, "1");
        homQfilter.and("datastatus", QCP.equals, "1");
        DataSet homDataSet = QueryServiceHelper.queryDataSet(algoKey, "hrpi_familymemb", "person.id,workunit", new QFilter[]{homQfilter, personQfilter}, null);
        DynamicObjectCollection homDynamicCollection = QueryServiceHelper.query("hrpi_familymemb", "person.id,workunit", new QFilter[]{homQfilter, personQfilter}, null);
        HashSet<Long> personSet = Sets.newHashSetWithExpectedSize(personIds.size() / 2);


        //todo 流入流出内连接教育经历 冗余查询组织的排序码 和人员 adminorg.industrytype
        DataSet finish1 = personFlow.leftJoin(eduDataSet).on("person.id", "person.id").select("adminorg as tdkw_rptadminorg", "adminorg.company.boid",  "graduateschool.id", "graduateschool.name", "schoolrecord", "education as tdkw_rptdegree", "adminorg.company as tdkw_rptcompany", "personname as tdkw_rptname", "flowtime as tdkw_rptleavedate", "adminorg.sortcode as sortcode", "person.id", "depemp.position.name","person.id as personid").finish();
        //todo 内连接人员时序性属性
        DataSet finish2 = finish1.leftJoin(hrpiPertspropDataSet).on("person.id", "person.id").select("tdkw_rptadminorg", "adminorg.company.boid", "graduateschool.id", "graduateschool.name", "schoolrecord", "tdkw_rptdegree", "tdkw_rptcompany", "tdkw_rptname", "sortcode", "person.id", "marriagestatus as tdkw_rptmarriagestatus", "depemp.position.name", "tdkw_rptleavedate","person.id as personid").finish();
        //todo 内连接服务年限
        DataSet finish3 = finish2.leftJoin(hrpiPerserlenDataSet).on("person.id", "person.id").select("tdkw_rptadminorg", "adminorg.company.boid",   "graduateschool.id", "graduateschool.name", "schoolrecord", "tdkw_rptdegree", "tdkw_rptcompany", "tdkw_rptname",      "sortcode", "person.id", "tdkw_rptmarriagestatus", "socialworkage as tdkw_rptsocialage",  "depemp.position.name","tdkw_rptleavedate","person.id as personid").finish();
        //todo 内连接人员非时序性属性
        DataSet finish4 = finish3.leftJoin(hrpiPernontspropDataSet).on("person.id", "person.id").select("tdkw_rptadminorg", "adminorg.company.boid", "graduateschool.id", "graduateschool.name", "schoolrecord", "tdkw_rptdegree", "tdkw_rptcompany", "tdkw_rptname", "sortcode", "person.id", "tdkw_rptmarriagestatus", "tdkw_rptsocialage", "gender as tdkw_rptgender", "age as tdkw_rptage", "depemp.position.name", "tdkw_rptleavedate","person.id as personid").finish();
        //todo 内连接基本信息
        DataSet finish5 = finish4.leftJoin(baseDataSet).on("person.id", "person.id").select("tdkw_rptadminorg", "adminorg.company.boid", "graduateschool.id", "graduateschool.name", "schoolrecord", "tdkw_rptdegree", "tdkw_rptcompany", "tdkw_rptname", "sortcode", "person.id", "tdkw_rptmarriagestatus", "tdkw_rptsocialage", "tdkw_rptgender", "tdkw_rptage", "politicalstatus as tdkw_rptpoliticalstatus", "depemp.position.name", "tdkw_rptleavedate","person.id as personid").finish();
        //todo 内连接人事业务档案
        DataSet finish = finish5.leftJoin(hspmErmanfileDataSet).on("person.id", "person.id").select("tdkw_number", "tdkw_rptadminorg", "adminorg.company.boid", "graduateschool.id", "graduateschool.name", "schoolrecord", "tdkw_rptdegree", "tdkw_rptcompany", "tdkw_rptname", "sortcode", "person.id", "tdkw_rptmarriagestatus", "tdkw_rptsocialage", "tdkw_rptgender", "tdkw_rptage", "tdkw_rptpoliticalstatus", "tdkw_rptleavedate", "boid", "depemp.position.name as newname","person.id as personid").finish().orderBy(new String[]{"sortcode"});
        logger.info("finish的数据" + ORM.create().toPlainDynamicObjectCollection(finish.copy()));

        //岗位标签特殊处理
        /*QFilter jobfQfilter = new QFilter("iscurrentversion", QCP.equals, "1");
        DataSet jobfDataSet = QueryServiceHelper.queryDataSet(algoKey, "hbjm_jobfamilyhr", "id,name as jobfname", new QFilter[]{jobfQfilter}, null);
        finish = finish.leftJoin(jobfDataSet).on("jobfid", "id").select(new String[]{"tdkw_number", "tdkw_rptadminorg", "adminorg.company.boid",  "depemp.position.tdkw_jobgradehr",  "graduateschool.id as tdkw_collegerpt", "graduateschool.name as tdkw_schoolname", "schoolrecord", "tdkw_rptdegree", "tdkw_rptcompany", "tdkw_rptname",      "sortcode", "person.id", "tdkw_rptmarriagestatus", "tdkw_rptsocialage",  "tdkw_rptgender", "tdkw_rptage", "tdkw_rptpoliticalstatus", "tdkw_index",   "boid", "newname", "asname", "personid"}, new String[]{"jobfname"}).finish();
        GroupbyDataSet groupbyDataSet = finish.groupBy(new String[]{"tdkw_number", "tdkw_rptadminorg", "adminorg.company.boid",  "depemp.position.tdkw_jobgradehr",  "tdkw_collegerpt", "tdkw_schoolname", "schoolrecord", "tdkw_rptdegree", "tdkw_rptcompany", "tdkw_rptname",     "sortcode", "person.id", "tdkw_rptmarriagestatus", "tdkw_rptsocialage",  "tdkw_rptgender", "tdkw_rptage", "tdkw_rptpoliticalstatus", "tdkw_index",   "boid", "newname", "asname", "personid"});
        finish = groupbyDataSet.groupConcat("jobfname", null, ",").finish();
        finish = finish.addField("case when jobfname = 'null' then '无' else jobfname end as jobfname1", "jobfname1");
        //处理展示学校
        finish = finish.addField("case when tdkw_schoolname = '其他院校' then schoolrecord else tdkw_schoolname end as tdkw_schoolname", "tdkw_schoolname");
        //处理展示岗位
        finish = finish.addField("case when asname = '' then newname else asname end as newname", "newname");*/
        Object searchName = reportQueryParam.getCustomParam().get("searchName");
        logger.info("姓名搜索框" + searchName);
//        if (searchName != null) {
//            if (StringUtils.isNotBlank((CharSequence) searchName)) {
//                finish = finish.filter(new FilterFunction() {
//                    @Override
//                    public boolean test(Row row) {
//                        String name = row.getString("tdkw_rptname");
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
                logger.info("拼音查询：" + pinYinName);
            } else {
                // 不包含字母
                finish = finish.filter(new FilterFunction() {
                    private static final long serialVersionUID = 4017820239971693392L;

                    @Override
                    public boolean test(Row row) {
                        logger.info("进入姓名模糊搜索");
                        String name = row.getString("tdkw_rptname");
                        return name.contains((CharSequence) searchName);
                    }
                });
                logger.info("拼音查询：" + searchName);
            }
        }
        List<FilterItemInfo> filterItems = reportQueryParam.getFilter().getFilterItems();
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            HashMap<String, Object> param = Maps.newHashMapWithExpectedSize(1);
            switch (propName) {
                //todo 流入流出表
                case "tdkw_name":
                    //姓名
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String name = row.getString("tdkw_rptname");
                                logger.info("进入姓名搜索");
                                return name.contains((CharSequence) value);
                            }
                        });
                        logger.info("查询姓名:" + value);
                        //finish = finish.filter("tdkw_rptname like 'temp'", param);
                        // flowQfilter.and("person.name", QCP.like, "%" + value + "%");
                    }
                    break;
                case "tdkw_changetype":
                    //异动类别
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> id = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", id);
                        finish = finish.filter("tdkw_rptchangetype in var", param);
                        // flowQfilter.and("tdkw_changetype", QCP.in, ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList()));
                    }
                    break;
                case "tdkw_reason":
                    //离职/退休原因
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_rptleavereason in var", param);
                        // flowQfilter.and("tdkw_changereason", QCP.equals, ((DynamicObject) value).getLong("id"));
                    }
                    break;
                case "tdkw_leavedate":
                    //离职/退休日期
                    if (null != value) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime((Date) value);
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
                        Date time = calendar.getTime();
                        param.put("var", time);
                        finish = finish.filter("tdkw_rptleavedate <= var", param);
                        //flowQfilter.and("flowtime", QCP.less_equals, value);
                    }
                    break;
                case "tdkw_currjob":
                    //从事行业 取流入流出表的行政组织的所属公司boid 作为id去查共公司信息的行业类别
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        //公司信息的行业类别
                        QFilter companyInfoqFilter = new QFilter("industrytype", QCP.in, ids);
                        //所属公司的boid
                        companyInfoqFilter.and("id", QCP.in, companyBoids);
                        companyInfoqFilter.and("iscurrentversion", QCP.equals, "1");
                        companyInfoqFilter.and("datastatus", QCP.equals, "1");
                        List<Object> industryTypeQueryIds = QueryServiceHelper.queryPrimaryKeys(algoKey, "haos_adminorgcompany", companyInfoqFilter.toArray(), null, companyBoids.size());
                        param.put("var", industryTypeQueryIds);
                        finish = finish.filter("adminorg.company.boid in var", param);
                    }
                    break;

                //todo 组织人
                case "tdkw_positionlevel":
                    //岗位层级
                    if (StringUtils.isNotBlank((String) value)) {
                        List<String> strings = Arrays.asList("1", "2", "3", "4", "5");
                        ArrayList<Object> valueList = new ArrayList<>();
                        for (String str : strings) {
                            if (((String) value).contains(str)) {
                                valueList.add(str);
                            }
                        }
                        param.put("var", valueList);
                        finish = finish.filter("tdkw_rptpositionlevel in var", param);
                    }
                    break;
                case "tdkw_ranks":
                    //职务
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        logger.info("职层的主键id" + ids);
                        DynamicObjectCollection query = QueryServiceHelper.query("hbjm_jobgradehr", "entryboid", new QFilter[]{new QFilter("id", QCP.in, ids)});
                        Collection<Long> entryboid = query.stream().map(i -> i.getLong("entryboid")).collect(Collectors.toList());
                        logger.info("职层的boid为" + entryboid);
                        param.put("var", entryboid);
                        finish = finish.filter("depemp.position.tdkw_jobgradehr in var", param);
                        //flowQfilter.and("depemp.position.tdkw_jobgradehr", QCP.equals, ((DynamicObject) value).getLong("id"));
                    }
                    break;
                case "tdkw_jobseq":
                    //岗位序列
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
//                        finish = finish.filter("depemp.position.tdkw_jobseqhr in var", param);
                    }
                    break;
                //todo 教育经历
                case "tdkw_degree":
                    //学历
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_rptdegree in var", param);
                    }
                    break;
                // todo 院校类别 要查教育经历的 冗余字段毕业院校hbss_college 用院校类别去筛选毕业院校
                case "tdkw_schooltype":
                    //院校类别
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        collegeTypeQfilter.and("collegecharact.fbasedataid", QCP.in, ids);
                        //根据院校类型查院校
                        collegeTypeQfilter.and("id", QCP.in, schoolIds);
                        //查出根据院校类型过滤出来的符合条件的院校
                        DynamicObjectCollection schools = QueryServiceHelper.query("hbss_college", "id", collegeTypeQfilter.toArray());
                        schoolIds = schools.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        //进行过滤
                        param.put("var", schoolIds);
                        finish = finish.filter("tdkw_collegerpt in var", param);
                    }
                    break;

                //todo 人员时序性属性 hrpi_pertsprop
                case "tdkw_marriage":
                    //婚育
                    if (ObjectUtils.isNotEmpty(value)) {
                        long id = ((DynamicObject) value).getLong("id");
                        param.put("var", id);
                        finish = finish.filter("tdkw_rptmarriagestatus =var", param);
                        //perTsPropQfilter.and("marriagestatus", QCP.equals, ((DynamicObject) value).getLong("id"));
                    }
                    break;

                //todo 服务年限
                case "tdkw_social_workage":
                    //起始社会工龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_rptsocialage >= var", param);
                        //  perserlenQfilter.and("socialworkage", QCP.large_equals, value);
                    }
                    break;
                case "tdkw_social_workagen":
                    //终止社会工龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_rptsocialage <= var", param);
                        // perserlenQfilter.and("socialworkage", QCP.less_equals, value);
                    }
                    break;
                case "tdkw_comagestart":
                    //起始集团司龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_rptcomsercount >= var", param);
                        perserlenQfilter.and("tdkw_newjointime", QCP.large_equals, value);
                    }
                    break;
                case "tdkw_comageend":
                    //终止集团司龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_rptcomsercount <= var", param);
                        perserlenQfilter.and("tdkw_newjointime", QCP.less_equals, value);
                    }
                    break;
                case "tdkw_company_agestart":
                    //起始公司司龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_serviceyear >= var", param);
                        //perserlenQfilter.and("tdkw_serviceyear", QCP.large_equals, value);
                    }
                    break;
                case "tdkw_company_ageend":
                    //终止公司司龄
                    if ((Integer) value != 0) {
                        BigDecimal bigDecimal = new BigDecimal(value.toString());
                        param.put("var", bigDecimal);
                        finish = finish.filter("tdkw_serviceyear <= var", param);
                        // perserlenQfilter.and("tdkw_serviceyear", QCP.less_equals, value);
                    }
                    break;
                //todo 人员非时序性属性
                case "tdkw_agestart":
                    //起始年龄
                    if ((Integer) value != 0) {
                        param.put("var", value);
                        finish = finish.filter("tdkw_rptage >= var", param);
                        // pernontsprQfilter.and("age", QCP.large_equals, value);
                    }
                    break;
                case "tdkw_ageend":
                    //终止年龄
                    if ((Integer) value != 0) {
                        param.put("var", value);
                        finish = finish.filter("tdkw_rptage <= var", param);
                        //pernontsprQfilter.and("age", QCP.less_equals, value);
                    }
                    break;
                case "tdkw_gender":
                    //性别
                    if (ObjectUtils.isNotEmpty(value)) {
                        long id = ((DynamicObject) value).getLong("id");
                        param.put("var", id);
                        finish = finish.filter("tdkw_rptgender = var", param);
                        //    pernontsprQfilter.and("gender", QCP.equals, ((DynamicObject) value).getLong("id"));
                    }
                    break;
                case "tdkw_workpalce":
                    //工作地点
                    if (StringUtils.isNotBlank((String) value)) {
                        finish = finish.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String address = row.getString("tdkw_contrworkloc.tdkw_detailedaddress");
                                return address.contains((CharSequence) value);
                            }
                        });
                        // pernontsprQfilter.and("tdkw_contrworkloc.tdkw_detailedaddress", QCP.like, "%" + value + "%");
                    }
                    break;
                //todo 基本信息补充
                case "tdkw_political_status":
                    //政治面貌
                    if (ObjectUtils.isNotEmpty(value)) {
                        List<Long> ids = ((DynamicObjectCollection) value).stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                        param.put("var", ids);
                        finish = finish.filter("tdkw_rptpoliticalstatus in var", param);
                    }
                    break;
                case "tdkw_famwork":
                    //家属工作单位
                    if (StringUtils.isNotBlank((String) value)) {
                        //将符合条件的人员id添加到set集合里
                        Set<Long> finishSet = homDynamicCollection.stream().filter(i -> i.getString("workunit") != null && i.getString("workunit").contains((CharSequence) value)).map(i -> i.getLong("person.id")).collect(Collectors.toSet());
                        //第二种方法
                        homDataSet.filter(new FilterFunction() {
                            @Override
                            public boolean test(Row row) {
                                String workPlace = row.getString("workunit");
                                if (workPlace.contains((CharSequence) value)) {
                                    Long id = row.getLong("person.id");
                                    personSet.add(id);
                                    return true;
                                }
                                return false;
                            }
                        });
                        // 对finish进行人员id过滤
                        param.put("var", finishSet);
                        finish = finish.filter("person.id in var", param);
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
                //  毕业院校
                case "tdkw_school":
                    if (StringUtils.isNotBlank((CharSequence) value)) {
                        finish = finish.where("tdkw_schoolname like '%" + value + "%' or schoolrecord like '%" + value + "%'");
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
                default:
                    logger.info("未执行的过滤条件为:" + propName);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("离职退休报表查询共耗时:" + (end - start) + "毫秒");
        finish.orderBy(new String[]{"tdkw_index"});
        finish = finish.orderBy(new String[]{"sortcode"});
        return finish;
    }

    private static void versionControl(QFilter eduQfilter, QFilter perTsPropQfilter, QFilter perserlenQfilter, QFilter pernontsprQfilter,
                                       QFilter politicalQfilter, QFilter collegeTypeQfilter, QFilter erManFileQfilter) {
        eduQfilter.and("iscurrentversion", QCP.equals, "1");
        eduQfilter.and("datastatus", QCP.equals, "1");
//        eduQfilter.and("tdkw_ishighestcheck", QCP.equals, "1");
        collegeTypeQfilter.and("iscurrentversion", QCP.equals, "1");
        collegeTypeQfilter.and("datastatus", QCP.equals, "1");
        collegeTypeQfilter.and("enable", QCP.equals, "1");
        perTsPropQfilter.and("iscurrentversion", QCP.equals, "1");
        perTsPropQfilter.and("datastatus", QCP.equals, "1");
        perserlenQfilter.and("iscurrentversion", QCP.equals, "1");
        perserlenQfilter.and("datastatus", QCP.equals, "1");
        pernontsprQfilter.and("iscurrentversion", QCP.equals, "1");
        pernontsprQfilter.and("datastatus", QCP.equals, "1");
        politicalQfilter.and("iscurrentversion", QCP.equals, "1");
        politicalQfilter.and("datastatus", QCP.equals, "1");
        erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
        //erManFileQfilter.and("datastatus", QCP.equals,"1");
        erManFileQfilter.and("businessstatus", QCP.equals, "1");
        //全职任职
        // TODO 任职类型 hbss_postype 怎么全职任职是XY00001，明明是"1010_S",需要改了
//        erManFileQfilter.and("empposrel.postype.number", QCP.equals, "XY00001");
        erManFileQfilter.and("empposrel.postype.number", QCP.equals, "1010_S");
        //erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFileQfilter.and("empposrel.datastatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
    }

    private static final String entryNumber = "tdkw_departandretire_pc";

    private QFilter getHasRightOrg(QFilter orgQfilter) {
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), entryNumber);
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        logger.info("控权暂时使用tdkw_statisticentry");
        logger.info("离职退休报表是否有所有权限:" + hasAllOrgPerm);
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            logger.info("有权限的组织为:" + Arrays.toString(new List[]{hasPerOrg}));
            orgQfilter.and("adminorg", QCP.in, hasPerOrg);
        }
        return orgQfilter;
    }


}
