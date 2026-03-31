# 元数据问题FAQ汇总

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=475358144147882496&id=228461585407443712&type=Knowledge&productLineId=29&lang=zh-CN

## 元数据问题FAQ汇总

 __

[![金蝶云社区-tara](https://vip.kingdee.com/download/0101e0c8ae98aa384ae3be4a93bbc157aafd.png)](javascript:;)

tara

更新于 2023-08-04 16:35

浏览数： 8,522 

****

**Q：设计器提示：“绑定的实体字段未找到，删除字段编辑控件后重新添加”**

**A** ：缺少实体字段，例如布局里的就是字段控件，需要跟原单的实体字段绑定才能使用，原单删掉了实体字段，布局里就会报这个错误

  


  


**Q：如何解决“属性不存在或未设置字段”？**

**A** ：参考链接：

<https://club.kdcloud.com/article/87207283972587520>

  


  


**Q** : **环境里的元数据数据不对了，如果查看谁做了修改操作，如何查看元数据历史版本****？**

**A** : 开发平台搜索-“元数据操作日志”

参考链接：[ https://club.kdcloud.com/article/58964707301700352](https://club.kdcloud.com/article/58964707301700352)

删除的元数据如何找回： <https://club.kdcloud.com/article/134345005765518336>

  


  


  


**Q** ：**删掉导入的应用，但是删除报失败，说这个脚本删除失败，这需要怎么处理呢？**

**A** : 

select * from t_meta_formdesign where fid in (

select fpageid from t_meta_scriptrelpage where fscriptid in (

select fid from T_META_PLUGINSCRIPT where fscriptname= '查询eas预算删除失败')) 

查询脚本插件被哪个其他应用单据注册，先取消注册，再删除

  


  


  


**Q：为什么我这个元数据没有扩展和继承按钮，其他的标准元数据都能操作？**  


**A** ：部分标准页面开发会配置“不允许扩展”的属性，限制二开能力

![](https://vip.kingdee.com/download/01008043919ba46e47178c69fee8baa07b8f.png)

  


  


**Q：更新元数据按钮灰显了，如何开启启用？**

**![](https://vip.kingdee.com/download/01004a95590c462242bd9538fc68d3e0672f.png)**

**A** ：控制只允许管理员可以修改，需要配置当前用户为管理员

![](https://vip.kingdee.com/download/01003428926affd44c8d91558f925a072d22.png)

  


  


**Q：插件代码中如何获取元数据？**

****Q：代码中如何获取控件的可见性？****

**A** ：
    
    
    MainEntityType mainType = this.getModel().getDataEntityType();
    FormMetadata formMeta = (FormMetadata)MetadataDao
    .readRuntimeMeta(
    MetadataDao.getIdByNumber(mainType.getName(), MetaCategory.Form), 
    MetaCategory.Form);
    //获取基础资料的type
    DynamicObject bas = (DynamicObject) this.getModel().getValue("基础资料字段");  
    IDataEntityType type = bas.getDataEntityType();  
    if (type instanceof BasedataEntityType) {   
      BasedataEntityType basedataEntityType = (BasedataEntityType) type;
    }
    //获取字段的属性，如可见性、背景色等
    for(ControlAp control:formMeta.getItems()) {    
       if(control instanceof FieldAp) {     
       control.getVisible();     
       control.getBackColor();    
       }   
    }

  


  


**Q：二开单据****使用BusinessDataServiceHelper或者QueryServiceHelper时提示元数据不存在**

**![](https://vip.kingdee.com/download/0100ebdc69edb97742d58b5912f232f6663c.png)**

**A** ：BusinessDataServiceHelper或者QueryServiceHelper使用时，继承单据传新单据标识,扩展单据传原单据标识

  


  


  


**Q：如何在运行期动态修改界面元数据，如控件的背景色，样式等？**

****A** ：**

**参考链接：<https://club.kdcloud.com/article/183388>  
**

  


  


  


**Q：如何修改单据的继承、模板**

**A** ：标准产品不允许修改继承树、模板，修改会导致已扩展的二次开发单据出现问题

对于二次开发的表单，参考方式 <https://club.kdcloud.com/article/142961097161585408>

  


  


  


  


 __上一篇：开发服务云FAQ总结

下一篇：如何查看开发平台的单据的扩展修改内容

 __

4.9 9人评分

内容反馈

*  __评论
收藏 21 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
