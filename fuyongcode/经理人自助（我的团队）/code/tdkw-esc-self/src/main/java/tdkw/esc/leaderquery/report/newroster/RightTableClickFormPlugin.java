package tdkw.esc.leaderquery.report.newroster;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
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
import kd.hr.hdm.common.util.HRServiceUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;
import tdkw.esc.leaderquery.common.hrmp.HRStructOrgUtils;

import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xxx
 * @version 1.0
 * @description: TODO
 * @date 2024/1/16 0016 上午 11:51
 */

public class RightTableClickFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {
    private static final Log logger = LogFactory.getLog(RightTableClickFormPlugin.class);

    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        super.beforeQuery(queryParam);
        List<FilterItemInfo> filterItems = queryParam.getFilter().getFilterItems();
        if(filterItems.size() <= 0) {
            Object tdkw_deadtline =  this.getModel().getValue("tdkw_deadtline");
            Object tdkw_isprimary =  this.getModel().getValue("tdkw_isprimary");
            queryParam.getFilter().addFilterItem("tdkw_deadtline", tdkw_deadtline);
            queryParam.getFilter().addFilterItem("tdkw_isprimary", tdkw_isprimary);
        }
    }

    /**
     * @param hyperLinkClickEvent 点击列的参数 有该行全部字段的数据
     */
    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        //点击获取人事业务档案的主键(隐藏列)
        DynamicObject rowData = hyperLinkClickEvent.getRowData();
        //参数为查询配置的查询字段的标识  跳转参数
//        String pkID = rowData.getString("personid");
        String pkID = rowData.getString("tdkw_personid");
//        DynamicObject dyn = HRServiceUtil.getPrimaryErmanFile(Long.parseLong(personId));
//        String pkID = dyn.getString("id");
                //人员工号 查询组织用
        String personNumber = rowData.getString("tdkw_personrpt.number");
        logger.info("跳转的人员工号为" + personNumber);
        logger.info("跳转的人员pkid为" + pkID);
        //判断是否再入职
        /*if (personNumber.length() > 6) {
            personNumber = personNumber.substring(0, 6);
        }*/
        //判断权限
        Boolean aTrue = Boolean.FALSE;
        //判断离职
        Boolean bTrue = Boolean.FALSE;
        //报表标识名
        String name = this.getModel().getDataEntityType().getName();
        AuthorizedOrgResult result = null;
        //事业部花名册
        if( StringUtils.equals(name,"tdkw_busiunitrosterrpt") ){
            result = HRStructOrgUtils.getStructUserAdminOrgs(UserServiceHelper.getCurrentUserId(), "tdkw_divisionroster_pc");
        }else if( StringUtils.equals(name,"tdkw_roster_report") ){//在职人员花名册
            result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_roster_pc");
        }
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        QFilter erManFileQfilter = new QFilter("person.number", QCP.equals, personNumber);
        erManFileQfilter.and("initstatus", QCP.equals, "2");
        erManFileQfilter.and("iscurrentversion", QCP.equals, "1");
        erManFileQfilter.and("datastatus", QCP.not_equals, "-1");
        erManFileQfilter.and("businessstatus", QCP.equals, "1");
//        erManFileQfilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");
        DynamicObjectCollection objectCollection = QueryServiceHelper.query("hspm_ermanfile", "empposrel.company.id as company,empposrel.adminorg.id as adminorg,empentrel.laborrelstatus.number as number,empposrel.startdate", new QFilter[]{erManFileQfilter},"empposrel.startdate");
        //判断是否能穿透
        if (!hasAllOrgPerm) {
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
                            aTrue = Boolean.TRUE;
                        }
                    }
                }
            }
        }else{
            aTrue = Boolean.TRUE;
        }

        if (aTrue) {
            try {
                for (DynamicObject obj : objectCollection) {
                    String number = obj.getString("number");
                    if(StringUtils.isNotEmpty(number)) {
                        if (StringUtils.equals(number, "XY00008") || StringUtils.equals(number, "XY00009") || StringUtils.equals(number, "XY000121") || StringUtils.equals(number, "XY00013")) {
                            bTrue = Boolean.TRUE;
                        }
                    }
                }
                FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                formShowParameter.setCustomParam("erfileId", pkID);
                formShowParameter.setCustomParam("isLeave", bTrue);
                formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                formShowParameter.setHasRight(true);
                this.getView().showForm(formShowParameter);
            } catch (Exception e) {
                this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "RightTableClickFormPlugin", "tdkw-esc-tdkw_integrated_query-report-ext", new Object[0]));
            }
        } else {
            this.getView().showErrorNotification("该员工已调离本组织，您无权查看当前人员的详细信息！");
        }
        logger.info("是否有权限查看"+ aTrue);
        logger.info("是否离职"+ bTrue);
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
