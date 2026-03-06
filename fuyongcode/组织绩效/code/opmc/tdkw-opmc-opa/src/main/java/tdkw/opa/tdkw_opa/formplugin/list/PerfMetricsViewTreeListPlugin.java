package tdkw.opa.tdkw_opa.formplugin.list;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.ITreeModel;
import kd.bos.entity.tree.TreeNode;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.control.TreeView;
import kd.bos.form.control.events.SearchEnterEvent;
import kd.bos.form.control.events.TreeNodeEvent;
import kd.bos.list.ListShowParameter;
import kd.bos.list.events.BuildTreeListFilterEvent;
import kd.bos.mvc.list.ListView;
import kd.bos.org.model.OrgTreeParam;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.hr.hbp.business.dao.factory.HRBaseDaoFactory;
import kd.hr.hbp.business.service.diff.HRPluginProxy;
import kd.hr.hbp.business.servicehelper.AppIdServiceHelper;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.business.servicehelper.QueryEntityServiceHelper;
import kd.hr.hbp.business.servicehelper.org.util.OrgTreeUtils;
import kd.hr.hbp.common.constants.org.OrgTreeDynEnum;
import kd.hr.hbp.common.constants.org.OrgTreeSearchParam;
import kd.hr.hbp.common.constants.org.TreeTemplateConstants;
import kd.hr.hbp.common.model.AuthorizedOrgResult;
import kd.hr.hbp.common.util.HRDateTimeUtils;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hbp.common.util.org.model.OrgTreeModel;
import kd.hr.hbp.formplugin.web.org.template.AdminOrgTreeListTemplate;
import kd.hr.hbp.formplugin.web.template.IHRF7AdminOrgTreeListPlugin;
import kd.hr.hbp.formplugin.web.util.perm.HRPermUtil;
import kd.sdk.hr.hrmp.hrpi.extpoint.F7TreeExtParam;
import kd.sdk.hr.hrmp.hrpi.extpoint.IPersonF7TreeListExt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @author 余梦圆
 * @description 左树形控件
 * @date 2024/08/17
 */
public class PerfMetricsViewTreeListPlugin extends AdminOrgTreeListTemplate implements IHRF7AdminOrgTreeListPlugin {

	private HRPluginProxy pluginProxy;

	public PerfMetricsViewTreeListPlugin() {
		super(new OrgTreeModel(OrgTreeDynEnum.ADMIN_STRUCT.getDynEntity(), OrgTreeDynEnum.ADMIN_MAIN_ENTITY.getDynEntity(), Boolean.FALSE, Boolean.TRUE, Boolean.TRUE));
	}

	public ITreeModel getTreeModel() {
		return super.getTreeModel();
	}

	public IDataModel getModel() {
		return super.getModel();
	}


	public QFilter buildNodeClickFilter(BuildTreeListFilterEvent ent) {
		String focusNodeId = ent.getNodeId().toString();
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
				String focusNodeLongNumber = (String)focusNodeDy.get("structlongnumber");
				filter = new QFilter("structlongnumber", "like", focusNodeLongNumber + "%");
			}

			DynamicObjectCollection dyColl = HRBaseDaoFactory.getInstance(this.getEntityName()).queryColl("adminorg.id adminorg", new QFilter[]{filter, cFilter, iFilter, dFilter}, null);
			Set<Long> ids = new HashSet<>(dyColl.size());
			ids.add(Long.valueOf(focusNodeId));
			int idx = 0;

			for(int size = dyColl.size(); idx < size; ++idx) {
				ids.add(dyColl.get(idx).getLong("adminorg"));
			}

			nodeClicFilter = new QFilter("tdkw_adminorg.id", "in", ids);

