package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import static cds0.opmc.cea.common.AppflgConstant.ASSESS_FORM_ROW;

public class AssessFormRowEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(ASSESS_FORM_ROW);

    public static AssessFormRowEntityService getInstance() {
        return ServiceFactory.getService(AssessFormRowEntityService.class);
    }
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    public DynamicObject queryByPk(Long pk){
        return loadSingle(pk);
    }
}
