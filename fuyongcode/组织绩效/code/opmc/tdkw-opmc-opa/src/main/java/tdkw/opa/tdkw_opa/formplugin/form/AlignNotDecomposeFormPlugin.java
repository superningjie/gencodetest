package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.FormShowParameter;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pjj
 * @date 2024-08-28
 * 已分解未对齐弹窗插件
 */
public class AlignNotDecomposeFormPlugin extends AbstractFormPlugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        //获取指标单对应的对齐单的分录中 所有未对齐指标的id
        Object targetBillId = formShowParameter.getCustomParam("targetBillId");
        DynamicObject alignment = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment",new QFilter[]{new QFilter("tdkw_targetid",QCP.equals,targetBillId)});
        List<Long> notAlignmentTarget = new ArrayList<>();
        List<Long> alignmentTarget = new ArrayList<>();
        if(ObjectUtils.isNotEmpty(alignment)) {
            DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
            if (ObjectUtils.isNotEmpty(dynamicObjectCollection)) {
                notAlignmentTarget = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_alignmentstatus").equals("20")).map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
                alignmentTarget = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_alignmentstatus").equals("10")).map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
            }
        }
        //查询所有分解单包含当前组织对应年份的单据  根据当前组织id和对齐单指标id过滤未对齐的数据显示至前端
        Object orgId = formShowParameter.getCustomParam("orgId");
        Date year;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            year = format.parse(formShowParameter.getCustomParam("year"));
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
        DynamicObject[] decompose = BusinessDataServiceHelper.load("tdkw_target_decompose", "tdkw_parentadminorg,tdkw_entryentity,tdkw_entryentity.tdkw_target," +
                        "tdkw_target.tdkw_metric_type,tdkw_entryentity.tdkw_subentryentity,tdkw_subentryentity.tdkw_decomposedesc,tdkw_subentryentity.tdkw_bedecomposeadmin",
                new QFilter[]{new QFilter("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.equals, orgId).and("tdkw_org_year",QCP.equals,year)});
//        List<Long> targetIds = Arrays.stream(decompose).flatMap(i -> i.getDynamicObjectCollection("tdkw_entryentity").stream()).map(i -> i.getLong("tdkw_target.id")).collect(Collectors.toList());
//        DynamicObject[] metrics = BusinessDataServiceHelper.load("tdkw_perf_metrics", "id,tdkw_metric_type", new QFilter[]{new QFilter("id", QCP.in, targetIds)});
//        //指标类型map
//        Map<Long, DynamicObject> targetMap = Arrays.stream(metrics).collect(Collectors.toMap(i -> i.getLong("id"), i -> i.getDynamicObject("tdkw_metric_type")));
        if(ObjectUtils.isNotEmpty(decompose)) {
            //获取所有子分录的数据
//            List<Long> finalTargetIdList = targetIdList;
//            List<DynamicObject> collect = Arrays.stream(decompose)
//                    .filter(i -> i.getDynamicObjectCollection("tdkw_entryentity") != null)
//                    .flatMap(i -> i.getDynamicObjectCollection("tdkw_entryentity").stream())
//                    .filter(i -> i.getDynamicObjectCollection("tdkw_entryentity") != null)
//                    .filter(i -> i.getLong("tdkw_parentadminorg.id") == orgId)
//                    .filter(i -> finalTargetIdList.contains(i.getLong("tdkw_findctid.id"))).collect(Collectors.toList());
            //当前动态表单分录
            DynamicObjectCollection localEntry = this.getModel().getEntryEntity("tdkw_entryentity");
            //所有子分录中包含当前组织的分解单数据  排除非本组织的和已经对齐的
            for (DynamicObject bill : decompose) {
                //分解单父单据体
                DynamicObjectCollection billParentEntry = bill.getDynamicObjectCollection("tdkw_entryentity");
                if( ObjectUtils.isNotEmpty(billParentEntry) ){
                    for (DynamicObject dynamicObject : billParentEntry) {
                        //分解单子单据体
                        DynamicObjectCollection billSonEntry = dynamicObject.getDynamicObjectCollection("tdkw_subentryentity");
                        if( ObjectUtils.isNotEmpty(billSonEntry) ){
                            for (DynamicObject object : billSonEntry) {
                                Long adminId = object.getLong("tdkw_bedecomposeadmin.id");
                                Long targetId = dynamicObject.getLong("tdkw_target.id");
                                //校验分解单 分解组织为当前组织  且对齐单数据中不包含该指标
                                if (orgId.equals(adminId) && check(notAlignmentTarget, alignmentTarget, targetId)) {
                                    //获取单据体创建行数据
                                    DynamicObject row = localEntry.addNew();
                                    //设置指标来源 取单据的表头
                                    row.set("tdkw_sourceadmin", bill.getDynamicObject("tdkw_parentadminorg"));
                                    //设置指标 取父分录指标
                                    row.set("tdkw_findctid", dynamicObject.getDynamicObject("tdkw_target"));
//                                    row.set("tdkw_basedatafield", targetMap.get(targetId));
                                    //设置分解说明 取子分录的分解说明
                                    String desc = object.getString("tdkw_decomposedesc");
                                    if (StringUtils.isNotBlank(desc)) {
                                        row.set("tdkw_decomposedesc", desc);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String fieldKey = e.getProperty().getName();
        if ("tdkw_weight".equals(fieldKey)) {
            Object newValue = e.getChangeSet()[0].getNewValue();
            Object oldValue = e.getChangeSet()[0].getOldValue();
            int rowIndex = e.getChangeSet()[0].getRowIndex();
            if( newValue instanceof String && kd.bos.dataentity.utils.StringUtils.isNotBlank((String)newValue) ){
                String weightStr = (String)newValue;
                // 转换百分比为数值
                BigDecimal weightValue = new BigDecimal(weightStr);

                // 校验百分比范围是否在0-100之间
                if (weightValue.compareTo(BigDecimal.ZERO) < 0 || weightValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                    this.getModel().setValue(fieldKey,oldValue,rowIndex);
                    this.getView().showErrorNotification("权重必须在 0 到 100 之间。");
                }
                if( !weightStr.matches("^\\d{1,3}$") ){
                    this.getModel().setValue(fieldKey,oldValue,rowIndex);
                    this.getView().showErrorNotification("请输入有效的权重值，如'50'。");
                }
            }
        }
    }

    private boolean check(List<Long> notAlignmentTarget, List<Long> alignmentTarget, Long targetId) {
        if (CollectionUtils.isEmpty(notAlignmentTarget) && CollectionUtils.isEmpty(alignmentTarget)) {
            return true;
        } else {
            return notAlignmentTarget.contains(targetId) && !alignmentTarget.contains(targetId);
        }

    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        OperationResult operationResult = afterDoOperationEventArgs.getOperationResult();
        String operateKey = afterDoOperationEventArgs.getOperateKey();
        if( operationResult != null &&operationResult.isSuccess() ){
            if( StringUtils.equals("success",operateKey) ){
                DynamicObjectCollection entry = this.getModel().getEntryEntity("tdkw_entryentity");
                if( CollectionUtils.isNotEmpty(entry) ){
                    List<DynamicObject> collect = entry.stream().filter(i -> i.getBoolean("tdkw_is_alignment")).collect(Collectors.toList());
                    if( CollectionUtils.isNotEmpty(collect) ){
                        this.getView().returnDataToParent(collect);
                    }
                }
                this.getView().close();
            }
        }
    }
}
