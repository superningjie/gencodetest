# beforeClick 事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=228984722775741184&type=Knowledge&productLineId=29&lang=zh-CN

## beforeClick 事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-18 11:51

浏览数： 9,102 

1 事件介绍

插件可以在此事件，进行数据校验，取消后续处理。

  


2 事件触发时机

用户点击文本字段的按钮时，触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.dynamicform.pcform.field.template;
     
    import java.util.EventObject;
     
    import kd.bos.dataentity.utils.StringUtils;
    import kd.bos.form.control.Control;
    import kd.bos.form.control.events.BeforeClickEvent;
    import kd.bos.form.plugin.AbstractFormPlugin;
     
    public class TextFieldBeforeClick extends AbstractFormPlugin {
     
           private final static String KEY_TEXTFIELD1 = "textfield1";
     
           @Override
           public void registerListener(EventObject e) {
                  super.registerListener(e);
     
                  // 侦听文本字段按钮点击事件
                  this.addClickListeners(KEY_TEXTFIELD1);
           }
     
           @Override
           public void beforeClick(BeforeClickEvent evt) {
                  super.beforeClick(evt);
                  Control source = (Control)evt.getSource();
                  if (StringUtils.equals(KEY_TEXTFIELD1, source.getKey())){
                         // TODO 在此添加业务逻辑
                  }
           }
     
    }

  


4 参数说明
    
    
    public class BeforeClickEvent extends ClickEvent：
           public Object getSource()：文本字段的控件编程模型，TextEdit对象实例；
           public void setCancel(boolean cancel)：取消后续处理。

  


5 应用示例
    
    
    /**
         * 按钮的beforeClick一般是用来一些事件校验使用
         * 
         * @param evt
         */
        @Override
        public void beforeClick(BeforeClickEvent evt) {
    
            if (getModel().getValue("text") == "test") {
                evt.setCancel(true);
                this.getView().showErrMessage("文本内容不能为test", "输入错误");
                return;
            }
    
            super.beforeClick(evt);
        }
    
        /**
         * 按钮的click在绑定的操作后执行，一般用来执行一些操作收尾逻辑，比如有些操作后需要发邮件通知用户接收相关信息
         * 
         * @param evt
         */
        @Override
        public void click(EventObject evt) {
            super.click(evt);
            // 发送邮件
            this.getView().showSuccessNotification("操作已成功，请稍后接收邮件信息查看。");
        }

  


__上一篇：itemClick事件（动态表单）

下一篇：click 事件

 __

4.8 4人评分

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
