package tdkw.hr.odc.haos.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import tdkw.hr.odc.haos.common.BzSelect;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName : BZchangeentFrom
 * @Description : 单据体根据控编方式计算数据
 * @Author : XYP
 * @Date: 2024-07-27 16:32
 */
public class BZchangeCountPlugin extends AbstractBillPlugIn {
    private static final Log LOGGER = LogFactory.getLog(BZchangeCountPlugin.class);
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        //当值改变为调整后弹性额度、方式
        if (StringUtils.equals("tdkw_belasticcontrol", name) || StringUtils.equals("tdkw_belasticcount", name)) {
            if (this.getModel().getValue("tdkw_belasticcontrol") != null && this.getModel().getValue("tdkw_belasticcount") != null) {
                //调整后弹性方式
                String belasticcontrol = (String) this.getModel().getValue("tdkw_belasticcontrol");
                //调整后弹性额度
                Integer tdkw_belasticcount = (Integer) this.getModel().getValue("tdkw_belasticcount");
                //使用组织
                List<String> bentryentityset = BzSelect.bentryentityset();
                if (belasticcontrol.equals("1")) {//人数
                    if (this.getModel().getValue("tdkw_bhxj2") != null) {
                        Integer tdkw_bhxj2 = (Integer) this.getModel().getValue("tdkw_bhxj2");
                        this.getModel().setValue("tdkw_byearstaffnu", tdkw_belasticcount + tdkw_bhxj2);
                    }
                    for (String bs : bentryentityset) {
                        if (this.getModel().getValue(bs) != null) {
                            Integer integer = (Integer) this.getModel().getValue(bs);
                            this.getModel().setValue(bs + "num", tdkw_belasticcount + integer);
                        }
                    }
                } else if (belasticcontrol.equals("2")) {//百分比
                    BigDecimal bigDecimal = BigDecimal.valueOf(tdkw_belasticcount);
                    BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                    if (this.getModel().getValue("tdkw_bhxj2") != null) {
                        Integer tdkw_bhxj2 = (Integer) this.getModel().getValue("tdkw_bhxj2");
                        BigDecimal bigDecimalv = BigDecimal.valueOf(tdkw_bhxj2);
                        BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                        this.getModel().setValue("tdkw_byearstaffnu", bigDecimalv.add(bigDecimalm));
                    }
                    for (String bs : bentryentityset) {
                        if (this.getModel().getValue(bs) != null) {
                            Integer integer = (Integer) this.getModel().getValue(bs);
                            BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                            BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                            this.getModel().setValue(bs + "num", bigDecimalv.add(bigDecimalm));
                        }
                    }
                }
            }
        }

