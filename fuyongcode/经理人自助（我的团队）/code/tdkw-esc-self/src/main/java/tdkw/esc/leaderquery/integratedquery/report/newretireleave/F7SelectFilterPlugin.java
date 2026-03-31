package tdkw.esc.leaderquery.integratedquery.report.newretireleave;

import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * F7SelectFilterPlugin
 *
 * @author xxx
 * @date 2023/9/7
 */
public class F7SelectFilterPlugin extends AbstractReportFormPlugin implements BeforeF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //异动类型
       /* BasedataEdit changeType = this.getControl("tdkw_changetype");
        //异动原因
        BasedataEdit changeReason = this.getControl("tdkw_reason");
        changeType.addBeforeF7SelectListener(this);
        changeReason.addBeforeF7SelectListener(this);*/

    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        String name = beforeF7SelectEvent.getProperty().getName();
        if (StringUtils.equals(name, "tdkw_changetype")) {
            ListShowParameter formShowParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            QFilter qFilter = new QFilter("tdkw_chgevent.number", QCP.equals, "1070_S");
            //formShowParameter.getTreeFilterParameter().getQFilters().add(qFilter);
            formShowParameter.getListFilterParameter().setFilter(qFilter);
        }else if (StringUtils.equals(name, "tdkw_reason")){
            ListShowParameter formShowParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            QFilter qFilter = new QFilter("tdkw_chgevent.number", QCP.equals, "1070_S");
            //formShowParameter.getTreeFilterParameter().getQFilters().add(qFilter);
            formShowParameter.getListFilterParameter().setFilter(qFilter);
        }
    }
}
