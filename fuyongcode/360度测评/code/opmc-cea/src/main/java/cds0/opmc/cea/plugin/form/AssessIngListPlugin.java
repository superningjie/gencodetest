package cds0.opmc.cea.plugin.form;

import static cds0.opmc.cea.common.AppflgConstant.*;

import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.entityservice.AssessTaskEntityService;
import cds0.opmc.cea.business.entityservice.DimassesserEntityService;
import cds0.opmc.cea.business.service.AssessObjDomainService;
import cds0.opmc.cea.business.service.MessageService;
import cds0.opmc.cea.common.AssessManageUtils;
import cds0.opmc.cea.common.enums.AssessStatusEnum;

import kd.bos.base.BaseShowParameter;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;

import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;

import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.HyperLinkClickArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseList;

import java.util.*;
import java.util.stream.Collectors;

public class AssessIngListPlugin extends HRDataBaseList {
    private static final String ASSESSGRESS = "assessergress";
    private static final String OP_KEY_UGRY = "ugry";
    private static final String OP_KEY_MODIFYASSESER = "modifyassesser";

    private static final AssessObjDomainService ASSESS_OBJ_DOMAIN_SERVICE = AssessObjDomainService.getInstance();

    private static final DimassesserEntityService DIMASSESSER_ENTITY_SERVICE = DimassesserEntityService.getInstance();
    private static final AssessTaskEntityService ASSESS_TASK_ENTITY_SERVICE = AssessTaskEntityService.getInstance();
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final MessageService MESSAGE_SERVICE = MessageService.getInstance();

