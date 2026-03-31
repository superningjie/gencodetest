package tdkw.esc.recruit.formplugin.mobile;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDException;
import kd.bos.form.cardentry.CardEntry;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.control.events.MobileSearchTextChangeEvent;
import kd.bos.form.control.events.MobileSearchTextChangeListener;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.list.MobileSearch;
import kd.bos.list.events.ListRowClickListener;
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
public class RecHrMobFormPlugin extends AbstractMobFormPlugin implements ListRowClickListener, MobileSearchTextChangeListener {

    private final static Log log = LogFactory.getLog(RecHrMobFormPlugin.class);


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
        this.addClickListeners("tdkw_rec_confirm");
        MobileSearch mobileSearch = this.getView().getControl("tdkw_mobilesearchap");
        mobileSearch.addMobileSearchTextChangeListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        String orgId = this.getView().getFormShowParameter().getCustomParam("orgId");
        refreshHrData("",orgId);
    }

    /**
     * 数据绑定之前、获取北森hr数据并赋值到动态表单分录中
     *
     * @param e
     * @author xysusj
     * @date 9:32 2023/6/19
     **/
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
    }

    /**
     * 选择hr人员事件
     *
     * @param evt
     * @author xysusj
     * @date 9:33 2023/6/19
     **/
    @Override
    public void beforeClick(BeforeClickEvent evt) {
        String key = ((Control) evt.getSource()).getKey();
        EntryGrid tdkwEntryentity = this.getControl("tdkw_rec_mob_entryentity");
        DynamicObject billObj = this.getModel().getDataEntity(true);
        DynamicObjectCollection entries = billObj.getDynamicObjectCollection("tdkw_rec_mob_entryentity");
        if (Objects.isNull(entries) || entries.size() == 0) {
            this.getView().showTipNotification("请选择招聘HR!");
        }
        Control source = (Control) evt.getSource();
        String sourceKey = source.getKey();
        int[] selectRows = tdkwEntryentity.getSelectRows();
        //监听确定按钮
        if ("tdkw_rec_confirm".equals(sourceKey)) {
            int selectRow = selectRows[0];
            //获取所选北森hr人员信息
            DynamicObject selectHrPerson = entries.get(selectRow);
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


    @Override
    public void click(MobileSearchTextChangeEvent mobileSearchTextChangeEvent) {
        String text = mobileSearchTextChangeEvent.getText();
        String orgId = this.getView().getFormShowParameter().getCustomParam("orgId");
        refreshHrData(text,orgId);
        this.getView().updateView("tdkw_rec_mob_entryentity");
//        this.getView().updateView();
    }


    /**
     * 获取并更新HR数据
     *
     * @author xysusj
     * @date 10:38 2023/6/30
     **/
    private void refreshHrData(String text,String orgId) {
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
                    Object data = hrMap.get("data");
                    List<LinkedHashMap<Object, Object>> hrList = (List<LinkedHashMap<Object, Object>>) data;
                    DynamicObject dataEntity = this.getView().getModel().getDataEntity(true);
                    DynamicObjectCollection entryInfo = dataEntity.getDynamicObjectCollection("tdkw_rec_mob_entryentity");
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
                        getModel().setValue("tdkw_rec_mob_entryentity", entryInfo);
                        CardEntry list = this.getView().getControl("tdkw_rec_mob_entryentity");
                        list.refreshData();
                    }
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
