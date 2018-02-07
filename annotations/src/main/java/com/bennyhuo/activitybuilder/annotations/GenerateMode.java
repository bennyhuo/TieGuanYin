package com.bennyhuo.activitybuilder.annotations;

/**
 * Created by benny on 2/3/18.
 */

public enum GenerateMode {
    /**
     * Generate Java utility method for Java developers only.
     */
    JavaOnly,

    /**
     * Generate Kotlin utility functions for Kotlin developers only.
     */
    KotlinOnly,

    Both,

    /**
     * Generate methods according to the source file.
     */
    Auto
}
