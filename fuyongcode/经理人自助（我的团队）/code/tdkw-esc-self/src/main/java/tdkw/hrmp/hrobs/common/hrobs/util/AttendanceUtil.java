package tdkw.hrmp.hrobs.common.hrobs.util;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import kd.wtc.wtbs.common.constants.WTCCommonConstants;
import kd.wtc.wtbs.common.model.period.PerAttPeriodReport;
import kd.wtc.wtbs.common.util.WTCCollections;
import kd.wtc.wtbs.common.util.WTCDateUtils;
import kd.wtc.wtss.business.servicehelper.mobile.MobileHomePageBusiness;
import kd.wtc.wtss.business.servicehelper.mobile.MobileHomePageServiceHelper;
import kd.wtc.wtss.business.servicehelper.summaryconf.SummaryConfigDetailService;
import kd.wtc.wtss.common.constants.MobileConfConstants;
import kd.wtc.wtss.common.dto.summaryconf.SummaryConfDetail;
import kd.wtc.wtss.common.enums.UnitDataEnum;
import tdkw.hrmp.hrobs.common.hrobs.pojo.AttPeriodItemStat;
import tdkw.hrmp.hrobs.common.hrobs.pojo.PerAttPeriod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/5/26 17:44
 * @Description PC端 当月出勤卡片 工具类
 * @Demander xxx
 * @Document 员工自助门户与个人卡片-XXX集团需求规格说明书_V2.0(4)
 * @Basedata tdkw_hrobs_pc_attendance
 * @Version 1.0
 **/
public class AttendanceUtil {
    private static final Log LOGGER = LogFactory.getLog(AttendanceUtil.class);

    /**
     * 获取假勤自助门户方案ID
     * <p>
     * 可能获取不到门户方案，员工的门户方案通过方案上的规则匹配的，如果规则不匹配将获取不到，获取不到时将返回 0
     *
     * @param selfServiceSchemeRule 自助门户匹配规则，可以直接传考勤档案Map，考勤档案通过 {@link #getAttFile(long, Date)} 获取
     * @return 假勤自助门户方案ID，如果获取不到将返回0
     */
    public static long getSelfServiceSchemeId(Map<String, String> selfServiceSchemeRule) {
        return MobileHomePageBusiness.getInstance().getSchemeId(selfServiceSchemeRule);
    }


    /**
     * 获取当前登录人对应的考勤人ID
     * <p>
     * 有可能获取不到考勤人ID，比如员工并未在考勤创建档案时就获取不到，此时返回null
     *
     * @return 当前登录人对应的考勤人ID
     */
    public static long getAttPersonIdOfCurrentLoginUser() {
        final Long userId = MobileCommonServiceHelper.getInstance().getUserId();
        if (userId == null) {
            return 0;
        }
        return userId;
    }

    /**
     * 获取考勤档案
     * <p>
     * 可能获取不到考勤档案，
     *
     * @param attPersonId 考勤人ID
     * @param attFileDate 档案日期，档案是时序性对象，需要指定日期获取
     * @return 考勤档案
     */
    public static Map<String, String> getAttFile(long attPersonId, Date attFileDate) {
        String attFileDateStr = WTCDateUtils.date2Str(attFileDate, WTCCommonConstants.DEF_DATE_FORMAT);
        return MobileHomePageServiceHelper.getAttFile(attPersonId, attFileDateStr);
    }

