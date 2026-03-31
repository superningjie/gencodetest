package tdkw.hrmp.hrobs.formplugin;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.AnchorItems;
import kd.bos.entity.param.CustomParam;
import kd.bos.entity.property.ComboProp;
import kd.bos.exception.KDBizException;
import kd.bos.ext.form.control.AnchorControl;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.OpenStyle;
import kd.bos.form.ShowType;
import kd.bos.form.container.Container;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Label;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.model.PermissionStatus;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.model.DimValueResult;
import kd.hr.hbp.common.util.HRStringUtils;
import org.apache.commons.collections4.CollectionUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRStructOrgUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRUserRoleCacheUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.formplugin.emputils.DateIntervalMerger;
import tdkw.hrmp.hrobs.formplugin.emputils.Interval;
import tdkw.hrmp.hrobs.formplugin.util.EmployeeInforUtil;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersonnelProfileDetailsFormPlugin extends AbstractFormPlugin {
    private final Log logger = LogFactory.getLog(PersonnelProfileDetailsFormPlugin.class);
    public GateWayUtils gateWayUtils = new GateWayUtils();

    DistributeSessionlessCache loginLossTime = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("loginLossTime");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        EntryGrid entryGrid = this.getView().getControl("tdkw_salaydentify1");
        Map<String, String> updatedMap = new HashMap<>();
        updatedMap.put("tdkw_salary_period", "近12个月总计");
        entryGrid.setFloatButtomData(updatedMap);


    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_labelap1", "tdkw_salary_expand", "tdkw_salary_info_entity");

    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        IFormView view = this.getView();
        String key = gateWayUtils.getClickKey(evt);
        String userLogin = RequestContext.get().getGlobalSessionId();
        if (org.apache.commons.lang3.StringUtils.equals("tdkw_labelap1", key) || org.apache.commons.lang3.StringUtils.equals("tdkw_salary_expand", key) || org.apache.commons.lang3.StringUtils.equals("tdkw_salary_info_entity", key)) {
            String loginUser = null != loginLossTime.get(UserServiceHelper.getCurrentUserId() + "loginUser" + userLogin) ? loginLossTime.get(UserServiceHelper.getCurrentUserId() + "loginUser" + userLogin) : null;
            String flag = this.getPageCache().get(UserServiceHelper.getCurrentUserId() + "ifFlag");
            if (null != loginUser || null != flag) {
                Container tdkwSalaryInfo = this.getControl("tdkw_salary_info_entity");
                Label salaryExpand = this.getControl("tdkw_salary_expand");
                if ("false".equals(flag)) {
                    // 单据体隐藏
                    tdkwSalaryInfo.setCollapse(true);
                    this.getPageCache().put(UserServiceHelper.getCurrentUserId() + "ifFlag", "true");
                    salaryExpand.setText("展开");
                } else {
                    // 单据体展开
                    tdkwSalaryInfo.setCollapse(false);
                    this.getPageCache().put(UserServiceHelper.getCurrentUserId() + "ifFlag", "false");

                    salaryExpand.setText("隐藏");
                }
            } else {
                // 弹出薪酬登录
                FormShowParameter showParameter = new FormShowParameter();
                OpenStyle openStyle = showParameter.getOpenStyle();
                openStyle.setShowType(ShowType.Floating);
                showParameter.setShowClose(false);
                showParameter.setFormId("tdkw_hrobs_pcpwdinit");
                showParameter.setShowTitle(false);
                showParameter.setCloseCallBack(new CloseCallBack(this, "donothing_login"));
                this.getView().showForm(showParameter);
            }
        }
    }

    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);
        FormShowParameter showParameter = (FormShowParameter) e.getSource();
        Map<String, Object> customParams = showParameter.getCustomParams();
        if (!customParams.containsKey("erfileId")) {
            return;
        }

        String erFileIdStr = customParams.get("erfileId").toString();
        logger.info("HR人员PKId：" + erFileIdStr);
        if (StringUtils.isBlank(erFileIdStr)) {
            e.setCancelMessage("链接erfileId参数为空");
            e.setCancel(true);
            return;
        }
        String formId = showParameter.getParentFormId();
        logger.info("formId" + formId);
        if (StringUtils.equals("tdkw_roster_report", formId) || StringUtils.equals("tdkw_busiunitrosterrpt", formId)) {
            QFilter filter = new QFilter("businessstatus", QCP.equals, "1");
            filter.and("datastatus", QCP.equals, "1");
            filter.and("isprimary", QCP.equals, "1");
            filter.and("iscurrentversion", QCP.equals, "1");
//            filter.and("person.tdkw_pkid", QCP.equals, erFileIdStr);
            filter.and("person.id", QCP.equals, Long.valueOf(erFileIdStr));
            DynamicObject queryOne = QueryServiceHelper.queryOne("hrpi_empposorgrel", "adminorg.id", new QFilter[]{filter});
            long orgId = queryOne.getLong("adminorg.id");
            logger.info("需查看人员简历的人员组织");
            //获取权限
            AuthorizedOrgResult result = null;
          /*  if (org.apache.commons.lang3.StringUtils.equals(formId, "tdkw_busiunitrosterrpt")) {
                result = HRStructOrgUtils.getStructUserAdminOrgs(UserServiceHelper.getCurrentUserId(), "tdkw_divisionroster_pc");
            } else if (org.apache.commons.lang3.StringUtils.equals(formId, "tdkw_roster_report")) {//在职人员花名册
                result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_roster_pc");
            }
            boolean hasAllOrgPerm = result.isHasAllOrgPerm();
            logger.info("是否有全部权限: " + hasAllOrgPerm);
            if (!hasAllOrgPerm) {
                List<Long> hasPerOrg = result.getHasPermOrgs();
                logger.info("组织权限: " + hasPerOrg);
                if (!hasPerOrg.contains(orgId)) {
                    e.setCancelMessage("您没有权限查看该人员信息");
                    e.setCancel(true);
                }
            }*/
        } else {
//            if (!ResumePermissions.queryAuthority(erFileIdStr, "PC")) {
//                e.setCancelMessage("您没有权限查看该人员信息");
//                e.setCancel(true);
//            }
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        Boolean flag = Boolean.FALSE;
        String sid = (String) this.getView().getFormShowParameter().getCustomParams().get("erfileId");
        Boolean isLeave = (Boolean) this.getView().getFormShowParameter().getCustomParams().get("isLeave");
        if (null != isLeave) {
            flag = isLeave;
            logger.info("跳转离职人员简历");
        }
        logger.info("获取sid" + sid);
        long id = 0;
        long adminorgId = 0L;
        if (flag && sid != null && !StringUtils.equals("", sid)) {

            QFilter qFilter = new QFilter("person.id", QCP.equals, Long.valueOf(sid));
            qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
            qFilter.and("empposrel.datastatus", QCP.equals, "1");
            qFilter.and("empposrel.iscurrentversion", QCP.equals, "1");
            qFilter.and("empposrel.isprimary", QCP.equals, "1");// 查询人员档案
            DynamicObject user = BusinessDataServiceHelper.load("hspm_ermanfile", "id,person,person.id,empposrel.adminorg.id", qFilter.toArray(), "modifytime desc")[0];
            if (null != user) {
                id = user.getLong("person.id");
                adminorgId = user.getLong("empposrel.adminorg.id");
            } else {
                throw new KDBizException("人员没有对应的人事业务档案或已离职");
            }
        } else if (sid != null && !StringUtils.equals("", sid)) {
//            QFilter qFilter = new QFilter("person.tdkw_pkid", QCP.equals, sid);
            // TODO 这里的sid明明是档案id啊，为啥这里变成了人员id，麻了啊，我也不敢乱改啊，从自定义控件过来的确实是档案id
            QFilter qFilter = new QFilter("person.id", QCP.equals, Long.valueOf(sid));
            qFilter.and("iscurrentversion", QCP.equals, "1");
            qFilter.and("empposrel.datastatus", QCP.equals, "1");
            qFilter.and("empposrel.iscurrentversion", QCP.equals, "1");
            qFilter.and("empposrel.isprimary", QCP.equals, "1");
            qFilter.and("empposrel.businessstatus", QCP.equals, "1");
            // 查询人员档案
            DynamicObject user = BusinessDataServiceHelper.loadSingle("hspm_ermanfile", qFilter.toArray()) != null ? BusinessDataServiceHelper.loadSingle("hspm_ermanfile", qFilter.toArray()) : null;
            if (null != user) {
                id = user.getLong("person.id");
                adminorgId = user.getLong("empposrel.adminorg.id");
            } else {
                throw new KDBizException("人员没有对应的人事业务档案或已离职");
            }
        } else {
            id = (long) this.getModel().getValue("tdkw_bigintfield");
        }

        AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
        List<AnchorItems> anchorItems = new ArrayList<>();
        anchorItems.add(new AnchorItems("tdkw_labelap", "基本信息", new ArrayList<>()));
        // 对单据头数据进行动态渲染
        this.setValueBaseInfor(id, anchorItems);

        // 对单据体中数据进行渲染
        this.setEmpposorgre(id, anchorItems);
        this.setPreworkexp(id, anchorItems);
        this.setSalaryInfo(id, anchorItems, adminorgId, flag);
        this.setPereduexp(id, anchorItems);
        this.setFamilymemb(id, anchorItems);
        this.setPercre(id, anchorItems);
        this.setEmrgcontact(id, anchorItems);
        // 二开基础资料，暂时屏蔽
//        this.setOtherSocialGroup(id, anchorItems);
        this.setPerprotitle(id, anchorItems);
        this.setPerocpqual(id, anchorItems);
        this.setLanguageski(id, anchorItems);
        this.setPerrprecord(id, anchorItems);
        // 二开基础资料，暂时屏蔽
//        this.setSkillidentify(id, anchorItems);
        this.setPercontact(id, anchorItems);
        anchorControl.addItems(anchorItems);
    }

    /**
     * @author xxx
     * @Description 获取人员对应的人事业务档案及基本信息
     * @Date 2023/9/tdkw_residentaddress
     */
    public void setValueBaseInfor(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        // qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // qFilter.and("empposrel.datastatus", QCP.equals, "1");
        // qFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        // qFilter.and("empposrel.isprimary", QCP.equals, "1");
        // qFilter.and("empposrel.businessstatus", QCP.equals, "1");
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("empposrel.datastatus", QCP.equals, "1");
        qFilter.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("empposrel.isprimary", QCP.equals, "1");
        // 查询人员档案
        DynamicObject user = BusinessDataServiceHelper.loadSingle("hspm_ermanfile", qFilter.toArray());

        QFilter perRegionFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE);
        perRegionFilter.and("datastatus", QCP.equals, "1");
        perRegionFilter.and("person", QCP.equals, personId);
        // 查询人员基本信息
        DynamicObject userBase = BusinessDataServiceHelper.loadSingle("hrpi_perregion", perRegionFilter.toArray());
        // 查询人员非时序性属性
        DynamicObject pernontsprop = BusinessDataServiceHelper.loadSingle("hrpi_pernontsprop", perRegionFilter.toArray());

        if (user != null) {
            if (pernontsprop == null) {
                throw new KDBizException("人员信息不完整：未找到对应的人员非时序表");
            }
            // 获取人员非时序性属性中头像信息
            if (null != pernontsprop.get("headsculpture")) {
                this.getModel().setValue("tdkw_picturefield", pernontsprop.get("headsculpture"));
                this.getView().updateView("tdkw_picturefield");
            }

            Label name = this.getControl("tdkw_name");
            if (null != user.getString("name")) {
                name.setText("姓名:  " + user.getString("name"));
            }
            // 设置人员公司等相关信息
            this.setEmpposorgrel(personId, anchorItems);

            Label number = this.getControl("tdkw_number");
            if (null != user.getString("number")) {
                number.setText("人员编码:  " + user.getString("number"));
            }

            Label name1 = this.getControl("tdkw_name1");
            if (null != user.getString("name")) {
                name1.setText("姓名:  " + user.getString("name"));
            }

            Label sex = this.getControl("tdkw_sex");
            if (null != pernontsprop.getDynamicObject("gender")) {
                String gender = pernontsprop.getDynamicObject("gender") != null ? pernontsprop.getDynamicObject("gender").getString("name") : "";
                sex.setText("性别:  " + gender);
            }

            Label brithday = this.getControl("tdkw_birthday");
            if (null != pernontsprop.getDate("birthday")) {
                brithday.setText("出生日期:  " + dateFormat.format(pernontsprop.getDate("birthday")));
            }

            Label orgin = this.getControl("tdkw_origin");
            orgin.setText("籍贯:  " +"深圳");
//            if (null != pernontsprop.getDynamicObject("tdkw_origin")) {
//                String origin = pernontsprop.getDynamicObject("tdkw_origin") != null ? pernontsprop.getDynamicObject("tdkw_origin").getString("tdkw_address") : "";
//                orgin.setText("籍贯:  " + origin);
//            }


            // 设置人员地址相关信息
            this.setAddress(personId, anchorItems);

            Label regresidencenature = this.getControl("tdkw_regresidencenature");
            if (userBase != null && null != userBase.getDynamicObject("regresidencenature")) {
                String regresidencenaturename = userBase.getDynamicObject("regresidencenature") != null ? userBase.getDynamicObject("regresidencenature").getString("name") : "";
                regresidencenature.setText("户口性质:  " + regresidencenaturename);
            }

            Label folk = this.getControl("tdkw_folk");
            if (null != pernontsprop.getDynamicObject("folk")) {
                String folkName = pernontsprop.getDynamicObject("folk") != null ? pernontsprop.getDynamicObject("folk").getString("name") : "";
                folk.setText("民族:  " + folkName);
            }

            Label politicalstatus = this.getControl("tdkw_politicalstatus");
            if (userBase != null && null != userBase.getDynamicObject("politicalstatus")) {
                String politicalstatusName = userBase.getDynamicObject("politicalstatus").get("name") != null ? userBase.getDynamicObject("politicalstatus").getString("name") : "";
                politicalstatus.setText("政治面貌:  " + politicalstatusName);
            }

            // 设置婚姻状态
            this.setMarriagestatus(personId, anchorItems);

            // 设置人员服务年限相关信息
            this.setPerserlen(personId, anchorItems);

            Label joinpartydate = this.getControl("tdkw_joinpartydate");
            if (userBase != null && null != userBase.getDate("joinpartydate")) {
                joinpartydate.setText("入党(团)时间:  " + dateFormat.format(userBase.getDate("joinpartydate")));
            }

            Label bloodtype = this.getControl("tdkw_bloodtype");
            if (null != pernontsprop.get("nbloodtype")) {
                // 获取选中下拉值
                DynamicObject genderInfo = pernontsprop.getDynamicObject("nbloodtype");
                // 通过下拉值获取下拉标题
                String displayName = genderInfo.getString(HRBaseConstants.NAME);
                bloodtype.setText("血型:  " + displayName);
            }

            Label nationality = this.getControl("tdkw_nationality");
            if (null != pernontsprop.getDynamicObject("nationality")) {
                String nationalityName = pernontsprop.getDynamicObject("nationality") != null ? pernontsprop.getDynamicObject("nationality").getString("name") : "";
                nationality.setText("国籍/地区:  " + nationalityName);
            }

            Label age = this.getControl("tdkw_age");
            if (null != pernontsprop.get("age")) {
                age.setText("年龄:  " + pernontsprop.get("age"));
            }


            Label birthplace = this.getControl("tdkw_permanentresidence");
            birthplace.setText("出生地:  " + "深圳");

//            if (null != pernontsprop.get("tdkw_administrative") && !StringUtils.equals("", pernontsprop.get("tdkw_administrative").toString())) {
//                logger.info("出生地：" + pernontsprop.get("tdkw_administrative"));
//                String birthplaceNumber = pernontsprop.get("tdkw_administrative") != null && !pernontsprop.getString("tdkw_administrative").equals("") ? pernontsprop.getString("tdkw_administrative") : null;
//                logger.info("出生地编码：" + birthplaceNumber);
//                if (birthplaceNumber != null) {
//                    DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(pernontsprop.get("tdkw_administrative"), "bd_admindivision");
//                    String birthplaceName = admindivision != null ? admindivision.getString("fullname") : "";
//                    if (!org.apache.commons.lang3.StringUtils.equals(birthplaceName, "")) {
//                        birthplaceName = birthplaceName.replaceAll("_", "");
//                    }
//                    birthplace.setText("出生地:  " + birthplaceName);
//                }
//            }


            Label height = this.getControl("tdkw_height");
            if (null != pernontsprop.get("height")) {
                height.setText("身高(cm):  " + pernontsprop.get("height"));
            }

            Label contrworkloc = this.getControl("tdkw_contrworkloc");
            if (userBase != null){
                String contrworklocName = HRStringUtils.isNotEmpty(userBase.getString("nativeplace")) ?
                        userBase.getString("nativeplace") : "";
                if (contrworklocName != null) {
                    contrworklocName = contrworklocName.replace("_", "");
                    contrworkloc.setText("工作地点:  " + contrworklocName);
                } else {
                    contrworkloc.setText("工作地点:  " + "");
                }
            }else {
                contrworkloc.setText("工作地点:  " + "");
            }


            this.setPerhobby(personId, anchorItems);
        } else {
            throw new KDBizException("人员信息不完整：未找到对应的人员基本信息");
        }
    }

    /**
     * 设置人员顶部相关信息
     */
    public void setEmpposorgrel(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("isprimary", QCP.equals, "1");
        // 获取人员工作经历相关信息
        DynamicObject[] empposorgrel = BusinessDataServiceHelper.load("hrpi_empposorgrel", "number,company,adminorg,position,startdate", qFilter.toArray(), "startdate desc");

        QFilter qFilter1 = new QFilter("number", QCP.equals, empposorgrel[0].getDynamicObject("company").get("number"));
        qFilter1.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter1.and("initstatus", QCP.equals, "2");
        DynamicObject haosAdminorg = BusinessDataServiceHelper.loadSingle("homs_adminorgdetail", qFilter1.toArray());
        if (empposorgrel.length > 0) {
            logger.info("头部人员工作信息相关" + empposorgrel[0]);
            Label company = this.getControl("tdkw_company");
            if (empposorgrel[0].getDynamicObject("company") != null) {
                String companyName = empposorgrel[0].getDynamicObject("company") != null ? empposorgrel[0].getDynamicObject("company").getString("name") : "";
                String orgLevel = haosAdminorg.getDynamicObject("adminorglayer") != null ? "(" + haosAdminorg.getDynamicObject("adminorglayer").getString("name") + ")" : "";
                company.setText("公司: " + companyName + orgLevel);
            }
            Label adminorg = this.getControl("tdkw_adminorg");
            if (empposorgrel[0].getDynamicObject("adminorg") != null) {
                String adminorgName = empposorgrel[0].getDynamicObject("adminorg") != null ? empposorgrel[0].getDynamicObject("adminorg").getString("name") : "";
                adminorg.setText("部门: " + adminorgName);
            }

//            Label postlevel = this.getControl("tdkw_postlevel");
//            if (empposorgrel[0].getString("tdkw_postlevel") != null && !StringUtils.equals("", empposorgrel[0].getString("tdkw_postlevel"))) {
//                // 获取选中下拉值
//                String genderInfo = empposorgrel[0].getString("tdkw_postlevel");
//                // 获取下拉列表字段
//                ComboProp comboProp = (ComboProp) empposorgrel[0].getDataEntityType().getProperties().get("tdkw_postlevel");
//                // 通过下拉值获取下拉标题
//                String displayName = comboProp.getItemByName(genderInfo);
//
//                String ranksName = empposorgrel[0].getDynamicObject("tdkw_ranks") != null ? "(" + empposorgrel[0].getDynamicObject("tdkw_ranks").getString("name") + ")" : "";
//
//                postlevel.setText("岗位层级:  " + displayName + ranksName);
//            }

        }
    }


    /**
     * 设置人员地址相关信息
     */
    public void setAddress(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

        DynamicObject[] peraddress = BusinessDataServiceHelper.load("hrpi_peraddress", "addresstype,addressinfo", qFilter.toArray());
//        this.getModel().setValue("tdkw_textareafield1", "福建省厦门市湖里区湖里街道自贸\n金融中心7号3楼310-311\n福建省厦门市湖里区湖里街道自贸\n福建省厦门市湖里区湖里街道自贸\n");
//        this.getView().updateView("tdkw_textareafield1");
        if (peraddress != null && peraddress.length > 0) {
            for (DynamicObject dynamicObject : peraddress) {
                logger.info("家庭成员信息：" + dynamicObject);
                if (null != dynamicObject.getDynamicObject("addresstype"))
                    if (StringUtils.equals("户口所在地", dynamicObject.getDynamicObject("addresstype").get("name").toString())) {
                        String permanentresidenceName = dynamicObject.getString("addressinfo") != null ? dynamicObject.getString("addressinfo") : "";
                        Label permanentresidence = this.getControl("tdkw_permanentresidence1");
                        permanentresidence.setText("户口所在地:  " + permanentresidenceName);

                        Label domicile = this.getControl("tdkw_domicile");
                        domicile.setText("户籍地址:  " + permanentresidenceName);
                    } else if (StringUtils.equals("通讯地址", dynamicObject.getDynamicObject("addresstype").get("name").toString())) {
                        String residentaddressName = dynamicObject.getString("addressinfo") != null ? dynamicObject.getString("addressinfo") : "";
                        this.getModel().setValue("tdkw_textareafield1", residentaddressName);
                        this.getView().updateView("tdkw_textareafield1");
                    } else if (StringUtils.equals("现居住地址", dynamicObject.getDynamicObject("addresstype").get("name").toString())) {
                        String homeaddressName = dynamicObject.getString("addressinfo") != null ? dynamicObject.getString("addressinfo") : "";
                        this.getModel().setValue("tdkw_textareafield", homeaddressName);
                        this.getView().updateView("tdkw_textareafield");
                    }/* else {
                        getView().showErrorNotification("获取人员地址出差，请检查tdkw_hrpi_peraddress_ext页面信息");
                    }*/
            }
        }
    }

    /**
     * 设置人员服务年限
     */
    public void setPerserlen(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

        DynamicObject peraddress = BusinessDataServiceHelper.loadSingle("hrpi_perserlen", "joinworktime,socialworkage", qFilter.toArray());

        if (peraddress != null) {
            if (null != peraddress.getDate("joinworktime")) {
                Label beginservicedate = this.getControl("tdkw_beginservicedate");
                beginservicedate.setText("参加工作时间:  " + dateFormat.format(peraddress.getDate("joinworktime")));
            }

            if (null != peraddress.get("socialworkage")) {
                Label seniority = this.getControl("tdkw_seniority");
                seniority.setText("工龄:  " + String.format("%.1f", peraddress.get("socialworkage")));
            }
        }
    }


    /**
     * 设置人员紧急联系人
     */
    public void setEmrgcontact(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("initstatus", QCP.equals, "2");

        DynamicObject[] emrgcontact = BusinessDataServiceHelper.load("hrpi_emrgcontact", "emrgname,emergcontactype,emrgphone", qFilter.toArray());
        if (emrgcontact.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_emrgcontact", "紧急联系人", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (emrgcontact.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_emrgcontact1", emrgcontact.length - 1);
            }
            for (int i = 0; i < emrgcontact.length; i++) {
                logger.info("紧急联系人信息：" + emrgcontact[i]);
                if (null != emrgcontact[i].get("emrgname")) {
                    this.getModel().setValue("tdkw_emrgname", emrgcontact[i].get("emrgname"), i);
                }
                if (null != emrgcontact[i].getDynamicObject("emergcontactype")) {
                    String emergcontactypeName = emrgcontact[i].getDynamicObject("emergcontactype") != null ? emrgcontact[i].getDynamicObject("emergcontactype").getString("name") : "";
                    this.getModel().setValue("tdkw_emergcontactype", emergcontactypeName, i);
                }
                if (null != emrgcontact[i].get("emrgphone")) {
                    this.getModel().setValue("tdkw_emrgphone", emrgcontact[i].get("emrgphone"), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_emrgcontact");
        }
    }


    /**
     * 设置人员证件信息
     */
    public void setPercre(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("initstatus", QCP.equals, "2");

        DynamicObject[] percres = BusinessDataServiceHelper.load("hrpi_percre", "credentialstype,number,expirationdate", qFilter.toArray());
        if (percres.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_identity", "证件信息", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (percres.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_identity1", percres.length - 1);
            }
            for (int i = 0; i < percres.length; i++) {
                logger.info("证件信息" + percres[i]);
                if (null != percres[i].getDynamicObject("credentialstype")) {
                    String idtypeName = percres[i].getDynamicObject("credentialstype") != null ? percres[i].getDynamicObject("credentialstype").getString("name") : "";
                    this.getModel().setValue("tdkw_idtype", idtypeName, i);
                }
                if (null != percres[i].get("number")) {
                    this.getModel().setValue("tdkw_idnumber", percres[i].get("number"), i);
                }
                if (null != percres[i].getDate("expirationdate")) {
                    this.getModel().setValue("tdkw_expirationdate", dateFormat.format(percres[i].getDate("expirationdate")), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_identity");
        }
    }


    /**
     * 设置人员婚姻状态
     */
    public void setMarriagestatus(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 查询人员时序性属性获取婚姻状态
        DynamicObject marriagestatu = BusinessDataServiceHelper.loadSingle("hrpi_pertsprop", qFilter.toArray());
        if (marriagestatu != null) {
            Label marriagestatus = this.getControl("tdkw_marriagestatus");
            if (null != marriagestatu.getDynamicObject("marriagestatus")) {
                String marriagestatusName = marriagestatu.getDynamicObject("marriagestatus") != null ? marriagestatu.getDynamicObject("marriagestatus").getString("name") : "";
                marriagestatus.setText("婚姻状态:  " + marriagestatusName);
            }
        }
    }

    /**
     * 设置人员特长及爱好
     */
    public void setPerhobby(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

        DynamicObject[] perhobby = BusinessDataServiceHelper.load("hrpi_perhobby", "hobby,description,interest", qFilter.toArray());
        Label description = this.getControl("tdkw_description");
        Label interest = this.getControl("tdkw_interest");
        if (perhobby.length > 1) {
            Label mainhobbytype = this.getControl("tdkw_mainhobbytype");

            mainhobbytype.setText("主要特长类型:  " + "多项特长");
            String descripstr = "";
            String intereststr = "";
            for (DynamicObject dynamicObject : perhobby) {
                logger.info("特长信息：" + dynamicObject);
                if (null != dynamicObject.get("hobby") && !StringUtils.equals("", dynamicObject.getString("hobby"))) {
                    descripstr += dynamicObject.getString("hobby");
                    if (perhobby[perhobby.length - 1] != dynamicObject) {
                        descripstr += ",";
                    }
                }
                if (null != dynamicObject.get("interest") && !StringUtils.equals("", dynamicObject.getString("interest"))) {
                    intereststr += dynamicObject.getString("interest");
                    if (perhobby[perhobby.length - 1] != dynamicObject) {
                        intereststr += ",";
                    }
                }
            }
            description.setText("特长描述:  " + descripstr);
            interest.setText("兴趣爱好:  " + intereststr);
        } else if (perhobby.length == 1) {
            if (perhobby[0] != null) {
                logger.info("perhobby" + perhobby[0]);
                Label mainhobbytype = this.getControl("tdkw_mainhobbytype");
                String mainhobbytypeName = perhobby[0].getString("hobby");
                /*if (perhobby[0].getDynamicObjectCollection("tdkw_mainhobbytype").size() > 0) {
                    String mainhobbytypeName = "";
                    for (int i = 0; i < perhobby[0].getDynamicObjectCollection("tdkw_mainhobbytype").size(); i++) {
                        if (null != perhobby[0].getDynamicObjectCollection("tdkw_mainhobbytype").get(i)) {
                            mainhobbytypeName += perhobby[0].getDynamicObjectCollection("tdkw_mainhobbytype").get(i).getDynamicObject(1) != null ? perhobby[0].getDynamicObjectCollection("tdkw_mainhobbytype").get(i).getDynamicObject(1).getString("name") : "";
                        }

                        if (i != perhobby[0].getDynamicObjectCollection("tdkw_mainhobbytype").size() - 1) {
                            mainhobbytypeName += ",";
                        }
                    }
                    mainhobbytype.setText("主要特长类型:  " + mainhobbytypeName);
                }*/
                mainhobbytype.setText("主要特长类型:  " + mainhobbytypeName);
                List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "[]", "无", null);

//                if (null != perhobby[0].get("hobby") && !"".equals(perhobby[0].get("hobby").toString()) && !"null".equals(perhobby[0].getString("hobby"))) {
                if (!invalidValues.contains(perhobby[0].getString("hobby"))) {
                    description.setText("特长描述:  " + (perhobby[0].getString("hobby") != null && !StringUtils.equals("", perhobby[0].getString("hobby")) ? perhobby[0].getString("hobby") : ""));
//                    this.getModel().setValue("tdkw_textfield4", "喜欢玩羽毛球，喜欢玩电脑，喜欢玩羽毛球，喜欢玩电脑，喜欢玩羽毛球，喜欢玩电脑，喜欢玩羽毛球，喜欢玩电脑");
//                    this.getView().updateView("tdkw_textfield4");
                }

//                if (null != perhobby[0].get("interest") && !"".equals(perhobby[0].get("interest").toString()) && !"null".equals(perhobby[0].getString("interest"))) {
                if (!invalidValues.contains(perhobby[0].getString("interest"))) {
                    String intereststr = "兴趣爱好:  " + (perhobby[0].getString("interest") != null && !StringUtils.equals("", perhobby[0].getString("interest")) ? perhobby[0].getString("interest") : "");
                    interest.setText(intereststr);
                }
            }
        }
    }

    /**
     * 设置人员联系方式
     */
    public void setPercontact(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("initstatus", QCP.equals, "2");

        DynamicObject percontact = BusinessDataServiceHelper.loadSingle("hrpi_percontact", qFilter.toArray());
        if (percontact != null) {
            Label homesituation = this.getControl("tdkw_thecityhous");
//            if (null != percontact.getDynamicObject("tdkw_thecityhous")) {
//                String percontactName = percontact.getDynamicObject("tdkw_thecityhous") != null ? percontact.getDynamicObject("tdkw_thecityhous").getString("name") : "";
            homesituation.setText("本市住房情况:  暂无");
//            }
        }

        DynamicObject[] percontacts = BusinessDataServiceHelper.load("hrpi_percontact", "phone,busemail,peremail,workphone", qFilter.toArray());
        if (percontacts.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_percontact", "联系方式", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (percontacts.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_percontact1", percontacts.length - 1);
            }
            for (int i = 0; i < percontacts.length; i++) {
                logger.info("联系方式" + percontacts[i]);
                if (percontacts[i].get("phone") != null) {
                    this.getModel().setValue("tdkw_percontactphone", percontacts[i].get("phone"), i);
                }
                if (percontacts[i].get("busemail") != null) {
                    this.getModel().setValue("tdkw_busemail", percontacts[i].get("busemail"), i);
                }
                if (percontacts[i].get("peremail") != null) {
                    this.getModel().setValue("tdkw_peremail", percontacts[i].get("peremail"), i);
                }
                if (percontacts[i].get("workphone") != null) {
                    this.getModel().setValue("tdkw_workphone", percontacts[i].get("workphone"), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_percontact");
        }
    }

    /**
     * 设置人员任职经历
     */
    public void setEmpposorgre(Long personId, List<AnchorItems> anchorItems) {
        // 根据人员id查出唯一索引，确保唯一性
        QFilter qFilter = new QFilter("id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("initstatus", QCP.equals, "2");
        qFilter.and("datastatus", QCP.equals, "1");
        DynamicObject dynamicObject = QueryServiceHelper.queryOne("hrpi_person", "personindexid,id,name", qFilter.toArray());
        Long personindexId = dynamicObject.getLong("personindexid");

        QFilter qFilter1 = new QFilter("personindexid", QCP.equals, personindexId);
        qFilter1.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter1.and("initstatus", QCP.equals, "2");
        qFilter1.and("datastatus", QCP.equals, "1");
        DynamicObjectCollection hrpiPerson = QueryServiceHelper.query("hrpi_person", "personindexid,id,name", qFilter1.toArray());

        List<Long> personIds = hrpiPerson.stream().map(person -> person.getLong("id")).collect(Collectors.toList());

        QFilter qqFilter = new QFilter("person.id", QCP.in, personIds);
        qqFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qqFilter.and("initstatus", QCP.equals, "2");
        // qqFilter.and("datastatus", QCP.not_equals, "-1");
        qqFilter.and("datastatus", QCP.equals, "1");
        DynamicObject[] empposorgrelList = BusinessDataServiceHelper.load("hrpi_emporgrelall",
//                "tdkw_companyname,adminorg,position,tdkw_changetype,tdkw_changereason,startdate,enddate", qqFilter.toArray(), "startdate desc");
                "company,adminorg,position,startdate,enddate", qqFilter.toArray(), "startdate desc");

        Map<String, List<DynamicObject>> empListMap = Arrays.stream(empposorgrelList)
//                .filter(dynamicObject1 -> !"XY00017".equals(dynamicObject1.getString("tdkw_changereason.number")))
                .collect(Collectors.groupingBy(empDynamicObject ->
                        empDynamicObject.getString("company")
                                + empDynamicObject.getString("adminorg")
                                + empDynamicObject.getString("position")));


//        ===============


        // 取出empListMap的value的size大于的数据返回map
        Map<String, List<DynamicObject>> empListMergeMap = empListMap.entrySet().stream().filter(entry -> entry.getValue().size() > 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 判断empListMergeMap的开始和结束时间是否间隔两天以上
        empListMergeMap.forEach((tagKey, dynamicObjects) -> {
            dynamicObjects.sort(Comparator.comparing(empDynamicObject -> empDynamicObject.getDate("startdate")));
            // 取出开始时间
            List<Date> startDateList = dynamicObjects.stream().map(empDynamicObject -> empDynamicObject.getDate("startdate")).collect(Collectors.toList());
            // 取出结束时间
            List<Date> endDateList = dynamicObjects.stream().map(empDynamicObject -> empDynamicObject.getDate("enddate")).collect(Collectors.toList());
            List<Interval> mergedIntervals = DateIntervalMerger.mergeIntervals(startDateList, endDateList);
            // 原始任职数据id集合
            List<Long> originIds = dynamicObjects.stream().mapToLong(empDynamicObject -> empDynamicObject.getLong("id")).boxed().collect(Collectors.toList());

            // 如果存在，则需要重新处理数据
            if (dynamicObjects.size() != mergedIntervals.size()) {
                List<Long> keepIds = new ArrayList<>();
                // 根据开始时间和结束时间取出数据
                mergedIntervals.forEach(interval -> {
                    Date start = interval.start;
                    Date end = interval.end;
                    List<Long> perKeepIds = dynamicObjects.stream().filter(perKeepObject -> {
                        Date startdate = perKeepObject.getDate("startdate");
                        Date enddate = perKeepObject.getDate("enddate");
                        return startdate.equals(start) && enddate.equals(end);
                    }).mapToLong(empDynamicObject -> empDynamicObject.getLong("id")).boxed().collect(Collectors.toList());
                    keepIds.addAll(perKeepIds);
                });
                // 取差集、得到要删除的数据
                originIds.removeAll(keepIds);
                // 取出要删除的数据
                List<DynamicObject> deleteList = dynamicObjects.stream().filter(empDynamicObject -> originIds.contains(empDynamicObject.getLong("id"))).collect(Collectors.toList());
                List<DynamicObject> keepList = dynamicObjects.stream().filter(empDynamicObject -> keepIds.contains(empDynamicObject.getLong("id"))).collect(Collectors.toList());
                int keepSize = keepIds.size();
                // 合并的条数
                int mergeSize = mergedIntervals.size();
                // 从待删除列表中取出数据重新设置值
                List<DynamicObject> mergeAfterList4Del = deleteList.subList(0, mergeSize - keepSize);
                // 把时间相同的数据移除
                mergedIntervals.removeIf(interval -> {
                    Date start = interval.start;
                    Date end = interval.end;
                    return dynamicObjects.stream().anyMatch(empDynamicObject -> {
                        Date startdate = empDynamicObject.getDate("startdate");
                        Date enddate = empDynamicObject.getDate("enddate");
                        return startdate.equals(start) && enddate.equals(end);
                    });
                });
                // 遍历设置合并后的时间
                for (int i = 0; i < mergeSize - keepSize; i++) {
                    mergeAfterList4Del.get(i).set("startdate", mergedIntervals.get(i).start);
                    mergeAfterList4Del.get(i).set("enddate", mergedIntervals.get(i).end);
                }
                // 把保留的数组和重新设置的数组进行合并
                List<DynamicObject> newMergeList = new ArrayList<>();
                newMergeList.addAll(mergeAfterList4Del);
                newMergeList.addAll(keepList);
                // 清空原来的数据
                empListMap.put(tagKey, newMergeList);
            }
        });
        // 取出empListMap中的所有List<DynamicObject> 对empList按照startdate降序排列
        List<DynamicObject> empList = empListMap.values().stream().flatMap(Collection::stream)
                .sorted((o1, o2) -> o2.getDate("startdate")
                        .compareTo(o1.getDate("startdate")))
                .collect(Collectors.toList());

        DynamicObject[] empposorgrels = new DynamicObject[empList.size()];
        for (int i = 0; i < empList.size(); i++) {
            empposorgrels[i] = empList.get(i);
        }

//        ======================


        logger.info("查询到任职经历：" + empposorgrels.length);
        if (empposorgrels.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_empposorgre", "任职经历", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (empposorgrels.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_empposorgre11", empposorgrels.length - 1);
            }
            int k = 0;
            for (int i = 0; i < empposorgrels.length; i++) {
                logger.info("单据体人员任职经历相关" + empposorgrels[i]);
                if (empposorgrels[i].getString("company") != null) {
                    String companyName = empposorgrels[i].getString("company") != null ? empposorgrels[i].getString("company") : "";
                    this.getModel().setValue("tdkw_empcompany", companyName, k);
                }
                if (empposorgrels[i].getString("adminorg") != null) {
                    String adminOrgName = empposorgrels[i].getString("adminorg") != null ? empposorgrels[i].getString("adminorg") : "";

                    this.getModel().setValue("tdkw_empadminorg", adminOrgName, k);
                }

                if (empposorgrels[i].getString("position") != null) {
                    String positionName = empposorgrels[i].getString("position") != null ? empposorgrels[i].getString("position") : "";
                    this.getModel().setValue("tdkw_empposition", positionName, k);
                }
                if (empposorgrels[i].getDate("startdate") != null) {
                    this.getModel().setValue("tdkw_emporgrelstartdate", dateFormat.format(empposorgrels[i].getDate("startdate")), k);
                }
                if (empposorgrels[i].getDate("enddate") != null) {
                    if ("2999-12-31".equals(dateFormat.format(empposorgrels[i].getDate("enddate")))) {
                        this.getModel().setValue("tdkw_emporgrelenddate", "至今", k);
                    } else {
                        this.getModel().setValue("tdkw_emporgrelenddate", dateFormat.format(empposorgrels[i].getDate("enddate")), k);
                    }
                }
                k++;
            }
        } else {
            this.getView().setVisible(false, "tdkw_empposorgre");
        }
    }

    /**
     * 设置人员教育经历
     */
    public void setPereduexp(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");
        qFilter.and("initstatus", QCP.equals, "2");

//        DynamicObject pereduexp = BusinessDataServiceHelper.loadSingle("hrpi_pereduexp", qFilter.toArray());
        DynamicObject[] pereduexps = BusinessDataServiceHelper.load("hrpi_pereduexp", "admissiondate,gradutiondate,graduateschool,major,education.name,degree.name,edunature,ishighestdegree,tdkw_firstdegree,tdkw_highestdegree,tdkw_jobhighesteducation,schoolrecord", qFilter.toArray(), "gradutiondate desc");

        if (pereduexps.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_advconap", "教育经历", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (pereduexps.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_pereduexp1", pereduexps.length - 1);
            }
            for (int i = 0; i < pereduexps.length; i++) {
                logger.info("教育经历：" + pereduexps[i]);
                if (pereduexps[i].getDate("admissiondate") != null) {
                    this.getModel().setValue("tdkw_admissiondate", dateFormat.format(pereduexps[i].getDate("admissiondate")), i);
                }
                if (pereduexps[i].getDate("gradutiondate") != null) {
                    this.getModel().setValue("tdkw_gradutiondate", dateFormat.format(pereduexps[i].getDate("gradutiondate")), i);
                }
                if (pereduexps[i].getDynamicObject("graduateschool") != null) {
                    // 毕业学校为其他院校时取其他院校字段
                    String graduateschoolName = null;
                    if (StringUtils.equals("其他院校", pereduexps[i].getDynamicObject("graduateschool").getString("name"))) {
                        graduateschoolName = pereduexps[i].getString("schoolrecord") != null ? pereduexps[i].getString("schoolrecord") : "";
                    } else {
                        graduateschoolName = pereduexps[i].getDynamicObject("graduateschool") != null ? pereduexps[i].getDynamicObject("graduateschool").getString("name") : "";
                    }
                    this.getModel().setValue("tdkw_graduateschool", graduateschoolName, i);
                }
                if (pereduexps[i].get("major") != null) {
                    if (StringUtils.equals("", pereduexps[i].get("major").toString())) {
                        this.getModel().setValue("tdkw_major", "专业", i);
                    } else {
                        this.getModel().setValue("tdkw_major", pereduexps[i].get("major"), i);
                    }
                }
                if (pereduexps[i].getDynamicObject("education") != null) {
                    String educationName = pereduexps[i].getDynamicObject("education") != null ? pereduexps[i].getDynamicObject("education").getString("name") : "";
                    this.getModel().setValue("tdkw_education", educationName, i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_advconap");
        }
    }

    /**
     * 设置人员前工作经历
     */
    public void setPreworkexp(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("initstatus", QCP.equals, "2");

        DynamicObject[] preworkexps = BusinessDataServiceHelper.load("hrpi_preworkexp", "startdate,enddate,unitname,department,position", qFilter.toArray(), "enddate desc");

        if (preworkexps.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_preworkexp", "前工作经历", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (preworkexps.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_preworkexp1", preworkexps.length - 1);
            }
            for (int i = 0; i < preworkexps.length; i++) {
                logger.info("前工作经历" + preworkexps[i]);
                if (preworkexps[i].getDate("startdate") != null) {
                    this.getModel().setValue("tdkw_startdate", dateFormat.format(preworkexps[i].getDate("startdate")), i);
                }
                if (preworkexps[i].getDate("enddate") != null) {
                    this.getModel().setValue("tdkw_enddate", dateFormat.format(preworkexps[i].getDate("enddate")), i);
                }
                if (preworkexps[i].get("unitname") != null) {
                    this.getModel().setValue("tdkw_unitname", preworkexps[i].get("unitname"), i);
                }
                if (preworkexps[i].get("department") != null) {
                    this.getModel().setValue("tdkw_department", preworkexps[i].get("department"), i);
                }
                if (preworkexps[i].get("position") != null) {
                    this.getModel().setValue("tdkw_position", preworkexps[i].get("position"), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_preworkexp");
        }
    }

    /**
     * 设置人员其它任职
     */
    public void setOtheremployinf(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

        DynamicObject[] otheremployinfors = BusinessDataServiceHelper.load("tdkw_hrpi_otheremployinf", "tdkw_startdate,tdkw_enddate,tdkw_company,tdkw_tenure,tdkw_companyfullname,tdkw_post,tdkw_whethereffective", qFilter.toArray(), "tdkw_enddate desc");

        if (otheremployinfors.length > 0) {
            if (otheremployinfors.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_otheremployinfor", otheremployinfors.length - 1);
            }
            for (int i = 0; i < otheremployinfors.length; i++) {
                if (otheremployinfors[i].getDate("tdkw_startdate") != null) {
                    this.getModel().setValue("tdkw_othstartdate", dateFormat.format(otheremployinfors[i].getDate("tdkw_startdate")), i);
                }
                if (otheremployinfors[i].getDate("tdkw_enddate") != null) {
                    this.getModel().setValue("tdkw_othenddate", dateFormat.format(otheremployinfors[i].getDate("tdkw_enddate")), i);
                }
                if (otheremployinfors[i].get("tdkw_company") != null) {
                    this.getModel().setValue("tdkw_othcompany", otheremployinfors[i].get("tdkw_company"), i);
                }
                if (otheremployinfors[i].get("tdkw_tenure") != null) {
                    this.getModel().setValue("tdkw_othtenure", otheremployinfors[i].get("tdkw_tenure"), i);
                }
                if (otheremployinfors[i].get("tdkw_companyfullname") != null) {
                    this.getModel().setValue("tdkw_companyfullname", otheremployinfors[i].get("tdkw_companyfullname"), i);
                }
                if (otheremployinfors[i].get("tdkw_post") != null) {
                    this.getModel().setValue("tdkw_othpost", otheremployinfors[i].get("tdkw_post"), i);
                }
                if (otheremployinfors[i].get("tdkw_whethereffective") != null) {
                    this.getModel().setValue("tdkw_whethereffective", otheremployinfors[i].get("tdkw_whethereffective"), i);
                }
            }
        }
    }

    /**
     * 设置人员奖惩情况
     */
    public void setPerrprecord(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("flag", QCP.equals, "1");

        DynamicObject[] perrprecords = BusinessDataServiceHelper.load("hrpi_perrprecord", "flag,level,unit,tdkw_honorarytitle,content,rewarddate,tdkw_maincontent,type", qFilter.toArray(), "rewarddate desc");

        if (perrprecords.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_reward", "奖励情况", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (perrprecords.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_reward1", perrprecords.length - 1);
            }
            for (int i = 0; i < perrprecords.length; i++) {
                if (perrprecords[i].getDate("rewarddate") != null) {
                    this.getModel().setValue("tdkw_rewarddate", dateFormat.format(perrprecords[i].getDate("rewarddate")), i);
                }
                if (perrprecords[i].get("unit") != null) {
                    this.getModel().setValue("tdkw_rewardunit", perrprecords[i].get("unit"), i);
                }
                if (perrprecords[i].get("content") != null) {
                    this.getModel().setValue("tdkw_rewardcontent", perrprecords[i].get("content"), i);
                }


            }
        } else {
            this.getView().setVisible(false, "tdkw_reward");
        }
    }

    /**
     * 设置人员家庭成员
     */
    public void setFamilymemb(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("initstatus", QCP.equals, "2");
        qFilter.and("datastatus", QCP.equals, "1");

        DynamicObject[] familymembs = BusinessDataServiceHelper.load("hrpi_familymemb", "name,familymembship,workunit", qFilter.toArray(), "familymembship.number asc");

        if (familymembs.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_familymemb", "家庭成员", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (familymembs.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_familymemb1", familymembs.length - 1);
            }
            for (int i = 0; i < familymembs.length; i++) {
                if (familymembs[i].getDynamicObject("familymembship") != null) {
                    String familymembshipName = familymembs[i].getDynamicObject("familymembship") != null ? familymembs[i].getDynamicObject("familymembship").getString("name") : "";
                    this.getModel().setValue("tdkw_familymembship", familymembshipName, i);
                }
                if (familymembs[i].get("name") != null) {
                    this.getModel().setValue("tdkw_faname", familymembs[i].get("name"), i);
                }
                /*if (familymembs[i].getDate("tdkw_dateofbirth") != null) {
                    this.getModel().setValue("tdkw_dateofbirth", dateFormat.format(familymembs[i].getDate("tdkw_dateofbirth")), i);
                }*/
                if (familymembs[i].get("workunit") != null) {
                    this.getModel().setValue("tdkw_workunit", familymembs[i].get("workunit"), i);
                }
                /*if (familymembs[i].get("tdkw_posts") != null) {
                    this.getModel().setValue("tdkw_faposts", familymembs[i].get("tdkw_posts"), i);
                }*/
            }
        } else {
            this.getView().setVisible(false, "tdkw_familymemb");
        }
    }

    /**
     * 设置人员职称信息
     */
    public void setPerprotitle(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        qFilter.and("datastatus", QCP.equals, "1");

        DynamicObject[] perprotitles = BusinessDataServiceHelper.load("hrpi_perprotitle", "awardtime,professional,prolevel,unit,certiicateid", qFilter.toArray(), "awardtime desc");

        if (perprotitles.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_perprotitle", "职称信息", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (perprotitles.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_perprotitle1", perprotitles.length - 1);
            }
            for (int i = 0; i < perprotitles.length; i++) {
                if (perprotitles[i].getDate("awardtime") != null) {
                    this.getModel().setValue("tdkw_awardtime", dateFormat.format(perprotitles[i].getDate("awardtime")), i);
                }
                if (perprotitles[i].getDynamicObject("professional") != null) {
                    String professionalName = perprotitles[i].getDynamicObject("professional") != null ? perprotitles[i].getDynamicObject("professional").getString("name") : "";

                    this.getModel().setValue("tdkw_professional", professionalName, i);
                }
                if (perprotitles[i].getDynamicObject("prolevel") != null) {
                    String prolevelName = perprotitles[i].getDynamicObject("prolevel") != null ? perprotitles[i].getDynamicObject("prolevel").getString("name") : "";

                    this.getModel().setValue("tdkw_prolevel", prolevelName, i);
                }
                if (perprotitles[i].get("unit") != null) {
                    this.getModel().setValue("tdkw_perprotitleunit", perprotitles[i].get("unit"), i);
                }
                if (perprotitles[i].get("certiicateid") != null) {
                    this.getModel().setValue("tdkw_certiicateid", perprotitles[i].get("certiicateid"), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_perprotitle");
        }
    }

    /**
     * 设置人员执(职)业资格
     */
    public void setPerocpqual(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

        DynamicObject[] perocpquals = BusinessDataServiceHelper.load("hrpi_perocpqual", "qualification,certiicateid,qualevel,grantunit,gettime", qFilter.toArray(), "gettime desc");

        if (perocpquals.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_perocpqual", "执(职)业资格", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (perocpquals.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_perocpqual1", perocpquals.length - 1);
            }
            for (int i = 0; i < perocpquals.length; i++) {

                if (perocpquals[i].getDynamicObject("qualification") != null) {
                    String qualificationName = perocpquals[i].getDynamicObject("qualification") != null ? perocpquals[i].getDynamicObject("qualification").getString("name") : "";

                    this.getModel().setValue("tdkw_qualification", qualificationName, i);
                }
                if (perocpquals[i].get("certiicateid") != null) {
                    this.getModel().setValue("tdkw_perocpqualcertiicate", perocpquals[i].getString("certiicateid"), i);
                }
                if (perocpquals[i].getDynamicObject("qualevel") != null) {
                    String qualevelName = perocpquals[i].getDynamicObject("qualevel") != null ? perocpquals[i].getDynamicObject("qualevel").getString("name") : "";

                    this.getModel().setValue("tdkw_qualevel", qualevelName, i);
                }
                if (perocpquals[i].get("grantunit") != null) {
                    this.getModel().setValue("tdkw_grantunit", perocpquals[i].get("grantunit"), i);
                }
                if (perocpquals[i].getDate("gettime") != null) {
                    this.getModel().setValue("tdkw_gettime", dateFormat.format(perocpquals[i].getDate("gettime")), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_perocpqual");
        }
    }

    /**
     * 设置人员语言能力
     */
    public void setLanguageski(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

        DynamicObject[] languageskis = BusinessDataServiceHelper.load("hrpi_languageskills", "language,name", qFilter.toArray());

        if (languageskis.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_languageski", "语言能力", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (languageskis.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_languageski1", languageskis.length - 1);
            }
            for (int i = 0; i < languageskis.length; i++) {

                if (languageskis[i].getDynamicObject("language") != null) {
                    String languageName = languageskis[i].getDynamicObject("language") != null ? languageskis[i].getDynamicObject("language").getString("name") : "";

                    this.getModel().setValue("tdkw_language", languageName, i);
                }

                if (languageskis[i].getString("name") != null && !StringUtils.equals("", languageskis[i].getString("name"))) {
                    this.getModel().setValue("tdkw_langname", languageskis[i].getString("name"), i);
                }

            }
        } else {
            this.getView().setVisible(false, "tdkw_languageski");
        }
    }

    /**
     * 设置人员职业技能鉴定
     */
    public void setSkillidentify(Long personId, List<AnchorItems> anchorItems) {
        QFilter qFilter = new QFilter("person.id", QCP.equals, personId);
        qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);

        DynamicObject[] skillidentifys = BusinessDataServiceHelper.load("tdkw_hrpi_skillidentify", "tdkw_startdate,tdkw_enddate,tdkw_name,tdkw_skilllevel,tdkw_number,tdkw_issuedunit", qFilter.toArray(), "tdkw_startdate desc");

        if (skillidentifys.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_skillidentify", "职业技能鉴定", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (skillidentifys.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_skillidentify1", skillidentifys.length - 1);
            }
            for (int i = 0; i < skillidentifys.length; i++) {

                if (skillidentifys[i].getDate("tdkw_startdate") != null) {
                    this.getModel().setValue("tdkw_skillidentifystartda", dateFormat.format(skillidentifys[i].getDate("tdkw_startdate")), i);
                }
                if (skillidentifys[i].getDate("tdkw_enddate") != null) {
                    this.getModel().setValue("tdkw_skillidentifyendda", dateFormat.format(skillidentifys[i].getDate("tdkw_enddate")), i);
                }
                if (skillidentifys[i].getString("tdkw_name") != null) {
                    this.getModel().setValue("tdkw_skillidentifyname", skillidentifys[i].getString("tdkw_name"), i);
                }
                if (skillidentifys[i].getDynamicObject("tdkw_skilllevel") != null) {
                    String skilllevelName = skillidentifys[i].getDynamicObject("tdkw_skilllevel") != null ? skillidentifys[i].getDynamicObject("tdkw_skilllevel").getString("name") : "";

                    this.getModel().setValue("tdkw_skillidentiskillleve", skilllevelName, i);
                }
                if (skillidentifys[i].get("tdkw_number") != null) {
                    this.getModel().setValue("tdkw_skillidentifynumber", skillidentifys[i].get("tdkw_number"), i);
                }
                if (skillidentifys[i].get("tdkw_issuedunit") != null) {
                    this.getModel().setValue("tdkw_skillidentifyissuedu", skillidentifys[i].get("tdkw_issuedunit"), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_skillidentify");
        }
    }


    /**
     * 设置人员薪酬信息
     */
    public void setSalaryInfo(Long personId, List<AnchorItems> anchorItems, Long adminorgId, Boolean isLeave) {
        logger.info("简历人员组织id" + adminorgId);
        // 查询是否是职能线权限
        boolean functionRole = HRRoleAndPersonUtils.getIsRole(UserServiceHelper.getCurrentUserId(), "XY_LEAD_PC_006");
        if (functionRole) {
            // 获取当前人员任职经历
            // 根据人员id查出唯一索引，确保唯一性
            QFilter qFilter = new QFilter("id", QCP.equals, personId);
            qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
            qFilter.and("initstatus", QCP.equals, "2");
            qFilter.and("datastatus", QCP.equals, "1");
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("hrpi_person", "personindexid,id,name", qFilter.toArray());
            Long personindexId = dynamicObject.getLong("personindexid");

            QFilter qFilter1 = new QFilter("personindexid", QCP.equals, personindexId);
            qFilter1.and("iscurrentversion", QCP.equals, Boolean.TRUE);
            qFilter1.and("initstatus", QCP.equals, "2");
            qFilter1.and("datastatus", QCP.equals, "1");
            DynamicObjectCollection hrpiPerson = QueryServiceHelper.query("hrpi_person", "personindexid,id,name", qFilter1.toArray());

            List<Long> personIds = hrpiPerson.stream().map(person -> person.getLong("id")).collect(Collectors.toList());

            String appId = getAppId("tdkw_resume_salary_pc");
            String permItemId = PermissionStatus.View;
            // dimNumber： 任职类型标识
            DimValueResult dimValueResult = DispatchServiceHelper.invokeBizService("hrmp", "hrcs", "IHRCSBizDataPermissionService", "getEntityDimValue", UserServiceHelper.getCurrentUserId(), appId, "tdkw_resume_salary_pc", permItemId, "hbss_postype");
            Set<String> dimValueIds = dimValueResult.getDimValueIds();
            List<Long> dimIds = dimValueIds.stream().map(Long::valueOf).collect(Collectors.toList());
            logger.info("任职类型id" + dimIds);
            QFilter qqFilter = new QFilter("person.id", QCP.in, personIds);
            qqFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
            qqFilter.and("initstatus", QCP.equals, "2");
            qqFilter.and("datastatus", QCP.not_equals, "-1");
            // 离职人员不过滤任职经历生效版本状态
            if (!isLeave) {
                qqFilter.and("businessstatus", QCP.equals, "1");
            }
            if (CollectionUtils.isNotEmpty(dimIds)) {
                qqFilter.and("postype", QCP.in, dimIds);
            }
            qqFilter.and("tdkw_changereason.number", QCP.not_equals, "XY00017");

            DynamicObjectCollection empposorgrels = QueryServiceHelper.query("hrpi_empposorgrel", "adminorg,tdkw_changetype", qqFilter.toArray());
            logger.info("查询到生效中任职经历：" + empposorgrels.size());
            List<Long> adminOrgs = empposorgrels.stream().map(i -> i.getLong("adminorg")).collect(Collectors.toList());

            // 获取当前登录人员组织权限
            AuthorizedOrgResult tdkwSalaryinfoPc = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_resume_salary_pc");
            boolean flag = true;
            logger.info("人员是否全组织:" + tdkwSalaryinfoPc.isHasAllOrgPerm());
            if (!tdkwSalaryinfoPc.isHasAllOrgPerm()) {
                // 人员不是全集团获取组织权限
                List<Long> hasPermOrgs = tdkwSalaryinfoPc.getHasPermOrgs();
                logger.info("人员组织权限:" + hasPermOrgs.toString());
                hasPermOrgs.retainAll(adminOrgs);
                if (CollectionUtils.isEmpty(hasPermOrgs)) {
                    flag = false;
                }
            }
            if (flag) {
                Container tdkwSalaryInfo = this.getControl("tdkw_salary_info_entity");
                String userLogin = RequestContext.get().getGlobalSessionId();
                logger.info("人员本次登录信息:" + userLogin);
                String loginUser = loginLossTime.get(UserServiceHelper.getCurrentUserId() + "loginUser" + userLogin);
                if (null != loginUser) {
                    // 读取参数配置决定进入页面时默认展开还是隐藏
                    Map<String, String> customParameter = SystemParamServiceHelper.loadCustomParameterFromCache(new CustomParam());
                    String isCollapse = null != (String) customParameter.get("SALARY_INFO_ISEXPAND") ? (String) customParameter.get("SALARY_INFO_ISEXPAND") : "false";
                    if ("false".equals(isCollapse)) {
                        tdkwSalaryInfo.setCollapse(false);
                        this.getPageCache().put(UserServiceHelper.getCurrentUserId() + "ifFlag", "false");
                        Label salaryExpand = this.getControl("tdkw_salary_expand");
                        salaryExpand.setText("隐藏");
                    } else {
                        tdkwSalaryInfo.setCollapse(true);
                        this.getPageCache().put(UserServiceHelper.getCurrentUserId() + "ifFlag", "true");
                        Label salaryExpand = this.getControl("tdkw_salary_expand");
                        salaryExpand.setText("展开");
                    }

                } else {
                    // 设置单据体默认隐藏
                    tdkwSalaryInfo.setCollapse(true);
                }

                Map<String, Object> maps = DispatchServiceHelper.invokeService("tdkw.swc.tdkw_hsas_ext",
                        "tdkw_hsas_ext", "SalaryInfoService", "payrollDetailByPersonId", personId);
                if ((boolean) maps.get("success")) {
                    List<Map<String, Object>> salaryList = (List<Map<String, Object>>) maps.get("data");
                    if (null != salaryList && salaryList.size() > 1) {
                        AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
                        anchorItems.add(new AnchorItems("tdkw_salary_info", "薪酬信息", new ArrayList<>()));
                        anchorControl.addItems(anchorItems);
                        if (salaryList.size() > 2) {
                            this.getModel().batchCreateNewEntryRow("tdkw_salaydentify1", salaryList.size() - 2);
                        }
                        List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "[]", "无", null);

                        // 最后一项数据为合计值
                        for (int i = 0; i < salaryList.size() - 1; i++) {
                            // 发薪期间
                            this.getModel().setValue("tdkw_salary_period", salaryList.get(i).get("payrollTime"), i);
                            // 应发工资
                            this.getModel().setValue("tdkw_grosssalary", invalidValues.contains(salaryList.get(i).get("grossSalary")) ? 0 : salaryList.get(i).get("grossSalary"), i);
                            // 实发工资
                            this.getModel().setValue("tdkw_decimalfield", invalidValues.contains(salaryList.get(i).get("netSalary")) ? 0 : salaryList.get(i).get("netSalary"), i);
                            // 基础工资
                            this.getModel().setValue("tdkw_basicsalary", invalidValues.contains(salaryList.get(i).get("basicSalary")) ? 0 : salaryList.get(i).get("basicSalary"), i);
                            // 职位工资
                            this.getModel().setValue("tdkw_positionsalary", invalidValues.contains(salaryList.get(i).get("positionalSalary")) ? 0 : salaryList.get(i).get("positionalSalary"), i);
                            // 司龄工资
                            this.getModel().setValue("tdkw_senioritysalary", invalidValues.contains(salaryList.get(i).get("longevityPay")) ? 0 : salaryList.get(i).get("longevityPay"), i);
                            // 浮动薪酬
                            this.getModel().setValue("tdkw_variablecompensation", invalidValues.contains(salaryList.get(i).get("flexiblePayRate")) ? 0 : salaryList.get(i).get("flexiblePayRate"), i);
                            // 福利补贴
                            this.getModel().setValue("tdkw_benefitsallowance", invalidValues.contains(salaryList.get(i).get("benefitsSubsidy")) ? 0 : salaryList.get(i).get("benefitsSubsidy"), i);
                            // 考勤扣款
                            this.getModel().setValue("tdkw_deductionsattendance", invalidValues.contains(salaryList.get(i).get("attendanceDeductions")) ? 0 : salaryList.get(i).get("attendanceDeductions"), i);
                            // 年终浮动奖金
                            this.getModel().setValue("tdkw_annualbonus", invalidValues.contains(salaryList.get(i).get("yearEndBonus")) ? 0 : salaryList.get(i).get("yearEndBonus"), i);
                            // 专项奖
                            this.getModel().setValue("tdkw_specialbonus", invalidValues.contains(salaryList.get(i).get("specialAward")) ? 0 : salaryList.get(i).get("specialAward"), i);
                            // 五险二金个人部分
                            this.getModel().setValue("tdkw_socialinsurancefun", invalidValues.contains(salaryList.get(i).get("piContributions")) ? 0 : salaryList.get(i).get("piContributions"), i);
                            // 个人所得税
                            this.getModel().setValue("tdkw_incometax", invalidValues.contains(salaryList.get(i).get("pit")) ? 0 : salaryList.get(i).get("pit"), i);
                            // 税后扣款
                            this.getModel().setValue("tdkw_after_tax_deduction", invalidValues.contains(salaryList.get(i).get("afterTaxDeduction")) ? 0 : salaryList.get(i).get("afterTaxDeduction"), i);
                        }
                    } else {
                        this.getView().setVisible(false, "tdkw_salary_info");
                    }
                } else {
                    this.getView().setVisible(false, "tdkw_salary_info");
                }
            } else {
                this.getView().setVisible(false, "tdkw_salary_info");
            }
        } else {
            this.getView().setVisible(false, "tdkw_salary_info");
        }
    }


    @Override
    public void closedCallBack(ClosedCallBackEvent close) {
        super.closedCallBack(close);
        String actionId = close.getActionId();
        if (StringUtils.equals("wordconversion1", actionId)) {
            Map<String, Object> returnData = (Map) close.getReturnData();
            if (!ObjectUtils.isEmpty(returnData)) {
                try {
                    this.getView().download((String) returnData.get("url"));
                    this.getView().showSuccessNotification("下载成功");
                    logger.info("人员完整度简历，简历下载成功：id" + (String) returnData.get("url"));
                } catch (Exception e) {
                    this.getView().getPageCache().put("flag", "true");
                    logger.error("人员完整度简历，简历下载失败，原因：" + e.getMessage());
                }
            }
        } else if (StringUtils.equals("donothing_login", actionId)) {
//            Map<String, Object> returnData = new HashMap<>();
            String returnData = "";
            try {
//                returnData = (Map) close.getReturnData();
                returnData = close.getReturnData().toString();
            } catch (Exception e) {
                logger.error("无返回值！");
            }
            if ("okok".equals(returnData)) {
                Map<String, String> customParameter = SystemParamServiceHelper.loadCustomParameterFromCache(new CustomParam());
                String loginTime = null != (String) customParameter.get("LOGIN_LOSS_TIME") ? (String) customParameter.get("LOGIN_LOSS_TIME") : "3";
                String userLogin = RequestContext.get().getGlobalSessionId();
                loginLossTime.put(UserServiceHelper.getCurrentUserId() + "loginUser" + userLogin,
                        String.valueOf(UserServiceHelper.getCurrentUserId()), Integer.valueOf(loginTime) * 60);
                // 设置默认展开
                this.getPageCache().put(UserServiceHelper.getCurrentUserId() + "ifFlag", "false");
                Container tdkwSalaryInfo = this.getControl("tdkw_salary_info_entity");
                // 单据体展开
                tdkwSalaryInfo.setCollapse(false);
                Label salaryExpand = this.getControl("tdkw_salary_expand");
                salaryExpand.setText("隐藏");

            }
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate source = (FormOperate) args.getSource();
        String operateKey = source.getOperateKey();
        if (StringUtils.equals("wordconversion1", operateKey)) {
            DynamicObjectCollection perProWordTempDy = queryPerProWord();
            if (ObjectUtils.isEmpty(perProWordTempDy)) {
                this.getView().showErrorNotification("未找到对应模板！！！");
                args.setCancel(true);
            }
        }
    }

    private static DynamicObjectCollection queryPerProWord() {
        QFilter qFilter = new QFilter("status", "=", "C");
        qFilter.and("enable", "=", "1");
        return QueryServiceHelper.query("tdkw_hspm_wordconversion", "id,name,number", qFilter.toArray());
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        super.afterDoOperation(e);
        String operateKey = e.getOperateKey();
        if (StringUtils.equals(operateKey, "wordconversion1")) {
            DynamicObjectCollection perProWordTempDy = queryPerProWord();
            String sid = (String) this.getView().getFormShowParameter().getCustomParams().get("erfileId");
            logger.info("获取下载sid" + sid);
            long id = 0;
            if (sid != null && !StringUtils.equals("", sid)) {
                QFilter[] qFilters = (new QFilter("tdkw_pkid", "=", sid)).and(new QFilter("iscurrentversion", "=", "1")).toArray();
                DynamicObject[] persons = BusinessDataServiceHelper.load("hrpi_person", "id,name,number", qFilters);
                // 人员在入职
                if (persons.length > 1) {
                    for (DynamicObject dynamicObject : persons) {
                        String number = dynamicObject.getString("number");
                        if (!number.contains("R")) {
                            id = dynamicObject.getLong("id");
                            break;
                        }
                    }
                } else {
                    // 获取需要查询的人员id信息
                    id = persons[0].getLong("id");
                }
            } else {
                id = (long) this.getModel().getValue("tdkw_bigintfield");
            }
            // DynamicObject primaryErmanfFile = HSPMServiceHelper.getPrimaryErmanfFile(id);
            QFilter filter = new QFilter("person", QCP.equals, id);
            filter.and("datastatus", QCP.equals, "1");
            filter.and("businessstatus", QCP.equals, "1");
            filter.and("iscurrentversion", QCP.equals, "1");
            DynamicObject empList = BusinessDataServiceHelper.loadSingle("hrpi_employee", "id,person,laborreltype.number", filter.toArray());
            List<Long> empIds = Collections.singletonList(empList.getLong("id"));
            DynamicObject[] erFileDys = EmployeeInforUtil.listPrimaryErmanfFileByEmployeeIds(empIds);
            DynamicObject primaryErmanfFile = new DynamicObject();
            if (erFileDys.length > 0) {
                primaryErmanfFile = erFileDys[0];
            }
            if (Objects.isNull(primaryErmanfFile)) {
                this.getView().showTipNotification("未找到该人员的人事业务档案，请联系管理员！");
                return;
            }
            QFilter qFilter = new QFilter("id", QCP.equals, primaryErmanfFile.get("id"));
            DynamicObjectCollection ermanFileColl = QueryServiceHelper.query("hspm_ermanfile", "person.id,person.name", qFilter.toArray());
            FormShowParameter showParameter = new FormShowParameter();
            OpenStyle openStyle = showParameter.getOpenStyle();
            openStyle.setShowType(ShowType.Modal);
            showParameter.setFormId("tdkw_hspm_wordtempprin_ex");
            showParameter.setCloseCallBack(new CloseCallBack(this, "wordconversion1"));
            showParameter.setCustomParam("wordTemplate", perProWordTempDy);
            showParameter.setCustomParam("ermanFile", ermanFileColl);
            showParameter.setCaption("请选择简历下载模板");
            this.getView().showForm(showParameter);
        }
    }

    /**
     * 查询单据所在的应用的Id，有原生应用就取原生的，
     * 注意：这不是查实体发布的应用，
     * 会先查询缓存的
     *
     * @param entityNumber 单据实体标识
     * @return appId
     */
    private static String getAppId(String entityNumber) {
        String cacheType = HRUserRoleCacheUtils.getTypeEntityApp();
        // 查询缓存中的数据
        String cacheVal = HRUserRoleCacheUtils.getCache(cacheType, entityNumber);
        if (StringUtils.isEmpty(cacheVal)) {
            QFilter qFilter = new QFilter("number", "=", entityNumber);
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("bos_entityobject", "id,bizappid.id", qFilter.toArray());
            if (dynamicObject == null) {
                throw new KDBizException(String.format("该实体元数据:%s 不存在", entityNumber));
            }
            String bizappid = dynamicObject.getString("bizappid.id");
            String masterid = HRRoleAndPersonUtils.getBaseStr(bizappid, "id", "bos_devportal_bizapp", "masterid");
            if (StringUtils.isNotEmpty(masterid)) {
                bizappid = masterid;
            }
            // 写入缓存
            HRUserRoleCacheUtils.putCache(cacheType, entityNumber, bizappid);
            return bizappid;
        } else {
            return cacheVal;
        }

    }

    // 新增处理单个员工列表的方法
    private static List<DynamicObject> processEmpList(List<DynamicObject> dynamicObjects) {
        List<Interval> mergedIntervals = DateIntervalMerger.mergeIntervals(
                dynamicObjects.stream().map(dynamicObject -> dynamicObject.getDate("startdate")).collect(Collectors.toList()),
                dynamicObjects.stream().map(dynamicObject -> dynamicObject.getDate("enddate")).collect(Collectors.toList())
        );

        if (dynamicObjects.size() == mergedIntervals.size()) {
            return dynamicObjects;
        }

        List<Long> keepIds = new ArrayList<>();
        List<DynamicObject> keepList = new ArrayList<>();
        Map<Long, DynamicObject> idToObjectMap = dynamicObjects.stream().collect(Collectors.toMap(dynamicObject -> dynamicObject.getLong("id"), Function.identity()));

        for (Interval interval : mergedIntervals) {
            Date start = interval.start;
            Date end = interval.end;

            List<Long> perKeepIds = dynamicObjects.stream()
                    .filter(emp -> start.equals(emp.getDate("startdate")) &&
                            end.equals(emp.getDate("enddate")))
                    .map(dynamicObject -> dynamicObject.getLong("id"))
                    .collect(Collectors.toList());

            keepIds.addAll(perKeepIds);
            keepList.addAll(perKeepIds.stream().map(idToObjectMap::get).collect(Collectors.toList()));
        }

        List<DynamicObject> deleteList = dynamicObjects.stream()
                .filter(emp -> !keepIds.contains(emp.getLong("id")))
                .collect(Collectors.toList());

        for (int i = 0; i < mergedIntervals.size() - keepIds.size(); i++) {
            deleteList.get(i).set("startdate", mergedIntervals.get(i).start);
            deleteList.get(i).set("enddate", mergedIntervals.get(i).end);
        }

        List<DynamicObject> newMergeList = new ArrayList<>(deleteList.subList(0, mergedIntervals.size() - keepIds.size()));
        newMergeList.addAll(keepList);

        return newMergeList;
    }

    /**
     * 设置其他社会团体经历
     *
     * @param id
     * @param anchorItems
     */
    private void setOtherSocialGroup(long id, List<AnchorItems> anchorItems) {
        // 查询数据
        QFilter qFilter = new QFilter("person.id", QCP.equals, id);
        qFilter.and("iscurrentversion", QCP.equals, "1");
        qFilter.and("datastatus", QCP.equals, "1");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DynamicObject[] otherSocialList = BusinessDataServiceHelper.load("tdkw_hrpi_social_group",
                "id,tdkw_start_date,tdkw_end_date,tdkw_group_name,tdkw_position_name,tdkw_other_info",
                qFilter.toArray(), "tdkw_start_date desc");
        if (otherSocialList.length > 0) {
            AnchorControl anchorControl = getControl("tdkw_anchorcontrolap");
            anchorItems.add(new AnchorItems("tdkw_other_social_group", "其他社会团体经历", new ArrayList<>()));
            anchorControl.addItems(anchorItems);
            if (otherSocialList.length > 1) {
                this.getModel().batchCreateNewEntryRow("tdkw_social_group", otherSocialList.length - 1);
            }
            for (int i = 0; i < otherSocialList.length; i++) {
                DynamicObject socialDy = otherSocialList[i];
                logger.info("其他社会团体经历：" + socialDy);
                if (socialDy.get("tdkw_start_date") != null) {
                    this.getModel().setValue("tdkw_start_date", dateFormat.format(socialDy.getDate("tdkw_start_date")), i);
                }
                if (socialDy.get("tdkw_end_date") != null) {
                    this.getModel().setValue("tdkw_end_date", dateFormat.format(socialDy.getDate("tdkw_end_date")), i);
                }
                if (socialDy.get("tdkw_group_name") != null) {
                    this.getModel().setValue("tdkw_group_name", socialDy.getString("tdkw_group_name"), i);
                }
                if (socialDy.get("tdkw_position_name") != null) {
                    this.getModel().setValue("tdkw_position_name", socialDy.getString("tdkw_position_name"), i);
                }
                if (socialDy.get("tdkw_other_info") != null) {
                    this.getModel().setValue("tdkw_other_info", socialDy.getString("tdkw_other_info"), i);
                }
            }
        } else {
            this.getView().setVisible(false, "tdkw_other_social_group");
        }
    }
}
