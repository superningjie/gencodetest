package cds0.opmc.cea.business.entityservice;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.opmc.pbs.business.domain.OpmcEntityService;

import java.util.*;

import static cds0.opmc.cea.common.AppflgConstant.CEA_ASSESSOBJ;

public class AssessObjEntityService extends OpmcEntityService {
    private static final HRBaseServiceHelper HELPER = new HRBaseServiceHelper(AppflgConstant.CEA_ASSESSOBJ);

    @Override
    protected HRBaseServiceHelper getServiceHelper() {
        return HELPER;
    }

    public static AssessObjEntityService getInstance() {
        return ServiceFactory.getService(AssessObjEntityService.class);
    }

    /**
     * 查询指定分组下所有待启动的测评对象
     *
     * @param dimsettingId
     * @return
     */
    public DynamicObject[] queryToStartUpAssessObjInDimSetting(Long dimsettingId) {
        QFilter qFilter = new QFilter("dimgroup.id", QCP.equals, dimsettingId).
                and("assesstaus", QCP.equals, AssessStatusEnum.TOASSESS.getValue());
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }

    /**
     * 查询指定分组下的测评中或已完成测评对象数量
     *
     * @param dimsettingId
     * @return
     */
    public int countBeIngAssessObjInDimSetting(Long dimsettingId) {
        QFilter qFilter = new QFilter("dimgroup.id", QCP.equals, dimsettingId).
                and("assesstaus", QCP.in,
                        new HashSet<String>(Arrays.asList(AssessStatusEnum.ASSESSINGIN.getValue(), AssessStatusEnum.PROCESSED.getValue())));
        return count(AppflgConstant.CEA_ASSESSOBJ, new QFilter[]{qFilter});
    }

    public DynamicObject queryObjNameById(Object id) {
        QFilter qFilter = new QFilter(HRBaseConstants.ID, QCP.equals, id);
        return queryOne("perffile", new QFilter[]{qFilter});
    }

    /**
     * 查询测评活动下存在测评中的测评对象数量
     *
     * @param assessActId
     * @return
     */
    public int countAssessingObjInActivity(List<Long> assessActId) {
        QFilter qFilter = new QFilter("assessact.id", QCP.in, assessActId).
                and("assesstaus", QCP.equals, AssessStatusEnum.ASSESSINGIN.getValue());
        return count(AppflgConstant.CEA_ASSESSOBJ, new QFilter[]{qFilter});
    }

