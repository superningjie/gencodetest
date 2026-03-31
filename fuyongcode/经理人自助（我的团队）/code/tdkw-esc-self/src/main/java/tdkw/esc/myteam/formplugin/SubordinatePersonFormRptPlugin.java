package tdkw.esc.myteam.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.exception.KDBizException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Search;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.SearchEnterListener;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author guokairong
 * @Date 2023/8/14 15:10
 */
public class SubordinatePersonFormRptPlugin extends AbstractReportFormPlugin implements BeforeF7SelectListener, HyperLinkClickListener, SearchEnterListener {
    private static final Log logger = LogFactory.getLog(SubordinatePersonFormRptPlugin.class);

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        String name = beforeF7SelectEvent.getProperty().getName();
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Long personId = formShowParameter.getCustomParam("personId");
        if ("tdkw_mainpositionfilter".equals(name)) {
            //查询任职经历
            QFilter personFilter = new QFilter("person.id", "=", personId);
            personFilter.and("iscurrentversion", "=", "1");
            personFilter.and("businessstatus", "=", "1");
            personFilter.and("datastatus", "=", "1");
            DynamicObject[] careerExperiences = BusinessDataServiceHelper.load("hrpi_empposorgrel", "position,position.id", personFilter.toArray());
            Set<Long> positionIdSet = new HashSet<>();
            for (DynamicObject careerExperience : careerExperiences) {
                positionIdSet.add(careerExperience.getLong("position.id"));
            }
            // 设置列表过滤条件
            QFilter qFilter = new QFilter("id", "in", positionIdSet);
            List<QFilter> treeFilterList = new ArrayList<>();
            treeFilterList.add(qFilter);
            ListShowParameter showParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            showParameter.getListFilterParameter().setQFilters(treeFilterList);
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        ReportList reportlistap = this.getControl("reportlistap");
        if (reportlistap != null) {
            reportlistap.addHyperClickListener(this);
        }
        // 侦听基础资料字段的事件
        BasedataEdit fieldEdit = this.getView().getControl("tdkw_mainpositionfilter");
        if (fieldEdit != null) {
            fieldEdit.addBeforeF7SelectListener(this);
        }
        //监听姓名搜索控件
        Search search = this.getControl("tdkw_searchap");
        search.addEnterListener(this);

    }


    @Override
    public void hyperLinkClick(HyperLinkClickEvent hyperLinkClickEvent) {
        if (StringUtils.equals("tdkw_person", hyperLinkClickEvent.getFieldName())) {
            Long personId = (Long) hyperLinkClickEvent.getRowData().getDynamicObject("tdkw_person").getPkValue();
            DynamicObject hrPerson = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
            if (hrPerson != null) {
                //跳转我的档案
                FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
                formShowParameter.setCustomParam("erfileId", hrPerson.getString(HRBaseConstants.ID));
                formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                formShowParameter.setHasRight(true);
                this.getView().showForm(formShowParameter);
            } else {
                throw new KDBizException("未找到对应人事业务档案");
            }
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        logger.info("afterCreateNewData执行了");
        super.afterCreateNewData(e);
        //查询任职类型并赋值
        DynamicObjectCollection posTypeForDisplay = new DynamicObjectCollection();
        QFilter filter = new QFilter("enable", QCP.equals, "1");
        DynamicObject[] load = BusinessDataServiceHelper.load("hbss_postype", "id", filter.toArray(),"number");
        for (DynamicObject tempId : load) {
            posTypeForDisplay.add(tempId);
        }

        this.getModel().setValue("tdkw_postype", posTypeForDisplay);
        this.getView().updateView("tdkw_postype");

        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Long positionId = Long.valueOf(formShowParameter.getCustomParam("positionId").toString());
        Long personId = Long.valueOf(formShowParameter.getCustomParam("personId").toString());
        Long collaborativeId = Long.valueOf(formShowParameter.getCustomParam("collaborativeId").toString());
        IDataModel model = this.getModel();

        //查询岗位并赋值
        if (positionId != 0l) {
            model.setValue("tdkw_mainpositionfilter", positionId);
        }
        if (personId != 0l) {
            model.setValue("tdkw_leader", personId);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("positionId", positionId);
        paramMap.put("personId", personId);
        paramMap.put("collaborativeId", collaborativeId);
        paramMap.put("searchName", searchName);
        this.getView().getQueryParam().setCustomParam(paramMap);
        this.getView().refresh();
    }

    private String searchName;

    @Override
    public void search(SearchEnterEvent searchEnterEvent) {
        logger.info("searchEnterEvent = " + searchEnterEvent);
        searchName = searchEnterEvent.getText();
        getView().refresh();
    }

    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        logger.info("beforeQuery执行了");
        Map<String, Object> customParam = queryParam.getCustomParam();
        logger.info("beforeQuery" + customParam);
        Search search = this.getControl("tdkw_searchap");
        searchName = search.getSearchKey();
        customParam.put("searchName", searchName);
        queryParam.setCustomParam(customParam);
    }
}
