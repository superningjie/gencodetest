package tdkw.hrmp.hrobs.formplugin.induction;

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
 * @Description 入职分析 报表表单插件
 * @Demander xxx
 * @Document PC端人力自助需规V0.3_0619(2)、https://www.kdocs.cn/l/cdDKeoQxzuPH
 * @Basedata tdkw_personinforpt
 * @Version 1.0
 **/
public class InductionPersonInfoReportFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {
    private static final String ROLE_ORG = "tdkw_rz_rybhqs_pc";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
    }

    private static final Log logger = LogFactory.getLog(InductionPersonInfoReportFormPlugin.class);

    @Override
    public void hyperLinkClick(HyperLinkClickEvent evt) {
        if (StringUtils.equals("person", evt.getFieldName())) {
            // 点击获取人事业务档案的主键(隐藏列)
            DynamicObject rowData = evt.getRowData();
            // 参数为查询配置的查询字段的标识  跳转参数
            String personId = rowData.getString("person.id");
            String pkID = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person").getString(HRBaseConstants.ID);
            // 人员工号 查询组织用
            String personNumber = rowData.getString("person.number");
            // 判断权限
            boolean jurisdiction = Boolean.FALSE;
            // 判断离职
            boolean dimission = Boolean.FALSE;
            // 查询最新人员
            if (personNumber.length() != 6) {
                personNumber = personNumber.substring(0, 6);
            }
            // 人事业务档案过滤条件
            QFilter erManFileQfilter = new QFilter("person.number", QCP.equals, personNumber);
            erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
            erManFileQfilter.and("datastatus", QCP.equals, "1");
            erManFileQfilter.and("businessstatus", QCP.equals, "1");
//            erManFileQfilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");
            DynamicObjectCollection ermanfile = QueryServiceHelper.query("hspm_ermanfile", "empposrel.adminorg.id as adminorg,empposrel.company.id as company,empentrel.laborrelstatus.number as number", new QFilter[]{erManFileQfilter});

            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), ROLE_ORG);// 如果角色控权包含10000L的话 不对权限进行控制
            boolean hasAllOrgPerm = result.isHasAllOrgPerm();

            // 判断是否能穿透
            if (!hasAllOrgPerm) {
                List<Long> hasPerOrg = result.getHasPermOrgs();
                Set<Long> allOrg = new HashSet<>();
                if(hasPerOrg.size() > 0) {
                    if(ObjectUtils.isNotEmpty(ermanfile)) {
                        for (DynamicObject obj : ermanfile) {
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
                                jurisdiction = Boolean.TRUE;
                            }
                        }
                    }
                }
            } else {
                jurisdiction = Boolean.TRUE;
            }

            if (jurisdiction) {
                try {
                    for (DynamicObject obj : ermanfile) {
                        String number = obj.getString("number");
                        if(StringUtils.isNotEmpty(number)) {
                            if (StringUtils.equals(number, "XY00008") || StringUtils.equals(number, "XY00009") || StringUtils.equals(number, "XY000121") || StringUtils.equals(number, "XY00013")) {
                                dimission = Boolean.TRUE;
                            }
                        }
                    }
                    FormShowParameter formShowParameter = new FormShowParameter();
                    formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                    formShowParameter.setCustomParam("erfileId", pkID);
                    formShowParameter.setCustomParam("isLeave", dimission);
                    formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                    formShowParameter.setHasRight(true);
                    this.getView().showForm(formShowParameter);
                } catch (Exception e) {
                    this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "SkipToThisMonthInReportFormPlugin_1", "tdkw-esc-hrobs-formplugin-report"));
                }
            } else {
                this.getView().showErrorNotification(ResManager.loadKDString("该员工已调离本组织，您无权查看当前人员的详细信息！", "SkipToThisMonthInReportFormPlugin_2", "tdkw-esc-hrobs-formplugin-report"));
            }
        }
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