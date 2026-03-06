package tdkw.hrmp.hrobs.common.myteam.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.property.ComboProp;
import kd.bos.fileservice.FileItem;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.threads.ThreadPool;
import kd.bos.threads.ThreadPools;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRCollUtil;
import kd.hr.hbp.common.util.HRImageUrlUtil;
import kd.hr.hbp.common.util.HRObjectUtils;
import kd.hr.hbpm.mservice.PositionServiceImpl;
import kd.hr.hbpm.mservice.api.IPositionService;
import kd.hrmp.hrpi.common.HRPIPageConstants;
import kd.sdk.hr.hspm.common.utils.QFilterUtil;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.myteam.common.excel.ExcelExportHandler;
import tdkw.hrmp.hrobs.common.myteam.common.excel.ExcelRowBaseData;
import tdkw.hrmp.hrobs.common.myteam.common.excel.MyTeamExcelFormatBean;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author xxx
 * @version 1.0
 * @date 2023/7/18-17:37
 * @description 人员下属信息工具类
 */
public class PersonSubordinateInfoUtils {

    /**
     * 当前版本的过滤
     */
    private static final QFilter CURRENTVERSION = new QFilter("iscurrentversion", "=", "1");
    public static final String SUCCESS = "success";
    private static final Log logger = LogFactory.getLog(PersonSubordinateInfoUtils.class);
    private static final String FIELD = "tdkw_reportcoreltype,tdkw_manager,tdkw_position,tdkw_entryentity.tdkw_subordinate,tdkw_latestappointment,tdkw_entryentity.tdkw_newstappointment,tdkw_entryentity.tdkw_headsculpture,tdkw_entryentity.tdkw_phone,tdkw_entryentity.tdkw_peremail,tdkw_entryentity.tdkw_adminorg,tdkw_entryentity.tdkw_position1";

    private static ThreadPool threadPool = ThreadPools.newFixedThreadPool("threadPool", 2);
//    private final static ThreadPool THREAD_POOL = ThreadPools.newFixedThreadPool("峰回路转",2);
//    private void  test(){
//        THREAD_POOL.execute();
//    }

    /**
     * 通过经理人Id和岗位Id返回其下属信息，经理人Id只支持HR人员信息Id
     *
     * @param positionId      HR岗位信息Id
     * @param collaborativeId 协作关系id
     */
    public static JSONObject getPersonnelSubordinates(Long positionId, Long collaborativeId) {


        long currentUserId = UserServiceHelper.getCurrentUserId();
        Long personId = getHRUser(currentUserId);

        logger.info("人员ID：" + personId + ";岗位ID：" + positionId + ";协作关系id：" + collaborativeId);
        // TODO 防止项目上出现入职到职务的，所以这里同时获取岗位下的职务，给元数据上添加一个职务，一起过滤
        DynamicObject position = BusinessDataServiceHelper.loadSingle(positionId, "hbpm_positionhr");
        // 获取岗位的职务
        DynamicObject job = position.getDynamicObject("job");
        SubordinatePersonnelVo resultVo = new SubordinatePersonnelVo();
        logger.info("经理人ID:" + personId + ";岗位ID:" + positionId);
        QFilter qFilter = new QFilter("tdkw_manager", "=", personId);
        if (collaborativeId != null) {
            qFilter.and("tdkw_reportcoreltype", "=", collaborativeId);
        }
        if (HRObjectUtils.isEmpty(job)) {
            qFilter.and(new QFilter("tdkw_position", QCP.equals, positionId));
        } else {
            qFilter.and(new QFilter("tdkw_position", QCP.equals, positionId).or(new QFilter("tdkw_job", QCP.equals, job.getLong(HRBaseConstants.ID))));
        }
        // 顶层经理人的人员下属信息
        DynamicObject top = BusinessDataServiceHelper.loadSingle("tdkw_appauth_businessunit", FIELD, qFilter.toArray());

        logger.info("下属信息集合:" + top);
        QFilter qFilter1 = new QFilter("person.id", "=", personId);
        // 联系方式
        DynamicObject percontact = BusinessDataServiceHelper.loadSingle("hrpi_percontact", "phone,busemail,person", qFilter1.and(CURRENTVERSION).toArray());
        // 全部的任职经历
        DynamicObject pernontsprop = BusinessDataServiceHelper.loadSingle("hrpi_pernontsprop", "headsculpture,person", qFilter1.and(CURRENTVERSION).toArray());
        DynamicObject empposorgrel = BusinessDataServiceHelper.loadSingle("hrpi_empposorgrel", "tdkw_ranks,person,position,adminorg", qFilter1.and("position.id", "=", positionId).and("businessstatus", "=", "1").and(CURRENTVERSION).toArray());
        logger.info("联系方式:" + percontact);
        logger.info("任职经历:" + empposorgrel);
        logger.info("人员非时序性属性:" + pernontsprop);
        // 头像
        resultVo.setId(top == null ? String.valueOf(personId) : String.valueOf(top.getLong("tdkw_manager.id")));
        resultVo.setIslatestrecord(top == null ? null : String.valueOf(top.getBoolean("tdkw_latestappointment")));
        String url = percontact == null ? "" : pernontsprop.getString("headsculpture");
        String imageFullUrl = HRImageUrlUtil.getImageFullUrl(url);
        resultVo.setIon(imageFullUrl);
        resultVo.setName(percontact == null ? "" : percontact.getString("person.name"));
        resultVo.setPosition(top == null ? null : top.getString("tdkw_position.name"));
        resultVo.setDepartment(empposorgrel == null ? "" : empposorgrel.getString("adminorg.name"));
        // 添加id，方便直接获取数据，导出Excel
        resultVo.setPositionid(top == null ? String.valueOf(positionId) : top.getString("tdkw_position.id"));
        resultVo.setDepartmentid(empposorgrel == null ? "" : empposorgrel.getString("adminorg.id"));
        resultVo.setPhone(percontact == null ? "" : percontact.getString("phone"));
        resultVo.setMail(percontact == null ? "" : percontact.getString("busemail"));
        List<SubordinatePersonnelVo> subordinatePersonnelVoList = new ArrayList<>();
        DynamicObjectCollection entryentity = new DynamicObjectCollection();
        if (top != null) {
            logger.info("顶层经理人不为空");
            entryentity = top.getDynamicObjectCollection("tdkw_entryentity");
        }
        if (entryentity.size() > 0) {
            List<SubordinatePersonnelVo> subordinatePersonnel = getSubordinatePersonnel(entryentity, collaborativeId);
            resultVo.setChildren(subordinatePersonnel);
        }
        JSONObject json = new JSONObject();
        Long userId = HRRoleAndPersonUtils.getCQUser(personId);
        JSONObject myTeamPersonAgeInfo = getMyTeamPersonAgeInfo(String.valueOf(userId), positionId, collaborativeId);
        JSONArray myTeamPersonAgeArray = new JSONArray();
        myTeamPersonAgeArray.add(myTeamPersonAgeInfo);
        json.put("teamBaseInfo", myTeamPersonAgeArray);
        json.put("data", resultVo);
        json.put("message", "人员下属返回成功");
        json.put("status", 200);
        return json;

    }

