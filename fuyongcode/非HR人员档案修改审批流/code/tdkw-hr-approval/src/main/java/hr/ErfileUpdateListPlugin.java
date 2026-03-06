package hr;

import kd.bos.bill.OperationStatus;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.Tuple;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.list.IListView;
import kd.bos.list.ListShowParameter;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.sdk.hr.hspm.business.repository.ErmanFileRepository;
import kd.sdk.hr.hspm.business.service.AttacheHandlerService;
import kd.sdk.hr.hspm.common.enums.ClientTypeEnum;

import java.util.*;

/**
 * 人员档案列表界面档案修改插件
 */
public class ErfileUpdateListPlugin  extends AbstractListPlugin {

    private final static Log logger = LogFactory.getLog(ErfileUpdateListPlugin.class);


    @Override
    public void afterDoOperation(AfterDoOperationEventArgs arg) {
        if (arg.getOperationResult() == null) {
            return;
        }
        String operateKey = arg.getOperateKey();
        if ("filesupdate".equals(operateKey)) {
            IListView listView = (IListView) this.getView();
            ListSelectedRowCollection listSelectedRows = listView.getSelectedRows();
            if (listSelectedRows.size() > 1) {
                this.getView().showTipNotification("只能选择一条数据进行修改");
                return;
            }
            if (listSelectedRows.size() == 0) {
                this.getView().showTipNotification("请勾选数据进行修改");
                return;
            }
            long erFileId = (Long) listSelectedRows.get(0).getPrimaryKeyValue();
            String billFormId = ((ListShowParameter) this.getView().getFormShowParameter()).getBillFormId();
            Map<String, Object> retMap = (Map) HRMServiceHelper.invokeHRService("hspm", "IHSPMService", "jumpErManFileDetail", new Object[]{erFileId, billFormId});
            FormShowParameter formShowParameter = (FormShowParameter) retMap.get("data");
            long personId = (Long) formShowParameter.getCustomParams().get("person");
            DynamicObject erFileDy = ErmanFileRepository.getPrimaryErmanFile(personId);
            Tuple tuple = AttacheHandlerService.getInstance().handleRuleEngine(this.getView(), erFileDy.getLong("id"), erFileDy, "hspm" + ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), (Map) null, false);
            Long configId = Long.parseLong(tuple.item2.toString());
            IAppCache appCache = AppCache.get("hspm");
            Long currUserId = RequestContext.get().getCurrUserId();
            appCache.put(currUserId + "hspm_myermanfile" + "personId", personId);
            appCache.put(currUserId + "hspm_myermanfile" + "configId", configId);
            FormShowParameter showParameter = new FormShowParameter();
            showParameter.setFormId("hspm_myermanfile");
            showParameter.setCaption(erFileDy.get("person.name") + "的档案");
            showParameter.setCustomParam("personId", erFileDy.get("person.id"));
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            showParameter.setCustomParam("configId", configId);
            showParameter.setStatus(OperationStatus.EDIT);
            this.getView().showForm(showParameter);
        }
    }
}
