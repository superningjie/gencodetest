package tdkw.esc.leaderquery.report.division;

import kd.bos.algo.*;
import kd.bos.algo.input.CollectionInput;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;

/**
 * @author xxx
 * @version 1.0
 * @description: TODO
 * @date 2023/11/13 0013 上午 10:32
 */

public class DivisionListReport extends AbstractReportListDataPlugin {

    private static final Log logger = LogFactory.getLog(DivisionListReport.class);

    private final String algoKey = this.getClass().getName();

    /**
     * 查询方法 返回给前端
     *
     * @param reportQueryParam
     * @param o
     * @return
     * @throws Throwable
     */
    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {

        QFilter qFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("iscurrentversion", QCP.equals, "1")
                .and("enable", QCP.equals, "1")
                .and("status", QCP.equals, "C");

        Map<String, Object> customParam = reportQueryParam.getCustomParam();
        logger.info("点击单元格传的参数:" + customParam.toString());
        HashMap<String, Object> map = (HashMap<String, Object>) customParam.get("data");
        //需要查询的人员数据的组织id
        List list = (List) map.get("list");
        logger.info("查询的事业部组织的id" + list);

        //点击行的列名
        String click = customParam.get("click").toString();
        //queryOrgPeople构造数据需要的名称
        ArrayList<HashMap<String, Object>> data = (ArrayList<HashMap<String, Object>>) map.get("data");
        //查询员工数据
        HashMap hashMap = queryOrgPeople(data, list, click);

        //点击行所有人员的id
        HashMap idsMap = (HashMap) hashMap.get("ids");
        hashMap.remove("ids");
        ArrayList idsList = (ArrayList) idsMap.get("ids");
        //传入数据构建dataset
        DataSet dataSet = createDataSet(hashMap, idsList);
        //查询personid关联的其他数据
        DataSet dataSet1 = queryPerson(idsList);
        DataSet finish = dataSet.leftJoin(dataSet1).on("id", "personid").select(new String[]{"orgname", "name", "post", "postlevel", "sortcode", "index"},
                new String[]{"number", "personid", "gender", "marry", "education", "politics", "birthday", "age", "societyage"}).finish();
        DataSet select = finish.select("number as tdkw_number,orgname as tdkw_org,name as tdkw_name,post as tdkw_post,postlevel as tdkw_postlevel,gender as tdkw_gender," +
                "marry as tdkw_marriage,education as tdkw_education,politics as tdkw_politics,birthday as tdkw_birthday,age as tdkw_age," +
                "societyage as tdkw_societyage,sortcode,index").orderBy(new String[]{"sortcode", "index"});

        return select;
    }

    /**
     * 根据事业部统计报表传过来的参数，查询该部门下属的组织
     *
     * @param orgId 传入参数 组织id
     * @return 返回下属组织id
     */
    private List queryOrgId(Long orgId) {
        //左树点击传过来的组织id及其下级id的数据
        ArrayList<Long> arrayList = new ArrayList<>();
        QFilter qFilter = new QFilter("datastatus", QCP.equals, "1").and("iscurrentversion", QCP.equals, "1").and("enable", QCP.equals, "1");
        //全部数据 排序码按顺序排列
        DynamicObjectCollection allData = QueryServiceHelper.query("haos_adminorghr", "structlongnumber,id,sortcode",
                new QFilter[]{new QFilter("id", QCP.not_equals, 100000L)
                        .and("structlongnumber", QCP.not_equals, "").and(qFilter)});
        if (ObjectUtils.isNotEmpty(orgId)) {
            //传过来的组织id的数据
            DynamicObject treeData = QueryServiceHelper.queryOne("haos_adminorghr", "structlongnumber",
                    new QFilter[]{new QFilter("id", QCP.equals, orgId).and(qFilter)});
            //长编码
            String structlongnumber = treeData.getString("structlongnumber");
            for (DynamicObject dynamicObject : allData) {
                //长编码
                String longNumber = dynamicObject.getString("structlongnumber");
                //长编码匹配下级
                if (structlongnumber.contains(longNumber)) {
                    arrayList.add(dynamicObject.getLong("id"));
                }
            }
        } else {
            for (DynamicObject dynamicObject : allData) {
                arrayList.add(dynamicObject.getLong("id"));
            }
        }
        logger.info("点击行的下级组织" + arrayList);
        return arrayList;
    }

