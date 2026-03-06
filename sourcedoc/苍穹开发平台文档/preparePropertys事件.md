# preparePropertys事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238614097267572224&id=225936290967823872&type=Knowledge&productLineId=29&lang=zh-CN

## preparePropertys事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:35

浏览数： 1,796 

1 事件介绍

反写插件可以在此事件中，指定需要用到的下游单据字段名，以确保在后续读取下游单据数据包时，会包含反写插件需用到的字段。 

这个事件比设置上下文方法setContext更早执行，在此事件中，只能依赖事件参数获取上下文信息。

  


2 事件触发时机

在读取下游目标单数据之前，触发此事件。

  


3 代码模板
    
    
    package kd.writeback.demo.plugin;
     
    import kd.bos.entity.botp.plugin.AbstractWriteBackPlugIn;
    import kd.bos.entity.botp.plugin.args.PreparePropertysEventArgs;
     
    public class WriteBackDemoPlugin extends AbstractWriteBackPlugIn{
     
        @Override
        public void preparePropertys(PreparePropertysEventArgs e) {
             // TODO Auto-generated method stub
             super.preparePropertys(e);
        }
    }

  


__上一篇：getCurrLinkSetItem事件

下一篇：beforeTrack事件

 __

1.0 2人评分

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
