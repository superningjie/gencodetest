package tdkw.hrmp.hrobs.formplugin.util;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.support.util.ObjectUtils;
import kd.bos.form.FormShowParameter;
import kd.bos.login.utils.StringUtils;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Metadata： XX
 * @Description ： XX
 * @ClassName ：InforProgress
 * @author xxx
 * @Date ：2023/7/18 19:09
 * @Version: 1.0
 */
public class InforProgress {
    /**
     * 获取信息完整度
     */
    public BigDecimal getInforProgress(Long personId) {

        BigDecimal pro = BigDecimal.ZERO;
        Boolean target = false;
        //方法一，和组织人事保持一致
        try {
            //特殊规则校验
            double num = SpecialRule(personId);
            //字段校验
            double percentageNum = integrityCheck(num, personId);
            pro = BigDecimal.valueOf(percentageNum * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
            //判断当前员是否在白名单上
            if (whiteList(personId)) {
                pro = BigDecimal.valueOf(100).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        } catch (Exception e) {
            target = true;
        }
        //方法二：从基础资料上取
        if (target) {
            QFilter qFilter = new QFilter("boid", QCP.equals, personId);
            qFilter.and("iscurrentversion", QCP.equals, true);
            DynamicObject person = BusinessDataServiceHelper.loadSingle("hrpi_person", "id", qFilter.toArray());
            if (person != null) {
                //信息完成进度
                QFilter qFilter1 = new QFilter("datastatus", QCP.equals, "1");
                qFilter1.and("initstatus", QCP.equals, "2");
                qFilter1.and("iscurrentversion", QCP.equals, "1");
                qFilter1.and("person", QCP.equals, person.getLong("id"));
                DynamicObject personinfo = BusinessDataServiceHelper.loadSingle("hspm_personinfo", "tdkw_progressbar", qFilter1.toArray());
                if (personinfo != null) {
                    pro = personinfo.getBigDecimal("tdkw_progressbar");
                }
            }
            pro = pro.setScale(2, BigDecimal.ROUND_HALF_UP);//pro.multiply(new BigDecimal("100")).setScale(2)
        }

        return pro;
    }


    private Map<String, Long> getPersonModelId() {
        Map<String, Object> personModelIdMap = (Map) HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonService", "getPersonModelId", new Object[0]);
        Map<String, Long> personModelInfo = personModelIdMap != null ? (Map) personModelIdMap.get("data") : null;
        return personModelInfo;
    }

    /**
     * @Description 2.5. 信息完整度校验-特殊规则（完整条数）
     * @author xxx
     * @date 2023/5/17 16:53
     */
    public double SpecialRule(Long personId) {
        //完整条数
        double progress = 0;

        //员工信息
        DynamicObject person = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
        //基本信息
        QFilter personInfoQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        personInfoQfilter.and("datastatus", QCP.equals, "1");
        personInfoQfilter.and("initstatus", QCP.equals, "2");
        personInfoQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject personInfo = BusinessDataServiceHelper.loadSingle("hspm_personinfo", "id", personInfoQfilter.toArray());

        //参加工作日期
        Date beginServiceDate = null;
        //婚姻状况
        DynamicObject marriageStatus = null;
        //出生日期
        Date birthday = null;
        if (personInfo != null) {
            personInfo = BusinessDataServiceHelper.loadSingle(personInfo.getPkValue(), "hspm_personinfo");
            //参加工作日期
            beginServiceDate = personInfo.getDate("beginservicedate");
            //婚姻状况
            marriageStatus = personInfo.getDynamicObject("marriagestatus");
            //出生日期
            birthday = personInfo.getDate("birthday");
        }

        //任职经历
        QFilter empposorgrelQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        //任职类型 = 全职任职（1010_S）
        empposorgrelQfilter.and("postype.number", QCP.equals, "1010_S");
        empposorgrelQfilter.and("datastatus", QCP.equals, "1");
        empposorgrelQfilter.and("initstatus", QCP.equals, "2");
        empposorgrelQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] empposorgrels = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,number", empposorgrelQfilter.toArray());
        //empposorgrels = toRemoval(empposorgrels, "number");

        //工作开始日期
        Date startDate = null;

        //教育经历
        QFilter pereduExpinQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        pereduExpinQfilter.and("datastatus", QCP.equals, "1");
        pereduExpinQfilter.and("initstatus", QCP.equals, "2");
        pereduExpinQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] pereduExpins = BusinessDataServiceHelper.load("hspm_pereduexpinfo", "id,number", pereduExpinQfilter.toArray());
        //最高学历
        DynamicObject education = null;
        //1010_S	博士研究生
        String doctoral = "1010_S";
        //1020_S	硕士研究生
        String master = "1020_S";
        //1030_S	本科
        String underGraduate = "1030_S";
        //1040_S	大专
        String junior = "1040_S";
        //1050_S	高中
        String high = "1050_S";
        //1060_S	中专
        String technical = "1060_S";
        //是否存在相关学历
        boolean isDoctoral = false, isMaster = false, isUnderGraduate = false, isJunior = false, isHigh = false, isTechnical = false;

        //家庭人员表单
        QFilter familyMembQfilter = new QFilter("person", QCP.equals, person.getPkValue());
        familyMembQfilter.and("datastatus", QCP.equals, "1");
        familyMembQfilter.and("initstatus", QCP.equals, "2");
        familyMembQfilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] familyMembs = BusinessDataServiceHelper.load("hspm_familymemb", "id,", familyMembQfilter.toArray());

