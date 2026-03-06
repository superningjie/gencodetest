package tdkw.hrmp.hrobs.common.result;

import javafx.util.Pair;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.openservicehelper.hrpi.HRPIPersonServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.myteam.common.bean.TeamResultMap;
import tdkw.hrmp.hrobs.common.myteam.common.orgscopeutil.GetTeamOrgScopeUtil;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xxx
 * @date 2024/3/25
 * @description 人员简历跳转权限
 */
public class ResumePermissions {
    // 日志
    private static final Log logger = LogFactory.getLog(ResumePermissions.class);
    // 员工简历
    private final static String CURRICULUMVITAE = "tdkw_hrmp_curriculumvitae";
    // PC端领导自助通用权限
    private final static String LEAD_PC_ROLE = "XY_LEAD_PC_001";
    // APP端领导自助通用权限
    private final static String LEAD_APP_ROLE = "XY_LEAD_APP_001";
    // 移动端&PC端-业务汇报线-经理角色
    private final static String TEAM_APP_ROLE = "XY_BUS_MGR_APP_001";

    /**
     * 封装获取当前用户ID和HR用户ID的逻辑
     */
    private static Pair<Long, Long> getUserIds() {
        try {
            Long cqUser = UserServiceHelper.getCurrentUserId();
            Long hrUser = HRRoleAndPersonUtils.getHRUser(cqUser);
            return new Pair<>(cqUser, hrUser);
        } catch (Exception ex) {
            logger.warn("当前用户信息获取失败", ex);
            return null;
        }
    }

    /**
     * 封装获取当前用户ID和HR用户ID的逻辑
     */
    private static Pair<Long, Long> getUserIdsBatch(Long userId) {
        try {
            Long cqUser = HRRoleAndPersonUtils.getCQUser(userId);
            return new Pair<>(cqUser, userId);
        } catch (Exception ex) {
            logger.warn("当前用户信息获取失败", ex);
            return null;
        }
    }

    /**
     * 封装获取下属人员ID的逻辑
     */
    private static List<Long> getSubordinatePersonnelIds(Long hrId) {
        try {
            return Optional.of(HRRoleAndPersonUtils.getSubordinatePersonnel(hrId))
                    .map(List::stream)
                    .orElseGet(Stream::empty)
                    .mapToLong(Long::valueOf)
                    .boxed()
                    .distinct()
                    .collect(Collectors.toList());
        } catch (NullPointerException ex) {
            logger.warn("获取下属人员信息失败", ex);
            return new ArrayList<>();
        }
    }

    /**
     * 封装获取人员信息的逻辑
     */
    private static DynamicObjectCollection getPersons(List<Long> ids) {
        try {
            return QueryServiceHelper.query("hrpi_person", "tdkw_pkid",
                    new QFilter("id", QCP.in, ids)
                            .and("iscurrentversion", QCP.equals, "1")
                            .and("datastatus", QCP.equals, "1").toArray());
        } catch (Exception ex) {
            logger.warn("查询人员信息时发生错误", ex);
            return null;
        }
    }

