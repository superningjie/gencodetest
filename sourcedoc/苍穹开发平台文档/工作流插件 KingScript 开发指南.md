# 工作流插件 KingScript 开发指南

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=720698994112075264&id=720670643922133504&type=Knowledge&productLineId=29&lang=zh-CN

## 工作流插件 KingScript 开发指南

 __

[![金蝶云社区-aohailin](https://vip.kingdee.com/download/010162cfa9d6437711e8b441060400ef5315.png)](javascript:;)

aohailin

更新于 2025-06-13 15:08

浏览数： 343 

### 目录

  1. [概述](file:///C:/Users/kingdee.gbl/Desktop/dist/html/user/guide/2.%E7%BC%96%E5%86%99KingScript/2.2%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91/2.2.10%E5%B7%A5%E4%BD%9C%E6%B5%81%E6%8F%92%E4%BB%B6/2.2.10.1%E5%B7%A5%E4%BD%9C%E6%B5%81%E6%8F%92%E4%BB%B6.html#%E6%A6%82%E8%BF%B0)

  2. [快速入门](file:///C:/Users/kingdee.gbl/Desktop/dist/html/user/guide/2.%E7%BC%96%E5%86%99KingScript/2.2%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91/2.2.10%E5%B7%A5%E4%BD%9C%E6%B5%81%E6%8F%92%E4%BB%B6/2.2.10.1%E5%B7%A5%E4%BD%9C%E6%B5%81%E6%8F%92%E4%BB%B6.html#%E5%BF%AB%E9%80%9F%E5%85%A5%E9%97%A8)

  3. [核心事件详解](file:///C:/Users/kingdee.gbl/Desktop/dist/html/user/guide/2.%E7%BC%96%E5%86%99KingScript/2.2%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91/2.2.10%E5%B7%A5%E4%BD%9C%E6%B5%81%E6%8F%92%E4%BB%B6/2.2.10.1%E5%B7%A5%E4%BD%9C%E6%B5%81%E6%8F%92%E4%BB%B6.html#%E6%A0%B8%E5%BF%83%E4%BA%8B%E4%BB%B6%E8%AF%A6%E8%A7%A3)




* * *

### 概述

在[流程设计](https://www.kingdee.com/products/cosmic_process_service.html?utm_source=shequ )时，如果标准的设置不满足需求，可以给工作流增加扩展插件来实现更加复杂的业务逻辑，工作流的插件场景主要集中在参与人、条件规则、[流程](https://www.kingdee.com/products/cosmic_process_service.html?utm_source=shequ )控制、节点控制、自动节点。

* * *

### 快速入门

本指南主要演示通过vscode编写脚本插件，并完成插件注册过程。

### 1\. 新建ts文件，继承`WorkflowPlugin`插件
    
    
    import { AgentExecution } from "@cosmic/bos-core/kd/bos/workflow/api";
    import { IApprovalRecordItem } from "@cosmic/bos-core/kd/bos/workflow/component/approvalrecord";
    import { WorkflowPlugin } from "@cosmic/bos-core/kd/bos/workflow/engine/extitf";
    import { ArrayList, List } from "@cosmic/bos-script/java/util";
    
    class MyWorkflowPlugin extends WorkflowPlugin {
        //事件根据自己的业务需要去重写，此处仅是演示，相关事件介绍参考核心事件详解章节
        calcUserIds(execution: AgentExecution): List {
            return new ArrayList();
        }
    
        hasTrueCondition(execution: AgentExecution): boolean {
            return true;
        }
    
        formatFlowRecord(item: IApprovalRecordItem): IApprovalRecordItem {
            return item;
        }
    
        notify(execution: AgentExecution): void {
    
        }
    
        notifyByWithdraw(execution: AgentExecution): void {
            
        }
    }
    
    let plugin = new MyWorkflowPlugin();
    
    export { plugin }

### 2\. 右键上传ts文件到环境中

![](https://vip.kingdee.com/download/0109cf775205a9364560b71540b04c15c716.png)

### 3\. 注册脚本插件，选择新建的脚本文件

可在参与人、条件规则、流程控制、节点控制、自动节点等位置注册脚本插件

![](https://vip.kingdee.com/download/0109d1dfc064a0014b7dadc554e9a4682a4d.png)

* * *

### 核心事件详解

事件| 说明  
---|---  
calcUserIds| 当用户创建好流程后，针对每个审批节点都要设置对应的参与人（审批人）  
hasTrueCondition| 平台考虑到用户在使用流程中可能会设置条件，所以在对应位置开放该权限  
formatFlowRecord| 用户查看审批详情时，可以在“节点记录格式化插件”中，放入自己的插件实现自己想要的逻辑，如修改显示值  
notify| 用户创建流程时，针对每个节点不同时机可以有不同的操作。该方法可用于自定义操作  
notifyByWithdraw| 节点离开，撤回时调用 notifyByWithdraw 方法  
  
  


 __上一篇：打印插件 KingScript 开发指南

下一篇：引入插件 KingScript 开发指南

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
