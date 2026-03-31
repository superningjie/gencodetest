package tdkw.hrmp.hrobs.formplugin.report.sendtip;

import com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.NumberFormatProvider;
import kd.bos.entity.datamodel.events.BeforeImportDataEventArgs;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.report.ReportView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.IReportView;
import kd.bos.report.ReportList;
import kd.bos.report.events.CreateFilterInfoEvent;
import kd.bos.report.events.SortAndFilterEvent;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Metadata： tdkw_personfileintegrity
 * @Description ： 人员档案完整度-报表
 * @ClassName ：PersonFileIntegrityReprotFormPlugin
 * @author xxx
 * @Date ：2023/6/14 15:37
 * @Version: 1.0
 */
public class PersonFileIntegrityReprotFormPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {

    private static final Log Logger = LogFactory.getLog(PersonFileIntegrityReprotFormPlugin.class);
    public static String filterField = "tdkw_relationship,tdkw_contactcellphone,tdkw_contactname,tdkw_emily,tdkw_phone,tdkw_absenceresume,tdkw_resumestart," + "tdkw_maritalstatus,tdkw_politicalstatus,tdkw_age,tdkw_worktime,tdkw_permanentaddress,tdkw_homeaddress,tdkw_domicile,tdkw_admissiontime," + "tdkw_birthplace,tdkw_degree,tdkw_educational";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        IReportView view = this.getView();
        ReportList reportList = getControl("reportlistap");
        reportList.addHyperClickListener(this);


    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent evt) {
        //点击获取人事业务档案的主键(隐藏列)
        DynamicObject rowData = evt.getRowData();
        //参数为查询配置的查询字段的标识
        Long personId = Long.valueOf(rowData.getString("tdkw_person"));
        QFilter qFilter = new QFilter("id", QCP.equals, personId);
        DynamicObject person = BusinessDataServiceHelper.loadSingle("hrpi_person", "tdkw_pkid", qFilter.toArray());
        String pkId = person.getString("tdkw_pkid");
        Logger.info("跳转的人员pkid为" + pkId);

        try {
            FormShowParameter formShowParameter = new FormShowParameter();
            formShowParameter.setFormId("tdkw_hrobs_erfilelistdv");
            formShowParameter.setCustomParam("erfileId", pkId);
            formShowParameter.getOpenStyle().setShowType(ShowType.NewWindow);
            formShowParameter.setHasRight(true);
            this.getView().showForm(formShowParameter);
        } catch (Exception e) {
            this.getView().showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "PersonFileIntegrityReprotFormPlugin", "tdkw-esc-tdkw_integrated_query-report-ext", new Object[0]));
        }
//                DynamicObject rowData = evt.getRowData();
//                String id = rowData.getString("tdkw_hrid");
//                List<Long> ids = new ArrayList<>();
//                ids.add(Long.valueOf(id));
//                try {
//                    Map<String, Object> retMap = (Map) HRMServiceHelper.invokeHRService("hspm", "IHSPMService", "jumpErManFileDetail", new Object[]{Long.valueOf(id), "hspm_erfilelist"});
//                    if ((Boolean) retMap.get("success")) {
//                        FormShowParameter formShowParameter = (FormShowParameter) retMap.get("data");
//                        formShowParameter.setCustomParam("ermanFileIds", ids);
//                        view.showForm(formShowParameter);
//                    } else {
//                        view.showErrorNotification((String) retMap.get("errormsg"));
//
//                    }
//                } catch (Exception var9) {
//                    view.showErrorNotification(ResManager.loadKDString("程序异常，请联系管理员", "PersonFileIntegrityReprotFormPlugin_0", "tdkw.hrmp.hrobs.formplugin.report", new Object[0]));
//                }
//            }
    }

    @Override
    public void setSortAndFilter(List<SortAndFilterEvent> allColumns) {
        super.setSortAndFilter(allColumns);
        for (SortAndFilterEvent allColumn : allColumns) {
            if (StringUtils.equals("tdkw_name", allColumn.getColumnName())) {
                allColumn.setFilter(true);
                allColumn.setSort(true);
            }
        }
    }

    @Override
    public void beforeCreateFilterInfo(CreateFilterInfoEvent event) {
        super.beforeCreateFilterInfo(event);
        if (event.getFieldKey().equals("tdkw_name")) {
            ArrayList<HashMap> filterItemList = new ArrayList();
            HashMap filterItem = new HashMap();
            filterItem.put("name", new LocaleString("等于"));
            filterItem.put("value", "");
            filterItem.put("inputCtlType", 1);
            //17-in ,67-=
            filterItem.put("id", "67");
            filterItemList.add(filterItem);
            Map filterInfoMap = new HashMap();
            filterInfoMap.put("filterItems", filterItemList);
            event.setFilterInfo(filterInfoMap);
            event.setCancel(true);
        }
        super.beforeCreateFilterInfo(event);
    }


    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        ReportView source = (ReportView) e.getSource();
        Map<String, Object> customParams = source.getFormShowParameter().getCustomParams();
        Set<Long> personIds = deleteNoPrimary(customParams);

        Boolean allPerson = (Boolean) this.getModel().getValue("tdkw_allperson");
        Map<String, Object> params = Maps.newHashMap();
        params.put("personIds_a", personIds);

        params.put("allperson", allPerson);
        source.getQueryParam().setCustomParam(params);
        source.refresh();
        this.getView().getPageCache().put("flag", "true");
        this.getView().getPageCache().put("listPerson", personIds.toString());
        this.getView().getPageCache().put("personSize", String.valueOf(personIds.size()));
