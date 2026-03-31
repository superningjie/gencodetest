package tdkw.opa.tdkw_opa.formplugin.list;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.ITreeModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.tree.TreeNode;
import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.IFormView;
import kd.bos.form.control.TreeView;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.TreeNodeEvent;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.events.BuildTreeListFilterEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mvc.list.ListView;
import kd.bos.org.model.OrgTreeParam;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.hr.hbp.business.dao.factory.HRBaseDaoFactory;
import kd.hr.hbp.business.service.diff.HRPluginProxy;
import kd.hr.hbp.business.servicehelper.org.util.OrgTreeUtils;
import kd.hr.hbp.common.constants.org.OrgTreeDynEnum;
import kd.hr.hbp.common.constants.org.OrgTreeSearchParam;
import kd.hr.hbp.common.constants.org.TreeTemplateConstants;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.common.util.org.model.OrgTreeModel;
import kd.hr.hbp.formplugin.web.org.template.AdminOrgTreeListTemplate;
import kd.hr.hbp.formplugin.web.template.IHRF7AdminOrgTreeListPlugin;
import kd.sdk.hr.hrmp.hrpi.extpoint.F7TreeExtParam;
import kd.sdk.hr.hrmp.hrpi.extpoint.IPersonF7TreeListExt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: xxx
 * @create: 2024/08/29 11:12
 * @description:
 **/
public class PerformanceMapTreeListPlugin extends AdminOrgTreeListTemplate implements IHRF7AdminOrgTreeListPlugin {
    private static final Log logger = LogFactory.getLog(PerformanceMapTreeListPlugin.class);
    private HRPluginProxy pluginProxy;

    public PerformanceMapTreeListPlugin() {
        super(new OrgTreeModel(OrgTreeDynEnum.ADMIN_STRUCT.getDynEntity(), OrgTreeDynEnum.ADMIN_MAIN_ENTITY.getDynEntity(), Boolean.FALSE, Boolean.TRUE, Boolean.TRUE));
    }

    @Override
    protected String getListPermProKey() {
        return "tdkw_adminorg";
    }

    public ITreeModel getTreeModel() {
        return super.getTreeModel();
    }

    public IDataModel getModel() {
        return super.getModel();
    }

    @Override
    public void setFilter(SetFilterEvent setFilterEvent) {
        super.setFilter(setFilterEvent);
    }


    public QFilter buildNodeClickFilter(BuildTreeListFilterEvent ent) {

        String focusNodeId = ent.getNodeId().toString();
        // 标准化处理  查询树节点获取不到当前版本的处理
        // START
        //通过id获取编码和 是否当前版本
        QFilter focusNodeIdFilter = new QFilter("id", QCP.equals, Long.valueOf(focusNodeId));
        DynamicObject focusNodeAdminOrg = QueryServiceHelper.queryOne("haos_adminorghr", "id,number,iscurrentversion", focusNodeIdFilter.toArray());
        boolean isCurVersion = focusNodeAdminOrg.getBoolean("iscurrentversion");
        if (!isCurVersion){
            //如果不是当前版本
            QFilter orgFilter = new QFilter("iscurrentversion", QCP.equals, "1");
            orgFilter.and("datastatus", QCP.equals, "1");
            orgFilter.and("status", QCP.equals, "C");
            orgFilter.and("number",QCP.equals,focusNodeAdminOrg.getString("number"));
            DynamicObject org = QueryServiceHelper.queryOne("haos_adminorghr", "id,number,iscurrentversion", orgFilter.toArray());
            if (org!=null){
                focusNodeId = org.getString("id");
            }
        }
        // END

        this.getView().getPageCache().put("orgId", focusNodeId);
        this.getModel().setValue("tdkw_orgid", focusNodeId);
        List<Long> allOrgBoIdList = null;
        QFilter nodeClicFilter = null;
        if (!this.getTreeModel().getRoot().getId().equals(focusNodeId)) {
            QFilter cFilter = new QFilter("iscurrentversion", "=", "1");
            QFilter iFilter = new QFilter("initstatus", "=", "2");
            List<String> dataStatusList = new ArrayList<>();
            dataStatusList.add("1");
            dataStatusList.add("2");
            QFilter dFilter = new QFilter("datastatus", "in", dataStatusList);
            QFilter focusNodeFilter = new QFilter("adminorg.id", "=", Long.valueOf(focusNodeId));
            DynamicObject focusNodeDy = HRBaseDaoFactory.getInstance(this.getEntityName()).queryOne("structlongnumber", new QFilter[]{focusNodeFilter, cFilter, iFilter, dFilter});
            QFilter filter = null;
            if (focusNodeDy != null){
                String focusNodeLongNumber = (String) focusNodeDy.get("structlongnumber");
                filter = new QFilter("structlongnumber", "like", focusNodeLongNumber + "%");

            }
            DynamicObjectCollection dyColl = HRBaseDaoFactory.getInstance(this.getEntityName()).queryColl("adminorg.id adminorg", new QFilter[]{filter, cFilter, iFilter, dFilter}, null);
            Set<Long> ids = new HashSet<>(dyColl.size());
            ids.add(Long.valueOf(focusNodeId));
            int idx = 0;

            for (int size = dyColl.size(); idx < size; ++idx) {
                ids.add(dyColl.get(idx).getLong("adminorg"));
            }

            switch (this.getView().getEntityId()) {
                case "hrpi_depemptreelistf7":
                case "hrpi_deppromembertreef7":
                    nodeClicFilter = new QFilter("adminorg.id", "in", ids);
                    break;
                case "hrpi_employeetreelistf7":
                case "hrpi_persontreelistf7":
                case "hrpi_employeelisttreef7":
                case "hrpi_employeequittreef7":
                    nodeClicFilter = new QFilter("hrpi_empposorgrel.adminorg.id", "in", ids);
                    break;
                case "tdkw_org_view":
                    nodeClicFilter = new QFilter("tdkw_adminorg.id", "in", ids);
                    break;
                default:
                    break;
            }

            if (!CollectionUtils.isEmpty(this.getPluginProxy().getPlugins())) {
                allOrgBoIdList = new ArrayList<>(ids);
            }
        }else {
            //通过id获取编码和 是否当前版本
            QFilter idFilter = new QFilter("id", QCP.equals, Long.valueOf(focusNodeId));
            DynamicObject adminOrg = QueryServiceHelper.queryOne("haos_adminorghr", "id,number,iscurrentversion", idFilter.toArray());
            boolean isCurrentVersion = adminOrg.getBoolean("iscurrentversion");
            if (!isCurrentVersion){
                //如果不是当前版本
                QFilter orgFilter = new QFilter("iscurrentversion", QCP.equals, "1");
                orgFilter.and("datastatus", QCP.equals, "1");
                orgFilter.and("status", QCP.equals, "C");
                orgFilter.and("number",QCP.equals,adminOrg.getString("number"));
                DynamicObject org = QueryServiceHelper.queryOne("haos_adminorghr", "id,number,iscurrentversion", orgFilter.toArray());
                if (org!=null){
                    String id = org.getString("id");
                    this.getView().getPageCache().put("orgId", id);
                    this.getModel().setValue("tdkw_orgid", id);
                }

            }

        }

        return !CollectionUtils.isEmpty(this.getPluginProxy().getPlugins()) ? this.getExtQFilter(allOrgBoIdList) : nodeClicFilter;
    }

