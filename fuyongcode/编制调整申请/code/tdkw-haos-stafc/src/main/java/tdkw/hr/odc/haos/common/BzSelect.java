package tdkw.hr.odc.haos.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName : BzSelect
 * @Description :
 * @Author : XYP
 * @Date: 2024-07-25 17:10
 */
public class BzSelect {

    /**
     * 使用组织
     *
     * @return
     */
    public static List<String> bentryentity() {
        List<String> list = new ArrayList<>();
//        list.add("id");
//        list.add("pid");

        list.add("buseorg");//使用组织
        list.add("bdutyorg");//责任组织
        list.add("blevel");//物理层级
        list.add("bstructlongnumber");//组织长编码
        list.add("bhavesubentry");//是否存在下级
        list.add("bparentlongname");//上级组织长名称
        list.add("bcontrolstrategy");//控编方式
        list.add("bstaffdimension");//编制维度
        list.add("belasticcontrol");//弹性方式
        list.add("belasticcount");//弹性额度
        //实际人数
        list.add("brealnumwithsub");//含下级
        list.add("bdirectnum");//直属
        //编制人数
        list.add("byearstaffnumwithsub");//含下级
        list.add("bstaffnumwithsub");//已分配额度
        list.add("byearstaff");//直属
        //直属
//        list.add("bhalfyearstaff1");//上半年
//        list.add("bhalfyearstaff2");//下半年
//        list.add("bquarterstaff1");//第一季度
//        list.add("bquarterstaff2");//第二季度
//        list.add("bquarterstaff3");//第三季度
//        list.add("bquarterstaff4");//第四季度
        list.add("bmonthstaff1");//1月
        list.add("bmonthstaff2");//2月
        list.add("bmonthstaff3");//3月
        list.add("bmonthstaff4");//4月
        list.add("bmonthstaff5");//5月
        list.add("bmonthstaff6");//6月
        list.add("bmonthstaff7");//7月
        list.add("bmonthstaff8");//8月
        list.add("bmonthstaff9");//9月
        list.add("bmonthstaff10");//10月
        list.add("bmonthstaff11");//11月
        list.add("bmonthstaff12");//12月

        //控编人数
        list.add("byearstaffnumwithsubnum");//含下级
        list.add("bstaffnumwithsubnum");//已分配额度
        list.add("byearstaffnum");//直属
        //直属
//        list.add("bhalfyearstaff1num");//上半年
//        list.add("bhalfyearstaff2num");//下半年
//        list.add("bquarterstaff1num");//第一季度
//        list.add("bquarterstaff2num");//第二季度
//        list.add("bquarterstaff3num");//第三季度
//        list.add("bquarterstaff4num");//第四季度
        list.add("bmonthstaff1num");//1月
        list.add("bmonthstaff2num");//2月
        list.add("bmonthstaff3num");//3月
        list.add("bmonthstaff4num");//4月
        list.add("bmonthstaff5num");//5月
        list.add("bmonthstaff6num");//6月
        list.add("bmonthstaff7num");//7月
        list.add("bmonthstaff8num");//8月
        list.add("bmonthstaff9num");//9月
        list.add("bmonthstaff10num");//10月
        list.add("bmonthstaff11num");//11月
        list.add("bmonthstaff12num");//12月
        return list;
    }


