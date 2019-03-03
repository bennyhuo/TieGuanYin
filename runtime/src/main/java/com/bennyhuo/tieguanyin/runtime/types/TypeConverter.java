package com.bennyhuo.tieguanyin.runtime.types;


/**
 *
 * @param <T>
 * @param <R> must be bundle supported types.
 */
public interface TypeConverter<T, R> {

    R convertFrom(T t);

    T convertTo(R r);

}
