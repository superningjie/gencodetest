package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.AfterAddRowEventArgs;
import kd.bos.entity.datamodel.events.AfterDeleteRowEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IClientViewProxy;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.SubEntryGrid;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListFilterParameter;
import kd.bos.list.ListShowParameter;
import kd.bos.mvc.form.ClientViewProxy;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.hrmp.hrpi.mservice.HRPIWorkRoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static tdkw.opa.tdkw_opa.formplugin.enums.EntityName.unitMap;

public class InnerAreaFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {


    private static Long getParentOrg(Long adminorgId) {
        // 标准化处理 替换项目上的编码
        // 集团、公司、区域、部门
        List<String> orgTypeList = Arrays.asList("1010_S","1020_S","1030_S","1040_S");
        if (0L == adminorgId){
            return adminorgId;
        }
        DynamicObject haosAdminorghr = BusinessDataServiceHelper.loadSingle(adminorgId, "haos_adminorghr");
        if (haosAdminorghr != null) {
            Long parentOrgId = haosAdminorghr.getLong("parent.id");

            if (orgTypeList.contains(haosAdminorghr.getString("orgtype.number"))) {
                return haosAdminorghr.getLong("id");
            }
            else {
                return getParentOrg(parentOrgId);
            }
        }
        return null;
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        // 侦听基础资料字段的事件
        BasedataEdit fieldEdit = this.getView().getControl("tdkw_metric");
        fieldEdit.addBeforeF7SelectListener(this);

        // 侦听文本字段按钮点击事件
        this.addClickListeners("tdkw_metric_str");

    }

