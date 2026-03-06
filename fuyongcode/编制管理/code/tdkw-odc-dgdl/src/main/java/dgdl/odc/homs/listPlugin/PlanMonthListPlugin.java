package dgdl.odc.homs.listPlugin;

import com.grapecity.documents.excel.S;
import dgdl.odc.homs.opplugin.CompilationPlanUrgeOp;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.report.CellStyle;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeCreateListColumnsArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.BillList;
import kd.bos.list.IListColumn;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author 姚帅
 * @date:2023/1/22
 * @description: 月度编制管理列表插件
 */
public class PlanMonthListPlugin extends AbstractListPlugin {
    private final static String FILED = "id,dgdl_planmonth_bill,dgdl_bz_jobproperty,dgdl_bz_laborreltype,dgdl_jz1,dgdl_jz2,dgdl_jz3,dgdl_jz4,dgdl_jz5,dgdl_jz6,dgdl_jz7,dgdl_jz8,dgdl_jz9,dgdl_jz10,dgdl_jz11,dgdl_jz12,dgdl_bz1,dgdl_bz2,dgdl_bz3,dgdl_bz4,dgdl_bz5,dgdl_bz6,dgdl_bz7,dgdl_bz8,dgdl_bz9,dgdl_bz10,dgdl_bz11,dgdl_bz12" +
            ",dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6";
    private final static String FILED_OR_RED = "id,dgdl_jz1,dgdl_jz2,dgdl_jz3,dgdl_jz4,dgdl_jz5,dgdl_jz6,dgdl_jz7,dgdl_jz8,dgdl_jz9,dgdl_jz10,dgdl_jz11,dgdl_jz12,dgdl_bz1,dgdl_bz2,dgdl_bz3,dgdl_bz4,dgdl_bz5,dgdl_bz6,dgdl_bz7,dgdl_bz8,dgdl_bz9,dgdl_bz10,dgdl_bz11,dgdl_bz12";

    private static Log logger = LogFactory.getLog(CompilationPlanUrgeOp.class);

    public PlanMonthListPlugin() {
        super();
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("toolbarap");
    }

    @Override
    public void setFilter(SetFilterEvent e) {
        super.setFilter(e);
        //筛选对应的月度详情
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        Long planMonthId = (Long) customParams.get("planId");
        QFilter qFilter1 = new QFilter("dgdl_planmonth_bill.dgdl_planmonth.id", "=", planMonthId);
        e.addCustomQFilter(qFilter1);
    }

