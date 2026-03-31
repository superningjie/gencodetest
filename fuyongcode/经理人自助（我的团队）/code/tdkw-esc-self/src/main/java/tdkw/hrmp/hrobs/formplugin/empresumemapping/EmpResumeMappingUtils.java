package tdkw.hrmp.hrobs.formplugin.empresumemapping;

import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.dynamicobject.DynamicLocaleProperty;
import kd.bos.entity.property.BasedataProp;
import kd.bos.entity.property.BooleanProp;
import kd.bos.entity.property.ComboProp;
import kd.bos.entity.property.DateTimeProp;
import kd.bos.entity.property.DecimalProp;
import kd.bos.entity.property.IntegerProp;
import kd.bos.entity.property.LongProp;
import kd.bos.entity.property.PictureProp;
import kd.bos.entity.property.TextProp;

/**
 * @Metadata： XX
 * @Description ： XX
 * @ClassName ：EmpResumeMappingUtils
 * @author xxx
 * @Date ：2023/6/29 16:04
 * @Version: 1.0
 */
public class EmpResumeMappingUtils {

    public String getFieldType(IDataEntityProperty property){
       String fieldType ="";
        if(property instanceof BasedataProp){
            fieldType = "DynamicOject";
        }
        else  if(property instanceof LongProp){
            fieldType = "long";
        }
        else  if(property instanceof DateTimeProp){
            fieldType = "date";
        }
        else  if(property instanceof IntegerProp){
            fieldType = "integer";
        }
        else  if(property instanceof TextProp){
            fieldType = "text";
        }
        else  if(property instanceof ComboProp){
            fieldType = "text";
        }
        else  if(property instanceof BooleanProp){
            fieldType = "boolean";
        }
        else  if(property instanceof DynamicLocaleProperty){
            fieldType = "DynamicCollection";
        }
        else  if(property instanceof DecimalProp){
            fieldType = "double";
        }
        else  if(property instanceof PictureProp){
            fieldType = "byte";
        }
        return fieldType;
    }

}
