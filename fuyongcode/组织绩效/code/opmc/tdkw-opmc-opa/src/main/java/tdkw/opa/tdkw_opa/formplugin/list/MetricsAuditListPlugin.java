package tdkw.opa.tdkw_opa.formplugin.list;

import kd.bos.context.RequestContext;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;
import tdkw.opa.tdkw_opa.formplugin.utils.HRRoleAndPersonUtils;

import java.util.List;

public class MetricsAuditListPlugin extends AbstractListPlugin {

    private static final Log logger = LogFactory.getLog(MetricsAuditListPlugin.class);

    @Override
    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);
        long currUserId = RequestContext.get().getCurrUserId();
        AuthorizedOrgResult orgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, EntityName.BILL_ORG_PERF_METRICS, "tdkw_adminorg");
        boolean hasAllOrgPerm = orgSet.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPermOrgs = orgSet.getHasPermOrgs();
            QFilter qFilter = new QFilter("tdkw_metrics_adminorg.fbasedataid", QCP.in, hasPermOrgs);
            e.addCustomQFilter(qFilter);
        } else {
            logger.info("有所有组织权限hasAllOrgPerm" + hasAllOrgPerm);
        }

    }
}
