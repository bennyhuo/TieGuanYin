package com.bennyhuo.compiler;

import com.sun.tools.javac.code.Symbol;

/**
 * Created by benny on 1/29/18.
 */

public class RequiredField implements Comparable<RequiredField>{
    private final Symbol.VarSymbol symbol;
    private final String name;
    private final boolean isRequired;

    public RequiredField(Symbol.VarSymbol symbol) {
        this(symbol, true);
    }

    public RequiredField(Symbol.VarSymbol symbol, boolean isRequired) {
        this.symbol = symbol;
        this.isRequired = isRequired;
        name = symbol.getQualifiedName().toString();
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
    public int compareTo(RequiredField paramBinding) {
        return name.compareTo(paramBinding.name);
    }
}
