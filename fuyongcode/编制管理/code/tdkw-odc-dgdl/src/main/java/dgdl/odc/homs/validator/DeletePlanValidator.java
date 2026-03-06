package dgdl.odc.homs.validator;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import dgdl.odc.homs.opplugin.SycnOnPersonOp;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;


/**
 * 删除编制计划校验
 *
 * @version 1.0
 * @author: yaoshuai
 * @date:2023/12/26
 */
public class DeletePlanValidator extends AbstractValidator {

    private static Log logger = LogFactory.getLog(SycnOnPersonOp.class);
    // 人工成本滚动预算计划
    private static final String DGDL_BUDGETPLANINSTALLPC = "dgdl_budgetplaninstallpc";
    // 人工成本预算计划
    private static final String DGDL_BUDGETPLANINSTALL = "dgdl_budgetplaninstall";

    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        for (ExtendedDataEntity dataEntitie : dataEntities) {
            DynamicObject dataEntity = dataEntitie.getDataEntity();
            long pkValue = (long) dataEntity.getPkValue();
            QFilter idFilter = new QFilter("dgdl_prepareplan.id", QCP.equals, pkValue);
            DynamicObjectCollection budget = QueryServiceHelper.query(DGDL_BUDGETPLANINSTALL, "number", new QFilter[]{idFilter});
            DynamicObjectCollection budgetpc = QueryServiceHelper.query(DGDL_BUDGETPLANINSTALLPC, "number", new QFilter[]{idFilter});
            if (budget.size() != 0 || budgetpc.size() != 0) {
                throw new KDBizException("当前计划已被人工成本预算引用,无法删除");
            }
        }
    }

    //    @Override
//    public void validate() {
//        ExtendedDataEntity[] dataEntities = this.getDataEntities();
//        for (ExtendedDataEntity dataEntitie : dataEntities) {
//            DynamicObject dataEntity = dataEntitie.getDataEntity();
//            //编制计划单位
//            DynamicObject org = dataEntity.getDynamicObject("dgdl_org");
//            logger.info("DeletePlanValidator编制计划单位=" + org);
//            if (Objects.nonNull(org)) {
//                String orgNumber = org.getString("number");
//                QFilter planQFilter = new QFilter("dgdl_planyear.number", QCP.equals, orgNumber);
//                logger.info("DeletePlanValidator查询sql=" + planQFilter);
//                DynamicObject planBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planyear_bill", "id", planQFilter.toArray());
//                if (Objects.nonNull(planBillObj)) {
//                    throw new KDBizException("当前计划已生成年度信息编制管理,无法删除");
//                }
//            }
//        }
//    }


}
