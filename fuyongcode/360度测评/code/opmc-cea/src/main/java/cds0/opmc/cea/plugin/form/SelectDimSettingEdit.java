package cds0.opmc.cea.plugin.form;
import cds0.opmc.cea.business.entityservice.DimSettingEntityService;
import cds0.opmc.cea.business.entityservice.DimassesserEntityService;
import cds0.opmc.cea.common.enums.AssessActivityStatusEnum;
import kd.bos.context.RequestContext;
import kd.bos.form.MessageTypes;
import cds0.opmc.cea.business.entityservice.AssessActivityEntityService;
import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.jobtask.ActivityAddActObjTask;
import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.BasedataEntityType;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.BeforeSetItemValueEventArgs;
import kd.bos.entity.datamodel.events.IDataModelChangeListener;
import kd.bos.entity.property.BasedataProp;
import kd.bos.entity.property.MulBasedataProp;
import kd.bos.form.CloseCallBack;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.*;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.constants.org.TreeTemplateConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;
import kd.opmc.epa.formplugin.web.utils.ActivityDispatchUtils;
import kd.opmc.pmd.business.application.service.PerffileApplicationService;

import java.util.*;
import java.util.stream.Collectors;

public class SelectDimSettingEdit extends HRDataBaseEdit implements BeforeF7SelectListener, BasedataEditListener, IDataModelChangeListener {
    private static final String GROUPSELECTF7 = "groupselect";
    private static final String PMD_PERFFILE = "pmd_perffile";
    private static final String SUCCESS_LIST = "success_list";
    private static final String FAILED_LIST = "faild_list";
    private static final String ALL = "all";
    private static final String ADD_PERFFILE_OBJ = "perffileobj";
    private static final String DIMGROUP = "dimGroup";
    /**
     * 期间开始时间
     */
    String PERIOD_START_DATE = "periodstartdate";

    /**
     * 期间结束时间
     */
    String PERIOD_END_DATE = "periodenddate";
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();
    private static final PerffileApplicationService PERF_FILE_APPLICATION_SERVICE = new PerffileApplicationService();
    private static final DimSettingEntityService DIM_SETTING_ENTITY_SERVICE = DimSettingEntityService.getInstance();
    @Override
    public void initialize() {
        super.initialize();
        this.getModel().addDataModelChangeListener(this);
        BasedataEdit fieldEdit = this.getControl(GROUPSELECTF7);
        fieldEdit.addBasedataEditListener(this);
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 分组基础资料字段监听
        BasedataEdit fieldEdit = this.getView().getControl(GROUPSELECTF7);
        fieldEdit.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent e) {
        String fileKey = e.getProperty().getName();
        if (HRStringUtils.isNotEmpty(fileKey) && GROUPSELECTF7.equals(fileKey)) {
            ListShowParameter showParameter = (ListShowParameter) e.getFormShowParameter();
            showParameter.getListFilterParameter().getQFilters()
                    .add(new QFilter(AppflgConstant.ENTRYID, QCP.equals, getView().getFormShowParameter().getCustomParam("assessActId")));
        }
    }

    @Override
    public void afterBindingData(AfterBindingDataEvent evt) {
        BasedataEdit edit = (BasedataEdit) evt.getSource();
        Object v = evt.getDataEntity();
        Object editSearchProp;
        Object displayProp = "";
        if (v == null) {
            return;
        }
        BasedataEntityType dt;
        if (((DynamicObject) v).getDataEntityType() instanceof BasedataEntityType) {
            dt = (BasedataEntityType) ((DynamicObject) v).getDataEntityType();
        } else {
            dt = (BasedataEntityType) ((BasedataProp) edit.getProperty()).getComplexType();
        }
        //获取数据包中的名称字段值
        String nameKey = dt.getNameProperty();
        IDataEntityProperty p2 = dt.findProperty(nameKey);
        if (p2 != null) {
            displayProp = p2.getValueFast(v);
            if (displayProp instanceof ILocaleString) {
                displayProp = displayProp.toString();
            }
        }
        //动态修改基础资料的显示属性为分组名称
        if (GROUPSELECTF7.equals(edit.getKey())) {
            nameKey = AppflgConstant.GROUPDIMNAME;
        }
        IDataEntityProperty p4 = dt.findProperty(nameKey);
        if (p4 != null) {
            displayProp = String.format("%s", p4.getValueFast(v));
        }
        editSearchProp = getEditSearchProp(edit.getProperty());
        //编辑显示属性也同步修改
        if (StringUtils.isNotBlank(editSearchProp)) {
            editSearchProp = editSearchProp.toString()
                    .replace(AppflgConstant.GROUPDIMNAME, displayProp == null ? "" : displayProp.toString());
        }

        //设置显示属性
        evt.setDisplayProp(displayProp.toString());
        //设置编辑显示属性
        evt.setEditSearchProp(editSearchProp == null ? "" : editSearchProp.toString());
    }

