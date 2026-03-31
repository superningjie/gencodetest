package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.IFormView;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.util.HRMapUtils;
import kd.sdk.wtc.wtp.business.quota.QuotaQueryParam;
import kd.sdk.wtc.wtp.business.quota.WTPQuotaHelper;
import tdkw.hrmp.hrobs.formplugin.util.AttendanceUtil;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;
import tdkw.hrmp.hrobs.formplugin.util.DynamicObjectUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author xxx
 * @Date 2023/5/29 11:13
 * @Description PC端 我的年假卡片 表单插件
 * @Demander xxx
 * @Document 员工自助门户与个人卡片-XXX集团需求规格说明书_V2.0(4)
 * @Basedata tdkw_hrobs_pc_annualleave
 * @Version 1.0
 **/
public class AnnualLeaveFormPlugin extends AbstractFormPlugin {
    private static final Log LOGGER = LogFactory.getLog(AnnualLeaveFormPlugin.class);

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.getWTCAnnualLeaveInfo();
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
//            getView().showErrorNotification(ResManager.loadKDString("未找到考勤人员，请联系管理员。", "AnnualLeaveFormPlugin_0", "tdkw-esc-hrobs-formplugin"));
            return;
        }
        Date now = new Date();

        // 人员档案
        Map<String, String> attFile = AttendanceUtil.getAttFile(attPersonId, now);
        if (HRMapUtils.isEmpty(attFile)) {
//            getView().showErrorNotification(ResManager.loadKDString("您无有效的考勤档案，请联系管理员。", "AnnualLeaveFormPlugin_1", "tdkw-esc-hrobs-formplugin"));
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
//            getView().showErrorNotification(ResManager.loadKDString("未匹配到系统生成的定额明细，请联系管理员。", "AnnualLeaveFormPlugin_2", "tdkw-esc-hrobs-formplugin"));
            return;
        }