    @Override
    public void search(SearchEnterEvent evt) {
        super.search(evt);
        if (!StringUtils.isBlank(evt.getText())) {
            OrgTreeParam orgTreeParam = this.getOrgTreeParam(0L);
            if (!CollectionUtils.isEmpty(orgTreeParam.getOrgRangeList())) {
                OrgTreeSearchParam searchParam = new OrgTreeSearchParam(evt.getText(), (ListView) this.getView());
                searchParam.getOrgRangeList().addAll(orgTreeParam.getOrgRangeList());
                searchParam.setClickNode(true);
            }
        }
    }

    private String getOrgProperty() {
        String entityId = this.getView().getEntityId();
        String orgProperty = null;
        switch (entityId) {
            case "hrpi_depemptreelistf7":
            case "hrpi_deppromembertreef7":
                orgProperty = "adminorg.id";
                break;
            case "hrpi_employeetreelistf7":
            case "hrpi_persontreelistf7":
            case "hrpi_employeelisttreef7":
            case "hrpi_employeequittreef7":
                orgProperty = "hrpi_empposorgrel.adminorg.id";
                break;
            case "tdkw_org_view":
                orgProperty = "tdkw_adminorg.id";
                break;
        }

        return orgProperty;
    }

    private HRPluginProxy<IPersonF7TreeListExt> getPluginProxy() {
        if (this.pluginProxy == null) {
            this.pluginProxy = new HRPluginProxy(null, IPersonF7TreeListExt.class, "kd.sdk.hr.hrmp.hrpi.extpoint.IPersonF7TreeListExt", null);
        }

        return this.pluginProxy;
    }

    private QFilter getExtQFilter(List<Long> allOrgBoIdList) {
        Object currentNodId = this.getTreeModel().getCurrentNodeId();
        QFilter qFilter = null;
        if (currentNodId == null) {
            return null;
        } else {
            Long nodeId = Long.valueOf(currentNodId.toString());
            Set[] orgSet = new Set[1];
            F7TreeExtParam param = new F7TreeExtParam(nodeId, allOrgBoIdList);
            this.getPluginProxy().callReplace((plugin) -> {
                LOGGER.info(String.format("proxy plugin: %s", plugin.getClass()));
                orgSet[0] = plugin.getOrgSetByCurrentNode(param);
                return null;
            });
            if (!CollectionUtils.isEmpty(orgSet[0])) {
                String orgProperty = this.getOrgProperty();
                if (orgProperty != null) {
                    qFilter = new QFilter(orgProperty, "in", orgSet[0]);
                }
            }

            return qFilter;
        }
    }

