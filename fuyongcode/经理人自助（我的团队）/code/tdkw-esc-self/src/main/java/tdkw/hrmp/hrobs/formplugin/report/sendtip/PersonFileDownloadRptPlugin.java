package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.*;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Metadata： tdkw_personfileintegrity  人员档案下载
 * @Description ：参考：tdkw.hr.hspm.formplugin.FileConversionListPluginExt
 * @ClassName ：PersonFileDownloadListPlugin
 * @author xxx
 * @Date ：2023/8/16
 * @Version: 1.0
 */
public class PersonFileDownloadRptPlugin extends AbstractReportFormPlugin {

    private static final Log Logger = LogFactory.getLog(PersonFileDownloadRptPlugin.class);

    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate source = (FormOperate) args.getSource();
        String operateKey = source.getOperateKey();
        if (StringUtils.equals("wordconversion", operateKey)) {
            DynamicObjectCollection perProWordTempDy = queryPerProWord();
            if (ObjectUtils.isEmpty(perProWordTempDy)) {
                this.getView().showTipNotification("未找到对应模板！！！");
                args.setCancel(true);
            }
            List<Long> ermanFileIds = this.getErmanFileIds();
            if(ermanFileIds.size()==0){
                this.getView().showTipNotification("请先选择数据！");
                args.setCancel(true);
            }
        }

    }


    public void afterDoOperation(AfterDoOperationEventArgs e) {
        super.afterDoOperation(e);
        String operateKey = e.getOperateKey();
        if (StringUtils.equals(operateKey, "wordconversion")) {
            DynamicObjectCollection perProWordTempDy = queryPerProWord();
            List<Long> pkIds = this.getErmanFileIds();
            QFilter qFilter = new QFilter("id", "in", pkIds);
            DynamicObjectCollection ermanFileColl = QueryServiceHelper.query("hspm_ermanfile", "person.id,person.name", qFilter.toArray());
            FormShowParameter showParameter = new FormShowParameter();
            OpenStyle openStyle = showParameter.getOpenStyle();
            openStyle.setShowType(ShowType.Modal);
            showParameter.setFormId("tdkw_hspm_wordtempprin");
            showParameter.setCloseCallBack(new CloseCallBack(this, "wordconversion"));
            showParameter.setCustomParam("wordTemplate", perProWordTempDy);
            showParameter.setCustomParam("ermanFile", ermanFileColl);
            showParameter.setCaption("请选择简历下载模板");
            this.getView().showForm(showParameter);
        }

    }


    public void closedCallBack(ClosedCallBackEvent close) {
        super.closedCallBack(close);
        String actionId = close.getActionId();
        if (StringUtils.equals("wordconversion", actionId)) {
            Map<String, Object> returnData = (Map) close.getReturnData();
            if (!ObjectUtils.isEmpty(returnData)) {
                try {
                    this.getView().download((String) returnData.get("url"));
                    this.getView().showSuccessNotification("下载成功");
                    Logger.info("人员完整度简历，简历下载成功：ids" + getErmanFileIds());

                } catch (Exception e) {
                    Logger.error("人员完整度简历，简历下载失败，原因：" + e.getMessage());
                    this.getView().showErrorNotification("下载失败，请联系管理员！");
                }
            }
        }
    }

    private static DynamicObjectCollection queryPerProWord() {
        QFilter qFilter = new QFilter("status", "=", "C");
        qFilter.and("enable", "=", "1");
        return QueryServiceHelper.query("tdkw_hspm_wordconversion", "id,name,number", qFilter.toArray());
    }

    /**
     * 获取人事业务档案id
     *
     * @return
     */
    public List<Long> getErmanFileIds() {
        List<Long> pkIds = new ArrayList();
        ReportList reportList = this.getView().getControl("reportlistap");
        int[] selectedRows = reportList.getEntryState().getSelectedRows();
        for (int selectedRow : selectedRows) {
            String hrId = (String) reportList.getReportModel().getValue(selectedRow, "tdkw_hrid");
            pkIds.add(Long.parseLong(hrId));
        }

        return pkIds;
    }
}
