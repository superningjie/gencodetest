package tdkw.hrmp.hrobs.formplugin.monitor;


import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;
import tdkw.hrmp.hrobs.common.monitor.MonitorUtil;

import java.util.EventObject;

/**
 * @description: PC运营监控监听按钮点击事件
 * @author xxx
 * @date: 2023/10/24 14:25
 * @param:
 * @param: null
 * @return: null
 **/
public class CommonButtonFormPlugin extends AbstractFormPlugin {


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 人员分析按钮监听
        this.addClickListeners("tdkw_querybtn");
        // 入职、离职分析按钮监听
        this.addClickListeners("tdkw_query");
    }


    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        if (key.equals("tdkw_querybtn") || key.equals("tdkw_query")) {
            DynamicObject dataEntity = this.getModel().getDataEntity();
            // 应用名称
            String appName = this.getView().getParentView().getFormShowParameter().getFormName();
            // 应用标识
            String appId = this.getView().getParentView().getFormShowParameter().getAppId();
            // 操作名称
            String operaName = this.getView().getFormShowParameter().getFormName();
            // 操作id
            String operaId = "";
            try {
                operaId = this.getView().getFormShowParameter().getPageId().split("root")[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
            //String query = DynamicObjectSerializeUtil.serialize(dataEntity, dataEntity.getDynamicObjectType());
            MonitorUtil.save(appId, appName, operaId, operaName, "button", "", "1");
        }
    }
}
