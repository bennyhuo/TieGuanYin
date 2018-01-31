package com.bennyhuo.compiler;

import com.bennyhuo.activitybuilder.ActivityBuilder;
import com.bennyhuo.activitybuilder.OnActivityCreateListener;
import com.bennyhuo.utils.Utils;
import com.squareup.javapoet.ClassName;
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
    private MethodSpec.Builder onActivityCreatedMethodBuilder;
    private ActivityClass activityClass;

    public InjectMethod(ActivityClass activityClass) {
        this.activityClass = activityClass;
        injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID);

        onActivityCreatedMethodBuilder = MethodSpec.methodBuilder("onActivityCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.app", "Activity"), "activity")
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .returns(TypeName.VOID)
                .beginControlFlow("if(activity instanceof $T)", activityClass.getType())
                .addStatement("$T typedActivity = ($T) activity", activityClass.getType(), activityClass.getType())
                .addStatement("$T extras = activity.getIntent().getExtras()", ClassName.get("android.os", "Bundle"));
    }

    public void visitBinding(RequiredField binding){
        String name = binding.getName();
        Set<Modifier> modifiers = binding.getSymbol().getModifiers();
        Type type = binding.getSymbol().type;
        TypeName typeName;
        if (type.isPrimitive()) {
            typeName = com.bennyhuo.compiler.Utils.toWrapperType(type);
        } else {
            typeName = TypeName.get(type);
        }

        if(!binding.isRequired()) {
            OptionalField optionalField = ((OptionalField)binding);
            onActivityCreatedMethodBuilder.addStatement("$T $LValue = $T.<$T>get(extras, $S, $L)", typeName, name, Utils.class, typeName, name, optionalField.getValue());
            onActivityCreatedMethodBuilder.beginControlFlow("if($LValue == null)", name)
                    .addStatement("$LValue = ($T)(new $T().create($T.class))", name, type, optionalField.getCreator(), typeName)
                    .endControlFlow();
        } else {
            onActivityCreatedMethodBuilder.addStatement("$T $LValue = $T.<$T>get(extras, $S)", typeName, name, Utils.class, typeName, name);
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            onActivityCreatedMethodBuilder.addStatement("typedActivity.set$L($LValue)", com.bennyhuo.compiler.Utils.capitalize(name), name);
        } else {
            onActivityCreatedMethodBuilder.addStatement("typedActivity.$L = $LValue", name, name);
        }
    }

    public void end(){
        onActivityCreatedMethodBuilder.addStatement("$T.INSTANCE.removeOnActivityCreateListener(this)", ActivityBuilder.class)
                .endControlFlow();
        TypeSpec onActivityCreateListenerType = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(OnActivityCreateListener.class)
                .addMethod(onActivityCreatedMethodBuilder.build())
                .build();
        injectMethodBuilder.addStatement("$T.INSTANCE.addOnActivityCreateListener($L)", ActivityBuilder.class, onActivityCreateListenerType);

    }

    public MethodSpec build() {
        return injectMethodBuilder.build();
    }
}
