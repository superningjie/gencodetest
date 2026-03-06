package tdkw.hrmp.hrobs.common.hrobs.util;

import com.alibaba.fastjson.JSONObject;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.armor.core.util.StringUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.FilterInfo;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import tdkw.hrmp.hrobs.common.hrobs.QuoteSourceEnum;
import tdkw.hrmp.hrobs.common.hrobs.pojo.PersonQuotaVo;
import tdkw.hrmp.hrobs.common.hrobs.pojo.QuotaBillDetailVo;
import tdkw.hrmp.hrobs.common.hrobs.pojo.QuotaVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 获取年假报表工具类
 *
 * @author : xycaitt
 * @date : 2024/1/23 11:25
 */
public class QTReportUtil {

    private static final Log logger = LogFactory.getLog(QTReportUtil.class);

    /**
     * 获取员工的年假信息-根据各类条件
     *
     * @param numberList 员工考勤档案编号list
     * @return 考勤档案主键->员工定额列表
     */
    public LinkedHashMap<Long, PersonQuotaVo> getQueryInfo(List<String> numberList, FilterInfo filter) {
        long startTime = System.currentTimeMillis();
        DynamicObjectCollection typeList = filter.getDynamicObjectCollection("xyjt_qttypename");
        Date date;
        Date searchYear = filter.getDate("xyjt_searchyear") == null ? new Date() : filter.getDate("xyjt_searchyear");
        boolean isYearEnd = filter.getBoolean("xyjt_checkboxfield");
        if (isYearEnd && Objects.equals(DateTimeUtils.getNowYear(), DateTimeUtils.getNowYear(searchYear))) {
            //今年年底
            date = DateTimeUtils.getEndDayOfYear();
        } else if (!Objects.equals(DateTimeUtils.getNowYear(), DateTimeUtils.getNowYear(searchYear))) {
            //非今年取往年年底
            date = DateTimeUtils.getEndDayOfYear(searchYear);
        } else {
            //今年当前日期
            date = new Date();
        }
        String typeFilter = " ";
        if (typeList != null) {
            StringBuilder quotaType = new StringBuilder();
            typeList.forEach(type -> quotaType.append(",").append(type.getLong("id")));
            typeFilter = StringUtil.isNotBlank(String.valueOf(quotaType)) ? " and t_wtp_qtlinedetail.fqttypeid in (" + quotaType.substring(1) + ") " : " ";
        }

        if (CollectionUtils.isEmpty(numberList)) {
            return null;
        }
        LinkedHashMap<Long, PersonQuotaVo> returnData = new LinkedHashMap<>();
        String numbers = numberList.stream().map(o -> "'" + o + "'").collect(Collectors.joining(","));
        //根据考勤档案编号获取考勤数据和定额明细数据
        logger.info("QTReportUtil.getQueryInfo入参:numberList = {},filter = {},typeList = {},numbers = {},date = {}", numberList, filter, typeFilter, numbers, date);

        String personFilter = " and t_wtp_attfilebase.fnumber in (" + numbers + ")";
        String orderSql = " order by t_wtp_attfilebase.fpersonnum,t_wtp_qttype.fname";

        String querySql = "select t_wtp_attfilebase.fid as baseId,t_wtp_attfilebase.fpersonnum as number,t_wtp_qtlinedetail.fid as qtDetailId" +
                ",t_wtp_qtlinedetail.fsource as source,t_wtp_qtlinedetail.fusestartdate as useStartDate,t_wtp_qtlinedetail.fuseenddate as useEndDate" +
                ",t_wtp_qtlinedetail.fownvalue as ownValue,t_wtp_qtlinedetail.fownvalueid as ownItemId" +
                ",t_wtp_qtlinedetail.fusedvalue as usedValue,t_wtp_qtlinedetail.fusedvalueid as usedItemId,t_wtp_qtlinedetail.fpastvalue as invalidValue" +
                ",t_wtp_qtlinedetail.ffreezevalue as freezeValue,t_wtp_qtlinedetail.ffreezevalueid as freezeItemId" +
                ",t_wtp_qttype.fnumber as qtTypeName,t_wtp_qttype.fid as qtTypeId,t_wtp_qttype.funit as qtUnit,t_wtp_qtlinedetail.fdetailsourceid as detailSourceId" +
                " from t_wtp_attfilebase" +
                " left join t_wtp_qtlinedetail on t_wtp_qtlinedetail.fattfileboid = t_wtp_attfilebase.fid and t_wtp_qtlinedetail.fbusstatus = '0'" +
                " left join t_wtp_qttype on  t_wtp_qtlinedetail.fqttypeid = t_wtp_qttype.fid" +
                " where t_wtp_attfilebase.fiscurrentversion = '1' and t_wtp_qtlinedetail.fid is not null" +
                typeFilter + personFilter + orderSql;
        // 定额明细数据
        DataSet quotaDetailData = DB.queryDataSet(this.getClass().getName(), DBRoute.of("hr"), querySql, null).distinct();
        logger.info("定额计算查询,用时：{}", (System.currentTimeMillis() - startTime));
        Iterator<Row> iterator = quotaDetailData.iterator();
        PersonQuotaVo personQuota;
        QuotaVo quotaVo;
        while (iterator.hasNext()) {
            quotaVo = null;
            personQuota = null;
            Row current = iterator.next();
            logger.info("current = {}", current);
            Long id = current.getLong("baseId");
            Date useStartDate = current.getDate("useStartDate");
            Date useEndDate = current.getDate("useEndDate");
            String source = current.getString("source");
            BigDecimal ownValue = current.getBigDecimal("ownValue");
            //已用项目ID -> 已用值
            HashMap<String, BigDecimal> usedItemMap = new HashMap<>();
            usedItemMap.put(current.getLong("qtDetailId") + "-" + current.getLong("usedItemId"), current.getBigDecimal("usedValue"));
            HashMap<String, BigDecimal> freezeItemMap = new HashMap<>();
            freezeItemMap.put(current.getLong("qtDetailId") + "-" + current.getLong("freezeItemId"), current.getBigDecimal("freezeValue"));
            String qtTypeName = current.getString("qtTypeName");
            Long qtType = current.getLong("qtTypeId");
            String number = current.getString("number");
            BigDecimal invalidValue = current.getBigDecimal("invalidValue");
            logger.info("id = {},returnData = {}", id, JSONObject.toJSON(returnData));
            String detailSourceId = current.getString("detailSourceId");

            try {
                if (!"0".equals(detailSourceId) && (QuoteSourceEnum.CARRY_BU.getValue().equals(source) || QuoteSourceEnum.MANUAL.getValue().equals(source))) {
                    QFilter[] filters = new QFilter("id", QCP.equals, Long.valueOf(detailSourceId))
                            .and(new QFilter("busstatus", QCP.equals, '0'))
                            .toArray();
                    DynamicObject detail = BusinessDataServiceHelper.loadSingle("wtp_qtlinedetail",
                            "id,ownvalue,usedvalue,usedvalueid.id,freezevalue,freezevalueid.id", filters);
                    //跨BU取来源情况
                    if (detail != null) {
                        ownValue = detail.getBigDecimal("ownvalue");
                        usedItemMap.put(detail.getLong("id") + "-" + detail.getLong("usedvalueid.id"), detail.getBigDecimal("usedvalue"));
                        freezeItemMap.put(detail.getLong("id") + "-" + detail.getLong("freezevalueid.id"), detail.getBigDecimal("freezevalue"));
                    }
                    logger.info("detailSourceId = {}, details = {}", detailSourceId, detail);
                }
            } catch (Exception e) {
                logger.error("detailSourceId查询报错,{}", e.getMessage(), e);
            }
            if (returnData.containsKey(id)) {
                personQuota = returnData.get(id);
                if (personQuota != null && CollectionUtils.isNotEmpty(personQuota.getQuotaVoList())) {
                    for (QuotaVo vo : personQuota.getQuotaVoList()) {
                        if (qtTypeName.equals(vo.getQtTypeName())) {
                            quotaVo = vo;
                            break;
                        }
                    }
                }
                logger.info("containsKey :personQuota = {},quotaVo = {}", JSONObject.toJSON(personQuota), JSONObject.toJSON(quotaVo));
            }
            if (personQuota == null) {
                //初始化定额对象
                personQuota = new PersonQuotaVo();
                personQuota.setId(id);
                personQuota.setDate(date);
                personQuota.setNumber(number);
                personQuota.setQuotaVoList(new LinkedList<>());
                returnData.put(id, personQuota);
                logger.info("init:personQuota = {}", JSONObject.toJSON(personQuota));
            }
            if (quotaVo == null) {
                //初始化对象的定额类型 并加入列表
                quotaVo = new QuotaVo();
                quotaVo.setAdjustValue(BigDecimal.ZERO);
                quotaVo.setAlreadyVaApplyDays(BigDecimal.ZERO);
                quotaVo.setAvailableDays(BigDecimal.ZERO);
                quotaVo.setFreezeDays(BigDecimal.ZERO);
                quotaVo.setCurrentPeriodEnjoy(BigDecimal.ZERO);
                quotaVo.setPreviousPeriodBalance(BigDecimal.ZERO);
                quotaVo.setQtTypeName(qtTypeName);
                quotaVo.setQtType(qtType);
                quotaVo.setInvalidDays(BigDecimal.ZERO);
                quotaVo.setFreezeDetail(new ArrayList());
                quotaVo.setUsedDetail(new ArrayList());
                LinkedList<QuotaVo> quotaVoList = personQuota.getQuotaVoList();
                quotaVoList.add(quotaVo);
                personQuota.setQuotaVoList(quotaVoList);
                logger.info("init:quotaVo = {},quotaVoList = {}", JSONObject.toJSON(quotaVo), JSONObject.toJSON(quotaVoList));
            }
            logger.info("计算前: quotaVo = {},personQuota = {}", JSONObject.toJSON(quotaVo), JSONObject.toJSON(personQuota));
            //计算明细
            calculateDetail(personQuota, quotaVo, usedItemMap, ownValue, invalidValue, freezeItemMap, source, useStartDate, useEndDate, current.getLong("qtDetailId"));
            logger.info("计算后: quotaVo = {},personQuota = {},returnData = {}", JSONObject.toJSON(quotaVo), JSONObject.toJSON(personQuota), JSONObject.toJSON(returnData));
        }
        logger.info("定额计算结束,用时：{}", (System.currentTimeMillis() - startTime));
        return returnData;
    }


