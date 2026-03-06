# customEvent事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=222767279933376256&type=Knowledge&productLineId=29&lang=zh-CN

## customEvent事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 19:55

浏览数： 5,655 

#### 1 事件介绍

本事件用于触发自定义控件的定制事件。

  


#### 2 事件触发时机

  1. 前端自定义控件，在与用户发生交互后，可以包装一个参数包，传入事件源，事件名，事件参数，发送一个事件触发请求到下一代web服务器，由系统转发给表单；

  2. 表单接收到此事件后，继续转发给业务插件，通知业务插件处理自定义控件的定制事件；

  3. 整个定制事件的触发过程，系统只作为传递通道。




#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.template;
     
    import   kd.bos.dataentity.utils.StringUtils;
    import   kd.bos.form.events.CustomEventArgs;
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class CustomEvent extends AbstractFormPlugin {
     
        private final static String KEY_CONTROL1 = "control1";
        private final static String EVENT_CUSTOM = "customevent";
       
        @Override
        public void customEvent(CustomEventArgs e) {
             if (StringUtils.equals(e.getKey(), KEY_CONTROL1)
                      && StringUtils.equals(e.getEventName(), EVENT_CUSTOM)){
                 // TODO 在此添加业务逻辑
             }
        }
    }

  


常量**KEY_CONTROL1** 替代自定义控件标识；说明：

常量**EVENT_CUSTOM** 替代自定义控件的定制事件名。

  


#### 4 参数说明

CustomEventArgs e：

  * **public** Object getSource()：表单IFormView；

  * **public** String getKey()：自定义控件；

  * **public** String getEventName()：事件名；

  * **public** String getEventArgs()：事件参数，Json字符串。




  


 __上一篇：onGetControl事件

下一篇：TimerElapsed事件

 __

4.5 2人评分

内容反馈

*  __评论
收藏 3 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