    /**
     * 根据组织id 查询人事业务的档案 构建map
     *
     * @param data  构造数据需要的组织名称
     * @param list  事业部统计报表传入的需要查询人员数据的组织id
     * @param click 事业部统计报表的点击列
     * @return
     */
    private HashMap queryOrgPeople(ArrayList<HashMap<String, Object>> data, List list, String click) {

        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1");
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("filetype.postype.number", QCP.equals, "XY00001");
        qFilter.and("empposrel.datastatus", QCP.equals, "1");
        qFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("empposrel.businessstatus", QCP.equals, "1");
        qFilter.and("empposrel.isprimary", QCP.equals, "1");
//        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1");
//        //qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        //qFilter.and("filetype.postype.number", QCP.equals, "XY00001");
//        qFilter.and("empposrel.datastatus", QCP.equals, "1");
//        qFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
//        qFilter.and("empposrel.businessstatus", QCP.equals, "1");
//        //qFilter.and("empposrel.isprimary", QCP.equals, "1");
//        qFilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");//待验证
        switch (click) {
            case "tdkw_formalperson":
//                qFilter.and("empposrel.tdkw_employtype.number", QCP.in, new String[]{"XY00001", "XY00005"});
                break;
            case "tdkw_dispatchperson":
//                qFilter.and("empposrel.tdkw_employtype.number", QCP.in, new String[]{"XY00007", "XY00008"});
                break;
            case "tdkw_outsourceperson":
//                qFilter.and("empposrel.tdkw_employtype.number", QCP.equals, "XY00009");
                break;
            default:
//                qFilter.and("empposrel.tdkw_employtype.number", QCP.in, new String[]{"XY00001", "XY00005", "XY00007", "XY00008", "XY00009"});
                break;
        }
        //点击小计合计时匹配不到id
        DynamicObjectCollection erManFile = QueryServiceHelper.query("hspm_ermanfile", "empposrel.company.id,empposrel.adminorg.id,empposrel.adminorg.name,empposrel.adminorg.sortcode,person.name,empposrel.position.name,person.id", new QFilter[]{qFilter, new QFilter("empposrel.adminorg.id", QCP.in, list).or("empposrel.company.id", QCP.in, list)});
        logger.info("查询员工的数量为:" + erManFile.size());
        logger.info("查询员工的数据为:" + erManFile.toString());

        HashMap<String, HashMap<String, Object>> allMap = new HashMap<>();
        ArrayList<Long> arrayList = new ArrayList<>();

        for (DynamicObject object : erManFile) {
            Long personId = object.getLong("person.id");
            HashMap<String, Object> singleMap = new HashMap<>();
            arrayList.add(personId);
            Long orgId = object.getLong("empposrel.adminorg.id");
            Long orgId1 = object.getLong("empposrel.company.id");
            //人事业务档案-任职经历-公司/部门
            for (HashMap<String, Object> obj : data) {
                String name = (String) obj.get("orgName");
                List<Long> affiliatedbusIds = (List<Long>) obj.get("affiliatedbusIds");
                if (affiliatedbusIds.contains(orgId) || affiliatedbusIds.contains(orgId1)) {
                    singleMap.put("orgname", name);
                }
            }
            singleMap.put("sortcode", object.getString("empposrel.adminorg.sortcode"));

            //人员id
            singleMap.put("id", personId);
            //人员名称
            singleMap.put("name", object.getString("person.name"));
            //职位
            singleMap.put("post", object.getString("empposrel.position.name"));
//            String postlevel = object.getString("empposrel.position.tdkw_positionlevel");
//            switch (postlevel) {
//                case "1":
//                    //职位等级
//                    singleMap.put("postlevel", "集团高管-集团直管");
//                    break;
//                case "2":
//                    //职位等级
//                    singleMap.put("postlevel", "集团高管-授权行业");
//                    break;
//                case "3":
//                    //职位等级
//                    singleMap.put("postlevel", "其他高管");
//                    break;
//                case "4":
//                    //职位等级
//                    singleMap.put("postlevel", "中层");
//                    break;
//                case "5":
//                    //职位等级
//                    singleMap.put("postlevel", "基层");
//                    break;
//            }
//            singleMap.put("index", object.getString("tdkw_index"));
            allMap.put(personId.toString(), singleMap);
        }
        HashMap<String, Object> ids = new HashMap();
        ids.put("ids", arrayList);
        logger.info("点击行所有人员的id：" + arrayList);
        allMap.put("ids", ids);
        logger.info("点击行人员的数据" + allMap.toString());
        return allMap;
    }

