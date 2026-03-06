package tdkw.hrmp.hrobs.formplugin.broadcastmap;

import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDBizException;
import kd.bos.form.control.events.ClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.ShareCommonUtil;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

import java.util.EventObject;
import java.util.Map;
import java.util.Objects;


/**
 * HR自助门户首页轮播图插件
 */
public class BroadcastMapFormPlugin extends AbstractFormPlugin {

    private static final Log logger = LogFactory.getLog(BroadcastMapFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_portal_broadcastmap");
    }


    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Map<String, Object> paramsMap = ((ClickEvent) evt).getParamsMap();
        if (MapUtils.isEmpty(paramsMap)) {
            return;
        }
        // 获取图片顺序
        String imageIndex = MapUtils.getString(paramsMap, "imageUrl");
        // 查询轮播图数据、首页轮播图固定编码 ！！！ 必须固定不然查不到数据
        QFilter qFilter = new QFilter("number", QCP.equals, "tdkw_portal_banner");
        DynamicObject bosCarouselBase = BusinessDataServiceHelper.loadSingle("bos_carouselbase", "entryentity.tdkw_banner_url,entryentity.tdkw_banner_type,entryentity.tdkw_banner_open_flag,entryentity.tdkw_banner_img_index", qFilter.toArray());
        DynamicObjectCollection broadcastDataList = bosCarouselBase.getDynamicObjectCollection("entryentity");
        DynamicObject broadcastData = broadcastDataList.stream().filter(dynamicObject -> ((Integer.parseInt(imageIndex) + 1) + "").equals(dynamicObject.get("tdkw_banner_img_index"))).findFirst().orElse(null);
        Objects.requireNonNull(broadcastData, "轮播图数据为空！");
        // 获取参数配置
        String type = broadcastData.getString("tdkw_banner_type.number");
        String bannerUrl = broadcastData.getString("tdkw_banner_url");
        String openFlag = broadcastData.getString("tdkw_banner_open_flag");
        String customParam = broadcastData.getString("tdkw_custom_param");
        JSONObject customParamObject = null;
        if (StringUtils.isNotBlank(customParam)) {
            try {
                customParamObject = JSONObject.parseObject(customParam);
            } catch (Exception e) {
                logger.error("轮播图自定义参数解析失败！", e);
            }
        }
        // 不跳转直接返回
        if (StringUtils.equals("0", openFlag)) {
            return;
        }
        if (StringUtils.isBlank(bannerUrl)) {
            throw new KDBizException("请检查url配置！");
        }
        String openUrl = getUrl(type, bannerUrl, customParamObject);
        // 链接为空不允许跳转
        if (StringUtils.isBlank(openUrl)) {
            return;
        }
        // 根据对应的类型拼接对应的单点登录url
        this.getView().openUrl(openUrl);
    }


    /**
     * 根据传入的bannerMc和bannerUrl获取最终的url
     *
     * @param type      类型
     * @param bannerUrl url
     */
    private String getUrl(String type, String bannerUrl, JSONObject customParamObject) {
        String url = "";
        String property;
        switch (type) {
            // 云学堂跳转
            case "XY00001":
                return ShareCommonUtil.getElSSOUrl(bannerUrl);
            // 社招跳转
            case "XY00002":
                return bannerUrl;
            // 内招跳转
            case "XY00003":
                return ShareCommonUtil.getRecSSOUrl(bannerUrl, customParamObject.getString("positionId"));
            // OA跳转
            case "XY00004":
                return ShareCommonUtil.getOASSOUrl(bannerUrl);
            // 普通外链
            case "XY00005":
                return url;
        }
        return url;
    }

}
