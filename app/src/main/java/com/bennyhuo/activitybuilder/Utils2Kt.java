package com.bennyhuo.activitybuilder;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

/**
 * Created by benny on 2/1/18.
 */

public class Utils2Kt {
    public static final void test2(@NotNull HelloActivity $receiver) {
        Intrinsics.checkParameterIsNotNull($receiver, "$receiver");
    }
}
