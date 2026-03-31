package tdkw.hrmp.hrobs.formplugin.frontext;

import kd.bos.context.RequestContext;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.dlock.DLock;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xxx
 * @date 2024年02月01日 16:34
 * @version: 1.0
 */
public class AuthorizedOrgCacheHelper {
    private Log logger = LogFactory.getLog(AuthorizedOrgCacheHelper.class);

    // 是否有全部的权限
    public static final String AUTHORG_CACHE_HAS_ALL = "AuthorgCacheHasAll";

    // 有权限的组织数量
    public static final String AUTHORG_CACHE_ORGS_SIZE = "AuthorgCacheOrgsSize";

    // 如果小于等于MAX_TO_CACHE_TABLE，缓存有权限的组织id,用,隔开
    public static final String AUTHORG_CACHE_ORGS_LIST = "AuthorgCacheOrgsList";

    // 如果大于MAX_TO_CACHE_TABLE，创建临时表
    public static final String AUTHORG_CACHE_TEMP_NAME = "AuthorgCacheTempName";

    // 如果大于这个值，则要换成临时表
    public static final int MAX_TO_CACHE_TABLE = 1000;

    // 当前表单插件
    private AbstractFormPlugin formPlugin = null;

    public AuthorizedOrgCacheHelper(AbstractFormPlugin formPlugin){
        this.formPlugin = formPlugin;
    }

    public boolean getHasAllOrgPerm(){
        boolean result = false;
        String hasAllOrgPermStr = formPlugin.getPageCache().get(AUTHORG_CACHE_HAS_ALL);
        if (StringUtils.isNotBlank(hasAllOrgPermStr)) {
            logger.info("getHasAllOrgPerm from pageCache：" + hasAllOrgPermStr);
            result = Boolean.valueOf(hasAllOrgPermStr);
            return result;
        }
        String lockKey = AUTHORG_CACHE_HAS_ALL + RequestContext.get().getCurrUserId();
        DLock lock = DLock.create(lockKey, "领导自助权限getHasAllOrgPerm页面缓存锁信息");
        // lock.fastMode(); //转换为性能模式，须在申请(tryLock/lock)前设置。
        logger.info("getHasAllOrgPerm lock：" + lock);
        // 未获得锁则返回false
        if (lock.tryLock()) {
            try {
                logger.info("getHasAllOrgPerm lock：" + lock);
                // 再次获取页面缓存
                hasAllOrgPermStr = formPlugin.getPageCache().get(AUTHORG_CACHE_HAS_ALL);
                if (StringUtils.isNotBlank(hasAllOrgPermStr)) {
                    logger.info("getHasAllOrgPerm from pageCache：" + hasAllOrgPermStr);
                    result = Boolean.valueOf(hasAllOrgPermStr);
                    return result;
                }
                // 进入业务逻辑
                // 查询组织树
                AuthorizedOrgResult authorizedOrgResult = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(),
                        "tdkw_roster_pc");
                result = authorizedOrgResult.isHasAllOrgPerm();
                if(!result){
                    List<Long> permOrgs = authorizedOrgResult.getHasPermOrgs();
                    // 如果大于设置的值，则需要创建临时表
                    if(permOrgs.size() > MAX_TO_CACHE_TABLE){
                        String tempTable = createTempTable(permOrgs);
                        // 缓存临时表名
                        formPlugin.getPageCache().put(AUTHORG_CACHE_TEMP_NAME, tempTable);
                    } else {
                        StringBuilder orgListStr = new StringBuilder();
                        for (Long permOrg : permOrgs) {
                            orgListStr.append(permOrg).append(",");
                        }
                        // 小于值则缓存所有id，用,隔开
                        formPlugin.getPageCache().put(AUTHORG_CACHE_ORGS_LIST, orgListStr.substring(0, orgListStr.length() - 1));
                    }
                    // 缓存有权限的组织数量
                    formPlugin.getPageCache().put(AUTHORG_CACHE_ORGS_SIZE, String.valueOf(permOrgs.size()));
                }
                // 如果有全部的组织权限
                formPlugin.getPageCache().put(AUTHORG_CACHE_HAS_ALL, String.valueOf(result));
                logger.info("getHasAllOrgPerm put pageCache：" + result);
            } finally {
                // 解锁
                lock.unlock();
                logger.info("getHasAllOrgPerm unlock：" + lock);
            }
        }
        return result;
    }

    public List<Long> getAuthOrgIdList(){
        List<Long> result = new ArrayList<>();
        String orgListStr = formPlugin.getPageCache().get(AUTHORG_CACHE_ORGS_LIST);
        if (StringUtils.isNotBlank(orgListStr)) {
            logger.info("getAuthOrgList from pageCache：" + orgListStr);
            String[] split = orgListStr.split(",");
            for (String idStr : split) {
                result.add(Long.parseLong(idStr));
            }
        }
        return result;
    }

    public int getAuthOrgSize(){
        String orgSizeStr = formPlugin.getPageCache().get(AUTHORG_CACHE_ORGS_SIZE);
        if (StringUtils.isNotBlank(orgSizeStr)) {
            logger.info("getAuthOrgSize from pageCache：" + orgSizeStr);
            return Integer.parseInt(orgSizeStr);
        }
        return 0;
    }

    public String getTempTableName(){
        String tempTableName = formPlugin.getPageCache().get(AUTHORG_CACHE_TEMP_NAME);
        if (StringUtils.isNotBlank(tempTableName)) {
            logger.info("获取页面缓存临时表：" + tempTableName);
            return tempTableName;
        }
        return "";
    }

    private String createTempTable(List<Long> orgs) {

        String tempTableName = "TMP_ORG_"+ System.currentTimeMillis();
        logger.info("创建组织缓存临时表：" + tempTableName);
        String createTableSql = "/*dialect*/ CREATE TABLE public."+tempTableName+" (" +
                "fid SERIAL PRIMARY KEY," +
                "forgid int8 NOT NULL DEFAULT 0" +
                ")";
        String createIndexSql = "/*dialect*/ CREATE INDEX idx_"+tempTableName+"_orgid ON public."+tempTableName+" USING btree (forgid)";
        DB.update(DBRoute.of("hr"), createTableSql);
        DB.update(DBRoute.of("hr"), createIndexSql);

        int batchSize = 5000;
        int count = orgs.size() / batchSize;
        if(orgs.size() == 0){
            logger.info("组织缓存临时表插入数据完成：" + tempTableName);
            formPlugin.getPageCache().put(AUTHORG_CACHE_TEMP_NAME, tempTableName);
            logger.info("组织缓存临时表放入页面缓存完成：" + tempTableName);
            return tempTableName;
        }
        for (int i = 0; i <= count; i++) {
            int start = i * batchSize;
            int end = (i + 1) * batchSize;
            if (i == count) {
                end = orgs.size();
            }
            List subList = orgs.subList(start, end);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("/*dialect*/ insert into ").append(tempTableName).append(" (forgid) values ");
            for (Long org : orgs) {
                insertSql.append("(").append(org).append("),");
            }

            String insert = insertSql.substring(0, insertSql.length() - 1);
            DB.update(DBRoute.of("hr"), insert);
        }
        logger.info("组织缓存临时表插入数据完成：" + tempTableName);
        formPlugin.getPageCache().put(AUTHORG_CACHE_TEMP_NAME, tempTableName);
        logger.info("组织缓存临时表放入页面缓存完成：" + tempTableName);
        return tempTableName;
    }
}
