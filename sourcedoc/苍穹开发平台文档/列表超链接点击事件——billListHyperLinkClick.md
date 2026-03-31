# 列表超链接点击事件——billListHyperLinkClick

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238607792339561472&id=224170381928364800&type=Knowledge&productLineId=29&lang=zh-CN

## 列表超链接点击事件——billListHyperLinkClick

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:02

浏览数： 1.1万 

一级标题

二级标题

三级标题

四级标题

五级标题

六级标题

添加图片链接

上传图片 
    
    
      
    # billListHyperLinkClick事件
    
    ## 1. 事件介绍
    
    插件可以在此事件，对列表超链接点击事件进行干预，如取消单据界面打开，自行显示其它界面。
    
    
    
    ## 2. 事件触发时机
    
    单据列表上显示为超链接的单元格，用户点击时，系统会默认打开单据维护界面。
    
    在打开单据界面之前，触发此事件。
    
    
    
    ## 3. 代码模板
    
    该事件定义在接口IFormPlugin下，插件抽象基类 kd.bos.form.plugin.AbstractFormPlugin 对接口进行了实现，因此可基于此抽象基类对插件进行定义。
    
    例：AbstractListPlugin 为PC端列表插件基类 该类继承于AbstractFormPlugin。 
    
    ```java
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.form.events.HyperLinkClickArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillListHyperLinkClick extends AbstractListPlugin {
        @Override
        public void billListHyperLinkClick(HyperLinkClickArgs args) {
             // TODO 在此添加业务逻辑
        }
    }
    ```
    
    
    
    ## 4. 事件参数
    
    **public** **class** HyperLinkClickArgs
    
    - **public** **void** setCancel(**boolean** isCancel)：取消后续处理；
    
    - **public** HyperLinkClickEvent getHyperLinkClickEvent()；
    
      **public** Object getSource()：事件源，单据列表控件BillList；
    
      **public** String getFieldName()：列名；
    
      **public** **int** getPageIndex()：页码；
    
      **public** **int** getRowIndex()：行号；
    
      **public** DynamicObject getRowData()：行数据。可以据此获取到当前单元格的内容，从而决定自行打开的子界面参数。
    
     
    
    ## 5. 应用示例
    
    ### 5.1 案例说明
    
    1. 列表上，单据编号显示为超链接；
    
    2. 点击单据编号超链接，取消打开单据本身的编辑界面；
    3. 打开自定义页面。
    
    ![image.png](/download/0100a5966d7fd28d4e51864610f9f87aa93a.png)
    
    ### 5.2 实例代码
    
    ```java
    package kd.bos.plugin.sample.bill.list.bizcase;
     
    import kd.bos.bill.BillShowParameter;
    import kd.bos.bill.OperationStatus;
    import kd.bos.dataentity.utils.StringUtils;
    import kd.bos.form.ShowType;
    import kd.bos.form.events.HyperLinkClickArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillListHyperLinkClickSample extends AbstractListPlugin {
     
        private final static String NUMBER = "number";
     
        /**
         * 用户点击超链接单元格时，触发此事件
         */
        @Override
        public void billListHyperLinkClick(HyperLinkClickArgs args) {
            String fieldName = args.getFieldName();
            if (NUMBER.equals(fieldName)) {
                // 超链接点击为编码时取消原逻辑 跳转至自定义页面
                showCustomForm();
                args.setCancel(true);
            }
        }
        
        private void showCustomForm() {
            ListSelectedRow currentRow = ((ListView) this.getView()).getCurrentSelectedRowInfo();
            FormShowParameter parameter = new FormShowParameter();
            String formId = "bill_key" // 自定义表单标识
            Map<String, Object> customParams = new HashMap<>();
            // 传入自定义参数
            customParams.put("pk", currentRow.getPrimaryKeyValue());
            customParams.put("number", currentRow.getNumber());
            parameter.setCustomParams(customParams);
            parameter.setFormId(formId);
            // 设置打开界面方式
            parameter.getOpenStyle().setShowType(ShowType.NewWindow);
            this.getView().showForm(parameter);
        }
    }
    `` 

# billListHyperLinkClick事件

## 1\. 事件介绍

插件可以在此事件，对列表超链接点击事件进行干预，如取消单据界面打开，自行显示其它界面。

## 2\. 事件触发时机

单据列表上显示为超链接的单元格，用户点击时，系统会默认打开单据维护界面。

在打开单据界面之前，触发此事件。

## 3\. 代码模板

该事件定义在接口IFormPlugin下，插件抽象基类 kd.bos.form.plugin.AbstractFormPlugin 对接口进行了实现，因此可基于此抽象基类对插件进行定义。

例：AbstractListPlugin 为PC端列表插件基类 该类继承于AbstractFormPlugin。
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.form.events.HyperLinkClickArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillListHyperLinkClick extends AbstractListPlugin {
        @Override
        public void billListHyperLinkClick(HyperLinkClickArgs args) {
             // TODO 在此添加业务逻辑
        }
    }
    

## 4\. 事件参数

**public** **class** HyperLinkClickArgs

  * **public** **void** setCancel(**boolean** isCancel)：取消后续处理；

  * **public** HyperLinkClickEvent getHyperLinkClickEvent()；

**public** Object getSource()：事件源，单据列表控件BillList；

**public** String getFieldName()：列名；

**public** **int** getPageIndex()：页码；

**public** **int** getRowIndex()：行号；

**public** DynamicObject getRowData()：行数据。可以据此获取到当前单元格的内容，从而决定自行打开的子界面参数。




## 5\. 应用示例

### 5.1 案例说明

  1. 列表上，单据编号显示为超链接；

  2. 点击单据编号超链接，取消打开单据本身的编辑界面；

  3. 打开自定义页面。




![image.png](https://vip.kingdee.com/download/0100a5966d7fd28d4e51864610f9f87aa93a.png)

### 5.2 实例代码
    
    
    package kd.bos.plugin.sample.bill.list.bizcase;
     
    import kd.bos.bill.BillShowParameter;
    import kd.bos.bill.OperationStatus;
    import kd.bos.dataentity.utils.StringUtils;
    import kd.bos.form.ShowType;
    import kd.bos.form.events.HyperLinkClickArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillListHyperLinkClickSample extends AbstractListPlugin {
     
        private final static String NUMBER = "number";
     
        /**
         * 用户点击超链接单元格时，触发此事件
         */
        @Override
        public void billListHyperLinkClick(HyperLinkClickArgs args) {
            String fieldName = args.getFieldName();
            if (NUMBER.equals(fieldName)) {
                // 超链接点击为编码时取消原逻辑 跳转至自定义页面
                showCustomForm();
                args.setCancel(true);
            }
        }
        
        private void showCustomForm() {
            ListSelectedRow currentRow = ((ListView) this.getView()).getCurrentSelectedRowInfo();
            FormShowParameter parameter = new FormShowParameter();
            String formId = "bill_key" // 自定义表单标识
            Map<String, Object> customParams = new HashMap<>();
            // 传入自定义参数
            customParams.put("pk", currentRow.getPrimaryKeyValue());
            customParams.put("number", currentRow.getNumber());
            parameter.setCustomParams(customParams);
            parameter.setFormId(formId);
            // 设置打开界面方式
            parameter.getOpenStyle().setShowType(ShowType.NewWindow);
            this.getView().showForm(parameter);
        }
    }
    ``

<h1><a id="billListHyperLinkClick_0"></a>billListHyperLinkClick事件</h1> <h2><a id="1__2"></a>1\. 事件介绍</h2> <p>插件可以在此事件，对列表超链接点击事件进行干预，如取消单据界面打开，自行显示其它界面。</p> <h2><a id="2__8"></a>2\. 事件触发时机</h2> <p>单据列表上显示为超链接的单元格，用户点击时，系统会默认打开单据维护界面。</p> <p>在打开单据界面之前，触发此事件。</p> <h2><a id="3__16"></a>3\. 代码模板</h2> <p>该事件定义在接口IFormPlugin下，插件抽象基类 kd.bos.form.plugin.AbstractFormPlugin 对接口进行了实现，因此可基于此抽象基类对插件进行定义。</p> <p>例：AbstractListPlugin 为PC端列表插件基类 该类继承于AbstractFormPlugin。</p> <pre><div class="hljs"><code class="lang-java"><span class="hljs-keyword">package</span> kd.bos.plugin.sample.bill.list.template; <span class="hljs-keyword">import</span> kd.bos.form.events.HyperLinkClickArgs; <span class="hljs-keyword">import</span> kd.bos.list.plugin.AbstractListPlugin; <span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">BillListHyperLinkClick</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">AbstractListPlugin</span> </span>{ <span class="hljs-meta">@Override</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">billListHyperLinkClick</span><span class="hljs-params">(HyperLinkClickArgs args)</span> </span>{ <span class="hljs-comment">// TODO 在此添加业务逻辑</span> } } </code></div></pre> <h2><a id="4__38"></a>4\. 事件参数</h2> <p><strong>public</strong> <strong>class</strong> HyperLinkClickArgs</p> <ul> <li> <p><strong>public</strong> <strong>void</strong> setCancel(<strong>boolean</strong> isCancel)：取消后续处理；</p> </li> <li> <p><strong>public</strong> HyperLinkClickEvent getHyperLinkClickEvent()；</p> <p><strong>public</strong> Object getSource()：事件源，单据列表控件BillList；</p> <p><strong>public</strong> String getFieldName()：列名；</p> <p><strong>public</strong> <strong>int</strong> getPageIndex()：页码；</p> <p><strong>public</strong> <strong>int</strong> getRowIndex()：行号；</p> <p><strong>public</strong> DynamicObject getRowData()：行数据。可以据此获取到当前单元格的内容，从而决定自行打开的子界面参数。</p> </li> </ul> <h2><a id="5__58"></a>5\. 应用示例</h2> <h3><a id="51__60"></a>5.1 案例说明</h3> <ol> <li> <p>列表上，单据编号显示为超链接；</p> </li> <li> <p>点击单据编号超链接，取消打开单据本身的编辑界面；</p> </li> <li> <p>打开自定义页面。</p> </li> </ol> <p><img src="/download/0100a5966d7fd28d4e51864610f9f87aa93a.png" alt="image.png" /></p> <h3><a id="52__69"></a>5.2 实例代码</h3> <pre><div class="hljs"><code class="lang-java"><span class="hljs-keyword">package</span> kd.bos.plugin.sample.bill.list.bizcase; <span class="hljs-keyword">import</span> kd.bos.bill.BillShowParameter; <span class="hljs-keyword">import</span> kd.bos.bill.OperationStatus; <span class="hljs-keyword">import</span> kd.bos.dataentity.utils.StringUtils; <span class="hljs-keyword">import</span> kd.bos.form.ShowType; <span class="hljs-keyword">import</span> kd.bos.form.events.HyperLinkClickArgs; <span class="hljs-keyword">import</span> kd.bos.list.plugin.AbstractListPlugin; <span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">BillListHyperLinkClickSample</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">AbstractListPlugin</span> </span>{ <span class="hljs-keyword">private</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">static</span> String NUMBER = <span class="hljs-string">"number"</span>; <span class="hljs-comment">/** * 用户点击超链接单元格时，触发此事件 */</span> <span class="hljs-meta">@Override</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">billListHyperLinkClick</span><span class="hljs-params">(HyperLinkClickArgs args)</span> </span>{ String fieldName = args.getFieldName(); <span class="hljs-keyword">if</span> (NUMBER.equals(fieldName)) { <span class="hljs-comment">// 超链接点击为编码时取消原逻辑 跳转至自定义页面</span> showCustomForm(); args.setCancel(<span class="hljs-keyword">true</span>); } } <span class="hljs-function"><span class="hljs-keyword">private</span> <span class="hljs-keyword">void</span> <span class="hljs-title">showCustomForm</span><span class="hljs-params">()</span> </span>{ ListSelectedRow currentRow = ((ListView) <span class="hljs-keyword">this</span>.getView()).getCurrentSelectedRowInfo(); FormShowParameter parameter = <span class="hljs-keyword">new</span> FormShowParameter(); String formId = <span class="hljs-string">"bill_key"</span> <span class="hljs-comment">// 自定义表单标识</span> Map&lt;String, Object&gt; customParams = <span class="hljs-keyword">new</span> HashMap&lt;&gt;(); <span class="hljs-comment">// 传入自定义参数</span> customParams.put(<span class="hljs-string">"pk"</span>, currentRow.getPrimaryKeyValue()); customParams.put(<span class="hljs-string">"number"</span>, currentRow.getNumber()); parameter.setCustomParams(customParams); parameter.setFormId(formId); <span class="hljs-comment">// 设置打开界面方式</span> parameter.getOpenStyle().setShowType(ShowType.NewWindow); <span class="hljs-keyword">this</span>.getView().showForm(parameter); } } ``</code></div></pre>

导航目录 __

# billListHyperLinkClick事件

## 1\. 事件介绍

插件可以在此事件，对列表超链接点击事件进行干预，如取消单据界面打开，自行显示其它界面。

## 2\. 事件触发时机

单据列表上显示为超链接的单元格，用户点击时，系统会默认打开单据维护界面。

在打开单据界面之前，触发此事件。

## 3\. 代码模板

该事件定义在接口IFormPlugin下，插件抽象基类 kd.bos.form.plugin.AbstractFormPlugin 对接口进行了实现，因此可基于此抽象基类对插件进行定义。

例：AbstractListPlugin 为PC端列表插件基类 该类继承于AbstractFormPlugin。
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.form.events.HyperLinkClickArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillListHyperLinkClick extends AbstractListPlugin {
        @Override
        public void billListHyperLinkClick(HyperLinkClickArgs args) {
             // TODO 在此添加业务逻辑
        }
    }
    

## 4\. 事件参数

**public** **class** HyperLinkClickArgs

  * **public** **void** setCancel(**boolean** isCancel)：取消后续处理；

  * **public** HyperLinkClickEvent getHyperLinkClickEvent()；

**public** Object getSource()：事件源，单据列表控件BillList；

**public** String getFieldName()：列名；

**public** **int** getPageIndex()：页码；

**public** **int** getRowIndex()：行号；

**public** DynamicObject getRowData()：行数据。可以据此获取到当前单元格的内容，从而决定自行打开的子界面参数。




## 5\. 应用示例

### 5.1 案例说明

  1. 列表上，单据编号显示为超链接；

  2. 点击单据编号超链接，取消打开单据本身的编辑界面；

  3. 打开自定义页面。




![image.png](https://vip.kingdee.com/download/0100a5966d7fd28d4e51864610f9f87aa93a.png)

### 5.2 实例代码
    
    
    package kd.bos.plugin.sample.bill.list.bizcase;
     
    import kd.bos.bill.BillShowParameter;
    import kd.bos.bill.OperationStatus;
    import kd.bos.dataentity.utils.StringUtils;
    import kd.bos.form.ShowType;
    import kd.bos.form.events.HyperLinkClickArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class BillListHyperLinkClickSample extends AbstractListPlugin {
     
        private final static String NUMBER = "number";
     
        /**
         * 用户点击超链接单元格时，触发此事件
         */
        @Override
        public void billListHyperLinkClick(HyperLinkClickArgs args) {
            String fieldName = args.getFieldName();
            if (NUMBER.equals(fieldName)) {
                // 超链接点击为编码时取消原逻辑 跳转至自定义页面
                showCustomForm();
                args.setCancel(true);
            }
        }
        
        private void showCustomForm() {
            ListSelectedRow currentRow = ((ListView) this.getView()).getCurrentSelectedRowInfo();
            FormShowParameter parameter = new FormShowParameter();
            String formId = "bill_key" // 自定义表单标识
            Map<String, Object> customParams = new HashMap<>();
            // 传入自定义参数
            customParams.put("pk", currentRow.getPrimaryKeyValue());
            customParams.put("number", currentRow.getNumber());
            parameter.setCustomParams(customParams);
            parameter.setFormId(formId);
            // 设置打开界面方式
            parameter.getOpenStyle().setShowType(ShowType.NewWindow);
            this.getView().showForm(parameter);
        }
    }
    ``

__上一篇：itemClick事件

下一篇：beforeShowBill 事件

 __

4.0 4人评分

内容反馈

*  __评论
收藏 22 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
