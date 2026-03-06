package tdkw.esc.leaderquery.report.division;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.PackageDataEvent;
import kd.bos.entity.report.ReportColumn;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.ReportList;
import kd.bos.report.ReportShowParameter;
import kd.bos.report.events.CellStyleRule;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.esc.leaderquery.common.hrmp.HRStructOrgUtils;

import java.util.*;

/**
 * @author xxx
 * @version 1.0
 * @description: TODO
 * @date 2023/10/25 0025 下午 5:39
 */

public class MergeBox extends AbstractReportFormPlugin implements HyperLinkClickListener {

    private static final Log logger = LogFactory.getLog(MergeBox.class);

    @Override
    public void setMergeColums(List<String> columns) {
        columns.add("tdkw_plate");
        columns.add("tdkw_industry");
        columns.add("tdkw_org");
        super.setMergeColums(columns);
    }

    //setFieldKey方法添加多个字段按最后一个字段取值，setCondition方法的判断条件按照行数据判断，修改单元格样式的方法在合并单元格之前调用，合并之后上面的单元格会覆盖下面的单元格会被修改
    @Override
    public void setCellStyleRules(List<CellStyleRule> cellStyleRules) {
        //由于一次只能添加一个字段 for循环需要的修改的报表列
        List<String> arrange = Arrays.asList("tdkw_plate", "tdkw_industry", "tdkw_org", "tdkw_sum", "tdkw_formalperson", "tdkw_dispatchperson", "tdkw_outsourceperson");
        for (String str : arrange) {
            //小计行颜色修改
            CellStyleRule subtotal = setSubtotalColor(str);
            //合计行颜色修改
            CellStyleRule aggregate = setAggregateColor(str);
            //总计行颜色修改
            CellStyleRule total = setTotalColor(str);
            cellStyleRules.add(subtotal);
            cellStyleRules.add(aggregate);
            cellStyleRules.add(total);
        }
        super.setCellStyleRules(cellStyleRules);
    }

    public CellStyleRule setSubtotalColor(String text) {
        CellStyleRule cellStyleRule = new CellStyleRule();
        cellStyleRule.setFieldKey(text);  //字段标识
        cellStyleRule.setForeColor("black");  //前景色
        cellStyleRule.setBackgroundColor("#e5e5e5");  //背景色
        cellStyleRule.setDegree(100);    //透明度
        cellStyleRule.setCondition("tdkw_org = '小计'"); // 前置条件，值与表达式计算器一致
        return cellStyleRule;
    }

    public CellStyleRule setAggregateColor(String text) {
        CellStyleRule cellStyleRule = new CellStyleRule();
        cellStyleRule.setFieldKey(text);  //字段标识
        // cellStyleRule.setForeColor("#e5e5e5");  //前景色
        cellStyleRule.setBackgroundColor("#d9d9d9");  //背景色
        cellStyleRule.setDegree(100);    //透明度
        cellStyleRule.setCondition("tdkw_industry = '合计'"); // 前置条件，值与表达式计算器一致
        return cellStyleRule;
    }

    public CellStyleRule setTotalColor(String text) {
        CellStyleRule cellStyleRule = new CellStyleRule();
        cellStyleRule.setFieldKey(text);  //字段标识
        // cellStyleRule.setForeColor("#e5e5e5");  //前景色
        cellStyleRule.setBackgroundColor("#cccccc");  //背景色
        cellStyleRule.setDegree(100);    //透明度
        cellStyleRule.setCondition("tdkw_plate = '总计'"); // 前置条件，值与表达式计算器一致
        return cellStyleRule;
    }

    //setMergeColums方法行数据不一致时不会合并,但是需要做空值判断
    @Override
    public void packageData(PackageDataEvent packageDataEvent) {
        ReportColumn reportColumn = (ReportColumn) packageDataEvent.getSource();
        //当前读取的列
        String fieldKey = reportColumn.getFieldKey();
        switch (fieldKey) {
            case "tdkw_plate":
                cacheData(packageDataEvent, "tdkw_plate", "cache_plate");
                break;
            case "tdkw_industry":
                cacheData(packageDataEvent, "tdkw_industry", "cache_industry");
                break;
            case "tdkw_org":
                cacheData(packageDataEvent, "tdkw_org", "cache_org");
                break;
        }
    }

