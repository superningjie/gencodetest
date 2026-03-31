package dgdl.odc.homs.validator;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.StringUtils;
import kd.hr.haos.business.servicehelper.OrgBatchBillHelper;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MonthBillSaveValidator extends AbstractValidator {

    private static Log logger = LogFactory.getLog(MonthBillSaveValidator.class);

    private final static String Filed = "adminorg,dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6,dgdl_bz_laborreltype,dgdl_bz_jobproperty,dgdl_jz1,dgdl_jz2,dgdl_jz3,dgdl_jz4,dgdl_jz5,dgdl_jz6,dgdl_jz7" +
            ",dgdl_jz8,dgdl_jz9,dgdl_jz10,dgdl_jz11,dgdl_jz12,dgdl_bz1,dgdl_bz2,dgdl_bz3,dgdl_bz4,dgdl_bz5,dgdl_bz6,dgdl_bz7,dgdl_bz8,dgdl_bz9,dgdl_bz10,dgdl_bz11,dgdl_bz12";


    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        for (ExtendedDataEntity dataEntitie : dataEntities) {
            DynamicObject dataEntity = dataEntitie.getDataEntity();
            Long pkValue = (Long) dataEntity.getPkValue();
            QFilter bzQFilter = new QFilter("dgdl_planmonth_bill.id", QCP.equals, pkValue);
            //计划
            DynamicObject planMonth = dataEntity.getDynamicObject("dgdl_planmonth");
            //编制信息
            DynamicObject[] bzLoads = BusinessDataServiceHelper.load("dgdl_planmonth_bz", Filed, bzQFilter.toArray());
            //维度
            String labeldimension = dataEntity.getDynamicObject("dgdl_planmonth").getString("dgdl_labeldimension");
            logger.info("MonthBillSaveValidator用工关系类型=" + labeldimension);
            if (bzLoads.length > 0 && StringUtils.isNotEmpty(labeldimension)) {
                //1=用工关系类型:dgdl_bz_laborreltype,2=一线非一线:dgdl_bz_jobproperty
                int i = 0;
                if (labeldimension.contains("1") && labeldimension.contains("2")) {
                    i = 3;
                } else if (labeldimension.contains("1")) {
                    i = 1;
                } else if (labeldimension.contains("2")) {
                    i = 2;
                }
                Map<String, List<DynamicObject>> classifyData = new HashMap<>();

                if (i == 3) {
                    classifyData = Arrays.stream(bzLoads).collect(Collectors.groupingBy(f -> f.getString("dgdl_bz_jobproperty")));
                    Set<String> keySet = classifyData.keySet();
                    for (String key : keySet) {
                        List<DynamicObject> objectList = classifyData.get(key);
                        //最小颗粒度
                        Map<String, List<DynamicObject>> dgdlBzLaborreltype = objectList.stream().collect(Collectors.groupingBy(f -> f.getString("dgdl_bz_laborreltype")));
                        groupAdmin(dgdlBzLaborreltype, planMonth);
                    }
                } else {
                    if (i == 1) {
                        //用工关系类型
                        classifyData = Arrays.stream(bzLoads).collect(Collectors.groupingBy(f -> f.getString("dgdl_bz_laborreltype")));
                    } else if (i == 2) {
                        //一线非一线
                        classifyData = Arrays.stream(bzLoads).collect(Collectors.groupingBy(f -> f.getString("dgdl_bz_jobproperty")));
                    }
                    groupAdmin(classifyData, planMonth);
                }
            }
        }
    }

    private void groupAdmin(Map<String, List<DynamicObject>> classifyData, DynamicObject planMonth) {
        //开始日期
        Date startmonth = planMonth.getDate("dgdl_startmonth");
        //结束日期
        Date endmonth = planMonth.getDate("dgdl_endmonth");
        //格式化日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int startInt = Integer.parseInt(dateFormat.format(startmonth).split("-")[1]);
        int endStrInt = Integer.parseInt(dateFormat.format(endmonth).split("-")[1]);
        //层级
        int orghierarchy = planMonth.getInt("dgdl_orghierarchy");
        //下级基准数
        StringBuilder jzSb = new StringBuilder();
        //下级编制数
        StringBuilder bzSb = new StringBuilder();
        //当月校验
        StringBuilder thisSb = new StringBuilder();
        //码值存储
        //岗位属性
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("0", "一线");
        propertyMap.put("1", "非一线");
        //用工关系类型
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("helpmate", "合作伙伴");
        typeMap.put("interns_reserve_personnel", "实习生");
        typeMap.put("labor_dispatch", "劳务派遣");
        typeMap.put("regular_workers", "正式工");
        typeMap.put("rehired_after_retirement", "退休返聘");
        typeMap.put("dayan", "大雁");
        
        //最终分组数据
        for (String bzStr : classifyData.keySet()) {
            List<DynamicObject> bzList = classifyData.get(bzStr);
            Set<Long> idSet = bzList.stream().map(bzData -> bzData.getDynamicObject("adminorg").getLong("id")).collect(Collectors.toSet());
            Map<Long, String> orgLongNameMap = OrgBatchBillHelper.getOrgLongNameByFid(idSet, new Date(), null);
            Map<Long, List<DynamicObject>> orgMap=new HashMap<>();


            for (int j = 1; j < orghierarchy; j++) {
                for (DynamicObject bz : bzList) {

                    //
                    DynamicObject lastOrg = bz.getDynamicObject("adminorg");

                    DynamicObject adminOrg = bz.getDynamicObject("dgdl_bz_adminorg" + j);



                    if(adminOrg==null||lastOrg.getPkValue().equals(adminOrg.getPkValue())) {

                        continue;
                    }

                    if(orgMap.containsKey(adminOrg.getLong("id"))){

                        List<DynamicObject> sonAdminList =orgMap.get(adminOrg.getLong("id"));
                        sonAdminList.add(bz);
                        orgMap.put(adminOrg.getLong("id"),sonAdminList);

                    }else{
                        List<DynamicObject> sonAdminList =new ArrayList<>();
                        sonAdminList.add(bz);
                        orgMap.put(adminOrg.getLong("id"),sonAdminList);
                    }


                }
            }
           
            for (DynamicObject bzData : bzList) {
                DynamicObject lastOrg = bzData.getDynamicObject("adminorg");
                //行政组织-组织长名称
                String orgLongName = orgLongNameMap.get(lastOrg.getLong("id"));
                if (StringUtils.isNotEmpty(orgLongName)) {
                    orgLongName = orgLongName.substring(orgLongName.indexOf("_") + 1).replaceAll("_", "-");
                }
                //获取当前组织层级
//                int hierarchy = 0;
//                for (int j = 1; j < orghierarchy; j++) {
//                    hierarchy = j;
//                    DynamicObject adminOrg = bzData.getDynamicObject("dgdl_bz_adminorg" + j);
//                    if (Objects.isNull(adminOrg)) {
//                        break;
//                    }
//                }
                //过滤标识
//                String adminStr = "dgdl_bz_adminorg" + hierarchy;
                //过滤出当前组织下的组织
                List<DynamicObject> sonAdminList = orgMap.get(lastOrg.getLong("id"));

//                List<DynamicObject> sonAdminList = bzList.stream()
//                        .filter(bz -> Objects.nonNull(bz.getDynamicObject(adminStr)))
//                        .filter(bz -> bz.getDynamicObject(adminStr).getPkValue().equals(lastOrg.getPkValue())
//                                && !bz.getDynamicObject("adminorg").getPkValue().equals(lastOrg.getPkValue())
//                        ).collect(Collectors.toList());
                //基准数标识
                String jz_Str = "dgdl_jz";
                //编制数标识
                String bz_Str = "dgdl_bz";
                for (int j = startInt; j <= endStrInt; j++) {
                    //当前组织基准数
                    int thisJzCount = Objects.isNull(bzData.get(jz_Str + j)) ? 0 : bzData.getInt(jz_Str + j);
                    //当前组织编制数
                    int thisBzCount = Objects.isNull(bzData.get(bz_Str + j)) ? 0 : bzData.getInt(bz_Str + j);
                    if (thisBzCount > thisJzCount) {
                        thisSb.append(orgLongName).append(j).append("月份编制数超过基准数;").append("\n");
                    }

                    //子级基准数
                    int sonJzCount = 0;
                    //编制基准数
                    int sonBzCount = 0;
                    if(sonAdminList==null){
                        continue;
                    }
                    for (DynamicObject sonAdmin : sonAdminList) {
                        sonJzCount = sonJzCount + (Objects.isNull(sonAdmin.get(jz_Str + j)) ? 0 : sonAdmin.getInt(jz_Str + j));
                        //岗位属性
                        String property = sonAdmin.getString("dgdl_bz_jobproperty");
                        //用工关系类型
                        String type = sonAdmin.getString("dgdl_bz_laborreltype");
                        if (sonJzCount > thisJzCount) {
                            jzSb.append(orgLongName).append("的下级组织")
                                    .append(StringUtils.isEmpty(property) ? "" : propertyMap.get(property))
                                    .append(StringUtils.isEmpty(type) ? "" : typeMap.get(type))
                                    .append(j).append("月份基准数超过当前组织;")
                                    .append("\n");
                            break;
                        }

                        sonBzCount = sonBzCount + (Objects.isNull(sonAdmin.get(bz_Str + j)) ? 0 : sonAdmin.getInt(bz_Str + j));
                        if (sonBzCount > thisBzCount) {
                            bzSb.append(orgLongName).append("的下级组织")
                                    .append(StringUtils.isEmpty(property) ? "" : propertyMap.get(property))
                                    .append(StringUtils.isEmpty(type) ? "" : typeMap.get(type))
                                    .append(j).append("月份编制数超过当前组织;")
                                    .append("\n");
                            break;
                        }
                    }
                }
            }
        }

        //基准数校验(强管控)
        if (!jzSb.toString().isEmpty()) {
            jzSb.append("请调整。");
            throw new KDBizException(jzSb.toString());
        }

        //本身基准数校验(弱管控)
        if (!thisSb.toString().isEmpty()) {
            thisSb.append("请注意。");
            this.addWarningMessage(this.getDataEntities()[0], thisSb.toString());
        }

        //编制数校验(弱管控)
        if (!bzSb.toString().isEmpty()) {
            bzSb.append("请注意。");
            this.addWarningMessage(this.getDataEntities()[0], bzSb.toString());
        }
    }
}