    /**
     * 指定测评活动下存在测评中的测评对象数量
     *
     * @param assessActId
     * @return
     */
    public int countAssessingObjInActivity(Long assessActId) {
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, assessActId).
                and("assesstaus", QCP.equals, AssessStatusEnum.ASSESSINGIN.getValue());
        return count(AppflgConstant.CEA_ASSESSOBJ, new QFilter[]{qFilter});
    }

    /**
     * 查询当前测评活动下测评对象的数量
     *
     * @param assessActId
     * @return
     */
    public int countAssessObjInCurrentActivity(Long assessActId) {
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, assessActId);
        return count(AppflgConstant.CEA_ASSESSOBJ, new QFilter[]{qFilter});
    }

    /**
     * 查询指定测评活动下的测评对象
     *
     * @param assessActId
     * @return
     */
    public DynamicObject[] queryAssessObjByActivitys(List<Long> assessActId) {
        QFilter qFilter = new QFilter("assessact.id", QCP.in, assessActId);
        return loadDynamicObjectArray(new QFilter[]{qFilter});
    }

    /**
     * 删除分组下的测评对象
     *
     * @param dimsettingIds
     * @return
     */
    public int deleteAssessObjByDimSettings(List<Long> dimsettingIds) {
        QFilter qFilter = new QFilter("dimgroup.id", QCP.in, dimsettingIds);
        return deleteByFilter(new QFilter[]{qFilter});
    }

    /**
     * 删除指定测评活动下的所有测评对象
     *
     * @param assessActId
     * @return
     */
    public int deleteAssessObjByActivitys(List<Long> assessActId) {
        QFilter qFilter = new QFilter("assessact.id", QCP.in, assessActId);
        return deleteByFilter(new QFilter[]{qFilter});
    }

    public DynamicObject queryAssessObjByPk(Long assobjId) {
        return queryOne(assobjId);
    }

    /**
     * 查询当前测评活动下的测评对象
     *
     * @param assessActId
     * @return
     */
    public DynamicObject[] queryAssessObjCurrentActivity(Long assessActId) {
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, assessActId);
        return queryOriginalArray("id,perffile.id", new QFilter[]{qFilter});
    }

    /**
     * 查询分组下的测评对象
     *
     * @param dimsettingIds
     * @return
     */
    public DynamicObject[] queryAssessObjByDimSettings(List<Long> dimsettingIds) {
        QFilter qFilter = new QFilter("dimgroup.id", QCP.in, dimsettingIds);
        return query("id,entryentity,entryentity.id", new QFilter[]{qFilter});
    }

    /**
     * 查询测评活动下某测评状态的测评对象数量
     *
     * @param assessActId
     * @param assesStatus
     * @return
     */
    public int countAssessObjActivityStatus(Long assessActId, String assesStatus) {
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, assessActId).
                and("assesstaus", QCP.equals, assesStatus);
        return count(CEA_ASSESSOBJ, new QFilter[]{qFilter});
    }

    /**
     * 查询测评活动下待启动合测评中的测评对象数量
     *
     * @param assessActId
     * @return
     */
    public int countAssessObjActivityHaving(Long assessActId) {
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, assessActId).
                and("assesstaus", QCP.in, new HashSet<>(Arrays.asList(AssessStatusEnum.TOASSESS.getValue(), AssessStatusEnum.ASSESSINGIN.getValue())));
        return count(CEA_ASSESSOBJ, new QFilter[]{qFilter});
    }

    /**
     * 生成指定测评活动下指定分组下的一批测评对象
     *
     * @param curActivityDynObj
     * @param dimgroup
     * @param assessObjectJoinInList
     * @return
     */
    public List<DynamicObject> generateAssessObj(DynamicObject curActivityDynObj, DynamicObject dimgroup, DynamicObjectCollection assessObjectJoinInList) {
        List<DynamicObject> assessObjs = new ArrayList<DynamicObject>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        assessObjectJoinInList.stream().forEach(perfFile -> {
            DynamicObject assessObjDy = generateEmptyDynamicObject();
            assessObjDy.set("enable", HRBaseConstants.INT_ONE);
            assessObjDy.set("perffile", perfFile);
            assessObjDy.set("person", perfFile.getDynamicObject("person"));
            assessObjDy.set("empposrel", perfFile.getDynamicObject("empposrel"));
            assessObjDy.set("employee", perfFile.getDynamicObject("employee"));
            assessObjDy.set("affiliateadminorg", perfFile.getDynamicObject("affiliateadminorg"));
            assessObjDy.set("perforg", curActivityDynObj.getDynamicObject("org"));
            assessObjDy.set("assessact", curActivityDynObj);
            assessObjDy.set("assesstaus", AssessStatusEnum.TOASSESS.getValue());
            assessObjDy.set("dimgroup", dimgroup);
            assessObjDy.set("addintime", new Date());
            assessObjs.add(assessObjDy);
        });
        return assessObjs;
    }

    /**
     * 查询一批测评对象
     *
     * @param pks
     * @return
     */
    public DynamicObject[] queryAssessObjByPks(List<Long> pks) {
        return loadDynamicObjectArray(pks.toArray());
    }

    /**
     * 根据测评对象id查询当前测评对象的维度测评人相关信息
     *
     * @param assesserObjIds
     * @return
     */
    public DynamicObject[] queryAssesserInfoByIds(List<Long> assesserObjIds) {
        QFilter qFilter = new QFilter("id", QCP.in, assesserObjIds);
        return query("id, perffile, perffile.id, assesstaus, overtime, calscore, modscore, perffile.name, perffile.billno, reportwork, reportwork.id dimgroup, dimgroup.id, dimgroup.dimension, entryentity, entryentity.dimweight, entryentity.assesser, entryentity.evaldim, entryentity.evaldim.dimindex, entryentity.evaldim.id, entryentity.evaldim.dimname, entryentity.evaldim.dimweight, entryentity.assesser.id, entryentity.assesser.name, entryentity.assesstatus", new QFilter[]{qFilter});
    }
}
