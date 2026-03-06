package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.JoinDataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.SqlBuilder;
import kd.bos.entity.report.*;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.fi.bd.util.QFBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Metadata： tdkw_personfileintegrity
 * @Description ： 人员档案完整度-查询
 * @ClassName ：PersonFileIntegrityRptListDataPlugin
 * @author xxx
 * @Date ：2023/6/14 17:17
 * @Version: 1.0
 */
public class PersonFileIntegrityRptListDataPlugin extends AbstractReportListDataPlugin {
    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        Map<String, Object> customParam = reportQueryParam.getCustomParam();
        FilterInfo filterInfo = reportQueryParam.getFilter();
        Boolean allperson = (Boolean) customParam.get("allperson");

        String selectFields = "number as tdkw_number,id as ermanid,tdkw_index,person as tdkw_person,id as tdkw_hrid,empposrel.company as tdkw_company,empposrel.adminorg as tdkw_dept,name as tdkw_name,empposrel.position as tdkw_position,empposrel.position.tdkw_positionlevel as tdkw_positionlevel,empentrel.laborreltype as tdkw_laborreltype,empposrel.adminorg.sortcode as sortcode";
        QFilter filter1 = new QFilter("empposrel.isprimary", QCP.equals, "1");
        filter1.and("iscurrentversion", QCP.equals, "1");
        filter1.and("initstatus", QCP.equals, "2");
        filter1.and("businessstatus", QCP.equals, "1");
        Set<Long> personIdsA = (Set<Long>) customParam.get("personIds_a");
        if (personIdsA.size() > 0) {
            filter1.and("person", QCP.in, personIdsA);
        }

        // 2023年10月25日16:44:36 改为sql查询、2023年11月8日09:27:26更新
        SqlBuilder propBuilder = new SqlBuilder();
        propBuilder.append("/*dialect*/");
        propBuilder.append("select ");
        propBuilder.append("fpersonid as tdkw_person, ");
        propBuilder.append("case ");
        propBuilder.append("when fk_tdkw_progressbar = 0 then '  0.00%' ");
        propBuilder.append("else to_char(100 * round(fk_tdkw_progressbar, 4), '999d00') || '%' ");
        propBuilder.append("end as tdkw_integrity ");
        propBuilder.append("from t_hrpi_pernontsprop ");
        propBuilder.append("where ");
        propBuilder.append("fiscurrentversion = '1' ");
        propBuilder.append("and finitstatus = '2' ");
        propBuilder.append("and fdatastatus = '1' ");
        if (allperson) {
            propBuilder.append("and fk_tdkw_progressbar < 1 ");
        }
        propBuilder.appendIn("and fpersonid ", personIdsA.toArray());
        DataSet pernontsprop = DB.queryDataSet("PersonFileIntegrityRptListDataPlugin.query.propBuilder", DBRoute.of("hr"), propBuilder);


        //表头筛选过滤-姓名
        List<FilterItemInfo> tableHeadFilterItems = filterInfo.getTableHeadFilterItems();
        for (FilterItemInfo tableHeadFilterItem : tableHeadFilterItems) {
            if ("tdkw_name".equals(tableHeadFilterItem.getPropName())) {
                filter1.and(filterItemInfoToQFilter("name", tableHeadFilterItem));
            }
        }

        DataSet ermanfile = QueryServiceHelper.queryDataSet("q2", "hspm_ermanfile", selectFields, filter1.toArray(), null);
        JoinDataSet join = ermanfile.leftJoin(pernontsprop);
        DataSet finish = join.on("tdkw_person", "tdkw_person").select(new String[]{"tdkw_number","ermanid", "tdkw_person", "tdkw_hrid", "tdkw_company", "tdkw_dept", "tdkw_name", "tdkw_position", "tdkw_positionlevel", "tdkw_laborreltype", "sortcode", "tdkw_index"},
                new String[]{"tdkw_integrity"}).finish();

