package org.nott.annotations;

import java.lang.annotation.*;

/**
 * 标识退款接口
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Refund {

    String value() default "";

}
