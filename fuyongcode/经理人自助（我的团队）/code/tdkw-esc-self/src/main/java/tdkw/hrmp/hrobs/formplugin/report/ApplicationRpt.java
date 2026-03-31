package tdkw.hrmp.hrobs.formplugin.report;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.DateTimeUtils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/6/14 11:01
 * @Description 我的申请 报表查询插件
 * @Demander xxx
 * @Document PC端我的申请需规说明_V2.0_0614
 * @Basedata tdkw_application
 * @Version 1.0
 **/
public class ApplicationRpt extends AbstractReportListDataPlugin {
    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        // 我的申请报表查询
        DataSet dataSet = this.queryApplicationBills(reportQueryParam);
        return dataSet;
    }

    /**
     * @author xxx
     * @Description 查询申请单
     * @Date 2023/6/14 11:03
     */
    public DataSet queryApplicationBills(ReportQueryParam param) {
        String algoKey = this.getClass().getName();

        Map<String, Object> customParam = param.getCustomParam();
        // 申请类型
        String applicationType = (String) customParam.get("applicationType");

        FilterInfo filterInfo = param.getFilter();
        // 单据类型
        String tdkwFilterBilltype = filterInfo.getString("tdkw_filter_billtype");
        // 提交日期-开始日期
        Date tdkwFilterStartdate = filterInfo.getDate("tdkw_filter_startdate");
        // 提交日期-结束日期
        Date tdkwFilterEnddate = filterInfo.getDate("tdkw_filter_enddate");

        return queryData(algoKey, tdkwFilterBilltype, tdkwFilterStartdate, tdkwFilterEnddate, applicationType);
    }

    /**
     * @author xxx
     * @Description 查询我的申请相关单据数据
     * @Date 2023/7/11 15:08
     */
    public static DataSet queryData(String algoKey, String tdkwFilterBilltype, Date tdkwFilterStartdate, Date tdkwFilterEnddate, String applicationType) {
        long currentUserId = UserServiceHelper.getCurrentUserId();
        // 仅包含单据创建人发起的业务单据。通过单据创建人判断，是否与当前登录账号相符。
        QFilter userFilter = new QFilter("creator", QCP.equals, currentUserId);
        // 个人信息修改单 单独处理
        QFilter infoApprovalFilter = new QFilter("creator", QCP.equals, currentUserId);
        // 银行卡变更申请单 人员过滤
        Long hrUserId = HRRoleAndPersonUtils.getHRUser(currentUserId);
        QFilter HRPersonFilter = new QFilter("person", QCP.equals, hrUserId);

        // sql状态
        StringBuilder statusBuilder = new StringBuilder();
        // 在办已办标识
        boolean isDoing = true;

        // 申请类型
        if (StringUtils.equals("1", applicationType)) {
            // 在办申请
            String[] doing = {"A", "B", "G", "D"};
            userFilter.and("billstatus", QCP.in, doing);
            infoApprovalFilter.and("billstatus", QCP.in, doing);
            HRPersonFilter.and("billstatus", QCP.in, doing);
            for (int i = 0; i < doing.length; i++) {
                if (i > 0) {
                    statusBuilder.append(",");
                }
                statusBuilder.append("'");
                statusBuilder.append(doing[i]);
                statusBuilder.append("'");
            }
            if (ObjectUtils.isNotEmpty(tdkwFilterStartdate)) {
                userFilter.and("createtime", QCP.large_equals, DateTimeUtils.getDayBegin(tdkwFilterStartdate));
                infoApprovalFilter.and("modifytime", QCP.large_equals, DateTimeUtils.getDayBegin(tdkwFilterStartdate));
                HRPersonFilter.and("createtime", QCP.large_equals, DateTimeUtils.getDayBegin(tdkwFilterStartdate));
            }
            if (ObjectUtils.isNotEmpty(tdkwFilterEnddate)) {
                userFilter.and("createtime", QCP.less_equals, DateTimeUtils.getDayEnd(tdkwFilterEnddate));
                infoApprovalFilter.and("modifytime", QCP.less_equals, DateTimeUtils.getDayEnd(tdkwFilterEnddate));
                HRPersonFilter.and("createtime", QCP.less_equals, DateTimeUtils.getDayEnd(tdkwFilterEnddate));
            }
        } else if (StringUtils.equals("2", applicationType)) {
            isDoing = false;
            // 已办申请
            String[] done = new String[]{"C", "E", "F"};
            userFilter.and("billstatus", QCP.in, done);
            infoApprovalFilter.and("billstatus", QCP.in, done);
            HRPersonFilter.and("billstatus", QCP.in, done);
            for (int i = 0; i < done.length; i++) {
                if (i > 0) {
                    statusBuilder.append(",");
                }
                statusBuilder.append("'");
                statusBuilder.append(done[i]);
                statusBuilder.append("'");
            }
            if (ObjectUtils.isNotEmpty(tdkwFilterStartdate)) {
                userFilter.and("submitdate", QCP.large_equals, DateTimeUtils.getDayBegin(tdkwFilterStartdate));
                infoApprovalFilter.and("modifytime", QCP.large_equals, DateTimeUtils.getDayBegin(tdkwFilterStartdate));
                HRPersonFilter.and("createtime", QCP.large_equals, DateTimeUtils.getDayBegin(tdkwFilterStartdate));
            }
            if (ObjectUtils.isNotEmpty(tdkwFilterEnddate)) {
                userFilter.and("submitdate", QCP.less_equals, DateTimeUtils.getDayEnd(tdkwFilterEnddate));
                infoApprovalFilter.and("modifytime", QCP.less_equals, DateTimeUtils.getDayEnd(tdkwFilterEnddate));
                HRPersonFilter.and("createtime", QCP.less_equals, DateTimeUtils.getDayEnd(tdkwFilterEnddate));
            }
        }

        String statusStr = statusBuilder.toString();
        // sql开始结束时间 时间 00.00.00 -> 23.59.59
        String tdkwFilterStartdateStr = null;
        String tdkwFilterEnddateStr = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (ObjectUtils.isNotEmpty(tdkwFilterStartdate)) {
            tdkwFilterStartdateStr = sdf.format(tdkwFilterStartdate) + " 00:00:00";
        }
        if (ObjectUtils.isNotEmpty(tdkwFilterEnddate)) {
            tdkwFilterEnddateStr = sdf.format(tdkwFilterEnddate) + " 23:59:59";
        }

        // 离职单
        // 只取自己离职的单
        QFilter quitQfilter = new QFilter("applytype", QCP.equals, "2");
        DataSet bill1 = ORM.create().queryDataSet(algoKey, "htm_quitapplyemp",
                "'1' as tdkw_type,'离职申请' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'1' as tdkw_billtype," +
                        "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime ,createtime as lastdate", new QFilter[]{userFilter, quitQfilter});
        // 请假单
        // 只取自己请假的单
        // QFilter bill2Filter = new QFilter("ischange", QCP.equals, Boolean.FALSE);
        // QFilter vaApplyQfilter = new QFilter("applytyperadio", QCP.equals, "0");
        // DataSet bill2 = ORM.create().queryDataSet(algoKey, "wtabm_vaapplyself",
        //         "entryentity.entryvacationtype.name as tdkw_type, '请假申请（' || vacationtypelist.name || applytime || '）' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'2' as tdkw_billtype," +
        //                 "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime ,createtime as lastdate", new QFilter[]{userFilter, bill2Filter, vaApplyQfilter});

        // 2023年10月9日09:47:19 改为sql查询
        StringBuilder bill2SqlBuilder = new StringBuilder();
        bill2SqlBuilder.append("/*dialect*/ ");
        bill2SqlBuilder.append("select ");
        bill2SqlBuilder.append("STRING_AGG(distinct sub.fname::text, ',') as tdkw_type, ");
        bill2SqlBuilder.append("'请假申请（开始时间：' || sub.startdate || '，' || STRING_AGG(CONCAT(sub.fname, '共', sub.total_hours), ',') || '）' as tdkw_billno, ");
        bill2SqlBuilder.append("sub.fbillno as tdkw_billno_hide, ");
        bill2SqlBuilder.append("sub.fid as tdkw_billid, ");
        bill2SqlBuilder.append("'2' as tdkw_billtype, ");
        bill2SqlBuilder.append("DATE(sub.fsubmitdate) as tdkw_submitdate, ");
        // 处理请假单特殊状态
        //  bill2SqlBuilder.append("sub.fauditstatus as tdkw_billstatuss, ");
        bill2SqlBuilder.append(" case ");
        bill2SqlBuilder.append("    when sub.fauditstatus = 'C' then 'I' ");
        bill2SqlBuilder.append("    else sub.fauditstatus ");
        bill2SqlBuilder.append(" end as tdkw_billstatuss, ");
        bill2SqlBuilder.append("sub.fcreatetime as createtime, ");
        bill2SqlBuilder.append("lastdate ");
        bill2SqlBuilder.append("from ( ");
        bill2SqlBuilder.append("select ");
        bill2SqlBuilder.append("vaapply.fid, ");
        bill2SqlBuilder.append("vaapply.fbillno, ");
        bill2SqlBuilder.append("vatype.fname, ");
        bill2SqlBuilder.append("vaapply.fsubmitdate, ");
        bill2SqlBuilder.append("vaapply.fauditstatus, ");
        bill2SqlBuilder.append("vaapply.fcreatetime, ");
        bill2SqlBuilder.append("vaapply.fcreatetime as lastdate, ");
        bill2SqlBuilder.append("DATE(vaapply.fstartdate) as startdate ,");
//        bill2SqlBuilder.append("ROUND(SUM(entry.fvatimehour)::numeric, 1) as total_hours ");
        bill2SqlBuilder.append(" CASE WHEN vaapply.fbillno like 'XJ%' then ROUND(SUM(entry.fvatimeday)::numeric, 1) || '天' ");
        bill2SqlBuilder.append(" ELSE ROUND(SUM(entry.fvatimehour)::numeric, 1) || '小时' ");
        bill2SqlBuilder.append(" END as total_hours ");
        bill2SqlBuilder.append("from t_wtabm_vaapply vaapply ");
        bill2SqlBuilder.append("left join t_wtabm_vaapplyentry entry on vaapply.fid = entry.fid ");
        bill2SqlBuilder.append("left join t_wtbd_vacationtype vatype on entry.fvacationtypeid = vatype.fid ");
        bill2SqlBuilder.append("where vatype.fname != '' ");
        bill2SqlBuilder.append("and vaapply.fischange = '0' ");
        bill2SqlBuilder.append("and vaapply.fapplytyperadio = '0' ");
        bill2SqlBuilder.append("and vaapply.fcreatorid = '").append(currentUserId).append("'");
        if (StringUtils.isNotBlank(statusStr)) {
            bill2SqlBuilder.append("and vaapply.fbillstatus in (").append(statusStr).append(")");
        }
        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
            bill2SqlBuilder.append("and ").append(isDoing ? "vaapply.fcreatetime" : "vaapply.fsubmitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
        }
        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
            bill2SqlBuilder.append("and ").append(isDoing ? "vaapply.fcreatetime" : "vaapply.fsubmitdate").append(" <= '").append(tdkwFilterEnddateStr).append("'");
        }

        bill2SqlBuilder.append("group by vaapply.fid, vaapply.fbillno, vatype.fname) sub ");
        bill2SqlBuilder.append("group by sub.fid, sub.fbillno, sub.fsubmitdate, sub.fauditstatus, sub.fcreatetime, sub.startdate, lastdate");
        DataSet bill2 = DB.queryDataSet(algoKey, DBRoute.of("wtc"), bill2SqlBuilder.toString());

        // 销假单
        // 只取自己销假的单
        // QFilter bill3Filter = new QFilter("ischange", QCP.equals, Boolean.TRUE);
        // DataSet bill3 = ORM.create().queryDataSet(algoKey, "wtabm_vaupdateself",
        //         "entryentity.entryvacationtype.name as tdkw_type,'销假申请（' || vacationtypelist.name || applytime || '）' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'3' as tdkw_billtype," +
        //                 "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{userFilter, bill3Filter, vaApplyQfilter});

        // 2023年10月9日10:24:40 改为sql查询
        StringBuilder bill3SqlBuilder = new StringBuilder();
        bill3SqlBuilder.append("/*dialect*/ ");
        bill3SqlBuilder.append("select ");
        bill3SqlBuilder.append("STRING_AGG(distinct sub.fname::text, ',') as tdkw_type, ");
        bill3SqlBuilder.append("'销假申请（开始时间：' || sub.startdate || '，' || STRING_AGG(CONCAT(sub.fname, '共', ROUND(sub.total_hours, 1), '小时'), ',') || '）' as tdkw_billno, ");
        bill3SqlBuilder.append("sub.fbillno as tdkw_billno_hide, ");
        bill3SqlBuilder.append("sub.fid as tdkw_billid, ");
        bill3SqlBuilder.append("'3' as tdkw_billtype, ");
        bill3SqlBuilder.append("DATE(sub.fsubmitdate) as tdkw_submitdate, ");
        bill3SqlBuilder.append("sub.fbillstatus as tdkw_billstatuss, ");
        bill3SqlBuilder.append("sub.fcreatetime as createtime, ");
        bill3SqlBuilder.append("lastdate ");
        bill3SqlBuilder.append("from ( ");
        bill3SqlBuilder.append("select ");
        bill3SqlBuilder.append("vaapply.fid, ");
        bill3SqlBuilder.append("vaapply.fbillno, ");
        bill3SqlBuilder.append("vatype.fname, ");
        bill3SqlBuilder.append("vaapply.fsubmitdate, ");
        bill3SqlBuilder.append("vaapply.fbillstatus, ");
        bill3SqlBuilder.append("vaapply.fcreatetime, ");
        bill3SqlBuilder.append("vaapply.fcreatetime as lastdate, ");
        bill3SqlBuilder.append("DATE(vaapply.fstartdate) as startdate ,");
        bill3SqlBuilder.append("ROUND(SUM(entry.fvatimehour)::numeric, 1) as total_hours ");
        bill3SqlBuilder.append("from t_wtabm_vaapply vaapply ");
        bill3SqlBuilder.append("left join t_wtabm_vaapplyentry entry on vaapply.fid = entry.fid ");
        bill3SqlBuilder.append("left join t_wtbd_vacationtype vatype on entry.fvacationtypeid = vatype.fid ");
        bill3SqlBuilder.append("where vatype.fname != '' ");
        bill3SqlBuilder.append("and vaapply.fischange = '1' ");
        bill3SqlBuilder.append("and vaapply.fapplytyperadio = '0' ");
        bill3SqlBuilder.append("and vaapply.fcreatorid = '").append(currentUserId).append("'");
        if (StringUtils.isNotBlank(statusStr)) {
            bill3SqlBuilder.append("and vaapply.fbillstatus in (").append(statusStr).append(")");
        }
        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
            bill3SqlBuilder.append("and ").append(isDoing ? "vaapply.fcreatetime" : "vaapply.fsubmitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
        }
        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
            bill3SqlBuilder.append("and ").append(isDoing ? "vaapply.fcreatetime" : "vaapply.fsubmitdate").append(" <= '").append(tdkwFilterEnddateStr).append("'");
        }
        bill3SqlBuilder.append("group by vaapply.fid, vaapply.fbillno, vatype.fname) sub ");
        bill3SqlBuilder.append("group by sub.fid, sub.fbillno, sub.fsubmitdate, sub.fbillstatus, sub.fcreatetime, sub.startdate, lastdate");
        DataSet bill3 = DB.queryDataSet(algoKey, DBRoute.of("wtc"), bill3SqlBuilder.toString());

        // 出差单---提交日期已添加
