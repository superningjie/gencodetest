# 如何使用HR动态数据范围

原文链接：https://vip.kingdee.com/knowledge/specialDetail/609415615861675008?category=632217957879262464&id=608709364961956864&type=Knowledge&productLineId=2&lang=zh-CN

## 如何使用HR动态数据范围

 __

[![金蝶云社区-haizhen](https://vip.kingdee.com/download/01018da118a52b414b8d9addb88930ebeaa7.png)](javascript:;)

haizhen

更新于 2025-12-23 11:10

浏览数： 645 

## **1 业务场景**

###  1.1 背景介绍

通常用户的权限范围与自己任职的部门、任职的公司密切相关，具有相似的规律。此类用户的数据范围可以统一归纳形成通用的数据范围。根据分配不同用户，得到不同的数据范围。

### 1.2 场景详细描述

1) 用户可查看的数据范围与当前用户的任职部门、任职公司一样。用户调动部门后，无需调整用户的数据范围，自动按最新的任职进行数据验权。

2) 除行政组织维度外，其他的维度也可以二开定义动态数据范围，例如：我管理的岗位。

## **2 解决方案**

**-场景1：给用户分配系统预置的动态数据范围。**

标品出厂预置了4个行政组织相关的动态数据范围，可直接在数据范围配置页面选择使用。

预置数据清单：  
  
序号| 动态数据范围| 备注  
---|---|---  
1| 任职部门| 包含主职、兼职部门  
2| 任职部门包含下级| 包含主职、兼职部门的下级  
3| 任职部门所属公司| 包含主职、兼职所属公司  
4| 任职部门所属公司包含下级| 包含主职、兼职所属公司的下级  
  
运用页面：角色数据范围页面、用户数据范围页面、动态授权方案-角色数据范围页面

![](https://vip.kingdee.com/download/01097737a8f3edf54d0189cafe9c574ef8c7.png)

 _注：动态数据范围与具体的维度值可任选一个进行维护，最终验权取两者并集。_

**-场景2：二开定义动态数据范围并分配。**

标品预置的动态数据范围不满足项目需求，可二开定义动态数据范围，例如：我管理的岗位、我负责的产品线等用户相关的业务数据范围。

操作步骤：

1) 二开定义动态数据范围方法

开发一个微服务方法，需实现接口：

kd.[hr](https://www.kingdee.com/products/cosmic_hr.html?utm_source=shequ).hbp.business.service.perm.dyna.condhandler.IDynaCondParser

需要实现的方法为：

List<FixedDimValue> parseDynaCond(String propType,Long userId,Map<String, Object> customParam);

其中参数propType为要过滤的属性的类型，值为long或者string, userId为当前用户，customParam为其他需要的参数，目前为空map。

二开需要实现此方法，返回值为当前用户有权限的维度值。

然后把实现类放到ServiceFactory的serviceMap中，其中key为开发者自行定义，value为实现类的全名，比如：

serviceMap.put("CustomDynaParser", "com.xxx.test.CustomDynaParserImpl ");

2）新增动态数据范围

操作路径：开发平台搜索“hrcs_dynacond 动态数据范围配置”；

新增表单，“解析调用类型”选择“调用微服务”（二开配置必选），“微服务所在应用”按业务需求选择对应的“应用”；“微服务”填写在serviceMap中二开微服务对应的key。

![](https://vip.kingdee.com/download/010014916e4114554a139da928f34554056f.png)

支持定义以下三种类型：

  * ①通用的动态数据范围，所有的维度都可使用




![](https://vip.kingdee.com/download/010001308701324e4edbbcd9f32f46f4bc36.png)

  * ②指定基础资料的动态数据范围，例如：指定“国家和地区”维度可使用




![](https://vip.kingdee.com/download/0100d4e37d184c8d40758dec0dd0818e7279.png)

  * ③指定组织团队的动态数据范围，例如：指定“行政组织”维度可使用




![](https://vip.kingdee.com/download/01009711c4e92af84e0b9569e4644dbfdfd8.png)

3）定义好之后，可运用在权限数据范围页面：角色数据范围页面、用户数据范围页面、动态授权方案-角色数据范围页面。根据以上配置的类型自动过滤可选择的动态数据范围。

## 3 动态数据范围验权效果

• 角色数据范围：角色的“行政组织”设置具体组织：Org1 ,还设置了预置动态数据范围：任职部门包含下级

• 权限分配：角色分配给用户1

• 生效任职经历：用户1的主任职部门是org2 ,兼职部门是org 3

• 验权效果：用户的行政组织范围为：Org1 、org2（包含下级）、org 3（包含下级）

## 4 禁用动态数据范围

如预置的动态数据范围不满足项目需求且业务不使用该预置数据，或二开的动态数据范围随着业务变更为废弃，则可以“禁用”该记录（禁用后选择页面不可见），进入开发平台搜索“hrcs_dynacond 动态数据范围配置”，进入列表进行“禁用”。

![](https://vip.kingdee.com/download/0109f8086879a6e34ceb83c3823d784211a2.png)

* * *

## 变更记录

**产品版本****（季度版本号/patch补丁号）**| **更新内容**| **更新时间**  
---|---|---  
**V 7.0.1**|  初始版本| 2024年09月  
  
 __上一篇：入职自动授予员工自助权限

下一篇：二开业务如何配置HR权限

 __

暂无评分

内容反馈

*  __评论
收藏 1 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
