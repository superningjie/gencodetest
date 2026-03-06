package tdkw.esc.myteam.formplugin;

import kd.bos.algo.Algo;
import kd.bos.algo.DataSet;
import kd.bos.algo.DataType;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.algo.RowMetaFactory;
import kd.bos.algo.input.CollectionInput;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.myteam.common.PersonSubordinateInfoUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @Author guokairong
 * @Date 2023/8/14 11:16
 */
public class SubordinatePersonRptPlugin extends AbstractReportListDataPlugin {
    /**
     * 报表的自定义字段
     * 公司名称、岗位、人员编码、姓名、缺少信息
     */
    private static String[] FIELDS = new String[]{"tdkw_person", "tdkw_mainpositions", "tdkw_age", "tdkw_entservicelen", "tdkw_servicelen", "tdkw_educate"};

    /**
     * 报表列表上所有字段对应的数据类型,必须和FIELDS中的元素一一对应
     * 公司名称、岗位、人员编码、姓名、缺少信息
     */
    private static final DataType[] DATATYPES = {DataType.LongType, DataType.LongType, DataType.IntegerType, DataType.BigDecimalType, DataType.BigDecimalType, DataType.LongType};
    private static final Log logger = LogFactory.getLog(SubordinatePersonRptPlugin.class);

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        // 创建一个空的DataSet
        Collection<Object[]> coll = new ArrayList<>();

        //将自定义的字段和类型对应插入
        RowMeta rowMeta = RowMetaFactory.createRowMeta(FIELDS, DATATYPES);
        CollectionInput inputs = new CollectionInput(rowMeta, coll);

        //Algo 创建dataSet
        DataSet resultDataSet = Algo.create(this.getClass().getName()).createDataSet(inputs);

        Map<String, Object> customParam = reportQueryParam.getCustomParam();
        Long positionId = (Long) customParam.get("positionId");
        Long personId = (Long) customParam.get("personId");
        Long collaborativeId = (Long) customParam.get("collaborativeId");

        //主兼岗
        DynamicObject positionFilter = reportQueryParam.getFilter().getDynamicObject("tdkw_mainpositionfilter");
        if (!ObjectUtils.isEmpty(positionFilter)) {
            positionId = (Long) positionFilter.getPkValue();
        }
        //领导
        DynamicObject leaderFilter = reportQueryParam.getFilter().getDynamicObject("tdkw_leader");
        if (!ObjectUtils.isEmpty(leaderFilter)) {
            personId = (Long) leaderFilter.getPkValue();
        }
        //所属组织
        DynamicObject organizationFilter = reportQueryParam.getFilter().getDynamicObject("tdkw_companyfilter");
        //任职类型
        DynamicObjectCollection postypeFilter = reportQueryParam.getFilter().getDynamicObjectCollection("tdkw_postype");

        //姓名
        String personName = reportQueryParam.getFilter().getString("tdkw_namefilter");

        //年龄
        int ageFilterStart = reportQueryParam.getFilter().getInt("tdkw_agestart");
        int ageFilterEnd = reportQueryParam.getFilter().getInt("tdkw_ageend");

        //司龄
        BigDecimal seniorityFilterStart = reportQueryParam.getFilter().getBigDecimal("tdkw_workagestart");
        BigDecimal seniorityFilterEnd = reportQueryParam.getFilter().getBigDecimal("tdkw_workageend");

        //工龄
        BigDecimal societyAgeFilterStart = reportQueryParam.getFilter().getBigDecimal("tdkw_senioritystart");
        BigDecimal societyAgeFilterEnd = reportQueryParam.getFilter().getBigDecimal("tdkw_seniorityend");

        //学历
        DynamicObject educationFilter = reportQueryParam.getFilter().getDynamicObject("tdkw_education");

        //查询下属信息表查下属
        QFilter underlingFilter = new QFilter("tdkw_manager", "=", personId);
        underlingFilter.and("tdkw_position", "=", positionId);
        if (collaborativeId.compareTo(0l) > 0) {
            underlingFilter.and("tdkw_reportcoreltype", "=", collaborativeId);
        }

        String partTime = System.getProperty("constant.hrmp.hbss.tdkw_hbss_changereason.fictitious");
        if (kd.bos.orm.util.StringUtils.isEmpty(partTime)) {
            partTime = "XY00017";
        }

        String educationSql = "left join(select edu.feducationid,edu.fpersonid from t_hrpi_pereduexp edu where edu.fiscurrentversion = '1'\n" +
//                "and edu.fk_tdkw_ishighestcheck = '1' and edu.fdatastatus = '1' ";
                // TODO 查旬标品的最高学历吧
                "and edu.fishighestdegree = '1' and edu.fdatastatus = '1' ";
        List<Map<String, Object>> subordinatePersonnelFilterList = PersonSubordinateInfoUtils.getSubordinatePersonnelFilter(personId, positionId, collaborativeId);

//        String sql = "select a.fpersonid person,a.fpositionid position,a.fpostypeid postype,b.fname,c.fname,d.fage age,per.fk_tdkw_newjointime entservicelen,per.fsocialworkage servicelen,e.feducationid education,er.fk_tdkw_index tdkw_index"
        String sql = "select a.fpersonid person,a.fpositionid position,a.fpostypeid postype,b.fname,c.fname,d.fage age,per.fcomsercount entservicelen,per.fsocialworkage servicelen,e.feducationid education,er.fid tdkw_index"
                + " from t_hrpi_empposorgrel a left join t_hbpm_position b on a.fpositionid = b.fid "
                + "left join t_hrpi_person c on c.fid=a.fpersonid "
                + "left join t_hrpi_pernontsprop d on d.fpersonid = a.fpersonid "
                + "left join t_hrpi_perserlen per on per.fpersonid = a.fpersonid "
//                + "left join tk_tdkw_changereason ch on ch.fid = a.fk_tdkw_changereason "
                + "left join t_hrpi_ermanfile er on er.fempposrelid = a.fid ";

