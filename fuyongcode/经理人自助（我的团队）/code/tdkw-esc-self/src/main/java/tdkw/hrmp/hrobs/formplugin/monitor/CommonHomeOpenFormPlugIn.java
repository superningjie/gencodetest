package tdkw.hrmp.hrobs.formplugin.monitor;

import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.AppMenuInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.form.control.TreeMenu;
import kd.bos.form.control.events.TreeMenuClickListener;
import kd.bos.form.control.events.TreeNodeEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import tdkw.hrmp.hrobs.common.monitor.MonitorUtil;

import java.util.EventObject;
import java.util.Objects;

/**
 * PC运营监控监听左侧菜单点击事件
 *
 * @author xxx
 */
public class CommonHomeOpenFormPlugIn extends AbstractFormPlugin implements TreeMenuClickListener {

    @Override
    public void registerListener(EventObject event) {
        TreeMenu tree = this.getView().getControl("navigationbar");
        tree.addTreeMenuClickListener(this);
    }

    @Override
    public void treeMenuClick(TreeNodeEvent treeNodeEvent) {
        // 应用名称
        String appName = this.getView().getFormShowParameter().getFormName();
        // 应用标识
        String appId = this.getView().getFormShowParameter().getAppId();
        // 操作id
        String operaId = treeNodeEvent.getNodeId().toString();
        // 当前操作名称
        // 2023年12月15日20:08:04 加上null判断
        AppMenuInfo appMenuInfo = AppMetadataCache.getAppMenuInfo(appId, operaId);
        if (Objects.nonNull(appMenuInfo)) {
            LocaleString formName = appMenuInfo.getFormName();
            if (Objects.nonNull(formName)) {
                String operaName = formName.getLocaleValue();
                MonitorUtil.save(appId, appName, operaId, operaName, "menu", "", "1");
            }
        }

    }

    @Override
    public void treeMenuDoubleClick(TreeNodeEvent treeNodeEvent) {

    }
}
