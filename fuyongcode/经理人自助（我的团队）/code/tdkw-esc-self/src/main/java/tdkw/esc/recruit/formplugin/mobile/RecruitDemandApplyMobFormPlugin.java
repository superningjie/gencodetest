package tdkw.esc.recruit.formplugin.mobile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.*;
import kd.bos.form.control.Control;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.list.MobileListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.haos.business.servicehelper.AdminOrgQueryServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 招聘申请单移动端表单插件
 *
 * @author xysusj
 */
public class RecruitDemandApplyMobFormPlugin extends AbstractMobFormPlugin implements BeforeF7SelectListener {

    private static final Log log = LogFactory.getLog(RecruitDemandApplyMobFormPlugin.class);

    /**
     * @param
     * @return void
     * @description 数据包创建之后的事件
     * @author xyliusn
     * @date 2023/5/21
     */
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //展示当前用户所属部门，创建人，创建日期，当前处理人
        long userId = UserServiceHelper.getCurrentUserId();
        long userMainOrgId = UserServiceHelper.getUserMainOrgId(userId);
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(userMainOrgId, "bos_org");
        this.getModel().setValue("tdkw_created_dept", dynamicObject);
    }

    /**
     * 表单界面数据绑定完毕事件
     * 设置单据编号
     *
     * @param e
     * @author xysusj
     * @date 9:11 2023/6/19
     **/
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        getView().setVisible(false, "tdkw_internation");
        if (this.getModel().getValue("tdkw_rec_apply_company") != null) {
            DynamicObject company = (DynamicObject) this.getModel().getValue("tdkw_rec_apply_company");
            if (isStockBoard(company.getLong("id"))) {
                getView().setVisible(true, "tdkw_internation");
            }
        }
        // 单据状态
//        Object status = this.getModel().getValue("billstatus");
//        if ("B".equalsIgnoreCase(String.valueOf(status))) {
//            this.getView().setVisible(Boolean.FALSE, "tdkw_rec_open_hr");
//        }
        Object process = this.getModel().getValue("tdkw_conprocess");
