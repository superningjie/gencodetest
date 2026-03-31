package tdkw.esc.leaderquery.report.division;

import com.google.common.collect.Lists;
import kd.bos.algo.Algo;
import kd.bos.algo.DataSet;
import kd.bos.algo.DataType;
import kd.bos.algo.RowMeta;
import kd.bos.algo.RowMetaFactory;
import kd.bos.algo.input.CollectionInput;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;
import tdkw.esc.leaderquery.common.hrmp.HRStructOrgUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @version 1.0
 * @description: TODO
 * @date 2023/10/25 0025 上午 9:39
 */

public class DivisionReport extends AbstractReportListDataPlugin {
    private static final Log logger = LogFactory.getLog(DivisionReport.class);

    private final String algoKey = this.getClass().getName();

    /**
     * 重写的报表查询方法
     *
     * @param reportQueryParam
     * @param o
     * @return
     * @throws Throwable
     */
    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        //公共查询条件
        QFilter qFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("iscurrentversion", QCP.equals, "1")
                .and("enable", QCP.equals, "1")
                .and("status", QCP.equals, "C");
        //查询行业的数据
        DynamicObjectCollection industryData = QueryServiceHelper.query("haos_adminorghr", "id,name,structlongnumber,sortcode",
                new QFilter[]{new QFilter("orgtype.number", QCP.equals, "XY00003").and(qFilter)}, null);
        logger.info("行业类型的HR行政组织数据：" + industryData.toString());
        //查询板块的数据
        DynamicObjectCollection plateData = QueryServiceHelper.query("haos_adminorghr", "id,name,structlongnumber,sortcode",
                new QFilter[]{new QFilter("parent.id", QCP.equals, 100000L).and(qFilter)}, null);
        logger.info("板块类型的HR行政组织数据：" + plateData.toString());
        //获取事业部权限
        // TODO 报错 屏蔽
//        AuthorizedOrgResult structAdminOrgs = HRStructOrgUtils.getStructUserAdminOrgs(UserServiceHelper.getCurrentUserId(), "tdkw_divisionroster_pc");
//        List<Long> hasPermOrgs = structAdminOrgs.getHasPermOrgs();
//        logger.info("非全功能用户拥有的组织权限的id" + hasPermOrgs);
//        logger.info("是否全组织用户" + structAdminOrgs.isHasAllOrgPerm());
//        //是否事业部统计维度
//        QFilter treeQfilter = new QFilter("tdkw_divisiondimension", QCP.equals, "1");
//        //是否全功能用户
//        if (!structAdminOrgs.isHasAllOrgPerm()) {
//            treeQfilter = treeQfilter.and("id", QCP.in, hasPermOrgs);
//        }

        // 2025-10-31 增加报表过滤，当前人组织
        //获取当前登录人员所属组织
        List<Long> userIds = new ArrayList<>(1);
        userIds.add(UserServiceHelper.getCurrentUserId());
        // 获取当前登录人员所属公司
        String adminOrg = String.valueOf(UserServiceHelper.getUserMainOrgId(UserServiceHelper.getCurrentUserId()));
        QFilter treeQfilter = new QFilter("id", QCP.in, Long.valueOf(adminOrg));
//        QFilter treeQfilter = new QFilter("id", QCP.in, 100000L);


