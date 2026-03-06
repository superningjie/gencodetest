package dgdl.odc.homs.util;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 月度计划编制信息重算工具类
 *
 * @Description:
 * @Author: e-Gang.Xu
 * @Since: 2024/3/21
 **/
public class PlanMonthBillReCalUtil {
    private static final List<String> laborreltype = Arrays.asList("helpmate", "interns_reserve_personnel", "labor_dispatch", "regular_workers", "rehired_after_retirement");

    /**
     * 重新计算人数 组织包含下级组织人数
     *
     * @param dynamicObject
     * @return
     */
    public static DynamicObject reCalActual(DynamicObject dynamicObject) {
        DynamicObjectCollection bzCollection = dynamicObject.getDynamicObjectCollection("dgdl_bz_entry");//编制分录
        if (bzCollection != null) {
            Map<Long, Integer> sumActualMap = new HashMap<>();
            //汇总人数
            for (DynamicObject item : bzCollection) {
                int sumActual = item.getInt("dgdl_bz_actual");
                DynamicObject dgdl_bz_adminorg1 = item.getDynamicObject("dgdl_bz_adminorg1");
                DynamicObject dgdl_bz_adminorg2 = item.getDynamicObject("dgdl_bz_adminorg2");
                DynamicObject dgdl_bz_adminorg3 = item.getDynamicObject("dgdl_bz_adminorg3");
                DynamicObject dgdl_bz_adminorg4 = item.getDynamicObject("dgdl_bz_adminorg4");
                DynamicObject dgdl_bz_adminorg5 = item.getDynamicObject("dgdl_bz_adminorg5");
                DynamicObject dgdl_bz_adminorg6 = item.getDynamicObject("dgdl_bz_adminorg6");
                //同岗位属性，同用工关系类型
                if (dgdl_bz_adminorg1 != null && dgdl_bz_adminorg2 == null) {
                    sumActual = bzCollection.stream().filter(e -> e.getDynamicObject("dgdl_bz_adminorg1") != null && e.getLong("dgdl_bz_adminorg1.id") == dgdl_bz_adminorg1.getLong("id")
                                    && e.getString("dgdl_bz_jobproperty").equals(item.getString("dgdl_bz_jobproperty")) && e.getString("dgdl_bz_laborreltype").equals(item.getString("dgdl_bz_laborreltype")))
                            .mapToInt(e -> e.getInt("dgdl_bz_actual")).sum();
                } else if (dgdl_bz_adminorg2 != null && dgdl_bz_adminorg3 == null) {
                    sumActual = bzCollection.stream().filter(e -> e.getDynamicObject("dgdl_bz_adminorg2") != null && e.getLong("dgdl_bz_adminorg2.id") == dgdl_bz_adminorg2.getLong("id")
                                    && e.getString("dgdl_bz_jobproperty").equals(item.getString("dgdl_bz_jobproperty")) && e.getString("dgdl_bz_laborreltype").equals(item.getString("dgdl_bz_laborreltype")))
                            .mapToInt(e -> e.getInt("dgdl_bz_actual")).sum();
                } else if (dgdl_bz_adminorg3 != null && dgdl_bz_adminorg4 == null) {
                    sumActual = bzCollection.stream().filter(e -> e.getDynamicObject("dgdl_bz_adminorg3") != null && e.getLong("dgdl_bz_adminorg3.id") == dgdl_bz_adminorg3.getLong("id")
                                    && e.getString("dgdl_bz_jobproperty").equals(item.getString("dgdl_bz_jobproperty")) && e.getString("dgdl_bz_laborreltype").equals(item.getString("dgdl_bz_laborreltype")))
                            .mapToInt(e -> e.getInt("dgdl_bz_actual")).sum();
                } else if (dgdl_bz_adminorg4 != null && dgdl_bz_adminorg5 == null) {
                    sumActual = bzCollection.stream().filter(e -> e.getDynamicObject("dgdl_bz_adminorg4") != null && e.getLong("dgdl_bz_adminorg4.id") == dgdl_bz_adminorg4.getLong("id")
                                    && e.getString("dgdl_bz_jobproperty").equals(item.getString("dgdl_bz_jobproperty")) && e.getString("dgdl_bz_laborreltype").equals(item.getString("dgdl_bz_laborreltype")))
                            .mapToInt(e -> e.getInt("dgdl_bz_actual")).sum();
                } else if (dgdl_bz_adminorg5 != null && dgdl_bz_adminorg6 == null) {
                    sumActual = bzCollection.stream().filter(e -> e.getDynamicObject("dgdl_bz_adminorg5") != null && e.getLong("dgdl_bz_adminorg5.id") == dgdl_bz_adminorg5.getLong("id")
                                    && e.getString("dgdl_bz_jobproperty").equals(item.getString("dgdl_bz_jobproperty")) && e.getString("dgdl_bz_laborreltype").equals(item.getString("dgdl_bz_laborreltype")))
                            .mapToInt(e -> e.getInt("dgdl_bz_actual")).sum();
                } else if (dgdl_bz_adminorg6 != null) {
                    sumActual = bzCollection.stream().filter(e -> e.getDynamicObject("dgdl_bz_adminorg6") != null && e.getLong("dgdl_bz_adminorg6.id") == dgdl_bz_adminorg6.getLong("id")
                                    && e.getString("dgdl_bz_jobproperty").equals(item.getString("dgdl_bz_jobproperty")) && e.getString("dgdl_bz_laborreltype").equals(item.getString("dgdl_bz_laborreltype")))
                            .mapToInt(e -> e.getInt("dgdl_bz_actual")).sum();
                }
                sumActualMap.put(item.getLong("id"), sumActual);
            }
            //填充人数
            for (DynamicObject item : bzCollection) {
                if (sumActualMap.get(item.getLong("id")) != null) {
                    item.set("dgdl_bz_actual", sumActualMap.get(item.getLong("id")));
                }
            }
        }
        return dynamicObject;
    }

