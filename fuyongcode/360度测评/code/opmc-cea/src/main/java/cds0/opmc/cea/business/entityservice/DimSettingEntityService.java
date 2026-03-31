package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import java.util.List;

public class DimSettingEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(AppflgConstant.CEA_DIMSETTING);

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }
    public static DimSettingEntityService getInstance() {
        return ServiceFactory.getService(DimSettingEntityService.class);
    }

    /**
     * 根据分组id查询分组维度设置
     * @param ids
     * @return
     */
    public DynamicObject[] queryDimSettingByIds(List<Long> ids){
        return loadDynamicObjectArray(ids.stream().toArray());
    }

    /**
     * 根据id查询分组维度设置
     * @param id
     * @return
     */
    public DynamicObject queryDimSettingByPk(Long id){
        return loadSingle(id);
    }

    //根据id查询当前分组维度是否自设比例
    public DynamicObject queryDimensionById(Long id){
        QFilter qFilter = new QFilter("id", QCP.equals, id);
        return queryOne("id, dimension",new QFilter[]{qFilter});
    }
}
