package tdkw.hrmp.hrobs.formplugin;


import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DBServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.hr.hbp.common.util.HRDateTimeUtils;
import kd.hrmp.hrpi.mservice.HRPIPersonService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * @Description  定时任务-识别用户组别 经理-领导-员工
 * @author xxx
 * @Date 2023/5/10
 */
public class EmployeePositionTask  extends AbstractTask {
    private static final Log logger = LogFactory.getLog(EmployeePositionTask.class);
    private static final  String  targetMeta = "tdkw_employeeposition"; //分组的元数据
    public List<Long> pids = new ArrayList<>();

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        logger.info("定时任务-识别用户组别，开始" );
        //事务带回滚
        try (TXHandle h = TX.required("push")) {
            try {
                QFilter s = new QFilter("number",QCP.equals,"");
                DynamicObjectCollection users = QueryServiceHelper.query("bos_user", "id,name", null);
                List<Long> ids = new ArrayList<>();
                DeleteServiceHelper.delete(targetMeta,null);
                for (DynamicObject user : users) {
                    Map<String, Object> personModel = new HRPIPersonService().getPersonModelIdByUserId(user.getLong("id"));
                    if(personModel.containsKey("data")){
                        Map<String, Long>  personMap = (Map<String, Long>) personModel.get("data");
                        Long personId = personMap.get("person");
                        DynamicObject employee = BusinessDataServiceHelper.newDynamicObject(targetMeta);
                        employee.set("tdkw_employee",user.getLong("id"));
                        employee.set("status","C");
                        employee.set("enable","1");
                        employee.set("tdkw_person",personId);
                        employee.set("tdkw_type","2");//用户组：默认为员工

                        ORM orm = ORM.create();
                        long id = orm.genLongId(targetMeta);
                        employee.set("id",id);
                        DynamicObjectCollection treeDoc = employee.getDynamicObjectCollection("tdkw_treeentryentity");

                        setEmploymentExperience( personId, treeDoc,false);
                        for (DynamicObject item : treeDoc) {
                            Object subordinate = item.get("tdkw_subordinate");
                            if(subordinate!=null){
                                employee.set("tdkw_type","3");//1-经理人、3-领导
                                break;
                            }
                        }
                        OperationResult result = OperationServiceHelper.executeOperate("save", targetMeta, new DynamicObject[] { employee }, OperateOption.create());
                    }

                }
            } catch (Exception e) {
                logger.info("定时任务-识别用户组别，出现异常开始回滚" );
                h.markRollback();
            }
        }
    }


    /**
     * 获取任职经历,全职和兼职
     * @param personId
     */
    public void setEmploymentExperience(Long personId, DynamicObjectCollection treeDoc,boolean isFirst){

        DynamicObject[] empposorgrels = getEmpposorgrel(personId);
        for (DynamicObject empposorgrel : empposorgrels) {
            DynamicObject treeParent = treeDoc.addNew();
            DynamicObject position = empposorgrel.getDynamicObject("position");
            DynamicObject postype = empposorgrel.getDynamicObject("postype");
            if(position!=null){
                long positionId = position.getLong("id");
                treeParent.set("tdkw_postype",postype);
                treeParent.set("tdkw_position", positionId);
                Long id = DBServiceHelper.genLongIds("tdkw_treeentryentity", 1)[0];
                treeParent.set("id",id);

                if(isFirst){
                    treeParent.set("tdkw_subordinate", personId);
                }
                if(pids.size()>0){
                    treeParent.set("pid", pids.get(pids.size() - 1));
                }

                DynamicObject[] belowPositions = getBelowPosition(positionId);
                if(belowPositions!=null){
                    pids.add(id);
                }else{
                    pids.remove(pids.get(pids.size() - 1));
                }
                for (DynamicObject belowPosition : belowPositions) {
                    long id1 = belowPosition.getLong("id");
                    DynamicObject[] belowPersons = getBelowPerson( id1 );
                    for (DynamicObject belowPerson : belowPersons) {
                        long person = belowPerson.getLong("person");
                        setEmploymentExperience( person,  treeDoc,true);
                    }
                }
            }
        }

    }


    /**
     * 根据 HR人员信息 查找工作经历
     * @param personId HR人员信息id
     * @return  员工现在的任职
     */
    public DynamicObject[]  getEmpposorgrel(Long personId){
        QFilter person = new QFilter("person", "=", personId);
        QFilter businessstatus = new QFilter("businessstatus", "=", "1");//业务状态：1-生效中
        QFilter iscurrentversion = new QFilter("iscurrentversion", "=", "1");
        QFilter startdate = new QFilter("startdate", "<=", HRDateTimeUtils.truncateDate(new Date()));
        //  QFilter enddate = new QFilter("enddate", ">=", HRDateTimeUtils.truncateDate(new Date()));
        String properties = "postype,position,id";
        DynamicObject[] empposorgrels = BusinessDataServiceHelper.load("hrpi_empposorgrel", properties, new QFilter[]{person, businessstatus, iscurrentversion, startdate});
        return  empposorgrels;
    }

    /**
     * 根据岗位id查找下级岗位
     * @param positionId  岗位id
     * @return  返回下级岗位
     */
    public DynamicObject[] getBelowPosition(long positionId){
        QFilter parentFilter = new QFilter("parent", "in", positionId);//上级岗位
        QFilter enableFilter = new QFilter("enable", "=", "1");
        QFilter excludeStand = new QFilter("createmode", "!=", "3");
        QFilter datastatusFilter = new QFilter("datastatus", "in", new String[]{"1", "2"});
        QFilter iscurrentVersionFilter = new QFilter("iscurrentversion", "=", "0");
        QFilter bsedFilter = new QFilter("bsed", "<=", HRDateTimeUtils.truncateDate(new Date()));
        QFilter bsledFilter = new QFilter("bsled", ">=", HRDateTimeUtils.truncateDate(new Date()));
        QFilter initStatusFilter = new QFilter("initstatus", "not in", new String[]{"0", "1"});
        String field = "adminorg,id, name, number,parent,positionclassify,positiontype,highjoblevel,lowjoblevel,highjobgrade,lowjobgrade,enable,status,isleader,deputytype,group,job,positiontype.ismanagetype,establishmentdate";
        DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("hbpm_positionhr",field, new QFilter[]{parentFilter, enableFilter, excludeStand, datastatusFilter, iscurrentVersionFilter, bsedFilter, bsledFilter, initStatusFilter});
        return dynamicObjects;
    }

    /**
     * 根据岗位id 查找任职人员
     * @param positionId 岗位id
     * @return  任职人员
     */
    public DynamicObject[] getBelowPerson(long positionId){
        QFilter person = new QFilter("position", "=", positionId);
        QFilter businessstatus = new QFilter("businessstatus", "=", "1");//业务状态：1-生效中
        QFilter iscurrentversion = new QFilter("iscurrentversion", "=", "1");
        QFilter startdate = new QFilter("startdate", "<=", HRDateTimeUtils.truncateDate(new Date()));
        //  QFilter enddate = new QFilter("enddate", ">=", HRDateTimeUtils.truncateDate(new Date()));
        String properties = "postype,position,id,person";
        DynamicObject[] empposorgrels = BusinessDataServiceHelper.load("hrpi_empposorgrel", properties, new QFilter[]{person, businessstatus, iscurrentversion, startdate});
        return  empposorgrels;

    }
}
