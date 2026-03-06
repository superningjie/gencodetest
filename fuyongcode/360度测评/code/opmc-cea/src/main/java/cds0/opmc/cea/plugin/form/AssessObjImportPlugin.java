package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessFormEntityService;
import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.entityservice.DimassesserEntityService;
import cds0.opmc.cea.business.validators.DimWeightValidateHandler;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.impt.common.enu.ValidatorEnum;
import kd.hr.impt.common.plugin.AfterConvertDynamicObjectsEventArgs;
import kd.hr.impt.common.plugin.AfterImportCompleteArgs;
import kd.hr.impt.common.plugin.BeforeInitValidatorEventArgs;
import kd.hr.impt.common.plugin.HRImportPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.ENTRYENTITY;
import static cds0.opmc.cea.common.AppflgConstant.ID;

public class AssessObjImportPlugin implements HRImportPlugin {
    @Override
    public void beforeInitValidator(BeforeInitValidatorEventArgs args) {
        HRImportPlugin.super.beforeInitValidator(args);
        args.setValidator(ValidatorEnum.CUSTOM_VALIDATOR, new DimWeightValidateHandler());
    }

    @Override
    public void afterConvertDynamicObjects(AfterConvertDynamicObjectsEventArgs args) {
        HRImportPlugin.super.afterConvertDynamicObjects(args);
    }
    @Override
    public void afterImportComplete(AfterImportCompleteArgs args) {
        HRImportPlugin.super.afterImportComplete(args);
    }
}
