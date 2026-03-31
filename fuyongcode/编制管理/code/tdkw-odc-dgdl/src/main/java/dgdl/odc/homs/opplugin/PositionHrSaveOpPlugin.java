package dgdl.odc.homs.opplugin;

import com.alibaba.druid.util.StringUtils;
import dgdl.odc.homs.common.ExceptionUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;

import java.util.Arrays;

/**
 * @author WQG
 * @version 1.0
 * @date 2023/6/6 13:32
 * @description: 实际岗位保存默认岗位类型
 * 2020.6.19 增加虚拟组织  不允许 增加实际岗位
 **/
public class PositionHrSaveOpPlugin extends AbstractOperationServicePlugIn {
    private static Log logger = LogFactory.getLog(PositionHrSaveOpPlugin.class);


    private final static String ORGDESIGNBU = "orgdesignbu";//组织规划责任单位
    private final static String DGDL_NAME = "dgdl_name";//实际岗位名称
    private final static String NAME = "name";//实际岗位名称
    private final static String DGDL_JOBNAME = "dgdl_jobname";//标准岗位名称
    private final static String DGDL_ADMINORGNAME = "dgdl_adminorgname";//所属行政组织名称
    private final static String DGDL_POSITIONNAME = "dgdl_positionname";//职衔名称
    private final static String JOB = "job";//标准岗位
    private final static String DGDL_INDICIA_CATEGORY = "dgdl_indicia_category";//职衔类别
    private final static String DGDL_POSITION = "dgdl_position";//职衔
    private final static String[] CATEGORYNAME = new String[]{"职能支持类","工程技术类","项目管理类"};//技术类职衔类别名称

    private final static String QCJTNUMBER = "hr-000002";//吉利汽车集团编码


    private final static String[] INDICIACATEGORYNAMEKEY = new String[]{"管理类-","类","设计类","架构师类"};//职衔类别名称关键字

    private final static String DGDL_ISPRINCIPAL = "dgdl_isprincipal";//是否核心岗位

    private final static String DGDL_EXTERNAL_DISPATCH = "dgdl_external_dispatch";//是否对外派驻

    private final static String ISLEADER = "isleader";//是否组织负责人

    private final static String ISSENSITIVE = "issensitive";//是否敏感岗位

    private final static String DGDL_CONCEAL_ADDRESSBOOK = "dgdl_conceal_addressbook";//是否隐藏通讯录

    private final static String DGDL_JOBLABEL = "dgdl_joblabel";//岗位标签

    private final static String DGDL_POST_AISLE = "dgdl_post_aisle";//岗位通道

    private final static String DGDL_JOBPROPERTY = "dgdl_jobproperty";//岗位属性

    private final static String JOBKEY = "岗";//标准岗位关键字

    /**
     * 是否虚拟组织
     */
    private  static final String DGDL_ISVIRTUAL_EXT="dgdl_isvirtual_ext";
    /**
     * 所属组织
     */
    private  static final String ADMINORG="adminorg";

    //编码
    private  static final String NUMBER = "number";
    //原岗位编码
    private  static final String DGDL_OLDNUMBER = "dgdl_oldnumber";

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add(ORGDESIGNBU);
        e.getFieldKeys().add("positiontype");
        e.getFieldKeys().add(DGDL_JOBNAME);
        e.getFieldKeys().add(DGDL_POSITIONNAME);
        e.getFieldKeys().add(DGDL_ADMINORGNAME);
        e.getFieldKeys().add(DGDL_NAME);
        e.getFieldKeys().add(NAME);
        e.getFieldKeys().add(ADMINORG);
        e.getFieldKeys().add(JOB);
        e.getFieldKeys().add(DGDL_POSITION);
        e.getFieldKeys().add(NUMBER);
        e.getFieldKeys().add(DGDL_OLDNUMBER);//原实际岗位编码
        e.getFieldKeys().add("dgdl_oldorgnum_ext");//原组织编码
        e.getFieldKeys().add("dgdl_oldcompan_ext");//原公司编码
        e.getFieldKeys().add("dgdl_common_ext");//原组织岗位编码
        e.getFieldKeys().add("adminorg.dgdl_isvirtual_ext");//是否虚拟

