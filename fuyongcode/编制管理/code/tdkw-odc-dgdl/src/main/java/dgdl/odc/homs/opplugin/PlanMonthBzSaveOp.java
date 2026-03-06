package dgdl.odc.homs.opplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * @Author: 姚帅
 * @CreateTime: 2024-04-15  15:02
 * @Description: 批量调整当前组织下级的所有父级编制数，列表变色需要
 */
public class PlanMonthBzSaveOp extends AbstractOperationServicePlugIn {

    private static Log logger = LogFactory.getLog(PlanMonthBzSaveOp.class);

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("dgdl_planmonth_bill");
        e.getFieldKeys().add("dgdl_layer");
        e.getFieldKeys().add("dgdl_bz_laborreltype");
        e.getFieldKeys().add("dgdl_bz_jobproperty");
        e.getFieldKeys().add("adminorg");
        e.getFieldKeys().add("dgdl_bz1");
        e.getFieldKeys().add("dgdl_bz2");
        e.getFieldKeys().add("dgdl_bz3");
        e.getFieldKeys().add("dgdl_bz4");
        e.getFieldKeys().add("dgdl_bz5");
        e.getFieldKeys().add("dgdl_bz6");
        e.getFieldKeys().add("dgdl_bz7");
        e.getFieldKeys().add("dgdl_bz8");
        e.getFieldKeys().add("dgdl_bz9");
        e.getFieldKeys().add("dgdl_bz10");
        e.getFieldKeys().add("dgdl_bz11");
        e.getFieldKeys().add("dgdl_bz12");
    }

    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        super.endOperationTransaction(e);
        DynamicObject[] dataEntities = e.getDataEntities();
        //操作标识
        String operationKey = e.getOperationKey();
        logger.info("PlanMonthBzSaveOp操作标识="+operationKey);
        if ("save".equals(operationKey)){
            for (DynamicObject dataEntitie : dataEntities) {
                //当前管理信息
                Long pkValue = (Long) dataEntitie.getDynamicObject("dgdl_planmonth_bill").getPkValue();
                //当前编制数
                int bz1 = dataEntitie.getInt("dgdl_bz1");
                int bz2 = dataEntitie.getInt("dgdl_bz2");
                int bz3 = dataEntitie.getInt("dgdl_bz3");
                int bz4 = dataEntitie.getInt("dgdl_bz4");
                int bz5 = dataEntitie.getInt("dgdl_bz5");
                int bz6 = dataEntitie.getInt("dgdl_bz6");
                int bz7 = dataEntitie.getInt("dgdl_bz7");
                int bz8 = dataEntitie.getInt("dgdl_bz8");
                int bz9 = dataEntitie.getInt("dgdl_bz9");
                int bz10 = dataEntitie.getInt("dgdl_bz10");
                int bz11 = dataEntitie.getInt("dgdl_bz11");
                int bz12 = dataEntitie.getInt("dgdl_bz12");
                //末级组织
                DynamicObject adminorg = dataEntitie.getDynamicObject("adminorg");
                //当前组织层级
                String layer = dataEntitie.getString("dgdl_layer");
                String adminStr = "dgdl_bz_adminorg" + layer + ".id";
                int intLayer = Integer.parseInt(layer);
                //下一层级
                String nextAdminStr = "dgdl_bz_adminorg" + (intLayer + 1);
                //岗位属性
                String property = dataEntitie.getString("dgdl_bz_jobproperty");
                //用公关系类型
                String type = dataEntitie.getString("dgdl_bz_laborreltype");
                if (intLayer < 6) {
                    //当前组织直接下级组织
                    QFilter nextQFiler = new QFilter("dgdl_planmonth_bill.id", QCP.equals, pkValue)
                            .and(adminStr, QCP.equals, adminorg.getLong("id"))
                            .and("dgdl_bz_jobproperty", QCP.equals, property)
                            .and("dgdl_bz_laborreltype", QCP.equals, type)
                            .and("dgdl_layer", QCP.equals, String.valueOf(intLayer + 1));
                    logger.info("PlanMonthBzSaveOp查询条件=" + nextQFiler);
                    DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "adminorg,dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6," +
                            "dgdl_p_bz1,dgdl_p_bz2,dgdl_p_bz3,dgdl_p_bz4,dgdl_p_bz5,dgdl_p_bz6," + "dgdl_p_bz7,dgdl_p_bz8,dgdl_p_bz9,dgdl_p_bz10,dgdl_p_bz11,dgdl_p_bz12", nextQFiler.toArray());
                    logger.info("PlanMonthBzSaveOp下级组织长度=" + bzObjs.length);
                    for (DynamicObject bzObj : bzObjs) {
                        bzObj.set("dgdl_p_bz1", bz1);
                        bzObj.set("dgdl_p_bz2", bz2);
                        bzObj.set("dgdl_p_bz3", bz3);
                        bzObj.set("dgdl_p_bz4", bz4);
                        bzObj.set("dgdl_p_bz5", bz5);
                        bzObj.set("dgdl_p_bz6", bz6);
                        bzObj.set("dgdl_p_bz7", bz7);
                        bzObj.set("dgdl_p_bz8", bz8);
                        bzObj.set("dgdl_p_bz9", bz9);
                        bzObj.set("dgdl_p_bz10", bz10);
                        bzObj.set("dgdl_p_bz11", bz11);
                        bzObj.set("dgdl_p_bz12", bz12);
                    }
                    SaveServiceHelper.update(bzObjs);
                }

                //上一层级
                String topAdminStr = "dgdl_bz_adminorg" + (intLayer - 1);
                //调整当前同级组织的编辑数
                if (intLayer > 1) {
                    //上级组织
                    DynamicObject topAdminOrg = dataEntitie.getDynamicObject(topAdminStr);
                    if (Objects.nonNull(topAdminOrg)) {
                        //当前组织直接同级组织
                        QFilter topQFiler = new QFilter("dgdl_planmonth_bill.id", QCP.equals, pkValue)
                                .and(topAdminStr, QCP.equals, topAdminOrg.getPkValue())
                                .and("dgdl_layer", QCP.equals, String.valueOf(intLayer))
                                .and("dgdl_bz_jobproperty", QCP.equals, property)
                                .and("dgdl_bz_laborreltype", QCP.equals, type);
                        logger.info("PlanMonthBzSaveOp获取统计组织=" + topQFiler);
                        DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "dgdl_bz1,dgdl_bz2,dgdl_bz3,dgdl_bz4,dgdl_bz5,dgdl_bz6,dgdl_bz7,dgdl_bz8,dgdl_bz9,dgdl_bz10,dgdl_bz11,dgdl_bz12," +
                                "dgdl_all_bz1,dgdl_all_bz2,dgdl_all_bz3,dgdl_all_bz4,dgdl_all_bz5,dgdl_all_bz6,dgdl_all_bz7,dgdl_all_bz8,dgdl_all_bz9,dgdl_all_bz10,dgdl_all_bz11,dgdl_all_bz12", topQFiler.toArray());
                        logger.info("PlanMonthBzSaveOp同级组织长度=" + bzObjs.length);
                        if (bzObjs.length > 0) {
                            int allBz1 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz1")).sum();
                            int allBz2 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz2")).sum();
                            int allBz3 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz3")).sum();
                            int allBz4 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz4")).sum();
                            int allBz5 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz5")).sum();
                            int allBz6 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz6")).sum();
                            int allBz7 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz7")).sum();
                            int allBz8 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz8")).sum();
                            int allBz9 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz9")).sum();
                            int allBz10 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz10")).sum();
                            int allBz11 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz11")).sum();
                            int allBz12 = Arrays.stream(bzObjs).mapToInt(a -> a.getInt("dgdl_bz12")).sum();
                            for (DynamicObject bzObj : bzObjs) {
                                bzObj.set("dgdl_all_bz1", allBz1);
                                bzObj.set("dgdl_all_bz2", allBz2);
                                bzObj.set("dgdl_all_bz3", allBz3);
                                bzObj.set("dgdl_all_bz4", allBz4);
                                bzObj.set("dgdl_all_bz5", allBz5);
                                bzObj.set("dgdl_all_bz6", allBz6);
                                bzObj.set("dgdl_all_bz7", allBz7);
                                bzObj.set("dgdl_all_bz8", allBz8);
                                bzObj.set("dgdl_all_bz9", allBz9);
                                bzObj.set("dgdl_all_bz10", allBz10);
                                bzObj.set("dgdl_all_bz11", allBz11);
                                bzObj.set("dgdl_all_bz12", allBz12);
                            }
                            SaveServiceHelper.update(bzObjs);
                        }
                    }
                }
            }
        }
    }
}
