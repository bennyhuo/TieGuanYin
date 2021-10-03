package com.bennyhuo.tieguanyin.annotations

/**
 * Created by benny on 2/3/18.
 */
enum class GenerateMode {
    /**
     * Generate Java utility method for Java developers only.
     */
    JavaOnly,

    /**
     * Generate Kotlin utility functions for Kotlin developers only.
     */
    KotlinOnly, Both,

    /**
     * Generate methods according to the source file.
     *
     * When source is Java file, generate java method, while source is Kotlin file, generate both.
     */
    Auto
}