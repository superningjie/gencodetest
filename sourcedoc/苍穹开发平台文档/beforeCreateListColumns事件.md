# beforeCreateListColumns事件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=238607792339561472&id=224120361749421824&type=Knowledge&productLineId=29&lang=zh-CN

## beforeCreateListColumns事件

 __

[![金蝶云社区-部伟](https://vip.kingdee.com/download/0101dcc97f4388df42d99062f90f23e33f10.png)](javascript:;)

部伟

更新于 2024-04-22 15:02

浏览数： 9,738 

1 事件简介

插件可以在此事件中，根据页面参数、过滤他条件，动态添加、删除单据列表的显示列。  


2 事件触发时机

刷新单据列表，构建单据列表显示的列时，触发此事件，传入在设计器中已配置的列集合。

3 代码模板
    
    
    package kd.bos.plugin.sample.bill.list.template;
    
    import kd.bos.form.events.BeforeCreateListColumnsArgs;
    import kd.bos.list.plugin.AbstractListPlugin;
    
    public class BeforeCreateListColumns extends AbstractListPlugin {
    
        @Override
        public void beforeCreateListColumns(BeforeCreateListColumnsArgs args) {
            // TODO 在此添加业务逻辑
        }
    }

  


4 参数说明 

  


**public** **class** BeforeCreateListColumnsArgs **extends** EventObject

  * **public** Object getSource()：事件源，单据列表控件BillList

  * **public** List<IListColumn> getListColumns()：列集合

  * **public** List<ListColumnGroup> getListGroupColumns()：列分组集合




  


5 应用示例
    
    
    import java.util.List;import java.util.Map;import kd.bos.dataentity.entity.LocaleString;import kd.bos.dataentity.utils.StringUtils;import kd.bos.form.events.BeforeCreateListColumnsArgs;import kd.bos.form.events.FilterContainerSearchClickArgs;import kd.bos.list.IListColumn;import kd.bos.list.ListColumn;import kd.bos.list.plugin.AbstractListPlugin;public class BeforeCreateListColumnsSample extends AbstractListPlugin {
    
        /** 用户选择的数据状态过滤值 */
        private String billStateFilterValue;
    
        /**
         * 用户在过滤条件面板，修改了过滤条件之后，触发此事件
         * @remark
         * 在此事件，获取用户设置的数据状态过滤值
         */
        @Override
        public void filterContainerSearchClick(FilterContainerSearchClickArgs args) {
            billStateFilterValue = (String) args.getFilterValue("billstatus");
        }
    
        /**
         * 在构建列表显示的列时触发，传入设计时预置的列集合
         * @remark
         * 在此事件，根据自定义参数值，动态添加列
         */
        @Override
        public void beforeCreateListColumns(BeforeCreateListColumnsArgs args) {
            // 根据自定义参数state的值，动态添加列        //int state = 1;        int state = 2;
            String stateParamValue = this.getView().getFormShowParameter().getCustomParam("state");
            if (StringUtils.isNotBlank(stateParamValue)){
                state = Integer.valueOf(stateParamValue);
            }
    
            switch (state) {
                case 1:
                    // 动态添加新列：文本1
                    ListColumn colText1 = this.createListColumn("textfield1", "文本1");
                    args.addListColumn(colText1);
    
                    break;
                case 2:
                default:
                    // 动态添加新列：文本2
                    ListColumn colText2 = this.createListColumn("textfield2", "文本2");
                    args.addListColumn(colText2);
    
                    break;
            }
    
            // 根据数据状态过滤条件值，动态添加列        if (StringUtils.equals(billStateFilterValue, "C")){
                ListColumn colUser = this.createListColumn("auditor.name", "审核人");
                args.addListColumn(colUser);
            }
            else {
                ListColumn colUser = this.createListColumn("creator.name", "创建人");
                args.addListColumn(colUser);
            }
        }
    
        /**
         * 创建列对象返回
         *
         * @param key 列标识，需要显示的字段，如"textfield"、 "basedatafield.name"
         * @param caption 列标题
         * @return
         */
        private ListColumn createListColumn(String key, String caption){
    
            ListColumn col = new ListColumn();
    
            col.setCaption(new LocaleString(caption));
            col.setKey(key);
            col.setListFieldKey(key);
    
            return col;
        }
    }

__上一篇：列表filterContainerInit事件

下一篇：beforeCreateListDataProvider事件

 __

5.0 2人评分

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
