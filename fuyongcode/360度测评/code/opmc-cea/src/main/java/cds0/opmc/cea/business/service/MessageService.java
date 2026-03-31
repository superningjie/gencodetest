package cds0.opmc.cea.business.service;

import cds0.opmc.cea.business.ServiceFactory;
import cds0.opmc.cea.business.entityservice.AssessActivityEntityService;
import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.entityservice.MessageEntityService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.Tuple;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.url.UrlService;
import kd.bos.util.CollectionUtils;
import kd.bos.workflow.engine.msg.info.MessageInfo;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRDateTimeUtils;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.opmc.pbs.business.domain.msg.service.MsgSendDomainService;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.ASSESS_TASK_LIST;

public class MessageService {
    public static MessageService getInstance() {
        return ServiceFactory.getService(MessageService.class);
    }
    private static final Log LOG = LogFactory.getLog(MessageService.class);
    private static final Long ASSESS_TASK_MESSAGE_TEMPLATE_ID = 1982090803773704192L;
    private static final Long ASSESS_TASK_MESSAGE_TEMPLATE_ID_URGING = 1982091809190317056L;
    private static final MessageEntityService messageEntityService = MessageEntityService.getInstance();
    private static final MsgSendDomainService MSG_SEND_DOMAIN_SERVICE = MsgSendDomainService.getInstance();
    private static final AssessObjEntityService assessObjEntityService = AssessObjEntityService.getInstance();



    /**
     * 发送测评任务消息
     * @param toSendAssessTasks
     * @param assessObj
     */
    public void sendAssessTaskMessage(List<DynamicObject> toSendAssessTasks , DynamicObject assessObj) {
        DynamicObject msgTemplate = messageEntityService.loadDynamicObject(new QFilter[]{new QFilter(HRBaseConstants.ID, QCP.equals, ASSESS_TASK_MESSAGE_TEMPLATE_ID)});
        // 通过业务对象获取消息模板标题和内容
        Tuple<String, String> messageContextTuple = getMessageContextTuple(assessObj, msgTemplate);
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("formId", ASSESS_TASK_LIST);
        urlParams.put("type", "list");
        urlParams.put("assessActivityId", String.valueOf(assessObj.getLong("assessact.id")));
        List<Long> userIds = toSendAssessTasks.stream().map(task -> task.getLong("assesserperson")).distinct().collect(Collectors.toList());
        Map<Long,Boolean> userEnableMap = batchQueryUserIsEnable(userIds);
        List<Long> disableUser = new ArrayList<Long>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        userIds.stream().forEach(user->{
            if(userEnableMap.get(user) == null || !userEnableMap.get(user)){
                disableUser.add(user);
            }
        });
        // 去除禁用的用户
        userIds.removeAll(disableUser);
        MSG_SEND_DOMAIN_SERVICE.sendMessageWithUrl(ASSESS_TASK_MESSAGE_TEMPLATE_ID, userIds, assessObj, urlParams);
    }

    public void sendAssessTaskMessageForUgryAssessObj(List<DynamicObject> toSendAssessTasks , DynamicObject assessObj) {
        DynamicObject msgTemplate = messageEntityService.loadDynamicObject(new QFilter[]{new QFilter(HRBaseConstants.ID, QCP.equals, ASSESS_TASK_MESSAGE_TEMPLATE_ID)});
        // 通过业务对象获取消息模板标题和内容
        Tuple<String, String> messageContextTuple = getMessageContextTuple(assessObj, msgTemplate);
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("formId", ASSESS_TASK_LIST);
        urlParams.put("type", "list");
        urlParams.put("assessActivityId", String.valueOf(assessObj.getLong("assessact.id")));
        List<Long> userIds = toSendAssessTasks.stream().map(task -> task.getLong("assesserperson.id")).collect(Collectors.toList());
        Map<Long,Boolean> userEnableMap = batchQueryUserIsEnable(userIds);
        List<Long> disableUser = new ArrayList<Long>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        userIds.stream().forEach(user->{
            if(userEnableMap.get(user) == null || !userEnableMap.get(user)){
                disableUser.add(user);
            }
        });
        // 去除禁用的用户
        userIds.removeAll(disableUser);
        MSG_SEND_DOMAIN_SERVICE.sendMessageWithUrl(ASSESS_TASK_MESSAGE_TEMPLATE_ID, userIds, assessObj, urlParams);
    }

