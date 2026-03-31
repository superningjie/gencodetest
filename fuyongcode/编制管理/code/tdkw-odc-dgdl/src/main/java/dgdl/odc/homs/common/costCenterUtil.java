package dgdl.odc.homs.common;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.drp.mdr.common.util.ObjectUtil;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author e-Liang.He
 * @Date 2023/11/3 14:00
 * @Version 1.0
 */
public class costCenterUtil {

    private static Log logger = LogFactory.getLog(costCenterUtil.class);

    /**
     * 个人成本中心赋值
     *
     * @param org       组织
     * @param costcenterOrg 成本中心
     * @param begindate 开始日期
     */
    public void updatePercostcenter(DynamicObject org, DynamicObject costcenterOrg, Date begindate) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date endDate=null;
        try {
          endDate = sdf.parse("2999-12-31");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        logger.info("开始日期为:" + begindate);
        String numner = org.getString("number");
        //通过部门查询 主任职经历
        List<DynamicObject> EmpInfoList = new HrOrgUtils().getPrimaryEmpInfoByOrgID(numner);
        logger.info("查询人员为EmpInfoList:" + EmpInfoList);

        Map<String,DynamicObject> EmpInfoMap=new HashMap<>();
        for (DynamicObject dynamicObject : EmpInfoList) {
            DynamicObject person = dynamicObject.getDynamicObject("person");
            if (person!=null){
                EmpInfoMap.put(person.getString("number"),dynamicObject);
            }
        }
        logger.info("查询人员为EmpInfoMap:" + EmpInfoMap);

        Date finalEndDate = endDate;

        //获取当前登录用户id
        long currUserId = RequestContext.get().getCurrUserId();
        DynamicObject userObj = BusinessDataServiceHelper.loadSingle(currUserId, "bos_user");
        //获取成本中心
        List<DynamicObject> personlist = EmpInfoList.stream()
                .filter(dynamicObject -> dynamicObject.getDynamicObject("person")!=null)
                .map(dynamicObject -> dynamicObject.getDynamicObject("person")).collect(Collectors.toList());
        List<String> numberList = personlist.stream().map(dynamicObject -> dynamicObject.getString("number")).collect(Collectors.toList());
        logger.info("查询人员为numberList:" + numberList);
        //查询人员成本中心
        HRBaseServiceHelper helper = new HRBaseServiceHelper("dgdl_hrpi_percostcenter");
        QFilter qFilter = new QFilter("person.number", QCP.in, numberList);
        qFilter.and(new QFilter("iscurrentversion", QCP.equals, true));
        DynamicObject[] percostObjs = helper.query("id,dgdl_startdate,dgdl_enddate,dgdl_orgcostcenter," +
                        "dgdl_perorgcostcenter,dgdl_payrolldept,dgdl_issubstitute,person,dgdl_empposrel,modifier,modifytime",
                qFilter.toArray(), "dgdl_startdate asc");

        Map<String,List<DynamicObject>> percostMap=new HashMap<>();
        if (percostObjs != null && percostObjs.length > 0) {
            for (DynamicObject percostObj : percostObjs) {
                DynamicObject person = percostObj.getDynamicObject("person");
                String number = person.getString("number");
                if (percostMap.containsKey(number)){
                    List<DynamicObject> list = percostMap.get(number);
                    list.add(percostObj);
                    percostMap.put(number,list);
                }else {
                    List<DynamicObject> list=new ArrayList<>();
                    list.add(percostObj);
                    percostMap.put(number,list);
                }
            }
        }
        logger.info("查询人员为percostMap:" + percostMap);

        if (!percostMap.isEmpty()){
            logger.info("修改人员成本中心数据开始");
            percostMap.forEach((number, dynamicObjectList) -> {
                //获取任职经历
                DynamicObject EmpObject = EmpInfoMap.get(number);
                DynamicObject person = EmpObject.getDynamicObject("person");

                //获取最晚开始日期对象
                DynamicObject maxStartdateobj = dynamicObjectList.stream().max(Comparator.comparing(
                        dynamicObject1 -> dynamicObject1.getDate("dgdl_startdate"))).get();
                //获取最早开始日期对象
                DynamicObject minStartdateobj = dynamicObjectList.stream().min(Comparator.comparing(
                        dynamicObject1 -> dynamicObject1.getDate("dgdl_startdate"))).get();

                //最早开始日期
                Date minStartdate = minStartdateobj.getDate("dgdl_startdate");
                Date minEnddate = minStartdateobj.getDate("dgdl_enddate");
                //最晚开始日期
                Date maxStartdate = maxStartdateobj.getDate("dgdl_startdate");
                Date maxEnddate = maxStartdateobj.getDate("dgdl_enddate");

                //若变更后组织上的成本中心开始时间早于当前所有的人员成本中心最早开始时间，则将当前的人员成本中心结束日期结束更新以最早的开始日期 并生成一条最早的开始日期
                if (begindate.compareTo(minStartdate) <0) {
                    //获取最小开始日期的成本中心
                    //结束日期为最小开始日期的前一天
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTime(minStartdate);
                    //拿到前一天
                    calendar.add(Calendar.DAY_OF_MONTH,-1);
                    Date lastDate = calendar.getTime();
                    //新生成一条个人成本中心
                    DynamicObject obj = helper.generateEmptyDynamicObject();
                    obj.set("person", person);//人员
                    obj.set("dgdl_startdate", begindate);//开始日期
                    obj.set("dgdl_enddate", lastDate);//结束日期
                    obj.set("dgdl_orgcostcenter", costcenterOrg);//所属组织成本中心（部门）
                    //obj.set("dgdl_comcostcenter", costcenterOrg.getDynamicObject("dgdl_company"));//所属组织成本中心（公司）
                    obj.set("dgdl_perorgcostcenter", null);//个人成本中心 (部门)
                    //obj.set("dgdl_percomcostcenter", costcenterOrg.get("dgdl_percomcostcenter"));//个人成本中心 （公司）
                    obj.set("dgdl_payrolldept", costcenterOrg.getDynamicObject("dgdl_company"));//发薪单位
                    obj.set("dgdl_issubstitute","N");//是否代发
                    obj.set("dgdl_empposrel", EmpObject);//员工任职
                    this.savePercostcenter(obj);
                    //若变更后组织上的成本中心的开始时间等于所有人员成本中心时间范围，则将当前接近人员成本中心进行结束时间进行更新，开始时间保持不变。生成一条当前成本中心，插入范围里
                } else if (begindate.compareTo(minStartdate) >=0 && begindate.compareTo(maxStartdate)<=0) {
                    //判断是否存在等于成本中心开始日期的数据
                    List<DynamicObject> dgdlStartdateList = Arrays.asList(percostObjs).stream().filter(dynamicObject1 -> begindate.equals(dynamicObject1.getDate("dgdl_startdate")))
                            .collect(Collectors.toList());
                    if (!ObjectUtil.isEmpty(dgdlStartdateList)){
                        DynamicObject dynamicObject2 = dgdlStartdateList.get(0);
                        //修改成本中心
                        dynamicObject2.set("dgdl_orgcostcenter",costcenterOrg);
                        dynamicObject2.set("modifier",userObj);
                        dynamicObject2.set("modifytime",new Date());
                        helper.update(new DynamicObject[]{dynamicObject2});
                    }else {
                        //获取最接近开始日期的 成本中心
                        DynamicObject approObj = Arrays.asList(percostObjs).stream().filter(dynamicObject1 -> begindate.compareTo(dynamicObject1.getDate("dgdl_startdate")) < 0).findFirst().get();
                        //结束日期为最近开始日期的前一天
                        Date dgdlStartdate = approObj.getDate("dgdl_startdate");
                        Calendar calendar=Calendar.getInstance();
                        calendar.setTime(dgdlStartdate);
                        //拿到前一天
                        calendar.add(Calendar.DAY_OF_MONTH,-1);
                        Date lastDate = calendar.getTime();
                        //新生成一条个人成本中心
                        DynamicObject obj = helper.generateEmptyDynamicObject();
                        obj.set("person", person);//人员
                        obj.set("dgdl_startdate", begindate);//开始日期
                        obj.set("dgdl_enddate", lastDate);//结束日期
                        obj.set("dgdl_orgcostcenter", costcenterOrg);//所属组织成本中心（部门）

                        //成本中心公司
                        DynamicObject dgdlPayrolldcompany = costcenterOrg.getDynamicObject("dgdl_company");
                        //个人成本中心 (部门)
                        DynamicObject dgdl_perorgcostcenter = approObj.getDynamicObject("dgdl_perorgcostcenter");
                        //是否代发
                        String dgdlIssubstitute = approObj.getString("dgdl_issubstitute");
                        //发薪单位
                        DynamicObject dgdlPayrolldept = approObj.getDynamicObject("dgdl_payrolldept");

                        //个人成本中心 (部门)
                        obj.set("dgdl_perorgcostcenter", dgdl_perorgcostcenter);
                        if ("Y".equals(dgdlIssubstitute)){
                            obj.set("dgdl_payrolldept", dgdlPayrolldept);//发薪单位
                            if (dgdlPayrolldcompany!=null && dgdlPayrolldept!=null){
                                if (!dgdlPayrolldcompany.getString("number").equals(dgdlPayrolldept.getString("number"))){
                                    obj.set("dgdl_issubstitute","Y");//是否代发
                                }
                                obj.set("dgdl_issubstitute","Y");//是否代发
                            }else {
                                obj.set("dgdl_issubstitute","N");//是否代发
                            }
                        }else {
                            obj.set("dgdl_payrolldept", dgdlPayrolldcompany);//发薪单位
                            obj.set("dgdl_issubstitute","N");//是否代发
                        }
                        obj.set("dgdl_empposrel", EmpObject);//员工任职
                        this.savePercostcenter(obj);
                    }

                    //若变更后组织上的成本中心的开始时间大于所有人员成本中心时间范围，则将当前接近人员成本中心进行结束时间进行更新，开始时间保持不变。并生成一条最早的开始日期
                }else if(begindate.compareTo(maxStartdate)>0){
                    //获取最大开始日期的成本中心
                    //取成本中心开始日期的前一天
                    Calendar calendar2=Calendar.getInstance();
                    calendar2.setTime(begindate);
                    //拿到前一天
                    calendar2.add(Calendar.DAY_OF_MONTH,-1);
                    Date lastDate2 = calendar2.getTime();
                    //新生成一条个人成本中心
                    DynamicObject obj = helper.generateEmptyDynamicObject();
                    obj.set("person", person);//人员
                    obj.set("dgdl_startdate", begindate);//开始日期
                    obj.set("dgdl_enddate", finalEndDate);//结束日期
                    obj.set("dgdl_orgcostcenter", costcenterOrg);//所属组织成本中心（部门）
                    //obj.set("dgdl_comcostcenter", costcenterOrg.getDynamicObject("dgdl_company"));//所属组织成本中心（公司）
                    obj.set("dgdl_perorgcostcenter", maxStartdateobj.getDynamicObject("dgdl_perorgcostcenter"));//个人成本中心 (部门)
                    //obj.set("dgdl_percomcostcenter", costcenterOrg.get("dgdl_percomcostcenter"));//个人成本中心 （公司）

                    //成本中心公司
                    DynamicObject dgdlPayrolldcompany = costcenterOrg.getDynamicObject("dgdl_company");
                    //个人成本中心 (部门)
                    DynamicObject dgdl_perorgcostcenter = maxStartdateobj.getDynamicObject("dgdl_perorgcostcenter");
                    DynamicObject dgdlPayrolldept = maxStartdateobj.getDynamicObject("dgdl_payrolldept");
                    //是否代发
                    String dgdlIssubstitute = maxStartdateobj.getString("dgdl_issubstitute");

                    if ("Y".equals(dgdlIssubstitute)){
                        obj.set("dgdl_payrolldept", dgdlPayrolldept);//发薪单位
                        if (dgdlPayrolldcompany!=null && dgdlPayrolldept!=null){
                            if (!dgdlPayrolldcompany.getString("number").equals(dgdlPayrolldept.getString("number"))){
                                obj.set("dgdl_issubstitute","Y");//是否代发
                            }
                            obj.set("dgdl_issubstitute","Y");//是否代发
                        }else {
                            obj.set("dgdl_issubstitute","N");//是否代发
                        }
                    }else {
                        obj.set("dgdl_payrolldept", dgdlPayrolldcompany);//发薪单位
                        obj.set("dgdl_issubstitute","N");//是否代发
                    }
                    obj.set("dgdl_empposrel", EmpObject);//员工任职
                    this.savePercostcenter(obj);
                    //修改日期
                    maxStartdateobj.set("dgdl_enddate",lastDate2);
                    maxStartdateobj.set("modifier",userObj);
                    maxStartdateobj.set("modifytime",new Date());
                    helper.update(new DynamicObject[]{maxStartdateobj});
                }
            });
            logger.info("修改人员成本中心数据结束");
        }

    }


    /**
     *
     * @param costcenterobj 人员成本中心
     */
    private void savePercostcenter(DynamicObject costcenterobj) {
        Long id = ORM.create().genLongId("dgdl_hrpi_percostcenter");
        costcenterobj.set("id", id);
        //获取当前登录用户id
        long currUserId = RequestContext.get().getCurrUserId();
        DynamicObject userObj = BusinessDataServiceHelper.loadSingle(currUserId, "bos_user");
        costcenterobj.set("creator", userObj);
        costcenterobj.set("modifier", userObj);
        costcenterobj.set("createtime", new Date());
        costcenterobj.set("modifytime", new Date());
        costcenterobj.set("datastatus", "1");
        costcenterobj.set("initstatus", "2");
        Map<String, Object> result = HrOrgUtils.addSaveBatch(costcenterobj);
        logger.info("result:" + result);
    }

}