//        DataSet bill4 = ORM.create().queryDataSet(algoKey, "tdkw_reqtripe",
//                "tdkw_triptype.name as tdkw_type, '出差申请（' || '出差地点：' || tdkw_travelsummary || '， 出差时间：' || tdkw_triptimeyc || '， 出差天数：' || tdkw_tripnum || '）' AS tdkw_billno ,billno as tdkw_billno_hide,id as tdkw_billid,'4' as tdkw_billtype," +
//                        "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{userFilter});

        // 2023年11月9日10:28:46 改sql查询
//        StringBuilder bill4SqlBuilder = new StringBuilder();
//        bill4SqlBuilder.append("/*dialect*/ select ");
//        bill4SqlBuilder.append(" tdkw_type, ");
//        bill4SqlBuilder.append(" '出差申请（' || '出差地点：' || fk_tdkw_travelsummary || '， 出差时间：' || fk_tdkw_triptimeyc || '， 出差天数：' || fk_tdkw_tripnum || '）' as tdkw_billno , ");
//        bill4SqlBuilder.append(" tdkw_billno_hide, ");
//        bill4SqlBuilder.append(" tdkw_billid, ");
//        bill4SqlBuilder.append(" '4' as tdkw_billtype, ");
//        bill4SqlBuilder.append(" tdkw_submitdate, ");
//        bill4SqlBuilder.append(" tdkw_billstatuss , ");
//        bill4SqlBuilder.append(" createtime, ");
//        bill4SqlBuilder.append(" lastdate ");
//        bill4SqlBuilder.append(" from  ( ");
//        bill4SqlBuilder.append(" select  ");
//        bill4SqlBuilder.append(" trip.fid as tdkw_billid, ");
//        bill4SqlBuilder.append(" triptype.fname as tdkw_type, ");
//        bill4SqlBuilder.append(" trip.fk_tdkw_triptimeyc, ");
//        bill4SqlBuilder.append(" trip.fbillno as tdkw_billno_hide, ");
//        bill4SqlBuilder.append(" trip.fbillstatus as tdkw_billstatuss, ");
//        bill4SqlBuilder.append(" trip.fcreatetime as createtime, ");
//        bill4SqlBuilder.append(" trip.fcreatetime as lastdate, ");
//        bill4SqlBuilder.append(" trip.fk_tdkw_submitdate as tdkw_submitdate, ");
//        bill4SqlBuilder.append(" case ");
//        bill4SqlBuilder.append(" when array_upper(string_to_array(trip.fk_tdkw_travelsummary, '、'), 1) > 3 then array_to_string(array(select unnest(string_to_array(trip.fk_tdkw_travelsummary, '、')) limit 3), '、') || '等' ");
//        bill4SqlBuilder.append(" else fk_tdkw_travelsummary ");
//        bill4SqlBuilder.append(" end as fk_tdkw_travelsummary, ");
//        bill4SqlBuilder.append(" trip.fk_tdkw_tripnum ");
//        bill4SqlBuilder.append(" from tk_tdkw_reqtripe trip ");
//        bill4SqlBuilder.append(" left join t_wtbd_traveltype triptype on  ");
//        bill4SqlBuilder.append(" trip.fk_tdkw_triptype = triptype.fid ");
//        bill4SqlBuilder.append(" where trip.fcreatorid = '").append(currentUserId).append("'");
//        if (StringUtils.isNotBlank(statusStr)) {
//            bill4SqlBuilder.append("and trip.fbillstatus in (").append(statusStr).append(")");
//        }
//        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
//            bill4SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
//        }
//        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
//            bill4SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" <= '").append(tdkwFilterEnddateStr).append("'");
//        }
//        bill4SqlBuilder.append(" ) z ");
//        DataSet bill4 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill4SqlBuilder.toString());


        // 销差单
