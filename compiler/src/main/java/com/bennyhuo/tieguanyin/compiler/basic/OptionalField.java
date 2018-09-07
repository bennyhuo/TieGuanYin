package com.bennyhuo.tieguanyin.compiler.basic;

import com.bennyhuo.tieguanyin.annotations.Optional;
import com.squareup.javapoet.ClassName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

/**
 * Created by benny on 1/31/18.
 */

public class OptionalField extends RequiredField {
    private static final String CONSTS_OPTIONAL_FIELD_PREFIX = "OPTIONAL_";

    private Object value;

    public OptionalField(Symbol.VarSymbol symbol) {
        super(symbol);
        Optional optional = symbol.getAnnotation(Optional.class);
        retrieveDefaultValue(symbol.type, optional);
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

    @Override
    public String getPrefix() {
        return CONSTS_OPTIONAL_FIELD_PREFIX;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int compareTo(RequiredField requiredField) {
        if(requiredField instanceof OptionalField){
            return super.compareTo(requiredField);
        } else {
            //如果与 RequiredField 比较，Optional 永远排在后面
            return 1;
        }
    }
}
