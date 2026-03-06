package cds0.opmc.cea.common;

import cds0.opmc.cea.business.entityservice.AssessActivityEntityService;
import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.entityservice.SystemParamEntityService;
import cds0.opmc.cea.business.service.AssessObjDomainService;
import cds0.opmc.cea.common.enums.AssessActivityStatusEnum;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.form.IFormView;
import kd.bos.form.container.TabPage;
import kd.hr.hbp.common.util.HRStringUtils;

import java.math.BigDecimal;

public class AssessManageUtils {
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();
    private static final SystemParamEntityService SYSTEM_PARAM_ENTITY_SERVICE = SystemParamEntityService.getInstance();
    private static final AssessObjDomainService ASSESS_OBJ_DOMAIN_SERVICE = AssessObjDomainService.getInstance();
    public static void refreshAssessObjCount(IFormView iFormView, Long assessActId) {
        IFormView parentView = iFormView.getParentView();
        int toStartUpCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.TOASSESS.getValue());
        int assessIngCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.ASSESSINGIN.getValue());
        int processedCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.PROCESSED.getValue());
        if (!ObjectUtils.isEmpty(parentView)) {
            TabPage tostartupAp = parentView.getControl("tostartup");
            TabPage assessIngAp = parentView.getControl("assessing");
            TabPage finishedAp = parentView.getControl("finished");
            tostartupAp.setText(new LocaleString(String.format(ResManager.loadKDString("待启动（%s）", "ProcessExecuteManageEditPlugin_0", AppflgConstant.KEY_APP_NAME),String.valueOf(toStartUpCount))));
            assessIngAp.setText(new LocaleString(String.format(ResManager.loadKDString("测评中（%s）", "ProcessExecuteManageEditPlugin_1", AppflgConstant.KEY_APP_NAME),String.valueOf(assessIngCount))));
            finishedAp.setText(new LocaleString(String.format(ResManager.loadKDString("已完成（%s）", "ProcessExecuteManageEditPlugin_2", AppflgConstant.KEY_APP_NAME),String.valueOf(processedCount))));
            iFormView.sendFormAction(parentView);
        }
    }

    public static boolean isAssessActivityFinished(Long activityId){
        DynamicObject assessAct = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(activityId);
        return HRStringUtils.equals(AssessActivityStatusEnum.FINISHED.getValue(), assessAct.getString("activitystatus")) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 测评对象算分
     * @param assessobjId
     * @return
     */
    public static BigDecimal compositeScore(Long assessobjId){
        DynamicObject param = SYSTEM_PARAM_ENTITY_SERVICE.querySystemParamByKey("CEA_SCORECAL");
        return ASSESS_OBJ_DOMAIN_SERVICE.compositeScore(assessobjId, param.getString("value"));
    }
}