//        DataSet bill5 = ORM.create().queryDataSet(algoKey, "tdkw_destroytripe",
//                "tdkw_triptype.name as tdkw_type,'销差申请（' || '出差地点：' || tdkw_travelsummary || '， 出差时间：' || tdkw_triptimeyc || '， 出差天数：' || tdkw_tripetime || '）' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'5' as tdkw_billtype," +
//                        "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{userFilter});

        // 2023年11月9日10:28:46 改sql查询
//        StringBuilder bill5SqlBuilder = new StringBuilder();
//        bill5SqlBuilder.append("/*dialect*/ select ");
//        bill5SqlBuilder.append(" tdkw_type, ");
//        bill5SqlBuilder.append(" '销差申请（' || '出差地点：' || fk_tdkw_travelsummary || '， 出差时间：' || fk_tdkw_triptimeyc || '， 出差天数：' || fk_tdkw_tripnum || '）' as tdkw_billno , ");
//        bill5SqlBuilder.append(" tdkw_billno_hide, ");
//        bill5SqlBuilder.append(" tdkw_billid, ");
//        bill5SqlBuilder.append(" '5' as tdkw_billtype, ");
//        bill5SqlBuilder.append(" tdkw_submitdate, ");
//        bill5SqlBuilder.append(" tdkw_billstatuss , ");
//        bill5SqlBuilder.append(" createtime, ");
//        bill5SqlBuilder.append(" lastdate ");
//        bill5SqlBuilder.append(" from  ( ");
//        bill5SqlBuilder.append(" select  ");
//        bill5SqlBuilder.append(" trip.fid as tdkw_billid, ");
//        bill5SqlBuilder.append(" triptype.fname as tdkw_type, ");
//        bill5SqlBuilder.append(" trip.fk_tdkw_triptimeyc, ");
//        bill5SqlBuilder.append(" trip.fbillno as tdkw_billno_hide, ");
//        bill5SqlBuilder.append(" trip.fbillstatus as tdkw_billstatuss, ");
//        bill5SqlBuilder.append(" trip.fcreatetime as createtime, ");
//        bill5SqlBuilder.append(" trip.fcreatetime as lastdate, ");
//        bill5SqlBuilder.append(" trip.fk_tdkw_submitdate as tdkw_submitdate, ");
//        bill5SqlBuilder.append(" case ");
//        bill5SqlBuilder.append(" when array_upper(string_to_array(trip.fk_tdkw_travelsummary, '、'), 1) > 3 then array_to_string(array(select unnest(string_to_array(trip.fk_tdkw_travelsummary, '、')) limit 3), '、') || '等' ");
//        bill5SqlBuilder.append(" else fk_tdkw_travelsummary ");
//        bill5SqlBuilder.append(" end as fk_tdkw_travelsummary, ");
//        bill5SqlBuilder.append(" trip.fk_tdkw_tripetime as fk_tdkw_tripnum ");
//        bill5SqlBuilder.append(" from tk_tdkw_destroytrip trip ");
//        bill5SqlBuilder.append(" left join t_wtbd_traveltype triptype on  ");
//        bill5SqlBuilder.append(" trip.fk_tdkw_triptype = triptype.fid ");
//        bill5SqlBuilder.append(" where trip.fcreatorid = '").append(currentUserId).append("'");
//        if (StringUtils.isNotBlank(statusStr)) {
//            bill5SqlBuilder.append("and trip.fbillstatus in (").append(statusStr).append(")");
//        }
//        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
//            bill5SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
//        }
//        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
//            bill5SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" <= '").append(tdkwFilterEnddateStr).append("'");
//        }
//        bill5SqlBuilder.append(" ) z ");
//        DataSet bill5 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill5SqlBuilder.toString());


        // 补卡单---提交日期已添加
        // 只查自己的
        // QFilter qFilter = new QFilter("applytyperadio", QCP.equals, "0");
        // DataSet bill6 = ORM.create().queryDataSet(algoKey, "wtpm_supsignself",
        //        "entryentity.applyreason.name as tdkw_type,'补卡申请（补卡日期：' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'6' as tdkw_billtype," +
        //                "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{userFilter, qFilter});

        // 2023年10月8日19:36:14 改为sql语句查询
        StringBuilder bill6SqlBuilder = new StringBuilder();
        bill6SqlBuilder.append("/*dialect*/ ");
        bill6SqlBuilder.append("select ");
        bill6SqlBuilder.append("subquery.reason as tdkw_type, ");
        bill6SqlBuilder.append("'补卡申请（补卡日期：' || ");
        bill6SqlBuilder.append("case ");
        bill6SqlBuilder.append("when array_length(dates, 1) > 3 then array_to_string(dates[1:3], '、') || '等' ");
        bill6SqlBuilder.append("else array_to_string(dates, '、') ");
        bill6SqlBuilder.append("end || '）' as tdkw_billno, ");
        bill6SqlBuilder.append("subquery.fbillno as tdkw_billno_hide, ");
        bill6SqlBuilder.append("subquery.fid as tdkw_billid, ");
        bill6SqlBuilder.append("'6' as tdkw_billtype, ");
        bill6SqlBuilder.append("DATE(subquery.fsubmitdate) as tdkw_submitdate, ");
        bill6SqlBuilder.append("subquery.fbillstatus as tdkw_billstatuss, ");
        bill6SqlBuilder.append("subquery.fcreatetime as createtime, ");
        bill6SqlBuilder.append("subquery.fcreatetime as lastdate ");
        bill6SqlBuilder.append("from ");
        bill6SqlBuilder.append("( ");
        bill6SqlBuilder.append("select ");
        bill6SqlBuilder.append("supsign.fid, ");
        bill6SqlBuilder.append("supsign.fbillno, ");
        bill6SqlBuilder.append("supsign.fsubmitdate, ");
        bill6SqlBuilder.append("supsign.fbillstatus, ");
        bill6SqlBuilder.append("supsign.fcreatetime, ");
        bill6SqlBuilder.append("ARRAY_AGG(DATE(supsigninfo.fsigndate) order by supsigninfo.fsigndate asc) as dates, ");
        bill6SqlBuilder.append("STRING_AGG(distinct resaon.fname::text, ',') as reason ");
        bill6SqlBuilder.append("from ");
        bill6SqlBuilder.append("t_wtpm_supsign supsign ");
        bill6SqlBuilder.append("left join t_wtpm_supsigninfo supsigninfo on ");
        bill6SqlBuilder.append("supsign.fid = supsigninfo.fid ");
        bill6SqlBuilder.append("left join t_wtbd_reason resaon on ");
        bill6SqlBuilder.append("supsigninfo.fapplyreasonid = resaon.fid ");
        bill6SqlBuilder.append("where ");
        bill6SqlBuilder.append("supsign.fapplytype = '0' ");
        bill6SqlBuilder.append("and supsign.fcreatorid = '").append(currentUserId).append("'");
        if (StringUtils.isNotBlank(statusStr)) {
            bill6SqlBuilder.append("and supsign.fbillstatus in (").append(statusStr).append(")");
        }
        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
            bill6SqlBuilder.append("and ").append(isDoing ? "supsign.fcreatetime" : "supsign.fsubmitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
        }
        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
            bill6SqlBuilder.append("and ").append(isDoing ? "supsign.fcreatetime" : "supsign.fsubmitdate").append(" <= '").append(tdkwFilterEnddateStr).append("'");
        }
        bill6SqlBuilder.append("group by ");
        bill6SqlBuilder.append("supsign.fid, ");
        bill6SqlBuilder.append("supsign.fbillno, ");
        bill6SqlBuilder.append("supsign.fsubmitdate, ");
        bill6SqlBuilder.append("supsign.fbillstatus, ");
        bill6SqlBuilder.append("supsign.fcreatetime) subquery");
        DataSet bill6 = DB.queryDataSet(algoKey, DBRoute.of("wtc"), bill6SqlBuilder.toString());

        // 按时段申请
        // QFilter bill7FilterA = new QFilter("otapplytype", QCP.equals, "1");
        // 按时长申请
        // QFilter bill7FilterB = new QFilter("otapplytype", QCP.equals, "2");
        // 加班单  只查自己的
        // 按时段申请
        // DataSet bill701 = ORM.create().queryDataSet(algoKey, "wtom_otbillself",
        //         "sdentry.sdottype.name as tdkw_type,'加班申请（加班时长：' || vatimetext || '）'  as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'7' as tdkw_billtype," +
        //                 "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{userFilter, bill7FilterA, qFilter});
        // 按时长申请
        // DataSet bill702 = ORM.create().queryDataSet(algoKey, "wtom_otbillself",
        //         "scentry.scottype.name as tdkw_type,'加班申请（加班时长：' || vatimetext || '）' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'7' as tdkw_billtype," +
        //                 "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{userFilter, bill7FilterB, qFilter});

        // 2023年10月9日15:39:50 改为sql查询
        StringBuilder bill7SqlBuilder = new StringBuilder();
        bill7SqlBuilder.append("/*dialect*/ ");
        bill7SqlBuilder.append("SELECT ");
        bill7SqlBuilder.append("STRING_AGG(distinct ottype.fname ::text, ',') as tdkw_type, ");
        bill7SqlBuilder.append("'加班申请（开始日期：' || subquery.min_date ||  '， 加班时长：' || SPLIT_PART(otapply.fvatimetext, 'h', 1) || 'h' || '）' as tdkw_billno, ");
        bill7SqlBuilder.append("otapply.fbillno as tdkw_billno_hide, ");
        bill7SqlBuilder.append("otapply.fid as tdkw_billid, ");
        bill7SqlBuilder.append("'7' as tdkw_billtype, ");
        bill7SqlBuilder.append("otapply.fsubmitdate as tdkw_submitdate, ");
        bill7SqlBuilder.append("otapply.fbillstatus as tdkw_billstatuss, ");
        bill7SqlBuilder.append("otapply.fcreatetime as createtime, ");
        bill7SqlBuilder.append("otapply.fcreatetime as lastdate ");
        bill7SqlBuilder.append("FROM ");
        bill7SqlBuilder.append("t_wtom_otapply otapply ");
        bill7SqlBuilder.append("LEFT JOIN t_wtom_otapplydentry entry ON ");
        bill7SqlBuilder.append("otapply.fid = entry.fid ");
        bill7SqlBuilder.append("LEFT JOIN t_wtbd_ottype ottype ON ");
        bill7SqlBuilder.append("entry.fsdottype = ottype.fid ");
        bill7SqlBuilder.append(" left join ( ");
        bill7SqlBuilder.append(" SELECT ");
        bill7SqlBuilder.append(" dentry.fid, ");
        bill7SqlBuilder.append(" TO_CHAR(MIN(dentry.fotstartdate), 'YYYY-MM-DD') AS min_date ");
        bill7SqlBuilder.append(" FROM ");
        bill7SqlBuilder.append(" t_wtom_otapplydentry dentry ");
        bill7SqlBuilder.append(" GROUP BY ");
        bill7SqlBuilder.append(" dentry.fid ");
        bill7SqlBuilder.append(" ) subquery ON otapply.fid = subquery.fid ");
        bill7SqlBuilder.append("WHERE ");
        bill7SqlBuilder.append("ottype.fenable = '1' ");
        bill7SqlBuilder.append("AND otapply.fapplytyperadio = '0' ");
        bill7SqlBuilder.append("AND otapply.fotapplytype IN ('1', '2') ");
        bill7SqlBuilder.append("AND otapply.fcreatorid = '").append(currentUserId).append("'");
        if (StringUtils.isNotBlank(statusStr)) {
            bill7SqlBuilder.append("and otapply.fbillstatus in (").append(statusStr).append(")");
        }
        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
            bill7SqlBuilder.append("and ").append(isDoing ? "otapply.fcreatetime" : "otapply.fsubmitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
        }
        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
            bill7SqlBuilder.append("and ").append(isDoing ? "otapply.fcreatetime" : "otapply.fsubmitdate").append(" <= '").append(tdkwFilterEnddateStr).append("'");
        }
        bill7SqlBuilder.append("GROUP BY ");
        bill7SqlBuilder.append("tdkw_billno, ");
        bill7SqlBuilder.append("tdkw_billno_hide, ");
        bill7SqlBuilder.append("tdkw_billid, ");
        bill7SqlBuilder.append("tdkw_billtype, ");
        bill7SqlBuilder.append("tdkw_submitdate, ");
        bill7SqlBuilder.append("tdkw_billstatuss, ");
        bill7SqlBuilder.append("createtime, ");
        bill7SqlBuilder.append("lastdate");
        DataSet bill7 = DB.queryDataSet(algoKey, DBRoute.of("wtc"), bill7SqlBuilder.toString());

        // 证明单---提交日期已添加 证明类型：$证明类型$
