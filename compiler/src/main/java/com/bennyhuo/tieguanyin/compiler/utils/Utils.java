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

    public static String camelToUnderline(String param){
        if (param==null||"".equals(param.trim())){
            return "";
        }
        int len=param.length();
        StringBuilder sb=new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c=param.charAt(i);
            if (Character.isUpperCase(c)){
                sb.append('_');
                sb.append(Character.toUpperCase(c));
            }else{
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    public static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == '_') {
                if (++i < len) {
                    sb.append(param.charAt(i));
                }
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

//    public static boolean isKotlinClass(){
//
//    }
}
