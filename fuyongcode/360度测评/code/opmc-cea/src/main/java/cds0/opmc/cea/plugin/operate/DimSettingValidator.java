package cds0.opmc.cea.plugin.operate;

import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.entity.ExtendedDataEntity;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.opplugin.validator.HRCoreBaseBillValidator;

import static cds0.opmc.cea.common.AppflgConstant.DIMWEIGHT;
import static cds0.opmc.cea.common.AppflgConstant.ENTRYENTITY;

public class DimSettingValidator extends HRCoreBaseBillValidator {
    @Override
    public void validate(){
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        if (ObjectUtils.isEmpty(dataEntities)) {
            return;
        }
        if(HRStringUtils.isEmpty(dataEntities[0].getDataEntity().getString(AppflgConstant.GROUPDIMNAME))){
            addErrorMessage(dataEntities[0],ResManager.loadKDString("名称不能为空", "DimSettingEdit_2", AppflgConstant.KEY_APP_NAME));
        }
        if(HRStringUtils.isEmpty(dataEntities[0].getDataEntity().getString(AppflgConstant.DIMENSION))){
            addErrorMessage(dataEntities[0],ResManager.loadKDString("测评维度不能为空", "DimSettingEdit_1", AppflgConstant.KEY_APP_NAME));
        }
        if(dataEntities[0].getDataEntity().getDynamicObjectCollection(ENTRYENTITY).size() == HRBaseConstants.INT_ZERO){
            addErrorMessage(dataEntities[0],ResManager.loadKDString("维度设置分录不能为空", "DimSettingEdit_3", AppflgConstant.KEY_APP_NAME));
            return;
        }
        Double sum = dataEntities[0].getDataEntity().getDynamicObjectCollection(ENTRYENTITY).stream().map(evaldim -> evaldim.getBigDecimal(DIMWEIGHT).doubleValue()).reduce(Double::sum).get();
        if(sum != Double.parseDouble("100")){
            addErrorMessage(dataEntities[0],ResManager.loadKDString("维度权重总和必须等于100%", "DimSettingEdit_0", AppflgConstant.KEY_APP_NAME));
        }
        if(dataEntities[0].getDataEntity().getDynamicObjectCollection(ENTRYENTITY).stream().filter(evaldim -> HRStringUtils.isEmpty(evaldim.getString("dimname"))).count() > 0){
            addErrorMessage(dataEntities[0],ResManager.loadKDString("维度名称为必填，不能为空", "DimSettingEdit_5", AppflgConstant.KEY_APP_NAME));
        }
        if(dataEntities[0].getDataEntity().getDynamicObjectCollection(ENTRYENTITY).stream().filter(evaldim -> evaldim.getLong("evalrole.id") == 0L).count() > 0){
            addErrorMessage(dataEntities[0],ResManager.loadKDString("测评人角色为必填，不能为空", "DimSettingEdit_6", AppflgConstant.KEY_APP_NAME));
        }
    }
}
