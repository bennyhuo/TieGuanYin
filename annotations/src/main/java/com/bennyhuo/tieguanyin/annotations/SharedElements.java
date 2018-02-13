package com.bennyhuo.tieguanyin.annotations;

/**
 * Created by benny on 2/13/18.
 */

public @interface SharedElements {
    SharedElement[] sharedElement() default {};
    SharedElementByNames[] sharedElementByNames() default {};
    SharedElementWithName[] sharedElementWithName() default {};
}