    /**
     * 用工关系
     *
     * @return
     */
    public static List<String> eentryentity() {
        List<String> list = new ArrayList<>();
        list.add("elaborreltype");//用工关系类型名称
        list.add("econtrolstrategy");//控编方式
        list.add("eelasticcontrol");//弹性方式
        list.add("eelasticcount");//弹性额度
        list.add("eyearstaff");//编制人数

//        list.add("ehalfyearstaff1");//上半年
//        list.add("ehalfyearstaff2");//下半年
//        list.add("equarterstaff1");//第一季度
//        list.add("equarterstaff2");//第二季度
//        list.add("equarterstaff3");//第三季度
//        list.add("equarterstaff4");//第四季度

        list.add("emonthstaff1");//1月
        list.add("emonthstaff2");//2月
        list.add("emonthstaff3");//3月
        list.add("emonthstaff4");//4月
        list.add("emonthstaff5");//5月
        list.add("emonthstaff6");//6月
        list.add("emonthstaff7");//7月
        list.add("emonthstaff8");//8月
        list.add("emonthstaff9");//9月
        list.add("emonthstaff10");//10月
        list.add("emonthstaff11");//11月
        list.add("emonthstaff12");//12月

        list.add("eyearstaffnum");//控编人数

//        list.add("ehalfyearstaff1num");//上半年
//        list.add("ehalfyearstaff2num");//下半年
//        list.add("equarterstaff1num");//第一季度
//        list.add("equarterstaff2num");//第二季度
//        list.add("equarterstaff3num");//第三季度
//        list.add("equarterstaff4num");//第四季度

        list.add("emonthstaff1num");//1月
        list.add("emonthstaff2num");//2月
        list.add("emonthstaff3num");//3月
        list.add("emonthstaff4num");//4月
        list.add("emonthstaff5num");//5月
        list.add("emonthstaff6num");//6月
        list.add("emonthstaff7num");//7月
        list.add("emonthstaff8num");//8月
        list.add("emonthstaff9num");//9月
        list.add("emonthstaff10num");//10月
        list.add("emonthstaff11num");//11月
        list.add("emonthstaff12num");//12月
        return list;
    }

    /**
     * 岗位
     *
     * @return
     */
    public static List<String> centryentity() {
        List<String> list = new ArrayList<>();
        list.add("cdutyworkrole");//岗位名称
        list.add("ccontrolstrategy");//控编方式
        list.add("celasticcontrol");//弹性方式
        list.add("celasticcount");//弹性额度
        list.add("cyearstaff");//编制人数

//        list.add("chalfyearstaff1");//上半年
//        list.add("chalfyearstaff2");//下半年
//        list.add("cquarterstaff1");//第一季度
//        list.add("cquarterstaff2");//第二季度
//        list.add("cquarterstaff3");//第三季度
//        list.add("cquarterstaff4");//第四季度

        list.add("cmonthstaff1");//1月
        list.add("cmonthstaff2");//2月
        list.add("cmonthstaff3");//3月
        list.add("cmonthstaff4");//4月
        list.add("cmonthstaff5");//5月
        list.add("cmonthstaff6");//6月
        list.add("cmonthstaff7");//7月
        list.add("cmonthstaff8");//8月
        list.add("cmonthstaff9");//9月
        list.add("cmonthstaff10");//10月
        list.add("cmonthstaff11");//11月
        list.add("cmonthstaff12");//12月

        list.add("cyearstaffnum");//控编人数

//        list.add("chalfyearstaff1num");//上半年
//        list.add("chalfyearstaff2num");//下半年
//        list.add("cquarterstaff1num");//第一季度
//        list.add("cquarterstaff2num");//第二季度
//        list.add("cquarterstaff3num");//第三季度
//        list.add("cquarterstaff4num");//第四季度

        list.add("cmonthstaff1num");//1月
        list.add("cmonthstaff2num");//2月
        list.add("cmonthstaff3num");//3月
        list.add("cmonthstaff4num");//4月
        list.add("cmonthstaff5num");//5月
        list.add("cmonthstaff6num");//6月
        list.add("cmonthstaff7num");//7月
        list.add("cmonthstaff8num");//8月
        list.add("cmonthstaff9num");//9月
        list.add("cmonthstaff10num");//10月
        list.add("cmonthstaff11num");//11月
        list.add("cmonthstaff12num");//12月
        return list;
    }

