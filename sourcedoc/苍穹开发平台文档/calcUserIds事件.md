# calcUserIds事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=253951924435959552&id=226276242965135872&type=Knowledge&productLineId=29&lang=zh-CN

## 工作流插件事件总览

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:51

浏览数： 7,016 

1 事件总览

工作流插件提供以下事件：

事件| 说明  
---|---  
**calcUserIds**|  当用户创建好[流程](https://www.kingdee.com/products/cosmic_process_service.html?utm_source=shequ )后，针对每个审批节点都要设置对应的参与人（审批人）  
**hasTrueCondition**|  平台考虑到用户在使用流程中可能会设置条件，所以在对应位置开放该权限  
**formatFlowRecord**|  用户查看审批详情时，可以在“节点记录格式化插件”中，放入自己的插件实现自己想要的逻辑，如修改显示值  
**notify**|  用户创建流程时，针对每个节点不同时机可以有不同的操作。该方法可用于自定义操作  
**notifyByWithdraw**|  节点离开，撤回时调用notifyByWithdraw方法  
  
  


 __上一篇：calcUserIds事件

下一篇：hasTrueCondition事件

 __

5.0 1人评分

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
