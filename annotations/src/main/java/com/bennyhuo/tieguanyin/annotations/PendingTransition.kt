package com.bennyhuo.tieguanyin.annotations

/**
 * Created by benny on 18/04/2018.
 */
annotation class PendingTransition(val enterAnim: Int = DEFAULT, val exitAnim: Int = DEFAULT) {
    companion object {
        const val DEFAULT = -1
    }
}