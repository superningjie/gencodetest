package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import java.util.List;

import static cds0.opmc.cea.common.AppflgConstant.ASSESS_FORM;

public class AssessFormEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(ASSESS_FORM);

    public static AssessFormEntityService getInstance() {
        return ServiceFactory.getService(AssessFormEntityService.class);
    }
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    /**
     * 查询指定测评表
     * @param pks
     * @return
     */
    public DynamicObject[] queryAssessFormByPks(List<Long> pks){
        return loadDynamicObjectArray(pks.toArray());
    }

    /**
     * 查询指定单个测评表
     * @param pk
     * @return
     */
    public DynamicObject queryAssessFormByPk(Long pk){
        return queryOne(pk);
    }

    /**
     * 查询测评表
     * @param assessFormId
     * @return
     */
    public DynamicObject queryOneByPk(Long assessFormId){
        return HELPER.queryOne("levelmapnumber,assformtype,entryentity,entryentity.indicatorindex", new QFilter[]{
                new QFilter("id", QCP.equals, assessFormId)
        });
    }

    public DynamicObject queryOneById(Long assessFormId){
        return HELPER.queryOne("levelmapnumber,assformtype,entryentity,entryentity.indicatorindex,entryentity.levelmap,entryentity.indicatortype", new QFilter[]{
                new QFilter("id", QCP.equals, assessFormId)
        });
    }
}
