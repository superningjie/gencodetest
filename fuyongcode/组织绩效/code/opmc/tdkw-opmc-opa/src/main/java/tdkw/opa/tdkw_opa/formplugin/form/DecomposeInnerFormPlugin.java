package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.AfterAddRowEventArgs;
import kd.bos.entity.datamodel.events.AfterDeleteRowEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.IFormView;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.SubEntryGrid;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;
import tdkw.opa.tdkw_opa.formplugin.utils.HRRoleAndPersonUtils;

import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pjj
 * @date 2024-08-26
 * 分解单显示容器插件
 */
public class DecomposeInnerFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //设置内嵌页面状态与父页面一致
        this.getView().getFormShowParameter().setStatus(this.getView().getParentView().getFormShowParameter().getStatus());
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        //指标库数据
        Long targetId = (Long) customParams.get("targetId");
        DynamicObject target = BusinessDataServiceHelper.loadSingle("tdkw_perf_metrics", new QFilter[]{new QFilter("id", QCP.equals, targetId)});
        int seq = (int) customParams.get("seq");
        this.getModel().setValue("tdkw_seq", seq);
        //父页面指标名称描述 评分标准
        this.getModel().setValue("tdkw_textfield21", customParams.get("targetName"));
        this.getModel().setValue("tdkw_textfield1", customParams.get("desc"));
        this.getModel().setValue("tdkw_textfield2", customParams.get("rating"));
        JSONArray rows = (JSONArray) customParams.get("rows");
        String status = (String) customParams.get("status");
        if (ObjectUtils.isEmpty(target)) {
            this.getModel().setValue("tdkw_target", target);
        }
        //基础资料无法直接赋值id 重查赋值dynamicobject
        Object value = this.getView().getParentView().getModel().getValue("id");
        DynamicObject loadSingle = BusinessDataServiceHelper.loadSingle("tdkw_target_decompose", new QFilter[]{new QFilter("id", QCP.equals, value)});
        if (!ObjectUtils.isEmpty(loadSingle)) {
            DynamicObjectCollection tdkwEntryentity = loadSingle.getDynamicObjectCollection("tdkw_entryentity");
            Map<Long, DynamicObject> map = tdkwEntryentity.stream().flatMap(i -> i.getDynamicObjectCollection("tdkw_subentryentity").stream()).map(j -> j.getDynamicObject("tdkw_bedecomposeadmin")).collect(Collectors.toMap(i -> i.getLong("id"), i -> i, ((existing, replacement) -> {
                return existing;
            })));
            //如果父页面为浏览和编辑状态则有数据传过来 如果为新增状态 则数据为空不渲染
            if (StringUtils.equals(status, "VIEW") || StringUtils.equals(status, "EDIT")) {
                if (!rows.isEmpty()) {
                    this.getModel().batchCreateNewEntryRow("tdkw_entryentity", rows.size());
                }
                for (int i = 0; i < rows.size(); i++) {
                    JSONObject jsonObject = rows.getJSONObject(i);
                    this.getModel().setValue("tdkw_basedatafield", map.get(jsonObject.getJSONObject("tdkw_bedecomposeadmin").getLong("id")), i);
                    this.getModel().setValue("tdkw_textfield", jsonObject.getString("tdkw_decomposedesc"), i);
                    this.getModel().setValue("tdkw_checkboxfield", jsonObject.getBoolean("tdkw_isshow"), i);
                }
            }
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        IFormView parentView = this.getView().getParentView();
        int seq = (int) this.getModel().getValue("tdkw_seq");
        IDataModel parentModel = parentView.getModel();
        int rowIndex = e.getChangeSet()[0].getRowIndex();
        Object newValue = e.getChangeSet()[0].getNewValue();

        //内嵌页面修改数据 同步修改父页面子单据体数据
        switch (name) {
            case "tdkw_basedatafield":
                parentModel.setValue("tdkw_bedecomposeadmin", newValue, rowIndex, seq);
                break;
            case "tdkw_textfield":
                parentModel.setValue("tdkw_decomposedesc", newValue, rowIndex, seq);
                break;
            case "tdkw_checkboxfield":
                parentModel.setValue("tdkw_isshow", newValue, rowIndex, seq);
                break;
            default:
                break;
        }
        this.getView().sendFormAction(parentView);
    }

    @Override
    public void afterDeleteRow(AfterDeleteRowEventArgs e) {
        super.afterDeleteRow(e);
        // 删除行之后，触发此事件；
        int seq = (int) this.getModel().getValue("tdkw_seq");
        int[] rowIndexs = e.getRowIndexs();
        IFormView parentView = this.getView().getParentView();
        IDataModel parentModel = parentView.getModel();
        SubEntryGrid parentEntryGrid = parentView.getControl("tdkw_subentryentity");
        parentEntryGrid.selectRows(rowIndexs, seq);
        this.getView().sendFormAction(parentView);
        parentModel.setEntryCurrentRowIndex("tdkw_entryentity", seq);
        this.getView().sendFormAction(parentView);
        parentView.invokeOperation("deleteentry");
        this.getView().sendFormAction(parentView);
    }

    @Override
    public void afterAddRow(AfterAddRowEventArgs e) {
        super.afterAddRow(e);
        int length = e.getRowDataEntities().length;
        int seq = (int) this.getModel().getValue("tdkw_seq");

        for (int i = 0; i < length; i++) {
            IFormView parentView = this.getView().getParentView();
            IDataModel parentModel = parentView.getModel();
            EntryGrid parentEntryGrid = parentView.getControl("tdkw_entryentity");
            parentEntryGrid.selectRows(seq);
            this.getView().sendFormAction(parentView);
            parentModel.setEntryCurrentRowIndex("tdkw_entryentity", seq);
            this.getView().sendFormAction(parentView);
            parentView.invokeOperation("newentrysub");
            this.getView().sendFormAction(parentView);
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        String operateKey = afterDoOperationEventArgs.getOperateKey();
        if (afterDoOperationEventArgs.getOperationResult() != null && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            if (StringUtils.equals(operateKey, "delall")) {
                //删除所有数据
                this.getModel().deleteEntryData("tdkw_entryentity");
                //刷新单据体
                this.getView().updateView("tdkw_entryentity");

                IFormView parentView = this.getView().getParentView();
                IDataModel model = parentView.getModel();
                int seq = (int) this.getModel().getValue("tdkw_seq");
                model.setEntryCurrentRowIndex("tdkw_entryentity", seq);
                parentView.invokeOperation("delall");
                this.getView().sendFormAction(parentView);

            }
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit fieldEdit = this.getView().getControl("tdkw_basedatafield");
        fieldEdit.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        String fieldKey = event.getProperty().getName();
        if (StringUtils.equals(fieldKey, "tdkw_basedatafield")) {
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
            List<Long> collect = entryEntity.stream().map(i -> i.getLong("tdkw_basedatafield.id")).collect(Collectors.toList());
            event.addCustomQFilter(new QFilter("id", QCP.not_in, collect));
            long currUserId = RequestContext.get().getCurrUserId();
            AuthorizedOrgResult orgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, EntityName.BILL_ORG_PERF_METRICS, "tdkw_adminorg");
            boolean hasAllOrgPerm = orgSet.isHasAllOrgPerm();
            if (!hasAllOrgPerm) {
                List<Long> hasPermOrgs = orgSet.getHasPermOrgs();
                QFilter qFilter = new QFilter("id", QCP.in, hasPermOrgs);
                event.addCustomQFilter(qFilter);
            }
        }
    }
}
