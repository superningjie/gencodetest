package dgdl.odc.homs.formplugin;

import com.alibaba.fastjson.JSONObject;
import dgdl.odc.homs.common.DateTimeCommon;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.BeforeImportEntryEventArgs;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
import kd.bos.form.*;
import kd.bos.form.container.Tab;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.TabSelectEvent;
import kd.bos.form.control.events.TabSelectListener;
import kd.bos.form.events.*;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.importentry.resolving.ImportEntryData;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.tree.TreeFilterParameter;
import kd.bos.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

/**
 * @Author: 陈路
 * @CreateTime: 2024-01-26  13:45
 * @Description: 年度编制详情表单插件
 */
public class PlanyearDetailFormPlugin extends AbstractBillPlugIn implements HyperLinkClickListener,TabSelectListener {
    private static Log logger = LogFactory.getLog(PlanyearDetailFormPlugin.class);

    private static String[] monthFieldArr = new String[]{"dgdl_prechg10", "dgdl_prechg11", "dgdl_prechg12", "dgdl_chg1", "dgdl_chg2", "dgdl_chg3", "dgdl_chg4", "dgdl_chg5", "dgdl_chg6", "dgdl_chg7", "dgdl_chg8", "dgdl_chg9", "dgdl_chg10", "dgdl_chg11", "dgdl_chg12", "dgdl_preavg10", "dgdl_preavg11", "dgdl_preavg12", "dgdl_avg1", "dgdl_avg2", "dgdl_avg3", "dgdl_avg4", "dgdl_avg5", "dgdl_avg6", "dgdl_avg7", "dgdl_avg8", "dgdl_avg9", "dgdl_avg10", "dgdl_avg11", "dgdl_avg12"};

