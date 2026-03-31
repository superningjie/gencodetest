package dgdl.odc.homs.opplugin;

import dgdl.odc.homs.common.DateTimeCommon;
import dgdl.odc.homs.util.OldCompanyAndDeptNumValidUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.StringUtils;

import java.util.Arrays;
import java.util.Date;

public class HomsAdminOrgdetSaveOp extends AbstractOperationServicePlugIn {
    private static Log log = LogFactory.getLog(HomsAdminOrgdetSaveOp.class);    //日志


    //字段监听
    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        //是否虚拟组织
        e.getFieldKeys().add("dgdl_isvirtual_ext");
        e.getFieldKeys().add("adminorgtype");
        e.getFieldKeys().add("parentorg");
        e.getFieldKeys().add("dgdl_oldcompan_ext");
        e.getFieldKeys().add("dgdl_oldorgnum_ext");
        e.getFieldKeys().add("dgdl_common_ext");
        e.getFieldKeys().add("number");
    }

    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        super.beforeExecuteOperationTransaction(e);
        //实体对象
        for (DynamicObject obj : e.getDataEntities()) {
            //行政组织导入时自动刷新层级
            setAdminorglayer(obj);
            setCostcenter(obj);

            //获取当前填写的行政组织
            DynamicObject adminorgtypeDy = obj.getDynamicObject("adminorgtype");
            //获取当前填写的是否虚拟组织
            Boolean dgdl_isvirtual_ext1 = obj.getBoolean("dgdl_isvirtual_ext");

            if (adminorgtypeDy != null  &&  !dgdl_isvirtual_ext1) {
                String adminorgtypeNumber = adminorgtypeDy.getString("number");
                //获取上级行政组织
                DynamicObject parentorg = obj.getDynamicObject("parentorg");
                if (parentorg != null) {
                    //获取上级行政组织是否虚拟组织
                    boolean dgdl_isvirtual_ext = parentorg.getBoolean("dgdl_isvirtual_ext");
                    if (dgdl_isvirtual_ext) {
                        parentorg = parentorg.getDynamicObject("parentorg");
                        parentorg = BusinessDataServiceHelper.loadSingleFromCache(parentorg.getPkValue(), "haos_adminorgbatch");
                    }
                    //获取上级行政组织的类型
                    DynamicObject adminorgtype = parentorg.getDynamicObject("adminorgtype");
                    String typeNumber = adminorgtype.getString("number");
                    String typeName = adminorgtype.getString("name");
                    String[] arr = null;
                    switch (typeNumber) {
                        //集团
                        case "1010_S":
                            arr = new String[]{"1011_S", "1012_S", "1020_S", "1021_S", "1040_S"};
                            if (!Arrays.asList(arr).contains(adminorgtypeNumber)){
                                throw new KDBizException("行政组织类型字段值填写错误请重新填写");
                            }
                            break;
                        //子集团
                        case "1011_S":
                            arr = new String[]{ "1012_S", "1020_S", "1021_S","1022_S", "1040_S", "1050_S"};
                            if (!Arrays.asList(arr).contains(adminorgtypeNumber)){
                                throw new KDBizException("行政组织类型字段值填写错误请重新填写");
                            }
                            break;
                        //业务板块
                        case "1012_S":
                            arr = new String[]{"1020_S", "1022_S", "1040_S", "1050_S"};
                            if (!Arrays.asList(arr).contains(adminorgtypeNumber)){
                                throw new KDBizException("行政组织类型字段值填写错误请重新填写");
                            }
                            break;
                        //公司
                        case "1020_S":
                            arr = new String[]{"1022_S", "1040_S", "1050_S", "1060_S","1025_S","1070_S"};
                            if (!Arrays.asList(arr).contains(adminorgtypeNumber)){
                                throw new KDBizException("行政组织类型字段值填写错误请重新填写");
                            }
                            break;
                        //子公司
                        case "1025_S":
                            //职能中心
                        case "1021_S":
                            //业务中心
                        case "1022_S":
                            arr = new String[]{"1040_S", "1060_S", "1070_S"};
                            if (!Arrays.asList(arr).contains(adminorgtypeNumber)){
                                throw new KDBizException("行政组织类型字段值填写错误请重新填写");
                            }
                            break;
                        //部门
                        case "1040_S":
                            //项目组
                        case "1050_S":
                            arr = new String[]{"1060_S", "1070_S", "1080_S"};
                            if (!Arrays.asList(arr).contains(adminorgtypeNumber)){
                                throw new KDBizException("行政组织类型字段值填写错误请重新填写");
                            }
                            break;
                        //科室
                        case "1060_S":
                            //模块
                        case "1070_S":
                            //工段
                        case "1080_S":
                            arr = new String[]{"1090_S","1200_S"};
                            if (!Arrays.asList(arr).contains(adminorgtypeNumber)){
                                throw new KDBizException("行政组织类型字段值填写错误请重新填写");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            //校验原公司，组织编码唯一性
            //获取编码
            String number2 = obj.getString("number");
            //获取原公司编码
            String oldCompany = obj.getString("dgdl_oldcompan_ext");
            //获取原组织编码
            String oldOrgNum = obj.getString("dgdl_oldorgnum_ext");
            //原公司，组织字段优化逻辑
            if (adminorgtypeDy != null){
                //获取行政组织类型
                DynamicObject adminorgtype = obj.getDynamicObject("adminorgtype");
                if (adminorgtype != null) {
                    //获取行政组织类型归属
                    DynamicObject adminorgtypestd = adminorgtype.getDynamicObject("adminorgtypestd");
                    if (adminorgtypestd != null) {
                        String number = adminorgtypestd.getString("number");
                        log.info("获取行政组织类型归属"+number);
                        //判断类型归属是否部门
                        if ("1040_S".equals(number)) {
                            log.info("进入判断类型归属是否部门"+number);
                            //获取上级行政组织
                            DynamicObject parentOrg = obj.getDynamicObject("parentorg");
                            if (parentOrg != null) {
                                String number1 = parentOrg.getString("number");
                                oldCompany = getOldcompany(number1);
                                log.info("获取原公司编码"+oldCompany);
                                obj.set("dgdl_oldcompan_ext",oldCompany);
                            }
                        }
                    }
                }
                //保存原公司组织编码
                log.info("获取原公司编码"+oldCompany+oldOrgNum);
                obj.set("dgdl_common_ext",oldCompany+oldOrgNum);
            }
            //查询是否存在原公司，组织编码唯一性
            String validResult = OldCompanyAndDeptNumValidUtil.validAll(number2, oldCompany, oldOrgNum);

            log.info("validResult:"+validResult);

            if (validResult!=null) {
                e.setCancelMessage(validResult);
                e.setCancel(true);
//                throw new KDBizException(validResult);
            }
        }
    }

    /**
     * 查询原公司编码
     * @param number 组织编码
     */
    private String getOldcompany(String number) {
        //查询上级组织
        QFilter[] filters = new QFilter("number", QCP.equals, number).and("datastatus",QCP.equals,"1").and("iscurrentversion",QCP.equals,"1").toArray();
        DynamicObject homsAdminorgdetail = BusinessDataServiceHelper.loadSingle("homs_adminorgdetail", "id,dgdl_oldcompan_ext,number,name,parentorg,adminorgtype", filters);
        if (homsAdminorgdetail != null) {
            //获取行政组织类型
            DynamicObject adminorgtype = homsAdminorgdetail.getDynamicObject("adminorgtype");
            //获取行政组织类型归属
            DynamicObject adminorgtypestd = adminorgtype.getDynamicObject("adminorgtypestd");
            String number1 = adminorgtypestd.getString("number");
            log.info("获取行政组织类型归属"+number1);
            //判断类型归属是否公司或集团
            if ("1020_S".equals(number1) || "1010_S".equals(number1)) {
                String dgdlOldcompanExt = homsAdminorgdetail.getString("dgdl_oldcompan_ext");
                log.info("原公司编码"+dgdlOldcompanExt);
                return dgdlOldcompanExt;
            }else {
                //获取上级行政组织
                DynamicObject parentOrg = homsAdminorgdetail.getDynamicObject("parentorg");
                if (parentOrg != null){
                    String number2 = parentOrg.getString("number");
                    return getOldcompany(number2);
                }
            }
        }
        return null;

    }

    /**
     * 导入给成本中心赋值
     * */
    public void setCostcenter(DynamicObject obj){
        DynamicObject parentorg =obj.getDynamicObject("parentorg");
        //获取是否继承
        String dgdlIsinheritExt =obj.getString("dgdl_isinherit_ext");

        if (parentorg!=null && !StringUtils.isEmpty(dgdlIsinheritExt) && dgdlIsinheritExt.equals("01")){
            String number = parentorg.getString("number");
            //根据值查询对于的基础资料
            QFilter[] adminorglayerFilter = new QFilter("number", QCP.equals, number).and("iscurrentversion",QCP.equals,true).toArray();
            DynamicObject adminorglayerObj = BusinessDataServiceHelper.loadSingleFromCache("haos_adminorgdetail", adminorglayerFilter);
            //获取成本中心
            DynamicObject costcenternameDy = adminorglayerObj.getDynamicObject("dgdl_costcenna_ext");


            if (costcenternameDy!=null){
                String costcenterNum = costcenternameDy.getString("number");
                //成本中心开始时间
                Date dgdl_begindate_ext = adminorglayerObj.getDate("dgdl_begindate_ext");
                //成本中心结束时间
                Date dgdl_enddate_ext = adminorglayerObj.getDate("dgdl_enddate_ext");

                String enddate="2199-12-31";
                //给当前单据的成本中心编码名称赋值
                obj.set("dgdl_costcenna_ext",costcenternameDy);
                obj.set("dgdl_costcentn_ext",costcenterNum);
//                obj.set("dgdl_begindate_ext",dgdl_begindate_ext);
                obj.set("dgdl_enddate_ext", DateTimeCommon.ConvertToDate(enddate));
            }
        }else{
            DynamicObject dgdl_costcenna_ext = obj.getDynamicObject("dgdl_costcenna_ext");
            if (dgdl_costcenna_ext!=null){
                String costcenterNum = dgdl_costcenna_ext.getString("number");
                obj.set("dgdl_costcentn_ext",costcenterNum);
            }
        }

    }


    public void setAdminorglayer(DynamicObject obj){
        //是否虚拟组织
        boolean dgdl_isvirtualorg =obj.getBoolean("dgdl_isvirtual_ext");
        log.info("组织发文判断组织新设是否虚拟组织："+dgdl_isvirtualorg);
        //获取上级行政组织
        DynamicObject parentorgDy = obj.getDynamicObject("parentorg");
        log.info("组织发文判断组织新设上级行政组织："+parentorgDy);
        //规划组织
//        DynamicObject org = obj.getDynamicObject("org");
        int a = 1;
        //如果上级是虚拟组织则继续向上查询本级不计数,
        if (!dgdl_isvirtualorg) {
            while (parentorgDy != null) {
                //虚拟组织--则继续向上查询
//                String number = parentorgDy.getString("number");
                QFilter filter = new QFilter("id", QCP.equals, parentorgDy.getPkValue());
                DynamicObject[] haos_adminorgf7s = BusinessDataServiceHelper.load("homs_adminorgdetail", "id,number,parentorg,dgdl_isvirtual_ext,org", filter.toArray());
                log.info("组织发文判断组织新设haos_adminorgf7s："+haos_adminorgf7s.length);
                if (haos_adminorgf7s != null && haos_adminorgf7s.length > 0) {
                    if (!haos_adminorgf7s[0].getBoolean("dgdl_isvirtual_ext")){
                        a++;
                    }
                    parentorgDy = haos_adminorgf7s[0].getDynamicObject("parentorg");
                }else {
                    parentorgDy = null;
                }
            }
            String number = "01";
            if (a < 10){
                number = "0"+String.valueOf(a);
            }else {
                number = String.valueOf(a);
            }
            //根据值查询对于的基础资料
            QFilter[] adminorglayerFilter = new QFilter("number", QCP.equals, number).toArray();
            DynamicObject adminorglayerObj = BusinessDataServiceHelper.loadSingleFromCache("haos_adminorglayer", adminorglayerFilter);
            //给行政组织层级赋值
            obj.set("adminorglayer", adminorglayerObj);
        }else if(dgdl_isvirtualorg){
            //情况行政组织层级字段值
            QFilter[] adminorglayerFilter = new QFilter("number", QCP.equals, "99").toArray();
            //查询虚拟组织
            DynamicObject adminorglayerObj = BusinessDataServiceHelper.loadSingleFromCache("haos_adminorglayer", adminorglayerFilter);
            obj.set("adminorglayer", adminorglayerObj);
        }

    }
}
