package com.bennyhuo.tieguanyin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by benny on 1/29/18.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Optional {
    @Deprecated
    String stringValue() default "";
    @Deprecated
    char charValue() default '0';
    @Deprecated
    byte byteValue() default 0;
    @Deprecated
    short shortValue() default 0;
    @Deprecated
    int intValue() default 0;
    @Deprecated
    long longValue() default 0;
    @Deprecated
    float floatValue() default 0f;
    @Deprecated
    double doubleValue() default 0.0;
    @Deprecated
    boolean booleanValue() default false;
}
