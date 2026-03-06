# 通用控件——Ajax

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=218035603349614848&id=224162647648608256&type=Knowledge&productLineId=29&lang=zh-CN

## 通用控件——Ajax

 __

[![金蝶云社区-云社区用户didY8903](https://vip.kingdee.com/download/010162cfa9d6437711e8b441060400ef5315.png)](javascript:;)

云社区用户didY8903

更新于 2024-04-22 15:21

浏览数： 3,052 

一级标题

二级标题

三级标题

四级标题

五级标题

六级标题

添加图片链接

上传图片 
    
    
      
    # 1 功能介绍
    用于需要向通过客户端往第三方服务发送Ajax请求时的控件
    # 2 控件对象
    `kd.bos.form.control.ClientAjax`
    # 3 属性说明
    ## 3.1 通用属性
    >通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)
    
    ## 3.2 样式属性
    >样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)
    ## 3.3 业务属性
    
    | 属性名 | 类型 | 默认值 | 说明 |
    | --- | --- | --- | --- |
    | Ajax请求URL | 文本 | - | 设置请求的目标地址，可设计时配置，也可插件中动态设置 |
    
    # 4 Ajax接口示例
    
    * 插件设置ajax请求参数，并发送ajax请求
    
    ```java
    ClientAjax ajax = this.getView().getControl("控件标识");
    
    ClientAjaxOption option = new ClientAjaxOption();
    
    option.setUrl("url链接"); *// 请求链接*
    option.setAsync(true);  *// 是否异步*
    option.setData("数据");  *// 请求数据*
    option.setDataType("数据类型");  *// 数据类型*
    option.setType("请求类型");  *// 请求类型：get、post// ...*
    ajax.request(option); *// 发送指令*
    ```
    
    * 插件监听响应
    
    ```java
    public class TestPlugin extends AbstractFormPlugin implements ResponseListener {
      public void regiterListener(EventObject event) {
        ClientAjax ajax = this.getView().getControl("控件标识");
        ajax.addResponseListener(this); 
      }
      
      public void afterReceiveResponse(ResponseEvent event) {
        *// do something...*
      }
    }
    ```
    
     

# 1 功能介绍

用于需要向通过客户端往第三方服务发送Ajax请求时的控件

# 2 控件对象

`kd.bos.form.control.ClientAjax`

# 3 属性说明

## 3.1 通用属性

> 通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)

## 3.2 样式属性

> 样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)

## 3.3 业务属性

属性名 | 类型 | 默认值 | 说明  
---|---|---|---  
Ajax请求URL | 文本 | - | 设置请求的目标地址，可设计时配置，也可插件中动态设置  
  
# 4 Ajax接口示例

  * 插件设置ajax请求参数，并发送ajax请求


    
    
    ClientAjax ajax = this.getView().getControl("控件标识");
    
    ClientAjaxOption option = new ClientAjaxOption();
    
    option.setUrl("url链接"); *// 请求链接*
    option.setAsync(true);  *// 是否异步*
    option.setData("数据");  *// 请求数据*
    option.setDataType("数据类型");  *// 数据类型*
    option.setType("请求类型");  *// 请求类型：get、post// ...*
    ajax.request(option); *// 发送指令*
    

  * 插件监听响应


    
    
    public class TestPlugin extends AbstractFormPlugin implements ResponseListener {
      public void regiterListener(EventObject event) {
        ClientAjax ajax = this.getView().getControl("控件标识");
        ajax.addResponseListener(this); 
      }
      
      public void afterReceiveResponse(ResponseEvent event) {
        *// do something...*
      }
    }
    

