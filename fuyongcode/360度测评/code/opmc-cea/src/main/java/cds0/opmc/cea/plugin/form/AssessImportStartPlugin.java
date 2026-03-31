package cds0.opmc.cea.plugin.form;

import kd.bos.form.IFormView;
import kd.bos.form.events.BeforeClosedEvent;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

public class AssessImportStartPlugin extends HRDataBaseEdit {
    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        IFormView parentView = this.getView().getParentView();
        if (parentView != null) {
            parentView.close();
        }
    }
}
