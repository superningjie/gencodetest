# setContext事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612178977499648&id=225572357534754048&type=Knowledge&productLineId=29&lang=zh-CN

## initializeOperationResult事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:27

浏览数： 3,757 

1 事件介绍

操作执行时，会先创建好操作结果对象，后续在操作执行过程中，把操作结果记录在操作结果对象上。操作插件，可以重写本事件，接收传入的操作结果对象，放在本地变量中，在后续其它事件中使用。

  


2 事件触发时机

操作创建好操作结果对象后，触发本事件。

  


3 代码模板
    
    
    package kd.operation.demo.plugin;
     
    import kd.bos.entity.operate.result.OperationResult;
    import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
     
    public class OperationDemoPlugin extends AbstractOperationServicePlugIn {
     
        @Override
        public void initializeOperationResult(OperationResult result) {
             // TODO Auto-generated method stub
             super.initializeOperationResult(result);
        }
    }

  


__上一篇：setContext事件

下一篇：单据转换插件-插件基类

 __

5.0 1人评分

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
