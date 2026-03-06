package tdkw.hrmp.hrobs.formplugin.talent;


import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.AppMetadataCache;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.container.Tab;
import kd.bos.form.control.events.TabSelectEvent;
import kd.bos.form.control.events.TabSelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date: 2024/6/17
 * @Meta: tdkw_hrobs_erfilelistdv
 * @Description: 人员简历的人才画像
 **/
public class PortraitFormOfResumePlugin extends AbstractFormPlugin implements TabSelectListener {
    private static final Log logger = LogFactory.getLog(PortraitFormOfResumePlugin.class);

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        String erfileId = this.getView().getFormShowParameter().getCustomParam("erfileId");
        boolean authority = talentPortrait(erfileId);
        //没有权限隐藏人才画像
        if (!authority) {
            this.getView().setVisible(false, "tdkw_portrait");
        }
    }

    /**
     * 查询人才画像权限，有权限缓存中存储人才档案id
     *
     * @param erfileId 人员的pkid
     */
    public boolean talentPortrait(String erfileId) {
        logger.info("erfileId" + erfileId);
        if (ObjectUtils.isEmpty(erfileId)) {
            logger.info("erfileIdisEmpty");
            return false;
        }
        QFilter idQf = new QFilter("id", QCP.equals, Long.valueOf(erfileId));
        QFilter iscurrentversionQf = new QFilter("iscurrentversion", QCP.equals, "1");
        //数据版本状态1-生效中
        QFilter datastatusQf = new QFilter("datastatus", QCP.equals, "1");
        // 根据档案Id 查询人员Id
        DynamicObject ermanfileObj = BusinessDataServiceHelper.loadSingle("hspm_ermanfile", "person", new QFilter[]{idQf,iscurrentversionQf,datastatusQf});
        if (ObjectUtils.isEmpty(ermanfileObj)) {
            logger.info("ermanfileObj isEmpty");
            return false;
        }

        QFilter perOntFilter = new QFilter("id", QCP.equals, ermanfileObj.getLong("person.id"));
        perOntFilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject propObj = BusinessDataServiceHelper.loadSingle("hrpi_person", "person", new QFilter[]{perOntFilter});
        if (ObjectUtils.isEmpty(propObj)) {
            logger.info("propObjisEmpty");
            return false;
        }
        QFilter filter = new QFilter("person.number", QCP.equals, propObj.getString("number"))
                .and("iscurrentversion", QCP.equals, "1")
                .and(new QFilter("isprimary", QCP.equals, true));
        DynamicObject talentFile = BusinessDataServiceHelper.loadSingle("tlmg_talentfile",
                "person,affiliateadminorg", new QFilter[]{filter});
        if (ObjectUtils.isEmpty(talentFile)) {
            logger.info("该人员没有人才档案erfileId" + erfileId + "number" + propObj.getString("number"));
            return false;
        }
        long talentId = talentFile.getLong("id");
//        boolean authority = hasAuthority(talentId, RequestContext.get().getCurrUserId());
//        logger.info("authority" + authority);
//        if (authority) {
//            this.getPageCache().put("talentId", String.valueOf(talentId));
//        }
//        return authority;
        if (!ObjectUtils.isEmpty(talentId)) {
            this.getPageCache().put("talentId", String.valueOf(talentId));
        }

        return true;
    }

    /**
     * 查看pc端是否有人才档案权限
     *
     * @param orgId 打开人员的行政组织id
     * @return 是否有权限观看
     */
    public static boolean hasAuthority(Long talentId, Long orgId, Long userId) {
        logger.info("talentId" + talentId + "orgId" + orgId + "userId" + userId);
        if (isCurrUser(talentId)) {
            return true;
        }
        //数据范围
        QFilter filter = HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSDataPermissionService", "getDataRule",
                UserServiceHelper.getCurrentUserId(), AppMetadataCache.getAppInfo("tdkw_work").getId(), "tdkw_tlmg_portrait_auth", PermissionStatus.View, null);
        if (!ObjectUtils.isEmpty(filter)) {
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("tdkw_tlmg_portrait_auth", "id", filter.toArray());
            List<Long> ids = Arrays.stream(dynamicObjects).map(o -> o.getLong("id")).collect(Collectors.toList());
            if (!ids.contains(talentId)) {
                logger.info("notinids");
                return false;
            }
        }
        boolean authority = PermissionServiceHelper.checkPermission(userId, AppMetadataCache.getAppInfo("tdkw_work").getId(), "tdkw_tlmg_portrait_auth", PermissionStatus.View);
        logger.info("authority" + authority);
        if (!authority) {
            return false;
        }
        AuthorizedOrgResult authorizedAdminOrgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(userId, "tdkw_tlmg_portrait_auth", "affiliateadminorg");
        if (authorizedAdminOrgSet.isHasAllOrgPerm()) {
            logger.info("isHasAllOrgPerm");
            return true;
        }
        List<Long> hasPermOrgs = authorizedAdminOrgSet.getHasPermOrgs();
        logger.info("hasPermOrgs" + hasPermOrgs);
        return hasPermOrgs.contains(orgId);
    }

    /**
     * 查看pc端是否有人才档案权限
     *
     * @param userId 打开人员的行政组织id
     * @return 是否有权限观看
     */
    public static boolean hasAuthority(Long talentId, Long userId) {
        if (isCurrUser(talentId)) {
            return true;
        }
        DynamicObject talentFile = BusinessDataServiceHelper.loadSingle(talentId, "tlmg_talentfile",
                "person,affiliateadminorg");
        boolean authority;
        if ("true".equals(System.getProperty("tdkw.tlmg.portrait.authority"))) {
            authority = hasAuthority(talentId, talentFile.getLong("affiliateadminorg.id"), userId);
            logger.info("tlmg-authority" + authority);
        } else {
            String erfileId = getErfileId(talentFile.getLong("person.id"));
            if (StringUtils.isEmpty(erfileId)) {
                logger.info("没有人事业务档案id" + talentId);
            }
            authority = DispatchServiceHelper.invokeService(
                    "tdkw.hrmp.tdkw_hrobs_ext.servicehelper",
                    "tdkw_hrobs_ext",
                    "ResumeQueryAuthorityService",
                    "queryAuthority",
                    erfileId, "PC"
            );
            logger.info("tdkw_hrobs_ext-authority" + authority);
        }
        return authority;
    }

    /**
     * 是否当前用户
     *
     * @param talentId 人才档案id
     * @return 是否查看自己的档案
     */
    public static boolean isCurrUser(Long talentId) {
        DynamicObject user = BusinessDataServiceHelper.loadSingle(RequestContext.get().getCurrUserId(), "bos_user");
        DynamicObject talentFile = BusinessDataServiceHelper.loadSingle(talentId, "tlmg_talentfile", "id,number");
        return StringUtils.equals(user.getString("number"), talentFile.getString("number"));
    }

    /**
     * 获取人事业务档案id
     *
     * @param personId 人员id
     * @return 人事业务档案id
     */
    public static String getErfileId(long personId) {
        //人员非时序属性
        QFilter perOntFilter = new QFilter("id", QCP.equals, personId);
        DynamicObject personObj = BusinessDataServiceHelper.loadSingle("hrpi_person", "tdkw_pkid", new QFilter[]{perOntFilter});
        return personObj.getString("tdkw_pkid");
    }

    @Override
    public void tabSelected(TabSelectEvent tabSelectEvent) {
        String subTabKey = tabSelectEvent.getTabKey();
        if ("tdkw_portrait".equals(subTabKey) && ObjectUtils.isEmpty(this.getPageCache().get("isInit"))) {
            Long talentId = Long.valueOf(this.getPageCache().get("talentId"));
            FormShowParameter showParameter = new FormShowParameter();
            showParameter.setCustomParam("talentId", talentId);
            showParameter.setFormId("tdkw_talent_portrait");
            showParameter.getOpenStyle().setShowType(ShowType.InContainer);
            showParameter.getOpenStyle().setTargetKey("tdkw_portraitflex");
            this.getView().showForm(showParameter);
            this.getPageCache().put("isInit", "true");
            this.getView().updateView("tdkw_portraitflex");
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Tab tab = this.getView().getControl("tdkw_tabap");
        tab.addTabSelectListener(this);
    }
}
