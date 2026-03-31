# 如何使用HR业务事件日志追踪业务消息上游发送与下游消费、业务处理状态

原文链接：https://vip.kingdee.com/knowledge/specialDetail/609422026687912192?category=609422073177232384&id=385382228291038976&type=Knowledge&productLineId=2&lang=zh-CN

## 如何使用HR业务事件日志追踪业务消息上游发送与下游消费、业务处理状态

 __

[![金蝶云社区-我是可爱的大福](https://vip.kingdee.com/download/01011fd23541d7734229812dc7a2f17530e8.png)](javascript:;)

我是可爱的大福

更新于 2024-10-29 22:52

浏览数： 765 

变更记录

产品版本| 变更记录| 变更日期  
---|---|---  
V5.0.023  
| 初始版本| 2023年7月24日  
V6.0.1| 按钮名称变更，截图调整| 2023年11月6日  
  
1 简介  


1.1 功能介绍

业务事件日志提供了对业务消息上游发送与下游消费、业务处理的追踪功能。

1.2 系统路径

HR基础服务云>HR通用服务>业务协同>业务事件日志

2 主要操作  


2.1消息接收失败重新发送

操作步骤

步骤1：进入业务事件日志列表页，选择消息接收状态为异常的数据，点击异常数据的编码，进入详情页

![](https://vip.kingdee.com/download/01097f503e4fff4d4e88af7049c55bcf6ed8.png)

步骤2：进入详细页后，找到消息接收状态为失败的数据，点击“重新发送”按钮，即可实现消息再发送

![](https://vip.kingdee.com/download/01095bf08432438b41e0be44ea0186bdedbd.png)

2.2 业务处理状态异常反馈

操作步骤

步骤1：进入业务事件日志列表页，选择业务处理状态为异常的数据，点击异常数据的编码，进入详情页

![](https://vip.kingdee.com/download/0109ffe44b244f274351b80cd59d1874d81e.png)

步骤2：在消费情况分录中，找到业务消息状态为全部失败的数据，设置“异常处理”字段值为已处理或忽略，同时在“异常处理反馈信息”中对异常反馈操作进行说明

![](https://vip.kingdee.com/download/01090a3608bf7b6c487e9eb41e9dd7ea8508.png)

步骤3： 完成异常处理信息维护后，“保存”按钮

![](https://vip.kingdee.com/download/0109a19f4ee2257447e0bc85a7dea0372d2f.png)

2.3  查看业务处理状态日志

操作步骤

步骤1：在业务事件日志详情页分录中点击“查看”按钮，即可查看业务处理状态日志

![](https://vip.kingdee.com/download/0109b87d16dd5930458eb772c5389a0f15e1.png)

步骤2：业务处理状态日志记录订阅方每一次的业务处理状态以及相关的异常处理信息，点击右侧“展开”按钮，可查看异常预警信息的发送记录

![](https://vip.kingdee.com/download/01090117e00cd32d4c0ab8e1b9577e577dde.png)

  


 __上一篇：如何配置HR业务协作的协作管理员

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
