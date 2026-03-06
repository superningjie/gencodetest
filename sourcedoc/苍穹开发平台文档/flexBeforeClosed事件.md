# flexBeforeClosed事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=222765063461977088&type=Knowledge&productLineId=29&lang=zh-CN

## flexBeforeClosed事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 19:54

浏览数： 2,842 

#### 1 事件介绍

插件可以对弹性域录入值进行校验，取消弹性域录入界面的关闭。

  


#### 2 事件触发时机

弹性域维护界面关闭时，触发父界面此事件。

  


#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.template;
     
    import   kd.bos.dataentity.utils.StringUtils;
    import   kd.bos.form.events.FlexBeforeClosedEvent;
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class FlexBeforeClosed extends AbstractFormPlugin {
     
        private final static String KEY_BASEDATA1 = "basedata1";
       
        @Override
        public void flexBeforeClosed(FlexBeforeClosedEvent e) {
             super.flexBeforeClosed(e);
             if (StringUtils.equals(KEY_BASEDATA1, e.getBasedataKey())){
                 // TODO 在此添加业务逻辑
             }
        }
    }

  
说明：

**KEY_BASEDATA1** 是弹性域父基础资料字段Key，需要根据实际业务替换。

  


#### 4 参数说明

FlexBeforeClosedEvent e：

  * **public** Object getSource()：弹性域维护界面的IFormView，可以据此获取用户录入的弹性域各维度值；

  * **public** String getBasedataKey()：弹性域父基础资料字段key；

  * **public** String getFlexKey()：弹性域字段key；

  * **public** **void** setCancel(**boolean** cancel)：取消弹性域界面关闭。




 __上一篇：closedCallBack事件

下一篇：onGetControl事件

 __

4.0 1人评分

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