    public void calculateDetail(PersonQuotaVo personQuotaVo, QuotaVo qtDetail, HashMap<String, BigDecimal> usedItemMap, BigDecimal ownValue, BigDecimal invalidValue, HashMap<String, BigDecimal> freezeItemMap, String source, Date useStartDate, Date useEndDate, Long qtDetailId) {
        //上期结余
        BigDecimal previousPeriodBalance = qtDetail.getPreviousPeriodBalance();
        //调整时长
        BigDecimal adjustValue = qtDetail.getAdjustValue();
        //本期享有
        BigDecimal currentPeriodEnjoy = qtDetail.getCurrentPeriodEnjoy();
        //已用时长
        BigDecimal alreadyVaApplyDays = qtDetail.getAlreadyVaApplyDays();
        //冻结时长
        BigDecimal freezeDays = qtDetail.getFreezeDays();
        //失效时长
        BigDecimal invalidDays = qtDetail.getInvalidDays();
        String calDate = DateFormatUtils.format(personQuotaVo.getDate(), "yyyy-MM-dd");
        String startDate = DateFormatUtils.format(useStartDate, "yyyy-MM-dd");
        String endDate = DateFormatUtils.format(useEndDate, "yyyy-MM-dd");
        //可用时长
        BigDecimal availableDays;
        int calYear = Integer.parseInt(DateFormatUtils.format(personQuotaVo.getDate(), "yyyy"));

        Map<String, BigDecimal> tempMap = new HashMap<>();
        //计算今年已休和冻结天数
        calculateItemMap(tempMap, "tempAlreadyVaApplyDays", usedItemMap, personQuotaVo, qtDetail, qtDetailId, calYear, true, false);
        calculateItemMap(tempMap, "tempFreezeDays", freezeItemMap, personQuotaVo, qtDetail, qtDetailId, calYear, true, true);

        //计算本期享有 -“来源”为“系统生成” && “使用开始日期”的年份=“查询日期”的年份 && “使用开始日期”≤“查询日期”≤“使用结束日期”
        if ((QuoteSourceEnum.SYS.getValue().equals(source) || QuoteSourceEnum.CARRY_BU.getValue().equals(source))
                && Integer.parseInt(DateFormatUtils.format(useStartDate, "yyyy")) == calYear
                && startDate.compareTo(calDate) <= 0 && endDate.compareTo(calDate) >= 0) {
            //本期享有
            currentPeriodEnjoy = currentPeriodEnjoy.add(ownValue);
            //累加今年已休和冻结天数
            alreadyVaApplyDays = alreadyVaApplyDays.add(tempMap.get("tempAlreadyVaApplyDays"));
            freezeDays = freezeDays.add(tempMap.get("tempFreezeDays"));
        }

        //计算上期结余 - “来源”为“系统生成” && “使用开始日期”的年份<“查询日期”的年份 && “使用结束日期”≥“查询日期” 扣减前年已用和冻结
        if ((QuoteSourceEnum.SYS.getValue().equals(source) || QuoteSourceEnum.CARRY_BU.getValue().equals(source))
                && Integer.parseInt(DateFormatUtils.format(useStartDate, "yyyy")) < calYear
                && endDate.compareTo(calDate) >= 0) {
            logger.info("扣减前：previousPeriodBalance = {},ownValue = {}", previousPeriodBalance, ownValue);
            //上期结余-享有
            previousPeriodBalance = previousPeriodBalance.add(ownValue);
            //累加今年已休和冻结天数
            alreadyVaApplyDays = alreadyVaApplyDays.add(tempMap.get("tempAlreadyVaApplyDays"));
            freezeDays = freezeDays.add(tempMap.get("tempFreezeDays"));
            //计算往年已休和冻结天数
            calculateItemMap(tempMap, "tempAlreadyVaApplyDays", usedItemMap, personQuotaVo, qtDetail, qtDetailId, calYear, false, false);
            calculateItemMap(tempMap, "tempFreezeDays", freezeItemMap, personQuotaVo, qtDetail, qtDetailId, calYear, false, true);
            //上期结余-享有扣减往年已休和冻结天数
            previousPeriodBalance = previousPeriodBalance.subtract(tempMap.get("tempAlreadyVaApplyDays")).subtract(tempMap.get("tempFreezeDays"));
            logger.info("扣减后：previousPeriodBalance = {},usedValue = {},freezeValue = {}", previousPeriodBalance, tempMap.get("tempAlreadyVaApplyDays"), tempMap.get("tempFreezeDays"));
        }

        //计算调整时长 待扣减已用和冻结 - “来源”为“手动生成” && “使用开始日期”≤“查询日期”≤“使用结束日期”
        if (QuoteSourceEnum.MANUAL.getValue().equals(source)
                && startDate.compareTo(calDate) <= 0 && endDate.compareTo(calDate) >= 0) {
            logger.info("扣减前：adjustValue = {},ownValue = {}", adjustValue, ownValue);
            //调整时长-享有
            adjustValue = adjustValue.add(ownValue);
            //累加今年已休和冻结天数
            alreadyVaApplyDays = alreadyVaApplyDays.add(tempMap.get("tempAlreadyVaApplyDays"));
            freezeDays = freezeDays.add(tempMap.get("tempFreezeDays"));
            //计算往年已休和冻结天数
            calculateItemMap(tempMap, "tempAlreadyVaApplyDays", usedItemMap, personQuotaVo, qtDetail, qtDetailId, calYear, false, false);
            calculateItemMap(tempMap, "tempFreezeDays", freezeItemMap, personQuotaVo, qtDetail, qtDetailId, calYear, false, true);
            //调整时长-享有扣减往年已休和冻结天数
            adjustValue = adjustValue.subtract(tempMap.get("tempAlreadyVaApplyDays")).subtract(tempMap.get("tempFreezeDays"));
            logger.info("扣减后：adjustValue = {},usedValue = {},freezeValue = {}", adjustValue, tempMap.get("tempAlreadyVaApplyDays"), tempMap.get("tempFreezeDays"));
        }

        if (startDate.compareTo(calDate) <= 0 && endDate.compareTo(calDate) >= 0) {
            invalidDays = invalidDays.add(invalidValue);
        }
        //计算可用时长
        availableDays = previousPeriodBalance.add(adjustValue).add(currentPeriodEnjoy).subtract(alreadyVaApplyDays).subtract(freezeDays);
        qtDetail.setAvailableDays(availableDays);
        qtDetail.setPreviousPeriodBalance(previousPeriodBalance);
        qtDetail.setAdjustValue(adjustValue);
        qtDetail.setCurrentPeriodEnjoy(currentPeriodEnjoy);
        qtDetail.setAlreadyVaApplyDays(alreadyVaApplyDays);
        qtDetail.setFreezeDays(freezeDays);
        qtDetail.setInvalidDays(invalidDays);
    }

