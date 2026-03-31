package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessActivityEntityService;
import cds0.opmc.cea.business.service.AssessObjDomainService;
import cds0.opmc.cea.common.AppflgConstant;
import cds0.opmc.cea.common.CommonUtils;
import cds0.opmc.cea.common.enums.AssessStatusEnum;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.BeforeCreateListDataProviderArgs;
import kd.bos.form.events.HyperLinkClickArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.mvc.list.ListDataProvider;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.formplugin.web.HRDataBaseList;

public class ProcessedListPlugin extends HRDataBaseList {

    private static final AssessObjDomainService ASSESS_OBJ_DOMAIN_SERVICE = AssessObjDomainService.getInstance();
    private static final AssessActivityEntityService ASSESS_ACTIVITY_ENTITY_SERVICE = AssessActivityEntityService.getInstance();

    @Override
    public void setFilter(SetFilterEvent evt) {
        // 设置过滤条件，过滤当前测评活动下的待启动的测评对象
        QFilter qFilter = new QFilter("assessact.id", QCP.equals, getView().getFormShowParameter().getCustomParam("assessActId")).
                and("assesstaus", QCP.equals, AssessStatusEnum.PROCESSED.getValue());
        evt.getQFilters().add(qFilter);
        evt.setOrderBy("overtime Desc");

    }


    @Override
    public void beforeCreateListDataProvider(BeforeCreateListDataProviderArgs args) {
        DynamicObject assessAct = ASSESS_ACTIVITY_ENTITY_SERVICE.queryAssessActivityByPk(getView().getFormShowParameter().getCustomParam("assessActId"));
        args.setListDataProvider(new ListDataProvider() {
            @Override
            public DynamicObjectCollection getData(int start, int limit) {
                DynamicObjectCollection rows = super.getData(start, limit);
                if (rows.isEmpty()) {
                    return rows;
                }
                for (DynamicObject row : rows) {
                    row.set("showscore", CommonUtils.setSumScoreScale(row.getBigDecimal("modscore"),assessAct.getString("mpnumaccuracy"),assessAct.getString("mpscaletype")));
                }
                return rows;
            }
        });
    }

    @Override
    public void billListHyperLinkClick(HyperLinkClickArgs args) {
        super.billListHyperLinkClick(args);
        String fieldName = args.getFieldName();
        if ("perffile_name".equals(fieldName)) {
            args.setCancel(true);
            showAssessResult();
        }
    }

    private void showAssessResult() {
        BaseShowParameter baseShowParameter = new BaseShowParameter();
        baseShowParameter.setFormId("cea_assessobjret");
        baseShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        baseShowParameter.setStatus(OperationStatus.VIEW);
        baseShowParameter.setPkId(this.getFocusRowPkId());
        baseShowParameter.setCustomParam("objId",this.getFocusRowPkId());
        DynamicObject dynamicObject = ASSESS_OBJ_DOMAIN_SERVICE.queryObjNameById(this.getFocusRowPkId());
        baseShowParameter.setPageId(this.getView().getPageId() + this.getView().getFormShowParameter().getAppId() + this.getFocusRowPkId());
        String name = dynamicObject.getString("perffile.name");
        String caption = String.format(ResManager.loadKDString("%s的能力素质考核结果", "AssessIngListPlugin_1", "cds-opmc-cea"), name);
        baseShowParameter.setHasRight(Boolean.TRUE);
        baseShowParameter.setCaption(caption);
        this.getView().showForm(baseShowParameter);
    }
}
