package com.bennyhuo.compiler.basic;

import com.bennyhuo.activitybuilder.runtime.core.ActivityBuilder;
import com.bennyhuo.compiler.result.ActivityResultClass;
import com.bennyhuo.compiler.utils.KotlinTypes;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.ParameterSpec;
import com.squareup.kotlinpoet.TypeName;

import kotlin.Unit;

/**
 * Created by benny on 1/31/18.
 */

public class OpenMethodKt {

    private String builderClassName;
    private FunSpec.Builder methodBuilder;
    private ActivityClass activityClass;

    public OpenMethodKt(ActivityClass activityClass, String builderClassName, String name) {

        this.activityClass = activityClass;
        this.builderClassName = builderClassName;
        methodBuilder = FunSpec.builder(name)
                .receiver(KotlinTypes.CONTEXT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addStatement("%T.INSTANCE.init(this)", ActivityBuilder.class);

        methodBuilder.addStatement("val intent = %T(this, %T::class.java)", KotlinTypes.INTENT, activityClass.getType());
    }

    public void visitField(RequiredField binding){
        String name = binding.getName();
        TypeName className = KotlinTypes.toKotlinType(binding.getSymbol().type);
        if(!binding.isRequired()){
            className = className.asNullable();
            methodBuilder.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
        } else {
            methodBuilder.addParameter(name, className);
        }
        methodBuilder.addStatement("intent.putExtra(%S, %L)", name, name);
    }

    public void endWithResult(ActivityResultClass activityResultClass){
        methodBuilder.beginControlFlow("if(this is %T)", KotlinTypes.ACTIVITY);
        if(activityResultClass != null){
            methodBuilder.addStatement("%T.INSTANCE.startActivityForResult(this, intent, %L)", ActivityBuilder.class, activityResultClass.createOnResultListenerObjectKt());
            methodBuilder.addParameter(activityResultClass.getListenerName(), activityResultClass.getListenerClassKt().asNullable());
        } else {
            methodBuilder.addStatement("startActivity(intent)");
        }
        methodBuilder.endControlFlow()
                .beginControlFlow("else")
                .addStatement("intent.addFlags(%T.FLAG_ACTIVITY_NEW_TASK)", KotlinTypes.INTENT)
                .addStatement("startActivity(intent)")
                .endControlFlow();
        methodBuilder.addStatement("%T.inject()", new com.squareup.kotlinpoet.ClassName(activityClass.packageName, builderClassName));
    }

    public FunSpec build() {
        return methodBuilder.build();
    }
}
