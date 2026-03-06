# registerListener事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=222731953945358592&type=Knowledge&productLineId=29&lang=zh-CN

## registerListener事件

 __

[![金蝶云社区-云社区用户4rLH879](https://vip.kingdee.com/download/010162cfa9d4437711e8b441060400ef5315.png)](javascript:;)

云社区用户4rLH879

更新于 2024-09-14 08:46

浏览数： 1.5万 

#### 1 事件介绍

在此事件，可以侦听各个控件的插件事件。

  


说明：控件必须在registerListener方法中进行注册之后才会执行控件的事件。比如页面中有一个按钮控件，需要先在registerListener方法中添加该按钮控件，点击按钮时才会执行click、beforeClick等方法，否则不会执行，按钮控件相关事件请参考[按钮控件 (kingdee.com)](https://vip.kingdee.com/article/224138661179454720?specialId=218022218066869248&productLineId=29&isKnowledge=2&lang=zh-CN)。其他控件同理。

  


#### 2 事件触发时机

用户与界面上的控件进行交互时，即会触发此事件。

  


#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.template;
     
    import   java.util.EventObject;
     
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class RegisterListener extends AbstractFormPlugin {
     
        @Override
        public void registerListener(EventObject e) {
             // TODO 侦听控件的插件事件
        }
    }

  


4 参数说明 

EventObject e: 事件参数对象，含有事件源

  * Object getSource(): 事件源，表单编程模型IFormView对象。




#### 5 应用示例

5.1 案例说明

  


1\. 响应主菜单上自定义的菜单点击事件；

2\. 响应自定义按钮点击事件；

3\. 响应单据体行点击事件；

4\. 响应树形控件节点点击事件。

5.2 实现方案

1\. 插件实现控件的插件事件接口，实现事件的处理方法；

2\. 捕获表单 registerListener事件，侦听控件的插件事件，传入实现了事件接口的插件实例；

3\. 略过事件处理的详细逻辑。

5.3 实例代码
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.bizcase;
     
    import java.util.EventObject;
     
    import   kd.bos.dataentity.utils.StringUtils;
    import   kd.bos.form.control.Button;
    import   kd.bos.form.control.Control;
    import   kd.bos.form.control.EntryGrid;
    import   kd.bos.form.control.Toolbar;
    import   kd.bos.form.control.TreeView;
    import   kd.bos.form.control.events.ClickListener;
    import   kd.bos.form.control.events.ItemClickEvent;
    import   kd.bos.form.control.events.ItemClickListener;
    import   kd.bos.form.control.events.RowClickEvent;
    import   kd.bos.form.control.events.RowClickEventListener;
    import   kd.bos.form.control.events.TreeNodeClickListener;
    import   kd.bos.form.control.events.TreeNodeEvent;
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class RegisterListenerSample extends AbstractFormPlugin implements ItemClickListener, ClickListener,   RowClickEventListener, TreeNodeClickListener {
       
        private final static String KEY_MBAR = "tbmain";
        private final static String KEY_BARITEM1 = "baritem1";
        private final static String KEY_BUTTON1 = "buttonap1";
        private final static String KEY_ENTRYENTITY = "entryentity";
        private final static String KEY_TREEVIEW1 = "treeviewap1";
       
        @Override
        public void registerListener(EventObject e) {
             super.registerListener(e);
            
             // 侦听各控件的插件事件，传入实现了事件接口的插件实例
            
             // 主菜单按钮点击
             Toolbar mbar = this.getView().getControl(KEY_MBAR);
             mbar.addItemClickListener(this);
            
             // 按钮点击
             Button button = this.getView().getControl(KEY_BUTTON1);
             button.addClickListener(this);
            
             // 单据体行点击
             EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY);
             entryGrid.addRowClickListener(this);
            
             // 树型控件点击
             TreeView treeView = this.getView().getControl(KEY_TREEVIEW1);
             treeView.addTreeNodeClickListener(this);
        }
       
        @Override
        public void itemClick(ItemClickEvent evt) {
             super.itemClick(evt);
             if (StringUtils.equals(KEY_BARITEM1, evt.getItemKey())){
                 // 事件处理代码略过
             }
        }
       
        @Override
        public void click(EventObject evt) {
             super.click(evt);
             Control source = (Control)evt.getSource();
             if (StringUtils.equals(KEY_BUTTON1, source.getKey())){
                 // 事件处理代码略过
             }
        }
       
        @Override
        public void entryRowClick(RowClickEvent evt) {
             Control source = (Control) evt.getSource();
             if (StringUtils.equals(KEY_ENTRYENTITY, source.getKey())){
                 // 事件处理代码略过
             }
        }
       
        @Override
        public void treeNodeClick(TreeNodeEvent evt) {
             // 事件处理代码略过
             TreeView treeView = (TreeView)evt.getSource();
             if (StringUtils.equals(KEY_TREEVIEW1, treeView.getKey())){
                 // 事件处理代码略过
             }
        }
    }

  


__上一篇：initialize事件

下一篇：getEntityType事件

 __

4.6 9人评分

内容反馈

*  __评论
收藏 20 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
