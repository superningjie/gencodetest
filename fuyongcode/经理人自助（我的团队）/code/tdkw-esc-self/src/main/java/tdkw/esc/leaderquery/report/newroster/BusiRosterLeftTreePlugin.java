package tdkw.esc.leaderquery.report.newroster;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.exception.KDBizException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IPageCache;
import kd.bos.form.control.Search;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.SearchEnterListener;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.filter.ReportFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.haos.formplugin.web.adminorg.report.AbstractReportPlugin;
import kd.hr.hbp.business.dao.factory.HRBaseDaoFactory;
import kd.hr.hbp.business.servicehelper.org.util.OrgTreeUtils;
import kd.hr.hbp.common.constants.org.OrgTreeDynEnum;
import kd.hr.hbp.common.constants.org.TreeTemplateConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.model.AuthorizedOrgResultWithSub;
import kd.hr.hbp.common.util.HRStringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRStructOrgUtils;
import tdkw.esc.leaderquery.report.newroster.BusiLeftTreePlugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BusiRosterRightPlugin
 *
 * @author xxx
 * @date 2023/10/25
 */
public class BusiRosterLeftTreePlugin extends AbstractReportPlugin implements SearchEnterListener {
    private static final Log logger = LogFactory.getLog(BusiLeftTreePlugin.class);

    private static final String entryNumber = "tdkw_divisionroster_pc";

    @Override
    protected AuthorizedOrgResultWithSub getPermOrgResultWithSub() {
        logger.info("进入getPermOrgResultWithSub方法");
        IPageCache pageCache = getView().getPageCache();
        String orgResultString = pageCache.get(TreeTemplateConstants.CACHE_PERM_ORG_RESULT_WITHSUB);
        logger.info("orgResultString" + orgResultString);
        if (!HRStringUtils.isEmpty(orgResultString)) {
            return SerializationUtils.fromJsonString(orgResultString, AuthorizedOrgResultWithSub.class);
        }
        AuthorizedOrgResultWithSub permOrgResultWithSub = super.getPermOrgResultWithSub();
        logger.info("permOrgResultWithSub" + permOrgResultWithSub.toString());
        if (permOrgResultWithSub != null) {
            pageCache.put(TreeTemplateConstants.CACHE_PERM_ORG_RESULT_WITHSUB, SerializationUtils.toJsonString(permOrgResultWithSub));
        }
        return permOrgResultWithSub;
    }

    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        //super.beforeQuery(queryParam);
        Search search = this.getControl("tdkw_searchap");
        name = search.getSearchKey();
        Map<String, Object> param = new HashMap();
        if (queryParam.getCustomParam().get("orgIds") == null) {
            param.put("orgIds", Collections.singletonList(Long.valueOf(initAllOrgTree().getId())));
            queryParam.setCustomParam(param);
        } else {
            param.put("orgIds", queryParam.getCustomParam().get("orgIds"));
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
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Search search = this.getControl("tdkw_searchap");
        search.addEnterListener(this);
    }

    private String name;

    @Override
    public void search(SearchEnterEvent searchEnterEvent) {
        Search search = (Search) searchEnterEvent.getSource();
        if (StringUtils.equals("tdkw_searchap",search.getKey())){
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
        HashMap<String, String> perParams = new HashMap<>();
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
            case "tdkw_busiunitrosterrpt":
                orgString = "tdkw_divisionroster_pc";
                //TODO 缺少配置
//                String property = System.getProperty("hrobs.properties.structprojectnumber");
                String property = "SY_1040_S";
                QFilter qFilter = new QFilter("number", QCP.equals, property);
                DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("haos_structproject", "id", new QFilter[]{qFilter});
                ArrayList<String> list = new ArrayList<>();
                list.add(dynamicObject.getPkValue().toString());
                perParams.put("struct_project_ids", list.toString());
                formShowParameter.setCustomParam("struct_project_ids", list.toString());
                break;
            default:
                logger.info("没有此张报表的控权处理:" + formId);
                orgString = "tdkw_divisionroster_pc";
        }
        logger.info("控权标识为:" + orgString);

        perParams.put("permEntityId", orgString);
        perParams.put("permItemId", PermissionStatus.View);
        perParams.put("appId", "tdkw_appauthorityleader");
        perParams.put("propKey", "tdkw_hrorg");
        formShowParameter.setCustomParam("permParams", perParams);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs changedArgs) {
        super.propertyChanged(changedArgs);
        String searchName = changedArgs.getProperty().getName();
        if (StringUtils.equals(searchName, "tdkw_searchap")) {
            name = changedArgs.getProperty().getValue("tdkw_searchap").toString();
        }
        logger.info("左树插件:isAdminStructProjectTree" + super.isAdminStructProjectTree());
        String name = changedArgs.getProperty().getName();
        logger.info("值更新控件名为:" + name);
        if (StringUtils.equals(name, "chkincludechild")) {
            getView().refresh();
        }
    }

