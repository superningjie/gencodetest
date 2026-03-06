package tdkw.opa.tdkw_opa.formplugin.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.id.ID;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.message.api.MessageChannels;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.url.UrlService;
import kd.bos.workflow.engine.msg.info.MessageInfo;
import tdkw.opa.tdkw_opa.formplugin.model.SendMessageDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 李柯
 * @description 发送XXXX信消息工具类
 * @date 2024/7/19
 */
public class SendYXMessageUtils {

    private static final Log logger = LogFactory.getLog(SendYXMessageUtils.class);


    /**
     * @author 李柯
     * @description 拼接XXXX信或统一待办的回调地址（PC）
     * @date 2024/8/14
     */
    public static Map<String, String> getMessageUrl(Map<String, String> urlParam) {

        //将参数转为url可识别的字符串（formId=xxx&billId=xxx）
        String paramString = urlParam.entrySet().stream().map(entry -> String.format(Locale.ROOT, "%s=%s", entry.getKey(), entry.getValue())).collect(Collectors.joining("&"));

        String url = System.getProperty("tdkw.inte.bos.common.todo.url");
        String prefixUrl = String.format("%s?url=", url);

        //当前系统地址（http://hr-dev-v6.xiangyu.cn/ierp/index.html?）
        String curUrl = RequestContext.get().getClientFullContextPath();
        if (StringUtils.isEmpty(curUrl)) {
            curUrl = UrlService.getDomainContextUrl() + "/";
        }
        curUrl += "index.html?";

        //拼接域名
        StringBuilder pcUrl = new StringBuilder(curUrl);
        //拼接参数
        pcUrl.append(paramString);

        //统一待办链接（需拼接OA单点地址）
        String dealToDoPcUrl = prefixUrl + pcUrl;
        //XXXX信消息链接
        String yXPcUrl = pcUrl.toString();

        logger.info("dealToDoPcUrl：" + dealToDoPcUrl);
        logger.info("YXPcUrl：" + yXPcUrl);

        Map<String, String> req = new HashMap<>();
        req.put("dealToDoPcUrl", dealToDoPcUrl);
        req.put("YXPcUrl", yXPcUrl);
        return req;
    }

    /**
     * @author 李柯
     * @description 发送统一待办
     * @date 2024/8/12
     */
    public static void dealToDo(SendMessageDto sendMessageDto) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> in0 = new HashMap<>();

