# 【报表】DataSet的二开扩展

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=475348231329898496&id=583617151014152704&type=Knowledge&productLineId=29&lang=zh-CN

## 【报表】DataSet的二开扩展

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-06-24 14:20

浏览数： 4,475 

## 变更记录  


  


**产品版本******（季度版本号/patch补丁号）****| **更新内容**| **更新日期**  
---|---|---  
V6.0.3_1217| 初始版本| 2023年12月  
  
  


  


## 1 简介  


1.1 功能介绍

用户可基于此功能，对查询插件查询出的dataset做二次加工，提供给用户更好的延展性和扩展性。

例如：排序、过滤和增加和删除列等等操作。

  


1.2 应用场景

1、当需要实现表头过滤时，可通过此插件，自定义实现过滤规则

2、需要对dataset的列处理时也可以通过此插件实现

1.3 系统路径

【开发平台】→【报表】->【报表列表】→【查询扩展插件】 

  


2 示例  


2.1 简单示例
    
    
    public class TestReportOperate extends AbstractReportListDataPluginExt {
    
        @Override
        public void afterQuery(AfterQueryEvent event) {
            DataSet newDateSet = QueryServiceHelper.queryDataSet(this.getClass().getName(), "d_sunp_goods",
                    "id,number,creator,enable", null, null);
            // do someting 
            // ......
            // 最后把最新的dataset传给event事件对象即可。
            event.setDataSet(newDateSet);
        }
    }

  


__上一篇：增加了“报表查询行数上限”的系统参数

下一篇：报表限流

 __

3.0 2人评分

内容反馈

*  __评论
收藏 10 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