    @Override
    public void setFilter(SetFilterEvent evt) {
        // 设置过滤条件，过滤当前测评活动下的待启动的测评对象
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, getView().getFormShowParameter().getCustomParam("assessActId")).
                and("assesstaus", QCP.equals, AssessStatusEnum.ASSESSINGIN.getValue());
        evt.getQFilters().add(qFilter);
        evt.setOrderBy("addintime Desc,id Asc,entryentity.evaldim.id Asc,entryentity.assesser.id Asc");
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(ASSESSGRESS);
    }

    @Override
    public void itemClick(ItemClickEvent evt){
        super.itemClick(evt);
        if(HRStringUtils.equals(ASSESSGRESS,evt.getItemKey())){
            if(AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))){
                // 测评活动已结束，该功能不可用
                getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用","finished_valid",KEY_APP_NAME));
                return;
            }
            // show出测评人进度查看列表
            showAssessProgress();
        }
    }

    private void ugryAssessTaskAssObj(){
        ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
        Set<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toSet());
        DynamicObject[] assessObjs = ASSESS_OBJ_ENTITY_SERVICE.queryAssessObjByPks(selectedIds.stream().collect(Collectors.toList()));
        // 测评对象映射Map
        Map<Long, DynamicObject> assessObjMap = Arrays.stream(assessObjs).collect(Collectors.toMap(k->k.getLong(ID), v->v));
        // 查询勾选的的测评对象 未填报状态的维度测评人对应的待处理的测评任务
        DynamicObject[] toUgryAssessTasks = ASSESS_TASK_ENTITY_SERVICE.queryAssessTaskDimAssesser(Arrays.stream(DIMASSESSER_ENTITY_SERVICE.queryDimAsserAssObj(selectedIds.stream().collect(Collectors.toList()))).
                filter(k-> HRStringUtils.equals(DimAssesserStatusEnum.UNFILLED.getValue(),k.getString("assesstatus")) || HRStringUtils.equals(DimAssesserStatusEnum.FILLING.getValue(),k.getString("assesstatus"))).map(e->e.getLong(ID)).collect(Collectors.toList()));
        // 按照测评对象分组
        Map<Long,List<DynamicObject>> assessObjAssessTaskMap = Arrays.stream(toUgryAssessTasks).collect(Collectors.groupingBy(k->k.getLong("assessobj.id"), Collectors.toList()));
        assessObjAssessTaskMap.entrySet().stream().forEach(entry -> {
            MESSAGE_SERVICE.sendAssessTaskMessageForUgryAssessObj(entry.getValue(),assessObjMap.get(entry.getKey()));
        });
        getView().showSuccessNotification(ResManager.loadKDString("催办成功","AssessIngListPlugin_0", KEY_APP_NAME));
    }

    /**
     * show出测评人进度查看页面
     */
    private void showAssessProgress(){
        ListShowParameter listShowParameter = new ListShowParameter();
        // 显示类型，设置为在容器中显示
        listShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        // 设置要嵌入的单据标识
        listShowParameter.setBillFormId(CEA_ASSESSERMONIT);
        // 设置列表风格
        listShowParameter.setFormId("bos_list");
        listShowParameter.setCustomParam("assessActId",getView().getFormShowParameter().getCustomParam("assessActId"));
        StyleCss styleCss = new StyleCss();
        styleCss.setHeight("91vh");
        listShowParameter.setHasRight(Boolean.TRUE);
        listShowParameter.getOpenStyle().setInlineStyleCss(styleCss);
        // 显示表单
        this.getView().showForm(listShowParameter);
    }

    /**
     * show出设置测评人可编辑列表
     */
    private void showSetAssesserListEdit(List<Long> assesserObjIds){
        FormShowParameter showParameter = new FormShowParameter();
        // 显示类型，设置为在容器中显示
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setStatus(OperationStatus.EDIT);
        // 设置列表风格
        showParameter.setFormId(CEA_DIMASSESSERSETLIST);
        showParameter.setCustomParam(KEY_ASSESSEROBJIDS, assesserObjIds);
        showParameter.setCustomParam(KEY_BUTTON, KEY_ASSESSERUPDATE);
        showParameter.setCustomParam("fromWhere","assessIng");
        StyleCss styleCss = new StyleCss();
        styleCss.setWidth("120vh");
        styleCss.setHeight("70vh");
        showParameter.getOpenStyle().setInlineStyleCss(styleCss);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_DIMASSESSERSETLIST));
        // 显示表单
        this.getView().showForm(showParameter);
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String actionId = closedCallBackEvent.getActionId();
        if(HRStringUtils.equals(CEA_DIMASSESSERSETLIST,actionId)){
            getView().invokeOperation("refresh");
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        //if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
        //        && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch(operateKey){
                case OP_KEY_MODIFYASSESER:
                    if(AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))){
                        // 测评活动已结束，该功能不可用
                        getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用","finished_valid",KEY_APP_NAME));
                        return;
                    }
                    ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
                    Set<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toSet());
                    showSetAssesserListEdit(selectedIds.stream().collect(Collectors.toList()));
                    break;
                case OP_KEY_UGRY:
                    if(AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))){
                        // 测评活动已结束，该功能不可用
                        getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用","finished_valid",KEY_APP_NAME));
                        return;
                    }
                    ugryAssessTaskAssObj();
                    break;
                case "refresh":
                    AssessManageUtils.refreshAssessObjCount(this.getView(),getView().getFormShowParameter().getCustomParam("assessActId"));
                    break;
            }
        //}
    }

    @Override
    public void billListHyperLinkClick(HyperLinkClickArgs args) {
        super.billListHyperLinkClick(args);
        String fieldName = args.getFieldName();
        if ("perffile_name".equals(fieldName)) {
            args.setCancel(true);
            showAssessResult();
        }
    }

    private void showAssessResult() {
        BaseShowParameter baseShowParameter = new BaseShowParameter();
        baseShowParameter.setFormId("cea_assessobjret");
        baseShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        baseShowParameter.setStatus(OperationStatus.VIEW);
        baseShowParameter.setPkId(this.getFocusRowPkId());
        baseShowParameter.setCustomParam("objId",this.getFocusRowPkId());
        DynamicObject dynamicObject = ASSESS_OBJ_DOMAIN_SERVICE.queryObjNameById(this.getFocusRowPkId());
        baseShowParameter.setPageId(this.getView().getPageId() + this.getView().getFormShowParameter().getAppId() + this.getFocusRowPkId());
        String name = dynamicObject.getString("perffile.name");
        String caption = String.format(ResManager.loadKDString("%s的能力素质考核结果", "AssessIngListPlugin_1", "cds-opmc-cea"), name);
        baseShowParameter.setHasRight(Boolean.TRUE);
        baseShowParameter.setCaption(caption);
        this.getView().showForm(baseShowParameter);
    }
}
