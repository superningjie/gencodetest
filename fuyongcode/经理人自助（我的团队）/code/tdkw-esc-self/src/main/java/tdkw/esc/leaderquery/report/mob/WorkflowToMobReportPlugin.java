package tdkw.esc.leaderquery.report.mob;

import kd.bos.bill.MobileBillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.CloseCallBack;
import kd.bos.form.IFormView;
import kd.bos.form.MobileFormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.BeforeCreateListDataProviderArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.list.BillList;
import kd.bos.list.MobileListShowParameter;
import kd.bos.list.events.ListRowClickEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.hrmp.hrobs.formplugin.messagecenter.mobile.WorkflowCommonListPlugin;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.report.mob.WorkflowToMobReportDataProvider;

/**
 * WorkflowToApplyListPlugin
 *
 * @author xxx
 * @date 2023/6/20
 */
public class WorkflowToMobReportPlugin extends WorkflowCommonListPlugin {
    private static final Log logger = LogFactory.getLog(WorkflowToMobReportPlugin.class);

    @Override
    public void listRowClick(ListRowClickEvent evt) {
        //创建弹出移动单据界面对象，MobileBillShowParameter 表示弹出页面为移动单据界面
        MobileBillShowParameter mobBillShowParameter = new MobileBillShowParameter();
        String appID = null;
        evt.setCancel(true);
        // super.listRowClick(evt);
        //ListSelectedRow currentListSelectedRow = evt.getCurrentListSelectedRow();
        String idAndType = (String) evt.getCurrentListSelectedRow().getPrimaryKeyValue();
        BillList billList = this.getView().getControl("billlistap");
        if (StringUtils.isBlank(idAndType)) {
            this.getView().showTipNotification(ResManager.loadKDString("数据可能已变更，正在刷新，请稍等", "WorkflowCommonListPlugin_0", "hrmp-hrobs-formplugin", new Object[0]));
            billList.refreshData();
        } else {
            //获取选中行的索引
            int rowKey = billList.getSelectedRows().getRowKeys()[0];
            String[] split = idAndType.split(":");
            String id = split[0];
            String billType = split[1];
            String billStatus = split[2];
            String billNo = split[3];
            switch (billType) {
                //离职
                case "1":
                    //读取MC参数  获取离职和代离职的单据编码维护信息
                    String quitParam = System.getProperty("eas.resign.isself.param");
                    //没有维护按照 代离职申请 LVE-20230905-00023
                    // 自己申请离职 ELV-20230904-00001
                    if (StringUtils.isNotBlank(billNo) && StringUtils.isBlank(quitParam)) {
                        //代离职
                        if (billNo.startsWith("LVE") || billNo.startsWith("QVE")) {
                            billType = "htm_quitapply_mob";
                        } else {
                            //自己离职
                            billType = "htm_quitapplyemp_mob";
                        }
                    }
                    appID = "1WTT6K3V3TD/";
                    break;
                //请假
                case "2":
                    //代他人休假
                    //读取MC参数  获取休假和代他人休假的单据编码维护信息
                    String vaApplySelf = System.getProperty("xiujia0905test");
                    // LE-     本人休假
                    // LEH-    代他人休假
                    if (StringUtils.isNotBlank(billNo) && StringUtils.isBlank(vaApplySelf)) {
                        if (billNo.startsWith("LEH")) {
                            billType = "wtabm_vaapplyappro";
                        } else {
                            //休假
                            billType = "wtabm_vaapplyappro_self";
                        }
                    }
                    mobBillShowParameter.setCustomParam("fromPage", "hssc");
                    appID = "hssc";
                    break;
                //销假
                case "3":
                    //代他人销假
                    //读取MC参数  获取休假和代他人休假的单据编码维护信息
                    String vaUpdateSelf = System.getProperty("xiaojia0905test");
                    // BG-     本人销假
                    // BGH-    代他人销假
                    if (StringUtils.isNotBlank(billNo) && StringUtils.isBlank(vaUpdateSelf)) {
                        if (billNo.startsWith("BGH")) {
                            billType = "wtabm_vaupdate_change";
                        } else {
                            //销假
                            billType = "wtabm_vaupdateself_change";
                        }
                    }
                    mobBillShowParameter.setCustomParam("fromPage", "hssc");
                    appID = "hssc";
                    break;
                //出差
                case "4":
                    billType = "tdkw_reqtripe_mob";
                    appID = "3=R8Q=F7B2Q4";
                    break;
                //销差
                case "5":
                    billType = "tdkw_destroytripe_mob";
                    appID = "3=R8Q=F7B2Q4";
                    break;
                //补卡
                case "6":
                    billType = "wtpm_supsignself_m";
                    appID = "2ZK3NJ2OQYKX";
                    break;
                //加班
                case "7":
                    billType = "wtom_otbillselef_m";
                    appID = "hssc";
                    mobBillShowParameter.setCustomParam("fromPage", "detail");
                    break;
                //证明
                case "8":
                    billType = "tdkw_prove_handle_mob";
                    appID = "3=6N+AFWM3AV";
                    break;
                //个人信息修改
                case "9":
                    billType = "hspm_infoapproval_mob";
                    appID = "2ZK3NJ2OQYKX";
                    break;
                //问询
                case "10":
                    billType = "tdkw_employee_inquiries_mob";
                    appID = "3=6N+AFWM3AV";
                    break;
                //招聘需求
                case "11":
                    billType = "tdkw_rec_apply_bill_mob";
                    appID = "3B8G6BOBFSO9";
                    break;
                case "12":
                    billType = "tdkw_hspm_infochg_mob";
                    appID = null;
                    break;
                case "14":
                    billType = "hspp_bankcardbilldetail";
                    appID = null;
                    break;
            }

            // 打开银行卡信息
            if ("hspp_bankcardbilldetail".equals(billType)) {
                MobileFormShowParameter parameter = new MobileFormShowParameter();
                parameter.setFormId(billType);
                parameter.setCustomParam("id", Long.parseLong(id));
                parameter.getOpenStyle().setShowType(ShowType.Floating);
                if ("A".equals(billStatus)) {
                    parameter.setStatus(OperationStatus.EDIT);
                } else {
                    parameter.setStatus(OperationStatus.VIEW);
                }
                parameter.setHasRight(true);
                //设置子页面关闭回调对象，回调本插件，标识为XXX
                parameter.setCloseCallBack(new CloseCallBack(this, "closeson"));
                this.getView().showForm(parameter);
            } else {
                //设置FormId
                mobBillShowParameter.setFormId(billType);
                //设置打开哪张单据
                mobBillShowParameter.setPkId(id);
                //允许重入
                mobBillShowParameter.setEnableUserReentrant(true);
                //设置弹出页面的打开方式
                mobBillShowParameter.getOpenStyle().setShowType(ShowType.Floating);
                //如果是暂存或者提交的的话 以修改状态打开
                if ("A".equals(billStatus) /*|| "B".equals(billStatus)*/) {
                    mobBillShowParameter.setStatus(OperationStatus.EDIT);
                } else {
                    mobBillShowParameter.setStatus(OperationStatus.VIEW);
                }
                mobBillShowParameter.setHasRight(true);
                if (StringUtils.isNotBlank(appID)) {
                    mobBillShowParameter.setAppId(appID);
                    logger.info("ServiceAppId:" + appID);
                    logger.info("跳转的billType为:" + billType);
                }
                mobBillShowParameter.setCustomParam("checkRightAppId",appID);
                //设置子页面关闭回调对象，回调本插件，标识为XXX
                mobBillShowParameter.setCloseCallBack(new CloseCallBack(this, "closeson"));
                //弹出界面
                this.getView().showForm(mobBillShowParameter);
            }
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (StringUtils.equals("closeson", closedCallBackEvent.getActionId())) {
            IFormView view = this.getView();
            view.updateView();
        }
    }

    @Override
    public void beforeCreateListDataProvider(BeforeCreateListDataProviderArgs args) {
        // args.setListDataProvider(new WorkflowToMobReportDataProvider());
        String isFinish = this.getView().getFormShowParameter().getCustomParam("isFinish");
        if (StringUtils.isNotBlank(isFinish)) {
            WorkflowToMobReportDataProvider workflowToMobReportDataProvider = new WorkflowToMobReportDataProvider(isFinish);
            this.getView().getPageCache().put("isFinishCache", isFinish);
            args.setListDataProvider(workflowToMobReportDataProvider);
            // this.getPageCache().put("isFinishCache", workflowToMobReportDataProvider));
        } else {
            WorkflowToMobReportDataProvider workflowToMobReportDataProvider = new WorkflowToMobReportDataProvider();
            args.setListDataProvider(workflowToMobReportDataProvider);
        }
    }

    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);
        String isFinish = e.getFormShowParameter().getCustomParam("isFinish");
        if (StringUtils.equals("false", isFinish)) {
            // 在办申请
            MobileListShowParameter source = (MobileListShowParameter) e.getSource();
            source.setCaption("在办申请");
        } else {
            // 在办申请
            MobileListShowParameter source = (MobileListShowParameter) e.getSource();
            source.setCaption("已办申请");
        }
    }
}
