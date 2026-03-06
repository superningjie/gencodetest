# beforeItemClick事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238607792339561472&id=224163598413438976&type=Knowledge&productLineId=29&lang=zh-CN

## beforeItemClick事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:08

浏览数： 6,594 

1 事件介绍

插件可以在此事件中，对列表选中数据进行检查，取消系统预置的按钮操作执行。

  


2 事件触发时机

用户点击单据列表主菜单工具栏上的按钮时，触发此事件。

#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.bill.list.template;
     
    import   kd.bos.dataentity.utils.StringUtils;
    import   kd.bos.form.control.events.BeforeItemClickEvent;
    import   kd.bos.list.plugin.AbstractListPlugin;
     
    public class BeforeItemClick extends AbstractListPlugin {
     
        private final static String KEY_BARITEM1 = "baritemap1";
       
        @Override
        public void beforeItemClick(BeforeItemClickEvent evt) {
             if (StringUtils.equals(KEY_BARITEM1, evt.getItemKey())){
                 // TODO 在此添加业务逻辑
             }
        }
    }

  


#### 4 事件参数

**public** **class** BeforeItemClickEvent **extends** ItemClickEvent

  * **public** String getItemKey()：按钮标识；

  * **public** String getOperationKey()：按钮绑定的操作；

  * **public** **void** setCancel(**boolean** cancel)：取消操作。




#### 5 应用示例

参阅[itemClick事件示例](https://vip.kingdee.com/article/224168410605527040)。

 __上一篇：filterContainerSearchClick事件

下一篇：itemClick事件

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
