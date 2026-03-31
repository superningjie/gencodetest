package tdkw.esc.leaderquery.report.mob;

import com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.algo.DataType;
import kd.bos.algo.MapFunction;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.list.QueryBuilder;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.list.ListDataProvider;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WorkflowToMobReportDataProvider
 *
 * @author xxx
 * @date 2023/6/20
 */
public class WorkflowToMobReportDataProvider extends
        ListDataProvider {
    private static final Log logger = LogFactory.getLog(WorkflowToMobReportDataProvider.class);
    private String isFinish;

    public String getIsFinish() {
        return isFinish;
    }

    public WorkflowToMobReportDataProvider(String isFinish) {
        this.isFinish = isFinish;
    }

    public WorkflowToMobReportDataProvider() {
    }

    @Override
    public List<QFilter> getQFilters() {
        return super.getQFilters();
    }

    @Override
    public QueryBuilder getQueryBuilder() {
        return super.getQueryBuilder();
    }

    @Override
    public DynamicObjectCollection getData(int start, int limit) {
        String finish = this.getIsFinish();
        //查找创建人跟当前用户对应的单据
        long currentUserId = UserServiceHelper.getCurrentUserId();
        QFilter userFilter = new QFilter("creator", QCP.equals, currentUserId);
        // 个人信息修改单 单独处理
        QFilter infoApprovalFilter = new QFilter("creator", QCP.equals, currentUserId);
        // 银行卡变更申请单 人员过滤
        Long hrUserId = HRRoleAndPersonUtils.getHRUser(currentUserId);
        QFilter HRPersonFilter = new QFilter("person", QCP.equals, hrUserId);
        String type = null;
        String textValue = null;
        // sql开始结束时间
        String tdkwFilterStartdateStr = null;
        String tdkwFilterEnddateStr = null;
        // 在办已办标识
        boolean isDoing = true;
        for (QFilter qFilter : getQFilters()) {
            String property = qFilter.getProperty();
            switch (property) {
                case "tdkw_billtype":
                    type = (String) qFilter.getValue();
                    break;
                case "tdkw_startdate":
                    Object value = qFilter.getValue();
                    logger.info("tdkw_startdate的数据" + value);
                    userFilter.and("submitdate", QCP.large_equals, value);
                    infoApprovalFilter.and("modifytime", QCP.large_equals, value);
                    break;
                case "tdkw_date":
                    QFilter[] dateFilter = qFilter.recombine();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Object startDate = dateFilter[0].getValue();
                    Object endDate = dateFilter[1].getValue();
                    logger.info("startDate的数据" + startDate);
                    logger.info("endDate的数据" + endDate);
                    tdkwFilterStartdateStr = sdf.format(startDate)+" 00:00:00";
                    tdkwFilterEnddateStr=sdf.format(endDate)+" 00:00:00";
                    logger.info("修改后startDate的数据" + tdkwFilterStartdateStr);
                    logger.info("修改后endDate的数据" + tdkwFilterEnddateStr);
                    //在办
                    if (StringUtils.equals("false", finish)) {
                        QFilter[] recombine = qFilter.recombine();
                        QFilter startQfilter = recombine[0];
                        startQfilter.setProperty("createtime");
                        QFilter endQfilter = recombine[1];
                        endQfilter.setProperty("createtime");
                        userFilter.and(startQfilter).and(endQfilter);
                        startQfilter.setProperty("createtime");
                        endQfilter.setProperty("createtime");
                        infoApprovalFilter.and(startQfilter).and(endQfilter);
                        HRPersonFilter.and(startQfilter).and(endQfilter);
                    } else if (StringUtils.equals("true", finish)) {
                        //已办
                        QFilter[] recombine = qFilter.recombine();
                        QFilter startQfilter = recombine[0];
                        startQfilter.setProperty("submitdate");
                        QFilter endQfilter = recombine[1];
                        endQfilter.setProperty("submitdate");
                        userFilter.and(startQfilter).and(endQfilter);
                        startQfilter.setProperty("modifytime");
                        endQfilter.setProperty("modifytime");
                        infoApprovalFilter.and(startQfilter).and(endQfilter);
                        HRPersonFilter.and(startQfilter).and(endQfilter);
                    }
                    break;
                case "1":
                    textValue = String.valueOf(qFilter.getValue());
                    break;
                default:
                    break;
            }
        }

        // sql状态
        StringBuilder statusBuilder = new StringBuilder();
        // 申请类型
        if (StringUtils.equals("false", finish)) {
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
        } else if (StringUtils.equals("true", finish)) {
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
        }
        String statusStr = statusBuilder.toString();
        String algoKey = this.getClass().getName();
        // 离职单
        // 只取自己离职的单
        QFilter quitQfilter = new QFilter("applytype", QCP.equals, "2");
        DataSet bill1 = ORM.create().queryDataSet(algoKey, "htm_quitapplyemp",
                "id as id,tdkw_changetype.name as tdkw_type,'离职申请' as tdkw_billno, id as tdkw_billid,'1' as tdkw_billtype,submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime as tdkw_createtime", new QFilter[]{userFilter, quitQfilter});
        logger.info("userFilter的条件" + userFilter);

        // 请假单
        // 只取自己请假的单
        // QFilter vaApplyQfilter = new QFilter("applytyperadio", QCP.equals, "0");
        // QFilter bill2Filter = new QFilter("ischange", QCP.equals, Boolean.FALSE);
        // DataSet bill2 = ORM.create().queryDataSet(algoKey, "wtabm_vaapplyself",
        //         "id as id,entryentity.entryvacationtype.name as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'2' as tdkw_billtype," +
        //                "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss", new QFilter[]{userFilter, bill2Filter, vaApplyQfilter});

        // 2023年10月9日09:47:19 改为sql查询
        StringBuilder bill2SqlBuilder = new StringBuilder();
        bill2SqlBuilder.append("/*dialect*/ ");
        bill2SqlBuilder.append("select ");
        bill2SqlBuilder.append("sub.fid as id, ");
        bill2SqlBuilder.append("STRING_AGG(distinct sub.fname::text, ',') as tdkw_type, ");
        bill2SqlBuilder.append("'请假申请（开始时间：' || sub.startdate || '，' || STRING_AGG(CONCAT(sub.fname, '共', sub.total_hours), ',') || '）' as tdkw_billno, ");
        bill2SqlBuilder.append("sub.fid as tdkw_billid, ");
        bill2SqlBuilder.append("'2' as tdkw_billtype, ");
        bill2SqlBuilder.append("DATE(sub.fsubmitdate) as tdkw_submitdate, ");
//        bill2SqlBuilder.append("sub.fbillstatus as tdkw_billstatuss, ");
        // 处理请假单特殊状态
        bill2SqlBuilder.append(" case ");
        bill2SqlBuilder.append("    when sub.fauditstatus = 'C' then '1' ");
        bill2SqlBuilder.append("    else sub.fauditstatus ");
        bill2SqlBuilder.append(" end as tdkw_billstatuss, ");
        bill2SqlBuilder.append("sub.fcreatetime as tdkw_createtime ");
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
            bill2SqlBuilder.append("and ").append(isDoing ? "vaapply.fcreatetime" : "vaapply.fsubmitdate").append(" < '").append(tdkwFilterEnddateStr).append("'");
        }
        bill2SqlBuilder.append("group by vaapply.fid, vaapply.fbillno, vatype.fname) sub ");
        bill2SqlBuilder.append("group by sub.fid, sub.fbillno, sub.fsubmitdate, sub.fauditstatus, sub.fcreatetime, sub.startdate, lastdate");
        logger.info("bill2的sql" + bill2SqlBuilder);
        DataSet bill2 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill2SqlBuilder.toString());

        // 销假单
        // 只取自己销假的单
        // QFilter bill3Filter = new QFilter("ischange", QCP.equals, Boolean.TRUE);
        // DataSet bill3 = ORM.create().queryDataSet(algoKey, "wtabm_vaupdateself",
        //        "id as id,entryentity.entryvacationtype.name as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'3' as tdkw_billtype," +
        //                "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss", new QFilter[]{userFilter, bill3Filter});

        // 2023年10月9日10:24:40 改为sql查询
        StringBuilder bill3SqlBuilder = new StringBuilder();
        bill3SqlBuilder.append("/*dialect*/ ");
        bill3SqlBuilder.append("select ");
        bill3SqlBuilder.append("sub.fid as id, ");
        bill3SqlBuilder.append("STRING_AGG(distinct sub.fname::text, ',') as tdkw_type, ");
        bill3SqlBuilder.append("'销假申请（开始时间：' || sub.startdate || '，' || STRING_AGG(CONCAT(sub.fname, '共', ROUND(sub.total_hours, 1), '小时'), ',') || '）' as tdkw_billno, ");
        bill3SqlBuilder.append("sub.fid as tdkw_billid, ");
        bill3SqlBuilder.append("'3' as tdkw_billtype, ");
        bill3SqlBuilder.append("DATE(sub.fsubmitdate) as tdkw_submitdate, ");
        bill3SqlBuilder.append("sub.fbillstatus as tdkw_billstatuss, ");
        bill3SqlBuilder.append("sub.fcreatetime as tdkw_createtime ");
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
            bill3SqlBuilder.append("and ").append(isDoing ? "vaapply.fcreatetime" : "vaapply.fsubmitdate").append(" < '").append(tdkwFilterEnddateStr).append("'");
        }
        bill3SqlBuilder.append("group by vaapply.fid, vaapply.fbillno, vatype.fname) sub ");
        bill3SqlBuilder.append("group by sub.fid, sub.fbillno, sub.fsubmitdate, sub.fbillstatus, sub.fcreatetime, sub.startdate, lastdate");
        logger.info("bill3的sql" + bill3SqlBuilder);
        DataSet bill3 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill3SqlBuilder.toString());

        // 出差单---提交日期已添加
