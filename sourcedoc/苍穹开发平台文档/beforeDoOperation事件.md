# beforeDoOperation事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=222748423148059648&type=Knowledge&productLineId=29&lang=zh-CN

## beforeDoOperation事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:01

浏览数： 2.6万 

#### 1 事件介绍

插件可以在此事件：

  1. 提示确认消息；

  2. 校验数据，取消操作的执行；

  3. 传递给自定义操作参数给操作服务、操作插件。




#### 2 事件触发时机

用户点击按钮、菜单，执行绑定的操作逻辑前，触发此事件；

  


表单beforeDoOperation事件与操作校验器的区别：

  


  * 运行时机不同：

1\. 表单beforeDoOperation事件，是由表单触发的，只有在表单上执行操作时，才会被触发；后台直接操作服务，不会触发此事件；数据校验逻辑放在这个事件，有可能漏过；

2\. 操作校验器，是由[微服务](https://www.kingdee.com/products/cosmic_development_services.html?utm_source=shequ )层操作引擎执行的，不管是在表单上执行操作，还是后台调用操作服务，都会执行微服务层操作引擎，都会执行操作校验器。




  


  * 适用的操作类型不同：

1\. 操作分为两种大类：




表单操作：对界面进行处理，如关闭界面；

实体操作：更新数据库，如保存；

2\. 只有实体操作，才允许配置操作校验；

3\. 对普通的表单操作进行数据校验，只能使用表单插件beforeDoOperation事件。

  


  * 控制颗粒度不同：




1\. 批量操作时，操作校验器，对批量数据进行逐个校验，略过校验失败的数据，继续执行校验成功数据；

2\. 表单beforeDoOperation事件，只能整体取消操作，不能对批量数据进行区分。

因此，对操作进行数据校验，尽可能配置操作校验器、或使用操作插件，而不是使用表单beforeDoOperation事件。

#### 3 代码模板
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.template;
     
    import   kd.bos.dataentity.utils.StringUtils;
    import   kd.bos.form.events.BeforeDoOperationEventArgs;
    import   kd.bos.form.operate.FormOperate;
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class BeforeDoOperation extends AbstractFormPlugin {
       
        final static String KEY_OPKEY = "myoperation";
       
        @Override
        public void   beforeDoOperation(BeforeDoOperationEventArgs args) {
             super.beforeDoOperation(args);
            
             FormOperate formOperate = (FormOperate)args.getSource();
             if ( StringUtils.equals(KEY_OPKEY, formOperate.getOperateKey())){
                 // TODO 在此添加业务逻辑
             }
        }
    }

说明：

常量**KEY_OPKEY** 是操作标识，要根据实际场景进行替换。

#### 4 参数说明

BeforeDoOperationEventArgs args：

  * Object getSource()：FormOperate类型，操作执行类，包含了操作的配置信息；

  * **void** setCancel(**boolean** cancel)：可以取消操作。




#### 5 应用示例

5.1 案例说明

1\. 用户点击"付款"菜单按钮时，提示用户确认：

  * 用户确认后，才继续执行付款操作；

  * 用户取消，则不执行付款操作。




5.2 实现方案

1\. 捕获表单beforeDoOperation 事件：

  * 开始执行付款前，显示交互提示，取消本次操作；

  * 用户确认后再次执行付款操作时，不再重复显示交互提示；

  * 通过自定义操作参数，标志是否为确认后再次执行操作。




  


2\. 捕获表单confirmCallBack 事件：

  * 获取用户确认结果；

  * 用户确认付款时，重新调用付款操作；

  * 设置自定义操作参数值，标志为确认后再次执行操作，避免重复显示交互提示。




5.3 实例代码
    
    
    package   kd.bos.plugin.sample.dynamicform.pcform.form.bizcase;
     
    import   kd.bos.dataentity.OperateOption;
    import   kd.bos.dataentity.RefObject;
    import   kd.bos.dataentity.utils.StringUtils;
    import   kd.bos.form.ConfirmCallBackListener;
    import   kd.bos.form.ConfirmTypes;
    import   kd.bos.form.MessageBoxOptions;
    import   kd.bos.form.MessageBoxResult;
    import   kd.bos.form.events.BeforeDoOperationEventArgs;
    import   kd.bos.form.events.MessageBoxClosedEvent;
    import   kd.bos.form.operate.FormOperate;
    import   kd.bos.form.plugin.AbstractFormPlugin;
     
    public class BeforeDoOperationSample extends AbstractFormPlugin {
     
        private final static String KEY_PAY = "pay";
        private final static String OPPARAM_AFTERCONFIRM = "afterconfirm";
       
        /**
         * 执行操作前，触发此事件
         */
        @Override
        public void   beforeDoOperation(BeforeDoOperationEventArgs args) {
             super.beforeDoOperation(args);
            
             FormOperate operate = (FormOperate)args.getSource();
             if (StringUtils.equals(operate.getOperateKey(), KEY_PAY)){
                 // 付款操作
                
                 // 尝试读取操作自定义参数：判断是否确认后再次执行付款操作
                 RefObject<String> afterConfirm = new RefObject<>();
                 if (!operate.getOption().tryGetVariableValue(OPPARAM_AFTERCONFIRM, afterConfirm)){
                      // 自定义操作参数中，没有afterconfirm参数：说明是首次执行付款操作，需要提示用户确认
     
                      // 显示确认消息
                      ConfirmCallBackListener confirmCallBacks = new ConfirmCallBackListener(KEY_PAY, this);
                      String confirmTip = "您确认要付款给xxx?";
                      this.getView().showConfirm(confirmTip, MessageBoxOptions.YesNo, ConfirmTypes.Default, confirmCallBacks);
     
                      // 在没有确认之前，先取消本次操作
                      args.setCancel(true);
                 }
     
                 // 如下代码，演示如何增加自定义操作参数，由系统传递给操作服务及操作插件
                 // （仅供演示，与本案例需求无关）
                 operate.getOption().setVariableValue(OPPARAM_AFTERCONFIRM, "true");
             }
        }
       
        /**
         * 用户确认了交互信息后，触发此事件
         */
        @Override
        public void confirmCallBack(MessageBoxClosedEvent messageBoxClosedEvent) {
             super.confirmCallBack(messageBoxClosedEvent);
            
             if (StringUtils.equals(KEY_PAY, messageBoxClosedEvent.getCallBackId())){
                 // 付款确认
                
                 if (messageBoxClosedEvent.getResult() == MessageBoxResult.Yes){
                      // 确认执行付款操作
                     
                      // 构建操作自定义参数，标志为确认后再次执行操作，避免重复显示交互提示
                      OperateOption operateOption = OperateOption.create();
                      operateOption.setVariableValue(OPPARAM_AFTERCONFIRM, "true");
                     
                      // 执行付款操作，并传入自定义操作参数
                      this.getView().invokeOperation(KEY_PAY, operateOption);
                 }
             }
        }
    }

__上一篇：beforeF7Select 事件

下一篇：afterDoOperation事件

 __

5.0 4人评分

内容反馈

*  __评论
收藏 29 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
