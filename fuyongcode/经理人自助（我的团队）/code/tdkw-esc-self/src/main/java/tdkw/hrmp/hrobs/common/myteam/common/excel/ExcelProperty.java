package tdkw.hrmp.hrobs.common.myteam.common.excel;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelProperty {

    /**
     * @return 属性列索引位置，从0开始
     */
    int index() default -1;

    /**
     * @return 列标题，用于导出时生成列标题及导入时校验列标题
     */
    String title() default "";

    /**
     * @return 默认的最新列宽度
     */
    int minWidth() default 10;
}