    /**
     * 岗位
     *
     * @return
     */
    public static List<String> dentryentity() {
        List<String> list = new ArrayList<>();
        list.add("djob");//岗位名称
        list.add("dcontrolstrategy");//控编方式
        list.add("delasticcontrol");//弹性方式
        list.add("delasticcount");//弹性额度
        list.add("dyearstaff");//编制人数

//        list.add("dhalfyearstaff1");//上半年
//        list.add("dhalfyearstaff2");//下半年
//        list.add("dquarterstaff1");//第一季度
//        list.add("dquarterstaff2");//第二季度
//        list.add("dquarterstaff3");//第三季度
//        list.add("dquarterstaff4");//第四季度

        list.add("dmonthstaff1");//1月
        list.add("dmonthstaff2");//2月
        list.add("dmonthstaff3");//3月
        list.add("dmonthstaff4");//4月
        list.add("dmonthstaff5");//5月
        list.add("dmonthstaff6");//6月
        list.add("dmonthstaff7");//7月
        list.add("dmonthstaff8");//8月
        list.add("dmonthstaff9");//9月
        list.add("dmonthstaff10");//10月
        list.add("dmonthstaff11");//11月
        list.add("dmonthstaff12");//12月

        list.add("dyearstaffnum");//控编人数

//        list.add("dhalfyearstaff1num");//上半年
//        list.add("dhalfyearstaff2num");//下半年
//        list.add("dquarterstaff1num");//第一季度
//        list.add("dquarterstaff2num");//第二季度
//        list.add("dquarterstaff3num");//第三季度
//        list.add("dquarterstaff4num");//第四季度

        list.add("dmonthstaff1num");//1月
        list.add("dmonthstaff2num");//2月
        list.add("dmonthstaff3num");//3月
        list.add("dmonthstaff4num");//4月
        list.add("dmonthstaff5num");//5月
        list.add("dmonthstaff6num");//6月
        list.add("dmonthstaff7num");//7月
        list.add("dmonthstaff8num");//8月
        list.add("dmonthstaff9num");//9月
        list.add("dmonthstaff10num");//10月
        list.add("dmonthstaff11num");//11月
        list.add("dmonthstaff12num");//12月
        return list;
    }


    /**
     * 职级
     *
     * @return
     */
    public static List<String> fentryentity() {
        List<String> list = new ArrayList<>();
        list.add("fbasicdata1");//职级名称
        list.add("fcontrolstrategy");//控编方式
        list.add("felasticcontrol");//弹性方式
        list.add("felasticcount");//弹性额度
        list.add("fyearstaff");//编制人数

        list.add("fhalfyearstaff1");//上半年
        list.add("fhalfyearstaff2");//下半年
        list.add("fquarterstaff1");//第一季度
        list.add("fquarterstaff2");//第二季度
        list.add("fquarterstaff3");//第三季度
        list.add("fquarterstaff4");//第四季度

        list.add("fmonthstaff1");//1月
        list.add("fmonthstaff2");//2月
        list.add("fmonthstaff3");//3月
        list.add("fmonthstaff4");//4月
        list.add("fmonthstaff5");//5月
        list.add("fmonthstaff6");//6月
        list.add("fmonthstaff7");//7月
        list.add("fmonthstaff8");//8月
        list.add("fmonthstaff9");//9月
        list.add("fmonthstaff10");//10月
        list.add("fmonthstaff11");//11月
        list.add("fmonthstaff12");//12月

        list.add("fyearstaffnum");//控编人数

        list.add("fhalfyearstaff1num");//上半年
        list.add("fhalfyearstaff2num");//下半年
        list.add("fquarterstaff1num");//第一季度
        list.add("fquarterstaff2num");//第二季度
        list.add("fquarterstaff3num");//第三季度
        list.add("fquarterstaff4num");//第四季度

        list.add("fmonthstaff1num");//1月
        list.add("fmonthstaff2num");//2月
        list.add("fmonthstaff3num");//3月
        list.add("fmonthstaff4num");//4月
        list.add("fmonthstaff5num");//5月
        list.add("fmonthstaff6num");//6月
        list.add("fmonthstaff7num");//7月
        list.add("fmonthstaff8num");//8月
        list.add("fmonthstaff9num");//9月
        list.add("fmonthstaff10num");//10月
        list.add("fmonthstaff11num");//11月
        list.add("fmonthstaff12num");//12月
        return list;
    }


