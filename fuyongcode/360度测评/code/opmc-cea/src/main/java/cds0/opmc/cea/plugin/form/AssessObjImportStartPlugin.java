package cds0.opmc.cea.plugin.form;

import kd.bos.form.IFormView;
import kd.bos.form.control.Control;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import java.util.EventObject;

public class AssessObjImportStartPlugin extends HRDataBaseEdit {
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("btndownimporttpl");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        if ("btndownimporttpl".equals(key)) {
            IFormView parentView = this.getView().getParentView();
            if (parentView != null) {
                parentView.invokeOperation("export_from_impttpl_hr");
                this.getView().sendFormAction(parentView);
            }
        }
    }
}
