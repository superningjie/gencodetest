package tdkw.hr.odc.haos.opplugin;


import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;

/**
 * @ClassName : DelivaryDateValidator
 * @Description :
 * @Author : XYP
 * @Date: 2024-08-09 15:59
 */
public class DelivaryDateValidator extends AbstractValidator {
    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntit = this.getDataEntities();
        for (ExtendedDataEntity extendedDataEntity : dataEntit) {
            DynamicObject rowDataModel = extendedDataEntity.getDataEntity();
            DynamicObjectCollection bentryentity = rowDataModel.getDynamicObjectCollection("bentryentity");
            for (DynamicObject dynamicObject : bentryentity) {
                String bcontrolstrategy = dynamicObject.getString("tdkw_bcontrolstrategy");
                if (bcontrolstrategy!=null && bcontrolstrategy.equals("3")){
                    if (dynamicObject.get("tdkw_belasticcontrol")==null || dynamicObject.get("tdkw_belasticcount")==null){
                        this.addErrorMessage(extendedDataEntity, "使用组织编码:"+dynamicObject.getDynamicObject("buseorg").getString("number")+"： 请填弹性方式、弹性额度!");
                    }
                }

                DynamicObjectCollection centryentity = dynamicObject.getDynamicObjectCollection("centryentity");
                for (DynamicObject cdy : centryentity) {
                    String ccontrolstrategy = cdy.getString("tdkw_ccontrolstrategy");
                    if (ccontrolstrategy!=null && ccontrolstrategy.equals("3")){
                        if (cdy.get("tdkw_celasticcontrol")==null || cdy.get("tdkw_celasticcount")==null){
                            this.addErrorMessage(extendedDataEntity,
                                    "使用组织编码:"+dynamicObject.getDynamicObject("buseorg").getString("number")+"，岗位编码:"
                                            +cdy.getDynamicObject("cdutyworkrole").getString("number")+"： 请填弹性方式、弹性额度!");
                        }
                    }
                }
                DynamicObjectCollection dentryentity = dynamicObject.getDynamicObjectCollection("dentryentity");
                for (DynamicObject ddy : dentryentity) {
                    String dcontrolstrategy = ddy.getString("tdkw_dcontrolstrategy");
                    if (dcontrolstrategy!=null &&dcontrolstrategy.equals("3")){
                        if (ddy.get("tdkw_delasticcontrol")==null || ddy.get("tdkw_delasticcount")==null){
                            this.addErrorMessage(extendedDataEntity,
                                    "使用组织编码:"+dynamicObject.getDynamicObject("buseorg").getString("number")+"，职位编码:"
                                            +ddy.getDynamicObject("dentryentity").getString("number")+"： 请填弹性方式、弹性额度!");
                        }
                    }
                }
                DynamicObjectCollection eentryentity = dynamicObject.getDynamicObjectCollection("eentryentity");
                for (DynamicObject edy : eentryentity) {
                    String econtrolstrategy = edy.getString("tdkw_econtrolstrategy");
                    if (econtrolstrategy!=null &&econtrolstrategy.equals("3")){
                        if (edy.get("tdkw_eelasticcontrol")==null || edy.get("tdkw_eelasticcount")==null){
                            this.addErrorMessage(extendedDataEntity,
                                    "使用组织编码:"+dynamicObject.getDynamicObject("buseorg").getString("number")+"，用工关系编码:"+
                                            edy.getDynamicObject("elaborreltype").getString("number")+"： 请填弹性方式、弹性额度!");
                        }
                    }
                }

                DynamicObjectCollection fentryentity = dynamicObject.getDynamicObjectCollection("fentryentity");
                for (DynamicObject fdy : fentryentity) {
                    String fcontrolstrategy = fdy.getString("tdkw_fcontrolstrategy");
                    if (fcontrolstrategy!=null &&fcontrolstrategy.equals("3")){
                        if (fdy.get("tdkw_felasticcontrol")==null || fdy.get("tdkw_felasticcount")==null){
                            this.addErrorMessage(extendedDataEntity,
                                    "使用组织编码:"+dynamicObject.getDynamicObject("buseorg").getString("number")+"，职级编码:"
                                            +fdy.getDynamicObject("fbasicdata1").getString("number")+"： 请填弹性方式、弹性额度!");
                        }
                    }
                }

            }
        }
    }
}
