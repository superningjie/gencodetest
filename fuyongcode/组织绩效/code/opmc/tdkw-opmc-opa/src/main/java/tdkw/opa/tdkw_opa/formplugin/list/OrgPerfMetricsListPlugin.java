package tdkw.opa.tdkw_opa.formplugin.list;

import kd.bos.bill.BillShowParameter;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.PackageDataEvent;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.list.column.ColumnDesc;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.filter.FilterColumn;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.BeforeCreateListColumnsArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.FilterContainerInitArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.FormOperate;
import kd.bos.list.BillList;
import kd.bos.list.IListColumn;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mutex.DataMutex;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;
import tdkw.opa.tdkw_opa.formplugin.enums.OperationConst;
import tdkw.opa.tdkw_opa.formplugin.utils.HRRoleAndPersonUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OrgPerfMetricsListPlugin extends AbstractListPlugin implements BeforeF7SelectListener {

    private static final Log logger = LogFactory.getLog(OrgPerfMetricsListPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        // 侦听基础资料字段的事件
        BasedataEdit fieldEdit = this.getView().getControl("tdkw_metric_type");
        fieldEdit.addBeforeF7SelectListener(this);
    }

    @Override
    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);
        long currUserId = RequestContext.get().getCurrUserId();
        AuthorizedOrgResult orgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, EntityName.BILL_ORG_PERF_METRICS, "tdkw_adminorg");
        boolean hasAllOrgPerm = orgSet.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPermOrgs = orgSet.getHasPermOrgs();
            QFilter qFilter = new QFilter("tdkw_adminorg.id", QCP.in, hasPermOrgs);
            e.addCustomQFilter(qFilter);
        } else {
            logger.info("有所有组织权限hasAllOrgPerm" + hasAllOrgPerm);
        }

    }

    @Override
    public void filterContainerInit(FilterContainerInitArgs args) {
        List<FilterColumn> fastFilterColumns = args.getFastFilterColumns();
        fastFilterColumns.removeIf(column -> "billno".equals(column.getFieldName()));
        List<FilterColumn> commonFilterColumns = args.getCommonFilterColumns();
        commonFilterColumns.clear();
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String stateParamValue = this.getView().getFormShowParameter().getCustomParam("viewType");
        if (StringUtils.isNotBlank(stateParamValue) && !StringUtils.equals(stateParamValue, "org")) {
            // 隐藏按钮
            this.getView().setVisible(false, "tblnew", "tdkw_import", "tblsubmit", "tbldel", "tdkw_calculate_score", "tdkw_viewonelog");
            if (StringUtils.equals(stateParamValue, "company")) {
                this.getView().setEnable(false, "tdkw_metric_type");
            }
        } else {
            this.getView().setVisible(false, "tdkw_metric_type");
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        String stateParamValue = this.getView().getFormShowParameter().getCustomParam("viewType");
        if (StringUtils.equals(stateParamValue, "company")) {
            // 默认查看指标类型 T0001经营业绩指标（公司级-KPI类）
            QFilter qFilter = new QFilter("number", QCP.equals, "T0001");
            DynamicObject type = QueryServiceHelper.queryOne(EntityName.BASE_METRIC_TYPE, "id", qFilter.toArray());
            this.getModel().setValue("tdkw_metric_type", type.getLong("id"));
        }

    }

    /**
     * 在构建列表显示的列时触发，传入设计时预置的列集合
     *
     * @remark 在此事件，根据自定义参数值，动态添加列
     */
    @Override
    public void beforeCreateListColumns(BeforeCreateListColumnsArgs args) {
        String stateParamValue = this.getView().getFormShowParameter().getCustomParam("viewType");
        if (StringUtils.isNotBlank(stateParamValue)) {
            List<IListColumn> listColumns = args.getListColumns();
            // 找到ListFieldKey为"tdkw_adminorg.name"的列
            IListColumn adminOrgColumn = listColumns.stream()
                    .filter(column -> "tdkw_adminorg.name".equals(column.getListFieldKey()))
                    .findFirst()
                    .orElse(null);

            switch (stateParamValue) {
                case "org":
                    listColumns.removeIf(column -> !EntityName.ORG_VIEW_FIELD.contains(column.getListFieldKey()));
                    // 如果在org视图，设置超链接
                    if (adminOrgColumn != null) {
                        adminOrgColumn.setHyperlink(true);
                    }
                    break;
                case "company":
                    listColumns.removeIf(column -> !EntityName.COMPANY_VIEW_FIELD.contains(column.getListFieldKey()));
                    break;
                case "center":
                    listColumns.removeIf(column -> !EntityName.CENTER_VIEW_FIELD.contains(column.getListFieldKey()));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void packageData(PackageDataEvent evt) {
        String stateParamValue = this.getView().getFormShowParameter().getCustomParam("viewType");

        if (StringUtils.isNotBlank(stateParamValue) && !StringUtils.equals(stateParamValue, "org")) {
            //动态设置billno列显示为超链接
            evt.getNoLinkKey().add(((ColumnDesc) evt.getSource()).getKey());
        }

    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String fieldKey = e.getProperty().getName();
        // 定义关心的字段列表
        List<String> concernedFields = Arrays.asList("tdkw_assess_year", "tdkw_adminorg", "tdkw_metric_type");
        // 检查 fieldKey 是否在关心的字段列表中
        if (concernedFields.contains(fieldKey)) {
            BillList billlist = this.getControl("billlistap");
            // 创建一个列表来存储所有过滤条件
            List<QFilter> filters = new ArrayList<>();

            // 获取所有相关字段的值
            Date assessYearValue = (Date) this.getModel().getValue("tdkw_assess_year");
            DynamicObjectCollection adminOrgValue = (DynamicObjectCollection) this.getModel().getValue("tdkw_adminorg");
            DynamicObject metricTypeValue = (DynamicObject) this.getModel().getValue("tdkw_metric_type");

            // 处理年份过滤条件
            if (assessYearValue != null) {
                LocalDate localDate = assessYearValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int year = localDate.getYear();
                filters.add(new QFilter("YEAR(tdkw_assess_year)", QCP.equals, year));
            }

            // 处理组织过滤条件
            if (adminOrgValue != null && !adminOrgValue.isEmpty()) {
                List<Long> ids = adminOrgValue.stream()
                        .map(obj -> obj.getDynamicObject("fbasedataid"))
                        .filter(Objects::nonNull)
                        .map(fbaseDataId -> fbaseDataId.getLong("id"))
                        .collect(Collectors.toList());

                if (!ids.isEmpty()) {
                    filters.add(new QFilter("tdkw_adminorg.id", QCP.in, ids));
                }
            }

            // 处理指标类型过滤条件
            if (metricTypeValue != null) {
                filters.add(new QFilter("tdkw_hideentry.tdkw_area_type.id", QCP.equals, metricTypeValue.getLong("id")));
            }

            // 将所有过滤条件合并为一个
            QFilter combinedFilter = null;
            for (QFilter filter : filters) {
                if (combinedFilter == null) {
                    combinedFilter = filter;
                } else {
                    combinedFilter = combinedFilter.and(filter);
                }
            }

            // 如果有过滤条件，则设置过滤
            if (combinedFilter != null) {
                billlist.setFilter(combinedFilter);
            }

            // 刷新数据
            billlist.refreshData();
        }
    }


    @Override
    public void beforeF7Select(BeforeF7SelectEvent args) {
        String fieldKey = args.getProperty().getName();
        if (StringUtils.equals(fieldKey, "tdkw_metric_type")) {
            String stateParamValue = this.getView().getFormShowParameter().getCustomParam("viewType");
            if (StringUtils.isNotBlank(stateParamValue)) {
                switch (stateParamValue) {
                    case "company":
                        // 公司默认查看指标类型 T0001经营业绩指标（公司级-KPI类）
                        args.addCustomQFilter(new QFilter("number", QCP.equals, "T0001"));
                        break;
                    case "center":
                        // 中心默认只能查看的指标类型，T0002经营业绩指标（部门级-KPI类）、T0003重点工作类、T0004协同类
                        args.addCustomQFilter(new QFilter("number", QCP.in, new String[]{"T0002", "T0003","T0004"}));
                        break;
                    default:
                        break;

                }
            }
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        // 计算分数
        if (StringUtils.equals("calculate_score", formOperate.getOperateKey())) {
            IListView view = (IListView) this.getView();
            ListSelectedRowCollection selectedRows = view.getSelectedRows();
            QFilter idFilter = new QFilter("id", QCP.in, selectedRows.getPrimaryKeyValues());
            DynamicObject[] metrics = BusinessDataServiceHelper.load("tdkw_org_perf_metrics", "id,tdkw_assess_year", idFilter.toArray());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
            Set<String> assessYear = Arrays.stream(metrics).map(o -> simpleDateFormat.format(o.getDate("tdkw_assess_year"))).collect(Collectors.toSet());
            if (assessYear.size() > 1) {
                this.getView().showErrorNotification("请选择同一年份计算");
                args.setCancel(true);
                return;
            }
            for (ListSelectedRow listSelectedDatum : selectedRows) {
                String billStatus = listSelectedDatum.getBillStatus();
                if (!StringUtils.equals("C", billStatus)) {
                    args.setCancel(true);
                    this.getView().showErrorNotification("仅有已审核的信息支持计算分数，请检查勾选记录");
                    return;
                }
            }
            // 先判断 是否存在
            QFilter qFilter = new QFilter("tdkw_calc_org_score.tdkw_perf_metricsid", QCP.in, selectedRows.getPrimaryKeyValues());
            DynamicObject[] calculateScore = BusinessDataServiceHelper.load(EntityName.BILL_CALCULATE_SCORE, "id", qFilter.toArray());
            if (calculateScore == null || calculateScore.length == 0) {
                // 如果没有找到相关的计算分数记录，继续正常执行，不取消操作
                return;
            }
            // 判断有无互斥锁
            Map<String, String> lockInfo;
            List<Long> ids = new ArrayList<>();
            try (DataMutex dataMutex = DataMutex.create()) {
                // 遍历 calculateScore，检查互斥锁
                for (DynamicObject score : calculateScore) {
                    Long calculateScoreId = score.getLong("id"); // 获取 calculateScore 的 ID

                    // 获取锁定信息
                    lockInfo = dataMutex.getLockInfo(calculateScoreId.toString(), "default_netctrl", EntityName.BILL_CALCULATE_SCORE);

                    // 如果有锁定信息，提示并取消操作
                    if (lockInfo != null && !lockInfo.isEmpty()) {
                        this.getView().showErrorNotification("该组织正在计算中,请勿重复打开单据");
                        args.setCancel(true);
                        return;
                    }
                    ids.add(calculateScoreId);
                }
            } catch (Exception e) {
                logger.error("检查互斥锁时出错：", e);
                this.getView().showErrorNotification("检查互斥锁时发生错误，请稍后重试。");
                args.setCancel(true);
                return;
            }
            // 如果没有互斥锁，删除相关的单据
            try {
                DeleteServiceHelper.delete(calculateScore[0].getDataEntityType(), ids.toArray());
                logger.info("删除旧数据:", ids);
            } catch (Exception e) {
                logger.error("删除旧数据时发生错误：", e);
                args.setCancel(true);
            }

        } else if (StringUtils.equals("exportlist_expt", formOperate.getOperateKey())) {
            IListView view = (IListView) this.getView();
            ListSelectedRowCollection selectedRows = view.getSelectedRows();
            if (selectedRows.size() != 1) {
                args.setCancel(true);
                this.getView().showTipNotification("请选择一条记录引出");
            }
        } else if (StringUtils.equals("viewonelog", formOperate.getOperateKey())) {
            IListView view = (IListView) this.getView();
            ListSelectedRowCollection selectedRows = view.getSelectedRows();
            if (selectedRows.size() != 1) {
                args.setCancel(true);
                this.getView().showTipNotification("请选择一条组织记录查看日志！");
            }
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
            List<Object> pkIds = args.getOperationResult().getSuccessPkIds();
            Object pkId = pkIds.get(0);
            QFilter qFilter = new QFilter("id", QCP.equals, pkId);
            DynamicObject orgPerfMetrics = QueryServiceHelper.queryOne(EntityName.BILL_ORG_PERF_METRICS, "id,tdkw_audit_billid", qFilter.toArray());
            long auditBillid = orgPerfMetrics.getLong("tdkw_audit_billid");
            //附件处理
            Map<String, List<Map<String, Object>>> attachmentsMap = AttachmentServiceHelper.getAttachments(EntityName.BILL_ORG_PERF_METRICS, pkIds.toArray(), "attachmentpanel", true);
            List<Map<String, Object>> attachments = attachmentsMap.values()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

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
        }
        // 撤回
        else if (StringUtils.equals("unsubmit", formOperate.getOperateKey())
                && args.getOperationResult() != null
                && args.getOperationResult().isSuccess()) {
            List<Object> pkIds = args.getOperationResult().getSuccessPkIds();
            // 找到撤回的单据
            QFilter qFilter = new QFilter("id", QCP.in, pkIds);
            DynamicObjectCollection orgPerfMetrics = QueryServiceHelper.query(EntityName.BILL_ORG_PERF_METRICS, "id,tdkw_audit_billid", qFilter.toArray());

            // 创建一个 Set 存储审批单主键，避免重复
            Set<Long> auditBillIds = new HashSet<>();

            // 遍历找到的单据，收集审批单主键
            for (DynamicObject orgPerfMetric : orgPerfMetrics) {
                long auditBillId = orgPerfMetric.getLong("tdkw_audit_billid");
                if (auditBillId != 0L) {
                    auditBillIds.add(auditBillId);
                }
            }

            if (!auditBillIds.isEmpty()) {
                // 构建撤回操作的参数
                OperationResult operationResult = OperationServiceHelper.executeOperate(OperationConst.UNSUBMIT, EntityName.BILL_ORG_TARGET_AUDIT, auditBillIds.toArray(), OperateOption.create());
                if (operationResult.isSuccess()) {
                    QFilter filter = new QFilter("tdkw_audit_billid", QCP.in, auditBillIds);
                    filter.and("id", QCP.not_in, pkIds);
                    DynamicObject[] needUnSubmitBill = BusinessDataServiceHelper.load(EntityName.BILL_ORG_PERF_METRICS, "id,billno,billstatus", filter.toArray());
                    // 更新撤回后单据的状态为"A"
                    for (DynamicObject bill : needUnSubmitBill) {
                        bill.set("billstatus", "A");
                    }

                    // 保存更新后的状态
                    SaveServiceHelper.save(needUnSubmitBill);
                }
            }
        }
        // 查看日志
        else if (StringUtils.equals("viewonelog", formOperate.getOperateKey())
                && args.getOperationResult() != null
                && args.getOperationResult().isSuccess()) {

            String modifyBillId = String.valueOf(args.getOperationResult().getSuccessPkIds().get(0));

            FormShowParameter formShowParameter = new FormShowParameter();
            formShowParameter.setFormId(EntityName.FORM_CHANGE_LOG);
            formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
            formShowParameter.setCustomParam("modifybillid", modifyBillId);
            this.getView().showForm(formShowParameter);
        } else if (StringUtils.equals("refresh", formOperate.getOperateKey())) {

            String stateParamValue = this.getView().getFormShowParameter().getCustomParam("viewType");
            //组织/公司
            if (StringUtils.equals(stateParamValue, "org") || StringUtils.equals(stateParamValue, "company")) {
                this.getModel().setValue("tdkw_assess_year", null);
                this.getModel().setValue("tdkw_adminorg", null);
                //中心
            } else if (StringUtils.equals(stateParamValue, "center")) {
                this.getModel().setValue("tdkw_assess_year", null);
                this.getModel().setValue("tdkw_adminorg", null);
                this.getModel().setValue("tdkw_metric_type", null);
            }
        } else if (StringUtils.equals("delete", formOperate.getOperateKey())
                && args.getOperationResult() != null
                && args.getOperationResult().isSuccess()) {
            List<Object> pkIds = args.getOperationResult().getSuccessPkIds();
            DynamicObject[] decomposeList = BusinessDataServiceHelper.load("tdkw_target_decompose", "id", new QFilter("tdkw_targetid", QCP.in, pkIds).toArray());
            DynamicObject[] alignmentList = BusinessDataServiceHelper.load("tdkw_target_alignment", "id", new QFilter("tdkw_targetid", QCP.in, pkIds).toArray());
            List<Long> decomposeIdList = Arrays.stream(decomposeList).map(o -> o.getLong("id")).collect(Collectors.toList());
            List<Long> alignmentIdList = Arrays.stream(alignmentList).map(o -> o.getLong("id")).collect(Collectors.toList());
            if (decomposeIdList.size() > 0) {
                int deleteDecompose = DeleteServiceHelper.delete("tdkw_target_decompose", new QFilter("id", QCP.in, decomposeIdList).toArray());
            }
            if (  alignmentIdList.size() > 0){
                int deleteAlignment = DeleteServiceHelper.delete("tdkw_target_alignment", new QFilter("id", QCP.in, alignmentIdList).toArray());
            }
        }
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        IFormView parentView = this.getView().getParentView();
        if (null != parentView) {
            if (parentView.getEntityId().equals(EntityName.FORM_ORG_LIST_VIEW)) {
                parentView.close();
                this.getView().sendFormAction(parentView);
            }
        }
    }


}
