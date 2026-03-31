package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDException;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author pjj
 * @date 2024-08-20
 * 对齐单表单插件
 */
public class AlignFormPlugin extends AbstractFormPlugin {

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> customParams = formShowParameter.getCustomParams();
        //赋值表头
        Date year;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            year = format.parse(formShowParameter.getCustomParam("year"));
        } catch (ParseException ex) {
            throw new KDException(ex, new ErrorCode("TXException", ex.getMessage()));
        }
        this.getModel().setValue("tdkw_org_year",year);
        JSONObject json = JSONObject.parseObject(String.valueOf(customParams.get("org")));
        //需要对齐的组织id
        Long orgId= json.getLong("id");
        this.getModel().setValue("tdkw_targetalienadmin", orgId);
        this.getModel().setValue("tdkw_targetid", customParams.get("targetBillId"));
        //分解单
        DynamicObject[] load = BusinessDataServiceHelper.load("tdkw_target_decompose", "tdkw_parentadminorg,tdkw_entryentity,tdkw_entryentity.tdkw_target," +
                        "tdkw_entryentity.tdkw_target.tdkw_metric_type.id,tdkw_entryentity.tdkw_subentryentity,tdkw_subentryentity.tdkw_decomposedesc,tdkw_subentryentity.tdkw_bedecomposeadmin",
                new QFilter[]{new QFilter("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.equals, orgId).and("tdkw_org_year",QCP.equals,year)});
        List<Long> collect = Arrays.stream(load).flatMap(i -> i.getDynamicObjectCollection("tdkw_entryentity").stream()).map(i -> i.getLong("tdkw_target.id")).collect(Collectors.toList());
        DynamicObject[] query = BusinessDataServiceHelper.load("tdkw_perf_metrics", "id,tdkw_metric_type", new QFilter[]{new QFilter("id", QCP.in, collect)});

        Map<Long, DynamicObject> map = Arrays.stream(query).collect(Collectors.toMap(i -> i.getLong("id"), i -> i.getDynamicObject("tdkw_metric_type")));
        //获取对齐单的分录
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_targetalientry");
        //分解单
        if (ObjectUtils.isNotEmpty(load)) {
            for (DynamicObject bill : load) {
                DynamicObjectCollection parentEntry = bill.getDynamicObjectCollection("tdkw_entryentity");
                //循环父单据体获取子单据体
                if (ObjectUtils.isNotEmpty(parentEntry)) {
                    for (DynamicObject parent : parentEntry) {
                        DynamicObjectCollection sonEntry = parent.getDynamicObjectCollection("tdkw_subentryentity");
                        if (ObjectUtils.isNotEmpty(sonEntry)) {
                            for (DynamicObject son : sonEntry) {
                                //分解单 子分录的分解组织 需与当前对齐组织一致
                                Long adminId = son.getLong("tdkw_bedecomposeadmin.id");
                                if(orgId.equals(adminId)) {
                                    //获取单据体创建行数据
                                    DynamicObject dynamicObject = entryEntity.addNew();
                                    //设置指标来源 取单据的表头
                                    dynamicObject.set("tdkw_sourceadmin", bill.getDynamicObject("tdkw_parentadminorg"));
                                    dynamicObject.set("tdkw_decomposebillid",bill.getPkValue());
                                    //设置指标 取父分录指标库
                                    dynamicObject.set("tdkw_findctid", parent.getDynamicObject("tdkw_target"));
                                    dynamicObject.set("tdkw_findctid_name", parent.getDynamicObject("tdkw_target").getString("name"));
                                    dynamicObject.set("tdkw_findcttype", map.get(parent.getLong("tdkw_target.id")));
                                    //dynamicObject.set("tdkw_findcttype",target.getDynamicObject("tdkw_metric_type"));
                                    //设置分解说明 取子分录的分解说明
                                    String desc = son.getString("tdkw_decomposedesc");
                                    if (StringUtils.isNotBlank(desc)) {
                                        dynamicObject.set("tdkw_decomposedesc", desc);
                                    }
                                    dynamicObject.set("tdkw_alignmentstatus", "20");
                                    dynamicObject.set("tdkw_findctdesc",parent.getDynamicObject("tdkw_target").getString("tdkw_description"));
                                }
                            }
                        }
                    }
                }
            }
        }
        this.getView().updateView("tdkw_targetalientry");
    }
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> customParams = formShowParameter.getCustomParams();
        String billStatus = (String) customParams.get("billStatus");
        if (StringUtils.isNotBlank(billStatus)){
            if (StringUtils.equals("A",billStatus)){
                this.getView().setVisible(Boolean.TRUE,"tdkw_cancel");
            }else {
                this.getView().setVisible(Boolean.FALSE,"tdkw_cancel");
            }
        }
    }
    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        EntryGrid  entryGrid  = this.getView().getControl("tdkw_targetalientry");
        int[] selectRows = entryGrid.getSelectRows();
        FormOperate formOperate = (FormOperate) args.getSource();
        String operateKey = formOperate.getOperateKey();
        if (StringUtils.equals("aligntarget", operateKey)) {
            if( selectRows.length != 1 ){
                this.getView().showErrorNotification("请选择一条指标分解记录！");
                args.setCancel(true);
            }else {
                String value = (String) this.getModel().getValue("tdkw_alignmentstatus", selectRows[0]);
                if (StringUtils.equals(value, "10")) {
                    this.getView().showErrorNotification("请勿重复对齐！");
                    args.setCancel(true);
                }
            }
        } else if (StringUtils.equals("cancel", operateKey)) {
            if( selectRows.length != 1 ){
                this.getView().showErrorNotification("请选择一条已对齐的指标分解记录！");
                args.setCancel(true);
            }else {
                String value = (String) this.getModel().getValue("tdkw_alignmentstatus", selectRows[0]);
                if (StringUtils.equals(value, "20")) {
                    this.getView().showErrorNotification("请选择一条已对齐的指标分解记录！");
                    args.setCancel(true);
                }
            }
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        String operateKey = afterDoOperationEventArgs.getOperateKey();
        if (afterDoOperationEventArgs.getOperationResult() != null && afterDoOperationEventArgs.getOperationResult().isSuccess()) {
            if (StringUtils.equals("aligntarget", operateKey)) {
                Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
                FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                formShowParameter.setFormId("tdkw_alignment_window");
                formShowParameter.setStatus(OperationStatus.ADDNEW);
                formShowParameter.setCustomParam("targetBillId", customParams.get("targetBillId"));
                formShowParameter.setCloseCallBack(new CloseCallBack(this, "alignment"));
                this.getView().showForm(formShowParameter);
            } else if (StringUtils.equals("cancel", operateKey)) {
                int row = this.getModel().getEntryCurrentRowIndex("tdkw_targetalientry");
                this.getModel().setValue("tdkw_alignmentstatus", "20", row);
                this.getModel().setValue("tdkw_alignmentid", null, row);
                this.getModel().setValue("tdkw_alignmentdesc", null, row);
                this.getModel().setValue("tdkw_alignmentweight", null, row);
                this.getModel().setValue("tdkw_unit", null, row);
                this.getModel().setValue("tdkw_alignmenttarget", null, row);
            } else if (kd.bos.dataentity.utils.StringUtils.equals(operateKey, "closeparent")) {
                this.getView().getParentView().invokeOperation("close");
                this.getView().sendFormAction(this.getView().getParentView());
            }
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        HashMap<String, String> map = new HashMap<>();
        map.put("yuan", "元");
        map.put("wan_yuan", "万元");
        map.put("bai_wan_yuan", "百万元");
        map.put("qian_wan_yuan", "千万元");
        map.put("percent", "百分比");
        String actionId = closedCallBackEvent.getActionId();
        if (StringUtils.equals(actionId, "alignment")) {
            //获取当前选中行
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_targetalientry");
            int row = this.getModel().getEntryCurrentRowIndex("tdkw_targetalientry");
            DynamicObject dynamicObject = entryEntity.get(row);
            //主键赋值
            ArrayList<Long> ids = new ArrayList<>();

            // 使用 StringJoiner 来拼接字符串，自动处理分隔符
            StringJoiner descriptionJoiner = new StringJoiner(";");
            StringJoiner weightJoiner = new StringJoiner(";");
            StringJoiner targetValueJoiner = new StringJoiner(";");
            StringJoiner unitJoiner = new StringJoiner(";");

            List<DynamicObject> returnData = (List) closedCallBackEvent.getReturnData();
            //循环返回数据拼接
            if (ObjectUtils.isNotEmpty(returnData)) {
                for (DynamicObject returnDatum : returnData) {
                    boolean checkBox = returnDatum.getBoolean("tdkw_checkboxfield");
                    if (checkBox) {
                        DynamicObject metric = (DynamicObject) returnDatum.get("tdkw_metric");

                        // 主键赋值
                        ids.add(metric.getLong("id"));

                        // 指标描述拼接
                        String str1 = metric.getString("tdkw_description");
                        if (StringUtils.isNotBlank(str1)) {
                            descriptionJoiner.add(str1);
                        }

                        // 权重拼接
                        String str2 = returnDatum.getString("tdkw_weight");
                        if (StringUtils.isNotBlank(str2)) {
                            weightJoiner.add(str2);
                        }


                        // 目标值拼接
                        String str3 = returnDatum.getString("tdkw_target_value");
                        if (StringUtils.isNotBlank(str3)) {
                            targetValueJoiner.add(str3);
                        }

                        // 单位拼接
                        String str4 = map.get(returnDatum.getString("tdkw_unit"));
                        if (StringUtils.isNotBlank(str4)) {
                            unitJoiner.add(str4);
                        }

                    }
                }
                //多选基础资料赋值
                DynamicObject[] metrics = BusinessDataServiceHelper.load("tdkw_perf_metrics", "", new QFilter[]{new QFilter("id", QCP.in, ids)});
                DynamicObjectCollection idList = new DynamicObjectCollection();
                for (DynamicObject dy : metrics) {
                    DynamicObject targetDynmicObject = new DynamicObject(dynamicObject.getDynamicObjectCollection("tdkw_alignmentid").getDynamicObjectType());
                    targetDynmicObject.set("fbasedataid", dy);
                    idList.add(targetDynmicObject);
                }
                if (CollectionUtils.isNotEmpty(idList)) {
                    dynamicObject.set("tdkw_alignmentid", idList);
                }
                dynamicObject.set("tdkw_alignmentdesc", descriptionJoiner.toString());
                dynamicObject.set("tdkw_alignmentweight", weightJoiner.toString());
                dynamicObject.set("tdkw_alignmenttarget", targetValueJoiner.toString());
                dynamicObject.set("tdkw_unit", unitJoiner.toString());
                dynamicObject.set("tdkw_alignmentstatus", "10");
            } else {
                this.getModel().setValue("tdkw_alignmentstatus", "20", row);
                this.getModel().setValue("tdkw_alignmentid", null, row);
                this.getModel().setValue("tdkw_alignmentdesc", null, row);
                this.getModel().setValue("tdkw_alignmentweight", null, row);
                this.getModel().setValue("tdkw_unit", null, row);
                this.getModel().setValue("tdkw_alignmenttarget", null, row);
            }
            this.getView().updateView("tdkw_targetalientry");
        }
    }
}
