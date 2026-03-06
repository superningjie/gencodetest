package hr;

import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.IFormView;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.ShowType;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.util.HRDateTimeUtils;
import kd.hr.hbp.common.util.HRJSONUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseList;
import kd.hr.htm.business.application.IHtmToHrcsAppService;
import kd.hr.htm.business.domain.repository.CertifyRepository;
import kd.hr.htm.business.domain.repository.QuitApplyHelper;
import kd.hr.htm.business.domain.service.impl.certify.QuitCertifyServiceImpl;
import kd.hr.htm.business.domain.service.quit.IQuitStaffService;
import kd.hr.htm.common.enums.ActivityStatusEnum;
import kd.hr.htm.common.enums.PrintPriviewTypeEnum;
import kd.hr.htm.common.enums.QuitCertificationStatusEnum;
import kd.hr.htm.common.enums.YesNo;

import java.text.MessageFormat;
import java.util.*;

/**
 * @Description TODO
 * @Version 1.0.0
 * @Date 2024/9/6 16:25
 * @Created by sxf
 */
public class QuitCertifyServiceExtImpl extends QuitCertifyServiceImpl {
    /**
     * 二开离职证明
     */
    private static final HRBaseServiceHelper CETIFY_SERVICE_HELPER = new HRBaseServiceHelper("tdkw_certifymange");
    /**
     * 调动单据转基础资料
     */
    private static final HRBaseServiceHelper BASE_SERVICE_HELPER = new HRBaseServiceHelper("tdkw_transferbaseinfo");
    /**
     * 调动申请单
     */
    private static final HRBaseServiceHelper TRANSFER_SERVICE_HELPER = new HRBaseServiceHelper("hdm_transferapply");
    /**
     * 离职证明日志
     */
    private static final HRBaseServiceHelper CETIFYLOG_SERVICE_HELPER = new HRBaseServiceHelper("htm_certifylog");



    @Override
    public void openCertifyValidate(AfterDoOperationEventArgs args, ListSelectedRowCollection selectedRows, HRDataBaseList pluginList, boolean isQuithand) {
        IFormView view = pluginList.getView();
        Object pkId = selectedRows.get(0).getPrimaryKeyValue();
        if ("opencertify".equals(((FormOperate)args.getSource()).getOperateKey())) {
            if (selectedRows.size() != 1) {
                view.showMessage(ResManager.loadKDString("一次只能操作一条数据", "QuitCertifyServiceImpl_0", "hr-htm-business", new Object[0]));
                return;
            }

            this.dealCertifyDialog(args, pluginList, view, pkId, isQuithand);
        }
    }


