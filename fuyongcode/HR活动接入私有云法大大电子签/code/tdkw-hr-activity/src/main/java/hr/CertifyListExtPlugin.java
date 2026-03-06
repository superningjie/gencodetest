package hr;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeCreateListDataProviderArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.form.operate.FormOperate;
import kd.bos.mvc.list.ListDataProvider;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.formplugin.web.HRDataBaseList;
import kd.hr.htm.business.domain.service.certify.IQuitCertifyService;
import kd.hr.htm.common.enums.YesNo;

import java.util.Arrays;

/**
 * @Description 开具离职证明列表插件
 * 参考标品 kd.hr.htm.formplugin.certify.CertifyListPlugin,重写afterDoOperation方法
 * @Version 1.0.0
 * @Date 2024/9/6 15:49
 * @Created by sxf
 */
public class CertifyListExtPlugin extends HRDataBaseList {

    // 离职证明实现服务，重写标品IQuitCertifyService
    private final IQuitCertifyService quitCertifyService = new QuitCertifyServiceExtImpl();


    public void beforeCreateListDataProvider(BeforeCreateListDataProviderArgs args) {
        args.setListDataProvider(new CertifyListExtPlugin.MyListDataProvider());
    }

    public void setFilter(SetFilterEvent setFilterEvent) {
        super.setFilter(setFilterEvent);
        // 自定义过滤条件
        setFilterEvent.getQFilters().add(new QFilter("iseffective", "=", YesNo.YES.getValue()));
        setFilterEvent.getQFilters().add(new QFilter("tdkw_transferbill.tdkw_certifystatus", "in", Arrays.asList("0,1".split(","))));
        HasPermOrgResult result = PermissionServiceHelper.getAllPermOrgs(RequestContext.get().getCurrUserId(), "htm", "tdkw_certifymange", "47150e89000000ac");
        if (result != null) {
            if (!result.hasAllOrgPerm()) {
                setFilterEvent.getQFilters().add(new QFilter("tdkw_transferbill.org", "in", result.getHasPermOrgs()));
            }

        }
    }



    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        if (args.getOperationResult() != null && args.getOperationResult().isSuccess()) {
            FormOperate operate = (FormOperate)args.getSource();
            ListSelectedRowCollection selectedRows = this.getSelectedRows();
            String operateKey = operate.getOperateKey();
            // 开具离职证明
            if ("opencertify".equals(operateKey)) {
                quitCertifyService.openCertifyValidate(args, selectedRows, this, false);
            }
            // 确认开具离职证明
            if ("confirmfinish".equals(operateKey)) {
                quitCertifyService.confirmFinishValidate(args, selectedRows, this);
            }

        }
    }

    static class MyListDataProvider extends ListDataProvider {
        MyListDataProvider() {
        }

        public DynamicObjectCollection getData(int arg0, int arg1) {
            DynamicObjectCollection rows = super.getData(arg0, arg1);
            return rows.isEmpty() ? rows : rows;
        }
    }
}
