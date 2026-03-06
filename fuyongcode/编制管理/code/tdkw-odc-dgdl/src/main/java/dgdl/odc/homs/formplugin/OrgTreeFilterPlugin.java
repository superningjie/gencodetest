package dgdl.odc.homs.formplugin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.plugin.AbstractTreeListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.list.TreeListModel;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.openservicehelper.permission.HRPermissionServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.model.DimValueResult;
import kd.hr.hrcs.mservice.api.IHRCSBizDataPermissionService;

import java.util.*;

/**
 * @author yaoshuai
 * @date:2023/9/8
 * @description: 末端组织组织树过滤
 */
public class OrgTreeFilterPlugin extends AbstractTreeListPlugin {

    private static Log logger = LogFactory.getLog(OrgTreeFilterPlugin.class);


    /**
     * 应用id
     */
    private static final String APPID = "homs";

    /**
     * 单据体标识
     */
    private static final String ENTRYNAME = "dgdl_networkmanagements";

    //权限项id
    private static final String PERMITEMID = PermissionStatus.View;

    @Override
    public void initializeTree(EventObject e) {
        logger.info("执行初始化方法");
        super.initializeTree(e);
        TreeListModel treeModel = (TreeListModel) this.getTreeModel();

        //获取当前用户
        long userId = UserServiceHelper.getCurrentUserId();

        QFilter qFilter = null;

        //获取有权限的组织
        AuthorizedOrgResult orgSet = HRPermissionServiceHelper.getAuthorizedAdminOrgSet(userId, APPID, ENTRYNAME, PERMITEMID, "dgdl_adminorg");
        logger.info("初始化中的权限=" + orgSet.isHasAllOrgPerm());
        // 获取登录人员拥有权限组织
        List<Long> allPermOrgIds = orgSet.getHasPermOrgs();
        logger.info("allPermOrgIds组织为：" + allPermOrgIds);
        if (!allPermOrgIds.isEmpty()) {
            qFilter = new QFilter("dgdl_adminorg.id", QCP.in, allPermOrgIds);
        }

        //获取用户有权限的纬度值
        DimValueResult resultMap = (DimValueResult) DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSBizDataPermissionService", "getEntityDimValue", userId, APPID, ENTRYNAME, PERMITEMID, "om002");
        logger.info("获取用户有权限的纬度值=" + resultMap);
        if (!resultMap.isAll()) {
            Set<String> dimValueIds = resultMap.getDimValueIds();
            List<Long> dimIds = new ArrayList<>();
            for (String str : dimValueIds) {
                dimIds.add(Long.valueOf(str));
            }
            if (Objects.nonNull(qFilter)) {
                qFilter.and("masterid", QCP.in, dimIds);
            } else {
                qFilter = new QFilter("masterid", QCP.in, dimIds);
            }
        }

        if (orgSet.isHasAllOrgPerm() && resultMap.isAll()) {
            return;
        }

        DynamicObject[] newWorks = BusinessDataServiceHelper.load("dgdl_networkmanagements", "id,dgdl_adminorg.structlongnumber", qFilter.toArray());
        logger.info("OrgTreeFilterPlugin末端组织查询条件=" + qFilter);
        logger.info("OrgTreeFilterPlugin末端组织查询长度=" + newWorks.length);
        Set<String> set = new HashSet<>();
        for (DynamicObject data : newWorks) {
            //获取网点长编码
            String longNumber = data.getString("longnumber");
            logger.info("allPermOrgIds长编码：" + longNumber);
            if (StringUtils.isNotEmpty(longNumber)) {
                String[] splitNumber = longNumber.split("\\.");
                List<String> list = Arrays.asList(splitNumber);
                set.addAll(list);
            }
        }

        List<QFilter> qFilters = new ArrayList<>();
        QFilter endQFilter = new QFilter("number", QCP.in, set);
        qFilters.add(endQFilter);
        logger.info("初始化中查询方法=" + qFilters);
        treeModel.setTreeFilter(qFilters);

    }

}
