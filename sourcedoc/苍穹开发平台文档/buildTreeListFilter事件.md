# buildTreeListFilter事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238609327538441216&id=225260761298429440&type=Knowledge&productLineId=29&lang=zh-CN

## buildTreeListFilter事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:18

浏览数： 2,845 

1 事件介绍

插件可以在此插件中，自行生成分组过滤条件，替换掉系统内部生成的分组条件。

  


2 事件触发时机

基于当前选择的分组节点，生成单据列表过滤条件时触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.list.events.BuildTreeListFilterEvent;
    import kd.bos.list.plugin.AbstractTreeListPlugin;
     
    public class BuildTreeListFilter extends AbstractTreeListPlugin {
       
        @Override
        public void buildTreeListFilter(BuildTreeListFilterEvent e) {
             super.buildTreeListFilter(e);
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** BuildTreeListFilterEvent **extends** EventObject

  * **public** Object getSource()：事件源；

  * **public** Object getNodeId()：当前点击的节点标识，需据此过滤列表数据；

  * **public** **void** setCancel(**boolean** cancel)：取消系统内置的列表分组过滤条件；

  * **public** **void** addQFilter(QFilter filter)：添加自定义的列表过滤条件。




  


5 应用示例

参见[refreshNode](https://vip.kingdee.com/article/225256652558630400)事件示例：

插件自行构建分组树节点，并根据当前点击的节点，生成列表过滤条件。

  


 __上一篇：treeNodeClick事件

下一篇：search事件

 __

暂无评分

内容反馈

*  __评论
收藏 2 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
