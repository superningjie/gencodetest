package cds0.opmc.cea.business.validators;

import kd.hr.impt.common.dto.ImportBillData;
import kd.hr.impt.common.dto.ImportLog;
import kd.hr.impt.common.enu.ValidatorOrderEnum;
import kd.hr.impt.core.validate.AbstractValidateHandler;

import java.util.List;

public class DimWeightValidateHandler extends AbstractValidateHandler {
    @Override
    public ValidatorOrderEnum setValidatorRole() {
        return ValidatorOrderEnum.DEFAULT;
    }

    @Override
    public void validate(List<ImportBillData> list, ImportLog importLog) {
    }
}
