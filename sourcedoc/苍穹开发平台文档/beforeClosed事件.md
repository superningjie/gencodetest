# beforeClosed事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=222768769984991488&type=Knowledge&productLineId=29&lang=zh-CN

## beforeClosed事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 19:56

浏览数： 5,246 

#### 1 事件介绍

插件可以在此事件，取消界面关闭。

  


#### 2 事件触发时机

界面关闭之前触发此事件。

  


#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.template;
     
    import   kd.bos.form.events.BeforeClosedEvent;
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class BeforeClosed extends AbstractFormPlugin {
       
        @Override
        public void beforeClosed(BeforeClosedEvent e) {
             super.beforeClosed(e);
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明 

**public** **class** BeforeClosedEvent **extends** EventObject：  


  * **public** Object getSource()：表单IFormView；

  * **public** **void** setCancel(**boolean** cancel)：设置true，取消界面关闭；

  * **public** **void** setCheckDataChange(**boolean** checkDataChange)：设置true，退出时，不提示数据改变（此属性只在单据表单插件上有效）。




  


 __上一篇：TimerElapsed事件

下一篇：destory事件

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
