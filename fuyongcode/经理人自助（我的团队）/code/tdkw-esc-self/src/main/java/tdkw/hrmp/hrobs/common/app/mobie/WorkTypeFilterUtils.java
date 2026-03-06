package tdkw.hrmp.hrobs.common.app.mobie;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorkTypeFilterUtils
 *
 * @author xxx
 * @date 2023/8/16
 */
public class WorkTypeFilterUtils {
    private static final Log logger = LogFactory.getLog(WorkTypeFilterUtils.class);

    public static List<Long> filterWorkType(List<Long> personIds, String highLevelType) {
        List<Long> filterPerson;
        if (StringUtils.isBlank(highLevelType)) {
            return personIds;
        }
        DataSet finishPerson = null;
        DataSet finishAsPerson = null;
        boolean flag = true;
        QFilter personFilter = new QFilter("person", QCP.in, personIds);
        final String algoKey = "WorkTypeFilterUtils";
        // 查询参加党派记录 是否当前版本=1，数据版本状态=1 参加党派记录-终止日期为空 或者 当前日期早于/等于终止日期
        QFilter versionFilter = new QFilter("iscurrentversion", QCP.equals, "1").and("datastatus", QCP.equals, "1").and(new QFilter("tdkw_enddate", QCP.equals, null).or("tdkw_enddate", QCP.large_equals, new Date()));
        DataSet partyDataSet = ORM.create().queryDataSet(algoKey, "tdkw_hrpi_attendrecords", "person,tdkw_partyorg,tdkw_partyaffairs", new QFilter[]{personFilter, versionFilter});
        // 查询其他任职信息 是否当前版本=1其  数据版本状态=1  结束时间为空 或者 当前日期早于/等于结束时间
        DataSet otherEmployInfoDataSet = ORM.create().queryDataSet(algoKey, "tdkw_hrpi_otheremployinf", "person,tdkw_post.number", new QFilter[]{personFilter, versionFilter});
        // 查询任职信息维护 任职信息维护中的单据状态=审批通过 任职结束时间为空 或者 当前日期早于/等于任职结束时间
        versionFilter = new QFilter("billstatus", QCP.equals, "C").and(new QFilter("tdkw_dgtjendtime", QCP.equals, null).or("tdkw_dgtjendtime", QCP.large_equals, new Date()));
        // 工团纪的人员标识单独处理
        personFilter = new QFilter("tdkw_dgtjperson", QCP.in, personIds);
        DataSet gtjExperienceDataSet = ORM.create().queryDataSet(algoKey, "tdkw_dgtj_experience", "tdkw_dgtjperson,tdkw_dgtjorg.tdkw_basedatafield.name", new QFilter[]{personFilter, versionFilter});
        // 对人员的任职类型进行过滤 党委会 dwh 董监事 djs 纪检委 jjw 经营班子 jybz 财务负责人 cwfzr 工会 gh  团委 tw
        switch (highLevelType) {
            case "dwh":
                finishPerson = partyDataSet.where("tdkw_partyorg is not null");
                break;
            case "gh":
                // 连接任职信息维护-工会
                finishAsPerson = gtjExperienceDataSet.where("tdkw_dgtjorg.tdkw_basedatafield.name = '工'");
                flag = false;
                break;
            case "tw":
                // 连接任职信息维护-团委
                finishAsPerson = gtjExperienceDataSet.where("tdkw_dgtjorg.tdkw_basedatafield.name = '团'");
                flag = false;
                break;
            case "jjw":
                // 连接任职信息维护-纪检委
                finishAsPerson = gtjExperienceDataSet.where("tdkw_dgtjorg.tdkw_basedatafield.name = '纪'");
                flag = false;
                break;
            case "jybz":
                // 连接其他任职信息 经营班子
                finishPerson = otherEmployInfoDataSet.where("tdkw_post.number in ('GGZW03','GGZW04','GGZW05')");
                break;
            case "cwfzr":
                // 连接其他任职信息 财务负责人
                finishPerson = otherEmployInfoDataSet.where("tdkw_post.number in ('GGZW10')");
                break;
            case "djs":
                // 连接其他任职信息 董监事
                finishPerson = otherEmployInfoDataSet.where("tdkw_post.number in ('GGZW01','GGZW02','GGZW06','GGZW07','GGZW08','GGZW09','GGZW11')");
                break;
            default:
                break;
        }
        if (flag) {
            // 人员的标识为 person
            DynamicObjectCollection persons = ORM.create().toPlainDynamicObjectCollection(finishPerson);
            filterPerson = persons.stream().map(i -> i.getLong("person")).distinct().collect(Collectors.toList());
        } else {
            // 人员的标识为 tdkw_dgtjperson
            DynamicObjectCollection persons = ORM.create().toPlainDynamicObjectCollection(finishAsPerson);
            filterPerson = persons.stream().map(i -> i.getLong("tdkw_dgtjperson")).distinct().collect(Collectors.toList());
        }
        if (StringUtils.equals("tw", highLevelType)) {
            logger.info("统计分析团委人员id：{}", filterPerson);
        }
        return filterPerson;
    }
}