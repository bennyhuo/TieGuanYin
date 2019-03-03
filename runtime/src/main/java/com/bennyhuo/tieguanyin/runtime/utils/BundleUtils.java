package com.bennyhuo.tieguanyin.runtime.utils;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;
import android.util.SizeF;

import java.io.Serializable;

/**
 * Created by benny on 1/30/18.
 */

public class BundleUtils {
    public static <T> T get(Bundle bundle, String key){
        return (T) bundle.get(key);
    }

    public static <T> T get(Bundle bundle, String key, Object defaultValue){
        Object obj = bundle.get(key);
        if(obj == null){
            obj = defaultValue;
        }
        return (T) obj;
    }

    public static <T> void put(Bundle bundle, String key, T value) {
        if( value.getClass() == Bundle.class){
            bundle.putBundle(key, (Bundle) value);
        } else if (value.getClass() == String.class) {
            bundle.putString(key, (String) value);
        } else if (value.getClass() == String[].class) {
            bundle.putStringArray(key, (String[]) value);
        } else if (value.getClass() == byte.class) {
            bundle.putByte(key, (Byte) value);
        } else if (value.getClass() == byte[].class) {
            bundle.putByteArray(key, (byte[]) value);
        } else if (value.getClass() == Byte.class) {
            bundle.putByte(key, (Byte) value);
        } else if (value.getClass() == Byte[].class) {
            bundle.putByteArray(key, (byte[]) value);
        } else if (value.getClass() == int.class) {
            bundle.putInt(key, (Integer) value);
        } else if (value.getClass() == int[].class) {
            bundle.putIntArray(key, (int[]) value);
        } else if (value.getClass() == Integer.class) {
            bundle.putInt(key, (Integer) value);
        } else if (value.getClass() == Integer[].class) {
            bundle.putIntArray(key, (int[]) value);
        } else if (value.getClass() == short.class) {
            bundle.putShort(key, (Short) value);
        } else if (value.getClass() == short[].class) {
            bundle.putShortArray(key, (short[]) value);
        } else if (value.getClass() == Short.class) {
            bundle.putShort(key, (Short) value);
        } else if (value.getClass() == Short[].class) {
            bundle.putShortArray(key, (short[]) value);
        } else if (value.getClass() == float.class) {
            bundle.putFloat(key, (Float) value);
        } else if (value.getClass() == float[].class) {
            bundle.putFloatArray(key, (float[]) value);
        } else if (value.getClass() == Float.class) {
            bundle.putFloat(key, (Float) value);
        } else if (value.getClass() == Float[].class) {
            bundle.putFloatArray(key, (float[]) value);
        } else if (value.getClass() == double.class) {
            bundle.putDouble(key, (Double) value);
        } else if (value.getClass() == double[].class) {
            bundle.putDoubleArray(key, (double[]) value);
        } else if (value.getClass() == Double.class) {
            bundle.putDouble(key, (Double) value);
        } else if (value.getClass() == Double[].class) {
            bundle.putDoubleArray(key, (double[]) value);
        } else if (value.getClass() == char.class) {
            bundle.putChar(key, (Character) value);
        } else if (value.getClass() == char[].class) {
            bundle.putCharArray(key, (char[]) value);
        } else if (value.getClass() == Character.class) {
            bundle.putChar(key, (Character) value);
        } else if (value.getClass() == CharSequence.class) {
            bundle.putCharSequence(key, (CharSequence) value);
        } else if (value.getClass() == CharSequence[].class) {
            bundle.putCharSequenceArray(key, (CharSequence[]) value);
        } else if (value.getClass() == Parcelable.class) {
            bundle.putParcelable(key, (Parcelable) value);
        } else if (value.getClass() == Parcelable[].class) {
            bundle.putParcelableArray(key, (Parcelable[]) value);
        } else if (value.getClass() == Serializable.class) {
            bundle.putSerializable(key, (Serializable) value);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (value.getClass() == Size.class) {
                    bundle.putSize(key, (Size) value);
                } else if (value.getClass() == SizeF.class) {
                    bundle.putSizeF(key, (SizeF) value);
                } else {
                    throw new UnsupportedOperationException("Unsupported type: " + value.getClass());
                }
            } else {
                throw new UnsupportedOperationException("Unsupported type: " + value.getClass());
            }
        }
    }
}