//        if ("BPM".equalsIgnoreCase(String.valueOf(process))) {
//            this.getView().setVisible(Boolean.TRUE, "tdkw_mbaritemap");
//        } else {
//            this.getView().setVisible(Boolean.FALSE, "tdkw_mbaritemap");
//        }
    }

    /**
     * 提示确认后的回调事件
     *
     * @param evt
     * @author xysusj
     * @date 9:12 2023/6/19
     **/
    @Override
    public void confirmCallBack(MessageBoxClosedEvent evt) {
        super.confirmCallBack(evt);
        String callBackId = evt.getCallBackId();
        if (StringUtils.equals("establishmentCheck", callBackId)) {
            if (evt.getResult() == MessageBoxResult.Yes) {
                this.getView().invokeOperation("submit");
                //打开流程节点选择弹框
            }
            // 标识置为空，使得再点击单据提交按钮时能触发弹框
            this.getModel().setValue("tdkw_rec_submit_flag", null);
        }
    }

    /**
     * 注册按钮监听事件
     * 任何事件触发都要进行注册监听！！！
     *
     * @param e
     */
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //监听申请部门，筛选出申请公司下的部门
        BasedataEdit applyDept = this.getView().getControl("tdkw_rec_apply_dept");
        // 申请公司
        BasedataEdit applyCompany = this.getView().getControl("tdkw_rec_apply_company");
        //监听内部职位，筛选出申请部门下的职位
        BasedataEdit inPosiName = this.getView().getControl("tdkw_rec_in_posi_name");
        //监听汇报对象，筛选出对应公司下的人员
        BasedataEdit recReportToPerson = this.getView().getControl("tdkw_rec_report_to_person");
        applyDept.addBeforeF7SelectListener(this);
        applyCompany.addBeforeF7SelectListener(this);
        inPosiName.addBeforeF7SelectListener(this);
        recReportToPerson.addBeforeF7SelectListener(this);
        this.addClickListeners("tdkw_rec_open_hr", "tdkw_rec_submit");
    }

    /**
     * 按钮点击事件触发，通过Key判断触发来源执行不同操作。
     * 点击hr打开打开北森hr列表动态表单
     */
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control control = (Control) evt.getSource();
        String key = control.getKey();
        if ("tdkw_rec_open_hr".equals(key)) {
            //打开子页面弹窗
            FormShowParameter showParameter = new FormShowParameter();
            OpenStyle openStyle = showParameter.getOpenStyle();
            openStyle.setShowType(ShowType.Modal);
            showParameter.setFormId("tdkw_rec_demand_mob_hr");
            if (this.getModel().getValue("tdkw_rec_apply_company") != null) {
                String orgId = ((DynamicObject) this.getModel().getValue("tdkw_rec_apply_company")).getString("tdkw_pkid");
                showParameter.setCustomParam("orgId", orgId);
            }
            showParameter.setCloseCallBack(new CloseCallBack(this, "tdkw_rec_apply_bill_hr"));
            getView().showForm(showParameter);
        }
    }


    /**
     * 关闭回调
     * 北森hr页面关闭将值写回父页面
     *
     * @param e
     * @author xysusj
     * @date 9:12 2023/6/19
     **/
    @Override
    public void closedCallBack(ClosedCallBackEvent e) {
        super.closedCallBack(e);
        String actionId = e.getActionId();
        if (StringUtils.equalsIgnoreCase(actionId, "tdkw_rec_apply_bill_hr") && e.getReturnData() != null) {
            Map returnData = (Map) e.getReturnData();
            this.getModel().setValue("tdkw_rec_apply_hr_name", returnData.get("name"));
            this.getModel().setValue("tdkw_rec_apply_hr_dept", returnData.get("dept"));
            this.getModel().setValue("tdkw_rec_apply_hr_email", returnData.get("email"));
            this.getModel().setValue("tdkw_rec_apply_hr_pk", returnData.get("id"));
        }
    }

    /**
     * 提交
     * 提交检测
     *
     * @param evt
     */
    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs evt) {
        FormOperate source = (FormOperate) evt.getSource();
        String operateKey = source.getOperateKey();
        String tdkwRecSubmitFlag = String.valueOf(this.getModel().getValue("tdkw_rec_submit_flag"));
        if (StringUtils.equalsIgnoreCase("submit", operateKey) && StringUtils.isEmpty(tdkwRecSubmitFlag)) {
            // 校验薪资范围
            BigDecimal minSalary = (BigDecimal) this.getModel().getValue("tdkw_rec_min_salary");
            BigDecimal maxSalary = (BigDecimal) this.getModel().getValue("tdkw_rec_max_salary");
            if (Objects.nonNull(minSalary) && Objects.nonNull(maxSalary)) {
                if (minSalary.compareTo(maxSalary) > 0) {
                    this.getView().showTipNotification("最低薪酬不能大于最高薪资!");
                    evt.setCancel(true);
                    return;
                }
            }
            // 期望到岗日期、截止日期校验 /期望到岗时间必须大于需求创建时间(当前时间)
            Date arriveDate = (Date) this.getModel().getValue("tdkw_rec_arrive_date");
            Date deadline = (Date) this.getModel().getValue("tdkw_rec_deadline");
            if (deadline.before(arriveDate)) {
                this.getView().showTipNotification("截至日期不能小于期望到岗日期!");
                evt.setCancel(true);
                return;
            }
            // 招聘人数
            Integer tdkwRecRecruitNum = (Integer) this.getModel().getValue("tdkw_rec_recruit_num");
            // 申请部门
            DynamicObject tdkwRecApplyDept = (DynamicObject) this.getModel().getValue("tdkw_rec_apply_dept");
            // 申请部门id
            Object pkValue = tdkwRecApplyDept.getPkValue();
            log.info(String.format("调用中台服务云获取编制人数入参.招聘人数=%s", tdkwRecRecruitNum));
            log.info(String.format("调用中台服务云获取编制人数入参.招聘部门id=%s", pkValue));
            Object result;
            try {
                result = DispatchServiceHelper.invokeService(
                        "tdkw.hrmp.haos.servicehelper",
                        "haos",
                        "IWeaveInformationService",
                        "findInformation", tdkwRecRecruitNum, pkValue);
                log.info(String.format("调用中台服务云获取编制人数响应=%s", JSON.toJSONString(result)));
                if (Objects.isNull(result) || StringUtils.isBlank(JSON.toJSONString(result))) {
                    this.getView().showTipNotification("调用中台服务云获取编制人数返回为空，请联系管理员！");
                    evt.setCancel(true);
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(result));
            // 是否超编
            Boolean allow = jsonObject.getBoolean("allow");
            if (!allow) {
                // 1表示编制校验弹框的提交按钮
                this.getModel().setValue("tdkw_rec_submit_flag", 1);
                ConfirmCallBackListener confirmCallBackListener = new ConfirmCallBackListener("establishmentCheck", this);
                // 设置页面确认框，参数为：标题，选项框类型，回调监听
                this.getView().showConfirm("编制人数不足，是否继续提交申请？", MessageBoxOptions.OKCancel, ConfirmTypes.Wait, confirmCallBackListener);
                evt.setCancel(true);
                return;
            }
            // 设置创建时间（即提交时间）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
            this.getModel().setValue("tdkw_rec_apply_date", sdf.format(new Date()));
            // 设置申请日期（取提交日期)
            this.getModel().setValue("tdkw_submit_date", dateSdf.format(new Date()));
            // 设置申请日期（取提交日期)
            this.getModel().setValue("submitdate", new Date());
            // 岗位标签和职位子序列隐藏字段赋值
            DynamicObject tdkwRecInPosiName = (DynamicObject) this.getModel().getValue("tdkw_rec_in_posi_name");
            // 职位子序列隐藏字段赋值
            DynamicObject childSeq = (DynamicObject) tdkwRecInPosiName.get("tdkw_jobfamilyhr");
            if (Objects.nonNull(childSeq)) {
                if (StringUtils.isNotBlank(String.valueOf(childSeq.get("name")))) {
                    String seqName = childSeq.get("name").toString();
                    this.getModel().setValue("tdkw_rec_pos_seq_text", seqName);
                }
                // 设置职位子序列码值
                if (StringUtils.isNotBlank(String.valueOf(childSeq.get("number")))) {
                    String seqCode = childSeq.get("number").toString();
                    this.getModel().setValue("tdkw_rec_pos_seq_code", seqCode);
                }
            } else {
                this.getModel().setValue("tdkw_rec_pos_seq_code", StringUtils.EMPTY);
                this.getModel().setValue("tdkw_rec_pos_seq_text", StringUtils.EMPTY);
            }
            // 岗位标签隐藏字段赋值
            DynamicObjectCollection tdkwPostionTag = (DynamicObjectCollection) tdkwRecInPosiName.get("tdkw_postiontag");
            if (CollectionUtils.isNotEmpty(tdkwPostionTag)) {
                StringBuilder positionlabelName = new StringBuilder();
                for (DynamicObject entry : tdkwPostionTag) {
                    // 获取岗位标签对象
                    DynamicObject positionLabel = (DynamicObject) entry.get(1);
                    Long pk = (Long) positionLabel.getPkValue();
                    DynamicObject hbjmJobfamilyhr = BusinessDataServiceHelper.loadSingle(pk, "hbjm_jobfamilyhr");
                    if (Objects.nonNull(hbjmJobfamilyhr)) {
                        String name = BusinessDataServiceHelper.loadSingle(pk, "hbjm_jobfamilyhr").getString("name");
                        if (StringUtils.isNotEmpty(name)) {
                            positionlabelName.append(name).append(",");
                        }
                    }
                }
                //去除最后一个分号
                if (positionlabelName.length() >= 1) {
                    this.getModel().setValue("tdkw_rec_pos_label_text", positionlabelName.substring(0, positionlabelName.length() - 1));
                }
            } else {
                this.getModel().setValue("tdkw_rec_pos_label_text", StringUtils.EMPTY);
            }
        }
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        String key = beforeF7SelectEvent.getProperty().getName();
        if ("tdkw_rec_apply_company".equals(key)) {
            // 只展示公司类型数据
            MobileListShowParameter showParameter = (MobileListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            // 查询所有公司类型的数据、 移动端是层级显示，需要过滤   集团、板块、行业、公司
            String[] orgTypeArray = {"XY00005", "XY00002", "XY00001", "XY00003"};
            List<String> orgTypes = Arrays.asList(orgTypeArray);
            QFilter qFilter = new QFilter("orgtype.number", QCP.in, orgTypes);
            qFilter.and("enable", QCP.equals, "1");
            qFilter.and("iscurrentversion", QCP.equals, "1");
            qFilter.and("datastatus", QCP.equals, "1");
            qFilter.and("status", QCP.equals, "C");
            DynamicObject[] adminOrgHrs = BusinessDataServiceHelper.load("haos_adminorghr", "id,number", qFilter.toArray());
            Set<Long> number = Arrays.stream(adminOrgHrs).map(dynamicObject -> dynamicObject.getLong("id")).collect(Collectors.toSet());
            QFilter orgIdFilter = new QFilter("id", QCP.in, number);
            showParameter.getListFilterParameter().getQFilters().add(orgIdFilter);
        }
        if ("tdkw_rec_apply_dept".equals(key)) {
            //只展示所选申请公司下的部门
            long companyId = ((DynamicObject) this.getModel().getValue("tdkw_rec_apply_company")).getLong(HRBaseConstants.ID);
            List<Map<String, Object>> subOrgs = AdminOrgQueryServiceHelper.batchQueryAllSubOrg(Collections.singletonList(companyId), new Date());
            List<Long> subOrgIds = subOrgs.stream().map(i -> (Long) i.get("orgId")).collect(Collectors.toList());
            // 只展示部门类型数据  1020_S 公司、1040_S 部门
            QFilter qFilter = new QFilter("id", QCP.in, subOrgIds);
//            List<String> deptList = Arrays.asList("XY00006","XY00007", "XY00008");
//            qFilter.and("orgtype.number", QCP.in, deptList);
            MobileListShowParameter showParameter = (MobileListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            showParameter.getListFilterParameter().setFilter(qFilter);
        }
        if ("tdkw_rec_in_posi_name".equals(key)) {
            //只展示所选申请部门下的内部职位
            long deptId = ((DynamicObject) this.getModel().getValue("tdkw_rec_apply_dept")).getLong(HRBaseConstants.ID);
            ListShowParameter positionParam = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            positionParam.getListFilterParameter().getQFilters().add(new QFilter("adminorg", QCP.equals, deptId));
            positionParam.getListFilterParameter();
        }
        if ("tdkw_rec_report_to_person".equals(key)) {
            //筛选出对应公司下的人员
            long companyId = ((DynamicObject) this.getModel().getValue("tdkw_rec_apply_company")).getLong(HRBaseConstants.ID);
            ListShowParameter companyParam = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            QFilter qFilter = new QFilter("empposrel.company", QCP.equals, companyId);
            QFilter qfilter = new QFilter("empposrel.tdkw_changereason.number",QCP.not_equals,"XY00017").and("filetype.number", QCP.not_equals,"1050_S");
            companyParam.getListFilterParameter().getQFilters().add(qFilter.and(qfilter));
            companyParam.getListFilterParameter();
        }
    }


    /**
     * 监听属性改变事件
     *
     * @param e
     * @author xysusj
     * @date 9:09 2023/6/19
     **/
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        ChangeData[] changeSet = e.getChangeSet();

        //当申请公司改变时申请部门置为null，防止展示其他公司下的部门
        if ("tdkw_rec_apply_company".equals(e.getProperty().getName())) {
            this.getModel().setValue("tdkw_rec_apply_dept", null);
            //如果公司为股份及股份以下的则，展示是否国际化
            this.getModel().setValue("tdkw_internation", false);
            DynamicObject company = (DynamicObject) changeSet[0].getNewValue();
            if (isStockBoard(company.getLong("id"))) {
                getView().setVisible(true, "tdkw_internation");
            } else {
                getView().setVisible(false, "tdkw_internation");
            }
        }
        //当申请部门改变时内部职位置为null，防止展示其他部门下的内部职位
        if ("tdkw_rec_apply_dept".equals(e.getProperty().getName())) {
            this.getModel().setValue("tdkw_rec_in_posi_name", null);
        }
        //当招聘HR清空时、同时清除招聘HR邮箱、ID
        if ("tdkw_rec_apply_hr_name".equals(e.getProperty().getName())) {
            this.getModel().setValue("tdkw_rec_apply_hr_email", null);
            this.getModel().setValue("tdkw_rec_apply_hr_pk", null);
        }
    }

    private boolean isStockBoard(Long orgId) {
        log.info("orgId = {}", orgId);
        DynamicObject adminOrg = BusinessDataServiceHelper.loadSingle(orgId, "haos_adminorghr");
        DynamicObject sector = adminOrg.getDynamicObject("tdkw_sectorid");
        if (sector == null) {
            return false;
        }
        sector = BusinessDataServiceHelper.loadSingle(sector.getLong("id"), sector.getDataEntityType().getName());
        //查询不到数据也不调用接口
        if ("象屿股份".equals(sector.getString("name"))) {
            return true;
        }
        return false;
    }

}
