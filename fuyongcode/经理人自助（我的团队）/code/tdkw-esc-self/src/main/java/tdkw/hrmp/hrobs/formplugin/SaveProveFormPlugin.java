package tdkw.hrmp.hrobs.formplugin;

import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;

public class SaveProveFormPlugin extends AbstractFormPlugin {

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        FormOperate formOperate = (FormOperate)args.getSource();
        if ("submit".equals(formOperate.getOperateKey())){
            // TODO 在此添加业务逻辑
            this.getView().invokeOperation("save");
        }
        super.beforeDoOperation(args);
    }
}
