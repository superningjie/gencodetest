package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.collections4.CollectionUtils;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;
import tdkw.opa.tdkw_opa.formplugin.utils.HRRoleAndPersonUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UseAnnualGoalFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {

    private static final Log logger = LogFactory.getLog(UseAnnualGoalFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        Button btnOK = this.getView().getControl("btnok");
        btnOK.addClickListener(this);

        BasedataEdit fieldEdit = this.getView().getControl("tdkw_target_user");
        fieldEdit.addBeforeF7SelectListener(this);
    }


    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String fieldKey = e.getProperty().getName();
        if (StringUtils.equals(fieldKey,"tdkw_target_user")) {
            DynamicObject newValue = (DynamicObject) e.getChangeSet()[0].getNewValue();
            if (newValue!=null) {
                FormShowParameter formShowParameter = this.getView().getFormShowParameter();
                int year = formShowParameter.getCustomParam("year");
                // 查询获取DynamicObjectCollection对象
                QFilter qFilter = new QFilter("tdkw_target_users", QCP.equals, newValue.getPkValue());
                qFilter.and("YEAR(tdkw_execute_plan_base.tdkw_year)", QCP.equals, year);
                DynamicObjectCollection myTargets = QueryServiceHelper.query(EntityName.BASE_MY_TARGET,
                        "tdkw_treeentryentity.tdkw_indctrname1 AS indctrname, " +
                                "tdkw_treeentryentity.tdkw_level1 AS level, " +
                                "tdkw_treeentryentity.tdkw_weight1 AS weight, " +
                                "tdkw_treeentryentity.tdkw_parentid AS parentid, " +
                                "tdkw_treeentryentity.tdkw_entryid AS entryid, " +
                                "tdkw_treeentryentity.tdkw_evaltype1 AS evaltype, " +
                                "tdkw_treeentryentity.tdkw_indctrdesc1 AS indctrdesc",
                        qFilter.toArray());

                // 处理数据，建立子父关系
                Map<Long, List<DynamicObject>> childrenMap = new HashMap<>();
                Map<Long, DynamicObject> entryMap = new HashMap<>();
                processEntries(myTargets, childrenMap, entryMap);

                // 展示数据到单据体
                displayEntries(0L, "", 1, childrenMap);
            }else {
                this.getModel().deleteEntryData("tdkw_entryentity");
            }
        }

    }



    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control)evt.getSource();
        if (StringUtils.equals("btnok", source.getKey())){
            EntryGrid entryGrid = this.getView().getControl("tdkw_entryentity");
            int[] selectRows = entryGrid.getSelectRows();
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");

            if (selectRows != null && selectRows.length > 0) {
                // 创建一个新的集合，用于存储选中的行数据
                DynamicObjectCollection selectedEntries = new DynamicObjectCollection();

                for (int rowIndex : selectRows) {
                    DynamicObject selectedRow = entryEntity.get(rowIndex);
                    selectedEntries.add(selectedRow);
                }

                // 返回选中的行数据给父视图
                this.getView().returnDataToParent(selectedEntries);
            }

            this.getView().close();
        }
    }



    private void processEntries(DynamicObjectCollection myTargets, Map<Long, List<DynamicObject>> childrenMap, Map<Long, DynamicObject> entryMap) {
        for (DynamicObject entry : myTargets) {
            Long entryId = entry.getLong("entryid");
            Long parentId = entry.getLong("parentid");

            entryMap.put(entryId, entry);
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(entry);
        }
    }

    private void displayEntries(Long parentId, String prefix, int level, Map<Long, List<DynamicObject>> childrenMap) {
        List<DynamicObject> children = childrenMap.get(parentId);
        if (children != null) {
            int index = 1;
            for (DynamicObject child : children) {
                String currentPrefix = prefix.isEmpty() ? String.valueOf(index) : prefix + "." + index;
                String name = child.getString("indctrname");
                // 获取权重并加上百分比符号
                BigDecimal weight = child.getBigDecimal("weight");
                String weightWithPercent = weight != null ? weight.toString() + "%" : "";

                String evaltype = child.getString("evaltype");
                String indctrdesc = child.getString("indctrdesc");

                // 创建新的行并设置序号和目标名称
                int rowIndex = this.getModel().createNewEntryRow("tdkw_entryentity");
                this.getModel().setValue("tdkw_serial_number", currentPrefix, rowIndex);
                this.getModel().setValue("tdkw_target_name", name, rowIndex);
                this.getModel().setValue("tdkw_weight", weightWithPercent, rowIndex);
                this.getModel().setValue("tdkw_evaltype", evaltype, rowIndex);
                this.getModel().setValue("tdkw_indctrdesc", indctrdesc, rowIndex);




                // 递归处理子节点
                displayEntries(child.getLong("entryid"), currentPrefix, level + 1, childrenMap);
                index++; // 在每次递归结束后正确地递增 index
            }
        }
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        long currUserId = RequestContext.get().getCurrUserId();
        AuthorizedOrgResult orgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, EntityName.BILL_ORG_PERF_METRICS, "tdkw_adminorg");
        boolean hasAllOrgPerm = orgSet.isHasAllOrgPerm();
        IDataModel parentModel = this.getView().getParentView().getParentView().getModel();
        DynamicObject adminOrg= (DynamicObject) parentModel.getValue("tdkw_adminorg");
//        long orgId = adminOrg.getLong("id");
        long orgId = 100000L;

        //获取下级组织(包括本组织)
        List<Long> orgIdList = HRRoleAndPersonUtils.getHROrgIds(String.valueOf(orgId));
        if (!hasAllOrgPerm) {
            List<Long> hasPermOrgs = orgSet.getHasPermOrgs();
            Collection<Long> intersection = CollectionUtils.intersection(hasPermOrgs, orgIdList);
            logger.info("intersection:{}",intersection);
//            orgIdList.retainAll(hasPermOrgs);
            QFilter qFilter = new QFilter("hrpi_empposorgrel.adminorg.id", QCP.in, intersection);
            event.addCustomQFilter(qFilter);
        } else {
            logger.info("有所有组织权限hasAllOrgPerm" + hasAllOrgPerm);
            QFilter qFilter = new QFilter("hrpi_empposorgrel.adminorg.id", QCP.in, orgIdList);
            event.addCustomQFilter(qFilter);
        }
    }
}
