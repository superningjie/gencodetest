package tdkw.hrmp.hrobs.common.app.api;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.custom.annotation.*;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.app.result.CustomApiResultEx;

//import tdkw.hrmp.tdkw_appauthority.common.hrmp.HRRoleAndPersonUtils;
//import tdkw.hrmp.hrobs.common.app.result.CustomApiResultEx;
//import tdkw.hrmp.tdkw_appauthority.mservice.model.request.AdminOrgChangeParamRequest;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xxx
 * @Date: 2023/07/07
 * @Description: 领导查询-组织变动
 */
@ApiController(value = "org", desc = "组织变动查询类")
@ApiMapping("/portal/manager/myteam")
public class AdminOrgChangeStatisticApi {
    private static final Log LOGGER = LogFactory.getLog(AdminOrgChangeStatisticApi.class);

    /**
     * 本月组织新增、组织撤销、组织变动统计
     *
     * @return CustomApiResult<Map < String, Object>>
     * @Date 2023/7/7
     * @author xxx
     **/
    @ApiPostMapping("/org_count")
    public CustomApiResult<CustomApiResultEx> count() throws ParseException {
        Map<String, Object> changeCount = new HashMap<>(3);

        long currUserId = RequestContext.get().getCurrUserId();

        //初始化状态-已完成
        QFilter filter = new QFilter("initstatus", QCP.equals, "2");
        // 获取本月第一天
        Date time = getFirstDate();
        //本月
        QFilter searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.large_equals, time);
        //组织新设
        QFilter changeSceneNewFilter = new QFilter("orgchgentry.changescene", QCP.equals, 1010L);
        // 过滤 部/室/院及以下组织类型
        QFilter orgTypeFilter = getOrgType();