        try {
            in0.put("source", "HR");
            in0.put("target", "TODO");
            in0.put("docType", "pushToDo");
            in0.put("docCode", sendMessageDto.getDocCode());
            paramMap.put("in0", in0);
            paramMap.put("sysCode", "HR");
            paramMap.put("secureCode", System.getProperty("tdkw.inte.bos.common.todo.secureCode"));
            paramMap.put("busNo", sendMessageDto.getDocCode());
            paramMap.put("title", sendMessageDto.getTitle());

            paramMap.put("busType", sendMessageDto.getBusType());
            paramMap.put("currentStatus", sendMessageDto.getCurrentStatus());

            paramMap.put("pcUrl", sendMessageDto.getPcUrl());
            paramMap.put("appUrl", sendMessageDto.getAppUrl());
            //操作类型：1、待办；2、已办；3、待阅；4、结束
            paramMap.put("optType", sendMessageDto.getOptType());
            paramMap.put("creator", sendMessageDto.getCreator());
            paramMap.put("creatorName", sendMessageDto.getCreatorName());
            paramMap.put("createDateTime", sendMessageDto.getCreateDateTime());
            paramMap.put("receivers", sendMessageDto.getReceivers());
            paramMap.put("receiverNames", sendMessageDto.getReceiverNames());
            paramMap.put("receiveDateTime", sendMessageDto.getReceiveDateTime());
            paramMap.put("isSupportBatch", sendMessageDto.getSupportBatch());
            paramMap.put("approveDateTime", sendMessageDto.getApproveDateTime());
            JSONObject json = new JSONObject(paramMap);
            logger.info("请求参数为=" + json.toJSONString());

            Object obj = DispatchServiceHelper.invokeBizService("isc", "iscb", "IscApicService", "invokeScriptApi2", "PushPendingTasks", paramMap, "");
            logger.info("响应报文为=" + obj);

            //生成统一认证人员校验码并保存
            generatePersonCheck(Lists.newArrayList(sendMessageDto.getDocCode()), sendMessageDto.getEntityNumber(), sendMessageDto.getMobileEntityNumber(), sendMessageDto.getReceivers());

        } catch (Exception ex) {
            logger.error("发送或修改统一待办消息报错：" + ex);
        }
    }


    public static void generatePersonCheck(List<Object> billIds, String entityNumber, String
            mobileEntityNumber, String creatorPerson) {
        try {
            TXHandle tx = TX.requiresNew();
            Throwable var5 = null;

            try {
                QFilter[] checkCode = (new QFilter("tdkw_pkid", "in", billIds.toString())).and(new QFilter("tdkw_pcindex", "=", entityNumber)).and(new QFilter("tdkw_mobindex", "=", mobileEntityNumber)).toArray();
                DynamicObject[] load = BusinessDataServiceHelper.load("tdkw_personcheck", "tdkw_pkid", checkCode);
                Map<Object, DynamicObject> collect = Arrays.stream(load).collect(Collectors.toMap(a -> a.get("tdkw_pkid"), a -> a));
                List<DynamicObject> addNewInfoList = Lists.newArrayList();
                for (Object billId : billIds) {
                    DynamicObject dynamicObject = collect.get(billId);
                    if (dynamicObject == null) {
                        logger.info("isCheck={} billId={} entityNumber={} mobileEntityNumber={} creatorPerson={}", new Object[]{false, billIds, entityNumber, mobileEntityNumber, creatorPerson});
                        DynamicObject addNewInfo = BusinessDataServiceHelper.newDynamicObject("tdkw_personcheck");
                        addNewInfo.set("tdkw_acc", creatorPerson);
                        addNewInfo.set("tdkw_pkid", billId.toString());
                        addNewInfo.set("tdkw_pcindex", entityNumber);
                        addNewInfo.set("tdkw_mobindex", mobileEntityNumber);
                        addNewInfo.set("enable", "1");
                        addNewInfo.set("status", "C");
                        addNewInfo.set("createtime", new Date());
                        addNewInfo.set("id", ID.genLongId());
                        addNewInfoList.add(addNewInfo);
                    }
                }
                SaveServiceHelper.save(addNewInfoList.toArray(new DynamicObject[0]));

            } catch (Throwable var17) {
                var5 = var17;
                throw var17;
            } finally {
                if (tx != null) {
                    if (var5 != null) {
                        try {
                            tx.close();
                        } catch (Throwable var16) {
                            var5.addSuppressed(var16);
                        }
                    } else {
                        tx.close();
                    }
                }

            }
        } catch (Exception var19) {
            logger.error(var19);
        }
    }

    /**
     * 标准化处理 苍穹插件发消息Demo
     *
     * @param sendMessageDto
     * @return
     */
    public static Map<String, Object> sendMessageDemo(SendMessageDto sendMessageDto) {
        // 构建消息体发送
        MessageInfo message = new MessageInfo();
        // 信息title
        ILocaleString title = new LocaleString();
        ILocaleString localeTaskName = new LocaleString(sendMessageDto.getTitle());
        title.setLocaleValue_en( localeTaskName.getLocaleValue_zh_CN());
        title.setLocaleValue_zh_CN(localeTaskName.getLocaleValue_zh_CN());
        title.setLocaleValue_zh_TW("");
        message.setTitle(title.toString());
        // 信息主体
        ILocaleString content = new LocaleString(sendMessageDto.getContext());
        content.setLocaleValue_en(content.getLocaleValue_zh_CN());
        content.setLocaleValue_zh_CN(content.getLocaleValue_zh_CN());
        content.setLocaleValue_zh_TW("");
        message.setMessageContent(content);
        // 信息接收人
        ArrayList<Long> receivers = new ArrayList<Long>();
        receivers.add(Long.parseLong(sendMessageDto.getReceivers()));
        message.setUserIds(receivers);
        // 信息标签
        ILocaleString tag = new LocaleString();
        tag.setLocaleValue_zh_CN("待办任务催办");
        message.setTag("待办任务催办");
        message.setMessageTag(tag);
        // 信息发送人
        message.setSenderId(RequestContext.get().getCurrUserId());
        message.setType(MessageInfo.TYPE_MESSAGE);
        String notifyType = String.valueOf(MessageChannels.MC.getNumber());
        message.setNotifyType(notifyType);
        message.setEntityNumber("tdkw_inner_decompose");
        message.setSource("tdkw_opa");
        message.setNestBillId(1L);
        message.setBizDataId(1L);
        Map<String, Object> map = MessageCenterServiceHelper.batchSendMessages(Collections.singletonList(message));
        logger.info("[MessageCenterServiceHelper.batchSendMessages]",map.get("success"));
        return map;
    }
}