    /**
     * 查询全部的协作类型
     */
    public static JSONArray queryAllCollaborativeType() throws NullPointerException {
        QFilter qFilter = new QFilter("status", "=", "C");
        // 开发环境为了展示两条数据，是否预置
//        qFilter.and("issyspreset", "=", "0");
        DynamicObject[] load = BusinessDataServiceHelper.load("hbpm_reportcoreltype",
                "id,name",
                qFilter.toArray());
        JSONArray jsonArray = new JSONArray();
        for (DynamicObject dynamicObject : load) {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id", dynamicObject.getLong("id") + "");
            OrmLocaleValue name = (OrmLocaleValue) dynamicObject.get("name");
            jsonObject1.put("name", name.get("zh_CN"));
            jsonArray.add(jsonObject1);
        }
        return jsonArray;
    }

    private static List<Long> queryPositionSet() {
        long currentUserId = UserServiceHelper.getCurrentUserId();
        Long hrUserId = getHRUser(currentUserId);
        QFilter qFilter = new QFilter("person.id", "=", hrUserId);
        qFilter.and("businessstatus", "=", "1");
        DynamicObject[] empposorgrelArray = BusinessDataServiceHelper.load("hrpi_empposorgrel", "position", qFilter.and(CURRENTVERSION).toArray());

        // 岗位id
        List<Long> positionList = Arrays.stream(empposorgrelArray).map((empposorgrel) -> {
            return empposorgrel.getLong("position.id");
        }).collect(Collectors.toList());
        return positionList;
    }

    /**
     * 返回当前人员的HR岗位
     */
    public static JSONArray getPositionSet() {
        List<Long> list = queryPositionSet();
        DynamicObject[] ins = BusinessDataServiceHelper.load("hbpm_positionhr",
                "id,name",
                new QFilter("id", "in", list).and(CURRENTVERSION).toArray());
        long currentUserId = UserServiceHelper.getCurrentUserId();
        Long hrUserId = getHRUser(currentUserId);
        QFilter qFilter = new QFilter("person.id", "=", hrUserId).and("isprimary", "=", "1");
        qFilter.and("businessstatus", "=", "1");
        DynamicObject empposorgrel = BusinessDataServiceHelper.loadSingle("hrpi_empposorgrel", qFilter.and(CURRENTVERSION).toArray());
        // 主任职id
        long aLong = 0L;
        if (empposorgrel != null) {
            aLong = empposorgrel.getLong("position.id");
        }
        // 岗位过滤虚拟兼职
        String partTime = System.getProperty("constant.hrmp.hbss.tdkw_hbss_changereason.fictitious");
        if (kd.bos.orm.util.StringUtils.isEmpty(partTime)) {
            partTime = "XY00017";
        }
        JSONArray jsonArray = new JSONArray();
        for (DynamicObject dynamicObject : ins) {
            logger.info(isPartTime(dynamicObject, partTime) + "是否存在虚拟兼职");
            if (isPartTime(dynamicObject, partTime)) {
                continue;
            }
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id", dynamicObject.getString("id"));
            OrmLocaleValue name = (OrmLocaleValue) dynamicObject.get("name");
            jsonObject1.put("name", name.get("zh_CN"));
            if (aLong == dynamicObject.getLong("id")) {
                jsonObject1.put("isprimary", "1");
            } else {
                jsonObject1.put("isprimary", "0");
            }
            jsonArray.add(jsonObject1);
        }

        return jsonArray;
    }


    private static boolean isPartTime(DynamicObject dynamicObject, String partTime) {
        /*QFilter qF = new QFilter("position.id", QCP.equals, dynamicObject.getLong("id"));
        OrmLocaleValue name = (OrmLocaleValue) dynamicObject.get("name");
        qF.and("position.name", QCP.equals, name.get("zh_CN"));
        qF.and("iscurrentversion", QCP.equals, "1");
        long currentUserId = UserServiceHelper.getCurrentUserId();
        qF.and("person.id", QCP.equals, getHRUser(currentUserId));
        DynamicObject hrpiEmpposorgrel = BusinessDataServiceHelper.loadSingle("hrpi_empposorgrel", "tdkw_changereason", qF.toArray());
        logger.info("查到的任职经历对应的异动原因：" + hrpiEmpposorgrel.getString("tdkw_changereason.number"));
        String changereasonNumber = hrpiEmpposorgrel.getString("tdkw_changereason.number");
        logger.info(dynamicObject.getString("name") + "==异动原因：" + changereasonNumber);
        return partTime.equals(changereasonNumber);*/
        /**
         * TODO 这里先注释，因为是二开在 hrpi_empposorgrel 这上面扩展的，业务性太强，暂时注释
         */
        return false;
    }

