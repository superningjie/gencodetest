package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.container.Container;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.util.HRMapUtils;
import kd.sdk.wtc.wtp.business.quota.QuotaQueryParam;
import kd.sdk.wtc.wtp.business.quota.WTPQuotaHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import tdkw.hrmp.hrobs.common.business.GetQTDetailService;
import tdkw.hrmp.hrobs.common.business.GetQTDetailServiceImpl;
import tdkw.hrmp.hrobs.common.hrobs.pojo.AttPeriodItemStat;
import tdkw.hrmp.hrobs.common.hrobs.pojo.PerAttPeriod;
import tdkw.hrmp.hrobs.common.hrobs.util.AttendanceUtil;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.DynamicObjectUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author xxx
 * @Date 2023/5/26 15:34
 * @Description PC端 当月出勤卡片 表单插件
 * @Change 需求变更 https://www.kdocs.cn/l/chxbmD4Vzaw0
 * @Demander xxx
 * @Document 员工自助门户与个人卡片-XXX集团需求规格说明书_V2.0(4)
 * @Basedata tdkw_attendancecalendar
 * @Version 1.0
 **/
public class AttendanceFormPlugin extends AbstractFormPlugin implements ClickListener {
    private static final Log LOGGER = LogFactory.getLog(AttendanceFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_flexpanelap2", "tdkw_flex1", "tdkw_flex2");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Container source = (Container) evt.getSource();
        String key = source.getKey();
        String startDate = this.getPageCache().get("startDate");
        String endDate = this.getPageCache().get("endDate");
        switch (key) {
            case "tdkw_flexpanelap2":
                // 我的年假
                FormShowParameter param = new FormShowParameter();
                param.setFormId("tdkw_hrobs_pc_annualleave");
                param.getOpenStyle().setShowType(ShowType.Modal);
                this.getView().showForm(param);
                break;
            case "tdkw_flex1":
                // 智慧考勤-异常记录
                ListShowParameter listShowParameter = new ListShowParameter();
                listShowParameter.setFormId("bos_list");
                // TODO 缺少tdkw_wtte_exrecord_inh 临时处理 用标品 wtte_exrecord
//                listShowParameter.setBillFormId("tdkw_wtte_exrecord_inh");
                listShowParameter.setBillFormId("wtte_exrecord");

                listShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                listShowParameter.setCustomParam("attendance", "attendance");
                if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
                    LOGGER.info("获取到异常记录日期：" + startDate + "~" + endDate);
                    listShowParameter.setCustomParam("startDate", startDate);
                    listShowParameter.setCustomParam("endDate", endDate);
                }
//                listShowParameter.setHasRight(true);
                this.getView().showForm(listShowParameter);
                break;
            case "tdkw_flex2":
                // TODO 缺少出差申请单 屏蔽处理 tdkw_reqtripe
                // 出差申请
                /*
                ListShowParameter listShowParameter2 = new ListShowParameter();
                listShowParameter2.setFormId("bos_list");
                listShowParameter2.setBillFormId("tdkw_reqtripe");
                listShowParameter2.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                listShowParameter2.setCustomParam("attendance", "attendance");
                if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
                    LOGGER.info("获取到异常记录日期：" + startDate + "~" + endDate);
                    listShowParameter2.setCustomParam("startDate", startDate);
                    listShowParameter2.setCustomParam("endDate", endDate);
                }
//                listShowParameter2.setHasRight(true);
                this.getView().showForm(listShowParameter2);
                */
                break;
            default:
                break;
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        this.getAttendanceInfo();
        this.getWTCAnnualLeaveInfo();
    }

