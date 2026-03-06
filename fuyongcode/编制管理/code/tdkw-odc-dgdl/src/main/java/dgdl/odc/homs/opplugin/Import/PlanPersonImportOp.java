package dgdl.odc.homs.opplugin.Import;

import dgdl.odc.homs.formplugin.PlanPersonFormPlugin;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.*;


/**
 * 编制计划催办配置导入赋值
 *
 * @version 1.0
 * @author: yaoshuai
 * @date:2023/12/5
 */
public class PlanPersonImportOp extends AbstractOperationServicePlugIn {


    private static Log logger = LogFactory.getLog(PlanPersonFormPlugin.class);

    /**
     * 任职经历
     */
    private final static String HRPI_EMPPOSORGREL = "hrpi_empposorgrel";

    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        logger.info("PlanPersonImportOp开始执行PlanPersonImportOp");
        super.beginOperationTransaction(e);
        DynamicObject[] dataEntities = e.getDataEntities();
        for (DynamicObject data : dataEntities) {
            DynamicObject personplan = data.getDynamicObject("dgdl_personplan");
            if (Objects.nonNull(personplan)) {
                //人员工号
                String personNumber = personplan.getDynamicObject("person").getString("number");
                //获取基础资料信
                QFilter empFiler = new QFilter("iscurrentversion", QCP.equals, "1").and("datastatus", QCP.equals, "1").and("businessstatus", QCP.equals, "1").and("isprimary", QCP.equals, "1").and("person.number", QCP.equals, personNumber);
                logger.info("PlanPersonFormPlugin获取任职经历信息sql=" + empFiler);
                //任职经历信息
                DynamicObject emoObj = BusinessDataServiceHelper.loadSingle(HRPI_EMPPOSORGREL, "id,adminorg", empFiler.toArray());
                if (Objects.nonNull(emoObj)) {
                    //获取行政组织
                    DynamicObject adminorg = emoObj.getDynamicObject("adminorg");
                    data.set("dgdl_adminorg", adminorg);
                    //组织
                    if (Objects.nonNull(adminorg)) {
                        DynamicObject org = adminorg.getDynamicObject("org");
                        data.set("dgdl_org", org);
                    }
                }
            }
        }
    }
}