    /**
     * 使用组织
     *
     * @return
     */
    public static List<String> bentryentityOp() {
        List<String> list = new ArrayList<>();
        list.add("bcontrolstrategy");//控编方式
        list.add("belasticcontrol");//弹性方式
        list.add("belasticcount");//弹性额度

        //编制人数
//        list.add("byearstaffnumwithsub");//含下级 -------tdkw_bhxj2
        list.add("bstaffnumwithsub");//已分配额度
        list.add("byearstaff");//直属
        //直属
//        list.add("bhalfyearstaff1");//上半年
//        list.add("bhalfyearstaff2");//下半年
//        list.add("bquarterstaff1");//第一季度
//        list.add("bquarterstaff2");//第二季度
//        list.add("bquarterstaff3");//第三季度
//        list.add("bquarterstaff4");//第四季度
        list.add("bmonthstaff1");//1月
        list.add("bmonthstaff2");//2月
        list.add("bmonthstaff3");//3月
        list.add("bmonthstaff4");//4月
        list.add("bmonthstaff5");//5月
        list.add("bmonthstaff6");//6月
        list.add("bmonthstaff7");//7月
        list.add("bmonthstaff8");//8月
        list.add("bmonthstaff9");//9月
        list.add("bmonthstaff10");//10月
        list.add("bmonthstaff11");//11月
        list.add("bmonthstaff12");//12月

        //控编人数
//        list.add("byearstaffnumwithsubnum");//含下级   ----------tdkw_byearstaffnu
        list.add("bstaffnumwithsubnum");//已分配额度
        list.add("byearstaffnum");//直属
        //直属
//        list.add("bhalfyearstaff1num");//上半年
//        list.add("bhalfyearstaff2num");//下半年
//        list.add("bquarterstaff1num");//第一季度
//        list.add("bquarterstaff2num");//第二季度
//        list.add("bquarterstaff3num");//第三季度
//        list.add("bquarterstaff4num");//第四季度
        list.add("bmonthstaff1num");//1月
        list.add("bmonthstaff2num");//2月
        list.add("bmonthstaff3num");//3月
        list.add("bmonthstaff4num");//4月
        list.add("bmonthstaff5num");//5月
        list.add("bmonthstaff6num");//6月
        list.add("bmonthstaff7num");//7月
        list.add("bmonthstaff8num");//8月
        list.add("bmonthstaff9num");//9月
        list.add("bmonthstaff10num");//10月
        list.add("bmonthstaff11num");//11月
        list.add("bmonthstaff12num");//12月
        return list;
    }


    /**
     * 岗位
     *
     * @return
     */
    public static List<String> centryentityOp() {
        List<String> list = new ArrayList<>();
        list.add("ccontrolstrategy");//控编方式
        list.add("celasticcontrol");//弹性方式
        list.add("celasticcount");//弹性额度
        list.add("cyearstaff");//编制人数

//        list.add("chalfyearstaff1");//上半年
//        list.add("chalfyearstaff2");//下半年
//        list.add("cquarterstaff1");//第一季度
//        list.add("cquarterstaff2");//第二季度
//        list.add("cquarterstaff3");//第三季度
//        list.add("cquarterstaff4");//第四季度

        list.add("cmonthstaff1");//1月
        list.add("cmonthstaff2");//2月
        list.add("cmonthstaff3");//3月
        list.add("cmonthstaff4");//4月
        list.add("cmonthstaff5");//5月
        list.add("cmonthstaff6");//6月
        list.add("cmonthstaff7");//7月
        list.add("cmonthstaff8");//8月
        list.add("cmonthstaff9");//9月
        list.add("cmonthstaff10");//10月
        list.add("cmonthstaff11");//11月
        list.add("cmonthstaff12");//12月

        list.add("cyearstaffnum");//控编人数

//        list.add("chalfyearstaff1num");//上半年
//        list.add("chalfyearstaff2num");//下半年
//        list.add("cquarterstaff1num");//第一季度
//        list.add("cquarterstaff2num");//第二季度
//        list.add("cquarterstaff3num");//第三季度
//        list.add("cquarterstaff4num");//第四季度

        list.add("cmonthstaff1num");//1月
        list.add("cmonthstaff2num");//2月
        list.add("cmonthstaff3num");//3月
        list.add("cmonthstaff4num");//4月
        list.add("cmonthstaff5num");//5月
        list.add("cmonthstaff6num");//6月
        list.add("cmonthstaff7num");//7月
        list.add("cmonthstaff8num");//8月
        list.add("cmonthstaff9num");//9月
        list.add("cmonthstaff10num");//10月
        list.add("cmonthstaff11num");//11月
        list.add("cmonthstaff12num");//12月
        return list;
    }


