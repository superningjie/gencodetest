package dgdl.odc.homs.opplugin;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.agrona.Strings;


/**
 * @Author: yaoshuai
 * @CreateTime: 2024-01-29  15:02
 * @Description: 二级组织锁定所有下级组织操作
 */
public class LockPersonOp extends AbstractOperationServicePlugIn {


    private static Log logger = LogFactory.getLog(LockPersonOp.class);

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        e.getFieldKeys().add("adminorg");
        e.getFieldKeys().add("dgdl_planyear");
        e.getFieldKeys().add("billno");
        e.getFieldKeys().add("billstatus");
    }

    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        String operationKey = e.getOperationKey();
        logger.info("LockPersonOp执行操作标识=" + operationKey);
        DynamicObject[] dataEntities = e.getDataEntities();
        for (DynamicObject dataEntry : dataEntities) {
            //末级组织
            DynamicObject lastOrg = dataEntry.getDynamicObject("adminorg");
            //编制计划
            DynamicObject planYear = dataEntry.getDynamicObject("dgdl_planyear");
            //获取当前二级组织下的所有详情
            QFilter detailQFilter = new QFilter("adminorg.number", QCP.not_equals, lastOrg.getString("number"))
                    .and("dgdl_planyear.number", QCP.equals, planYear.getString("number"))
                    .and("dgdl_adminorg2.number", QCP.equals, lastOrg.getString("number"));
            logger.info("LockPersonOp获取详情SQL=" + detailQFilter);
            DynamicObject[] detailObjs = BusinessDataServiceHelper.load("dgdl_planyear_detail", "id,billstatus,adminorg,billno,dgdl_chg1,dgdl_chg2,dgdl_chg3,dgdl_chg4,dgdl_chg5,dgdl_chg6,dgdl_chg7,dgdl_chg8,dgdl_chg9,dgdl_chg10,dgdl_chg11,dgdl_chg12,dgdl_add,dgdl_reduce", detailQFilter.toArray());
            logger.info("LockPersonOp下级数量=" + detailObjs.length);
            if (detailObjs.length > 0) {
                for (DynamicObject dataObj : detailObjs) {
                    if ("lock".equals(operationKey)) {
                        //编制状态
                        String billstatus = dataObj.getString("billstatus");
                        if (!"I".equals(billstatus)) {
                            throw new KDBizException("存在未确认下级,无法锁定");
                        }
                        //已锁定
                        dataObj.set("billstatus", "H");
                    } else if ("cancellock".equals(operationKey)) {
                        //取消锁定
                        dataObj.set("billstatus", "I");
                    } else if ("submit".equals(operationKey)) {
                        dataObj.set("billstatus", "B");
                    } else if ("submiteffect".equals(operationKey)) {
                        dataObj.set("billstatus", "C");
                    } else if ("unsubmit".equals(operationKey)){
                        dataObj.set("billstatus", "H");
                    } else if ("wfauditing".equals(operationKey)){
                        dataObj.set("billstatus", "D");
                    } else if ("audit".equals(operationKey)){
                        dataObj.set("billstatus", "C");
                    }
                }
                SaveServiceHelper.update(detailObjs);
//                //执行保存操作
//                OperateOption option = OperateOption.create();
//                OperationResult endResult = OperationServiceHelper.executeOperate("save", "dgdl_planyear_detail", detailObjs, option);
//                logger.info("LockPersonOp锁定所有下级组织操作=" + endResult);
            }
            //调整当前单据状态
            OperateOption thisOption = OperateOption.create();
            if ("lock".equals(operationKey)) {
                dataEntry.set("billstatus", "H");
            } else if ("cancellock".equals(operationKey)) {
                dataEntry.set("billstatus", "I");
            } else if ("submit".equals(operationKey)) {
                //单据编码
                String billno = dataEntry.getString("billno");
                if (Strings.isEmpty(billno)){
                    String dgdlPlanyearDetail = CodeRuleServiceHelper.getNumber("dgdl_planyear_detail", dataEntry, null);
                    dataEntry.set("billno", dgdlPlanyearDetail);
                }
                dataEntry.set("billstatus", "B");
            } else if ("submiteffect".equals(operationKey)) {
                dataEntry.set("billstatus", "C");
            } else if ("unsubmit".equals(operationKey)) {
                dataEntry.set("billstatus", "H");
            }
            OperationResult thisResult = OperationServiceHelper.executeOperate("save", "dgdl_planyear_detail", new DynamicObject[]{dataEntry}, thisOption);
            logger.info("LockPersonOp本级操作=" + thisResult);
        }
    }
}
