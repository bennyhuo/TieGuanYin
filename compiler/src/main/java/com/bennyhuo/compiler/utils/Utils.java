package com.bennyhuo.compiler.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.kotlinpoet.TypeNames;
import com.sun.tools.javac.code.Type;

import java.util.List;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

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

    public static TypeName toWrapperType(Type type){
        switch (type.getKind()){
            case BOOLEAN:
                return ClassName.get(Boolean.class);
            case BYTE:
                return ClassName.get(Byte.class);
            case SHORT:
                return ClassName.get(Short.class);
            case INT:
                return ClassName.get(Integer.class);
            case LONG:
                return ClassName.get(Long.class);
            case CHAR:
                return ClassName.get(Character.class);
            case FLOAT:
                return ClassName.get(Float.class);
            case DOUBLE:
                return ClassName.get(Double.class);
            default:
                throw new IllegalArgumentException(type + " is not primitive type.");
        }
    }

    public static final com.squareup.kotlinpoet.ClassName STRING = new com.squareup.kotlinpoet.ClassName("kotlin", "String");
    public static final com.squareup.kotlinpoet.ClassName LONG_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "LongArray");
    public static final com.squareup.kotlinpoet.ClassName INT_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "IntArray");
    public static final com.squareup.kotlinpoet.ClassName SHORT_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "ShortArray");
    public static final com.squareup.kotlinpoet.ClassName BYTE_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "ByteArray");
    public static final com.squareup.kotlinpoet.ClassName CHAR_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "CharArray");
    public static final com.squareup.kotlinpoet.ClassName BOOLEAN_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "BooleanArray");
    public static final com.squareup.kotlinpoet.ClassName FLOAT_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "FloatArray");
    public static final com.squareup.kotlinpoet.ClassName DOUBLE_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "DoubleArray");

    public static com.squareup.kotlinpoet.TypeName toKotlinType(TypeMirror typeMirror){
        switch (typeMirror.getKind()){
            case BOOLEAN:
                return TypeNames.BOOLEAN;
            case BYTE:
                return TypeNames.BYTE;
            case SHORT:
                return TypeNames.SHORT;
            case INT:
                return TypeNames.INT;
            case LONG:
                return TypeNames.LONG;
            case CHAR:
                return TypeNames.CHAR;
            case FLOAT:
                return TypeNames.FLOAT;
            case DOUBLE:
                return TypeNames.DOUBLE;
            case DECLARED:
                if(typeMirror.toString().equals("java.lang.String")){
                    return STRING;
                }
            case ARRAY:
                ArrayType arrayType = (ArrayType) typeMirror;
                switch (arrayType.getComponentType().getKind()){
                    case BOOLEAN:
                        return BOOLEAN_ARRAY;
                    case BYTE:
                        return BYTE_ARRAY;
                    case SHORT:
                        return SHORT_ARRAY;
                    case INT:
                        return INT_ARRAY;
                    case LONG:
                        return LONG_ARRAY;
                    case CHAR:
                        return CHAR_ARRAY;
                    case FLOAT:
                        return FLOAT_ARRAY;
                    case DOUBLE:
                        return DOUBLE_ARRAY;
                }
        }
        return TypeNames.get(typeMirror);
    }
}
