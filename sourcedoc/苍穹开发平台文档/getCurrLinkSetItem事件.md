# getCurrLinkSetItem事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238614097267572224&id=225935913598354176&type=Knowledge&productLineId=29&lang=zh-CN

## getCurrLinkSetItem事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:35

浏览数： 1,292 

1 事件介绍

反写引擎执行时，会逐个实体进行循环处理，搜索这个实体有没有记录和源单的关联关系。如果有，则执行反写。 如果没有，则跳过反写。 反写插件调用此方法，可以获取当前正在处理的实体。 这不是插件事件，插件不需要重写此方法。

  


2 代码模板
    
    
    package kd.writeback.demo.plugin;
     
    import kd.bos.entity.LinkSetItemElement;
    import kd.bos.entity.botp.plugin.AbstractWriteBackPlugIn;
     
    public class WriteBackDemoPlugin extends AbstractWriteBackPlugIn{
     
        @Override
        public LinkSetItemElement getCurrLinkSetItem() {
             // TODO Auto-generated method stub
             return super.getCurrLinkSetItem();
        }
    }

  


__上一篇：getTargetSubMainType事件

下一篇：preparePropertys事件

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
