package tdkw.hrmp.hrobs.formplugin.induction;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * PageCachePlugin
 *
 * @author xxx
 * @date 2023/10/16
 */
public class PageCachePlugin extends AbstractFormPlugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        putCache();
    }

    private void putCache() {
        Long org;
        // 获取权限放入缓存
        AuthorizedOrgResult orgScope = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), this.getModel().getDataEntityType().getName(), "tdkw_org");
        // 有全部组织权限
        if (orgScope.isHasAllOrgPerm()) {
            this.getPageCache().put("isHasAllOrgPerm", "true");

            // 2025-10-31 增加报表过滤，当前人组织
            //获取当前登录人员所属组织
            List<Long> userIds = new ArrayList<>(1);
            userIds.add(UserServiceHelper.getCurrentUserId());
            // 获取当前登录人员所属组织
            Long adminOrg = UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId());
            this.getPageCache().put("tdkw_org", String.valueOf(adminOrg));
//            org = 100000L;
//            this.getPageCache().put("tdkw_org", String.valueOf(org));
        } else {
            this.getPageCache().put("isHasAllOrgPerm", "false");
            List<Long> hasPermOrgs = orgScope.getHasPermOrgs();
            this.getPageCache().put("orgScope", SerializationUtils.toJsonString(hasPermOrgs));
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("haos_adminorghr", "id", new QFilter[]{new QFilter("orgtype.number", QCP.in, new String[]{"XY00001", "XY00002", "XY00003", "XY00004", "XY00005"})
                    .and("id", QCP.in, hasPermOrgs).and("datastatus", QCP.equals, "1").and("enable", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1")}, "sortcode");
            if (null != dynamicObjects && dynamicObjects.length > 0) {
                org = dynamicObjects[0].getLong("id");
                this.getPageCache().put("tdkw_org", String.valueOf(org));
            }
        }
    }
}