        //查询事业部组织的id
        DataSet orgDataSet = QueryServiceHelper.queryDataSet(algoKey, "haos_adminorgdetail", "id," +
                "name,number,sortcode,isvirtualorg,structlongnumber", new QFilter[]{qFilter, treeQfilter}, null);
        DynamicObjectCollection query = QueryServiceHelper.query("haos_adminorgdetail", "id", new QFilter[]{qFilter, treeQfilter});
        List<Long> idlist = query.stream().map(i -> i.getLong("id")).collect(Collectors.toList());
        if(ObjectUtils.isEmpty(idlist)){
            idlist.add(0L);
        }
        String sql = "select fstructlongnumber,fadminorgid from T_HAOS_ADMINSTRUCT " +
                " where fiscurrentversion = '1' and fdatastatus = '1' AND fenable = '1' " +
                " and fstatus = 'C' and fstructprojectid = '1783244783607885824'" +
                " and fadminorgid in (" + HRRoleAndPersonUtils.convertToLong(idlist) + ")";
        DataSet dataSet = DB.queryDataSet(algoKey, DBRoute.of("hr"), sql, null);
        orgDataSet = orgDataSet.leftJoin(dataSet).on("id", "fadminorgid").select("id", "name", "number", "sortcode","isvirtualorg", "structlongnumber").finish();
        DynamicObjectCollection orgData = ORM.create().toPlainDynamicObjectCollection(orgDataSet.copy());
        logger.info("公司和事业部类型的HR行政组织数据：" + orgData.toString());
        //根据权限组织构建map存放事业部，板块，二级板块
        HashMap<String, HashMap<String, String>> map = creatMap(orgData, plateData, industryData);
        logger.info("HR行政组织查询出的数据" + map.toString());

        HashMap<Long, List<Long>> objectObjectHashMap = new HashMap<>();
        ArrayList<Long> idList = new ArrayList<>();
        ArrayList<String> numberList = new ArrayList<>();
        // TODO 报错屏蔽
        //取到事业部权限组织 查询事业部的所有下级
        for (DynamicObject orgDatum : orgData) {
            String number = orgDatum.getString("number");
            Long id = orgDatum.getLong("id");
            numberList.add(number);
            // 这里的level参数项目原来是写死的还是动态获取的
            List<Long> structOrgIds = HRStructOrgUtils.getStructOrgIds(id.toString(), 9999);
            idList.addAll(structOrgIds);
            objectObjectHashMap.put(id, structOrgIds);
        }
        logger.info("权限组织id及下级组织id:" + objectObjectHashMap);

