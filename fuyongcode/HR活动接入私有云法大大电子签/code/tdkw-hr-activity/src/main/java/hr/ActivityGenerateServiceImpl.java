package hr;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.util.HRObjectUtils;
import kd.hr.htm.common.utils.ObjectUtils;

import java.text.MessageFormat;
import java.util.*;

/**
 * @Description TODO
 * @Version 1.0.0
 * @Date 2024/9/5 17:08
 * @Created by sxf
 */
public class ActivityGenerateServiceImpl implements ActivityGenerateService{

    private static final Log LOGGER = LogFactory.getLog(ActivityGenerateServiceImpl.class);

    @Override
    public void initActivities(DynamicObject[] applyDys,Long activityInsId) {
        for (DynamicObject dyn:applyDys){
            TXHandle txHandle = TX.required();
            try {
                // 下推离职证明对象
                Long activityId = saveQuitCertifyDym(dyn,activityInsId);

                // 调动单据转基础资料二开字段更新
                transferBaseInfoUpdate(dyn, activityId);
                LOGGER.info("ActivityGenerateServiceImpl excute activityId:{0}",activityId);

            } catch (KDBizException var15) {
                txHandle.markRollback();
                LOGGER.error("ActivityGenerateServiceImpl excute error:{0}",var15);
            } finally {
                txHandle.close();
            }

        }
    }

    private Long saveQuitCertifyDym(DynamicObject dyn,Long activityInsId) {
        HRBaseServiceHelper helper = new HRBaseServiceHelper("tdkw_certifymange");
        DynamicObjectCollection certifyDys = new DynamicObjectCollection();
        // 获取活动方案id、二开离职证明对象
        Long activityId = initActivity(dyn, certifyDys);
        for (DynamicObject certifyDy:certifyDys){
            certifyDy.set("activityins", activityInsId);
        }
        // 保存二开离职证明实体
        helper.save(certifyDys);
        return activityId;
    }

    private void transferBaseInfoUpdate(DynamicObject dyn, Long activityId) {
        // 调动单据转基础资料
        HRBaseServiceHelper transBaseHelper = new HRBaseServiceHelper("tdkw_transferbaseinfo");
        // 主单据相关状态更新,活动方案绑定
        DynamicObject transBaseDyn = transBaseHelper.queryOne("tdkw_certifystatus,tdkw_activityplan", dyn.getPkValue());
        // 二开 证明开具状态  "0" 未开具
        transBaseDyn.set("tdkw_certifystatus","0");
        // 二开 活动方案
        transBaseDyn.set("tdkw_activityplan", activityId);
        transBaseHelper.updateOne(transBaseDyn);
    }

    private Long initActivity(DynamicObject applyDy, DynamicObjectCollection certifyDys) {
        DynamicObject activityPlan = null;

        try {
            Map<String, Object> planInfo = (Map)HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSActivityService", "getActivitySchemeFromWorkflow", new Object[]{applyDy, "id,actschemeentry.activity,actschemeentry.activity,actschemeentry.actbizobj"});
            if (planInfo != null && Boolean.TRUE.equals(planInfo.get("success"))) {
                activityPlan = (DynamicObject)planInfo.get("data");
            }
        } catch (KDBizException var8) {
            String message = MessageFormat.format("ActivityGenerateServiceImpl.initAllActivity error:{0}", applyDy.getLong("id"));
            LOGGER.error("ActivityGenerateServiceImpl.initActivity,message:{0},var8:{1}",message, var8);
        }

        return activityPlan == null ? 0L : this.generateActivity(applyDy, activityPlan, certifyDys);
    }

    public Long generateActivity(DynamicObject applyDy, DynamicObject activityPlan, DynamicObjectCollection certifyDys) {
        DynamicObjectCollection entityCol = activityPlan.getDynamicObjectCollection("actschemeentry");
        Long id = activityPlan.getLong("id");
        Long certifyId = 0L;
        Iterator col = entityCol.iterator();

        while(col.hasNext()) {
            DynamicObject dy = (DynamicObject)col.next();
            Long activityId = dy.getLong("activity.id");
            String pageNumber = this.getActivityPageNumber(dy);
            certifyId = activityId;
            if (!ObjectUtils.isEmpty(certifyId)) {
                // 添加生成离职证明活动对象
                certifyDys.add(this.generateQuitCertifyActivity(certifyId, applyDy));
            }
        }

        return id;
    }

    private String getActivityPageNumber(DynamicObject activity) {
        DynamicObject actInfoDy = (DynamicObject)activity.getDynamicObjectCollection("actinfo").get(0);
        return actInfoDy.getString("actbizobj.number");
    }

    @Override
    public DynamicObject generateQuitCertifyActivity(Long certifyId, DynamicObject applyDy) {
        HRBaseServiceHelper helper = new HRBaseServiceHelper("tdkw_certifymange");
        DynamicObject certifyDy = helper.generateEmptyDynamicObject();
        // 设置公共字段
        initActivityCommonField(certifyDy, applyDy);
        certifyDy.set("activity", certifyId);
        certifyDy.set("iseffective", Boolean.TRUE);
        return certifyDy;
    }

    /**
     * 初始化活动公共字段
     *
     * @param activityDy
     * @param applyDy
     */
    private void initActivityCommonField(DynamicObject activityDy, DynamicObject applyDy) {
        activityDy.set("creator", applyDy.getDynamicObject("creator"));
        activityDy.set("createtime", new Date());
        activityDy.set("modifier", applyDy.getDynamicObject("modifier"));
        activityDy.set("modifytime", new Date());

        // 离职人员
        activityDy.set("person", applyDy.getDynamicObject("person"));
        // 公司
        activityDy.set("cmphis", applyDy.getDynamicObject("bcompany"));
        // 部门
        activityDy.set("dephis", applyDy.getDynamicObject("borg"));
        // 岗位
        activityDy.set("poshis", applyDy.getDynamicObject("bposition"));

        // 触发日期
        activityDy.set("triggertime", new Date());
        // 调动申请单
        activityDy.set("tdkw_transferbill", applyDy);
    }

}
