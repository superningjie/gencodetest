package tdkw.hrmp.hrobs.formplugin.homepage.guide.task;

import kd.bos.algo.DataSet;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.DBServiceHelper;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * #143610 UAT环境新员工登录出现了操作指引，需要排查生产是否有同样的问题。-->需要将新用户登录的操作指引去除
 * http://ones.xxx.com/project/#/team/JbjqrWit/task/Ww14VUTMucro55Ja
 */
public class EmployeeGuideMarkTask extends AbstractTask {


    private final static Log logger = LogFactory.getLog(EmployeeGuideMarkTask.class);

    /**
     * 批量新增SQL
     */
    private static final String INSERT_SQL = "INSERT INTO t_bas_user_paras_config ( fid, fuserid, freceivemessage, fusenewportal, fcreatorid, fcreatetime, fmodifierid, fmodifytime, ftableallenable, ftableisgridstriped, ftablevertical, ftabledisplaymode, ftablerowhigh, ffirstnewportal, fappearancemode ) " +
            "VALUES (?, ?, '1', '0', 0, now(), 1, now(), '0', '0', '0', ' ', 0, '1', ' ')";


    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        logger.info("============EmployeeGuideMarkTask开始============");
        // 路由到系统库
        DBRoute sys = DBRoute.of("sys");

        // 查询所有没有进入过hr系统的用户
        String name = this.getClass().getName();
        String notFirstLogin = "/*dialect*/  select fid from t_sec_user where fid not in (select fuserid from t_bas_user_paras_config);";
        DataSet notFirstLoginDataSet = DB.queryDataSet(name, sys, notFirstLogin);
        DynamicObjectCollection notFirstLoginData = ORM.create().toPlainDynamicObjectCollection(notFirstLoginDataSet);

        // 创建新增列表
        List<Object[]> addList = notFirstLoginData.stream().map(dynamicObject -> new Object[]{DBServiceHelper.genGlobalLongId(), dynamicObject.getLong("fid")}).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            DB.executeBatch(sys, INSERT_SQL, addList);
        }
        logger.info("EmployeeGuideMarkTask addList {}", addList.size());

        // 更新状态为非第一次登录 update t_bas_user_paras_config set ffirstnewportal='1'
        DB.execute(sys, "/*dialect*/ update t_bas_user_paras_config set ffirstnewportal='1'");

        logger.info("============EmployeeGuideMarkTask结束============");
    }
}