    public void expendTreeNode(TreeNodeEvent e) {
        String parentPageId = this.getView().getFormShowParameter().getParentPageId();
        IFormView parentview = this.getView().getViewNoPlugin(parentPageId);
        String openFlag = this.getPageCache().get("page_open_flag");
        if (!HRStringUtils.equals("1", openFlag) && parentview != null && parentview.getFormShowParameter().getCustomParam("adminorg") != null) {
            this.getView().getPageCache().put("page_open_flag", "1");
            Long curNodeId = Long.parseLong(parentview.getFormShowParameter().getCustomParam("adminorg"));
            TreeNode treeNode = this.getSearchNodesBySearchId(curNodeId).get(0);
            OrgTreeSearchParam searchParam = new OrgTreeSearchParam(treeNode.getText(), (ListView) this.getView());
            List<TreeNode> treeNodes = new ArrayList<>();
            treeNodes.add(treeNode);
            this.searchFromLazyOrgTree(searchParam, treeNodes);
            TreeView treeView = this.getTreeListView().getTreeView();
            treeView.treeNodeClick(treeNode.getParentid(), treeNode.getId());
            treeView.uncheckNodes(treeView.getTreeState().getSelectedNodeId());
            treeView.focusNode(treeNode);
        }

    }

    private void searchFromLazyOrgTree(OrgTreeSearchParam searchParam, List<TreeNode> treeNodes) {
        this.searchExistsNodeFromLazyOrgTree(searchParam, treeNodes, 0);
    }

    private TreeNode searchExistsNodeFromLazyOrgTree(OrgTreeSearchParam searchParam, List<TreeNode> treeNodes, int recursionCount) {
        if (!CollectionUtils.isEmpty(treeNodes) && recursionCount <= 20) {
            TreeNode node = treeNodes.get(0);
            if (node == null) {
                return null;
            } else {
                String parentId = node.getParentid();
                TreeNode parentNode = OrgTreeUtils.getNode(searchParam.getRootNode(), parentId);
                if (OrgTreeUtils.isChildNode(parentNode, node)) {
                    Set<String> loopController = Sets.newHashSetWithExpectedSize(16);
                    List<String> expendIds = Lists.newArrayListWithCapacity(16);
                    OrgTreeUtils.expandParentNode(searchParam, parentId, loopController, expendIds, 0);

                    for (int i = expendIds.size() - 1; i >= 0; --i) {
                        searchParam.getTreeView().expand(expendIds.get(i));
                    }
                } else {
                    this.queryTreeNodeWithParent(searchParam, node);
                }

                node = OrgTreeUtils.getNode(searchParam.getRootNode(), node.getId());
                if (node == null) {
                    TreeNode removeNode = treeNodes.remove(0);
                    String subNodeLongNumber = removeNode.getLongNumber() + "!";
                    List<TreeNode> removeSubNodes = Lists.newArrayList();

                    for (TreeNode subNode : treeNodes) {
                        if (subNode.getLongNumber() != null && subNode.getLongNumber().startsWith(subNodeLongNumber)) {
                            removeSubNodes.add(subNode);
                        }
                    }

                    treeNodes.removeAll(removeSubNodes);
                    return this.searchExistsNodeFromLazyOrgTree(searchParam, treeNodes, recursionCount);
                } else {
                    return node;
                }
            }
        } else {
            return null;
        }
    }

    private void queryTreeNodeWithParent(OrgTreeSearchParam searchParam, TreeNode node) {
        Object currentNodeId = this.getTreeModel().getCurrentNodeId();
        TreeNode curTreeNode = OrgTreeUtils.getNode(this.getTreeModel().getRoot(), currentNodeId.toString());
        String structLongNumber = node.getLongNumber();
        int nextLongNumberLength = curTreeNode.getLongNumber().length() + TreeTemplateConstants.LONG_NUMBER_AND_SPLIT_LENGTH;
        if (structLongNumber.length() >= nextLongNumberLength) {
            String newParentStructLongNumber = structLongNumber.substring(0, nextLongNumberLength);
            String notLoadParentLongNumber = newParentStructLongNumber.substring(newParentStructLongNumber.lastIndexOf(33) + 1);
            int layerCount = (structLongNumber.length() - newParentStructLongNumber.length()) / TreeTemplateConstants.LONG_NUMBER_AND_SPLIT_LENGTH;
            List<String> structNumbers = new ArrayList<>();
            structNumbers.add(notLoadParentLongNumber);
            this.getView().getFormShowParameter().setCustomParam("layerCount", layerCount);
            this.queryTreeNodeChildrenByStructNumbers(searchParam, structNumbers, node);
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs changedArgs) {
        super.propertyChanged(changedArgs);

    }

    @Override
    public void customEvent(CustomEventArgs e) {
        super.customEvent(e);
        CustomControl control = this.getControl("tdkw_customcontrolap");
        String eventArgs = e.getEventArgs();
        String eventName = e.getEventName();

    }
}