    /**
     * 根据项目ID计算对应值
     *
     * @param tempMap       返回对象
     * @param field         赋值字段
     * @param itemMap       项目MAP
     * @param personQuotaVo 定额信息
     * @param qtDetail      定额明细信息
     * @param qtDetailId    定额明细ID
     * @param calYear       计算年份
     * @param isCurrentYear 是否当前年
     * @param isFreeze      是否冻结
     */
    public void calculateItemMap(Map<String, BigDecimal> tempMap, String field, Map<String, BigDecimal> itemMap, PersonQuotaVo personQuotaVo, QuotaVo qtDetail, Long qtDetailId, int calYear, Boolean isCurrentYear, Boolean isFreeze) {
        tempMap.put(field, BigDecimal.ZERO);
        itemMap.forEach((key, value) -> {
            long itemId = Long.parseLong(key.split("-")[1]);
            logger.info("key = {},itemId = {}", key, itemId);

            if (BigDecimal.ZERO.compareTo(value) != 0 && itemId != 0) {
                BigDecimal days = tempMap.get(field);
                logger.info("项目和值不为空情况，计算前：项目ID = {},值 = {},累计值 = {}", itemId, value, days);
                //计算已用 - 年份==当前计算年份
                days = days.add(getValueByItemId(personQuotaVo.getId(), itemId, calYear, isCurrentYear, qtDetail, isFreeze));
                tempMap.put(field, days);
                logger.info("项目和值不为空情况，计算后：项目ID = {},累计值 = {}", itemId, days);
            } else if (BigDecimal.ZERO.compareTo(value) != 0 && itemId == 0) {
                BigDecimal days = tempMap.get(field);
                logger.info("值不为空项目为空情况，计算前：项目ID = {},值 = {},累计值 = {}", itemId, value, days);
                //计算已用 - 年份==当前计算年份
                days = days.add(getValueByQtDetailId(personQuotaVo.getId(), qtDetailId, calYear, isCurrentYear, qtDetail, isFreeze));
                tempMap.put(field, days);
                logger.info("值不为空项目为空情况，计算后：项目ID = {},累计值 = {}", itemId, days);
            }
        });
    }


