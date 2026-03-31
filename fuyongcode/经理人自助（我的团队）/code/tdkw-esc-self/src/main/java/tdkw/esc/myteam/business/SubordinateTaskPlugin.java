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
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.hr.hbpm.mservice.PositionServiceImpl;
import kd.hr.hbpm.mservice.api.IPositionService;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.*;

public class SubordinateTaskPlugin extends AbstractTask {

    private static final Log logger = LogFactory.getLog(AuthorityInitTaskPlugin.class);

    // 经理角色权限-MOB显示 角色编码
    private final static String RoleNumber = "XY_TEAM_APP_001";

    /**
     * 需要操作的职层编码集合
     */
    private final static List<String> LIST = new ArrayList<>();

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
    }

    /**
     * 当前版本的过滤
     */
    private final QFilter CURRENTVERSION = new QFilter("iscurrentversion", "=", "1");

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {

        Set<Map.Entry<String, Object>> entries = map.entrySet();
        List<Long> personIdList = new ArrayList<>();
        boolean resetUnit = true;
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

        //查询团队角色
        DynamicObjectCollection teamRoleList = QueryServiceHelper.query("perm_role", "id,number", new QFilter("number", "like", "%XY_TEAM%").toArray());
        //查询领导角色
        DynamicObjectCollection leaderRoleList = QueryServiceHelper.query("perm_role", "id,number", new QFilter("number", "like", "%XY_LEAD%").toArray());

        String roleId = null;
        String teamPcRoleId = null;
        if (role != null) {
            roleId = role.getString("id");
        }

        if (teamPcRole != null) {
            teamPcRoleId = teamPcRole.getString("id");
        }

        if (resetUnit) {
            DeleteServiceHelper.delete("tdkw_myteam_branchun_copy", null);
            // 查询有下级岗位的岗位数据
//            SqlBuilder positionBuilder = new SqlBuilder();
//            positionBuilder.append("/*dialect*/");
//            positionBuilder.append(" select fid as id ,fnumber as number,fname as name,fboid as boid ");
//            positionBuilder.append(" from t_hbpm_position ");
//            positionBuilder.append(" where  fid in ( ");
//            positionBuilder.append(" select distinct fparentid ");
//            positionBuilder.append(" from t_hbpm_position ");
//            positionBuilder.append(" where fiscurrentversion = '1' ");
//            positionBuilder.append(" and fdatastatus = '1' ");
//            positionBuilder.append(" and fenable = '1' ");
//            positionBuilder.append(" and fstatus = 'C' ");
//            positionBuilder.append(" and fparentid != 0 ) ");
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append(" select\n" +
                    "\tfid as id ,\n" +
                    "\tfnumber as number,\n" +
                    "\tfname as name,\n" +
                    "\tfboid as boid\n" +
                    "from\n" +
                    "\tt_hbpm_position\n" +
                    "where\n" +
                    "\tfid in (\n" +
                    "\tselect\n" +
                    "\t\tdistinct pos.fid\n" +
                    "\tfrom\n" +
                    "\t\tt_hbpm_reportingrelation relation\n" +
                    "\tleft join t_hbpm_workroles workrole on\n" +
                    "\t\tworkrole.fid = relation.fparentid\n" +
                    "\tleft join t_hbpm_position pos on\n" +
                    "\t\tworkrole.fid = pos.fworkroleid\n" +
                    "\twhere\n" +
                    "\t\trelation.fiscurrentversion = '1'\n" +
                    "\t\tand relation.fenable = '1'\n" +
                    "\t\tand relation.fdatastatus = '1'\n" +
                    "\t\tand workrole.fiscurrentversion = '1'\n" +
                    "\t\tand workrole.fdatastatus = '1'\n" +
                    "\t\tand workrole.fenable = '1'\n" +
                    "\t\tand pos.fiscurrentversion = '1'\n" +
                    "\t\tand pos.fdatastatus = '1'\n" +
                    "\t\tand pos.fenable = '1'\n" +
                    "\t\tand pos.fstatus = 'C' ) ");
            DataSet positionDataSet = DB.queryDataSet("SubordinateTaskPlugin", DBRoute.of("hr"), stringBuilder.toString());
            DynamicObjectCollection plainDynamicObjectCollection = ORM.create().toPlainDynamicObjectCollection(positionDataSet);
            //根据HR岗位跑下属岗位调度任务
            setBranchUnit(plainDynamicObjectCollection);
        }

        //非时序性属性获取头像
        DynamicObject[] pernontsprop = BusinessDataServiceHelper.load("hrpi_pernontsprop", "headsculpture,person", CURRENTVERSION.toArray());
        //联系方式
        DynamicObject[] percontact = BusinessDataServiceHelper.load("hrpi_percontact", "phone,busemail,person", CURRENTVERSION.toArray());

        //过滤虚拟兼职
        String partTime = System.getProperty("constant.hrmp.hbss.tdkw_hbss_changereason.fictitious");
        if (kd.bos.orm.util.StringUtils.isEmpty(partTime)) {
            partTime = "XY00017";
        }
        QFilter orFilter = new QFilter("tdkw_changereason.number", QCP.not_equals, partTime);
        orFilter.or("tdkw_changereason.id", QCP.is_null, null);
        //全部的任职经历
        DynamicObject[] empposorgrel = BusinessDataServiceHelper.load("hrpi_empposorgrel", "tdkw_ranks,person,position,adminorg,person.headsculpture,company,islatestrecord,tdkw_ranks,tdkw_ranks.number", CURRENTVERSION.and("businessstatus", "=", "1").and("datastatus", "=", "1").and(orFilter).toArray());

        QFilter notInRanksFilter = new QFilter("tdkw_ranks.number", "not in", LIST);
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
        //HR用户ID 查询 苍穹的用户ID
        List<Long> cqUserList = HRRoleAndPersonUtils.getCQUser(new ArrayList<>(notInRanksPersonIdSet));
        //遍历人员判断不在指定岗位职层中的人员是否分配的团队角色，若有则删除对应角色
        for (Long userId : cqUserList) {
            List<String> userRole = HRRoleAndPersonUtils.getUserRole(userId);

            //判断是否有领或团队导角色,若有则删除对应角色
            for (String userRoleId : userRole) {
                for (DynamicObject teamRole : teamRoleList) {
                    String teamRoleNumber = teamRole.getString("id");
                    if (StringUtils.equals(teamRoleNumber, userRoleId)) {
                        HRRoleAndPersonUtils.deleteUserRole(userId, teamRole.getString("number"));
                    }
                }
            }
        }
        if (personIdList.size() == 0) {
            DeleteServiceHelper.delete("tdkw_appauth_busines_copy", null);
            //查询指定岗位层级对应的岗位的任职经历的岗位和人员。
            QFilter appointmentFilter = new QFilter("businessstatus", "=", "1").and(CURRENTVERSION);
            DynamicObject[] appointments = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person,position,position.boid,islatestrecord,tdkw_ranks,tdkw_ranks.number", appointmentFilter.toArray());
            setReportCorelType(teamRoleList, leaderRoleList, roleId, teamPcRoleId, pernontsprop, percontact, empposorgrel, appointments);
        } else {
            QFilter deleteFilter = new QFilter("tdkw_manager.id", "in", personIdList);
            DeleteServiceHelper.delete("tdkw_appauth_busines_copy", deleteFilter.toArray());

            QFilter CareerExperienceFilter = new QFilter("iscurrentversion", "=", "1");
            CareerExperienceFilter.and("businessstatus", "=", "1").and("datastatus", "=", "1").and(orFilter);

            //查询指定人员的任职经历
            QFilter appointmentFilter = new QFilter("person.id", "in", personIdList).and(CareerExperienceFilter);
            DynamicObject[] appointments = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person,position,position.boid,islatestrecord,tdkw_ranks,tdkw_ranks.number", appointmentFilter.toArray());
            if (appointments != null) {
                setReportCorelType(teamRoleList, leaderRoleList, roleId, teamPcRoleId, pernontsprop, percontact, empposorgrel, appointments);
            }
        }
    }


    /**
     * 设置下属岗位
     *
     * @param hrUnits HR岗位
     */
    private void setBranchUnit(DynamicObjectCollection hrUnits) {
        //获取所有协作关系类型，然后根据协作关系类型分别落下属标表。
        QFilter collaborationTypeFilter = new QFilter("status", "=", "C");
        DynamicObject[] collaborationTypes = BusinessDataServiceHelper.load("hbpm_reportcoreltype", "id,number,name", collaborationTypeFilter.toArray());
        logger.info("协作类型集合：" + Arrays.toString(collaborationTypes));
        List<DynamicObject> branchUnitList = new ArrayList<>();
        for (DynamicObject collaborationType : collaborationTypes) {
            //根据岗位和协作类型查询下属岗位
            for (DynamicObject hrUnit : hrUnits) {
                //下属岗位实体
                DynamicObject branchUnit = BusinessDataServiceHelper.newDynamicObject("tdkw_myteam_branchun_copy");
                branchUnit.set("tdkw_position", hrUnit.getLong("id"));
                branchUnit.set("tdkw_reportcoreltype", collaborationType);
                branchUnit.set("enable", "1");
                IPositionService positionService = new PositionServiceImpl();
                List<Long> businessIdList = new ArrayList<>();
                businessIdList.add(hrUnit.getLong("boid"));
                //查询岗位的所有下级fid，及工作角色的fid
                Map<String, Object> underlingMap = positionService.queryChildPositionAndWorkRole(businessIdList, new Date(), collaborationType.getLong("id"));
                logger.info(businessIdList + "位的所有下级fid" + underlingMap);
                Map<Long, List<Map<String, Object>>> underlingData = (Map<Long, List<Map<String, Object>>>) underlingMap.get("data");
                logger.info(businessIdList + "位的所有下级fid-data" + underlingMap);
                List<Map<String, Object>> underlingDataMapList = underlingData.get(hrUnit.getLong("boid"));
                logger.info(businessIdList + "位的所有下级fid-boid" + underlingDataMapList);
                //下属岗位分录集合
                DynamicObjectCollection branchUnitEntryList = branchUnit.getDynamicObjectCollection("tdkw_entryentity");
                if (underlingDataMapList != null && underlingDataMapList.size() > 0) {
                    //遍历接口返回的下级岗位
                    for (Map<String, Object> stringObjectMap : underlingDataMapList) {
                        //获取接口下级岗位Id
                        Long positonBoId = (Long) stringObjectMap.get("positonBoId");
                        QFilter qFilter = new QFilter("boid", "=", positonBoId);
                        qFilter.and("iscurrentversion", "=", "1");

                        DynamicObject positions = BusinessDataServiceHelper.loadSingle("hbpm_positionhr", "id,number,name", qFilter.toArray());
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
     * @param teamRoleList   团队经理角色
     * @param leaderRoleList 领导角色
     * @param roleId
     * @param teamPcRoleId
     * @param pernontsprop   人员非时序性实体
     * @param percontact     联系方式实体
     * @param empposorgrel   任职经历实体
     * @param appointments   任职经历实体
     */
    private static void setReportCorelType(DynamicObjectCollection teamRoleList, DynamicObjectCollection leaderRoleList, String roleId, String teamPcRoleId, DynamicObject[] pernontsprop, DynamicObject[] percontact, DynamicObject[] empposorgrel, DynamicObject[] appointments) {
        //获取所有协作关系类型，然后根据协作关系类型分别落下属标表。
        QFilter collaborationTypeFilter = new QFilter("status", "=", "C");
        DynamicObject[] collaborationTypes = BusinessDataServiceHelper.load("hbpm_reportcoreltype", "id,number,name", collaborationTypeFilter.toArray());
        for (DynamicObject collaborationType : collaborationTypes) {
            // 1.根据角色协作类型和指定岗位层级的任职经历岗位查询岗位申请表中，协作关系分录中协作类型为当前协作类型并且协作岗位为当前岗位的所有数据
            List<DynamicObject> underlingList = new ArrayList<>();
            if (!ObjectUtils.isEmpty(collaborationType)) {
                for (DynamicObject appointment : appointments) {
                    //获取岗位
                    DynamicObject position = appointment.getDynamicObject("position");
                    //获取人员
                    DynamicObject person = appointment.getDynamicObject("person");

                    DynamicObject underling = BusinessDataServiceHelper.newDynamicObject("tdkw_appauth_busines_copy");
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
                        QFilter branchUnitFilter = new QFilter("tdkw_position.id", "=", position.getPkValue());
                        branchUnitFilter.and("tdkw_reportcoreltype.id", "=", collaborationType.getPkValue());
                        DynamicObject branchUnit = BusinessDataServiceHelper.loadSingle("tdkw_myteam_branchun_copy", "tdkw_entryentity,tdkw_entryentity.tdkw_branchposition", branchUnitFilter.toArray());
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
                                setUnderLing(pernontsprop, percontact, empposorgrel, subordinateInfoList, branchUnitEntryList, branchPositionIdList);
                                //1.若有岗位但无对应任职人员，则判断该岗位是否有下级岗位;
                                //2.若有下级岗位，且有对应人员则获取下级岗位对应人员为当前岗位对应人员的下属
                                //3.若有下级岗位但无对应人员则重复1-2步骤直至无下级岗位
                                if (branchPositionIdList.size() > 0) {
                                    logger.info("断层岗位id" + branchPositionIdList);
                                    setUnderlingPerson(pernontsprop, percontact, empposorgrel, subordinateInfoList, branchPositionIdList, collaborationType.getLong("id"));
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
                                        }
                                        logger.info("是否有领导角色" + hasLeaderRole);
                                        if (!hasLeaderRole) {
                                            //无领导角色,分配团队角色
                                            if (!hasTeamAppRoleId) {
                                                HRRoleAndPersonUtils.role(cqUser, RoleNumber);
                                            }
                                            if (!hasTeamPcRoleId) {
                                                HRRoleAndPersonUtils.role(cqUser, "XY_TEAM_PC_001");
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
     * @param pernontsprop         人员非时序性属性
     * @param percontact           联系方式
     * @param empposorgrel         任职经历
     * @param subordinateInfoList  人员下属分录
     * @param branchUnitEntryList  下属岗位分录集合
     * @param branchPositionIdList 下属岗位id集合
     */
    private static void setUnderLing(DynamicObject[] pernontsprop, DynamicObject[] percontact, DynamicObject[] empposorgrel, DynamicObjectCollection subordinateInfoList, DynamicObjectCollection branchUnitEntryList, List<Long> branchPositionIdList) {
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
                            for (DynamicObject personNotTiming : pernontsprop) {
                                DynamicObject notTiming = personNotTiming.getDynamicObject("person");
                                if (!ObjectUtils.isEmpty(notTiming)) {
                                    Long notTimingPkValue = (Long) notTiming.getPkValue();
                                    if (notTimingPkValue.compareTo(subordinatePersonPkValue) == 0) {
                                        subordinateInfo.set("tdkw_headsculpture", personNotTiming.getString("headsculpture"));
                                    }
                                }
                            }
                            //遍历联系方式获取手机号码
                            for (DynamicObject contact : percontact) {
                                DynamicObject contactPerson = contact.getDynamicObject("person");
                                if (!ObjectUtils.isEmpty(contactPerson)) {
                                    Long contactPersonPkValue = (Long) contactPerson.getPkValue();
                                    if (subordinatePersonPkValue.compareTo(contactPersonPkValue) == 0) {
                                        //手机号
                                        subordinateInfo.set("tdkw_phone", contact.get("phone"));
                                        //邮箱
                                        subordinateInfo.set("tdkw_peremail", contact.get("busemail"));
                                    }
                                }
                            }

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
    private static void setUnderlingPerson(DynamicObject[] pernontsprop, DynamicObject[] percontact, DynamicObject[] empposorgrel, DynamicObjectCollection subordinateInfoList, List<Long> branchPositionIdList, long collaborationTypeId) {
        QFilter branchUnitFilter = new QFilter("tdkw_position.id", "in", branchPositionIdList);
        branchUnitFilter.and("tdkw_reportcoreltype.id", "=", collaborationTypeId);
        DynamicObject[] branchUnits = BusinessDataServiceHelper.load("tdkw_myteam_branchun_copy", "tdkw_entryentity,tdkw_entryentity.tdkw_branchposition", branchUnitFilter.toArray());

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
                setUnderLing(pernontsprop, percontact, empposorgrel, subordinateInfoList, branchUnitEntryList, positionIdList);
            }
        }
        //递归获取岗位断层下属
        if (positionIdList.size() > 0) {
            setUnderlingPerson(pernontsprop, percontact, empposorgrel, subordinateInfoList, positionIdList, collaborationTypeId);
        }
        logger.info("返回断层下属列表" + subordinateInfoList);
    }
}
