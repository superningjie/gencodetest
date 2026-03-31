# afterSaveSourceBill事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238614097267572224&id=225991761930069504&type=Knowledge&productLineId=29&lang=zh-CN

## afterSaveSourceBill事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:37

浏览数： 2,085 

1 事件介绍

afterSaveSourceBill事件在源单数据保存到数据库后调用。

  


2 代码模板
    
    
    package kd.writeback.demo.plugin;
     
    import kd.bos.entity.botp.plugin.AbstractWriteBackPlugIn;
    import kd.bos.entity.botp.plugin.args.AfterSaveSourceBillEventArgs;
     
    public class WriteBackDemoPlugin extends AbstractWriteBackPlugIn{
     
        @Override
        public void afterSaveSourceBill(AfterSaveSourceBillEventArgs e) {
             // TODO Auto-generated method stub
             super.afterSaveSourceBill(e);
        }
    }

__上一篇：beforeSaveSourceBill事件

下一篇：rollbackSave事件

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
