package dgdl.odc.homs.task;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author 姚帅
 * @Date 2024/03/07 13:40
 * @Version 1.0
 * 月度计划统计实际人数
 */
public class MonthReportExportTask extends AbstractTask {

    private static final Log logger = LogFactory.getLog(MonthReportExportTask.class);

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        feedbackProgress(10, "开始执行任务", null);
        //执行逻辑
        dataEncapsulation(map);
    }


    /**
     * 查询数据进行封装
     */
    private void dataEncapsulation(Map<String, Object> map) {
        Long planId = (Long) map.get("planId");
        feedbackProgress(20, "开始执行任务", null);
        //获取管理明细
        QFilter detailQFilter = new QFilter("dgdl_planmonth_bill.dgdl_planmonth.id", QCP.equals, planId)
                .and("dgdl_bz_actual", QCP.large_than, 0);
        DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz",
                "id,dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6,adminorg," +
                        "dgdl_bz_jobproperty,dgdl_bz_laborreltype,dgdl_bz_actual,dgdl_layer", detailQFilter.toArray());
        logger.info("MonthReportExportTask获取有数据的明细的长度" + bzObjs.length);
        //获取需要计算的组织信息
        Set<Long> findOrgList = new HashSet<>();
        for (DynamicObject bzObj : bzObjs) {
            //当前组织层级
            int layer = Integer.parseInt(bzObj.getString("dgdl_layer"));
            for (int i = 1; i <= layer; i++) {
                String orgLayer = "dgdl_bz_adminorg" + i;
                //获取组织
                DynamicObject org = bzObj.getDynamicObject(orgLayer);
                if (Objects.nonNull(org)) {
                    findOrgList.add((Long) org.getPkValue());
                }
            }
        }
        logger.info("MonthReportExportTask获取组织的长度" + findOrgList.size());
        QFilter orgQFilter = new QFilter("adminorg.id", QCP.in, findOrgList).and("dgdl_planmonth_bill.dgdl_planmonth.id", QCP.equals, planId);
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz",
                "id,dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6,adminorg," +
                        "dgdl_bz_jobproperty,dgdl_bz_laborreltype,dgdl_bz_actual,dgdl_layer", orgQFilter.toArray());
        logger.info("MonthReportExportTask过滤之后的长度" + orgObjs.length);
        for (DynamicObject orgObj : orgObjs) {
            //当前组织
            DynamicObject lastOrg = orgObj.getDynamicObject("adminorg");
            //岗位属性
            String property = StringUtils.isEmpty(orgObj.getString("dgdl_bz_jobproperty")) ? "" : orgObj.getString("dgdl_bz_jobproperty");
            //用工关系类型
            String type = StringUtils.isEmpty(orgObj.getString("dgdl_bz_laborreltype")) ? "" : orgObj.getString("dgdl_bz_laborreltype");
            //当前组织层级
            String layer = orgObj.getString("dgdl_layer");
            String orgLayer = "dgdl_bz_adminorg" + layer;
            List<DynamicObject> allCollect = Arrays.stream(bzObjs).filter(obj -> Objects.nonNull(obj.getDynamicObject(orgLayer)) &&
                    obj.getDynamicObject(orgLayer).getPkValue().equals(lastOrg.getPkValue())).collect(Collectors.toList());
            if (!allCollect.isEmpty()) {
                int allActual = allCollect.stream()
                        .filter(obj -> property.equals(obj.getString("dgdl_bz_jobproperty")))
                        .filter(obj -> type.equals(obj.getString("dgdl_bz_laborreltype")))
                        .mapToInt(obj -> obj.getInt("dgdl_bz_actual")).sum();
                orgObj.set("dgdl_bz_actual", allActual);
            }
        }
        SaveServiceHelper.update(orgObjs);

        QFilter bzQFilter = new QFilter("dgdl_planmonth.id", QCP.equals, planId);
        //编制信息管理
        DynamicObject bzObj = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bill", "id,dgdl_is_syn", bzQFilter.toArray());
        bzObj.set("dgdl_is_syn", "01");
        SaveServiceHelper.update(new DynamicObject[]{bzObj});
    }
}
