package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.container.Tab;
import kd.bos.form.container.TabPage;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.control.events.TabSelectEvent;
import kd.bos.form.control.events.TabSelectListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.list.ListShowParameter;
import static cds0.opmc.cea.common.AppflgConstant.*;

import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessExecuteManageEditPlugin extends HRDataBaseEdit implements RowClickEventListener, TabSelectListener {

    private static final AssessObjEntityService ASSESS_OBJ_ENTITY_SERVICE = AssessObjEntityService.getInstance();

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 页签添加监听事件
        Tab tab = this.getView().getControl("tabap");
        tab.addTabSelectListener(this);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        // 初始化页签名
        initTabLabelText();
        // 默认页签选择 待启动
        Tab tab = this.getView().getControl("tabap");
        tab.activeTab("tostartup");
    }

    @Override
    public void tabSelected(TabSelectEvent tabSelectEvent) {
        String activeTabKey = tabSelectEvent.getTabKey();
        switch (activeTabKey){
            case "tostartup":
                // show出待启动列表
                showTabSelectedListPage(activeTabKey,CEA_ASSOBJ_TOSTARTUP);
                break;
            case "assessing":
                // show出测评中列表
                showTabSelectedListPage(activeTabKey,CEA_ASSOBJ_ASSESSING);
                break;
            case "finished":
                // show出已完成列表
                showTabSelectedListPage(activeTabKey,CEA_ASSOBJ_FINISHED);
                break;
        }
        // 刷新tab
        initTabLabelText();
    }

    /**
     * show出选中页签列表页面
     * @param tabKey
     * @param formId
     */
    private void showTabSelectedListPage(String tabKey,String formId){
        ListShowParameter listShowParameter = new ListShowParameter();
        // 显示类型，设置为在容器中显示
        listShowParameter.getOpenStyle().setShowType(ShowType.InContainer);
        // 设置要嵌入的单据标识
        listShowParameter.setBillFormId(formId);
        // 将页面嵌入哪个容器，容器标识，需要将 targetKey 替换为具体的容器标识，比如你单据上的 test_flex
        listShowParameter.getOpenStyle().setTargetKey(tabKey);
        // 设置列表风格
        listShowParameter.setFormId("bos_list");
        listShowParameter.setCustomParam("assessActId",getView().getFormShowParameter().getCustomParam("assessActId"));
        StyleCss styleCss = new StyleCss();
        styleCss.setHeight("91vh");
        listShowParameter.setHasRight(Boolean.TRUE);
        listShowParameter.getOpenStyle().setInlineStyleCss(styleCss);
        // 显示表单
        this.getView().showForm(listShowParameter);
    }

    /**
     * 初始化页签名
     */
    private void initTabLabelText() {
        Long assessActId = getView().getFormShowParameter().getCustomParam("assessActId");
        int toStartUpCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.TOASSESS.getValue());
        int assessIngCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.ASSESSINGIN.getValue());
        int processedCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.PROCESSED.getValue());
        TabPage tostartupAp = this.getControl("tostartup");
        TabPage assessIngAp = this.getControl("assessing");
        TabPage finishedAp = this.getControl("finished");
        tostartupAp.setText(new LocaleString(String.format(ResManager.loadKDString("待启动（%s）", "ProcessExecuteManageEditPlugin_0", AppflgConstant.KEY_APP_NAME),String.valueOf(toStartUpCount))));
        assessIngAp.setText(new LocaleString(String.format(ResManager.loadKDString("测评中（%s）", "ProcessExecuteManageEditPlugin_1", AppflgConstant.KEY_APP_NAME),String.valueOf(assessIngCount))));
        finishedAp.setText(new LocaleString(String.format(ResManager.loadKDString("已完成（%s）", "ProcessExecuteManageEditPlugin_2", AppflgConstant.KEY_APP_NAME),String.valueOf(processedCount))));
    }

    /**
     * 刷新页签名
     * @param tab
     */
    private void refreshTabName(Tab tab){
        Long assessActId = getView().getFormShowParameter().getCustomParam("assessActId");
        int toStartUpCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.TOASSESS.getValue());
        int assessIngCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.ASSESSINGIN.getValue());
        int processedCount = ASSESS_OBJ_ENTITY_SERVICE.countAssessObjActivityStatus(assessActId, AssessStatusEnum.PROCESSED.getValue());
        tab.updateTabName("tostartup",String.format(ResManager.loadKDString("待启动（%s）", "ProcessExecuteManageEditPlugin_0", AppflgConstant.KEY_APP_NAME),String.valueOf(toStartUpCount)));
        tab.updateTabName("assessing",String.format(ResManager.loadKDString("测评中（%s）", "ProcessExecuteManageEditPlugin_1", AppflgConstant.KEY_APP_NAME),String.valueOf(assessIngCount)));
        tab.updateTabName("finished",String.format(ResManager.loadKDString("已完成（%s）", "ProcessExecuteManageEditPlugin_2", AppflgConstant.KEY_APP_NAME),String.valueOf(processedCount)));
    }
}
