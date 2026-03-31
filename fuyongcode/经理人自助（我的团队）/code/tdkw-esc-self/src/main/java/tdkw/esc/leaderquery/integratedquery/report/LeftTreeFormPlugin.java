package tdkw.esc.leaderquery.integratedquery.report;


import com.google.common.collect.Lists;
import kd.bamp.mbis.common.mega.model.OrgViewType;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.entity.tree.TreeNode;
import kd.bos.exception.KDBizException;
import kd.bos.form.control.TreeView;
import kd.bos.form.control.events.TreeNodeClickListener;
import kd.bos.form.control.events.TreeNodeEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.org.model.OrgTreeParam;
import kd.bos.report.events.SearchEvent;
import kd.bos.report.filter.ReportFilter;
import kd.bos.report.filter.SearchListener;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.constants.HRBaseConstants;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;

/**
 * TestFormPlugin
 *
 * @author xxx
 * @date 2023/6/15
 */
public class LeftTreeFormPlugin extends AbstractReportFormPlugin implements TreeNodeClickListener, SearchListener {
    private static final Log logger = LogFactory.getLog(LeftTreeFormPlugin.class);

    /**
     * 过滤条件输入值校验
     *
     * @param queryParam
     */
    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        super.beforeQuery(queryParam);
        //集团司龄
        FilterItemInfo cliqueStart = queryParam.getFilter().getFilterItem("tdkw_comagestart");
        FilterItemInfo cliqueEnd = queryParam.getFilter().getFilterItem("tdkw_comageend");
        if (ObjectUtils.isNotEmpty(cliqueStart) && ObjectUtils.isNotEmpty(cliqueEnd)) {
            Integer cliqueStartInt = (Integer) cliqueStart.getValue();
            Integer cliqueEndInt = (Integer) cliqueEnd.getValue();
            boolean check = check(cliqueStartInt, cliqueEndInt);
            if (check) {
                logger.info("集团司龄输入有误");
                throw new KDBizException("集团司龄输入数据有误,请检查!");
            }
        }
        //社会工龄
        FilterItemInfo socialStart = queryParam.getFilter().getFilterItem("tdkw_social_workage");
        FilterItemInfo socialEnd = queryParam.getFilter().getFilterItem("tdkw_social_workagen");
        if (ObjectUtils.isNotEmpty(socialStart) && ObjectUtils.isNotEmpty(socialEnd)) {
            Integer socialStartInt = (Integer) socialStart.getValue();
            Integer socialEndInt = (Integer) socialEnd.getValue();
            boolean check = check(socialStartInt, socialEndInt);
            if (check) {
                logger.info("社会工龄输入有误");
                throw new KDBizException("社会工龄输入数据有误,请检查!");
            }
        }
        //年龄
        FilterItemInfo ageStart = queryParam.getFilter().getFilterItem("tdkw_agestart");
        FilterItemInfo ageEnd = queryParam.getFilter().getFilterItem("tdkw_ageend");
        if (ObjectUtils.isNotEmpty(ageStart) && ObjectUtils.isNotEmpty(ageEnd)) {
            Integer ageStartInt = (Integer) ageStart.getValue();
            Integer ageEndInt = (Integer) ageEnd.getValue();
            boolean check = check(ageStartInt, ageEndInt);
            if (check) {
                logger.info("年龄输入有误");
                throw new KDBizException("年龄输入数据有误,请检查!");
            }
        }
        //公司司龄
        FilterItemInfo companyStart = queryParam.getFilter().getFilterItem("tdkw_company_agestart");
        FilterItemInfo companyEnd = queryParam.getFilter().getFilterItem("tdkw_company_ageend");
        if (ObjectUtils.isNotEmpty(companyStart) && ObjectUtils.isNotEmpty(companyEnd)) {
            Integer companyStartInt = (Integer) companyStart.getValue();
            Integer companyEndInt = (Integer) companyEnd.getValue();
            boolean check = check(companyStartInt, companyEndInt);
            if (check) {
                logger.info("公司司龄输入有误");
                throw new KDBizException("公司司龄输入数据有误,请检查!");
            }
        }

    }

    /**
     * 数据合法性校验
     *
     * @param start
     * @param end
     * @return 没有通过返回true
     */
    private Boolean check(Integer start, Integer end) {
        boolean check = false;
        if (start > end || start < 0 || start > 120 || end > 120) {
            check = true;
        }
        return check;
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        this.buildTreeNode();
        getView().refresh();
        // ReportFilter rf = this.getView().getControl("reportfilterap");
        // rf.setCollapse(false);

    }


    /**
     * 构建树  论坛方法
     */
    private void buildTreeNode1() {
        OrgTreeParam param = new OrgTreeParam();
        param.setOrgViewNumber(OrgViewType.OrgUnit);
        TreeNode rootNode = OrgUnitServiceHelper.getTreeRootNodeById(param);
        TreeView tv1 = this.getView().getControl("tdkw_treeviewap");
        getTreeChildren(rootNode.getId(), rootNode);
        tv1.addNode(rootNode);
    }


    /**
     * 构建树  改造方法
     */
    private void buildTreeNode() {
        TreeView tv1 = this.getView().getControl("tdkw_treeviewap");
        Date date = new Date();
        //这里要换成取当前用户有权限的行政组织范围，来构造组织树
        List<Map<String, Object>> list = HRMServiceHelper.invokeHRMPService("haos",
                "IHAOSBatchAdminOrgInfoQueryService",
                "batchGetAllSubOrg",
                Lists.newArrayList(100000l), date);
        list.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Map o11 = (Map) o1;
                Map o12 = (Map) o2;
                String orgNumber = (String) o11.get("orgNumber");
                String orgNumber1 = (String) o12.get("orgNumber");
                return orgNumber.compareTo(orgNumber1);
            }
        });
        TreeNode rootNode = new TreeNode();
        rootNode.setId("100000");
        rootNode.setText("XXX集团");
        tv1.addNode(rootNode);

        TreeView treeView = this.getView().getControl("tdkw_treeviewap");
        List<TreeNode> nodes = new ArrayList<>(HRBaseConstants.INT_CAPACITY);
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> row = list.get(i);
            TreeNode treeNode = new TreeNode();
            treeNode.setId(row.get("orgId").toString());
            treeNode.setText((String) row.get("orgName"));
            treeNode.setParentid(String.valueOf(row.get("parentOrgId")));
            // treeNode.setLeaf(false);
            nodes.add(treeNode);
        }
        for (TreeNode childNode : nodes) {
            getTreeChildren(childNode.getId(), childNode);
            childNode.setExpend(false);
            tv1.addNode(childNode);
        }
        //  tdkw.hrmp.hrobs.formplugin.EmployeePositionTask
    }

    /**
     * 递归获取子节点
     *
     * @param parentId
     * @param rootNode
     * @return
     */
    private List<TreeNode> getTreeChildren(String parentId, TreeNode rootNode) {
        OrgTreeParam param = new OrgTreeParam();
        param.setOrgViewNumber(OrgViewType.OrgUnit);
        param.setId(Long.valueOf(parentId));
        List<TreeNode> rootInfoTree = OrgUnitServiceHelper.getTreeChildren(param);
        if (rootNode.getId().equals(parentId)) {
            rootNode.addChildren(rootInfoTree);
        }
        if (rootInfoTree.size() > 0) {
            for (TreeNode treeNode : rootInfoTree) {

                List<TreeNode> children = getTreeChildren(treeNode.getId(), rootNode);
                if (children.size() > 0) {
                    treeNode.addChildren(children);
                }
            }
        }
        return rootInfoTree;
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        TreeView tv1 = this.getView().getControl("tdkw_treeviewap");
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.addSearchListener(this);
        tv1.addTreeNodeClickListener(this);
    }

    @Override
    public void treeNodeClick(TreeNodeEvent evt) {
        TreeNodeClickListener.super.treeNodeClick(evt);
        String orgId = evt.getNodeId().toString();
        Map<String, Object> customParam = new HashMap<>();
        customParam.put("orgId", orgId);
        this.getQueryParam().setCustomParam(customParam);
        this.getView().refresh();
    }

    /**
     * 搜索后展开过滤框
     *
     * @param searchEvent
     */
    @Override
    public void search(SearchEvent searchEvent) {
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);
    }


}
