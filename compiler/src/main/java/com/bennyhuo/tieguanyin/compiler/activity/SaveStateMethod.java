package com.bennyhuo.tieguanyin.compiler.activity;

import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.bennyhuo.tieguanyin.compiler.utils.Utils;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class SaveStateMethod {

    private MethodSpec.Builder methodBuilder;
    private ArrayList<RequiredField> visitedBindings = new ArrayList<>();

    public SaveStateMethod(ActivityClass activityClass) {
        methodBuilder = MethodSpec.methodBuilder("saveState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.ACTIVITY, "activity")
                .addParameter(JavaTypes.BUNDLE, "outState")
                .beginControlFlow("if(activity instanceof $T)", activityClass.getType())
        .addStatement("$T typedActivity = ($T) activity", activityClass.getType(), activityClass.getType());

        methodBuilder.addStatement("$T intent = new $T()", JavaTypes.INTENT, JavaTypes.INTENT);
    }

    public void visitField(RequiredField binding) {
        String name = binding.getName();
        Set<Modifier> modifiers = binding.getSymbol().getModifiers();
        if(modifiers.contains(Modifier.PRIVATE)){
            methodBuilder.addStatement("intent.putExtra($S, typedActivity.get$L())", name, Utils.capitalize(name));
        } else {
            methodBuilder.addStatement("intent.putExtra($S, typedActivity.$L)", name, name);
        }
        visitedBindings.add(binding);
    }

    public void end() {
        methodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow();
    }

    public MethodSpec build() {
        return methodBuilder.build();
    }
}