//        DataSet bill4 = ORM.create().queryDataSet(algoKey, "tdkw_reqtripe",
//                "id as id,tdkw_triptype.name as tdkw_type,'出差申请（' || '出差地点：' || tdkw_travelsummary || '， 出差时间：' || tdkw_triptimeyc || '， 出差天数：' || tdkw_tripnum || '）' AS tdkw_billno,id as tdkw_billid,'4' as tdkw_billtype," +
//                        "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime as tdkw_createtime", new QFilter[]{userFilter});


        // 2023年11月9日10:28:46 改sql查询
        StringBuilder bill4SqlBuilder = new StringBuilder();
        bill4SqlBuilder.append("/*dialect*/ select ");
        bill4SqlBuilder.append("tdkw_billid as id, ");
        bill4SqlBuilder.append(" tdkw_type, ");
        bill4SqlBuilder.append(" '出差申请（' || '出差地点：' || fk_tdkw_travelsummary || '， 出差时间：' || fk_tdkw_triptimeyc || '， 出差天数：' || fk_tdkw_tripnum || '）' as tdkw_billno , ");
        bill4SqlBuilder.append(" tdkw_billid, ");
        bill4SqlBuilder.append(" '4' as tdkw_billtype, ");
        bill4SqlBuilder.append(" tdkw_submitdate, ");
        bill4SqlBuilder.append(" tdkw_billstatuss , ");
        bill4SqlBuilder.append(" createtime as tdkw_createtime ");
        bill4SqlBuilder.append(" from  ( ");
        bill4SqlBuilder.append(" select  ");
        bill4SqlBuilder.append(" trip.fid as tdkw_billid, ");
        bill4SqlBuilder.append(" triptype.fname as tdkw_type, ");
        bill4SqlBuilder.append(" trip.fk_tdkw_triptimeyc, ");
        bill4SqlBuilder.append(" trip.fbillno as tdkw_billno_hide, ");
        bill4SqlBuilder.append(" trip.fbillstatus as tdkw_billstatuss, ");
        bill4SqlBuilder.append(" trip.fcreatetime as createtime, ");
        bill4SqlBuilder.append(" trip.fcreatetime as lastdate, ");
        bill4SqlBuilder.append(" trip.fk_tdkw_submitdate as tdkw_submitdate, ");
        bill4SqlBuilder.append(" case ");
        bill4SqlBuilder.append(" when array_upper(string_to_array(trip.fk_tdkw_travelsummary, '、'), 1) > 3 then array_to_string(array(select unnest(string_to_array(trip.fk_tdkw_travelsummary, '、')) limit 3), '、') || '等' ");
        bill4SqlBuilder.append(" else fk_tdkw_travelsummary ");
        bill4SqlBuilder.append(" end as fk_tdkw_travelsummary, ");
        bill4SqlBuilder.append(" trip.fk_tdkw_tripnum ");
        bill4SqlBuilder.append(" from tk_tdkw_reqtripe trip ");
        bill4SqlBuilder.append(" left join t_wtbd_traveltype triptype on  ");
        bill4SqlBuilder.append(" trip.fk_tdkw_triptype = triptype.fid ");
        bill4SqlBuilder.append(" where trip.fcreatorid = '").append(currentUserId).append("'");
        if (StringUtils.isNotBlank(statusStr)) {
            bill4SqlBuilder.append("and trip.fbillstatus in (").append(statusStr).append(")");
        }
        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
            bill4SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
        }
        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
            bill4SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" < '").append(tdkwFilterEnddateStr).append("'");
        }
        bill4SqlBuilder.append(" ) z ");
        logger.info("bill4的sql" + bill4SqlBuilder);
        DataSet bill4 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill4SqlBuilder.toString());

        // 销差单
