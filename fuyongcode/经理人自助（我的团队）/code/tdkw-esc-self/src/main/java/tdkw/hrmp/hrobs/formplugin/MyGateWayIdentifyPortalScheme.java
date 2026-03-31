package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hrcs.bussiness.servicehelper.perm.RoleServiceHelper;
import kd.sdk.hr.hrmp.hrobs.extpoint.IPortalSchemeExtService;
import kd.sdk.hr.hrmp.hrobs.extpoint.dto.PortalSchemeExtDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Metadata ：
 * @Description ：根据当前用户识别门户视图方案
 * @author xxx
 * @Date ：2023/05/30
 * @Version: 1.0
 */
public class MyGateWayIdentifyPortalScheme implements IPortalSchemeExtService {

    private static final Log logger = LogFactory.getLog(MyGateWayIdentifyPortalScheme.class);

    //领导视图编码
    public String leadNumber = "XY_LEAD_PC_001";

    /**
     * 获取二开埋点的门户视图方案ID
     *
     * @param portalSchemeExtDTO 门户视图方案查询条件
     * @return 门户视图方案ID
     */
    @Override
    public Long matchPortalSchemeId(PortalSchemeExtDTO portalSchemeExtDTO) {
        logger.info("HR自助门户-方案开始识别");
        // 获取用户ID
        Long userId = portalSchemeExtDTO.getUserId();
//      // 获取自然人ID
//      Long personId = portalSchemeExtDTO.getPersonId();
        //获取角色管理ids
        logger.info("HR自助门户-获取角色管理");
        //entityNumber -> hspm_myermanfile--tdkw_statisticentry
        Set<String> userRoleIds =  PermissionServiceHelper.getRolesByUser(userId);
        logger.info("HR自助门户-获取角色管理结果：" + userRoleIds);

        List<String> userRoleIdsList = new ArrayList<String>(userRoleIds);
        Map<String, Map<String, Object>> roleMembers = RoleServiceHelper.getRoleMembers(userRoleIdsList);
        List<String> roleNumber = roleMembers.values().stream().map(item -> (String) item.get("number")).collect(Collectors.toList());
        Map<String, String> roleIdAndNumber = new HashMap<>();
        for (String key : roleMembers.keySet()) {
            Map<String, Object> value = roleMembers.get(key);
            roleIdAndNumber.put((String) value.get("number"), key);
        }

        //角色管理编码- XY_LEAD_PC_001（PC端领导自助通用权限） 固定
        if (roleNumber.contains(leadNumber)) {
            userRoleIds.clear();
            userRoleIds.add(roleIdAndNumber.get(leadNumber));
        }

        QFilter portalFilter = new QFilter("tdkw_role", QCP.in, userRoleIds);
        DynamicObject portalscheme = BusinessDataServiceHelper.loadSingle("hrobs_portalscheme", "id", portalFilter.toArray());
        logger.info("HR自助门户-获取视图方案：" + portalscheme);
        if (portalscheme != null) {
            long portalschemeId = portalscheme.getLong("id");
            logger.info("HR自助门户-获取视图方案id：" + portalschemeId);
            return portalschemeId;
        }
        // 返回门户视图方案（hrobs_portalscheme）ID，返回null或0则会继续执行门户标准逻辑
        //        return null;
        // TODO 临时处理 默认领导视图
        return 1561671618558706688L;

    }
}
