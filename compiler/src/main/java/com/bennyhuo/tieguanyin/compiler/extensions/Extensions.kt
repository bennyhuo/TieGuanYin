package com.bennyhuo.tieguanyin.compiler.extensions

import com.bennyhuo.tieguanyin.annotations.PendingTransition

fun PendingTransition.isDefault(): Boolean {
    return enterAnim == PendingTransition.DEFAULT && exitAnim == PendingTransition.DEFAULT
}