    /**
     * @author xxx
     * @Description 调用接口，统计当月出勤信息
     * @Date 2023/5/29 14:50
     */
    public void getAttendanceInfo() {

        // 获取考勤人ID
        long attPersonId = AttendanceUtil.getAttPersonIdOfCurrentLoginUser();
        LOGGER.info("考勤人员id为:" + attPersonId);

        // 考勤人员ID可能获取不到，比如当前人员未在考勤系统创建档案
        LOGGER.info("attPersonId:" + attPersonId);
        if (attPersonId == 0L) {
//            getView().showErrorNotification(ResManager.loadKDString("未找到考勤人员，请联系管理员。", "AttendanceFormPlugin_0", "tdkw-esc-hrobs-formplugin"));
            return;
        }

        Date now = new Date();

        // 获取当前考勤档案信息，获取考勤档案需要指定日期，因为档案是时序性对象，按当前日期获取
        Map<String, String> attFile = AttendanceUtil.getAttFile(attPersonId, now);

        // 当前人员在指定日期可能没有考勤档案，需要校验提示
        LOGGER.info("attFile:" + attFile);
        if (HRMapUtils.isEmpty(attFile)) {
//            getView().showErrorNotification(ResManager.loadKDString("您无有效的考勤档案，请联系管理员。", "AttendanceFormPlugin_1", "tdkw-esc-hrobs-formplugin"));
            return;
        }

        // 获取假勤门户方案id
        long schemeId = AttendanceUtil.getSelfServiceSchemeId(attFile);

        // 可能找不到适用的门户方案，需要校验提示
        LOGGER.info("schemeId:" + schemeId);
        if (schemeId == 0L) {
//            getView().showErrorNotification(ResManager.loadKDString("未匹配到假勤门户方案，请联系管理员。", "AttendanceFormPlugin_2", "tdkw-esc-hrobs-formplugin"));
            return;
        }

        // 获取当前人员考勤期间,先获取全量人员考勤期间，再获取当前的
        PerAttPeriod currentAttPeriod = AttendanceUtil.getPerAttPeriod(attPersonId, now);

        // 可能找不到当前人员的考勤期间，需要校验提示
        LOGGER.info("currentAttPeriod:" + currentAttPeriod);
        if (currentAttPeriod == null) {
//            getView().showErrorNotification(ResManager.loadKDString("找不到当前人员考勤期间，请联系管理员。", "AttendanceFormPlugin_3", "tdkw-esc-hrobs-formplugin"));
            return;
        }

        // 获取期间汇总数据源相关配置和统计值

        List<AttPeriodItemStat> attPeriodItemStats = AttendanceUtil.getAttPeriodItemStat(attPersonId, "A", currentAttPeriod.getId(), schemeId, 2);
        LOGGER.info("attPeriodItemStats:" + attPeriodItemStats);
        IDataModel model = this.getModel();
        IFormView view = this.getView();
        int size = attPeriodItemStats.size();
        int newNumber = getNewNumber(attPersonId, now);

        if (size == 0) {
            view.setVisible(false, "tdkw_flex1", "tdkw_flex2");
//            getView().showErrorNotification(ResManager.loadKDString("当前人员未配置默认期间汇总组合，请联系管理员。", "AttendanceFormPlugin_4", "tdkw-esc-hrobs-formplugin"));
            return;
        } else if (size == 1) {
            view.setVisible(true, "tdkw_flex1");
            view.setVisible(false, "tdkw_flex2");
            model.setValue("tdkw_data1", newNumber);
            //  model.setValue("tdkw_data1", attPeriodItemStats.get(0).getAttItemValue());
            model.setValue("tdkw_text1", "本月" + attPeriodItemStats.get(0).getName());
            LOGGER.info("newNumber:" + newNumber);
            LOGGER.info("oldNumber:" + attPeriodItemStats.get(0).getAttItemValue());
        } else {
            view.setVisible(true, "tdkw_flex1", "tdkw_flex2");
            model.setValue("tdkw_data1", newNumber);
            // model.setValue("tdkw_data1", attPeriodItemStats.get(0).getAttItemValue());
            model.setValue("tdkw_text1", "本月" + attPeriodItemStats.get(0).getName());
            model.setValue("tdkw_data2", attPeriodItemStats.get(1).getAttItemValue());
            model.setValue("tdkw_text2", attPeriodItemStats.get(1).getName());
            LOGGER.info("newNumber:" + newNumber);
            LOGGER.info("oldNumber:" + attPeriodItemStats.get(0).getAttItemValue());
        }

        view.updateView("tdkw_flex1");
        view.updateView("tdkw_flex2");

    }

    private static Integer getNewNumber(long attPersonId, Date now) {
        // 本人
        QFilter qFilter = new QFilter("personid", QCP.equals, attPersonId);
        qFilter.and("recorddate", QCP.less_equals, now);
        // 获取月初
        Date start = getDate(now);
        // 本月
        qFilter.and("recorddate", QCP.large_equals, start);
        // 异常类型
        qFilter.and("exattributeid.number", QCP.in, new String[]{"1010_S", "1020_S", "1030_S", "1040_S"});
        // 数据类别 单独处理 只取有结果的 详见 kd.wtc.wtte.formplugin.web.ex.ExRecordList 的datatype逻辑
        qFilter.and("attitemvid", QCP.large_than, 0L);
        DynamicObject[] load = BusinessDataServiceHelper.load("wtte_exrecord", "id", qFilter.toArray());
        if (null == load) {
            return 0;
        } else {
            return load.length;
        }
    }

    @NotNull
    private static Date getDate(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date start = calendar.getTime();
        return start;
    }

