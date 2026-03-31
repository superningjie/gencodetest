package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

public class AddAreaFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("btnok");
        BasedataEdit areaType = this.getControl("tdkw_area_type");
        areaType.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);
        Control source = (Control)evt.getSource();
        if (StringUtils.equals("btnok", source.getKey())){
            DynamicObject areaType = (DynamicObject) this.getModel().getValue("tdkw_area_type");
            if (areaType == null){
                this.getView().showTipNotification("请选择区域类型");
                evt.setCancel(true);
            }

        }
    }


    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        String key = source.getKey();
        //点击确定
        if (StringUtils.equals("btnok", key)) {
            DynamicObject areaType = (DynamicObject) this.getModel().getValue("tdkw_area_type");
            this.getView().returnDataToParent(areaType);
            this.getView().close();
        }

    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        String name = event.getProperty().getName();
        if (StringUtils.equals("tdkw_area_type",name)){
            List<String> typeNumberList = this.getView().getFormShowParameter().getCustomParam("typeNumberList");
            if (typeNumberList!=null && typeNumberList.size()>0){
                event.getCustomQFilters().add(new QFilter("number", QCP.not_in,typeNumberList));
            }
            String orgType = this.getView().getFormShowParameter().getCustomParam("orgType");
            // 这是行政组织的类型，不能改
            // 标准化处理 替换项目上的编码
            // 集团、公司、区域
            List<String> orgTypeList = Arrays.asList("1010_S","1020_S","1030_S");
            if (StringUtils.isNotBlank(orgType)){
                if (orgTypeList.contains(orgType)){
                    // 指标类型-集团
                    event.getCustomQFilters().add(new QFilter("number", QCP.equals,"T0001"));
                }else {
                    // 指标类型-公司、部门、区域
                    event.getCustomQFilters().add(new QFilter("number", QCP.in,Arrays.asList("T0002","T0003","T0004")));
                }
            }
        }
    }
}