    /**
     * 补全组织下行数
     *
     * @param dynamicObject
     */
    public static void createMonthDetail(DynamicObject dynamicObject) {
        DynamicObjectCollection bzCollection = dynamicObject.getDynamicObjectCollection("dgdl_bz_entry");//编制分录
        if (bzCollection != null) {
            int labeldimension = getLabeldimension(dynamicObject.getDynamicObject("dgdl_planmonth"));
            //用工关系类型维度
            Map<Long, List<DynamicObject>> orgMap = bzCollection.stream().filter(e -> e.getDynamicObject("adminorg") != null).collect(Collectors.groupingBy(e -> e.getLong("adminorg.id")));
            for (Long org : orgMap.keySet()) {
                List<DynamicObject> dynamicObjectList = orgMap.get(org);
                DynamicObject bzDy0 = dynamicObjectList.get(0);
                //一线
                boolean hasYixian = dynamicObjectList.stream().anyMatch(e -> e.getString("dgdl_bz_jobproperty").equals("0"));
                if (!hasYixian) {
                    if (labeldimension == 2) {
                        createBzColl("2", bzCollection, bzDy0, "0");
                    } else if (labeldimension == 3) {
                        createBzColl("", bzCollection, bzDy0, "0");
                    }
                }
                //非一线
                boolean hasNotYixian = dynamicObjectList.stream().anyMatch(e -> e.getString("dgdl_bz_jobproperty").equals("1"));
                if (!hasNotYixian) {
                    if (labeldimension == 2) {
                        createBzColl("2", bzCollection, bzDy0, "1");
                    } else if (labeldimension == 3) {
                        createBzColl("", bzCollection, bzDy0, "1");
                    }
                }
            }
        }
    }

