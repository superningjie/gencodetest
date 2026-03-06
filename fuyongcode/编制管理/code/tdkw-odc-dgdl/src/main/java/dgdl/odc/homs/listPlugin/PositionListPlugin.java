package dgdl.odc.homs.listPlugin;

import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.ListShowParameter;
import kd.bos.list.plugin.AbstractListPlugin;

import java.util.EventObject;

/**
 * @author WQG
 * @version 1.0
 * @date 2023/10/31 10:30
 * @description: 实际岗位列表点击变动明细按钮跳转变动明细列表
 **/
public class PositionListPlugin extends AbstractListPlugin {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("dgdl_chgrecord");
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("dgdl_chgrecord".equals(itemKey)){
            //弹窗
            ListShowParameter listShowParameter = new ListShowParameter();
            //设置FormId，列表的FormId固定为bos_list
            listShowParameter.setFormId("bos_list");
            //设置BillFormId，为列表所对应单据的标识
            listShowParameter.setBillFormId("hbpm_chgrecord");
            //设置弹出页面的打开方式
            listShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            //绑定此页面
            this.getView().showForm(listShowParameter);
        }
    }
}
