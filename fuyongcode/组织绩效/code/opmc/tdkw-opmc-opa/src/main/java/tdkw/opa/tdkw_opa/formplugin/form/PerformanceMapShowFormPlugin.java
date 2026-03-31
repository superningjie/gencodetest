package tdkw.opa.tdkw_opa.formplugin.form;

import kd.bos.form.CloseCallBack;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QFilter;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.util.EventObject;
import java.util.List;

/**
 * @author: xxx
 * @create: 2024/08/29 11:42
 * @description:
 **/
public class PerformanceMapShowFormPlugin extends AbstractFormPlugin {
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //创建弹出列表界面对象，ListShowParameter 表示弹出页面为列表界面
        ListShowParameter listShowParameter = new ListShowParameter();
        //设置FormId，列表的FormId固定为"bos_list"
        listShowParameter.setFormId("tdkw_performance_map_temp");
        //设置BillFormId，为列表所对应单据的标识
        listShowParameter.setBillFormId("tdkw_performance_map_bill");
        //设置弹出页面标题
        listShowParameter.setCaption("");
        //设置弹出页面的打开方式
        listShowParameter.getOpenStyle().setShowType(ShowType.InContainer);
        listShowParameter.getOpenStyle().setTargetKey("tdkw_tabpageap");

        //设置为不能多选，如果为true则表示可以多选
        listShowParameter.setMultiSelect(false);
        //设置子页面关闭回调参数，回调标识为XXX
        listShowParameter.setCloseCallBack(new CloseCallBack(this, "XXX"));
        //弹出列表界面
        this.getView().showForm(listShowParameter);
    }
}
