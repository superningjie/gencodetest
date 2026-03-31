package dgdl.odc.homs.opplugin;


import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author: yaoshuai
 * @CreateTime: 2024-03-13  10:00
 * @Description: 保存详情时, 如果分录行没有数据则删除当前编制详情
 */
public class SavePlanOp extends AbstractOperationServicePlugIn {

    private static Log logger = LogFactory.getLog(SavePlanOp.class);

    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        super.beforeExecuteOperationTransaction(e);
        DynamicObject[] dataEntities = e.getDataEntities();
        for (DynamicObject dataEntry : dataEntities) {
            //末级组织
            DynamicObject lastOrg = dataEntry.getDynamicObject("adminorg");
            //编制计划
            DynamicObject planYear = dataEntry.getDynamicObject("dgdl_planyear");
            //组织层级
            String hierarchy = planYear.getString("dgdl_orghierarchy");
            int intHierarchy = Integer.parseInt(hierarchy);
            String adminStr = "dgdl_adminorg";
            for (int i = 1; i <= intHierarchy; i++) {
                DynamicObject admin = dataEntry.getDynamicObject(adminStr + i);
                if (admin.getString("number").equals(lastOrg.getString("number"))) {
                    adminStr = adminStr + i + ".number";
                    break;
                }
            }
            //获取当前组织下的所有详情
            QFilter detailQFilter = new QFilter("adminorg.number", QCP.not_equals, lastOrg.getString("number"))
                    .and("dgdl_planyear.number", QCP.equals, planYear.getString("number"))
                    .and(adminStr, QCP.equals, lastOrg.getString("number"));
            logger.info("SavePlanOp获取详情SQL=" + detailQFilter);
            DynamicObject[] detailObjs = BusinessDataServiceHelper.load("dgdl_planyear_detail", "id,billstatus,adminorg", detailQFilter.toArray());
            logger.info("SavePlanOp下级数量=" + detailObjs.length);
            for (DynamicObject dataObj : detailObjs) {
                //编制状态
                String billstatus = dataObj.getString("billstatus");
                DynamicObject lastAdmin = dataObj.getDynamicObject("adminorg");
                if (!"I".equals(billstatus)) {
                    throw new KDBizException("存在未确认下级,无法确认");
                }
            }
        }
    }
}