    /**
     * 权限查询
     */
    public static boolean queryAuthority(String erfileId, String platform) {
        if (!isValidPlatform(platform)) {
            logger.warn("无效的平台参数: {}", platform);
            return false;
        }

        Pair<Long, Long> userIds = getUserIds();
        if (userIds == null) {
            logger.warn("获取用户信息失败");
            return false;
        }

        logger.info("当前用户信息：" + userIds);

        List<Long> ids = getSubordinatePersonnelIds(userIds.getValue());

        if (platform.equals("PC")) {
            DynamicObject person = QueryServiceHelper.queryOne("hrpi_person", "tdkw_pkid",
                    new QFilter("id", QCP.equals, userIds.getValue()).toArray());
            String pkid = person.getString("tdkw_pkid");
            if (StringUtils.equals(pkid, erfileId)) return true;

            boolean isRole = HRRoleAndPersonUtils.getIsRole(userIds.getKey(), LEAD_PC_ROLE);
            if (isRole && leaderQuery(ids, userIds.getKey())) return true;
        }
        if (platform.equals("APP")) {
            if (userIds.getValue().compareTo(Long.parseLong(erfileId)) == 0) return true;
            boolean isLead = HRRoleAndPersonUtils.getIsRole(userIds.getKey(), LEAD_APP_ROLE);
            if (isLead && leaderQuery(ids, userIds.getKey())) return true;
        }

        boolean isTeam = HRRoleAndPersonUtils.getIsRole(userIds.getKey(), TEAM_APP_ROLE);
        if (isTeam && functionalInquiry(userIds.getKey(), ids)) return true;

        logger.info("当前用户权限人员：" + ids);
        return platform.equals("PC") ? checkPersonExists(erfileId, ids) : ids.contains(Long.parseLong(erfileId));
    }

    /**
     * 职能团队权限查询
     *
     * @param userId 苍穹id
     * @param ids    权限人员
     * @return true：有权限     false:无权限
     */
    private static boolean functionalInquiry(Long userId, List<Long> ids) {
        TeamResultMap teamOrgScope = GetTeamOrgScopeUtil.getTeamOrgScope(userId, "tdkw_manageteam", "gwbq001", true);
        AuthorizedOrgResult authorizedOrgResult = teamOrgScope.getResult();
        if (authorizedOrgResult.isHasAllOrgPerm()) {
            return true;
        } else {
            setPerson(ids, authorizedOrgResult);
        }
        return false;
    }

