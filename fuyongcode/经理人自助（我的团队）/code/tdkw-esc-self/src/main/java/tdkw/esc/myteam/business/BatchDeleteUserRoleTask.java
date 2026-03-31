package tdkw.esc.myteam.business;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 调度计划
 * 删除 职层不在【e1-e3  &  m1-3】，拥有角色为【TEAM-APP-001 & TEAM-PC-001】的人员
 */
public class BatchDeleteUserRoleTask extends AbstractTask {

    private static final Log LOGGER = LogFactory.getLog(BatchDeleteUserRoleTask.class);

    /**
     * 需要操作的职层编码集合
     */
    private final static List<String> LIST = new ArrayList<>();

    static {
        LIST.add("E1");
        LIST.add("E2");
        LIST.add("E3");
        LIST.add("M1");
        LIST.add("M2");
        LIST.add("M3");
    }

    private static final List<String> ROLE = new ArrayList<>();
    static {
        ROLE.add("TEAM-APP-001");
        ROLE.add("TEAM-PC-001");
    }

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        //查询指定岗位层级对应的岗位的任职经历的人员
        QFilter appointmentFilter = new QFilter("tdkw_ranks.number", QCP.not_in, LIST);
        appointmentFilter.and("businessstatus", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] coll = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person", appointmentFilter.toArray());
        //人员ID
        Set<Long> set = new HashSet<>();
        for (DynamicObject object : coll) {
            DynamicObject person = object.getDynamicObject("person");
            if (person != null) {
                set.add((Long) person.getPkValue());
            }
        }
        //HR用户ID 查询 苍穹的用户ID
        List<Long> cqUser = HRRoleAndPersonUtils.getCQUser(new ArrayList<>(set));
        //获取这些角色的角色id
        List<String> list = HRRoleAndPersonUtils.getBaseStr(ROLE, "number", "perm_role", "id", QCP.in);
        //批量查询用户已分配的所有HR角色ID
        Map<Long, List<String>> userRoles = HRRoleAndPersonUtils.getUserRoles(cqUser);
        Set<Long> userIds = new HashSet<>();
        for (Long personId : cqUser) {
            List<String> roleIds = userRoles.get(personId);
            roleIds.retainAll(list);
            if (!roleIds.isEmpty()) {
                userIds.add(personId);
            }
        }
        LOGGER.info("BatchDeleteUserRoleTask:需要删除领导角色的UserId="+userIds);
        //删除角色
        for (Long user : userIds) {
            for (String number : ROLE) {
                HRRoleAndPersonUtils.deleteUserRole(user, number);
            }
        }
    }
}
