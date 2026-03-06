package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessTaskEntityService;
import cds0.opmc.cea.business.entityservice.DimassesserEntityService;
import cds0.opmc.cea.common.AssessTaskStatusEnum;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.form.operate.MutexHelper;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mutex.DataMutex;
import kd.bos.mutex.impl.MutexLockInfo;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.formplugin.web.HRDataBaseList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.*;


/*
 * 测评任务列表
 */
public class AssessTaskListPlugin extends HRDataBaseList {

    //测评任务信息表
    private static final Log log = LogFactory.getLog(AssessTaskListPlugin.class);
    //测评活动
    private static final String KEY_ASSESSACT = "assessact";
    //测评人id
    private static final String KEY_USERID = "assesserperson.id";
    //”评价“按钮
    private static final String KEY_EVALUATE = "evaluate";
    private static final AssessTaskEntityService assessTaskEntityService = AssessTaskEntityService.getInstance();
    private static final DimassesserEntityService dimassesserEntityService = DimassesserEntityService.getInstance();


    @Override
    public void setFilter(SetFilterEvent evt) {
        super.setFilter(evt);
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long activityId = Long.parseLong(parameter.getCustomParam("assessActivityId"));
        Long userId = UserServiceHelper.getCurrentUserId();
        List<QFilter> qfilters = evt.getQFilters();
        //过滤评测活动id
        QFilter filterByActivity = new QFilter(KEY_ASSESSACT, QCP.equals, activityId);
        //过滤评测人id
        QFilter filterByUserId = new QFilter(KEY_USERID, QCP.equals, userId);
        qfilters.add(filterByActivity);
        qfilters.add(filterByUserId);
        //根据任务到达时间倒序展示
        evt.setOrderBy("taskarrivaltime Desc");
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        String itemKey = evt.getItemKey();
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Long assessActivityId = Long.parseLong(formShowParameter.getCustomParam("assessActivityId"));

        //点击评价按钮
        if (KEY_EVALUATE.equals(itemKey)) {
            //查询当前测评活动中当前测评人的所有测评任务
            List<DynamicObject> assesserTasks = queryTaskByCurrentAssesser();
            //判断当前测评人是否还有需要评价的测评任务,没有则提示”测评任务已完成“
            long count = assesserTasks.stream().filter(assesserTask -> AssessTaskStatusEnum.WAITTING.getValue().equals(assesserTask.getString("assestaskstatus"))
                    || AssessTaskStatusEnum.TEMPSTORAGE.getValue().equals(assesserTask.getString("assestaskstatus"))).count();
            if (count == 0) {
                getView().showTipNotification(ResManager.loadKDString("测评任务已完成", "AssessIngListPlugin_0", KEY_APP_NAME));
                return;
            }
            //解决统一用户打开多个页面导致按钮不生效问题
            DataMutex dataMutex = DataMutex.create();
            Long userId = UserServiceHelper.getCurrentUserId();
            //获取锁
            Map<String, String> lockInfoMap = dataMutex.getLockInfo(assessActivityId.toString() + userId, "default_netctrl", "epa_actevalobj");
            if (lockInfoMap == null) {
                MutexLockInfo lockInfo = new MutexLockInfo();
                lockInfo.setStrict(true);
                lockInfo.setEntityNumber("epa_actevalobj");
                lockInfo.setOperationKey("nodeview");
                lockInfo.setCallSource("default");
                lockInfo.setDataObjId(assessActivityId.toString() + userId);
                StringBuilder msg = new StringBuilder();
                // 添加互斥
                boolean require = MutexHelper.require(lockInfo, msg);
            } else {
                getView().showTipNotification(ResManager.loadKDString("您已在其他页面打开测评任务详情", "AssessIngListPlugin_2", KEY_APP_NAME));
            }
//            将当前测评活动当前测评人的维度测评状态由”未填报“改为”填报中“
            updateAssesserStatus();
            //跳转到评分详情页
            showGradedetails(assessActivityId);
        }
    }

    /**
     * 查询当前测评活动中当前测评人的所有测评任务
     */
    public List<DynamicObject> queryTaskByCurrentAssesser() {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long activityId = Long.parseLong(parameter.getCustomParam("assessActivityId"));
        Long userId = UserServiceHelper.getCurrentUserId();
        return Arrays.stream(assessTaskEntityService.queryAssessTaskByActivityAndAssesser(activityId, userId)).collect(Collectors.toList());
    }


    /**
     * 将当前测评活动当前测评人的维度测评状态由”未填报“改为”填报中“
     */
    public void updateAssesserStatus() {
        //查询当前测评活动中当前测评人的所有测评任务
        List<DynamicObject> assesserTasks = queryTaskByCurrentAssesser();
        //根据id查询出当前测评人的所有状态为”未填报“的维度测评人信息
        List<Long> dimAssesserIds = assesserTasks.stream().map(obj -> obj.getLong("dimassesser.id")).collect(Collectors.toList());
        DynamicObject[] dimAssessers = dimassesserEntityService.querydatabyIds(dimAssesserIds,DimAssesserStatusEnum.UNFILLED.getValue());

        Arrays.stream(dimAssessers).collect(Collectors.toList()).forEach(dimAssesser -> dimAssesser.set("assesstatus", DimAssesserStatusEnum.FILLING.getValue()));
        dimassesserEntityService.update(dimAssessers);
    }

    /**
     * 跳转到评分详情页
     */
    public void showGradedetails(Long assessActivityId) {
        BaseShowParameter showParameter = new BaseShowParameter();
        showParameter.setFormId(GRADEDETAILS);
        showParameter.setStatus(OperationStatus.EDIT);
        showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        showParameter.setCustomParam("assessActivityId", assessActivityId);
        showParameter.setHasRight(Boolean.TRUE);
        showParameter.setCloseCallBack(new CloseCallBack(this, GRADEDETAILS));
        Long userId = UserServiceHelper.getCurrentUserId();
        showParameter.setPageId(assessActivityId + "_" + userId + "_" + 0);
        this.getView().showForm(showParameter);
    }
}
