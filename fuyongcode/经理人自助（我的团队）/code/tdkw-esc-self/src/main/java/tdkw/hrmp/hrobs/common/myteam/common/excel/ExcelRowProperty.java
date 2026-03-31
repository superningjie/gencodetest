package tdkw.hrmp.hrobs.common.myteam.common.excel;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelRowProperty {
    /**
     * @return 默认标题行高
     */
    short titleHeight() default (short) (16.5 * 20);

    /**
     * @return 默认数据行高
     */
    short height() default (short) (16.5 * 20);

    /**
     * @return 列宽自适应
     */
    boolean autoFitCol() default true;
}