        //获取组织新增授权组织
        QFilter allPermFilter = null;
        HasPermOrgResult allPermOrg = PermissionServiceHelper.getAllPermOrgs(currUserId, "11", "217WYC/L9U7E", "homs_orgchgrecord", "47150e89000000ac");
        if (!allPermOrg.hasAllOrgPerm()) {
            List<Long> permOrgList = allPermOrg.getHasPermOrgs();
            allPermFilter = new QFilter("adminorg.org.id", QCP.in, permOrgList);
        }
        QFilter adminOrgFilter = null;
        AuthorizedOrgResult addOrgResult = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, "homs_orgchgrecord", "adminorg");
        if (!addOrgResult.isHasAllOrgPerm()) {
            List<Long> orgIds = addOrgResult.getHasPermOrgs();
            adminOrgFilter = new QFilter("adminorg.id", QCP.in, orgIds);
        }
        LOGGER.info("AdminOrgChangeStatisticApi#count组织新增:{}", addOrgResult.isHasAllOrgPerm() ? "全部权限" : "组织id:" + addOrgResult.getHasPermOrgs());


        //本月组织新设
        DynamicObject[] allOrgFastChgAdd = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneNewFilter, allPermFilter, orgTypeFilter});
        if (allOrgFastChgAdd.length == 0) {
            changeCount.put("addCount", 0);
        } else {
            DynamicObject[] adminOrgFastChgAdd = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneNewFilter, adminOrgFilter, orgTypeFilter});
            Set<Long> allOrgFastChgAddIds = Arrays.stream(allOrgFastChgAdd).map(item -> item.getLong("id")).collect(Collectors.toSet());
            Set<Long> adminOrgFastChgAddIds = Arrays.stream(adminOrgFastChgAdd).map(item -> item.getLong("id")).collect(Collectors.toSet());
            allOrgFastChgAddIds = allOrgFastChgAddIds.stream().filter(adminOrgFastChgAddIds::contains).collect(Collectors.toSet());
            changeCount.put("addCount", allOrgFastChgAddIds.size());
        }

        //组织撤销
        QFilter changeSceneCancelFilter = new QFilter("orgchgentry.changescene", QCP.equals, 1040L);
        //本月组织撤销
        DynamicObject[] allOrgFastChgDisable = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneCancelFilter, allPermFilter, orgTypeFilter});
        if (allOrgFastChgDisable.length == 0) {
            changeCount.put("backCount", 0);
        } else {
            DynamicObject[] adminOrgFastChgDisable = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneCancelFilter, adminOrgFilter, orgTypeFilter});
            Set<Long> allOrgFastChgDisableIds = Arrays.stream(allOrgFastChgDisable).map(item -> item.getLong("id")).collect(Collectors.toSet());
            Set<Long> adminOrgFastChgDisableIds = Arrays.stream(adminOrgFastChgDisable).map(item -> item.getLong("id")).collect(Collectors.toSet());
            allOrgFastChgDisableIds = allOrgFastChgDisableIds.stream().filter(adminOrgFastChgDisableIds::contains).collect(Collectors.toSet());
            changeCount.put("backCount", allOrgFastChgDisableIds.size());
        }

        //上级调整/信息变更
        QFilter changeSceneChangeFilter = new QFilter("orgchgentry.changescene", QCP.in, Stream.of(1020L, 1030L).collect(Collectors.toList()));
        //本月组织变更
        DynamicObject[] allOrgFastChgInfo = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneChangeFilter, allPermFilter, orgTypeFilter});
        if (allOrgFastChgInfo.length == 0) {
            changeCount.put("changeCount", 0);
        } else {
            DynamicObject[] adminOrgFastChgInfo = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneChangeFilter, adminOrgFilter, orgTypeFilter});
            Set<Long> allOrgFastChgInfoIds = Arrays.stream(allOrgFastChgInfo).map(item -> item.getLong("id")).collect(Collectors.toSet());
            Set<Long> adminOrgFastChgInfoIds = Arrays.stream(adminOrgFastChgInfo).map(item -> item.getLong("id")).collect(Collectors.toSet());
            allOrgFastChgInfoIds = allOrgFastChgInfoIds.stream().filter(adminOrgFastChgInfoIds::contains).collect(Collectors.toSet());
            changeCount.put("changeCount", allOrgFastChgInfoIds.size());
        }
        return CustomApiResultEx.success(changeCount);
    }

    /**
     * 本月组织新设列表
     *
     * @return CustomApiResult<Map < String, Object>>
     * @Date 2023/7/7
     * @author xxx
     **/
    @ApiPostMapping("/org_adddetail")
    public CustomApiResult<@ApiResponseBody Map<String, Object>> getAddDetails(@Valid @ApiRequestBody AdminOrgChangeParamRequest data) throws ParseException {
        Map<String, Object> result = new HashMap<>(7);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LOGGER.info("AdminOrgChangeStatisticApi.getAddDetails: " + data.toString());
        String orgNum = data.getOrgNum();
        Object[] orgTypeNum = data.getOrgType();
        Date start = data.getStart();
        Date end = data.getEnd();
        Integer pageNum = data.getPageNum();
        Integer pageSize = data.getPageSize();
        String name = data.getName();

        //初始化状态-已完成
        QFilter filter = new QFilter("initstatus", QCP.equals, "2");
        // 获取本月第一天
        Date time = null;
        //日期条件
        QFilter searchDateFilter = null;
        if (start != null) {
            time = start;
            searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.large_equals, start);
            if (end != null) {
                searchDateFilter.and(new QFilter("orgchgentry.chgeffecttime", QCP.less_equals, end));
            }
        }
        if (start == null && end != null) {
            searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.less_equals, end);
        }
        if (searchDateFilter == null) {
            //本月第一天
            time = getFirstDate();
            //本月
            searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.large_equals, getFirstDate());
        }
        //组织以及下级组织
        List<Long> orgIds = null;
        if (StringUtils.isNotEmpty(orgNum)) {
            orgIds = getOrgIds(orgNum);
        }
        // 过滤 部/室/院及以下组织类型
        QFilter orgTypeFilter = getOrgType();

        //获取组织新增授权组织
        long currUserId = RequestContext.get().getCurrUserId();
        QFilter allPermFilter = null;
        HasPermOrgResult allPermOrg = PermissionServiceHelper.getAllPermOrgs(currUserId, "11", "217WYC/L9U7E", "homs_orgchgrecord", "47150e89000000ac");
        if (!allPermOrg.hasAllOrgPerm()) {
            List<Long> permOrgList = allPermOrg.getHasPermOrgs();
            allPermFilter = new QFilter("adminorg.org.id", QCP.in, permOrgList);
        }

        QFilter adminOrgFilter = null;
        AuthorizedOrgResult addOrgResult = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, "homs_orgchgrecord", "adminorg");
        if (!addOrgResult.isHasAllOrgPerm()) {
            List<Long> powerOrgIds = addOrgResult.getHasPermOrgs();
            orgIds = orgIds != null ? orgIds.stream().filter(powerOrgIds::contains).collect(Collectors.toList()) : powerOrgIds;
            adminOrgFilter = new QFilter("adminorg.id", QCP.in, orgIds);
        } else {
            if (orgIds != null) {
                adminOrgFilter = new QFilter("adminorg.id", QCP.in, orgIds);
            }
        }
        LOGGER.info("AdminOrgChangeStatisticApi#count组织新增:{}", addOrgResult.isHasAllOrgPerm() ? "全部权限" : "组织id:" + addOrgResult.getHasPermOrgs());
        //行政组织编码
        if (!Objects.isNull(orgTypeNum)) {
            Set<Long> set = getOrgType(orgTypeNum);
            if (!set.isEmpty()) {
                searchDateFilter.and(new QFilter("adminorg.adminorgtype.id", QCP.in, set));
            }
        }
        //关键词
        if (StringUtils.isNotBlank(name)) {
            searchDateFilter.and(new QFilter("adminorg.name", QCP.like, "%" + name + "%"));
        }

        //组织新设
        QFilter changeSceneNewFilter = new QFilter("orgchgentry.changescene", QCP.equals, 1010L);
        DynamicObject[] dyList = new DynamicObject[0];
        //本月组织新设
        DynamicObject[] allOrgFastChgAdd = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneNewFilter, allPermFilter, orgTypeFilter});
        if (allOrgFastChgAdd.length != 0) {
            DynamicObject[] adminOrgFastChgAdd = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneNewFilter, adminOrgFilter, orgTypeFilter});
            Set<Long> allOrgFastChgAddIds = Arrays.stream(allOrgFastChgAdd).map(item -> item.getLong("id")).collect(Collectors.toSet());
            Set<Long> adminOrgFastChgAddIds = Arrays.stream(adminOrgFastChgAdd).map(item -> item.getLong("id")).collect(Collectors.toSet());
            allOrgFastChgAddIds = allOrgFastChgAddIds.stream().filter(adminOrgFastChgAddIds::contains).collect(Collectors.toSet());
            QFilter idFilter = new QFilter("id", QCP.in, allOrgFastChgAddIds);
            dyList = BusinessDataServiceHelper.load("homs_orgchgrecord", "id,adminorg,orgchgentry.chgeffecttime,orgchgentry.changescene", idFilter.toArray(), "adminorg.adminorgtype.number,adminorg.number");
        }
        //根据编码查询HR行政组织
        Set<String> adminOrgNumbers = Arrays.stream(dyList).map(org -> org.getString("adminorg.number")).collect(Collectors.toSet());
        QFilter orgFilter = new QFilter("number", QCP.in, adminOrgNumbers);
        orgFilter.and("initstatus", QCP.equals, "2");
        orgFilter.and("datastatus", QCP.equals, "1");
        orgFilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] adminOrgHr = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,tdkw_sectorid", orgFilter.toArray());

        List<Map<String, Object>> details = new ArrayList<>();
        Date finalTime = time;
        Arrays.stream(dyList).forEach(org -> {
            Map<String, Object> detail = new HashMap<>(5);
            //组织变动记录
            DynamicObjectCollection orgChgEntry = org.getDynamicObjectCollection("orgchgentry");
            //行政组织
            DynamicObject adminOrg = org.getDynamicObject("adminorg");
            if (adminOrg == null) {
                LOGGER.info("AdminOrgChangeStatisticApi#getAddDetails adminOrg:HR行政组织为NULL");
                return;
            }
            Object orgName = adminOrg.get("name") == null ? "" : ((OrmLocaleValue) adminOrg.get("name")).get("zh_CN");
            //行政组织类型
            Object orgType = adminOrg.get("adminorgtype.name") == null ? "" : ((OrmLocaleValue) adminOrg.get("adminorgtype.name")).get("zh_CN");
            //所属公司
            Object company = adminOrg.get("belongcompany.name") == null ? "" : ((OrmLocaleValue) adminOrg.get("belongcompany.name")).get("zh_CN");
            //变动生效日期
            List<DynamicObject> dynamicObjectStream = new ArrayList<>();
            for (DynamicObject entry : orgChgEntry) {
                Object pkValue = entry.getDynamicObject("changescene").getPkValue();
                if (finalTime == null) {
                    if (Objects.equals(1010L, pkValue) && entry.getDate("chgeffecttime").before(end)) {
                        dynamicObjectStream.add(entry);
                    }
                } else {
                    if (Objects.equals(1010L, pkValue) && !entry.getDate("chgeffecttime").before(finalTime)) {
                        dynamicObjectStream.add(entry);
                    }
                }
            }
            Object effectiveDate = dynamicObjectStream.isEmpty() ? "" : dynamicObjectStream.get(0).get("chgeffecttime");
            final Object[] sector = {null};
            String orgNumber = adminOrg.getString("number");
            Arrays.stream(adminOrgHr).forEach(hrOrg -> {
                String hrOrgNumber = hrOrg.getString("number");
                if (StringUtils.equals(orgNumber, hrOrgNumber)) {
                    sector[0] = hrOrg.get("tdkw_sectorid.name") == null ? "" : ((OrmLocaleValue) hrOrg.get("tdkw_sectorid.name")).get("zh_CN");
                }
            });

            detail.put("orgName", orgName);
            detail.put("orgType", orgType);
            detail.put("sector", sector[0]);
            detail.put("company", company);
            detail.put("effectiveDate", effectiveDate == null ? "" : format.format(effectiveDate));
            details.add(detail);
        });
        //从第几条开始取数
        int num = (pageNum - 1) * pageSize;
        //到第几条
        int count = pageNum * pageSize;
        //总条数
        int size = details.size();
        List<Map<String, Object>> datas = new ArrayList<>();
        for (int i = num; i < count; i++) {
            if (i >= size) {
                break;
            }
            datas.add(details.get(i));
        }
        result.put("pageSize", pageSize);
        result.put("pageNum", pageNum);
        //总条数
        result.put("total", size);
        //总页数
        result.put("pages", (int) Math.ceil(size * 1.0 / pageSize));
        result.put("data", datas);
        result.put("statusCode", 200);
        result.put("message", "success");
        return CustomApiResult.success(result);
    }

    /**
     * 本月组织撤销列表
     *
     * @return CustomApiResult<Map < String, Object>>
     * @Date 2023/7/7
     * @author xxx
     **/
    @ApiPostMapping("/org_backdetail")
    public CustomApiResult<@ApiResponseBody Map<String, Object>> getBackDetails(@Valid @ApiRequestBody AdminOrgChangeParamRequest data) throws ParseException {
        Map<String, Object> result = new HashMap<>(7);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LOGGER.info("AdminOrgChangeStatisticApi.getBackDetails: " + data.toString());
        String orgNum = data.getOrgNum();
        Object[] orgTypeNum = data.getOrgType();
        Date start = data.getStart();
        Date end = data.getEnd();
        Integer pageNum = data.getPageNum();
        Integer pageSize = data.getPageSize();
        String name = data.getName();

        //初始化状态-已完成
        QFilter filter = new QFilter("initstatus", QCP.equals, "2");

        Date time = null;
        //日期条件
        QFilter searchDateFilter = null;
        if (start != null) {
            searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.large_equals, start);
            time = start;
            if (end != null) {
                searchDateFilter.and(new QFilter("orgchgentry.chgeffecttime", QCP.less_equals, end));
            }
        }
        if (start == null && end != null) {
            searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.less_equals, end);
        }
        //开始日期、结束日期都为空
        if (searchDateFilter == null) {
            //本月第一天
            time = getFirstDate();
            //本月
            searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.large_equals, time);
        }
        //组织以及下级组织
        List<Long> orgIds = null;
        if (StringUtils.isNotEmpty(orgNum)) {
            orgIds = getOrgIds(orgNum);
        }
        // 过滤 部/室/院及以下组织类型
        QFilter orgTypeFilter = getOrgType();

        //获取组织撤销授权组织
        long currUserId = RequestContext.get().getCurrUserId();
        QFilter allPermFilter = null;
        HasPermOrgResult allPermOrg = PermissionServiceHelper.getAllPermOrgs(currUserId, "11", "217WYC/L9U7E", "homs_orgchgrecord", "47150e89000000ac");
        if (!allPermOrg.hasAllOrgPerm()) {
            List<Long> permOrgList = allPermOrg.getHasPermOrgs();
            allPermFilter = new QFilter("adminorg.org.id", QCP.in, permOrgList);
        }

        QFilter adminOrgFilter = null;
        AuthorizedOrgResult cancelOrgResult = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, "homs_orgchgrecord", "adminorg");
        if (!cancelOrgResult.isHasAllOrgPerm()) {
            List<Long> powerOrgIds = cancelOrgResult.getHasPermOrgs();
            orgIds = orgIds != null ? orgIds.stream().filter(powerOrgIds::contains).collect(Collectors.toList()) : powerOrgIds;
            adminOrgFilter = new QFilter("adminorg.id", QCP.in, orgIds);
        } else {
            if (orgIds != null) {
                adminOrgFilter = new QFilter("adminorg.id", QCP.in, orgIds);
            }
        }
        LOGGER.info("AdminOrgChangeStatisticApi#count组织撤销:{}", cancelOrgResult.isHasAllOrgPerm() ? "全部权限" : "组织id:" + cancelOrgResult.getHasPermOrgs());
        //行政组织编码
        if (!Objects.isNull(orgTypeNum)) {
            Set<Long> set = getOrgType(orgTypeNum);
            if (!set.isEmpty()) {
                searchDateFilter.and(new QFilter("adminorg.adminorgtype.id", QCP.in, set));
            }
        }
        //关键词
        if (StringUtils.isNotBlank(name)) {
            searchDateFilter.and(new QFilter("adminorg.name", QCP.like, "%" + name + "%"));
        }

        //组织停用
        QFilter changeSceneCancelFilter = new QFilter("orgchgentry.changescene", QCP.equals, 1040L);
        DynamicObject[] dyList = new DynamicObject[0];
        //本月组织停用
        DynamicObject[] allOrgFastChgDisable = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneCancelFilter, allPermFilter, orgTypeFilter});
        if (allOrgFastChgDisable.length != 0) {
            DynamicObject[] adminOrgFastChgDisable = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneCancelFilter, adminOrgFilter, orgTypeFilter});
            Set<Long> allOrgFastChgDisableIds = Arrays.stream(allOrgFastChgDisable).map(item -> item.getLong("id")).collect(Collectors.toSet());
            Set<Long> adminOrgFastChgDisableIds = Arrays.stream(adminOrgFastChgDisable).map(item -> item.getLong("id")).collect(Collectors.toSet());
            allOrgFastChgDisableIds = allOrgFastChgDisableIds.stream().filter(adminOrgFastChgDisableIds::contains).collect(Collectors.toSet());
            QFilter idFilter = new QFilter("id", QCP.in, allOrgFastChgDisableIds);
            dyList = BusinessDataServiceHelper.load("homs_orgchgrecord", "id,adminorg,orgchgentry.chgeffecttime,orgchgentry.changescene", idFilter.toArray(), "adminorg.adminorgtype.number,adminorg.number");
        }
        //根据编码查询HR行政组织
        Set<String> adminOrgNumbers = Arrays.stream(dyList).map(org -> org.getString("adminorg.number")).collect(Collectors.toSet());
        QFilter orgFilter = new QFilter("number", QCP.in, adminOrgNumbers);
        orgFilter.and("initstatus", QCP.equals, "2");
        orgFilter.and("datastatus", QCP.equals, "1");
        orgFilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject[] adminOrgHr = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,tdkw_sectorid", orgFilter.toArray());

        List<Map<String, Object>> details = new ArrayList<>();
        Date finalTime = time;
        Arrays.stream(dyList).forEach(org -> {
            Map<String, Object> detail = new HashMap<>(5);
            //组织变动记录
            DynamicObjectCollection orgChgEntry = org.getDynamicObjectCollection("orgchgentry");
            //行政组织
            DynamicObject adminOrg = org.getDynamicObject("adminorg");
            if (adminOrg == null) {
                LOGGER.info("AdminOrgChangeStatisticApi#getBackDetails adminOrg:HR行政组织为NULL");
                return;
            }
            Object orgName = adminOrg.get("name") == null ? "" : ((OrmLocaleValue) adminOrg.get("name")).get("zh_CN");
            //行政组织类型
            Object orgType = adminOrg.get("adminorgtype.name") == null ? "" : ((OrmLocaleValue) adminOrg.get("adminorgtype.name")).get("zh_CN");
            //所属公司
            Object company = adminOrg.get("belongcompany.name") == null ? "" : ((OrmLocaleValue) adminOrg.get("belongcompany.name")).get("zh_CN");
            //变动生效日期
            List<DynamicObject> dynamicObjectStream = new ArrayList<>();
            for (DynamicObject entry : orgChgEntry) {
                Object pkValue = entry.getDynamicObject("changescene").getPkValue();
                if (finalTime == null) {
                    if (Objects.equals(1040L, pkValue) && entry.getDate("chgeffecttime").before(end)) {
                        dynamicObjectStream.add(entry);
                    }
                } else {
                    if (Objects.equals(1040L, pkValue) && !entry.getDate("chgeffecttime").before(finalTime)) {
                        dynamicObjectStream.add(entry);
                    }
                }
            }
            Object effectiveDate = dynamicObjectStream.isEmpty() ? "" : dynamicObjectStream.get(0).get("chgeffecttime");
            final Object[] sector = {null};
            String orgNumber = adminOrg.getString("number");
            Arrays.stream(adminOrgHr).forEach(hrOrg -> {
                String hrOrgNumber = hrOrg.getString("number");
                if (StringUtils.equals(orgNumber, hrOrgNumber)) {
                    sector[0] = hrOrg.get("tdkw_sectorid.name") == null ? "" : ((OrmLocaleValue) hrOrg.get("tdkw_sectorid.name")).get("zh_CN");
                }
            });

            detail.put("orgName", orgName);
            detail.put("orgType", orgType);
            detail.put("sector", sector[0]);
            detail.put("company", company);
            detail.put("effectiveDate", effectiveDate == null ? "" : format.format(effectiveDate));
            details.add(detail);
        });
        //从第几条开始取数
        int num = (pageNum - 1) * pageSize;
        //到第几条
        int count = pageNum * pageSize;
        //总条数
        int size = details.size();
        List<Map<String, Object>> datas = new ArrayList<>();
        for (int i = num; i < count; i++) {
            if (i >= size) {
                break;
            }
            datas.add(details.get(i));
        }
        result.put("pageSize", pageSize);
        result.put("pageNum", pageNum);
        //总条数
        result.put("total", size);
        //总页数
        result.put("pages", (int) Math.ceil(size * 1.0 / pageSize));
        result.put("data", datas);
        result.put("statusCode", 200);
        result.put("message", "success");
        return CustomApiResult.success(result);
    }

    /**
     * 本月组织变动列表
     *
     * @return CustomApiResult<Map < String, Object>>
     * @Date 2023/7/7
     * @author xxx
     **/
    @ApiPostMapping("/org_changedetail")
    public CustomApiResult<@ApiResponseBody Map<String, Object>> getChangeDetails(@Valid @ApiRequestBody AdminOrgChangeParamRequest data) {
        Map<String, Object> result = new HashMap<>(7);
        LOGGER.info("AdminOrgChangeStatisticApi.getChangeDetails: " + data.toString());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String orgNum = data.getOrgNum();
        Object[] orgTypeNum = data.getOrgType();
        Date start = data.getStart();
        Date end = data.getEnd();
        Integer pageNum = data.getPageNum();
        Integer pageSize = data.getPageSize();
        String name = data.getName();

        try {
            //初始化状态-已完成
            QFilter filter = new QFilter("initstatus", QCP.equals, "2");
            // 获取本月第一天
            Date time = null;

            //日期条件
            QFilter searchDateFilter = null;
            if (start != null) {
                time = start;
                searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.large_equals, start);
                if (end != null) {
                    searchDateFilter.and(new QFilter("orgchgentry.chgeffecttime", QCP.less_equals, end));
                }
            }
            if (start == null && end != null) {
                searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.less_equals, end);
            }
            if (searchDateFilter == null) {
                time = getFirstDate();
                //本月
                searchDateFilter = new QFilter("orgchgentry.chgeffecttime", QCP.large_equals, time);
            }

            //组织以及下级组织
            List<Long> orgIds = null;
            if (StringUtils.isNotEmpty(orgNum)) {
                orgIds = getOrgIds(orgNum);
            }
            // 过滤 部/室/院及以下组织类型
            QFilter orgTypeFilter = getOrgType();

            //获取组织变动授权组织
            long currUserId = RequestContext.get().getCurrUserId();
            QFilter allPermFilter = null;
            HasPermOrgResult allPermOrg = PermissionServiceHelper.getAllPermOrgs(currUserId, "11", "217WYC/L9U7E", "homs_orgchgrecord", "47150e89000000ac");
            if (!allPermOrg.hasAllOrgPerm()) {
                List<Long> permOrgList = allPermOrg.getHasPermOrgs();
                allPermFilter = new QFilter("adminorg.org.id", QCP.in, permOrgList);
            }

            QFilter adminOrgFilter = null;
            AuthorizedOrgResult changeOrgResult = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(currUserId, "homs_orgchgrecord", "adminorg");
            if (!changeOrgResult.isHasAllOrgPerm()) {
                List<Long> powerOrgIds = changeOrgResult.getHasPermOrgs();
                orgIds = orgIds != null ? orgIds.stream().filter(powerOrgIds::contains).collect(Collectors.toList()) : powerOrgIds;
                adminOrgFilter = new QFilter("adminorg.id", QCP.in, orgIds);
            } else {
                if (orgIds != null) {
                    adminOrgFilter = new QFilter("adminorg.id", QCP.in, orgIds);
                }
            }
            LOGGER.info("AdminOrgChangeStatisticApi#count组织变动:{}", changeOrgResult.isHasAllOrgPerm() ? "全部权限" : "组织id:" + changeOrgResult.getHasPermOrgs());
            //行政组织编码
            if (!Objects.isNull(orgTypeNum)) {
                Set<Long> set = getOrgType(orgTypeNum);
                if (!set.isEmpty()) {
                    searchDateFilter.and(new QFilter("adminorg.adminorgtype.id", QCP.in, set));
                }
            }
            //关键词
            if (StringUtils.isNotBlank(name)) {
                searchDateFilter.and(new QFilter("adminorg.name", QCP.like, "%" + name + "%"));
            }

            //组织变动
            QFilter changeSceneChangeFilter = new QFilter("orgchgentry.changescene", QCP.in, Stream.of(1020L, 1030L).collect(Collectors.toList()));
            //本月组织变动，组织变动明细查询
            DynamicObject[] dyList = new DynamicObject[0];
            //本月组织停用
            DynamicObject[] allOrgFastChgInfo = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneChangeFilter, allPermFilter, orgTypeFilter});
            if (allOrgFastChgInfo.length != 0) {
                DynamicObject[] adminOrgFastChgInfo = BusinessDataServiceHelper.load("homs_orgchgrecord", "id", new QFilter[]{filter, searchDateFilter, changeSceneChangeFilter, adminOrgFilter, orgTypeFilter});
                Set<Long> allOrgFastChgInfoIds = Arrays.stream(allOrgFastChgInfo).map(item -> item.getLong("id")).collect(Collectors.toSet());
                Set<Long> adminOrgFastChgInfoIds = Arrays.stream(adminOrgFastChgInfo).map(item -> item.getLong("id")).collect(Collectors.toSet());
                allOrgFastChgInfoIds = allOrgFastChgInfoIds.stream().filter(adminOrgFastChgInfoIds::contains).collect(Collectors.toSet());
                QFilter idFilter = new QFilter("id", QCP.in, allOrgFastChgInfoIds);
                dyList = BusinessDataServiceHelper.load("homs_orgchgrecord", "id,adminorg,orgchgentry.chgeffecttime,orgchgentry.changescene", idFilter.toArray(), "adminorg.adminorgtype.number,adminorg.number");
            }
            //根据编码查询HR行政组织
            Set<String> adminOrgNumbers = Arrays.stream(dyList).map(org -> org.getString("adminorg.number")).collect(Collectors.toSet());
            QFilter orgFilter = new QFilter("number", QCP.in, adminOrgNumbers);
            orgFilter.and("initstatus", QCP.equals, "2");
            orgFilter.and("datastatus", QCP.equals, "1");
            orgFilter.and("iscurrentversion", QCP.equals, "1");
            DynamicObject[] adminOrgHr = BusinessDataServiceHelper.load("haos_adminorghr", "id,number,tdkw_sectorid", orgFilter.toArray());

            List<Map<String, Object>> details = new ArrayList<>();
            Date finalTime = time;
            for (DynamicObject dynamicObject : dyList) {
                Map<String, Object> detail = new HashMap<>(5);
                //组织变动记录
                DynamicObjectCollection orgChgEntry = dynamicObject.getDynamicObjectCollection("orgchgentry");
                //行政组织
                DynamicObject adminOrg = dynamicObject.getDynamicObject("adminorg");
                if (adminOrg == null) {
                    LOGGER.info("AdminOrgChangeStatisticApi#getChangeDetails adminOrg:HR行政组织为NULL");
                    break;
                }
                Object orgName = adminOrg.get("name") == null ? "" : ((OrmLocaleValue) adminOrg.get("name")).get("zh_CN");
                //行政组织类型
                Object orgType = adminOrg.get("adminorgtype.name") == null ? "" : ((OrmLocaleValue) adminOrg.get("adminorgtype.name")).get("zh_CN");
                //所属公司
                Object company = adminOrg.get("belongcompany.name") == null ? "" : ((OrmLocaleValue) adminOrg.get("belongcompany.name")).get("zh_CN");
                List<DynamicObject> dynamicObjectStream = new ArrayList<>();
                List<Map<String, Object>> subDetails = new ArrayList<>();
                for (DynamicObject entry : orgChgEntry) {
                    Object pkValue = entry.getDynamicObject("changescene").getPkValue();
                    DynamicObject object = null;
                    //变动场景: 组织更名、上级调整
                    boolean flag = Objects.equals(1020L, pkValue) || Objects.equals(1030L, pkValue);
                    if (finalTime == null) {
                        //在结束时间之前
                        if (flag && entry.getDate("chgeffecttime").before(end)) {
                            dynamicObjectStream.add(entry);
                            object = entry;
                        }
                    } else {
                        //在开始时间之后
                        if (flag && !entry.getDate("chgeffecttime").before(finalTime)) {
                            dynamicObjectStream.add(entry);
                            object = entry;
                        }
                    }
                    if (object != null) {
                        Map<String, Object> subDetail = new HashMap<>(4);
                        Object pkValue2 = object.getPkValue();
                        QFilter entryFilter = new QFilter("entryid", QCP.equals, pkValue2);
                        DynamicObject subEntryEntity = QueryServiceHelper.queryOne("homs_subentryentity", "chgentitynumber,chgpageelement,beforechgentity,afterchgentity", entryFilter.toArray());
                        if (subEntryEntity != null) {
                            //变动实体
                            String entityNumber = subEntryEntity.getString("chgentitynumber");
                            //变动实体页面标识
                            String chgPageElement = subEntryEntity.getString("chgpageelement");
                            //变更前实体ID
                            Long beforeChgEntityId = subEntryEntity.getLong("beforechgentity");
                            DynamicObject beforeChgEntity = BusinessDataServiceHelper.loadSingle(beforeChgEntityId, entityNumber);
                            Object beforeValue = getValue(beforeChgEntity.get(chgPageElement));
                            //变更后实体ID
                            Long afterChgEntityId = subEntryEntity.getLong("afterchgentity");
                            DynamicObject afterChgEntity = BusinessDataServiceHelper.loadSingle(afterChgEntityId, entityNumber);
                            String elementContent = afterChgEntity.getDataEntityType().getProperties().get(chgPageElement).getDisplayName().getLocaleValue_zh_CN();
                            Object afterValue = getValue(afterChgEntity.get(chgPageElement));
                            subDetail.put("elementContent", elementContent);
                            subDetail.put("beforeVal", beforeValue == null ? "" : beforeValue);
                            subDetail.put("afterVal", afterValue == null ? "" : afterValue);
                            subDetails.add(subDetail);
                        }
                        detail.put("changeScene", object.get("changescene") == null ? "" : object.getDynamicObject("changescene").getString("name"));
                    }
                }
                detail.put("changeDetails", subDetails);
                //变动生效日期
                Object effectiveDate = dynamicObjectStream.isEmpty() ? null : dynamicObjectStream.get(0).get("chgeffecttime");
                final Object[] sector = {null};
                String orgNumber = adminOrg.getString("number");
                Arrays.stream(adminOrgHr).forEach(hrOrg -> {
                    String hrOrgNumber = hrOrg.getString("number");
                    if (StringUtils.equals(orgNumber, hrOrgNumber)) {
                        sector[0] = hrOrg.get("tdkw_sectorid.name") == null ? "" : ((OrmLocaleValue) hrOrg.get("tdkw_sectorid.name")).get("zh_CN");
                    }
                });

                detail.put("orgName", orgName);
                detail.put("orgType", orgType);
                detail.put("sector", sector[0]);
                detail.put("company", company);
                detail.put("effectiveDate", effectiveDate == null ? "" : format.format(effectiveDate));
                details.add(detail);
            }
            //从第几条开始取数
            int num = (pageNum - 1) * pageSize;
            //到第几条
            int count = pageNum * pageSize;
            //总条数
            int size = details.size();
            List<Map<String, Object>> datas = new ArrayList<>();
            for (int i = num; i < count; i++) {
                if (i >= size) {
                    break;
                }
                datas.add(details.get(i));
            }
            result.put("pageSize", pageSize);
            result.put("pageNum", pageNum);
            //总条数
            result.put("total", size);
            //总页数
            result.put("pages", (int) Math.ceil(size * 1.0 / pageSize));
            result.put("data", datas);
            result.put("statusCode", 200);
            result.put("message", "success");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
            return CustomApiResult.fail("500", e.getMessage());
        }
        return CustomApiResult.success(result);
    }

    /**
     * 组织变动明细
     *
     * @param dynamicObjectStream 本月组织变动明细
     * @return 变动明细
     */