    /**
     * 职位
     *
     * @return
     */
    public static List<String> dentryentityOp() {
        List<String> list = new ArrayList<>();
        list.add("dcontrolstrategy");//控编方式
        list.add("delasticcontrol");//弹性方式
        list.add("delasticcount");//弹性额度
        list.add("dyearstaff");//编制人数

//        list.add("dhalfyearstaff1");//上半年
//        list.add("dhalfyearstaff2");//下半年
//        list.add("dquarterstaff1");//第一季度
//        list.add("dquarterstaff2");//第二季度
//        list.add("dquarterstaff3");//第三季度
//        list.add("dquarterstaff4");//第四季度

        list.add("dmonthstaff1");//1月
        list.add("dmonthstaff2");//2月
        list.add("dmonthstaff3");//3月
        list.add("dmonthstaff4");//4月
        list.add("dmonthstaff5");//5月
        list.add("dmonthstaff6");//6月
        list.add("dmonthstaff7");//7月
        list.add("dmonthstaff8");//8月
        list.add("dmonthstaff9");//9月
        list.add("dmonthstaff10");//10月
        list.add("dmonthstaff11");//11月
        list.add("dmonthstaff12");//12月

        list.add("dyearstaffnum");//控编人数

//        list.add("dhalfyearstaff1num");//上半年
//        list.add("dhalfyearstaff2num");//下半年
//        list.add("dquarterstaff1num");//第一季度
//        list.add("dquarterstaff2num");//第二季度
//        list.add("dquarterstaff3num");//第三季度
//        list.add("dquarterstaff4num");//第四季度

        list.add("dmonthstaff1num");//1月
        list.add("dmonthstaff2num");//2月
        list.add("dmonthstaff3num");//3月
        list.add("dmonthstaff4num");//4月
        list.add("dmonthstaff5num");//5月
        list.add("dmonthstaff6num");//6月
        list.add("dmonthstaff7num");//7月
        list.add("dmonthstaff8num");//8月
        list.add("dmonthstaff9num");//9月
        list.add("dmonthstaff10num");//10月
        list.add("dmonthstaff11num");//11月
        list.add("dmonthstaff12num");//12月
        return list;
    }


