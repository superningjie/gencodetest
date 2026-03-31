package dgdl.odc.homs.common;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hspm.common.constants.HRPIGenericConstants;
import kd.hrmp.hrpi.mservice.HRPIPersonGenericService;

import java.util.*;

/**
 * 组织帮助类
 */
public class HrOrgUtils {

    private final String algoKey = this.getClass().getName();

    /**
     * 获取 主任职经历 根据组织id
     * @param number
     * @return
     */
    public  List<DynamicObject> getPrimaryEmpInfoByOrgID(String number) {

        List<QFilter> personnelqfilter = new ArrayList<>();
        List<DynamicObject> EmpInfoList = new ArrayList<>();
        personnelqfilter.add(new QFilter("adminorg.number", QCP.equals, number));
        //是否生效
        personnelqfilter.add(new QFilter("businessstatus", QCP.equals, "1"));
        //任职状态 在岗
        personnelqfilter.add(new QFilter("isprimary", QCP.equals, "1"));
        //是否当前版本
        personnelqfilter.add(new QFilter("iscurrentversion", QCP.equals, true));
        //数据状态
        personnelqfilter.add(new QFilter("datastatus", QCP.equals, "1"));
        QFilter[] personnelqfilters = new QFilter[personnelqfilter.size()];
        personnelqfilter.toArray(personnelqfilters);
        //任职经历
        HRBaseServiceHelper helper = new HRBaseServiceHelper("hrpi_empposorgrel");
        DynamicObject[] empposorgrelObjs = helper.query("id,person,number,createtime", personnelqfilters, "createtime desc");
        if (empposorgrelObjs.length> 0 && empposorgrelObjs!=null) {
            Arrays.asList(empposorgrelObjs).forEach(
                    dynamicObject -> EmpInfoList.add(dynamicObject)
            );
        }
        return EmpInfoList;
    }

    /**
     * 通过组织id  查询获取该组织下所有对应任职经历人员 id
     *
     * @return
     */

    public List<Long> getPersonInfoByOrgID(long id) {

        List<QFilter> personnelqfilter = new ArrayList<>();
        List<Long> personIds = new ArrayList<>();
        personnelqfilter.add(new QFilter("adminorg.id", QCP.equals, id));
        personnelqfilter.add(new QFilter("businessstatus", QCP.equals, "1"));
        //任职状态 在岗
        personnelqfilter.add(new QFilter("posstatus.number", QCP.equals, "1020_S"));
        //主任职
        personnelqfilter.add(new QFilter("isprimary", QCP.equals, "1"));
        personnelqfilter.add(new QFilter("iscurrentversion", QCP.equals, "1"));
        personnelqfilter.add(new QFilter("datastatus", QCP.equals, "1"));
        QFilter[] personnelqfilters = new QFilter[personnelqfilter.size()];
        personnelqfilter.toArray(personnelqfilters);
        //任职经历
        DynamicObjectCollection dynamicObjectCollection = QueryServiceHelper.query(algoKey, "hrpi_empposorgrel",
                "id,person,createtime", personnelqfilters, "createtime desc");
        if (dynamicObjectCollection.size() > 0) {
            dynamicObjectCollection.forEach(
                    dynamicObject -> personIds.add(dynamicObject.getLong("id"))
            );
        }
        return personIds;
    }

    /**
     * 通过员工工号 查出员工任职经历信息
     *
     * @param numberList
     * @return
     */
    public Map<String, List<DynamicObject>> getEmpposorgreByNumber(List<String> numberList) {

        Map<String, List<DynamicObject>> empMap = new HashMap<>();
        List<QFilter> personnelqfilter = new ArrayList<>();
        personnelqfilter.add(new QFilter("person.number", QCP.in, numberList));
        personnelqfilter.add(new QFilter("businessstatus", QCP.equals, "1"));
        //任职状态 在岗
        personnelqfilter.add(new QFilter("posstatus.number", QCP.equals, "1020_S"));
        personnelqfilter.add(new QFilter("iscurrentversion", QCP.equals, "1"));
        personnelqfilter.add(new QFilter("datastatus", QCP.equals, "1"));
        QFilter[] personnelqfilters = new QFilter[personnelqfilter.size()];
        personnelqfilter.toArray(personnelqfilters);
        //任职经历
        DynamicObject[] load = BusinessDataServiceHelper.load("hrpi_empposorgrel",
                "id,person,position,postype,posstatus,startdate,adminorg,createtime", personnelqfilters, "createtime desc");
        if (load.length > 0 && load != null) {
            for (DynamicObject dynamicObject : load) {
                String number = dynamicObject.getDynamicObject("person").getString("number");
                if (empMap.containsKey(number)) {
                    List<DynamicObject> dynamicObjectList = empMap.get(number);
                    dynamicObjectList.add(dynamicObject);
                } else {
                    List<DynamicObject> list = new ArrayList<>();
                    list.add(dynamicObject);
                    empMap.put(number, list);
                }

            }
        }
        return empMap;
    }

    /**
     * 获取下级组织编码
     * @param orgID
     * @return
     */
    /**
     * 获取当前组织所有下游组织
     */
    public List<String> getJuniorOrg(String number, List<String> list){
        //查询当前组织下游
        DynamicObject[] orgObjs = this.getOrgObjByParentNumber(number);
        if (orgObjs!=null && orgObjs.length>0){
            for (DynamicObject downObj : orgObjs) {
                String number2 = downObj.getString("number");
                list.add(number2);
                //再判断该下级 是否还有下级组织
                DynamicObject[] orgJuniorObjs = this.getOrgObjByParentNumber(number2);
                if (orgJuniorObjs!=null && orgJuniorObjs.length>0){
                    this.getJuniorOrg(number2,list);
                }else {
                    continue;
                }
            }
        }
        return list;
    }

    /**
     * 根据父类编码获取下级组织
     * @param number
     * @return
     */
    public DynamicObject[] getOrgObjByParentNumber(String number){

        QFilter[] adminorglayerFilter = new QFilter("parentorg.number", QCP.equals,number)
                .and("iscurrentversion",QCP.equals,true).toArray();
        DynamicObject adminorgdetailDownObjs[] = BusinessDataServiceHelper.load("homs_adminorgdetail",
                "id,parentorg,adminorglayer,number,boid,establishmentdate", adminorglayerFilter);
        if (adminorgdetailDownObjs!=null && adminorgdetailDownObjs.length>0){
            return adminorgdetailDownObjs;
        }else {
            return null;
        }

    }

    /**
     * 组装参数调用人员通用微服务保存接口
     * @param obj
     * @return
     */
    public static Map<String, Object> addSaveBatch(DynamicObject obj) {
        List<Map<String, Object>> datas = new ArrayList<>();
        DynamicObjectCollection hisDyns = new DynamicObjectCollection();
        hisDyns.add(obj);
        Map<String, Object> entity = new HashMap<>();
        entity.put("hisDyns", hisDyns);
        datas.add(entity);
        Map<String, Object> paramMap = new HashMap<>(16);
        paramMap.put("data", datas);
        paramMap.put("caller", "initialize");
        paramMap.put("mustAllSuccess", true);
        Long eventId = ORM.create().genLongId("haos_otevent");
        paramMap.put(HRPIGenericConstants.PARAM_EVENTID, eventId);
        // 调用IHRPIPersonGenericService. saveBatch
        HRPIPersonGenericService hrpiPersonGenericService = new HRPIPersonGenericService();
        Map<String, Object> result = hrpiPersonGenericService.saveBatch(paramMap);
        return result;
    }


}
