package tdkw.esc.recruit.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Search;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.SearchEnterListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.DispatchServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.*;

/**
 * @author xyliusn
 * @description 北森HR列表动态表单插件
 * @date 2023/5/20
 */
public class RecHrFormPlugin extends AbstractFormPlugin implements SearchEnterListener {

    private final static Log log = LogFactory.getLog(RecHrFormPlugin.class);


    /**
     * 注册监听
     *
     * @param e
     * @author xysusj
     * @date 9:32 2023/6/19
     **/
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //按钮增加监听
        this.addClickListeners("btnok");
        Search search = this.getControl("tdkw_searchap");
        search.addEnterListener(this);
    }


    /**
     * 取北森hr数据并赋值到动态表单分录中
     *
     * @param e
     */
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        // 子页面获取参数
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        // 获取单个参数
        String orgId = showParameter.getCustomParam("orgId");
        refreshHrData("", orgId);
        this.getView().updateView();

    }


    /**
     * 选择hr人员事件
     *
     * @param evt
     * @author xysusj
     * @date 9:33 2023/6/19
     **/
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        EntryGrid tdkwEntryentity = this.getControl("tdkw_entryentity");
        int[] selectRows = tdkwEntryentity.getSelectRows();
        if (Objects.isNull(selectRows) || selectRows.length == 0) {
            this.getView().showTipNotification("请选择人员!");
        }
        Control source = (Control) evt.getSource();
        String sourceKey = source.getKey();
        //监听确定按钮
        if ("btnok".equals(sourceKey)) {
            int selectRow = selectRows[0];
            DynamicObjectCollection hrPersons = this.getModel().getEntryEntity("tdkw_entryentity");
            //获取所选北森hr人员信息
            DynamicObject selectHrPerson = hrPersons.get(selectRow);
            HashMap<String, String> hashMap = new HashMap<>();
            String name = selectHrPerson.getString("tdkw_rec_apply_hr_name");
            String dept = selectHrPerson.getString("tdkw_rec_apply_hr_dept");
            String email = selectHrPerson.getString("tdkw_rec_apply_hr_email");
            String id = selectHrPerson.getString("tdkw_rec_apply_hr_pk");
            hashMap.put("name", name);
            hashMap.put("dept", dept);
            hashMap.put("email", email);
            hashMap.put("id", id);
            //向父页面传所选北森hr人员id及对应部门id
            this.getView().returnDataToParent(hashMap);
            this.getView().close();
        }
    }

    /**
     * 搜索
     */
    @Override
    public void search(SearchEnterEvent evt) {
        Search search = (Search) evt.getSource();
        if (StringUtils.equals("tdkw_searchap", search.getKey())) {
            String searchText = evt.getText();
            String orgId = this.getView().getFormShowParameter().getCustomParam("orgId");
            refreshHrData(searchText, orgId);
            this.getView().updateView();
        }
    }

    /**
     * 获取并更新HR数据
     *
     * @author xysusj
     * @date 10:38 2023/6/30
     **/
    private void refreshHrData(String text, String orgId) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", text);
            paramMap.put("pkOrg", orgId);
            Object obj = DispatchServiceHelper.invokeBizService(
                    "isc",
                    "iscb",
                    "IscApicService",
                    "invokeScriptApi2",
                    "listAccountStaffPS",
                    paramMap,
                    "");
            if (Objects.nonNull(obj)) {
                LinkedHashMap<Object, Object> hrMap = (LinkedHashMap<Object, Object>) obj;
                Object statusCode = hrMap.get("statusCode");
                String statusCodeStr = statusCode.toString();
                if (StringUtils.isNumeric(statusCodeStr) && Integer.parseInt(statusCodeStr) == HttpStatus.SC_OK) {
                    List<LinkedHashMap<Object, Object>> hrList = new ArrayList<>();
                    Object data = hrMap.get("data");
                    if (data != null) {
                        hrList = (List<LinkedHashMap<Object, Object>>) data;
                    }
                    DynamicObject dataEntity = this.getView().getModel().getDataEntity(true);
                    DynamicObjectCollection entryInfo = dataEntity.getDynamicObjectCollection("tdkw_entryentity");
                    entryInfo.clear();
                    if (CollectionUtils.isNotEmpty(hrList)) {
                        for (LinkedHashMap<Object, Object> staffInfo : hrList) {
                            DynamicObject dynamicObject = entryInfo.addNew();
                            Object name = staffInfo.get("name");
                            Object deptName = staffInfo.get("departmentName");
                            Object email = staffInfo.get("email");
                            Object deptId = staffInfo.get("departmentId");
                            Object staffId = staffInfo.get("userId");
                            dynamicObject.set("tdkw_rec_apply_hr_name", name);
                            dynamicObject.set("tdkw_rec_apply_hr_dept", deptName);
                            dynamicObject.set("tdkw_rec_apply_hr_email", email);
                            dynamicObject.set("tdkw_rec_apply_hr_dept_pk", deptId);
                            dynamicObject.set("tdkw_rec_apply_hr_pk", staffId);
                        }
                    }
                    getModel().setValue("tdkw_entryentity", entryInfo);
                    EntryGrid entryGrid = this.getView().getControl("tdkw_entryentity");
                    entryGrid.getView().updateView();
                    this.getView().updateView("tdkw_entryentity");
                } else {
                    log.error("获取北森HR人员数据失败！");
                    this.getView().showErrorNotification("获取北森HR人员数据失败！");
                }
            }
        } catch (KDException ex) {
            log.error("获取北森HR人员数据失败！", ex);
            this.getView().showErrorNotification("获取北森HR人员数据失败！");
        }
    }


}
