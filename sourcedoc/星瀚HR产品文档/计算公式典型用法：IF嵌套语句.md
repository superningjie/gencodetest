# 计算公式典型用法：IF嵌套语句

原文链接：https://vip.kingdee.com/knowledge/specialDetail/341893144348365568?category=444210935553486848&id=322812556429765376&type=Knowledge&productLineId=2&lang=zh-CN

## 计算公式典型用法：IF嵌套语句

 __

[![金蝶云社区-直播助教小杨](https://vip.kingdee.com/download/0101875901458a3844ef8a6902b3be9c0728.png)](javascript:;)

直播助教小杨

更新于 2025-12-23 22:08

浏览数： 876 

**应用场景：** 对于复杂的判断场景，使用IF嵌套提取公共部分的条件判断，可以有效简化公式，降低书写复杂度，提高可读性

**业务需求：计算司龄工资**  
---  
司龄工资仅一线员工，且中国员工享有，并根据司龄决定司龄工资金额小于1年，无司龄工资1-2年，司龄工资=1002-3年，司龄工资=2003-5年，司龄工资=3005-7年，司龄工资=400大于7年，司龄工资=500  
  
**不使用IF嵌套**  
---  
SP[司龄]=FC[取两个日期间相差年数](FT[入职日期],FT[截止日期],30,12)IF FT[国籍名称]= "中国" AND BS[一线员工标识]= "Y" AND SP[司龄]<1THEN RESULT=0ELSEIF FT[国籍名称]= "中国" AND BS[一线员工标识]= "Y" AND SP[司龄]<2THEN RESULT=100ELSEIF FT[国籍名称]= "中国" AND BS[一线员工标识]= "Y" AND SP[司龄]<3THEN RESULT=200ELSEIF FT[国籍名称]= "中国" AND BS[一线员工标识]= "Y" AND SP[司龄]<5THEN RESULT=300ELSEIF FT[国籍名称]= "中国" AND BS[一线员工标识]= "Y" AND SP[司龄]<7THEN RESULT=400ELSEIF FT[国籍名称]= "中国" AND BS[一线员工标识]= "Y" AND SP[司龄]>=7THEN RESULT=500ENDIF  
  
****

**使用IF嵌套**  
---  
SP[司龄]=FC[取两个日期间相差年数](FT[入职日期],FT[截止日期],30,12)IF FT[国籍名称]= "中国" AND BS[一线员工标识]= "Y"THEN IF SP[司龄]<1 THEN RESULT=0 ELSEIF SP[司龄]<2 THEN RESULT=100 ELSEIF SP[司龄]<3 THEN RESULT=200 ELSEIF SP[司龄]<5 THEN RESULT=300 ELSEIF SP[司龄]<7 THEN RESULT=400 ELSE RESULT=500 ENDIFENDIF  
  
  


 __上一篇：薪酬计算公式语法大全

下一篇：计算公式典型用法：EXIT退出公式

 __

5.0 1人评分

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
