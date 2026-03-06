package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import static cds0.opmc.cea.common.AppflgConstant.CEA_ASSERSEEKCONF;

public class AsserSeekConfEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(CEA_ASSERSEEKCONF);

    public static AsserSeekConfEntityService getInstance() {
        return ServiceFactory.getService(AsserSeekConfEntityService.class);
    }

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }
}
