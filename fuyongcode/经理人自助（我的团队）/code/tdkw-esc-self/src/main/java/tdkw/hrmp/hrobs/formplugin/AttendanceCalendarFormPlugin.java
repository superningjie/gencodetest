package tdkw.hrmp.hrobs.formplugin;

import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * @Metadata： tdkw_attendancecalendar  考勤日历
 * @Description ： HR自助门户-考勤日历卡片
 * @ClassName ：AttendanceCalendarFormPlugin
 * @author xxx
 * @Date ：2023/7/8 15:04
 * @Version: 1.0
 */
public class AttendanceCalendarFormPlugin extends AbstractFormPlugin {
    public GateWayUtils gateWayUtils = new GateWayUtils();

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_clickl", "tdkw_clicki");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        FormShowParameter param = new FormShowParameter();
        param.setFormId("tdkw_wtss_pcpersonhom_inh");
//        param.setFormId("wtss_pcpersonhome");
        param.getOpenStyle().setShowType(ShowType.InContainer);
        param.getOpenStyle().setTargetKey("tdkw_flexpanelap");
        param.setCustomParam("target", true);
        this.getView().showForm(param);
    }

    public void click(EventObject evt) {
        super.click(evt);
        IFormView view = this.getView();
        String key = gateWayUtils.getClickKey(evt);
        if (StringUtils.equals("tdkw_clickl", key) || StringUtils.equals("tdkw_clicki", key)) {
            FormShowParameter param = new FormShowParameter();
            param.setFormId("wtss_pcpersonhome");
            param.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            param.getOpenStyle().setTargetKey("tdkw_flexpanelap3");
            param.setCustomParam("target", false);
            this.getView().showForm(param);
        }
    }

}