    /**
     * 返回当前人员的HR岗位的协作关系Id
     */
    public static Set<Object> getCollaborative(Long positionId) {
        List<Long> list = new ArrayList<>();
        list.add(positionId);
        IPositionService positionService = new PositionServiceImpl();
        Map<String, Object> map = positionService.queryPosition(list);
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.get("data");
        logger.info("map信息List" + mapList);
        Set<Object> set = new HashSet<>();
        if (!CollectionUtils.isEmpty(mapList) && mapList.size() > 0) {
            Map<String, Object> roleMap = mapList.get(0);
            logger.info("map信息" + roleMap);
            // 角色id对应的角色协作关系集合
//        Map<Long, List<Map<String, Object>>> workRolesMap = (Map<Long, List<Map<String, Object>>>) map1.get("reportRelation");
            List<Map<String, Object>> workRolesMapList = (List<Map<String, Object>>) roleMap.get("reportRelation");
            if (workRolesMapList == null) {
                return set;
            }

            for (Map<String, Object> stringObjectMap : workRolesMapList) {
                Object id = stringObjectMap.get("id");
                set.add(id);
            }
        }

        return set;
    }

    /**
     * 通过苍穹人员Id查询对应的HR人员Id
     *
     * @param id 苍穹人员Id
     * @return HR人员信息Id
     */
    public static Long getHRUser(Long id) {
        if (id == null || id == 0L) {
            return 0L;
        }
        QFilter qFilter = new QFilter("user", QCP.equals, id);
        DynamicObject dynamicObject = QueryServiceHelper.queryOne("hrpi_personuserrel", "person", qFilter.toArray());
        if (dynamicObject == null) {
            return 0L;
        }
        long person = dynamicObject.getLong("person");
        DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("hrpi_person", "name,number", new QFilter("id", "=", person).toArray());
        QFilter filter = new QFilter("iscurrentversion", "!=", false)
                .and("name", "=", dynamicObject1.get("name"))
                .and("number", "=", dynamicObject1.get("number"));
        if (dynamicObject1 == null) {
            return 0L;
        }
        DynamicObject dynamicObject2 = BusinessDataServiceHelper.loadSingle("hrpi_person", "id", filter.toArray());
        if (dynamicObject2 == null) {
            return 0L;
        }
        return dynamicObject2.getLong("id");
    }

    /**
     * 查看人员档案
     */
    public static void viewPersonFile(Long positionId, Long personId, IFormView formView) {
        /*long currentUserId = UserServiceHelper.getCurrentUserId();

        QFilter qFilter = new QFilter("position.id", "=", positionId);
        qFilter.and("person.id", "=", personId);
        qFilter.and(CURRENTVERSION);
        qFilter.and("businessstatus", "=", "1");
        // 查询任职经历
        DynamicObjectCollection empposorgrels = QueryServiceHelper.query("hrpi_empposorgrel",
                "id,depemp", qFilter.toArray(), "startdate desc");
        if (HRCollUtil.isEmpty(empposorgrels)) {
            formView.showTipNotification("找不到对应的任职经历");
            return;
        }
        *//**
         * TODO 项目上为啥在自然人身上获取人事业务档案id呢？
         * 任职经历有了，为啥不根据组织人直接获取人事业务档案的id？？
         *//*
        // 找到唯一对应人事业务档案
        DynamicObject personFile = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
        if (personFile == null) {
            formView.showTipNotification("找不到对应的人事业务档案");
            return;
        }
         String pkValue = personFile.getString("tdkw_pkid");
        // 我这边查询下人事业务档案算求,
        long depEmpId = empposorgrels.get(HRBaseConstants.INT_ZERO).getLong("depemp");
        DynamicObject erFileByDepemp = getErFileByDepemp(depEmpId);
        if (HRObjectUtils.isEmpty(erFileByDepemp)) {
            formView.showTipNotification("找不到对应的人事业务档案");
        }*/
        // 跳转我的档案
        FormShowParameter formShowParameter = new FormShowParameter();
        formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
        formShowParameter.setCustomParam("erfileId", String.valueOf(personId));
        formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
        formShowParameter.setHasRight(true);
        formView.showForm(formShowParameter);
    }

    public static DynamicObject getErFileByDepemp(long depEmpId) {
        HRBaseServiceHelper serviceHelper = new HRBaseServiceHelper(HRPIPageConstants.PAGE_ERMANFILE);
        QFilter currentQf = QFilterUtil.getCurrentQf();
        currentQf.and(QFilterUtil.getDataStatusFilter());
        currentQf.and(QFilterUtil.getBusinessStatus());
        currentQf.and(new QFilter("depemp", QCP.equals, depEmpId));
        return serviceHelper.queryOne(currentQf.toArray());
    }

