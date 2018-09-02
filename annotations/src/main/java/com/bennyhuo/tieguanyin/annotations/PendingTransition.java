package com.bennyhuo.tieguanyin.annotations;

/**
 * Created by benny on 18/04/2018.
 */
public @interface PendingTransition {
    int DEFAULT = -1;

    int enterAnim() default DEFAULT;
    int exitAnim() default DEFAULT;

}
