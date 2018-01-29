package com.bennyhuo.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Created by benny on 1/29/18.
 */
@Target(AnnotationTarget.CLASS)
@Retention(BINARY)
annotation class GenerateBuilder

@Target(PROPERTY)
@Retention(BINARY)
annotation class Required(val name: String)