package tdkw.opa.tdkw_opa.formplugin.basedata;

import kd.bos.base.BaseShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.AppMenuInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.container.Tab;
import kd.bos.form.control.TreeMenu;
import kd.bos.form.control.events.TreeMenuClickListener;
import kd.bos.form.control.events.TreeNodeEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.util.EventObject;

public class AppHomeFormPlugin extends AbstractFormPlugin implements TreeMenuClickListener {

    @Override
    public void registerListener(EventObject e) {
        TreeMenu navigationBar = this.getView().getControl("navigationbar");
        navigationBar.addTreeMenuClickListener(this);
    }



    @Override
    public void treeMenuClick(TreeNodeEvent treeNodeEvent) {
        //获取点击得当前菜单得源页面编码
        String nodeId = (String) treeNodeEvent.getNodeId();
        AppMenuInfo appMenuInfo = AppMetadataCache.getAppMenuInfo("tdkw_opa", nodeId);
        IPageCache pageCache = this.getView().getPageCache();
        if (appMenuInfo != null && appMenuInfo.getFormId().equals(EntityName.BASE_NOTIFY_CONFIG)) {
            treeNodeEvent.setCancel(true);
            String notifyConfigPageId = pageCache.get("notifyConfigPageId");
            if (StringUtils.isNotBlank(notifyConfigPageId)) {
                Tab submaintab = this.getView().getControl("_submaintab_");

                // 遍历 submaintab 的 items 判断是否包含 notifyConfigPageId
                boolean containsKey = false;
                for (int i = 0; i < submaintab.getItems().size(); i++) {
                    if (submaintab.getItems().get(i).getKey().equals(notifyConfigPageId)) {
                        containsKey = true;
                        break;
                    }
                }

                // 如果包含 notifyConfigPageId，则激活该 Tab
                if (containsKey) {
                    submaintab.activeTab(notifyConfigPageId);
                } else {
                    // 如果不包含 notifyConfigPageId，加载新的页面
                    loadNotifyConfigPage();
                }
            } else {
                // 如果 notifyConfigPageId 为空，加载新的页面
                loadNotifyConfigPage();
            }
        }
    }

    // 加载新的页面的方法
    private void loadNotifyConfigPage() {
        QFilter qFilter = new QFilter("number", QCP.equals, "TZRYPZ_00001");
        DynamicObject notifyConfig = QueryServiceHelper.queryOne(EntityName.BASE_NOTIFY_CONFIG, "id", qFilter.toArray());

        BaseShowParameter parameter = new BaseShowParameter();
        parameter.setFormId(EntityName.BASE_NOTIFY_CONFIG);
        parameter.setPkId(notifyConfig.get("id"));
        parameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        parameter.setCustomParam("open", true);
        String pageId = parameter.getPageId();
        IPageCache pageCache = this.getView().getPageCache();
        pageCache.put("notifyConfigPageId", pageId);
        this.getView().showForm(parameter);
    }

    @Override
    public void treeMenuDoubleClick(TreeNodeEvent treeNodeEvent) {

    }


}
