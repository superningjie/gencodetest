package tdkw.hrmp.hrobs.common.hrobs.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.FilterInfo;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.wtc.wtbs.business.model.EffectiveEntityVo;
import kd.wtc.wtp.business.cumulate.trading.QTDealRecordDBService;
import kd.wtc.wtp.business.cumulate.trading.model.EffectiveEntityVoExt;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import tdkw.hrmp.hrobs.common.hrobs.pojo.PersonQuotaVo;
import tdkw.hrmp.hrobs.common.hrobs.pojo.QuotaVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 获取年假信息工具类
 *
 * @author : zhousy
 * @date : 2024/1/23 11:25
 */
public class QTDetailUtil {

    private static final Log logger = LogFactory.getLog(QTDetailUtil.class);

    /**
     * 根据员工编号获取该员工的年假信息 - 二开SQL报表查询方式
     * 默认：查询日期为当前日期
     *
     * @param numberList   员工工号list
     * @param qtTypeNumber 定额类型编码 默认：1020_S(年假_定额)
     * @return key->员工编号
     * value：
     * 上期结余 previousPeriodBalance
     * 调整时长 adjustValue
     * 本期享有 currentPeriodEnjoy
     * 已休天数 alreadyVaApplyDays
     * 冻结天数 freezeDays
     * 可用天数 availableDays
     */
    public static Map<String, Map<String, BigDecimal>> getQTDetailByUserNumberList(List<String> numberList, String qtTypeNumber) throws Exception {
        HashMap<String, Map<String, BigDecimal>> returnData = new HashMap<>();

        //根据工号获取最新的考勤档案编号
        QFilter[] qf = new QFilter("personnum", QCP.in, numberList)
                .and("atttag.number", QCP.equals, "1010_S")
                .and("iscurrentversion", QCP.equals, true)
                .toArray();
        DynamicObject[] attFileBaseList = BusinessDataServiceHelper.load("wtp_attfilebase",
                "personnum,number", qf);
        List<String> attFileNumList = Arrays.stream(attFileBaseList).map(x -> x.getString("number")).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(attFileNumList)) {
            logger.error("该员工无对应有效的考勤档案编号：{}",numberList);
            return returnData;
        }
        LinkedHashMap<Long, PersonQuotaVo> queryInfo = new QTReportUtil().getQueryInfo(attFileNumList, new FilterInfo());
        String finalQtTypeNumber = StringUtils.isEmpty(qtTypeNumber) ? "1020_S" : qtTypeNumber;
        if (!queryInfo.isEmpty()) {
            queryInfo.forEach((baseId, personQuotaVo) -> {
                HashMap<String, BigDecimal> quoteMap = new HashMap();
                for (QuotaVo quotaVo : personQuotaVo.getQuotaVoList()) {
                    if (finalQtTypeNumber.equals(quotaVo.getQtTypeName())) {
                        quoteMap.put("previousPeriodBalance", quotaVo.getPreviousPeriodBalance());
                        quoteMap.put("adjustValue", quotaVo.getAdjustValue());
                        quoteMap.put("currentPeriodEnjoy", quotaVo.getCurrentPeriodEnjoy());
                        quoteMap.put("freezeDays", quotaVo.getFreezeDays());
                        quoteMap.put("alreadyVaApplyDays", quotaVo.getAlreadyVaApplyDays());
                        quoteMap.put("availableDays", quotaVo.getAvailableDays());
                        break;
                    }
                }
                returnData.put(personQuotaVo.getNumber(), quoteMap);
            });
        }
        return returnData;
    }

    /**
     * 根据员工编号获取该员工的年假信息 - 调用标品方式
     * 默认：查询日期为当前日期
     *
     * @param numberList   员工工号list
     * @param qtTypeNumber 定额类型编码 默认：1020_S(年假_定额)
     * @return key->员工编号
     * value：
     * 上期结余 previousPeriodBalance
     * 调整时长 adjustValue
     * 本期享有 currentPeriodEnjoy
     * 已休天数 alreadyVaApplyDays
     * 冻结天数 freezeDays
     * 可用天数 availableDays
     */
    public static Map<String, Map<String, BigDecimal>> getQTDetailByUserNumberListOld(List<String> numberList, String qtTypeNumber) throws Exception {
        long startTime = System.currentTimeMillis();

        //默认定额类型为年假
        if (StringUtils.isEmpty(qtTypeNumber)) {
            qtTypeNumber = "1020_S";
        }

        //封装需要返回的定额信息
        HashMap<String, Map<String, BigDecimal>> returnData = new HashMap<>();
        Date now = new Date();

        //汇总上期结余、调整时长、本期享有查出的休假信息，用于计算已修天数。
        HashMap<String, ArrayList<DynamicObject>> allVaApplyMap = new HashMap<>();

        //查询定额明细的数据 并将数据处理成Map key->工号 value->数据行
        QFilter[] qf = new QFilter("attfileid.attperson.number", QCP.in, numberList)
                .and("qttype.number", QCP.equals, qtTypeNumber)
                .and("attfilebo.atttag.number", QCP.equals, "1010_S")
                .and("attfilebo.iscurrentversion", QCP.equals, true)
                .toArray();
        DynamicObject[] qtLineDetailList = null;
        DynamicObject[] qtLineDetails = BusinessDataServiceHelper.load("wtp_qtlinedetail",
                "id,attfileid,usestartdate,useenddate,source,ownvalue,usedvalueid,busstatus,detailsourceid.id", qf);

        for (DynamicObject d : qtLineDetails) {
            if ("DT-005".equals(d.getString("source")) && !"0".equals(d.getString("detailsourceid.id"))) {
                Long detailSourceId = Long.valueOf(d.getString("detailsourceid.id"));
                QFilter[] filters = new QFilter("id", QCP.equals, detailSourceId)
                        .toArray();
                DynamicObject[] details = BusinessDataServiceHelper.load("wtp_qtlinedetail",
                        "attfileid,usestartdate,useenddate,source,ownvalue,usedvalueid,busstatus,detailsourceid", filters);
                if (details != null && details.length > 0) {
                    qtLineDetailList = ArrayUtils.addAll(qtLineDetails, details[0]);
                    d.set("ownvalue", BigDecimal.ZERO);
                    logger.info("存在跨BU情况，源明细ID = {},detailSourceId = {},details = {}", d.getString("id"), detailSourceId, details[0]);
                }
            }
        }

        if (qtLineDetailList == null || qtLineDetailList.length < 1) {
            qtLineDetailList = qtLineDetails;
        }

        //定额信息根据工号进行分组
        Map<String, List<DynamicObject>> qtLineDetailsMap = Arrays.stream(qtLineDetailList).collect(
                Collectors.groupingBy(x -> {
                    DynamicObject attPerson = x.getDynamicObject("attfileid.attperson");
                    return attPerson.getString("number");
                }));

        //计算【上期结余】
        calculatePreviousPeriodBalance(qtLineDetailsMap, returnData, now, allVaApplyMap, qtTypeNumber);
        logger.info("PreviousPeriodBalance_returnData={}", returnData);

        //计算【调整时长】
        calculateAdjust(qtLineDetailsMap, returnData, now, allVaApplyMap, qtTypeNumber);
        logger.info("Adjust_returnData={}", returnData);

        //计算【本期享有】
        calculateCurrentPeriodEnjoy(qtLineDetailsMap, returnData, now, allVaApplyMap);
        logger.info("CurrentPeriodEnjoy_returnData={}", returnData);

        //计算【已休天数】
        for (String key : allVaApplyMap.keySet()) {
            ArrayList<DynamicObject> data = allVaApplyMap.get(key);
            logger.info("key={} allVaApplyList={}", key, data.stream()
                    .map(x -> x.getString("billno")).collect(Collectors.toList()));
        }
        calculateAlreadyVaApplyDays(allVaApplyMap, now, returnData, qtTypeNumber);
        logger.info("AlreadyVaApplyDays_returnData={}", returnData);

        //计算【冻结天数】
        calculateFreezeDays(allVaApplyMap, now, returnData, qtTypeNumber);
        logger.info("FreezeDays_returnData={}", returnData);

        //计算【可用天数】
        calculateAvailableDays(returnData);
        logger.info("AvailableDays_returnData={}", returnData);
        logger.info("定额计算结束,用时：{}", (System.currentTimeMillis() - startTime));
        return returnData;
    }

    /**
     * 计算可用天数
     *
     * @param returnData 每个人的年假信息 key->工号 value->年假信息Map
     */
    private static void calculateAvailableDays(HashMap<String, Map<String, BigDecimal>> returnData) {

        for (String key : returnData.keySet()) {
            Map<String, BigDecimal> value = returnData.get(key);
            BigDecimal previousPeriodBalance = value.get("previousPeriodBalance");
            BigDecimal adjustValue = value.get("adjustValue");
            BigDecimal currentPeriodEnjoy = value.get("currentPeriodEnjoy");
            BigDecimal alreadyVaApplyDays = value.get("alreadyVaApplyDays");
            BigDecimal freezeDays = value.get("freezeDays");
            BigDecimal availableDays = previousPeriodBalance.add(adjustValue).add(currentPeriodEnjoy)
                    .subtract(alreadyVaApplyDays).subtract(freezeDays);
            value.put("availableDays", availableDays.setScale(2, RoundingMode.DOWN));
            logger.info("number={} availableDays={}", key, availableDays);
        }
    }

    /**
     * 计算已冻结天数
     *
     * @param allVaApplyMap 休假信息
     * @param now           当前时间
     * @param returnData    每个人的年假信息 key->工号 value->年假信息Map
     * @param qtTypeNumber  定额类型
     */
    private static void calculateFreezeDays(HashMap<String, ArrayList<DynamicObject>> allVaApplyMap,
                                            Date now,
                                            HashMap<String, Map<String, BigDecimal>> returnData, String qtTypeNumber) {

        for (String key : allVaApplyMap.keySet()) {

            QFilter[] qf = new QFilter("personid.number", QCP.equals, key)
                    .and("attfile.atttag.number", QCP.equals, "1010_S")
                    .and("attfile.iscurrentversion", QCP.equals, true)
                    .toArray();
            DynamicObject[] info = BusinessDataServiceHelper.load("xyjt_wtabm_vaapply_all",
                    "billstatus,billno,xyjt_sumvatimeday,entryentity.entrystartdate,entryentity.entryenddate,entryentity.entryvatimeday,entryentity.entryvacationtype",
                    qf);
            List<DynamicObject> value = Arrays.asList(info);
            logger.info("billNoList={}", Arrays.stream(info).map(x -> x.getString("billno")).collect(Collectors.toList()));

            //【过滤出冻结数据】
            List<DynamicObject> vaApplyInfo = value.stream()
                    .filter(data -> "D".equalsIgnoreCase(data.getString("billstatus")) && data.getString("billno").startsWith("LE"))
                    .collect(Collectors.toList());

            //【累加“休假发生时间的年份”<“查询日期”的年份】
            BigDecimal sumVaApplyDay = new BigDecimal("0");
            for (DynamicObject vaApplyData : vaApplyInfo) {

                DynamicObjectCollection entryEntity = vaApplyData.getDynamicObjectCollection("entryentity");
                for (DynamicObject entry : entryEntity) {
                    Date entryStartDate = entry.getDate("entrystartdate");
                    Date entryEndDate = entry.getDate("entryenddate");
                    int startYear = Integer.parseInt(DateFormatUtils.format(entryStartDate, "yyyy"));
                    int endYear = Integer.parseInt(DateFormatUtils.format(entryEndDate, "yyyy"));
                    int nowYear = Integer.parseInt(DateFormatUtils.format(now, "yyyy"));
                    BigDecimal sumVaTimeDay = entry.getBigDecimal("entryvatimeday");
                    logger.info("calculateFreezeDays startYear={} endYear={} nowYear={} sumVaTimeDay={}",
                            startYear, endYear, nowYear, sumVaTimeDay);
                    //当前不是休假类型不是年假就跳过
                    String vaApplyType = entry.getString("entryvacationtype.number");
                    if (!"3000_S".equalsIgnoreCase(vaApplyType) && "1020_S".equalsIgnoreCase(qtTypeNumber)) {
                        continue;
                    }

                    //开始结束时间的年都等于查询日期年份
                    if (startYear == nowYear && endYear == nowYear) {
                        sumVaApplyDay = sumVaApplyDay.add(sumVaTimeDay);

                    }
                }
                //设置冻结天数数据
                Map<String, BigDecimal> returnDetail = returnData.get(key);
                returnDetail.put("freezeDays", sumVaApplyDay.setScale(2, RoundingMode.DOWN));
            }
        }
    }

    /**
     * 计算已冻结天数
     *
     * @param allVaApplyMap 休假信息
     * @param now           当前时间
     * @param returnData    每个人的年假信息 key->工号 value->年假信息Map
     */
