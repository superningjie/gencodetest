package tdkw.esc.tdkw_appauthority.common.mobie;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrgAndPeopleStatisticsUtil
 * 如果没有组织过滤,以板块统计 如果有组织过滤,以下属公司统计
 *
 * @author XXXXyx
 * @date 2023/8/22
 */
public class OrgAndPeopleStatisticsUtil {
    private static final Log logger = LogFactory.getLog(OrgAndPeopleStatisticsUtil.class);

    /**
     * 如果没有组织过滤,以板块统计 如果有组织过滤,以下属公司统计
     *
     * @param adminOrgMap 没有组织过滤,以板块统计 如果有组织过滤,以下属公司统计
     * @param pkOrg
     * @return
     */
    public static Map getOrgAndPeopleMap(Map<Long, Long> adminOrgMap, String pkOrg) {
        // adminOrgMap.put(10000L, 76L);
        logger.info("入参组织id" + Arrays.toString(adminOrgMap.keySet().toArray()));
        logger.info("入参组织数量" + Arrays.toString(adminOrgMap.values().toArray()));
        HashMap<Object, Long> finishMap = new HashMap<>();
        // 如果没有组织过滤,以板块统计
        if (StringUtils.isBlank(pkOrg)) {
            pkOrg = "100000";
        }
        QFilter qFilter = new QFilter("parent", QCP.equals, Long.valueOf(pkOrg));
        qFilter.and("iscurrentversion", QCP.equals, '1');
        qFilter.and("datastatus", QCP.equals, '1');
        qFilter.and("enable", QCP.equals, '1');
        DynamicObject[] topLoad = BusinessDataServiceHelper.load("haos_adminorghr", "id,name,structlongnumber", qFilter.toArray());
        if (null == topLoad || topLoad.length == 0) {
            return adminOrgMap;
        }
        List<String> name = Arrays.stream(topLoad).map(i -> i.getString("name")).collect(Collectors.toList());
        logger.info("下级组织名称" + Arrays.toString(name.toArray()));
//        System.out.println("下级组织名称" + Arrays.toString(name.toArray()));
        // 获取所有一级组织的长编码和id的映射
        Map<String, Long> idAndStructLongNumberMap = Arrays.stream(topLoad).collect(Collectors.toMap(i -> i.getString("structlongnumber"), i -> i.getLong("id")));
        logger.info("一级组织的长编码：" + idAndStructLongNumberMap);
        // 获取所有参数组织的长编码和id的映射
        List<Object> orgList = new ArrayList<>(adminOrgMap.keySet());
        // orgList = new ArrayList<>();
        // orgList.add(100000L);
        // List<Long> orgLongList = orgList.stream().map(i -> Long.parseLong((String) i)).collect(Collectors.toList());
        qFilter = new QFilter("id", QCP.in, orgList);
        qFilter.and("iscurrentversion", QCP.equals, '1');
        qFilter.and("datastatus", QCP.equals, '1');
        qFilter.and("enable", QCP.equals, '1');
        DynamicObject[] parameterLoad = BusinessDataServiceHelper.load("haos_adminorghr", "id,structlongnumber", qFilter.toArray());
        Map<String, Object> parameterIdAndStructLongNumberMap = Arrays.stream(parameterLoad).collect(Collectors.toMap(i -> i.getString("structlongnumber"), i -> i.getLong("id")));
        logger.info("组织上下级长编码：" + parameterIdAndStructLongNumberMap);
        // IdentityHashMap<Object, Long> identityHashMap = new IdentityHashMap<>();
        // 遍历入参map
        long start = System.currentTimeMillis();
        for (String paramStruct : parameterIdAndStructLongNumberMap.keySet()) {
            for (String topStruct : idAndStructLongNumberMap.keySet()) {
                if (paramStruct.contains(topStruct)) {
                    finishMap.put(idAndStructLongNumberMap.get(topStruct),
                            finishMap.containsKey(idAndStructLongNumberMap.get(topStruct)) ?
                                    finishMap.get(idAndStructLongNumberMap.get(topStruct)) + adminOrgMap.get(parameterIdAndStructLongNumberMap.get(paramStruct)) :
                                    adminOrgMap.get(parameterIdAndStructLongNumberMap.get(paramStruct)));
                }
            }
        }
        long end = System.currentTimeMillis();
        long time = (end - start) / 1000;
//        System.out.println("for循环执行时间 = " + time);
        logger.info("for循环执行时间 = " + time);
        logger.info("返回值组织id" + Arrays.toString(finishMap.keySet().toArray()));
        logger.info("返回值组织数量" + Arrays.toString(finishMap.values().toArray()));
        // System.out.println("返回值组织数量" + Arrays.toString(adminOrgMap.values().toArray()));
        // 排序
        qFilter = new QFilter("id", QCP.in, finishMap.keySet());
        qFilter.and("iscurrentversion", QCP.equals, '1');
        qFilter.and("datastatus", QCP.equals, '1');
        qFilter.and("enable", QCP.equals, '1');
        DynamicObject[] finishLoad = BusinessDataServiceHelper.load("haos_adminorghr", "id,sortcode", qFilter.toArray());
        // 获取id和排序码的集合
        Map<Long, String> idAndSortCodeMap = Arrays.stream(finishLoad).collect(Collectors.toMap(i -> i.getLong("id"), i -> i.getString("sortcode")));
        logger.info("最终HR行政组织：" + idAndSortCodeMap);
        // 根据排序码排序
        LinkedHashMap<Long, Long> idAndSortCodeSortMap = new LinkedHashMap<>();
        idAndSortCodeMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(x -> idAndSortCodeSortMap.put(x.getKey(), finishMap.get(x.getKey())));
        return idAndSortCodeSortMap;
    }
}
