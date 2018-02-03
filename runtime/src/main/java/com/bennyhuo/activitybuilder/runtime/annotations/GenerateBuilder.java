package com.bennyhuo.activitybuilder.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by benny on 1/29/18.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface GenerateBuilder {
    GenerateMode mode() default GenerateMode.Both;
    boolean forResult() default false;
    ResultEntity[] resultTypes() default {};
}
