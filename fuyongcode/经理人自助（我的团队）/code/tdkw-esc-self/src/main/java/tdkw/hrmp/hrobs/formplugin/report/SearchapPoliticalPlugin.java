package tdkw.hrmp.hrobs.formplugin.report;

import cfca.svs.api.util.StringUtil;
import com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Search;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.SearchEnterListener;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.report.ReportView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchapPoliticalPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener, SearchEnterListener {
    private static final Log logger = LogFactory.getLog(SearchapPoliticalPlugin.class);
    private String name;

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Search search = this.getControl("tdkw_searchap");
        search.addEnterListener(this);
        ReportList reportlistap = this.getControl("reportlistap");
        reportlistap.addHyperClickListener(this);
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent evt) {
        if (StringUtils.equals("tdkw_person", evt.getFieldName())) {
            //点击获取人事业务档案的主键(隐藏列)
            DynamicObject rowData = evt.getRowData();
            //参数为查询配置的查询字段的标识
            Long personId = rowData.getLong("tdkw_person.id");
            QFilter qFilter = new QFilter("id", QCP.equals, personId);
            DynamicObject person = BusinessDataServiceHelper.loadSingle("hrpi_person", "tdkw_pkid", qFilter.toArray());
            String pkId = person.getString("tdkw_pkid");
            logger.info("跳转的人员pkid为" + pkId);

            try {
                FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                formShowParameter.setCustomParam("erfileId", pkId);
                formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
                formShowParameter.setHasRight(true);
                this.getView().showForm(formShowParameter);
            } catch (Exception e) {
                this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "RightTableClickFormPlugin", "tdkw-esc-tdkw_integrated_query-report-ext", new Object[0]));
            }
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        ReportView source = (ReportView) e.getSource();
        String personIds = source.getFormShowParameter().getCustomParam("personIds");
        String personIdsDepart = source.getFormShowParameter().getCustomParam("personIdsDepart");
        if (StringUtils.equals(personIdsDepart, "[]")) {
            this.getView().showErrorNotification("信息为空！请返回！");
            return;
        }
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtil.isNotEmpty(personIds)) {
            params.put("personIds_a", personIds);
        } else {
            params.put("personIds_depart", personIdsDepart);
        }
        source.getQueryParam().setCustomParam(params);
        source.refresh();
    }

    @Override
    public void search(SearchEnterEvent searchEnterEvent) {
        Search search = (Search) searchEnterEvent.getSource();
        Map<String, Object> param = new HashMap();


        if (StringUtils.equals("tdkw_searchap", search.getKey())) {
//            System.out.println("searchEnterEvent = " + searchEnterEvent);
            name = searchEnterEvent.getText();
            param.put("searchName", name);
            this.getQueryParam().getCustomParam().put("searchName", name);
            logger.info("param.put(searchName, name)" + name);
            getView().refresh();

        }
    }

    @Override
    public List<String> getSearchList(SearchEnterEvent evt) {
        return SearchEnterListener.super.getSearchList(evt);
    }

    @Override
    public List<Object> getComPlexSearchList(SearchEnterEvent evt) {
        return SearchEnterListener.super.getComPlexSearchList(evt);
    }
}