    /**
     * 创建月度编制明细后，若同一末级组织下缺少岗位属性，用工关系类型维度数据，则补全分录行人数默认0
     *
     * @param labeldimension 月度编制计划标签维度选择一线非一线，全选时 需要按照维度补全一线非一线数据行
     * @param bzCollection
     * @param bzDy
     * @param dgdl_bz_jobproperty
     */
    private static void createBzColl(String labeldimension, DynamicObjectCollection bzCollection, DynamicObject bzDy, String dgdl_bz_jobproperty) {
        //按最细维度补行
        if (StringUtils.isEmpty(labeldimension)) {
            for (String laborreltypeNumber : laborreltype) {
                DynamicObject bzNew = bzCollection.addNew();//编制行
                bzNew.set("adminorg", bzDy.get("adminorg"));
                bzNew.set("dgdl_bz_adminorg1", bzDy.get("dgdl_bz_adminorg1"));
                bzNew.set("dgdl_bz_adminorg2", bzDy.get("dgdl_bz_adminorg2"));
                bzNew.set("dgdl_bz_adminorg3", bzDy.get("dgdl_bz_adminorg3"));
                bzNew.set("dgdl_bz_adminorg4", bzDy.get("dgdl_bz_adminorg4"));
                bzNew.set("dgdl_bz_adminorg5", bzDy.get("dgdl_bz_adminorg5"));
                bzNew.set("dgdl_bz_adminorg6", bzDy.get("dgdl_bz_adminorg6"));
                bzNew.set("dgdl_bz_controlediting", bzDy.getString("dgdl_bz_controlediting"));
                bzNew.set("dgdl_bz_way", bzDy.getString("dgdl_bz_way"));
                bzNew.set("dgdl_isfreeze", "01");
                bzNew.set("dgdl_bz_percent", bzDy.getInt("dgdl_bz_percent"));
                bzNew.set("dgdl_bz_jobproperty", dgdl_bz_jobproperty);
                bzNew.set("dgdl_bz_laborreltype", laborreltypeNumber);
                //实际人数
                bzNew.set("dgdl_bz_actual", 0);
                bzNew.set("dgdl_bz_isinsert", "01");//系统内置
                //正式工拆分大雁行
                if (laborreltypeNumber.equals("regular_workers")) {
                    DynamicObject bzNew2 = bzCollection.addNew();//编制行
                    bzNew2.set("adminorg", bzDy.get("adminorg"));
                    bzNew2.set("dgdl_bz_adminorg1", bzDy.get("dgdl_bz_adminorg1"));
                    bzNew2.set("dgdl_bz_adminorg2", bzDy.get("dgdl_bz_adminorg2"));
                    bzNew2.set("dgdl_bz_adminorg3", bzDy.get("dgdl_bz_adminorg3"));
                    bzNew2.set("dgdl_bz_adminorg4", bzDy.get("dgdl_bz_adminorg4"));
                    bzNew2.set("dgdl_bz_adminorg5", bzDy.get("dgdl_bz_adminorg5"));
                    bzNew2.set("dgdl_bz_adminorg6", bzDy.get("dgdl_bz_adminorg6"));
                    bzNew2.set("dgdl_bz_controlediting", bzDy.getString("dgdl_bz_controlediting"));
                    bzNew2.set("dgdl_bz_way", bzDy.getString("dgdl_bz_way"));
                    bzNew2.set("dgdl_isfreeze", "01");
                    bzNew2.set("dgdl_bz_percent", bzDy.getInt("dgdl_bz_percent"));
                    bzNew2.set("dgdl_bz_jobproperty", dgdl_bz_jobproperty);
                    bzNew2.set("dgdl_bz_laborreltype", "dayan");
                    //实际人数
                    bzNew2.set("dgdl_bz_actual", 0);
                    bzNew2.set("dgdl_bz_isinsert", "01");//系统内置
                }
            }
        } else {
            //按一线非一线维度补行
            DynamicObject bzNew = bzCollection.addNew();//编制行
            bzNew.set("adminorg", bzDy.get("adminorg"));
            bzNew.set("dgdl_bz_adminorg1", bzDy.get("dgdl_bz_adminorg1"));
            bzNew.set("dgdl_bz_adminorg2", bzDy.get("dgdl_bz_adminorg2"));
            bzNew.set("dgdl_bz_adminorg3", bzDy.get("dgdl_bz_adminorg3"));
            bzNew.set("dgdl_bz_adminorg4", bzDy.get("dgdl_bz_adminorg4"));
            bzNew.set("dgdl_bz_adminorg5", bzDy.get("dgdl_bz_adminorg5"));
            bzNew.set("dgdl_bz_adminorg6", bzDy.get("dgdl_bz_adminorg6"));
            bzNew.set("dgdl_bz_controlediting", bzDy.getString("dgdl_bz_controlediting"));
            bzNew.set("dgdl_bz_way", bzDy.getString("dgdl_bz_way"));
            bzNew.set("dgdl_isfreeze", "01");
            bzNew.set("dgdl_bz_percent", bzDy.getInt("dgdl_bz_percent"));
            bzNew.set("dgdl_bz_jobproperty", dgdl_bz_jobproperty);
            bzNew.set("dgdl_bz_laborreltype", "");
            //实际人数
            bzNew.set("dgdl_bz_actual", 0);
            bzNew.set("dgdl_bz_isinsert", "01");//系统内置
        }
    }

