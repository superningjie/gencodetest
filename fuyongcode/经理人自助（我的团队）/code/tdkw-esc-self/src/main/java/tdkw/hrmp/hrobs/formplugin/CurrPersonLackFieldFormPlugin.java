//package tdkw.hrmp.hrobs.formplugin;
//
//import kd.bos.dataentity.entity.DynamicObject;
//import kd.bos.dataentity.entity.LocaleString;
//import kd.bos.entity.Tips;
//import kd.bos.form.control.Label;
//import kd.bos.form.plugin.AbstractFormPlugin;
//import kd.bos.orm.query.QCP;
//import kd.bos.orm.query.QFilter;
//import kd.bos.servicehelper.BusinessDataServiceHelper;
//import tdkw.hrmp.hrobs.formplugin.util.PersonnelCheckUtil;
//import tdkw.hr.hspm.business.service.PersonnelIntegrityService;
//import tdkw.hr.hspm.business.service.impl.PersonnelIntegrityServiceImpl;
//
//import java.text.ParseException;
//import java.util.EventObject;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author xxx
// * @Date 2023/10/24 9:09
// * @PackageName:tdkw.hrmp.hrobs.formplugin
// * @ClassName: CurrPersonLackFieldFormPlugin
// * @Description: TODO
// * @Version 1.0
// */
//public class CurrPersonLackFieldFormPlugin extends AbstractFormPlugin {
//    @Override
//    public void afterCreateNewData(EventObject e) {
//        super.afterCreateNewData(e);
//        // 单据头修改时触发修改单据头的字段帮助文本修改
//        Label control1 = this.getControl("tdkw_percentage");
//        Tips tips = new Tips();
//        tips.setTitle(new LocaleString("缺失信息："));
//        String person = null;
//        try {
//            person = getCurrPersonLackField(this.getView().getFormShowParameter().getCustomParam("erfileid"));
//        } catch (ParseException ex) {
//            throw new RuntimeException(ex);
//        }
//        tips.setContent(new LocaleString(person));
//        tips.setShowIcon(false);
//        tips.setType("text");
//        tips.setPlace("right");
//        control1.addTips(tips);
//    }
//
//    public String getCurrPersonLackField(Long personId) throws ParseException {
//
//        DynamicObject person = BusinessDataServiceHelper.loadSingle(this.getView().getFormShowParameter().getCustomParam("person"), "hrpi_person");
//        List<String> personFields = PersonnelCheckUtil.specialRule(person);
//        List<String> integrityModuleList = PersonnelCheckUtil.integrityModule();
//        // 读取配置表有哪些附表
//        String allField="";
//        for (int idx = 0; idx < integrityModuleList.size(); idx++) {
//            List<String> moduleFieldList = PersonnelCheckUtil.moduleField(integrityModuleList.get(idx), person);
//            String module= integrityModuleList.get(idx).replace("模块","");
//            for (String moduleField : moduleFieldList) {
//                moduleField = "缺少" + module + moduleField + ";\n";
//                allField=allField+moduleField;
//            }
//        }
//
//        for (String personField : personFields) {
//            allField = allField + personField + "\n";
//        }
//
//
//
//
////        String selectProperties = "id,tdkw_person,tdkw_company,tdkw_post";
////        QFilter qFilter = new QFilter("status", "=", "C");
////        qFilter.and("enable", "=", "1");
////        DynamicObject[] personnellackof = BusinessDataServiceHelper.load("tdkw_hspm_personnellackof", "id,name,tdkw_number", qFilter.toArray());
////        Map<String, String> lockPropertiesMapName = new HashMap<>();
////        for (DynamicObject item : personnellackof) {
////            String number = item.getString("tdkw_number");
////            selectProperties = selectProperties + "," + number;
////            lockPropertiesMapName.put(number, item.getLocaleString("name").getLocaleValue_zh_CN());
////        }
////        QFilter personnelinforQFilter = new QFilter("tdkw_person", QCP.equals,personId);
////        DynamicObject[] personInfos = BusinessDataServiceHelper.load("tdkw_hspm_personnelinfor", selectProperties, personnelinforQFilter.toArray());
////        String lackofinformation = "";
////        for (DynamicObject personInfo : personInfos) {
////
////            // Long tdkwPerson = (Long) personInfo.get("tdkw_person");
////            for (String lockProperty : lockPropertiesMapName.keySet()) {
////                if (personInfo.getBoolean(lockProperty)) {
////                    if (org.apache.commons.lang3.StringUtils.isEmpty(lackofinformation)) {
////                        lackofinformation = lockPropertiesMapName.get(lockProperty);
////                    } else {
////                        lackofinformation = lackofinformation + ";" + lockPropertiesMapName.get(lockProperty);
////                    }
////                }
////            }
////        }
//        return allField;
//    }
//}
