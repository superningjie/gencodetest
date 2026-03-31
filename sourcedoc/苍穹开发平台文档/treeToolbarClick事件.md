# treeToolbarClick事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238609327538441216&id=225252834181350400&type=Knowledge&productLineId=29&lang=zh-CN

## treeToolbarClick事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:15

浏览数： 1,644 

1 事件介绍

插件可以在此事件中，自行处理分组新增(btnnew)、修改(btnedit)、删除(btndel)功能。

  


2 事件触发时机

用户点击了分组树上的工具栏按钮时，触发此事件。

  


 _特别说明：_

  *  _分组基础资料列表上默认配置的通用插件TemplateGroupBaseDataPlugin(派生自_[ _StandardTreeListPlugin_](https://vip.kingdee.com/article/225266958768505856) _)，已经对分组树工具栏按钮的点击进行了处理，业务插件不需要重写；_

  *  _如果需要重写，请停用系统默认配置的通用插件TemplateGroupBaseDataPlugin，然后扩展该类，重写treeToolbarClick事件。_




  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import java.util.EventObject;
     
    import kd.bos.list.plugin.AbstractTreeListPlugin;
     
    public class TreeToolbarClick extends AbstractTreeListPlugin {
       
        @Override
        public void treeToolbarClick(EventObject e) {
             super.treeToolbarClick(e);
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** EventObject **implements** java.io.Serializable

  * **public** Object getSource()：事件源。




  


5 应用示例

业务插件通常不需要捕捉此事件，示例略过。

  


 __上一篇：initTreeToolbar事件

下一篇：refreshNode事件

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
