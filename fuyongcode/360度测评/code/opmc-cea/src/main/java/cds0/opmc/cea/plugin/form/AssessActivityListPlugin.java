package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessActivityEntityService;
import cds0.opmc.cea.business.service.AssessActDimSettingDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.enums.AssessActivityStatusEnum;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.filter.FilterColumn;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.*;

import static cds0.opmc.cea.common.AppflgConstant.*;

import kd.bos.form.operate.AbstractOperate;
import kd.bos.form.operate.FormOperate;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.list.ListDataProvider;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseList;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class AssessActivityListPlugin extends HRDataBaseList {
    private static final Log LOG = LogFactory.getLog(AssessActivityListPlugin.class);
    private static final AssessActDimSettingDomainService ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE = AssessActDimSettingDomainService.getInstance();
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();

    @Override
    public void setFilter(SetFilterEvent evt) {
        evt.setOrderBy("modifytime Desc,createtime Desc");
    }

    @Override
    public void registerListener(EventObject e){
        super.registerListener(e);
        this.addItemClickListeners("test");
    }

    @Override
    public void itemClick(ItemClickEvent evt){
        super.itemClick(evt);
        if(HRStringUtils.equals("test",evt.getItemKey())){
            //Set<Long> assessObjIds = new HashSet<>(Arrays.asList(1991567917648464896L));
            //Long evalDimId = 1990069201770919937L;
            //AssessTaskDomainService.getInstance().assembleAssessObjStatuSubmit(assessObjIds.stream().collect(Collectors.toList()), evalDimId);//nextEvalDimAssessTaskTrigger(assessObjIds.stream().collect(Collectors.toList()), evalDimId);

            //List<Long> personIds = new ArrayList<Long>();
            //personIds.add(1976056009080202240L);
            //Map<Long, List<Long>> superiorMap = HandlerFindDomainService.getInstance().getPeerColegeIdsByPersonIds(personIdSet.stream().collect(Collectors.toList()));// getPeerColegeIdsByPersonIds DimassesserDomainService.getInstance().getSuperiorIdsByPersonIds(personIds);*/
            //List<Long> superiorList = new ArrayList<>();
            //superiorMap.entrySet().stream().forEach(entry -> superiorList.addAll(entry.getValue()));
            //Map<Long, List<Long>> superiorMap1 = HandlerFindDomainService.getInstance().getDirectChildrenIdsByPersonIds(superiorList.stream().collect(Collectors.toSet()).stream().collect(Collectors.toList()));
            return;
        }
    }
    @Override
    public void filterContainerInit(FilterContainerInitArgs args) {
        super.filterContainerInit(args);
        FilterColumn filterColumn = args.getFilterColumn("org.name");
        filterColumn.setDefaultValue(HRStringUtils.EMPTY);
        //LOG.info("默认选中组织: " + filterColumn.getDefaultValues());
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
    }

    /**
     * 列表初始化
     *
     * @param args args
     */
    @Override
    public void beforeCreateListDataProvider(BeforeCreateListDataProviderArgs args) {
        args.setListDataProvider(new ListDataProvider() {
            @Override
            public DynamicObjectCollection getData(int start, int limit) {
                DynamicObjectCollection rows = super.getData(start, limit);
                if (rows.isEmpty()) {
                    return rows;
                }
                for (DynamicObject row : rows) {
                    Long assessActId = row.getLong(ID);
                    row.set("assessorcount", ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.countAssessObjInCurrentActivity(assessActId));
                }
                return rows;
            }
        });
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        AbstractOperate source = (AbstractOperate) args.getSource();
        OperateOption option = source.getOption();
        String operateKey = source.getOperateKey();
        switch(operateKey){
            case OP_KEY_DELETE:
                // 校验该数据状态是否可删除
                if(!doCheckBeforeDelete()){
                    args.setCancel(Boolean.TRUE);
                }
                break;
        }
    }


    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String actionId = closedCallBackEvent.getActionId();
        if(HRStringUtils.equals(CEA_ACTFINISHCONFIRM,actionId)){
            if(HRStringUtils.equals(OP_KEY_CANCEL,getView().getPageCache().get("confirmop"))){
                // 取消结束活动
                return;
            }else if(HRStringUtils.equals(OP_KEY_FINISH,getView().getPageCache().get("confirmop"))){
                // 结束活动
                ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
                List<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toList());
                ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.finishAssessActivity(selectedIds);
                getView().updateView();
            }
        }
    }

    /**
     * 删除测评活动前校验是否允许删除
     * @return
     */
    private boolean doCheckBeforeDelete(){
        ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
        List<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toList());
        DynamicObject[] assessActs = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityArrayByPk(selectedIds);
        Set<String> selectedAssessActStatusSet = Arrays.stream(assessActs).map(act -> act.getString(ASSESS_ACTIVITY_STATUS)).collect(Collectors.toSet());
        if(selectedAssessActStatusSet.contains(AssessActivityStatusEnum.HAVINGIN.getValue()) || selectedAssessActStatusSet.contains(AssessActivityStatusEnum.FINISHED.getValue())){
            // 选择的测评活动存在进行中的数据，提示并终止删除操作
            getView().showTipNotification(ResManager.loadKDString("存在进行中或已结束的测评活动，不支持删除", "AssessActivityListPlugin_2", AppflgConstant.KEY_APP_NAME));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * show出结束测评活动二次确认弹窗
     */
    private void showSecondAssActFinishConfirm(String validMessage){
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId(CEA_ACTFINISHCONFIRM);
        showParameter.setStatus(OperationStatus.VIEW);
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setCustomParam("validMessage", validMessage);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_ACTFINISHCONFIRM));
        this.getView().showForm(showParameter);
    }

    /**
     * 执行删除测评活动
     */
    private void doDeleteAssessAct(){
        ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
        List<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toList());
        ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.deleteAssessActivity(selectedIds);
        getView().updateView();
        getView().showSuccessNotification("删除成功");
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
                && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch(operateKey){
                case OP_KEY_STARTUP:
                    startUpBatch();
                    getView().updateView();
                    break;
                case OP_KEY_DELETE:
                    doDeleteAssessAct();
                    break;
                case OP_KEY_FINISH:
                    finishAssessActivity();
                    break;
                case OP_KEY_PROCESSMANAGE:
                    FormOperate source = (FormOperate)afterDoOperationEventArgs.getSource();
                    Long activityId = (Long) source.getListFocusRow().getPrimaryKeyValue();
                    showProcessManage(activityId);
                    break;
            }
        }
    }

    /**
     * show出执行过程管理页面
     */
    private void showProcessManage(Long activityId){
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId(CEA_PROCESSMANAGE);
        showParameter.setStatus(OperationStatus.VIEW);
        showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        showParameter.setCustomParam("assessActId",activityId);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_PROCESSMANAGE));
        this.getView().showForm(showParameter);
    }

    /**
     * 结束测评活动
     */
    private void finishAssessActivity(){
        // 校验要结束的测评活动中是否存在测评中的测评对象
        StringBuffer validMessage = new StringBuffer();
        ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
        List<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toList());
        DynamicObject[] assessActs = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityArrayByPk(selectedIds);
        Set<String> selectedAssessActStatusSet = Arrays.stream(assessActs).map(act -> act.getString(ASSESS_ACTIVITY_STATUS)).collect(Collectors.toSet());
        if(selectedAssessActStatusSet.contains(AssessActivityStatusEnum.FINISHED.getValue())){
            // 选择的测评活动存在已结束的数据，提示并终止结束操作
            getView().showTipNotification(ResManager.loadKDString("存在已结束的测评活动，不允许结束", "AssessActivityListPlugin_4", AppflgConstant.KEY_APP_NAME));
            return;
        }
        AtomicInteger deny = new AtomicInteger(1);
        selectedRows.stream().forEach(row -> {
            if(ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.hasAssessingObjInActivity(Long.parseLong(String.valueOf(row.getPrimaryKeyValue())))){
                if(deny.get() > HRBaseConstants.INT_ONE){
                    validMessage.append("、");
                }
                validMessage.append(row.getName());
                deny.getAndIncrement();
            }
        });
        if(!HRStringUtils.isEmpty(validMessage.toString())){
            // show出二次确认弹窗
            showSecondAssActFinishConfirm(String.format(ResManager.loadKDString("活动:%s下存在测评中的测评对象，结束活动会失效活动下的测评任务","AssessActivityListPlugin_3", KEY_APP_NAME),validMessage.toString()));
        }else{
            // 测评活动没有测评中的测评对象,直接失效
            ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.finishAssessActivity(selectedIds);
            getView().updateView();
            getView().showSuccessNotification(ResManager.loadKDString("结束成功", "AssessActivityListPlugin_5", KEY_APP_NAME));
        }
    }

    /**
     * 列表批量启动测评活动
     */
    private void startUpBatch(){
        ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
        List<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toList());
        DynamicObject[] assessActs = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityArrayByPk(selectedIds);
        Set<String> selectedAssessActStatusSet = Arrays.stream(assessActs).map(act -> act.getString(ASSESS_ACTIVITY_STATUS)).collect(Collectors.toSet());
        if(selectedAssessActStatusSet.contains(AssessActivityStatusEnum.HAVINGIN.getValue()) || selectedAssessActStatusSet.contains(AssessActivityStatusEnum.FINISHED.getValue())){
            // 选择的测评活动存在进行中或已结束的数据，提示并终止启动操作
            getView().showTipNotification(ResManager.loadKDString("存在进行中或已结束的测评活动，不允许启动", "AssessActivityListPlugin_1", AppflgConstant.KEY_APP_NAME));
            return;
        }
        ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.startUpBatch(selectedIds);
        getView().showSuccessNotification(ResManager.loadKDString("启动成功", "AssessActivityListPlugin_0", AppflgConstant.KEY_APP_NAME));
    }
}
