# afterOutputElement事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238613466461055488&id=225930754470445568&type=Knowledge&productLineId=29&lang=zh-CN

## afterOutputElement事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-18 11:46

浏览数： 4,768 

1 事件介绍

打印引擎对设计的控件进行解析并生成绘制对象后触发。  


  


2 代码模板
    
    
    @Override
    public void afterOuputElement(OutputElementArgs e){
        if( "text1".equals(e.getKey( ))){
            //Do something
        }
    }

  


3 参数说明

**public** **class** OutputElementArgs **extends** EventObject

  * **public** String getKey()：获取控件标识；

  * **public** String getCurrentDataSource()：获取控件绑定数据源；

  * **public** IPrintScriptable getOutput()：获取控件输出对象。




  


4 应用示例

4.1 案例说明

1、标识为”text”的控件绑定了一个布尔的字段，需要展示位是/否；

2、标识为”text1” 的控件需要输出为页码；

3、标识为”text2” 的控件设计时绑定为fielda，现需要输出为fieldb的值。

  


4.2 代码实例
    
    
    @override
    public void afterOutputElement(OutputElementArgs e) {
            if (e.getKey().equals( "text")){
                IPrintscriptable apw = e.getOutput();
                Object value = apw.getFieldValue( "fieldb");
                if ( "true".equals(value)) {
                    apw.setValue("是");
                }else{
                apw.setValue("否");
                }
            }else if (e.getKey( ).equals( "text1")) {
                /*获取页码并赋值给text1控件*/
                IPrintscriptableapw =e.getOutput();
                int pageNumber = apw.getPageNumber( );
                apw.setValue(pageNumber);
            }else if (e.getKey().equals( "text2")){
                /*获取同一数据源下绑定的字段为fjis.ldb的值并赋值给text2控件*/
                IPrintscriptableapw = e.getOutput();
                Object newValue = apw.getFieldValue( "fieldb");
                apw.setValue(newValue);
            }
    }

  


__上一篇：beforeOuputElement事件

下一篇：反写插件手册

 __

暂无评分

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
