package tdkw.hrmp.hrobs.formplugin.frontext;

import com.google.gson.Gson;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.IFormView;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.util.CollectionUtils;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import tdkw.hrmp.hrobs.formplugin.pojo.OrgTreeVO;
import tdkw.hrmp.hrobs.formplugin.pojo.PersonalQueryParams;
import tdkw.hrmp.hrobs.formplugin.util.BuildSqlUtil;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.common.hrobs.util.PinyinUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date 2024年01月23日 11:54
 * @version: 1.0
 */
public class CustomControlDemoFormPlugin extends AbstractFormPlugin {

    private Log logger = LogFactory.getLog(CustomControlDemoFormPlugin.class);

    public static final String tdkw_CUSTOMCONTROLAP = "tdkw_customcontrolap";
    public static final String KEY = "key";
    public static final String PERSONDATA_KEY = "persondata";

    private static final String PATTERN_DATE = "yyyy-MM-dd";

    @Override
    public void customEvent(CustomEventArgs e) {
        long start = System.currentTimeMillis();
        super.customEvent(e);
        IDataModel model = this.getView().getParentView().getModel();
        IFormView view = this.getView().getParentView();
        String key = e.getEventName();
        String values = e.getEventArgs();
        logger.info("eventArgs:" + values+" key:" + key);
        // 自定义控件返回结果对象
        CustomControl customControl = this.getView().getControl(tdkw_CUSTOMCONTROLAP);
        if("persondata".equals(key)) {
            // 返回结果
            Map res = new HashMap<String, Object>();
            res.put(KEY, key);
            res.put("timestamp", System.currentTimeMillis());
            res.put("username", RequestContext.get().getUserName());
            res.put("userid", RequestContext.get().getCurrUserId());
            // 返回给前端
            customControl.setData(res);
            return;
        } else if("getPersonData".equals(key)) {
            Gson gson = new Gson();
            // 获取前端请求参数
            PersonalQueryParams params = gson.fromJson(values, PersonalQueryParams.class);
            // 截止日期
            if(StringUtils.isBlank(params.getEndDate())){
                // 格式化日期
                String today = DateFormatUtils.format(new Date(), PATTERN_DATE);
                // 没有默认今天
                params.setEndDate(today);
            }
            Map result = new HashMap<String, Object>();
            result.put(KEY, key);
            result.put("timestamp", System.currentTimeMillis());
            // 测试数据
            result.put("username", RequestContext.get().getUserName());
            result.put("userid", RequestContext.get().getCurrUserId());

            // 查询总条数
            String countSql = this.getPersonCountSql(params);
            Long total = DB.query(DBRoute.of("hr"), countSql, rs -> {
                while (rs.next()) {
                    return rs.getLong("total");
                }
                return null;
            });

            // 从数据库查询人员数据查询数据
            String querySql = this.getPersonQeurySql(params);
            List<Map<String, Object>> dataList = queryPersonDataFromDB(querySql);
            // 查询行政区划，行政区划在sys库
            Map<Long, String> admindivisionMap = getAdmindivisionMap();
            // 赋值行政区划
            for (Map<String, Object> data : dataList) {
                Object originId = data.get("originId");
                if(originId!=null){
                    data.put("origin", admindivisionMap.get(Long.parseLong(originId.toString())));
                }
            }
            result.put("dataList", dataList);
            result.put("total", total);
            result.put("pageNo", params.getPageNo());
            result.put("pageSize", params.getPageSize());
            logger.info("query DataList : " + dataList);
            // 返回给前端
            customControl.setData(result);

        } else if ("getOrgData".equals(key)) {
            // 查询组织树
            String sql = this.getOrgQuerySql();
            List<OrgTreeVO> allOrgList = DB.query(DBRoute.of("hr"), sql, rs -> {
                List<OrgTreeVO> resultList = new ArrayList<>();
                while (rs.next()) {
                    OrgTreeVO orgTreeVO = new OrgTreeVO();
                    orgTreeVO.setOrgId(rs.getString("orgId"));
                    orgTreeVO.setOrgName(rs.getString("orgName"));
                    orgTreeVO.setPid(rs.getString("pid"));
                    orgTreeVO.setSortcode(rs.getString("sortcode"));
                    resultList.add(orgTreeVO);
                }
                return resultList;
            });
            // 按照树状结构封装数据
            Map<String, List<OrgTreeVO>> orgMaps = allOrgList.stream().collect(Collectors.groupingBy(OrgTreeVO::getPid));
            List<String> orgIds = allOrgList.stream().map(orgVO -> orgVO.getOrgId()).collect(Collectors.toList());
            List<OrgTreeVO> dataList = new ArrayList<>();
            allOrgList.forEach(orgTreeVO -> {
                List<OrgTreeVO> children = orgMaps.get(orgTreeVO.getOrgId());
                if(children != null && children.size() > 0){
                    orgTreeVO.setHasChildren(true);
                    orgTreeVO.setChildren(children);
                }
                // 如果找不到上级节点，则为空
                if (!orgIds.contains(orgTreeVO.getPid())) {
                    dataList.add(orgTreeVO);
                }
            });
            Map result = new HashMap<String, Object>();
            result.put(KEY, key);
            result.put("timestamp", System.currentTimeMillis());
            result.put("dataList", dataList);
            // 返回给前端
            customControl.setData(result);
        }
        logger.info(key +" 总体耗时: " + (System.currentTimeMillis() - start) + "ms.");
    }

