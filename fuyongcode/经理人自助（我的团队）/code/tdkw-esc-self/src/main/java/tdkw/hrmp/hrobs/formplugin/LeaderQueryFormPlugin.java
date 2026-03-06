package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.exception.KDBizException;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.Label;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.report.ReportShowParameter;
import tdkw.hrmp.hrobs.common.app.api.AdminOrgChangeStatisticApi;
import tdkw.hrmp.hrobs.formplugin.report.SkipToThisMonthInReport;
import tdkw.hrmp.hrobs.formplugin.report.SkipToThisMonthOutReport;
import tdkw.hrmp.hrobs.common.app.result.CustomApiResultEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Map;


/**
 * @author xxx
 * @date： 2023/7/11
 * @description : 领导查询表单插件
 */
public class LeaderQueryFormPlugin extends AbstractFormPlugin {
    private static final Log LOGGER = LogFactory.getLog(LeaderQueryFormPlugin.class);
    /**
     * 本月入职报表标识
     */
    public static final String MONTHONBOARDINGRPT = "tdkw_monthonboardingrpt";
    /**
     * 本月离职报表标识
     */
    public static final String MONTHLEAVE = "tdkw_monthleave";

    /**
     * 本月异动报表标识·
     */
    public static final String CHANGES_THIS_MONTH = "tdkw_monthchange";
    /**
     * 组织变动明细查询单据标识
     */
    private static final String HOMS_ORGCHGRECORD = "homs_orgchgrecord";
    /**
     * 组织新增控件标识
     */
    private static final String ADD_COUNT = "tdkw_addcount";
    /**
     * 组织撤销控件标识
     */
    private static final String BACK_COUNT = "tdkw_backcount";
    /**
     * 组织变动控件标识
     */
    private static final String CHANGE_COUNT = "tdkw_changecount";
    /**
     * 本月入职控件标识
     */
    private static final String INDUCTION_COUNT = "tdkw_inductioncount";
    /**
     * 本月离职控件标识
     */
    private static final String DEPARTURE_COUNT = "tdkw_departurecount";
    /**
     * 本月退休控件标识
     */
    private static final String RETIRE_COUNT = "tdkw_retire";
    /**
     * 本月异动控件标识
     */
    private static final String MOVE_COUNT = "tdkw_movecount";


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        AdminOrgChangeStatisticApi adminOrgChangeStatisticApi = new AdminOrgChangeStatisticApi();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        LOGGER.info("组织startdata---"+dateFormat.format(new Date()));

        try {
            CustomApiResult<CustomApiResultEx> count = adminOrgChangeStatisticApi.count();
            Map data = (Map) count.getData().getData();
            setLabName(ADD_COUNT, data == null ? "0" : data.get("addCount").toString());
            setLabName(BACK_COUNT, data == null ? "0" : data.get("backCount").toString());
            setLabName(CHANGE_COUNT, data == null ? "0" : data.get("changeCount").toString());
        } catch (ParseException ex) {
            throw new KDBizException("获取数据异常");
        }
        LOGGER.info("组织enddata---"+dateFormat.format(new Date()));

        LOGGER.info("入职startdata---"+dateFormat.format(new Date()));
        SkipToThisMonthInReport skipToThisMonthInReport = new SkipToThisMonthInReport();
        String thisMonthIn = skipToThisMonthInReport.getThisMonthIn(this.getClass().getName());
        setLabName(INDUCTION_COUNT, thisMonthIn == null ? "0" : thisMonthIn);
        LOGGER.info("入职enddata---"+dateFormat.format(new Date()));

        LOGGER.info("离职startdata---"+dateFormat.format(new Date()));
        SkipToThisMonthOutReport skipToThisMonthOutReport = new SkipToThisMonthOutReport();
        String thisMonthOut = skipToThisMonthOutReport.getThisMonthOut(this.getClass().getName(),"dimission");
        setLabName(DEPARTURE_COUNT, thisMonthOut == null ? "0" : thisMonthOut);
        LOGGER.info("离职enddata---"+dateFormat.format(new Date()));

        LOGGER.info("退休startdata---"+dateFormat.format(new Date()));
        String retireMonthOut = skipToThisMonthOutReport.getThisMonthOut(this.getClass().getName(),"retire");
        setLabName(RETIRE_COUNT, retireMonthOut == null ? "0" : retireMonthOut);
        LOGGER.info("退休enddata---"+dateFormat.format(new Date()));

        LOGGER.info("异动startdata---"+dateFormat.format(new Date()));
//        ChangesThisMonthRptPlugin changesThisMonthRptPlugin = new ChangesThisMonthRptPlugin();
//        String changeSize = changesThisMonthRptPlugin.changeSize();
        // TODO 缺少字段 临时屏蔽处理
        String changeSize = "2";
        setLabName(MOVE_COUNT, changeSize == null ? "0" : changeSize);
        LOGGER.info("异动enddata---"+dateFormat.format(new Date()));
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners(ADD_COUNT, BACK_COUNT, CHANGE_COUNT, INDUCTION_COUNT, DEPARTURE_COUNT, MOVE_COUNT,RETIRE_COUNT);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        String key = ((Control) evt.getSource()).getKey();
        switch (key) {
            // 组织新增
            case ADD_COUNT:
                showList(new String[]{"1010"});
                break;
            // 组织撤销
            case BACK_COUNT:
                showList(new String[]{"1040"});
                break;
            // 组织更名
            case CHANGE_COUNT:
                showList(new String[]{"1020","1030"});
                break;
            // 本月入职
            case INDUCTION_COUNT:
                showReport(MONTHONBOARDINGRPT,null);
                break;
            // 本月离职
            case DEPARTURE_COUNT:
                showReport(MONTHLEAVE,"dimission");
                break;
            // 本月退休
            case RETIRE_COUNT:
                showReport(MONTHLEAVE,"retire");
                break;
            // 本月异动
            case MOVE_COUNT:
                showReport(CHANGES_THIS_MONTH,null);
                break;
            default:
                throw new KDBizException("未知处理类型");
        }
    }

    /**
     * 展示列表
     *
     * @param id 变动场景id
     */
    private void showList(String[] id) {
        ListShowParameter showParameter = new ListShowParameter();
        showParameter.setCustomParam("searchchangescene",id);
        showParameter.setSelectedEntity("subentryentity");
        showParameter.setBillFormId(HOMS_ORGCHGRECORD);
        showParameter.setHasRight(true);
        showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        this.getView().showForm(showParameter);
    }

    /**
     * 展示报表
     *
     * @param formId 报表标识
     */
    private void showReport(String formId , String diMissionType) {
        ReportShowParameter showParameter = new ReportShowParameter();
        showParameter.setFormId(formId);
        if (StringUtils.isNotEmpty(diMissionType)){
            showParameter.setCustomParam("diMissionType",diMissionType);
        }
        showParameter.setHasRight(true);
        showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        this.getView().showForm(showParameter);
    }

    /**
     * 设置控件名称
     *
     * @param property 控件标识
     * @param name     控件名称
     */
    private void setLabName(String property, String name) {
        Label label = this.getControl(property);
        label.setText(name);
    }
}