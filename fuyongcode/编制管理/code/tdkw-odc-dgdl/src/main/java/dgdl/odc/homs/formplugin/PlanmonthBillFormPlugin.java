package dgdl.odc.homs.formplugin;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.CellStyle;
import kd.bos.exception.KDBizException;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.tree.TreeFilterParameter;
import kd.bos.url.UrlService;
import kd.bos.util.StringUtils;
import kd.bos.workflow.engine.msg.info.MessageInfo;
import kd.hr.hbp.common.constants.org.TreeTemplateConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author 姚帅
 * @date:2023/1/22
 * @description: 月度编制管理表单插件
 */
public class PlanmonthBillFormPlugin extends AbstractFormPlugin {

    private static Log logger = LogFactory.getLog(PlanmonthBillFormPlugin.class);

    private static final String[] hideStr = new String[]{"dgdl_entry1", "dgdl_entry2", "dgdl_entry3", "dgdl_entry4", "dgdl_entry5", "dgdl_entry6", "dgdl_entry7", "dgdl_entry8", "dgdl_entry9", "dgdl_entry10", "dgdl_entry11", "dgdl_entry12"};

    private static final String entryFiled = "dgdl_bz_entry.dgdl_bz1," +
            "dgdl_bz_entry.dgdl_bz2," +
            "dgdl_bz_entry.dgdl_bz3," +
            "dgdl_bz_entry.dgdl_bz4," +
            "dgdl_bz_entry.dgdl_bz5," +
            "dgdl_bz_entry.dgdl_bz6," +
            "dgdl_bz_entry.dgdl_bz7," +
            "dgdl_bz_entry.dgdl_bz8," +
            "dgdl_bz_entry.dgdl_bz9," +
            "dgdl_bz_entry.dgdl_bz10," +
            "dgdl_bz_entry.dgdl_bz11," +
            "dgdl_bz_entry.dgdl_bz12";

    /**
     * 客户端地址
     */
    private String clientPath = "";//客户端地址

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
        this.addItemClickListeners("dgdl_advcontoolbarap");

        //角色编码
        List<String> roleNumbers = new ArrayList<>();
        roleNumbers.add("BZ00001");
        roleNumbers.add("BZ00002");
        roleNumbers.add("BZ00003");
        roleNumbers.add("BZ00004");
        roleNumbers.add("BZ00005");
        //催办人员过滤
        QFilter roleFilter = new QFilter("role.number", QCP.in, roleNumbers);
        DynamicObject[] userPermRoleDy = BusinessDataServiceHelper.load("perm_userrole", "id,user", new QFilter[]{roleFilter});
        //用户id
        List<Long> userIds = new ArrayList<>();
        for (DynamicObject userObj : userPermRoleDy) {
            userIds.add((Long) userObj.getDynamicObject("user").getPkValue());
        }
        BasedataEdit personplan = this.getView().getControl("dgdl_personplan");
        personplan.addBeforeF7SelectListener((listener) -> {
            ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
            QFilter personQfilter = new QFilter("id", QCP.in, userIds);
            showParam.getListFilterParameter().getQFilters().add(personQfilter);

        });

        IDataModel model = this.getModel();
        //计划
        DynamicObject dgdlPlanmonth = (DynamicObject) model.getValue("dgdl_planmonth");
        //组织
        DynamicObject org = dgdlPlanmonth.getDynamicObject("dgdl_org");
        BasedataEdit adminorg1 = this.getView().getControl("dgdl_bz_adminorg1");
        adminorg1.addBeforeF7SelectListener((listener) -> {
            ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
            QFilter personQfilter = new QFilter("org.id", QCP.equals, org.getPkValue());
            showParam.getListFilterParameter().getQFilters().add(personQfilter);

            TreeFilterParameter treeFilterParameter = new TreeFilterParameter();
            List<QFilter> list = new ArrayList<>();
            list.add(new QFilter("org.id", QCP.equals, org.getPkValue()));
            treeFilterParameter.setQFilters(list);
            showParam.setTreeFilterParameter(treeFilterParameter);
        });