<h1><a id="1__0"></a>1 功能介绍</h1> <p>用于需要向通过客户端往第三方服务发送Ajax请求时的控件</p> <h1><a id="2__2"></a>2 控件对象</h1> <p><code>kd.bos.form.control.ClientAjax</code></p> <h1><a id="3__4"></a>3 属性说明</h1> <h2><a id="31__5"></a>3.1 通用属性</h2> <blockquote> <p>通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考<a href="https://vip.kingdee.com/article/215559076720798976" target="_blank">通用属性</a></p> </blockquote> <h2><a id="32__8"></a>3.2 样式属性</h2> <blockquote> <p>样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考<a href="https://vip.kingdee.com/article/252017936767406336" target="_blank">样式属性</a></p> </blockquote> <h2><a id="33__10"></a>3.3 业务属性</h2> <table> <thead> <tr> <th>属性名</th> <th>类型</th> <th>默认值</th> <th>说明</th> </tr> </thead> <tbody> <tr> <td>Ajax请求URL</td> <td>文本</td> <td>-</td> <td>设置请求的目标地址，可设计时配置，也可插件中动态设置</td> </tr> </tbody> </table> <h1><a id="4_Ajax_16"></a>4 Ajax接口示例</h1> <ul> <li>插件设置ajax请求参数，并发送ajax请求</li> </ul> <pre><div class="hljs"><code class="lang-java">ClientAjax ajax = <span class="hljs-keyword">this</span>.getView().getControl(<span class="hljs-string">"控件标识"</span>); ClientAjaxOption option = <span class="hljs-keyword">new</span> ClientAjaxOption(); option.setUrl(<span class="hljs-string">"url链接"</span>); *<span class="hljs-comment">// 请求链接*</span> option.setAsync(<span class="hljs-keyword">true</span>); *<span class="hljs-comment">// 是否异步*</span> option.setData(<span class="hljs-string">"数据"</span>); *<span class="hljs-comment">// 请求数据*</span> option.setDataType(<span class="hljs-string">"数据类型"</span>); *<span class="hljs-comment">// 数据类型*</span> option.setType(<span class="hljs-string">"请求类型"</span>); *<span class="hljs-comment">// 请求类型：get、post// ...*</span> ajax.request(option); *<span class="hljs-comment">// 发送指令*</span> </code></div></pre> <ul> <li>插件监听响应</li> </ul> <pre><div class="hljs"><code class="lang-java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">TestPlugin</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">AbstractFormPlugin</span> <span class="hljs-keyword">implements</span> <span class="hljs-title">ResponseListener</span> </span>{ <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">regiterListener</span><span class="hljs-params">(EventObject event)</span> </span>{ ClientAjax ajax = <span class="hljs-keyword">this</span>.getView().getControl(<span class="hljs-string">"控件标识"</span>); ajax.addResponseListener(<span class="hljs-keyword">this</span>); } <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">afterReceiveResponse</span><span class="hljs-params">(ResponseEvent event)</span> </span>{ *<span class="hljs-comment">// do something...*</span> } } </code></div></pre>

导航目录 __

# 1 功能介绍

用于需要向通过客户端往第三方服务发送Ajax请求时的控件

# 2 控件对象

`kd.bos.form.control.ClientAjax`

# 3 属性说明

## 3.1 通用属性

> 通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)

## 3.2 样式属性

> 样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)

## 3.3 业务属性

属性名 | 类型 | 默认值 | 说明  
---|---|---|---  
Ajax请求URL | 文本 | - | 设置请求的目标地址，可设计时配置，也可插件中动态设置  
  
# 4 Ajax接口示例

  * 插件设置ajax请求参数，并发送ajax请求


    
    
    ClientAjax ajax = this.getView().getControl("控件标识");
    
    ClientAjaxOption option = new ClientAjaxOption();
    
    option.setUrl("url链接"); *// 请求链接*
    option.setAsync(true);  *// 是否异步*
    option.setData("数据");  *// 请求数据*
    option.setDataType("数据类型");  *// 数据类型*
    option.setType("请求类型");  *// 请求类型：get、post// ...*
    ajax.request(option); *// 发送指令*
    

  * 插件监听响应


    
    
    public class TestPlugin extends AbstractFormPlugin implements ResponseListener {
      public void regiterListener(EventObject event) {
        ClientAjax ajax = this.getView().getControl("控件标识");
        ajax.addResponseListener(this); 
      }
      
      public void afterReceiveResponse(ResponseEvent event) {
        *// do something...*
      }
    }
    

__上一篇：IFrame

下一篇：HTML控件

 __

5.0 1人评分

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
