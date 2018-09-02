package com.bennyhuo.tieguanyin.compiler.utils;


import com.squareup.kotlinpoet.AnnotationSpec;
import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.ParameterizedTypeName;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeNames;

import java.util.Collections;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by benny on 2/2/18.
 */

public class KotlinTypes {
    public static final ClassName BUNDLE = new ClassName("android.os", "Bundle");
    public static final ClassName INTENT = new ClassName("android.content", "Intent");
    public static final ClassName ACTIVITY = new ClassName("android.app", "Activity");
    public static final ClassName CONTEXT = new ClassName("android.content", "Context");
    public static final ClassName VIEW = new ClassName("android.view", "View");
    public static final ClassName VIEW_GROUP = new ClassName("android.view", "ViewGroup");
    public static final ClassName FRAGMENT = new ClassName("android.app", "Fragment");
    public static final ClassName SUPPORT_FRAGMENT = new ClassName("android.support.v4.app", "Fragment");
    public static final ClassName SUPPORT_ACTIVITY = new ClassName("android.support.v4.app", "FragmentActivity");
    public static final ClassName VIEW_COMPAT = new ClassName("android.support.v4.view", "ViewCompat");

    public static final ClassName RUNTIME_UTILS = new ClassName("com.bennyhuo.tieguanyin.runtime.utils", "BundleUtils");
    public static final ClassName VIEW_UTILS = new ClassName("com.bennyhuo.tieguanyin.runtime.utils", "ViewUtils");

    public static final ClassName ON_ACTIVITY_RESULT_LISTENER = new ClassName("com.bennyhuo.tieguanyin.runtime.core", "OnActivityResultListener");
    public static final ClassName ACTIVITY_BUILDER = new ClassName("com.bennyhuo.tieguanyin.runtime.core", "ActivityBuilder");
    public static final ClassName FRAGMENT_BUILDER = new ClassName("com.bennyhuo.tieguanyin.runtime.core", "FragmentBuilder");
    public static final ClassName ON_ACTIVITY_CREATE_LISTENER = new ClassName("com.bennyhuo.tieguanyin.runtime.core", "OnActivityCreateListener");
    public static final ClassName ON_FRAGMENT_CREATE_LISTENER = new ClassName("com.bennyhuo.tieguanyin.runtime.core", "OnFragmentCreateListener");

    public static final ClassName STRING = new com.squareup.kotlinpoet.ClassName("kotlin", "String");
    public static final ParameterizedTypeName STRING_ARRAY = new ParameterizedTypeName(null, new com.squareup.kotlinpoet.ClassName("kotlin", "Array"), Collections.<TypeName>singletonList(STRING), false, Collections.<AnnotationSpec>emptyList());
    public static final ClassName LONG_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "LongArray");
    public static final ClassName INT_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "IntArray");
    public static final ClassName SHORT_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "ShortArray");
    public static final ClassName BYTE_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "ByteArray");
    public static final ClassName CHAR_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "CharArray");
    public static final ClassName BOOLEAN_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "BooleanArray");
    public static final ClassName FLOAT_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "FloatArray");
    public static final ClassName DOUBLE_ARRAY = new com.squareup.kotlinpoet.ClassName("kotlin", "DoubleArray");
    public static final ClassName ARRAY_LIST = new ClassName("java.util", "ArrayList");
    public static final ClassName HASH_MAP = new ClassName("java.util", "HashMap");
    public static final ClassName SUPPORT_PAIR = new ClassName("android.support.v4.util", "Pair");
    public static final ClassName ACTIVITY_COMPAT = new ClassName("android.support.v4.app", "ActivityCompat");

    public static TypeName toKotlinType(TypeMirror typeMirror){
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
                    return KotlinTypes.STRING;
                }
                break;
            case ARRAY:
                ArrayType arrayType = (ArrayType) typeMirror;
                switch (arrayType.getComponentType().getKind()){
                    case BOOLEAN:
                        return KotlinTypes.BOOLEAN_ARRAY;
                    case BYTE:
                        return KotlinTypes.BYTE_ARRAY;
                    case SHORT:
                        return KotlinTypes.SHORT_ARRAY;
                    case INT:
                        return KotlinTypes.INT_ARRAY;
                    case LONG:
                        return KotlinTypes.LONG_ARRAY;
                    case CHAR:
                        return KotlinTypes.CHAR_ARRAY;
                    case FLOAT:
                        return KotlinTypes.FLOAT_ARRAY;
                    case DOUBLE:
                        return KotlinTypes.DOUBLE_ARRAY;
                    case DECLARED:
                        if(typeMirror.toString().equals("java.lang.String[]")){
                            return KotlinTypes.STRING_ARRAY;
                        }
                }
        }
        return TypeNames.get(typeMirror);
    }

}
