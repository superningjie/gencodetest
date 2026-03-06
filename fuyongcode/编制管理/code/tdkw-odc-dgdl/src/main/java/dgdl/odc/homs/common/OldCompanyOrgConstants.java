package dgdl.odc.homs.common;

/**
 * @author: ws
 * @date:2024/8/19 14:21
 * @description:
 */
public interface OldCompanyOrgConstants {

    // 行政组织元数据标识
    String ADMIN_ORG_HR = "haos_adminorghr";
    //行政组织编码
    String NUMBER = "number";
    // 变动场景
    String CHANGE_SCENE = "changescene";
    //所属公司
    String BE_LONG_COMPANY = "belongcompany";
    //原组织编码
    String OLD_ORG_NUMBER = "dgdl_oldorgnum_ext";
    //原公司编码
    String OLD_COMPANY_NUMBER = "dgdl_oldcompan_ext";
    // 原公司组织编码
    String OLD_COMMON_NUMBER = "dgdl_common_ext";
    //行政组织类型
    String ADMIN_ORG_TYPE = "adminorgtype";
    //获取上级组织
    String PARENT_ORG = "parentorg";
    // 是否虚拟组织
    String IS_VIRTUAL = "dgdl_isvirtual_ext";

    // 行政组织变动场景编码 : 组织新设
    String ADD_NEW = "1010_S";
    // 行政组织变动场景编码 : 上级调整
    String CHG_PARENT = "1020_S";
    // 行政组织变动场景编码 : 信息变更
    String CHG_INFO = "1030_S";
    // 行政组织变动场景编码 : 组织停用
    String ORG_DISABLE = "1040_S";
    // 行政组织变动场景编码 : 组织修订
    String ORG_MODIFY = "CS_1110_SY01";
    // 组织形态为集团的编码
    String ORG_PATTERN1 = "Orgform01";
    // 组织形态为公司的编码
    String ORG_PATTERN2 = "Orgform02";
    // 组织形态为部门的编码
    String ORG_PATTERN4 = "Orgform04";
    // 组织形态为事业部的编码
    String ORG_PATTERN6 = "Orgform06";

}
