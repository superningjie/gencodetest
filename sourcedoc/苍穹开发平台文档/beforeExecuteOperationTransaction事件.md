# beforeExecuteOperationTransaction事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238612178977499648&id=225569169830609664&type=Knowledge&productLineId=29&lang=zh-CN

## beforeExecuteOperationTransaction事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:02

浏览数： 2万 

1 事件介绍

插件可以在此事件，对已经通过校验的数据，进行整理，或者取消操作的执行。

这个事件触发时，还没有启动事务保护，请勿在此修改数据库数据。

  


2 事件触发时机

操作校验通过之后，开启事务存储数据之前，触发此事件。

  


3 代码模板
    
    
    package kd.bos.plugin.sample.bill.bizoperation.template;
     
    import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
    import kd.bos.entity.plugin.args.BeforeOperationArgs;
     
    public class BeforeExecuteOperationTransaction extends AbstractOperationServicePlugIn {
     
        @Override
        public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
             // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明

**public** **class** BeforeOperationArgs **extends** OperationArgs

  * **public** List<ExtendedDataEntity> getSelectedRows()：已经通过校验的单据；

  * **public** DynamicObject[] getDataEntities()：本次操作的全部单据，含校验失败的单据；

  * cancel：是否取消后续操作；

  * **public** **void** setCancelMessage(String cancelMessage)：操作取消原因 。




  


5 应用示例

5.1 案例说明

1\. 子单据体中，有预计送货日期字段，父单据体中有最迟送货日期字段。

2\. 预计送货日期，不能迟于最迟送货日期；

3\. 如果不满足此条件，该单不允许继续操作（批量操作单据时，把这些不满足条件的单据剔除出去）；

4\. 其他满足条件的单据，可以继续操作。

  


5.2 实现方案

1\. 本案例的场景，最优方案是采用自定义校验器 ；

2\. 本实例演示如何在beforeExecuteOperationTransaction事件达成同样的效果。

  


5.3 实例代码
    
    
    package kd.bos.plugin.sample.bill.bizoperation.bizcase;
     
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;
     
    import kd.bos.dataentity.entity.DynamicObject;
    import kd.bos.dataentity.entity.DynamicObjectCollection;
    import kd.bos.entity.ExtendedDataEntity;
    import kd.bos.entity.MainEntityType;
    import kd.bos.entity.formula.RowDataModel;
    import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
    import kd.bos.entity.plugin.PreparePropertysEventArgs;
    import kd.bos.entity.plugin.args.BeforeOperationArgs;
    import kd.bos.entity.validate.ErrorLevel;
    import kd.bos.entity.validate.ValidationErrorInfo;
     
    public class BeforeExecuteOperationTransactionSample extends AbstractOperationServicePlugIn {
     
        /** 单据体 */
        private final static String KEY_ENTRYENTITY = "entryentity";
        /** 子单据体 */
        private final static String KEY_SUBENTRYENTITY = "subentryentity";
       
        /** 预计送货日期字段标识 */
        private final static String KEY_DELIVERYDATE = "deliverydate";
        /** 最迟送货日期字段标识 */
        private final static String KEY_LASTDATE = "lastdate";
       
        private SimpleDateFormat timesdf = new SimpleDateFormat("yyyy-MM-dd");
       
        /**
         * 操作执行前，准备加载单据数据之前，触发此事件
         * @remark
         * 插件可以在此事件中，指定需要加载的字段
         */
        @Override
        public void onPreparePropertys(PreparePropertysEventArgs e) {
             // 要求加载预计送货日期、最迟送货日期字段
             e.getFieldKeys().add(KEY_DELIVERYDATE);
             e.getFieldKeys().add(KEY_LASTDATE);
        }
       
        /**
         * 操作校验执行完毕，开启事务保存单据之前，触发此事件
         * @remark
         * 可以在此事件，对单据数据包进行整理、取消操作
         */
        @Override
        public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
            
             List<ExtendedDataEntity> passDataEntitys = new ArrayList<>();
            
             // 逐单校验送货日期
             for(ExtendedDataEntity dataEntity : e.getSelectedRows()){
                 if (delivaryDateValidate(dataEntity)){
                      passDataEntitys.add(dataEntity);
                 }
             }
     
             // 向系统传回校验通过的单据
             e.getSelectedRows().clear();
             if (passDataEntitys.isEmpty()){
                 e.cancel = true;     // 没有单据通过了校验，取消后续操作
             }
             else {
                 e.getSelectedRows().addAll(passDataEntitys);
             }
        }
       
        /**
         * 校验单据的送货日期是否晚于最迟送货日期
         *
         * @param dataEntity
         * @return
         */
        private boolean delivaryDateValidate(ExtendedDataEntity dataEntity){
            
             // 构建子单据体行数据模型：用于快速访问子单据体行、单据体行、单据头上的字段值
             MainEntityType mainType = (MainEntityType)dataEntity.getDataEntity().getDataEntityType();
             RowDataModel rowDataModel = new RowDataModel(KEY_SUBENTRYENTITY, mainType);
            
             // 取全部单据体行
             DynamicObjectCollection entryRows = dataEntity.getDataEntity().getDynamicObjectCollection(KEY_ENTRYENTITY);
             // 对单据体行循环
             for (DynamicObject entryRow : entryRows){
                 // 取子单据体行
                 DynamicObjectCollection subEntryRows = entryRow.getDynamicObjectCollection(KEY_SUBENTRYENTITY);
                 // 对子单据体行进行循环
                 for (DynamicObject subEntryRow : subEntryRows){
                      rowDataModel.setRowContext(subEntryRow);
                      Date deliveryDate = (Date)rowDataModel.getValue(KEY_DELIVERYDATE);
                      Date lastDate = (Date)rowDataModel.getValue(KEY_LASTDATE);
                      if (deliveryDate.compareTo(lastDate) > 0 ){
                          // 校验不通过，输出一条错误提示，并略过后续行的检查
                          this.addErrMessage(dataEntity,
                                   String.format("预计送货日期(%s)，不能晚于最迟送货日期(%s)！",
                                           timesdf.format(deliveryDate), timesdf.format(lastDate)));
                          return false;    // 校验不通过
                      }
                 }
             }
            
             return true;
        }
       
        /**
         * 向操作结果，添加一条错误提示
         *
         * @param dataEntity
         * @param errMsg
         * @return
         */
        private void addErrMessage(ExtendedDataEntity dataEntity, String errMsg){
            
             Object pkId = dataEntity.getDataEntity().getPkValue();
             int dataIndex = dataEntity.getDataEntityIndex();
             int rowIndex = 0;
             ErrorLevel errorLevel = ErrorLevel.Error;
            
             ValidationErrorInfo errInfo = new ValidationErrorInfo("",
                      pkId, dataIndex, rowIndex,
                      "BeforeExecuteOperationTransactionSample",
                      "送货日期检查",
                      errMsg,
                      errorLevel);
            
             this.operationResult.addErrorInfo(errInfo);
        }
    }

  


__上一篇：onAddValidators事件

下一篇：beginOperationTransaction事件

 __

4.6 10人评分

内容反馈

*  __评论
收藏 5 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
