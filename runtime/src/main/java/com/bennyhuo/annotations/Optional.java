package com.bennyhuo.annotations;

import com.bennyhuo.factory.ObjectCreator;
import com.bennyhuo.factory.impl.DefaultCreator;

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
    String value() default "";
     Class<? extends ObjectCreator> creator() default DefaultCreator.class;
}
