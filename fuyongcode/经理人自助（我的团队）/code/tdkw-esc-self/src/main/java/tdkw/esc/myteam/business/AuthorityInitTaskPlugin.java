package tdkw.esc.myteam.business;

import com.alibaba.fastjson.JSON;
import kd.bos.algo.DataSet;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.hr.hbpm.mservice.PositionServiceImpl;
import kd.hr.hbpm.mservice.api.IPositionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorityInitTaskPlugin extends AbstractTask {

    private static final Log logger = LogFactory.getLog(AuthorityInitTaskPlugin.class);

    // 经理角色权限-MOB显示 角色编码
    private final static String RoleNumber = "XY_TEAM_APP_001";

    /**
     * 需要操作的职层编码集合
     */
    private final static List<String> LIST = new ArrayList<>();
    /**
     * 团队自助角色自动分配时，需过滤部分用工关系类型人员
     * XY00005退休返聘人员
     * XY00006顾问
     * Y00007劳务人员
     * XY00008劳务派遣人员
     * XY00009劳务外包人员
     * XY00010其他外部人员
     */
    private final static Map<String, String> TEAM_LAB_TYPE_MAP = new HashMap<>();

    static {
        LIST.add("E1");
        LIST.add("E2");
        LIST.add("E3");
        LIST.add("M4");
        LIST.add("M5");
        LIST.add("M6");
        LIST.add("M1");
        LIST.add("M2");
        LIST.add("M3");
        // 退休返聘人员
        TEAM_LAB_TYPE_MAP.put("XY00005", "XY00005");
        // 顾问
        TEAM_LAB_TYPE_MAP.put("XY00006", "XY00006");
        // 劳务人员
        TEAM_LAB_TYPE_MAP.put("XY00007", "XY00007");
        // 劳务派遣人员
        TEAM_LAB_TYPE_MAP.put("XY00008", "XY00008");
        // 劳务外包人员
        TEAM_LAB_TYPE_MAP.put("XY00009", "XY00009");
        // 其他外部人员
        TEAM_LAB_TYPE_MAP.put("XY00010", "XY00010");
    }

    /**
     * 当前版本的过滤
     */
    private final QFilter CURRENTVERSION = new QFilter("iscurrentversion", "=", "1");
    private final QFilter DATASTATUS = new QFilter("datastatus", "=", "1");

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {

        try {
            Set<Map.Entry<String, Object>> entries = map.entrySet();
            List<Long> personIdList = new ArrayList<>();
            Boolean resetUnit = true;
            for (Map.Entry<String, Object> entry : entries) {
                if (entry.getKey().contains("person")) {
                    Map<String, Object> person = (Map<String, Object>) JSON.parse(String.valueOf(entry.getValue()));
                    if (person != null) {
                        String personId = String.valueOf(person.get("id"));
                        if (StringUtils.isNotEmpty(personId)) {
                            personIdList.add(Long.valueOf(personId));
                        }
                    }
                } else if ("resetUnit".equals(entry.getKey())) {
                    resetUnit = false;
                }
            }
            DynamicObject role = QueryServiceHelper.queryOne("perm_role", "id", new QFilter("number", "=", RoleNumber).toArray());
            DynamicObject teamPcRole = QueryServiceHelper.queryOne("perm_role", "id", new QFilter("number", "=", "XY_TEAM_PC_001").toArray());
            DynamicObject teamJxPcRole = QueryServiceHelper.queryOne("perm_role", "id", new QFilter("number", "=", "XY_TEAM_PC_010").toArray());
            DynamicObject teamJxAppRole = QueryServiceHelper.queryOne("perm_role", "id", new QFilter("number", "=", "XY_TEAM_APP_010").toArray());

            //查询团队角色
            DynamicObjectCollection teamRoleList = QueryServiceHelper.query("perm_role", "id,number", new QFilter("number", "like", "%XY_TEAM%").toArray());
            List<String> teamRoleNumberList = teamRoleList.stream().map(dynamicObject -> dynamicObject.getString("number")).collect(Collectors.toList());

            //查询领导角色
            DynamicObjectCollection leaderRoleList = QueryServiceHelper.query("perm_role", "id,number", new QFilter("number", "like", "%XY_LEAD%").toArray());

            String roleId = null;
            String teamPcRoleId = null;
            //绩效
            String teamJxPcRoleId = null;
            String teamJxAppRoleId = null;
            if (role != null) {
                roleId = role.getString("id");
            }

            if (teamPcRole != null) {
                teamPcRoleId = teamPcRole.getString("id");
            }
            //绩效
            if (teamJxPcRole != null) {
                teamJxPcRoleId = teamJxPcRole.getString("id");
            }
            if (teamJxAppRole != null) {
                teamJxAppRoleId = teamJxAppRole.getString("id");
            }

            if (resetUnit) {
                //查询全部的HR岗位
                QFilter branchUnitFilter = new QFilter("datastatus", "=", "1").and("iscurrentversion", "=", "1");
                DynamicObject[] hrUnits = BusinessDataServiceHelper.load("hbpm_positionhr", "id,number,name,boid", branchUnitFilter.toArray());
                //根据HR岗位跑下属岗位调度任务
                setBranchUnit(hrUnits);
            }

            //非时序性属性获取头像
            DynamicObject[] personProp = BusinessDataServiceHelper.load("hrpi_pernontsprop", "headsculpture,person", CURRENTVERSION.toArray());
            //联系方式
            DynamicObject[] personTact = BusinessDataServiceHelper.load("hrpi_percontact", "phone,busemail,person", CURRENTVERSION.toArray());
            //人事业务档案
            DynamicObject[] personFile = BusinessDataServiceHelper.load("hspm_ermanfile", "id,tdkw_index,empposrel", new QFilter[]{CURRENTVERSION, DATASTATUS});

            // 全部转为map
            Map<Long, String> personPropMap = Arrays.stream(personProp).collect(Collectors.toMap(
                    prop -> prop.getDynamicObject("person").getLong("id"),
                    prop -> prop.getString("headsculpture"),
                    (oldObj, newObj) -> oldObj
            ));
            Map<Long, String> personPhoneMap = Arrays.stream(personTact).collect(Collectors.toMap(
                    phone -> phone.getDynamicObject("person").getLong("id"),
                    phone -> phone.getString("phone"),
                    (oldObj, newObj) -> oldObj
            ));
            Map<Long, String> personEmailMap = Arrays.stream(personTact).collect(Collectors.toMap(
                    email -> email.getDynamicObject("person").getLong("id"),
                    email -> email.getString("busemail"),
                    (oldObj, newObj) -> oldObj
            ));
            Map<Long, String> personFileMap = Arrays.stream(personFile).collect(Collectors.toMap(
                    file -> file.getDynamicObject("empposrel").getLong("id"),
                    file -> file.getString("tdkw_index"),
                    (oldObj, newObj) -> oldObj
            ));

            //过滤虚拟兼职
            String partTime = System.getProperty("constant.hrmp.hbss.tdkw_hbss_changereason.fictitious");
            if (kd.bos.orm.util.StringUtils.isEmpty(partTime)) {
                partTime = "XY00017";
            }
            QFilter orFilter = new QFilter("tdkw_changereason.number", QCP.not_equals, partTime);
            orFilter.or("tdkw_changereason.id", QCP.is_null, null);
            //全部的任职经历
            DynamicObject[] empposorgrel = BusinessDataServiceHelper.load("hrpi_empposorgrel", "tdkw_ranks,person,position,adminorg,person.headsculpture,company,islatestrecord,tdkw_ranks,tdkw_ranks.number", CURRENTVERSION.and("businessstatus", "=", "1").and("datastatus", "=", "1").and(orFilter).toArray());

            QFilter notInRanksFilter = new QFilter("tdkw_ranks.number", QCP.not_in, LIST);
            notInRanksFilter.and(CURRENTVERSION);
            //查询岗位职层不在指定职层中的任职经历
            DynamicObject[] notInRanks = BusinessDataServiceHelper.load("hrpi_empposorgrel", "tdkw_ranks,person,position,adminorg,person.headsculpture,company,islatestrecord,tdkw_ranks,tdkw_ranks.number", notInRanksFilter.toArray());
            //根据任职经历获取人员
            Set<Long> notInRanksPersonIdSet = new HashSet<>();
            for (DynamicObject notInRank : notInRanks) {
                DynamicObject notInRanksPerson = notInRank.getDynamicObject("person");
                if (!ObjectUtils.isEmpty(notInRanksPerson)) {
                    notInRanksPersonIdSet.add(notInRanksPerson.getLong("id"));
                }
            }

            //遍历人员判断不在指定岗位职层中的人员是否分配的团队角色，若有则删除对应角色
            //HR用户ID 查询 苍穹的用户ID
            List<Long> cqUserList = HRRoleAndPersonUtils.getCQUser(new ArrayList<>(notInRanksPersonIdSet));
            // 批量查询人员对应的所有角色
            QFilter userRoleFilter = new QFilter("user.id", QCP.in, cqUserList);
            DataSet userRoleDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hrcs_userrolerelat", "id,role.id,role.number,user.id", userRoleFilter.toArray());
            DynamicObjectCollection userRoleData = ORM.create().toPlainDynamicObjectCollection(userRoleDataSet);
            // 转为map
            Map<Long, List<String>> userRoleMap = userRoleData.stream()
                    .collect(Collectors.groupingBy(
                            dynamicObject -> dynamicObject.getLong("user.id"),
                            Collectors.mapping(
                                    dynamicObject -> dynamicObject.getString("role.number"),
                                    Collectors.toList())));

            // 删除对应角色
            userRoleMap.forEach((userId, userRoles) -> {
                // 系统用户id
                // 把当前用户有的角色和团队角色取交集，删除交集的角色数据
                userRoles.retainAll(teamRoleNumberList);
                if (CollectionUtils.isNotEmpty(userRoles)) {
                    // 循环删除角色
                    userRoles.forEach(userRoleId -> HRRoleAndPersonUtils.deleteUserRole(userId, userRoleId));
                }
            });

            if (personIdList.size() == 0) {
                //查询指定岗位层级对应的岗位的任职经历的岗位和人员。
                QFilter appointmentFilter = new QFilter("businessstatus", "=", "1").and(CURRENTVERSION);
                DynamicObject[] appointments = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person,position,position.boid,islatestrecord,tdkw_ranks,tdkw_ranks.number", appointmentFilter.toArray());
                setReportCorelType(teamRoleList, leaderRoleList, roleId, teamPcRoleId, personFileMap,personPropMap, personPhoneMap, personEmailMap, empposorgrel, appointments, teamJxPcRoleId, teamJxAppRoleId);
            } else {
                QFilter CareerExperienceFilter = new QFilter("iscurrentversion", "=", "1");
                CareerExperienceFilter.and("businessstatus", "=", "1").and("datastatus", "=", "1").and(orFilter);
                //查询指定人员的任职经历
                QFilter appointmentFilter = new QFilter("person.id", "in", personIdList).and(CareerExperienceFilter);
                DynamicObject[] appointments = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person,position,position.boid,islatestrecord,tdkw_ranks,tdkw_ranks.number", appointmentFilter.toArray());
                if (appointments != null) {
                    setReportCorelType(teamRoleList, leaderRoleList, roleId, teamPcRoleId,personFileMap, personPropMap, personPhoneMap, personEmailMap, empposorgrel, appointments, teamJxPcRoleId, teamJxAppRoleId);
                }
            }

            logger.info("下属调度执行正常，开始迁移数据");
            // 将中间表数据迁移到正式表
            // 先删除正式表数据
            DB.execute(DBRoute.of("secd"), "/*dialect*/ " +
                    "truncate table tk_tdkw_myteam_branchunit; " +
                    "truncate table tk_tdkw_myteam_unitdetail; " +
                    "truncate table tk_tdkw_businessunit; " +
                    "truncate table tk_tdkw_subordinatedetais; " +
                    "insert into tk_tdkw_myteam_branchunit select * from tk_tdkw_myteam_branch_tem; " +
                    "insert into tk_tdkw_myteam_unitdetail select * from tk_tdkw_myteam_detail_tem; " +
                    "insert into tk_tdkw_businessunit select * from tk_tdkw_businessunit_temp; " +
                    "insert into tk_tdkw_subordinatedetais select * from tk_tdkw_subordinate_temp; "
            );

            logger.info("下属调度执行正常，结束迁移数据");
        } catch (Exception e) {
            logger.error("下属调度失败：" + e.getMessage());
            throw new KDBizException("下属调度失败：" + e.getMessage());
        } finally {
            // 清空中间表
            DB.execute(DBRoute.of("secd"), "/*dialect*/ " +
                    " truncate table tk_tdkw_myteam_branch_tem; " +
                    "truncate table tk_tdkw_myteam_detail_tem; " +
                    "truncate table tk_tdkw_businessunit_temp; " +
                    "truncate table tk_tdkw_subordinate_temp; " +
                    "");
            logger.info("下属调度执行正常，清空中间表数据");
        }
    }


    /**
     * 设置下属岗位
     *
     * @param hrUnits HR岗位
     */
    private void setBranchUnit(DynamicObject[] hrUnits) {
        // 批量查询岗位信息
        QFilter positionFilter = new QFilter("iscurrentversion", QCP.equals, "1");
        DynamicObject[] positionList = BusinessDataServiceHelper.load("hbpm_positionhr", "id,number,name,boid", positionFilter.toArray());
        // 转为map
        Map<Long, DynamicObject> positionMap = Arrays.stream(positionList).collect(Collectors.toMap(
                position -> position.getLong("boid"),
                position -> position,
                (oldObj, newObj) -> oldObj
        ));
        // 取出所有岗位的id
        List<Long> hrUnitBoIdList = Arrays.stream(hrUnits).mapToLong(dynamicObject -> dynamicObject.getLong("boid")).boxed().collect(Collectors.toList());
        //获取所有协作关系类型，然后根据协作关系类型分别落下属标表。
        QFilter collaborationTypeFilter = new QFilter("status", "=", "C");
        DynamicObject[] collaborationTypes = BusinessDataServiceHelper.load("hbpm_reportcoreltype", "id,number,name", collaborationTypeFilter.toArray());
        logger.info("协作类型集合：" + Arrays.toString(collaborationTypes));
        // 先查询所有数据
        // 业务上级 XY001
        Map<Long, List<Map<String, Object>>> businessMap = new HashMap<>();
        // 行政上级 1010_S
        Map<Long, List<Map<String, Object>>> administrativeMap = new HashMap<>();
        for (DynamicObject collaborationType : collaborationTypes) {
            String number = collaborationType.getString("number");
            long collaborationTypeId = collaborationType.getLong("id");
            IPositionService positionService = new PositionServiceImpl();
            Map<String, Object> resultMap = positionService.queryChildPositionAndWorkRole(hrUnitBoIdList, new Date(), collaborationTypeId);
            Map<Long, List<Map<String, Object>>> data = (Map<Long, List<Map<String, Object>>>) MapUtils.getMap(resultMap, "data");
            if ("XY001".equals(number)) {
                businessMap = data;
            }
            if ("1010_S".equals(number)) {
                administrativeMap = data;
                ;
            }
        }


        List<DynamicObject> branchUnitList = new ArrayList<>();
        for (DynamicObject collaborationType : collaborationTypes) {
            String number = collaborationType.getString("number");
            //根据岗位和协作类型查询下属岗位
            for (DynamicObject hrUnit : hrUnits) {
                //下属岗位实体
                DynamicObject branchUnit = BusinessDataServiceHelper.newDynamicObject("tdkw_myteam_branchun_temp");
                branchUnit.set("tdkw_position", hrUnit.getLong("id"));
                branchUnit.set("tdkw_reportcoreltype", collaborationType);
                branchUnit.set("enable", "1");
                long boId = hrUnit.getLong("boid");
                List<Map<String, Object>> underlingDataMapList;
                if ("XY001".equals(number)) {
                    underlingDataMapList = businessMap.get(boId);
                } else if ("1010_S".equals(number)) {
                    underlingDataMapList = administrativeMap.get(boId);
                } else {
                    continue;
                }
                logger.info(boId + "位的所有下级fid-boid" + underlingDataMapList);
                //下属岗位分录集合
                DynamicObjectCollection branchUnitEntryList = branchUnit.getDynamicObjectCollection("tdkw_entryentity");
                if (underlingDataMapList != null && underlingDataMapList.size() > 0) {
                    //遍历接口返回的下级岗位
                    for (Map<String, Object> stringObjectMap : underlingDataMapList) {
                        //获取接口下级岗位Id
                        Long positionBoId = (Long) stringObjectMap.get("positonBoId");
                        DynamicObject positions = positionMap.get(positionBoId);
                        if (!ObjectUtils.isEmpty(positions)) {
                            DynamicObject branchUnitEntry = branchUnitEntryList.addNew();
                            branchUnitEntry.set("tdkw_branchposition", positions);
                        }
                    }
                }
                if (branchUnitEntryList.size() > 0) {
                    branchUnit.set("tdkw_entryentity", branchUnitEntryList);
                    branchUnitList.add(branchUnit);
                    logger.info("新增下属岗位");
                    if (branchUnitList.size() == 200) {
                        logger.info(collaborationType.getString("name") + "线路的下属岗位" + branchUnitList);
                        SaveServiceHelper.save(branchUnitList.toArray(new DynamicObject[0]));
                        branchUnitList.clear();
                    }
                }
            }
        }
        if (branchUnitList.size() > 0) {
            SaveServiceHelper.save(branchUnitList.toArray(new DynamicObject[0]));
            branchUnitList.clear();
        }
    }

    /**
     * 带协作关系类型的下属
     *
     * @param teamRoleList    团队经理角色
     * @param leaderRoleList  领导角色
     * @param roleId
     * @param teamPcRoleId
     * @param empposorgrel    任职经历实体
     * @param appointments    任职经历实体
     * @param teamJxPcRoleId
     * @param teamJxAppRoleId
     */
    private static void setReportCorelType(DynamicObjectCollection teamRoleList, DynamicObjectCollection leaderRoleList, String roleId,
                                           String teamPcRoleId, Map<Long, String> personFileMap,Map<Long, String> personPropMap, Map<Long, String> personPhoneMap,
                                           Map<Long, String> personEmailMap, DynamicObject[] empposorgrel, DynamicObject[] appointments, String teamJxPcRoleId, String teamJxAppRoleId) {
        //获取所有协作关系类型，然后根据协作关系类型分别落下属标表。
        QFilter collaborationTypeFilter = new QFilter("status", "=", "C");
        DynamicObject[] collaborationTypes = BusinessDataServiceHelper.load("hbpm_reportcoreltype", "id,number,name", collaborationTypeFilter.toArray());
        // 查询用工关系类型
        Set<Long> personIdLIst = Arrays.stream(empposorgrel).map(dynamicObject -> dynamicObject.getDynamicObject("person").getLong("id")).collect(Collectors.toSet());
        Map<Long, String> labTypeMap = getLabTypeMap(personIdLIst);
        for (DynamicObject collaborationType : collaborationTypes) {
            // 1.根据角色协作类型和指定岗位层级的任职经历岗位查询岗位申请表中，协作关系分录中协作类型为当前协作类型并且协作岗位为当前岗位的所有数据
            List<DynamicObject> underlingList = new ArrayList<>();
            if (!ObjectUtils.isEmpty(collaborationType)) {
                for (DynamicObject appointment : appointments) {
                    //获取岗位
                    DynamicObject position = appointment.getDynamicObject("position");
                    //获取人员
                    DynamicObject person = appointment.getDynamicObject("person");

                    DynamicObject underling = BusinessDataServiceHelper.newDynamicObject("tdkw_appauth_busines_temp");
                    underling.set("enable", "1");
                    //经理人
                    underling.set("tdkw_manager", person);
                    //岗位
                    underling.set("tdkw_position", position);
                    //协作类型
                    underling.set("tdkw_reportcoreltype", collaborationType);
                    underling.set("tdkw_latestappointment", appointment.getBoolean("islatestrecord"));
                    //根据岗位和协作类型找到协作关系集合
                    if (!ObjectUtils.isEmpty(position)) {
                        DynamicObjectCollection subordinateInfoList = underling.getDynamicObjectCollection("tdkw_entryentity");

                        //根据协作关系类型和岗位id查询当前岗位的下属岗位——(下属岗位表)
                        QFilter branchUnitFilter = new QFilter("tdkw_position.id", QCP.equals, position.getPkValue());
                        branchUnitFilter.and("tdkw_reportcoreltype.id", QCP.equals, collaborationType.getPkValue());
                        DynamicObject branchUnit = BusinessDataServiceHelper.loadSingle("tdkw_myteam_branchun_temp", "tdkw_entryentity,tdkw_entryentity.tdkw_branchposition", branchUnitFilter.toArray());
                        if (!ObjectUtils.isEmpty(branchUnit)) {
                            DynamicObjectCollection branchUnitEntryList = branchUnit.getDynamicObjectCollection("tdkw_entryentity");
                            if (branchUnitEntryList != null && branchUnitEntryList.size() > 0) {
                                List<Long> branchPositionIdList = new ArrayList<>();
                                for (DynamicObject branchUnitEntry : branchUnitEntryList) {
                                    DynamicObject branchPosition = branchUnitEntry.getDynamicObject("tdkw_branchposition");
                                    if (!ObjectUtils.isEmpty(branchPosition)) {
                                        branchPositionIdList.add(branchPosition.getLong("id"));
                                    }
                                }
                                logger.info("全部岗位id" + branchPositionIdList);
                                //遍历返回的下级岗位
                                setUnderLing(personFileMap,personPropMap, personPhoneMap, personEmailMap, empposorgrel, subordinateInfoList, branchUnitEntryList, branchPositionIdList);
                                //1.若有岗位但无对应任职人员，则判断该岗位是否有下级岗位;
                                //2.若有下级岗位，且有对应人员则获取下级岗位对应人员为当前岗位对应人员的下属
                                //3.若有下级岗位但无对应人员则重复1-2步骤直至无下级岗位
                                if (branchPositionIdList.size() > 0) {
                                    logger.info("断层岗位id" + branchPositionIdList);
                                    setUnderlingPerson(personFileMap,personPropMap, personPhoneMap, personEmailMap, empposorgrel, subordinateInfoList, branchPositionIdList, collaborationType.getLong("id"));
                                }
                            }
                        }


                        if (subordinateInfoList.size() > 0) {
                            logger.info("协作关系领导2" + person.getString("name"));
                            underling.set("tdkw_entryentity", subordinateInfoList);
                            underlingList.add(underling);
                            if (underlingList.size() == 200) {
                                logger.info("保存200条下属数据");
                                SaveServiceHelper.save(underlingList.toArray(new DynamicObject[0]));
                                underlingList.clear();
                            }
                            DynamicObject ranks = appointment.getDynamicObject("tdkw_ranks");
                            logger.info("岗位层级实体" + ranks);
                            if (ranks != null) {
                                String ranksNumber = ranks.getString("number");
                                //若人员职层为M1-M6,E1-E3为经理人分配经理人角色
                                logger.info("岗位层级" + ranksNumber);
                                if (LIST.contains(ranksNumber)) {
                                    Long cqUser = HRRoleAndPersonUtils.getCQUser(person.getLong("id"));
                                    logger.info("苍穹用户id" + cqUser);
                                    if (cqUser != 0L) {

                                        List<String> userRole = HRRoleAndPersonUtils.getUserRole(cqUser);
                                        logger.info("角色编码集合" + userRole);
                                        logger.info("领导角色编码集合" + leaderRoleList);
                                        boolean hasLeaderRole = false;
                                        boolean hasTeamAppRoleId = false;
                                        boolean hasTeamPcRoleId = false;
                                        boolean hasJxTeamAppRoleId = false;
                                        boolean hasJxTeamPcRoleId = false;
                                        //判断是否有领导角色，若有领导角色则不分配团队角色;判断是否有团队角色

                                        for (String userRoleId : userRole) {
                                            //判断是否有领导角色，若有领导角色则不分配团队角色
                                            for (DynamicObject leaderRole : leaderRoleList) {
                                                String leaderRoleId = leaderRole.getString("id");
                                                if (StringUtils.equals(leaderRoleId, userRoleId)) {
                                                    hasLeaderRole = true;
                                                }
                                            }
                                            //判断是否有团队角色,没有领导角色并且没有团队角色则增加团队角色
                                            if (StringUtils.equals(roleId, userRoleId)) {
                                                hasTeamAppRoleId = true;
                                            }
                                            if (StringUtils.equals(teamPcRoleId, userRoleId)) {
                                                hasTeamPcRoleId = true;
                                            }
                                            //绩效
                                            if (StringUtils.equals(teamJxPcRoleId, userRoleId)) {
                                                hasJxTeamPcRoleId = true;
                                            }
                                            if (StringUtils.equals(teamJxAppRoleId, userRoleId)) {
                                                hasJxTeamAppRoleId = true;
                                            }
                                        }
                                        logger.info("是否有领导角色" + hasLeaderRole);
                                        if (!hasLeaderRole) {
                                            String labType = labTypeMap.get(person.getLong("id"));
                                            if (StringUtils.isBlank(labType)) {
                                                continue;
                                            }
                                            // 判断是否是不分配团队角色的用工关系类型人员
                                            boolean labTypeFlag = TEAM_LAB_TYPE_MAP.containsKey(labType);
                                            //无领导角色,分配团队角色
                                            if (!hasTeamAppRoleId && !labTypeFlag) {
                                                // #131040 【权限优化】团队自助角色自动分配时，需过滤部分用工关系类型人员 http://ones.xiangyu.com/project/#/team/JbjqrWit/task/DBzAg9wBvZSmwNRQ
                                                HRRoleAndPersonUtils.role(cqUser, RoleNumber);
                                            }
                                            if (!hasTeamPcRoleId && !labTypeFlag) {
                                                // #131040 【权限优化】团队自助角色自动分配时，需过滤部分用工关系类型人员 http://ones.xiangyu.com/project/#/team/JbjqrWit/task/DBzAg9wBvZSmwNRQ
                                                HRRoleAndPersonUtils.role(cqUser, "XY_TEAM_PC_001");
                                            }
                                            //绩效
                                            if (!hasJxTeamAppRoleId && !labTypeFlag) {
                                                HRRoleAndPersonUtils.role(cqUser, "XY_TEAM_APP_010");
                                            }
                                            if (!hasJxTeamPcRoleId && !labTypeFlag) {
                                                HRRoleAndPersonUtils.role(cqUser, "XY_TEAM_PC_010");
                                            }
                                        } else {
                                            //当有领导角色删除团队角色
                                            for (DynamicObject dynamicObject : teamRoleList) {
                                                String id = dynamicObject.getString("id");
                                                for (String userRoleId : userRole) {
                                                    if (StringUtils.equals(userRoleId, id)) {
                                                        logger.info("删除的角色编码" + dynamicObject.getString("number"));
                                                        HRRoleAndPersonUtils.deleteUserRole(cqUser, dynamicObject.getString("number"));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                if (underlingList.size() > 0) {
                    SaveServiceHelper.save(underlingList.toArray(new DynamicObject[0]));
                    logger.info("保存不足200条下属数据");
                    underlingList.clear();
                }
            }
        }
    }

    /**
     * 设置下属
     *
     * @param empposorgrel         任职经历
     * @param subordinateInfoList  人员下属分录
     * @param branchUnitEntryList  下属岗位分录集合
     * @param branchPositionIdList 下属岗位id集合
     */
    private static void setUnderLing(Map<Long, String> personFileMap,Map<Long, String> personPropMap, Map<Long, String> personPhoneMap, Map<Long, String> personEmailMap,
                                     DynamicObject[] empposorgrel, DynamicObjectCollection subordinateInfoList, DynamicObjectCollection branchUnitEntryList, List<Long> branchPositionIdList) {
        for (DynamicObject careerExperience : empposorgrel) {
            //根据查询到的岗位number匹配任职经历里的人员
            DynamicObject careerPosition = careerExperience.getDynamicObject("position");
            if (!ObjectUtils.isEmpty(careerPosition)) {
                for (DynamicObject branchUnitEntry : branchUnitEntryList) {
                    //获取下级岗位Id
                    DynamicObject branchPosition = branchUnitEntry.getDynamicObject("tdkw_branchposition");
                    Long branchPositionId = branchPosition.getLong("id");

                    Long positionId = careerPosition.getLong("id");
                    if (branchPositionId.compareTo(positionId) == 0) {
                        logger.info("下属的岗位id22" + branchPositionId);
                        branchPositionIdList.remove(branchPositionId);
                        DynamicObject subordinateInfo = subordinateInfoList.addNew();
                        //所属部门
                        subordinateInfo.set("tdkw_adminorg", careerExperience.getDynamicObject("adminorg"));
                        subordinateInfo.set("tdkw_position1", careerExperience.getDynamicObject("position"));
                        subordinateInfo.set("tdkw_company", careerExperience.getDynamicObject("company"));
                        subordinateInfo.set("tdkw_newstappointment", careerExperience.getBoolean("islatestrecord"));
                        //从任职经历中取人员
                        DynamicObject subordinatePerson = careerExperience.getDynamicObject("person");
                        if (!ObjectUtils.isEmpty(subordinatePerson)) {
                            Long subordinatePersonPkValue = (Long) subordinatePerson.getPkValue();
                            subordinateInfo.set("tdkw_subordinate", subordinatePerson);
                            //新增排序号
                            subordinateInfo.set("tdkw_index", personFileMap.get(careerExperience.getLong("id")));
                            subordinateInfo.set("tdkw_headsculpture", personPropMap.get(subordinatePersonPkValue));
                            //遍历联系方式获取手机号码
                            subordinateInfo.set("tdkw_phone", personPhoneMap.get(subordinatePersonPkValue));
                            subordinateInfo.set("tdkw_peremail", personEmailMap.get(subordinatePersonPkValue));
                        }
                    }
                }
            }
        }
        logger.info("返回正常下属列表" + subordinateInfoList);
    }

    /**
     * 岗位断档问题处理
     *
     * @param subordinateInfoList  人员下属集合
     * @param branchPositionIdList 下属岗位集合
     * @param collaborationTypeId  协作类型id
     */
    private static void setUnderlingPerson(Map<Long, String> personFileMap,Map<Long, String> personPropMap, Map<Long, String> personPhoneMap, Map<Long, String> personEmailMap,
                                           DynamicObject[] empposorgrel, DynamicObjectCollection subordinateInfoList, List<Long> branchPositionIdList, long collaborationTypeId) {
        QFilter branchUnitFilter = new QFilter("tdkw_position.id", "in", branchPositionIdList);
        branchUnitFilter.and("tdkw_reportcoreltype.id", "=", collaborationTypeId);
        DynamicObject[] branchUnits = BusinessDataServiceHelper.load("tdkw_myteam_branchun_temp", "tdkw_entryentity,tdkw_entryentity.tdkw_branchposition", branchUnitFilter.toArray());

        List<Long> positionIdList = new ArrayList<>();

        //遍历集合获取岗位id
        for (DynamicObject branchUnit : branchUnits) {
            DynamicObjectCollection branchUnitEntryList = branchUnit.getDynamicObjectCollection("tdkw_entryentity");
            for (DynamicObject branchUnitEntry : branchUnitEntryList) {
                DynamicObject branchPosition = branchUnitEntry.getDynamicObject("tdkw_branchposition");
                if (!ObjectUtils.isEmpty(branchPosition)) {
                    positionIdList.add(branchPosition.getLong("id"));
                }
            }
        }
        //遍历集合设置下属
        for (DynamicObject branchUnit : branchUnits) {
            DynamicObjectCollection branchUnitEntryList = branchUnit.getDynamicObjectCollection("tdkw_entryentity");
            if (branchUnitEntryList != null && branchUnitEntryList.size() > 0) {
                setUnderLing(personFileMap,personPropMap, personPhoneMap, personEmailMap, empposorgrel, subordinateInfoList, branchUnitEntryList, positionIdList);
            }
        }
        //递归获取岗位断层下属
        if (positionIdList.size() > 0) {
            setUnderlingPerson(personFileMap,personPropMap, personPhoneMap, personEmailMap, empposorgrel, subordinateInfoList, positionIdList, collaborationTypeId);
        }
        logger.info("返回断层下属列表" + subordinateInfoList);
    }


    /**
     * 通过人员id查询用工关系类型
     *
     * @param personIdList
     * @return
     */
    public static Map<Long, String> getLabTypeMap(Set<Long> personIdList) {
        QFilter filter = new QFilter("person", QCP.in, personIdList);
        filter.and("datastatus", QCP.equals, "1");
        filter.and("businessstatus", QCP.equals, "1");
        filter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] empList = BusinessDataServiceHelper.load("hrpi_employee", "id,person,laborreltype.number", filter.toArray());
        return Arrays.stream(empList).collect(Collectors.toMap(emp -> emp.getDynamicObject("person").getLong("id"), emp -> emp.getString("laborreltype.number")));
    }
}
