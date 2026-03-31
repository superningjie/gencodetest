package tdkw.hrmp.hrobs.common.hrobs.util;

import com.google.common.collect.Lists;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.openservicehelper.hrpi.HRPIPersonServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/6/30 10:54
 * @Description 人员详情信息 工具类
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Version 1.0
 **/
public class PersonInfoUtil {
    public static Log logger = LogFactory.getLog(PersonInfoUtil.class);

    /**
     * @author xxx
     * @Description 获取人员对应的人事业务档案
     * @Parameter List<Long> personIds HR人员信息id
     * @Parameter Date 日期
     * @Date 2023/7/7 10:58
     */
    public static List<Long> getAllErManFileByPerson(List<Long> personIds) {
        QFilter personFiler = new QFilter("person.id", QCP.in, personIds);
        personFiler.and("businessstatus", QCP.equals, "1");
        personFiler.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        personFiler.and("filetype.postype.number", QCP.equals, "XY00001");
        personFiler.and("empposrel.businessstatus", QCP.equals, "1");
        personFiler.and("empposrel.datastatus", QCP.equals, "1");
        personFiler.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);

        logger.info("人员信息查询SQL：" + personFiler.toString());
//        personFiler.and("empposrel.company.id", QCP.in, subOrgIds);

        // 人事业务档案
        DynamicObjectCollection hspm_ermanfile = QueryServiceHelper.query("hspm_ermanfile", "id,person.id", personFiler.toArray());
        logger.info("人员信息（人事业务档案）查询结果：" + hspm_ermanfile.size());
        List<Long> erManFileIds = hspm_ermanfile.stream().map(i -> (Long) i.get("id")).collect(Collectors.toList());