//        DataSet bill8 = ORM.create().queryDataSet(algoKey, "tdkw_prove_handle",
//                "tdkw_basedatafield.name as tdkw_type,'证明申请（证明类型：' || tdkw_basedatafield.name || '）' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'8' as tdkw_billtype,submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate",
//                new QFilter[]{userFilter});

        // 个人信息修改单---提交日期待确认（提交变更时间）
        // DataSet bill9 = ORM.create().queryDataSet(algoKey, "hspm_infoapproval",
        //        "'修改' as tdkw_type,billno as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'9' as tdkw_billtype,modifytime as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{infoApprovalFilter});

        // 2023年10月9日16:24:30 改为sql查询
        StringBuilder bill9SqlBuilder = new StringBuilder();
        bill9SqlBuilder.append("/*dialect*/ ");
        bill9SqlBuilder.append("select ");
        bill9SqlBuilder.append("'修改' as tdkw_type, ");
        bill9SqlBuilder.append("'个人信息修改申请（' || ");
        bill9SqlBuilder.append("case ");
        bill9SqlBuilder.append("    when array_length(typenames, 1) > 3 then regexp_replace(array_to_string(typenames[1:3], '、'), '[A-Z]', '', 'g') || '等' ");
        bill9SqlBuilder.append("    else regexp_replace(array_to_string(typenames, '、'), '[A-Z]', '', 'g') ");
        bill9SqlBuilder.append("end || '）' as tdkw_billno, ");
        bill9SqlBuilder.append("subquery.tdkw_billno_hide, ");
        bill9SqlBuilder.append("subquery.tdkw_billid, ");
        bill9SqlBuilder.append("'9' as tdkw_billtype, ");
        bill9SqlBuilder.append("subquery.tdkw_submitdate, ");
        bill9SqlBuilder.append("subquery.tdkw_billstatuss, ");
        bill9SqlBuilder.append("subquery.createtime, ");
        bill9SqlBuilder.append("subquery.lastdate ");
        bill9SqlBuilder.append("from ");
        bill9SqlBuilder.append("( ");
        bill9SqlBuilder.append("    select ");
        bill9SqlBuilder.append("    tdkw_billno_hide, ");
        bill9SqlBuilder.append("    tdkw_billid, ");
        bill9SqlBuilder.append("    tdkw_billtype, ");
        bill9SqlBuilder.append("    tdkw_submitdate, ");
        bill9SqlBuilder.append("    tdkw_billstatuss, ");
        bill9SqlBuilder.append("    createtime, ");
        bill9SqlBuilder.append("    lastdate, ");
        bill9SqlBuilder.append("    ARRAY_AGG(distinct t.sort order by t.sort) as typenames ");
        bill9SqlBuilder.append("    from ");
        bill9SqlBuilder.append("    ( ");
        bill9SqlBuilder.append("        select ");
        bill9SqlBuilder.append("        info.fbillno as tdkw_billno_hide, ");
        bill9SqlBuilder.append("        info.fid as tdkw_billid, ");
        bill9SqlBuilder.append("        '9' as tdkw_billtype, ");
        bill9SqlBuilder.append("        info.fmodifytime as tdkw_submitdate, ");
        bill9SqlBuilder.append("        info.fbillstatus as tdkw_billstatuss, ");
        bill9SqlBuilder.append("        info.fcreatetime as createtime, ");
        bill9SqlBuilder.append("        info.fcreatetime as lastdate, ");
        bill9SqlBuilder.append("        entry.ffirstgroup, ");
        bill9SqlBuilder.append("        s.sort ");
        bill9SqlBuilder.append("        from ");
        bill9SqlBuilder.append("        t_hspm_infoapproval info ");
        bill9SqlBuilder.append("        left join t_hspm_infoapproentity entry on ");
        bill9SqlBuilder.append("        info.fid = entry.fid ");
        bill9SqlBuilder.append("        left join ( ");
        bill9SqlBuilder.append("            select ");
        bill9SqlBuilder.append("            distinct ");
        bill9SqlBuilder.append("            case ");
        bill9SqlBuilder.append("                ffirstgroup when '基本信息' then 'A基本信息' ");
        bill9SqlBuilder.append("                when '前工作经历' then 'B前工作经历' ");
        bill9SqlBuilder.append("                when '教育经历' then 'C教育经历' ");
        bill9SqlBuilder.append("                when '家庭成员' then 'D家庭成员' ");
        bill9SqlBuilder.append("                when '证件信息' then 'E证件信息' ");
        bill9SqlBuilder.append("                when '联系方式' then 'F联系方式' ");
        bill9SqlBuilder.append("                when '紧急联系人' then 'G紧急联系人' ");
        bill9SqlBuilder.append("                when '人员地址' then 'H人员地址' ");
        bill9SqlBuilder.append("                when '职称信息' then 'I职称信息' ");
        bill9SqlBuilder.append("                when '执（职）业资格' then 'J执（职）业资格' ");
        bill9SqlBuilder.append("                when '语言能力' then 'K语言能力' ");
        bill9SqlBuilder.append("                when '奖惩情况' then 'L奖惩情况' ");
        bill9SqlBuilder.append("                when '职业技能鉴定' then 'M职业技能鉴定' ");
        bill9SqlBuilder.append("                when '特长及爱好' then 'N特长及爱好' ");
        bill9SqlBuilder.append("                when '其他社会团体经历' then 'O其他社会团体经历' ");
        bill9SqlBuilder.append("            end as sort, ");
        bill9SqlBuilder.append("            ffirstgroup ");
        bill9SqlBuilder.append("            from ");
        bill9SqlBuilder.append("            t_hspm_infoapproentity ");
        bill9SqlBuilder.append("            order by ");
        bill9SqlBuilder.append("            sort) s on ");
        bill9SqlBuilder.append("        s.ffirstgroup = entry.ffirstgroup ");
        bill9SqlBuilder.append("        where ");
        bill9SqlBuilder.append("    info.fcreatorid = '").append(currentUserId).append("'");
        if (StringUtils.isNotBlank(statusStr)) {
            bill9SqlBuilder.append(" and info.fbillstatus in (").append(statusStr).append(")");
        }
        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
            bill9SqlBuilder.append(" and info.fmodifytime >= '").append(tdkwFilterStartdateStr).append("'");
        }
        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
            bill9SqlBuilder.append(" and info.fmodifytime <= '").append(tdkwFilterEnddateStr).append("'");
        }
        bill9SqlBuilder.append("        group by ");
        bill9SqlBuilder.append("        info.fid, ");
        bill9SqlBuilder.append("        info.fbillno, ");
        bill9SqlBuilder.append("        info.fmodifytime, ");
        bill9SqlBuilder.append("        info.fbillstatus, ");
        bill9SqlBuilder.append("        info.fcreatetime, ");
        bill9SqlBuilder.append("        entry.ffirstgroup, ");
        bill9SqlBuilder.append("        s.sort ");
        bill9SqlBuilder.append("        order by ");
        bill9SqlBuilder.append("        s.sort ");
        bill9SqlBuilder.append("    ) t ");
        bill9SqlBuilder.append("    group by ");
        bill9SqlBuilder.append("    tdkw_billno_hide, ");
        bill9SqlBuilder.append("    tdkw_billid, ");
        bill9SqlBuilder.append("    tdkw_billtype, ");
        bill9SqlBuilder.append("    tdkw_submitdate, ");
        bill9SqlBuilder.append("    tdkw_billstatuss, ");
        bill9SqlBuilder.append("    createtime, ");
        bill9SqlBuilder.append("    lastdate ");
        bill9SqlBuilder.append(") subquery");
        DataSet bill9 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill9SqlBuilder.toString());

        // 问询单---提交日期已添加
