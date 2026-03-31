package dgdl.odc.homs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.BeforeImportDataEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.exception.KDBizException;
import kd.bos.form.ClientProperties;
import kd.bos.form.IFormView;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.fi.ap.util.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Description:月度编制信息管理编制明细 表单插件
 * @Author: zhangjun
 * @Since: 2024/3/25
 **/
public class PlanmonthBillBzFormPlugin extends AbstractFormPlugin {


    private static Log logger = LogFactory.getLog(PlanmonthBillBzFormPlugin.class);


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("new");
    }


    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //当前页面
        IDataModel model = this.getModel();
        //获取父页面
        IFormView parentView = this.getView().getParentView().getParentView();
        //获取父模板
        IDataModel parentModel = parentView.getModel();
        //编制计划
        DynamicObject planMonth = (DynamicObject) parentModel.getValue("dgdl_planmonth");
        int labeldimension = 0;
        //设置超编飘红
        setBzColor();
        //根据级数隐藏设置组织可见性
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        DynamicObject dgdlPlanmonthBill = dataEntity.getDynamicObject("dgdl_planmonth_bill");
        //层级
        int orghierarchy = planMonth.getInt("dgdl_orghierarchy");
        if (orghierarchy < 6) {
            for (int i = orghierarchy + 1; i <= 6; i++) {
                String tag = "dgdl_bz_adminorg" + i;
                this.getView().setVisible(false, tag);
            }
        }

        if (dgdlPlanmonthBill == null) {
            //选中组织id
            String adminId = parentView.getPageCache().get("adminId");
            Long adminOrg = Long.valueOf(adminId);
            //编制管理
            model.setValue("dgdl_planmonth_bill", parentModel.getValue("id"));
            //控编方式
            model.setValue("dgdl_bz_controlediting", planMonth.getString("dgdl_controlediting"));
            //弹性方式
            model.setValue("dgdl_bz_way", planMonth.getString("dgdl_way"));
            //弹性额度
            model.setValue("dgdl_bz_percent", planMonth.getInt("dgdl_people"));
            //末级组织
            model.setValue("adminorg", adminOrg);
            //当前组织层级
            DynamicObject adminorg = (DynamicObject) model.getValue("adminorg");
            String thisLayer = adminorg.getDynamicObject("adminorglayer").getString("number");
            String layer = "";
            switch (thisLayer) {
                case "02":
                    layer = "1";
                    break;
                case "03":
                    layer = "2";
                    break;
                case "04":
                    layer = "3";
                    break;
                case "05":
                    layer = "4";
                    break;
                case "06":
                    layer = "5";
                    break;
                case "07":
                    layer = "6";
                    break;
            }
            model.setValue("dgdl_layer", layer);
            int longLayer = Integer.parseInt(thisLayer);
            logger.info("PlanmonthBillBzFormPlugin当前组织层级=" + longLayer);
            //是否统一控编
            boolean isUnify = planMonth.getBoolean("dgdl_isunify");
            if (isUnify) {
                this.getView().setEnable(false, "dgdl_bz_controlediting");
                this.getView().setEnable(false, "dgdl_bz_way");
                this.getView().setEnable(false, "dgdl_bz_percent");
                this.getView().setEnable(false, "dgdl_isfreeze");
            }
            //上级组织编码
            Long parentOrgId = adminOrg;
            for (int i = (longLayer - 1); i >= 1; i--) {
                DynamicObject adminorghr = BusinessDataServiceHelper.loadSingle(parentOrgId, "haos_adminorghr");
                model.setValue("dgdl_bz_adminorg" + i, adminorghr);

                while(Objects.nonNull(adminorghr) && Objects.nonNull(adminorghr.getDynamicObject("parent"))) {

                   if(!adminorghr.getDynamicObject("parent").getBoolean("dgdl_isvirtual_ext") ){

                       parentOrgId = (Long) adminorghr.getDynamicObject("parent").getPkValue();
                       break;
                   }
                    adminorghr = BusinessDataServiceHelper.loadSingle(adminorghr.getDynamicObject("parent").getLong("id"), "haos_adminorghr");
                }
            }
        }
        List<String> dgdlLabeldimensions = Arrays.asList(planMonth.getString("dgdl_labeldimension").split(","));//标签维度
        if (dgdlLabeldimensions.contains("1") && !dgdlLabeldimensions.contains("2")) {
            labeldimension = 1;
        } else if (!dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 2;
        } else if (dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 3;
        }
        //通过计划的标签维度，控制可见性
        if (labeldimension == 1) {
            this.getView().setVisible(false, "dgdl_bz_jobproperty");
        } else if (labeldimension == 2) {
            //岗位属性
            this.getView().setVisible(false, "dgdl_bz_laborreltype");
        } else if (labeldimension == 0) {
            //全不选
            this.getView().setVisible(false, "dgdl_bz_jobproperty");
            this.getView().setVisible(false, "dgdl_bz_laborreltype");
        }

        //系统内置数据设置字段不可编辑
        if (dataEntity.getString("dgdl_bz_isinsert").equals("01")) {
            this.getView().setEnable(false, "dgdl_bz_jobproperty");
            this.getView().setEnable(false, "dgdl_bz_laborreltype");
            this.getView().setEnable(false, "dgdl_bz_controlediting");
            this.getView().setEnable(false, "dgdl_bz_way");
            this.getView().setEnable(false, "dgdl_bz_percent");
            this.getView().setEnable(false, "dgdl_isfreeze");
        }
        //月度编制数根据当前月份限制过往月份编制数不可编辑
