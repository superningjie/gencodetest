# setCellFieldValue事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238607792339561472&id=224220389189678848&type=Knowledge&productLineId=29&lang=zh-CN

## setCellFieldValue事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:12

浏览数： 2,672 

1 事件介绍

列表插件可以在此事件，给详细面板上的字段赋值，从而实现动态展示数据效果。

  


2 事件触发时机

列表嵌套显示详细信息面板，用户点击展开，打开详细信息面板时触发。

  


3 代码模板

  

    
    
    package kd.billlist.demo.plugin;
     
    import kd.bos.list.events.SetCellFieldValueArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillListDemoPlugin extends AbstractListPlugin{
     
        @Override
        public void setCellFieldValue(SetCellFieldValueArgs args) {
             // TODO Auto-generated method stub
             super.setCellFieldValue(args);
        }
    }

  


4 应用示例

@Override

public void expandClick(ListExpandEvent evt)

{

// TODO Auto-generated method stub

setCellFieldValue("billstatus", evt.getRowIndex(), "B");

}

  


__上一篇：baseDataColumnDependFieldSet事件

下一篇：左树右表单据列表插件-插件基类

 __

5.0 3人评分

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