    /**
     * 用工
     *
     * @return
     */
    public static List<String> eentryentityOp() {
        List<String> list = new ArrayList<>();
        list.add("econtrolstrategy");//控编方式
        list.add("eelasticcontrol");//弹性方式
        list.add("eelasticcount");//弹性额度
        list.add("eyearstaff");//编制人数

//        list.add("ehalfyearstaff1");//上半年
//        list.add("ehalfyearstaff2");//下半年
//        list.add("equarterstaff1");//第一季度
//        list.add("equarterstaff2");//第二季度
//        list.add("equarterstaff3");//第三季度
//        list.add("equarterstaff4");//第四季度

        list.add("emonthstaff1");//1月
        list.add("emonthstaff2");//2月
        list.add("emonthstaff3");//3月
        list.add("emonthstaff4");//4月
        list.add("emonthstaff5");//5月
        list.add("emonthstaff6");//6月
        list.add("emonthstaff7");//7月
        list.add("emonthstaff8");//8月
        list.add("emonthstaff9");//9月
        list.add("emonthstaff10");//10月
        list.add("emonthstaff11");//11月
        list.add("emonthstaff12");//12月

        list.add("eyearstaffnum");//控编人数

//        list.add("ehalfyearstaff1num");//上半年
//        list.add("ehalfyearstaff2num");//下半年
//        list.add("equarterstaff1num");//第一季度
//        list.add("equarterstaff2num");//第二季度
//        list.add("equarterstaff3num");//第三季度
//        list.add("equarterstaff4num");//第四季度

        list.add("emonthstaff1num");//1月
        list.add("emonthstaff2num");//2月
        list.add("emonthstaff3num");//3月
        list.add("emonthstaff4num");//4月
        list.add("emonthstaff5num");//5月
        list.add("emonthstaff6num");//6月
        list.add("emonthstaff7num");//7月
        list.add("emonthstaff8num");//8月
        list.add("emonthstaff9num");//9月
        list.add("emonthstaff10num");//10月
        list.add("emonthstaff11num");//11月
        list.add("emonthstaff12num");//12月
        return list;

    }


    /**
     * 职级
     *
     * @return
     */
    public static List<String> fentryentityOp() {
        List<String> list = new ArrayList<>();
        list.add("fcontrolstrategy");//控编方式
        list.add("felasticcontrol");//弹性方式
        list.add("felasticcount");//弹性额度
        list.add("fyearstaff");//编制人数

//        list.add("fhalfyearstaff1");//上半年
//        list.add("fhalfyearstaff2");//下半年
//        list.add("fquarterstaff1");//第一季度
//        list.add("fquarterstaff2");//第二季度
//        list.add("fquarterstaff3");//第三季度
//        list.add("fquarterstaff4");//第四季度

        list.add("fmonthstaff1");//1月
        list.add("fmonthstaff2");//2月
        list.add("fmonthstaff3");//3月
        list.add("fmonthstaff4");//4月
        list.add("fmonthstaff5");//5月
        list.add("fmonthstaff6");//6月
        list.add("fmonthstaff7");//7月
        list.add("fmonthstaff8");//8月
        list.add("fmonthstaff9");//9月
        list.add("fmonthstaff10");//10月
        list.add("fmonthstaff11");//11月
        list.add("fmonthstaff12");//12月

        list.add("fyearstaffnum");//控编人数

//        list.add("fhalfyearstaff1num");//上半年
//        list.add("fhalfyearstaff2num");//下半年
//        list.add("fquarterstaff1num");//第一季度
//        list.add("fquarterstaff2num");//第二季度
//        list.add("fquarterstaff3num");//第三季度
//        list.add("fquarterstaff4num");//第四季度

        list.add("fmonthstaff1num");//1月
        list.add("fmonthstaff2num");//2月
        list.add("fmonthstaff3num");//3月
        list.add("fmonthstaff4num");//4月
        list.add("fmonthstaff5num");//5月
        list.add("fmonthstaff6num");//6月
        list.add("fmonthstaff7num");//7月
        list.add("fmonthstaff8num");//8月
        list.add("fmonthstaff9num");//9月
        list.add("fmonthstaff10num");//10月
        list.add("fmonthstaff11num");//11月
        list.add("fmonthstaff12num");//12月
        return list;

    }


    /**
     * 使用组织
     *
     * @return
     */
    public static List<String> bentryentityset() {
        List<String> list = new ArrayList<>();
//        list.add("tdkw_bhxj2");//含下级
        list.add("tdkw_bstaffnumwithsub");//已分配额度
        list.add("tdkw_byearstaff");//直属
        //直属
        list.add("tdkw_bmonthstaff1");//1月
        list.add("tdkw_bmonthstaff2");//2月
        list.add("tdkw_bmonthstaff3");//3月
        list.add("tdkw_bmonthstaff4");//4月
        list.add("tdkw_bmonthstaff5");//5月
        list.add("tdkw_bmonthstaff6");//6月
        list.add("tdkw_bmonthstaff7");//7月
        list.add("tdkw_bmonthstaff8");//8月
        list.add("tdkw_bmonthstaff9");//9月
        list.add("tdkw_bmonthstaff10");//10月
        list.add("tdkw_bmonthstaff11");//11月
        list.add("tdkw_bmonthstaff12");//12月
        return list;
    }