    @Override
    public void afterCreateNewData(EventObject e) {

        super.afterCreateNewData(e);
        FormShowParameter showParameter = this.getView().getFormShowParameter();

        String billStatus = showParameter.getCustomParam("billStatus");

        Long typeId = showParameter.getCustomParam("typeId");
        String typeName = showParameter.getCustomParam("typeName");

        JSONArray rows = showParameter.getCustomParam("rows");
        int index = showParameter.getCustomParam("index");


        //给内嵌页面上的字段赋值
        this.getModel().setValue("tdkw_billstatusfield", billStatus);
        this.getModel().setValue("tdkw_metric_type", typeId);
        this.getModel().setValue("tdkw_metric_type_str", typeName);
        this.getModel().setValue("tdkw_integerfield", index);


        if (!rows.isEmpty()) {
            this.getModel().batchCreateNewEntryRow("tdkw_entryentity", rows.size());

        }
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (int i = 0, rowsSize = rows.size(); i < rowsSize; i++) {
            JSONObject row = rows.getJSONObject(i);
            String entryWeightStr = row.getString("tdkw_weight");
            if (StringUtils.isNotBlank(entryWeightStr)) {
                BigDecimal entryWeightValue = new BigDecimal(entryWeightStr.replace("%", ""));
                totalWeight = totalWeight.add(entryWeightValue);
            }
            for (String field : EntityName.FIELD_LIST) {
                Object o = row.get(field);
                if (o instanceof JSONObject) {
                    this.getModel().setValue(field, ((JSONObject) o).getLong("id"), i);
                }
                else {
                    this.getModel().setValue(field, o, i);
                }
            }
        }
        this.getModel().setValue("tdkw_weight_sum_str", totalWeight + "%");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        DynamicObject metricType = (DynamicObject) this.getModel().getValue("tdkw_metric_type");
        String typeNumber = metricType.getString("number");
        if (typeNumber.equals("T0001") || typeNumber.equals("T0002")) {
            // KPI类 才可以 按钮编辑
            ClientViewProxy clientViewProxy = this.getView().getService(IClientViewProxy.class);
            clientViewProxy.invokeControlMethod("tdkw_entryentity", "setColEditorProp", "tdkw_metric_str", "showEditButton", true);
            clientViewProxy.invokeControlMethod("tdkw_entryentity", "setColEditorProp", "tdkw_metric_str", "eb", false);
            if (typeNumber.equals("T0001")) {
                boolean b = alignNotDecopose();
                this.getView().setVisible(b, "tdkw_view_unaligned");
                if (b) {
                    FormShowParameter formShowParameter = this.getView().getFormShowParameter();
                    String orgName = formShowParameter.getCustomParam("orgName");
                    if (StringUtils.isNotBlank(orgName)) {
                        this.getView().showErrorNotification(orgName + "存在上级已分解且未对齐的指标，详情请点击\"查看已分解未对齐指标\"确认！");
                    }
                }
            }
            this.getView().sendFormAction(this.getView().getParentView());
        }
        if (typeNumber.equals("T0005")) {
            //新增一行赋值
            FormShowParameter showParameter = this.getView().getFormShowParameter();
            JSONArray rows = showParameter.getCustomParam("rows");
            if (rows.size() == 0) {
                this.getView().invokeOperation("addorg");
                //设置上级组织
                Long adminorgId = this.getView().getParentView().getModel().getDataEntity(true).getLong("tdkw_adminorg.parent.id");
                adminorgId = getParentOrg(adminorgId);
                this.getModel().setValue("tdkw_superorg", adminorgId, 0);
                //更新父页面的指标
                IFormView parentView = this.getView().getParentView();
                parentView.getModel().setValue("tdkw_superorg", adminorgId, 0, showParameter.getCustomParam("index"));
                parentView.updateView("tdkw_subentryentity");
                this.getView().sendFormAction(parentView);
                this.getView().updateView("tdkw_entryentity");
            }
            this.getView().setVisible(false, "tdkw_toolbarap_right");
        }
        FormShowParameter formShowParameter = this.getView().getParentView().getFormShowParameter();
        Object viewKpi = formShowParameter.getCustomParam("viewKpi");
        if (ObjectUtils.isNotEmpty(viewKpi)) {
            this.getView().setVisible(false, "tdkw_toolbarap_right", "tdkw_toolbarap_left");
            this.getView().setEnable(false, "tdkw_entryentity");
        }

    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String fieldKey = e.getProperty().getName();

        if (EntityName.FIELD_LIST.contains(fieldKey)) {
            Object oldValue = e.getChangeSet()[0].getOldValue();
            Object newValue = e.getChangeSet()[0].getNewValue();
            int rowIndex = e.getChangeSet()[0].getRowIndex();
            FormShowParameter showParameter = this.getView().getFormShowParameter();
            int index = showParameter.getCustomParam("index");
            IFormView parentView = this.getView().getParentView();
            IDataModel parentModel = parentView.getModel();

            // 权重输入校验
            if ("tdkw_weight".equals(fieldKey)) {
                // 检查新值是否为空，防止空指针异常
                if (newValue instanceof String && StringUtils.isNotBlank((String) newValue)) {
                    String weightStr = (String) newValue;

                    // 校验输入是否为整数，且范围在 0 到 100 之间
                    if (weightStr.matches("^\\d{1,3}$")) {
                        // 转换为数值
                        BigDecimal weightValue = new BigDecimal(weightStr);

                        // 校验是否在0-100之间
                        if (weightValue.compareTo(BigDecimal.ZERO) < 0 || weightValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                            this.getModel().setValue(fieldKey, "", rowIndex);
                            parentView.showErrorNotification("权重必须在 0 到 100 之间。");
                            this.getView().sendFormAction(parentView);
                            return;

                        }

                        // 设置当前权重值
                        parentModel.setValue(fieldKey, newValue, rowIndex, index);

                        // 获取所有权重的总和
                        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
                        BigDecimal totalWeight = BigDecimal.ZERO;
                        for (DynamicObject entry : entryEntity) {
                            String entryWeightStr = entry.getString("tdkw_weight");
                            if (StringUtils.isNotBlank(entryWeightStr)) {
                                BigDecimal entryWeightValue = new BigDecimal(entryWeightStr);
                                totalWeight = totalWeight.add(entryWeightValue);
                            }
                        }

                        // 校验总权重是否超过100%
                        if (totalWeight.compareTo(BigDecimal.valueOf(100)) > 0) {
                            this.getModel().setValue(fieldKey, "", rowIndex);
                            parentView.showErrorNotification("所有权重的总和不能超过 100%。");
                        }
                        else {
                            // 获取父页面所有分录的权重总和
                            BigDecimal parentTotalWeight = BigDecimal.ZERO;
                            DynamicObjectCollection hideentry = parentModel.getEntryEntity("tdkw_hideentry");
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

                            if (parentTotalWeight.compareTo(BigDecimal.valueOf(100)) > 0) {
                                this.getModel().setValue(fieldKey, "", rowIndex);
                                parentView.showErrorNotification("组织绩效权重不能超过 100%。");
                            }
                            else {
                                this.getModel().setValue("tdkw_weight_sum", totalWeight);
                            }
                        }
                    }
                    else {
                        this.getModel().setValue(fieldKey, "", rowIndex);
                        parentView.showErrorNotification("请输入有效的权重值，范围为 0 到 100。");
                    }
                }
                if (newValue instanceof String && StringUtils.isBlank((String) newValue) && StringUtils.isNotBlank((String) oldValue)) {
                    parentModel.setValue(fieldKey, newValue, rowIndex, index);

                    // 获取所有权重的总和
                    DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    for (DynamicObject entry : entryEntity) {
                        String entryWeightStr = entry.getString("tdkw_weight");
                        if (StringUtils.isNotBlank(entryWeightStr)) {
                            BigDecimal entryWeightValue = new BigDecimal(entryWeightStr);
                            totalWeight = totalWeight.add(entryWeightValue);
                        }
                    }
                    this.getModel().setValue("tdkw_weight_sum", totalWeight);
                }
            }
            else {
                parentModel.setValue(fieldKey, newValue, rowIndex, index);
            }
            this.getView().sendFormAction(parentView);
        }
        else if ("tdkw_weight_sum".equals(fieldKey)) {
            Object newValue = e.getChangeSet()[0].getNewValue();
            this.getModel().setValue("tdkw_weight_sum_str", newValue + "%");
        }
    }


    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        String operateKey = formOperate.getOperateKey();

