package tdkw.esc.leaderquery.integratedquery.report.newretireleave;

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
        if (StringUtils.equals("tdkw_rptname", hyperLinkClickEvent.getFieldName())) {
            //点击获取人事业务档案的主键(隐藏列)
            DynamicObject rowData = hyperLinkClickEvent.getRowData();
            //参数为查询配置的查询字段的标识
            String pkId = (String) rowData.get("personid");
            //查询人员工号，判断是否在入职
            String personNumber = rowData.getString("tdkw_number");
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
            DynamicObjectCollection objectCollection = null;
            //在入职人员获取最新任职经历中对应部门
            Long personAmdinorg = null;
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
//            erManFileQfilter.and("empposrel.tdkw_changereason.number", QCP.not_equals, "XY00017");
            objectCollection = QueryServiceHelper.query("hspm_ermanfile", "empposrel.company.id as company,empposrel.adminorg.id as adminorg", new QFilter[]{erManFileQfilter});
            //获取当前人员权限
            AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_departandretire_pc");
            if (!result.isHasAllOrgPerm()) {
                List<Long> hasPerOrg = result.getHasPermOrgs();
                Set<Long> allOrg = new HashSet<>();
                if (hasPerOrg.size() > 0) {
                    if (ObjectUtils.isNotEmpty(objectCollection)) {
                        for (DynamicObject obj : objectCollection) {
                            if (ObjectUtils.isNotEmpty(obj.getLong("company"))) {
                                allOrg.add(obj.getLong("company"));
                            }
                            if (ObjectUtils.isNotEmpty(obj.getLong("adminorg"))) {
                                allOrg.add(obj.getLong("adminorg"));
                            }
                        }
                        logger.info("所有组织数据" + allOrg);
                        for (Long aLong : allOrg) {
                            if (hasPerOrg.contains(aLong)) {
                                isOrgPerm = Boolean.TRUE;
                            }
                        }
                    }
                } else {
                    if (hasPerOrg.contains(personAmdinorg)) {
                        isOrgPerm = Boolean.TRUE;
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
//        String fieldName = hyperLinkClickEvent.getFieldName();
//        //点击获取人事业务档案的主键(隐藏列)
//        DynamicObject rowData = hyperLinkClickEvent.getRowData();
//        //参数为查询配置的查询字段的标识
//        String pkId = (String) rowData.get("personid");
//        logger.info("跳转的人员pkid为" + pkId);
//        try {
//            FormShowParameter formShowParameter = new FormShowParameter();
//            formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
//            formShowParameter.setCustomParam("erfileId", pkId);
//            formShowParameter.setCustomParam("isLeave", Boolean.TRUE);
//            formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
//            formShowParameter.setHasRight(true);
//            this.getView().showForm(formShowParameter);
//        } catch (Exception e) {
//            this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "RightTableClickFormPlugin", "tdkw-esc-tdkw_integrated_query-report-ext", new Object[0]));
//        }
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