//        getView().refresh();
    }


    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        String operateKey = args.getOperateKey();
        // 获取报表控件
        ReportList billList = this.getView().getControl("reportlistap");
        int[] selectedRows = billList.getEntryState().getSelectedRows();
        switch (operateKey) {
            case "sendtip":
                //获取选中行集合
                if (selectedRows.length==0){
                    this.getView().showErrorNotification("请先选择行！");
                    return;
                }
                DynamicObjectCollection rowData = billList.getReportModel().getRowData(0, Integer.parseInt(this.getView().getPageCache().get("personSize")), true);
//                按报表隐藏列获取人员信息
                List<Long> selectRows = rowData.stream().map(person -> person.getLong("tdkw_person")).collect(Collectors.toList());
                String personList = this.getView().getPageCache().get("listPerson");
                personList = personList.replace("[", "").replace("]", ""); // 去除方括号
                String[] stringArray = personList.split(","); // 拆分为字符串数组
                List<Long> listPerson = new ArrayList<>();
                for (String str : stringArray) {
                    str = str.trim();
                    listPerson.add(Long.parseLong(str));
                }
                Logger.info("人员信息：" + selectRows);
                FormShowParameter fsp = new FormShowParameter();
                fsp.setFormId("tdkw_sendtip");
                fsp.setCustomParam("selectRows", selectRows);
                fsp.setCustomParam("listPerson", listPerson);
                fsp.setCustomParam("isNotVisible", "true");
                fsp.getOpenStyle().setShowType(ShowType.Modal);
                this.getView().showForm(fsp);
                break;
            case "checkall":
                String flag = this.getView().getPageCache().get("flag");
                // 一键全选
                if (flag.equals("true")) {
                    billList.selectAllRows();
                    if (billList.getEntryState().isSelectAllRows()) {
                        String personSize = this.getView().getPageCache().get("personSize");
                        int n = Integer.parseInt(personSize);
                        int[] result = new int[n];
                        for (int i = 0; i < n; i++) {
                            result[i] = i + 1;
                        }
                        billList.clearSelection();
                        billList.selectRows(result, 1);
                    }
                    this.getView().getPageCache().put("flag", "false");
                } else {
                    //取消全选
                    billList.clearSelection();
                    selectedRows = null;
                    this.getView().getPageCache().put("flag", "true");
                }
                break;
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        ChangeData changeData = e.getChangeSet()[0];
        if (StringUtils.equals("tdkw_choose", name)) {
            boolean newValue = (boolean) changeData.getNewValue();
            String[] filterFields = filterField.split(",");
            for (String field : filterFields) {
                this.getModel().setValue(field, newValue);
            }
        }
        if (StringUtils.equals("tdkw_allperson", name)) {
            boolean newValue = (boolean) changeData.getNewValue();
            ReportView source = (ReportView) this.getView();
            Map<String, Object> customParam = source.getQueryParam().getCustomParam();
            customParam.put("allperson", newValue);
            source.getQueryParam().setCustomParam(customParam);
            source.refresh();
        }
    }

