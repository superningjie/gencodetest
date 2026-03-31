# execute事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=253953358401388288&productLineId=29&lang=zh-CN

## execute事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:55

浏览数： 8,776 

1 事件介绍

该事件用于执行任务。

  


2 代码模板
    
    
    package kd.task.demo.plugin;
     
    import java.util.Map;
     
    import kd.bos.context.RequestContext;
    import kd.bos.exception.KDException;
    import kd.bos.schedule.executor.AbstractTask;
     
    public class TaskDemoPlugin extends AbstractTask{
     
        @Override
        public void execute(RequestContext arg0, Map<String, Object> arg1) throws KDException {
             // TODO Auto-generated method stub        
        }
    }

  


3 应用示例

3.1 案例说明

每隔一个月给用户增加一天年假

  


3.2 实现方案

配置后台任务，在execute事件中给用户增加一天年假。

  


3.3 实例代码
    
    
    package kd.task.demo.plugin;
     
    import java.util.Map;
     
    import kd.bos.context.RequestContext;
    import kd.bos.dataentity.entity.DynamicObject;
    import kd.bos.exception.KDException;
    import kd.bos.orm.query.QCP;
    import kd.bos.orm.query.QFilter;
    import kd.bos.schedule.executor.AbstractTask;
    import kd.bos.servicehelper.BusinessDataServiceHelper;
    import kd.bos.servicehelper.operation.SaveServiceHelper;
     
    public class TaskDemoPlugin extends AbstractTask{
     
        @Override
        public void execute(RequestContext arg0, Map<String, Object> arg1) throws KDException {
             QFilter qFilter = new QFilter("kdec_applytype", QCP.equals, "A");
             DynamicObject dObject = BusinessDataServiceHelper.loadSingle("kdec_applybill", "kdec_billno,kdec_applytype,kdec_totalday", new QFilter[] {qFilter});
             int totalDay = dObject.getInt("kdec_totalday");
             totalDay = totalDay + 1;
             dObject.set("kdec_totalday", totalDay);
             SaveServiceHelper.save(new DynamicObject[] {dObject});
        }
    }

  


__上一篇：后台任务插件-插件基类

下一篇：业务场景扩展点插件

 __

5.0 1人评分

内容反馈

*  __评论
收藏 3 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
