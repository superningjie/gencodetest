package tdkw.opa.tdkw_opa.formplugin.op;

import kd.bos.coderule.api.CodeRuleInfo;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.formula.RowDataModel;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.entity.validate.ErrorLevel;
import kd.bos.entity.validate.ValidationErrorInfo;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrgPerfMetricsSubmitOpPlugin extends AbstractOperationServicePlugIn {

    /**
     * 日志
     */
    private static final Log logger = LogFactory.getLog(OrgPerfMetricsSubmitOpPlugin.class);

    /**
     * 操作执行，加载单据数据包之前，触发此事件；
     *
     * @remark 在单据列表上执行单据操作，传入的是单据内码；
     * 系统需要先根据传入的单据内码，加载单据数据包，其中只包含操作要用到的字段，然后再执行操作；
     * 在加载单据数据包之前，操作引擎触发此事件；
     * <p>
     * 插件需要在此事件，添加需要用到的字段；
     * 否则，系统加载的单据数据包，可能没有插件要用到的字段值，从而引发中断
     */
    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        e.getFieldKeys().add("tdkw_adminorg");
        e.getFieldKeys().add("tdkw_assess_year");
        e.getFieldKeys().add("id");
        e.getFieldKeys().add("tdkw_audit_billid");

        e.getFieldKeys().add("tdkw_area_type");

        e.getFieldKeys().add("tdkw_superorg");
        e.getFieldKeys().add("tdkw_metric");
        e.getFieldKeys().add("tdkw_metric_str");
        e.getFieldKeys().add("tdkw_weight");
        e.getFieldKeys().add("tdkw_metric_desc");
        e.getFieldKeys().add("tdkw_target_value");
        e.getFieldKeys().add("tdkw_unit");
        e.getFieldKeys().add("tdkw_rating_standard");


    }

    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        super.beforeExecuteOperationTransaction(e);
        List<ExtendedDataEntity> passDataEntitys = new ArrayList<>();

        // 逐单校验 权重值
        for (ExtendedDataEntity dataEntity : e.getSelectedRows()) {
            if (delivaryDateValidate(dataEntity) ) {
                passDataEntitys.add(dataEntity);
            }
        }
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        for (ExtendedDataEntity extendedDataEntity : passDataEntitys) {
            HashMap<String, Object> map = new HashMap<>();
            DynamicObject dynamicObject = extendedDataEntity.getDataEntity();
            long id = dynamicObject.getLong("id");
            long orgId = dynamicObject.getLong("tdkw_adminorg.id");
            Date year = dynamicObject.getDate("tdkw_assess_year");

            map.put("id", id);
            map.put("orgId", orgId);
            map.put("year", year);
            list.add(map);
        }
        List<Long> idList = list.stream().map(o -> (Long) o.get("id")).collect(Collectors.toList());
        DynamicObject[] alignments = BusinessDataServiceHelper.load("tdkw_target_alignment", "tdkw_targetid,tdkw_targetalientry,tdkw_targetalientry.tdkw_alignmentstatus,tdkw_targetalientry.tdkw_findctid,tdkw_targetalientry.tdkw_findctid.id", new QFilter[]{new QFilter("tdkw_targetid", QCP.in, idList)});
        Map<Long, DynamicObject> targetIdAlignmentMap = Arrays.stream(alignments).collect(Collectors.toMap(o -> o.getLong("tdkw_targetid"), o -> o, (a, b) -> a));

        List<Long> orgIdList = list.stream().map(o -> (Long) o.get("orgId")).collect(Collectors.toList());
        List<Integer> yearList = list.stream().map(o -> (Date) o.get("year")).map(date -> {
            // 使用 LocalDate 提取年份
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
        })
                .distinct() // 去重
                .collect(Collectors.toList());

        // 将每个年份转换为对应的日期区间
        DynamicObjectCollection decomposes = QueryServiceHelper.query("tdkw_target_decompose", "tdkw_org_year,tdkw_parentadminorg,tdkw_entryentity,tdkw_entryentity.tdkw_target," +
                        "tdkw_entryentity.tdkw_target.tdkw_metric_type.id,tdkw_entryentity.tdkw_subentryentity.tdkw_decomposedesc,tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id,tdkw_entryentity.tdkw_target.id,tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.name",
                new QFilter[]{new QFilter("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.in, orgIdList).and("YEAR(tdkw_org_year)", QCP.in, yearList)});
        Map<String, List<DynamicObject>> orgYearDecomposeMap = decomposes.stream().collect(Collectors.groupingBy(o -> o.getLong("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id") + o.getString("tdkw_org_year")));


        List<ExtendedDataEntity> passDataTwoEntitys = new ArrayList<>();
        for (ExtendedDataEntity dataEntity : passDataEntitys) {
            if (!delivaryAlignment(dataEntity, targetIdAlignmentMap, orgYearDecomposeMap)) {
                passDataTwoEntitys.add(dataEntity);
            }


        }

        // 向系统传回校验通过的单据
        e.getSelectedRows().clear();
        if (passDataTwoEntitys.isEmpty()) {
            e.cancel = true;     // 没有单据通过了校验，取消后续操作
        } else {
            e.getSelectedRows().addAll(passDataTwoEntitys);
        }
    }
    // 获取指定年份的开始日期 (1月1日 00:00:00)
    private static Date getYearStartDate(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // 获取指定年份的结束日期 (12月31日 23:59:59)
    private static Date getYearEndDate(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private boolean delivaryDateValidate(ExtendedDataEntity dataEntity) {

        // 构建子单据体行数据模型：用于快速访问子单据体行、单据体行、单据头上的字段值
        String orgName = dataEntity.getDataEntity().getString("tdkw_adminorg.name");
        MainEntityType mainType = (MainEntityType) dataEntity.getDataEntity().getDataEntityType();
        RowDataModel rowDataModel = new RowDataModel("tdkw_subentryentity", mainType);

        // 取全部单据体行
        DynamicObjectCollection entryRows = dataEntity.getDataEntity().getDynamicObjectCollection("tdkw_hideentry");

        BigDecimal total = BigDecimal.ZERO;
        // 对单据体行循环
        for (DynamicObject entryRow : entryRows) {
            // 取子单据体行
            DynamicObjectCollection subEntryRows = entryRow.getDynamicObjectCollection("tdkw_subentryentity");
            // 对子单据体行进行循环


            for (DynamicObject subEntryRow : subEntryRows) {
                rowDataModel.setRowContext(subEntryRow);

                String weight = (String) rowDataModel.getValue("tdkw_weight");

                BigDecimal bigDecimal = new BigDecimal(weight);
                total = total.add(bigDecimal);
            }

        }
        if (total.compareTo(new BigDecimal(100)) != 0) {
            this.addErrMessage(dataEntity,
                    orgName+"所有指标权重和需为100%");
            return false;    // 校验不通过
        }

        return true;
    }

    private boolean delivaryAlignment(ExtendedDataEntity dataEntity, Map<Long, DynamicObject> targetIdAlignmentMap, Map<String, List<DynamicObject>> orgYearDecomposeMap) {

        Long targetBillId = dataEntity.getDataEntity().getLong("id");
        if (targetBillId == 0L) {
            return false;
        }
        long orgId = dataEntity.getDataEntity().getLong("tdkw_adminorg.id");
        //Long orgId = adminorg.getLong("tdkw_adminorg.id");
        Date year = dataEntity.getDataEntity().getDate("tdkw_assess_year");

        DynamicObject dynamicObject = dataEntity.getDataEntity();
        DynamicObjectCollection entryEntity = dynamicObject.getDynamicObjectCollection("tdkw_hideentry");
        List<DynamicObject> list =  entryEntity.stream().filter(o -> StringUtils.equals(o.getString("tdkw_area_type.number"), "T0001")).collect(Collectors.toList());
        if (list==null || list.size()==0){
            return false;
        }
        //获取指标单对应的对齐单的分录中 所有未对齐指标的id
        DynamicObject alignment = targetIdAlignmentMap.get(targetBillId);
        List<Long> notAlignmentTarget = new ArrayList<>();
        List<Long> alignmentTarget = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(alignment)) {
            DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
            if (ObjectUtils.isNotEmpty(dynamicObjectCollection)) {
                notAlignmentTarget = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_alignmentstatus").equals("20")).map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
                alignmentTarget = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_alignmentstatus").equals("10")).map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
            }
        }
        List<DynamicObject> decompose = orgYearDecomposeMap.get(orgId+ dataEntity.getDataEntity().getString("tdkw_assess_year"));
        if (ObjectUtils.isNotEmpty(decompose)) {

            //所有子分录中包含当前组织的分解单数据  排除非本组织的和已经对齐的
            for (DynamicObject bill : decompose) {

                Long adminId = bill.getLong("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id");
                Long targetId = bill.getLong("tdkw_entryentity.tdkw_target.id");
                //分解单父单据体
                if (orgId == adminId && check(notAlignmentTarget, alignmentTarget, targetId)) {
                    String adminName = bill.getString("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.name");

                    this.addErrMessage(dataEntity,
                            adminName+"需对齐已分解的指标");
                    return true;
                }
            }
        }
        return false;
    }


    private boolean check(List<Long> notAlignmentTarget, List<Long> alignmentTarget, Long targetId) {
        if (CollectionUtils.isEmpty(notAlignmentTarget) && CollectionUtils.isEmpty(alignmentTarget)) {
            return true;
        } else {
            return notAlignmentTarget.contains(targetId) && !alignmentTarget.contains(targetId);
        }

    }

    private void addErrMessage(ExtendedDataEntity dataEntity, String errMsg) {

        Object pkId = dataEntity.getDataEntity().getPkValue();
        int dataIndex = dataEntity.getDataEntityIndex();
        int rowIndex = 0;
        ErrorLevel errorLevel = ErrorLevel.Error;

        ValidationErrorInfo errInfo = new ValidationErrorInfo("",
                pkId, dataIndex, rowIndex,
                "BeforeExecuteOperationTransactionSample",
                "提交检查",
                errMsg,
                errorLevel);

        this.operationResult.addErrorInfo(errInfo);
    }

    /**
     * 单据数据已经提交到数据库之后，事务未提交之前，触发此事件；
     * P
     *
     * @remark 可以在此事件，进行数据同步处理；
     */
    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        DynamicObject[] dataEntities = e.getDataEntities();
        // 收集所有的审批单主键
        // 查到审批单
        DynamicObject auditBill = BusinessDataServiceHelper.newDynamicObject(EntityName.BILL_ORG_TARGET_AUDIT, true, OperateOption.create());
        CodeRuleInfo codeRule = CodeRuleServiceHelper.getCodeRule(auditBill.getDataEntityType().getName(), auditBill, null);
        String number = CodeRuleServiceHelper.getNumber(codeRule, auditBill);
        auditBill.set("billno", number);
        DynamicObjectCollection orgs = auditBill.getDynamicObjectCollection("tdkw_metrics_adminorg");
        DynamicObjectType targetType = orgs.getDynamicObjectType(); // 获取单据体的类型

        DynamicObjectCollection orgEntry = auditBill.getDynamicObjectCollection("tdkw_org_entryentity");
        DynamicObjectType orgEntryType = orgEntry.getDynamicObjectType();
        for (DynamicObject dataEntity : dataEntities) {
            DynamicObject targetRow = new DynamicObject(targetType);    // 创建一行数据
            targetRow.set("fbasedataid", dataEntity.getLong("tdkw_adminorg.id"));
            orgs.add(targetRow);

            DynamicObject orgEntryRow = new DynamicObject(orgEntryType);
            orgEntryRow.set("tdkw_adminorg", dataEntity.getLong("tdkw_adminorg.id"));
            orgEntryRow.set("tdkw_assess_year", dataEntity.getDate("tdkw_assess_year"));
            DynamicObjectCollection subentry = orgEntryRow.getDynamicObjectCollection("tdkw_org_subentry");
            DynamicObjectType subType = subentry.getDynamicObjectType();

            DynamicObjectCollection hideEntry = dataEntity.getDynamicObjectCollection("tdkw_hideentry");
            for (DynamicObject entry : hideEntry) {
                DynamicObjectCollection subEntryEntity = entry.getDynamicObjectCollection("tdkw_subentryentity");
                long typeId = entry.getLong("tdkw_area_type.id");
                for (DynamicObject subEntry : subEntryEntity) {
                    DynamicObject subRow = new DynamicObject(subType);
                    for (String field : EntityName.AUDIT_FIELDS) {
                        subRow.set(field, subEntry.get(field));
                    }
                    subRow.set("tdkw_metric_type", typeId);
                    subentry.add(subRow);
                }
            }

            orgEntry.add(orgEntryRow);

        }
        DynamicObject[] save = (DynamicObject[]) SaveServiceHelper.save(new DynamicObject[]{auditBill});
        for (DynamicObject dataEntity : dataEntities) {
            dataEntity.set("tdkw_audit_billid", save[0].getPkValue());
        }
        SaveServiceHelper.update(dataEntities);

    }

}
