package tdkw.hrmp.hrobs.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Search;
import kd.bos.form.control.events.EntryGridBindDataListener;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.SearchEnterListener;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date： 2023/9/25
 * @description : 人员搜索列表
 * @formId: tdkw_personlist
 */
public class PersonListFormPlugin extends AbstractFormPlugin implements HyperLinkClickListener, EntryGridBindDataListener, SearchEnterListener {
    private static final String KEY_ENTRYENTITY = "tdkw_entryentity";

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        String keyword = (String) this.getView().getFormShowParameter().getCustomParams().get("keyword");
        createEntryentity(keyword);
        Search search = this.getControl("tdkw_searchap");
        search.setSearchKey(keyword);
    }


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid entryentity = this.getControl(KEY_ENTRYENTITY);
        entryentity.addHyperClickListener(this);
        entryentity.addDataBindListener(this);
        this.addItemClickListeners("tdkw_advcontoolbarap");
        this.addItemClickListeners("tdkw_isonjob");
        this.addItemClickListeners("tdkw_textfield");
        Search search = this.getControl("tdkw_searchap");
        search.addEnterListener(this);
    }


    private void createEntryentity(String keyWord) {
        QFilter qFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE)
                .and("empposrel.datastatus", QCP.equals, "1")
                .and("empposrel.isprimary", QCP.equals, "1")
                .and("empposrel.businessstatus", QCP.equals, "1");

        //模糊搜索姓名
        QFilter personNameFilter = new QFilter("person.name", QCP.like, "%" + keyWord + "%");
        if (StringUtils.isNotEmpty(keyWord)) {
            qFilter.and(personNameFilter);
        }
        //查询人员档案
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ryjlcx_pc", "tdkw_hrorg");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //全组织查看权限
        List<Long> hasPerOrg = new ArrayList<>();
        if (!hasAllOrgPerm) {
            //权限组织
            hasPerOrg = result.getHasPermOrgs();
        }
        if (!hasAllOrgPerm) {
            qFilter.and("empposrel.adminorg", QCP.in, hasPerOrg);
        }
        DynamicObject[] users = BusinessDataServiceHelper.load("hspm_ermanfile", "id,person,depemp,empposrel", qFilter.toArray());
        List<Long> personIds = Arrays.stream(users).map(i -> i.getLong("person.id")).collect(Collectors.toList());
        QFilter personFilter = new QFilter("person.id", QCP.in, personIds);
        DynamicObject[] positions = BusinessDataServiceHelper.load("hbpm_positionhr", "id,tdkw_positionlevel", null);
        Map<Long, String> positionMap = new HashMap<>();
        for (DynamicObject position : positions) {
            positionMap.put(position.getLong("id"), position.getString("tdkw_positionlevel"));
        }
        DynamicObject[] contacts = BusinessDataServiceHelper.load("hrpi_percontact", "id,person,phone", personFilter.toArray());
        Map<Long, String> contactsMap = new HashMap<>();
        for (DynamicObject contact : contacts) {
            contactsMap.put(contact.getLong("person.id"), contact.getString("phone"));
        }


        DynamicObjectCollection entryColl = this.getModel().getEntryEntity(KEY_ENTRYENTITY);
        DynamicObjectType entryType = entryColl.getDynamicObjectType();
        for (DynamicObject user : users) {
            DynamicObject entryObj = new DynamicObject(entryType);
            entryObj.set("tdkw_name", user.getString("person.name"));
            entryObj.set("tdkw_company", user.getDynamicObject("empposrel.company"));
            entryObj.set("tdkw_org", user.getDynamicObject("empposrel.adminorg"));
            entryObj.set("tdkw_position", user.getDynamicObject("empposrel.position"));
            entryObj.set("tdkw_positionlevel", positionMap.get(user.getLong("empposrel.position.id")));
            entryObj.set("tdkw_phone", contactsMap.get(user.getLong("person.id")));
            entryObj.set("tdkw_person", user.getLong("person.id"));
            entryColl.add(entryObj);
        }
        this.getModel().updateEntryCache(this.getModel().getEntryEntity(KEY_ENTRYENTITY));
        this.getView().updateView(KEY_ENTRYENTITY);
    }

    @Override
    public void search(SearchEnterEvent evt) {
        Search search = (Search) evt.getSource();
        if (StringUtils.equals("tdkw_searchap", search.getKey())) {
            String searchText = evt.getText();
            if (StringUtils.isEmpty(searchText)) {
                return;
            }
            this.getModel().deleteEntryData(KEY_ENTRYENTITY);
            createEntryentity(searchText);
        }
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        String fieldName = hyperLinkClickEvent.getFieldName();
        if (StringUtils.equals("tdkw_name", fieldName)) {
            Object personId = this.getModel().getValue("tdkw_person", hyperLinkClickEvent.getRowIndex());
            DynamicObject person = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
            FormShowParameter formShowParameter = new FormShowParameter();
            formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
            formShowParameter.setCustomParam("erfileId", person.getString("tdkw_pkid"));
            formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
            formShowParameter.setHasRight(true);
            this.getView().showForm(formShowParameter);
        }
    }
}
