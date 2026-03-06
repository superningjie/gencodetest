package dgdl.odc.homs.util;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.SqlBuilder;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

/**
 * 原公司原组织编码重复性校验工具
 */
public class OldCompanyAndDeptNumValidUtil {

    /**
     * 校验行政组织表中是否存在重复的原公司原组织编码
     * @param orgnumber 行政组织编码
     * @param companyNumber 原公司编码
     * @param deptNumber 原组织编码
     * @return true-校验通过 false-校验不通过
     */
    public static Boolean validAdminorg(String orgnumber, String companyNumber, String deptNumber) {
        //校验原公司，组织编码唯一性
        QFilter[] filters = new QFilter("dgdl_oldcompan_ext", QCP.equals, companyNumber)
                .and("dgdl_oldorgnum_ext", QCP.equals, deptNumber)
                .and("number", QCP.not_equals, orgnumber)
                .and("number", QCP.not_equals, " ")
                .and("number", QCP.not_equals, "")
                .and(QFilter.isNotNull("number"))
                .and("datastatus", QCP.equals, "1")
                .and("iscurrentversion", QCP.equals, true).toArray();
        //查询是否存在原公司，组织编码唯一性
        DynamicObject[] homsAdminorgdetail = BusinessDataServiceHelper.load("homs_adminorgdetail", "id,dgdl_oldcompan_ext,number,name,parentorg,adminorgtype", filters);
        if (homsAdminorgdetail != null && homsAdminorgdetail.length > 0) {
            return false;
        }
        return true;
    }

    /**
     * 校验审批中组织调整申请单据是否存在重复的原公司原组织编码
     * @param orgnumber 行政组织编码
     * @param companyNumber 原公司编码
     * @param deptNumber 原组织编码
     * @return 不存在重复时，返回空；重复时，返回单号
     */
    public static String validChgBill(String orgnumber, String companyNumber, String deptNumber) {
        // 查所有未审批完成的组织调整申请单
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("select bill.fbillno,entry.fnumber,entry.fk_dgdl_oldcompany_ext,entry.fk_dgdl_oldorgnumber_ext " +
                "from t_homs_orgchgbillentry entry left join t_homs_orgchgbill bill on entry.fid = bill.fid " +
                "where bill.fbillstatus not in ('C','E','F') and fchangesceneid != 1040 " +
                "and entry.fk_dgdl_oldcompany_ext = ? and entry.fk_dgdl_oldorgnumber_ext = ? and entry.fnumber != ?", companyNumber, deptNumber, orgnumber);
        DataSet dataSet = DB.queryDataSet("OldCompanyAndDeptNumValidUtil", DBRoute.of("hmp"), sqlBuilder);
        //查询是否存在原公司，组织编码唯一性
        if (dataSet != null) {
            for (Row row : dataSet) {
                return row.getString("fbillno");
            }
        }
        return null;
    }

    public static String validAll(String orgnumber, String companyNumber, String deptNumber) {
        Boolean validResult = OldCompanyAndDeptNumValidUtil.validAdminorg(orgnumber, companyNumber, deptNumber);
        if (!validResult) {
            return "原公司编码+原组织编码(" + companyNumber + "-" + deptNumber + ")组合字段与现有组织重复！";
        } else {
            String billno = validChgBill(orgnumber, companyNumber, deptNumber);
            if (StringUtils.isNotBlank(billno)) {
                return "原公司编码+原组织编码(" + companyNumber + "-" + deptNumber + ")组合字段与申请单(" +  billno + ")中组织重复！";
            }
        }
        return null;
    }
}
