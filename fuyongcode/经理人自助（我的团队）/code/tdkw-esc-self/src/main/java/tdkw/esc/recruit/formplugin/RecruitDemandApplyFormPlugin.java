package tdkw.esc.recruit.formplugin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.ConfirmTypes;
import kd.bos.form.FormShowParameter;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.OpenStyle;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.Label;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.field.TextEdit;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author xyliusn
 * @description 招聘申请单表单插件
 * @date 2023/5/20
 */
public class RecruitDemandApplyFormPlugin extends AbstractFormPlugin implements ClickListener {

    private final static Log log = LogFactory.getLog(RecruitDemandApplyFormPlugin.class);

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
        // 展示当前用户所属部门，创建人，创建日期，当前处理人
        String billNo = (String) this.getModel().getValue("billno");
        QFilter[] qFilters = new QFilter("billno", QCP.equals, billNo).toArray();
        DynamicObject object = BusinessDataServiceHelper.loadSingle( "tdkw_rec_apply_bill",qFilters);
        if (object != null) {
            DynamicObject creator = object.getDynamicObject("creator");
            DynamicObject creatorDept = object.getDynamicObject("tdkw_created_dept");
            Date creatorDate = object.getDate("tdkw_apply_date");
            setHeaderInfo(creator, creatorDept, creatorDate);
        } else {
            setHeaderInfo(null, null, null);
        }
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
        // 设置单据编号
        Label billNoLabel = this.getView().getControl("tdkw_billno");
        String billNo = (String) this.getModel().getValue("billno");
        billNoLabel.setText(billNo);
        Object billStatus = this.getModel().getValue("billstatus");
        // 设置表头
        Date applyDate = (Date) this.getModel().getValue("tdkw_apply_date");
        DynamicObject createdDept = (DynamicObject) this.getModel().getValue("tdkw_created_dept");
        DynamicObject creator = (DynamicObject) this.getModel().getValue("creator");
        setHeaderInfo(creator, createdDept, applyDate);
        getView().setVisible(false, "tdkw_internation");
        if (this.getModel().getValue("tdkw_rec_apply_company") != null) {
            DynamicObject company = (DynamicObject) this.getModel().getValue("tdkw_rec_apply_company");
            if (isStockBoard(company.getLong("id"))) {
                getView().setVisible(true, "tdkw_internation");
            }
        }
    }

    /**
     * 设置表头信息
     *
     * @author xysusj
     * @date 17:44 2023/6/26
     **/
    private void setHeaderInfo(DynamicObject creator, DynamicObject creatorDept, Date creatorDate) {
        if (creator == null) {
            long userId = UserServiceHelper.getCurrentUserId();
            long userMainOrgId = UserServiceHelper.getUserMainOrgId(userId);
            creatorDept = BusinessDataServiceHelper.loadSingle(userMainOrgId, "bos_org");
            creator = BusinessDataServiceHelper.loadSingle(userId, "bos_user");
            creatorDate = new Date();
        }

        String orgName = creatorDept.getString("name");
        Label creatorDeptLabel = this.getView().getControl("tdkw_belong_dept");
        this.getModel().setValue("tdkw_created_dept", creatorDept);
        creatorDeptLabel.setText(orgName);

        String userName = creator.getString("name");
        Label creatorLabel = this.getView().getControl("tdkw_creator");
        creatorLabel.setText(userName);

        Label createDateLabel = this.getView().getControl("tdkw_create_date");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        createDateLabel.setText(simpleDateFormat.format(creatorDate));

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
                // 打开流程节点选择弹框
            }
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
        // 给菜单栏所有按钮添加事件监听
        this.addItemClickListeners("tbmain");
        // 北森招聘hr文本按钮添加点击事件
        TextEdit textEdit = this.getView().getControl("tdkw_rec_apply_hr_name");
        textEdit.addButtonClickListener(this);
    }


    /**
     * 关闭回调
     * 北森hr页面关闭将值写回父页面
     *
     * @param closedCallBackEvent
     * @author xysusj
     * @date 9:12 2023/6/19
     **/
    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (closedCallBackEvent.getReturnData() instanceof HashMap) {
            Map returnData = (Map) closedCallBackEvent.getReturnData();
            this.getModel().setValue("tdkw_rec_apply_hr_name", returnData.get("name"));
            this.getModel().setValue("tdkw_rec_apply_hr_dept", returnData.get("dept"));
            this.getModel().setValue("tdkw_rec_apply_hr_email", returnData.get("email"));
            this.getModel().setValue("tdkw_rec_apply_hr_pk", returnData.get("id"));
        }
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
        if ("tdkw_rec_apply_hr_name".equals(key)) {
            // 打开子页面弹窗
            FormShowParameter showParameter = new FormShowParameter();
            OpenStyle openStyle = showParameter.getOpenStyle();
            openStyle.setShowType(ShowType.Modal);
            showParameter.setFormId("tdkw_rec_apply_bill_hr");
            if (this.getModel().getValue("tdkw_rec_apply_company") != null) {
                String orgId = ((DynamicObject) this.getModel().getValue("tdkw_rec_apply_company")).getString("tdkw_pkid");
                showParameter.setCustomParam("orgId", orgId);
            }
            showParameter.setCloseCallBack(new CloseCallBack(this, "tdkw_rec_apply_bill_hr"));
            getView().showForm(showParameter);
        }
    }

    /**
     * 用户点击菜单按钮后，在执行按钮绑定的操作前，触发此事件
     * 提交检测
     *
     * @param evt
     */
    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        if ("bar_submit".equals(evt.getItemKey())) {
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
            // 调用编制数据进行编制校验【先按照：在职人数+需求人数不能大于编制人数】
            // 招聘人数
            Integer tdkwRecRecruitNum = (Integer) this.getModel().getValue("tdkw_rec_recruit_num");
            // 申请部门
            DynamicObject tdkwRecApplyDept = (DynamicObject) this.getModel().getValue("tdkw_rec_apply_dept");
            // 申请部门id
            Object pkValue = tdkwRecApplyDept.getPkValue();
            log.info(String.format("调用中台服务云获取编制人数入参.招聘人数=%s", tdkwRecRecruitNum));
            log.info(String.format("调用中台服务云获取编制人数入参.招聘部门id=%s", pkValue));

            evt.setCancel(true);
            return;
            // TODO 二开依赖
           /* Object result;
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
            }*/
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
        // 当申请公司改变时申请部门置为null，防止展示其他公司下的部门
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
        // 当申请部门改变时内部职位置为null，防止展示其他部门下的内部职位
        if ("tdkw_rec_apply_dept".equals(e.getProperty().getName())) {
            this.getModel().setValue("tdkw_rec_in_posi_name", null);
        }
        // 当招聘HR清空时、同时清除招聘HR邮箱、ID
        if ("tdkw_rec_apply_hr_name".equals(e.getProperty().getName())) {
            this.getModel().setValue("tdkw_rec_apply_hr_email", null);
            this.getModel().setValue("tdkw_rec_apply_hr_pk", null);
        }
    }

    private boolean isStockBoard(Long orgId) {
        /*log.info("orgId = {}", orgId);
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
        return false;*/
        return false;
    }

}
