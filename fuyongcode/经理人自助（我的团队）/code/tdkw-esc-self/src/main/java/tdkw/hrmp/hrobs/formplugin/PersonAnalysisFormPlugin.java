package tdkw.hrmp.hrobs.formplugin;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * @author xxx
 * @Date 2023/6/28 9:54
 * @Description 人员分析 表单插件
 * @Demander xxx
 * @Document https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personanalysis
 * @Version 1.0
 **/
public class PersonAnalysisFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {
    private static final Log logger = LogFactory.getLog(PersonAnalysisFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit tdkw_org = this.getControl("tdkw_org");
        tdkw_org.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        if ("tdkw_org".equals(event.getProperty().getName())) {
            // 所属组织
            /*QFilter qFilter = new QFilter("orgtype.number", QCP.in, new String[]{"XY00001", "XY00002", "XY00003", "XY00004", "XY00005"});
            ListShowParameter showParameter = (ListShowParameter) event.getFormShowParameter();
            showParameter.getListFilterParameter().getQFilters().add(qFilter);*/

            // 2025-10-31 增加报表过滤，当前人组织
            //获取当前登录人员所属组织
            List<Long> userIds = new ArrayList<>(1);
            userIds.add(UserServiceHelper.getCurrentUserId());
            // 获取当前登录人员所属公司
            String adminOrg = String.valueOf(UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId()));
            QFilter qFilter = new QFilter("orgtype.number", QCP.in, new String[]{adminOrg});
            ListShowParameter showParameter = (ListShowParameter) event.getFormShowParameter();
            showParameter.getListFilterParameter().getQFilters().add(qFilter);
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        DynamicObjectCollection employTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_laborreltype", "id", filter.toArray());
        for (DynamicObject tempId : load) {
            employTypeForDisplay.add(tempId);
        }
// TODO 报错 屏蔽

/*        this.getModel().setValue("tdkw_employtype", employTypeForDisplay);
        this.getView().updateView("tdkw_employtype");*/


        Long userId = Long.valueOf(RequestContext.get().getCurrUserId());


        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, this.getModel().getDataEntityType().getName(), "tdkw_org");

        logger.info(String.valueOf(authorizedAdminOrgSet.getHasPermOrgs()));
        if (authorizedAdminOrgSet.isHasAllOrgPerm()) {

            // 2025-10-31 增加报表过滤，当前人组织
            //获取当前登录人员所属组织
            List<Long> userIds = new ArrayList<>(1);
            userIds.add(UserServiceHelper.getCurrentUserId());
            // 获取当前登录人员所属公司
            String adminOrg = String.valueOf(UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId()));
            this.getModel().setValue("tdkw_org", Long.valueOf(adminOrg));
            //全集团用户默认XXX集团
//            this.getModel().setValue("tdkw_org", 100000);
        } else {
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("haos_adminorghr", "id",
                    new QFilter[]{new QFilter("orgtype.number", QCP.in, new String[]{"XY00001", "XY00002", "XY00003", "XY00004", "XY00005"})
                    .and("id", QCP.in, authorizedAdminOrgSet.getHasPermOrgs()).and("datastatus",QCP.equals,"1").and("enable", QCP.equals,"1").and("iscurrentversion", QCP.equals,"1")}, "sortcode");

            if (null != dynamicObjects && dynamicObjects.length > 0){
                this.getModel().setValue("tdkw_org", dynamicObjects[0].get("id"));
            }
        }

        this.getView().updateView("tdkw_org");

    }
}
