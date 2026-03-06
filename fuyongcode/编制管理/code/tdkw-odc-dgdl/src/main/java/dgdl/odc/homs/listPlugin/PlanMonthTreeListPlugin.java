package dgdl.odc.homs.listPlugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.tree.TreeNode;
import kd.bos.form.IFormView;
import kd.bos.form.control.events.TreeNodeEvent;
import kd.bos.form.events.BeforeCreateListDataProviderArgs;
import kd.bos.list.BillList;
import kd.bos.list.events.BuildTreeListFilterEvent;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hrmp.hbpm.formplugin.web.position.PositionOrgTreeList;

import java.util.*;


/**
 * @author 姚帅
 * @date:2023/1/22
 * @description: 月度编制树列表插件
 */
public class PlanMonthTreeListPlugin extends PositionOrgTreeList {


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


    /**
     * 将当前点击的树节点传出给父页面
     *
     * @param e
     */
    @Override
    public void treeNodeClick(TreeNodeEvent e) {
        super.treeNodeClick(e);
        //获取行政组织节点
        String nodeId = (String) e.getNodeId();
        this.getView().getPageCache().put("adminId", nodeId);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs changedArgs) {
        super.propertyChanged(changedArgs);
        String name = changedArgs.getProperty().getName();
        if("chkincludechild".equals(name)){
            this.getView().invokeOperation("refresh");
        }

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
    public void beforeCreateListDataProvider(BeforeCreateListDataProviderArgs args) {
        super.beforeCreateListDataProvider(args);
        String adminId = this.getView().getPageCache().get("adminId");
        IFormView view = this.getView();
        if (StringUtils.isEmpty(adminId) || "100000".equals(adminId)) {
            view.setEnable(false, "dgdl_new");
        } else {
            DynamicObject adminorghr = BusinessDataServiceHelper.loadSingle(Long.valueOf(adminId), "haos_adminorghr");
            if (Objects.nonNull(adminorghr)){
                String layer = adminorghr.getDynamicObject("adminorglayer").getString("number");
                logger.info("PlanMonthTreeListPlugin编码=" + layer);
                if ("99".equals(layer)){
                    view.setEnable(false, "dgdl_new");
                }else {
                    view.setEnable(true, "dgdl_new");
                }
            }
        }
        logger.info("PlanMonthTreeListPlugin传给父页面的节点id=" + adminId);
        ((BillList) args.getSource()).getView().getParentView().getPageCache().put("adminId", adminId);
    }
}
