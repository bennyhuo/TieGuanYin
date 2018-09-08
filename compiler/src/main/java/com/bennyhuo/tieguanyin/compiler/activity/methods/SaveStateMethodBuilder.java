package com.bennyhuo.tieguanyin.compiler.activity.methods;

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass;
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes;
import com.bennyhuo.tieguanyin.compiler.utils.Utils;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by benny on 1/31/18.
 */

public class SaveStateMethodBuilder {

    private ActivityClass activityClass;

    public SaveStateMethodBuilder(ActivityClass activityClass) {
        this.activityClass = activityClass;
    }

    public void build(TypeSpec.Builder typeBuilder){
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("saveState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.ACTIVITY, "activity")
                .addParameter(JavaTypes.BUNDLE, "outState")
                .beginControlFlow("if(activity instanceof $T)", activityClass.getType())
                .addStatement("$T typedActivity = ($T) activity", activityClass.getType(), activityClass.getType());

        methodBuilder.addStatement("$T intent = new $T()", JavaTypes.INTENT, JavaTypes.INTENT);

        for (RequiredField requiredField : activityClass.getRequiredFieldsRecursively()) {
            String name = requiredField.getName();
            Set<Modifier> modifiers = requiredField.getSymbol().getModifiers();
            if(modifiers.contains(Modifier.PRIVATE)){
                methodBuilder.addStatement("intent.putExtra($S, typedActivity.get$L())", name, Utils.capitalize(name));
            } else {
                methodBuilder.addStatement("intent.putExtra($S, typedActivity.$L)", name, name);
            }
        }

        methodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow();

        typeBuilder.addMethod(methodBuilder.build());
    }

}
