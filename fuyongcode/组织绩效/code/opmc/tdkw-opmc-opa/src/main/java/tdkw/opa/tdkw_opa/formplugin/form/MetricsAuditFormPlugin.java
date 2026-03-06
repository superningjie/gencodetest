package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.container.Tab;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.Date;
import java.util.EventObject;
import java.util.UUID;

public class MetricsAuditFormPlugin extends AbstractFormPlugin {
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        drawMiddleAreas();
    }

    private void drawMiddleAreas() {
        String first= "";
        DynamicObjectCollection orgEntryentity = this.getModel().getEntryEntity("tdkw_org_entryentity");
        for (DynamicObject orgEntry : orgEntryentity) {
            FormShowParameter showParameter = new FormShowParameter();
            showParameter.setFormId("tdkw_audti_innner");
            showParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
            showParameter.getOpenStyle().setTargetKey("tdkw_tabap");
            String orgName = orgEntry.getString("tdkw_adminorg.name");
            showParameter.setCaption(orgName);
            Date assessYear = orgEntry.getDate("tdkw_assess_year");
            DynamicObjectCollection rows = orgEntry.getDynamicObjectCollection("tdkw_org_subentry");
            showParameter.setCustomParam("assessYear", assessYear);
            showParameter.setCustomParam("rows", rows);
            String pageId = UUID.randomUUID().toString().replace("-", "");
            if (StringUtils.isBlank(first)){
                first = pageId;
            }
            //设置为异步处理
            showParameter.setSendToClient(true);

            this.getView().showForm(showParameter);
        }
        Tab tab = this.getView().getControl("tdkw_tabap");
        tab.activeTab(first);

    }

}
