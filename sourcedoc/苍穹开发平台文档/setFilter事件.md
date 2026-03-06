# setFilter事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238607792339561472&id=224127979746336768&type=Knowledge&productLineId=29&lang=zh-CN

## setFilter事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:07

浏览数： 1万 

1 事件介绍

插件可以调整条件内容、追加条件，从而影响列表取数。  


  


2 事件触发时机

单据列表控件，在构建好取数条件，准备重新取数之前，触发此事件。

3 代码模板
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.form.events.SetFilterEvent;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class SetFilter extends AbstractListPlugin {
     
        @Override
        public void setFilter(SetFilterEvent e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** SetFilterEvent **extends** EventObject  


  * **public** Object getSource()：事件源，过滤控件FilterContainer实例；

  * **public** List<QFilter> getQFilters()：系统根据过滤面板中的条件内容，生成的列表过滤条件集合；可以从此集合移除条件、添加条件。




  


5 应用示例

### **示例：setFilter(****如果是树型列表的过滤条件添加建议重载 buildTreeListFilter)**

@Override

**public** **void** setFilter(SetFilterEvent e) {

e.addCustomQFilter(**new** QFilter("combofield", "=", "1"));

}

 __上一篇：beforeCreateListDataProvider事件

下一篇：filterContainerSearchClick事件

 __

5.0 2人评分

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
