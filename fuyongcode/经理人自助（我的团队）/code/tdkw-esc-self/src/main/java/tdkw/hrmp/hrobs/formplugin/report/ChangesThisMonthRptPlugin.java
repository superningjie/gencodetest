package tdkw.hrmp.hrobs.formplugin.report;


import com.google.common.collect.Maps;
import kd.bos.algo.DataSet;
import kd.bos.algo.FilterFunction;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.business.openservicehelper.hrpi.HRPIPersonServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PinyinUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date： 2023/8/7
 * @description : 本月异动报表列表插件
 */
public class ChangesThisMonthRptPlugin extends AbstractReportListDataPlugin {
    private static final Log LOGGER = LogFactory.getLog(ChangesThisMonthRptPlugin.class);
    private static final List<String> LIST = Arrays.asList(
            "XY00003", "XY00004", "XY00005", "XY00006");
    private static final List<String> SINGLE_LIST = Arrays.asList("XY00008",
            "XY00009", "XY00010", "XY00011", "XY00012", "XY00013");
    private static final List<String> PERSON_LIST = Arrays.asList("XY00001", "XY00005", "XY00007", "XY00008", "XY00009");

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        DataSet dataSet = getDataSet(reportQueryParam);
        return dataSet;
    }

    public String changeSize() {
        DataSet dataSet = getDataSet(null);
        int size = ORM.create().toPlainDynamicObjectCollection(dataSet.copy()).size();
        LOGGER.info(" 本月异动人数为:" + size + "人");
        return String.valueOf(size);
    }

    private DataSet getDataSet(ReportQueryParam reportQueryParam) {
        HashMap<String, QFilter> topFilterMap;
        if (reportQueryParam == null) {
            topFilterMap = Maps.newHashMapWithExpectedSize(16);
            topFilterMap.put("flowLaborFilter", new QFilter("laborreltype.number", QCP.in, PERSON_LIST));
            topFilterMap.put("officeLaborFilter", new QFilter("tdkw_employtype.number", QCP.in, PERSON_LIST));
            topFilterMap.put("beginFlowtimeFilter", new QFilter("flowtime", ">=", getData()));
            topFilterMap.put("beginStartdateFilter", new QFilter("startdate", ">=", getData()));
            topFilterMap.put("endFlowtimeFilter", new QFilter("flowtime", "<", new Date()));
            topFilterMap.put("endStartdateFilter", new QFilter("startdate", "<", new Date()));
            topFilterMap.put("pernontsprQfilter", new QFilter("iscurrentversion", QCP.equals, "1").and("datastatus", QCP.equals, "1"));
        } else {
            topFilterMap = getTopFilter(reportQueryParam);
        }
        long startdata = System.currentTimeMillis();
        List<Long> orgIds = getOrgIds();
        QFilter abnormalQFilter = new QFilter("1",QCP.equals,1);
        QFilter tempPosQFilter = new QFilter("1",QCP.equals,1);
        if(ObjectUtils.isNotEmpty(orgIds)){
            abnormalQFilter.and("adminorghis", QCP.in, orgIds);
            tempPosQFilter.and("adminorg", QCP.in,orgIds);
        }
        LOGGER.info("getPersonId---" + (System.currentTimeMillis() - startdata));
        QFilter companyFilter = topFilterMap.get("companyFilter");
        QFilter postleveFilter = topFilterMap.get("postleveFilter");

        if (companyFilter == null && postleveFilter == null) {
            DataSet abnormalDataSet = abnormalDataSet(topFilterMap, abnormalQFilter);
            DataSet tempPosDataSet = tempPosDataSet(topFilterMap, tempPosQFilter);
            DataSet dataSet = abnormalDataSet.union(tempPosDataSet).orderBy(new String[]{"postlevel", "orderthree", "changedate desc", "number"});
            return dataSet;
        }
        DataSet dataSet = abnormalDataSet(topFilterMap, new QFilter("adminorghis", QCP.in, orgIds)).orderBy(new String[]{"postlevel", "orderthree", "changedate desc", "number"});
        return dataSet;
    }

    private DataSet abnormalDataSet(HashMap<String, QFilter> topFilterMap, QFilter powerFilter) {
        QFilter flowQFilter = new QFilter("1", "=", 1)
                .and(topFilterMap.get("companyFilter"))
                .and(topFilterMap.get("flowLaborFilter"))
                .and(topFilterMap.get("postleveFilter"))
                .and(topFilterMap.get("workTypeFilter"))
                .and(topFilterMap.get("beginFlowtimeFilter"))
                .and(topFilterMap.get("endFlowtimeFilter"))
                .and(powerFilter);
        DynamicObject[] query = HRBaseServiceHelper.create("hpfs_personflow").query("person", new QFilter[]{flowQFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        LOGGER.info("流入流出表的数据数量" + personIds.size());
        LOGGER.info("流入流出表的数据" + personIds);
        LOGGER.info("流入流出表的查询条件" + flowQFilter);
        powerFilter = new QFilter("person", QCP.in, personIds);


        QFilter erManFile = new QFilter("1", QCP.equals, 1)
                .and(powerFilter)
                .and("businessstatus", QCP.equals, "1");
        erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFile.and("empposrel.businessstatus", QCP.equals, "1");
        erManFile.and("empposrel.datastatus", QCP.equals, "1");
        erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and(topFilterMap.get("personName"));
        DataSet ermanFileDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hspm_ermanfile", "id,person,tdkw_index,number", new QFilter[]{erManFile});

        QFilter inflowFilter = new QFilter("1", "=", 1)
                .and(powerFilter)
                .and(topFilterMap.get("companyFilter"))
                .and(topFilterMap.get("flowLaborFilter"))
                .and(topFilterMap.get("postleveFilter"))
                .and(topFilterMap.get("workTypeFilter"))
                .and(topFilterMap.get("beginFlowtimeFilter"))
                .and(topFilterMap.get("endFlowtimeFilter"))
                .and(topFilterMap.get("personName"));

        QFilter outflowFilter = new QFilter("1", "=", 1)
                .and(powerFilter)
                .and(topFilterMap.get("beginFlowtimeFilter"))
                .and(topFilterMap.get("endFlowtimeFilter"));


        QFilter employeeFilter = new QFilter("1", "=", 1)
                .and(powerFilter)
                .and("iscurrentversion", QCP.equals, Boolean.TRUE)
                .and("datastatus", QCP.equals, "1");

        QFilter pernontsprQfilter = new QFilter("1", "=", 1)
                .and(powerFilter)
                .and(topFilterMap.get("pernontsprQfilter"))
                .and("iscurrentversion", QCP.equals, "1")
                .and("datastatus", QCP.equals, "1");

        // 人员非时序性属性
        DataSet hrpiPernontspropDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hrpi_pernontsprop", "person,tdkw_full_pinyin_name as pinyin", new QFilter[]{pernontsprQfilter});
        DynamicObjectCollection plainDynamicObjectCollection = ORM.create().toPlainDynamicObjectCollection(hrpiPernontspropDataSet.copy());
        List<Long> personId = plainDynamicObjectCollection.stream().map(id -> id.getLong("person")).collect(Collectors.toList());
        //任职经历总
        DataSet employeeDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hrpi_emporgrelall", "person,depemp,depemp.position.boid as oldpositionboid,depemp.position.adminorg as oldadminorg,depemp.position.adminorg.name as oldadminorgname,position as oldposition,tdkw_postlevel as oldpostlevel,company as oldcompany,tdkw_ranks.name as jobgradehr", new QFilter[]{employeeFilter});

        //流入流出
        QFilter moveFilter = new QFilter("tdkw_changetype.number", QCP.in, LIST);

        QFilter flowTypeInFilter = new QFilter("flowtype", QCP.equals, "1").and(moveFilter);
        QFilter flowTypeOutFilter = new QFilter("flowtype", QCP.equals, "2").and(moveFilter);

        //查询报表数据
        DataSet inDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hpfs_personflow", "person,depemp,depemp.position.name as positionname,depemp.position.boid as positionboid,depemp.position.tdkw_positionlevel  as postlevel,depemp.adminorg.company.name as companyname,depemp.adminorg as adminorg,depemp.adminorg.name as adminorgname,tdkw_changetype.name as changetype,tdkw_changereason.name as changereason,bill,depemp.adminorg.sortcode as orderthree",
                new QFilter[]{inflowFilter, flowTypeInFilter});
        DataSet outDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hpfs_personflow", "person,flowtime as changedate,depemp,bill",//depemp.position as oldposition,depemp.position.tdkw_positionlevel  as oldpostlevel,depemp.adminorg.company as oldcompany,depemp.position.tdkw_jobgradehr  as jobgradehr,
                new QFilter[]{outflowFilter, flowTypeOutFilter});

        DataSet data = hrpiPernontspropDataSet.leftJoin(outDataSet).on("person", "person").select("pinyin", "person", "changedate", "depemp", "bill").finish();

        outDataSet = data.leftJoin(employeeDataSet).on("depemp", "depemp").select("pinyin", "oldpositionboid", "oldadminorg", "person", "changedate", "depemp", "bill", "oldposition", "oldpostlevel", "oldcompany", "jobgradehr","oldadminorgname").finish();
        long startTime = System.currentTimeMillis();
        DynamicObjectCollection outDataSetSetCollection = ORM.create().toPlainDynamicObjectCollection(outDataSet.copy());
        Map<String, String> person = new HashMap<>();
        for (DynamicObject dynamicObject : outDataSetSetCollection) {
            if (!"".equals(dynamicObject.getString("bill"))) {
                person.put(dynamicObject.getString("bill"), (dynamicObject.getString("oldpositionboid") + dynamicObject.getString("oldposition") + dynamicObject.getString("oldadminorg")));
            }
        }
        LOGGER.info("遍历任职经历存map耗时:" + (System.currentTimeMillis() - startTime));
        LOGGER.info("Map信息：" + person);
        inDataSet = inDataSet.filter(new FilterFunction() {

            @Override
            public boolean test(Row row) {
                String newName = row.getString("positionboid") + row.getString("positionname") + row.getString("adminorg");
                String bill = row.getString("bill");
                String oldName = person.get(bill);
                LOGGER.info("新岗位信息" + newName + "原岗位信息" + oldName);
                // oldName和newName 四种情况
//                if (Objects.isNull(oldName) && Objects.isNull(newName)) {
//                    return false;
//                }
                return !Objects.equals(oldName, newName);
                //return !oldName.equals(newName);
            }
        });
        LOGGER.info("过滤耗时:" + (System.currentTimeMillis() - startTime));

        DataSet doubleAbnormalDataSet = inDataSet.join(outDataSet).select().on("bill", "bill").select("pinyin", "person", "changedate", "positionname", "postlevel", "companyname", "adminorgname", "oldposition", "oldpostlevel", "oldcompany","oldadminorgname", "jobgradehr", "changetype", "changereason", "orderthree").finish();


        //只有流入获流出的数据
        QFilter outFlowFilter = new QFilter("tdkw_changetype.number", QCP.in, SINGLE_LIST).and(inflowFilter).and("person", QCP.in, personId);

        //查询报表数据
        DataSet singleAbnormalDataSet = ORM.create().queryDataSet(this.getClass().getName(),
                "hpfs_personflow", "'' as pinyin ,person,flowtime as changedate,depemp.position.name as positionname,depemp.position.tdkw_positionlevel  as postlevel,depemp.adminorg.company.name as companyname,depemp.adminorg.name as adminorgname,'' as oldposition ,'' as oldpostlevel ,'' as oldcompany ,'' as oldadminorgname,'' as jobgradehr ,tdkw_changetype.name as changetype,tdkw_changereason.name as changereason,depemp.adminorg.sortcode  as orderthree ",
                outFlowFilter.toArray());
        DataSet dataSet = doubleAbnormalDataSet.union(singleAbnormalDataSet);
        DataSet completeDataSet = dataSet.leftJoin(ermanFileDataSet).select("pinyin", "person", "changedate", "positionname", "postlevel", "companyname", "adminorgname", "oldposition", "oldpostlevel", "oldcompany","oldadminorgname", "jobgradehr", "changetype", "changereason", "tdkw_index", "orderthree", "number").on("person", "person").finish();
        completeDataSet = completeDataSet.addField("case when oldposition != null then oldposition end as tdkw_oldposition1", "tdkw_oldposition1");
        completeDataSet = completeDataSet.addField("case when oldcompany != null then oldcompany end as tdkw_oldcompany1", "tdkw_oldcompany1");
        completeDataSet = completeDataSet.addField("case when jobgradehr != null then jobgradehr end as tdkw_jobgradehr1", "tdkw_jobgradehr1");
        completeDataSet = completeDataSet.addField("case when oldadminorgname != null then oldadminorgname end as tdkw_adminorgname1", "tdkw_adminorgname1");
        return completeDataSet;
    }

    private DataSet tempPosDataSet(HashMap<String, QFilter> topFilterMap, QFilter powerFilter) {
        QFilter officeLaborFilter = topFilterMap.get("officeLaborFilter");
        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1")
                .and("isprimary", QCP.equals, "1")
                .and(powerFilter)
                .and(officeLaborFilter);

        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create("hrpi_empposorgrel");
        DynamicObject[] query = hrBaseServiceHelper.query("person", new QFilter[]{qFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter laborFilter = new QFilter("person.id", QCP.in, personIds);


        QFilter erManFile = new QFilter("1", QCP.equals, 1);
        erManFile.and(laborFilter);
        erManFile.and("businessstatus", QCP.equals, "1");
        erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
        erManFile.and("empposrel.businessstatus", QCP.equals, "1");
        erManFile.and("empposrel.datastatus", QCP.equals, "1");
        erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        erManFile.and(topFilterMap.get("personName"));
        DataSet ermanFileDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hspm_ermanfile", "id,person,tdkw_index,number", new QFilter[]{erManFile});


//        QFilter officeLaborFilter = topFilterMap.get("officeLaborFilter");
//        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1")
//                .and("isprimary", QCP.equals, "1")
//                .and(powerFilter)
//                .and(officeLaborFilter);
//
//        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create("hrpi_empposorgrel");
//        DynamicObject[] query = hrBaseServiceHelper.query("person", new QFilter[]{qFilter});
//        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
//        QFilter laborFilter = new QFilter("person.id", QCP.in, personIds);

        QFilter empOrgFilter = new QFilter("postype", QCP.equals, "挂职")
                //.and(powerFilter)
                .and("iscurrentversion", QCP.equals, Boolean.TRUE)
                .and("datastatus", QCP.equals, "1")
                .and(laborFilter)
                .and(topFilterMap.get("workTypeFilter"))
                .and(topFilterMap.get("beginStartdateFilter"))
                .and(topFilterMap.get("endStartdateFilter"));
        empOrgFilter.and(topFilterMap.get("personName"));
        DataSet tenureDataSet = ORM.create().queryDataSet(this.getClass().getName(), "hrpi_emporgrelout",
                "'' as pinyin,person ,startdate as changedate, position as positionname,'99' as postlevel,company as companyname,adminorg as adminorgname ,'' as oldposition ,'' as oldpostlevel ,'' as oldcompany ,'' as  oldadminorgname,'' as jobgradehr ,'' as changetype,'' as changereason ,'4223372036854775807' as orderthree ",
                new QFilter[]{empOrgFilter}, null);

        DataSet completeDataSet = tenureDataSet.leftJoin(ermanFileDataSet).select("pinyin", "person", "changedate", "positionname", "postlevel", "companyname", "adminorgname", "oldposition", "oldpostlevel", "oldcompany","oldadminorgname", "jobgradehr", "changetype", "changereason", "tdkw_index", "orderthree", "number").on("person", "person").finish();
        completeDataSet = completeDataSet.addField("case when oldposition != null then oldposition end as tdkw_oldposition1", "tdkw_oldposition1");
        completeDataSet = completeDataSet.addField("case when oldcompany != null then oldcompany end as tdkw_oldcompany1", "tdkw_oldcompany1");
        completeDataSet = completeDataSet.addField("case when jobgradehr != null then jobgradehr end as tdkw_jobgradehr1", "tdkw_jobgradehr1");
        completeDataSet = completeDataSet.addField("case when oldadminorgname != null then oldadminorgname end as tdkw_adminorgname1", "tdkw_adminorgname1");
        return completeDataSet;
    }

    private HashMap<String, QFilter> getTopFilter(ReportQueryParam reportQueryParam) {
        HashMap<String, QFilter> map = Maps.newHashMapWithExpectedSize(16);
        map.put("flowLaborFilter", new QFilter("laborreltype.number", QCP.in, PERSON_LIST));
        map.put("officeLaborFilter", new QFilter("tdkw_employtype.number", QCP.in, PERSON_LIST));
        map.put("beginFlowtimeFilter", new QFilter("flowtime", ">=", getData()));
        map.put("beginStartdateFilter", new QFilter("startdate", ">=", getData()));
        map.put("endFlowtimeFilter", new QFilter("flowtime", "<", new Date()));
        map.put("endStartdateFilter", new QFilter("startdate", "<", new Date()));
        map.put("pernontsprQfilter", new QFilter("iscurrentversion", QCP.equals, "1").and("datastatus", QCP.equals, "1"));
        FilterInfo filter = reportQueryParam.getFilter();
        List<FilterItemInfo> filterItems = filter.getFilterItems();
        for (FilterItemInfo filterItem : filterItems) {
            String propName = filterItem.getPropName();
            Object value = filterItem.getValue();
            if (value == null) {
                continue;
            }
            switch (propName) {
                case "tdkw_companyfilter":
                    //所属组织
                    DynamicObjectCollection org = (DynamicObjectCollection) filterItem.getValue();
                    List<Long> orgIds = org.stream().map(dynamicObject -> dynamicObject.getLong("id")).collect(Collectors.toList());
                    QFilter companyFilter = new QFilter("depemp.adminorg.company", QCP.in, orgIds).or("depemp.adminorg", QCP.in, orgIds);
                    map.put("companyFilter", companyFilter);
                    break;
                case "tdkw_postlevefilter":
                    //岗位层级
                    map.put("postleveFilter", new QFilter("depemp.position.tdkw_positionlevel", "=", value));
                    break;
                case "tdkw_laborreltypefilter":
                    //人员类型
                    DynamicObjectCollection dynamicObjectCollection = (DynamicObjectCollection) filterItem.getValue();
                    List<String> labors = dynamicObjectCollection.stream().map(dynamicObject -> dynamicObject.getString("number")).collect(Collectors.toList());
                    map.put("flowLaborFilter", new QFilter("laborreltype.number", QCP.in, labors));
                    map.put("officeLaborFilter", new QFilter("tdkw_employtype.number", QCP.in, labors));
                    break;
                case "tdkw_postypefilter":
                    //任职类型
                    map.put("workTypeFilter", workTypeFilter(value.toString()));
                    break;
                case "startdate":
                    //日期范围
                    map.put("beginFlowtimeFilter", new QFilter("flowtime", ">=", value));
                    map.put("beginStartdateFilter", new QFilter("startdate", ">=", value));
                    break;
                case "enddate":
                    //日期范围
                    LocalDate localDate = ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate newLocalDate = localDate.plusDays(1);
                    Date newDate = Date.from(newLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    map.put("endFlowtimeFilter", new QFilter("flowtime", "<", newDate));
                    map.put("endStartdateFilter", new QFilter("startdate", "<", newDate));
                    break;
                case "tdkw_personname":
                    //姓名
                    String personName = value.toString();
                    if (StringUtils.isNotBlank((String) value)) {
                        String nameStr = value.toString();
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
                            map.put("pernontsprQfilter", new QFilter("tdkw_full_pinyin_name", QCP.like, "%" + pinYinName + "%"));
                        } else {
                            // 不包含字母
                            map.put("personName", new QFilter("person.name", QCP.like, "%" + personName + "%"));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return map;
    }

    private Date getData() {
        //获取本月月初
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        Date date = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return date;
    }


    private QFilter workTypeFilter(String workType) {
        QFilter versionFilter = new QFilter("iscurrentversion", QCP.equals, "1")
                .and("datastatus", QCP.equals, "1")
                .and(new QFilter("tdkw_enddate", QCP.equals, null).or("tdkw_enddate", ">=", new Date()));
        QFilter experienceFilter = new QFilter("billstatus", QCP.equals, "C")
                .and(new QFilter("tdkw_dgtjendtime", QCP.equals, null).or("tdkw_dgtjendtime", ">=", new Date()));
        switch (workType) {
            case "A":
                //参加党派记录
                return personFilter("tdkw_hrpi_attendrecords", versionFilter);
            case "B":
                //任职信息维护-工会
                return personExperienceFilter("tdkw_dgtj_experience", experienceFilter.and("tdkw_dgtjorg.tdkw_basedatafield.name", QCP.equals, "工"));
            case "C":
                //任职信息维护-团委
                return personExperienceFilter("tdkw_dgtj_experience", experienceFilter.and("tdkw_dgtjorg.tdkw_basedatafield.name", QCP.equals, "团"));
            case "D":
                //任职信息维护-纪检委
                return personExperienceFilter("tdkw_dgtj_experience", experienceFilter.and("tdkw_dgtjorg.tdkw_basedatafield.name", QCP.equals, "纪"));
            case "E":
                //其他任职信息 经营班子
                return personFilter("tdkw_hrpi_otheremployinf", versionFilter.and("tdkw_post.number", QCP.in, new String[]{"GGZW03", "GGZW04", "GGZW05"}));
            case "F":
                //连接其他任职信息 财务负责人
                return personFilter("tdkw_hrpi_otheremployinf", versionFilter.and("tdkw_post.number", QCP.in, new String[]{"GGZW10"}));
            case "G":
                //连接其他任职信息 董监事
                return personFilter("tdkw_hrpi_otheremployinf", versionFilter.and("tdkw_post.number", QCP.in, new String[]{"GGZW01", "GGZW02", "GGZW06", "GGZW07", "GGZW08", "GGZW09", "GGZW11"}));
            default:
                break;
        }
        return null;
    }

    private QFilter personExperienceFilter(String entityName, QFilter qFilter) {
        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create(entityName);
        DynamicObject[] query = hrBaseServiceHelper.query("tdkw_dgtjperson", new QFilter[]{qFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("tdkw_dgtjperson.id")).collect(Collectors.toList());
        return new QFilter("person", QCP.in, personIds);
    }

    private QFilter personFilter(String entityName, QFilter qFilter) {
        HRBaseServiceHelper hrBaseServiceHelper = HRBaseServiceHelper.create(entityName);
        DynamicObject[] query = hrBaseServiceHelper.query("person", new QFilter[]{qFilter});
        List<Long> personIds = Arrays.stream(query).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        return new QFilter("person", QCP.in, personIds);
    }

    /**
     * 获取授权的人员id
     *
     * @return 授权的人员id
     */
    private static List<Long> getPersonId() {
        List<Long> personIdList;
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_monthchanges_pc");//如果角色控权包含10000L的话 不对权限进行控制
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            //获取权限范围组织下所有人员id
            List<Map<String, Object>> personIdByOrg = HRPIPersonServiceHelper.getPersonByOrgs(hasPerOrg, new Date());
            personIdList = personIdByOrg.stream().map(o -> (Long) o.get("person")).collect(Collectors.toList());


        } else {
            List<Long> allBelowHROrg = HRRoleAndPersonUtils.getHROrgIds("100000");
            //按组织与日期查询人员
            List<Map<String, Object>> personIdByOrg = HRPIPersonServiceHelper.getPersonByOrgs(allBelowHROrg, new Date());
            personIdList = personIdByOrg.stream().map(o -> (Long) o.get("person")).collect(Collectors.toList());
        }
        LOGGER.info("personIdList---" + personIdList);
        return personIdList;
    }

    private List<Long> getOrgIds() {
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_monthchanges_pc");//如果角色控权包含10000L的话 不对权限进行控制
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPerOrg = result.getHasPermOrgs();
            return hasPerOrg;
        }
        return null;
    }
}
