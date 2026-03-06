package cds0.opmc.cea.plugin.form;

import static cds0.opmc.cea.common.AppflgConstant.*;

import cds0.opmc.cea.business.entityservice.AssessActivityEntityService;
import cds0.opmc.cea.business.entityservice.DimSettingEntityService;
import cds0.opmc.cea.business.service.AssessActDimSettingDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.AssessManageUtils;
import cds0.opmc.cea.common.enums.AssessActivityStatusEnum;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.*;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.*;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.AbstractOperate;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.constants.newhismodel.HisLineTimeTplConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;


import java.util.*;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.CEA_DIMSETTING;

public class AssessActivityEdit extends HRDataBaseEdit implements BeforeF7SelectListener, HyperLinkClickListener {
    private static final String TOOLBAR = "toolbar";
    // 新增按钮
    private static final String ADD = "add";
    // 删除按钮
    private static final String DEL = "del";
    // 分组设置分录编辑按钮
    private static final String OPERATION = "edit";
    private static final String OPVIEWASSESSFORM = "viewassessform";
    private AssessActDimSettingDomainService ASSESS_ACT_DIMSETTING_DOMAIN_SERVICE = AssessActDimSettingDomainService.getInstance();
    private DimSettingEntityService DIM_SETTING_ENTITY_SERVICE = DimSettingEntityService.getInstance();
    private AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();
    private AssessActDimSettingDomainService ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE = AssessActDimSettingDomainService.getInstance();
    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {

    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        e.setCheckDataChange(false);
    }

