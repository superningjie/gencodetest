# beforePropertyChanged事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=228912833529089024&type=Knowledge&productLineId=29&lang=zh-CN

## beforePropertyChanged事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-18 11:51

浏览数： 6,884 

1 事件介绍

该事件用于通知插件字段值将要发生了改变，同步调整其他字段值。

  


2 事件触发时机

修改字段值之前触发。

  


说明：界面数据初始时，不触发此事件，例如在[afterCreateNewData](https://vip.kingdee.com/article/222735398575848448)事件中，修改字段值，不会触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.dynamicform.pcform.field.template;
    import kd.bos.dataentity.utils.StringUtils;
    import kd.bos.entity.datamodel.events.PropertyChangedArgs;
    import kd.bos.form.plugin.AbstractFormPlugin;
    public class BeforePropertyChanged extends AbstractFormPlugin {
    private final static String KEY_TEXTFIELD1 = "textfield1";
        @Override
        public void beforePropertyChanged(PropertyChangedArgs e) {
            String fieldKey = e.getProperty().getName();
            if (StringUtils.equals(KEY_TEXTFIELD1, fieldKey)){
                // TODO 在此添加业务逻辑
            }
        }
    }

  


说明：常量KEY_TEXTFIELD1是示例字段标识。

  


4 参数说明
    
    
    public class PropertyChangedArgs
    public IDataEntityProperty getProperty()：值发生了改变字段属性对象；
    public ChangeData[] getChangeSet()：正在发生改变的数据
    public class ChangeData  extends RowDataEntity
            public int getRowIndex()：分录行号；如果字段在单据头，此属性为0；如果字段在单据体，此属性为单据体数据行索引，从0开始；
            public Object getOldValue()：字段旧值；
            public Object getNewValue()：字段新值；
            public DynamicObject getDataEntity()：分录数据包；如果字段在单据头，此属性为表单数据包；如果字段在单据体，此属性为单据体数据行；

  


 __上一篇：pageRelease事件

下一篇：propertyChanged事件

 __

3.7 3人评分

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
