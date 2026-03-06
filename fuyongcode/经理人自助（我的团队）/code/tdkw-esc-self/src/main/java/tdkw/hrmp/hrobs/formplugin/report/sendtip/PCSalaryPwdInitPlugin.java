package tdkw.hrmp.hrobs.formplugin.report.sendtip;


import tdkw.hrmp.hrobs.formplugin.salary.PCSalarySlipHelper;

import java.util.*;

public class PCSalaryPwdInitPlugin extends PCSalaryPwdVerifyPlugin {


    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        //查询中台人员
        Long personId = PCSalarySlipHelper.getHrPersonId(this.getPageCache());
        Map<String, Object> result = PCSalarySlipHelper.checkHasPassword(personId);
        if (result != null && result.get("data") != null && Boolean.TRUE.equals(((Map) result.get("data")).get("exist"))) {
            //已设置密码则直接跳转登录页
            this.showLoginPage();
        }
    }
}


