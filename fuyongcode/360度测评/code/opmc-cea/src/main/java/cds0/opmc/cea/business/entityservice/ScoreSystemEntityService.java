package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import static cds0.opmc.cea.common.AppflgConstant.PERFLEVEL;
import static cds0.opmc.cea.common.AppflgConstant.SCORE_SYSTEM;

public class ScoreSystemEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper ScoreSystemEntityServiceHelper = new HRBaseServiceHelper(SCORE_SYSTEM);

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return ScoreSystemEntityServiceHelper;
    }

    public static ScoreSystemEntityService getInstance() {
        return ServiceFactory.getService(ScoreSystemEntityService.class);
    }


}
