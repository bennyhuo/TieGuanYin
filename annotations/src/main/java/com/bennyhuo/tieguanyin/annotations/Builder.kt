package com.bennyhuo.tieguanyin.annotations

/**
 * Created by benny on 1/29/18.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Builder(
    val mode: GenerateMode = GenerateMode.Auto,
    val sharedElements: Array<SharedElement> = [],
    val sharedElementsByNames: Array<SharedElementByNames> = [],
    val sharedElementsWithName: Array<SharedElementWithName> = [],
    /**
     * For Activities Only
     */
    val resultTypes: Array<ResultEntity> = [],
    /**
     * For Activities Only
     */
    val pendingTransition: PendingTransition = PendingTransition(),
    /**
     * For Activities Only
     */
    val pendingTransitionOnFinish: PendingTransition = PendingTransition(),
    /**
     * For Activities Only
     */
    val categories: Array<String> = [],
    /**
     * For Activities Only
     */
    val flags: IntArray = []
)