    /**
     * 用工关系
     *
     * @return
     */
    public static List<String> eentryentityset() {
        List<String> list = new ArrayList<>();
        list.add("tdkw_emonthstaff1");//1月
        list.add("tdkw_emonthstaff2");//2月
        list.add("tdkw_emonthstaff3");//3月
        list.add("tdkw_emonthstaff4");//4月
        list.add("tdkw_emonthstaff5");//5月
        list.add("tdkw_emonthstaff6");//6月
        list.add("tdkw_emonthstaff7");//7月
        list.add("tdkw_emonthstaff8");//8月
        list.add("tdkw_emonthstaff9");//9月
        list.add("tdkw_emonthstaff10");//10月
        list.add("tdkw_emonthstaff11");//11月
        list.add("tdkw_emonthstaff12");//12月
        list.add("tdkw_eyearstaff");//调整后编制人数
        return list;
    }

    /**
     * 岗位
     *
     * @return
     */
    public static List<String> centryentityset() {
        List<String> list = new ArrayList<>();
        list.add("tdkw_cmonthstaff1");//1月
        list.add("tdkw_cmonthstaff2");//2月
        list.add("tdkw_cmonthstaff3");//3月
        list.add("tdkw_cmonthstaff4");//4月
        list.add("tdkw_cmonthstaff5");//5月
        list.add("tdkw_cmonthstaff6");//6月
        list.add("tdkw_cmonthstaff7");//7月
        list.add("tdkw_cmonthstaff8");//8月
        list.add("tdkw_cmonthstaff9");//9月
        list.add("tdkw_cmonthstaff10");//10月
        list.add("tdkw_cmonthstaff11");//11月
        list.add("tdkw_cmonthstaff12");//12月
        list.add("tdkw_cyearstaff");//调整后编制人数
        return list;
    }


    /**
     * 职位
     *
     * @return
     */
    public static List<String> dentryentityset() {
        List<String> list = new ArrayList<>();
        list.add("tdkw_dmonthstaff1");//1月
        list.add("tdkw_dmonthstaff2");//2月
        list.add("tdkw_dmonthstaff3");//3月
        list.add("tdkw_dmonthstaff4");//4月
        list.add("tdkw_dmonthstaff5");//5月
        list.add("tdkw_dmonthstaff6");//6月
        list.add("tdkw_dmonthstaff7");//7月
        list.add("tdkw_dmonthstaff8");//8月
        list.add("tdkw_dmonthstaff9");//9月
        list.add("tdkw_dmonthstaff10");//10月
        list.add("tdkw_dmonthstaff11");//11月
        list.add("tdkw_dmonthstaff12");//12月
        list.add("tdkw_dyearstaff");//调整后编制人数
        return list;
    }

    /**
     * 职级
     *
     * @return
     */
    public static List<String> fentryentityset() {
        List<String> list = new ArrayList<>();
        list.add("tdkw_fmonthstaff1");//1月
        list.add("tdkw_fmonthstaff2");//2月
        list.add("tdkw_fmonthstaff3");//3月
        list.add("tdkw_fmonthstaff4");//4月
        list.add("tdkw_fmonthstaff5");//5月
        list.add("tdkw_fmonthstaff6");//6月
        list.add("tdkw_fmonthstaff7");//7月
        list.add("tdkw_fmonthstaff8");//8月
        list.add("tdkw_fmonthstaff9");//9月
        list.add("tdkw_fmonthstaff10");//10月
        list.add("tdkw_fmonthstaff11");//11月
        list.add("tdkw_fmonthstaff12");//12月
        list.add("tdkw_fyearstaff");//调整后编制人数
        return list;
    }
}
