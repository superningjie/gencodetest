package hr;

import kd.bos.bill.BillOperationStatus;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Toolbar;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.operate.AbstractOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.metadata.form.container.FlexPanelAp;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.control.HRFlexPanelAp;
import kd.hr.hbp.common.util.HRDateTimeUtils;
import kd.hr.hbp.common.util.HRImageUrlUtil;
import kd.hr.htm.business.domain.service.activity.IActivityHandleService;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 通用活动处理插件
 * @Version 1.0.0
 * @Date 2024/5/27 15:17
 * @Created by sxf
 */
public class ActivityCommonPlugin extends AbstractFormPlugin {

    //由于处理完成的按钮在工具栏里面，所以先注册处理完成按钮的监听，才能在beforeItemClick事件中获取到
    @Override
    public void registerListener(EventObject e) {
        //新增工具栏监听
        Toolbar toolbar = this.getControl("tbmain");
        toolbar.addItemClickListener(this);
        this.addItemClickListeners("consent");
        super.registerListener(e);
    }

    public void afterBindData(EventObject evt) {
        super.afterBindData(evt);

        //初始化日志信息
//        this.initLog();

        //处理下发待办活动
//        this.handleDistributeActivity();

    }

    //初始化日志信息
    private void initLog() {
        DynamicObject dy = this.getModel().getDataEntity();
        //获取活动实例
        QFilter billFilter = new QFilter("activityins", "in", dy.getLong("activityins.id"));
        DynamicObjectCollection logs = (DynamicObjectCollection) HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSActivityService", "getActivityInsOPRecColl", new Object[]{"creator,activityins,assigntype,createtime,auditmessage,mulhandler", billFilter, "createtime desc", 20});
        if (null != logs) {
            int size = logs.size();
            this.buildLogPanel(size);
            //循环获取活动处理日志信息
            for(int index = 0; index < size; ++index) {
                DynamicObject item = (DynamicObject)logs.get(index);
                String operate = item.getString("assigntype");
                String handler = item.getString("creator.name");
                String handlerNumber = item.getString("creator.number");
                String headScrupture = item.getString("creator.picturefield");
                String handTime = HRDateTimeUtils.format(item.getDate("createtime"), "yyyy-MM-dd HH:mm");
                String recevier = IActivityHandleService.getInstance().getAllHandlers(item.getDynamicObjectCollection("mulhandler"));
                String description = item.getString("auditmessage");
                FormShowParameter showParameter = new FormShowParameter();
                showParameter.setFormId("htm_commonlogshow");
                showParameter.getOpenStyle().setShowType(ShowType.InContainer);
                Map<String, Object> paramsMap = new HashMap();
                paramsMap.put("handlerName", handler);
                paramsMap.put("handletime", handTime);
                paramsMap.put("headsculpture", HRImageUrlUtil.getImageFullUrl(headScrupture));
                paramsMap.put("handlerNum", handlerNumber);
                paramsMap.put("handler", recevier);
                paramsMap.put("receivorName", recevier);
                paramsMap.put("operate", operate);
                paramsMap.put("description", description);
//                paramsMap.put("taskname", MessageFormat.format(ResManager.loadKDString("{0}任务", "ActivityCommonPlugin_2", "hr-htm-formplugin", new Object[0]), this.getTaskName()));
                if (index == 0) {
                    paramsMap.put("isShowBlank", "1");
                } else {
                    paramsMap.put("isShowBlank", "0");
                }
                showParameter.setCustomParams(paramsMap);
                showParameter.getOpenStyle().setTargetKey("cxsk_logpanel" + index);
                showParameter.setSendToClient(true);
                this.getView().showForm(showParameter);
            }

        }
    }

    //构建日志面板
    private void buildLogPanel(int size) {
        FlexPanelAp headPanelAp = new FlexPanelAp();
        headPanelAp.setId("cxsk_logpanel");
//        headPanelAp.setName(new LocaleString(MessageFormat.format(ResManager.loadKDString("{0}日志", "ActivityCommonPlugin_3", "hr-htm-formplugin", new Object[0]), this.getTaskName())));
        headPanelAp.setKey("cxsk_logpanel");
        headPanelAp.setDirection("column");
        headPanelAp.setJustifyContent("flex-start");
        headPanelAp.setAlignItems("center");
        for(int index = 0; index < size; ++index) {
            FlexPanelAp flexPanelAp = createMappedFieldApBuilder("cxsk_logpanel" + index).build();
            headPanelAp.getItems().add(flexPanelAp);
        }
        this.getView().updateControlMetadata("cxsk_logpanel", headPanelAp.createControl());
    }

    private static HRFlexPanelAp.Builder createMappedFieldApBuilder(String flexKey) {
        return (new HRFlexPanelAp.Builder(flexKey)).setDirection("row").setJustifyContent("center").setAlignItems("center").setGrow(0).setShrink(0).setClickable(true);
    }


    //处理下发待办活动
    private void handleDistributeActivity() {
        DynamicObject dataEntity = this.getModel().getDataEntity();
        String taskstatus = dataEntity.getString("activityins.taskstatus");
        BillShowParameter formShowParameter = (BillShowParameter)this.getView().getFormShowParameter();
        BillOperationStatus status = formShowParameter.getBillStatus();
        if (!status.equals(BillOperationStatus.VIEW)&&!"40".equals(taskstatus)) {
            //主单据标识
            String bizbillnumber=this.getModel().getDataEntity().getString("bizbillnumber");
            //主单据id
            Long bizbillid = this.getModel().getDataEntity().getLong("bizbillid");
            //获取活动实例id
            long activityinsId = this.getModel().getDataEntity().getLong("activityins.id");
            //获取主单据信息
            DynamicObject billInfo = BusinessDataServiceHelper.loadSingle(bizbillid,bizbillnumber);
//            DistributeTodoUtils.genInsertActivities(this.getView(),activityinsId,billInfo);
        }

    }

    //在处理完成操作之后，将缓存中的字段取出来进行赋值
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        IDataModel model = this.getModel();
        DynamicObject billDy = model.getDataEntity();
        //获取操作，只有处理完成时才赋值
        AbstractOperate op = (AbstractOperate)afterDoOperationEventArgs.getSource();
        String operateKey = op.getOperateKey();
        if ("consent".equals(operateKey) && afterDoOperationEventArgs.getOperationResult().isSuccess()==true) {

            //主单据标识
            String bizbillnumber=this.getModel().getDataEntity().getString("bizbillnumber");
            //主单据id
            Long bizbillid = this.getModel().getDataEntity().getLong("bizbillid");

                DynamicObject dataEntity =this.getView().getModel().getDataEntity();
                //获取主单据信息
                DynamicObject billInfo = BusinessDataServiceHelper.loadSingle(bizbillid,bizbillnumber);
//                DistributeTodoUtils.commitActivity( dataEntity ,billInfo);

            this.getView().invokeOperation("refresh");
        }
    }


}
