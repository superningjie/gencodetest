package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import static cds0.opmc.cea.common.AppflgConstant.PERF_FIlE;

public class PerfFileEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper perfFileServiceHelper = new HRBaseServiceHelper(PERF_FIlE);
    public static PerfFileEntityService getInstance() {
        return ServiceFactory.getService(PerfFileEntityService.class);
    }
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return perfFileServiceHelper;
    }
}