    @Override
    public void confirmFinishValidate(AfterDoOperationEventArgs args, ListSelectedRowCollection selectedRows, HRDataBaseList pluginList) {
        IFormView view = pluginList.getView();
        Object pkId = selectedRows.get(0).getPrimaryKeyValue();
        if (selectedRows.size() != 1) {
            view.showMessage(ResManager.loadKDString("一次只能操作一条数据", "QuitCertifyServiceImpl_4", "hr-htm-business", new Object[0]));
        } else {
            DynamicObject certify = CETIFY_SERVICE_HELPER.queryOne("id,tdkw_transferbill.tdkw_certifystatus,tdkw_transferbill.tdkw_transferstatus,tdkw_transferbill.id,issuancetimes,finishdate,activity.number,activityins", Long.valueOf(pkId.toString()));
            if (certify == null) {
                view.showMessage(ResManager.loadKDString("数据异常", "QuitCertifyServiceImpl_3", "hr-htm-business", new Object[0]));
            } else {
                DynamicObject[] logs = CETIFYLOG_SERVICE_HELPER.query("id", new QFilter[]{new QFilter("certifyhandle", "=", Long.valueOf(pkId.toString()))}, "createtime desc");
                if (logs.length == 0) {
                    view.showMessage(ResManager.loadKDString("无开具离职证明记录，请先开具离职证明后再确认", "QuitCertifyServiceImpl_5", "hr-htm-business", new Object[0]));
                } else {
                    String openDetailFlag = certify.getString("tdkw_transferbill.tdkw_certifystatus");
                    if (QuitCertificationStatusEnum.FINISHED.getStatus().equals(openDetailFlag)) {
                        view.showMessage(ResManager.loadKDString("离职证明开具已完成，请勿再次确认", "QuitCertifyServiceImpl_6", "hr-htm-business", new Object[0]));
                    } else {
                        if (QuitCertificationStatusEnum.PENDING.getStatus().equals(openDetailFlag)) {
                            DynamicObject activityinsObj = certify.getDynamicObject("activityins");
                            Long currUserId = RequestContext.get().getCurrUserId();
                            boolean canUseTaskStatus = IHtmToHrcsAppService.getInstance().canSubmitStatus(activityinsObj.getString("taskstatus"));
                            if (canUseTaskStatus) {
                                OperationResult activityAddHandlerResult = IHtmToHrcsAppService.getInstance().activityAddHandler(activityinsObj, currUserId);
                                if (!activityAddHandlerResult.isSuccess()) {
                                    view.showOperationResult(activityAddHandlerResult);
                                    return;
                                }

                                OperationResult consentTaskResult = IHtmToHrcsAppService.getInstance().consentTask(activityinsObj.getLong("id"), currUserId, "");
                                if (!consentTaskResult.isSuccess()) {
                                    view.showOperationResult(consentTaskResult);
                                    return;
                                }
                            }

                            certify.set("issuancetimes", certify.getInt("issuancetimes") + 1);
                            certify.set("finishdate", new Date());
                            // 调动单离职证明开具状态更新
                            DynamicObject transferApply = BASE_SERVICE_HELPER.queryOne("tdkw_certifystatus", certify.getLong("tdkw_transferbill.id"));
                            transferApply.set("tdkw_certifystatus", QuitCertificationStatusEnum.FINISHED.getStatus());
                            TXHandle txHandle = TX.required();

                            try {
                                CETIFY_SERVICE_HELPER.updateDataOne(certify);
                                BASE_SERVICE_HELPER.update(new DynamicObject[]{transferApply});
                                // 离职活动消息  IQuitStaffService.getInstance().sendQuitActivityMsg(certify.getLong("quitapply.id"), certify.getString("activity.number"));
//                                IQuitStaffService.getInstance().sendQuitActivityMsg(certify.getLong("tdkw_transferbill.id"), certify.getString("tdkw_activity.number"));
                            } catch (Exception var18) {
                                txHandle.markRollback();
                            } finally {
                                txHandle.close();
                            }

                            view.showSuccessNotification(ResManager.loadKDString("操作成功", "QuitCertifyServiceImpl_11", "hr-htm-business", new Object[0]));
                            view.invokeOperation("refresh");
                            view.sendFormAction(view);
                        } else {
                            view.showMessage(ResManager.loadKDString("数据异常", "QuitCertifyServiceImpl_3", "hr-htm-business", new Object[0]));
                        }

                    }
                }
            }
        }
    }

    @Override
    public void showCertifyDeatilPage(IFormView view, Object pkId) {
        BaseShowParameter showParameter = new BaseShowParameter();
        Map<String, Object> map = new HashMap();
        map.put("isEditPage", "1");
        showParameter.setCustomParams(map);
        showParameter.setCaption(ResManager.loadKDString("开具离职证明", "QuitCertifyServiceImpl_7", "hr-htm-business", new Object[0]));
        showParameter.setFormId("tdkw_certifymange");
        showParameter.setPkId(pkId);
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setStatus(OperationStatus.EDIT);
        view.showForm(showParameter);
    }