    private Map<Long, String> getAdmindivisionMap() {
        DynamicObject[] admindivisions = BusinessDataServiceHelper.load("bd_admindivision", "id,tdkw_address", new QFilter[0]);
        Map<Long, String> admindivisionMaps = new HashMap();
        for (DynamicObject admindivision : admindivisions) {
            admindivisionMaps.put(admindivision.getLong("id"), (String) admindivision.get("tdkw_address"));
        }
        return admindivisionMaps;
    }

    /**
     * 获取查询左侧组织树的sql
     * @return
     */
    private String getOrgQuerySql() {
        StringBuilder selectSql = new StringBuilder();
        StringBuilder fromSql = new StringBuilder();
        StringBuilder joinSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        StringBuilder orderBySql = new StringBuilder();
        selectSql.append("/*dialect*/ select fid as orgId, fname as orgName, fparentid as pid, fsortcode as sortcode");
        fromSql.append(" from t_haos_adminorg org");
        whereSql.append(" where fiscurrentversion = '1' and fenable = '1'");
        // 查询组织树过滤权限，
        AuthorizedOrgResult result = HRRoleAndPersonUtils.getAuthorizedAdminOrgSet(UserServiceHelper.getCurrentUserId(),
                "tdkw_roster_pc");
        boolean hasAllOrgPerm = result.isHasAllOrgPerm();
        if (!hasAllOrgPerm) {
            List<Long> hasPermOrgs = result.getHasPermOrgs();
            whereSql.append(" and").append(BuildSqlUtil.getInSql(" fid in ", hasPermOrgs, false));
        }
        orderBySql.append(" order by fsortcode");
        return selectSql.append(fromSql).append(whereSql).append(orderBySql).toString();
    }

