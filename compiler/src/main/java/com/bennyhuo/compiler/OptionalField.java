package com.bennyhuo.compiler;

import com.bennyhuo.annotations.Optional;
import com.sun.tools.javac.code.Symbol;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by benny on 1/31/18.
 */

public class OptionalField extends RequiredField {

    private String value;
    private TypeMirror creator;

    public OptionalField(Symbol.VarSymbol symbol) {
        super(symbol, false);
        Optional optional = symbol.getAnnotation(Optional.class);
        value = optional.value();
        try {
            optional.creator();
        } catch (MirroredTypeException e) {
            creator = e.getTypeMirror();
        }
    }

    public String getValue() {
        return value;
    }

    public TypeMirror getCreator() {
        return creator;
    }
}