//        DataSet bill10 = ORM.create().queryDataSet(algoKey, "tdkw_employee_inquiries",
//                "tdkw_basedatafield.name as tdkw_type,'问询单（办理事项：' || tdkw_basedatafield.name || '）'  as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'10' as tdkw_billtype,submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate",
//                new QFilter[]{userFilter});

        // 招聘需求单---提交日期已添加 tdkw_rec_apply_dept.name
//        DataSet bill11 = ORM.create().queryDataSet(algoKey, "tdkw_rec_apply_bill",
//                "tdkw_rec_type.name as tdkw_type,tdkw_rec_apply_dept,tdkw_rec_in_posi_name.name  as post_name,'招聘需求（'  as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'11' as tdkw_billtype,submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate",
//                new QFilter[]{userFilter});
        // 招聘需求部门id
//        DynamicObjectCollection bill11DeptIds = QueryServiceHelper.query("tdkw_rec_apply_bill", "tdkw_rec_apply_dept", userFilter.toArray());
//        List<Long> bill11DeptIdList = bill11DeptIds.stream().mapToLong(dynamicObject -> dynamicObject.getLong("tdkw_rec_apply_dept")).boxed().collect(Collectors.toList());
//        QFilter orgFilter = new QFilter("id", QCP.in, bill11DeptIdList);
//        orgFilter.and("iscurrentversion", QCP.equals, '1');
//        orgFilter.and("datastatus", QCP.equals, '1');
//        orgFilter.and("enable", QCP.equals, '1');
//        DataSet recDeptDataSet = QueryServiceHelper.queryDataSet(algoKey, "haos_adminorghr", "id,name", orgFilter.toArray(), "");
//        bill11 = bill11.leftJoin(recDeptDataSet).on("tdkw_rec_apply_dept", "id").select("tdkw_type", "tdkw_billno", "name", "post_name", "tdkw_billno_hide", "tdkw_billid", "tdkw_billtype",
//                "tdkw_submitdate", "tdkw_billstatuss", "createtime", "lastdate").finish();
//        // 分组拼接部门、内部职位
//        StringBuilder recExecSqlBuilder = new StringBuilder();
//        recExecSqlBuilder.append("/*dialect*/ ")
//                .append("SELECT ")
//                .append("tdkw_type, ")
//                .append("(tdkw_billno + name + '，' + post_name + '）') AS tdkw_billno, ")
//                .append("tdkw_billno_hide, ")
//                .append("tdkw_billid, ")
//                .append("tdkw_billtype, ")
//                .append("tdkw_submitdate, ")
//                .append("tdkw_billstatuss, ")
//                .append("createtime, ")
//                .append("lastdate ")
//                .append("GROUP BY ")
//                .append("tdkw_type, ")
//                .append("tdkw_billno, ")
//                .append("name, ")
//                .append("post_name, ")
//                .append("tdkw_billno_hide, ")
//                .append("tdkw_billid, ")
//                .append("tdkw_billtype, ")
//                .append("tdkw_submitdate, ")
//                .append("tdkw_billstatuss, ")
//                .append("createtime, ")
//                .append("lastdate");
//        bill11 = bill11.executeSql(recExecSqlBuilder.toString());


        // 个人信息删除单---提交日期待确认（提交变更时间）
        //DataSet bill12 = ORM.create().queryDataSet(algoKey, "tdkw_hspm_infochg", "'删除' as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'12' as tdkw_billtype,modifytime as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{infoApprovalFilter});


        //因私出国申请单
