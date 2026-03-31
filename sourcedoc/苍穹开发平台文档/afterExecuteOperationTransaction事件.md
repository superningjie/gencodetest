# afterExecuteOperationTransaction事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612178977499648&id=225571812191347968&type=Knowledge&productLineId=29&lang=zh-CN

## afterExecuteOperationTransaction事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:27

浏览数： 1.5万 

1 事件介绍

插件可以在此事件，对操作结果进行整理，或者执行其他无需事务保护的逻辑。

这个事件触发时，事务已经完成并提交，没有了事务保护，请勿在此事件更新数据库。

  


2 事件触发时机

操作执行完毕，事务提交之后，触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.bizoperation.template;
     
    import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
    import kd.bos.entity.plugin.args.AfterOperationArgs;
     
    public class AfterExecuteOperationTransaction extends AbstractOperationServicePlugIn {
     
        @Override
        public void afterExecuteOperationTransaction(AfterOperationArgs e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** AfterOperationArgs **extends** OperationArgs

  * **public** DynamicObject[] getDataEntities ()：操作成功的单据。




  


 __上一篇：rollbackOperation事件

下一篇：setContext事件

 __

5.0 6人评分

内容反馈

*  __评论
收藏 1 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
