package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.Label;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.HttpUtils;
import tdkw.hrmp.hrobs.formplugin.report.ApplicationRpt;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.EventObject;
import java.util.HashMap;

/**
 * @author xxx
 * @Date 2023/6/15 10:57
 * @Description PC_我的申请卡片 表单插件
 * @Change 需求变更 https://www.kdocs.cn/l/chxbmD4Vzaw0
 * @Change 需求变更 #78300 PC端员工自助门户“我的申请”单据标题需支持点击跳转 http://ones.xxx.com/project/#/team/JbjqrWit/task/DE84BgNqhMsOQo5W
 * @Demander xxx
 * @Document PC端我的申请需规说明_V2.0_0614
 * @Basedata tdkw_myapply
 * @Version 1.0
 **/
public class ApplicationCardFormPlugin extends AbstractFormPlugin implements ClickListener {
    public static final Log log = LogFactory.getLog(ApplicationCardFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_classroomdata", "tdkw_inprogressdata", "tdkw_completeddata",
                "tdkw_inprogressflex1", "tdkw_inprogressflex2", "tdkw_completedflex1", "tdkw_completedflex2");
        this.addClickListeners("tdkw_flex1", "tdkw_flex2", "tdkw_flex3");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.setApplicationQty();
        this.setLatestBillInfo();
        try {
            this.setClassroomInfo();
        } catch (Exception exception) {
            log.info("待学课堂接口异常");
        }
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        DynamicObject dataEntity = this.getModel().getDataEntity();
        Control source = (Control) evt.getSource();
        ReportShowParameter showParameter = new ReportShowParameter();
        log.info("sourceKey" + source.getKey());
        switch (source.getKey()) {
            case "tdkw_inprogressdata":
                // 在办申请
                showParameter.setCustomParam("applicationType", "1");
                showParameter.setFormId("tdkw_application");
                showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                showParameter.setHasRight(true);
                this.getView().showForm(showParameter);
                break;
            case "tdkw_completeddata":
                // 已办申请
                showParameter.setCustomParam("applicationType", "2");
                showParameter.setFormId("tdkw_application");
                showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                showParameter.setHasRight(true);
                this.getView().showForm(showParameter);
                break;
            case "tdkw_classroomdata":
                // 待学课堂
                // TODO 异常屏蔽处理
//                this.openOnlineUrl();
                break;
            case "tdkw_flex3":
                // 待学课堂
                // TODO 异常屏蔽处理
//                this.openOnlineUrl();
                break;
            case "tdkw_inprogressflex1":
                // 在办申请1
                String tdkwInprogressbillname1 = dataEntity.getString("tdkw_inprogressbillname1");
                String tdkw_inprogressbill1id = dataEntity.getString("tdkw_inprogressbill1id");
                String tdkw_inprogresbillnumber1 = dataEntity.getString("tdkw_inprogresbillnumber1");
                this.showBillByBillType(tdkwInprogressbillname1, tdkw_inprogressbill1id, tdkw_inprogresbillnumber1);
                break;
            case "tdkw_inprogressflex2":
                // 在办申请2
                String tdkw_inprogressbillname2 = dataEntity.getString("tdkw_inprogressbillname2");
                String tdkw_inprogressbill2id = dataEntity.getString("tdkw_inprogressbill2id");
                String tdkw_inprogresbillnumber2 = dataEntity.getString("tdkw_inprogresbillnumber2");
                this.showBillByBillType(tdkw_inprogressbillname2, tdkw_inprogressbill2id, tdkw_inprogresbillnumber2);
                break;
            case "tdkw_completedflex1":
                // 已办申请1
                String tdkw_completedbillname1 = dataEntity.getString("tdkw_completedbillname1");
                String tdkw_completedbill1id = dataEntity.getString("tdkw_completedbill1id");
                String tdkw_completedbillnumber1 = dataEntity.getString("tdkw_completedbillnumber1");
                this.showBillByBillType(tdkw_completedbillname1, tdkw_completedbill1id, tdkw_completedbillnumber1);
                break;
            case "tdkw_completedflex2":
                // 已办申请2
                String tdkw_completedbillname2 = dataEntity.getString("tdkw_completedbillname2");
                String tdkw_completedbill2id = dataEntity.getString("tdkw_completedbill2id");
                String tdkw_completedbillnumber2 = dataEntity.getString("tdkw_completedbillnumber2");
                this.showBillByBillType(tdkw_completedbillname2, tdkw_completedbill2id, tdkw_completedbillnumber2);
                break;
            default:
                break;
        }
    }

    /**
     * @author xxx
     * @Description 单点登录跳转外部链接-在线学习
     * @Date 2023/7/14 10:28
     */
    public void openOnlineUrl() {
        /*
            http://
            10.91.35.93:8888
            /transfer/el/sso/kingdee/login?sid=
            jiay.lin
            &returnUrl=https://xycs.yunxuetang.cn/study/%23/userhome%20&token=
            test001
            &type=PC
        */
        /*
            http://
            10.91.16.183:8080
            /app/user/kingdee/login
            ?sid=
            jiay.lin
            &returnUrl=
            /app/course/courseTodo
            &token=
            test001
            &type=PC
         */
        /*StringBuilder url = new StringBuilder();
        url.append("https://");
        String ip = System.getProperty("hr.ipconfig.onlineip");
        String middle = System.getProperty("hr.ipconfig.onlinemiddle");
        String returnurl = System.getProperty("hr.ipconfig.onlinereturnurl");
        url.append(ip);
        url.append(middle);
        url.append("?sid=");
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员非时序性属性
        DynamicObject hrpi_pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
        // sid
        String tdkw_domainaccount = hrpi_pernontsprop.getString("tdkw_domainaccount");
        url.append(tdkw_domainaccount);
        url.append("&returnUrl=");
        url.append(returnurl);
        url.append("&token=");
        String token = GetTokenUtil.getToken(tdkw_domainaccount);
        url.append(token);
        url.append("&type=PC");
        log.info(url.toString());
        this.getView().openUrl(url.toString());*/
    }

    /**
     * @author xxx
     * @Description 设置待学课堂数据
     * @Date 2023/7/26 11:36
     */
    public void setClassroomInfo() {
        long currentUserId = UserServiceHelper.getCurrentUserId();
        log.info("当前用户ID：" + currentUserId);
        Long hrUserId = getHRUser(currentUserId);
        log.info("HR人员信息ID：" + currentUserId);
        if (hrUserId.equals(0)) {
            log.info("HR人员信息ID为0，直接返回0");
            return;
        }
        QFilter qFilter = new QFilter("person", "=", hrUserId).and("iscurrentversion", "=", "1");
        DynamicObject pernontsprop = BusinessDataServiceHelper.loadSingle("hrpi_pernontsprop", "tdkw_domainaccount", qFilter.toArray());
        if (pernontsprop == null) {
            log.info("非时序性属性为空，直接返回0");
            return;
        }

        //登录总线的账号的密码
        String account = System.getProperty("api.esc.appauthority.accountpwd");
        //域账号
        String domainaccount = pernontsprop.getString("tdkw_domainaccount");
        //总线api地址和端口
        String busapiaddr = System.getProperty("api.esc.appauthority.busapiaddr");
        //文件路径
        String filepath = System.getProperty("api.esc.appauthority.filepath");
        //拼接成URL
        String url = busapiaddr + filepath;
//        String url = getBigValueParam("siturl")+"/SAAST/EL/ProxyServices/getTodoCountPS";
        log.info("请求第三方接口路径" + url);
        HashMap<String, String> header = new HashMap<>();
        String basicAuth = account;
//        String basicAuth="weblogic:weblogic321";
        String encoding = DatatypeConverter.printBase64Binary(basicAuth.getBytes(StandardCharsets.UTF_8));
        header.put("Authorization", "Basic " + encoding);
        JSONObject requestBody = new JSONObject();
        setRequestParameter(requestBody);
        if (domainaccount == null || "".equals(domainaccount)) {
            log.info("域账号为空，直接返回0");
            return;
        }

        requestBody.put("loginId", domainaccount);
//        requestBody.put("loginId","wl.chen");
        try {
            String responseBody = HttpUtils.sendHttpPost(url, header, requestBody);
            JSONObject response = JSONObject.parseObject(responseBody);
            log.info("接口返回信息：" + response);
            JSONObject jsonObject = response.getJSONObject("out");
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject elTodoCountDto = data.getJSONObject("elTodoCountDto");
            Integer count = elTodoCountDto.getInteger("count");
            Label tdkw_classroomdata = this.getView().getControl("tdkw_classroomdata");
            tdkw_classroomdata.setText(String.valueOf(count));
            JSONArray elTodoDtoList = data.getJSONArray("elTodoDtoList");
            if (elTodoDtoList.isEmpty() || elTodoDtoList.size() == 0) {
                return;
            }
            // 2023年9月8日11:49:09 返回的日期为空会NPE、 云学堂返回的接口已经排序、不再排序
            //elTodoDtoList.sort(Comparator.comparing(obj -> {
            //    Date startDate = ((JSONObject) obj).getDate("startDate");
            //    return startDate;
            // }).reversed());

            IFormView view = this.getView();
            IDataModel model = this.getModel();
            for (int i = 0; i < (Math.min(elTodoDtoList.size(), 2)); i++) {
                model.setValue("tdkw_classroomname" + (i + 1), ((JSONObject) elTodoDtoList.get(i)).get("title"));
                model.setValue("tdkw_classroomdate" + (i + 1), ((JSONObject) elTodoDtoList.get(i)).get("startDate"));
                view.updateView("tdkw_classroomname" + (i + 1));
                view.updateView("tdkw_classroomdate" + (i + 1));
            }
        } catch (Exception e) {
            log.info("在线学习代办数量接口调用失败：" + e.getMessage());
        }
    }

    /**
     * 设置请求体参数
     */
    private static void setRequestParameter(JSONObject requestBody) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pageTotal", "");
        jsonObject.put("docType", "getTodoCount");
        jsonObject.put("pageNo", "");
        jsonObject.put("property", "");
        jsonObject.put("docCode", "");
        jsonObject.put("source", "HR");
        jsonObject.put("target", "EL");
        requestBody.put("in0", jsonObject);
    }

    /**
     * 通过苍穹人员Id查询对应的HR人员Id
     *
     * @param id 苍穹人员Id
     * @return HR人员信息Id
     */
    public static Long getHRUser(Long id) {
        if (id == null || id == 0L) {
            return 0L;
        }
        QFilter qFilter = new QFilter("user", QCP.equals, id);
        DynamicObject dynamicObject = QueryServiceHelper.queryOne("hrpi_personuserrel", "person", qFilter.toArray());
        if (dynamicObject != null) {
            return dynamicObject.getLong("person");
        } else {
            return 0L;
        }
    }

    /**
     * @author xxx
     * @Description 根据单据类型跳转对应单据
     * @Date 2023/7/26 11:07
     */
    public void showBillByBillType(String billType, String billId, String billNumber) {
        if (StringUtils.isEmpty(billType)) {
            return;
        }

        switch (billType) {
            case "1":
                // 离职单
                //读取MC参数  获取离职和代离职的单据编码维护信息
                String quitParam = System.getProperty("eas.resign.isself.param");
                //没有维护按照 代离职申请 LVE-20230905-00023
                // 自己申请离职 ELV-20230904-00001
                if (StringUtils.isNotBlank(billNumber) && StringUtils.isBlank(quitParam)) {
                    //代离职
                    if (billNumber.startsWith("LVE")) {
                        this.showBillForm("htm_quitapply", billId, "");
                    } else if (billNumber.startsWith("QVE")) {
                        //快速离职
                        this.showBillForm("htm_quitapplyfast", billId, "");
                    } else {
                        //员工离职
                        this.showBillForm("htm_quitapplyemp", billId, "tag");
                    }
                }
                break;
            case "2":
                // 请假单
                //读取MC参数  获取休假和代他人休假的单据编码维护信息
                String vaApplySelf = System.getProperty("xiujia0905test");
                // LE-     本人休假
                // LEH-    代他人休假
                if (StringUtils.isNotBlank(billNumber) && StringUtils.isBlank(vaApplySelf)) {
                    if (billNumber.startsWith("LEH-")) {
                        // 代他人休假
                        this.showBillForm("wtabm_vaapply", billId);
                    } else {
                        //休假
                        this.showBillForm("wtabm_vaapplyself", billId);
                    }
                }
                break;
            case "3":
                // 销假单
                //读取MC参数  获取休假和代他人休假的单据编码维护信息
                String vaUpdateSelf = System.getProperty("xiaojia0905test");
                // BG-     本人销假
                // BGH-    代他人销假
                if (StringUtils.isNotBlank(billNumber) && StringUtils.isBlank(vaUpdateSelf)) {
                    if (billNumber.startsWith("BGH")) {
                        // 代他人销假
                        this.showBillForm("wtabm_vaupdate", billId);
                    } else {
                        //销假
                        this.showBillForm("wtabm_vaupdateself", billId);
                    }
                }
                break;
            case "4":
                // 出差单
                this.showBillForm("tdkw_reqtripe", billId);
                break;
            case "5":
                // 销差单
                this.showBillForm("tdkw_destroytripe", billId);
                break;
            case "6":
                // 补卡单
                this.showBillForm("wtpm_supsignself", billId);
                break;
            case "7":
                // 加班单
                this.showBillForm("wtom_otbillself", billId);
                break;
            case "8":
                // 证明单
                this.showBillForm("tdkw_prove_handle", billId);
                break;
            case "9":
                // 个人信息修改单
                this.showBillForm("hspm_infoapproval", billId);
                break;
            case "10":
                // 问询单
                this.showBillForm("tdkw_employee_inquiries", billId);
                break;
            case "11":
                // 招聘需求单
                this.showBillForm("tdkw_rec_apply_bill", billId);
                break;
            case "12":
                //   个人信息删除单
                this.showBillForm("tdkw_hspm_infochg", billId);
                break;
            case "13":
                // 个因私出国申请单
                this.showBillForm("tdkw_wsgl_form_yscgsq", billId);
                break;
            case "14":
                // 银行卡修改申请单
                this.showBillForm("hsas_perbceditbill", billId);
                break;
            default:
                break;
        }
    }

    /**
     * @author xxx
     * @Description 点击标题跳转到对应单据
     * @Date 2023/7/26 11:02
     */
    public void showBillForm(String entityNumber, String billId) {
        if (StringUtils.isNotEmpty(entityNumber) && StringUtils.isNotEmpty(billId)) {
            BillShowParameter billShowParameter = new BillShowParameter();
            billShowParameter.setFormId(entityNumber);
            billShowParameter.setPkId(billId);
            billShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            billShowParameter.setHasRight(true);
            this.getView().showForm(billShowParameter);
        }
    }

    /**
     * @author xxx
     * @Description 点击标题跳转到对应单据 转为跳转离职申请准备
     * @Date 2023/7/26 11:02
     */
    public void showBillForm(String entityNumber, String billId, String tag) {
        if (StringUtils.isNotEmpty(entityNumber) && StringUtils.isNotEmpty(billId)) {
            BillShowParameter billShowParameter = new BillShowParameter();
            billShowParameter.setFormId(entityNumber);
            billShowParameter.setPkId(billId);
            billShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            billShowParameter.setHasRight(true);
            billShowParameter.setCustomParam("tag", tag);
            billShowParameter.setStatus(OperationStatus.VIEW);
            this.getView().showForm(billShowParameter);
        }
    }

    /**
     * @author xxx
     * @Description 查询最新的单据信息
     * @Date 2023/7/11 15:11
     */
    public void setLatestBillInfo() {
        String algoKey = this.getClass().getName();
        DynamicObject dataEntity = this.getModel().getDataEntity();

        DataSet dataSet = ApplicationRpt.queryData(algoKey, null, null, null, null);

        // 在办申请，根据单据状态字段进行过滤，即单据状态=暂存/审批中/已提交/待重新提交
        DataSet dataSet1 = dataSet.filter(" tdkw_billstatuss in ('A','B','G','D') ")
                .orderBy(new String[]{"lastdate desc"}).limit(0, 2);
        // 已办申请，根据单据状态字段进行过滤，即单据状态=审批通过/已废弃/审批不通过
        DataSet dataSet2 = dataSet.filter(" tdkw_billstatuss in ('C','E','F') ")
                .orderBy(new String[]{"lastdate desc"}).limit(0, 2);

        this.getView().setVisible(!dataSet1.isEmpty(), "tdkw_flexpanelap1");
        this.getView().setVisible(!dataSet2.isEmpty(), "tdkw_flexpanelap11");

        int i = 1;
        for (Row row : dataSet1) {
            dataEntity.set("tdkw_inprogressbillname" + i, row.get("tdkw_billtype"));
            dataEntity.set("tdkw_inprogressbilldate" + i, row.get("lastdate"));
            dataEntity.set("tdkw_inprogresbillnumber" + i, row.get("tdkw_billno"));
            dataEntity.set("tdkw_inprogressbill" + i + "id", row.get("tdkw_billid"));
            i++;
        }
        int j = 1;
        for (Row row : dataSet2) {
            dataEntity.set("tdkw_completedbillname" + j, row.get("tdkw_billtype"));
            dataEntity.set("tdkw_completedbilldate" + j, row.get("tdkw_submitdate"));
            dataEntity.set("tdkw_completedbillnumber" + j, row.get("tdkw_billno"));
            dataEntity.set("tdkw_completedbill" + j + "id", row.get("tdkw_billid"));
            j++;
        }
        IFormView view = this.getView();
        view.updateView("tdkw_inprogressbillname1");
        view.updateView("tdkw_inprogressbilldate1");
        view.updateView("tdkw_inprogressbillname2");
        view.updateView("tdkw_inprogressbilldate2");
        view.updateView("tdkw_completedbillname1");
        view.updateView("tdkw_completedbilldate1");
        view.updateView("tdkw_completedbillname2");
        view.updateView("tdkw_completedbilldate2");
        view.updateView("tdkw_inprogressbill1id");
        view.updateView("tdkw_inprogressbill2id");
        view.updateView("tdkw_completedbill1id");
        view.updateView("tdkw_completedbill2id");
        view.updateView("tdkw_inprogresbillnumber1");
        view.updateView("tdkw_inprogresbillnumber2");
        view.updateView("tdkw_completedbillnumber1");
        view.updateView("tdkw_completedbillnumber2");
    }

    /**
     * @author xxx
     * @Description 设置申请数量
     * @Date 2023/6/15 10:59
     */
    public void setApplicationQty() {
        // -在办申请，根据单据状态字段进行过滤，即单据状态=暂存/审批中/已提交/待重新提交
        int inProgressQty = 0;
        // -已完成申请，根据单据状态字段进行过滤，即单据状态=审批通过/已废弃/审批不通过
        int completedQty = 0;

        // 离职单、请假单、销假单、出差单、销差单、补卡单、加班单、个人信息修改单 单据状态编码统一
        // A	暂存
        // B	已提交
        // G	待重新提交
        // D	审批中
        // C	审批通过
        // E	审批不通过
        // F	已废弃
        /*
        需求变更：
        证明单、问询单、招聘需求单：单据状态统一为上述标准编码
         */
        // TODO 缺少一些单据数据，屏蔽处理 "tdkw_prove_handle","tdkw_employee_inquiries","tdkw_rec_apply_bill","tdkw_hspm_infochg", "tdkw_wsgl_form_yscgsq"
//        String[] entityNameList = {"htm_quitapplyemp", "wtabm_vaapplyself", "wtabm_vaupdateself", "tdkw_reqtripe",
//                "tdkw_destroytripe", "wtpm_supsignself", "wtom_otbillself", "hspm_infoapproval",
//                "tdkw_prove_handle", "tdkw_employee_inquiries", "tdkw_rec_apply_bill", "tdkw_hspm_infochg", "tdkw_wsgl_form_yscgsq"};

        String[] entityNameList = {"htm_quitapplyemp", "wtabm_vaapplyself", "wtabm_vaupdateself",
                 "wtpm_supsignself", "wtom_otbillself", "hspm_infoapproval"};

        String[] doingStatus = {"A", "B", "G", "D"};
        String[] doneStatus = {"C", "E", "F"};
        inProgressQty = this.queryBillQty(entityNameList, doingStatus);
        completedQty = this.queryBillQty(entityNameList, doneStatus);


        // 查询【银行卡变更申请单】数量
        long currentUserId = UserServiceHelper.getCurrentUserId();
        // 银行卡变更申请单 人员过滤
        Long hrUserId = HRRoleAndPersonUtils.getHRUser(currentUserId);
        QFilter HRPersonFilter = new QFilter("person", QCP.equals, hrUserId);
        int bankCardChangeDoingCount = ORM.create().queryDataSet("hsas_perbceditbill", "hsas_perbceditbill", "id", new QFilter[]{HRPersonFilter, new QFilter("billstatus", QCP.in, doingStatus)}).count("id", Boolean.TRUE);
        int bankCardChangeDoneCount = ORM.create().queryDataSet("hsas_perbceditbill", "hsas_perbceditbill", "id", new QFilter[]{HRPersonFilter, new QFilter("billstatus", QCP.in, doneStatus)}).count("id", Boolean.TRUE);


        Label tdkw_inprogressdata = this.getView().getControl("tdkw_inprogressdata");
        tdkw_inprogressdata.setText(String.valueOf(inProgressQty + bankCardChangeDoingCount));
        Label tdkw_completeddata = this.getView().getControl("tdkw_completeddata");
        tdkw_completeddata.setText(String.valueOf(completedQty + bankCardChangeDoneCount));
    }

    /**
     * @author xxx
     * @Description 查询当前用户名下 对应状态的 相关单据数量
     * @Date 2023/6/15 11:03
     */
    public int queryBillQty(String[] entityNameList, String[] billStatusList) {
        long currentUserId = UserServiceHelper.getCurrentUserId();
        QFilter userFilter = new QFilter("creator", QCP.equals, currentUserId);

        int billQty = 0;
        for (String entityName : entityNameList) {
            QFilter statusFilter = new QFilter("billstatus", QCP.in, billStatusList);
            if (StringUtils.equals(entityName, "wtabm_vaapplyself")) {
                // 请假单
                statusFilter.and("ischange", QCP.equals, Boolean.FALSE);
                //只取自己申请的
                statusFilter.and("applytyperadio", QCP.equals, "0");
            } else if (StringUtils.equals(entityName, "wtabm_vaupdateself")) {
                // 销假单
                statusFilter.and("ischange", QCP.equals, Boolean.TRUE);
                //只取自己申请的
                statusFilter.and("applytyperadio", QCP.equals, "0");
            } else if (StringUtils.equals(entityName, "htm_quitapplyemp")) {
                //离职
                // 只取自己申请的
                statusFilter.and("applytype", QCP.equals, "2");
            } else if (StringUtils.equals(entityName, "wtpm_supsignself")) {
                //补签
                // 只取自己申请的
                statusFilter.and("applytyperadio", QCP.equals, "0");
            } else if (StringUtils.equals(entityName, "wtom_otbillself")) {
                //加班
                statusFilter.and("applytyperadio", QCP.equals, "0");
            }
            DynamicObjectCollection dynamicObjectCollection = QueryServiceHelper.query(entityName, "id",
                    new QFilter[]{userFilter, statusFilter});
            if (ObjectUtils.isNotEmpty(dynamicObjectCollection) || dynamicObjectCollection.size() != 0) {
                int size = dynamicObjectCollection.size();
                billQty += size;
            }
        }
        return billQty;
    }
}