# beforeShowBill 事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238607792339561472&id=224173589094529792&type=Knowledge&productLineId=29&lang=zh-CN

## billClosedCallBack事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:02

浏览数： 4,196 

1 事件介绍

插件可以在此事件，接收单据界面返回的值，进行后续处理；

也可以与双击、超链接等事件处理配合，打开自定义界面，接收自定义界面的返回值。

  


#### 2 事件触发时机

列表打开的单据界面关闭并返回到列表时，触发此事件；

  


特别说明：

列表界面的billClosedCallBack事件，与[closedCallBack](https://vip.kingdee.com/article/222764161971668736)事件的区别：

列表打开的单据界面关闭时，如果设置了回调属性，会先后触发列表插件的[billClosedCallBack](https://vip.kingdee.com/article/224173589077752576)、closeCallBack两个事件；

在billClosedCallBack事件，系统会自动把单据界面上的单据内码，传递给事件；

而在closeCallBack事件，只能收到单据界面主动返回给列表界面的任意数据。

#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.bill.list.template;
     
    import   kd.bos.list.events.BillClosedCallBackEvent;
    import   kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillClosedCallBack extends AbstractListPlugin {
     
        @Override
        public void billClosedCallBack(BillClosedCallBackEvent   e) {
             // TODO 在此添加业务逻辑
        }
    }

  


#### 4 参数说明

**public** **class** BillClosedCallBackEvent **extends** EventObject

  * **public** Object getSource()：事件源，单据列表控件BillList；

  * **public** Object getPkId()： 获取单据内码；

  * **public** CloseCallBack getCloseCallBack()：回调参数，据此了解回调源头。




  


#### 5 应用示例

5.1 案例说明

1\. 单据列表上，单据编号、文本1两列，均显示为超链接；

2\. 点击文本1，打开物料新增界面；

3\. 比较billClosedCallBack、closedCallBack两个事件的事件参数值。

5.2 实现方案

1\. 捕获[billListHyperLinkClick](https://vip.kingdee.com/article/224170381911587584)事件，显示物料新增界面，并指定回调参数；

2\. 捕获billClosedCallBack，接收事件参数pkid，提示出来；

3\. 捕获closedCallBack，接收事件参数returndata，提示出来。

5.3 实例代码
    
    
    package   kd.bos.plugin.sample.bill.list.bizcase;
     
    import   kd.bos.bill.BillShowParameter;
    import   kd.bos.bill.OperationStatus;
    import   kd.bos.dataentity.utils.StringUtils;
    import   kd.bos.form.CloseCallBack;
    import   kd.bos.form.ShowType;
    import   kd.bos.form.events.ClosedCallBackEvent;
    import   kd.bos.form.events.HyperLinkClickArgs;
    import   kd.bos.list.events.BillClosedCallBackEvent;
    import   kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillClosedCallBackSample extends AbstractListPlugin {
     
        private final static String KEY_TEXTFIELD1 = "textfield1";
     
        /**
         * 用户点击超链接单元格时，触发此事件
         */
        @Override
        public void billListHyperLinkClick(HyperLinkClickArgs args) {
             if (StringUtils.equals(KEY_TEXTFIELD1,    args.getHyperLinkClickEvent().getFieldName())){
                 // 当前点击的是文本1
                
                 // 取消系统自动打开本单的处理
                 args.setCancel(true);
                
                 // 打开物料新增界面
                 BillShowParameter showParameter = new BillShowParameter();
                 showParameter.setFormId("bd_material");
                 showParameter.getOpenStyle().setShowType(ShowType.Modal);
                 showParameter.setStatus(OperationStatus.ADDNEW);
                
                 CloseCallBack closeCallBack = new CloseCallBack(this, KEY_TEXTFIELD1);
                 showParameter.setCloseCallBack(closeCallBack);
                
                 this.getView().showForm(showParameter);
             }
        }
     
        /**
         * 单据界面关闭时，触发本事件，传入单据内码
         */
        @Override
        public void billClosedCallBack(BillClosedCallBackEvent   e) {
            
             if (StringUtils.equals(KEY_TEXTFIELD1, e.getCloseCallBack().getActionId())){
                 // 自定义的物料新增界面返回
                 long materialId = (long)e.getPkId();
                 this.getView().showMessage(String.format("事件 billClosedCallBack，可以收到系统自动打包的子界面内码%d", materialId));
             }
        }
     
        /**
         * 单据界面关闭时，也会触发本事件；但是默认不带数据返回
         */
        @Override
        public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
             if (StringUtils.equals(KEY_TEXTFIELD1, closedCallBackEvent.getActionId())){
                 // 自定义的物料新增界面返回
                 Object returnData = closedCallBackEvent.getReturnData();
                 //this.getView().showMessage(String.format("事件 closedCallBack，只能收到子界面主动返回给列表界面的数据%s", returnData));
             }
        }
       
    }

  


__上一篇：beforeShowBill 事件

下一篇：listRowClick 事件

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
