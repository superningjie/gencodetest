package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.container.Tab;
import kd.bos.form.control.events.TabSelectEvent;
import kd.bos.form.control.events.TabSelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.mvc.list.ListView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.util.Collections;
import java.util.EventObject;
import java.util.List;

public class OrgPerfListShowFormPlugin extends AbstractFormPlugin implements TabSelectListener {
    public void registerListener (EventObject e){
        // 页签添加监听事件*
        Tab tab = this.getView().getControl("tdkw_tabap");
        tab.addTabSelectListener(this);
    }
    @Override
    public void tabSelected(TabSelectEvent event) {
        String subTabKey = event.getTabKey();
        String childPageId = this.getPageCache().get(subTabKey);
        IFormView childView = null;
        if (StringUtils.isNotBlank(childPageId)) {

            //获取子页面的view
            childView = this.getView().getView(childPageId);
            ListView listView = (ListView) childView;
            listView.refresh();


            //发送指令，不可缺少
            this.getView().sendFormAction(childView);
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        // 按组织查看
        this.getView().showForm(createViewParameter(
                "tdkw_view_by_org",
                "org",
                null,
                null
        ));

        // 按公司指标查看
        List<QFilter> companyFilters = Collections.singletonList(
                new QFilter("tdkw_hideentry.tdkw_area_type.number", QCP.equals, "T0001")
        );
        this.getView().showForm(createViewParameter(
                "tdkw_view_by_company",
                "company",
                "tdkw_subentryentity",
                companyFilters
        ));

        // 按中心指标查看
        List<QFilter> centerFilters = Collections.singletonList(
                new QFilter("tdkw_hideentry.tdkw_area_type.number", QCP.in, new String[]{"T0002", "T0003","T0004"})
        );
        this.getView().showForm(createViewParameter(
                "tdkw_view_by_center",
                "center",
                "tdkw_subentryentity",
                centerFilters
        ));

    }

    private ListShowParameter createViewParameter(String targetKey, String viewType, String selectedEntity, List<QFilter> filters) {
        ListShowParameter viewParameter = new ListShowParameter();
        viewParameter.setFormId(EntityName.FORM_ORG_VIEW);
        viewParameter.setBillFormId(EntityName.BILL_ORG_PERF_METRICS);
        viewParameter.getOpenStyle().setShowType(ShowType.InContainer);
        viewParameter.getOpenStyle().setTargetKey(targetKey);
        viewParameter.setCustomParam("viewType", viewType);
        String pageId = viewParameter.getPageId();
        this.getView().getPageCache().put(targetKey,pageId);
        if (selectedEntity != null) {
            viewParameter.setSelectedEntity(selectedEntity);
            viewParameter.setCaption("组织绩效列表（按指标）");
        }
        if (filters != null && !filters.isEmpty()) {
            viewParameter.getListFilterParameter().getQFilters().addAll(filters);
        }
        viewParameter.setSendToClient(true);
        return viewParameter;
    }


}