    /**
     * 根据已用项目ID获取实际已用时长
     *
     * @param itemId
     * @param calYear
     */
    public BigDecimal getValueByItemId(Long baseId, Long itemId, int calYear, boolean isCurrentYear, QuotaVo quotaVo, boolean isFreeze) {
        //根据已用项目ID获取已用单据数据
        if (itemId == 0L) {
            return BigDecimal.ZERO;
        }
        BigDecimal sumUsed = BigDecimal.ZERO;
        String billStatus = isFreeze ? "D" : "C";
        String querySql = "select t_wtte_quotadetail.fattitemvid as projectId,t_wtp_qtbillentrydeal.fapplyunit,t_wtp_qtbeddetail.fdapplyvalue as applyValue,t_wtp_qtbillentrydeal.fstartdate as startDate,t_wtp_qtbillentrydeal.fenddate  as endDate" +
                " ,t_wtabm_vaapply.fbillno as billNo" +
                " from t_wtte_quotadetail" +
                " left join t_wtp_qtbeddetail on t_wtp_qtbeddetail.fqtsummarydetailid = t_wtte_quotadetail.fbid" +
                " left join t_wtp_qtbillentrydeal on t_wtp_qtbillentrydeal.fid = t_wtp_qtbeddetail.fid" +
                " left join t_wtp_qtbilldeal on t_wtp_qtbilldeal.fid =  t_wtp_qtbillentrydeal.fpid" +
                " left join t_wtabm_vaapply on t_wtabm_vaapply.fid = t_wtp_qtbilldeal.fbillid" +
                " where t_wtte_quotadetail.fid =" + itemId + " and t_wtabm_vaapply.fbillstatus = '" + billStatus + "'";
        // 项目(已用/项目)明细数据
        DataSet usedItemDetailData = DB.queryDataSet(this.getClass().getName(), DBRoute.of("hr"), querySql, null);
        for (Row row : usedItemDetailData) {
            logger.info("usedItemDetailIterator = {}", row);
            BigDecimal applyValue = row.getBigDecimal("applyValue");
            String billNo = row.getString("billNo");
            String unit = row.getString("fapplyunit");
            int startYear = Integer.parseInt(DateFormatUtils.format(row.getDate("startDate"), "yyyy"));
            int endYear = Integer.parseInt(DateFormatUtils.format(row.getDate("endDate"), "yyyy"));
            //是否当年
            if (isCurrentYear) {
                QuotaBillDetailVo vo = new QuotaBillDetailVo();
                vo.setBeginDate(row.getDate("startDate"));
                vo.setEndDate(row.getDate("endDate"));
                vo.setValue(applyValue);
                vo.setBillNo(billNo);
                vo.setId(baseId);
                vo.setUnit(unit);
                //考勤项目ID
                vo.setProjectId(row.getLong("projectId"));
                vo.setQuoteTypeId(quotaVo.getQtType());
                if (calYear == startYear && endYear == calYear) {
                    //如果都是当年，则直接取定额计算值
                    sumUsed = sumUsed.add(applyValue);
                    if (isFreeze) {
                        quotaVo.getFreezeDetail().add(vo);
                    } else {
                        quotaVo.getUsedDetail().add(vo);
                    }
                } else if (calYear == startYear || calYear == endYear) {
                    //如果有存在部分当年的，则需要结合单据扣减非当年的 todo
//                    BigDecimal sumNeedCutDays = getBillDays(billNo, calYear, quotaVo.getQtTypeName(), isCurrentYear);
//                    sumUsed = applyValue.compareTo(sumNeedCutDays) > 0 ? applyValue.subtract(sumNeedCutDays) : BigDecimal.ZERO;
                }
            } else {
//                //要找非当年且之前的已用数据
                if (calYear > startYear && calYear > endYear) {
                    //如果都是非当年的，则直接取定额计算值
                    sumUsed = sumUsed.add(applyValue);
                } else if (calYear != startYear || endYear != calYear) {
                    //如果有存在部分非当年的，则需要结合单据扣减 todo
//                    BigDecimal sumNeedCutDays = getBillDays(billNo, calYear, quotaVo.getQtTypeName(), !isCurrentYear);
//                    sumUsed = applyValue.compareTo(sumNeedCutDays) > 0 ? applyValue.subtract(sumNeedCutDays) : BigDecimal.ZERO;
                }
            }
        }
        return sumUsed;
    }


