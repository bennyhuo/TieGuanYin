package com.bennyhuo.tieguanyin.runtime.types;

import android.os.Bundle;

import com.bennyhuo.tieguanyin.annotations.Serialize;
import com.bennyhuo.tieguanyin.runtime.Tieguanyin;
import com.bennyhuo.tieguanyin.runtime.utils.BundleUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DefaultTypeConverter<T> implements TypeConverter<T, Bundle> {

    public static class FieldInfo {
        public final Field field;
        public final String name;
        public final boolean isInternal;

        public FieldInfo(Field field, String name) {
            this.field = field;
            this.name = name;
            isInternal = SupportedTypes.isInternalType(field.getType());
            this.field.setAccessible(true);
        }
    }

    private Map<String, FieldInfo> fields;
    private TypeCreator<T> creator;

    private void setUpClass(Class cls) {
        if (this.fields == null) {
            fields = new HashMap<>();
            Class thisClass = cls;
            while (thisClass != null && thisClass != Object.class) {
                for (Field field : thisClass.getDeclaredFields()) {
                    Serialize serialize = field.getAnnotation(Serialize.class);
                    if(serialize == null) continue;
                    String name = serialize.name().length() == 0 ? field.getName() : serialize.name();
                    FieldInfo duplicatedFieldInfo = fields.put(name, new FieldInfo(field, name));
                    if (duplicatedFieldInfo != null) {
                        throw new IllegalArgumentException("Name of field: " + field + " is duplicated with " + duplicatedFieldInfo.field);
                    }
                }
                thisClass = thisClass.getSuperclass();
            }
        }
        if(creator == null){
            creator = Tieguanyin.findProperCreator(cls);
        }
    }

    @Override
    public Bundle convertFrom(T o) {
        if(o == null) return null;
        setUpClass(o.getClass());
        Bundle result = new Bundle();
        try {
            for (Map.Entry<String, FieldInfo> entry : fields.entrySet()) {
                if(entry.getValue().isInternal){
                    BundleUtils.put(result, entry.getKey(), entry.getValue().field.get(o));
                } else {
                    BundleUtils.put(result, entry.getKey(), Tieguanyin.findProperConverter((Class<Object>) entry.getValue().field.getType()).convertFrom(entry.getValue().field.get(o)));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public T convertTo(Bundle bundle) {
        if(bundle == null) return null;
        T object = creator.create();
        try {
            for (Map.Entry<String, FieldInfo> entry : fields.entrySet()) {
                if(entry.getValue().isInternal){
                    entry.getValue().field.set(object, BundleUtils.get(bundle, entry.getKey()));
                } else {
                    entry.getValue().field.set(object, Tieguanyin.findProperConverter((Class<Object>) entry.getValue().field.getType()).convertTo(BundleUtils.get(bundle, entry.getKey())));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }
}
