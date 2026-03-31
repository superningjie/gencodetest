package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.bo.AssObjScoItemInstEntityBO;
import cds0.opmc.cea.business.bo.AssessTaskEntityBO;
import cds0.opmc.cea.business.entityservice.*;
import cds0.opmc.cea.business.service.AssessTaskDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.AssessTaskStatusEnum;
import cds0.opmc.cea.common.enums.AssessFormTypeEnum;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.IPageCache;
import kd.bos.form.container.Container;
import kd.bos.form.control.Control;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.operate.MutexHelper;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.form.container.FlexPanelAp;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.sdk.hr.hspm.formplugin.web.file.ermanfile.drawutil.ApCreateUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static kd.bos.form.ShowType.InContainer;

public class GradeDetailsPlugin extends AbstractFormPlugin {
    private static final Log LOG = LogFactory.getLog(DimSettingEdit.class);
    protected static final AssessActivityEntityService assessActivityEntityService = AssessActivityEntityService.getInstance();
    protected static final AssessTaskEntityService assessTaskEntityService = AssessTaskEntityService.getInstance();
    protected static final AssessFormEntityService assessFormEntityService = AssessFormEntityService.getInstance();
    private static final AssObjScoItemInstEntityService assObjScoItemInstEntityService = AssObjScoItemInstEntityService.getInstance();
    protected static final DimassesserEntityService dimassesserEntityService = DimassesserEntityService.getInstance();
    protected static final AssessTaskDomainService assessTaskDomainService = AssessTaskDomainService.getInstance();


    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessActivityId = parameter.getCustomParam("assessActivityId");
        String flag = parameter.getCustomParam("flag");
        String evalDimIdList = parameter.getCustomParam("evalDimIds");
        String subpagesStr = parameter.getCustomParam("subpagesStr");
        String assessFormRowNum = parameter.getCustomParam("assessFormRowNum");
        String dimAssessors = parameter.getCustomParam("dimAssessors");
        String dimAssessorList = parameter.getCustomParam("dimAssessorList");
        this.getPageCache().put("subpagesStr",subpagesStr);
        this.getPageCache().put("assessFormRowNum",assessFormRowNum);
        this.getPageCache().put("dimAssessors",dimAssessors);
        this.getPageCache().put("dimAssessorList",dimAssessorList);
        Integer number = 0;
        if("true".equals(flag)){
            number = parameter.getCustomParam("index");
        }else {
            flag = "false";
        }
        //初始化页面数据
        loadData(0, assessActivityId, flag,number,evalDimIdList,subpagesStr);
        //判断活动的文案参数是否为空，为空隐藏文案区域
        DynamicObject assessActivity = assessActivityEntityService.queryAssessActivityByPk(assessActivityId);
        if (HRStringUtils.isEmpty(assessActivity.getString("introcontent"))) {
            this.getView().setVisible(false, "cds0_flexpanelap1");
        }
    }
    public void loadData(int index, Long assessActivityId,String flag, Integer number, String evalDimIdList,String subpagesStr) {
        if("true".equals(flag)){
            loadDataForNextOne(assessActivityId,number,evalDimIdList,subpagesStr);
        }else {
            DynamicObject assessActivity = assessActivityEntityService.queryAssessActivityByPk(assessActivityId);
            if (assessActivity != null) {
                //设置周期
                this.getModel().setValue("cds0_textfield", getCycle(assessActivity.getDate("periodstartdate"), assessActivity.getDate("periodenddate")));
            }
            Long scoreSystem = assessActivity.getLong("scoresystem.id");
            //获取当前用户
            Long userId = UserServiceHelper.getCurrentUserId();
            //获取当前用户所有任务
            DynamicObject[] allAssessTasks = assessTaskEntityService.queryWaitingTempStorageByCurrentUserId(assessActivityId, userId);
            //根据分组先将所有任务分组，然后根据分组的顺序排序，得到key是分组value是分组下的任务的list
            List<Map.Entry<Long, List<DynamicObject>>> dimGroupAssessTasks = Arrays.stream(allAssessTasks).collect(Collectors.groupingBy(assessTask -> assessTask.getLong("dimgroup.id"))).entrySet().stream().sorted(new Comparator<Map.Entry<Long, List<DynamicObject>>>() {
                @Override
                public int compare(Map.Entry<Long, List<DynamicObject>> entry1, Map.Entry<Long, List<DynamicObject>> entry2) {
                    //获取分组排序
                    int seq1 = entry1.getValue().get(0).getInt("dimgroup.groupindex");
                    int seq2 = entry2.getValue().get(0).getInt("dimgroup.groupindex");
                    if (seq1 > seq2) {
                        return 1;
                    } else if (seq1 == seq2) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            }).collect(Collectors.toList());
            //将分好的任务list，先按照分组，再按照分组下的维度顺序排序分组
            List<Map.Entry<Long, List<DynamicObject>>> evalDimAssessTasksList = new ArrayList<>();
            List<List<Long>> evalDimIds = new ArrayList<>();
            dimGroupAssessTasks.stream().forEach(entry -> {
                List<Long> taskList = new ArrayList<>();
                entry.getValue().stream().collect(Collectors.groupingBy(evalDimAssessTask -> evalDimAssessTask.getLong("evaldim_id"))).entrySet().stream().sorted(new Comparator<Map.Entry<Long, List<DynamicObject>>>() {
                    @Override
                    public int compare(Map.Entry<Long, List<DynamicObject>> entry1, Map.Entry<Long, List<DynamicObject>> entry2) {
                        //获取维度排序
                        int seq1 = entry1.getValue().get(0).getInt("evaldim.dimindex");
                        int seq2 = entry2.getValue().get(0).getInt("evaldim.dimindex");
                        if (seq1 > seq2) {
                            return 1;
                        } else if (seq1 == seq2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                }).collect(Collectors.toList()).forEach(assessTask -> {
                    evalDimAssessTasksList.add(assessTask);
                });
            });
            //根据维度分页并存缓存，以便页数切换
            evalDimAssessTasksList.forEach(evalDimAssessTasks -> {
                List<Long> longList = new ArrayList<>();
                evalDimAssessTasks.getValue().forEach(item -> longList.add(item.getLong("id")));
                evalDimIds.add(longList);
            });
            this.getPageCache().put("evalDimIds", JSON.toJSONString(evalDimIds));
            //获取第一页数据
            DynamicObject[] assessTasks = evalDimAssessTasksList.get(index).getValue().toArray(new DynamicObject[0]);
            if (assessTasks != null && assessTasks.length > 0) {
                //设置分组
                this.getModel().setValue("cds0_textfield1", assessTasks[0].get("dimgroup.groupdimname"));
                //获取维度id
                Long evalDimId = assessTasks[0].getLong("evaldim_id");
                //设置提示语
                this.getModel().setValue("cds0_basedatafield1", assessActivityId);
                //设置测评表名称
                Long assessFormId = assessTasks[0].getLong("dimgroup.assessform.id");
                this.getModel().setValue("cds0_basedatafield11", assessFormId);
                //根据表名获取表下所有指标
                DynamicObject assessForm = assessFormEntityService.queryAssessFormByPk(assessFormId);
                DynamicObjectCollection assessFormRows = assessForm.getDynamicObjectCollection("entryentity");
                //设置总数
                this.getModel().setValue("cds0_integerfield1", assessFormRows.size());
                //设置绩效等级
                String levelMapNumber = assessFormRows.get(0).getString("levelmap");
                this.getPageCache().put("levelMapNumber", levelMapNumber);
                //获取总的维度测评人ids
                List<Long> dimAssessorIds = Arrays.stream(assessTasks).map(assessTask -> assessTask.getLong("dimassesser.id")).collect(Collectors.toList());
                this.getPageCache().put("dimAssessors", dimAssessorIds.size() + "");
                this.getPageCache().put("dimAssessorList", JSON.toJSONString(dimAssessorIds));
                this.getPageCache().put("assessFormRowNum", assessFormRows.size() + "");
                //通过 绩效等级 总的指标 总的测评对象 show指标测评项
                showGradeExample(assessActivityId, this.getView().getControl("cds0_flexpanelap9"), assessActivity.getString("scoresystemmap"), evalDimId, scoreSystem, assessFormId, assessFormRows, dimAssessorIds);
            }
        }

    }
    public void loadDataForNextOne(Long assessActivityId,Integer number,String evalDimIdsList,String subpagesStr){
        Boolean isShow = false;
        List<List<Long>> meterHeaderList = JSON.parseObject(evalDimIdsList, new TypeReference<List<List<Long>>>() {});
        this.getPageCache().put("evalDimIds", evalDimIdsList);
        this.getPageCache().put("taskIndex",String.valueOf(number));
        this.getPageCache().put("subpagesStr",subpagesStr);
        DynamicObject assessActivity = assessActivityEntityService.queryOne(assessActivityId);
        if (assessActivity != null) {
            //设置周期
            this.getModel().setValue("cds0_textfield", getCycleForNextOne(assessActivity.getDate("periodstartdate"), assessActivity.getDate("periodenddate")));
        }
        Long scoreSystem = assessActivity.getLong("scoresystem.id");
        //获取当前用户
        Long userId = UserServiceHelper.getCurrentUserId();
        //获取所有任务
        DynamicObject[] assessTasks = assessTaskEntityService.queryAssessTaskByEvaldim(assessActivityId,userId,meterHeaderList,number);
        DynamicObject dynamicObject = Arrays.stream(assessTasks).collect(Collectors.toList()).get(0);
        //获取总的维度测评人ids
        List<Long> dimAssessorIds = Arrays.stream(assessTasks).map(assessTask -> Long.valueOf(assessTask.get("dimassesser.id").toString())).collect(Collectors.toList());
        String assestaskstatus = dynamicObject.getString("assestaskstatus");
        if(assestaskstatus.equals(AssessTaskStatusEnum.PROCESSED.getValue())){
            isShow = true;
        }
        if(assestaskstatus.equals(AssessTaskStatusEnum.WAITTING.getValue()) || assestaskstatus.equals(AssessTaskStatusEnum.TEMPSTORAGE.getValue())){
            this.getView().setVisible(true, "cds0_save", "cds0_savetemp");
        }
        if (assessTasks != null && assessTasks.length > 0) {
            //设置分组
            this.getModel().setValue("cds0_textfield1", assessTasks[0].get("dimgroup.groupdimname"));
            //获取维度id
            Long evalDimId = Long.valueOf(assessTasks[0].get("evaldim.id").toString());
            //设置提示语
            this.getModel().setValue("cds0_basedatafield1", assessActivityId);
            //设置测评表名称
            Long assessFormId = Long.valueOf(assessTasks[0].get("dimgroup.assessform.id").toString());
            this.getModel().setValue("cds0_basedatafield11", assessFormId);
            //根据表名获取表下所有指标
            DynamicObject assessForm = assessFormEntityService.queryOneById(assessFormId);
            DynamicObjectCollection assessFormRows = (DynamicObjectCollection) assessForm.get("entryentity");
            //设置总数
            this.getModel().setValue("cds0_integerfield1", assessFormRows.size());
            //设置绩效等级
            String levelMapNumber = assessFormRows.get(0).getString("levelmap");
            this.getPageCache().put("levelMapNumber", levelMapNumber);
            //设置测评表类型
            boolean isSingleChoice = false;
            if (assessForm.get("assformtype").toString().equals(AssessFormTypeEnum.GRADE.getValue())) {
                isSingleChoice = true;
            }
            if (!isSingleChoice) {
                this.getView().setVisible(false, "cds0_labelap4");
            }
            this.getPageCache().put("isSingleChoice", isSingleChoice + "");
            this.getPageCache().put("dimAssessors", dimAssessorIds.size() + "");
            this.getPageCache().put("assessFormRowNum", assessFormRows.size() + "");
            this.getPageCache().put("dimAssessorList", JSON.toJSONString(dimAssessorIds));
            //通过 绩效等级 总的指标 总的测评对象 show指标测评项
            showGradeExampleForNextOne(assessActivityId,this.getView().getControl("cds0_flexpanelap9"), evalDimId, scoreSystem, assessFormId, levelMapNumber, isSingleChoice, assessFormRows, dimAssessorIds,isShow);
        }
    }

    //根据开始时间结束时间计算周期标识
    public String getCycleForNextOne(Date star, Date end) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(star) + "~" + df.format(end);
    }

    public void showGradeExampleForNextOne(Long assessActivityId,Container container, Long evalDimId, Long scoreSystem, Long assessFormId, String levelMapNumber, boolean isSingleChoice, DynamicObjectCollection assessFormRows, List<Long> dimAssessorIds,Boolean isShow) {
        List<DynamicObject> assessFormRowList = assessFormRows.stream().sorted(new Comparator<DynamicObject>() {
            @Override
            public int compare(DynamicObject entry1, DynamicObject entry2) {
                //获取分组排序
                int seq1 = (int) entry1.get("indicatorindex");
                int seq2 = (int) entry2.get("indicatorindex");
                if (seq1 > seq2) {
                    return 1;
                } else if (seq1 == seq2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }).collect(Collectors.toList());
        int index = 0;
        //show之前清理存子页面pageId的缓存
        this.getPageCache().remove("subpages");
        List<String> subpages = new ArrayList<>();
        String levelMap = null;
        boolean isAgreement = true;
        for (DynamicObject assessFormRow : assessFormRowList) {
            if (levelMap == null) {
                levelMap = assessFormRow.getString("levelmap");
            } else if (!levelMap.equals(assessFormRow.getString("levelmap"))) {
                isAgreement = false;
            }
            if (!assessFormRow.get("indicatortype").equals(AssessFormTypeEnum.GRADE.getValue())) {
                isAgreement = false;
            }
            FlexPanelAp flexPanelAp = ApCreateUtils.createNewFlexAp("gradeExample" + index, "gradeExample" + index);
            flexPanelAp.setParentId("cds0_flexpanelap9");
            flexPanelAp.setWidth(new LocaleString("100%"));
            List<Map<String, Object>> items = new ArrayList<>();
            items.add(flexPanelAp.createControl());
            container.addControls(items);
            FormShowParameter parameter = new FormShowParameter();
            parameter.setFormId("cea_gradeexample");
            parameter.getOpenStyle().setShowType(InContainer);
            parameter.getOpenStyle().setTargetKey("gradeExample" + index++);
            //设置绩效等级 测评表类型 指标id 测评对象ids
            parameter.setCustomParam("evalDimId", evalDimId);
            parameter.setCustomParam("scoreSystem", scoreSystem);
            parameter.setCustomParam("assessFormId", assessFormId);
            parameter.setCustomParam("levelMapNumber", levelMapNumber);
            parameter.setCustomParam("isSingleChoice", isSingleChoice);
            parameter.setCustomParam("assessFormRowId", assessFormRow.get("id"));
            parameter.setCustomParam("dimAssessorIds", dimAssessorIds);
            parameter.setCustomParam("assessActivityId", assessActivityId);
            parameter.setCustomParam("isShow", isShow);
            parameter.setHasRight(Boolean.TRUE);
            this.getView().showForm(parameter);
            subpages.add(parameter.getPageId());
        }
        if (isAgreement) {
            this.getView().setVisible(true, "cds0_labelap4");
        } else {
            this.getView().setVisible(false, "cds0_labelap4");
        }
        this.getPageCache().put("subpages", String.join(",", subpages));
    }





    //根据开始时间结束时间计算周期标识
    public String getCycle(Date star, Date end) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(star) + "~" + df.format(end);
//        LocalDate starDate = star.toLocalDateTime().toLocalDate();
//        LocalDate endDate = end.toLocalDateTime().toLocalDate();
//        long difference = ChronoUnit.MONTHS.between(starDate, endDate);
//        if (endDate.getYear() - star.getYear() > 0) {
//            return starDate.getYear() + "Y";
//        }
//        if (difference >= 12) {
//            return starDate.getYear() + "Y";
//        } else if (difference < 12 && difference > 4) {
//            if (starDate.getMonth().getValue() <= 6) {
//                return starDate.getYear() + "H1";
//            } else {
//                return starDate.getYear() + "H2";
//            }
//        } else if (difference <= 4 && difference > 1) {
//            return starDate.getYear() + "Q";
//        } else {
//            return starDate.getYear() + "M";
//        }
    }

    public void showGradeExample(Long assessActivityId, Container container, String scoreSystemMap, Long evalDimId, Long scoreSystem, Long assessFormId, DynamicObjectCollection assessFormRows, List<Long> dimAssessorIds) {
        //指标排序
        List<DynamicObject> assessFormRowList = assessFormRows.stream().sorted(new Comparator<DynamicObject>() {
            @Override
            public int compare(DynamicObject entry1, DynamicObject entry2) {
                //获取分组排序
                int seq1 = entry1.getInt("indicatorindex");
                int seq2 = entry2.getInt("indicatorindex");
                if (seq1 > seq2) {
                    return 1;
                } else if (seq1 == seq2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }).collect(Collectors.toList());
        int index = 0;
        //show之前清理存子页面pageId的缓存
        this.getPageCache().remove("subpages");
        List<String> subpages = new ArrayList<>();
        String levelMap = null;
        boolean isAgreement = true;
        //遍历show出指标元数据
        for (DynamicObject assessFormRow : assessFormRowList) {
            if (levelMap == null) {
                levelMap = assessFormRow.getString("levelmap");
            } else if (!HRStringUtils.equals(levelMap, assessFormRow.getString("levelmap"))) {
                isAgreement = false;
            }
            if (!HRStringUtils.equals(assessFormRow.getString("indicatortype"), AssessFormTypeEnum.GRADE.getValue())) {
                isAgreement = false;
            }
            FlexPanelAp flexPanelAp = ApCreateUtils.createNewFlexAp("gradeExample" + index, "gradeExample" + index);
            flexPanelAp.setParentId("cds0_flexpanelap9");
            flexPanelAp.setWidth(new LocaleString("100%"));
            List<Map<String, Object>> items = new ArrayList<>();
            items.add(flexPanelAp.createControl());
            container.addControls(items);
            FormShowParameter parameter = new FormShowParameter();
            parameter.setFormId("cea_gradeexample");
            parameter.getOpenStyle().setShowType(InContainer);
            parameter.getOpenStyle().setTargetKey("gradeExample" + index++);
            //设置绩效等级 测评表类型 指标id 测评对象ids
            parameter.setCustomParam("evalDimId", evalDimId);
            parameter.setCustomParam("scoreSystemMap", scoreSystemMap);
            parameter.setCustomParam("scoreSystem", scoreSystem);
            parameter.setCustomParam("assessFormId", assessFormId);
            parameter.setCustomParam("assessFormRowId", assessFormRow.get("id"));
            parameter.setCustomParam("dimAssessorIds", dimAssessorIds);
            parameter.setCustomParam("assessActivityId", assessActivityId);
            parameter.setHasRight(Boolean.TRUE);
            this.getView().showForm(parameter);
            subpages.add(parameter.getPageId());
        }
        if (isAgreement) {
            this.getView().setVisible(true, "cds0_labelap4");
        } else {
            this.getView().setVisible(false, "cds0_labelap4");
        }
        this.getPageCache().put("subpages", String.join(",", subpages));
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("cds0_save", "cds0_savetemp");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control)evt.getSource();
        //监听保存按钮，点击后根据缓存的子页面pageId，改变子页面保存字段数据
        if (source.getKey().equals("cds0_save")){
            String subpagesStr = this.getPageCache().get("subpages");
            if (subpagesStr != null && subpagesStr != "") {
                List<String> subpages = Arrays.asList(subpagesStr.split(","));
                for (String subpage : subpages) {
                    IFormView subView = this.getView().getView(subpage);
                    subView.getModel().setValue("cds0_save", new Date().getTime());
                    this.getView().sendFormAction(subView);
                }
            }
        } else if (source.getKey().equals("cds0_savetemp")) {
            //监听暂存按钮，点击后根据缓存的子页面pageId，改变子页面暂存字段数据
            String subpagesStr = this.getPageCache().get("subpages");
            if (subpagesStr != null && subpagesStr != "") {
                List<String> subpages = Arrays.asList(subpagesStr.split(","));
                for (String subpage : subpages) {
                    IFormView subView = this.getView().getView(subpage);
                    subView.getModel().setValue("cds0_savetemp", new Date().getTime());
                    this.getView().sendFormAction(subView);
                }
            }
        }
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        FormShowParameter parameter = this.getView().getFormShowParameter();
        Long assessActivityId = parameter.getCustomParam("assessActivityId");
        Long userId = UserServiceHelper.getCurrentUserId();
        MutexHelper.release("epa_actevalobj", "nodeview", assessActivityId.toString()+userId);
        //查询当前测评活动中当前测评人的所有测评任务
        List<Long> dimAssesserIds = new ArrayList<>();
        List<DynamicObject> assesserTasks =Arrays.stream(assessTaskEntityService.queryAssessTaskByActivityAndAssesser(assessActivityId, userId)).collect(Collectors.toList());
        assesserTasks.stream().collect(Collectors.toList()).forEach(obj -> dimAssesserIds.add(obj.getLong("dimassesser.id")));
        DynamicObject[] dimAssessers = dimassesserEntityService.querydatabyIds(dimAssesserIds,DimAssesserStatusEnum.FILLING.getValue());
        Arrays.stream(dimAssessers).collect(Collectors.toList()).forEach(dimAssesser -> dimAssesser.set("assesstatus", DimAssesserStatusEnum.UNFILLED.getValue()));
        dimassesserEntityService.update(dimAssessers);
        e.setCheckDataChange(false);
        this.getModel().setDataChanged(false);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        String subpagesStr = this.getPageCache().get("subpages");
        int assessFormRowNum = Integer.parseInt(this.getPageCache().get("assessFormRowNum"));
        String propertyName = e.getProperty().getName();
        if (HRStringUtils.equals(propertyName, "cds0_issuccess")) {
            //监听保存成功字段
            for (ChangeData changeData: e.getChangeSet()) {
                String value = changeData.getOldValue() + "," + changeData.getNewValue().toString().split("_")[0];
                //如果每个子页面都保存成功，则从缓存获取数据，进行事务处理
                if (value.contains(subpagesStr)) {
                    IPageCache iPageCache = this.getPageCache();
                    int dimAssessorNum = Integer.valueOf(iPageCache.get("dimAssessors"));
                    boolean isSingleChoice = Boolean.valueOf(iPageCache.get("isSingleChoice"));
                    boolean checkOneObjectValueFail = false;
                    if (isSingleChoice) {
                        boolean checkOneIndexValueFail = Boolean.valueOf(iPageCache.get("checkOneIndexValueFail"));
                        if(dimAssessorNum == 1) {
                            checkOneIndexValueFail = false;
                        }
                        if (checkOneIndexValueFail == true) {
                            checkSubmitValue(iPageCache, ResManager.loadKDString("不同评估对象同一指标不能全部最高","GradeDetailsPlugin_0", AppflgConstant.KEY_APP_NAME));
                            return;
                        }
                        HashMap<String, Integer> userScoreMap = JSON.parseObject(this.getPageCache().get("userScoreMap"), new TypeReference<HashMap<String, Integer>>() {});
                        for (Map.Entry<String,Integer> entry: userScoreMap.entrySet()) {
                            if (entry.getValue() >= assessFormRowNum) {
                                checkOneObjectValueFail = true;
                            }
                        }
                        if (checkOneObjectValueFail == true) {
                            checkSubmitValue(iPageCache, ResManager.loadKDString("同一评估对象所有指标不能全部最高","GradeDetailsPlugin_1", AppflgConstant.KEY_APP_NAME));
                            return;
                        }
                    }
                    //更新所有实例
                    List<AssObjScoItemInstEntityBO> itemInstList = JSON.parseObject(iPageCache.get("itemInstList"), new TypeReference<List<AssObjScoItemInstEntityBO>>() {});
                    Map<Long,DynamicObject> itemInstObjectMap = Arrays.stream(assObjScoItemInstEntityService.loadDynamicObjectArray(itemInstList.stream().map(k->k.getId()).collect(Collectors.toList()).toArray())).collect(Collectors.toMap(k -> k.getLong("id"), v->v));
                    List<DynamicObject> itemInstObjectList = new ArrayList<>();
                    itemInstList.stream().forEach(item -> {
                        DynamicObject dynamicObject = itemInstObjectMap.get(item.getId());
                        dynamicObject.set("scorevalue", item.getScoreValue());
                        dynamicObject.set("levelentry", item.getLevelEntry());
                        dynamicObject.set("levelscore", item.getLevelScore());
                        dynamicObject.set("mapscore", item.getMapScore());
                        itemInstObjectList.add(dynamicObject);
                    });

                    //任务状态改为已处理
                    List<AssessTaskEntityBO> assessTaskList = JSON.parseObject(iPageCache.get("assessTaskList"), new TypeReference<List<AssessTaskEntityBO>>() {});
                    Map<Long,DynamicObject> assessTaskObjectMap = Arrays.stream(assessTaskEntityService.loadDynamicObjectArray(assessTaskList.stream().map(k->k.getId()).collect(Collectors.toList()).toArray())).collect(Collectors.toMap(k -> k.getLong("id"), v->v));
                    List<DynamicObject> assessTaskObjectList = new ArrayList<>();
                    assessTaskList.stream().forEach(item -> {
                        DynamicObject dynamicObject = assessTaskObjectMap.get(item.getId());
                        dynamicObject.set("assestaskstatus", item.getAssesTaskStatus());
                        assessTaskObjectList.add(dynamicObject);
                    });

                    //维度测评人状态改为已填报
                    List<Long> dimAssessorList = JSON.parseArray(iPageCache.get("dimAssessorList"), Long.class);
                    Map<Long,DynamicObject> dimAssessorObjectMap = Arrays.stream(dimassesserEntityService.loadDynamicObjectArray(dimAssessorList.toArray())).collect(Collectors.toMap(k -> k.getLong("id"), v->v));
                    List<DynamicObject> dimAssessorObjectList = new ArrayList<>();
                    dimAssessorList.stream().forEach(item -> {
                        DynamicObject dynamicObject = dimAssessorObjectMap.get(item);
                        dynamicObject.set("assesstatus", DimAssesserStatusEnum.FILLED.getValue());
                        dimAssessorObjectList.add(dynamicObject);
                    });

                    //获取全测评对象和维度调用
                    Set<Long> assessObjectIdList = JSON.parseObject(iPageCache.get("assessObjectIdList"), new TypeReference<HashSet<Long>>() {});
                    TXHandle required = TX.required();
                    try {
                        assObjScoItemInstEntityService.update(itemInstObjectList.toArray(new DynamicObject[0]));
                        assessTaskEntityService.update(assessTaskObjectList.toArray(new DynamicObject[0]));
                        dimassesserEntityService.update(dimAssessorObjectList.toArray(new DynamicObject[0]));
                        assessTaskDomainService.assembleAssessObjStatuSubmit(assessObjectIdList.stream().collect(Collectors.toList()), Long.valueOf(iPageCache.get("evalDimId")));
                    } catch (Exception exception) {
                        LOG.error("startupAssessObject 1 fail:", e);
                        required.markRollback();
                    } finally {
                        required.close();
                    }

                    iPageCache.remove("itemInstList");
                    iPageCache.remove("assessTaskList");
                    iPageCache.remove("dimAssessorList");
                    iPageCache.remove("assessObjectIdList");
                    iPageCache.remove("evalDimId");
                    iPageCache.remove("checkOneIndexValueFail");
                    iPageCache.remove("userScoreMap");

                    this.getView().showSuccessNotification(ResManager.loadKDString("提交成功！","SubmitSuccess_0", AppflgConstant.KEY_APP_NAME));
                    this.getModel().initValue("cds0_issuccess", null);
                    this.getView().updateView("cds0_issuccess");
                    //隐藏提交 暂存 一键评分
                    this.getView().setVisible(false, "cds0_save", "cds0_savetemp", "cds0_labelap4");
                    List<String> subpages = Arrays.asList(subpagesStr.split(","));
                    subpages.forEach(subpage -> {
                        IFormView subView = this.getView().getView(subpage);
                        subView.getModel().setValue("cds0_savesuccess", "1");
                        this.getView().sendFormAction(subView);
                    });
                } else {
                    this.getModel().initValue("cds0_issuccess", value);
                    this.getView().updateView("cds0_issuccess");
                }
            }
        } else if (HRStringUtils.equals(propertyName, "cds0_textfield4")) {
            //监听进度字段，每个子页面加载成功后，计算一遍进度
            int isCompletedNum = 0;
            if (this.getPageCache().get("subpages") != null && this.getPageCache().get("subpages") != "") {
                for (String subpage : this.getPageCache().get("subpages").split(",")) {
                    IFormView subView = this.getView().getView(subpage);
                    boolean isCompleted = Boolean.valueOf(subView.getPageCache().get(subpage + "isCompleted"));
                    if (isCompleted) {
                        isCompletedNum++;
                    }
                }
                this.getModel().setValue("cds0_integerfield", isCompletedNum);
            }
        }
    }


    private void checkSubmitValue(IPageCache iPageCache, String msg) {
        iPageCache.remove("itemInstList");
        iPageCache.remove("assessTaskList");
        iPageCache.remove("dimAssessorList");
        iPageCache.remove("assessObjectIdList");
        iPageCache.remove("evalDimId");
        iPageCache.remove("checkOneIndexValueFail");
        iPageCache.remove("userScoreMap");
        this.getView().showTipNotification(msg);
        this.getModel().initValue("cds0_issuccess", null);
        this.getView().updateView("cds0_issuccess");
    }
}
