package dgdl.odc.homs.opplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static dgdl.odc.homs.common.DateTimeCommon.getDateStr;

/**
 * @Author：CW
 * @version：1.0
 * @date：14:50
 * @description: 年度编制计划 保存和启用时校验: 同一编制规划单位，编制计划之间开始月份与结束月份不得有交集
 */
public class CompilationPlanSaveAndEnableOp extends AbstractOperationServicePlugIn {
    private static Log logger = LogFactory.getLog(CompilationPlanSaveAndEnableOp.class);

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("dgdl_org");
        e.getFieldKeys().add("dgdl_startmonth");
        e.getFieldKeys().add("dgdl_endmonth");
        e.getFieldKeys().add("dgdl_year");
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        // 添加自定义的校验器
        e.addValidator(new CompilationPlanSaveAndEnableOpVal());
    }
}

/*
 * 自定义校验器
 * */
class CompilationPlanSaveAndEnableOpVal extends AbstractValidator {
    private static final Log logger = LogFactory.getLog(CompilationPlanSaveAndEnableOpVal.class.getName());

    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        for (ExtendedDataEntity dataEntity : dataEntities) {
            DynamicObject obj = dataEntity.getDataEntity();//单据对象
            String billKey = obj.getDataEntityType().getName();//获取当前单据标识
            String number = obj.getString("number");
            //获取编制规划单位
            DynamicObject dgdlOrg = obj.getDynamicObject("dgdl_org");
            long orgId = (long) dgdlOrg.getPkValue();
            //获取开始月份
            Date startMonth = obj.getDate("dgdl_startmonth");
            int startMonthStr = Integer.parseInt(getDateStr(startMonth));
            //获取结束月份
            Date endMonth = obj.getDate("dgdl_endmonth");
            int endMonthStr = Integer.parseInt(getDateStr(endMonth));
            //获取所属年度
            Date year = obj.getDate("dgdl_year");
            //查询当前编制规划单位所有计划
            QFilter[] filters = new QFilter[] {new QFilter("dgdl_year", QCP.equals,year).
                    and("dgdl_org.id", QCP.equals, orgId).
                    and("number",QCP.not_equals,number).
                    and("iscurrentversion",QCP.equals,"1")};
            DynamicObject[] objects = BusinessDataServiceHelper.load(billKey, "id,number,dgdl_startmonth,dgdl_endmonth", filters);
            logger.info("CompilationPlanSaveAndEnableOpVal.objects.length:"+objects.length);
            List<String> numbers = Arrays.asList(objects).stream().map(planMonthObj -> planMonthObj.getString("number")).collect(Collectors.toList());
            if(numbers.size()>0){
                logger.info("CompilationPlanSaveAndEnableOpVal.numbers:"+ SerializationUtils.toJsonString(numbers));
            }
            //校验同一编制规划单位，编制计划之间开始月份与结束月份不得有交集
            for (DynamicObject object : objects) {
                Date oldStartMonth = object.getDate("dgdl_startmonth");
                int oldStartMonthStr = Integer.parseInt(getDateStr(oldStartMonth));
                Date oldEndMonth = object.getDate("dgdl_endmonth");
                int oldEndMonthStr = Integer.parseInt(getDateStr(oldEndMonth));
                //校验
                if (startMonthStr >= oldStartMonthStr && startMonthStr <= oldEndMonthStr){
                    logger.info("CompilationPlanSaveAndEnableOpVal.单据编号:"+object.getString("number"));
                    String str = String.valueOf(startMonthStr);
                    String substring4 = str.substring(str.length()-2,str.length());
                    addErrorMessage(dataEntity, "已存在"+substring4+"月编制数据！");
                    //"已存在当前年度"+substring4+"月的编制数据,单据编号为【"+object.getString("number")+"】"
                    return;
                }
                if (endMonthStr >= oldStartMonthStr && endMonthStr <= oldEndMonthStr){
                    logger.info("CompilationPlanSaveAndEnableOpVal.单据编号:"+object.getString("number"));
                    String str = String.valueOf(endMonthStr);
                    String substring4 = str.substring(str.length()-2,str.length());
                    addErrorMessage(dataEntity, "已存在"+substring4+"月编制数据！");
                    //"已存在当前年度"+substring4+"月的编制数据,单据编号为【"+object.getString("number")+"】"
                    return;
                }
            }
        }
    }
}
