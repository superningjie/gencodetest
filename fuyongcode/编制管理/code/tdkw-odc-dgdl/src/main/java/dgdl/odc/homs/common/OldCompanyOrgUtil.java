package dgdl.odc.homs.common;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ws
 * @date:2024/8/14 13:22
 * @description: 原公司编码，原组织编码逻辑调整
 */
public class OldCompanyOrgUtil {

    private static Log LOGGER = LogFactory.getLog(OldCompanyOrgUtil.class);
    // 全量的原公司编码集合 001-999
    private static List<String> OLD_COMPANY_NUMBER_ALL = null;
    // 吉利控股原公司编码
    private static String GEELY_OLD_COMPANY_NUMBER = null;
    private static Map<Long, DynamicObject> ADMIN_ORG_TYPE_MAP = null;

    private static final DynamicObjectType ORG_OLD_COMPANY_NUMBER_TYPE = EntityMetadataCache.getDataEntityType("dgdl_org_oldcompanynumber");
    static {
        DynamicObject[] adminOrgTypeArray = BusinessDataServiceHelper.load("haos_adminorgtype",
                "id,number,orgpattern.number",
                new QFilter[]{new QFilter("enable", QCP.equals, "1")});
        ADMIN_ORG_TYPE_MAP = Arrays.stream(adminOrgTypeArray).collect(Collectors.toMap(t -> t.getLong("id"), t -> t));

        DynamicObject geelyOrgDyn = BusinessDataServiceHelper.loadSingle(OldCompanyOrgConstants.ADMIN_ORG_HR,
                "id,dgdl_oldcompan_ext",
                new QFilter[]{
                        new QFilter("boid", QCP.equals, 100000L),
                        new QFilter("iscurrentversion", QCP.equals, "1")
        });
        GEELY_OLD_COMPANY_NUMBER = geelyOrgDyn.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER);

