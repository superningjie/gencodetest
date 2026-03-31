package tdkw.hrmp.hrobs.formplugin.empresumemapping;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.property.BasedataProp;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.form.ControlAp;
import kd.bos.metadata.form.FormMetadata;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @Metadata： tdkw_empresumemapping
 * @Description ： ES配置字段表
 * @ClassName ：EmpResumeMappingFormPlugin
 * @author xxx
 * @Date ：2023/6/26 9:30
 * @Version: 1.0
 */
public class EmpResumeMappingFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener {
    public EmpResumeMappingUtils emUtils = new EmpResumeMappingUtils();
    public Map<String, String> fieldType = new HashMap<>();

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        ChangeData[] changeSet = e.getChangeSet();
        if (StringUtils.equals(name, "tdkw_chooseentity")) {
            DynamicObject newValue = (DynamicObject) changeSet[0].getNewValue();
            this.getModel().deleteEntryData("tdkw_esentry");
            if (newValue != null) {
                String entityKey = newValue.getString("number");
                this.getModel().setValue("tdkw_entitykey", entityKey);
                String entityName = newValue.getString("name");
                this.getModel().setValue("tdkw_entityname", entityName);
                Map<String, String> allField = getAllField(entityKey);
                for (String key : allField.keySet()) {
                    int newEntryRow = this.getModel().createNewEntryRow("tdkw_esentry");
                    this.getModel().setValue("tdkw_fieldkey", key, newEntryRow);
                    this.getModel().setValue("tdkw_fieldname", allField.get(key), newEntryRow);
                    this.getModel().setValue("tdkw_fieldtype", fieldType.get(key), newEntryRow);
                }
            } else {
                this.getModel().setValue("tdkw_entitykey", null);
            }
        }
    }

    /**
     * 根据实体标识获取所有字段
     *
     * @param entityKey 实体标识
     * @return
     */
    public Map<String, String> getAllField(String entityKey) {
        Map<String, String> fields = new HashMap<>();
        List<String> fieldList = new ArrayList<>();
        //获取实体所有字段
        DynamicObject dynamicObject = new HRBaseServiceHelper(entityKey).generateEmptyDynamicObject();
        DataEntityPropertyCollection properties = dynamicObject.getDataEntityType().getProperties();
        for (IDataEntityProperty property : properties) {
            String propNumber = property.getName();
            fieldList.add(propNumber);
            fieldType.put(propNumber, emUtils.getFieldType(property));
            if (property instanceof BasedataProp) {
                String baseEntityId = ((BasedataProp) property).getBaseEntityId();
                if (StringUtils.equals(baseEntityId, "hrpi_person")) {
                    this.getModel().setValue("tdkw_datamappingfield", propNumber);
                }
            }
        }
        //功能：根据表单标识获取表单所有控件
        //根据表单编码获取表单id
        String id = MetadataDao.getIdByNumber(entityKey, MetaCategory.Form);
        //获取表单元数据
        FormMetadata formMeta = (FormMetadata) MetadataDao.readRuntimeMeta(id, MetaCategory.Form);
        //获取所有控件集合
        List<ControlAp<?>> items = formMeta.getItems();
        for (ControlAp<?> item : items) {
            //控件名称
            String name = item.getName().getLocaleValue();
            //控件编码
            String key = item.getKey();
            if (fieldList.contains(key)) {
                fields.put(key, name);
            }
        }
        return fields;

    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit chooseEntity = this.getControl("tdkw_chooseentity");
        chooseEntity.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent evt) {
        String name = evt.getProperty().getName();
        //只允许选择基础资料和单据、只允许选择<员工信息下中心>下面的单据
        if (StringUtils.equals(name, "tdkw_chooseentity")) {
            QFilter qFilter1 = new QFilter("modeltype", QCP.equals, "BillFormModel");
            qFilter1.or("modeltype", QCP.equals, "BaseFormModel");
            QFilter qFilter2 = new QFilter("bizappid", QCP.equals, "0QO140ANVBBZ");
            qFilter2.or("bizappid", QCP.equals, "1WXBPN7+OHJZ");
            QFilter qFilter3 = new QFilter("masterid", QCP.is_null, null).or("masterid", QCP.equals, "");

            evt.addCustomQFilter(qFilter1);
            evt.addCustomQFilter(qFilter2);
            evt.addCustomQFilter(qFilter3);
        }
    }

}
