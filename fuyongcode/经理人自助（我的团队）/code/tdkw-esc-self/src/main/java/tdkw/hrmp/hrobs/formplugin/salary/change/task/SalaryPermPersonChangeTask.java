package tdkw.hrmp.hrobs.formplugin.salary.change.task;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.param.CustomParam;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取【XY_LEAD_PC_006	PC端领导自助特殊权限-简历薪酬】角色中人员发生异动数据调度
 * 取流入流出表异动类型+前三天
 * XY00003	内退_最新
 * XY00001	公司内调动_新
 * 102090_S	轮岗终止
 * 102060_S	轮岗
 * 101200_S	退休
 * 101140_S	外派终止
 * 101130_S	外派
 * 101120_S	借调终止
 * 101110_S	借调
 * 101100_S	兼职终止
 * 101070_S	兼职
 * 101060_S	跨公司调动
 * 101020_S	雇佣人员离职
 */
public class SalaryPermPersonChangeTask extends AbstractTask {

    // PC端领导自助特殊权限-简历薪酬
    private final static String XY_LEAD_PC_006 = "XY_LEAD_PC_006";
    // 日志
    private static final Log logger = LogFactory.getLog(SalaryPermPersonChangeTask.class);
    // 不存在原任职的变动类型
    private static final String[] types = new String[]{"102090_S", "102060_S", "101200_S", "101140_S", "101130_S", "101120_S", "101110_S", "101100_S", "101070_S", "101020_S"};
    // 存在原任职的变动类型
    private static final String[] transfer = new String[]{"XY00001", "101060_S", "XY00003"};
    // 流入流出表查询字段
    private static final String selectProperties = "person,chgcategory,flowtime,depemp,depemp";

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        // 查询角色的id
        QFilter roleFilter = new QFilter("number", QCP.equals, XY_LEAD_PC_006);
        roleFilter.and("enable", QCP.equals, "1");
        roleFilter.and("status", QCP.equals, "C");
        DynamicObject permRole = BusinessDataServiceHelper.loadSingle("perm_role", "id,number", roleFilter.toArray());
        if (Objects.isNull(permRole)) {
            logger.info("【PC端领导自助特殊权限-简历薪酬】角色不存在");
            return;
        }
        // 获取角色下的人员
        QFilter userRoleFilter = new QFilter("role.id", QCP.equals, permRole.getString("id"));
        DynamicObjectCollection userRoleList = QueryServiceHelper.query("hrcs_userrolerelat", "id,role.id,user.id", userRoleFilter.toArray());
        // 系统用户id
        List<Long> bosUserIdList = userRoleList.stream().mapToLong(dynamicObject -> dynamicObject.getLong("user.id")).boxed().collect(Collectors.toList());
        // 查询hr用户id
        QFilter userFilter = new QFilter("user", QCP.in, bosUserIdList);
        DynamicObjectCollection userList = QueryServiceHelper.query("hrpi_personuserrel", "id,person", userFilter.toArray());
        List<Long> hrUserIdList = userList.stream().mapToLong(dynamicObject -> dynamicObject.getLong("person")).boxed().collect(Collectors.toList());

