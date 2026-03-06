package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

public class SystemParamEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper("bos_customparam");

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    public static SystemParamEntityService getInstance() {
        return ServiceFactory.getService(SystemParamEntityService.class);
    }

    /**
     * 根据参数key查询参数
     * @param key
     * @return
     */
    public DynamicObject querySystemParamByKey(String key){
        QFilter qFilter = new QFilter("key", QCP.equals, key);
        return queryOne("id,name,numbere,key,value", new QFilter[]{qFilter});
    }
}
