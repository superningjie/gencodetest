package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pjj
 * @date 2024-08-27
 * 承载分解单和对齐单的表单插件
 */
public class DecomposeAlignFormPlugin extends AbstractFormPlugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Object targetBillId = formShowParameter.getCustomParam("targetBillId");
        String Status = this.getView().getFormShowParameter().getCustomParam("OperationStatus");

        QFilter qFilter = new QFilter("tdkw_targetid", QCP.equals, targetBillId);
        DynamicObject decompose = BusinessDataServiceHelper.loadSingle("tdkw_target_decompose", new QFilter[]{qFilter});
        BillShowParameter decomposeParameter = new BillShowParameter();
        decomposeParameter.setCustomParams(formShowParameter.getCustomParams());
        //内嵌动态表单
        decomposeParameter.setFormId("tdkw_target_decompose");
        //打开状态
        if (ObjectUtils.isEmpty(decompose)) {
            decomposeParameter.setStatus(OperationStatus.ADDNEW);
        } else {
            decomposeParameter.setStatus(OperationStatus.valueOf(Status));
            decomposeParameter.setPkId(decompose.getLong("id"));
        }
        //打开方式为页签
        decomposeParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
        //父容器为页签控件
        decomposeParameter.getOpenStyle().setTargetKey("tdkw_tabap");
        //标题
        decomposeParameter.setCaption("指标分解");
        this.getView().showForm(decomposeParameter);

        DynamicObject alignment = BusinessDataServiceHelper.loadSingle("tdkw_target_alignment", new QFilter[]{qFilter});
        BillShowParameter alignmentParameter = new BillShowParameter();
        alignmentParameter.setCustomParams(formShowParameter.getCustomParams());
        alignmentParameter.setFormId("tdkw_target_alignment");
        if (ObjectUtils.isEmpty(alignment)) {
            alignmentParameter.setStatus(OperationStatus.ADDNEW);
        } else {
            updateDate(alignment, formShowParameter);
            alignmentParameter.setStatus(OperationStatus.valueOf(Status));
            alignmentParameter.setPkId(alignment.getLong("id"));
        }
        alignmentParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
        alignmentParameter.getOpenStyle().setTargetKey("tdkw_tabap");
        alignmentParameter.setCaption("指标对齐");
        this.getView().showForm(alignmentParameter);
    }

    private void updateDate(DynamicObject alignment, FormShowParameter formShowParameter) {
        DynamicObjectCollection dynamicObjectCollection = alignment.getDynamicObjectCollection("tdkw_targetalientry");
        List<Long> alignmentTargetList = dynamicObjectCollection.stream().map(i -> i.getLong("tdkw_findctid.id")).collect(Collectors.toList());
        Date year;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            year = format.parse(formShowParameter.getCustomParam("year"));
        } catch (ParseException ex) {
            throw new KDException(ex, new ErrorCode("TXException", ex.getMessage()));
        }
        JSONObject json = formShowParameter.getCustomParam("org");
        //上级分解给本组织的数据
        DynamicObjectCollection query = QueryServiceHelper.query("tdkw_target_decompose", "id,tdkw_parentadminorg as orgid,tdkw_entryentity.tdkw_target.id as targetid," +
                        "tdkw_entryentity.tdkw_target.name as targetname,tdkw_entryentity.tdkw_target.tdkw_description as targetdesc,tdkw_entryentity.tdkw_target.tdkw_metric_type.id as typeid," +
                        "tdkw_entryentity.tdkw_subentryentity.tdkw_decomposedesc as decomposedesc",
                new QFilter[]{new QFilter("tdkw_org_year", QCP.equals, year).and("tdkw_entryentity.tdkw_subentryentity.tdkw_bedecomposeadmin.id", QCP.equals, json.getLong("id"))});
        List<Long> decomposeTargetList = query.stream().map(i -> i.getLong("targetid")).collect(Collectors.toList());

        //交集
        Collection<Long> intersection = CollectionUtils.intersection(decomposeTargetList, alignmentTargetList);
        //需要增加的集合
        Collection<Long> addCollection = CollectionUtils.subtract(decomposeTargetList, intersection);
        //需要删除的集合
        Collection<Long> delCollection = CollectionUtils.subtract(alignmentTargetList, intersection);

        for (int i = dynamicObjectCollection.size() - 1; i >= 0; i--) {
            DynamicObject row = dynamicObjectCollection.get(i);
            long targetId = row.getLong("tdkw_findctid.id");
            if (delCollection.contains(targetId)) {
                dynamicObjectCollection.remove(i);
            }
        }

        for (DynamicObject dynamicObject : query) {
            if (addCollection.contains(dynamicObject.getLong("targetid"))) {
                DynamicObject newEntryRow = dynamicObjectCollection.addNew();
                newEntryRow.set("tdkw_decomposebillid", dynamicObject.getLong("id"));
                newEntryRow.set("tdkw_sourceadmin", dynamicObject.getLong("orgid"));
                newEntryRow.set("tdkw_findctid", dynamicObject.getLong("targetid"));
                newEntryRow.set("tdkw_findctid_name", dynamicObject.getString("targetname"));
                newEntryRow.set("tdkw_findcttype", dynamicObject.getLong("typeid"));
                newEntryRow.set("tdkw_findctdesc", dynamicObject.getString("targetdesc"));
                newEntryRow.set("tdkw_decomposedesc", dynamicObject.getString("decomposedesc"));
                newEntryRow.set("tdkw_alignmentstatus", "20");
            }
        }
        SaveServiceHelper.save(new DynamicObject[]{alignment});

    }
}
