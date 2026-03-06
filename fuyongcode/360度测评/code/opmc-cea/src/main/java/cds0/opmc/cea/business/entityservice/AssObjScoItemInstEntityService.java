package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import java.util.List;

import static cds0.opmc.cea.common.AppflgConstant.*;

/**
 * 动态表单插件
 */
public class AssObjScoItemInstEntityService extends OpmcEntityService {

    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(CEA_ASSOBJSCOITEMINS);

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    public static AssObjScoItemInstEntityService getInstance() {
        return ServiceFactory.getService(AssObjScoItemInstEntityService.class);
    }

    /**
     * 删除维度测评人下指标打分实例数据
     * @param dimassesserIds
     * @return
     */
    public int deleteScoreItemInsAssObj(List<Long> dimassesserIds){
        QFilter qFilter = new QFilter("entryid", QCP.in,dimassesserIds);
        return deleteByFilter(new QFilter[]{qFilter});
    }

    public DynamicObject[] queryScoreItemInsAssobj(List<Long> assObjIds){
        QFilter qFilter = new QFilter("assessobj.id", QCP.in, assObjIds);
        return query("id,entryid", new QFilter[]{qFilter});
    }

    public DynamicObject[] queryScoreItemInsByDimer(Long dimassesserIds) {
        QFilter qFilter = new QFilter("entryid", QCP.equals,dimassesserIds);
        return query("assessformrow , indicatorweight, scorevalue, levelscore",new QFilter[]{qFilter});
    }

    public DynamicObject[] queryScoreItemInsByObjId(Long id) {
        QFilter qFilter = new QFilter("assessobj", QCP.equals, id);
        return query("id,entryid,  assessformrow , indicatorweight, scorevalue, levelscore", new QFilter[]{qFilter});
    }

    public DynamicObject[] queryScoreItemsByObjId(Long id) {
        QFilter qFilter = new QFilter("assessobj", QCP.equals, id);
        return query("levelentry,levelscore,indicatorweight,entryid,mapscore", qFilter.toArray());
    }

    public DynamicObject[] queryByAssessFormRowIdAndDimAssessorIds(Long assessFormRowId, List<Long> dimAssessorIds) {
        return query("id,assessobj,assessobj.person.id,scorevalue,levelentry", new QFilter[]{
                new QFilter("assessformrow", QCP.equals, assessFormRowId),
                new QFilter("entryid", QCP.in, dimAssessorIds)
        }, "entryid desc");
    }
}