package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.message.api.EmailInfo;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import org.apache.commons.lang.StringUtils;
import tdkw.hrmp.hrobs.formplugin.util.SendTipTuple;
//import tdkw.inte.inte.common.message.XYEmailPush;
//import tdkw.inte.inte.common.message.YXMessageSOAPEnvelopeBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description ： 字段定义、邮件发送
 * @ClassName ：SendTipFieldUtils
 * @author xxx
 * @Date ：2023/6/15 14:16
 * @Version: 1.0
 */
public class SendTipFieldUtils {

    /**
     * 发送信息-自定义内容
     **/
    public static String tipContent = "<p>亲爱的同事：</p><p>您好！</p><p>为保证您的个人信息准确完整，" + "避免影响您的切身利益，确保及时更新各项福利政策统计信息，敬请您在7日内完成个人信息完善及更新工作，以免" + "影响到您的OA及邮箱等办公系统的正常使用。非常感谢您的支持与配合，祝您工作顺利、生活愉快！</p><p class" + "=\"ql-align-right\" style=\"text-align: right;\">人力资源部</p>";

    /**
     * 按钮-确认-点击
     **/
    public static String btOk = "btnok";

    /**
     * 页签-用户页签
     **/
    public static String tabuser = "tdkw_tabuser";

    /**
     * 页签-组织页签
     **/
    public static String taborg = "tdkw_taborg";

    /**
     * 富文本字段--内容字段
     **/
    public static String content = "tdkw_content";

    /**
     * 页签栏--用户和组织
     **/
    public static String tab = "tdkw_tabap";

    /**
     * 字段 组织 基础资料
     **/
    public static String userRadio = "tdkw_user";

    /**
     * 按钮组 值1
     **/
    public static String radio1 = "1";

    /**
     * 按钮组 值2
     **/
    public static String radio2 = "2";

    /**
     * 按钮组 值3
     **/
    public static String radio3 = "3";

    /**
     * 字段 组织 基础资料
     **/
    public static String org = "tdkw_org";

    /**
     * 字段 完整度 整数
     **/
    public static String integrity = "tdkw_integritynum";

    public static final Log Logger = LogFactory.getLog(SendTipFieldUtils.class);

    /**
     * 发送邮箱
     *
     * @param content 正文内容
     * @param persons 人员Ids
     */
    public static SendTipTuple<Boolean, String> sendEmail(String content, List<Long> persons) {
        String msg = "";
        List<String> receivers = getEmailByPersonId(persons);
        boolean isSuccess = sendEmail(receivers, content);
        if (isSuccess) {
            msg = "邮箱提醒发送成功！";
        } else {
            msg = "邮箱提醒发送失败，请联系管理员开放系统邮箱！";
        }
        return SendTipTuple.create(isSuccess, msg);
    }

    /**
     * 发送OA提示
     *
     * @param content
     * @param persons
     */
    public static SendTipTuple<Boolean, String> sendOA(String content, List<Long> persons) {
        Boolean isSuccess = false;
        String msg = "";
        Set<String> domainAccounts = getDomainAccount(persons);
        int successRow = 0;
        int sum = domainAccounts.size();
        for (String domainAccount : domainAccounts) {
            try {
                Map<String, Object> paramMap = Maps.newHashMap();
                paramMap.put("loginid", domainAccount);
                paramMap.put("type", 0);//0提醒 1完成
                Map<String, Object> sendResult = DispatchServiceHelper.invokeBizService("isc", "iscb", "IscApicService", "invokeScriptApi2", "AuditNotification", paramMap, "");
                successRow++;
//                if(result.containsKey("success")){
//                   if ((Boolean) result.get("success")){
//                       successRow++;
//                   }
//                }
            } catch (Exception e) {
                Logger.info("人员完整度提示，发送OA错误：" + e.getMessage());
                Logger.info("人员完整度提示，发送OA错误，域账号：" + domainAccount);
            }
        }

        if (sum == successRow && successRow > 0) {
            msg = "OA提醒发送成功";
            isSuccess = true;
        } else if (sum != successRow && successRow > 0) {
            msg = "OA提醒发送成功！总条数：" + sum + "，成功条数：" + successRow;
            isSuccess = true;
        }

        if (sum == 0) {
            msg = "OA提醒发送失败：未获取到域账号，请联系管理员维护！";
            isSuccess = false;
        }

        return SendTipTuple.create(isSuccess, msg);
    }


