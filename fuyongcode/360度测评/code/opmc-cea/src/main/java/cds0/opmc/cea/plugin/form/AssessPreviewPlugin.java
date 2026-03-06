package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessActivityEntityService;
import cds0.opmc.cea.business.entityservice.AssessFormEntityService;
import cds0.opmc.cea.business.entityservice.DimSettingEntityService;
import cds0.opmc.cea.common.enums.AssessFormTypeEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.form.FormShowParameter;
import kd.bos.form.StyleCss;
import kd.bos.form.container.Container;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.metadata.form.container.FlexPanelAp;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.sdk.hr.hspm.formplugin.web.file.ermanfile.drawutil.ApCreateUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static kd.bos.form.ShowType.InContainer;

public class AssessPreviewPlugin extends AbstractFormPlugin {

    protected static final AssessActivityEntityService assessActivityEntityService = AssessActivityEntityService.getInstance();
    protected static final AssessFormEntityService assessFormEntityService = AssessFormEntityService.getInstance();
    protected static final DimSettingEntityService dimSettingEntityService = DimSettingEntityService.getInstance();

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterBindData(e);
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessActivityId = parameter.getCustomParam("assessActivityId");
        Long dimGroupId = parameter.getCustomParam("dimGroupId");
        loadData(dimGroupId, assessActivityId);
    }

    public void loadData(Long dimGroupId, Long assessActivityId) {
        DynamicObject assessActivity = assessActivityEntityService.queryOne(assessActivityId);
        if (HRStringUtils.isEmpty((String) assessActivity.get("introcontent"))) {
            this.getView().setVisible(false, "cds0_flexpanelap1");
        }
        if (assessActivity != null) {
            //设置周期
            this.getModel().setValue("cds0_textfield", getCycle(assessActivity.getDate("periodstartdate"), assessActivity.getDate("periodenddate")));
        }
        //根据分组id 查询分组信息
        DynamicObject dimSettingEntity = dimSettingEntityService.queryDimSettingByPk(dimGroupId);
        if(dimSettingEntity == null){
            return;
        }
        //设置分组
        this.getModel().setValue("cds0_textfield1", dimSettingEntity.get("groupdimname"));
        //设置提示语
        this.getModel().setValue("cds0_basedatafield1", assessActivityId);
        //设置测评表名称
        Long assessFormId = Long.valueOf(dimSettingEntity.get("assessform.id").toString());
        this.getModel().setValue("cds0_basedatafield11", assessFormId);
        //根据表名获取表下所有指标
        DynamicObject assessForm = assessFormEntityService.queryOneByPk(assessFormId);
        if(assessForm == null){
            return;
        }
        //设置绩效等级
        String levelMapNumber = assessForm.get("levelmapnumber").toString();
        //设置测评表类型
        boolean isSingleChoice = false;
        if (assessForm.get("assformtype").toString().equals(AssessFormTypeEnum.GRADE.getValue())) {
            isSingleChoice = true;
        }
        //通过 绩效等级 总的指标 总的测评对象 show指标测评项
        showAssessPreviewExample(this.getView().getControl("cds0_flexpanelap9"), levelMapNumber, isSingleChoice, (DynamicObjectCollection) assessForm.get("entryentity"));
    }

    //根据开始时间结束时间计算周期标识
    public String getCycle(Date star, Date end) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(star) + "~" + df.format(end);
    }

    public void showAssessPreviewExample(Container container, String levelMapNumber, boolean isSingleChoice, DynamicObjectCollection assessFormRows) {
        List<DynamicObject> assessFormRowList = assessFormRows.stream().sorted(new Comparator<DynamicObject>() {
            @Override
            public int compare(DynamicObject entry1, DynamicObject entry2) {
                //获取分组排序
                int seq1 = (int) entry1.get("indicatorindex");
                int seq2 = (int) entry2.get("indicatorindex");
                if (seq1 > seq2) {
                    return 1;
                } else if (seq1 == seq2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }).collect(Collectors.toList());
        int index = 0;
        for (DynamicObject assessFormRow : assessFormRowList) {
            FlexPanelAp flexPanelAp = ApCreateUtils.createNewFlexAp("cds0_cea_previewexample" + index, "cds0_cea_previewexample" + index);
            flexPanelAp.setParentId("cds0_flexpanelap9");
            flexPanelAp.setWidth(new LocaleString("100%"));
            List<Map<String, Object>> items = new ArrayList<>();
            items.add(flexPanelAp.createControl());
            container.addControls(items);
            FormShowParameter parameter = new FormShowParameter();
            parameter.setFormId("cds0_cea_previewexample");
            parameter.getOpenStyle().setShowType(InContainer);
            parameter.getOpenStyle().setTargetKey("cds0_cea_previewexample" + index++);
            //设置绩效等级 测评表类型 指标id 测评对象ids
            parameter.setCustomParam("levelMapNumber", levelMapNumber);
            parameter.setCustomParam("isSingleChoice", isSingleChoice);
            parameter.setCustomParam("assessFormRowId", assessFormRow.get("id"));
            parameter.setHasRight(Boolean.TRUE);
            StyleCss styleCss = new StyleCss();
            styleCss.setWidth("100vh");
            parameter.getOpenStyle().setInlineStyleCss(styleCss);
            this.getView().showForm(parameter);
        }
    }
}
