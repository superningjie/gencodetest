package tdkw.esc.leaderquery.integratedquery.report;

import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.List;

/**
 * ReportOrgScopeUtils
 *
 * @author xxx
 * @date 2023/7/12
 */
public class ReportOrgScopeUtils {
    /**
     * 报表获取权限项过滤器
     *
     * @param roleManagerStr 权限管理角色管理编码
     * @param fieldName      组织对应字段名
     * @return
     */
    public static QFilter getOrgFilter(String roleManagerStr, String fieldName) {
        QFilter orgQFilter;
        //获取角色权限
        AuthorizedOrgResult statisticEntry = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), roleManagerStr);
        //是否有所有组织(1000L)权限
        boolean hasAllOrgPerm = statisticEntry.isHasAllOrgPerm();
        if (hasAllOrgPerm) {
            orgQFilter = new QFilter("1", QCP.equals, 1);
        } else {
            List<Long> hasPerOrg = statisticEntry.getHasPermOrgs();
            orgQFilter = new QFilter(fieldName, QCP.in, hasPerOrg);
        }
        return orgQFilter;

    }
}
