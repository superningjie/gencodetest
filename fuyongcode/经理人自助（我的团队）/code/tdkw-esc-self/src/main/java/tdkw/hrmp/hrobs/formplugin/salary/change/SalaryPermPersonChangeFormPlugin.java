package tdkw.hrmp.hrobs.formplugin.salary.change;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.param.CustomParam;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.events.BeforeFilterF7SelectEvent;
import kd.bos.list.BillList;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.*;

public class SalaryPermPersonChangeFormPlugin extends AbstractListPlugin {

    private static final String[] variationType = new String[]{"XY00003", "XY00001", "102090_S", "102060_S", "101200_S", "101140_S", "101130_S", "101120_S", "101110_S", "101100_S", "101060_S", "101070_S", "101020_S"};

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        BillList billList = this.getControl(BILLLISTID);
        billList.setOrderBy("tdkw_change_time desc,tdkw_change_pos.index asc");
    }

    @Override
    public void filterContainerBeforeF7Select(BeforeFilterF7SelectEvent args) {
        super.filterContainerBeforeF7Select(args);
        if (StringUtils.equals("tdkw_change_chgcategory.id", args.getFieldName())) {
            List<QFilter> qFilters = new ArrayList<>();
            Map<String, String> parameterHelper = SystemParamServiceHelper.loadCustomParameterFromCache(new CustomParam());
            String salaryPermChangeType = parameterHelper.get("SALARY_PERM_CHANGE_TYPE");

            QFilter qFilter = new QFilter("1", QCP.equals, 1);
            if (StringUtils.isBlank(salaryPermChangeType)) {
                qFilter.and("number", QCP.in, variationType);
            } else {
                String[] salaryPermChangeTypes = salaryPermChangeType.split(",");
                qFilter.and("number", QCP.in, salaryPermChangeTypes);
            }
            qFilters.add(qFilter);
            args.setQfilters(qFilters);
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BillList billList = this.getControl(BILLLISTID);
        billList.setOrderBy("tdkw_change_time desc,tdkw_change_pos.index asc");
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        BillList billList = this.getControl(BILLLISTID);
        switch (evt.getItemKey()) {
            // 无需调整
            case "tdkw_without":
                permissionAdjustments(billList, "1");
                break;
            // 已发起权限变更申请
            case "tdkw_application":
                permissionAdjustments(billList, "2");
                break;
            // 取消确认
            case "tdkw_unconfirm":
                permissionAdjustments(billList, "3");
                break;
        }
        billList.refresh();
    }

    /**
     * 权限调整确认
     *
     * @param billList 单据列表
     * @param value    权限值
     */
    private void permissionAdjustments(BillList billList, String value) {
        Object[] primaryKeyValues = billList.getSelectedRows().getPrimaryKeyValues();
        DynamicObject[] load = BusinessDataServiceHelper.load(billList.getBillFormId(), "tdkw_change_confirm,tdkw_operator,tdkw_operationtime", new QFilter("id", QCP.in, primaryKeyValues).toArray());
        long cqId = UserServiceHelper.getCurrentUserId();
        Long hrUser = HRRoleAndPersonUtils.getHRUser(cqId);
        for (DynamicObject dynamicObject : load) {
            dynamicObject.set("tdkw_change_confirm", value);
            dynamicObject.set("tdkw_operator", hrUser);
            dynamicObject.set("tdkw_operationtime", new Date());
        }
        SaveServiceHelper.save(load, OperateOption.create());
    }
}