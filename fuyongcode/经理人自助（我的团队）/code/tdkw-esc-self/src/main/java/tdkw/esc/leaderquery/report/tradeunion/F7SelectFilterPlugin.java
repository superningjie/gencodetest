package tdkw.esc.leaderquery.report.tradeunion;

import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.plugin.AbstractReportFormPlugin;

import java.util.EventObject;

/**
 * F7SelectFilterPlugin
 *
 * @author xxx
 * @date 2023/7/18
 */
public class F7SelectFilterPlugin extends AbstractReportFormPlugin implements BeforeF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit position = this.getControl("tdkw_dgtj_org");
        position.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        ListShowParameter listShowParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
        QFilter qFilter = new QFilter("group.number", QCP.equals, "XY002");
        QFilter treeFilter = new QFilter("number", QCP.equals, "XY002");
        listShowParameter.getTreeFilterParameter().getQFilters().add(treeFilter);
        listShowParameter.getListFilterParameter().setFilter(qFilter);
    }
}
