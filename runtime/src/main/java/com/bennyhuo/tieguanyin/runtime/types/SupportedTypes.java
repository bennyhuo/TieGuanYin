package com.bennyhuo.tieguanyin.runtime.types;

import android.os.Build;
import android.os.Parcelable;
import android.util.Size;
import android.util.SizeF;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SupportedTypes {
    private static final Set<Class> internalTypeSet = new HashSet<>();

    static {
        internalTypeSet.add(byte.class);
        internalTypeSet.add(byte[].class);
        internalTypeSet.add(Byte.class);
        internalTypeSet.add(Byte[].class);

        internalTypeSet.add(int.class);
        internalTypeSet.add(int[].class);
        internalTypeSet.add(Integer.class);
        internalTypeSet.add(Integer[].class);

        internalTypeSet.add(short.class);
        internalTypeSet.add(short[].class);
        internalTypeSet.add(Short.class);
        internalTypeSet.add(Short[].class);

        internalTypeSet.add(float.class);
        internalTypeSet.add(float[].class);
        internalTypeSet.add(Float.class);
        internalTypeSet.add(Float[].class);

        internalTypeSet.add(double.class);
        internalTypeSet.add(double[].class);
        internalTypeSet.add(Double.class);
        internalTypeSet.add(Double[].class);

        internalTypeSet.add(char.class);
        internalTypeSet.add(char[].class);
        internalTypeSet.add(Character.class);
        internalTypeSet.add(CharSequence.class);
        internalTypeSet.add(CharSequence[].class);

        internalTypeSet.add(String.class);
        internalTypeSet.add(String[].class);

        internalTypeSet.add(Parcelable.class);
        internalTypeSet.add(Parcelable[].class);

        internalTypeSet.add(Serializable.class);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            internalTypeSet.add(Size.class);
            internalTypeSet.add(SizeF.class);
        }
    }

    public static boolean isInternalType(Class cls){
        return internalTypeSet.contains(cls);
    }
}
