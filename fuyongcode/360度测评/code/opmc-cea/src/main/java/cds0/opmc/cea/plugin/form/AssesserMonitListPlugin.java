package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.business.service.MessageService;
import cds0.opmc.cea.common.AssessTaskStatusEnum;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.*;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.*;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseList;

import java.util.*;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.*;

/*
 * 测评人监控列表
 */
public class AssesserMonitListPlugin extends HRDataBaseList {

    private static final Log log = LogFactory.getLog(AssesserMonitListPlugin.class);
    //”催办”按钮
    private static final String URGING = "urgentprocessing";
    //测评活动
    private static final String KEY_ASSESSACT = "assessact";
    //测评任务状态
    private static final String KEY_ASSESSTASKSTATUS = "assestaskstatus";
    //进入设置维度测评人页面的入口
    private static final String KEY_BUTTON = "buttonkey";
    private static final String KEY_ASSESSERSET = "assesserset";
    private static final String KEY_ASSESSERTASKIDS = "assessertaskids";
    private static final AssessTaskEntityService assessTaskEntityService = AssessTaskEntityService.getInstance();
    private static final MessageService messageService = MessageService.getInstance();


    @Override
    public void setFilter(SetFilterEvent evt) {
        super.setFilter(evt);

        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long activityId = parameter.getCustomParam("assessActId");
        List<QFilter> qfilters = evt.getQFilters();
        //过滤评测活动id
        QFilter filterByActivity = new QFilter(KEY_ASSESSACT, QCP.equals, activityId).
                and(KEY_ASSESSTASKSTATUS, QCP.not_in, new HashSet<String>(Arrays.asList(AssessTaskStatusEnum.EXPRIED.getValue())));
        qfilters.add(filterByActivity);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        String operateKey = afterDoOperationEventArgs.getOperateKey();
        //”调整测评人“ 按钮
        if (KEY_ASSESSERSET.equals(operateKey)) {
            ListSelectedRowCollection selectedRows = getSelectedRows();
            //获取选中的数据
            List<Long> selectedIds = selectedRows.stream().map(row -> Long.parseLong(String.valueOf(row.getPrimaryKeyValue()))).collect(Collectors.toList());
            //打开测评人设置页面
            showSetAssesserListEdit(selectedIds);
        }
        //”催办“ 按钮
        if (URGING.equals(operateKey)) {
            //需要发送催办消息的测评人id
            List<Long> urgingAssesserIds = new ArrayList<>();
            //获取当前选中数据
            List<Long> assesserIdsBySelectedRows = this.getSelectedRows().stream().map(selectedRow -> (Long) selectedRow.getPrimaryKeyValue()).collect(Collectors.toList());
            //根据当前选中的测评任务id查询出测评任务信息，取出测评人id
            DynamicObject[] assesstasks = assessTaskEntityService.queryAssessTaskByIds(assesserIdsBySelectedRows);
            //判断选中催办数据是否有不能催办的  仅测评任务状态为“待处理”可催办
            long cantUrgingCount = Arrays.stream(assesstasks).filter(obj -> !HRStringUtils.equals(AssessTaskStatusEnum.WAITTING.getValue(), obj.getString("assestaskstatus"))).count();
            if (cantUrgingCount > 0) {
                getView().showTipNotification(ResManager.loadKDString("仅支持催办待处理的测评任务", "AssesserMonitListPlugin_0", KEY_APP_NAME));
                return;
            }
            List<DynamicObject> collect = Arrays.stream(assesstasks).filter(assesstask -> HRStringUtils.equals(DimAssesserStatusEnum.UNFILLED.getValue(), assesstask.getString("dimassesser.assesstatus")) || HRStringUtils.equals(DimAssesserStatusEnum.FILLING.getValue(), assesstask.getString("dimassesser.assesstatus"))).collect(Collectors.toList());
            for (DynamicObject assesstask : collect) {
                messageService.sendAssessTaskMessageByUrging(assesstask.getLong("assesserperson.id"), assesstask.getLong("assessobj.id"));
                urgingAssesserIds.add(assesstask.getLong("assesserperson.id"));
            }
            getView().showSuccessNotification(ResManager.loadKDString("催办成功", "AssessIngListPlugin_0", KEY_APP_NAME));
        }
    }

    /**
     * show出设置测评人可编辑列表
     */
    private void showSetAssesserListEdit(List<Long> assesserTaskIds) {
        FormShowParameter showParameter = new FormShowParameter();
        // 显示类型，设置为在容器中显示
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setStatus(OperationStatus.EDIT);
        // 设置列表风格
        showParameter.setFormId(CEA_DIMASSESSERSETLIST);
        showParameter.setCustomParam(KEY_ASSESSERTASKIDS, assesserTaskIds);
        showParameter.setCustomParam(KEY_BUTTON, KEY_ASSESSERSET);
        showParameter.setCustomParam("fromWhere", "monitList");
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
        if (HRStringUtils.equals(CEA_DIMASSESSERSETLIST, actionId)) {
            getView().invokeOperation("refresh");
        }
    }

}
