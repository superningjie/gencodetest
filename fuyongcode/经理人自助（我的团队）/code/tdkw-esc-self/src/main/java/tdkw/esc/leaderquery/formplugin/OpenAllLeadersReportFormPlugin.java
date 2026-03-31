package tdkw.esc.leaderquery.formplugin;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.AppMetadataCache;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.report.ReportShowParameter;

import java.util.EventObject;

public class OpenAllLeadersReportFormPlugin extends AbstractFormPlugin {

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        String autoopen = this.getView().getMainView().getPageCache().get("autoopen");
        if (StringUtils.equals("1", autoopen)) {
            //获取指定appid对应的app内码（即根据t_meta_bizapp表的fnumber获取fid）
            String appId = AppMetadataCache.getAppInfo("tdkw_leader_query").getAppId();
            //创建弹出的单据对象并设置相关属性
            ReportShowParameter showParameter = new ReportShowParameter();
            //打开的单据页面标识
            showParameter.setFormId("tdkw_roster_report");
            //打开的单据标题
            showParameter.setCaption("花名册");
            //打开的应用appId
            showParameter.setAppId("tdkw_leader_query");
            //设置customParam中的appid属性值（必须设置该属性才能出现菜单栏）
            showParameter.setCustomParam("appid", appId);
            //打开风格，这里为新增页签的写法
            showParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
            //设置页签容器的key，这里为应用中的页签容器的标识
            showParameter.getOpenStyle().setTargetKey("_submaintab_");
            this.getView().showForm(showParameter);
        }
    }
}
