package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author xxx
 * @date： 2023/10/9
 * @description :
 */
public class PersonInfoCustomPlugin extends AbstractFormPlugin {
    private static final Log logger = LogFactory.getLog(PersonInfoCustomPlugin.class);

    @Override
    public void customEvent(CustomEventArgs e) {
        super.customEvent(e);
        logger.info("customEvent开始执行");
        logger.info(e.getKey() + "/" + e.getVarMap() + "/" + e.getEventArgs() + "/" + e.getEventName());
        String eventName = e.getEventName();
        logger.info("eventName:" + eventName);
        CustomControl control = this.getView().getControl("tdkw_customcontrolap");
        String eventArgs = e.getEventArgs();
        //将字符串转成json
        JSONObject param = JSONObject.parseObject(eventArgs);

        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(), "tdkw_ryjlcx_pc", "tdkw_hrorg");
        //如果包含10000L就返回true
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        //全组织查看权限
        List<Long> hasPerOrg = new ArrayList<>();
        if (!hasAllOrgPerm) {
            //权限组织
            hasPerOrg = result.getHasPermOrgs();
        }

        //搜索更多
        if ("search".equals(eventName)) {
            QFilter qFilter = new QFilter("iscurrentversion", QCP.equals, Boolean.TRUE)
                    .and("empposrel.datastatus", QCP.equals, "1")
                    .and("empposrel.isprimary", QCP.equals, "1")
                    .and("empposrel.businessstatus", QCP.equals, "1");
            if (!hasAllOrgPerm) {
                qFilter.and("empposrel.adminorg", QCP.in, hasPerOrg);
            }
            //模糊搜索姓名
            String keyWord = String.valueOf(param.get("keyword"));
            logger.info("keyWord:" + keyWord);
            QFilter personNameFilter = new QFilter("person.name", QCP.like, "%" + keyWord + "%");
            if (StringUtils.isNotEmpty(keyWord)) {
                qFilter.and(personNameFilter);
            }
            //查询人员档案
            DynamicObject[] persons = BusinessDataServiceHelper.load("hspm_ermanfile", "id,person,depemp,empposrel", qFilter.toArray(), null, 6);
            logger.info("persons:" + persons.toString());
            JSONObject dataInfo = new JSONObject();
            JSONArray dateInfoArray = new JSONArray();
            dataInfo.put("data", dateInfoArray);
            for (DynamicObject person : persons) {
                JSONObject personInfo = new JSONObject();
                StringBuilder clientFullContextPath = new StringBuilder(RequestContext.get().getClientFullContextPath());
                personInfo.put("person", person.getString("person.name"));
                personInfo.put("headsculpture", clientFullContextPath.append("/attachment/downloadImage/").append(person.getString("person.headsculpture")));
                personInfo.put("company", person.getString("empposrel.company.name"));
                personInfo.put("adminorg", person.getString("empposrel.adminorg.name"));
                personInfo.put("position", person.getString("empposrel.position.name"));
                personInfo.put("personId", person.getString("person.id"));
                dateInfoArray.add(personInfo);
            }
            dataInfo.put("timestamp", System.currentTimeMillis());
            logger.info("dataInfo:" + dataInfo);
            control.setData(dataInfo);
        }
        //跳转人员列表
        if ("jumpPersonList".equals(eventName)) {
            logger.info("jumpPersonList");
            String keyword = String.valueOf(param.get("keyword"));
            IFormView view = this.getView();
            logger.info("keyword:" + keyword + "view:" + view);
            FormShowParameter formShowParameter = new FormShowParameter();
            formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
            formShowParameter.setFormId("tdkw_personlist");
            formShowParameter.setCustomParam("keyword", keyword);
            view.showForm(formShowParameter);
        }
        //跳转人员列表
        if ("jumpPerson".equals(eventName)) {
            logger.info("jumpPerson");
            String personId = String.valueOf(param.get("personId"));
            logger.info("personId:" + personId);
            DynamicObject person = BusinessDataServiceHelper.loadSingle(Long.valueOf(personId), "hrpi_person");
            logger.info("pkid:" + person.getString("tdkw_pkid"));
            FormShowParameter formShowParameter = new FormShowParameter();
            formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
            formShowParameter.setCustomParam("erfileId", person.getString("tdkw_pkid"));
            formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
            formShowParameter.setHasRight(true);
            this.getView().showForm(formShowParameter);
        }
    }
}
