package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import static cds0.opmc.cea.common.AppflgConstant.MSG_TEMPLATE;

public class MessageEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper messageEntityService = new HRBaseServiceHelper(MSG_TEMPLATE);
    public static MessageEntityService getInstance() {
        return ServiceFactory.getService(MessageEntityService.class);
    }
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return messageEntityService;
    }

}
