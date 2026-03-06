package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.ext.form.control.CountDown;
import kd.bos.ext.form.control.events.CountDownEvent;
import kd.bos.ext.form.control.events.CountDownListener;
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

import java.util.*;

public class PCSalaryPwdForgetPlugin extends AbstractFormPlugin {
    /**
     * 发送消息操作代码
     */
    private static final String DONOTHING_SENDMSG = "donothing_sendmsg";
    /**
     * 确认操作代码
     */
    private static final String DONOTHING_CONFIEM = "donothing_confirm";
    /**
     * 手机号标识
     */
    private static final String KEY_PHONE = "tdkw_phone";
    /**
     * 验证码标识
     */
    private static final String KEY_CODE = "tdkw_code";
    /**
     * 计时器标识
     */
    private static final String KEY_COUNTDOWNAP = "tdkw_countdownap";
    /**
     * 等待倒计时面板
     */
    private static final String KEY_WAIT_PANELAP = "tdkw_waitpanelap";
    /**
     * 待获取验证码面板
     */
    private static final String KEY_GET_CODE_PANELAP = "tdkw_getcodepanelap";
    /**
     * 重密码重置页面
     */
    private static final String HSPP_PCPWDRESET = "tdkw_hrobs_pcpwdreset";

    /**
     * 对象锁
     */
    private static final Object MESSAGE_LOCKER = new Object();

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addCountDownListener();
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.setCountDownPanelap(false);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs e) {
        super.beforeDoOperation(e);
        String formOp = ((FormOperate) e.getSource()).getOperateKey();
        switch (formOp) {
            case DONOTHING_SENDMSG:
                if (!isValidPhone()) {
                    e.setCancel(true);
                }
                break;
            case DONOTHING_CONFIEM:
                this.validPhoneCode(e);
                break;
            default:
                break;
        }


    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        String formOp = ((FormOperate) e.getSource()).getOperateKey();
        if (DONOTHING_SENDMSG.equals(formOp)) {
            this.sendMessage();
        }
    }

    /**
     * 发送短信
     */
    private void sendMessage() {
        Long personId = PCSalarySlipHelper.getHrPersonId(this.getPageCache());
        String phone = (String) this.getModel().getValue(KEY_PHONE);
        synchronized (MESSAGE_LOCKER) {
            Map<String, Object> result = PCSalarySlipHelper.sendCodeMessage(personId, phone, null);
            if (result == null) {
                return;
            }

            if (Boolean.TRUE.equals(result.get("success"))) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null) {
                    Integer duration = (Integer) data.getOrDefault("duration", 60);
                    String msg = (String) data.get("msg");
                    this.startCountDown(duration);
                    this.getView().showSuccessNotification(msg);
                    this.setCountDownPanelap(true);
                }
            } else {
                String msg = (String) result.get("message");
                this.getView().showErrorNotification(msg);
            }
        }
    }

    /**
     * 开始倒计时
     *
     * @param duration
     */
    private void startCountDown(Integer duration) {
        CountDown countDown = this.getControl(KEY_COUNTDOWNAP);
        countDown.setDuration(duration);
        this.getView().updateView(KEY_COUNTDOWNAP);
    }

    /**
     * 添加倒计时监听器
     */
    private void addCountDownListener() {
        CountDown phoneNewCountDown = this.getControl(KEY_COUNTDOWNAP);
        phoneNewCountDown.addCountDownListener(new CountDownListener() {
            public void onCountDownEnd(CountDownEvent evt) {
                PCSalaryPwdForgetPlugin.this.setCountDownPanelap(false);
            }
        });
    }

    /**
     * 1、校验验证码是否过期 2、匹配验证码
     *
     * @param e
     */
    private void validPhoneCode(BeforeDoOperationEventArgs e) {

        String code = this.getModel().getDataEntity().getString(KEY_CODE);
        if (StringUtils.isEmpty(code)) {
            this.getView().showErrorNotification(ResManager.loadKDString("验证码不能为空", "PCSalaryPwdForgetPlugin_0", "swc-hspp-formplugin"));
            e.setCancel(true);
            return;
        }

        Long personId = PCSalarySlipHelper.getHrPersonId(this.getPageCache());
        Map<String, Object> result = PCSalarySlipHelper.validPhoneCode(personId, code);
        if (result != null) {
            if (Boolean.TRUE.equals(result.get("success"))) {
                this.showResetPage();
            } else if (result.get("message") != null) {
                this.getView().showErrorNotification((String) result.get("message"));
            }
        }
    }

    /**
     * 校验手机号
     *
     * @return true:校验通过 false:校验失败
     */
    private boolean isValidPhone() {
        String phone = (String) this.getModel().getValue(KEY_PHONE);
        if (StringUtils.isEmpty(phone)) {
            this.getView().showTipNotification(ResManager.loadKDString("手机号码不能为空", "PCSalaryPwdForgetPlugin_1", "swc-hspp-formplugin"));
            return false;
        }
        return true;
    }

    /**
     * 设置倒计时模块的展示与隐藏
     *
     * @param isWaitting 是否等待中
     */
    private void setCountDownPanelap(boolean isWaitting) {
        this.getView().setVisible(isWaitting, KEY_WAIT_PANELAP);
        this.getView().setVisible(!isWaitting, KEY_GET_CODE_PANELAP);
    }

    /**
     * 打开重设密码页面
     */
    private void showResetPage() {
        FormShowParameter formShowParameter = new FormShowParameter();
        formShowParameter.setFormId(HSPP_PCPWDRESET);
        formShowParameter.getOpenStyle().setShowType(ShowType.Floating);
        formShowParameter.setCloseCallBack(new CloseCallBack(this, "donothing_login"));
        formShowParameter.setShowTitle(false);
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
