package tdkw.hrmp.hrobs.formplugin.report;

import cfca.svs.api.util.StringUtil;
import com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.report.ReportView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.*;

/**
 * @author xxx
 * @Date 2023/6/29 10:08
 * @Description 人员详情报表 报表表单插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personinforpt
 * @Version 1.0
 **/
public class PersonInfoReportFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {
    private static final Log logger = LogFactory.getLog(PersonInfoReportFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent evt) {
        if (StringUtils.equals("person", evt.getFieldName())) {
            //点击获取人事业务档案的主键(隐藏列)
            DynamicObject rowData = evt.getRowData();
            //参数为查询配置的查询字段的标识
            Long personId = (Long) evt.getRowData().getDynamicObject("person").getPkValue();
            QFilter qFilter = new QFilter("id", QCP.equals, personId);
//            DynamicObject person = BusinessDataServiceHelper.loadSingle("hrpi_person", "tdkw_pkid", qFilter.toArray());
            DynamicObject person = BusinessDataServiceHelper.loadSingle("hrpi_person", qFilter.toArray());
//            String pkId = person.getString("tdkw_pkid");
            String pkId = person.getString(HRBaseConstants.ID);
            //查询人员工号，判断是否在入职
            String personNumber = rowData.getString("person.number");
            logger.info("跳转的人员pkid为" + pkId);
            logger.info("跳转的人员personNumber为" + personNumber);
            //人员是否入职
            boolean ifReEmp = false;
            //人员是否有权限
            boolean isOrgPerm = false;
            //判断是否再入职
            /*if (personNumber.length() > 6) {
                //截取前6位人员工号
                personNumber = personNumber.substring(0, 6);
                //人员再入职
                ifReEmp = true;
            }*/
            //在入职人员获取最新任职经历中对应部门
            Long personAmdinorg =null;
            DynamicObjectCollection objectCollection = null;
            if (ifReEmp) {
                //人员员工号
                QFilter erManFileQfilter = new QFilter("person.number", QCP.equals, personNumber);
                //初始化状态
                erManFileQfilter.and("initstatus", QCP.equals, "2");
                //是否当前版本
                erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
                //数据版本状态
                erManFileQfilter.and("datastatus", QCP.equals, "1");
                //业务档案状态
                erManFileQfilter.and("businessstatus", QCP.equals, "1");
//                erManFileQfilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");
                objectCollection = QueryServiceHelper.query("hspm_ermanfile", "empposrel.company.id as company,empposrel.adminorg.id as adminorg", new QFilter[]{erManFileQfilter});
            } else {
                QFilter erManFile = new QFilter("businessstatus", QCP.equals, "1");
                erManFile.and("person.id", QCP.equals, personId);
                erManFile.and("iscurrentversion", QCP.equals, Boolean.TRUE);
                //全职任职
                erManFile.and("empposrel.postype.number", QCP.equals, "1010_S");
                //erManFile.and("filetype.postype.number", QCP.equals, "XY00001");
                erManFile.and("empposrel.datastatus", QCP.equals, "1");
                erManFile.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
//                erManFile.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");
                objectCollection = QueryServiceHelper.query("hspm_ermanfile", "empposrel.adminorg as adminorg,empposrel.company.id as company", new QFilter[]{erManFile});
            }
            //获取当前人员权限
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_lz_rybhqs_pc");
            if (!result.isHasAllOrgPerm()) {
                List<Long> hasPerOrg = result.getHasPermOrgs();
                Set<Long> allOrg = new HashSet<>();
                if(hasPerOrg.size() > 0) {
                    if(ObjectUtils.isNotEmpty(objectCollection)) {
                        for (DynamicObject obj : objectCollection) {
                            if ( ObjectUtils.isNotEmpty(obj.getLong("company")) ){
                                allOrg.add(obj.getLong("company"));
                            }
                            if( ObjectUtils.isNotEmpty(obj.getLong("adminorg")) ){
                                allOrg.add(obj.getLong("adminorg"));
                            }
                        }
                        logger.info("所有组织数据" + allOrg);
                        for (Long aLong : allOrg) {
                            if(hasPerOrg.contains(aLong)) {
                                isOrgPerm = Boolean.TRUE;
                            }
                        }
                    }
                }
            } else {
                isOrgPerm = true;
            }
            if (isOrgPerm) {
                try {
                    FormShowParameter formShowParameter = new FormShowParameter();
                    formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                    formShowParameter.setCustomParam("erfileId", pkId);
                    formShowParameter.setCustomParam("isLeave", Boolean.TRUE);
                    formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                    formShowParameter.setHasRight(true);
                    this.getView().showForm(formShowParameter);
                } catch (Exception e) {
                    this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "SkipToThisMonthOutReportFormPlugin", "tdkw-esc-hrobs-formplugin-ext", new Object[0]));
                }
            } else {
                this.getView().showErrorNotification("该员工已入职XXX内其他组织，您无权查看当前人员的详细信息");
            }
        }

//        if (StringUtils.equals("person", evt.getFieldName())) {
//            Long personId = (Long) evt.getRowData().getDynamicObject("person").getPkValue();
//            QFilter qFilter = new QFilter("id", QCP.equals, personId);
//            DynamicObject person = BusinessDataServiceHelper.loadSingle("hrpi_person", "tdkw_pkid", qFilter.toArray());
//            String pkId = person.getString("tdkw_pkid");
//            Logger.info("跳转的人员pkid为" + pkId);
//
//            try {
//                FormShowParameter formShowParameter = new FormShowParameter();
//                formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
//                formShowParameter.setCustomParam("erfileId", pkId);
//                formShowParameter.setCustomParam("isLeave", Boolean.TRUE);
//                formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
//                formShowParameter.setHasRight(true);
//                this.getView().showForm(formShowParameter);
//            } catch (Exception e) {
//                this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "PersonInfoReportFormPlugin", "tdkw-esc-hrobs-formplugin-ext", new Object[0]));
//            }
//        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        ReportView source = (ReportView) e.getSource();
        String personIds = source.getFormShowParameter().getCustomParam("personIds");
        String personIdsDepart = source.getFormShowParameter().getCustomParam("personIdsDepart");
        if (StringUtils.equals(personIdsDepart, "[]")) {
            this.getView().showErrorNotification("信息为空！请返回！");
            return;
        }
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtil.isNotEmpty(personIds)) {
            params.put("personIds_a", personIds);
        } else {
            params.put("personIds_depart", personIdsDepart);
        }
        source.getQueryParam().setCustomParam(params);
        source.refresh();
    }
}