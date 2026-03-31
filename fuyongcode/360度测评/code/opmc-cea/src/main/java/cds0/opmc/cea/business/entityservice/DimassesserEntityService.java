package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import java.util.List;

import static cds0.opmc.cea.common.AppflgConstant.CEA_DIMASSESSER;

public class DimassesserEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper dimassesserService = new HRBaseServiceHelper(CEA_DIMASSESSER);
    public static DimassesserEntityService getInstance() {
        return ServiceFactory.getService(DimassesserEntityService.class);
    }
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return dimassesserService;
    }

    public DynamicObject[] queryEvalDims(Long evalDimId){
        QFilter qFilter = new QFilter("evaldim.id", QCP.equals, evalDimId);
        return query("id,dimweight",new QFilter[]{qFilter});
    }

    public DynamicObject[] queryEvalDims(List<Long> evalDimIds){
        QFilter qFilter = new QFilter("evaldim.id", QCP.in, evalDimIds);
        return query("id,dimweight",new QFilter[]{qFilter});
    }

    public DynamicObject[] queryDimweight(Long evalDim){
        QFilter filter = new QFilter("evaldim", QCP.equals, evalDim);
        return super.query(new QFilter[]{filter});
    }
    /**
     * 根据测评人id和维度删除数据
     */
    public void delByAssesserAndEvalDim(Long assesser,List<Long> evalDims){
        // 先查再删
        int index = 0;
        Object[] pks = new Object[evalDims.size()];
        for(Long evalDim : evalDims){
            QFilter filter = new QFilter("assesser", QCP.equals, assesser).and("evaldim",QCP.equals,evalDim);
            DynamicObject dimweight = super.queryOne(new QFilter[]{filter});
            pks[index] = dimweight.get("id");
            index++;
        }
        super.delete(pks);
    }
    /**
     * 根据测id查数据
     */
    public DynamicObject[] querybyIds(List<Long> dimassesserIds){
        QFilter qFilter = new QFilter("id", QCP.in, dimassesserIds);
        return query("id,dimweight",new QFilter[]{qFilter});
    }

    /**
     * 根据id查出状态为”未填报“的数据
     */
    public DynamicObject[] querydatabyIds(List<Long> dimassesserIds,String dimAssesserStatus) {
        QFilter qFilter = new QFilter("id", QCP.in, dimassesserIds).and("assesstatus",QCP.equals,dimAssesserStatus);
        return super.query("id, assesstatus", new QFilter[]{qFilter});
    }

    /**
     * 调整测评人时，根据 测评对象、测评人、维度 查询对应的维度测评人数据
     * @param addNewDimAssesserObjList
     * @param assesserIdList
     * @param evalDimIdList
     * @return
     */
    public DynamicObject[] queryDimAssesserForAdjust(List<Long> addNewDimAssesserObjList, List<Long> assesserIdList, List<Long> evalDimIdList){
        QFilter qFilter = new QFilter("assobj.id", QCP.in, addNewDimAssesserObjList).
                and("assesser.id", QCP.in, assesserIdList).
                and("evaldim.id", QCP.in, evalDimIdList);
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }

    /**
     * 查询指定测评对象在某个维度上的维度测评人
     * @param assobjIds
     * @param evalDimId
     * @return
     */
    public DynamicObject[] queryDimAssesserAssobjEvalDim(List<Long> assobjIds, Long evalDimId){
        // and("assesser.id", QCP.not_equals, 0L)
        QFilter qFilter = new QFilter("assobj.id", QCP.in, assobjIds).
                and("evaldim.id", QCP.equals, evalDimId);
        return queryOriginalArray("id,assobj.id", new QFilter[]{qFilter});
    }

    public DynamicObject[] queryDimAsserAssObjEvalDims(List<Long> assobjIds, List<Long> evalDimIds){
        QFilter qFilter = new QFilter("assobj.id", QCP.in, assobjIds).
                and("evaldim.id", QCP.in, evalDimIds);
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }

    /**
     * 查询指定测评对象的维度测评人
     * @param assobjIds
     * @return
     */
    public DynamicObject[] queryDimAsserAssObj(List<Long> assobjIds){
        QFilter qFilter = new QFilter("assobj.id", QCP.in, assobjIds);
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }

    /**
     * 查询指定分组的维度测评人
     * @param dimsettingId
     * @return
     */
    public DynamicObject[] queryDimAsserDimGroup(Long dimsettingId){
        QFilter qFilter = new QFilter("groupdim.id", QCP.equals, dimsettingId);
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }

    /**
     * 根据测id查数据
     */
    public DynamicObject[] queryByObjId(Long objId){
        QFilter qFilter = new QFilter("assobj", QCP.equals, objId);
        return query("id,dimweight",new QFilter[]{qFilter});
    }

    /**
     * 根据测id查数据
     */
    public DynamicObject[] queryEvaldimByIds(List<Long> dimassesserIds){
        QFilter qFilter = new QFilter("id", QCP.in, dimassesserIds);
        return query("id,dimweight,evaldim",new QFilter[]{qFilter});
    }

    public DynamicObject[] queryByIds(List<Long> ids){
        return query(new QFilter[]{
                new QFilter("id", QCP.in, ids)
        });
    }
}