    @Override
    public void beforeCreateListColumns(BeforeCreateListColumnsArgs args) {
        super.beforeCreateListColumns(args);
        //隐藏列，获取最大层级
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        String orghierarchy = (String) customParams.get("dgdl_orghierarchy");
        if (orghierarchy != null) {
            List<String> columnList = new ArrayList<>();
            int index = Integer.valueOf(orghierarchy);
            List<IListColumn> listColumns = args.getListColumns();
            //公共关系类型
            String labeldimension = (String) customParams.get("labeldimension");
            if (labeldimension.contains("1") && labeldimension.contains("2")) {
            } else if (labeldimension.contains("1")) {
                columnList.add("dgdl_bz_jobproperty");
            } else if (labeldimension.contains("2")) {
                columnList.add("dgdl_bz_laborreltype");
            }
            List<String> hideList = getHidecolumnList(index, columnList);
            List<IListColumn> collect = listColumns.stream().filter(listColumn -> !hideList.contains(listColumn.getListFieldKey())).collect(Collectors.toList());
            args.setListColumns(collect);
        }
    }


    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
        String isFreeze = null;
        if (StringUtils.equals("freeze", evt.getItemKey())) {
            isFreeze = "02";
            if (selectedRows == null || selectedRows.isEmpty()) {
                this.getView().showErrorNotification("请选择一行数据操作");
                evt.setCancel(true);
            }
        } else if (StringUtils.equals("unfreeze", evt.getItemKey())) {
            isFreeze = "01";
            if (selectedRows == null || selectedRows.isEmpty()) {
                this.getView().showErrorNotification("请选择一行数据操作");
                evt.setCancel(true);
            }
        } else if (StringUtils.equals("dgdl_delete1", evt.getItemKey())) {
            //选中的详情
            List<Long> checkIds = new ArrayList<>();
            for (ListSelectedRow selectedRow : selectedRows) {
                checkIds.add((Long) selectedRow.getPrimaryKeyValue());
            }
            //删除的组织
            Map<Long,Long> deleteIdMap = new HashMap<>();
            QFilter qFilter = new QFilter("id", QCP.in, checkIds);
            DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "id,adminorg,dgdl_planmonth_bill.dgdl_planmonth", qFilter.toArray());
            for (DynamicObject baObj : bzObjs) {
                deleteIdMap.put((Long) baObj.getDynamicObject("adminorg").getPkValue(),(Long) baObj.getDynamicObject("dgdl_planmonth_bill.dgdl_planmonth").getPkValue());
            }
            //同一组织数量
            Map<Long, List<DynamicObject>> groupOrg = Arrays.stream(bzObjs).filter(bz -> Objects.nonNull(bz.getDynamicObject("adminorg")))
                    .collect(Collectors.groupingBy(bz -> bz.getDynamicObject("adminorg").getLong("id")));
            Set<Long> deleteIds = deleteIdMap.keySet();
            logger.info("PlanMonthListPlugin删除的组织=" +deleteIds);
            for (Long deleteId : deleteIds) {
                //计划id
                Long planId = deleteIdMap.get(deleteId);
                List<Long> hasOrgIds = new ArrayList<>();
                QFilter orgQFilter = new QFilter("adminorg.id", QCP.equals, deleteId).and("dgdl_planmonth_bill.dgdl_planmonth.id",QCP.equals,planId);
                DynamicObject[] bzOrgObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "id,adminorg", orgQFilter.toArray());
                for (DynamicObject bzOrg : bzOrgObjs) {
                    hasOrgIds.add((Long) bzOrg.getDynamicObject("adminorg").getPkValue());
                }
                List<DynamicObject> checkOrgs = groupOrg.get(deleteId);
                logger.info("PlanMonthListPlugin当前剩余组织=" + hasOrgIds.size() + ",选中组织=" + checkOrgs.size());
                if (hasOrgIds.size() == checkOrgs.size()) {
                    //计划id
                    DynamicObject planMonth = BusinessDataServiceHelper.loadSingle(planId, "dgdl_planmonth");
                    if (Objects.nonNull(planMonth)) {
                        DynamicObjectCollection delOrgCollect = planMonth.getDynamicObjectCollection("dgdl_delorg");
                        DynamicObject org = BusinessDataServiceHelper.loadSingle(deleteId, "haos_adminorghr");
                        DynamicObject newCurrency = new DynamicObject(delOrgCollect.getDynamicObjectType());
                        newCurrency.set("fbasedataid", org.getPkValue());
                        delOrgCollect.add(newCurrency);
                        planMonth.set("dgdl_delorg", delOrgCollect);
                        SaveServiceHelper.save(new DynamicObject[]{planMonth});
                    }
                }
            }
        }

        if (selectedRows != null && (StringUtils.equals("unfreeze", evt.getItemKey()) || StringUtils.equals("freeze", evt.getItemKey()))) {
            List<Long> ids = new ArrayList<>();
            for (ListSelectedRow selectedRow : selectedRows) {
                ids.add((Long) selectedRow.getPrimaryKeyValue());
            }
            if (!ids.isEmpty()) {
                QFilter qFilter = new QFilter("id", QCP.in, ids);
                DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "id,dgdl_isfreeze", qFilter.toArray());
                for (DynamicObject bzObj : bzObjs) {
                    bzObj.set("dgdl_isfreeze", isFreeze);
                }
                //调用保存操作
                SaveServiceHelper.update(bzObjs);
            }
            this.getView().invokeOperation("refresh");
            evt.setCancel(true);
        }
    }

    /**
     * 删除后记录被删除组织
     */
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        String operateKey = args.getOperateKey();
        if ("delete".equals(operateKey) && args.getOperationResult().isSuccess()) {
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
            //选中的详情
            List<Long> checkIds = new ArrayList<>();
            for (ListSelectedRow selectedRow : selectedRows) {
                checkIds.add((Long) selectedRow.getPrimaryKeyValue());
            }
            //删除的组织
            Set<Long> deleteIds = new HashSet<>();
            QFilter qFilter = new QFilter("id", QCP.in, checkIds);
            DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "id,adminorg,dgdl_planmonth_bill.dgdl_planmonth", qFilter.toArray());
            for (DynamicObject baObj : bzObjs) {
                deleteIds.add((Long) baObj.getDynamicObject("adminorg").getPkValue());
            }
            logger.info("PlanMonthListPlugin删除的组织=" + deleteIds);
            if (!deleteIds.isEmpty()) {
                Set<Long> hasOrgIds = new HashSet<>();
                QFilter orgQFilter = new QFilter("adminorg.id", QCP.in, deleteIds);
                DynamicObject[] bzOrgObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "id,adminorg", orgQFilter.toArray());
                for (DynamicObject bzOrg : bzOrgObjs) {
                    hasOrgIds.add((Long) bzOrg.getDynamicObject("adminorg").getPkValue());
                }
                logger.info("PlanMonthListPlugin还存在数据的组织=" + hasOrgIds);
                deleteIds.removeAll(hasOrgIds);
                logger.info("PlanMonthListPlugin剩余的组织=" + deleteIds);
                if (!deleteIds.isEmpty()) {
                    //计划id
                    Long planId = (Long) bzObjs[0].getDynamicObject("dgdl_planmonth_bill.dgdl_planmonth").getPkValue();
                    DynamicObject planMonth = BusinessDataServiceHelper.loadSingle(planId, "dgdl_planmonth");
                    if (Objects.nonNull(planMonth)) {
                        DynamicObjectCollection delOrgCollect = planMonth.getDynamicObjectCollection("dgdl_delorg");
                        for (Long orgId : deleteIds) {
                            DynamicObject org = BusinessDataServiceHelper.loadSingle(orgId, "haos_adminorghr");
                            delOrgCollect.add(org);
                        }
                        SaveServiceHelper.update(new DynamicObject[]{planMonth});
                    }
                }
            }
        }
    }


    /**
     * 需要隐藏的列
     *
     * @param index
     * @return
     */
    private List<String> getHidecolumnList(int index, List<String> columnList) {
        for (int i = index + 1; i <= 6; i++) {
            columnList.add("dgdl_bz_adminorg" + i + ".name");
        }
        return columnList;
    }

    /**
     * 一级组织-六级组织，每月编制数飘红处理
     *
     * @param id
     * @param data
     */
    private void setListUnitStyle(Long id, DynamicObjectCollection data) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bz", FILED, new QFilter[]{new QFilter("id", QCP.equals, id)});
        this.doCellStyle(dynamicObject, data);//飘红
    }

    private void doCellStyle(DynamicObject dataEntity, DynamicObjectCollection data) {

        List<CellStyle> cellStyles = new ArrayList<>();
        DynamicObjectCollection orgSonColl = null;
        List<Long> orgSonIds = new ArrayList<>();
        DynamicObject dgdl_bz_adminorg1 = dataEntity.getDynamicObject("dgdl_bz_adminorg1");
        DynamicObject dgdl_bz_adminorg2 = dataEntity.getDynamicObject("dgdl_bz_adminorg2");
        DynamicObject dgdl_bz_adminorg3 = dataEntity.getDynamicObject("dgdl_bz_adminorg3");
        DynamicObject dgdl_bz_adminorg4 = dataEntity.getDynamicObject("dgdl_bz_adminorg4");
        DynamicObject dgdl_bz_adminorg5 = dataEntity.getDynamicObject("dgdl_bz_adminorg5");
        DynamicObject dgdl_bz_adminorg6 = dataEntity.getDynamicObject("dgdl_bz_adminorg6");
        if (dgdl_bz_adminorg1 != null && dgdl_bz_adminorg2 == null) {
            //1级组织
            QFilter qFilterorg2 = new QFilter("dgdl_bz_adminorg1", QCP.is_notnull, null).and(new QFilter("dgdl_bz_adminorg1", QCP.not_equals, 0L))
                    .and(new QFilter("dgdl_bz_adminorg1", QCP.equals, dgdl_bz_adminorg1.getLong("id")))
                    .and(new QFilter("dgdl_planmonth_bill", QCP.equals, dataEntity.getLong("dgdl_planmonth_bill.id")))
                    .and(new QFilter("dgdl_bz_jobproperty", QCP.equals, dataEntity.getString("dgdl_bz_jobproperty")))
                    .and(new QFilter("dgdl_bz_laborreltype", QCP.equals, dataEntity.getString("dgdl_bz_laborreltype")))
                    .and(new QFilter("id", QCP.not_equals, dataEntity.getLong("id")));
            orgSonColl = QueryServiceHelper.query("dgdl_planmonth_bz", FILED_OR_RED, qFilterorg2.toArray());
            orgSonIds = orgSonColl.stream().map(f -> f.getLong("id")).collect(Collectors.toList());
        } else if (dgdl_bz_adminorg2 != null && dgdl_bz_adminorg3 == null) {
            //2级组织
            QFilter qFilterorg2 = new QFilter("dgdl_bz_adminorg2", QCP.is_notnull, null).and(new QFilter("dgdl_bz_adminorg2", QCP.not_equals, 0L))
                    .and(new QFilter("dgdl_bz_adminorg2", QCP.equals, dgdl_bz_adminorg2.getLong("id")))
                    .and(new QFilter("dgdl_planmonth_bill", QCP.equals, dataEntity.getLong("dgdl_planmonth_bill.id")))
                    .and(new QFilter("dgdl_bz_jobproperty", QCP.equals, dataEntity.getString("dgdl_bz_jobproperty")))
                    .and(new QFilter("dgdl_bz_laborreltype", QCP.equals, dataEntity.getString("dgdl_bz_laborreltype")))
                    .and(new QFilter("id", QCP.not_equals, dataEntity.getLong("id")));
            orgSonColl = QueryServiceHelper.query("dgdl_planmonth_bz", FILED_OR_RED, qFilterorg2.toArray());
            orgSonIds = orgSonColl.stream().map(f -> f.getLong("id")).collect(Collectors.toList());
        } else if (dgdl_bz_adminorg3 != null && dgdl_bz_adminorg4 == null) {
            //3级组织
            QFilter qFilterorg3 = new QFilter("dgdl_bz_adminorg3", QCP.is_notnull, null).and(new QFilter("dgdl_bz_adminorg3", QCP.not_equals, 0L))
                    .and(new QFilter("dgdl_bz_adminorg3", QCP.equals, dgdl_bz_adminorg3.getLong("id")))
                    .and(new QFilter("dgdl_planmonth_bill", QCP.equals, dataEntity.getLong("dgdl_planmonth_bill.id")))
                    .and(new QFilter("dgdl_bz_jobproperty", QCP.equals, dataEntity.getString("dgdl_bz_jobproperty")))
                    .and(new QFilter("dgdl_bz_laborreltype", QCP.equals, dataEntity.getString("dgdl_bz_laborreltype")))
                    .and(new QFilter("id", QCP.not_equals, dataEntity.getLong("id")));
            orgSonColl = QueryServiceHelper.query("dgdl_planmonth_bz", FILED_OR_RED, qFilterorg3.toArray());
            orgSonIds = orgSonColl.stream().map(f -> f.getLong("id")).collect(Collectors.toList());
        } else if (dgdl_bz_adminorg4 != null && dgdl_bz_adminorg5 == null) {
            //4级组织
            QFilter qFilterorg4 = new QFilter("dgdl_bz_adminorg4", QCP.is_notnull, null).and(new QFilter("dgdl_bz_adminorg4", QCP.not_equals, 0L))
                    .and(new QFilter("dgdl_bz_adminorg4", QCP.equals, dgdl_bz_adminorg4.getLong("id")))
                    .and(new QFilter("dgdl_planmonth_bill", QCP.equals, dataEntity.getLong("dgdl_planmonth_bill.id")))
                    .and(new QFilter("dgdl_bz_jobproperty", QCP.equals, dataEntity.getString("dgdl_bz_jobproperty")))
                    .and(new QFilter("dgdl_bz_laborreltype", QCP.equals, dataEntity.getString("dgdl_bz_laborreltype")))
                    .and(new QFilter("id", QCP.not_equals, dataEntity.getLong("id")));
            orgSonColl = QueryServiceHelper.query("dgdl_planmonth_bz", FILED_OR_RED, qFilterorg4.toArray());
            orgSonIds = orgSonColl.stream().map(f -> f.getLong("id")).collect(Collectors.toList());
        } else if (dgdl_bz_adminorg5 != null && dgdl_bz_adminorg6 == null) {
            //5级组织
            QFilter qFilterorg5 = new QFilter("dgdl_bz_adminorg5", QCP.is_notnull, null).and(new QFilter("dgdl_bz_adminorg5", QCP.not_equals, 0L))
                    .and(new QFilter("dgdl_bz_adminorg5", QCP.equals, dgdl_bz_adminorg5.getLong("id")))
                    .and(new QFilter("dgdl_planmonth_bill", QCP.equals, dataEntity.getLong("dgdl_planmonth_bill.id")))
                    .and(new QFilter("dgdl_bz_jobproperty", QCP.equals, dataEntity.getString("dgdl_bz_jobproperty")))
                    .and(new QFilter("dgdl_bz_laborreltype", QCP.equals, dataEntity.getString("dgdl_bz_laborreltype")))
                    .and(new QFilter("id", QCP.not_equals, dataEntity.getLong("id")));
            orgSonColl = QueryServiceHelper.query("dgdl_planmonth_bz", FILED_OR_RED, qFilterorg5.toArray());
            orgSonIds = orgSonColl.stream().map(f -> f.getLong("id")).collect(Collectors.toList());
        }
        //渲染颜色
        int month = 12;
        for (int i = 1; i <= month; i++) {
            Set<Long> forRedIds = new HashSet<>();//待飘红的行
            String dgdl_jzStr = "dgdl_jz" + i;
            String dgdl_bzStr = "dgdl_bz" + i;
            int dgdl_jz = dataEntity.getInt(dgdl_jzStr);
            int dgdl_bz = dataEntity.getInt(dgdl_bzStr);
            //编制>基准 飘红
            if (dgdl_bz > dgdl_jz) {
                logger.info("PlanMonthListPlugin获取基准数=" + dataEntity.get(dgdl_jzStr));
                if (dgdl_jz != 0) {
                    forRedIds.add(dataEntity.getLong("id"));
                }
            }
            //下级总和超上级 飘红
            if (orgSonColl != null) {
                int sum = orgSonColl.stream().mapToInt(f -> f.getInt(dgdl_bzStr)).sum();
                if (sum > dgdl_bz) {
                    orgSonIds = orgSonColl.stream().filter(bz -> bz.getInt(dgdl_bzStr) != 0).map(f -> f.getLong("id")).collect(Collectors.toList());
                    forRedIds.addAll(orgSonIds);
                }
            }
            for (int d = 0; d < data.size(); d++) {
                DynamicObject dynamicObject = data.get(d);
                if (forRedIds.contains(dynamicObject.getLong("id"))) {
                    serCell(d, cellStyles, dgdl_bzStr);
                }
            }
        }
        // 获取单据列表控件
        BillList billList = getView().getControl(BILLLISTID);
        billList.setCellStyle(cellStyles);
    }

    private static void serCell(int row, List<CellStyle> cellStyles, String dgdl_bzStr) {
        CellStyle cellStyle = new CellStyle();
        cellStyle.setRow(row);
        // 设置字段
        cellStyle.setFieldKey(dgdl_bzStr);
        // 字体颜色
        cellStyle.setForeColor("#ff0000");
        cellStyles.add(cellStyle);
    }
}
