package com.bennyhuo.compiler;

import com.sun.tools.javac.code.Symbol;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Created by benny on 1/29/18.
 */

public class ActivityClass {
    private TypeElement type;
    HashSet<Symbol.VarSymbol> symbols = new HashSet<>();

    public ActivityClass(TypeElement type) {
        this.type = type;
    }

    public void addSymbol(Symbol.VarSymbol symbol){
        System.out.println("Add Symbol: " + symbol);
        symbols.add(symbol);
    }

    public Set<Symbol.VarSymbol> getSymbols(){
        return symbols;
    }

    public TypeElement getType() {
        return type;
    }

    public String getPackage(){
        if(type.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)){
            return type.getEnclosingElement().asType().toString();
        }else{
            throw new IllegalArgumentException(type.getEnclosingElement().toString());
        }
    }
}
