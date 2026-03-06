package tdkw.hrmp.hrobs.formplugin.report;

import com.google.common.collect.Maps;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.form.ClientProperties;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ShowType;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.mvc.report.ReportView;
import kd.bos.report.IReportView;
import kd.bos.report.ReportList;
import kd.bos.report.events.SearchEvent;
import kd.bos.report.filter.ReportFilter;
import kd.bos.report.filter.SearchListener;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.formplugin.util.DynamicObjectUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xxx
 * @Date 2023/6/14 16:41
 * @Description 我的申请 报表表单插件
 * @Demander xxx
 * @Document PC端我的申请需规说明_V2.0_0614
 * @Basedata tdkw_application
 * @Version 1.0
 **/
public class ApplicationFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener, SearchListener {
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        ReportView source = (ReportView) e.getSource();
        String applicationType = source.getFormShowParameter().getCustomParam("applicationType");
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtils.isNotEmpty(applicationType)) {
            params.put("applicationType", applicationType);
            source.getQueryParam().setCustomParam(params);
            source.refresh();
            ReportFilter rf = this.getView().getControl("reportfilterap");
            rf.setCollapse(false);
        }
        if (StringUtils.equals("1", applicationType)) {
            // 在办申请
            Map<String, Object> fieldProp = new HashMap<>(1);
            fieldProp.put(ClientProperties.Caption, new LocaleString("创建时间"));
            getView().updateControlMetadata("tdkw_filter_submitdate", fieldProp);
            ReportList entryGrid = this.getControl("reportlistap");
            entryGrid.setColumnProperty("tdkw_submitdate", ClientProperties.Header, new LocaleString("创建时间"));
        }
    }

    
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.addSearchListener(this);
    }

    @Override
    public void search(SearchEvent searchEvent) {
        ReportFilter rf = this.getView().getControl("reportfilterap");
        this.getView().updateView();
        rf.setCollapse(false);
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        ReportFilter rf = this.getView().getControl("reportfilterap");
        this.getView().updateView();
        rf.setCollapse(false);
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent evt) {
        String billNo = evt.getRowData().getString("tdkw_billno_hide");
        String billType = evt.getRowData().getString("tdkw_billtype");
        switch (billType) {
            case "1":
                // 离职单
                //读取MC参数  获取离职和代离职的单据编码维护信息
                String quitParam = System.getProperty("eas.resign.isself.param");
                //没有维护按照 代离职申请 LVE-20230905-00023
                // 自己申请离职 ELV-20230904-00001
                if (StringUtils.isNotBlank(billNo) && StringUtils.isBlank(quitParam)) {
                    //代离职
                    if (billNo.startsWith("LVE")) {
                        this.showBillForm("htm_quitapply", billNo, "");
                    } else if (billNo.startsWith("QVE")) {
                        //快速离职
                        this.showBillForm("htm_quitapplyfast", billNo, "");
                    } else {
                        //员工离职
                        this.showBillForm("htm_quitapplyemp", billNo, "");
                    }
                }
                break;
            case "2":
                // 请假单
                //读取MC参数  获取休假和代他人休假的单据编码维护信息
                String vaApplySelf = System.getProperty("xiujia0905test");
                // LE-     本人休假
                // LEH-    代他人休假
                if (StringUtils.isNotBlank(billNo) && StringUtils.isBlank(vaApplySelf)) {
                    if (billNo.startsWith("LEH-")) {
                        // 代他人休假
                        this.showBillForm("wtabm_vaapply", billNo);
                    } else {
                        //休假
                        this.showBillForm("wtabm_vaapplyself", billNo);
                    }
                }
                // this.showBillForm("wtabm_vaapplyself", billNo);
                break;
            case "3":
                // 销假单
                //读取MC参数  获取休假和代他人休假的单据编码维护信息
                String vaUpdateSelf = System.getProperty("xiaojia0905test");
                // BG-     本人销假
                // BGH-    代他人销假
                if (StringUtils.isNotBlank(billNo) && StringUtils.isBlank(vaUpdateSelf)) {
                    if (billNo.startsWith("BGH")) {
                        // 代他人销假
                        this.showBillForm("wtabm_vaupdate", billNo);
                    } else {
                        //销假
                        this.showBillForm("wtabm_vaupdateself", billNo);
                    }
                }
                // this.showBillForm("wtabm_vaupdateself", billNo);
                break;
            case "4":
                // 出差单
                this.showBillForm("tdkw_reqtripe", billNo);
                break;
            case "5":
                // 销差单
                this.showBillForm("tdkw_destroytripe", billNo);
                break;
            case "6":
                // 补卡单
                this.showBillForm("wtpm_supsignself", billNo);
                break;
            case "7":
                // 加班单
                this.showBillForm("wtom_otbillself", billNo);
                break;
            case "8":
                // 证明单
                this.showBillForm("tdkw_prove_handle", billNo);
                break;
            case "9":
                // 个人信息修改单
                this.showBillForm("hspm_infoapproval", billNo);
                break;
            case "10":
                // 问询单
                this.showBillForm("tdkw_employee_inquiries", billNo);
                break;
            case "11":
                // 招聘需求单
                this.showBillForm("tdkw_rec_apply_bill", billNo);
                break;
            case "12":
                // 个人信息删除单
                this.showBillForm("tdkw_hspm_infochg", billNo);
                break;
            case "13":
                // 个因私出国申请单
                this.showBillForm("tdkw_wsgl_form_yscgsq", billNo);
                break;
            case "14":
                // 银行卡变更申请单
                this.showBillForm("hsas_perbceditbill", billNo);
                break;
            default:
                break;
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (StringUtils.equals("closeson", closedCallBackEvent.getActionId())) {
            IReportView view = this.getView();
            view.refresh();
        }
    }

    /**
     * @author xxx
     * @Description 点击单据编码跳转到对应单据
     * @Date 2023/6/15 9:59
     */
    public void showBillForm(String entityNumber, String billNo) {
        DynamicObject object = DynamicObjectUtils.findDynamicObjectByKey(entityNumber, "billno", billNo, null, null);
        if (ObjectUtils.isNotEmpty(object)) {
            BillShowParameter billShowParameter = new BillShowParameter();
            billShowParameter.setFormId(entityNumber);
            billShowParameter.setPkId(object.getPkValue());
            billShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            billShowParameter.setHasRight(true);
            billShowParameter.setCloseCallBack(new CloseCallBack(this, "closeson"));
            // billShowParameter.setStatus(OperationStatus.VIEW);
            //billShowParameter.setty
            this.getView().showForm(billShowParameter);
        }
    }

    /**
     * @author xxx
     * @Description 点击标题跳转到对应单据 转为跳转离职申请准备
     * @Date 2023/7/26 11:02
     */
    public void showBillForm(String entityNumber, String billNo, String tag) {
        DynamicObject object = DynamicObjectUtils.findDynamicObjectByKey(entityNumber, "billno", billNo, null, null);
        if (ObjectUtils.isNotEmpty(object)) {
            BillShowParameter billShowParameter = new BillShowParameter();
            billShowParameter.setFormId(entityNumber);
            billShowParameter.setPkId(object.getPkValue());
            billShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            billShowParameter.setHasRight(true);
            billShowParameter.setCloseCallBack(new CloseCallBack(this, "closeson"));
            // billShowParameter.setStatus(OperationStatus.VIEW);
            //billShowParameter.setty
            billShowParameter.setStatus(OperationStatus.VIEW);
            this.getView().showForm(billShowParameter);
        }
    }
}