    /**
     * 反解析选择的数据，并设置过滤条件
     *
     * @param e
     */
    @Override
    public void beforeSetItemValue(BeforeSetItemValueEventArgs e) {
        Object value = e.getValue();
        if (value == null) {
            return;
        }
        String searchKey = null;
        IDataEntityProperty property = e.getProperty();
        if (GROUPSELECTF7.equals(property.getName())) {
            searchKey = AppflgConstant.GROUPDIMNAME;
        }
        if (StringUtils.isBlank(searchKey)) {
            return;
        }
        String[] split = value.toString().split(",|;");
        String[] qfliterArr = new String[split.length];
        for (int i = 0; i < split.length; i++) {
            qfliterArr[i] = split[i].split("[( )]")[0];
        }

        //设置查询的字段
        e.setSearchKey(searchKey);
        //设置查询的数据
        e.setSearchArgs(qfliterArr);
    }

    /**
     * 获取配置的编辑显示属性
     *
     * @param property
     * @return
     */
    private String getEditSearchProp(IDataEntityProperty property) {
        BasedataProp basedataProp = null;

        if ((property instanceof BasedataProp)) {
            basedataProp = (BasedataProp) property;
        } else if (property instanceof MulBasedataProp) {
            basedataProp = (BasedataProp) ((MulBasedataProp) property).getRefBaseProp();
        }

        return basedataProp.getEditSearchProp();
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
                && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch (operateKey){
                case AppflgConstant.OP_KEY_CONFIRM:
                    // 先校验该分组是否已上传了测评表
                    if(doCheckBeforeSelect()){
                        // show出绩效档案选择列表
                        showPerfFileSelect();
                    }
                    break;
            }
        }
    }

    private boolean doCheckBeforeSelect(){
        DynamicObject dimGroup = DIM_SETTING_ENTITY_SERVICE.queryDimSettingByPk(this.getModel().getDataEntity().getDynamicObject(GROUPSELECTF7).getLong(AppflgConstant.ID));
        if(dimGroup.getLong("assessform.id") == HRBaseConstants.INT_ZERO){
            getView().showTipNotification(ResManager.loadKDString("当前所选分组未导入测评表，请先导入测评表，再添加测评对象","SelectDimSettingEdit_3", AppflgConstant.KEY_APP_NAME));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * show 出选择绩效档案弹窗（过滤掉已经加入到当前测评活动下的绩效档案）
     */
    private void showPerfFileSelect(){
        ListShowParameter listShowParameter = new ListShowParameter();
        listShowParameter.setBillFormId("pmd_perffile");
        listShowParameter.setFormId("pmd_perff7list"); // pmd_perff7list

        listShowParameter.getOpenStyle().setShowType(ShowType.Modal);
        StyleCss styleCss = new StyleCss();
        styleCss.setWidth("960");
        styleCss.setHeight("580");
        listShowParameter.getOpenStyle().setInlineStyleCss(styleCss);
        listShowParameter.setCloseCallBack(new CloseCallBack(this, PMD_PERFFILE));
        listShowParameter.setLookUp(true);
        listShowParameter.setShowTitle(false);
        DynamicObject[] hasInAssessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjCurrentActivity(getView().getFormShowParameter().getCustomParam("assessActId"));
        Set<Long> perfFileIdSet = Arrays.stream(hasInAssessObjs).map(assObj -> assObj.getLong("perffile.id")).collect(Collectors.toSet());
        listShowParameter.getListFilterParameter().setFilter(new QFilter(HRBaseConstants.BILLSTATUS, QFilter.equals, HRBaseConstants.STATUS_AUDIT));
        listShowParameter.getListFilterParameter().setFilter(new QFilter(HRBaseConstants.ID, QFilter.not_in, perfFileIdSet));
        listShowParameter.setHasRight(Boolean.TRUE);
        AuthorizedOrgResult permResult = HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSBizDataPermissionService", "getAuthorizedAdminOrgsF7",
                new Object[]{RequestContext.get().getCurrUserId(), "4513U+LB/HOD", "cea_assactivity_list", "49B87WCEVDFK", "adminorg"});
        HasPermOrgResult result = PermissionServiceHelper.getAllPermOrgs(RequestContext.get().getCurrUserId(), OrgViewType.OrgUnit, "4513U+LB/HOD",
                "cea_assactivity_list","49B87WCEVDFK");
        if(!permResult.isHasAllOrgPerm()){
            listShowParameter.getListFilterParameter().setFilter(new QFilter("affiliateadminorg.id", "in", permResult.getHasPermOrgs()));
        }
        if (!result.hasAllOrgPerm()) {
            listShowParameter.getListFilterParameter().setFilter(new QFilter("pmdorg", "in", result.getHasPermOrgs()));
        }
        this.getView().getFormShowParameter().setCustomParam(TreeTemplateConstants.CUSTOM_PARENT_F7_PROP, "adminorg");
        this.getView().getFormShowParameter().setCustomParam("customHREntityNumber", "cea_assactivity_list");
        this.getView().getFormShowParameter().setCustomParam("customHRPermItemId", "49B87WCEVDFK");
        this.getView().cacheFormShowParameter();
        this.getView().showForm(listShowParameter);
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        Object returnData = closedCallBackEvent.getReturnData();
        DynamicObject assessAct = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(getView().getFormShowParameter().getCustomParam("assessActId"));
        DynamicObject dimGroup = this.getModel().getDataEntity().getDynamicObject(GROUPSELECTF7);
        if (PMD_PERFFILE.equals(closedCallBackEvent.getActionId())) {
            if (returnData instanceof ListSelectedRowCollection) {
                ListSelectedRowCollection rowCollection = (ListSelectedRowCollection) returnData;
                Object[] perfFilePkArr = rowCollection.getPrimaryKeyValues();
                List<Long> perfFiles = Arrays.asList(perfFilePkArr).stream().map(data -> ((Long) data)).collect(Collectors.toList());
                DynamicObject[] perfFileDynObjArr = PERF_FILE_APPLICATION_SERVICE.getPerffileListByIds("person.name,person.number,startdate,enddate,archivesstatus", perfFiles);
                if(checkAssessObjectAndSave(perfFileDynObjArr,dimGroup)){
                    // 添加完测评对象，就将测评活动状态更新为进行中  BT-01817960 测评已完成，这里还是待启动状态 有人测评中 活动状态 应为进行中 无进行中数据 都为已完成 活动状态应为已完成
                    assessAct.set("activitystatus", AssessActivityStatusEnum.HAVINGIN.getValue());
                    ASSESS_ACTIVITY_ENTITY_SERVICE.updateOne(assessAct);
                }
            }
        }else if(AppflgConstant.CEA_DIMSETTINGSELECT.equals(closedCallBackEvent.getActionId())){
            this.getView().close();
        }
    }

    /**
     * 检查和保存测评对象
     * @param perfFileDynObjArr
     * @param dimGroup
     * @return
     */
    private boolean checkAssessObjectAndSave(DynamicObject[] perfFileDynObjArr, DynamicObject dimGroup) {
        DynamicObject curActivityDynObj = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(getView().getFormShowParameter().getCustomParam("assessActId"));
        StringBuilder errorMessage = new StringBuilder();
        DynamicObjectCollection successList = new DynamicObjectCollection();
        int fail = checkPerfFile(perfFileDynObjArr, curActivityDynObj, errorMessage, successList);
        IFormView parentView = this.getView().getParentView();
        Map<String, String> result = new HashMap<>();
        result.put(SUCCESS_LIST, String.valueOf(successList.size()));
        result.put(FAILED_LIST, String.valueOf(fail));
        result.put(ALL, String.valueOf(perfFileDynObjArr.length));
        result.put("errorMessage", errorMessage.toString());
        this.getView().getParentView().getPageCache().put(result);
        parentView.getFormShowParameter().getCustomParams().putAll(result);

        if (successList.size() != 0) {
            Map<String, Object> params = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
            params.put(AppflgConstant.ACTIVITY_ID, curActivityDynObj.getLong(AppflgConstant.ID));
            params.put(SUCCESS_LIST, successList.stream().map(data -> data.getLong(AppflgConstant.ID)).collect(Collectors.toList()));
            params.put(DIMGROUP,dimGroup.getLong(AppflgConstant.ID));
            ActivityDispatchUtils.dispatch(this, params, ActivityAddActObjTask.class.getName(), "添加测评对象", true, AppflgConstant.CEA_DIMSETTINGSELECT);
            //saveAssessObjectAndEvaluationObject(curActivityDynObj, successList);
        } else {
            if (fail > 0) {
                String title = ResManager.loadKDString("共{0}条单据：添加成功{1}条，失败{2}条",
                        "SelectDimSettingEdit_2", AppflgConstant.KEY_APP_NAME,
                        parentView.getFormShowParameter().getCustomParam(ALL), parentView.getFormShowParameter().getCustomParam(SUCCESS_LIST),
                        parentView.getFormShowParameter().getCustomParam(FAILED_LIST));
                parentView.showMessage(title, parentView.getFormShowParameter().getCustomParam("errorMessage"), MessageTypes.Default);
                //this.getView().invokeOperation("refresh");
                return false;
            }
        }
        return true;
    }

    private int checkPerfFile(DynamicObject[] perfFileDynObjArr, DynamicObject curActivityDynObj, StringBuilder errorMessage, DynamicObjectCollection successList) {
        // 校验2的条件3，已经加入的测评对象对象
        Set<Long> joinedObjIds = Arrays.stream(ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjCurrentActivity(getView().getFormShowParameter().getCustomParam("assessActId")))
                .map(assObj -> assObj.getLong("perffile.id")).collect(Collectors.toSet());
        // 校验4
        Date actStart = curActivityDynObj.getDate(PERIOD_START_DATE);
        Date actEnd = curActivityDynObj.getDate(PERIOD_END_DATE);
        int fail = 0;
        for (DynamicObject object : perfFileDynObjArr) {
            // 校验2：考核执行起止日期不在档案的生效期间内
            Date perfStartDate = object.getDate("startdate");
            Date perfEndDate = object.getDate("enddate");
            if (perfStartDate != null && perfEndDate != null) {
                if (perfEndDate.compareTo(actStart) < 0 || perfStartDate.compareTo(actEnd) > 0) {
                    recordPerfTimeValidateMessage(object, errorMessage);
                    fail++;
                    continue;
                }
            }

            // 校验3，条件3.已经加入的评估对象
            long perfFileId = object.getLong(AppflgConstant.ID);
            boolean joinedObj = joinedObjIds.contains(perfFileId);
            if (joinedObj) {
                recordNotExistValidateMessage(object, errorMessage);
                fail++;
                continue;
            }
            if (HRStringUtils.equals(object.getString("archivesstatus"), "2")) {
                recordPerfileStopMessage(object, errorMessage);
                fail++;
                continue;
            }
            successList.add(object);
        }
        return fail;
    }

    /**
     * 测评活动周期不在档案的生效期间内
     *
     * @param object  实体
     * @param message 提示信息
     */
    private void recordPerfTimeValidateMessage(DynamicObject object, StringBuilder message) {
        if (StringUtils.isNotBlank(message)) {
            message.append("\n");
        }
        message.append(ResManager.loadKDString("{0}-{1}：档案有效期间不在测评周期起止日期内",
                "SelectDimSettingEdit_0", AppflgConstant.KEY_APP_NAME,
                object.getDynamicObject(AppflgConstant.PERSON).getString(AppflgConstant.NAME),
                object.getDynamicObject(AppflgConstant.PERSON).getString(AppflgConstant.NUMBER)));
    }


    /**
     * 数据不存在校验
     *
     * @param object  当前实体
     * @param message 提示信息
     */
    private void recordNotExistValidateMessage(DynamicObject object, StringBuilder message) {
        if (StringUtils.isNotBlank(message)) {
            message.append("\n");
        }
        message.append(ResManager.loadKDString("{0}-{1}：数据不存在或已加入测评活动",
                "SelectDimSettingEdit_1", AppflgConstant.KEY_APP_NAME,
                object.getDynamicObject(AppflgConstant.PERSON).getString(AppflgConstant.NAME),
                object.getDynamicObject(AppflgConstant.PERSON).getString(AppflgConstant.NUMBER)));
    }

    /**
     * 已参与校验
     *
     * @param object 当前实体
     */
    private void recordPerfileStopMessage(DynamicObject object, StringBuilder message) {
        if (StringUtils.isNotBlank(message)) {
            message.append("\n");
        }
        // 已在{考核活动.名称}中参与考核
        message.append(ResManager.loadKDString("{0}-{1}：档案状态为“停止考核”",
                "SelectDimSettingEdit_2", AppflgConstant.KEY_APP_NAME,
                object.getDynamicObject(AppflgConstant.PERSON).getString(AppflgConstant.NAME),
                object.getDynamicObject(AppflgConstant.PERSON).getString(AppflgConstant.NUMBER)));
    }

}
