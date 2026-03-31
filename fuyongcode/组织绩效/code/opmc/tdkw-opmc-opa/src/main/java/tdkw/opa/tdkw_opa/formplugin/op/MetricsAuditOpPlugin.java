package tdkw.opa.tdkw_opa.formplugin.op;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import tdkw.opa.tdkw_opa.formplugin.enums.BaseConstant;

import java.util.Date;

public class MetricsAuditOpPlugin extends AbstractOperationServicePlugIn {

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        e.getFieldKeys().add("tdkw_perf_metricsid");
    }

    /**
     * 单据数据已经提交到数据库之后，事务未提交之前，触发此事件；
     *
     * @remark 可以在此事件，进行数据同步处理；
     */
    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        DynamicObject[] dataEntities = e.getDataEntities();

        for (DynamicObject dataEntity : dataEntities) {
            Date auditdate = dataEntity.getDate("auditdate");
            long auditor = dataEntity.getLong("auditor.id");
            QFilter qFilter = new QFilter("tdkw_audit_billid", QCP.equals, dataEntity.getPkValue());
            DynamicObject[] orgPerfMetrics = BusinessDataServiceHelper.load("tdkw_org_perf_metrics", "id,billstatus,auditdate,auditor", qFilter.toArray());
            for (DynamicObject orgMetric : orgPerfMetrics) {
                switch (e.getOperationKey()) {
                    case "audit":
                        orgMetric.set(BaseConstant.BILL_STATUS, "C");
                        orgMetric.set("auditdate", auditdate);
                        orgMetric.set("auditor", auditor);
                        break;
                    case "statusconvert":
                        orgMetric.set(BaseConstant.BILL_STATUS, "A");
                        break;
                }
            }
            // 保存回写后的数据
            SaveServiceHelper.save(orgPerfMetrics);

        }
    }
}
