# setPluginName事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=221687078613332224&type=Knowledge&productLineId=29&lang=zh-CN

## setPluginName事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 19:44

浏览数： 8,994 

#### 1 事件介绍

界面显示前，准备构建界面显示参数时，系统会先创建界面插件实例。

在构建JS插件时，触发此事件，传入脚本名称；

  


2 事件触发时机

这个事件在插件刚刚构建之后即会触发，是插件能接受到的第一个事件。

只有JS代码插件，会触发这个事件；Java代码插件，不触发。

#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.template;
     
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class SetPluginName extends AbstractFormPlugin {
        String plugName;
       
        @Override
        public void setPluginName(String name) {
             this.plugName = name;
        }
       
        @Override
        public String getPluginName() {
             return this.plugName;
        }
    }

  


4 参数说明 

String name：脚本插件标识

  


#### 5 应用示例

这个插件事件触发的时机太早，没有什么实际用处，无需捕获。

  


 __上一篇：动态表单插件事件总览

下一篇：preOpenForm事件

 __

5.0 1人评分

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