        IFormView parentView = this.getView().getParentView();
        IDataModel parentModel = parentView.getModel();
        Object parentPkId = parentModel.getValue("id");
        if (args.getOperationResult() != null && args.getOperationResult().isSuccess()) {
            if (StringUtils.equals("moveentryup", operateKey) || StringUtils.equals("moveentrydown", operateKey) || StringUtils.equals("deletearea", operateKey)) {
                FormShowParameter showParameter = this.getView().getFormShowParameter();
                int index = showParameter.getCustomParam("index");
                EntryGrid parentEntryGrid = parentView.getControl("tdkw_hideentry");
                parentEntryGrid.selectRows(index);
                this.getView().sendFormAction(parentView);
                parentModel.setEntryCurrentRowIndex("tdkw_hideentry", index);
                this.getView().sendFormAction(parentView);
                parentView.invokeOperation(operateKey);
                this.getView().sendFormAction(parentView);
            }
            else if (StringUtils.equals("decompose_align", operateKey)) {
                FormShowParameter formShowParameter = new FormShowParameter();
                DynamicObject org = (DynamicObject) parentModel.getValue("tdkw_adminorg");
                Date year = (Date) parentModel.getValue("tdkw_assess_year");
                formShowParameter.setCustomParam("year", year);
                formShowParameter.setCustomParam("org", org);
                formShowParameter.setFormId("tdkw_decompose_align_form");
                formShowParameter.setCustomParam("targetBillId", parentPkId);
                formShowParameter.setCustomParam("OperationStatus", parentView.getFormShowParameter().getStatus());
                //单据状态
                String billStatus = (String) this.getModel().getValue("tdkw_billstatusfield");
                if (StringUtils.isNotBlank(billStatus)) {
                    formShowParameter.setCustomParam("billStatus", billStatus);
                }
                formShowParameter.setStatus(OperationStatus.ADDNEW);
                formShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                this.getView().showForm(formShowParameter);
            }
            else if (StringUtils.equals("view_unaligned", operateKey)) {
                DynamicObject org = (DynamicObject) parentModel.getValue("tdkw_adminorg");
                long orgId = org.getLong("id");
                FormShowParameter formShowParameter = new FormShowParameter();
                Date year = (Date) parentModel.getValue("tdkw_assess_year");
                formShowParameter.setFormId("tdkw_not_alignment");
                formShowParameter.setCustomParam("year", year);
                formShowParameter.setCustomParam("targetBillId", parentPkId);
                formShowParameter.setCustomParam("orgId", orgId);
                formShowParameter.setStatus(OperationStatus.ADDNEW);
                formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                formShowParameter.setCloseCallBack(new CloseCallBack(this, "not_aliment"));
                //更新数据
                updateDate((Long) parentPkId, formShowParameter);
                this.getView().showForm(formShowParameter);
            }
            else if (StringUtils.equals("use_annual_goal", operateKey)) {
                FormShowParameter formShowParameter = new FormShowParameter();

                DynamicObject org = (DynamicObject) parentModel.getValue("tdkw_adminorg");
                HRPIWorkRoleService hrpiWorkRoleService = new HRPIWorkRoleService();
                List<Long> orgList = new ArrayList<>();
                orgList.add(org.getLong("id"));
                List<Map<String, Object>> mainChargeByOrg = hrpiWorkRoleService.getMainChargeByOrg(orgList);
                if (!mainChargeByOrg.isEmpty()) {
                    Object personId = mainChargeByOrg.get(0).get(org.getString("id"));
                    formShowParameter.setCustomParam("person", personId);
                }

                Date yearDate = (Date) parentModel.getValue("tdkw_assess_year");
                LocalDate localDate = yearDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int year = localDate.getYear();
                formShowParameter.setCustomParam("year", year);

                formShowParameter.setFormId(EntityName.FORM_USE_ANNUAL_GOAL);
                formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                formShowParameter.setCloseCallBack(new CloseCallBack(this, "use_annual_goal"));

                this.getView().showForm(formShowParameter);
            }
            else if (StringUtils.equals("tdkw_viewkpi", operateKey)) {
                DynamicObjectCollection collection = this.getModel().getDataEntity(true).getDynamicObjectCollection("tdkw_entryentity");
                if (collection.size() > 0) {
                    //获取组织
                    DynamicObject superorg = collection.get(0).getDynamicObject("tdkw_superorg");
                    //获取年份
                    Date assessYear = this.getView().getParentView().getModel().getDataEntity(true).getDate("tdkw_assess_year");
                    if (assessYear == null) {
                        this.getView().showTipNotification("请先填写年份！");
                        return;
                    }
                    //获取上级组织指标页面
                    if (superorg != null) {
                        DynamicObject parentKpi = BusinessDataServiceHelper.loadSingle("tdkw_org_perf_metrics", new QFilter[]{
                                new QFilter("tdkw_adminorg.id", QCP.equals, superorg.getLong("id")),
                                new QFilter("tdkw_assess_year", QCP.equals, assessYear)}
                        );
                        if (parentKpi != null) {
                            Long parentKpiId = parentKpi.getLong("id");
                            BillShowParameter formShowParameter = new BillShowParameter();
                            formShowParameter.setFormId("tdkw_org_perf_metrics");
                            formShowParameter.setStatus(OperationStatus.VIEW);
                            formShowParameter.setPkId(parentKpiId);
                            formShowParameter.getCustomParams().put("viewKpi", "1");
                            formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                            formShowParameter.setHasRight(true);
                            StyleCss inlineStyleCss = new StyleCss();
                            inlineStyleCss.setHeight("70%");
                            inlineStyleCss.setWidth("90%");
                            formShowParameter.getOpenStyle().setInlineStyleCss(inlineStyleCss);
                            this.getView().showForm(formShowParameter);
                        }
                        else {
                            this.getView().showTipNotification("本级经营业绩指标暂未制定！");
                        }
                    }
                    else {
                        this.getView().showTipNotification("请先填写上级组织！");
                    }
                }
                else {
                    this.getView().showTipNotification("请先填写上级组织！");
                }

            }

        }
        if (operateKey.equals("addorg")) {
            // 添加、插入、复制新行完毕，给新行填写了默认值之后，触发此事件；
            FormShowParameter showParameter = this.getView().getFormShowParameter();
            int index = showParameter.getCustomParam("index");
            EntryGrid parentEntryGrid = parentView.getControl("tdkw_hideentry");
            parentEntryGrid.selectRows(index);
            this.getView().sendFormAction(parentView);
            parentModel.setEntryCurrentRowIndex("tdkw_hideentry", index);
            this.getView().sendFormAction(parentView);
            parentView.invokeOperation("newsubentry");
            parentView.updateView("tdkw_subentryentity");
            this.getView().sendFormAction(parentView);
        }

    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if (StringUtils.equals("tdkw_metric_str", source.getKey())) {
            DynamicObject metricType = (DynamicObject) this.getModel().getValue("tdkw_metric_type");
            String typeNumber = metricType.getString("number");
            if (typeNumber.equals("T0001") || typeNumber.equals("T0002")) {
                ListShowParameter parameter = new ListShowParameter();
                parameter.setFormId("bos_templatetreelistf7");
                parameter.setBillFormId(EntityName.BASE_PERF_METRICS);
                parameter.setLookUp(true);
                parameter.setF7Style(0);
                parameter.setMultiSelect(true);
                StyleCss style = new StyleCss();

                style.setWidth("1200");

                style.setHeight("600");

                parameter.getOpenStyle().setInlineStyleCss(style);
                parameter.getOpenStyle().setShowType(ShowType.Modal);
                parameter.setShowTitle(false);
                parameter.setCloseCallBack(new CloseCallBack(this, "perf_metrics"));
                // 排除已选
                DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
                List<Long> metricIds = new ArrayList<>();

                if (!entryEntity.isEmpty()) {
                    metricIds = entryEntity.stream()
                            .map(dynamicObject -> dynamicObject.getLong("tdkw_metric.id"))
                            .collect(Collectors.toList());

                }
                ListFilterParameter filterParameter = parameter.getListFilterParameter();

                filterParameter.getQFilters().add(new QFilter("tdkw_metric_type.id", QCP.equals, metricType.getLong("id")));
                filterParameter.getQFilters().add(new QFilter("id", QCP.not_in, metricIds));

                this.getView().showForm(parameter);
            }
        }
    }


    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        String fieldKey = event.getProperty().getName();
        if (StringUtils.equals(fieldKey, "tdkw_metric_str")) {
            DynamicObject metricType = (DynamicObject) this.getModel().getValue("tdkw_metric_type");
            event.addCustomQFilter(new QFilter("tdkw_metric_type.id", QCP.equals, metricType.getLong("id")));
        }
    }


    @Override
    public void afterAddRow(AfterAddRowEventArgs e) {
        // 添加、插入、复制新行完毕，给新行填写了默认值之后，触发此事件；
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        int index = showParameter.getCustomParam("index");
        IFormView parentView = this.getView().getParentView();
        IDataModel parentModel = parentView.getModel();
        EntryGrid parentEntryGrid = parentView.getControl("tdkw_hideentry");
        parentEntryGrid.selectRows(index);
        this.getView().sendFormAction(parentView);
        parentModel.setEntryCurrentRowIndex("tdkw_hideentry", index);
        this.getView().sendFormAction(parentView);
        parentView.invokeOperation("newsubentry");
        this.getView().sendFormAction(parentView);
    }


    @Override
    public void afterDeleteRow(AfterDeleteRowEventArgs e) {
        // 删除行之后，触发此事件；
        FormShowParameter showParameter = this.getView().getFormShowParameter();

        int index = showParameter.getCustomParam("index");
        int[] rowIndexs = e.getRowIndexs();
        IFormView parentView = this.getView().getParentView();
        IDataModel parentModel = parentView.getModel();

        SubEntryGrid parentEntryGrid = parentView.getControl("tdkw_subentryentity");
        parentEntryGrid.selectRows(rowIndexs, index);
        this.getView().sendFormAction(parentView);
        parentModel.setEntryCurrentRowIndex("tdkw_hideentry", index);
        this.getView().sendFormAction(parentView);
        parentView.invokeOperation("deletesubentry");
        this.getView().sendFormAction(parentView);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        IFormView parentView = this.getView().getParentView();
        IDataModel parentModel = parentView.getModel();
        FormOperate formOperate = (FormOperate) args.getSource();
        String operateKey = formOperate.getOperateKey();
        if (operateKey.equals("decompose_align") || operateKey.equals("view_unaligned")) {
            Long value = (Long) Optional.ofNullable(parentModel.getValue("id")).orElse(0L);
            if (value.equals(0L)) {
                this.getView().showErrorNotification("请先保存！");
                args.setCancel(true);
            }
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        IFormView parentView = this.getView().getParentView();
        IDataModel parentModel = parentView.getModel();
        //表头编辑
        if ("use_annual_goal".equals(closedCallBackEvent.getActionId()) && closedCallBackEvent.getReturnData() != null) {
            DynamicObjectCollection returnData = (DynamicObjectCollection) closedCallBackEvent.getReturnData();
            if (returnData != null && !returnData.isEmpty()) {
                for (DynamicObject returnDatum : returnData) {
                    int row = this.getModel().createNewEntryRow("tdkw_entryentity");
                    this.getModel().setValue("tdkw_metric_str", returnDatum.getString("tdkw_target_name"), row);
                    String weight = returnDatum.getString("tdkw_weight").replace("%", "");
                    weight = weight.contains(".") ? weight.split("\\.")[0] : weight;
                    this.getModel().setValue("tdkw_weight", weight, row);
                    this.getModel().setValue("tdkw_metric_desc", returnDatum.getString("tdkw_indctrdesc"), row);
                }
            }
        }
        else if ("perf_metrics".equals(closedCallBackEvent.getActionId()) && closedCallBackEvent.getReturnData() != null) {
            ListSelectedRowCollection returnData = (ListSelectedRowCollection) closedCallBackEvent.getReturnData();
            Object[] primaryKeyValues = returnData.getPrimaryKeyValues();
            QFilter qFilter = new QFilter("id", QCP.in, primaryKeyValues);
            DynamicObjectCollection perfMetrics = QueryServiceHelper.query(EntityName.BASE_PERF_METRICS, "id,name,tdkw_description,tdkw_rating_standard", qFilter.toArray());

            for (int i = 0; i < perfMetrics.size(); i++) {
                DynamicObject perfMetric = perfMetrics.get(i);
                long id = perfMetric.getLong("id");
                String name = perfMetric.getString("name");
                String description = perfMetric.getString("tdkw_description");
                String ratingStandard = perfMetric.getString("tdkw_rating_standard");
                if (i == 0) {
                    this.getModel().setValue("tdkw_metric", id);
                    this.getModel().setValue("tdkw_metric_str", name);
                    this.getModel().setValue("tdkw_metric_desc", description);
                    this.getModel().setValue("tdkw_rating_standard", ratingStandard);
                }
                else {
                    int row = this.getModel().createNewEntryRow("tdkw_entryentity");
                    this.getModel().setValue("tdkw_metric", id, row);
                    this.getModel().setValue("tdkw_metric_str", name, row);
                    this.getModel().setValue("tdkw_metric_desc", description, row);
                    this.getModel().setValue("tdkw_rating_standard", ratingStandard, row);
                }
            }
        }
        else if ("not_aliment".equals(closedCallBackEvent.getActionId()) && closedCallBackEvent.getReturnData() != null) {
            //生成对齐单
            DynamicObject bill = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment", new QFilter[]{new QFilter("tdkw_targetid", QCP.equals, this.getView().getParentView().getModel().getValue("id"))});
            if (ObjectUtils.isEmpty(bill)) {
                bill = BusinessDataServiceHelper.newDynamicObject("tdkw_target_alignment");
                String number = CodeRuleServiceHelper.getNumber("tdkw_target_alignment", bill, null);
                bill.set("billno", number);
                bill.set("billstatus", "A");
                bill.set("tdkw_targetid", parentModel.getValue("id"));
                bill.set("tdkw_targetalienadmin", parentModel.getValue("tdkw_adminorg"));
                bill.set("tdkw_org_year", parentModel.getValue("tdkw_assess_year"));
                bill.set("createtime", new Date());
            }
            DynamicObjectCollection entryentity = bill.getDynamicObjectCollection("tdkw_targetalientry");


            List<DynamicObject> returnData = (List<DynamicObject>) closedCallBackEvent.getReturnData();
            if (CollectionUtils.isNotEmpty(returnData)) {
                for (DynamicObject returnDatum : returnData) {
                    int newEntryRow = this.getModel().createNewEntryRow("tdkw_entryentity");
                    this.getModel().setValue("tdkw_metric", returnDatum.getDynamicObject("tdkw_findctid"), newEntryRow);
                    this.getModel().setValue("tdkw_metric_str", returnDatum.getDynamicObject("tdkw_findctid").getString("name"), newEntryRow);
                    this.getModel().setValue("tdkw_rating_standard", returnDatum.getDynamicObject("tdkw_findctid").getString("tdkw_rating_standard"), newEntryRow);
                    this.getModel().setValue("tdkw_metric_desc", returnDatum.getDynamicObject("tdkw_findctid").getString("tdkw_description"), newEntryRow);
                    this.getModel().setValue("tdkw_weight", returnDatum.getString("tdkw_weight"), newEntryRow);
                    this.getModel().setValue("tdkw_target_value", returnDatum.getBigDecimal("tdkw_target_value"), newEntryRow);
                    this.getModel().setValue("tdkw_unit", returnDatum.getString("tdkw_unit"), newEntryRow);
                    this.getView().updateView("tdkw_entryentity");
                    if (entryentity.isEmpty()) {
                        //对齐单分录无数据直接新增
                        DynamicObject dynamicObject = entryentity.addNew();
                        addNewRow(dynamicObject, returnDatum);
                    }
                    else {
                        //对齐单分录有数据  循环分录判断分录中的指标id是否在closecallback返回的数据中
                        //returndata返回的数据可能包含在分录中也可能不包含
                        boolean isNew = false;
                        int i = -1;
                        for (int j = 0; j < entryentity.size(); j++) {
                            long targetId = entryentity.get(j).getLong("tdkw_findctid.id");
                            if (returnDatum.getLong("tdkw_findctid.id") == targetId) {
                                isNew = true;
                                i = j;
                                break;
                            }
                        }
                        if (isNew) {
                            //更新已有数据
                            updateData(entryentity.get(i), returnDatum);
                        }
                        else {
                            //新增数据
                            DynamicObject dynamicObject = entryentity.addNew();
                            addNewRow(dynamicObject, returnDatum);
                        }
                    }

                }
                //保存对齐单
                SaveServiceHelper.save(new DynamicObject[]{bill});
                //保存指标单
                parentView.invokeOperation("save");
                this.getView().sendFormAction(parentView);
            }
            boolean b = alignNotDecopose();
            this.getView().setVisible(b, "tdkw_view_unaligned");
            if (b) {
                FormShowParameter formShowParameter = this.getView().getFormShowParameter();
                String orgName = formShowParameter.getCustomParam("orgName");
                if (StringUtils.isNotBlank(orgName)) {
                    this.getView().showErrorNotification(orgName + "存在上级已分解且未对齐的指标，详情请点击\"查看已分解未对齐指标\"确认！");
                }
            }
//            this.getView().getParentView().setVisible(b, "tdkw_labelap1");
//            this.getView().getParentView().setVisible(b, "tdkw_labelap2");
            this.getView().sendFormAction(this.getView().getParentView());
        }
    }

    /**
     * 对齐单新增分录行数据
     *
     * @param newRow      对齐单新增行
     * @param returnDatum closecallback返回的数据
     */
    private void addNewRow(DynamicObject newRow, DynamicObject returnDatum) {
        IFormView parentView = this.getView().getParentView();
        IDataModel parentModel = parentView.getModel();
        newRow.set("tdkw_decomposebillid", parentModel.getValue("id"));
        newRow.set("tdkw_sourceadmin", returnDatum.getDynamicObject("tdkw_sourceadmin"));
        newRow.set("tdkw_findctid", returnDatum.getDynamicObject("tdkw_findctid"));
        newRow.set("tdkw_findctid_name", returnDatum.getDynamicObject("tdkw_findctid").getString("name"));
        newRow.set("tdkw_findcttype", returnDatum.getDynamicObject("tdkw_findctid").getDynamicObject("tdkw_metric_type"));
        newRow.set("tdkw_findctdesc", returnDatum.getDynamicObject("tdkw_findctid").getString("tdkw_description"));
        newRow.set("tdkw_decomposedesc", returnDatum.getString("tdkw_decomposedesc"));
        newRow.set("tdkw_alignmentstatus", "10");
        newRow.set("tdkw_alignmentdesc", returnDatum.getDynamicObject("tdkw_findctid").getString("tdkw_description"));
        newRow.set("tdkw_alignmentweight", returnDatum.getString("tdkw_weight"));
        newRow.set("tdkw_alignmenttarget", returnDatum.getBigDecimal("tdkw_target_value"));
        newRow.set("tdkw_unit", unitMap.get(returnDatum.getString("tdkw_unit")));
        DynamicObjectCollection idList = new DynamicObjectCollection();
        DynamicObject targetDynmicObject = new DynamicObject(newRow.getDynamicObjectCollection("tdkw_alignmentid").getDynamicObjectType());
        targetDynmicObject.set("fbasedataid", returnDatum.getDynamicObject("tdkw_findctid"));
        idList.add(targetDynmicObject);
        newRow.set("tdkw_alignmentid", idList);
    }


    /**
     * 对齐单已经有分录数据 修改数据
     *
     * @param rowData     对齐单分录数据
     * @param returnDatum closecallback返回的数据
     */
    private void updateData(DynamicObject rowData, DynamicObject returnDatum) {
        rowData.set("tdkw_alignmentweight", returnDatum.getString("tdkw_weight"));
        rowData.set("tdkw_alignmenttarget", returnDatum.getBigDecimal("tdkw_target_value"));
        rowData.set("tdkw_unit", unitMap.get(returnDatum.getString("tdkw_unit")));
        DynamicObjectCollection idList = new DynamicObjectCollection();
        DynamicObject targetDynmicObject = new DynamicObject(rowData.getDynamicObjectCollection("tdkw_alignmentid").getDynamicObjectType());
        targetDynmicObject.set("fbasedataid", returnDatum.getDynamicObject("tdkw_findctid"));
        idList.add(targetDynmicObject);
        rowData.set("tdkw_alignmentid", idList);
        rowData.set("tdkw_alignmentdesc", returnDatum.getDynamicObject("tdkw_findctid").getString("tdkw_description"));
        rowData.set("tdkw_alignmentstatus", "10");
    }


    private boolean alignNotDecopose() {
        IDataModel parentModel = this.getView().getParentView().getModel();
        Long targetBillId = (Long) parentModel.getValue("id");
        if (targetBillId == 0L) {
            return false;
        }
        DynamicObject org = (DynamicObject) parentModel.getValue("tdkw_adminorg");
        long orgId = org.getLong("id");
        //Long orgId = adminorg.getLong("tdkw_adminorg.id");
        Date year = (Date) parentModel.getValue("tdkw_assess_year");
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

    private void updateDate(Long targetBillId, FormShowParameter formShowParameter) {
        QFilter qFilter = new QFilter("tdkw_targetid", QCP.equals, targetBillId);
        DynamicObject alignment = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment", new QFilter[]{qFilter});
        if (ObjectUtils.isEmpty(alignment)) {
            return;
        }
        DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
        List<Long> alignmentTargetList = dynamicObjectCollection.stream().map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
        Date year = formShowParameter.getCustomParam("year");
        Long orgId = formShowParameter.getCustomParam("orgId");
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

}
