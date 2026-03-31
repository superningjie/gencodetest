# confirmCallBack事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238600539112877056&id=222757573626215424&type=Knowledge&productLineId=29&lang=zh-CN

## confirmCallBack事件

 __

[![金蝶云社区-lloam](https://vip.kingdee.com/download/010162cfa9d5437711e8b441060400ef5315.png)](javascript:;)

lloam

更新于 2024-04-22 15:02

浏览数： 1.4万 

#### 1 事件介绍

插件可以在此事件，了解用户的态度，决定后续业务逻辑。

说明：

1\. 必须在显示交互提示时，设置回调参数，才会触发此事件；

2\. 要注意避免重复显示相同交互信息，进入死循环。

  


#### 2 事件触发时机

用户确认了交互提示信息后，触发此事件，通知插件进行后续处理。

示例：

下面的代码显示一条交互提示，需要用户确认；用户确认后，即会触发confirCallBack事件。
    
    
    ConfirmCallBackListener confirmCallBacks = new ConfirmCallBackListener(CALLBACKID, this);
                    String confirmTip = "test confirm call back";
                    this.getView().showConfirm(confirmTip, MessageBoxOptions.OKCancel, ConfirmTypes.Default, confirmCallBacks);

  


3 代码模板 

### 3.1 代码示例  

    
    
    package kd.bos.plugin.sample.dynamicform.pcform.form.template;
    
    import kd.bos.dataentity.utils.StringUtils;
    import kd.bos.form.ConfirmCallBackListener;
    import kd.bos.form.ConfirmTypes;
    import kd.bos.form.MessageBoxOptions;
    import kd.bos.form.MessageBoxResult;
    import kd.bos.form.control.Button;
    import kd.bos.form.events.MessageBoxClosedEvent;
    import kd.bos.form.plugin.AbstractFormPlugin;
    
    import java.util.EventObject;
    
    /**
     * 动态表单，PC端界面事件代码模板
     * 
     * @author rd_johnnyding
     * @remark
     * confirmCallBack事件，在用户确认交互提示后触发
     */
    public class ConfirmCallBack extends AbstractFormPlugin {
    
    	// 按钮标识
    	public static final String CONFIRM_CALLBACKBTN_KEY = "confirmcallback";
    	// 回调标识
    	public static final String CALLBACKID = "contentChange";
    
    	@Override
    	public void initialize() {
    		// 添加按钮点击监听
    		this.addClickListeners(CONFIRM_CALLBACKBTN_KEY);
    	}
    
    	@Override
    	public void click(EventObject evt) {
    		Object source = evt.getSource();
    		if (source instanceof Button) {
    			Button btn = (Button) source;
    
    			// 设置回调
    			if (StringUtils.equals(btn.getKey(), CONFIRM_CALLBACKBTN_KEY)) {
    				ConfirmCallBackListener confirmCallBacks = new ConfirmCallBackListener(CALLBACKID, this);
    				// 设置回调提示
    				String confirmTip = "test confirm call back";
    				this.getView().showConfirm(confirmTip, MessageBoxOptions.OKCancel, ConfirmTypes.Default, confirmCallBacks);
    			}
    		}
    	}
    
    	@Override
    	public void confirmCallBack(MessageBoxClosedEvent messageBoxClosedEvent) {
    		// 回调标识正确，并且点击了确认
    		if (StringUtils.equals(messageBoxClosedEvent.getCallBackId(), CALLBACKID) && messageBoxClosedEvent.getResult() == MessageBoxResult.Yes) {
    			// todo 在此添加业务逻辑
    			this.getView().showSuccessNotification("confirm call back success");
    		}
    	}
    }

  


说明：  
常量**CALLBACKID** 需要根据实际场景进行替换。

### 3.2 执行效果

  * 设置回调确认->弹出确认弹框  





![](https://vip.kingdee.com/download/01095c6ccdd2f9eb4b6f846bf9734a5ef1e0.png)

  


  * 点击确定




![](https://vip.kingdee.com/download/010938908855328641a5ae0c17613e6b9bdf.png)

#### 4 参数说明

MessageBoxClosedEvent messageBoxClosedEvent：

  * MessageBoxResult getResult()：用户选择的确认结果，如Yes, No等；

  * String getCallBackId()：多处代码显示交互提示时，以此区分。




#### 5 应用示例

请参阅[beforeDoOperation](https://vip.kingdee.com/article/222748423013841920)事件的应用示例：

1\. 执行付款操作前，显示确认提示消息；

2\. 用户确认后，在confirmCallBack事件，重新执行付款操作。

 __上一篇：afterDoOperation事件

下一篇：closedCallBack事件

 __

4.6 5人评分

内容反馈

*  __评论
收藏 12 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
