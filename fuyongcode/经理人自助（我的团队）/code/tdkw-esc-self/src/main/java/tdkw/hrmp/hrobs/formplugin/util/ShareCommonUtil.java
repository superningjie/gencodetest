package tdkw.hrmp.hrobs.formplugin.util;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

/**
 * 封装一些常用的方法
 */
public class ShareCommonUtil {

    private static final Log logger = LogFactory.getLog(ShareCommonUtil.class);


    /**
     * 获取当前用户的域账号
     *
     * @return 当前用户的SID
     */
    public static String getSid() {
        // 创建QFilter对象，用于查询人员信息
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 查询人员非时序性属性
        DynamicObject pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
        // 如果查询结果为空，则记录错误日志并返回null
        if (pernontsprop == null) {
            logger.error("人员非时序性属性不存在当前人员");
            return null;
        }
        // 返回人员非时序性属性中的tdkw_domainaccount字段值
        return pernontsprop.getString("tdkw_domainaccount");
    }

    /**
     * 从URL中获取子URL。
     * 该方法查找参数"url="在URL中的位置，如果找到，则返回"url="后面的部分；如果未找到，则返回原URL。
     *
     * @param url 输入的URL字符串。
     * @return 如果找到"url="，则返回"url="后面的部分；如果未找到，则返回原URL。
     */
    public static String getSubUrl(String url) {
        // 查找"url="在url字符串中的位置
        int index = url.indexOf("url=");
        if (index == -1) {
            return url;
        } else {
            // 如果找到"url="，则返回"url="后面的部分
            return url.substring(index + 4);
        }
    }

    /**
     * 生成OA单点链接
     *
     * @param bannerUrl 目标链接
     * @return OA分享链接
     */
    public static String getOASSOUrl(String bannerUrl) {
//        String baseUrl = System.getProperty("oa.article");
//        logger.info("获取OA配置的MC参数：" + baseUrl);
//        String sid = getSid();
//        String token = GetTokenUtil.getToken(sid);
//        String completeUrl = baseUrl + "?sid=" + sid + "&token=" + token + "&url=" + bannerUrl;
//        logger.info("ShareCommonUtil.getOASSOUrl=" + completeUrl);
//        return completeUrl;
        return "";
    }

    /**
     * 生成云学堂单点链接
     *
     * @param bannerUrl 目标链接
     * @return OA分享链接
     */
    public static String getElSSOUrl(String bannerUrl) {
//        String property = System.getProperty("oa.bannerstudy");
//        logger.info("获取云学堂配置的MC参数：" + property);
//        String sid = getSid();
//        String token = GetTokenUtil.getToken(sid);
//        String subUrl = ShareCommonUtil.getSubUrl(bannerUrl);
//        String completeUrl = property + "&returnUrl=" + subUrl + "&sid=" + sid + "&token=" + token;
//        logger.info("ShareCommonUtil.getElSSOUrl=" + completeUrl);
//        return completeUrl;
        return "";
    }


    /**
     * 生成招聘单点链接
     *
     * @param bannerUrl 目标链接
     * @return OA分享链接
     */
    public static String getRecSSOUrl(String bannerUrl, String positionId) {
//        String sid = getSid();
//        String saasT = System.getProperty("oa.articleSaast");
//        logger.info("获取saas配置的MC参数：" + saasT);
//        String completeUrl = "https://" +
//                saasT +
//                ":8808/rec/pc/?systemSource=HR&skipPage=PositionDetail&recPostId=" +
//                positionId +
//                "&sid=" +
//                sid +
//                "&token=" +
//                GetTokenUtil.getToken(sid);
//        logger.info("ShareCommonUtil.getRecSSOUrl=" + completeUrl);
//        return completeUrl;
        return "";
    }

}
