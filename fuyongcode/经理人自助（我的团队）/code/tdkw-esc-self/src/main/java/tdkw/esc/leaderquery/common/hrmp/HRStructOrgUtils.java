package tdkw.esc.leaderquery.common.hrmp;

import com.alibaba.fastjson.JSONObject;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.hr.haos.mservice.HAOSStructProjectService;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hrcs.mservice.HRCSBizDataPermissionService;
import tdkw.esc.leaderquery.common.hrmp.HROrgTree;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xxx
 * @Date 2023/10/11 18:02
 * @PackageName:tdkw.esc.tdkw_appauthority.common.hrmp
 * @ClassName: HRStructOrgUtils
 * @Description: TODO
 * @Version 1.0
 */
public class HRStructOrgUtils {
    private static final Log logger = LogFactory.getLog(HRStructOrgUtils.class);


    /**
     * 查询HR角色:员工 分配给用户的实体的HR行政业务组织（事业部架构）
     * 查询 组织职能类型 是HR组织的，其它组织职能的可能会返回空
     * AuthorizedOrgResult.isHasAllOrgPerm() == true 时表示全集团
     *
     * @param userId       苍穹的人员Id
     * @param entityNumber 单据编码
     * @return AuthorizedOrgResult
     * <p>
     * 返回结果没有考虑历史数据，请注意使用方式！！！！！
     */
    public static AuthorizedOrgResult getStructUserAdminOrgs(Long userId, String entityNumber) {
        Map<String, Object> map1 = new HashMap<>();
        List<Long> a = new ArrayList<>();
//        String property = System.getProperty("hrobs.properties.structprojectnumber");
        String property = "SY_1040_S";
        DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("haos_structproject", new QFilter[]{new QFilter("number", QCP.equals, property)});
        if (dynamicObject1 != null){
            a.add(dynamicObject1.getLong("id"));
        }

        //事业部架构参数
        map1.put("hr_dataperm_structprojectid", a);
        String appId = getAppId(entityNumber);//应用Id
        String permItemId = PermissionStatus.View;
        String propKey = "tdkw_hrorg";
        AuthorizedOrgResult authorizedAdminOrgSet = HRCSBizDataPermissionService.getInstance().getUserAdminOrgs(userId, appId, entityNumber, permItemId, propKey, map1);
        return authorizedAdminOrgSet;
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
        //查询缓存中的数据
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
        return bizappid;
    }


    /**
     * HR行政组织（事业部架构），查询上级组织，返回的会包含当前传进来的组织ID
     * 查找该组织的所有上级组织（事业部架构），有多少组织层级就会查询多少次，不会频繁访问数据库
     * 拒绝递归,从我做起 -v-
     *
     * @param orgIds HR组织ID（事业部架构）
     * @return 上级组织ID集合（事业部架构）
     */
    public static List<Long> getParentStructOrg(List<Long> orgIds) {
        // 存储符合的组织ID
        if (orgIds == null || orgIds.isEmpty()) {
            return new ArrayList<>();
        }

        DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("haos_structproject", new QFilter[]{new QFilter("number", QCP.equals, System.getProperty("hrobs.properties.structprojectnumber"))});
        HAOSStructProjectService haosStructProjectService = new HAOSStructProjectService();
//下级组织id集合
        List<Long> ids = new ArrayList<>();
        Map<String, Map<String, Object>> StructInfoMap = haosStructProjectService.queryStructInfoByProIdAndLevel(orgIds, null, dynamicObject1.getLong("id"), 0);
        Iterator StructInfos = StructInfoMap.entrySet().iterator();
        while (StructInfos.hasNext()) {
            Map.Entry<String, Map<String, Object>> next = (Map.Entry<String, Map<String, Object>>) StructInfos.next();
            ids.add((Long) next.getValue().get("parentorg"));
        }
        return ids;
    }

    /**
     * 获取HR行政组织树的JSON格式对象
     * 在有缓存时就把缓存的值直接转为JSON对象，不用先转为OrgTree实体再转JSON，从而提高效率
     * 不包含下级组织
     * 先查询缓存
     *
     * @param orgIds 组织ID
     * @return 组织树实体
     */
    public static JSONObject getHROrgTreeJson(List<Long> orgIds) {
        HROrgTree hrOrgTree = getStructOrgTree(orgIds);
        if (hrOrgTree != null) {
            return HROrgTree.convertHROrgTreeToJson(hrOrgTree);
        } else {
            return null;
        }
    }

    /**
     * 获取HR行政组织树(事业部架构)
     * 不包含下级组织
     * 先查询缓存
     *
     * @param orgIds 组织ID
     * @return 组织树实体
     */
    public static HROrgTree getStructOrgTree(List<Long> orgIds) {
        try {
            if (orgIds == null || orgIds.isEmpty()) {
                return null;
            }
            List<Long> parentHROrg = HRRoleAndPersonUtils.getParentHROrg(orgIds);
            parentHROrg.addAll(orgIds);
            if (parentHROrg.isEmpty()) {
                logger.info("getHROrgTree 获取HR行政组织树, 组织为空, 需要构建组织树的组织ID=" + parentHROrg.toString());
                return null;
            }
            DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("haos_structproject", new QFilter[]{new QFilter("number", QCP.equals, System.getProperty("hrobs.properties.structprojectnumber"))});

            String sql = "select ad.fid,ad.fnumber,ad.fname,st.fparentid fparentid,ad.fsortcode,ad.flevel,ad.findex,ad.fk_tdkw_ncid ncid,ty.fnumber typenumber from t_haos_adminorg ad " +
                    " left join t_haos_adminorgtype ty on ty.fid = ad.fadminorgtypeid " +
                    " left join t_haos_adminstruct st on st.fadminorgid = ad.fid " +
                    " where ad.fiscurrentversion = '1' and ad.fdatastatus = '1'" +
                    "and st.fiscurrentversion='1' and st.fdatastatus = '1'" +
                    "and st.fstructprojectid in (" + dynamicObject1.getLong("id") + ")" +
                    " and ad.fid in (" + convertToLong(parentHROrg) + ") order by ad.flevel,ad.fnumber";
            logger.info("getHROrgTree 获取HR行政组织树, 查询SQL：" + sql);
            DataSet dataSet = DB.queryDataSet("getOrgTree", DBRoute.of("hr"), sql, null);
            HROrgTree orgTree = getStructOrgTree(dataSet);
            if (orgTree == null) {
                logger.info("getHROrgTree 获取HR行政组织树为空");
                return new HROrgTree("100000", "XXX集团", "XXX集团", "00000", "org", "", "0");
            } else {
                logger.info("getHROrgTree 获取HR行政组织树成功");
                return orgTree;
            }

        } catch (Exception e) {
            String error = error(e);
            logger.error(error);
            throw new KDBizException(error);
        }
    }