        BasedataEdit adminorg2 = this.getView().getControl("dgdl_bz_adminorg2");
        adminorg2.addBeforeF7SelectListener((listener) -> {
            ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
            QFilter personQfilter = new QFilter("org.id", QCP.equals, org.getPkValue());
            showParam.getListFilterParameter().getQFilters().add(personQfilter);

            TreeFilterParameter treeFilterParameter = new TreeFilterParameter();
            List<QFilter> list = new ArrayList<>();
            list.add(new QFilter("org.id", QCP.equals, org.getPkValue()));
            treeFilterParameter.setQFilters(list);
            showParam.setTreeFilterParameter(treeFilterParameter);
        });

        BasedataEdit adminorg3 = this.getView().getControl("dgdl_bz_adminorg3");
        adminorg3.addBeforeF7SelectListener((listener) -> {
            ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
            QFilter personQfilter = new QFilter("org.id", QCP.equals, org.getPkValue());
            showParam.getListFilterParameter().getQFilters().add(personQfilter);
        });

        BasedataEdit adminorg4 = this.getView().getControl("dgdl_bz_adminorg4");
        adminorg4.addBeforeF7SelectListener((listener) -> {
            ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
            QFilter personQfilter = new QFilter("org.id", QCP.equals, org.getPkValue());
            showParam.getListFilterParameter().getQFilters().add(personQfilter);
        });

        BasedataEdit adminorg5 = this.getView().getControl("dgdl_bz_adminorg5");
        adminorg5.addBeforeF7SelectListener((listener) -> {
            ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
            QFilter personQfilter = new QFilter("org.id", QCP.equals, org.getPkValue());
            showParam.getListFilterParameter().getQFilters().add(personQfilter);
        });

        BasedataEdit adminorg6 = this.getView().getControl("dgdl_bz_adminorg6");
        adminorg6.addBeforeF7SelectListener((listener) -> {
            ListShowParameter showParam = (ListShowParameter) listener.getFormShowParameter();
            QFilter personQfilter = new QFilter("org.id", QCP.equals, org.getPkValue());
            showParam.getListFilterParameter().getQFilters().add(personQfilter);
        });
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        //改变字段
        String filed = e.getProperty().getName();
        //改变行下标
        int rowIndex = e.getChangeSet()[0].getRowIndex();
        IDataModel model = this.getModel();
        //存储值改变值,计算控编人数
        List<String> filedList = new ArrayList<>();
        filedList.add("dgdl_bz_controlediting");
        filedList.add("dgdl_bz1");
        filedList.add("dgdl_bz2");
        filedList.add("dgdl_bz3");
        filedList.add("dgdl_bz4");
        filedList.add("dgdl_bz5");
        filedList.add("dgdl_bz6");
        filedList.add("dgdl_bz7");
        filedList.add("dgdl_bz8");
        filedList.add("dgdl_bz9");
        filedList.add("dgdl_bz10");
        filedList.add("dgdl_bz11");
        filedList.add("dgdl_bz12");
        filedList.add("dgdl_personplan");

