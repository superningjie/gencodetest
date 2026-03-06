package tdkw.hrmp.hrobs.formplugin.report;

import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.Row;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PersonInfoUtil;
import tdkw.hrmp.hrobs.common.hrobs.util.PinyinUtil;

import java.util.ArrayList;
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
public class PersonInfoRpt extends AbstractReportListDataPlugin {
    private Log logger = LogFactory.getLog(PersonInfoRpt.class);

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        Map<String, Object> customParam = reportQueryParam.getCustomParam();
        String personIdsA = (String) customParam.get("personIds_a");
        String personIdsDepart = (String) customParam.get("personIds_depart");
        if (personIdsA != null) {
            if (StringUtils.equals("[]", personIdsA)) {
                return null;
            }
            logger.info("缓存中读取数据personIdsA" + personIdsA);
            String[] ids = personIdsA.substring(1, personIdsA.length() - 1).replaceAll(" ", "").split(",");
            // 人员id
            List<Long> collect = Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
            ArrayList<Long> personFlowIds = new ArrayList<>();
            logger.info("去重前人员id：" + collect.size());
            logger.info("去重前人员id：" + collect);
            //去除重复人员id
            for (Long personId : collect) {
                if (personFlowIds.contains(personId)) {
                    continue;
                }
                personFlowIds.add(personId);
            }
            logger.info("人员id：" + personFlowIds.size());
            logger.info("人员id：" + personFlowIds);
            String algoKey = this.getClass().getName();
            DataSet dataSet = PersonInfoUtil.queryPersonInfo(personFlowIds, algoKey);
            //报表数据按组织排序码 人员排序码排
            dataSet = dataSet.orderBy(new String[]{"empposrel.adminorg.sortcode"});

//          dataSet = dataSet.orderBy(new String[]{"empposrel.adminorg.sortcode", "tdkw_index"});

            Object searchName = reportQueryParam.getCustomParam().get("searchName");
//            if (searchName != null) {
//                if (StringUtils.isNotBlank((CharSequence) searchName)) {
//                    dataSet = dataSet.filter(new FilterFunction() {
//                        @Override
//                        public boolean test(Row row) {
//                            String name = row.getString("tdkw_name");
//                            return name.contains((CharSequence) searchName);
//                        }
//                    });
//                }
//            }
            // 判断搜索的searchName中是否包含字母、正则
            if (searchName != null && StringUtils.isNotBlank((CharSequence) searchName)) {
                String nameStr = searchName.toString();
                if (PinyinUtil.containsLetters(nameStr)) {
                    // 包含字母 pinyin
                    String pinYinName;
                    try {
                        pinYinName = PinyinUtil.getPingYin(nameStr);
                    } catch (Exception e) {
                        // 按字符分割数组对单个文字进行转拼音
                        // 拼接汇总拼音
                        StringBuilder pinYinNameBuilder = new StringBuilder();
                        for (int i = 0; i < nameStr.length(); i++) {
                            String pingYin = PinyinUtil.getPingYin(String.valueOf(nameStr.charAt(i)));
                            pinYinNameBuilder.append(pingYin);
                        }
                        pinYinName = pinYinNameBuilder.toString();
                    }
                    dataSet = dataSet.where(" pinyin like '%" + pinYinName + "%'");
                } else {
                    // 不包含字母
                    dataSet = dataSet.filter(new FilterFunction() {
                        private static final long serialVersionUID = 4017820239971693392L;

                        @Override
                        public boolean test(Row row) {
                            String name = row.getString("tdkw_name");
                            return name.contains((CharSequence) searchName);
                        }
                    });
                }
            }
           /* dataSet = dataSet.executeSql("SELECT graduateschool.name,person, tdkw_company, empposrel.adminorg.sortcode, politicalstatus.name, tdkw_person, tdkw_name, tdkw_position, tdkw_postlevel, tdkw_employtype, tdkw_gender, tdkw_age, tdkw_index, tdkw_nativeplace, tdkw_politicalstatus, tdkw_marriagestatus, tdkw_education, tdkw_graduateschool, tdkw_major, tdkw_joincomdate, tdkw_comsercount, tdkw_socialworkage, tdkw_progressbar, tdkw_dept,schoolrecord " +
                    ",CASE " +
                    "WHEN graduateschool.name = '其他院校' " +
                    "THEN schoolrecord ELSE graduateschool.name " +
                    "END AS tdkw_schoolrecord  ");*/
            dataSet = dataSet.executeSql("SELECT graduateschool.name,person, tdkw_company, empposrel.adminorg.sortcode, politicalstatus.name, tdkw_person, tdkw_name, tdkw_position, tdkw_gender, tdkw_age " +
                    ",CASE " +
                    "WHEN graduateschool.name = '其他院校' " +
                    "THEN schoolrecord ELSE graduateschool.name " +
                    "END AS tdkw_schoolrecord  ");

            return dataSet;
        } else {
            if (StringUtils.equals("[]", personIdsDepart)) {
                return null;
            }
            String[] ids = personIdsDepart.substring(1, personIdsDepart.length() - 1).replaceAll(" ", "").split(",");
            // 人员id
            List<Long> collect = Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
            ArrayList<Long> personFlowIds = new ArrayList<>();
            //去除重复人员id
            for (Long personId : collect) {
                if (personFlowIds.contains(personId)) {
                    continue;
                }
                personFlowIds.add(personId);
            }

            String algoKey = this.getClass().getName();
            DataSet dataSetDep = PersonInfoUtil.queryPersonInfoDep(personFlowIds, algoKey);
            //报表数据1.按照岗位层级排序；
            //2.岗位层级一致的情况下，按照所属组织排序号排序；
            //3.1和2都一致的情况下，按照离职日期排序；
            //4.1-3都一致的情况下，按照工号排序
            // TODO 屏蔽二开字段 "tdkw_postlevel"
//            dataSetDep = dataSetDep.orderBy(new String[]{"tdkw_postlevel", "empposrel.adminorg.sortcode", "tdkw_flowtime desc", "person.number"});
            dataSetDep = dataSetDep.orderBy(new String[]{ "empposrel.adminorg.sortcode", "tdkw_flowtime desc", "person.number"});
            return dataSetDep;
        }
    }
}
