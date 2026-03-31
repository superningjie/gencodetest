package dgdl.odc.homs.opplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.hr.hbp.business.domain.service.impl.newhismodel.HisNonLineTimeService;
import kd.hr.hbp.business.domain.service.newhismodel.IHisNonLineTimeService;

public class PlanyearDeleteOp extends AbstractOperationServicePlugIn {
    private IHisNonLineTimeService hisNonLineTimeService = HisNonLineTimeService.getInstance();

    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        super.beforeExecuteOperationTransaction(e);
        for (DynamicObject dataEntity : e.getDataEntities()) {;
            DeleteServiceHelper.delete("dgdl_planyear_detail",new QFilter[]{new QFilter("dgdl_planyear.id", QCP.equals,dataEntity.getLong("id"))});
            DeleteServiceHelper.delete("dgdl_planyear_bill",new QFilter[]{new QFilter("dgdl_planyear.id", QCP.equals,dataEntity.getLong("id"))});
//            this.hisNonLineTimeService.handleDeleteHisData(dataEntities);

        }
    }

    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        super.afterExecuteOperationTransaction(e);
        for (DynamicObject dataEntity : e.getDataEntities()) {;
            DeleteServiceHelper.delete("dgdl_planyear",new QFilter[]{new QFilter("boid", QCP.equals,dataEntity.getLong("id"))});
        }
    }

}