//        DataSet bill13 = ORM.create().queryDataSet(algoKey, "tdkw_wsgl_form_yscgsq",
//                "'因私出国' as tdkw_type,'因私出国申请单（前往国家/地区：' || tdkw_gocountriesstxt || ' 时间：' || to_char(tdkw_tmdeparture,'yyyy-MM-dd') || ' 至 ' || to_char(tdkw_rettm,'yyyy-MM-dd') || '）' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'13' as tdkw_billtype," +
//                        "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime as lastdate", new QFilter[]{userFilter});
//        bill13 = bill13.addField("case when tdkw_billno = null then '因私出国申请单（换证/办理签证、签注）' else tdkw_billno end", "tdkw_billno1");
//        bill13 = bill13.select("tdkw_type","tdkw_billno1 as tdkw_billno","tdkw_billno_hide","tdkw_billid","tdkw_billtype","tdkw_submitdate","tdkw_billstatuss","createtime","lastdate");


        // 银行卡变更申请单
//        DataSet bill14 = ORM.create().queryDataSet(algoKey, "hsas_perbceditbill",
//                "'银行卡信息维护' as tdkw_type,'银行卡信息维护' as tdkw_billno,billno as tdkw_billno_hide,id as tdkw_billid,'14' as tdkw_billtype," +
//                        "createtime as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime,createtime  as lastdate", new QFilter[]{HRPersonFilter});