//    private static void calculateFreezeDays(HashMap<String, ArrayList<DynamicObject>> allVaApplyMap,
//                                            Date now,
//                                            HashMap<String, Map<String, BigDecimal>> returnData) {
//        ArrayList<String> freezeDaysBillNo = new ArrayList<>();
//
//        for (String key : allVaApplyMap.keySet()) {
//            ArrayList<DynamicObject> value = allVaApplyMap.get(key);
//
//            //【过滤出冻结数据】
//            List<DynamicObject> vaApplyInfo = value.stream()
//                    .filter(data -> "D".equalsIgnoreCase(data.getString("billstatus")))
//                    .collect(Collectors.toList());
//
//            //【累加“休假发生时间的年份”<“查询日期”的年份】
//            BigDecimal sumVaApplyDay = new BigDecimal("0");
//            for (DynamicObject vaApplyData : vaApplyInfo) {
//                String billNo = vaApplyData.getString("billno");
//                //没有被重复计算过
//                if (!freezeDaysBillNo.contains(billNo)) {
//                    freezeDaysBillNo.add(billNo);
//
//                    DynamicObjectCollection entryEntity = vaApplyData.getDynamicObjectCollection("entryentity");
//                    for (DynamicObject entry : entryEntity) {
//                        Date entryStartDate = entry.getDate("entrystartdate");
//                        Date entryEndDate = entry.getDate("entryenddate");
//                        int startYear = Integer.parseInt(DateFormatUtils.format(entryStartDate, "yyyy"));
//                        int endYear = Integer.parseInt(DateFormatUtils.format(entryEndDate, "yyyy"));
//                        int nowYear = Integer.parseInt(DateFormatUtils.format(now, "yyyy"));
//                        BigDecimal sumVaTimeDay = vaApplyData.getBigDecimal("xyjt_sumvatimeday");
//                        logger.info("calculateFreezeDays startYear={} endYear={} nowYear={} sumVaTimeDay={}",
//                                startYear, endYear, nowYear, sumVaTimeDay);
//
//                        //开始结束时间的年都等于查询日期年份
//                        if (startYear == nowYear && endYear == nowYear) {
//                            sumVaApplyDay = sumVaApplyDay.add(sumVaTimeDay);
//
//                            //开始时间的年=查询日期年份 但 结束时间的年>查询日期年份
//                        } else if (startYear == nowYear && endYear > nowYear) {
//                            //TODO 跨年的待处理
//
//                            //开始时间的年<查询日期年份 但 结束时间的年=查询日期年份
//                        } else if (startYear > nowYear && endYear == nowYear) {
//                            //TODO 跨年待处理
//
//                        }
//                    }
//                    //设置冻结天数数据
//                    Map<String, BigDecimal> returnDetail = returnData.get(key);
//                    returnDetail.put("freezeDays", sumVaApplyDay);
//                }
//            }
//        }
//    }

    /**
     * 计算已休天数 （当前年）
     *
     * @param allVaApplyMap 休假信息
     * @param now           当前时间
     * @param returnData    每个人的年假信息 key->工号 value->年假信息Map
     * @param qtTypeNumber  定额类型
     */
    private static void calculateAlreadyVaApplyDays(HashMap<String, ArrayList<DynamicObject>> allVaApplyMap,
                                                    Date now,
                                                    HashMap<String, Map<String, BigDecimal>> returnData, String qtTypeNumber) {
        ArrayList<String> alreadyUseBillNo = new ArrayList<>();

        for (String key : allVaApplyMap.keySet()) {
            ArrayList<DynamicObject> value = allVaApplyMap.get(key);

            //【过滤掉冻结的数据(状态为审批中) 剩下的都是已使用的数据】
            List<DynamicObject> vaApplyInfo = value.stream()
                    .filter(data -> !"D".equalsIgnoreCase(data.getString("billstatus")))
                    .collect(Collectors.toList());

            //【累加“休假发生时间的年份”<“查询日期”的年份】
            BigDecimal sumVaApplyDay = new BigDecimal("0");
            for (DynamicObject vaApplyData : vaApplyInfo) {
                String billNo = vaApplyData.getString("billno");
                //没有被重复计算过
                if (!alreadyUseBillNo.contains(billNo)) {
                    alreadyUseBillNo.add(billNo);

                    DynamicObjectCollection entryEntity = vaApplyData.getDynamicObjectCollection("entryentity");
                    for (DynamicObject entry : entryEntity) {
                        Date entryStartDate = entry.getDate("entrystartdate");
                        Date entryEndDate = entry.getDate("entryenddate");
                        int startYear = Integer.parseInt(DateFormatUtils.format(entryStartDate, "yyyy"));
                        int endYear = Integer.parseInt(DateFormatUtils.format(entryEndDate, "yyyy"));
                        int nowYear = Integer.parseInt(DateFormatUtils.format(now, "yyyy"));
                        BigDecimal sumVaTimeDay = entry.getBigDecimal("entryvatimeday");
                        logger.info("calculateAlreadyVaApplyDays startYear={} endYear={} nowYear={} sumVaTimeDay={}",
                                startYear, endYear, nowYear, sumVaTimeDay);
                        //当前不是休假类型不是年假就跳过
                        String vaApplyType = entry.getString("entryvacationtype.number");
                        if (!"3000_S".equalsIgnoreCase(vaApplyType) && "1020_S".equalsIgnoreCase(qtTypeNumber)) {
                            continue;
                        }
                        //开始结束时间的年都等于查询日期年份
                        if (startYear == nowYear && endYear == nowYear) {
                            sumVaApplyDay = sumVaApplyDay.add(sumVaTimeDay);

                            //开始时间的年=查询日期年份 但 结束时间的年>查询日期年份
                        } else if (startYear == nowYear && endYear > nowYear) {
                            //TODO 跨年的待处理

                            //开始时间的年<查询日期年份 但 结束时间的年=查询日期年份
                        } else if (startYear > nowYear && endYear == nowYear) {
                            //TODO 跨年待处理

                        }
                    }
                    //设置已休天数数据
                    Map<String, BigDecimal> returnDetail = returnData.get(key);
                    returnDetail.put("alreadyVaApplyDays", sumVaApplyDay.setScale(2, RoundingMode.DOWN));
                }
            }
        }
    }

    /**
     * 计算本期享有
     *
     * @param qtLineDetailsMap 定额明细根据工号分类Map
     * @param returnData       每个人的年假信息 key->工号 value->年假信息Map
     * @param now              查询时间
     * @param allVaApplyMap    所有休假
     */
    private static void calculateCurrentPeriodEnjoy(Map<String, List<DynamicObject>> qtLineDetailsMap,
                                                    HashMap<String, Map<String, BigDecimal>> returnData,
                                                    Date now,
                                                    HashMap<String, ArrayList<DynamicObject>> allVaApplyMap) {

        for (String key : qtLineDetailsMap.keySet()) {
            ArrayList<DynamicObject> allVaApplyMapValue = allVaApplyMap.get(key);
            //收集满足条件的休假数据
            ArrayList<DynamicObject> vaApplyList = new ArrayList<>();

            Map<String, BigDecimal> returnDataDetail = returnData.get(key);
            //累加享有时长
            BigDecimal sumOwnValue = new BigDecimal("0");
            //按照标品逻辑查询出的休假信息。
            Map<String, BigDecimal> standardSearchVaDetail = new HashMap<>();
            //当前人的定额数据
            List<DynamicObject> value = qtLineDetailsMap.get(key);
            for (DynamicObject data : value) {
                //1.来源系统生成(DT-000) 状态为生效
                String source = data.getString("source");
                String busStatus = data.getString("busstatus");
                boolean isSystemProduce = ("DT-000".equalsIgnoreCase(source) || "DT-005".equalsIgnoreCase(source)) && "0".equalsIgnoreCase(busStatus);

                //2.“使用开始日期”的年份=“查询日期”的年份
                Date useStartDate = data.getDate("usestartdate");
                boolean isUsedStartYearBeforeNow = Integer.parseInt(DateFormatUtils.format(useStartDate, "yyyy"))
                        == Integer.parseInt(DateFormatUtils.format(now, "yyyy"));

                //3.“使用结束日期”≥“查询日期”
                Date useEndDate = data.getDate("useenddate");
                boolean isLargeOrEqualsNow = useEndDate.after(now) || useEndDate.equals(now);

                //4.“使用开始日期”<=“查询日期”
                boolean isBeforeOrEqualsNow = useStartDate.before(now) || useStartDate.equals(now);

                if (isSystemProduce && isUsedStartYearBeforeNow && isLargeOrEqualsNow && isBeforeOrEqualsNow) {
                    BigDecimal ownValue = data.getBigDecimal("ownvalue");
                    sumOwnValue = sumOwnValue.add(ownValue);
                    logger.info("calculateCurrentPeriodEnjoy key={} ownValue={} sumOwnValue={}", key, ownValue, sumOwnValue);
                    //查询休假数据
                    DynamicObject[] searchVaApplyInfo = searchVaApplyData(data, standardSearchVaDetail);
                    vaApplyList.addAll(Arrays.asList(searchVaApplyInfo));
                }
            }

            //过滤掉不需要的休假数据
            filterVaApplyInfo(vaApplyList);
            //保存一份数据用于查询已休天数
            allVaApplyMapValue.addAll(vaApplyList);
            logger.info("calculateCurrentPeriodEnjoy sumOwnValue={} vaApplyList={} allVaApplyMapValue={}", sumOwnValue,
                    vaApplyList.stream().map(x -> x.getString("billno")).collect(Collectors.toList()),
                    allVaApplyMapValue.stream().map(x -> x.getString("billno")).collect(Collectors.toList()));

            //封装上期结余数据
            returnDataDetail.put("currentPeriodEnjoy", sumOwnValue.setScale(2, RoundingMode.DOWN));
            returnData.put(key, returnDataDetail);
            logger.info("calculateCurrentPeriodEnjoy returnData={}", returnData);

            standardSearchVaDetail.clear();
        }
    }

    /**
     * 计算调整时长
     *
     * @param qtLineDetailsMap 定额明细根据工号分类Map
     * @param returnData       每个人的年假信息 key->工号 value->年假信息Map
     * @param now              查询时间
     * @param allVaApplyMap    所有休假
     * @param qtTypeNumber     定额类型
     */
    private static void calculateAdjust(Map<String, List<DynamicObject>> qtLineDetailsMap,
                                        HashMap<String, Map<String, BigDecimal>> returnData,
                                        Date now,
                                        HashMap<String, ArrayList<DynamicObject>> allVaApplyMap, String qtTypeNumber) {

        for (String key : qtLineDetailsMap.keySet()) {
            ArrayList<DynamicObject> allVaApplyMapValue = allVaApplyMap.get(key);
            //收集满足条件的休假数据
            ArrayList<DynamicObject> vaApplyList = new ArrayList<>();

            Map<String, BigDecimal> returnDataDetail = returnData.get(key);
            //累加享有时长
            BigDecimal sumOwnValue = new BigDecimal("0");
            //按照标品逻辑查询出的休假信息。
            Map<String, BigDecimal> standardSearchVaDetail = new HashMap<>();
            //当前人的定额数据
            List<DynamicObject> value = qtLineDetailsMap.get(key);
            for (DynamicObject data : value) {
                //1.来源手动生成(DT-002) 状态为生效
                String source = data.getString("source");
                String busStatus = data.getString("busstatus");
                boolean isManualProduce = "DT-002".equalsIgnoreCase(source) && "0".equalsIgnoreCase(busStatus);
                //2.“使用开始日期”≤“查询日期”
                Date useStartDate = data.getDate("usestartdate");
                boolean isUsedStartDateBeforeNow = useStartDate.before(now) || useStartDate.equals(now);
                //3.“查询日期”≤“使用结束日期”
                Date useEndDate = data.getDate("useenddate");
                boolean isLargeOrEqualsNow = useEndDate.after(now) || useEndDate.equals(now);

                if (isManualProduce && isUsedStartDateBeforeNow && isLargeOrEqualsNow) {
                    BigDecimal ownValue = data.getBigDecimal("ownvalue");
                    logger.info("calculateAdjust key={} ownValue={}", key, ownValue);
                    sumOwnValue = sumOwnValue.add(ownValue);
                    //查询休假数据
                    DynamicObject[] searchVaApplyInfo = searchVaApplyData(data, standardSearchVaDetail);
                    vaApplyList.addAll(Arrays.asList(searchVaApplyInfo));
                }
            }

            //过滤掉不需要的休假数据
            filterVaApplyInfo(vaApplyList);
            //保存一份数据用于查询已休天数
            allVaApplyMapValue.addAll(vaApplyList);
            logger.info("calculateAdjust sumOwnValue={} vaApplyList={} allVaApplyMapValue={}", sumOwnValue,
                    vaApplyList.stream().map(x -> x.getString("billno")).collect(Collectors.toList()),
                    allVaApplyMapValue.stream().map(x -> x.getString("billno")).collect(Collectors.toList()));

            //【过滤掉冻结的数据(状态为审批中) 剩下的都是已使用的数据】
            List<DynamicObject> vaApplyData = vaApplyList.stream()
                    .filter(data -> !"D".equalsIgnoreCase(data.getString("billstatus")))
                    .collect(Collectors.toList());

            //过滤掉标品中不需要参与计算的单据 得出最终需要参与计算的可用天数数据
            filterStandardBill(standardSearchVaDetail, vaApplyData);
            //计算出第一版可用天数（未扣除其他年份）
            BigDecimal sumStandardAvailable = standardSearchVaDetail.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            //【累加“休假发生时间的年份”<“查询日期”的年份】
            BigDecimal sumVaApplyDay = BigDecimal.ZERO;
            BigDecimal VaApplyDay;
            for (DynamicObject data : vaApplyData) {
                VaApplyDay = BigDecimal.ZERO;
                String billNo = data.getString("billno");
                BigDecimal standardDay = standardSearchVaDetail.get(billNo);
                DynamicObjectCollection entryEntity = data.getDynamicObjectCollection("entryentity");
                for (DynamicObject entry : entryEntity) {
                    Date entryStartDate = entry.getDate("entrystartdate");
                    Date entryEndDate = entry.getDate("entryenddate");
                    int startYear = Integer.parseInt(DateFormatUtils.format(entryStartDate, "yyyy"));
                    int endYear = Integer.parseInt(DateFormatUtils.format(entryEndDate, "yyyy"));
                    int nowYear = Integer.parseInt(DateFormatUtils.format(now, "yyyy"));
                    BigDecimal sumVaTimeDay = entry.getBigDecimal("entryvatimeday");
                    logger.info("calculateAdjust startYear={} endYear={} nowYear={} sumVaTimeDay={}",
                            startYear, endYear, nowYear, sumVaTimeDay);
                    //当前不是休假类型不是年假就跳过
                    String vaApplyType = entry.getString("entryvacationtype.number");
                    if (!"3000_S".equalsIgnoreCase(vaApplyType) && "1020_S".equalsIgnoreCase(qtTypeNumber)) {
                        continue;
                    }
                    //开始结束时间的年都小于查询日期年份
                    if (!(startYear < nowYear && endYear < nowYear)) {
                        VaApplyDay = VaApplyDay.add(sumVaTimeDay);
                    }
                }
                if (standardDay.compareTo(VaApplyDay) > 0) {
                    sumVaApplyDay = sumVaApplyDay.add(VaApplyDay);
                } else {
                    sumVaApplyDay = sumVaApplyDay.add(standardDay);
                }
            }
            BigDecimal finalAvailable = sumStandardAvailable.subtract(sumVaApplyDay);
            //计算需要扣除的已冻结天数
            Date subtractDate = subtractYears(now, 1);
            BigDecimal previousPeriodBalance = returnDataDetail.get("previousPeriodBalance");
            logger.info("subtractDate={}", DateFormatUtils.format(subtractDate, "YYYY-MM-dd HH:mm:ss"));
            BigDecimal sumNeedCutFreezeDays = calculateNeedCutFreezeDays(key, subtractDate, qtTypeNumber);
            logger.info("calculateNeedCutFreezeDays={} previousPeriodBalance={}", sumNeedCutFreezeDays, previousPeriodBalance);

            //如果上期结余天数小于冻结天数 就继续扣调整天数
            logger.info("adjustValue0 returnData={}", returnData);
            if (sumNeedCutFreezeDays.compareTo(previousPeriodBalance) > 0) {
                returnDataDetail.put("previousPeriodBalance", BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
                returnDataDetail.put("adjustValue", sumOwnValue.subtract(finalAvailable).subtract(sumNeedCutFreezeDays.subtract(previousPeriodBalance)).setScale(2, RoundingMode.DOWN));
            } else {
                returnDataDetail.put("previousPeriodBalance", previousPeriodBalance.subtract(sumNeedCutFreezeDays).setScale(2, RoundingMode.DOWN));
                returnDataDetail.put("adjustValue", sumOwnValue.subtract(finalAvailable).setScale(2, RoundingMode.DOWN));
            }

            //封装上期结余数据
            returnData.put(key, returnDataDetail);
            logger.info("adjustValue1 returnData={}", returnData);
            standardSearchVaDetail.clear();
        }
    }

    /**
     * 根据过滤后的休假单据再次过滤标品查出的休假单数据，得到最终需要参与计算的可用天数数据。
     *
     * @param standardSearchVaDetail 标品逻辑查出的休假单据信息
     * @param vaApplyData            第一次过滤后的休假信息
     */
    private static void filterStandardBill(Map<String, BigDecimal> standardSearchVaDetail, List<DynamicObject> vaApplyData) {
        List<String> billNoList = vaApplyData.stream().map(x -> x.getString("billno")).collect(Collectors.toList());
        logger.info("standardSearchVaDetail0={}  billNoList={}", standardSearchVaDetail, billNoList);
        //删除不需要参与计算的单据
        standardSearchVaDetail.keySet().removeIf(key -> !billNoList.contains(key));
        logger.info("standardSearchVaDetail1={}", standardSearchVaDetail);
    }

    /**
     * 根据日期获取前years年的当前日期
     *
     * @param date  日期
     * @param years 前几年
     * @return 日期
     */
    public static Date subtractYears(Date date, int years) {
        // 创建一个 Calendar 实例
        Calendar calendar = Calendar.getInstance();

        // 将 Date 对象设置为需要操作的日期
        calendar.setTime(date);

        // 将日期减去指定的年数
        calendar.add(Calendar.YEAR, -years);

        // 获取减去指定年数后的日期
        return calendar.getTime();
    }

    /**
     * 计算上期结余数据
     *
     * @param qtLineDetailsMap 定额明细根据工号分类Map
     * @param returnData       每个人的年假信息 key->工号 value->年假信息Map
     * @param now              查询时间
     * @param allVaApplyMap    所有休假
     * @param qtTypeNumber     定额类型
     */
    private static void calculatePreviousPeriodBalance(Map<String, List<DynamicObject>> qtLineDetailsMap,
                                                       HashMap<String, Map<String, BigDecimal>> returnData,
                                                       Date now,
                                                       HashMap<String, ArrayList<DynamicObject>> allVaApplyMap, String qtTypeNumber) {

        for (String key : qtLineDetailsMap.keySet()) {
            //收集满足条件的休假数据

            ArrayList<DynamicObject> vaApplyList = new ArrayList<>();

            HashMap<String, BigDecimal> returnDataDetail = new HashMap<>();
            //初始化值
            initReturnDataDetail(returnDataDetail);
            //按照标品逻辑查询出的休假信息。
            Map<String, BigDecimal> standardSearchVaDetail = new HashMap<>();
            //累加享有时长
            BigDecimal sumOwnValue = new BigDecimal("0");
            //当前人的定额数据
            List<DynamicObject> value = qtLineDetailsMap.get(key);
            for (DynamicObject data : value) {
                //1.来源系统生成(DT-000) 状态为生效
                String source = data.getString("source");
                String busStatus = data.getString("busstatus");
                boolean isSystemProduce = ("DT-000".equalsIgnoreCase(source) || "DT-005".equalsIgnoreCase(source)) && "0".equalsIgnoreCase(busStatus);

                //2.“使用开始日期”的年份<“查询日期”的年份
                Date useStartDate = data.getDate("usestartdate");
                boolean isUsedStartYearBeforeNow = Integer.parseInt(DateFormatUtils.format(useStartDate, "yyyy"))
                        < Integer.parseInt(DateFormatUtils.format(now, "yyyy"));
                //3.“使用结束日期”≥“查询日期”
                Date useEndDate = data.getDate("useenddate");
                boolean isLargeOrEqualsNow = useEndDate.after(now) || useEndDate.equals(now);

                if (isSystemProduce && isUsedStartYearBeforeNow && isLargeOrEqualsNow) {
                    BigDecimal ownValue = data.getBigDecimal("ownvalue");
                    sumOwnValue = sumOwnValue.add(ownValue);
                    logger.info("calculatePreviousPeriodBalance key={} ownValue={} sumOwnValue={}",
                            key, ownValue, sumOwnValue);
                    //查询休假数据
                    DynamicObject[] searchVaApplyInfo = searchVaApplyData(data, standardSearchVaDetail);
                    vaApplyList.addAll(Arrays.asList(searchVaApplyInfo));
                }
            }

            //过滤掉不需要的休假数据
            filterVaApplyInfo(vaApplyList);
            //保存一份数据用于查询已休天数
            ArrayList<DynamicObject> vaApplyListCurrent = new ArrayList<>(vaApplyList);
            allVaApplyMap.put(key, vaApplyListCurrent);
            logger.info("calculatePreviousPeriodBalance sumOwnValue={} vaApplyList={}", sumOwnValue,
                    vaApplyList.stream().map(x -> x.getString("billno")).collect(Collectors.toList()));

            //【过滤掉冻结的数据(状态为审批中) 剩下的都是已使用的数据】
            List<DynamicObject> vaApplyData = vaApplyList.stream()
                    .filter(data -> !"D".equalsIgnoreCase(data.getString("billstatus")))
                    .collect(Collectors.toList());

            //过滤掉标品中不需要参与计算的单据 得出最终需要参与计算的可用天数数据
            filterStandardBill(standardSearchVaDetail, vaApplyData);
            //计算出第一版可用天数（未扣除其他年份）
            BigDecimal sumStandardAvailable = standardSearchVaDetail.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal vaApplyDay;
            //【累加“休假发生时间的年份”<“查询日期”的年份】
            BigDecimal sumVaApplyDay = new BigDecimal("0");
            for (DynamicObject data : vaApplyData) {
                //单据总时长
                vaApplyDay = BigDecimal.ZERO;
                DynamicObjectCollection entryEntity = data.getDynamicObjectCollection("entryentity");
                String billNo = data.getString("billno");
                //标品单据计算值
                BigDecimal standardDay = standardSearchVaDetail.get(billNo);
                for (DynamicObject entry : entryEntity) {
                    Date entryStartDate = entry.getDate("entrystartdate");
                    Date entryEndDate = entry.getDate("entryenddate");
                    int startYear = Integer.parseInt(DateFormatUtils.format(entryStartDate, "yyyy"));
                    int endYear = Integer.parseInt(DateFormatUtils.format(entryEndDate, "yyyy"));
                    int nowYear = Integer.parseInt(DateFormatUtils.format(now, "yyyy"));
                    //单据分录时长
                    BigDecimal entryVaTimeDay = entry.getBigDecimal("entryvatimeday");
                    logger.info("calculatePreviousPeriodBalance startYear={} endYear={} nowYear={} entryvatimeday={}",
                            startYear, endYear, nowYear, entryVaTimeDay);
                    //当前不是休假类型不是年假就跳过
                    String vaApplyType = entry.getString("entryvacationtype.number");
                    if (!"3000_S".equalsIgnoreCase(vaApplyType) && "1020_S".equalsIgnoreCase(qtTypeNumber)) {
                        continue;
                    }
                    //开始结束时间的年都小于查询日期年份
                    if (!(startYear < nowYear && endYear < nowYear)) {
                        vaApplyDay = vaApplyDay.add(entryVaTimeDay);
                    }
                }
                //存在单据拆单情况-此时标准计算时长<单据时长情况 ->取标准计算时长
                if (standardDay.compareTo(vaApplyDay) > 0) {
                    sumVaApplyDay = sumVaApplyDay.add(vaApplyDay);
                } else {
                    sumVaApplyDay = sumVaApplyDay.add(standardDay);
                }
            }
            BigDecimal finalAvailable = sumStandardAvailable.subtract(sumVaApplyDay);
            //封装上期结余数据
            returnDataDetail.put("previousPeriodBalance", sumOwnValue.subtract(finalAvailable).setScale(2, RoundingMode.DOWN));
            returnData.put(key, returnDataDetail);
            logger.info("calculatePreviousPeriodBalance returnData={}", returnData);
            standardSearchVaDetail.clear();
        }
    }

    /**
     * 计算上期结余/调整时长需要扣减的冻结天数
     *
     * @param key          工号
     * @param now          查询时间
     * @param qtTypeNumber 定额类型
     */
    private static BigDecimal calculateNeedCutFreezeDays(String key, Date now, String qtTypeNumber) {
        BigDecimal sumNeedCutFreezeDays = new BigDecimal("0");

        QFilter[] qf = new QFilter("personid.number", QCP.equals, key)
                .and("attfile.atttag.number", QCP.equals, "1010_S")
                .and("attfile.iscurrentversion", QCP.equals, true)
                .toArray();
        DynamicObject[] info = BusinessDataServiceHelper.load("xyjt_wtabm_vaapply_all",
                "billstatus,billno,xyjt_sumvatimeday,entryentity.entrystartdate,entryentity.entryenddate,entryentity.entryvatimeday,entryentity.entryvacationtype",
                qf);
        List<DynamicObject> value = Arrays.asList(info);

        //【过滤出冻结数据】
        List<DynamicObject> vaApplyInfo = value.stream()
                .filter(data -> "D".equalsIgnoreCase(data.getString("billstatus")) && data.getString("billno").startsWith("LE"))
                .collect(Collectors.toList());

        logger.info("calculateNeedCutFreezeDays billNoList={}",
                vaApplyInfo.stream().map(x -> x.getString("billno")).collect(Collectors.toList()));

        //【累加“休假发生时间的年份”<“查询日期”的年份】
        for (DynamicObject data : vaApplyInfo) {
            DynamicObjectCollection entryEntity = data.getDynamicObjectCollection("entryentity");
            for (DynamicObject entry : entryEntity) {
                Date entryStartDate = entry.getDate("entrystartdate");
                Date entryEndDate = entry.getDate("entryenddate");
                int startYear = Integer.parseInt(DateFormatUtils.format(entryStartDate, "yyyy"));
                int endYear = Integer.parseInt(DateFormatUtils.format(entryEndDate, "yyyy"));
                int nowYear = Integer.parseInt(DateFormatUtils.format(now, "yyyy"));
                BigDecimal sumVaTimeDay = entry.getBigDecimal("entryvatimeday");
                logger.info("calculateNeedCutFreezeDays startYear={} endYear={} nowYear={} sumVaTimeDay={}",
                        startYear, endYear, nowYear, sumVaTimeDay);
                //当前不是休假类型不是年假就跳过
                String vaApplyType = entry.getString("entryvacationtype.number");
                if (!"3000_S".equalsIgnoreCase(vaApplyType) && "1020_S".equalsIgnoreCase(qtTypeNumber)) {
                    continue;
                }
                //开始结束时间的年都小于查询日期年份
                if (startYear == nowYear && endYear == nowYear) {
                    sumNeedCutFreezeDays = sumNeedCutFreezeDays.add(sumVaTimeDay);
                }
            }
        }
        return sumNeedCutFreezeDays;
    }

    /**
     * 初始化每个人的年假信息 默认为0
     *
     * @param returnDataDetail 年假信息map
     */
    private static void initReturnDataDetail(HashMap<String, BigDecimal> returnDataDetail) {
        //上期结余
        returnDataDetail.put("previousPeriodBalance", BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
        //调整时长
        returnDataDetail.put("adjustValue", BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
        //本期享有
        returnDataDetail.put("currentPeriodEnjoy", BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
        //已休天数
        returnDataDetail.put("alreadyVaApplyDays", BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
        //冻结天数
        returnDataDetail.put("freezeDays", BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
        //可用天数
        returnDataDetail.put("availableDays", BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));
    }

    /**
     * 根据一定的规则过滤掉不需要的休假数据
     *
     * @param vaApplyList 休假数据list
     */
    private static void filterVaApplyInfo(ArrayList<DynamicObject> vaApplyList) {
        //【过滤失效单据】
        ArrayList<String> needRemoveBill = new ArrayList<>();
        vaApplyList.forEach(data -> {
            String billStatus = data.getString("billstatus");
            //失效的单据
            if ("F".equalsIgnoreCase(billStatus)) {
                needRemoveBill.add(data.getString("billno"));
            }
        });
        logger.info("needRemoveBill={}", needRemoveBill);

        Iterator<DynamicObject> iterator = vaApplyList.iterator();
        while (iterator.hasNext()) {
            DynamicObject next = iterator.next();
            String billNo = next.getString("billno");
            if (needRemoveBill.contains(billNo)) {
                iterator.remove();
            }
        }
        logger.info("remainBill={}", vaApplyList);
    }

    /**
     * 根据定额明细的【已用项目】查询出所有休假单
     *
     * @param data 定额明细数据包
     * @return 休假数据包
     */
//    private static DynamicObject[] searchVaApplyData(DynamicObject data) {
//
//        long usedId = data.getLong("usedvalueid.id");
//
//        //通过【定额明细】的已用项目ID查询 【定额考勤项目明细】的bid->汇总表明细分录id
//        DynamicObject[] quotaDetail = BusinessDataServiceHelper.load("wtte_quotadetail", "bid",
//                new QFilter[]{new QFilter("id", QCP.in, usedId)});
//        List<Long> bidList = Arrays.stream(quotaDetail).map(x -> x.getLong("bid")).collect(Collectors.toList());
//
//        //通过【定额考勤项目明细】的bid查询 【交易记录明细-查询】的定额汇总明细fid
//        DynamicObject[] qtBedDetail = BusinessDataServiceHelper.load("wtp_qtbeddetail", "fid",
//                new QFilter[]{new QFilter("qtsummarydetail.id", QCP.in, bidList)});
//        List<Long> idList = Arrays.stream(qtBedDetail).map(x -> x.getLong("fid")).collect(Collectors.toList());
//
//        //通过【交易记录-单据分录】的fid查询 【交易记录明细-查询】的定额汇总明细ID
//        DynamicObject[] qtBillEntryDeal = BusinessDataServiceHelper.load("wtp_qtbillentrydeal", "pid",
//                new QFilter[]{new QFilter("id", QCP.in, idList)});
//        List<Long> pidList = Arrays.stream(qtBillEntryDeal).map(x -> x.getLong("pid")).collect(Collectors.toList());
//
//        //通过【交易记录-单据】的id查询 【交易记录明细-查询】的定额汇总明细ID
//        DynamicObject[] qtBillDeal = BusinessDataServiceHelper.load("wtp_qtbilldeal", "billid",
//                new QFilter[]{new QFilter("id", QCP.in, pidList)});
//        List<Long> billidList = Arrays.stream(qtBillDeal).map(x -> x.getLong("billid")).collect(Collectors.toList());
//
//        //查询休假单数据
//        return BusinessDataServiceHelper.load("wtabm_vaapply",
//                "billstatus,billno,xyjt_sumvatimeday,entryentity.entrystartdate,entryentity.entryenddate",
//                new QFilter[]{new QFilter("id", QCP.in, billidList)});
//
//    }

    /**
     * 查询休假数据
     *
     * @param type                   默认3
     * @param lineToDetailMap        key->定额明细ID value->已用项目ID
     * @param standardSearchVaDetail 标品查询休假信息明细
     */
    public static void getDataForUsed(int type, Map<Long, Long> lineToDetailMap, List<String> allBillList,
                                      Map<String, BigDecimal> standardSearchVaDetail) {

        List<Long> lineIds = Lists.newArrayListWithExpectedSize(lineToDetailMap.size());

        for (Object aLong : lineToDetailMap.keySet()) {
            lineIds.add(Long.parseLong(String.valueOf(aLong)));
        }
        logger.info("getDataForUsed:lineIds = {}", lineIds);
        //根据标品逻辑获取年假休假数据
        Map<Long, List<EffectiveEntityVoExt>> billMap = QTDealRecordDBService.loadBillDeal(lineIds, type);

        for (Entry<Long, List<EffectiveEntityVoExt>> entry : billMap.entrySet()) {
            List<EffectiveEntityVoExt> billList = entry.getValue();
            for (EffectiveEntityVoExt effectiveEntityVoExt : billList) {
                if (standardSearchVaDetail.containsKey(effectiveEntityVoExt.getBillno())) {
                    BigDecimal value = standardSearchVaDetail.get(effectiveEntityVoExt.getBillno());
                    value = value.add(effectiveEntityVoExt.getValue());
                    standardSearchVaDetail.put(effectiveEntityVoExt.getBillno(), value);
                } else {
                    standardSearchVaDetail.put(effectiveEntityVoExt.getBillno(), effectiveEntityVoExt.getValue());
                }
            }
            allBillList.addAll(billList.stream().map(EffectiveEntityVo::getBillno).collect(Collectors.toList()));
            logger.info("allBillList={}", allBillList);
        }
    }

    /**
     * 查询年假数据
     *
     * @param data                   定额明细行数据
     * @param standardSearchVaDetail 标品查询休假数据明细
     * @return 休假单据数据包
     */
    private static DynamicObject[] searchVaApplyData(DynamicObject data, Map<String, BigDecimal> standardSearchVaDetail) {

        List<String> vaApplyInfo = new ArrayList<>();
        long usedId = data.getLong("usedvalueid.id");
        Map<Long, Long> lineToDetailMap = Maps.newHashMapWithExpectedSize(1);
        lineToDetailMap.put(data.getLong("id"), usedId);
        getDataForUsed(3, lineToDetailMap, vaApplyInfo, standardSearchVaDetail);
        logger.info("getDataForUsed:lineToDetailMap = {}, standardSearchVaDetail = {}", lineToDetailMap, standardSearchVaDetail);
        //重新查出休假数据信息
        QFilter[] qf = new QFilter("billno", QCP.in, vaApplyInfo)
//                .and("attfile.atttag.number", QCP.equals, "1010_S")
                .and("attfile.iscurrentversion", QCP.equals, true)
                .toArray();
        DynamicObject[] allVaApplyInfo = BusinessDataServiceHelper.load("xyjt_wtabm_vaapply_all",
                "billstatus,billno,xyjt_sumvatimeday,entryentity.entrystartdate,entryentity.entryenddate,entryentity.entryvatimeday,entryentity.entryvacationtype",
                qf);
        logger.info("allVaApplyInfo_billNo={}", Arrays.stream(allVaApplyInfo)
                .map(x -> x.getString("billno")).collect(Collectors.toList()));
        return allVaApplyInfo;
    }
}