    private List<Map<String, Object>> queryPersonDataFromDB(String querySql) {
        List<Map<String, Object>> dataList = DB.query(DBRoute.of("hr"), querySql, rs -> {
            List<Map<String, Object>> resultList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> data = new HashMap();

                data.put("personid", rs.getLong("personid"));
                data.put("corpName", rs.getString("corp_name"));
                data.put("deptName", rs.getString("deptName"));
                data.put("username", rs.getString("username"));
                data.put("postName", rs.getString("postName"));
                data.put("postLevel", rs.getString("postLevel"));
                data.put("labor", rs.getString("labor"));
                data.put("sex", rs.getString("sex"));// 性别
                data.put("school", rs.getString("school"));// 学校
                data.put("degree", rs.getString("degree"));// 学位
                data.put("major", rs.getString("major"));// 专业
                data.put("mari", rs.getString("mari"));// 婚姻状况
                data.put("political", rs.getString("political"));// 政治面貌
                data.put("age", rs.getString("age"));// 年龄
                data.put("serviceAge", rs.getString("serviceAge"));// 服务年限
                data.put("number", rs.getString("number"));// 工号
                data.put("originId", rs.getString("originId"));// 籍贯ID
                resultList.add(data);
            }
            return resultList;
        });
        return dataList;
    }

    private String getPersonCountSql(PersonalQueryParams params){
        StringBuilder selectSql = new StringBuilder();
        StringBuilder fromSql = new StringBuilder();
        StringBuilder joinSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        StringBuilder orderBySql = new StringBuilder();

        selectSql.append("/*dialect*/ select count(1) as total");

        buildCommonSql(params, fromSql, joinSql, whereSql);

        return selectSql.append(fromSql)
                .append(joinSql)
                .append(whereSql)
                .toString();
    }

    private void buildCommonSql(PersonalQueryParams params, StringBuilder fromSql, StringBuilder joinSql, StringBuilder whereSql) {
        // 人员任职经历基础
        fromSql.append(" from t_hrpi_empposorgrel emp ");

        // 人事业务档案
        joinSql.append(" left join t_hrpi_ermanfile efile on efile.fempposrelid = emp.fid ")
                .append(" left join t_hrpi_person b on efile.fpersonid = b.fid ")
                /*异动原因*/
                .append(" left join tk_tdkw_changereason d on emp.fk_tdkw_changereason = d.fid ")
                /*人员非时序属性*/
                .append(" left join t_hrpi_pernontsprop e on e.fpersonid = efile.fpersonid ")
                /*人员时序属性*/
                .append(" left join t_hrpi_pertsprop f on f.fpersonid = efile.fpersonid ")
                .append(" left join t_haos_adminorg corp on emp.fcompanyid = corp.fid")
                .append(" left join t_haos_adminorg dept on emp.fadminorgid  = dept.fid")
                .append(" left join t_hbpm_position post on emp.fpositionid = post.fid")
                /*异动原因*/
                .append(" left join t_hbss_laborreltype labor on emp.fk_tdkw_employtype=labor.fid  ")

                .append(" left join t_hbss_sex sex on e.fgenderid = sex.fid")
                .append(" left join t_hrpi_pereduexp edu on edu.fpersonid = efile.fpersonid")
                .append(" left join t_hbss_college college on edu.fgraduateschool = college.fid")
                .append(" left join t_hbss_diploma dip on edu.feducationid = dip.fid")
                .append(" left join t_hbss_marriagestatus mari on f.fmarriagestatusid = mari.fid")
                /*基本信息补充*/
                .append(" left join t_hrpi_perregion h on h.fpersonid = efile.fpersonid ")
                .append(" left join t_hbss_politicalstatus poli on h.fpoliticalstatusid = poli.fid ")
                /*服务年限*/
                .append(" left join t_hrpi_perserlen len on len.fpersonid = efile.fpersonid ");

        whereSql.append(" where efile.fiscurrentversion  = '1' and efile.fbusinessstatus = '1'")
                .append(" and b.fiscurrentversion = '1' and b.fdatastatus = '1'")
                // 过滤是否主职.append("and emp.fisprimary = '1'")
                .append(" and emp.finitstatus = '2' and emp.fiscurrentversion = '1' and emp.fdatastatus != '-1'")
                .append(" and d.fnumber != 'XY00017'")
                .append(" and e.fiscurrentversion = '1' AND e.fdatastatus = '1'")
                .append(" and f.fiscurrentversion = '1' AND f.fdatastatus = '1'")
                .append(" and edu.fiscurrentversion = '1' AND edu.fdatastatus = '1' AND edu.fishighestdegree = '1'")
                .append(" and h.fiscurrentversion = '1' AND h.fdatastatus = '1'")
                .append(" and len.fiscurrentversion = '1' AND len.fdatastatus = '1'");

        // 过滤权限，从页面缓存中获取数据
        AuthorizedOrgCacheHelper cacheHelper = new AuthorizedOrgCacheHelper(this);
        boolean hasAllOrgPerm = cacheHelper.getHasAllOrgPerm();
        logger.info("是否有全部组织权限:" + hasAllOrgPerm);
        if (!hasAllOrgPerm) {
            int authOrgSize = cacheHelper.getAuthOrgSize();
            if(authOrgSize > AuthorizedOrgCacheHelper.MAX_TO_CACHE_TABLE){
                // 临时表
                String tempTableName = cacheHelper.getTempTableName();
                joinSql.append(" and exists (select 1 from ").append(tempTableName).append(" temp where corp.fid = temp.forgid)");
            } else {
                List<Long> hasPermOrgs = cacheHelper.getAuthOrgIdList();
                whereSql.append(" and ").append(BuildSqlUtil.getInSql(" corp.fid in ", hasPermOrgs, false));
            }
        }
        // 列表查询条件
        // 截止时间条件
        whereSql.append(" and emp.fstartdate <= date'"+params.getEndDate()+"' and emp.fenddate >= date'"+params.getEndDate()+"' ");
        // 判断搜索的searchName中是否包含字母、正则
        String searchName = params.getSearchName();
        if (StringUtils.isNotBlank(searchName)) {
            if (searchName.matches(".*[a-zA-Z]+.*")) {
                // 包含字母 pinyin
                String pinYinName;
                try {
                    pinYinName = PinyinUtil.getPingYin(searchName);
                } catch (Exception e) {
                    // 按字符分割数组对单个文字进行转拼音
                    // 拼接汇总拼音
                    StringBuilder pinYinNameBuilder = new StringBuilder();
                    for (int i = 0; i < searchName.length(); i++) {
                        String pingYin = PinyinUtil.getPingYin(String.valueOf(searchName.charAt(i)));
                        pinYinNameBuilder.append(pingYin);
                    }
                    pinYinName = pinYinNameBuilder.toString();
                }
                whereSql.append(" and pinyin like '%" + pinYinName + "%'");
            } else {
                // 不包含字母
                whereSql.append(" and efile.fname like '%" + searchName + "%'");
            }
        }
        // 岗位层级的条件
        if (CollectionUtils.isNotEmpty(params.getPostLevel())) {
            whereSql.append(" and ").append(BuildSqlUtil.getInSql(" emp.fk_tdkw_postlevel in ", params.getPostLevel(), false));
        }
        // 学校名称模糊查询
        if(StringUtils.isNotBlank(params.getSchool())){
            whereSql.append(" and college.fname like '%" + params.getSchool() + "%'");
        }
        // 学历查询的条件
        if (CollectionUtils.isNotEmpty(params.getDegrees())) {
            whereSql.append(" and ").append(BuildSqlUtil.getInSql(" edu.feducationid in ", params.getDegrees(), false));
        }

    }

    private String getPersonQeurySql(PersonalQueryParams params) {
        StringBuilder selectSql = new StringBuilder();
        StringBuilder fromSql = new StringBuilder();
        StringBuilder joinSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        StringBuilder orderBySql = new StringBuilder();

        selectSql.append("/*dialect*/ select efile.fpersonid personid,corp.fname corp_name,dept.fname deptName,efile.fname username,post.fname postName,")
                .append("emp.fk_tdkw_postlevel postLevel,labor.fname labor, sex.fname sex,")
                .append("e.fk_tdkw_origin as originId,college.fname school,dip.fname degree,edu.fmajor major,mari.fname mari")
                .append(",poli.fname political ,poli.fid,")
                .append("e.fage age,e.fservicelen serviceAge,len.fk_tdkw_newjointime,len.fk_tdkw_serviceyear,b.fnumber number");

        buildCommonSql(params,fromSql, joinSql, whereSql);

        orderBySql.append(" order by efile.fk_tdkw_index");

        // 分页查询，DB类不支持分页查询SQL
        int limit = 20;
        if(params.getPageSize() != null && params.getPageSize() > 0){
            limit = params.getPageSize();
        }
        int offset = 0;
        if(params.getPageNo() != null && params.getPageNo() > 0){
            offset = (params.getPageNo() - 1) * limit;
        }
        String pageSql = " limit " + limit + " offset " + offset;
        return selectSql.append(fromSql)
                .append(joinSql)
                .append(whereSql)
                .append(orderBySql)
                .append(pageSql)
                .toString();
    }
}
