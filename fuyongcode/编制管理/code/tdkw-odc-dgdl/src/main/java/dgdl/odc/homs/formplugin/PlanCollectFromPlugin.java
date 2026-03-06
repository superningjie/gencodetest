package dgdl.odc.homs.formplugin;

import dgdl.odc.homs.common.DateTimeCommon;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.BeforeImportEntryEventArgs;
import kd.bos.form.ClientProperties;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 刘治凡
 * @CreateTime: 2023/1/31 14:55
 * @Description: 年度编制计划汇总 表单插件
 */
public class PlanCollectFromPlugin extends AbstractFormPlugin {


    private static Log logger = LogFactory.getLog(PlanCollectFromPlugin.class);

    private static String[] monthFieldArr = new String[]{"dgdl_preavg10", "dgdl_preavg11", "dgdl_preavg12", "dgdl_avg1"
            , "dgdl_avg2", "dgdl_avg3", "dgdl_avg4", "dgdl_avg5", "dgdl_avg6", "dgdl_avg7", "dgdl_avg8", "dgdl_avg9", "dgdl_avg10", "dgdl_avg11", "dgdl_avg12"};


    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //隐藏明细页签分录月份字段
        this.getView().setVisible(false, monthFieldArr);
        Map<String, Object> map = this.getView().getFormShowParameter().getCustomParams();
        if (map.containsKey("detailId")) {
            //获取年度编制计划详情id
            Long detailId = (Long) map.get("detailId");
            logger.info("年度编制计划详情id" + detailId);

            //获取汇总类型
            if (map.containsKey("type")) {
                String type = (String) map.get("type");
                logger.info("获取汇总类型" + type);
                switch (type) {
                    case "dgdl_flexjoblevel":
                        //岗级汇总表
                        this.setJobLevelDate(detailId);
                        break;
                    case "dgdl_flexjobtag":
                        //岗位标签汇总表
                        this.setJobTagDate(detailId);
                        break;
                    case "dgdl_flexproperty":
                        //岗位属性汇总表
                        this.setPropertyDate(detailId);
                        break;
                    case "dgdl_flexlaborreltype":
                        //一线非一线+用工关系类型汇总表
                        this.setLaborreltypeDate(detailId);
                        break;
                    default:
                        break;
                }
            }

        }
        if (map.containsKey("planId")) {
            //获取年度编制计划id
            Long planId = (Long) map.get("planId");
            logger.info("年度编制计划id" + planId);
            //获取年度编制计划
            DynamicObject object = BusinessDataServiceHelper.loadSingle(planId, "dgdl_planyear");
            if (object == null) {
                return;
            }
            //所属年度
            Date dgdlYear = object.getDate("dgdl_year");
            int year = DateTimeCommon.getYear(dgdlYear);
            //分录字段重命名
            //开始月份
            Date dgdlStartmonth = object.getDate("dgdl_startmonth");
            //结束月份
            Date dgdlEndmonth = object.getDate("dgdl_endmonth");
            long monthCount = DateTimeCommon.reduceMon(dgdlStartmonth, dgdlEndmonth);
            EntryGrid entryGrid = this.getControl("dgdl_entryentity");

            for (long i = 0; i < monthCount + 1; i++) {
                int startYear = DateTimeCommon.getYear(dgdlStartmonth);
                String dateMonthStr = DateTimeCommon.getDateMonthStr(dgdlStartmonth);
                int month = DateTimeCommon.getMonth(dgdlStartmonth);
                //月份字段名
                String fieldKey2 = "dgdl_avg" + month;
                if (startYear != year) {
                    //取上一年月份字段名
                    fieldKey2 = "dgdl_preavg" + month;
                }
                entryGrid.setColumnProperty(fieldKey2, ClientProperties.Header, new LocaleString(dateMonthStr));
                this.getView().setVisible(true, fieldKey2);
                dgdlStartmonth = DateTimeCommon.addMonth(dgdlStartmonth, 1);
            }
        }
    }

    //一线非一线+用工关系类型汇总表
    private void setLaborreltypeDate(Long detailId) {
        logger.info("一线非一线+用工关系类型汇总表");
        //获取单据体控件
        EntryGrid entryGrid = this.getControl("dgdl_entryentity");
        entryGrid.setColumnProperty("dgdl_textfield", ClientProperties.Header, new LocaleString("按岗位属性+用工关系统计"));
        DynamicObjectCollection dgdlEntryentity = (DynamicObjectCollection) this.getModel().getValue("dgdl_entryentity");
        //查询编制详情
        DynamicObject object = BusinessDataServiceHelper.loadSingle(detailId, "dgdl_planyear_detail");
        //获取明细数据
        DynamicObjectCollection dgdlEntry = object.getDynamicObjectCollection("dgdl_entry");
        int index = 0;
        if (dgdlEntry != null && !dgdlEntry.isEmpty()) {
            List<DynamicObject> objects = new ArrayList<>(dgdlEntry);
            //根据用工关系分组
            Map<String, List<DynamicObject>> collect = objects.stream()
                    .filter(item -> Objects.nonNull(item.getDynamicObject("dgdl_laborreltype")))
                    .collect(Collectors.groupingBy(item -> item.getString("dgdl_laborreltype.name")));
            IDataModel model = this.getModel();
            for (String key : collect.keySet()) {
                List<DynamicObject> dynamicObjects = collect.get(key);
                if (dynamicObjects != null && !dynamicObjects.isEmpty()) {
                    //筛选一线非一线直产,直辅
                    List<DynamicObject> objectList = dynamicObjects.stream()
                            .filter(s -> "直辅".equals(s.getString("dgdl_jobproperty.name")) || "直产".equals(s.getString("dgdl_jobproperty.name"))).collect(Collectors.toList());
                    if (!objectList.isEmpty()) {
                        dgdlEntryentity.addNew();
                        //分录赋值
                        this.setCollectDate(objectList, "一线" + "(" + key + ")", index);
                        index++;
                    }
                    //筛选非一线
                    List<DynamicObject> list = dynamicObjects.stream()
                            .filter(s -> "管理".equals(s.getString("dgdl_jobproperty.name"))
                                    || "制造".equals(s.getString("dgdl_jobproperty.name"))
                                    || "研发".equals(s.getString("dgdl_jobproperty.name"))
                                    || "销售".equals(s.getString("dgdl_jobproperty.name"))
                                    || "基建".equals(s.getString("dgdl_jobproperty.name"))
                            ).collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        dgdlEntryentity.addNew();
                        //分录赋值
                        this.setCollectDate(list, "非一线" + "(" + key + ")", index);
                        index++;
                    }
                }
            }
            //空值单独处理
            List<DynamicObject> nullList = objects.stream().filter(item -> Objects.isNull(item.getDynamicObject("dgdl_laborreltype"))).collect(Collectors.toList());
            if (!nullList.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(nullList, "空值", index);
                index++;
            }
            //合计分录
            setSumEntry(index);
            this.getView().updateView("dgdl_entryentity");
        }
    }

    //岗位属性汇总表
    private void setPropertyDate(Long detailId) {
        logger.info("岗位属性汇总表");
        //获取单据体控件
        EntryGrid entryGrid = this.getControl("dgdl_entryentity");
        entryGrid.setColumnProperty("dgdl_textfield", ClientProperties.Header, new LocaleString("岗位属性"));
        DynamicObjectCollection dgdlEntryentity = (DynamicObjectCollection) this.getModel().getValue("dgdl_entryentity");
        //查询编制详情
        DynamicObject object = BusinessDataServiceHelper.loadSingle(detailId, "dgdl_planyear_detail");
        //获取明细数据
        DynamicObjectCollection dgdlEntry = object.getDynamicObjectCollection("dgdl_entry");
        if (dgdlEntry != null && !dgdlEntry.isEmpty()) {
            int index = 0;
            IDataModel model = this.getModel();
            List<DynamicObject> objects = new ArrayList<>(dgdlEntry);
            //根据岗位属性分组
            Map<String, List<DynamicObject>> collect = objects.stream()
                    .filter(item -> Objects.nonNull(item.getDynamicObject("dgdl_jobproperty")))
                    .collect(Collectors.groupingBy(item -> item.getString("dgdl_jobproperty.name")));
            for (String key : collect.keySet()) {
                //获取分组数据
                List<DynamicObject> dynamicObjects = collect.get(key);
                if (dynamicObjects != null && !dynamicObjects.isEmpty()) {
                    dgdlEntryentity.addNew();
                    this.setCollectDate(dynamicObjects, key, index);
                    index++;
                }
            }
            //空值单独处理
            List<DynamicObject> nullList = objects.stream().filter(item -> Objects.isNull(item.getDynamicObject("dgdl_jobproperty"))).collect(Collectors.toList());
            if (!nullList.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(nullList, "空值", index);
                index++;
            }
            //合计分录
            setSumEntry(index);
        }
        this.getView().updateView("dgdl_entryentity");
    }

    //岗位标签汇总表
    private void setJobTagDate(Long detailId) {
        logger.info("岗位标签汇总表");
        //获取单据体控件
        EntryGrid entryGrid = this.getControl("dgdl_entryentity");
        entryGrid.setColumnProperty("dgdl_textfield", ClientProperties.Header, new LocaleString("特殊标签统计"));
        DynamicObjectCollection dgdlEntryentity = (DynamicObjectCollection) this.getModel().getValue("dgdl_entryentity");
        //查询编制详情
        DynamicObject object = BusinessDataServiceHelper.loadSingle(detailId, "dgdl_planyear_detail");
        //获取明细数据
        DynamicObjectCollection dgdlEntry = object.getDynamicObjectCollection("dgdl_entry");
        int index = 0;
        if (dgdlEntry != null && !dgdlEntry.isEmpty()) {
            List<DynamicObject> objects = new ArrayList<>(dgdlEntry);
            //过滤新四化
            List<DynamicObject> objectList = objects.stream()
                    .filter(s -> "1".equals(s.getString("dgdl_label2"))).collect(Collectors.toList());
            if (!objectList.isEmpty()) {
                dgdlEntryentity.addNew();
                //分录赋值
                this.setCollectDate(objectList, "新四化人才", index);
                index++;
            }
            //过滤国际化
            List<DynamicObject> objectList1 = objects.stream()
                    .filter(s -> "1".equals(s.getString("dgdl_label3"))).collect(Collectors.toList());
            if (!objectList1.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(objectList1, "国际化人才", index);
                index++;
            }
            //过滤数字化
            List<DynamicObject> objectList2 = objects.stream()
                    .filter(s -> "1".equals(s.getString("dgdl_label4"))).collect(Collectors.toList());
            if (!objectList2.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(objectList2, "数字化人才", index);
                index++;
            }
            //过滤大雁
            List<DynamicObject> objectList3 = objects.stream()
                    .filter(s -> "1".equals(s.getString("dgdl_label1"))).collect(Collectors.toList());
            if (!objectList3.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(objectList3, "大雁", index);
                index++;
            }

            //是否对外派驻
            //过滤大雁
            List<DynamicObject> objectList4 = objects.stream()
                    .filter(s -> "Y".equals(s.getString("dgdl_dispatch"))).collect(Collectors.toList());
            if (!objectList4.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(objectList4, "对外派驻", index);
                index++;
            }

            //空值处理
            List<DynamicObject> objectList5 = objects.stream()
                    .filter(s -> "0".equals(s.getString("dgdl_label1"))
                            || "0".equals(s.getString("dgdl_label2"))
                            || "0".equals(s.getString("dgdl_label3"))
                            || "0".equals(s.getString("dgdl_label4"))
                            || "N".equals(s.getString("dgdl_dispatch"))
                    )
                    .collect(Collectors.toList());
            if (!objectList5.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(objectList5, "空值", index);
                index++;
            }
            //合计分录
            setSumEntry(index);
            this.getView().updateView("dgdl_entryentity");
        }

    }

    //岗级汇总表
    private void setJobLevelDate(Long detailId) {
        //查询编制详情
        DynamicObject object = BusinessDataServiceHelper.loadSingle(detailId, "dgdl_planyear_detail");
        //获取明细数据
        DynamicObjectCollection dgdlEntry = object.getDynamicObjectCollection("dgdl_entry");
        DynamicObjectCollection dgdlEntryentity = (DynamicObjectCollection) this.getModel().getValue("dgdl_entryentity");
        if (dgdlEntry != null && !dgdlEntry.isEmpty()) {
            List<DynamicObject> objects = new ArrayList<>(dgdlEntry);
            //根据岗级分组
            Map<String, List<DynamicObject>> collect = objects.stream()
                    .filter(item -> Objects.nonNull(item.getDynamicObject("dgdl_personrank")))
                    .collect(Collectors.groupingBy(item -> item.getString("dgdl_personrank.name")));
            int index = 0;
            for (String key : collect.keySet()) {
                //获取分组数据
                List<DynamicObject> dynamicObjects = collect.get(key);
                if (dynamicObjects != null && !dynamicObjects.isEmpty()) {
                    dgdlEntryentity.addNew();
                    this.setCollectDate(dynamicObjects, key, index);
                    index++;
                }
            }

            //空值单独处理
            List<DynamicObject> nullList = objects.stream().filter(item -> Objects.isNull(item.getDynamicObject("dgdl_personrank"))).collect(Collectors.toList());
            if (!nullList.isEmpty()) {
                dgdlEntryentity.addNew();
                this.setCollectDate(nullList, "空值", index);
                index++;
            }

            //获取单据体控件
            EntryGrid entryGrid = this.getControl("dgdl_entryentity");
            entryGrid.setColumnProperty("dgdl_textfield", ClientProperties.Header, new LocaleString("按岗级统计"));
            this.getView().updateView("dgdl_entryentity");
            //合计分录
            setSumEntry(index);
        }

    }


    private void setCollectDate(List<DynamicObject> collect, String collectName, int index) {
        IDataModel model = this.getModel();
        model.setValue("dgdl_textfield", collectName, index);
        //实际人数和
        int actual = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_actual")) ? 0 : a.getInt("dgdl_actual")).sum();
        model.setValue("dgdl_actual", actual, index);
        //去年10月
        int dgdl_preavg10 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_preavg10")) ? 0 : a.getInt("dgdl_preavg10")).sum();
        model.setValue("dgdl_preavg10", dgdl_preavg10, index);

        //去年11月
        int dgdl_preavg11 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_preavg11")) ? 0 : a.getInt("dgdl_preavg11")).sum();
        model.setValue("dgdl_preavg11", dgdl_preavg11, index);

        //去年12月
        int dgdl_preavg12 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_preavg12")) ? 0 : a.getInt("dgdl_preavg12")).sum();
        model.setValue("dgdl_preavg12", dgdl_preavg12, index);

        //1月
        int dgdl_avg1 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg1")) ? 0 : a.getInt("dgdl_avg1")).sum();
        model.setValue("dgdl_avg1", dgdl_avg1, index);

        //2月
        int dgdl_avg2 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg2")) ? 0 : a.getInt("dgdl_avg2")).sum();
        model.setValue("dgdl_avg2", dgdl_avg2, index);

        //3月
        int dgdl_avg3 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg3")) ? 0 : a.getInt("dgdl_avg3")).sum();
        model.setValue("dgdl_avg3", dgdl_avg3, index);

        //4月
        int dgdl_avg4 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg4")) ? 0 : a.getInt("dgdl_avg4")).sum();
        model.setValue("dgdl_avg4", dgdl_avg4, index);

        //5月
        int dgdl_avg5 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg5")) ? 0 : a.getInt("dgdl_avg5")).sum();
        model.setValue("dgdl_avg5", dgdl_avg5, index);

        //6月
        int dgdl_avg6 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg6")) ? 0 : a.getInt("dgdl_avg6")).sum();
        model.setValue("dgdl_avg6", dgdl_avg6, index);

        //7月
        int dgdl_avg7 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg7")) ? 0 : a.getInt("dgdl_avg7")).sum();
        model.setValue("dgdl_avg7", dgdl_avg7, index);

        //8月
        int dgdl_avg8 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg8")) ? 0 : a.getInt("dgdl_avg8")).sum();
        model.setValue("dgdl_avg8", dgdl_avg8, index);

        //9月
        int dgdl_avg9 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg9")) ? 0 : a.getInt("dgdl_avg9")).sum();
        model.setValue("dgdl_avg9", dgdl_avg9, index);

        //10月
        int dgdl_avg10 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg10")) ? 0 : a.getInt("dgdl_avg10")).sum();
        model.setValue("dgdl_avg10", dgdl_avg10, index);

        //11月
        int dgdl_avg11 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg11")) ? 0 : a.getInt("dgdl_avg11")).sum();
        model.setValue("dgdl_avg11", dgdl_avg11, index);

        //12月
        int dgdl_avg12 = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_avg12")) ? 0 : a.getInt("dgdl_avg12")).sum();
        model.setValue("dgdl_avg12", dgdl_avg12, index);

        //平均人数
        int dgdl_average = collect.stream().mapToInt(a -> Objects.isNull(a.get("dgdl_average")) ? 0 : a.getInt("dgdl_average")).sum();
        model.setValue("dgdl_average", dgdl_average, index);
    }

    //合计分录
    private void setSumEntry(int index) {
        DynamicObjectCollection entity = this.getModel().getEntryEntity("dgdl_entryentity");
        entity.addNew();
        List<DynamicObject> objects = new ArrayList<>(entity);
        this.setCollectDate(objects, "总人数", index);
        this.getView().updateView("dgdl_entryentity");
    }
}
