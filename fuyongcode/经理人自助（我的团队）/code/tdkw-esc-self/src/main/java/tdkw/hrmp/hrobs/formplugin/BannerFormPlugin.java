package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.EventObject;

/**
 * @Metadata： tdkw_hrobs_pc_banner
 * @Description ： 图片获取赋值
 * @author xxx
 * @Date ：2023/6/1 15:05
 * @Version: 1.0
 */
public class BannerFormPlugin extends AbstractFormPlugin {
    private static final Log logger = LogFactory.getLog(BannerFormPlugin.class);

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        QFilter qFilter = new QFilter("enable", QCP.equals, "1");
        DynamicObject[] load = BusinessDataServiceHelper.load("tdkw_portalbanner", "tdkw_picture,tdkw_url,name", qFilter.toArray(), "createtime desc", 1);
        if (load.length > 0) {
            Object picture = load[0].get("tdkw_picture");
            this.getModel().setValue("tdkw_picture", picture);
            logger.info("图片路径为：", picture);

            String pkValue = String.valueOf(load[0].getPkValue());
            this.getPageCache().put("id", pkValue);
            logger.info("单据内码：", pkValue);
        }
    }
}