        OLD_COMPANY_NUMBER_ALL = new ArrayList<>();
        for (int i = 1; i <= 999; i++) {
            String number = String.format("%03d", i);
            OLD_COMPANY_NUMBER_ALL.add(number);
        }

    }

    public static void setOldNumberBatch(DynamicObject[] orgDynArray, String orgChangeScene){

        LOGGER.info("OldCompanyOrgUtil&setOldNumberBatch start");
        if (orgDynArray == null || orgDynArray.length == 0){
            return;
        }
        // 组织停用场景不处理
        if (OldCompanyOrgConstants.ORG_DISABLE.equals(orgChangeScene)){
            return;
        }

        //实体对象
        for (DynamicObject obj : orgDynArray) {
            setOldNumber(obj, orgChangeScene);
        }
    }

    public static void setOldNumber(DynamicObject orgDyn, String orgChangeScene){

        LOGGER.info("OldCompanyOrgUtil&setOldNumber start");
        // 判断是否为虚拟组织
        if (orgDyn.getBoolean(OldCompanyOrgConstants.IS_VIRTUAL)){
            // 虚拟组织取对应上级行政组织的所属公司
            Object parentOrgId = orgDyn.get("parentorg.id");
            DynamicObject parentOrgDyn = BusinessDataServiceHelper.loadSingle(parentOrgId, OldCompanyOrgConstants.ADMIN_ORG_HR);
            if (parentOrgDyn != null){
                setOldNumber(parentOrgDyn, orgChangeScene);
            } else {
                throw new KDBizException("找不到对应组织的数据,组织id: " + parentOrgId);
            }
        }

        // 非虚拟组织
        // 当前行政组织类型
        DynamicObject adminOrgType = ADMIN_ORG_TYPE_MAP.get(orgDyn.getLong("adminorgtype.id"));
        // 获取组织形态
        String orgPatternNumber = adminOrgType.getString("orgpattern.number");

        switch (orgChangeScene) {
            // 组织新设的数据
            case OldCompanyOrgConstants.ADD_NEW:
                setOldNumberAddNew(orgDyn, orgPatternNumber);
                break;
            // 信息变更的数据
            case OldCompanyOrgConstants.CHG_INFO:
                setOldNumberChgInfo(orgDyn, orgPatternNumber);
                break;
            // 上级调整的数据
            case OldCompanyOrgConstants.CHG_PARENT:
                setOldNumberChgParent(orgDyn, orgPatternNumber);
                break;
            default:

        }
    }

    // 组织新设时赋值原公司，原组织编码
    public static void setOldNumberAddNew(DynamicObject orgDyn, String orgPatternNumber){

        // 新建组织: 原组织编码默认用行政组织编码
        orgDyn.set(OldCompanyOrgConstants.OLD_ORG_NUMBER, orgDyn.getString(OldCompanyOrgConstants.NUMBER));
        switch (orgPatternNumber){
            // 组织形态为集团的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN1:
                // 原公司编码 默认用吉利控股集团的原公司编码
                orgDyn.set(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, GEELY_OLD_COMPANY_NUMBER);
                break;
            // 组织形态为公司的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN2:
                // 原公司编码 根据中间表和生成规则生成一个三位的原公司编码
                orgDyn.set(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, getOldCompanyNumber());
                break;
            // 组织形态为部门,事业部的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN4:
            case OldCompanyOrgConstants.ORG_PATTERN6:
                DynamicObject parentOrg = BusinessDataServiceHelper.loadSingle(orgDyn.get("parentorg.id"), OldCompanyOrgConstants.ADMIN_ORG_HR);
                orgDyn.set(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, parentOrg.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER));
                break;
        }
        // 原公司组织编码
        orgDyn.set(OldCompanyOrgConstants.OLD_COMMON_NUMBER,
                orgDyn.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER) + orgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER));
    }

    // 组织信息变更时赋值原公司，原组织编码
    public static void setOldNumberChgInfo(DynamicObject orgDyn, String orgPatternNumber){
        switch (orgPatternNumber){
            // 组织形态为集团的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN1:
                break;
            // 组织形态为公司的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN2:
                break;
            // 组织形态为部门,事业部的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN4:
            case OldCompanyOrgConstants.ORG_PATTERN6:
                break;
        }
    }

    // 组织上级调整时赋值原公司，原组织编码
    public static void setOldNumberChgParent(DynamicObject orgDyn, String orgPatternNumber){
        switch (orgPatternNumber){
            // 组织形态为集团的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN1:
                break;
            // 组织形态为公司的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN2:
                break;
            // 组织形态为部门,事业部的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN4:
            case OldCompanyOrgConstants.ORG_PATTERN6:
                break;
        }
    }


    public static Map<String, String> getOldNumber(DynamicObject orgDyn, String orgChangeScene){

        LOGGER.info("OldCompanyOrgUtil&getOldNumber start");
        Map<String, String> result = new HashMap<>();
        result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, "");
        result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, "");
        result.put(OldCompanyOrgConstants.OLD_COMMON_NUMBER, "");
        // 判断是否为虚拟组织
        if (orgDyn.getBoolean(OldCompanyOrgConstants.IS_VIRTUAL)) {
            LOGGER.info("OldCompanyOrgUtil&getOldNumber 为虚拟组织");
            // 虚拟组织取对应上级行政组织的所属公司
            Object parentOrgId = orgDyn.get("parentorg.id");
            DynamicObject parentOrgDyn = BusinessDataServiceHelper.loadSingle(parentOrgId, OldCompanyOrgConstants.ADMIN_ORG_HR);
            if (parentOrgDyn != null) {
                result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, parentOrgDyn.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER));
                if (orgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER) == null || orgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER).length() == 0) {
                    result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, orgDyn.getString(OldCompanyOrgConstants.NUMBER));
                } else {
                    result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, orgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER));
                }
                result.put(OldCompanyOrgConstants.OLD_COMMON_NUMBER,
                        result.get(OldCompanyOrgConstants.OLD_COMPANY_NUMBER) + result.get(OldCompanyOrgConstants.OLD_ORG_NUMBER));
            }
            LOGGER.info("OldCompanyOrgUtil&getOldNumber 虚拟组织 result : " + result);
            return result;
        }

        // 非虚拟组织
        // 当前行政组织类型
        DynamicObject adminOrgType = ADMIN_ORG_TYPE_MAP.get(orgDyn.getLong("adminorgtype.id"));
        if (adminOrgType == null){
            return result;
        }
        // 获取组织形态
        String orgPatternNumber = adminOrgType.getString("orgpattern.number");
        LOGGER.info("OldCompanyOrgUtil&getOldNumber orgPatternNumber : " + orgPatternNumber);

        switch (orgChangeScene) {
            // 组织新设的数据
            case OldCompanyOrgConstants.ADD_NEW:
                return getOldNumberAddNew(orgDyn, orgPatternNumber);
            // 信息变更的数据
            case OldCompanyOrgConstants.CHG_INFO:
                return getOldNumberChgInfo(orgDyn);
            // 上级调整的数据
            case OldCompanyOrgConstants.CHG_PARENT:
                return getOldNumberChgParent(orgDyn);
            default:

        }
        return null;
    }

    // 组织新设时赋值原公司，原组织编码
    public static Map<String, String> getOldNumberAddNew(DynamicObject orgDyn, String orgPatternNumber){
        LOGGER.info("OldCompanyOrgUtil&getOldNumberAddNew start : " + orgPatternNumber);
        Map<String,String> result = new HashMap<>();
        // 新建组织: 原组织编码默认用行政组织编码
        result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, orgDyn.getString(OldCompanyOrgConstants.NUMBER));
        switch (orgPatternNumber){
            // 组织形态为集团的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN1:
                // 原公司编码 默认用吉利控股集团的原公司编码
                result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, GEELY_OLD_COMPANY_NUMBER);
                break;
            // 组织形态为公司的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN2:
                // 原公司编码 根据中间表和生成规则生成一个三位的原公司编码
                result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, getOldCompanyNumber());
                break;
            // 组织形态为部门,事业部的数据处理
            case OldCompanyOrgConstants.ORG_PATTERN4:
            case OldCompanyOrgConstants.ORG_PATTERN6:
                DynamicObject parentOrg = BusinessDataServiceHelper.loadSingle(orgDyn.get("parentorg.id"), OldCompanyOrgConstants.ADMIN_ORG_HR);
                result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, parentOrg.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER));
                break;
        }
        // 原公司组织编码
        result.put(OldCompanyOrgConstants.OLD_COMMON_NUMBER,
                orgDyn.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER) + orgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER));
        LOGGER.info("OldCompanyOrgUtil&getOldNumberAddNew result : " + result);
        return result;
    }

    // 组织信息变更时赋值原公司，原组织编码
    public static Map<String, String> getOldNumberChgInfo(DynamicObject newOrgDyn){
        LOGGER.info("OldCompanyOrgUtil&getOldNumberChgInfo start");
        // 初始化原公司，原组织编码
        Map<String,String> result = new HashMap<>();
        result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, newOrgDyn.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER));
        result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, newOrgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER));
        result.put(OldCompanyOrgConstants.OLD_COMMON_NUMBER, newOrgDyn.getString(OldCompanyOrgConstants.OLD_COMMON_NUMBER));

        DynamicObject oldOrgDyn = getOrgDyn(newOrgDyn.getString("number"), "parent.number,orgtype.id");
        // 判断行政组织类型是否变更
        DynamicObject newOrgTypeDyn = ADMIN_ORG_TYPE_MAP.get(newOrgDyn.getLong("adminorgtype.id"));
        DynamicObject oldOrgTypeNDyn = ADMIN_ORG_TYPE_MAP.get(oldOrgDyn.getLong("orgtype.id"));

        String oldOrgPatternNumber = oldOrgTypeNDyn.getString("orgpattern.number");
        String newOrgPatternNumber = newOrgTypeDyn.getString("orgpattern.number");
        // 行政组织类型未变更 或行政组织类型对应所属形态未变更
        if (newOrgTypeDyn.getString("number").equals(oldOrgTypeNDyn.getString("number"))
            || newOrgPatternNumber.equals(oldOrgPatternNumber)){
            return result;
        }

        // 旧的组织形态为部门/事业部 新的组织形态为公司
        if ((OldCompanyOrgConstants.ORG_PATTERN4.equals(oldOrgPatternNumber) || OldCompanyOrgConstants.ORG_PATTERN6.equals(oldOrgPatternNumber)
              && OldCompanyOrgConstants.ORG_PATTERN2.equals(newOrgPatternNumber))){
            result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, getOldCompanyNumber());
            result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, newOrgDyn.getString(OldCompanyOrgConstants.NUMBER));
            result.put(OldCompanyOrgConstants.OLD_COMMON_NUMBER,
                    result.get(OldCompanyOrgConstants.OLD_COMPANY_NUMBER) + result.get(OldCompanyOrgConstants.OLD_ORG_NUMBER));
        }
        LOGGER.info("OldCompanyOrgUtil&getOldNumberChgInfo result : " + result);
        return result;
    }

    // 组织上级调整时赋值原公司，原组织编码
    public static Map<String, String> getOldNumberChgParent(DynamicObject newOrgDyn){
        LOGGER.info("OldCompanyOrgUtil&getOldNumberChgParent start");
        // 初始化原公司，原组织编码
        Map<String,String> result = new HashMap<>();
        result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, newOrgDyn.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER));
        result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, newOrgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER));
        result.put(OldCompanyOrgConstants.OLD_COMMON_NUMBER, newOrgDyn.getString(OldCompanyOrgConstants.OLD_COMMON_NUMBER));

        DynamicObject oldOrgDyn = getOrgDyn(newOrgDyn.getString("number"), "parent.number,orgtype.id");
        // 判断上级行政组织是否变更
        String newParentOrgNumber = newOrgDyn.getString("parentorg.number");
        String oldParentOrgNumber = oldOrgDyn.getString("parent.number");

        // 如果上级行政组织未变更
        if (newParentOrgNumber.equals(oldParentOrgNumber)){
            return result;
        }

        // 判断行政组织类型是否变更
        DynamicObject newOrgTypeDyn = ADMIN_ORG_TYPE_MAP.get(newOrgDyn.getLong("adminorgtype.id"));
        DynamicObject oldOrgTypeNDyn = ADMIN_ORG_TYPE_MAP.get(oldOrgDyn.getLong("orgtype.id"));

        String oldOrgPatternNumber = oldOrgTypeNDyn.getString("orgpattern.number");
        String newOrgPatternNumber = newOrgTypeDyn.getString("orgpattern.number");
        // 行政组织类型未变更 或行政组织类型对应所属形态未变更 且上级调整了
        if (newOrgTypeDyn.getString("number").equals(oldOrgTypeNDyn.getString("number"))
                || newOrgPatternNumber.equals(oldOrgPatternNumber)){
            // 当前组织形态为公司
            if (OldCompanyOrgConstants.ORG_PATTERN2.equals(newOrgPatternNumber)){
                return result;
            }
            // 当前组织形态为部门/事业部
            if (OldCompanyOrgConstants.ORG_PATTERN4.equals(newOrgPatternNumber)
                    || OldCompanyOrgConstants.ORG_PATTERN6.equals(newOrgPatternNumber)){

                DynamicObject newParentOrgDyn = getOrgDyn(newParentOrgNumber, "dgdl_oldorgnum_ext,dgdl_oldcompan_ext,dgdl_common_ext");

                result.put(OldCompanyOrgConstants.OLD_COMPANY_NUMBER, newParentOrgDyn.getString(OldCompanyOrgConstants.OLD_COMPANY_NUMBER));
                result.put(OldCompanyOrgConstants.OLD_ORG_NUMBER, newParentOrgDyn.getString(OldCompanyOrgConstants.OLD_ORG_NUMBER));
                result.put(OldCompanyOrgConstants.OLD_COMMON_NUMBER, newParentOrgDyn.getString(OldCompanyOrgConstants.OLD_COMMON_NUMBER));
            }
        }
        LOGGER.info("OldCompanyOrgUtil&getOldNumberChgParent result : " + result);
        return result;
    }

    // 新增原公司编码时生成新的三位原公司编码
    private static String getOldCompanyNumber(){
        LOGGER.info("OldCompanyOrgUtil&getOldCompanyNumber start");
        DynamicObject[] DynArray = BusinessDataServiceHelper.load("dgdl_org_oldcompanynumber",
                "dgdl_oldcompanynumber",
                new QFilter[]{new QFilter("id", QCP.not_equals, 0)});
        Set<String> oldCompanyNumbers = Arrays.stream(DynArray).map(t -> t.getString("dgdl_oldcompanynumber")).collect(Collectors.toSet());

        for (String s : OLD_COMPANY_NUMBER_ALL) {
            if (!oldCompanyNumbers.contains(s)){
                DynamicObject dynamicObject = new DynamicObject(ORG_OLD_COMPANY_NUMBER_TYPE);
                dynamicObject.set("dgdl_oldcompanynumber", s);
                SaveServiceHelper.save(new DynamicObject[]{dynamicObject});
                LOGGER.info("OldCompanyOrgUtil&getOldCompanyNumber s : " + s);
                return s;
            }
        }
        return null;
    }

    private static DynamicObject getOrgDyn(String orgNumber, String selectProperties){
        DynamicObject orgDyn = BusinessDataServiceHelper.loadSingle(
                OldCompanyOrgConstants.ADMIN_ORG_HR,
                selectProperties,
                new QFilter[]{
                        new QFilter("number", QCP.equals, orgNumber),
                        new QFilter("iscurrentversion", QCP.equals, "1")
                });
        return orgDyn;
    }

}
