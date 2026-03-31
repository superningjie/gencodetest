# 【薪酬问题排查攻略】: 为什么使用Oracle数据库时会在F7中搜索报错

原文链接：https://vip.kingdee.com/knowledge/specialDetail/341893144348365568?category=718495472012498432&id=528525724224164864&type=Knowledge&productLineId=2&lang=zh-CN

## 【薪酬问题排查攻略】: 为什么使用Oracle数据库时会在F7中搜索报错

 __

[![金蝶云社区-击居侠](https://vip.kingdee.com/download/01011ca250277e1141edbebc3b96d7a7b33c.png)](javascript:;)

击居侠

更新于 2025-12-23 22:02

浏览数： 217 

索引：W0002  


  


问题：为什么使用Oracle数据库时会在F7中搜索报错？

  


具体场景：

使用Oracle数据库情况下，通过薪资核算任务添加核算人员F7，进行大批量数据搜索时报错：

mservice:ORA-00604：递归SQL级别1出现错误

ORA-01013：用户请求取消当前的操作...

  


**排查思路：**

检查数据库配置：对于Oracle库，需要将in查询优化阈值设置为1000。

  


![上传图片](https://vip.kingdee.com/download/0100ed0397205a8a47d4b733f7aa15ae0b4b.png)

![上传图片](https://vip.kingdee.com/download/0100e2f4184d6bee4ea19fc24d2102ecc4c8.png)

  


  


产品版本  
| 更新内容| 更新日期  
---|---|---  
V6.0.001| 初始版本| 2023年10月15日  
  
  
  
  


 __上一篇：【薪酬问题排查攻略】: 薪资计算后找不到新增的薪酬项目

下一篇：【薪酬问题排查攻略】：薪资核算审核时，提示正在导入数据中，无法审核

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
