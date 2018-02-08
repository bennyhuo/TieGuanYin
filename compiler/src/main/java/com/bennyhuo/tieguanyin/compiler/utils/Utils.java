package com.bennyhuo.tieguanyin.compiler.utils;

import java.util.List;

/**
 * Created by benny on 1/30/18.
 */

public class Utils {

    public static String joinString(List<String> strings, String sep){
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        for (String string : strings) {
            if (isFirst) {
                isFirst = false;
            } else {
                stringBuilder.append(sep);
            }
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

    public static String capitalize(String original){
        if(original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}
