package dgdl.odc.homs.opplugin;

import com.alibaba.druid.util.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;

import java.util.Arrays;

/**
 * @author WQG
 * @version 1.0
 * @date 2023/8/1 14:35
 * @description: 导入变更
 **/
public class PositionHrChangeOpPlugin extends AbstractOperationServicePlugIn {

    private static Log logger = LogFactory.getLog(PositionHrChangeOpPlugin.class);

    private final static String NUMBER = "number";//编码
    private final static String ORGDESIGNBU = "orgdesignbu";//组织规划责任单位
    private final static String DGDL_NAME = "dgdl_name";//实际岗位名称
    private final static String NAME = "name";//实际岗位名称
    private final static String DGDL_JOBNAME = "dgdl_jobname";//标准岗位名称
    private final static String DGDL_ADMINORGNAME = "dgdl_adminorgname";//所属行政组织名称
    private final static String DGDL_POSITIONNAME = "dgdl_positionname";//职衔名称
    private final static String JOB = "job";//标准岗位
    private final static String DGDL_INDICIA_CATEGORY = "dgdl_indicia_category";//职衔类别
    private final static String DGDL_POSITION = "dgdl_position";//职衔
    private final static String[] CATEGORYNAME = new String[]{"职能支持类", "工程技术类", "项目管理类"};//技术类职衔类别名称

    private final static String QCJTNUMBER = "hr-000002";//吉利汽车集团编码


    private final static String[] INDICIACATEGORYNAMEKEY = new String[]{"管理类-", "类", "设计类", "架构师类"};//职衔类别名称关键字

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
     * 所属组织
     */
    private static final String ADMINORG = "adminorg";