    @Override
    public void registerListener(EventObject e){
        super.registerListener(e);
        // 工具栏监听
        Toolbar scoreToolbar = this.getView().getControl(TOOLBAR);
        scoreToolbar.addItemClickListener(this);
        ///注册分组维度设置表格分录的超链接点击事件
        EntryGrid entryGrid = this.getView().getControl(ENTRYENTITY);
        entryGrid.addHyperClickListener(this);
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.afterBindData(e);
        if(OperationStatus.EDIT.equals(getPermOpStatus()) && !HRStringUtils.equals(HRBaseConstants.STR_TRUE,getView().getPageCache().get("newEntryAdd"))){
            DynamicObjectCollection entity = getModel().getEntryEntity(ENTRYENTITY);
            DynamicObject[] dimsettings = DIM_SETTING_ENTITY_SERVICE.queryDimSettingByIds(entity.stream().map(dimsetting -> dimsetting.getLong(ID)).collect(Collectors.toList()));
            Map<Long,DynamicObject> dimsettingMap = Arrays.stream(dimsettings).collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
            entity.stream().forEach(row -> {
                row.set("viewassessform", ResManager.loadKDString("查看", "AssessActivityEdit_0", AppflgConstant.KEY_APP_NAME));
                row.set("edit",ResManager.loadKDString("编辑", "AssessActivityEdit_1", AppflgConstant.KEY_APP_NAME));
                row.set(DIMWEIGHTDIGEST, assembleDimWeightDigest(dimsettingMap.get(row.getLong(ID)).getDynamicObjectCollection(ENTRYENTITY)));
            });
        }
    }



    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        DynamicObjectCollection entity = getModel().getEntryEntity(ENTRYENTITY);
        DynamicObject[] dimSettings = DIM_SETTING_ENTITY_SERVICE.queryDimSettingByIds(entity.stream().map(dimsetting -> dimsetting.getLong(ID)).collect(Collectors.toList()));
        List<Long> dimSettingIds = Arrays.stream(dimSettings).map(d -> d.getLong(ID)).collect(Collectors.toList());
        for(Long id : dimSettingIds){
            boolean flag = ASSESS_ACT_DIM_SETTING_DOMAIN_SERVICE.hasBeingAssessObjInCurrentDimsetting(id);
            if(flag == Boolean.TRUE){ //置灰
                this.getView().setEnable(false,"scoresystemmap","scoresystem","mpnumaccuracy","mpscaletype");
            }
        }
        if(OperationStatus.EDIT.equals(getPermOpStatus())){
            // 测评活动已结束，按钮控制及置灰处理
            if(AssessManageUtils.isAssessActivityFinished(getModel().getDataEntity().getLong(ID))){
                this.getView().setVisible(Boolean.FALSE, "toolbar","save");
                this.getView().setEnable(Boolean.FALSE, "org","adminorg","number","name","realoranoymous","daterangefield","introcontent");
            }
        }
    }
    
    protected OperationStatus getPermOpStatus() {
        // 判断是否是变更状态进来的，如果是则判断是否有修改权限
        boolean isaudit = isAudit();
        // 是不是已经暂存的数据
        boolean isStash = HRBaseConstants.INT_ZERO != (Long) getModel().getValue(ID);
        return (isaudit || isStash)  ? OperationStatus.EDIT : OperationStatus.ADDNEW;
    }

    private boolean isAudit() {
        String ruleStatus = this.getModel().getDataEntity().getString(HRBaseConstants.STATUS);
        String auditStatus = this.getView().getFormShowParameter().getCustomParam(HisLineTimeTplConstants.KEY_HIS_ACTION);
        return HRBaseConstants.STATUS_AUDIT.equals(ruleStatus)
                // 修改已有暂存版本
                || HisLineTimeTplConstants.ACTION_OPEN_DATA_PAGE.equals(auditStatus)
                // 新增数据版本
                || HisLineTimeTplConstants.ACTION_OPEN_INSERT_DATA_PAGE.equals(auditStatus)
                // 历史列表打开单据的历史详情页(修改数据)
                || HisLineTimeTplConstants.ACTION_MODIFY_DATA_PAGE.equals(auditStatus);
    }

    @Override
    public void itemClick(ItemClickEvent evt){
        super.itemClick(evt);
        if(HRStringUtils.equals(ADD,evt.getItemKey())){
            // show出分组维度设置弹窗
            showDimSetting();
        }else if(HRStringUtils.equals(DEL,evt.getItemKey())){
            // 删除分组维度设置
            deleteDimSetting();
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String actionId = closedCallBackEvent.getActionId();
        if(CEA_DIMSETTING.equals(actionId)){
            DynamicObject dimsetting = (DynamicObject) closedCallBackEvent.getReturnData();
            if(dimsetting != null){
                if(HRStringUtils.equals("add",getView().getPageCache().get("entryop"))){
                    refreshDimSettingEntryCache(dimsetting);
                }else if(HRStringUtils.equals("edit",getView().getPageCache().get("entryop"))){
                    updateCurrentDimSettingEntry(dimsetting);
                }
            }
        }else if(CEA_ASSESSACTCONFIRM.equals(actionId)){
            Long activityId = (Long)closedCallBackEvent.getReturnData();
            if(activityId == null){
                activityId = this.getModel().getDataEntity().getLong(ID);
            }
            DynamicObject activity = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(activityId);
            if(!HRStringUtils.equals(AssessActivityStatusEnum.TOBESTARTUP.getValue(),activity.getString("activitystatus"))){
                showProcessManage(activityId);
                getView().close();
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
        showParameter.setCustomParam("assessActId", activityId);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_PROCESSMANAGE));
        this.getView().showForm(showParameter);
    }

    /**
     * 删除分组维度设置
     */
    private void deleteDimSetting(){
        EntryGrid entryGrid = this.getControl(ENTRYENTITY);
        int selectRows[] = entryGrid.getSelectRows();
        DynamicObjectCollection entity=this.getModel().getEntryEntity(ENTRYENTITY);
        if(selectRows!=null && selectRows.length>0){
            StringBuffer message = new StringBuffer();
            // 选中的要删除的分组
            List<DynamicObject> selectDelRows = new ArrayList<DynamicObject>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
            int deny = 1;
            for(int selectRow :selectRows){
                DynamicObject dynamicObject=entity.get(selectRow);  //获取选中行的单据体数据
                // 校验当前维度分组下是否已存在测评中或已完成测评对象
                if(ASSESS_ACT_DIMSETTING_DOMAIN_SERVICE.hasBeingAssessObjInCurrentDimsetting(dynamicObject.getLong(ID))){
                    if(deny > HRBaseConstants.INT_ONE){
                        message.append("\r\n");
                    }
                    message.append(String.format(ResManager.loadKDString("分组:%s下存在测评中或已完成的测评对象，不可删除","AssessActivityEdit_2", KEY_APP_NAME),dynamicObject.getString(GROUPDIMNAME)));
                    deny ++;
                }else{
                    selectDelRows.add(dynamicObject);
                }
            }
            if(!HRStringUtils.isEmpty(message.toString())){
                getView().showTipNotification(message.toString());
            }
            // 删除维度分组及下面的维度数据
            ASSESS_ACT_DIMSETTING_DOMAIN_SERVICE.deleteDimSetting(selectDelRows.stream().map(row -> row.getLong(ID)).collect(Collectors.toList()));
            // 刷新分组维度分录
            entity.removeAll(selectDelRows);
            getModel().updateEntryCache(entity);
            getView().updateView();
        }
    }

    /**
     * show出分组维度设置弹窗
     */
    private void showDimSetting(){
        BaseShowParameter showParameter = new BaseShowParameter();
        showParameter.setFormId(CEA_DIMSETTING);
        showParameter.setStatus(OperationStatus.ADDNEW);
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_DIMSETTING));
        this.getView().showForm(showParameter);
    }

    /**
     * show出分组维度设置弹窗
     */
    private void showDimSettingEdit(OperationStatus operationStatus,Long pkId){
        BaseShowParameter showParameter = new BaseShowParameter();
        showParameter.setFormId(CEA_DIMSETTING);
        showParameter.setPkId(pkId);
        showParameter.setStatus(operationStatus);
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_DIMSETTING));
        this.getView().showForm(showParameter);
    }


    /**
     * 组装维度权重摘要
     * @param dimEntrys
     * @return
     */
    private String assembleDimWeightDigest(DynamicObjectCollection dimEntrys){
        StringBuffer sb = new StringBuffer();
        for(DynamicObject entry : dimEntrys){
            sb.append(entry.getDynamicObject(EVALROLE).getString(NAME)).
            append("(").
            append(entry.getBigDecimal(DIMWEIGHT).doubleValue()).
            append("); ");
        }
        return sb.toString();
    }

    /**
     * 刷新分组维度设置分录缓存
     */
    public void refreshDimSettingEntryCache(DynamicObject dimsetting){
        DynamicObjectCollection dimEntrys = getModel().getEntryEntity(ENTRYENTITY);
        DynamicObject newEntry = dimEntrys.addNew();
        newEntry.set(ID,dimsetting.getLong(ID));
        newEntry.set(GROUPDIMNAME,dimsetting.get(GROUPDIMNAME));
        newEntry.set(DIMWEIGHTDIGEST, assembleDimWeightDigest(dimsetting.getDynamicObjectCollection(ENTRYENTITY)));
        newEntry.set(DESCRIPTION,dimsetting.get(DESCRIPTION));
        newEntry.set(DIMENSION,dimsetting.get(DIMENSION));
        newEntry.set("viewassessform", ResManager.loadKDString("查看", "AssessActivityEdit_0", AppflgConstant.KEY_APP_NAME));
        newEntry.set("edit",ResManager.loadKDString("编辑", "AssessActivityEdit_1", AppflgConstant.KEY_APP_NAME));
        // 打标记新添加分组维度分录
        getView().getPageCache().put("newEntryAdd","true");
        getModel().updateEntryCache(dimEntrys);
        getView().updateView(ENTRYENTITY);
    }

    /**
     * 刷新当前编辑修改的维度分组
     * @param dimsetting
     */
    public void updateCurrentDimSettingEntry(DynamicObject dimsetting){
        DynamicObjectCollection dimEntrys = getModel().getEntryEntity(ENTRYENTITY);
        dimEntrys.stream().filter(e-> e.getLong(ID) == dimsetting.getLong(ID)).collect(Collectors.toList()).forEach(entry ->{
            entry.set(ID,dimsetting.getLong(ID));
            entry.set(GROUPDIMNAME,dimsetting.get(GROUPDIMNAME));
            entry.set(DIMWEIGHTDIGEST, assembleDimWeightDigest(dimsetting.getDynamicObjectCollection(ENTRYENTITY)));
            entry.set(DESCRIPTION,dimsetting.get(DESCRIPTION));
            entry.set(DIMENSION,dimsetting.get(DIMENSION));
            entry.set("viewassessform", ResManager.loadKDString("查看", "AssessActivityEdit_0", AppflgConstant.KEY_APP_NAME));
            entry.set("edit",ResManager.loadKDString("编辑", "AssessActivityEdit_1", AppflgConstant.KEY_APP_NAME));
        });
        getModel().updateEntryCache(dimEntrys);
        getView().updateView(ENTRYENTITY);
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        if(HRStringUtils.equals(hyperLinkClickEvent.getFieldName(),OPERATION)){
            EntryGrid entryGrid = this.getControl(ENTRYENTITY);
            int row = hyperLinkClickEvent.getRowIndex();
            // 当前操作的分组
            DynamicObject rowDimsetting = entryGrid.getModel().getEntryRowEntity(ENTRYENTITY, row);
            if(ASSESS_ACT_DIMSETTING_DOMAIN_SERVICE.hasBeingAssessObjInCurrentDimsetting(rowDimsetting.getLong(ID))){
                // 该分组已有测评中或已完成测评对象，只能查看
                showDimSettingEdit(OperationStatus.VIEW, rowDimsetting.getLong(ID));
            }else{
                if(OperationStatus.EDIT.equals(getPermOpStatus()) && AssessManageUtils.isAssessActivityFinished(getModel().getDataEntity().getLong(ID))){
                    // 编辑态度。且测评活动状态未已结束，分组只能查看
                    showDimSettingEdit(OperationStatus.VIEW, rowDimsetting.getLong(ID));
                }else{
                    // 该分组下没有测评中或已完成测评对象，允许修改
                    // 修改之前先做一次保存 BT-01817482 现场发现 360能力测评 -测评活动 分组设置 新增分组后，点击编辑，显示分组已经被删除
                    ASSESS_ACTIVITY_ENTITY_SERVICE.saveOne(getModel().getDataEntity(true));
                    showDimSettingEdit(OperationStatus.EDIT, rowDimsetting.getLong(ID));
                }
            }

        }else if(HRStringUtils.equals(hyperLinkClickEvent.getFieldName(),OPVIEWASSESSFORM)){
            EntryGrid entryGrid = this.getControl(ENTRYENTITY);
            int row = hyperLinkClickEvent.getRowIndex();
            // 当前操作的分组
            DynamicObject rowDimsetting = entryGrid.getModel().getEntryRowEntity(ENTRYENTITY, row);
            if (rowDimsetting.getLong("assessform.id") == 0L){
                getView().showTipNotification(ResManager.loadKDString("当前分组未导入测评表","AssessActivityEdit_3", KEY_APP_NAME));
                return;
            }
            FormShowParameter showParameter = new FormShowParameter();
            showParameter.getOpenStyle().setShowType(ShowType.Modal);
            showParameter.setStatus(OperationStatus.EDIT);
            showParameter.setFormId(CEA_ASSESSPREVIEW);
            showParameter.setCustomParam("assessActivityId", this.getModel().getDataEntity().getLong(ID));
            showParameter.setCustomParam("dimGroupId",rowDimsetting.getLong(ID));
            showParameter.setCloseCallBack(new CloseCallBack(this, CEA_ASSESSPREVIEW));
            // 显示表单
            this.getView().showForm(showParameter);
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        AbstractOperate source = (AbstractOperate) args.getSource();
        OperateOption option = source.getOption();
        String operateKey = source.getOperateKey();
        switch(operateKey){
            case OP_KEY_SAVE:
                getModel().setValue("status", "A");
                // 保存前处理分组维度设置数据
                assembleDimSettingData();
                break;
        }
    }

    /**
     * 保存前处理分组维度设置数据
     */
    public void assembleDimSettingData(){
        int index = 1;
        for (DynamicObject dimsetting : getModel().getDataEntity().getDynamicObjectCollection(ENTRYENTITY)) {
            dimsetting.set("groupindex", index);
            index++;
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
                && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch(operateKey){
                case OP_KEY_SAVE:
                    // show出操作确认
                    if(HRStringUtils.equals(AssessActivityStatusEnum.TOBESTARTUP.getValue(),String.valueOf(getModel().getValue(ASSESS_ACTIVITY_STATUS)))){
                        // 待启动状态的测评活动，弹出确认框
                        showOperateConfirm(afterDoOperationEventArgs.getOperationResult().getSuccessPkIds().get(0));
                    }
                    break;
            }
        }
    }

    /**
     * show出操作确认框
     */
    private void showOperateConfirm(Object pk){
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId(CEA_ASSESSACTCONFIRM);
        showParameter.setStatus(OperationStatus.VIEW);
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setCustomParam(ASSESS_ACTIVITY_ID, pk);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_ASSESSACTCONFIRM));
        this.getView().showForm(showParameter);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        // BT-01826526 360能力测评 基础设置分制映射=否，分制为非必填，反之必填
        BasedataEdit scoreSystem = (BasedataEdit)this.getControl("scoresystem");
        if(HRStringUtils.equals(HRBaseConstants.STR_ONE, String.valueOf(getModel().getValue("scoresystemmap")))){
            scoreSystem.setMustInput(Boolean.TRUE);
        }else{
            scoreSystem.setMustInput(Boolean.FALSE);
        }
    }
}
