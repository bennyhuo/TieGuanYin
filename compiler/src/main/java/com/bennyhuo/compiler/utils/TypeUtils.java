package com.bennyhuo.compiler.utils;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Created by benny on 2/3/18.
 */
public class TypeUtils {
    public static Types types;

    /**
     * Uses both {@link Types#erasure} and string manipulation to strip any generic types.
     */
    public static String doubleErasure(TypeMirror elementType) {
        String name = types.erasure(elementType).toString();
        int typeParamStart = name.indexOf('<');
        if (typeParamStart != -1) {
            name = name.substring(0, typeParamStart);
        }
        return name;
    }

    public static String simpleName(TypeMirror elementType) {
        String name = doubleErasure(elementType);
        return name.substring(name.lastIndexOf(".") + 1);
    }

    public static String getPackageName(TypeElement type){
        if(type.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)){
            return type.getEnclosingElement().asType().toString();
        }else{
            throw new IllegalArgumentException(type.getEnclosingElement().toString());
        }
    }
}
