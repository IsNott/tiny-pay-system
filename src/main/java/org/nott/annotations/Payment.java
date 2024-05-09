package org.nott.annotations;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 标识实现类注解
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Payment {

    @AliasFor("code")
    String value() default "";

    @AliasFor("value")
    String code() default "";

}
