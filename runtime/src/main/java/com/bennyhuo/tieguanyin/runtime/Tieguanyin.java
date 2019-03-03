package com.bennyhuo.tieguanyin.runtime;

import android.content.Context;

import com.bennyhuo.tieguanyin.runtime.core.ActivityBuilder;
import com.bennyhuo.tieguanyin.runtime.types.DefaultTypeConverter;
import com.bennyhuo.tieguanyin.runtime.types.DefaultTypeCreator;
import com.bennyhuo.tieguanyin.runtime.types.InternalTypeConverter;
import com.bennyhuo.tieguanyin.runtime.types.SupportedTypes;
import com.bennyhuo.tieguanyin.runtime.types.TypeConverter;
import com.bennyhuo.tieguanyin.runtime.types.TypeCreator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tieguanyin {
    public static void init(Context context){
        ActivityBuilder.INSTANCE.init(context);
    }

    public static boolean DEBUG = false;

    private static Map<Class, TypeConverter> converters = new ConcurrentHashMap<>();

    public static void registerConverter(Class cls, TypeConverter converter){
        converters.put(cls, converter);
    }

    public static <T, R> TypeConverter<T, R> findProperConverter(Class<T> cls){
        if(SupportedTypes.isInternalType(cls)){
            return InternalTypeConverter.INSTANCE;
        }

        TypeConverter<T, R> converter = (TypeConverter<T, R>) converters.get(cls);
        if(converter == null){
            converter  = new DefaultTypeConverter(cls);
            converters.put(cls, converter);
        }
        return converter;
    }

    private static Map<Class<?>, TypeCreator<?>> creators = new ConcurrentHashMap<>();

    public static void registerCreator(Class cls, TypeCreator converter){
        creators.put(cls, converter);
    }

    public static <T> TypeCreator<T> findProperCreator(Class<T> cls){
        TypeCreator<T> creator = (TypeCreator<T>) creators.get(cls);
        if(creator == null){
            return DefaultTypeCreator.INSTANCE;
        }
        return creator;
    }
}
