package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Label;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import tdkw.hrmp.hrobs.formplugin.salary.PCSalarySlipHelper;

import java.util.EventObject;
import java.util.Map;


public class PCSalaryPwdLoginPlugin extends AbstractFormPlugin {
    DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("loginLossTime");

    /**
     * 登录操作代码
     */
    private static final String DONOTHING_LOGIN = "donothing_login";
    /**
     * 忘记密码操作代码
     */
    private static final String DONOTHING_FORGETPAWD = "donothing_forgetpwd";
    /**
     * 名称标签标识
     */
    private static final String KEY_NAME_LABLE = "tdkw_namelabel";
    /**
     * 密码标识
     */
    private static final String KEY_PWD = "tdkw_pwd";
    /**
     * 忘记密码页面
     */
    private static final String HSPP_PCPWDFORGET = "tdkw_hrobs_pcpwdforget";


    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String name = RequestContext.get().getUserName();
        Label label = this.getView().getControl(KEY_NAME_LABLE);
        label.setText(name);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs e) {
        super.beforeDoOperation(e);
        String formOp = ((FormOperate) e.getSource()).getOperateKey();
        //todo 密码校验
        if (DONOTHING_LOGIN.equals(formOp)) {
            this.validPassWord(e);
        }
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        String ifLogin = null != this.getPageCache().get("actionId") ? this.getPageCache().get("actionId") : "close";
        this.getView().returnDataToParent(ifLogin);
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        String formOp = ((FormOperate) e.getSource()).getOperateKey();
        switch (formOp) {
            case DONOTHING_LOGIN:
                String ifLogin = "okok";
                this.getPageCache().put("actionId", ifLogin);
//                Map<String, String> customParameter = SystemParamServiceHelper.loadCustomParameterFromCache(new CustomParam());
//                String loginTime = null != (String) customParameter.get("LOGIN_LOSS_TIME") ? (String) customParameter.get("LOGIN_LOSS_TIME") : "3";
//                cache.put(UserServiceHelper.getCurrentUserId() + "loginUser",
//                        String.valueOf(UserServiceHelper.getCurrentUserId()), Integer.valueOf(loginTime) * 60);
//                //设置默认展开
//                cache.put(UserServiceHelper.getCurrentUserId() + "ifFlag",
//                        String.valueOf(false),Integer.valueOf(loginTime) * 60);
                this.getView().close();

                break;
            case DONOTHING_FORGETPAWD:
                //跳转至忘记密码页面
                this.showForgetPage();
                break;
            default:
                break;
        }
    }


    /**
     * 校验密码
     *
     * @param e
     */
    private void validPassWord(BeforeDoOperationEventArgs e) {
        Long personId = PCSalarySlipHelper.getHrPersonId(this.getPageCache());
        String loginPwq = (String) this.getModel().getValue(KEY_PWD);
        Map<String, Object> result = PCSalarySlipHelper.authenticatePassword(personId, loginPwq);
        if (result == null) {
            e.setCancel(true);
        } else if (!Boolean.TRUE.equals(result.get("success"))) {
            this.getView().showTipNotification((String) result.get("message"));
            e.setCancel(true);
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String actionId = closedCallBackEvent.getActionId();
        this.getView().returnDataToParent(actionId);
        if (null != actionId && StringUtils.equals("donothing_login", actionId)) {
            if (null != actionId && !"close".equals(closedCallBackEvent.getReturnData().toString())) {
                this.getPageCache().put("actionId", closedCallBackEvent.getReturnData().toString());
            }
            this.getView().close();
        }
    }

    /**
     * 打开忘记密码页面
     */
    private void showForgetPage() {
        FormShowParameter formShowParameter = new FormShowParameter();
        formShowParameter.setFormId(HSPP_PCPWDFORGET);
        formShowParameter.setShowTitle(false);
        formShowParameter.getOpenStyle().setShowType(ShowType.Floating);
        formShowParameter.setCloseCallBack(new CloseCallBack(this, "donothing_login"));
        this.getView().showForm(formShowParameter);
    }
}

