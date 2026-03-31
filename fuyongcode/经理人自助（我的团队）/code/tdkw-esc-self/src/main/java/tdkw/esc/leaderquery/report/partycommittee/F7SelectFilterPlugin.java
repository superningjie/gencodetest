package tdkw.esc.leaderquery.report.partycommittee;

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
        //党内职务
        BasedataEdit position = this.getControl("tdkw_partyaffairs");
        //党组织
        BasedataEdit partyGroup = this.getControl("tdkw_basedatafield");
        position.addBeforeF7SelectListener(this);
        partyGroup.addBeforeF7SelectListener(this);
    }

    /**
     * 只展示党
     * @param beforeF7SelectEvent
     */
    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        if ("tdkw_partyaffairs".equals(beforeF7SelectEvent.getProperty().getName())){
            ListShowParameter listShowParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            QFilter qFilter = new QFilter("group.number", QCP.equals, "XY001");
            QFilter treeFilter = new QFilter("number", QCP.equals, "XY001");
            listShowParameter.getTreeFilterParameter().getQFilters().add(treeFilter);
            listShowParameter.getListFilterParameter().setFilter(qFilter);
        }else if ("tdkw_basedatafield".equals(beforeF7SelectEvent.getProperty().getName())){
            ListShowParameter listShowParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            QFilter qFilter = new QFilter("tdkw_basedatafield.number", QCP.equals, "XY001");
            QFilter treeFilter = new QFilter("tdkw_basedatafield.number", QCP.equals, "XY001");
            listShowParameter.getTreeFilterParameter().getQFilters().add(treeFilter);
            listShowParameter.getListFilterParameter().setFilter(qFilter);

        }

    }
}