    @Override
    public void resetColumns(List<AbstractReportColumn> columns) {
        super.resetColumns(columns);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        //TODO 缺少配置
//      String property = System.getProperty("hrobs.properties.structprojectnumber");
        String property = "SY_1040_S";

        QFilter qFilter = new QFilter("number", QCP.equals, property);
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("haos_structproject", "id", new QFilter[]{qFilter});
        this.getModel().setValue("structproject", dynamicObject.getPkValue());
        super.afterCreateNewData(e);
        String formId = this.getModel().getDataEntityType().getName();
        this.getModel().setValue("chkincludechild", Boolean.TRUE);
        this.getView().updateView("chkincludechild");
        this.getView().getPageCache().put("chkincludechild", String.valueOf(Boolean.TRUE));
        getView().refresh();
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);
    }

    @Override
    public DynamicObjectCollection queryCurTreeViewDynamicCollection(QFilter[] qFilters) {
        if (getOrgTreeModel().isHisTree() || !isAdminStructProjectTree()) {
            DynamicObjectCollection dynCol = HRBaseDaoFactory.getInstance(getOrgTreeModel().getEntityName())
                    .queryColl("adminorg.id id, adminorg.boid boid, adminorg.name name, " +
                            "parentorg.id parentorg, structlongnumber, isleaf, adminorg.enable enable,adminorg.tobedisableflag tobedisableflag,adminorg.number", qFilters, "adminorg.number asc");
            if (!isAdminStructProjectTree()) {
                return dynCol;
            }
            if (dynCol == null) return null;
            Map<Long, DynamicObject> dynBoMap = dynCol.stream().collect(Collectors.toMap(dyn -> dyn.getLong(TreeTemplateConstants.BO_ID), dyn -> dyn));
            QFilter sortCodeAdminOrgFilter = new QFilter(TreeTemplateConstants.ADMIN_ORG_ID, QFilter.in, dynBoMap.keySet());
            DynamicObjectCollection sortCodeCol = HRBaseDaoFactory.getInstance(OrgTreeDynEnum.ADMIN_SORT_CODE.getDynEntity())
                    .queryColl("id,adminorg.id orgId, adminorg.number number, sortcode",
                            new QFilter[]{sortCodeAdminOrgFilter, getDataStatusFilter()}, null);
            OrgTreeUtils.sortDynColsOrderBys(sortCodeCol, "sortcode");
            DynamicObjectCollection retDynCol = new DynamicObjectCollection();
            for (DynamicObject dyn : sortCodeCol) {
                long orgId = dyn.getLong("orgId");
                DynamicObject treeNodeDyn = dynBoMap.get(orgId);
                if (treeNodeDyn == null) continue;
                retDynCol.add(treeNodeDyn);
            }
            return retDynCol;
        }
        DynamicObjectCollection dynCol = HRBaseDaoFactory.getInstance(getOrgTreeModel().isHisTree() || !isAdminStructProjectTree() ? getOrgTreeModel().getEntityName() : getOrgTreeModel().getMainEntityName())
                .queryColl(getOrgTreeModel().isHisTree() || !isAdminStructProjectTree() ? "adminorg.id id, adminorg.boid boid, adminorg.name name, " +
                        "parentorg.id parentorg, structlongnumber, isleaf, adminorg.enable enable,adminorg.tobedisableflag tobedisableflag" : " id, boid , name , " + "parentorg,'' isleaf, structlongnumber, enable, tobedisableflag", qFilters, "sortcode");
        return dynCol;
    }

    private QFilter getDataStatusFilter() {
        QFilter dataStatusFilter = new QFilter("datastatus", "in", this.getOrgTreeModel().getDataStatusList());
        if (kd.bos.dataentity.utils.StringUtils.isNotBlank(this.getOrgTreeModel().getInitStatus())) {
            dataStatusFilter.and(new QFilter("initstatus", "=", this.getOrgTreeModel().getInitStatus()));
        }

        dataStatusFilter.and(new QFilter("iscurrentversion", "=", "1"));
        return dataStatusFilter;
    }
}