    private void cacheData(PackageDataEvent packageDataEvent, String fieldKey, String cacheName) {
        //某列某行单元格的数据
        Object formatValue = packageDataEvent.getFormatValue();
        if (ObjectUtils.isNotEmpty(formatValue)) {
            String s = formatValue.toString();
            //获取上一行数据
            String condition_prevalue = this.getPageCache().get(cacheName);
            if (condition_prevalue == null || condition_prevalue.isEmpty()) {
                ;
            } else if (!condition_prevalue.equals(s)) {
                //设置取消某列当前行与上一行的合并
                packageDataEvent.getNoMergeKey().add(fieldKey);
            }
            //把当前单元格数据保存到页面缓存中
            this.getPageCache().put(cacheName, s);
        } else {
            //当前单元格为空 取消单元格合并
            packageDataEvent.getNoMergeKey().add(fieldKey);
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //监听报表列表控件
        ReportList list = (ReportList) this.getControl("reportlistap");
        list.addHyperClickListener(this);
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent e) {
        ReportShowParameter param = new ReportShowParameter();
        param.setFormId("tdkw_divisionlist_report");
        param.getOpenStyle().setTargetKey(getView().getFormShowParameter().getOpenStyle().getTargetKey());
        param.getOpenStyle().setShowType(getView().getFormShowParameter().getFormConfig().getShowType());
        ReportQueryParam reportQueryParam = new ReportQueryParam();
        Map map = createMap(e);
        reportQueryParam.setCustomParam(map);
        param.setQueryParam(reportQueryParam);
        this.getView().showForm(param);
    }

    private Map createMap(HyperLinkClickEvent e) {
        QFilter qFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("iscurrentversion", QCP.equals, true)
                .and("enable", QCP.equals, "1")
                .and("status", QCP.equals, "C");
        // TODO 报错屏蔽
       /* AuthorizedOrgResult structAdminOrgs = HRStructOrgUtils.getStructUserAdminOrgs(UserServiceHelper.getCurrentUserId(), "tdkw_division_pc");
        List<Long> hasPermOrgs = structAdminOrgs.getHasPermOrgs();
        if (!structAdminOrgs.isHasAllOrgPerm()) {
            qFilter.and("id", QCP.in, hasPermOrgs);
        }*/
        //传参map
        HashMap<String, Object> map = new HashMap<>();
        DynamicObject rowData = e.getRowData();
        //点击单元格的列名
        String fieldName = e.getFieldName();
        //点击单元行的数据
        Long orgId = rowData.getLong("tdkw_orgid");
        String orgName = rowData.getString("tdkw_org");
        String industry = rowData.getString("tdkw_industry");
        Long industryId = rowData.getLong("tdkw_industryid");
        Long plateId = rowData.getLong("tdkw_plateid");
        String plate = rowData.getString("tdkw_plate");
        HashMap<String, Object> objectHashMap = null;
        //查询权限组织
//        DynamicObjectCollection query = QueryServiceHelper.query("haos_adminorgdetail", "name,id,structlongnumber",
//                new QFilter[]{new QFilter("tdkw_divisiondimension", QCP.equals, "1"), qFilter}, null);
        DynamicObjectCollection query = QueryServiceHelper.query("haos_adminorgdetail", "name,id,structlongnumber",
                new QFilter[]{qFilter}, null);


        HashMap<Long, Object> objectObjectHashMap = new HashMap<>();
        //取到事业部权限组织 查询事业部的所有下级
        for (DynamicObject orgDatum : query) {
            Map<String, Object> map1 = new HashMap<>();
            Long id = orgDatum.getLong("id");
            String name = orgDatum.getString("name");
            String longNumber = orgDatum.getString("structlongnumber");
            List<Long> structOrgIds = HRStructOrgUtils.getStructOrgIds(id.toString(), 9999);
            map1.put("ids", structOrgIds);
            map1.put("name", name);
            map1.put("longNumber", longNumber);
            objectObjectHashMap.put(id, map1);
        }
        //判断点击行的类型是否合计过 取下级组织的id
        if (orgName.equals("小计")) {
            logger.info("industryId" + industryId);
            if (industryId == 0L) {
                if( plateId != 0L ){
                    //没有行业但是有板块
                    objectHashMap = queryAllOrg(0L,plateId);
                }else{
                    //没有行业没有板块的情况
                    objectHashMap = queryAllOrg(0L,0L);
                }
            } else {
                //正常数据
                objectHashMap = queryAllOrg(objectObjectHashMap, industryId);
            }
        } else if (industry.equals("合计")) {
            logger.info("plateId" + plateId);
            if (plateId == 0L) {
                //没有板块 暂时没有出现有行业无板块情况
                objectHashMap = queryAllOrg(0L,0L);
            } else {
                //正常数据
                objectHashMap = queryAllOrg(objectObjectHashMap, plateId);
            }
        } else if (plate.equals("总计")) {
            objectHashMap = queryAllOrg(objectObjectHashMap, null);
        } else {
            logger.info("orgId" + orgId);
            if (industryId == 0L || plateId == 0L) {
                //板块行业都没有情况
                objectHashMap = queryAllOrg(orgId,0L);
            } else {
                //正常数据
                objectHashMap = queryAllOrg(objectObjectHashMap, orgId);
            }
        }
        map.put("data", objectHashMap);
        map.put("click", fieldName);
        return map;
    }

    private HashMap<String, Object> queryAllOrg(long orgid,long plate) {
        logger.info("进入事业部报表无板块数据特殊处理方法");
        ReportList billList = this.getView().getControl("reportlistap");
        int rowCount = billList.getReportModel().getRowCount();
        List<Long> list = new ArrayList<>();
        HashMap<String, Object> dataMap = new HashMap<>();
        //遍历所有行数据
        if (orgid == 0L) {
            logger.info("报表取数开始");
            for (int index = 1; index < rowCount; index++) {
                DynamicObject rowData = billList.getReportModel().getRowData(index);
                Long orgId = rowData.getLong("tdkw_orgid");
                Long industryId = rowData.getLong("tdkw_industryid");
                Long plateId = rowData.getLong("tdkw_plateid");
                if (industryId == 0L && plateId == plate) {
                    list.add(orgId);
                }
            }
        } else {
            list.add(orgid);
        }
        QFilter qFilter = new QFilter("datastatus", QCP.equals, "1")
                .and("iscurrentversion", QCP.equals, true)
                .and("enable", QCP.equals, "1")
                .and("status", QCP.equals, "C")
                .and("id", QCP.in, list);
        logger.info("报表取数list" + list);
        logger.info("报表取数list" + list.size());
        logger.info("报表取数结束");

//        DynamicObjectCollection query = QueryServiceHelper.query("haos_adminorgdetail", "name,id,number",
//                new QFilter[]{new QFilter("tdkw_divisiondimension", QCP.equals, "1"), qFilter}, null);

        DynamicObjectCollection query = QueryServiceHelper.query("haos_adminorgdetail", "name,id,number",
                new QFilter[]{qFilter}, null);

        ArrayList<Long> idList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> mapList = new ArrayList<>();
        logger.info("开始构造数据");
        //取到事业部权限组织 查询事业部的所有下级
        for (DynamicObject orgDatum : query) {
            HashMap map = new HashMap();
            String id = orgDatum.getString("id");
            String name = (String) orgDatum.get("name");
            List<Long> ids = HRStructOrgUtils.getStructOrgIds(id.toString(), 9999);;
            map.put("orgName", name);
            map.put("affiliatedbusIds", ids);
            idList.addAll(ids);
            mapList.add(map);
        }
        logger.info("构造数据结束");
        dataMap.put("data", mapList);
        dataMap.put("list", idList);
        logger.info("构造的数据" + dataMap);
        return dataMap;
    }


    /**
     * @param objectObjectHashMap 权限组织的id 对应的下级组织id
     * @param id                  所属板块/二级板块/事业部的id
     * @return
     */
    private HashMap<String, Object> queryAllOrg(HashMap<Long, Object> objectObjectHashMap, Long id) {
        HashMap<String, Object> dataMap = new HashMap<>();
        String fatherLongNumber = null;
        if (ObjectUtils.isNotEmpty(id)) {
            //明细报表需要查询人员的组织id
            QFilter qFilter = new QFilter("datastatus", QCP.equals, "1")
                    .and("iscurrentversion", QCP.equals, "1")
                    .and("enable", QCP.equals, "1")
                    .and("status", QCP.equals, "C");
            //根据点击行类型 查询对应的行政组织数据
            DynamicObject plateData = QueryServiceHelper.queryOne("haos_adminorghr", "structlongnumber",
                    new QFilter[]{new QFilter("id", QCP.equals, id).and(qFilter)});
            logger.info("plateData" + plateData);
            //所属板块/二级板块/事业部的长编码
            if (ObjectUtils.isNotEmpty(plateData)) {
                fatherLongNumber = plateData.getString("structlongnumber");
                logger.info("fatherLongNumber" + fatherLongNumber);
            }
        }
        ArrayList<Long> idList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> mapList = new ArrayList<>();
        Set<Long> longs = objectObjectHashMap.keySet();
        for (Long longId : longs) {
            HashMap map = new HashMap();
            Map<String, Object> idMap = (Map<String, Object>) objectObjectHashMap.get(longId);
            String name = (String) idMap.get("name");
            String longNumber = (String) idMap.get("longNumber");
            List<Long> ids = (List<Long>) idMap.get("ids");
            if (StringUtils.isNotEmpty(fatherLongNumber)) {
                if (longNumber.contains(fatherLongNumber)) {
                    map.put("orgName", name);
                    map.put("affiliatedbusIds", ids);
                    idList.addAll(ids);
                    mapList.add(map);
                }
            } else {
                map.put("orgName", name);
                map.put("affiliatedbusIds", ids);
                idList.addAll(ids);
                mapList.add(map);
            }
        }

        dataMap.put("data", mapList);
        dataMap.put("list", idList);
        return dataMap;
    }


}