    private static final String SELECTPROPERTIES = "id,name,number,orgdesignbu,dgdl_name,adminorg,dgdl_isprincipal,dgdl_external_dispatch,dgdl_conceal_addressbook,isleader,dgdl_indicia_category,dgdl_post_aisle,dgdl_jobproperty,dgdl_position,dgdl_joblabel,job,issensitive,dgdl_positionname,dgdl_adminorgname,dgdl_jobname";

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add(ORGDESIGNBU);
        e.getFieldKeys().add(DGDL_JOBNAME);
        e.getFieldKeys().add(NAME);
        e.getFieldKeys().add(DGDL_POSITIONNAME);
        e.getFieldKeys().add(DGDL_ADMINORGNAME);
        e.getFieldKeys().add(DGDL_NAME);
        e.getFieldKeys().add(ADMINORG);
        e.getFieldKeys().add(JOB);
        e.getFieldKeys().add(DGDL_POSITION);
        e.getFieldKeys().add(NUMBER);
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
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        logger.info("PositionHrChangeOpPlugin导入");
        super.endOperationTransaction(e);
        for (DynamicObject obj : e.getDataEntities()) {
            HRBaseServiceHelper positionHrServiceHelper = new HRBaseServiceHelper("homs_position");
            QFilter qFilter = new QFilter(NUMBER, QCP.equals, obj.getString(NUMBER)).and("iscurrentversion", QCP.equals, true);
            DynamicObject positionObj = BusinessDataServiceHelper.loadSingleFromCache("homs_position", SELECTPROPERTIES, qFilter.toArray());
            positionObj.set(DGDL_NAME, obj.getString(NAME));
            positionObj.set(NAME, obj.getString(NAME));
            logger.info("PositionHrChangeOpPlugin实际岗位名称=" + obj.getString(NAME));
            //是否对外派驻
            String externalDispatch = obj.getString(DGDL_EXTERNAL_DISPATCH);
            if (StringUtils.isEmpty(externalDispatch)) {
                positionObj.set(DGDL_EXTERNAL_DISPATCH, "N");
            }
            //是否组织负责人
            String isLeader = obj.getString(ISLEADER);
            if (StringUtils.isEmpty(isLeader)) {
                positionObj.set(ISLEADER, "2");
            }
            //是否敏感岗位
            String isSensitive = obj.getString(ISSENSITIVE);
            if (StringUtils.isEmpty(isSensitive)) {
                positionObj.set(ISSENSITIVE, "N");
            }
            //是否核心岗位
            String concealAddressBook = obj.getString(DGDL_CONCEAL_ADDRESSBOOK);
            if (StringUtils.isEmpty(concealAddressBook)) {
                positionObj.set(DGDL_CONCEAL_ADDRESSBOOK, "N");
            }
            //标准岗位
            DynamicObject jobObj = obj.getDynamicObject(JOB);
            if (jobObj != null) {
                QFilter filter = new QFilter("id", QCP.equals, jobObj.getPkValue())
                        .and("iscurrentversion", QCP.equals, true);
                //重新查询标准岗位
                DynamicObject jobHrObj = BusinessDataServiceHelper.loadSingleFromCache("hjm_jobhr", "dgdl_corejob,dgdl_joblabel,dgdl_post_aisle,dgdl_jobproperty,dgdl_indicia_category", filter.toArray());
                //是否核心岗位
                String isPrincipal = obj.getString(DGDL_ISPRINCIPAL);
                if (StringUtils.isEmpty(isPrincipal)) {
                    positionObj.set(DGDL_ISPRINCIPAL, jobHrObj.getString("dgdl_corejob"));
                }
                //岗位标签
                DynamicObjectCollection jobLabel = obj.getDynamicObjectCollection(DGDL_JOBLABEL);
                if (jobLabel.size() == 0) {
                    positionObj.set(DGDL_JOBLABEL, jobHrObj.getDynamicObjectCollection(DGDL_JOBLABEL));
                }
                //岗位通道
                DynamicObject postAisle = obj.getDynamicObject(DGDL_POST_AISLE);
                if (postAisle == null) {
                    positionObj.set(DGDL_POST_AISLE, jobHrObj.getDynamicObject(DGDL_POST_AISLE));
                }
                //岗位属性
                DynamicObject jobProperty = obj.getDynamicObject(DGDL_JOBPROPERTY);
                if (jobProperty == null) {
                    positionObj.set(DGDL_JOBPROPERTY, jobHrObj.getDynamicObject(DGDL_JOBPROPERTY));
                }
            }
            //根据不同的职衔类别拼接实际岗位名称
//            this.setPositionName(positionObj);
            logger.info("PositionHrChangeOpPlugin导入变更不在修改实际岗位名称");
            positionHrServiceHelper.saveOne(positionObj);
        }
    }


    /**
     * @return
     * @Author WQG
     * @Description //根据不同的职衔类别拼接实际岗位名称
     * @Date 10:42 2023/5/10
     **/
    private void setPositionName(DynamicObject obj) {
        //组织规划责任单位
        DynamicObject orgDesignBu = obj.getDynamicObject(ORGDESIGNBU);
        //标准岗位
        DynamicObject job = obj.getDynamicObject(JOB);
        //职衔
        DynamicObject position = obj.getDynamicObject(DGDL_POSITION);
        //行政组织
        DynamicObject adminOrg = obj.getDynamicObject(ADMINORG);
        if (orgDesignBu != null && QCJTNUMBER.equals(orgDesignBu.getString("number")) && job != null && position != null && adminOrg != null) {
            //职衔类别
            DynamicObject category = job.getDynamicObject(DGDL_INDICIA_CATEGORY);
            if (category != null) {
                //职衔类别名称
                String categoryName = category.getString("name");
                //标准岗位名称
                String jobName = job.getString("name");
                //职衔名称
                String positionName = position.getString("name");
                //行政组织名称
                String adminOrgName = adminOrg.getString("name");
                obj.set(DGDL_POSITIONNAME, positionName);
                obj.set(DGDL_ADMINORGNAME, adminOrgName);
                obj.set(DGDL_JOBNAME, jobName);
                //是否包含技术类
                if (Arrays.toString(CATEGORYNAME).contains(categoryName)) {
                    //标准岗位名称"岗"所在下标
//                    int indexOf = jobName.indexOf(JOBKEY);
//                    if (indexOf == -1 && b){
//                        this.getView().showMessage("请选择标准岗位名称最后一个字为'岗'的标准岗位");
//                        return;
//                    }
                    //分割标准岗位名称
                    String substring = jobName.substring(0, jobName.length() - 1);
                    //设置实际岗位名称 = 标准岗位名称去"岗"+职衔名称
                    obj.set(NAME, substring + positionName);
                    obj.set(DGDL_NAME, substring + positionName);
                    //是否设计类
                } else if (StringUtils.equals(INDICIACATEGORYNAMEKEY[2], categoryName)) {
                    //设置实际岗位名称 = 职衔名称+标准岗位名称
                    obj.set(NAME, positionName + jobName);
                    obj.set(DGDL_NAME, positionName + jobName);
                    //是否架构师类
                } else if (StringUtils.equals(INDICIACATEGORYNAMEKEY[3], categoryName)) {
                    if (jobName.length() - 3 <= 0 && !StringUtils.isEmpty(jobName) && !StringUtils.isEmpty(positionName)) {
                        throw new KDBizException("请选择标准岗位名称大于3个字的标准岗位");
                    }
                    //分割标准岗位名称
                    String substring = jobName.substring(0, jobName.length() - 3);
                    //设置实际岗位名称 = 标准岗位名称去后三字+职衔名称
                    obj.set(NAME, substring + positionName);
                    obj.set(DGDL_NAME, substring + positionName);
                    //是否管理类
                } else if (categoryName.contains(INDICIACATEGORYNAMEKEY[0])) {
                    //设置实际岗位名称 = 组织名称+职衔名称
                    obj.set(NAME, adminOrgName + positionName);
                    obj.set(DGDL_NAME, adminOrgName + positionName);
                    //其他类
                } else {
                    //设置实际岗位名称 = 组织名称+职衔名称
                    obj.set(NAME, jobName);
                    obj.set(DGDL_NAME, jobName);
                }
            }
        }
    }

}
