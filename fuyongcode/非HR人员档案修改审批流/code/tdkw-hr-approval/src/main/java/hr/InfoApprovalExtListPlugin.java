package hr;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.events.PackageDataEvent;
import kd.bos.entity.list.column.AbstractColumnDesc;
import kd.bos.form.IFormView;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.exception.WFEngineException;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.sdk.hr.hspm.common.utils.PageCacheUtils;

import java.util.List;

/**
 * @Description：人员档案信息变更申请 列表插件
 */
public class InfoApprovalExtListPlugin extends AbstractListPlugin {

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        FormOperate formOperate = (FormOperate) afterDoOperationEventArgs.getSource();
        String operateKey = formOperate.getOperateKey();
        if("kdcd_termproces".equals(operateKey)){
            List<Object> idList = afterDoOperationEventArgs.getOperationResult().getSuccessPkIds();
            for (Object id : idList) {
                try {
                    WorkflowServiceHelper.abandonByBusienssKey(String.valueOf(id));
                } catch (Exception e) {
//                    Object[] objects = ((WFEngineException) e).getArgs();
                    QFilter infoApprovalFilter = new QFilter("id", QCP.equals, id);
                    DynamicObject infoApproval = QueryServiceHelper.queryOne("hspm_infoapproval", "billno", infoApprovalFilter.toArray());
//                    this.getView().showTipNotification("单据编号：" + infoApproval.getString("billno") + objects[0].toString());
                    this.getView().showTipNotification("单据编号：" + infoApproval.getString("billno") + " " + e);
                    break;
                }
            }
        }

        // 删除
        if ("delete".equals(operateKey)) {
            IFormView view = PageCacheUtils.getHomePage(this.getView());
            // 清除缓存
            view.getPageCache().put("submitVerson", null);
        }
    }

    @Override
    public void packageData(PackageDataEvent e) {
        super.packageData(e);
        AbstractColumnDesc columnDesc = (AbstractColumnDesc)e.getSource();
        String fieldKey = columnDesc.getFieldKey();
        if ("person.name".equals(fieldKey)) {
            String pName = e.getRowData().getString("person.name");
            if (HRStringUtils.isEmpty(pName)) {
                pName = "";
            }
            pName.replaceAll("的人员档案信息变更申请","");
            e.setFormatValue(pName);
        }

    }
}
