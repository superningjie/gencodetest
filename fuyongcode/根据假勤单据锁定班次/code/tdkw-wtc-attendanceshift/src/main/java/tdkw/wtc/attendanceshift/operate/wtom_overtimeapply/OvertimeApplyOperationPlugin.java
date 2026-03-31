package tdkw.wtc.attendanceshift.operate.wtom_overtimeapply;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @description 加班申请操作插件
 * @author ljl
 * @date 2024/4/16 13:27
 **/
public class OvertimeApplyOperationPlugin extends AbstractOperationServicePlugIn implements Plugin {

    //提交
    private static final String SUBMIT="submit";
    //提交并生效
    private static final String SUBMITEFFECT="submiteffect";
    //撤销
    private static final String UNSUBMIT="unsubmit";
    //驳回至提交人
    private static final String WFREJECTTOSUBMIT="wfrejecttosubmit";
    //反审核
    private static final String UNAUDIT="unaudit";
    //审核不通过
    private static final String WFAUDITNOTPASS="wfauditnotpass";
    //审批通过
    private static final String AUDIT="audit";

    //人员组实体
    private static final String EMPGROUP = "hbss_empgroup";

    //标准工时员工编码
    private static final String STANDARD_WORKING_HOURS = "TW_ABS_0001";

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("parent");
        e.getFieldKeys().add("otapplytype");
        e.getFieldKeys().add("sdentry");
        e.getFieldKeys().add("otdutydate");
        e.getFieldKeys().add("scentry");
        e.getFieldKeys().add("otdate");
        e.getFieldKeys().add("personid");
        e.getFieldKeys().add("otstartdate");
        e.getFieldKeys().add("otenddate");
        e.getFieldKeys().add("attfile");
    }

    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        String operationKey = e.getOperationKey();
        DynamicObject[] dataEntities = e.getDataEntities();
        for (DynamicObject dynamicObject : dataEntities) {
            DynamicObject attfile = dynamicObject.getDynamicObject("attfile");
            long id = attfile.getLong("id");
            QFilter[] attfileBaseFilter = new QFilter("boid", QCP.equals,id)
                    .and("iscurrentversion",QCP.equals,true)
                    .and("datastatus",QCP.equals,true).toArray();
            DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("wtp_attfilebase", "empgroup.id,id,boid", attfileBaseFilter);
            long empgroupId = dynamicObject1.getLong("empgroup.id");
            QFilter[] empgroupFilter = new QFilter("id", QCP.equals,empgroupId)
                    .and("enable",QCP.equals,true)
                    .and("status",QCP.equals,"C").toArray();
            DynamicObject dynamicObjectEmpgroup = BusinessDataServiceHelper.loadSingle(EMPGROUP, "number", empgroupFilter);
            String number = dynamicObjectEmpgroup.getString("number");
            if (STANDARD_WORKING_HOURS.equals(number)){
                continue;
            }
            List<DynamicObject> personrosterCollection = new ArrayList<>(16);
            List<DynamicObject> addPersonrosterCollection = new ArrayList<>(16);
            long parentid = dynamicObject.getLong("parent");
            Integer isclock = null;
            List<String> status = new ArrayList<>();
            Collections.addAll(status,WFAUDITNOTPASS,UNSUBMIT,WFREJECTTOSUBMIT,UNAUDIT);
                queryData(dynamicObject,personrosterCollection,addPersonrosterCollection,operationKey,false);
                if (operationKey.equals(SUBMIT) || operationKey.equals(SUBMITEFFECT)){//提交-锁定计划班次和实际班次
                    isclock = 1;
                } else if (status.contains(operationKey)) {//审核不通过-解锁计划班次和实际班次
                    isclock = 0;
                }
                if (isclock != null) {
                    for (DynamicObject personroster : personrosterCollection) {
                        personroster.set("islock", isclock);
                    }
                    for (DynamicObject addPersonroster:addPersonrosterCollection) {
                        addPersonroster.set("islock", isclock);
                    }
                }
            if ((operationKey.equals(AUDIT)||status.contains(operationKey)||operationKey.equals(SUBMITEFFECT)) && parentid != 0) {//审核通过-锁定当前变更单的排班，解锁原单的排班
                DynamicObject parentData = BusinessDataServiceHelper.loadSingle(parentid, "wtom_overtimeapplybill");
                queryData(parentData,personrosterCollection,addPersonrosterCollection,operationKey,true);
            }
            SaveServiceHelper.save(personrosterCollection.toArray(new DynamicObject[0]));
            SaveServiceHelper.save(addPersonrosterCollection.toArray(new DynamicObject[0]));
        }
        super.beginOperationTransaction(e);
    }

    /**
     * 获取人员排班信息
     * @param dynamicObject     单据对象
     * @param personrosterCollection 修改的排班信息
     * @param addPersonrosterCollection 新增的排班信息
     * @param operationKey       操作标识
     */
    private void queryData(DynamicObject dynamicObject,List<DynamicObject> personrosterCollection,
                           List<DynamicObject> addPersonrosterCollection,String operationKey,Boolean flag){
        // 加班申请方式-值为2为时段；否则为时长
        String otapplytype = dynamicObject.getString("otapplytype");
        String entryName = "sdentry";
        String dateName = "otdutydate";
        if ("2".equals(otapplytype)){
            entryName = "scentry";
            dateName = "otdate";
        }
        DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection(entryName);
        for (DynamicObject entity : entryentity) {
            LocalDate start = entity.getDate("otstartdate").toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = entity.getDate("otenddate").toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            QFilter qFilter = new QFilter("attperson", QCP.equals,dynamicObject.get("personid.id"))
                    .and("rosterdate",QCP.large_equals,start)
                    .and("rosterdate",QCP.less_equals,end);
            DynamicObject[] personrosters = BusinessDataServiceHelper.load("wts_personroster",
                    "attperson,rosterdate,islock,ischange,rostertype,ismodify,orgindatetype,orgindateproperty,datetype,attfilebase", qFilter.toArray());
            List<DynamicObject> personrosterList = Arrays.asList(personrosters);
            Map<String, List<DynamicObject>> personrosterMap = personrosterList.stream()
                    .collect(Collectors.groupingBy(x -> x.getString("attperson") + x.getString("rosterdate")));
            personrosterMap.keySet().forEach(key -> {
                if (personrosterMap.get(key).size() < 2){
                    DynamicObject sourceObject = personrosterMap.get(key).get(0);
                    DynamicObject object = BusinessDataServiceHelper.newDynamicObject("wts_personroster");
                    object.set("attperson",sourceObject.get("attperson"));
                    object.set("islock",sourceObject.get("islock"));
                    object.set("rosterdate",sourceObject.get("rosterdate"));
                    object.set("ischange",sourceObject.get("ischange"));
                    object.set("iscurrentversion",1);
                    object.set("datastatus",1);
                    object.set("sourcevid",0);
                    object.set("shift",0);
                    object.set("ischange",0);
                    object.set("rostertype",sourceObject.getInt("rostertype") == 0 ? 1 : 0);
                    object.set("ismodify",sourceObject.get("ismodify"));
                    object.set("orgindatetype",sourceObject.get("orgindatetype"));
                    object.set("orgindateproperty",sourceObject.get("orgindateproperty"));
                    object.set("datetype",sourceObject.get("datetype"));
                    object.set("attfilebase",sourceObject.get("attfilebase"));
                    addPersonrosterCollection.add(object);
                }
            });
            if (flag){
                if (operationKey.equals(AUDIT)||operationKey.equals(SUBMITEFFECT)){//审核通过-锁定当前变更单的排班，解锁原单的排班
                    // 变更单id
                    List<Object> changeIds = personrosterCollection.stream().map(x -> x.get("id")).collect(Collectors.toList());
                    for (DynamicObject personroster:personrosterList) {
                        if (!changeIds.contains(personroster.get("id"))){
                            personroster.set("islock",0);
                        }
                    }
                }else {// 审核不通过-解锁变更单排班，锁定原单排班
                    // 原单id
                    List<Object> originalIds = personrosterList.stream().map(x -> x.get("id")).collect(Collectors.toList());
                    // 重复的数据
                    List<DynamicObject> repeatData = personrosterCollection.stream().filter(x -> originalIds.contains(x.get("id"))).collect(Collectors.toList());
                    personrosterCollection.removeAll(repeatData);
                    personrosterList.stream().forEach(x -> x.set("islock",1));
                }
            }
//            if (operationKey.equals(AUDIT)){
//                for (DynamicObject personroster:personrosterList) {
//                    personroster.set("islock",0);
//                }
//            }
            personrosterCollection.addAll(personrosterList);
        }
    }
}