    /**
     * 发送屿信提醒
     * http://emdoc.wx.weaver.com.cn/web/#/1/263
     *
     * @param content
     * @param persons
     * @return
     */
    public static SendTipTuple<Boolean, String> sendXYTip(String content, List<Long> persons) {
        Boolean isSuccess = false;
        String msg = "";
        Set<String> domainAccounts = getDomainAccount(persons);
        //发送屿信  "http://10.91.17.205:8011/OA/XyNewCustomMessageService/ProxyServices/XyNewCustomMessageServicePS?wsdl";
        String host = System.getProperty("YXMessage");
        HashMap<String, String> headerAuth = new HashMap(16);
//        headerAuth.put("username", "weblogic");
//        headerAuth.put("password", "weblogic123");
        HashMap<String, String> bodyIn0 = new HashMap(16);
        //业务编码
        bodyIn0.put("docCode", "sendNewCustomMessage");
        //业务类型
        bodyIn0.put("docType", "1111");
        //当前请求页
        bodyIn0.put("pageNo", "1");
        //总页数
        bodyIn0.put("pageTotal", "1");
        //备用字段
        bodyIn0.put("property", "1");
        //来源系统
//        bodyIn0.put("source", "HR");
        //目标系统
//        bodyIn0.put("target", "ELINK");
        HashMap<String, Object> bodyIn1 = new HashMap(16);
        //消息key   人员完整度：08fc2311     08fc2333     08fc2391 ，人力数字化工作台： rp7Z9Dq4
        bodyIn1.put("messageKey", "08fc2333");
        //标题
        bodyIn1.put("title", "个人信息完善及更新");
        //副标题
        bodyIn1.put("detailTitle", "个人信息完善及更新通知");
        //正文
        bodyIn1.put("context", content);

        String url = System.getProperty("tdkw.hrobs.url");
//        String appUrl = System.getProperty("tdkw.hrobs.appurl");
//        RequestContext requestContext = RequestContext.get();
//        String curUrl = requestContext.getClientFullContextPath() != null ? requestContext.getClientFullContextPath() : "";
//        String curUrl = System.getProperty("tdkw.hrobs.curUrl");
        //     curUrl ="http://hr-dev.xxx.cn/ierp/";
        //打开我的档案
        String pcUrl = url + "index.html?formId=hspm_myermanfile&app=hssc";
//        if (curUrl.contains("ierp/")) {
//            curUrl = curUrl.substring(0, curUrl.length() - 5);
//        }
//
//        if (curUrl.contains("ierp")) {
//            curUrl = curUrl.substring(0, curUrl.length() - 4);
//        }

        String mobileUrl = url + "mobile.html?form=hspm_moberhome&app=hssc";
        //移动端链接
        bodyIn1.put("linkMobileUrl", mobileUrl);
        //pc端链接
        bodyIn1.put("linkUrl", pcUrl);

        //接收人
        List<String> userIdList = new ArrayList<>(domainAccounts);
        bodyIn1.put("userIdList", userIdList);

        try {
            Logger.info("员工信息完整度：发送屿信完整度提醒接收人：" + userIdList + "bodyIn1参数：" + bodyIn1);
//            HashMap<String, String> response = YXMessageSOAPEnvelopeBuilder.buildSOAPEnvelope(host, headerAuth, bodyIn0, bodyIn1);
//            Logger.info("员工信息完整度：发送屿信返回结果：" + response);
//            if (StringUtils.equals(response.get("success"), "true")) {
//                msg = "发送成功";
//                isSuccess = true;
//            } else {
//                msg = "发送失败";
//            }
        } catch (Exception e) {

            Logger.info("员工信息完整度：发送屿信返回结果异常：" + e.getMessage());
        }
        return SendTipTuple.create(isSuccess, msg);
    }


    /**
     * 发送邮箱
     *
     * @param receivers 接收邮箱
     * @param content   发送内容
     * @return 是否成功
     */
    public static boolean sendEmail(List<String> receivers, String content) {
        if (StringUtils.isEmpty(content)) {
            content = "请及时去系统完善信息!";
        }
        EmailInfo emailInfo = new EmailInfo();
        emailInfo.setTitle("信息完整度提醒");
        emailInfo.setContent(content);
        //先固定一个，后续再改
//        receivers.clear();
//        receivers.add("");
        emailInfo.setReceiver(receivers);
//        Map<String, Object> stringObjectMap = EmailHandler.sendEmail(emailInfo);
        //发送邮件改集成接口
//        Map<String, Object> stringObjectMap =  XYEmailPush.sendXYEmail(receivers,"信息完整度提醒",content,null);
//        Logger.info("员工信息完整度：发送邮箱结果：" + stringObjectMap);
//        Boolean result = (Boolean) stringObjectMap.get("result");
//        return result;
        return true;
    }


    /**
     * 根据人事业务档案id(hspm_ermanfile)获取对应人员邮箱
     *
     * @param ermanFileIds 人事业务档案id集合
     * @return 邮箱
     */
    public static List<String> getEmailByErmanFileId(List<String> ermanFileIds) {
        List<Long> personIds = new ArrayList<>();
        for (String ermanFileId : ermanFileIds) {
            DynamicObject ermanFile = BusinessDataServiceHelper.loadSingle(ermanFileId, "hspm_ermanfile", "person");
            DynamicObject person = ermanFile.getDynamicObject("person");
            personIds.add(person.getLong("id"));
        }
        List<String> emails = getEmailByPersonId(personIds);
        return emails;
    }