        // 查询流入流出表前三天异动数据
        Map<String, String> parameterHelper = SystemParamServiceHelper.loadCustomParameterFromCache(new CustomParam());
        String salaryPermChangeType = parameterHelper.get("SALARY_PERM_CHANGE_TYPE");
        logger.info("变动类型：" + salaryPermChangeType);
        // 简历薪酬人员异动表
        DynamicObject[] cvsalarypchange = BusinessDataServiceHelper.load("tdkw_cvsalarypchange", "tdkw_personflowid", null);
        // 记录添加流入流出表id，防止重复录入
        List<Long> personflowid = Arrays.stream(cvsalarypchange).map(i -> i.getLong("tdkw_personflowid")).collect(Collectors.toList());
        // 简历薪酬人员异动表
        List<DynamicObject> cvsalarypchanges = new ArrayList<>();
        // 获取日期参数
        String date = (String) map.get("date");
        Date parse;
        if (StringUtils.isBlank(date)) {
            parse = new Date();
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                parse = dateFormat.parse(date);
            } catch (ParseException e) {
                throw new KDBizException("日期解析错误！");
            }
        }
        // 结束日期
        Calendar end = Calendar.getInstance();
        end.setTime(parse);
        end.set(Calendar.HOUR, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        Date endDate = end.getTime();
        // 开始日期
        Calendar start = Calendar.getInstance();
        start.setTime(parse);
        start.add(Calendar.DAY_OF_MONTH, -3);
        start.set(Calendar.HOUR, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        Date startDate = start.getTime();
        if (StringUtils.isBlank(salaryPermChangeType)) {
            logger.info("【PC端领导自助特殊权限-简历薪酬】系统参数-移动类型未配置-取默认异动类型");
            // 流入流出表查询条件,流动类型=流入
            QFilter flowIntoQfilter = new QFilter("chgcategory.number", QCP.in, transfer)
                    .and("person.id", QCP.in, hrUserIdList)
                    .and("flowtype", QCP.equals, "1")
                    .and("flowtime", QCP.large_equals, startDate)
                    .and("flowtime", QCP.less_equals, endDate)
                    .and("id", QCP.not_in, personflowid);
            // 流入流出表查询条件,流动类型=流出
            QFilter outflowQfilter = new QFilter("chgcategory.number", QCP.in, transfer)
                    .and("person.id", QCP.in, hrUserIdList)
                    .and("flowtype", QCP.equals, "2")
                    .and("flowtime", QCP.large_equals, startDate)
                    .and("flowtime", QCP.less_equals, endDate)
                    .and("id", QCP.not_in, personflowid);
            // 存在原任职流入流出表取值
            existingOffice(flowIntoQfilter, outflowQfilter, cvsalarypchanges);

            // 不存在原任职过滤
            QFilter personFlowFilter = new QFilter("chgcategory.number", QCP.in, types)
                    .and("person.id", QCP.in, hrUserIdList)
                    .and("flowtime", QCP.large_equals, startDate)
                    .and("flowtime", QCP.less_equals, endDate)
                    .and("id", QCP.not_in, personflowid);
            // 不存在原任职流入流出表取值
            noPreviousEmploymentExists(personFlowFilter, cvsalarypchanges);
        } else {
            List<String> variationType = new ArrayList<>();
            if (salaryPermChangeType.contains("XY00001")) {
                variationType.add("XY00001");
            }
            if (salaryPermChangeType.contains("101060_S")) {
                variationType.add("101060_S");
            }
            if (salaryPermChangeType.contains("XY00003")) {
                variationType.add("XY00003");
            }
            if (variationType.size() > 0) {
                // 流入流出表查询条件,流动类型=流入
                QFilter flowIntoQfilter = getFlowIntoQfilter(hrUserIdList, startDate, endDate, variationType, personflowid);
                // 流入流出表查询条件,流动类型=流出
                QFilter outflowQfilter = getOutflowQfilter(hrUserIdList, startDate, endDate, variationType, personflowid);
                // 存在原任职流入流出表取值
                existingOffice(flowIntoQfilter, outflowQfilter, cvsalarypchanges);
            }

            // 变动类型
            String[] salaryPermChangeTypes = salaryPermChangeType.split(",");
            // 过滤存在原入职编码
            List<String> chgcategorys = Arrays.stream(salaryPermChangeTypes).filter(e -> !"XY00001".equals(e) && !"101060_S".equals(e) && !"XY00003".equals(e)).collect(Collectors.toList());
            // 不存在原任职过滤
            QFilter personFlowFilter = getPersonFlowFilter(hrUserIdList, startDate, endDate, chgcategorys, personflowid);
            // 不存在原任职流入流出表取值
            noPreviousEmploymentExists(personFlowFilter, cvsalarypchanges);
        }
        SaveServiceHelper.save(cvsalarypchanges.toArray(new DynamicObject[]{}), OperateOption.create());
    }

    /**
     * 获取不存在原任职的流入流出表过滤
     *
     * @param hrUserIdList hr人员集合
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param chgcategorys 变动类型集合
     * @return QFilter
     */
    private static QFilter getPersonFlowFilter(List<Long> hrUserIdList, Date startDate, Date endDate, List<String> chgcategorys, List<Long> personflowid) {
        return new QFilter("chgcategory.number", QCP.in, chgcategorys)
                .and("person.id", QCP.in, hrUserIdList)
                .and("flowtime", QCP.large_equals, startDate)
                .and("flowtime", QCP.less_equals, endDate)
                .and("id", QCP.not_in, personflowid);
    }

    /**
     * 获取存在原任职的流入流出表 流出过滤
     *
     * @param hrUserIdList  hr人员集合
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param variationType 变动类型集合
     * @return QFilter
     */
    private static QFilter getOutflowQfilter(List<Long> hrUserIdList, Date startDate, Date endDate, List<String> variationType, List<Long> personflowid) {
        return new QFilter("chgcategory.number", QCP.in, variationType)
                .and("person.id", QCP.in, hrUserIdList)
                .and("flowtype", QCP.equals, "2")
                .and("flowtime", QCP.large_equals, startDate)
                .and("flowtime", QCP.less_equals, endDate)
                .and("id", QCP.not_in, personflowid);
    }

    /**
     * 获取存在原任职的流入流出表 流入过滤
     *
     * @param hrUserIdList  hr人员集合
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param variationType 变动类型集合
     * @return QFilter
     */
    private static QFilter getFlowIntoQfilter(List<Long> hrUserIdList, Date startDate, Date endDate, List<String> variationType, List<Long> personflowid) {
        return new QFilter("chgcategory.number", QCP.in, variationType)
                .and("person.id", QCP.in, hrUserIdList)
                .and("flowtype", QCP.equals, "1")
                .and("flowtime", QCP.large_equals, startDate)
                .and("flowtime", QCP.less_equals, endDate)
                .and("id", QCP.not_in, personflowid);
    }

    /**
     * 不存在原任职的流入流出表取值
     *
     * @param personFlowFilter 流入流出表过滤
     * @param cvsalarypchanges 简历薪酬人员异动表集合
     */
    private void noPreviousEmploymentExists(QFilter personFlowFilter, List<DynamicObject> cvsalarypchanges) {
        // 流入流出表
        DynamicObject[] personflows = BusinessDataServiceHelper.load("hpfs_personflow", selectProperties, new QFilter[]{personFlowFilter});
        for (DynamicObject personflow : personflows) {
            DynamicObject cvsalarypchange = getDynamicObject(personflow);
            cvsalarypchanges.add(cvsalarypchange);
        }
    }

    /**
     * 存在原任职的流入流出表取值
     *
     * @param flowIntoQfilter  流入流出表 流入过滤
     * @param outflowQfilter   流入流出表 流出过滤
     * @param cvsalarypchanges 简历薪酬人员异动表集合
     */
    private void existingOffice(QFilter flowIntoQfilter, QFilter outflowQfilter, List<DynamicObject> cvsalarypchanges) {
        // 流入流出表，流动类型=流入
        DynamicObject[] flowIntos = BusinessDataServiceHelper.load("hpfs_personflow", selectProperties, new QFilter[]{flowIntoQfilter});
        // 流入流出表，流动类型=流出
        DynamicObject[] outflows = BusinessDataServiceHelper.load("hpfs_personflow", selectProperties, new QFilter[]{outflowQfilter});

        for (DynamicObject flowInto : flowIntos) {
            Long incomingPerson = flowInto.getLong("person.id");
            for (DynamicObject outflow : outflows) {
                Long outflowPerson = outflow.getLong("person.id");
                if (incomingPerson.compareTo(outflowPerson) == 0) {
                    DynamicObject cvsalarypchange = getDynamicObject(flowInto);
                    // 组织人内码
                    long depempId = outflow.getLong("depemp.id");
                    // 组织人单据
                    DynamicObject depemp = BusinessDataServiceHelper.loadSingle(depempId, "hrpi_depemp");
                    // 原所属公司
                    cvsalarypchange.set("tdkw_change_old_company", depemp.get("adminorg.company"));
                    // 原所属部门
                    cvsalarypchange.set("tdkw_change_old_dept", depemp.get("adminorg"));
                    // 原岗位
                    cvsalarypchange.set("tdkw_change_old_pos", depemp.get("position"));

                    cvsalarypchanges.add(cvsalarypchange);
                }
            }
        }
    }

    /**
     * @param flowInto 流入流出表
     * @return 简历薪酬人员异动表
     */
    @NotNull
    private static DynamicObject getDynamicObject(DynamicObject flowInto) {
        // 变动类型编码
        String number = flowInto.getString("chgcategory.number");
        // 简历薪酬人员异动表
        DynamicObject cvsalarypchange = BusinessDataServiceHelper.newDynamicObject("tdkw_cvsalarypchange");
        // 变动时间
        switch (number) {
            case "101200_S":
            case "101020_S":
            case "102090_S":
            case "101140_S":
            case "101100_S":
                Date flowtime = flowInto.getDate("flowtime");
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(flowtime);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                cvsalarypchange.set("tdkw_change_time", calendar.getTime());
                break;
            default:
                cvsalarypchange.set("tdkw_change_time", flowInto.get("flowtime"));
                break;
        }


        // 人员
        cvsalarypchange.set("tdkw_change_user", flowInto.get("person"));
        // 变动类型
        cvsalarypchange.set("tdkw_change_chgcategory", flowInto.get("chgcategory"));

        // 组织人内码
        long depempId = flowInto.getLong("depemp.id");
        // 组织人单据
        DynamicObject depemp = BusinessDataServiceHelper.loadSingle(depempId, "hrpi_depemp");
        // 所属公司
        cvsalarypchange.set("tdkw_change_company", depemp.get("adminorg.company"));
        // 所属部门
        cvsalarypchange.set("tdkw_change_dept", depemp.get("adminorg"));
        // 岗位
        cvsalarypchange.set("tdkw_change_pos", depemp.get("position"));
        // 权限调整确认
        cvsalarypchange.set("tdkw_change_confirm", "3");
        // 操作人
        cvsalarypchange.set("tdkw_operator", null);
        // 操作时间
        cvsalarypchange.set("tdkw_operationtime", null);
        // 流入流出表id
        cvsalarypchange.set("tdkw_personflowid", flowInto.get("id"));
        return cvsalarypchange;
    }
}