        if (filedList.contains(filed)) {
            switch (filed) {
                case "dgdl_bz_controlediting":
                    String controlediting = (String) model.getValue("dgdl_bz_controlediting", rowIndex);
                    if (StringUtils.isNotEmpty(controlediting)) {
                        if ("3".equals(controlediting)) {
                            this.getView().setVisible(true, "dgdl_bz_way", "dgdl_bz_percent");
                        } else {
                            //分录中没有弹性控编,则隐藏弹性方式
                            DynamicObjectCollection entry = model.getEntryEntity("dgdl_bz_entry");
                            int size = 0;
                            for (int i = 0; i < entry.size(); i++) {
                                String entryType = (String) model.getValue("dgdl_bz_controlediting", i);
                                if (!"3".equals(entryType)) {
                                    size++;
                                }
                            }
                            if (size == entry.size()) {
                                this.getView().setVisible(false, "dgdl_bz_way", "dgdl_bz_percent");
                            }
                        }
                    }
                    break;
                case "dgdl_personplan":
                    //催办人员
                    DynamicObjectCollection personplan = (DynamicObjectCollection) model.getValue("dgdl_personplan");
                    List<Long> userList = new ArrayList<>();
                    for (DynamicObject person : personplan) {
                        userList.add((Long) person.getDynamicObject("fbasedataid").getPkValue());
                    }
                    userList.add(1737148357501250560L);
                    //处理时间
                    SimpleDateFormat yearSim = new SimpleDateFormat("yyyy");
                    //计划
                    DynamicObject planmonth = (DynamicObject) model.getValue("dgdl_planmonth");
                    Date year = planmonth.getDate("dgdl_year");
                    //编制单位
                    String sonName = planmonth.getDynamicObject("dgdl_org").getString("name");
                    //所属年份
                    String yearStr = yearSim.format(year);
                    //获取月度详情
                    QFilter planQFilter = new QFilter("dgdl_planmonth.id", QCP.equals, planmonth.getPkValue());
                    DynamicObject planBillObj = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bill", "id", planQFilter.toArray());
                    //标题
                    String content = "请尽快维护" + yearStr + "年" + sonName + "集团编制信息,如已维护,请忽略此消息";
                    String url = getUrl((Long) planBillObj.getPkValue(), "dgdl_planmonth_bill");
                    for (Long userId : userList) {
                        sendMessage("月度编制计划消息通知", content, Collections.singletonList(userId), url);
                    }
                    break;
                default:
                    //获取分录
                    EntryGrid grid = this.getView().getControl("dgdl_bz_entry");
                    String month = null;
                    //获取月份
                    if (filed.length() == 8) {
                        month = filed.substring(filed.length() - 1);
                    } else {
                        month = filed.substring(filed.length() - 2);
                    }
                    //控编字段
                    String kb = "dgdl_kb" + month;
                    //基准数
                    String jz = "dgdl_jz" + month;
                    //编制数
                    int bzCount = (int) model.getValue(filed, rowIndex);
                    //基准数
                    int jzCount = (int) model.getValue(jz, rowIndex);
                    //控编方式
                    String controled = (String) model.getValue("dgdl_bz_controlediting", rowIndex);
                    //额度
                    int percent = (int) model.getValue("dgdl_bz_percent", rowIndex);
                    if (StringUtils.isNotEmpty(controled) && "3".equals(controled)) {
                        //弹性方式
                        String way = (String) model.getValue("dgdl_bz_way", rowIndex);
                        if ("1".equals(way)) {
                            //计算额度
                            BigDecimal divide = new BigDecimal(percent).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                            //计算增量
                            BigDecimal multiply = divide.multiply(new BigDecimal(bzCount)).setScale(0, RoundingMode.UP);
                            model.setValue(kb, multiply.add(new BigDecimal(bzCount)), rowIndex);
                        } else {
                            bzCount = bzCount + percent;
                            model.setValue(kb, bzCount, rowIndex);
                        }
                    } else {
                        model.setValue(kb, bzCount, rowIndex);
                    }

                    //实时变色
                    CellStyle cs = new CellStyle();
                    ArrayList<CellStyle> csList = new ArrayList<>();
                    if (bzCount > jzCount) {
                        cs.setForeColor("#ff0000");

                    } else {
                        cs.setForeColor("#272727");
                    }
                    //列标识
                    cs.setFieldKey(filed);
                    //行索引
                    cs.setRow(rowIndex);
                    csList.add(cs);
                    grid.setCellStyle(csList);
                    break;
            }
        }

    }


    /**
     * 保存后给页面字体渲染颜色
     *
     * @param eventArgs
     */
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs eventArgs) {
        super.afterDoOperation(eventArgs);
        String operateKey = eventArgs.getOperateKey();
        if ("save".equals(operateKey)) {
            List<Object> successPkIds = eventArgs.getOperationResult().getSuccessPkIds();
            if (!successPkIds.isEmpty()) {
                IDataModel model = this.getModel();
                //格式化日期
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                //月度计划
                DynamicObject planmonth = (DynamicObject) model.getValue("dgdl_planmonth");
                //开始日期
                Date startmonth = planmonth.getDate("dgdl_startmonth");
                //结束日期
                Date endmonth = planmonth.getDate("dgdl_endmonth");
                int startStr = Integer.parseInt(dateFormat.format(startmonth).split("-")[1]);
                int endStr = Integer.parseInt(dateFormat.format(endmonth).split("-")[1]);
                this.setStyle(startStr, endStr);
            }
        }
    }


    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        IDataModel model = this.getModel();
        FormOperate operate = (FormOperate) args.getSource();
        String operateKey = operate.getOperateKey();
        EntryGrid dgdlKbEntry = this.getControl("dgdl_bz_entry");
        int[] selectRows = dgdlKbEntry.getSelectRows();
        //获取分录
        if ("press".equals(operateKey)) {
            BasedataEdit control = this.getView().getControl("dgdl_personplan");
            control.click();
        } else if ("newentry".equals(operateKey)) {
            //控编分录增行
            this.getView().invokeOperation("kbnewEntry");
        } else if ("deleteentry".equals(operateKey)) {
            model.deleteEntryRows("dgdl_kb_entry", selectRows);
        } else if ("save".equals(operateKey)) {
            //保存
            checkSave();
        }
    }


    @Override
    public void afterBindData(EventObject e) {
        logger.info("PlanmonthBillFormPlugin进入afterBindData");
        super.afterBindData(e);
        IDataModel model = this.getModel();
        this.getView().setVisible(false, hideStr);
        //格式化日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //月度计划
        DynamicObject planmonth = (DynamicObject) model.getValue("dgdl_planmonth");
        //开始日期
        Date startmonth = planmonth.getDate("dgdl_startmonth");
        //结束日期
        Date endmonth = planmonth.getDate("dgdl_endmonth");
        //所属年度
        model.setValue("dgdl_year", planmonth.getDate("dgdl_year"));
        int startStr = Integer.parseInt(dateFormat.format(startmonth).split("-")[1]);
        int endStr = Integer.parseInt(dateFormat.format(endmonth).split("-")[1]);
        String entry = "dgdl_entry";
        String kb = "dgdl_kb";
        for (int i = startStr; i <= endStr; i++) {
            this.getView().setVisible(true, entry + i);
            this.getView().setVisible(true, kb + i);
        }

        //渲染颜色
        this.setStyle(startStr, endStr);
        //分录弹性方式/人数显示隐藏
        String dgdlWay = planmonth.getString("dgdl_way");
        if (StringUtils.isNotEmpty(dgdlWay)) {
            this.getView().setVisible(true, "dgdl_bz_way", "dgdl_bz_percent");
            this.getView().setVisible(true, "dgdl_kb_way", "dgdl_kb_percent");
        } else {
            this.getView().setVisible(false, "dgdl_bz_way", "dgdl_bz_percent");
            this.getView().setVisible(false, "dgdl_kb_way", "dgdl_kb_percent");
        }

        //行政组织列隐藏
        List<String> list = new ArrayList<>();
        list.add("dgdl_bz_adminorg1");
        list.add("dgdl_bz_adminorg2");
        list.add("dgdl_bz_adminorg3");
        list.add("dgdl_bz_adminorg4");
        list.add("dgdl_bz_adminorg5");
        list.add("dgdl_bz_adminorg6");
        list.add("dgdl_kb_adminorg1");
        list.add("dgdl_kb_adminorg2");
        list.add("dgdl_kb_adminorg3");
        list.add("dgdl_kb_adminorg4");
        list.add("dgdl_kb_adminorg5");
        list.add("dgdl_kb_adminorg6");
        List<String> showList = new ArrayList<>();
        //组织层级
        String hierarchy = planmonth.getString("dgdl_orghierarchy");
        int intHierarchy = Integer.parseInt(hierarchy);
        String bzOrg = "dgdl_bz_adminorg";
        String kbOrg = "dgdl_kb_adminorg";
        for (int i = 1; i <= intHierarchy; i++) {
            showList.add(bzOrg + i);
            showList.add(kbOrg + i);
        }
        list.removeAll(showList);
        for (String str : list) {
            this.getView().setVisible(false, str);
        }

        //业务单元
        DynamicObject orgObj = planmonth.getDynamicObject("dgdl_org");
        String sonNumber = orgObj.getString("number");
        //加载编制页签
        ListShowParameter bzLsp = new ListShowParameter();
        bzLsp.setBillFormId("dgdl_planmonth_bz");
        //打开的位置
        bzLsp.getOpenStyle().setTargetKey("dgdl_tab_bz");
        bzLsp.getOpenStyle().setShowType(ShowType.InContainer);
        //计划
        bzLsp.setCustomParam("planId", planmonth.getPkValue());
        //层级
        bzLsp.setCustomParam("dgdl_orghierarchy", hierarchy);
        //用工关系类型
        String labeldimension = planmonth.getString("dgdl_labeldimension");
        bzLsp.setCustomParam("labeldimension", labeldimension);

        Set<Long> adminList = getAdminByOrg(sonNumber, hierarchy);
        adminList.add(100000L);
        QFilter bzQFilter = new QFilter("dgdl_planmonth_bill.dgdl_planmonth.id", QCP.in, planmonth.getPkValue())
                .and("dgdl_is_org", QCP.equals, "02");
        //编制信息管理详情
        DynamicObject[] bzObjs = BusinessDataServiceHelper.load("dgdl_planmonth_bz", "id,dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6", bzQFilter.toArray());
        //非当前BU下的行政组织
        Set<Long> notThistOrgList = new HashSet<>();
        String filed = "dgdl_bz_adminorg";
        for (DynamicObject obj : bzObjs) {
            for (int i = 1; i < 7; i++) {
                DynamicObject orgAdmin = obj.getDynamicObject(filed + i);
                if (Objects.nonNull(orgAdmin)) {
                    DynamicObject parent = orgAdmin.getDynamicObject("parent");
                    if (Objects.nonNull(parent)){
                        notThistOrgList.add((Long) parent.getPkValue());
                    }
                    notThistOrgList.add((Long) orgAdmin.getPkValue());
                }
            }
        }
        logger.info("PlanmonthBillFormPlugin获取不属于当前组织BU的行政组织" + notThistOrgList);
        if (!notThistOrgList.isEmpty()) {
            //无效组织单独处理
            if (notThistOrgList.contains(1774822336449484800L)) {
                notThistOrgList.add(1774542341323883520L);
            }
            adminList.addAll(notThistOrgList);
        }
        //过滤左树
        QFilter treeQfilte = new QFilter("adminorg.id", QCP.in, adminList);
        bzLsp.setCustomParam(TreeTemplateConstants.BIZ_QFILTER_KEY, treeQfilte.toSerializedString());
        this.getView().showForm(bzLsp);
        model.setDataChanged(false);
    }

    private void setStyle(int startStr, int endStr) {
        IDataModel model = this.getModel();
        //渲染颜色
        DynamicObjectCollection bzCollection = model.getEntryEntity("dgdl_bz_entry");
        EntryGrid grid = this.getView().getControl("dgdl_bz_entry");
        //月度计划
        DynamicObject planmonth = (DynamicObject) model.getValue("dgdl_planmonth");
        //层级
        String hierarchy = planmonth.getString("dgdl_orghierarchy");
        int intHierarchy = Integer.parseInt(hierarchy);
        ArrayList<CellStyle> csList = new ArrayList<>();
        //基准数
        String jz1 = "dgdl_jz";
        //编制数
        String bz1 = "dgdl_bz";
        for (int i = 0; i < bzCollection.size(); i++) {
            DynamicObject bzObj = bzCollection.get(i);
            for (int j = startStr; j <= endStr; j++) {
                //基准数
                int jzCount = bzObj.getInt(jz1 + j);
                //编制数
                int bzCount = bzObj.getInt(bz1 + j);
                CellStyle cs = new CellStyle();
                if (jzCount < bzCount) {
                    //字体颜色
                    cs.setForeColor("#ff0000");
                } else {
                    //字体颜色
                    cs.setForeColor("#272727");
                }
                //列标识
                cs.setFieldKey(bz1 + j);
                //行索引
                cs.setRow(i);
                csList.add(cs);
            }
        }
        grid.setCellStyle(csList);
    }

    private void checkSave() {
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        DynamicObject dgdlPlanmonth = dataEntity.getDynamicObject("dgdl_planmonth");
        List<String> dgdlLabeldimensions = Arrays.asList(dgdlPlanmonth.getString("dgdl_labeldimension").split(","));//标签维度
        DynamicObjectCollection dgdlBzEntryColl = dataEntity.getDynamicObjectCollection("dgdl_bz_entry");
        //校验必填
        for (int i = 0; i < dgdlBzEntryColl.size(); i++) {
            int row = i + 1;
            DynamicObject dgdlBzEntry = dgdlBzEntryColl.get(i);
            if (dgdlLabeldimensions.contains("1") && StringUtils.isEmpty(dgdlBzEntry.getString("dgdl_bz_laborreltype"))) {
                throw new KDBizException("编制数据第" + row + "行用工关系类型必填");
            }
            if (dgdlLabeldimensions.contains("2") && StringUtils.isEmpty(dgdlBzEntry.getString("dgdl_bz_jobproperty"))) {
                throw new KDBizException("编制数据第" + row + "行岗位属性必填");
            }
            String dgdlBzControlediting = dgdlBzEntry.getString("dgdl_bz_controlediting");
            if (dgdlBzControlediting.equals("3")) {
                if (StringUtils.isEmpty(dgdlBzEntry.getString("dgdl_bz_way"))) {
                    throw new KDBizException("编制数据第" + row + "行弹性方式必填");
                }
                if (dgdlBzEntry.getBigDecimal("dgdl_bz_percent").doubleValue() == 0D) {
                    throw new KDBizException("编制数据第" + row + "行弹性额度必填");
                }
            }
        }
    }

    /**
     * 发送消息
     *
     * @param title    标题
     * @param content  内容
     * @param userList 用户列表
     */
    public static void sendMessage(String title, String content, List<Long> userList, String url) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setType(MessageInfo.TYPE_MESSAGE);
        messageInfo.setTitle(title);
        messageInfo.setTitle(content);
        messageInfo.setUserIds(userList);
        messageInfo.setContentUrl(url);
        long l = MessageCenterServiceHelper.sendMessage(messageInfo);
        logger.info("消息id" + l);
    }


    private String getUrl(Long successId, String frombill) {
        RequestContext ctx = RequestContext.get();
        //拿到完整的客户端网址
        this.clientPath = UrlService.getDomainContextUrlByTenantCode(ctx.getTenantCode());
        StringBuilder contentUrl = new StringBuilder(clientPath);
        //pc端跳转单据路径
        contentUrl.append("?formId=").append(frombill).append("&app=dgdl_homs_ext").append("&pkId=").append(successId);
        logger.info("PlanmonthBillFormPlugin:跳转地址为 " + contentUrl);
        return contentUrl.toString();
    }


    private Set<Long> getAdminByOrg(String orgNumber, String hierarchy) {
        Set<Long> adminList = new HashSet<>();
        logger.info("PlanmonthBillFormPlugin所需生成的组织层级" + hierarchy + ",组织编码=" + orgNumber);
        //转换层级
        int intOrg = Integer.parseInt(hierarchy);
        //控制层级
        List<String> orghierarchyList = new ArrayList<>();
        for (int i = 1; i <= intOrg + 1; i++) {
            orghierarchyList.add("0" + i);
        }
        orghierarchyList.add("99");
        //获取当前子集团下所选层级的行政组织
        QFilter orgQFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("org.number", QCP.equals, orgNumber)
                .and("enable", QCP.equals, "1")
                .and("adminorglayer.number", QCP.in, orghierarchyList)
                .and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] orgObjs = BusinessDataServiceHelper.load("haos_adminorghr", "id", orgQFilter.toArray());
        for (DynamicObject orgObj : orgObjs) {
            adminList.add((Long) orgObj.getPkValue());
        }
        logger.info("PlanmonthBillFormPlugin行政组织数量=" + adminList);
        return adminList;
    }

}
