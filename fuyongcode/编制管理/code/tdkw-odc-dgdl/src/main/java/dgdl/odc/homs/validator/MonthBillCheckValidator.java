package dgdl.odc.homs.validator;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.haos.business.servicehelper.OrgBatchBillHelper;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MonthBillCheckValidator extends AbstractValidator {

    private static Log logger = LogFactory.getLog(MonthBillCheckValidator.class);

    private final static String Filed = "adminorg,dgdl_bz_adminorg1,dgdl_bz_adminorg2,dgdl_bz_adminorg3,dgdl_bz_adminorg4,dgdl_bz_adminorg5,dgdl_bz_adminorg6,dgdl_bz_laborreltype,dgdl_bz_jobproperty,dgdl_jz1,dgdl_jz2,dgdl_jz3,dgdl_jz4,dgdl_jz5,dgdl_jz6,dgdl_jz7" +
            ",dgdl_jz8,dgdl_jz9,dgdl_jz10,dgdl_jz11,dgdl_jz12,dgdl_bz1,dgdl_bz2,dgdl_bz3,dgdl_bz4,dgdl_bz5,dgdl_bz6,dgdl_bz7,dgdl_bz8,dgdl_bz9,dgdl_bz10,dgdl_bz11,dgdl_bz12";


    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        List<Long> level1=new ArrayList<>();
        List<Long> level2=new ArrayList<>();
        List<Long> level3=new ArrayList<>();
        List<Long> level4=new ArrayList<>();
        List<Long> level5=new ArrayList<>();

        if(dataEntities.length==0){
            return;
        }

        DynamicObject bill=dataEntities[0].getDataEntity();
        String alllevelstr = bill.getDynamicObject("dgdl_planmonth_bill")
                .getDynamicObject("dgdl_planmonth").getString("dgdl_orghierarchy");

        String labeldimension = bill.getDynamicObject("dgdl_planmonth_bill")
                .getDynamicObject("dgdl_planmonth").getString("dgdl_labeldimension");

        //1=用工关系类型:dgdl_bz_laborreltype,2=一线非一线:dgdl_bz_jobproperty
        int  label= 0;

        if (StringUtils.isNotEmpty(labeldimension)) {
            if (labeldimension.contains("1") && labeldimension.contains("2")) {
                label = 3;
            } else if (labeldimension.contains("1")) {
                label = 1;
            } else if (labeldimension.contains("2")) {
                label = 2;
            }

        }
        //用工关系类型
        Map<String,String> laborreltypeMap=new HashMap<>(6);
        laborreltypeMap.put("helpmate","合作伙伴");
        laborreltypeMap.put("interns_reserve_personnel","实习生");
        laborreltypeMap.put("labor_dispatch","劳务派遣");
        laborreltypeMap.put("regular_workers","正式工");
        laborreltypeMap.put("rehired_after_retirement","退休返聘");
        laborreltypeMap.put ("dayan","大雁");
        //一线非一线
        Map<String,String> jobpropertypeMap=new HashMap<>(6);
        jobpropertypeMap.put("0","一线");
        jobpropertypeMap.put("1","非一线");


        for (ExtendedDataEntity dataEntitiy : dataEntities) {
            DynamicObject dataEntity = dataEntitiy.getDataEntity();
            String level = dataEntity.getString("dgdl_layer");
            switch (level){
                case"1":
                    level1.add(dataEntity.getLong("adminorg.id"));
                    break;
                case"2":
                    level2.add(dataEntity.getLong("adminorg.id"));

                    break;
                case"3":
                    level3.add(dataEntity.getLong("adminorg.id"));

                    break;
                case"4":
                    level4.add(dataEntity.getLong("adminorg.id"));

                    break;
                case"5":
                    level5.add(dataEntity.getLong("adminorg.id"));

                    break;
            }
        }

        Integer alllevel=Integer.parseInt(alllevelstr);


        QFilter planfilter=new QFilter("dgdl_planmonth_bill.id", QCP.equals,bill.getDynamicObject("dgdl_planmonth_bill").getLong("id"));


        Map<String,List<DynamicObject>> suborgmap=new HashMap<>();
        for (int i = alllevel-1; i>0 ; i--) {
            List parentorglist=new ArrayList<>();
            switch (i){
                case 1:
                    parentorglist=level1;
                    break;
                case 2:
                    parentorglist=level2;
                    break;
                case 3:
                    parentorglist=level3;
                    break;
                case 4:
                    parentorglist=level4;
                    break;
                case 5:
                    parentorglist=level5;
                    break;
            }
            String parentorgkey="dgdl_bz_adminorg"+(i+1);
            DynamicObjectCollection bills= QueryServiceHelper.query("dgdl_planmonth_bz",Filed,new QFilter[]{planfilter.and(parentorgkey,QCP.in,parentorglist)});
            for (DynamicObject subbill : bills) {
                List<DynamicObject> suborglist=new ArrayList<>();
                String  jobproperty = subbill.getString("dgdl_bz_jobproperty");
                String  laborreltype = subbill.getString("dgdl_bz_laborreltype");
                String  parentorgid = subbill.getString(parentorgkey+".id");
                String key=parentorgid+"-"+jobproperty+"-"+laborreltype;
                if(suborgmap.containsKey(key)){
                    suborglist=suborgmap.get(key);
                }
                suborglist.add(subbill);
                suborgmap.put(key,suborglist);

            }

        }
        for (ExtendedDataEntity dataEntitiy : dataEntities) {
            DynamicObject dataEntity = dataEntitiy.getDataEntity();
            String level = dataEntity.getString("dgdl_layer");
            if(level.equals(alllevelstr)){
                continue;
            }

            String  jobproperty = dataEntity.getString("dgdl_bz_jobproperty");
            String  laborreltype = dataEntity.getString("dgdl_bz_laborreltype");
            String orgid = dataEntity.getString("adminorg.id");


            String key=orgid+"-"+jobproperty+"-"+laborreltype;
            String type="";
            if(label==1){
                type=laborreltypeMap.get(laborreltype);
            }else if(label==2){
                type=jobpropertypeMap.get(jobproperty);
            }else {
                type=jobpropertypeMap.get(jobproperty)+laborreltypeMap.get(laborreltype);

            }
            int longnameLevel=Integer.parseInt(level);
            String longname= "";
            for (int i = 1; i <longnameLevel; i++) {
                longname=longname+dataEntity.getString("dgdl_bz_adminorg"+i)+"-";
            }
            longname=longname+dataEntity.getString("adminorg.name");
            List<DynamicObject> suborglist=suborgmap.get(key);
            if(suborglist!=null&&suborglist.size()>0){
                for (int i = 1; i < 13; i++) {
                    int basic=0;
                    int establishing=0;
                    int parentbasic=dataEntity.getInt("dgdl_jz"+i);
                    int parentestablishing=dataEntity.getInt("dgdl_bz"+i);

                    for (DynamicObject otherbill : suborglist) {
                        basic=basic+otherbill.getInt("dgdl_jz"+i);
                        establishing=establishing+otherbill.getInt("dgdl_bz"+i);
                    }
                    if(parentestablishing<establishing){
                        this.addErrorMessage(dataEntitiy,"上下级编制数的校验:"+longname+"的直接下级组织"+type+"类别"+i+"月编制数合计（"+establishing+"）超过上级组织编制数（"+parentestablishing+"），请调整。");
                    }
                    if(parentbasic<basic){
                        this.addWarningMessage(dataEntitiy,"上下级基准数的校验:"+longname+"的直接下级组织"+type+"类别"+i+"月基准数合计（"+basic+"）超过上级组织基准数（"+parentbasic+"），请注意。");

                    }
                }
            }


        }



    }

}