			if (!CollectionUtils.isEmpty(this.getPluginProxy().getPlugins())) {
				allOrgBoIdList = new ArrayList<>(ids);
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
				OrgTreeSearchParam searchParam = new OrgTreeSearchParam(evt.getText(), (ListView)this.getView());
				searchParam.getOrgRangeList().addAll(orgTreeParam.getOrgRangeList());
				searchParam.setClickNode(true);
			}
		}
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
				qFilter = new QFilter("tdkw_adminorg.id", "in", orgSet[0]);
			}

			return qFilter;
		}
	}

	@Override
	protected String getListPermProKey() {
		return "tdkw_adminorg";
	}


	public void expendTreeNode(TreeNodeEvent e) {
		String parentPageId = this.getView().getFormShowParameter().getParentPageId();
		IFormView parentview = this.getView().getViewNoPlugin(parentPageId);
		String openFlag = this.getPageCache().get("page_open_flag");
		if (!HRStringUtils.equals("1", openFlag) && parentview != null && parentview.getFormShowParameter().getCustomParam("adminorg") != null) {
			this.getView().getPageCache().put("page_open_flag", "1");
			Long curNodeId = Long.parseLong(parentview.getFormShowParameter().getCustomParam("adminorg"));
			TreeNode treeNode = this.getSearchNodesBySearchId(curNodeId).get(0);
			OrgTreeSearchParam searchParam = new OrgTreeSearchParam(treeNode.getText(), (ListView)this.getView());
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

					for(int i = expendIds.size() - 1; i >= 0; --i) {
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


	protected AuthorizedOrgResult getPermOrgResult() {
		Long userId = RequestContext.get().getCurrUserId();
		String orgResultString = this.getPageCache().get("perm_org_result");
		if (!HRStringUtils.isEmpty(orgResultString)) {
			return (AuthorizedOrgResult) SerializationUtils.fromJsonString(orgResultString, AuthorizedOrgResult.class);
		} else {
			ListShowParameter listParameter = (ListShowParameter) this.getView().getFormShowParameter();
			String permEntityId = listParameter.getBillFormId();
			String permItemId = "47150e89000000ac";
			boolean isF7 = listParameter.isLookUp();
			if (isF7) {
				permEntityId = HRPermUtil.getF7ParentEntityId(this.getView());
				permItemId = HRPermUtil.getF7ParentPermItemId(this.getView());
			}

			permEntityId = QueryEntityServiceHelper.getDataEntityNumber(permEntityId);
			String appId = this.getAppIdWithDealThirdApp(listParameter, permEntityId);
			if (this.isCheckIgnoreConfig()) {
				boolean isIgnoreDataRule = (Boolean) HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSBizDataPermissionService", "isIgnoreEntityDataRule", new Object[]{appId, permEntityId});
				if (isIgnoreDataRule) {
					AuthorizedOrgResult permResult = AuthorizedOrgResult.allOrg();
					this.getPageCache().put("perm_org_result", SerializationUtils.toJsonString(permResult));
					return permResult;
				}
			}

			String cachedSearchDateStr = this.getPageCache().get("searchdate");
			Date searchDate = HRDateTimeUtils.truncateDate(new Date());
			if (HRStringUtils.isNotEmpty(cachedSearchDateStr)) {
				searchDate = (Date) JSON.parseObject(cachedSearchDateStr, Date.class);
			}

			AuthorizedOrgResult authorizedOrgResult;
			if (isF7) {
				authorizedOrgResult = (AuthorizedOrgResult) HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSBizDataPermissionService", "getUserAdminOrgsF7", new Object[]{userId, appId, permEntityId, permItemId, this.getParentF7Prop(listParameter), this.getPermParam()});
			} else {
				authorizedOrgResult = (AuthorizedOrgResult) HRMServiceHelper.invokeHRMPService("hrcs", "IHRCSBizDataPermissionService", "getUserAdminOrgs", new Object[]{userId, appId, permEntityId, permItemId, this.getListPermProKey(), this.getPermParam()});
			}

			if (!authorizedOrgResult.isHasAllOrgPerm() && !CollectionUtils.isEmpty(authorizedOrgResult.getHasPermOrgs())) {
				HRBaseServiceHelper orgMasterHelper = new HRBaseServiceHelper("haos_adminorgdetail");
				DynamicObjectCollection dynamicObjects = orgMasterHelper.queryOriginalCollection("id,boid", new QFilter[]{new QFilter("id", "in", authorizedOrgResult.getHasPermOrgs()), new QFilter("datastatus", "!=", "-2")});
				List<Long> orgBoIdS = new ArrayList(dynamicObjects.size());
				dynamicObjects.stream().forEach((dynamicObject) -> {
					orgBoIdS.add(dynamicObject.getLong("boid"));
				});
				authorizedOrgResult.getHasPermOrgs().clear();
				authorizedOrgResult.getHasPermOrgs().addAll(orgBoIdS);
			}

			this.getPageCache().put("perm_org_result", SerializationUtils.toJsonString(authorizedOrgResult));
			return authorizedOrgResult;
		}
	}

	private String getParentF7Prop(ListShowParameter listParameter) {
		return this.getParentF7PropWrap(listParameter);
	}


	private String getAppIdWithDealThirdApp(FormShowParameter showParameter, String entityNumber) {
		String entryAppId = HRPermUtil.getAppIdFromShowParam(showParameter);
		return AppIdServiceHelper.getPermAppId(entryAppId, entityNumber);
	}

	private Map<String, Object> getPermParam() {
		Map<String, Object> map = new HashMap();
		List<Object> structProjectIdList = new ArrayList();
		structProjectIdList.add(this.getStructProjectCapable().getStructProject().getLong("id"));
		map.put("hr_dataperm_structprojectid", structProjectIdList);
		map.put("hr_dataperm_bsedtime", this.getDateParam());
		map.put("hr_dataperm_bsledtime", this.getDateParam());
		return map;
	}
}