    /**
     * 构建HR行政组织树
     *
     * @param dataSet HR行政组织
     * @return 组织树实体
     */
    private static HROrgTree getStructOrgTree(DataSet dataSet) {
        logger.info("getOrgTree 开始构建组织树");
        try {
            List<HROrgTree> list = new ArrayList<>();
            Iterator<Row> iterator = dataSet.iterator();
            while (iterator.hasNext()) {
                Row object = iterator.next();
                String id = object.getString("fid");
                String number = object.getString("fnumber");
                String name = object.getString("fname");
                String pid = object.getString("fparentid");
                String sortCode = object.getString("fsortcode");
                String level = object.getString("flevel");
                String type = object.getString("typenumber");
                Integer index = object.getInteger("findex");
                String ncId = object.getString("ncid");

                HROrgTree org = new HROrgTree(id, name, number, sortCode, type, pid, level, index, ncId);
                list.add(org);
            }
            logger.info(" 一共有 " + list.size() + " 个组织");
            Map<String, HROrgTree> nodeMap = new HashMap<>();
            // 构建组织节点映射
            for (HROrgTree node : list) {
                nodeMap.put(node.getId(), node);
            }
            HROrgTree root = null;

            // 遍历节点列表，构建组织树
            for (HROrgTree node : list) {
                String parentId = node.getParentId();
                if (parentId == null || parentId.isEmpty() || StringUtils.equals("0", parentId)) {
                    // 该节点没有父节点，则将其设置为树的根节点
                    root = node;
                } else {
                    //  上级组织
                    HROrgTree parent = nodeMap.get(parentId);
                    if (parent != null) {
                        // 将当前节点添加到父节点的子节点列表中
                        parent.addChild(node);
                    }
                }
            }
            logger.info("getOrgTree 组织树构建完成");
            if (root == null) {
                logger.info(" 但是组织树为空 ");
            }

            return root;
        } catch (Exception e) {
            String error = error(e);
            logger.error(error);
            throw new KDBizException(error);
        }
    }

    /**
     * HR行政组织（事业部架构）
     * 查找该部门或公司的所有下级组织，有多少组织层级就会查询多少次，不会频繁访问数据库
     *
     * @param orgId 组织Id
     * @return 下级组织集合（事业部架构）
     */
    public static List<Long> getStructOrgIds(String orgId, int level) {
        List<Long> list = new ArrayList<>();
        if (StringUtils.isEmpty(orgId)) {
            return list;
        }
        // TODO 缺少配置
//        String property = System.getProperty("hrobs.properties.structprojectnumber");
        String property = "SY_1040_S";
        DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("haos_structproject", new QFilter[]{new QFilter("number", QCP.equals, property)});

        HAOSStructProjectService haosStructProjectService = new HAOSStructProjectService();
        //组织id
        List<Long> idss = new ArrayList<>();
        idss.add(Long.valueOf(orgId));
        //下级组织id集合
        List<Long> ids = new ArrayList<>();
        Map<String, Map<String, Object>> StructInfoMap = haosStructProjectService.queryStructInfoByProIdAndLevel(idss, null, dynamicObject1.getLong("id"), level);
        Iterator StructInfos = StructInfoMap.entrySet().iterator();
        while (StructInfos.hasNext()) {
            Map.Entry<String, Map<String, Object>> next = (Map.Entry<String, Map<String, Object>>) StructInfos.next();
            ids.add(Long.valueOf(next.getKey()));
        }


        return ids;
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

    private static String convertToLong(List<Long> ids) {
        StringBuilder sd = new StringBuilder();
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        int size = ids.size();
        for (int i = 0; i < size; i++) {
            Long id = ids.get(i);
            sd.append(id);
            if (i < size - 1) {
                sd.append(",");
            }
        }
        return sd.toString();
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
     * 异常捕捉
     *
     * @param e Exception
     * @return 错误信息
     */
    public static String error(Exception e) {
        StringBuilder sd = new StringBuilder();
        if (e == null) {
            return "";
        }
        StackTraceElement[] stackTrace = e.getStackTrace();
        sd.append("Error: ").append(e.getMessage()).append("\n");
        if (stackTrace != null) {
            for (StackTraceElement element : stackTrace) {
                sd.append("\tat ").append(element.toString()).append("\n");
            }
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            sd.append("Error: ").append(cause.getMessage()).append("\n");
            StackTraceElement[] stackTrace1 = cause.getStackTrace();
            if (stackTrace1 != null) {
                for (StackTraceElement element : stackTrace1) {
                    sd.append("\tat ").append(element.toString()).append("\n");
                }
            }
        }

        return sd.toString();
    }
}
