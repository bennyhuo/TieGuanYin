package com.bennyhuo.compiler.activity;

import com.bennyhuo.compiler.basic.RequiredField;
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
    private FunSpec.Builder openExtFunBuilderForContext;
    private FunSpec.Builder openExtFunBuilderForView;
    private FunSpec.Builder openExtFunBuilderForFragment;
    private ActivityClass activityClass;

    public OpenMethodKt(ActivityClass activityClass, String builderClassName, String name) {

        this.activityClass = activityClass;
        this.builderClassName = builderClassName;
        openExtFunBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.CONTEXT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T(this, %T::class.java)", KotlinTypes.INTENT, activityClass.getType());

        openExtFunBuilderForView = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);

        openExtFunBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit.class);
    }

    public void visitField(RequiredField binding){
        String name = binding.getName();
        TypeName className = KotlinTypes.toKotlinType(binding.getSymbol().type);
        if(!binding.isRequired()){
            className = className.asNullable();
            openExtFunBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build());
        } else {
            openExtFunBuilderForContext.addParameter(name, className);
        }
        openExtFunBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name);
    }

    public void endWithResult(ActivityResultClass activityResultClass){
        openExtFunBuilderForContext.beginControlFlow("if(this is %T)", KotlinTypes.ACTIVITY);
        if(activityResultClass != null){
            openExtFunBuilderForContext
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
            openExtFunBuilderForContext.addStatement("startActivity(intent)");
        }
        openExtFunBuilderForContext.endControlFlow()
                .beginControlFlow("else")
                .addStatement("intent.addFlags(%T.FLAG_ACTIVITY_NEW_TASK)", KotlinTypes.INTENT)
                .addStatement("startActivity(intent)")
                .endControlFlow();
        openExtFunBuilderForContext.addStatement("%T.inject()", new com.squareup.kotlinpoet.ClassName(activityClass.packageName, builderClassName));

        StringBuilder paramBuilder = new StringBuilder();
        for (ParameterSpec parameterSpec : openExtFunBuilderForContext.getParameters$kotlinpoet()) {
            paramBuilder.append(parameterSpec.getName()).append(",");
            openExtFunBuilderForView.addParameter(parameterSpec);
            openExtFunBuilderForFragment.addParameter(parameterSpec);
        }
        if(paramBuilder.length() > 0) {
            paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        }
        openExtFunBuilderForView.addStatement("context.%L(%L)", openExtFunBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
        openExtFunBuilderForFragment.addStatement("activity?.%L(%L)", openExtFunBuilderForContext.getName$kotlinpoet(), paramBuilder.toString());
    }

    public FunSpec buildForContext() {
        return openExtFunBuilderForContext.build();
    }

    public FunSpec buildForView() {
        return openExtFunBuilderForView.build();
    }

    public FunSpec buildForFragment() {
        return openExtFunBuilderForFragment.build();
    }
}
