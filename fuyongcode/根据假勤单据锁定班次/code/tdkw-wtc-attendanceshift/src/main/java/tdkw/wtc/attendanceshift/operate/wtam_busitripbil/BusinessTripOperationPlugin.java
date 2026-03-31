package tdkw.wtc.attendanceshift.operate.wtam_busitripbil;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import tdkw.wtc.attendanceshift.operate.wtam_busitripbil.integration.utils.RosterLockUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @description 出差申请操作插件
 * @author ljl
 * @date 2024/4/16 13:27
 **/
public class BusinessTripOperationPlugin extends AbstractOperationServicePlugIn implements Plugin {

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("parent");
        e.getFieldKeys().add("entryentity");
        e.getFieldKeys().add("triptime");
        e.getFieldKeys().add("startdate");
        e.getFieldKeys().add("enddate");
        e.getFieldKeys().add("personid");
        e.getFieldKeys().add("attfile");
    }
    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        super.beginOperationTransaction(e);
        String operationKey = e.getOperationKey();
        DynamicObject[] dataEntities = e.getDataEntities();
        RosterLockUtil.lockOrUnLock(dataEntities,operationKey);
    }

}