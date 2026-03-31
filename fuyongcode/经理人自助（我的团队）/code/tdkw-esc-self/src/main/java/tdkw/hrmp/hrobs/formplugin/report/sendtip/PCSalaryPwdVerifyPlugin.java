package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import tdkw.hrmp.hrobs.formplugin.salary.PCSalarySlipHelper;


import java.util.Map;


public class PCSalaryPwdVerifyPlugin extends AbstractFormPlugin {

    /**
     * 新密码
     */
    public static final String KEY_NEWPAWD = "tdkw_newpwd";

    /**
     * 确认密码
     */
    public static final String KEY_CONFIRMPWD = "tdkw_confirmpwd";

    /**
     * 确认操作
     */
    public static final String DONOTHING_CONFIEM = "donothing_confirm";

    /**
     * 登录页面标识
     */
    private static final String HSPP_PCPWDLOGIN = "tdkw_hrobs_pcpwdlogin";

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs e) {
        super.beforeDoOperation(e);
        String formOp = ((FormOperate) e.getSource()).getOperateKey();
        switch (formOp) {
            case DONOTHING_CONFIEM:
                if (!this.verifyPassword() || !this.verifyConfirmPassword()) {
                    e.setCancel(true);
                } else if (!this.savePassword()) {
                    e.setCancel(true);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        String formOp = ((FormOperate) e.getSource()).getOperateKey();
        if (DONOTHING_CONFIEM.equals(formOp)) {
            this.showLoginPage();
        }
    }

    /**
     * 校验新密码
     *
     * @return true: 校验通过 false:校验失败
     */
    private boolean verifyPassword() {
        String newPassword = (String) this.getModel().getValue(KEY_NEWPAWD);
        // 检验新密码是否为空
        if (StringUtils.isEmpty(newPassword)) {
            String msg = ResManager.loadKDString("密码不能为空", "PCSalaryPwdVerifyPlugin_0", "swc-hspp-formplugin");
            this.getView().showTipNotification(msg);
            return false;
        }
        return true;
    }

    /**
     * 校验确认密码
     *
     * @return true: 校验通过 false:校验失败
     */
    private boolean verifyConfirmPassword() {
        String newPassword = (String) this.getModel().getValue(KEY_NEWPAWD);
        String confirmPassword = (String) this.getModel().getValue(KEY_CONFIRMPWD);
        boolean result = true;
        if (StringUtils.isEmpty(confirmPassword)) {
            this.getView().showTipNotification(ResManager.loadKDString("密码不能为空", "PCSalaryPwdVerifyPlugin_0", "swc-hspp-formplugin"));
            result = false;
        } else if (!StringUtils.equals(newPassword, confirmPassword)) {
            this.getView().showTipNotification(ResManager.loadKDString("密码不一致", "PCSalaryPwdVerifyPlugin_1", "swc-hspp-formplugin"));
            result = false;
        }
        return result;
    }

    /**
     * 修改密码
     *
     * @return true:修改密码成功 false:修改密码失败
     */
    private boolean savePassword() {
        Long personId = PCSalarySlipHelper.getHrPersonId(this.getPageCache());
        String newPassword = (String) this.getModel().getValue(KEY_NEWPAWD);
        String confirmPassword = (String) this.getModel().getValue(KEY_CONFIRMPWD);

        Map<String, Object> result = PCSalarySlipHelper.verifyPassword(newPassword, confirmPassword, PCSalarySlipHelper.FIELD_VERIFY_TYPE_ALL);
        if (result != null && Boolean.FALSE.equals(result.get("success"))) {
            String msg = (String) result.get("message");
            this.getView().showTipNotification(msg);
            return false;
        }

        result = PCSalarySlipHelper.saveOrUpdatePassword(personId, newPassword, confirmPassword);
        if (result != null && Boolean.FALSE.equals(result.get("success"))) {
            String msg = (String) result.get("message");
            this.getView().showTipNotification(msg);
            return false;
        }
        return true;
    }

    /**
     * 打开登录页面
     */
    protected void showLoginPage() {
        FormShowParameter formShowParameter = new FormShowParameter();
        formShowParameter.setFormId(HSPP_PCPWDLOGIN);
        formShowParameter.setShowTitle(false);
        formShowParameter.getOpenStyle().setShowType(ShowType.Floating);
        formShowParameter.setCloseCallBack(new CloseCallBack(this, "donothing_login"));
        this.getView().showForm(formShowParameter);
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        String ifLogin = null != this.getPageCache().get("actionId") ? this.getPageCache().get("actionId") : "close";
        this.getView().returnDataToParent(ifLogin);

    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String actionId = closedCallBackEvent.getActionId();
        this.getView().returnDataToParent(actionId);
        if (null != actionId && StringUtils.equals("donothing_login", actionId)) {
            if (!"close".equals(closedCallBackEvent.getReturnData().toString())) {
                this.getPageCache().put("actionId", closedCallBackEvent.getReturnData().toString());
            }
            this.getView().close();
        }
    }
}
