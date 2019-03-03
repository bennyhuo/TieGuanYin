package com.bennyhuo.tieguanyin.runtime.types;

public class InternalTypeConverter<T> implements TypeConverter<T, T> {

    public final static InternalTypeConverter INSTANCE = new InternalTypeConverter();

    @Override
    public T convertFrom(T t) {
        return t;
    }

    @Override
    public T convertTo(T t) {
        return t;
    }
}