    /**
     * 创建Excel
     *
     * @param formView
     * @throws IOException
     * @throws IllegalAccessException
     */
    public static void buildExcel(IFormView formView, SubordinatePersonnelVo subordinatePersonnelVo) throws IOException, IllegalAccessException {
        List<ExcelRowBaseData<MyTeamExcelFormatBean>> list = queryDownloadData(subordinatePersonnelVo);
        String fileName = "我的下属们.xls";
        String filePath = String.format("%s%s", "/tdkw/common/myteam/", fileName);
//        //输出流
        OutputStream os = new ByteArrayOutputStream();
        // 写入数据
        new ExcelExportHandler<MyTeamExcelFormatBean>().write(os, "sheet1", list, MyTeamExcelFormatBean.class);
        // 转换为输入流
        InputStream is = new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray());
        // 文件服务o
        FileService fs = FileServiceFactory.getAttachmentFileService();
        String path = fs.upload(new FileItem(fileName, filePath, is));
        // 下载
        formView.openUrl(RequestContext.get().getClientFullContextPath() + "/attachment/download.do?path=" + path);

    }


    /**
     * 生成Excel模型对象
     *
     * @param subordinatePersonnelVo
     * @return
     */
    private static List<ExcelRowBaseData<MyTeamExcelFormatBean>> queryDownloadData(SubordinatePersonnelVo subordinatePersonnelVo) {
        List<ExcelRowBaseData<MyTeamExcelFormatBean>> list = new ArrayList<>();

        // map中包含岗位id,人员id和部门id
        List<Map<String, String>> maps = resolvingJson(subordinatePersonnelVo);

        // 人员、岗位、部门id集合
        List<Long> personIdList = maps.stream().map(map -> Long.valueOf(ObjectUtils.isEmpty(map.get("personId")) ? "0" : map.get("personId"))).filter(personId -> personId != 0).collect(Collectors.toList());
        List<Long> positionIdList = maps.stream().map(map -> Long.valueOf(ObjectUtils.isEmpty(map.get("positionId")) ? "0" : map.get("positionId"))).filter(positionId -> positionId != 0).collect(Collectors.toList());
        List<Long> departmentIdList = maps.stream().map(map -> Long.valueOf(ObjectUtils.isEmpty(map.get("departmentId")) ? "0" : map.get("departmentId"))).filter(departmentId -> departmentId != 0).collect(Collectors.toList());

        QFilter qFilter = new QFilter("person.id", "in", personIdList);
        qFilter.and("position.id", "in", positionIdList);
        qFilter.and("adminorg.id", "in", departmentIdList);
        qFilter.and("businessstatus", QCP.equals, "1");
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and(CURRENTVERSION);
        // 任职经历。好像没什么要获取的
        DynamicObject[] empposorgrels = BusinessDataServiceHelper.load("hrpi_empposorgrel", "person,person.id,company,company.name,tdkw_jobsequence,tdkw_jobsequence.name,tdkw_employtype,tdkw_employtype.name", qFilter.toArray());

        // 人员Id映射证件号码
        Map<Long, String> positionIdMapPositionlevel = getPositionIdMapPositionlevel(positionIdList);
        // 岗位Id映射岗位职层
        Map<Long, String> personIdMapIDNO = getPersonIdMapIDNO(personIdList);
        // 人员Id和非时序性属性的映射
        Map<Long, Map> personIdMapP = getPersonIdMapP(personIdList);
        // 人员Id和非时序性属性的映射
        Map<Long, Map> personTimingMapP = getPersonTimingMapP(personIdList);

        // Excel实体模型赋值
        int rowNum = 0;
        for (Map<String, String> map : maps) {
            String personId = map.get("personId") == null ? "0" : map.get("personId");
            String positionId = map.get("positionId") == null ? "0" : map.get("positionId");
            String departmentId = map.get("departmentId") == null ? "0" : map.get("departmentId");
            String position = map.get("position") == null ? "" : map.get("position");
            String department = map.get("department") == null ? "" : map.get("department");

            MyTeamExcelFormatBean myTeamExcelFormatBean = new MyTeamExcelFormatBean();
            if (!ObjectUtils.isEmpty(personIdMapP)) {
                Map mapP = personIdMapP.get(Long.valueOf(personId));
                String gender = "";
                String name = "";
                String number = "";
                String folk = "";
                String age = "";
                String origin = "";
                String workdate = "";
                if (mapP != null) {
                    gender = mapP.get("gender") == null ? "" : (String) mapP.get("gender");
                    name = mapP.get("name") == null ? "" : (String) mapP.get("name");
                    number = mapP.get("number") == null ? "" : (String) mapP.get("number");
                    folk = mapP.get("folk") == null ? "" : (String) mapP.get("folk");
                    age = mapP.get("age") == null ? "" : (String) mapP.get("age");
                    origin = mapP.get("origin") == null ? "" : (String) mapP.get("origin");
                    workdate = mapP.get("workdate") == null ? "1" : (String) mapP.get("workdate");
                }
                myTeamExcelFormatBean.setNo(number);
                myTeamExcelFormatBean.setName(name);
                myTeamExcelFormatBean.setGender(gender);
                myTeamExcelFormatBean.setFolk(folk);
                myTeamExcelFormatBean.setAge(age);
                myTeamExcelFormatBean.setOrigin(origin);
                myTeamExcelFormatBean.setWorkdate(workdate);
            }
            // 婚姻状况
            if (!ObjectUtils.isEmpty(personTimingMapP)) {
                Map mapP = personTimingMapP.get(Long.valueOf(personId));
                String marriage = "";
                if (mapP != null) {
                    marriage = mapP.get("marriage") == null ? "" : (String) mapP.get("marriage");
                }
                myTeamExcelFormatBean.setMarriage(marriage);
            }
            // 任职经历
            if (!ObjectUtils.isEmpty(empposorgrels)) {
                for (DynamicObject empposorgrel : empposorgrels) {
                    DynamicObject person = empposorgrel.getDynamicObject("person");
                    if (!ObjectUtils.isEmpty(person)) {
                        long id = person.getLong("id");
                        if (personId.compareTo(String.valueOf(id)) == 0) {
                            DynamicObject company = empposorgrel.getDynamicObject("company");
                            if (!ObjectUtils.isEmpty(company)) {
                                myTeamExcelFormatBean.setOrg(company.getString("name"));
                            }
//                            DynamicObject jobSequence = empposorgrel.getDynamicObject("tdkw_jobsequence");
//                            if (!ObjectUtils.isEmpty(jobSequence)) {
                                myTeamExcelFormatBean.setSequence("测试职位序列");
//                            }
//                            DynamicObject employType = empposorgrel.getDynamicObject("tdkw_employtype");
//                            if (!ObjectUtils.isEmpty(employType)) {
                                myTeamExcelFormatBean.setPersontype("测试员工类型");
//                            }
                        }
                    }
                }
            }
            if (StringUtils.isNotEmpty(personId)) {
                String IdNO = personIdMapIDNO.get(Long.valueOf(personId));
                myTeamExcelFormatBean.setIdno(IdNO);
            }
            if (StringUtils.isNotEmpty(positionId)) {
                String postlevel = positionIdMapPositionlevel.get(Long.valueOf(positionId));
                myTeamExcelFormatBean.setPostlevel(postlevel);
            }
            myTeamExcelFormatBean.setDep(department);
            myTeamExcelFormatBean.setPosition(position);
            ExcelRowBaseData excelRowBaseData = new ExcelRowBaseData(myTeamExcelFormatBean);
            excelRowBaseData.setRowNum(rowNum);
            rowNum++;
            list.add(excelRowBaseData);
        }
        // personIdList转为set
        Set<Long> personIdSet = new HashSet<>(personIdList);
        QFilter filter = new QFilter("person", QCP.in, personIdSet)
                .and("affiliateadminorg", QCP.in, departmentIdList)
                .and("iscurrentversion", QCP.equals, "1")
                .and("businessstatus", QCP.equals, "1")
                .and("datastatus", QCP.equals, "1");
        // 查询人事业务档案并排序
        DynamicObjectCollection erManList = QueryServiceHelper.query("hspm_ermanfile", "id,person.number", filter.toArray(), "id");
        // 转map
        Map<String, Integer> erManMap = IntStream.range(0, erManList.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> erManList.get(i).getString("person.number"), // 键提取器
                        i -> i + 1, // 值提取器
                        (existingValue, newValue) -> existingValue // 合并函数，保留已存在的值
                ));

        // 把工号重复的数据且靠后的数据移除
        return new ArrayList<>(list.stream()
                // 首先按照工号对应的优先级进行排序
                .sorted(Comparator.comparingInt(child -> erManMap.getOrDefault(child.getRowData().getNo(), Integer.MAX_VALUE)))
                // 使用LinkedHashMap来保留排序后的顺序，并确保工号的唯一性
                .collect(Collectors.toMap(
                        child -> child.getRowData().getNo(), // 工号作为key
                        child -> child, // 完整的对象作为value
                        (existing, replacement) -> existing, // 如果有重复的key，保留第一个
                        LinkedHashMap::new))
                .values());
    }


    /**
     * 查询详细信息
     *
     * @return
     */
    private static Object queryDetailedInfo() {
        return null;
    }


    /**
     * 人员Id和非时序性属性的映射
     *
     * @param personIdList
     * @return
     */
    private static Map<Long, Map> getPersonIdMapP(List<Long> personIdList) {
        // 非时序性属性。查询到【性别】、【姓名】、【工号】、【民族】、【年龄】、【籍贯】
        Map<Long, Map> personIdMapP = new HashMap<>();
        if (!CollectionUtils.isEmpty(personIdList)) {
            DynamicObject[] pernontsprop = BusinessDataServiceHelper.load("hrpi_pernontsprop", "gender,person,folk,age,tdkw_origin,tdkw_origin.tdkw_origin,beginservicedate",
                    new QFilter("person.id", "in", personIdList).and(CURRENTVERSION).toArray());
            // 人员Id和非时序性属性的映射
            personIdMapP = Arrays.stream(pernontsprop).collect(Collectors.toMap(dynamicObject -> dynamicObject.getLong("person.id"), dynamicObject -> {
                Map map = new HashMap<>();
                map.put("gender", dynamicObject.getString("gender.name"));
                map.put("name", dynamicObject.getString("person.name"));
                map.put("number", dynamicObject.getString("person.number"));
                map.put("folk", dynamicObject.getString("folk.name"));
                map.put("age", dynamicObject.getString("age"));
                // TODO 没这个元数据，给个默认值吧
                map.put("origin", "测试地址");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date workData = dynamicObject.getDate("beginservicedate");
                if (workData != null) {
                    map.put("workdate", simpleDateFormat.format(workData));
                }
                return map;
            }));
        }
        return personIdMapP;
    }

    /**
     * 人员Id和时序性属性的映射
     *
     * @param personIdList
     * @return
     */
    private static Map<Long, Map> getPersonTimingMapP(List<Long> personIdList) {
        // 非时序性属性。查询到【性别】、【姓名】、【工号】、【民族】、【年龄】、【籍贯】
        Map<Long, Map> personIdMapP = new HashMap<>();
        if (!CollectionUtils.isEmpty(personIdList)) {
            DynamicObject[] pernontsprop = BusinessDataServiceHelper.load("hrpi_pertsprop", "marriagestatus,person,person.id",
                    new QFilter("person.id", "in", personIdList).and(CURRENTVERSION).toArray());
            // 人员Id和非时序性属性的映射
            personIdMapP = Arrays.stream(pernontsprop).collect(Collectors.toMap(dynamicObject -> dynamicObject.getLong("person.id"), dynamicObject -> {
                Map map = new HashMap<>();
                map.put("marriage", dynamicObject.getString("marriagestatus.name"));
                return map;
            }));
        }
        return personIdMapP;
    }

    /**
     * 人员Id映射证件号码
     *
     * @param personIdList
     * @return
     */
    private static Map<Long, String> getPersonIdMapIDNO(List<Long> personIdList) {
        // 证件信息。【证件号码】，
        DynamicObject[] percre = BusinessDataServiceHelper.load("hrpi_percre", "number,person",
                new QFilter("person.id", "in", personIdList).and("ismajor", "=", "1").and(CURRENTVERSION).toArray());

        Map<Long, String> personIdMapIDNO = Arrays.stream(percre).collect(Collectors.toMap(dy -> dy.getLong("person.id"), dy -> dy.getString("number")));
        return personIdMapIDNO;
    }

    /**
     * 岗位Id映射岗位职层。如需要继续添加字段，请参照非时序性属性
     *
     * @param positionIdList
     * @return
     */
    private static Map<Long, String> getPositionIdMapPositionlevel(List<Long> positionIdList) {
        // Hr岗位。【岗位层级】
        DynamicObject[] positionhr = BusinessDataServiceHelper.load("hbpm_positionhr", "id,tdkw_positionlevel",
                new QFilter("id", "in", positionIdList).and(CURRENTVERSION).toArray());
        // 岗位Id映射岗位职层
        Map<Long, String> positionIdMapPositionlevel = Arrays.stream(positionhr).collect(Collectors.toMap(dy -> dy.getLong("id"), dy -> {
            /**
             * TODO 层级什么鬼，不知道，瞎给个值吧
             */
            /*String value = dy.getString("tdkw_positionlevel");
            // 获取单据实体类型
            MainEntityType mainEntityType = (MainEntityType) dy.getDynamicObjectType();
            DataEntityPropertyCollection properties = mainEntityType.getProperties();
            ComboProp positionlevel = (ComboProp) properties.get("tdkw_positionlevel");
            String key = positionlevel.getItemByName(value);
            return key;*/
            return HRBaseConstants.STR_ONE;
        }));
        return positionIdMapPositionlevel;
    }

    /**
     * 解析json，用于获取人员信息
     *
     * @return
     */
    private static List<Map<String, String>> resolvingJson(SubordinatePersonnelVo myTeamBriefInfoBean) {
        List<Map<String, String>> targetList = new ArrayList<>();

        // 将json字符串转成对象

//        Gson gson = new Gson();
//        SubordinatePersonnelVo myTeamBriefInfoBean = gson.fromJson(jsonString, SubordinatePersonnelVo.class);

        setValue(targetList, myTeamBriefInfoBean);

        List<SubordinatePersonnelVo> list = myTeamBriefInfoBean.getChildren();
        if (list != null) {
            for (SubordinatePersonnelVo teamBriefInfoBean : list) {
                recursion(targetList, teamBriefInfoBean);
            }
        }
        return targetList;
    }

    /**
     * 将我的团队中人员id，岗位id和部门id提取到目标集合中
     *
     * @param targetList
     * @param teamBriefInfoBean
     */
    private static void setValue(List<Map<String, String>> targetList, SubordinatePersonnelVo teamBriefInfoBean) {
        Map<String, String> map = new HashMap<>();
        // 人员id
        map.put("personId", teamBriefInfoBean.getId());
        // 岗位id
        map.put("positionId", teamBriefInfoBean.getPositionid());
        // 部门id
        map.put("departmentId", teamBriefInfoBean.getDepartmentid());
        // 岗位名称
        map.put("position", teamBriefInfoBean.getPosition());
        // 部门名称
        map.put("department", teamBriefInfoBean.getDepartment());

        targetList.add(map);
    }

    /**
     * 递归解析json字符串
     *
     * @param targetList
     * @param teamBriefInfoBean
     */
    private static void recursion(List<Map<String, String>> targetList, SubordinatePersonnelVo teamBriefInfoBean) {
        setValue(targetList, teamBriefInfoBean);
        List<SubordinatePersonnelVo> list = teamBriefInfoBean.getChildren();
        if (list != null && list.size() > 0) {
            for (SubordinatePersonnelVo myTeamBriefInfoBean : list) {
                recursion(targetList, myTeamBriefInfoBean);
            }
        }
    }

    /**
     * 获取HR人员的团队人员的平均年龄，人数，司龄
     *
     * @param id              苍穹人员Id
     * @param positionId
     * @param collaborativeId
     * @return JSON
     */
    public static JSONObject getMyTeamPersonAgeInfo(String id, Long positionId, Long collaborativeId) {
        JSONObject obj = new JSONObject();
        if (StringUtils.isEmpty(id)) {
            id = String.valueOf(RequestContext.get().getCurrUserId());
        }
        Long hrUser = getHRUser(Long.valueOf(id));
        List<Long> ids = new ArrayList<>();
        if (collaborativeId == null || collaborativeId == 0L) {
            ids = getSubordinatePersonnel(hrUser, positionId, null);
        } else {
            ids = getSubordinatePersonnel(hrUser, positionId, collaborativeId);
        }
        if (ids.size() > 0) {
            // 业务档案状态  1 = 生效中
            QFilter businessstatusQF = new QFilter("businessstatus", QCP.equals, "1");
            // 数据版本状态  1 = 生效中
            QFilter datastatusQF = new QFilter("datastatus", QCP.equals, "1");

            QFilter qFilter1 = new QFilter("person.id", QCP.in, ids);
            // 是否当前版本  Boolean类型  true = 是
            QFilter qFilter2 = new QFilter("iscurrentversion", QCP.equals, true);
            QFilter qFilter3 = qFilter1.and(qFilter2).and(datastatusQF);
            // 人员非时序性属性
            DynamicObjectCollection query = QueryServiceHelper.query("hrpi_pernontsprop", "id,person,age", qFilter3.toArray());
            // 人数
            obj.put("psnCount", query.size());
            // 平均年龄
            BigDecimal averageAge = BigDecimal.ZERO;
            BigDecimal age = BigDecimal.ZERO;
            for (DynamicObject object : query) {
                age = age.add(object.getBigDecimal("age"));
            }
            if (query.size() > 0) {
                averageAge = age.divide(new BigDecimal(query.size()), 1, RoundingMode.HALF_UP).setScale(1);
            }
            // 平均年龄
            obj.put("avgAge", averageAge);
            // 服务年限
            DynamicObjectCollection query1 = QueryServiceHelper.query("hrpi_perserlen", "person,comsercount", qFilter3.toArray());
            BigDecimal avgJoinGroupAge = new BigDecimal("0");
            BigDecimal count = new BigDecimal("0");
            for (DynamicObject object : query1) {
                count = count.add(object.getBigDecimal("comsercount"));
            }
            if (query1.size() > 0) {
                avgJoinGroupAge = count.divide(new BigDecimal(query1.size()), 1, RoundingMode.HALF_UP);
            }
            obj.put("avgJoinGroupAge", avgJoinGroupAge);
        } else {
            // 人数
            obj.put("psnCount", 0);
            // 平均年龄
            obj.put("avgAge", 0);
            // 平均司龄
            obj.put("avgJoinGroupAge", 0);
        }

        return obj;
    }

    /**
     * 查询实体：tdkw_appauth_businessunit
     * 获取该HR人员的下属人员
     *
     * @param hrUserId HR人员Id
     * @return 下属员工Id集合
     */
    public static List<Long> getSubordinatePersonnel(Long hrUserId, Long postId, Long collaborativeId) {
        Set<Long> list = new HashSet<>();
        List<Map<String, Object>> filterList = new ArrayList<>();
        QFilter qFilter = new QFilter("tdkw_manager.id", "=", hrUserId);
        if (postId != null && postId != 0L) {
            qFilter.and("tdkw_position.id", "=", postId);
        }
        if (collaborativeId != null && collaborativeId != 0L) {
            qFilter.and("tdkw_reportcoreltype.id", "=", collaborativeId);
        }
        DynamicObject[] collection = BusinessDataServiceHelper.load("tdkw_appauth_businessunit", "tdkw_entryentity,tdkw_entryentity.tdkw_subordinate,tdkw_entryentity.tdkw_position1", qFilter.toArray());
        List<Long> ids = new ArrayList<>();
        List<Map<String, Object>> queryMapList = new ArrayList<>();
        if (collection != null) {
            for (DynamicObject dynamicObject : collection) {
                DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                for (DynamicObject object : entryentity) {
                    Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                    long positionId = object.getLong("tdkw_position1.id");
                    Map<String, Object> filterMap = new HashMap<>();
                    filterMap.put("person", id);
                    filterMap.put("position", positionId);
                    filterList.add(filterMap);
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put(String.valueOf(id), positionId);
                    queryMapList.add(queryMap);
                    ids.add(id);
                    list.add(id);
                }
            }
        }
        if (ids.size() < 1) {
            return new ArrayList<>(list);
        }
        do {
            int i = list.size();
            QFilter qFilterList = new QFilter("tdkw_manager.id", QCP.in, ids);
            // 防止上下级混乱导致的死循环
            DynamicObject[] coll = BusinessDataServiceHelper.load("tdkw_appauth_businessunit", FIELD, qFilterList.toArray());
            ids.clear();
            if (coll != null) {
                List<Map<String, Object>> newQueryMapList = new ArrayList<>();
                for (DynamicObject dynamicObject : coll) {
                    Long managerId = dynamicObject.getLong("tdkw_manager.id");
                    Long position = dynamicObject.getLong("tdkw_position.id");
                    for (Map<String, Object> queryMap : queryMapList) {
                        if (queryMap.get(String.valueOf(managerId)) != null) {
                            Long managerPositionId = (Long) queryMap.get(String.valueOf(managerId));
                            if (position.compareTo(managerPositionId) == 0) {
                                DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                                for (DynamicObject object : entryentity) {
                                    Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                                    ids.add(id);
                                    list.add(id);
                                    long positionId = object.getLong("tdkw_position1.id");
                                    Map<String, Object> filterMap = new HashMap<>();
                                    filterMap.put("person", id);
                                    filterMap.put("position", positionId);
                                    Map<String, Object> newQueryMap = new HashMap<>();
                                    newQueryMap.put(String.valueOf(id), positionId);
                                    newQueryMapList.add(newQueryMap);
                                    filterList.add(filterMap);
                                }
                            }
                        }
                    }
                }
                queryMapList = newQueryMapList;
            }
            if (i == list.size()) {
                break;
            }
        } while (ids.size() != 0);

        return new ArrayList<>(list);
    }

    /**
     * 查询实体：tdkw_appauth_businessunit
     * 获取该HR人员的下属人员
     *
     * @return 下属员工Id集合
     */
    public static List<SubordinatePersonnelVo> getSubordinatePersonnel(DynamicObjectCollection dynamicObjects, Long collaborativeId) {
        List<SubordinatePersonnelVo> subordinatePersonnelVoList = new ArrayList<>();
        Map<String, Object> allPersonMap = new HashMap<>();
        Set<String> idList = new HashSet<>();
        // 遍历第一层下属
        List<Long> ids = new ArrayList<>();
        for (DynamicObject object : dynamicObjects) {
            SubordinatePersonnelVo underlingVo = new SubordinatePersonnelVo();
            long aLong = object.getLong("tdkw_subordinate.id");
            long bLong = object.getLong("tdkw_position1.id");
            String underlingUrl = object.getString("tdkw_headsculpture");
            String imageUnderlingUrl = HRImageUrlUtil.getImageFullUrl(underlingUrl);
            underlingVo.setIslatestrecord(String.valueOf(object.getBoolean("tdkw_newstappointment")));
            underlingVo.setIon(imageUnderlingUrl);
            underlingVo.setName(object.getString("tdkw_subordinate.name"));
            underlingVo.setPosition(object.getString("tdkw_position1.name"));
            underlingVo.setDepartment(object.getString("tdkw_adminorg.name"));
            // 添加id，方便直接获取数据，导出Excel
            underlingVo.setPositionid(object.getString("tdkw_position1.id"));
            underlingVo.setDepartmentid(object.getString("tdkw_adminorg.id"));
            underlingVo.setMail(object.getString("tdkw_peremail"));
            underlingVo.setPhone(object.getString("tdkw_phone"));
            underlingVo.setId(String.valueOf(aLong));
            subordinatePersonnelVoList.add(underlingVo);
            allPersonMap.put("" + aLong + bLong, underlingVo);
            ids.add(aLong);
            String idString = "" + aLong + bLong;
            idList.add(idString);
        }
        if (ids.size() < 1) {
            return null;
        }
        do {
            int i = idList.size();
            QFilter qFilterList = new QFilter("tdkw_manager.id", QCP.in, ids);
            if (collaborativeId != null && collaborativeId != 0L) {
                qFilterList.and("tdkw_reportcoreltype.id", "=", collaborativeId);
            }
            // 防止上下级混乱导致的死循环
            DynamicObject[] coll = BusinessDataServiceHelper.load("tdkw_appauth_businessunit", FIELD, qFilterList.toArray());
            ids.clear();
            if (coll != null) {
                for (DynamicObject dynamicObject : coll) {
                    Long managerId = dynamicObject.getLong("tdkw_manager.id");
                    Long position = dynamicObject.getLong("tdkw_position.id");
                    if (allPersonMap.get("" + managerId + position) != null) {
                        SubordinatePersonnelVo mapVo = (SubordinatePersonnelVo) allPersonMap.get("" + managerId + position);
                        if (mapVo.getPositionid().equals(String.valueOf(position))) {
                            DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                            for (DynamicObject object : entryentity) {
                                long personId = object.getLong("tdkw_subordinate.id");
                                long posId = object.getLong("tdkw_position1.id");
                                SubordinatePersonnelVo underlingVo = new SubordinatePersonnelVo();
                                underlingVo.setId(String.valueOf(personId));
                                underlingVo.setIslatestrecord(String.valueOf(object.getBoolean("tdkw_newstappointment")));
                                String underlingUrl = object.getString("tdkw_headsculpture");
                                String underlingImageFullUrl = HRImageUrlUtil.getImageFullUrl(underlingUrl);
                                underlingVo.setIon(underlingImageFullUrl);
                                underlingVo.setName(object.getString("tdkw_subordinate.name"));
                                underlingVo.setPosition(object.getString("tdkw_position1.name"));
                                underlingVo.setDepartment(object.getString("tdkw_adminorg.name"));
                                // 添加id，方便直接获取数据，导出Excel
                                underlingVo.setPositionid(String.valueOf(posId));
                                underlingVo.setDepartmentid(object.getString("tdkw_adminorg.id"));
                                underlingVo.setPhone(object.getString("tdkw_phone"));
                                underlingVo.setMail(object.getString("tdkw_peremail"));
                                underlingVo.setManagerPositionId(String.valueOf(position));
                                underlingVo.setManagerId(String.valueOf(dynamicObject.getLong("tdkw_manager.id")));
                                String idString = "" + personId + posId;
                                allPersonMap.put(idString, underlingVo);
                                ids.add(personId);
                                idList.add(idString);
                            }

                        }
                    }
                }
            }
            if (i == idList.size()) {
                break;
            }
        } while (ids.size() != 0);
        logger.info("所有key集合" + idList);
        logger.info("所有map集合" + allPersonMap);
        for (String personId : idList) {
            SubordinatePersonnelVo subordinatePersonnelVo = (SubordinatePersonnelVo) allPersonMap.get(personId);
            if (!ObjectUtils.isEmpty(subordinatePersonnelVo)) {
                String managerId = subordinatePersonnelVo.getManagerId();
                String positionId = subordinatePersonnelVo.getManagerPositionId();
                if (StringUtils.isNotEmpty(managerId)) {
                    SubordinatePersonnelVo manager = (SubordinatePersonnelVo) allPersonMap.get("" + managerId + positionId);
                    if (!ObjectUtils.isEmpty(manager)) {
                        manager.addChild(subordinatePersonnelVo);
                    }
                }
            }

        }
        return subordinatePersonnelVoList;
    }

    /**
     * 查询实体：tdkw_appauth_businessunit
     * 获取该HR人员的下属人员map集合
     *
     * @param hrUserId HR人员Id
     * @return 下属员工Id集合
     */
    public static List<Map<String, Object>> getSubordinatePersonnelFilter(Long hrUserId, Long postId, Long collaborativeId) {
        Set<Long> list = new HashSet<>();
        List<Map<String, Object>> filterList = new ArrayList<>();
        QFilter qFilter = new QFilter("tdkw_manager.id", "=", hrUserId);
        if (postId != null && postId != 0L) {
            qFilter.and("tdkw_position.id", "=", postId);
        }
        if (collaborativeId != null && collaborativeId != 0L) {
            qFilter.and("tdkw_reportcoreltype.id", "=", collaborativeId);
        }
        DynamicObject[] collection = BusinessDataServiceHelper.load("tdkw_appauth_businessunit", "tdkw_entryentity,tdkw_entryentity.tdkw_subordinate,tdkw_entryentity.tdkw_position1", qFilter.toArray());
        List<Long> ids = new ArrayList<>();
        List<Map<String, Object>> queryMapList = new ArrayList<>();
        if (collection != null) {
            for (DynamicObject dynamicObject : collection) {
                DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                for (DynamicObject object : entryentity) {
                    Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                    long positionId = object.getLong("tdkw_position1.id");
                    Map<String, Object> filterMap = new HashMap<>();
                    filterMap.put("person", id);
                    filterMap.put("position", positionId);
                    filterList.add(filterMap);
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put(String.valueOf(id), positionId);
                    queryMapList.add(queryMap);
                    ids.add(id);
                    list.add(id);
                }
            }
        }
        if (ids.size() < 1) {
            return filterList;
        }
        do {
            int i = list.size();
            QFilter qFilterList = new QFilter("tdkw_manager.id", QCP.in, ids);
            // 防止上下级混乱导致的死循环
            DynamicObject[] coll = BusinessDataServiceHelper.load("tdkw_appauth_businessunit", FIELD, qFilterList.toArray());
            ids.clear();
            if (coll != null) {
                List<Map<String, Object>> newQueryMapList = new ArrayList<>();
                for (DynamicObject dynamicObject : coll) {
                    Long managerId = dynamicObject.getLong("tdkw_manager.id");
                    Long position = dynamicObject.getLong("tdkw_position.id");
                    for (Map<String, Object> queryMap : queryMapList) {
                        if (queryMap.get(String.valueOf(managerId)) != null) {
                            Long managerPositionId = (Long) queryMap.get(String.valueOf(managerId));
                            if (position.compareTo(managerPositionId) == 0) {
                                DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("tdkw_entryentity");
                                for (DynamicObject object : entryentity) {
                                    Long id = (Long) object.getDynamicObject("tdkw_subordinate").getPkValue();
                                    ids.add(id);
                                    list.add(id);
                                    long positionId = object.getLong("tdkw_position1.id");
                                    Map<String, Object> filterMap = new HashMap<>();
                                    filterMap.put("person", id);
                                    filterMap.put("position", positionId);
                                    Map<String, Object> newQueryMap = new HashMap<>();
                                    newQueryMap.put(String.valueOf(id), positionId);
                                    newQueryMapList.add(newQueryMap);
                                    filterList.add(filterMap);
                                }
                            }
                        }
                    }
                }
                queryMapList = newQueryMapList;
            }
            if (i == list.size()) {
                break;
            }
        } while (ids.size() != 0);

        return filterList;
    }

}