    /**
     * 创建月度编制明细后，若同一末级组织下缺少岗位属性，用工关系类型维度数据，则补全分录行人数默认0
     *
     * @param kbCollection
     * @param kbDy
     * @param dgdl_bz_jobproperty
     */
    private static void createKbColl(DynamicObjectCollection kbCollection, DynamicObject kbDy, String dgdl_bz_jobproperty) {
        for (String laborreltypeNumber : laborreltype) {
            DynamicObject kbNew = kbCollection.addNew();//编制行
            kbNew.set("dgdl_kb_adminorg", kbDy.get("dgdl_kb_adminorg"));
            kbNew.set("dgdl_kb_adminorg1", kbDy.get("dgdl_kb_adminorg1"));
            kbNew.set("dgdl_kb_adminorg2", kbDy.get("dgdl_kb_adminorg2"));
            kbNew.set("dgdl_kb_adminorg3", kbDy.get("dgdl_kb_adminorg3"));
            kbNew.set("dgdl_kb_adminorg4", kbDy.get("dgdl_kb_adminorg4"));
            kbNew.set("dgdl_kb_adminorg5", kbDy.get("dgdl_kb_adminorg5"));
            kbNew.set("dgdl_kb_adminorg6", kbDy.get("dgdl_kb_adminorg6"));
            kbNew.set("dgdl_kb_controlediting", kbDy.getString("dgdl_kb_controlediting"));
            kbNew.set("dgdl_kb_way", kbDy.getString("dgdl_kb_way"));
            kbNew.set("dgdl_kb_isfreeze", "01");
            kbNew.set("dgdl_kb_percent", kbDy.getInt("dgdl_kb_percent"));
            kbNew.set("dgdl_kb_jobproperty", dgdl_bz_jobproperty);
            kbNew.set("dgdl_kb_laborreltype", laborreltypeNumber);
            //实际人数
            kbNew.set("dgdl_kb_actual", 0);
            //正式工拆分大雁行
            if (laborreltypeNumber.equals("regular_workers")) {
                DynamicObject kbNew2 = kbCollection.addNew();//控编行
                kbNew2.set("dgdl_kb_adminorg", kbDy.get("dgdl_kb_adminorg"));
                kbNew2.set("dgdl_kb_adminorg1", kbDy.get("dgdl_kb_adminorg1"));
                kbNew2.set("dgdl_kb_adminorg2", kbDy.get("dgdl_kb_adminorg2"));
                kbNew2.set("dgdl_kb_adminorg3", kbDy.get("dgdl_kb_adminorg3"));
                kbNew2.set("dgdl_kb_adminorg4", kbDy.get("dgdl_kb_adminorg4"));
                kbNew2.set("dgdl_kb_adminorg5", kbDy.get("dgdl_kb_adminorg5"));
                kbNew2.set("dgdl_kb_adminorg6", kbDy.get("dgdl_kb_adminorg6"));
                kbNew2.set("dgdl_kb_controlediting", kbDy.getString("dgdl_kb_controlediting"));
                kbNew2.set("dgdl_kb_way", kbDy.getString("dgdl_kb_way"));
                kbNew2.set("dgdl_kb_isfreeze", "01");
                kbNew2.set("dgdl_kb_percent", kbDy.getInt("dgdl_kb_percent"));
                kbNew2.set("dgdl_kb_jobproperty", dgdl_bz_jobproperty);
                kbNew2.set("dgdl_kb_laborreltype", "dayan");
                //实际人数
                kbNew2.set("dgdl_kb_actual", 0);
            }
        }
    }

    /**
     * 获取月度计划标签维度
     *
     * @return 0-全不选 1-用工关系类型 2-岗位标签 3-全选
     */
    public static int getLabeldimension(DynamicObject dataEntity) {
        int labeldimension = 0;//全不选
        List<String> dgdlLabeldimensions = Arrays.asList(dataEntity.getString("dgdl_labeldimension").split(","));//标签维度
        if (dgdlLabeldimensions.contains("1") && !dgdlLabeldimensions.contains("2")) {
            labeldimension = 1;
        } else if (!dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 2;
        } else if (dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 3;
        }
        return labeldimension;
    }
}
