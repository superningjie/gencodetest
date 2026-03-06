# beforeBuildGroupMode事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612670079680256&id=225692169153914112&type=Knowledge&productLineId=29&lang=zh-CN

## beforeBuildGroupMode事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-17 20:31

浏览数： 3,166 

1 事件介绍

插件可以在此事件，调整分单依赖的字段，影响后续的分单。

  


2 事件触发时机

系统按照单据转换规则 – 分单策略，对读取到的源单行，进行分组前，触发此事件。

  


单据转换规则的分单策略，把源单行进行分组，不同的组生成不同的目标单：

  * 一对一：一张源单，生成一张目标单，即按照源单单据内码分组；

  * 多对一：本次选择的全部源单，下推到一张目标单，即按照常量值分组，全部单都会分到一个组；

  * 按规则分单：本次选择的全部源单行，按照分单字段值进行分组。




单据转换规则支持的分录行合并策略，把源单行进行分组，不同的组合并为一条目标单分录行：

  * 一对一：一条源单分录行，生成一条目标单分录行，即按照源单分录行内码分组；

  * 多对一：本次选择的全部源单行，合并为一条目标单分录行，即按常量分组，全部源单行，都回分到一个组；

  * 按规则合并：本次选择的全部源单行，按照字段值进行分组；相同的字段值分为一组，合并为一条分录行。




正常情况下，可以配置转换规则 – 分单策略，控制分单、合并；

插件可以根据实际业务数据，在此事件中，动态调整分单、合并依赖的字段，从而影响后续的分单。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.billconvert.template;
     
    import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
    import kd.bos.entity.botp.plugin.args.BeforeBuildGroupModeEventArgs;
     
    public class BeforeBuildGroupMode extends AbstractConvertPlugIn {
     
        @Override
        public void beforeBuildGroupMode(BeforeBuildGroupModeEventArgs e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** BeforeBuildGroupModeEventArgs **extends** ConvertPluginEventArgs

  * **public** String getHeadGroupKey()：现有的分单依据字段；

  * **public** **void** setHeadGroupKey(String headGroupKey) ：设置新的分单依据字段；

  * **public** String getEntryGroupKey()：现有的单据体行合并依据字段；

  * **public** **void** setEntryGroupKey(String entryGroupKey)：设置单据体行合并依据字段；

  * **public** String getSubEntryGroupKey()：子单据体行合并字段；

  * **public** **void** setSubEntryGroupKey(String subEntryGroupKey)：设置子单据体行合并字段。




  


 __上一篇：afterGetSourceData事件

下一篇：afterCreateTarget事件

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