    @Override
    public void registerListener(EventObject e) {
        Tab tab = this.getControl("dgdl_tabap");
        tab.addTabSelectListener(this);
        super.registerListener(e);
        this.addItemClickListeners("dgdl_advcontoolbarap");
        EntryGrid entryGrid = this.getControl("dgdl_entry");
        entryGrid.addHyperClickListener(this);

        IFormView view = this.getView();
        IDataModel model = this.getModel();

        BasedataEdit personrank = view.getControl("dgdl_personrank");
        personrank.addBeforeF7SelectListener((listener) -> {
            //业务单元
            DynamicObject dgdlPlanyear = (DynamicObject) model.getValue("dgdl_planyear");
            if (Objects.nonNull(dgdlPlanyear)) {
                DynamicObject org = dgdlPlanyear.getDynamicObject("dgdl_org");
                //职级通道
                QFilter filter = new QFilter("createorg.id", QCP.equals, org.getPkValue()).and("enable", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1").and("datastatus", QCP.equals, "1");
                DynamicObject obj = BusinessDataServiceHelper.loadSingle("hbjm_joblevelscmhr", "id", filter.toArray());
                if (Objects.nonNull(obj)) {
                    ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
                    QFilter personQfilter = new QFilter("joblevelscm.id", QCP.equals, obj.getPkValue());
                    showParam.getListFilterParameter().getQFilters().add(personQfilter);
                }
            }
        });

        //岗位通道过滤
        BasedataEdit aisleCol = view.getControl("dgdl_post_aisle");
        aisleCol.addBeforeF7SelectListener((listener) -> {
            //业务单元
            DynamicObject dgdlPlanyear = (DynamicObject) model.getValue("dgdl_planyear");
            if (Objects.nonNull(dgdlPlanyear)) {
                DynamicObject org = dgdlPlanyear.getDynamicObject("dgdl_org");
                ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
                QFilter filter1 = new QFilter("createorg.id", QCP.equals, org.getPkValue());
                QFilter filter2 = new QFilter("ctrlstrategy", QCP.equals, "5");
                showParam.getListFilterParameter().getQFilters().add(filter2.or(filter1));
            }
        });

        //标准岗位过滤
        BasedataEdit jobEdit = this.getView().getControl("dgdl_stdposition");
        jobEdit.addBeforeF7SelectListener((listener) -> {
            //行政组织
            DynamicObject firstAdmin = (DynamicObject) model.getValue("dgdl_adminorg1");
            List<String> adminOrgNumberList = new ArrayList();
            if (firstAdmin != null){
                //查询行政组织结构
                QFilter qFilter = new QFilter("adminorg.id",QCP.equals,firstAdmin.getPkValue()).and("iscurrentversion",QCP.equals,true);
                DynamicObject adminOrgStruct = BusinessDataServiceHelper.loadSingleFromCache("haos_adminorgstruct", "structlongnumber", qFilter.toArray());
                if (adminOrgStruct != null){
                    //组织上下级结构长编码
                    String orgStructNumber = adminOrgStruct.getString("structlongnumber");
                    logger.info("实际岗位行政组织结构上下级结构长编码"+orgStructNumber);
                    //因为结构长编码是通过!拼接的所以通过!来切割
                    String[] structNumber = orgStructNumber.split("!");
                    //查询行政组织获取编码过滤
                    QFilter qFilter1 = new QFilter("structnumber",QCP.in,structNumber).and("iscurrentversion",QCP.equals,true);
                    DynamicObject[] adminOrg = BusinessDataServiceHelper.load("haos_adminorghr", "number", qFilter1.toArray());
                    for (DynamicObject dynamicObject : adminOrg) {
                        adminOrgNumberList.add(dynamicObject.getString("number"));
                    }
                }

            }
            logger.info("实际岗位行政组织结构最终数据"+adminOrgNumberList);
            //标准岗位过滤
            QFilter filter = new QFilter("dgdl_org.fbasedataid.number", QCP.in, adminOrgNumberList);
            listener.addCustomQFilter(filter);
        });

    }


    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

//        Tab tab = this.getControl("dgdl_tabap");
//        String currentTab = tab.getCurrentTab();
//        if (currentTab.equals("dgdl_tabpageap")) {
//            this.getModel().setValue("dgdl_joblevel",false);
//            this.getModel().setValue("dgdl_jobtag",false);
//            this.getModel().setValue("dgdl_property",false);
//            this.getModel().setValue("dgdl_hz_laborreltype",false);
//
//        }
        IFormView view = this.getView();
        //隐藏明细页签分录月份字段
        this.getView().setVisible(false, monthFieldArr);
        IDataModel model = this.getModel();
        //编制计划
        DynamicObject dgdlPlanObj = (DynamicObject) model.getValue("dgdl_planyear");
        if (dgdlPlanObj == null) {
            return;
        }
        //所属年度
        Date dgdlYear = dgdlPlanObj.getDate("dgdl_year");
        int year = DateTimeCommon.getYear(dgdlYear);
        //分录字段重命名
        //开始月份
        Date dgdlStartmonth = dgdlPlanObj.getDate("dgdl_startmonth");
        //结束月份
        Date dgdlEndmonth = dgdlPlanObj.getDate("dgdl_endmonth");
        long monthCount = DateTimeCommon.reduceMon(dgdlStartmonth, dgdlEndmonth);
        EntryGrid entryGrid = this.getControl("dgdl_entry");
        //末级组织
        DynamicObject adminorg = (DynamicObject) model.getValue("adminorg");
        //组织
        Object orgfield = model.getValue("dgdl_orgfield");
        if (Objects.isNull(orgfield)) {
            model.setValue("dgdl_orgfield", dgdlPlanObj.getDynamicObject("dgdl_org"));
        }

        for (long i = 0; i < monthCount + 1; i++) {
            int startYear = DateTimeCommon.getYear(dgdlStartmonth);
            String dateMonthStr = DateTimeCommon.getDateMonthStr(dgdlStartmonth);
            int month = DateTimeCommon.getMonth(dgdlStartmonth);
            //月份字段名
            String fieldKey1 = "dgdl_chg" + month;
            String fieldKey2 = "dgdl_avg" + month;
            if (startYear != year) {
                //取上一年月份字段名
                fieldKey1 = "dgdl_prechg" + month;
                fieldKey2 = "dgdl_preavg" + month;
            }
            entryGrid.setColumnProperty(fieldKey1, ClientProperties.Header, new LocaleString(dateMonthStr));
            entryGrid.setColumnProperty(fieldKey2, ClientProperties.Header, new LocaleString(dateMonthStr));
            this.getView().setVisible(true, fieldKey1, fieldKey2);
            dgdlStartmonth = DateTimeCommon.addMonth(dgdlStartmonth, 1);
        }

        DynamicObjectCollection entryEntity = model.getEntryEntity("dgdl_entry");
        //标准岗位相关信息锁定
        for (int i = 0; i < entryEntity.size(); i++) {
            DynamicObject entryObj = entryEntity.get(i);
            //实际人数
            int actual = entryObj.getInt("dgdl_actual");
            //分录末级组织
            Long entryLastOrg = (Long) entryObj.getDynamicObject("dgdl_detail_adminorg").getPkValue();
            if (adminorg.getPkValue().equals(entryLastOrg)) {
                if (actual == 0) {
                    this.setEnable(view, i, true);
                    model.setValue("dgdl_detail_adminorg1", model.getValue("dgdl_adminorg1"), i);
                    model.setValue("dgdl_detail_adminorg2", model.getValue("dgdl_adminorg2"), i);
                    model.setValue("dgdl_detail_adminorg3", model.getValue("dgdl_adminorg3"), i);
                    model.setValue("dgdl_detail_adminorg4", model.getValue("dgdl_adminorg4"), i);
                    model.setValue("dgdl_detail_adminorg5", model.getValue("dgdl_adminorg5"), i);
                    model.setValue("dgdl_detail_adminorg6", model.getValue("dgdl_adminorg6"), i);
                    //计划新增
                    int add = (int) (model.getValue("dgdl_add", i) == null ? 0 : model.getValue("dgdl_add", i));
                    if (add != 0) {
                        model.setValue("dgdl_add_str", add, i);
                    }
                    //计划减少
                    int reduce = (int) (model.getValue("dgdl_reduce", i) == null ? 0 : model.getValue("dgdl_actual", i));
                    if (reduce != 0) {
                        model.setValue("dgdl_reduce_str", reduce, i);
                    }
                } else {
                    view.setEnable(false, i, "dgdl_stdposition");
                    view.setEnable(false, i, "dgdl_post_aisle");
                    view.setEnable(false, i, "dgdl_jobproperty");
                    view.setEnable(false, i, "dgdl_personrank");
                    view.setEnable(false, i, "dgdl_dispatch");
                    view.setEnable(false, i, "dgdl_laborreltype");
                    view.setEnable(false, i, "dgdl_workplace");
                    view.setEnable(false, i, "dgdl_label1");
                    view.setEnable(false, i, "dgdl_label2");
                    view.setEnable(false, i, "dgdl_label3");
                    view.setEnable(false, i, "dgdl_label4");
                }
            } else {
                this.setEnable(view, i, false);
            }
        }

        //行政组织列隐藏
        List<String> list = new ArrayList<>();
        list.add("dgdl_detail_adminorg1");
        list.add("dgdl_detail_adminorg2");
        list.add("dgdl_detail_adminorg3");
        list.add("dgdl_detail_adminorg4");
        list.add("dgdl_detail_adminorg5");
        list.add("dgdl_detail_adminorg6");
        List<String> showList = new ArrayList<>();
        if (entryEntity.isEmpty()) {
            //组织层级
            String hierarchy = dgdlPlanObj.getString("dgdl_orghierarchy");
            int intHierarchy = Integer.parseInt(hierarchy);
            String adminStr = "dgdl_adminorg";
            //分录字段
            String entryAdminStr = "dgdl_detail_adminorg";
            for (int i = 1; i <= intHierarchy; i++) {
                DynamicObject adminObj = (DynamicObject) model.getValue(adminStr + i);
                if (Objects.nonNull(adminObj)) {
                    showList.add(entryAdminStr + i);
                }
            }
        } else {
            for (String adminName : list) {
                for (DynamicObject entry : entryEntity) {
                    DynamicObject adminObj = entry.getDynamicObject(adminName);
                    if (Objects.nonNull(adminObj)) {
                        showList.add(adminName);
                        break;
                    }
                }
            }
        }
        list.removeAll(showList);
        for (String str : list) {
            this.getView().setVisible(false, str);
        }
        this.getView().updateView("dgdl_entry");
    }


    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String fieldKey = e.getProperty().getName();
        Object newValue = e.getChangeSet()[0].getNewValue();
        int rowIndex = e.getChangeSet()[0].getRowIndex();
        IDataModel model = this.getModel();
        //值改变字段
        List<String> filedList = new ArrayList<>();
        filedList.add("dgdl_chg1");
        filedList.add("dgdl_chg2");
        filedList.add("dgdl_chg3");
        filedList.add("dgdl_chg4");
        filedList.add("dgdl_chg5");
        filedList.add("dgdl_chg6");
        filedList.add("dgdl_chg7");
        filedList.add("dgdl_chg8");
        filedList.add("dgdl_chg9");
        filedList.add("dgdl_chg10");
        filedList.add("dgdl_chg11");
        filedList.add("dgdl_chg12");
        filedList.add("dgdl_add");
        filedList.add("dgdl_reduce");
        filedList.add("dgdl_joblevel");
        filedList.add("dgdl_jobtag");
        filedList.add("dgdl_property");
        filedList.add("dgdl_hz_laborreltype");
        filedList.add("dgdl_personrank");
        filedList.add("dgdl_stdposition");
        //实际人数
        int actual = 0;
        //总数
        int all = 0;
        //计划新增
        int add = 0;
        //计划减少
        int reduce = 0;
        logger.info("PlanyearDetailFormPlugin改变字段=" + fieldKey);
        if ("dgdl_add".equals(fieldKey) || "dgdl_reduce".equals(fieldKey)) {
            //实际人数
            actual = (int) (model.getValue("dgdl_actual", rowIndex) == null ? 0 : model.getValue("dgdl_actual", rowIndex));
            add = (int) (model.getValue("dgdl_add", rowIndex) == null ? 0 : model.getValue("dgdl_add", rowIndex));
            reduce = (int) (model.getValue("dgdl_reduce", rowIndex) == null ? 0 : model.getValue("dgdl_reduce", rowIndex));
            all = add + reduce;
        }
        if (filedList.contains(fieldKey)) {
            switch (fieldKey) {
                case "dgdl_joblevel":
                    //岗级汇总表
                    this.showHzPortForm((Boolean) newValue, "dgdl_flexjoblevel", "1");
                    this.getModel().setDataChanged(false);
                    break;
                case "dgdl_jobtag":
                    //岗位标签汇总表
                    this.showHzPortForm((Boolean) newValue, "dgdl_flexjobtag", "2");
                    this.getModel().setDataChanged(false);
                    break;
                case "dgdl_property":
                    //岗位属性汇总表
                    this.showHzPortForm((Boolean) newValue, "dgdl_flexproperty", "3");
                    this.getModel().setDataChanged(false);
                    break;
                case "dgdl_hz_laborreltype":
                    //一线非一线+用工关系类型汇总表
                    this.showHzPortForm((Boolean) newValue, "dgdl_flexlaborreltype", "4");
                    this.getModel().setDataChanged(false);
                    break;
                case "dgdl_add":
                    if (add < 0) {
                        model.setValue("dgdl_add", 0, rowIndex);
                        this.getView().showErrorNotification("计划新增人数不能为负数");
                    } else {
                        model.setValue("dgdl_control", all, rowIndex);
                        model.setValue("dgdl_plan", all + actual, rowIndex);
                    }
                    break;
                case "dgdl_reduce":
                    if (reduce > 0) {
                        model.setValue("dgdl_reduce", 0, rowIndex);
                        this.getView().showErrorNotification("计划减少人数不能为正数");
                    } else {
                        model.setValue("dgdl_control", all, rowIndex);
                        model.setValue("dgdl_plan", all + actual, rowIndex);
                    }
                    break;
                case "dgdl_personrank":
                    //个人职级
                    DynamicObject rank = (DynamicObject) model.getValue("dgdl_personrank", rowIndex);
                    if (Objects.nonNull(rank)) {
                        //岗位通道
                        DynamicObject channel = rank.getDynamicObject("dgdl_post_channel");
                        model.setValue("dgdl_post_aisle", channel, rowIndex);
                    } else {
                        model.setValue("dgdl_post_aisle", null, rowIndex);
                    }
                    break;
                case "dgdl_stdposition":
                    //标准岗位
                    DynamicObject stdposition = (DynamicObject) model.getValue("dgdl_stdposition", rowIndex);
                    if (Objects.nonNull(stdposition)) {
                        //岗位属性
                        DynamicObject jobproperty = stdposition.getDynamicObject("dgdl_jobproperty");
                        model.setValue("dgdl_jobproperty", jobproperty, rowIndex);
                    } else {
                        model.setValue("dgdl_jobproperty", null, rowIndex);
                    }
                    break;
                default:
                    String str = "dgdl_avg";
                    DynamicObject planYear = (DynamicObject) model.getValue("dgdl_planyear");
                    //计划类型
                    String planType = planYear.getString("dgdl_plantype");
                    //计划开始时间
                    Date dgdlStartmonth = planYear.getDate("dgdl_startmonth");
                    int startMonth = Integer.parseInt(new SimpleDateFormat("MM").format(dgdlStartmonth));
                    int type = 0;
                    switch (planType) {
                        case "1":
                            if (startMonth > 1) {
                                type = 12 - startMonth + 1;
                            } else {
                                type = 12;
                            }
                            break;
                        case "2":
                            if (startMonth > 7) {
                                type = 12 - startMonth + 1;
                            } else {
                                type = 6;
                            }
                            //开始月份重新赋值
                            startMonth = Math.max(startMonth, type);
                            break;
                        case "3":
                            if (startMonth > 10) {
                                type = 12 - startMonth + 1;
                            } else {
                                type = 3;
                            }
                            startMonth = Math.max(startMonth, type);
                            break;
                    }
                    int count = 0;
                    for (int i = startMonth; i <= 12; i++) {
                        Object number = model.getValue(str + i, rowIndex);
                        if (Objects.nonNull(number)) {
                            count = count + (int) number;
                        }
                    }
                    logger.info("PlanyearDetailFormPlugin开始日期=" + startMonth + "被除数=" + type);
                    if (count > 0) {
                        BigDecimal divide = new BigDecimal(count).divide(new BigDecimal(type), 1, RoundingMode.HALF_UP);
                        model.setValue("dgdl_average", divide, rowIndex);
                    }
                    break;
            }
        }
    }

    /**
     * 打开汇总表
     *
     * @param b
     * @param type
     */
    private void showHzPortForm(boolean b, String type, String table) {
        String dgdlFlexjoblevel = this.getPageCache().get(type);
        if (b && dgdlFlexjoblevel == null) {
            FormShowParameter fsp = new FormShowParameter();
            if ("1".equals(table)) {
                fsp.setFormId("dgdl_plancollect");
            } else if ("2".equals(table)) {
                fsp.setFormId("dgdl_plancollect2");
            } else if ("3".equals(table)) {
                fsp.setFormId("dgdl_plancollect3");
            } else if ("4".equals(table)) {
                fsp.setFormId("dgdl_plancollect4");
            }
            //编制详情ID
            fsp.setCustomParam("detailId", this.getModel().getDataEntity().getPkValue());
            //编制计划ID
            DynamicObject dgdlPlanObj = (DynamicObject) this.getModel().getValue("dgdl_planyear");
            fsp.setCustomParam("planId", dgdlPlanObj.getPkValue());
            //类型
            fsp.setCustomParam("type", type);
            fsp.getOpenStyle().setShowType(ShowType.InContainer);
            fsp.getOpenStyle().setTargetKey(type);
            this.getView().showForm(fsp);
            this.getPageCache().put(type, "true");
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("dgdl_barsyndata".equals(itemKey)) {
            DynamicObject dgdlPlanObj = (DynamicObject) this.getModel().getValue("dgdl_planyear");
            //开始月份
            Date dgdlStartmonth = dgdlPlanObj.getDate("dgdl_startmonth");
            Date monthStartDate = DateTimeCommon.getMonthStartDate(dgdlStartmonth);
            Date date = DateTimeCommon.addDay(monthStartDate, -1);
            if (date.compareTo(new Date()) > 0) {
                date = new Date();
            }
            //同步在岗人员
            FormShowParameter fsp = new FormShowParameter();
            fsp.setFormId("dgdl_syn_person");
            //同步日期默认开始月份前一天
            fsp.setCustomParam("syn_date", date);
            //编制计划
            fsp.setCustomParam("planId", dgdlPlanObj.getPkValue());
            //编制详情ID
            fsp.setCustomParam("detailId", this.getModel().getDataEntity().getPkValue());
            fsp.getOpenStyle().setShowType(ShowType.Modal);
            fsp.setCloseCallBack(new CloseCallBack(this, "detail"));
            this.getView().showForm(fsp);
        } else if ("dgdl_addnew".equals(itemKey)) {
            DynamicObjectCollection dgdlEntry = this.getModel().getEntryEntity("dgdl_entry");
            //获取分录
            int index = dgdlEntry.size() - 1;
            IDataModel model = this.getModel();
            model.setValue("dgdl_detail_adminorg1", model.getValue("dgdl_adminorg1"), index);
            model.setValue("dgdl_detail_adminorg2", model.getValue("dgdl_adminorg2"), index);
            model.setValue("dgdl_detail_adminorg3", model.getValue("dgdl_adminorg3"), index);
            model.setValue("dgdl_detail_adminorg4", model.getValue("dgdl_adminorg4"), index);
            model.setValue("dgdl_detail_adminorg5", model.getValue("dgdl_adminorg5"), index);
            model.setValue("dgdl_detail_adminorg6", model.getValue("dgdl_adminorg6"), index);
            model.setValue("dgdl_detail_adminorg", model.getValue("adminorg"), index);
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent e) {
        super.closedCallBack(e);
        String actionId = e.getActionId();
        if ("detail".equals(actionId)) {
            this.getView().invokeOperation("refresh");
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate operate = (FormOperate) args.getSource();
        String operateKey = operate.getOperateKey();
        IDataModel model = this.getModel();
        //末端组织
        DynamicObject adminorg = (DynamicObject) model.getValue("adminorg");
        EntryGrid entryGrid = this.getControl("dgdl_entry");
        int[] selectRows = entryGrid.getSelectRows();
        DynamicObjectCollection dgdlEntry = model.getEntryEntity("dgdl_entry");
        logger.info("PlanyearDetailFormPlugin操作按钮=" + operateKey);
        if ("confirm".equals(operateKey)) {
            for (DynamicObject entry : dgdlEntry) {
                if (StringUtils.isEmpty(entry.getString("dgdl_groupid"))) {
                    throw new KDBizException("请先保存在确认。");
                }
            }
            this.getView().invokeOperation("save");
        } else if ("lock".equals(operateKey)) {
            for (DynamicObject entry : dgdlEntry) {
                if (StringUtils.isEmpty(entry.getString("dgdl_groupid"))) {
                    throw new KDBizException("请先保存在锁定。");
                }
            }
        } else if ("submit".equals(operateKey)) {
            boolean isSubmit = true;
            for (DynamicObject entry : dgdlEntry) {
                if (StringUtils.isEmpty(entry.getString("dgdl_groupid"))) {
                    isSubmit = false;
                    break;
                }
            }
            if (!isSubmit) {
//                this.getView().setVisible(false, "bar_save", "dgdl_baritemap1");
//                this.getView().setEnable(false, "bar_submit");
//            } else {
                throw new KDBizException("请先保存在提交。");
            }
        } else if ("deleteentry".equals(operateKey)) {
            //可以删除行
            List<Integer> deleteList = new ArrayList<>();
            for (int index : selectRows) {
                if (index < dgdlEntry.size()) {
                    DynamicObject entryData = dgdlEntry.get(index);
                    if (Objects.nonNull(entryData)) {
                        DynamicObject lastOrg = entryData.getDynamicObject("dgdl_detail_adminorg");
                        if (Objects.nonNull(adminorg) && Objects.nonNull(lastOrg)) {
                            if (!adminorg.getString("number").equals(lastOrg.getString("number"))) {
                                this.getView().showErrorNotification("第" + (index + 1) + "行不属于本组织数据,无法删除");
                            } else {
                                deleteList.add(index);
                            }
                        }
                    }
                }
            }
            if (!deleteList.isEmpty()) {
                int[] integers = new int[deleteList.size()];
                for (int i = 0; i < deleteList.size(); i++) {
                    integers[i] = deleteList.get(i);
                }
                model.deleteEntryRows("dgdl_entry", integers);
            }
            args.setCancel(true);
        } else if ("save".equals(operateKey)) {
            for (int i = 0; i < dgdlEntry.size(); i++) {
                DynamicObject entry = dgdlEntry.get(i);
                //计划增加和减少必须和计划增减员人数保存一致
                int addCount = 0;
                int reduceCount = 0;
                //计划增加
                int add = (int) (model.getValue("dgdl_add", i) == null ? 0 : model.getValue("dgdl_add", i));
                //计划减少
                int reduce = (int) (model.getValue("dgdl_reduce", i) == null ? 0 : model.getValue("dgdl_reduce", i));
                //计划减少
                String preStr = "dgdl_prechg";
                for (int j = 10; j <= 12; j++) {
                    Object numberObj = entry.get(preStr + j);
                    if (Objects.nonNull(numberObj)) {
                        int number = (int) numberObj;
                        if (number > 0) {
                            addCount = addCount + number;
                        } else {
                            reduceCount = reduceCount + number;
                        }
                    }
                }
                String chgStr = "dgdl_chg";
                for (int j = 1; j <= 12; j++) {
                    Object numberObj = entry.get(chgStr + j);
                    if (Objects.nonNull(numberObj)) {
                        int number = (int) numberObj;
                        if (number > 0) {
                            addCount = addCount + number;
                        } else {
                            reduceCount = reduceCount + number;
                        }
                    }
                }
                if (add != addCount) {
                    throw new KDBizException("第" + (i + 1) + "行计划新增必须和计划增加的人数保持一致");
                } else if (reduce != reduceCount) {
                    throw new KDBizException("第" + (i + 1) + "行计划减少必须和计划减少的人数保持一致");
                }
                //分录末级组织
                DynamicObject lastOrg = entry.getDynamicObject("dgdl_detail_adminorg");
                //实际人数
                int actual = entry.getInt("dgdl_actual");
                if (adminorg.getPkValue().equals(lastOrg.getPkValue()) && actual == 0) {
                    //分录唯一值
                    StringBuilder groupId = new StringBuilder();
                    //报错字段
                    StringBuilder errorStr = new StringBuilder();
                    groupId.append(lastOrg.getPkValue()).append("-");
                    //用工类型
                    DynamicObject laborreltype = entry.getDynamicObject("dgdl_laborreltype");
                    if (Objects.nonNull(laborreltype)) {
                        groupId.append(laborreltype.getPkValue()).append("-");
                    }
                    //标准岗位
                    DynamicObject stdposition = entry.getDynamicObject("dgdl_stdposition");
                    if (Objects.nonNull(stdposition)) {
                        groupId.append(stdposition.getPkValue()).append("-");
                    } else {
                        errorStr.append("标准岗位,");
                    }
                    //职级
                    DynamicObject personrank = entry.getDynamicObject("dgdl_personrank");
                    if (Objects.nonNull(personrank)) {
                        groupId.append(personrank.getPkValue()).append("-");
                    } else {
                        errorStr.append("职级,");
                    }
                    //岗位通道
                    DynamicObject aisle = entry.getDynamicObject("dgdl_post_aisle");
                    if (Objects.nonNull(aisle)) {
                        groupId.append(aisle.getPkValue()).append("-");
                    } else {
                        errorStr.append("岗位通道,");
                    }
                    //岗位属性
                    DynamicObject jobproperty = entry.getDynamicObject("dgdl_jobproperty");
                    if (Objects.nonNull(jobproperty)) {
                        groupId.append(jobproperty.getPkValue()).append("-");
                    } else {
                        errorStr.append("岗位属性,");
                    }
                    //获取编制计划
                    DynamicObject planYear = (DynamicObject) model.getValue("dgdl_planyear");
                    //获取浮动标签
                    String dimension = planYear.getString("dgdl_labeldimension");
                    //大雁
                    String label1 = entry.getString("dgdl_label1");
                    if (StringUtils.isNotEmpty(label1)) {
                        groupId.append(label1).append("-");
                    } else {
                        if (StringUtils.isNotEmpty(dimension) && dimension.contains("1")) {
                            errorStr.append("大雁,");
                        }
                    }
                    //新四化
                    String label2 = entry.getString("dgdl_label2");
                    if (StringUtils.isNotEmpty(label2)) {
                        groupId.append(label2).append("-");
                    } else {
                        if (StringUtils.isNotEmpty(dimension) && dimension.contains("2")) {
                            errorStr.append("新四化,");
                        }
                    }
                    //国际化
                    String label3 = entry.getString("dgdl_label3");
                    if (StringUtils.isNotEmpty(label3)) {
                        groupId.append(label3).append("-");
                    } else {
                        if (StringUtils.isNotEmpty(dimension) && dimension.contains("3")) {
                            errorStr.append("国际化,");
                        }
                    }
                    //数字化
                    String label4 = entry.getString("dgdl_label4");
                    if (StringUtils.isNotEmpty(label4)) {
                        groupId.append(label4).append("-");
                    } else {
                        if (StringUtils.isNotEmpty(dimension) && dimension.contains("4")) {
                            errorStr.append("数字化,");
                        }
                    }
                    //是否派驻
                    String dispatch = entry.getString("dgdl_dispatch");
                    if (StringUtils.isNotEmpty(dispatch)) {
                        groupId.append(dispatch).append("-");
                    } else {
                        if (StringUtils.isNotEmpty(dimension) && dimension.contains("5")) {
                            errorStr.append("是否派驻,");
                        }
                    }
                    //工作地点
                    DynamicObject workplace = entry.getDynamicObject("dgdl_workplace");
                    if (Objects.nonNull(workplace)) {
                        groupId.append(workplace.getPkValue()).append("-");
                    } else {
                        if (StringUtils.isNotEmpty(dimension) && dimension.contains("6")) {
                            errorStr.append("工作地点,");
                        }
                    }
                    String stringError = errorStr.toString();
                    if (StringUtils.isNotEmpty(stringError)) {
                        throw new KDBizException("第" + (i + 1) + "行" + stringError.substring(0, stringError.length() - 1) + "为空,请补充完整");
                    }
                    //设置分录唯一值
                    model.setValue("dgdl_groupid", groupId.toString(), i);
                }
            }
        }
    }


    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        OperationResult operationResult = args.getOperationResult();
        if (operationResult != null && operationResult.isSuccess()) {
            String operateKey = args.getOperateKey();
            logger.info("PlanyearDetailFormPlugin操作标识=" + operateKey);
            if ("lock".equals(operateKey) || "cancellock".equals(operateKey)) {
                this.getView().invokeOperation("save");
            } else if ("unsubmit".equals(operateKey)) {
                this.getView().setVisible(true, "bar_save", "dgdl_baritemap1");
                this.getView().setEnable(true, "bar_submit");
                this.getView().invokeOperation("refresh");
            } else if ("save".equals(operateKey)) {
                List<Object> successPkIds = operationResult.getSuccessPkIds();
                //如果分录为空,则删除当前单据
                DynamicObject detailObj = BusinessDataServiceHelper.loadSingle(successPkIds.get(0), "dgdl_planyear_detail");
                if (Objects.nonNull(detailObj)) {
                    DynamicObjectCollection collection = detailObj.getDynamicObjectCollection("dgdl_entry");
                    if (collection.isEmpty()) {
                        ConfirmCallBackListener confirmCallBackListener = new ConfirmCallBackListener("deleteDetail", this);
                        this.getView().showConfirm("当前组织人员为空,是否移除相应编制信息。", MessageBoxOptions.OKCancel, ConfirmTypes.Default, confirmCallBackListener);
                    }
                }
            }
        }
    }

    /**
     * 选择框回调函数
     *
     * @param event
     */
    @Override
    public void confirmCallBack(MessageBoxClosedEvent event) {
        super.confirmCallBack(event);
        if ("deleteDetail".equals(event.getCallBackId())) {
            MessageBoxResult result = event.getResult();
            if (MessageBoxResult.Yes.equals(result)) {
                this.getView().invokeOperation("delete");
            }
        }
    }

    @Override
    public void beforeImportEntry(BeforeImportEntryEventArgs args) {
        logger.info("PlanyearDetailFormPlugin进入beforeImportEntry");
        HashMap itemEntry = (HashMap) args.getSource();
        ArrayList importList = (ArrayList) itemEntry.get("dgdl_entry");
        Iterator iterator = importList.iterator();
        //获取当前分录数据
        IDataModel model = this.getModel();
        //获取编制计划
        DynamicObject planYear = (DynamicObject) model.getValue("dgdl_planyear");
        List<String> fieldList = setFieldList(planYear);
        //编制单位
        DynamicObject org = planYear.getDynamicObject("dgdl_org");
        //获取浮动标签
        String dimension = planYear.getString("dgdl_labeldimension");
        String type = planYear.getString("dgdl_useworktype");
        //末级组织
        DynamicObject adminOrg = (DynamicObject) model.getValue("adminorg");
        DynamicObjectCollection collection = model.getEntryEntity("dgdl_entry");
        Map<String, Integer> thisMap = this.joinStr(collection);
        //存储当前导入分录
        Map<String, String> importMap = new HashMap<>();
        //导入数据行
        int index = 1;
        while (iterator.hasNext()) {
            //拼接字符串
            StringBuilder sb = new StringBuilder();
            //报错字段
            StringBuilder errorStr = new StringBuilder();
            ImportEntryData entryData = (ImportEntryData) iterator.next();
            JSONObject entryDataJson = entryData.getData();
            logger.info("PlanyearDetailFormPlugin:JSON=" + entryDataJson);
            //唯一id
            String groupId = entryDataJson.getString("dgdl_groupid");
            if (StringUtils.isEmpty(groupId)) {
                //末端组织
                JSONObject importAdmin = entryDataJson.getJSONObject("dgdl_detail_adminorg");
                String adminOrgNumber = importAdmin.getString("number");
                if (!adminOrgNumber.equals(adminOrg.getString("number"))) {
                    throw new KDBizException("第" + index + "行末级组织必须为" + adminOrg.getString("name") + ",请调整相对应导入信息");
                } else {
                    sb.append(adminOrgNumber).append("-");
                }
                //职位编码
                JSONObject position = entryDataJson.getJSONObject("dgdl_stdposition");
                if (Objects.nonNull(position)) {
                    sb.append(position.getString("number")).append("-");
                }
                //职级编码
                JSONObject rank = entryDataJson.getJSONObject("dgdl_personrank");
                if (Objects.nonNull(rank)) {
                    String rankName = rank.getString("name");
                    sb.append(rankName).append("-");
                }
                //通道编码
                JSONObject aisle = entryDataJson.getJSONObject("dgdl_post_aisle");
                if (Objects.nonNull(aisle)) {
                    sb.append(aisle.getString("name")).append("-");
                }
                //岗位属性编码
                JSONObject jobproperty = entryDataJson.getJSONObject("dgdl_jobproperty");
                if (Objects.nonNull(jobproperty)) {
                    sb.append(jobproperty.getString("name")).append("-");
                }
                //用工关系类型编码
                JSONObject typeObj = entryDataJson.getJSONObject("dgdl_laborreltype");
                if (StringUtils.isNotEmpty(type)) {
                    if (Objects.nonNull(typeObj)) {
                        String typeName = typeObj.getString("name");
                        Map<String, String> typeNumberMap = new HashMap<>();
                        typeNumberMap.put("合作伙伴", "helpmate");
                        typeNumberMap.put("实习生", "interns_reserve_personnel");
                        typeNumberMap.put("劳务/外包", "labor_dispatch");
                        typeNumberMap.put("正式工", "regular_workers");
                        typeNumberMap.put("退休返聘", "rehired_after_retirement");
                        String typeNumber = typeNumberMap.get(typeName);
                        if (typeNumber!=null&&type.contains(typeNumber)) {
                            sb.append(typeName).append("-");
                        } else {
                            entryDataJson.remove("dgdl_laborreltype");
                        }
                    } else {
                        errorStr.append("用工关系类型,");
                    }
                } else {
                    entryDataJson.remove("dgdl_laborreltype");
                }
                //标签维度
                if (StringUtils.isNotEmpty(dimension)) {
                    //大雁
                    String label1 = entryDataJson.getString("dgdl_label1");
                    if (dimension.contains("1")) {
                        if (StringUtils.isNotEmpty(label1)) {
                            sb.append(isHas(label1)).append("-");
                        } else {
                            errorStr.append("大雁,");
                        }
                    } else {
                        entryDataJson.remove("dgdl_label1");
                    }
                    //新四化
                    String label2 = entryDataJson.getString("dgdl_label2");
                    if (dimension.contains("2")) {
                        if (StringUtils.isNotEmpty(label2)) {
                            sb.append(isHas(label2)).append("-");
                        } else {
                            errorStr.append("新四化,");
                        }
                    } else {
                        entryDataJson.remove("dgdl_label2");
                    }
                    //数字化
                    String label3 = entryDataJson.getString("dgdl_label3");
                    if (dimension.contains("3")) {
                        if (StringUtils.isNotEmpty(label3)) {
                            sb.append(isHas(label3)).append("-");
                        } else {
                            errorStr.append("国际化,");
                        }
                    } else {
                        entryDataJson.remove("dgdl_label3");
                    }
                    //国际化
                    String label4 = entryDataJson.getString("dgdl_label4");
                    if (dimension.contains("4")) {
                        if (StringUtils.isNotEmpty(label4)) {
                            sb.append(isHas(label4)).append("-");
                        } else {
                            errorStr.append("数字化,");
                        }
                    } else {
                        entryDataJson.remove("dgdl_label4");
                    }
                    //对外派驻
                    String dispatch = entryDataJson.getString("dgdl_dispatch");
                    if (dimension.contains("5")) {
                        if (StringUtils.isNotEmpty(dispatch)) {
                            sb.append(isHas(dispatch)).append("-");
                        } else {
                            errorStr.append("对外派驻,");
                        }
                    } else {
                        entryDataJson.remove("dgdl_dispatch");
                    }
                    //工作地点编码
                    JSONObject workPlace = entryDataJson.getJSONObject("dgdl_workplace");
                    if (dimension.contains("6")) {
                        if (Objects.nonNull(workPlace)) {
                            sb.append(workPlace.getString("name")).append("-");
                        } else {
                            errorStr.append("工作地点,");
                        }
                    } else {
                        entryDataJson.remove("dgdl_workplace");
                    }
                }
                logger.info("PlanyearDetailFormPlugin错误日志=" + errorStr);
                String stringError = errorStr.toString();
                if (StringUtils.isNotEmpty(stringError)) {
                    throw new KDBizException("第" + (index) + "行" + stringError.substring(0, stringError.length() - 1) + "导入数据为空,请补充完整");
                }
                //获取数据
                String value = importMap.get(sb.toString());
                if (Objects.isNull(value) || StringUtils.isEmpty(value)) {
                    Integer thisIndex = thisMap.get(sb.toString());
                    logger.info("第" + index + "行导入数据=" + sb + ",匹配分录信息=" + thisIndex);
                    if (Objects.nonNull(thisIndex)) {
                        //分录重新赋值
                        Integer dgdlAdd = entryDataJson.getInteger("dgdl_add");
                        Integer dgdlReduce = entryDataJson.getInteger("dgdl_reduce");

//                        Integer chg1 = entryDataJson.getInteger("dgdl_chg1");
//                        Integer chg2 = entryDataJson.getInteger("dgdl_chg2");
//                        Integer chg3 = entryDataJson.getInteger("dgdl_chg3");
//                        Integer chg4 = entryDataJson.getInteger("dgdl_chg4");
//                        Integer chg5 = entryDataJson.getInteger("dgdl_chg5");
//                        Integer chg6 = entryDataJson.getInteger("dgdl_chg6");
//                        Integer chg7 = entryDataJson.getInteger("dgdl_chg7");
//                        Integer chg8 = entryDataJson.getInteger("dgdl_chg8");
//                        Integer chg9 = entryDataJson.getInteger("dgdl_chg9");
//                        Integer chg10 = entryDataJson.getInteger("dgdl_chg10");
//                        Integer chg11 = entryDataJson.getInteger("dgdl_chg11");
//                        Integer chg12 = entryDataJson.getInteger("dgdl_chg12");
//                        Integer prechg10 = entryDataJson.getInteger("dgdl_prechg10");
//                        Integer prechg11 = entryDataJson.getInteger("dgdl_prechg11");
//                        Integer prechg12 = entryDataJson.getInteger("dgdl_prechg12");
                        model.setValue("dgdl_add", dgdlAdd, thisIndex);
                        model.setValue("dgdl_reduce", dgdlReduce, thisIndex);
//                        model.setValue("dgdl_chg1", chg1, thisIndex);
//                        model.setValue("dgdl_chg2", chg2, thisIndex);
//                        model.setValue("dgdl_chg3", chg3, thisIndex);
//                        model.setValue("dgdl_chg4", chg4, thisIndex);
//                        model.setValue("dgdl_chg5", chg5, thisIndex);
//                        model.setValue("dgdl_chg6", chg6, thisIndex);
//                        model.setValue("dgdl_chg7", chg7, thisIndex);
//                        model.setValue("dgdl_chg8", chg8, thisIndex);
//                        model.setValue("dgdl_chg9", chg9, thisIndex);
//                        model.setValue("dgdl_chg10", chg10, thisIndex);
//                        model.setValue("dgdl_chg11", chg11, thisIndex);
//                        model.setValue("dgdl_chg12", chg12, thisIndex);
//                        model.setValue("dgdl_prechg10", prechg10, thisIndex);
//                        model.setValue("dgdl_prechg11", prechg11, thisIndex);
//                        model.setValue("dgdl_prechg12", prechg12, thisIndex);
                        for (String monthField : monthFieldArr) {
                            if(!fieldList.contains(monthField)){
                                entryDataJson.put(monthField,"0");
                            }
                        }

//                        for (String field : fieldList) {
//                            if(field.contains("chg")){
//                                Integer chg = entryDataJson.getInteger(field);
//
//                                model.setValue(field, chg, thisIndex);
//                            }
//                        }
                        iterator.remove();
                    }

                }
                index++;
            }
        }
    }

    private String isHas(String key) {
        return "是".equals(key) ? "1" : "0";
    }

    private Map<String, Integer> joinStr(DynamicObjectCollection collection) {
        Map<String, Integer> map = new HashMap<>();
        int index = 0;
        for (DynamicObject data : collection) {
            StringBuilder sb = new StringBuilder();
            //末端组织
            DynamicObject adminOrg = data.getDynamicObject("dgdl_detail_adminorg");
            if (Objects.nonNull(adminOrg)) {
                sb.append(adminOrg.getString("number")).append("-");
            }
            //职位编码
            DynamicObject position = data.getDynamicObject("dgdl_stdposition");
            if (Objects.nonNull(position)) {
                sb.append(position.getString("number")).append("-");
            }
            //职级编码
            DynamicObject rank = data.getDynamicObject("dgdl_personrank");
            if (Objects.nonNull(rank)) {
                sb.append(rank.getString("name")).append("-");
            }
            //通道编码
            DynamicObject aisle = data.getDynamicObject("dgdl_post_aisle");
            if (Objects.nonNull(aisle)) {
                sb.append(aisle.getString("name")).append("-");
            }
            //岗位属性编码
            DynamicObject jobproperty = data.getDynamicObject("dgdl_jobproperty");
            if (Objects.nonNull(jobproperty)) {
                sb.append(jobproperty.getString("name")).append("-");
            }
            //用工关系类型编码
            DynamicObject type = data.getDynamicObject("dgdl_laborreltype");
            if (Objects.nonNull(type)) {
                sb.append(type.getString("name")).append("-");
            }
            //大雁
            String label1 = data.getString("dgdl_label1");
            if (StringUtils.isNotEmpty(label1)) {
                sb.append(label1).append("-");
            }
            //新四化
            String label2 = data.getString("dgdl_label2");
            if (StringUtils.isNotEmpty(label2)) {
                sb.append(label2).append("-");
            }
            //数字化
            String label3 = data.getString("dgdl_label3");
            if (StringUtils.isNotEmpty(label3)) {
                sb.append(label3).append("-");
            }
            //国际化
            String label4 = data.getString("dgdl_label4");
            if (StringUtils.isNotEmpty(label4)) {
                sb.append(label4).append("-");
            }
            //对外派驻
            String dispatch = data.getString("dgdl_dispatch");
            if (StringUtils.isNotEmpty(dispatch)) {
                sb.append(dispatch).append("-");
            }
            //工作地点编码
            DynamicObject workPlace = data.getDynamicObject("dgdl_workplace");
            if (Objects.nonNull(workPlace)) {
                sb.append(workPlace.getString("name")).append("-");
            }
            map.put(sb.toString(), index);
            index++;
            logger.info("分录第" + index + ",行数据为=" + sb);
        }
        return map;
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent evt) {
        String fieldName = evt.getFieldName();
        //实际人数超链接点击
        if ("dgdl_actual".equals(fieldName)) {
            //分录行
            int rowIndex = evt.getRowIndex();
            DynamicObject dgdlEntryObj = this.getModel().getEntryRowEntity("dgdl_entry", rowIndex);
            //弹出在岗人员名单
            FormShowParameter fsp = new FormShowParameter();
            fsp.setFormId("dgdl_plan_persons");
            //编制详情ID
            fsp.setCustomParam("detailId", this.getModel().getDataEntity().getPkValue());
            Long pkValue = (Long) dgdlEntryObj.getPkValue();
            if (Objects.nonNull(pkValue) && pkValue != 0) {
                //编制详情分录ID
                fsp.setCustomParam("entryId", dgdlEntryObj.getPkValue());
                fsp.getOpenStyle().setShowType(ShowType.Modal);
                this.getView().showForm(fsp);
            }
        }
    }

    private   List<String>  setFieldList(DynamicObject dgdlPlanObj) {
        List<String> fieldlist=new ArrayList<>();

        //隐藏明细页签分录月份字段
        if (dgdlPlanObj == null) {
            return fieldlist;
        }
        //所属年度

        Date dgdlYear = dgdlPlanObj.getDate("dgdl_year");
        int year = DateTimeCommon.getYear(dgdlYear);
        //分录字段重命名
        //开始月份
        Date dgdlStartmonth = dgdlPlanObj.getDate("dgdl_startmonth");
        LocalDate date1 = dgdlStartmonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        //结束月份
        Date dgdlEndmonth = dgdlPlanObj.getDate("dgdl_endmonth");
        LocalDate date2 = dgdlEndmonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long monthCount = DateTimeCommon.reduceMon(dgdlStartmonth, dgdlEndmonth);

        // 罗列年份和月份
        List<YearMonth> yearMonths = new ArrayList<>();
        YearMonth startYearMonth = YearMonth.of(date1.getYear(), date1.getMonth());
        YearMonth endYearMonth = YearMonth.of(date2.getYear(), date2.getMonth());

        YearMonth current = startYearMonth;
        while (!current.isAfter(endYearMonth)) {
            yearMonths.add(current);
            current = current.plusMonths(1);
        }

        // 打印结果
        for (YearMonth ym : yearMonths) {
            int monthValue = ym.getMonthValue();
            int ymyear = ym.getYear();
            if(ymyear==year){
                String fieldKey1 = "dgdl_chg" + monthValue;
                String fieldKey2 = "dgdl_avg" + monthValue;
                fieldlist.add(fieldKey1);
                fieldlist.add(fieldKey2);
            }else {
                String fieldKey1 = "dgdl_prechg" + monthValue;
                String fieldKey2 = "dgdl_preavg" + monthValue;
                fieldlist.add(fieldKey1);
                fieldlist.add(fieldKey2);
            }
        }



        return fieldlist;
    }
    private void setEnable(IFormView view, int i, boolean item) {
        view.setEnable(item, i, "dgdl_stdposition");
        view.setEnable(item, i, "dgdl_post_aisle");
        view.setEnable(item, i, "dgdl_jobproperty");
        view.setEnable(item, i, "dgdl_personrank");
        view.setEnable(item, i, "dgdl_dispatch");
        view.setEnable(item, i, "dgdl_laborreltype");
        view.setEnable(item, i, "dgdl_workplace");
        view.setEnable(item, i, "dgdl_label1");
        view.setEnable(item, i, "dgdl_label2");
        view.setEnable(item, i, "dgdl_label3");
        view.setEnable(item, i, "dgdl_label4");
        view.setEnable(item, i, "dgdl_prechg10");
        view.setEnable(item, i, "dgdl_prechg11");
        view.setEnable(item, i, "dgdl_prechg12");
        view.setEnable(item, i, "dgdl_chg1");
        view.setEnable(item, i, "dgdl_chg2");
        view.setEnable(item, i, "dgdl_chg3");
        view.setEnable(item, i, "dgdl_chg4");
        view.setEnable(item, i, "dgdl_chg5");
        view.setEnable(item, i, "dgdl_chg6");
        view.setEnable(item, i, "dgdl_chg7");
        view.setEnable(item, i, "dgdl_chg8");
        view.setEnable(item, i, "dgdl_chg9");
        view.setEnable(item, i, "dgdl_chg10");
        view.setEnable(item, i, "dgdl_chg11");
        view.setEnable(item, i, "dgdl_chg12");
        view.setEnable(item, i, "dgdl_add_str");
        view.setEnable(item, i, "dgdl_reduce_str");
    }

    @Override
    public void tabSelected(TabSelectEvent tabSelectEvent) {
        String tabKey = tabSelectEvent.getTabKey();

        this.getModel().setValue("dgdl_joblevel",false);
        this.getModel().setValue("dgdl_jobtag",false);
        this.getModel().setValue("dgdl_property",false);
        this.getModel().setValue("dgdl_hz_laborreltype",false);


    }
}
