package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import com.alibaba.druid.util.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.*;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 上传述职报告测评对象列表
 */
public class AttachmentTestListPlugin extends AbstractFormPlugin {

    private final Log log = LogFactory.getLog(AttachmentTestListPlugin.class);
    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();
    //刷新
    private static final String REFRESH = "refresh";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);


    }

    @Override
    public void afterCreateNewData(EventObject e) {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        List<Long> assesserObjIds = parameter.getCustomParam("assesserObjIds");
        //过滤测评对象id
        QFilter filterByAssesserObjIds = new QFilter("id", QCP.in, assesserObjIds);
        BillList billList = getControl("cds0_billlistap");
        billList.setFilter(filterByAssesserObjIds);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        BillList billList = getControl("cds0_billlistap");
        ListSelectedRowCollection selectedRows = billList.getSelectedRows();
        ListSelectedRowCollection currentListAllRowCollection = billList.getCurrentListAllRowCollection();
        Map<Long, Map<String, String>> assesserObjNameAndNo = new HashMap<>();
        List<Map<String, String>> assesserObjNameAndNos = new ArrayList<>();
        //获取列表中的测评对象工号
        List<Long> assesserObjIds = currentListAllRowCollection.stream().map(listSelectedRow -> (Long) listSelectedRow.getPrimaryKeyValue()).collect(Collectors.toList());

        //将测评对象名和工号上传
        Arrays.stream(ASSESS_OBJ_ENTITY_SERVICE.queryAssesserInfoByIds(assesserObjIds)).forEach(assesserObj -> {
            Map<String, String> assesserObjName = new HashMap<>();
            assesserObjName.put("name", assesserObj.getString("perffile.name"));
            assesserObjName.put("billno", assesserObj.getString("perffile.billno"));
            assesserObjNameAndNo.put(assesserObj.getLong("id"), assesserObjName);
            assesserObjNameAndNos.add(assesserObjName);
        });

        FormShowParameter showParameter = new FormShowParameter();
        List<Map<String, Object>> formData = new ArrayList<>();
        CloseCallBack callBack = new CloseCallBack(this, REFRESH);
        switch (args.getOperateKey()) {
            //批量上传附件
            case "uploadbills":
                showParameter.setFormId("cea_batchuploadreportwork");
                showParameter.getOpenStyle().setShowType(ShowType.Modal);
                for (ListSelectedRow row : currentListAllRowCollection) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("rowKey", row.getRowKey());
                    data.put("billId", row.getPrimaryKeyValue());
                    data.put("billNo", row.getBillNo());
                    formData.add(data);
                }
                showParameter.getCustomParams().put("formData", SerializationUtils.toJsonString(formData));
                showParameter.setCustomParam("assesserObjs", assesserObjNameAndNo);
                showParameter.setCustomParam("assesserObjNameAndNos", assesserObjNameAndNos);
                showParameter.setCustomParam("uploadMethod", "batch");
                showParameter.setCloseCallBack(callBack);
                this.getView().showForm(showParameter);
                break;
            //上传单个附件
            case "uploadbill":
                showParameter.setFormId("cea_uploadreportwork");
                showParameter.getOpenStyle().setShowType(ShowType.Modal);
                for (ListSelectedRow row : selectedRows) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("rowKey", row.getRowKey());
                    data.put("billId", row.getPrimaryKeyValue());
                    data.put("billNo", row.getBillNo());
                    formData.add(data);
                }
                //因批量上传和单个上传共用同一插件方法，所以在此对单个上传附件的参数进行差异化调整
                List<Long> assesserObj = new ArrayList<>();
                assesserObj.add((Long) formData.get(0).get("billId"));
                DynamicObject[] assesserInfoByIds = ASSESS_OBJ_ENTITY_SERVICE.queryAssesserInfoByIds(assesserObj);
                long assesserObjId = assesserInfoByIds[0].getLong("id");
                String assesserObjBillNo = assesserInfoByIds[0].getString("perffile.billno");
                Map<Long, Map<String, String>> assesserObjNameAndNoOne = new HashMap<>();
                assesserObjNameAndNo.forEach((aLong, stringMap) -> {
                    if (aLong == assesserObjId) {
                        assesserObjNameAndNoOne.put(aLong, stringMap);
                    }
                });

                List<Map<String, String>> NameAndNos = new ArrayList<>();
                NameAndNos = assesserObjNameAndNos.stream().filter(assesserObjInfo -> assesserObjBillNo.equals(assesserObjInfo.get("billno"))).collect(Collectors.toList());

                showParameter.getCustomParams().put("formData", SerializationUtils.toJsonString(formData));
                showParameter.setCustomParam("assesserObjs", assesserObjNameAndNoOne);
                showParameter.setCustomParam("assesserObjNameAndNos", NameAndNos);
                showParameter.setCustomParam("uploadMethod", "one");
                showParameter.setCloseCallBack(callBack);
                this.getView().showForm(showParameter);
                break;
        }
    }

    /**
     * 监控附件上传结果，上传成功则刷新页面
     */
    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (StringUtils.equals(closedCallBackEvent.getActionId(), REFRESH)) {
            String data = (String) closedCallBackEvent.getReturnData();
            if (data != null && !data.isEmpty() && REFRESH.equals(data)) {
                getView().invokeOperation(REFRESH);
            }
        }
    }

}