        return erManFileIds;
    }

    /**
     * @author xxx
     * @Description 获取组织下所有人员
     * @Parameter Long orgId HR行政组织id
     * @Parameter Date 日期
     * @Date 2023/7/7 9:41
     */
    public static List<Long> getAllPersonByOrg(Long orgId, Date date) {

        //组织集合（获取下级组织）
        List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(orgId.toString());

        //获取权限信息
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ry_zrs_pc");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //全组织查看权限
        if (!hasAllOrgPerm) {
            //权限组织
            List<Long> hasPerOrg = result.getHasPermOrgs();
            //获取普通组织与权限组织的交集
            allBelowHROrg.retainAll(hasPerOrg);
        }
//        //根据组织与日期过滤人员信息
//        DynamicObject[] currentPersonByOrg = PeopleCountingUtils.getCurrentPersonByOrgs(allBelowHROrg, date);
//
//        return Arrays.stream(currentPersonByOrg).map(object -> (Long) object.get("person.id")).collect(Collectors.toList());
//
//        QFilter filter = new QFilter("empposrel.adminorg", QCP.in, allBelowHROrg);
        //日期过滤
//        filter.and(new QFilter("empposrel.startdate", "<=", date).and("empposrel.enddate", ">=", date));
        //人员任职经历
        QFilter qFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and("isprimary", QCP.equals, "1");
        qFilter.and("businessstatus", QCP.equals, "1");

        //组织过滤
        qFilter.and("adminorg", QCP.in, allBelowHROrg);
        //日期过滤
        qFilter.and(new QFilter("startdate", "<=", date).and("enddate", ">=", date));
        DynamicObjectCollection loads = QueryServiceHelper.query("hrpi_empposorgrel", "person.id", qFilter.toArray());

        return loads.stream().map(object -> (Long) object.get("person.id")).collect(Collectors.toList());
    }

    /**
     * @author xxx
     * @Description 获取需要统计的行政组织
     * @Parameter Long orgId HR行政组织id
     * @Parameter Date 日期
     * @Date 2023/7/7 9:41
     */
    public static List<Long> getAllPersonByOrg(Long orgId) {

        //组织集合（获取下级组织）
        List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(orgId.toString());

        //获取权限信息
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ry_zrs_pc");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //全组织查看权限
        if (!hasAllOrgPerm) {
            //权限组织
            List<Long> hasPerOrg = result.getHasPermOrgs();
            //获取普通组织与权限组织的交集
            allBelowHROrg.retainAll(hasPerOrg);
        }
//        QFilter filter = new QFilter("empposrel.adminorg", QCP.in, allBelowHROrg);
//        //日期过滤
//        filter.and(new QFilter("empposrel.startdate", "<=", date).and("empposrel.enddate", ">=", date));

        return allBelowHROrg;
    }

    /**
     * @author xxx
     * @Description 获取人员与角色数据范围权限的人员
     * @Date 2023/8/15 14:25
     */
    public static List<Long> getHasRightPersons(List<Long> persons, String role) {
        //todo 控权组组织过滤,过滤当前组织
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), role);
        //如果包含10000L就返回true 不对组织进行过滤
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            //获取权限范围组织下所有人员id
            List<Map<String, Object>> personIdByOrg = HRPIPersonServiceHelper.getPersonByOrgs(hasPerOrg, new Date());
            List<Long> personIds = personIdByOrg.stream().map(i -> (Long) i.get("person")).collect(Collectors.toList());
            //取两次过滤人员id集合的交集
            persons.retainAll(personIds);
        }
        return persons;
    }

    /**
     * @author xxx
     * @Description 查询人员信息
     * @Parameter List<Long> personIds HR人员信息ids
     * @Parameter String algoKey
     * @Date 2023/6/27 14:43
     */
    public static DataSet queryPersonInfo(List<Long> personIds, String algoKey) {
        QFilter personFiler = new QFilter("person.id", QCP.in, personIds);
        QFilter erManFile = new QFilter("businessstatus", QCP.equals, "1");
//        QFilter erManFile = new QFilter("1", QCP.in, 1);
        erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and("filetype.number", QCP.in, new String[]{"1010_S", "1050_S", "1060_S", "1070_S", "1110_S", "1190_S"});
//        erManFile.and("empposrel.businessstatus", QCP.equals, "1");
        erManFile.and("empposrel.datastatus", QCP.equals, "1");
        erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and("empposrel.isprimary", QCP.equals, "1");

        // 人事业务档案
        DataSet hspm_ermanfile = ORM.create().queryDataSet(algoKey, "hspm_ermanfile", "person,empposrel.company," +
                "person.name,empposrel.position," +
                "pernontsprop.gender,pernontsprop.age,empposrel.adminorg,empposrel.adminorg.sortcode", new QFilter[]{personFiler, erManFile});

        QFilter perRegionFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        perRegionFilter.and("datastatus", QCP.equals, "1");
        // 基本信息补充
        DataSet hrpi_perregion = ORM.create().queryDataSet(algoKey, "hrpi_perregion", "person,politicalstatus,politicalstatus.name",
                new QFilter[]{personFiler, perRegionFilter});

        QFilter perTsPropFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员时序性属性
        DataSet hrpi_pertsprop = ORM.create().queryDataSet(algoKey, "hrpi_pertsprop", "person,marriagestatus",
                new QFilter[]{personFiler, perTsPropFilter});

        QFilter perserlenFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 服务年限基础页面
        DataSet hrpi_perserlen = ORM.create().queryDataSet(algoKey, "hrpi_perserlen", "person,joincomdate,socialworkage",
                new QFilter[]{personFiler, perserlenFilter});

        QFilter personInfoFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        personInfoFilter.and("datastatus", QCP.equals, "1");
        // 基本信息表单
        DataSet hspm_personinfo = ORM.create().queryDataSet(algoKey, "hspm_personinfo", "person",
                new QFilter[]{personFiler, personInfoFilter});

//        QFilter pereduexpFilter = new QFilter("tdkw_ishighestcheck", QCP.equals, "1");
//        pereduexpFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        QFilter pereduexpFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        pereduexpFilter.and("datastatus", QCP.equals, "1");
        // 教育经历
        DataSet hrpi_pereduexp = ORM.create().queryDataSet(algoKey, "hrpi_pereduexp", "person,education,graduateschool,major,schoolrecord,graduateschool.name",
                new QFilter[]{personFiler, pereduexpFilter});

        //人员非时序性属性
        DataSet hrpi_pernontsprop = ORM.create().queryDataSet(algoKey, "hrpi_pernontsprop", "person",
                new QFilter[]{personFiler, personInfoFilter});
        DataSet finish = hspm_ermanfile.leftJoin(hrpi_perregion).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg.sortcode", "politicalstatus.name",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "empposrel.adminorg").finish();
        finish = finish.leftJoin(hrpi_pertsprop).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg", "empposrel.adminorg.sortcode", "politicalstatus.name",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "marriagestatus").finish();
        finish = finish.leftJoin(hrpi_pereduexp).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg", "empposrel.adminorg.sortcode", "politicalstatus.name",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "marriagestatus", "education", "graduateschool", "graduateschool.name", "schoolrecord", "major").finish();
        finish = finish.leftJoin(hrpi_perserlen).on("person", "person").select("person", "empposrel.company as tdkw_company", "empposrel.adminorg as tdkw_dept", "empposrel.adminorg.sortcode", "politicalstatus.name",
                "person.name as tdkw_name", "empposrel.position as tdkw_position",
                "pernontsprop.gender as tdkw_gender", "pernontsprop.age as tdkw_age",
                "politicalstatus as tdkw_politicalstatus", "marriagestatus as tdkw_marriagestatus", "education as tdkw_education", "graduateschool as tdkw_graduateschool", "graduateschool.name", "schoolrecord", "major as tdkw_major",
                "joincomdate as tdkw_joincomdate", "socialworkage as tdkw_socialworkage").finish();
        finish = finish.leftJoin(hspm_personinfo).on("person", "person").select("person", "tdkw_company", "empposrel.adminorg.sortcode", "politicalstatus.name",
                "person as tdkw_person", "tdkw_name", "tdkw_position",  "tdkw_gender", "tdkw_age",
                "tdkw_politicalstatus", "tdkw_marriagestatus", "tdkw_education", "tdkw_graduateschool", "graduateschool.name", "schoolrecord", "tdkw_major",
                "tdkw_joincomdate",  "tdkw_socialworkage", "tdkw_dept").finish();
        finish = finish.leftJoin(hrpi_pernontsprop).on("person", "person").select("person", "tdkw_company", "empposrel.adminorg.sortcode", "politicalstatus.name",
                "person as tdkw_person", "tdkw_name", "tdkw_position", "tdkw_gender", "tdkw_age",
                "tdkw_politicalstatus", "tdkw_marriagestatus", "tdkw_education", "tdkw_graduateschool", "graduateschool.name", "schoolrecord", "tdkw_major",
                "tdkw_joincomdate",  "tdkw_socialworkage",  "tdkw_dept").finish();
        finish = finish.distinct();

        return finish;
    }

    public static DataSet queryPersonInfoDep(List<Long> personIds, String algoKey) {
        QFilter personFiler = new QFilter("person.id", QCP.in, personIds);
        QFilter erManFile = new QFilter("businessstatus", QCP.equals, "1");
        erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and("empposrel.isprimary", QCP.equals, "1");
        erManFile.and("empposrel.datastatus", QCP.equals, "1");
        erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and("hisversion", QCP.equals, "");

        // 人事业务档案
        DataSet hspm_ermanfile = ORM.create().queryDataSet(algoKey, "hspm_ermanfile", "person,person.number,empposrel.company," +
                "person.name,empposrel.position,empposrel.adminorg.sortcode," +
                "pernontsprop.gender,pernontsprop.age", new QFilter[]{personFiler, erManFile});

        //人员流入流出表
        /*DynamicObject[] changeTypes = BusinessDataServiceHelper.load("tdkw_hbss_changetype", "tdkw_chgevent", new QFilter("tdkw_chgevent.name", QCP.equals, "离职").toArray());
        List<Long> idList = new ArrayList<>();
        for (DynamicObject changeType : changeTypes) {
            Long pkValue = changeType.getLong("id");
            idList.add(pkValue);
        }*/
        QFilter flowQfilter = new QFilter("depemp.isprimary", QCP.equals, "1");
        flowQfilter.and("chgcategory.number", QCP.in, Lists.newArrayList("101020_S", "101200_S"));
        DataSet hpfs_personflow1 = ORM.create().queryDataSet(algoKey, "hpfs_personflow", "id,flowtime,person,depemp,laborreltype,employee", new QFilter[]{personFiler, flowQfilter});
        ArrayList<Long> personFlowIds = new ArrayList<>();
        ArrayList<String> uniquePersons = new ArrayList<>();
        //过滤相同person的单
        for (Row row : hpfs_personflow1) {
            if (uniquePersons.contains(row.getString("person"))) {
                continue;
            }
            personFlowIds.add(row.getLong("id"));
            uniquePersons.add(row.getString("person"));
        }
        DataSet hpfs_personflow = ORM.create().queryDataSet(algoKey, "hpfs_personflow", "id,person,depemp,laborreltype,employee", new QFilter("id", QCP.in, personFlowIds).toArray());
//        DataSet hpfs_personflow = ORM.create().queryDataSet(algoKey, "hpfs_personflow", "flowtime,person,tdkw_changereason,tdkw_changetype,depemp,laborreltype,employee", new QFilter[]{personFiler, personFlow});
        DataSet quitfileDataSet = ORM.create().queryDataSet(algoKey, "htm_quitfileinfo", "employee.id,contractenddate as flowtime", new QFilter[]{});
        hpfs_personflow = hpfs_personflow.leftJoin(quitfileDataSet).on("employee", "employee.id").select("id", "flowtime", "person",  "depemp", "laborreltype", "employee").finish();
        QFilter perRegionFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE).and("hisversion", QCP.equals, "").and("datastatus", QCP.equals, "1");
        // 基本信息补充
        DataSet hrpi_perregion = ORM.create().queryDataSet(algoKey, "hrpi_perregion", "person,politicalstatus",
                new QFilter[]{personFiler, perRegionFilter});

        QFilter perTsPropFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE).and("hisversion", QCP.equals, "").and("datastatus", QCP.equals, "1");
        // 人员时序性属性
        DataSet hrpi_pertsprop = ORM.create().queryDataSet(algoKey, "hrpi_pertsprop", "person,marriagestatus",
                new QFilter[]{personFiler, perTsPropFilter});

        // TODO 二开字段屏蔽