    /**
     * 获取指定日期所在的人员考勤期间，如果获取不到将返回null
     *
     * @param attPersonId        考勤人员id
     * @param dateInPerAttPeriod 人员考勤期间包含的日期
     * @return 人员考勤期间，如果查询不到将返回null
     */
    public static PerAttPeriod getPerAttPeriod(long attPersonId, Date dateInPerAttPeriod) {
        List<PerAttPeriodReport> userAllPeriods = MobileHomePageServiceHelper.getUserAllPeriods(attPersonId);
        LOGGER.info("userAllPeriods:"+userAllPeriods);
        if (WTCCollections.isEmpty(userAllPeriods)) {
            return null;
        }
        //long nowTime = dateInPerAttPeriod.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (PerAttPeriodReport periodReport : userAllPeriods) {
            String dateInPerAttPeriodStr = simpleDateFormat.format(dateInPerAttPeriod);
            LOGGER.info("dateInPerAttPeriodStr:"+dateInPerAttPeriodStr);
            String perAttBeginDateStr = simpleDateFormat.format(periodReport.getPerAttBeginDate());
            LOGGER.info("perAttBeginDateStr:"+perAttBeginDateStr);
            String perAttEndDateStr = simpleDateFormat.format(periodReport.getPerAttEndDate());
            LOGGER.info("perAttEndDateStr:"+perAttEndDateStr);
            Date dateInPerAttPeriodStrFrom = null;
            Date perAttBeginDateStrFrom = null;
            Date perAttEndDateStrFrom = null;
            try {
                dateInPerAttPeriodStrFrom = simpleDateFormat.parse(dateInPerAttPeriodStr);
                perAttBeginDateStrFrom = simpleDateFormat.parse(perAttBeginDateStr);
                perAttEndDateStrFrom = simpleDateFormat.parse(perAttEndDateStr);
                LOGGER.info("dateInPerAttPeriodStrFrom:"+dateInPerAttPeriodStrFrom+"perAttBeginDateStrFrom"+perAttBeginDateStrFrom
                +"perAttEndDateStrFrom"+perAttEndDateStrFrom);
                int isBegin = dateInPerAttPeriodStrFrom.compareTo(perAttBeginDateStrFrom);
                int isEnd = dateInPerAttPeriodStrFrom.compareTo(perAttEndDateStrFrom);
                if(isBegin >= 0 && isEnd <=0){
                    // 当前期间
                    final PerAttPeriod perAttPeriod = new PerAttPeriod();
                    perAttPeriod.setAttPeriodEndDate(periodReport.getAttPeriodEndDate());
                    perAttPeriod.setAttPeriodId(periodReport.getAttPeriodId());
                    perAttPeriod.setAttPeriodStartDate(periodReport.getAttPeriodStartDate());
                    perAttPeriod.setFileBoId(periodReport.getFileBoId());
                    perAttPeriod.setFileId(periodReport.getFileId());
                    perAttPeriod.setId(periodReport.getId());
                    perAttPeriod.setMhsa(periodReport.getMhsa());
                    perAttPeriod.setPersonId(periodReport.getPersonId());
                    perAttPeriod.setPerAttPeriodEndDate(periodReport.getPerAttEndDate());
                    perAttPeriod.setPerAttPeriodStartDate(periodReport.getAttPeriodStartDate());
                    LOGGER.info("perAttPeriod"+perAttPeriod);
                    return perAttPeriod;
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("linhao:isnull");
        return null;
    }

    /**
     * 查询期间汇总配置信息
     * 用户配置和系统配置交集
     *
     * @param attPersonId         考勤人ID
     * @param type                工作空间类型 例 A 个人 B 团队
     * @param personAttPeriodId   人员考勤期间ID
     * @param selfServiceSchemeId 自助门户方案ID
     * @param scene               适用终端 1 移动 2 PC
     * @return 期间汇总项目统计列表
     */
    public static List<AttPeriodItemStat> getAttPeriodItemStat(long attPersonId, String type,
                                                               String personAttPeriodId, long selfServiceSchemeId, int scene) {
        LOGGER.info("attPersonId:"+attPersonId+"type："+type+"personAttPeriodId:"+personAttPeriodId+"selfServiceSchemeId:"+selfServiceSchemeId+"scene:"+scene);
        Map<String, Object> summaryConfig = SummaryConfigDetailService.getInstance().query(attPersonId, type, personAttPeriodId, selfServiceSchemeId, scene);
        List<SummaryConfDetail> showSummary = (List<SummaryConfDetail>) summaryConfig.get(MobileConfConstants.KEY_SHOW);
        LOGGER.info("showSummary:"+showSummary);
        if (WTCCollections.isEmpty(showSummary)) {
            return Collections.emptyList();
        }
        return showSummary.stream().map(summaryConfDetail -> {
            final AttPeriodItemStat attPeriodItemStat = new AttPeriodItemStat();
            attPeriodItemStat.setAttItems(summaryConfDetail.getAttItems());
            attPeriodItemStat.setAttItemValue(summaryConfDetail.getAttItemValue());
            attPeriodItemStat.setDatatype(summaryConfDetail.getDatatype());
            attPeriodItemStat.setName(summaryConfDetail.getName());
            attPeriodItemStat.setUnit(summaryConfDetail.getUnit());
            attPeriodItemStat.setUnitName(UnitDataEnum.getRetDesc(summaryConfDetail.getUnit()));
            LOGGER.info("attPeriodItemStat:"+attPeriodItemStat);
            return attPeriodItemStat;
        }).collect(Collectors.toList());
    }
}