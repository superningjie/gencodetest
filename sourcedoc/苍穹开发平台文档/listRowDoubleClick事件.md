# listRowDoubleClick事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238607792339561472&id=224178109145445632&type=Knowledge&productLineId=29&lang=zh-CN

## listRowDoubleClick事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:02

浏览数： 3,691 

1 事件介绍

普通单据列表双击行，会自动打开单据修改界面；而单据F7列表双击行，会把选中的行，返回给调用页面；

插件可以在此事件中，取消上述系统内置的逻辑。

  


2 事件触发时机

用户双击单据列表行时，触发此事件。

3 代码模板
    
    
    package kd.bos.plugin.sample.bill.list.template;
     
    import kd.bos.list.events.ListRowClickEvent;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class ListRowDoubleClick extends AbstractListPlugin {
       
        @Override
        public void listRowDoubleClick(ListRowClickEvent evt) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

参阅[listRowClick事件](https://vip.kingdee.com/article/224176095946186752)参数。

5 应用示例

5.1 案例说明

1\. 用户单击列表行时，不做任何处理；

2\. 用户双击列表行时，不弹出默认的单据界面，而是打开当前行使用的物料界面。

  


5.2 实现方案

1\. 捕获 listRowClick 事件，取消后续操作；

2\. 捕获 listRowDoubleClick 事件；

  * 取消系统预置的后续操作；

  * 获取当前行上的物料内码，自行打开物料查看界面。




  


5.3 实例代码
    
    
    package kd.bos.plugin.sample.bill.list.bizcase;
     
    import kd.bos.bill.BillShowParameter;
    import kd.bos.bill.OperationStatus;
    import kd.bos.form.ShowType;
    import kd.bos.list.ListShowParameter;
    import kd.bos.list.events.ListRowClickEvent;
    import kd.bos.list.plugin.AbstractListPlugin;
     
    public class ListRowDoubleClickSample extends AbstractListPlugin {
     
        private final static String ENTITYID_MATERIAL = "bd_material";
        private final static String KEY_MATERIALID = "material";
       
        /**
         * 用户单击行时触发此事件
         * @remark
         * 在移动端单据列表上，用户单击行打开单据界面；
         * 可以在此事件，取消上述逻辑
         */
        @Override
        public void listRowClick(ListRowClickEvent evt) {
             // 取消后续处理
             evt.setCancel(true);
        }
     
        /**
         * 用户双击行时触发此事件
         * @remark
         * 普通单据列表，双击行，会自动打开单据修改界面；
         * 而单据F7列表双击行行，会把选中的行，返回给调用页面；
         * 可以在插件中，取消上述逻辑
         */
        @Override
        public void listRowDoubleClick(ListRowClickEvent evt) {
            
             if (!isLookup()){
                 // 取消系统内置的逻辑处理
                 evt.setCancel(true);
                
                 // 自行打开物料查看界面
                 if (!evt.getCurrentListSelectedRow().getDataMap().containsKey(KEY_MATERIALID)){
                      return;
                 }
                 long materialId = (long)evt.getCurrentListSelectedRow().getDataMap().get(KEY_MATERIALID);
                 if (materialId == 0){
                      return;
                 }
                
                 BillShowParameter showParameter = new BillShowParameter();
                 showParameter.setFormId(ENTITYID_MATERIAL);
                 showParameter.setPkId(materialId);
                
                 showParameter.getOpenStyle().setShowType(ShowType.Modal);
                 showParameter.setStatus(OperationStatus.VIEW);
                
                 this.getView().showForm(showParameter);
             }
        }
       
        /**
         * 是否F7列表
         *
         * @return true是，false不是
         */
        private boolean isLookup() {
             boolean isLookup = false;
             if (this.getView().getFormShowParameter() instanceof ListShowParameter) {
                 ListShowParameter listShowParameter = (ListShowParameter) this.getView().getFormShowParameter();
                 isLookup = listShowParameter.isLookUp();
             }
             return isLookup;
        }
    }

  


__上一篇：listRowClick 事件

下一篇：filterContainerBeforeF7Select事件

 __

5.0 1人评分

内容反馈

*  __评论
收藏 2 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
