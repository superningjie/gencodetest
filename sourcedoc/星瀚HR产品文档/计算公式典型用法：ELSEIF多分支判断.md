# 计算公式典型用法：ELSEIF多分支判断

原文链接：https://vip.kingdee.com/knowledge/specialDetail/341893144348365568?category=444210935553486848&id=323034209155768320&type=Knowledge&productLineId=2&lang=zh-CN

## 计算公式典型用法：ELSEIF多分支判断

 __

[![金蝶云社区-直播助教小杨](https://vip.kingdee.com/download/0101875901458a3844ef8a6902b3be9c0728.png)](javascript:;)

直播助教小杨

更新于 2025-12-23 22:08

浏览数： 699 

**应用场景：** 对于多分支的判断场景，使用ELSEIF语句可有效简化公式

**业务需求：计算交通补贴**  
---  
仅销售类员工享有交通补贴，并根据工作地城市决定交通补贴金额北京，交通补贴=500上海，交通补贴=400深圳，交通补贴=450广州，交通补贴=350其他，交通补贴=300  
  
**不使用ELSEIF语句**  
---  
IF FT[职务类别名称]= "销售类"THEN IF FT[工作地城市名称]= "北京" THEN RESULT=500 ELSE IF FT[工作地城市名称]= "上海" THEN RESULT=400 ELSE IF FT[工作地城市名称]= "深圳" THEN RESULT=450 ELSE IF FT[工作地城市名称]= "广州" THEN RESULT=350 ELSE RESULT=300 ENDIF ENDIF ENDIF ENDIFENDIF  
  
****

**使用ELSEIF语句**  
---  
IF FT[职务类别名称]= "销售类"THEN IF FT[工作地城市名称]= "北京" THEN RESULT=500 ELSEIF FT[工作地城市名称]= "上海" THEN RESULT=400 ELSEIF FT[工作地城市名称]= "深圳" THEN RESULT=450 ELSEIF FT[工作地城市名称]= "广州" THEN RESULT=350 ELSE RESULT=300 ENDIFENDIF  
  
  


 __上一篇：计算公式典型用法：EXIT退出公式

下一篇：计算公式典型用法：临时变量

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
