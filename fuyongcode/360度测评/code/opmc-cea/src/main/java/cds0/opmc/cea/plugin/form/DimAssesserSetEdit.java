package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.bo.DimassesserBO;
import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.business.entityservice.AssessTaskEntityService;
import cds0.opmc.cea.business.entityservice.DimSettingEntityService;
import cds0.opmc.cea.business.entityservice.EvalDimSettingEntityService;
import cds0.opmc.cea.business.service.DimassesserDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.AssessManageUtils;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import cds0.opmc.cea.common.enums.DimAssesserStatusEnum;
import com.alibaba.druid.util.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.control.*;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseEdit;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.*;

/*
 * 单据插件
 */
public class DimAssesserSetEdit extends HRDataBaseEdit {

    private static final Log log = LogFactory.getLog(DimAssesserSetEdit.class);
    //分录
    private static final String KEY_ENTRYENTITY = "entryentity";
    //取消按钮
    private static final String KEY_CANCEL = "cancel";
    //确认按钮
    private static final String KEY_CONFIRM = "confirm";
    //调整测评人按钮
    private static final String KEY_ASSESSERSET = "assesserset";
    //新增测评人按钮
    private static final String KEY_ASSESSERADD = "assesseradd";
    //修改测评人按钮
    private static final String KEY_ASSESSERUPDATE = "modifyassesser";
    //删除测评人按钮
    private static final String KEY_ASSESSERDEL = "assesserdel";
    //进入设置维度测评人页面的入口
    private static final String KEY_BUTTON = "buttonkey";
    private static final String KEY_ASSESSERTASKIDS = "assessertaskids";
    //分录操作框
    private final String KEY_ADVCONTOOLVARAP = "advcontoolbarap";
    //工具栏
    private final String KEY_CONFIRMCANCELLBUTTON = "confirmcancellbutton";
    //当前测评对象的所有测评任务是否都已完成
    private final String KEY_FINISHASSESSTASK = "finishassesstask";
    private static final DimassesserDomainService dimassesserDomainService = DimassesserDomainService.getInstance();
    private static final AssessTaskEntityService assessTaskEntityService = AssessTaskEntityService.getInstance();
    private static final DimSettingEntityService dimSettingEntityService = DimSettingEntityService.getInstance();
    private static final AssessObjEntityService assessObjEntityService = AssessObjEntityService.getInstance();
    private static final EvalDimSettingEntityService EVAL_DIM_SETTING_ENTITY_SERVICE = EvalDimSettingEntityService.getInstance();

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 工具栏监听
        Toolbar scoreToolbar = this.getView().getControl(KEY_ADVCONTOOLVARAP);
        Toolbar scoreToolbar2 = this.getView().getControl(KEY_CONFIRMCANCELLBUTTON);
        scoreToolbar2.addItemClickListener(this);
        scoreToolbar.addItemClickListener(this);
    }

    /*
     * 初始化页面
     */
    @Override
    public void afterCreateNewData(EventObject e) {
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        Object buttonKey = customParams.get(KEY_BUTTON);
        List<Long> assesserTaskIds = (List) customParams.get(KEY_ASSESSERTASKIDS);
        List<Long> assesserObjIds = (List) customParams.get(KEY_ASSESSEROBJIDS);

        switch (String.valueOf(buttonKey)) {
            //添加、调整、删除测评人，从 测评人进度列表 页面进入
            case KEY_ASSESSERSET:
                findAssesserInfoByAssessTaskId(assesserTaskIds);
                break;
            //从 ”待启动“测评对象列表、”测评中“测评对象列表 页面进入
            case KEY_ASSESSERUPDATE:
                findAssesserInfoByAssessObjId(assesserObjIds);
                break;
        }
    }

    /*
     * 通过测评任务id查询当前测评对象所有的维度测评人信息
     */
    public void findAssesserInfoByAssessTaskId(List<Long> assesserTaskIds) {
        DynamicObject[] assessTasks = assessTaskEntityService.loadDynamicObjectArray(assesserTaskIds.toArray());
        List<Long> assessObjIds = Arrays.stream(assessTasks).map(e -> e.getLong("assessobj.id")).distinct().collect(Collectors.toList());
        findAssesserInfoByAssessObjId(assessObjIds);
    }

    /*
     * 根据测评对象id查询测对象维度相关信息
     */
    public void findAssesserInfoByAssessObjId(List<Long> assesserObjIds) {
        List<Map<String, Object>> showFieldList = new ArrayList<>();
        //获取当前测评对象的的测评维度信息
        DynamicObject[] assesserObjs = assessObjEntityService.queryAssesserInfoByIds(assesserObjIds);
        Map<Long, DynamicObject> groupDimMap = Arrays.stream(dimSettingEntityService.queryDimSettingByIds(Arrays.stream(assesserObjs).map(assobj -> assobj.getLong("dimgroup.id")).collect(Collectors.toSet()).stream().collect(Collectors.toList()))).collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
        Arrays.stream(assesserObjs).forEach(assessObj -> {
            assessObj.getDynamicObjectCollection(ENTRYENTITY).stream().forEach(dimasser -> {
                Map<String, Object> showField = new HashMap<>();
                showField.put("assessobjbaseinfo", assessObj.getLong("id"));
                showField.put("evaldimbaseinfo", dimasser.getLong("evaldim.id"));
                showField.put("assesserbaseinfo", dimasser.getLong("assesser.id"));
                //判断维度权重是自动分配还自设  1--自动分配  2--自设比例
                showField.put("automaticmanual", groupDimMap.get(assessObj.getLong("dimgroup.id")).getString("dimension"));
                showField.put("dimweight", dimasser.getBigDecimal("evaldim.dimweight"));
                showField.put("assesserdimweight", dimasser.getBigDecimal("dimweight"));
                //已填报的评分的维度测评人不可更改
                if (DimAssesserStatusEnum.FILLED.getValue().equals(dimasser.getString("assesstatus"))) {
                    showField.put("assessercanset", "2");
                } else {
                    showField.put("assessercanset", "1");
                }
                switch (dimasser.getString("assesstatus")) {
                    //设置维度测评人状态  10-未填报 、 20-填报中 、 30-已填报
                    case "10":
                        showField.put("dimassesstatus", DimAssesserStatusEnum.UNFILLED.getName());
                        break;
                    case "20":
                        showField.put("dimassesstatus", DimAssesserStatusEnum.FILLING.getName());
                        break;
                    case "30":
                        showField.put("dimassesstatus", DimAssesserStatusEnum.FILLED.getName());
                        break;
                }
                showFieldList.add(showField);
            });
        });

        if (showFieldList.size() > 0) {
            getModel().batchCreateNewEntryRow(KEY_ENTRYENTITY, showFieldList.size());
        }
        //将展示的维度测评人列表进行排序，与测评中页面保持一致
        showFieldList.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                //先根据测评对象升序排序
                int assesserObjName = Long.valueOf(o1.get("assessobjbaseinfo").toString()).compareTo(Long.valueOf(o2.get("assessobjbaseinfo").toString()));
                //接着根据维度顺序升序排序
                int dimIndex = Long.valueOf(o1.get("evaldimbaseinfo").toString()).compareTo(Long.valueOf(o2.get("evaldimbaseinfo").toString()));
                //再根据维度测评人升序排序
                int assesser = Long.valueOf(o1.get("assesserbaseinfo").toString()).compareTo(Long.valueOf(o2.get("assesserbaseinfo").toString()));

                if (assesserObjName == 0) {
                    if (dimIndex == 0) {
                        return assesser;
                    } else {
                        return dimIndex;
                    }
                } else {
                    return assesserObjName;
                }
            }
        });
        //设置维度测评人列表数据
        AtomicInteger i = new AtomicInteger();
        showFieldList.stream().forEach(showField -> {
            this.getModel().setValue("assessobjbaseinfo", showField.get("assessobjbaseinfo"), i.get());
            this.getModel().setValue("evaldimbaseinfo", showField.get("evaldimbaseinfo"), i.get());
            this.getModel().setValue("assesserbaseinfo", showField.get("assesserbaseinfo"), i.get());
            this.getModel().setValue("automaticmanual", showField.get("automaticmanual"), i.get());
            this.getModel().setValue("dimweight", showField.get("dimweight"), i.get());
            this.getModel().setValue("assesserdimweight", showField.get("assesserdimweight"), i.get());
            this.getModel().setValue("assessercanset", showField.get("assessercanset"), i.get());
            this.getModel().setValue("dimassesstatus", showField.get("dimassesstatus"), i.get());
            i.getAndIncrement();
        });
        // 刷新分录缓存
        this.getModel().updateEntryCache(getModel().getEntryEntity(KEY_ENTRYENTITY));
        // 更新分录
        this.getView().updateView(KEY_ENTRYENTITY);
    }

    /**
     * 添加测评人
     */
    private void addNewAssesser() {
        EntryGrid entryGrid = this.getControl(ENTRYENTITY);
        DynamicObjectCollection entity = this.getModel().getEntryEntity(ENTRYENTITY);
        int[] selectRows = entryGrid.getSelectRows();
        List<DynamicObject> addRows = new ArrayList<>();
        Arrays.stream(selectRows).forEach(row -> addRows.add(entity.get(row)));

        for (DynamicObject addRow : addRows) {
            this.getModel().createNewEntryRow(ENTRYENTITY);
            int newRowIndex = this.getModel().getEntryEntity(ENTRYENTITY).size() - 1;
            this.getModel().setValue("assessobjbaseinfo", addRow.getLong("assessobjbaseinfo.id"), newRowIndex);
            this.getModel().setValue("evaldimbaseinfo", addRow.getLong("evaldimbaseinfo.id"), newRowIndex);
            this.getModel().setValue("automaticmanual", addRow.getString("automaticmanual"), newRowIndex);
            this.getModel().setValue("dimweight", addRow.getBigDecimal("dimweight"), newRowIndex);
            this.getModel().setValue("assesserdimweight", addRow.getBigDecimal("assesserdimweight"), newRowIndex);
            // 重新计算测评人权重（自设比例需要重新计算测评人权重）
            reCalulateAssesserWeigth(addRow);
        }
    }

    /**
     * 删除维度测评人
     */
    public void delOldAssesser() {
        EntryGrid entryGrid = this.getControl(ENTRYENTITY);
        DynamicObjectCollection entity = this.getModel().getEntryEntity(ENTRYENTITY);
        int[] selectRows = entryGrid.getSelectRows();
        List<DynamicObject> delRows = new ArrayList<>();
        List<Long> delRowsId = new ArrayList<>();
        Arrays.stream(selectRows).forEach(row -> {
            delRows.add(entity.get(row));
            delRowsId.add(entity.get(row).getLong("seq"));
        });
        //校验维度测评人是否可以删除
        for (DynamicObject delRow : delRows) {
            if (DimAssesserStatusEnum.FILLING.getName().equals(delRow.getString("dimassesstatus"))
                    || DimAssesserStatusEnum.FILLED.getName().equals(delRow.getString("dimassesstatus"))) {
                getView().showTipNotification(ResManager.loadKDString("不支持删除已填报/填报中 测评人", "DimAssesserSetEdit_6", KEY_APP_NAME));
                return;
            }
        }
        //每个维度请至少保留一行测评人数据
        // 先按测评对象分组
        Map<Long, List<DynamicObject>> assObjMap = entity.stream().collect(Collectors.groupingBy(k -> k.getLong("assessobjbaseinfo.id"), Collectors.toList()));
        for (Map.Entry<Long, List<DynamicObject>> entry : assObjMap.entrySet()) {
            //再按维度分组 删除前
            int beforeDelSize = entry.getValue().stream().collect(Collectors.groupingBy(k -> k.getLong("evaldimbaseinfo.id"), Collectors.toList())).size();
            int afterDelSize = entry.getValue().stream().filter(assessObj -> !delRowsId.contains(assessObj.getLong("seq"))).collect(Collectors.groupingBy(k -> k.getLong("evaldimbaseinfo.id"), Collectors.toList())).size();
            if (beforeDelSize != afterDelSize) {
                getView().showTipNotification(ResManager.loadKDString("每个维度请至少保留一行测评人", "DimAssesserSetEdit_5", KEY_APP_NAME));
                return;
            }
        }
        //删除选中行数据
        this.getModel().deleteEntryRows(ENTRYENTITY, selectRows);

        delRows.stream().forEach(delRow -> {
            reCalulateAssesserWeigth(delRow);
        });
    }

    /**
     * 重新计算测评人权重
     *
     * @param rawObj
     */
    private void reCalulateAssesserWeigth(DynamicObject rawObj) {
        DynamicObjectCollection entity = this.getModel().getEntryEntity(ENTRYENTITY);
        if (entity != null && !entity.isEmpty()) {
            // 先按测评对象分组
            Map<Long, List<DynamicObject>> assObjMap = entity.stream().collect(Collectors.groupingBy(k -> k.getLong("assessobjbaseinfo.id"), Collectors.toList()));
            if (HRStringUtils.equals(HRBaseConstants.STR_ONE, rawObj.getString("automaticmanual"))) {
                // -----  重新计算测评人权重 ------
                // 获取新增的那条数据对应的维度测评人数据,再按维度分组
                List<DynamicObject> dynamicObjectList = assObjMap.get(rawObj.getLong("assessobjbaseinfo.id"));
                if (dynamicObjectList != null && !dynamicObjectList.isEmpty()) {
                    Map<Long, List<DynamicObject>> evalDimMap = dynamicObjectList.stream().collect(Collectors.groupingBy(k -> k.getLong("evaldimbaseinfo.id"), Collectors.toList()));
                    // 获取当前维度的所有测评人数据，重新计算平均权重
                    List<DynamicObject> currentEvalDimAssers = evalDimMap.get(rawObj.getLong("evaldimbaseinfo.id"));
                    Double avg = Math.floor(rawObj.getBigDecimal("dimweight").doubleValue() / currentEvalDimAssers.size()); // 向下取整
                    Double modval = rawObj.getBigDecimal("dimweight").doubleValue() % currentEvalDimAssers.size();
                    currentEvalDimAssers.stream().forEach(asser -> {
                        asser.set("assesserdimweight", avg);
                    });
                    currentEvalDimAssers.get(HRBaseConstants.INT_ZERO).set("assesserdimweight", avg + modval);
                }
            }
        }
        // 刷新分录缓存
        this.getModel().updateEntryCache(entity);
        // 更新分录
        this.getView().updateView(KEY_ENTRYENTITY);
    }

    @Override
    public void confirmCallBack(MessageBoxClosedEvent evt) {
        switch (evt.getCallBackId()) {
            case KEY_FINISHASSESSTASK:
                if (MessageBoxResult.Yes.equals(evt.getResult())) {
                    List<Long> assessObjIds = new ArrayList<>();
                    List<DimassesserBO> dimassesserBOS = verifyWeightAndAssembleData(new ArrayList<String>(), new ArrayList<String>());
                    // 校验通过，允许调整测评人
                    dimassesserDomainService.adjustDimAssesser(dimassesserBOS, this.getView().getFormShowParameter().getCustomParam("fromWhere"));
                    //将测评对象状态改为已完成
                    dimassesserBOS.forEach(dimassesserBO -> {
                        assessObjIds.add(dimassesserBO.getAssessObjId());
                    });
                    DynamicObject[] assesserInfoByIds = assessObjEntityService.queryAssesserInfoByIds(assessObjIds);
                    Arrays.stream(assesserInfoByIds).forEach(assessObj -> {
                        assessObj.set("assesstaus", AssessStatusEnum.PROCESSED.getValue());
                        // 反写测评对象结束时间
                        assessObj.set("overtime", new Date());
                        // 计算测评对象综合得分反写到测评对象上
                        assessObj.set("calscore", AssessManageUtils.compositeScore(assessObj.getLong(AppflgConstant.ID)));
                        assessObj.set("modscore", AssessManageUtils.compositeScore(assessObj.getLong(AppflgConstant.ID)));
                    });
                    assessObjEntityService.save(assesserInfoByIds);
                    this.getView().close();
                    return;
                }
                break;
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();

        //确认按钮
        if (StringUtils.equals(KEY_CONFIRM, itemKey)) {
            // 权重校验信息
            List<String> dimWeightValidMessages = new ArrayList<String>();
            // 测评人校验信息
            List<String> asserValidMessages = new ArrayList<String>();
            Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
            Object buttonKey = customParams.get(KEY_BUTTON);
            // 校验并组装数据
            List<DimassesserBO> dimassesserBOS = verifyWeightAndAssembleData(dimWeightValidMessages, asserValidMessages);
            if (dimWeightValidMessages.isEmpty() && asserValidMessages.isEmpty()) {

                List<Long> asseserObjIds = new ArrayList<>();
                dimassesserBOS.forEach(dimassesserBO -> {
                    asseserObjIds.add(dimassesserBO.getAssessObjId());
                });
                DynamicObject[] assesserInfoByIds = assessObjEntityService.queryAssesserInfoByIds(asseserObjIds);
                String comformMsg = "";
                for (DynamicObject assesserInfoById : assesserInfoByIds) {
                    if (comformMsg.isEmpty()) {
                        comformMsg += assesserInfoById.getString("perffile.name");
                    } else {
                        comformMsg += "、" + assesserInfoById.getString("perffile.name");
                    }
                }

                List<DynamicObject> dimAssesserInfos = getEntryEntityDynamicObject();
                long noFilledCount = dimAssesserInfos.stream()
                        .filter(dimAssesserInfo -> !DimAssesserStatusEnum.FILLED.getName().equals(dimAssesserInfo.getString("dimassesstatus"))).count();
                if (noFilledCount == 0) {
                    this.getView().showConfirm(ResManager.LoadKDString("确认后\r " + comformMsg + " 将完成测评，请确认。", "DimAssesserSetEdit_7"),
                            MessageBoxOptions.OKCancel, new ConfirmCallBackListener(KEY_FINISHASSESSTASK, this));
                    return;
                }
                // 校验通过，允许调整测评人
                dimassesserDomainService.adjustDimAssesser(dimassesserBOS, this.getView().getFormShowParameter().getCustomParam("fromWhere"));
                this.getView().close();
            } else {
                String errmsg = "";
                for (String dimWeightValidMessage : dimWeightValidMessages) {
                    errmsg += dimWeightValidMessage + "\r\n";
                }
                for (String asserValidMessage : asserValidMessages) {
                    errmsg += asserValidMessage + "\r\n";
                }
                // 校验不通过，弹窗提示框
                this.getView().showMessage(ResManager.loadKDString(errmsg, "DimAssesserSetEdit_2", AppflgConstant.KEY_APP_NAME));
                return;
            }
        }
        if (StringUtils.equals(KEY_CANCEL, itemKey)) {
            log.info("cancel button is trigger");
        }
        if (StringUtils.equals(KEY_ASSESSERADD, itemKey)) {
            // 添加维度测评人
            EntryGrid entryGrid = this.getControl(ENTRYENTITY);
            if (entryGrid.getSelectRows().length < HRBaseConstants.INT_ONE) {
                this.getView().showTipNotification(ResManager.loadKDString("请选择至少一条数据添加", "DimAssesserSetEdit_3", AppflgConstant.KEY_APP_NAME));
                return;
            } else {
                // 添加测评人
                addNewAssesser();
            }
        }

        if (StringUtils.equals(KEY_ASSESSERDEL, itemKey)) {
            // 删除维度测评人
            EntryGrid entryGrid = this.getControl(ENTRYENTITY);
            if (entryGrid.getSelectRows().length < HRBaseConstants.INT_ONE) {
                this.getView().showTipNotification(ResManager.loadKDString("请选择至少一条数据删除", "DimAssesserSetEdit_4", AppflgConstant.KEY_APP_NAME));
                return;
            } else {
                //删除维度测评人
                delOldAssesser();
            }
        }
    }

    /**
     * 校验并组装数据
     *
     * @return
     */
    public List<DimassesserBO> verifyWeightAndAssembleData(List<String> dimWeightValidMessages, List<String> asserValidMessages) {
        List<DimassesserBO> dimassesserBOList = new ArrayList<>();
        // 获取表格行数据
        List<DynamicObject> entryEntityDynamicObject = getEntryEntityDynamicObject();
        Map<Long, DynamicObject> assessObjMap = Arrays.stream(assessObjEntityService.queryAssessObjByPks(entryEntityDynamicObject.stream().map(e -> e.getLong("assessobjbaseinfo.id")).distinct().collect(Collectors.toList()))).collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
        Map<Long, DynamicObject> evalDimMap = Arrays.stream(EVAL_DIM_SETTING_ENTITY_SERVICE.loadDynamicObjectArray(entryEntityDynamicObject.stream().map(e -> e.getLong("evaldimbaseinfo.id")).distinct().collect(Collectors.toList()).toArray())).collect(Collectors.toMap(k -> k.getLong(ID), v -> v));
        //根据测评对象分组
        Map<Long, List<DynamicObject>> assessObjIdListMap = entryEntityDynamicObject.stream().collect(Collectors.groupingBy(k -> k.getLong("assessobjbaseinfo.id"), Collectors.toList()));
        assessObjIdListMap.entrySet().forEach(entry -> {
            DimassesserBO dimassesserBO = new DimassesserBO();
            Map<Long, List<Map<Long, BigDecimal>>> evalDimAssesserDimweight = new HashMap<>();
            // 按维度分组
            Map<Long, List<DynamicObject>> currEvalDimMap = entry.getValue().stream().collect(Collectors.groupingBy(k -> k.getLong("evaldimbaseinfo.id"), Collectors.toList()));
            currEvalDimMap.entrySet().forEach(evalDimEntry -> {
                List<Map<Long, BigDecimal>> evalDimAsserList = new ArrayList<>();
                // 判断当前维度的维度测评人权重之和是否不等于维度权重
                if (evalDimEntry.getValue().stream().mapToDouble(dimasser -> dimasser.getBigDecimal("assesserdimweight").doubleValue()).sum() != evalDimEntry.getValue().stream().map(e -> e.getBigDecimal("dimweight")).collect(Collectors.toSet()).iterator().next().doubleValue()) {
                    dimWeightValidMessages.add(ResManager.loadKDString("测评对象:{0},维度:{1}下的维度测评人权重之和不等于该维度的权重。", "DimAssesserSetEdit_0", AppflgConstant.KEY_APP_NAME, assessObjMap.get(entry.getKey()).getString("perffile.name"), evalDimMap.get(evalDimEntry.getKey()).getString("dimname")));
                }
                // 同一个维度可能存在设置相同多个测评人  -- 空测评人可重复
                List<DynamicObject> collect = evalDimEntry.getValue().stream().filter(e -> e.getLong("assesserbaseinfo.id") != 0).collect(Collectors.toList());
                if (collect.stream().map(e -> e.getLong("assesserbaseinfo.id")).collect(Collectors.toSet()).size() != collect.size()) {
                    asserValidMessages.add(ResManager.loadKDString("测评对象:{0},维度:{1}下存在相同的测评人", "DimAssesserSetEdit_1", AppflgConstant.KEY_APP_NAME, assessObjMap.get(entry.getKey()).getString("perffile.name"), evalDimMap.get(evalDimEntry.getKey()).getString("dimname")));
                }
                // 组装当前维度下的测评人数据
                evalDimEntry.getValue().stream().forEach(dimasser -> {
                    Map<Long, BigDecimal> asser = new HashMap<>();
                    asser.put(dimasser.getLong("assesserbaseinfo.id"), dimasser.getBigDecimal("assesserdimweight"));
                    evalDimAsserList.add(asser);
                });
                evalDimAssesserDimweight.put(evalDimEntry.getKey(), evalDimAsserList);
            });
            // 组装当前测评对象 维度测评人数据
            dimassesserBO.setAssessObjId(entry.getKey());
            dimassesserBO.setEvalDimAssesserDimweight(evalDimAssesserDimweight);
            dimassesserBOList.add(dimassesserBO);
        });
        return dimassesserBOList;
    }

    /*
    获取表格数据
     */
    public List<DynamicObject> getEntryEntityDynamicObject() {
        //获取表格
        EntryGrid gradeinformation = this.getControl(KEY_ENTRYENTITY);
        EntryData entryData = gradeinformation.getEntryData();
        //获取表格数据
        DynamicObject[] dataEntitys = entryData.getDataEntitys();
        return Arrays.asList(dataEntitys);
    }
}
