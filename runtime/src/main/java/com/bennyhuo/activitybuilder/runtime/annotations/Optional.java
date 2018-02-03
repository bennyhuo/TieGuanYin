package com.bennyhuo.activitybuilder.runtime.annotations;

import com.bennyhuo.activitybuilder.runtime.factory.ObjectCreator;
import com.bennyhuo.activitybuilder.runtime.factory.impl.DefaultCreator;

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
    String stringValue() default "";

    int intValue() default 0;

    float floatValue() default 0f;

    boolean booleanValue() default false;

    Class<? extends ObjectCreator> creator() default DefaultCreator.class;
}
