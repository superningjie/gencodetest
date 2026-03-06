package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import kd.bos.algo.DataSet;
import kd.bos.algo.JoinDataSet;
import kd.bos.algo.JoinType;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.container.Tab;
import kd.bos.form.control.Button;
import kd.bos.form.control.RichTextEditor;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.threads.ThreadPools;
import org.apache.commons.lang.StringUtils;
import tdkw.hrmp.hrobs.formplugin.util.SendTipTuple;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;


/**
 * @Metadata： tdkw_sendtip
 * @Description ： 发送提醒信息框
 * @ClassName ：SendTipFormPlugin
 * @author xxx
 * @Date ：2023/6/15 11:46
 * @Version: 1.0
 */
public class SendTipFormPlugin extends AbstractFormPlugin {

    private static final Log Logger = LogFactory.getLog(SendTipFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners(SendTipFieldUtils.btOk);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        RichTextEditor edit = this.getView().getControl(SendTipFieldUtils.content);
        String content = SendTipFieldUtils.tipContent;
        edit.setText(content);
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);
        Object source = evt.getSource();
        if (source instanceof Button) {
            String key = ((Button) source).getKey();
            if (StringUtils.equals(key, SendTipFieldUtils.btOk)) {
                //判断邮件内容是否为空
                RichTextEditor editor = this.getControl(SendTipFieldUtils.content);
                String text = editor.getText();
                if (StringUtils.isEmpty(text)) {
                    this.getView().showTipNotification(ResManager.loadKDString("请填写发送内容！", "SendTipFormPlugin_0", "tdkw.hrmp.hrobs.formplugin"));
                    evt.setCancel(true);
                    return;
                }

                Tab tab = this.getControl(SendTipFieldUtils.tab);
                String currentTab = tab.getCurrentTab();
                if (StringUtils.equals(currentTab, SendTipFieldUtils.tabuser)) {
                    String userRadio = (String) this.getModel().getValue(SendTipFieldUtils.userRadio);
                    if (StringUtils.equals(userRadio, SendTipFieldUtils.radio1)) {
                        List<String> selectRows = this.getView().getFormShowParameter().getCustomParam("selectRows");
                        if (selectRows.size() == 0) {
                            this.getView().showTipNotification(ResManager.loadKDString("请先勾选需要发送邮件的人员！", "SendTipFormPlugin_1", "tdkw.hrmp.hrobs.formplugin"));
                            evt.setCancel(true);
                            return;
                        }
                    }
                    if (StringUtils.equals(userRadio, SendTipFieldUtils.radio2)) {
                        int integrity = (int) this.getModel().getValue(SendTipFieldUtils.integrity);
                        if (integrity < 1) {
                            this.getView().showTipNotification(ResManager.loadKDString("请填写完整度！", "SendTipFormPlugin_2", "tdkw.hrmp.hrobs.formplugin"));
                            evt.setCancel(true);
                            return;
                        }
                    }
                }
                if (StringUtils.equals(currentTab, SendTipFieldUtils.taborg)) {
                    DynamicObject org = (DynamicObject) this.getModel().getValue(SendTipFieldUtils.org);
                    if (org == null) {
                        this.getView().showTipNotification(ResManager.loadKDString("请先选择组织！", "SendTipFormPlugin_3", "tdkw.hrmp.hrobs.formplugin"));
                        evt.setCancel(true);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Object source = evt.getSource();
        if (source instanceof Button) {
            String key = ((Button) source).getKey();
            if (StringUtils.equals(key, SendTipFieldUtils.btOk)) {
                String tip = ResManager.loadKDString("确定发送消息提醒?", "SendTipFormPlugin_4", "tdkw.hrmp.hrobs.formplugin");
                this.getView().showConfirm(tip, MessageBoxOptions.YesNo, new ConfirmCallBackListener("ok"));
            }
        }
    }


    @Override
    public void confirmCallBack(MessageBoxClosedEvent evt) {
        super.confirmCallBack(evt);
        String actionId = evt.getCallBackId();
        MessageBoxResult result = evt.getResult();
        if (StringUtils.equals(actionId, "ok") && result.equals(MessageBoxResult.Yes)) {
            RichTextEditor editor = this.getControl(SendTipFieldUtils.content);
            String content = editor.getText();
            String channel = (String) this.getModel().getValue("tdkw_channel");
            Tab tab = this.getControl(SendTipFieldUtils.tab);
            String currentTab = tab.getCurrentTab();
            //选择按人员下
            if (StringUtils.equals(currentTab, SendTipFieldUtils.tabuser)) {
                String value = (String) this.getModel().getValue(SendTipFieldUtils.userRadio);
                //按人员中发送提醒给选中的人完善信息
                if (StringUtils.equals(value, SendTipFieldUtils.radio1)) {
                    List<Long> selectRows = this.getView().getFormShowParameter().getCustomParam("selectRows");
                    Logger.info("员工信息完整度：按人员接收人数量：" + selectRows.size());
                    sendEmailOrOA(content, selectRows, channel);
                }
                //按人员中只发给完整度小于%的人员
                if (StringUtils.equals(value, SendTipFieldUtils.radio2)) {
                    //获取列表人员信息
                    List<Long> listPerson = this.getView().getFormShowParameter().getCustomParam("listPerson");
                    int integrityNum = (int) this.getModel().getValue(SendTipFieldUtils.integrity);
                    QFilter filter = new QFilter("tdkw_progressbar", QCP.less_than, (double) integrityNum / 100);
                    filter.and("iscurrentversion", QCP.equals, true);
                    filter.and("datastatus", QCP.equals, "1");
                    filter.and("person", QCP.in, listPerson);
                    DynamicObjectCollection personInfos = QueryServiceHelper.query("hspm_personinfo", "person", filter.toArray());
                    List<Long> persons = new ArrayList<>();
                    for (DynamicObject personInfo : personInfos) {
                        Long personId = personInfo.getLong("person");
                        persons.add(personId);
                    }
                    Logger.info("员工信息完整度：按百分比人员接收人数量：" + persons.size());
                    sendEmailOrOA(content, persons, channel);
                }
            }
            if (StringUtils.equals(currentTab, SendTipFieldUtils.taborg)) {
                //获取列表人员信息
                List<Long> listPerson = this.getView().getFormShowParameter().getCustomParam("listPerson");
                DynamicObject org = (DynamicObject) this.getModel().getValue(SendTipFieldUtils.org);
                boolean isContainBelow = (boolean) this.getModel().getValue("tdkw_iscontainbelow");
                QFilter filter = new QFilter("tdkw_progressbar", QCP.less_than, 100);
                filter.and("iscurrentversion", QCP.equals, true);
                filter.and("datastatus", QCP.equals, "1");
                filter.and("person", QCP.in, listPerson);
                //完整度小于100%的人员信息
                DataSet pernontsprop = QueryServiceHelper.queryDataSet("q1", "hrpi_pernontsprop", "person,tdkw_progressbar,height", filter.toArray(), null);
                QFilter filter1 = new QFilter("empposrel.isprimary", QCP.equals, "1");
                filter1.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
                filter1.and("empposrel.businessstatus", QCP.equals, "1");
                filter1.and("person", QCP.in, listPerson);
                if (isContainBelow) {
                    List<Long> orgIds = new ArrayList<>();
                    orgIds.add(org.getLong("id"));
                    List<Long> allBelowHROrg = SendTipFieldUtils.getAllBelowHROrg(orgIds, new ArrayList<>());
                    filter1.and("empposrel.adminorg", QCP.in, allBelowHROrg);
                } else {
                    filter1.and("empposrel.adminorg", QCP.equals, org.getPkValue());
                }

                DataSet ermanfile = QueryServiceHelper.queryDataSet("q2", "hspm_ermanfile", "person,number", filter1.toArray(), null);
                JoinDataSet join = ermanfile.join(pernontsprop, JoinType.LEFT);
                DataSet finish = join.on("person", "person").select(new String[]{"person", "number"}, new String[]{"tdkw_progressbar", "height"}).finish();
                List<Long> persons = new ArrayList<>();
                for (Row row : finish) {
                    persons.add(row.getLong("person"));
                }
                Logger.info("员工信息完整度：按组织接收人数量：" + persons.size());
                sendEmailOrOA(content, persons, channel);
            }
        }

    }

    /**
     * 拆分人员
     *
     * @param inputList
     * @param chunkSize
     * @return
     */
    public static List<List<Long>> splitList(List<Long> inputList, int chunkSize) {
        List<List<Long>> result = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, inputList.size());
            List<Long> chunk = inputList.subList(i, end);
            result.add(chunk);
        }
        return result;
    }

    public void sendEmailOrOA(String content, List<Long> persons, String channel) {
        Logger.info("员工信息完整度：接收人：" + persons);
        long start = System.currentTimeMillis();
        List<List<Long>> splitLists;

        boolean flag = false;
        //当人员大于1000时每次发送1000人
        Integer chunkSize = Integer.valueOf(System.getProperty("hrobs.sendPersonInfo"));
        if (persons.size() > chunkSize) {
            splitLists = splitList(persons, chunkSize);
            flag = true;
        } else {
            splitLists = new ArrayList<>();
        }
        if (StringUtils.equals(channel, SendTipFieldUtils.radio1)) {
            if (flag) {
                ThreadPools.executeOnce("sendPersonInfoMsg", () -> {
                    int i = 1;
                    for (List<Long> personIds : splitLists) {
                        SendTipTuple<Boolean, String> emailResult = SendTipFieldUtils.sendEmail(content, personIds);
                        if (personIds.equals(splitLists.get(splitLists.size() - 1))) {
                            pageShowMsg(emailResult);
                        }
                        Logger.info("人员拆分第" + i++ + "批发送邮件耗时" + (System.currentTimeMillis() - start));
                    }
                });
                pageShowMsg(SendTipTuple.create(true, "发送成功！"));
            } else {
                SendTipTuple<Boolean, String> emailResult = SendTipFieldUtils.sendEmail(content, persons);
                pageShowMsg(emailResult);
            }
            Logger.info("人员发送邮件耗时" + (System.currentTimeMillis() - start));

        } else if (StringUtils.equals(channel, SendTipFieldUtils.radio2)) {
            if (flag) {
                ThreadPools.executeOnce("sendPersonInfoMsg", () -> {
                    int i = 1;
                    for (List<Long> personIds : splitLists) {
                        SendTipTuple<Boolean, String> sendOAResult = SendTipFieldUtils.sendOA(content, personIds);
                        SendTipTuple<Boolean, String> sendXYResult = SendTipFieldUtils.sendXYTip(content, personIds);
//                        String msg = sendOAResult.message + "\n" + sendXYResult.message;
//                        if (sendXYResult.isSuccess && sendOAResult.isSuccess) {
//                            pageShowMsg(SendTipTuple.create(true, "发送成功！"));
//                        } else {
//                            pageShowMsg(SendTipTuple.create(false, msg));
//                        }
                        Logger.info("人员拆分发送OA信息第" + i++ + "批提醒耗时" + (System.currentTimeMillis() - start));
                    }
                });
                pageShowMsg(SendTipTuple.create(true, "发送成功！"));
            } else {
                SendTipTuple<Boolean, String> sendOAResult = SendTipFieldUtils.sendOA(content, persons);
                SendTipTuple<Boolean, String> sendXYResult = SendTipFieldUtils.sendXYTip(content, persons);
                String msg = sendOAResult.message + "\n" + sendXYResult.message;
                if (sendXYResult.isSuccess && sendOAResult.isSuccess) {
                    pageShowMsg(SendTipTuple.create(true, "发送成功！"));
                } else {
                    pageShowMsg(SendTipTuple.create(false, msg));
                }
            }
            Logger.info("人员发送OA信息提醒耗时" + (System.currentTimeMillis() - start));

        } else {
            if (flag) {
                ThreadPools.executeOnce("sendPersonInfoMsg", () -> {
                    int i = 1;
                    for (List<Long> personIds : splitLists) {
                        SendTipTuple<Boolean, String> emailResult = SendTipFieldUtils.sendEmail(content, personIds);
                        SendTipTuple<Boolean, String> sendOAResult = SendTipFieldUtils.sendOA(content, personIds);
                        SendTipTuple<Boolean, String> sendXYResult = SendTipFieldUtils.sendXYTip(content, personIds);
//                            String msg = emailResult.message + "\n" + sendOAResult.message + "\n" + sendXYResult.message;
//                            if (emailResult.isSuccess && sendOAResult.isSuccess && sendXYResult.isSuccess) {
//                                pageShowMsg(SendTipTuple.create(true, "发送成功！"));
//                            } else {
//                                pageShowMsg(SendTipTuple.create(false, msg));
//                            }
                        Logger.info("人员拆分发送邮件和OA信息第" + i++ + "批提醒耗时" + (System.currentTimeMillis() - start));
                    }
                });
                pageShowMsg(SendTipTuple.create(true, "发送成功！"));
            } else {
                SendTipTuple<Boolean, String> emailResult = SendTipFieldUtils.sendEmail(content, persons);
                SendTipTuple<Boolean, String> sendOAResult = SendTipFieldUtils.sendOA(content, persons);
                SendTipTuple<Boolean, String> sendXYResult = SendTipFieldUtils.sendXYTip(content, persons);
                String msg = emailResult.message + "\n" + sendOAResult.message + "\n" + sendXYResult.message;
                if (emailResult.isSuccess && sendOAResult.isSuccess && sendXYResult.isSuccess) {
                    pageShowMsg(SendTipTuple.create(true, "发送成功！"));
                } else {
                    pageShowMsg(SendTipTuple.create(false, msg));
                }
                Logger.info("人员发送邮件和OA信息提醒耗时" + (System.currentTimeMillis() - start));
            }
        }

    }

    /**
     * 界面提示语
     *
     * @param result
     */
    public void pageShowMsg(SendTipTuple<Boolean, String> result) {
        Boolean isSuccess = result.isSuccess;
        String msg = ResManager.loadKDString(result.message, "SendTipFormPlugin_5", "tdkw.hrmp.hrobs.formplugin");
        this.getView().close();
        if (isSuccess) {
            this.getView().getParentView().showSuccessNotification(msg);
        } else {
            this.getView().getParentView().showTipNotification(msg);
        }
        this.getView().sendFormAction(this.getView().getParentView());
        //   this.getView().getParentView().invokeOperation("refresh");
    }


}
