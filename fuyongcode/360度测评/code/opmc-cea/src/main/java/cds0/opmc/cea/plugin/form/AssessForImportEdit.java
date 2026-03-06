package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.service.AssessActDimSettingDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.ext.hr.metadata.edit.HisModelBasedataEdit;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.TextEdit;
import kd.bos.form.operate.AbstractOperate;
import kd.bos.form.operate.FormOperate;
import kd.hr.hbp.common.util.HRObjectUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import java.util.EventObject;

import static cds0.opmc.cea.common.AppflgConstant.ID;

public class AssessForImportEdit extends HRDataBaseEdit {
    private AssessActDimSettingDomainService ASSESS_ACT_DIMSETTING_DOMAIN_SERVICE = AssessActDimSettingDomainService.getInstance();
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        BasedataEdit groupSelect = this.getControl("groupselect");
        groupSelect.setMustInput(true);
        //HisModelBasedataEdit perfLevel = this.getControl("cds0_perflevel");
        //perfLevel.setMustInput(true);
        TextEdit name = this.getControl("name");
        name.setMustInput(true);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        AbstractOperate source = (AbstractOperate) args.getSource();
        String operateKey = source.getOperateKey();
        switch(operateKey){
            case "nextimport":
                checkMustInput(args);
                break;
            default:
                break;
        }
    }
    private void checkMustInput(BeforeDoOperationEventArgs args) {
        if (HRObjectUtils.isEmpty(this.getModel().getDataEntity().getDynamicObject("groupselect"))) {
            this.getView().showErrorNotification(ResManager
                    .loadKDString("请填写“分组”", "AssessForImportEdit_0",
                            AppflgConstant.KEY_APP_NAME));
            args.setCancel(true);
            return;
        }
        /*if (HRObjectUtils.isEmpty(this.getModel().getDataEntity().getDynamicObject("cds0_perflevel"))) {
            this.getView().showErrorNotification(ResManager
                    .loadKDString("请填写“绩效等级”", "AssessForImportEdit_1",
                            AppflgConstant.KEY_APP_NAME));
            args.setCancel(true);
            return;
        }*/
        if (HRObjectUtils.isEmpty(this.getModel().getDataEntity().getString("name"))) {
            this.getView().showErrorNotification(ResManager
                    .loadKDString("请填写“测评表名称”", "AssessForImportEdit_2",
                            AppflgConstant.KEY_APP_NAME));
            args.setCancel(true);
        }
        if(ASSESS_ACT_DIMSETTING_DOMAIN_SERVICE.hasBeingAssessObjInCurrentDimsetting(this.getModel().getDataEntity().getDynamicObject("groupselect").getLong(ID))){
            // 当前分组已存在测评中或已完成的测评对象，不允许导入测评表
            this.getView().showErrorNotification(ResManager
                    .loadKDString("当前所选分组已存在测评中或已完成的测评对象，不允许更新测评表", "AssessForImportEdit_3",
                            AppflgConstant.KEY_APP_NAME));
            args.setCancel(true);
        }
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        this.getModel().setDataChanged(false);
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        FormOperate operate = (FormOperate) args.getSource();
        String operateKey = operate.getOperateKey();
        OperationResult operationResult = args.getOperationResult();
        if (operationResult != null && operationResult.isSuccess()) {
            switch (operateKey) {
                case "nextimport":
                    this.getView().invokeOperation("importdata_hr");
                    this.getView().sendFormAction(this.getView());
                    break;
                default:
                    break;
            }
        }
    }
}
