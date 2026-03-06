package tdkw.hr.hlcmext.plugin;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.operation.OperationServiceImpl;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 批量劳动合同续签OP插件
 */
public class RenewBatchOpPlugin extends AbstractOperationServicePlugIn {

    private static Log logger = LogFactory.getLog(RenewBatchOpPlugin.class);

    public void onPreparePropertys(PreparePropertysEventArgs args) {
        List<String> fieldKeys = args.getFieldKeys();
        fieldKeys.add("tdkw_itemids_tag");
        fieldKeys.add("tdkw_affiliationord");
    }

    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        super.beforeExecuteOperationTransaction(e);
        String operationKey = e.getOperationKey();
        DynamicObject[] dynamicObjects = e.getDataEntities();
        if ("audit".equals(operationKey)) {
            //审批通过
            auditItems(dynamicObjects, e);
        }else if ("save".equals(operationKey) || "submit".equals(operationKey)){
            checkDocumentOrg(dynamicObjects, e);
        }
    }


    public void checkDocumentOrg(DynamicObject[] dynamicObjects, BeforeOperationArgs e){
        for (DynamicObject dataEntity:dynamicObjects){
            String structNumber = dataEntity.getString("tdkw_affiliationord.structnumber");
            //查询所属组织所有下属组织
            List<Long> hrOrgIdList = AdminOrgHrUtils.getLowerOrgIds(Arrays.asList(structNumber));
            String itemIds = dataEntity.getString("tdkw_itemids_tag");

            if (StringUtils.isNotEmpty(itemIds)){
                Object[] pkIds = Arrays.stream(itemIds.split(",")).map(n->Long.valueOf(n)).toArray();
                DynamicObject[] items = BusinessDataServiceHelper.load("hlcm_contractapplyrenew",
                        "curcompany,empnumber",
                        new QFilter("id", QCP.in, pkIds).toArray());
                List<String> notCheckNumber = new ArrayList<>();
                Arrays.stream(items).forEach(n->{
                    long curCompany = n.getLong("curcompany.id");
                    if (!hrOrgIdList.contains(curCompany)){
                        notCheckNumber.add(n.getString("empnumber"));
                    }
                });
                if (notCheckNumber.size() > 0){
                    e.setCancel(true);
                    e.setCancelMessage(String.format("工号：%s,不属于该所属组织,请核对数据后重新发起。", StringUtils.join(notCheckNumber.toArray(), ",")));
                    return;
                }
            }else{
                e.setCancel(true);
                e.setCancelMessage(String.format("单号：%s,至少存在一条分录数据。", dataEntity.getString("billno")));
                return;
            }
        }
    }

    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        super.afterExecuteOperationTransaction(e);
        String operationKey = e.getOperationKey();
        DynamicObject[] dynamicObjects = e.getDataEntities();
        if ("save".equals(operationKey)  || "submit".equals(operationKey)){
            //保存
            changeStatus(dynamicObjects);
        }else if ("wfauditing".equals(operationKey)) {
            //审批中
            changeStatus(dynamicObjects, "D");
        }else if ("wfauditnotpass".equals(operationKey) || "invalid".equals(operationKey) || "delete".equals(operationKey)) {
            //审批不通过 or  作废
            changeStatus(dynamicObjects, "F");
        }
    }

    private void auditItems(DynamicObject[] dynamicObjects,BeforeOperationArgs args) {
        try (TXHandle h1 = TX.requiresNew("tdkwRenewBatchOpPlugin_auditItems")) {
            try {
                Arrays.stream(dynamicObjects).forEach(n -> {
                    logger.info("单据号：{}，审批通过操作开始。", n.getString("billno"));
                    String itemIds = n.getString("tdkw_itemids_tag");
                    if (StringUtils.isNotEmpty(itemIds)) {
                        Object[] ids = Arrays.stream(itemIds.split(",")).map(id -> Long.valueOf(id)).toArray();
                        OperationServiceImpl opImpl = new OperationServiceImpl();
                        OperateOption operateOption = OperateOption.create();
                        operateOption.setVariableValue(OperateOptionConst.SKIPCHECKPERMISSION, Boolean.TRUE.toString());
                        operateOption.setVariableValue(OperateOptionConst.ISHASRIGHT, Boolean.TRUE.toString());
                        OperationResult operationResult = opImpl.localInvokeOperation("audit",
                                "hlcm_contractapplyrenew",
                                ids, operateOption);
                        if (operationResult.isSuccess()) {
                            h1.commit();
                            logger.info("单据号：{}，审批通过操作完成。", n.getString("billno"));
                        }else {
                            List<IOperateInfo> errorInfos = operationResult.getAllErrorOrValidateInfo();
                            String error = StringUtils.join(errorInfos.stream().map(i -> i.getMessage()).collect(Collectors.toList()));
                            String content = String.format("单据号：%s，子单据审批异常:%s",n.getString("billno"), error);
                            h1.markRollback();
                            logger.error(content);
                            args.setCancel(true);
                            args.setCancelMessage(content);
                        }
                    }
                });
            }catch (Throwable e){
                h1.markRollback();
                logger.error("批量劳动合同审批通过子单据执行异常:", e);
                e.printStackTrace();
            }
        }
    }

    private void changeStatus(DynamicObject[] dynamicObjects){
        Arrays.stream(dynamicObjects).forEach(n->{
            String itemIds = n.getString("tdkw_itemids_tag");
            long pkId = n.getLong("id");
            List<DynamicObject> updateList = new ArrayList<>();
            List ids = new ArrayList();
            //绑定新增的子单
            if (StringUtils.isNotEmpty(itemIds)){
                ids.addAll(Arrays.stream(itemIds.split(",")).mapToLong(id->Long.valueOf(id)).boxed().collect(Collectors.toList()));
                DynamicObject[] items = BusinessDataServiceHelper.load("hlcm_contractapplyrenew",
                        "billstatus,handlestatus,auditstatus,tdkw_renewbatch",
                        new QFilter("id", QCP.in, ids).toArray());
                Arrays.stream(items).forEach(item->{
                    item.set("billstatus", "B");
                    item.set("handlestatus", "2");
                    item.set("auditstatus", "B");
                    item.set("tdkw_renewbatch", pkId);
                    updateList.add(item);
                });
            }
            //解绑删除的子单
            DynamicObject[] itemsOld = BusinessDataServiceHelper.load("hlcm_contractapplyrenew",
                    "billstatus,handlestatus,auditstatus,tdkw_renewbatch",
                    new QFilter("tdkw_renewbatch.id", QCP.equals, pkId).toArray());
            Arrays.stream(itemsOld).filter(i-> !ids.contains(i.getLong("id")))
                    .forEach(item->{
                        item.set("billstatus", "A");
                        item.set("handlestatus", "1");
                        item.set("auditstatus", "A");
                        item.set("tdkw_renewbatch", null);
                        updateList.add(item);
                    });
            SaveServiceHelper.update(updateList.toArray(new DynamicObject[updateList.size()]));
        });
    }

    private void changeStatus(DynamicObject[] dynamicObjects,String status){
        Arrays.stream(dynamicObjects).forEach(n->{
            long pkId = n.getLong("id");
            DynamicObject[] items = BusinessDataServiceHelper.load("hlcm_contractapplyrenew",
                    "billstatus,handlestatus,auditstatus,tdkw_renewbatch",
                    new QFilter("tdkw_renewbatch.id", QCP.equals, pkId).toArray());
            Arrays.stream(items).forEach(item->{
                switch (status){
                    case "D":
                        item.set("billstatus", "D");
                        break;
                    case "F":
                        item.set("billstatus", "A");
                        item.set("handlestatus", "1");
                        item.set("auditstatus", "A");
                        item.set("tdkw_renewbatch", null);
                }
            });
            SaveServiceHelper.update(items);
        });
    }
}
