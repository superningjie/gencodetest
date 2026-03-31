# afterConvert事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612670079680256&id=225705003707377152&type=Knowledge&productLineId=29&lang=zh-CN

## afterConvert事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:02

浏览数： 1.1万 

1 事件介绍

插件可以在这个事件，对生成的目标单数据，进行最后的调整。

  


2 事件触发时机

目标单据生成完毕，触发此事件。

这个事件，是最后触发的，至此，全部业务逻辑已经执行完毕。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.billconvert.template;
     
    import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
    import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
     
    public class AfterConvert extends AbstractConvertPlugIn {
     
        @Override
        public void afterConvert(AfterConvertEventArgs e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** AfterConvertEventArgs **extends** ConvertPluginEventArgs

  * **public** ExtendedDataEntitySet getTargetExtDataEntitySet()：生成的目标单扩展数据包；

  * **public** Map<String, DynamicProperty> getFldProperties() ：源单字段与源单行数据包属性对象映射字典，需要据此到源单行中取需要的字段值。




  


5 应用示例

5.1 案例说明

1\. 采购单据，转固定资产卡片时，需每个物品生成一张卡片，即按数量分单；

2\. 当前转换规则的分单策略，无法配置出此需求，只能插件开发。

  


5.2 实现方案

1\. 捕获afterConvert事件，复制新单：根据资产数量字段值，确认新单的复制次数。

  


5.3 实例代码
    
    
    package kd.bos.plugin.sample.bill.billconvert.bizcase;
     
    import java.util.ArrayList;
    import java.util.List;
     
    import org.tmatesoft.sqljet.core.internal.lang.SqlParser.bool_return;
     
    import kd.bos.dataentity.entity.DynamicObject;
    import kd.bos.dataentity.utils.OrmUtils;
    import kd.bos.entity.ExtendedDataEntity;
    import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
    import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
    import kd.bos.entity.botp.runtime.ConvertConst;
     
    /**
     * 演示单据转换插件 afterConvert 事件的使用
     *
     * @author rd_JohnnyDing
     * @remark
     * 案例说明
     * 1. 采购单据，转固定资产卡片时，每个物品生成一张开片，即按数量分单
     * 2. 当前转换规则的分单策略，无法配置出此需求，只能插件开发
     *
     * 实现方案
     * 1. 捕获afterConvert事件，检查目标单数量，超过1则拆分出来
     *
     */
     
     public class AfterConvertSample extends AbstractConvertPlugIn {
     
           private final static String FAREALCARD_ENTITYNAME = "fa_card_real";
          
           @Override
           public void afterConvert(AfterConvertEventArgs e) {
                 
                  // 获取已生成的资产卡片
                  ExtendedDataEntity[] billDataEntitys = e.getTargetExtDataEntitySet().FindByEntityKey(FAREALCARD_ENTITYNAME);
                 
                  // 构造 ExtendedDataEntity 时需要的索引值
                  int dataIndex = billDataEntitys.length;
                 
                  List<ExtendedDataEntity> copyDataEntitys = new ArrayList<>();
                  for(ExtendedDataEntity billDataEntity : billDataEntitys){
                        
                         // 如下代码演示如何取本次下推关联的源单行数据包（本示例无需用到关联的源单行，代码注释掉）
                         //List<DynamicObject> sourceRows = (List<DynamicObject>)billDataEntity.getValue(ConvertConst.ConvExtDataKey_SourceRows);
                         //boolean isNewEntity = (boolean)billDataEntity.getValue(ConvertConst.ConvExtDataKey_IsNewEntity);
                        
                         // 原始的资产数量
                         int qty = (int) billDataEntity.getValue("assetamount");
                        
                         // 将资产数量改为1
                         billDataEntity.setValue("assetamount", 1);
                        
                         // 来源分录拆分序号 从1开始
                         int splitSeq = 1;
                         billDataEntity.setValue("sourceentrysplitseq", splitSeq++);
                        
                         // 复制 （原始的资产数量 - 1）个卡片对象
                         for(int i = 1; i < qty; i++){
                                DynamicObject copyObj = (DynamicObject) OrmUtils.clone(billDataEntity.getDataEntity(), false, true);
                                copyObj.set("sourceentrysplitseq", splitSeq++);
                               
                                copyDataEntitys.add(new ExtendedDataEntity(copyObj, dataIndex++, 0));
                         }
                  }
                 
                  // 将复制出的单据，放入 targetExtDataEntitySet ，最终就会生成那么多的卡片
                  e.getTargetExtDataEntitySet().AddExtendedDataEntities(FAREALCARD_ENTITYNAME, copyDataEntitys);
           }
    }

__上一篇：afterCreateLink事件

下一篇：afterBuildDrawFilter事件

 __

4.3 6人评分

内容反馈

*  __评论
收藏 9 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
