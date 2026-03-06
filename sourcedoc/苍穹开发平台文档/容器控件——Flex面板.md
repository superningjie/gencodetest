# 容器控件——Flex面板

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=218035632357421312&id=226621396334707456&type=Knowledge&productLineId=29&lang=zh-CN

## 容器控件——Flex面板

 __

[![金蝶云社区-逮虾的虎](https://vip.kingdee.com/download/01011d85de89730d4f439daeb640456f5717.png)](javascript:;)

逮虾的虎

更新于 2025-03-11 13:55

浏览数： 1.3万 

一级标题

二级标题

三级标题

四级标题

五级标题

六级标题

添加图片链接

上传图片 
    
    
      
    # 变更记录
    
    | 产品版本 | 更新内容 | 更新日期 |
    | --- | --- | --- |
    | V7.0.7 | 支持设置容器折叠摘要常显，详见5 | 2024-03-11 |
    
    
    # 1 功能介绍
    Flex面板是具有弹性布局特性的容器控件。
    # 2 控件对象
    `kd.bos.form.container.Container`
    # 3 视觉效果
    pc端：
    ![1.png](/download/0100513abd05ef784ab0a8db2ff60da31d2b.png)
    移动端：
    ![2.png](/download/01002b2072d700d64e54bd56a8007354e42c.png)
    # 4 属性介绍
    ## 4.1 通用属性
    >通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)
    ## 4.2 样式属性
    >样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)
    ## 4.3 业务属性
    
    | 属性名 | 类型 | 默认值 | 说明 | PC | Mobile |
    | --- | --- | --- | --- | --- | --- |
    | 可折叠 | 复选框 | false | 设置面板是否可折叠 | V4.0及以上 | V4.0及以上 |
    | 默认折叠 | 复选框 | false | 设置面板默认的折叠状态 | V4.0及以上 | V4.0及以上 |
    | 折叠摘要 | 弹框选择 | - | 面板折叠情况下显示已配置的折叠摘要信息 | V4.0及以上 | V4.0及以上 |
    | 允许点击 | 复选框 | fasle | 点击面板发送click请求 | V4.0及以上 | V4.0及以上 |
    | 允许全屏 | 复选框 | false | 允许flex面板全屏 | V4.0及以上 | 不支持 |
    | 按需加载 | 复选框 | true | 设置按需加载控件 | V6.0及以上 | 不支持 |
    | 帮助文本 | 弹框选择 | - | 设置面板帮助文本信息 | V6.0及以上 | 不支持 |
    | 标题背景色 | 颜色选项 | - | 设置面板标题背景色 | 不支持 | V4.0及以上 |
    | 隐藏标题 | 复选框 | false | 允许隐藏面板标题 | 不支持 | V4.0及以上 |
    
    
    # 5 Flex面板接口介绍
    
    * 动态往容器中插入子控件
    
    ```java
    Container flexPanel = this.getView().getControl("Flex面板标识");
    
    ButtonAp button = new ButtonAp();
    button.setKey("testkey");
    button.setName(new LocalString("动态添加按钮"));
    
    flexPanel.insertControls(index, Arrays.asList(button.createControl()));
    
    ```
    
    * 动态删除容器中的子控件
    
    ```java
    Container flexPanel = this.getView().getControl("Flex面板标识");
    String[] keys = new String[]{"控件标识1", "控件标识2"};*// ...*
    flexPanel.deleteControls(keys);
    
    ```
    
    * 设置Flex面板折叠/展开
    
    ```java
    Container flexPanel = this.getView().getControl("Flex面板标识");*// 设置折叠*
    flexPanel.setCollapse(true);
    *// 展开*
    flexPanel.setCollapse(false);
    
    ```
    
    * 动态设置容器背景图片
    
    ```java
    Container flexPanel = this.getView().getControl("Flex面板标识");
    String url = "";
    flexPanel.setBackgroundImg(url);
    
    ```
    
    * 设置容器摘要常显
    
    ```java
    
    Map<String, Object> map = new HashMap<>();
    map.put("showAbstract", true);
    this.getView().updateControlMetadata("Flex面板标识", map);
    
    
     

# 变更记录

产品版本 | 更新内容 | 更新日期  
---|---|---  
V7.0.7 | 支持设置容器折叠摘要常显，详见5 | 2024-03-11  
  
# 1 功能介绍

Flex面板是具有弹性布局特性的容器控件。

# 2 控件对象

`kd.bos.form.container.Container`

# 3 视觉效果

pc端：  
![1.png](https://vip.kingdee.com/download/0100513abd05ef784ab0a8db2ff60da31d2b.png)  
移动端：  
![2.png](https://vip.kingdee.com/download/01002b2072d700d64e54bd56a8007354e42c.png)

# 4 属性介绍

## 4.1 通用属性

> 通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)

## 4.2 样式属性

> 样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)

## 4.3 业务属性

属性名 | 类型 | 默认值 | 说明 | PC | Mobile  
---|---|---|---|---|---  
可折叠 | 复选框 | false | 设置面板是否可折叠 | V4.0及以上 | V4.0及以上  
默认折叠 | 复选框 | false | 设置面板默认的折叠状态 | V4.0及以上 | V4.0及以上  
折叠摘要 | 弹框选择 | - | 面板折叠情况下显示已配置的折叠摘要信息 | V4.0及以上 | V4.0及以上  
允许点击 | 复选框 | fasle | 点击面板发送click请求 | V4.0及以上 | V4.0及以上  
允许全屏 | 复选框 | false | 允许flex面板全屏 | V4.0及以上 | 不支持  
按需加载 | 复选框 | true | 设置按需加载控件 | V6.0及以上 | 不支持  
帮助文本 | 弹框选择 | - | 设置面板帮助文本信息 | V6.0及以上 | 不支持  
标题背景色 | 颜色选项 | - | 设置面板标题背景色 | 不支持 | V4.0及以上  
隐藏标题 | 复选框 | false | 允许隐藏面板标题 | 不支持 | V4.0及以上  
  
# 5 Flex面板接口介绍

  * 动态往容器中插入子控件


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");
    
    ButtonAp button = new ButtonAp();
    button.setKey("testkey");
    button.setName(new LocalString("动态添加按钮"));
    
    flexPanel.insertControls(index, Arrays.asList(button.createControl()));
    
    

  * 动态删除容器中的子控件


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");
    String[] keys = new String[]{"控件标识1", "控件标识2"};*// ...*
    flexPanel.deleteControls(keys);
    
    

  * 设置Flex面板折叠/展开


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");*// 设置折叠*
    flexPanel.setCollapse(true);
    *// 展开*
    flexPanel.setCollapse(false);
    
    

  * 动态设置容器背景图片


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");
    String url = "";
    flexPanel.setBackgroundImg(url);
    
    

  * 设置容器摘要常显


    
    
    Map<String, Object> map = new HashMap<>();
    map.put("showAbstract", true);
    this.getView().updateControlMetadata("Flex面板标识", map);
    
    
    

<h1><a id="_0"></a>变更记录</h1> <table> <thead> <tr> <th>产品版本</th> <th>更新内容</th> <th>更新日期</th> </tr> </thead> <tbody> <tr> <td>V7.0.7</td> <td>支持设置容器折叠摘要常显，详见5</td> <td>2024-03-11</td> </tr> </tbody> </table> <h1><a id="1__7"></a>1 功能介绍</h1> <p>Flex面板是具有弹性布局特性的容器控件。</p> <h1><a id="2__9"></a>2 控件对象</h1> <p><code>kd.bos.form.container.Container</code></p> <h1><a id="3__11"></a>3 视觉效果</h1> <p>pc端：<br /> <img src="/download/0100513abd05ef784ab0a8db2ff60da31d2b.png" alt="1.png" /><br /> 移动端：<br /> <img src="/download/01002b2072d700d64e54bd56a8007354e42c.png" alt="2.png" /></p> <h1><a id="4__16"></a>4 属性介绍</h1> <h2><a id="41__17"></a>4.1 通用属性</h2> <blockquote> <p>通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考<a href="https://vip.kingdee.com/article/215559076720798976" target="_blank">通用属性</a></p> </blockquote> <h2><a id="42__19"></a>4.2 样式属性</h2> <blockquote> <p>样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考<a href="https://vip.kingdee.com/article/252017936767406336" target="_blank">样式属性</a></p> </blockquote> <h2><a id="43__21"></a>4.3 业务属性</h2> <table> <thead> <tr> <th>属性名</th> <th>类型</th> <th>默认值</th> <th>说明</th> <th>PC</th> <th>Mobile</th> </tr> </thead> <tbody> <tr> <td>可折叠</td> <td>复选框</td> <td>false</td> <td>设置面板是否可折叠</td> <td>V4.0及以上</td> <td>V4.0及以上</td> </tr> <tr> <td>默认折叠</td> <td>复选框</td> <td>false</td> <td>设置面板默认的折叠状态</td> <td>V4.0及以上</td> <td>V4.0及以上</td> </tr> <tr> <td>折叠摘要</td> <td>弹框选择</td> <td>-</td> <td>面板折叠情况下显示已配置的折叠摘要信息</td> <td>V4.0及以上</td> <td>V4.0及以上</td> </tr> <tr> <td>允许点击</td> <td>复选框</td> <td>fasle</td> <td>点击面板发送click请求</td> <td>V4.0及以上</td> <td>V4.0及以上</td> </tr> <tr> <td>允许全屏</td> <td>复选框</td> <td>false</td> <td>允许flex面板全屏</td> <td>V4.0及以上</td> <td>不支持</td> </tr> <tr> <td>按需加载</td> <td>复选框</td> <td>true</td> <td>设置按需加载控件</td> <td>V6.0及以上</td> <td>不支持</td> </tr> <tr> <td>帮助文本</td> <td>弹框选择</td> <td>-</td> <td>设置面板帮助文本信息</td> <td>V6.0及以上</td> <td>不支持</td> </tr> <tr> <td>标题背景色</td> <td>颜色选项</td> <td>-</td> <td>设置面板标题背景色</td> <td>不支持</td> <td>V4.0及以上</td> </tr> <tr> <td>隐藏标题</td> <td>复选框</td> <td>false</td> <td>允许隐藏面板标题</td> <td>不支持</td> <td>V4.0及以上</td> </tr> </tbody> </table> <h1><a id="5_Flex_36"></a>5 Flex面板接口介绍</h1> <ul> <li>动态往容器中插入子控件</li> </ul> <pre><div class="hljs"><code class="lang-java">Container flexPanel = <span class="hljs-keyword">this</span>.getView().getControl(<span class="hljs-string">"Flex面板标识"</span>); ButtonAp button = <span class="hljs-keyword">new</span> ButtonAp(); button.setKey(<span class="hljs-string">"testkey"</span>); button.setName(<span class="hljs-keyword">new</span> LocalString(<span class="hljs-string">"动态添加按钮"</span>)); flexPanel.insertControls(index, Arrays.asList(button.createControl())); </code></div></pre> <ul> <li>动态删除容器中的子控件</li> </ul> <pre><div class="hljs"><code class="lang-java">Container flexPanel = <span class="hljs-keyword">this</span>.getView().getControl(<span class="hljs-string">"Flex面板标识"</span>); String[] keys = <span class="hljs-keyword">new</span> String[]{<span class="hljs-string">"控件标识1"</span>, <span class="hljs-string">"控件标识2"</span>};*<span class="hljs-comment">// ...*</span> flexPanel.deleteControls(keys); </code></div></pre> <ul> <li>设置Flex面板折叠/展开</li> </ul> <pre><div class="hljs"><code class="lang-java">Container flexPanel = <span class="hljs-keyword">this</span>.getView().getControl(<span class="hljs-string">"Flex面板标识"</span>);*<span class="hljs-comment">// 设置折叠*</span> flexPanel.setCollapse(<span class="hljs-keyword">true</span>); *<span class="hljs-comment">// 展开*</span> flexPanel.setCollapse(<span class="hljs-keyword">false</span>); </code></div></pre> <ul> <li>动态设置容器背景图片</li> </ul> <pre><div class="hljs"><code class="lang-java">Container flexPanel = <span class="hljs-keyword">this</span>.getView().getControl(<span class="hljs-string">"Flex面板标识"</span>); String url = <span class="hljs-string">""</span>; flexPanel.setBackgroundImg(url); </code></div></pre> <ul> <li>设置容器摘要常显</li> </ul> <pre><div class="hljs"><code class="lang-java"> Map&lt;String, Object&gt; map = <span class="hljs-keyword">new</span> HashMap&lt;&gt;(); map.put(<span class="hljs-string">"showAbstract"</span>, <span class="hljs-keyword">true</span>); <span class="hljs-keyword">this</span>.getView().updateControlMetadata(<span class="hljs-string">"Flex面板标识"</span>, map); </code></div></pre>

导航目录 __

# 变更记录

产品版本 | 更新内容 | 更新日期  
---|---|---  
V7.0.7 | 支持设置容器折叠摘要常显，详见5 | 2024-03-11  
  
# 1 功能介绍

Flex面板是具有弹性布局特性的容器控件。

# 2 控件对象

`kd.bos.form.container.Container`

# 3 视觉效果

pc端：  
![1.png](https://vip.kingdee.com/download/0100513abd05ef784ab0a8db2ff60da31d2b.png)  
移动端：  
![2.png](https://vip.kingdee.com/download/01002b2072d700d64e54bd56a8007354e42c.png)

# 4 属性介绍

## 4.1 通用属性

> 通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)

## 4.2 样式属性

> 样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)

## 4.3 业务属性

属性名 | 类型 | 默认值 | 说明 | PC | Mobile  
---|---|---|---|---|---  
可折叠 | 复选框 | false | 设置面板是否可折叠 | V4.0及以上 | V4.0及以上  
默认折叠 | 复选框 | false | 设置面板默认的折叠状态 | V4.0及以上 | V4.0及以上  
折叠摘要 | 弹框选择 | - | 面板折叠情况下显示已配置的折叠摘要信息 | V4.0及以上 | V4.0及以上  
允许点击 | 复选框 | fasle | 点击面板发送click请求 | V4.0及以上 | V4.0及以上  
允许全屏 | 复选框 | false | 允许flex面板全屏 | V4.0及以上 | 不支持  
按需加载 | 复选框 | true | 设置按需加载控件 | V6.0及以上 | 不支持  
帮助文本 | 弹框选择 | - | 设置面板帮助文本信息 | V6.0及以上 | 不支持  
标题背景色 | 颜色选项 | - | 设置面板标题背景色 | 不支持 | V4.0及以上  
隐藏标题 | 复选框 | false | 允许隐藏面板标题 | 不支持 | V4.0及以上  
  
# 5 Flex面板接口介绍

  * 动态往容器中插入子控件


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");
    
    ButtonAp button = new ButtonAp();
    button.setKey("testkey");
    button.setName(new LocalString("动态添加按钮"));
    
    flexPanel.insertControls(index, Arrays.asList(button.createControl()));
    
    

  * 动态删除容器中的子控件


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");
    String[] keys = new String[]{"控件标识1", "控件标识2"};*// ...*
    flexPanel.deleteControls(keys);
    
    

  * 设置Flex面板折叠/展开


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");*// 设置折叠*
    flexPanel.setCollapse(true);
    *// 展开*
    flexPanel.setCollapse(false);
    
    

  * 动态设置容器背景图片


    
    
    Container flexPanel = this.getView().getControl("Flex面板标识");
    String url = "";
    flexPanel.setBackgroundImg(url);
    
    

  * 设置容器摘要常显


    
    
    Map<String, Object> map = new HashMap<>();
    map.put("showAbstract", true);
    this.getView().updateControlMetadata("Flex面板标识", map);
    
    
    

__上一篇：通用过滤

下一篇：高级面板

 __

5.0 4人评分

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
