# beforeCreateLink事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612670079680256&id=225704290760032768&type=Knowledge&productLineId=29&lang=zh-CN

## beforeCreateLink事件

 __

[![金蝶云社区-iihorse](https://vip.kingdee.com/download?fileName=010162cfa9d4437711e8b441060400ef5315.png)](javascript:;)

iihorse

更新于 2024-04-17 20:32

浏览数： 3,183 

1 事件介绍

插件可以在此事件，撤销记录源单信息。

  


2 事件触发时机

目标单字段值填写完毕，开始在目标单关联子实体中，记录源单信息之前，触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.billconvert.template;
     
    import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
    import kd.bos.entity.botp.plugin.args.BeforeCreateLinkEventArgs;
     
    public class BeforeCreateLink extends AbstractConvertPlugIn {
     
        @Override
        public void beforeCreateLink(BeforeCreateLinkEventArgs e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** BeforeCreateLinkEventArgs **extends** ConvertPluginEventArgs

  * **public** **void** setCancel(**boolean** cancel)：取消记录关联关系；

  * **public** ExtendedDataEntitySet getTargetExtDataEntitySet()：生成的目标单扩展数据包；

  * **public** Map<String, DynamicProperty> getFldProperties() ：源单字段与源单行数据包属性对象映射字典，需要据此到源单行中取需要的字段值。




  


5 应用示例

5.1 案例说明

1\. 根据动态条件，决定是否记录与源单的关联关系。

  


5.2 实现方案

1\. 捕获beforeCreateLink事件；

2\. 判断条件，如果条件满足，则取消记录关联关系。

  


5.3 实例代码
    
    
    package kd.bos.plugin.sample.bill.billconvert.bizcase;
     
    import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
    import kd.bos.entity.botp.plugin.args.BeforeCreateLinkEventArgs;
     
    public class BeforeCreateLinkSample extends AbstractConvertPlugIn {
     
        @Override
        public void beforeCreateLink(BeforeCreateLinkEventArgs e) {
             e.setCancel(this.isCancelLink());
        }
       
        private boolean isCancelLink(){
             return true;
        }
    }

  


__上一篇：afterFieldMapping事件

下一篇：afterCreateLink事件

 __

1.0 1人评分

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