        //用工
        if ((StringUtils.equals("tdkw_eelasticcount", name) || StringUtils.equals("tdkw_eelasticcontrol", name))
                &&(this.getModel().getValue("tdkw_eelasticcount") != null && this.getModel().getValue("tdkw_eelasticcontrol") != null)) {
            //调整后弹性方式
            String elasticcontrol = (String) this.getModel().getValue("tdkw_eelasticcontrol");
            //调整后弹性额度
            Integer elasticcount = (Integer) this.getModel().getValue("tdkw_eelasticcount");
            //用工
            List<String> entryentity = BzSelect.eentryentityset();
            if (elasticcontrol.equals("1")){
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        this.getModel().setValue(bs + "num", elasticcount + integer);
                    }
                }
            }else if (elasticcontrol.equals("2")){
                BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                        BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                        this.getModel().setValue(bs + "num", bigDecimalv.add(bigDecimalm));
                    }
                }
            }
        }



        //岗位
        if ((StringUtils.equals("tdkw_celasticcount", name) || StringUtils.equals("tdkw_celasticcontrol", name))
                &&(this.getModel().getValue("tdkw_celasticcount") != null && this.getModel().getValue("tdkw_celasticcontrol") != null)) {
            //调整后弹性方式
            String elasticcontrol = (String) this.getModel().getValue("tdkw_celasticcontrol");
            //调整后弹性额度
            Integer elasticcount = (Integer) this.getModel().getValue("tdkw_celasticcount");
            //用工
            List<String> entryentity = BzSelect.centryentityset();
            if (elasticcontrol.equals("1")){
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        this.getModel().setValue(bs + "num", elasticcount + integer);
                    }
                }
            }else if (elasticcontrol.equals("2")){
                BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                        BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                        this.getModel().setValue(bs + "num", bigDecimalv.add(bigDecimalm));
                    }
                }
            }
        }


        //职位  d
        if ((StringUtils.equals("tdkw_delasticcount", name) || StringUtils.equals("tdkw_delasticcontrol", name))
                &&(this.getModel().getValue("tdkw_delasticcount") != null && this.getModel().getValue("tdkw_delasticcontrol") != null)) {
            //调整后弹性方式
            String elasticcontrol = (String) this.getModel().getValue("tdkw_delasticcontrol");
            //调整后弹性额度
            Integer elasticcount = (Integer) this.getModel().getValue("tdkw_delasticcount");
            //用工
            List<String> entryentity = BzSelect.dentryentityset();
            if (elasticcontrol.equals("1")){
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        this.getModel().setValue(bs + "num", elasticcount + integer);
                    }
                }
            }else if (elasticcontrol.equals("2")){
                BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                        BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                        this.getModel().setValue(bs + "num", bigDecimalv.add(bigDecimalm));
                    }
                }
            }
        }

        //职级  f
        if ((StringUtils.equals("tdkw_felasticcount", name) || StringUtils.equals("tdkw_felasticcontrol", name))
                &&(this.getModel().getValue("tdkw_felasticcount") != null && this.getModel().getValue("tdkw_felasticcontrol") != null)) {
            //调整后弹性方式
            String elasticcontrol = (String) this.getModel().getValue("tdkw_felasticcontrol");
            //调整后弹性额度
            Integer elasticcount = (Integer) this.getModel().getValue("tdkw_felasticcount");
            //用工
            List<String> entryentity = BzSelect.fentryentityset();
            if (elasticcontrol.equals("1")){
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        this.getModel().setValue(bs + "num", elasticcount + integer);
                    }
                }
            }else if (elasticcontrol.equals("2")){
                BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                for (String bs : entryentity) {
                    if (this.getModel().getValue(bs) != null) {
                        Integer integer = (Integer) this.getModel().getValue(bs);
                        BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                        BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                        this.getModel().setValue(bs + "num", bigDecimalv.add(bigDecimalm));
                    }
                }
            }
        }

        //当为弹性编制时 --使用组织
        if (BzSelect.bentryentityset().contains(name) && this.getModel().getValue("tdkw_bcontrolstrategy")!=null &&this.getModel().getValue("tdkw_bcontrolstrategy").equals("3")){//使用组织
            if (this.getModel().getValue("tdkw_belasticcontrol") != null && this.getModel().getValue("tdkw_belasticcount") != null && this.getModel().getValue(name)!=null){
                //调整后弹性方式
                String belasticcontrol = (String) this.getModel().getValue("tdkw_belasticcontrol");
                //调整后弹性额度
                Integer tdkw_belasticcount = (Integer) this.getModel().getValue("tdkw_belasticcount");
                if (belasticcontrol.equals("1")) {//人数
                        Integer integer = (Integer) this.getModel().getValue(name);
                        if (name.equals("tdkw_bhxj2")){
                            this.getModel().setValue("tdkw_byearstaffnu", tdkw_belasticcount + integer);
                        }else {
                            this.getModel().setValue(name + "num", tdkw_belasticcount + integer);
                        }
                } else if (belasticcontrol.equals("2")) {//百分比
                    BigDecimal bigDecimal = BigDecimal.valueOf(tdkw_belasticcount);
                    BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                        Integer integer = (Integer) this.getModel().getValue(name);
                        BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                        BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                        if (name.equals("tdkw_bhxj2")){
                            this.getModel().setValue("tdkw_byearstaffnu", bigDecimalv.add(bigDecimalm));
                        }else {
                            this.getModel().setValue(name + "num", bigDecimalv.add(bigDecimalm));
                        }
                }
            }
        }

        //当为弹性编制时 --岗位
        if (BzSelect.centryentityset().contains(name)&& this.getModel().getValue("tdkw_ccontrolstrategy")!=null && this.getModel().getValue("tdkw_ccontrolstrategy").equals("3")){
            if (this.getModel().getValue("tdkw_celasticcount") != null && this.getModel().getValue("tdkw_celasticcontrol") != null && this.getModel().getValue(name)!=null){
                //调整后弹性方式
                String elasticcontrol = (String) this.getModel().getValue("tdkw_celasticcontrol");
                //调整后弹性额度
                Integer elasticcount = (Integer) this.getModel().getValue("tdkw_celasticcount");
                Integer integer = (Integer) this.getModel().getValue(name);
                if (elasticcontrol.equals("1")){
                    this.getModel().setValue(name + "num", elasticcount + integer);
                }else if (elasticcontrol.equals("2")){
                    BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                    BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                    BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                    BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                    this.getModel().setValue(name + "num", bigDecimalv.add(bigDecimalm));
                }
            }

        }


        //当为弹性编制时 -- 职位
        if (BzSelect.dentryentityset().contains(name) && this.getModel().getValue("tdkw_dcontrolstrategy")!=null && this.getModel().getValue("tdkw_dcontrolstrategy").equals("3")){
            if (this.getModel().getValue("tdkw_delasticcount") != null && this.getModel().getValue("tdkw_delasticcontrol") != null && this.getModel().getValue(name)!=null){
                //调整后弹性方式
                String elasticcontrol = (String) this.getModel().getValue("tdkw_delasticcontrol");
                //调整后弹性额度
                Integer elasticcount = (Integer) this.getModel().getValue("tdkw_delasticcount");
                Integer integer = (Integer) this.getModel().getValue(name);
                if (elasticcontrol.equals("1")){
                    this.getModel().setValue(name + "num", elasticcount + integer);
                }else if (elasticcontrol.equals("2")){
                    BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                    BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                    BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                    BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                    this.getModel().setValue(name + "num", bigDecimalv.add(bigDecimalm));
                }
            }
        }


        //当为弹性编制时 -- 用工
        if (BzSelect.eentryentityset().contains(name) && this.getModel().getValue("tdkw_econtrolstrategy")!=null && this.getModel().getValue("tdkw_econtrolstrategy").equals("3") ){
            List<String> eentryentityset = BzSelect.eentryentityset();
            //弹性控编时
            if (this.getModel().getValue("tdkw_eelasticcount") != null && this.getModel().getValue("tdkw_eelasticcontrol") != null && this.getModel().getValue(name)!=null){
                //调整后弹性方式
                String elasticcontrol = (String) this.getModel().getValue("tdkw_eelasticcontrol");
                //调整后弹性额度
                Integer elasticcount = (Integer) this.getModel().getValue("tdkw_eelasticcount");
                Integer integer = (Integer) this.getModel().getValue(name);
                if (elasticcontrol.equals("1")){
                    this.getModel().setValue(name + "num", elasticcount + integer);
                }else if (elasticcontrol.equals("2")){
                    BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                    BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                    BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                    BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                    this.getModel().setValue(name + "num", bigDecimalv.add(bigDecimalm));
                }
            }
        }


        //当为弹性编制时 -- 职级
        if (BzSelect.fentryentityset().contains(name)&& this.getModel().getValue("tdkw_fcontrolstrategy")!=null && this.getModel().getValue("tdkw_fcontrolstrategy").equals("3")){
            if (this.getModel().getValue("tdkw_felasticcount") != null && this.getModel().getValue("tdkw_felasticcontrol") != null && this.getModel().getValue(name)!=null){
                //调整后弹性方式
                String elasticcontrol = (String) this.getModel().getValue("tdkw_felasticcontrol");
                //调整后弹性额度
                Integer elasticcount = (Integer) this.getModel().getValue("tdkw_felasticcount");
                Integer integer = (Integer) this.getModel().getValue(name);
                if (elasticcontrol.equals("1")){
                    this.getModel().setValue(name + "num", elasticcount + integer);
                }else if (elasticcontrol.equals("2")){
                    BigDecimal bigDecimal = BigDecimal.valueOf(elasticcount);
                    BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
                    BigDecimal bigDecimalv = BigDecimal.valueOf(integer);
                    BigDecimal bigDecimalm = bigDecimalv.multiply(divide).setScale(0, BigDecimal.ROUND_DOWN);
                    this.getModel().setValue(name + "num", bigDecimalv.add(bigDecimalm));
                }
            }
        }

    }
}