//        DataSet finalData = bill1.union(bill2).union(bill3).union(bill4).union(bill5).union(bill6).union(bill7)
//                .union(bill8).union(bill9).union(bill10).union(bill11).union(bill13).union(bill14);

        DataSet finalData = bill1.union(bill2).union(bill3).union(bill6).union(bill7)
                .union(bill9);

        if (!StringUtils.equals("0", tdkwFilterBilltype) && StringUtils.isNotEmpty(tdkwFilterBilltype)) {
            Map<String, Object> params = new HashMap<>();
            params.put("tdkwFilterBilltype", tdkwFilterBilltype);
            finalData = finalData.filter("tdkw_billtype = tdkwFilterBilltype", params);
        }

        // 去重
        DataSet dataSet = finalData.executeSql("select distinct tdkw_type,tdkw_billno,tdkw_billno_hide,tdkw_billtype,createtime,lastdate,tdkw_submitdate," +
                "tdkw_billstatuss,tdkw_billid group by tdkw_billno,tdkw_billno_hide,tdkw_billtype,createtime,lastdate,tdkw_submitdate,tdkw_billstatuss,tdkw_billid,tdkw_type");

        // 分组合并
        dataSet = dataSet.executeSql("select tdkw_billno,tdkw_billno_hide,tdkw_billtype,createtime,lastdate,tdkw_submitdate,tdkw_billstatuss,tdkw_billid," +
                "group_concat(tdkw_type) group by tdkw_billno,tdkw_billno_hide,tdkw_billtype,createtime,lastdate,tdkw_submitdate,tdkw_billstatuss,tdkw_billid");
        // 类型为null则不展示
