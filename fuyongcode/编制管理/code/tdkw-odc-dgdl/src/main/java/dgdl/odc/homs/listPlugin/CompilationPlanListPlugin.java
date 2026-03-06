package dgdl.odc.homs.listPlugin;

import kd.bos.base.BaseShowParameter;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.api.JobInfo;
import kd.bos.schedule.api.JobType;
import kd.bos.schedule.api.TaskInfo;
import kd.bos.schedule.form.JobForm;
import kd.bos.schedule.form.JobFormInfo;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.hr.hbp.business.domain.repository.HisCommonEntityRepository;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.cache.HRPageCache;
import kd.hr.hbp.common.constants.org.TreeTemplateConstants;
import kd.hr.hbp.common.util.HRObjectUtils;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.util.newhismodel.HisAttachmentTool;

import java.util.*;

/**
 * @Author: lzf
 * @CreateTime: 2023-09-06  13:45
 * @Description: 年度编制计划 启用后开放链接
 */
public class CompilationPlanListPlugin extends AbstractListPlugin {
    private static Log logger = LogFactory.getLog(CompilationPlanListPlugin.class);

    /**
     * 任职经历获取字段
     */
    private final static String EMP_FILED = "id as empId,person.id as personId," +
            "job.id as dgdl_stdposition," +
            "job.dgdl_post_aisle.id as dgdl_post_aisle," +
            "job.dgdl_jobproperty.id as dgdl_jobproperty," +
            "job.dgdl_joblabel.fbasedataid as jobLabelId," +
            "position.dgdl_external_dispatch as dgdl_dispatch," +
            "position.workplace.id as dgdl_workplace";


    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        FormOperate operate = (FormOperate) args.getSource();
        String operateKey = operate.getOperateKey();
        if (args.getOperationResult() != null && args.getOperationResult().isSuccess()) {
            //获取选中行数据id
            long pkValue = (Long) operate.getListFocusRow().getPrimaryKeyValue();
            switch (operateKey) {
                //进入年度编制计划明细
                case "compilationplan":
                    //获取选中行数据id
                    long id = (Long) operate.getListFocusRow().getPrimaryKeyValue();
                    //获取数据
                    DynamicObject object = BusinessDataServiceHelper.loadSingle(id, "dgdl_planyear");
                    //获取使用状态
                    String enable = object.getString("enable");
//                    if ("0".equals(enable)) {
//                        this.getView().showErrorNotification("禁用状态数据无法编制");
//                    } else
                    if ("1".equals(enable) || "0".equals(enable)) {
                        //打开对应年度编制信息管理页面
                        BillShowParameter fsp = new BillShowParameter();
                        fsp.setFormId("dgdl_planyear_bill");
                        //查询年度编制信息
                        QFilter filter = new QFilter("dgdl_planyear.id", QCP.equals, id);
                        DynamicObject yearBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planyear_bill", "id", filter.toArray());
                        if (yearBillObj != null) {
                            fsp.setPkId(yearBillObj.getPkValue());
                            fsp.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                            this.getView().showForm(fsp);
                        } else {
                            this.getView().showErrorNotification("编辑计划明细未生成，请重新启用");
                        }
                    } else if ("2".equals(enable)) {
                        this.getView().showErrorNotification("编制计划待启用");
                    }
                    break;
                //进入月度编制明细
                case "monthplan":
                    //获取选中行数据id
                    long monthId = (Long) operate.getListFocusRow().getPrimaryKeyValue();
                    //获取数据
                    DynamicObject monthObj = BusinessDataServiceHelper.loadSingle(monthId, "dgdl_planmonth");
                    //获取使用状态
                    String monthanable = monthObj.getString("enable");
                    if ("0".equals(monthanable)) {
                        this.getView().showErrorNotification("禁用状态数据无法编制");
                    } else if ("1".equals(monthanable)) {
                        //打开对应年度编制信息管理页面
                        BillShowParameter fsp = new BillShowParameter();
                        QFilter treeQfilte = new QFilter("adminorg.id", QCP.in, "");
                        fsp.setCustomParam(TreeTemplateConstants.BIZ_QFILTER_KEY, treeQfilte.toSerializedString());
                        fsp.setFormId("dgdl_planmonth_bill");
                        //查询年度编制信息
                        QFilter filter = new QFilter("dgdl_planmonth.id", QCP.equals, monthId);
                        DynamicObject monthBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bill", "id", filter.toArray());
                        if (monthBillObj != null) {
                            fsp.setPkId(monthBillObj.getPkValue());
                            fsp.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                            this.getView().showForm(fsp);
                        } else {
                            this.getView().showErrorNotification("编辑计划明细未生成，请重新启用");
                        }
                    } else if ("2".equals(monthanable)) {
                        this.getView().showErrorNotification("编制计划待启用");
                    }
                    break;
                //变更
                case "change":
                    if (args.getOperationResult().isSuccess()) {
                        String formId = "dgdl_planyear";
                        this.showModel(formId);
                    }
                    break;
                case "monthrevise":
                    if (args.getOperationResult().isSuccess()) {
                        String formId = "dgdl_planmonth";
                        this.showModel(formId);
                    }
                    break;
                //年度启用
                case "enable":
                    //如果存在下游数据,则不生成
                    QFilter planQFilter = new QFilter("dgdl_planyear.id", QCP.equals, pkValue).and("dgdl_entry.dgdl_groupid", QCP.not_equals, "");
                    DynamicObject planBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planyear_detail", "id,", planQFilter.toArray());
                    if (Objects.isNull(planBillObj)) {
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("planId", pkValue);
                        this.dispatch(paramMap, "dgdl_planyear");
                    }
                    break;
                //月度启用
                case "monthenable":
                    QFilter monthQFilter = new QFilter("dgdl_planmonth.id", QCP.equals, pkValue).and("dgdl_is_syn", QCP.equals, "01");
                    DynamicObject monthBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bill", "id,", monthQFilter.toArray());
                    if (Objects.isNull(monthBillObj)) {
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("planId", pkValue);
                        this.dispatch(paramMap, "dgdl_planmonth");
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private void showModel(String formId) {
        ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
        //获取当前勾选单据的id
        Long pkId = (Long) selectedRows.get(0).getPrimaryKeyValue();
        BillShowParameter showChangeParameter = this.newBillShowParameter(formId);
        showChangeParameter.setFormId(formId);
        showChangeParameter.setCloseCallBack(new CloseCallBack(this, "refreshParent"));
        showChangeParameter.getOpenStyle().setShowType(ShowType.Modal);
        HRPageCache pageCache = new HRPageCache(this.getView().getPageCache());
        Map<String, Object> customParamMap = (Map) pageCache.get("customParamMap", Map.class);
        if (customParamMap == null) {
            customParamMap = new HashMap();
        }

        String useOrgId = (String) this.getView().getFormShowParameter().getCustomParam("useorgId");
        if (HRStringUtils.isNotEmpty(useOrgId)) {
            ((Map) customParamMap).put("useorgId", useOrgId);
        }

        ((Map) customParamMap).put("isChange", Boolean.TRUE);
        ((Map) customParamMap).put("pkId", pkId);
        ((Map) customParamMap).put("boid", pkId);
        ((Map) customParamMap).put("fromPage", "fromHisAction");
        ((Map) customParamMap).put("disable_control_title", "true");
        this.setPersonalCustomParams((Map) customParamMap);
        showChangeParameter.setCustomParams((Map) customParamMap);
        showChangeParameter.setHasRight(true);
        String pageId = this.getView().getPageId() + "_" + pkId + "__insertData";
        showChangeParameter.setPageId(pageId);
        showChangeParameter.setStatus(OperationStatus.EDIT);
        HRBaseServiceHelper serviceHelper = new HRBaseServiceHelper(formId);
        DynamicObject tempDy = HisCommonEntityRepository.getNonLineTimeTempByBoid(serviceHelper, pkId);
        HisAttachmentTool.putAttachmentsIntoCustomParam(formId, HRObjectUtils.isEmpty(tempDy) ? pkId : tempDy.getLong("id"), showChangeParameter);
        this.getView().showForm(showChangeParameter);
    }

    private BillShowParameter newBillShowParameter(String entityId) {
        Boolean isBaseDataCtrl = BaseDataServiceHelper.checkBaseDataCtrl(entityId);
        return (BillShowParameter) (isBaseDataCtrl ? new BillShowParameter() : new BaseShowParameter());
    }

    private void setPersonalCustomParams(Map<String, Object> customParams) {
        Boolean isBaseDataCtrl = BaseDataServiceHelper.checkBaseDataCtrl(this.getModel().getDataEntityType().getName());
        if (isBaseDataCtrl) {
            Object isPersonalizeData = this.getView().getFormShowParameter().getCustomParam("isPersonalizeData");
            if (Objects.nonNull(isPersonalizeData) && (Boolean) isPersonalizeData) {
                customParams.put("isPersonalizeSaveData", Boolean.TRUE);
            }

            Long id = (Long) this.getModel().getValue("id");
            Long masterId = (Long) this.getModel().getValue("masterid");
            if (Objects.nonNull(id) && Objects.nonNull(masterId) && id != 0L && masterId != 0L && !id.equals(masterId)) {
                customParams.put("isPersonalizeSaveData", Boolean.TRUE);
            }

        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(new String[]{"toolbarap"});
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        DynamicObject object = null;
        if (StringUtils.equals("dgdl_change", evt.getItemKey())) {
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
            if (selectedRows == null || selectedRows.isEmpty()) {
                this.getView().showErrorNotification("请选择一行数据操作");
                evt.setCancel(true);
                return;
            }
            Long billId = (Long) selectedRows.get(0).getPrimaryKeyValue();//获取当前勾选单据的id
            if (selectedRows.size() != 1) {
                this.getView().showErrorNotification("请选择一行数据操作");
                evt.setCancel(true);
                return;
            }
            object = BusinessDataServiceHelper.loadSingle(billId, "dgdl_planyear", "id,enable");
        } else if (StringUtils.equals("dgdl_revise", evt.getItemKey())) {
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
            if (selectedRows == null || selectedRows.isEmpty()) {
                this.getView().showErrorNotification("请选择一行数据操作");
                evt.setCancel(true);
                return;
            }

            Long billId = (Long) selectedRows.get(0).getPrimaryKeyValue();//获取当前勾选单据的id
            if (selectedRows.size() != 1) {
                this.getView().showErrorNotification("请选择一行数据操作");
                evt.setCancel(true);
                return;
            }
            object = BusinessDataServiceHelper.loadSingle(billId, "dgdl_planmonth", "id,enable");
        }
        if (Objects.nonNull(object)) {
            //获取使用状态
            String enable = object.getString("enable");
            if ("1".equals(enable)) {
                this.getView().showErrorNotification("当前计划已启用，不允许变更！");
                evt.setCancel(true);
            }
        }
    }

    /**
     * 创建任务目标，发布新任务
     */

    private void dispatch(Map<String, Object> paramMap, String item) {
        // 创建任务目标
        JobInfo jobInfo = new JobInfo();
        jobInfo.setAppId("homs");                // 执行类所在的应用名
        jobInfo.setJobType(JobType.REALTIME);   // 即时执行
        if ("dgdl_planyear".equals(item)) {
            jobInfo.setName("年度编制计划同步在岗人员");
            jobInfo.setTaskClassname("dgdl.odc.homs.task.YearReportExportTask");
        } else if ("dgdl_planmonth".equals(item)) {
            jobInfo.setName("月度编制计划同步实际人数");
            jobInfo.setTaskClassname("dgdl.odc.homs.task.MonthReportExportTask");
        }
        jobInfo.setId(UUID.randomUUID().toString());// 随机产生一个JobId (任务目标的标识)
        jobInfo.setRunByUserId(RequestContext.get().getCurrUserId());//设置当前操作人
        //自定义参数
        jobInfo.setParams(paramMap);
        // 回调参数，设置一个回调处理标识(actionId)
        CloseCallBack closeCallBack = new CloseCallBack(this, "taskcloseback");
        JobFormInfo jobFormInfo = new JobFormInfo(jobInfo);
        jobFormInfo.setCaption("任务执行进度"); // 进度界面标题
        jobFormInfo.setCloseCallBack(closeCallBack); // 任务完成后的回调处理
        jobFormInfo.setCanBackground(true); // 允许切换到后台执行
        jobFormInfo.setCanStop(true); // 允许中途取消
        jobFormInfo.setClickClassName("dgdl.odc.homs.task.YearReportExportClickTask");
        // 发布任务，并显示进度
        JobForm.dispatch(jobFormInfo, this.getView());
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (org.apache.commons.lang.StringUtils.equals(closedCallBackEvent.getActionId(), "taskcloseback")) {
            Map<String, Object> result = (Map<String, Object>) closedCallBackEvent.getReturnData();
            logger.info("年度编制计划回调" + result);
            if (result != null && result.containsKey("taskinfo")) {
                String taskInfoStr = (String) result.get("taskinfo");
                if (org.apache.commons.lang.StringUtils.isNotBlank(taskInfoStr)) {
                    TaskInfo taskInfo = SerializationUtils.fromJsonString(taskInfoStr, TaskInfo.class);
                    if (taskInfo.isTaskEnd()) {
                        // 获取任务执行完毕，生成的内容
                        HashMap<String, Object> map = SerializationUtils.fromJsonString(taskInfo.getData(), Map.class);
                        if ((boolean) map.get("success")) {
                            String urls = (String) map.get("urls");
                            this.getView().openUrl(urls);
                        } else {
                            String msg = (String) map.get("msg");
                            this.getView().showErrorNotification(msg);
                        }
                    }
                }
            }
        }
    }

    private Map<String, DynamicObject> getAdminByOrg(String orgNumber, String hierarchy) {
        logger.info("CompilationPlanUrgeOp所需生成的组织层级" + hierarchy + ",组织编码=" + orgNumber);
        //转换层级
        int intOrg = Integer.parseInt(hierarchy);
        //控制层级
        List<String> orghierarchyList = new ArrayList<>();
        for (int i = 1; i <= intOrg + 1; i++) {
            orghierarchyList.add("0" + i);
        }
        //获取当前子集团下所选层级的行政组织
        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1").and("org.number", QCP.equals, orgNumber).and("adminorglayer.number", QCP.in, orghierarchyList).and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,parent.number,structlongnumber,structnumber,adminorglayer.number", orgQFilter.toArray(), " createtime desc");
        logger.info("CompilationPlanUrgeOp行政组织长度=" + orgObjs.length);
        Map<String, DynamicObject> orgMap = new HashMap<>();
        for (DynamicObject orgData : orgObjs) {
            String orgStructNumber = orgData.getString("structnumber");
            orgMap.put(orgStructNumber, orgData);
        }
        return orgMap;
    }
}
