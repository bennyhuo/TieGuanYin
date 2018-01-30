package com.bennyhuo.compiler;

import com.bennyhuo.activitybuilder.ActivityBuilder;
import com.bennyhuo.activitybuilder.OnActivityCreateListener;
import com.bennyhuo.utils.BundleUtils;
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

    public void visitBinding(ParamBinding binding){
        String name = binding.getName();
        Set<Modifier> modifiers = binding.getSymbol().getModifiers();
        Type type = binding.getSymbol().type;
        TypeName typeName;
        if (type.isPrimitive()) {
            typeName = Utils.toWrapperType(type);
        } else {
            typeName = TypeName.get(type);
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            onActivityCreatedMethodBuilder.addStatement("typedActivity.set$L($T.<$T>get(extras, $S))", Utils.capitalize(name), BundleUtils.class, typeName, name);
        } else {
            onActivityCreatedMethodBuilder.addStatement("typedActivity.$L = $T.<$T>get(extras, $S)", name, BundleUtils.class, typeName, name);
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
