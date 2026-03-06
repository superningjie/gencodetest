package tdkw.opa.tdkw_opa.formplugin.basedata;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;

/**
 * @author: xxx
 * @create: 2024/09/12 15:59
 * @description:
 **/
public class UsageCategoryFormPlugin extends AbstractBasePlugIn {
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        String operateKey = formOperate.getOperateKey();
        // 提交
        if (StringUtils.equals("save", operateKey)) {
            this.getView().close();
        }
    }
}
