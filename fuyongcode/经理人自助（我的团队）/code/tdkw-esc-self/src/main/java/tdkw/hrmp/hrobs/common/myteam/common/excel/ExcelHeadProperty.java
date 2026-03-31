package tdkw.hrmp.hrobs.common.myteam.common.excel;

import java.lang.annotation.*;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/7/18-17:37
 * @description Excel头部定义
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelHeadProperty {

    /**
     * @return 起始行
     */
    int firstRow() default 0;

    /**
     * @return 截止行
     */
    int lastRow() default 0;

    /**
     * @return 起始列
     */
    int firstCol() default 0;

    /**
     * @return 截止列
     */
    int lastCol() default 0;

    /**
     * @return 标题行高
     */
    short height() default (short) (24 * 20);

    /**
     * @return 标题文本
     */
    String text() default "";

}
