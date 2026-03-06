package tdkw.esc.recruit.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 招聘需求工具类
 *
 * @author xysusj
 */
public class RecruitDemandUtil {

    private static final Log logger = LogFactory.getLog(RecruitDemandUtil.class);

    /**
     * 调用北森接口、创建招聘需求
     *
     * @param billNo 单据编号
     * @author xysusj
     * @date 17:31 2023/6/25
     **/
    public static void createRequirement(String billNo) {
        try {
            if (StringUtils.isBlank(billNo)) {
                return;
            }
            String fields = "creator,tdkw_created_dept,createtime,tdkw_rec_apply_dept,tdkw_rec_work_address,tdkw_rec_apply_company.name," +
                    "tdkw_rec_recruit_num,tdkw_rec_arrive_date,tdkw_rec_sex,tdkw_rec_major,tdkw_rec_job_duty,tdkw_rec_job_require," +
                    "tdkw_rec_rpthav,tdkw_rec_type,tdkw_rec_deadline,tdkw_rec_pos_seq_code,tdkw_rec_edu_req,tdkw_rec_experience," +
                    "tdkw_rec_max_salary,tdkw_rec_min_salary,tdkw_rec_source,tdkw_rec_reason,tdkw_rec_out_posi_name,tdkw_rec_in_posi_name," +
                    "tdkw_rec_report_to_person,tdkw_conprocess,tdkw_rec_apply_hr_pk,tdkw_rec_job_duty_text,tdkw_rec_job_req_text,tdkw_internation";
            DynamicObject recruitDemObj = BusinessDataServiceHelper.loadSingle("tdkw_rec_apply_bill", fields, new QFilter[]{new QFilter("billno", QCP.equals, billNo)});
//            RecruitDemandDto dto = new RecruitDemandDto();
            Map<String, Object> paramMap = new HashMap<>();
            if (recruitDemObj == null) {
                throw new KDBizException("调用北森招聘需求失败:单据信息为空！(" + billNo + ")");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (Objects.nonNull(recruitDemObj.getDynamicObject("creator"))) {
                paramMap.put("createdBy",recruitDemObj.getDynamicObject("creator").getString("name"));
                paramMap.put("employeeNo",recruitDemObj.getDynamicObject("creator").getString("number"));
            }
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_created_dept"))) {
                paramMap.put("createdDept",recruitDemObj.getDynamicObject("tdkw_created_dept").getString("name"));
            }
            // 设置时间为 当天的23:59:59
            Date arrivalTime = recruitDemObj.getDate("tdkw_rec_arrive_date");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
            String arrivalTimeStr = formatter.format(arrivalTime);
            paramMap.put("arrivalTime",arrivalTimeStr);
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_rec_report_to_person"))) {
                paramMap.put("reportObject",recruitDemObj.getDynamicObject("tdkw_rec_report_to_person").getString("name"));
            }
            String recruitSource = getDynamicObjectCollectionName(recruitDemObj.getDynamicObjectCollection("tdkw_rec_source"));
            paramMap.put("recruitSource",recruitSource);
            String recruitReason = getDynamicObjectCollectionName(recruitDemObj.getDynamicObjectCollection("tdkw_rec_reason"));
            paramMap.put("recruitReason",recruitReason);
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_rec_apply_company"))) {
                paramMap.put("applyCompany",recruitDemObj.getDynamicObject("tdkw_rec_apply_company").getString("name"));
            }
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_rec_apply_dept"))) {
                paramMap.put("applyDept",recruitDemObj.getDynamicObject("tdkw_rec_apply_dept").getString("name"));
//                paramMap.put("applyDept",recruitDemObj.getDynamicObject("tdkw_rec_apply_dept").getString(""));
                logger.info("tdkw_rec_apply_dept : {}",recruitDemObj.getDynamicObject("tdkw_rec_apply_dept"));
                paramMap.put("pkDept",String.valueOf(recruitDemObj.getDynamicObject("tdkw_rec_apply_dept").getString("tdkw_pkid")));
            }
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_rec_type"))) {
                paramMap.put("recruitType",recruitDemObj.getDynamicObject("tdkw_rec_type").getString("name"));
            }
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_rec_edu_req"))) {
                paramMap.put("eduReq",recruitDemObj.getDynamicObject("tdkw_rec_edu_req").getString("name"));
            }
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_rec_experience"))) {
                paramMap.put("experience",recruitDemObj.getDynamicObject("tdkw_rec_experience").getString("name"));
            }
            if (Objects.nonNull(recruitDemObj.getDynamicObject("tdkw_rec_in_posi_name"))) {
                DynamicObject inPostName = recruitDemObj.getDynamicObject("tdkw_rec_in_posi_name");
                paramMap.put("inName",inPostName.getString("name"));
                //岗位标签
                String jobTag = getDynamicObjectCollectionName(inPostName.getDynamicObjectCollection("tdkw_postiontag"));
                paramMap.put("jobTag",jobTag);
                //职位子序列
                paramMap.put("postSubsequence", inPostName.getDynamicObject("tdkw_jobfamilyhr").getString("name"));
            }
            paramMap.put("createDate",sdf.format(recruitDemObj.getDate("createtime")));
            paramMap.put("deadline",sdf.format(recruitDemObj.getDate("tdkw_rec_deadline")));
            paramMap.put("major",recruitDemObj.getString("tdkw_rec_major"));
            paramMap.put("maxSalary",recruitDemObj.getBigDecimal("tdkw_rec_max_salary").setScale(2, RoundingMode.HALF_UP));
            paramMap.put("minSalary",recruitDemObj.getBigDecimal("tdkw_rec_min_salary").setScale(2, RoundingMode.HALF_UP));
            paramMap.put("outName",recruitDemObj.getString("tdkw_rec_out_posi_name"));
            paramMap.put("recruitNum",recruitDemObj.getInt("tdkw_rec_recruit_num"));
            // #138223 人力系统无法提招聘需求，具体如图，请协助尽快解决。【目前字段控制245个中文字符，需要扩大】历史数据要迁移
            // http://ones.xiangyu.com/project/#/team/JbjqrWit/task/Ww14VUTMzpPIxFgW
            paramMap.put("responsibility",recruitDemObj.getString("tdkw_rec_job_duty_text"));
            paramMap.put("specification",recruitDemObj.getString("tdkw_rec_job_req_text"));
            paramMap.put("sexSel",recruitDemObj.getString("tdkw_rec_sex"));
            paramMap.put("workplace",recruitDemObj.getString("tdkw_rec_work_address"));
            paramMap.put("subordinateNum",recruitDemObj.getInt("tdkw_rec_rpthav"));
            paramMap.put("userId",recruitDemObj.getString("tdkw_rec_apply_hr_pk"));
            paramMap.put("internation",recruitDemObj.getBoolean("tdkw_internation"));
            Map<String, Object> cpMap = new HashMap<>();
            cpMap.put("docCode", billNo);
            cpMap.put("source", "HR");
            cpMap.put("target", "SAAST");
            paramMap.put("cp", cpMap);
            logger.info(String.format("createRequirement，请求参数=%s", JSON.toJSONString(paramMap)));
