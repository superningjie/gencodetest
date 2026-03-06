package tdkw.esc.leaderquery.integratedquery.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.entity.tree.TreeNode;
import kd.bos.exception.KDBizException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.Search;
import kd.bos.form.control.TreeView;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.SearchEnterListener;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.filter.ReportFilter;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.hr.haos.common.constants.masterdata.AdminOrgConstants;
import kd.hr.haos.formplugin.web.adminorg.report.AbstractReportPlugin;
import kd.hr.haos.formplugin.web.adminorg.util.TreeNodeNameSetter;
import kd.hr.hbp.business.dao.factory.HRBaseDaoFactory;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.constants.org.OrgTreeDynEnum;
import kd.hr.hbp.common.model.OrgSubInfo;
import kd.hr.hbp.common.util.org.model.OrgTreeModel;
import kd.hr.hbp.formplugin.web.org.structproject.StructProjectCapable;
import kd.hr.hbp.formplugin.web.org.structproject.imp.AdminOrgSpProcessor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.integratedquery.report.LeftTreePlugin;

import java.util.*;
import java.util.stream.Collectors;


/**
 * TestLeftTreePlugin
 *
 * @author xxx
 * @date 2023/7/25
 */


public class LeftTreePluginTest extends AbstractReportPlugin implements SearchEnterListener {


