package com.bennyhuo.tieguanyin.runtime.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by benny on 1/29/18.
 */

public class IntentUtils {

    public static void fillIntent(Intent intent, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value == null) {
                intent.putExtra(key, (Serializable) null);
            } else if (value instanceof Integer) {
                intent.putExtra(key, (int) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (long) value);
            } else if (value instanceof String) {
                intent.putExtra(key, (String) value);
            } else if (value instanceof CharSequence) {
                intent.putExtra(key, (CharSequence) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (float) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (double) value);
            } else if (value instanceof Character) {
                intent.putExtra(key, (char) value);
            } else if (value instanceof Short) {
                intent.putExtra(key, (short) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (boolean) value);
            } else if (value instanceof Bundle) {
                intent.putExtra(key, (Bundle) value);
            } else if (value instanceof Parcelable) {
                intent.putExtra(key, (Parcelable) value);
            } else if (value instanceof int[]) {
                intent.putExtra(key, (int[]) value);
            } else if (value instanceof long[]) {
                intent.putExtra(key, (long[]) value);
            } else if (value instanceof float[]) {
                intent.putExtra(key, (float[]) value);
            } else if (value instanceof double[]) {
                intent.putExtra(key, (double[]) value);
            } else if (value instanceof char[]) {
                intent.putExtra(key, (char[]) value);
            } else if (value instanceof short[]) {
                intent.putExtra(key, (short[]) value);
            } else if (value instanceof boolean[]) {
                intent.putExtra(key, (boolean[]) value);
            } else if (value instanceof String[]) {
                intent.putExtra(key, (String[]) value);
            } else if (value instanceof CharSequence[]) {
                intent.putExtra(key, (CharSequence[]) value);
            } else if (value instanceof Parcelable[]) {
                intent.putExtra(key, (Parcelable[]) value);
            } else if (value instanceof Serializable) {
                intent.putExtra(key, (Serializable) value);
            } else {
                throw new IllegalArgumentException("Unsupport type of args: " + key + " with value: " + value);
            }
        }
    }

}