        e.getFieldKeys().add(DGDL_ISPRINCIPAL);
        e.getFieldKeys().add(DGDL_EXTERNAL_DISPATCH);
        e.getFieldKeys().add(ISLEADER);
        e.getFieldKeys().add(ISSENSITIVE);
        e.getFieldKeys().add(DGDL_CONCEAL_ADDRESSBOOK);
        e.getFieldKeys().add(DGDL_JOBLABEL);
        e.getFieldKeys().add(DGDL_POST_AISLE);
        e.getFieldKeys().add(DGDL_JOBPROPERTY);
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        super.onAddValidators(e);
        e.addValidator(new PositionValidator());
    }

    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        super.beforeExecuteOperationTransaction(e);
        //实体对象
        for (DynamicObject obj : e.getDataEntities()) {
            //查询岗位类型
            DynamicObject[] positionTypes = BusinessDataServiceHelper.load("hbpm_positiontype", "id", null);
            if (positionTypes.length > 0) {
                DynamicObject positionType = BusinessDataServiceHelper.loadSingle(positionTypes[0].getPkValue(), "hbpm_positiontype");
                obj.set("positiontype", positionType);
            }
            //获取所属组织
            DynamicObject adminorg = obj.getDynamicObject(ADMINORG);
            HRBaseServiceHelper orgHelper = new HRBaseServiceHelper("homs_adminorgdetail");
            if (adminorg!=null){
                Long orgid = adminorg.getLong("id");
                adminorg = orgHelper.queryOne("id,number,name,dgdl_isvirtual_ext,dgdl_oldcompan_ext,dgdl_oldorgnum_ext", orgid);
                if (adminorg.getBoolean("dgdl_isvirtual_ext")){
                    e.setCancel(true);
                    String message="虚拟组织"+adminorg.getString("number")+"下不能创建岗位";
                    this.operationResult.addErrorInfo(ExceptionUtil.buildErrMessage(obj, new KDBizException(message)));
                }
                //赋值原公司编码
                obj.set("dgdl_oldcompan_ext", adminorg.getString("dgdl_oldcompan_ext"));
                //赋值原组织编码
                obj.set("dgdl_oldorgnum_ext", adminorg.getString("dgdl_oldorgnum_ext"));
            }

        }
    }

    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        super.beginOperationTransaction(e);
        logger.info("beginOperationTransaction原实际岗位编码不自动赋值");
        for (int i = 0; i < e.getDataEntities().length; i++) {
            
        }
        for (DynamicObject obj : e.getDataEntities()) {
            obj.set(DGDL_NAME, obj.getString(NAME));
//            String oldNumber = obj.getString(DGDL_OLDNUMBER);
//            if (StringUtils.isEmpty(oldNumber)){
//                obj.set(DGDL_OLDNUMBER, obj.getString(NUMBER));
//            }
            //是否对外派驻
            String externalDispatch = obj.getString(DGDL_EXTERNAL_DISPATCH);
            if (StringUtils.isEmpty(externalDispatch)){
                obj.set(DGDL_EXTERNAL_DISPATCH,"N");
            }
            //是否组织负责人
            String isLeader = obj.getString(ISLEADER);
            if (StringUtils.isEmpty(isLeader)){
                obj.set(ISLEADER,"2");
            }
            //是否敏感岗位
            String isSensitive = obj.getString(ISSENSITIVE);
            if (StringUtils.isEmpty(isSensitive)){
                obj.set(ISSENSITIVE,"N");
            }
            //是否核心岗位
            String concealAddressBook = obj.getString(DGDL_CONCEAL_ADDRESSBOOK);
            if (StringUtils.isEmpty(concealAddressBook)){
                obj.set(DGDL_CONCEAL_ADDRESSBOOK,"N");
            }
            //标准岗位
            DynamicObject jobObj = obj.getDynamicObject(JOB);
            if (jobObj != null){
                QFilter filter = new QFilter("id", QCP.equals,jobObj.getPkValue())
                        .and("iscurrentversion", QCP.equals,true);
                //重新查询标准岗位
                DynamicObject jobHrObj = BusinessDataServiceHelper.loadSingleFromCache("hjm_jobhr", "dgdl_corejob,dgdl_joblabel,dgdl_post_aisle,dgdl_jobproperty,dgdl_indicia_category", filter.toArray());
                //是否核心岗位
                String isPrincipal = obj.getString(DGDL_ISPRINCIPAL);
                if (StringUtils.isEmpty(isPrincipal)){
                    obj.set(DGDL_ISPRINCIPAL,jobHrObj.getString("dgdl_corejob"));
                }
                //岗位标签
                DynamicObjectCollection jobLabel = obj.getDynamicObjectCollection(DGDL_JOBLABEL);
                if (jobLabel.size() == 0){
                    obj.set(DGDL_JOBLABEL,jobHrObj.getDynamicObjectCollection(DGDL_JOBLABEL));
                }
                //岗位通道
                DynamicObject postAisle = obj.getDynamicObject(DGDL_POST_AISLE);
                if (postAisle == null){
                    obj.set(DGDL_POST_AISLE,jobHrObj.getDynamicObject(DGDL_POST_AISLE));
                }
                //岗位属性
                DynamicObject jobProperty = obj.getDynamicObject(DGDL_JOBPROPERTY);
                if (jobProperty == null){
                    obj.set(DGDL_JOBPROPERTY,jobHrObj.getDynamicObject(DGDL_JOBPROPERTY));
                }
            }
            //根据不同的职衔类别拼接实际岗位名称
            this.setPositionName(obj);
        }
    }

    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        super.afterExecuteOperationTransaction(e);
        for (DynamicObject obj : e.getDataEntities()) {
            //赋值原组织岗位编码（原公司编码+原组织编码+原实际岗位编码）
            String oldcompanExt = obj.getString("dgdl_oldcompan_ext");
            String dgdlOldorgnumExt = obj.getString("dgdl_oldorgnum_ext");
            String string = obj.getString(DGDL_OLDNUMBER);
            String common = oldcompanExt + dgdlOldorgnumExt;
            if (kd.bos.dataentity.utils.StringUtils.isNotBlank(string)) {
                common = common +string;
            }
            logger.info("原组织岗位编码"+common);
            //赋值原组织岗位编码
            obj.set("dgdl_common_ext",common);
            SaveServiceHelper.update(obj);
        }
    }

    /**
     * @Author WQG
     * @Description //根据不同的职衔类别拼接实际岗位名称
     * @Date 10:42 2023/5/10
     * @return
     **/
    private void setPositionName(DynamicObject obj){
        //组织规划责任单位
        DynamicObject orgDesignBu = obj.getDynamicObject(ORGDESIGNBU);
        //标准岗位
        DynamicObject job = obj.getDynamicObject(JOB);
        //职衔
        DynamicObject position = obj.getDynamicObject(DGDL_POSITION);
        //行政组织
        DynamicObject adminOrg = obj.getDynamicObject(ADMINORG);
        if (orgDesignBu != null &&  QCJTNUMBER.equals(orgDesignBu.getString("number")) && job != null && position != null && adminOrg != null){
            //职衔类别
            DynamicObject category = job.getDynamicObject(DGDL_INDICIA_CATEGORY);
            if (category != null){
                //职衔类别名称
                String categoryName = category.getString("name");
                //标准岗位名称
                String jobName = job.getString("name");
                //职衔名称
                String positionName = position.getString("name");
                //行政组织名称
                String adminOrgName = adminOrg.getString("name");
                obj.set(DGDL_POSITIONNAME,positionName);
                obj.set(DGDL_ADMINORGNAME,adminOrgName);
                obj.set(DGDL_JOBNAME,jobName);
                //是否包含技术类
                if (Arrays.toString(CATEGORYNAME).contains(categoryName)){
                    //标准岗位名称"岗"所在下标
//                    int indexOf = jobName.indexOf(JOBKEY);
//                    if (indexOf == -1 && b){
//                        this.getView().showMessage("请选择标准岗位名称最后一个字为'岗'的标准岗位");
//                        return;
//                    }
                    //分割标准岗位名称
                    String substring = jobName.substring(0, jobName.length()-1);
                    //设置实际岗位名称 = 标准岗位名称去"岗"+职衔名称
                    obj.set(NAME,substring+positionName);
                    obj.set(DGDL_NAME,substring+positionName);
                    //是否设计类
                } else if (StringUtils.equals(INDICIACATEGORYNAMEKEY[2],categoryName)) {
                    //设置实际岗位名称 = 职衔名称+标准岗位名称
                    obj.set(NAME,positionName+jobName);
                    obj.set(DGDL_NAME,positionName+jobName);
                    //是否架构师类
                } else if (StringUtils.equals(INDICIACATEGORYNAMEKEY[3],categoryName)) {
                    if (jobName.length()-3 <= 0 && !StringUtils.isEmpty(jobName) && !StringUtils.isEmpty(positionName)){
                        throw new KDBizException("请选择标准岗位名称大于3个字的标准岗位");
                    }
                    //分割标准岗位名称
                    String substring = jobName.substring(0, jobName.length()-3);
                    //设置实际岗位名称 = 标准岗位名称去后三字+职衔名称
                    obj.set(NAME,substring+positionName);
                    obj.set(DGDL_NAME,substring+positionName);
                    //是否管理类
                } else if (categoryName.contains(INDICIACATEGORYNAMEKEY[0])) {
                    //设置实际岗位名称 = 组织名称+职衔名称
                    obj.set(NAME,adminOrgName+positionName);
                    obj.set(DGDL_NAME,adminOrgName+positionName);
                    //其他类
                }else {
                    //设置实际岗位名称 = 组织名称+职衔名称
                    obj.set(NAME,jobName);
                    obj.set(DGDL_NAME,jobName);
                }
            }
        }
    }

}