    public void sendAssessTaskMessageByUrging(Long userId, Long assessobjId) {
        DynamicObject assessObj =  assessObjEntityService.queryOne("id, perffile, name, assessact.id",new QFilter[] {
                new QFilter("id" , QCP.equals, assessobjId)
        });
        DynamicObject msgTemplate = messageEntityService.loadDynamicObject(new QFilter[]{new QFilter(HRBaseConstants.ID, QCP.equals, ASSESS_TASK_MESSAGE_TEMPLATE_ID)});
        // 通过业务对象获取消息模板标题和内容
        Tuple<String, String> messageContextTuple = getMessageContextTuple(assessObj, msgTemplate);
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("formId", ASSESS_TASK_LIST);
        urlParams.put("type", "list");
        urlParams.put("assessActivityId", String.valueOf(assessObj.getLong("assessact.id")));
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        Map<Long,Boolean> userEnableMap = batchQueryUserIsEnable(userIds);
        List<Long> disableUser = new ArrayList<Long>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        userIds.stream().forEach(user->{
            if(userEnableMap.get(user) == null || !userEnableMap.get(user)){
                disableUser.add(user);
            }
        });
        // 去除禁用的用户
        userIds.removeAll(disableUser);
        MSG_SEND_DOMAIN_SERVICE.sendMessageWithUrl(ASSESS_TASK_MESSAGE_TEMPLATE_ID_URGING, userIds, assessObj, urlParams);
    }

    /**
     * 批量查询用户是否可用,包含未禁用
     *
     * @param userIds 平台用户id
     * @return 是否可用,包含未禁用
     */
    private Map<Long, Boolean> batchQueryUserIsEnable(List<Long> userIds) {
        List<Map<String, Object>> userInfo = UserServiceHelper.get(userIds, new String[]{"id", "isforbidden","enable"}, null);
        Map<Long, Boolean> ret = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        if (CollectionUtils.isEmpty(userInfo)) {
            return ret;
        } else {
            for (Map<String, Object> user : userInfo) {
                ret.put((Long) user.get("id"), !(Boolean) user.get("isforbidden") && "1".equals(user.get("enable")));
            }
            return ret;
        }
    }



    protected void batchSendMessages(List<Long> userIds, DynamicObject businessObject, Map<String, String> urlParams,
                                     String tag, DynamicObject msgTemplate, Tuple<String, String> messageContextTuple) {
        List<MessageInfo> messageList = new ArrayList<MessageInfo>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        for (Long userId : userIds) {
            // 构造消息包，发送消息
            MessageInfo message = new MessageInfo();
            message.setType(msgTemplate.getString("msgtype"));
            message.setNotifyType(msgTemplate.getString("msgchannel"));
            message.setTitle(messageContextTuple.item1);
            message.setContent(messageContextTuple.item2);
            message.setUserIds(userIds);
            message.setSendTime(new Date());
            message.setTag(tag);
            message.setEntityNumber(msgTemplate.getString("msgentity.number"));
            urlParams.put("userId", userId.toString());
            message.setContentUrl(buildURL(urlParams));
            message.setSenderId(UserServiceHelper.getCurrentUserId());
            message.setMessageSenderName(wrapLocalStringProperty(UserServiceHelper.getCurrentUser("name").getLocaleString("name")));
            message.setBizDataId(businessObject.getLong(HRBaseConstants.ID));
            message.setNestBillId(businessObject.getLong(HRBaseConstants.ID));
            message.setNestBillno(businessObject.getString(HRBaseConstants.ID));
            //MessageCenterConsumer中，回调的条件
            message.setSource("NoCodeFlow");
            LOG.info(message.toString());
            messageList.add(message);
        }
        MessageCenterServiceHelper.batchSendMessages(messageList);
    }