    /**
     * @author xxx
     * @Description 调用接口，获取年假信息
     * @Date 2023/5/29 11:14
     */
    public void getAnnualLeaveInfo() {
        QuotaQueryParam queryParam = new QuotaQueryParam();

        long attPersonId = AttendanceUtil.getAttPersonIdOfCurrentLoginUser();
        if (attPersonId == 0L) {
            return;
        }
        Date now = new Date();

        // 人员档案
        Map<String, String> attFile = AttendanceUtil.getAttFile(attPersonId, now);
        if (HRMapUtils.isEmpty(attFile)) {
            return;
        }

        // 人员档案BOID，必填，否则返回空集合
        queryParam.setAttFileBoId(Long.parseLong(attFile.get("boid")));

        List<Long> qoutaTypeIdList = new ArrayList<>();
        // 定额类型-年假
        DynamicObject wtp_qttype = DynamicObjectUtils.findDynamicObjectByNumber("wtp_qttype", "1020_S");
        qoutaTypeIdList.add((Long) wtp_qttype.getPkValue());

        // 需要查询的定额类型，为 null或者空集合时将查询该人所有类型的额度信息
        queryParam.setQuotaTypeIdList(qoutaTypeIdList);

        /**
         * 时间范围过滤模式：
         * 0(仅使用范围过滤),
         * 1(仅生成范围过滤),
         * 2(使用范围和生成范围同时过滤),
         * 3(使用范围或生成范围过滤),
         * 传入其他则返回空结果集
         */
        queryParam.setRangQueryType(0);

        /**
         * 时间范围过滤的开始时间，可为 null，为 null 时将直接使用当前时间查询且不考虑 endDate 的值。
         * 当 startDate 和 endDate 任一一个为 null 时，将直接认定为使用当前系统日期查询。
         * 当 startDate 和 endDate 均不为 null 时，要求 startDate 小于等于 endDate，否则返回空结果。
         */
        queryParam.setStartDate(DateTimeUtils.getBeginDayOfYear());

        /**
         * 时间范围过滤的结束时间，可为 null，为 null 时将直接使用当前时间查询且不考虑 startDate 的值。
         * 当 startDate 和 endDate 任一一个为 null 时，将直接认定为使用当前系统日期查询。
         * 当 startDate 和 endDate 均不为 null 时，要求 startDate 小于等于 endDate，否则返回空结果。
         */
//        queryParam.setEndDate(DateTimeUtils.getEndDayOfYear());
        //时间结束范围到今天
        queryParam.setEndDate(new Date());

        List<DynamicObject> objects = WTPQuotaHelper.queryQuota(queryParam);
        if (objects.size() == 0) {
            return;
        }

        // 可用天数
        BigDecimal usableValue = BigDecimal.ZERO;
        for (DynamicObject dyn : objects) {
            usableValue = usableValue.add(dyn.getBigDecimal("usablevalue"));
        }

        IDataModel model = this.getModel();
        IFormView view = this.getView();
        model.setValue("tdkw_usablevalue", usableValue);
        view.updateView("tdkw_usablevalue");
    }

    /**
     * @author xxx
     * @Description 调用接口，获取年假信息
     * @Date 2024/1/31 15:38
     */
    public void getWTCAnnualLeaveInfo() {
        // 可用天数
        BigDecimal usableValue = BigDecimal.ZERO;
        //获取当前登录人员工号
        QFilter qFilter = new QFilter("id", QCP.in, UserServiceHelper.getCurrentUserId())
                .and("enable", QCP.equals, "1")
                .and("status", QCP.equals, "C");
        DynamicObject query = QueryServiceHelper.queryOne("bos_user", "id,number", new QFilter[]{qFilter});
        String number = query.getString("number");
        Map<String, BigDecimal> result = null;
        try {
            /*Object maps = DispatchServiceHelper.invokeService("tdkw.wtc.trip.servicehelper", "tdkw_trip",
                    "GetQTDetailService", "getQtDetail", new Object[]{Arrays.asList(number), null});
            map = (Map<String, BigDecimal>) ((HashMap) maps).get(number);
            LOGGER.info("年假接口调用返回值："+map.toString());*/
            GetQTDetailService service = new GetQTDetailServiceImpl();
            Map<String, Map<String, BigDecimal>> qtDetail = service.getQtDetail(Arrays.asList(number), null);
            result = qtDetail.get(number);
        } catch (Exception e) {
            LOGGER.info("年假接口调用:" + e.getMessage());
        }
        if (Objects.nonNull(result)) {
            if (!result.isEmpty()) {
                usableValue = result.get("availableDays");
            }
        }
        IDataModel model = this.getModel();
        IFormView view = this.getView();
        model.setValue("tdkw_usablevalue", usableValue);
        view.updateView("tdkw_usablevalue");
    }
}