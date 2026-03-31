package tdkw.hr.odc.haos.common;

import com.google.common.collect.Sets;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;

import java.util.HashSet;
import java.util.Set;

public class StaffExtHelper {

    /**
     * 根据管理组织ID获取职级方案
     * @param orgId     组织体系管理组织ID
     * @return
     */
    public static Set<Long> queryJoblevelscmIdsByOrgId(long orgId) {
        if (orgId == 0L) {
            return Sets.newHashSet();
        }
        String sql = "select fdataid from t_hbjm_joblevelscm_U where fuseorgid = " + orgId + "";
        DataSet dataSet = DB.queryDataSet("StaffaddjoblevelFilter_getJoblevelscmIdsByAssignUseOrg", new DBRoute("hr"), sql);
        Set<Long> dataidSet = new HashSet<>();
        for (Row row : dataSet) {
            dataidSet.add(row.getLong("fdataid"));
        }
        return dataidSet;
    }

}
