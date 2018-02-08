package com.bennyhuo.tieguanyin.compiler.activity;

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
                .addParameter(JavaTypes.ACTIVITY, "activity")
                .addParameter(JavaTypes.BUNDLE, "savedInstanceState")
                .returns(TypeName.VOID)
                .beginControlFlow("if(activity instanceof $T)", activityClass.getType())
                .addStatement("$T typedActivity = ($T) activity", activityClass.getType(), activityClass.getType())
                .addStatement("$T extras = activity.getIntent().getExtras()", JavaTypes.BUNDLE);
    }

    public void visitField(RequiredField binding){
        String name = binding.getName();
        Set<Modifier> modifiers = binding.getSymbol().getModifiers();
        Type type = binding.getSymbol().type;
        TypeName typeName = TypeName.get(type).box();

        if(!binding.isRequired()) {
            OptionalField optionalField = ((OptionalField)binding);
            onActivityCreatedMethodBuilder.addStatement("$T $LValue = $T.<$T>get(extras, $S, $L)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name, optionalField.getValue());
        } else {
            onActivityCreatedMethodBuilder.addStatement("$T $LValue = $T.<$T>get(extras, $S)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name);
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            onActivityCreatedMethodBuilder.addStatement("typedActivity.set$L($LValue)", Utils.capitalize(name), name);
        } else {
            onActivityCreatedMethodBuilder.addStatement("typedActivity.$L = $LValue", name, name);
        }
    }

    public void end(){
        onActivityCreatedMethodBuilder.addStatement("$T.INSTANCE.removeOnActivityCreateListener(this)", JavaTypes.ACTIVITY_BUILDER)
                .endControlFlow();
        TypeSpec onActivityCreateListenerType = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(JavaTypes.ON_ACTIVITY_CREATE_LISTENER)
                .addMethod(onActivityCreatedMethodBuilder.build())
                .build();
        injectMethodBuilder.addStatement("$T.INSTANCE.addOnActivityCreateListener($L)", JavaTypes.ACTIVITY_BUILDER, onActivityCreateListenerType);

    }

    public MethodSpec build() {
        return injectMethodBuilder.build();
    }
}
