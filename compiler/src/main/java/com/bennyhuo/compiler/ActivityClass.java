package com.bennyhuo.compiler;

import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Created by benny on 1/29/18.
 */

public class ActivityClass {
    private TypeElement type;
    TreeSet<ParamBinding> optionalBindings = new TreeSet<>();
    TreeSet<ParamBinding> requiredBindings = new TreeSet<>();

    public ActivityClass(TypeElement type) {
        this.type = type;
    }

    public void addSymbol(ParamBinding binding) {
        if (binding.isRequired()) {
            requiredBindings.add(binding);
        } else {
            optionalBindings.add(binding);
        }
    }

    public Set<ParamBinding> getAllBindings(){
        Set<ParamBinding> set = new TreeSet<>();
        set.addAll(requiredBindings);
        set.addAll(optionalBindings);
        return set;
    }

    public Set<ParamBinding> getRequiredBindings(){
        return requiredBindings;
    }

    public Set<ParamBinding> getOptionalBindings() {
        return optionalBindings;
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
