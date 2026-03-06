package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.jobtask.ActivityDeleteActObjTask;
import cds0.opmc.cea.business.jobtask.ActivityStartUpActObjTask;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.AssessManageUtils;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.*;
import kd.bos.form.operate.AbstractOperate;
import kd.bos.mvc.list.ListDataProvider;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseList;
import kd.opmc.epa.formplugin.web.utils.ActivityDispatchUtils;

import java.util.*;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.*;

public class ToStartUpListPlugin extends HRDataBaseList {
    private static final String SUCCESS_LIST = "success_list";
    private static final String ADD = "tblnew";
    private static final String UPLOADJOBREPORT = "uploadjobreport";
    private static final String SETASSESSER = "setassesser";
    private static final String VIEW_ASSESSFORM = "viewassessform";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(ADD);
        this.addItemClickListeners(SETASSESSER);
        this.addItemClickListeners("");
    }

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
                    if (HRStringUtils.isEmpty(row.getString("reportwork.fattachmentname"))) {
                        row.set("hasreportwork", ResManager.loadKDString("未上传", "tostartuplistplugin_1", AppflgConstant.KEY_APP_NAME));
                    } else {
                        row.set("hasreportwork", ResManager.loadKDString("已上传", "tostartuplistplugin_2", AppflgConstant.KEY_APP_NAME));
                    }
                }
                return rows;
            }
        });
    }

    @Override
    public void setFilter(SetFilterEvent evt) {
        // 设置过滤条件，过滤当前测评活动下的待启动的测评对象
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, getView().getFormShowParameter().getCustomParam("assessActId")).
                and("assesstaus", QCP.equals, AssessStatusEnum.TOASSESS.getValue());
        evt.getQFilters().add(qFilter);
        evt.setOrderBy("addintime Desc,id Asc,entryentity.evaldim.id Asc,entryentity.assesser.id Asc");
    }

    /**
     * show出选择分组弹窗
     */
    private void showSelectDimSetting() {
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId(CEA_DIMSETTINGSELECT);
        showParameter.setStatus(OperationStatus.EDIT);
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCustomParam("assessActId", getView().getFormShowParameter().getCustomParam("assessActId"));
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_DIMSETTINGSELECT));
        getView().showForm(showParameter);
    }

    /**
     * show出上传测评对象的述职报告的弹窗
     */
    private void showUploadjobreport() {

        List<Long> assessers = new ArrayList<>();
        getSelectedRows().stream().forEach(row -> assessers.add((Long) row.getPrimaryKeyValue()));
        List<Long> assesserObjIds = assessers.stream().distinct().collect(Collectors.toList());

        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId("cea_uploadjobreport");
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setCustomParam("assesserObjIds", assesserObjIds);
        showParameter.setHasRight(Boolean.TRUE);
        this.getView().showForm(showParameter);

    }

    /**
     * 批量删除测评对象
     */
    private void deleteAssessObjBatch() {
        ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
        Set<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toSet());
        Map<String, Object> params = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        params.put(AppflgConstant.ACTIVITY_ID, getView().getFormShowParameter().getCustomParam("assessActId"));
        params.put(SUCCESS_LIST, selectedIds.stream().collect(Collectors.toList()));
        ActivityDispatchUtils.dispatch(this, params, ActivityDeleteActObjTask.class.getName(), "删除测评对象", true, AppflgConstant.CEA_ASSOBJ_TOSTARTUP);
    }


    /**
     * 批量启动测评对象
     */
    private void startUpBatch() {
        ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
        Set<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toSet());
        Map<String, Object> params = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        params.put(AppflgConstant.ACTIVITY_ID, getView().getFormShowParameter().getCustomParam("assessActId"));
        params.put(SUCCESS_LIST, selectedIds.stream().collect(Collectors.toList()));
        ActivityDispatchUtils.dispatch(this, params, ActivityStartUpActObjTask.class.getName(), "启动测评对象", true, AppflgConstant.CEA_ASSOBJ_TOSTARTUP);
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (AppflgConstant.CEA_ASSOBJ_TOSTARTUP.equals(closedCallBackEvent.getActionId())) {
            this.getView().invokeOperation("refresh");
            AssessManageUtils.refreshAssessObjCount(this.getView(), getView().getFormShowParameter().getCustomParam("assessActId"));
        } else if (CEA_DIMSETTINGSELECT.equals(closedCallBackEvent.getActionId())) {
            this.getView().invokeOperation("refresh");
            AssessManageUtils.refreshAssessObjCount(this.getView(), getView().getFormShowParameter().getCustomParam("assessActId"));
        } else if (CEA_DIMASSESSERSETLIST.equals(closedCallBackEvent.getActionId())) {
            this.getView().invokeOperation("refresh");
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        AbstractOperate source = (AbstractOperate) args.getSource();
        OperateOption option = source.getOption();
        String operateKey = source.getOperateKey();
        switch (operateKey) {
            case "importdata_hr":
            case "export_from_impttpl_hr":
                if (AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))) {
                    // 测评活动已结束，该功能不可用
                    getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用", "finished_valid", KEY_APP_NAME));
                    args.setCancel(true);
                }
                break;
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (!ObjectUtils.isEmpty(afterDoOperationEventArgs.getOperationResult())
                && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            String operateKey = afterDoOperationEventArgs.getOperateKey();
            switch (operateKey) {
                case OP_KEY_ASSESSOBJSTARTUP:
                    if (AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))) {
                        // 测评活动已结束，该功能不可用
                        getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用", "finished_valid", KEY_APP_NAME));
                        return;
                    }
                    // 启动测评对象
                    startUpBatch();
                    break;
                case OP_KEY_DELETEASSOBJ:
                    if (AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))) {
                        // 测评活动已结束，该功能不可用
                        getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用", "finished_valid", KEY_APP_NAME));
                        return;
                    }
                    deleteAssessObjBatch();
                    break;
                case "import_assessformfix":
                    if (AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))) {
                        // 测评活动已结束，该功能不可用
                        getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用", "finished_valid", KEY_APP_NAME));
                        return;
                    }
                    BaseShowParameter formShowParameter = new BaseShowParameter();
                    formShowParameter.setFormId("cea_assessforimport");
                    formShowParameter.setCustomParam("assessActId", this.getView().getFormShowParameter().getCustomParam("assessActId"));
                    formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                    formShowParameter.setHasRight(Boolean.TRUE);
                    formShowParameter.setStatus(OperationStatus.ADDNEW);
                    this.getView().showForm(formShowParameter);
                    break;
                case "setassesser":
                    if (AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))) {
                        // 测评活动已结束，该功能不可用
                        getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用", "finished_valid", KEY_APP_NAME));
                        return;
                    }
                    ListSelectedRowCollection selectedRows = getSelectedRows();   //获取选中的数据
                    Set<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toSet());
                    showSetAssesserListEdit(selectedIds.stream().collect(Collectors.toList()));
                    break;
            }
        }
    }


    /**
     * show出设置测评人可编辑列表
     */
    private void showSetAssesserListEdit(List<Long> assesserObjIds) {
        FormShowParameter showParameter = new FormShowParameter();
        // 显示类型，设置为在容器中显示
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setStatus(OperationStatus.EDIT);
        // 设置列表风格
        showParameter.setFormId(CEA_DIMASSESSERSETLIST);
        showParameter.setCustomParam(KEY_ASSESSEROBJIDS, assesserObjIds);
        showParameter.setCustomParam(KEY_BUTTON, KEY_ASSESSERUPDATE);
        showParameter.setCustomParam("fromWhere", "toStartUp");
        showParameter.setHasRight(Boolean.TRUE);
        StyleCss styleCss = new StyleCss();
        styleCss.setWidth("120vh");
        styleCss.setHeight("70vh");
        showParameter.getOpenStyle().setInlineStyleCss(styleCss);
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_DIMASSESSERSETLIST));
        // 显示表单
        this.getView().showForm(showParameter);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        if (HRStringUtils.equals(ADD, evt.getItemKey()) || HRStringUtils.equals(VIEW_ASSESSFORM, evt.getItemKey()) || HRStringUtils.equals(UPLOADJOBREPORT, evt.getItemKey())) {
            if (AssessManageUtils.isAssessActivityFinished(this.getView().getFormShowParameter().getCustomParam("assessActId"))) {
                // 测评活动已结束，该功能不可用
                getView().showTipNotification(ResManager.loadKDString("测评活动已结束，该功能不可用", "finished_valid", KEY_APP_NAME));
                return;
            }
            // show出选择分组的弹窗
            if (HRStringUtils.equals(ADD, evt.getItemKey())) {
                showSelectDimSetting();
            }
            // show出预览测评表的弹窗
            if (HRStringUtils.equals(VIEW_ASSESSFORM, evt.getItemKey())) {
                shoViewassessform();
            }
            // show出上传测评对象的述职报告的弹窗
            if (HRStringUtils.equals(UPLOADJOBREPORT, evt.getItemKey())) {
                if (getSelectedRows().size() > 0) {
                    showUploadjobreport();
                } else {
                    getView().showTipNotification(ResManager.loadKDString("请选择要执行的数据。", "tostartuplistplugin_0", KEY_APP_NAME));
                    return;
                }
            }
        }
    }

    /**
     * show出预览测评表
     */
    public void shoViewassessform() {
        FormShowParameter showParameter = new FormShowParameter();
        // 显示类型，设置为在容器中显示
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setStatus(OperationStatus.EDIT);
        // 设置列表风格
        showParameter.setFormId(CEA_EVALUATION_PREVIEW);
        showParameter.setHasRight(Boolean.TRUE);
        StyleCss styleCss = new StyleCss();
        styleCss.setWidth("80vh");
        styleCss.setHeight("50vh");
        showParameter.setCustomParam("assessActId", getView().getFormShowParameter().getCustomParam("assessActId"));
        showParameter.getOpenStyle().setInlineStyleCss(styleCss);
        // 显示表单
        this.getView().showForm(showParameter);

    }

}