    /**
     * 根据定额明细ID获取实际已用时长
     *
     * @param qtDetailId
     * @param calYear
     */
    public BigDecimal getValueByQtDetailId(Long baseId, Long qtDetailId, int calYear, boolean isCurrentYear, QuotaVo quotaVo, boolean isFreeze) {
        //根据定额明细ID获取交易单据数据
        BigDecimal sumUsed = BigDecimal.ZERO;
        String billStatus = isFreeze ? "D" : "C";
        String querySql = "select t_wtp_qtbeddetail.fqtsummarydetailid,t_wtp_qtbillentrydeal.fapplyunit,t_wtp_qtbeddetail.fdapplyvalue as applyValue,t_wtp_qtbillentrydeal.fstartdate as startDate,t_wtp_qtbillentrydeal.fenddate  as endDate" +
                " ,t_wtabm_vaapply.fbillno as billNo" +
                " from  t_wtp_qtbeddetail " +
                " left join t_wtp_qtbillentrydeal on t_wtp_qtbillentrydeal.fid = t_wtp_qtbeddetail.fid" +
                " left join t_wtp_qtbilldeal on t_wtp_qtbilldeal.fid =  t_wtp_qtbillentrydeal.fpid" +
                " left join t_wtabm_vaapply on t_wtabm_vaapply.fid = t_wtp_qtbilldeal.fbillid" +
                " where t_wtp_qtbeddetail.fqtsummarydetailid  =" + qtDetailId + " and t_wtabm_vaapply.fbillstatus = '" + billStatus + "'";
        // 项目(已用/项目)明细数据
        DataSet usedItemDetailData = DB.queryDataSet(this.getClass().getName(), DBRoute.of("hr"), querySql, null);
        for (Row row : usedItemDetailData) {
            logger.info("getValueByQtDetailId.usedItemDetailIterator = {}", row);
            BigDecimal applyValue = row.getBigDecimal("applyValue");
            String billNo = row.getString("billNo");
            String unit = row.getString("fapplyunit");
            int startYear = Integer.parseInt(DateFormatUtils.format(row.getDate("startDate"), "yyyy"));
            int endYear = Integer.parseInt(DateFormatUtils.format(row.getDate("endDate"), "yyyy"));
            //是否当年
            if (isCurrentYear) {
                QuotaBillDetailVo vo = new QuotaBillDetailVo();
                vo.setBeginDate(row.getDate("startDate"));
                vo.setEndDate(row.getDate("endDate"));
                vo.setValue(applyValue);
                vo.setBillNo(billNo);
                vo.setId(baseId);
                vo.setUnit(unit);
                //考勤项目ID todo
//                vo.setProjectId(row.getLong("projectId"));
                vo.setQuoteTypeId(quotaVo.getQtType());
                if (calYear == startYear && endYear == calYear) {
                    //如果都是当年，则直接取定额计算值
                    sumUsed = sumUsed.add(applyValue);
                    if (isFreeze) {
                        quotaVo.getFreezeDetail().add(vo);
                    } else {
                        quotaVo.getUsedDetail().add(vo);
                    }
                } else if (calYear == startYear || calYear == endYear) {
                    //如果有存在部分当年的，则需要结合单据扣减非当年的 todo
//                    BigDecimal sumNeedCutDays = getBillDays(billNo, calYear, quotaVo.getQtTypeName(), isCurrentYear);
//                    sumUsed = applyValue.compareTo(sumNeedCutDays) > 0 ? applyValue.subtract(sumNeedCutDays) : BigDecimal.ZERO;
                }
            } else {
//                //要找非当年且之前的已用数据
                if (calYear > startYear && calYear > endYear) {
                    //如果都是非当年的，则直接取定额计算值
                    sumUsed = sumUsed.add(applyValue);
                } else if (calYear != startYear || endYear != calYear) {
                    //如果有存在部分非当年的，则需要结合单据扣减 todo
//                    BigDecimal sumNeedCutDays = getBillDays(billNo, calYear, quotaVo.getQtTypeName(), !isCurrentYear);
//                    sumUsed = applyValue.compareTo(sumNeedCutDays) > 0 ? applyValue.subtract(sumNeedCutDays) : BigDecimal.ZERO;
                }
            }
        }
        return sumUsed;
    }

