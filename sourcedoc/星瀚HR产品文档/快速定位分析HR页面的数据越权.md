# 快速定位分析HR页面的数据越权

原文链接：https://vip.kingdee.com/knowledge/specialDetail/609415615861675008?category=609415772661881088&id=654381741154611712&type=Knowledge&productLineId=2&lang=zh-CN

## 快速定位分析HR页面的数据越权

 __

[![金蝶云社区-haizhen](https://vip.kingdee.com/download/01018da118a52b414b8d9addb88930ebeaa7.png)](javascript:;)

haizhen

更新于 2024-12-13 20:00

浏览数： 303 

## 1 问题描述

通过[HR](https://www.kingdee.com/products/cosmic_hr.html?utm_source=shequ)通用服务分配相关角色权限后，页面没按权限范围控制。

  


## 2 原因分析

原因1：二开应用未配置权限数据。请依次排查《章节3.1、3.2、3.3、3.4》；  


原因2：未配置业务对象维度映射，或者配置字段错误。请优先排查《章节3.3》；

原因3：未启用新版数据规则方案。请优先排查《章节3.1》；

原因4：用户有多个角色权限，数据范围取合集。请优先排查《章节3.4》。

  


## 3 解决方案

### 3.1：启用新版数据规则方案

7.0以后的版本检查：开发平台搜索“旧数据规则启用开关”，点击预览表单，确认“启用新数据规则”是否开启状态，若开启则忽略，若关闭则需要切换开关为开启状态。

7.0以前的版本检查：开发平台搜索“旧数据规则启用开关”，点击预览表单，确认“启用旧数据规则”是否关闭状态，若关闭则忽略，若开启则需要切换开关为关闭状态。

  


### 3.2：预置二开应用数据

二开的应用相关页面出现越权，检查是否有预置应用数据（预置的应用才能进入HR数据权限管理），若没有，参考以下脚本预置sys库的t_perm_custpermserv应用数据。

IF NOT EXISTS(SELECT 1 FROM T_PERM_CUSTPERMSERV WHERE FAPPID = 'TODO_appid')

INSERT INTO T_PERM_CUSTPERMSERV(FID,FSERVFACTORY,FSERVNAME,FISSKIP,FISAND,FAPPID,FSERVAPPNUM) VALUES(TODO_fid,'kd.[hr](https://www.kingdee.com/products/cosmic_hr.html?utm_source=shequ)mp.hrcs.servicehelper.ServiceFactory','IHRCSDataPermissionService','0','1','TODO_appid','hrcs');

注意：1、需要把TODO_fid改成不重复的fid，把TODO_appid替换成新应用的appid。2、应用的appid可以在在meta库的 t_meta_bizapp 查询。

  


### 3.3：检查是否配置了业务对象维度映射

检查是否配置了业务对象的维度映射数据，并且配置的字段是正确的（检查配置字段是否一致可参考《[数据范围问题分析》](https://vip.kingdee.com/article/646672037058225664)中的章节3.1）。

操作路径：HR通用服务>权限管理>业务对象维度映射

例如：劳动合同新签，选择人员时，需要按行政组织范围显示员工数据；

![](https://vip.kingdee.com/download/010928f8b4d0614d4c34be7546910a087be0.png)

则需要在业务对象“劳动合同新签”中设置“员工.行政组织”字段按“行政组织”维度控权。  


![](https://vip.kingdee.com/download/01099704283be5d94ce691eb79bd6e082c2b.png)

配置时注意控权范围选项：

  * 基础资料选择范围：控制用户打开F7时的数据，按维度范围显示可选择的数据；

  * 行数据范围：控制用户打开列表是看到的数据范围；

  * 全部：包含列表过滤和选择F7时的数据。




配置之后，检查角色页面是否有相关的记录，如果没有，则需要重新保存一次角色。

![](https://vip.kingdee.com/download/0109ccbe1d1426984bb99ed4f495e3364a97.png)  


### 3.4：避免多个角色影响，删除其他角色

只保留一个HR角色进行验证，并删除“安全管理>权限管理” 中分配的所有权限。

  


  


  


##   


  


 __上一篇：HR通用服务中搜索不到功能权限，如何处理？

下一篇：金蝶云·星瀚V5.0新特性系列课程 星瀚人力云：HR业务服务平台产品演示

 __

5.0 1人评分

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