    private static final Log logger = LogFactory.getLog(LeftTreePlugin.class);
    private String name;
    private static final String KEY_TREEVIEW = "treeviewap";
    private OrgTreeModel orgTreeModel;
    private TreeNode rootNode;
    private static final String CUR_TREE_ROOT_STRUCT_FIELDS = "adminorg.id id, adminorg.boid boid,adminorg.name name, structlongnumber, adminorg.enable enable,adminorg.tobedisableflag tobedisableflag";
    private static final String CUR_SUB_TREE_STRUCT_FIELDS = "adminorg.id id, adminorg.boid boid, adminorg.name name, parentorg.id parentorg, structlongnumber, isleaf, adminorg.enable enable,adminorg.tobedisableflag tobedisableflag";
    private static final String CUR_SUB_TREE_FIELDS = " id, boid , name , parentorg,'' isleaf, structlongnumber, enable, tobedisableflag";
    private List<String> allPermLongNumberList;
    private QFilter dataStatusAndBSedFilter;
    private QFilter orgEnableFilter;
    public static final String SELECT_ALL_TREENODE = "select_all_treenode";
    String subTreeOrderBys = "sortcode";
    private static final String CHK_INCLUDE_CHILD = "chkincludechild";
    private String orgStructNumberProperty = "adminorg.structnumber";
    private TreeNodeNameSetter treeNodeNameSetter;
    private TreeView treeView;
    private Boolean isShowDisable = null;
    public static final String PARENT_ORG = "parentorg";
    private StructProjectCapable structProjectCapable;


    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        //this.getView().getPageCache().put(TreeTemplateConstants.CHK_SHOW_DISABLE + StructProjectConstants.ORG_TREE_FILTER, "1");
        Search search = this.getControl("tdkw_searchap");
        name = search.getSearchKey();
        Map<String, Object> param = new HashMap();
        if (queryParam.getCustomParam().get("orgIds") == null) {
            List<Long> longs = Collections.singletonList(Long.valueOf(initAllOrgTree().getId()));
            String rootId = super.initAllOrgTree().getId();
            List<Long> orgIds = this.getOrgIds(rootId);
            param.put("orgIds", orgIds);
            logger.info("左树默认进去传至右表的参数" + longs);
            queryParam.setCustomParam(param);
        } else {
            List<Long> orgIds = (List<Long>) queryParam.getCustomParam().get("orgIds");
            param.put("orgIds", orgIds);
            logger.info("左树点击传至右表的id的size" + orgIds.size());
            logger.info("左树点击传至右表的id的" + orgIds);
        }
        param.put("searchName", name);
        logger.info("param.put(searchName, name)" + name);
        queryParam.setCustomParam(param);
        //集团司龄
        FilterItemInfo cliqueStart = queryParam.getFilter().getFilterItem("tdkw_comagestart");
        FilterItemInfo cliqueEnd = queryParam.getFilter().getFilterItem("tdkw_comageend");
        if (ObjectUtils.isNotEmpty(cliqueStart) && ObjectUtils.isNotEmpty(cliqueEnd)) {
            Integer cliqueStartInt = (Integer) cliqueStart.getValue();
            Integer cliqueEndInt = (Integer) cliqueEnd.getValue();
            boolean check = check(cliqueStartInt, cliqueEndInt);
            if (check) {
                logger.info("集团司龄输入有误");
                throw new KDBizException("集团司龄输入数据有误,请检查!");
            }
        }
        //社会工龄
        FilterItemInfo socialStart = queryParam.getFilter().getFilterItem("tdkw_social_workage");
        FilterItemInfo socialEnd = queryParam.getFilter().getFilterItem("tdkw_social_workagen");
        if (ObjectUtils.isNotEmpty(socialStart) && ObjectUtils.isNotEmpty(socialEnd)) {
            Integer socialStartInt = (Integer) socialStart.getValue();
            Integer socialEndInt = (Integer) socialEnd.getValue();
            boolean check = check(socialStartInt, socialEndInt);
            if (check) {
                logger.info("社会工龄输入有误");
                throw new KDBizException("社会工龄输入数据有误,请检查!");
            }
        }
        //年龄
        FilterItemInfo ageStart = queryParam.getFilter().getFilterItem("tdkw_agestart");
        FilterItemInfo ageEnd = queryParam.getFilter().getFilterItem("tdkw_ageend");
        if (ObjectUtils.isNotEmpty(ageStart) && ObjectUtils.isNotEmpty(ageEnd)) {
            Integer ageStartInt = (Integer) ageStart.getValue();
            Integer ageEndInt = (Integer) ageEnd.getValue();
            boolean check = check(ageStartInt, ageEndInt);
            if (check) {
                logger.info("年龄输入有误");
                throw new KDBizException("年龄输入数据有误,请检查!");
            }
        }
        //公司司龄
        FilterItemInfo companyStart = queryParam.getFilter().getFilterItem("tdkw_company_agestart");
        FilterItemInfo companyEnd = queryParam.getFilter().getFilterItem("tdkw_company_ageend");
        if (ObjectUtils.isNotEmpty(companyStart) && ObjectUtils.isNotEmpty(companyEnd)) {
            Integer companyStartInt = (Integer) companyStart.getValue();
            Integer companyEndInt = (Integer) companyEnd.getValue();
            boolean check = check(companyStartInt, companyEndInt);
            if (check) {
                logger.info("公司司龄输入有误");
                throw new KDBizException("公司司龄输入数据有误,请检查!");
            }
        }


    }

    @Override
    public void propertyChanged(PropertyChangedArgs changedArgs) {
        boolean adminStructProjectTree = super.isAdminStructProjectTree();
        logger.info("前左树插件:isAdminStructProjectTree" + adminStructProjectTree);
        String name = changedArgs.getProperty().getName();
        super.propertyChanged(changedArgs);
        //logger.info("左树插件:isAdminStructProjectTree" + super.isAdminStructProjectTree());
        adminStructProjectTree = super.isAdminStructProjectTree();
        logger.info("后左树插件:isAdminStructProjectTree" + adminStructProjectTree);
        // String name = changedArgs.getProperty().getName();
        logger.info("值更新控件名为:" + name);
        if (StringUtils.equals(name, "chkincludechild")) {
            getView().refresh();
        }
    }

    /**
     * 数据合法性校验
     *
     * @param start
     * @param end
     * @return 没有通过返回true
     */


    private Boolean check(Integer start, Integer end) {
        boolean check = false;
        if (start > end || start < 0 || start > 120 || end > 120) {
            check = true;
        }
        return check;
    }

    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);
        FormShowParameter formShowParameter = e.getFormShowParameter();
        //权限控制假实体标识
        String orgString = "";
        String formId = formShowParameter.getFormId();
        switch (formId) {
            case "tdkw_leave_retire_report":
                orgString = "tdkw_departandretire_pc";
                break;
            case "tdkw_allleaders_report":
                orgString = "tdkw_allexecutive_pc";
                break;
            case "tdkw_discipline_report":
                orgString = "tdkw_disciplineinspec_pc";
                break;
            case "tdkw_management_report":
                orgString = "tdkw_frgsggrz_pc";
                break;
            case "tdkw_groupcom_report":
                orgString = "tdkw_leaguecommittee_pc";
                break;
            case "tdkw_middle_leader_report":
                orgString = "tdkw_middlelevelcadres_pc";
                break;
            case "tdkw_party_com_report":
                orgString = "tdkw_partycommittee_pc";
                break;
            case "tdkw_roster_report":
                orgString = "tdkw_roster_pc";
                break;
            case "tdkw_financeman_report":
                orgString = "tdkw_financeperson_pc";
                break;
            case "tdkw_trade_union_report":
                orgString = "tdkw_tradeunion_pc";
                break;
            default:
                orgString = "tdkw_divisionroster_pc";
        }
        HashMap<String, String> perParams = new HashMap<>();
        perParams.put("permEntityId", orgString);
        perParams.put("permItemId", PermissionStatus.View);
        perParams.put("appId", "tdkw_appauthorityleader");
        perParams.put("propKey", "tdkw_hrorg");
        formShowParameter.setCustomParam("permParams", perParams);
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Search search = this.getControl("tdkw_searchap");
        search.addEnterListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        this.getModel().setValue("chkincludechild", Boolean.TRUE);
        this.getView().updateView("chkincludechild");
        this.getView().getPageCache().put("chkincludechild", String.valueOf(Boolean.TRUE));
        getView().refresh();
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);
    }

    @Override
    public void search(SearchEnterEvent searchEnterEvent) {
        Search search = (Search) searchEnterEvent.getSource();
        if (StringUtils.equals("tdkw_searchap", search.getKey())) {
//            System.out.println("searchEnterEvent = " + searchEnterEvent);
            name = searchEnterEvent.getText();
            getView().refresh();
        }

    }

    @Override
    public List<String> getSearchList(SearchEnterEvent evt) {
        return SearchEnterListener.super.getSearchList(evt);
    }

    @Override
    public List<Object> getComPlexSearchList(SearchEnterEvent evt) {
        return SearchEnterListener.super.getComPlexSearchList(evt);
    }

    private List<Long> getOrgIds(String nodeId) {
        QFilter qFilter = new QFilter(AdminOrgConstants.ADMINORG, QCP.equals, Long.valueOf(nodeId));
        // 根据nodeID构建树
        OrgTreeModel orgTreeModel4Scene = this.getOrgTreeModel4Scene();
        DynamicObject dynamicObject = HRBaseDaoFactory.getInstance(orgTreeModel4Scene.getEntityName())
                .queryOne(AdminOrgConstants.STRUCT_LONG_NUMBER, new QFilter[]{getOrgTreeFilter4Scene(), qFilter});
        String longNumber = dynamicObject.getString(AdminOrgConstants.STRUCT_LONG_NUMBER);
        QFilter longNumberLike = new QFilter(AdminOrgConstants.STRUCT_LONG_NUMBER, QFilter.like, longNumber + "%");
        QFilter enabledFilter = new QFilter(HRBaseConstants.ENABLE, QCP.equals, HRBaseConstants.ENABLED);
        if (isShowDisable4Scene()) {
            enabledFilter = new QFilter(HRBaseConstants.ENABLE, QCP.in, new String[]{HRBaseConstants.ENABLED, HRBaseConstants.DISABLED});
        }
        List<Long> permOrgIds = this.getPermOrgResultWithSub().getHasPermOrgsWithSub().stream().map(OrgSubInfo::getOrgId).collect(Collectors.toList());
        QFilter permQFilter = this.getPermOrgResultWithSub().isHasAllOrgPerm() ? new QFilter(HRBaseConstants.STR_ONE, QCP.equals, HRBaseConstants.INT_ONE) : new QFilter("adminorg.id", QCP.in, permOrgIds);
        DynamicObjectCollection dynamicObjectCollection = HRBaseDaoFactory.getInstance(orgTreeModel4Scene.getEntityName())
                .queryColl("adminorg.id", new QFilter[]{getOrgTreeFilter4Scene(), longNumberLike, permQFilter, enabledFilter}, null);
        List<Long> ids = dynamicObjectCollection.stream().map(dyn -> dyn.getLong("adminorg.id")).collect(Collectors.toList());
        if (this.getPermOrgResultWithSub().isHasAllOrgPerm()) {
            ids.add(Long.valueOf(nodeId));
        } else {
            List<Long> permIds = this.getPermOrgResultWithSub().getHasPermOrgsWithSub().stream().map(OrgSubInfo::getOrgId).collect(Collectors.toList());
            if (permIds.contains(Long.valueOf(nodeId))) {
                ids.add(Long.valueOf(nodeId));
            }
        }
        return ids;
    }


    private OrgTreeModel getOrgTreeModel4Scene() {
        return new OrgTreeModel(OrgTreeDynEnum.ADMIN_STRUCT.getDynEntity(), OrgTreeDynEnum.ADMIN_MAIN_ENTITY.getDynEntity(), Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
    }

    private boolean isShowDisable4Scene() {
        if (this.isShowDisable != null) {
            return this.isShowDisable;
        } else {
            String cacheStrShowDisable = this.getView().getPageCache().get("chkshowdisableorg_tree_filter");
            this.isShowDisable = cacheStrShowDisable == null ? Boolean.FALSE : Boolean.parseBoolean(cacheStrShowDisable);
            return this.isShowDisable;
        }
    }

    private QFilter getOrgTreeFilter4Scene() {
        QFilter orgTreeFilter = this.getOrgCommonFilter4Scene();
        if (this.structProjectCapable == null) {
            this.structProjectCapable = new AdminOrgSpProcessor();
        }
        if (this.structProjectCapable.enableStructProjectCode()) {
            orgTreeFilter.and(this.getTreeStructProjectFilter());
        }
        return orgTreeFilter;
    }

    private QFilter getOrgCommonFilter4Scene() {
        QFilter dataStatusAndBSedFilter = this.getDataStatusAndBSedFilter();
        dataStatusAndBSedFilter.and(this.getOrgEnableFilter());
        return dataStatusAndBSedFilter;
    }


    private void setDefaultStructProject4Scene() {
        if (this.hasStructProjectControl()) {
            this.getModel().setValue("structproject", this.structProjectCapable.getStructProject());
            this.cacheStructProject4perm4Scene();
        }
    }

    private void cacheStructProject4perm4Scene() {
        List<String> list = new ArrayList<>();
        list.add(this.structProjectCapable.getStructProject().getString("id"));
        this.getView().getPageCache().put("tree_perm_structprojectids", SerializationUtils.toJsonString(list));
    }
}