    @Override
    public void dealCertifyDialog(AfterDoOperationEventArgs args, AbstractFormPlugin pluginList, IFormView view, Object pkId, boolean isQuitHand) {
        String pkStr = pkId.toString();
        Long aLong = HRJSONUtils.getLongValOfCustomParam(pkId);
        DynamicObject certify;
        if (isQuitHand) {
            certify = CETIFY_SERVICE_HELPER.queryOne("id,tdkw_transferbill.tdkw_certifystatus,tdkw_transferbill.tdkw_transferstatus,issuancetimes", new QFilter[]{new QFilter("tdkw_transferbill.id", "=", aLong), new QFilter("iseffective", "=", Boolean.TRUE)});
            if (certify == null) {
                // 调动单
                DynamicObject transferApplyDy = TRANSFER_SERVICE_HELPER.queryOne("id, billno, creator,modifier,person,bcompany,borg,bposition", aLong);
                ActivityGenerateService activityGenerateService = new ActivityGenerateServiceImpl();
                certify = activityGenerateService.generateQuitCertifyActivity((Long)null, transferApplyDy);
                CETIFY_SERVICE_HELPER.save(new DynamicObject[]{certify});
            }
        } else {
            certify = CETIFY_SERVICE_HELPER.queryOne("id,tdkw_transferbill.tdkw_certifystatus,tdkw_transferbill.tdkw_transferstatus,issuancetimes", aLong);
        }

        if (certify == null) {
            view.showMessage(ResManager.loadKDString("数据异常", "QuitCertifyServiceImpl_3", "hr-htm-business", new Object[0]));
        } else {
            String tdkw_certifystatus = certify.getString("tdkw_transferbill.tdkw_certifystatus");
            if (QuitCertificationStatusEnum.FINISHED.getStatus().equals(tdkw_certifystatus)) {
                this.showConfirm(args, pluginList, view, certify.get("id"));
            } else if (QuitCertificationStatusEnum.PENDING.getStatus().equals(tdkw_certifystatus)) {
                this.showCertifyDeatilPage(view, certify.get("id"));
            } else {
                // 调动单
                DynamicObject apply = TRANSFER_SERVICE_HELPER.queryOne("person", Long.valueOf(pkStr));
                if (apply == null) {
                    view.showMessage(ResManager.loadKDString("数据已发生改变，请刷新列表重试", "QuitCertifyServiceImpl_12", "hr-htm-business", new Object[0]));
                }
            }
        }
    }

    private void showConfirm(AfterDoOperationEventArgs args, AbstractFormPlugin pluginList, IFormView view, Object primaryKeyValue) {
        HashMap<String, String> lastoptMap = getLastPrintDateAndPerson(HRJSONUtils.getLongValOfCustomParam(primaryKeyValue));
        HashMap<String, String> optMap = new HashMap();
        optMap.put("0", ResManager.loadKDString("打印纸质离职证明", "QuitCertifyServiceImpl_8", "hr-htm-business", new Object[0]));
        optMap.put("1", ResManager.loadKDString("发送离职证明", "QuitCertifyServiceImpl_9", "hr-htm-business", new Object[0]));
        if (lastoptMap == null) {
            this.showCertifyDeatilPage(view, primaryKeyValue);
        } else {
            view.showConfirm(MessageFormat.format(ResManager.loadKDString("【{0}】已于【{1}】操作【{2}】，确定要再次开具离职证明吗？", "QuitCertifyServiceImpl_10", "hr-htm-business", new Object[0]), lastoptMap.get("personname"), lastoptMap.get("logcreatetime"), optMap.get(lastoptMap.get("issuancetype"))), MessageBoxOptions.OKCancel, new ConfirmCallBackListener("htm_certifydialog_callbackid", pluginList));
        }

    }

