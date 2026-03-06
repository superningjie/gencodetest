package tdkw.hrmp.hrobs.common.app.hrmp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.utils.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 组织树实体类
 * printHROrgTree：打印组织树的组织名称到控制台
 * toString：获取组织树实体的JSONObject格式字符串
 * convertHROrgTreeToJson：将组织树转为JSONObject
 * convertJsonToHROrgTree：将组织树的JSONObject串转为组织树实体
 * addChild：为当前组织添加下级组织
 * @author xxx
 */
public class HROrgTree {

    //组织id
    private String id;
    //组织名称
    private String name;
    //组织编码
    private String number;
    //组织长编码
    private String longNumber;
    //组织类型
    private String type;
    //上级组织ID, 也就是节点ID
    private String parentId;
    //组织层级
    private String level;
    //排序号
    private int index;
    //用友ID
    private String ncId;
    //下级组织
    private List<HROrgTree> children;

    public HROrgTree(String id, String name, String number, String longNumber, String type, String parentId) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.longNumber = longNumber;
        this.type = type;
        this.parentId = parentId;
        this.level = "";
        this.index = 0;
    }

    public HROrgTree(String id, String name, String number, String longNumber, String type, String parentId, String level) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.longNumber = longNumber;
        this.type = type;
        this.parentId = parentId;
        this.level = level;
        this.index = 0;
    }

    public HROrgTree(String id, String name, String number, String longNumber, String type, String parentId, String level, Integer index) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.longNumber = longNumber;
        this.type = type;
        this.parentId = parentId;
        this.level = level;
        if (index == null || index < 0) {
            this.index = 0;
        } else {
            this.index = index;
        }
    }

    public HROrgTree(String id, String name, String number, String longNumber, String type, String parentId, String level, Integer index, String ncId) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.longNumber = longNumber;
        this.type = type;
        this.parentId = parentId;
        this.level = level;
        if (index == null || index < 0) {
            this.index = 0;
        } else {
            this.index = index;
        }
        this.ncId = ncId;
    }

    /**
     * 打印组织树
     * @param node 组织树实体
     * @param prefix 前缀字符
     * - 组织A
     *   -- 组织B
     *     --- 组织C
     *   -- 组织D
     * - 组织E
     */
    public static void printHROrgTree(HROrgTree node, String prefix) {
        if (node != null) {
//            System.out.println(prefix + node.getName());

            List<HROrgTree> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                for (HROrgTree child : children) {
                    printHROrgTree(child, prefix + "-");
                }
            }
        }
    }

    /**
     * 获取该组织树中所有类型为 公司 的组织
     * @param node 组织树
     * @param trees 列表
     */
    public static void getCompanyOrg(HROrgTree node, List<HROrgTree> trees) {
        if (node != null) {
            List<HROrgTree> children = node.getChildren();
            if (children != null) {
                for (HROrgTree tree : children) {
                    String type = tree.getType();
                    if (StringUtils.equals("XY00005", type)) {
                        List<HROrgTree> children1 = tree.getChildren();
                        boolean is = true;
                        if (children1 != null) {
                            for (HROrgTree orgTree : children1) {
                                String type1 = orgTree.getType();
                                if (StringUtils.equals("XY00005", type1)) {
                                    trees.add(orgTree);
                                    is = false;
                                }
                            }
                        }
                        if (is) {
                            trees.add(tree);
                        }
                    }
                    getCompanyOrg(tree, trees);
                }
            }
        }
    }

    /**
     * 查询组织树中的某个组织
     * @param node 组织树
     * @param id 组织ID，不是节点ID
     * @return  组织树
     */
    public static HROrgTree getHROrgTree(HROrgTree node, String id) {
        HROrgTree orgTree = null;
        if (node != null) {
            String nodeId = node.getId();
            if (StringUtils.equals(id, nodeId)) {
                orgTree = node;
            } else {
                List<HROrgTree> treeList = node.getChildren();
                if (treeList == null || treeList.isEmpty()) {
                    return null;
                } else {
                    for (HROrgTree tree : treeList) {
                        HROrgTree hrOrgTree = getHROrgTree(tree, id);
                        if (hrOrgTree != null) {
                            return hrOrgTree;
                        }
                    }
                }
            }
        }
        return orgTree;
    }

    /**
     * 查询组织树该节点下的所属组织信息
     * @param node 组织树
     * @param pid 节点ID
     * @return 组织树list
     */
    public static List<HROrgTree> getHROrgTreeSubordinate(HROrgTree node, String pid) {
        List<HROrgTree> list = new ArrayList<>();
        List<HROrgTree> treeList = node.getChildren();
        String nodeParentId = node.getParentId();
        if (StringUtils.equals(nodeParentId, pid)) {
            return treeList;
        }
        if (treeList == null || treeList.isEmpty()) {
            return list;
        } else {
            for (HROrgTree orgTree : treeList) {
                List<HROrgTree> trees = getHROrgTreeSubordinate(orgTree, pid);
                if (!trees.isEmpty()) {
                    return trees;
                }
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return convertHROrgTreeToJson(this).toJSONString();
    }

    /**
     * 将组织树实体转为JSON
     * @param node 组织树
     * @return 组织树的JSON
     */
    public static JSONObject convertHROrgTreeToJson(HROrgTree node) {
        JSONObject jsonNode = new JSONObject();
        jsonNode.put("id", node.getId());
        jsonNode.put("name", node.getName());
        jsonNode.put("type", node.getType());
        jsonNode.put("code", node.getNumber());
        jsonNode.put("longNumber", node.getLongNumber());
        jsonNode.put("level", node.getLevel());
        jsonNode.put("pid", node.getParentId());
        jsonNode.put("index", node.getIndex());
        jsonNode.put("ncId", node.getNcId());

        List<HROrgTree> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            JSONArray jsonChildren = new JSONArray();
            for (HROrgTree child : children) {
                jsonChildren.add(convertHROrgTreeToJson(child));
            }
            jsonNode.put("children", jsonChildren);
        }

        return jsonNode;
    }

    /**
     * 将组织树转换的JSONObject重新转为组织树实体
     * @param jsonNode 组织树的JSON
     * @return 组织树实体
     */
    public static HROrgTree convertJsonToHROrgTree(JSONObject jsonNode) {
        String id = jsonNode.getString("id");
        String name = jsonNode.getString("name");
        String code = jsonNode.getString("code");
        String longNumber = jsonNode.getString("longNumber");
        String type = jsonNode.getString("type");
        String level = jsonNode.getString("level");
        String pid = jsonNode.getString("pid");
        Integer index = jsonNode.getInteger("index");
        String ncId = jsonNode.getString("ncId");

        JSONArray jsonChildren = jsonNode.getJSONArray("children");
        List<HROrgTree> children = new ArrayList<>();
        if (jsonChildren != null) {
            for (int i = 0; i < jsonChildren.size(); i++) {
                JSONObject jsonChild = jsonChildren.getJSONObject(i);
                children.add(convertJsonToHROrgTree(jsonChild));
            }
        }
        HROrgTree orgTree = new HROrgTree(id, name, code, longNumber, type, pid, level, index, ncId);
        orgTree.setChildren(children);

        return orgTree;
    }

    /**
     * 当有多个同层级的组织时先按照index排序，再按照number排序
     * @param child 组织实体
     */
    public void addChild(HROrgTree child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        if (children.isEmpty()) {
            children.add(child);
        } else {
            children.add(child);
            Comparator<HROrgTree> comparator = Comparator.comparing(HROrgTree::getIndex).thenComparing(HROrgTree::getNumber);
            children.sort(comparator);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLongNumber() {
        return longNumber;
    }

    public void setLongNumber(String longNumber) {
        this.longNumber = longNumber;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getNcId() {
        return ncId;
    }

    public void setNcId(String ncId) {
        this.ncId = ncId;
    }

    public List<HROrgTree> getChildren() {
        return children == null ? new ArrayList<>() : children;
    }

    public void setChildren(List<HROrgTree> children) {
        this.children = children;
    }
}