//    @NotNull
    private List<Map<String, Object>> getSubDetails(List<DynamicObject> dynamicObjectStream) {
        //组织变动明细
        List<Map<String, Object>> subDetails = new ArrayList<>();
        dynamicObjectStream.forEach(dyObj -> {
            Map<String, Object> subDetail = new HashMap<>(4);
            Object pkValue = dyObj.getPkValue();
            QFilter entryFilter = new QFilter("entryid", QCP.equals, pkValue);
            DynamicObject subEntryEntity = QueryServiceHelper.queryOne("homs_subentryentity", "chgentitynumber,chgpageelement,beforechgentity,afterchgentity", entryFilter.toArray());
            if (subEntryEntity != null) {
                //变动实体
                String entityNumber = subEntryEntity.getString("chgentitynumber");
                //变动实体页面标识
                String chgPageElement = subEntryEntity.getString("chgpageelement");
                //变更前实体ID
                Long beforeChgEntityId = subEntryEntity.getLong("beforechgentity");
                DynamicObject beforeChgEntity = BusinessDataServiceHelper.loadSingle(beforeChgEntityId, entityNumber);
                Object beforeValue = getValue(beforeChgEntity.get(chgPageElement));
                //变更后实体ID
                Long afterChgEntityId = subEntryEntity.getLong("afterchgentity");
                DynamicObject afterChgEntity = BusinessDataServiceHelper.loadSingle(afterChgEntityId, entityNumber);
                String elementContent = afterChgEntity.getDataEntityType().getProperties().get(chgPageElement).getDisplayName().getLocaleValue_zh_CN();
                Object afterValue = getValue(afterChgEntity.get(chgPageElement));
                subDetail.put("changeScene", dyObj.get("changescene") == null ? "" : dyObj.getDynamicObject("changescene").getString("name"));
                subDetail.put("elementContent", elementContent);
                subDetail.put("beforeVal", beforeValue == null ? "" : beforeValue);
                subDetail.put("afterVal", afterValue == null ? "" : afterValue);
                subDetails.add(subDetail);
            }
        });
        return subDetails;
    }

    /**
     * 获取详细值
     *
     * @param obj 变动字段值
     * @return 详细值
     */
    private Object getValue(Object obj) {
        Object val;
        if (obj instanceof ILocaleString) {
            val = ((ILocaleString) obj).getLocaleValue_zh_CN();
        } else if (obj instanceof DynamicObject) {
            val = ((DynamicObject) obj).getString("name");
        } else if (obj instanceof Boolean) {
            val = ((Boolean) obj) ? "是" : "否";
        } else {
            val = obj;
        }
        return val;
    }

    /**
     * 获得所在月份的第一天
     *
     * @return java.util.Date
     * @Date 2023/7/10
     * @author xxx
     **/
    private Date getFirstDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = calendar.getTime();
        return sdf.parse(sdf.format(firstDayOfMonth));
    }

    /**
     * 查询当前组织以及下级组织的ID
     *
     * @param orgNum 当前组织编码
     * @return 组织ID
     */
    private static List<Long> getOrgIds(String orgNum) {
        List<Long> list = new ArrayList<>();
        if (StringUtils.isEmpty(orgNum)) {
            return list;
        }
        QFilter qFilter = new QFilter("number", QCP.equals, orgNum);
        QFilter isCurrentVersionFilter = new QFilter("iscurrentversion", QCP.equals, "1");
        DynamicObject object = QueryServiceHelper.queryOne("haos_adminorgdetail", "id", qFilter.and(isCurrentVersionFilter).toArray());
        List<Long> ids = new ArrayList<>();
        if (object == null) {
            return list;
        } else {
            list.add(object.getLong("id"));
            ids.add(object.getLong("id"));
        }
        do {
            QFilter qFilter1 = new QFilter("parentorg.id", QCP.in, ids);
            DynamicObjectCollection objectCollection = QueryServiceHelper.query("haos_adminorgdetail", "id", qFilter1.and(isCurrentVersionFilter).toArray());
            ids.clear();
            if (objectCollection.size() > 0) {
                for (DynamicObject dynamicObject : objectCollection) {
                    Long id = dynamicObject.getLong("id");
                    ids.add(id);
                    list.add(id);
                }
            }
        } while (ids.size() != 0);

        return list;
    }

    private static Set<Long> getOrgType(Object[] orgType) {
        Set<Long> set = new HashSet<>();
        QFilter qf = new QFilter("number", QCP.in, orgType);
        DynamicObjectCollection dynamicObject = QueryServiceHelper.query("haos_adminorgtype", "id", qf.toArray());
        if (dynamicObject == null || dynamicObject.size() == 0) {
            return set;
        } else {
            for (DynamicObject object : dynamicObject) {
                set.add(object.getLong("id"));
            }
        }
        return set;
    }

    /**
     * 过滤部/室/院及以下组织类型
     *
     * @return
     */
    private static QFilter getOrgType() {
        // 2023年10月9日20:04:17 #101379 【领导自助PC\APP】组织新增、撤销、变动，过滤部/室/院及以下组织类型的信息 http://ones.xxx.com/project/#/team/JbjqrWit/task/DBzAg9wBToAayAlx
        List<String> orgTypeList = new ArrayList<>();
        orgTypeList.add("XY00007");
        orgTypeList.add("XY00008");
        orgTypeList.add("1030_S");
        orgTypeList.add("1040_S");
        return new QFilter("adminorg.adminorgtype.number", QCP.not_in, orgTypeList);
    }
}
