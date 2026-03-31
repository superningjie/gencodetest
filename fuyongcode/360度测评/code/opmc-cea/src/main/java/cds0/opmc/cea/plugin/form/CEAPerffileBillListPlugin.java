package cds0.opmc.cea.plugin.form;

import kd.bos.context.RequestContext;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.model.AuthorizedOrgResultWithSub;
import kd.opmc.pmd.formplugin.web.perffile.PerffileBillListPlugin;
import java.util.HashMap;

public class CEAPerffileBillListPlugin extends PerffileBillListPlugin {
    @Override
    public void setFilter(SetFilterEvent e) {
        HasPermOrgResult result = PermissionServiceHelper.getAllPermOrgs(RequestContext.get().getCurrUserId(), OrgViewType.OrgUnit, "4513U+LB/HOD",
                "cea_assactivity_list","49B87WCEVDFK");
        e.setMainOrgQFilter(new QFilter("pmdorg", "in", result.getHasPermOrgs()));
//        QFilter qFilter = (QFilter) HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSDataPermissionService", "getDataRule",
//                new Object[]{RequestContext.get().getCurrUserId(), "4513U+LB/HOD", "cea_assactivity_list", "49B87WCEVDFK", new HashMap<String,Object>()});
        AuthorizedOrgResult permResult = (AuthorizedOrgResult)HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSBizDataPermissionService", "getAuthorizedAdminOrgsF7",
                new Object[]{RequestContext.get().getCurrUserId(), "4513U+LB/HOD", "cea_assactivity_list", "49B87WCEVDFK", "adminorg"});

        e.getQFilters().add(new QFilter("affiliateadminorg.id", "in", permResult.getHasPermOrgs()));
    }
}
