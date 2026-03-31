package tdkw.esc.leaderquery.integratedquery.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;

import java.util.EventObject;

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
        String fieldName = hyperLinkClickEvent.getFieldName();
        //点击获取人事业务档案的主键(隐藏列)
        DynamicObject rowData = hyperLinkClickEvent.getRowData();
        //参数为查询配置的查询字段的标识
        String pkId = (String) rowData.get("personid");
        logger.info("跳转的人员pkid为" + pkId);
        try {
            FormShowParameter formShowParameter = new FormShowParameter();
            formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
            formShowParameter.setCustomParam("erfileId", pkId);
            formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
            formShowParameter.setHasRight(true);
            this.getView().showForm(formShowParameter);
        } catch (Exception e) {
            this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "RightTableClickFormPlugin", "tdkw-esc-tdkw_integrated_query-report-ext", new Object[0]));
        }
        // kd.hr.htm.formplugin.quitfile.QuitFileInfoListPlugin;
        // kd.hr.hpfs.formplugin.file.ERManFileCommonListPlugin
        // try {
        //     //打开窗口
        //     Map<String, Object> retMap = (Map) HRMServiceHelper.invokeHRService("hspm", "IHSPMService", "jumpErManFileDetail", new Object[]{Long.valueOf(fileFid), "hspm_erfilelist"});
        //     if ((Boolean) retMap.get("success")) {
        //         FormShowParameter formShowParameter = (FormShowParameter) retMap.get("data");
        //         this.getView().showForm(formShowParameter);
        //     } else {
        //         this.getView().showErrorNotification((String) retMap.get("errormsg"));
        //
        //     }
        // } catch (Exception var9) {
        //     logger.info("人事业务档案boid" + fileFid);
        //  this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "PersonFileIntegrityReprotFormPlugin_0", "tdkw.hrmp.hrobs.formplugin.report", new Object[0]));
        // }
        // getView().showForm(showParameter);
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
