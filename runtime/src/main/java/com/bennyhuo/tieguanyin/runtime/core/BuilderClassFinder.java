package com.bennyhuo.tieguanyin.runtime.core;

public class BuilderClassFinder {
    private static final String BUILDER_NAME_POSIX = "Builder";

    public static Class<?> findBuilderClass(Object object) throws ClassNotFoundException {
        Class<?> cls = object.getClass();
        StringBuilder stringBuilder = new StringBuilder(cls.getSimpleName());

        Class<?> enclosingClass = cls.getEnclosingClass();
        while (enclosingClass != null) {
            stringBuilder.insert(0, '_')
                    .insert(0, enclosingClass.getSimpleName());
            enclosingClass = enclosingClass.getEnclosingClass();
        }

        stringBuilder.insert(0, '.').insert(0, cls.getPackage().getName()).append(BUILDER_NAME_POSIX);
        return Class.forName(stringBuilder.toString());
    }
}