        List<FilterItemInfo> filterItems = filterInfo.getFilterItems();
        if (filterItems.size() != 0) {
            //全选时剔除全选按钮标识
            if ("tdkw_choose".equals(filterItems.get(filterItems.size() - 1).getPropName())) {
                filterItems.remove(filterItems.size() - 1);
            }
        }
        QFBuilder qFilterBuilder = new QFBuilder();
        boolean target = false;
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Boolean value = (Boolean) filterItem.getValue();
            if (value) {
                target = true;
                QFilter w = new QFilter(propName, QCP.equals, value);
                if (qFilterBuilder.size() == 0) {
                    qFilterBuilder.add(w);
                } else {
                    qFilterBuilder.or(w);
                }
            }
        }
        if (target) {
            qFilterBuilder.and(new QFilter("tdkw_person.person.id", QCP.in, personIdsA));
            DataSet personnelInfor = QueryServiceHelper.queryDataSet("q0", "tdkw_hspm_personnelinfor", "tdkw_person as tdkw_hrid", qFilterBuilder.toArray(), null);
            JoinDataSet join2 = personnelInfor.leftJoin(finish);
            finish = join2.on("tdkw_hrid", "tdkw_hrid").select(new String[]{"tdkw_number","ermanid", "tdkw_person", "tdkw_hrid", "tdkw_company", "tdkw_dept", "tdkw_name", "tdkw_position", "tdkw_positionlevel", "tdkw_laborreltype", "tdkw_integrity", "sortcode", "tdkw_index"},
                    null).finish();
            finish = finish.distinct();
        }
        // 2023年10月25日16:11:20 改为sql查询、2023年11月8日10:10:26更新
        SqlBuilder lineFormBuilder = new SqlBuilder();
        lineFormBuilder.append("/*dialect*/ ");
        lineFormBuilder.append("SELECT  ");
        lineFormBuilder.append("    fk_tdkw_person,  ");
        lineFormBuilder.append("    STRING_AGG(DISTINCT tdkw_lackfield, ', ') AS tdkw_lackfield ");
        lineFormBuilder.append("FROM (SELECT   ");
        lineFormBuilder.append(" fk_tdkw_person , ");
        lineFormBuilder.append(" CONCAT_WS(', ', ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_lackpost = '1' THEN '缺少岗位' END, CASE WHEN fk_tdkw_domainaccount = '1' THEN '缺少域账号' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_placestowork = '1' THEN '缺少工作地点' END,CASE WHEN fk_tdkw_middlemanager = '1' THEN '缺少首任中层时间' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_seniorexecutive = '1' THEN '缺少首任高层时间' END, CASE WHEN fk_tdkw_photograph = '1' THEN '缺少照片' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_admissiontime = '1' THEN '缺少入党(团)日期' END, CASE WHEN fk_tdkw_nativeplace = '1' THEN '缺少籍贯' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_birthplace = '1' THEN '缺少出生地' END, CASE WHEN fk_tdkw_organization = '1' THEN '缺少组织' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_politicalstatus = '1' THEN '缺少政治面貌' END,CASE WHEN fk_tdkw_professionaltitle = '1' THEN '缺少职称' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_grouptime = '1' THEN '缺少加入集团时间' END,CASE WHEN fk_tdkw_jioncompanytime = '1' THEN '缺少加入本公司时间' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_maritalstatus = '1' THEN '缺少婚姻状况' END,CASE WHEN fk_tdkw_homeaddress = '1' THEN '缺少家庭住址' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_starttime = '1' THEN '缺少履历开始日期' END,CASE WHEN fk_tdkw_resumecompany = '1' THEN '缺少履历公司' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_resumeposition = '1' THEN '缺少履历岗位' END,CASE WHEN fk_tdkw_age = '1' THEN '缺少年龄' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_educational = '1' THEN '缺少学历' END,CASE WHEN fk_tdkw_degree = '1' THEN '缺少学位' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_relationship = '1' THEN '缺少家庭成员与本人关系' END,CASE WHEN fk_tdkw_membername = '1' THEN '缺少家庭成员姓名' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_familybirth = '1' THEN '缺少家庭成员出生日期' END,CASE WHEN fk_tdkw_workunit = '1' THEN '缺少家庭成员工作单位' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_memberduties = '1' THEN '缺少家庭成员职务' END,CASE WHEN fk_tdkw_familypolitical = '1' THEN '缺少家庭成员政治面貌' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_contactname = '1' THEN '缺少紧急联系人姓名' END,CASE WHEN fk_tdkw_contactcellphone = '1' THEN '缺少紧急联系人手机' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_companyage = '1' THEN '缺少集团司龄' END,CASE WHEN fk_tdkw_lackofcontract = '1' THEN '缺少合同信息' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_absenceresume = '1' THEN '履历断月' END,CASE WHEN fk_tdkw_resumestart = '1' THEN '履历开始时间断档' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_workingerror = '1' THEN '首段正式任职开始时间与参加工作时间不一致' END,CASE WHEN fk_tdkw_lackofeducational = '1' THEN '缺少在职最高学历' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_lackofparents = '1' THEN '家庭成员缺少父亲或母亲' END,CASE WHEN fk_tdkw_phone = '1' THEN '缺少手机' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_lackofinlawparent = '1' THEN '缺少岳父母/公婆' END,CASE WHEN fk_tdkw_lackofmate = '1' THEN '缺少配偶' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_disqualification = '1' THEN '学历断档' END,CASE WHEN fk_tdkw_worktime = '1' THEN '缺少参加工作时间' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_permanentaddress = '1' THEN '缺少常住地址' END, CASE WHEN fk_tdkw_domicile = '1' THEN '缺少户口所在地' END,");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_familybirthanomal = '1' THEN '家庭成员出生日期异常' END,CASE WHEN fk_tdkw_checkboxfield = '1' THEN '缺少最高学历' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_higheducational = '1' THEN '缺少全日制最高学历' END,CASE WHEN fk_tdkw_emily = '1' THEN '缺少公司邮箱' END, ");
        lineFormBuilder.append(" CASE WHEN fk_tdkw_positionlevel = '1' THEN '缺少岗位层级' END,CASE WHEN fk_tdkw_gender = '1' THEN '缺少性别' END ");
        lineFormBuilder.append(" ) AS tdkw_lackfield  ");
        lineFormBuilder.append(" FROM tk_tdkw_personnelinforme  ) AS subquery ");
        lineFormBuilder.append(" GROUP BY fk_tdkw_person ");
        DataSet lineForm = DB.queryDataSet("PersonFileIntegrityRptListDataPlugin.query.lineFormBuilder", DBRoute.of("hr"), lineFormBuilder);

        finish = finish.leftJoin(lineForm).on("ermanid", "fk_tdkw_person").select("tdkw_number","ermanid", "tdkw_person", "tdkw_hrid", "tdkw_company", "tdkw_dept", "tdkw_name", "tdkw_position", "tdkw_positionlevel",
                "tdkw_laborreltype", "tdkw_integrity", "tdkw_lackfield", "sortcode", "tdkw_index").finish();
        finish = finish.orderBy(new String[]{"sortcode", "tdkw_index"});
        finish = finish.filter(new FilterFunction() {
            @Override
            public boolean test(Row row) {
                String tdkw_integrity = row.getString("tdkw_integrity");
                return null != tdkw_integrity;
            }
        });
        return finish;
    }


    @Override
    public List<AbstractReportColumn> getColumns(List<AbstractReportColumn> columns) throws Throwable {

//        columns.add(createReprotColumn("id","long","人事id"));
//        columns.add(createReprotColumn("company","text","公司"));
//        columns.add(createReprotColumn("dept","text","组长"));
//        columns.add(createReprotColumn("name","text","姓名"));
//        columns.add(createReprotColumn("position","text","岗位"));

        return super.getColumns(columns);
    }

    public ReportColumn createReprotColumn(String field, String fieldType, String caption) {
        ReportColumn reportColumn = new ReportColumn();
        reportColumn.setFieldKey(field);
        reportColumn.setCaption(new LocaleString(caption));
        reportColumn.setFieldType(fieldType);
        return reportColumn;
    }

    public QFilter filterItemInfoToQFilter(String property, FilterItemInfo filterItemInfo) {
        QFilter qFilter = new QFilter(property, filterItemInfo.getCompareType(), filterItemInfo.getValue());
        return qFilter;
    }


}
