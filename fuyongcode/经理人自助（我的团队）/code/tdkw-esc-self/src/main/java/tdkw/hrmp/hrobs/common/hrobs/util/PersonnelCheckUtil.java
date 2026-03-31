package tdkw.hrmp.hrobs.common.hrobs.util;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.plugin.support.util.ObjectUtils;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.form.ControlAp;
import kd.bos.metadata.form.FormMetadata;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xxx
 * @Description 获取人员档案完整度
 * @date 2023/7/21 10:33
 */
public class PersonnelCheckUtil {

    private static final Log LOGGER = LogFactory.getLog(PersonnelCheckUtil.class);

    public static double personnelFile(DynamicObject person) {
        //特殊规则校验
        double num = 0;
        List<String> SpecialRuleList;
        try {
            SpecialRuleList = specialRule(person);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        num += SpecialRuleList.size();
        //字段校验 模块
        List<String> integrityModuleList = integrityModule();
        // 读取配置表有哪些附表
        for (int idx = 0; idx < integrityModuleList.size(); idx++) {
            List<String> moduleFieldList = moduleField(integrityModuleList.get(idx), person);
            num += moduleFieldList.size();
        }

        return (allNum() - num + 13) / (allNum() + 13);
    }

    public static double allNum() {
        double num = 0;
        //可用
        QFilter qFilter = new QFilter("enable", QCP.equals, "1");
        //查询信息完整度校验配置表
        DynamicObject integrityModule = BusinessDataServiceHelper.loadSingle("tdkw_hspm_integrity", "id,tdkw_entryentity,tdkw_entryentity.tdkw_subentryentity,tdkw_subentryentity.tdkw_modulefield", qFilter.toArray());
        //模块分录
        DynamicObjectCollection entryEntity = integrityModule.getDynamicObjectCollection("tdkw_entryentity");
        for (DynamicObject entry : entryEntity) {
            //子分录
            DynamicObjectCollection integrityModules = entry.getDynamicObjectCollection("tdkw_subentryentity");
            if (integrityModules.size() != 0) {
                String[] modulefield = integrityModules.get(0).getString("tdkw_modulefield").split(",");
                num += modulefield.length - 1;
            }
        }
        return num;
    }


    /**
     * @Description 模块
     * @author xxx
     * @date 2023/6/6 19:27
     */
    public static List<String> integrityModule() {
        //可用
        QFilter qFilter = new QFilter("enable", QCP.equals, "1");
        //查询信息完整度校验配置表
        DynamicObject integrityModule = BusinessDataServiceHelper.loadSingle("tdkw_hspm_integrity", "id,tdkw_entryentity,tdkw_entryentity.tdkw_integritymodule", qFilter.toArray());
        //模块分录
        DynamicObjectCollection entryEntity = integrityModule.getDynamicObjectCollection("tdkw_entryentity");
        List<String> integrityModuleList = new ArrayList<>();
        for (DynamicObject entry : entryEntity) {
            //信息完整度校验模块值
            DynamicObject integrityModules = entry.getDynamicObject("tdkw_integritymodule");
            integrityModuleList.add(integrityModules.getString("name"));
        }
        return integrityModuleList;
    }

    /**
     * @Description 字段
     * @author xxx
     * @date 2023/6/6 19:28
     */
    public static List<String> moduleField(String moduleStr, DynamicObject person) {
        List<String> moduleFieldList = new ArrayList<>();
        //可用
        QFilter qFilter = new QFilter("enable", QCP.equals, "1");
        //查询信息完整度校验配置表
        DynamicObject integrityModule = BusinessDataServiceHelper.loadSingle("tdkw_hspm_integrity", "id,tdkw_entryentity,tdkw_entryentity.tdkw_integritymodule,tdkw_entryentity.tdkw_subentryentity,tdkw_subentryentity.tdkw_modulefield", qFilter.toArray());
        //模块分录
        DynamicObjectCollection entryEntity = integrityModule.getDynamicObjectCollection("tdkw_entryentity");
        for (DynamicObject entry : entryEntity) {
            //信息完整度校验模块值
            DynamicObject integrityModules = entry.getDynamicObject("tdkw_integritymodule");
            if (!ObjectUtils.isEmpty(integrityModules)) {
                //模块单据标识
                String name = integrityModules.getString("name");
                if (moduleStr.equals(name)) {
                    //根据表单编码获取表单id
                    String id = MetadataDao.getIdByNumber(integrityModules.getString("number"), MetaCategory.Form);
                    //获取表单元数据
                    FormMetadata formMeta = (FormMetadata) MetadataDao.readRuntimeMeta(id, MetaCategory.Form);
                    //获取所有控件集合
                    List<ControlAp<?>> items = formMeta.getItems();
                    Map<String, String> map = new HashMap<>();
                    for (ControlAp<?> item : items) {
                        map.put(item.getKey(), item.getName().getLocaleValue());
                    }
                    //字段信息分录
                    DynamicObjectCollection subentryEntity = entry.getDynamicObjectCollection("tdkw_subentryentity");
                    for (DynamicObject dynamicObject : subentryEntity) {
                        //查询配置表对应字段数据是否存在
                        String moduleField[] = dynamicObject.getString("tdkw_modulefield").split(",");
                        //模块信息分录
                        QFilter entryQfilter = new QFilter("person", QCP.equals, person.getPkValue());
                        entryQfilter.and("datastatus", QCP.equals, "1");
                        entryQfilter.and("initstatus", QCP.equals, "2");
                        entryQfilter.and("iscurrentversion", QCP.equals, "1");
                        //模块单据标识
                        String number = integrityModules.getString("number");
                        Map<String, String> strMap = new HashMap<>();
                        DynamicObject[] entryObject = BusinessDataServiceHelper.load(number, dynamicObject.getString("tdkw_modulefield"), entryQfilter.toArray());
                        for (DynamicObject object : entryObject) {
                            //是否存在该数据
                            for (String module : moduleField) {
                                boolean isExist = true;
                                if (StringUtils.isNotEmpty(module)) {
                                    if ("degree".equals(module)) {
                                        isExist = notInStock(module, person);
                                    } else {
                                        if (ObjectUtils.isEmpty(object.get(module))) {
                                            isExist = false;
                                        }
                                    }
                                    //不存在则计算
                                    if (!isExist) {
                                        if (map.get(module) != null) {
                                            strMap.put(map.get(module), map.get(module));
                                        }
                                    }
                                }
                            }
                        }
                        Set<String> strings = strMap.keySet();
                        for (String string : strings) {
                            moduleFieldList.add(string);
                        }
                        if (entryObject.length == 0) {
                            //是否存在该数据
                            for (String module : moduleField) {
                                if (map.get(module) != null) {
                                    moduleFieldList.add(map.get(module));
                                }
                            }
                        }
                    }
                }
            }
        }
        return moduleFieldList;
    }

    /**
     * @Description 表单未落库字段
     * @author xxx
     * @date 2023/7/12 20:41
     */
    public static boolean notInStock(String number, DynamicObject person) {
        boolean isExist = false;
        if ("degree".equals(number)) {
            //1010_S	博士研究生
            String doctoral = System.getProperty("constant.hr.hbss.hbss_diploma.doctoral");
            if ("".equals(doctoral) || doctoral == null) {
                doctoral = "1010_S";
            }
            //1020_S	硕士研究生
            String master = System.getProperty("constant.hr.hbss.hbss_diploma.master");
            if ("".equals(master) || master == null) {
                master = "1020_S";
            }
            //1030_S	本科
            String underGraduate = System.getProperty("constant.hr.hbss.hbss_diploma.undergraduate");
            if ("".equals(underGraduate) || underGraduate == null) {
                underGraduate = "1030_S";
            }
            //人员时序性
            QFilter pertspropQfilter = new QFilter("person", QCP.equals, person.getPkValue());
            pertspropQfilter.and("datastatus", QCP.equals, "1");
            pertspropQfilter.and("initstatus", QCP.equals, "2");
            pertspropQfilter.and("iscurrentversion", QCP.equals, "1");
            String selectProperties = "id,education," + number;
            DynamicObject pertsprop = BusinessDataServiceHelper.loadSingle("hspm_pereduexpinfo", selectProperties, pertspropQfilter.toArray());
            if (pertsprop != null) {
                if (!ObjectUtils.isEmpty(pertsprop.getDynamicObject("education"))) {
                    String string = pertsprop.getDynamicObject("education").getString("number");
                    if (doctoral.equals(string) || master.equals(string) || doctoral.equals(underGraduate)) {
                        if (pertsprop.get(number) != null) {
                            //学历
                            isExist = true;
                        }
                    } else {
                        //学历
                        isExist = true;
                    }
                }
            }
        }
        return isExist;

    }

    /**
     * @Description 2.5. 信息完整度校验-特殊规则（完整条数）
     * @author xxx
     * @date 2023/5/17 16:53
     */
    public static List<String> specialRule(DynamicObject person) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> SpecialRuleList = new ArrayList<>();
        //基本信息
        QFilter personInfoQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        personInfoQfilter.and("datastatus", QCP.equals, "1");
        personInfoQfilter.and("initstatus", QCP.equals, "2");
        personInfoQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject personInfo = BusinessDataServiceHelper.loadSingle("hspm_personinfo", "id," +
                "tdkw_progressbar,gender,beginservicedate,marriagestatus,birthday,age,gender,tdkw_domainaccount,headsculpture,politicalstatus," +
                "joinpartydate", personInfoQfilter.toArray());

        //任职经历
        QFilter empposorgrelQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        //任职类型 = 全职任职（1010_S）
        String poststateall = System.getProperty("constant.hr.hbss.hbss_postype.poststateall");
        if ("".equals(poststateall) || poststateall == null) {
            poststateall = "XY00001";
        }
        empposorgrelQfilter.and("postype.number", QCP.equals, poststateall);
        empposorgrelQfilter.and("datastatus", QCP.equals, "1");
        empposorgrelQfilter.and("initstatus", QCP.equals, "2");
        empposorgrelQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] empposorgrels = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id," +
                "number,enddate,startdate", empposorgrelQfilter.toArray());

        //工作开始日期
        Date startDate = null;

        //教育经历
        QFilter pereduExpinQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        pereduExpinQfilter.and("datastatus", QCP.equals, "1");
        pereduExpinQfilter.and("initstatus", QCP.equals, "2");
        pereduExpinQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] pereduExpins = BusinessDataServiceHelper.load("hspm_pereduexpinfo", "id," +
                "number,education,ishighestdegree,gradutiondate,admissiondate,edunature,tdkw_jobhighesteducation,tdkw_highestdegree", pereduExpinQfilter.toArray());
        //最高学历
        DynamicObject education = null;
        //1010_S	博士研究生
        String doctoral = System.getProperty("constant.hr.hbss.hbss_diploma.doctoral");
        if ("".equals(doctoral) || doctoral == null) {
            doctoral = "1010_S";
        }
        //1020_S	硕士研究生
        String master = System.getProperty("constant.hr.hbss.hbss_diploma.master");
        if ("".equals(master) || master == null) {
            master = "1020_S";
        }
        //1030_S	本科
        String underGraduate = System.getProperty("constant.hr.hbss.hbss_diploma.undergraduate");
        if ("".equals(underGraduate) || underGraduate == null) {
            underGraduate = "1030_S";
        }
        //1040_S	大专
        String junior = System.getProperty("constant.hr.hbss.hbss_diploma.junior");
        if ("".equals(junior) || junior == null) {
            junior = "1040_S";
        }
        //1050_S	高中
        String high = System.getProperty("constant.hr.hbss.hbss_diploma.high");
        if ("".equals(high) || high == null) {
            high = "1050_S";
        }
        //1060_S	中专
        String technical = System.getProperty("constant.hr.hbss.hbss_diploma.technical");
        if ("".equals(technical) || technical == null) {
            technical = "1060_S";
        }
        //初中
        String highSchools = returnStr("constant.hr.hspm.hspm_personinfo.junior", "1070_S");
        //是否存在相关学历
        boolean isDoctoral = false, isMaster = false, isUnderGraduate = false, isJunior = false, isHigh = false, isTechnical = false;

        //家庭人员表单
        QFilter familyMembQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        familyMembQfilter.and("datastatus", QCP.equals, "1");
        familyMembQfilter.and("initstatus", QCP.equals, "2");
        familyMembQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] familyMembs = BusinessDataServiceHelper.load("hspm_familymemb", "id,familymembship,tdkw_dateofbirth", familyMembQfilter.toArray());
        Date birthday = null;
        //参加工作日期
        Date beginServiceDate = null;
        //性别
        String genderType = "";
        //婚姻状况
        DynamicObject marriageStatus = null;
        //基本信息不为空
        if (!ObjectUtils.isEmpty(personInfo)) {
            //参加工作日期
            beginServiceDate = personInfo.getDate("beginservicedate");
            //性別
            DynamicObject gender = personInfo.getDynamicObject("gender");
            if (!ObjectUtils.isEmpty(gender)) {
                genderType = gender.getString("name");
            }
            //婚姻状况
            marriageStatus = personInfo.getDynamicObject("marriagestatus");
            //人员时序性
            QFilter pertspropQfilter = new QFilter("person", QCP.equals, person.getPkValue());
            pertspropQfilter.and("datastatus", QCP.equals, "1");
            pertspropQfilter.and("initstatus", QCP.equals, "2");
            pertspropQfilter.and("iscurrentversion", QCP.equals, "1");
            DynamicObject pertsprop = BusinessDataServiceHelper.loadSingle("hrpi_pertsprop", "id,marriagestatus", pertspropQfilter.toArray());
            if (pertsprop != null) {
                marriageStatus = pertsprop.getDynamicObject("marriagestatus");
            }
            //出生日期
            birthday = personInfo.getDate("birthday");
            //政治面貌
            DynamicObject politicalStatus = personInfo.getDynamicObject("politicalstatus");
            //入党团时间
            Date joinPartyDate = personInfo.getDate("joinpartydate");
            //人员非时序性
            QFilter pernontspropQfilter = new QFilter("person", QCP.equals, person.getPkValue());
            pernontspropQfilter.and("datastatus", QCP.equals, "1");
            pernontspropQfilter.and("initstatus", QCP.equals, "2");
            pernontspropQfilter.and("iscurrentversion", QCP.equals, "1");
            String selectProperties = "id,politicalstatus,joinpartydate";
            DynamicObject perregion = BusinessDataServiceHelper.loadSingle("hrpi_perregion", selectProperties, pernontspropQfilter.toArray());
            if (perregion != null) {
                //政治面貌
                politicalStatus = perregion.getDynamicObject("politicalstatus");
                //入党团时间
                joinPartyDate = perregion.getDate("joinpartydate");
            }
            //1、如果政治面貌为群众（1130_S），则入党团时间可为空，此时无须稽核到简历完整度中，否则稽核到简历完整度  1
            if (!ObjectUtils.isEmpty(politicalStatus)) {
                //群众（1130_S）
                String masses = returnStr("constant.hr.hbss.hbss_politicalstatus.masses", "1130_S");
                if (!ObjectUtils.isEmpty(politicalStatus)) {
                    //政治面貌.编码
                    String number = politicalStatus.getString("number");
                    if (!(number.equals(masses)) && joinPartyDate == null) {
                        SpecialRuleList.add(ResManager.loadKDString("政治面貌不为群众，且入党团日期为空!", "IncompleteProjectDetailsCardPlugin_3", "tdkw-hr-hspm-formplugin-ext"));
                    }
                }
            } else {
                if (joinPartyDate == null) {
                    SpecialRuleList.add(ResManager.loadKDString("政治面貌不为群众，且入党团日期为空!", "IncompleteProjectDetailsCardPlugin_3", "tdkw-hr-hspm-formplugin-ext"));
                }
            }
        } else {
            SpecialRuleList.add(ResManager.loadKDString("政治面貌不为群众，且入党团日期为空!", "IncompleteProjectDetailsCardPlugin_3", "tdkw-hr-hspm-formplugin-ext"));
        }

        //户口所在地
        String domicile = returnStr("constant.hr.hbss.hbss_addresstype.domicile", "1020_S");
        boolean isDomicile = true;
        //家庭住址
        String home = returnStr("constant.hr.hbss.hbss_addresstype.home", "XY00002");
        boolean isHome = true;
        //常住地址
        String permanent = returnStr("constant.hr.hbss.hbss_addresstype.permanent", "XY00003");
        boolean isPermanent = true;
        //人员地址信息
        QFilter peraddressQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        peraddressQfilter.and("datastatus", QCP.equals, "1");
        peraddressQfilter.and("initstatus", QCP.equals, "2");
        peraddressQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] peraddress = BusinessDataServiceHelper.load("hrpi_peraddress", "id,addresstype", peraddressQfilter.toArray());
        for (DynamicObject dynamicObject : peraddress) {
            //地址类型
            DynamicObject addresstype = dynamicObject.getDynamicObject("addresstype");
            if (!ObjectUtils.isEmpty(addresstype)) {
                if (home.equals(addresstype.getString("number"))) {
                    //家庭住址
                    isHome = false;
                }
                if (domicile.equals(addresstype.getString("number"))) {
                    //户口所在地
                    isDomicile = false;
                }
                if (permanent.equals(addresstype.getString("number"))) {
                    //常住地址
                    isPermanent = false;
                }
            }
        }
        if (isDomicile) {
            SpecialRuleList.add(ResManager.loadKDString("户口所在地未填写!", "IncompleteProjectDetailsCardPlugin_13", "tdkw-hr-hspm-formplugin-ext"));
        }
        if (isHome) {
            SpecialRuleList.add(ResManager.loadKDString("家庭住址未填写!", "IncompleteProjectDetailsCardPlugin_14", "tdkw-hr-hspm-formplugin-ext"));
        }
        if (isPermanent) {
            SpecialRuleList.add(ResManager.loadKDString("常住地址未填写!", "IncompleteProjectDetailsCardPlugin_15", "tdkw-hr-hspm-formplugin-ext"));
        }

        //2、①任职经历若存在开始时间断档，则稽核到简历完整度；②任职经历若存在中途时间断档，则稽核到简历完整度；③参加工作时间异常  3

        //是否存在任职经历
        for (int i = 0; i < empposorgrels.length; i++) {
            DynamicObject empposorgrel = empposorgrels[i];
            //开始日期
            Date startDates = empposorgrel.getDate("startdate");
            if (startDate == null) {
                startDate = startDates;
            } else if (startDate.after(startDates)) {
                startDate = startDates;
            }
        }
        //前工作经历
        QFilter preworkexpQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        preworkexpQfilter.and("datastatus", QCP.equals, "1");
        preworkexpQfilter.and("initstatus", QCP.equals, "2");
        preworkexpQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] preworkexp = BusinessDataServiceHelper.load("hrpi_preworkexp", "trade,id,startdate,unitname,position,startdate,enddate", preworkexpQfilter.toArray());

        //职业信息
        QFilter empentrelQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        empentrelQfilter.and("datastatus", QCP.equals, "1");
        empentrelQfilter.and("initstatus", QCP.equals, "2");
        empentrelQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject empentrel = BusinessDataServiceHelper.loadSingle("hrpi_empentrel", "id,laborreltype", empentrelQfilter.toArray());
        //是否是实习生
        boolean isLaborrelType = false;
        //用工关系类型
        if (!ObjectUtils.isEmpty(empentrel)) {
            DynamicObject laborrelType = empentrel.getDynamicObject("laborreltype");
            //实习生
            String intern = returnStr("constant.hr.hom.laborreltype.intern", "XY00004");
            if (!ObjectUtils.isEmpty(laborrelType)) {
                if (intern.equals(laborrelType.getString("number"))) {
                    isLaborrelType = true;
                }
            }
        }
        //系统外任职经历
        QFilter emporgreloutQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        emporgreloutQfilter.and("datastatus", QCP.equals, "1");
        emporgreloutQfilter.and("initstatus", QCP.equals, "2");
        emporgreloutQfilter.and("iscurrentversion", QCP.equals, "1");
        //emporgreloutQfilter.and("businessstatus", QCP.equals, "1");
        //“任职类型”=“XY00005-XXX内任职"
        String externalEmployment = returnStr("constant.hr.hbss.hbss_employmenttype.externalEmployment", "XY00005");
        emporgreloutQfilter.and("tdkw_postypeoutside.number", QCP.equals, externalEmployment);
        DynamicObject[] emporgrelout = BusinessDataServiceHelper.load("hrpi_emporgrelout", "id,startdate,enddate,laborreltype", emporgreloutQfilter.toArray());

        //任职经历 开始、结束日期集合
        Map<Integer, List<Date>> map = new HashMap<>();
        //②任职经历若存在中途时间断档，则稽核到简历完整度；
        for (int i = 0; i < empposorgrels.length; i++) {
            //开始、结束日期集合
            List<Date> list = new ArrayList<>();
            DynamicObject empposorgrel = empposorgrels[i];
            //开始日期
            Date startDates = empposorgrel.getDate("startdate");
            if (startDates == null) {
                list.add(format.parse("2999-12-31"));
            } else {
                list.add(startDates);
            }
            //结束日期
            Date endDates = empposorgrel.getDate("enddate");
            if (endDates == null) {
                list.add(format.parse("2999-12-31"));
            } else {
                list.add(endDates);
            }
            map.put(i, list);
        }
        //工作开始时间（排除待业）
        Date waitStartDate = startDate;
        for (int i = empposorgrels.length; i < preworkexp.length + empposorgrels.length; i++) {
            //开始、结束日期集合
            List<Date> list = new ArrayList<>();
            DynamicObject prework = preworkexp[i - empposorgrels.length];
            //开始日期
            Date startDates = prework.getDate("startdate");
            if (startDates == null) {
                list.add(format.parse("2999-12-31"));
            } else {
                list.add(startDates);
            }
            //从事行业
            DynamicObject trade = prework.getDynamicObject("trade");
            //待业
            String wait = returnStr("constant.hr.hbss.hbss_industrytype.wait", "99");
            if (startDates != null && startDate != null) {
                if (!ObjectUtils.isEmpty(trade)) {
                    LOGGER.info(person.getString("number") + "前工作经历从事行业编码：" + trade.getString("number"));
                    if (!wait.equals(trade.getString("number"))) {
                        //工作开始时间
                        if (startDates.before(waitStartDate)) {
                            waitStartDate = startDates;
                        }
                    }
                }else{
                    //工作开始时间
                    if (startDates.before(waitStartDate)) {
                        waitStartDate = startDates;
                    }
                }
                //工作开始时间
                if (startDates.before(startDate)) {
                    startDate = startDates;
                }
            }
            //结束日期
            Date endDates = prework.getDate("enddate");
            if (endDates == null) {
                list.add(format.parse("2999-12-31"));
            } else {
                list.add(endDates);
            }
            map.put(i, list);
        }
        for (int i = preworkexp.length + empposorgrels.length; i < preworkexp.length + empposorgrels.length + emporgrelout.length; i++) {
            //开始、结束日期集合
            List<Date> list = new ArrayList<>();
            DynamicObject emporgrel = emporgrelout[i - preworkexp.length - empposorgrels.length];
            //开始日期
            Date startDates = emporgrel.getDate("startdate");
            if (startDates == null) {
                list.add(format.parse("2999-12-31"));
            } else {
                list.add(startDates);
            }
            if (startDates != null && startDate != null) {
                //工作开始时间
                if (startDates.before(startDate)) {
                    startDate = startDates;
                }
                //工作开始时间
                if (startDates.before(waitStartDate)) {
                    waitStartDate = startDates;
                }
            }
            //结束日期
            Date endDates = emporgrel.getDate("enddate");
            if (endDates == null) {
                list.add(format.parse("2999-12-31"));
            } else {
                list.add(endDates);
            }
            map.put(i, list);
        }
        //排序
        Map<Integer, List<Date>> maps = new HashMap<>();
        for (int i = 0; i < map.size(); i++) {
            int num = map.size();
            for (int j = 0; j < map.size(); j++) {
                if (map.get(i).get(0) != null && map.get(j).get(0) != null) {
                    if (!map.get(i).get(0).after(map.get(j).get(0))) {
                        num -= 1;
                    }
                }
            }
            if (maps.get(num) != null) {
                maps.put(num + 1, map.get(i));
            } else {
                maps.put(num, map.get(i));
            }
        }
        LOGGER.info(person.getString("number") + ",履历任职经历正序maps:" + maps);
        boolean stoppage = false;
        if (maps.size() > 1) {
            for (int i = 0; i < maps.size() - 1; i++) {
                if (maps.get(i) != null && maps.get(i + 1) != null) {
                    if (maps.get(i).get(1) != null && maps.get(i + 1).get(0) != null) {
                        int days = differentDaysByMillisecond(maps.get(i).get(1), maps.get(i + 1).get(0));
                        if (days > 31) {
                            stoppage = true;
                            LOGGER.info(person.getString("number") + ",履历任职经历正序map断档key" + i + ",断档天数：" + days);
                        }
                    }
                }
            }
        }
        if (stoppage && !isLaborrelType) {
            SpecialRuleList.add(ResManager.loadKDString("履历存在中途时间断档!", "IncompleteProjectDetailsCardPlugin_5", "tdkw-hr-hspm-formplugin-ext"));
        }
        //①任职经历若存在开始时间断档，则稽核到简历完整度；
        if (startDate != null) {
            //取毕业时间
            Date gradutionDates = null;
            for (DynamicObject pereduExpin : pereduExpins) {
                //毕业时间
                Date gradutionDate = pereduExpin.getDate("gradutiondate");
                //全日制最高学历
                boolean isHighestDegree = pereduExpin.getBoolean("tdkw_highestdegree");
                if (!isHighestDegree) {
                    continue;
                }
                if (gradutionDate != null) {
                    if (gradutionDates == null) {
                        gradutionDates = gradutionDate;
                    } else if (gradutionDate != null) {
                        if (gradutionDates.before(gradutionDate)) {
                            gradutionDates = gradutionDate;
                        }
                    }
                }
            }
            //履历信息中，最高全日制教育结束日期超过31天没履历记录,则存在存在开始时间断档
            if (gradutionDates != null) {
                int days = differentDaysByMillisecond(gradutionDates, startDate);
                if (days > 31 && !isLaborrelType) {
                    SpecialRuleList.add(ResManager.loadKDString("履历存在开始时间断档!", "IncompleteProjectDetailsCardPlugin_4", "tdkw-hr-hspm-formplugin-ext"));
                }
            }
            //服务年限
            QFilter perserlenQfilter = new QFilter("person", QCP.equals, person.getPkValue());
            perserlenQfilter.and("datastatus", QCP.equals, "1");
            perserlenQfilter.and("initstatus", QCP.equals, "2");
            perserlenQfilter.and("iscurrentversion", QCP.equals, "1");
            DynamicObject perserlen = BusinessDataServiceHelper.loadSingle("hrpi_perserlen", "id,joinworktime", perserlenQfilter.toArray());
            if (!ObjectUtils.isEmpty(perserlen)) {
                beginServiceDate = perserlen.getDate("joinworktime");
            }
            //③参加工作时间异常
            if (waitStartDate != null) {
                if (beginServiceDate != null) {
                    if (!format.format(beginServiceDate).equals(format.format(waitStartDate)) && !isLaborrelType) {
                        LOGGER.info(person.getString("name") + "参加工作时间：" + format.format(beginServiceDate) + ",实际工作开始时间：" + format.format(startDate)+",实际工作开始时间（待业）：" +format.format(waitStartDate)+ ",是否不是实习生：" + !isLaborrelType);
                        SpecialRuleList.add(ResManager.loadKDString("首段正式任职开始时间与参加工作时间不一致!", "IncompleteProjectDetailsCardPlugin_6", "tdkw-hr-hspm-formplugin-ext"));
                    }
                }
            }
            if (waitStartDate == null || beginServiceDate == null && !isLaborrelType) {
                SpecialRuleList.add(ResManager.loadKDString("首段正式任职开始时间与参加工作时间不一致!", "IncompleteProjectDetailsCardPlugin_6", "tdkw-hr-hspm-formplugin-ext"));
            }
        } else {
            if (!isLaborrelType) {
                SpecialRuleList.add(ResManager.loadKDString("履历存在开始时间断档!", "IncompleteProjectDetailsCardPlugin_4", "tdkw-hr-hspm-formplugin-ext"));
                SpecialRuleList.add(ResManager.loadKDString("首段正式任职开始时间与参加工作时间不一致!", "IncompleteProjectDetailsCardPlugin_6", "tdkw-hr-hspm-formplugin-ext"));
            }
        }

        //3、①与本人关系中必须有父母亲录入；②已婚状态必须维护配偶；③已婚状态必须维护公婆/岳父母信息；④家庭成员出生年月异常  4
        //是否维护相关家庭关系
        boolean isSpouse = false, isFather = false, isMother = false, isFatherInLaw = false, isMotherInLaw = false;
        //家庭成员出生年月是否异常
        boolean outError = true;
        boolean wifeError = true;
        //1010_S	配偶
        String spouse = System.getProperty("constant.hr.hbss.hbss_familymemberrel.spouse");
        if ("".equals(spouse) || spouse == null) {
            spouse = "1010_S";
        }
        //1020_S	父亲
        String father = System.getProperty("constant.hr.hbss.hbss_familymemberrel.father");
        if ("".equals(father) || father == null) {
            father = "1020_S";
        }
        //1030_S	母亲
        String mother = System.getProperty("constant.hr.hbss.hbss_familymemberrel.mother");
        if ("".equals(mother) || mother == null) {
            mother = "1030_S";
        }
        //          岳父
        String fatherInLaw = System.getProperty("constant.hr.hbss.hbss_familymemberrel.fatherinlaw");
        if ("".equals(fatherInLaw) || fatherInLaw == null) {
            fatherInLaw = "XY00003";
        }
        //          岳母
        String motherInLaw = System.getProperty("constant.hr.hbss.hbss_familymemberrel.motherinlaw");
        if ("".equals(motherInLaw) || motherInLaw == null) {
            motherInLaw = "XY00004";
        }
        //1020_S    已婚
        String married = System.getProperty("constant.hr.hbss.hbss_marriagestatus.married");
        if ("".equals(married) || married == null) {
            married = "1020_S";
        }
        //公公
        String grandpa = returnStr("constant.hr.hbss.hbss_familymemberrel.grandpa", "1120_S");
        //婆婆
        String grandma = returnStr("constant.hr.hbss.hbss_familymemberrel.grandma", "1130_S");
        //丈夫
        String husband = returnStr("constant.hr.hbss.hbss_familymemberrel.husband", "XY00007");
        //妻子
        String wife = returnStr("constant.hr.hbss.hbss_familymemberrel.wife", "XY00008");
        for (DynamicObject familyMemb : familyMembs) {
            //家庭成员关系
            DynamicObject familyMembship = familyMemb.getDynamicObject("familymembship");
            if (!ObjectUtils.isEmpty(familyMembship)) {
                //出生日期
                Date dateOfbirth = familyMemb.getDate("tdkw_dateofbirth");
                //家庭成员关系.编码
                String number = familyMembship.getString("number");
                //①与本人关系中必须有父母亲录入；
                if (father.equals(number)) {
                    isFather = true;
                    //2、家庭成员中父母不允许出现两方的出生年份都晚于本人
                    if (birthday != null && dateOfbirth != null) {
                        if (birthday.before(dateOfbirth)) {
                            outError = false;
                        }
                    }
                }
                if (mother.equals(number)) {
                    isMother = true;
                    //2、家庭成员中父母不允许出现两方的出生年份都晚于本人
                    if (birthday != null && dateOfbirth != null) {
                        if (birthday.before(dateOfbirth)) {
                            wifeError = false;
                        }
                    }
                }
                //②已婚状态必须维护配偶；
                if (!ObjectUtils.isEmpty(marriageStatus)) {
                    //婚姻状况.编码
                    String marriageStatusNumber = marriageStatus.getString("number");
                    if (married.equals(marriageStatusNumber)) {
                        if ("男".equals(genderType)) {
                            if (wife.equals(number)) {
                                isSpouse = true;
                            }
                            //③已婚状态必须维护岳父母信息；
                            if (fatherInLaw.equals(number)) {
                                isFatherInLaw = true;
                            }
                            if (motherInLaw.equals(number)) {
                                isMotherInLaw = true;
                            }
                        } else if ("女".equals(genderType)) {
                            if (husband.equals(number)) {
                                isSpouse = true;
                            }
                            //③已婚状态必须维护公婆信息；
                            if (grandpa.equals(number)) {
                                isFatherInLaw = true;
                            }
                            if (grandma.equals(number)) {
                                isMotherInLaw = true;
                            }
                        } else {
                            if (husband.equals(number) || wife.equals(number)) {
                                isSpouse = true;
                            }
                            if (fatherInLaw.equals(number) || grandpa.equals(number)) {
                                isFatherInLaw = true;
                            }
                            if (motherInLaw.equals(number) || grandma.equals(number)) {
                                isFatherInLaw = true;
                            }
                        }

                    } else {
                        isMotherInLaw = true;
                        isFatherInLaw = true;
                    }
                }
                //④家庭成员出生年月异常
                Date isDate = format.parse("1899-12-31");
                //1、家庭成员出生年份限制从1900年后起填
                if (dateOfbirth != null) {
                    if (dateOfbirth.before(isDate)) {
                        outError = false;
                        wifeError = false;
                    }
                }
            }
        }
        //婚姻状况编码
        String marriageStatusNumber = "";
        if (!ObjectUtils.isEmpty(marriageStatus)) {
            marriageStatusNumber = marriageStatus.getString("number");
        }

        if (familyMembs.length == 0) {
            SpecialRuleList.add(ResManager.loadKDString("家庭成员出生年月异常!", "IncompleteProjectDetailsCardPlugin_7", "tdkw-hr-hspm-formplugin-ext"));
            SpecialRuleList.add(ResManager.loadKDString("与本人关系中无父母亲录入!", "IncompleteProjectDetailsCardPlugin_8", "tdkw-hr-hspm-formplugin-ext"));
            if (married.equals(marriageStatusNumber)) {
                SpecialRuleList.add(ResManager.loadKDString("已婚状态未维护配偶!", "IncompleteProjectDetailsCardPlugin_9", "tdkw-hr-hspm-formplugin-ext"));
                SpecialRuleList.add(ResManager.loadKDString("已婚状态必须维护公婆/岳父母信息!", "IncompleteProjectDetailsCardPlugin_10", "tdkw-hr-hspm-formplugin-ext"));
            }
        } else {
            //④家庭成员出生年月异常
            if (!outError && !wifeError) {
                SpecialRuleList.add(ResManager.loadKDString("家庭成员出生年月异常!", "IncompleteProjectDetailsCardPlugin_7", "tdkw-hr-hspm-formplugin-ext"));
            }
            //①与本人关系中必须有父母亲录入
            if (!isFather || !isMother) {
                SpecialRuleList.add(ResManager.loadKDString("与本人关系中无父母亲录入!", "IncompleteProjectDetailsCardPlugin_8", "tdkw-hr-hspm-formplugin-ext"));
            }
            //②已婚状态必须维护配偶
            if (!isSpouse && married.equals(marriageStatusNumber)) {
                SpecialRuleList.add(ResManager.loadKDString("已婚状态未维护配偶!", "IncompleteProjectDetailsCardPlugin_9", "tdkw-hr-hspm-formplugin-ext"));
            }
            //③已婚状态必须维护公婆/岳父母信息
            if ((!isFatherInLaw || !isMotherInLaw) && married.equals(marriageStatusNumber)) {
                SpecialRuleList.add(ResManager.loadKDString("已婚状态必须维护公婆/岳父母信息!", "IncompleteProjectDetailsCardPlugin_10", "tdkw-hr-hspm-formplugin-ext"));
            }
        }

        //4、高中以上学历填写后，必须维护基础学历（高中、大专、本科、研究生），需稽核到简历完整度 1
        for (int i = 0; i < pereduExpins.length; i++) {
            DynamicObject pereduExpin = pereduExpins[i];
            //是否最高学历
            String isHighestDegree = pereduExpin.getString("ishighestdegree");
            if ("1".equals(isHighestDegree)) {
                education = pereduExpin.getDynamicObject("education");
            }
        }
        //初中
        boolean highSchool = false;
        //大专毕业时间
        Date juniorEndDate = null;
        //大专入学时间
        Date juniorStartDate = null;
        //研究生入学时间
        Date graduateStartDate = null;
        //学习方式-全国普通高等院校全日制
        String isfull = returnStr("constant.hr.hom.hbss_diplomatype.fullTime", "XY00001");
        //大专全日制
        String isfullTime = "";
        //博士在读时间
        int DoctoralTime = 0;
        for (DynamicObject pereduExpin : pereduExpins) {
            //学历
            DynamicObject educations = pereduExpin.getDynamicObject("education");
            if (!ObjectUtils.isEmpty(educations)) {
                //最高学历.编码
                String educationNumber = educations.getString("number");
                //博士研究生
                if (doctoral.equals(educationNumber)) {
                    isDoctoral = true;
                    //博士在读时间
                    Date DoctoralEndDate = pereduExpin.getDate("gradutiondate");
                    Date DoctoralStartDate = pereduExpin.getDate("admissiondate");
                    if (DoctoralStartDate != null && DoctoralEndDate != null) {
                        int times = differentDaysByMillisecond(DoctoralEndDate, DoctoralStartDate);
                        DoctoralTime = Math.abs(times);
                    }
                }
                //硕士研究生
                if (master.equals(educationNumber)) {
                    isMaster = true;
                    graduateStartDate = pereduExpin.getDate("admissiondate");
                }
                //本科
                if (underGraduate.equals(educationNumber)) {
                    isUnderGraduate = true;
                }
                //大专
                if (junior.equals(educationNumber)) {
                    isJunior = true;
                    juniorEndDate = pereduExpin.getDate("gradutiondate");
                    juniorStartDate = pereduExpin.getDate("admissiondate");
                    //全日制
                    if (!ObjectUtils.isEmpty(pereduExpin.getDynamicObject("edunature"))) {
                        isfullTime = pereduExpin.getDynamicObject("edunature").getString("number");
                    }
                }
                //高中
                if (high.equals(educationNumber)) {
                    isHigh = true;
                }
                //中专
                if (technical.equals(educationNumber)) {
                    isTechnical = true;
                }
                //初中
                if (highSchools.equals(educationNumber)) {
                    highSchool = true;
                }
            }
        }
        //高中以上学历填写后，未维护基础学历!
        boolean disqualIfication = true;
        if (!ObjectUtils.isEmpty(education)) {
            //最高学历.编码
            String educationNumber = education.getString("number");
            //博士研究生
            if (doctoral.equals(educationNumber)) {
                if (graduateStartDate != null && juniorEndDate != null) {
                    int times = differentDaysByMillisecond(graduateStartDate, juniorEndDate);
                    if (Math.abs(times) > 730) {
                        if (isMaster && (isHigh || isTechnical)) {
                            disqualIfication = false;
                        }
                    }
                }
                if (juniorStartDate != null && juniorEndDate != null) {
                    int times = differentDaysByMillisecond(juniorStartDate, juniorEndDate);
                    if (Math.abs(times) > (4 * 365)) {
                        if (isMaster && isUnderGraduate) {
                            disqualIfication = false;
                        }
                    }
                }
                if (DoctoralTime > (4 * 365)) {
                    if (isUnderGraduate && (isHigh || isTechnical)) {
                        disqualIfication = false;
                    }
                }
                if (StringUtils.isNotEmpty(isfullTime)) {
                    if (!isfull.equals(isfullTime) && highSchool) {
                        if (isMaster && isUnderGraduate) {
                            disqualIfication = false;
                        }
                    }
                }
                if (isMaster && isUnderGraduate && (isHigh || isTechnical)) {
                    disqualIfication = false;
                }

            }
            //硕士研究生
            if (master.equals(educationNumber)) {
                if (graduateStartDate != null && juniorEndDate != null) {
                    int times = differentDaysByMillisecond(graduateStartDate, juniorEndDate);
                    if (Math.abs(times) > 730) {
                        if ((isHigh || isTechnical)) {
                            disqualIfication = false;
                        }
                    }
                }
                if (juniorStartDate != null && juniorEndDate != null) {
                    int times = differentDaysByMillisecond(juniorStartDate, juniorEndDate);
                    if (Math.abs(times) > (4 * 365)) {
                        if (isUnderGraduate) {
                            disqualIfication = false;
                        }
                    }
                }
                if (StringUtils.isNotEmpty(isfullTime)) {
                    if (!isfull.equals(isfullTime) && highSchool) {
                        if (isUnderGraduate) {
                            disqualIfication = false;
                        }
                    }
                }

                if (isUnderGraduate && (isHigh || isTechnical)) {
                    disqualIfication = false;
                }


            }
            //本科
            if (underGraduate.equals(educationNumber)) {
                if (juniorStartDate != null && juniorEndDate != null) {
                    int times = differentDaysByMillisecond(juniorStartDate, juniorEndDate);
                    if (Math.abs(times) > (4 * 365)) {
                        disqualIfication = false;
                    }
                }
                if (StringUtils.isNotEmpty(isfullTime)) {
                    if (!isfull.equals(isfullTime) && highSchool) {
                        disqualIfication = false;
                    }
                }
                if ((isHigh || isTechnical)) {
                    disqualIfication = false;
                }
            }
            //大专
            if (junior.equals(educationNumber)) {
                if (juniorStartDate != null && juniorEndDate != null) {
                    int times = differentDaysByMillisecond(juniorStartDate, juniorEndDate);
                    if (Math.abs(times) > (4 * 365)) {
                        disqualIfication = false;
                    }
                }
                if (StringUtils.isNotEmpty(isfullTime)) {
                    if (!isfull.equals(isfullTime) && highSchool) {
                        disqualIfication = false;
                    }
                }
                if ((isHigh || isTechnical)) {
                    disqualIfication = false;
                }
            }
            if (!doctoral.equals(educationNumber) && !master.equals(educationNumber) && !underGraduate.equals(educationNumber) && !junior.equals(educationNumber)) {
                disqualIfication = false;
            }
        }
        if (disqualIfication) {
            SpecialRuleList.add(ResManager.loadKDString("高中以上学历填写后，未维护基础学历!", "IncompleteProjectDetailsCardPlugin_11", "tdkw-hr-hspm-formplugin-ext"));
        }

        //在职教育
        String inServiceEducation = returnStr("constant.hr.hbss.hbss_diplomatype.inServiceEducation", "XY00010");
        //全国普通高等院校非全日制统招
        String nurecruitment = returnStr("constant.hr.hbss.hbss_diplomatype.NUrecruitment", "JYXS_0201");
        //非全日制网络教育
        String nuonlineeducation = returnStr("constant.hr.hbss.hbss_diplomatype.NUOnlineEducation", "JYXS_0301");
        //非全日制成人高考
        String nuadult = returnStr("constant.hr.hbss.hbss_diplomatype.NUadult", "JYXS_0302");
        //非全日制国家开放大学
        String nuopenuniversity = returnStr("constant.hr.hbss.hbss_diplomatype.NUOpenUniversity", "JYXS_0303");
        //非全日制自学考试
        String nuselfstudy = returnStr("constant.hr.hbss.hbss_diplomatype.NUselfStudy", "JYXS_0304");
        //非国民教育(党校|社科院等)
        String nurepublicchina = returnStr("constant.hr.hbss.hbss_diplomatype.NURepublicChina", "JYXS_0501");
        //5、存在学习方式为在职教育，但不存在在职最高学历
        boolean fiveIsTrue = false;
        boolean isInServiceEducation = false;
        for (DynamicObject pereduExpin : pereduExpins) {
            //学习方式
            DynamicObject edunaTure = pereduExpin.getDynamicObject("edunature");
            if (!ObjectUtils.isEmpty(edunaTure)) {
                //学习方式.编码
                String edunaTureNumber = edunaTure.getString("number");
                if (inServiceEducation.equals(edunaTureNumber) || nurecruitment.equals(edunaTureNumber) || nuonlineeducation.equals(edunaTureNumber)
                        || nuadult.equals(edunaTureNumber)
                        || nuopenuniversity.equals(edunaTureNumber) || nuselfstudy.equals(edunaTureNumber) || nurepublicchina.equals(edunaTureNumber)) {
                    isInServiceEducation = true;
                }
            }
            //在职最高学历
            if (pereduExpin.getBoolean("tdkw_jobhighesteducation")) {
                fiveIsTrue = true;
            }
        }
        if (isInServiceEducation && !fiveIsTrue) {
            SpecialRuleList.add(ResManager.loadKDString("存在学习方式为在职教育，但不存在在职最高学历!", "IncompleteProjectDetailsCardPlugin_12", "tdkw-hr-hspm-formplugin-ext"));
        }
        return SpecialRuleList;
    }

    /**
     * @Description mc配置编码
     * @author xxx
     * @date 2023/7/18 15:48
     */
    public static String returnStr(String strPro, String returnStr) {
        String returnStrs = System.getProperty(strPro);
        if ("".equals(returnStrs) || returnStrs == null) {
            returnStrs = returnStr;
        }
        return returnStrs;
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }
}
