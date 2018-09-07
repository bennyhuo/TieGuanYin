package com.bennyhuo.tieguanyin.compiler.basic;

import com.sun.tools.javac.code.Symbol;

/**
 * Created by benny on 1/29/18.
 */

public class RequiredField implements Comparable<RequiredField>{
    private static final String CONSTS_REQUIRED_FIELD_PREFIX = "REQUIRED_";

    private final Symbol.VarSymbol symbol;
    private final String name;

    public RequiredField(Symbol.VarSymbol symbol) {
        this.symbol = symbol;
        name = symbol.getQualifiedName().toString();
    }

    public Symbol.VarSymbol getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getPrefix(){
        return CONSTS_REQUIRED_FIELD_PREFIX;
    }

    @Override
    public int compareTo(RequiredField requiredField) {
        return name.compareTo(requiredField.name);
    }
}
