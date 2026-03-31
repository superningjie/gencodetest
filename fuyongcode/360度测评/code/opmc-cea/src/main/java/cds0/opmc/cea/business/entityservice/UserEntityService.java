package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

public class UserEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper("bos_user");

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    public static UserEntityService getInstance() {
        return ServiceFactory.getService(UserEntityService.class);
    }

    /**
     * 根据用户名和工号查找用户
     * @param number
     * @return
     */
    public DynamicObject queryUserByNumber(String number){
        QFilter qFilter = new QFilter("number", QCP.equals, number);
        return queryOne(new QFilter[]{qFilter});
    }
}
