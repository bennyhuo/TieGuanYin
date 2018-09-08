package com.bennyhuo.tieguanyin.compiler.activity.methods;

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass;
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

public class InjectMethodBuilder {

    private ActivityClass activityClass;

    public InjectMethodBuilder(ActivityClass activityClass) {
        this.activityClass = activityClass;
    }

    public void build(TypeSpec.Builder typeBuilder){
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addParameter(JavaTypes.ACTIVITY, "activity")
                .addParameter(JavaTypes.BUNDLE, "savedInstanceState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .beginControlFlow("if(activity instanceof $T)", activityClass.getType())
                .addStatement("$T typedActivity = ($T) activity", activityClass.getType(), activityClass.getType())
                .addStatement("$T extras = savedInstanceState == null ? activity.getIntent().getExtras() : savedInstanceState", JavaTypes.BUNDLE)
                .beginControlFlow("if(extras != null)");

        for (RequiredField requiredField : activityClass.getRequiredFieldsRecursively()) {
            String name = requiredField.getName();
            Set<Modifier> modifiers = requiredField.getSymbol().getModifiers();
            Type type = requiredField.getSymbol().type;
            TypeName typeName = TypeName.get(type).box();

            if(requiredField instanceof OptionalField) {
                OptionalField optionalField = ((OptionalField)requiredField);
                injectMethodBuilder.addStatement("$T $LValue = $T.<$T>get(extras, $S, $L)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name, optionalField.getValue());
            } else {
                injectMethodBuilder.addStatement("$T $LValue = $T.<$T>get(extras, $S)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name);
            }
            if (modifiers.contains(Modifier.PRIVATE)) {
                injectMethodBuilder.addStatement("typedActivity.set$L($LValue)", Utils.capitalize(name), name);
            } else {
                injectMethodBuilder.addStatement("typedActivity.$L = $LValue", name, name);
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow();

        typeBuilder.addMethod(injectMethodBuilder.build());
    }
}
