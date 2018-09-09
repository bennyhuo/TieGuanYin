package com.bennyhuo.tieguanyin.compiler.utils

/**
 * Created by benny on 1/30/18.
 */
fun String.camelToUnderline(): String {
    return fold(StringBuilder()) { acc, c ->
        if (c.isUpperCase()) {
            acc.append("_").append(c.toLowerCase())
        } else acc.append(c)
    }.toString()
}

fun String.underlineToCamel(): String {
    var upperNext = false
    return fold(StringBuilder()) { acc, c ->
        if (c == '_') upperNext = true
        else acc.append(if (upperNext) c.toUpperCase() else c)
        acc
    }.toString()
}
