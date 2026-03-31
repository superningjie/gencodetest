package tdkw.esc.leaderquery.task;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import tdkw.esc.leaderquery.common.PinyinUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * @author xxx
 * @version 1.0
 * @description: 给人员非时序添加拼音全称
 * @date 2024/1/2 0002 上午 11:26
 */

public class PernontsprTask extends AbstractTask {

    private static final Log logger = LogFactory.getLog(PernontsprTask.class);

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        QFilter qFilter = new QFilter("iscurrentversion", QCP.equals, "1");
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.or("tdkw_full_pinyin_name", QCP.is_null, null);
        qFilter.or("tdkw_full_pinyin_name", QCP.equals, "");
        qFilter.or("tdkw_full_pinyin_name", QCP.equals, " ");
        DynamicObject[] personProps = BusinessDataServiceHelper.load("hrpi_pernontsprop", "person.name,tdkw_full_pinyin_name", new QFilter[]{qFilter});
        if (personProps.length > 0) {
            logger.info("进入拼音全称调度，一共有：" + personProps.length + "条数据");
            Arrays.stream(personProps).forEach(entry -> {
                // try catch 捕获异常如果异常则先跳过这个，继续往下执行
                String name4Pinyin;
                try {
                    String name = entry.getString("person.name");
                    name4Pinyin = PinyinUtil.getPingYin(name);
                } catch (Exception e) {
                    logger.error("拼音全称调度异常：" + e.getMessage());
                    logger.error("拼音全称调度异常，跳过：" + entry.getString("person.name"));
                    return;
                }
                entry.set("tdkw_full_pinyin_name", name4Pinyin);
                SaveServiceHelper.save(new DynamicObject[]{entry}, OperateOption.create());
            });
        }
    }
}
