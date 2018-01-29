package com.bennyhuo.compiler;

import com.sun.tools.javac.code.Symbol;

import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Created by benny on 1/29/18.
 */

public class ActivityClass {
    private TypeElement type;
    TreeSet<ParamBinding> bindings = new TreeSet<>();

    public ActivityClass(TypeElement type) {
        this.type = type;
    }

    public void addSymbol(Symbol.VarSymbol symbol){
        System.out.println("Add Symbol: " + symbol);
        bindings.add(new ParamBinding(symbol));
    }

    public Set<ParamBinding> getBindings(){
        return bindings;
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
