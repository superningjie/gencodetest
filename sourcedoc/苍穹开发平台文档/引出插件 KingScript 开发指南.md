# 引出插件 KingScript 开发指南

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=720698994112075264&id=720672097952902912&type=Knowledge&productLineId=29&lang=zh-CN

## 引出插件 KingScript 开发指南

 __

[![金蝶云社区-aohailin](https://vip.kingdee.com/download/010162cfa9d6437711e8b441060400ef5315.png)](javascript:;)

aohailin

更新于 2025-06-13 15:08

浏览数： 295 

### 目录

  1. [概述](file:///C:/Users/kingdee.gbl/Desktop/dist/html/user/guide/2.%E7%BC%96%E5%86%99KingScript/2.2%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91/2.2.11%E5%BC%95%E5%85%A5%E5%BC%95%E5%87%BA%E6%8F%92%E4%BB%B6/2.2.11.2%E5%BC%95%E5%87%BA%E6%8F%92%E4%BB%B6.html#%E6%A6%82%E8%BF%B0)

  2. [快速入门](file:///C:/Users/kingdee.gbl/Desktop/dist/html/user/guide/2.%E7%BC%96%E5%86%99KingScript/2.2%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91/2.2.11%E5%BC%95%E5%85%A5%E5%BC%95%E5%87%BA%E6%8F%92%E4%BB%B6/2.2.11.2%E5%BC%95%E5%87%BA%E6%8F%92%E4%BB%B6.html#%E5%BF%AB%E9%80%9F%E5%85%A5%E9%97%A8)

  3. [核心事件详解](file:///C:/Users/kingdee.gbl/Desktop/dist/html/user/guide/2.%E7%BC%96%E5%86%99KingScript/2.2%E6%8F%92%E4%BB%B6%E5%BC%80%E5%8F%91/2.2.11%E5%BC%95%E5%85%A5%E5%BC%95%E5%87%BA%E6%8F%92%E4%BB%B6/2.2.11.2%E5%BC%95%E5%87%BA%E6%8F%92%E4%BB%B6.html#%E6%A0%B8%E5%BF%83%E4%BA%8B%E4%BB%B6%E8%AF%A6%E8%A7%A3)




* * *

### 概述

引出实质为将列表数据按照模板或列表格式导出为Excel。使用引出插件时，继承`AbstractListPlugin`插件并注册到列表即可

* * *

### 快速入门

本指南主要演示通过vscode编写脚本插件，并完成插件注册过程。

### 1\. 新建ts文件，继承`AbstractListPlugin`插件
    
    
    import { AfterQueryOfExportEvent } from "@cosmic/bos-core/kd/bos/form/events";
    import { AbstractListPlugin } from "@cosmic/bos-core/kd/bos/list/plugin";
    
    class MyExportPlugin extends AbstractListPlugin {
        //事件根据自己的业务需要去重写，此处仅是演示，相关事件介绍参考核心事件详解章节
        afterQueryOfExport(e: AfterQueryOfExportEvent): void {
            super.afterQueryOfExport(e);
        }
    }
    
    let plugin = new MyExportPlugin();
    
    export { plugin };

### 2\. 右键上传ts文件到环境中

![](https://vip.kingdee.com/download/0109fef5d74e153e4f8ca9f04ae10846810b.png)

### 3\. 注册脚本插件，选择新建的脚本文件

![](https://vip.kingdee.com/download/0109d353c06c74a149c08d74e7f6ea0cf167.png)

* * *

### 核心事件详解

事件| 说明| 典型用途  
---|---|---  
beforeQueryOfExport| 查询导出数据前事件| 可以用来修改过滤条件、排序规则等  
afterQueryOfExport| 查询导出数据后事件| 可以用来修改查询返回的数据，比如修改字段数据等  
beforeExportFile| 导出文件前事件| 目前只可以用来修改导出的文件名  
afterExportFile| 导出文件后事件| 可以用来修改导出的文件内容，比如修改excel数据、格式、加密等  
  
  


 __上一篇：引入插件 KingScript 开发指南

下一篇：脚本SDK

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