    /**
     * 封装接收人id
     * userids	否	string	消息接收人ID列表（集成系统中的人员ID标识，多个接收者用竖线分隔）。特殊情况：指定为@all，则向该企业应用的全部成员发送
     * "userids": "63|64|65",
     *
     * userMainIdPs:
     *{
     *     "returnCode": "S",
     *     "returnObj": [
     *         {
     *             "belongto": 0,
     *             "companyName": "XXX科技",
     *             "departName": "软件平台部",
     *             "departmentId": 3087,
     *             "id": 10705,
     *             "jobtitleName": "",
     *             "jobtitleid": 1001019485,
     *             "loginid": "wl.chen",
     *             "managerId": 0,
     *             "status": 0,
     *             "subcompanyid1": 383,
     *             "userName": "陈文良"
     *         }
     *     ]
     * }
     * @param domainAccounts 域账号
     * @return
     */
//    public static String setXYUserIds(Set<String> domainAccounts) {
//        Logger.info("员工信息完整度：获取根据域账户获取XYid："+domainAccounts);
//        String userids = "";
//        for (String domainAccount : domainAccounts) {
//            String userMainIdPs = YXEmpBlessingUtil.getUserMainIdPs(domainAccount);
//            Map<String, Object> userMainIdMap =  JSON.parseObject(userMainIdPs, HashMap.class);
//            if(userMainIdMap!=null){
//                if(userMainIdMap.containsKey("returnObj")){
//                    List<Map<String,Object>> returnObj = (List<Map<String, Object>>) userMainIdMap.get("returnObj");
//                    List<String>  returnIds =  returnObj.stream().map(item->String.valueOf(item.get("id"))).collect(Collectors.toList());
//                    for (String returnId : returnIds) {
//                        if (StringUtils.isBlank(userids)) {
//                            userids = returnId;
//                        } else {
//                            userids = userids + "|" + returnId;
//                        }
//                    }
//                }
//            }
//        }
//        Logger.info("员工信息完整度：获取根据域账户结果："+userids);
//        return userids;
//    }

    /**
     * 根据HR人员信息Id(hrpi_person)获取对应人员邮箱
     *
     * @param personIds HR人员信息
     * @return 邮箱
     */
    public static List<String> getEmailByPersonId(List<Long> personIds) {
        List<Long> userIds = new ArrayList<>();

        //kd.hr.htm.business.util.SendMessageUtil#getBosUserIdsByPersonIds
        Map<String, List<Long>> idMaps = new HashMap(1);
        idMaps.put("person", personIds);
        Map<String, Object> userIdMaps = (Map) HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonService", "getUserIdByPersonInfo", new Object[]{idMaps});
        if (Boolean.TRUE.equals(userIdMaps.get("success"))) {
            Map<Long, Map<String, Object>> data = (Map) userIdMaps.get("data");
            if (data != null) {
                userIds = (List) data.entrySet().stream().map((entry) -> {
                    return (Long) ((Map) entry.getValue()).get("user");
                }).collect(Collectors.toList());
            }
        }

        List<String> emails = new ArrayList();
        QFilter filter = new QFilter("id", QCP.in, userIds);
        DynamicObject[] users = BusinessDataServiceHelper.load("bos_user", "email", filter.toArray());
        for (DynamicObject user : users) {
            String email = user.getString("email");
            emails.add(email);
        }
        return emails;
    }

    /**
     * 获取hr行政组织的下级组织
     *
     * @param hrOrg    hr组织
     * @param allBelow 获取到的下级组织
     * @return
     */
    public static List<Long> getAllBelowHROrg(List<Long> hrOrg, List<Long> allBelow) {
        QFilter filter = new QFilter("parent", QCP.in, hrOrg);
        DynamicObjectCollection adminOrgHR = QueryServiceHelper.query("haos_adminorghr", "id", filter.toArray());
        hrOrg.clear();
        for (DynamicObject item : adminOrgHR) {
            long id = item.getLong("id");
            hrOrg.add(id);
            allBelow.add(id);
        }
        if (hrOrg.size() > 0) {
            getAllBelowHROrg(hrOrg, allBelow);
        } else {
            return allBelow;
        }
        return allBelow;
    }

    /**
     * 根据HR人员信息Id(hrpi_person)获取域账号
     *
     * @param personIds HR人员信息
     * @return
     */
    public static Set<String> getDomainAccount(List<Long> personIds) {
        Set<String> domainAccounts = new HashSet<>();
        QFilter filter = new QFilter("person", QCP.in, personIds);
        filter.and("iscurrentversion", QCP.equals, "1");
        filter.and("initstatus", QCP.equals, "2");
        DynamicObject[] perNontsProps = BusinessDataServiceHelper.load("hrpi_pernontsprop", "tdkw_domainaccount", filter.toArray());
        for (DynamicObject perNontsProp : perNontsProps) {
            String domainAccount = perNontsProp.getString("tdkw_domainaccount");
            domainAccounts.add(domainAccount);
        }
        return domainAccounts;
    }

}
