package com.bennyhuo.compiler;

import com.bennyhuo.annotations.Optional;
import com.squareup.javapoet.ClassName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by benny on 1/31/18.
 */

public class OptionalField extends RequiredField {

    private Object value;
    private TypeMirror creator;

    public OptionalField(Symbol.VarSymbol symbol) {
        super(symbol, false);
        Optional optional = symbol.getAnnotation(Optional.class);
        retrieveDefaultValue(symbol.type, optional);
        try {
            optional.creator();
        } catch (MirroredTypeException e) {
            creator = e.getTypeMirror();
        }
    }

    private void retrieveDefaultValue(Type type, Optional optional) {
        switch (type.getKind()) {
            case BOOLEAN:
                value = optional.booleanValue();
                break;
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case CHAR:
                value = optional.intValue();
                break;
            case FLOAT:
            case DOUBLE:
                value = optional.floatValue();
                break;
            default:
                if (ClassName.get(type).equals(ClassName.get(String.class))) {
                    //以字面量形式注入，所以字符串要加引号
                    value = "\"" + optional.stringValue() + "\"";
                }
        }

    }

    public Object getValue() {
        return value;
    }

    public TypeMirror getCreator() {
        return creator;
    }
}