//            HashMap<String, Object> reqMap = convertObjectToHashMap(dto);
//            HashMap reqMap = new ObjectMapper().convertValue(dto, HashMap.class);
            // 调用北森创建需求接口
            Object obj = DispatchServiceHelper.invokeBizService(
                    "isc",
                    "iscb",
                    "IscApicService",
                    "invokeScriptApi2",
                    "createRequirement",
                    paramMap,
                    "");
            logger.info(String.format("返回结果=%s", JSON.toJSONString(obj)));
            JSONObject result = JSONObject.parseObject(JSON.toJSONString(obj));
            Integer statusCode = result.getInteger("statusCode");
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException("北森接口返回失败！" + result.getString("message"));
            }
        } catch (Exception e) {
            logger.error("招聘需求创建失败！", e);
            logger.info("招聘需求创建失败！" + e);
            logger.info("招聘需求创建失败！" + e.getMessage());
            throw new RuntimeException("调用北森接口异常！");
        }
    }

    public static HashMap<String, Object> convertObjectToHashMap(Object obj) throws IllegalAccessException {
        HashMap<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            map.put(field.getName(), value);
        }
        return map;
    }

    /**
     * 获取多选基础资料-name
     *
     * @param sourceCollection
     * @return
     */
    private static String getDynamicObjectCollectionName(DynamicObjectCollection sourceCollection) {
        List<String> list = new ArrayList<>();
        logger.info("sourceCollection.size = {}",sourceCollection.size());
        for (DynamicObject dynamicObject : sourceCollection) {
            OrmLocaleValue object = (OrmLocaleValue) ((DynamicObject) dynamicObject.get(1)).get("name");
            if (StringUtils.isNotBlank(object.getLocaleValue_zh_CN())) {
                if(!list.contains(object.getLocaleValue_zh_CN())){
                    list.add(object.getLocaleValue_zh_CN());
                }
            }
        }
        return StringUtils.join(list,",");
    }

}