//        QFilter pereduexpFilter = new QFilter("tdkw_ishighestcheck", QCP.equals, Boolean.TRUE);
        QFilter pereduexpFilter = new QFilter("ishighestdegree", QCP.equals, Boolean.TRUE);
        pereduexpFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE).and("hisversion", QCP.equals, "").and("datastatus", QCP.equals, "1");
        // 教育经历
        DataSet hrpi_pereduexp = ORM.create().queryDataSet(algoKey, "hrpi_pereduexp", "person,education",
                new QFilter[]{personFiler, pereduexpFilter});

        QFilter perserlenFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE).and("hisversion", QCP.equals, "");
        // 服务年限基础页面
        DataSet hrpi_perserlen = ORM.create().queryDataSet(algoKey, "hrpi_perserlen", "person,joincomdate,comsercount,socialworkage",
                new QFilter[]{personFiler, perserlenFilter});

        QFilter personInfoFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        personInfoFilter.and("datastatus", QCP.equals, "1").and("hisversion", QCP.equals, "").and("datastatus", QCP.equals, "1");
        // 基本信息表单
        DataSet hspm_personinfo = ORM.create().queryDataSet(algoKey, "hspm_personinfo", "person",
                new QFilter[]{personFiler, personInfoFilter});
        //人员非时序性属性
        DataSet hrpi_pernontsprop = ORM.create().queryDataSet(algoKey, "hrpi_pernontsprop", "person",
                new QFilter[]{personFiler, personInfoFilter});
        DataSet finish = hspm_ermanfile.leftJoin(hrpi_perregion).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg.sortcode", "person.number",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus").finish();
        finish = finish.leftJoin(hrpi_pertsprop).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg.sortcode", "person.number",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "marriagestatus").finish();
        finish = finish.leftJoin(hpfs_personflow).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg.sortcode", "person.number",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "marriagestatus", "employee",
                "laborreltype", "depemp", "flowtime" ).finish();
        finish = finish.leftJoin(hrpi_pereduexp).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg.sortcode", "person.number",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "marriagestatus", "education", "employee",
                "laborreltype", "depemp", "flowtime").finish();
        finish = finish.leftJoin(hrpi_perserlen).on("person", "person").select("person", "empposrel.company as tdkw_company", "empposrel.adminorg.sortcode", "person.number",
                "person.name as tdkw_name", "empposrel.position as tdkw_position",
                "pernontsprop.gender as tdkw_gender", "pernontsprop.age as tdkw_age",
                "politicalstatus as tdkw_politicalstatus", "marriagestatus as tdkw_marriagestatus", "education as tdkw_education",
                "joincomdate as tdkw_joincomdate", "socialworkage as tdkw_socialworkage",
                "employee as tdkw_employee", "laborreltype as tdkw_laborreltype", "depemp as tdkw_depemp", "flowtime as tdkw_flowtime").finish();
        finish = finish.leftJoin(hspm_personinfo).on("person", "person").select("tdkw_flowtime", "person", "tdkw_company", "empposrel.adminorg.sortcode", "person.number",
                "tdkw_name", "tdkw_position","tdkw_gender", "tdkw_age",
                "tdkw_politicalstatus", "tdkw_marriagestatus", "tdkw_education",
                "tdkw_joincomdate", "tdkw_socialworkage",  "tdkw_employee", "tdkw_laborreltype",
                "tdkw_depemp").finish();
        finish = finish.leftJoin(hrpi_pernontsprop).on("person", "person").select("tdkw_flowtime", "person", "tdkw_company", "empposrel.adminorg.sortcode", "person.number",
                "tdkw_name", "tdkw_position", "tdkw_gender", "tdkw_age",
                "tdkw_politicalstatus", "tdkw_marriagestatus", "tdkw_education",
                "tdkw_joincomdate", "tdkw_socialworkage",  "tdkw_employee", "tdkw_laborreltype",
                "tdkw_depemp").finish();

        return finish;
    }


    /**
     * @author xxx
     * @Description 获取人员档案信息稽核范围内的员工信息
     * @Date 2023/11/15
     */
    public static DataSet getPersonInfoIntegrity(QFilter filter, String algoKey) {
        filter.and("businessstatus", QCP.equals, "1");
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("filetype.postype.number", QCP.equals, "XY00001");
        filter.and("empposrel.datastatus", QCP.equals, "1");
        filter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        filter.and("empposrel.isprimary", QCP.equals, "1");
        filter.and("empposrel.businessstatus", QCP.equals, "1");
        filter.and("empposrel.initstatus", QCP.equals, "2");

        // 人事业务档案
        DataSet hspm_ermanfile = ORM.create().queryDataSet(algoKey, "hspm_ermanfile", "person,person.id,empposrel.company," +
                "person.name," +
                "empposrel.adminorg,empposrel.adminorg.id as adminorg.id, empposrel.position.name as position.name", new QFilter[]{filter});


        QFilter personInfoFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        personInfoFilter.and("datastatus", QCP.equals, "1");
        // 基本信息表单
        DataSet hspm_personinfo = ORM.create().queryDataSet(algoKey, "hspm_personinfo", "person",
                new QFilter[]{personInfoFilter});

        //职业信息
        DataSet hrpi_empentrel = ORM.create().queryDataSet(algoKey, "hrpi_empentrel", "person,laborrelstatus.number",
                new QFilter[]{personInfoFilter});

        DataSet finish = hspm_ermanfile.leftJoin(hspm_personinfo).on("person", "person").select("person", "person.id", "empposrel.company as tdkw_company",
                "person.name", "adminorg.id", "position.name", "empposrel.adminorg as tdkw_dept").finish();
        finish = finish.leftJoin(hrpi_empentrel).on("person", "person").select("person", "person.id", " tdkw_company",
                "person.name", "adminorg.id", "position.name", " tdkw_dept",  "laborrelstatus.number").finish();
        finish = finish.distinct();

        //人事业务档案
        return finish;
    }

    /**
     * 剔除主任职的所属板块=XXX地产，且岗位名称不包含“置业顾问
     *
     * @param personList
     * @return
     */
    public static List<DynamicObject> removeSectoridAndPosition(List<DynamicObject> personList) {
        //剔除主任职的所属板块=XXX地产，且岗位名称不包含“置业顾问
        List<DynamicObject> notPerson = new ArrayList<>();
        for (DynamicObject person : personList) {
            if ("XXX地产".equals(person.getString("tdkw_sectorid.name"))) {
                if (person.getString("position.name").contains("置业顾问")) {
                    notPerson.add(person);
                }
            }
        }
        personList.removeAll(notPerson);
        return personList;
    }
}
