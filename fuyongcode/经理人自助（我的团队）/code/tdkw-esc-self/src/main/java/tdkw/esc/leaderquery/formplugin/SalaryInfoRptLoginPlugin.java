package tdkw.esc.leaderquery.formplugin;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.context.RequestContext;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.report.ReportShowParameter;

import java.util.EventObject;

public class SalaryInfoRptLoginPlugin extends AbstractFormPlugin {

    /**
     * 薪酬信息
     */
    private static final String Leader_SalaryInfo = "tdkw_salary_info_report";
    private static final String HSPP_PCPWDLOGIN = "tdkw_hspp_pcpwdlogin";
    DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("salaryRptLoginLoss");
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        String userLogin = RequestContext.get().getGlobalSessionId();
        String key = "salaryInfoLogin" +userLogin;
        String formId = HSPP_PCPWDLOGIN;
        if (cache.get(key) != null && cache.get(key).equals("entry")){
            formId = Leader_SalaryInfo;
            ReportShowParameter showParameter = new ReportShowParameter();
            showParameter.setFormId(formId);
            showParameter.getOpenStyle().setShowType(ShowType.InCurrentForm);//打开方式
            showParameter.setCaption("薪酬信息");
            showParameter.setCustomParam("opentype","salaryrpt");
            this.getView().showForm(showParameter);
        }else {
            FormShowParameter showParameter = new FormShowParameter();
            showParameter.setFormId(formId);
            showParameter.getOpenStyle().setShowType(ShowType.InCurrentForm);//打开方式
            showParameter.setCaption("薪酬信息");
            showParameter.setCustomParam("opentype","salaryrpt");
            this.getView().showForm(showParameter);
        }

    }
}
