package tdkw.hrmp.hrobs.common.app.hrmp;

import com.google.common.collect.Lists;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeCacheHAPolicy;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.cache.CacheKeyUtil;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.datamanager.DataEntityCacheManager;
import kd.bos.servicehelper.permission.PermissionServiceHelper;

import java.util.Calendar;
import java.util.List;

/**
 * 工具类 HRRoleAndPersonUtils 使用
 * @author xxx
 */
public class HRUserRoleCacheUtils {

    private static final Log LOGGER = LogFactory.getLog(HRUserRoleCacheUtils.class);

    private static final DistributeSessionlessCache CACHE = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("HR_USERROLE", new DistributeCacheHAPolicy(true, true));

    private static final String DATA_APP = "CUSTOM_HR_ROLE_BD_DATA_APP";
    private static final String ORG_SET_ALL = "CUSTOM_HR_ROLE_BD_ORG_SET_ALL";
    private static final String USER_ROLE_ENTITY = "CUSTOM_HR_ROLE_BD_USER_ROLE_ENTITY";
    private static final String HR_ORG_TREE = "CUSTOM_HR_ROLE_BD_HR_ORG_TREE";
    private static final String HR_ORG_TREE_LONG = "CUSTOM_HR_ROLE_BD_HR_ORG_TREE_LONG";
    private static final String HR_COMBO_FIE_ID = "CUSTOM_HR_COMBO_FIE_ID";
    public static String[] cacheTypePreArr = new String[]{DATA_APP, ORG_SET_ALL, USER_ROLE_ENTITY, HR_ORG_TREE, HR_ORG_TREE_LONG, HR_COMBO_FIE_ID};

    public HRUserRoleCacheUtils() {

    }

    /**
     * 单据所在的应用id
     * @return key
     */
    public static String getTypeEntityApp() {
        return getTypeByPrefix(DATA_APP);
    }

    /**
     * 角色的行政组织
     * @return key
     */
    public static String getTypeOrgSetAll() {
        return getTypeByPrefix(ORG_SET_ALL);
    }

    /**
     * HR行政组织树
     * @return key
     */
    public static String getTypeHROrgTree() {
        return getTypeByPrefix(HR_ORG_TREE);
    }

    /**
     * HR行政组织树
     * @return key
     */
    public static String getTypeHROrgTreeLong() {
        return getTypeByPrefix(HR_ORG_TREE_LONG);
    }

    /**
     * 用户已分配的角色info
     * @return key
     */
    public static String getTypeUserRoleEntity() {
        return getTypeByPrefix(USER_ROLE_ENTITY);
    }

    /**
     * 下拉列表
     * @return key
     */
    public static String getTypeComboField() {
        return getTypeByPrefix(HR_COMBO_FIE_ID);
    }

    public static String getTypeByPrefix(String typePre) {
        return typePre + "_" + getAcctId();
    }

    private static String getAcctId() {
        String acctId = CacheKeyUtil.getAcctId();
        if (acctId.length() != 0) {
            return acctId;
        } else {
            throw new RuntimeException(ResManager.loadKDString("当前数据中心为空。", "HRUserRoleCacheUtils_0", "hrmp-hrcs-business"));
        }
    }

    public static String getCache(String type, String key) {
        try {
            return CACHE.get(type, key);
        } catch (Exception e) {
            LOGGER.error("HRUserRoleCacheUtils.getCache error.", e);
            return null;
        }
    }

    public static void putCache(String type, String key, String value) {
        try {
            CACHE.put(type, key, value, 14400);
        } catch (Exception e) {
            LOGGER.error("HRUserRoleCacheUtils.putCache error.", e);
        }

    }

    public static void putCache(String type, String key, String value, int pExpireTime) {
        try {
            CACHE.put(type, key, value, pExpireTime);
        } catch (Exception e) {
            LOGGER.error("HRUserRoleCacheUtils.putCache error.", e);
        }
    }

    public static void putCacheToday(String type, String key, String value) {
        try {
            int expireTime = secsToTomorrow();
            if (expireTime > 28800) {
                expireTime = 28800;
            }
            CACHE.put(type, key, value, expireTime);
        } catch (Exception e) {
            LOGGER.error("HRUserRoleCacheUtils.putCache error.", e);
        }

    }

    public static void clearCache(String[] arrKeys) {
        try {
            CACHE.remove(arrKeys);
        } catch (Exception e) {
            LOGGER.error("HRUserRoleCacheUtils.clearCache error.", e);
        }
    }

    public static void clearDataEntityCache(String pEntityNum) {
        try {
            DataEntityCacheManager cacheManager = new DataEntityCacheManager(EntityMetadataCache.getDataEntityType(pEntityNum));
            cacheManager.removeByDt();
        } catch (Exception e) {
            LOGGER.error("HRUserRoleCacheUtils.clearDataEntityCache error.", e);
        }

    }

    public static void clearAllCache() {
        clearAllHrCache();
        PermissionServiceHelper.clearAllCache();
    }

    public static void clearAllHrCache() {
        StringBuilder info = new StringBuilder();

        try {
            info.append("HRUserRoleCacheUtils.[clearAllHrCache]has cleaned redis type = ");
            List<String> delKey = Lists.newArrayListWithExpectedSize(16);

            for(int idx = 0; idx < cacheTypePreArr.length; ++idx) {
                String cleanType = getTypeByPrefix(cacheTypePreArr[idx]);
                delKey.add(cleanType);
                info.append(idx + 1).append(".[").append(cleanType).append("] \r\n");
            }

            clearCache(delKey.toArray(new String[0]));
            info.append("has cleaned DataEntityCache.entityNum = ");
            String[] dataEntityCacheArr = new String[]{"hrcs_orgpermconfig"};

            for(int idx = 0; idx < dataEntityCacheArr.length; ++idx) {
                clearDataEntityCache(dataEntityCacheArr[idx]);
                info.append(idx + 1).append(".[").append(dataEntityCacheArr[idx]).append("] \r\n");
            }
        } catch (Exception e) {
            LOGGER.error("HRUserRoleCacheUtils.[clearAllCache] error：{} ", e.getMessage(), e);
        } finally {
            LOGGER.info(info.toString());
        }

    }


    /**
     * 返回一个整数值，表示当前时间距离明天零点之间的秒数
     * @return 当天剩余时间 秒
     */
    public static int secsToTomorrow() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        Calendar now = Calendar.getInstance();
        return (int)(tomorrow.getTime().getTime() - now.getTime().getTime()) / 1000;
    }
}