    public HashMap<String, String> getLastPrintDateAndPerson(Long pk) {
        QFilter filter = (new QFilter("certifyhandle", "=", pk)).and(QFilter.isNotNull("modifytime"));
        DynamicObject[] certifyLog = CETIFYLOG_SERVICE_HELPER.query("personid,createtime,issuancetype", new QFilter[]{filter}, "createtime");
        if (certifyLog.length == 0) {
            return null;
        } else {
            DynamicObject certify = CertifyRepository.getInstance().queryOne("person", pk);
            HashMap<String, String> map = new HashMap();
            Date date = certifyLog[0].getDate("createtime");
            DynamicObject person = certify.getDynamicObject("person");
            map.put("personname", person.getString("name"));
            map.put("logcreatetime", HRDateTimeUtils.format(date, "yyyy-MM-dd"));
            map.put("issuancetype", certifyLog[0].getString("issuancetype"));
            return map;
        }
    }
    @Override
    public List<HashMap<String, Object>> getCertifyAllNumber(Long personid) {
        List<HashMap<String, Object>> list = (List) HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonService", "listPersonAttachs", new Object[]{personid, "hrpi_percre"});
        List<HashMap<String, Object>> returnList = new ArrayList(list.size());
        Iterator var4 = list.iterator();

        while(var4.hasNext()) {
            HashMap<String, Object> dy = (HashMap)var4.next();
            HashMap<String, Object> map = new HashMap(4);
            map.put("certificatetype", dy.get("credentialstype_id"));
            map.put("number", dy.get("number"));
            map.put("ismajor", dy.get("ismajor"));
            returnList.add(map);
        }

        return returnList;
    }

    @Override
    public String getCertifyNumber(long personId, long type) {
        List<HashMap<String, Object>> certifyAllNumber = this.getCertifyAllNumber(personId);
        Optional<HashMap<String, Object>> certifyNumberOpt = certifyAllNumber.stream().filter((row) -> {
            Long certifyTypeId = HRJSONUtils.getLongValOfCustomParam(row.get("certificatetype"));
            return certifyTypeId != null ? certifyTypeId.equals(type) : false;
        }).findAny();
        return certifyNumberOpt.isPresent() ? (String)((HashMap)certifyNumberOpt.get()).get("number") : "";
    }

    @Override
    public void updateQuitApplyAndCertifyByPrint(DynamicObject certify) {
        DynamicObject certifyNNew = CETIFY_SERVICE_HELPER.queryOne("tdkw_transferbill,lastoperate,certifylog,issuancetimes,finishdate,activity.number", new QFilter[]{new QFilter("id", "=", certify.getLong("id"))});
        certifyNNew.set("issuancetimes", certify.getInt("issuancetimes") + 1);
        certifyNNew.set("finishdate", new Date());
        DynamicObject transferApply = BASE_SERVICE_HELPER.queryOne("id,tdkw_certifystatus",certifyNNew.getLong("tdkw_transferbill.id"));
        transferApply.set("tdkw_certifystatus", QuitCertificationStatusEnum.FINISHED.getStatus());

        DynamicObject certifyLog = this.getCertifyLogByLastOpt(certify.getLong("id"), YesNo.YES.getValue());
        certifyLog.set("modifytime", new Date());
        TXHandle txHandle = TX.required();

        try {
            CETIFYLOG_SERVICE_HELPER.saveOne(certifyLog);
            CETIFY_SERVICE_HELPER.saveOne(certifyNNew);
            BASE_SERVICE_HELPER.updateOne(transferApply);
            if (certifyNNew.get("activity") != null) {
                IQuitStaffService.getInstance().sendQuitActivityMsg(certifyNNew.getLong("quitapply.id"), certifyNNew.getString("activity.number"));
            }
        } catch (Exception var10) {
            txHandle.markRollback();
        } finally {
            txHandle.close();
        }

    }


