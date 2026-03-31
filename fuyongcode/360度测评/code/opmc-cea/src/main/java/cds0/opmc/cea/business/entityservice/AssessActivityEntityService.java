package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.opmc.pbs.business.domain.OpmcEntityService;
import java.util.List;

import static cds0.opmc.cea.common.AppflgConstant.*;

public class AssessActivityEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(CEA_ASSESSACTIVITY);

    public static AssessActivityEntityService getInstance() {
        return ServiceFactory.getService(AssessActivityEntityService.class);
    }
    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    /**
     * 查询制定测评活动
     * @param assessActId
     * @return
     */
    public DynamicObject queryAssessActivityByPk(Long assessActId){
        return loadSingle(assessActId);
    }

    /**
     * 根据测评活动ID查询测评活动列表
     * @param assessActIds
     * @return
     */
    public DynamicObject[] queryAssessActivityArrayByPk(List<Long> assessActIds){
        QFilter qFilter = new QFilter("id", QCP.in, assessActIds);
        return query("id,activitystatus,entryentity,entryentity.id,overtime", new QFilter[]{qFilter});
    }

    /**
     * 删除指定Pk的测评活动
     * @return
     */
    public void deleteByActivityPks(List<Long> assessActIds){
        delete(assessActIds.toArray());
    }

    /**
     * 根据活动ID查询预览测评表相关数据
     * @param assessActId
     * @return
     */
    public DynamicObject queryActivityAndDimNameById(Long assessActId){
        QFilter qFilter = new QFilter("id", QCP.equals, assessActId);
        return this.queryOne("id,entryentity,entryentity.assessform,entryentity.groupdimname", new QFilter[]{qFilter});
    }

}
