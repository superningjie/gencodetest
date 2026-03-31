package tdkw.esc.myteam.formplugin.functionalteams;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.exception.KDBizException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Label;
import kd.bos.form.control.Search;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.SearchEnterListener;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.util.HRCollUtil;
import tdkw.hrmp.hrobs.common.myteam.common.bean.TeamResultMap;
import tdkw.hrmp.hrobs.common.myteam.common.orgscopeutil.GetTeamOrgScopeUtil;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Author guokairong
 * @Date 2023/8/14 15:10
 */
public class FunctionalTeamsFormRptPlugin extends AbstractReportFormPlugin implements BeforeF7SelectListener, HyperLinkClickListener, SearchEnterListener {

    private static final Log logger = LogFactory.getLog(FunctionalTeamsFormRptPlugin.class);
    //    private static String searchName;
    // 将 searchName 更改为 AtomicReference 类型  避免多线程同时进来造成的污染
    private static final AtomicReference<String> searchNameRef = new AtomicReference<>();

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        try {
            String name = beforeF7SelectEvent.getProperty().getName();
            FormShowParameter formShowParameter = this.getView().getFormShowParameter();
            long currentUserId = UserServiceHelper.getCurrentUserId();
            TeamResultMap teamOrgScope = GetTeamOrgScopeUtil.getTeamOrgScope(currentUserId, "tdkw_manageteam", "gwbq001", false);
            Set<String> dimValueIds = teamOrgScope.getDimValueIds();
            if ("tdkw_teams".equals(name)) {
                List<Long> dimValueIdList = dimValueIds.stream().map(string -> Long.parseLong(string)).collect(Collectors.toList());
                logger.info("职位子序列id集合" + dimValueIdList);
                // TODO 先不走控权，直接查看所有的职位族
                if (HRCollUtil.isNotEmpty(dimValueIds)) {
                    // 设置列表过滤条件
                    QFilter qFilter = new QFilter("id", "in", dimValueIdList);
                    List<QFilter> treeFilterList = new ArrayList<>();
                    treeFilterList.add(qFilter);
                    ListShowParameter showParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
                    showParameter.getListFilterParameter().setQFilters(treeFilterList);
                }
            }
        } catch (Exception exception) {
            this.getView().showMessage("beforeF7Select异常");
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        if (reportlistap != null) {
            reportlistap.addHyperClickListener(this);
        }
        // 侦听基础资料字段的事件
        BasedataEdit fieldEdit = this.getView().getControl("tdkw_mainpositionfilter");
        if (fieldEdit != null) {
            fieldEdit.addBeforeF7SelectListener(this);
        }

        // 侦听基础资料字段的事件
        BasedataEdit teams = this.getView().getControl("tdkw_teams");
        if (teams != null) {
            teams.addBeforeF7SelectListener(this);
        }

        Search search = this.getControl("tdkw_searchap");
        if (search != null) {
            search.addEnterListener(this);
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        try {
            String name = e.getProperty().getName();
            if ("tdkw_teams".equals(name)) {
                long currentUserId = UserServiceHelper.getCurrentUserId();
                String dimNumber = getModel().getDataEntity().getString("tdkw_teams.number");
                TeamResultMap teamOrgScope = GetTeamOrgScopeUtil.getTeamOrgScope(currentUserId, "tdkw_manageteam", dimNumber, true);
                Set<String> dimValueIds = teamOrgScope.getDimValueIds();
                DynamicObject teams = (DynamicObject) this.getModel().getValue("tdkw_teams");
                // todo 判断teams空值
                Long teamsId = null;
                if (teams != null){
                    teamsId = teams.getLong("id");
                }
                AuthorizedOrgResult result = teamOrgScope.getResult();
                Map<String, Object> paramMap = new HashMap<>();
                if (result != null) {
                    List<Long> hasPermOrgs = result.getHasPermOrgs();
                    paramMap.put("positionids", hasPermOrgs);
                }
                paramMap.put("teams", teamsId);
                this.getView().getQueryParam().setCustomParam(paramMap);
                logger.info("当前用户可查看的岗位id集合" + dimValueIds);
                logger.info("propertyChanged.paramMap" + paramMap);
                this.getView().refresh();

            }
        } catch (KDBizException exception) {
            this.getView().showMessage("propertyChanged异常");
        }

    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        if (StringUtils.equals("tdkw_person", hyperLinkClickEvent.getFieldName())) {
            Long personId = (Long) hyperLinkClickEvent.getRowData().getDynamicObject("tdkw_person").getPkValue();
            DynamicObject hrPerson = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
            if (hrPerson != null) {
                String id = hrPerson.getString(HRBaseConstants.ID);
                //跳转我的档案
                FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                formShowParameter.setCustomParam("erfileId", id);
                formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                formShowParameter.setHasRight(true);
                this.getView().showForm(formShowParameter);
            } else {
                throw new KDBizException("未找到对应人事业务档案");
            }
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //查询任职类型并赋值
        DynamicObjectCollection posTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("enable", QCP.equals, "1");
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_postype", "id", filter.toArray(),"number");
        for (DynamicObject tempId : load) {
            posTypeForDisplay.add(tempId);
        }

        this.getModel().setValue("tdkw_postype", posTypeForDisplay);
        this.getView().updateView("tdkw_postype");
        QFilter enableQf = new QFilter("enable", QCP.equals, "1");
        DynamicObject[] jobfamilyhrDb = BusinessDataServiceHelper.load("hbjm_jobfamilyhr", "id", enableQf.toArray(),"number");
        // 标准化处理 赋默认值
        if (jobfamilyhrDb != null && jobfamilyhrDb.length > 0){
            this.getModel().setValue("tdkw_teams", jobfamilyhrDb[0]);
        }

        try {
            setValue(true);
            this.getView().refresh();
        } catch (Exception exception) {
            this.getView().showMessage("afterCreateNewData异常");
        }
    }

    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        super.beforeQuery(queryParam);
        try {
            setValue(false);
        } catch (Exception e) {
            this.getView().showMessage("beforeQuery异常");
        }
    }

    @Override
    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {
        super.processRowData(gridPK, rowData, queryParam);
        try {
            Map<String, Object> customParam = queryParam.getCustomParam();
            logger.info("processRowData.customParam:{}", customParam);
            if (customParam != null) {
                String avgAgeString = String.valueOf(customParam.get("avgAgeString"));
                String avgWorkAgeString = String.valueOf(customParam.get("avgWorkAgeString"));
                String peopleSize = String.valueOf(customParam.get("peopleSize"));

                logger.info("processRowData.avgAgeString:{}", avgAgeString);
                logger.info("processRowData.avgWorkAgeString:{}", avgWorkAgeString);
                logger.info("processRowData.peopleSize:{}", peopleSize);

                Label peopleNumber = this.getView().getControl("tdkw_teammembers");
                Label averAgeWorkAge = this.getView().getControl("tdkw_averageworkage");
                Label averAgeAge = this.getView().getControl("tdkw_averageage");
                peopleNumber.setText(StringUtils.isEmpty(peopleSize) || "null".equals(peopleSize) ? "0" : peopleSize);
                averAgeWorkAge.setText(StringUtils.isEmpty(avgWorkAgeString) || "null".equals(avgWorkAgeString) ? "0" : avgWorkAgeString);
                averAgeAge.setText(StringUtils.isEmpty(avgAgeString) || "null".equals(avgAgeString) ? "0" : avgAgeString);

            }
        } catch (Exception e) {
            this.getView().showMessage("processRowData异常");
        }
    }

    /**
     * 设置传递的值
     */
    private void setValue(boolean needSetValue) {
        long currentUserId = UserServiceHelper.getCurrentUserId();
        TeamResultMap teamOrgScope = GetTeamOrgScopeUtil.getTeamOrgScope(currentUserId, "tdkw_manageteam", "gwbq001", true);
        Set<String> dimValueIds = teamOrgScope.getDimValueIds();
        // TODO 先不做控权
        if (dimValueIds != null && dimValueIds.size() > 0 && needSetValue) {
            List<String> teamIds = new ArrayList<>(dimValueIds);
            String teamsId = teamIds.get(0);
            this.getModel().setValue("tdkw_teams", Long.valueOf(teamsId));
        }
        try {
            DynamicObject teams = (DynamicObject) this.getModel().getValue("tdkw_teams");
            // todo 判断teams空值
            Long teamsId = null;
            if (teams != null){
                teamsId = teams.getLong("id");
            }

            AuthorizedOrgResult result = teamOrgScope.getResult();
            Map<String, Object> paramMap = new HashMap<>();
            if (result != null) {
                boolean allOrgPerm = result.isHasAllOrgPerm();
                List<Long> hasPermOrgs = result.getHasPermOrgs();
                if (!allOrgPerm) {
                    if (hasPermOrgs.size() > 0) {
                        paramMap.put("positionids", hasPermOrgs);
                    }
                }
            }
            paramMap.put("teams", teamsId);
            //Search search = this.getControl("tdkw_searchap");
            // if (StringUtils.isNotEmpty(searchAp)){
            paramMap.put("name", this.getModel().getValue("tdkw_searchname"));
            logger.info("setValue.paramMap:{}", paramMap);
            // }
            this.getView().getQueryParam().setCustomParam(paramMap);

        } catch (Exception exception) {
            this.getView().showMessage("当前用户没有相关权限");
        }
    }

    @Override
    public void search(SearchEnterEvent searchEnterEvent) {
        Search search = (Search) searchEnterEvent.getSource();
        if (StringUtils.equals("tdkw_searchap", search.getKey())) {
            String searchName = searchEnterEvent.getText();
            this.getModel().setValue("tdkw_searchname", searchName);
            this.getView().refresh();
            logger.info("search.paramMap:{}", searchName);
        }
    }
}
