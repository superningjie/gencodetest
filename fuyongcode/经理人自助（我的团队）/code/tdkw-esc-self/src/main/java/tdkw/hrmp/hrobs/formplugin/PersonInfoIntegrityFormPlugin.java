package tdkw.hrmp.hrobs.formplugin;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.EventObject;

public class PersonInfoIntegrityFormPlugin extends AbstractFormPlugin {
    private static final Log logger = LogFactory.getLog(PersonInfoIntegrityFormPlugin.class);


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        Long userId = Long.valueOf(RequestContext.get().getCurrUserId());

        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, "tdkw_ryxxwzd_pc");

        logger.info("人员组织权限" + authorizedAdminOrgSet.getHasPermOrgs());
        if (authorizedAdminOrgSet.isHasAllOrgPerm()) {
            //全集团用户默认XXX集团
            this.getModel().setValue("tdkw_org", 100000);
        } else {
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("haos_adminorghr", "id",
                    new QFilter[]{new QFilter("id", QCP.in, authorizedAdminOrgSet.getHasPermOrgs()).and("datastatus", QCP.equals, "1").and("enable", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1")}, "sortcode");

            logger.info("人员组织权限"+dynamicObjects);
            if (null != dynamicObjects && dynamicObjects.length > 0) {
                this.getModel().setValue("tdkw_org", dynamicObjects[0].get("id"));
            }
        }

        this.getView().updateView("tdkw_org");

    }
}
