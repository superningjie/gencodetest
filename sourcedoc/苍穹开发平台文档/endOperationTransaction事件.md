# endOperationTransaction事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612178977499648&id=225571350851462656&type=Knowledge&productLineId=29&lang=zh-CN

## endOperationTransaction事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:26

浏览数： 1.7万 

1 事件介绍

可以在此事件，同步处理关联数据。

特别说明：

1. 苍穹是分布式多数据库架构，不能在此事件直接跨库更新数据，跨库需采用KDTX框架实现数据一致性；

2. 此事件触发时，已执行更新数据库的SQL语句，如果要中止操作，请抛异常中断来触发事务回滚；

  


2 事件触发时机

单据数据已经更新到数据库，事务还未提交之前，触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.bizoperation.template;
     
    import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
    import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
     
    public class EndOperationTransaction extends AbstractOperationServicePlugIn {
     
        @Override
        public void endOperationTransaction(EndOperationTransactionArgs e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

public class EndOperationTransactionArgs extends OperationArgs

  * public DynamicObject[] getDataEntities()：已经保存到数据库的单据。




  


  


 __上一篇：beginOperationTransaction事件

下一篇：rollbackOperation事件

 __

5.0 4人评分

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