    /**
     * 构建dataset
     *
     * @param map  根据组织id查询出来的数据
     * @param list 人员id集合
     * @return
     */
    private DataSet createDataSet(HashMap map, ArrayList list) {
        // 创建一个空的DataSet
        Collection<Object[]> coll = new ArrayList<Object[]>();
        for (Object obj : list) {
            String s = obj.toString();
            HashMap<String, Object> map1 = (HashMap<String, Object>) map.get(s);
            Object[] objArr = new Object[7];
            objArr[0] = map1.get("id");
            objArr[1] = map1.get("orgname");
            objArr[2] = map1.get("name");
            objArr[3] = map1.get("post");
            objArr[4] = map1.get("postlevel");
            objArr[5] = map1.get("sortcode");
            objArr[6] = map1.get("index");
            coll.add(objArr);
        }
        String[] fields = new String[]{"id", "orgname", "name", "post", "postlevel", "sortcode", "index"};
        DataType[] types = new DataType[]{DataType.LongType, DataType.StringType, DataType.StringType, DataType.StringType, DataType.StringType, DataType.StringType, DataType.StringType};

        RowMeta rowMeta = RowMetaFactory.createRowMeta(fields, types);
        CollectionInput inputs = new CollectionInput(rowMeta, coll);
        DataSet resultDataSet = Algo.create(this.getClass().getName()).createDataSet(inputs);
        logger.info("构建完毕的dataset的数据：" + ORM.create().toPlainDynamicObjectCollection(resultDataSet.copy()).toString());
        return resultDataSet;
    }

    /**
     * 查询人员的其他属性
     *
     * @param idsList 人员id集合
     * @return
     */
    private DataSet queryPerson(ArrayList idsList) {
        QFilter qFilter = new QFilter("datastatus", QCP.equals, "1");
        qFilter.and("iscurrentversion", QCP.equals, "1");
        //人员非时序属性 性别 籍贯 出生日期 年龄
        DataSet pernontsprop = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pernontsprop", "gender.name,birthday,age,person.id", new QFilter[]{new QFilter("person.id", QCP.in, idsList).and(qFilter)}, null);
        logger.info("人员非时序属性" + ORM.create().toPlainDynamicObjectCollection(pernontsprop.copy()));
        //基本信息补充 政治面貌
        DataSet perregion = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perregion", "politicalstatus.name,person.id", new QFilter[]{new QFilter("person.id", QCP.in, idsList).and(qFilter)}, null);
        logger.info("基本信息补充" + ORM.create().toPlainDynamicObjectCollection(perregion.copy()));
        //服务年限 社会工龄 集团司龄 公司司龄
        DataSet perserlen = QueryServiceHelper.queryDataSet(algoKey, "hrpi_perserlen", "socialworkage,person.id", new QFilter[]{new QFilter("person.id", QCP.in, idsList).and(qFilter)}, null);
        logger.info("服务年限" + ORM.create().toPlainDynamicObjectCollection(perserlen.copy()));
        //人员时序属性 婚姻状况
        DataSet pertsprop = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pertsprop", "marriagestatus.name,person.id", new QFilter[]{new QFilter("person.id", QCP.in, idsList).and(qFilter)}, null);
        logger.info("人员时序属性" + ORM.create().toPlainDynamicObjectCollection(pertsprop.copy()));
        //教育经历 学历(最高)
//        DataSet pereduexp = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pereduexp", "education.name,person.id,person.number", new QFilter[]{new QFilter("person.id", QCP.in, idsList).and("tdkw_ishighestcheck", QCP.equals, "1").and(qFilter)}, null);
        DataSet pereduexp = QueryServiceHelper.queryDataSet(algoKey, "hrpi_pereduexp", "education.name,person.id,person.number", new QFilter[]{new QFilter("person.id", QCP.in, idsList).and(qFilter)}, null);

        logger.info("教育经历" + ORM.create().toPlainDynamicObjectCollection(pereduexp.copy()));

        DataSet select = pernontsprop.leftJoin(perregion).on("person.id", "person.id").select(new String[]{"gender.name", "birthday", "age", "person.id"}, new String[]{"politicalstatus.name"}).finish();
        select = select.leftJoin(perserlen).on("person.id", "person.id").select(new String[]{"gender.name","birthday", "age", "person.id", "politicalstatus.name"}, new String[]{"socialworkage"}).finish();
        select = select.leftJoin(pertsprop).on("person.id", "person.id").select(new String[]{"gender.name", "birthday", "age", "person.id", "politicalstatus.name", "socialworkage"}, new String[]{"marriagestatus.name"}).finish();
        select = select.leftJoin(pereduexp).on("person.id", "person.id").select(new String[]{"gender.name", "birthday", "age", "person.id", "politicalstatus.name", "socialworkage", "marriagestatus.name"}, new String[]{"education.name","person.number"}).finish();
        DataSet dataSet = select.select("person.number as number,person.id as personid,gender.name as gender,marriagestatus.name as marry,education.name as education,politicalstatus.name as politics,birthday as birthday,age as age,socialworkage as societyage");

        return dataSet;
    }

}
