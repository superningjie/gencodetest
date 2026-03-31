package tdkw.hrmp.hrobs.common.myteam.common.orgscopeutil;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.haos.business.servicehelper.AdminOrgQueryServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.model.DimValueResult;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRUserRoleCacheUtils;
import tdkw.hrmp.hrobs.common.myteam.common.bean.TeamRequestVO;
import tdkw.hrmp.hrobs.common.myteam.common.bean.TeamResultMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GetOrgScopeUtil
 *
 * @author xxx
 * @date 2023/10/20
 */
public class GetTeamOrgScopeUtil {
    private static final Log logger = LogFactory.getLog(GetTeamOrgScopeUtil.class);

    /**
     * @param userId       UserServiceHelper.getCurrentUserId()
     * @param entityNumber 假实体标识
     * @param dimNumber    职能标识 如职位子序列为 jobfamilyhr 岗位标签为 gwbq001
     * @param wantOrg      只需要职位子序列入参true  需要职位子序列和权限入参false
     * @return 封装的结果集
     */
    public static TeamResultMap getTeamOrgScope(Long userId, String entityNumber, String dimNumber, Boolean wantOrg) {
        String appId = getAppId(entityNumber);
        String permItemId = PermissionStatus.View;
        DimValueResult dimValueResult = DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSBizDataPermissionService", "getEntityDimValue", userId, appId, entityNumber, permItemId, dimNumber);
        Set<String> dimValueIds = dimValueResult.getDimValueIds();
        List<Long> dimIds = dimValueIds.stream().map(Long::valueOf).collect(Collectors.toList());
        QFilter qFilter = new QFilter("id", "in", dimIds);
        DynamicObject[] positionSub = BusinessDataServiceHelper.load("hbjm_jobfamilyhr", "name,id", new QFilter[]{qFilter}, "number asc");
        TeamResultMap teamResultMap;
        if (wantOrg) {
            //需要返回组织
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, entityNumber);
            teamResultMap = new TeamResultMap(dimValueIds, dimValueIds.size(), positionSub, result);
        } else {
            //只返回职位子序列
            teamResultMap = new TeamResultMap(dimValueIds, dimValueIds.size(), positionSub);
        }
        return teamResultMap;
    }

    /**
     * @param algoKey       this.getClass.getNmae()
     * @param teamResultMap GetTeamOrgScopeUtil.getTeamOrgScope()
     * @param number        岗位标签的编码
     * @return 团队人员id集合
     */
    public static List<Long> getTeamPersons(String algoKey, TeamResultMap teamResultMap, String number) {
        AuthorizedOrgResult result = teamResultMap.getResult();
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        QFilter erManFileQfilter = new QFilter("1", QCP.equals, 1);
        //查出来哪些岗位的岗位标签包含选中的岗位标签
        //根据编码查出岗位标签的id
        List<Long> positionIds = getPositionIdsByNumber(number);
        erManFileQfilter.and("empposrel.position.id", QCP.in, positionIds);
        List<Long> hasPermOrgs = result.getHasPermOrgs();
        logger.info("团队经理人有权限的组织集合为：" + hasPermOrgs);
        if (hasAllOrgPerm) {

        } else {
            erManFileQfilter.and("empposrel.adminorg", QCP.in, hasPermOrgs);
        }
        erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
        erManFileQfilter.and("businessstatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.datastatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFileQfilter.and("empposrel.businessstatus", QCP.equals, "1");
        QFilter orFilter = new QFilter("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017").or("empposrel.tdkw_changereason.id", QCP.is_null, null);
        erManFileQfilter.and(orFilter);
        DataSet dataSet = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", "person.id", new QFilter[]{erManFileQfilter}, "tdkw_index");
        List<Long> personIds = ORM.create().toPlainDynamicObjectCollection(dataSet).stream().map(i -> i.getLong("person.id")).collect(Collectors.toList());
        logger.info("团队经理人团队人员id集合为：" + personIds);
        return personIds;
    }


    /**
     * 查询单据所在的应用的Id，有原生应用就取原生的，
     * 注意：这不是查实体发布的应用，
     * 会先查询缓存的
     *
     * @param entityNumber 单据实体标识
     * @return appId
     */
    private static String getAppId(String entityNumber) {
        String cacheType = HRUserRoleCacheUtils.getTypeEntityApp();
        //查询缓存中的数据
        String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, entityNumber);
        if (StringUtils.isEmpty(cacheVal)) {
            QFilter qFilter = new QFilter("number", "=", entityNumber);
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("bos_entityobject", "id,bizappid.id", qFilter.toArray());
            if (dynamicObject == null) {
                throw new KDBizException(String.format("该实体元数据:%s 不存在", entityNumber));
            }
            String bizappid = dynamicObject.getString("bizappid.id");
            String masterid = getBaseStr(bizappid, "id", "bos_devportal_bizapp", "masterid");
            if (StringUtils.isNotEmpty(masterid)) {
                bizappid = masterid;
            }
            //写入缓存
            HRUserRoleCacheUtils.putCache(cacheType, entityNumber, bizappid);
            return bizappid;
        } else {
            return cacheVal;
        }

    }

    /**
     * 查询基础资料 String类型的字段
     *
     * @param param      条件参数
     * @param property   条件字段
     * @param entityName 基础资料标识
     * @param select     查询字段
     * @return 返回 String
     */
    public static String getBaseStr(Object param, String property, String entityName, String select) {
        Object base = getBase(param, property, entityName, select, true, QCP.equals);
        if (base instanceof DynamicObjectCollection) {
            DynamicObjectCollection collection = (DynamicObjectCollection) base;
            return collection.size() > 0 ? collection.get(0).getString(select) : null;
        } else {
            return (String) base;
        }
    }

    private static Object getBase(Object param, String property, String entity, String select, boolean isString, String qcp) {
        Object obj;
        if (isString) {
            obj = null;
        } else {
            obj = 0L;
        }
        if (Objects.isNull(param)) {
            return obj;
        }
        if (param instanceof String) {
            if (StringUtils.isEmpty((String) param)) {
                return obj;
            }
        } else if (param instanceof Long) {
            if ((Long) param == 0L) {
                return obj;
            }
        }
        QFilter qf = new QFilter(property, qcp, param);
        return QueryServiceHelper.query(entity, select, qf.toArray());
    }

    /**
     * pc端报表查询
     *
     * @param requestVO
     * @return
     */
    public static DataSet getResultOfPc(TeamRequestVO requestVO) {
        String partTime = System.getProperty("constant.hrmp.hbss.tdkw_hbss_changereason.fictitious");
        if (kd.bos.orm.util.StringUtils.isEmpty(partTime)) {
            partTime = "XY00017";
        }

        String educationSql = "left join(select edu.feducationid,edu.fschoolrecord,edu.fpersonid,edu.fgraduateschool,edu.fmajor from t_hrpi_pereduexp edu where edu.fiscurrentversion = '1'\n" +
//                "and edu.fk_tdkw_ishighestcheck = '1' and edu.fdatastatus = '1' ";
                // TODO 查旬标品的最高学历吧
                "and edu.fishighestdegree = '1' and edu.fdatastatus = '1' ";
        String collegeSql = " left join(select fid,fname from t_hbss_college  where fiscurrentversion = '1') col on e.fgraduateschool = col.fid";
//        String sql = "/*dialect*/   select a.fpersonid person,a.fadminorgid adminorg,a.fpostypeid postype,a.fcompanyid company,a.fpositionid as position,b.fname,c.fname,d.fage age,per.fk_tdkw_newjointime entservicelen,per.fsocialworkage servicelen,e.feducationid education,er.fk_tdkw_index tdkw_index,a.fisprimary isprimary,e.fgraduateschool graduateschool,e.fmajor major,col.fname,"
        String sql = "/*dialect*/   select a.fpersonid person,a.fadminorgid adminorg,a.fpostypeid postype,a.fcompanyid company,a.fpositionid as position,b.fname,c.fname,d.fage age,per.fcomsercount entservicelen,per.fsocialworkage servicelen,e.feducationid education,er.fid tdkw_index,a.fisprimary isprimary,e.fgraduateschool graduateschool,e.fmajor major,col.fname,"
                + " case when col.fname = '其他院校' then e.fschoolrecord else col.fname end as schoolname"
                + " from t_hrpi_empposorgrel a left join t_hbpm_position b on a.fpositionid = b.fid "
                + "left join t_hrpi_person c on c.fid=a.fpersonid "
                + "left join t_hrpi_pernontsprop d on d.fpersonid = a.fpersonid "
                + "left join t_hrpi_perserlen per on per.fpersonid = a.fpersonid "
//                + "left join tk_tdkw_changereason ch on ch.fid = a.fk_tdkw_changereason "
                + "left join t_hrpi_ermanfile er on er.fempposrelid = a.fid ";
        educationSql = educationSql + " )e on e.fpersonid = a.fpersonid";
        educationSql = educationSql + collegeSql;
        String sqlFilter = " where a.fdatastatus = '1' and a.fbusinessstatus = '1' and a.fiscurrentversion = '1' "
                + " and b.fiscurrentversion = '1' and c.fiscurrentversion = '1' and d.fiscurrentversion = '1'"
                + "and per.fiscurrentversion = '1' and per.fbusinessstatus = '1' and per.fdatastatus = '1'" +
//                "and ch.fnumber != '" + partTime + "' and er.fiscurrentversion = '1' and er.fdatastatus = '1'";
                " and er.fiscurrentversion = '1' and er.fdatastatus = '1'";
        if (requestVO.getTeam() != null && requestVO.getTeam().compareTo(0l) != 0) {
            // 标准化 getPositionIdsById(requestVO.getTeam());
            List<Long> positionIds = new ArrayList<>();
            if (ObjectUtils.isNotEmpty(positionIds) && positionIds.size() != 0) {
                sqlFilter = sqlFilter + " and b.fdatastatus = '1' and b.fid in" + positionIds + " ";
                sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
            }
        }
        if (StringUtils.isNotEmpty(requestVO.getTeamNumber())) {
            List<Long> positionIds = getPositionIdsByNumber(requestVO.getTeamNumber());
            if (ObjectUtils.isNotEmpty(positionIds) && positionIds.size() != 0) {
                sqlFilter = sqlFilter + " and b.fdatastatus = '1' and b.fid in" + positionIds + " ";
                sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
            }
        }
        if ((requestVO.getPositionList() != null && requestVO.getPositionList().size() > 0) || (requestVO.getAdminorg() != null && requestVO.getAdminorg().compareTo(0l) != 0)) {
            Long organizationFilter = requestVO.getAdminorg();
            List<Long> positionList = requestVO.getPositionList();
            Long departmentFilter = requestVO.getDepartment();
            if (requestVO.getPositionList() != null && requestVO.getPositionList().size() > 0){
                sqlFilter = sqlFilter + " and a.fadminorgid in " + positionList + " ";
            }
            // 2025-10-31 公司过滤
            if (organizationFilter != null && organizationFilter.compareTo(0l) != 0) {
                List<Long> orgList = new ArrayList<>();
                orgList.add(organizationFilter);
                List<Map<String, Object>> subOrgs = AdminOrgQueryServiceHelper.batchQueryAllSubOrg(orgList, new Date());
                List<Long> subOrgIds = subOrgs.stream().map(i -> (Long) i.get("orgId")).collect(Collectors.toList());
                subOrgIds.add(organizationFilter);
                List<Long> allPersonByOrg = getAllPersonByOrg(organizationFilter);
                subOrgIds.addAll(allPersonByOrg);
                sqlFilter = sqlFilter + " and a.fcompanyid in " + subOrgIds + " ";

            }
            sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
            // 2025-10-31 部门过滤
            if (departmentFilter != null && departmentFilter.compareTo(0l) != 0) {
                sqlFilter = sqlFilter + " and a.fadminorgid = " + departmentFilter + " ";
            }

        }
        sql = sql + educationSql + sqlFilter;
        if (requestVO.getTeam() != null && requestVO.getTeam().compareTo(0l) != 0) {
            Long educationFilter = requestVO.getEducation();
            List<Long> postypeFilter = requestVO.getPostype();
            String personName = requestVO.getName();
            String isPartTime = requestVO.getIsPartTime();
            int ageFilterStart = requestVO.getAgeStart();
            int ageFilterEnd = requestVO.getAgeEnd();
            double seniorityFilterStart = requestVO.getSeniorityStart();
            double seniorityFilterEnd = requestVO.getSeniorityEnd();
            double societyAgeFilterStart = requestVO.getSocialWorkAgeStart();
            double societyAgeFilterEnd = requestVO.getSocialWorkAgeEnd();


            if (educationFilter != null && educationFilter.compareTo(0l) != 0) {
                //过滤学历
                sql = sql + " and e.feducationid = " + educationFilter;
            }
            if (postypeFilter != null && !kd.bos.dataentity.utils.ObjectUtils.isEmpty(postypeFilter)) {
                //过滤任职类型
                String idStr = postypeFilter.stream().map(String::valueOf).collect(Collectors.joining(","));
                sql = sql + " and a.fpostypeid in (" + idStr + ") ";
            }
//            if (requestVO.getSchool() != null && requestVO.getSchool().compareTo(0l) != 0) {
//                sql = sql + " and e.fgraduateschool = " + requestVO.getSchool() + " ";
//            }
            if (StringUtils.isNotEmpty(requestVO.getSchoolName())) {
                sql = sql + " and (case when col.fname = '其他院校' then e.fschoolrecord else col.fname end) like '%" + requestVO.getSchoolName() + "%' ";
            }
            if (StringUtils.isNotEmpty(requestVO.getSpeciality())) {
                sql = sql + " and e.fmajor like '%" + requestVO.getSpeciality() + "%' ";
            }
            if (!StringUtils.isEmpty(personName)) {
                //过滤姓名
                sql = sql + " and c.fname like '%" + personName + "%'";
            }
            if (!StringUtils.isEmpty(isPartTime)) {
                //过滤姓名
                sql = sql + " and a.fisprimary = '1'";
            }
            if (ageFilterStart != 0) {
                //过滤年龄
                sql = sql + " and d.fage >= " + ageFilterStart;
            }
            if (ageFilterEnd != 0) {
                //过滤年龄
                sql = sql + " and d.fage <= " + ageFilterEnd;
            }
            if (seniorityFilterStart > 0d) {
                //过滤司龄
                sql = sql + " and per.fk_tdkw_newjointime >= " + seniorityFilterStart;
            }
            if (seniorityFilterEnd > 0d) {
                //过滤司龄
                sql = sql + " and per.fk_tdkw_newjointime <= " + seniorityFilterEnd;
            }
            if (societyAgeFilterStart > 0d) {
                //过滤工龄
                sql = sql + " and per.fsocialworkage >= " + societyAgeFilterStart;
            }
            if (societyAgeFilterEnd > 0d) {
                //过滤工龄
                sql = sql + " and per.fsocialworkage <= " + societyAgeFilterEnd;
            }
            logger.info("下属列表sql" + sql);
            DataSet CareerExperiences = DB.queryDataSet("queryFuntionalTeamsPC", DBRoute.of("hr"), sql, null).orderBy(new String[]{"tdkw_index asc"});

            return CareerExperiences;
        }
        return null;
    }


    /**
     * 职能团队pc端报表查询
     *
     * @param requestVO
     * @return
     */
    public static DataSet getBusTeamResultOfPc(TeamRequestVO requestVO) {
        String partTime = System.getProperty("constant.hrmp.hbss.tdkw_hbss_changereason.fictitious");
        if (kd.bos.orm.util.StringUtils.isEmpty(partTime)) {
            partTime = "XY00017";
        }

        String educationSql = "left join(select edu.feducationid,edu.fschoolrecord,edu.fpersonid,edu.fgraduateschool,edu.fmajor from t_hrpi_pereduexp edu where edu.fiscurrentversion = '1'\n" +
                "and edu.fk_tdkw_ishighestcheck = '1' and edu.fdatastatus = '1' ";
        String collegeSql = " left join(select fid,fname from t_hbss_college  where fiscurrentversion = '1') col on e.fgraduateschool = col.fid";
        String sql = "/*dialect*/   select a.fpersonid tdkw_person,a.fadminorgid tdkw_department,a.fpostypeid postype,a.fcompanyid tdkw_company,a.fpositionid as tdkw_mainpositions,b.fname,c.fname,d.fage tdkw_age,per.fk_tdkw_newjointime tdkw_entservicelen,per.fsocialworkage tdkw_servicelen,e.feducationid tdkw_educate,er.fk_tdkw_index tdkw_index,a.fisprimary isprimary,e.fgraduateschool tdkw_schoolrecord,e.fmajor tdkw_specialityname,col.fname,"
                + " case when col.fname = '其他院校' then e.fschoolrecord else col.fname end as tdkw_schoolname"
                + " from t_hrpi_empposorgrel a left join t_hbpm_position b on a.fpositionid = b.fid "
                + "left join t_hrpi_person c on c.fid=a.fpersonid "
                + "left join t_hrpi_pernontsprop d on d.fpersonid = a.fpersonid "
                + "left join t_hrpi_perserlen per on per.fpersonid = a.fpersonid "
                + "left join tk_tdkw_changereason ch on ch.fid = a.fk_tdkw_changereason " +
                "left join t_hrpi_ermanfile er on er.fempposrelid = a.fid ";
        educationSql = educationSql + " )e on e.fpersonid = a.fpersonid";
        educationSql = educationSql + collegeSql;
        String sqlFilter = " where a.fdatastatus = '1' and a.fbusinessstatus = '1' and a.fiscurrentversion = '1' "
                + " and b.fiscurrentversion = '1' and c.fiscurrentversion = '1' and d.fiscurrentversion = '1'"
                + "and per.fiscurrentversion = '1' and per.fbusinessstatus = '1' and per.fdatastatus = '1'" +
                "and ch.fnumber != '" + partTime + "' and er.fiscurrentversion = '1' and er.fdatastatus = '1'";
        if (requestVO.getTeam() != null && requestVO.getTeam().compareTo(0L) != 0) {
            List<Long> positionIds = getPositionIdsById(requestVO.getTeam());
            if (ObjectUtils.isNotEmpty(positionIds) && positionIds.size() != 0) {
                sqlFilter = sqlFilter + " and b.fdatastatus = '1' and b.fid in" + positionIds + " ";
                sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
            }
        }
        if (StringUtils.isNotEmpty(requestVO.getTeamNumber())) {
            List<Long> positionIds = getPositionIdsByNumber(requestVO.getTeamNumber());
            if (ObjectUtils.isNotEmpty(positionIds) && positionIds.size() != 0) {
                sqlFilter = sqlFilter + " and b.fdatastatus = '1' and b.fid in" + positionIds + " ";
                sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
            }
        }
        if ((requestVO.getPositionList() != null && requestVO.getPositionList().size() > 0) || (requestVO.getAdminorg() != null && requestVO.getAdminorg().compareTo(0L) != 0)) {
            Long organizationFilter = requestVO.getAdminorg();
            List<Long> positionList = requestVO.getPositionList();
            if (requestVO.getPositionList() != null && requestVO.getPositionList().size() > 0){
                sqlFilter = sqlFilter + " and a.fadminorgid in " + positionList + " ";
            }
            if (organizationFilter != null && organizationFilter.compareTo(0L) != 0) {
                List<Long> orgList = new ArrayList<>();
                orgList.add(organizationFilter);
                List<Map<String, Object>> subOrgs = AdminOrgQueryServiceHelper.batchQueryAllSubOrg(orgList, new Date());
                List<Long> subOrgIds = subOrgs.stream().map(i -> (Long) i.get("orgId")).collect(Collectors.toList());
                subOrgIds.add(organizationFilter);
                List<Long> allPersonByOrg = getAllPersonByOrg(organizationFilter);
                subOrgIds.addAll(allPersonByOrg);
                sqlFilter = sqlFilter + " and a.fadminorgid in " + subOrgIds + " ";

            }
            sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
        }
        sql = sql + educationSql + sqlFilter;
        if (requestVO.getTeam() != null && requestVO.getTeam().compareTo(0L) != 0) {
            Long educationFilter = requestVO.getEducation();
            List<Long> postypeFilter = requestVO.getPostype();
            String personName = requestVO.getName();
            String isPartTime = requestVO.getIsPartTime();
            int ageFilterStart = requestVO.getAgeStart();
            int ageFilterEnd = requestVO.getAgeEnd();
            double seniorityFilterStart = requestVO.getSeniorityStart();
            double seniorityFilterEnd = requestVO.getSeniorityEnd();
            double societyAgeFilterStart = requestVO.getSocialWorkAgeStart();
            double societyAgeFilterEnd = requestVO.getSocialWorkAgeEnd();


            if (educationFilter != null && educationFilter.compareTo(0L) != 0) {
                //过滤学历
                sql = sql + " and e.feducationid = " + educationFilter;
            }
            if (postypeFilter != null && !kd.bos.dataentity.utils.ObjectUtils.isEmpty(postypeFilter)) {
                //过滤任职类型
                String idStr = postypeFilter.stream().map(String::valueOf).collect(Collectors.joining(","));
                sql = sql + " and a.fpostypeid in (" + idStr + ") ";
            }
//            if (requestVO.getSchool() != null && requestVO.getSchool().compareTo(0l) != 0) {
//                sql = sql + " and e.fgraduateschool = " + requestVO.getSchool() + " ";
//            }
            if (StringUtils.isNotEmpty(requestVO.getSchoolName())) {
                sql = sql + " and (case when col.fname = '其他院校' then e.fschoolrecord else col.fname end) like '%" + requestVO.getSchoolName() + "%' ";
            }
            if (StringUtils.isNotEmpty(requestVO.getSpeciality())) {
                sql = sql + " and e.fmajor like '%" + requestVO.getSpeciality() + "%' ";
            }
            if (!StringUtils.isEmpty(personName)) {
                //过滤姓名
                sql = sql + " and c.fname like '%" + personName + "%'";
            }
            if (!StringUtils.isEmpty(isPartTime)) {
                //过滤姓名
                sql = sql + " and a.fisprimary = '1'";
            }
            if (ageFilterStart != 0) {
                //过滤年龄
                sql = sql + " and d.fage >= " + ageFilterStart;
            }
            if (ageFilterEnd != 0) {
                //过滤年龄
                sql = sql + " and d.fage <= " + ageFilterEnd;
            }
            if (seniorityFilterStart > 0d) {
                //过滤司龄
                sql = sql + " and per.fk_tdkw_newjointime >= " + seniorityFilterStart;
            }
            if (seniorityFilterEnd > 0d) {
                //过滤司龄
                sql = sql + " and per.fk_tdkw_newjointime <= " + seniorityFilterEnd;
            }
            if (societyAgeFilterStart > 0d) {
                //过滤工龄
                sql = sql + " and per.fsocialworkage >= " + societyAgeFilterStart;
            }
            if (societyAgeFilterEnd > 0d) {
                //过滤工龄
                sql = sql + " and per.fsocialworkage <= " + societyAgeFilterEnd;
            }
            logger.info("职能团队下属列表sql" + sql);
            DataSet CareerExperiences = DB.queryDataSet("getBusTeamResultOfPc", DBRoute.of("hr"), sql, null).orderBy(new String[]{"tdkw_index asc"});

            return CareerExperiences;
        }
        return null;
    }



    public static List<Long> getPositionIdsByNumber(String requestVOTeamNumber) {
        //查出来哪些岗位的岗位标签包含选中的岗位标签
        //根据编码查出岗位标签的id
        QFilter postTagFilter = new QFilter("number", QCP.equals, requestVOTeamNumber);
        postTagFilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("hbjm_jobfamilyhr", "id", postTagFilter.toArray());
        //查出带有要求岗位标签的岗位
        QFilter qFilter = new QFilter("tdkw_postiontag.fbasedataid", QCP.in, dynamicObject.getPkValue());
        DynamicObject[] load = BusinessDataServiceHelper.load("hbpm_positionhr", "id", new QFilter[]{qFilter});
        List<Long> positionIds = Arrays.stream(load).map(i -> i.getLong("id")).collect(Collectors.toList());
        logger.info("查出来的匹配岗位标签的岗位id为:" + positionIds);
        return positionIds;
    }

    public static List<Long> getPositionIdsById(Long requestVOTeamId) {
        //查出带有要求岗位标签的岗位
        QFilter qFilter = new QFilter("tdkw_postiontag.fbasedataid", QCP.in, requestVOTeamId);
        DynamicObject[] load = BusinessDataServiceHelper.load("hbpm_positionhr", "id", new QFilter[]{qFilter});
        List<Long> positionIds = Arrays.stream(load).map(i -> i.getLong("id")).collect(Collectors.toList());
        logger.info("查出来的匹配岗位标签的岗位id为:" + positionIds);
        return positionIds;
    }

    /**
     * app端查询报表
     *
     * @param requestVO
     * @return
     */
    public static List<Long> getResultOfApp(TeamRequestVO requestVO) {
        logger.info("查询人员列表");
        String partTime = System.getProperty("constant.hrmp.hbss.tdkw_hbss_changereason.fictitious");
        List<Long> personList = new ArrayList<>();
        if (kd.bos.orm.util.StringUtils.isEmpty(partTime)) {
            partTime = "XY00017";
        }

        String educationSql = "left join(select edu.feducationid,edu.fpersonid from t_hrpi_pereduexp edu where edu.fiscurrentversion = '1'\n" +
                "and edu.fk_tdkw_ishighestcheck = '1' ";

        String sql = "select a.fpersonid person,a.fadminorgid adminorg,a.fcompanyid company,a.fpositionid as position,b.fname,c.fname,d.fage age,per.fcomsercount entservicelen,per.fsocialworkage servicelen,e.feducationid education,er.fk_tdkw_index tdkw_index,a.fisprimary isprimary"
                + " from t_hrpi_empposorgrel a left join t_hbpm_position b on a.fpositionid = b.fid "
                + "left join t_hrpi_person c on c.fid=a.fpersonid "
                + "left join t_hrpi_pernontsprop d on d.fpersonid = a.fpersonid "
                + "left join t_hrpi_perserlen per on per.fpersonid = a.fpersonid "
                + "left join tk_tdkw_changereason ch on ch.fid = a.fk_tdkw_changereason " +
                "left join t_hrpi_ermanfile er on er.fempposrelid = a.fid " +
                "left join t_hbjm_jobfamily jo on jo.fid = b.fk_tdkw_jobfamilyhr ";
        educationSql = educationSql + " )e on e.fpersonid = a.fpersonid";
        String sqlFilter = " where a.fdatastatus = '1' and a.fbusinessstatus = '1' and a.fiscurrentversion = '1' "
                + " and b.fiscurrentversion = '1' and c.fiscurrentversion = '1' and d.fiscurrentversion = '1'"
                + "and per.fiscurrentversion = '1' and per.fbusinessstatus = '1' and per.fdatastatus = '1'" +
                "and ch.fnumber != '" + partTime + "' and er.fiscurrentversion = '1' and er.fdatastatus = '1'";

        if (StringUtils.isNotEmpty(requestVO.getTeamNumber())) {
            List<Long> positionIdsByNumber = getPositionIdsByNumber(requestVO.getTeamNumber());
            if (ObjectUtils.isNotEmpty(positionIdsByNumber) && positionIdsByNumber.size() != 0) {
                logger.info("查出来的匹配岗位标签的岗位id为:" + positionIdsByNumber);
                sqlFilter = sqlFilter + " and jo.fiscurrentversion = '1' and a.fpositionid in " + positionIdsByNumber + " ";
                sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
            }
        }
        if (requestVO.getPositionList() != null && requestVO.getPositionList().size() > 0) {
            sqlFilter = sqlFilter + " and a.fadminorgid in" + requestVO.getPositionList() + " ";
            sqlFilter = sqlFilter.replace("[", "(").replace("]", ")");
        }
        sql = sql + educationSql + sqlFilter;
        String personName = requestVO.getName();

        if (!StringUtils.isEmpty(personName)) {
            //过滤姓名
            sql = sql + " and c.fname like '%" + personName + "%'";
        }
        logger.info("下属列表sql" + sql);
        DataSet CareerExperiences = DB.queryDataSet("queryFuntionalTeamsApp", DBRoute.of("hr"), sql, null).orderBy(new String[]{"tdkw_index asc"});

        personList = ORM.create().toPlainDynamicObjectCollection(CareerExperiences).stream().map(map -> map.getLong("person")).collect(Collectors.toList());
        return personList;
    }

    public static List<Long> getAllPersonByOrg(Long orgId) {

        //组织集合（获取下级组织）
        List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(orgId.toString());

        //获取权限信息
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_myteam_positionson");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //全组织查看权限
        if (!hasAllOrgPerm) {
            //权限组织
            List<Long> hasPerOrg = result.getHasPermOrgs();
            //获取普通组织与权限组织的交集
            allBelowHROrg.retainAll(hasPerOrg);
        }

        return allBelowHROrg;
    }
}
