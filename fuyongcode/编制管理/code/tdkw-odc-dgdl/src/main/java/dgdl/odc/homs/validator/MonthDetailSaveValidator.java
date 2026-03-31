package dgdl.odc.homs.validator;

import dgdl.odc.homs.opplugin.PlanMonthBzSaveOp;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;

public class MonthDetailSaveValidator extends AbstractValidator {
    private  static String BASICFEILD="dgdl_jz1,dgdl_jz2,dgdl_jz3,dgdl_jz4,dgdl_jz5,dgdl_jz6,dgdl_jz7,dgdl_jz8,dgdl_jz9,dgdl_jz10,dgdl_jz11,dgdl_jz12";
    private  static String BZFEILD="dgdl_bz1,dgdl_bz2,dgdl_bz3,dgdl_bz4,dgdl_bz5,dgdl_bz6,dgdl_bz7,dgdl_bz8,dgdl_bz9,dgdl_bz10,dgdl_bz11,dgdl_bz12";
    private static Log logger = LogFactory.getLog(MonthDetailSaveValidator.class);

    @Override
    public void validate() {
        if(this.dataEntities.length==1){

            for (ExtendedDataEntity dataEntity : this.dataEntities) {
                DynamicObject bill = dataEntity.getDataEntity();
                DynamicObject adminorg = bill.getDynamicObject("adminorg");
                DynamicObject planmonthbill = bill.getDynamicObject("dgdl_planmonth_bill");
                String levelstr=bill.getString("dgdl_layer");
                if(levelstr!=null&&!levelstr.isEmpty()){
                    int intLayer = Integer.parseInt(levelstr);
                    int parentlevel=intLayer-1;

                    //单条数据的，循环数不多，3*12=36，就拆开循环


                    //基准数与编制数的校验

                    checkBasicAndEstablishing(dataEntity);

                    //上下级编制数的校验,基准数校验
                    if(!levelstr.equals("1")&&!this.getOperateKey().equals("save")){

                        checkParentEstablishing(dataEntity,parentlevel,planmonthbill);
                    }

                }


            }
        }

    }

    private void checkParentEstablishing(ExtendedDataEntity dataEntity, int parentlevel, DynamicObject planmonthbill) {
        DynamicObject bill=dataEntity.getDataEntity();
        DynamicObject parentorg = bill.getDynamicObject("dgdl_bz_adminorg"+parentlevel);
        String  jobproperty = bill.getString("dgdl_bz_jobproperty");
        String  laborreltype = bill.getString("dgdl_bz_laborreltype");
        String levelstr=bill.getString("dgdl_layer");

        QFilter planfilter=new QFilter("dgdl_planmonth_bill.id", QCP.equals,planmonthbill.getLong("id"));
        QFilter typefilter=new QFilter("dgdl_bz_jobproperty", QCP.equals,jobproperty).and("dgdl_bz_laborreltype",QCP.equals,laborreltype);
        QFilter parentfilter=new QFilter("dgdl_bz_adminorg"+parentlevel+".id", QCP.equals,parentorg.getLong("id"));
        QFilter orgfilter=new QFilter("adminorg.id", QCP.equals,parentorg.getLong("id"));

        QFilter selffilter=new QFilter("id", QCP.not_equals,bill.getLong("id")).and("adminorg.id",QCP.not_equals,parentorg.getLong("id")).and("dgdl_layer",QCP.equals,levelstr);
        DynamicObject parentbill=QueryServiceHelper.queryOne("dgdl_planmonth_bz","dgdl_bz_adminorg1,dgdl_planmonth_bill,"+BASICFEILD+","+BZFEILD,new QFilter[]{orgfilter,planfilter,typefilter});
        logger.info("parentbill:{}",parentbill);
        if(parentbill==null){
            logger.info("无法获取上级");
            return;
        }
        //所有同级数据
        DynamicObjectCollection bills= QueryServiceHelper.query("dgdl_planmonth_bz",BASICFEILD+","+BZFEILD,new QFilter[]{planfilter.and(parentfilter).and(selffilter).and(typefilter)});

        for (int i = 1; i < 13; i++) {
            int basic=bill.getInt("dgdl_jz"+i);
            int establishing=bill.getInt("dgdl_bz"+i);
            int parentbasic=parentbill.getInt("dgdl_jz"+i);
            int parentestablishing=parentbill.getInt("dgdl_bz"+i);

            for (DynamicObject otherbill : bills) {
                basic=basic+otherbill.getInt("dgdl_jz"+i);
                establishing=establishing+otherbill.getInt("dgdl_bz"+i);
            }
            if(parentestablishing<establishing){
                this.addErrorMessage(dataEntity,"上下级编制数的校验:当前同组织层级、同类别下"+i+"月编制数合计（"+establishing+"）超过上级组织"+parentorg.getString("name")+"（"+parentestablishing+"），请调整。");
            }
            if(parentbasic<basic){
                this.addWarningMessage(dataEntity,"上下级基准数的校验:当前同组织层级、同类别下"+i+"月基准数合计（"+basic+"）超过上级组织"+parentorg.getString("name")+"（"+parentbasic+"），请注意。");

            }
        }



    }

    private void checkBasicAndEstablishing(ExtendedDataEntity dataEntity) {
        DynamicObject bill=dataEntity.getDataEntity();

        for (int i = 1; i < 13; i++) {
            int basic=bill.getInt("dgdl_jz"+i);
            int establishing=bill.getInt("dgdl_bz"+i);
            if(establishing>basic){
                this.addErrorMessage(dataEntity,"基准数与编制数的校验:"+i+"月编制数超过基准数，请调整。");
            }
        }


    }
}
