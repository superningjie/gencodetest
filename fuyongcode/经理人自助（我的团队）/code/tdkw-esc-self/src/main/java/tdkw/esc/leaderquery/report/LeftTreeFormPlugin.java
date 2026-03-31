package tdkw.esc.leaderquery.report;


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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestFormPlugin
 *
 * @author xxx
 * @date 2023/6/15
 */
public class LeftTreeFormPlugin extends AbstractReportFormPlugin implements TreeNodeClickListener, SearchListener {
    private static final Log logger = LogFactory.getLog(LeftTreeFormPlugin.class);

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        this.buildTreeNode();
        getView().refresh();
        ReportFilter rf = this.getView().getControl("reportfilterap");
        rf.setCollapse(false);

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


    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        super.beforeQuery(queryParam);
        //集团司龄开始
        FilterItemInfo workStart = queryParam.getFilter().getFilterItem("tdkw_workage");
        //集团司龄结束
        FilterItemInfo workEnd = queryParam.getFilter().getFilterItem("tdkw_workagen");
        // 年龄开始
        FilterItemInfo ageStart = queryParam.getFilter().getFilterItem("tdkw_agestart");
        //年龄结束
        FilterItemInfo ageEnd = queryParam.getFilter().getFilterItem("tdkw_ageend");
        //对输入值进行合法性校验
        if (null != workStart && null != workEnd) {
            if ((workEnd.getValue() instanceof Integer && workStart.getValue() instanceof Integer)) {
                boolean flag = (Integer) workEnd.getValue() >= (Integer) workStart.getValue();
                if (!flag || (Integer) workEnd.getValue() > 120 || (Integer) workStart.getValue() < 0 || (Integer) workStart.getValue() > 120 || (Integer) workEnd.getValue() < 0) {
                    logger.info("集团司龄输入数据有误");
                    throw new KDBizException("集团司龄输入数据有误,请检查!");
                }
            } else {
                logger.info("集团司龄必须为整数");
                throw new KDBizException("集团司龄必须为整数!");
            }
        }
        if (null != ageStart && null != ageEnd) {
            if (ageEnd.getValue() instanceof Integer && ageStart.getValue() instanceof Integer) {
                boolean flag = (Integer) ageEnd.getValue() >= (Integer) ageStart.getValue();
                if (!flag || (Integer) ageEnd.getValue() > 120 || (Integer) ageStart.getValue() < 0 || (Integer) ageStart.getValue() > 120 || (Integer) ageEnd.getValue() < 0) {
                    logger.info("年龄输入数据有误");
                    throw new KDBizException("年龄输入数据有误,请检查!");
                }
            } else {
                logger.info("年龄必须为整数");
                throw new KDBizException("年龄必须为整数!");
            }
        }
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
