# SpreadJS控件

原文链接：https://vip.kingdee.com/knowledge/specialDetail/218022218066869248?category=218035603349614848&id=229915214085755648&type=Knowledge&productLineId=29&lang=zh-CN

## SpreadJS控件

 __

[![金蝶云社区-wenq](https://vip.kingdee.com/download/0101e2fa2e05f085497e9b8ce5f6ef7ac0fd.png)](javascript:;)

wenq

更新于 2025-09-24 19:37

浏览数： 6,177 

一级标题

二级标题

三级标题

四级标题

五级标题

六级标题

添加图片链接

上传图片 
    
    
      
    # 变更记录
    
    | 产品版本 | 更新内容 | 更新日期 |
    | --- | --- | --- |
    | V5.0.024 | 新增了Spread 表格参数设置后点击确定可保存表格参数的功能，满足了用户需要记住表格参数设置的需求，详见5.1.2 | 2023年7月 |
    | V5.0.025 | Spread表格支持了设定选中区域指令的位置参数，满足了用户希望将选定区域放置在指定的可视范围的需求，详见6：setSelections | 2023年7月 |
    | V6.0.15 | 完善了Spread多表联动的功能，支持多表同步滚动条、行高、列宽、缩放，提高用户设置报表模板的易用性，详见6：syncSpreads | 2024年7月 |
    | v7.0.1 | 1、getSpreadJson与setSpreadJson现在支持对单个工作表（sheet）进行操作，详见6：getSpreadJson、setSpreadJson<br>2、出于第三方商业合规性要求，伙伴/二开环境的表单设计器中将不再内置SpreadJS控件，详见1：下架说明 | 2024年10月 |
    | v7.0.3 | 1、updatavalue支持字典模式传输数据，降低传输数据量，提升性能，详见6：updatavalue<br>2、优化了SpreadJS表格设置列格式时数据导入的性能，避免页面卡顿 | 2024年12月 |
    | V7.0.4 | setSpreadParams新增isDeleteLockedSendRequest参数：delete快捷键清除锁定单元格数据时向后端发送网络请求，可二开实现框选单元格包含锁定单元格时，仅清除非锁定单元格的数据，详见6：setSpreadParams | 2024年12月 |
    | V8.0.1 | 升级了Spread控件版本，增强了相关功能并修复了若干问题，满足不同客户的使用场景。注意：如果后端开发用到了GCExcel组件，则为了前后端组件版本适配，需要同步升级后端GCExcel组件，详见5.2 | 2025年9月 |
    
    # 1 功能介绍
    SpreadJS 是一款基于 HTML5 的纯前端电子表格控件，提供了与 Excel 高度类似的功能和兼容性。
    该控件仅在PC端支持。
    ## 下架说明
    出于第三方商业合规性要求，SpreadJS 控件的使用权限严格限定于本公司旗下标品业务（包括但不限于“星空”、“星瀚”系列），若在二开中继续使用，需自行承担潜在的商业风险。
    从苍穹 7.0.1 版本开始，针对伙伴或二开的苍穹环境在其表单设计器中隐藏 SpreadJS 控件，在控件面板中无法选到该控件参与布局设计。若客户有在线使用类似 SpreadJS 控件的需求，我们建议：
    
    * 自行采购 SpreadJS 官方授权，并集成到项目中使用
    * 选择其他功能类似的第三方表格控件进行集成
    
    # 2 控件对象
    `kd.bos.form.spread`
    # 3 视觉展示
    ![1.png](/download/0100f9b26b4809864739b9609ec460d642f6.png)
    # 4 属性说明
    ## 4.1 通用属性
    >通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)
    ## 4.2 样式属性
    >样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)
    ## 4.3 业务属性
    
    | 属性名 | 类型 | 运行时参数名 | 默认值 | 说明 | PC |
    | --- | --- | --- | --- | --- | --- |
    | 显示编辑工具栏 | boolean | setb | true | 是否默认显示工具栏，开启时将自动展开，关闭时则自动收起。无论默认状态如何，随时可以手动展开或收起工具栏 | V4.0及以上 |
    | 允许多页签 | boolean | smt | false | 是否开启多页签模式，开启后控件底部将显示页签栏，支持快速切换和新增。导入功能也与该属性相关，开启时，允许同时选择多个sheet导入，关闭时仅允许选择单个sheet导入 | V4.0及以上 |
    | 支持导出excel | boolean | ee | false | 是否支持批量导出，开启后，控件将支持批量导出功能。可以使用 batchExportExcelFiles 指令，将多个 Spread 的数据合并导出为一个 Excel 文件。适用于列表页面，需要批量导出多个Spread数据的场景 | V4.0及以上 |
    
    # 5 功能详情
    ## 5.1 工具栏
    ### 5.1.1工具栏功能介绍
    spread工具栏配置了表格常用的一些功能
    
    * 对于单元格样式的一些设置：字体样式、字体大小、字体颜色、文本居中、单元格背景色
    * 对于单元格格式的一些设置：常规、日期、时间、会计专用等等
    * 对于工作表的一些设置：冻结表格、过滤表格、打印、显示公式等等
    
    以往的文章中我们已经对这些特性进行过介绍
    链接直达：
    [Spread工具栏新特性](https://vip.kingdee.com/article/390596666833372416)
    [Spread 全新视觉交互升级](https://developer.kingdee.com/article/502501692391512576)
    [Spread支持查找替换功能](https://developer.kingdee.com/article/85798461483727872)
    [Spread支持数据有效性](https://developer.kingdee.com/article/322746947583383040)
    [Spread支持打印](https://developer.kingdee.com/article/137514916042362624)
    [Spread支持自定义单元格格式](https://developer.kingdee.com/article/324229535094933760)
    [Spread支持条件格式](https://developer.kingdee.com/article/523501187660638464)
    ### 5.1.2表格设置
    设置表格配置参数后（功能入口：表格工具栏的配置按钮），点击确定，可将参数保存到当前表格，该参数只对当前表格生效。再次打开该表格或其他用户使用该表格，该参数依旧生效。
    ![2.png](/download/010012cffaed11c044ac9662e9dfc8e96a88.png)
    ## 5.2 SpreadJS 版本升级
    鉴于在过往客户反馈中许多问题需通过升级 SpreadJS 版本解决，为提升产品稳定性与功能性，我们在苍穹平台 8.0.1 版本中，正式将内置的 SpreadJS 第三方控件从 V14.2.6 升级至 V17.1。
    升级影响与必要操作：
    
    * 如果您的项目后端使用了 GCExcel 组件，必须将其同步升级至 V7 版本，以确保前后端功能一致性与稳定性；
    * 为了提升存储何计算性能，SpreadJS 第三方组件设计了共享公式，当存在两个相同的公式时将提取到sharedFormulas中，直接解析 SpreadJson 数据获取公式可能获取正确的公式。官方建议后端通过 GCExcel 的 api 去获取和改动这些信息；
    * 除了公式，由于 SpreadJS 产品版本持续在升级，SpreadJson 中的数据结构很有可能发生变化，建议都通过 GCExcel 的 api 执行操作；
    
    版本详情：有关 V17.1 版本解决的具体问题与新特性，请参阅官方发布说明：
    [17.1 < 发布说明 | 葡萄城 SpreadJS 表格控件在线文档](/tolink?target=https://demo.grapecity.com.cn/spreadjs/help/docs/rnotes/171)
    
    # 6 SpreadJS接口介绍
    spread功能高度依赖业务，因此部分功能需要后端配置响应的指令才能正常使用，比如插行、删行。
    
    | 指令 | 参数 | 功能 | 支持版本 | 备注 |
    | --- | --- | --- | --- | --- |
    | callbackAction | {callback:'invokemethod', invokemethod:'addRows'} | 回调函数指令，后端告诉前端它还有后续动作 | v4.0及以上 |  |
    | setSpreadJson | spreadJS压缩过后的json串 | 加载spreadJS的json串<br>支持加载sheet的JSON字符串（V7.0） | v4.0及以上 |  |
    | appendRows | {count:1, si:0} | 在指定工作表尾部追加count行<br>count：行数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | appendCols | {count:1, si:0} | 在指定工作表尾部追加coun列<br>count：列数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | deleteRow | {data:[2, 1], si:0} || {data: [{index:1, count: 2}], si:} | 删除工作表指定索引处的行<br>data：指定行的索引（必填），删除连续行时索引应该倒序从后往前删除<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：删除的起始位置<br>count：删除多少行 | v4.0及以上 |  |
    | deleteCol | {data:[2, 1], si:0} || {data: [{index:1, count: 3}], si:} | 删除工作表指定索引处的列<br>data：指定列的索引（必填），删除连续行时索引应该倒序从后往前删除<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：删除的起始位置<br>count：删除多少列 | v4.0及以上 |  |
    | insertRow | {data:[1, 2], dir: 'bottom', copyStyle: false, copySpans: false, si:0} || {data:[{ index: 1, count:2 }], dir: 'bottom', copyStyle: false, si:0} | 在工作表的指定索引处添加一行<br>data：指定行的索引（必填）<br>copyStyle：是否复制指定行的样式，默认复制（可选）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：要复制的行的索引<br>count：要复制多少行<br>dir：向上插入还是向下插入（默认向上如需要向下输入则需要将dir设置为bottom）<br>copySpans：是否复制指定行的融合（这个只能添加行的时候用，列还不支持） | v4.0及以上 |  |
    | insertCol | {data:[1, 3], dir: 'after', copyStyle: false, si: 0} || {data:[{ index:1, count: 2}], dir: 'after', copyStyle: false, si: 0} | 在工作表的指定索引处添加一列<br>data：指定列的索引（必填）<br>copyStyle：是否复制指定列的样式，默认复制（可选）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：要复制的列的索引<br>count：要复制多少列<br>dir：向前插入还是向后插入（默认向前如需要向后输入则需要将dir设置为after） | v4.0及以上 |  |
    | setColumnsWidth | {index:[0], num:20, si: 0,area: 'viewport'} | 设置列宽<br>index：指定列的索引（必填）<br>num：宽度（以像素为单位，必填）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>area：指定区域（可选）<br>area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域 | v4.0及以上 |  |
    | setRowsHeight | {index:[0], num:20, si: 0,area: 'viewport'} | 设置行高<br>index：指定行的索引（必填）<br>num：高度（以像素为单位，必填）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>area：指定区域（可选）<br>area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域 | v4.0及以上 |  |
    | getColumnsWidth | {index:[2,3], callback: 'invokeAction', invokemethod: 'invokemethod', si:'0'} | 获取列宽<br>index：指定行（列）的索引（必填）<br>callback：回调的请求方法名（必填）<br>invokemethod：invokemethod（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | getRowsHeight | {index:[2,3], callback: 'invokeAction', invokemethod: 'invokemethod', si:'0'} | 获取行高<br>index：指定行（列）的索引（必填）<br>callback：回调的请求方法名（必填）<br>invokemethod：invokemethod（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | setRowsVisible | {rows: [1, 3], value: false, si:0} | 设置行可见性rows：行的索引，是一个数组（必填）<br>value：可见性true||false（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | setColumnsVisible | {cols: [2, 4], value: true, si:0} | 设置列可见性cols：列的索引，是一个数组（必填）<br>value：可见性true||false（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | lockCell | [{r:0,c:0,rc:1,cc:1}] || {selections: [{r:0, c:0, rc:1, cc:1}], si:0} | 锁定工作表指定区域的单元格<br>selections：指定表格区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | unlockCell | [{r:0,c:0,rc:1,cc:1}] || {selections: [{r:0, c:0, rc:1, cc:1}], si:1} | 解锁工作表指定区域的单元格<br>selections：指定表格区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | lockSheet | {si: [0] } | 锁定工作表<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | unlockSheet | {si: [1] } | 解锁工作表<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | updataValue | {si:0, area:'', cells: [{r:0, c:0, v:1}]} || [{r:0, c:0, v:1, area:'',}] || {uvJsson} | 更新单元格的值清空值的时候不要用“”要用null;不要用updataValue去更新公式<br>cells：指定单元格的行、列、值（必填）|| dr、dc（字典模式，dr为字典的行标、dc为字典的列标）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>area：指定区域（可选），area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域），默认是单元格区域设置行头区域,列的索引设置为0;设置列头区域,行的索引设置成0;<br>dic：字典模式，此参数不为空时默认开启字典模式，接收一个二维数组<br>uvJson：base64形式的json数据（v6.0.1） | v4.0及以上,<br>字典模式v7.0.3支持 |  |
    | setSpan | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 合并指定区域的单元格<br>range：指定表格区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | setCellStyle | {data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bl:{bls:['dashDot'], blc:['#00f']} } }] ,si:} //设置单元格边框样式<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bkc:'#666', frc:'#000'} }],si:} //设置单元格的前景色和背景色<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{f: '8pt Arial'} }],si:} //设置单元格字体<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{fm:'0.00%'} }],si:} //设置单元格格式<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{va:1, ha:1} }],si:} //设置单元格垂直、水平对齐方式<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{l:true} }],si:} //指示是否将单元格标记为已锁定<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ww:true} }],si:} //设置单元格是否自动换行<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{stf:true} }],si:} //指示内容是否收缩以适应<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ti:2} }],si:} //表示单元格中文本的缩进单元数（一个整数值），其中增量1表示8个像素。<br>{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: 'colHeader', style:{td:} }]} //td的值为数字 设置文本下划线（underline）:1 双下划线（doubleUnderline）:8 删除线（lineThrough）:2 无（none）:0 <br>{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: 'colHeader', style:{ep:true} }]} //ep为一个布尔值 true表示设置文本省略符 文本省略符的悬浮提示是默认存在的（显示文本全部内容），目前（2020.08）没有相关属性。 | 设置单元格样式/格式<br>range：指定表格区域（必填）<br>style：设置样式信息（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>设置单元格边框中的bls表示边框线的样式，可以设置成：dashDot,dashDotDot,dashed,dotted,double,empty,hair,medium,mediumDashDot,mediumDashDotDot,mediumDashed,slantedDashDot,thick,thin。blc表示边框线的颜色。设置行头区域,列的索引设置为0;设置列头区域,行的索引设置成0;rc和cc一般设置成1;设置单元格格式，fm是自定义单元格格式，与excel自定义单元格格式规则相同 | v4.0及以上 |  |
    | fieldInsertRow | {range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 } | 在指定的工作表区域新增行/列，并复制指定行/列的样式<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>styleIndex：复制指定行/列样式的索引（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | fieldInsertCol | {range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 } | 在指定的工作表区域新增行/列，并复制指定行/列的样式<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>styleIndex：复制指定行/列样式的索引（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | fieldDeleteRow | {range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0} | 删除指定的工作表区域的行<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | fieldDeleteCol | {range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0} | 删除指定的工作表区域的列<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | setFListCell | {cell:[{r:1,c:1}], selectType:0, dst:1,lockClickable: true} || {range: [{r:0, c:0, rc:1, cc:1}], selectType:0, dst:1,lockClickable:true} | 设置f7类型单元格<br>selectType：0（支持模糊查询），1（不支持模糊查询）<br>dst：0（默认风格），1（显示风格为下拉）<br>lockClickable：true（支持锁定状态下的点击） | v4.0及以上 |  |
    | setComboCell | [{cell:{r:0,c:0}, option:["a","b","c"]}] | 设置下拉列表类型单元格<br>cell：指定单元格（必填）<br>option：下拉项（必填） | v4.0及以上 |  |
    | setBtnCell | [{cell:{r:0,c:0},text:''}] | 设置按钮类型单元格 | v4.0及以上 |  |
    | setCustomBtnCell | [{cell:{r:0,c:1},text:''}] | 设置自定义按钮类型单元格 | v4.0及以上 |  |
    | setCellTag | [{r: 2, c: 10, st: true, pos:[0,1,2,3], bc:'green', height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true}] || {range: [{r: 2, c: 10, rc: 10, cc: 10}], st: true, pos:[0,1,2,3], bc:'green', height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true} | 给单元格设置特殊标识（小红点）<br>r：指定行（必填）<br>c：指定列（必填）<br>st：控制是否显示小红点（必填）<br>bc：背景颜色，同css颜色设置一致<br>height：标识高度（仅在canClick为true时支持, v6.0.1）<br>width标识宽度（v6.0.1）<br>hoverHeight：鼠标可以覆盖到的高度，也就是实际可点击的高度（v6.0.1）<br>hoverWidth：鼠标可以覆盖到的宽度（v6.0.1）<br>canClick：是否可点击，点击时向后端发送网络请求（v6.0.1）<br>pos：指定小红点的位置，0-3分别代表左上、右上、右下、左下（必填）（v6.0.1） | v4.0及以上 |  |
    | resetCell | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 重置指定工作表区域的单元格类型<br>range：指定工作表区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | getSpreadJson | {callback:'invokemethod', invokemethod:'invokeAction'} | 获得表格bsae64格式文件，回传请求指令和后台触发事件<br>callback：回调的请求方法名（必填）<br>invokemethod：invokemethod（必填）<br>sheetName：sheet页的名称（选填，填写该参数后会获取对应sheet页的json，V7.0支持） | v4.0及以上 |  |
    | setLookupData | {r:0,c:0,data:[]} | 获取f7单元格类型的lookup数据 | v4.0及以上 |  |
    | setCustomFormulaCell | {cell:[{r:0,c:0}], si:0} | 设置自定义的公式单元格 | v4.0及以上 |  |
    | setAllCustomFormulaCell | {si: 0} | 设置所有单元格都为自定义公式单元格 | v4.0及以上 |  |
    | registerCustomFormula | [{  formulaName: 'FACTORIAL',  argsNum: 1,  minArgs: 1,  maxArgs: 1,  returnType: '',  isAsynUpdate: true, defaultValue: 'loading...', evaluateMode: 0, callback: 'autoFitColumn', invokeAction: 'invokeAction', descriptionInfo: {    description: '自定义公式',    parameters: [{      name: ''    }]  }}] | 注册自定义公式<br>isAsynUpdate：是否异步更新<br>defaultValue：默认值<br>evaluateMode：计算模式，0 单元格需要计算时进行重算 1 只计算一次 2 定时计算<br>callback：回传给后端的，用于功能的具体实现<br>invokeAction：回传给后端的，用于处理前端spread所有的请求 | v4.0及以上 |  |
    | setFormula | [{r:0,c:0,f:''}] || {cell: [{r:0,c:0,f:''}], si:0} | 给单元格设置公式<br>cell：指定单元格的行列和公式，f为""则清除公式（必填）<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | getFormula | { callback: 'invokeAction', invokemethod: 'invokemethod', si: 0,options:[{r:0,c:0,rc:10,cc:10}]||'all'} | callback：回调函数<br>invokeMethod：invokeMethod<br>si：指定工作表的索引，默认是当前工作表（可选）<br>options：获取的单元格（all为全部） | v4.0及以上 |  |
    | setSelections | {r:0, c:0, rc:1, cc:1, si:0, position: { row: 'nearest', col: 'nearest' }, autoFocus: true} | 设置指定工作表区域单元格选中（会跳转到指定页签的指定单元格）<br>r：行索引,<br>c：列索引,<br>rc：行选中范围单元格数量,<br>cc：列选中范围单元格数量,<br>si：指定工作表的索引，默认是当前工作表（可选）<br>position：显示的区域相对浏览器的位置（center：中心，left：左边，nearest：最近的边缘，right：右侧）（v6.0.1）<br>autoFocus：是否自动聚焦（v6.0.1） | v4.0及以上 |  |
    | exportExcelFile | {fileName: 'test'} | 导出excel文件fileName：文件名（必填） | v4.0及以上 |  |
    | lockToolbarItems | [{name: 'FontStyle', isLock:  true|false, isHide:true｜false}, group: 'default'] or {isLock: true|false, allowSingleUnlock: true|false} | 锁定工具栏的某些操作，第二种参数是锁定整个工具栏,isLock控制锁定，isHide控制按钮显隐,两个属性共同生效互不影响<br>allowSingleUnlock：仅对全局锁定生效，默认全局锁定后不允许单个解锁，若想要允许单个解锁需加上这个参数并设置为true<br>name：工具栏项对应的key<br>fontFamily（字体）、fontSize（字号）、FontDecoration（文本样式，包含加粗、倾斜、下划线）、ForeAndBackColor（前后背景色）、CellsBorder（设置边框）、textAlignLeft（左对齐）、textAlignCenter（居中对齐）、textAlignRight（右对齐）、VerticalAlign（垂直对齐）、TextIndent（缩进）、WordWrap（自动换行）、MergeCells（合并单元格）、CellFormat（单元格格式）、CellFormatPercentage（百分比）、CellFormatThousand（千分位）、CellFormatDecimalAdd（增加小数位数）、CellFormatDecimalSub（减少小数位数）、FrozenSheets（冻结）、LockCells（锁定）、SwitchView（显示公式）、SortAndFilter（筛选）、ConditionalFormats（条件格式）、Find（查找与替换）、InsertRowAndCol（行列操作）、UploadFile（导入）、ExportFile（导出）、DataValidation（数据有效性）、CheckFormula（检查公式）、Print（打印）、Setting（设置） | v4.0及以上 |  |
    | hideContextMenuItems | {isHide: true/false} or [{name:'pasteOptions' , isHide: true,subMenu: 'pasteValues'}] | 第一种参数格式，直接隐藏整个右键菜单<br>第二种参数格式，隐藏指定的菜单项<br>name：菜单的key<br>isHide：是否隐藏<br>subMenu：子菜单的key，隐藏某个子菜单时，需要同时传递name和subMenu<br>菜单项对应的key可见 [SpreadJS控件右键菜单数据结构](https://vip.kingdee.com/link/s/ZLiTn) | v4.0及以上 |  |
    | setViewOptions | [{type: ,text: ,baseType: }] | 设置当前工作表的视图 | v4.0及以上 |  |
    | print | {index:, showPanel: true, printInfo: [{index:[],info:{}}]} | 打印spread表格<br>showPanel是否显示打印面板 | v4.0及以上 |  |
    | clearCellsStyle | [{r:0,c:0,rc:1,cc:1}] | 清除单元格格式 | v4.0及以上 |  |
    | controlToolbarItems | [{name: 工具栏按钮名称, isCtl: true/false}] | 设置需要跟后端交互的工具栏按钮 | v4.0及以上 |  |
    | setDisplayContent | [{key: ,callback ,invokemethod, text: , s: {w: '宽度', fc: '字体颜色'} }, ...] | 设置展示区域的内容支持点击向后端发送请求，请求内容与右键相似 | v4.0及以上 |  |
    | clearDisplayContent | ['key', ...] | 清除展示区域指定内容 | v4.0及以上 |  |
    | closeToolbar | {} | 关闭工具栏 | v4.0及以上 |  |
    | setExpandBtnVisible | {visible: true|false} | 设置工具栏“展开/收起”按钮的可见性 | v4.0及以上 |  |
    | frozenSheet | {r:0, c:0, tr:0, tc:0, flc:'red', si:0} | 冻结行列<br>r：表示第一条行冻结线所在的位置，默认值0<br>tr：表示第二条行冻结线所在的位置，默认值0<br>c：表示第一条列冻结线所在位置，默认值0<br>tc：表示第二条列冻结线所在的位置，默认值0<br>flc：表示冻结线的颜色，默认值'red'<br>si：表示第几张表，默认值是当前选中的表单 | v4.0及以上 |  |
    | autoFitColumns | {c:[1,3,5], si:0} | 自适应列宽<br>c：列的索引（必填）-1表示所有列<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | autoFitRows | {r:[2,4,6], si:0} | 自适应行高<br>r：行的索引（必填）-1表示所有行<br>si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | setWorkbookOptions | {allowCopyPasteExcelStyle:false, allowExtendPasteRange:false，...}<br>{copyPasteHeaderOptions:allHeaders,...}<br>{defaultDragFillType:auto||...}<br>{showDragFillSmartTag:true,showHorizontalScrollbar:true,showVerticalScrollbar:true,...}<br>{tabStripRatio:0.2}<br>{cutCopyIndi/catorBorderColor:'red'}<br>{backColor:'red'}<br>{backgroundImageLayout:center||center||none||stretch||zoom}<br>{showResizeTip:both||column||none||row}<br>{showScrollTip:both||horizontal||none||vertical}<br>{resizeZeroIndicator:default||enhanced}<br>{autoFitType :cell||cellWithHeader}<br>{referenceStyle:a1||r1c1}<br>{allowAutoCreateHyperlink:true}<br>{allowUserEditFormula: true} | 设置工作簿选项<br>allowCopyPasteExcelStyle：指定用户是否可以从SpreadJS复制样式然后粘贴到Excel，或者从Excel复制样式然后粘贴到SpreadJS，可选。<br>allowExtendPasteRange：指定如果粘贴范围不足以进行粘贴，是否扩展粘贴范围，可选。<br>allowUserDragMerge：是否允许用户拖动合并单元格，可选<br>allowUserDragDrop：指定是否允许用户拖放范围数据<br>allowUserDragFill：指定是否允许用户拖动填充范围<br>allowUserZoom：指定是否在按住Ctrl键的同时滚动鼠标滚轮来缩放显示：allowUserResize：指定是否允许用户调整列和行的大小<br>allowUndo：指定是否允许用户撤消编辑：<br>allowSheetReorder：指定用户是否可以在扩展组件中对图纸进行重新排序<br>allowContextMenu：指定用户是否可以打开内置的上下文菜单：<br>copyPasteHeaderOptions：指定在复制或粘贴数据时要包括的标题（<br>allHeaders复制数据时包括选定的标题；粘贴数据时覆盖选定的标题columnHeaders复制数据时包括选定的列标题；粘贴数据时覆盖选定的列标题<br>noHeaders复制数据时既不包含列标题也不包含行标题；粘贴数据时不会覆盖选定的列或行标题。<br>rowHeaders复制数据时包括选定的行标题；粘贴数据时覆盖选定的行标题）<br>defaultDragFillType：表示拖默认的填充类型（复制单元格：0，以序列方式填充：1，仅填充格式：2，不带格式填充：3，默认：5）<br>showDragFillSmartTag：指定是否显示拖动填充对话框<br>showHorizontalScrollbar：指定是否显示水平滚动条<br>showVerticalScrollbar：指定是否显示垂直滚动条<br>scrollMaxShowMax：指定显示的滚动条是否基于工作表中的全部列和行数<br>scrollbarMaxAlign：指定滚动条是否与活动工作表的最后一行和最后一列对齐<br>tabStripVisible：指定是否显示图纸标签条<br>tabStripRatio：标签条的宽度表示为水平滚动条总宽度的百分比（0到1的小数）<br>tabEditable：指定是否允许用户编辑工作表标签栏<br>newTabVisible：指定电子表格是否显示特殊选项卡，以允许用户插入新的工作表<br>tabNavigationVisible：指定是否显示图纸选项卡导航<br>cutCopyIndicatorVisible：指定在复制或剪切所选项目时是否显示指示器<br>cutCopyIndcatorBorderColor：用户剪切或复制所选内容时显示的指示器的边框颜色<br>rowHeaders复制数据时包括选定的行标题；粘贴数据时覆盖选定的行标题）<br>defaultDragFillType：表示拖默认的填充类型（复制单元格：0，以序列方式填充：1，仅填充格式：2，不带格式填充：3，默认：5）<br>showDragFillSmartTag：指定是否显示拖动填充对话框<br>showHorizontalScrollbar：指定是否显示水平滚动条<br>showVerticalScrollbar：指定是否显示垂直滚动条<br>scrollMaxShowMax：指定显示的滚动条是否基于工作表中的全部列和行数<br>scrollbarMaxAlign：指定滚动条是否与活动工作表的最后一行和最后一列对齐<br>tabStripVisible：指定是否显示图纸标签条<br>tabStripRatio：标签条的宽度表示为水平滚动条总宽度的百分比（0到1的小数）<br>tabEditable：指定是否允许用户编辑工作表标签栏<br>newTabVisible：指定电子表格是否显示特殊选项卡，以允许用户插入新的工作表<br>tabNavigationVisible：指定是否显示图纸选项卡导航<br>cutCopyIndicatorVisible：指定在复制或剪切所选项目时是否显示指示器<br>cutCopyIndicatorBorderColor：用户剪切或复制所选内容时显示的指示器的边框颜色 | v4.0及以上 |  |
    | setWorksheetOptions | [{si:, options: {allowCellOverflow: false}}]<br>[{si:, options: {isProtected: true || false}}]<br>[{si:, options: {clipBoardOptions: 0-3}}]<br>[{si:, options: {sheetTabColor: 'red'}}]<br>[{si:, options: {frozenlineColor: 'red'}}]<br>[{si:0, options: {gridline:{color:'red',showVerticalGridline:false,showHorizontalGridline:false}}}]<br>[{si:, options: {rowHeaderVisible/colHeaderVisible: true||false}}]<br>[{si:, options: {rowHeaderAutoText/colHeaderAutoText: blank||letters||numbers}}]<br>[{si:, options: {rowHeaderAutoTextIndex/colHeaderAutoTextIndex:0}}]<br>[{si:, options: {rowHeaderAutoTextIndex/colHeaderAutoTextIndex:0}}]<br>[{si:0, options: {protectionOptions:{allowSelectLockedCells:true||false, ...}}}]<br>[{si:, options: {selectionBackColor:'red'}}]<br>[{si:, options: {selectionBorderColor:'red'}}]<br>[{si:0, options: {sheetAreaOffset:{left:0,top:0}}}] | 设置工作表选项<br>si：工作表的索引，默认值是当前选中的表单<br>allowCellOverflow：允许单元格溢出<br>isProtected：设置表单保护状态<br>clipBoardOptions：设置spread的ctrlV粘贴选项，0（默认）,1（值）,2（格式）,3（公式）<br>sheetTabColor：设置工作表标签颜色,支持RGB,16进制等表示方法<br>frozenlineColor：设置工作表冻结线颜色,支持RGB,16进制等表示方法<br>gridline：设置工作表网格线：color：颜色<br>showVerticalGridline：垂直方向的网格线：true：显示：false：不显示。默认不显示<br>colHeaderVisible/rowHeaderVisible：指定行/列标头是否可见<br>rowHeaderAutoText/colHeaderAutoText：指定行/列标头显示blank：空白：letters：字母：number：数字<br>rowHeaderAutoTextIndex/colHeaderAutoTextIndex：指定当有多个行/列标题行时,哪个行/列标题行显示自动文本<br>protectionOptions：指定用户能够更改的元素（allowSelectLockedCells：是否可以选择锁定的单元格<br>allowSelectUnlockedCells：是否可以选择未锁定的单元格<br>allowSort：是否可以对范围进行排序<br>allowFilter：是否可以过滤范围<br>allowEditObjects：是否可以编辑浮动对象<br>allowResizeRows：是否可以调整列的大小<br>allowDragInsertRows：是否可以执行拖动操作以插入行<br>allowDragInsertColumns：是否可以执行拖动操作以插入列<br>allowInsertRows：是否可以插入行<br>allowInsertColumns：是否可以插入列<br>allowDeleteRows：是否可以删除行<br>allowDeleteColumns：是否可以删除列）<br>selectionBackColor：指定工作表的背景颜色<br>selectionBorderColor：指定工作表的边框颜色<br>sheetAreaOffset：设置工作表的偏移量（number类型）<br>rowHeaderVisible/colHeaderVisible：设置行/列头的可见性 | v4.0及以上 |  |
    | setShortcutKey | {commandName:'ctrlA', key:65, ctrl:true, shift:false, alt:false, meta:false} | 注册快捷键 | v4.0及以上 |  |
    | setCornerMark | [{ range: [{r:0, c:0, rc:1, cc:4}], vi: true|false, text: 'T', bc: 'green', fc: 'white', pos: [0,1 ] }] | 给单元格设置角标range：范围<br>vi：可见性text：角标上的文字<br>bc：背景色，角标的颜色fc：前景色，文字的颜色pos：角标的位置，0，1，2，3分别是左上、右上、右下、左下 | v4.0及以上 |  |
    | addContextMenuItems | {callback: 'invokeAction',subMenu:[{name:'',text:'',...}], items: [{name: 'insertRowsBehind', text: '向后插入行', workArea: ['viewport', 'colHeader', 'rowHeader', 'slicer', 'corner', 'sheetTab']}]} | 添加右键菜单项<br>callback：回调函数<br>name：菜单项的key<br>text：菜单项文本<br>workArea：新增菜单项的作用区域，viewport表示数据区域，colHeader表示列头，rowHeader表示行头，slicer表示切片区域，corner表示角标区域sheetTab 表示页签区域<br>subMenu 二级菜单、 三级菜单<br>type：当需要设置分割线时，只需要设置该值为'separator' | v4.0及以上 | text是唯一id，若加入与原有菜单相同的text可能会引起异常设置分割线：sa.action.addContextMenuItems（{items: [{type: 'separator'}]}, sa.t） |
    | getRangeValues | {range: [{r:0, c:0, rc:1, cc:1}],compression:  false} | 获取指定范围的值 <br>compression true表示压缩后上传 false表示和原来的使用方式一样<br>压缩成了base64的格式,解压后是一个一纬数组 在数组的最后 会把{r:, c:, rc:, cc:}这个放进去  这个就是你获取的范围 比如你调起前端的指令 {r:2, c:2, rc:9, cc:9} 前端会把81个单元格的信息传给后端 如果想获取六行七列单元格的值 则对应数组下标 cc*（6-r）+7-c = 41 向后端发送的指令也发生了改变, invokeControlMethodOnly（model, 'invokeAction', [{ si, data: valueBase64, invokemethod: 'rangeValues' }]） | v4.0及以上 |  |
    | setVirtualMode | { callback: 'invokeAction', invokemethod: 'invokemethod', isOpen: true, unRepeatRequest: false } | 设置虚模式加载，开启之后，表格在第一次加载和每次滚动的时候会把当前的可是区域发送给后端<br>callback: 回调的请求方法名（必填）<br>invokemethod: invokemethod（必填）<br>isOpen: 用于开启和关闭虚模式加载，设置成false则停止发送请求，默认值是true（选填）<br>unRepeatRequest：不重复发送网络请求，默认为false，也就是滚动区域也会重复发送网络请求（v7.0.1） | v4.0及以上 |  |
    | setOutlineColumn | {c:0, options: {showCheckBox:false, maxLevel:2, collapsed: true}, si:} | 设置分组列折叠<br>c: 列的索引，<br>options是可选项，<br>showCheckBox设置是否显示复选框，<br>maxLevel控制数据分层级别。默认值是10<br>collapsed设置默认折叠<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>分组列不能和虚模式一起使用 | v4.0及以上 |  |
    | setRowOutlines | {groups: [{index:1, count:5, isHide: false}], si:0} | 设置行的区域分组，从指定的起始索引将行的范围分组到大纲（范围组）中。<br>groups: 支持传一个或多个分组信息（必选）<br>index: 要分组起始索引（必选）<br>count: 要分组的起始行或列的数目（必选）<br>si: 指定页签的索引，默认是当前工作表（可选）<br>isHide: 显示/隐藏分组（可选）（v6.0.1） | v4.0及以上 |  |
    | setColumnOutlines | {groups: [{index:1, count:5, isHide: false}], si:1} | 设置列的区域分组，从指定的起始索引将列的范围分组到大纲（范围组）中。<br>groups: 支持传一个或多个分组信息（必选）<br>index: 要分组起始索引（必选）<br>count: 要分组的起始行或列的数目（必选）<br>si: 指定页签的索引，默认是当前工作表（可选）<br>isHide: 显示/隐藏分组（可选）（v6.0.1） | v4.0及以上 |  |
    | expandRowOutlines | {levels: [0,1], expand: false, si:0} | 使用指定的级别展开或收起大纲（范围组）<br>levels: 分组的级别，从0开始（必选）<br>expand: 是否展开分组（必选）<br>si: 指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | expandColumnOutlines | {levels: [0,1], expand: false, si:1} | 使用指定的级别展开或收起大纲（范围组）<br>levels: 分组的级别，从1开始（必选）<br>expand: 是否展开分组（必选）<br>si: 指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |  |
    | addSheets | {sheets: [{index:1, name:'ssss', rc:100, cc:10}]} | 在指定索引处插入一个工作表<br>sheets: 支持传一个或多个工作表的信息（必选）<br>index: 用来添加工作表的索引（必选）<br>name: 工作表的名称（可选）<br>rc: 工作表的初始行数，默认是200（可选）<br>cc: 工作表的初始列数，默认是20（可选） | v4.0及以上 |  |
    | deleteSheets | {index: [2,3]} | 在指定索引处删除工作表<br>index：要删除的工作表索引 | v4.0及以上 |  |
    | setSheetsVisible | {index: [1,2], visible: false} | 设置工作表的可见性<br>index: 要显示/隐藏的工作表索引<br>显示时index可选 会按照顺序显示隐藏的一个sheet<br>vidible: true（显示）false（隐藏） | v4.0及以上 |  |
    | getRangeValues | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 设置工作表的可见性<br>index: 要显示/隐藏的工作表索引<br>显示时index可选 会按照顺序显示隐藏的一个sheet<br>vidible: true（显示）false（隐藏） | v4.0及以上 |  |
    | setComment（设置批注） | [{ options: [{ r: 0, c: 0, v: '这是一个批注', remove: false }, { r: 1, c: 1, v: '', remove: true }], si: 0 }] （注意参数是个数组） | 设置批注<br>remove:true（清除批注）/false（设置批注时可以不传）<br>v | v4.0及以上 |  |
    | copyCellsTo（复制指定区域的样式到另一个区域） | {range: [{fromRow: 0, fromCol: 0, toRow: 1, toCol: 1, rc: 1, cc: 1}], si:0, type: 0,useSpreadApi: true, isUndo: true} | 复制指定区域的样式到另一个区域<br>range: [{<br>  fromRow: 来源行<br>  toRow: 目标列<br>  formCol: 来源列<br>  toCol: 目标列,<br>}]<br>si: sheetIndex（单元表索引）<br>type: 0：粘贴所有 | 1：粘贴值 | 2：粘贴样式<br>useSpreadApi: 是否使用spread复制API（spread复制API的复制逻辑与手动复制粘贴的逻辑一样，可以实现公式更新）（v6.0.1）<br>如果想复制整行，可以把对应的fromCol设置成-1 对应的复制整列可以把fromRow设置成-1<br>（源和目标两片区域大小必须一样所以rc和cc指的既是from也是to的行数或列数）<br>例如，将第一行的数据复制到第二行：sa.action.copyCellsTo（{range: [{fromRow: 0, fromCol: -1, toRow: 1, toCol: -1, rc: 1, cc: 20}], si:0, type: 0}, sa.t） | v4.0及以上 | 使用spread的复制api时，复制整行可以使用简洁指令，不需要去获取一行有多少列，例如：sa.action.copyCellsTo（{range: [{fromRow: 0, fromCol: -1, toRow: 1, toCol: -1, rc: 1, cc: -1}], si:0, type: 0,useSpreadApi: true, isUndo: true}, sa.t） |
    | setRowFilter（设置过滤） | [{ r: 4, c: 2, rc: 8, cc: 9, si: 0 }]（注意参数是个数组） | 设置过滤区域<br>r:起始行的索引<br>c:起始列的索引<br>rc:要设置行的个数<br>cc:要设置列的个数 | v4.0及以上 |  |
    | setHyperLinkCell（设置超链接） | { callback:, invokemethod:, si:, range: [{r:1, c:1, rc: 10, cc:1, options:{text:'超链接',value:'[https://www.baidu.com](https://www.baidu.com',color:'red',visitedColor:'blue',toolTip)[',color:'red',visitedColor:'blue',toolTip](https://www.baidu.com',color:'red',visitedColor:'blue',toolTip): '超链接' }}] } | callback: 回调的请求方法名（必填）<br>invokemethod: invokemethod（必填）<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>range:区域范围（必填）<br>options:超链接相关（可选） {<br>text:单元格显示的内容<br>value:点击超链接跳转的地址（不传时会向后端发送指令）SpreadJS的超链接支持许多协议，例如：http / https / ftp / file / mailto...。此外，还支持以“ sjs：//”开头的url，它引用工作表位置如sjs://Sheet1!A1:B2 也支持邮件的跳转<br>color:超链接访问前的前景色<br>visitedColor:超链接访问后的前景色<br>toolTip:表示超链接的提示消息，当鼠标悬停在带有超链接的单元格上时显示。 | v4.0及以上 |  |
    | setSheetName 设置表名 | [{oldName:'sheet1',si:0,newName:'资产负债表'}] | oldName要设置的表的名称<br>si要设置的表的索引<br>newName表的新名称<br>oldName与si都可不传 | v4.0及以上 |  |
    | setZoom（设置缩放比例） | {zoom:0.25~4} | 默认1 | v4.0及以上 |  |
    | setSheetDefaults（设置spread默认配置） | { si, setting: { rowHeight: value，... } } | si要设置的表的索引<br>setting 配置<br>rowHeight：行高<br>colWidthl : 列宽 | v4.0及以上 |  |
    | addToolbarItems（向工具栏里添加自定义按钮） | 自定义按钮为单个按钮<br>[{<br>        key: 'CheckFormula2', // 唯一标识<br>        icon: 'kdfont kdfont-shanchuxing', // 字体图标<br>        title: {<br>          zh_CN: '中文',<br>          zh_TW: '繁体',<br>          en_US: 'english'<br>        },         <br>        showTitle: true,<br>        callback: 'sss',<br>        invokeAction: 'sssss'<br>}]<br>自定义按钮为下拉菜单<br>[{<br>        key: 'CheckFormula0', // 唯一标识<br>        icon: 'kdfont kdfont-shanchuxing', // 字体图标<br>        dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击<br>        title: {<br>            zh_CN: '中文',<br>            zh_TW: '繁体',<br>            en_US: 'english'<br>        }, <br>        showTitle: true,<br>        item: [ // 下拉项<br>          {<br>            title:{<br>              zh_CN: '中文',<br>              zh_TW: '繁体',<br>              en_US: 'english'<br>            } ,<br>            icon: 'kdfont kdfont-guolvpaixu', // 字体图标，<br>            callback: 'sss',<br>            invokeAction: 'sssss',<br>            key: 'AutoFitColumn0123',           }<br>        ]<br>}] | key ：按钮标识<br>icon： 字体图标<br>title： tips或下拉项的内容，为兼容国际化，建议传对象形式的参数，包含三种语言显示的文字，也可直接传字符串<br>callback 后端具体实现的类<br>invokeAction 后端同于同一处理spread的接口<br>showTitle: 是否显示标题文字<br>item: 下拉选项 | v5.0及以上 |  |
    | setActiveSheet（切换页签） | {name:'',si:''} | 激活/选中指定页签<br>name：要切换页签的名字<br>si：要切换页签的索引<br>这两个传一个就行 | v4.0及以上 |  |
    | excuteToolbarActions | {name: 'SwitchView',  // 按钮标识itemName: 'SwitchView_ValueView' // 执行的按钮活动} | name: 按钮标识<br>itemName:  执行的按钮活动 | v5.0及以上 |  |
    | setToolbarGroups（自定义工具栏分组） | [{<br>  name: '默认1',<br>  key: 'default1',<br>  toolbarItems: [{key: 'Undo'},{key: 'Undo'}， // 默认功能<br>   {<br>    key: 'CheckFormula0', // 唯一标识<br>        icon: 'kdfont kdfont-shanchuxing', // 字体图标<br>        title: { zh_CN: '中文', zh_TW: '繁体', en_US: 'english' }, <br>        showTitle: true,<br>        items: [ // 下拉项<br>          {<br>            title:{ zh_CN: '中文',zh_TW: '繁体',en_US: 'english' } ,<br>            icon: 'kdfont kdfont-guolvpaixu', // 字体图标，<br>            callback: 'sss',<br>            invokeAction: 'sssss',<br>            key: 'AutoFitColumn0123'          }<br>       ]<br>   },<br>  ],<br> }] | name: 需要显示的分组名称<br>key: 分组唯一的key值，用于辨识分组，不可重复<br>toolbarItems: 工具栏需要显示的按钮，数组顺序即按钮顺序。自定义功能参数与添加自定义按钮（addToolbarItems）相同。需要注意的是，自定义添加了一个新参数，isCustom，如果是自定义按钮这个参数必填true<br>提示：默认情况下，默认分组一直存在，因此自定义工具栏分组时，如果不需要修改默认分组，则不需要传默认分组的参数，只需要传扩展分组，会自动将扩展分组添加到分组中。传递包含默认分组的参数则视为修改默认分组。 | v5.0及以上 |  |
    | setToolbarCustomExpend（自定义公式扩展区域） | {itemConfig:{<br>        showTitle: true,<br>        key: 'CheckFormula0', // 唯一标识<br>        icon: 'kdfont kdfont-shanchuxing', // 字体图标<br>        title: { zh_CN: '测试',zh_TW: '繁体',en_US: 'english' },<br>        callback: 'autoFitColumn', // 回传给后端的，用于功能的具体实现<br>        invokeAction: 'invokeAction', // 回传给后端的，用于处理前端spread所有的请求,<br>        dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击<br>        items: [ // 下拉项<br>          {<br>            title:{zh_CN: '测试',zh_TW: '繁体',en_US: 'english'},<br>            callback: 'sss',icon: 'kdfont kdfont-guolvpaixu', // 字体图标，<br>            invokeAction: 'sssss',<br>            key: 'AutoFitColumn0123',           }<br>        ]}, <br>inputConfig: {<br>   callback: 'autoFitColumn', // 回传给后端的，用于功能的具体实现<br>   invokeAction: 'invokeAction'<br>}} | itemConfig：自定义按钮配置，与自定义按钮相似inputConfig：自定义输入配置 | v5.0及以上 |  |
    | setToolbarCustomInputValue（与setToolbarCustomExpend搭配使用，自定义公式扩展区域输入值） | string' |  | v4.0及以上 |  |
    | setCustomSheetMenu（自定义页签菜单右键顺序） | [{name: 'hideSheet'}, {type: 'separator'}, { name: 'setBGC' }, {name: 'unhideSheet'}, {name: 'insertRowsBehind'} ,{name: 'insertSheet'}] | name: 菜单名称<br>（insertSheet：插入，deleteSheet：删除，hideSheet：隐藏，unhideSheet：显示，setBGC：颜色选择器）<br>type: 只有分割线才使用type（type: 'separator'） | v4.0及以上 |  |
    | getLockedCells（获取锁定单元格） | [{ callback: 'invokeAction', invokemethod: 'invokemethod', si: 0, compression: false }] | callback：回调函数<br>invokeAction：后端同一处理spread的接口<br>si：sheetIndex<br>compression：是否压缩 | v4.0及以上 |  |
    | hideRow（隐藏行）/unhideRow（显示行） | {data: [1, 2], si: 0, isUndo: true} ||{data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似 | 在工作表的指定索引处添加一行/列<br>data：指定行（行）的索引（必填）<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>index: 要隐藏/显示的行的索引<br>count: 要隐藏/显示多少行 | v4.0及以上 |  |
    | hideColumn（隐藏列）/unhideColumn（显示列） | {data: [1, 2], si: 0, isUndo: true} ||{data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似 | 在工作表的指定索引处添加一行/列<br>data：指定行（列）的索引（必填）<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>index: 要隐藏/显示的列的索引<br>count: 要隐藏/显示多少列 | v4.0及以上 |  |
    | setSpreadParams | {data: {key: value}} | 添加全局spread属性（也可直接通过JSON设置，在spread新增一个自定义属性serverSpreadParams，将需要全局定义的属性作为对象赋值给serverSpreadParams）<br>key：属性名<br>value：属性值 | v5.0.027及以上 | 目前已有的属性：<br>isEditSendRequest：鼠标进入编辑态与退出编辑态时是否向后端发送网络请求（5.0.0.27）<br>allowMaxInsert9999：插入行列支持最大输入9999（v6.0.1）<br>useNewInsert：使用新版插入（向上插入/向下插入）（v6.0.1）<br>usePtFontSize：使用pt作为font-size的单位，默认是px（v6.0.7）<br>useSheetPrintConfig：按工作表保存打印配置（v6.0.12）<br>useExcelCellsFormat：使用与excel同步的单元格格式（v6.0.14）<br>isUpdateStyleSendRequest：样式更新时发送网络请求（v7.0.0）<br>isDeleteLockedSendRequest：delete快捷键清除锁定单元格数据时向后端发送网络请求（v7.0.4） |
    | setGCParams | {data: {key: value}} | 添加全局GC属性（与setSpreadParams不同的是，这个是GC层面的属性，设置之后不会随着json的设置而覆盖，建议本条指令在所有指令之前设置）<br>key：属性名<br>value：属性值 | v6.0及以上 | 目前已有的属性：calcPrecision：计算精度，默认为14，当出现精度问题时，可以调整为12。不建议调整其他值，可能会出现计算问题（v6.0.1） |
    | hideZero | {ishide: true, si: 0} | ishide：为0时是否隐藏<br>si：sheetIndex | v4.0及以上 | 如果表格中已经存在0值，指令开启后需要重新计算才能将已有的0隐藏 |
    | setGroupColNode（设置分组列） |  [{r:number, c:number,  groupNodeType:1||2||3, vAlign:bumber, drill:1||0, radix:number, spaceRight: number, isVertical: false}] | r:number, （行坐标）<br>c:number, （列坐标）<br>groupNodeType :1||2||3, （1: 收起状态，2：展开状态，3：空）<br>vAlign:bumber, （距离左侧的位置）<br>drill:1||0, （1：有图片，2：无图片）<br>radix:number,  （基数，与vAlign共同计算左侧距离）<br>spaceRight: number, （分组号距离右侧的距离）<br>isVertical: false （是否垂直分组） | v4.0及以上 | 没有直接的接口去取消分组，可以手动将groupNodeType 设置为0， drill设置为2 |
    | setStatusBarVisible | {visible: false} | 设置spread底部缩放的显示（true）与隐藏（false）<br>{visible: true|false} | v4.0及以上 |  |
    | setValidator | {type: 0, option: {vt: 1, v1: '1', vco: 0, ht: 0, hsc: '#ff0000', es: 0, et: '不允许输入'}, range: [{row: 0, col: 0, rowCount: 5, colCount: 5}], callback: 'invokeAction', invokemethod: 'test', si: 0} | type: number （0:设置, 1:取消）<br>option: object （配置项，详见备注）<br>range: array （应用的单元格）<br>callback: 回调函数<br>invokemethod: 回调函数<br>si: sheetindex | v4.0及以上 | VALIDATOR_TYPE = 'vt' // 验证器<br>IS_FORMULA_LIST_VALIDATOR = 'iflv'//<br>VALIDATOR_COMPARISON_OPERATOR = 'vco'// 比较运算符<br>VALUE_1 = 'v1'// 值一<br>VALUE_2 = 'v2'// 值二<br>IS_INTEGER = 'ii' // 是否是整数<br>SHOW_INPUT_MSG = 'sim'// 显示输入消息<br>INPUT_TITLE = 'it'// 标题,<br>INPUT_MESSAGE = 'im'// 信息<br>IGNORE_BLANK = 'ib' // 忽略空白<br>SHOW_ERROR_MESSAGE = 'sem' // 显示错误信息<br>ERROR_STYLE = 'es'// 错误类型<br>ERROR_TITLE = 'et'// 错误标题<br>ERROR_MSG = 'em' // 错误信息<br>HIGHLIGHT_TYPE = 'ht' // 圆圈样式<br>DOGEAR_POSITION = 'dp'// 位置 与圆圈样式有关  类型dogear才会用到<br>ICON_POSITION = 'ip'// 位置 与圆圈样式有关  类型icon才会用到<br>HIGHLIGHT_STYLE_COLOR = 'hsc'// 颜色<br>HIGHLIGHT_STYLE_IMAGE = 'hsi'// 图标 与圆圈样式有关  类型icon才会用到 |
    | batchExportExcelFiles | base64加密过的对象：{data:[{json:''},...], fileName:'fileName'} | json：spread的json文件，注意，这里是原生的json字符串，不是通过base64加密过的<br>fileName：导出的文件名称 | v4.0及以上 |  |
    | syncSpreads | { slaveSpreadKeys: [key2, key3], syncOptions: [scrollBar, rowHeight, colWidth, zoom], sync: true } | 多表联动，仅仅对各个当前活动的工作表进行联动处理<br>slaveSpreadKeys：需要联动的spread的key<br>syncOptions：需要联动的属性，目前只支持scrollBar（滚动）, rowHeight（行高）, colWidth（列宽）, zoom（缩放）<br>sync：是否联动，如果已经联动的填写这个参数用于关闭联动 | v6.0.15及以上 |  |
    
    # 7 插件示例
    ```java
    public class Spread extends Container{
    
        private SpreadPostDataInfo postData ;
        //删除行
        public void deleteRows(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().deleteRows(new SpreadEvent(this,postData));
        }
        //删除列
        public void deleteColumns(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().deleteColumns(new SpreadEvent(this,postData));
        }
        //插行
        public void addRows(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().addRows(new SpreadEvent(this,postData));
        }
        //插列
        public void addColumns(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().addColumns(new SpreadEvent(this,postData));
        }
        //切换单元格
        public void entryRowClick(int arg){
            getActionService().selectedSpread(new SpreadEvent(this,postData));        
        }
        //前端通用指令
        public void askExecute(LinkedHashMap<String,Object> postDatas){
            packInvokeParams(postDatas);
            getActionService().askExecute(new SpreadEvent(this,postData));
        }
    }
    
    ```
    插行示例：
    前端控制台模拟后端指令
    ![3.png](/download/01004a82934bef0d4b92b3699ad2294ba4ce.png)
    
    # 8 参考链接
    
    1. [SpreadJS 控件常见问题](https://vip.kingdee.com/link/s/ZLiPJ)
    
     

# 变更记录

产品版本 | 更新内容 | 更新日期  
---|---|---  
V5.0.024 | 新增了Spread 表格参数设置后点击确定可保存表格参数的功能，满足了用户需要记住表格参数设置的需求，详见5.1.2 | 2023年7月  
V5.0.025 | Spread表格支持了设定选中区域指令的位置参数，满足了用户希望将选定区域放置在指定的可视范围的需求，详见6：setSelections | 2023年7月  
V6.0.15 | 完善了Spread多表联动的功能，支持多表同步滚动条、行高、列宽、缩放，提高用户设置报表模板的易用性，详见6：syncSpreads | 2024年7月  
v7.0.1 | 1、getSpreadJson与setSpreadJson现在支持对单个工作表（sheet）进行操作，详见6：getSpreadJson、setSpreadJson  
2、出于第三方商业合规性要求，伙伴/二开环境的表单设计器中将不再内置SpreadJS控件，详见1：下架说明 | 2024年10月  
v7.0.3 | 1、updatavalue支持字典模式传输数据，降低传输数据量，提升性能，详见6：updatavalue  
2、优化了SpreadJS表格设置列格式时数据导入的性能，避免页面卡顿 | 2024年12月  
V7.0.4 | setSpreadParams新增isDeleteLockedSendRequest参数：delete快捷键清除锁定单元格数据时向后端发送网络请求，可二开实现框选单元格包含锁定单元格时，仅清除非锁定单元格的数据，详见6：setSpreadParams | 2024年12月  
V8.0.1 | 升级了Spread控件版本，增强了相关功能并修复了若干问题，满足不同客户的使用场景。注意：如果后端开发用到了GCExcel组件，则为了前后端组件版本适配，需要同步升级后端GCExcel组件，详见5.2 | 2025年9月  
  
# 1 功能介绍

SpreadJS 是一款基于 HTML5 的纯前端电子表格控件，提供了与 Excel 高度类似的功能和兼容性。  
该控件仅在PC端支持。

## 下架说明

出于第三方商业合规性要求，SpreadJS 控件的使用权限严格限定于本公司旗下标品业务（包括但不限于“星空”、“星瀚”系列），若在二开中继续使用，需自行承担潜在的商业风险。  
从苍穹 7.0.1 版本开始，针对伙伴或二开的苍穹环境在其表单设计器中隐藏 SpreadJS 控件，在控件面板中无法选到该控件参与布局设计。若客户有在线使用类似 SpreadJS 控件的需求，我们建议：

  * 自行采购 SpreadJS 官方授权，并集成到项目中使用
  * 选择其他功能类似的第三方表格控件进行集成



# 2 控件对象

`kd.bos.form.spread`

# 3 视觉展示

![1.png](https://vip.kingdee.com/download/0100f9b26b4809864739b9609ec460d642f6.png)

# 4 属性说明

## 4.1 通用属性

> 通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)

## 4.2 样式属性

> 样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)

## 4.3 业务属性

属性名 | 类型 | 运行时参数名 | 默认值 | 说明 | PC  
---|---|---|---|---|---  
显示编辑工具栏 | boolean | setb | true | 是否默认显示工具栏，开启时将自动展开，关闭时则自动收起。无论默认状态如何，随时可以手动展开或收起工具栏 | V4.0及以上  
允许多页签 | boolean | smt | false | 是否开启多页签模式，开启后控件底部将显示页签栏，支持快速切换和新增。导入功能也与该属性相关，开启时，允许同时选择多个sheet导入，关闭时仅允许选择单个sheet导入 | V4.0及以上  
支持导出excel | boolean | ee | false | 是否支持批量导出，开启后，控件将支持批量导出功能。可以使用 batchExportExcelFiles 指令，将多个 Spread 的数据合并导出为一个 Excel 文件。适用于列表页面，需要批量导出多个Spread数据的场景 | V4.0及以上  
  
# 5 功能详情

## 5.1 工具栏

### 5.1.1工具栏功能介绍

spread工具栏配置了表格常用的一些功能

  * 对于单元格样式的一些设置：字体样式、字体大小、字体颜色、文本居中、单元格背景色
  * 对于单元格格式的一些设置：常规、日期、时间、会计专用等等
  * 对于工作表的一些设置：冻结表格、过滤表格、打印、显示公式等等



以往的文章中我们已经对这些特性进行过介绍  
链接直达：  
[Spread工具栏新特性](https://vip.kingdee.com/article/390596666833372416)  
[Spread 全新视觉交互升级](https://developer.kingdee.com/article/502501692391512576)  
[Spread支持查找替换功能](https://developer.kingdee.com/article/85798461483727872)  
[Spread支持数据有效性](https://developer.kingdee.com/article/322746947583383040)  
[Spread支持打印](https://developer.kingdee.com/article/137514916042362624)  
[Spread支持自定义单元格格式](https://developer.kingdee.com/article/324229535094933760)  
[Spread支持条件格式](https://developer.kingdee.com/article/523501187660638464)

### 5.1.2表格设置

设置表格配置参数后（功能入口：表格工具栏的配置按钮），点击确定，可将参数保存到当前表格，该参数只对当前表格生效。再次打开该表格或其他用户使用该表格，该参数依旧生效。  
![2.png](https://vip.kingdee.com/download/010012cffaed11c044ac9662e9dfc8e96a88.png)

## 5.2 SpreadJS 版本升级

鉴于在过往客户反馈中许多问题需通过升级 SpreadJS 版本解决，为提升产品稳定性与功能性，我们在苍穹平台 8.0.1 版本中，正式将内置的 SpreadJS 第三方控件从 V14.2.6 升级至 V17.1。  
升级影响与必要操作：

  * 如果您的项目后端使用了 GCExcel 组件，必须将其同步升级至 V7 版本，以确保前后端功能一致性与稳定性；
  * 为了提升存储何计算性能，SpreadJS 第三方组件设计了共享公式，当存在两个相同的公式时将提取到sharedFormulas中，直接解析 SpreadJson 数据获取公式可能获取正确的公式。官方建议后端通过 GCExcel 的 api 去获取和改动这些信息；
  * 除了公式，由于 SpreadJS 产品版本持续在升级，SpreadJson 中的数据结构很有可能发生变化，建议都通过 GCExcel 的 api 执行操作；



版本详情：有关 V17.1 版本解决的具体问题与新特性，请参阅官方发布说明：  
[17.1 < 发布说明 | 葡萄城 SpreadJS 表格控件在线文档](/tolink?target=https://demo.grapecity.com.cn/spreadjs/help/docs/rnotes/171)

# 6 SpreadJS接口介绍

spread功能高度依赖业务，因此部分功能需要后端配置响应的指令才能正常使用，比如插行、删行。

指令 | 参数 | 功能 | 支持版本 | 备注  
---|---|---|---|---  
callbackAction | {callback:‘invokemethod’, invokemethod:‘addRows’} | 回调函数指令，后端告诉前端它还有后续动作 | v4.0及以上 |   
setSpreadJson | spreadJS压缩过后的json串 | 加载spreadJS的json串  
支持加载sheet的JSON字符串（V7.0） | v4.0及以上 |   
appendRows | {count:1, si:0} | 在指定工作表尾部追加count行  
count：行数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
appendCols | {count:1, si:0} | 在指定工作表尾部追加coun列  
count：列数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
deleteRow | {data:[2, 1], si:0} |  | {data: [{index:1, count: 2}], si:} | 删除工作表指定索引处的行  
data：指定行的索引（必填），删除连续行时索引应该倒序从后往前删除  
si：指定工作表的索引，默认是当前工作表（可选）  
index：删除的起始位置  
count：删除多少行  
deleteCol | {data:[2, 1], si:0} |  | {data: [{index:1, count: 3}], si:} | 删除工作表指定索引处的列  
data：指定列的索引（必填），删除连续行时索引应该倒序从后往前删除  
si：指定工作表的索引，默认是当前工作表（可选）  
index：删除的起始位置  
count：删除多少列  
insertRow | {data:[1, 2], dir: ‘bottom’, copyStyle: false, copySpans: false, si:0} |  | {data:[{ index: 1, count:2 }], dir: ‘bottom’, copyStyle: false, si:0} | 在工作表的指定索引处添加一行  
data：指定行的索引（必填）  
copyStyle：是否复制指定行的样式，默认复制（可选）  
si：指定工作表的索引，默认是当前工作表（可选）  
index：要复制的行的索引  
count：要复制多少行  
dir：向上插入还是向下插入（默认向上如需要向下输入则需要将dir设置为bottom）  
copySpans：是否复制指定行的融合（这个只能添加行的时候用，列还不支持）  
insertCol | {data:[1, 3], dir: ‘after’, copyStyle: false, si: 0} |  | {data:[{ index:1, count: 2}], dir: ‘after’, copyStyle: false, si: 0} | 在工作表的指定索引处添加一列  
data：指定列的索引（必填）  
copyStyle：是否复制指定列的样式，默认复制（可选）  
si：指定工作表的索引，默认是当前工作表（可选）  
index：要复制的列的索引  
count：要复制多少列  
dir：向前插入还是向后插入（默认向前如需要向后输入则需要将dir设置为after）  
setColumnsWidth | {index:[0], num:20, si: 0,area: ‘viewport’} | 设置列宽  
index：指定列的索引（必填）  
num：宽度（以像素为单位，必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
area：指定区域（可选）  
area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域 | v4.0及以上 |   
setRowsHeight | {index:[0], num:20, si: 0,area: ‘viewport’} | 设置行高  
index：指定行的索引（必填）  
num：高度（以像素为单位，必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
area：指定区域（可选）  
area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域 | v4.0及以上 |   
getColumnsWidth | {index:[2,3], callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si:‘0’} | 获取列宽  
index：指定行（列）的索引（必填）  
callback：回调的请求方法名（必填）  
invokemethod：invokemethod（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
getRowsHeight | {index:[2,3], callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si:‘0’} | 获取行高  
index：指定行（列）的索引（必填）  
callback：回调的请求方法名（必填）  
invokemethod：invokemethod（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setRowsVisible | {rows: [1, 3], value: false, si:0} | 设置行可见性rows：行的索引，是一个数组（必填）  
value：可见性true |  | false（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
setColumnsVisible | {cols: [2, 4], value: true, si:0} | 设置列可见性cols：列的索引，是一个数组（必填）  
value：可见性true |  | false（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
lockCell | [{r:0,c:0,rc:1,cc:1}] |  | {selections: [{r:0, c:0, rc:1, cc:1}], si:0} | 锁定工作表指定区域的单元格  
selections：指定表格区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
unlockCell | [{r:0,c:0,rc:1,cc:1}] |  | {selections: [{r:0, c:0, rc:1, cc:1}], si:1} | 解锁工作表指定区域的单元格  
selections：指定表格区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
lockSheet | {si: [0] } | 锁定工作表  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
unlockSheet | {si: [1] } | 解锁工作表  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
updataValue | {si:0, area:’’, cells: [{r:0, c:0, v:1}]} |  | [{r:0, c:0, v:1, area:’’,}] |   
setSpan | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 合并指定区域的单元格  
range：指定表格区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setCellStyle | {data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bl:{bls:[‘dashDot’], blc:[’#00f’]} } }] ,si:} //设置单元格边框样式  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bkc:’#666’, frc:’#000’} }],si:} //设置单元格的前景色和背景色  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{f: ‘8pt Arial’} }],si:} //设置单元格字体  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{fm:‘0.00%’} }],si:} //设置单元格格式  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{va:1, ha:1} }],si:} //设置单元格垂直、水平对齐方式  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{l:true} }],si:} //指示是否将单元格标记为已锁定  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ww:true} }],si:} //设置单元格是否自动换行  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{stf:true} }],si:} //指示内容是否收缩以适应  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ti:2} }],si:} //表示单元格中文本的缩进单元数（一个整数值），其中增量1表示8个像素。  
{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: ‘colHeader’, style:{td:} }]} //td的值为数字 设置文本下划线（underline）:1 双下划线（doubleUnderline）:8 删除线（lineThrough）:2 无（none）:0   
{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: ‘colHeader’, style:{ep:true} }]} //ep为一个布尔值 true表示设置文本省略符 文本省略符的悬浮提示是默认存在的（显示文本全部内容），目前（2020.08）没有相关属性。 | 设置单元格样式/格式  
range：指定表格区域（必填）  
style：设置样式信息（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
设置单元格边框中的bls表示边框线的样式，可以设置成：dashDot,dashDotDot,dashed,dotted,double,empty,hair,medium,mediumDashDot,mediumDashDotDot,mediumDashed,slantedDashDot,thick,thin。blc表示边框线的颜色。设置行头区域,列的索引设置为0;设置列头区域,行的索引设置成0;rc和cc一般设置成1;设置单元格格式，fm是自定义单元格格式，与excel自定义单元格格式规则相同 | v4.0及以上 |   
fieldInsertRow | {range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 } | 在指定的工作表区域新增行/列，并复制指定行/列的样式  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
styleIndex：复制指定行/列样式的索引（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
fieldInsertCol | {range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 } | 在指定的工作表区域新增行/列，并复制指定行/列的样式  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
styleIndex：复制指定行/列样式的索引（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
fieldDeleteRow | {range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0} | 删除指定的工作表区域的行  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
fieldDeleteCol | {range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0} | 删除指定的工作表区域的列  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setFListCell | {cell:[{r:1,c:1}], selectType:0, dst:1,lockClickable: true} |  | {range: [{r:0, c:0, rc:1, cc:1}], selectType:0, dst:1,lockClickable:true} | 设置f7类型单元格  
selectType：0（支持模糊查询），1（不支持模糊查询）  
dst：0（默认风格），1（显示风格为下拉）  
lockClickable：true（支持锁定状态下的点击）  
setComboCell | [{cell:{r:0,c:0}, option:[“a”,“b”,“c”]}] | 设置下拉列表类型单元格  
cell：指定单元格（必填）  
option：下拉项（必填） | v4.0及以上 |   
setBtnCell | [{cell:{r:0,c:0},text:’’}] | 设置按钮类型单元格 | v4.0及以上 |   
setCustomBtnCell | [{cell:{r:0,c:1},text:’’}] | 设置自定义按钮类型单元格 | v4.0及以上 |   
setCellTag | [{r: 2, c: 10, st: true, pos:[0,1,2,3], bc:‘green’, height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true}] |  | {range: [{r: 2, c: 10, rc: 10, cc: 10}], st: true, pos:[0,1,2,3], bc:‘green’, height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true} | 给单元格设置特殊标识（小红点）  
r：指定行（必填）  
c：指定列（必填）  
st：控制是否显示小红点（必填）  
bc：背景颜色，同css颜色设置一致  
height：标识高度（仅在canClick为true时支持, v6.0.1）  
width标识宽度（v6.0.1）  
hoverHeight：鼠标可以覆盖到的高度，也就是实际可点击的高度（v6.0.1）  
hoverWidth：鼠标可以覆盖到的宽度（v6.0.1）  
canClick：是否可点击，点击时向后端发送网络请求（v6.0.1）  
pos：指定小红点的位置，0-3分别代表左上、右上、右下、左下（必填）（v6.0.1）  
resetCell | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 重置指定工作表区域的单元格类型  
range：指定工作表区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
getSpreadJson | {callback:‘invokemethod’, invokemethod:‘invokeAction’} | 获得表格bsae64格式文件，回传请求指令和后台触发事件  
callback：回调的请求方法名（必填）  
invokemethod：invokemethod（必填）  
sheetName：sheet页的名称（选填，填写该参数后会获取对应sheet页的json，V7.0支持） | v4.0及以上 |   
setLookupData | {r:0,c:0,data:[]} | 获取f7单元格类型的lookup数据 | v4.0及以上 |   
setCustomFormulaCell | {cell:[{r:0,c:0}], si:0} | 设置自定义的公式单元格 | v4.0及以上 |   
setAllCustomFormulaCell | {si: 0} | 设置所有单元格都为自定义公式单元格 | v4.0及以上 |   
registerCustomFormula | [{ formulaName: ‘FACTORIAL’, argsNum: 1, minArgs: 1, maxArgs: 1, returnType: ‘’, isAsynUpdate: true, defaultValue: ‘loading…’, evaluateMode: 0, callback: ‘autoFitColumn’, invokeAction: ‘invokeAction’, descriptionInfo: { description: ‘自定义公式’, parameters: [{ name: ‘’ }] }}] | 注册自定义公式  
isAsynUpdate：是否异步更新  
defaultValue：默认值  
evaluateMode：计算模式，0 单元格需要计算时进行重算 1 只计算一次 2 定时计算  
callback：回传给后端的，用于功能的具体实现  
invokeAction：回传给后端的，用于处理前端spread所有的请求 | v4.0及以上 |   
setFormula | [{r:0,c:0,f:’’}] |  | {cell: [{r:0,c:0,f:’’}], si:0} | 给单元格设置公式  
cell：指定单元格的行列和公式，f为""则清除公式（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
getFormula | { callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si: 0,options:[{r:0,c:0,rc:10,cc:10}] |  | ‘all’} | callback：回调函数  
invokeMethod：invokeMethod  
si：指定工作表的索引，默认是当前工作表（可选）  
options：获取的单元格（all为全部）  
setSelections | {r:0, c:0, rc:1, cc:1, si:0, position: { row: ‘nearest’, col: ‘nearest’ }, autoFocus: true} | 设置指定工作表区域单元格选中（会跳转到指定页签的指定单元格）  
r：行索引,  
c：列索引,  
rc：行选中范围单元格数量,  
cc：列选中范围单元格数量,  
si：指定工作表的索引，默认是当前工作表（可选）  
position：显示的区域相对浏览器的位置（center：中心，left：左边，nearest：最近的边缘，right：右侧）（v6.0.1）  
autoFocus：是否自动聚焦（v6.0.1） | v4.0及以上 |   
exportExcelFile | {fileName: ‘test’} | 导出excel文件fileName：文件名（必填） | v4.0及以上 |   
lockToolbarItems | [{name: ‘FontStyle’, isLock: true | false, isHide:true｜false}, group: ‘default’] or {isLock: true | false, allowSingleUnlock: true | false}  
hideContextMenuItems | {isHide: true/false} or [{name:‘pasteOptions’ , isHide: true,subMenu: ‘pasteValues’}] | 第一种参数格式，直接隐藏整个右键菜单  
第二种参数格式，隐藏指定的菜单项  
name：菜单的key  
isHide：是否隐藏  
subMenu：子菜单的key，隐藏某个子菜单时，需要同时传递name和subMenu  
菜单项对应的key可见 [SpreadJS控件右键菜单数据结构](https://vip.kingdee.com/link/s/ZLiTn) | v4.0及以上 |   
setViewOptions | [{type: ,text: ,baseType: }] | 设置当前工作表的视图 | v4.0及以上 |   
print | {index:, showPanel: true, printInfo: [{index:[],info:{}}]} | 打印spread表格  
showPanel是否显示打印面板 | v4.0及以上 |   
clearCellsStyle | [{r:0,c:0,rc:1,cc:1}] | 清除单元格格式 | v4.0及以上 |   
controlToolbarItems | [{name: 工具栏按钮名称, isCtl: true/false}] | 设置需要跟后端交互的工具栏按钮 | v4.0及以上 |   
setDisplayContent | [{key: ,callback ,invokemethod, text: , s: {w: ‘宽度’, fc: ‘字体颜色’} }, …] | 设置展示区域的内容支持点击向后端发送请求，请求内容与右键相似 | v4.0及以上 |   
clearDisplayContent | [‘key’, …] | 清除展示区域指定内容 | v4.0及以上 |   
closeToolbar | {} | 关闭工具栏 | v4.0及以上 |   
setExpandBtnVisible | {visible: true | false} | 设置工具栏“展开/收起”按钮的可见性 | v4.0及以上  
frozenSheet | {r:0, c:0, tr:0, tc:0, flc:‘red’, si:0} | 冻结行列  
r：表示第一条行冻结线所在的位置，默认值0  
tr：表示第二条行冻结线所在的位置，默认值0  
c：表示第一条列冻结线所在位置，默认值0  
tc：表示第二条列冻结线所在的位置，默认值0  
flc：表示冻结线的颜色，默认值’red’  
si：表示第几张表，默认值是当前选中的表单 | v4.0及以上 |   
autoFitColumns | {c:[1,3,5], si:0} | 自适应列宽  
c：列的索引（必填）-1表示所有列  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
autoFitRows | {r:[2,4,6], si:0} | 自适应行高  
r：行的索引（必填）-1表示所有行  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setWorkbookOptions | {allowCopyPasteExcelStyle:false, allowExtendPasteRange:false，…}  
{copyPasteHeaderOptions:allHeaders,…}  
{defaultDragFillType:auto |  | …}  
{showDragFillSmartTag:true,showHorizontalScrollbar:true,showVerticalScrollbar:true,…}  
{tabStripRatio:0.2}  
{cutCopyIndi/catorBorderColor:‘red’}  
{backColor:‘red’}  
{backgroundImageLayout:center |   
setWorksheetOptions | [{si:, options: {allowCellOverflow: false}}]  
[{si:, options: {isProtected: true |  | false}}]  
[{si:, options: {clipBoardOptions: 0-3}}]  
[{si:, options: {sheetTabColor: ‘red’}}]  
[{si:, options: {frozenlineColor: ‘red’}}]  
[{si:0, options: {gridline:{color:‘red’,showVerticalGridline:false,showHorizontalGridline:false}}}]  
[{si:, options: {rowHeaderVisible/colHeaderVisible: true |   
setShortcutKey | {commandName:‘ctrlA’, key:65, ctrl:true, shift:false, alt:false, meta:false} | 注册快捷键 | v4.0及以上 |   
setCornerMark | [{ range: [{r:0, c:0, rc:1, cc:4}], vi: true | false, text: ‘T’, bc: ‘green’, fc: ‘white’, pos: [0,1 ] }] | 给单元格设置角标range：范围  
vi：可见性text：角标上的文字  
bc：背景色，角标的颜色fc：前景色，文字的颜色pos：角标的位置，0，1，2，3分别是左上、右上、右下、左下 | v4.0及以上  
addContextMenuItems | {callback: ‘invokeAction’,subMenu:[{name:’’,text:’’,…}], items: [{name: ‘insertRowsBehind’, text: ‘向后插入行’, workArea: [‘viewport’, ‘colHeader’, ‘rowHeader’, ‘slicer’, ‘corner’, ‘sheetTab’]}]} | 添加右键菜单项  
callback：回调函数  
name：菜单项的key  
text：菜单项文本  
workArea：新增菜单项的作用区域，viewport表示数据区域，colHeader表示列头，rowHeader表示行头，slicer表示切片区域，corner表示角标区域sheetTab 表示页签区域  
subMenu 二级菜单、 三级菜单  
type：当需要设置分割线时，只需要设置该值为’separator’ | v4.0及以上 | text是唯一id，若加入与原有菜单相同的text可能会引起异常设置分割线：sa.action.addContextMenuItems（{items: [{type: ‘separator’}]}, sa.t）  
getRangeValues | {range: [{r:0, c:0, rc:1, cc:1}],compression: false} | 获取指定范围的值   
compression true表示压缩后上传 false表示和原来的使用方式一样  
压缩成了base64的格式,解压后是一个一纬数组 在数组的最后 会把{r:, c:, rc:, cc:}这个放进去 这个就是你获取的范围 比如你调起前端的指令 {r:2, c:2, rc:9, cc:9} 前端会把81个单元格的信息传给后端 如果想获取六行七列单元格的值 则对应数组下标 cc*（6-r）+7-c = 41 向后端发送的指令也发生了改变, invokeControlMethodOnly（model, ‘invokeAction’, [{ si, data: valueBase64, invokemethod: ‘rangeValues’ }]） | v4.0及以上 |   
setVirtualMode | { callback: ‘invokeAction’, invokemethod: ‘invokemethod’, isOpen: true, unRepeatRequest: false } | 设置虚模式加载，开启之后，表格在第一次加载和每次滚动的时候会把当前的可是区域发送给后端  
callback: 回调的请求方法名（必填）  
invokemethod: invokemethod（必填）  
isOpen: 用于开启和关闭虚模式加载，设置成false则停止发送请求，默认值是true（选填）  
unRepeatRequest：不重复发送网络请求，默认为false，也就是滚动区域也会重复发送网络请求（v7.0.1） | v4.0及以上 |   
setOutlineColumn | {c:0, options: {showCheckBox:false, maxLevel:2, collapsed: true}, si:} | 设置分组列折叠  
c: 列的索引，  
options是可选项，  
showCheckBox设置是否显示复选框，  
maxLevel控制数据分层级别。默认值是10  
collapsed设置默认折叠  
si: 指定工作表的索引，默认是当前工作表（可选）  
分组列不能和虚模式一起使用 | v4.0及以上 |   
setRowOutlines | {groups: [{index:1, count:5, isHide: false}], si:0} | 设置行的区域分组，从指定的起始索引将行的范围分组到大纲（范围组）中。  
groups: 支持传一个或多个分组信息（必选）  
index: 要分组起始索引（必选）  
count: 要分组的起始行或列的数目（必选）  
si: 指定页签的索引，默认是当前工作表（可选）  
isHide: 显示/隐藏分组（可选）（v6.0.1） | v4.0及以上 |   
setColumnOutlines | {groups: [{index:1, count:5, isHide: false}], si:1} | 设置列的区域分组，从指定的起始索引将列的范围分组到大纲（范围组）中。  
groups: 支持传一个或多个分组信息（必选）  
index: 要分组起始索引（必选）  
count: 要分组的起始行或列的数目（必选）  
si: 指定页签的索引，默认是当前工作表（可选）  
isHide: 显示/隐藏分组（可选）（v6.0.1） | v4.0及以上 |   
expandRowOutlines | {levels: [0,1], expand: false, si:0} | 使用指定的级别展开或收起大纲（范围组）  
levels: 分组的级别，从0开始（必选）  
expand: 是否展开分组（必选）  
si: 指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
expandColumnOutlines | {levels: [0,1], expand: false, si:1} | 使用指定的级别展开或收起大纲（范围组）  
levels: 分组的级别，从1开始（必选）  
expand: 是否展开分组（必选）  
si: 指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
addSheets | {sheets: [{index:1, name:‘ssss’, rc:100, cc:10}]} | 在指定索引处插入一个工作表  
sheets: 支持传一个或多个工作表的信息（必选）  
index: 用来添加工作表的索引（必选）  
name: 工作表的名称（可选）  
rc: 工作表的初始行数，默认是200（可选）  
cc: 工作表的初始列数，默认是20（可选） | v4.0及以上 |   
deleteSheets | {index: [2,3]} | 在指定索引处删除工作表  
index：要删除的工作表索引 | v4.0及以上 |   
setSheetsVisible | {index: [1,2], visible: false} | 设置工作表的可见性  
index: 要显示/隐藏的工作表索引  
显示时index可选 会按照顺序显示隐藏的一个sheet  
vidible: true（显示）false（隐藏） | v4.0及以上 |   
getRangeValues | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 设置工作表的可见性  
index: 要显示/隐藏的工作表索引  
显示时index可选 会按照顺序显示隐藏的一个sheet  
vidible: true（显示）false（隐藏） | v4.0及以上 |   
setComment（设置批注） | [{ options: [{ r: 0, c: 0, v: ‘这是一个批注’, remove: false }, { r: 1, c: 1, v: ‘’, remove: true }], si: 0 }] （注意参数是个数组） | 设置批注  
remove:true（清除批注）/false（设置批注时可以不传）  
v | v4.0及以上 |   
copyCellsTo（复制指定区域的样式到另一个区域） | {range: [{fromRow: 0, fromCol: 0, toRow: 1, toCol: 1, rc: 1, cc: 1}], si:0, type: 0,useSpreadApi: true, isUndo: true} | 复制指定区域的样式到另一个区域  
range: [{  
fromRow: 来源行  
toRow: 目标列  
formCol: 来源列  
toCol: 目标列,  
}]  
si: sheetIndex（单元表索引）  
type: 0：粘贴所有 | 1：粘贴值 | 2：粘贴样式  
useSpreadApi: 是否使用spread复制API（spread复制API的复制逻辑与手动复制粘贴的逻辑一样，可以实现公式更新）（v6.0.1）  
如果想复制整行，可以把对应的fromCol设置成-1 对应的复制整列可以把fromRow设置成-1  
（源和目标两片区域大小必须一样所以rc和cc指的既是from也是to的行数或列数）  
例如，将第一行的数据复制到第二行：sa.action.copyCellsTo（{range: [{fromRow: 0, fromCol: -1, toRow: 1, toCol: -1, rc: 1, cc: 20}], si:0, type: 0}, sa.t）  
setRowFilter（设置过滤） | [{ r: 4, c: 2, rc: 8, cc: 9, si: 0 }]（注意参数是个数组） | 设置过滤区域  
r:起始行的索引  
c:起始列的索引  
rc:要设置行的个数  
cc:要设置列的个数 | v4.0及以上 |   
setHyperLinkCell（设置超链接） | { callback:, invokemethod:, si:, range: [{r:1, c:1, rc: 10, cc:1, options:{text:‘超链接’,value:'[https://www.baidu.com](https://www.baidu.com',color:'red',visitedColor:'blue',toolTip)[’,color:‘red’,visitedColor:‘blue’,toolTip](https://www.baidu.com',color:'red',visitedColor:'blue',toolTip): ‘超链接’ }}] } | callback: 回调的请求方法名（必填）  
invokemethod: invokemethod（必填）  
si: 指定工作表的索引，默认是当前工作表（可选）  
range:区域范围（必填）  
options:超链接相关（可选） {  
text:单元格显示的内容  
value:点击超链接跳转的地址（不传时会向后端发送指令）SpreadJS的超链接支持许多协议，例如：http / https / ftp / file / mailto…。此外，还支持以“ sjs：//”开头的url，它引用工作表位置如sjs://Sheet1!A1:B2 也支持邮件的跳转  
color:超链接访问前的前景色  
visitedColor:超链接访问后的前景色  
toolTip:表示超链接的提示消息，当鼠标悬停在带有超链接的单元格上时显示。 | v4.0及以上 |   
setSheetName 设置表名 | [{oldName:‘sheet1’,si:0,newName:‘资产负债表’}] | oldName要设置的表的名称  
si要设置的表的索引  
newName表的新名称  
oldName与si都可不传 | v4.0及以上 |   
setZoom（设置缩放比例） | {zoom:0.25~4} | 默认1 | v4.0及以上 |   
setSheetDefaults（设置spread默认配置） | { si, setting: { rowHeight: value，… } } | si要设置的表的索引  
setting 配置  
rowHeight：行高  
colWidthl : 列宽 | v4.0及以上 |   
addToolbarItems（向工具栏里添加自定义按钮） | 自定义按钮为单个按钮  
[{  
key: ‘CheckFormula2’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
title: {  
zh_CN: ‘中文’,  
zh_TW: ‘繁体’,  
en_US: ‘english’  
},   
showTitle: true,  
callback: ‘sss’,  
invokeAction: ‘sssss’  
}]  
自定义按钮为下拉菜单  
[{  
key: ‘CheckFormula0’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击  
title: {  
zh_CN: ‘中文’,  
zh_TW: ‘繁体’,  
en_US: ‘english’  
},   
showTitle: true,  
item: [ // 下拉项  
{  
title:{  
zh_CN: ‘中文’,  
zh_TW: ‘繁体’,  
en_US: ‘english’  
} ,  
icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，  
callback: ‘sss’,  
invokeAction: ‘sssss’,  
key: ‘AutoFitColumn0123’, }  
]  
}] | key ：按钮标识  
icon： 字体图标  
title： tips或下拉项的内容，为兼容国际化，建议传对象形式的参数，包含三种语言显示的文字，也可直接传字符串  
callback 后端具体实现的类  
invokeAction 后端同于同一处理spread的接口  
showTitle: 是否显示标题文字  
item: 下拉选项 | v5.0及以上 |   
setActiveSheet（切换页签） | {name:’’,si:’’} | 激活/选中指定页签  
name：要切换页签的名字  
si：要切换页签的索引  
这两个传一个就行 | v4.0及以上 |   
excuteToolbarActions | {name: ‘SwitchView’, // 按钮标识itemName: ‘SwitchView_ValueView’ // 执行的按钮活动} | name: 按钮标识  
itemName: 执行的按钮活动 | v5.0及以上 |   
setToolbarGroups（自定义工具栏分组） | [{  
name: ‘默认1’,  
key: ‘default1’,  
toolbarItems: [{key: ‘Undo’},{key: ‘Undo’}， // 默认功能  
{  
key: ‘CheckFormula0’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
title: { zh_CN: ‘中文’, zh_TW: ‘繁体’, en_US: ‘english’ },   
showTitle: true,  
items: [ // 下拉项  
{  
title:{ zh_CN: ‘中文’,zh_TW: ‘繁体’,en_US: ‘english’ } ,  
icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，  
callback: ‘sss’,  
invokeAction: ‘sssss’,  
key: ‘AutoFitColumn0123’ }  
]  
},  
],  
}] | name: 需要显示的分组名称  
key: 分组唯一的key值，用于辨识分组，不可重复  
toolbarItems: 工具栏需要显示的按钮，数组顺序即按钮顺序。自定义功能参数与添加自定义按钮（addToolbarItems）相同。需要注意的是，自定义添加了一个新参数，isCustom，如果是自定义按钮这个参数必填true  
提示：默认情况下，默认分组一直存在，因此自定义工具栏分组时，如果不需要修改默认分组，则不需要传默认分组的参数，只需要传扩展分组，会自动将扩展分组添加到分组中。传递包含默认分组的参数则视为修改默认分组。 | v5.0及以上 |   
setToolbarCustomExpend（自定义公式扩展区域） | {itemConfig:{  
showTitle: true,  
key: ‘CheckFormula0’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
title: { zh_CN: ‘测试’,zh_TW: ‘繁体’,en_US: ‘english’ },  
callback: ‘autoFitColumn’, // 回传给后端的，用于功能的具体实现  
invokeAction: ‘invokeAction’, // 回传给后端的，用于处理前端spread所有的请求,  
dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击  
items: [ // 下拉项  
{  
title:{zh_CN: ‘测试’,zh_TW: ‘繁体’,en_US: ‘english’},  
callback: ‘sss’,icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，  
invokeAction: ‘sssss’,  
key: ‘AutoFitColumn0123’, }  
]},   
inputConfig: {  
callback: ‘autoFitColumn’, // 回传给后端的，用于功能的具体实现  
invokeAction: ‘invokeAction’  
}} | itemConfig：自定义按钮配置，与自定义按钮相似inputConfig：自定义输入配置 | v5.0及以上 |   
setToolbarCustomInputValue（与setToolbarCustomExpend搭配使用，自定义公式扩展区域输入值） | string’ |  | v4.0及以上 |   
setCustomSheetMenu（自定义页签菜单右键顺序） | [{name: ‘hideSheet’}, {type: ‘separator’}, { name: ‘setBGC’ }, {name: ‘unhideSheet’}, {name: ‘insertRowsBehind’} ,{name: ‘insertSheet’}] | name: 菜单名称  
（insertSheet：插入，deleteSheet：删除，hideSheet：隐藏，unhideSheet：显示，setBGC：颜色选择器）  
type: 只有分割线才使用type（type: ‘separator’） | v4.0及以上 |   
getLockedCells（获取锁定单元格） | [{ callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si: 0, compression: false }] | callback：回调函数  
invokeAction：后端同一处理spread的接口  
si：sheetIndex  
compression：是否压缩 | v4.0及以上 |   
hideRow（隐藏行）/unhideRow（显示行） | {data: [1, 2], si: 0, isUndo: true} |  | {data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似 | 在工作表的指定索引处添加一行/列  
data：指定行（行）的索引（必填）  
si: 指定工作表的索引，默认是当前工作表（可选）  
index: 要隐藏/显示的行的索引  
count: 要隐藏/显示多少行  
hideColumn（隐藏列）/unhideColumn（显示列） | {data: [1, 2], si: 0, isUndo: true} |  | {data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似 | 在工作表的指定索引处添加一行/列  
data：指定行（列）的索引（必填）  
si: 指定工作表的索引，默认是当前工作表（可选）  
index: 要隐藏/显示的列的索引  
count: 要隐藏/显示多少列  
setSpreadParams | {data: {key: value}} | 添加全局spread属性（也可直接通过JSON设置，在spread新增一个自定义属性serverSpreadParams，将需要全局定义的属性作为对象赋值给serverSpreadParams）  
key：属性名  
value：属性值 | v5.0.027及以上 | 目前已有的属性：  
isEditSendRequest：鼠标进入编辑态与退出编辑态时是否向后端发送网络请求（5.0.0.27）  
allowMaxInsert9999：插入行列支持最大输入9999（v6.0.1）  
useNewInsert：使用新版插入（向上插入/向下插入）（v6.0.1）  
usePtFontSize：使用pt作为font-size的单位，默认是px（v6.0.7）  
useSheetPrintConfig：按工作表保存打印配置（v6.0.12）  
useExcelCellsFormat：使用与excel同步的单元格格式（v6.0.14）  
isUpdateStyleSendRequest：样式更新时发送网络请求（v7.0.0）  
isDeleteLockedSendRequest：delete快捷键清除锁定单元格数据时向后端发送网络请求（v7.0.4）  
setGCParams | {data: {key: value}} | 添加全局GC属性（与setSpreadParams不同的是，这个是GC层面的属性，设置之后不会随着json的设置而覆盖，建议本条指令在所有指令之前设置）  
key：属性名  
value：属性值 | v6.0及以上 | 目前已有的属性：calcPrecision：计算精度，默认为14，当出现精度问题时，可以调整为12。不建议调整其他值，可能会出现计算问题（v6.0.1）  
hideZero | {ishide: true, si: 0} | ishide：为0时是否隐藏  
si：sheetIndex | v4.0及以上 | 如果表格中已经存在0值，指令开启后需要重新计算才能将已有的0隐藏  
setGroupColNode（设置分组列） | [{r:number, c:number, groupNodeType:1 |  | 2 |   
setStatusBarVisible | {visible: false} | 设置spread底部缩放的显示（true）与隐藏（false）  
{visible: true | false} | v4.0及以上  
setValidator | {type: 0, option: {vt: 1, v1: ‘1’, vco: 0, ht: 0, hsc: ‘#ff0000’, es: 0, et: ‘不允许输入’}, range: [{row: 0, col: 0, rowCount: 5, colCount: 5}], callback: ‘invokeAction’, invokemethod: ‘test’, si: 0} | type: number （0:设置, 1:取消）  
option: object （配置项，详见备注）  
range: array （应用的单元格）  
callback: 回调函数  
invokemethod: 回调函数  
si: sheetindex | v4.0及以上 | VALIDATOR_TYPE = ‘vt’ // 验证器  
IS_FORMULA_LIST_VALIDATOR = ‘iflv’//  
VALIDATOR_COMPARISON_OPERATOR = ‘vco’// 比较运算符  
VALUE_1 = ‘v1’// 值一  
VALUE_2 = ‘v2’// 值二  
IS_INTEGER = ‘ii’ // 是否是整数  
SHOW_INPUT_MSG = ‘sim’// 显示输入消息  
INPUT_TITLE = ‘it’// 标题,  
INPUT_MESSAGE = ‘im’// 信息  
IGNORE_BLANK = ‘ib’ // 忽略空白  
SHOW_ERROR_MESSAGE = ‘sem’ // 显示错误信息  
ERROR_STYLE = ‘es’// 错误类型  
ERROR_TITLE = ‘et’// 错误标题  
ERROR_MSG = ‘em’ // 错误信息  
HIGHLIGHT_TYPE = ‘ht’ // 圆圈样式  
DOGEAR_POSITION = ‘dp’// 位置 与圆圈样式有关 类型dogear才会用到  
ICON_POSITION = ‘ip’// 位置 与圆圈样式有关 类型icon才会用到  
HIGHLIGHT_STYLE_COLOR = ‘hsc’// 颜色  
HIGHLIGHT_STYLE_IMAGE = ‘hsi’// 图标 与圆圈样式有关 类型icon才会用到  
batchExportExcelFiles | base64加密过的对象：{data:[{json:’’},…], fileName:‘fileName’} | json：spread的json文件，注意，这里是原生的json字符串，不是通过base64加密过的  
fileName：导出的文件名称 | v4.0及以上 |   
syncSpreads | { slaveSpreadKeys: [key2, key3], syncOptions: [scrollBar, rowHeight, colWidth, zoom], sync: true } | 多表联动，仅仅对各个当前活动的工作表进行联动处理  
slaveSpreadKeys：需要联动的spread的key  
syncOptions：需要联动的属性，目前只支持scrollBar（滚动）, rowHeight（行高）, colWidth（列宽）, zoom（缩放）  
sync：是否联动，如果已经联动的填写这个参数用于关闭联动 | v6.0.15及以上 |   
  
# 7 插件示例
    
    
    public class Spread extends Container{
    
        private SpreadPostDataInfo postData ;
        //删除行
        public void deleteRows(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().deleteRows(new SpreadEvent(this,postData));
        }
        //删除列
        public void deleteColumns(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().deleteColumns(new SpreadEvent(this,postData));
        }
        //插行
        public void addRows(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().addRows(new SpreadEvent(this,postData));
        }
        //插列
        public void addColumns(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().addColumns(new SpreadEvent(this,postData));
        }
        //切换单元格
        public void entryRowClick(int arg){
            getActionService().selectedSpread(new SpreadEvent(this,postData));        
        }
        //前端通用指令
        public void askExecute(LinkedHashMap<String,Object> postDatas){
            packInvokeParams(postDatas);
            getActionService().askExecute(new SpreadEvent(this,postData));
        }
    }
    
    

插行示例：  
前端控制台模拟后端指令  
![3.png](https://vip.kingdee.com/download/01004a82934bef0d4b92b3699ad2294ba4ce.png)

# 8 参考链接

  1. [SpreadJS 控件常见问题](https://vip.kingdee.com/link/s/ZLiPJ)



<h1><a id="_0"></a>变更记录</h1> <table> <thead> <tr> <th>产品版本</th> <th>更新内容</th> <th>更新日期</th> </tr> </thead> <tbody> <tr> <td>V5.0.024</td> <td>新增了Spread 表格参数设置后点击确定可保存表格参数的功能，满足了用户需要记住表格参数设置的需求，详见5.1.2</td> <td>2023年7月</td> </tr> <tr> <td>V5.0.025</td> <td>Spread表格支持了设定选中区域指令的位置参数，满足了用户希望将选定区域放置在指定的可视范围的需求，详见6：setSelections</td> <td>2023年7月</td> </tr> <tr> <td>V6.0.15</td> <td>完善了Spread多表联动的功能，支持多表同步滚动条、行高、列宽、缩放，提高用户设置报表模板的易用性，详见6：syncSpreads</td> <td>2024年7月</td> </tr> <tr> <td>v7.0.1</td> <td>1、getSpreadJson与setSpreadJson现在支持对单个工作表（sheet）进行操作，详见6：getSpreadJson、setSpreadJson<br>2、出于第三方商业合规性要求，伙伴/二开环境的表单设计器中将不再内置SpreadJS控件，详见1：下架说明</td> <td>2024年10月</td> </tr> <tr> <td>v7.0.3</td> <td>1、updatavalue支持字典模式传输数据，降低传输数据量，提升性能，详见6：updatavalue<br>2、优化了SpreadJS表格设置列格式时数据导入的性能，避免页面卡顿</td> <td>2024年12月</td> </tr> <tr> <td>V7.0.4</td> <td>setSpreadParams新增isDeleteLockedSendRequest参数：delete快捷键清除锁定单元格数据时向后端发送网络请求，可二开实现框选单元格包含锁定单元格时，仅清除非锁定单元格的数据，详见6：setSpreadParams</td> <td>2024年12月</td> </tr> <tr> <td>V8.0.1</td> <td>升级了Spread控件版本，增强了相关功能并修复了若干问题，满足不同客户的使用场景。注意：如果后端开发用到了GCExcel组件，则为了前后端组件版本适配，需要同步升级后端GCExcel组件，详见5.2</td> <td>2025年9月</td> </tr> </tbody> </table> <h1><a id="1__12"></a>1 功能介绍</h1> <p>SpreadJS 是一款基于 HTML5 的纯前端电子表格控件，提供了与 Excel 高度类似的功能和兼容性。<br /> 该控件仅在PC端支持。</p> <h2><a id="_15"></a>下架说明</h2> <p>出于第三方商业合规性要求，SpreadJS 控件的使用权限严格限定于本公司旗下标品业务（包括但不限于“星空”、“星瀚”系列），若在二开中继续使用，需自行承担潜在的商业风险。<br /> 从苍穹 7.0.1 版本开始，针对伙伴或二开的苍穹环境在其表单设计器中隐藏 SpreadJS 控件，在控件面板中无法选到该控件参与布局设计。若客户有在线使用类似 SpreadJS 控件的需求，我们建议：</p> <ul> <li>自行采购 SpreadJS 官方授权，并集成到项目中使用</li> <li>选择其他功能类似的第三方表格控件进行集成</li> </ul> <h1><a id="2__22"></a>2 控件对象</h1> <p><code>kd.bos.form.spread</code></p> <h1><a id="3__24"></a>3 视觉展示</h1> <p><img src="/download/0100f9b26b4809864739b9609ec460d642f6.png" alt="1.png" /></p> <h1><a id="4__26"></a>4 属性说明</h1> <h2><a id="41__27"></a>4.1 通用属性</h2> <blockquote> <p>通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考<a href="https://vip.kingdee.com/article/215559076720798976" target="_blank">通用属性</a></p> </blockquote> <h2><a id="42__29"></a>4.2 样式属性</h2> <blockquote> <p>样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考<a href="https://vip.kingdee.com/article/252017936767406336" target="_blank">样式属性</a></p> </blockquote> <h2><a id="43__31"></a>4.3 业务属性</h2> <table> <thead> <tr> <th>属性名</th> <th>类型</th> <th>运行时参数名</th> <th>默认值</th> <th>说明</th> <th>PC</th> </tr> </thead> <tbody> <tr> <td>显示编辑工具栏</td> <td>boolean</td> <td>setb</td> <td>true</td> <td>是否默认显示工具栏，开启时将自动展开，关闭时则自动收起。无论默认状态如何，随时可以手动展开或收起工具栏</td> <td>V4.0及以上</td> </tr> <tr> <td>允许多页签</td> <td>boolean</td> <td>smt</td> <td>false</td> <td>是否开启多页签模式，开启后控件底部将显示页签栏，支持快速切换和新增。导入功能也与该属性相关，开启时，允许同时选择多个sheet导入，关闭时仅允许选择单个sheet导入</td> <td>V4.0及以上</td> </tr> <tr> <td>支持导出excel</td> <td>boolean</td> <td>ee</td> <td>false</td> <td>是否支持批量导出，开启后，控件将支持批量导出功能。可以使用 batchExportExcelFiles 指令，将多个 Spread 的数据合并导出为一个 Excel 文件。适用于列表页面，需要批量导出多个Spread数据的场景</td> <td>V4.0及以上</td> </tr> </tbody> </table> <h1><a id="5__39"></a>5 功能详情</h1> <h2><a id="51__40"></a>5.1 工具栏</h2> <h3><a id="511_41"></a>5.1.1工具栏功能介绍</h3> <p>spread工具栏配置了表格常用的一些功能</p> <ul> <li>对于单元格样式的一些设置：字体样式、字体大小、字体颜色、文本居中、单元格背景色</li> <li>对于单元格格式的一些设置：常规、日期、时间、会计专用等等</li> <li>对于工作表的一些设置：冻结表格、过滤表格、打印、显示公式等等</li> </ul> <p>以往的文章中我们已经对这些特性进行过介绍<br /> 链接直达：<br /> <a href="https://vip.kingdee.com/article/390596666833372416" target="_blank">Spread工具栏新特性</a><br /> <a href="https://developer.kingdee.com/article/502501692391512576" target="_blank">Spread 全新视觉交互升级</a><br /> <a href="https://developer.kingdee.com/article/85798461483727872" target="_blank">Spread支持查找替换功能</a><br /> <a href="https://developer.kingdee.com/article/322746947583383040" target="_blank">Spread支持数据有效性</a><br /> <a href="https://developer.kingdee.com/article/137514916042362624" target="_blank">Spread支持打印</a><br /> <a href="https://developer.kingdee.com/article/324229535094933760" target="_blank">Spread支持自定义单元格格式</a><br /> <a href="https://developer.kingdee.com/article/523501187660638464" target="_blank">Spread支持条件格式</a></p> <h3><a id="512_57"></a>5.1.2表格设置</h3> <p>设置表格配置参数后（功能入口：表格工具栏的配置按钮），点击确定，可将参数保存到当前表格，该参数只对当前表格生效。再次打开该表格或其他用户使用该表格，该参数依旧生效。<br /> <img src="/download/010012cffaed11c044ac9662e9dfc8e96a88.png" alt="2.png" /></p> <h2><a id="52_SpreadJS__60"></a>5.2 SpreadJS 版本升级</h2> <p>鉴于在过往客户反馈中许多问题需通过升级 SpreadJS 版本解决，为提升产品稳定性与功能性，我们在苍穹平台 8.0.1 版本中，正式将内置的 SpreadJS 第三方控件从 V14.2.6 升级至 V17.1。<br /> 升级影响与必要操作：</p> <ul> <li>如果您的项目后端使用了 GCExcel 组件，必须将其同步升级至 V7 版本，以确保前后端功能一致性与稳定性；</li> <li>为了提升存储何计算性能，SpreadJS 第三方组件设计了共享公式，当存在两个相同的公式时将提取到sharedFormulas中，直接解析 SpreadJson 数据获取公式可能获取正确的公式。官方建议后端通过 GCExcel 的 api 去获取和改动这些信息；</li> <li>除了公式，由于 SpreadJS 产品版本持续在升级，SpreadJson 中的数据结构很有可能发生变化，建议都通过 GCExcel 的 api 执行操作；</li> </ul> <p>版本详情：有关 V17.1 版本解决的具体问题与新特性，请参阅官方发布说明：<br /> <a href="/tolink?target=https://demo.grapecity.com.cn/spreadjs/help/docs/rnotes/171" target="_blank">17.1 &lt; 发布说明 | 葡萄城 SpreadJS 表格控件在线文档</a></p> <h1><a id="6_SpreadJS_71"></a>6 SpreadJS接口介绍</h1> <p>spread功能高度依赖业务，因此部分功能需要后端配置响应的指令才能正常使用，比如插行、删行。</p> <table> <thead> <tr> <th>指令</th> <th>参数</th> <th>功能</th> <th>支持版本</th> <th>备注</th> </tr> </thead> <tbody> <tr> <td>callbackAction</td> <td>{callback:‘invokemethod’, invokemethod:‘addRows’}</td> <td>回调函数指令，后端告诉前端它还有后续动作</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setSpreadJson</td> <td>spreadJS压缩过后的json串</td> <td>加载spreadJS的json串<br>支持加载sheet的JSON字符串（V7.0）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>appendRows</td> <td>{count:1, si:0}</td> <td>在指定工作表尾部追加count行<br>count：行数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>appendCols</td> <td>{count:1, si:0}</td> <td>在指定工作表尾部追加coun列<br>count：列数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>deleteRow</td> <td>{data:[2, 1], si:0}</td> <td></td> <td>{data: [{index:1, count: 2}], si:}</td> <td>删除工作表指定索引处的行<br>data：指定行的索引（必填），删除连续行时索引应该倒序从后往前删除<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：删除的起始位置<br>count：删除多少行</td> </tr> <tr> <td>deleteCol</td> <td>{data:[2, 1], si:0}</td> <td></td> <td>{data: [{index:1, count: 3}], si:}</td> <td>删除工作表指定索引处的列<br>data：指定列的索引（必填），删除连续行时索引应该倒序从后往前删除<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：删除的起始位置<br>count：删除多少列</td> </tr> <tr> <td>insertRow</td> <td>{data:[1, 2], dir: ‘bottom’, copyStyle: false, copySpans: false, si:0}</td> <td></td> <td>{data:[{ index: 1, count:2 }], dir: ‘bottom’, copyStyle: false, si:0}</td> <td>在工作表的指定索引处添加一行<br>data：指定行的索引（必填）<br>copyStyle：是否复制指定行的样式，默认复制（可选）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：要复制的行的索引<br>count：要复制多少行<br>dir：向上插入还是向下插入（默认向上如需要向下输入则需要将dir设置为bottom）<br>copySpans：是否复制指定行的融合（这个只能添加行的时候用，列还不支持）</td> </tr> <tr> <td>insertCol</td> <td>{data:[1, 3], dir: ‘after’, copyStyle: false, si: 0}</td> <td></td> <td>{data:[{ index:1, count: 2}], dir: ‘after’, copyStyle: false, si: 0}</td> <td>在工作表的指定索引处添加一列<br>data：指定列的索引（必填）<br>copyStyle：是否复制指定列的样式，默认复制（可选）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>index：要复制的列的索引<br>count：要复制多少列<br>dir：向前插入还是向后插入（默认向前如需要向后输入则需要将dir设置为after）</td> </tr> <tr> <td>setColumnsWidth</td> <td>{index:[0], num:20, si: 0,area: ‘viewport’}</td> <td>设置列宽<br>index：指定列的索引（必填）<br>num：宽度（以像素为单位，必填）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>area：指定区域（可选）<br>area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setRowsHeight</td> <td>{index:[0], num:20, si: 0,area: ‘viewport’}</td> <td>设置行高<br>index：指定行的索引（必填）<br>num：高度（以像素为单位，必填）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>area：指定区域（可选）<br>area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>getColumnsWidth</td> <td>{index:[2,3], callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si:‘0’}</td> <td>获取列宽<br>index：指定行（列）的索引（必填）<br>callback：回调的请求方法名（必填）<br>invokemethod：invokemethod（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>getRowsHeight</td> <td>{index:[2,3], callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si:‘0’}</td> <td>获取行高<br>index：指定行（列）的索引（必填）<br>callback：回调的请求方法名（必填）<br>invokemethod：invokemethod（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setRowsVisible</td> <td>{rows: [1, 3], value: false, si:0}</td> <td>设置行可见性rows：行的索引，是一个数组（必填）<br>value：可见性true</td> <td></td> <td>false（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> </tr> <tr> <td>setColumnsVisible</td> <td>{cols: [2, 4], value: true, si:0}</td> <td>设置列可见性cols：列的索引，是一个数组（必填）<br>value：可见性true</td> <td></td> <td>false（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> </tr> <tr> <td>lockCell</td> <td>[{r:0,c:0,rc:1,cc:1}]</td> <td></td> <td>{selections: [{r:0, c:0, rc:1, cc:1}], si:0}</td> <td>锁定工作表指定区域的单元格<br>selections：指定表格区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> </tr> <tr> <td>unlockCell</td> <td>[{r:0,c:0,rc:1,cc:1}]</td> <td></td> <td>{selections: [{r:0, c:0, rc:1, cc:1}], si:1}</td> <td>解锁工作表指定区域的单元格<br>selections：指定表格区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> </tr> <tr> <td>lockSheet</td> <td>{si: [0] }</td> <td>锁定工作表<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>unlockSheet</td> <td>{si: [1] }</td> <td>解锁工作表<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>updataValue</td> <td>{si:0, area:’’, cells: [{r:0, c:0, v:1}]}</td> <td></td> <td>[{r:0, c:0, v:1, area:’’,}]</td> <td></td> </tr> <tr> <td>setSpan</td> <td>{range:[{r:0, c:0, rc:1, cc:1}], si:0}</td> <td>合并指定区域的单元格<br>range：指定表格区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setCellStyle</td> <td>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bl:{bls:[‘dashDot’], blc:[’#00f’]} } }] ,si:} //设置单元格边框样式<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bkc:’#666’, frc:’#000’} }],si:} //设置单元格的前景色和背景色<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{f: ‘8pt Arial’} }],si:} //设置单元格字体<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{fm:‘0.00%’} }],si:} //设置单元格格式<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{va:1, ha:1} }],si:} //设置单元格垂直、水平对齐方式<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{l:true} }],si:} //指示是否将单元格标记为已锁定<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ww:true} }],si:} //设置单元格是否自动换行<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{stf:true} }],si:} //指示内容是否收缩以适应<br>{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ti:2} }],si:} //表示单元格中文本的缩进单元数（一个整数值），其中增量1表示8个像素。<br>{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: ‘colHeader’, style:{td:} }]} //td的值为数字 设置文本下划线（underline）:1 双下划线（doubleUnderline）:8 删除线（lineThrough）:2 无（none）:0 <br>{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: ‘colHeader’, style:{ep:true} }]} //ep为一个布尔值 true表示设置文本省略符 文本省略符的悬浮提示是默认存在的（显示文本全部内容），目前（2020.08）没有相关属性。</td> <td>设置单元格样式/格式<br>range：指定表格区域（必填）<br>style：设置样式信息（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）<br>设置单元格边框中的bls表示边框线的样式，可以设置成：dashDot,dashDotDot,dashed,dotted,double,empty,hair,medium,mediumDashDot,mediumDashDotDot,mediumDashed,slantedDashDot,thick,thin。blc表示边框线的颜色。设置行头区域,列的索引设置为0;设置列头区域,行的索引设置成0;rc和cc一般设置成1;设置单元格格式，fm是自定义单元格格式，与excel自定义单元格格式规则相同</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>fieldInsertRow</td> <td>{range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 }</td> <td>在指定的工作表区域新增行/列，并复制指定行/列的样式<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>styleIndex：复制指定行/列样式的索引（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>fieldInsertCol</td> <td>{range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 }</td> <td>在指定的工作表区域新增行/列，并复制指定行/列的样式<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>styleIndex：复制指定行/列样式的索引（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>fieldDeleteRow</td> <td>{range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0}</td> <td>删除指定的工作表区域的行<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>fieldDeleteCol</td> <td>{range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0}</td> <td>删除指定的工作表区域的列<br>range：指定表格区域（必填）<br>index：指定插入的索引（必填）<br>count：插入的行/列数（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setFListCell</td> <td>{cell:[{r:1,c:1}], selectType:0, dst:1,lockClickable: true}</td> <td></td> <td>{range: [{r:0, c:0, rc:1, cc:1}], selectType:0, dst:1,lockClickable:true}</td> <td>设置f7类型单元格<br>selectType：0（支持模糊查询），1（不支持模糊查询）<br>dst：0（默认风格），1（显示风格为下拉）<br>lockClickable：true（支持锁定状态下的点击）</td> </tr> <tr> <td>setComboCell</td> <td>[{cell:{r:0,c:0}, option:[“a”,“b”,“c”]}]</td> <td>设置下拉列表类型单元格<br>cell：指定单元格（必填）<br>option：下拉项（必填）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setBtnCell</td> <td>[{cell:{r:0,c:0},text:’’}]</td> <td>设置按钮类型单元格</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setCustomBtnCell</td> <td>[{cell:{r:0,c:1},text:’’}]</td> <td>设置自定义按钮类型单元格</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setCellTag</td> <td>[{r: 2, c: 10, st: true, pos:[0,1,2,3], bc:‘green’, height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true}]</td> <td></td> <td>{range: [{r: 2, c: 10, rc: 10, cc: 10}], st: true, pos:[0,1,2,3], bc:‘green’, height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true}</td> <td>给单元格设置特殊标识（小红点）<br>r：指定行（必填）<br>c：指定列（必填）<br>st：控制是否显示小红点（必填）<br>bc：背景颜色，同css颜色设置一致<br>height：标识高度（仅在canClick为true时支持, v6.0.1）<br>width标识宽度（v6.0.1）<br>hoverHeight：鼠标可以覆盖到的高度，也就是实际可点击的高度（v6.0.1）<br>hoverWidth：鼠标可以覆盖到的宽度（v6.0.1）<br>canClick：是否可点击，点击时向后端发送网络请求（v6.0.1）<br>pos：指定小红点的位置，0-3分别代表左上、右上、右下、左下（必填）（v6.0.1）</td> </tr> <tr> <td>resetCell</td> <td>{range:[{r:0, c:0, rc:1, cc:1}], si:0}</td> <td>重置指定工作表区域的单元格类型<br>range：指定工作表区域（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>getSpreadJson</td> <td>{callback:‘invokemethod’, invokemethod:‘invokeAction’}</td> <td>获得表格bsae64格式文件，回传请求指令和后台触发事件<br>callback：回调的请求方法名（必填）<br>invokemethod：invokemethod（必填）<br>sheetName：sheet页的名称（选填，填写该参数后会获取对应sheet页的json，V7.0支持）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setLookupData</td> <td>{r:0,c:0,data:[]}</td> <td>获取f7单元格类型的lookup数据</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setCustomFormulaCell</td> <td>{cell:[{r:0,c:0}], si:0}</td> <td>设置自定义的公式单元格</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setAllCustomFormulaCell</td> <td>{si: 0}</td> <td>设置所有单元格都为自定义公式单元格</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>registerCustomFormula</td> <td>[{ formulaName: ‘FACTORIAL’, argsNum: 1, minArgs: 1, maxArgs: 1, returnType: ‘’, isAsynUpdate: true, defaultValue: ‘loading…’, evaluateMode: 0, callback: ‘autoFitColumn’, invokeAction: ‘invokeAction’, descriptionInfo: { description: ‘自定义公式’, parameters: [{ name: ‘’ }] }}]</td> <td>注册自定义公式<br>isAsynUpdate：是否异步更新<br>defaultValue：默认值<br>evaluateMode：计算模式，0 单元格需要计算时进行重算 1 只计算一次 2 定时计算<br>callback：回传给后端的，用于功能的具体实现<br>invokeAction：回传给后端的，用于处理前端spread所有的请求</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setFormula</td> <td>[{r:0,c:0,f:’’}]</td> <td></td> <td>{cell: [{r:0,c:0,f:’’}], si:0}</td> <td>给单元格设置公式<br>cell：指定单元格的行列和公式，f为&quot;&quot;则清除公式（必填）<br>si：指定工作表的索引，默认是当前工作表（可选）</td> </tr> <tr> <td>getFormula</td> <td>{ callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si: 0,options:[{r:0,c:0,rc:10,cc:10}]</td> <td></td> <td>‘all’}</td> <td>callback：回调函数<br>invokeMethod：invokeMethod<br>si：指定工作表的索引，默认是当前工作表（可选）<br>options：获取的单元格（all为全部）</td> </tr> <tr> <td>setSelections</td> <td>{r:0, c:0, rc:1, cc:1, si:0, position: { row: ‘nearest’, col: ‘nearest’ }, autoFocus: true}</td> <td>设置指定工作表区域单元格选中（会跳转到指定页签的指定单元格）<br>r：行索引,<br>c：列索引,<br>rc：行选中范围单元格数量,<br>cc：列选中范围单元格数量,<br>si：指定工作表的索引，默认是当前工作表（可选）<br>position：显示的区域相对浏览器的位置（center：中心，left：左边，nearest：最近的边缘，right：右侧）（v6.0.1）<br>autoFocus：是否自动聚焦（v6.0.1）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>exportExcelFile</td> <td>{fileName: ‘test’}</td> <td>导出excel文件fileName：文件名（必填）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>lockToolbarItems</td> <td>[{name: ‘FontStyle’, isLock: true</td> <td>false, isHide:true｜false}, group: ‘default’] or {isLock: true</td> <td>false, allowSingleUnlock: true</td> <td>false}</td> </tr> <tr> <td>hideContextMenuItems</td> <td>{isHide: true/false} or [{name:‘pasteOptions’ , isHide: true,subMenu: ‘pasteValues’}]</td> <td>第一种参数格式，直接隐藏整个右键菜单<br>第二种参数格式，隐藏指定的菜单项<br>name：菜单的key<br>isHide：是否隐藏<br>subMenu：子菜单的key，隐藏某个子菜单时，需要同时传递name和subMenu<br>菜单项对应的key可见 <a href="https://vip.kingdee.com/link/s/ZLiTn" target="_blank">SpreadJS控件右键菜单数据结构</a></td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setViewOptions</td> <td>[{type: ,text: ,baseType: }]</td> <td>设置当前工作表的视图</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>print</td> <td>{index:, showPanel: true, printInfo: [{index:[],info:{}}]}</td> <td>打印spread表格<br>showPanel是否显示打印面板</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>clearCellsStyle</td> <td>[{r:0,c:0,rc:1,cc:1}]</td> <td>清除单元格格式</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>controlToolbarItems</td> <td>[{name: 工具栏按钮名称, isCtl: true/false}]</td> <td>设置需要跟后端交互的工具栏按钮</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setDisplayContent</td> <td>[{key: ,callback ,invokemethod, text: , s: {w: ‘宽度’, fc: ‘字体颜色’} }, …]</td> <td>设置展示区域的内容支持点击向后端发送请求，请求内容与右键相似</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>clearDisplayContent</td> <td>[‘key’, …]</td> <td>清除展示区域指定内容</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>closeToolbar</td> <td>{}</td> <td>关闭工具栏</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setExpandBtnVisible</td> <td>{visible: true</td> <td>false}</td> <td>设置工具栏“展开/收起”按钮的可见性</td> <td>v4.0及以上</td> </tr> <tr> <td>frozenSheet</td> <td>{r:0, c:0, tr:0, tc:0, flc:‘red’, si:0}</td> <td>冻结行列<br>r：表示第一条行冻结线所在的位置，默认值0<br>tr：表示第二条行冻结线所在的位置，默认值0<br>c：表示第一条列冻结线所在位置，默认值0<br>tc：表示第二条列冻结线所在的位置，默认值0<br>flc：表示冻结线的颜色，默认值’red’<br>si：表示第几张表，默认值是当前选中的表单</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>autoFitColumns</td> <td>{c:[1,3,5], si:0}</td> <td>自适应列宽<br>c：列的索引（必填）-1表示所有列<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>autoFitRows</td> <td>{r:[2,4,6], si:0}</td> <td>自适应行高<br>r：行的索引（必填）-1表示所有行<br>si：指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setWorkbookOptions</td> <td>{allowCopyPasteExcelStyle:false, allowExtendPasteRange:false，…}<br>{copyPasteHeaderOptions:allHeaders,…}<br>{defaultDragFillType:auto</td> <td></td> <td>…}<br>{showDragFillSmartTag:true,showHorizontalScrollbar:true,showVerticalScrollbar:true,…}<br>{tabStripRatio:0.2}<br>{cutCopyIndi/catorBorderColor:‘red’}<br>{backColor:‘red’}<br>{backgroundImageLayout:center</td> <td></td> </tr> <tr> <td>setWorksheetOptions</td> <td>[{si:, options: {allowCellOverflow: false}}]<br>[{si:, options: {isProtected: true</td> <td></td> <td>false}}]<br>[{si:, options: {clipBoardOptions: 0-3}}]<br>[{si:, options: {sheetTabColor: ‘red’}}]<br>[{si:, options: {frozenlineColor: ‘red’}}]<br>[{si:0, options: {gridline:{color:‘red’,showVerticalGridline:false,showHorizontalGridline:false}}}]<br>[{si:, options: {rowHeaderVisible/colHeaderVisible: true</td> <td></td> </tr> <tr> <td>setShortcutKey</td> <td>{commandName:‘ctrlA’, key:65, ctrl:true, shift:false, alt:false, meta:false}</td> <td>注册快捷键</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setCornerMark</td> <td>[{ range: [{r:0, c:0, rc:1, cc:4}], vi: true</td> <td>false, text: ‘T’, bc: ‘green’, fc: ‘white’, pos: [0,1 ] }]</td> <td>给单元格设置角标range：范围<br>vi：可见性text：角标上的文字<br>bc：背景色，角标的颜色fc：前景色，文字的颜色pos：角标的位置，0，1，2，3分别是左上、右上、右下、左下</td> <td>v4.0及以上</td> </tr> <tr> <td>addContextMenuItems</td> <td>{callback: ‘invokeAction’,subMenu:[{name:’’,text:’’,…}], items: [{name: ‘insertRowsBehind’, text: ‘向后插入行’, workArea: [‘viewport’, ‘colHeader’, ‘rowHeader’, ‘slicer’, ‘corner’, ‘sheetTab’]}]}</td> <td>添加右键菜单项<br>callback：回调函数<br>name：菜单项的key<br>text：菜单项文本<br>workArea：新增菜单项的作用区域，viewport表示数据区域，colHeader表示列头，rowHeader表示行头，slicer表示切片区域，corner表示角标区域sheetTab 表示页签区域<br>subMenu 二级菜单、 三级菜单<br>type：当需要设置分割线时，只需要设置该值为’separator’</td> <td>v4.0及以上</td> <td>text是唯一id，若加入与原有菜单相同的text可能会引起异常设置分割线：sa.action.addContextMenuItems（{items: [{type: ‘separator’}]}, sa.t）</td> </tr> <tr> <td>getRangeValues</td> <td>{range: [{r:0, c:0, rc:1, cc:1}],compression: false}</td> <td>获取指定范围的值 <br>compression true表示压缩后上传 false表示和原来的使用方式一样<br>压缩成了base64的格式,解压后是一个一纬数组 在数组的最后 会把{r:, c:, rc:, cc:}这个放进去 这个就是你获取的范围 比如你调起前端的指令 {r:2, c:2, rc:9, cc:9} 前端会把81个单元格的信息传给后端 如果想获取六行七列单元格的值 则对应数组下标 cc*（6-r）+7-c = 41 向后端发送的指令也发生了改变, invokeControlMethodOnly（model, ‘invokeAction’, [{ si, data: valueBase64, invokemethod: ‘rangeValues’ }]）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setVirtualMode</td> <td>{ callback: ‘invokeAction’, invokemethod: ‘invokemethod’, isOpen: true, unRepeatRequest: false }</td> <td>设置虚模式加载，开启之后，表格在第一次加载和每次滚动的时候会把当前的可是区域发送给后端<br>callback: 回调的请求方法名（必填）<br>invokemethod: invokemethod（必填）<br>isOpen: 用于开启和关闭虚模式加载，设置成false则停止发送请求，默认值是true（选填）<br>unRepeatRequest：不重复发送网络请求，默认为false，也就是滚动区域也会重复发送网络请求（v7.0.1）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setOutlineColumn</td> <td>{c:0, options: {showCheckBox:false, maxLevel:2, collapsed: true}, si:}</td> <td>设置分组列折叠<br>c: 列的索引，<br>options是可选项，<br>showCheckBox设置是否显示复选框，<br>maxLevel控制数据分层级别。默认值是10<br>collapsed设置默认折叠<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>分组列不能和虚模式一起使用</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setRowOutlines</td> <td>{groups: [{index:1, count:5, isHide: false}], si:0}</td> <td>设置行的区域分组，从指定的起始索引将行的范围分组到大纲（范围组）中。<br>groups: 支持传一个或多个分组信息（必选）<br>index: 要分组起始索引（必选）<br>count: 要分组的起始行或列的数目（必选）<br>si: 指定页签的索引，默认是当前工作表（可选）<br>isHide: 显示/隐藏分组（可选）（v6.0.1）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setColumnOutlines</td> <td>{groups: [{index:1, count:5, isHide: false}], si:1}</td> <td>设置列的区域分组，从指定的起始索引将列的范围分组到大纲（范围组）中。<br>groups: 支持传一个或多个分组信息（必选）<br>index: 要分组起始索引（必选）<br>count: 要分组的起始行或列的数目（必选）<br>si: 指定页签的索引，默认是当前工作表（可选）<br>isHide: 显示/隐藏分组（可选）（v6.0.1）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>expandRowOutlines</td> <td>{levels: [0,1], expand: false, si:0}</td> <td>使用指定的级别展开或收起大纲（范围组）<br>levels: 分组的级别，从0开始（必选）<br>expand: 是否展开分组（必选）<br>si: 指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>expandColumnOutlines</td> <td>{levels: [0,1], expand: false, si:1}</td> <td>使用指定的级别展开或收起大纲（范围组）<br>levels: 分组的级别，从1开始（必选）<br>expand: 是否展开分组（必选）<br>si: 指定工作表的索引，默认是当前工作表（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>addSheets</td> <td>{sheets: [{index:1, name:‘ssss’, rc:100, cc:10}]}</td> <td>在指定索引处插入一个工作表<br>sheets: 支持传一个或多个工作表的信息（必选）<br>index: 用来添加工作表的索引（必选）<br>name: 工作表的名称（可选）<br>rc: 工作表的初始行数，默认是200（可选）<br>cc: 工作表的初始列数，默认是20（可选）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>deleteSheets</td> <td>{index: [2,3]}</td> <td>在指定索引处删除工作表<br>index：要删除的工作表索引</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setSheetsVisible</td> <td>{index: [1,2], visible: false}</td> <td>设置工作表的可见性<br>index: 要显示/隐藏的工作表索引<br>显示时index可选 会按照顺序显示隐藏的一个sheet<br>vidible: true（显示）false（隐藏）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>getRangeValues</td> <td>{range:[{r:0, c:0, rc:1, cc:1}], si:0}</td> <td>设置工作表的可见性<br>index: 要显示/隐藏的工作表索引<br>显示时index可选 会按照顺序显示隐藏的一个sheet<br>vidible: true（显示）false（隐藏）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setComment（设置批注）</td> <td>[{ options: [{ r: 0, c: 0, v: ‘这是一个批注’, remove: false }, { r: 1, c: 1, v: ‘’, remove: true }], si: 0 }] （注意参数是个数组）</td> <td>设置批注<br>remove:true（清除批注）/false（设置批注时可以不传）<br>v</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>copyCellsTo（复制指定区域的样式到另一个区域）</td> <td>{range: [{fromRow: 0, fromCol: 0, toRow: 1, toCol: 1, rc: 1, cc: 1}], si:0, type: 0,useSpreadApi: true, isUndo: true}</td> <td>复制指定区域的样式到另一个区域<br>range: [{<br> fromRow: 来源行<br> toRow: 目标列<br> formCol: 来源列<br> toCol: 目标列,<br>}]<br>si: sheetIndex（单元表索引）<br>type: 0：粘贴所有</td> <td>1：粘贴值</td> <td>2：粘贴样式<br>useSpreadApi: 是否使用spread复制API（spread复制API的复制逻辑与手动复制粘贴的逻辑一样，可以实现公式更新）（v6.0.1）<br>如果想复制整行，可以把对应的fromCol设置成-1 对应的复制整列可以把fromRow设置成-1<br>（源和目标两片区域大小必须一样所以rc和cc指的既是from也是to的行数或列数）<br>例如，将第一行的数据复制到第二行：sa.action.copyCellsTo（{range: [{fromRow: 0, fromCol: -1, toRow: 1, toCol: -1, rc: 1, cc: 20}], si:0, type: 0}, sa.t）</td> </tr> <tr> <td>setRowFilter（设置过滤）</td> <td>[{ r: 4, c: 2, rc: 8, cc: 9, si: 0 }]（注意参数是个数组）</td> <td>设置过滤区域<br>r:起始行的索引<br>c:起始列的索引<br>rc:要设置行的个数<br>cc:要设置列的个数</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setHyperLinkCell（设置超链接）</td> <td>{ callback:, invokemethod:, si:, range: [{r:1, c:1, rc: 10, cc:1, options:{text:‘超链接’,value:'<a href="https://www.baidu.com',color:'red',visitedColor:'blue',toolTip" target="_blank">https://www.baidu.com</a><a href="https://www.baidu.com',color:'red',visitedColor:'blue',toolTip" target="_blank">’,color:‘red’,visitedColor:‘blue’,toolTip</a>: ‘超链接’ }}] }</td> <td>callback: 回调的请求方法名（必填）<br>invokemethod: invokemethod（必填）<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>range:区域范围（必填）<br>options:超链接相关（可选） {<br>text:单元格显示的内容<br>value:点击超链接跳转的地址（不传时会向后端发送指令）SpreadJS的超链接支持许多协议，例如：http / https / ftp / file / mailto…。此外，还支持以“ sjs：//”开头的url，它引用工作表位置如sjs://Sheet1!A1:B2 也支持邮件的跳转<br>color:超链接访问前的前景色<br>visitedColor:超链接访问后的前景色<br>toolTip:表示超链接的提示消息，当鼠标悬停在带有超链接的单元格上时显示。</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setSheetName 设置表名</td> <td>[{oldName:‘sheet1’,si:0,newName:‘资产负债表’}]</td> <td>oldName要设置的表的名称<br>si要设置的表的索引<br>newName表的新名称<br>oldName与si都可不传</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setZoom（设置缩放比例）</td> <td>{zoom:0.25~4}</td> <td>默认1</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setSheetDefaults（设置spread默认配置）</td> <td>{ si, setting: { rowHeight: value，… } }</td> <td>si要设置的表的索引<br>setting 配置<br>rowHeight：行高<br>colWidthl : 列宽</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>addToolbarItems（向工具栏里添加自定义按钮）</td> <td>自定义按钮为单个按钮<br>[{<br> key: ‘CheckFormula2’, // 唯一标识<br> icon: ‘kdfont kdfont-shanchuxing’, // 字体图标<br> title: {<br> zh_CN: ‘中文’,<br> zh_TW: ‘繁体’,<br> en_US: ‘english’<br> }, <br> showTitle: true,<br> callback: ‘sss’,<br> invokeAction: ‘sssss’<br>}]<br>自定义按钮为下拉菜单<br>[{<br> key: ‘CheckFormula0’, // 唯一标识<br> icon: ‘kdfont kdfont-shanchuxing’, // 字体图标<br> dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击<br> title: {<br> zh_CN: ‘中文’,<br> zh_TW: ‘繁体’,<br> en_US: ‘english’<br> }, <br> showTitle: true,<br> item: [ // 下拉项<br> {<br> title:{<br> zh_CN: ‘中文’,<br> zh_TW: ‘繁体’,<br> en_US: ‘english’<br> } ,<br> icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，<br> callback: ‘sss’,<br> invokeAction: ‘sssss’,<br> key: ‘AutoFitColumn0123’, }<br> ]<br>}]</td> <td>key ：按钮标识<br>icon： 字体图标<br>title： tips或下拉项的内容，为兼容国际化，建议传对象形式的参数，包含三种语言显示的文字，也可直接传字符串<br>callback 后端具体实现的类<br>invokeAction 后端同于同一处理spread的接口<br>showTitle: 是否显示标题文字<br>item: 下拉选项</td> <td>v5.0及以上</td> <td></td> </tr> <tr> <td>setActiveSheet（切换页签）</td> <td>{name:’’,si:’’}</td> <td>激活/选中指定页签<br>name：要切换页签的名字<br>si：要切换页签的索引<br>这两个传一个就行</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>excuteToolbarActions</td> <td>{name: ‘SwitchView’, // 按钮标识itemName: ‘SwitchView_ValueView’ // 执行的按钮活动}</td> <td>name: 按钮标识<br>itemName: 执行的按钮活动</td> <td>v5.0及以上</td> <td></td> </tr> <tr> <td>setToolbarGroups（自定义工具栏分组）</td> <td>[{<br> name: ‘默认1’,<br> key: ‘default1’,<br> toolbarItems: [{key: ‘Undo’},{key: ‘Undo’}， // 默认功能<br> {<br> key: ‘CheckFormula0’, // 唯一标识<br> icon: ‘kdfont kdfont-shanchuxing’, // 字体图标<br> title: { zh_CN: ‘中文’, zh_TW: ‘繁体’, en_US: ‘english’ }, <br> showTitle: true,<br> items: [ // 下拉项<br> {<br> title:{ zh_CN: ‘中文’,zh_TW: ‘繁体’,en_US: ‘english’ } ,<br> icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，<br> callback: ‘sss’,<br> invokeAction: ‘sssss’,<br> key: ‘AutoFitColumn0123’ }<br> ]<br> },<br> ],<br> }]</td> <td>name: 需要显示的分组名称<br>key: 分组唯一的key值，用于辨识分组，不可重复<br>toolbarItems: 工具栏需要显示的按钮，数组顺序即按钮顺序。自定义功能参数与添加自定义按钮（addToolbarItems）相同。需要注意的是，自定义添加了一个新参数，isCustom，如果是自定义按钮这个参数必填true<br>提示：默认情况下，默认分组一直存在，因此自定义工具栏分组时，如果不需要修改默认分组，则不需要传默认分组的参数，只需要传扩展分组，会自动将扩展分组添加到分组中。传递包含默认分组的参数则视为修改默认分组。</td> <td>v5.0及以上</td> <td></td> </tr> <tr> <td>setToolbarCustomExpend（自定义公式扩展区域）</td> <td>{itemConfig:{<br> showTitle: true,<br> key: ‘CheckFormula0’, // 唯一标识<br> icon: ‘kdfont kdfont-shanchuxing’, // 字体图标<br> title: { zh_CN: ‘测试’,zh_TW: ‘繁体’,en_US: ‘english’ },<br> callback: ‘autoFitColumn’, // 回传给后端的，用于功能的具体实现<br> invokeAction: ‘invokeAction’, // 回传给后端的，用于处理前端spread所有的请求,<br> dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击<br> items: [ // 下拉项<br> {<br> title:{zh_CN: ‘测试’,zh_TW: ‘繁体’,en_US: ‘english’},<br> callback: ‘sss’,icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，<br> invokeAction: ‘sssss’,<br> key: ‘AutoFitColumn0123’, }<br> ]}, <br>inputConfig: {<br> callback: ‘autoFitColumn’, // 回传给后端的，用于功能的具体实现<br> invokeAction: ‘invokeAction’<br>}}</td> <td>itemConfig：自定义按钮配置，与自定义按钮相似inputConfig：自定义输入配置</td> <td>v5.0及以上</td> <td></td> </tr> <tr> <td>setToolbarCustomInputValue（与setToolbarCustomExpend搭配使用，自定义公式扩展区域输入值）</td> <td>string’</td> <td></td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>setCustomSheetMenu（自定义页签菜单右键顺序）</td> <td>[{name: ‘hideSheet’}, {type: ‘separator’}, { name: ‘setBGC’ }, {name: ‘unhideSheet’}, {name: ‘insertRowsBehind’} ,{name: ‘insertSheet’}]</td> <td>name: 菜单名称<br>（insertSheet：插入，deleteSheet：删除，hideSheet：隐藏，unhideSheet：显示，setBGC：颜色选择器）<br>type: 只有分割线才使用type（type: ‘separator’）</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>getLockedCells（获取锁定单元格）</td> <td>[{ callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si: 0, compression: false }]</td> <td>callback：回调函数<br>invokeAction：后端同一处理spread的接口<br>si：sheetIndex<br>compression：是否压缩</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>hideRow（隐藏行）/unhideRow（显示行）</td> <td>{data: [1, 2], si: 0, isUndo: true}</td> <td></td> <td>{data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似</td> <td>在工作表的指定索引处添加一行/列<br>data：指定行（行）的索引（必填）<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>index: 要隐藏/显示的行的索引<br>count: 要隐藏/显示多少行</td> </tr> <tr> <td>hideColumn（隐藏列）/unhideColumn（显示列）</td> <td>{data: [1, 2], si: 0, isUndo: true}</td> <td></td> <td>{data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似</td> <td>在工作表的指定索引处添加一行/列<br>data：指定行（列）的索引（必填）<br>si: 指定工作表的索引，默认是当前工作表（可选）<br>index: 要隐藏/显示的列的索引<br>count: 要隐藏/显示多少列</td> </tr> <tr> <td>setSpreadParams</td> <td>{data: {key: value}}</td> <td>添加全局spread属性（也可直接通过JSON设置，在spread新增一个自定义属性serverSpreadParams，将需要全局定义的属性作为对象赋值给serverSpreadParams）<br>key：属性名<br>value：属性值</td> <td>v5.0.027及以上</td> <td>目前已有的属性：<br>isEditSendRequest：鼠标进入编辑态与退出编辑态时是否向后端发送网络请求（5.0.0.27）<br>allowMaxInsert9999：插入行列支持最大输入9999（v6.0.1）<br>useNewInsert：使用新版插入（向上插入/向下插入）（v6.0.1）<br>usePtFontSize：使用pt作为font-size的单位，默认是px（v6.0.7）<br>useSheetPrintConfig：按工作表保存打印配置（v6.0.12）<br>useExcelCellsFormat：使用与excel同步的单元格格式（v6.0.14）<br>isUpdateStyleSendRequest：样式更新时发送网络请求（v7.0.0）<br>isDeleteLockedSendRequest：delete快捷键清除锁定单元格数据时向后端发送网络请求（v7.0.4）</td> </tr> <tr> <td>setGCParams</td> <td>{data: {key: value}}</td> <td>添加全局GC属性（与setSpreadParams不同的是，这个是GC层面的属性，设置之后不会随着json的设置而覆盖，建议本条指令在所有指令之前设置）<br>key：属性名<br>value：属性值</td> <td>v6.0及以上</td> <td>目前已有的属性：calcPrecision：计算精度，默认为14，当出现精度问题时，可以调整为12。不建议调整其他值，可能会出现计算问题（v6.0.1）</td> </tr> <tr> <td>hideZero</td> <td>{ishide: true, si: 0}</td> <td>ishide：为0时是否隐藏<br>si：sheetIndex</td> <td>v4.0及以上</td> <td>如果表格中已经存在0值，指令开启后需要重新计算才能将已有的0隐藏</td> </tr> <tr> <td>setGroupColNode（设置分组列）</td> <td>[{r:number, c:number, groupNodeType:1</td> <td></td> <td>2</td> <td></td> </tr> <tr> <td>setStatusBarVisible</td> <td>{visible: false}</td> <td>设置spread底部缩放的显示（true）与隐藏（false）<br>{visible: true</td> <td>false}</td> <td>v4.0及以上</td> </tr> <tr> <td>setValidator</td> <td>{type: 0, option: {vt: 1, v1: ‘1’, vco: 0, ht: 0, hsc: ‘#ff0000’, es: 0, et: ‘不允许输入’}, range: [{row: 0, col: 0, rowCount: 5, colCount: 5}], callback: ‘invokeAction’, invokemethod: ‘test’, si: 0}</td> <td>type: number （0:设置, 1:取消）<br>option: object （配置项，详见备注）<br>range: array （应用的单元格）<br>callback: 回调函数<br>invokemethod: 回调函数<br>si: sheetindex</td> <td>v4.0及以上</td> <td>VALIDATOR_TYPE = ‘vt’ // 验证器<br>IS_FORMULA_LIST_VALIDATOR = ‘iflv’//<br>VALIDATOR_COMPARISON_OPERATOR = ‘vco’// 比较运算符<br>VALUE_1 = ‘v1’// 值一<br>VALUE_2 = ‘v2’// 值二<br>IS_INTEGER = ‘ii’ // 是否是整数<br>SHOW_INPUT_MSG = ‘sim’// 显示输入消息<br>INPUT_TITLE = ‘it’// 标题,<br>INPUT_MESSAGE = ‘im’// 信息<br>IGNORE_BLANK = ‘ib’ // 忽略空白<br>SHOW_ERROR_MESSAGE = ‘sem’ // 显示错误信息<br>ERROR_STYLE = ‘es’// 错误类型<br>ERROR_TITLE = ‘et’// 错误标题<br>ERROR_MSG = ‘em’ // 错误信息<br>HIGHLIGHT_TYPE = ‘ht’ // 圆圈样式<br>DOGEAR_POSITION = ‘dp’// 位置 与圆圈样式有关 类型dogear才会用到<br>ICON_POSITION = ‘ip’// 位置 与圆圈样式有关 类型icon才会用到<br>HIGHLIGHT_STYLE_COLOR = ‘hsc’// 颜色<br>HIGHLIGHT_STYLE_IMAGE = ‘hsi’// 图标 与圆圈样式有关 类型icon才会用到</td> </tr> <tr> <td>batchExportExcelFiles</td> <td>base64加密过的对象：{data:[{json:’’},…], fileName:‘fileName’}</td> <td>json：spread的json文件，注意，这里是原生的json字符串，不是通过base64加密过的<br>fileName：导出的文件名称</td> <td>v4.0及以上</td> <td></td> </tr> <tr> <td>syncSpreads</td> <td>{ slaveSpreadKeys: [key2, key3], syncOptions: [scrollBar, rowHeight, colWidth, zoom], sync: true }</td> <td>多表联动，仅仅对各个当前活动的工作表进行联动处理<br>slaveSpreadKeys：需要联动的spread的key<br>syncOptions：需要联动的属性，目前只支持scrollBar（滚动）, rowHeight（行高）, colWidth（列宽）, zoom（缩放）<br>sync：是否联动，如果已经联动的填写这个参数用于关闭联动</td> <td>v6.0.15及以上</td> <td></td> </tr> </tbody> </table> <h1><a id="7__171"></a>7 插件示例</h1> <pre><div class="hljs"><code class="lang-java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Spread</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Container</span></span>{ <span class="hljs-keyword">private</span> SpreadPostDataInfo postData ; <span class="hljs-comment">//删除行</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">deleteRows</span><span class="hljs-params">(List&lt;Integer&gt; postDatas)</span></span>{ packInvokeListParams(postDatas); <span class="hljs-keyword">this</span>.getActionService().deleteRows(<span class="hljs-keyword">new</span> SpreadEvent(<span class="hljs-keyword">this</span>,postData)); } <span class="hljs-comment">//删除列</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">deleteColumns</span><span class="hljs-params">(List&lt;Integer&gt; postDatas)</span></span>{ packInvokeListParams(postDatas); <span class="hljs-keyword">this</span>.getActionService().deleteColumns(<span class="hljs-keyword">new</span> SpreadEvent(<span class="hljs-keyword">this</span>,postData)); } <span class="hljs-comment">//插行</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">addRows</span><span class="hljs-params">(List&lt;Integer&gt; postDatas)</span></span>{ packInvokeListParams(postDatas); <span class="hljs-keyword">this</span>.getActionService().addRows(<span class="hljs-keyword">new</span> SpreadEvent(<span class="hljs-keyword">this</span>,postData)); } <span class="hljs-comment">//插列</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">addColumns</span><span class="hljs-params">(List&lt;Integer&gt; postDatas)</span></span>{ packInvokeListParams(postDatas); <span class="hljs-keyword">this</span>.getActionService().addColumns(<span class="hljs-keyword">new</span> SpreadEvent(<span class="hljs-keyword">this</span>,postData)); } <span class="hljs-comment">//切换单元格</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">entryRowClick</span><span class="hljs-params">(<span class="hljs-keyword">int</span> arg)</span></span>{ getActionService().selectedSpread(<span class="hljs-keyword">new</span> SpreadEvent(<span class="hljs-keyword">this</span>,postData)); } <span class="hljs-comment">//前端通用指令</span> <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">askExecute</span><span class="hljs-params">(LinkedHashMap&lt;String,Object&gt; postDatas)</span></span>{ packInvokeParams(postDatas); getActionService().askExecute(<span class="hljs-keyword">new</span> SpreadEvent(<span class="hljs-keyword">this</span>,postData)); } } </code></div></pre> <p>插行示例：<br /> 前端控制台模拟后端指令<br /> <img src="/download/01004a82934bef0d4b92b3699ad2294ba4ce.png" alt="3.png" /></p> <h1><a id="8__212"></a>8 参考链接</h1> <ol> <li><a href="https://vip.kingdee.com/link/s/ZLiPJ" target="_blank">SpreadJS 控件常见问题</a></li> </ol>

导航目录 __

# 变更记录

产品版本 | 更新内容 | 更新日期  
---|---|---  
V5.0.024 | 新增了Spread 表格参数设置后点击确定可保存表格参数的功能，满足了用户需要记住表格参数设置的需求，详见5.1.2 | 2023年7月  
V5.0.025 | Spread表格支持了设定选中区域指令的位置参数，满足了用户希望将选定区域放置在指定的可视范围的需求，详见6：setSelections | 2023年7月  
V6.0.15 | 完善了Spread多表联动的功能，支持多表同步滚动条、行高、列宽、缩放，提高用户设置报表模板的易用性，详见6：syncSpreads | 2024年7月  
v7.0.1 | 1、getSpreadJson与setSpreadJson现在支持对单个工作表（sheet）进行操作，详见6：getSpreadJson、setSpreadJson  
2、出于第三方商业合规性要求，伙伴/二开环境的表单设计器中将不再内置SpreadJS控件，详见1：下架说明 | 2024年10月  
v7.0.3 | 1、updatavalue支持字典模式传输数据，降低传输数据量，提升性能，详见6：updatavalue  
2、优化了SpreadJS表格设置列格式时数据导入的性能，避免页面卡顿 | 2024年12月  
V7.0.4 | setSpreadParams新增isDeleteLockedSendRequest参数：delete快捷键清除锁定单元格数据时向后端发送网络请求，可二开实现框选单元格包含锁定单元格时，仅清除非锁定单元格的数据，详见6：setSpreadParams | 2024年12月  
V8.0.1 | 升级了Spread控件版本，增强了相关功能并修复了若干问题，满足不同客户的使用场景。注意：如果后端开发用到了GCExcel组件，则为了前后端组件版本适配，需要同步升级后端GCExcel组件，详见5.2 | 2025年9月  
  
# 1 功能介绍

SpreadJS 是一款基于 HTML5 的纯前端电子表格控件，提供了与 Excel 高度类似的功能和兼容性。  
该控件仅在PC端支持。

## 下架说明

出于第三方商业合规性要求，SpreadJS 控件的使用权限严格限定于本公司旗下标品业务（包括但不限于“星空”、“星瀚”系列），若在二开中继续使用，需自行承担潜在的商业风险。  
从苍穹 7.0.1 版本开始，针对伙伴或二开的苍穹环境在其表单设计器中隐藏 SpreadJS 控件，在控件面板中无法选到该控件参与布局设计。若客户有在线使用类似 SpreadJS 控件的需求，我们建议：

  * 自行采购 SpreadJS 官方授权，并集成到项目中使用
  * 选择其他功能类似的第三方表格控件进行集成



# 2 控件对象

`kd.bos.form.spread`

# 3 视觉展示

![1.png](https://vip.kingdee.com/download/0100f9b26b4809864739b9609ec460d642f6.png)

# 4 属性说明

## 4.1 通用属性

> 通用属性包含字段和控件的一些公有的属性，如宽高，帮助文本等等。请参考[通用属性](https://vip.kingdee.com/article/215559076720798976)

## 4.2 样式属性

> 样式属性是每个控件在设计器右侧样式栏可以设置的属性，请参考[样式属性](https://vip.kingdee.com/article/252017936767406336)

## 4.3 业务属性

属性名 | 类型 | 运行时参数名 | 默认值 | 说明 | PC  
---|---|---|---|---|---  
显示编辑工具栏 | boolean | setb | true | 是否默认显示工具栏，开启时将自动展开，关闭时则自动收起。无论默认状态如何，随时可以手动展开或收起工具栏 | V4.0及以上  
允许多页签 | boolean | smt | false | 是否开启多页签模式，开启后控件底部将显示页签栏，支持快速切换和新增。导入功能也与该属性相关，开启时，允许同时选择多个sheet导入，关闭时仅允许选择单个sheet导入 | V4.0及以上  
支持导出excel | boolean | ee | false | 是否支持批量导出，开启后，控件将支持批量导出功能。可以使用 batchExportExcelFiles 指令，将多个 Spread 的数据合并导出为一个 Excel 文件。适用于列表页面，需要批量导出多个Spread数据的场景 | V4.0及以上  
  
# 5 功能详情

## 5.1 工具栏

### 5.1.1工具栏功能介绍

spread工具栏配置了表格常用的一些功能

  * 对于单元格样式的一些设置：字体样式、字体大小、字体颜色、文本居中、单元格背景色
  * 对于单元格格式的一些设置：常规、日期、时间、会计专用等等
  * 对于工作表的一些设置：冻结表格、过滤表格、打印、显示公式等等



以往的文章中我们已经对这些特性进行过介绍  
链接直达：  
[Spread工具栏新特性](https://vip.kingdee.com/article/390596666833372416)  
[Spread 全新视觉交互升级](https://developer.kingdee.com/article/502501692391512576)  
[Spread支持查找替换功能](https://developer.kingdee.com/article/85798461483727872)  
[Spread支持数据有效性](https://developer.kingdee.com/article/322746947583383040)  
[Spread支持打印](https://developer.kingdee.com/article/137514916042362624)  
[Spread支持自定义单元格格式](https://developer.kingdee.com/article/324229535094933760)  
[Spread支持条件格式](https://developer.kingdee.com/article/523501187660638464)

### 5.1.2表格设置

设置表格配置参数后（功能入口：表格工具栏的配置按钮），点击确定，可将参数保存到当前表格，该参数只对当前表格生效。再次打开该表格或其他用户使用该表格，该参数依旧生效。  
![2.png](https://vip.kingdee.com/download/010012cffaed11c044ac9662e9dfc8e96a88.png)

## 5.2 SpreadJS 版本升级

鉴于在过往客户反馈中许多问题需通过升级 SpreadJS 版本解决，为提升产品稳定性与功能性，我们在苍穹平台 8.0.1 版本中，正式将内置的 SpreadJS 第三方控件从 V14.2.6 升级至 V17.1。  
升级影响与必要操作：

  * 如果您的项目后端使用了 GCExcel 组件，必须将其同步升级至 V7 版本，以确保前后端功能一致性与稳定性；
  * 为了提升存储何计算性能，SpreadJS 第三方组件设计了共享公式，当存在两个相同的公式时将提取到sharedFormulas中，直接解析 SpreadJson 数据获取公式可能获取正确的公式。官方建议后端通过 GCExcel 的 api 去获取和改动这些信息；
  * 除了公式，由于 SpreadJS 产品版本持续在升级，SpreadJson 中的数据结构很有可能发生变化，建议都通过 GCExcel 的 api 执行操作；



版本详情：有关 V17.1 版本解决的具体问题与新特性，请参阅官方发布说明：  
[17.1 < 发布说明 | 葡萄城 SpreadJS 表格控件在线文档](/tolink?target=https://demo.grapecity.com.cn/spreadjs/help/docs/rnotes/171)

# 6 SpreadJS接口介绍

spread功能高度依赖业务，因此部分功能需要后端配置响应的指令才能正常使用，比如插行、删行。

指令 | 参数 | 功能 | 支持版本 | 备注  
---|---|---|---|---  
callbackAction | {callback:‘invokemethod’, invokemethod:‘addRows’} | 回调函数指令，后端告诉前端它还有后续动作 | v4.0及以上 |   
setSpreadJson | spreadJS压缩过后的json串 | 加载spreadJS的json串  
支持加载sheet的JSON字符串（V7.0） | v4.0及以上 |   
appendRows | {count:1, si:0} | 在指定工作表尾部追加count行  
count：行数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
appendCols | {count:1, si:0} | 在指定工作表尾部追加coun列  
count：列数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
deleteRow | {data:[2, 1], si:0} |  | {data: [{index:1, count: 2}], si:} | 删除工作表指定索引处的行  
data：指定行的索引（必填），删除连续行时索引应该倒序从后往前删除  
si：指定工作表的索引，默认是当前工作表（可选）  
index：删除的起始位置  
count：删除多少行  
deleteCol | {data:[2, 1], si:0} |  | {data: [{index:1, count: 3}], si:} | 删除工作表指定索引处的列  
data：指定列的索引（必填），删除连续行时索引应该倒序从后往前删除  
si：指定工作表的索引，默认是当前工作表（可选）  
index：删除的起始位置  
count：删除多少列  
insertRow | {data:[1, 2], dir: ‘bottom’, copyStyle: false, copySpans: false, si:0} |  | {data:[{ index: 1, count:2 }], dir: ‘bottom’, copyStyle: false, si:0} | 在工作表的指定索引处添加一行  
data：指定行的索引（必填）  
copyStyle：是否复制指定行的样式，默认复制（可选）  
si：指定工作表的索引，默认是当前工作表（可选）  
index：要复制的行的索引  
count：要复制多少行  
dir：向上插入还是向下插入（默认向上如需要向下输入则需要将dir设置为bottom）  
copySpans：是否复制指定行的融合（这个只能添加行的时候用，列还不支持）  
insertCol | {data:[1, 3], dir: ‘after’, copyStyle: false, si: 0} |  | {data:[{ index:1, count: 2}], dir: ‘after’, copyStyle: false, si: 0} | 在工作表的指定索引处添加一列  
data：指定列的索引（必填）  
copyStyle：是否复制指定列的样式，默认复制（可选）  
si：指定工作表的索引，默认是当前工作表（可选）  
index：要复制的列的索引  
count：要复制多少列  
dir：向前插入还是向后插入（默认向前如需要向后输入则需要将dir设置为after）  
setColumnsWidth | {index:[0], num:20, si: 0,area: ‘viewport’} | 设置列宽  
index：指定列的索引（必填）  
num：宽度（以像素为单位，必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
area：指定区域（可选）  
area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域 | v4.0及以上 |   
setRowsHeight | {index:[0], num:20, si: 0,area: ‘viewport’} | 设置行高  
index：指定行的索引（必填）  
num：高度（以像素为单位，必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
area：指定区域（可选）  
area的值有三种：colHeader（列头）、rowHeader（行头）、viewport（单元格区域）默认是单元格区域 | v4.0及以上 |   
getColumnsWidth | {index:[2,3], callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si:‘0’} | 获取列宽  
index：指定行（列）的索引（必填）  
callback：回调的请求方法名（必填）  
invokemethod：invokemethod（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
getRowsHeight | {index:[2,3], callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si:‘0’} | 获取行高  
index：指定行（列）的索引（必填）  
callback：回调的请求方法名（必填）  
invokemethod：invokemethod（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setRowsVisible | {rows: [1, 3], value: false, si:0} | 设置行可见性rows：行的索引，是一个数组（必填）  
value：可见性true |  | false（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
setColumnsVisible | {cols: [2, 4], value: true, si:0} | 设置列可见性cols：列的索引，是一个数组（必填）  
value：可见性true |  | false（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
lockCell | [{r:0,c:0,rc:1,cc:1}] |  | {selections: [{r:0, c:0, rc:1, cc:1}], si:0} | 锁定工作表指定区域的单元格  
selections：指定表格区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
unlockCell | [{r:0,c:0,rc:1,cc:1}] |  | {selections: [{r:0, c:0, rc:1, cc:1}], si:1} | 解锁工作表指定区域的单元格  
selections：指定表格区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
lockSheet | {si: [0] } | 锁定工作表  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
unlockSheet | {si: [1] } | 解锁工作表  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
updataValue | {si:0, area:’’, cells: [{r:0, c:0, v:1}]} |  | [{r:0, c:0, v:1, area:’’,}] |   
setSpan | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 合并指定区域的单元格  
range：指定表格区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setCellStyle | {data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bl:{bls:[‘dashDot’], blc:[’#00f’]} } }] ,si:} //设置单元格边框样式  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{bkc:’#666’, frc:’#000’} }],si:} //设置单元格的前景色和背景色  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{f: ‘8pt Arial’} }],si:} //设置单元格字体  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{fm:‘0.00%’} }],si:} //设置单元格格式  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{va:1, ha:1} }],si:} //设置单元格垂直、水平对齐方式  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{l:true} }],si:} //指示是否将单元格标记为已锁定  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ww:true} }],si:} //设置单元格是否自动换行  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{stf:true} }],si:} //指示内容是否收缩以适应  
{data:[{range: [{r:2, c:2, rc:2, cc:2}], style:{ti:2} }],si:} //表示单元格中文本的缩进单元数（一个整数值），其中增量1表示8个像素。  
{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: ‘colHeader’, style:{td:} }]} //td的值为数字 设置文本下划线（underline）:1 双下划线（doubleUnderline）:8 删除线（lineThrough）:2 无（none）:0   
{data: [{range: [{r:2, c:2, rc:2, cc:2}],area: ‘colHeader’, style:{ep:true} }]} //ep为一个布尔值 true表示设置文本省略符 文本省略符的悬浮提示是默认存在的（显示文本全部内容），目前（2020.08）没有相关属性。 | 设置单元格样式/格式  
range：指定表格区域（必填）  
style：设置样式信息（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
设置单元格边框中的bls表示边框线的样式，可以设置成：dashDot,dashDotDot,dashed,dotted,double,empty,hair,medium,mediumDashDot,mediumDashDotDot,mediumDashed,slantedDashDot,thick,thin。blc表示边框线的颜色。设置行头区域,列的索引设置为0;设置列头区域,行的索引设置成0;rc和cc一般设置成1;设置单元格格式，fm是自定义单元格格式，与excel自定义单元格格式规则相同 | v4.0及以上 |   
fieldInsertRow | {range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 } | 在指定的工作表区域新增行/列，并复制指定行/列的样式  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
styleIndex：复制指定行/列样式的索引（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
fieldInsertCol | {range:{r:0,c:0,rc:1,cc:1,},index:1 ,count:1 ,styleIndex:0, si:0 } | 在指定的工作表区域新增行/列，并复制指定行/列的样式  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
styleIndex：复制指定行/列样式的索引（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
fieldDeleteRow | {range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0} | 删除指定的工作表区域的行  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
fieldDeleteCol | {range:{r:0,c:0,rc:1,cc:1},index:1,count:1,si:0} | 删除指定的工作表区域的列  
range：指定表格区域（必填）  
index：指定插入的索引（必填）  
count：插入的行/列数（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setFListCell | {cell:[{r:1,c:1}], selectType:0, dst:1,lockClickable: true} |  | {range: [{r:0, c:0, rc:1, cc:1}], selectType:0, dst:1,lockClickable:true} | 设置f7类型单元格  
selectType：0（支持模糊查询），1（不支持模糊查询）  
dst：0（默认风格），1（显示风格为下拉）  
lockClickable：true（支持锁定状态下的点击）  
setComboCell | [{cell:{r:0,c:0}, option:[“a”,“b”,“c”]}] | 设置下拉列表类型单元格  
cell：指定单元格（必填）  
option：下拉项（必填） | v4.0及以上 |   
setBtnCell | [{cell:{r:0,c:0},text:’’}] | 设置按钮类型单元格 | v4.0及以上 |   
setCustomBtnCell | [{cell:{r:0,c:1},text:’’}] | 设置自定义按钮类型单元格 | v4.0及以上 |   
setCellTag | [{r: 2, c: 10, st: true, pos:[0,1,2,3], bc:‘green’, height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true}] |  | {range: [{r: 2, c: 10, rc: 10, cc: 10}], st: true, pos:[0,1,2,3], bc:‘green’, height: 4, width: 4, hoverHeight: 4, hoverwidth: 4, canClick: true} | 给单元格设置特殊标识（小红点）  
r：指定行（必填）  
c：指定列（必填）  
st：控制是否显示小红点（必填）  
bc：背景颜色，同css颜色设置一致  
height：标识高度（仅在canClick为true时支持, v6.0.1）  
width标识宽度（v6.0.1）  
hoverHeight：鼠标可以覆盖到的高度，也就是实际可点击的高度（v6.0.1）  
hoverWidth：鼠标可以覆盖到的宽度（v6.0.1）  
canClick：是否可点击，点击时向后端发送网络请求（v6.0.1）  
pos：指定小红点的位置，0-3分别代表左上、右上、右下、左下（必填）（v6.0.1）  
resetCell | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 重置指定工作表区域的单元格类型  
range：指定工作表区域（必填）  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
getSpreadJson | {callback:‘invokemethod’, invokemethod:‘invokeAction’} | 获得表格bsae64格式文件，回传请求指令和后台触发事件  
callback：回调的请求方法名（必填）  
invokemethod：invokemethod（必填）  
sheetName：sheet页的名称（选填，填写该参数后会获取对应sheet页的json，V7.0支持） | v4.0及以上 |   
setLookupData | {r:0,c:0,data:[]} | 获取f7单元格类型的lookup数据 | v4.0及以上 |   
setCustomFormulaCell | {cell:[{r:0,c:0}], si:0} | 设置自定义的公式单元格 | v4.0及以上 |   
setAllCustomFormulaCell | {si: 0} | 设置所有单元格都为自定义公式单元格 | v4.0及以上 |   
registerCustomFormula | [{ formulaName: ‘FACTORIAL’, argsNum: 1, minArgs: 1, maxArgs: 1, returnType: ‘’, isAsynUpdate: true, defaultValue: ‘loading…’, evaluateMode: 0, callback: ‘autoFitColumn’, invokeAction: ‘invokeAction’, descriptionInfo: { description: ‘自定义公式’, parameters: [{ name: ‘’ }] }}] | 注册自定义公式  
isAsynUpdate：是否异步更新  
defaultValue：默认值  
evaluateMode：计算模式，0 单元格需要计算时进行重算 1 只计算一次 2 定时计算  
callback：回传给后端的，用于功能的具体实现  
invokeAction：回传给后端的，用于处理前端spread所有的请求 | v4.0及以上 |   
setFormula | [{r:0,c:0,f:’’}] |  | {cell: [{r:0,c:0,f:’’}], si:0} | 给单元格设置公式  
cell：指定单元格的行列和公式，f为""则清除公式（必填）  
si：指定工作表的索引，默认是当前工作表（可选）  
getFormula | { callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si: 0,options:[{r:0,c:0,rc:10,cc:10}] |  | ‘all’} | callback：回调函数  
invokeMethod：invokeMethod  
si：指定工作表的索引，默认是当前工作表（可选）  
options：获取的单元格（all为全部）  
setSelections | {r:0, c:0, rc:1, cc:1, si:0, position: { row: ‘nearest’, col: ‘nearest’ }, autoFocus: true} | 设置指定工作表区域单元格选中（会跳转到指定页签的指定单元格）  
r：行索引,  
c：列索引,  
rc：行选中范围单元格数量,  
cc：列选中范围单元格数量,  
si：指定工作表的索引，默认是当前工作表（可选）  
position：显示的区域相对浏览器的位置（center：中心，left：左边，nearest：最近的边缘，right：右侧）（v6.0.1）  
autoFocus：是否自动聚焦（v6.0.1） | v4.0及以上 |   
exportExcelFile | {fileName: ‘test’} | 导出excel文件fileName：文件名（必填） | v4.0及以上 |   
lockToolbarItems | [{name: ‘FontStyle’, isLock: true | false, isHide:true｜false}, group: ‘default’] or {isLock: true | false, allowSingleUnlock: true | false}  
hideContextMenuItems | {isHide: true/false} or [{name:‘pasteOptions’ , isHide: true,subMenu: ‘pasteValues’}] | 第一种参数格式，直接隐藏整个右键菜单  
第二种参数格式，隐藏指定的菜单项  
name：菜单的key  
isHide：是否隐藏  
subMenu：子菜单的key，隐藏某个子菜单时，需要同时传递name和subMenu  
菜单项对应的key可见 [SpreadJS控件右键菜单数据结构](https://vip.kingdee.com/link/s/ZLiTn) | v4.0及以上 |   
setViewOptions | [{type: ,text: ,baseType: }] | 设置当前工作表的视图 | v4.0及以上 |   
print | {index:, showPanel: true, printInfo: [{index:[],info:{}}]} | 打印spread表格  
showPanel是否显示打印面板 | v4.0及以上 |   
clearCellsStyle | [{r:0,c:0,rc:1,cc:1}] | 清除单元格格式 | v4.0及以上 |   
controlToolbarItems | [{name: 工具栏按钮名称, isCtl: true/false}] | 设置需要跟后端交互的工具栏按钮 | v4.0及以上 |   
setDisplayContent | [{key: ,callback ,invokemethod, text: , s: {w: ‘宽度’, fc: ‘字体颜色’} }, …] | 设置展示区域的内容支持点击向后端发送请求，请求内容与右键相似 | v4.0及以上 |   
clearDisplayContent | [‘key’, …] | 清除展示区域指定内容 | v4.0及以上 |   
closeToolbar | {} | 关闭工具栏 | v4.0及以上 |   
setExpandBtnVisible | {visible: true | false} | 设置工具栏“展开/收起”按钮的可见性 | v4.0及以上  
frozenSheet | {r:0, c:0, tr:0, tc:0, flc:‘red’, si:0} | 冻结行列  
r：表示第一条行冻结线所在的位置，默认值0  
tr：表示第二条行冻结线所在的位置，默认值0  
c：表示第一条列冻结线所在位置，默认值0  
tc：表示第二条列冻结线所在的位置，默认值0  
flc：表示冻结线的颜色，默认值’red’  
si：表示第几张表，默认值是当前选中的表单 | v4.0及以上 |   
autoFitColumns | {c:[1,3,5], si:0} | 自适应列宽  
c：列的索引（必填）-1表示所有列  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
autoFitRows | {r:[2,4,6], si:0} | 自适应行高  
r：行的索引（必填）-1表示所有行  
si：指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
setWorkbookOptions | {allowCopyPasteExcelStyle:false, allowExtendPasteRange:false，…}  
{copyPasteHeaderOptions:allHeaders,…}  
{defaultDragFillType:auto |  | …}  
{showDragFillSmartTag:true,showHorizontalScrollbar:true,showVerticalScrollbar:true,…}  
{tabStripRatio:0.2}  
{cutCopyIndi/catorBorderColor:‘red’}  
{backColor:‘red’}  
{backgroundImageLayout:center |   
setWorksheetOptions | [{si:, options: {allowCellOverflow: false}}]  
[{si:, options: {isProtected: true |  | false}}]  
[{si:, options: {clipBoardOptions: 0-3}}]  
[{si:, options: {sheetTabColor: ‘red’}}]  
[{si:, options: {frozenlineColor: ‘red’}}]  
[{si:0, options: {gridline:{color:‘red’,showVerticalGridline:false,showHorizontalGridline:false}}}]  
[{si:, options: {rowHeaderVisible/colHeaderVisible: true |   
setShortcutKey | {commandName:‘ctrlA’, key:65, ctrl:true, shift:false, alt:false, meta:false} | 注册快捷键 | v4.0及以上 |   
setCornerMark | [{ range: [{r:0, c:0, rc:1, cc:4}], vi: true | false, text: ‘T’, bc: ‘green’, fc: ‘white’, pos: [0,1 ] }] | 给单元格设置角标range：范围  
vi：可见性text：角标上的文字  
bc：背景色，角标的颜色fc：前景色，文字的颜色pos：角标的位置，0，1，2，3分别是左上、右上、右下、左下 | v4.0及以上  
addContextMenuItems | {callback: ‘invokeAction’,subMenu:[{name:’’,text:’’,…}], items: [{name: ‘insertRowsBehind’, text: ‘向后插入行’, workArea: [‘viewport’, ‘colHeader’, ‘rowHeader’, ‘slicer’, ‘corner’, ‘sheetTab’]}]} | 添加右键菜单项  
callback：回调函数  
name：菜单项的key  
text：菜单项文本  
workArea：新增菜单项的作用区域，viewport表示数据区域，colHeader表示列头，rowHeader表示行头，slicer表示切片区域，corner表示角标区域sheetTab 表示页签区域  
subMenu 二级菜单、 三级菜单  
type：当需要设置分割线时，只需要设置该值为’separator’ | v4.0及以上 | text是唯一id，若加入与原有菜单相同的text可能会引起异常设置分割线：sa.action.addContextMenuItems（{items: [{type: ‘separator’}]}, sa.t）  
getRangeValues | {range: [{r:0, c:0, rc:1, cc:1}],compression: false} | 获取指定范围的值   
compression true表示压缩后上传 false表示和原来的使用方式一样  
压缩成了base64的格式,解压后是一个一纬数组 在数组的最后 会把{r:, c:, rc:, cc:}这个放进去 这个就是你获取的范围 比如你调起前端的指令 {r:2, c:2, rc:9, cc:9} 前端会把81个单元格的信息传给后端 如果想获取六行七列单元格的值 则对应数组下标 cc*（6-r）+7-c = 41 向后端发送的指令也发生了改变, invokeControlMethodOnly（model, ‘invokeAction’, [{ si, data: valueBase64, invokemethod: ‘rangeValues’ }]） | v4.0及以上 |   
setVirtualMode | { callback: ‘invokeAction’, invokemethod: ‘invokemethod’, isOpen: true, unRepeatRequest: false } | 设置虚模式加载，开启之后，表格在第一次加载和每次滚动的时候会把当前的可是区域发送给后端  
callback: 回调的请求方法名（必填）  
invokemethod: invokemethod（必填）  
isOpen: 用于开启和关闭虚模式加载，设置成false则停止发送请求，默认值是true（选填）  
unRepeatRequest：不重复发送网络请求，默认为false，也就是滚动区域也会重复发送网络请求（v7.0.1） | v4.0及以上 |   
setOutlineColumn | {c:0, options: {showCheckBox:false, maxLevel:2, collapsed: true}, si:} | 设置分组列折叠  
c: 列的索引，  
options是可选项，  
showCheckBox设置是否显示复选框，  
maxLevel控制数据分层级别。默认值是10  
collapsed设置默认折叠  
si: 指定工作表的索引，默认是当前工作表（可选）  
分组列不能和虚模式一起使用 | v4.0及以上 |   
setRowOutlines | {groups: [{index:1, count:5, isHide: false}], si:0} | 设置行的区域分组，从指定的起始索引将行的范围分组到大纲（范围组）中。  
groups: 支持传一个或多个分组信息（必选）  
index: 要分组起始索引（必选）  
count: 要分组的起始行或列的数目（必选）  
si: 指定页签的索引，默认是当前工作表（可选）  
isHide: 显示/隐藏分组（可选）（v6.0.1） | v4.0及以上 |   
setColumnOutlines | {groups: [{index:1, count:5, isHide: false}], si:1} | 设置列的区域分组，从指定的起始索引将列的范围分组到大纲（范围组）中。  
groups: 支持传一个或多个分组信息（必选）  
index: 要分组起始索引（必选）  
count: 要分组的起始行或列的数目（必选）  
si: 指定页签的索引，默认是当前工作表（可选）  
isHide: 显示/隐藏分组（可选）（v6.0.1） | v4.0及以上 |   
expandRowOutlines | {levels: [0,1], expand: false, si:0} | 使用指定的级别展开或收起大纲（范围组）  
levels: 分组的级别，从0开始（必选）  
expand: 是否展开分组（必选）  
si: 指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
expandColumnOutlines | {levels: [0,1], expand: false, si:1} | 使用指定的级别展开或收起大纲（范围组）  
levels: 分组的级别，从1开始（必选）  
expand: 是否展开分组（必选）  
si: 指定工作表的索引，默认是当前工作表（可选） | v4.0及以上 |   
addSheets | {sheets: [{index:1, name:‘ssss’, rc:100, cc:10}]} | 在指定索引处插入一个工作表  
sheets: 支持传一个或多个工作表的信息（必选）  
index: 用来添加工作表的索引（必选）  
name: 工作表的名称（可选）  
rc: 工作表的初始行数，默认是200（可选）  
cc: 工作表的初始列数，默认是20（可选） | v4.0及以上 |   
deleteSheets | {index: [2,3]} | 在指定索引处删除工作表  
index：要删除的工作表索引 | v4.0及以上 |   
setSheetsVisible | {index: [1,2], visible: false} | 设置工作表的可见性  
index: 要显示/隐藏的工作表索引  
显示时index可选 会按照顺序显示隐藏的一个sheet  
vidible: true（显示）false（隐藏） | v4.0及以上 |   
getRangeValues | {range:[{r:0, c:0, rc:1, cc:1}], si:0} | 设置工作表的可见性  
index: 要显示/隐藏的工作表索引  
显示时index可选 会按照顺序显示隐藏的一个sheet  
vidible: true（显示）false（隐藏） | v4.0及以上 |   
setComment（设置批注） | [{ options: [{ r: 0, c: 0, v: ‘这是一个批注’, remove: false }, { r: 1, c: 1, v: ‘’, remove: true }], si: 0 }] （注意参数是个数组） | 设置批注  
remove:true（清除批注）/false（设置批注时可以不传）  
v | v4.0及以上 |   
copyCellsTo（复制指定区域的样式到另一个区域） | {range: [{fromRow: 0, fromCol: 0, toRow: 1, toCol: 1, rc: 1, cc: 1}], si:0, type: 0,useSpreadApi: true, isUndo: true} | 复制指定区域的样式到另一个区域  
range: [{  
fromRow: 来源行  
toRow: 目标列  
formCol: 来源列  
toCol: 目标列,  
}]  
si: sheetIndex（单元表索引）  
type: 0：粘贴所有 | 1：粘贴值 | 2：粘贴样式  
useSpreadApi: 是否使用spread复制API（spread复制API的复制逻辑与手动复制粘贴的逻辑一样，可以实现公式更新）（v6.0.1）  
如果想复制整行，可以把对应的fromCol设置成-1 对应的复制整列可以把fromRow设置成-1  
（源和目标两片区域大小必须一样所以rc和cc指的既是from也是to的行数或列数）  
例如，将第一行的数据复制到第二行：sa.action.copyCellsTo（{range: [{fromRow: 0, fromCol: -1, toRow: 1, toCol: -1, rc: 1, cc: 20}], si:0, type: 0}, sa.t）  
setRowFilter（设置过滤） | [{ r: 4, c: 2, rc: 8, cc: 9, si: 0 }]（注意参数是个数组） | 设置过滤区域  
r:起始行的索引  
c:起始列的索引  
rc:要设置行的个数  
cc:要设置列的个数 | v4.0及以上 |   
setHyperLinkCell（设置超链接） | { callback:, invokemethod:, si:, range: [{r:1, c:1, rc: 10, cc:1, options:{text:‘超链接’,value:'[https://www.baidu.com](https://www.baidu.com',color:'red',visitedColor:'blue',toolTip)[’,color:‘red’,visitedColor:‘blue’,toolTip](https://www.baidu.com',color:'red',visitedColor:'blue',toolTip): ‘超链接’ }}] } | callback: 回调的请求方法名（必填）  
invokemethod: invokemethod（必填）  
si: 指定工作表的索引，默认是当前工作表（可选）  
range:区域范围（必填）  
options:超链接相关（可选） {  
text:单元格显示的内容  
value:点击超链接跳转的地址（不传时会向后端发送指令）SpreadJS的超链接支持许多协议，例如：http / https / ftp / file / mailto…。此外，还支持以“ sjs：//”开头的url，它引用工作表位置如sjs://Sheet1!A1:B2 也支持邮件的跳转  
color:超链接访问前的前景色  
visitedColor:超链接访问后的前景色  
toolTip:表示超链接的提示消息，当鼠标悬停在带有超链接的单元格上时显示。 | v4.0及以上 |   
setSheetName 设置表名 | [{oldName:‘sheet1’,si:0,newName:‘资产负债表’}] | oldName要设置的表的名称  
si要设置的表的索引  
newName表的新名称  
oldName与si都可不传 | v4.0及以上 |   
setZoom（设置缩放比例） | {zoom:0.25~4} | 默认1 | v4.0及以上 |   
setSheetDefaults（设置spread默认配置） | { si, setting: { rowHeight: value，… } } | si要设置的表的索引  
setting 配置  
rowHeight：行高  
colWidthl : 列宽 | v4.0及以上 |   
addToolbarItems（向工具栏里添加自定义按钮） | 自定义按钮为单个按钮  
[{  
key: ‘CheckFormula2’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
title: {  
zh_CN: ‘中文’,  
zh_TW: ‘繁体’,  
en_US: ‘english’  
},   
showTitle: true,  
callback: ‘sss’,  
invokeAction: ‘sssss’  
}]  
自定义按钮为下拉菜单  
[{  
key: ‘CheckFormula0’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击  
title: {  
zh_CN: ‘中文’,  
zh_TW: ‘繁体’,  
en_US: ‘english’  
},   
showTitle: true,  
item: [ // 下拉项  
{  
title:{  
zh_CN: ‘中文’,  
zh_TW: ‘繁体’,  
en_US: ‘english’  
} ,  
icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，  
callback: ‘sss’,  
invokeAction: ‘sssss’,  
key: ‘AutoFitColumn0123’, }  
]  
}] | key ：按钮标识  
icon： 字体图标  
title： tips或下拉项的内容，为兼容国际化，建议传对象形式的参数，包含三种语言显示的文字，也可直接传字符串  
callback 后端具体实现的类  
invokeAction 后端同于同一处理spread的接口  
showTitle: 是否显示标题文字  
item: 下拉选项 | v5.0及以上 |   
setActiveSheet（切换页签） | {name:’’,si:’’} | 激活/选中指定页签  
name：要切换页签的名字  
si：要切换页签的索引  
这两个传一个就行 | v4.0及以上 |   
excuteToolbarActions | {name: ‘SwitchView’, // 按钮标识itemName: ‘SwitchView_ValueView’ // 执行的按钮活动} | name: 按钮标识  
itemName: 执行的按钮活动 | v5.0及以上 |   
setToolbarGroups（自定义工具栏分组） | [{  
name: ‘默认1’,  
key: ‘default1’,  
toolbarItems: [{key: ‘Undo’},{key: ‘Undo’}， // 默认功能  
{  
key: ‘CheckFormula0’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
title: { zh_CN: ‘中文’, zh_TW: ‘繁体’, en_US: ‘english’ },   
showTitle: true,  
items: [ // 下拉项  
{  
title:{ zh_CN: ‘中文’,zh_TW: ‘繁体’,en_US: ‘english’ } ,  
icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，  
callback: ‘sss’,  
invokeAction: ‘sssss’,  
key: ‘AutoFitColumn0123’ }  
]  
},  
],  
}] | name: 需要显示的分组名称  
key: 分组唯一的key值，用于辨识分组，不可重复  
toolbarItems: 工具栏需要显示的按钮，数组顺序即按钮顺序。自定义功能参数与添加自定义按钮（addToolbarItems）相同。需要注意的是，自定义添加了一个新参数，isCustom，如果是自定义按钮这个参数必填true  
提示：默认情况下，默认分组一直存在，因此自定义工具栏分组时，如果不需要修改默认分组，则不需要传默认分组的参数，只需要传扩展分组，会自动将扩展分组添加到分组中。传递包含默认分组的参数则视为修改默认分组。 | v5.0及以上 |   
setToolbarCustomExpend（自定义公式扩展区域） | {itemConfig:{  
showTitle: true,  
key: ‘CheckFormula0’, // 唯一标识  
icon: ‘kdfont kdfont-shanchuxing’, // 字体图标  
title: { zh_CN: ‘测试’,zh_TW: ‘繁体’,en_US: ‘english’ },  
callback: ‘autoFitColumn’, // 回传给后端的，用于功能的具体实现  
invokeAction: ‘invokeAction’, // 回传给后端的，用于处理前端spread所有的请求,  
dropDownType: 0, // 可选项，为0时为左右不分离的下拉列表，为1时为左右分离的下拉列表，左边默认为第一个按钮点击  
items: [ // 下拉项  
{  
title:{zh_CN: ‘测试’,zh_TW: ‘繁体’,en_US: ‘english’},  
callback: ‘sss’,icon: ‘kdfont kdfont-guolvpaixu’, // 字体图标，  
invokeAction: ‘sssss’,  
key: ‘AutoFitColumn0123’, }  
]},   
inputConfig: {  
callback: ‘autoFitColumn’, // 回传给后端的，用于功能的具体实现  
invokeAction: ‘invokeAction’  
}} | itemConfig：自定义按钮配置，与自定义按钮相似inputConfig：自定义输入配置 | v5.0及以上 |   
setToolbarCustomInputValue（与setToolbarCustomExpend搭配使用，自定义公式扩展区域输入值） | string’ |  | v4.0及以上 |   
setCustomSheetMenu（自定义页签菜单右键顺序） | [{name: ‘hideSheet’}, {type: ‘separator’}, { name: ‘setBGC’ }, {name: ‘unhideSheet’}, {name: ‘insertRowsBehind’} ,{name: ‘insertSheet’}] | name: 菜单名称  
（insertSheet：插入，deleteSheet：删除，hideSheet：隐藏，unhideSheet：显示，setBGC：颜色选择器）  
type: 只有分割线才使用type（type: ‘separator’） | v4.0及以上 |   
getLockedCells（获取锁定单元格） | [{ callback: ‘invokeAction’, invokemethod: ‘invokemethod’, si: 0, compression: false }] | callback：回调函数  
invokeAction：后端同一处理spread的接口  
si：sheetIndex  
compression：是否压缩 | v4.0及以上 |   
hideRow（隐藏行）/unhideRow（显示行） | {data: [1, 2], si: 0, isUndo: true} |  | {data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似 | 在工作表的指定索引处添加一行/列  
data：指定行（行）的索引（必填）  
si: 指定工作表的索引，默认是当前工作表（可选）  
index: 要隐藏/显示的行的索引  
count: 要隐藏/显示多少行  
hideColumn（隐藏列）/unhideColumn（显示列） | {data: [1, 2], si: 0, isUndo: true} |  | {data: [{ index: 0, count: 2 }], si: 0, isUndo: true}// 参数与插入行列类似 | 在工作表的指定索引处添加一行/列  
data：指定行（列）的索引（必填）  
si: 指定工作表的索引，默认是当前工作表（可选）  
index: 要隐藏/显示的列的索引  
count: 要隐藏/显示多少列  
setSpreadParams | {data: {key: value}} | 添加全局spread属性（也可直接通过JSON设置，在spread新增一个自定义属性serverSpreadParams，将需要全局定义的属性作为对象赋值给serverSpreadParams）  
key：属性名  
value：属性值 | v5.0.027及以上 | 目前已有的属性：  
isEditSendRequest：鼠标进入编辑态与退出编辑态时是否向后端发送网络请求（5.0.0.27）  
allowMaxInsert9999：插入行列支持最大输入9999（v6.0.1）  
useNewInsert：使用新版插入（向上插入/向下插入）（v6.0.1）  
usePtFontSize：使用pt作为font-size的单位，默认是px（v6.0.7）  
useSheetPrintConfig：按工作表保存打印配置（v6.0.12）  
useExcelCellsFormat：使用与excel同步的单元格格式（v6.0.14）  
isUpdateStyleSendRequest：样式更新时发送网络请求（v7.0.0）  
isDeleteLockedSendRequest：delete快捷键清除锁定单元格数据时向后端发送网络请求（v7.0.4）  
setGCParams | {data: {key: value}} | 添加全局GC属性（与setSpreadParams不同的是，这个是GC层面的属性，设置之后不会随着json的设置而覆盖，建议本条指令在所有指令之前设置）  
key：属性名  
value：属性值 | v6.0及以上 | 目前已有的属性：calcPrecision：计算精度，默认为14，当出现精度问题时，可以调整为12。不建议调整其他值，可能会出现计算问题（v6.0.1）  
hideZero | {ishide: true, si: 0} | ishide：为0时是否隐藏  
si：sheetIndex | v4.0及以上 | 如果表格中已经存在0值，指令开启后需要重新计算才能将已有的0隐藏  
setGroupColNode（设置分组列） | [{r:number, c:number, groupNodeType:1 |  | 2 |   
setStatusBarVisible | {visible: false} | 设置spread底部缩放的显示（true）与隐藏（false）  
{visible: true | false} | v4.0及以上  
setValidator | {type: 0, option: {vt: 1, v1: ‘1’, vco: 0, ht: 0, hsc: ‘#ff0000’, es: 0, et: ‘不允许输入’}, range: [{row: 0, col: 0, rowCount: 5, colCount: 5}], callback: ‘invokeAction’, invokemethod: ‘test’, si: 0} | type: number （0:设置, 1:取消）  
option: object （配置项，详见备注）  
range: array （应用的单元格）  
callback: 回调函数  
invokemethod: 回调函数  
si: sheetindex | v4.0及以上 | VALIDATOR_TYPE = ‘vt’ // 验证器  
IS_FORMULA_LIST_VALIDATOR = ‘iflv’//  
VALIDATOR_COMPARISON_OPERATOR = ‘vco’// 比较运算符  
VALUE_1 = ‘v1’// 值一  
VALUE_2 = ‘v2’// 值二  
IS_INTEGER = ‘ii’ // 是否是整数  
SHOW_INPUT_MSG = ‘sim’// 显示输入消息  
INPUT_TITLE = ‘it’// 标题,  
INPUT_MESSAGE = ‘im’// 信息  
IGNORE_BLANK = ‘ib’ // 忽略空白  
SHOW_ERROR_MESSAGE = ‘sem’ // 显示错误信息  
ERROR_STYLE = ‘es’// 错误类型  
ERROR_TITLE = ‘et’// 错误标题  
ERROR_MSG = ‘em’ // 错误信息  
HIGHLIGHT_TYPE = ‘ht’ // 圆圈样式  
DOGEAR_POSITION = ‘dp’// 位置 与圆圈样式有关 类型dogear才会用到  
ICON_POSITION = ‘ip’// 位置 与圆圈样式有关 类型icon才会用到  
HIGHLIGHT_STYLE_COLOR = ‘hsc’// 颜色  
HIGHLIGHT_STYLE_IMAGE = ‘hsi’// 图标 与圆圈样式有关 类型icon才会用到  
batchExportExcelFiles | base64加密过的对象：{data:[{json:’’},…], fileName:‘fileName’} | json：spread的json文件，注意，这里是原生的json字符串，不是通过base64加密过的  
fileName：导出的文件名称 | v4.0及以上 |   
syncSpreads | { slaveSpreadKeys: [key2, key3], syncOptions: [scrollBar, rowHeight, colWidth, zoom], sync: true } | 多表联动，仅仅对各个当前活动的工作表进行联动处理  
slaveSpreadKeys：需要联动的spread的key  
syncOptions：需要联动的属性，目前只支持scrollBar（滚动）, rowHeight（行高）, colWidth（列宽）, zoom（缩放）  
sync：是否联动，如果已经联动的填写这个参数用于关闭联动 | v6.0.15及以上 |   
  
# 7 插件示例
    
    
    public class Spread extends Container{
    
        private SpreadPostDataInfo postData ;
        //删除行
        public void deleteRows(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().deleteRows(new SpreadEvent(this,postData));
        }
        //删除列
        public void deleteColumns(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().deleteColumns(new SpreadEvent(this,postData));
        }
        //插行
        public void addRows(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().addRows(new SpreadEvent(this,postData));
        }
        //插列
        public void addColumns(List<Integer> postDatas){
            packInvokeListParams(postDatas);
            this.getActionService().addColumns(new SpreadEvent(this,postData));
        }
        //切换单元格
        public void entryRowClick(int arg){
            getActionService().selectedSpread(new SpreadEvent(this,postData));        
        }
        //前端通用指令
        public void askExecute(LinkedHashMap<String,Object> postDatas){
            packInvokeParams(postDatas);
            getActionService().askExecute(new SpreadEvent(this,postData));
        }
    }
    
    

插行示例：  
前端控制台模拟后端指令  
![3.png](https://vip.kingdee.com/download/01004a82934bef0d4b92b3699ad2294ba4ce.png)

# 8 参考链接

  1. [SpreadJS 控件常见问题](https://vip.kingdee.com/link/s/ZLiPJ)



__上一篇：HTML控件

下一篇：进度条

 __

5.0 5人评分

内容反馈

*  __评论
收藏 7 

![复制链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_link_pc.png)复制链接

![复制标题链接](https://cdn-vip.kingdee.com/statics/webfront/icon/copy_l.png)复制标题+链接

分享

 __

扫一扫用手机继续查看

![vip.kingdee.com]()

 __手机播放

编辑举报