        //基本信息不为空
        if (!ObjectUtils.isEmpty(personInfo)) {
            //政治面貌
            DynamicObject politicalStatus = personInfo.getDynamicObject("politicalstatus");
            //入党团时间
            Date joinPartyDate = personInfo.getDate("joinpartydate");
            //1、如果政治面貌为群众（1130_S），则入党团时间可为空，此时无须稽核到简历完整度中，否则稽核到简历完整度  1
            if (ObjectUtils.isEmpty(politicalStatus)) {
                //群众（1130_S）
                String masses = "1130_S";
                if (!ObjectUtils.isEmpty(politicalStatus)) {
                    //政治面貌.编码
                    String number = politicalStatus.getString("number");
                    if (number.equals(masses) && joinPartyDate == null) {
                        progress += 1;
                    }
                }
            }
        }

        //2、①任职经历若存在开始时间断档，则稽核到简历完整度；②任职经历若存在中途时间断档，则稽核到简历完整度；③参加工作时间异常  3

        //是否存在任职经历
        if (empposorgrels != null && empposorgrels.length != 0) {
            for (int i = 0; i < empposorgrels.length; i++) {
                DynamicObject empposorgrel = empposorgrels[i];
                empposorgrel = BusinessDataServiceHelper.loadSingle(empposorgrel.getPkValue(), "hrpi_empposorgrel");
                //开始日期
                Date startDates = empposorgrel.getDate("startdate");
                if (startDate == null) {
                    startDate = startDates;
                } else if (startDate.after(startDates)) {
                    startDate = startDates;
                }
            }
            //①任职经历若存在开始时间断档，则稽核到简历完整度；
            if (startDate != null) {
                //取毕业时间
                Date gradutionDates = null;
                for (DynamicObject pereduExpin : pereduExpins) {
                    pereduExpin = BusinessDataServiceHelper.loadSingle(pereduExpin.getPkValue(), "hspm_pereduexpinfo");
                    //毕业时间
                    Date gradutionDate = pereduExpin.getDate("gradutiondate");
                    if (gradutionDates == null) {
                        gradutionDates = gradutionDate;
                    } else if (gradutionDates.before(gradutionDate)) {
                        gradutionDates = gradutionDate;
                    }
                }
                //履历信息中，最高全日制教育结束日期超过31天没履历记录,则存在存在开始时间断档
                if (gradutionDates != null) {
                    int days = differentDaysByMillisecond(startDate, gradutionDates);
                    if (days < 31) {
                        progress += 1;
                    }
                }
            }


            //②任职经历若存在中途时间断档，则稽核到简历完整度；
            if (empposorgrels.length > 1) {
                //任职经历 开始、结束日期集合
                Map<Integer, List<Date>> map = new HashMap<>();
                for (int i = 0; i < empposorgrels.length; i++) {
                    //开始、结束日期集合
                    List<Date> list = new ArrayList<>();
                    DynamicObject empposorgrel = empposorgrels[i];
                    empposorgrel = BusinessDataServiceHelper.loadSingle(empposorgrel.getPkValue(), "hrpi_empposorgrel");
                    //开始日期
                    Date startDates = empposorgrel.getDate("startdate");
                    list.add(startDates);
                    //结束日期
                    Date endDates = empposorgrel.getDate("enddate");
                    list.add(endDates);
                    map.put(i, list);
                }
                //是否中途时间断档
                boolean isTrue = true;
                for (int i = 0; i < empposorgrels.length; i++) {
                    //满足条件数
                    int num = 0;
                    //开始日期
                    Date startDates = map.get(i).get(0);
                    if (startDates != null) {
                        for (int i1 = 0; i1 < empposorgrels.length; i1++) {
                            //结束日期
                            Date endDates = map.get(i).get(1);
                            if (endDates != null) {
                                int days = differentDaysByMillisecond(startDate, endDates);
                                if (days < 31) {
                                    num += 1;
                                }
                            }
                        }
                    }
                    if (num == 0) {
                        isTrue = false;
                    }
                }
                if (isTrue) {
                    progress += 1;
                }
            } else {
                progress += 1;
            }

            //③参加工作时间异常
            if (startDate != null) {
                if (beginServiceDate != null) {
                    if (beginServiceDate.after(startDate)) {
                        progress += 1;
                    }

                }
            }
        }

