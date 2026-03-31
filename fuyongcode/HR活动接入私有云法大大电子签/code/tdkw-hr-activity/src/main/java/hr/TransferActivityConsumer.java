package hr;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mq.MessageAcker;
import kd.bos.mq.MessageConsumer;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.util.HRJSONUtils;
import kd.hr.hom.business.application.hrcs.IHomToHrcsAppService;


import java.util.Date;
import java.util.Map;

/**
 * @Description 调动通用活动消费类
 * @Version 1.0.0
 * @Date 2024/5/23 16:48
 * @Created by sxf
 */
public class TransferActivityConsumer implements MessageConsumer {
    private static final Log LOGGER = LogFactory.getLog(TransferActivityConsumer.class);

    //消费mq消息，生成活动
    @Override
    public void onMessage(Object message, String messageId, boolean resend, MessageAcker messageAcker) {
        Long bizBillId = 0L;
        try {
            LOGGER.info("TransferActivityConsumer.onMessage-start-message {0}",message);
            if(message instanceof Map) {
                Map<String, String> map = (Map) message;
                //生成活动单据id
                bizBillId = HRJSONUtils.getLongValOfCustomParam(map.get("bizBillId"));
                // 消息业务处理
                handleMessage(message);

            }
            // 消费确认
            messageAcker.ack(messageId);

        } catch (KDBizException e) {
            LOGGER.error("TransferActivityConsumer.onMessage-end-bizBillId {0},exception_is {1}:", bizBillId, e);
        }

    }


    public void handleMessage(Object message) {
        Map<String, Object> messageObj = HRJSONUtils.convertJSONObjectToMap(message);
        String bindBizKey = String.valueOf(messageObj.get("bindBizKey"));
        // 业务主单据id
        Long bizBillId = HRJSONUtils.getLongValOfCustomParam(messageObj.get("bizBillId"));
        // 活动id
        Long activityId = HRJSONUtils.getLongValOfCustomParam(messageObj.get("activityId"));
        // 活动实例id
        Long instanceId = HRJSONUtils.getLongValOfCustomParam(messageObj.get("instanceId"));
        // 通用活动标识
        String activityKey =  String.valueOf(messageObj.get("formid"));
        // 业务主单据标识 调动申请
        String bizBillKey = (String)messageObj.get("bizBillKey");
        // 业务主单据查询
        HRBaseServiceHelper helper = new HRBaseServiceHelper(bizBillKey);
        DynamicObject applyDys = helper.queryOne(bizBillId);
        if (applyDys != null) {

            // 1) 保存通用活动数据
            saveCommonActivityData(bizBillId, instanceId, bizBillKey,activityKey);

            // 2) 通过主单据（调动单）对象下推离职证明对象
            ActivityGenerateService service = new ActivityGenerateServiceImpl();
            service.initActivities(new DynamicObject[]{applyDys},instanceId);
            // 3)日志记录

        }
    }

    /**
     * 保存通用活动数据
     *
     * @param bizBillId bizBillId
     * @param instanceId instanceId
     * @param bizBillKey bizBillKey
     * @param activityKey activityKey
     */
    private void saveCommonActivityData(Long bizBillId, Long instanceId, String bizBillKey,String activityKey) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject(activityKey);
        //活动任务
        dynamicObject.set("activityins", instanceId);
        //主单据标识
        dynamicObject.set("bizbillnumber", bizBillKey);
        //主单据id
        dynamicObject.set("bizbillid", bizBillId.toString());
        //保存通用活动数据
        SaveServiceHelper.save(new DynamicObject[]{dynamicObject});
        /**
         * 更新活动中关联的业务单据信息
         * @param taskId        活动实例ID
         * @param bindBizBillId 关联的业务单据id（非主单据id），必填
         * @param bindBizNum    关联的业务单据编码，非必填
         */
        HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSActivityService", "updateTaskBindBillInfo", instanceId, dynamicObject.getPkValue(), "");
    }


}
