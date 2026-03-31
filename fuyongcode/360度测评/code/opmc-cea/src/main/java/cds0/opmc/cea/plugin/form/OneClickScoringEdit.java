package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessFormEntityService;
import cds0.opmc.cea.business.entityservice.PerformanceLevelEntityService;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.macc.sca.common.utils.CollectionsUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 一键评分插件
 *
 * @author sxf
 * @date 2024-07-9 11:45:38
 */
public class OneClickScoringEdit extends AbstractBillPlugIn implements BeforeF7SelectListener {
    private static final PerformanceLevelEntityService PERFORMANCE_LEVEL_SERVICE = PerformanceLevelEntityService.getInstance();
    private static final String BASE_RESULT = "cds0_checklevelmap";
    private static final String ENTRY_ENTITY = "cds0_entryentity";
    private static final String BTNOK = "btnok";


    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {

    }

    @Override
    public void afterCreateNewData(EventObject e) {
        String perflevelId = this.getView().getParentView().getPageCache().get("levelMapNumber");
        DynamicObjectCollection scoresubentryentity = PERFORMANCE_LEVEL_SERVICE.getPerformanceLevel(perflevelId);
        if(scoresubentryentity != null && scoresubentryentity.size()>0) {
            //新建行并赋值
            this.getModel().batchCreateNewEntryRow(ENTRY_ENTITY, scoresubentryentity.size());
            int i = 0;
            for (DynamicObject levelObject : scoresubentryentity) {
                String scorelevel = levelObject.getString("scorelevel");
                this.getModel().setValue("option_name", scorelevel, i);
                i++;
            }
        }
    }


    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        //如果是确认按钮，则进行一键评分
        if (source.getKey().equals(BTNOK)) {
            //获取勾选项
            EntryGrid entryGrid = this.getControl(ENTRY_ENTITY);
            int selectRows[] = entryGrid.getSelectRows();
            if (selectRows != null && selectRows.length > 0) {
                //遍历所有的指标页面,并进行赋值
                String subpages = this.getView().getParentView().getPageCache().get("subpages");
                if (subpages != null && subpages != "") {
                    List<String> subpageList = Arrays.asList(subpages.split(","));
                    for (String subpage : subpageList) {
                        IFormView subView = this.getView().getView(subpage);
                        //给所有用户赋值
                        DynamicObjectCollection entryEntitys = subView.getModel().getEntryEntity("cds0_entryentity");
                        for (int i=0;i<entryEntitys.size();i++) {
                            subView.getModel().setValue(BASE_RESULT + (selectRows[0] + 1), true,i);
                        }
                        this.getView().sendFormAction(subView);
                    }
                }
            }
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners(BTNOK);
    }

}
