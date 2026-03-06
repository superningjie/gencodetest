package tdkw.hr.odc.haos.formplugin;

import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.events.BeforeImportEntryEventArgs;
import kd.bos.entity.plugin.ImportLogger;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.plugin.importentry.resolving.ImportEntryData;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;
import org.apache.commons.lang3.StringUtils;
import tdkw.hr.odc.haos.common.StaffExtHelper;

import java.util.*;

/**
 * @ClassName : StaffWork
 * @Description : 编制信息维护表单扩展插件（提交 、查看流程图、引入干预）
 * @Author : XYP
 * @Date: 2024-08-03 10:42
 */
public class StaffExtEditPlugin extends HRDataBaseEdit {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        Map<String, Object> map = new HashMap<>();
//        map.put("teci", "tdkw_teci");
        this.getView().updateControlMetadata("bentryentity", map);
        if (this.getView().getFormShowParameter().getStatus().equals(OperationStatus.EDIT)){
            this.getView().setVisible(false,"tdkw_doedit");
        }else if (this.getView().getFormShowParameter().getStatus().equals(OperationStatus.VIEW)){
            this.getView().setVisible(true,"tdkw_doedit");
        }
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String itemKey = evt.getItemKey();
        String tdkw_staffflowstatus = (String) this.getModel().getValue("tdkw_staffflowstatus");
        if (StringUtils.equals("tdkw_tj", itemKey)) {//提交
            if (!tdkw_staffflowstatus.equals("")){
                if (tdkw_staffflowstatus.equals("D") || tdkw_staffflowstatus.equals("B")) {
                    this.getView().showTipNotification("单据在流程中,不允许操作!");
                    evt.setCancel(true);
                } else if (tdkw_staffflowstatus.equals("C")) {
                    this.getView().showTipNotification("单据已审批通过,不允许操作!");
                    evt.setCancel(true);
                }
            }
        } else if (StringUtils.equals("tdkw_viewflowchart", itemKey)) {//查看流程图
            Long tdkw_staffid = (Long) this.getModel().getValue("tdkw_staffid");
            WorkflowServiceHelper.viewFlowchart(this.getView().getPageId(), tdkw_staffid);
        } else if (StringUtils.equals("tdkw_doedit", itemKey) || StringUtils.equals("bar_enable", itemKey)) {//变更启用
            if (!tdkw_staffflowstatus.equals("")){
                if (tdkw_staffflowstatus.equals("D") || tdkw_staffflowstatus.equals("B")) {
                    this.getView().showTipNotification("单据在流程中,不允许操作!");
                    evt.setCancel(true);
                }else {
                    doedit();
                }
            }else {
                doedit();
            }
        } else if (StringUtils.equals("bar_modify", itemKey)) {
            if (tdkw_staffflowstatus.equals("C")) {
                this.getView().showTipNotification("单据已审批通过,不允许操作!");
                evt.setCancel(true);
            } else if (tdkw_staffflowstatus.equals("D") || tdkw_staffflowstatus.equals("B")) {
                this.getView().showTipNotification("单据在流程中,不允许操作!");
                evt.setCancel(true);
            }
        }
    }

    private void doedit() {
        this.getView().setStatus(OperationStatus.EDIT);
        this.getView().setVisible(Boolean.TRUE, new String[]{"bar_save"});
        this.getView().setVisible(Boolean.FALSE, new String[]{"tdkw_doedit", "bar_enable", "bar_disable"});
        this.getView().getPageCache().remove("showDisable");
        this.getView().setVisible(Boolean.FALSE, new String[]{"flexpanelap9"});
        this.getView().setFormTitle(new LocaleString(ResManager.loadKDString("变更编制信息-", "OrgStaffMainEdit_2", "hrmp-haos-formplugin", new Object[0]).concat(this.getModel().getDataEntity().getString("name"))));
        this.getView().updateView();
        this.getModel().setDataChanged(false);
    }

    /**
     * 引入干预逻辑
     * @param e
     */
    @Override
    public void beforeImportEntry(BeforeImportEntryEventArgs e) {
        super.beforeImportEntry(e);
        Map<String, List<ImportEntryData>> source = (Map) e.getSource();
        if (source.containsKey("fentryentity_import")){
            List<ImportEntryData> entry = (List) source.get("fentryentity_import");
            if (entry.size() != 0) {
                Map<Integer, String> indexVsMsgMap = validateEntry(entry);
                //校验并记录错误
                Set<Integer> indexSet = indexVsMsgMap.keySet();
                Map<String, List<Object>> logMap = e.getEntryDataMap();
                ImportLogger importLogger = (ImportLogger) logMap.get("fentryentity_import").get(0);
                Set<Map.Entry<Integer, String>> indexVsMsgEntries = indexVsMsgMap.entrySet();
                //干预导入结果里成功、失败条数
                for (Map.Entry<Integer, String> indexVsMsgEntry : indexVsMsgEntries) {
                    // 封装错误信息
                    Integer index = indexVsMsgEntry.getKey();
                    importLogger.log(index, indexVsMsgEntry.getValue());
                    importLogger.fail();
                    importLogger.setTotal(importLogger.getTotal() + 1);
                }
                //干预导出错误结果，移除后excel无该条数据
                Iterator<ImportEntryData> iterator = entry.iterator();
                while (iterator.hasNext()) {
                    ImportEntryData entryData = iterator.next();
                    Integer rowNum = (Integer) entryData.getData().get("rowNum");
                    if (indexSet.contains(rowNum)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private Map<Integer, String> validateEntry(List<ImportEntryData> entry) {
        //存储错误信息 k:引入数据行号 v:错误信息
        Map<Integer, String> resultMap = new HashMap<>();
        //引入不为空时查询出所有职级
        Map<String, Long> map = new HashMap<>();
        Map<String, Long> mapjob = new HashMap<>();
        if (entry.size() > 0) {
            //获取当前页面组织
            DynamicObject org = (DynamicObject) this.getModel().getValue("org");
            List<QFilter> qFilters = new ArrayList<>();
            qFilters.add(new QFilter("enable", QCP.equals, "1"));
            if (org != null) {
                Set<Long> idSet = StaffExtHelper.queryJoblevelscmIdsByOrgId(org.getLong("id"));
                qFilters.add(new QFilter("joblevelscm.id", QCP.in, idSet));
            }
            QFilter[] Filters = new QFilter[qFilters.size()];
            qFilters.toArray(Filters);
            //查询职级
            DynamicObject[] jobleve = BusinessDataServiceHelper.load("hbjm_joblevelhr", "number,joblevelscm",Filters);
            for (DynamicObject dy : jobleve) {
                String number = dy.getString("number");
                String joblevelscm = dy.getDynamicObject("joblevelscm").getString("number");
                map.put(number + "_" + joblevelscm, dy.getLong("id"));
            }
            //查询职级方案
            DynamicObject[] joblevelscm = BusinessDataServiceHelper.load("hbjm_joblevelscmhr", "id,number", new QFilter[]{
                    new QFilter("iscurrentversion", QCP.equals, "1")
            });
            for (DynamicObject dyjob : joblevelscm) {
                mapjob.put(dyjob.getString("number"), dyjob.getLong("id"));
            }
        }
        for (ImportEntryData importEntryDatum : entry) {
            JSONObject data = importEntryDatum.getData();
            Integer rowNum = data.getInteger("rowNum");
            //获取职级方案
            JSONObject tdkw_joblevelscm = (JSONObject) data.get("tdkw_joblevelscm");
            String joblevelscm = (String) tdkw_joblevelscm.get("number");
            if (mapjob.get(joblevelscm) != null) {
                tdkw_joblevelscm.put("id", mapjob.get(joblevelscm));
            } else {
                resultMap.put(rowNum, "职级方案编码" + joblevelscm + ":数据不存在或不符合条件!");
            }
            //获取职级
            JSONObject fbasicdata1_import = (JSONObject) data.get("fbasicdata1_import");
            String number = (String) fbasicdata1_import.get("number");
            fbasicdata1_import.put("importprop","id");
            String a = number + "_" + joblevelscm;
            if (map.get(a) != null) {
                fbasicdata1_import.put("id", map.get(a));
            } else {
                resultMap.put(rowNum, "职级编码" + number + ":根据当前组织和职级方案未找到相关职级!");
            }
        }
        return resultMap;
    }

}
