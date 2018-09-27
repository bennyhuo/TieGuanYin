package com.bennyhuo.tieguanyin.runtime.core;

public class BuilderClassFinder {
    private static final String BUILDER_NAME_POSIX = "Builder";

    public static Class<?> findBuilderClass(Object object) throws ClassNotFoundException {
        Class<?> cls = object.getClass();
        String builderClassName = cls.getName().substring(cls.getName().lastIndexOf(".") + 1).replace("$", "_") + BUILDER_NAME_POSIX;
        return Class.forName(builderClassName);
    }
}
