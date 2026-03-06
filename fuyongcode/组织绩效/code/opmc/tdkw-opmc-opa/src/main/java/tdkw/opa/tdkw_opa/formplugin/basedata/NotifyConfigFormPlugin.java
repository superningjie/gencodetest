package tdkw.opa.tdkw_opa.formplugin.basedata;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.BeforeImportEntryEventArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.importentry.resolving.ImportEntryData;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mutex.DataMutex;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;
import tdkw.opa.tdkw_opa.formplugin.utils.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotifyConfigFormPlugin extends AbstractBasePlugIn implements BeforeF7SelectListener {
    private static final Log logger = LogFactory.getLog(NotifyConfigFormPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
        BasedataEdit fieldEdit = this.getView().getControl("tdkw_person");
        fieldEdit.addBeforeF7SelectListener(this);
    }

    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);
        FormShowParameter formShowParameter = e.getFormShowParameter();
        Object open = formShowParameter.getCustomParam("open");
        if (open == null) {
            e.setCancel(true);
        } else {
            QFilter qFilter = new QFilter("number", QCP.equals, "TZRYPZ_00001");
            DynamicObject notifyConfig = QueryServiceHelper.queryOne(EntityName.BASE_NOTIFY_CONFIG, "id", qFilter.toArray());

            Map<String, String> lockInfo;
            try (DataMutex dataMutex = DataMutex.create()) {
                // 遍历 calculateScore，检查互斥锁
                // 获取锁定信息
                lockInfo = dataMutex.getLockInfo(notifyConfig.getString("id"), "default_netctrl", EntityName.BASE_NOTIFY_CONFIG);

                // 如果有锁定信息，提示并取消操作
                if (lockInfo != null && !lockInfo.isEmpty()) {
                    Long userid = Long.valueOf(lockInfo.get("userid"));
                    DynamicObject user = BusinessDataServiceHelper.loadSingle("bos_user", "name", new QFilter("id", QCP.equals, userid).toArray());
                    String name = user.getString("name");
                    formShowParameter.setCustomParam("lockUserName", name);
                }
            } catch (Exception exception) {
                logger.info("互斥锁出错：" + exception);

            }
        }

    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Object name = formShowParameter.getCustomParam("lockUserName");
        if (StringUtils.isNotBlank(name)) {
            this.getView().showTipNotification(name + "正在PC端编辑该记录，请稍后再试或联系系统管理员。");
        }


    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String itemKey = evt.getItemKey();
        //删除
        if (StringUtils.equals("tdkw_baritemap3", itemKey)) {
            EntryGrid notifyDetails = this.getControl("tdkw_notify_details");
            int[] selectRows = notifyDetails.getSelectRows();
            if (selectRows == null || selectRows.length == 0) {
                this.getView().showTipNotification("请选择需要删除的数据！");
                evt.setCancel(true);
            }
        }
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        long currUserId = RequestContext.get().getCurrUserId();
        AuthorizedOrgResult orgSet = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, EntityName.BILL_ORG_PERF_METRICS, "tdkw_adminorg");
        boolean hasAllOrgPerm = orgSet.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPermOrgs = orgSet.getHasPermOrgs();
            QFilter qFilter = new QFilter("empposrel.company.id", QCP.in, hasPermOrgs);
            event.addCustomQFilter(qFilter);
        } else {
            logger.info("有所有组织权限hasAllOrgPerm" + hasAllOrgPerm);
        }
    }



    @Override
    public void beforeImportEntry(BeforeImportEntryEventArgs e) {
        super.beforeImportEntry(e);
        Map<String, Object> source = (Map<String, Object>) e.getSource();
        List<ImportEntryData> notifyDetails = (List<ImportEntryData>) source.get("tdkw_notify_details");

        // Step 1: 收集所有的 personNumber
        List<String> personNumbers = new ArrayList<>();
        for (ImportEntryData row : notifyDetails) {
            JSONObject data = row.getData();
            if (data.containsKey("tdkw_person")) {
                JSONObject person = data.getJSONObject("tdkw_person");
                String personNumber = person.getString("number");
                if (StringUtils.isNotEmpty(personNumber)) {
                    personNumbers.add(personNumber);
                }
            }
        }

        // Step 2: 批量查询所有的 person
        Map<String, Long> personMap = new HashMap<>();
        if (!personNumbers.isEmpty()) {
            QFilter personFilter = new QFilter("person.number", QCP.in, personNumbers);
            personFilter.and("iscurrentversion", QCP.equals, "1");  // 当前版本
            personFilter.and("datastatus", QCP.equals, "1");  // 生效中
            personFilter.and("businessstatus", QCP.equals, "1"); // 生效中
            DynamicObjectCollection ermanFile = QueryServiceHelper.query("hspm_ermanfile", "id,number", personFilter.toArray());

            // 将查询到的 person id 和 number 映射存入 personMap
            for (DynamicObject file : ermanFile) {
                String number = file.getString("number");
                Long id = file.getLong("id");
                personMap.put(number, id);
            }
        }

        // Step 4: 遍历 ImportEntryData 列表，填充数据
        for (ImportEntryData row : notifyDetails) {
            JSONObject data = row.getData();

            // 处理 tdkw_report_org
            if (data.containsKey("tdkw_report_org")) {
                JSONObject reportOrg = data.getJSONObject("tdkw_report_org");
                String reportOrgNumber = reportOrg.getString("number");
                if (StringUtils.isNotEmpty(reportOrgNumber)) {
                    QFilter reportOrgFilter = new QFilter("number", QCP.equals, reportOrgNumber);
                    reportOrgFilter.and("iscurrentversion", QCP.equals, "1"); // 当前版本
                    reportOrgFilter.and("datastatus", QCP.equals, "1"); // 生效中
                    DynamicObject reportOrgObject = QueryServiceHelper.queryOne("haos_adminorghr", "id", reportOrgFilter.toArray());

                    if (reportOrgObject != null) {
                        Long reportOrgId = reportOrgObject.getLong("id");
                        reportOrg.put("id", reportOrgId);
                    }
                    reportOrg.remove("number");
                }
                reportOrg.put("importprop", "id");
            }

            // 处理 tdkw_person 和 tdkw_empposorgrel
            if (data.containsKey("tdkw_person")) {
                JSONObject person = data.getJSONObject("tdkw_person");
                String personNumber = person.getString("number");
                if (personMap.containsKey(personNumber)) {
                    Long personId = personMap.get(personNumber);
                    person.put("id", personId);
                    person.remove("number");
                    person.put("importprop", "id");
                }
            }
        }
    }


}
