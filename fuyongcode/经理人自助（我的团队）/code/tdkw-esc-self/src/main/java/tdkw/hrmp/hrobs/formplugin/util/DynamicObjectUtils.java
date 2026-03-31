package tdkw.hrmp.hrobs.formplugin.util;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.entity.MainEntityType;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.MetadataServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


/**
 * 内容描述：查询基础资料工具类
 *
 * @author xxx
 * @date 2022/6/24
 */
public class DynamicObjectUtils {

    /**
     * 根据编码获取基础资料
     *
     * @param entityNumber 单据标识
     * @param number       编码
     * @return DynamicObject
     * @author xxx
     * @date 2022/6/24
     */
    public static DynamicObject findDynamicObjectByNumber(String entityNumber, String number) {
        return findDynamicObjectByNumber(entityNumber, number, null, null);
    }

    /**
     * @author xxx
     * @Description 根据名称获取基础资料
     * @Date 2022/11/29 15:08
     */
    public static DynamicObject findDynamicObjectByName(String entityNumber, String name) {
        return findDynamicObjectByName(entityNumber, name, null, null);
    }

    /**
     * 根据编码获取基础资料
     *
     * @param entityNumber    单据标识
     * @param number          编码
     * @param requireMsg      错误信息
     * @param requireMsgForCq 查询星瀚基础资料错误信息
     * @return DynamicObject
     * @author xxx
     * @date 2022/6/24
     */
    public static DynamicObject findDynamicObjectByNumber(String entityNumber, String number, String requireMsg, String requireMsgForCq) {
        if (requireMsg != null) {
            Assert.notNull(number, requireMsg);
        } else if (StringUtils.isBlank(number)) {
            return null;
        }
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(entityNumber, "id", new QFilter("number", "=", number).toArray());
        if (StringUtils.isEmpty(requireMsgForCq) && dynamicObject == null) {
            return null;
        }
        Assert.notNull(dynamicObject, requireMsgForCq);
        return BusinessDataServiceHelper.loadSingle(dynamicObject.get("id"), entityNumber);
    }

    /**
     * 根据名称获取基础资料
     *
     * @param entityNumber    单据标识
     * @param name            名称
     * @param requireMsg      错误信息
     * @param requireMsgForCq 查询星瀚基础资料错误信息
     * @return DynamicObject
     * @author xxx
     * @date 2022/6/24
     */
    public static DynamicObject findDynamicObjectByName(String entityNumber, String name, String requireMsg, String requireMsgForCq) {
        if (requireMsg != null) {
            Assert.notNull(name, requireMsg);
        } else if (StringUtils.isBlank(name)) {
            return null;
        }
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(entityNumber, "id", new QFilter("name", "=", name).toArray());
        if (StringUtils.isEmpty(requireMsgForCq) && dynamicObject == null) {
            return null;
        }
        Assert.notNull(dynamicObject, requireMsgForCq);
        return BusinessDataServiceHelper.loadSingle(dynamicObject.get("id"), entityNumber);
    }

    /**
     * 根据编码获取基础资料
     *
     * @param entityNumber    单据标识
     * @param filterKey       被过滤的字段
     * @param number          编码
     * @param requireMsg      错误信息
     * @param requireMsgForCq 查询星瀚基础资料错误信息
     * @return DynamicObject
     * @author xxx
     * @date 2022/6/24
     */
    public static DynamicObject findDynamicObjectByKey(String entityNumber, String filterKey, String number, String requireMsg, String requireMsgForCq) {
        if (requireMsg != null) {
            Assert.notNull(number, requireMsg);
        } else if (StringUtils.isBlank(number)) {
            return null;
        }
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(entityNumber, "id", new QFilter(filterKey, "=", number).toArray());
        if (StringUtils.isEmpty(requireMsgForCq) && dynamicObject == null) {
            return null;
        }
        Assert.notNull(dynamicObject, requireMsgForCq);
        return BusinessDataServiceHelper.loadSingle(dynamicObject.get("id"), entityNumber);
    }

    /**
     * 获取人员主职部门
     *
     * @author xxx
     * @date 2022-06-27
     */
    public static Object findUserPrimaryDept(DynamicObject user) {
        if (user == null) {
            return null;
        }
        return user.getDynamicObjectCollection("entryentity")
                .stream()
                .filter(it -> !it.getBoolean("ispartjob"))
                .map(it -> it.getDynamicObject("dpt").getPkValue())
                .findFirst().orElse(null);

    }

    /**
     * 获取人员主职职位
     *
     * @author xxx
     * @date 2022-12-05
     */
    public static Object findUserPrimaryPosition(DynamicObject user) {
        if (user == null) {
            return null;
        }
        return user.getDynamicObjectCollection("entryentity")
                .stream()
                .filter(it -> !it.getBoolean("ispartjob"))
                .map(it -> it.get("position"))
                .findFirst().orElse(null);

    }


    /**
     *
     * @param entityName 单据名称
     * @return 单据所有字段名
     */
    public static String getAllFields(String entityName){
        //获取单据类型
        MainEntityType dataEntityType = MetadataServiceHelper.getDataEntityType(entityName);
        Map<String, IDataEntityProperty> allFields = dataEntityType.getAllFields();
        //所有字段
        Set<String> set = allFields.keySet();
        ArrayList<String> list = new ArrayList<>(set);
        return String.join(",", list);
    }
}