        DataSet createDataSet = createDataSet(map, numberList);
//        return createDataSet;
        //联合构造的DataSet和部门人数数据
        DataSet union = unionData(createDataSet, objectObjectHashMap, idList);
        return union;
    }

    /**
     * 将左树传过来的id查询他的下级组织id
     *
     * @param orgIds 左树传过来的id
     * @return 下级组织id的list
     */
    private ArrayList<Long> createLeftTreeId(List orgIds) {
        //左树点击传过来的组织id及其下级id的数据
        ArrayList<Long> arrayList = new ArrayList<>();
        //左树点击数据的长编码
        ArrayList<String> list = new ArrayList<>();
        //传过来的组织id的数据
        DynamicObjectCollection treeData = QueryServiceHelper.query("haos_adminorghr", "structlongnumber",
                new QFilter[]{new QFilter("id", QCP.in, orgIds)
                        .and("datastatus", QCP.equals, "1")
                        .and("iscurrentversion", QCP.equals, "1")
                        .and("enable", QCP.equals, "1")});
        //全部数据
        DynamicObjectCollection allData = QueryServiceHelper.query("haos_adminorghr", "structlongnumber,id",
                new QFilter[]{new QFilter("id", QCP.not_equals, 100000L)
                        .and("structlongnumber", QCP.not_equals, "")
                        .and("datastatus", QCP.equals, "1")
                        .and("iscurrentversion", QCP.equals, "1")
                        .and("enable", QCP.equals, "1")});
        for (DynamicObject object : treeData) {
            String structlongnumber = Optional.ofNullable(object.getString("structlongnumber")).orElse(null);
            list.add(structlongnumber);
        }
        for (String str : list) {
            for (DynamicObject dynamicObject : allData) {
                //长编码
                String longNumber = Optional.ofNullable(dynamicObject.getString("structlongnumber")).orElse(null);
                if (longNumber.contains(str)) {
                    arrayList.add(dynamicObject.getLong("id"));
                }
            }
        }
        logger.info("左树的下级组织" + arrayList);
        return arrayList;
    }

    /**
     * 查询出来的数据按照事业部/中心的number构造map对象，后续取出数据创建DatasSet
     *
     * @param orgData      事业部/中心的DynamicObjectCollection
     * @param plateData    板块的DynamicObjectCollection
     * @param industryData 行业的DynamicObjectCollection
     * @return
     */
    private HashMap<String, HashMap<String, String>> creatMap(DynamicObjectCollection orgData, DynamicObjectCollection plateData, DynamicObjectCollection industryData) {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();
        for (DynamicObject object : orgData) {
            HashMap<String, String> map2 = new HashMap<>();
            //HR行政组织名称
            map2.put("name", object.getString("name"));
            //HR行政组织id
            map2.put("id", object.getString("id"));
            //HR行政组织number
            String number = object.getString("number");
            map2.put("number", number);
            map2.put("orgSortCode", object.getString("sortcode"));
            //是否虚拟组织
            boolean isvirtualorg = object.getBoolean("isvirtualorg");
            //部门长编码
            String lnum = object.getString("structlongnumber");
            if (StringUtils.isNotEmpty(lnum)) {

                for (DynamicObject obj : plateData) {
                    //板块长编码
                    String longNumber = obj.getString("structlongnumber");
                    //板块名称
                    String plate = obj.getString("name");
                    //板块id
                    String plateId = obj.getString("id");
                    String plateSortCode = obj.getString("sortcode");
                    //部门的长编码包含板块长编码则是一个板块下
                    // TODO ???? 怎么个事儿 倒反天罡？
//                    if (lnum.contains(longNumber)) {
                    if (longNumber.contains(lnum)) {
                        map2.put("plate", plate);
                        map2.put("plateid", plateId);
                        //map2.put("industry", plate);
                        //map2.put("industryid", plateId);
                        map2.put("plateSortCode", plateSortCode);
                        //map2.put("industrySortCode", plateSortCode);
                    }
                }
                if(!isvirtualorg) {
                    for (DynamicObject obj : industryData) {
                        String longNumber = obj.getString("structlongnumber");
                        String industryName = obj.getString("name");
                        String industryId = obj.getString("id");
                        String industrySortCode = obj.getString("sortcode");
                        if (lnum.contains(longNumber)) {
                            map2.put("industry", industryName);
                            map2.put("industryid", industryId);
                            map2.put("industrySortCode", industrySortCode);
                        }
                    }
                }
            }
            map.put(number, map2);

        }
        logger.info("构建的查询数据：" + map.toString());
        return map;
    }

    /**
     * 将map转为dataset
     *
     * @param map  需要转换的数据
     * @param list 取map的key
     * @return
     */
    private DataSet createDataSet(HashMap map, ArrayList list) {
        // 创建一个空的DataSet
        Collection<Object[]> coll = new ArrayList<Object[]>();
        for (Object obj : list) {
            String s = obj.toString();
            HashMap<String, String> map1 = (HashMap<String, String>) map.get(s);
            Object[] objArr = new Object[10];
            if (ObjectUtils.isNotEmpty(map1)) {
                objArr[0] = map1.get("id");
                objArr[1] = map1.get("name");
                objArr[2] = map1.get("plate");
                objArr[3] = map1.get("industry");
                objArr[4] = map1.get("number");
                objArr[5] = map1.get("plateid");
                objArr[6] = map1.get("industryid");
                objArr[7] = map1.get("plateSortCode");
                objArr[8] = map1.get("industrySortCode");
                objArr[9] = map1.get("orgSortCode");
                coll.add(objArr);
            }
        }
        String[] fields = new String[]{"tdkw_orgid", "tdkw_org", "tdkw_plate", "tdkw_industry", "number", "tdkw_plateid", "tdkw_industryid", "plateSortCode", "industrySortCode", "orgSortCode"};
        DataType[] types = new DataType[]{DataType.LongType, DataType.StringType, DataType.StringType, DataType.StringType, DataType.StringType, DataType.LongType, DataType.LongType, DataType.StringType, DataType.StringType, DataType.StringType};

        RowMeta rowMeta = RowMetaFactory.createRowMeta(fields, types);
        CollectionInput inputs = new CollectionInput(rowMeta, coll);
        DataSet resultDataSet = Algo.create(this.getClass().getName()).createDataSet(inputs);
        logger.info("构建完毕的dataset的数据：" + ORM.create().toPlainDynamicObjectCollection(resultDataSet.copy()).toString());
        return resultDataSet;
    }

    /**
     * 将公司部门信息的dataset和员工数量的dataset进行union合并
     *
     * @param queryDate 有权限的组织的数据
     * @param orgIds    权限组织的所有下级id
     * @param map       权限组织的id 对应的下级id
     * @return
     */
    private DataSet unionData(DataSet queryDate, HashMap<Long, List<Long>> map, List<Long> orgIds) {
        //hr行政组织id = 员工所属部门id
        //查询正式员工/退休返聘
        DataSet formal = formal(orgIds, map);
        //查询劳务派遣/劳务人员
        DataSet dispatch = dispatch(orgIds, map);
        //查询劳务外包人员
        DataSet outsource = outsource(orgIds, map);

        //查询的人员数据和构建的部门dataset根据部门id进行左连接
        queryDate = queryDate.leftJoin(formal).on("tdkw_orgid", "cid").select(new String[]{"tdkw_orgid", "tdkw_org",
                "tdkw_plate", "tdkw_industry", "number", "tdkw_plateid", "tdkw_industryid", "plateSortCode", "industrySortCode", "orgSortCode"},
                new String[]{"number as tdkw_formalperson"}).finish();
        queryDate = queryDate.leftJoin(dispatch).on("tdkw_orgid", "cid").select(new String[]{"tdkw_orgid",
                "tdkw_org", "tdkw_plate", "tdkw_industry", "number", "tdkw_plateid", "tdkw_industryid", "plateSortCode",
                "industrySortCode", "orgSortCode", "tdkw_formalperson"}, new String[]{"number as tdkw_dispatchperson"}).finish();
        queryDate = queryDate.leftJoin(outsource).on("tdkw_orgid", "cid").select(new String[]{"tdkw_orgid", "tdkw_org",
                "tdkw_plate", "tdkw_industry", "number", "tdkw_plateid", "tdkw_industryid", "plateSortCode", "industrySortCode",
                "orgSortCode", "tdkw_formalperson", "tdkw_dispatchperson"}, new String[]{"number as tdkw_outsourceperson"}).finish();

        DataSet select = queryDate.select("tdkw_plate,tdkw_industry,tdkw_org,tdkw_formalperson + tdkw_dispatchperson + " +
                "tdkw_outsourceperson as tdkw_sum,tdkw_formalperson,tdkw_dispatchperson,tdkw_outsourceperson,tdkw_orgid," +
                "tdkw_industryid,tdkw_plateid,plateSortCode,industrySortCode,orgSortCode")
                .orderBy(new String[]{"plateSortCode", "industrySortCode", "orgSortCode"});

        queryDate.close();

        DataSet select1 = select.copy();
        DataSet select2 = select.copy();
        DataSet copy = select.copy();

        //计算小计 合计 总计行
        DataSet subtotal = subtotal(select);
        DataSet aggregate = aggregate(select1);
        DataSet total = total(select2);
        DataSet union = copy.union(subtotal);
        union = union.orderBy(new String[]{"industrySortCode", "plateSortCode"});
        union = union.union(aggregate);
        union = union.orderBy(new String[]{"plateSortCode"});
        union = union.union(total);
        logger.info("返回给前端的数据：" + ORM.create().toPlainDynamicObjectCollection(union.copy()).toString());
        return union;
    }

    /**
     * 正式员工/退休返聘的员工数据
     *
     * @param orgIds 权限组织的所有下级id
     * @param map    权限组织的id 对应的下级id
     * @return
     */
    private DataSet formal(List<Long> orgIds, HashMap<Long, List<Long>> map) {
        //公司/部门
        String[] str = new String[]{"XY00001", "XY00005"};//测试数据    正式数据"XY00001", "XY00005"
//        QFilter qFilter = new QFilter("empposrel.tdkw_employtype.number", QCP.in, str);
        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1");
//        qFilter.and("businessstatus", QCP.equals, "1");
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // TODO 这个是项目上的类型啊 复用环境根本没有
//        qFilter.and("filetype.postype.number", QCP.equals, "XY00001");
        // 1010_S和1150_S，雇佣员工和返聘员工
        qFilter.and("filetype.postype.number", QCP.in, Lists.newArrayList("1010_S", "1150_S"));
        qFilter.and("empposrel.datastatus", QCP.equals, "1");
        qFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("empposrel.businessstatus", QCP.equals, "1");
        qFilter.and("empposrel.isprimary", QCP.equals, "1");
        QFilter qFilter1 = new QFilter("empposrel.adminorg.id", QCP.in, orgIds);
        qFilter1.or("empposrel.company.id", QCP.in, orgIds);
        logger.info("人员查询条件" + qFilter.toString());
        //查询传入的组织id所包含的人员
        DataSet query = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", "empposrel.adminorg.id as " +
                "cid,empposrel.company.id as bid,empposrel.adminorg.structlongnumber as orgLongNumber,number", new QFilter[]{qFilter, qFilter1}, null);
        //按照组织id和长编码分组
        DataSet group = query.groupBy(new String[]{"cid", "bid"}).count("number").finish();
        //分组之后的数据根据长编码匹配 汇总到原组织
        DynamicObjectCollection groupObject = ORM.create().toPlainDynamicObjectCollection(group);
        logger.info("构造前正式员工/退休返聘的员工数据", groupObject.toString());
        DataSet sumData = createSumData(groupObject, map);
        logger.info("正式员工/退休返聘的员工数据：" + ORM.create().toPlainDynamicObjectCollection(sumData.copy()).toString());
        return sumData;
    }

    /**
     * 劳务派遣/劳务人员的员工数据
     *
     * @param orgIds 权限组织的所有下级id
     * @param map    权限组织的id 对应的下级id
     * @return
     */
    private DataSet dispatch(List<Long> orgIds, HashMap<Long, List<Long>> map) {
        String[] str = new String[]{"XY00007", "XY00008"};//测试数据  正式数据"XY00007","XY00008"
//        QFilter qFilter = new QFilter("empposrel.tdkw_employtype.number", QCP.in, str);
//        qFilter.and("businessstatus", QCP.equals, "1");
        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1");
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        qFilter.and("filetype.postype.number", QCP.equals, "XY00001");
        // 这里暂时给一个 1110_S 劳务派遣人员主档案
        qFilter.and("filetype.postype.number", QCP.in, Lists.newArrayList("1110_S"));
        qFilter.and("empposrel.datastatus", QCP.equals, "1");
        qFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("empposrel.businessstatus", QCP.equals, "1");
        qFilter.and("empposrel.isprimary", QCP.equals, "1");
        QFilter qFilter1 = new QFilter("empposrel.adminorg.id", QCP.in, orgIds);
        qFilter1.or("empposrel.company.id", QCP.in, orgIds);
        logger.info("人员查询条件" + qFilter.toString());
        //查询传入的组织id所包含的人员
        DataSet query = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", "empposrel.adminorg.id as cid,empposrel.company.id as bid,empposrel.adminorg.structlongnumber as orgLongNumber,number", new QFilter[]{qFilter, qFilter1}, null);
        //按照组织id和长编码分组
        DataSet group = query.groupBy(new String[]{"cid", "bid"}).count("number").finish();
        //分组之后的数据根据长编码匹配 汇总到原组织
        DynamicObjectCollection groupObject = ORM.create().toPlainDynamicObjectCollection(group);
        logger.info("构造前劳务派遣/劳务人员的员工数据", groupObject.toString());
        DataSet sumData = createSumData(groupObject, map);
        logger.info("劳务派遣/劳务人员的员工数据：" + ORM.create().toPlainDynamicObjectCollection(sumData.copy()).toString());
        return sumData;
    }

    /**
     * 劳务外包的员工数据
     *
     * @param orgIds 权限组织的所有下级id
     * @param map    权限组织的id 对应的下级id
     * @return
     */
    private DataSet outsource(List<Long> orgIds, HashMap<Long, List<Long>> map) {
//        QFilter qFilter = new QFilter("empposrel.tdkw_employtype.number", QCP.equals, "XY00009");
//        qFilter.and("businessstatus", QCP.equals, "1");
        QFilter qFilter = new QFilter("businessstatus", QCP.equals, "1");
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        qFilter.and("filetype.postype.number", QCP.equals, "XY00001");
        // 给个外部人员吧，我也不知道是啥
        qFilter.and("filetype.postype.number", QCP.equals, "1200_S");
        qFilter.and("empposrel.datastatus", QCP.equals, "1");
        qFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("empposrel.businessstatus", QCP.equals, "1");
        qFilter.and("empposrel.isprimary", QCP.equals, "1");
        QFilter qFilter1 = new QFilter("empposrel.adminorg.id", QCP.in, orgIds);
        qFilter1.or("empposrel.company.id", QCP.in, orgIds);
        logger.info("人员查询条件" + qFilter.toString());
        //查询传入的组织id所包含的人员
        DataSet query = QueryServiceHelper.queryDataSet(algoKey, "hspm_ermanfile", "empposrel.adminorg.id as cid,empposrel.company.id as bid,empposrel.adminorg.structlongnumber as orgLongNumber,number", new QFilter[]{qFilter, qFilter1}, null);
        //按照组织id和长编码分组
        DataSet group = query.groupBy(new String[]{"cid", "bid"}).count("number").finish();
        //分组之后的数据根据长编码匹配 汇总到原组织
        DynamicObjectCollection groupObject = ORM.create().toPlainDynamicObjectCollection(group);
        logger.info("构造前劳务外包的员工数据", groupObject.toString());
        DataSet sumData = createSumData(groupObject, map);
        logger.info("劳务外包的员工数据：" + ORM.create().toPlainDynamicObjectCollection(sumData.copy()).toString());
        return sumData;
    }

    /**
     * @param query 根据组织id查询的人员数据
     * @param map   权限组织的id 对应的下级id
     * @return
     */
    private DataSet createSumData(DynamicObjectCollection query, HashMap<Long, List<Long>> map) {
        //query 的id in orgIds orgids的下标 = affiliatedbusIds的下标
        Set<Long> set = map.keySet();
        Map<Long, Integer> dataMap = new HashMap<>();
        for (Long id : set) {
            List<Long> ids = map.get(id);
            for (DynamicObject object : query) {
                long cid = object.getLong("cid");
                long bid = object.getLong("bid");
                int number = object.getInt("number");
                if (ids.contains(cid) || ids.contains(bid)) {
                    if (ObjectUtils.isNotEmpty(dataMap.get(id))) {
                        dataMap.put(id, number + dataMap.get(id));
                    } else {
                        dataMap.put(id, number);
                    }
                }
            }
        }
        // 创建一个空的DataSet
        Collection<Object[]> coll = new ArrayList<Object[]>();
        for (Long id : set) {
            Object[] objArr = new Object[2];
            objArr[0] = id;
            objArr[1] = dataMap.get(id);
            coll.add(objArr);
        }
        String[] fields = new String[]{"cid", "number"};
        DataType[] types = new DataType[]{DataType.LongType, DataType.IntegerType};
        RowMeta rowMeta = RowMetaFactory.createRowMeta(fields, types);
        CollectionInput inputs = new CollectionInput(rowMeta, coll);
        DataSet resultDataSet = Algo.create(this.getClass().getName()).createDataSet(inputs);
        logger.info("构建完毕的dataset的数据：" + ORM.create().toPlainDynamicObjectCollection(resultDataSet.copy()).toString());
        return resultDataSet;
    }

    /**
     * 计算小计行
     *
     * @param select
     * @return
     */
    private DataSet subtotal(DataSet select) {
        //分组 小计之后的数据
        DataSet finishDataSet = select.groupBy(new String[]{"tdkw_plate", "tdkw_industry", "tdkw_plateid", "tdkw_industryid", "industrySortCode", "plateSortCode"}).sum("tdkw_formalperson").sum("tdkw_dispatchperson").sum("tdkw_outsourceperson").sum("tdkw_sum").finish();
        finishDataSet = finishDataSet.addField("'小计'", "tdkw_org");
        finishDataSet = finishDataSet.addNullField("tdkw_orgid").addNullField("orgSortCode");
        //添加数据列  人员数据和板块数据union合并需要字段数量类型统一
        //重新查询一遍 然后union
        DataSet data = finishDataSet.select("tdkw_plate, tdkw_industry,tdkw_org, tdkw_sum, tdkw_formalperson, tdkw_dispatchperson, tdkw_outsourceperson,tdkw_orgid,tdkw_industryid,tdkw_plateid,plateSortCode,industrySortCode,orgSortCode");
        logger.info("小计行的数据：" + ORM.create().toPlainDynamicObjectCollection(data.copy()).toString());
        finishDataSet.close();
        return data;
    }

    /**
     * 计算合计行
     *
     * @param select
     * @return
     */
    private DataSet aggregate(DataSet select) {
        //分组 合计之后的数据
        DataSet finishDataSet = select.groupBy(new String[]{"tdkw_plate", "tdkw_plateid", "plateSortCode"}).sum("tdkw_formalperson").sum("tdkw_dispatchperson").sum("tdkw_outsourceperson").sum("tdkw_sum").finish();
        //添加数据列  人员数据和板块数据union合并需要字段数量类型统一
        finishDataSet = finishDataSet.addField("'合计'", "tdkw_industry");
        finishDataSet = finishDataSet.addNullField("tdkw_org");
        finishDataSet = finishDataSet.addNullField("tdkw_orgid");
        finishDataSet = finishDataSet.addNullField("tdkw_industryid").addNullField("industrySortCode").addNullField("orgSortCode");
        //重新查询一遍 然后union
        DataSet data = finishDataSet.select("tdkw_plate, tdkw_industry,tdkw_org, tdkw_sum, tdkw_formalperson, tdkw_dispatchperson, tdkw_outsourceperson,tdkw_orgid,tdkw_industryid,tdkw_plateid,plateSortCode,industrySortCode,orgSortCode");
        logger.info("合计行的数据：" + ORM.create().toPlainDynamicObjectCollection(data.copy()).toString());
        finishDataSet.close();
        //合计
        return data;
    }

    /**
     * 计算总计行
     *
     * @param select
     * @return
     */
    private DataSet total(DataSet select) {
        //分组 总计之后的数据
        DataSet finishDataSet = select.groupBy(null).sum("tdkw_formalperson").sum("tdkw_dispatchperson").sum("tdkw_outsourceperson").sum("tdkw_sum").finish();
        finishDataSet = finishDataSet.addField("'总计'", "tdkw_plate");
        //添加数据列  人员数据和板块数据union合并需要字段数量类型统一
        finishDataSet = finishDataSet.addNullField("tdkw_org").addNullField("tdkw_industry").addNullField("tdkw_orgid").addNullField("tdkw_plateid").addNullField("tdkw_industryid").addNullField("plateSortCode").addNullField("industrySortCode").addNullField("orgSortCode");
        //重新查询一遍 然后union
        DataSet data = finishDataSet.select("tdkw_plate, tdkw_industry,tdkw_org, tdkw_sum, tdkw_formalperson, tdkw_dispatchperson, tdkw_outsourceperson,tdkw_orgid,tdkw_industryid,tdkw_plateid,plateSortCode,industrySortCode,orgSortCode");
        logger.info("总计行的数据：" + ORM.create().toPlainDynamicObjectCollection(data.copy()).toString());
        finishDataSet.close();
        //总计
        return data;
    }
}
