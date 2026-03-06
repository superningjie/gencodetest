# expendTreeNode事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238609327538441216&id=225258706055955456&type=Knowledge&productLineId=29&lang=zh-CN

## expendTreeNode事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:17

浏览数： 1,408 

1 事件介绍

插件可以在此方法，构建子节点返回；

  


2 事件触发时机

用户点击树节点前的"+"标识时，触发此事件，向系统请求懒加载子节点。

  


 _特别说明：_

  1.  _节点需要setChildren(new ArrayList <TreeNode>())，相当于打上了懒加载标志，才会在用户点击+时，触发此事件；_

  2.  _树形列表界面插件基类 AbstractTreeListPlugin 已经捕获此事件，并即时触发 refreshNode 事件。因此，自定义插件不需要捕获expendTreeNode事件，统一在 refreshNode 事件加载分组树节点。_




3 代码模板
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.form.control.events.TreeNodeEvent;
    import kd.bos.list.plugin.AbstractTreeListPlugin;
     
    public class ExpendTreeNode extends AbstractTreeListPlugin {
       
        @Override
        public void expendTreeNode(TreeNodeEvent e) {
             super.expendTreeNode(e);
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** TreeNodeEvent **extends** EventObject

  * **public** Object getSource()：事件源

  * **public** Object getNodeId()：当前待展开的节点标识，需要懒加载此节点的子节点

  * **public** Object getParentNodeId()：上一级节点标识

  * **public** **boolean** isPropagation()：不适用

  * **public** **void** setExpandedNode(TreeNode expandedNode)：不适用




  


5 应用示例

通常不需要捕获本事件，示例略过。

  


 __上一篇：refreshNode事件

下一篇：treeNodeClick事件

 __

暂无评分

内容反馈

*  __评论
收藏

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
