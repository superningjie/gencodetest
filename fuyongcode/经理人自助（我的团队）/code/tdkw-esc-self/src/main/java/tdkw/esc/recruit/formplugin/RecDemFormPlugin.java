package tdkw.esc.recruit.formplugin;


import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.haos.business.servicehelper.AdminOrgQueryServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : xyliusn
 * @version : 1.0
 * @description : 招聘需求申请F7插件
 * @date : 2023/5/4 9:24
 */
public class RecDemFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {
    private static final Log logger = LogFactory.getLog(RecDemFormPlugin.class);

    /**
     * 注册监听事件
     *
     * @param e
     * @author xysusj
     * @date 9:09 2023/6/19
     **/
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
        this.addClickListeners("tdkw_rec_apply_hr_name");
    }

    /**
     * 监听f7
     *
     * @param beforeF7SelectEvent
     * @author xysusj
     * @date 9:09 2023/6/19
     **/
    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        String key = beforeF7SelectEvent.getProperty().getName();
        if ("tdkw_rec_apply_company".equals(key)) {
            /*// 只展示公司类型数据
            ListShowParameter param = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            // "XY00005","XY00002" 创建List<String> orgTypes 使用asList
            String[] orgTypeArray = {"XY00005", "XY00002", "XY00001", "XY00003"};
            List<String> orgTypes = Arrays.asList(orgTypeArray);
            QFilter qFilter = new QFilter("orgtype.number", QCP.in, orgTypes);
            param.getListFilterParameter().getQFilters().add(qFilter);*/
        }
        if ("tdkw_rec_apply_dept".equals(key)) {
            //只展示所选申请公司下的部门
            long companyId = ((DynamicObject) this.getModel().getValue("tdkw_rec_apply_company")).getLong(HRBaseConstants.ID);
            ListShowParameter param = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            logger.info("AdminOrgQueryServiceHelper.batchQueryAllSubOrg入参={}",companyId);
            List<Map<String, Object>> subOrgs = AdminOrgQueryServiceHelper.batchQueryAllSubOrg(Collections.singletonList(companyId), new Date());
            List<Long> subOrgIds = subOrgs.stream().map(i -> (Long) i.get("orgId")).collect(Collectors.toList());
            logger.info("AdminOrgQueryServiceHelper.batchQueryAllSubOrg返回参数={}",subOrgs);
            logger.info("subOrgIds={}",subOrgIds);
            // 只展示部门类型数据  1020_S 公司、1040_S 部门
            QFilter qFilter = new QFilter("id", QCP.in, subOrgIds);
//            List<String> deptList = Arrays.asList("XY00006","XY00007", "XY00008");
//            qFilter.and("orgtype.number", QCP.in, deptList);
            param.getListFilterParameter().getQFilters().add(qFilter);
        }
        if ("tdkw_rec_in_posi_name".equals(key)) {
            //只展示所选申请部门下的内部职位
            /*long deptId = ((DynamicObject) this.getModel().getValue("tdkw_rec_apply_dept")).getLong(HRBaseConstants.ID);
            ListShowParameter positionParam = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            positionParam.getListFilterParameter().getQFilters().add(new QFilter("adminorg", QCP.equals, deptId));*/
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
}