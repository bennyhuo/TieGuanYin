package com.bennyhuo.compiler;

import com.bennyhuo.annotations.Optional;
import com.bennyhuo.annotations.Required;
import com.sun.tools.javac.code.Symbol;

/**
 * Created by benny on 1/29/18.
 */

public class ParamBinding implements Comparable<ParamBinding>{
    private final Symbol.VarSymbol symbol;
    private final String name;
    private final boolean isRequired;

    public ParamBinding(Symbol.VarSymbol symbol) {
        this(symbol, true);
    }

    public ParamBinding(Symbol.VarSymbol symbol, boolean isRequired) {
        this.symbol = symbol;
        this.isRequired = isRequired;
        if(isRequired){
            name = symbol.getAnnotation(Required.class).value();
        } else {
            name = symbol.getAnnotation(Optional.class).value();
        }
    }

    public Symbol.VarSymbol getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public int compareTo(ParamBinding paramBinding) {
        return name.compareTo(paramBinding.name);
    }
}
