package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.BasedataItem;
import kd.bos.entity.datamodel.events.AfterDeleteRowEventArgs;
import kd.bos.entity.datamodel.events.AfterMoveEntryEventArgs;
import kd.bos.entity.datamodel.events.ImportDataEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.datamodel.events.QueryImportBasedataEventArgs;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperateErrorInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.ConfirmTypes;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IPageCache;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.Label;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.form.container.FlexPanelAp;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;
import tdkw.opa.tdkw_opa.formplugin.enums.OperationConst;
import tdkw.opa.tdkw_opa.formplugin.list.OrgPerfMetricsListPlugin;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class OrgPerfMetricsFormPlugin extends AbstractBillPlugIn {

    private static final Log logger = LogFactory.getLog(OrgPerfMetricsListPlugin.class);
    // 行政组织类型：集团、公司、区域
    private static final List<String> ORGTYPES = Arrays.asList("1010_S", "1020_S", "1030_S");

    public static String getOperationResultErrorInfos(OperationResult operationResult) {
        if (operationResult.isSuccess()) {
            return com.alibaba.nacos.api.utils.StringUtils.EMPTY;
        }

        List<IOperateInfo> errorInfos = operationResult.getAllErrorOrValidateInfo();
        int size = errorInfos.size() + operationResult.getSuccessPkIds().size();
        if (size > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            for (int len = errorInfos.size(); i < 5 && i < len; ++i) {
                stringBuilder.append((errorInfos.get(i)).getMessage());
            }
            return stringBuilder.toString();
        }
        else if (!errorInfos.isEmpty()) {
            OperateErrorInfo errorInfo = (OperateErrorInfo) errorInfos.get(0);
            return errorInfo.getMessage() == null ? "" : errorInfo.getMessage();
        }
        else {
            return operationResult.getMessage() == null ? "" : operationResult.getMessage();
        }
    }

    /**
     * @param empPosOrgRelDy
     * @param adminOrgDy
     * @param fieldName
     */

    private static void setIndustry(DynamicObject empPosOrgRelDy, DynamicObject adminOrgDy, List<String> numberList, String fieldName) {
        if (!ObjectUtils.isEmpty(adminOrgDy)) {
            QFilter qFilter = new QFilter("id", "=", adminOrgDy.getLong("id"));
            DynamicObject adminOrgHrDy = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "orgtype,parent,name", qFilter.toArray());
            if (!ObjectUtils.isEmpty(adminOrgHrDy)) {
                DynamicObject orgTypeDy = adminOrgHrDy.getDynamicObject("orgtype");
                if (!ObjectUtils.isEmpty(orgTypeDy)) {
                    String orgTypeNumber = orgTypeDy.getString("number");

                    if (numberList.contains(orgTypeNumber)) {
                        empPosOrgRelDy.set(fieldName, adminOrgHrDy.getString("name"));
                    }
                    else {
                        // 标准化处理递归异常
                        empPosOrgRelDy.set(fieldName, null);
                    }
                }
            }
        }

    }

    private static String getTypeNumber(String property, String number) {
        String addressTypeNumber = System.getProperty(property);
        if (StringUtils.isEmpty(addressTypeNumber)) {
            addressTypeNumber = number;
        }

        return addressTypeNumber;
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        this.addClickListeners("tdkw_add_area", "tdkw_labelap", "tdkw_vectorap");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        DynamicObject org = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
        if (ObjectUtils.isNotEmpty(org)) {
            Label label = this.getView().getControl("tdkw_labelap1");
            label.setText(org.getString("name") + "存在上级已分解且未对齐的指标，");
        }
        Object value = this.getModel().getValue("id");
        if (ObjectUtils.isNotEmpty(value)) {
            updateDate((Long) value);
        }
        Object viewKpi = this.getView().getFormShowParameter().getCustomParam("viewKpi");
        if (ObjectUtils.isNotEmpty(viewKpi)) {
            DynamicObject type = BusinessDataServiceHelper.loadSingle("tdkw_metric_type", new QFilter[]{new QFilter("number", QCP.equals, "T0001")});
            DynamicObjectCollection hideEntry = this.getModel().getDataEntity(true).getDynamicObjectCollection("tdkw_hideentry");
            hideEntry.removeIf(s -> s.getLong("tdkw_area_type.id") != type.getLong("id"));
            this.getView().setVisible(false, "attachmentpanel", "pagepanel");
        }
        drawMiddleAreas();
    }

    @Override
    public void afterCopyData(EventObject e) {
        super.afterCopyData(e);
        this.getModel().setValue("tdkw_audit_billid", null);
        this.getModel().setValue("tdkw_eval_res_list", null);
        this.getModel().setValue("tdkw_assess_year", null);
    }

    @Override
    public void afterMoveEntryUp(AfterMoveEntryEventArgs e) {
        // 单据体行往上移动后，触发此事件；
        drawMiddleAreas();
    }

    @Override
    public void afterMoveEntryDown(AfterMoveEntryEventArgs e) {
        // 单据体行往下移动后，触发此事件；
        drawMiddleAreas();
    }

    @Override
    public void afterDeleteRow(AfterDeleteRowEventArgs e) {
        drawMiddleAreas();
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        if (StringUtils.equals("save", formOperate.getOperateKey()) || StringUtils.equals("submit", formOperate.getOperateKey())) {
            if (StringUtils.equals("save", formOperate.getOperateKey())) {
                String saveMessage = this.getPageCache().get("saveMessage");
                if (StringUtils.equals(saveMessage, "not")) {
                    formOperate.getOption().setVariableValue(OperateOptionConst.ISSHOWMESSAGE, "false");
                    this.getPageCache().remove("saveMessage");
                }
            }
            // 校验必录
            DynamicObjectCollection hideEntry = this.getModel().getEntryEntity("tdkw_hideentry");

            for (int hideEntryIndex = 0; hideEntryIndex < hideEntry.size(); hideEntryIndex++) {
                DynamicObject parentRow = hideEntry.get(hideEntryIndex);
                DynamicObject areaType = parentRow.getDynamicObject("tdkw_area_type");

                DynamicObjectCollection subEntryEntity = parentRow.getDynamicObjectCollection("tdkw_subentryentity");
                // 提交时校验subEntryEntity不能为空
                if (StringUtils.equals("submit", formOperate.getOperateKey()) && (subEntryEntity == null || subEntryEntity.isEmpty())) {
                    args.setCancel(true);
                    this.getView().showErrorNotification(areaType.getString("name") + " 区域未维护指标内容，不允许提交！");
                    return;
                }
                // KPI类 基础资料必填
                for (int subEntryIndex = 0; subEntryIndex < subEntryEntity.size(); subEntryIndex++) {
                    DynamicObject row = subEntryEntity.get(subEntryIndex);
                    String metricName = row.getString("tdkw_metric_str");
                    String metricDesc = row.getString("tdkw_metric_desc");
                    String ratingStandard = row.getString("tdkw_rating_standard");
                    String weight = row.getString("tdkw_weight");
                    DynamicObject superOrg = row.getDynamicObject("tdkw_superorg");
                    if (StringUtils.isBlank(weight)) {
                        args.setCancel(true);
                        this.getView().showErrorNotification(areaType.getString("name") + "区域的第" + (subEntryIndex + 1) + "行分录权重为必填!");
                        return;
                    }
                    String areaTypeNumber = areaType.getString("number");
                    if (!areaTypeNumber.equals("T0005")) {
                        if (StringUtils.isBlank(metricName)) {
                            args.setCancel(true);
                            this.getView().showErrorNotification(areaType.getString("name") + "区域的第" + (subEntryIndex + 1) + "行分录指标名称为必填!");
                            return;
                        }
                        List<String> list = Arrays.asList("T0001", "T0002");
                        if (StringUtils.isBlank(metricDesc) && !list.contains(areaTypeNumber)) {
                            args.setCancel(true);
                            this.getView().showErrorNotification(areaType.getString("name") + "区域的第" + (subEntryIndex + 1) + "行分录指标描述为必填!");
                            return;
                        }
                        if (StringUtils.isBlank(ratingStandard) && !list.contains(areaTypeNumber)) {
                            args.setCancel(true);
                            this.getView().showErrorNotification(areaType.getString("name") + "区域的第" + (subEntryIndex + 1) + "行分录评分标准为必填!");
                            return;
                        }
                        if (StringUtils.isNotBlank(metricName)) {
                            // 获取当前时间戳
                            long timestamp = System.currentTimeMillis();
                            // 将 metricName 和时间戳组合
                            String metricLogValue = metricName + "+" + timestamp;

                            this.getModel().setValue("tdkw_metric_log", metricLogValue, subEntryIndex, hideEntryIndex);
                        }
                    }
                    else {
                        if (superOrg == null) {
                            args.setCancel(true);
                            this.getView().showErrorNotification(areaType.getString("name") + "区域的第" + (subEntryIndex + 1) + "行分录上级组织为必填!");
                            return;
                        }
                    }
                }
            }
            Long billId = (Long) this.getModel().getValue("id");
            if (billId != 0L) {
                DynamicObjectCollection modelEntry = this.getModel().getEntryEntity("tdkw_hideentry");
                DynamicObject orgPerfBill = BusinessDataServiceHelper.loadSingle("tdkw_org_perf_metrics", new QFilter[]{new QFilter("id", QCP.equals, billId)});
                if (ObjectUtils.isNotEmpty(orgPerfBill)) {
                    //父分录
                    DynamicObjectCollection dataBaseEntry = orgPerfBill.getDynamicObjectCollection("tdkw_hideentry");
                    //数据库中 父分录中kpi类型的子分录中指标的id集合
                    List<Long> dataBaseIds = dataBaseEntry.stream().filter(i -> i.getString("tdkw_area_type.number").equals("T0001")).flatMap(i -> i.getDynamicObjectCollection("tdkw_subentryentity").stream()).map(i -> i.getLong("tdkw_metric.id")).collect(Collectors.toList());
                    //页面数据 父分录中kpi类型的子分录中指标的id集合
                    List<Long> modelIds = modelEntry.stream().filter(i -> i.getString("tdkw_area_type.number").equals("T0001")).flatMap(i -> i.getDynamicObjectCollection("tdkw_subentryentity").stream()).map(i -> i.getLong("tdkw_metric.id")).collect(Collectors.toList());
                    //数据交集
                    Collection<Long> intersection = CollectionUtils.intersection(modelIds, dataBaseIds);
                    //需要删除的数据
                    Collection<Long> delCollection = CollectionUtils.subtract(dataBaseIds, intersection);
                    deleteLocalAlignment(delCollection);
                }
            }
            if (StringUtils.equals("submit", formOperate.getOperateKey())) {
                boolean notAlign = alignNotDecompose();
                if (notAlign) {
                    this.getView().showErrorNotification("未对齐上级已分解的指标，请先对齐再提交");
                    args.setCancel(true);
                    return;
                }
                // 获取父页面所有分录的权重总和
                BigDecimal parentTotalWeight = BigDecimal.ZERO;
                DynamicObjectCollection hideentry = this.getModel().getEntryEntity("tdkw_hideentry");
                for (DynamicObject parentRow : hideentry) {
                    DynamicObjectCollection subentryentity = parentRow.getDynamicObjectCollection("tdkw_subentryentity");
                    for (DynamicObject row : subentryentity) {
                        String parentWeight = row.getString("tdkw_weight");
                        if (StringUtils.isNotBlank(parentWeight)) {
                            BigDecimal parentWeightValue = new BigDecimal(parentWeight);
                            parentTotalWeight = parentTotalWeight.add(parentWeightValue);
                        }
                    }
                }

                if (parentTotalWeight.compareTo(BigDecimal.valueOf(100)) != 0) {
                    this.getView().showErrorNotification("所有指标权重和需为100%");
                    args.setCancel(true);
                    return;
                }
            }
        }
        else if (StringUtils.equals("edit_metrics", formOperate.getOperateKey())) {
            formOperate.getOption().setVariableValue(OperateOptionConst.ISSHOWMESSAGE, "false");
        }
    }

    private boolean alignNotDecompose() {

        Long targetBillId = (Long) this.getModel().getValue("id");
        if (targetBillId == 0L) {
            return false;
        }
        //有无kpi区域
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_hideentry");
        List<DynamicObject> list = entryEntity.stream().filter(o -> StringUtils.equals(o.getString("tdkw_area_type.number"), "T0001")).collect(Collectors.toList());
        if (list == null || list.size() == 0) {
            return false;
        }
        DynamicObject org = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
        long orgId = org.getLong("id");
        //Long orgId = adminorg.getLong("tdkw_adminorg.id");
        Date year = (Date) this.getModel().getValue("tdkw_assess_year");
        //获取指标单对应的对齐单的分录中 所有未对齐指标的id
        DynamicObject alignment = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment", new QFilter[]{new QFilter("tdkw_targetid", QCP.equals, targetBillId)});
        List<Long> notAlignmentTarget = new ArrayList<>();
        List<Long> alignmentTarget = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(alignment)) {
            DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
            if (ObjectUtils.isNotEmpty(dynamicObjectCollection)) {
                notAlignmentTarget = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_alignmentstatus").equals("20")).map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
                alignmentTarget = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_alignmentstatus").equals("10")).map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
            }
        }
        DynamicObject[] decompose = BusinessDataServiceHelper.load("tdkw_target_decompose", "tdkw_parentadminorg,tdkw_entryentity,tdkw_entryentity.tdkw_target," +
                        "tdkw_entryentity.tdkw_target.tdkw_metric_type.id,tdkw_entryentity.tdkw_subentryentity,tdkw_subentryentity.tdkw_decomposedesc,tdkw_subentryentity.tdkw_bedecomposeadmin",
                new QFilter[]{new QFilter("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.equals, orgId).and("tdkw_org_year", QCP.equals, year)});
        if (ObjectUtils.isNotEmpty(decompose)) {
            //所有子分录中包含当前组织的分解单数据  排除非本组织的和已经对齐的
            for (DynamicObject bill : decompose) {
                //分解单父单据体
                DynamicObjectCollection billParentEntry = bill.getDynamicObjectCollection("tdkw_entryentity");
                if (ObjectUtils.isNotEmpty(billParentEntry)) {
                    for (DynamicObject dynamicObject : billParentEntry) {
                        //分解单子单据体
                        DynamicObjectCollection billSonEntry = dynamicObject.getDynamicObjectCollection("tdkw_subentryentity");
                        if (ObjectUtils.isNotEmpty(billSonEntry)) {
                            for (DynamicObject object : billSonEntry) {
                                Long adminId = object.getLong("tdkw_bedecomposeadmin.id");
                                Long targetId = dynamicObject.getLong("tdkw_target.id");
                                //校验分解单 分解组织为当前组织  且对齐单数据中不包含该指标
                                if (orgId == adminId && check(notAlignmentTarget, alignmentTarget, targetId)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean check(List<Long> notAlignmentTarget, List<Long> alignmentTarget, Long targetId) {
        if (CollectionUtils.isEmpty(notAlignmentTarget) && CollectionUtils.isEmpty(alignmentTarget)) {
            return true;
        }
        else {
            return notAlignmentTarget.contains(targetId) && !alignmentTarget.contains(targetId);
        }

    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        // 提交
        if (StringUtils.equals("submit", formOperate.getOperateKey())
                && args.getOperationResult() != null
                && args.getOperationResult().isSuccess()) {
            this.getView().updateView("tdkw_audit_billid");

            long auditBillid = (long) this.getModel().getValue("tdkw_audit_billid");
            Object pkId = this.getModel().getValue("id");

            //附件处理
            List<Map<String, Object>> attachments = AttachmentServiceHelper.getAttachments(EntityName.BILL_ORG_PERF_METRICS, pkId, "attachmentpanel", true);
            for (Map<String, Object> attachment : attachments) {
                attachment.put("lastModified", null);
                String relativeUrl = (String) attachment.get("relativeUrl");
                attachment.put("url", relativeUrl);
            }

            AttachmentServiceHelper.upload(EntityName.BILL_ORG_TARGET_AUDIT, auditBillid, "attachmentpanel", attachments);

            OperationResult operationResult = OperationServiceHelper.executeOperate(OperationConst.SUBMIT, EntityName.BILL_ORG_TARGET_AUDIT, new Object[]{auditBillid}, OperateOption.create());
            if (operationResult.isSuccess()) {
                BillShowParameter parameter = new BillShowParameter();
                parameter.setFormId(EntityName.BILL_ORG_TARGET_AUDIT);
                parameter.setPkId(auditBillid);
                parameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                this.getView().showForm(parameter);
            }
            drawMiddleAreas();
        }
        else if (StringUtils.equals("save", formOperate.getOperateKey())
                && args.getOperationResult() != null
                && args.getOperationResult().isSuccess()) {
            afterSaveUpdateData();
        }
        // 撤回
        else if (StringUtils.equals("unsubmit", formOperate.getOperateKey())
                && args.getOperationResult() != null
                && args.getOperationResult().isSuccess()) {

            long auditBillid = (long) this.getModel().getValue("tdkw_audit_billid");
            long id = (long) this.getModel().getValue("id");

            if (auditBillid != 0L) {
                // 构建撤回操作的参数
                OperationResult operationResult = OperationServiceHelper.executeOperate(OperationConst.UNSUBMIT, EntityName.BILL_ORG_TARGET_AUDIT, new Object[]{auditBillid}, OperateOption.create());
                if (operationResult.isSuccess()) {
                    QFilter filter = new QFilter("tdkw_audit_billid", QCP.equals, auditBillid);
                    filter.and("id", QCP.not_equals, id);
                    DynamicObject[] needUnSubmitBill = BusinessDataServiceHelper.load(EntityName.BILL_ORG_PERF_METRICS, "id,billno,billstatus", filter.toArray());
                    // 更新撤回后单据的状态为"A"
                    for (DynamicObject bill : needUnSubmitBill) {
                        bill.set("billstatus", "A");
                    }
                    // 保存更新后的状态
                    SaveServiceHelper.save(needUnSubmitBill);
                }
            }
            drawMiddleAreas();
        }
        else if (StringUtils.equals("edit_metrics", formOperate.getOperateKey())
                && args.getOperationResult().isSuccess()) {
            drawMiddleAreas();
        }
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);
        Control source = (Control) evt.getSource();
        if (StringUtils.equals("tdkw_add_area", source.getKey()) || StringUtils.equals("tdkw_labelap", source.getKey()) || StringUtils.equals("tdkw_vectorap", source.getKey())) {
            Object adminOrg = this.getModel().getValue("tdkw_adminorg");
            Object assessYear = this.getModel().getValue("tdkw_assess_year");
            if (adminOrg == null) {
                this.getView().showErrorNotification("请先填写所属组织");
                evt.setCancel(true);
                return;
            }
            if (assessYear == null) {
                this.getView().showErrorNotification("请先填写考核年份");
                evt.setCancel(true);
            }
        }
    }

    /**
     * 保存操作之后更新分解单数据
     */
    private void afterSaveUpdateData() {
        Long id = (Long) this.getModel().getValue("id");
        if (id != 0L) {
            DynamicObject orgPerfBill = BusinessDataServiceHelper.loadSingle("tdkw_org_perf_metrics", new QFilter[]{new QFilter("id", QCP.equals, id)});
            if (ObjectUtils.isNotEmpty(orgPerfBill)) {
                //父分录
                DynamicObjectCollection entryEntity = orgPerfBill.getDynamicObjectCollection("tdkw_hideentry");
                //父分录中kpi类型的子分录中指标的id集合
                List<Long> perfTargetIds = entryEntity.stream().filter(i -> i.getString("tdkw_area_type.number").equals("T0001")).flatMap(i -> i.getDynamicObjectCollection("tdkw_subentryentity").stream()).map(i -> i.getLong("tdkw_metric.id")).collect(Collectors.toList());
                Map<Long, DynamicObject> targetMap = entryEntity.stream().filter(i -> i.getString("tdkw_area_type.number").equals("T0001")).flatMap(i -> i.getDynamicObjectCollection("tdkw_subentryentity").stream()).map(i -> i.getDynamicObject("tdkw_metric")).collect(Collectors.toMap(i -> i.getLong("id"), i -> i, (existing, replacement) -> {
                    // 如果 ID 重复，决定使用哪个值，这里选择保留第一个
                    return existing;
                }));

                //指标单对应的分解单
                DynamicObject decomposeBill = BusinessDataServiceHelper.loadSingle("tdkw_target_decompose", new QFilter[]{new QFilter("tdkw_targetid", QCP.equals, id)});
                //分解单无数据不处理  只处理分解单有数据且指标单指标增删改的情况
                if (CollectionUtils.isNotEmpty(perfTargetIds)) {
                    if (ObjectUtils.isNotEmpty(decomposeBill)) {
                        //分解单的父分录的指标id集合
                        DynamicObjectCollection decomposeBillParentEntry = decomposeBill.getDynamicObjectCollection("tdkw_entryentity");
                        List<Long> decomposeTargetIds = decomposeBillParentEntry.stream().map(i -> i.getLong("tdkw_target.id")).collect(Collectors.toList());
                        if (decomposeTargetIds.isEmpty()) {
                            //有分解单但是无数据直接增加  对齐单不需要处理
                            for (Long l : targetMap.keySet()) {
                                DynamicObject target = targetMap.get(l);
                                DynamicObject row = decomposeBillParentEntry.addNew();
                                row.set("tdkw_target", target);
                                row.set("tdkw_target_name", target.getString("name"));
                                row.set("tdkw_findctdesc", target.getString("tdkw_rating_standard"));
                                row.set("tdkw_rating_standard", target.getString("tdkw_rating_standard"));
                            }
                            //保存分解单
                            SaveServiceHelper.save(new DynamicObject[]{decomposeBill});
                        }
                        else {
                            //分解单有数据 取指标指标的交集和分解单指标的交集
                            //删除分解单指标数据和交集的差集 和对应的下级对齐单分录的数据
                            //分解单增加 指标单指标数据和交集的差集

                            //分解单和指标单指标的数据交集
                            Collection<Long> intersection = CollectionUtils.intersection(perfTargetIds, decomposeTargetIds);
                            //指标单指标数据和交集之间的差集  需要添加至分解单
                            Collection<Long> addCollection = CollectionUtils.subtract(perfTargetIds, intersection);
                            //分解单和交集的差集 需要删除的数据
                            Collection<Long> delCollection = CollectionUtils.subtract(decomposeTargetIds, intersection);
                            //先删除分解单对应的下级组织对其单的数据   对齐单分录数据对应的分解单数据
                            //deleteDate(delCollection,decomposeBill.getLong("id"));
                            for (int i = decomposeBillParentEntry.size() - 1; i >= 0; i--) {
                                DynamicObject row = decomposeBillParentEntry.get(i);
                                long targetId = row.getLong("tdkw_target.id");
                                if (delCollection.contains(targetId)) {
                                    decomposeBillParentEntry.remove(i);
                                }
                            }
                            for (Long l : addCollection) {
                                DynamicObject target = targetMap.get(l);
                                DynamicObject row = decomposeBillParentEntry.addNew();
                                row.set("tdkw_target", target);
                                row.set("tdkw_target_name", target.getString("name"));
                                row.set("tdkw_findctdesc", target.getString("tdkw_rating_standard"));
                                row.set("tdkw_rating_standard", target.getString("tdkw_rating_standard"));
                            }
                            //删除本单对应的对齐单里面已对齐数据的指标
                            SaveServiceHelper.save(new DynamicObject[]{decomposeBill});
                        }
                    }
                }
                else {
                    if (ObjectUtils.isNotEmpty(decomposeBill)) {
                        DynamicObjectCollection dynamicObjectCollection = decomposeBill.getDynamicObjectCollection("tdkw_entryentity");
                        dynamicObjectCollection.clear();
                        //删除本单对应的对齐单里面已对齐数据的指标
                        SaveServiceHelper.save(new DynamicObject[]{decomposeBill});
                    }
                }
            }
        }
    }

    /**
     * 基础资料查询事件，查不到或者查到多个时通知业务筛选正确的id
     *
     * @param e
     */
    @Override
    public void queryImportBasedata(QueryImportBasedataEventArgs e) {
        Map<BasedataItem, List<Object>> searchResult = e.getSearchResult();
        for (Map.Entry<BasedataItem, List<Object>> entry : searchResult.entrySet()) {
            if (entry.getKey().getEntityNumber().equals("haos_adminorghr")) {
                QFilter orgFilter = new QFilter(entry.getKey().getSearchKey(), QCP.equals, entry.getKey().getSearchValue());
                orgFilter.and("iscurrentversion", QCP.equals, "1"); // 当前版本
                orgFilter.and("datastatus", QCP.equals, "1"); // 生效中
                orgFilter.and("status", QCP.equals, "C");

                DynamicObject orgObject = QueryServiceHelper.queryOne("haos_adminorghr", "id", orgFilter.toArray());
                long id = orgObject.getLong("id");
                List<Object> basedata = entry.getValue();
                // 使用 Iterator 遍历并移除不等于 id 的元素
                Iterator<Object> iterator = basedata.iterator();
                while (iterator.hasNext()) {
                    Object basedataItem = iterator.next();
                    if (basedataItem instanceof Long) {
                        if ((Long) basedataItem != id) {
                            iterator.remove();  // 移除不等于 id 的元素
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除所有本单对应的分解单 分解给下级组织的数据
     */
    private void deleteDate(Collection<Long> delCollection, Long billId) {
        DynamicObject[] alignmentBills = BusinessDataServiceHelper.load("tdkw_target_alignment", "id,tdkw_targetalientry.tdkw_decomposebillid,tdkw_targetalientry.tdkw_findctid", new QFilter[]{new QFilter("tdkw_targetalientry.tdkw_decomposebillid", QCP.equals, billId)});
        if (ObjectUtils.isNotEmpty(alignmentBills)) {
            for (DynamicObject alignmentBill : alignmentBills) {
                DynamicObjectCollection dynamicObjectCollection = alignmentBill.getDynamicObjectCollection("tdkw_targetalientry");
                if (CollectionUtils.isNotEmpty(dynamicObjectCollection)) {
                    for (int i = dynamicObjectCollection.size() - 1; i >= 0; i--) {
                        DynamicObject row = dynamicObjectCollection.get(i);
                        long decomposeBillId = row.getLong("tdkw_decomposebillid");
                        long targetId = row.getLong("tdkw_findctid.id");
                        //当前行的指标id为分解单和交集的差集之一 并且来源分解单id为本指标单对应的分级单
                        if (billId.equals(decomposeBillId) && delCollection.contains(targetId)) {
                            dynamicObjectCollection.remove(i);
                        }
                    }
                }
            }
            SaveServiceHelper.save(alignmentBills);
        }
    }

    /**
     * 删除本指标单对应对齐单分录中已对齐状态分录数据 对应的对齐指标
     */
    private void deleteLocalAlignment(Collection<Long> delCollection) {
        Long localId = (Long) this.getModel().getValue("id");
        //对齐单
        DynamicObject alignmentBill = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment", new QFilter[]{new QFilter("tdkw_targetid", QCP.equals, localId)});
        if (ObjectUtils.isNotEmpty(alignmentBill)) {
            //对齐单分录
            DynamicObjectCollection entry = alignmentBill.getDynamicObjectCollection("tdkw_targetalientry");
            if (ObjectUtils.isNotEmpty(entry)) {
                //未对齐状态没有已对齐指标数据
                for (DynamicObject row : entry) {
                    //已对其指标（多选基础资料）
                    DynamicObjectCollection dynamicObjectCollection = row.getDynamicObjectCollection("tdkw_alignmentid");
                    int size = dynamicObjectCollection.size();
                    //循环外置变量获取删除行的下标数据 用于移除对应指标的描述，权重，目标值，单位
                    ArrayList<Integer> listRemove = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(dynamicObjectCollection)) {
                        for (int i = dynamicObjectCollection.size() - 1; i >= 0; i--) {
                            DynamicObject target = dynamicObjectCollection.get(i);
                            //如果分解单中需要删除的指标id包含对齐单已对其的指标id 则移除该指标数据
                            if (delCollection.contains(target.getDynamicObject("fbasedataid").getLong("id"))) {
                                dynamicObjectCollection.remove(i);
                                listRemove.add(i);
                            }
                        }
                    }
                    if (listRemove.size() == size) {
                        row.set("tdkw_alignmentdesc", null);
                        row.set("tdkw_alignmentweight", null);
                        row.set("tdkw_alignmenttarget", null);
                        row.set("tdkw_unit", null);
                        row.set("tdkw_alignmentstatus", "20");
                    }
                    else {
                        row.set("tdkw_alignmentdesc", deleteString(row.getString("tdkw_alignmentdesc"), listRemove));
                        row.set("tdkw_alignmentweight", deleteString(row.getString("tdkw_alignmentweight"), listRemove));
                        row.set("tdkw_alignmenttarget", deleteString(row.getString("tdkw_alignmenttarget"), listRemove));
                        row.set("tdkw_unit", deleteString(row.getString("tdkw_unit"), listRemove));

                    }
                }
            }
            SaveServiceHelper.save(new DynamicObject[]{alignmentBill});
        }
    }

    private String deleteString(String str, ArrayList<Integer> listRemove) {
        String[] strings = str.split(";");
        StringBuffer s = new StringBuffer();
        for (int i = strings.length - 1; i >= 0; i--) {
            if (!listRemove.contains(i)) {
                s.append(strings[i]).append(";");
            }
        }
        return s.toString();
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        //点击添加区域
        if (StringUtils.equals("tdkw_add_area", source.getKey()) || StringUtils.equals("tdkw_labelap", source.getKey()) || StringUtils.equals("tdkw_vectorap", source.getKey())) {
            FormShowParameter param = new FormShowParameter();
            DynamicObjectCollection hideEntry = this.getModel().getEntryEntity("tdkw_hideentry");
            List<String> typeNumberList = hideEntry.stream().map(o -> o.getString("tdkw_area_type.number")).collect(Collectors.toList());
            param.setCustomParam("typeNumberList", typeNumberList);
            DynamicObject org = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
            String orgTypeNumber = org.getString("orgtype.number");
            param.setCustomParam("orgType", orgTypeNumber);
            param.setFormId(EntityName.FORM_ADD_AREA);
            param.getOpenStyle().setShowType(ShowType.Modal);
            // 设置回调属性
            param.setCloseCallBack(new CloseCallBack(this, "add_area"));
            this.getView().showForm(param);
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent event) {
        super.closedCallBack(event);

        if (StringUtils.equals(event.getActionId(), "add_area")
                && event.getReturnData() != null) {
            DynamicObject returnData = (DynamicObject) event.getReturnData();
            // kpi类  tdkw_orgtarget_kpi
            int row = this.getModel().createNewEntryRow("tdkw_hideentry");
            this.getModel().setValue("tdkw_area_type", returnData.getPkValue(), row);
            this.getPageCache().put("saveMessage", "not");
            OperationResult operationResult = this.getView().invokeOperation("save");
            if (operationResult.isSuccess()) {
                //绘制中间区域
                drawMiddleAreas();
            }
            else {
                this.getView().showErrorNotification(getOperationResultErrorInfos(operationResult));
                this.getModel().deleteEntryRow("tdkw_hideentry", row);
            }
        }
    }

    /*
     * 绘制中间区域
     */
    private void drawMiddleAreas() {
        FlexPanelAp areaShowAp = this.getShowAp("tdkw_addflexpanelap");//flex容器
        DynamicObjectCollection entryentity = this.getModel().getEntryEntity("tdkw_hideentry");//单据体标识：借阅内容隐藏分录
        int index = 0;
        String formId = "tdkw_inner_area";
        String billStatus = (String) this.getModel().getValue("billstatus");
        DynamicObject adminOrg = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
        String orgName = "";
        if (adminOrg != null) {
            orgName = adminOrg.getString("name");
        }

        for (Iterator<DynamicObject> var5 = entryentity.iterator(); var5.hasNext(); ++index) {
            DynamicObject row = var5.next();
            Long typeId = row.getLong("tdkw_area_type.id");
            String typeName = row.getString("tdkw_area_type.name");
            DynamicObjectCollection rows = row.getDynamicObjectCollection("tdkw_subentryentity");
            this.showArea(areaShowAp, index, formId, billStatus, typeName, typeId, rows, orgName);
        }
        //更新元数据，(key,Value)
        this.getView().updateControlMetadata(areaShowAp.getKey(), areaShowAp.createControl());
    }

    /*
     * 显示区域
     */
    private void showArea(FlexPanelAp areaShowAp, int index, String formId, String billStatus, String typeName, Long typeId, DynamicObjectCollection rows, String orgName) {
        String targetKey = areaShowAp.getKey() + System.currentTimeMillis();
        // 创建一个flex容器
        FlexPanelAp cardAp = new FlexPanelAp();
        cardAp.setKey(targetKey);
        // 创建一个内嵌页面
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId(formId);
        showParameter.getOpenStyle().setShowType(ShowType.InContainer);
        showParameter.getOpenStyle().setTargetKey(targetKey);
        showParameter.setCustomParam("index", index);
        showParameter.setCustomParam("billStatus", billStatus);
        if (StringUtils.isNotBlank(orgName)) {
            showParameter.setCustomParam("orgName", orgName);
        }
        if (!billStatus.equals("A")) {
            showParameter.setStatus(OperationStatus.EDIT);
        }
        showParameter.setCustomParam("typeId", typeId);
        showParameter.setCustomParam("typeName", typeName);

        showParameter.setCustomParam("rows", rows);
        this.getView().showForm(showParameter);
        areaShowAp.getItems().add(cardAp);
    }

    /*
     * 获取flex容器
     */
    private FlexPanelAp getShowAp(String key) {
        FlexPanelAp flexPanelAp = new FlexPanelAp();
        flexPanelAp.setKey(key);
        flexPanelAp.setDirection("column");
        flexPanelAp.setAlignItems("stretch");
        flexPanelAp.setJustifyContent("flex-start");
        flexPanelAp.setWrap(false);
        flexPanelAp.setGrow(1);
        flexPanelAp.setShrink(0);
        return flexPanelAp;
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String fieldName = e.getProperty().getName();
        Object oldValue = e.getChangeSet()[0].getOldValue();
        DynamicObjectCollection parentEntry = this.getModel().getEntryEntity("tdkw_hideentry");
        // 判断是否是 tdkw_adminorg 或 tdkw_assess_year 字段且 oldValue 不为空
        if ((StringUtils.equals("tdkw_adminorg", fieldName) || StringUtils.equals("tdkw_assess_year", fieldName)) && oldValue != null) {
            IPageCache pageCache = this.getView().getPageCache();
            String ignoreChanged = pageCache.get("ignoreChanged");
            if (StringUtils.isNotBlank(ignoreChanged)) {
                pageCache.remove("ignoreChanged");
                return;
            }
            if (!parentEntry.isEmpty()) {
                String oldValueStr = "";
                if (StringUtils.equals("tdkw_adminorg", fieldName)) {
                    // 缓存组织的 ID
                    oldValueStr = String.valueOf(((DynamicObject) oldValue).getLong("id"));
                }
                else if (StringUtils.equals("tdkw_assess_year", fieldName)) {
                    // 缓存年份的字符串表示
                    oldValueStr = oldValue.toString();
                }

                // 将旧值存入页面缓存
                pageCache.put(fieldName + "_oldValue", oldValueStr);  // 缓存旧值
                pageCache.put(fieldName + "_newValue", oldValueStr);  // 缓存旧值

                // 弹出确认提示
                ConfirmCallBackListener confirmCallBacks = new ConfirmCallBackListener("clearDataOp", this);
                String confirmTip = "修改所属组织/考核年份后，已维护指标及其分解对齐关系将被清空，确认修改吗？";
                this.getView().showConfirm(confirmTip, "", MessageBoxOptions.OKCancel, ConfirmTypes.Default, confirmCallBacks, null, fieldName);
            }
            else {
                if (e.getChangeSet()[0].getNewValue() == null) {
                    return;
                }
                if (StringUtils.equals("tdkw_adminorg", fieldName)) {
                    DynamicObject adminOrg = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
                    this.createAreaBj(adminOrg);
                }
            }
            if (StringUtils.equals("tdkw_adminorg", fieldName)) {
                if (e.getChangeSet()[0].getNewValue() == null) {
                    return;
                }
                DynamicObject dataEntity = this.getModel().getDataEntity();

                DynamicObject adminOrg = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
                setIndustry(dataEntity, adminOrg, Collections.singletonList(getTypeNumber("constant.hr.haos.haos_adminorgtype.industrynumber", "XY00003")), "tdkw_industry_name");
                setIndustry(dataEntity, adminOrg, Arrays.asList("XY00006", "XY00007", "XY00008"), "tdkw_department_name");

                this.getView().updateView("tdkw_industry_name");
                this.getView().updateView("tdkw_department_name");

                // kpi类  tdkw_orgtarget_kpi
            }
            DynamicObject org = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
            if (ObjectUtils.isNotEmpty(org)) {
                Label label = this.getView().getControl("tdkw_labelap1");
                label.setText(org.getString("name") + "存在上级已分解且未对齐的指标，");
            }
        }
        else if ((StringUtils.equals("tdkw_adminorg", fieldName) && oldValue == null)) {
            if (e.getChangeSet()[0].getNewValue() == null) {
                return;
            }
            if (StringUtils.equals("tdkw_adminorg", fieldName)) {
                DynamicObject adminOrg = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
                this.createAreaBj(adminOrg);
            }
        }
    }

    private void createAreaBj(DynamicObject adminOrg) {
        //本级公司组织绩效
        DynamicObject type = BusinessDataServiceHelper.loadSingle("tdkw_metric_type", new QFilter[]{new QFilter("number", QCP.equals, "T0005")});
        DynamicObject org = BusinessDataServiceHelper.loadSingle("haos_adminorghr", new QFilter[]{new QFilter("id", QCP.equals, adminOrg.getLong("id"))});
        String orgType = org.getString("orgtype.number");
        if (!ORGTYPES.contains(orgType)) {
            DynamicObjectCollection hideEntry = this.getModel().getDataEntity(true).getDynamicObjectCollection("tdkw_hideentry");
            hideEntry.removeIf(m -> m.getLong("tdkw_area_type.id") == type.getLong("id"));
//                drawMiddleAreas();

            int row = this.getModel().createNewEntryRow("tdkw_hideentry");
            this.getModel().setValue("tdkw_area_type", type.getLong("id"), row);
            //绘制中间区域
            drawMiddleAreas();
        }
        else {
            DynamicObjectCollection hideEntry = this.getModel().getDataEntity(true).getDynamicObjectCollection("tdkw_hideentry");
            hideEntry.removeIf(m -> m.getLong("tdkw_area_type.id") == type.getLong("id"));
            drawMiddleAreas();
        }
    }

    // 回调处理
    @Override
    public void confirmCallBack(MessageBoxClosedEvent evt) {
        String callBackId = evt.getCallBackId();
        MessageBoxResult result = evt.getResult();

        if (StringUtils.equals("clearDataOp", callBackId)) {
            if (result == MessageBoxResult.Yes) {
                this.getModel().deleteEntryData("tdkw_hideentry");
                DynamicObject type = BusinessDataServiceHelper.loadSingle("tdkw_metric_type", new QFilter[]{new QFilter("number", QCP.equals, "T0005")});
                DynamicObject org = BusinessDataServiceHelper.loadSingle("haos_adminorghr", new QFilter[]{new QFilter("id", QCP.equals,
                        this.getModel().getDataEntity(true).getLong("tdkw_adminorg.id"))});
                if (ObjectUtils.isNotEmpty(org)) {
                    String orgType = org.getString("orgtype.number");
                    if (!ORGTYPES.contains(orgType)) {
                        int row = this.getModel().createNewEntryRow("tdkw_hideentry");
                        this.getModel().setValue("tdkw_area_type", type.getLong("id"), row);
                    }
                }

                drawMiddleAreas();
                long id = this.getModel().getDataEntity().getLong("id");
                if (id != 0L) {
                    QFilter qFilter = new QFilter("tdkw_targetid", QCP.equals, id);
                    DeleteServiceHelper.delete(EntityName.BILL_TARGET_DECOMPOSE, qFilter.toArray());
                    DeleteServiceHelper.delete(EntityName.BILL_TARGET_ALIGNMENT, qFilter.toArray());
                }

            }
            else if (result == MessageBoxResult.Cancel) {
                // 用户取消修改，恢复旧值
                IPageCache pageCache = this.getView().getPageCache();
                String fieldName = evt.getCustomVaule();
                DynamicObject dataEntity = this.getModel().getDataEntity();
                Object oldValue = pageCache.get(fieldName + "_oldValue");
                pageCache.put("ignoreChanged", "true");
                if ("tdkw_assess_year".equals(fieldName)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                    try {
                        oldValue = sdf.parse(oldValue.toString());
                    } catch (ParseException e) {
                        throw new KDBizException(e.getMessage());
                    }

                }
                else {
                    oldValue = Long.valueOf(oldValue.toString());
                }
                this.getModel().setValue(fieldName, oldValue);

                if (StringUtils.equals("tdkw_adminorg", fieldName)) {

                    DynamicObject adminOrg = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
                    setIndustry(dataEntity, adminOrg, Collections.singletonList(getTypeNumber("constant.hr.haos.haos_adminorgtype.industrynumber", "XY00003")), "tdkw_industry_name");
                    setIndustry(dataEntity, adminOrg, Arrays.asList("XY00006", "XY00007", "XY00008"), "tdkw_department_name");

                    this.getView().updateView("tdkw_industry_name");
                    this.getView().updateView("tdkw_department_name");

                }
                DynamicObject org = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
                if (ObjectUtils.isNotEmpty(org)) {
                    Label label = this.getView().getControl("tdkw_labelap1");
                    label.setText(org.getString("name") + "存在上级已分解且未对齐的指标，");
                }

            }
        }
    }

    private void updateDate(Long targetBillId) {
        QFilter qFilter = new QFilter("tdkw_targetid", QCP.equals, targetBillId);
        DynamicObject alignment = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment", new QFilter[]{qFilter});
        if (ObjectUtils.isEmpty(alignment)) {
            return;
        }
        DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
        List<Long> alignmentTargetList = dynamicObjectCollection.stream().map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
        Date year = (Date) this.getModel().getValue("tdkw_assess_year");
        DynamicObject org = (DynamicObject) this.getModel().getValue("tdkw_adminorg");
        long orgId = org.getLong("id");
        //上级分解给本组织的数据
        DynamicObjectCollection query = QueryServiceHelper.query("tdkw_target_decompose", "id,tdkw_parentadminorg as orgid,tdkw_entryentity.tdkw_target.id as targetid," +
                        "tdkw_entryentity.tdkw_target.name as targetname,tdkw_entryentity.tdkw_target.tdkw_description as targetdesc,tdkw_entryentity.tdkw_target.tdkw_metric_type.id as typeid," +
                        "tdkw_entryentity.tdkw_subentryentity.tdkw_decomposedesc as decomposedesc",
                new QFilter[]{new QFilter("tdkw_org_year", QCP.equals, year).and("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.equals, orgId)});
        List<Long> decomposeTargetList = query.stream().map(i -> i.getLong("targetid")).collect(Collectors.toList());

        //交集
        Collection<Long> intersection = CollectionUtils.intersection(decomposeTargetList, alignmentTargetList);
        //需要增加的集合
        Collection<Long> addCollection = CollectionUtils.subtract(decomposeTargetList, intersection);
        //需要删除的集合
        Collection<Long> delCollection = CollectionUtils.subtract(alignmentTargetList, intersection);

        for (int i = dynamicObjectCollection.size() - 1; i >= 0; i--) {
            DynamicObject row = dynamicObjectCollection.get(i);
            long targetId = row.getLong("tdkw_findctid.id");
            if (delCollection.contains(targetId)) {
                dynamicObjectCollection.remove(i);
            }
        }

        for (DynamicObject dynamicObject : query) {
            if (addCollection.contains(dynamicObject.getLong("targetid"))) {
                DynamicObject newEntryRow = dynamicObjectCollection.addNew();
                newEntryRow.set("tdkw_decomposebillid", dynamicObject.getLong("id"));
                newEntryRow.set("tdkw_sourceadmin", dynamicObject.getLong("orgid"));
                newEntryRow.set("tdkw_findctid", dynamicObject.getLong("targetid"));
                newEntryRow.set("tdkw_findctid_name", dynamicObject.getString("targetname"));
                newEntryRow.set("tdkw_findcttype", dynamicObject.getLong("typeid"));
                newEntryRow.set("tdkw_findctdesc", dynamicObject.getString("targetdesc"));
                newEntryRow.set("tdkw_decomposedesc", dynamicObject.getString("decomposedesc"));
                newEntryRow.set("tdkw_alignmentstatus", "20");
            }
        }
        SaveServiceHelper.save(new DynamicObject[]{alignment});

    }

    @Override
    public void afterImportData(ImportDataEventArgs e) {
        super.afterImportData(e);

    }
}
