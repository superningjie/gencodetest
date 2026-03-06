package dgdl.odc.homs.formplugin;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import dgdl.odc.homs.common.DateTimeCommon;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.hr.haos.mservice.HAOSBatchAdminOrgInfoQueryService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 陈路
 * @CreateTime: 2024-01-26  13:45
 * @Description: 年度编制同步在岗人员弹窗插件
 */
public class PlanyearSynPersonFormPlugin extends AbstractFormPlugin {

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


    /**
     * 查询下级组织微服务接口类
     */
    private static HAOSBatchAdminOrgInfoQueryService hAOSBatchAdminOrgInfoQueryService = new HAOSBatchAdminOrgInfoQueryService();


    private static Log logger = LogFactory.getLog(PlanyearSynPersonFormPlugin.class);

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        String synDate = (String) customParams.get("syn_date");
        this.getModel().setValue("dgdl_date", DateTimeCommon.ConvertToDate(synDate));
        this.getView().updateView("dgdl_date");
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("btnok");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        String key = source.getKey();
        if ("btnok".equals(key)) {
            Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
            //同步日期
            Date dgdlDate = (Date) this.getModel().getValue("dgdl_date");
            //详情id
            Long detailId = (Long) customParams.get("detailId");
            //组织id
            Long admingId = (Long) customParams.get("admingId");
            //计划id
            Long planId = (Long) customParams.get("planId");
            logger.info("PlanyearSynPersonFormPlugin接收的详情ID=" + detailId + ",同步组织id=" + admingId + ",计划id=" + planId);

            List<Long> adminList = new ArrayList<>();
            boolean isLast = true;
            DynamicObject lastOrg = null;
            if (Objects.nonNull(detailId)){
                QFilter detailQFilter = new QFilter("id", QCP.equals,detailId);
                DynamicObject detailObj = BusinessDataServiceHelper.loadSingle("dgdl_planyear_detail", "id,dgdl_adminorg1,dgdl_adminorg2,dgdl_adminorg3,dgdl_adminorg4,dgdl_adminorg5,dgdl_adminorg6,adminorg,dgdl_planyear", detailQFilter.toArray());
                //末端组织
                lastOrg = detailObj.getDynamicObject("adminorg");
                //判断当时是否是最末级组织
                isLast = this.isLastOrg(detailObj);
            }else {
                lastOrg = BusinessDataServiceHelper.loadSingle(admingId, "haos_adminorghr");
                //组织层级
                String adminorglayer = lastOrg.getString("adminorglayer.number");
                DynamicObject planYear = BusinessDataServiceHelper.loadSingle(planId, "dgdl_planyear");
                //当前计划层级
                String thisLayer = planYear.getString("dgdl_orghierarchy");
                switch (thisLayer){
                    case "1":
                        thisLayer = "02";
                        break;
                    case "2":
                        thisLayer = "03";
                        break;
                    case "3":
                        thisLayer = "04";
                        break;
                    case "4":
                        thisLayer = "05";
                        break;
                    case "5":
                        thisLayer = "06";
                        break;
                    case "6":
                        thisLayer = "07";
                        break;
                    default:;
                }
                if (!thisLayer.equals(adminorglayer)){
                    isLast = false;
                }
            }
            if (isLast) {
                List<Long> allList = this.getAllOrg(lastOrg.getLong("id"));
                adminList.addAll(allList);
            } else {
                adminList.add(lastOrg.getLong("id"));
            }

            if (Objects.nonNull(admingId)) {
                //获取任职经历查询条件
                QFilter empFiler = new QFilter("iscurrentversion", QCP.equals, "1")
                        .and("datastatus", QCP.equals, "1")
                        .and("isprimary", QCP.equals, "1")
                        .and("adminorg.id", QCP.in, adminList)
                        .and("enddate", QCP.large_equals, dgdlDate)
                        .and("startdate", QCP.less_equals, dgdlDate);
                logger.info("PlanyearBillFormPlugin任职经历同步SQL=" + empFiler);
                DynamicObject empData = BusinessDataServiceHelper.loadSingle("hrpi_empposorgrel", EMP_FILED, empFiler.toArray());
                if (Objects.isNull(empData)) {
                    throw new KDBizException("该组织当前同步时间下没有人员,无法同步");
                }
            }
            if (Objects.isNull(detailId) && Objects.nonNull(admingId)) {
                //获取行政组织
                DynamicObject adminorghr = BusinessDataServiceHelper.loadSingle(admingId, "haos_adminorghr");
                if (Objects.nonNull(adminorghr)) {
                    //查询岗位标签
                    QFilter joblabelFilter = new QFilter("enable", QCP.equals, "1");
                    DataSet joblabelDataSet = QueryServiceHelper.queryDataSet(this.getClass().getName(), "dgdl_hbss_joblabel", "id,number", joblabelFilter.toArray(), "");
                    DynamicObject detailBill = BusinessDataServiceHelper.newDynamicObject("dgdl_planyear_detail");
                    //获取编制规划单位
                    DynamicObject dgdlPlanObj = BusinessDataServiceHelper.loadSingle(planId, "dgdl_planyear");
                    DynamicObject org = dgdlPlanObj.getDynamicObject("dgdl_org");
                    //末端组织
                    detailBill.set("adminorg", adminorghr);
                    //获取层级
                    String hierarchy = dgdlPlanObj.getString("dgdl_orghierarchy");
                    //获取组织管理体系的所有行政组织
                    Map<String, DynamicObject> hierarchyMap = getAdminByOrg(org.getString("number"), hierarchy);
                    //通过长编码获取各个父级行政组织
                    String stringLongNumber = adminorghr.getString("structlongnumber");
                    String[] splitArray = stringLongNumber.split("!");
                    for (String s : splitArray) {
                        DynamicObject obj = hierarchyMap.get(s);
                        logger.info("行政组织上下级编码=" + s + ",对应信息=" + obj);
                        if (Objects.nonNull(obj)) {
                            String adminorglayer = obj.getString("adminorglayer.number");
                            if ("02".equals(adminorglayer)) {
                                detailBill.set("dgdl_adminorg1", obj);
                            } else if ("03".equals(adminorglayer)) {
                                detailBill.set("dgdl_adminorg2", obj);
                            } else if ("04".equals(adminorglayer)) {
                                detailBill.set("dgdl_adminorg3", obj);
                            } else if ("05".equals(adminorglayer)) {
                                detailBill.set("dgdl_adminorg4", obj);
                            } else if ("06".equals(adminorglayer)) {
                                detailBill.set("dgdl_adminorg5", obj);
                            } else if ("07".equals(adminorglayer)) {
                                detailBill.set("dgdl_adminorg6", obj);
                            }
                        }
                    }

                    //人员同步时间
                    detailBill.set("dgdl_synctime", new Date());
                    //编制状态
                    detailBill.set("billstatus", "A");
                    //审批状态
                    detailBill.set("auditstatus", "A");
                    //年度编制计划
                    detailBill.set("dgdl_planyear", dgdlPlanObj);
                    //执行保存操作
                    OperateOption option = OperateOption.create();
                    DynamicObject[] planObjs = new DynamicObject[]{detailBill};
                    OperationResult endResult = OperationServiceHelper.executeOperate("save", "dgdl_planyear_detail", planObjs, option);
                    logger.info("PlanyearBillFormPlugin同步在岗人员信息=" + endResult);
                    if(endResult.getSuccessPkIds().size()>0){

                        detailId = (Long) endResult.getSuccessPkIds().get(0);
                    }else {
                        throw new KDBizException("同步在岗人员信息失败："+endResult.getValidateResult().getMessage());
                    }

                } else {
                    throw new KDBizException("所选行政组织异常");
                }
            }
            this.getView().getPageCache().put("detailId", String.valueOf(detailId));
            //执行同步在岗人员操作
            OperateOption option = OperateOption.create();
            option.setVariableValue("dgdl_date", DateTimeCommon.covertToyyyyMMdd(dgdlDate));
            OperationResult operationResult = OperationServiceHelper.executeOperate("syndata", "dgdl_planyear_detail", new Object[]{detailId}, option);
            if (operationResult.isSuccess()) {
                //弹出在岗人员名单
                FormShowParameter fsp = new FormShowParameter();
                fsp.setFormId("dgdl_plan_persons");
                //编制详情ID
                fsp.setCustomParam("detailId", detailId);
                fsp.setCloseCallBack(new CloseCallBack(this, "synPerson"));
                fsp.getOpenStyle().setShowType(ShowType.Modal);
                this.getView().showForm(fsp);
            } else {
                this.getView().showErrorNotification(operationResult.getMessage());
            }
        }
    }


    @Override
    public void closedCallBack(ClosedCallBackEvent e) {
        super.closedCallBack(e);
        String actionId = e.getActionId();
        if ("synPerson".equals(actionId)) {
            //详情id
            String detailId = this.getView().getPageCache().get("detailId");
            this.getView().getParentView().getPageCache().put("detailId",detailId);
            this.getView().returnDataToParent(detailId);
            //关闭父页面
            this.getView().close();
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
        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("org.number", QCP.equals, orgNumber)
                .and("adminorglayer.number", QCP.in, orghierarchyList)
                .and("iscurrentversion", QCP.equals, "1");
        logger.info("CompilationPlanUrgeOp最终查询SQL" + orgQFilter);
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,parent.number,structlongnumber,structnumber,adminorglayer.number", orgQFilter.toArray());
        logger.info("CompilationPlanUrgeOp行政组织长度=" + orgObjs.length);
        Map<String, DynamicObject> orgMap = new HashMap<>();
        for (DynamicObject orgData : orgObjs) {
            String orgStructNumber = orgData.getString("structnumber");
            orgMap.put(orgStructNumber, orgData);
        }
        return orgMap;
    }


    /**
     * 获取当前组织以及下级组织
     *
     * @param orgId
     * @return
     */
    private List<Long> getAllOrg(Long orgId) {
        List<Long> orgIdList = new ArrayList<>();
        orgIdList.add(orgId);
        List<Map<String, Object>> allSubOrg = hAOSBatchAdminOrgInfoQueryService.batchGetAllSubOrg(orgIdList, new Date());
        return allSubOrg.stream().map(t -> (Long) t.get("orgId")).collect(Collectors.toList());
    }

    /**
     * 判断当前是否是最末级组织
     *
     * @param dataEntity
     * @return
     */
    private boolean isLastOrg(DynamicObject dataEntity) {

        //编制计划
        DynamicObject planyear = dataEntity.getDynamicObject("dgdl_planyear");
        //末端组织
        DynamicObject adminorg = dataEntity.getDynamicObject("adminorg");
        String lastName = adminorg.getString("number");
        logger.info("DeletePlanDetailValidator末端组织编码=" + lastName);
        //组织层级
        String hierarchy = planyear.getString("dgdl_orghierarchy");
        int intHierarchy = Integer.parseInt(hierarchy);
        logger.info("DeletePlanDetailValidator编制计划层级=" + intHierarchy);
        String adminStr = "dgdl_adminorg";
        List<String> adminList = new ArrayList<>();
        for (int i = 1; i <= intHierarchy; i++) {
            DynamicObject admin = dataEntity.getDynamicObject(adminStr + i);
            if (Objects.nonNull(admin)){
                adminList.add(adminStr + i);
                if (admin.getString("number").equals(lastName)) {
                    break;
                }
            }
        }
        logger.info("DeletePlanDetailValidator要匹配的集合=" + adminList);
        //上级行政组织
        String selectName = adminList.get(adminList.size() - 1);
        logger.info("DeletePlanDetailValidator要匹配的行政组织=" + selectName);

        QFilter qFilter = new QFilter("dgdl_planyear.id", QCP.equals, planyear.getPkValue())
                .and(selectName + ".number", QCP.equals, lastName);
        logger.info("DeletePlanDetailValidator查询语句=" + qFilter);
        DynamicObject[] load = BusinessDataServiceHelper.load("dgdl_planyear_detail", "id", qFilter.toArray());
        logger.info("DeletePlanDetailValidator查询长度=" + load.length);
        if (load.length > 1) {
            return false;
        } else {
            return true;
        }
    }
}