        //3、①与本人关系中必须有父母亲录入；②已婚状态必须维护配偶；③已婚状态必须维护公婆/岳父母信息；④家庭成员出生年月异常  4
        //是否维护相关家庭关系
        boolean isSpouse = false, isFather = false, isMother = false, isFatherInLaw = false, isMotherInLaw = false;
        //家庭成员出生年月是否异常
        boolean outError = true;
        for (DynamicObject familyMemb : familyMembs) {
            familyMemb = BusinessDataServiceHelper.loadSingle(familyMemb.getPkValue(), "hspm_familymemb");
            //家庭成员关系
            DynamicObject familyMembship = familyMemb.getDynamicObject("familymembship");
            //1010_S	配偶
            String spouse = "1010_S";
            //1020_S	父亲
            String father = "1020_S";
            //1030_S	母亲
            String mother = "1030_S";
            //          岳父
            String fatherInLaw = "XY00003";
            //          岳母
            String motherInLaw = "XY00004";
            //1020_S    已婚
            String married = "1020_S";
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
                        if (birthday.after(dateOfbirth)) {
                            outError = false;
                        }
                    }
                }
                if (mother.equals(number)) {
                    isMother = true;
                    //2、家庭成员中父母不允许出现两方的出生年份都晚于本人
                    if (birthday != null && dateOfbirth != null) {
                        if (birthday.after(dateOfbirth)) {
                            outError = false;
                        }
                    }
                }
                //②已婚状态必须维护配偶；
                if (!ObjectUtils.isEmpty(marriageStatus)) {
                    //婚姻状况.编码
                    String marriageStatusNumber = marriageStatus.getString("number");
                    if (married.equals(marriageStatusNumber)) {
                        if (spouse.equals(number)) {
                            isSpouse = true;
                        }
                    } else {
                        isSpouse = true;
                    }
                } else {
                    isSpouse = true;
                }
                //③已婚状态必须维护公婆/岳父母信息；
                if (fatherInLaw.equals(number) || "XY00005".equals(number)) {
                    isFatherInLaw = true;
                }
                if (motherInLaw.equals(number) || "XY00006".equals(number)) {
                    isMotherInLaw = true;
                }
                //④家庭成员出生年月异常
                Date isDate = new Date(1899 - 12 - 31);
                //1、家庭成员出生年份限制从1900年后起填
                if (dateOfbirth != null) {
                    if (dateOfbirth.after(isDate)) {
                        outError = false;
                    }
                }
            }
            //④家庭成员出生年月异常
            if (outError) {
                progress += 1;
            }
            //①与本人关系中必须有父母亲录入
            if (isFather && isMother) {
                progress += 1;
            }
            //②已婚状态必须维护配偶
            if (isSpouse) {
                progress += 1;
            }
            //③已婚状态必须维护公婆/岳父母信息
            if (isFatherInLaw && isMotherInLaw) {
                progress += 1;
            }
        }

        //4、高中以上学历填写后，必须维护基础学历（高中、大专、本科、研究生），需稽核到简历完整度 1
        for (int i = 0; i < pereduExpins.length; i++) {
            DynamicObject pereduExpin = pereduExpins[i];
            pereduExpin = BusinessDataServiceHelper.loadSingle(pereduExpin.getPkValue(), "hspm_pereduexpinfo");
            //是否最高学历
            String isHighestDegree = pereduExpin.getString("ishighestdegree");
            if ("1".equals(isHighestDegree)) {
                education = pereduExpin.getDynamicObject("education");
            }
        }
        for (DynamicObject pereduExpin : pereduExpins) {
            pereduExpin = BusinessDataServiceHelper.loadSingle(pereduExpin.getPkValue(), "hspm_pereduexpinfo");
            //学历
            DynamicObject educations = pereduExpin.getDynamicObject("education");
            if (!ObjectUtils.isEmpty(educations)) {
                //最高学历.编码
                String educationNumber = educations.getString("number");
                //博士研究生
                if (doctoral.equals(educationNumber)) {
                    isDoctoral = true;
                }
                //硕士研究生
                if (master.equals(educationNumber)) {
                    isMaster = true;
                }
                //本科
                if (underGraduate.equals(educationNumber)) {
                    isUnderGraduate = true;
                }
                //大专
                if (junior.equals(educationNumber)) {
                    isJunior = true;
                }
                //高中
                if (high.equals(educationNumber)) {
                    isHigh = true;
                }
                //中专
                if (technical.equals(educationNumber)) {
                    isTechnical = true;
                }
            }
        }
        if (!ObjectUtils.isEmpty(education)) {
            //最高学历.编码
            String educationNumber = education.getString("number");
            //博士研究生
            if (doctoral.equals(educationNumber)) {
                if (isMaster && isUnderGraduate && isJunior && (isHigh || isTechnical)) {
                    progress += 1;
                }
            }
            //硕士研究生
            if (master.equals(educationNumber)) {
                if (isUnderGraduate && isJunior && (isHigh || isTechnical)) {
                    progress += 1;
                }
            }
            //本科
            if (underGraduate.equals(educationNumber)) {
                if (isJunior && (isHigh || isTechnical)) {
                    progress += 1;
                }
            }
            //大专
            if (junior.equals(educationNumber)) {
                if ((isHigh || isTechnical)) {
                    progress += 1;
                }
            }
        }

        //在职教育
        String inServiceEducation = "XY00010";
        //5、存在学习方式为在职教育，但不存在在职最高学历
        boolean fiveIsTrue = false;
        boolean isInServiceEducation = false;
        for (DynamicObject pereduExpin : pereduExpins) {
            pereduExpin = BusinessDataServiceHelper.loadSingle(pereduExpin.getPkValue(), "hspm_pereduexpinfo");
            //学习方式
            DynamicObject edunaTure = pereduExpin.getDynamicObject("edunature");
            if (!ObjectUtils.isEmpty(edunaTure)) {
                //学习方式.编码
                String edunaTureNumber = edunaTure.getString("number");
                if (inServiceEducation.equals(edunaTureNumber)) {
                    isInServiceEducation = true;
                    if (education != null) {
                        fiveIsTrue = true;
                    }
                }
            }
        }
        if (!isInServiceEducation || fiveIsTrue) {
            progress += 1;
        }
        return progress;
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

    /**
     * @Description 字段校验
     * @author xxx
     * @date 2023/5/22 9:17
     */
    public double integrityCheck(double num, Long personId) {
        //完整条数
        double progress = num;
        //总数
        double total = 0;
        //可用
        QFilter qFilter = new QFilter("enable", QCP.equals, "1");
        //查询信息完整度校验配置表
        DynamicObject integrityModule = BusinessDataServiceHelper.loadSingle("tdkw_hspm_integrity", "id", qFilter.toArray());
        integrityModule = BusinessDataServiceHelper.loadSingle(integrityModule.getPkValue(), "tdkw_hspm_integrity");
        //模块分录
        DynamicObjectCollection entryEntity = integrityModule.getDynamicObjectCollection("tdkw_entryentity");
        for (DynamicObject entry : entryEntity) {
            //信息完整度校验模块值
            DynamicObject integrityModules = entry.getDynamicObject("tdkw_integritymodule");
            if (!ObjectUtils.isEmpty(integrityModules)) {
                //员工信息
                DynamicObject person = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
                //模块信息分录
                QFilter entryQfilter = new QFilter("person", QCP.equals, person.getPkValue());
                entryQfilter.and("datastatus", QCP.equals, "1");
                entryQfilter.and("initstatus", QCP.equals, "2");
                entryQfilter.and("iscurrentversion", QCP.equals, "1");
                //模块单据标识
                String number = integrityModules.getString("number");
                DynamicObject[] entryObject = BusinessDataServiceHelper.load(number, "id", entryQfilter.toArray());
                //字段信息分录
                DynamicObjectCollection subentryEntity = entry.getDynamicObjectCollection("tdkw_subentryentity");
                for (DynamicObject dynamicObject : subentryEntity) {
                    //查询配置表对应字段数据是否存在
                    String moduleField[] = dynamicObject.getString("tdkw_modulefield").split(",");
                    //计算字段总数
                    for (String module : moduleField) {
                        if (StringUtils.isNotEmpty(module)) {
                            total += 1;
                        }
                    }
                    for (DynamicObject object : entryObject) {
                        object = BusinessDataServiceHelper.loadSingle(object.getPkValue(), number);
                        //是否存在该数据
                        for (String module : moduleField) {
                            boolean isExist = true;
                            if (StringUtils.isNotEmpty(module)) {
                                if (ObjectUtils.isEmpty(object.get(module))) {
                                    isExist = false;
                                }
                                //存在则存在总数+1
                                if (isExist) {
                                    progress += 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return progress / (total + 10);
    }

    /**
     * @Description 中台--业务规则管理--名单管理，配置相应的白名单列表，白名单列表中的人不参与信息完整度计算，信息完整度进度条默认为100%。
     * @author xxx
     * @date 2023/5/18 17:28
     */
    public boolean whiteList(Long personId) {
        //完整条数
        double progress = 0;
        //员工信息
        DynamicObject person = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
        //工号
        String number = person.getString("number");
        //查询白名单列表
        QFilter qFilter = new QFilter("brm_list_person.entityperson.number", QCP.equals, number);
        DynamicObject[] special = BusinessDataServiceHelper.load("brm_special_list", "id", qFilter.toArray());
        return special.length != 0;
    }

}
