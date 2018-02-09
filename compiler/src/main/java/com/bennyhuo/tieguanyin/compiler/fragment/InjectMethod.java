package com.bennyhuo.tieguanyin.compiler.fragment;

import com.bennyhuo.tieguanyin.compiler.basic.OptionalField;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.bennyhuo.tieguanyin.compiler.utils.Utils;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Type;

import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class InjectMethod {

    private MethodSpec.Builder injectMethodBuilder;

    public InjectMethod(FragmentClass fragmentClass) {
        injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addParameter(JavaTypes.SUPPORT_FRAGMENT, "fragment")
                .addParameter(JavaTypes.BUNDLE, "savedInstanceState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .beginControlFlow("if(fragment instanceof $T)", fragmentClass.getType())
                .addStatement("$T typedFragment = ($T) fragment", fragmentClass.getType(), fragmentClass.getType())
                .addStatement("$T args = savedInstanceState == null ? typedFragment.getArguments() : savedInstanceState", JavaTypes.BUNDLE)
                .beginControlFlow("if(args != null)");
    }

    public void visitField(RequiredField binding){
        String name = binding.getName();
        Set<Modifier> modifiers = binding.getSymbol().getModifiers();
        Type type = binding.getSymbol().type;
        TypeName typeName = TypeName.get(type).box();

        if(!binding.isRequired()) {
            OptionalField optionalField = ((OptionalField)binding);
            injectMethodBuilder.addStatement("$T $LValue = $T.<$T>get(args, $S, $L)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name, optionalField.getValue());
        } else {
            injectMethodBuilder.addStatement("$T $LValue = $T.<$T>get(args, $S)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name);
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            injectMethodBuilder.addStatement("typedFragment.set$L($LValue)", Utils.capitalize(name), name);
        } else {
            injectMethodBuilder.addStatement("typedFragment.$L = $LValue", name, name);
        }
    }

    public void end(){
        injectMethodBuilder.endControlFlow().endControlFlow();
    }

    public MethodSpec build() {
        return injectMethodBuilder.build();
    }
}
