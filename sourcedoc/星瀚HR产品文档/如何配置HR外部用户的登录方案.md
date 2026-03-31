# 如何配置HR外部用户的登录方案

原文链接：https://vip.kingdee.com/knowledge/specialDetail/340089103180200704?category=340130952184082688&id=385131334303667968&type=Knowledge&productLineId=2&lang=zh-CN

## 如何配置HR外部用户的登录方案

 __

[![金蝶云社区-haizhen](https://vip.kingdee.com/download/01018da118a52b414b8d9addb88930ebeaa7.png)](javascript:;)

haizhen

更新于 2026-01-30 18:16

浏览数： 1,510 

## 

## 变更记录

产品版本| 更新内容| 更新日期  
---|---|---  
V5.0.023| 初始版本| 2023年06月30日  
V6.0.16| 新增邮箱、匿名账号登录、PC端登录配置| 2024年7月30日  
  
  


## 1简介

### 1.1 功能介绍

登录管理用于配置HR外部临时用户的移动端登录方案，支持按用户类型和登录场景配置登录页显示效果。外部临时用户在通过移动端登录时，按规则自动匹配以展示不同的登录页面。

  


### 1.2 系统路径

系统路径：[HR](https://www.kingdee.com/products/cosmic_hr.html?utm_source=shequ)基础服务云>HR基础服务>登录管理>登录页配置

## 2主要操作

### 2.1登录页面方案配置

步骤一：进入登录页配置列表，点击 “新增”按钮。

![](https://vip.kingdee.com/download/01004f344e4162164fc096dcac757ce88287.png)

步骤二：录入登录页基本信息、移动端、PC端等信息，并点击保存则保存成功；移动端、PC端配置只需配置任意一个即可。

![](https://vip.kingdee.com/download/0100382fc5d7e3054fe8b0f7ba05ce0291d9.png)

![](https://vip.kingdee.com/download/01003a9e28a2d86b4bdba85a2d4bf2483b87.png)

  


特殊说明：

1、隐私声明：选择在平台已配置好的隐私声明，用于用户登录时的隐私声明签署；

2、 平台隐私声明配置路径：系统管理>隐私声明服务>隐私声明；

3、登录方式：短信登录、邮箱登录、匿名登录。登录页面可通过短信登录、邮箱登录、匿名登录三种方式。目前匿名登录方式只有央国企测评场景使用了；

4、短信登录、邮箱登录是必须签署隐私声明的。匿名登录无需签署隐私声明；

5、移动端配置和PC端配置不一定全都需要配置，按需配置即可。注意：只配置移动端则默认只有移动端登录，pc端打开依旧是移动端登录页面效果。

5.1移动端配置：

跳转表单：配置登录后自动跳转的表单页面，临时用户登录成功后系统自动跳转至该表单页面；

logo和背景图：预置数据自带系统已预置Logo和背景图，若不符合需求，点击选择后插入相应图片。

例如：完美入职向用户发送入职邀约，登录页展示效果如下：

![](https://vip.kingdee.com/download/0100d0342e3dd65b4cc3b5f71db7a26a5419.png)

  


  


5.2 PC端配置：

跳转表单：配置登录后PC端自动跳转的表单页面，临时用户登录成功后系统自动跳转至该表单页面；

logo和背景图：预置数据自带Logo和背景图，可添加客户的logo和背景图片。

![](https://vip.kingdee.com/download/01005bf37b96467d472589036deab6da0c63.png)

  


### 2.2登录页匹配规则配置

如何配置不同人员使用不同登录方案？以入职登录为例。

在HR基础服务云>业务规则管理>策略管理中，修改系统预置场景的“登录配置方案”下配置策略及规则。具体策略及规则配置可参考以下帮助说明：

1、[如何定义及配置业务规则-策略？ ](https://vip.kingdee.com/knowledge/specialDetail/340864080368263680?category=340881852791689984&id=325011128646647808&productLineId=2)

2、[如何配置业务规则-决策集？ ](https://vip.kingdee.com/article/326662343084050944?productLineId=2&isKnowledge=2)

###   


### 2.3使用场景示例

已入职邀约为例：目前只支持移动端登录，支持短信、邮箱登录方式。

1、设置登录页配置：

![](https://vip-admin.kingdee.com/download/0100382fc5d7e3054fe8b0f7ba05ce0291d9.png)

2、入职发送邀约：根据规则引策略匹配，发送入职邀约短信，登录链接中可定义登录的默认登录语言，默认登录方式。

![](https://vip.kingdee.com/download/01090b85bce46b60481796bd17526dcc9665.png)

3、候选人收到入职邀约短信。

![](https://vip.kingdee.com/download/0109d2bdec3fa89b4bb6857c9ae8c1bd73b2.png)

4、候选人点击链接，进入登录页面。

![](https://vip-admin.kingdee.com/download/0100d0342e3dd65b4cc3b5f71db7a26a5419.png)

5、输入账号验证码，登录成功后，自动跳转入职信息登记页面。

  


## 3、注意事项

  * 入职登录方式支持短信、邮箱验证码登录，默认短信登录。

  * 登录功能依赖客户对短信服务的购买。




  


 __上一篇：HR基础资料参数

下一篇：项目二开新增云或应用，如何才可配置业务规则/HR导入导出模板/业务协同等？

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
