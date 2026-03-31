package tdkw.hrmp.hrobs.formplugin.induction;

import kd.bos.algo.DataSet;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/6/27 14:34
 * @Description 人员详情报表 报表查询插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personinforpt
 * @Version 1.0
 **/
public class InductionInfoRpt extends AbstractReportListDataPlugin {
    private final Log logger = LogFactory.getLog(InductionInfoRpt.class);

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        Map<String, Object> customParam = reportQueryParam.getCustomParam();
        String personIdsA = (String) customParam.get("personIds_a");
        if (personIdsA != null) {
            if (StringUtils.equals("[]", personIdsA)) {
                return null;
            }
            String[] ids = personIdsA.substring(1, personIdsA.length() - 1).replaceAll(" ", "").split(",");
            // 人员id
            List<Long> personFlowIds = Arrays.stream(ids).map(Long::parseLong).distinct().collect(Collectors.toList());
            logger.info("去重后人员id：" + personFlowIds.size());
            logger.info("去重后人员id：" + personFlowIds);
            String algoKey = this.getClass().getName();
            DataSet dataSet = queryPersonInfo(personFlowIds, algoKey);
            // 报表数据按人事业务档案中"tdkw_index"进行排序
            dataSet = dataSet.orderBy(new String[]{"tdkw_joincomdate desc", "number"});
            return dataSet;
        }
        return null;
    }

    private static DataSet queryPersonInfo(List<Long> personIds, String algoKey) {
        QFilter personFiler = new QFilter("person.id", QCP.in, personIds);
        QFilter erManFile = new QFilter("businessstatus", QCP.equals, "1");
        erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFile.and("empposrel.datastatus", QCP.equals, "1");
        erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);

        // 人事业务档案
        DataSet hspm_ermanfile = ORM.create().queryDataSet(algoKey, "hspm_ermanfile", "person,empposrel.company," +
                "person.name,empposrel.position," +
                "pernontsprop.gender,pernontsprop.age,empposrel.adminorg,empposrel.adminorg.sortcode as sortcode,number", new QFilter[]{personFiler, erManFile});

        QFilter perRegionFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        perRegionFilter.and("datastatus", QCP.equals, "1");
        // 基本信息补充
        DataSet hrpi_perregion = ORM.create().queryDataSet(algoKey, "hrpi_perregion", "person,nativeplace,politicalstatus",
                new QFilter[]{personFiler, perRegionFilter});

        QFilter perTsPropFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员时序性属性
        DataSet hrpi_pertsprop = ORM.create().queryDataSet(algoKey, "hrpi_pertsprop", "person,marriagestatus",
                new QFilter[]{personFiler, perTsPropFilter});

        QFilter perserlenFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 服务年限基础页面
        DataSet hrpi_perserlen = ORM.create().queryDataSet(algoKey, "hrpi_perserlen", "person,joincomdate,comsercount,socialworkage",
                new QFilter[]{personFiler, perserlenFilter});

        QFilter personInfoFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        personInfoFilter.and("datastatus", QCP.equals, "1");
        // 基本信息表单
        DataSet hspm_personinfo = ORM.create().queryDataSet(algoKey, "hspm_personinfo", "person",
                new QFilter[]{personFiler, personInfoFilter});

//        QFilter pereduexpFilter = new QFilter("tdkw_ishighestcheck", QCP.equals, "1");
        QFilter pereduexpFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
//        pereduexpFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        pereduexpFilter.and("datastatus", QCP.equals, "1");
        // 教育经历
        DataSet hrpi_pereduexp = ORM.create().queryDataSet(algoKey, "hrpi_pereduexp", "person,education",
                new QFilter[]{personFiler, pereduexpFilter});

        // 人员非时序性属性
        DataSet hrpi_pernontsprop = ORM.create().queryDataSet(algoKey, "hrpi_pernontsprop", "person",
                new QFilter[]{personFiler, personInfoFilter});
        DataSet finish = hspm_ermanfile.leftJoin(hrpi_perregion).on("person", "person").select("person", "empposrel.company",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "empposrel.adminorg", "number").finish();
        finish = finish.leftJoin(hrpi_pertsprop).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "marriagestatus", "number").finish();
        finish = finish.leftJoin(hrpi_pereduexp).on("person", "person").select("person", "empposrel.company", "empposrel.adminorg",
                "person.name", "empposrel.position",
                "pernontsprop.gender", "pernontsprop.age", "politicalstatus", "marriagestatus", "education","number").finish();
        finish = finish.leftJoin(hrpi_perserlen).on("person", "person").select("person", "empposrel.company as tdkw_company", "empposrel.adminorg as tdkw_dept",
                "person.name as tdkw_name", "empposrel.position as tdkw_position",
                "pernontsprop.gender as tdkw_gender", "pernontsprop.age as tdkw_age",
                "politicalstatus as tdkw_politicalstatus", "marriagestatus as tdkw_marriagestatus", "education as tdkw_education",
                "joincomdate as tdkw_joincomdate", "comsercount as tdkw_comsercount", "socialworkage as tdkw_socialworkage", "number").finish();
        finish = finish.leftJoin(hspm_personinfo).on("person", "person").select("person", "tdkw_company",
                "tdkw_name", "tdkw_position", "tdkw_gender", "tdkw_age",
                "tdkw_politicalstatus", "tdkw_marriagestatus", "tdkw_education",
                "tdkw_joincomdate", "tdkw_comsercount", "tdkw_socialworkage", "tdkw_dept", "number").finish();
        finish = finish.leftJoin(hrpi_pernontsprop).on("person", "person").select("person", "tdkw_company",
                "tdkw_name", "tdkw_position", "tdkw_gender", "tdkw_age",
                 "tdkw_politicalstatus", "tdkw_marriagestatus", "tdkw_education",
                "tdkw_joincomdate", "tdkw_comsercount", "tdkw_socialworkage", "tdkw_dept", "number").finish();
        finish = finish.distinct();

        return finish;
    }
}