package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.util.StringUtils;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

import java.util.EventObject;

/**
 * @author xxx
 * @date 2023/11/13
 * @description 人力自助首页长图，需要能够单点跳转某个链接
 */
public class PictureClick extends AbstractFormPlugin {
    private static final Log logger = LogFactory.getLog(PictureClick.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        String id = this.getPageCache().get("id");
        if (StringUtils.isNotEmpty(id)) {
            DynamicObject portalbanner = BusinessDataServiceHelper.loadSingle(id, "tdkw_portalbanner", "id,tdkw_skipornot,tdkw_url,tdkw_mc,enable");
            if ("1".equals(portalbanner.getString("enable"))) {
                if (portalbanner.getBoolean("tdkw_skipornot")) {
                    String openUrl = getUrl(portalbanner);
                    logger.info("url组装参数：" + openUrl);
                    if (openUrl != null) {
                        this.getView().openUrl(openUrl);
                    }
                }
            } else {
                logger.info("原首页图片已被禁用,刷新首页图片");
                QFilter qFilter = new QFilter("enable", QCP.equals, "1");
                DynamicObject[] load = BusinessDataServiceHelper.load("tdkw_portalbanner", "id,tdkw_skipornot,tdkw_url,tdkw_mc,enable,tdkw_picture", qFilter.toArray(), "createtime desc", 1);
                if (load != null && load.length > 0) {
                    Object picture = load[0].get("tdkw_picture");
                    this.getModel().setValue("tdkw_picture", picture);
                    logger.info("图片路径为：", picture);
                    String pkValue = String.valueOf(load[0].getPkValue());
                    this.getPageCache().put("id", pkValue);
                    logger.info("单据内码：", pkValue);
                    this.getView().updateView();
                }

            }


        }
    }

    private String getUrl(DynamicObject entity) {
//        String url = entity.getString("tdkw_url");
//        logger.info("获取跳转路径为：" + url);
//        String mc = entity.getString("tdkw_mc");
//        String property;
//        if (StringUtils.isNotEmpty(mc)) {
//            switch (mc) {
//                case "0":
//                    property = System.getProperty("oa.bannerstudy");
//                    logger.info("获取云学堂MC参数：" + property);
//                    String sid = getSid();
//                    String token = GetTokenUtil.getToken(sid);
//                    String subUrl = getSubUrl(url);
//                    String completeUrl = property + "&returnUrl=" + subUrl + "&sid=" + sid + "&token=" + token;
//                    logger.info("completeUrl=" + completeUrl);
//                    return completeUrl;
//                case "1":
//                    logger.info("社招路径直接打开：" + url);
//                    return url;
//            }
//        }
        return null;
    }

    private String getSubUrl(String url) {
        int index = url.indexOf("url=");
        if (index == -1) {
            return url;
        } else {
            return url.substring(index + 4);
        }
    }

    private String getSid() {
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员非时序性属性
        DynamicObject pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
        if (pernontsprop == null) {
            logger.error("人员非时序性属性不存在当前人员");
            return null;
        }
        return pernontsprop.getString("tdkw_domainaccount");
    }
}