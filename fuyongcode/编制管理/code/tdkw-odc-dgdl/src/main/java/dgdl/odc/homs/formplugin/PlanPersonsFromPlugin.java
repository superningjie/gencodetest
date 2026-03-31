package dgdl.odc.homs.formplugin;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.*;


/**
 * @Author: yaoshuai
 * @CreateTime: 2024-01-30 13:45
 * @Description: 在岗人员名单表单插件
 */
public class PlanPersonsFromPlugin extends AbstractBillPlugIn {


    private static Log logger = LogFactory.getLog(PlanPersonsFromPlugin.class);

    private static String[] hideStr = new String[]{"dgdl_detail_adminorg1", "dgdl_detail_adminorg2", "dgdl_detail_adminorg3", "dgdl_detail_adminorg4", "dgdl_detail_adminorg5", "dgdl_detail_adminorg6", "dgdl_laborreltype", "dgdl_check1", "dgdl_check2", "dgdl_check3", "dgdl_check4", "dgdl_check5", "dgdl_workplace", "dgdl_dispatch"};


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("btncancel", "btnok");
    }

    /**
     * 编制详情字段
     */
    private final static String DETAIL_FILED = "id as detailId," +
            "dgdl_entry.id as entryId," +
            "dgdl_entry.dgdl_detail_adminorg1 as admin1," +
            "dgdl_entry.dgdl_detail_adminorg2 as admin2," +
            "dgdl_entry.dgdl_detail_adminorg3 as admin3," +
            "dgdl_entry.dgdl_detail_adminorg4 as admin4," +
            "dgdl_entry.dgdl_detail_adminorg5 as admin5," +
            "dgdl_entry.dgdl_detail_adminorg6 as admin6," +
            "dgdl_entry.dgdl_detail_adminorg as admin," +
            "dgdl_entry.dgdl_laborreltype as type.id," +
            "dgdl_entry.dgdl_stdposition as stdposition.id," +
            "dgdl_entry.dgdl_personrank as personrank.id," +
            "dgdl_entry.dgdl_post_aisle as aisil.id," +
            "dgdl_entry.dgdl_jobproperty as jobproperty.id," +
            "dgdl_entry.dgdl_label1 as label1," +
            "dgdl_entry.dgdl_label2 as label2," +
            "dgdl_entry.dgdl_label3 as label3," +
            "dgdl_entry.dgdl_label4 as label4," +
            "dgdl_entry.dgdl_dispatch as dispatch," +
            "dgdl_entry.dgdl_workplace as workplace.id," +
            "dgdl_entry.dgdl_mulperson.fbasedataid as mulperson.id";

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        String algoKey = this.getClass().getName();
        //当前分录
        IDataModel model = this.getModel();
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        //编制详情
        Long detailId = (Long) customParams.get("detailId");
        //获取分录id
        Long entryId = (Long) customParams.get("entryId");
        logger.info("PlanPersonsFromPlugin详情id=" + detailId + ",分录id=" + entryId);
        DynamicObject planDetail = BusinessDataServiceHelper.loadSingleFromCache(detailId, "dgdl_planyear_detail");
        if (Objects.nonNull(planDetail)) {
            //编制计划
            DynamicObject planYear = planDetail.getDynamicObject("dgdl_planyear");
            //显示隐藏
            this.getView().setVisible(false, hideStr);
            //层级
            String hierarchy = planYear.getString("dgdl_orghierarchy");
            logger.info("PlanPersonFormPlugin获取层级=" + hierarchy);
            //末端组织
            DynamicObject lastAdminorg = planDetail.getDynamicObject("adminorg");
            //标签维度
            String labeldimension = planYear.getString("dgdl_labeldimension");
            logger.info("PlanPersonFormPlugin标签维度=" + labeldimension);
            List<String> hideList;
            //隐藏标识
            String hideCheck = "dgdl_check";
            if (StringUtils.isNotEmpty(labeldimension)) {
                String[] split = labeldimension.split(",");
                hideList = Arrays.asList(split);
                logger.info("PlanPersonFormPlugin可以释放的标签字段=" + hideList);
                for (String str : split) {
                    if ("6".equals(str)) {
                        this.getView().setVisible(true, "dgdl_workplace");
                    } else if ("5".equals(str)) {
                        this.getView().setVisible(true, "dgdl_dispatch");
                    } else {
                        this.getView().setVisible(true, hideCheck + str);
                    }
                }
            }
            //用工关系类型
            String useworktype = planYear.getString("dgdl_useworktype");
            logger.info("PlanPersonFormPlugin用工关系类型=" + useworktype);
            if (StringUtils.isNotEmpty(useworktype)) {
                this.getView().setVisible(true, "dgdl_laborreltype");
            }
            QFilter detailQFilter = new QFilter("id", QCP.equals, detailId);
            if (Objects.nonNull(entryId)) {
                detailQFilter.and("dgdl_entry.id", QCP.equals, entryId);
            } else {
                //排除其他组织下的人员
                detailQFilter.and("dgdl_entry.dgdl_detail_adminorg", QCP.equals, lastAdminorg.getPkValue());
            }
            logger.info("PlanPersonFormPlugin获取详情查询sql=" + detailQFilter);
            DataSet queryDataSet = QueryServiceHelper.queryDataSet(algoKey, "dgdl_planyear_detail", DETAIL_FILED, detailQFilter.toArray(), "");
            int dataSetCount = queryDataSet.copy().count("detailId", false);
            logger.info("PlanPersonFormPlugin获取详情总条数=" + dataSetCount);

            //在岗人员分录信息
            DynamicObjectCollection entryEntity = model.getEntryEntity("dgdl_entry");
            for (int i = 0; i < dataSetCount; i++) {
                entryEntity.addNew();
            }
            int index = 0;
            while (queryDataSet.hasNext()) {
                Row rowData = queryDataSet.next();
                //人员
                Long personId = rowData.getLong("mulperson.id");
                model.setValue("dgdl_person", personId, index);
                logger.info("PlanPersonFormPlugin人员id=" + personId);
                //组织层级
                model.setValue("dgdl_detail_adminorg1", rowData.getLong("admin1"), index);
                model.setValue("dgdl_detail_adminorg2", rowData.getLong("admin2"), index);
                model.setValue("dgdl_detail_adminorg3", rowData.getLong("admin3"), index);
                model.setValue("dgdl_detail_adminorg4", rowData.getLong("admin4"), index);
                model.setValue("dgdl_detail_adminorg5", rowData.getLong("admin5"), index);
                model.setValue("dgdl_detail_adminorg6", rowData.getLong("admin6"), index);
                //隐藏标识
                String iden = "admin";
                for (int i = 1; i <= Integer.parseInt(hierarchy); i++) {
                    Long adminId = rowData.getLong(iden + i);
                    logger.info("PlanPersonFormPlugin层级隐藏字段=" + iden + i + ",具体信息" + adminId);
                    if (0 != adminId) {
                        this.getView().setVisible(true, "dgdl_detail_adminorg" + i);
                    }
                }
                logger.info("PlanPersonFormPlugin用工关系类型id=" + rowData.getLong("type.id") + "标准岗位id=" + rowData.getLong("stdposition.id"));
                //用工关系类型
                model.setValue("dgdl_laborreltype", rowData.getLong("type.id"), index);
                //标准岗位
                model.setValue("dgdl_stdposition", rowData.getLong("stdposition.id"), index);
                //个人职级
                model.setValue("dgdl_personrank", rowData.getLong("personrank.id"), index);
                //岗位通道
                model.setValue("dgdl_post_aisle", rowData.getLong("aisil.id"), index);
                //岗位属性
                model.setValue("dgdl_jobproperty", rowData.getLong("jobproperty.id"), index);
                logger.info("PlanPersonFormPlugin岗位通道=" + rowData.getLong("aisil.id") + "岗位属性=" + rowData.getLong("jobproperty.id"));
                //大雁
                model.setValue("dgdl_check1", rowData.getString("label1"), index);
                //新四化
                model.setValue("dgdl_check2", rowData.getString("label2"), index);
                //国际化
                model.setValue("dgdl_check3", rowData.getString("label3"), index);
                //数字化
                model.setValue("dgdl_check4", rowData.getString("label4"), index);
                //对外派驻
                model.setValue("dgdl_dispatch", rowData.getString("dispatch"), index);
                //工作地
                model.setValue("dgdl_workplace", rowData.getLong("workplace.id"), index);
                index++;
            }
            this.getView().updateView("dgdl_entry");
        }
    }

}
