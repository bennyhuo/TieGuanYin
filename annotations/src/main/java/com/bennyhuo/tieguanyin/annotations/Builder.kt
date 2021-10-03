package com.bennyhuo.tieguanyin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by benny on 1/29/18.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Builder {
    GenerateMode mode() default GenerateMode.Auto;
    SharedElement[] sharedElements() default {};
    SharedElementByNames[] sharedElementsByNames() default {};
    SharedElementWithName[] sharedElementsWithName() default {};

    /**
     * For Activities Only
     */
    ResultEntity[] resultTypes() default {};

    /**
     * For Activities Only
     */
    PendingTransition pendingTransition() default @PendingTransition;

    /**
     * For Activities Only
     */
    PendingTransition pendingTransitionOnFinish() default @PendingTransition;

    /**
     * For Activities Only
     */
    String[] categories() default {};

    /**
     * For Activities Only
     */
    int[] flags() default {};
}
