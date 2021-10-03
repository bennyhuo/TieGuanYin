package com.bennyhuo.tieguanyin.annotations

/**
 * Created by benny on 1/29/18.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class Optional(
    val stringValue: String = "",
    val charValue: Char = '0',
    val byteValue: Byte = 0,
    val shortValue: Short = 0,
    val intValue: Int = 0,
    val longValue: Long = 0,
    val floatValue: Float = 0f,
    val doubleValue: Double = 0.0,
    val booleanValue: Boolean = false
)