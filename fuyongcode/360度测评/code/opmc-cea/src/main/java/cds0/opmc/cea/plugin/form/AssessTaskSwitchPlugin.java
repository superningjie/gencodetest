package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.common.AppflgConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.EventObject;
import java.util.List;

import static cds0.opmc.cea.common.AppflgConstant.GRADEDETAILS;


/**
 * 任务切换
 */
public class AssessTaskSwitchPlugin extends AbstractFormPlugin {
    /**
     * 监听按钮
     * cds0_buttonap11 -- 上一个
     * cds0_buttonap12 -- 下一个
     * index:页码下标
     * @param e
     */
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("cds0_buttonap11", "cds0_buttonap12");
    }
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control)evt.getSource();
        FormShowParameter parameter = this.getView().getFormShowParameter();
        String subpagesStr = this.getPageCache().get("subpages");
        Long assessActivityId = parameter.getCustomParam("assessActivityId");
        String assessFormRowNum = this.getPageCache().get("assessFormRowNum");
        String dimAssessors = this.getPageCache().get("dimAssessors");
        String dimAssessorList = this.getPageCache().get("dimAssessorList");
        String evalDimIds = this.getPageCache().get("evalDimIds");
        List<List<Long>> meterHeader = JSON.parseObject(this.getPageCache().get("evalDimIds"), new TypeReference<List<List<Long>>>() {});
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        int size = meterHeader.size();
        Integer taskIndex = (Integer) showParameter.getCustomParam("index");
        if(taskIndex == null ){
            taskIndex = 0;
        }
        if (source.getKey().equals("cds0_buttonap11")){ //上一个,index:页码下标
            if(taskIndex.equals(0)){
                getView().showTipNotification(ResManager.loadKDString("暂无上一个待测评任务", "AssessTaskSwitchPlugin_1", AppflgConstant.KEY_APP_NAME));
                return;
            }
            if(taskIndex > 0){
                taskIndex = taskIndex - 1;
            }
            showGradedetails(assessActivityId,taskIndex,subpagesStr,assessFormRowNum,dimAssessors,dimAssessorList,evalDimIds);
            this.getPageCache().put("taskIndex",String.valueOf(taskIndex));
        } else if (source.getKey().equals("cds0_buttonap12")) { //下一个,index:页码下标
            if(size == 0){
                return;
            }
            if(taskIndex+1 >= size){
                getView().showTipNotification(ResManager.loadKDString("暂无下一个待测评任务", "AssessTaskSwitchPlugin_2", AppflgConstant.KEY_APP_NAME));

            }else {
                taskIndex = taskIndex + 1;
                showGradedetails(assessActivityId,taskIndex,subpagesStr,assessFormRowNum,dimAssessors,dimAssessorList,evalDimIds);
            }

        }
    }
    public void showGradedetails(Long assessActivityId,Integer index,String subpagesStr,String assessFormRowNum,String dimAssessors,String dimAssessorList, String meterHeader) {
        BaseShowParameter showParameter = new BaseShowParameter();
        showParameter.setFormId(GRADEDETAILS);
        showParameter.setStatus(OperationStatus.EDIT);
        showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        showParameter.setCustomParam("assessActivityId", assessActivityId);
        showParameter.setCustomParam("index", index);
        showParameter.setCustomParam("flag", "true");
        showParameter.setCustomParam("evalDimIds", meterHeader);
        showParameter.setCustomParam("subpagesStr", subpagesStr);
        showParameter.setCustomParam("assessFormRowNum", assessFormRowNum);
        showParameter.setCustomParam("dimAssessors", dimAssessors);
        showParameter.setCustomParam("dimAssessorList", dimAssessorList);
        long userId = RequestContext.get().getCurrUserId();

        showParameter.setPageId(assessActivityId+"_"+userId+"_"+index);
        showParameter.setCloseCallBack(new CloseCallBack(this, GRADEDETAILS));
        this.getView().showForm(showParameter);
    }
}
