package dgdl.odc.homs.validator;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.Objects;

/**
 * 年度编制计划校验规划单位创建不能存在已启用的
 *
 * @version 1.0
 * @author: yaoshuai
 * @date:2023/12/26
 */
public class PlanYearSaveValidator extends AbstractValidator {

    private static Log logger = LogFactory.getLog(PlanYearSaveValidator.class);

    /**
     * 年度编制计划
     */
    private static final String PLANYEAR = "dgdl_planyear";

    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        logger.info("PlanYearSaveValidator开始执行校验操作");
        for (ExtendedDataEntity entity : dataEntities) {
            //获取当前实体
            DynamicObject dataEntity = entity.getDataEntity();
            //获取编制规划单位
            DynamicObject org = dataEntity.getDynamicObject("dgdl_org");
            if (Objects.nonNull(org)) {
                String orgNumber = org.getString("number");
                QFilter qFilter = new QFilter("dgdl_org.number", QCP.equals, orgNumber)
                        .and("enable", QCP.equals, "1")
                        .and("iscurrentversion", QCP.equals, "1")
                        .and("datastatus", QCP.equals, "1")
                        ;
                logger.info("PlanYearSaveValidator查询语句" + qFilter);
                DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(PLANYEAR, "id", qFilter.toArray());
                if (Objects.nonNull(dynamicObject)) {
                    throw new KDBizException("已存在相同已启用的编制规划单位的年度编制计划,请重新选择");
                }
            }
        }
    }
}
