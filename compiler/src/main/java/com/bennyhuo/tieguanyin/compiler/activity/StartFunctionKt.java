package com.bennyhuo.tieguanyin.compiler.activity;

import com.bennyhuo.tieguanyin.compiler.basic.RequiredField;
import com.bennyhuo.tieguanyin.compiler.result.ActivityResultClass;
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.ParameterSpec;
import com.squareup.kotlinpoet.TypeName;

import kotlin.Unit;

/**
 * Created by benny on 1/31/18.
 */

public class StartFunctionKt {

    private String builderClassName;
    private FunSpec.Builder funBuilderForContext;
    private FunSpec.Builder funBuilderForView;
    private FunSpec.Builder funBuilderForFragment;
    private ActivityClass activityClass;

    public StartFunctionKt(ActivityClass activityClass, String builderClassName, String name) {

        this.activityClass = activityClass;
        this.builderClassName = builderClassName;
        funBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.CONTEXT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T(this, %T::class.java)", KotlinTypes.INTENT, activityClass.getType());

        funBuilderForView = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);

        funBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);
    }

    public void visitField(RequiredField binding){
        String name = binding.getName();
        TypeName className = KotlinTypes.toKotlinType(binding.getSymbol().type);
        if(!binding.isRequired()){
            className = className.asNullable();
            funBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
        } else {
            funBuilderForContext.addParameter(name, className);
        }
        funBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name);
    }

    public void endWithResult(ActivityResultClass activityResultClass){
        funBuilderForContext.beginControlFlow("if(this is %T)", KotlinTypes.ACTIVITY);
        if(activityResultClass != null){
            funBuilderForContext
                    .beginControlFlow("if(%N == null)", activityResultClass.getListenerName())
                    .addStatement("startActivityForResult(intent, 1)")
                    .endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("%T.INSTANCE.startActivityForResult(this, intent, %L)", KotlinTypes.ACTIVITY_BUILDER, activityResultClass.createOnResultListenerObjectKt())
                    .endControlFlow()
                    .addParameter(
                            ParameterSpec.builder(activityResultClass.getListenerName(), activityResultClass.getListenerClassKt().asNullable())
                                    .defaultValue("null").build());
        } else {
            funBuilderForContext.addStatement("startActivity(intent)");
        }
        funBuilderForContext.endControlFlow()
                .beginControlFlow("else")
                .addStatement("intent.addFlags(%T.FLAG_ACTIVITY_NEW_TASK)", KotlinTypes.INTENT)
                .addStatement("startActivity(intent)")
                .endControlFlow();
        funBuilderForContext.addStatement("%T.inject()", new com.squareup.kotlinpoet.ClassName(activityClass.packageName, builderClassName));

        StringBuilder paramBuilder = new StringBuilder();
        for (ParameterSpec parameterSpec : funBuilderForContext.getParameters$kotlinpoet()) {
            paramBuilder.append(parameterSpec.getName()).append(",");
            funBuilderForView.addParameter(parameterSpec);
            funBuilderForFragment.addParameter(parameterSpec);
        }
        if(paramBuilder.length() > 0) {
            paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        }
        funBuilderForView.addStatement("context.%L(%L)", funBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
        funBuilderForFragment.addStatement("activity?.%L(%L)", funBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
    }

    public FunSpec buildForContext() {
        return funBuilderForContext.build();
    }

    public FunSpec buildForView() {
        return funBuilderForView.build();
    }

    public FunSpec buildForFragment() {
        return funBuilderForFragment.build();
    }
}
