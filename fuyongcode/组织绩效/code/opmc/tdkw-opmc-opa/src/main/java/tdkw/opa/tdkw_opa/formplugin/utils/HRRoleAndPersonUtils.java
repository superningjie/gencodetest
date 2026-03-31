package tdkw.opa.tdkw_opa.formplugin.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.entity.EntityItem;
import kd.bos.metadata.entity.EntityMetadata;
import kd.bos.metadata.entity.commonfield.ComboField;
import kd.bos.metadata.entity.commonfield.ComboItem;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.common.api.EnumResponseCode;
import kd.hr.hbp.common.api.HrApiResponse;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.model.perm.DimGroupParam;
import kd.hr.hbp.common.model.perm.DimValueParam;
import kd.hr.hbp.common.model.perm.UserBucaPermDataParam;
import kd.hr.hrcs.bussiness.service.perm.HRPermCacheMgr;
import kd.hr.hrcs.bussiness.service.perm.check.helper.UserRoleServiceHelper;
import kd.hr.hrcs.common.model.UserRoleInfo;
import tdkw.esc.tdkw_appauthority.common.hrmp.HROrgTree;
import tdkw.esc.tdkw_appauthority.common.hrmp.HRUserRoleCacheUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <html lang="en">
 * <head>
 * </head>
 * <body>
 *      <div class="gradient">
 *          <h1 style="color: #C00000;">免责声明(bushi</h1>
 *          <h2 style="color: #FFFF00;">复制该插件的方法到自己的类中时</h2>
 *          <h2 style="color: #FFFF00;">  可能会产生运行报错、死循环、代码编译报错等等</h2>
 *          <h2 style="color: #FFFF00;">请及时更新代码</h2>
 *          <h1 style="color: #BBBBBB;">综合工具类</h1>
 *          <h2 style="color: #FFFF00;">角色相关</h2>
 *          <ol>
 *          <li>getUserROleEntity：判断该用户的角色是否有该实体</li>
 *          <li>getIsRole：判断该用户是否分配了该角色</li>
 *          <li>getRoleEntity：查询该实体分配给了哪些角色</li>
 *          <li>getUserRoleInEntity：批量查询用户已分配的角色的ID集合，角色的功能权限包含该单据实体</li>
 *          <li>getUserRoleIds：查询用户已分配的角色的ID集合，角色的功能权限包含该单据实体</li>
 *          <li>getUserRole：查询用户已分配的所有HR角色ID</li>
 *          <li>getUserRoles：批量查询用户已分配的所有HR角色ID</li>
 *          <li>getUserRole：查询用户已分配的角色info，角色的功能权限包含该单据实体和查询权限项</li>
 *          <li>role：给角色分配用户，组织为最高级组织(id=100000)，不包含下级</li>
 *          <li>batchRole：批量给角色分配角色</li>
 *          <li>addUserRole：给角色分配用户</li>
 *          <li>deleteUserRole：删除角色中已分配的用户</li>
 *          <li>getDataRuleQuery：获取角色的数据范围中的高级配置的已配置查询权限的人员编码</li>
 *          <li>getDataRule：取角色的数据范围中的高级配置的已配置权限的人员编码</li>
 *          <li>getRoleEntityNumber：查询该实体分配给了哪些角色</li>
 *          </ol>
 *          <h2 style="color: #FFFF00">人员相关</h2>
 *          <ol>
 *          <li>getMyTeamPersonAgeInfo：获取HR人员的团队人员的平均年龄，人数，司龄</li>
 *          <li>getSubordinatePersonnel：获取该HR人员的下属人员</li>
 *          <li>getCQUser：通过HR人员Id查询对应的苍穹人员Id</li>
 *          <li>getHRUser：通过苍穹人员Id查询对应的HR人员Id</li>
 *          </ol>
 *          <h2 style="color: #FFFF00">组织相关</h2>
 *          <ol>
 *          <li>getHROrgTreeComplete：获取HR行政组织树，包含这些组织的所有下级组织</li>
 *          <li>getHROrgTree：获取HR行政组织树，不包含下级组织</li>
 *          <li>getAuthorizedAdminOrgSet：查询HR角色:员工 分配给用户的实体的HR行政业务组织</li>
 *          <li>getRoleOrgs：查询该人员、角色分配的组织范围，不是行政业务组织</li>
 *          <li>getAuthorizedAdminOrgSetAll：查询通用角色的数据范围中的行政类组织团队，该方法请谨慎使用</li>
 *          <li>getAuthorizedAdminOrgID：查询用户有该单据的角色的数据范围中的行政组织ID</li>
 *          <li>getDataQF：获取行政组织范围和数据规则方案的Qfilter</li>
 *          <li>getHROrgIds：HR行政组织，单个查找该部门或公司的所有下级组织</li>
 *          <li>getHROrgIds：HR行政组织，批量查找这些部门或公司的所有下级组织</li>
 *          <li>getOrgIds：bos_org业务单元，查找该部门或公司的所有下级组织</li>
 *          <li>getParentHROrg：HR行政组织，查找该组织的所有上级组织</li>
 *          </ol>
 *          <h2 style="color: #FFFF00">其余方法</h2>
 *          <ol>
 *          <li>getComboField：获取单据中下拉列表字段的下拉值和标题，不能获取单据体中的字段</li>
 *          <li>getCloud：查询业务云的ID</li>
 *          <li>getAppIds：批量查询应用Id</li>
 *          <li>getBaseStr：查询基础资料 String类型的字段</li>
 *          <li>getBaseLong：查询基础资料 Long类型的字段</li>
 *          <li>error：异常捕捉，返回报错信息以及报错的类、方法和行数</li>
 *          <li>clearAllCache：删除 HRPermCacheMgr 和 HRUserRoleCacheUtils 的所有缓存</li>
 *          </ol>
 *      </div>
 *  </body>
 * </html>
 *
 * @author hgl
 */
public class HRRoleAndPersonUtils {

    private static final Log logger = LogFactory.getLog(HRRoleAndPersonUtils.class);


    /**
     * 删除 HRPermCacheMgr 和 HRUserRoleCacheUtils 的所有缓存
     * 更新数据后没有生效时使用
     */
    public static void clearAllCache() {
        HRUserRoleCacheUtils.clearAllCache();
        HRPermCacheMgr.clearAllCache();
    }

    /**
     * 获取单据中下拉列表字段的下拉值和标题，不能获取单据体中的字段
     *
     * @param billentity 单据标识
     * @param property   字段标识
     * @return map {下拉值，下拉标题}
     */
    public static Map<String, String> getComboField(String billentity, String property) {
        String cacheType = HRUserRoleCacheUtils.getTypeComboField();
        String cacheKey = billentity + "_" + property;
        String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, cacheKey);
        if (StringUtils.isEmpty(cacheVal)) {
            Map<String, String> map = new HashMap<>();
            String id = MetadataDao.getIdByNumber(billentity, MetaCategory.Form);
            EntityMetadata entityMeta = (EntityMetadata) MetadataDao.readRuntimeMeta(id, MetaCategory.Entity);
            List<EntityItem<?>> items = entityMeta.getRootEntity().getItems();
            for (EntityItem<?> item : items) {
                String key = item.getKey();
                if (StringUtils.equals(property, key)) {
                    ComboField combo = (ComboField) item;
                    List<ComboItem> comboItems = combo.getItems();
                    for (ComboItem comboItem : comboItems) {
                        String value = comboItem.getValue();
                        String cnName = comboItem.getCaption().getLocaleValue_zh_CN();
                        map.put(value, cnName);
                    }
                    break;
                }
            }
            HRUserRoleCacheUtils.putCache(cacheType, cacheKey, SerializationUtils.toJsonString(map));
            return map;
        } else {
            return (Map) SerializationUtils.fromJsonString(cacheVal, Map.class);
        }
    }

    /**
     * 获取有查询权限的业务单元。（带职能类型参数）
     *
     * @param userId      苍穹用户ID
     * @param orgViewType 职能类型
     * @param entityNum   实体编码
     * @return 即使HasPermOrgResult的hasAllOrgPerm（）为true，如果mustQuery为true，HasPermOrgResult的getHasPermOrgs()返回值就是有权的组织范围，
     * 否则当HasPermOrgResult的hasAllOrgPerm（）为true时，HasPermOrgResult的getHasPermOrgs()返回值的内容为空，但表示有权范围为所有业务单元。
     * 当HasPermOrgResult的hasAllOrgPerm（）为false时， getHasPermOrgs()返回值的内容就是有权的业务单元范围。
     */
    public static HasPermOrgResult getAllPermOrgs(long userId, String orgViewType, String entityNum) {
        String appId = getAppId(entityNum);//应用Id
        //  指定查询权限项, 并查出组织列表内容
        HasPermOrgResult allPermOrgs = PermissionServiceHelper.getAllPermOrgs(userId, orgViewType, appId, entityNum, PermissionStatus.View, true);

        List<Long> hasPermOrgs = allPermOrgs.getHasPermOrgs();
        System.out.println(hasPermOrgs);
        return allPermOrgs;
    }

    /**
     * 获取有查询权限的业务单元。（不带职能类型参数）
     *
     * @param userId    苍穹用户ID
     * @param entityNum 实体编码
     * @return 即使HasPermOrgResult的hasAllOrgPerm（）为true，如果mustQuery为true，HasPermOrgResult的getHasPermOrgs()返回值就是有权的组织范围，
     * 否则当HasPermOrgResult的hasAllOrgPerm（）为true时，HasPermOrgResult的getHasPermOrgs()返回值的内容为空，但表示有权范围为所有业务单元。
     * 当HasPermOrgResult的hasAllOrgPerm（）为false时， getHasPermOrgs()返回值的内容就是有权的业务单元范围。
     */
    public static HasPermOrgResult getAllPermOrgs(long userId, String entityNum) {
        String appId = getAppId(entityNum);//应用Id
        //  指定查询权限项, 并查出组织列表内容
        HasPermOrgResult allPermOrgs = PermissionServiceHelper.getAllPermOrgs(userId, appId, entityNum, PermissionStatus.View);
        return allPermOrgs;
    }

    /**
     * List<Map<String, Object>>排序
     *
     * @param list list
     * @param key  键
     */
    public static void sortMap(List<Map<String, Object>> list, String key) {
        List<String> sort = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Object o = map.get(key);
            String index = (String) o;
            sort.add(index);
        }
        Collections.sort(sort);
        List<Map<String, Object>> list2 = new ArrayList<>();
        for (String index : sort) {
            for (Map<String, Object> map : list) {
                Object o = map.get(key);
                String index2 = (String) o;
                if (index.equals(index2)) {
                    list2.add(map);
                }
            }
        }
        list.clear();
        list.addAll(list2);
    }

    /**
     * 获取人员信息
     *
     * @param ids HR人员ID
     * @return info
     */
    public static Map<Long, Map<String, Object>> getPersonInfo(List<Long> ids) {
        Map<Long, Map<String, Object>> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        try {
            String toLong = convertToLong(ids);
            String sql = "select person.fid id, person.fname name, person.fnumber number, person.fheadsculpture headsculpture, person.fk_tdkw_head_thumbnail thumbnail, " +
                    " pernon.fage age, " +
                    " len.fworkyear workyear, len.fcomsercount comsercount, " +
                    " dip.fname eduname " +
                    " from t_hrpi_person person " +
                    " left join t_hrpi_pernontsprop pernon on pernon.fpersonid = person.fid and pernon.fiscurrentversion = '1' and pernon.fdatastatus = '1' " +
                    " left join t_hrpi_perserlen len on len.fpersonid = person.fid and len.fiscurrentversion = '1' and len.fdatastatus = '1' " +
                    " left join t_hrpi_pereduexp edu on edu.fpersonid = person.fid and edu.fiscurrentversion = '1' and edu.fdatastatus = '1' and edu.fishighestdegree = '1' " +
                    " left join t_hbss_diploma dip on dip.fid = edu.feducationid" +
                    " where person.fid in (" + toLong + ") ";
            logger.info("HRRoleAndPersonUtils.getPersonInfo 查询SQL：" + sql);
            DataSet dataSet = DB.queryDataSet("HRRoleAndPersonUtils.getPersonInfo", DBRoute.of("hr"), sql, null);
            Iterator<Row> iterator = dataSet.iterator();
            while (iterator.hasNext()) {
                Row next = iterator.next();
                Map<String, Object> permap = new HashMap<>();
                //HR人员的ID
                permap.put("id", next.getLong("id"));
                //姓名
                permap.put("name", next.getString("name"));
                //工号
                permap.put("number", next.getString("number"));
                //人员的pkid
                permap.put("pkid", next.getString("pkid"));
                //人员的ncId
                permap.put("ncId", next.getString("pkid"));
                //人员的年龄
                permap.put("age", next.getInteger("age"));
                //人员的工龄
                BigDecimal workyear = next.getBigDecimal("workyear");
                permap.put("workyear", workyear == null ? 0.0 : workyear.setScale(1, RoundingMode.HALF_UP));
                //人员的司龄
                BigDecimal comsercount = next.getBigDecimal("comsercount");
                permap.put("comsercount", comsercount == null ? 0.0 : comsercount.setScale(1, RoundingMode.HALF_UP));
                //学历
                permap.put("eduname", next.getString("eduname"));
                //头像原图
                permap.put("headsculpture", next.getString("headsculpture"));
                //头像缩略图
                permap.put("thumbnail", next.getString("thumbnail"));

                map.put(next.getLong("id"), permap);
            }
        } catch (Exception e) {
            String error = error(e);
            logger.info("HRRoleAndPersonUtils.getPersonInfo 获取人员信息时出现错误：" + error);
            throw new KDBizException("获取人员信息时出现错误：" + error);
        }

        return map;
    }

    public static Map<Long, Map<String, Object>> getEmpPosOrgRel(List<Long> empIds) {
        Map<Long, Map<String, Object>> map = new HashMap<>();
        if (empIds == null || empIds.isEmpty()) {
            return map;
        }
        String toLong = convertToLong(empIds);
        String sql = "select emp.fpersonid id, emp.fisprimary, position.fid posid, laborrel.fname laname, " +
                " position.fname postname, company.fname copname, adminorg.fname adminname, erm.fk_tdkw_index findex " +
                " from t_hrpi_empposorgrel emp " +
                " left join t_hbss_laborreltype laborrel on laborrel.fid = emp.fk_tdkw_employtype " +
                " left join t_hbpm_position position on position.fid = emp.fpositionid " +
                " left join t_hbjm_jobgrade job on job.fjobgradeid = emp.fk_tdkw_ranks " +
                " left join t_haos_adminorg company on company.fid = emp.fcompanyid " +
                " left join t_haos_adminorg adminorg on adminorg.fid = emp.fadminorgid " +
                " left join t_hrpi_ermanfile erm on erm.fempposrelid = emp.fid and erm.fiscurrentversion = '1' and erm.fdatastatus = '1' " +
                " where emp.fid in (" + toLong + ") order by findex";
        logger.info("HRRoleAndPersonUtils.getPositionInfo 查询SQL：" + sql);
        DataSet dataSet = DB.queryDataSet("HRRoleAndPersonUtils.getPositionInfo", DBRoute.of("hr"), sql, null);
        List<Long> list = new ArrayList<>();
        Iterator<Row> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            Row next = iterator.next();
            Map<String, Object> permap;
            Long id = next.getLong("id");
            list.add(id);
            Map<String, Object> objectMap = map.get(id);
            List<Map<String, Object>> arrayList = new ArrayList<>();
            if (objectMap == null || objectMap.isEmpty()) {
                permap = new HashMap<>();
                permap.put("id", id);
            } else {
                permap = objectMap;
                arrayList = (List<Map<String, Object>>) objectMap.get("partjobs");
            }
            //是否主职
            String fisprimary = next.getString("fisprimary");
            if (StringUtils.equals("1", fisprimary)) {
                permap.put("isMainJob", "Y");
                permap.put("postname", next.getString("postname"));
                permap.put("psnclname", next.getString("laname"));
                String index = next.getString("findex");
                permap.put("index", StringUtils.isEmpty(index) ? "" : index);
            } else {
                if (arrayList == null) {
                    arrayList = new ArrayList<>();
                }
                Map<String, Object> permap2 = new HashMap<>();
                permap2.put("pk", next.getString("posid"));
                permap2.put("postname", next.getString("postname"));
                permap2.put("orgname", next.getString("copname"));
                permap2.put("deptname", next.getString("adminname"));
                arrayList.add(permap2);
                permap.put("partjobs", arrayList);
            }

            map.put(id, permap);
        }
        Map<String, Object> listMap = new HashMap<>();
        listMap.put("person", list);
        map.put(-10L, listMap);

        return map;
    }

    /**
     * 获取人员主任职的所属公司
     *
     * @param personId 人员ID
     * @return 组织ID
     */
    public static List<Long> getPersonOrgId(List<Long> personId) {
        if (personId == null || personId.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> orgIds = new HashSet<>();
        QFilter dataStatusQF = new QFilter("datastatus", QCP.equals, "1");
        QFilter isPrimaryQF = new QFilter("isprimary", QCP.equals, "1");
        QFilter businessStatusQF = new QFilter("businessstatus", QCP.equals, "1");
        QFilter isCurrentVerSionQF = new QFilter("iscurrentversion", QCP.equals, "1");
        QFilter personQF = new QFilter("person.id", QCP.in, personId);
        //任职经历
        DynamicObjectCollection query = QueryServiceHelper.query("hrpi_empposorgrel", "company.id", new QFilter[]{dataStatusQF, isPrimaryQF, businessStatusQF, isCurrentVerSionQF, personQF});
        for (DynamicObject object : query) {
            orgIds.add(object.getLong("company.id"));
        }

        return new ArrayList<>(orgIds);
    }

    /**
     * 获取该人员与岗位的所属公司
     *
     * @param map 人员ID--岗位集合
     * @return 组织ID
     */
    public static List<Long> getPersonOrgId(Map<Long, Set<Long>> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> set = map.keySet();
        Set<Long> orgIds = new HashSet<>();
        QFilter dataStatusQF = new QFilter("datastatus", QCP.equals, "1");
        QFilter businessStatusQF = new QFilter("businessstatus", QCP.equals, "1");
        QFilter isCurrentVerSionQF = new QFilter("iscurrentversion", QCP.equals, "1");
        QFilter personQF = new QFilter("person.id", QCP.in, set);
        //任职经历
        DynamicObjectCollection query = QueryServiceHelper.query("hrpi_empposorgrel", "person.id,company.id,position.id", new QFilter[]{dataStatusQF, businessStatusQF, isCurrentVerSionQF, personQF});
        for (DynamicObject object : query) {
            long personId = object.getLong("person.id");
            long position = object.getLong("position.id");
            Set<Long> longSet = map.get(personId);
            if (longSet.contains(position)) {
                long company = object.getLong("company.id");
                orgIds.add(company);
            }
        }

        return new ArrayList<>(orgIds);
    }

    /**
     * 根据人员在任职经历中主职或者兼职的所属部门，去查询上级部门组织，最后根据这些组织构建组织树
     * 要求速度尽量低于 200ms
     *
     * @param personId 人员ID
     * @return 组织树
     */
    public static HROrgTree getHROrgTreeLong(List<Long> personId) {
        if (personId == null || personId.isEmpty()) {
            return null;
        }
        String cacheType = HRUserRoleCacheUtils.getTypeHROrgTreeLong();
        String cacheKey = personId + "_" + "HR_ORG_LONG";
        String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, cacheKey);
        logger.info("getHROrgTreeLong 获取HR行政组织树, 缓存是否为空：" + StringUtils.isEmpty(cacheVal));
        if (StringUtils.isEmpty(cacheVal)) {
            String ids = convertToLong(personId);
            //查询这些人员的主任职的任职经历的所属部门
            String sql = " select ad.fid, ad.fsortcode, ad.flevel " +
                    " from t_hrpi_person person " +
                    " inner join t_hrpi_empposorgrel emp on emp.fpersonid = person.fid and emp.fiscurrentversion = '1' and emp.fdatastatus = '1' and emp.fisprimary = '1' and emp.fbusinessstatus = '1' " +
                    " left join t_haos_adminorg ad on ad.fid = emp.fadminorgid " +
                    " where person.fid in (" + ids + ") " +
                    " order by ad.flevel desc ";
            DataSet dataSet = DB.queryDataSet("HRRoleAndPersonUtils.getHROrgTreeCustom", DBRoute.of("hr"), sql, null);
            Set<String> adminLongNumber = new HashSet<>();
            // 组织层级
            int max = 0;
            int min = 999;
            Iterator<Row> iterator = dataSet.iterator();
            while (iterator.hasNext()) {
                Row next = iterator.next();
                String sortCode = next.getString("fsortcode");
                adminLongNumber.add(sortCode);
            }
            HROrgTree orgTree = getHROrgTreeToLongNumber(adminLongNumber);
            if (orgTree == null) {
                logger.info("getHROrgTreeLong 获取HR行政组织树为空");
                return new HROrgTree("100000", "XXXX集团", "XXXX集团", "00000", "org", "", "0");
            } else {
                HRUserRoleCacheUtils.putCache(cacheType, cacheKey, orgTree.toString());
                return orgTree;
            }
        } else {
            return HROrgTree.convertJsonToHROrgTree(JSONObject.parseObject(cacheVal));
        }
    }

    private static HROrgTree getHROrgTreeToLongNumber(Set<String> longNumber) {
        Set<String> numbers = new HashSet<>();
        for (String s : longNumber) {
            extractCharactersBeforeLastExclamation(s, numbers);
        }
        numbers.addAll(longNumber);
        String convert = convertToString(new ArrayList<>(numbers));
        if (StringUtils.isEmpty(convert)) {
            return null;
        }
        String sql = "select ad.fid,ad.fnumber,ad.fname,ad.fparentid,ad.fsortcode,ad.flevel,ad.findex,ty.fnumber typenumber from t_haos_adminorg ad " +
                " left join t_haos_adminorgtype ty on ty.fid = ad.fadminorgtypeid " +
                " where ad.fiscurrentversion = '1' and ad.fdatastatus = '1'" +
                " and ad.fsortcode in (" + convert + ") order by ad.flevel,ad.fnumber";
        logger.info("getHROrgTreeToLongNumber 获取HR行政组织树, 查询SQL：" + sql);
        DataSet dataSet = DB.queryDataSet("getOrgTree", DBRoute.of("hr"), sql, null);

        return getOrgTree(dataSet);
    }

    /**
     * 获取HR行政组织树
     * 包含这些组织的所有下级组织
     * 先查询缓存
     *
     * @param orgIds 组织ID
     * @return 组织树实体
     */
    public static HROrgTree getHROrgTreeComplete(List<Long> orgIds) {
        List<Long> ids = getHROrgIds(orgIds);
        return getHROrgTree(ids);
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
        String cacheType = HRUserRoleCacheUtils.getTypeHROrgTree();
        String cacheKey = orgIds + "_" + "HR_ORG";
        String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, cacheKey);
        if (StringUtils.isEmpty(cacheVal)) {
            HROrgTree hrOrgTree = getHROrgTree(orgIds);
            if (hrOrgTree != null) {
                return HROrgTree.convertHROrgTreeToJson(hrOrgTree);
            } else {
                return null;
            }
        } else {
            return JSONObject.parseObject(cacheVal);
        }
    }

    /**
     * 获取HR行政组织树
     * 不包含下级组织
     * 先查询缓存
     *
     * @param orgIds 组织ID
     * @return 组织树实体
     */
    public static HROrgTree getHROrgTree(List<Long> orgIds) {
        try {
            if (orgIds == null || orgIds.isEmpty()) {
                return null;
            }
            String cacheType = HRUserRoleCacheUtils.getTypeHROrgTree();
            String cacheKey = orgIds + "_" + "HR_ORG";
            String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, cacheKey);
            logger.info("getHROrgTree 获取HR行政组织树, 缓存是否为空：" + StringUtils.isEmpty(cacheVal));
            if (StringUtils.isEmpty(cacheVal)) {
                List<Long> parentHROrg = getParentHROrg(orgIds);
                parentHROrg.addAll(orgIds);
                if (parentHROrg.isEmpty()) {
                    logger.info("getHROrgTree 获取HR行政组织树, 组织为空, 需要构建组织树的组织ID=" + parentHROrg.toString());
                    return null;
                }
                String sql = "select ad.fid,ad.fnumber,ad.fname,ad.fparentid,ad.fsortcode,ad.flevel,ad.findex,ty.fnumber typenumber from t_haos_adminorg ad " +
                        " left join t_haos_adminorgtype ty on ty.fid = ad.fadminorgtypeid " +
                        " where ad.fiscurrentversion = '1' and ad.fdatastatus = '1'" +
                        " and ad.fid in (" + convertToLong(parentHROrg) + ") order by ad.flevel,ad.fnumber";
                logger.info("getHROrgTree 获取HR行政组织树, 查询SQL：" + sql);
                DataSet dataSet = DB.queryDataSet("getOrgTree", DBRoute.of("hr"), sql, null);
                HROrgTree orgTree = getOrgTree(dataSet);
                if (orgTree == null) {
                    logger.info("getHROrgTree 获取HR行政组织树为空");
                    return new HROrgTree("100000", "XXXX集团", "XXXX集团", "00000", "org", "", "0");
                } else {
                    HRUserRoleCacheUtils.putCache(cacheType, cacheKey, orgTree.toString());
                    logger.info("getHROrgTree 获取HR行政组织树成功, 并写入缓存");
                    return orgTree;
                }
            } else {
                return HROrgTree.convertJsonToHROrgTree(JSONObject.parseObject(cacheVal));
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
    private static HROrgTree getOrgTree(DataSet dataSet) {
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
     * 查询通用角色的数据范围中的行政类组织团队
     * 注意：该方法会先去取缓存中的
     * 注意：角色编码为空时则查询员工的所有HR角色
     * 注意：不会判断人员的角色范围属性
     * 该方法请谨慎使用
     *
     * @param userId 苍穹用户ID
     * @param role   角色编码
     * @return 组织集合
     */
    public static Set<String> getAuthorizedAdminOrgSetAll(Long userId, String role) {
        Set<String> set = new HashSet<>();
        if (userId == null) {
            return set;
        }
        String cacheType = HRUserRoleCacheUtils.getTypeOrgSetAll();
        String cacheKey = userId + role;
        String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, cacheKey);
        if (StringUtils.isEmpty(cacheVal)) {
            QFilter qf;
            if (StringUtils.isEmpty(role)) {
                List<String> userRole = getUserRole(userId);
                qf = new QFilter("role.id", QCP.in, userRole);
            } else {
                //判断该用户是否分配了该角色
                boolean isRole = getIsRole(userId, role);
                if (!isRole) {
                    return set;
                } else {
                    //角色id
                    String id = getBaseStr(role, "number", "perm_role", "id");
                    qf = new QFilter("role.id", QCP.equals, id);
                }
            }
            //行政类组织团队 维度id
            Long dimId = getBaseLong("adminorgteam", "number", "hrcs_dimension", "id");

            QFilter qf1 = new QFilter("entry.dimension.id", QCP.equals, dimId);
            DynamicObject[] entities = BusinessDataServiceHelper.load("hrcs_roledimgrp", "role,bucafunc,entry,entry.dimval,entry.containssub", qf.and(qf1).toArray());
            for (DynamicObject dynamicObject : entities) {
                DynamicObjectCollection entry = dynamicObject.getDynamicObjectCollection("entry");
                for (DynamicObject object : entry) {
                    String dimval = object.getString("dimval");
                    if (StringUtils.isNotEmpty(dimval)) {
                        //是否包含下级
                        boolean containssub = object.getBoolean("containssub");
                        if (containssub) {
                            //查询所有下级
                            List<Long> orgIds = getHROrgIds(dimval);
                            for (Long number : orgIds) {
                                set.add(String.valueOf(number));
                            }
                        } else {
                            set.add(dimval);
                        }
                    }
                }
            }
            HRUserRoleCacheUtils.putCache(cacheType, cacheKey, String.valueOf(set));
            return set;
        } else {
            return convertSetString(SerializationUtils.fromJsonString(cacheVal, Set.class));
        }
    }

    /**
     * 获取HR人员的团队人员的平均年龄，人数，司龄
     *
     * @param id 苍穹人员Id
     * @return JSON
     */
    public static JSONObject getMyTeamPersonAgeInfo(String id) {
        JSONObject obj = new JSONObject();
        if (StringUtils.isEmpty(id)) {
            id = String.valueOf(RequestContext.get().getCurrUserId());
        }
        Long hrUser = getHRUser(Long.valueOf(id));
        List<Long> ids = getSubordinatePersonnel(hrUser);
        if (ids.size() > 0) {
            //业务档案状态  1 = 生效中
            QFilter businessstatusQF = new QFilter("businessstatus", QCP.equals, "1");
            //数据版本状态  1 = 生效中
            QFilter datastatusQF = new QFilter("datastatus", QCP.equals, "1");

            QFilter qFilter1 = new QFilter("person.id", QCP.in, ids);
            //是否当前版本  Boolean类型  true = 是
            QFilter qFilter2 = new QFilter("iscurrentversion", QCP.equals, true);
            QFilter qFilter3 = qFilter1.and(qFilter2).and(datastatusQF);
            //人员非时序性属性
            DynamicObjectCollection query = QueryServiceHelper.query("hrpi_pernontsprop", "id,person,age", qFilter3.toArray());
            //人数
            obj.put("psnCount", query.size());
            //平均年龄
            int averageAge = 0;
            int age = 0;
            for (DynamicObject object : query) {
                age += object.getInt("age");
            }
            if (query.size() > 0) {
                averageAge = age / query.size();
            }
            //平均年龄
            obj.put("avgAge", averageAge);
            //服务年限
            DynamicObjectCollection query1 = QueryServiceHelper.query("hrpi_perserlen", "person,comsercount", qFilter3.toArray());
            BigDecimal avgJoinGroupAge = new BigDecimal("0");
            BigDecimal count = new BigDecimal("0");
            for (DynamicObject object : query1) {
                count = count.add(object.getBigDecimal("comsercount"));
            }
            if (query1.size() > 0) {
                avgJoinGroupAge = count.divide(new BigDecimal(query1.size()), 1, RoundingMode.HALF_UP);
            }
            obj.put("avgJoinGroupAge", avgJoinGroupAge);
        } else {
            //人数
            obj.put("psnCount", 0);
            //平均年龄
            obj.put("avgAge", 0);
            //平均司龄
            obj.put("avgJoinGroupAge", 0);
        }

        return obj;
    }

    /**
     * 查询实体：tdkw_appauth_businessunit
     * 获取该HR人员的下属人员
     *
     * @param hrUserId HR人员Id
     * @return 下属员工Id集合
     */
    public static List<Long> getSubordinatePersonnel(Long hrUserId) {
        List<Long> ids = new ArrayList<>();
        if (hrUserId == null) {
            return new ArrayList<>();
        }
        ids.add(hrUserId);

        return getSubordinatePersonnel(ids, null);
    }

    /**
     * 查询实体：tdkw_appauth_businessunit
     * 获取该HR人员的下属人员，按岗位过滤
     *
     * @param hrUserId HR人员Id
     * @param postId   岗位Id
     * @return 下属员工Id集合
     */
    public static List<Long> getSubordinatePersonnel(Long hrUserId, Long postId) {
        List<Long> ids = new ArrayList<>();
        if (hrUserId == null) {
            return new ArrayList<>();
        }
        ids.add(hrUserId);

        return getSubordinatePersonnel(ids, postId);
    }

    /**
     * 查询实体：tdkw_appauth_businessunit
     * 获取该HR人员的下属人员
     *
     * @param hrUserId HR人员Id
     * @return 下属员工Id集合
     */
    public static List<Long> getSubordinatePersonnel(List<Long> hrUserId, Long postId) {
        Set<Long> list = new HashSet<>();
        if (hrUserId == null || hrUserId.isEmpty()) {
            return new ArrayList<>();
        }
        // 构建人员与岗位的映射关系
        Map<Long, Set<Long>> map = new HashMap<>();
        QFilter qFilter = new QFilter("tdkw_manager.id", QCP.in, hrUserId);
        QFilter qFilter2 = new QFilter("tdkw_reportcoreltype.number", QCP.equals, "1010_S");
        if (postId != null && postId != 0L) {
            qFilter.and("tdkw_position.id", "=", postId);
        }
        DynamicObject[] collection = BusinessDataServiceHelper.load("tdkw_appauth_businessunit",
                "tdkw_entryentity,tdkw_entryentity.tdkw_subordinate,tdkw_entryentity.tdkw_position1", qFilter.and(qFilter2).toArray());
        List<Long> ids = new ArrayList<>();
        if (collection != null) {
            for (DynamicObject dynamicObject : collection) {
                DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                for (DynamicObject object : entryentity) {
                    Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                    Long post = (Long) object.getDynamicObject("tdkw_position1").getPkValue();
                    ids.add(id);
                    if (map.get(id) == null) {
                        Set<Long> temp = new HashSet<>();
                        temp.add(post);
                        map.put(id, temp);
                    } else {
                        Set<Long> set = map.get(id);
                        set.add(post);
                        map.put(id, set);
                    }
                    list.add(id);
                }
            }
        }
        if (ids.size() < 1) {
            return new ArrayList<>(list);
        }
        do {
            // 防止上下级混乱导致的死循环
            int i = list.size();
            QFilter qFilter1 = new QFilter("tdkw_manager.id", QCP.in, ids);
            DynamicObject[] coll = BusinessDataServiceHelper.load("tdkw_appauth_businessunit",
                    "tdkw_manager,tdkw_position,tdkw_entryentity,tdkw_entryentity.tdkw_subordinate,tdkw_entryentity.tdkw_position1", qFilter1.and(qFilter2).toArray());
            ids.clear();
            if (coll != null) {
                for (DynamicObject dynamicObject : coll) {
                    Long manager = (Long) dynamicObject.getDynamicObject("tdkw_manager").getPkValue();
                    Long position = (Long) dynamicObject.getDynamicObject("tdkw_position").getPkValue();
                    Set<Long> longSet = map.get(manager);
                    if (!longSet.contains(position)) {
                        continue;
                    }
                    DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                    for (DynamicObject object : entryentity) {
                        Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                        Long post = (Long) object.getDynamicObject("tdkw_position1").getPkValue();
                        ids.add(id);
                        if (map.get(id) == null) {
                            Set<Long> temp = new HashSet<>();
                            temp.add(post);
                            map.put(id, temp);
                        } else {
                            Set<Long> set = map.get(id);
                            set.add(post);
                            map.put(id, set);
                        }
                        list.add(id);
                    }
                }
            }
            if (i == list.size()) {
                break;
            }
        } while (ids.size() != 0);

        return new ArrayList<>(list);
    }

    /**
     * 获取该HR人员的下属人员以及其岗位ID
     *
     * @param hrUserId HR人员Id
     * @return 下属员工Id -- 员工岗位ID集合
     */
    public static Map<Long, Set<Long>> getSubordinatePositions(Long hrUserId) {
        List<Long> ids = new ArrayList<>();
        if (hrUserId == null) {
            return new HashMap<>();
        }
        ids.add(hrUserId);

        return getSubordinatePositions(ids, null);
    }

    /**
     * 查询实体：tdkw_appauth_businessunit
     * 获取该HR人员的下属人员以及其岗位ID
     *
     * @param hrUserId HR人员Id
     * @param postId   岗位ID
     * @return 下属员工Id -- 员工岗位ID集合
     */
    public static Map<Long, Set<Long>> getSubordinatePositions(List<Long> hrUserId, Long postId) {
        Map<Long, Set<Long>> map = new HashMap<>();
        Set<Long> hashSet = new HashSet<>();
        if (hrUserId == null || hrUserId.isEmpty()) {
            return map;
        }
        QFilter qFilter = new QFilter("tdkw_manager.id", QCP.in, hrUserId);
        QFilter qFilter2 = new QFilter("tdkw_reportcoreltype.number", QCP.equals, "1010_S");
        if (postId != null && postId != 0L) {
            qFilter.and("tdkw_position.id", "=", postId);
        }
        DynamicObject[] collection = BusinessDataServiceHelper.load("tdkw_appauth_businessunit",
                "tdkw_entryentity,tdkw_entryentity.tdkw_subordinate,tdkw_entryentity.tdkw_position1", qFilter.and(qFilter2).toArray());
        List<Long> ids = new ArrayList<>();
        if (collection != null) {
            for (DynamicObject dynamicObject : collection) {
                DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                for (DynamicObject object : entryentity) {
                    Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                    Long post = (Long) object.getDynamicObject("tdkw_position1").getPkValue();
                    ids.add(id);
                    if (map.get(id) == null) {
                        Set<Long> temp = new HashSet<>();
                        temp.add(post);
                        map.put(id, temp);
                    } else {
                        Set<Long> set = map.get(id);
                        set.add(post);
                        map.put(id, set);
                    }
                    hashSet.add(id);
                }
            }
        }
        if (ids.size() < 1) {
            return map;
        }
        do {
            // 防止上下级混乱导致的死循环
            int i = hashSet.size();
            QFilter qFilter1 = new QFilter("tdkw_manager.id", QCP.in, ids);
            DynamicObject[] coll = BusinessDataServiceHelper.load("tdkw_appauth_businessunit",
                    "tdkw_manager,tdkw_position,tdkw_entryentity,tdkw_entryentity.tdkw_subordinate,tdkw_entryentity.tdkw_position1", qFilter1.and(qFilter2).toArray());
            ids.clear();
            if (coll != null) {
                for (DynamicObject dynamicObject : coll) {
                    Long manager = (Long) dynamicObject.getDynamicObject("tdkw_manager").getPkValue();
                    Long position = (Long) dynamicObject.getDynamicObject("tdkw_position").getPkValue();
                    Set<Long> longSet = map.get(manager);
                    if (!longSet.contains(position)) {
                        continue;
                    }
                    DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                    for (DynamicObject object : entryentity) {
                        Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                        Long post = (Long) object.getDynamicObject("tdkw_position1").getPkValue();
                        ids.add(id);
                        if (map.get(id) == null) {
                            Set<Long> temp = new HashSet<>();
                            temp.add(post);
                            map.put(id, temp);
                        } else {
                            Set<Long> set = map.get(id);
                            set.add(post);
                            map.put(id, set);
                        }
                        hashSet.add(id);
                    }
                }
            }
            if (i == hashSet.size()) {
                break;
            }
        } while (ids.size() != 0);

        return map;
    }

    /**
     * 查询业务云的ID
     *
     * @param number 云编码
     * @return 云ID
     */
    public static String getCloud(String number) {
        return getBaseStr(number, "number", "bos_devportal_bizcloud", "id");
    }

    /**
     * 查询该人员、角色分配的组织范围
     *
     * @param userId     userId
     * @param roleNumber 角色编码
     * @return 组织Id
     */
    public static Set<Long> getRoleOrgs(Long userId, String roleNumber) {
        Set<Long> ids = new HashSet<>();
        if (userId == null || userId == 0L) {
            return ids;
        }
        String roleId = getBaseStr(roleNumber, "number", "perm_role", "id");
        if (roleId == null) {
            return ids;
        }
        QFilter userQF = new QFilter("user.id", QCP.equals, userId);
        QFilter roleQF = new QFilter("role.id", QCP.equals, roleId);
        QFilter dimtypeQF = new QFilter("dimtype", QCP.equals, "dimtype");
        DynamicObjectCollection userrole = QueryServiceHelper.query("perm_userrole", "id,org.id", userQF.and(roleQF).and(dimtypeQF).toArray());
        for (DynamicObject dynamicObject : userrole) {
            ids.add(dynamicObject.getLong("org.id"));
        }
        return ids;
    }

    /**
     * 判断该用户的角色是否有该实体
     *
     * @param userId       苍穹用户ID
     * @param entityNumber 单据标识
     * @return true表示有权限
     */
    public static boolean getUserROleEntity(Long userId, String entityNumber) {
        //分配了该实体的角色Id
        List<String> roleIds = getRoleEntity(entityNumber);
        if (roleIds.isEmpty()) {
            return false;
        }
        //查询用户的HR角色ID
        List<String> userRole = getUserRole(userId);
        if (userRole.isEmpty()) {
            return false;
        }

        //交集不为空表示有权限
        roleIds.retainAll(userRole);
        return !roleIds.isEmpty();
    }

    /**
     * 判断该用户是否分配了该角色
     *
     * @param userId     userId
     * @param roleNumber 角色编码
     * @return true = 已分配
     */
    public static boolean getIsRole(Long userId, String roleNumber) {
        List<String> userRole = getUserRole(userId);
        String id = getBaseStr(roleNumber, "number", "perm_role", "id");
        return userRole.contains(id);
    }

    /**
     * 查询该实体分配给了哪些角色
     *
     * @param entityNumber 单据标识
     * @return 角色Id
     */
    public static List<String> getRoleEntity(String entityNumber) {
        List<String> ids = new ArrayList<>();
        String baseLong = getBaseStr(entityNumber, "number", "bos_objecttype", "id");
        QFilter qFilter = new QFilter("roleperm.entity.id", QCP.equals, baseLong);
        boolean exists = QueryServiceHelper.exists("perm_roleperm", qFilter.toArray());
        if (exists) {
            DynamicObject[] coll = BusinessDataServiceHelper.load("perm_roleperm", "id,roleid", qFilter.toArray());
            ids = Arrays.stream(coll).map((it) -> it.getDynamicObject("roleid").getString("id")).collect(Collectors.toList());
        }
        return ids;
    }

    /**
     * 查询该实体分配给了哪些角色，同时也可以指定是否有这些权限项其中的某个权限
     * 比如查询哪些角色有实体bos_user的查询或新增权限，传值 getRoleEntity(("bos_user"), ("QXX0001", "QXX0002"))
     *
     * @param entityNumber 单据标识
     * @param perm         权限项编码
     * @return map{实体编码，角色Id}
     */
    public static Map<String, List<String>> getRoleEntity(List<String> entityNumber, List<String> perm) {
        Map<String, List<String>> map = new HashMap<>();

        QFilter permQF = null;
        //权限项
        if (perm != null && !perm.isEmpty()) {
            List<String> permIds = getBaseStr(perm, "number", "perm_permitem", "id", "in");
            for (String permId : permIds) {
                if (permQF == null) {
                    permQF = new QFilter("roleperm.permitem.id", "=", permIds);
                } else {
                    permQF.or(new QFilter("roleperm.permitem.id", "=", permIds));
                }
            }
        }
        //通过 业务对象 查询 角色功能权限
        QFilter typeQF = new QFilter("number", "in", entityNumber);
        DynamicObjectCollection bosObjectType = QueryServiceHelper.query("bos_objecttype", "id,number", typeQF.toArray());
        for (DynamicObject object : bosObjectType) {
            QFilter qFilter = new QFilter("roleperm.entity.id", QCP.equals, object.get("id"));
            if (permQF != null) {
                qFilter.and(permQF);
            }
            boolean exists = QueryServiceHelper.exists("perm_roleperm", qFilter.toArray());
            if (exists) {
                DynamicObject[] coll = BusinessDataServiceHelper.load("perm_roleperm", "id,roleid", qFilter.toArray());
                List<String> ids = Arrays.stream(coll).map((it) -> it.getDynamicObject("roleid").getString("id")).collect(Collectors.toList());
                map.put(object.getString("number"), ids);
            }
        }

        return map;
    }

    /**
     * 查询该实体分配给了哪些角色
     *
     * @param entityNumber 单据标识
     * @return 角色编码
     */
    public static List<String> getRoleEntityNumber(String entityNumber) {
        List<String> numbers = new ArrayList<>();
        String baseLong = getBaseStr(entityNumber, "number", "bos_objecttype", "id");
        QFilter qFilter = new QFilter("roleperm.entity.id", QCP.equals, baseLong);
        boolean exists = QueryServiceHelper.exists("perm_roleperm", qFilter.toArray());
        if (exists) {
            DynamicObject[] coll = BusinessDataServiceHelper.load("perm_roleperm", "id,roleid", qFilter.toArray());
            numbers = Arrays.stream(coll).map((it) -> it.getDynamicObject("roleid").getString("number")).collect(Collectors.toList());
        }
        return numbers;
    }

    /**
     * 查询用户已分配的所有HR角色ID
     *
     * @param userId user用户ID
     * @return HR角色id
     */
    public static List<String> getUserRole(Long userId) {
        return getBaseStr(userId, "user.id", "hrcs_userrolerelat", "role.id", QCP.equals);
    }

    /**
     * 批量查询用户已分配的所有HR角色ID
     *
     * @param userIds user用户ID
     * @return 用户ID--HR角色id
     */
    public static Map<Long, List<String>> getUserRoles(List<Long> userIds) {
        Map<Long, List<String>> map = new HashMap<>();
        QFilter qFilter = new QFilter("user.id", QCP.in, userIds);
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        //  构建用户与角色的映射
        for (Long userId : userIds) {
            map.put(userId, new ArrayList<>());
        }
        DynamicObjectCollection query = QueryServiceHelper.query("hrcs_userrolerelat", "role.id,user.id,user.number", qFilter.toArray(), "user.number");
        for (DynamicObject object : query) {
            long userId = object.getLong("user.id");
            String roleId = object.getString("role.id");
            if (StringUtils.isNotEmpty(roleId)) {
                List<String> list = map.get(userId);
                list.add(roleId);
                map.put(userId, list);
            }
        }
        return map;
    }

    /**
     * 通过HR人员Id查询对应的苍穹人员Id
     *
     * @param id 苍穹人员Id
     * @return HR人员信息Id
     */
    public static Long getCQUser(Long id) {
        return getBaseLong(id, "person", "hrpi_personuserrel", "user");
    }

    public static List<Long> getCQUser(List<Long> ids) {
        return getBaseLong(ids, "person", "hrpi_personuserrel", "user", QCP.in);
    }

    /**
     * 通过苍穹人员Id查询对应的HR人员Id
     *
     * @param id 苍穹人员Id
     * @return HR人员信息Id
     */
    public static Long getHRUser(Long id) {
        return getBaseLong(id, "user", "hrpi_personuserrel", "person");
    }

    public static List<Long> getHRUser(List<Long> ids) {
        return getBaseLong(ids, "user", "hrpi_personuserrel", "person", QCP.in);
    }


    /**
     * 批量查询用户已分配的角色的ID集合
     * 角色的功能权限包含该单据实体, 返回的ID是实体：通用角色(perm_role)的fid
     *
     * @param userIds      用户ID
     * @param entityNumber 单据标识
     * @return 角色id list
     */
    public static Map<Long, List<String>> getUserRoleInEntity(List<Long> userIds, String entityNumber) {
        Map<Long, List<String>> map = new HashMap<>();
        if (userIds.isEmpty() || StringUtils.isEmpty(entityNumber)) {
            return map;
        }
        //应用Id
        String appId = getAppId(entityNumber);
        //权限项Id, number = QXX0001
        String permItemId = PermissionStatus.View;
        for (Long userId : userIds) {
            List<UserRoleInfo> userRole = UserRoleServiceHelper.queryUserRoleSet(userId, appId, entityNumber, permItemId);
            List<String> list = new ArrayList<>();
            for (UserRoleInfo userRoleInfo : userRole) {
                String roleId = userRoleInfo.getRoleId();
                list.add(roleId);
            }
            map.put(userId, list);
        }
        return map;
    }

    /**
     * 查询用户已分配的角色的ID集合
     * 角色的功能权限包含该单据实体
     * 先查询缓存
     *
     * @param userId       用户ID
     * @param entityNumber 单据标识
     * @return 角色id list
     */
    public static List<String> getUserRoleIds(Long userId, String entityNumber) {
        List<String> list = new ArrayList<>();
        List<UserRoleInfo> userRole = getUserRole(userId, entityNumber);
        for (UserRoleInfo userRoleInfo : userRole) {
            String roleId = userRoleInfo.getRoleId();
            list.add(roleId);
        }
        return list;
    }

    /**
     * 查询用户已分配的角色info
     * 角色的功能权限包含该单据实体和查询权限项
     * 先查询缓存
     *
     * @param userId       用户ID
     * @param entityNumber 单据标识
     * @return 角色Info
     */
    public static List<UserRoleInfo> getUserRole(Long userId, String entityNumber) {
        List<UserRoleInfo> list = new ArrayList<>();
        if (userId == null || StringUtils.isEmpty(entityNumber)) {
            return list;
        }
        String cacheType = HRUserRoleCacheUtils.getTypeUserRoleEntity();
        String cacheKey = userId + "_" + entityNumber + "_getUserRole";
        String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, cacheKey);
        if (StringUtils.isEmpty(cacheVal)) {
            //应用Id
            String appId = getAppId(entityNumber);
            //权限项Id, number = QXX0001
            String permItemId = PermissionStatus.View;
            //查询用户已分配的角色
            List<UserRoleInfo> userRoleInfoList = UserRoleServiceHelper.queryUserRoleSet(userId, appId, entityNumber, permItemId);
            HRUserRoleCacheUtils.putCache(cacheType, cacheKey, SerializationUtils.toJsonString(userRoleInfoList));
            return userRoleInfoList;
        } else {
            return (List<UserRoleInfo>) SerializationUtils.fromJsonStringToList(cacheVal, UserRoleInfo.class);
        }
    }

    /**
     * 批量给角色分配角色
     *
     * @param userIds    要分配的用户
     * @param roleNumber 要分配的角色的编码
     */
    public static void batchRole(List<Long> userIds, String roleNumber) {
        logger.info("HRRoleAndPersonUtils.batchRole:批量分配角色 roleNumber=" + roleNumber + ", userIds=" + userIds);
        Map<Long, UserBucaPermDataParam> userBucaPermDataParam = getUserBucaPermDataParam();
        if (!userIds.isEmpty()) {
            for (Long userId : userIds) {
                HrApiResponse apiResult = addUserRole(userId, roleNumber, false, userBucaPermDataParam);
                String errorCode = apiResult.getCode();//失败，"500"
                if (StringUtils.equals(EnumResponseCode.FAIL.getCode(), errorCode)) {
                    logger.info("HRRoleAndPersonUtils.role: 角色分配失败, userId=" + userId + ", role=" + roleNumber + ", 失败信息：" + apiResult.getErrorMessage());
                }
            }
        }
    }

    /**
     * 给角色分配用户
     * 组织为最高级组织(id=100000)，不包含下级
     *
     * @param userId     要分配的用户
     * @param roleNumber 要分配的角色的编码
     * @return 返回结果
     */
    public static HrApiResponse role(Long userId, String roleNumber) {
        logger.info("HRRoleAndPersonUtils.role: 分配角色 userId=" + userId + ", roleNumber=" + roleNumber);
        HrApiResponse apiResult = addUserRole(userId, roleNumber, false, getUserBucaPermDataParam());
        String errorCode = apiResult.getCode();//失败，"fail"
        if (StringUtils.equals(EnumResponseCode.FAIL.getCode(), errorCode)) {
            logger.info("HRRoleAndPersonUtils.role: 角色分配失败, userId=" + userId + ", role=" + roleNumber + ", 失败信息：" + apiResult.getErrorMessage());
        }
        return apiResult;
    }

    private static Map<Long, UserBucaPermDataParam> getUserBucaPermDataParam() {
        // TODO:2023-08-08  另加类似方法，需要根据角色的数据范围进行配置，不需要默认

        //标识 hrcs_dimension 维度Id = 1109486343509107712
        UserBucaPermDataParam param = new UserBucaPermDataParam();

        //业务管理视图Id, 取值实体bos_org_viewschema_biz，编码number=11 的Id
        Long org = 11L;
        param.setBucaId(org);

        Map<Long, Boolean> orgInfoMap = new HashMap<>();
        //组织Id, 是否包含下级
        orgInfoMap.put(100000L, false);
        param.setOrgInfos(orgInfoMap);

        //查询系统预置的维度, 编码 = adminorgteam
        DynamicObject dimensionObj = QueryServiceHelper.queryOne("hrcs_dimension", "id,teamtype.id", new QFilter("number", "=", "adminorgteam").toArray());
        //维度值Id, 取值实体 hrcs_dimension, 编码 number=adminorgteam 的Id
        String dimenSionId = dimensionObj.getString("id");

        DimValueParam dimValueParam = new DimValueParam();
        //是否不限
        dimValueParam.setAll(false);
        //维度值Id
        dimValueParam.setDimVal(dimenSionId);
        //是否自身包含下级
        dimValueParam.setContainsSub(false);
        //是否行政包含下级
        dimValueParam.setAdminContainsSub(false);
        //组织团队类型Id, 取值取值实体hrcs_dimension, 编码 number=adminorgteam 的数据中的基础资料字段 teamtype 的Id
        dimValueParam.setOtClassify(dimensionObj.getLong("teamtype.id"));

        List<DimValueParam> dimValueParamList = new ArrayList<>();
        dimValueParamList.add(dimValueParam);
        Map<Long, List<DimValueParam>> dimValueMap = new HashMap<>();
        //维度Id ： 维度值组
        dimValueMap.put(Long.parseLong(dimenSionId), dimValueParamList);

        DimGroupParam dimGroupParam = new DimGroupParam();
        dimGroupParam.setDimValuesMap(dimValueMap);

        List<DimGroupParam> list = new ArrayList<>();
        list.add(dimGroupParam);
        param.setUserDimGroupData(list);

        Map<Long, UserBucaPermDataParam> map = new HashMap<>();
        map.put(org, param);

        return map;
    }

    /**
     * 给角色分配用户
     *
     * @param userId       人员Id
     * @param roleNumber   角色编码
     * @param isCustomData 用户是否自定义范围
     * @param bucaPermData 业务管理视图id：职能数据范围
     * @return apiResult.getErrorCode()=="success" 表示成功
     */
    public static HrApiResponse addUserRole(Long userId, String roleNumber, boolean isCustomData, Map<Long, UserBucaPermDataParam> bucaPermData) {
        LocalDate date = LocalDate.now();
        LocalDate date1 = LocalDate.of(2999, 12, 31);
        Instant instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant instant1 = date1.atStartOfDay(ZoneId.systemDefault()).toInstant();
        //生效时间
        Date validStart = Date.from(instant);
        //失效时间
        Date validEnd = Date.from(instant1);
        //云，应用，服务名称，方法名，。。。。。。。
        return DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSPermManageService", "userAssignRole",
                userId, roleNumber, validStart, validEnd, isCustomData, bucaPermData, getAdministratorId());
    }


    /**
     * 删除角色中已分配的用户
     *
     * @param userId     人员Id
     * @param roleNumber 角色编码
     * @return 返回结果
     */
    public static HrApiResponse deleteUserRole(long userId, String roleNumber) {
        logger.info("HRRoleAndPersonUtils.deleteUserRole:userId=" + userId + ", roleNumber=" + roleNumber);
        HrApiResponse apiResult = DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSPermManageService", "deleteUserRole", userId, roleNumber);
        logger.info("HRRoleAndPersonUtils.deleteUserRole:" + apiResult.getErrorMessage());
        return apiResult;
    }

    /**
     * 查询HR角色:员工 分配给用户的实体的HR行政业务组织
     * 查询 组织职能类型 是HR组织的，其它组织职能的可能会返回空
     * AuthorizedOrgResult.isHasAllOrgPerm() == true 时表示全集团
     *
     * @param userId       苍穹的人员Id
     * @param entityNumber 单据编码
     * @param propKey      字段标识
     * @param permItemId   权限项Id
     * @return AuthorizedOrgResult
     */
    public static AuthorizedOrgResult getAuthorizedAdminOrgSet(Long userId, String entityNumber, String propKey, String permItemId) {
        logger.info("HRRoleAndPersonUtils.getAuthorizedAdminOrgSet:userId=" + userId + ", entityNumber=" + entityNumber);
        AuthorizedOrgResult authorizedOrgResult = new AuthorizedOrgResult();
        authorizedOrgResult.setHasAllOrgPerm(false);
        authorizedOrgResult.setHasPermOrgs(Lists.newArrayListWithCapacity(0));
        if (userId == null || userId == 0L) {
            return authorizedOrgResult;
        }
        if (StringUtils.isEmpty(entityNumber)) {
            return authorizedOrgResult;
        }
        Map<String, Object> map2 = new HashMap<>();
        List<Long> b = new ArrayList<>();
        b.add(1010L);
        map2.put("hr_dataperm_structprojectid", b);
        String appId = getAppId(entityNumber);//应用Id
        return DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSBizDataPermissionService", "getUserAdminOrgs", userId, appId, entityNumber, permItemId, propKey, map2);
    }

    /**
     * 查询HR角色:员工 分配给用户的实体的HR行政业务组织
     * 查询 组织职能类型 是HR组织的，其它组织职能的可能会返回空
     * AuthorizedOrgResult.isHasAllOrgPerm() == true 时表示全集团
     *
     * @param userId       苍穹的人员Id
     * @param entityNumber 单据编码
     * @param propKey      字段标识
     * @return AuthorizedOrgResult
     */
    public static AuthorizedOrgResult getAuthorizedAdminOrgSet(Long userId, String entityNumber, String propKey) {
        logger.info("HRRoleAndPersonUtils.getAuthorizedAdminOrgSet:userId=" + userId + ", entityNumber=" + entityNumber);
        AuthorizedOrgResult authorizedOrgResult = new AuthorizedOrgResult();
        authorizedOrgResult.setHasAllOrgPerm(false);
        authorizedOrgResult.setHasPermOrgs(Lists.newArrayListWithCapacity(0));
        if (userId == null || userId == 0L) {
            return authorizedOrgResult;
        }
        if (StringUtils.isEmpty(entityNumber)) {
            return authorizedOrgResult;
        }
        String appId = getAppId(entityNumber);//应用Id
        //权限项Id, number = QXX0001
        String permItemId = PermissionStatus.View;
        Map<String, Object> map2 = new HashMap<>();
        List<Long> b = new ArrayList<>();
        b.add(1010L);
        map2.put("hr_dataperm_structprojectid", b);
        return DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSBizDataPermissionService", "getUserAdminOrgs", userId, appId, entityNumber, permItemId, propKey, map2);
    }

    /**
     * 查询HR角色:员工 分配给用户的实体的HR行政业务组织
     * 查询 组织职能类型 是HR组织的，其它组织职能的可能会返回空
     * AuthorizedOrgResult.isHasAllOrgPerm() == true 时表示全集团
     *
     * @param userId       苍穹的人员Id
     * @param entityNumber 单据编码
     * @return AuthorizedOrgResult
     */
    public static AuthorizedOrgResult getAuthorizedAdminOrgSet(Long userId, String entityNumber) {
        logger.info("HRRoleAndPersonUtils.getAuthorizedAdminOrgSet:userId=" + userId + ", entityNumber=" + entityNumber);
        AuthorizedOrgResult authorizedOrgResult = new AuthorizedOrgResult();
        authorizedOrgResult.setHasAllOrgPerm(false);
        authorizedOrgResult.setHasPermOrgs(Lists.newArrayListWithCapacity(0));
        if (userId == null || userId == 0L) {
            return authorizedOrgResult;
        }
        if (StringUtils.isEmpty(entityNumber)) {
            return authorizedOrgResult;
        }
        Map<String, Object> map2 = new HashMap<>();
        List<Long> b = new ArrayList<>();
        b.add(1010L);
        map2.put("hr_dataperm_structprojectid", b);
        String appId = getAppId(entityNumber);//应用Id
        //权限项Id, number = QXX0001
        String permItemId = PermissionStatus.View;
        String propKey = "tdkw_hrorg";//字段标识
        return DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSBizDataPermissionService", "getUserAdminOrgs", userId, appId, entityNumber, permItemId, propKey, map2);
    }

    /**
     * 获取角色的数据范围中的高级配置的已配置查询权限的人员编码
     *
     * @param userId       苍穹用户Id
     * @param entityNumber 单据实体标识
     * @return 人员编码
     */
    public static List<String> getDataRuleQuery(Long userId, String entityNumber) {
        return getDataRule(userId, entityNumber, "QXX0001");
    }

    /**
     * 取角色的数据范围中的高级配置的已配置权限的人员编码
     *
     * @param userId       苍穹用户Id
     * @param entityNumber 单据实体标识
     * @param permItem     权限项编码
     * @return 人员编码
     */
    public static List<String> getDataRule(Long userId, String entityNumber, String permItem) {
        QFilter dataRule = getDataQF(userId, entityNumber, permItem);
        if (dataRule != null) {
            String property = dataRule.getProperty();
            if (StringUtils.equals("creator.number", property)) {
                return (List<String>) dataRule.getValue();
            }
        }
        return new ArrayList<>();
    }

    /**
     * 获取行政组织范围和数据规则方案的Qfilter
     *
     * @param userId       苍穹用户Id
     * @param entityNumber 单据实体标识
     * @param permItem     权限项编码
     * @return QFilter
     */
    public static QFilter getDataQF(Long userId, String entityNumber, String permItem) {
        if (userId == null || userId <= 0L) {
            return null;
        }
        if (StringUtils.isEmpty(entityNumber)) {
            return null;
        }
        if (StringUtils.isEmpty(permItem)) {
            return null;
        }
        String appId = getAppId(entityNumber);//应用Id
        String permItemId = getPermItemId(permItem);//权限项Id

        QFilter qFilter = DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSDataPermissionService", "getDataRule",
                userId, appId, entityNumber, permItemId, null);
        logger.info("HRRoleAndPersonUtils.getDataQF:userId=" + userId + ", entityNumber=" + entityNumber + ", permItem=" + permItem + ", QFilter=" + qFilter);
        return qFilter;
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
     * 批量查询应用Id
     *
     * @param list 应用编码
     * @return 应用Id
     */
    public static Map<String, String> getAppIds(List<String> list) {
        Map<String, String> map = new HashMap<>();
        if (list.isEmpty()) {
            return map;
        }
        QFilter qFilter = new QFilter("number", QCP.in, list);
        //查询业务应用实体的Id
        DynamicObjectCollection collection = QueryServiceHelper.query("bos_devportal_bizapp", "id,number", qFilter.toArray());
        for (DynamicObject dynamicObject : collection) {
            map.put(dynamicObject.getString("number"), dynamicObject.getString("id"));
        }
        return map;
    }

    /**
     * bos_org 业务单元
     * 查找该部门或公司的所有下级组织
     *
     * @param orgId 组织Id
     */
    public static List<Long> getOrgIds(String orgId) {
        List<Long> list = new ArrayList<>();
        if (StringUtils.isEmpty(orgId)) {
            return list;
        }
        //parent = 上级行政组织
        QFilter qFilter = new QFilter("parent.id", QCP.equals, Long.valueOf(orgId));
        //禁用状态 可用
        QFilter enableQF = new QFilter("enable", QCP.equals, "1");
        DynamicObjectCollection collection = QueryServiceHelper.query("bos_org_structure", "org,parent", new QFilter[]{qFilter, enableQF});
        List<Long> ids = new ArrayList<>();
        list.add(Long.valueOf(orgId));
        if (collection.size() > 0) {
            for (DynamicObject dynamicObject : collection) {
                Long id = dynamicObject.getLong("org");
                ids.add(id);
                list.add(id);
            }
        }
        if (ids.size() < 1) {
            return list;
        }
        do {
            QFilter qFilter1 = new QFilter("parent.id", QCP.in, ids);
            DynamicObjectCollection objectCollection = QueryServiceHelper.query("bos_org_structure", "org", qFilter1.and(enableQF).toArray());
            ids.clear();
            if (objectCollection.size() > 0) {
                for (DynamicObject dynamicObject : objectCollection) {
                    Long id = dynamicObject.getLong("org");
                    ids.add(id);
                    list.add(id);
                }
            }
        } while (ids.size() != 0);

        return list;
    }

    /**
     * HR行政组织
     * 查找该部门或公司的所有下级组织，有多少组织层级就会查询多少次，不会频繁访问数据库
     *
     * @param orgId 组织Id
     * @return 下级组织集合
     */
    public static List<Long> getHROrgIds(String orgId) {
        List<Long> list = new ArrayList<>();
        if (StringUtils.isEmpty(orgId)) {
            return list;
        }
        //parent = 上级行政组织
        QFilter qFilter = new QFilter("parent.id", QCP.equals, Long.valueOf(orgId));
        QFilter iscurrentversionQF = new QFilter("iscurrentversion", QCP.equals, "1");
        QFilter datastatusQF = new QFilter("datastatus", QCP.equals, "1");
        DynamicObjectCollection collection = QueryServiceHelper.query("haos_adminorghr", "id,parent", qFilter.and(iscurrentversionQF).and(datastatusQF).toArray());
        List<Long> ids = new ArrayList<>();
        list.add(Long.valueOf(orgId));
        if (collection.size() > 0) {
            for (DynamicObject dynamicObject : collection) {
                Long id = dynamicObject.getLong("id");
                ids.add(id);
                list.add(id);
            }
        }
        if (ids.size() < 1) {
            return list;
        }
        do {
            QFilter qFilter1 = new QFilter("parent.id", QCP.in, ids);
            DynamicObjectCollection objectCollection = QueryServiceHelper.query("haos_adminorghr", "id", qFilter1.and(iscurrentversionQF).and(datastatusQF).toArray());
            ids.clear();
            if (objectCollection.size() > 0) {
                for (DynamicObject dynamicObject : objectCollection) {
                    Long id = dynamicObject.getLong("id");
                    ids.add(id);
                    list.add(id);
                }
            }
        } while (ids.size() != 0);

        return list;
    }

    /**
     * HR行政组织
     * 批量查找这些部门或公司的所有下级组织，有多少组织层级就会查询多少次，不会频繁访问数据库
     *
     * @param orgIds 组织Ids
     * @return 下级组织集合
     */
    public static List<Long> getHROrgIds(List<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) {
            return new ArrayList<>();
        }
        //parent = 上级行政组织
        QFilter qFilter = new QFilter("parent.id", QCP.in, orgIds);
        QFilter enableQF = new QFilter("enable", QCP.equals, "1");
        QFilter iscurrentversionQF = new QFilter("iscurrentversion", QCP.equals, "1");
        QFilter datastatusQF = new QFilter("datastatus", QCP.equals, "1");
        DynamicObjectCollection collection = QueryServiceHelper.query("haos_adminorghr", "id,parent", qFilter.and(iscurrentversionQF).and(enableQF).and(datastatusQF).toArray());
        List<Long> ids = new ArrayList<>();
        Set<Long> list = new HashSet<>(orgIds);
        if (collection.size() > 0) {
            for (DynamicObject dynamicObject : collection) {
                Long id = dynamicObject.getLong("id");
                ids.add(id);
                list.add(id);
            }
        }
        if (ids.size() < 1) {
            return new ArrayList<>(list);
        }
        do {
            int i = list.size();
            QFilter qFilter1 = new QFilter("parent.id", QCP.in, ids);
            DynamicObjectCollection objectCollection = QueryServiceHelper.query("haos_adminorghr", "id", qFilter1.and(iscurrentversionQF).and(enableQF).and(datastatusQF).toArray());
            ids.clear();
            if (objectCollection != null) {
                if (objectCollection.size() > 0) {
                    for (DynamicObject dynamicObject : objectCollection) {
                        Long id = dynamicObject.getLong("id");
                        ids.add(id);
                        list.add(id);
                    }
                }
            }
            if (i == list.size()) {
                break;
            }
        } while (ids.size() != 0);

        return new ArrayList<>(list);
    }

    /**
     * HR行政组织，查询上级组织，返回的会包含当前传进来的组织ID
     * 查找该组织的所有上级组织，有多少组织层级就会查询多少次，不会频繁访问数据库
     * 拒绝递归,从我做起 -v-
     *
     * @param orgIds HR组织ID
     * @return 上级组织ID集合
     */
    public static List<Long> getParentHROrg(List<Long> orgIds) {
        // 存储符合的组织ID
        Set<Long> list = new HashSet<>();
        if (orgIds == null || orgIds.isEmpty()) {
            return new ArrayList<>();
        }
        QFilter qFilter = new QFilter("id", QCP.in, orgIds);
        QFilter iscurrentversionQF = new QFilter("iscurrentversion", QCP.equals, "1");
        DynamicObjectCollection collection = QueryServiceHelper.query("haos_adminorghr", "id,parent.id", qFilter.and(iscurrentversionQF).toArray());
        //  存储当前组织的上级组织ID
        Set<Long> ids = new HashSet<>();
        for (DynamicObject object : collection) {
            long parent = object.getLong("parent.id");
            if (parent == 0L) {
                continue;
            }
            ids.add(parent);
            list.add(parent);
        }
        // 如果当前组织没有上级组织则说明是根组织, 可以直接返回
        if (ids.size() < 1) {
            return orgIds;
        }
        do {
            int i = list.size();
            QFilter qFilter1 = new QFilter("id", QCP.in, ids);
            DynamicObjectCollection objectCollection = QueryServiceHelper.query("haos_adminorghr", "parent.id", qFilter1.and(iscurrentversionQF).toArray());
            ids.clear();
            if (objectCollection.size() > 0) {
                for (DynamicObject dynamicObject : objectCollection) {
                    Long id = dynamicObject.getLong("parent.id");
                    if (id == 0L) {
                        continue;
                    }
                    ids.add(id);
                    list.add(id);
                }
            }
            if (i == list.size()) {
                break;
            }
        } while (ids.size() != 0);
        list.addAll(orgIds);

        return new ArrayList<>(list);
    }

    /**
     * 查询用户有该单据的行政组织ID
     *
     * @param userId       用户ID
     * @param entityNumber 单据编码
     * @return 组织ID
     */
    @Deprecated
    public static Set<Long> getAuthorizedAdminOrgID(Long userId, String entityNumber) {
        //角色的数据范围中的行政类组织团队
        Set<Long> orgIDSet = new HashSet<>();

        List<String> roleIds = getRoleEntity(entityNumber);
        if (roleIds.isEmpty()) {
            return orgIDSet;
        }

        //查询用户的HR角色ID
        List<String> userRole = getUserRole(userId);
        if (userRole.isEmpty()) {
            return orgIDSet;
        }

        //交集不为空表示有权限
        roleIds.retainAll(userRole);
        if (roleIds.isEmpty()) {
            return orgIDSet;
        }

        QFilter qFilter1 = new QFilter("id", QCP.in, roleIds);
        DynamicObjectCollection query = QueryServiceHelper.query("perm_role", "id,number", qFilter1.toArray());
        for (DynamicObject dynamicObject : query) {
            Set<String> authorizedAdminOrgSetAll = HRRoleAndPersonUtils.getAuthorizedAdminOrgSetAll(userId, dynamicObject.getString("number"));

            for (String orgId : authorizedAdminOrgSetAll) {
                orgIDSet.add(Long.valueOf(orgId));
            }
        }

        return orgIDSet;
    }

    /**
     * 查询权限项的Id
     *
     * @param permNumber 权限项编码
     * @return 值
     */
    private static String getPermItemId(String permNumber) {
        return getBaseStr(permNumber, "number", "perm_permitem", "id");
    }

    /**
     * 查询管理员用户的userId
     *
     * @return 管理员userId
     */
    private static Long getAdministratorId() {
        return getBaseLong("administrator", "name", "bos_user", "id");
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

    /**
     * 查询基础资料 String类型的字段
     *
     * @param param      条件参数
     * @param property   条件字段
     * @param entityName 基础资料标识
     * @param select     查询字段
     * @param qcp        QCP
     * @return 返回 List<String>
     */
    public static List<String> getBaseStr(Object param, String property, String entityName, String select, String qcp) {
        List<String> list = new ArrayList<>();
        Object base = getBase(param, property, entityName, select, true, qcp);
        if (base instanceof DynamicObjectCollection) {
            DynamicObjectCollection collection = (DynamicObjectCollection) base;
            for (DynamicObject dynamicObject : collection) {
                list.add(dynamicObject.getString(select));
            }
        }
        return list;
    }

    /**
     * 查询基础资料 Long类型的字段
     *
     * @param param      条件参数
     * @param property   条件字段
     * @param entityName 基础资料标识
     * @param select     查询字段
     * @return 返回 Long
     */
    public static Long getBaseLong(Object param, String property, String entityName, String select) {
        Object base = getBase(param, property, entityName, select, false, QCP.equals);
        if (base instanceof DynamicObjectCollection) {
            DynamicObjectCollection collection = (DynamicObjectCollection) base;
            return collection.size() > 0 ? collection.get(0).getLong(select) : 0L;
        } else {
            return (Long) base;
        }
    }

    /**
     * 查询基础资料 Long类型的字段
     *
     * @param param      条件参数
     * @param property   条件字段
     * @param entityName 基础资料标识
     * @param select     查询字段
     * @param qcp        QCP
     * @return 返回 List<Long>
     */
    public static List<Long> getBaseLong(Object param, String property, String entityName, String select, String qcp) {
        List<Long> list = new ArrayList<>();
        Object base = getBase(param, property, entityName, select, false, qcp);
        if (base instanceof DynamicObjectCollection) {
            DynamicObjectCollection collection = (DynamicObjectCollection) base;
            for (DynamicObject dynamicObject : collection) {
                list.add(dynamicObject.getLong(select));
            }
        }
        return list;
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

    private static Set<String> convertSetString(Set<?> set) {
        Set<String> result = new HashSet<>();
        if (set == null || set.isEmpty()) {
            return result;
        }
        for (Object obj : set) {
            if (!Objects.isNull(obj)) {
                result.add(String.valueOf(obj));
            }
        }
        return result;
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

    public static String convertToLong(List<Long> ids) {
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

    private static String convertToString(List<String> ids) {
        StringBuilder sd = new StringBuilder();
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            sd.append("'").append(id).append("'");
            if (i < ids.size() - 1) {
                sd.append(",");
            }
        }
        return sd.toString();
    }

    public static String extractCharactersBeforeNthExclamation(String str, int n) {
        int index = 0;
        int count = 0;
        while (count < n && index != -1) {
            index = str.indexOf("!", index + 1);
            count++;
        }

        if (index != -1) {
            return str.substring(0, index);
        }

        return null;
    }

    public static void extractCharactersBeforeLastExclamation(String str, Set<String> set) {
        int index = str.lastIndexOf("!");
        if (index != -1) {
            String subString = str.substring(0, index);
            set.add(subString);
            extractCharactersBeforeLastExclamation(subString, set);
        }
    }

    public static String extractCharactersBeforeLastExclamation(String str) {
        int index = str.lastIndexOf("!");
        if (index != -1) {
            return str.substring(0, index);
        }

        return str;
    }

    /**
     * 获取角色下所有HR人员
     *
     * @return
     */
    public static List<Long> getHrPersonsByRole(List<String> roleNumbers) {
        logger.info("查询的角色编码为: " + roleNumbers);
        List<Long> personIds = new ArrayList<>();
        QFilter roleFilter = new QFilter("role.number", QCP.in, roleNumbers);
        DynamicObject[] roleAndUserIds = BusinessDataServiceHelper.load("perm_userrole", "user.id", new QFilter[]{roleFilter});
        if (null == roleAndUserIds || roleAndUserIds.length == 0) {
            logger.info("该角色:" + roleNumbers + "下没有人员");
            return personIds;
        }
        List<Long> userIds = Arrays.stream(roleAndUserIds).map(i -> i.getLong("user.id")).collect(Collectors.toList());
        logger.info("该角色:" + roleNumbers + "下平台人员id为:" + userIds);
        personIds = HRRoleAndPersonUtils.getHRUser(userIds);
        logger.info("该角色:" + roleNumbers + "下中台人员id为:" + personIds);
        return personIds;
    }

    /**
     * 获取角色下所有HR人员
     *
     * @return
     */
    public static List<Long> getHrPersonsByRole(String roleNumber, Long orgId) {
        List<Long> personIds = new ArrayList<>();
        logger.info("查询的角色编码为: " + roleNumber);
        QFilter roleFilter = new QFilter("role.number", QCP.equals, roleNumber);
        DynamicObject[] roleAndUserIds = BusinessDataServiceHelper.load("perm_userrole", "user.id", new QFilter[]{roleFilter});
        if (null == roleAndUserIds || roleAndUserIds.length == 0) {
            logger.info("该角色:" + roleNumber + "下没有人员");
            return personIds;
        }
        List<Long> userIds = Arrays.stream(roleAndUserIds).map(i -> i.getLong("user.id")).collect(Collectors.toList());
        logger.info("该角色:" + roleNumber + "下平台人员id为:" + userIds);
        personIds = HRRoleAndPersonUtils.getHRUser(userIds);
        logger.info("该角色:" + roleNumber + "下中台人员id为:" + personIds);
        if (orgId != 100000L) {
            List<Long> orgIds = HRRoleAndPersonUtils.getOrgIds(String.valueOf(orgId));
            QFilter qFilter = new QFilter("adminorg.id", QCP.in, orgIds);
            qFilter.and("iscurrentversion", QCP.equals, "1");
            qFilter.and("datastatus", QCP.equals, "1");
            qFilter.and("isprimary", QCP.equals, "1");
            qFilter.and("tdkw_changereason.number", QCP.not_equals, "XY00017");

            DynamicObject[] person = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person.id", new QFilter[]{qFilter});
            if (null == person || person.length == 0) {
                logger.info("任职经历为空");
                return new ArrayList<>();
            }
            List<Long> orgPersonIds = Arrays.stream(person).map(i -> i.getLong("person.id")).collect(Collectors.toList());
            personIds.retainAll(orgPersonIds);
        }
        logger.info("最终返回HR人员id：" + personIds);
        return personIds;

    }

}
