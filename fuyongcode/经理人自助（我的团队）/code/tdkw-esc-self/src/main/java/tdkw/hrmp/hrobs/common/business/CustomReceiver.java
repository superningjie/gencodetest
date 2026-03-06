package tdkw.hrmp.hrobs.common.business;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.earlywarn.EarlyWarnContext;
import kd.bos.entity.earlywarn.warn.plugin.IEarlyWarnCustomReceiver;
import kd.bos.entity.param.CustomParam;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;
//import tdkw.inte.inte.common.message.YXMessageSOAPEnvelopeBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date 2024/3/4
 * @description 权限调整确认自定义消息接收人插件（预警插件）
 */
public class CustomReceiver implements IEarlyWarnCustomReceiver {
    private final static Log logger = LogFactory.getLog(CustomReceiver.class);

    @Override
    public List<Long> getReceiverIds(EarlyWarnContext earlyWarnContext, DynamicObject[] dynamicObjects) {
        logger.info("接收" + dynamicObjects.length + "条数据");
        Map<String, String> parameterHelper = SystemParamServiceHelper.loadCustomParameterFromCache(new CustomParam());
        String resumeSalaryReceiver = parameterHelper.get("RESUME_SALARY_RECEIVER");
        logger.info("消息接收人：" + resumeSalaryReceiver);
        String[] receiver = resumeSalaryReceiver.split(",");
        if (receiver.length == 0) {
            throw new KDBizException("请到系统参数维护接收人员域账号（多个使用逗号隔开）");
        }
        Set<String> domainAccounts = Arrays.stream(receiver).collect(Collectors.toSet());
        sendXYTip(domainAccounts);

        List<Long> ids = new ArrayList<>();
        ids.add(0L);
        return ids;
    }

    public static void sendXYTip(Set<String> domainAccounts) {
        String host = System.getProperty("YXMessage");
        HashMap<String, String> headerAuth = new HashMap<>(16);
        HashMap<String, String> bodyIn0 = new HashMap<>(16);
        bodyIn0.put("docCode", "sendNewCustomMessage");
        bodyIn0.put("docType", "1111");
        bodyIn0.put("pageNo", "1");
        bodyIn0.put("pageTotal", "1");
        bodyIn0.put("property", "1");
        HashMap<String, Object> bodyIn1 = new HashMap<>(16);
        bodyIn1.put("messageKey", "08fc2333");
        bodyIn1.put("title", "请确认领导自助-简历薪酬查询功能的人员权限是否需要变更？");
        bodyIn1.put("detailTitle", "请确认领导自助-简历薪酬查询功能的人员权限是否需要变更？");
        bodyIn1.put("context", "简历薪酬查看人员有发生异动，请确认人员权限范围");
        String property = System.getProperty("share.resume.salaryinfo.url", "https://hr-uat.xxx.cn/index.html?type=list&formId=bos_list&billFormId=tdkw_cvsalarypchange&app=hspm");
        bodyIn1.put("linkUrl", property);
        List<String> userIdList = new ArrayList<>(domainAccounts);
        bodyIn1.put("userIdList", userIdList);

//        HashMap<String, String> response = YXMessageSOAPEnvelopeBuilder.buildSOAPEnvelope(host, headerAuth, bodyIn0, bodyIn1);
//        logger.info("权限调整确认：发送屿信返回结果：" + response);
    }
}