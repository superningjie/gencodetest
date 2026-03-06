# beforeBuildRowCondition事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612670079680256&id=225657245114565120&type=Knowledge&productLineId=29&lang=zh-CN

## beforeBuildRowCondition事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:02

浏览数： 4,959 

1 事件介绍

插件可以在此事件，忽略转换规则上配置的条件，改用插件定制条件，或者追加插件定制条件。

  


2 事件触发时机

编译转换规则 - 数据范围，配置的源单数据筛选条件之前，触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.billconvert.template;
     
    import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
    import kd.bos.entity.botp.plugin.args.BeforeBuildRowConditionEventArgs;
     
    public class BeforeBuildRowCondition extends AbstractConvertPlugIn {
     
        @Override
        public void beforeBuildRowCondition(BeforeBuildRowConditionEventArgs e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** BeforeBuildRowConditionEventArgs **extends** ConvertPluginEventArgs

  * **public** **void** setIgnoreRuleFilterPolicy(**boolean** ignoreRuleFilterPolicy)：完全忽略转换规则上的数据范围；

  * **public** List<QFilter> getCustQFilters()：插件定制条件，用于数据库取数；

  * **public** **void** setCustFilterExpression(String custFilterExpression)：插件定制条件表达式，用于内存运算；条件含义需要与CustQFilters一致，系统会分别用于不同的时机点；

  * **public** **void** setCustFilterDesc(String custFilterDesc)：插件定制条件描述，当源单数据行，不符合此条件时，系统会把这段条件描述，提示给用户，告诉用户数据行不允许下推的原因。




  


5 应用示例

5.1 案例说明

1\. 单据下推，需要根据系统选项，动态增加数据筛选条件：

  * 如果勾选了"锁定的订单不允许下推"选项，则不能下推锁定状态为已锁定(B)的单据；

  * 如果没有勾选此选项，则不做此限制。




  


5.2 实现方案

1\. 捕捉 beforeBuildRowCondition 事件，读取选项值；

2\. 如果勾选了选项，则追加数据行筛选条件，只允许下推未锁定的单据。

  


5.3 实例代码
    
    
    package kd.bos.plugin.sample.bill.billconvert.bizcase;
     
    import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
    import kd.bos.entity.botp.plugin.args.BeforeBuildRowConditionEventArgs;
    import kd.bos.orm.query.QCP;
    import kd.bos.orm.query.QFilter;
     
    public class BeforeBuildRowConditionSample extends AbstractConvertPlugIn {
     
        @Override
        public void beforeBuildRowCondition(BeforeBuildRowConditionEventArgs e) {
            
             if (!cannotPushLockBill()){
                 e.setCustFilterDesc("不允许下推已锁定的单据");        // 给出不允许下推的原因
                
                 // 设置条件表达式，用于脚本执行 （必选）
                 e.setCustFilterExpression(" lockstatus = 'A' ");
                
                 // 同时设置具有相同含义的QFilter条件，用于选单数据查询 （必选）
                 QFilter qFilter = new QFilter("lockstatus", QCP.equals, "A");
                 e.getCustQFilters().add(qFilter);
             }
        }
       
        /**
         * 读取业务应用的系统参数值
         * @return
         */
        private boolean cannotPushLockBill(){
             // 实际业务系统并没有这个选项，本演示代码，直接返回false，演示添加条件
             return false;
             //return (boolean)SystemParamterServiceHelper.getParameter(0, 0, "sal", "cannotpushlockbill");
        }
    }

  


__上一篇：afterBuildQueryParemeter事件

下一篇：beforeGetSourceData事件

 __

5.0 2人评分

内容反馈

*  __评论
收藏 3 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
