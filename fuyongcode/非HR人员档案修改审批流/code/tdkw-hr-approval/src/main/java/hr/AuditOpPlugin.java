package hr;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.workflow.engine.msg.info.MessageInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description：人员档案信息变更申请——审批通过
 */
public class AuditOpPlugin extends AbstractOperationServicePlugIn {

    private static final Log logger = LogFactory.getLog(AuditOpPlugin.class);

    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        DynamicObject[] entities = e.getDataEntities();

        if (null != entities) {

            // 创建集合存放需要发送消息的人事业务档案，key -> 人事业务档案id，value -> map<key,value>
            Map<Long, Map<String, String>> ermanFileIdMap = new HashMap<>();

            for (DynamicObject entity : entities) {

                entity = BusinessDataServiceHelper.loadSingle(entity.getLong("id"), "hspm_infoapproval");
                // 获取person
                DynamicObject person = entity.getDynamicObject("person");
                if (null != person) {
                    long personId = person.getLong("id");
                    // 根据 person 查询 职业信息基础页面
                    QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
                    qFilter.and("iscurrentversion", QCP.equals, "1");
                    qFilter.and("datastatus", QCP.equals, "1");
                    DynamicObject object = BusinessDataServiceHelper.loadSingle("hrpi_empentrel",
                            "id,person,iscurrentversion,datastatus,laborreltype", new QFilter[]{qFilter});
                    if (null != object) {
                        // 获取 用工关系类型
                        DynamicObject laborRelType = object.getDynamicObject("laborreltype");
                        // 用工关系类型编码
                        String laborRelTypeNumber = null != laborRelType ? laborRelType.getString("number") : null;
                        // 如果是实习生修改档案，根据业务需要发送消息
                       /* if ("1020_S".equals(laborRelTypeNumber)) {
                            List<String> list = new ArrayList<>();
                            list.add("1");
                            list.add("-3");

                            // 根据person 查询 教育经历基础页面
                            QFilter qFilter2 = new QFilter("person.id", QCP.equals, personId);
                            qFilter2.and("iscurrentversion", QCP.equals, "1");
                            qFilter2.and("datastatus", QCP.in, list);
                            DynamicObject[] load = BusinessDataServiceHelper.load("hrpi_pereduexp",
                                    "id,boid,person,iscurrentversion,datastatus", new QFilter[]{qFilter2});
                            if (null != load && load.length > 0) {
                                // 获取所有的boid
                                Set<Long> boIdSet = Arrays.stream(load).map(s -> s.getLong("boid")).collect(Collectors.toSet());

                                // 根据boid 查询 教育证件
                                QFilter qFilter3 = new QFilter("pereduexp.id", QCP.in, boIdSet);
                                qFilter3.and("person.id", QCP.equals, personId);
                                // 证件类型为毕业证
                                qFilter3.and("certtype.number", QCP.equals, "1010_S");
                                qFilter3.and("iscurrentversion", QCP.equals, "1");
                                qFilter3.and("datastatus", QCP.in, list);
                                DynamicObject[] perEduExpCerts = BusinessDataServiceHelper.load("hrpi_pereduexpcert",
                                        "id,boid,person,pereduexp,certtype,iscurrentversion,datastatus",
                                        new QFilter[]{qFilter3});
                                // 获取所有的boid
                                Set<Long> perEduExpCertsBoIdSet = Arrays.stream(perEduExpCerts).map(s -> s.getLong("boid")).collect(Collectors.toSet());

                                // 获取 单据体
                                DynamicObjectCollection entryEntity = entity.getDynamicObjectCollection("entryentity");
                                if (null != entryEntity && !entryEntity.isEmpty()) {
                                    for (DynamicObject entry : entryEntity) {
                                        // 获取数据id
                                        long dataId = entry.getLong("dataid");
                                        // 获取 字段显示名
                                        String displayName = entry.getString("displayname");
                                        if (perEduExpCertsBoIdSet.contains(dataId) && "教育证件".equals(displayName)) {
                                            // 获取 oldvalue
                                            String oldValue = entry.getString("oldvalue");
                                            logger.info("oldvalue: " + oldValue);

                                            // 获取 newvalue
                                            String newValue = entry.getString("newvalue");
                                            logger.info("newValue: " + newValue);

                                            if (!StringUtils.equals(oldValue, newValue) && "0".equals(oldValue) && !"0".equals(newValue)) {
                                                // 根据人员查询人事业务档案
                                                QFilter qFilter1 = new QFilter("person.id", QCP.equals, personId);
                                                qFilter1.and("iscurrentversion", QCP.equals, "1");
                                                qFilter1.and("datastatus", QCP.equals, "1");
                                                DynamicObject erManFile = BusinessDataServiceHelper.loadSingle("hspm_ermanfile",
                                                        "id,number,name,person,iscurrentversion,datastatus", new QFilter[]{qFilter1});
                                                if (null != erManFile) {
                                                    // key -> 工号， value -> 姓名
                                                    Map<String, String> numberNameMap = new HashMap<>();
                                                    numberNameMap.put(erManFile.getString("number"), erManFile.getString("name"));

                                                    ermanFileIdMap.put(erManFile.getLong("id"), numberNameMap);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }*/
                    }
                }
            }

            if (!ermanFileIdMap.isEmpty()) {

                for (Long ermanFileId : ermanFileIdMap.keySet()) {
                    List<Long> resultList = this.getSalaryRelationPosLaborRelationUsers(ermanFileId);

                    // 获取当前登录用户
//                    long currUserId = RequestContext.get().getCurrUserId();
//                    resultList.add(currUserId);

                    // 获取 工号、姓名
                    Map<String, String> map = ermanFileIdMap.get(ermanFileId);
                    if (null != map) {
                        String key = map.keySet().iterator().next();
                        String value = map.get(key);
                        // 推送消息
                        MessageInfo message = getMessageInfo(resultList, key, value);

                        // 推送消息
                        MessageCenterServiceHelper.sendMessage(message);
                    }
                }
            }
        }
    }

    private MessageInfo getMessageInfo(List<Long> resultList, String number, String name) {
        MessageInfo message = new MessageInfo();
        message.setType(MessageInfo.TYPE_MESSAGE);
        // 标题
        message.setTitle("【实习生转劳动用工】");
        // 接收用户
        message.setUserIds(resultList);
        // 发送人
        message.setSenderName("系统发送");

        // 消息内容
        String content = "实习生：" + name + "(" + number + ")" + "已上传毕业证，请及时处理。";
        message.setContent(content);

        message.setContentUrl(content);

        // 设置来源
        message.setSource("毕业证上传");

        Map<String, Object> map = new HashMap<>();
        map.put("number", number);
        message.setParams(map);

        return message;
    }

    /**
     * 获取员工所在公司的薪酬专员
     * 需求：查询员工所在【公司】->薪酬福利组【业务组】->岗位【业务组属性=“劳动关系”】的岗位下所有人员
     */
    public List<Long> getSalaryRelationPosLaborRelationUsers(Long ermanFileId) {
        // 人事业务档案 归属公司【HR行政组织】
        DynamicObject ermanFile = BusinessDataServiceHelper.loadSingle(ermanFileId, "hspm_ermanfile");
        DynamicObject empPosorgrel = this.getEmpposorgrelData(ermanFile.getLong("person.id"));
        DynamicObject company = empPosorgrel.getDynamicObject("company");
        //根据 HR行政组织 查询该公司下的薪酬福利组【业务组】
        QFilter filter = new QFilter("company.id", QCP.equals, company.getPkValue())
                .and(new QFilter("name", QCP.equals, "薪酬福利组"))
                .and(new QFilter("orgtype.number", QCP.equals, "1043_S"))
                .and(new QFilter("iscurrentversion", QCP.equals, "1"))
                .and(new QFilter("datastatus", QCP.equals, "1"));
        DynamicObject salaryDepart = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "id,name,number", filter.toArray());
        if (salaryDepart != null) {
            //根据 HR岗位 查询该部门下【业务组属性=劳动关系 】的岗位信息
            QFilter qFilter = new QFilter("kdcd_ywzsxx.number", QCP.equals, "TYN-00054")
                    .and("adminorg.id", QCP.equals, salaryDepart.getLong("id"))
                    .and("enable", QCP.equals, "1")
                    .and("iscurrentversion", QCP.equals, "1")
                    .and("status", QCP.equals, "C");
            DynamicObject[] positions = BusinessDataServiceHelper.load("hbpm_positionhr", "id", new QFilter[]{qFilter});
            if (positions.length > 0) {
                //从任职经历中查询以上岗位下对应的所有人员信息【包括兼职任职】
                Set<Long> hrPositionIds = Arrays.stream(positions).map(x -> x.getLong("id")).collect(Collectors.toSet());
                QFilter pfilter = new QFilter("position.id", QCP.in, hrPositionIds)
                        .and("iscurrentversion", QCP.equals, "1")
                        .and("datastatus", QCP.equals, "1");
                DynamicObject[] empposorgrel = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,person,adminorg,position", new QFilter[]{pfilter});
                if (empposorgrel.length > 0) {
                    return Arrays.stream(empposorgrel).map(x -> x.getLong("person.id")).sorted().collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * 根据人员id查询任职经历信息
     *
     * @param personId
     * @return
     */
    private DynamicObject getEmpposorgrelData(Long personId) {
        QFilter filter = new QFilter("person.id", QCP.equals, personId);
        //业务状态为生效中
        filter.and(new QFilter("businessstatus", QCP.equals, "1"));
        //主任职
        filter.and(new QFilter("isprimary", QCP.equals, "1"));
        //任职类型-全职任职
        filter.and(new QFilter("postype.number", QCP.equals, "1010_S"));
        //查询最新版本
        filter.and(new QFilter("iscurrentversion", QCP.equals, "1"));
        return BusinessDataServiceHelper.loadSingle("hrpi_empposorgrel", filter.toArray());
    }

}
