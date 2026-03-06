# initTreeToolbar事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238609327538441216&id=226716338566047232&type=Knowledge&productLineId=29&lang=zh-CN

## initTreeToolbar事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:55

浏览数： 1,705 

1 事件介绍

工具栏面板中，包含分组节点搜索框、新增、修改、删除节点按钮；

插件可以在此事件，设置工具栏面板的可见性。

  


2 事件触发时机

左树右表列表界面初始化，分组树控件的工具栏面板初始化结束后，触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import java.util.EventObject;
     
    import kd.bos.list.plugin.AbstractTreeListPlugin;
     
    public class InitTreeToolbar extends AbstractTreeListPlugin {
     
        @Override
        public void initTreeToolbar(EventObject e) {
             super.initTreeToolbar(e);
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** EventObject **implements** java.io.Serializable

  * **public** Object getSource()：事件源。




  


5 应用示例

示例代码节选（完整实例请参阅[refreshNode事件](https://vip.kingdee.com/article/225256652558630400)示例）
    
    
        /**
         * 初始化树分组控件上的工具面板时，触发此事件
         * @remark
         * 插件在此事件，隐藏树工具面板
         */
        @Override
        public void initTreeToolbar(EventObject e) {
             super.initTreeToolbar(e);
             this.getView().setVisible(false, KEY_TREEBUTTONPANEL);
             // 如下代码演示单独隐藏树分组面板-新增按钮
             //this.getView().setVisible(false, "btnnew");
        }

  


__上一篇：initializeTree事件

下一篇：treeToolbarClick事件

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
