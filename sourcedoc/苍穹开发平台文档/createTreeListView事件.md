# createTreeListView事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238609327538441216&id=225199266711314176&type=Knowledge&productLineId=29&lang=zh-CN

## createTreeListView事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:14

浏览数： 1,840 

1 事件介绍

插件可以在此事件中，构建自定义的树列表视图模型实例，代替系统预置的树列表视图模型实例TreeListView。

  


2 事件触发时机

当系统或者插件，尝试获取树列表视图模型实例时，即触发此事件。

此事件属于高级编程，业务插件自行实现列表视图模型[ITreeListView](https://vip.kingdee.com/article/225179627000274944)接口，完成更加灵活的业务需求。

  


3 代码模型

  

    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.list.events.CreateTreeListViewEvent;
    import kd.bos.list.plugin.AbstractTreeListPlugin;
     
    public class CreateTreeListView extends AbstractTreeListPlugin {
     
        @Override
        public void createTreeListView(CreateTreeListViewEvent e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

  


**public** **class** CreateTreeListViewEvent **extends** EventObject

  * **public** Object getSource()：事件源，列表视图模型IListView接口实例；

  * **public** **void** setView(AbstractTreeListView view)：设置自定义的树形列表视图模型实例，扩展实现了ITreeListView接口的抽象基类AbstractTreeListView。




  


5 应用示例

  


通常情况下，业务插件不需要关注此事件，示例略。

 __上一篇：setView事件

下一篇：setTreeListView事件

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
