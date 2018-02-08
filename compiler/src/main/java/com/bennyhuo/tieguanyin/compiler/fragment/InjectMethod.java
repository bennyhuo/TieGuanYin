package com.bennyhuo.tieguanyin.compiler.fragment;

import com.bennyhuo.tieguanyin.compiler.basic.OptionalField;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.bennyhuo.tieguanyin.compiler.utils.Utils;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Type;

import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class InjectMethod {

    private MethodSpec.Builder injectMethodBuilder;
    private MethodSpec.Builder onFragmentCreatedMethodBuilder;

    public InjectMethod(FragmentClass fragmentClass) {
        injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID);

        onFragmentCreatedMethodBuilder = MethodSpec.methodBuilder("onFragmentCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JavaTypes.SUPPORT_FRAGMENT, "fragment")
                .addParameter(JavaTypes.BUNDLE, "savedInstanceState")
                .returns(TypeName.VOID)
                .beginControlFlow("if(fragment instanceof $T)", fragmentClass.getType())
                .addStatement("$T typedFragment = ($T) fragment", fragmentClass.getType(), fragmentClass.getType())
                .addStatement("$T args = typedFragment.getArguments()", JavaTypes.BUNDLE)
                .beginControlFlow("if(args != null)");
    }

    public void visitField(RequiredField binding){
        String name = binding.getName();
        Set<Modifier> modifiers = binding.getSymbol().getModifiers();
        Type type = binding.getSymbol().type;
        TypeName typeName = TypeName.get(type).box();

        if(!binding.isRequired()) {
            OptionalField optionalField = ((OptionalField)binding);
            onFragmentCreatedMethodBuilder.addStatement("$T $LValue = $T.<$T>get(args, $S, $L)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name, optionalField.getValue());
        } else {
            onFragmentCreatedMethodBuilder.addStatement("$T $LValue = $T.<$T>get(args, $S)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name);
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            onFragmentCreatedMethodBuilder.addStatement("typedFragment.set$L($LValue)", Utils.capitalize(name), name);
        } else {
            onFragmentCreatedMethodBuilder.addStatement("typedFragment.$L = $LValue", name, name);
        }
    }

    public void end(){
        onFragmentCreatedMethodBuilder.endControlFlow();
        onFragmentCreatedMethodBuilder.addStatement("$T.INSTANCE.removeOnFragmentCreateListener(this)", JavaTypes.FRAGMENT_BUILDER)
                .endControlFlow();
        TypeSpec onFragmentCreateListenerType = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(JavaTypes.ON_FRAGMENT_CREATE_LISTENER)
                .addMethod(onFragmentCreatedMethodBuilder.build())
                .build();
        injectMethodBuilder.addStatement("$T.INSTANCE.addOnFragmentCreateListener($L)", JavaTypes.FRAGMENT_BUILDER, onFragmentCreateListenerType);

    }

    public MethodSpec build() {
        return injectMethodBuilder.build();
    }
}