//        dataSet = dataSet.executeSql("select tdkw_billno,tdkw_billtype,createtime,lastdate,tdkw_submitdate,tdkw_billstatuss,tdkw_billid," +
//                "case when tdkw_type = 'null' then '' else tdkw_type end as tdkw_type " +
//                "case when tdkw_submitdate = 'null' then createtime else tdkw_submitdate end as lastdate");
//        dataSet = dataSet.orderBy(new String[]{"lastdate desc"});
        if (StringUtils.equals("1", applicationType)) {
            // 在办申请
            dataSet = dataSet.executeSql("select tdkw_billno,tdkw_billno_hide,tdkw_billtype,createtime,lastdate,tdkw_submitdate,tdkw_billstatuss,tdkw_billid," +
                    "case when tdkw_type = 'null' then '' else tdkw_type end as tdkw_type ");
        } else if (StringUtils.equals("2", applicationType)) {
            // 已办申请
            dataSet = dataSet.executeSql("select tdkw_billno,tdkw_billno_hide,tdkw_billtype,createtime,lastdate,tdkw_submitdate,tdkw_billstatuss,tdkw_billid," +
                    "case when tdkw_type = 'null' then '' else tdkw_type end as tdkw_type " +
                    "tdkw_submitdate  as lastdate");
        }
        dataSet = dataSet.orderBy(new String[]{"lastdate desc"});
        return dataSet;
    }
}
