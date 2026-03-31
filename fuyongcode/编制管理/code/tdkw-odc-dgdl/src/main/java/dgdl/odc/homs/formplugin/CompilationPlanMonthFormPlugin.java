package dgdl.odc.homs.formplugin;

import dgdl.odc.homs.opplugin.CompilationPlanMonthSaveOp;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

import java.util.EventObject;

/**
 * @author lzf
 * @date 2023/9/7 15:49
 * @description
 */
public class CompilationPlanMonthFormPlugin extends AbstractBillPlugIn {
    private static Log logger = LogFactory.getLog(CompilationPlanMonthFormPlugin.class);

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        IDataModel model = this.getModel();

    }

}