    /**
     * Gets message context.
     *
     * @param msgTemplateDyc        消息模版对象
     * @return 标题，文本内容 message context
     */
    protected Tuple<String, String> getMessageContextTuple(DynamicObject businessDynamicObject, DynamicObject msgTemplateDyc) {
        if (Objects.isNull(msgTemplateDyc)) {
            return Tuple.create("", "");
        }
        String msgTemplate = msgTemplateDyc.getLocaleString("msgtemplate").getLocaleValue();
        // 预防没有翻译对应消息模版（简体中文出厂预制）
        if (HRStringUtils.isEmpty(msgTemplate)) {
            msgTemplate = msgTemplateDyc.getLocaleString("msgtemplate").getLocaleValue_zh_CN();
        }
        String title = setParams(msgTemplate, businessDynamicObject, "title");
        String content = setParams(msgTemplate, businessDynamicObject, "content");
        return Tuple.create(title, content);
    }

    /**
     * 填充消息模版
     *
     * @param msgTemplate           消息模版
     * @param businessDynamicObject 业务对象
     * @param content               标题或内容
     * @return 填充后标题或内容
     */
    protected String setParams(String msgTemplate, DynamicObject businessDynamicObject, String content) {
        List<String> params = new ArrayList<>();
        JSONObject templateObject = JSON.parseObject(msgTemplate);
        String str = parseMsgTemplate(templateObject.getString(content), params);
        List<String> result = new ArrayList<>();
        for (String param : params) {
            String[] split = param.split("\\.");
            DynamicObject object = getDynamicObject(split, businessDynamicObject, 1);
            String strId = split[split.length - 1];
            if (Objects.isNull(object)) {
                result.add(strId);
                continue;
            }
            String string;
            if (object.get(strId) != null && Timestamp.class.equals(object.get(strId).getClass())) {
                string = HRDateTimeUtils.format(object.getDate(strId), "yyyy-MM-dd");
            } else {
                string = object.getString(strId);
            }
            result.add(string);
        }
        return MessageFormat.format(str, result.toArray());
    }

    /**
     * 通过消息变量获取动态对象
     *
     * @param split         model.person.name
     * @param dynamicObject 这里返回person的动态对象
     * @param index         下标标识，从1开始拿到person
     * @return DynamicObject
     */

    protected DynamicObject getDynamicObject(String[] split, DynamicObject dynamicObject, int index) {
        if (index < split.length - 1) {
            DynamicObject dynamicObject1 = dynamicObject.getDynamicObject(split[index]);
            return getDynamicObject(split, dynamicObject1, index + 1);
        } else {
            return dynamicObject;
        }
    }

    /**
     * 通过模板拿到消息变量的参数
     *
     * @param msgTemplate 您好，{model.person.name}的评估对象xxx,
     * @param params      组装 model.person
     * @return String
     */
    protected String parseMsgTemplate(String msgTemplate, List<String> params) {
        if (HRStringUtils.isEmpty(msgTemplate)) {
            return "";
        }
        String result = msgTemplate;
        String[] split = msgTemplate.split("\\{");
        if (split.length <= 1) {
            return result;
        }
        for (int i = 1; i < split.length; i++) {
            String param = split[i];
            params.add(param.substring(0, param.indexOf('}')));
            result = result.replace('{' + param.substring(0, param.indexOf('}')) + "}", "{" + (i - 1) + "}");
        }
        return result;
    }

    /**
     * 拼装跳转链接,带参数
     *
     * @param params params
     * @return url
     */
    protected String buildURL(Map<String, String> params) {
        if(ObjectUtils.isEmpty(params)){
            //参数为空时，不发送连接url
            return null;
        }
        String contentUrl = UrlService.getDomainContextUrl();
        //拼接url,根据不同的表单id,传入不同的参数
        String paramString = params.entrySet()
                .stream()
                .map(entry -> String.format(Locale.ROOT, "%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("&"));

        return String.format(Locale.ROOT, "%s/?%s", contentUrl, paramString);
    }

    private LocaleString wrapLocalStringProperty(Object property){
        LocaleString ls = new LocaleString();
        if(property != null){
            Map<String, Object> tmap = (Map<String, Object>)property;
            for (Entry<String, Object> entry : tmap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                ls.setItem(key, (String)value);
            }
        }
        return ls;
    }
}