    @Override
    public void updateCertifyByPreview(DynamicObject certify) {
        DynamicObject certifyNNew = CETIFY_SERVICE_HELPER.queryOne("lastoperate,certifylog,issuancetimes,operatelogid", new QFilter[]{new QFilter("id", "=", certify.getLong("id"))});
        certifyNNew.set("lastoperate", PrintPriviewTypeEnum.DETAIL.getValue());
        DynamicObject certifyLog = CETIFYLOG_SERVICE_HELPER.generateEmptyDynamicObject();
        this.setLog(certify, certifyLog);
        certifyLog.set("modifier", RequestContext.get().getCurrUserId());
        TXHandle txHandle = TX.required();

        try {
            CETIFY_SERVICE_HELPER.save(new DynamicObject[]{certifyNNew});
            CETIFYLOG_SERVICE_HELPER.save(new DynamicObject[]{certifyLog});
        } catch (Exception var9) {
            txHandle.markRollback();
        } finally {
            txHandle.close();
        }

    }

    private void setLog(DynamicObject certify, DynamicObject certifyLog) {
        certifyLog.set("certificatetype", certify.get("certificatetype"));
        certifyLog.set("certifyhandle", certify.get("id"));
        if (certify.getDynamicObject("cmphis") != null) {
            certifyLog.set("cmphis", certify.getDynamicObject("cmphis"));
        }

        if (certify.getDynamicObject("poshis") != null) {
            certifyLog.set("poshis", certify.getDynamicObject("poshis"));
        }
        // 劳动结束日
//        certifyLog.set("contractenddate", certify.getDate("quitapply.contractenddate"));
        certifyLog.set("starttime", certify.get("starttime"));
        certifyLog.set("endtime", certify.get("endtime"));
        // 入职日期
//        certifyLog.set("enterdate", certify.get("quitapply.enterdate"));
        certifyLog.set("handletime", new Date());
        certifyLog.set("handler", RequestContext.get().getCurrUserId());
        certifyLog.set("creator", RequestContext.get().getCurrUserId());
        certifyLog.set("createtime", new Date());
        certifyLog.set("issuancetype", certify.get("issuancetype"));
    }

    @Override
    public void updateCertifyByHandCertifyLogPreview(Long id, Long logId, String lastOpt) {
        DynamicObject certifyNNew = CETIFY_SERVICE_HELPER.queryOne("lastoperate,certifylog,issuancetimes,operatelogid", new QFilter[]{new QFilter("id", "=", id)});
        certifyNNew.set("lastoperate", lastOpt);
        certifyNNew.set("operatelogid", logId);
        CETIFY_SERVICE_HELPER.updateDataOne(certifyNNew);
    }

    @Override
    public DynamicObject getCertifyLogByLastOpt(Long pk, String lastOpt) {
        String properties = "certifyhandle,cmphis,poshis,contractenddaten,starttime,endtime,certificatetype,entrydate,modifytime,createtime,creator";
        DynamicObject[] dynamicObjects = CETIFYLOG_SERVICE_HELPER.query(properties, new QFilter[]{new QFilter("id", QCP.equals,pk)});
        DynamicObject dynamicObject = CETIFYLOG_SERVICE_HELPER.generateEmptyDynamicObject();
        if (PrintPriviewTypeEnum.DETAIL.getValue().equals(lastOpt)) {
            return dynamicObjects[0];
        } else if (PrintPriviewTypeEnum.LIST.getValue().equals(lastOpt)) {
            Optional<DynamicObject> modifytime = Arrays.stream(dynamicObjects).filter((row) -> {
                return row.get("modifytime") != null;
            }).findFirst();
            return modifytime.isPresent() ? (DynamicObject)modifytime.get() : dynamicObject;
        } else {
            DynamicObject certify = CETIFY_SERVICE_HELPER.queryOne("operatelogid", pk);
            Long logId = certify.getLong("operatelogid");
            Optional<DynamicObject> any = Arrays.stream(dynamicObjects).filter((row) -> {
                return logId.equals(row.getLong("id"));
            }).findAny();
            return any.isPresent() ? (DynamicObject)any.get() : dynamicObject;
        }
    }
}