//        DataSet bill5 = ORM.create().queryDataSet(algoKey, "tdkw_destroytripe",
//                "id as id,tdkw_triptype.name as tdkw_type,'销差申请（' || '出差地点：' || tdkw_travelsummary || '， 出差时间：' || tdkw_triptimeyc || '， 出差天数：' || tdkw_tripetime || '）' as tdkw_billno,id as tdkw_billid,'5' as tdkw_billtype," +
//                        "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime as tdkw_createtime", new QFilter[]{userFilter});


        // 2023年11月9日10:28:46 改sql查询
        StringBuilder bill5SqlBuilder = new StringBuilder();
        bill5SqlBuilder.append("/*dialect*/ select ");
        bill5SqlBuilder.append("tdkw_billid as id, ");
        bill5SqlBuilder.append(" tdkw_type, ");
        bill5SqlBuilder.append(" '销差申请（' || '出差地点：' || fk_tdkw_travelsummary || '， 出差时间：' || fk_tdkw_triptimeyc || '， 出差天数：' || fk_tdkw_tripnum || '）' as tdkw_billno , ");
        bill5SqlBuilder.append(" tdkw_billid, ");
        bill5SqlBuilder.append(" '5' as tdkw_billtype, ");
        bill5SqlBuilder.append(" tdkw_submitdate, ");
        bill5SqlBuilder.append(" tdkw_billstatuss , ");
        bill5SqlBuilder.append(" createtime as tdkw_createtime ");
        bill5SqlBuilder.append(" from  ( ");
        bill5SqlBuilder.append(" select  ");
        bill5SqlBuilder.append(" trip.fid as tdkw_billid, ");
        bill5SqlBuilder.append(" triptype.fname as tdkw_type, ");
        bill5SqlBuilder.append(" trip.fk_tdkw_triptimeyc, ");
        bill5SqlBuilder.append(" trip.fbillno as tdkw_billno_hide, ");
        bill5SqlBuilder.append(" trip.fbillstatus as tdkw_billstatuss, ");
        bill5SqlBuilder.append(" trip.fcreatetime as createtime, ");
        bill5SqlBuilder.append(" trip.fcreatetime as lastdate, ");
        bill5SqlBuilder.append(" trip.fk_tdkw_submitdate as tdkw_submitdate, ");
        bill5SqlBuilder.append(" case ");
        bill5SqlBuilder.append(" when array_upper(string_to_array(trip.fk_tdkw_travelsummary, '、'), 1) > 3 then array_to_string(array(select unnest(string_to_array(trip.fk_tdkw_travelsummary, '、')) limit 3), '、') || '等' ");
        bill5SqlBuilder.append(" else fk_tdkw_travelsummary ");
        bill5SqlBuilder.append(" end as fk_tdkw_travelsummary, ");
        bill5SqlBuilder.append(" trip.fk_tdkw_tripetime as fk_tdkw_tripnum ");
        bill5SqlBuilder.append(" from tk_tdkw_destroytrip trip ");
        bill5SqlBuilder.append(" left join t_wtbd_traveltype triptype on  ");
        bill5SqlBuilder.append(" trip.fk_tdkw_triptype = triptype.fid ");
        bill5SqlBuilder.append(" where trip.fcreatorid = '").append(currentUserId).append("'");
        if (StringUtils.isNotBlank(statusStr)) {
            bill5SqlBuilder.append("and trip.fbillstatus in (").append(statusStr).append(")");
        }
        if (StringUtils.isNotBlank(tdkwFilterStartdateStr)) {
            bill5SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" >= '").append(tdkwFilterStartdateStr).append("'");
        }
        if (StringUtils.isNotBlank(tdkwFilterEnddateStr)) {
            bill5SqlBuilder.append("and ").append(isDoing ? "trip.fcreatetime" : "trip.fk_tdkw_submitdate").append(" < '").append(tdkwFilterEnddateStr).append("'");
        }
        bill5SqlBuilder.append(" ) z ");
        logger.info("bill5的sql" + bill5SqlBuilder);
        DataSet bill5 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill5SqlBuilder.toString());


        // 补卡单---提交日期已添加
        // 只查自己的
        // QFilter qFilter = new QFilter("applytyperadio", QCP.equals, "0");
        // DataSet bill6 = ORM.create().queryDataSet(algoKey, "wtpm_supsignself",
        //        "id as id,entryentity.applyreason.name as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'6' as tdkw_billtype," +
        //                "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss", new QFilter[]{userFilter, qFilter});

        // 2023年10月8日19:36:14 改为sql语句查询
        StringBuilder bill6SqlBuilder = new StringBuilder();
        bill6SqlBuilder.append("/*dialect*/ ");
        bill6SqlBuilder.append("select ");
        bill6SqlBuilder.append("subquery.fid as id, ");
        bill6SqlBuilder.append("subquery.reason as tdkw_type, ");
        bill6SqlBuilder.append("'补卡申请（补卡日期：' || ");
        bill6SqlBuilder.append("case ");
        bill6SqlBuilder.append("when array_length(dates, 1) > 3 then array_to_string(dates[1:3], '、') || '等' ");
        bill6SqlBuilder.append("else array_to_string(dates, '、') ");
        bill6SqlBuilder.append("end || '）' as tdkw_billno, ");
        bill6SqlBuilder.append("subquery.fid as tdkw_billid, ");
        bill6SqlBuilder.append("'6' as tdkw_billtype, ");
        bill6SqlBuilder.append("DATE(subquery.fsubmitdate) as tdkw_submitdate, ");
        bill6SqlBuilder.append("subquery.fbillstatus as tdkw_billstatuss, ");
        bill6SqlBuilder.append("subquery.fcreatetime as tdkw_createtime ");
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
            bill6SqlBuilder.append("and ").append(isDoing ? "supsign.fcreatetime" : "supsign.fsubmitdate").append(" < '").append(tdkwFilterEnddateStr).append("'");
        }
        bill6SqlBuilder.append("group by ");
        bill6SqlBuilder.append("supsign.fid, ");
        bill6SqlBuilder.append("supsign.fbillno, ");
        bill6SqlBuilder.append("supsign.fsubmitdate, ");
        bill6SqlBuilder.append("supsign.fbillstatus, ");
        bill6SqlBuilder.append("supsign.fcreatetime) subquery");
        logger.info("bill6的sql" + bill6SqlBuilder);
        DataSet bill6 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill6SqlBuilder.toString());

        // 按时段申请
        // QFilter bill7FilterA = new QFilter("otapplytype", QCP.equals, "1");
        // 按时长申请
        // QFilter bill7FilterB = new QFilter("otapplytype", QCP.equals, "2");
        // 加班单  只查自己申请的
        // 按时段申请
        // DataSet bill701 = ORM.create().queryDataSet(algoKey, "wtom_otbillself",
        //         "id as id,sdentry.sdottype.name as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'7' as tdkw_billtype," +
        //                 "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss", new QFilter[]{userFilter, bill7FilterA, qFilter});
        // 按时长申请
        // DataSet bill702 = ORM.create().queryDataSet(algoKey, "wtom_otbillself",
        //         "id as id,scentry.scottype.name as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'7' as tdkw_billtype," +
        //                 "submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss", new QFilter[]{userFilter, bill7FilterB, qFilter});

        // 2023年10月9日15:39:50 改为sql查询
        StringBuilder bill7SqlBuilder = new StringBuilder();
        bill7SqlBuilder.append("/*dialect*/ ");
        bill7SqlBuilder.append("SELECT ");
        bill7SqlBuilder.append("otapply.fid as id, ");
        bill7SqlBuilder.append("STRING_AGG(distinct ottype.fname ::text, ',') as tdkw_type, ");
        bill7SqlBuilder.append("'加班申请（开始日期：' || subquery.min_date ||  '， 加班时长：' || SPLIT_PART(otapply.fvatimetext, 'h', 1) || 'h' || '）' as tdkw_billno, ");
        bill7SqlBuilder.append("otapply.fid as tdkw_billid, ");
        bill7SqlBuilder.append("'7' as tdkw_billtype, ");
        bill7SqlBuilder.append("otapply.fsubmitdate as tdkw_submitdate, ");
        bill7SqlBuilder.append("otapply.fbillstatus as tdkw_billstatuss, ");
        bill7SqlBuilder.append("otapply.fcreatetime as tdkw_createtime ");
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
            bill7SqlBuilder.append("and ").append(isDoing ? "otapply.fcreatetime" : "otapply.fsubmitdate").append(" < '").append(tdkwFilterEnddateStr).append("'");
        }
        bill7SqlBuilder.append("GROUP BY ");
        bill7SqlBuilder.append("tdkw_billno, ");
        bill7SqlBuilder.append("tdkw_billid, ");
        bill7SqlBuilder.append("tdkw_billtype, ");
        bill7SqlBuilder.append("tdkw_submitdate, ");
        bill7SqlBuilder.append("tdkw_billstatuss ");
        logger.info("bill7的sql" + bill7SqlBuilder);
        DataSet bill7 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill7SqlBuilder.toString());

        // 证明单---提交日期已添加
        DataSet bill8 = ORM.create().queryDataSet(algoKey, "tdkw_prove_handle",
                "id as id,tdkw_basedatafield.name as tdkw_type,'证明申请（证明类型：' || tdkw_basedatafield.name || '）' as tdkw_billno,id as tdkw_billid,'8' as tdkw_billtype,submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime as tdkw_createtime",
                new QFilter[]{userFilter});

        // 个人信息修改单---提交日期待确认（提交变更时间）
        // DataSet bill9 = ORM.create().queryDataSet(algoKey, "hspm_infoapproval",
        //        "id as id,'修改' as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'9' as tdkw_billtype,modifytime as tdkw_submitdate,billstatus as tdkw_billstatuss", new QFilter[]{infoApprovalFilter});

        // 2023年10月9日16:24:30 改为sql查询
        StringBuilder bill9SqlBuilder = new StringBuilder();
        bill9SqlBuilder.append("/*dialect*/ ");
        bill9SqlBuilder.append("select ");
        bill9SqlBuilder.append("subquery.tdkw_billid as id, ");
        bill9SqlBuilder.append("'修改' as tdkw_type, ");
        bill9SqlBuilder.append("'个人信息修改申请（' || ");
        bill9SqlBuilder.append("case ");
        bill9SqlBuilder.append("    when array_length(typenames, 1) > 3 then regexp_replace(array_to_string(typenames[1:3], '、'), '[A-Z]', '', 'g') || '等' ");
        bill9SqlBuilder.append("    else regexp_replace(array_to_string(typenames, '、'), '[A-Z]', '', 'g') ");
        bill9SqlBuilder.append("end || '）' as tdkw_billno, ");
        bill9SqlBuilder.append("subquery.tdkw_billid, ");
        bill9SqlBuilder.append("'9' as tdkw_billtype, ");
        bill9SqlBuilder.append("subquery.tdkw_submitdate, ");
        bill9SqlBuilder.append("subquery.tdkw_billstatuss, ");
        bill9SqlBuilder.append("subquery.createtime as tdkw_createtime ");
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
            bill9SqlBuilder.append(" and info.fmodifytime < '").append(tdkwFilterEnddateStr).append("'");
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
        logger.info("bill9的sql" + bill9SqlBuilder);
        DataSet bill9 = DB.queryDataSet(algoKey, DBRoute.of("hr"), bill9SqlBuilder.toString());

        // 问询单---提交日期已添加
        DataSet bill10 = ORM.create().queryDataSet(algoKey, "tdkw_employee_inquiries",
                "id as id,tdkw_basedatafield.name as tdkw_type, '问询单（办理事项：' || tdkw_basedatafield.name || '）'  as tdkw_billno,id as tdkw_billid,'10' as tdkw_billtype,submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime as tdkw_createtime",
                new QFilter[]{userFilter});

        // 招聘需求单---提交日期已添加
        DataSet bill11 = ORM.create().queryDataSet(algoKey, "tdkw_rec_apply_bill",
                "id as id,tdkw_rec_type.name as tdkw_type,tdkw_rec_apply_dept,tdkw_rec_in_posi_name.name as post_name,'招聘需求（'  as tdkw_billno,id as tdkw_billid,'11' as tdkw_billtype,submitdate as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime as tdkw_createtime",
                new QFilter[]{userFilter});
        // 招聘需求部门id
        DynamicObjectCollection bill11DeptIds = QueryServiceHelper.query("tdkw_rec_apply_bill", "tdkw_rec_apply_dept", userFilter.toArray());
        List<Long> bill11DeptIdList = bill11DeptIds.stream().mapToLong(dynamicObject -> dynamicObject.getLong("tdkw_rec_apply_dept")).boxed().collect(Collectors.toList());
        QFilter orgFilter = new QFilter("id", QCP.in, bill11DeptIdList);
        orgFilter.and("iscurrentversion", QCP.equals, '1');
        orgFilter.and("datastatus", QCP.equals, '1');
        orgFilter.and("enable", QCP.equals, '1');
        DataSet recDeptDataSet = QueryServiceHelper.queryDataSet(algoKey, "haos_adminorghr", "id as dept_id,name", orgFilter.toArray(), "");
        bill11 = bill11.leftJoin(recDeptDataSet).on("tdkw_rec_apply_dept", "dept_id").select("id", "tdkw_type", "tdkw_billno", "name", "post_name", "tdkw_billid", "tdkw_billtype",
                "tdkw_submitdate", "tdkw_billstatuss", "tdkw_createtime").finish();
        // 分组拼接部门、内部职位
        StringBuilder recExecSqlBuilder = new StringBuilder();
        recExecSqlBuilder.append("SELECT ")
                .append("id, ")
                .append("tdkw_type, ")
                .append("(tdkw_billno + name + '，' + post_name + '）') AS tdkw_billno, ")
                .append("tdkw_billid, ")
                .append("tdkw_billtype, ")
                .append("tdkw_submitdate, ")
                .append("tdkw_billstatuss, ")
                .append("tdkw_createtime ")
                .append("GROUP BY ")
                .append("id, ")
                .append("tdkw_type, ")
                .append("tdkw_billno, ")
                .append("name, ")
                .append("post_name, ")
                .append("tdkw_billid, ")
                .append("tdkw_billtype, ")
                .append("tdkw_submitdate, ")
                .append("tdkw_billstatuss, ")
                .append("tdkw_createtime ");
        bill11 = bill11.executeSql(recExecSqlBuilder.toString());

        // 银行卡变更申请单
        DataSet bill14 = ORM.create().queryDataSet(algoKey, "hsas_perbceditbill",
                "id,'银行卡信息维护' as tdkw_type,'银行卡信息维护' as tdkw_billno,id as tdkw_billid,'14' as tdkw_billtype," +
                        "createtime as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime", new QFilter[]{HRPersonFilter});

        // 个人信息删除单---提交日期待确认（提交变更时间）
        //DataSet bill12 = ORM.create().queryDataSet(algoKey, "tdkw_hspm_infochg", "id as id,'删除' as tdkw_type,billno as tdkw_billno,id as tdkw_billid,'12' as tdkw_billtype,modifytime as tdkw_submitdate,billstatus as tdkw_billstatuss,createtime as tdkw_createtime", new QFilter[]{infoApprovalFilter});

        DataSet finalData = bill1.union(bill2).union(bill3).union(bill4).union(bill5).union(bill6).union(bill7)
                .union(bill8).union(bill9).union(bill10).union(bill11).union(bill14);
        if (!StringUtils.equals("0", type) && StringUtils.isNotEmpty(type)) {
            Map<String, Object> params = new HashMap<>();
            params.put("tdkwFilterBilltype", type);
            finalData = finalData.filter("tdkw_billtype = tdkwFilterBilltype", params);
        }
        // 去重
        DataSet dataSet = finalData.executeSql("select distinct id,tdkw_type,tdkw_billno,tdkw_billtype,tdkw_submitdate,tdkw_createtime," +
                "tdkw_billstatuss,tdkw_billid group by id, tdkw_billno,tdkw_billtype,tdkw_submitdate,tdkw_createtime,tdkw_billstatuss,tdkw_billid,tdkw_type");

        // 分组合并
        dataSet = dataSet.executeSql("select id, tdkw_billno,tdkw_billtype,tdkw_submitdate,tdkw_createtime,tdkw_billstatuss,tdkw_billid," +
                "group_concat(tdkw_type) group by id, tdkw_billno,tdkw_billtype,tdkw_submitdate,tdkw_createtime,tdkw_billstatuss,tdkw_billid");
        DataSet dataSetOrder;
        if (StringUtils.equals("false", finish)) {
            // 在办申请
            String[] submitDate = new String[1];
            submitDate[0] = "tdkw_createtime desc";
            dataSet = dataSet.orderBy(submitDate);
        } else if (StringUtils.equals("true", finish)) {
            // 已办申请
            String[] submitDate = new String[1];
            submitDate[0] = "tdkw_submitdate desc";
            dataSet = dataSet.orderBy(submitDate);
        }


        // dataSet.addField("id",null);
        dataSetOrder = dataSet.map(new MapFunction() {
            @Override
            public RowMeta getResultRowMeta() {
                String[] fieldNames = new String[]{"id", "tdkw_type", "tdkw_billno", "tdkw_billid", "tdkw_billtype", "tdkw_submitdate", "tdkw_billstatuss", "tdkw_createtime"};
                DataType[] dataTypes = new DataType[]{DataType.StringType, DataType.StringType, DataType.StringType, DataType.StringType, DataType.StringType, DataType.TimestampType, DataType.StringType, DataType.TimestampType};
                return new RowMeta(fieldNames, dataTypes);
            }

            @Override
            public Object[] map(Row row) {
                return new Object[]{row.get("id") + ":" + row.get("tdkw_billtype") + ":" + row.get("tdkw_billstatuss") + ":" + row.get("tdkw_billno"), row.get("tdkw_type"), row.get("tdkw_billno"), row.get("tdkw_billid"), row.get("tdkw_billtype"), row.get("tdkw_submitdate"), row.get("tdkw_billstatuss"), row.get("tdkw_createtime")};

            }
        });
        //已办
        // 离职单、请假单、销假单、出差单、销差单、补卡单、加班单、个人信息修改单 单据状态编码统一
        // A 暂存
        // B 已提交
        // G 待重新提交
        // D 审批中
        // C 审批通过
        // E 审批不通过
        // F 已废弃
        // 证明单
        // A 待提交
        // B 已提交
        // C 已审核
        // 问询单
        // A 待提交
        // B 处理中
        // C 已完成
        // 招聘需求单
        // A 暂存
        // B 已提交
        // C 已审核
        //在办
        DynamicObjectCollection collection = ORM.create().toPlainDynamicObjectCollection(dataSetOrder);
        //对搜索框的单据类别进行单独处理
        List<String> billTypeStrList = new ArrayList<>();
        boolean filterBillType = false;
        if (StringUtils.isNotBlank(textValue)) {
            int index = textValue.lastIndexOf("#");
            if (index != 0) {
                textValue = textValue.substring(textValue.lastIndexOf("#") + 1);
            }
            HashMap<String, String> map = createMap();
            for (String key : map.keySet()) {
                String value = map.get(key);
                if (value.contains(textValue)) {
                    billTypeStrList.add(key);
                    filterBillType = true;
                }
            }
            for (int i = 0; i < collection.size(); i++) {
                DynamicObject dynamicObject = collection.get(i);
                String billType = dynamicObject.getString("tdkw_billtype");
                if (filterBillType) {
                    //过滤单据类别
                    if (!(dynamicObject.getString("tdkw_billno").contains(textValue)
                            || dynamicObject.getString("tdkw_type").contains(textValue)
                            || billTypeStrList.contains(billType))) {
                        collection.remove(dynamicObject);
                        i--;
                    }
                } else {
                    //只过滤编码和类别
                    if (!(dynamicObject.getString("tdkw_billno").contains(textValue)
                            || dynamicObject.getString("tdkw_type").contains(textValue)
                    )) {
                        collection.remove(dynamicObject);
                        i--;
                    }
                }

            }
        }
        return collection;


    }

    private static HashMap<String, String> createMap() {
        HashMap<String, String> map = Maps.newHashMapWithExpectedSize(11);
        map.put("1", "离职申请单");
        map.put("2", "请假申请单");
        map.put("3", "销假申请单");
        map.put("4", "出差申请单");
        map.put("5", "销差申请单");
        map.put("6", "补卡申请单");
        map.put("7", "加班申请单");
        map.put("8", "证明办理单");
        map.put("9", "个人信息修改申请单");
        map.put("10", "问询单");
        map.put("11", "招聘需求单");
        map.put("12", "个人信息删除申请单");
        map.put("14", "银行卡信息维护申请单");
        return map;
    }

}
