package tdkw.hrmp.hrobs.common.business;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.earlywarn.EarlyWarnContext;
import kd.bos.entity.earlywarn.warn.plugin.IEarlyWarnDataSource;
import kd.bos.entity.filter.FilterCondition;
import kd.bos.entity.tree.TreeNode;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.List;
import java.util.Map;

public class SalaryResumeDataSource implements IEarlyWarnDataSource {

    private final static Log logger = LogFactory.getLog(SalaryResumeDataSource.class);

    @Override
    public List<QFilter> buildFilter(String s, FilterCondition filterCondition, EarlyWarnContext earlyWarnContext) {
        return null;
    }

    @Override
    public DynamicObjectCollection getData(String s, List<QFilter> list, EarlyWarnContext earlyWarnContext) {
        QFilter qFilter = new QFilter("tdkw_change_confirm", QCP.equals, "3");
        DynamicObject[] load = BusinessDataServiceHelper.load(s, "id,tdkw_change_user", qFilter.toArray());
        DynamicObjectCollection dynamicObjects = new DynamicObjectCollection();
        logger.info("取出" + load.length + "条数据");
        if (load.length > 0) {
            dynamicObjects.add(load[0]);
            return dynamicObjects;
        }
        logger.info("传递" + load.length + "条数据");
        return dynamicObjects;
    }

    @Override
    public List<Map<String, Object>> getCommonFilterColumns(String s) {
        return null;
    }

    @Override
    public TreeNode getSingleMessageFieldTree(String s) {
        return null;
    }

    @Override
    public TreeNode getMergeMessageFieldTree(String s) {
        return null;
    }
}
