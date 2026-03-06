package dgdl.odc.homs.formplugin.Import;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.BasedataItem;
import kd.bos.entity.datamodel.events.QueryImportBasedataEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author: YS
 * @date:2024/5/23 15:09
 * @description: 月度编制计划导入过滤
 */
public class PlanMonthBillBzFormPlugin  extends AbstractFormPlugin {


    /**
     * 导入状态
     */
    private static final String ISIMPORTSTATUS = "isImportStatus";

    private static final Log logger = LogFactory.getLog(PlanMonthBillBzFormPlugin.class);

    /**
     * 当基础资料查不到或者查到多个结果时，设置正确的id
     *
     * @param e
     */
    @Override
    public void queryImportBasedata(QueryImportBasedataEventArgs e) {
        super.queryImportBasedata(e);
        this.getPageCache().put(ISIMPORTSTATUS, "true");
        Map<BasedataItem, List<Object>> searchResult = e.getSearchResult();
        try {
            for (Map.Entry<BasedataItem, List<Object>> entry : searchResult.entrySet()) {
                //导入字段
                BasedataItem basedataItem = entry.getKey();
                //字段标识
                String fieldKey = basedataItem.getFieldKey();
                logger.error("PlanMonthBillBzFormPlugin导入实体", basedataItem.getEntityNumber());
                //导入值
                List<Object> basedata = entry.getValue();
                //基础资料匹配了多个值
                if (basedata.size() > 1) {
                    Iterator it = basedata.iterator();
                    while (it.hasNext()) {
                        Object id = it.next();
                        DynamicObject basedataObj = BusinessDataServiceHelper.loadSingleFromCache(id, basedataItem.getEntityNumber());
                        switch (fieldKey) {
                            //员工工号
                            case "dgdl_bz_adminorg1":
                            case "dgdl_bz_adminorg2":
                            case "dgdl_bz_adminorg3":
                            case "dgdl_bz_adminorg4":
                            case "dgdl_bz_adminorg5":
                            case "dgdl_bz_adminorg6":
                            case "adminorg":
                                if (!basedataObj.getBoolean("iscurrentversion")
                                        || !"1".equals(basedataObj.getString("datastatus"))
                                ) {
                                    it.remove();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("PlanMonthBillBzFormPlugin：", ex);
            throw ex;
        }
    }
}