//        int month = DateUtils.getMonth(new Date());
//        if (month > 1) {
//            for (int i = 1; i < month; i++) {
//                String dgdl_bzStr = "dgdl_bz" + i;
//                String dgdl_jzStr = "dgdl_jz" + i;
//                this.getView().setEnable(false, dgdl_bzStr);
//                this.getView().setEnable(false, dgdl_jzStr);
//            }
//        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate operate = (FormOperate) args.getSource();
        String operateKey = operate.getOperateKey();
        if (StringUtils.equals("save", operateKey)) {
            //保存校验
            checkSave();
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String filedName = e.getProperty().getName();
        if (filedName.contains("dgdl_jz")) {
            setBzColor();//设置超编飘红
        } else if (filedName.contains("dgdl_bz")){
            setBzColor(); //设置超编飘红
            setKbNumber();//设置控编数
        } else if (filedName.equals("dgdl_bz_controlediting")) {
            setKbNumber();//设置控编数
        } else if (filedName.equals("dgdl_bz_way")) {
            setKbNumber();//设置控编数
        } else if (filedName.equals("dgdl_bz_percent")) {
            setKbNumber();//设置控编数
        }
    }

    @Override
    public void beforeImportData(BeforeImportDataEventArgs e) {
        super.beforeImportData(e);
        Map<String, Object> optionMap = e.getOption();
        Map<String, Object> sourceData = e.getSourceData();
        String importtype = (String) optionMap.get("importtype");
        //新增
        if (importtype.equals("new")) {
            Map<String, Object> adminorgMap = (Map<String, Object>) sourceData.get("adminorg");
            String adminorgNumber = (String) adminorgMap.get("number");
            QFilter qFilter = new QFilter("number", QCP.equals, adminorgNumber);
            qFilter.and(new QFilter("iscurrentversion", QCP.equals, true));
            DynamicObject aminOrgObj = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "id,structnumber,structlongnumber", qFilter.toArray());
            if (aminOrgObj == null) {
                throw new KDBizException("系统中不存在该末级组织");
            }
            //末级组织根据id插入
            adminorgMap.put("importprop", "id");
            adminorgMap.put("id", aminOrgObj.getString("id"));
            sourceData.put("adminorg", adminorgMap);
            //通过长编码获取各个父级行政组织
            String stringLongNumber = aminOrgObj.getString("structlongnumber");
            String[] splitArray = stringLongNumber.split("!");
            for (int i = 0; i < splitArray.length; i++) {
                QFilter orgqFilter = new QFilter("structnumber", QCP.equals, splitArray[i]);
                orgqFilter.and(new QFilter("iscurrentversion", QCP.equals, true));
                DynamicObject obj = BusinessDataServiceHelper.loadSingle("haos_adminorghr", "id,structnumber,structlongnumber,adminorglayer", orgqFilter.toArray());
                if (Objects.nonNull(obj)) {
                    String adminorglayer = obj.getString("adminorglayer.number");
                    Map<String, String> dgdl_bz_adminorgMap = new HashMap<>();
                    dgdl_bz_adminorgMap.put("importprop", "id");
                    dgdl_bz_adminorgMap.put("id", obj.getString("id"));
                    if ("02".equals(adminorglayer)) {
                        sourceData.put("dgdl_bz_adminorg1", dgdl_bz_adminorgMap);
                    } else if ("03".equals(adminorglayer)) {
                        sourceData.put("dgdl_bz_adminorg2", dgdl_bz_adminorgMap);
                    } else if ("04".equals(adminorglayer)) {
                        sourceData.put("dgdl_bz_adminorg3", dgdl_bz_adminorgMap);
                    } else if ("05".equals(adminorglayer)) {
                        sourceData.put("dgdl_bz_adminorg4", dgdl_bz_adminorgMap);
                    } else if ("06".equals(adminorglayer)) {
                        sourceData.put("dgdl_bz_adminorg5", dgdl_bz_adminorgMap);
                    } else if ("07".equals(adminorglayer)) {
                        sourceData.put("dgdl_bz_adminorg6", dgdl_bz_adminorgMap);
                    }
                }
            }
        }
        //数据校验
        checkImport(sourceData, importtype);
    }

    private void checkSave() {
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        DynamicObject dgdlPlanmonthBill = dataEntity.getDynamicObject("dgdl_planmonth_bill");
        if (dgdlPlanmonthBill == null) {
            throw new KDBizException("请填写月度编制管理信息");
        }
        DynamicObject dgdlPlanmonth = BusinessDataServiceHelper.loadSingle(dataEntity.getLong("dgdl_planmonth_bill.dgdl_planmonth.id"), "dgdl_planmonth");
        List<String> dgdlLabeldimensions = Arrays.asList(dgdlPlanmonth.getString("dgdl_labeldimension").split(","));//标签维度
        //校验必填
        if (dgdlLabeldimensions.contains("1") && StringUtils.isEmpty(dataEntity.getString("dgdl_bz_laborreltype"))) {
            throw new KDBizException("用工关系类型必填");
        }
        if (dgdlLabeldimensions.contains("2") && StringUtils.isEmpty(dataEntity.getString("dgdl_bz_jobproperty"))) {
            throw new KDBizException("岗位属性必填");
        }
        String dgdlBzControlediting = dataEntity.getString("dgdl_bz_controlediting");
        if (dgdlBzControlediting.equals("3")) {
            if (StringUtils.isEmpty(dataEntity.getString("dgdl_bz_way"))) {
                throw new KDBizException("弹性方式必填");
            }
            if (dataEntity.getBigDecimal("dgdl_bz_percent").doubleValue() == 0D) {
                throw new KDBizException("弹性额度必填");
            }
        }
        //重复性校验 计划信息+组织+维度
        int labeldimension = getLabeldimension();
        QFilter qFilter = new QFilter("dgdl_planmonth_bill", QCP.equals, dataEntity.getLong("dgdl_planmonth_bill.id"));
        qFilter.and("adminorg", QCP.equals, dataEntity.getLong("adminorg.id"));
        qFilter.and("id", QCP.not_in, dataEntity.getLong("id"));
        if (labeldimension == 1) {
            qFilter.and("dgdl_bz_laborreltype", QCP.equals, dataEntity.getString("dgdl_bz_laborreltype"));
        } else if (labeldimension == 2) {
            qFilter.and("dgdl_bz_jobproperty", QCP.equals, dataEntity.getString("dgdl_bz_jobproperty"));
        } else if (labeldimension == 3) {
            qFilter.and("dgdl_bz_laborreltype", QCP.equals, dataEntity.getString("dgdl_bz_laborreltype"));
            qFilter.and("dgdl_bz_jobproperty", QCP.equals, dataEntity.getString("dgdl_bz_jobproperty"));
        }
        boolean exists = QueryServiceHelper.exists("dgdl_planmonth_bz", qFilter.toArray());
        if (exists) {
            throw new KDBizException("已存在相同维度数据，请检查");
        }
    }

    private void checkImport(Map<String, Object> sourceData, String importtype) {
        String dgdl_bz_jobproperty = (String) sourceData.get("dgdl_bz_jobproperty");
        String dgdl_bz_laborreltype = (String) sourceData.get("dgdl_bz_laborreltype");
        String dgdl_bz_controlediting = (String) sourceData.get("dgdl_bz_controlediting");//控编方式
        String dgdl_bz_way = (String) sourceData.get("dgdl_bz_way");//弹性方式
        String dgdl_bz_percent = (String) sourceData.get("dgdl_bz_percent");//弹性额度
        logger.info("PlanmonthBillBzFormPlugin数据=" + dgdl_bz_percent);
        Map<String, Object> dgdlPlanmonthBillMap = (Map<String, Object>) sourceData.get("dgdl_planmonth_bill");
        String billno = (String) dgdlPlanmonthBillMap.get("billno");
        DynamicObject dgdl_planmonth_billDy = BusinessDataServiceHelper.loadSingle("dgdl_planmonth_bill", "id,dgdl_planmonth", new QFilter[]{new QFilter("billno", QCP.equals, billno)});
        if (dgdl_planmonth_billDy == null) {
            throw new KDBizException("系统中不存在该月度编制管理信息");
        }
        //统一控编取计划
        DynamicObject dgdlPlanmonth = dgdl_planmonth_billDy.getDynamicObject("dgdl_planmonth");
        boolean dgdlIsunify = dgdlPlanmonth.getBoolean("dgdl_isunify");
        if (dgdlIsunify) {
            sourceData.put("dgdl_bz_controlediting", dgdlPlanmonth.getString("dgdl_controlediting"));
            sourceData.put("dgdl_bz_way", dgdlPlanmonth.getString("dgdl_way"));
            sourceData.put("dgdl_bz_percent", dgdlPlanmonth.getString("dgdl_people"));
            dgdl_bz_controlediting = (String) sourceData.get("dgdl_bz_controlediting");//控编方式
            dgdl_bz_way = (String) sourceData.get("dgdl_bz_way");
            dgdl_bz_percent = (String) sourceData.get("dgdl_bz_percent");
        }
        List<String> dgdlLabeldimensions = Arrays.asList(dgdlPlanmonth.getString("dgdl_labeldimension").split(","));//标签维度
        //合法性校验
        if (!dgdlLabeldimensions.contains("1") && !dgdlLabeldimensions.contains("2")) {
            if (StringUtils.isNotEmpty(dgdl_bz_laborreltype) || StringUtils.isNotEmpty(dgdl_bz_jobproperty)) {
                throw new KDBizException("用工关系类型,岗位属性不可填写");
            }
        } else if (dgdlLabeldimensions.contains("1") && !dgdlLabeldimensions.contains("2")) {
            if (StringUtils.isNotEmpty(dgdl_bz_jobproperty)) {
                throw new KDBizException("岗位属性不可填写");
            }
        } else if (!dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            if (StringUtils.isNotEmpty(dgdl_bz_laborreltype)) {
                throw new KDBizException("用工关系类型不可填写");
            }
        } else if (dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            if (StringUtils.isEmpty(dgdl_bz_laborreltype) || StringUtils.isEmpty(dgdl_bz_jobproperty)) {
                throw new KDBizException("用工关系类型,岗位属性必填");
            }
        }
        if (dgdlLabeldimensions.contains("1") && StringUtils.isEmpty(dgdl_bz_laborreltype)) {
            throw new KDBizException("用工关系类型必填");
        }
        if (dgdlLabeldimensions.contains("2") && StringUtils.isEmpty(dgdl_bz_jobproperty)) {
            throw new KDBizException("岗位属性必填");
        }
        //弹性控编
        if (dgdl_bz_controlediting.equals("3")) {
            if (kd.bos.util.StringUtils.isEmpty(dgdl_bz_way)) {
                throw new KDBizException("弹性方式必填");
            }
            if (kd.bos.util.StringUtils.isEmpty(dgdl_bz_percent)) {
                throw new KDBizException("弹性额度必填");
            }
        }
        Map<String, Object> adminorgMap = (Map<String, Object>) sourceData.get("adminorg");
        //重复性校验 计划信息+组织+维度
        int labeldimension = 0;
        if (dgdlLabeldimensions.contains("1") && !dgdlLabeldimensions.contains("2")) {
            labeldimension = 1;
        } else if (!dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 2;
        } else if (dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 3;
        }
        String adminorgNumber = (String) adminorgMap.get("number");
        QFilter qFilter = new QFilter("dgdl_planmonth_bill", QCP.equals, dgdl_planmonth_billDy.getLong("id"));
        qFilter.and("adminorg.number", QCP.equals, adminorgNumber);
        if ("override".equals(importtype)) {
            Long id = (Long) sourceData.get("id");
            qFilter.and("id", QCP.not_in, id);
        }
        if (labeldimension == 1) {
            qFilter.and("dgdl_bz_laborreltype", QCP.equals, dgdl_bz_laborreltype);
        } else if (labeldimension == 2) {
            qFilter.and("dgdl_bz_jobproperty", QCP.equals, dgdl_bz_jobproperty);
        } else if (labeldimension == 3) {
            qFilter.and("dgdl_bz_laborreltype", QCP.equals, dgdl_bz_laborreltype);
            qFilter.and("dgdl_bz_jobproperty", QCP.equals, dgdl_bz_jobproperty);
        }
        boolean exists = QueryServiceHelper.exists("dgdl_planmonth_bz", qFilter.toArray());
        if (exists) {
            throw new KDBizException("已存在相同维度数据，请检查");
        }
        //设置控编数
        if (dgdl_bz_controlediting.equals("1") || dgdl_bz_controlediting.equals("2")) {
            for (int i = 1; i <= 12; i++) {
                String dgdl_bzStr = "dgdl_bz" + i;
                String dgdl_kbStr = "dgdl_bz_kb" + i;
                String dgdl_bz = (String) sourceData.get(dgdl_bzStr);
                if (dgdl_bz != null) {
                    sourceData.put(dgdl_kbStr, dgdl_bz);
                }
            }
        } else if (dgdl_bz_controlediting.equals("3")) {
            if (StringUtils.isNotEmpty(dgdl_bz_way)) {
                //百分比
                if (dgdl_bz_way.equals("1")) {
                    for (int i = 1; i <= 12; i++) {
                        String dgdl_bzStr = "dgdl_bz" + i;
                        String dgdl_kbStr = "dgdl_bz_kb" + i;
                        String dgdl_bz = (String) sourceData.get(dgdl_bzStr);
                        logger.info("PlanmonthBillBzFormPlugin月份=" + dgdl_bzStr + "数据=" + dgdl_bz + ",控编字段" + dgdl_kbStr + ",值" + dgdl_bz_percent);
                        if (dgdl_bz == null) {
                            continue;
                        }
                        BigDecimal bzNumber = BigDecimal.valueOf(Long.parseLong(dgdl_bz));
                        //计算额度
                        BigDecimal divide = new BigDecimal(dgdl_bz_percent).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                        //计算增量
                        BigDecimal multiply = divide.multiply(bzNumber).setScale(0, RoundingMode.UP);
                        sourceData.put(dgdl_kbStr,multiply.add(bzNumber));
                    }
                } else if (dgdl_bz_way.equals("2")) {
                    //人数
                    for (int i = 1; i <= 12; i++) {
                        String dgdl_bzStr = "dgdl_bz" + i;
                        String dgdl_kbStr = "dgdl_bz_kb" + i;
                        String dgdl_bzS = (String) sourceData.get(dgdl_bzStr);
                        logger.info("PlanmonthBillBzFormPlugin月份=" + dgdl_bzStr + "数据=" + dgdl_bzS + ",控编字段" + dgdl_kbStr + ",值" + dgdl_bz_percent);
                        if (dgdl_bzS == null) {
                            continue;
                        }
                        int dgdl_bz = Integer.parseInt(dgdl_bzS) + Integer.parseInt(dgdl_bz_percent);
                        logger.info("PlanmonthBillBzFormPlugin最终计算的总和=" + dgdl_bz);
                        sourceData.put(dgdl_kbStr, dgdl_bz);
                    }
                }
            }
        }
    }

    /**
     * 获取月度计划标签维度
     *
     * @return 0-全不选 1-用工关系类型 2-岗位标签 3-全选
     */
    private int getLabeldimension() {
        int labeldimension = 0;//全不选
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        DynamicObject dgdlPlanmonthBill = dataEntity.getDynamicObject("dgdl_planmonth_bill");
        if (dgdlPlanmonthBill == null) {
            throw new KDBizException("请填写月度编制管理信息");
        }
        DynamicObject dgdlPlanmonth = BusinessDataServiceHelper.loadSingle(dataEntity.getLong("dgdl_planmonth_bill.dgdl_planmonth.id"), "dgdl_planmonth");
        List<String> dgdlLabeldimensions = Arrays.asList(dgdlPlanmonth.getString("dgdl_labeldimension").split(","));//标签维度
        if (dgdlLabeldimensions.contains("1") && !dgdlLabeldimensions.contains("2")) {
            labeldimension = 1;
        } else if (!dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 2;
        } else if (dgdlLabeldimensions.contains("1") && dgdlLabeldimensions.contains("2")) {
            labeldimension = 3;
        }
        return labeldimension;
    }

    private void setBzColor() {
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        //渲染颜色
        int month = 12;
        for (int i = 1; i <= month; i++) {
            String dgdl_jzStr = "dgdl_jz" + i;
            String dgdl_bzStr = "dgdl_bz" + i;
            int dgdl_jz = dataEntity.getInt(dgdl_jzStr);
            int dgdl_bz = dataEntity.getInt(dgdl_bzStr);
            HashMap<String, Object> fieldMap = new HashMap<>();
            //编制>基准
            if (dgdl_bz > dgdl_jz) {
                if (dgdl_jz != 0){
                    //设置前景色
                    fieldMap.put(ClientProperties.ForeColor, "#ff0000");
                }
            } else {
                //设置前景色
                fieldMap.put(ClientProperties.ForeColor, "#272727");
            }
            this.getView().updateControlMetadata(dgdl_bzStr, fieldMap);
        }
    }

    /**
     * 设置控编数
     * ①控编方式=允许超编、不允许超编，每月控编人数=编制人数；
     * ②控编方式=弹性控编，每月控编人数=编制数+弹性控编数量。
     * 当编制数运算后不为整数，向上取整。
     */
    private void setKbNumber() {
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        int month = 12;
        if (dataEntity.getString("dgdl_bz_controlediting").equals("1") || dataEntity.getString("dgdl_bz_controlediting").equals("2")) {
            for (int i = 1; i <= month; i++) {
                String dgdl_bzStr = "dgdl_bz" + i;
                String dgdl_kbStr = "dgdl_bz_kb" + i;
                int dgdl_bz = dataEntity.getInt(dgdl_bzStr);
                this.getModel().setValue(dgdl_kbStr, dgdl_bz);
            }
        } else if (dataEntity.getString("dgdl_bz_controlediting").equals("3")) {
            String dgdlBzWay = dataEntity.getString("dgdl_bz_way");//弹性方式
            int dgdlBzPercent = dataEntity.getInt("dgdl_bz_percent");//弹性额度
            if (StringUtils.isNotEmpty(dgdlBzWay)) {
                //百分比
                if (dgdlBzWay.equals("1")) {
                    for (int i = 1; i <= month; i++) {
                        String dgdl_bzStr = "dgdl_bz" + i;
                        String dgdl_kbStr = "dgdl_bz_kb" + i;
                        BigDecimal bzNumber = dataEntity.getBigDecimal(dgdl_bzStr);//编制数
                        if (bzNumber == null) {
                            continue;
                        }
                        //计算额度
                        BigDecimal divide = new BigDecimal(dgdlBzPercent).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                        //计算增量
                        BigDecimal multiply = divide.multiply(bzNumber).setScale(0, RoundingMode.UP);
                        this.getModel().setValue(dgdl_kbStr,multiply.add(bzNumber));
                    }
                } else if (dgdlBzWay.equals("2")) {
                    //人数
                    for (int i = 1; i <= month; i++) {
                        String dgdl_bzStr = "dgdl_bz" + i;
                        String dgdl_kbStr = "dgdl_bz_kb" + i;
                        int dgdl_bz = dataEntity.getInt(dgdl_bzStr) + dgdlBzPercent;
                        this.getModel().setValue(dgdl_kbStr, dgdl_bz);
                    }
                }
            }
        }
    }
}