//        // 只取系统生成的定额明细
//        List<DynamicObject> finalDatas = objects.stream().filter(i -> i.get("source").equals("DT-000")).collect(Collectors.toList());
//        if (finalDatas.size() == 0) {
//            getView().showErrorNotification(ResManager.loadKDString("未匹配到系统生成的定额明细，请联系管理员。", "AnnualLeaveFormPlugin_2", "tdkw-esc-hrobs-formplugin"));
//            return;
//        }
//
//        DynamicObject object = finalDatas.get(0);
//        // 本期享有
//        BigDecimal ownvalue = object.getBigDecimal("ownvalue");
//        // 上期结余
//        BigDecimal cdedvalue = object.getBigDecimal("cdedvalue");
//        // 可用天数
//        BigDecimal usablevalue = object.getBigDecimal("usablevalue");
//        // 冻结天数
//        BigDecimal freezevalue = object.getBigDecimal("freezevalue");
//        // 已休天数
//        BigDecimal usedvalue = object.getBigDecimal("usedvalue");
//        // 调整时长 = 可用天数+已休天数+冻结天数-上期结余-本期享有
//        BigDecimal adjustDuration = usablevalue.add(usedvalue).add(freezevalue).subtract(cdedvalue).subtract(ownvalue);

        // 上期结余
        BigDecimal prePeriodValue = BigDecimal.ZERO;
        // 调整时长
        BigDecimal adjustValue = BigDecimal.ZERO;
        // 本期享有
        BigDecimal currentPeriodValue = BigDecimal.ZERO;
        // 已休天数
        BigDecimal usedValue = BigDecimal.ZERO;
        // 冻结天数
        BigDecimal frozenValue = BigDecimal.ZERO;
        // 可用天数
        BigDecimal usableValue = BigDecimal.ZERO;
        for (DynamicObject dyn : objects) {
            final String source = dyn.getString("source");
            final BigDecimal ownvalue = dyn.getBigDecimal("ownvalue");
            if ("DT-001".equals(source)) {
                prePeriodValue = prePeriodValue.add(ownvalue);
            } else if ("DT-002".equals(source)) {
                adjustValue = adjustValue.add(ownvalue);
            }
            currentPeriodValue = currentPeriodValue.add(ownvalue);
            usedValue = usedValue.add(dyn.getBigDecimal("usedvalue"));
            frozenValue = frozenValue.add(dyn.getBigDecimal("freezevalue"));
            usableValue = usableValue.add(dyn.getBigDecimal("usablevalue"));
        }

        //本期享有 = 本期享有减去调整时长
        currentPeriodValue = currentPeriodValue.subtract(adjustValue);
        //如果本期享有小于0，那么赋值为0
        if (currentPeriodValue.compareTo(BigDecimal.ZERO) < 0) {
            currentPeriodValue = BigDecimal.ZERO;
        }

        IDataModel model = this.getModel();
        IFormView view = this.getView();
        model.setValue("tdkw_ownvalue", currentPeriodValue);
        model.setValue("tdkw_cdedvalue", prePeriodValue);
        model.setValue("tdkw_usablevalue", usableValue);
        model.setValue("tdkw_freezevalue", frozenValue);
        model.setValue("tdkw_usedvalue", usedValue);
        model.setValue("tdkw_adjustDuration", adjustValue);

        view.updateView("tdkw_ownvalue");
        view.updateView("tdkw_cdedvalue");
        view.updateView("tdkw_usablevalue");
        view.updateView("tdkw_freezevalue");
        view.updateView("tdkw_usedvalue");
        view.updateView("tdkw_adjustDuration");
    }


    public void getWTCAnnualLeaveInfo() {
        // 上期结余
        BigDecimal prePeriodValue = BigDecimal.ZERO;
        // 调整时长
        BigDecimal adjustValue = BigDecimal.ZERO;
        // 本期享有
        BigDecimal currentPeriodValue = BigDecimal.ZERO;
        // 已休天数
        BigDecimal usedValue = BigDecimal.ZERO;
        // 冻结天数
        BigDecimal frozenValue = BigDecimal.ZERO;
        // 可用天数
        BigDecimal usableValue = BigDecimal.ZERO;
        //获取当前登录人员工号
        QFilter qFilter = new QFilter("id", QCP.in, UserServiceHelper.getCurrentUserId())
                .and("enable", QCP.equals, "1")
                .and("status", QCP.equals, "C");
        DynamicObject query = QueryServiceHelper.queryOne("bos_user", "id,number", new QFilter[]{qFilter});
        String number = query.getString("number");
        Map<String, BigDecimal> map = new HashMap<>();
        try {
            Object maps = DispatchServiceHelper.invokeService("tdkw.wtc.trip.servicehelper", "tdkw_trip",
                    "GetQTDetailService", "getQtDetail", new Object[]{Arrays.asList(number), null});
            map = (Map<String, BigDecimal>) ((HashMap) maps).get(number);
            LOGGER.info("年假接口调用返回值："+map.toString());
        } catch (Exception e) {
            LOGGER.info("年假接口调用:" + e.getMessage());
        }
        if (Objects.nonNull(map)) {
            if (!map.isEmpty()) {
                prePeriodValue = map.get("previousPeriodBalance");
                adjustValue = map.get("adjustValue");
                currentPeriodValue = map.get("currentPeriodEnjoy");
                usedValue = map.get("alreadyVaApplyDays");
                frozenValue = map.get("freezeDays");
                usableValue = map.get("availableDays");
            }
        }
        IDataModel model = this.getModel();
        IFormView view = this.getView();
        model.setValue("tdkw_ownvalue", currentPeriodValue);
        model.setValue("tdkw_cdedvalue", prePeriodValue);
        model.setValue("tdkw_usablevalue", usableValue);
        model.setValue("tdkw_freezevalue", frozenValue);
        model.setValue("tdkw_usedvalue", usedValue);
        model.setValue("tdkw_adjustDuration", adjustValue);

        view.updateView("tdkw_ownvalue");
        view.updateView("tdkw_cdedvalue");
        view.updateView("tdkw_usablevalue");
        view.updateView("tdkw_freezevalue");
        view.updateView("tdkw_usedvalue");
        view.updateView("tdkw_adjustDuration");
    }
}