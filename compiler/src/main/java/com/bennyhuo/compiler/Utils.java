package com.bennyhuo.compiler;

import com.squareup.javapoet.MethodSpec;

import java.util.List;

/**
 * Created by benny on 1/30/18.
 */

public class Utils {

    public static MethodSpec.Builder copyMethodWithNewName(String name, MethodSpec methodSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(name);
        builder.addJavadoc(methodSpec.javadoc);
        builder.addAnnotations(methodSpec.annotations);
        builder.addModifiers(methodSpec.modifiers);
        builder.addTypeVariables(methodSpec.typeVariables);
        builder.returns(methodSpec.returnType);
        builder.addParameters(methodSpec.parameters);
        builder.addExceptions(methodSpec.exceptions);
        builder.addCode(methodSpec.code);
        builder.varargs(methodSpec.varargs);
        if(methodSpec.defaultValue != null)
            builder.defaultValue(methodSpec.defaultValue);
        return builder;
    }
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
