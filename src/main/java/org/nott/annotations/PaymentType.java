package org.nott.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 标识类型注解
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentType {

    @AliasFor("type")
    String value() default "";

    @AliasFor("value")
    String type() default "";
}
