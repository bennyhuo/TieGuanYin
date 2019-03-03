package com.bennyhuo.tieguanyin.compiler.basic.types;

import com.bennyhuo.aptutils.logger.Logger;
import com.sun.tools.javac.code.Type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SupportedTypes {
    private static final Set<String> internalTypeSet = new HashSet<>();

    static {
        internalTypeSet.add(byte.class.getCanonicalName());
        internalTypeSet.add(byte[].class.getCanonicalName());
        internalTypeSet.add(Byte.class.getCanonicalName());
        internalTypeSet.add(Byte[].class.getCanonicalName());

        internalTypeSet.add(int.class.getCanonicalName());
        internalTypeSet.add(int[].class.getCanonicalName());
        internalTypeSet.add(Integer.class.getCanonicalName());
        internalTypeSet.add(Integer[].class.getCanonicalName());

        internalTypeSet.add(short.class.getCanonicalName());
        internalTypeSet.add(short[].class.getCanonicalName());
        internalTypeSet.add(Short.class.getCanonicalName());
        internalTypeSet.add(Short[].class.getCanonicalName());

        internalTypeSet.add(float.class.getCanonicalName());
        internalTypeSet.add(float[].class.getCanonicalName());
        internalTypeSet.add(Float.class.getCanonicalName());
        internalTypeSet.add(Float[].class.getCanonicalName());

        internalTypeSet.add(double.class.getCanonicalName());
        internalTypeSet.add(double[].class.getCanonicalName());
        internalTypeSet.add(Double.class.getCanonicalName());
        internalTypeSet.add(Double[].class.getCanonicalName());

        internalTypeSet.add(char.class.getCanonicalName());
        internalTypeSet.add(char[].class.getCanonicalName());
        internalTypeSet.add(Character.class.getCanonicalName());
        internalTypeSet.add(CharSequence.class.getCanonicalName());
        internalTypeSet.add(CharSequence[].class.getCanonicalName());

        internalTypeSet.add(String.class.getCanonicalName());
        internalTypeSet.add(String[].class.getCanonicalName());

        internalTypeSet.add("android.os.Parcelable");
        internalTypeSet.add("android.os.Parcelable[]");

        internalTypeSet.add(Serializable.class.getCanonicalName());

        internalTypeSet.add("android.util.Size");
        internalTypeSet.add("android.util.SizeF");
    }

    public static boolean isInternalType(Type type) {
        Logger.INSTANCE.warn("isInternalType, type: " + type + ", class: " + type.getClass() + ", name: " + type.asElement().name);
        if(type.isPrimitive()) return true;
        if(type instanceof Type.ClassType){
            Logger.INSTANCE.warn("-- ClassType, " + type.asElement().getQualifiedName().toString());
            return internalTypeSet.contains(type.asElement().getQualifiedName().toString());
        }
        if(type instanceof Type.ArrayType){
            Logger.INSTANCE.warn("-- ArrayType, " + type.asElement().getQualifiedName().toString());
            return internalTypeSet.contains(type.toString());
        }
        return false;
    }
}
