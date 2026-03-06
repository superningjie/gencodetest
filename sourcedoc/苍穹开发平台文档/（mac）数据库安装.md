# （mac）数据库安装

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=218025841056723200&id=558312387053264896&type=Knowledge&productLineId=29&lang=zh-CN

## （mac）数据库安装

 __

[![金蝶云社区-hhkwan](https://vip.kingdee.com/download/0101de5f9bcd7d164f62b0fe71bd8b0b22fb.png)](javascript:;)

hhkwan

更新于 2025-12-19 12:42

浏览数： 1.7万 

## 1 背景说明

搭建轻量级开发环境时需要数据库，我们推荐版本12的PostgreSQL，若使用其他版本，可能会出现未知问题，请谨慎选择。

本文为**mac上** 安装PostgreSQL数据库及修改配置参数的操作指引。

## 2 数据库安装

### 2.1 数据库下载

通过以下链接，下载12.18版本的PostgreSQL数据库：

[https://get.enterprisedb.com/postgresql/postgresql-12.18-1-osx.dmg](/tolink?target=https%3A%2F%2Fget.enterprisedb.com%2Fpostgresql%2Fpostgresql-12.18-1-osx.dmg)

### 2.2 数据库安装

下载安装包后，双击执行，进入安装[流程](https://www.kingdee.com/products/cosmic_process_service.html?utm_source=shequ )，使用默认值，按照安装提示依次点击next即可。

![](https://vip.kingdee.com/download/01098b632a6e97024f1ba0ba4a18573a9a9f.png)

**Step1：选择数据库安装目录（推荐使用默认路径）**

注意：安装目录**最好使用默认目录（即/Library）** ，若安装在其他目录下，可能会因缺少权限等导致数据库安装失败，无法使用。

![](https://vip.kingdee.com/download/01095a928db328d240c5bbde2019a5c75c94.png)

**Step2：选择数据库的数据存储目录**

默认勾选四项，点击next。

![](https://vip.kingdee.com/download/0109b590d143e09f41bdaba7d1ea9fd74448.png)

选择数据库的数据存储路径，使用默认值即可

![](https://vip.kingdee.com/download/010926351027cebd436c90432d99c8536f6f.png)

**Step3：设置连接密码**

在此页面设置数据库密码，并再次输入确定密码。

数据库的用户名默认是：postgres

![](https://vip.kingdee.com/download/0109db148995409e4339a4ce6d877898ef8a.png)

**Step4：设置数据库监听端口**

使用默认值5432即可，若5432端口已被别的软件占用，可修改端口号，如改成5433。

![](https://vip.kingdee.com/download/0109fe8f96dca01b49bbb74111b777f75137.png)

**Step5：设置locale**

使用默认值即可。

![](https://vip.kingdee.com/download/0109d1c03948d9d8482aaa5da485c9352f0e.png)

**Step6：检查环境信息**

![](https://vip.kingdee.com/download/0109a84f02d433a44df4bd96ba409301d913.png)

**Step7：数据库安装**

**![](https://vip.kingdee.com/download/01092ba7570c9ced4067810f660e3f407230.png)**

**![](https://vip.kingdee.com/download/0109cfaf73fcc40248acbaf8743a8a22ab22.png)**

**Step8：安装成功后，取消附件软件安装**

到这个页面，PG数据库已成功安装，取消附件软件安装勾选，点finish即可。

![](https://vip.kingdee.com/download/0109e06e262a36744f4597906d65cd7e3fb8.png)

至此，数据库已安装完成，需要进行参数调整。

### 2.3 数据库参数调整

数据库安装完成后，需要调整数据库安装目录下pg_hba.conf、postgresql.conf两个文件中的参数，再重启数据库。

以下介绍通过命令行方式修改参数，直接复制1~6步的命令，执行即可。

打开mac终端：

1） 通过**sudo su –** 命令将用户切换为root，输入密码

2） 通过**su postgres** 命令切换至用户postgres

3） 通过**cd ~** 命令进入PostgreSQL安装目录（ _这步若报Permission denied，忽略，直接进行下一步_ ）

4） 通过**echo "host all all 0.0.0.0/0 md5" >> ~/data/pg_hba.conf **命令修改pg_hba.conf文件，开放数据库端口

5） 通过 **sed -i "" "s/max_connections = 100/max_connections = 1000/g" ~/data/postgresql.conf** 命令修改postgresql.conf中的最大连接数，调整至1000或者以上

6） 通过**~/bin/pg_ctl restart -D ~/data** 命令重启数据库

![](https://vip.kingdee.com/download/01096a25552b47cd4a0582475019542f3c6d.png)

 __上一篇：（windows)数据库安装

下一篇：环境安装

 __

4.8 16人评分

内容反馈

*  __评论
收藏 4 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
