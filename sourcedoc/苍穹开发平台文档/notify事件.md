# notify事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=253951924435959552&id=226282282796220928&type=Knowledge&productLineId=29&lang=zh-CN

## notify事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2026-01-21 17:10

浏览数： 7,037 

1 事件介绍

用户创建[流程](https://www.kingdee.com/products/cosmic_process_service.html?utm_source=shequ )时，针对每个节点不同时机可以有不同的操作。当平台提供的操作不能满足用户需要时，用户可以通过插件来实现自己需要的功能。正向执行时调用notify方法。

  


2 代码模板
    
    
    package kd.workflow.demo.plugin;
     
    import kd.bos.workflow.api.AgentExecution;
    import kd.bos.workflow.engine.extitf.IWorkflowPlugin;
     
    public class WorkflowDemoPlugin implements IWorkflowPlugin{
     
        @Override
        public void notify(AgentExecution execution) {
             IWorkflowPlugin.super.notify(execution);
        }
    }

  


3 应用示例

3.1 案例说明

用户提交请假申请，当请假被驳回时，当前请假单请假天数清零，剩余请假天数复原。

  


3.2 实现方案

在驳回时注册插件，在notify方法中，对请假单进行处理，并保存。

  


3.3 实例代码
    
    
    package kd.workflow.demo.plugin;
     
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;
     
    import kd.bos.context.RequestContext;
    import kd.bos.dataentity.entity.DynamicObject;
    import kd.bos.orm.query.QCP;
    import kd.bos.orm.query.QFilter;
    import kd.bos.servicehelper.BusinessDataServiceHelper;
    import kd.bos.servicehelper.QueryServiceHelper;
    import kd.bos.servicehelper.operation.SaveServiceHelper;
    import kd.bos.servicehelper.user.UserServiceHelper;
    import kd.bos.workflow.api.AgentExecution;
    import kd.bos.workflow.api.WorkflowElement;
    import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;
    import kd.bos.workflow.engine.extitf.IWorkflowPlugin;
     
    public class WorkflowDemoPlugin implements IWorkflowPlugin{
        @Override
        public void notify(AgentExecution execution) {
             String businessKey = execution.getBusinessKey();//单据的BusinessKey(业务ID)
             DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(businessKey, "kdec_applybill");
             //请假天数
             int applyDay = dynamicObject.getInt("kdec_applyday");
             //剩余天数
             int residueDay = dynamicObject.getInt("kdec_residueday");
             //请假不通过，剩余天数复原
             applyDay = 0;
             residueDay = residueDay + applyDay;
             dynamicObject.set("kdec_applyday", applyDay);
             dynamicObject.set("kdec_residueday", residueDay);
             SaveServiceHelper.save(new DynamicObject[] {dynamicObject});
             IWorkflowPlugin.super.notify(execution);
        }
    }

  


__上一篇：formatFlowRecord事件

下一篇：引入引出插件-插件基类

 __

4.5 2人评分

内容反馈

*  __评论
收藏 8 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
