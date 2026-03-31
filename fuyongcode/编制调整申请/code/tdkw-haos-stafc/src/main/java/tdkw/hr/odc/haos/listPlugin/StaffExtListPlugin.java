package tdkw.hr.odc.haos.listPlugin;

import com.google.common.collect.Sets;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.AbstractOperate;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : xiekun
 * @description : 编制信息维护列表扩展插件.
 * @date : 2024-05-16 14:45
 **/
public class StaffExtListPlugin extends AbstractListPlugin {

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        AbstractOperate operate = (AbstractOperate) args.getSource();
        String operateKey = operate.getOperateKey();
        if ("donothing_modify".equalsIgnoreCase(operateKey) || "enable".equalsIgnoreCase(operateKey)) {
            ListSelectedRowCollection selectedRows = this.getSelectedRows();
            Set<Long> ids = Sets.newHashSetWithExpectedSize(selectedRows.size());
            for (ListSelectedRow row : selectedRows) {
                String idStr = String.valueOf(row.getPrimaryKeyValue());
                ids.add(Long.valueOf(idStr));
            }
            DynamicObjectCollection staffColl =
                    QueryServiceHelper.query("haos_staff", "id,tdkw_staffflowstatus",
                            new QFilter[]{new QFilter("id", QCP.in, ids)});
            Set<String> statusSet =
                    staffColl.stream().map(staff -> staff.getString("tdkw_staffflowstatus")).collect(Collectors.toSet());
            if (statusSet.contains("B") || statusSet.contains("D")) {
                this.getView().showTipNotification("数据已在流程中，不允许操作!");
                args.setCancel(true);
            }
        }
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String itemKey = evt.getItemKey();
        ListSelectedRowCollection selectedRows = this.getSelectedRows();
        if (StringUtils.equals("tdkw_tj", itemKey) || StringUtils.equals("tdkw_viewflowchart", itemKey)
                || StringUtils.equals("tblmodify", itemKey) || StringUtils.equals("baritemap", itemKey)
                || StringUtils.equals("tdkw_doedit", itemKey)
        ) {
            if (selectedRows.size() != 1) {
                this.getView().showTipNotification("请只选择一条数据!");
                evt.setCancel(true);
            } else {
                Object entryPrimaryKeyValue = selectedRows.get(0).getPrimaryKeyValue();
                DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("haos_staff", "tdkw_staffid,tdkw_staffflowstatus", new QFilter[]{
                        new QFilter("id", QCP.equals, entryPrimaryKeyValue)
                });
                String tdkw_staffflowstatus = dynamicObject.getString("tdkw_staffflowstatus");
                if (StringUtils.equals("tdkw_tj", itemKey)) {//提交
                    if (!tdkw_staffflowstatus.equals("")) {
                        if (tdkw_staffflowstatus.equals("D") || tdkw_staffflowstatus.equals("B")) {
                            this.getView().showTipNotification("单据在流程中,不允许操作!");
                            evt.setCancel(true);
                        } else if (tdkw_staffflowstatus.equals("C")) {
                            this.getView().showTipNotification("单据已审批通过,不允许操作!");
                            evt.setCancel(true);
                        }
                    }
                } else if (StringUtils.equals("tdkw_viewflowchart", itemKey)) {//查看流程图

                    Long tdkw_staffid = dynamicObject.getLong("tdkw_staffid");
                    WorkflowServiceHelper.viewFlowchart(this.getView().getPageId(), tdkw_staffid);
                } else if (StringUtils.equals("tdkw_doedit", itemKey) || StringUtils.equals("baritemap", itemKey)) {//变更启用
                    if (!tdkw_staffflowstatus.equals("")) {
                        if (tdkw_staffflowstatus.equals("D") || tdkw_staffflowstatus.equals("B")) {
                            this.getView().showTipNotification("单据在流程中,不允许操作!");
                            evt.setCancel(true);
                        } else {
                            OperateOption option = OperateOption.create();
                            option.setVariableValue(OperateOptionConst.ISHASRIGHT, String.valueOf(true));
                            this.getView().invokeOperation("donothing_modify", option);
                        }
                    } else {
                        OperateOption option = OperateOption.create();
                        option.setVariableValue(OperateOptionConst.ISHASRIGHT, String.valueOf(true));
                        this.getView().invokeOperation("donothing_modify", option);
                    }
                } else if (StringUtils.equals("tblmodify", itemKey)) {
                    if (tdkw_staffflowstatus.equals("C")) {
                        this.getView().showTipNotification("单据已审批通过,不允许操作!");
                        evt.setCancel(true);
                    } else if (tdkw_staffflowstatus.equals("D") || tdkw_staffflowstatus.equals("B")) {
                        this.getView().showTipNotification("单据在流程中,不允许操作!");
                        evt.setCancel(true);
                    }
                }
            }
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if (StringUtils.equals("tdkw_tj", afterDoOperationEventArgs.getOperateKey())) {
            OperationResult opResult = afterDoOperationEventArgs.getOperationResult();
            if (opResult != null && opResult.isSuccess()) {
                DeleteServiceHelper.delete("tdkw_staffflow", new QFilter[]{
                        new QFilter("name", QCP.equals, this.getSelectedRows().get(0).getName()),
                        new QFilter("number", QCP.equals, this.getSelectedRows().get(0).getNumber())
                });

                Object pkValue = this.getSelectedRows().get(0).getPrimaryKeyValue();
                DynamicObject staffFlowDyn = BusinessDataServiceHelper.loadSingle(pkValue,"tdkw_staffflow");

                OperateOption operateOption = OperateOption.create();
                OperationResult operationResult= OperationServiceHelper.executeOperate("submit", "tdkw_staffflow",
                        new DynamicObject[] {staffFlowDyn},operateOption );
                if (operationResult.isSuccess()) {
                    Object pkId = operationResult.getSuccessPkIds().get(0);
                    DynamicObject dataEntity = BusinessDataServiceHelper.loadSingle("haos_staff", "tdkw_staffid,tdkw_staffflowstatus", new QFilter[]{
                            new QFilter("id", QCP.equals, pkValue)
                    });
                    //表单赋值
                    dataEntity.set("tdkw_staffid", pkId);
                    dataEntity.set("tdkw_staffflowstatus", staffFlowDyn.get("status"));
                    SaveServiceHelper.save(new DynamicObject[]{dataEntity});
                } else {
                    this.getView().showMessage(operationResult.getAllErrorOrValidateInfo().get(0).getMessage());
                }
            }
        }
    }

}