    /**
     * 领导自助权限查询
     */
    private static boolean leaderQuery(List<Long> ids, Long userId) {
        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, CURRICULUMVITAE);
        if (authorizedAdminOrgSet.isHasAllOrgPerm()) {
            return true;
        } else {
            setPerson(ids, authorizedAdminOrgSet);
        }
        return false;
    }

    /**
     * 查询组织成员
     */
    private static void setPerson(List<Long> ids, AuthorizedOrgResult authorizedAdminOrgSet) {
        List<Long> hasPermOrgList = authorizedAdminOrgSet.getHasPermOrgs();
        List<Map<String, Object>> personByOrg = HRPIPersonServiceHelper.getPersonByOrgs(hasPermOrgList, new Date());
        // 查询流入流出表离职人员数据
        QFilter qFilter = new QFilter("tdkw_changetype.tdkw_chgevent.number", QCP.equals, "1070_S");
        QFilter orgFilter = new QFilter("adminorg", QCP.in, hasPermOrgList);
        DynamicObjectCollection erManFile = QueryServiceHelper.query("hpfs_personflow", "person.id", new QFilter[]{qFilter, orgFilter});
        // 合并人员id
        Stream<Long> leavePersonStream = erManFile.stream().mapToLong(dynamicObject -> dynamicObject.getLong("person.id")).boxed();
        Stream<Long> onJobPersonIdStream = personByOrg.stream()
                .filter(i -> Objects.nonNull(i.get("person")))
                .map(i -> Optional.ofNullable(i.get("person"))
                        .map(Object::toString)
                        .map(Long::valueOf)
                        .orElse(null))
                .filter(Objects::nonNull);
        List<Long> allPersonIds = Stream.concat(leavePersonStream, onJobPersonIdStream).collect(Collectors.toList());
        ids.addAll(allPersonIds);
    }

    /**
     * 校验平台参数
     */
    private static boolean isValidPlatform(String platform) {
        return "PC".equals(platform) || "APP".equals(platform);
    }

    /**
     * 检查人员是否存在
     */
    private static boolean checkPersonExists(String erfileId, List<Long> ids) {
        DynamicObjectCollection persons = getPersons(ids);
        if (persons == null || persons.isEmpty()) {
            logger.warn("未找到相关人员信息");
            return false;
        }
        return persons.stream().anyMatch(i -> i.getString("tdkw_pkid").equals(erfileId));
    }




    /**
     * 查询组织成员
     */
    private static List<Long> setPersonBatch(AuthorizedOrgResult authorizedAdminOrgSet) {
        List<Map<String, Object>> personByOrg = HRPIPersonServiceHelper.getPersonByOrgs(authorizedAdminOrgSet.getHasPermOrgs(), new Date());
        return personByOrg.stream()
                .filter(i -> i.get("person") != null)
                .map(i -> Long.valueOf(i.get("person").toString()))
                .collect(Collectors.toList());
    }

    /**
     * 领导自助权限查询
     */
    private static List<Long> leaderQueryBatch(Long userId) {
        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, CURRICULUMVITAE);
        if (authorizedAdminOrgSet.isHasAllOrgPerm()) {
            return null;
        } else {
            return setPersonBatch(authorizedAdminOrgSet);
        }
    }

    /**
     * 职能团队权限查询
     *
     * @param userId 苍穹id
     * @return true：有权限     false:无权限
     */
    private static List<Long> functionalInquiryBatch(Long userId) {
        TeamResultMap teamOrgScope = GetTeamOrgScopeUtil.getTeamOrgScope(userId, "tdkw_manageteam", "gwbq001", true);
        AuthorizedOrgResult authorizedOrgResult = teamOrgScope.getResult();
        if (authorizedOrgResult.isHasAllOrgPerm()) {
            return null;
        } else {
            return setPersonBatch(authorizedOrgResult);
        }
    }


    public static Map<String, Object> queryAuthorityBatch(Long userId, String platform) {
        Map<String, Object> resultMap = new HashMap<>();
        // 默认没有全部权限
        resultMap.put("hasAllOrgPerm", Boolean.FALSE);
        List<Long> allUserIds = new ArrayList<>();
        Pair<Long, Long> userIds = getUserIdsBatch(userId);
        if (Objects.isNull(userIds)) {
            resultMap.put("hasUserPerm", allUserIds);
            return resultMap;
        }
        allUserIds.add(userIds.getValue());
        logger.info("当前用户信息：" + userIds);
        if (platform.equals("PC")) {
            // 查询领导自助下权限的人员
            boolean isPCLead = HRRoleAndPersonUtils.getIsRole(userIds.getKey(), LEAD_PC_ROLE);
            if (isPCLead) {
                List<Long> pcUserIdList = leaderQueryBatch(userIds.getKey());
                if (Objects.nonNull(pcUserIdList)) {
                    allUserIds.addAll(pcUserIdList);
                } else {
                    resultMap.put("hasAllOrgPerm", Boolean.TRUE);
                }
            }
        }

        if (platform.equals("APP")) {
            // 查询领导自助下权限的人员
            boolean isPCLead = HRRoleAndPersonUtils.getIsRole(userIds.getKey(), LEAD_APP_ROLE);
            if (isPCLead) {
                List<Long> pcUserIdList = leaderQueryBatch(userIds.getKey());
                if (Objects.nonNull(pcUserIdList)) {
                    allUserIds.addAll(pcUserIdList);
                } else {
                    resultMap.put("hasAllOrgPerm", Boolean.TRUE);
                }
            }
        }

        // 团队下属
        List<Long> myTeamIdList = getSubordinatePersonnelIds(userIds.getValue());
        allUserIds.addAll(myTeamIdList);

        // 职能团队
        boolean isTeam = HRRoleAndPersonUtils.getIsRole(userIds.getKey(), TEAM_APP_ROLE);
        if (isTeam) {
            List<Long> functionalUserIdList = functionalInquiryBatch(userIds.getKey());
            if (Objects.nonNull(functionalUserIdList)) {
                allUserIds.addAll(functionalUserIdList);
            } else {
                resultMap.put("hasAllOrgPerm", Boolean.TRUE);
            }
        }
        resultMap.put("hasUserPerm", allUserIds);
        return resultMap;
    }

}