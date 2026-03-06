package hr;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.form.FormShowParameter;
import kd.bos.form.container.AdvContainer;
import kd.bos.form.container.Container;
import kd.bos.form.control.Control;
import kd.bos.util.StringUtils;

import java.util.*;

/**
 * @Description：人员档案信息变更申请 表单插件
 */
public class InfoApprovalFormPlugin extends AbstractBillPlugIn {

    @Override
    public void beforeBindData(EventObject e) {

        Map<String, String> map = new HashMap<>();
        // 家庭成员
        map.put("hrpi_familymemb", "家庭成员");
        // 语言能力
        map.put("hrpi_languageskills", "语言能力");
        // 教育信息
        map.put("hrpi_pereduexp", "教育信息");
        // 生育信息
        map.put("hrpi_fertilityinfo", "生育信息");
        // 证件信息
        map.put("hrpi_percre", "证件信息");
        // 职称信息
        map.put("hrpi_perprotitle", "职称信息");
        // 服务年限
        map.put("hrpi_perserlen", "服务年限");
        // 基本信息
        map.put("hrpi_pernontsprop", "基本信息");

        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        // 获取页面参数
        String data = formShowParameter.getCustomParam("data");

        if (StringUtils.isEmpty(data)) {
            return;
        }

        // 多个的情况
        List<String> entityNameValueList = new ArrayList<>();

        if (StringUtils.isNotEmpty(data)) {
            // 使用','分割
            String[] split = data.split(",");
            // 如果有多个
            for (String s : split) {
                // 获取对应的value
                String entityName = map.get(s);
                if (StringUtils.isNotEmpty(entityName)) {
                    entityNameValueList.add(entityName);
                }
            }
        }

//        DynamicObjectCollection dynamicObjects = this.getModel().getEntryEntity("entryentity");
//        List<DynamicObject> sortedDynList = this.sortMulLineTableByCreateTimeOrExt(dynamicObjects);
//
//        if (!sortedDynList.isEmpty()) {
//            // 获取所有的附表名
//            List<String> entityNameList = sortedDynList.stream().map(s -> s.getString("entityname")).collect(Collectors.toList());
//        }

        Container control = this.getView().getControl("infogroupshow");
        List<Control> items = control.getItems();

        if (null != items && !items.isEmpty()) {
            for (Control item : items) {
                if (item instanceof AdvContainer) {
                    String key = item.getKey();
                    if (StringUtils.isNotEmpty(key)) {
                        // 使用'_'分割,取第一部分
                        String[] split = key.split("_");
                        if (split.length > 0) {
                            String s = split[0];
                            // 如果不存在
                            if (!entityNameValueList.contains(s)) {
                                // 不展示
                                control.deleteControls(key);
                            }
                        }
                    }
                }
            }
        }
    }

}
