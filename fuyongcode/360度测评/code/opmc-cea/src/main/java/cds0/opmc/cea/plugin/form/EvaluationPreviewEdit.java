package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.*;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import java.util.*;


import static cds0.opmc.cea.common.AppflgConstant.CEA_ASSESSPREVIEW;


/**
 * 预览测评表插件
 *
 * @author sxf
 * @date 2024-07-12 11:45:38
 */
public class EvaluationPreviewEdit extends AbstractBillPlugIn implements BeforeF7SelectListener {
    private static final String KEY_ENTITY = "cds0_entryentity";
    private static final String KEY_GROUP_NUMBER = "cds0_groupnumber";
    private static final String ASSESS_ACT_ID = "assessActId";
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {

    }

    @Override
    public void afterCreateNewData(EventObject e) {
        Long assessActId = this.getView().getParentView().getFormShowParameter().getCustomParam(ASSESS_ACT_ID);
        DynamicObject assessActs = ASSESS_ACTIVITY_ENTITY_SERVICE.queryActivityAndDimNameById(assessActId);
        this.getView().getPageCache().put(ASSESS_ACT_ID, assessActId.toString());
        DynamicObjectCollection entryentity = (DynamicObjectCollection) assessActs.get("entryentity");
        this.setValue(entryentity);
    }

    @Override
    public void registerListener(EventObject e) {
    }

    private void setValue(DynamicObjectCollection entryentity) {
        Integer size = entryentity.size();
        for (DynamicObject dynamicObject : entryentity) {
            if (dynamicObject.get("assessform") == null) {
                size--;
            }
        }
        if (size != null && size > 0) {
            this.getModel().batchCreateNewEntryRow(KEY_ENTITY, size);
            //赋值
            for (int i = 0; i < entryentity.size(); i++) {
                DynamicObject assessform = (DynamicObject) entryentity.get(i).get("assessform");
                if (assessform != null) {
                    DynamicObject dimgroup = (DynamicObject) assessform.get("dimgroup");
                    //获取分组数据
                    this.getModel().setValue("cds0_name", entryentity.get(i).get("groupdimname"), i);
                    this.getModel().setValue(KEY_GROUP_NUMBER, dimgroup.getPkValue(), i);
                }
            }
        }
    }

    //点击预览
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        EntryGrid entryentity = this.getControl(KEY_ENTITY);
        int[] selectRows = entryentity.getSelectRows();
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setStatus(OperationStatus.EDIT);
        showParameter.setFormId(CEA_ASSESSPREVIEW);
        showParameter.setCustomParam("assessActivityId", Long.parseLong(this.getView().getPageCache().get(ASSESS_ACT_ID)));
        showParameter.setCustomParam("dimGroupId", this.getModel().getValue(KEY_GROUP_NUMBER, selectRows[0]));
        showParameter.setCloseCallBack(new CloseCallBack(this, CEA_ASSESSPREVIEW));
        // 显示表单
        this.getView().showForm(showParameter);
    }

}
