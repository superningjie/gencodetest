package tdkw.esc.leaderquery.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RightTableClickFormPlugin
 *
 * @author xxx
 * @date 2023/6/17
 */
public class RightTableClickFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {
    private static final Log logger = LogFactory.getLog(RightTableClickFormPlugin.class);

    /**
     * @param hyperLinkClickEvent 点击列的参数 有该行全部字段的数据
     */
    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        String formId = this.getView().getFormShowParameter().getFormId();
        String orgString = "";
        switch (formId){
            case "tdkw_middle_leader_report":
                orgString = "tdkw_middlelevelcadres_pc";
                break;
            case "tdkw_allleaders_report":
                orgString = "tdkw_allexecutive_pc";
                break;
            case "tdkw_management_report":
                orgString = "tdkw_frgsggrz_pc";
                break;
            case "tdkw_financeman_report":
                orgString = "tdkw_financeperson_pc";
                break;
            case "tdkw_party_com_report":
                orgString = "tdkw_partycommittee_pc";
                break;
            case "tdkw_trade_union_report":
                orgString = "tdkw_tradeunion_pc";
                break;
            case "tdkw_groupcom_report":
                orgString = "tdkw_leaguecommittee_pc";
                break;
            case "tdkw_discipline_report":
                orgString = "tdkw_disciplineinspec_pc";
                break;
            default:
                break;
        }

        //点击获取人事业务档案的主键(隐藏列)
        DynamicObject rowData = hyperLinkClickEvent.getRowData();
        //参数为查询配置的查询字段的标识
        String personNumber = (String) rowData.get("tdkw_number");
        QFilter qFilter = new QFilter("iscurrentversion",QCP.equals,"1").and("datastatus",QCP.equals,"1").and("number", QCP.equals, personNumber);;
        DynamicObject person = QueryServiceHelper.queryOne("hrpi_person", "number,tdkw_pkid,id", new QFilter[]{qFilter});
        if (personNumber.length() != 6) {
            personNumber = personNumber.substring(0, 6);
        }
        logger.info("跳转的人员工号为" + personNumber);
        //判断权限
        Boolean jurisdiction = Boolean.FALSE;
        //判断离职
        Boolean dimission = Boolean.FALSE;
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), orgString);
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        QFilter erManFileQfilter = new QFilter("person.number", QCP.equals, personNumber);
        erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
        erManFileQfilter.and("datastatus", QCP.equals, "1");
        erManFileQfilter.and("businessstatus", QCP.equals, "1");
        erManFileQfilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");
        DynamicObjectCollection ermanfile = QueryServiceHelper.query("hspm_ermanfile", "empposrel.company.id as company,empposrel.adminorg.id as adminorg,empentrel.laborrelstatus.number as number,empposrel.startdate", new QFilter[]{erManFileQfilter});
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
        }else{
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
                formShowParameter.setCustomParam("erfileId", person.getString("tdkw_pkid"));
                formShowParameter.setCustomParam("isLeave", dimission);
                formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                formShowParameter.setHasRight(true);
                this.getView().showForm(formShowParameter);
            } catch (Exception e) {
                this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "RightTableClickFormPlugin", "tdkw-esc-tdkw_integrated_query-report-ext", new Object[0]));
            }
        } else {
            this.getView().showErrorNotification("该员工已调离本组织，您无权查看当前人员的详细信息！");
        }
        logger.info("是否有权限查看" + jurisdiction);
        logger.info("是否离职" + dimission);
    }

    /**
     * 监听右表-报表列表控件
     *
     * @param e
     */
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList rightList = this.getView().getControl("reportlistap");
        rightList.addHyperClickListener(this);
    }
}
