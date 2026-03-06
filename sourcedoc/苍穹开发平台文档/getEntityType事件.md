# getEntityType事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=222733151671020800&type=Knowledge&productLineId=29&lang=zh-CN

## getEntityType事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 19:47

浏览数： 5,233 

#### 1 事件介绍

表单基于实体模型，创建数据包之前，触发此事件，传入系统自动读取到的表单主实体模型；

插件可以在此事件，修改表单原始实体模型，动态注册新的属性，从而实现向界面动态添加字段。

特别说明：

向界面动态添加字段，还需要和另外几个事件一起配合，详见[loadCustomControlMetas](https://vip.kingdee.com/article/222725870442950912)事件说明。

  


#### 2 事件触发时机

创建数据包之前。

#### 3 代码模板

本事件需要与其他事件一起配合使用，代码模板放在一起，详见loadCustomControlMetas事件。

#### 4 参数说明

GetEntityTypeEventArgs e：

  * MainEntityType getOriginalEntityType()：界面原始的实体模型；

  * setNewEntityType(MainEntityType newEntityType)：设置界面最新的实体模型；通常是以原始界面实体模型为模板，克隆后，动态添加一些新的属性对象，传回界面。




#### 5 应用示例

参见[loadCustomControlMetas事件](https://vip.kingdee.com/article/222725870442950912)的应用示例，动态添加字段后，同步处理此事件，向主实体注册新字段对应的属性。

 __上一篇：registerListener事件

下一篇：createNewData事件

 __

暂无评分

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