    /**
     * 获取非计算年份的单据时长
     *
     * @param billNo  单据编号
     * @param calYear 计算年份
     * @param qtType  年假类型
     * @return
     */
    public BigDecimal getBillDays(String billNo, int calYear, String qtType, boolean isCurrentYear) {
        // 休假数据
        BigDecimal sumNeedCutDays = BigDecimal.ZERO;
        QFilter[] qf = new QFilter("billno", QCP.equals, billNo).toArray();
        DynamicObject[] info = BusinessDataServiceHelper.load("xyjt_wtabm_vaapply_all",
                "billstatus,billno,xyjt_sumvatimeday,entryentity.entrystartdate,entryentity.entryenddate,entryentity.entryvatimeday,entryentity.entryvacationtype",
                qf);

        //当前休假类型不匹配则跳过
        DynamicObject[] dataList = BusinessDataServiceHelper.load("xyjt_vacation_quota", "number,xyjt_vaction_name,xyjt_quote_number,xyjt_quota_name", new QFilter("xyjt_quote_number", QCP.equals, qtType).toArray());
        if (dataList == null) {
            logger.error("{}:定额类型未匹配到休假类型", qtType);
            return sumNeedCutDays;
        }
        List<String> vaApplyTypeList = Arrays.asList(dataList).stream().map(x -> x.getString("number")).collect(Collectors.toList());
        for (DynamicObject data : info) {
            DynamicObjectCollection entryEntity = data.getDynamicObjectCollection("entryentity");
            for (DynamicObject entry : entryEntity) {
                Date entryStartDate = entry.getDate("entrystartdate");
                Date entryEndDate = entry.getDate("entryenddate");
                int startYear = Integer.parseInt(DateFormatUtils.format(entryStartDate, "yyyy"));
                int endYear = Integer.parseInt(DateFormatUtils.format(entryEndDate, "yyyy"));
                BigDecimal sumVaTimeDay = entry.getBigDecimal("entryvatimeday");
                logger.info("calculateNeedCutFreezeDays startYear={} endYear={} nowYear={} sumVaTimeDay={}",
                        startYear, endYear, calYear, sumVaTimeDay);
                String vaApplyType = entry.getString("entryvacationtype.number");
                if (!vaApplyTypeList.contains(vaApplyType)) {
                    continue;
                }
                if (isCurrentYear) {
                    //当年
                    if (startYear == calYear && endYear == calYear) {
                        sumNeedCutDays = sumNeedCutDays.add(sumVaTimeDay);
                    }
                } else {
                    //非当年
                    if (startYear != calYear || endYear != calYear) {
                        sumNeedCutDays = sumNeedCutDays.add(sumVaTimeDay);
                    }
                }

            }
        }
        logger.info("getBillDays sumNeedCutDays={} billNo={} nowYear={} sumVaTimeDay={}", sumNeedCutDays, billNo, calYear, qtType);

        return sumNeedCutDays;
    }


}