        educationSql = educationSql + " )e on e.fpersonid = a.fpersonid";
        String sqlFilter = " where a.fdatastatus = '1' and a.fbusinessstatus = '1' and a.fiscurrentversion = '1' "
                + " and b.fiscurrentversion = '1' and c.fiscurrentversion = '1' and d.fiscurrentversion = '1'"
                + "and per.fiscurrentversion = '1' and per.fbusinessstatus = '1' and per.fdatastatus = '1'" +
//                "and ch.fnumber != '" + partTime + "' and er.fiscurrentversion = '1' and er.fdatastatus = '1'";
                "and er.fiscurrentversion = '1' and er.fdatastatus = '1'";
        sql = sql + educationSql + sqlFilter;
        if (subordinatePersonnelFilterList.size() > 0) {
            subordinatePersonnelFilterList = subordinatePersonnelFilterList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(u -> u.get("position") + ";" + u.get("person")))), ArrayList::new));
            String filterSql = "";
            for (Map<String, Object> filterMap : subordinatePersonnelFilterList) {
                filterSql = filterSql + "(a.fpositionid = " + filterMap.get("position") + " and a.fpersonid =" + filterMap.get("person") + ") or";
            }
            String substring = filterSql.substring(0, filterSql.lastIndexOf("or"));
            sql = sql + " and(" + substring + ")";


            // 根据查询到的人员id集合查询人员非时序性属性hrpi_pernontsprop,右连接任职经历表查询岗位
            if (!ObjectUtils.isEmpty(organizationFilter)) {
                //过滤任职经历的部门
                List<Long> allPersonByOrg = getAllPersonByOrg((Long) organizationFilter.getPkValue());
                String idStr = allPersonByOrg.stream().map(String::valueOf).collect(Collectors.joining(","));
                sql = sql + " and a.fadminorgid in (" + idStr + ") ";
            }
            if (!ObjectUtils.isEmpty(educationFilter)) {
                sql = sql + " and e.feducationid = " + educationFilter.getPkValue();
            }
            if (!ObjectUtils.isEmpty(postypeFilter)) {
                //过滤任职类型
                List<Long> postypes = postypeFilter.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
                String idStr = postypes.stream().map(String::valueOf).collect(Collectors.joining(","));
                sql = sql + " and a.fpostypeid in (" + idStr + ") ";
            }

            if (!StringUtils.isEmpty(personName)) {
                //过滤姓名
                sql = sql + " and c.fname like '%" + personName + "%'";
            }
            //姓名搜索框
            Object searchName = reportQueryParam.getCustomParam().get("searchName");
            if (org.apache.commons.lang3.StringUtils.isNotBlank((CharSequence) searchName)) {
                sql = sql + " and c.fname like '%" + searchName + "%'";
            }
            if (ageFilterStart != 0) {
                //过滤年龄
                sql = sql + " and d.fage >= " + ageFilterStart;
            }
            if (ageFilterEnd != 0) {
                //过滤年龄
                sql = sql + " and d.fage <= " + ageFilterEnd;
            }
            if (seniorityFilterStart.compareTo(new BigDecimal(0)) > 0) {
                //过滤司龄
                sql = sql + " and per.fcomsercount >= " + seniorityFilterStart;
            }
            if (seniorityFilterEnd.compareTo(new BigDecimal(0)) > 0) {
                //过滤司龄
                sql = sql + " and per.fcomsercount <= " + seniorityFilterEnd;
            }
            if (societyAgeFilterStart.compareTo(new BigDecimal(0)) > 0) {
                //过滤工龄
                sql = sql + " and per.fsocialworkage >= " + societyAgeFilterStart;
            }
            if (societyAgeFilterEnd.compareTo(new BigDecimal(0)) > 0) {
                //过滤工龄
                sql = sql + " and per.fsocialworkage <= " + societyAgeFilterEnd;
            }
            logger.info("下属列表sql" + sql);
            DataSet CareerExperiences = DB.queryDataSet("getCareerExperience", DBRoute.of("hr"), sql, null).orderBy(new String[]{"tdkw_index asc"});
            setResultData(CareerExperiences, coll);
        }
        return resultDataSet;
    }


    /**
     * 构建返回结果集
     *
     * @param dataSet
     * @param coll
     */
    private void setResultData(DataSet dataSet, Collection<Object[]> coll) {
        for (Row row : dataSet) {
            Object[] resultData = new Object[6];
            //姓名
            resultData[0] = row.getString("person");
            //岗位
            resultData[1] = row.getString("position");
            //年龄
            resultData[2] = row.getBigDecimal("age");
            //司龄
            resultData[3] = row.getBigDecimal("entservicelen");
            //工龄
            resultData[4] = row.getBigDecimal("servicelen");
            //学历
            resultData[5] = row.getBigDecimal("education");
            coll.add(resultData);
        }
    }


    public static List<Long> getAllPersonByOrg(Long orgId) {

        //组织集合（获取下级组织）
        List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds(orgId.toString());

        //获取权限信息
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_pc_myteam_pc");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //全组织查看权限
        if (!hasAllOrgPerm) {
            //权限组织
            List<Long> hasPerOrg = result.getHasPermOrgs();
            logger.info("组织范围:" + hasPerOrg.toString());
            //获取普通组织与权限组织的交集
            allBelowHROrg.retainAll(hasPerOrg);
        }

        return allBelowHROrg;
    }
}
