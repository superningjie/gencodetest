package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import java.util.ArrayList;
import java.util.List;

import static cds0.opmc.cea.common.AppflgConstant.*;
import static cds0.opmc.cea.common.AppflgConstant.EVALROLE;

public class EvalDimSettingEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(AppflgConstant.CEA_EVALDIMSETTING);

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    public static EvalDimSettingEntityService getInstance() {
        return ServiceFactory.getService(EvalDimSettingEntityService.class);
    }

    /**
     * 生成维度数据
     * @param rawEvaldims
     * @param dimsettingId
     * @return
     */
    public List<DynamicObject> generateEvalDimSetting(DynamicObjectCollection rawEvaldims, Long dimsettingId){
        List<DynamicObject> newEvaldims = new ArrayList<>(rawEvaldims.size());
        rawEvaldims.stream().forEach(evaldim ->{
            DynamicObject newEvalDim = generateEmptyDynamicObject();
            newEvalDim.set(DIMINDEX,evaldim.get(DIMINDEX));
            newEvalDim.set(DIMNAME,evaldim.get(DIMNAME));
            newEvalDim.set(NAME,evaldim.get(DIMNAME));
            newEvalDim.set(NUMBER, DB.genStringId("t_cea_evaldimsetting"));
            newEvalDim.set(DIMWEIGHT,evaldim.get(DIMWEIGHT));
            newEvalDim.set(EVALROLE,evaldim.getDynamicObject(EVALROLE).getLong(ID));
            newEvalDim.set(ENTRYID, dimsettingId);
            newEvaldims.add(newEvalDim);
        });
        return newEvaldims;
    }

    /**
     * 生成维度数据
     * @param rawEvaldims
     * @param dimsettingId
     * @return
     */
    public List<DynamicObject> generateEvalDimSetting(List<DynamicObject> rawEvaldims, Long dimsettingId){
        List<DynamicObject> newEvaldims = new ArrayList<>(rawEvaldims.size());
        rawEvaldims.stream().forEach(evaldim ->{
            DynamicObject newEvalDim = generateEmptyDynamicObject();
            newEvalDim.set(DIMINDEX,evaldim.get(DIMINDEX));
            newEvalDim.set(DIMNAME,evaldim.get(DIMNAME));
            newEvalDim.set(NAME,evaldim.get(DIMNAME));
            newEvalDim.set(NUMBER,System.currentTimeMillis());
            newEvalDim.set(DIMWEIGHT,evaldim.get(DIMWEIGHT));
            newEvalDim.set(EVALROLE,evaldim.getDynamicObject(EVALROLE).getLong(ID));
            newEvalDim.set(ENTRYID, dimsettingId);
            newEvaldims.add(newEvalDim);
        });
        return newEvaldims;
    }

    /**
     * 删除维度分组下面的维度数据
     * @param dimsettingIds
     * @return
     */
    public int deleteEvalDimByDimSettingId(List<Long> dimsettingIds){
        QFilter qFilter = new QFilter(ENTRYID, QCP.in, dimsettingIds);
        return deleteByFilter(new QFilter[]{qFilter});
    }

    /**
     * 查询分组下的维度数据
     * @param dimsettingId
     * @return
     */
    public DynamicObject[] queryEvalDimByDimSettingId(Long dimsettingId){
        QFilter qFilter = new QFilter(ENTRYID, QCP.equals, dimsettingId);
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }

    /**
     * 根据测id查数据
     */
    public DynamicObject[] queryByIds(List<Long> ids){
        QFilter qFilter = new QFilter("id", QCP.in, ids);
        return query("id,dimweight",new QFilter[]{qFilter});
    }
}
