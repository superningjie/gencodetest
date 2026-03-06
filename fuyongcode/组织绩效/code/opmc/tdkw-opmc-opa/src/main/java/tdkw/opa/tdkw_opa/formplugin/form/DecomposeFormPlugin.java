package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.MessageTypes;
import kd.bos.form.ShowType;
import kd.bos.form.container.Tab;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.CollectionUtils;
import kd.hr.hbp.common.util.HRStringUtils;
import tdkw.opa.tdkw_opa.formplugin.model.SendMessageDto;
import tdkw.opa.tdkw_opa.formplugin.utils.SendYXMessageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author pjj
 * @date 2024-08-20
 * 指标分解表单插件
 */
public class DecomposeFormPlugin extends AbstractFormPlugin implements RowClickEventListener {

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        drawMiddleAreas();
    }

    private void drawMiddleAreas() {
        String first = "";
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
        OperationStatus status = this.getView().getFormShowParameter().getStatus();
        for (int i = 0; i < entryEntity.size(); i++) {
            DynamicObject orgEntry = entryEntity.get(i);
            FormShowParameter showParameter = new FormShowParameter();
            showParameter.setFormId("tdkw_inner_decompose");
            showParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
            showParameter.getOpenStyle().setTargetKey("tdkw_tabap");
            String targetName = orgEntry.getString("tdkw_target_name");
            showParameter.setCaption(targetName);
            DynamicObjectCollection rows = orgEntry.getDynamicObjectCollection("tdkw_subentryentity");
            showParameter.setCustomParam("targetId", orgEntry.getLong("tdkw_target.id"));
            showParameter.setCustomParam("seq", i);
            showParameter.setCustomParam("desc", orgEntry.getString("tdkw_findctdesc"));
            showParameter.setCustomParam("rating", orgEntry.getString("tdkw_rating_standard"));
            showParameter.setCustomParam("targetName", orgEntry.getString("tdkw_target_name"));
            if (status.equals(OperationStatus.VIEW) || status.equals(OperationStatus.EDIT)) {
                showParameter.setCustomParam("rows", rows);
            }
            showParameter.setCustomParam("status", status);
            showParameter.setCloseCallBack(new CloseCallBack(this, "decompose"));
            String pageId = UUID.randomUUID().toString().replace("-", "");
            if (StringUtils.isBlank(first)) {
                first = pageId;
            }
            //设置为异步处理
            showParameter.setSendToClient(true);

            this.getView().showForm(showParameter);
        }
        Tab tab = this.getView().getControl("tdkw_tabap");
        tab.activeTab(first);

    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> customParams = formShowParameter.getCustomParams();
        //赋值表头
        this.getModel().setValue("tdkw_org_year", customParams.get("year"));
        JSONObject json = JSONObject.parseObject(String.valueOf(customParams.get("org")));
        this.getModel().setValue("tdkw_parentadminorg", json.getLong("id"));
        //赋值隐藏字段 绩效地图用
        this.getModel().setValue("tdkw_targetid", customParams.get("targetBillId"));
        //赋值父单据体
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("tdkw_org_perf_metrics", new QFilter[]{new QFilter("id", QCP.equals, customParams.get("targetBillId"))});
        if (!ObjectUtils.isEmpty(dynamicObject)) {
            //隐藏父分录
            DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection("tdkw_hideentry");
            //过滤kpi类数据
            List<DynamicObject> collect = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_area_type.number").equals("T0001")).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(collect)) {
                DynamicObjectCollection sonEntry = collect.get(0).getDynamicObjectCollection("tdkw_subentryentity");
                if (CollectionUtils.isNotEmpty(sonEntry)) {
                    //循环新增行
                    for (DynamicObject object : sonEntry) {
                        DynamicObject row = entryEntity.addNew();
                        DynamicObject mtric = object.getDynamicObject("tdkw_metric");
                        row.set("tdkw_target", mtric);
                        row.set("tdkw_target_name", mtric.getString("name"));
                        row.set("tdkw_findctdesc", mtric.getString("tdkw_description"));
                        row.set("tdkw_rating_standard", mtric.getString("tdkw_rating_standard"));
                    }
                }
            }
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //监听父单据体
        EntryGrid eg = this.getControl("tdkw_entryentity");
        eg.addRowClickListener(this);
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        String operateKey = afterDoOperationEventArgs.getOperateKey();
        if (afterDoOperationEventArgs.getOperationResult() != null && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            if (StringUtils.equals(operateKey, "delall")) {
                //删除所有数据
                this.getModel().deleteEntryData("tdkw_subentryentity");
                //刷新单据体
                this.getView().updateView("tdkw_subentryentity");
            } else if (StringUtils.equals(operateKey, "closeparent")) {
                this.getView().getParentView().invokeOperation("close");
                this.getView().sendFormAction(this.getView().getParentView());
            }
        }

        //发送通知
        if ("inform".equals(operateKey)) {
            //发送XXXX信
            sendYXMessage(this.getView());
        }
    }

    /**
     * @author 李柯
     * @description 发送XXXX信消息
     * @date 2024/9/3
     */
    public static void sendYXMessage(IFormView view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        IDataModel model = view.getModel();

        //指标父分录
        DynamicObjectCollection entryEntity = model.getEntryEntity("tdkw_entryentity");
        //指标集合
        List<Long> targetIds = entryEntity.stream().map(object -> object.getLong("tdkw_target.id")).collect(Collectors.toList());
        //考核年份
        int year = Integer.parseInt(sdf.format(model.getValue("tdkw_org_year")));

        //年份匹配
        QFilter filter = new QFilter("YEAR(tdkw_org_year)", QCP.equals, year);
        //指标匹配
        filter.and("tdkw_targetalientry.tdkw_findctid.id", QCP.in, targetIds);
        //指标对齐，当指标已对齐时不发送消息提醒
        DynamicObject[] alignment = BusinessDataServiceHelper.load("tdkw_target_alignment", "id,tdkw_targetalienadmin,tdkw_targetalientry,tdkw_targetalientry.tdkw_findctid,tdkw_targetalientry.tdkw_alignmentstatus", filter.toArray());
        //指标对齐纪录
        Map<Long, DynamicObjectCollection> targetAliEntry = Arrays.stream(alignment).collect(Collectors.toMap(object -> object.getLong("tdkw_targetalienadmin.id"), object -> object.getDynamicObjectCollection("tdkw_targetalientry")));

        //通知人员配置表
        DynamicObject[] configs = BusinessDataServiceHelper.load("tdkw_notify_config", "id,tdkw_notify_details,tdkw_notify_details.tdkw_person,tdkw_notify_details.tdkw_report_org", null);
        //人员域账号
        Map<Long, DynamicObject> userNameMap = new HashMap<>();
        if (configs.length > 0) {
            //HR人员ID
            List<Long> personIds = configs[0].getDynamicObjectCollection("tdkw_notify_details").stream().map(object -> object.getLong("tdkw_person.person.id")).collect(Collectors.toList());
            //获取人员域账号
            userNameMap = personToUser(personIds);
        }

        //成功的list
        List<DynamicObject> successList = new ArrayList<>();
        //失败的list
        List<DynamicObject> failingList = new ArrayList<>();
        //失败的message
        StringBuilder errorMessage = new StringBuilder();

        //分解-指标父分录
        for (DynamicObject entry : entryEntity) {
            //指标
            DynamicObject target = entry.getDynamicObject("tdkw_target");

            //分解-指标子分录
            DynamicObjectCollection subEntryEntity = entry.getDynamicObjectCollection("tdkw_subentryentity");
            for (DynamicObject subEntry : subEntryEntity) {
                successList.add(subEntry);
                //分解组织
                DynamicObject bedeComposeAdmin = subEntry.getDynamicObject("tdkw_bedecomposeadmin");

                //是否满足发送条件
                boolean sendOrNot = sendCondition(bedeComposeAdmin.getLong("id"), target.getLong("id"), targetAliEntry);
                if (!sendOrNot) {
                    addErrorMessage(target, bedeComposeAdmin, errorMessage, "该分解已对齐。");
                    failingList.add(subEntry);
                    continue;
                }

                //通知人员为空时不发送XXXX信消息
                if (configs.length == 0) {
                    addErrorMessage(target, bedeComposeAdmin, errorMessage, "通知人员配置表为空，请联系管理员配置。");
                    failingList.add(subEntry);
                    continue;
                }

                //通知人员配置表 默认只存在一条数据
                DynamicObjectCollection detailsEntryEntity = configs[0].getDynamicObjectCollection("tdkw_notify_details");
                //填报组织过滤
                DynamicObject detailsEntry = detailsEntryEntity.stream().filter(object -> bedeComposeAdmin.getLong("id") == object.getLong("tdkw_report_org.id")).findFirst().orElse(null);

                //通知人员配置表未配置该分解组织的消息接收人
                if (null == detailsEntry) {
                    addErrorMessage(target, bedeComposeAdmin, errorMessage, "通知人员配置表未配置该分解组织的消息接收人。");
                    failingList.add(subEntry);
                    continue;
                }

                //XXXX信参数封装
                SendMessageDto param = param(view, detailsEntry, userNameMap);
                // 标准化剥离，用苍穹消息渠道集成开发
                Map<String, Object> map = SendYXMessageUtils.sendMessageDemo(param);
                //发送成功
//                if (!"true".equals(map.get("success"))) {
//                    addErrorMessage(target, bedeComposeAdmin, errorMessage, "发送失败。");
//                    failingList.add(subEntry);
//                }
            }
        }

        if (HRStringUtils.isEmpty(errorMessage)) {
            view.showSuccessNotification(ResManager.loadKDString("发送成功{0}条", "DecomposeFormPlugin_2", "tdkw-opmc-tdkw_opa-formplugin-ext", successList.size()));
        } else {
            String title = ResManager.loadKDString("共{0}条单据：发送成功{1}条，失败{2}条", "DecomposeFormPlugin_3", "tdkw-opmc-tdkw_opa-formplugin-ext", successList.size(), successList.size() - failingList.size(), failingList.size());
            view.showMessage(title, errorMessage.toString(), MessageTypes.Default);
        }
    }

    /**
     * @author 李柯
     * @description HR人员转user
     * @date 2024/9/3
     */
    public static Map<Long, DynamicObject> personToUser(List<Long> personIds) {
        QFilter filter = new QFilter("person", QCP.in, personIds);
        DynamicObject[] personUserRel = BusinessDataServiceHelper.load("hrpi_personuserrel", "id,user,person", filter.toArray());

        List<Long> userIds = Arrays.stream(personUserRel).map(object -> object.getLong("user")).collect(Collectors.toList());
        DynamicObject[] user = BusinessDataServiceHelper.load("bos_user", "id,name,username", new QFilter("id", QCP.in, userIds).toArray());
        Map<Long, DynamicObject> userMap = Arrays.stream(user).collect(Collectors.toMap(object -> object.getLong("id"), object -> object));

        return Arrays.stream(personUserRel).collect(Collectors.toMap(object -> object.getLong("person"), object -> userMap.get(object.getLong("user"))));
    }

    /**
     * @author 李柯
     * @description 查询已对齐的组织
     * @date 2024/9/3
     */
    public static boolean sendCondition(Long bedeComposeAdminId, Long targetId, Map<Long, DynamicObjectCollection> targetAliEntryMap) {

        //该组织不存在对齐数据时跳过
        if (null == targetAliEntryMap || null == targetAliEntryMap.get(bedeComposeAdminId)) {
            return true;
        }

        //指标对齐分录
        DynamicObjectCollection targetAliEntry = targetAliEntryMap.get(bedeComposeAdminId);
        for (DynamicObject targetAli : targetAliEntry) {
            //对齐-指标ID
            long findCtId = targetAli.getLong("tdkw_findctid.id");
            //对齐状态
            String alignmentStatus = targetAli.getString("tdkw_alignmentstatus");

            if (targetId == findCtId && "10".equals(alignmentStatus)) {
                return false;
            }
        }


        return true;
    }

    /**
     * @author 李柯
     * @description 发送XXXX信消息参数封装
     * @date 2024/9/3
     */
    public static SendMessageDto param(IFormView view, DynamicObject details, Map<Long, DynamicObject> userNameMap) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        DynamicObject parentAdminOrg = (DynamicObject) view.getModel().getValue("tdkw_parentadminorg");
        //消息标题
        String title = parentAdminOrg.getString("name") + "已分解组织绩效指标，请及时填报！";
        //消息正文
        String context = parentAdminOrg.getString("name") + "已分解组织绩效指标，请及时填报！";

        long personId = details.getLong("tdkw_person.person.id");
        //接收人域账号
        DynamicObject user = userNameMap.get(personId);
        //接收人域账号
        String receivers = user.getString("username");
        //接收人名称
        String receiverNames = user.getString("name");

        //标识
        String entityName = details.getDataEntityType().getName();

        //封装消息发送参数
        SendMessageDto sendMessageDto = new SendMessageDto();
        //实体标识
        sendMessageDto.setEntityNumber("tdkw_notify_config");
        //移动端实体标识
        sendMessageDto.setMobileEntityNumber("");
        //消息标题
        sendMessageDto.setTitle(title);
        //消息正文
        sendMessageDto.setContext(context);
        //唯一流水号
        sendMessageDto.setDocCode(entityName + details.getLong("id"));
        //业务层级
        sendMessageDto.setBusType("人力数字化平台,组织绩效");
        //app回调地址
        sendMessageDto.setAppUrl(System.getProperty("tdkw.inte.bos.common.todo.appImgUrl"));
        //发起时间
        sendMessageDto.setCreateDateTime(sdf.format(new Date()));
        //接收时间
        sendMessageDto.setReceiveDateTime(sdf.format(new Date()));
        //是否支持批量操作
        sendMessageDto.setSupportBatch(false);
        //审批时间
        sendMessageDto.setApproveDateTime(sdf.format(new Date()));

        //统一待办消息状态
        sendMessageDto.setOptType(1);
        //XXXX信消息状态
        sendMessageDto.setMsgStatus("");
        // 标准化剥离，调整接收id，发送平台消息
        sendMessageDto.setReceivers(user.getString("id"));

        //接收人姓名
        sendMessageDto.setReceiverNames(receiverNames);

        return sendMessageDto;
    }

    private static void addErrorMessage(DynamicObject target, DynamicObject bedeComposeAdmin, StringBuilder message, String errorMessage) {
        if (StringUtils.isNotBlank(message)) {
            message.append("\n");
        }

        message.append(ResManager.loadKDString("{0}-{1}（{2}）：" + errorMessage, "DecomposeFormPlugin_1", "tdkw-opmc-tdkw_opa-formplugin-ext", target.getString("name"), bedeComposeAdmin.getString("name"), bedeComposeAdmin.getString("number")));
    }

}