//    @Override
//    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {
//        super.processRowData(gridPK, rowData, queryParam);
//        /**
//         * 完整度小数处理加%
//         */
//        for (DynamicObject rowDatum : rowData) {
//            String integrity = (String) rowDatum.get("tdkw_integrity");
//            if (StringUtils.isNotBlank(integrity)) {
//                BigDecimal integrityBD = new BigDecimal(integrity);
//                BigDecimal result = integrityBD.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
//                rowDatum.set("tdkw_integrity", String.valueOf(result) + "%");
//            }
//            String lackField = getCurrPersonLackField((String) rowDatum.get("tdkw_hrid"));
//            rowDatum.set("tdkw_lackfield", lackField);
//        }
//
//    }

    @Override
    public void preProcessExportData(List<AbstractReportColumn> exportColumns, DynamicObjectCollection data, NumberFormatProvider numberFormatProvider) {

//        ReportColumn abstractReportColumn = (ReportColumn) exportColumns.get(10);
//        DynamicProperty fieldProperty = abstractReportColumn.getFieldProperty();
//        List<FmtField> fmtFields = numberFormatProvider.getFmtFields();
//
//        FmtField fmtField = new FmtField(fieldProperty ,"tdkw_lackfield",null);
//        fmtFields.add(fmtField);
//        exportColumns.add(abstractReportColumn);
//
        super.preProcessExportData(exportColumns, data, numberFormatProvider);
    }

    @Override
    public void beforeImportData(BeforeImportDataEventArgs e) {
        super.beforeImportData(e);
    }


    public String getCurrPersonLackField(String personId) {

        String selectProperties = "id,tdkw_person,tdkw_company,tdkw_post";
        QFilter qFilter = new QFilter("status", "=", "C");
        qFilter.and("enable", "=", "1");
        DynamicObject[] personnellackof = BusinessDataServiceHelper.load("tdkw_hspm_personnellackof", "id,name,tdkw_number", qFilter.toArray());
        Map<String, String> lockPropertiesMapName = new HashMap<>();
        for (DynamicObject item : personnellackof) {
            String number = item.getString("tdkw_number");
            selectProperties = selectProperties + "," + number;
            lockPropertiesMapName.put(number, item.getLocaleString("name").getLocaleValue_zh_CN());
        }
        QFilter personnelinforQFilter = new QFilter("tdkw_person", QCP.equals, Long.valueOf(personId));
        DynamicObject[] personInfos = BusinessDataServiceHelper.load("tdkw_hspm_personnelinfor", selectProperties, personnelinforQFilter.toArray());
        String lackofinformation = "";
        for (DynamicObject personInfo : personInfos) {

            // Long tdkwPerson = (Long) personInfo.get("tdkw_person");
            for (String lockProperty : lockPropertiesMapName.keySet()) {
                if (personInfo.getBoolean(lockProperty)) {
                    if (org.apache.commons.lang3.StringUtils.isEmpty(lackofinformation)) {
                        lackofinformation = lockPropertiesMapName.get(lockProperty);
                    } else {
                        lackofinformation = lackofinformation + ";" + lockPropertiesMapName.get(lockProperty);
                    }
                }
            }
        }
        return lackofinformation;
    }

    /**
     * 去除不是主任职的人员id
     *
     * @param customParams
     * @return
     */
    public Set<Long> deleteNoPrimary(Map<String, Object> customParams) {
        Logger.info("人员完整度报表，接收参数：" + customParams);
        Set<Long> personIdsNoPrimary = new HashSet<>();
        String personIds = (String) customParams.get("personIds");
        String orgIdString = (String) customParams.get("orgId");

        List<Long> orgIds = new ArrayList<>();
        if (StringUtils.isNotBlank(orgIdString)) {
            orgIds.add(Long.parseLong(orgIdString));
        }
        List<Long> allBelowHROrg = SendTipFieldUtils.getAllBelowHROrg(orgIds, new ArrayList<>());
        if (!allBelowHROrg.contains(customParams.get("orgId"))){
            allBelowHROrg.add(0,Long.valueOf(customParams.get("orgId").toString()));
        }
        Logger.info("人员完整度报表，获取到下级组织集合：" + allBelowHROrg);
        QFilter filter1 = new QFilter("empposrel.isprimary", QCP.equals, "1");
        filter1.and("iscurrentversion", QCP.equals, "1");
        filter1.and("initstatus", QCP.equals, "2");
        filter1.and("businessstatus", QCP.equals, "1");
        if (StringUtils.isNotEmpty(personIds) && !StringUtils.equals("[]", personIds)) {
            String[] ids = personIds.substring(1, personIds.length() - 1).replaceAll(" ", "").split(",");
            // 人员id
            List<Long> collect = Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
            filter1.and("person", QCP.in, collect);
        }
        Logger.info("人员完整度报表，构建条件：" + filter1);
        DynamicObject[] hspmErmanfiles = BusinessDataServiceHelper.load("hrpi_empposorgrel", "id,person,company,adminorg", filter1.toArray());
        for (DynamicObject hspmErmanfile : hspmErmanfiles) {
            long personId = hspmErmanfile.getLong("person.id");
            long companyId = hspmErmanfile.getLong("company.id");
            long adminorg = hspmErmanfile.getLong("adminorg.id");
            if (allBelowHROrg.contains(companyId) || allBelowHROrg.contains(adminorg)) {
                personIdsNoPrimary.add(personId);
            }
        }
        Logger.info("人员完整度报表，结果：" + personIdsNoPrimary);
        return personIdsNoPrimary;
    }
}
