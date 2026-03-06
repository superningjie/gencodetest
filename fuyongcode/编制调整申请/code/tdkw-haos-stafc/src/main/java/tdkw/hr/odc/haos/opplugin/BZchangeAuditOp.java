package tdkw.hr.odc.haos.opplugin;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.form.IFormView;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.cd.common.form.handler.AutoCloseViewHandler;
import tdkw.hr.odc.haos.common.BzSelect;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName : BZchangeOp
 * @Description : 编制审核通过后
 * @Author : XYP
 * @Date: 2024-07-26 10:14
 */
public class BZchangeAuditOp extends AbstractOperationServicePlugIn {
    private static final Log LOGGER = LogFactory.getLog(BZchangeAuditOp.class);

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        List<String> fieldKeys = e.getFieldKeys();
        String name = this.billEntityType.getName();
        Map<String, IDataEntityProperty> fieldMap = EntityMetadataCache.getDataEntityType(name).getAllFields();
        for (String key : fieldMap.keySet()) {
            fieldKeys.add(key);
        }
    }

    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        super.afterExecuteOperationTransaction(e);
        DynamicObject[] dataEntities1 = e.getDataEntities();
        for (DynamicObject dynamicObject : dataEntities1) {
            this.savestaff(dynamicObject);
        }
    }

    void savestaff(DynamicObject dynamicObject) {
        Long tdkw_staff = dynamicObject.getDynamicObject("tdkw_staff").getLong("id");
        try (AutoCloseViewHandler viewHandler = AutoCloseViewHandler.of("haos_staff")) {
            IFormView childView = (IFormView) viewHandler.openModifyView(tdkw_staff);
            //获取本页面的数据并存储为map
            DynamicObjectCollection tdkw_bentryentity = dynamicObject.getDynamicObjectCollection("bentryentity");
            //分录id --- 树形单据体内容使用组织
            Map<Long, DynamicObject> bmap = new HashMap<>();
            //分录id --- 子单据体内容岗位
            Map<Long, DynamicObject> cmap = new HashMap<>();
            //分录id --- 子单据体内容职位
            Map<Long, DynamicObject> dmap = new HashMap<>();
            //分录id --- 子单据体内容用工
            Map<Long, DynamicObject> emap = new HashMap<>();
            //分录id --- 子单据体内容职级
            Map<Long, DynamicObject> fmap = new HashMap<>();
            for (DynamicObject tdkwbent : tdkw_bentryentity) {
                bmap.put(tdkwbent.getLong("tdkw_bid"), tdkwbent);
                DynamicObjectCollection tdkw_centryentity = tdkwbent.getDynamicObjectCollection("centryentity");
                for (DynamicObject csub : tdkw_centryentity) {
                    cmap.put(csub.getLong("tdkw_cid"), csub);
                }
                DynamicObjectCollection tdkw_dentryentity = tdkwbent.getDynamicObjectCollection("dentryentity");
                for (DynamicObject dsub : tdkw_dentryentity) {
                    dmap.put(dsub.getLong("tdkw_did"), dsub);
                }
                DynamicObjectCollection tdkw_eentryentity = tdkwbent.getDynamicObjectCollection("eentryentity");
                for (DynamicObject esub : tdkw_eentryentity) {
                    emap.put(esub.getLong("tdkw_eid"), esub);
                }
                /*DynamicObjectCollection tdkw_fentryentity = tdkwbent.getDynamicObjectCollection("fentryentity");
                for (DynamicObject fsub : tdkw_fentryentity) {
                    fmap.put(fsub.getLong("tdkw_fid"), fsub);
                }*/
            }

            //获取编制信息的内容 -- 回写使用组织
            DynamicObjectCollection bentryentitys = childView.getModel().getEntryEntity("bentryentity");
            String isv = "tdkw_";
            for (DynamicObject bent : bentryentitys) {
                Long id = bent.getLong("id");
                if (bmap.containsKey(id)) {
                    //取出 变更页面 的数据
                    DynamicObject tdkwbentdy = bmap.get(id);
                    List<String> bop = BzSelect.bentryentityOp();
                    for (String bs : bop) {//标识
                        if (tdkwbentdy.get(isv + bs) != null && !tdkwbentdy.get(isv + bs).equals("") ) {
                            bent.set(bs, tdkwbentdy.get(isv + bs));
                        }
                    }
                    //标识太长单独处理
                    if (tdkwbentdy.get("tdkw_bhxj2") != null) {
                        bent.set("byearstaffnumwithsub", tdkwbentdy.get("tdkw_bhxj2"));
                    }
                    if (tdkwbentdy.get("tdkw_byearstaffnu") != null) {
                        bent.set("byearstaffnumwithsubnum", tdkwbentdy.get("tdkw_byearstaffnu"));

                    }
                    //子单据体处理 -- 岗位
                    DynamicObjectCollection ccontrolstrategy = bent.getDynamicObjectCollection("centryentity");
                    for (DynamicObject csub : ccontrolstrategy) {
                        if (cmap.containsKey(csub.getLong("id"))) {
                            DynamicObject cyc = cmap.get(csub.getLong("id"));
                            List<String> cop = BzSelect.centryentityOp();
                            for (String cs : cop) {
                                if (cyc.get(isv + cs) != null && !cyc.get(isv + cs).equals("")) {
                                    csub.set(cs, cyc.get(isv + cs));
                                }
                            }
                        }
                    }
                    //子单据体处理 -- 职位
                    DynamicObjectCollection dcontrolstrategy = bent.getDynamicObjectCollection("dentryentity");
                    for (DynamicObject dsub : dcontrolstrategy) {
                        if (dmap.containsKey(dsub.getLong("id"))) {
                            DynamicObject dyc = dmap.get(dsub.getLong("id"));
                            List<String> dop = BzSelect.dentryentityOp();
                            for (String ds : dop) {
                                if (dyc.get(isv + ds) != null && !dyc.get(isv + ds).equals("")) {
                                    dsub.set(ds, dyc.get(isv + ds));
                                }
                            }
                        }
                    }
                    //子单据体处理 -- 用工
                    DynamicObjectCollection econtrolstrategy = bent.getDynamicObjectCollection("eentryentity");
                    for (DynamicObject esub : econtrolstrategy) {
                        if (emap.containsKey(esub.getLong("id"))) {
                            DynamicObject eyc = emap.get(esub.getLong("id"));
                            List<String> eop = BzSelect.eentryentityOp();
                            for (String es : eop) {
                                if (eyc.get(isv + es) != null && !eyc.get(isv + es).equals("") ) {
                                    esub.set(es, eyc.get(isv + es));
                                }
                            }
                        }
                    }
                    //子单据体处理 -- 职级
                    /*DynamicObjectCollection fcontrolstrategy = bent.getDynamicObjectCollection("fentryentity");
                    for (DynamicObject fsub : fcontrolstrategy) {
                        if (fmap.containsKey(fsub.getLong("id"))) {
                            DynamicObject fyc = fmap.get(fsub.getLong("id"));
                            List<String> fop = BzSelect.fentryentityOp();
                            for (String es : fop) {
                                if (fyc.get(isv + es) != null && !fyc.get(isv + es).equals("")) {
                                    fsub.set(es, fyc.get(isv + es));
                                }
                            }
                        }
                    }*/
                }
            }
            OperateOption option = OperateOption.create();
            option.setVariableValue(OperateOptionConst.ISHASRIGHT, "true");// 跳过数据权限校验
            OperationResult save = childView.invokeOperation("save", option);
            if (!save.isSuccess()) {
                LOGGER.error("反写失败:" + save.getAllErrorOrValidateInfo().get(0).getMessage());
            }else {
                OperationResult enable = childView.invokeOperation("enable",option);
                if (!enable.isSuccess()){
                    LOGGER.error("启用失败:" + enable.getAllErrorOrValidateInfo().get(0).getMessage());
                }
            }
        } catch (Exception e) {
            // 异常处理
            LOGGER.error("反写失败:" + ExceptionUtils.getStackTrace(e));
        }
    }
}
