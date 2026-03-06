package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.FormShowParameter;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.CollectionUtils;

import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pjj
 * @date 2024-08-21
 * 关联对齐弹窗插件
 */
public class AlignMentWindowPlugin extends AbstractFormPlugin {

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> customParams = formShowParameter.getCustomParams();
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("tdkw_org_perf_metrics", new QFilter[]{new QFilter("id", QCP.equals, customParams.get("targetBillId"))});
        //动态表单分录
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
        if(!ObjectUtils.isEmpty(dynamicObject)){
            //隐藏父分录
            DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection("tdkw_hideentry");
            //过滤kpi类数据
            List<DynamicObject> collect = dynamicObjectCollection.stream().filter(i -> i.getString("tdkw_area_type.number").equals("T0001")).collect(Collectors.toList());
            if( !ObjectUtils.isEmpty(collect) ){
                DynamicObjectCollection sonEntry = collect.get(0).getDynamicObjectCollection("tdkw_subentryentity");
                if(CollectionUtils.isNotEmpty(sonEntry)) {
                    //循环新增行
                    for (DynamicObject object : sonEntry) {
                        DynamicObject row = entryEntity.addNew();
                        row.set("tdkw_metric",object.getDynamicObject("tdkw_metric"));
                        row.set("tdkw_weight",object.getString("tdkw_weight"));
                        row.set("tdkw_target_value",object.getBigDecimal("tdkw_target_value"));
                        row.set("tdkw_unit",object.getString("tdkw_unit"));
                    }
                }
            }
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate)args.getSource();
        String operateKey = formOperate.getOperateKey();
        if(StringUtils.equals(operateKey,"confirm")){
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("tdkw_entryentity");
            List<DynamicObject> collect = entryEntity.stream().filter(i -> i.getBoolean("tdkw_checkboxfield")).collect(Collectors.toList());
            if(!ObjectUtils.isEmpty(collect)){
                this.getView().returnDataToParent(collect);
            }
            this.getView().close();
        }
    }
}
