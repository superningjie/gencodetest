package tdkw.hrmp.hrobs.formplugin.salary.change.task;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.param.CustomParam;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;
//import tdkw.inte.inte.common.message.YXMessageSOAPEnvelopeBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date 2024/3/6
 * @description 异动人员发送屿信消息
 */
public class TransactionOfficerTask extends AbstractTask {
    private final static Log logger = LogFactory.getLog(TransactionOfficerTask.class);

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        String changeConfirm = (String) map.get("changeConfirm");
        switch (changeConfirm) {
            case "无需调整":
                changeConfirm = "1";
                break;
            case "已发起权限变更申请":
                changeConfirm = "2";
                break;
            case "未确认":
            default:
                changeConfirm = "3";
                break;
        }
        DynamicObject[] cvsalarypchange = BusinessDataServiceHelper.load("tdkw_cvsalarypchange", "",
                new QFilter("tdkw_change_confirm", QCP.equals, changeConfirm).toArray());

        if (cvsalarypchange.length > 0) {
            Map<String, String> parameterHelper = SystemParamServiceHelper.loadCustomParameterFromCache(new CustomParam());
            String resumeSalaryReceiver = parameterHelper.get("RESUME_SALARY_RECEIVER");
            logger.info("消息接收人：" + resumeSalaryReceiver);
            String[] receiver = resumeSalaryReceiver.split(",");
            if (receiver.length == 0) {
                throw new KDBizException("请到系统参数维护接收人员域账号（多个使用逗号隔开）");
            }
            Set<String> domainAccounts = Arrays.stream(receiver).collect(Collectors.toSet());
            sendXYTip(domainAccounts);
        }
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
        // local http://localhost:8080/ierp/index.html?formId=pc_main_console&appNumber=hspm&jumpFormId=tdkw_cvsalarypchange
        // uat https://hr-uat.xxx.cn/index.html?formId=pc_main_console&appNumber=hspm&jumpFormId=tdkw_cvsalarypchange
        String property = System.getProperty("share.resume.salaryinfo.url", "https://hr.xxx.cn/index.html?formId=pc_main_console&appNumber=hspm&jumpFormId=tdkw_cvsalarypchange");
        bodyIn1.put("linkUrl", property);
        List<String> userIdList = new ArrayList<>(domainAccounts);
        bodyIn1.put("userIdList", userIdList);

//        HashMap<String, String> response = YXMessageSOAPEnvelopeBuilder.buildSOAPEnvelope(host, headerAuth, bodyIn0, bodyIn1);
//        logger.info("权限调整确认：发送屿信返回结果：" + response);
    }
}