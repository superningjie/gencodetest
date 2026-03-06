package cds0.opmc.cea.plugin.form;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.metadata.dynamicobject.DynamicProperty;
import kd.bos.entity.MainEntityType;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.events.BuildTreeListFilterEvent;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.MetadataServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.util.org.model.OrgTreeModel;
import kd.hr.hbp.formplugin.web.org.template.AdminOrgTreeListTemplate;

import java.util.HashMap;

public class CEAPerfFileBillF7TreeList extends AdminOrgTreeListTemplate {
    private static final String CHK_INCLUDE_CHILD = "chkincludechild";
    private static final String AFFILIATEADMINORG = "affiliateadminorg";
    private static final String AFFILIATEADMINORG_ID = "affiliateadminorg.id";
    private static final String HAOS_ADMINORGSTRUCT = "haos_adminorgstruct";
    private static final String HAOS_ADMINORGDETAIL = "haos_adminorgdetail";

    public CEAPerfFileBillF7TreeList() {
        super(new OrgTreeModel("haos_adminorgstruct", "haos_adminorgdetail", Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
    }

    public void initialize() {
        super.initialize();
    }

    protected String getListPermProKey() {
        return "affiliateadminorg";
    }

    protected QFilter buildNodeClickFilter(BuildTreeListFilterEvent buildTreeListFilterEvent) {
        return new QFilter("affiliateadminorg.id", "in", this.getAllOrgBoIdList());
    }

    protected boolean isDefDirectlySubOrg() {
        return (Boolean)this.getModel().getValue("chkincludechild");
    }

    private boolean getDefaultIsInCludeChild() {
        MainEntityType metadata = MetadataServiceHelper.getDataEntityType(this.getView().getFormShowParameter().getFormId());
        DynamicProperty isInCludeChildProp = metadata.getProperty("chkincludechild");
        boolean isInCludeChild = (Boolean)isInCludeChildProp.getDefaultValue();
        return isInCludeChild;
    }

    public boolean isInCludeChild() {
        return this.getModel().getDataEntity().getBoolean("chkincludechild");
    }

    public void setFilter(SetFilterEvent e) {
        HasPermOrgResult result = PermissionServiceHelper.getAllPermOrgs(RequestContext.get().getCurrUserId(), OrgViewType.OrgUnit, "4513U+LB/HOD",
                "cea_assactivity_list","49B87WCEVDFK");
        e.setMainOrgQFilter(new QFilter("pmdorg", "in", result.getHasPermOrgs()));
        e.getQFilters().add(new QFilter("affiliateadminorg.id", "in", this.getAllOrgBoIdList()));
    }
}
