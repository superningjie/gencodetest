package dgdl.odc.homs.listPlugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.tree.TreeNode;
import kd.bos.form.IFormView;
import kd.bos.form.control.TreeView;
import kd.bos.form.control.events.TreeNodeCheckEvent;
import kd.bos.form.control.events.TreeNodeCheckListener;
import kd.bos.form.control.events.TreeNodeEvent;
import kd.bos.list.events.BuildTreeListFilterEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.common.constants.org.TreeTemplateConstants;
import kd.hrmp.hbpm.formplugin.web.position.PositionOrgTreeList;

import java.util.*;

/**
 * @author 陈路
 * @date:2023/1/22
 * @description: 年度编制管理树形表单插件
 */
public class PlanyearTreeListPlugin extends PositionOrgTreeList implements TreeNodeCheckListener {
    public PlanyearTreeListPlugin() {
        super();
    }

    @Override
    protected boolean isShowDisableAndSearchDate() {
        return true;
    }

    @Override
    public void initializeTree(EventObject e) {
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        if(customParams.containsKey("orgId")){
            long orgId = (Long)customParams.get("orgId");
            this.hisSubWhereCommonSql.append(" and forgid="+orgId);
        }
        super.initializeTree(e);
    }

    @Override
    protected QFilter buildNodeClickFilter(BuildTreeListFilterEvent buildTreeListFilterEvent) {
        if(this.isInCludeChild()){
            QFilter focusNodeFilter = new QFilter("adminorg.id", "in", this.getAllOrgBoIdList());
            return focusNodeFilter;
        }else{
            //选中左树节点时过滤出自己组织和下级的数据
            Object currentNodeId = this.getTreeModel().getCurrentNodeId();
            TreeNode treeNode = this.getTreeModel().getRoot().getTreeNode(currentNodeId.toString());
            List<Long> ids = new ArrayList<>();
            Long parentId = Long.valueOf(currentNodeId.toString());
            ids.add(parentId);
            List<TreeNode> children = treeNode.getChildren();
            if(children != null && !children.isEmpty()){
                for (TreeNode child : children) {
                    ids.add(Long.valueOf(child.getId()));
                }
            }else {
                //获取业务单元
                DynamicObject bosOrg = BusinessDataServiceHelper.loadSingle(parentId, "bos_org");
                ids.add(bosOrg.getLong("id"));
            }
            QFilter focusNodeFilter = new QFilter("adminorg.id", "in", ids);
            return focusNodeFilter;
        }
    }

    @Override
    public void treeNodeCheck(TreeNodeCheckEvent e) {

    }

    /**
     * 将当前点击的树节点传出给父页面
     * @param e
     */
    @Override
    public void treeNodeClick(TreeNodeEvent e) {
        super.treeNodeClick(e);
        //获取行政组织节点
        String nodeId = (String) e.getNodeId();
        logger.info("PlanyearTreeListPlugin组织信息=" + nodeId);
        IFormView parentView = this.getView().getParentView();
        parentView.getPageCache().put("admingId",nodeId);
        //控制父页面同步在岗人员按钮锁定
        IDataModel model = parentView.getModel();
        DynamicObject planyear = (DynamicObject)model.getValue("dgdl_planyear");
        QFilter detailQFilter = new QFilter("dgdl_planyear.number", QCP.equals, planyear.getString("number"))
                .and("adminorg.id",QCP.equals,Long.valueOf(nodeId));
        DynamicObject thisDetail = BusinessDataServiceHelper.loadSingle("dgdl_planyear_detail", "id", detailQFilter.toArray());
        logger.info("PlanyearTreeListPlugin获取自身详情信息SQL=" + thisDetail);
        if (StringUtils.isEmpty(nodeId) || "100000".equals(nodeId) || Objects.nonNull(thisDetail)){
            parentView.setEnable(false,"dgdl_syn_person");
        }else {
            parentView.setEnable(true,"dgdl_syn_person");
        }
        this.getView().sendFormAction(parentView);
    }
}
