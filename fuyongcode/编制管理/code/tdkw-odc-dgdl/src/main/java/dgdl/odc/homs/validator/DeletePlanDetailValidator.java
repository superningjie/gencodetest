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

import java.util.ArrayList;
import java.util.List;


/**
 * @Author: yaoshuai
 * @CreateTime: 2024-02-20  10:00
 * @Description: 删除编制详情, 校验不是最末级组织不能删除
 */
public class DeletePlanDetailValidator extends AbstractValidator {

    private static Log logger = LogFactory.getLog(DeletePlanDetailValidator.class);

    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        for (ExtendedDataEntity dataEntitie : dataEntities) {
            DynamicObject dataEntity = dataEntitie.getDataEntity();
            //编制计划
            DynamicObject planyear = dataEntity.getDynamicObject("dgdl_planyear");
            //末端组织
            DynamicObject adminorg = dataEntity.getDynamicObject("adminorg");
            String lastName = adminorg.getString("number");
            logger.info("DeletePlanDetailValidator末端组织编码=" + lastName);
            //组织层级
            String hierarchy = planyear.getString("dgdl_orghierarchy");
            int intHierarchy = Integer.parseInt(hierarchy);
            logger.info("DeletePlanDetailValidator编制计划层级=" + intHierarchy);
            String adminStr = "dgdl_adminorg";
            List<String> adminList = new ArrayList<>();
            for (int i = 1; i <= intHierarchy; i++) {
                DynamicObject admin = dataEntity.getDynamicObject(adminStr + i);
                adminList.add(adminStr + i);
                if (admin.getString("number").equals(lastName)) {
                    break;
                }
            }
            logger.info("DeletePlanDetailValidator要匹配的集合=" + adminList);
            //上级行政组织
            String selectName = adminList.get(adminList.size() - 1);
            logger.info("DeletePlanDetailValidator要匹配的行政组织=" + selectName);

            QFilter qFilter = new QFilter("dgdl_planyear.id", QCP.equals, planyear.getPkValue())
                    .and(selectName+".number", QCP.equals, lastName);
            logger.info("DeletePlanDetailValidator查询语句=" + qFilter);
            DynamicObject[] load = BusinessDataServiceHelper.load("dgdl_planyear_detail", "id", qFilter.toArray());
            logger.info("DeletePlanDetailValidator查询长度=" + load.length);
            if (load.length > 1) {
                throw new KDBizException("当前组织还存在下级组织,无法删除");
            }
        }
    }
}
